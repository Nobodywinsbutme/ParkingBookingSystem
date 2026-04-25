package com.app.repository;

import com.app.domain.entity.ParkingSlotEntity;
import com.app.domain.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlotEntity, String> {
    Optional<ParkingSlotEntity> findByIdAndParkingAreaIdAndDeletedAtIsNullAndActiveTrue(String id, String parkingAreaId);

    List<ParkingSlotEntity> findByParkingAreaIdAndDeletedAtIsNullAndActiveTrueOrderByCodeAsc(String parkingAreaId);

    long countByDeletedAtIsNullAndActiveTrueAndStatus(SlotStatus status);

    List<ParkingSlotEntity> findByDeletedAtIsNullOrderByParkingAreaIdAscCodeAsc();

    Optional<ParkingSlotEntity> findByIdAndDeletedAtIsNull(String id);

    boolean existsByParkingAreaIdAndCodeAndDeletedAtIsNull(String parkingAreaId, String code);

    boolean existsByParkingAreaIdAndCodeAndDeletedAtIsNullAndIdNot(String parkingAreaId, String code, String id);
}
