package com.app.dto.demo;

import java.util.List;

/**
 * Grouped booking counts for demo JSON.
 */
public record BookingStatsDemoView(
        List<BookingStatusCountRow> byStatus,
        List<BookingAreaCountRow> byParkingArea) {
}
