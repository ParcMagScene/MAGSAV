package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.RMA;
import com.magscene.magsav.backend.entity.RMA.RMAStatus;
import com.magscene.magsav.backend.entity.RMA.RMAReasonType;
import com.magscene.magsav.backend.entity.RMA.RMAPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RMARepository extends JpaRepository<RMA, Long> {

    Optional<RMA> findByRmaNumber(String rmaNumber);

    List<RMA> findByStatus(RMAStatus status);

    List<RMA> findByPriority(RMAPriority priority);

    List<RMA> findByReason(RMAReasonType reason);

    List<RMA> findByCustomerName(String customerName);

    List<RMA> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);

    List<RMA> findByReturnDateBetween(LocalDate startDate, LocalDate endDate);

    List<RMA> findByTrackingNumber(String trackingNumber);

    @Query("SELECT r FROM RMA r WHERE r.equipmentName LIKE %:keyword% OR r.rmaNumber LIKE %:keyword% OR r.customerName LIKE %:keyword%")
    List<RMA> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(r) FROM RMA r WHERE r.status = :status")
    long countByStatus(@Param("status") RMAStatus status);

    @Query("SELECT COUNT(r) FROM RMA r WHERE r.reason = :reason")
    long countByReason(@Param("reason") RMAReasonType reason);

    @Query("SELECT COALESCE(SUM(r.estimatedValue), 0) FROM RMA r WHERE r.status = 'COMPLETED'")
    double getTotalValueCompleted();

    @Query("SELECT COALESCE(AVG(r.refundAmount), 0) FROM RMA r WHERE r.refundAmount IS NOT NULL")
    double getAverageRefundAmount();
}
