package com.magscene.magsav.backend.service;

import com.magsav.entities.*;
import com.magsav.enums.ImportStatus;
import com.magscene.magsav.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des fournisseurs et de leurs catalogues
 */
@Service
@Transactional
public class SupplierService {
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private SupplierCatalogRepository catalogRepository;
    
    @Autowired
    private CatalogItemRepository catalogItemRepository;
    
    @Autowired
    private GroupedOrderRepository groupedOrderRepository;
    
    // *** GESTION DES FOURNISSEURS ***
    
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }
    
    public List<Supplier> findActiveSuppliers() {
        return supplierRepository.findByActiveTrueOrderByName();
    }
    
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id);
    }
    
    public Optional<Supplier> findByName(String name) {
        return supplierRepository.findByName(name);
    }
    
    public List<Supplier> searchSuppliers(String searchTerm) {
        return supplierRepository.searchSuppliers(searchTerm);
    }
    
    public Supplier save(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    
    public Supplier createSupplier(String name, String email, String contactPerson) {
        Supplier supplier = new Supplier(name);
        supplier.setEmail(email);
        supplier.setContactPerson(contactPerson);
        return supplierRepository.save(supplier);
    }
    
    public void deleteSupplier(Long id) {
        // Vérifier qu'il n'y a pas de commandes en cours
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isPresent()) {
            List<GroupedOrder> activeOrders = groupedOrderRepository.findOpenOrdersBySupplier(supplier.get());
            if (!activeOrders.isEmpty()) {
                throw new IllegalStateException("Impossible de supprimer le fournisseur : commandes en cours");
            }
            supplierRepository.deleteById(id);
        }
    }
    
    public void deactivateSupplier(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isPresent()) {
            supplier.get().setActive(false);
            supplierRepository.save(supplier.get());
        }
    }
    
    // *** RECHERCHE PAR SERVICES ***
    
    public List<Supplier> findSuppliersByServices(boolean hasAfterSales, boolean hasRMA, 
                                                  boolean hasParts, boolean hasEquipment) {
        return supplierRepository.findByServices(hasAfterSales, hasRMA, hasParts, hasEquipment);
    }
    
    public List<Supplier> findPartsSuppliers() {
        return supplierRepository.findByHasPartsServiceTrue();
    }
    
    public List<Supplier> findSAVSuppliers() {
        return supplierRepository.findByHasAfterSalesServiceTrue();
    }
    
    public List<Supplier> findRMASuppliers() {
        return supplierRepository.findByHasRMAServiceTrue();
    }
    
    // *** GESTION DES CATALOGUES ***
    
    public List<SupplierCatalog> findCatalogsBySupplier(Long supplierId) {
        return catalogRepository.findBySupplierId(supplierId);
    }
    
    public List<SupplierCatalog> findActiveCatalogs() {
        return catalogRepository.findByActiveTrueOrderByImportDateDesc();
    }
    
    public SupplierCatalog importCatalog(Long supplierId, String catalogName, MultipartFile file) {
        Optional<Supplier> supplierOpt = supplierRepository.findById(supplierId);
        if (!supplierOpt.isPresent()) {
            throw new IllegalArgumentException("Fournisseur introuvable : " + supplierId);
        }
        
        Supplier supplier = supplierOpt.get();
        
        // Créer le catalogue
        SupplierCatalog catalog = new SupplierCatalog(supplier, catalogName);
        catalog.setFileName(file.getOriginalFilename());
        catalog.setFileSize(file.getSize());
        catalog.setImportStatus(ImportStatus.PENDING);
        
        // Déterminer le format
        String filename = file.getOriginalFilename().toLowerCase();
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            catalog.setFileFormat(CatalogFormat.EXCEL);
        } else if (filename.endsWith(".pdf")) {
            catalog.setFileFormat(CatalogFormat.PDF);
        } else if (filename.endsWith(".csv")) {
            catalog.setFileFormat(CatalogFormat.CSV);
        } else {
            catalog.setFileFormat(CatalogFormat.CUSTOM_OTHER);
        }
        
        catalog = catalogRepository.save(catalog);
        
        // TODO: Implémenter l'import asynchrone du fichier
        // Pour l'instant, marquer comme terminé
        catalog.setImportStatus(ImportStatus.COMPLETED);
        catalog.setImportLog("Import simulé - à implémenter");
        
        return catalogRepository.save(catalog);
    }
    
    public void deleteCatalog(Long catalogId) {
        catalogRepository.deleteById(catalogId);
    }
    
    // *** RECHERCHE DANS LES CATALOGUES ***
    
    public List<CatalogItem> searchCatalogItems(String query) {
        return catalogItemRepository.searchItems(query);
    }
    
    public List<CatalogItem> searchCatalogItems(String query, List<Long> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return searchCatalogItems(query);
        }
        
        List<CatalogItem> allItems = catalogItemRepository.findBySupplierIds(supplierIds);
        return allItems.stream()
            .filter(item -> matchesQuery(item, query))
            .toList();
    }
    
    private boolean matchesQuery(CatalogItem item, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        
        String lowerQuery = query.toLowerCase();
        return (item.getReference() != null && item.getReference().toLowerCase().contains(lowerQuery)) ||
               (item.getName() != null && item.getName().toLowerCase().contains(lowerQuery)) ||
               (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery)) ||
               (item.getBrand() != null && item.getBrand().toLowerCase().contains(lowerQuery)) ||
               (item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerQuery));
    }
    
    public List<CatalogItem> findItemsBySupplier(Long supplierId) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        if (supplier.isPresent()) {
            return catalogItemRepository.findBySupplierAndAvailable(supplier.get());
        }
        return List.of();
    }
    
    public List<String> getAvailableCategories() {
        return catalogItemRepository.findDistinctCategories();
    }
    
    public List<String> getAvailableBrands() {
        return catalogItemRepository.findDistinctBrands();
    }
    
    // *** GESTION DES SEUILS ***
    
    public List<Supplier> findSuppliersWithThreshold() {
        return supplierRepository.findSuppliersWithThreshold();
    }
    
    public boolean checkThresholdReached(GroupedOrder order) {
        if (order.getSupplier() == null || !order.getSupplier().hasThresholdConfigured()) {
            return false;
        }
        
        return order.getCurrentAmount().compareTo(order.getSupplier().getFreeShippingThreshold()) >= 0;
    }
    
    public void updateSupplierThreshold(Long supplierId, BigDecimal threshold) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        if (supplier.isPresent()) {
            supplier.get().setFreeShippingThreshold(threshold);
            supplierRepository.save(supplier.get());
        }
    }
    
    // *** STATISTIQUES ***
    
    public long countActiveSuppliers() {
        return supplierRepository.countActiveSuppliers();
    }
    
    public long countPartsSuppliers() {
        return supplierRepository.countPartsSuppliers();
    }
    
    public long countActiveCatalogs() {
        return catalogRepository.countActiveCatalogs();
    }
    
    public long countAvailableItems() {
        return catalogItemRepository.countAvailableItems();
    }
    
    public List<Object[]> getItemsBySupplierStats() {
        return catalogItemRepository.countItemsBySupplier();
    }
    
    public List<Object[]> getItemsByCategoryStats() {
        return catalogItemRepository.countItemsByCategory();
    }
    
    // *** MÉTHODES UTILITAIRES ***
    
    public boolean isSupplierNameAvailable(String name) {
        return !supplierRepository.findByName(name).isPresent();
    }
    
    public boolean isSupplierEmailAvailable(String email) {
        return !supplierRepository.findByEmail(email).isPresent();
    }
    
    public void updateSupplierServices(Long supplierId, boolean hasAfterSales, boolean hasRMA, 
                                      boolean hasParts, boolean hasEquipment) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        if (supplier.isPresent()) {
            Supplier s = supplier.get();
            s.setHasAfterSalesService(hasAfterSales);
            s.setHasRMAService(hasRMA);
            s.setHasPartsService(hasParts);
            s.setHasEquipmentService(hasEquipment);
            supplierRepository.save(s);
        }
    }
}