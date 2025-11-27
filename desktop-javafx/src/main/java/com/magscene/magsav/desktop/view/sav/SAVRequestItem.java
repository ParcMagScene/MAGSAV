package com.magscene.magsav.desktop.view.sav;

import java.util.Map;

import com.magscene.magsav.desktop.component.DetailPanelProvider;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * Wrapper pour les demandes SAV implémentant DetailPanelProvider
 */
public class SAVRequestItem implements DetailPanelProvider {
    private final Map<String, Object> data;

    public SAVRequestItem(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getId() {
        return String.valueOf(data.get("id"));
    }

    public String getTitle() {
        return (String) data.get("title");
    }

    public String getType() {
        return (String) data.get("type");
    }

    public String getStatus() {
        return (String) data.get("status");
    }

    public String getPriority() {
        return (String) data.get("priority");
    }

    public String getCreatedAt() {
        String date = (String) data.get("createdAt");
        return (date != null && date.length() >= 10) ? date.substring(0, 10) : "";
    }

    public String getAssignedTechnician() {
        String tech = (String) data.get("assignedTechnician");
        return tech != null ? tech : "Non assigné";
    }

    @Override
    public String getDetailTitle() {
        return "Demande SAV #" + getId();
    }

    @Override
    public String getDetailSubtitle() {
        return getTitle();
    }

    @Override
    public Image getDetailImage() {
        return null;
    }

    @Override
    public String getQRCodeData() {
        return getId();
    }

    @Override
    public String getDetailId() {
        return getId();
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(10);
        content.getChildren().addAll(
                new Label("Type: " + getType()),
                new Label("Statut: " + getStatus()),
                new Label("Priorité: " + getPriority()),
                new Label("Date création: " + getCreatedAt()),
                new Label("Technicien: " + getAssignedTechnician()));
        return content;
    }
}
