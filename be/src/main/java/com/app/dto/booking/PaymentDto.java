package com.app.dto.booking;

import com.app.domain.entity.PaymentEntity;

import java.time.Instant;

public record PaymentDto(
        String id,
        PaymentEntity.PaymentMethod method,
        PaymentEntity.PaymentStatus status,
        Integer amount,
        String currency,
        String providerTxnRef,
        Instant createdAt,
        Instant updatedAt
) {}
