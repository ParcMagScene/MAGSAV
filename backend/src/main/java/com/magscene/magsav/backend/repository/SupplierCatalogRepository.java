package com.magscene.magsav.backend.repository;

import com.magsav.entities.SupplierCatalog;
import com.magsav.entities.Supplier;
import com.magsav.enums.ImportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des catalogues fournisseurs
 */
@Repository
public interface SupplierCatalogRepository extends JpaRepository<SupplierCatalog, Long> {
    
    // Recherche par fournisseur
    List<SupplierCatalog> findBySupplier(Supplier supplier);
    
    List<SupplierCatalog> findBySupplierId(Long supplierId);
    
    List<SupplierCatalog> findBySupplierAndActiveTrue(Supplier supplier);
    
    // Recherche par nom
    Optional<SupplierCatalog> findBySupplierAndName(Supplier supplier, String name);
    
    List<SupplierCatalog> findByNameContainingIgnoreCase(String name);
    
    // Recherche par statut d'import
    List<SupplierCatalog> findByImportStatus(ImportStatus status);
    
    List<SupplierCatalog> findByImportStatusAndActiveTrue(ImportStatus status);
    
    // Catalogues actifs
    List<SupplierCatalog> findByActiveTrueOrderByImportDateDesc();
    
    // Recherche par période
    List<SupplierCatalog> findByImportDateBetween(LocalDateTime start, LocalDateTime end);
    
    // Catalogues avec items
    @Query("SELECT c FROM SupplierCatalog c WHERE c.itemsCount > 0 AND c.active = true")
    List<SupplierCatalog> findCatalogsWithItems();
    
    // Recherche par fournisseur avec items
    @Query("SELECT c FROM SupplierCatalog c WHERE c.supplier = :supplier AND c.itemsCount > 0 AND c.active = true")
    List<SupplierCatalog> findCatalogsWithItemsBySupplier(@Param("supplier") Supplier supplier);
    
    // Catalogues récents
    @Query("SELECT c FROM SupplierCatalog c WHERE c.importDate >= :since ORDER BY c.importDate DESC")
    List<SupplierCatalog> findRecentCatalogs(@Param("since") LocalDateTime since);
    
    // Recherche textuelle
    @Query("SELECT c FROM SupplierCatalog c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.supplier.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<SupplierCatalog> searchCatalogs(@Param("searchTerm") String searchTerm);
    
    // Statistiques
    @Query("SELECT COUNT(c) FROM SupplierCatalog c WHERE c.active = true")
    long countActiveCatalogs();
    
    @Query("SELECT SUM(c.itemsCount) FROM SupplierCatalog c WHERE c.active = true")
    Long countTotalItems();
    
    @Query("SELECT c.supplier.name, COUNT(c) FROM SupplierCatalog c WHERE c.active = true GROUP BY c.supplier.name")
    List<Object[]> countCatalogsBySupplier();
}