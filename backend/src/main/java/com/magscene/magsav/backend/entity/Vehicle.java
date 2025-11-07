package com.magscene.magsav.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Entité Vehicle pour la gestion du parc véhicules
 * Gestion planning, maintenance, entretiens et locations externes
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {
    
    // Énumérations internes
    public enum VehicleType {
        VAN("Fourgon"),
        TRUCK("Camion"),
        TRAILER("Remorque"),
        CAR("Voiture"),
        MOTORCYCLE("Moto"),
        OTHER("Autre");
        
        private final String displayName;
        
        VehicleType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum VehicleStatus {
        AVAILABLE("Disponible"),
        IN_USE("En utilisation"),
        MAINTENANCE("En maintenance"),
        OUT_OF_ORDER("Hors service"),
        RENTED_OUT("Loué externe"),
        RESERVED("Réservé");
        
        private final String displayName;
        
        VehicleStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum FuelType {
        GASOLINE("Essence"),
        DIESEL("Diesel"),
        ELECTRIC("Électrique"),
        HYBRID("Hybride"),
        GPL("GPL"),
        OTHER("Autre");
        
        private final String displayName;
        
        FuelType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom du véhicule est obligatoire")
    @Size(max = 200)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "brand")
    private String brand;
    
    @Column(name = "model")
    private String model;
    
    @Column(name = "license_plate", unique = true)
    private String licensePlate;
    
    @Column(name = "vin", unique = true)
    private String vin; // Vehicle Identification Number
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private VehicleType type = VehicleType.VAN;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VehicleStatus status = VehicleStatus.AVAILABLE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type")
    private FuelType fuelType = FuelType.DIESEL;
    
    @Column(name = "year_manufactured")
    private Integer yearManufactured;
    
    @Column(name = "mileage")
    private Integer mileage; // Kilométrage
    
    @Column(name = "max_payload", precision = 10, scale = 2)
    private BigDecimal maxPayload; // Charge utile maximale en kg
    
    @Column(name = "dimensions")
    private String dimensions; // Dimensions L x l x h
    
    @Column(name = "insurance_number")
    private String insuranceNumber;
    
    @Column(name = "insurance_expiration")
    private LocalDate insuranceExpiration;
    
    @Column(name = "technical_control_expiration")
    private LocalDate technicalControlExpiration; // Contrôle technique
    
    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;
    
    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;
    
    @Column(name = "maintenance_interval_km")
    private Integer maintenanceIntervalKm; // Intervalle maintenance en km
    
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
    
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;
    
    @Column(name = "daily_rental_rate", precision = 10, scale = 2)
    private BigDecimal dailyRentalRate; // Tarif location journalier
    
    @Column(name = "current_location")
    private String currentLocation;
    
    @Column(name = "assigned_driver")
    private String assignedDriver; // Conducteur principal assigné
    
    @Column(name = "notes", length = 2000)
    private String notes;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Constructeurs
    public Vehicle() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Vehicle(String name, String brand, String model) {
        this();
        this.name = name;
        this.brand = brand;
        this.model = model;
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        updateTimestamp();
    }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { 
        this.brand = brand;
        updateTimestamp();
    }
    
    public String getModel() { return model; }
    public void setModel(String model) { 
        this.model = model;
        updateTimestamp();
    }
    
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { 
        this.licensePlate = licensePlate;
        updateTimestamp();
    }
    
    public String getVin() { return vin; }
    public void setVin(String vin) { 
        this.vin = vin;
        updateTimestamp();
    }
    
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { 
        this.type = type;
        updateTimestamp();
    }
    
    public VehicleStatus getStatus() { return status; }
    public void setStatus(VehicleStatus status) { 
        this.status = status;
        updateTimestamp();
    }
    
    public FuelType getFuelType() { return fuelType; }
    public void setFuelType(FuelType fuelType) { 
        this.fuelType = fuelType;
        updateTimestamp();
    }
    
    public Integer getYearManufactured() { return yearManufactured; }
    public void setYearManufactured(Integer yearManufactured) { 
        this.yearManufactured = yearManufactured;
        updateTimestamp();
    }
    
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { 
        this.mileage = mileage;
        updateTimestamp();
    }
    
    public BigDecimal getMaxPayload() { return maxPayload; }
    public void setMaxPayload(BigDecimal maxPayload) { 
        this.maxPayload = maxPayload;
        updateTimestamp();
    }
    
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { 
        this.dimensions = dimensions;
        updateTimestamp();
    }
    
    public String getInsuranceNumber() { return insuranceNumber; }
    public void setInsuranceNumber(String insuranceNumber) { 
        this.insuranceNumber = insuranceNumber;
        updateTimestamp();
    }
    
    public LocalDate getInsuranceExpiration() { return insuranceExpiration; }
    public void setInsuranceExpiration(LocalDate insuranceExpiration) { 
        this.insuranceExpiration = insuranceExpiration;
        updateTimestamp();
    }
    
    public LocalDate getTechnicalControlExpiration() { return technicalControlExpiration; }
    public void setTechnicalControlExpiration(LocalDate technicalControlExpiration) { 
        this.technicalControlExpiration = technicalControlExpiration;
        updateTimestamp();
    }
    
    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { 
        this.lastMaintenanceDate = lastMaintenanceDate;
        updateTimestamp();
    }
    
    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { 
        this.nextMaintenanceDate = nextMaintenanceDate;
        updateTimestamp();
    }
    
    public Integer getMaintenanceIntervalKm() { return maintenanceIntervalKm; }
    public void setMaintenanceIntervalKm(Integer maintenanceIntervalKm) { 
        this.maintenanceIntervalKm = maintenanceIntervalKm;
        updateTimestamp();
    }
    
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { 
        this.purchaseDate = purchaseDate;
        updateTimestamp();
    }
    
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { 
        this.purchasePrice = purchasePrice;
        updateTimestamp();
    }
    
    public BigDecimal getDailyRentalRate() { return dailyRentalRate; }
    public void setDailyRentalRate(BigDecimal dailyRentalRate) { 
        this.dailyRentalRate = dailyRentalRate;
        updateTimestamp();
    }
    
    public String getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(String currentLocation) { 
        this.currentLocation = currentLocation;
        updateTimestamp();
    }
    
    public String getAssignedDriver() { return assignedDriver; }
    public void setAssignedDriver(String assignedDriver) { 
        this.assignedDriver = assignedDriver;
        updateTimestamp();
    }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { 
        this.notes = notes;
        updateTimestamp();
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTimestamp();
    }
    
    /**
     * Retourne le nom complet du véhicule
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder(name);
        if (licensePlate != null && !licensePlate.trim().isEmpty()) {
            fullName.append(" (").append(licensePlate).append(")");
        }
        return fullName.toString();
    }
    
    /**
     * Retourne une représentation pour les ComboBox
     */
    public String getDisplayText() {
        StringBuilder display = new StringBuilder(name);
        if (brand != null && !brand.trim().isEmpty()) {
            display.append(" - ").append(brand);
        }
        if (model != null && !model.trim().isEmpty()) {
            display.append(" ").append(model);
        }
        if (licensePlate != null && !licensePlate.trim().isEmpty()) {
            display.append(" (").append(licensePlate).append(")");
        }
        return display.toString();
    }
    
    /**
     * Vérifie si le véhicule nécessite une maintenance
     */
    public boolean needsMaintenance() {
        if (nextMaintenanceDate != null) {
            return nextMaintenanceDate.isBefore(LocalDate.now().plusDays(30)); // 30 jours d'avance
        }
        if (lastMaintenanceDate != null && maintenanceIntervalKm != null && mileage != null) {
            // Calcul basé sur le kilométrage
            return (mileage - getLastMaintenanceMileage()) >= maintenanceIntervalKm;
        }
        return false;
    }
    
    private Integer getLastMaintenanceMileage() {
        // Logique pour récupérer le kilométrage de la dernière maintenance
        // Pour l'instant, on suppose que c'est le kilométrage actuel moins l'intervalle
        return mileage != null && maintenanceIntervalKm != null ? 
            mileage - maintenanceIntervalKm : 0;
    }
    
    /**
     * Vérifie si les documents sont à jour
     */
    public boolean hasValidDocuments() {
        LocalDate now = LocalDate.now();
        return (insuranceExpiration == null || insuranceExpiration.isAfter(now)) &&
               (technicalControlExpiration == null || technicalControlExpiration.isAfter(now));
    }
    
    @Override
    public String toString() {
        return "Vehicle{id=%d, name='%s', licensePlate='%s', status=%s}"
            .formatted(id, name, licensePlate, status);
    }
}
