package com.magscene.magsav.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.magsav.entities.GroupedOrder;
import com.magsav.entities.GroupedOrderStatus;
import com.magsav.entities.Supplier;

/**
 * Repository pour la gestion des commandes groupées
 */
@Repository
public interface GroupedOrderRepository extends JpaRepository<GroupedOrder, Long> {

       // Récupération avec supplier eager loaded
       @Query("SELECT go FROM GroupedOrder go JOIN FETCH go.supplier")
       List<GroupedOrder> findAllWithSupplier();

       // Recherche par numéro de commande
       Optional<GroupedOrder> findByOrderNumber(String orderNumber);

       // Recherche par fournisseur
       List<GroupedOrder> findBySupplier(Supplier supplier);

       List<GroupedOrder> findBySupplierOrderByCreatedAtDesc(Supplier supplier);

       List<GroupedOrder> findBySupplierId(Long supplierId);

       // Recherche par statut
       List<GroupedOrder> findByStatus(GroupedOrderStatus status);

       List<GroupedOrder> findByStatusOrderByCreatedAtDesc(GroupedOrderStatus status);

       List<GroupedOrder> findByStatusIn(List<GroupedOrderStatus> statuses);

       // Commandes ouvertes par fournisseur
       List<GroupedOrder> findBySupplierAndStatus(Supplier supplier, GroupedOrderStatus status);

       Optional<GroupedOrder> findBySupplierAndStatusOrderByCreatedAtDesc(Supplier supplier, GroupedOrderStatus status);

       @Query("SELECT go FROM GroupedOrder go WHERE go.supplier = :supplier AND go.status IN ('OPEN', 'THRESHOLD_REACHED') ORDER BY go.createdAt DESC")
       List<GroupedOrder> findOpenOrdersBySupplier(@Param("supplier") Supplier supplier);

       // Commandes atteignant le seuil
       @Query("SELECT go FROM GroupedOrder go WHERE go.status = 'THRESHOLD_REACHED' AND go.thresholdAlertSent = false")
       List<GroupedOrder> findOrdersReachingThresholdWithoutAlert();

       // Commandes dépassant le seuil
       @Query("SELECT go FROM GroupedOrder go JOIN go.supplier s WHERE " +
                     "go.currentAmount >= s.freeShippingThreshold AND " +
                     "s.freeShippingThreshold IS NOT NULL AND s.freeShippingThreshold > 0 AND " +
                     "go.status = 'OPEN'")
       List<GroupedOrder> findOrdersExceedingThreshold();

       // Commandes validées en attente
       List<GroupedOrder> findByStatusAndValidatedAtIsNotNull(GroupedOrderStatus status);

       // Recherche par validateur
       List<GroupedOrder> findByValidatedBy(String validatedBy);

       List<GroupedOrder> findByValidatedByOrderByValidatedAtDesc(String validatedBy);

       // Recherche par période
       List<GroupedOrder> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

       List<GroupedOrder> findByValidatedAtBetween(LocalDateTime start, LocalDateTime end);

       // Commandes récentes
       @Query("SELECT go FROM GroupedOrder go WHERE go.createdAt >= :since ORDER BY go.createdAt DESC")
       List<GroupedOrder> findRecentOrders(@Param("since") LocalDateTime since);

       // Commandes avec auto-validation
       List<GroupedOrder> findByAutoValidateOnThresholdTrue();

       @Query("SELECT go FROM GroupedOrder go WHERE go.autoValidateOnThreshold = true AND go.status = 'THRESHOLD_REACHED'")
       List<GroupedOrder> findAutoValidatableOrders();

       // Recherche par montant
       List<GroupedOrder> findByCurrentAmountGreaterThan(BigDecimal amount);

       List<GroupedOrder> findByCurrentAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

       // Recherche textuelle
       @Query("SELECT go FROM GroupedOrder go WHERE " +
                     "LOWER(go.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(go.supplier.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(go.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                     "LOWER(go.validationNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
       List<GroupedOrder> searchOrders(@Param("searchTerm") String searchTerm);

       // Statistiques par fournisseur
       @Query("SELECT go.supplier.name, COUNT(go) FROM GroupedOrder go GROUP BY go.supplier.name ORDER BY COUNT(go) DESC")
       List<Object[]> countOrdersBySupplier();

       @Query("SELECT go.supplier.name, SUM(go.currentAmount) FROM GroupedOrder go WHERE go.currentAmount IS NOT NULL GROUP BY go.supplier.name ORDER BY SUM(go.currentAmount) DESC")
       List<Object[]> sumAmountsBySupplier();

       // Statistiques par statut
       @Query("SELECT go.status, COUNT(go) FROM GroupedOrder go GROUP BY go.status")
       List<Object[]> countOrdersByStatus();

       // Statistiques temporelles
       @Query("SELECT DATE(go.createdAt), COUNT(go) FROM GroupedOrder go WHERE go.createdAt >= :since GROUP BY DATE(go.createdAt) ORDER BY DATE(go.createdAt)")
       List<Object[]> countOrdersByDay(@Param("since") LocalDateTime since);

       // Temps moyen de validation
       @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, go.createdAt, go.validatedAt)) FROM GroupedOrder go WHERE go.validatedAt IS NOT NULL")
       Double averageValidationTimeInHours();

       // Montant total des commandes
       @Query("SELECT SUM(go.currentAmount) FROM GroupedOrder go WHERE go.currentAmount IS NOT NULL")
       BigDecimal sumTotalAmount();

       @Query("SELECT SUM(go.currentAmount) FROM GroupedOrder go WHERE go.status = :status AND go.currentAmount IS NOT NULL")
       BigDecimal sumTotalAmountByStatus(@Param("status") GroupedOrderStatus status);

       // Commandes nécessitant une attention
       @Query("SELECT go FROM GroupedOrder go WHERE " +
                     "(go.status = 'OPEN' AND go.createdAt < :oldDate) OR " +
                     "(go.status = 'THRESHOLD_REACHED' AND go.thresholdAlertSent = false) OR " +
                     "(go.status = 'VALIDATED' AND go.validatedAt < :oldValidationDate)")
       List<GroupedOrder> findOrdersNeedingAttention(@Param("oldDate") LocalDateTime oldDate,
                     @Param("oldValidationDate") LocalDateTime oldValidationDate);
}