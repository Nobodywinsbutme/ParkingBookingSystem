package com.app.dto.booking;

public record CancelBookingResponse(
        String bookingId,
        boolean cancelled,
        boolean refunded,
        int cancelledPendingPayments
) {}
