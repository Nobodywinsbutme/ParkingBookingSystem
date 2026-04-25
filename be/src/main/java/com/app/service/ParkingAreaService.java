package com.app.service;

import com.app.domain.entity.ParkingAreaEntity;
import com.app.repository.ParkingAreaRepository;
import com.app.support.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ParkingAreaService {
    private final ParkingAreaRepository parkingAreaRepository;

    public ParkingAreaService(ParkingAreaRepository parkingAreaRepository) {
        this.parkingAreaRepository = parkingAreaRepository;
    }

    public List<ParkingAreaEntity> listAllForAdmin() {
        return parkingAreaRepository.findByDeletedAtIsNullOrderByNameAsc();
    }

    public Optional<ParkingAreaEntity> findForAdmin(String id) {
        return parkingAreaRepository.findByIdAndDeletedAtIsNull(id);
    }

    public ParkingAreaEntity getForAdminOrThrow(String id) {
        return findForAdmin(id)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "Parking area not found"));
    }

    @Transactional
    public ParkingAreaEntity create(String city, String name, String addressLine1) {
        if (name == null || name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Name is required");
        }
        Instant now = Instant.now();
        ParkingAreaEntity e = new ParkingAreaEntity();
        e.setId(newId());
        e.setCity(blankToNull(city));
        e.setName(name.trim());
        e.setAddressLine1(blankToNull(addressLine1));
        e.setActive(true);
        e.setCreatedAt(now);
        e.setUpdatedAt(now);
        return parkingAreaRepository.save(e);
    }

    @Transactional
    public ParkingAreaEntity update(String id, String city, String name, String addressLine1) {
        ParkingAreaEntity e = getForAdminOrThrow(id);
        if (name == null || name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Name is required");
        }
        e.setCity(blankToNull(city));
        e.setName(name.trim());
        e.setAddressLine1(blankToNull(addressLine1));
        e.setUpdatedAt(Instant.now());
        return parkingAreaRepository.save(e);
    }

    /**
     * Admin "delete": deactivate (hide from public listing). Does not set {@code deleted_at}.
     */
    @Transactional
    public void disable(String id) {
        ParkingAreaEntity e = getForAdminOrThrow(id);
        e.setActive(false);
        e.setUpdatedAt(Instant.now());
        parkingAreaRepository.save(e);
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
