package com.magscene.magsav.desktop.view;

import java.util.Map;

/**
 * Classe représentant un équipement dans l'interface JavaFX
 * Encapsule les données JSON de l'API REST dans un objet JavaFX friendly
 */
public class EquipmentItem {
    private final Long id;
    private final String name;
    private final String description;
    private final String category;
    private final String status;
    private final String qrCode;
    private final String brand;
    private final String model;
    private final String serialNumber;
    private final Double purchasePrice;
    private final String location;
    private final String notes;
    
    public EquipmentItem(Map<String, Object> equipmentData) {
        this.id = getLongValue(equipmentData, "id");
        this.name = getStringValue(equipmentData, "name");
        this.description = getStringValue(equipmentData, "description");
        this.category = getStringValue(equipmentData, "category");
        this.status = getStringValue(equipmentData, "status");
        this.qrCode = getStringValue(equipmentData, "qrCode");
        this.brand = getStringValue(equipmentData, "brand");
        this.model = getStringValue(equipmentData, "model");
        this.serialNumber = getStringValue(equipmentData, "serialNumber");
        this.purchasePrice = getDoubleValue(equipmentData, "purchasePrice");
        this.location = getStringValue(equipmentData, "location");
        this.notes = getStringValue(equipmentData, "notes");
    }
    
    // Méthodes utilitaires pour l'extraction sécurisée des données
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
    
    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
    
    private Double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    // Getters pour JavaFX
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    public String getQrCode() { return qrCode; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public Double getPurchasePrice() { return purchasePrice; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
    
    @Override
    public String toString() {
        return String.format("%s - %s %s (%s)", name, brand, model, status);
    }
}