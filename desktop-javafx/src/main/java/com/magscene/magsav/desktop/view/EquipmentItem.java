package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Classe représentant un équipement dans l'interface JavaFX
 * Encapsule les données JSON de l'API REST dans un objet JavaFX friendly
 */
public class EquipmentItem implements DetailPanelProvider {
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
        this.status = convertStatusToDisplay(getStringValue(equipmentData, "status"));
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
    
    // Conversion des statuts de la DB vers l'affichage
    private String convertStatusToDisplay(String dbStatus) {
        if (dbStatus == null || dbStatus.isEmpty()) {
            return "Inconnu";
        }
        
        switch (dbStatus.toUpperCase()) {
            case "AVAILABLE":
                return "Disponible";
            case "IN_USE":
                return "En Cours D'utilisation";
            case "MAINTENANCE":
                return "En Maintenance";
            case "OUT_OF_ORDER":
                return "Hors Service";
            case "IN_SAV":
                return "En SAV";
            case "RETIRED":
                return "Retiré Du Service";
            default:
                // Formatage en Title Case pour les valeurs inconnues
                return formatToTitleCase(dbStatus);
        }
    }
    
    // Méthode utilitaire pour formater en Title Case
    private String formatToTitleCase(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String[] words = text.toLowerCase().split("_");
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

    // Implémentation de DetailPanelProvider
    @Override
    public String getDetailTitle() {
        return name != null && !name.isEmpty() ? name : "Équipement sans nom";
    }

    @Override
    public String getDetailSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        if (brand != null && !brand.isEmpty() && model != null && !model.isEmpty()) {
            subtitle.append(brand).append(" ").append(model);
        } else if (brand != null && !brand.isEmpty()) {
            subtitle.append(brand);
        } else if (model != null && !model.isEmpty()) {
            subtitle.append(model);
        }
        
        if (serialNumber != null && !serialNumber.isEmpty()) {
            if (subtitle.length() > 0) {
                subtitle.append(" • ");
            }
            subtitle.append("SN: ").append(serialNumber);
        }
        
        return subtitle.toString();
    }

    @Override
    public Image getDetailImage() {
        // Pour l'instant, pas d'image - sera ajoutée plus tard
        return null;
    }

    @Override
    public String getQRCodeData() {
        StringBuilder qrData = new StringBuilder();
        qrData.append("EQUIPMENT|");
        qrData.append("ID:").append(id != null ? id : "").append("|");
        qrData.append("NAME:").append(name != null ? name : "").append("|");
        if (serialNumber != null && !serialNumber.isEmpty()) {
            qrData.append("SN:").append(serialNumber).append("|");
        }
        if (qrCode != null && !qrCode.isEmpty()) {
            qrData.append("QR:").append(qrCode);
        }
        return qrData.toString();
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(8);
        
        if (category != null && !category.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Catégorie", category));
        }
        
        if (status != null && !status.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Statut", status));
        }
        
        if (location != null && !location.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Localisation", location));
        }
        
        if (purchasePrice != null && purchasePrice > 0) {
            content.getChildren().add(DetailPanel.createInfoRow("Prix d'achat", String.format("%.2f €", purchasePrice)));
        }
        
        if (description != null && !description.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Description", description));
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
