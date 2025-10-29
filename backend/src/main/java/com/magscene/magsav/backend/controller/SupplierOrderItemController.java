package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.SupplierOrderItem;
import com.magscene.magsav.backend.repository.SupplierOrderItemRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des articles de commande fournisseur
 */
@RestController
@RequestMapping("/api/supplier-order-items")
@CrossOrigin(origins = "*")
public class SupplierOrderItemController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierOrderItemController.class);

    @Autowired
    private SupplierOrderItemRepository supplierOrderItemRepository;

    /**
     * RÃƒÂ©cupÃƒÂ¨re tous les articles de commande
     */
    @GetMapping
    public ResponseEntity<List<SupplierOrderItem>> getAllSupplierOrderItems() {
        try {
            List<SupplierOrderItem> items = supplierOrderItemRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            logger.info("RÃƒÂ©cupÃƒÂ©ration de {} articles de commande", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des articles de commande: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re un article de commande par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierOrderItem> getSupplierOrderItemById(@PathVariable Long id) {
        try {
            Optional<SupplierOrderItem> item = supplierOrderItemRepository.findById(id);
            if (item.isPresent()) {
                logger.info("Article trouvÃƒÂ©: {}", item.get().getItemName());
                return ResponseEntity.ok(item.get());
            } else {
                logger.warn("Article non trouvÃƒÂ© avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration de l'article {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les articles d'une commande fournisseur
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<SupplierOrderItem>> getItemsByOrderId(@PathVariable Long orderId) {
        try {
            List<SupplierOrderItem> items = supplierOrderItemRepository.findBySupplierOrderId(orderId);
            logger.info("Articles pour commande {}: {} trouvÃƒÂ©s", orderId, items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des articles pour commande {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CrÃƒÂ©e un nouvel article de commande
     */
    @PostMapping
    public ResponseEntity<SupplierOrderItem> createSupplierOrderItem(@Valid @RequestBody SupplierOrderItem item) {
        try {
            SupplierOrderItem savedItem = supplierOrderItemRepository.save(item);
            logger.info("Nouvel article crÃƒÂ©ÃƒÂ©: {} (ID: {})", savedItem.getItemName(), savedItem.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation de l'article: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met ÃƒÂ  jour un article de commande
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierOrderItem> updateSupplierOrderItem(@PathVariable Long id, @Valid @RequestBody SupplierOrderItem itemDetails) {
        try {
            Optional<SupplierOrderItem> itemOptional = supplierOrderItemRepository.findById(id);
            if (itemOptional.isPresent()) {
                SupplierOrderItem item = itemOptional.get();
                
                // Mise ÃƒÂ  jour des champs
                item.setItemReference(itemDetails.getItemReference());
                item.setItemName(itemDetails.getItemName());
                item.setDescription(itemDetails.getDescription());
                item.setQuantity(itemDetails.getQuantity());
                item.setQuantityReceived(itemDetails.getQuantityReceived());
                item.setUnit(itemDetails.getUnit());
                item.setUnitPrice(itemDetails.getUnitPrice());
                item.setNotes(itemDetails.getNotes());
                
                // Recalculer le montant total
                item.calculateTotalAmount();

                SupplierOrderItem updatedItem = supplierOrderItemRepository.save(item);
                logger.info("Article mis ÃƒÂ  jour: {} (ID: {})", updatedItem.getItemName(), updatedItem.getId());
                return ResponseEntity.ok(updatedItem);
            } else {
                logger.warn("Article non trouvÃƒÂ© pour mise ÃƒÂ  jour avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour de l'article {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un article de commande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplierOrderItem(@PathVariable Long id) {
        try {
            if (supplierOrderItemRepository.existsById(id)) {
                supplierOrderItemRepository.deleteById(id);
                logger.info("Article supprimÃƒÂ© avec l'ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Article non trouvÃƒÂ© pour suppression avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'article {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des articles par nom de produit
     */
    @GetMapping("/search")
    public ResponseEntity<List<SupplierOrderItem>> searchItems(@RequestParam String productName) {
        try {
            List<SupplierOrderItem> items = supplierOrderItemRepository.findByItemNameContainingIgnoreCase(productName);
            logger.info("Recherche '{}': {} articles trouvÃƒÂ©s", productName, items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche d'articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les articles partiellement reÃƒÂ§us
     */
    @GetMapping("/partially-received")
    public ResponseEntity<List<SupplierOrderItem>> getPartiallyReceivedItems() {
        try {
            List<SupplierOrderItem> items = supplierOrderItemRepository.findPartiallyReceived();
            logger.info("Articles partiellement reÃƒÂ§us: {} trouvÃƒÂ©s", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des articles partiellement reÃƒÂ§us: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les articles non reÃƒÂ§us
     */
    @GetMapping("/not-received")
    public ResponseEntity<List<SupplierOrderItem>> getNotReceivedItems() {
        try {
            List<SupplierOrderItem> items = supplierOrderItemRepository.findNotReceived();
            logger.info("Articles non reÃƒÂ§us: {} trouvÃƒÂ©s", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des articles non reÃƒÂ§us: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met ÃƒÂ  jour la quantitÃƒÂ© reÃƒÂ§ue d'un article
     */
    @PatchMapping("/{id}/received-quantity")
    public ResponseEntity<SupplierOrderItem> updateReceivedQuantity(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Optional<SupplierOrderItem> itemOptional = supplierOrderItemRepository.findById(id);
            if (itemOptional.isPresent()) {
                SupplierOrderItem item = itemOptional.get();
                Integer receivedQuantity = request.get("quantityReceived");
                
                if (receivedQuantity != null && receivedQuantity >= 0 && receivedQuantity <= item.getQuantity()) {
                    item.setQuantityReceived(receivedQuantity);
                    
                    SupplierOrderItem updatedItem = supplierOrderItemRepository.save(item);
                    logger.info("QuantitÃƒÂ© reÃƒÂ§ue mise ÃƒÂ  jour pour article {}: {} sur {}", id, receivedQuantity, item.getQuantity());
                    return ResponseEntity.ok(updatedItem);
                } else {
                    logger.warn("QuantitÃƒÂ© reÃƒÂ§ue invalide: {} pour article {}", receivedQuantity, id);
                    return ResponseEntity.badRequest().build();
                }
            } else {
                logger.warn("Article non trouvÃƒÂ© pour mise ÃƒÂ  jour quantitÃƒÂ© reÃƒÂ§ue avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour de la quantitÃƒÂ© reÃƒÂ§ue pour article {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les statistiques des articles
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getItemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Comptage total
            long totalItems = supplierOrderItemRepository.count();
            stats.put("total", totalItems);
            
            // Articles en attente de rÃƒÂ©ception
            long pendingItems = supplierOrderItemRepository.countPendingItems();
            stats.put("pending", pendingItems);
            
            // Articles partiellement reÃƒÂ§us
            long partiallyReceived = supplierOrderItemRepository.findPartiallyReceived().size();
            stats.put("partiallyReceived", partiallyReceived);
            
            // Articles non reÃƒÂ§us
            long notReceived = supplierOrderItemRepository.findNotReceived().size();
            stats.put("notReceived", notReceived);
            
            logger.info("Statistiques articles gÃƒÂ©nÃƒÂ©rÃƒÂ©es: {} articles au total", totalItems);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la gÃƒÂ©nÃƒÂ©ration des statistiques articles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

