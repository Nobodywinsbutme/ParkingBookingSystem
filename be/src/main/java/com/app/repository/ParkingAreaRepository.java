package com.app.repository;

import com.app.domain.entity.ParkingAreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingAreaRepository extends JpaRepository<ParkingAreaEntity, String> {
    Optional<ParkingAreaEntity> findByIdAndDeletedAtIsNullAndActiveTrue(String id);

    List<ParkingAreaEntity> findByDeletedAtIsNullAndActiveTrueOrderByNameAsc();

    List<ParkingAreaEntity> findByDeletedAtIsNullOrderByNameAsc();

    Optional<ParkingAreaEntity> findByIdAndDeletedAtIsNull(String id);
}
