package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * Entité représentant un article d'une commande fournisseur
 */
@Entity
@Table(name = "supplier_order_items")
public class SupplierOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La commande fournisseur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_order_id", nullable = false)
    private SupplierOrder supplierOrder;

    @NotBlank(message = "La référence de l'article est obligatoire")
    @Column(name = "item_reference", nullable = false, length = 100)
    private String itemReference;

    @NotBlank(message = "Le nom de l'article est obligatoire")
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au moins 1")
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_received")
    private Integer quantityReceived = 0;

    @Column(length = 50)
    private String unit; // Unité (pcs, m, kg, etc.)

    @DecimalMin(value = "0.0", message = "Le prix unitaire doit être positif")
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", message = "Le montant total doit être positif")
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Informations produit
    @Column(length = 100)
    private String brand; // Marque

    @Column(length = 100)
    private String model; // Modèle

    @Column(length = 200)
    private String category; // Catégorie

    // Statut de l'article
    @Enumerated(EnumType.STRING)
    private ItemStatus status = ItemStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Énumération pour le statut de l'article
    public enum ItemStatus {
        PENDING,        // En attente
        ORDERED,        // Commandé
        SHIPPED,        // Expédié
        PARTIALLY_RECEIVED, // Partiellement reçu
        RECEIVED,       // Reçu
        CANCELLED       // Annulé
    }

    // Constructors
    public SupplierOrderItem() {}

    public SupplierOrderItem(SupplierOrder supplierOrder, String itemReference, String itemName, Integer quantity, BigDecimal unitPrice) {
        this.supplierOrder = supplierOrder;
        this.itemReference = itemReference;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Méthode pour calculer le montant total
    public void calculateTotalAmount() {
        if (quantity != null && unitPrice != null) {
            this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Méthode pour vérifier si l'article est complètement reçu
    public boolean isFullyReceived() {
        return quantityReceived != null && quantityReceived.equals(quantity);
    }

    // Méthode pour vérifier si l'article est partiellement reçu
    public boolean isPartiallyReceived() {
        return quantityReceived != null && quantityReceived > 0 && quantityReceived < quantity;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SupplierOrder getSupplierOrder() { return supplierOrder; }
    public void setSupplierOrder(SupplierOrder supplierOrder) { this.supplierOrder = supplierOrder; }

    public String getItemReference() { return itemReference; }
    public void setItemReference(String itemReference) { this.itemReference = itemReference; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
        calculateTotalAmount();
    }

    public Integer getQuantityReceived() { return quantityReceived; }
    public void setQuantityReceived(Integer quantityReceived) { this.quantityReceived = quantityReceived; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice; 
        calculateTotalAmount();
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "SupplierOrderItem{" +
                "id=" + id +
                ", itemReference='" + itemReference + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                '}';
    }
}