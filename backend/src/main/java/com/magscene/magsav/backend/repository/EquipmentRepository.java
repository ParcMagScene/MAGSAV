package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

/**
 * Repository pour l'entité Equipment avec Spring Data JPA
 * Utilise les nouvelles fonctionnalités Java 21 pour les requêtes
 */
@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    /**
     * Recherche par nom (insensible ÃƒÂ  la casse)
     */
    List<Equipment> findByNameContainingIgnoreCase(String name);
    
    /**
     * Recherche par catÃƒÂ©gorie
     */
    List<Equipment> findByCategoryIgnoreCase(String category);
    
    /**
     * Recherche par catÃƒÂ©gorie (exacte)
     */
    List<Equipment> findByCategory(String category);
    
    /**
     * Recherche par statut
     */
    List<Equipment> findByStatus(Equipment.Status status);
    
    /**
     * Recherche par QR Code
     */
    Optional<Equipment> findByQrCode(String qrCode);
    
    /**
     * Recherche par numÃƒÂ©ro de sÃƒÂ©rie
     */
    Optional<Equipment> findBySerialNumber(String serialNumber);
    
    /**
     * Recherche par marque
     */
    List<Equipment> findByBrandContainingIgnoreCase(String brand);
    
    /**
     * Compte les ÃƒÂ©quipements par statut - utilise le nouveau switch Java 21
     */
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.status = :status")
    long countByStatus(@Param("status") Equipment.Status status);
    
    /**
     * RÃƒÂ©cupÃƒÂ¨re toutes les catÃƒÂ©gories distinctes
     */
    @Query("SELECT DISTINCT e.category FROM Equipment e ORDER BY e.category")
    List<String> findAllCategories();
    
    /**
     * Recherche avancÃƒÂ©e avec critÃƒÂ¨res multiples
     */
    @Query("""
        SELECT e FROM Equipment e 
        WHERE (:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))
        AND (:category IS NULL OR LOWER(e.category) = LOWER(:category))
        AND (:brand IS NULL OR LOWER(e.brand) LIKE LOWER(CONCAT('%', :brand, '%')))
        AND (:status IS NULL OR e.status = :status)
        ORDER BY e.updatedAt DESC
        """)
    List<Equipment> findBySearchCriteria(
        @Param("name") String name,
        @Param("category") String category,
        @Param("brand") String brand,
        @Param("status") Equipment.Status status
    );
    
    /**
     * Calcule la valeur totale des ÃƒÂ©quipements
     */
    @Query("SELECT COALESCE(SUM(e.purchasePrice), 0) FROM Equipment e WHERE e.purchasePrice IS NOT NULL")
    Double calculateTotalValue();
    
    /**
     * Statistiques par catÃƒÂ©gorie
     */
    @Query("SELECT e.category, COUNT(e) FROM Equipment e GROUP BY e.category ORDER BY COUNT(e) DESC")
    List<Object[]> getEquipmentCountByCategory();
    
    /**
     * Ãƒâ€°quipements nÃƒÂ©cessitant une maintenance (exemple: anciens ou en panne)
     */
    @Query("""
        SELECT e FROM Equipment e 
        WHERE e.status IN (com.magscene.magsav.backend.entity.Equipment$Status.MAINTENANCE, 
                          com.magscene.magsav.backend.entity.Equipment$Status.OUT_OF_ORDER)
        ORDER BY e.updatedAt ASC
        """)
    List<Equipment> findEquipmentNeedingMaintenance();
}

