package com.magsav.service;

import com.magsav.util.Views;
import com.magsav.util.AppLogger;
import javafx.stage.Stage;

/**
 * Service de navigation centralisé avec méthodes statiques
 */
public final class NavigationService {
    
    // Méthodes statiques pour faciliter l'utilisation dans les contrôleurs
    public static void openProductManagement() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Gestion des Produits");
        Views.openInNewWindow("/fxml/products/management/product_management.fxml", "Gestion des Produits");
    }
    
    public static void openCategories() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Catégories");
        try {
            Views.openInNewWindow("/fxml/categories/categories.fxml", "Catégories");
            AppLogger.info("gui", "Navigation: Fenêtre Catégories ouverte avec succès");
        } catch (Exception e) {
            AppLogger.error("Navigation: Erreur lors de l'ouverture des catégories: " + e.getMessage());
            e.printStackTrace();
            // Afficher une alerte à l'utilisateur
            try {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Erreur lors de l'ouverture des catégories");
                alert.setContentText("Détails: " + e.getMessage());
                alert.showAndWait();
            } catch (Exception alertError) {
                System.err.println("Impossible d'afficher l'alerte d'erreur: " + alertError.getMessage());
            }
        }
    }
    
    public static void openManufacturers() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Fabricants");
        Views.openInNewWindow("/fxml/societes/lists/manufacturers.fxml", "Fabricants");
    }
    
    public static void openSuppliers() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Fournisseurs");
        Views.openInNewWindow("/fxml/societes/lists/suppliers.fxml", "Fournisseurs");
    }
    
    public static void openExternalSav() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre SAV Externe");
        Views.openInNewWindow("/fxml/societes/lists/external_sav.fxml", "SAV Externe");
    }
    
    public static void openClients() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Clients");
        Views.openInNewWindow("/fxml/societes/hubs/hub_clients.fxml", "Gestion des Clients");
    }
    
    public static void openRequestsParts() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Demandes de pièces");
        Views.openInNewWindow("/fxml/requests/lists/requests_parts.fxml", "Demandes de pièces");
    }
    
    public static void openRequestsEquipment() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Demandes d'équipement");
        Views.openInNewWindow("/fxml/requests/lists/requests_equipment.fxml", "Demandes d'équipement");
    }
    
    public static void openProductDetail(Long productId) {
        if (productId == null) {
            AppLogger.warn("gui", "Navigation: ID produit null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail produit ID: " + productId);
        Views.openProductSheet(productId);
    }
    
    public static void openInterventionDetail(Long interventionId) {
        if (interventionId == null) {
            AppLogger.warn("gui", "Navigation: ID intervention null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail intervention ID: " + interventionId);
        Views.openInterventionDetail(interventionId);
    }
    
    public static void openManufacturerDetail(Long manufacturerId) {
        if (manufacturerId == null) {
            AppLogger.warn("gui", "Navigation: ID fabricant null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail fabricant ID: " + manufacturerId);
        Views.openManufacturerDetail(manufacturerId);
    }
    
    public static void openManufacturerDetail(com.magsav.model.Company manufacturer) {
        if (manufacturer == null) {
            AppLogger.warn("gui", "Navigation: Fabricant null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail fabricant: " + manufacturer.getName());
        Views.openManufacturerDetail(manufacturer);
    }
    
    public static void openClientDetail(Long clientId) {
        if (clientId == null) {
            AppLogger.warn("gui", "Navigation: ID client null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail client ID: " + clientId);
        Views.openClientDetail(clientId);
    }
    
    public static void openClientDetail(com.magsav.model.Company client) {
        if (client == null) {
            AppLogger.warn("gui", "Navigation: Client null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail client: " + client.getName());
        Views.openClientDetail(client);
    }
    
    public static void openCompanyDetail(Long companyId) {
        if (companyId == null) {
            AppLogger.warn("gui", "Navigation: ID société null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail société ID: " + companyId);
        Views.openCompanyDetail(companyId);
    }
    
    public static void openCompanyDetail(com.magsav.model.Company company) {
        if (company == null) {
            AppLogger.warn("gui", "Navigation: Société null");
            return;
        }
        AppLogger.info("gui", "Navigation: Ouverture détail société: " + company.getName());
        Views.openCompanyDetail(company);
    }
    
    public static void openPreferences() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Préférences");
        Views.openInNewWindow("/fxml/preferences.fxml", "Préférences MAGSAV");
    }
    
    public static void openPreferences(String selectedTab) {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Préférences - onglet " + selectedTab);
        Views.openPreferencesWithTab("/fxml/preferences.fxml", "Préférences MAGSAV", selectedTab);
    }
    
    public static void openNewIntervention(Long productId) {
        AppLogger.info("gui", "Navigation: Ouverture nouvelle intervention" + 
                      (productId != null ? " pour produit ID: " + productId : ""));
        Views.openInNewWindow("/fxml/interventions/forms/new_intervention.fxml", "Nouvelle intervention");
    }
    
    public static void closeCurrentWindow(javafx.scene.Node node) {
        if (node == null) {
            AppLogger.warn("gui", "Navigation: Node null pour fermeture");
            return;
        }
        AppLogger.info("gui", "Navigation: Fermeture fenêtre courante");
        Stage stage = (Stage) node.getScene().getWindow();
        stage.close();
    }
    
    public static boolean confirmClose(String windowName, boolean hasUnsavedChanges) {
        AppLogger.info("gui", "Navigation: Demande confirmation fermeture fenêtre: " + windowName);
        
        if (!hasUnsavedChanges) {
            return true;
        }
        
        // TODO: Implémenter dialogue de confirmation
        AppLogger.warn("gui", "Navigation: Changements non sauvegardés ignorés temporairement");
        return true;
    }
    
    public static void openRequestDetail(long requestId) {
        AppLogger.info("gui", "Navigation: Ouverture détail demande ID: " + requestId);
        Views.openRequestDetail(requestId);
    }

    public static void openUserDetail(Integer userId) {
        AppLogger.info("gui", "Navigation: Ouverture détail utilisateur ID: " + userId);
        Views.openUserDetail(userId);
    }

    public static void openVehiculeDetail(int vehiculeId) {
        AppLogger.info("gui", "Navigation: Ouverture détail véhicule ID: " + vehiculeId);
        Views.openVehiculeDetail(vehiculeId);
    }

    public static void openImageMaintenance() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Gestion des Médias");
        Views.openInNewWindow("/fxml/media_management_v2.fxml", "Gestion des Médias");
    }

    public static void openImageScrapingPreferences() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Préférences par onglets");
        Views.openInNewWindow("/fxml/preferences.fxml", "Préférences de l'Application");
    }
    
    public static void openImageValidation() {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre Validation des Images");
        Views.openInNewWindow("/fxml/image_validation.fxml", "Validation des Correspondances Images-Produits");
    }
    
    // ==================== NOUVELLES MÉTHODES POUR INTERFACES CENTRALISÉES ====================
    
    public static void openInNewWindow(String fxmlPath, String title) {
        AppLogger.info("gui", "Navigation: Ouverture fenêtre " + title);
        Views.openInNewWindow(fxmlPath, title);
    }
    
    public static void showInfoDialog(String title, String message) {
        AppLogger.info("gui", "Navigation: Affichage dialog info - " + title);
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'affichage du dialog d'information", e);
        }
    }
    
    public static void showErrorDialog(String title, String message) {
        AppLogger.error("Navigation: Affichage dialog erreur - " + title + ": " + message);
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'affichage du dialog d'erreur", e);
        }
    }
    
    public static boolean showConfirmDialog(String title, String message) {
        AppLogger.info("gui", "Navigation: Affichage dialog confirmation - " + title);
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK;
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'affichage du dialog de confirmation", e);
            return false;
        }
    }
    
    private NavigationService() {}
}