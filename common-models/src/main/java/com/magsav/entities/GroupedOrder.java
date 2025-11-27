package com.magsav.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.magsav.enums.RequestUrgency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Entité représentant une commande groupée avant transformation en
 * SupplierProcurementOrder
 */
@Entity
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "handler" }, ignoreUnknown = true)
@Table(name = "grouped_orders", indexes = {
        @Index(name = "idx_grouped_order_number", columnList = "order_number"),
        @Index(name = "idx_grouped_supplier", columnList = "supplier_id"),
        @Index(name = "idx_grouped_status", columnList = "status")
})
public class GroupedOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, length = 50)
    private String orderNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GroupedOrderStatus status = GroupedOrderStatus.OPEN;

    // Seuils et alertes
    @Column(name = "current_amount", precision = 12, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "threshold_alert_sent")
    private boolean thresholdAlertSent = false;

    @Column(name = "auto_validate_on_threshold")
    private boolean autoValidateOnThreshold = false;

    // Validation
    @Column(name = "validated_by", length = 100)
    private String validatedBy;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "validation_notes", columnDefinition = "TEXT")
    private String validationNotes;

    // Commande effective résultante
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_order_id")
    private SupplierProcurementOrder supplierOrder;

    // Informations de livraison
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "urgency")
    @Enumerated(EnumType.STRING)
    private RequestUrgency urgency = RequestUrgency.NORMAL;

    // Métadonnées
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relations
    @JsonIgnore
    @OneToMany(mappedBy = "groupedOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderAllocation> allocations = new HashSet<>();

    // Constructeurs
    public GroupedOrder() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        generateOrderNumber();
    }

    public GroupedOrder(Supplier supplier) {
        this();
        this.supplier = supplier;
    }

    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public GroupedOrderStatus getStatus() {
        return status;
    }

    public void setStatus(GroupedOrderStatus status) {
        this.status = status;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public boolean isThresholdAlertSent() {
        return thresholdAlertSent;
    }

    public void setThresholdAlertSent(boolean thresholdAlertSent) {
        this.thresholdAlertSent = thresholdAlertSent;
    }

    public boolean isAutoValidateOnThreshold() {
        return autoValidateOnThreshold;
    }

    public void setAutoValidateOnThreshold(boolean autoValidateOnThreshold) {
        this.autoValidateOnThreshold = autoValidateOnThreshold;
    }

    public String getValidatedBy() {
        return validatedBy;
    }

    public void setValidatedBy(String validatedBy) {
        this.validatedBy = validatedBy;
    }

    public LocalDateTime getValidatedAt() {
        return validatedAt;
    }

    public void setValidatedAt(LocalDateTime validatedAt) {
        this.validatedAt = validatedAt;
    }

    public String getValidationNotes() {
        return validationNotes;
    }

    public void setValidationNotes(String validationNotes) {
        this.validationNotes = validationNotes;
    }

    public SupplierProcurementOrder getSupplierOrder() {
        return supplierOrder;
    }

    public void setSupplierOrder(SupplierProcurementOrder supplierOrder) {
        this.supplierOrder = supplierOrder;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RequestUrgency urgency) {
        this.urgency = urgency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<OrderAllocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(Set<OrderAllocation> allocations) {
        this.allocations = allocations;
    }

    // Méthodes utilitaires
    private void generateOrderNumber() {
        this.orderNumber = "GRP-" + System.currentTimeMillis();
    }

    public void addAllocation(OrderAllocation allocation) {
        allocations.add(allocation);
        allocation.setGroupedOrder(this);
        recalculateAmount();
    }

    public void removeAllocation(OrderAllocation allocation) {
        allocations.remove(allocation);
        allocation.setGroupedOrder(null);
        recalculateAmount();
    }

    public void recalculateAmount() {
        this.currentAmount = allocations.stream()
                .filter(allocation -> allocation.getEstimatedPrice() != null)
                .map(allocation -> allocation.getEstimatedPrice()
                        .multiply(BigDecimal.valueOf(allocation.getAllocatedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isThresholdReached() {
        if (supplier == null || !supplier.hasThresholdConfigured()) {
            return false;
        }
        return currentAmount.compareTo(supplier.getFreeShippingThreshold()) >= 0;
    }

    public BigDecimal getRemainingToThreshold() {
        if (supplier == null || !supplier.hasThresholdConfigured()) {
            return null;
        }
        BigDecimal remaining = supplier.getFreeShippingThreshold().subtract(currentAmount);
        return remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO;
    }

    public void validate(String validator) {
        this.status = GroupedOrderStatus.VALIDATED;
        this.validatedBy = validator;
        this.validatedAt = LocalDateTime.now();
    }

    public void markAsOrdered(SupplierProcurementOrder order) {
        this.status = GroupedOrderStatus.ORDERED;
        this.supplierOrder = order;
    }

    public boolean canBeModified() {
        return status == GroupedOrderStatus.OPEN || status == GroupedOrderStatus.THRESHOLD_REACHED;
    }

    public boolean canBeValidated() {
        return (status == GroupedOrderStatus.OPEN || status == GroupedOrderStatus.THRESHOLD_REACHED)
                && !allocations.isEmpty();
    }

    public boolean isOpen() {
        return status == GroupedOrderStatus.OPEN;
    }

    public boolean isValidated() {
        return status == GroupedOrderStatus.VALIDATED;
    }

    public boolean isOrdered() {
        return status == GroupedOrderStatus.ORDERED;
    }

    public int getTotalItemsCount() {
        return allocations.stream().mapToInt(OrderAllocation::getAllocatedQuantity).sum();
    }

    public int getUniqueRequestsCount() {
        return (int) allocations.stream()
                .map(OrderAllocation::getMaterialRequest)
                .distinct()
                .count();
    }

    public String getDisplayName() {
        return orderNumber + " - " + (supplier != null ? supplier.getName() : "N/A");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GroupedOrder))
            return false;
        GroupedOrder that = (GroupedOrder) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
