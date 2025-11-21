package com.magscene.magsav.backend.repository;

import com.magsav.entities.MaterialRequestItem;
import com.magsav.entities.MaterialRequest;
import com.magsav.entities.CatalogItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion des articles de demandes de matériel
 */
@Repository
public interface MaterialRequestItemRepository extends JpaRepository<MaterialRequestItem, Long> {
    
    // Recherche par demande
    List<MaterialRequestItem> findByMaterialRequest(MaterialRequest materialRequest);
    
    List<MaterialRequestItem> findByMaterialRequestOrderByPriorityAsc(MaterialRequest materialRequest);
    
    // Recherche par article de catalogue
    List<MaterialRequestItem> findByCatalogItem(CatalogItem catalogItem);
    
    // Articles libres (pas dans catalogue)
    List<MaterialRequestItem> findByCatalogItemIsNull();
    
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.catalogItem IS NULL AND mri.freeReference IS NOT NULL")
    List<MaterialRequestItem> findFreeReferenceItems();
    
    // Articles non complètement alloués
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.requestedQuantity > mri.quantityAllocated")
    List<MaterialRequestItem> findUnallocatedItems();
    
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.materialRequest = :request AND mri.requestedQuantity > mri.quantityAllocated")
    List<MaterialRequestItem> findUnallocatedItemsByRequest(@Param("request") MaterialRequest request);
    
    // Articles partiellement livrés
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.quantityDelivered > 0 AND mri.quantityDelivered < mri.quantityAllocated")
    List<MaterialRequestItem> findPartiallyDeliveredItems();
    
    // Articles complètement livrés
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE mri.quantityDelivered >= mri.requestedQuantity")
    List<MaterialRequestItem> findFullyDeliveredItems();
    
    // Recherche par référence
    List<MaterialRequestItem> findByFreeReferenceContainingIgnoreCase(String reference);
    
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE " +
           "(mri.catalogItem IS NOT NULL AND LOWER(mri.catalogItem.reference) LIKE LOWER(CONCAT('%', :reference, '%'))) OR " +
           "(mri.catalogItem IS NULL AND LOWER(mri.freeReference) LIKE LOWER(CONCAT('%', :reference, '%')))")
    List<MaterialRequestItem> findByAnyReference(@Param("reference") String reference);
    
    // Recherche textuelle
    @Query("SELECT mri FROM MaterialRequestItem mri WHERE " +
           "LOWER(mri.freeReference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mri.freeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mri.freeDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mri.freeBrand) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(mri.specifications) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<MaterialRequestItem> searchFreeItems(@Param("searchTerm") String searchTerm);
    
    // Articles par priorité
    List<MaterialRequestItem> findByPriority(Integer priority);
    
    List<MaterialRequestItem> findByPriorityLessThanEqualOrderByPriorityAsc(Integer maxPriority);
    
    // Articles acceptant des alternatives
    List<MaterialRequestItem> findByAlternativesAcceptedTrue();
    
    // Statistiques par fournisseur (via catalogue)
    @Query("SELECT mri.catalogItem.catalog.supplier.name, COUNT(mri) FROM MaterialRequestItem mri WHERE mri.catalogItem IS NOT NULL GROUP BY mri.catalogItem.catalog.supplier.name")
    List<Object[]> countItemsBySupplier();
    
    // Statistiques par catégorie
    @Query("SELECT mri.catalogItem.category, COUNT(mri) FROM MaterialRequestItem mri WHERE mri.catalogItem IS NOT NULL AND mri.catalogItem.category IS NOT NULL GROUP BY mri.catalogItem.category")
    List<Object[]> countItemsByCategory();
    
    // Total des quantités demandées
    @Query("SELECT SUM(mri.requestedQuantity) FROM MaterialRequestItem mri")
    Long sumTotalRequestedQuantity();
    
    @Query("SELECT SUM(mri.quantityAllocated) FROM MaterialRequestItem mri")
    Long sumTotalAllocatedQuantity();
    
    @Query("SELECT SUM(mri.quantityDelivered) FROM MaterialRequestItem mri")
    Long sumTotalDeliveredQuantity();
    
    // Valeur estimée totale
    @Query("SELECT SUM(mri.estimatedPrice * mri.requestedQuantity) FROM MaterialRequestItem mri WHERE mri.estimatedPrice IS NOT NULL")
    Double sumTotalEstimatedValue();
}