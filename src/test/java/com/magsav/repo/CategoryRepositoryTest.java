package com.magsav.repo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategoryRepositoryTest {

    @Test
    void testResolveCategoryHierarchy_WithEmoji() {
        CategoryRepository repo = new CategoryRepository();
        
        // Test avec émoji ⚡ - comme la catégorie n'existe pas, elle devrait retourner 
        // une hiérarchie avec le nom nettoyé en sous-catégorie
        CategoryRepository.CategoryHierarchy hierarchy = repo.resolveCategoryHierarchy("⚡ Recepteur HF");
        
        assertEquals("", hierarchy.mainCategory());
        assertEquals("Recepteur HF", hierarchy.subCategory());  // Nom nettoyé sans emoji
        assertEquals("", hierarchy.subSubCategory());
    }
    
    @Test
    void testResolveCategoryHierarchy_WithoutEmoji() {
        CategoryRepository repo = new CategoryRepository();
        
        // Test sans émoji - comme la catégorie n'existe pas, elle devrait retourner
        // une hiérarchie avec le nom original en sous-catégorie  
        CategoryRepository.CategoryHierarchy hierarchy = repo.resolveCategoryHierarchy("Enceinte passive");
        
        assertEquals("", hierarchy.mainCategory());
        assertEquals("Enceinte passive", hierarchy.subCategory());
        assertEquals("", hierarchy.subSubCategory());
    }
    
    @Test
    void testResolveCategoryHierarchy_EmptyString() {
        CategoryRepository repo = new CategoryRepository();
        
        // Test avec chaîne vide
        CategoryRepository.CategoryHierarchy hierarchy = repo.resolveCategoryHierarchy("");
        
        assertEquals("", hierarchy.mainCategory());
        assertEquals("", hierarchy.subCategory());
        assertEquals("", hierarchy.subSubCategory());
    }
    
    @Test
    void testResolveCategoryHierarchy_UnknownCategory() {
        CategoryRepository repo = new CategoryRepository();
        
        // Test avec catégorie inconnue
        CategoryRepository.CategoryHierarchy hierarchy = repo.resolveCategoryHierarchy("Catégorie Inexistante");
        
        assertEquals("", hierarchy.mainCategory());
        assertEquals("Catégorie Inexistante", hierarchy.subCategory());
        assertEquals("", hierarchy.subSubCategory());
    }
}