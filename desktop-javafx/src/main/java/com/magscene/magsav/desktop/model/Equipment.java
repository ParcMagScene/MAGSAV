package com.magscene.magsav.desktop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant un équipement
 */
public class Equipment {
    
    public enum EquipmentStatus {
        AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE, RENTED
    }
    
    private Long id;
    private String name;
    private String category;
    private String brand;
    private String model;
    private String serialNumber;
    private String qrCode;
    private LocalDate purchaseDate;
    private Double purchasePrice;
    private Double currentValue;
    private String location;
    private String condition;
    private EquipmentStatus status;
    private boolean warranty;
    private LocalDate warrantyExpirationDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Equipment() {
        this.status = EquipmentStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(Double purchasePrice) { this.purchasePrice = purchasePrice; }
    
    public Double getCurrentValue() { return currentValue; }
    public void setCurrentValue(Double currentValue) { this.currentValue = currentValue; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    
    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }
    
    public boolean isWarranty() { return warranty; }
    public void setWarranty(boolean warranty) { this.warranty = warranty; }
    
    public LocalDate getWarrantyExpirationDate() { return warrantyExpirationDate; }
    public void setWarrantyExpirationDate(LocalDate warrantyExpirationDate) { this.warrantyExpirationDate = warrantyExpirationDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return name + " (" + serialNumber + ")";
    }
}
