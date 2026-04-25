package com.app.repository;

import com.app.domain.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    boolean existsByBookingIdAndDeletedAtIsNull(String bookingId);

    List<PaymentEntity> findByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(String bookingId);

    PaymentEntity findFirstByBookingIdAndDeletedAtIsNullOrderByCreatedAtDesc(String bookingId);

    PaymentEntity findFirstByProviderTxnRefAndDeletedAtIsNull(String providerTxnRef);

    @Modifying
    @Query("""
        update PaymentEntity p
           set p.status = :toStatus,
               p.updatedAt = :now
         where p.bookingId = :bookingId
           and p.deletedAt is null
           and p.status in :fromStatuses
    """)
    int updateStatusByBooking(
            @Param("bookingId") String bookingId,
            @Param("fromStatuses") List<PaymentEntity.PaymentStatus> fromStatuses,
            @Param("toStatus") PaymentEntity.PaymentStatus toStatus,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        update PaymentEntity p
           set p.status = 'CANCELLED',
               p.refundedAt = :now,
               p.updatedAt = :now
         where p.bookingId = :bookingId
           and p.deletedAt is null
           and p.status = 'PAID'
           and p.method <> 'CASH'
    """)
    int refundSucceededOnlineByBooking(@Param("bookingId") String bookingId, @Param("now") Instant now);

    @Modifying
    @Query("""
        update PaymentEntity p
           set p.status = :status,
               p.providerTxnRef = :reference,
               p.updatedAt = :now,
               p.paidAt = :paidAt
         where p.id = :paymentId
    """)
    int markPaymentStatus(
            @Param("paymentId") String paymentId,
            @Param("status") PaymentEntity.PaymentStatus status,
            @Param("reference") String reference,
            @Param("now") Instant now,
            @Param("paidAt") Instant paidAt
    );
}
