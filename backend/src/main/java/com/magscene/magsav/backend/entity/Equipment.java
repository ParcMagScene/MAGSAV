package com.magscene.magsav.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "equipment")
public class Equipment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 100)
    private String category;
    
    // Relation avec la nouvelle entitÃƒÂ© Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "equipment"})
    private Category categoryEntity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    
    @Column(unique = true, length = 50)
    private String qrCode;
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 100)
    private String model;
    
    @Column(unique = true, length = 100)
    private String serialNumber;
    
    private Double purchasePrice;
    
    private LocalDateTime purchaseDate;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Photos de l'ÃƒÂ©quipement
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<EquipmentPhoto> photos;
    
    // Localisation de l'ÃƒÂ©quipement
    @Column(length = 200)
    private String location;
    
    // Notes personnalisÃƒÂ©es
    @Column(length = 2000)
    private String notes;
    
    // RÃƒÂ©fÃƒÂ©rence interne/numÃƒÂ©ro d'inventaire
    @Column(length = 50)
    private String internalReference;
    
    // Poids en kilogrammes
    private Double weight;
    
    // Dimensions (L x l x h en cm)
    @Column(length = 50)
    private String dimensions;
    
    // Garantie (date d'expiration)
    private LocalDateTime warrantyExpiration;
    
    // Fournisseur
    @Column(length = 200)
    private String supplier;
    
    // Valeur d'assurance
    private Double insuranceValue;
    
    // DerniÃƒÂ¨re maintenance
    private LocalDateTime lastMaintenanceDate;
    
    // Prochaine maintenance programmée
    private LocalDateTime nextMaintenanceDate;
    
    // Chemin de la photo principale (relatif au dossier Photos)
    @Column(length = 500)
    private String photoPath;
    
    // Enum pour le statut
    public enum Status {
        AVAILABLE("Disponible"),
        IN_USE("En cours d'utilisation"),
        MAINTENANCE("En maintenance"),
        OUT_OF_ORDER("Hors service"),
        IN_SAV("En SAV"),
        RETIRED("Retiré du service");
        
        private final String displayName;
        
        Status(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * Convertit un displayName ou nom d'enum en Status
         */
        public static Status fromDisplayName(String name) {
            if (name == null) return null;
            
            // D'abord essayer par displayName
            for (Status s : values()) {
                if (s.displayName.equalsIgnoreCase(name)) {
                    return s;
                }
            }
            
            // Ensuite essayer par nom d'enum
            try {
                return valueOf(name.toUpperCase().replace(" ", "_").replace("'", ""));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }
    
    // Constructeurs
    public Equipment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getQrCode() {
        return qrCode;
    }
    
    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getSerialNumber() {
        return serialNumber;
    }
    
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
    
    public Double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }
    
    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
    
    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Category getCategoryEntity() {
        return categoryEntity;
    }

    public void setCategoryEntity(Category categoryEntity) {
        this.categoryEntity = categoryEntity;
    }
    
    public List<EquipmentPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<EquipmentPhoto> photos) {
        this.photos = photos;
    }
    
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInternalReference() {
        return internalReference;
    }

    public void setInternalReference(String internalReference) {
        this.internalReference = internalReference;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public LocalDateTime getWarrantyExpiration() {
        return warrantyExpiration;
    }

    public void setWarrantyExpiration(LocalDateTime warrantyExpiration) {
        this.warrantyExpiration = warrantyExpiration;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public Double getInsuranceValue() {
        return insuranceValue;
    }

    public void setInsuranceValue(Double insuranceValue) {
        this.insuranceValue = insuranceValue;
    }

    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    
    @Override
    public String toString() {
        return "Equipment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", status=" + status +
                ", qrCode='" + qrCode + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}

