package com.magsav.gui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.magsav.util.AppLogger;

/**
 * Contrôleur dédié à la gestion des exports de données
 * Extrait du MainController pour améliorer la lisibilité et la maintenabilité
 */
public class ExportController {

    /**
     * Créer l'onglet export avec toutes les options d'export
     */
    public Tab createExportTab() {
        Tab exportTab = new Tab("Export");
        exportTab.setClosable(false);
        
        try {
            VBox exportContent = createExportContent();
            exportTab.setContent(exportContent);
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la création de l'onglet Export: " + e.getMessage(), e);
            
            // Contenu d'erreur
            VBox errorContent = new VBox();
            errorContent.setAlignment(Pos.CENTER);
            errorContent.setSpacing(20);
            errorContent.setPadding(new Insets(50));
            
            Label errorIcon = new Label("⚠️");
            errorIcon.setStyle("-fx-font-size: 48px;");
            
            Label errorLabel = new Label("Erreur lors du chargement des exports");
            errorLabel.getStyleClass().add("error-text");
            
            Label errorDetail = new Label(e.getMessage());
            errorDetail.getStyleClass().add("error-detail");
            
            errorContent.getChildren().addAll(errorIcon, errorLabel, errorDetail);
            exportTab.setContent(errorContent);
        }
        
        return exportTab;
    }
    
    /**
     * Créer le contenu principal de l'export
     */
    private VBox createExportContent() {
        VBox content = new VBox();
        content.setSpacing(20);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        Label title = new Label("Export de données");
        title.getStyleClass().add("content-title");
        
        // Options d'export
        VBox exportOptions = new VBox();
        exportOptions.setSpacing(16);
        
        VBox produits = createExportOption("📦 Export produits", "Exporter la liste complète des produits", "CSV, Excel, PDF");
        VBox interventions = createExportOption("🔧 Export interventions", "Exporter l'historique des interventions", "CSV, Excel, PDF");
        VBox stock = createExportOption("📊 Export stock", "Exporter les données de stock et mouvements", "CSV, Excel");
        VBox clients = createExportOption("👥 Export clients", "Exporter la base clients", "CSV, Excel, vCard");
        VBox statistiques = createExportOption("📈 Export statistiques", "Exporter les rapports statistiques", "PDF, Excel");
        
        exportOptions.getChildren().addAll(produits, interventions, stock, clients, statistiques);
        
        content.getChildren().addAll(title, exportOptions);
        
        return content;
    }
    
    /**
     * Créer une option d'export
     */
    private VBox createExportOption(String title, String description, String formats) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.getStyleClass().add("rapport-option");
        
        HBox headerBox = new HBox();
        headerBox.setSpacing(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("rapport-title");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button exportBtn = new Button("Exporter");
        exportBtn.getStyleClass().add("primary-button");
        exportBtn.setOnAction(e -> showExportAlert("Info", "Export " + title + " à implémenter"));
        
        headerBox.getChildren().addAll(titleLabel, spacer, exportBtn);
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("rapport-description");
        
        Label formatsLabel = new Label("Formats: " + formats);
        formatsLabel.getStyleClass().add("placeholder-subtitle");
        
        box.getChildren().addAll(headerBox, descLabel, formatsLabel);
        
        return box;
    }
    
    /**
     * Afficher une alerte pour les exports
     */
    private void showExportAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}