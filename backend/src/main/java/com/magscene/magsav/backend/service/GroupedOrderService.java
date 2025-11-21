package com.magscene.magsav.backend.service;

import com.magsav.entities.*;
import com.magscene.magsav.backend.entity.SupplierOrder;
import com.magscene.magsav.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des commandes groupées
 */
@Service
@Transactional
public class GroupedOrderService {
    
    @Autowired
    private GroupedOrderRepository groupedOrderRepository;
    
    @Autowired
    private OrderAllocationRepository orderAllocationRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private NotificationService notificationService;
    
    // *** GESTION DES COMMANDES GROUPÉES ***
    
    public List<GroupedOrder> findAll() {
        return groupedOrderRepository.findAll();
    }
    
    public List<GroupedOrder> findOpenOrders() {
        return groupedOrderRepository.findByStatus(GroupedOrderStatus.OPEN);
    }
    
    public List<GroupedOrder> findValidatedOrders() {
        return groupedOrderRepository.findByStatus(GroupedOrderStatus.VALIDATED);
    }
    
    public Optional<GroupedOrder> findById(Long id) {
        return groupedOrderRepository.findById(id);
    }
    
    public List<GroupedOrder> findOrdersBySupplier(Long supplierId) {
        return groupedOrderRepository.findBySupplierId(supplierId);
    }
    
    public List<GroupedOrder> findOpenOrdersBySupplier(Long supplierId) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        if (supplier.isPresent()) {
            return groupedOrderRepository.findOpenOrdersBySupplier(supplier.get());
        }
        return List.of();
    }
    
    public GroupedOrder save(GroupedOrder order) {
        return groupedOrderRepository.save(order);
    }
    
    // *** CRÉATION ET GESTION DES COMMANDES ***
    
    public GroupedOrder createOrGetOpenOrder(Long supplierId) {
        Optional<Supplier> supplierOpt = supplierRepository.findById(supplierId);
        if (!supplierOpt.isPresent()) {
            throw new IllegalArgumentException("Fournisseur introuvable : " + supplierId);
        }
        
        Supplier supplier = supplierOpt.get();
        
        // Chercher une commande ouverte existante
        List<GroupedOrder> openOrders = groupedOrderRepository.findOpenOrdersBySupplier(supplier);
        if (!openOrders.isEmpty()) {
            return openOrders.get(0); // Retourner la plus récente
        }
        
        // Créer une nouvelle commande groupée
        GroupedOrder newOrder = new GroupedOrder(supplier);
        return groupedOrderRepository.save(newOrder);
    }
    
    public void addAllocation(Long orderId, MaterialRequestItem item, Integer quantity) {
        Optional<GroupedOrder> orderOpt = groupedOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Commande groupée introuvable : " + orderId);
        }
        
        GroupedOrder order = orderOpt.get();
        if (!order.canBeModified()) {
            throw new IllegalStateException("Cette commande ne peut plus être modifiée");
        }
        
        // Créer l'allocation
        OrderAllocation allocation = new OrderAllocation(item.getMaterialRequest(), item, quantity);
        allocation.setGroupedOrder(order);
        
        order.addAllocation(allocation);
        orderAllocationRepository.save(allocation);
        
        // Mettre à jour l'item
        item.addAllocation(quantity);
        
        // Vérifier si le seuil est atteint
        checkAndUpdateThresholdStatus(order);
        
        groupedOrderRepository.save(order);
    }
    
    public void allocateItem(MaterialRequestItem item, Integer quantity) {
        // Déterminer le fournisseur via le catalogue
        Supplier supplier = null;
        if (item.getCatalogItem() != null && 
            item.getCatalogItem().getCatalog() != null) {
            supplier = item.getCatalogItem().getCatalog().getSupplier();
        } else {
            // Pour les items libres, utiliser un fournisseur par défaut ou laisser l'admin choisir
            // TODO: Implémenter la logique de sélection de fournisseur
            throw new IllegalStateException("Impossible de déterminer le fournisseur pour l'item : " + item.getId());
        }
        
        GroupedOrder order = createOrGetOpenOrder(supplier.getId());
        addAllocation(order.getId(), item, quantity);
    }
    
    // *** VALIDATION ET TRANSFORMATION ***
    
    public void validateOrder(Long orderId, String validator) {
        Optional<GroupedOrder> orderOpt = groupedOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Commande groupée introuvable : " + orderId);
        }
        
        GroupedOrder order = orderOpt.get();
        if (!order.canBeValidated()) {
            throw new IllegalStateException("Cette commande ne peut pas être validée");
        }
        
        order.validate(validator);
        groupedOrderRepository.save(order);
        
        // Notification
        notificationService.sendOrderValidationNotification(order);
    }
    
    public SupplierOrder convertToSupplierOrder(Long groupedOrderId) {
        Optional<GroupedOrder> orderOpt = groupedOrderRepository.findById(groupedOrderId);
        if (!orderOpt.isPresent()) {
            throw new IllegalArgumentException("Commande groupée introuvable : " + groupedOrderId);
        }
        
        GroupedOrder groupedOrder = orderOpt.get();
        if (!groupedOrder.isValidated()) {
            throw new IllegalStateException("Seules les commandes validées peuvent être transformées");
        }
        
        // TODO: Créer la SupplierOrder
        // Pour l'instant, simuler la création
        groupedOrder.setStatus(GroupedOrderStatus.ORDERED);
        groupedOrderRepository.save(groupedOrder);
        
        // Notification
        notificationService.sendOrderCreationNotification(groupedOrder);
        
        return null; // TODO: Retourner la vraie SupplierOrder
    }
    
    // *** GESTION DES SEUILS ***
    
    public void checkAndUpdateThresholdStatus(GroupedOrder order) {
        if (supplierService.checkThresholdReached(order)) {
            if (order.getStatus() == GroupedOrderStatus.OPEN) {
                order.setStatus(GroupedOrderStatus.THRESHOLD_REACHED);
                
                if (!order.isThresholdAlertSent()) {
                    sendThresholdAlert(order);
                    order.setThresholdAlertSent(true);
                }
                
                // Auto-validation si configurée
                if (order.isAutoValidateOnThreshold()) {
                    order.validate("SYSTEM_AUTO");
                }
                
                groupedOrderRepository.save(order);
            }
        }
    }
    
    public void sendThresholdAlert(GroupedOrder order) {
        notificationService.sendThresholdAlert(order);
    }
    
    public void checkAndSendThresholdAlerts() {
        List<GroupedOrder> ordersReachingThreshold = groupedOrderRepository.findOrdersReachingThresholdWithoutAlert();
        
        for (GroupedOrder order : ordersReachingThreshold) {
            checkAndUpdateThresholdStatus(order);
        }
    }
    
    // *** RECHERCHE ET FILTRES ***
    
    public List<GroupedOrder> searchOrders(String searchTerm) {
        return groupedOrderRepository.searchOrders(searchTerm);
    }
    
    public List<GroupedOrder> findRecentOrders(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return groupedOrderRepository.findRecentOrders(since);
    }
    
    public List<GroupedOrder> findOrdersNeedingAttention() {
        LocalDateTime oldDate = LocalDateTime.now().minusDays(7);
        LocalDateTime oldValidationDate = LocalDateTime.now().minusDays(3);
        return groupedOrderRepository.findOrdersNeedingAttention(oldDate, oldValidationDate);
    }
    
    public List<GroupedOrder> findAutoValidatableOrders() {
        return groupedOrderRepository.findAutoValidatableOrders();
    }
    
    // *** GESTION DES ALLOCATIONS ***
    
    public List<OrderAllocation> findAllocationsByOrder(Long orderId) {
        Optional<GroupedOrder> order = groupedOrderRepository.findById(orderId);
        if (order.isPresent()) {
            return orderAllocationRepository.findByGroupedOrder(order.get());
        }
        return List.of();
    }
    
    public void removeAllocation(Long allocationId) {
        Optional<OrderAllocation> allocationOpt = orderAllocationRepository.findById(allocationId);
        if (!allocationOpt.isPresent()) {
            return;
        }
        
        OrderAllocation allocation = allocationOpt.get();
        GroupedOrder order = allocation.getGroupedOrder();
        
        if (order != null && !order.canBeModified()) {
            throw new IllegalStateException("Cette commande ne peut plus être modifiée");
        }
        
        // Remettre à jour l'item
        if (allocation.getRequestItem() != null) {
            allocation.getRequestItem().setQuantityAllocated(
                allocation.getRequestItem().getQuantityAllocated() - allocation.getAllocatedQuantity()
            );
        }
        
        // Retirer de la commande groupée
        if (order != null) {
            order.removeAllocation(allocation);
            order.recalculateAmount();
            groupedOrderRepository.save(order);
        }
        
        orderAllocationRepository.delete(allocation);
    }
    
    public void updateAllocationQuantity(Long allocationId, Integer newQuantity) {
        Optional<OrderAllocation> allocationOpt = orderAllocationRepository.findById(allocationId);
        if (!allocationOpt.isPresent()) {
            return;
        }
        
        OrderAllocation allocation = allocationOpt.get();
        GroupedOrder order = allocation.getGroupedOrder();
        
        if (order != null && !order.canBeModified()) {
            throw new IllegalStateException("Cette commande ne peut plus être modifiée");
        }
        
        Integer oldQuantity = allocation.getAllocatedQuantity();
        allocation.setAllocatedQuantity(newQuantity);
        
        // Mettre à jour l'item
        if (allocation.getRequestItem() != null) {
            Integer currentAllocated = allocation.getRequestItem().getQuantityAllocated();
            allocation.getRequestItem().setQuantityAllocated(currentAllocated - oldQuantity + newQuantity);
        }
        
        orderAllocationRepository.save(allocation);
        
        // Recalculer le montant de la commande
        if (order != null) {
            order.recalculateAmount();
            checkAndUpdateThresholdStatus(order);
            groupedOrderRepository.save(order);
        }
    }
    
    // *** STATISTIQUES ***
    
    public List<Object[]> getOrdersBySupplierStats() {
        return groupedOrderRepository.countOrdersBySupplier();
    }
    
    public List<Object[]> getOrdersByStatusStats() {
        return groupedOrderRepository.countOrdersByStatus();
    }
    
    public List<Object[]> getAmountsBySupplierStats() {
        return groupedOrderRepository.sumAmountsBySupplier();
    }
    
    public Double getAverageValidationTimeInHours() {
        return groupedOrderRepository.averageValidationTimeInHours();
    }
    
    public BigDecimal getTotalOrdersAmount() {
        return groupedOrderRepository.sumTotalAmount();
    }
    
    public List<Object[]> getOrdersByDayStats(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return groupedOrderRepository.countOrdersByDay(since);
    }
    
    // *** MÉTHODES UTILITAIRES ***
    
    public void cancelOrder(Long orderId, String reason) {
        Optional<GroupedOrder> orderOpt = groupedOrderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            return;
        }
        
        GroupedOrder order = orderOpt.get();
        if (order.isOrdered()) {
            throw new IllegalStateException("Impossible d'annuler une commande déjà passée");
        }
        
        order.setStatus(GroupedOrderStatus.CANCELLED);
        order.setNotes((order.getNotes() != null ? order.getNotes() + "\n" : "") + "Annulée: " + reason);
        
        // Libérer toutes les allocations
        for (OrderAllocation allocation : order.getAllocations()) {
            if (allocation.getRequestItem() != null) {
                allocation.getRequestItem().setQuantityAllocated(
                    allocation.getRequestItem().getQuantityAllocated() - allocation.getAllocatedQuantity()
                );
            }
            allocation.cancel(reason);
        }
        
        groupedOrderRepository.save(order);
    }
    
    public boolean hasOrdersNeedingValidation() {
        return !groupedOrderRepository.findByStatus(GroupedOrderStatus.THRESHOLD_REACHED).isEmpty() ||
               !groupedOrderRepository.findByStatus(GroupedOrderStatus.VALIDATED).isEmpty();
    }
}