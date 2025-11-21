package com.magscene.magsav.backend.repository;

import com.magsav.entities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des fournisseurs
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    
    // Recherche par nom
    Optional<Supplier> findByName(String name);
    
    List<Supplier> findByNameContainingIgnoreCase(String name);
    
    // Recherche par email
    Optional<Supplier> findByEmail(String email);
    
    // Fournisseurs actifs
    List<Supplier> findByActiveTrue();
    
    List<Supplier> findByActiveTrueOrderByName();
    
    // Fournisseurs par services proposés
    List<Supplier> findByHasAfterSalesServiceTrue();
    
    List<Supplier> findByHasRMAServiceTrue();
    
    List<Supplier> findByHasPartsServiceTrue();
    
    List<Supplier> findByHasEquipmentServiceTrue();
    
    // Recherche multi-services
    @Query("SELECT s FROM Supplier s WHERE " +
           "(:hasAfterSales = false OR s.hasAfterSalesService = :hasAfterSales) AND " +
           "(:hasRMA = false OR s.hasRMAService = :hasRMA) AND " +
           "(:hasParts = false OR s.hasPartsService = :hasParts) AND " +
           "(:hasEquipment = false OR s.hasEquipmentService = :hasEquipment) AND " +
           "s.active = true")
    List<Supplier> findByServices(@Param("hasAfterSales") boolean hasAfterSales,
                                 @Param("hasRMA") boolean hasRMA,
                                 @Param("hasParts") boolean hasParts,
                                 @Param("hasEquipment") boolean hasEquipment);
    
    // Fournisseurs avec seuil configuré
    @Query("SELECT s FROM Supplier s WHERE s.freeShippingThreshold IS NOT NULL AND s.freeShippingThreshold > 0")
    List<Supplier> findSuppliersWithThreshold();
    
    // Recherche textuelle complète
    @Query("SELECT s FROM Supplier s WHERE " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.contactPerson) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(s.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Supplier> searchSuppliers(@Param("searchTerm") String searchTerm);
    
    // Statistiques
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.active = true")
    long countActiveSuppliers();
    
    @Query("SELECT COUNT(s) FROM Supplier s WHERE s.hasPartsService = true AND s.active = true")
    long countPartsSuppliers();
}