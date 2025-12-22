package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import com.magscene.magsav.desktop.service.MediaService;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import java.util.Map;

/**
 * Classe représentant un véhicule dans l'interface JavaFX
 * Encapsule les données JSON de l'API REST dans un objet JavaFX friendly
 */
public class VehicleItem implements DetailPanelProvider {
    
    private final Long id;
    private final String name;
    private final String brand;
    private final String model;
    private final String licensePlate;
    private final String type;
    private final String status;
    private final String fuelType;
    private final Double mileage;
    private final String location;
    private final String notes;
    private final String lastMaintenance;
    private final String nextMaintenance;
    private final String insuranceExpiry;
    private final String technicalControlExpiry;
    private final Double dailyRate;
    private final String availability;
    private final String owner;
    private final String color;
    private final String photoPath;
    
    public VehicleItem(Map<String, Object> vehicleData) {
        this.id = getLongValue(vehicleData, "id");
        this.name = getStringValue(vehicleData, "name");
        this.brand = getStringValue(vehicleData, "brand");
        this.model = getStringValue(vehicleData, "model");
        this.licensePlate = getStringValue(vehicleData, "licensePlate");
        this.type = convertTypeToDisplay(getStringValue(vehicleData, "type"));
        this.status = convertStatusToDisplay(getStringValue(vehicleData, "status"));
        this.fuelType = convertFuelTypeToDisplay(getStringValue(vehicleData, "fuelType"));
        this.mileage = getDoubleValue(vehicleData, "mileage");
        this.location = getStringValue(vehicleData, "currentLocation");
        this.notes = getStringValue(vehicleData, "notes");
        this.lastMaintenance = getStringValue(vehicleData, "lastMaintenanceDate");
        this.nextMaintenance = getStringValue(vehicleData, "nextMaintenanceDate");
        this.insuranceExpiry = getStringValue(vehicleData, "insuranceExpiration");
        this.technicalControlExpiry = getStringValue(vehicleData, "technicalControlExpiration");
        this.dailyRate = getDoubleValue(vehicleData, "dailyRentalRate");
        this.availability = getStringValue(vehicleData, "availability");
        this.owner = getStringValue(vehicleData, "owner");
        this.color = getStringValue(vehicleData, "color");
        this.photoPath = getStringValue(vehicleData, "photoPath");
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
    
    private String convertStatusToDisplay(String status) {
        if (status == null || status.isEmpty()) return "Inconnu";
        
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> "Disponible";
            case "IN_USE" -> "En cours d'utilisation";
            case "MAINTENANCE" -> "En maintenance";
            case "OUT_OF_ORDER" -> "Hors service";
            case "RESERVED" -> "Réservé";
            default -> capitalize(status);
        };
    }
    
    private String convertTypeToDisplay(String type) {
        if (type == null || type.isEmpty()) return "Autre";
        
        return switch (type.toUpperCase()) {
            case "VAN" -> "Fourgon";
            case "VL" -> "VL";
            case "VL_17M3" -> "VL 17 m³";
            case "VL_20M3" -> "VL 20 m³";
            case "TRUCK" -> "Camion";
            case "PORTEUR" -> "Porteur";
            case "TRACTEUR" -> "Tracteur";
            case "SEMI_REMORQUE" -> "Semi-remorque";
            case "SCENE_MOBILE" -> "Scène Mobile";
            case "TRAILER" -> "Remorque";
            case "CAR" -> "Voiture";
            case "MOTORCYCLE" -> "Moto";
            case "OTHER" -> "Autre";
            default -> capitalize(type);
        };
    }
    
    private String convertFuelTypeToDisplay(String fuelType) {
        if (fuelType == null || fuelType.isEmpty()) return "";
        
        return switch (fuelType.toUpperCase()) {
            case "DIESEL" -> "Diesel";
            case "GASOLINE", "ESSENCE" -> "Essence";
            case "ELECTRIC" -> "Électrique";
            case "HYBRID" -> "Hybride";
            case "LPG" -> "GPL";
            case "CNG" -> "GNV";
            default -> capitalize(fuelType);
        };
    }
    
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        String[] words = text.toLowerCase().split("_| ");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            
            String word = words[i];
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1));
                }
            }
        }
        
        return result.toString();
    }
    
    // Getters pour JavaFX
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getLicensePlate() { return licensePlate; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getFuelType() { return fuelType; }
    public Double getMileage() { return mileage; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }
    public String getLastMaintenance() { return lastMaintenance; }
    public String getNextMaintenance() { return nextMaintenance; }
    public String getOwner() { return owner; }
    public String getColor() { return color; }
    public String getInsuranceExpiry() { return insuranceExpiry; }
    public String getTechnicalControlExpiry() { return technicalControlExpiry; }
    public Double getDailyRate() { return dailyRate; }
    public String getAvailability() { return availability; }
    
    /**
     * Récupère le logo de la marque du véhicule
     */
    public Image getBrandLogo() {
        if (brand != null && !brand.isEmpty()) {
            return MediaService.getInstance().getBrandLogo(brand, 60, 40);
        }
        return null;
    }
    
    /**
     * Chemin de la photo du véhicule
     */
    public String getPhotoPath() {
        return photoPath;
    }
    
    /**
     * Image du véhicule chargée depuis le MediaService
     */
    public Image getVehicleImage() {
        return getVehicleImage(80, 60);
    }
    
    /**
     * Image du véhicule avec dimensions personnalisées
     */
    public Image getVehicleImage(double width, double height) {
        if (photoPath != null && !photoPath.isEmpty()) {
            return MediaService.getInstance().loadVehiclePhoto(photoPath, width, height);
        }
        return null;
    }
    
    public String getDisplayName() {
        StringBuilder name = new StringBuilder();
        if (brand != null && !brand.isEmpty()) {
            name.append(brand);
        }
        if (model != null && !model.isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(model);
        }
        if (licensePlate != null && !licensePlate.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(licensePlate);
        }
        return name.toString();
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }

    // Implémentation de DetailPanelProvider
    @Override
    public String getDetailTitle() {
        // Utiliser le nom en priorité
        if (name != null && !name.isEmpty()) {
            return name;
        }
        // Sinon utiliser marque + modèle
        StringBuilder title = new StringBuilder();
        if (brand != null && !brand.isEmpty() && model != null && !model.isEmpty()) {
            title.append(brand).append(" ").append(model);
        } else if (brand != null && !brand.isEmpty()) {
            title.append(brand);
        } else if (model != null && !model.isEmpty()) {
            title.append(model);
        } else {
            title.append("Véhicule");
        }
        return title.toString();
    }

    @Override
    public String getDetailSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        
        if (licensePlate != null && !licensePlate.isEmpty()) {
            subtitle.append("🚗 ").append(licensePlate);
        }
        
        if (type != null && !type.isEmpty()) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append(type);
        }
        
        if (fuelType != null && !fuelType.isEmpty()) {
            if (subtitle.length() > 0) subtitle.append(" • ");
            subtitle.append(fuelType);
        }
        
        return subtitle.toString();
    }

    @Override
    public Image getDetailImage() {
        // Retourner uniquement la photo du véhicule
        // Le logo de marque est géré séparément via getBrand()
        if (photoPath != null && !photoPath.isEmpty()) {
            Image vehiclePhoto = MediaService.getInstance().loadVehiclePhoto(photoPath, 120, 90);
            if (vehiclePhoto != null) {
                return vehiclePhoto;
            }
        }
        
        return null;
    }

    @Override
    public String getQRCodeData() {
        // Les véhicules n'ont pas de QR code
        return "";
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(4);
        
        // Informations principales
        if (name != null && !name.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Désignation", name));
        }
        
        if (brand != null && !brand.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Marque", brand));
        }
        
        if (model != null && !model.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Modèle", model));
        }
        
        if (color != null && !color.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Couleur", color));
        }
        
        if (owner != null && !owner.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Propriétaire", owner));
        }
        
        if (status != null && !status.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Statut", status));
        }
        
        if (fuelType != null && !fuelType.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Carburant", fuelType));
        }
        
        if (location != null && !location.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Localisation", location));
        }
        
        if (mileage != null && mileage > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Kilométrage", String.format("%.0f km", mileage)));
        }
        
        if (dailyRate != null && dailyRate > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Tarif journalier", String.format("%.2f €", dailyRate)));
        }
        
        if (lastMaintenance != null && !lastMaintenance.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Dernière maintenance", lastMaintenance));
        }
        
        if (nextMaintenance != null && !nextMaintenance.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Prochaine maintenance", nextMaintenance));
        }
        
        if (insuranceExpiry != null && !insuranceExpiry.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Expiration assurance", insuranceExpiry));
        }
        
        if (technicalControlExpiry != null && !technicalControlExpiry.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Contrôle technique", technicalControlExpiry));
        }
        
        if (availability != null && !availability.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Disponibilité", availability));
        }
        
        if (notes != null && !notes.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Notes", notes));
        }
        
        return content;
    }

    @Override
    public String getDetailId() {
        return id != null ? id.toString() : "";
    }
}
