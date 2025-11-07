package com.magscene.magsav.backend.dto;

import com.magscene.magsav.backend.entity.Equipment;
import java.time.LocalDateTime;

/**
 * DTO pour transférer les données d'équipement sans les relations JPA complexes
 */
public class EquipmentDTO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private String status;
    private String qrCode;
    private String brand;
    private String model;
    private String serialNumber;
    private Double purchasePrice;
    private LocalDateTime purchaseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String location;
    private String notes;
    private String internalReference;
    private Double weight;
    private String dimensions;
    private LocalDateTime warrantyExpiration;
    private String supplier;
    private Double insuranceValue;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDate;
    
    // Constructeur vide
    public EquipmentDTO() {}
    
    // Constructeur depuis Equipment entity
    public EquipmentDTO(Equipment equipment) {
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.description = equipment.getDescription();
        this.category = equipment.getCategory();
        this.status = equipment.getStatus() != null ? equipment.getStatus().getDisplayName() : null;
        this.qrCode = equipment.getQrCode();
        this.brand = equipment.getBrand();
        this.model = equipment.getModel();
        this.serialNumber = equipment.getSerialNumber();
        this.purchasePrice = equipment.getPurchasePrice();
        this.purchaseDate = equipment.getPurchaseDate();
        this.createdAt = equipment.getCreatedAt();
        this.updatedAt = equipment.getUpdatedAt();
        this.location = equipment.getLocation();
        this.notes = equipment.getNotes();
        this.internalReference = equipment.getInternalReference();
        this.weight = equipment.getWeight();
        this.dimensions = equipment.getDimensions();
        this.warrantyExpiration = equipment.getWarrantyExpiration();
        this.supplier = equipment.getSupplier();
        this.insuranceValue = equipment.getInsuranceValue();
        this.lastMaintenanceDate = equipment.getLastMaintenanceDate();
        this.nextMaintenanceDate = equipment.getNextMaintenanceDate();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }
    
    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getInternalReference() { return internalReference; }
    public void setInternalReference(String internalReference) { this.internalReference = internalReference; }
    
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    
    public LocalDateTime getWarrantyExpiration() { return warrantyExpiration; }
    public void setWarrantyExpiration(LocalDateTime warrantyExpiration) { this.warrantyExpiration = warrantyExpiration; }
    
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    
    public Double getInsuranceValue() { return insuranceValue; }
    public void setInsuranceValue(Double insuranceValue) { this.insuranceValue = insuranceValue; }
    
    public LocalDateTime getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    
    public LocalDateTime getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
}
