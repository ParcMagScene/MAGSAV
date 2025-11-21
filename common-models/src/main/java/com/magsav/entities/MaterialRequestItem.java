package com.magsav.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un article demandé dans une MaterialRequest
 */
@Entity
@Table(name = "material_request_items")
public class MaterialRequestItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_request_id", nullable = false)
    private MaterialRequest materialRequest;
    
    // Référence catalogue (optionnelle)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id")
    private CatalogItem catalogItem;
    
    // Référence libre (si pas dans catalogue)
    @Column(name = "free_reference", length = 100)
    private String freeReference;
    
    @Column(name = "free_name", length = 300)
    private String freeName;
    
    @Column(name = "free_description", columnDefinition = "TEXT")
    private String freeDescription;
    
    @Column(name = "free_brand", length = 100)
    private String freeBrand;
    
    @Column(name = "free_model", length = 100)
    private String freeModel;
    
    // Quantité et unité
    @Column(name = "requested_quantity", nullable = false)
    private Integer requestedQuantity = 1;
    
    @Column(length = 20)
    private String unit;
    
    // Prix estimé
    @Column(name = "estimated_price", precision = 12, scale = 2)
    private BigDecimal estimatedPrice;
    
    @Column(name = "currency", length = 3)
    private String currency = "EUR";
    
    // Suivi des allocations
    @Column(name = "quantity_allocated")
    private Integer quantityAllocated = 0;
    
    @Column(name = "quantity_delivered")
    private Integer quantityDelivered = 0;
    
    // Spécifications supplémentaires
    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;
    
    @Column(name = "alternatives_accepted")
    private boolean alternativesAccepted = false;
    
    @Column(name = "priority")
    private Integer priority = 1; // 1 = plus important
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @OneToMany(mappedBy = "requestItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderAllocation> allocations = new HashSet<>();
    
    // Constructeurs
    public MaterialRequestItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public MaterialRequestItem(MaterialRequest materialRequest, Integer quantity) {
        this();
        this.materialRequest = materialRequest;
        this.requestedQuantity = quantity;
    }
    
    // Constructeur avec CatalogItem
    public MaterialRequestItem(MaterialRequest materialRequest, CatalogItem catalogItem, Integer quantity) {
        this(materialRequest, quantity);
        this.catalogItem = catalogItem;
        if (catalogItem != null) {
            this.unit = catalogItem.getUnit();
            this.estimatedPrice = catalogItem.getUnitPrice();
            this.currency = catalogItem.getCurrency();
        }
    }
    
    // Constructeur avec référence libre
    public MaterialRequestItem(MaterialRequest materialRequest, String freeReference, String freeName, 
                              Integer quantity, BigDecimal estimatedPrice) {
        this(materialRequest, quantity);
        this.freeReference = freeReference;
        this.freeName = freeName;
        this.estimatedPrice = estimatedPrice;
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
    
    public CatalogItem getCatalogItem() { return catalogItem; }
    public void setCatalogItem(CatalogItem catalogItem) { this.catalogItem = catalogItem; }
    
    public String getFreeReference() { return freeReference; }
    public void setFreeReference(String freeReference) { this.freeReference = freeReference; }
    
    public String getFreeName() { return freeName; }
    public void setFreeName(String freeName) { this.freeName = freeName; }
    
    public String getFreeDescription() { return freeDescription; }
    public void setFreeDescription(String freeDescription) { this.freeDescription = freeDescription; }
    
    public String getFreeBrand() { return freeBrand; }
    public void setFreeBrand(String freeBrand) { this.freeBrand = freeBrand; }
    
    public String getFreeModel() { return freeModel; }
    public void setFreeModel(String freeModel) { this.freeModel = freeModel; }
    
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer requestedQuantity) { this.requestedQuantity = requestedQuantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public BigDecimal getEstimatedPrice() { return estimatedPrice; }
    public void setEstimatedPrice(BigDecimal estimatedPrice) { this.estimatedPrice = estimatedPrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public Integer getQuantityAllocated() { return quantityAllocated; }
    public void setQuantityAllocated(Integer quantityAllocated) { this.quantityAllocated = quantityAllocated; }
    
    public Integer getQuantityDelivered() { return quantityDelivered; }
    public void setQuantityDelivered(Integer quantityDelivered) { this.quantityDelivered = quantityDelivered; }
    
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    
    public boolean isAlternativesAccepted() { return alternativesAccepted; }
    public void setAlternativesAccepted(boolean alternativesAccepted) { 
        this.alternativesAccepted = alternativesAccepted; 
    }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<OrderAllocation> getAllocations() { return allocations; }
    public void setAllocations(Set<OrderAllocation> allocations) { this.allocations = allocations; }
    
    // Méthodes utilitaires
    public boolean isFromCatalog() {
        return catalogItem != null;
    }
    
    public boolean isFreeReference() {
        return catalogItem == null;
    }
    
    public String getDisplayReference() {
        return isFromCatalog() ? catalogItem.getReference() : freeReference;
    }
    
    public String getDisplayName() {
        return isFromCatalog() ? catalogItem.getName() : freeName;
    }
    
    public String getDisplayBrand() {
        return isFromCatalog() ? catalogItem.getBrand() : freeBrand;
    }
    
    public String getDisplayModel() {
        return isFromCatalog() ? catalogItem.getModel() : freeModel;
    }
    
    public String getDisplayDescription() {
        return isFromCatalog() ? catalogItem.getDescription() : freeDescription;
    }
    
    public BigDecimal getTotalEstimatedPrice() {
        if (estimatedPrice == null) return null;
        return estimatedPrice.multiply(BigDecimal.valueOf(requestedQuantity));
    }
    
    public Integer getQuantityPending() {
        return requestedQuantity - quantityAllocated;
    }
    
    public Integer getQuantityNotDelivered() {
        return quantityAllocated - quantityDelivered;
    }
    
    public boolean isFullyAllocated() {
        return quantityAllocated >= requestedQuantity;
    }
    
    public boolean isFullyDelivered() {
        return quantityDelivered >= requestedQuantity;
    }
    
    public boolean isPartiallyDelivered() {
        return quantityDelivered > 0 && quantityDelivered < requestedQuantity;
    }
    
    public void addAllocation(Integer quantity) {
        this.quantityAllocated = (quantityAllocated != null ? quantityAllocated : 0) + quantity;
    }
    
    public void addDelivery(Integer quantity) {
        this.quantityDelivered = (quantityDelivered != null ? quantityDelivered : 0) + quantity;
    }
    
    public String getSupplierName() {
        if (isFromCatalog() && catalogItem.getCatalog() != null && 
            catalogItem.getCatalog().getSupplier() != null) {
            return catalogItem.getCatalog().getSupplier().getName();
        }
        return "N/A";
    }
    
    @Override
    public String toString() {
        return getDisplayReference() + " - " + getDisplayName() + " (x" + requestedQuantity + ")";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MaterialRequestItem)) return false;
        MaterialRequestItem that = (MaterialRequestItem) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}