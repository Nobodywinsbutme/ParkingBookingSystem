package com.app.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeClientConfig {

    private final StripeProperties stripeProperties;

    public StripeClientConfig(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    @PostConstruct
    public void init() {
        if (stripeProperties.isConfigured()) {
            Stripe.apiKey = stripeProperties.getSecretKey();
        }
    }
}
