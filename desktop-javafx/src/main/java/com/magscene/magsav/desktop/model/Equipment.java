package com.magscene.magsav.desktop.model;

import java.time.LocalDateTime;

/**
 * DTO pour Equipment côté JavaFX
 */
public class Equipment {
    
    public enum EquipmentStatus {
        AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_ORDER, RETIRED
    }
    
    private Long id;
    private String name;
    private String brand;
    private String model;
    private String serialNumber;
    private String category;
    private String description;
    private EquipmentStatus status;
    private String location;
    private String qrCode;
    private String internalReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructeurs
    public Equipment() {}

    public Equipment(Long id, String name, String brand, String model, String serialNumber) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getInternalReference() { return internalReference; }
    public void setInternalReference(String internalReference) { this.internalReference = internalReference; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + " (" + brand + " " + model + ")";
    }

    public String getDisplayName() {
        return toString();
    }
}