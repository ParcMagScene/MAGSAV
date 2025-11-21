package com.magscene.magsav.backend.controller;

import com.magsav.entities.*;
import com.magscene.magsav.backend.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des fournisseurs
 */
@RestController
@RequestMapping("/api/suppliers")
@CrossOrigin(origins = "*")
public class SupplierController {
    
    @Autowired
    private SupplierService supplierService;
    
    // *** CRUD FOURNISSEURS ***
    
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        List<Supplier> suppliers = supplierService.findAll();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Supplier>> getActiveSuppliers() {
        List<Supplier> suppliers = supplierService.findActiveSuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Optional<Supplier> supplier = supplierService.findById(id);
        return supplier.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {
        // Vérifier que le nom n'existe pas déjà
        if (!supplierService.isSupplierNameAvailable(supplier.getName())) {
            return ResponseEntity.badRequest().build();
        }
        
        Supplier savedSupplier = supplierService.save(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedSupplier);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, 
                                                   @Valid @RequestBody Supplier supplier) {
        Optional<Supplier> existingSupplier = supplierService.findById(id);
        if (!existingSupplier.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        supplier.setId(id);
        Supplier updatedSupplier = supplierService.save(supplier);
        return ResponseEntity.ok(updatedSupplier);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        try {
            supplierService.deleteSupplier(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateSupplier(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.ok().build();
    }
    
    // *** RECHERCHE FOURNISSEURS ***
    
    @GetMapping("/search")
    public ResponseEntity<List<Supplier>> searchSuppliers(@RequestParam String q) {
        List<Supplier> suppliers = supplierService.searchSuppliers(q);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/by-services")
    public ResponseEntity<List<Supplier>> getSuppliersByServices(
            @RequestParam(defaultValue = "false") boolean hasAfterSales,
            @RequestParam(defaultValue = "false") boolean hasRMA,
            @RequestParam(defaultValue = "false") boolean hasParts,
            @RequestParam(defaultValue = "false") boolean hasEquipment) {
        
        List<Supplier> suppliers = supplierService.findSuppliersByServices(
            hasAfterSales, hasRMA, hasParts, hasEquipment);
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/parts-suppliers")
    public ResponseEntity<List<Supplier>> getPartsSuppliers() {
        List<Supplier> suppliers = supplierService.findPartsSuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/sav-suppliers")
    public ResponseEntity<List<Supplier>> getSAVSuppliers() {
        List<Supplier> suppliers = supplierService.findSAVSuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    @GetMapping("/rma-suppliers")
    public ResponseEntity<List<Supplier>> getRMASuppliers() {
        List<Supplier> suppliers = supplierService.findRMASuppliers();
        return ResponseEntity.ok(suppliers);
    }
    
    // *** GESTION DES CATALOGUES ***
    
    @GetMapping("/{id}/catalogs")
    public ResponseEntity<List<SupplierCatalog>> getSupplierCatalogs(@PathVariable Long id) {
        List<SupplierCatalog> catalogs = supplierService.findCatalogsBySupplier(id);
        return ResponseEntity.ok(catalogs);
    }
    
    @PostMapping("/{id}/catalogs")
    public ResponseEntity<SupplierCatalog> importCatalog(@PathVariable Long id,
                                                        @RequestParam String name,
                                                        @RequestParam MultipartFile file) {
        try {
            SupplierCatalog catalog = supplierService.importCatalog(id, name, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(catalog);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/catalogs/{catalogId}")
    public ResponseEntity<Void> deleteCatalog(@PathVariable Long catalogId) {
        supplierService.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }
    
    // *** RECHERCHE DANS LES CATALOGUES ***
    
    @GetMapping("/catalog/search")
    public ResponseEntity<List<CatalogItem>> searchCatalogItems(@RequestParam String q) {
        List<CatalogItem> items = supplierService.searchCatalogItems(q);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/catalog/advanced-search")
    public ResponseEntity<List<CatalogItem>> advancedSearchCatalogItems(
            @RequestParam String q,
            @RequestParam(required = false) List<Long> supplierIds) {
        List<CatalogItem> items = supplierService.searchCatalogItems(q, supplierIds);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/{id}/catalog-items")
    public ResponseEntity<List<CatalogItem>> getSupplierCatalogItems(@PathVariable Long id) {
        List<CatalogItem> items = supplierService.findItemsBySupplier(id);
        return ResponseEntity.ok(items);
    }
    
    @GetMapping("/catalog/categories")
    public ResponseEntity<List<String>> getAvailableCategories() {
        List<String> categories = supplierService.getAvailableCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/catalog/brands")
    public ResponseEntity<List<String>> getAvailableBrands() {
        List<String> brands = supplierService.getAvailableBrands();
        return ResponseEntity.ok(brands);
    }
    
    // *** GESTION DES SEUILS ***
    
    @GetMapping("/with-threshold")
    public ResponseEntity<List<Supplier>> getSuppliersWithThreshold() {
        List<Supplier> suppliers = supplierService.findSuppliersWithThreshold();
        return ResponseEntity.ok(suppliers);
    }
    
    @PutMapping("/{id}/threshold")
    public ResponseEntity<Void> updateSupplierThreshold(@PathVariable Long id,
                                                        @RequestBody BigDecimal threshold) {
        supplierService.updateSupplierThreshold(id, threshold);
        return ResponseEntity.ok().build();
    }
    
    // *** CONFIGURATION DES SERVICES ***
    
    @PutMapping("/{id}/services")
    public ResponseEntity<Void> updateSupplierServices(@PathVariable Long id,
                                                       @RequestParam boolean hasAfterSales,
                                                       @RequestParam boolean hasRMA,
                                                       @RequestParam boolean hasParts,
                                                       @RequestParam boolean hasEquipment) {
        supplierService.updateSupplierServices(id, hasAfterSales, hasRMA, hasParts, hasEquipment);
        return ResponseEntity.ok().build();
    }
    
    // *** STATISTIQUES ***
    
    @GetMapping("/stats/count")
    public ResponseEntity<StatsResponse> getSupplierStats() {
        StatsResponse stats = new StatsResponse();
        stats.activeSuppliers = supplierService.countActiveSuppliers();
        stats.partsSuppliers = supplierService.countPartsSuppliers();
        stats.activeCatalogs = supplierService.countActiveCatalogs();
        stats.availableItems = supplierService.countAvailableItems();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/items-by-supplier")
    public ResponseEntity<List<Object[]>> getItemsBySupplierStats() {
        List<Object[]> stats = supplierService.getItemsBySupplierStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/items-by-category")
    public ResponseEntity<List<Object[]>> getItemsByCategoryStats() {
        List<Object[]> stats = supplierService.getItemsByCategoryStats();
        return ResponseEntity.ok(stats);
    }
    
    // *** VALIDATION ***
    
    @GetMapping("/validate/name")
    public ResponseEntity<Boolean> isSupplierNameAvailable(@RequestParam String name) {
        boolean available = supplierService.isSupplierNameAvailable(name);
        return ResponseEntity.ok(available);
    }
    
    @GetMapping("/validate/email")
    public ResponseEntity<Boolean> isSupplierEmailAvailable(@RequestParam String email) {
        boolean available = supplierService.isSupplierEmailAvailable(email);
        return ResponseEntity.ok(available);
    }
    
    // *** CLASSES INTERNES ***
    
    public static class StatsResponse {
        public long activeSuppliers;
        public long partsSuppliers;
        public long activeCatalogs;
        public long availableItems;
    }
}