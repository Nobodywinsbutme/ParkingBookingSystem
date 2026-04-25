package com.app.repository;

import com.app.domain.entity.BookingEntity;
import com.app.domain.enums.BookingStatus;
import com.app.dto.demo.BookingAreaCountRow;
import com.app.dto.demo.BookingJoinRow;
import com.app.dto.demo.BookingStatusCountRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<BookingEntity, String> {
    ZoneId OVERLAP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Query("""
        select b from BookingEntity b
         where b.userId = :userId
           and b.deletedAt is null
         order by b.createdAt desc
    """)
    List<BookingEntity> listUserBookings(@Param("userId") String userId);

    @Query("""
        select b from BookingEntity b
         where b.userId = :userId
           and b.deletedAt is null
           and b.status = :status
         order by b.createdAt desc
    """)
    List<BookingEntity> listUserBookingsByStatus(
            @Param("userId") String userId,
            @Param("status") BookingStatus status
    );

    Optional<BookingEntity> findByIdAndUserIdAndDeletedAtIsNull(String id, String userId);

    /**
     * Overlap rule: (new.start &lt; existing.end) AND (new.end &gt; existing.start).
     * Only counts non-deleted bookings in PENDING or CONFIRMED status.
     */
    @Query("""
        select count(b) > 0 from BookingEntity b
         where b.parkingSlotId = :parkingSlotId
           and b.deletedAt is null
           and b.status in :activeStatuses
           and b.startAt < :endAt
           and b.endAt > :startAt
    """)
    boolean existsOverlappingBooking(
            @Param("parkingSlotId") String parkingSlotId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );

    default boolean existsOverlappingBooking(String parkingSlotId, LocalDateTime start, LocalDateTime end) {
        if (parkingSlotId == null || start == null || end == null) {
            return false;
        }
        Instant startAt = start.atZone(OVERLAP_ZONE).toInstant();
        Instant endAt = end.atZone(OVERLAP_ZONE).toInstant();
        List<BookingStatus> active = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        return existsOverlappingBooking(parkingSlotId, startAt, endAt, active);
    }

    @Query("""
        select count(b) > 0 from BookingEntity b
         where b.userId = :userId
           and b.deletedAt is null
           and b.status in :activeStatuses
           and b.startAt < :endAt
           and b.endAt > :startAt
    """)
    boolean existsOverlappingBookingForUser(
            @Param("userId") String userId,
            @Param("startAt") Instant startAt,
            @Param("endAt") Instant endAt,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );

    @Modifying
    @Query("""
        update BookingEntity b
           set b.status = :status,
               b.cancelledAt = :now,
               b.updatedAt = :now
         where b.id = :bookingId
           and b.userId = :userId
           and b.deletedAt is null
           and b.status in :allowed
    """)
    int cancelBooking(
            @Param("bookingId") String bookingId,
            @Param("userId") String userId,
            @Param("status") BookingStatus status,
            @Param("allowed") List<BookingStatus> allowed,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update BookingEntity b
           set b.checkedInAt = :now,
               b.updatedAt = :now
         where b.id = :bookingId
           and b.userId = :userId
           and b.deletedAt is null
           and b.status = :confirmed
           and b.checkedInAt is null
           and b.checkedOutAt is null
           and b.startAt <= :now
           and b.endAt > :now
    """)
    int checkIn(
            @Param("bookingId") String bookingId,
            @Param("userId") String userId,
            @Param("now") Instant now,
            @Param("confirmed") BookingStatus confirmed
    );

    @Modifying
    @Query("""
        update BookingEntity b
           set b.checkedOutAt = :now,
               b.status = :completed,
               b.updatedAt = :now
         where b.id = :bookingId
           and b.userId = :userId
           and b.deletedAt is null
           and b.status = :confirmed
           and b.checkedInAt is not null
           and b.checkedOutAt is null
    """)
    int checkOut(
            @Param("bookingId") String bookingId,
            @Param("userId") String userId,
            @Param("now") Instant now,
            @Param("confirmed") BookingStatus confirmed,
            @Param("completed") BookingStatus completed
    );

    long countByUserIdAndDeletedAtIsNull(String userId);

    // -------------------------------------------------------------------------
    // Database coursework demos (do not use for core business; see controllers)
    // -------------------------------------------------------------------------

    /**
     * JPQL: multi-table join — booking, user, slot, and area. Returns one row per booking
     * with user email, slot code, and area name.
     */
    @Query(
            """
            select new com.app.dto.demo.BookingJoinRow(
                b.id, u.email, s.code, a.name, b.startAt, b.endAt
            )
            from BookingEntity b
                join UserEntity u on b.userId = u.id
                join ParkingSlotEntity s on b.parkingSlotId = s.id
                join ParkingAreaEntity a on b.parkingAreaId = a.id
            where b.deletedAt is null
            order by b.createdAt desc
            """
    )
    List<BookingJoinRow> findAllWithUserSlotAreaJoin();

    /**
     * JPQL: overlap predicate using {@code count(b) > 0} (semantics match {@link #existsOverlappingBooking}).
     * Course demo for COUNT-based existence checks.
     */
    default boolean overlapsUsingCountJpql(
            String parkingSlotId,
            Instant startAt,
            Instant endAt,
            List<BookingStatus> activeStatuses) {
        return existsOverlappingBooking(parkingSlotId, startAt, endAt, activeStatuses);
    }

    /**
     * Native MySQL: same overlap rule as production JPQL, expressed with {@code EXISTS (SELECT 1 ...)}.
     * Statuses fixed to PENDING and CONFIRMED to mirror default overlap checks.
     */
    @Query(
            value =
                    """
                    SELECT EXISTS(
                        SELECT 1
                        FROM booking b
                        WHERE b.parking_slot_id = :slotId
                          AND b.deleted_at IS NULL
                          AND b.status IN ('PENDING', 'CONFIRMED')
                          AND b.start_at < :endAt
                          AND b.end_at > :startAt
                    )
                    """,
            nativeQuery = true
    )
    boolean existsOverlappingBookingNativeEx(
            @Param("slotId") String slotId, @Param("startAt") Instant startAt, @Param("endAt") Instant endAt);

    /**
     * JPQL: number of (non-deleted) bookings per {@link com.app.domain.enums.BookingStatus}.
     */
    @Query(
            """
            select new com.app.dto.demo.BookingStatusCountRow(b.status, count(b))
            from BookingEntity b
            where b.deletedAt is null
            group by b.status
            order by b.status
            """
    )
    List<BookingStatusCountRow> countBookingsGroupByStatus();

    /**
     * JPQL: number of (non-deleted) bookings per parking area, with area name from join.
     */
    @Query(
            """
            select new com.app.dto.demo.BookingAreaCountRow(b.parkingAreaId, a.name, count(b))
            from BookingEntity b
                join ParkingAreaEntity a on b.parkingAreaId = a.id
            where b.deletedAt is null
            group by b.parkingAreaId, a.name
            order by a.name
            """
    )
    List<BookingAreaCountRow> countBookingsGroupByParkingArea();
}
