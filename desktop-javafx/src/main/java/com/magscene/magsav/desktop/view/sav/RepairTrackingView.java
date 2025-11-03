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
import com.magscene.magsav.desktop.dialog.ServiceRequestDialog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Interface avancée de suivi des réparations et interventions SAV
 * Permet un suivi détaillé de l'état des réparations avec historique
 */
public class RepairTrackingView extends VBox {
    
    private final ApiService apiService;
    private final ObservableList<ServiceRequest> serviceRequests;
    private final TableView<ServiceRequest> requestsTable;
    private final TextArea historyArea;
    private final Label statusSummaryLabel;
    
    // Filtres et recherche
    private final TextField searchField;
    private final ComboBox<String> statusFilter;
    private final ComboBox<String> priorityFilter;
    private final ComboBox<String> typeFilter;
    private final DatePicker dateFromFilter;
    private final DatePicker dateToFilter;
    
    public RepairTrackingView() {
        this.apiService = new ApiService();
        this.serviceRequests = FXCollections.observableArrayList();
        
        // Configuration principale
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        this.setStyle("-fx-background-color: #f8f9fa;");
        
        // Initialisation des composants
        this.searchField = new TextField();
        this.statusFilter = new ComboBox<>();
        this.priorityFilter = new ComboBox<>();
        this.typeFilter = new ComboBox<>();
        this.dateFromFilter = new DatePicker();
        this.dateToFilter = new DatePicker();
        this.requestsTable = createRequestsTable();
        this.historyArea = new TextArea();
        this.statusSummaryLabel = new Label();
        
        // Construction de l'interface
        setupInterface();
        setupEventHandlers();
        
        // Chargement initial des données
        loadServiceRequests();
    }
    
    private void setupInterface() {
        // En-tête avec titre et résumé
        HBox headerBox = createHeaderSection();
        
        // Section de filtres et recherche
        VBox filtersSection = createFiltersSection();
        
        // Section principale avec tableau et détails
        HBox mainSection = createMainSection();
        
        // Barre d'actions
        HBox actionsBar = createActionsBar();
        
        this.getChildren().addAll(headerBox, filtersSection, mainSection, actionsBar);
    }
    
    private HBox createHeaderSection() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 15, 0));
        
        // Titre principal
        Label titleLabel = new Label("🔧 Suivi des Réparations SAV");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Séparateur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Résumé des statuts
        statusSummaryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-background-color: white; -fx-padding: 8px 12px; -fx-background-radius: 4px;");
        
        headerBox.getChildren().addAll(titleLabel, spacer, statusSummaryLabel);
        return headerBox;
    }
    
    private VBox createFiltersSection() {
        VBox filtersSection = new VBox(10);
        filtersSection.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        Label filtersTitle = new Label("🔍 Filtres et Recherche");
        filtersTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        // Ligne 1 : Recherche textuelle
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Rechercher :");
        searchLabel.setMinWidth(80);
        
        searchField.setPromptText("Titre, description, demandeur...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 4px; -fx-border-color: #bdc3c7; -fx-border-radius: 4px;");
        
        Button clearSearchBtn = new Button("✕");
        clearSearchBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 10px;");
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            applyFilters();
        });
        
        searchBox.getChildren().addAll(searchLabel, searchField, clearSearchBtn);
        
        // Ligne 2 : Filtres par combos
        HBox combosBox = new HBox(15);
        combosBox.setAlignment(Pos.CENTER_LEFT);
        
        // Configuration des ComboBox de filtres
        setupFilterComboBoxes();
        
        combosBox.getChildren().addAll(
            createFilterGroup("Statut :", statusFilter),
            createFilterGroup("Priorité :", priorityFilter),
            createFilterGroup("Type :", typeFilter)
        );
        
        // Ligne 3 : Filtres par dates
        HBox datesBox = new HBox(15);
        datesBox.setAlignment(Pos.CENTER_LEFT);
        
        dateFromFilter.setPromptText("Date début");
        dateToFilter.setPromptText("Date fin");
        
        Button todayBtn = new Button("Aujourd'hui");
        todayBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px;");
        todayBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            dateFromFilter.setValue(today);
            dateToFilter.setValue(today);
            applyFilters();
        });
        
        Button weekBtn = new Button("Cette semaine");
        weekBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px;");
        weekBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            dateFromFilter.setValue(today.minusDays(7));
            dateToFilter.setValue(today);
            applyFilters();
        });
        
        Button clearDatesBtn = new Button("Réinitialiser");
        clearDatesBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 4px; -fx-font-size: 11px;");
        clearDatesBtn.setOnAction(e -> {
            dateFromFilter.setValue(null);
            dateToFilter.setValue(null);
            applyFilters();
        });
        
        datesBox.getChildren().addAll(
            new Label("Période :"), dateFromFilter, new Label("à"), dateToFilter,
            todayBtn, weekBtn, clearDatesBtn
        );
        
        filtersSection.getChildren().addAll(filtersTitle, searchBox, combosBox, datesBox);
        return filtersSection;
    }
    
    private VBox createFilterGroup(String labelText, ComboBox<String> combo) {
        VBox group = new VBox(3);
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        combo.setPrefWidth(120);
        combo.setStyle("-fx-background-radius: 4px; -fx-border-color: #bdc3c7; -fx-border-radius: 4px;");
        group.getChildren().addAll(label, combo);
        return group;
    }
    
    private void setupFilterComboBoxes() {
        // Configuration du filtre de statut
        statusFilter.getItems().addAll("Tous", "Ouverte", "En cours", "En attente de pièces", "Résolue", "Fermée", "Annulée");
        statusFilter.setValue("Tous");
        
        // Configuration du filtre de priorité
        priorityFilter.getItems().addAll("Toutes", "Faible", "Moyenne", "Élevée", "Urgente");
        priorityFilter.setValue("Toutes");
        
        // Configuration du filtre de type
        typeFilter.getItems().addAll("Tous types", "Réparation", "Maintenance", "Installation", "Formation", "RMA", "Garantie");
        typeFilter.setValue("Tous types");
    }
    
    private HBox createMainSection() {
        HBox mainSection = new HBox(15);
        mainSection.setAlignment(Pos.TOP_LEFT);
        
        // Tableau des demandes (70% de la largeur)
        VBox tableSection = createTableSection();
        
        // Panneau de détails et historique (30% de la largeur)
        VBox detailsSection = createDetailsSection();
        
        // Configuration des proportions
        HBox.setHgrow(tableSection, Priority.ALWAYS);
        detailsSection.setPrefWidth(350);
        detailsSection.setMinWidth(300);
        
        mainSection.getChildren().addAll(tableSection, detailsSection);
        return mainSection;
    }
    
    private VBox createTableSection() {
        VBox tableSection = new VBox(10);
        tableSection.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        Label tableTitle = new Label("📋 Liste des Interventions");
        tableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        // Configuration du tableau
        requestsTable.setPrefHeight(400);
        requestsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        requestsTable.setStyle("-fx-background-color: transparent;");
        
        tableSection.getChildren().addAll(tableTitle, requestsTable);
        VBox.setVgrow(requestsTable, Priority.ALWAYS);
        
        return tableSection;
    }
    
    private VBox createDetailsSection() {
        VBox detailsSection = new VBox(10);
        detailsSection.setStyle("-fx-background-color: white; -fx-padding: 15px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        Label detailsTitle = new Label("📄 Détails & Historique");
        detailsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        // Zone d'historique
        historyArea.setPrefHeight(350);
        historyArea.setEditable(false);
        historyArea.setWrapText(true);
        historyArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 4px; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
        historyArea.setPromptText("Sélectionnez une demande pour voir les détails et l'historique...");
        
        detailsSection.getChildren().addAll(detailsTitle, historyArea);
        VBox.setVgrow(historyArea, Priority.ALWAYS);
        
        return detailsSection;
    }
    
    private HBox createActionsBar() {
        HBox actionsBar = new HBox(10);
        actionsBar.setAlignment(Pos.CENTER_LEFT);
        actionsBar.setPadding(new Insets(15, 0, 0, 0));
        
        Button newRequestBtn = new Button("➕ Nouvelle Demande");
        newRequestBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-padding: 10px 15px;");
        newRequestBtn.setOnAction(e -> openServiceRequestDialog(null));
        
        Button editRequestBtn = new Button("✏️ Modifier");
        editRequestBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        editRequestBtn.setDisable(true);
        editRequestBtn.setOnAction(e -> {
            ServiceRequest selected = requestsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openServiceRequestDialog(selected);
            }
        });
        
        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        refreshBtn.setOnAction(e -> loadServiceRequests());
        
        Button exportBtn = new Button("📊 Exporter");
        exportBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 8px 15px;");
        exportBtn.setOnAction(e -> exportToCSV());
        
        // Activation/désactivation des boutons selon la sélection
        requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            editRequestBtn.setDisable(newSel == null);
        });
        
        // Région pour pousser les stats à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statsLabel = new Label("Statistiques en temps réel");
        statsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        
        actionsBar.getChildren().addAll(newRequestBtn, editRequestBtn, refreshBtn, exportBtn, spacer, statsLabel);
        return actionsBar;
    }
    
    private TableView<ServiceRequest> createRequestsTable() {
        TableView<ServiceRequest> table = new TableView<>();
        table.setItems(serviceRequests);
        
        // Colonne ID avec indicateur de priorité
        TableColumn<ServiceRequest, String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(60);
        idCol.setCellValueFactory(data -> {
            ServiceRequest request = data.getValue();
            String priority = request.getPriority() != null ? request.getPriority().toString() : "MEDIUM";
            String icon = getPriorityIcon(priority);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + request.getId());
        });
        
        // Colonne Titre
        TableColumn<ServiceRequest, String> titleCol = new TableColumn<>("Titre");
        titleCol.setPrefWidth(200);
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        
        // Colonne Type
        TableColumn<ServiceRequest, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(100);
        typeCol.setCellValueFactory(data -> {
            String type = data.getValue().getType() != null ? data.getValue().getType().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(type);
        });
        
        // Colonne Statut avec couleur
        TableColumn<ServiceRequest, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getStatus() != null ? data.getValue().getStatus().toString() : "OPEN";
            return new javafx.beans.property.SimpleStringProperty(getStatusIcon(status) + " " + status);
        });
        
        // Colonne Demandeur
        TableColumn<ServiceRequest, String> requesterCol = new TableColumn<>("Demandeur");
        requesterCol.setPrefWidth(150);
        requesterCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getRequesterName() != null ? data.getValue().getRequesterName() : "N/A"));
        
        // Colonne Technicien
        TableColumn<ServiceRequest, String> technicianCol = new TableColumn<>("Technicien");
        technicianCol.setPrefWidth(130);
        technicianCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getAssignedTechnician() != null ? data.getValue().getAssignedTechnician() : "Non assigné"));
        
        // Colonne Date création
        TableColumn<ServiceRequest, String> dateCol = new TableColumn<>("Créé le");
        dateCol.setPrefWidth(100);
        dateCol.setCellValueFactory(data -> {
            if (data.getValue().getCreatedAt() != null) {
                String formattedDate = data.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yy"));
                return new javafx.beans.property.SimpleStringProperty(formattedDate);
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        table.getColumns().addAll(idCol, titleCol, typeCol, statusCol, requesterCol, technicianCol, dateCol);
        
        // Style du tableau
        table.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else {
                    String priority = newItem.getPriority() != null ? newItem.getPriority().toString() : "MEDIUM";
                    String status = newItem.getStatus() != null ? newItem.getStatus().toString() : "OPEN";
                    
                    String backgroundColor = getRowBackgroundColor(priority, status);
                    row.setStyle(backgroundColor + "; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");
                }
            });
            return row;
        });
        
        return table;
    }
    
    private void setupEventHandlers() {
        // Gestionnaire pour la sélection dans le tableau
        requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayServiceRequestDetails(newSelection);
            }
        });
        
        // Gestionnaires pour les filtres
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        priorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFromFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateToFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void applyFilters() {
        // Implementation du filtrage en temps réel
        // Cette méthode sera appelée à chaque changement de filtre
        String searchText = searchField.getText().toLowerCase();
        String statusFilterValue = statusFilter.getValue();
        String priorityFilterValue = priorityFilter.getValue(); 
        String typeFilterValue = typeFilter.getValue();
        LocalDate dateFrom = dateFromFilter.getValue();
        LocalDate dateTo = dateToFilter.getValue();
        
        // Appliquer les filtres (implémentation complète nécessaire)
        updateStatusSummary();
    }
    
    private void displayServiceRequestDetails(ServiceRequest request) {
        StringBuilder details = new StringBuilder();
        
        // En-tête avec informations principales
        details.append("═══ DÉTAILS DE LA DEMANDE SAV ═══\n\n");
        details.append("🆔 ID: ").append(request.getId()).append("\n");
        details.append("📝 Titre: ").append(request.getTitle()).append("\n");
        details.append("📊 Statut: ").append(getStatusIcon(request.getStatus().toString())).append(" ").append(request.getStatus()).append("\n");
        details.append("⚡ Priorité: ").append(getPriorityIcon(request.getPriority().toString())).append(" ").append(request.getPriority()).append("\n");
        details.append("🔧 Type: ").append(request.getType()).append("\n\n");
        
        // Personnes impliquées
        details.append("═══ PERSONNES ═══\n");
        details.append("👤 Demandeur: ").append(request.getRequesterName() != null ? request.getRequesterName() : "N/A").append("\n");
        details.append("📧 Email: ").append(request.getRequesterEmail() != null ? request.getRequesterEmail() : "N/A").append("\n");
        details.append("🔨 Technicien: ").append(request.getAssignedTechnician() != null ? request.getAssignedTechnician() : "Non assigné").append("\n\n");
        
        // Équipement concerné
        if (request.getEquipment() != null) {
            details.append("═══ ÉQUIPEMENT ═══\n");
            details.append("🖥️ ID Équipement: ").append(request.getEquipment().getId()).append("\n\n");
        }
        
        // Description
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            details.append("═══ DESCRIPTION ═══\n");
            details.append(request.getDescription()).append("\n\n");
        }
        
        // Coût estimé
        if (request.getEstimatedCost() != null) {
            details.append("💰 Coût estimé: ").append(String.format("%.2f €", request.getEstimatedCost())).append("\n\n");
        }
        
        // Notes techniques
        if (request.getResolutionNotes() != null && !request.getResolutionNotes().trim().isEmpty()) {
            details.append("═══ NOTES TECHNIQUES ═══\n");
            details.append(request.getResolutionNotes()).append("\n\n");
        }
        
        // Dates importantes
        details.append("═══ CHRONOLOGIE ═══\n");
        if (request.getCreatedAt() != null) {
            details.append("📅 Créé le: ").append(request.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }
        if (request.getUpdatedAt() != null) {
            details.append("🔄 Modifié le: ").append(request.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
        }
        
        historyArea.setText(details.toString());
    }
    
    private void updateStatusSummary() {
        long total = serviceRequests.size();
        long open = serviceRequests.stream().mapToLong(r -> 
            (r.getStatus().toString().equals("OPEN") || r.getStatus().toString().equals("IN_PROGRESS")) ? 1 : 0).sum();
        long urgent = serviceRequests.stream().mapToLong(r -> 
            r.getPriority().toString().equals("URGENT") ? 1 : 0).sum();
        
        statusSummaryLabel.setText(String.format("📊 Total: %d | 🔓 Ouvertes: %d | 🚨 Urgentes: %d", total, open, urgent));
    }
    
    private String getPriorityIcon(String priority) {
        switch (priority.toUpperCase()) {
            case "LOW": return "🟢";
            case "MEDIUM": return "🟡";
            case "HIGH": return "🟠"; 
            case "URGENT": return "🔴";
            default: return "⚪";
        }
    }
    
    private String getStatusIcon(String status) {
        switch (status.toUpperCase()) {
            case "OPEN": return "🔓";
            case "IN_PROGRESS": return "⚙️";
            case "WAITING_FOR_PARTS": return "📦";
            case "RESOLVED": return "✅";
            case "CLOSED": return "🔒";
            case "CANCELLED": return "❌";
            default: return "❓";
        }
    }
    
    private String getRowBackgroundColor(String priority, String status) {
        // Couleur de fond selon priorité et statut
        if (priority.equals("URGENT")) {
            return "-fx-background-color: #ffebee";
        } else if (status.equals("RESOLVED")) {
            return "-fx-background-color: #e8f5e8";
        } else if (status.equals("IN_PROGRESS")) {
            return "-fx-background-color: #fff3e0";
        }
        return "-fx-background-color: white";
    }
    
    private void loadServiceRequests() {
        // Indicateur de chargement
        statusSummaryLabel.setText("🔄 Chargement en cours...");
        
        Task<List<ServiceRequest>> loadTask = new Task<List<ServiceRequest>>() {
            @Override
            protected List<ServiceRequest> call() throws Exception {
                // Appel asynchrone à l'API
                // Simulation de données pour le moment
                return RepairTrackingView.this.createSimulatedServiceRequests();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    List<ServiceRequest> requests = getValue();
                    serviceRequests.clear();
                    if (requests != null) {
                        serviceRequests.addAll(requests);
                    }
                    updateStatusSummary();
                    applyFilters(); // Réappliquer les filtres actuels
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    statusSummaryLabel.setText("❌ Erreur de chargement");
                    AlertUtil.showError("Erreur", "Impossible de charger les demandes SAV: " + 
                        getException().getMessage());
                });
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    private void openServiceRequestDialog(ServiceRequest existingRequest) {
        ServiceRequestDialog dialog = new ServiceRequestDialog(existingRequest);
        java.util.Optional<ServiceRequest> result = dialog.showAndWait();
        
        if (result.isPresent()) {
            // Sauvegarder via l'API puis recharger
            saveServiceRequest(result.get());
        }
    }
    
    private void saveServiceRequest(ServiceRequest request) {
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (request.getId() != null) {
                    // Modification
                    apiService.updateServiceRequest(request.getId(), request).get();
                } else {
                    // Création
                    apiService.createServiceRequest(request).get();
                }
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    loadServiceRequests(); // Recharger la liste
                    AlertUtil.showInfo("Succès", "Demande SAV sauvegardée avec succès");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    AlertUtil.showError("Erreur", "Impossible de sauvegarder la demande SAV: " + 
                        getException().getMessage());
                });
            }
        };
        
        Thread saveThread = new Thread(saveTask);
        saveThread.setDaemon(true);
        saveThread.start();
    }
    
    private void exportToCSV() {
        // TODO: Implémenter l'export CSV des demandes filtrées
        AlertUtil.showInfo("Export", "Fonctionnalité d'export en cours de développement");
    }
    
    /**
     * Méthode publique pour créer une nouvelle demande depuis l'interface parent
     */
    public void createNewServiceRequest() {
        openServiceRequestDialog(null);
    }
    
    /**
     * Méthode publique pour rafraîchir les données
     */
    public void refreshData() {
        loadServiceRequests();
    }
    
    private List<ServiceRequest> createSimulatedServiceRequests() {
        List<ServiceRequest> requests = new java.util.ArrayList<>();
        
        // Simulation de quelques demandes SAV
        ServiceRequest req1 = new ServiceRequest();
        req1.setId(1L);
        req1.setTitle("Panne éclairage scène principale");
        req1.setDescription("Plusieurs projecteurs ne fonctionnent plus sur la scène principale");
        req1.setType(ServiceRequest.ServiceRequestType.MAINTENANCE);
        req1.setStatus(ServiceRequest.ServiceRequestStatus.IN_PROGRESS);
        req1.setPriority(ServiceRequest.Priority.HIGH);
        req1.setRequesterName("Technicien A");
        req1.setAssignedTechnician("Expert Éclairage");
        req1.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
        requests.add(req1);
        
        ServiceRequest req2 = new ServiceRequest();
        req2.setId(2L);
        req2.setTitle("Installation nouveau système son");
        req2.setDescription("Demande d'installation d'une nouvelle console audio");
        req2.setType(ServiceRequest.ServiceRequestType.INSTALLATION);
        req2.setStatus(ServiceRequest.ServiceRequestStatus.OPEN);
        req2.setPriority(ServiceRequest.Priority.MEDIUM);
        req2.setRequesterName("Direction Technique");
        req2.setCreatedAt(java.time.LocalDateTime.now().minusHours(6));
        requests.add(req2);
        
        ServiceRequest req3 = new ServiceRequest();
        req3.setId(3L);
        req3.setTitle("Réparation caméra défaillante");
        req3.setDescription("Caméra n°5 présente des dysfonctionnements");
        req3.setType(ServiceRequest.ServiceRequestType.REPAIR);
        req3.setStatus(ServiceRequest.ServiceRequestStatus.RESOLVED);
        req3.setPriority(ServiceRequest.Priority.LOW);
        req3.setRequesterName("Opérateur Vidéo");
        req3.setAssignedTechnician("Spécialiste Caméra");
        req3.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
        req3.setResolvedAt(java.time.LocalDateTime.now().minusDays(1));
        requests.add(req3);
        
        return requests;
    }
}