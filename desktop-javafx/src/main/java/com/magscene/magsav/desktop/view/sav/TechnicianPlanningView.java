package com.magscene.magsav.desktop.view.sav;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import javafx.concurrent.Task;

import com.magscene.magsav.desktop.model.ServiceRequest;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.util.AlertUtil;
import com.magscene.magsav.desktop.util.ViewUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Interface de planning intelligent pour les techniciens SAV
 * Optimisation automatique des tournées et gestion des compétences
 */
public class TechnicianPlanningView extends VBox {
    
    private final ApiService apiService;
    private final ObservableList<TechnicianSchedule> schedules;
    private final TableView<TechnicianSchedule> planningTable;
    private final DatePicker planningDate;
    private final ComboBox<String> technicianFilter;
    private final TextArea optimizationSummaryArea;
    
    // Zones géographiques et distance estimées
    private final Map<String, Double> zoneDistances;
    private final Map<String, List<String>> technicianSkills;
    
    public TechnicianPlanningView() {
        this.apiService = new ApiService();
        this.schedules = FXCollections.observableArrayList();
        this.zoneDistances = new HashMap<>();
        this.technicianSkills = new HashMap<>();
        
        // Configuration principale
        this.setSpacing(0); // AUCUN ESPACEMENT comme Ventes et Installations
        this.setPadding(new Insets(5)); // Padding minimal
        this.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Initialisation des composants
        this.planningDate = new DatePicker(LocalDate.now());
        this.technicianFilter = new ComboBox<>();
        this.planningTable = createPlanningTable();
        this.optimizationSummaryArea = new TextArea();
        
        // Initialisation des données de référence
        initializeReferenceData();
        
        // Construction de l'interface
        setupPlanningInterface();
        setupPlanningEventHandlers();
        
        // Chargement initial du planning
        loadTechnicianPlanning();
    }
    
    private void initializeReferenceData() {
        // Zones géographiques et distances approximatives (en km)
        zoneDistances.put("Centre-ville Lyon", 0.0);
        zoneDistances.put("Villeurbanne", 8.5);
        zoneDistances.put("Vénissieux", 12.3);
        zoneDistances.put("Bron", 15.7);
        zoneDistances.put("Décines", 18.4);
        zoneDistances.put("Meyzieu", 22.1);
        zoneDistances.put("Saint-Priest", 16.8);
        zoneDistances.put("Chassieu", 20.2);
        
        // Compétences des techniciens (simulation)
        technicianSkills.put("Marc Dupont", List.of("Audio", "Vidéo", "Éclairage", "Réseau"));
        technicianSkills.put("Sophie Martin", List.of("Éclairage", "Mécanique", "Électronique"));
        technicianSkills.put("Thomas Leroux", List.of("Vidéo", "Projection", "Calibrage", "Réseau"));
        technicianSkills.put("Julie Moreau", List.of("Audio", "Sonorisation", "HF", "Enregistrement"));
        technicianSkills.put("David Rousseau", List.of("Mécanique", "Levage", "Sécurité", "Installation"));
    }
    
    private void setupPlanningInterface() {
        // Toolbar unifiée avec tous les contrôles
        HBox toolbar = createUnifiedToolbar();
        
        // Section principale avec tableau et optimisation
        HBox mainSection = createPlanningMainSection();
        
        this.getChildren().addAll(toolbar, mainSection);
        VBox.setVgrow(mainSection, Priority.ALWAYS);
    }
    
    private HBox createUnifiedToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle(
            "-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #8B91FF; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 3);");
        
        // Filtre Date avec navigation
        VBox dateBox = new VBox(3);
        Label dateLabel = new Label("📅 Date");
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StandardColors.getTextColor() + ";");
        
        HBox dateNav = new HBox(3);
        Button prevDay = new Button("◀");
        prevDay.setOnAction(e -> { planningDate.setValue(planningDate.getValue().minusDays(1)); loadTechnicianPlanning(); });
        Button today = new Button("Auj.");
        today.setOnAction(e -> { planningDate.setValue(LocalDate.now()); loadTechnicianPlanning(); });
        Button nextDay = new Button("▶");
        nextDay.setOnAction(e -> { planningDate.setValue(planningDate.getValue().plusDays(1)); loadTechnicianPlanning(); });
        dateNav.getChildren().addAll(prevDay, planningDate, nextDay, today);
        dateBox.getChildren().addAll(dateLabel, dateNav);
        
        // Filtre Technicien
        VBox techBox = ViewUtils.createFilterBox("👤 Technicien", 
            new String[]{"Tous les techniciens", "Marc Dupont", "Sophie Martin", "Thomas Leroux", "Julie Moreau", "David Rousseau"}, 
            "Tous les techniciens", value -> loadTechnicianPlanning());
        if (techBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) techBox.getChildren().get(1);
            technicianFilter.getItems().setAll(combo.getItems());
            technicianFilter.setValue(combo.getValue());
            combo.valueProperty().bindBidirectional(technicianFilter.valueProperty());
        }
        
        // Boutons d'optimisation
        Button optimizeBtn = new Button("🗺️ Optimiser trajets");
        optimizeBtn.getStyleClass().add("btn-primary");
        optimizeBtn.setOnAction(e -> optimizeRoutes());
        
        Button autoAssignBtn = new Button("🎯 Auto-assignation");
        autoAssignBtn.getStyleClass().add("btn-secondary");
        autoAssignBtn.setOnAction(e -> autoAssignTechnicians());
        
        Button balanceBtn = new Button("⚖️ Équilibrer");
        balanceBtn.getStyleClass().add("btn-secondary");
        balanceBtn.setOnAction(e -> balanceWorkload());
        
        Button refreshBtn = ViewUtils.createRefreshButton("🔄 Actualiser", this::loadTechnicianPlanning);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(dateBox, techBox, optimizeBtn, autoAssignBtn, balanceBtn, spacer, refreshBtn);
        return toolbar;
    }
    
    private HBox createPlanningHeaderSection() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 5, 0)); // Padding minimal
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Métriques du planning
        VBox metricsBox = new VBox(3);
        metricsBox.setAlignment(Pos.CENTER_RIGHT);
        metricsBox.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 8px 12px; -fx-background-radius: 6px;");
        
        Label efficiencyLabel = new Label("🎯 Efficacité tournée: 0%");
        efficiencyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StandardColors.SUCCESS_GREEN + ";");
        
        Label distanceLabel = new Label("🚗 Distance totale: 0 km");
        distanceLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + StandardColors.DANGER_RED + ";");
        
        metricsBox.getChildren().addAll(efficiencyLabel, distanceLabel);
        
        headerBox.getChildren().addAll(spacer, metricsBox);
        return headerBox;
    }
    
    private VBox createPlanningControlSection() {
        VBox controlSection = new VBox(10);
        controlSection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(41,128,185,0.3), 6, 0, 0, 2);");
        
        Label controlTitle = new Label("🎛️ Contrôles de Planning");
        controlTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + StandardColors.PRIMARY_BLUE + ";");
        
        // Ligne de sélection de date et technicien
        HBox selectionBox = new HBox(20);
        selectionBox.setAlignment(Pos.CENTER_LEFT);
        
        // Sélection de date avec navigation rapide
        VBox dateBox = new VBox(3);
        Label dateLabel = new Label("📅 Date de planning :");
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StandardColors.getTextColor() + ";");
        
        HBox dateNavBox = new HBox(5);
        dateNavBox.setAlignment(Pos.CENTER_LEFT);
        
        Button prevDayBtn = new Button("◀");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        prevDayBtn.setOnAction(e -> {
            planningDate.setValue(planningDate.getValue().minusDays(1));
            loadTechnicianPlanning();
        });
        
        planningDate.setStyle("-fx-background-radius: 4px; -fx-border-color: " + StandardColors.PRIMARY_BLUE + "; -fx-border-radius: 4px;");
        
        Button nextDayBtn = new Button("▶");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        nextDayBtn.setOnAction(e -> {
            planningDate.setValue(planningDate.getValue().plusDays(1));
            loadTechnicianPlanning();
        });
        
        Button todayBtn = new Button("Aujourd'hui");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        todayBtn.setOnAction(e -> {
            planningDate.setValue(LocalDate.now());
            loadTechnicianPlanning();
        });
        
        dateNavBox.getChildren().addAll(prevDayBtn, planningDate, nextDayBtn, todayBtn);
        dateBox.getChildren().addAll(dateLabel, dateNavBox);
        
        // Filtre par technicien
        VBox technicianBox = new VBox(3);
        Label technicianLabel = new Label("👤 Technicien :");
        technicianLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StandardColors.getTextColor() + ";");
        
        technicianFilter.getItems().addAll("Tous les techniciens", "Marc Dupont", "Sophie Martin", "Thomas Leroux", "Julie Moreau", "David Rousseau");
        technicianFilter.setValue("Tous les techniciens");
        technicianFilter.setStyle("-fx-background-radius: 4px; -fx-border-color: " + StandardColors.PRIMARY_BLUE + "; -fx-border-radius: 4px;");
        technicianFilter.setPrefWidth(180);
        
        technicianBox.getChildren().addAll(technicianLabel, technicianFilter);
        
        // Actions d'optimisation
        VBox optimizationBox = new VBox(3);
        Label optimizationLabel = new Label("🚀 Optimisation automatique :");
        optimizationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + StandardColors.getTextColor() + ";");
        
        HBox optimizationBtns = new HBox(5);
        
        Button optimizeRoutesBtn = new Button("🗺️ Optimiser trajets");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        optimizeRoutesBtn.setOnAction(e -> optimizeRoutes());
        
        Button autoAssignBtn = new Button("🎯 Assignation auto");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        autoAssignBtn.setOnAction(e -> autoAssignTechnicians());
        
        Button balanceWorkloadBtn = new Button("⚖️ Équilibrer charge");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        balanceWorkloadBtn.setOnAction(e -> balanceWorkload());
        
        optimizationBtns.getChildren().addAll(optimizeRoutesBtn, autoAssignBtn, balanceWorkloadBtn);
        optimizationBox.getChildren().addAll(optimizationLabel, optimizationBtns);
        
        selectionBox.getChildren().addAll(dateBox, technicianBox, optimizationBox);
        
        controlSection.getChildren().addAll(controlTitle, selectionBox);
        return controlSection;
    }
    
    private HBox createPlanningMainSection() {
        HBox mainSection = new HBox(15);
        mainSection.setAlignment(Pos.TOP_LEFT);
        
        // Tableau du planning (65% de la largeur)
        VBox tableSection = createPlanningTableSection();
        
        // Panneau d'optimisation et résumé (35% de la largeur)
        VBox summarySection = createOptimizationSummarySection();
        
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        summarySection.setPrefWidth(350);
        
        mainSection.getChildren().addAll(tableSection, summarySection);
        return mainSection;
    }
    
    private VBox createPlanningTableSection() {
        VBox tableSection = new VBox(10);
        tableSection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 20px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        Label tableTitle = new Label("📋 Planning du Jour");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + StandardColors.getTextColor() + ";");
        
        planningTable.setPrefHeight(400);
        planningTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        tableSection.getChildren().addAll(tableTitle, planningTable);
        VBox.setVgrow(planningTable, Priority.ALWAYS);
        
        return tableSection;
    }
    
    private VBox createOptimizationSummarySection() {
        VBox summarySection = new VBox(15);
        summarySection.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(41,128,185,0.3), 6, 0, 0, 2);");
        
        Label summaryTitle = new Label("📊 Analyse & Optimisation");
        summaryTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + StandardColors.PRIMARY_BLUE + ";");
        
        // Indicateurs clés de performance
        GridPane kpiGrid = createKPIGrid();
        
        // Zone de résumé d'optimisation
        Label analysisLabel = new Label("🔍 Analyse des tournées");
        analysisLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + StandardColors.getTextColor() + ";");
        
        optimizationSummaryArea.setPrefHeight(200);
        optimizationSummaryArea.setEditable(false);
        optimizationSummaryArea.setWrapText(true);
        optimizationSummaryArea.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-border-color: " + StandardColors.PRIMARY_BLUE + "; -fx-border-radius: 4px; -fx-font-family: 'Segoe UI'; -fx-font-size: 11px;");
        optimizationSummaryArea.setPromptText("Cliquez sur 'Optimiser trajets' pour analyser le planning...");
        
        // Recommandations intelligentes
        VBox recommendationsBox = createRecommendationsBox();
        
        summarySection.getChildren().addAll(summaryTitle, kpiGrid, analysisLabel, optimizationSummaryArea, recommendationsBox);
        VBox.setVgrow(optimizationSummaryArea, Priority.ALWAYS);
        
        return summarySection;
    }
    
    private GridPane createKPIGrid() {
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(10);
        kpiGrid.setVgap(8);
        // kpiGrid supprimé - Style géré par CSS
        Label[] kpiLabels = {
            new Label("📍 Interventions:"),
            new Label("🚗 Distance totale:"),
            new Label("⏱️ Temps moyen:"),
            new Label("🎯 Taux d'occupation:")
        };
        
        Label[] kpiValues = {
            new Label("0"), // interventions
            new Label("0 km"), // distance
            new Label("0h"), // temps moyen
            new Label("0%") // taux d'occupation
        };
        
        for (int i = 0; i < kpiLabels.length; i++) {
            kpiLabels[i].setStyle("-fx-font-size: 10px; -fx-text-fill: " + StandardColors.NEUTRAL_GRAY + ";");
            kpiValues[i].setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + StandardColors.getTextColor() + ";");
            
            kpiGrid.add(kpiLabels[i], 0, i);
            kpiGrid.add(kpiValues[i], 1, i);
        }
        
        return kpiGrid;
    }
    
    private VBox createRecommendationsBox() {
        VBox recommendationsBox = new VBox(5);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        Label recommendationsTitle = new Label("💡 Recommandations");
        recommendationsTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + StandardColors.SUCCESS_GREEN + ";");
        
        Label recommendation1 = new Label("• Optimiser l'ordre des interventions");
        Label recommendation2 = new Label("• Vérifier les compétences requises");
        Label recommendation3 = new Label("• Considérer le trafic en temps réel");
        
        recommendation1.setStyle("-fx-font-size: 10px; -fx-text-fill: " + StandardColors.SUCCESS_GREEN + ";");
        recommendation2.setStyle("-fx-font-size: 10px; -fx-text-fill: " + StandardColors.SUCCESS_GREEN + ";");
        recommendation3.setStyle("-fx-font-size: 10px; -fx-text-fill: " + StandardColors.SUCCESS_GREEN + ";");
        
        recommendationsBox.getChildren().addAll(recommendationsTitle, recommendation1, recommendation2, recommendation3);
        return recommendationsBox;
    }
    
    private HBox createPlanningActionsBar() {
        HBox actionsBar = new HBox(10);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(15, 0, 0, 0));
        
        Button scheduleInterventionBtn = new Button("📅 Planifier intervention");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        Button manageAvailabilityBtn = new Button("🕐 Gérer disponibilités");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        Button exportPlanningBtn = new Button("📋 Exporter planning");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        Button sendNotificationsBtn = new Button("📱 Notifier techniciens");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        Button refreshBtn = new Button("🔄 Actualiser");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        refreshBtn.setOnAction(e -> loadTechnicianPlanning());
        
        actionsBar.getChildren().addAll(scheduleInterventionBtn, manageAvailabilityBtn, exportPlanningBtn, sendNotificationsBtn, refreshBtn);
        return actionsBar;
    }
    
    private TableView<TechnicianSchedule> createPlanningTable() {
        TableView<TechnicianSchedule> table = new TableView<>();
        table.setItems(schedules);
        
        // Colonne Technicien avec indicateur de compétences
        TableColumn<TechnicianSchedule, String> technicianCol = new TableColumn<>("Technicien");
        technicianCol.setPrefWidth(120);
        technicianCol.setCellValueFactory(data -> {
            String name = data.getValue().getTechnicianName();
            int skillCount = technicianSkills.getOrDefault(name, List.of()).size();
            return new javafx.beans.property.SimpleStringProperty("👤 " + name + " (" + skillCount + " compétences)");
        });
        
        // Colonne Heure avec indicateur de ponctualité
        TableColumn<TechnicianSchedule, String> timeCol = new TableColumn<>("Heure");
        timeCol.setPrefWidth(80);
        timeCol.setCellValueFactory(data -> {
            LocalTime time = data.getValue().getScheduledTime();
            String status = data.getValue().getStatus();
            String icon = getTimeStatusIcon(status);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + time.format(DateTimeFormatter.ofPattern("HH:mm")));
        });
        
        // Colonne Intervention avec priorité
        TableColumn<TechnicianSchedule, String> interventionCol = new TableColumn<>("Intervention");
        interventionCol.setPrefWidth(200);
        interventionCol.setCellValueFactory(data -> {
            String title = data.getValue().getInterventionTitle();
            String priority = data.getValue().getPriority();
            String priorityIcon = getPriorityIcon(priority);
            return new javafx.beans.property.SimpleStringProperty(priorityIcon + " " + title);
        });
        
        // Colonne Lieu avec distance
        TableColumn<TechnicianSchedule, String> locationCol = new TableColumn<>("Lieu");
        locationCol.setPrefWidth(130);
        locationCol.setCellValueFactory(data -> {
            String location = data.getValue().getLocation();
            Double distance = zoneDistances.getOrDefault(location, 0.0);
            return new javafx.beans.property.SimpleStringProperty("📍 " + location + " (" + String.format("%.1f km", distance) + ")");
        });
        
        // Colonne Durée estimée
        TableColumn<TechnicianSchedule, String> durationCol = new TableColumn<>("Durée");
        durationCol.setPrefWidth(70);
        durationCol.setCellValueFactory(data -> {
            int duration = data.getValue().getEstimatedDuration();
            return new javafx.beans.property.SimpleStringProperty(duration + "min");
        });
        
        // Colonne Statut avec couleur
        TableColumn<TechnicianSchedule, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(100);
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getStatus();
            String icon = getStatusIcon(status);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + status);
        });
        
        // Colonne Compétences requises
        TableColumn<TechnicianSchedule, String> skillsCol = new TableColumn<>("Compétences");
        skillsCol.setPrefWidth(120);
        skillsCol.setCellValueFactory(data -> {
            List<String> required = data.getValue().getRequiredSkills();
            List<String> available = technicianSkills.getOrDefault(data.getValue().getTechnicianName(), List.of());
            
            long matchCount = required.stream().mapToLong(skill -> available.contains(skill) ? 1 : 0).sum();
            String matchIcon = matchCount == required.size() ? "✅" : "⚠️";
            
            return new javafx.beans.property.SimpleStringProperty(matchIcon + " " + matchCount + "/" + required.size());
        });
        
        // Ajout individuel des colonnes pour éviter les warnings de generic array
        table.getColumns().add(technicianCol);
        table.getColumns().add(timeCol);
        table.getColumns().add(interventionCol);
        table.getColumns().add(locationCol);
        table.getColumns().add(durationCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(skillsCol);
        
        // Style conditionnel des lignes
        table.setRowFactory(tv -> {
            TableRow<TechnicianSchedule> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty() || row.getItem() == null) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme
                    row.setStyle("-fx-background-color: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                } else {
                    // Style basé sur le statut et la priorité
                    TechnicianSchedule item = row.getItem();
                    String backgroundColor = getScheduleRowBackgroundColor(item.getStatus(), item.getPriority());
                    row.setStyle(backgroundColor + "; -fx-border-color: " + StandardColors.NEUTRAL_LIGHT + "; -fx-border-width: 0 0 1 0;");
                }
            };
            
            // Écouter les changements de sélection et d'item
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            return row;
        });
        
        return table;
    }
    
    private void setupPlanningEventHandlers() {
        // Gestionnaire de changement de date
        planningDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) {
                loadTechnicianPlanning();
            }
        });
        
        // Gestionnaire de filtre technicien
        technicianFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyPlanningFilters());
        
        // Gestionnaire de sélection dans le tableau
        planningTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayScheduleDetails(newSelection);
            }
        });
    }
    
    private void applyPlanningFilters() {
        // Implémentation du filtrage du planning
        String selectedTechnician = technicianFilter.getValue();
        // Logique de filtrage basée sur le technicien sélectionné
    }
    
    private void displayScheduleDetails(TechnicianSchedule schedule) {
        // Afficher les détails de l'intervention sélectionnée; // Cette méthode pourrait ouvrir un dialogue détaillé ou mettre à jour une zone d'information
    }
    
    private void optimizeRoutes() {
        // Algorithme d'optimisation des trajets
        optimizationSummaryArea.setText("🔍 ANALYSE DES TRAJETS EN COURS...\n\n");
        
        Task<String> optimizationTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                Thread.sleep(2000); // Simulation du calcul
                
                StringBuilder analysis = new StringBuilder();
                analysis.append("🗺️ OPTIMISATION DES TRAJETS TERMINÉE\n\n");
                analysis.append("📊 Résultats de l'analyse:\n");
                analysis.append("• Distance totale actuelle: 127.5 km\n");
                analysis.append("• Distance optimisée: 89.2 km (-30%)\n");
                analysis.append("• Temps de trajet économisé: 1h 15min\n");
                analysis.append("• Carburant économisé: ~15.2L\n\n");
                
                analysis.append("🎯 Recommandations d'optimisation:\n");
                analysis.append("1. Regrouper interventions Villeurbanne (9h-11h)\n");
                analysis.append("2. Reporter intervention Meyzieu à 14h30\n");
                analysis.append("3. Échanger Marc/Sophie pour secteur Bron\n");
                analysis.append("4. Prévoir pause déjeuner à Décines\n\n");
                
                analysis.append("⚠️ Points d'attention:\n");
                analysis.append("• Trafic dense prévu 17h-18h secteur Centre\n");
                analysis.append("• Compétence spécialisée requise intervention 15h\n");
                analysis.append("• Client prioritaire à traiter en premier (urgence)\n");
                
                return analysis.toString();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    optimizationSummaryArea.setText(getValue());
                    updatePlanningMetrics();
                });
            }
        };
        
        Thread optimizationThread = new Thread(optimizationTask);
        optimizationThread.setDaemon(true);
        optimizationThread.start();
    }
    
    private void autoAssignTechnicians() {
        // Assignation automatique basée sur les compétences et la charge de travail
        AlertUtil.showInfo("Assignation Automatique", 
            "Algorithme d'assignation basé sur:\n" +
            "• Compétences techniques requises\n" +
            "• Charge de travail actuelle\n" +
            "• Proximité géographique\n" +
            "• Historique de performance\n\n" +
            "3 réassignations optimales proposées");
    }
    
    private void balanceWorkload() {
        // Équilibrage de la charge de travail entre techniciens
        AlertUtil.showInfo("Équilibrage de Charge", 
            "Répartition optimale de la charge:\n" +
            "• Marc Dupont: 6h30 → 7h15 (+12%)\n" +
            "• Sophie Martin: 8h45 → 7h00 (-20%)\n" +
            "• Thomas Leroux: 5h15 → 6h30 (+24%)\n" +
            "• Julie Moreau: 7h30 → 7h45 (+3%)\n\n" +
            "Écart-type réduit de 1h23 à 28min");
    }
    
    private void updatePlanningMetrics() {
        // Mise à jour des métriques affichées dans l'interface; // Cette méthode met à jour les KPIs et indicateurs
    }
    
    private void loadTechnicianPlanning() {
        // Chargement des données de planning pour la date sélectionnée
        schedules.clear();
        
        // Simulation de données de planning
        LocalDate selectedDate = planningDate.getValue();
        
        schedules.addAll(
            new TechnicianSchedule("Marc Dupont", LocalTime.of(9, 0), "Réparation console audio MX-24", 
                                 "Centre-ville Lyon", 120, "Planifié", "Élevée", 
                                 List.of("Audio", "Électronique")),
            new TechnicianSchedule("Sophie Martin", LocalTime.of(9, 30), "Maintenance éclairage LED", 
                                 "Villeurbanne", 90, "En cours", "Moyenne", 
                                 List.of("Éclairage", "Mécanique")),
            new TechnicianSchedule("Thomas Leroux", LocalTime.of(10, 15), "Installation vidéo-projection", 
                                 "Bron", 180, "Planifié", "Urgente", 
                                 List.of("Vidéo", "Installation")),
            new TechnicianSchedule("Julie Moreau", LocalTime.of(11, 0), "Calibrage système sonorisation", 
                                 "Vénissieux", 150, "Planifié", "Moyenne", 
                                 List.of("Audio", "Calibrage")),
            new TechnicianSchedule("David Rousseau", LocalTime.of(14, 0), "Levage et installation structure", 
                                 "Saint-Priest", 240, "Planifié", "Élevée", 
                                 List.of("Mécanique", "Levage", "Sécurité"))
        );
        
        updatePlanningMetrics();
    }
    
    // Méthodes utilitaires pour les icônes et couleurs
    private String getPriorityIcon(String priority) {
        switch (priority.toLowerCase()) {
            case "faible": return "🟢";
            case "moyenne": return "🟡";
            case "élevée": return "🟠";
            case "urgente": return "🔴";
            default: return "⚪";
        }
    }
    
    private String getStatusIcon(String status) {
        switch (status.toLowerCase()) {
            case "planifié": return "📋";
            case "en cours": return "⚙️";
            case "terminé": return "✅";
            case "reporté": return "⏸️";
            case "annulé": return "❌";
            default: return "❓";
        }
    }
    
    private String getTimeStatusIcon(String status) {
        switch (status.toLowerCase()) {
            case "en avance": return "🟢";
            case "à l'heure": return "🟡";
            case "en retard": return "🔴";
            default: return "🕐";
        }
    }
    
    private String getScheduleRowBackgroundColor(String status, String priority) {
        if (priority.equals("Urgente")) {
            return "-fx-background-color: " + StandardColors.DANGER_LIGHT;
        } else if (status.equals("En cours")) {
            return "-fx-background-color: " + StandardColors.WARNING_LIGHT;
        } else if (status.equals("Terminé")) {
            return "-fx-background-color: " + StandardColors.SUCCESS_LIGHT;
        }
        return "-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor();
    }
    
    /**
     * Classe interne pour représenter un créneau de planning technicien
     */
    public static class TechnicianSchedule {
        private String technicianName;
        private LocalTime scheduledTime;
        private String interventionTitle;
        private String location;
        private int estimatedDuration;
        private String status;
        private String priority;
        private List<String> requiredSkills;
        
        public TechnicianSchedule(String technicianName, LocalTime scheduledTime, String interventionTitle,
                                String location, int estimatedDuration, String status, String priority,
                                List<String> requiredSkills) {
            this.technicianName = technicianName;
            this.scheduledTime = scheduledTime;
            this.interventionTitle = interventionTitle;
            this.location = location;
            this.estimatedDuration = estimatedDuration;
            this.status = status;
            this.priority = priority;
            this.requiredSkills = requiredSkills;
        }
        
        // Getters
        public String getTechnicianName() { return technicianName; }
        public LocalTime getScheduledTime() { return scheduledTime; }
        public String getInterventionTitle() { return interventionTitle; }
        public String getLocation() { return location; }
        public int getEstimatedDuration() { return estimatedDuration; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
        public List<String> getRequiredSkills() { return requiredSkills; }
        
        // Setters
        public void setTechnicianName(String technicianName) { this.technicianName = technicianName; }
        public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }
        public void setInterventionTitle(String interventionTitle) { this.interventionTitle = interventionTitle; }
        public void setLocation(String location) { this.location = location; }
        public void setEstimatedDuration(int estimatedDuration) { this.estimatedDuration = estimatedDuration; }
        public void setStatus(String status) { this.status = status; }
        public void setPriority(String priority) { this.priority = priority; }
        public void setRequiredSkills(List<String> requiredSkills) { this.requiredSkills = requiredSkills; }
    }
}
