package com.app.dto.demo;

import com.app.domain.enums.BookingStatus;

/**
 * Result row for {@code GROUP BY} booking status in JPQL.
 */
public record BookingStatusCountRow(BookingStatus status, Long total) {
}
