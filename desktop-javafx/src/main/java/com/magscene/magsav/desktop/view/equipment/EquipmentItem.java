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

    public String getStatus() {
        return (String) data.get("status");
    }

    public String getQrCode() {
        return (String) data.get("qrCode");
    }

    public String getLocation() {
        return (String) data.get("location");
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
                new Label("QR Code: " + getQrCode()),
                new Label("Localisation: " + getLocation()));
        return content;
    }
}
