package com.magscene.magsav.desktop.component;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import java.util.Map;

/**
 * Implémentation générique de DetailPanelProvider pour les éléments simples
 * Peut être utilisée comme base ou adaptée pour différents types d'objets
 */
public class GenericDetailItem implements DetailPanelProvider {
    
    private final String title;
    private final String subtitle;
    private final String id;
    private final Map<String, String> properties;
    private final String qrCodePrefix;
    
    public GenericDetailItem(String title, String subtitle, String id, 
                            Map<String, String> properties, String qrCodePrefix) {
        this.title = title != null ? title : "";
        this.subtitle = subtitle != null ? subtitle : "";
        this.id = id != null ? id : "";
        this.properties = properties != null ? properties : Map.of();
        this.qrCodePrefix = qrCodePrefix != null ? qrCodePrefix : "GENERIC";
    }
    
    @Override
    public String getDetailTitle() {
        return title.isEmpty() ? "Élément sans titre" : title;
    }
    
    @Override
    public String getDetailSubtitle() {
        return subtitle;
    }
    
    @Override
    public Image getDetailImage() {
        return null; // Pas d'image par défaut
    }
    
    @Override
    public String getQRCodeData() {
        StringBuilder qrData = new StringBuilder();
        qrData.append(qrCodePrefix).append("|");
        qrData.append("ID:").append(id).append("|");
        qrData.append("TITLE:").append(title);
        return qrData.toString();
    }
    
    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(8);
        
        // Ajout automatique de toutes les propriétés
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                content.getChildren().add(
                    DetailPanel.createInfoRow(entry.getKey(), entry.getValue())
                );
            }
        }
        
        return content;
    }
    
    @Override
    public String getDetailId() {
        return id;
    }
    
    /**
     * Crée un GenericDetailItem à partir d'un Map de données
     */
    public static GenericDetailItem fromMap(Map<String, Object> data, 
                                          String titleKey, String subtitleKey, 
                                          String idKey, String qrCodePrefix) {
        String title = getStringFromMap(data, titleKey);
        String subtitle = getStringFromMap(data, subtitleKey);
        String id = getStringFromMap(data, idKey);
        
        // Convertir toutes les autres propriétés en String
        Map<String, String> properties = data.entrySet().stream()
            .filter(entry -> !entry.getKey().equals(titleKey) && 
                           !entry.getKey().equals(subtitleKey) && 
                           !entry.getKey().equals(idKey))
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue() != null ? entry.getValue().toString() : ""
            ));
        
        return new GenericDetailItem(title, subtitle, id, properties, qrCodePrefix);
    }
    
    private static String getStringFromMap(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
}