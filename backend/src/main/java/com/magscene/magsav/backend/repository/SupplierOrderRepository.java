package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.SupplierOrder;
import com.magscene.magsav.backend.entity.SupplierOrder.OrderStatus;
import com.magscene.magsav.backend.entity.SupplierOrder.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour la gestion des commandes fournisseurs
 */
@Repository
public interface SupplierOrderRepository extends JpaRepository<SupplierOrder, Long> {

    /**
     * Trouve une commande par son numÃƒÂ©ro
     */
    Optional<SupplierOrder> findByOrderNumber(String orderNumber);

    /**
     * Trouve toutes les commandes d'un statut donnÃƒÂ©
     */
    List<SupplierOrder> findByStatus(OrderStatus status);

    /**
     * Trouve toutes les commandes d'un type donnÃƒÂ©
     */
    List<SupplierOrder> findByType(OrderType type);

    /**
     * Trouve toutes les commandes d'un fournisseur
     */
    List<SupplierOrder> findBySupplierNameContainingIgnoreCase(String supplierName);

    /**
     * Trouve toutes les commandes d'un responsable
     */
    List<SupplierOrder> findByOrderedByContainingIgnoreCase(String orderedBy);

    /**
     * Trouve les commandes en attente d'approbation
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.status = 'PENDING'")
    List<SupplierOrder> findPendingApproval();

    /**
     * Trouve les commandes en cours (commandÃƒÂ©es mais non reÃƒÂ§ues)
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.status IN ('APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED')")
    List<SupplierOrder> findActiveOrders();

    /**
     * Trouve les commandes avec livraison prÃƒÂ©vue dans une pÃƒÂ©riode
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.expectedDeliveryDate BETWEEN :startDate AND :endDate")
    List<SupplierOrder> findByExpectedDeliveryDateBetween(@Param("startDate") LocalDate startDate, 
                                                         @Param("endDate") LocalDate endDate);

    /**
     * Trouve les commandes reÃƒÂ§ues dans une pÃƒÂ©riode
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.actualDeliveryDate BETWEEN :startDate AND :endDate")
    List<SupplierOrder> findByActualDeliveryDateBetween(@Param("startDate") LocalDate startDate, 
                                                       @Param("endDate") LocalDate endDate);

    /**
     * Trouve les commandes liÃƒÂ©es ÃƒÂ  un projet
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.project.id = :projectId")
    List<SupplierOrder> findByProjectId(@Param("projectId") Long projectId);

    /**
     * Recherche globale dans les commandes
     */
    @Query("SELECT so FROM SupplierOrder so WHERE " +
           "LOWER(so.orderNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(so.supplierName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(so.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<SupplierOrder> searchOrders(@Param("searchTerm") String searchTerm);

    /**
     * Compte les commandes par statut
     */
    @Query("SELECT so.status, COUNT(so) FROM SupplierOrder so GROUP BY so.status")
    List<Object[]> countOrdersByStatus();

    /**
     * Compte les commandes par type
     */
    @Query("SELECT so.type, COUNT(so) FROM SupplierOrder so GROUP BY so.type")
    List<Object[]> countOrdersByType();

    /**
     * Compte les commandes par fournisseur
     */
    @Query("SELECT so.supplierName, COUNT(so) FROM SupplierOrder so GROUP BY so.supplierName ORDER BY COUNT(so) DESC")
    List<Object[]> countOrdersBySupplier();

    /**
     * Trouve les commandes en retard de livraison
     */
    @Query("SELECT so FROM SupplierOrder so WHERE " +
           "so.expectedDeliveryDate < CURRENT_DATE AND so.status IN ('APPROVED', 'ORDERED', 'PARTIALLY_RECEIVED')")
    List<SupplierOrder> findOverdueOrders();

    /**
     * Trouve les commandes rÃƒÂ©centes (crÃƒÂ©ÃƒÂ©es dans les N derniers jours)
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.orderDate >= :sinceDate ORDER BY so.orderDate DESC")
    List<SupplierOrder> findRecentOrders(@Param("sinceDate") LocalDate sinceDate);

    /**
     * Calcule le montant total des commandes par pÃƒÂ©riode
     */
    @Query("SELECT SUM(so.amountTTC) FROM SupplierOrder so WHERE " +
           "so.status = 'RECEIVED' AND so.actualDeliveryDate BETWEEN :startDate AND :endDate")
    Double calculateTotalOrderAmount(@Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);

    /**
     * Trouve les commandes nÃƒÂ©cessitant un suivi urgent
     */
    @Query("SELECT so FROM SupplierOrder so WHERE " +
           "(so.status = 'PENDING' AND so.orderDate < :urgentDate) OR " +
           "(so.expectedDeliveryDate < CURRENT_DATE AND so.status IN ('ORDERED', 'PARTIALLY_RECEIVED'))")
    List<SupplierOrder> findOrdersNeedingUrgentFollowUp(@Param("urgentDate") LocalDate urgentDate);

    /**
     * Trouve les top fournisseurs par montant commandÃƒÂ©
     */
    @Query("SELECT so.supplierName, SUM(so.amountTTC) as total FROM SupplierOrder so " +
           "WHERE so.status IN ('RECEIVED', 'PARTIALLY_RECEIVED') " +
           "GROUP BY so.supplierName ORDER BY total DESC")
    List<Object[]> findTopSuppliersByAmount();

    /**
     * Trouve les commandes nécessitant un suivi urgent (alias pour le contrôleur)
     */
    @Query("SELECT so FROM SupplierOrder so WHERE so.status IN ('ORDERED', 'PARTIALLY_RECEIVED') " +
           "AND so.orderDate <= :urgentDate")
    List<SupplierOrder> findOrdersRequiringUrgentFollowup(@Param("urgentDate") LocalDate urgentDate);

    /**
     * Statistiques par fournisseur (alias pour le contrÃƒÂ´leur)
     */
    @Query("SELECT so.supplierName, COUNT(so), AVG(so.amountTTC), SUM(so.amountTTC) " +
           "FROM SupplierOrder so GROUP BY so.supplierName ORDER BY COUNT(so) DESC")
    List<Object[]> getSupplierStats();

    /**
     * Trouve les commandes par pÃƒÂ©riode de commande
     */
    List<SupplierOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
}

