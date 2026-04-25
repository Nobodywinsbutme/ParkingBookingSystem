package com.app.dto.demo;

import java.time.Instant;

/**
 * JSON for comparing JPQL count-based and native {@code EXISTS} overlap checks in demos.
 */
public record OverlapCheckDemoView(
        String slotId,
        Instant startAt,
        Instant endAt,
        boolean jpqlCountGreaterThanZero,
        boolean nativeSqlExists) {
}
