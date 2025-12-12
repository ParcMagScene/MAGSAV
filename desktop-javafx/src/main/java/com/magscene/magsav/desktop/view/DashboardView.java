package com.magscene.magsav.desktop.view;

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

/**
 * Dashboard principal de MAGSAV-3.0
 * Vue d'ensemble avec statistiques et informations clés
 */
public class DashboardView extends BorderPane {

    public DashboardView() {
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

        // Carte Équipements
        VBox equipmentCard = createStatsCard("📦", "Équipements", "1,247", "Total en parc");

        // Carte SAV
        VBox savCard = createStatsCard("🔧", "SAV Actifs", "23", "En cours");

        // Carte Clients
        VBox clientsCard = createStatsCard("👥", "Clients", "89", "Actifs");

        // Carte Véhicules
        VBox vehiclesCard = createStatsCard("🚐", "Véhicules", "12", "Flotte");

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
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);

        barChart.setTitle("Nombre d'interventions");
        barChart.setPrefHeight(250);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Interventions");

        // Données simulées
        series.getData().add(new XYChart.Data<>("Mai", 15));
        series.getData().add(new XYChart.Data<>("Juin", 22));
        series.getData().add(new XYChart.Data<>("Juillet", 18));
        series.getData().add(new XYChart.Data<>("Août", 28));
        series.getData().add(new XYChart.Data<>("Sept", 25));
        series.getData().add(new XYChart.Data<>("Oct", 23));

        barChart.getData().add(series);

        // Application des couleurs harmoniques au BarChart
        barChart.setAnimated(false);

        // Appliquer les couleurs après rendu
        javafx.application.Platform.runLater(() -> {
            barChart.lookupAll(".chart-bar").forEach(node -> {
                node.setStyle("-fx-bar-fill: #6B71F2; -fx-background-color: #6B71F2;");
            });
            barChart.lookupAll(".default-color0").forEach(node -> {
                node.setStyle("-fx-bar-fill: #6B71F2; -fx-background-color: #6B71F2;");
            });
        });

        chartContainer.getChildren().addAll(chartTitle, barChart);

        // Forcer l'application des couleurs harmoniques
        String[] barColors = { "#6B71F2", "#F26BA6", "#A6F26B", "#6BF2A6", "#8A7DD3" };
        forceChartColors(barChart, barColors);

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

        PieChart pieChart = new PieChart();
        pieChart.setPrefHeight(250);

        // Données simulées
        PieChart.Data audioData = new PieChart.Data("Audio", 35);
        PieChart.Data videoData = new PieChart.Data("Vidéo", 25);
        PieChart.Data eclairageData = new PieChart.Data("Éclairage", 20);
        PieChart.Data structureData = new PieChart.Data("Structure", 15);
        PieChart.Data autresData = new PieChart.Data("Autres", 5);

        pieChart.getData().addAll(audioData, videoData, eclairageData, structureData, autresData);

        // Application des couleurs harmoniques au PieChart
        String[] harmonicColors = { "#6B71F2", "#F26BA6", "#A6F26B", "#6BF2A6", "#8A7DD3" };
        pieChart.setAnimated(false);

        // Appliquer les couleurs directement aux données
        for (int i = 0; i < pieChart.getData().size(); i++) {
            final int colorIndex = i;
            PieChart.Data data = pieChart.getData().get(i);

            // Ajouter un listener pour appliquer le style au nœud
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + harmonicColors[colorIndex] +
                            "; -fx-background-color: " + harmonicColors[colorIndex] + ";");
                }
            });
        }

        // Appliquer aussi après rendu complet
        javafx.application.Platform.runLater(() -> {
            for (int i = 0; i < pieChart.getData().size(); i++) {
                final int colorIndex = i;
                pieChart.lookupAll(".default-color" + i).forEach(node -> {
                    node.setStyle("-fx-pie-color: " + harmonicColors[colorIndex] +
                            "; -fx-background-color: " + harmonicColors[colorIndex] + ";");
                });
                pieChart.lookupAll(".chart-pie").forEach(node -> {
                    if (node.getStyleClass().contains("default-color" + colorIndex)) {
                        node.setStyle("-fx-pie-color: " + harmonicColors[colorIndex] +
                                "; -fx-background-color: " + harmonicColors[colorIndex] + ";");
                    }
                });
            }
        });

        chartContainer.getChildren().addAll(chartTitle, pieChart);

        // Forcer l'application des couleurs harmoniques pour le PieChart
        forceChartColors(pieChart, harmonicColors);

        // Styliser les labels du camembert
        stylePieChartLabels(pieChart);

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
        // Simulation du chargement des données en arrière-plan; // Dans une vraie
        // implémentation, ceci ferait appel aux services appropriés
        System.out.println("📊 Chargement des données du dashboard...");
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
        loadDashboardData();
        System.out.println("🔄 Dashboard rafraîchi");
    }
}
