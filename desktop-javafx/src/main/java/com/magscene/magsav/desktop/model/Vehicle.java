package com.magscene.magsav.desktop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modèle représentant un véhicule
 */
public class Vehicle {
    
    private Long id;
    private String type;
    private String brand;
    private String model;
    private String registrationNumber;
    private Integer year;
    private LocalDate purchaseDate;
    private Integer mileage;
    private Integer loadCapacity; // en kg
    private Integer volumeCapacity; // en m³
    private String status;
    private LocalDate lastMaintenanceDate;
    private LocalDate nextMaintenanceDate;
    private LocalDate insuranceExpiry;
    private LocalDate technicalControlExpiry;
    private Double dailyRate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Vehicle() {
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    
    public Integer getLoadCapacity() { return loadCapacity; }
    public void setLoadCapacity(Integer loadCapacity) { this.loadCapacity = loadCapacity; }
    
    public Integer getVolumeCapacity() { return volumeCapacity; }
    public void setVolumeCapacity(Integer volumeCapacity) { this.volumeCapacity = volumeCapacity; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }
    
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }
    
    public LocalDate getInsuranceExpiry() { return insuranceExpiry; }
    public void setInsuranceExpiry(LocalDate insuranceExpiry) { this.insuranceExpiry = insuranceExpiry; }
    
    public LocalDate getTechnicalControlExpiry() { return technicalControlExpiry; }
    public void setTechnicalControlExpiry(LocalDate technicalControlExpiry) { this.technicalControlExpiry = technicalControlExpiry; }
    
    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return brand + " " + model + " (" + registrationNumber + ")";
    }
}
