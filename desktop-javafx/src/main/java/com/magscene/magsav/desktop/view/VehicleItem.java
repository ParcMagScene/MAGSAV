package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import java.util.Map;

/**
 * Classe représentant un véhicule dans l'interface JavaFX
 * Encapsule les données JSON de l'API REST dans un objet JavaFX friendly
 */
public class VehicleItem implements DetailPanelProvider {
    
    private final Long id;
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
    
    public VehicleItem(Map<String, Object> vehicleData) {
        this.id = getLongValue(vehicleData, "id");
        this.brand = getStringValue(vehicleData, "brand");
        this.model = getStringValue(vehicleData, "model");
        this.licensePlate = getStringValue(vehicleData, "licensePlate");
        this.type = getStringValue(vehicleData, "type");
        this.status = convertStatusToDisplay(getStringValue(vehicleData, "status"));
        this.fuelType = getStringValue(vehicleData, "fuelType");
        this.mileage = getDoubleValue(vehicleData, "mileage");
        this.location = getStringValue(vehicleData, "location");
        this.notes = getStringValue(vehicleData, "notes");
        this.lastMaintenance = getStringValue(vehicleData, "lastMaintenance");
        this.nextMaintenance = getStringValue(vehicleData, "nextMaintenance");
        this.insuranceExpiry = getStringValue(vehicleData, "insuranceExpiry");
        this.technicalControlExpiry = getStringValue(vehicleData, "technicalControlExpiry");
        this.dailyRate = getDoubleValue(vehicleData, "dailyRate");
        this.availability = getStringValue(vehicleData, "availability");
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
    public String getInsuranceExpiry() { return insuranceExpiry; }
    public String getTechnicalControlExpiry() { return technicalControlExpiry; }
    public Double getDailyRate() { return dailyRate; }
    public String getAvailability() { return availability; }
    
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
        // Photo du véhicule ou logo fabricant
        if (model != null && brand != null) {
            // Format: véhicule_{marque}_{modèle}.jpg
            String vehicleName = (brand + "_" + model).toLowerCase().replaceAll("[\\s-]+", "_");
            try {
                return new Image(getClass().getResourceAsStream("/images/vehicles/" + vehicleName + ".jpg"));
            } catch (Exception e) {
                // Sinon cherche le logo du fabricant
                String manufacturerLogo = brand.toLowerCase().replaceAll("[\\s-]+", "_");
                try {
                    return new Image(getClass().getResourceAsStream("/images/manufacturers/" + manufacturerLogo + ".png"));
                } catch (Exception ex) {
                    // Image par défaut pour les véhicules
                    try {
                        return new Image(getClass().getResourceAsStream("/images/vehicles/default_vehicle.png"));
                    } catch (Exception exc) {
                        return null;
                    }
                }
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
        VBox content = new VBox(8);
        
        if (status != null && !status.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Statut", status));
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