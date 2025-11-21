package com.magsav.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un article dans un catalogue fournisseur
 */
@Entity
@Table(name = "catalog_items", indexes = {
    @Index(name = "idx_catalog_reference", columnList = "reference"),
    @Index(name = "idx_catalog_name", columnList = "name"),
    @Index(name = "idx_catalog_category", columnList = "category"),
    @Index(name = "idx_catalog_brand", columnList = "brand")
})
public class CatalogItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "catalog_id", nullable = false)
    private SupplierCatalog catalog;
    
    @Column(nullable = false, length = 100)
    private String reference;
    
    @Column(nullable = false, length = 300)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(length = 100)
    private String category;
    
    @Column(length = 100)
    private String subcategory;
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 100)
    private String model;
    
    // Informations tarifaires
    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "currency", length = 3)
    private String currency = "EUR";
    
    @Column(length = 20)
    private String unit;
    
    @Column(name = "minimum_quantity")
    private Integer minimumQuantity = 1;
    
    @Column(name = "package_quantity")
    private Integer packageQuantity = 1;
    
    // Disponibilité
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "available")
    private boolean available = true;
    
    @Column(name = "delivery_time")
    private String deliveryTime;
    
    // Métadonnées
    @Column(name = "weight", precision = 8, scale = 3)
    private BigDecimal weight; // en kg
    
    @Column(name = "dimensions", length = 100)
    private String dimensions; // format: "L x l x h"
    
    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "datasheet_url", length = 500)
    private String datasheetUrl;
    
    @Column(name = "manufacturer_url", length = 500)
    private String manufacturerUrl;
    
    // Caractéristiques techniques (JSON)
    @Column(name = "technical_specs", columnDefinition = "TEXT")
    private String technicalSpecs;
    
    @Column(name = "keywords", length = 500)
    private String keywords;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @OneToMany(mappedBy = "catalogItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MaterialRequestItem> requestItems = new HashSet<>();
    
    // Constructeurs
    public CatalogItem() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public CatalogItem(SupplierCatalog catalog, String reference, String name) {
        this();
        this.catalog = catalog;
        this.reference = reference;
        this.name = name;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public SupplierCatalog getCatalog() { return catalog; }
    public void setCatalog(SupplierCatalog catalog) { this.catalog = catalog; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public Integer getMinimumQuantity() { return minimumQuantity; }
    public void setMinimumQuantity(Integer minimumQuantity) { this.minimumQuantity = minimumQuantity; }
    
    public Integer getPackageQuantity() { return packageQuantity; }
    public void setPackageQuantity(Integer packageQuantity) { this.packageQuantity = packageQuantity; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    public String getDeliveryTime() { return deliveryTime; }
    public void setDeliveryTime(String deliveryTime) { this.deliveryTime = deliveryTime; }
    
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public String getDatasheetUrl() { return datasheetUrl; }
    public void setDatasheetUrl(String datasheetUrl) { this.datasheetUrl = datasheetUrl; }
    
    public String getManufacturerUrl() { return manufacturerUrl; }
    public void setManufacturerUrl(String manufacturerUrl) { this.manufacturerUrl = manufacturerUrl; }
    
    public String getTechnicalSpecs() { return technicalSpecs; }
    public void setTechnicalSpecs(String technicalSpecs) { this.technicalSpecs = technicalSpecs; }
    
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<MaterialRequestItem> getRequestItems() { return requestItems; }
    public void setRequestItems(Set<MaterialRequestItem> requestItems) { this.requestItems = requestItems; }
    
    // Méthodes utilitaires
    public String getFullReference() {
        if (catalog != null && catalog.getSupplier() != null) {
            return catalog.getSupplier().getName() + " - " + reference;
        }
        return reference;
    }
    
    public BigDecimal getTotalPrice(Integer quantity) {
        if (unitPrice == null || quantity == null) return null;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    public boolean isInStock() {
        return available && (stockQuantity == null || stockQuantity > 0);
    }
    
    public boolean hasImage() {
        return imageUrl != null && !imageUrl.trim().isEmpty();
    }
    
    public boolean hasDatasheet() {
        return datasheetUrl != null && !datasheetUrl.trim().isEmpty();
    }
    
    public String getDisplayName() {
        return reference + " - " + name;
    }
    
    public String getSupplierName() {
        return catalog != null && catalog.getSupplier() != null ? 
               catalog.getSupplier().getName() : "N/A";
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CatalogItem)) return false;
        CatalogItem that = (CatalogItem) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}