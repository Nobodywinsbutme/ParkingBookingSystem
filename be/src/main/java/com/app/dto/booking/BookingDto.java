package com.app.dto.booking;

import com.app.domain.enums.BookingStatus;

import java.time.Instant;
import java.util.List;

public record BookingDto(
        String id,
        String userId,
        String parkingAreaId,
        String parkingSlotId,
        Instant startAt,
        Instant endAt,
        Instant checkedInAt,
        Instant checkedOutAt,
        BookingStatus status,
        String currency,
        Integer amountSubtotal,
        Integer amountTotal,
        String notes,
        Instant cancelledAt,
        Instant createdAt,
        Instant updatedAt,
        List<PaymentDto> payments
) {}
