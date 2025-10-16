package com.magsav.gui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import com.magsav.util.AppLogger;

/**
 * Contrôleur dédié à la gestion des statistiques
 * Extrait du MainController pour améliorer la lisibilité et la maintenabilité
 */
public class StatistiquesController {
    
    /**
     * Créer l'onglet statistiques avec tous ses sous-onglets
     */
    public Tab createStatistiquesTab() {
        Tab statistiquesTab = new Tab("Statistiques");
        statistiquesTab.setClosable(false);
        
        try {
            TabPane statistiquesTabPane = new TabPane();
            statistiquesTabPane.getStyleClass().add("sub-tab-pane");
            
            // Sous-onglets statistiques
            Tab vueEnsembleTab = new Tab("Vue d'ensemble");
            vueEnsembleTab.setClosable(false);
            vueEnsembleTab.setContent(createStatistiquesOverviewContent());
            
            Tab interventionsTab = new Tab("Interventions");
            interventionsTab.setClosable(false);
            interventionsTab.setContent(createStatistiquesInterventionsContent());
            
            Tab stockTab = new Tab("Stock");
            stockTab.setClosable(false);
            stockTab.setContent(createStatistiquesStockContent());
            
            Tab financierTab = new Tab("Financier");
            financierTab.setClosable(false);
            financierTab.setContent(createStatistiquesFinancierContent());
            
            statistiquesTabPane.getTabs().addAll(vueEnsembleTab, interventionsTab, stockTab, financierTab);
            statistiquesTab.setContent(statistiquesTabPane);
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la création des onglets Statistiques: " + e.getMessage(), e);
            
            // Contenu d'erreur
            VBox errorContent = new VBox();
            errorContent.setAlignment(Pos.CENTER);
            errorContent.setSpacing(20);
            errorContent.setPadding(new Insets(50));
            
            Label errorIcon = new Label("⚠️");
            errorIcon.setStyle("-fx-font-size: 48px;");
            
            Label errorLabel = new Label("Erreur lors du chargement des statistiques");
            errorLabel.getStyleClass().add("error-text");
            
            Label errorDetail = new Label(e.getMessage());
            errorDetail.getStyleClass().add("error-detail");
            
            errorContent.getChildren().addAll(errorIcon, errorLabel, errorDetail);
            statistiquesTab.setContent(errorContent);
        }
        
        return statistiquesTab;
    }
    
    /**
     * Créer le contenu de la vue d'ensemble des statistiques
     */
    private VBox createStatistiquesOverviewContent() {
        VBox content = new VBox();
        content.setSpacing(20);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        Label title = new Label("Statistiques générales");
        title.getStyleClass().add("content-title");
        
        // Métriques principales
        HBox metricsBox = new HBox();
        metricsBox.setSpacing(20);
        metricsBox.getStyleClass().add("metrics-container");
        
        VBox interventionsBox = createStockMetricBox("Total interventions", "156", "#4a90e2");
        VBox produitsBox = createStockMetricBox("Produits gérés", "322", "#51cf66");
        VBox ca = createStockMetricBox("CA mensuel", "€12,450", "#ffd43b");
        VBox satisfaction = createStockMetricBox("Satisfaction", "94%", "#ff6b6b");
        
        metricsBox.getChildren().addAll(interventionsBox, produitsBox, ca, satisfaction);
        
        // Graphiques placeholder
        VBox chartsBox = new VBox();
        chartsBox.setSpacing(16);
        
        VBox chart1 = createChartPlaceholder("Évolution du nombre d'interventions", "Graphique linéaire des 12 derniers mois");
        VBox chart2 = createChartPlaceholder("Répartition par type d'intervention", "Graphique en secteurs");
        
        chartsBox.getChildren().addAll(chart1, chart2);
        
        content.getChildren().addAll(title, metricsBox, chartsBox);
        
        return content;
    }
    
    /**
     * Créer le contenu des statistiques d'interventions
     */
    private VBox createStatistiquesInterventionsContent() {
        VBox content = new VBox();
        content.setSpacing(16);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        Label title = new Label("Statistiques des interventions");
        title.getStyleClass().add("content-title");
        
        VBox chart1 = createChartPlaceholder("Temps de résolution moyen", "Évolution des délais par mois");
        VBox chart2 = createChartPlaceholder("Top 10 des pannes", "Analyse des problèmes les plus fréquents");
        VBox chart3 = createChartPlaceholder("Performance par technicien", "Comparaison des interventions résolues");
        
        content.getChildren().addAll(title, chart1, chart2, chart3);
        
        return content;
    }
    
    /**
     * Créer le contenu des statistiques de stock
     */
    private VBox createStatistiquesStockContent() {
        VBox content = new VBox();
        content.setSpacing(16);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        Label title = new Label("Statistiques de stock");
        title.getStyleClass().add("content-title");
        
        VBox chart1 = createChartPlaceholder("Rotation des stocks", "Produits à rotation lente/rapide");
        VBox chart2 = createChartPlaceholder("Valorisation par catégorie", "Répartition de la valeur du stock");
        VBox chart3 = createChartPlaceholder("Évolution des sorties", "Tendances des mouvements de stock");
        
        content.getChildren().addAll(title, chart1, chart2, chart3);
        
        return content;
    }
    
    /**
     * Créer le contenu des statistiques financières
     */
    private VBox createStatistiquesFinancierContent() {
        VBox content = new VBox();
        content.setSpacing(16);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        
        Label title = new Label("Statistiques financières");
        title.getStyleClass().add("content-title");
        
        // Métriques financières
        HBox metricsBox = new HBox();
        metricsBox.setSpacing(20);
        metricsBox.getStyleClass().add("metrics-container");
        
        VBox ca = createStockMetricBox("CA annuel", "€149,680", "#51cf66");
        VBox margeBox = createStockMetricBox("Marge moyenne", "34%", "#4a90e2");
        VBox impayesBox = createStockMetricBox("Impayés", "€2,180", "#ff6b6b");
        
        metricsBox.getChildren().addAll(ca, margeBox, impayesBox);
        
        VBox chart1 = createChartPlaceholder("Évolution du chiffre d'affaires", "CA mensuel des 12 derniers mois");
        VBox chart2 = createChartPlaceholder("Répartition par client", "Top clients par CA");
        
        content.getChildren().addAll(title, metricsBox, chart1, chart2);
        
        return content;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Créer un placeholder pour graphique
     */
    private VBox createChartPlaceholder(String title, String description) {
        VBox box = new VBox();
        box.setSpacing(12);
        box.getStyleClass().add("content-section");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");
        
        VBox placeholder = new VBox();
        placeholder.setMinHeight(200);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.getStyleClass().add("chart-placeholder");
        
        Label chartIcon = new Label("📊");
        chartIcon.setStyle("-fx-font-size: 48px;");
        
        Label chartLabel = new Label(title);
        chartLabel.getStyleClass().add("placeholder-text");
        
        Label chartSubtitle = new Label(description);
        chartSubtitle.getStyleClass().add("placeholder-subtitle");
        
        placeholder.getChildren().addAll(chartIcon, chartLabel, chartSubtitle);
        box.getChildren().addAll(titleLabel, placeholder);
        
        return box;
    }
    
    /**
     * Créer une boîte de métrique pour le stock (réutilisée pour les statistiques)
     */
    private VBox createStockMetricBox(String title, String value, String color) {
        VBox box = new VBox();
        box.setSpacing(8);
        box.getStyleClass().add("metric-box");
        box.setPrefWidth(150);
        
        // Indicateur coloré
        javafx.scene.layout.Region indicator = new javafx.scene.layout.Region();
        indicator.setPrefHeight(4);
        indicator.setStyle("-fx-background-color: " + color + ";");
        
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("metric-title");
        
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("metric-value");
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        box.getChildren().addAll(indicator, titleLabel, valueLabel);
        
        return box;
    }
}