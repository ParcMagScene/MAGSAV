package com.magscene.magsav.desktop.view.equipment;

import java.util.Map;

import com.magscene.magsav.desktop.component.DetailPanelProvider;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * Wrapper pour les données d'équipement implémentant DetailPanelProvider
 */
public class EquipmentItem implements DetailPanelProvider {
    private final Map<String, Object> data;

    public EquipmentItem(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getId() {
        return String.valueOf(data.get("id"));
    }

    public String getName() {
        return (String) data.get("name");
    }

    public String getBrand() {
        return (String) data.get("brand");
    }

    public String getCategory() {
        return (String) data.get("category");
    }
    
    /**
     * Récupère la catégorie parente depuis les notes d'import LOCMAT
     * Format attendu: "Catégorie: SONORISATION\nSous-catégorie: ENCEINTE"
     */
    public String getParentCategory() {
        String notes = getNotes();
        if (notes != null && notes.contains("Catégorie:")) {
            // Extraire la catégorie des notes
            int start = notes.indexOf("Catégorie:") + 10;
            int end = notes.indexOf("\n", start);
            if (end > start) {
                return notes.substring(start, end).trim();
            }
        }
        // Fallback: utiliser la catégorie comme catégorie parente
        return getCategory();
    }

    public String getStatus() {
        return (String) data.get("status");
    }

    public String getQrCode() {
        return (String) data.get("qrCode");
    }

    public String getLocation() {
        return (String) data.get("location");
    }
    
    public String getSupplier() {
        // D'abord essayer le champ supplier
        String supplier = (String) data.get("supplier");
        if (supplier != null && !supplier.isEmpty()) {
            return supplier;
        }
        // Sinon extraire le propriétaire des notes LOCMAT
        String notes = getNotes();
        if (notes != null && notes.contains("Propriétaire:")) {
            int start = notes.indexOf("Propriétaire:") + 13;
            int end = notes.indexOf("\n", start);
            if (end > start) {
                return notes.substring(start, end).trim();
            }
        }
        return null;
    }
    
    public String getNotes() {
        return (String) data.get("notes");
    }

    @Override
    public String getDetailTitle() {
        return getName();
    }

    @Override
    public String getDetailSubtitle() {
        return getBrand() + " - " + getCategory();
    }

    @Override
    public Image getDetailImage() {
        return null;
    }

    @Override
    public String getQRCodeData() {
        return getQrCode();
    }

    @Override
    public String getDetailId() {
        return getId();
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("ID: " + getId()),
                new Label("Marque: " + getBrand()),
                new Label("Catégorie: " + getCategory()),
                new Label("Statut: " + getStatus()),
                new Label("Propriétaire: " + (getSupplier() != null ? getSupplier() : "Non défini")),
                new Label("QR Code: " + getQrCode()),
                new Label("Localisation: " + getLocation()));
        if (getNotes() != null && !getNotes().isEmpty()) {
            content.getChildren().add(new Label("Notes: " + getNotes()));
        }
        return content;
    }
}
