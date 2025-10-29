package com.magscene.magsav.backend.repository;

import java.util.List;
import com.magscene.magsav.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // RÃƒÂ©cupÃƒÂ©rer les catÃƒÂ©gories racines (sans parent)
    List<Category> findByParentIsNullOrderByDisplayOrderAscNameAsc();
    
    // RÃƒÂ©cupÃƒÂ©rer les sous-catÃƒÂ©gories d'un parent
    List<Category> findByParentIdOrderByDisplayOrderAscNameAsc(Long parentId);
    
    // RÃƒÂ©cupÃƒÂ©rer les catÃƒÂ©gories actives
    List<Category> findByActiveTrue();
    
    // RÃƒÂ©cupÃƒÂ©rer les catÃƒÂ©gories inactives
    List<Category> findByActiveFalse();
    
    // Recherche par nom (insensible ÃƒÂ  la casse)
    List<Category> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
    
    // Recherche par description (insensible ÃƒÂ  la casse)
    List<Category> findByDescriptionContainingIgnoreCaseOrderByNameAsc(String description);
    
    // VÃƒÂ©rifier si un nom de catÃƒÂ©gorie existe dÃƒÂ©jÃƒÂ 
    boolean existsByNameIgnoreCase(String name);
    
    // VÃƒÂ©rifier si un nom existe dÃƒÂ©jÃƒÂ  pour un parent donnÃƒÂ© (ÃƒÂ©viter les doublons dans une mÃƒÂªme branche)
    boolean existsByNameIgnoreCaseAndParentId(String name, Long parentId);
    
    // VÃƒÂ©rifier si un nom existe dÃƒÂ©jÃƒÂ  dans les catÃƒÂ©gories racines
    boolean existsByNameIgnoreCaseAndParentIsNull(String name);
    
    // Compter les ÃƒÂ©quipements dans une catÃƒÂ©gorie
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.categoryEntity.id = :categoryId")
    Long countEquipmentsByCategoryId(@Param("categoryId") Long categoryId);
    
    // Compter rÃƒÂ©cursivement les ÃƒÂ©quipements dans une catÃƒÂ©gorie et ses sous-catÃƒÂ©gories
    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "SELECT id, parent_id FROM categories WHERE id = :categoryId " +
            "UNION ALL " +
            "SELECT c.id, c.parent_id FROM categories c " +
            "INNER JOIN category_tree ct ON c.parent_id = ct.id" +
            ") " +
            "SELECT COUNT(*) FROM equipment e " +
            "WHERE e.category_entity_id IN (SELECT id FROM category_tree)",
            nativeQuery = true)
    Long countEquipmentsInCategoryTreeRecursive(@Param("categoryId") Long categoryId);
    
    // RÃƒÂ©cupÃƒÂ©rer toutes les catÃƒÂ©gories avec le nombre d'ÃƒÂ©quipements
    @Query("SELECT c, COUNT(e) FROM Category c LEFT JOIN Equipment e ON c.id = e.categoryEntity.id GROUP BY c")
    List<Object[]> findAllCategoriesWithEquipmentCount();
    
    // RÃƒÂ©cupÃƒÂ©rer l'arbre complet des catÃƒÂ©gories avec leurs enfants
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.displayOrder ASC, c.name ASC")
    List<Category> findRootCategoriesWithChildren();
    
    // RÃƒÂ©cupÃƒÂ©rer les catÃƒÂ©gories d'un niveau spÃƒÂ©cifique dans la hiÃƒÂ©rarchie
    @Query(value = "WITH RECURSIVE category_levels AS (" +
            "SELECT id, name, parent_id, 0 as level FROM categories WHERE parent_id IS NULL " +
            "UNION ALL " +
            "SELECT c.id, c.name, c.parent_id, cl.level + 1 " +
            "FROM categories c " +
            "INNER JOIN category_levels cl ON c.parent_id = cl.id" +
            ") " +
            "SELECT c.* FROM categories c " +
            "INNER JOIN category_levels cl ON c.id = cl.id " +
            "WHERE cl.level = :level " +
            "ORDER BY c.display_order ASC, c.name ASC",
            nativeQuery = true)
    List<Category> findCategoriesByLevel(@Param("level") int level);
    
    // RÃƒÂ©cupÃƒÂ©rer le chemin complet vers une catÃƒÂ©gorie
    @Query(value = "WITH RECURSIVE category_path AS (" +
            "SELECT id, name, parent_id, name as path FROM categories WHERE id = :categoryId " +
            "UNION ALL " +
            "SELECT c.id, c.name, c.parent_id, CONCAT(c.name, ' > ', cp.path) " +
            "FROM categories c " +
            "INNER JOIN category_path cp ON c.id = cp.parent_id" +
            ") " +
            "SELECT path FROM category_path WHERE parent_id IS NULL",
            nativeQuery = true)
    String findCategoryPath(@Param("categoryId") Long categoryId);
    
    // VÃƒÂ©rifier si une catÃƒÂ©gorie a des sous-catÃƒÂ©gories
    boolean existsByParentId(Long parentId);
    
    // RÃƒÂ©cupÃƒÂ©rer les catÃƒÂ©gories les plus utilisÃƒÂ©es (avec le plus d'ÃƒÂ©quipements)
    @Query("SELECT c FROM Category c LEFT JOIN c.equipment e GROUP BY c ORDER BY COUNT(e) DESC")
    List<Category> findMostUsedCategories();
    
    // Recherche globale dans les catÃƒÂ©gories (nom + description)
    @Query("SELECT c FROM Category c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.name ASC")
    List<Category> searchCategories(@Param("searchTerm") String searchTerm);
}

