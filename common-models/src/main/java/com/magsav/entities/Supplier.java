package com.magsav.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entité représentant un fournisseur avec ses services et configurations
 */
@Entity
@Table(name = "suppliers")
public class Supplier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    @Column(name = "email", length = 150)
    private String email;
    
    @Column(name = "phone", length = 50)
    private String phone;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    // Services proposés par le fournisseur
    @Column(name = "has_after_sales_service")
    private boolean hasAfterSalesService = false;
    
    @Column(name = "has_rma_service")
    private boolean hasRMAService = false;
    
    @Column(name = "has_parts_service")
    private boolean hasPartsService = false;
    
    @Column(name = "has_equipment_service")
    private boolean hasEquipmentService = false;
    
    // Configuration financière
    @Column(name = "free_shipping_threshold", precision = 10, scale = 2)
    private BigDecimal freeShippingThreshold;
    
    @Column(name = "handling_fee", precision = 10, scale = 2)
    private BigDecimal handlingFee;
    
    // Protocoles de commande
    @Enumerated(EnumType.STRING)
    @Column(name = "order_protocol", length = 20)
    private OrderProtocol orderProtocol = OrderProtocol.EMAIL;
    
    @Column(name = "order_email", length = 150)
    private String orderEmail;
    
    @Column(name = "order_url", length = 300)
    private String orderUrl;
    
    @Column(name = "order_phone", length = 50)
    private String orderPhone;
    
    // Métadonnées
    @Column(name = "active")
    private boolean active = true;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Relations
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SupplierCatalog> catalogs = new HashSet<>();
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SupplierProcurementOrder> orders = new HashSet<>();
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GroupedOrder> groupedOrders = new HashSet<>();
    
    // Constructeurs
    public Supplier() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Supplier(String name) {
        this();
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
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public boolean isHasAfterSalesService() { return hasAfterSalesService; }
    public void setHasAfterSalesService(boolean hasAfterSalesService) { 
        this.hasAfterSalesService = hasAfterSalesService; 
    }
    
    public boolean isHasRMAService() { return hasRMAService; }
    public void setHasRMAService(boolean hasRMAService) { this.hasRMAService = hasRMAService; }
    
    public boolean isHasPartsService() { return hasPartsService; }
    public void setHasPartsService(boolean hasPartsService) { this.hasPartsService = hasPartsService; }
    
    public boolean isHasEquipmentService() { return hasEquipmentService; }
    public void setHasEquipmentService(boolean hasEquipmentService) { 
        this.hasEquipmentService = hasEquipmentService; 
    }
    
    public BigDecimal getFreeShippingThreshold() { return freeShippingThreshold; }
    public void setFreeShippingThreshold(BigDecimal freeShippingThreshold) { 
        this.freeShippingThreshold = freeShippingThreshold; 
    }
    
    public BigDecimal getHandlingFee() { return handlingFee; }
    public void setHandlingFee(BigDecimal handlingFee) { this.handlingFee = handlingFee; }
    
    public OrderProtocol getOrderProtocol() { return orderProtocol; }
    public void setOrderProtocol(OrderProtocol orderProtocol) { this.orderProtocol = orderProtocol; }
    
    public String getOrderEmail() { return orderEmail; }
    public void setOrderEmail(String orderEmail) { this.orderEmail = orderEmail; }
    
    public String getOrderUrl() { return orderUrl; }
    public void setOrderUrl(String orderUrl) { this.orderUrl = orderUrl; }
    
    public String getOrderPhone() { return orderPhone; }
    public void setOrderPhone(String orderPhone) { this.orderPhone = orderPhone; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Set<SupplierCatalog> getCatalogs() { return catalogs; }
    public void setCatalogs(Set<SupplierCatalog> catalogs) { this.catalogs = catalogs; }
    
    public Set<SupplierProcurementOrder> getOrders() { return orders; }
    public void setOrders(Set<SupplierProcurementOrder> orders) { this.orders = orders; }
    
    public Set<GroupedOrder> getGroupedOrders() { return groupedOrders; }
    public void setGroupedOrders(Set<GroupedOrder> groupedOrders) { this.groupedOrders = groupedOrders; }
    
    // Méthodes utilitaires
    public boolean supportsService(String serviceType) {
        switch (serviceType.toUpperCase()) {
            case "SAV": return hasAfterSalesService;
            case "RMA": return hasRMAService;
            case "PARTS": return hasPartsService;
            case "EQUIPMENT": return hasEquipmentService;
            default: return false;
        }
    }
    
    public boolean hasThresholdConfigured() {
        return freeShippingThreshold != null && freeShippingThreshold.compareTo(BigDecimal.ZERO) > 0;
    }
    
    @Override
    public String toString() {
        return name != null ? name : "Supplier#" + id;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Supplier)) return false;
        Supplier supplier = (Supplier) o;
        return id != null && id.equals(supplier.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
