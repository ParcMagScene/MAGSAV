package com.magscene.magsav.backend.controller;

import com.magsav.entities.*;
import com.magsav.enums.RequestUrgency;
import com.magscene.magsav.backend.entity.SupplierOrder;
import com.magscene.magsav.backend.service.GroupedOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des commandes groupées
 */
@RestController
@RequestMapping("/api/grouped-orders")
@CrossOrigin(origins = "*")
public class GroupedOrderController {
    
    @Autowired
    private GroupedOrderService groupedOrderService;
    
    // *** CRUD COMMANDES GROUPÉES ***
    
    @GetMapping
    public ResponseEntity<List<GroupedOrder>> getAllGroupedOrders() {
        List<GroupedOrder> orders = groupedOrderService.findAll();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/open")
    public ResponseEntity<List<GroupedOrder>> getOpenOrders() {
        List<GroupedOrder> orders = groupedOrderService.findOpenOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/validated")
    public ResponseEntity<List<GroupedOrder>> getValidatedOrders() {
        List<GroupedOrder> orders = groupedOrderService.findValidatedOrders();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<GroupedOrder> getGroupedOrderById(@PathVariable Long id) {
        Optional<GroupedOrder> order = groupedOrderService.findById(id);
        return order.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<GroupedOrder> createGroupedOrder(@Valid @RequestBody CreateGroupedOrderDTO orderDTO) {
        GroupedOrder order = groupedOrderService.createOrGetOpenOrder(orderDTO.supplierId);
        
        if (orderDTO.deliveryAddress != null) {
            order.setDeliveryAddress(orderDTO.deliveryAddress);
        }
        if (orderDTO.urgency != null) {
            order.setUrgency(orderDTO.urgency);
        }
        if (orderDTO.notes != null) {
            order.setNotes(orderDTO.notes);
        }
        
        GroupedOrder savedOrder = groupedOrderService.save(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
    }
    
    // *** RECHERCHE COMMANDES GROUPÉES ***
    
    @GetMapping("/search")
    public ResponseEntity<List<GroupedOrder>> searchOrders(@RequestParam String q) {
        List<GroupedOrder> orders = groupedOrderService.searchOrders(q);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<GroupedOrder>> getOrdersBySupplier(@PathVariable Long supplierId) {
        List<GroupedOrder> orders = groupedOrderService.findOrdersBySupplier(supplierId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/supplier/{supplierId}/open")
    public ResponseEntity<List<GroupedOrder>> getOpenOrdersBySupplier(@PathVariable Long supplierId) {
        List<GroupedOrder> orders = groupedOrderService.findOpenOrdersBySupplier(supplierId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<GroupedOrder>> getRecentOrders(@RequestParam(defaultValue = "30") int days) {
        List<GroupedOrder> orders = groupedOrderService.findRecentOrders(days);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/needing-attention")
    public ResponseEntity<List<GroupedOrder>> getOrdersNeedingAttention() {
        List<GroupedOrder> orders = groupedOrderService.findOrdersNeedingAttention();
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/auto-validatable")
    public ResponseEntity<List<GroupedOrder>> getAutoValidatableOrders() {
        List<GroupedOrder> orders = groupedOrderService.findAutoValidatableOrders();
        return ResponseEntity.ok(orders);
    }
    
    // *** WORKFLOW DE VALIDATION ***
    
    @PutMapping("/{id}/validate")
    public ResponseEntity<Void> validateOrder(@PathVariable Long id,
                                             @RequestBody ValidateOrderDTO validateDTO) {
        try {
            groupedOrderService.validateOrder(id, validateDTO.validator);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/convert-to-supplier-order")
    public ResponseEntity<SupplierOrder> convertToSupplierOrder(@PathVariable Long id) {
        try {
            SupplierOrder supplierOrder = groupedOrderService.convertToSupplierOrder(id);
            return ResponseEntity.ok(supplierOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id,
                                           @RequestBody CancelOrderDTO cancelDTO) {
        try {
            groupedOrderService.cancelOrder(id, cancelDTO.reason);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // *** GESTION DES ALLOCATIONS ***
    
    @GetMapping("/{id}/allocations")
    public ResponseEntity<List<OrderAllocation>> getOrderAllocations(@PathVariable Long id) {
        List<OrderAllocation> allocations = groupedOrderService.findAllocationsByOrder(id);
        return ResponseEntity.ok(allocations);
    }
    
    @PostMapping("/{id}/allocations")
    public ResponseEntity<Void> addAllocation(@PathVariable Long id,
                                             @RequestBody AddAllocationDTO allocationDTO) {
        try {
            // TODO: Récupérer le MaterialRequestItem via un repository
            // Pour l'instant, retourner une erreur non implémentée
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/allocations/{allocationId}")
    public ResponseEntity<Void> removeAllocation(@PathVariable Long allocationId) {
        try {
            groupedOrderService.removeAllocation(allocationId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/allocations/{allocationId}/quantity")
    public ResponseEntity<Void> updateAllocationQuantity(@PathVariable Long allocationId,
                                                         @RequestBody UpdateQuantityDTO quantityDTO) {
        try {
            groupedOrderService.updateAllocationQuantity(allocationId, quantityDTO.quantity);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // *** GESTION DES SEUILS ***
    
    @PostMapping("/check-thresholds")
    public ResponseEntity<Void> checkAndSendThresholdAlerts() {
        groupedOrderService.checkAndSendThresholdAlerts();
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/send-threshold-alert")
    public ResponseEntity<Void> sendThresholdAlert(@PathVariable Long id) {
        Optional<GroupedOrder> order = groupedOrderService.findById(id);
        if (order.isPresent()) {
            groupedOrderService.sendThresholdAlert(order.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // *** STATISTIQUES ***
    
    @GetMapping("/stats/by-supplier")
    public ResponseEntity<List<Object[]>> getOrdersBySupplierStats() {
        List<Object[]> stats = groupedOrderService.getOrdersBySupplierStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/by-status")
    public ResponseEntity<List<Object[]>> getOrdersByStatusStats() {
        List<Object[]> stats = groupedOrderService.getOrdersByStatusStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/amounts-by-supplier")
    public ResponseEntity<List<Object[]>> getAmountsBySupplierStats() {
        List<Object[]> stats = groupedOrderService.getAmountsBySupplierStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/stats/validation-time")
    public ResponseEntity<Double> getAverageValidationTime() {
        Double avgTime = groupedOrderService.getAverageValidationTimeInHours();
        return ResponseEntity.ok(avgTime);
    }
    
    @GetMapping("/stats/total-amount")
    public ResponseEntity<BigDecimal> getTotalOrdersAmount() {
        BigDecimal totalAmount = groupedOrderService.getTotalOrdersAmount();
        return ResponseEntity.ok(totalAmount);
    }
    
    @GetMapping("/stats/by-day")
    public ResponseEntity<List<Object[]>> getOrdersByDayStats(@RequestParam(defaultValue = "30") int days) {
        List<Object[]> stats = groupedOrderService.getOrdersByDayStats(days);
        return ResponseEntity.ok(stats);
    }
    
    // *** UTILITAIRES ***
    
    @GetMapping("/has-orders-needing-validation")
    public ResponseEntity<Boolean> hasOrdersNeedingValidation() {
        boolean hasOrders = groupedOrderService.hasOrdersNeedingValidation();
        return ResponseEntity.ok(hasOrders);
    }
    
    // *** CLASSES DTO ***
    
    public static class CreateGroupedOrderDTO {
        public Long supplierId;
        public String deliveryAddress;
        public RequestUrgency urgency;
        public String notes;
    }
    
    public static class ValidateOrderDTO {
        public String validator;
        public String notes;
    }
    
    public static class CancelOrderDTO {
        public String reason;
    }
    
    public static class AddAllocationDTO {
        public Long materialRequestItemId;
        public Integer quantity;
    }
    
    public static class UpdateQuantityDTO {
        public Integer quantity;
    }
}