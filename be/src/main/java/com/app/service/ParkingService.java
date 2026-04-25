package com.app.service;

import com.app.domain.entity.ParkingAreaEntity;
import com.app.domain.entity.ParkingSlotEntity;
import com.app.repository.ParkingAreaRepository;
import com.app.repository.ParkingSlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParkingService {
    private final ParkingAreaRepository parkingAreaRepository;
    private final ParkingSlotRepository parkingSlotRepository;

    public ParkingService(ParkingAreaRepository parkingAreaRepository, ParkingSlotRepository parkingSlotRepository) {
        this.parkingAreaRepository = parkingAreaRepository;
        this.parkingSlotRepository = parkingSlotRepository;
    }

    public List<ParkingAreaEntity> listActiveAreas() {
        return parkingAreaRepository.findByDeletedAtIsNullAndActiveTrueOrderByNameAsc();
    }

    public ParkingAreaEntity getActiveArea(String id) {
        return parkingAreaRepository.findByIdAndDeletedAtIsNullAndActiveTrue(id).orElse(null);
    }

    public List<ParkingSlotEntity> listActiveSlotsForArea(String areaId) {
        return parkingSlotRepository.findByParkingAreaIdAndDeletedAtIsNullAndActiveTrueOrderByCodeAsc(areaId);
    }
}
