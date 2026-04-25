package com.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public class StripeProperties {

    /**
     * Stripe secret API key (sk_test_... / sk_live_...).
     */
    private String secretKey = "";

    /**
     * Webhook signing secret (whsec_...).
     */
    private String webhookSecret = "";

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public boolean isConfigured() {
        return secretKey != null && !secretKey.isBlank();
    }

    public boolean isWebhookConfigured() {
        return webhookSecret != null && !webhookSecret.isBlank();
    }
}
