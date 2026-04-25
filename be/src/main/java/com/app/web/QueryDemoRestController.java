package com.app.web;

import com.app.domain.enums.BookingStatus;
import com.app.dto.demo.BookingJoinRow;
import com.app.dto.demo.BookingStatsDemoView;
import com.app.dto.demo.OverlapCheckDemoView;
import com.app.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * Read-only API for database course demos (JOIN, EXISTS, GROUP BY). No impact on business flows.
 */
@RestController
@RequestMapping("/api/demo/queries")
public class QueryDemoRestController {

    private static final List<BookingStatus> OVERLAP_STATUSES = List.of(
            BookingStatus.PENDING, BookingStatus.CONFIRMED);

    private final BookingRepository bookingRepository;

    public QueryDemoRestController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    /**
     * JPQL: joined booking + user + slot + area.
     */
    @GetMapping("/bookings/join")
    public List<BookingJoinRow> bookingsJoin() {
        return bookingRepository.findAllWithUserSlotAreaJoin();
    }

    /**
     * Overlap: JPQL count &gt; 0 vs native {@code EXISTS} (same time window, PENDING/CONFIRMED).
     */
    @GetMapping("/bookings/overlap-check")
    public OverlapCheckDemoView overlapCheck(
            @RequestParam String slotId,
            @RequestParam String startAt,
            @RequestParam String endAt) {
        if (slotId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slotId is required");
        }
        Instant start;
        Instant end;
        try {
            start = Instant.parse(startAt);
            end = Instant.parse(endAt);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "startAt and endAt must be ISO-8601 instants, e.g. 2025-12-01T10:00:00Z", e);
        }
        boolean jpql = bookingRepository.overlapsUsingCountJpql(slotId, start, end, OVERLAP_STATUSES);
        boolean nativeEx = bookingRepository.existsOverlappingBookingNativeEx(slotId, start, end);
        return new OverlapCheckDemoView(slotId, start, end, jpql, nativeEx);
    }

    /**
     * GROUP BY: counts by status and by parking area.
     */
    @GetMapping("/bookings/stats")
    public BookingStatsDemoView stats() {
        return new BookingStatsDemoView(
                bookingRepository.countBookingsGroupByStatus(),
                bookingRepository.countBookingsGroupByParkingArea());
    }
}
