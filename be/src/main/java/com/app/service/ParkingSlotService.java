package com.app.service;

import com.app.domain.entity.ParkingAreaEntity;
import com.app.domain.entity.ParkingSlotEntity;
import com.app.domain.enums.SlotStatus;
import com.app.repository.ParkingAreaRepository;
import com.app.repository.ParkingSlotRepository;
import com.app.support.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParkingSlotService {
    private final ParkingSlotRepository parkingSlotRepository;
    private final ParkingAreaRepository parkingAreaRepository;

    public ParkingSlotService(
            ParkingSlotRepository parkingSlotRepository, ParkingAreaRepository parkingAreaRepository) {
        this.parkingSlotRepository = parkingSlotRepository;
        this.parkingAreaRepository = parkingAreaRepository;
    }

    public List<ParkingSlotEntity> listAllForAdmin() {
        return parkingSlotRepository.findByDeletedAtIsNullOrderByParkingAreaIdAscCodeAsc();
    }

    public Optional<ParkingSlotEntity> findForAdmin(String id) {
        return parkingSlotRepository.findByIdAndDeletedAtIsNull(id);
    }

    public ParkingSlotEntity getForAdminOrThrow(String id) {
        return findForAdmin(id)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parking slot not found"));
    }

    @Transactional
    public ParkingSlotEntity create(
            String parkingAreaId, String code, String floor, SlotStatus status) {
        validateAdminSlotStatusForWrite(status, null);
        ParkingAreaEntity area = findAreaOr404(parkingAreaId);
        if (area.getDeletedAt() != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Parking area is not valid");
        }
        if (code == null || code.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Slot code is required");
        }
        String normalized = code.trim();
        if (parkingSlotRepository.existsByParkingAreaIdAndCodeAndDeletedAtIsNull(parkingAreaId, normalized)) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "This code already exists in the selected area");
        }
        Instant now = Instant.now();
        ParkingSlotEntity e = new ParkingSlotEntity();
        e.setId(newId());
        e.setParkingAreaId(parkingAreaId);
        e.setCode(normalized);
        e.setFloor(blankToNull(floor));
        e.setStatus(status != null ? status : SlotStatus.AVAILABLE);
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        return parkingSlotRepository.save(e);
    }

    @Transactional
    public ParkingSlotEntity update(String id, String parkingAreaId, String code, String floor, SlotStatus status) {
        ParkingSlotEntity e = getForAdminOrThrow(id);
        validateAdminSlotStatusForWrite(status, e.getStatus());
        findAreaOr404(parkingAreaId);
        if (code == null || code.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Slot code is required");
        }
        String normalized = code.trim();
        if (parkingSlotRepository.existsByParkingAreaIdAndCodeAndDeletedAtIsNullAndIdNot(
                parkingAreaId, normalized, id)) {
            throw new ApiException(HttpStatus.CONFLICT, "CONFLICT", "This code already exists in the selected area");
        }
        e.setParkingAreaId(parkingAreaId);
        e.setCode(normalized);
        e.setFloor(blankToNull(floor));
        e.setStatus(status);
        e.setUpdatedAt(Instant.now());
        return parkingSlotRepository.save(e);
    }

    /**
     * Admin "delete": deactivate slot (hidden from public availability paths that filter active).
     */
    @Transactional
    public void disable(String id) {
        ParkingSlotEntity e = getForAdminOrThrow(id);
        e.setActive(false);
        e.setUpdatedAt(Instant.now());
        parkingSlotRepository.save(e);
    }

    public List<ParkingAreaEntity> listAreasForSlotForms() {
        return parkingAreaRepository.findByDeletedAtIsNullOrderByNameAsc();
    }

    private ParkingAreaEntity findAreaOr404(String id) {
        return parkingAreaRepository
                .findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Parking area not found"));
    }

    /**
     * Create: only {@link SlotStatus#AVAILABLE} or {@link SlotStatus#MAINTENANCE}. Update: same, or keep
     * {@link SlotStatus#BOOKED}; may move BOOKED → MAINTENANCE to take a slot offline.
     */
    private void validateAdminSlotStatusForWrite(SlotStatus newStatus, SlotStatus previous) {
        if (newStatus == null) {
            return;
        }
        if (newStatus == SlotStatus.BOOKED) {
            if (previous != SlotStatus.BOOKED) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Cannot set status to BOOKED from admin form");
            }
            return;
        }
        if (newStatus == SlotStatus.AVAILABLE || newStatus == SlotStatus.MAINTENANCE) {
            return;
        }
        throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid slot status");
    }

    private static String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
