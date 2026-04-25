package com.app.dto.parking;

import com.app.domain.enums.SlotStatus;

public record SlotAvailabilityJson(
        String id,
        String code,
        String floor,
        SlotStatus status,
        boolean selectableForRange
) {}
