package com.app.service;

import com.app.config.AppProperties;
import com.app.config.StripeProperties;
import com.app.domain.entity.BookingEntity;
import com.app.domain.entity.PaymentEntity;
import com.app.domain.enums.BookingStatus;
import com.app.repository.BookingRepository;
import com.app.repository.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

    private final StripeProperties stripeProperties;
    private final AppProperties appProperties;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final SlotAvailabilityNotifier slotAvailabilityNotifier;

    public StripePaymentService(
            StripeProperties stripeProperties,
            AppProperties appProperties,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            SlotAvailabilityNotifier slotAvailabilityNotifier
    ) {
        this.stripeProperties = stripeProperties;
        this.appProperties = appProperties;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.slotAvailabilityNotifier = slotAvailabilityNotifier;
    }

    public boolean isReady() {
        return stripeProperties.isConfigured();
    }

    @Transactional
    public String createCheckoutSession(String bookingId, String userId) throws StripeException {
        if (!isReady()) {
            throw new IllegalStateException("Stripe is not configured. Set STRIPE_SECRET_KEY.");
        }
        BookingEntity booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getDeletedAt() == null && userId.equals(b.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Booking is not awaiting payment.");
        }
        PaymentEntity payment = paymentRepository.findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(bookingId);
        if (payment == null
                || payment.getMethod() != PaymentEntity.PaymentMethod.STRIPE
                || payment.getStatus() != PaymentEntity.PaymentStatus.PENDING) {
            throw new IllegalStateException("No pending Stripe payment for this booking.");
        }

        String base = appProperties.getPublicBaseUrl().replaceAll("/$", "");
        String successUrl = base + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = base + "/payment/cancel?bookingId=" + bookingId;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setClientReferenceId(bookingId)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(booking.getCurrency().toLowerCase())
                                                .setUnitAmount((long) booking.getAmountTotal())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Parking booking")
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("bookingId", bookingId)
                .putMetadata("paymentId", payment.getId())
                .putMetadata("userId", userId)
                .build();

        Session session = Session.create(params);
        payment.setProviderTxnRef(session.getId());
        payment.setUpdatedAt(Instant.now());
        paymentRepository.save(payment);
        return session.getUrl();
    }

    @Transactional
    public void fulfillFromCheckoutSessionId(String sessionId, String userId) throws StripeException {
        Session session = Session.retrieve(sessionId);
        if (!"paid".equals(session.getPaymentStatus())) {
            log.warn("Checkout session {} payment_status={}", sessionId, session.getPaymentStatus());
            return;
        }
        String bookingId = resolveBookingId(session);
        BookingEntity booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Booking does not belong to current user.");
        }
        PaymentEntity payment = paymentRepository.findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(bookingId);
        if (payment == null) {
            throw new IllegalStateException("Payment missing.");
        }
        markBookingPaidFromStripe(session, booking, payment);
    }

    @Transactional
    public void abandonCheckout(String bookingId, String userId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .filter(b -> b.getDeletedAt() == null && userId.equals(b.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Booking not found."));
        if (booking.getStatus() != BookingStatus.PENDING) {
            return;
        }
        PaymentEntity payment = paymentRepository.findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(bookingId);
        if (payment != null
                && payment.getMethod() == PaymentEntity.PaymentMethod.STRIPE
                && payment.getStatus() == PaymentEntity.PaymentStatus.PENDING) {
            Instant now = Instant.now();
            payment.setStatus(PaymentEntity.PaymentStatus.FAILED);
            payment.setUpdatedAt(now);
            paymentRepository.save(payment);
        }
        Instant now = Instant.now();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(now);
        booking.setUpdatedAt(now);
        bookingRepository.save(booking);
        slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
    }

    @Transactional
    public void handleWebhookPayload(String payload, String sigHeader) throws SignatureVerificationException {
        if (!stripeProperties.isWebhookConfigured()) {
            throw new IllegalStateException("Stripe webhook secret not configured (STRIPE_WEBHOOK_SECRET).");
        }
        Event event = Webhook.constructEvent(payload, sigHeader, stripeProperties.getWebhookSecret());
        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = toSession(event);
                if (session != null && "paid".equals(session.getPaymentStatus())) {
                    String bookingId = resolveBookingId(session);
                    BookingEntity booking = bookingRepository.findById(bookingId)
                            .filter(b -> b.getDeletedAt() == null)
                            .orElse(null);
                    if (booking == null) {
                        log.warn("Webhook: booking {} not found", bookingId);
                        return;
                    }
                    PaymentEntity payment = paymentRepository.findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(
                            bookingId);
                    if (payment == null) {
                        return;
                    }
                    markBookingPaidFromStripe(session, booking, payment);
                }
            }
            case "checkout.session.expired" -> {
                Session session = toSession(event);
                if (session != null && session.getId() != null) {
                    expireCheckoutSession(session.getId());
                }
            }
            default -> log.debug("Ignoring Stripe event type {}", event.getType());
        }
    }

    private void markBookingPaidFromStripe(Session session, BookingEntity booking, PaymentEntity payment) {
        if (payment.getStatus() == PaymentEntity.PaymentStatus.PAID) {
            return;
        }
        if (payment.getStatus() != PaymentEntity.PaymentStatus.PENDING) {
            log.warn("Cannot mark paid: payment {} status {}", payment.getId(), payment.getStatus());
            return;
        }
        Instant now = Instant.now();
        payment.setStatus(PaymentEntity.PaymentStatus.PAID);
        payment.setPaidAt(now);
        payment.setUpdatedAt(now);
        String paymentIntent = session.getPaymentIntent();
        if (paymentIntent != null) {
            payment.setProviderTxnRef(paymentIntent);
        }
        paymentRepository.save(payment);

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(now);
            bookingRepository.save(booking);
        }
        slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
    }

    private void expireCheckoutSession(String stripeSessionId) {
        PaymentEntity payment = paymentRepository.findFirstByProviderTxnRefAndDeletedAtIsNull(stripeSessionId);
        if (payment == null || payment.getStatus() != PaymentEntity.PaymentStatus.PENDING) {
            return;
        }
        String bookingId = payment.getBookingId();
        BookingEntity booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getDeletedAt() != null) {
            return;
        }
        Instant now = Instant.now();
        payment.setStatus(PaymentEntity.PaymentStatus.FAILED);
        payment.setUpdatedAt(now);
        paymentRepository.save(payment);
        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(now);
            booking.setUpdatedAt(now);
            bookingRepository.save(booking);
        }
        slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
    }

    private static String resolveBookingId(Session session) {
        return Optional.ofNullable(session.getClientReferenceId())
                .orElseGet(() -> session.getMetadata() != null ? session.getMetadata().get("bookingId") : null);
    }

    private static Session toSession(Event event) {
        Optional<StripeObject> obj = event.getDataObjectDeserializer().getObject();
        if (obj.isPresent() && obj.get() instanceof Session s) {
            return s;
        }
        return null;
    }
}
