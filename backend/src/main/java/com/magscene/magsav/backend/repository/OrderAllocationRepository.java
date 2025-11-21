package com.magscene.magsav.backend.repository;

import com.magsav.entities.OrderAllocation;
import com.magsav.entities.AllocationStatus;
import com.magsav.entities.MaterialRequest;
import com.magsav.entities.MaterialRequestItem;
import com.magsav.entities.GroupedOrder;
import com.magscene.magsav.backend.entity.SupplierOrder;
import com.magscene.magsav.backend.entity.SupplierOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des allocations de commande
 */
@Repository
public interface OrderAllocationRepository extends JpaRepository<OrderAllocation, Long> {
    
    // Recherche par demande de matériel
    List<OrderAllocation> findByMaterialRequest(MaterialRequest materialRequest);
    
    List<OrderAllocation> findByMaterialRequestOrderByCreatedAtAsc(MaterialRequest materialRequest);
    
    // Recherche par item de demande
    List<OrderAllocation> findByRequestItem(MaterialRequestItem requestItem);
    
    // Recherche par commande groupée
    List<OrderAllocation> findByGroupedOrder(GroupedOrder groupedOrder);
    
    List<OrderAllocation> findByGroupedOrderOrderByCreatedAtAsc(GroupedOrder groupedOrder);
    
    // Recherche par commande fournisseur
    List<OrderAllocation> findBySupplierOrder(SupplierOrder supplierOrder);
    
    List<OrderAllocation> findBySupplierOrderOrderByCreatedAtAsc(SupplierOrder supplierOrder);
    
    // Recherche par item de commande fournisseur
    List<OrderAllocation> findByOrderItem(SupplierOrderItem orderItem);
    
    // Recherche par statut
    List<OrderAllocation> findByStatus(AllocationStatus status);
    
    List<OrderAllocation> findByStatusOrderByCreatedAtDesc(AllocationStatus status);
    
    List<OrderAllocation> findByStatusIn(List<AllocationStatus> statuses);
    
    // Allocations par demandeur
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.materialRequest.requesterEmail = :requesterEmail")
    List<OrderAllocation> findByRequester(@Param("requesterEmail") String requesterEmail);
    
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.materialRequest.requesterEmail = :requesterEmail ORDER BY oa.createdAt DESC")
    List<OrderAllocation> findByRequesterOrderByCreatedAtDesc(@Param("requesterEmail") String requesterEmail);
    
    // Allocations non complètement livrées
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.allocatedQuantity > oa.deliveredQuantity")
    List<OrderAllocation> findPendingDeliveries();
    
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.allocatedQuantity > oa.deliveredQuantity AND oa.status != 'CANCELLED'")
    List<OrderAllocation> findActivePendingDeliveries();
    
    // Allocations partiellement livrées
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.deliveredQuantity > 0 AND oa.deliveredQuantity < oa.allocatedQuantity")
    List<OrderAllocation> findPartiallyDeliveredAllocations();
    
    // Allocations complètement livrées
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.deliveredQuantity >= oa.allocatedQuantity")
    List<OrderAllocation> findFullyDeliveredAllocations();
    
    // Allocations par fournisseur
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.groupedOrder.supplier.id = :supplierId")
    List<OrderAllocation> findBySupplierId(@Param("supplierId") Long supplierId);
    
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.groupedOrder.supplier.name = :supplierName")
    List<OrderAllocation> findBySupplierName(@Param("supplierName") String supplierName);
    
    // Recherche par période
    List<OrderAllocation> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<OrderAllocation> findByDeliveredAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Allocations récentes
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.createdAt >= :since ORDER BY oa.createdAt DESC")
    List<OrderAllocation> findRecentAllocations(@Param("since") LocalDateTime since);
    
    // Livraisons récentes
    @Query("SELECT oa FROM OrderAllocation oa WHERE oa.deliveredAt >= :since ORDER BY oa.deliveredAt DESC")
    List<OrderAllocation> findRecentDeliveries(@Param("since") LocalDateTime since);
    
    // Recherche par référence d'article
    @Query("SELECT oa FROM OrderAllocation oa WHERE " +
           "(oa.requestItem.catalogItem IS NOT NULL AND " +
           "LOWER(oa.requestItem.catalogItem.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) OR " +
           "(oa.requestItem.catalogItem IS NULL AND " +
           "LOWER(oa.requestItem.freeReference) LIKE LOWER(CONCAT('%', :reference, '%')))")
    List<OrderAllocation> findByItemReference(@Param("reference") String reference);
    
    // Recherche textuelle
    @Query("SELECT oa FROM OrderAllocation oa WHERE " +
           "LOWER(oa.materialRequest.requesterName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(oa.materialRequest.requestNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(oa.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(oa.deliveryNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<OrderAllocation> searchAllocations(@Param("searchTerm") String searchTerm);
    
    // Statistiques par fournisseur
    @Query("SELECT oa.groupedOrder.supplier.name, COUNT(oa) FROM OrderAllocation oa GROUP BY oa.groupedOrder.supplier.name ORDER BY COUNT(oa) DESC")
    List<Object[]> countAllocationsBySupplier();
    
    @Query("SELECT oa.groupedOrder.supplier.name, SUM(oa.allocatedQuantity) FROM OrderAllocation oa GROUP BY oa.groupedOrder.supplier.name")
    List<Object[]> sumQuantitiesBySupplier();
    
    // Statistiques par statut
    @Query("SELECT oa.status, COUNT(oa) FROM OrderAllocation oa GROUP BY oa.status")
    List<Object[]> countAllocationsByStatus();
    
    // Statistiques par demandeur
    @Query("SELECT oa.materialRequest.requesterName, COUNT(oa) FROM OrderAllocation oa GROUP BY oa.materialRequest.requesterName ORDER BY COUNT(oa) DESC")
    List<Object[]> countAllocationsByRequester();
    
    // Totaux des quantités
    @Query("SELECT SUM(oa.allocatedQuantity) FROM OrderAllocation oa")
    Long sumTotalAllocatedQuantity();
    
    @Query("SELECT SUM(oa.deliveredQuantity) FROM OrderAllocation oa")
    Long sumTotalDeliveredQuantity();
    
    @Query("SELECT SUM(oa.allocatedQuantity - oa.deliveredQuantity) FROM OrderAllocation oa WHERE oa.allocatedQuantity > oa.deliveredQuantity")
    Long sumPendingDeliveryQuantity();
    
    // Valeurs financières
    @Query("SELECT SUM(oa.estimatedPrice * oa.allocatedQuantity) FROM OrderAllocation oa WHERE oa.estimatedPrice IS NOT NULL")
    Double sumTotalEstimatedValue();
    
    @Query("SELECT SUM(oa.actualPrice * oa.allocatedQuantity) FROM OrderAllocation oa WHERE oa.actualPrice IS NOT NULL")
    Double sumTotalActualValue();
    
    // Temps moyen de livraison
    @Query("SELECT AVG(TIMESTAMPDIFF(DAY, oa.createdAt, oa.deliveredAt)) FROM OrderAllocation oa WHERE oa.deliveredAt IS NOT NULL")
    Double averageDeliveryTimeInDays();
    
    // Allocations nécessitant une attention
    @Query("SELECT oa FROM OrderAllocation oa WHERE " +
           "oa.status = 'ALLOCATED' AND oa.createdAt < :oldDate AND oa.deliveredQuantity = 0")
    List<OrderAllocation> findLongPendingAllocations(@Param("oldDate") LocalDateTime oldDate);
}