package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.SupplierOrder;
import com.magscene.magsav.backend.entity.SupplierOrder.OrderStatus;
import com.magscene.magsav.backend.entity.SupplierOrderItem;
import com.magscene.magsav.backend.repository.SupplierOrderRepository;
import com.magscene.magsav.backend.repository.SupplierOrderItemRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des commandes fournisseurs
 */
@RestController
@RequestMapping("/api/supplier-orders")
@CrossOrigin(origins = "*")
public class SupplierOrderController {

    private static final Logger logger = LoggerFactory.getLogger(SupplierOrderController.class);

    @Autowired
    private SupplierOrderRepository supplierOrderRepository;

    @Autowired
    private SupplierOrderItemRepository supplierOrderItemRepository;

    /**
     * RÃƒÂ©cupÃƒÂ¨re toutes les commandes fournisseurs
     */
    @GetMapping
    public ResponseEntity<List<SupplierOrder>> getAllSupplierOrders() {
        try {
            List<SupplierOrder> orders = supplierOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            logger.info("RÃƒÂ©cupÃƒÂ©ration de {} commandes fournisseurs", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes fournisseurs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re une commande fournisseur par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierOrder> getSupplierOrderById(@PathVariable Long id) {
        try {
            Optional<SupplierOrder> order = supplierOrderRepository.findById(id);
            if (order.isPresent()) {
                logger.info("Commande trouvÃƒÂ©e: {}", order.get().getOrderNumber());
                return ResponseEntity.ok(order.get());
            } else {
                logger.warn("Commande non trouvÃƒÂ©e avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration de la commande {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CrÃƒÂ©e une nouvelle commande fournisseur
     */
    @PostMapping
    public ResponseEntity<SupplierOrder> createSupplierOrder(@Valid @RequestBody SupplierOrder order) {
        try {
            // GÃƒÂ©nÃƒÂ©ration automatique du numÃƒÂ©ro de commande si non fourni
            if (order.getOrderNumber() == null || order.getOrderNumber().isEmpty()) {
                order.setOrderNumber(generateOrderNumber());
            }
            
            SupplierOrder savedOrder = supplierOrderRepository.save(order);
            logger.info("Nouvelle commande crÃƒÂ©ÃƒÂ©e: {} (ID: {})", savedOrder.getOrderNumber(), savedOrder.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation de la commande: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met ÃƒÂ  jour une commande fournisseur
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierOrder> updateSupplierOrder(@PathVariable Long id, @Valid @RequestBody SupplierOrder orderDetails) {
        try {
            Optional<SupplierOrder> orderOptional = supplierOrderRepository.findById(id);
            if (orderOptional.isPresent()) {
                SupplierOrder order = orderOptional.get();
                
                // Mise ÃƒÂ  jour des champs
                order.setOrderNumber(orderDetails.getOrderNumber());
                order.setType(orderDetails.getType());
                order.setStatus(orderDetails.getStatus());
                order.setSupplierName(orderDetails.getSupplierName());
                order.setSupplierContact(orderDetails.getSupplierContact());
                order.setSupplierEmail(orderDetails.getSupplierEmail());
                order.setSupplierPhone(orderDetails.getSupplierPhone());
                order.setSupplierAddress(orderDetails.getSupplierAddress());
                order.setOrderDate(orderDetails.getOrderDate());
                order.setExpectedDeliveryDate(orderDetails.getExpectedDeliveryDate());
                order.setActualDeliveryDate(orderDetails.getActualDeliveryDate());
                order.setDeliveryAddress(orderDetails.getDeliveryAddress());
                order.setDeliveryContact(orderDetails.getDeliveryContact());
                order.setAmountHT(orderDetails.getAmountHT());
                order.setVatRate(orderDetails.getVatRate());
                order.setAmountTTC(orderDetails.getAmountTTC());
                order.setShippingCost(orderDetails.getShippingCost());
                order.setPaymentTerms(orderDetails.getPaymentTerms());
                order.setPaymentMethod(orderDetails.getPaymentMethod());
                order.setNotes(orderDetails.getNotes());
                order.setInternalNotes(orderDetails.getInternalNotes());
                order.setTrackingNumber(orderDetails.getTrackingNumber());
                order.setCarrierName(orderDetails.getCarrierName());
                order.setOrderedBy(orderDetails.getOrderedBy());
                order.setApprovedBy(orderDetails.getApprovedBy());
                order.setApprovalDate(orderDetails.getApprovalDate());
                order.setProject(orderDetails.getProject());

                SupplierOrder updatedOrder = supplierOrderRepository.save(order);
                logger.info("Commande mise ÃƒÂ  jour: {} (ID: {})", updatedOrder.getOrderNumber(), updatedOrder.getId());
                return ResponseEntity.ok(updatedOrder);
            } else {
                logger.warn("Commande non trouvÃƒÂ©e pour mise ÃƒÂ  jour avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour de la commande {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime une commande fournisseur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplierOrder(@PathVariable Long id) {
        try {
            if (supplierOrderRepository.existsById(id)) {
                // Supprimer d'abord les items de la commande
                List<SupplierOrderItem> items = supplierOrderItemRepository.findBySupplierOrderId(id);
                supplierOrderItemRepository.deleteAll(items);
                
                // Puis supprimer la commande
                supplierOrderRepository.deleteById(id);
                logger.info("Commande supprimÃƒÂ©e avec l'ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Commande non trouvÃƒÂ©e pour suppression avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la commande {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les commandes par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SupplierOrder>> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<SupplierOrder> orders = supplierOrderRepository.findByStatus(status);
            logger.info("Commandes avec statut {}: {} trouvÃƒÂ©es", status, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes par statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les commandes par fournisseur
     */
    @GetMapping("/supplier/{supplierName}")
    public ResponseEntity<List<SupplierOrder>> getOrdersBySupplier(@PathVariable String supplierName) {
        try {
            List<SupplierOrder> orders = supplierOrderRepository.findBySupplierNameContainingIgnoreCase(supplierName);
            logger.info("Commandes du fournisseur {}: {} trouvÃƒÂ©es", supplierName, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes par fournisseur: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les commandes en retard
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<SupplierOrder>> getOverdueOrders() {
        try {
            List<SupplierOrder> orders = supplierOrderRepository.findOverdueOrders();
            logger.info("Commandes en retard: {} trouvÃƒÂ©es", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes en retard: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les commandes nÃƒÂ©cessitant un suivi urgent
     */
    @GetMapping("/urgent-followup")
    public ResponseEntity<List<SupplierOrder>> getOrdersRequiringUrgentFollowup() {
        try {
            LocalDate urgentDate = LocalDate.now().minusDays(7);
            List<SupplierOrder> orders = supplierOrderRepository.findOrdersRequiringUrgentFollowup(urgentDate);
            logger.info("Commandes nécessitant suivi urgent: {} trouvées", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes nÃƒÂ©cessitant suivi urgent: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les statistiques par fournisseur
     */
    @GetMapping("/supplier-stats")
    public ResponseEntity<List<Object[]>> getSupplierStats() {
        try {
            List<Object[]> stats = supplierOrderRepository.getSupplierStats();
            logger.info("Statistiques fournisseurs gÃƒÂ©nÃƒÂ©rÃƒÂ©es pour {} fournisseurs", stats.size());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la gÃƒÂ©nÃƒÂ©ration des statistiques fournisseurs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les commandes par projet
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<SupplierOrder>> getOrdersByProject(@PathVariable Long projectId) {
        try {
            List<SupplierOrder> orders = supplierOrderRepository.findByProjectId(projectId);
            logger.info("Commandes pour le projet {}: {} trouvÃƒÂ©es", projectId, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes par projet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les commandes par pÃƒÂ©riode
     */
    @GetMapping("/period")
    public ResponseEntity<List<SupplierOrder>> getOrdersByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<SupplierOrder> orders = supplierOrderRepository.findByOrderDateBetween(startDate, endDate);
            logger.info("Commandes entre {} et {}: {} trouvÃƒÂ©es", startDate, endDate, orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des commandes par pÃƒÂ©riode: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met ÃƒÂ  jour le statut d'une commande
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SupplierOrder> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Optional<SupplierOrder> orderOptional = supplierOrderRepository.findById(id);
            if (orderOptional.isPresent()) {
                SupplierOrder order = orderOptional.get();
                OrderStatus newStatus = OrderStatus.valueOf(request.get("status"));
                order.setStatus(newStatus);
                
                // Si la commande est livrÃƒÂ©e, mettre ÃƒÂ  jour la date de livraison rÃƒÂ©elle
                if (newStatus == OrderStatus.RECEIVED && order.getActualDeliveryDate() == null) {
                    order.setActualDeliveryDate(LocalDate.now());
                }
                
                SupplierOrder updatedOrder = supplierOrderRepository.save(order);
                logger.info("Statut de la commande {} mis ÃƒÂ  jour: {}", id, newStatus);
                return ResponseEntity.ok(updatedOrder);
            } else {
                logger.warn("Commande non trouvÃƒÂ©e pour mise ÃƒÂ  jour statut avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour du statut de la commande {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les statistiques globales des commandes
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Comptage total
            long totalOrders = supplierOrderRepository.count();
            stats.put("total", totalOrders);
            
            // Comptage par statut
            List<Object[]> statusCounts = supplierOrderRepository.countOrdersByStatus();
            Map<String, Long> byStatus = new HashMap<>();
            for (Object[] row : statusCounts) {
                byStatus.put(row[0].toString(), (Long) row[1]);
            }
            stats.put("byStatus", byStatus);
            
            // Commandes en retard
            long overdueOrders = supplierOrderRepository.findOverdueOrders().size();
            stats.put("overdue", overdueOrders);
            
            // Commandes nécessitant suivi urgent
            LocalDate urgentDate = LocalDate.now().minusDays(7);
            long urgentOrders = supplierOrderRepository.findOrdersRequiringUrgentFollowup(urgentDate).size();
            stats.put("urgentFollowup", urgentOrders);
            
            logger.info("Statistiques commandes gÃƒÂ©nÃƒÂ©rÃƒÂ©es: {} commandes au total", totalOrders);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la gÃƒÂ©nÃƒÂ©ration des statistiques commandes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GÃƒÂ©nÃƒÂ¨re un numÃƒÂ©ro de commande unique
     */
    private String generateOrderNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = supplierOrderRepository.count() + 1;
        return "CMD-" + year + "-" + String.format("%05d", count);
    }
}

