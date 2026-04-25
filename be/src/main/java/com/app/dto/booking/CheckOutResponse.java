package com.app.dto.booking;

import java.time.Instant;

public record CheckOutResponse(String bookingId, Instant checkedOutAt, String status) {}
