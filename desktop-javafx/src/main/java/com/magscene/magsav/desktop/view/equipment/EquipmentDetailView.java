package com.magscene.magsav.desktop.view.equipment;

import com.magscene.magsav.desktop.component.EntityDetailView;

import java.util.Map;

/**
 * Fiche de détails spécialisée pour les équipements
 * Hérite du système unifié EntityDetailView
 */
public class EquipmentDetailView extends EntityDetailView {
    
    public EquipmentDetailView() {
        super("Équipement");
    }
    
    /**
     * Initialise la fiche avec les données d'un équipement
     */
    public void setEquipmentData(Map<String, Object> equipmentData) {
        // Vider le contenu précédent
        clearDynamicContent();
        
        // Informations principales
        String name = (String) equipmentData.getOrDefault("name", "Équipement sans nom");
        String description = (String) equipmentData.getOrDefault("description", "");
        setEntityInfo(name, description, "Équipement");
        
        // Image par défaut pour équipement
        setDefaultImage("equipment");
        
        // Section Identification (Code LOCMAT)
        String internalRef = (String) equipmentData.get("internalReference");
        if (internalRef != null) {
            internalRef = internalRef.replace("*", "").trim(); // Nettoyer les *
        }
        addInfoRow("Code LOCMAT", internalRef, true);
        addInfoRow("Numéro de série", (String) equipmentData.get("serialNumber"));
        addInfoRow("Code QR", (String) equipmentData.get("qrCode"));
        addSeparator();
        
        // Section Technique
        addInfoRow("Marque", (String) equipmentData.get("brand"));
        addInfoRow("Modèle", (String) equipmentData.get("model"));
        addInfoRow("Catégorie", (String) equipmentData.get("category"));
        addInfoRow("Poids", formatWeight(equipmentData.get("weight")));
        addInfoRow("Dimensions", (String) equipmentData.get("dimensions"));
        addSeparator();
        
        // Section État et Statut
        String status = (String) equipmentData.get("status");
        addInfoRow("Statut", status, true);
        addInfoRow("Localisation", (String) equipmentData.get("location"));
        addInfoRow("Dernière maintenance", formatDate(equipmentData.get("lastMaintenance")));
        addInfoRow("Prochaine maintenance", formatDate(equipmentData.get("nextMaintenance")));
        addSeparator();
        
        // Section Financière
        addInfoRow("Prix d'achat", formatPrice(equipmentData.get("purchasePrice")));
        addInfoRow("Date d'achat", formatDate(equipmentData.get("purchaseDate")));
        addInfoRow("Valeur d'assurance", formatPrice(equipmentData.get("insuranceValue")));
        addInfoRow("Fournisseur", (String) equipmentData.get("supplier"));
        addSeparator();
        
        // Section Garantie
        addInfoRow("Fin de garantie", formatDate(equipmentData.get("warrantyExpiration")));
        addInfoRow("Notes", (String) equipmentData.get("notes"));
        
        // QR Code
        if (equipmentData.containsKey("id")) {
            String qrData = "EQUIPMENT_" + equipmentData.get("id");
            generateQRCode(qrData);
        }
        
        // Boutons d'action
        addActionButton("✏️ Modifier", "primary", this::editEquipment);
        addActionButton("🔧 Maintenance", "secondary", this::scheduleMaintenance);
        addActionButton("📋 Historique", "secondary", this::viewHistory);
        
        // Bouton conditionnel selon le statut
        if ("DISPONIBLE".equals(status)) {
            addActionButton("📦 Réserver", "success", this::reserveEquipment);
        } else if ("MAINTENANCE".equals(status)) {
            addActionButton("✅ Valider", "success", this::completeMaintenance);
        }
    }
    
    /**
     * Formate le poids pour l'affichage
     */
    private String formatWeight(Object weight) {
        if (weight == null) return null;
        try {
            double w = Double.parseDouble(weight.toString());
            return w + " kg";
        } catch (NumberFormatException e) {
            return weight.toString();
        }
    }
    
    /**
     * Formate le prix pour l'affichage
     */
    private String formatPrice(Object price) {
        if (price == null) return null;
        try {
            double p = Double.parseDouble(price.toString());
            return String.format("%.2f €", p);
        } catch (NumberFormatException e) {
            return price.toString();
        }
    }
    
    /**
     * Formate la date pour l'affichage
     */
    private String formatDate(Object date) {
        if (date == null) return null;
        // TODO: Formatter selon le type de date reçu
        return date.toString();
    }
    
    // Actions spécifiques aux équipements
    private void editEquipment() {
        // TODO: Ouvrir le dialog d'édition
        System.out.println("Édition de l'équipement");
        close();
    }
    
    private void scheduleMaintenance() {
        // TODO: Ouvrir le planning de maintenance
        System.out.println("Planification de maintenance");
    }
    
    private void viewHistory() {
        // TODO: Afficher l'historique des interventions
        System.out.println("Historique de l'équipement");
    }
    
    private void reserveEquipment() {
        // TODO: Réserver l'équipement
        System.out.println("Réservation de l'équipement");
    }
    
    private void completeMaintenance() {
        // TODO: Valider la fin de maintenance
        System.out.println("Validation de maintenance");
    }
    
    /**
     * Méthode statique pour créer rapidement une fiche d'équipement
     */
    public static EquipmentDetailView createAndShow(Map<String, Object> equipmentData) {
        EquipmentDetailView detail = new EquipmentDetailView();
        detail.setEquipmentData(equipmentData);
        detail.show();
        return detail;
    }
}