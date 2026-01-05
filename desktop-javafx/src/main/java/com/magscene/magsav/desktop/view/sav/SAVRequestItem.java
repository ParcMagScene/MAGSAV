package com.magscene.magsav.desktop.view.sav;

import java.util.Map;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import com.magscene.magsav.desktop.util.SAVTranslations;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * Wrapper pour les demandes SAV implémentant DetailPanelProvider
 */
public class SAVRequestItem implements DetailPanelProvider {
        /**
         * Indique si la demande SAV peut être validée (statut OPEN uniquement)
         * Une demande OPEN peut être validée par un admin pour créer une intervention
         */
        public boolean canBeValidated() {
            String rawStatus = (String) data.get("status");
            return rawStatus != null && rawStatus.equalsIgnoreCase("OPEN");
        }
        
        /**
         * Indique si la demande SAV a déjà été validée
         */
        public boolean isValidated() {
            String rawStatus = (String) data.get("status");
            return rawStatus != null && rawStatus.equalsIgnoreCase("VALIDATED");
        }
        
        /**
         * Indique si c'est une intervention (statut IN_PROGRESS, WAITING_PARTS, RESOLVED, CLOSED, CANCELLED, EXTERNAL)
         */
        public boolean isIntervention() {
            String rawStatus = (String) data.get("status");
            if (rawStatus == null) return false;
            return rawStatus.equalsIgnoreCase("IN_PROGRESS") ||
                   rawStatus.equalsIgnoreCase("WAITING_PARTS") ||
                   rawStatus.equalsIgnoreCase("RESOLVED") ||
                   rawStatus.equalsIgnoreCase("CLOSED") ||
                   rawStatus.equalsIgnoreCase("CANCELLED") ||
                   rawStatus.equalsIgnoreCase("EXTERNAL");
        }
        
        /**
         * Indique si c'est une demande (statut OPEN ou VALIDATED)
         */
        public boolean isDemande() {
            String rawStatus = (String) data.get("status");
            if (rawStatus == null) return false;
            return rawStatus.equalsIgnoreCase("OPEN") || rawStatus.equalsIgnoreCase("VALIDATED");
        }
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

    /**
     * Retourne le type traduit en français pour l'affichage
     */
    public String getType() {
        String rawType = (String) data.get("type");
        return SAVTranslations.translateType(rawType);
    }

    /**
     * Retourne le statut traduit en français pour l'affichage
     */
    public String getStatus() {
        String rawStatus = (String) data.get("status");
        return SAVTranslations.translateStatus(rawStatus);
    }

    /**
     * Retourne la priorité traduite en français pour l'affichage
     */
    public String getPriority() {
        String rawPriority = (String) data.get("priority");
        return SAVTranslations.translatePriority(rawPriority);
    }
    
    // ========== DONNÉES ÉQUIPEMENT LIÉ ==========
    
    /**
     * Retourne l'ID de l'équipement lié (peut être dans equipment.id ou equipmentId)
     */
    @SuppressWarnings("unchecked")
    public Long getEquipmentId() {
        // Essayer d'abord l'objet equipment
        Object equipment = data.get("equipment");
        if (equipment instanceof Map) {
            Object id = ((Map<String, Object>) equipment).get("id");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        // Sinon essayer equipmentId directement
        Object equipmentId = data.get("equipmentId");
        if (equipmentId instanceof Number) {
            return ((Number) equipmentId).longValue();
        }
        return null;
    }
    
    /**
     * Retourne le nom de l'équipement lié
     */
    @SuppressWarnings("unchecked")
    public String getEquipmentName() {
        // Essayer d'abord l'objet equipment
        Object equipment = data.get("equipment");
        if (equipment instanceof Map) {
            Object name = ((Map<String, Object>) equipment).get("name");
            if (name != null) return name.toString();
        }
        // Sinon essayer equipmentName directement
        Object equipmentName = data.get("equipmentName");
        return equipmentName != null ? equipmentName.toString() : null;
    }
    
    /**
     * Retourne le code LOCMAT de l'équipement lié
     */
    @SuppressWarnings("unchecked")
    public String getEquipmentLocmat() {
        Object equipment = data.get("equipment");
        if (equipment instanceof Map) {
            Object locmat = ((Map<String, Object>) equipment).get("locmatCode");
            if (locmat != null) return locmat.toString();
        }
        return (String) data.get("locmatCode");
    }
    
    /**
     * Retourne la marque de l'équipement lié
     */
    @SuppressWarnings("unchecked")
    public String getEquipmentBrand() {
        Object equipment = data.get("equipment");
        if (equipment instanceof Map) {
            Object brand = ((Map<String, Object>) equipment).get("brand");
            if (brand != null) return brand.toString();
        }
        return (String) data.get("brand");
    }
    
    /**
     * Retourne la catégorie de l'équipement lié
     */
    @SuppressWarnings("unchecked")
    public String getEquipmentCategory() {
        Object equipment = data.get("equipment");
        if (equipment instanceof Map) {
            Object category = ((Map<String, Object>) equipment).get("category");
            if (category != null) return category.toString();
        }
        return (String) data.get("category");
    }
    
    /**
     * Retourne le modèle de l'équipement lié
     */
    @SuppressWarnings("unchecked")
    public String getEquipmentModel() {
        Object equipment = data.get("equipment");
        if (equipment instanceof Map) {
            Object model = ((Map<String, Object>) equipment).get("model");
            if (model != null) return model.toString();
        }
        return (String) data.get("model");
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
        // Retourne la photo d'équipement si disponible
        String photoPath = getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            // Essaye avec plusieurs extensions
            Image img = com.magscene.magsav.desktop.service.MediaService.getInstance().loadEquipmentPhoto(photoPath + ".jpg", 70, 70);
            if (img == null) {
                img = com.magscene.magsav.desktop.service.MediaService.getInstance().loadEquipmentPhoto(photoPath + ".png", 70, 70);
            }
            if (img == null) {
                img = com.magscene.magsav.desktop.service.MediaService.getInstance().loadEquipmentPhoto(photoPath + ".jpeg", 70, 70);
            }
            if (img == null) {
                img = com.magscene.magsav.desktop.service.MediaService.getInstance().loadEquipmentPhoto(photoPath, 70, 70);
            }
            return img;
        }
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
    
    /**
     * Retourne le nom de la marque pour charger le logo dans le DetailPanel
     */
    @Override
    public String getBrandName() {
        return getEquipmentBrand();
    }
    
    /**
     * Retourne le chemin de la photo (LOCMAT, modèle ou nom de l'équipement lié)
     */
    @Override
    public String getPhotoPath() {
        // Priorité : LOCMAT > modèle > nom
        String locmat = getEquipmentLocmat();
        if (locmat != null && !locmat.isEmpty()) {
            return locmat;
        }
        String model = getEquipmentModel();
        if (model != null && !model.isEmpty()) {
            return model;
        }
        return getEquipmentName();
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(10);
        
        // Section Intervention SAV
        content.getChildren().add(DetailPanel.createSectionTitle("🔧 Intervention"));
        content.getChildren().addAll(
            DetailPanel.createInfoRow("Type", getType()),
            DetailPanel.createInfoRow("Statut", getStatus()),
            DetailPanel.createInfoRow("Priorité", getPriority()),
            DetailPanel.createInfoRow("Date création", getCreatedAt()),
            DetailPanel.createInfoRow("Assignation", getAssignedTechnician()));
        
        // Section Équipement lié (si présent)
        String equipName = getEquipmentName();
        if (equipName != null && !equipName.isEmpty()) {
            content.getChildren().add(DetailPanel.createSectionTitle("📦 Équipement concerné"));
            content.getChildren().add(DetailPanel.createInfoRow("Nom", equipName));
            
            String locmat = getEquipmentLocmat();
            if (locmat != null && !locmat.isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Code LOCMAT", locmat));
            }
            
            String brand = getEquipmentBrand();
            if (brand != null && !brand.isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Marque", brand));
            }
            
            String category = getEquipmentCategory();
            if (category != null && !category.isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Catégorie", category));
            }
        }
        
        return content;
    }
}
