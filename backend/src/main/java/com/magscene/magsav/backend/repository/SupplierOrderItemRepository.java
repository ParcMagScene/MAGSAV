package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.SupplierOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository pour les articles de commande fournisseur
 */
@Repository
public interface SupplierOrderItemRepository extends JpaRepository<SupplierOrderItem, Long> {

    /**
     * Trouve tous les articles d'une commande fournisseur
     */
    List<SupplierOrderItem> findBySupplierOrderId(Long supplierOrderId);

    /**
     * Trouve les articles par nom de produit
     */
    List<SupplierOrderItem> findByItemNameContainingIgnoreCase(String itemName);

    /**
     * Trouve les articles partiellement reÃƒÂ§us
     */
    @Query("SELECT soi FROM SupplierOrderItem soi WHERE soi.quantityReceived > 0 AND soi.quantityReceived < soi.quantity")
    List<SupplierOrderItem> findPartiallyReceived();

    /**
     * Trouve les articles non reÃƒÂ§us
     */
    @Query("SELECT soi FROM SupplierOrderItem soi WHERE soi.quantityReceived = 0")
    List<SupplierOrderItem> findNotReceived();

    /**
     * Calcule la valeur totale des articles d'une commande
     */
    @Query("SELECT COALESCE(SUM(soi.totalAmount), 0) FROM SupplierOrderItem soi WHERE soi.supplierOrder.id = :orderId")
    Double calculateTotalAmountForOrder(@Param("orderId") Long orderId);

    /**
     * Compte les articles en attente de rÃƒÂ©ception
     */
    @Query("SELECT COUNT(soi) FROM SupplierOrderItem soi WHERE soi.quantityReceived < soi.quantity")
    Long countPendingItems();
}

