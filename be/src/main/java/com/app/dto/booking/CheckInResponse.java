package com.app.dto.booking;

import java.time.Instant;

public record CheckInResponse(String bookingId, Instant checkedInAt) {}
