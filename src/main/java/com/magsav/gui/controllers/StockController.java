package com.magsav.gui.controllers;

import com.magsav.gui.utils.TabBuilderUtils;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Pos;

import java.util.Arrays;

/**
 * Contrôleur dédié à la section Stock
 * Gère les onglets Aperçu, Mouvements, Alertes, Rapports
 */
public class StockController {
    
    /**
     * Crée l'onglet Aperçu du stock
     */
    public Tab createStockOverviewTab() {
        Tab tab = TabBuilderUtils.createBasicTab("📊 Aperçu");
        VBox content = TabBuilderUtils.createTabContent();
        
        // Métriques du stock
        HBox metricsBox = new HBox();
        metricsBox.setSpacing(0);
        metricsBox.getStyleClass().add("metrics-container");
        
        VBox totalBox = createStockMetricBox("Total produits", "322", "#4a90e2");
        VBox stockBasBox = createStockMetricBox("Stock bas", "12", "#ff6b6b");
        VBox valeurBox = createStockMetricBox("Valeur totale", "€45,234", "#51cf66");
        VBox mouvementsBox = createStockMetricBox("Mouvements (7j)", "28", "#ffd43b");
        
        metricsBox.getChildren().addAll(totalBox, stockBasBox, valeurBox, mouvementsBox);
        
        // Graphique simple représentant l'évolution du stock
        VBox chartBox = new VBox();
        chartBox.setSpacing(12);
        chartBox.getStyleClass().add("content-section");
        
        Label chartTitle = new Label("Évolution du stock (30 derniers jours)");
        chartTitle.getStyleClass().add("section-title");
        
        // Placeholder pour graphique
        VBox chartPlaceholder = new VBox();
        chartPlaceholder.setMinHeight(200);
        chartPlaceholder.setAlignment(Pos.CENTER);
        chartPlaceholder.getStyleClass().add("chart-placeholder");
        
        Label chartLabel = new Label("📈 Graphique d'évolution du stock");
        chartLabel.getStyleClass().add("placeholder-text");
        chartPlaceholder.getChildren().add(chartLabel);
        
        chartBox.getChildren().addAll(chartTitle, chartPlaceholder);
        
        // Alertes rapides
        VBox alertsBox = new VBox();
        alertsBox.setSpacing(12);
        alertsBox.getStyleClass().add("content-section");
        
        Label alertsTitle = new Label("Alertes récentes");
        alertsTitle.getStyleClass().add("section-title");
        
        VBox alertsList = new VBox();
        alertsList.setSpacing(8);
        
        // Exemples d'alertes
        alertsList.getChildren().addAll(
            createAlertItem("⚠️ Stock faible", "Produit XYZ - Quantité: 2", "warning"),
            createAlertItem("🔴 Rupture de stock", "Produit ABC - Quantité: 0", "danger"),
            createAlertItem("📦 Réapprovisionnement", "Commande #123 livrée", "success")
        );
        
        alertsBox.getChildren().addAll(alertsTitle, alertsList);
        
        content.getChildren().addAll(metricsBox, chartBox, alertsBox);
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Mouvements de stock
     */
    public Tab createStockMouvementsTab() {
        Tab tab = TabBuilderUtils.createBasicTab("📦 Mouvements");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Mouvements de stock");
        title.getStyleClass().add("content-title");
        
        // Contrôles de filtrage
        HBox controlsBox = new HBox();
        controlsBox.setSpacing(10);
        
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Entrée", "Sortie", "Transfert", "Ajustement");
        typeFilter.setValue("Tous");
        typeFilter.getStyleClass().add("dark-combo-box");
        
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un produit...");
        searchField.getStyleClass().add("dark-text-field");
        
        Button searchBtn = new Button("🔍");
        searchBtn.getStyleClass().add("icon-button");
        
        controlsBox.getChildren().addAll(typeFilter, searchField, searchBtn);
        
        // Table des mouvements
        TableView<String> mouvementsTable = new TableView<>();
        mouvementsTable.getStyleClass().add("dark-table-view");
        
        TableColumn<String, String> dateCol = new TableColumn<>("Date");
        dateCol.setPrefWidth(120);
        
        TableColumn<String, String> produitCol = new TableColumn<>("Produit");
        produitCol.setPrefWidth(150);
        
        TableColumn<String, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        
        TableColumn<String, String> quantiteCol = new TableColumn<>("Quantité");
        quantiteCol.setPrefWidth(80);
        
        TableColumn<String, String> utilisateurCol = new TableColumn<>("Utilisateur");
        utilisateurCol.setPrefWidth(100);
        
        TableColumn<String, String> commentaireCol = new TableColumn<>("Commentaire");
        commentaireCol.setPrefWidth(200);
        
        mouvementsTable.getColumns().addAll(Arrays.asList(dateCol, produitCol, typeCol, quantiteCol, utilisateurCol, commentaireCol));
        
        // Placeholder pour les données
        Label placeholder = new Label("Aucun mouvement de stock récent");
        placeholder.getStyleClass().add("table-placeholder");
        mouvementsTable.setPlaceholder(placeholder);
        
        VBox.setVgrow(mouvementsTable, Priority.ALWAYS);
        content.getChildren().addAll(title, controlsBox, mouvementsTable);
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Alertes de stock
     */
    public Tab createStockAlertesTab() {
        Tab tab = TabBuilderUtils.createBasicTab("⚠️ Alertes");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Alertes de stock");
        title.getStyleClass().add("content-title");
        
        // Résumé des alertes
        HBox summaryBox = new HBox();
        summaryBox.setSpacing(20);
        summaryBox.getStyleClass().add("alert-summary");
        
        VBox criticalBox = createAlertSummaryBox("Critiques", "3", "#ff4757");
        VBox warningBox = createAlertSummaryBox("Avertissements", "12", "#ffa502");
        VBox infoBox = createAlertSummaryBox("Informations", "5", "#3742fa");
        
        summaryBox.getChildren().addAll(criticalBox, warningBox, infoBox);
        
        // Liste des alertes détaillées
        VBox alertsListBox = new VBox();
        alertsListBox.setSpacing(8);
        alertsListBox.getStyleClass().add("alerts-list");
        
        // Exemples d'alertes détaillées
        alertsListBox.getChildren().addAll(
            createDetailedAlert("🔴 Rupture de stock", "Microphone Sans-fil XLR", "Stock: 0 unités", "critical"),
            createDetailedAlert("⚠️ Stock faible", "Projecteur LED 500W", "Stock: 2 unités (seuil: 5)", "warning"),
            createDetailedAlert("⚠️ Stock faible", "Câble XLR 10m", "Stock: 3 unités (seuil: 10)", "warning"),
            createDetailedAlert("ℹ️ Réapprovisionnement", "Console de mixage", "Commande passée le 12/10", "info")
        );
        
        VBox.setVgrow(alertsListBox, Priority.ALWAYS);
        content.getChildren().addAll(title, summaryBox, alertsListBox);
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Rapports de stock
     */
    public Tab createStockRapportsTab() {
        Tab tab = TabBuilderUtils.createBasicTab("📋 Rapports");
        VBox content = TabBuilderUtils.createTabContent();
        
        Label title = new Label("Rapports de stock");
        title.getStyleClass().add("content-title");
        
        // Options de rapports
        VBox rapportsBox = new VBox();
        rapportsBox.setSpacing(16);
        rapportsBox.getStyleClass().add("reports-container");
        
        rapportsBox.getChildren().addAll(
            createRapportOption("Inventaire complet", "Liste détaillée de tous les produits en stock"),
            createRapportOption("Valeur du stock", "Rapport financier sur la valeur totale du stock"),
            createRapportOption("Mouvements mensuels", "Analyse des mouvements de stock du mois"),
            createRapportOption("Produits à faible rotation", "Identification des produits peu utilisés"),
            createRapportOption("Prévisions de réapprovisionnement", "Suggestions basées sur l'historique")
        );
        
        content.getChildren().addAll(title, rapportsBox);
        
        tab.setContent(content);
        return tab;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    private VBox createStockMetricBox(String label, String value, String color) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("metric-box");
        box.setStyle("-fx-border-color: " + color + ";");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        Label labelText = new Label(label);
        labelText.getStyleClass().add("metric-label");
        
        box.getChildren().addAll(valueLabel, labelText);
        return box;
    }
    
    private HBox createAlertItem(String title, String description, String type) {
        HBox alertBox = new HBox();
        alertBox.setSpacing(10);
        alertBox.getStyleClass().addAll("alert-item", "alert-" + type);
        
        VBox textBox = new VBox();
        textBox.setSpacing(2);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("alert-description");
        
        textBox.getChildren().addAll(titleLabel, descLabel);
        alertBox.getChildren().add(textBox);
        
        return alertBox;
    }
    
    private VBox createAlertSummaryBox(String title, String count, String color) {
        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("alert-summary-box");
        box.setStyle("-fx-border-color: " + color + ";");
        
        Label countLabel = new Label(count);
        countLabel.getStyleClass().add("alert-count");
        countLabel.setStyle("-fx-text-fill: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        
        box.getChildren().addAll(countLabel, titleLabel);
        return box;
    }
    
    private HBox createDetailedAlert(String icon, String title, String description, String type) {
        HBox alertBox = new HBox();
        alertBox.setSpacing(12);
        alertBox.getStyleClass().addAll("detailed-alert", "alert-" + type);
        
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("alert-icon");
        
        VBox textBox = new VBox();
        textBox.setSpacing(4);
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("alert-title");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("alert-description");
        
        textBox.getChildren().addAll(titleLabel, descLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button actionBtn = new Button("Action");
        actionBtn.getStyleClass().add("alert-action-btn");
        
        alertBox.getChildren().addAll(iconLabel, textBox, spacer, actionBtn);
        return alertBox;
    }
    
    private VBox createRapportOption(String title, String description) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.getStyleClass().add("rapport-option");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("rapport-title");
        
        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("rapport-description");
        
        Button generateBtn = new Button("Générer");
        generateBtn.getStyleClass().add("primary-button");
        
        box.getChildren().addAll(titleLabel, descLabel, generateBtn);
        return box;
    }
}