package com.app.domain.entity;

import com.app.domain.enums.BookingStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "booking")
public class BookingEntity {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "user_id", nullable = false, length = 32)
    private String userId;

    @Column(name = "parking_area_id", nullable = false, length = 32)
    private String parkingAreaId;

    @Column(name = "parking_slot_id", nullable = false, length = 32)
    private String parkingSlotId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "checked_in_at")
    private Instant checkedInAt;

    @Column(name = "checked_out_at")
    private Instant checkedOutAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            columnDefinition = "ENUM('PENDING','CONFIRMED','CANCELLED','COMPLETED')"
    )
    private BookingStatus status;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "amount_subtotal", nullable = false)
    private Integer amountSubtotal;

    @Column(name = "amount_total", nullable = false)
    private Integer amountTotal;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getParkingAreaId() {
        return parkingAreaId;
    }

    public void setParkingAreaId(String parkingAreaId) {
        this.parkingAreaId = parkingAreaId;
    }

    public String getParkingSlotId() {
        return parkingSlotId;
    }

    public void setParkingSlotId(String parkingSlotId) {
        this.parkingSlotId = parkingSlotId;
    }

    public Instant getStartAt() {
        return startAt;
    }

    public void setStartAt(Instant startAt) {
        this.startAt = startAt;
    }

    public Instant getEndAt() {
        return endAt;
    }

    public void setEndAt(Instant endAt) {
        this.endAt = endAt;
    }

    public Instant getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(Instant checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public Instant getCheckedOutAt() {
        return checkedOutAt;
    }

    public void setCheckedOutAt(Instant checkedOutAt) {
        this.checkedOutAt = checkedOutAt;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getAmountSubtotal() {
        return amountSubtotal;
    }

    public void setAmountSubtotal(Integer amountSubtotal) {
        this.amountSubtotal = amountSubtotal;
    }

    public Integer getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(Integer amountTotal) {
        this.amountTotal = amountTotal;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
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
