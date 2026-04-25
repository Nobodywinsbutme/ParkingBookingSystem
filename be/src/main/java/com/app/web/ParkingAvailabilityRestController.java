package com.app.web;

import com.app.dto.parking.SlotAvailabilityJson;
import com.app.service.ParkingAvailabilityService;
import com.app.service.ParkingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/parking-areas")
public class ParkingAvailabilityRestController {

    private final ParkingService parkingService;
    private final ParkingAvailabilityService parkingAvailabilityService;

    public ParkingAvailabilityRestController(
            ParkingService parkingService,
            ParkingAvailabilityService parkingAvailabilityService
    ) {
        this.parkingService = parkingService;
        this.parkingAvailabilityService = parkingAvailabilityService;
    }

    @GetMapping("/{areaId}/slots")
    public List<SlotAvailabilityJson> slotsForRange(
            @PathVariable String areaId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt
    ) {
        if (parkingService.getActiveArea(areaId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Area not found");
        }
        try {
            return parkingAvailabilityService.slotsForAreaAndRange(areaId, startAt, endAt);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
}
