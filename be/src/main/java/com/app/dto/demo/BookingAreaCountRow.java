package com.app.dto.demo;

/**
 * Result row for {@code GROUP BY} parking area in JPQL.
 */
public record BookingAreaCountRow(String parkingAreaId, String parkingAreaName, Long total) {
}
