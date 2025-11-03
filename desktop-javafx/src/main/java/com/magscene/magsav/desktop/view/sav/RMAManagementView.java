package com.magscene.magsav.desktop.view.sav;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.concurrent.Task;

import com.magscene.magsav.desktop.model.ServiceRequest;
import com.magscene.magsav.desktop.model.Equipment;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.util.AlertUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface de gestion des RMA (Return Merchandise Authorization)
 * Permet la gestion complète des retours matériel avec traçabilité
 */
public class RMAManagementView extends VBox {
    
    private final ApiService apiService;
    private final ObservableList<RMARecord> rmaRecords;
    private final TableView<RMARecord> rmaTable;
    private final TextArea rmaDetailsArea;
    
    // Filtres spécifiques aux RMA
    private final ComboBox<String> rmaStatusFilter;
    private final ComboBox<String> rmaTypeFilter;
    private final TextField rmaSearchField;
    private final DatePicker rmaDateFrom;
    private final DatePicker rmaDateTo;
    
    public RMAManagementView() {
        this.apiService = new ApiService();
        this.rmaRecords = FXCollections.observableArrayList();
        
        // Configuration principale
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #f8f9fa;");
        
        // Initialisation des composants
        this.rmaStatusFilter = new ComboBox<>();
        this.rmaTypeFilter = new ComboBox<>();
        this.rmaSearchField = new TextField();
        this.rmaDateFrom = new DatePicker();
        this.rmaDateTo = new DatePicker();
        this.rmaTable = createRMATable();
        this.rmaDetailsArea = new TextArea();
        
        // Construction de l'interface
        setupRMAInterface();
        setupRMAEventHandlers();
        
        // Chargement initial des données
        loadRMARecords();
    }
    
    private void setupRMAInterface() {
        // En-tête spécifique aux RMA
        HBox headerBox = createRMAHeaderSection();
        
        // Section de filtres RMA
        VBox filtersSection = createRMAFiltersSection();
        
        // Section principale avec tableau et workflow RMA
        HBox mainSection = createRMAMainSection();
        
        // Barre d'actions RMA
        HBox actionsBar = createRMAActionsBar();
        
        this.getChildren().addAll(headerBox, filtersSection, mainSection, actionsBar);
    }
    
    private HBox createRMAHeaderSection() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        Label titleLabel = new Label("📦 Gestion des RMA (Return Merchandise Authorization)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Indicateurs spécifiques aux RMA
        VBox statsBox = new VBox(3);
        statsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Label pendingLabel = new Label("⏳ En attente: 0");
        pendingLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #f39c12;");
        
        Label processedLabel = new Label("✅ Traités: 0");
        processedLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
        
        statsBox.getChildren().addAll(pendingLabel, processedLabel);
        
        headerBox.getChildren().addAll(titleLabel, spacer, statsBox);
        return headerBox;
    }
    
    private VBox createRMAFiltersSection() {
        VBox filtersSection = new VBox(10);
        filtersSection.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.2), 6, 0, 0, 2);");
        
        Label filtersTitle = new Label("🔍 Filtres RMA");
        filtersTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");
        
        // Configuration des filtres spécifiques aux RMA
        setupRMAFilterComboBoxes();
        
        // Ligne de recherche RMA
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        rmaSearchField.setPromptText("N° RMA, équipement, motif...");
        rmaSearchField.setPrefWidth(300);
        rmaSearchField.setStyle("-fx-background-radius: 4px; -fx-border-color: #e74c3c; -fx-border-radius: 4px;");
        
        searchBox.getChildren().addAll(new Label("Rechercher :"), rmaSearchField);
        
        // Ligne de filtres par statut et type
        HBox combosBox = new HBox(15);
        combosBox.setAlignment(Pos.CENTER_LEFT);
        
        combosBox.getChildren().addAll(
            createRMAFilterGroup("Statut RMA :", rmaStatusFilter),
            createRMAFilterGroup("Type de retour :", rmaTypeFilter)
        );
        
        // Ligne de filtres par dates
        HBox datesBox = new HBox(15);
        datesBox.setAlignment(Pos.CENTER_LEFT);
        
        Button thisMonthBtn = new Button("Ce mois");
        thisMonthBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px;");
        thisMonthBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            rmaDateFrom.setValue(today.withDayOfMonth(1));
            rmaDateTo.setValue(today);
            applyRMAFilters();
        });
        
        datesBox.getChildren().addAll(
            new Label("Période RMA :"), rmaDateFrom, new Label("à"), rmaDateTo, thisMonthBtn
        );
        
        filtersSection.getChildren().addAll(filtersTitle, searchBox, combosBox, datesBox);
        return filtersSection;
    }
    
    private VBox createRMAFilterGroup(String labelText, ComboBox<String> combo) {
        VBox group = new VBox(3);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #c0392b;");
        combo.setPrefWidth(150);
        combo.setStyle("-fx-background-radius: 4px; -fx-border-color: #e74c3c; -fx-border-radius: 4px;");
        group.getChildren().addAll(label, combo);
        return group;
    }
    
    private void setupRMAFilterComboBoxes() {
        // Statuts RMA
        rmaStatusFilter.getItems().addAll(
            "Tous statuts", "Initié", "Autorisé", "En transit retour", 
            "Reçu", "En cours d'analyse", "Réparé", "Remplacé", "Remboursé", "Refusé"
        );
        rmaStatusFilter.setValue("Tous statuts");
        
        // Types de retour
        rmaTypeFilter.getItems().addAll(
            "Tous types", "Défaut de fabrication", "Dommage transport", 
            "Non-conformité", "Fin de garantie", "Upgrade", "Erreur commande"
        );
        rmaTypeFilter.setValue("Tous types");
    }
    
    private HBox createRMAMainSection() {
        HBox mainSection = new HBox(15);
        mainSection.setAlignment(Pos.TOP_LEFT);
        
        // Tableau des RMA (60% de la largeur)
        VBox tableSection = createRMATableSection();
        
        // Panneau de workflow RMA (40% de la largeur)
        VBox workflowSection = createRMAWorkflowSection();
        
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        workflowSection.setPrefWidth(400);
        
        mainSection.getChildren().addAll(tableSection, workflowSection);
        return mainSection;
    }
    
    private VBox createRMATableSection() {
        VBox tableSection = new VBox(10);
        tableSection.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        Label tableTitle = new Label("📋 Registre des RMA");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        rmaTable.setPrefHeight(400);
        rmaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tableSection.getChildren().addAll(tableTitle, rmaTable);
        VBox.setVgrow(rmaTable, Priority.ALWAYS);
        
        return tableSection;
    }
    
    private VBox createRMAWorkflowSection() {
        VBox workflowSection = new VBox(15);
        workflowSection.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(231,76,60,0.2), 6, 0, 0, 2);");
        
        Label workflowTitle = new Label("⚙️ Workflow RMA");
        workflowTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");
        
        // Étapes du workflow RMA avec indicateurs visuels
        VBox stepsBox = createRMAStepsIndicator();
        
        // Zone de détails RMA
        Label detailsLabel = new Label("📄 Détails RMA sélectionné");
        detailsLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        rmaDetailsArea.setPrefHeight(200);
        rmaDetailsArea.setEditable(false);
        rmaDetailsArea.setWrapText(true);
        rmaDetailsArea.setStyle("-fx-background-color: #fdf2f2; -fx-border-color: #e74c3c; -fx-border-radius: 4px; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        rmaDetailsArea.setPromptText("Sélectionnez un RMA pour voir les détails...");
        
        // Actions rapides RMA
        HBox quickActionsBox = createRMAQuickActions();
        
        workflowSection.getChildren().addAll(workflowTitle, stepsBox, detailsLabel, rmaDetailsArea, quickActionsBox);
        VBox.setVgrow(rmaDetailsArea, Priority.ALWAYS);
        
        return workflowSection;
    }
    
    private VBox createRMAStepsIndicator() {
        VBox stepsBox = new VBox(8);
        stepsBox.setStyle("-fx-background-color: #fdf2f2; -fx-padding: 10px; -fx-background-radius: 6px;");
        
        String[] steps = {
            "1️⃣ Initiation RMA",
            "2️⃣ Autorisation", 
            "3️⃣ Expédition retour",
            "4️⃣ Réception & analyse",
            "5️⃣ Résolution",
            "6️⃣ Clôture"
        };
        
        for (String step : steps) {
            Label stepLabel = new Label(step);
            stepLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-padding: 3px 0;");
            stepsBox.getChildren().add(stepLabel);
        }
        
        return stepsBox;
    }
    
    private HBox createRMAQuickActions() {
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER);
        
        Button authorizeBtn = new Button("✅ Autoriser");
        authorizeBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 5px 8px;");
        
        Button receiveBtn = new Button("📦 Marquer reçu");
        receiveBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 5px 8px;");
        
        Button closeBtn = new Button("🔒 Clôturer");
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px; -fx-padding: 5px 8px;");
        
        // Désactivés par défaut, activés selon la sélection
        authorizeBtn.setDisable(true);
        receiveBtn.setDisable(true);
        closeBtn.setDisable(true);
        
        actionsBox.getChildren().addAll(authorizeBtn, receiveBtn, closeBtn);
        return actionsBox;
    }
    
    private HBox createRMAActionsBar() {
        HBox actionsBar = new HBox(10);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(15, 0, 0, 0));
        
        Button newRMABtn = new Button("📦 Nouveau RMA");
        newRMABtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        newRMABtn.setOnAction(e -> openNewRMADialog());
        
        Button printLabelBtn = new Button("🏷️ Imprimer étiquette");
        printLabelBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        
        Button generateReportBtn = new Button("📊 Rapport RMA");
        generateReportBtn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        refreshBtn.setOnAction(e -> loadRMARecords());
        
        actionsBar.getChildren().addAll(newRMABtn, printLabelBtn, generateReportBtn, refreshBtn);
        return actionsBar;
    }
    
    private TableView<RMARecord> createRMATable() {
        TableView<RMARecord> table = new TableView<>();
        table.setItems(rmaRecords);
        
        // Colonne N° RMA
        TableColumn<RMARecord, String> rmaNumberCol = new TableColumn<>("N° RMA");
        rmaNumberCol.setPrefWidth(100);
        rmaNumberCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRmaNumber()));
        
        // Colonne Équipement
        TableColumn<RMARecord, String> equipmentCol = new TableColumn<>("Équipement");
        equipmentCol.setPrefWidth(150);
        equipmentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEquipmentName()));
        
        // Colonne Motif
        TableColumn<RMARecord, String> reasonCol = new TableColumn<>("Motif");
        reasonCol.setPrefWidth(120);
        reasonCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getReturnReason()));
        
        // Colonne Statut avec icône
        TableColumn<RMARecord, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getStatus();
            String icon = getRMAStatusIcon(status);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + status);
        });
        
        // Colonne Client/Demandeur
        TableColumn<RMARecord, String> customerCol = new TableColumn<>("Client");
        customerCol.setPrefWidth(130);
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCustomerName()));
        
        // Colonne Date création
        TableColumn<RMARecord, String> dateCol = new TableColumn<>("Créé le");
        dateCol.setPrefWidth(90);
        dateCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getCreatedAt();
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        
        // Colonne Valeur estimée
        TableColumn<RMARecord, String> valueCol = new TableColumn<>("Valeur");
        valueCol.setPrefWidth(80);
        valueCol.setCellValueFactory(data -> {
            Double value = data.getValue().getEstimatedValue();
            return new javafx.beans.property.SimpleStringProperty(value != null ? String.format("%.0f €", value) : "N/A");
        });
        
        table.getColumns().addAll(rmaNumberCol, equipmentCol, reasonCol, statusCol, customerCol, dateCol, valueCol);
        
        // Style conditionnel pour les lignes
        table.setRowFactory(tv -> {
            TableRow<RMARecord> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    String backgroundColor = getRMARowBackgroundColor(newItem.getStatus());
                    row.setStyle(backgroundColor + "; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");
                }
            });
            return row;
        });
        
        return table;
    }
    
    private void setupRMAEventHandlers() {
        // Gestionnaire de sélection RMA
        rmaTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayRMADetails(newSelection);
            }
        });
        
        // Gestionnaires de filtres
        rmaSearchField.textProperty().addListener((obs, oldText, newText) -> applyRMAFilters());
        rmaStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
        rmaTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
        rmaDateFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
        rmaDateTo.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
    }
    
    private void applyRMAFilters() {
        // Implémentation du filtrage des RMA
        // Logique de filtrage basée sur les critères sélectionnés
    }
    
    private void displayRMADetails(RMARecord rma) {
        StringBuilder details = new StringBuilder();
        
        details.append("═══ DÉTAILS RMA ═══\n\n");
        details.append("📦 N° RMA: ").append(rma.getRmaNumber()).append("\n");
        details.append("📊 Statut: ").append(getRMAStatusIcon(rma.getStatus())).append(" ").append(rma.getStatus()).append("\n");
        details.append("🖥️ Équipement: ").append(rma.getEquipmentName()).append("\n");
        details.append("📝 Motif: ").append(rma.getReturnReason()).append("\n");
        details.append("👤 Client: ").append(rma.getCustomerName()).append("\n\n");
        
        if (rma.getEstimatedValue() != null) {
            details.append("💰 Valeur estimée: ").append(String.format("%.2f €", rma.getEstimatedValue())).append("\n");
        }
        
        details.append("📅 Créé le: ").append(rma.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n\n");
        
        if (rma.getDescription() != null && !rma.getDescription().trim().isEmpty()) {
            details.append("═══ DESCRIPTION ═══\n");
            details.append(rma.getDescription()).append("\n\n");
        }
        
        details.append("═══ WORKFLOW ═══\n");
        details.append("• Prochaine étape recommandée selon le statut actuel\n");
        details.append("• Historique des actions (à implémenter)\n");
        
        rmaDetailsArea.setText(details.toString());
    }
    
    private String getRMAStatusIcon(String status) {
        switch (status.toUpperCase()) {
            case "INITIÉ": return "🆕";
            case "AUTORISÉ": return "✅";
            case "EN TRANSIT RETOUR": return "🚚";
            case "REÇU": return "📦";
            case "EN COURS D'ANALYSE": return "🔍";
            case "RÉPARÉ": return "🔧";
            case "REMPLACÉ": return "🔄";
            case "REMBOURSÉ": return "💸";
            case "REFUSÉ": return "❌";
            default: return "❓";
        }
    }
    
    private String getRMARowBackgroundColor(String status) {
        switch (status.toUpperCase()) {
            case "INITIÉ": return "-fx-background-color: #fff3cd";
            case "AUTORISÉ": return "-fx-background-color: #d1ecf1"; 
            case "EN TRANSIT RETOUR": return "-fx-background-color: #e2e3e5";
            case "REÇU": return "-fx-background-color: #d4edda";
            case "RÉPARÉ": case "REMPLACÉ": case "REMBOURSÉ": return "-fx-background-color: #d1ecf1";
            case "REFUSÉ": return "-fx-background-color: #f8d7da";
            default: return "-fx-background-color: white";
        }
    }
    
    private void loadRMARecords() {
        // Simulation de données RMA pour développement
        rmaRecords.clear();
        
        // Exemples de RMA
        rmaRecords.addAll(
            new RMARecord("RMA-2024-001", "Projecteur LED XR-300", "Défaut de fabrication", 
                         "Initié", "MagScène Production", 1250.0, LocalDateTime.now().minusDays(2),
                         "Projecteur ne s'allume plus après 3 semaines d'utilisation normale"),
            new RMARecord("RMA-2024-002", "Console Audio MX-48", "Dommage transport", 
                         "Autorisé", "Festival Été Lyon", 3400.0, LocalDateTime.now().minusDays(5),
                         "Dommages visibles sur le châssis et plusieurs faders défaillants"),
            new RMARecord("RMA-2024-003", "Écran LED P3.9", "Non-conformité", 
                         "Reçu", "Théâtre Municipal", 2100.0, LocalDateTime.now().minusDays(10),
                         "Résolution d'affichage non conforme aux spécifications commandées")
        );
    }
    
    private void openNewRMADialog() {
        // Ouvrir un dialog spécialisé pour créer un nouveau RMA
        Dialog<RMARecord> dialog = new Dialog<>();
        dialog.setTitle("Nouveau RMA");
        dialog.setHeaderText("Créer une nouvelle demande de retour matériel");
        
        // Configuration du dialogue (implémentation complète nécessaire)
        ButtonType createButtonType = new ButtonType("Créer RMA", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // TODO: Implémenter le formulaire complet de création RMA
        
        dialog.showAndWait().ifPresent(rma -> {
            rmaRecords.add(rma);
            AlertUtil.showInfo("RMA Créé", "Le RMA " + rma.getRmaNumber() + " a été créé avec succès");
        });
    }
    
    /**
     * Classe interne pour représenter un enregistrement RMA
     */
    public static class RMARecord {
        private String rmaNumber;
        private String equipmentName;
        private String returnReason;
        private String status;
        private String customerName;
        private Double estimatedValue;
        private LocalDateTime createdAt;
        private String description;
        
        public RMARecord(String rmaNumber, String equipmentName, String returnReason, 
                        String status, String customerName, Double estimatedValue, 
                        LocalDateTime createdAt, String description) {
            this.rmaNumber = rmaNumber;
            this.equipmentName = equipmentName;
            this.returnReason = returnReason;
            this.status = status;
            this.customerName = customerName;
            this.estimatedValue = estimatedValue;
            this.createdAt = createdAt;
            this.description = description;
        }
        
        // Getters
        public String getRmaNumber() { return rmaNumber; }
        public String getEquipmentName() { return equipmentName; }
        public String getReturnReason() { return returnReason; }
        public String getStatus() { return status; }
        public String getCustomerName() { return customerName; }
        public Double getEstimatedValue() { return estimatedValue; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getDescription() { return description; }
        
        // Setters
        public void setRmaNumber(String rmaNumber) { this.rmaNumber = rmaNumber; }
        public void setEquipmentName(String equipmentName) { this.equipmentName = equipmentName; }
        public void setReturnReason(String returnReason) { this.returnReason = returnReason; }
        public void setStatus(String status) { this.status = status; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public void setEstimatedValue(Double estimatedValue) { this.estimatedValue = estimatedValue; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public void setDescription(String description) { this.description = description; }
    }
}