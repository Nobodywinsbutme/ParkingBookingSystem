package com.app.domain.entity;

import com.app.domain.enums.SlotStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "parking_slot",
        uniqueConstraints = @UniqueConstraint(name = "unique_slot_per_area", columnNames = {"parking_area_id", "code"})
)
public class ParkingSlotEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "parking_area_id", nullable = false, length = 32)
    private String parkingAreaId;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "floor", length = 32)
    private String floor;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "ENUM('AVAILABLE','BOOKED','MAINTENANCE')"
    )
    private SlotStatus status;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(String parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public SlotStatus getStatus() {
        return status;
    }

    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
