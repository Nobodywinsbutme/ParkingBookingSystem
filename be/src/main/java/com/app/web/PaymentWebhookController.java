package com.app.web;

import com.app.service.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentWebhookController {

    private static final Logger log = LoggerFactory.getLogger(PaymentWebhookController.class);

    private final StripePaymentService stripePaymentService;

    public PaymentWebhookController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String stripeSignature
    ) {
        if (stripeSignature == null || stripeSignature.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Stripe-Signature");
        }
        try {
            stripePaymentService.handleWebhookPayload(payload, stripeSignature);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (IllegalStateException e) {
            log.warn("Webhook not accepted: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
        return ResponseEntity.ok("ok");
    }
}
