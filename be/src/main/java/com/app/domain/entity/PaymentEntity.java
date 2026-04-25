package com.app.domain.entity;

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
        name = "payment",
        uniqueConstraints = @UniqueConstraint(name = "unique_booking_payment", columnNames = {"booking_id"})
)
public class PaymentEntity {
    public enum PaymentMethod {
        STRIPE, CASH
    }

    /**
     * PENDING: awaiting Stripe Checkout completion (or other async settlement).
     * PAID: successfully captured.
     * FAILED: checkout abandoned, declined, or expired.
     * CANCELLED: invalidated by user/system (e.g. booking cancelled, refund path).
     */
    public enum PaymentStatus {
        PENDING, PAID, FAILED, CANCELLED
    }

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "booking_id", nullable = false, length = 32)
    private String bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 32)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    @Column(name = "provider_txn_ref")
    private String providerTxnRef;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "refunded_at")
    private Instant refundedAt;

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

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProviderTxnRef() {
        return providerTxnRef;
    }

    public void setProviderTxnRef(String providerTxnRef) {
        this.providerTxnRef = providerTxnRef;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(Instant refundedAt) {
        this.refundedAt = refundedAt;
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
