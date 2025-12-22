package com.magscene.magsav.desktop.view.equipment;

import java.util.Map;

import com.magscene.magsav.desktop.component.DetailPanelProvider;
import com.magscene.magsav.desktop.service.MediaService;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * Wrapper pour les données d'équipement implémentant DetailPanelProvider
 * Supporte l'affichage des photos et logos de marques
 */
public class EquipmentItem implements DetailPanelProvider {
    private final Map<String, Object> data;
    
    // Cache pour l'image de l'équipement
    private Image cachedImage;
    private boolean imageLoaded = false;

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
     * Récupère le chemin de la photo de l'équipement
     */
    public String getPhotoPath() {
        return (String) data.get("photoPath");
    }
    
    /**
     * Récupère l'image de l'équipement (avec cache)
     */
    public Image getEquipmentImage() {
        if (!imageLoaded) {
            String photoPath = getPhotoPath();
            if (photoPath != null && !photoPath.isEmpty()) {
                cachedImage = MediaService.getInstance().loadEquipmentPhoto(photoPath, 180, 180);
            }
            imageLoaded = true;
        }
        return cachedImage;
    }
    
    /**
     * Invalide le cache de l'image pour forcer un rechargement
     */
    public void invalidateImageCache() {
        cachedImage = null;
        imageLoaded = false;
    }
    
    /**
     * Récupère le logo de la marque
     */
    public Image getBrandLogo() {
        String brand = getBrand();
        if (brand != null && !brand.isEmpty()) {
            return MediaService.getInstance().getBrandLogo(brand, 60, 40);
        }
        return null;
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
            if (end == -1) {
                end = notes.length(); // Pas de newline, prendre jusqu'à la fin
            }
            if (end > start) {
                String owner = notes.substring(start, end).trim();
                if (!owner.isEmpty()) {
                    return owner;
                }
            }
        }
        // Fallback: essayer le champ 'owner' directement
        String owner = (String) data.get("owner");
        if (owner != null && !owner.isEmpty()) {
            return owner;
        }
        // Par défaut: déduire le propriétaire du QR code/UID
        String uid = getQrCode();
        if (uid != null && uid.length() >= 3) {
            String prefix = uid.substring(0, 3).toUpperCase();
            // Préfixes de catégories MAG SCENE
            if (prefix.equals("SON") || prefix.equals("ECL") || prefix.equals("VID") || 
                prefix.equals("STR") || prefix.equals("ENE") || prefix.equals("DIV") || 
                prefix.equals("OUT") || prefix.equals("VEH") || prefix.equals("STO") || 
                prefix.equals("CAB") || prefix.equals("INF")) {
                return "MAG SCENE";
            }
        }
        return null;
    }
    
    public String getNotes() {
        return (String) data.get("notes");
    }
    
    /**
     * Récupère le code LocMat (référence interne)
     */
    public String getLocmatCode() {
        String code = (String) data.get("internalReference");
        // Nettoyer les "*" des codes LOCMAT
        return code != null ? code.replace("*", "").trim() : null;
    }
    
    /**
     * Récupère le numéro de série
     */
    public String getSerialNumber() {
        return (String) data.get("serialNumber");
    }
    
    /**
     * Récupère la quantité
     */
    public String getQuantity() {
        Object qty = data.get("quantity");
        if (qty != null) {
            return String.valueOf(qty);
        }
        return "1"; // Par défaut 1 si non défini
    }

    @Override
    public String getDetailTitle() {
        return getName();
    }

    @Override
    public String getDetailSubtitle() {
        String brand = getBrand();
        String category = getCategory();
        StringBuilder sb = new StringBuilder();
        if (brand != null && !brand.isEmpty()) {
            sb.append(brand);
        }
        if (category != null && !category.isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(category);
        }
        return sb.toString();
    }

    @Override
    public Image getDetailImage() {
        // Utiliser l'image mise en cache si disponible
        return getEquipmentImage();
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
        VBox content = new VBox(8);
        content.setStyle("-fx-padding: 5;");
        
        // Code LOCMAT (référence interne) - déjà nettoyé par getLocmatCode()
        String locmatCode = getLocmatCode();
        
        // Créer les labels avec style
        Label locmatLabel = new Label("📋 Code LOCMAT: " + (locmatCode != null && !locmatCode.isEmpty() ? locmatCode : "N/A"));
        locmatLabel.setStyle("-fx-font-weight: bold;");
        
        Label serialLabel = new Label("🔢 N° Série: " + (getSerialNumber() != null && !getSerialNumber().isEmpty() ? getSerialNumber() : "N/A"));
        Label categoryLabel = new Label("📁 Catégorie: " + (getCategory() != null ? getCategory() : "N/A"));
        Label statusLabel = new Label("📊 Statut: " + (getStatus() != null ? getStatus() : "N/A"));
        Label uidLabel = new Label("🏷️ UID: " + (getQrCode() != null ? getQrCode() : "N/A"));
        Label locationLabel = new Label("📍 Localisation: " + (getLocation() != null ? getLocation() : "N/A"));
        
        content.getChildren().addAll(locmatLabel, serialLabel, categoryLabel, statusLabel, uidLabel, locationLabel);
        
        // Photo
        String photoPath = getPhotoPath();
        if (photoPath != null && !photoPath.isEmpty()) {
            content.getChildren().add(new Label("📷 Photo: " + photoPath));
        }
        
        System.out.println("✅ getDetailInfoContent() appelé pour: " + getName() + " - LOCMAT: " + locmatCode);
        return content;
    }
}
