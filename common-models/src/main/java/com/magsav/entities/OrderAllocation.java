package com.magsav.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant l'allocation d'un item de demande vers une commande groupée
 */
@Entity
@Table(name = "order_allocations", indexes = {
    @Index(name = "idx_allocation_request", columnList = "material_request_id"),
    @Index(name = "idx_allocation_grouped_order", columnList = "grouped_order_id"),
    @Index(name = "idx_allocation_supplier_order", columnList = "supplier_order_id")
})
public class OrderAllocation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Référence vers la demande d'origine
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", nullable = false)
    private MaterialRequest materialRequest;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_item_id", nullable = false)
    private MaterialRequestItem requestItem;
    
    // Allocation vers commande groupée
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grouped_order_id")
    private GroupedOrder groupedOrder;
    
    // Allocation vers commande finale (après validation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_order_id")
    private SupplierProcurementOrder supplierOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_order_item_id")
    private ProcurementItem orderItem;
    
    // Quantités
    @Column(name = "allocated_quantity", nullable = false)
    private Integer allocatedQuantity;
    
    @Column(name = "delivered_quantity")
    private Integer deliveredQuantity = 0;
    
    // Prix au moment de l'allocation
    @Column(name = "estimated_price", precision = 12, scale = 2)
    private BigDecimal estimatedPrice;
    
    @Column(name = "actual_price", precision = 12, scale = 2)
    private BigDecimal actualPrice;
    
    @Column(name = "currency", length = 3)
    private String currency = "EUR";
    
    // Statut de l'allocation
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private AllocationStatus status = AllocationStatus.ALLOCATED;
    
    // Notes et commentaires
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "delivery_notes", columnDefinition = "TEXT")
    private String deliveryNotes;
    
    // Métadonnées
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
    
    // Constructeurs
    public OrderAllocation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public OrderAllocation(MaterialRequest materialRequest, MaterialRequestItem requestItem, 
                          Integer allocatedQuantity) {
        this();
        this.materialRequest = materialRequest;
        this.requestItem = requestItem;
        this.allocatedQuantity = allocatedQuantity;
        this.estimatedPrice = requestItem.getEstimatedPrice();
        this.currency = requestItem.getCurrency();
    }
    
    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public MaterialRequest getMaterialRequest() { return materialRequest; }
    public void setMaterialRequest(MaterialRequest materialRequest) { this.materialRequest = materialRequest; }
    
    public MaterialRequestItem getRequestItem() { return requestItem; }
    public void setRequestItem(MaterialRequestItem requestItem) { this.requestItem = requestItem; }
    
    public GroupedOrder getGroupedOrder() { return groupedOrder; }
    public void setGroupedOrder(GroupedOrder groupedOrder) { this.groupedOrder = groupedOrder; }
    
    public SupplierProcurementOrder getSupplierOrder() { return supplierOrder; }
    public void setSupplierOrder(SupplierProcurementOrder supplierOrder) { this.supplierOrder = supplierOrder; }
    
    public ProcurementItem getOrderItem() { return orderItem; }
    public void setOrderItem(ProcurementItem orderItem) { this.orderItem = orderItem; }
    
    public Integer getAllocatedQuantity() { return allocatedQuantity; }
    public void setAllocatedQuantity(Integer allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }
    
    public Integer getDeliveredQuantity() { return deliveredQuantity; }
    public void setDeliveredQuantity(Integer deliveredQuantity) { this.deliveredQuantity = deliveredQuantity; }
    
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    
    public BigDecimal getActualPrice() { return actualPrice; }
    public void setActualPrice(BigDecimal actualPrice) { this.actualPrice = actualPrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public AllocationStatus getStatus() { return status; }
    public void setStatus(AllocationStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getDeliveryNotes() { return deliveryNotes; }
    public void setDeliveryNotes(String deliveryNotes) { this.deliveryNotes = deliveryNotes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    
    // Méthodes utilitaires
    public BigDecimal getTotalEstimatedAmount() {
        if (estimatedPrice == null) return null;
        return estimatedPrice.multiply(BigDecimal.valueOf(allocatedQuantity));
    }
    
    public BigDecimal getTotalActualAmount() {
        if (actualPrice == null) return null;
        return actualPrice.multiply(BigDecimal.valueOf(allocatedQuantity));
    }
    
    public Integer getPendingDeliveryQuantity() {
        return allocatedQuantity - deliveredQuantity;
    }
    
    public boolean isFullyDelivered() {
        return deliveredQuantity >= allocatedQuantity;
    }
    
    public boolean isPartiallyDelivered() {
        return deliveredQuantity > 0 && deliveredQuantity < allocatedQuantity;
    }
    
    public void addDelivery(Integer quantity) {
        this.deliveredQuantity = (deliveredQuantity != null ? deliveredQuantity : 0) + quantity;
        if (isFullyDelivered()) {
            this.status = AllocationStatus.DELIVERED;
            this.deliveredAt = LocalDateTime.now();
        } else if (isPartiallyDelivered()) {
            this.status = AllocationStatus.PARTIALLY_DELIVERED;
        }
    }
    
    public void cancel(String reason) {
        this.status = AllocationStatus.CANCELLED;
        this.notes = (notes != null ? notes + "\n" : "") + "Annulé: " + reason;
    }
    
    public String getItemReference() {
        return requestItem != null ? requestItem.getDisplayReference() : "N/A";
    }
    
    public String getItemName() {
        return requestItem != null ? requestItem.getDisplayName() : "N/A";
    }
    
    public String getRequesterName() {
        return materialRequest != null ? materialRequest.getRequesterName() : "N/A";
    }
    
    public String getSupplierName() {
        if (groupedOrder != null && groupedOrder.getSupplier() != null) {
            return groupedOrder.getSupplier().getName();
        }
        if (supplierOrder != null && supplierOrder.getSupplier() != null) {
            return supplierOrder.getSupplier().getName();
        }
        return "N/A";
    }
    
    @Override
    public String toString() {
        return getItemReference() + " x" + allocatedQuantity + " (" + getRequesterName() + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderAllocation)) return false;
        OrderAllocation that = (OrderAllocation) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
