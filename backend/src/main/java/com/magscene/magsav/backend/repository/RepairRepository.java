package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Repair;
import com.magscene.magsav.backend.entity.Repair.RepairStatus;
import com.magscene.magsav.backend.entity.Repair.RepairPriority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Long> {

    Optional<Repair> findByRepairNumber(String repairNumber);

    List<Repair> findByStatus(RepairStatus status);

    List<Repair> findByPriority(RepairPriority priority);

    List<Repair> findByTechnicianName(String technicianName);

    List<Repair> findByCustomerName(String customerName);

    List<Repair> findByWarrantyCovered(Boolean warrantyCovered);

    List<Repair> findByRequestDateBetween(LocalDate startDate, LocalDate endDate);

    List<Repair> findByCompletionDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT r FROM Repair r WHERE r.equipmentName LIKE %:keyword% OR r.repairNumber LIKE %:keyword% OR r.customerName LIKE %:keyword%")
    List<Repair> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT COUNT(r) FROM Repair r WHERE r.status = :status")
    long countByStatus(@Param("status") RepairStatus status);

    @Query("SELECT COUNT(r) FROM Repair r WHERE r.technicianName = :technicianName AND r.status IN ('IN_PROGRESS', 'DIAGNOSED')")
    long countActiveRepairsByTechnician(@Param("technicianName") String technicianName);

    @Query("SELECT COALESCE(AVG(r.actualDurationHours), 0) FROM Repair r WHERE r.status = 'COMPLETED' AND r.actualDurationHours IS NOT NULL")
    double getAverageDuration();

    @Query("SELECT COALESCE(AVG(r.actualCost), 0) FROM Repair r WHERE r.status = 'COMPLETED' AND r.actualCost IS NOT NULL")
    double getAverageCost();
}
