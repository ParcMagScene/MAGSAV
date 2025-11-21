package com.magsav.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un article dans une commande fournisseur
 */
@Entity
@Table(name = "procurement_items")
public class ProcurementItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_order_id", nullable = false)
    private SupplierProcurementOrder supplierOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_item_id")
    private CatalogItem catalogItem;
    
    @Column(name = "item_name", nullable = false)
    private String itemName;
    
    @Column(name = "item_description")
    private String itemDescription;
    
    @Column(name = "item_reference")
    private String itemReference;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;
    
    @Column(name = "line_total", precision = 10, scale = 2)
    private BigDecimal lineTotal;
    
    @Column(name = "received_quantity")
    private Integer receivedQuantity;
    
    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Constructeurs
    public ProcurementItem() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.receivedQuantity = 0;
    }
    
    public ProcurementItem(String itemName, Integer quantity, BigDecimal unitPrice) {
        this();
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public SupplierProcurementOrder getSupplierOrder() {
        return supplierOrder;
    }

    public void setSupplierOrder(SupplierProcurementOrder supplierOrder) {
        this.supplierOrder = supplierOrder;
    }
    
    public CatalogItem getCatalogItem() {
        return catalogItem;
    }
    
    public void setCatalogItem(CatalogItem catalogItem) {
        this.catalogItem = catalogItem;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public String getItemDescription() {
        return itemDescription;
    }
    
    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }
    
    public String getItemReference() {
        return itemReference;
    }
    
    public void setItemReference(String itemReference) {
        this.itemReference = itemReference;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateLineTotal();
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateLineTotal();
    }
    
    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }
    
    public void setDiscountPercent(BigDecimal discountPercent) {
        this.discountPercent = discountPercent;
        calculateLineTotal();
    }
    
    public BigDecimal getLineTotal() {
        return lineTotal;
    }
    
    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }
    
    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }
    
    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }
    
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }
    
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    // Méthodes utilitaires
    
    /**
     * Calcule le total de la ligne en tenant compte de la remise
     */
    public void calculateLineTotal() {
        if (quantity != null && unitPrice != null) {
            BigDecimal baseTotal = unitPrice.multiply(new BigDecimal(quantity));
            
            if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountAmount = baseTotal.multiply(discountPercent).divide(new BigDecimal("100"));
                this.lineTotal = baseTotal.subtract(discountAmount);
            } else {
                this.lineTotal = baseTotal;
            }
        }
    }
    
    /**
     * Vérifie si l'article est complètement livré
     */
    public boolean isFullyDelivered() {
        return receivedQuantity != null && receivedQuantity.equals(quantity);
    }
    
    /**
     * Vérifie si l'article est partiellement livré
     */
    public boolean isPartiallyDelivered() {
        return receivedQuantity != null && receivedQuantity > 0 && receivedQuantity < quantity;
    }
    
    /**
     * Calcule la quantité manquante
     */
    public Integer getRemainingQuantity() {
        if (receivedQuantity == null) {
            return quantity;
        }
        return Math.max(0, quantity - receivedQuantity);
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "ProcurementItem{" +
                "id=" + id +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", lineTotal=" + lineTotal +
                ", receivedQuantity=" + receivedQuantity +
                '}';
    }
}

