package com.magscene.magsav.desktop.view.vehicle;

import com.magscene.magsav.desktop.component.EntityDetailView;

import java.util.Map;

/**
 * Fiche de détails spécialisée pour les véhicules
 * Hérite du système unifié EntityDetailView
 */
public class VehicleDetailView extends EntityDetailView {
    
    public VehicleDetailView() {
        super("Véhicule");
    }
    
    /**
     * Initialise la fiche avec les données d'un véhicule
     */
    public void setVehicleData(Map<String, Object> vehicleData) {
        // Vider le contenu précédent
        clearDynamicContent();
        
        // Informations principales
        String name = (String) vehicleData.getOrDefault("name", "Véhicule sans nom");
        String description = (String) vehicleData.getOrDefault("description", "");
        setEntityInfo(name, description, "Véhicule");
        
        // Image par défaut pour véhicule
        setDefaultImage("vehicle");
        
        // Section Identification
        addInfoRow("Immatriculation", (String) vehicleData.get("licensePlate"), true);
        addInfoRow("Numéro de châssis", (String) vehicleData.get("chassisNumber"));
        addInfoRow("Référence interne", (String) vehicleData.get("internalRef"));
        addSeparator();
        
        // Section Technique
        addInfoRow("Marque", (String) vehicleData.get("brand"));
        addInfoRow("Modèle", (String) vehicleData.get("model"));
        addInfoRow("Année", formatYear(vehicleData.get("year")));
        addInfoRow("Carburant", (String) vehicleData.get("fuelType"));
        addInfoRow("Puissance", formatPower(vehicleData.get("power")));
        addInfoRow("Charge utile", formatWeight(vehicleData.get("payload")));
        addSeparator();
        
        // Section État et Statut
        String status = (String) vehicleData.get("status");
        addInfoRow("Statut", status, true);
        addInfoRow("Kilométrage", formatKilometers(vehicleData.get("mileage")));
        addInfoRow("Localisation", (String) vehicleData.get("location"));
        addInfoRow("Conducteur assigné", (String) vehicleData.get("assignedDriver"));
        addSeparator();
        
        // Section Entretien
        addInfoRow("Dernière révision", formatDate(vehicleData.get("lastService")));
        addInfoRow("Prochaine révision", formatDate(vehicleData.get("nextService")));
        addInfoRow("Contrôle technique", formatDate(vehicleData.get("technicalInspection")));
        addInfoRow("Fin d'assurance", formatDate(vehicleData.get("insuranceExpiry")));
        addSeparator();
        
        // Section Financière
        addInfoRow("Prix d'achat", formatPrice(vehicleData.get("purchasePrice")));
        addInfoRow("Date d'achat", formatDate(vehicleData.get("purchaseDate")));
        addInfoRow("Valeur actuelle", formatPrice(vehicleData.get("currentValue")));
        addInfoRow("Coût par km", formatPricePerKm(vehicleData.get("costPerKm")));
        addSeparator();
        
        // Section Location/Utilisation
        addInfoRow("Tarif location/jour", formatPrice(vehicleData.get("dailyRate")));
        addInfoRow("Jours utilisés", formatDays(vehicleData.get("usageDays")));
        addInfoRow("Notes", (String) vehicleData.get("notes"));
        
        // QR Code
        if (vehicleData.containsKey("id")) {
            String qrData = "VEHICLE_" + vehicleData.get("id");
            generateQRCode(qrData);
        }
        
        // Boutons d'action
        addActionButton("✏️ Modifier", "primary", this::editVehicle);
        addActionButton("🔧 Entretien", "secondary", this::scheduleMaintenance);
        addActionButton("📋 Carnet", "secondary", this::viewLogbook);
        
        // Boutons conditionnels selon le statut
        if ("DISPONIBLE".equals(status)) {
            addActionButton("📅 Réserver", "success", this::reserveVehicle);
        } else if ("EN_MAINTENANCE".equals(status)) {
            addActionButton("✅ Valider", "success", this::completeMaintenance);
        } else if ("EN_MISSION".equals(status)) {
            addActionButton("🏁 Terminer", "danger", this::endMission);
        }
    }
    
    /**
     * Formate l'année pour l'affichage
     */
    private String formatYear(Object year) {
        if (year == null) return null;
        return year.toString();
    }
    
    /**
     * Formate la puissance pour l'affichage
     */
    private String formatPower(Object power) {
        if (power == null) return null;
        try {
            int p = Integer.parseInt(power.toString());
            return p + " ch";
        } catch (NumberFormatException e) {
            return power.toString();
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
     * Formate les kilomètres pour l'affichage
     */
    private String formatKilometers(Object km) {
        if (km == null) return null;
        try {
            int k = Integer.parseInt(km.toString());
            return String.format("%,d km", k);
        } catch (NumberFormatException e) {
            return km.toString();
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
     * Formate le prix par kilomètre
     */
    private String formatPricePerKm(Object pricePerKm) {
        if (pricePerKm == null) return null;
        try {
            double p = Double.parseDouble(pricePerKm.toString());
            return String.format("%.3f €/km", p);
        } catch (NumberFormatException e) {
            return pricePerKm.toString();
        }
    }
    
    /**
     * Formate les jours d'utilisation
     */
    private String formatDays(Object days) {
        if (days == null) return null;
        try {
            int d = Integer.parseInt(days.toString());
            return d + " jours";
        } catch (NumberFormatException e) {
            return days.toString();
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
    
    // Actions spécifiques aux véhicules
    private void editVehicle() {
        // TODO: Ouvrir le dialog d'édition
        System.out.println("Édition du véhicule");
        close();
    }
    
    private void scheduleMaintenance() {
        // TODO: Ouvrir le planning de maintenance
        System.out.println("Planification de maintenance du véhicule");
    }
    
    private void viewLogbook() {
        // TODO: Afficher le carnet de bord
        System.out.println("Carnet de bord du véhicule");
    }
    
    private void reserveVehicle() {
        // TODO: Réserver le véhicule
        System.out.println("Réservation du véhicule");
    }
    
    private void completeMaintenance() {
        // TODO: Valider la fin de maintenance
        System.out.println("Validation de maintenance du véhicule");
    }
    
    private void endMission() {
        // TODO: Terminer la mission en cours
        System.out.println("Fin de mission du véhicule");
    }
    
    /**
     * Méthode statique pour créer rapidement une fiche de véhicule
     */
    public static VehicleDetailView createAndShow(Map<String, Object> vehicleData) {
        VehicleDetailView detail = new VehicleDetailView();
        detail.setVehicleData(vehicleData);
        detail.show();
        return detail;
    }
}