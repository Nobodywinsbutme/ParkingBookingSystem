package com.app.service;

import com.app.domain.entity.BookingEntity;
import com.app.domain.entity.ParkingSlotEntity;
import com.app.domain.entity.PaymentEntity;
import com.app.domain.enums.BookingStatus;
import com.app.domain.enums.SlotStatus;
import com.app.dto.booking.*;
import com.app.repository.*;
import com.app.support.ApiException;
import com.app.support.BookingConflictException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class BookingService {
    private static final int PRICE_PER_HOUR_MINOR = 5000;
    private static final long PENDING_EXPIRY_MS = 3L * 60L * 60L * 1000L;
    private static final ZoneId DEFAULT_BOOKING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final List<DateTimeFormatter> FALLBACK_DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("M/d/yyyy h:mm a", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a", Locale.ENGLISH)
    );

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ParkingAreaRepository parkingAreaRepository;
    private final ParkingSlotRepository parkingSlotRepository;
    private final SlotAvailabilityNotifier slotAvailabilityNotifier;

    public BookingService(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ParkingAreaRepository parkingAreaRepository,
            ParkingSlotRepository parkingSlotRepository,
            SlotAvailabilityNotifier slotAvailabilityNotifier
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.parkingAreaRepository = parkingAreaRepository;
        this.parkingSlotRepository = parkingSlotRepository;
        this.slotAvailabilityNotifier = slotAvailabilityNotifier;
    }

    public List<BookingDto> listBookings(String userId, BookingStatus status, int limit, int offset) {
        List<BookingEntity> items = status == null
                ? bookingRepository.listUserBookings(userId)
                : bookingRepository.listUserBookingsByStatus(userId, status);
        int from = Math.min(Math.max(offset, 0), items.size());
        int to = Math.min(from + Math.max(limit, 1), items.size());
        return items.subList(from, to).stream().map(this::toDto).toList();
    }

    public BookingDto getBooking(String userId, String bookingId) {
        BookingEntity booking = bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Booking not found"));
        return toDto(booking);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BookingDto createBooking(String userId, CreateBookingRequest request) {
        Instant startAt = parseInstant(request.startAt(), "Invalid startAt/endAt");
        Instant endAt = parseInstant(request.endAt(), "Invalid startAt/endAt");
        if (!endAt.isAfter(startAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "End time must be after start time.");
        }

        Instant now = Instant.now();
        if (!startAt.isAfter(now)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR",
                    "Booking start time must be in the future."
            );
        }

        if (!parkingAreaRepository.findByIdAndDeletedAtIsNullAndActiveTrue(request.parkingAreaId()).isPresent()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parking area not found or inactive.");
        }

        ParkingSlotEntity slot = parkingSlotRepository.findById(request.parkingSlotId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parking slot not found."));
        if (slot.getDeletedAt() != null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parking slot not found.");
        }
        if (!request.parkingAreaId().equals(slot.getParkingAreaId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Slot does not belong to the selected parking area.");
        }
        if (!slot.isActive()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Parking slot is not active.");
        }
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Parking slot is not available for booking.");
        }

        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        if (bookingRepository.existsOverlappingBookingForUser(userId, startAt, endAt, activeStatuses)) {
            throw new BookingConflictException("You already have another booking that overlaps this time range.");
        }
        if (bookingRepository.existsOverlappingBooking(
                request.parkingSlotId(), startAt, endAt, activeStatuses)) {
            throw new BookingConflictException("This parking slot is already booked for an overlapping time range.");
        }

        int amountSubtotal = calculateBookingAmount(startAt, endAt);
        int amountTotal = Math.max(0, amountSubtotal);

        String requestedMethod = (request.paymentMethod() == null || request.paymentMethod().isBlank())
                ? "CASH"
                : request.paymentMethod().trim().toUpperCase();
        String storedMethodRaw = requestedMethod.equals("PAYOS") ? "STRIPE" : requestedMethod;
        PaymentEntity.PaymentMethod storedMethod;
        try {
            storedMethod = PaymentEntity.PaymentMethod.valueOf(storedMethodRaw);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid booking data");
        }
        boolean requiresOnlineCheckout = storedMethod == PaymentEntity.PaymentMethod.STRIPE;

        BookingEntity booking = new BookingEntity();
        booking.setId(uuidLikeId());
        booking.setUserId(userId);
        booking.setParkingAreaId(request.parkingAreaId());
        booking.setParkingSlotId(request.parkingSlotId());
        booking.setStartAt(startAt);
        booking.setEndAt(endAt);
        booking.setStatus(requiresOnlineCheckout ? BookingStatus.PENDING : BookingStatus.CONFIRMED);
        booking.setCurrency("USD");
        booking.setAmountSubtotal(amountSubtotal);
        booking.setAmountTotal(amountTotal);
        booking.setNotes(blankToNull(request.notes()));
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        BookingEntity savedBooking = bookingRepository.save(booking);

        if (paymentRepository.existsByBookingIdAndDeletedAtIsNull(savedBooking.getId())) {
            throw new BookingConflictException("A payment already exists for this booking (data integrity).");
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setId(uuidLikeId());
        payment.setBookingId(savedBooking.getId());
        payment.setMethod(storedMethod);
        payment.setStatus(requiresOnlineCheckout ? PaymentEntity.PaymentStatus.PENDING : PaymentEntity.PaymentStatus.PAID);
        payment.setAmount(amountTotal);
        payment.setCurrency("USD");
        if (!requiresOnlineCheckout) {
            payment.setPaidAt(now);
        }
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);
        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            throw new BookingConflictException(
                    "Could not create payment: duplicate booking link or constraint violation. "
                            + ex.getMostSpecificCause().getMessage()
            );
        }

        slotAvailabilityNotifier.notifyAreaChanged(request.parkingAreaId());
        return toDto(savedBooking);
    }

    @Transactional
    public CancelBookingResponse cancelBooking(String userId, String bookingId) {
        BookingEntity booking = bookingRepository.findByIdAndUserIdAndDeletedAtIsNull(bookingId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING
                && booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Booking cannot be cancelled in current status");
        }

        Instant now = Instant.now();
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            Instant cutoff = booking.getStartAt().minus(30, ChronoUnit.MINUTES);
            if (now.isAfter(cutoff)) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "VALIDATION_ERROR",
                        "Confirmed booking can only be cancelled at least 30 minutes before start time"
                );
            }
        }

        int cancelledBooking = bookingRepository.cancelBooking(
                bookingId,
                userId,
                BookingStatus.CANCELLED,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED),
                now
        );
        if (cancelledBooking == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Booking not found or already not cancellable");
        }

        int cancelledPending = paymentRepository.updateStatusByBooking(
                bookingId,
                List.of(PaymentEntity.PaymentStatus.PENDING),
                PaymentEntity.PaymentStatus.CANCELLED,
                now
        );
        int refunded = paymentRepository.refundSucceededOnlineByBooking(bookingId, now);
        slotAvailabilityNotifier.notifyAreaChanged(booking.getParkingAreaId());
        return new CancelBookingResponse(bookingId, true, refunded > 0, cancelledPending);
    }

    @Transactional
    public CheckInResponse checkIn(String userId, String bookingId) {
        Instant now = Instant.now();
        int updated = bookingRepository.checkIn(bookingId, userId, now, BookingStatus.CONFIRMED);
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Booking not found or cannot be checked in at this time");
        }
        return new CheckInResponse(bookingId, now);
    }

    @Transactional
    public CheckOutResponse checkOut(String userId, String bookingId) {
        Instant now = Instant.now();
        int updated = bookingRepository.checkOut(
                bookingId,
                userId,
                now,
                BookingStatus.CONFIRMED,
                BookingStatus.COMPLETED
        );
        if (updated == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Booking not found or cannot be checked out");
        }
        return new CheckOutResponse(bookingId, now, BookingStatus.COMPLETED.name());
    }

    @Transactional
    public MapResult expireStalePendingBookings() {
        Instant now = Instant.now();
        Instant cutoff = now.minusMillis(PENDING_EXPIRY_MS);
        List<BookingEntity> stale = bookingRepository.findAll().stream()
                .filter(b -> b.getDeletedAt() == null)
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .filter(b -> b.getCreatedAt() != null && (b.getCreatedAt().equals(cutoff) || b.getCreatedAt().isBefore(cutoff)))
                .toList();

        if (stale.isEmpty()) return new MapResult(0);

        List<String> ids = stale.stream().map(BookingEntity::getId).toList();
        for (BookingEntity b : stale) {
            b.setStatus(BookingStatus.CANCELLED);
            b.setCancelledAt(now);
            b.setUpdatedAt(now);
            bookingRepository.save(b);
        }
        for (String bookingId : ids) {
            paymentRepository.updateStatusByBooking(
                    bookingId,
                    List.of(PaymentEntity.PaymentStatus.PENDING),
                    PaymentEntity.PaymentStatus.CANCELLED,
                    now
            );
        }
        for (BookingEntity b : stale) {
            slotAvailabilityNotifier.notifyAreaChanged(b.getParkingAreaId());
        }
        return new MapResult(ids.size());
    }

    private int calculateBookingAmount(Instant startAt, Instant endAt) {
        long durationMs = endAt.toEpochMilli() - startAt.toEpochMilli();
        long oneHourMs = 60L * 60L * 1000L;
        long durationHours = Math.max(1, (long) Math.ceil((double) durationMs / oneHourMs));
        return (int) durationHours * PRICE_PER_HOUR_MINOR;
    }

    private BookingDto toDto(BookingEntity booking) {
        List<PaymentDto> payments = paymentRepository.findByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(booking.getId())
                .stream()
                .map(p -> new PaymentDto(
                        p.getId(),
                        p.getMethod(),
                        p.getStatus(),
                        p.getAmount(),
                        p.getCurrency(),
                        p.getProviderTxnRef(),
                        p.getCreatedAt(),
                        p.getUpdatedAt()
                ))
                .toList();

        return new BookingDto(
                booking.getId(),
                booking.getUserId(),
                booking.getParkingAreaId(),
                booking.getParkingSlotId(),
                booking.getStartAt(),
                booking.getEndAt(),
                booking.getCheckedInAt(),
                booking.getCheckedOutAt(),
                booking.getStatus(),
                booking.getCurrency(),
                booking.getAmountSubtotal(),
                booking.getAmountTotal(),
                booking.getNotes(),
                booking.getCancelledAt(),
                booking.getCreatedAt(),
                booking.getUpdatedAt(),
                payments
        );
    }

    private Instant parseInstant(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errorMessage);
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            // continue with local datetime parsing below
        }

        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(DEFAULT_BOOKING_ZONE)
                    .toInstant();
        } catch (Exception ignored) {
            // continue with fallback formats below
        }

        for (DateTimeFormatter formatter : FALLBACK_DATETIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter)
                        .atZone(DEFAULT_BOOKING_ZONE)
                        .toInstant();
            } catch (Exception ignored) {
                // try next formatter
            }
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", errorMessage);
    }

    private String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String uuidLikeId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public record MapResult(int cancelledCount) {}
}
