package com.app.web;

import com.app.security.DbUserDetails;
import com.app.service.StripePaymentService;
import com.stripe.exception.StripeException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
public class PaymentMvcController {

    private final StripePaymentService stripePaymentService;

    public PaymentMvcController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @GetMapping("/checkout")
    public String checkout(
            @RequestParam String bookingId,
            @AuthenticationPrincipal DbUserDetails user,
            RedirectAttributes redirectAttributes
    ) {
        if (!stripePaymentService.isReady()) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Online payment is not configured. Set STRIPE_SECRET_KEY and restart the app."
            );
            return "redirect:/bookings";
        }
        try {
            String url = stripePaymentService.createCheckoutSession(bookingId, user.getUserId());
            return "redirect:" + url;
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/bookings";
        } catch (StripeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Stripe error: " + ex.getMessage());
            return "redirect:/bookings";
        }
    }

    @GetMapping("/success")
    public String success(
            @RequestParam("session_id") String sessionId,
            @AuthenticationPrincipal DbUserDetails user,
            RedirectAttributes redirectAttributes
    ) {
        try {
            stripePaymentService.fulfillFromCheckoutSessionId(sessionId, user.getUserId());
            redirectAttributes.addFlashAttribute("successMessage", "Payment successful. Your booking is confirmed.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (StripeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not verify payment: " + ex.getMessage());
        }
        return "redirect:/bookings";
    }

    @GetMapping("/cancel")
    public String cancel(
            @RequestParam String bookingId,
            @AuthenticationPrincipal DbUserDetails user,
            RedirectAttributes redirectAttributes
    ) {
        try {
            stripePaymentService.abandonCheckout(bookingId, user.getUserId());
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Checkout cancelled. The pending booking has been released."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/bookings";
    }
}
