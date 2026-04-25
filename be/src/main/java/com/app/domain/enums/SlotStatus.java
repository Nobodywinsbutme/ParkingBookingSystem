package com.app.domain.enums;

/**
 * Physical slot availability for new bookings. OVERLAP queries still enforce time ranges;
 * BOOKED can be used to administratively block a slot without a time-based row.
 */
public enum SlotStatus {
    AVAILABLE,
    BOOKED,
    MAINTENANCE
}
