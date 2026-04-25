package com.app.dto.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        @NotBlank String parkingAreaId,
        @NotBlank String parkingSlotId,
        @NotBlank String startAt,
        @NotBlank String endAt,
        String paymentMethod,
        @Size(max = 500) String notes
) {}
