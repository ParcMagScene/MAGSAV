package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Category;
import com.magscene.magsav.backend.dto.CategoryDTO;
import com.magscene.magsav.backend.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class CategoryRestController {

    @Autowired
    private CategoryRepository categoryRepository;

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer toutes les catÃƒÆ’Ã‚Â©gories
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryRepository.findAll()
                .stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer une catÃƒÆ’Ã‚Â©gorie par son ID
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(c -> ResponseEntity.ok(new CategoryDTO(c)))
                      .orElse(ResponseEntity.notFound().build());
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer les catÃƒÆ’Ã‚Â©gories racines (sans parent)
    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByDisplayOrderAscNameAsc();
        return ResponseEntity.ok(rootCategories);
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer les sous-catÃƒÆ’Ã‚Â©gories d'un parent
    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<Category>> getChildCategories(@PathVariable Long parentId) {
        List<Category> childCategories = categoryRepository.findByParentIdOrderByDisplayOrderAscNameAsc(parentId);
        return ResponseEntity.ok(childCategories);
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer les catÃƒÆ’Ã‚Â©gories actives
    @GetMapping("/active")
    public ResponseEntity<List<Category>> getActiveCategories() {
        List<Category> activeCategories = categoryRepository.findByActiveTrue();
        return ResponseEntity.ok(activeCategories);
    }

    // Rechercher des catÃƒÆ’Ã‚Â©gories
    @GetMapping("/search")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String q) {
        List<Category> searchResults = categoryRepository.searchCategories(q);
        return ResponseEntity.ok(searchResults);
    }

    // CrÃƒÆ’Ã‚Â©er une nouvelle catÃƒÆ’Ã‚Â©gorie
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        // VÃƒÆ’Ã‚Â©rifier les doublons
        if (category.getParent() != null) {
            boolean exists = categoryRepository.existsByNameIgnoreCaseAndParentId(
                category.getName(), category.getParent().getId());
            if (exists) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            boolean exists = categoryRepository.existsByNameIgnoreCaseAndParentIsNull(category.getName());
            if (exists) {
                return ResponseEntity.badRequest().build();
            }
        }

        // DÃƒÆ’Ã‚Â©finir les valeurs par dÃƒÆ’Ã‚Â©faut
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        if (category.getActive() == null) {
            category.setActive(true);
        }
        if (category.getDisplayOrder() == null) {
            category.setDisplayOrder(0);
        }

        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCategory);
    }

    // Mettre ÃƒÆ’Ã‚Â  jour une catÃƒÆ’Ã‚Â©gorie
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category categoryDetails) {
        Optional<Category> optionalCategory = categoryRepository.findById(id);
        if (!optionalCategory.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Category category = optionalCategory.get();
        
        // VÃƒÆ’Ã‚Â©rifier les doublons si le nom change
        if (!category.getName().equalsIgnoreCase(categoryDetails.getName())) {
            if (categoryDetails.getParent() != null) {
                boolean exists = categoryRepository.existsByNameIgnoreCaseAndParentId(
                    categoryDetails.getName(), categoryDetails.getParent().getId());
                if (exists) {
                    return ResponseEntity.badRequest().build();
                }
            } else {
                boolean exists = categoryRepository.existsByNameIgnoreCaseAndParentIsNull(categoryDetails.getName());
                if (exists) {
                    return ResponseEntity.badRequest().build();
                }
            }
        }

        // Mettre ÃƒÆ’Ã‚Â  jour les champs
        category.setName(categoryDetails.getName());
        category.setDescription(categoryDetails.getDescription());
        category.setParent(categoryDetails.getParent());
        category.setColor(categoryDetails.getColor());
        category.setIcon(categoryDetails.getIcon());
        category.setDisplayOrder(categoryDetails.getDisplayOrder());
        category.setActive(categoryDetails.getActive());
        category.setUpdatedAt(LocalDateTime.now());

        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // Supprimer une catÃƒÆ’Ã‚Â©gorie
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (!category.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        // VÃƒÆ’Ã‚Â©rifier s'il y a des ÃƒÆ’Ã‚Â©quipements ou des sous-catÃƒÆ’Ã‚Â©gories
        Long equipmentCount = categoryRepository.countEquipmentsByCategoryId(id);
        boolean hasChildren = categoryRepository.existsByParentId(id);
        
        if (equipmentCount > 0 || hasChildren) {
            // Ne pas supprimer si utilisÃƒÆ’Ã‚Â©e, retourner un conflit
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Obtenir les statistiques d'une catÃƒÆ’Ã‚Â©gorie
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getCategoryStats(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (!category.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("equipmentCount", categoryRepository.countEquipmentsByCategoryId(id));
        stats.put("equipmentCountRecursive", categoryRepository.countEquipmentsInCategoryTreeRecursive(id));
        stats.put("hasChildren", categoryRepository.existsByParentId(id));
        stats.put("level", category.get().getLevel());
        stats.put("path", category.get().getFullPath());

        return ResponseEntity.ok(stats);
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer toutes les catÃƒÆ’Ã‚Â©gories avec le nombre d'ÃƒÆ’Ã‚Â©quipements
    @GetMapping("/with-counts")
    public ResponseEntity<List<Map<String, Object>>> getCategoriesWithCounts() {
        List<Object[]> results = categoryRepository.findAllCategoriesWithEquipmentCount();
        List<Map<String, Object>> categoriesWithCounts = results.stream()
            .map(result -> {
                Category category = (Category) result[0];
                Long count = (Long) result[1];
                Map<String, Object> categoryData = new HashMap<>();
                categoryData.put("category", category);
                categoryData.put("equipmentCount", count);
                return categoryData;
            })
            .toList();
        
        return ResponseEntity.ok(categoriesWithCounts);
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer les catÃƒÆ’Ã‚Â©gories les plus utilisÃƒÆ’Ã‚Â©es
    @GetMapping("/most-used")
    public ResponseEntity<List<Category>> getMostUsedCategories() {
        List<Category> mostUsedCategories = categoryRepository.findMostUsedCategories();
        return ResponseEntity.ok(mostUsedCategories);
    }

    // RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â©rer le chemin complet d'une catÃƒÆ’Ã‚Â©gorie
    @GetMapping("/{id}/path")
    public ResponseEntity<String> getCategoryPath(@PathVariable Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (!category.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        String path = category.get().getFullPath();
        return ResponseEntity.ok(path);
    }

    // DÃƒÆ’Ã‚Â©placer une catÃƒÆ’Ã‚Â©gorie vers un nouveau parent
    @PutMapping("/{id}/move")
    public ResponseEntity<Category> moveCategory(@PathVariable Long id, @RequestBody Map<String, Long> moveData) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (!categoryOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Category category = categoryOpt.get();
        Long newParentId = moveData.get("parentId");
        
        // VÃƒÆ’Ã‚Â©rifier que la catÃƒÆ’Ã‚Â©gorie ne devient pas son propre parent ou descendant
        if (newParentId != null && newParentId.equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        Category newParent = null;
        if (newParentId != null) {
            Optional<Category> newParentOpt = categoryRepository.findById(newParentId);
            if (!newParentOpt.isPresent()) {
                return ResponseEntity.badRequest().build();
            }
            newParent = newParentOpt.get();
            
            // VÃƒÆ’Ã‚Â©rifier qu'on ne crÃƒÆ’Ã‚Â©e pas de cycle
            if (isDescendantOf(newParent, category)) {
                return ResponseEntity.badRequest().build();
            }
        }

        category.setParent(newParent);
        category.setUpdatedAt(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    // MÃƒÆ’Ã‚Â©thode utilitaire pour vÃƒÆ’Ã‚Â©rifier les cycles
    private boolean isDescendantOf(Category potential, Category ancestor) {
        Category current = potential;
        while (current != null) {
            if (current.getId().equals(ancestor.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    // Activer/dÃƒÆ’Ã‚Â©sactiver une catÃƒÆ’Ã‚Â©gorie
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<Category> toggleCategoryStatus(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (!categoryOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Category category = categoryOpt.get();
        category.setActive(!category.getActive());
        category.setUpdatedAt(LocalDateTime.now());
        
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }
}


