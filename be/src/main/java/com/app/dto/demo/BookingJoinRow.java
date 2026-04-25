package com.app.dto.demo;

import java.time.Instant;

/**
 * JPQL {@code select new} projection for a multi-table join over booking, user, slot, and area.
 */
public record BookingJoinRow(
        String bookingId,
        String userEmail,
        String slotCode,
        String parkingAreaName,
        Instant startAt,
        Instant endAt
) {
}
