package com.magscene.magsav.desktop.model;package com.magscene.magsav.desktop.model;package com.magscene.magsav.desktop.model;



/**

 * Classe Equipment simplifiée pour maintenir la compatibilité après refactoring

 *//**import com.magscene.magsav.desktop.component.DetailPanel;

public class Equipment {

    private Long id; * Classe Equipment simplifiée pour maintenir la compatibilitéimport com.magscene.magsav.desktop.component.DetailPanelProvider;

    private String name;

     */import javafx.scene.image.Image;

    public Equipment() {}

    public class Equipment {import javafx.scene.layout.VBox;

    public Equipment(Long id, String name) {

        this.id = id;    private Long id;

        this.name = name;

    }    private String name;import java.time.LocalDateTime;

    

    public Long getId() {    import java.time.format.DateTimeFormatter;

        return id;

    }    public Equipment() {}

    

    public void setId(Long id) {    /**

        this.id = id;

    }    public Equipment(Long id, String name) { * DTO pour Equipment côté JavaFX

    

    public String getName() {        this.id = id; */

        return name;

    }        this.name = name;public class Equipment implements DetailPanelProvider {

    

    public void setName(String name) {    }    

        this.name = name;

    }        public enum EquipmentStatus {

}
    public Long getId() {        AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_ORDER, RETIRED

        return id;    }

    }    

        private Long id;

    public void setId(Long id) {    private String name;

        this.id = id;    private String brand;

    }    private String model;

        private String serialNumber;

    public String getName() {    private String category;

        return name;    private String description;

    }    private EquipmentStatus status;

        private String location;

    public void setName(String name) {    private String qrCode;

        this.name = name;    private String internalReference;

    }    private LocalDateTime createdAt;

}    private LocalDateTime updatedAt;

    // Constructeurs
    public Equipment() {}

    public Equipment(Long id, String name, String brand, String model, String serialNumber) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.model = model;
        this.serialNumber = serialNumber;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EquipmentStatus getStatus() { return status; }
    public void setStatus(EquipmentStatus status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getInternalReference() { return internalReference; }
    public void setInternalReference(String internalReference) { this.internalReference = internalReference; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + " (" + brand + " " + model + ")";
    }

    public String getDisplayName() {
        return toString();
    }

    // Implémentation de DetailPanelProvider
    @Override
    public String getDetailTitle() {
        return name != null ? name : "Équipement sans nom";
    }

    @Override
    public String getDetailSubtitle() {
        StringBuilder subtitle = new StringBuilder();
        if (brand != null && model != null) {
            subtitle.append(brand).append(" ").append(model);
        } else if (brand != null) {
            subtitle.append(brand);
        } else if (model != null) {
            subtitle.append(model);
        }
        
        if (serialNumber != null) {
            if (subtitle.length() > 0) {
                subtitle.append(" • ");
            }
            subtitle.append("SN: ").append(serialNumber);
        }
        
        return subtitle.toString();
    }

    @Override
    public Image getDetailImage() {
        // Essaye de charger l'image de l'équipement ou le logo fabricant
        if (name != null) {
            // Format: équipement_{nom_équipement}.jpg ou logo_{fabricant}.png
            String imageName = name.toLowerCase().replaceAll("[\\s-]+", "_");
            try {
                // Cherche d'abord l'image spécifique de l'équipement
                return new Image(getClass().getResourceAsStream("/images/equipment/" + imageName + ".jpg"));
            } catch (Exception e) {
                // Sinon cherche le logo du fabricant
                if (brand != null) {
                    String manufacturerLogo = brand.toLowerCase().replaceAll("[\\s-]+", "_");
                    try {
                        return new Image(getClass().getResourceAsStream("/images/manufacturers/" + manufacturerLogo + ".png"));
                    } catch (Exception ex) {
                        // Image par défaut pour les équipements
                        try {
                            return new Image(getClass().getResourceAsStream("/images/equipment/default_equipment.png"));
                        } catch (Exception exc) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getQRCodeData() {
        StringBuilder qrData = new StringBuilder();
        qrData.append("EQUIPMENT|");
        qrData.append("ID:").append(id != null ? id : "").append("|");
        qrData.append("NAME:").append(name != null ? name : "").append("|");
        if (serialNumber != null) {
            qrData.append("SN:").append(serialNumber).append("|");
        }
        if (internalReference != null) {
            qrData.append("REF:").append(internalReference);
        }
        return qrData.toString();
    }

    @Override
    public VBox getDetailInfoContent() {
        VBox content = new VBox(8);
        
        if (category != null && !category.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Catégorie", category));
        }
        
        if (status != null) {
            String statusText = getStatusDisplayText(status);
            content.getChildren().add(DetailPanel.createInfoRow("Statut", statusText));
        }
        
        if (location != null && !location.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Localisation", location));
        }
        
        if (internalReference != null && !internalReference.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Référence interne", internalReference));
        }
        
        if (description != null && !description.trim().isEmpty()) {
            content.getChildren().add(DetailPanel.createInfoRow("Description", description));
        }
        
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            content.getChildren().add(DetailPanel.createInfoRow("Créé le", createdAt.format(formatter)));
        }
        
        if (updatedAt != null && !updatedAt.equals(createdAt)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            content.getChildren().add(DetailPanel.createInfoRow("Modifié le", updatedAt.format(formatter)));
        }
        
        return content;
    }

    @Override
    public String getDetailId() {
        return id != null ? id.toString() : "";
    }

    private String getStatusDisplayText(EquipmentStatus status) {
        switch (status) {
            case AVAILABLE: return "Disponible";
            case IN_USE: return "En cours d'utilisation";
            case MAINTENANCE: return "En maintenance";
            case OUT_OF_ORDER: return "Hors service";
            case RETIRED: return "Retiré";
            default: return status.toString();
        }
    }
}