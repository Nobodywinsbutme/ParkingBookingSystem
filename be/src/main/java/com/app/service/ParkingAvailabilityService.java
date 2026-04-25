package com.app.service;

import com.app.domain.enums.BookingStatus;
import com.app.domain.enums.SlotStatus;
import com.app.dto.parking.SlotAvailabilityJson;
import com.app.repository.BookingRepository;
import com.app.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ParkingAvailabilityService {

    private static final ZoneId BOOKING_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ParkingSlotRepository parkingSlotRepository;
    private final BookingRepository bookingRepository;

    public ParkingAvailabilityService(
            ParkingSlotRepository parkingSlotRepository,
            BookingRepository bookingRepository
    ) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<SlotAvailabilityJson> slotsForAreaAndRange(String areaId, LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End must be after start.");
        }
        Instant startAt = start.atZone(BOOKING_ZONE).toInstant();
        Instant endAt = end.atZone(BOOKING_ZONE).toInstant();
        List<BookingStatus> active = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);

        return parkingSlotRepository
                .findByParkingAreaIdAndDeletedAtIsNullAndActiveTrueOrderByCodeAsc(areaId)
                .stream()
                .map(slot -> {
                    boolean overlap = bookingRepository.existsOverlappingBooking(
                            slot.getId(),
                            startAt,
                            endAt,
                            active
                    );
                    boolean selectable = slot.getStatus() == SlotStatus.AVAILABLE && !overlap;
                    return new SlotAvailabilityJson(
                            slot.getId(),
                            slot.getCode(),
                            slot.getFloor(),
                            slot.getStatus(),
                            selectable
                    );
                })
                .toList();
    }
}
