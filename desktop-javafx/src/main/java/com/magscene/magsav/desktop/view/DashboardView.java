package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.DashboardService;
import com.magscene.magsav.desktop.service.DashboardService.CategoryData;
import com.magscene.magsav.desktop.service.DashboardService.DashboardStats;
import com.magscene.magsav.desktop.service.DashboardService.MonthlyData;
import com.magscene.magsav.desktop.theme.ThemeConstants;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Dashboard principal de MAGSAV-3.0
 * Vue d'ensemble avec statistiques et informations clés
 * Utilise les données réelles du backend
 */
public class DashboardView extends BorderPane {

    private final DashboardService dashboardService;
    
    // Labels des cartes statistiques pour mise à jour dynamique
    private Label equipmentValueLabel;
    private Label savValueLabel;
    private Label clientsValueLabel;
    private Label vehiclesValueLabel;
    
    // Graphiques pour mise à jour dynamique
    private BarChart<String, Number> savBarChart;
    private PieChart equipmentPieChart;

    public DashboardView() {
        this.dashboardService = DashboardService.getInstance();
        initializeComponents();
        createLayout();
        loadDashboardData();
    }

    private void initializeComponents() {
        this.getStyleClass().add("dashboard-container");
    }

    private void createLayout() {
        // En-tête du Dashboard
        VBox header = createHeaderSection();
        this.setTop(header);

        // Contenu principal avec cartes statistiques
        VBox mainContent = createMainContent();
        this.setCenter(mainContent);

        // Footer avec informations système
        HBox footer = createFooterSection();
        this.setBottom(footer);

        // Padding uniforme - utilise ThemeConstants
        setPadding(ThemeConstants.PADDING_STANDARD);
    }

    private VBox createHeaderSection() {
        // Plus de header avec titre - navigation par onglets sans titre
        VBox header = new VBox(0);
        header.setVisible(false);
        header.setManaged(false);
        return header;
    }

    private VBox createMainContent() {
        VBox content = new VBox(ThemeConstants.SPACING_XL);
        content.setPadding(ThemeConstants.PADDING_STANDARD);

        // Cartes statistiques rapides
        HBox statsCards = createStatsCards();

        // Graphiques et données
        HBox chartsSection = createChartsSection();

        // Actions rapides
        HBox quickActions = createQuickActions();

        content.getChildren().addAll(statsCards, chartsSection, quickActions);
        return content;
    }

    private HBox createStatsCards() {
        HBox statsContainer = new HBox(15);
        statsContainer.setAlignment(Pos.CENTER);

        // Carte Équipements - valeur initialisée à "..." en attendant les données
        VBox equipmentCard = createStatsCard("📦", "Équipements", "...", "Total en parc");
        equipmentValueLabel = (Label) equipmentCard.getChildren().get(2);

        // Carte SAV
        VBox savCard = createStatsCard("🔧", "SAV Actifs", "...", "En cours");
        savValueLabel = (Label) savCard.getChildren().get(2);

        // Carte Clients
        VBox clientsCard = createStatsCard("👥", "Clients", "...", "Total");
        clientsValueLabel = (Label) clientsCard.getChildren().get(2);

        // Carte Véhicules
        VBox vehiclesCard = createStatsCard("🚐", "Véhicules", "...", "Flotte");
        vehiclesValueLabel = (Label) vehiclesCard.getChildren().get(2);

        statsContainer.getChildren().addAll(equipmentCard, savCard, clientsCard, vehiclesCard);
        return statsContainer;
    }

    private VBox createStatsCard(String icon, String title, String value, String description) {
        VBox card = new VBox(8);
        card.getStyleClass().add("dashboard-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setPrefWidth(200);
        card.setPrefHeight(120);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("card-icon");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("card-description");

        card.getChildren().addAll(iconLabel, titleLabel, valueLabel, descLabel);
        return card;
    }

    private HBox createChartsSection() {
        HBox chartsContainer = new HBox(20);
        chartsContainer.setAlignment(Pos.CENTER);

        // Graphique en barres - SAV par mois
        VBox savChart = createSAVChart();

        // Graphique en secteurs - Répartition équipements
        VBox equipmentChart = createEquipmentChart();

        chartsContainer.getChildren().addAll(savChart, equipmentChart);
        HBox.setHgrow(savChart, Priority.ALWAYS);
        HBox.setHgrow(equipmentChart, Priority.ALWAYS);

        return chartsContainer;
    }

    private VBox createSAVChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.getStyleClass().add("chart-container");

        Label chartTitle = new Label("📊 Évolution SAV - 6 derniers mois");
        chartTitle.getStyleClass().addAll("chart-title", "dashboard-sav-title");

        // Force programmatique du style pour éviter les overrides CSS
        Platform.runLater(() -> {
            chartTitle.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                    + "; -fx-text-fill: #6B71F2; " +
                    "-fx-padding: 8px 12px; -fx-background-radius: 4px;");
        });

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        savBarChart = new BarChart<>(xAxis, yAxis);

        savBarChart.setTitle("Nombre d'interventions");
        savBarChart.setPrefHeight(250);

        // Données initiales vides - seront chargées depuis l'API
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Interventions");
        savBarChart.getData().add(series);

        // Application des couleurs harmoniques au BarChart
        savBarChart.setAnimated(false);

        chartContainer.getChildren().addAll(chartTitle, savBarChart);

        // Forcer l'application des couleurs harmoniques
        String[] barColors = { "#6B71F2", "#F26BA6", "#A6F26B", "#6BF2A6", "#8A7DD3" };
        forceChartColors(savBarChart, barColors);

        return chartContainer;
    }

    private VBox createEquipmentChart() {
        VBox chartContainer = new VBox(10);
        chartContainer.getStyleClass().add("chart-container");

        Label chartTitle = new Label("🥧 Répartition Équipements");
        chartTitle.getStyleClass().addAll("chart-title", "dashboard-equipment-title");

        // Force programmatique du style pour éviter les overrides CSS
        Platform.runLater(() -> {
            chartTitle.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                    + "; -fx-text-fill: #6B71F2; " +
                    "-fx-padding: 8px 12px; -fx-background-radius: 4px;");
        });

        equipmentPieChart = new PieChart();
        equipmentPieChart.setPrefHeight(250);

        // Données initiales vides - seront chargées depuis l'API
        equipmentPieChart.setAnimated(false);

        chartContainer.getChildren().addAll(chartTitle, equipmentPieChart);

        return chartContainer;
    }

    private HBox createQuickActions() {
        HBox actionsContainer = new HBox(15);
        actionsContainer.setAlignment(Pos.CENTER);
        actionsContainer.getStyleClass().add("quick-actions");

        Label actionsTitle = new Label("⚡ Actions Rapides");
        actionsTitle.getStyleClass().add("section-title");

        VBox actionsWrapper = new VBox(10);
        actionsWrapper.getChildren().addAll(actionsTitle, actionsContainer);

        HBox wrapper = new HBox();
        wrapper.getChildren().add(actionsWrapper);
        return wrapper;
    }

    private HBox createFooterSection() {
        HBox footer = new HBox();
        footer.getStyleClass().add("dashboard-footer");
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setAlignment(Pos.CENTER_LEFT);

        Label statusLabel = new Label("✅ Système opérationnel");
        statusLabel.getStyleClass().add("status-ok");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lastUpdate = new Label(
                "🕐 Dernière mise à jour: " + java.time.LocalTime.now().toString().substring(0, 8));
        lastUpdate.getStyleClass().add("last-update");

        footer.getChildren().addAll(statusLabel, spacer, lastUpdate);
        return footer;
    }

    private void loadDashboardData() {
        System.out.println("📊 Chargement des données réelles du dashboard...");
        
        // Charger les statistiques globales
        dashboardService.getStats().thenAccept(stats -> {
            Platform.runLater(() -> {
                // Mettre à jour les cartes statistiques
                equipmentValueLabel.setText(formatNumber(stats.totalEquipment));
                savValueLabel.setText(String.valueOf(stats.activeSav));
                clientsValueLabel.setText(String.valueOf(stats.totalClients));
                vehiclesValueLabel.setText(String.valueOf(stats.totalVehicles));
                System.out.println("✅ Statistiques du dashboard mises à jour");
            });
        });
        
        // Charger les données SAV par mois
        dashboardService.getSavByMonth().thenAccept(monthlyData -> {
            Platform.runLater(() -> {
                updateSavChart(monthlyData);
                System.out.println("✅ Graphique SAV mis à jour avec " + monthlyData.size() + " mois");
            });
        });
        
        // Charger la répartition des équipements par catégorie
        dashboardService.getEquipmentByCategory().thenAccept(categoryData -> {
            Platform.runLater(() -> {
                updateEquipmentChart(categoryData);
                System.out.println("✅ Graphique équipements mis à jour avec " + categoryData.size() + " catégories");
            });
        });
    }
    
    private void updateSavChart(List<MonthlyData> monthlyData) {
        if (savBarChart == null || savBarChart.getData().isEmpty()) return;
        
        XYChart.Series<String, Number> series = savBarChart.getData().get(0);
        series.getData().clear();
        
        for (MonthlyData data : monthlyData) {
            series.getData().add(new XYChart.Data<>(data.month, data.count));
        }
        
        // Appliquer les couleurs après mise à jour
        String[] barColors = { "#6B71F2", "#F26BA6", "#A6F26B", "#6BF2A6", "#8A7DD3" };
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(e -> {
            savBarChart.lookupAll(".chart-bar").forEach(node -> {
                node.setStyle("-fx-bar-fill: #6B71F2; -fx-background-color: #6B71F2;");
            });
        });
        pause.play();
    }
    
    private void updateEquipmentChart(List<CategoryData> categoryData) {
        if (equipmentPieChart == null) return;
        
        equipmentPieChart.getData().clear();
        
        String[] harmonicColors = { "#6B71F2", "#F26BA6", "#A6F26B", "#6BF2A6", "#8A7DD3", "#F2A66B", "#6BA6F2" };
        
        for (int i = 0; i < categoryData.size(); i++) {
            CategoryData data = categoryData.get(i);
            PieChart.Data pieData = new PieChart.Data(data.category + " (" + data.count + ")", data.count);
            equipmentPieChart.getData().add(pieData);
            
            // Appliquer la couleur
            final int colorIndex = i % harmonicColors.length;
            pieData.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + harmonicColors[colorIndex] + ";");
                }
            });
        }
        
        // Styliser les labels
        stylePieChartLabels(equipmentPieChart);
    }
    
    private String formatNumber(long number) {
        if (number >= 1000) {
            return String.format("%,d", number).replace(",", " ");
        }
        return String.valueOf(number);
    }

    private void forceChartColors(javafx.scene.Node chart, String[] colors) {
        // Méthode pour forcer l'application des couleurs harmoniques
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(event -> {
            chart.applyCss();
            chart.autosize();

            // Forcer les couleurs sur tous les nœuds trouvés
            for (int i = 0; i < colors.length; i++) {
                final String color = colors[i];
                final int index = i;

                // Chercher tous les types de nœuds possibles
                chart.lookupAll("*").forEach(node -> {
                    String styleClass = node.getStyleClass().toString();
                    if (styleClass.contains("default-color" + index)) {
                        String style = "-fx-background-color: " + color +
                                "; -fx-bar-fill: " + color +
                                "; -fx-pie-color: " + color + ";";
                        node.setStyle(style);
                        System.out.println("🎨 Couleur appliquée à " + node.getClass().getSimpleName() +
                                " (color" + index + "): " + color);
                    }
                });
            }
        });
        pause.play();
    }

    private void stylePieChartLabels(PieChart pieChart) {
        // Styliser les labels du camembert en #6B71F2
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        pause.setOnFinished(event -> {
            pieChart.applyCss();

            // Styliser TOUS les éléments texte du PieChart
            pieChart.lookupAll("Text").forEach(node -> {
                if (node instanceof javafx.scene.text.Text) {
                    javafx.scene.text.Text textNode = (javafx.scene.text.Text) node;
                    textNode.setFill(javafx.scene.paint.Color.web("#6B71F2"));
                    System.out.println("📝 Texte du camembert stylisé: " + textNode.getText());
                }
            });

            // Styliser les labels spécifiques
            pieChart.lookupAll(".chart-pie-label").forEach(node -> {
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                System.out.println("🏷️ Label de camembert stylisé en #6B71F2");
            });

            // Styliser les lignes de connexion des labels
            pieChart.lookupAll(".chart-pie-label-line").forEach(node -> {
                node.setStyle("-fx-stroke: #6B71F2 !important;");
                System.out.println("📏 Ligne de label stylisée en #6B71F2");
            });

            // Approche alternative pour les textes
            pieChart.lookupAll(".text").forEach(node -> {
                node.setStyle("-fx-fill: #6B71F2 !important; -fx-text-fill: #6B71F2 !important;");
                System.out.println("📄 Élément text stylisé en #6B71F2");
            });

            // Forcer sur tous les enfants récursivement
            stylePieChartChildrenRecursively(pieChart);
        });
    }

    private void stylePieChartChildrenRecursively(javafx.scene.Node node) {
        if (node instanceof javafx.scene.text.Text) {
            javafx.scene.text.Text textNode = (javafx.scene.text.Text) node;
            try {
                // Essayer de modifier la couleur via setFill si possible
                if (!textNode.fillProperty().isBound()) {
                    textNode.setFill(javafx.scene.paint.Color.web("#6B71F2"));
                    System.out.println("🔤 Texte récursif stylisé via Fill: " + textNode.getText());
                } else {
                    // Sinon utiliser le style CSS
                    textNode.setStyle("-fx-fill: #6B71F2 !important;");
                    System.out.println("🎨 Texte récursif stylisé via CSS: " + textNode.getText());
                }
            } catch (Exception e) {
                // En cas d'erreur, utiliser uniquement le style CSS
                textNode.setStyle("-fx-fill: #6B71F2 !important;");
                System.out.println("⚠️ Texte stylisé via CSS (fallback): " + textNode.getText());
            }
        }

        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                stylePieChartChildrenRecursively(child);
            }
        }
    }

    /**
     * Méthode pour rafraîchir les données du dashboard
     */
    public void refreshData() {
        System.out.println("🔄 Rafraîchissement du dashboard...");
        loadDashboardData();
    }
}
