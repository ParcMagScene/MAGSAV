package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.view.base.AbstractManagerView;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.dialog.salesinstallation.ProjectDialog;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.util.AlertUtil;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gestionnaire des projets (Ventes & Installations) - VERSION STANDARDISÉE
 * Hérite d'AbstractManagerView pour respecter l'architecture uniforme
 * 
 * STRUCTURE AUTOMATIQUE :
 * - Top: Toolbar (recherche + filtres + actions)  
 * - Center: DetailPanelContainer (table + volet détail)
 */
public class ProjectManagerView extends AbstractManagerView {
    
    // ========================================
    // 💼 COMPOSANTS SPÉCIFIQUES PROJETS; // ========================================
    
    private TableView<ProjectItem> projectTable;
    private ObservableList<ProjectItem> projectData;
    private FilteredList<ProjectItem> filteredData;
    private SortedList<ProjectItem> sortedData;
    
    // Filtres spécifiques projets
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;
    private ComboBox<String> clientFilter;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    
    // Boutons d'action
    private Button addButton;
    private Button editButton;
    private Button deleteButton;
    private Button refreshButton;
    private Button exportButton;
    
    // ========================================
    // 🏗️ CONSTRUCTEUR; // ========================================
    
    public ProjectManagerView(ApiService apiService) {
        super(apiService);
        
        // Chargement des données après construction complète
        Platform.runLater(this::loadProjects);
    }
    
    // ========================================
    // 📊 IMPLÉMENTATION ABSTRAITE OBLIGATOIRE; // ========================================
    
    @Override
    protected String getViewCssClass() {
        return "project-manager";
    }
    
    @Override
    protected String getSearchPromptText() {
        return "N° projet, nom, client, description...";
    }
    
    @Override
    protected void initializeContent() {
        // Initialisation des données
        projectData = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(projectData);
        sortedData = new SortedList<>(filteredData);
        
        // Création de la table
        createProjectTable();
    }
    
    @Override
    protected void createFilters() {
        // 📊 Filtre par statut
        addFilter("📊 Statut", 
            new String[]{"Tous", "DRAFT", "QUOTED", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ON_HOLD"}, 
            "Tous", 
            this::onStatusFilterChanged);
        
        // 🔧 Filtre par type de projet  
        addFilter("🔧 Type",
            new String[]{"Tous", "Vente", "Installation", "Location", "Prestation", "Maintenance"},
            "Tous",
            this::onTypeFilterChanged);
            
        // 👤 Filtre par client
        addFilter("👤 Client",
            new String[]{"Tous", "Mag Scène", "Théâtre National", "Festival d'Avignon", "Concert Hall", "Autres"},
            "Tous", 
            this::onClientFilterChanged);
        
        // 📅 Période (date de début) - Composant personnalisé
        addCustomFilter("📅 Du", createDatePicker("Date début", true));
        
        // 📅 Période (date de fin) - Composant personnalisé  
        addCustomFilter("Au", createDatePicker("Date fin", false));
        
        // Récupération des ComboBox pour les callbacks
        setupFilterReferences();
    }
    
    @Override
    protected void createActions() {
        // ➕ Nouveau projet
        addButton = ViewUtils.createAddButton("➕ Nouveau Projet", this::addProject);
        addActionButton(addButton);
        
        // ✏️ Modifier projet
        editButton = ViewUtils.createEditButton("✏️ Modifier", this::editSelectedProject, 
            getTableSelectionProperty().isNull());
        addActionButton(editButton);
        
        // 🗑️ Supprimer projet
        deleteButton = ViewUtils.createDeleteButton("🗑️ Supprimer", this::deleteSelectedProject,
            getTableSelectionProperty().isNull());
        addActionButton(deleteButton);
        
        // 🔄 Actualiser données
        refreshButton = ViewUtils.createRefreshButton("🔄 Actualiser", this::loadProjects);
        addActionButton(refreshButton);
        
        // 📊 Exporter projets (bouton personnalisé)
        exportButton = new Button("📊 Exporter");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        exportButton.setOnAction(e -> exportProjects());
        addActionButton(exportButton);
    }
    
    @Override
    protected Region createCenterContent() {
        // DetailPanelContainer avec table + volet de détail intégré
        return new DetailPanelContainer(projectTable);
    }
    
    @Override
    protected void onSearchTextChanged(String searchText) {
        updateFilters();
    }
    
    // ========================================
    // 🔧 CRÉATION DE LA TABLE; // ========================================
    
    @SuppressWarnings("unchecked")
    private void createProjectTable() {
        projectTable = new TableView<>();
        projectTable.setItems(sortedData);
        // projectTable supprimé - Style géré par CSS
        sortedData.comparatorProperty().bind(projectTable.comparatorProperty());
        
        // Colonnes de la table
        createTableColumns();
        
        // Configuration de la table
        projectTable.setRowFactory(tv -> {
            TableRow<ProjectItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewSelectedProject();
                }
            });
            return row;
        });
    }
    
    @SuppressWarnings("unchecked")
    private void createTableColumns() {
        // ID
        TableColumn<ProjectItem, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getId()));
        idColumn.setPrefWidth(60);
        
        // N° Projet
        TableColumn<ProjectItem, String> numberColumn = new TableColumn<>("N° Projet");
        numberColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getProjectNumber()));
        numberColumn.setPrefWidth(120);
        
        // Nom du projet
        TableColumn<ProjectItem, String> nameColumn = new TableColumn<>("Nom");
        nameColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(200);
        
        // Type
        TableColumn<ProjectItem, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getType()));
        typeColumn.setPrefWidth(100);
        
        // Statut
        TableColumn<ProjectItem, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStatus()));
        statusColumn.setPrefWidth(120);
        
        // Client
        TableColumn<ProjectItem, String> clientColumn = new TableColumn<>("Client");
        clientColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getClient()));
        clientColumn.setPrefWidth(150);
        
        // Date début
        TableColumn<ProjectItem, String> startDateColumn = new TableColumn<>("Date début");
        startDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getStartDate()));
        startDateColumn.setPrefWidth(100);
        
        // Date fin
        TableColumn<ProjectItem, String> endDateColumn = new TableColumn<>("Date fin");
        endDateColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEndDate()));
        endDateColumn.setPrefWidth(100);
        
        // Montant estimé
        TableColumn<ProjectItem, String> amountColumn = new TableColumn<>("Montant estimé");
        amountColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(data.getValue().getEstimatedAmount()));
        amountColumn.setPrefWidth(120);
        
        projectTable.getColumns().addAll(idColumn, numberColumn, nameColumn, typeColumn, 
                                        statusColumn, clientColumn, startDateColumn, 
                                        endDateColumn, amountColumn);
    }
    
    // ========================================
    // 🔍 GESTION DES FILTRES; // ========================================
    
    private void setupFilterReferences() {
        Platform.runLater(() -> {
            if (filtersContainer.getChildren().size() >= 3) {
                statusFilter = getFilterComboBox(0);
                typeFilter = getFilterComboBox(1); 
                clientFilter = getFilterComboBox(2);
                // Les DatePickers sont gérés séparément
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private ComboBox<String> getFilterComboBox(int index) {
        try {
            return (ComboBox<String>) ((VBox) filtersContainer.getChildren().get(index)).getChildren().get(1);
        } catch (Exception e) {
            System.err.println("Erreur récupération ComboBox filtre " + index + ": " + e.getMessage());
            return null;
        }
    }
    
    private DatePicker createDatePicker(String promptText, boolean isStart) {
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText(promptText);
        datePicker.setOnAction(e -> updateFilters());
        
        if (isStart) {
            startDatePicker = datePicker;
        } else {
            endDatePicker = datePicker;
        }
        
        return datePicker;
    }
    
    private void addCustomFilter(String label, DatePicker datePicker) {
        VBox filterBox = new VBox(5);
        Label filterLabel = new Label(label);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        filterBox.getChildren().addAll(filterLabel, datePicker);
        filtersContainer.getChildren().add(filterBox);
    }
    
    private void onStatusFilterChanged(String status) {
        updateFilters();
    }
    
    private void onTypeFilterChanged(String type) {
        updateFilters();
    }
    
    private void onClientFilterChanged(String client) {
        updateFilters();
    }
    
    private void updateFilters() {
        filteredData.setPredicate(project -> {
            // Filtre de recherche textuelle
            String searchText = getSearchField().getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!project.getName().toLowerCase().contains(lowerCaseFilter) &&
                    !project.getProjectNumber().toLowerCase().contains(lowerCaseFilter) &&
                    !project.getClient().toLowerCase().contains(lowerCaseFilter) &&
                    !project.getType().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }
            
            // Filtre par statut
            if (statusFilter != null && statusFilter.getValue() != null && 
                !statusFilter.getValue().equals("Tous")) {
                if (!project.getStatus().equals(statusFilter.getValue())) {
                    return false;
                }
            }
            
            // Filtre par type
            if (typeFilter != null && typeFilter.getValue() != null && 
                !typeFilter.getValue().equals("Tous")) {
                if (!project.getType().equals(typeFilter.getValue())) {
                    return false;
                }
            }
            
            // Filtre par client
            if (clientFilter != null && clientFilter.getValue() != null && 
                !clientFilter.getValue().equals("Tous")) {
                if (!project.getClient().contains(clientFilter.getValue()) && 
                    !clientFilter.getValue().equals("Autres")) {
                    return false;
                }
            }
            
            // Filtres par dates (si définies)
            if (startDatePicker != null && startDatePicker.getValue() != null) {
                LocalDate filterStartDate = startDatePicker.getValue();
                LocalDate projectStartDate = LocalDate.parse(project.getStartDate());
                if (projectStartDate.isBefore(filterStartDate)) {
                    return false;
                }
            }
            
            if (endDatePicker != null && endDatePicker.getValue() != null) {
                LocalDate filterEndDate = endDatePicker.getValue();
                LocalDate projectEndDate = LocalDate.parse(project.getEndDate());
                if (projectEndDate.isAfter(filterEndDate)) {
                    return false;
                }
            }
            
            return true;
        });
    }
    
    // ========================================
    // ⚡ ACTIONS DES BOUTONS; // ========================================
    
    private void addProject() {
        ProjectDialog dialog = new ProjectDialog("Nouveau Projet", null);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(projectData -> {
            try {
                Map<String, Object> createdProject = apiService.create("projects", projectData);
                
                // Recharger la liste après création
                refresh();
                
                // Sélectionner le projet créé
                if (createdProject != null && createdProject.containsKey("id")) {
                    Platform.runLater(() -> selectProjectById(createdProject.get("id")));
                }
                
                AlertUtil.showInfo("Succès", "Projet créé avec succès !");
                
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de créer le projet: " + e.getMessage());
            }
        });
    }
    
    private void editSelectedProject() {
        ProjectItem selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Map<String, Object> projectData = selected.toMap();
            ProjectDialog dialog = new ProjectDialog("Modifier Projet", projectData);
            Optional<Map<String, Object>> result = dialog.showAndWait();
            
            result.ifPresent(updatedData -> {
                try {
                    apiService.update("projects", Long.parseLong(selected.getId()), updatedData);
                    refresh();
                    AlertUtil.showInfo("Succès", "Projet modifié avec succès !");
                } catch (Exception e) {
                    AlertUtil.showError("Erreur", "Impossible de modifier le projet: " + e.getMessage());
                }
            });
        }
    }
    
    private void viewSelectedProject() {
        ProjectItem selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Créer ProjectDialog en mode lecture seule
            Map<String, Object> projectData = selected.toMap();
            ProjectDialog dialog = new ProjectDialog("Détails Projet", projectData);
            dialog.showAndWait();
        }
    }
    
    private void deleteSelectedProject() {
        ProjectItem selected = projectTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer le projet");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer le projet \"" + selected.getName() + "\" ?");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        apiService.delete("projects", Long.parseLong(selected.getId()));
                        projectData.remove(selected);
                        AlertUtil.showInfo("Succès", "Projet supprimé avec succès !");
                    } catch (Exception e) {
                        AlertUtil.showError("Erreur", "Impossible de supprimer le projet: " + e.getMessage());
                    }
                }
            });
        }
    }
    
    private void exportProjects() {
        // TODO: Implémentation export (CSV, Excel, PDF...)
        AlertUtil.showInfo("Export", "Export de " + projectData.size() + " projets en cours...");
    }
    
    // ========================================
    // 📊 GESTION DES DONNÉES; // ========================================
    
    private void loadProjects() {
        try {
            List<Map<String, Object>> projects = apiService.getAll("projects");
            projectData.clear();
            
            for (Map<String, Object> project : projects) {
                projectData.add(new ProjectItem(project));
            }
            
            System.out.println("✅ " + projectData.size() + " projets chargés");
            
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les projets: " + e.getMessage());
            
            // Fallback avec données de démonstration
            createDemoProjects();
        }
    }
    
    private void createDemoProjects() {
        projectData.clear();
        projectData.addAll(
            new ProjectItem("1", "PROJ001", "Festival Musique 2025", "Installation", "IN_PROGRESS", 
                          "Festival d'Avignon", "2025-06-01", "2025-07-15", "25000.00"),
            new ProjectItem("2", "PROJ002", "Théâtre Municipal", "Vente", "QUOTED", 
                          "Théâtre National", "2025-03-01", "2025-04-30", "45000.00"),
            new ProjectItem("3", "PROJ003", "Concert Hall Setup", "Location", "CONFIRMED", 
                          "Concert Hall", "2025-02-15", "2025-02-20", "8000.00")
        );
        System.out.println("📝 " + projectData.size() + " projets de démonstration créés");
    }
    
    private void selectProjectById(Object projectId) {
        for (ProjectItem project : projectTable.getItems()) {
            if (project.getId().equals(projectId.toString())) {
                projectTable.getSelectionModel().select(project);
                projectTable.scrollTo(project);
                break;
            }
        }
    }
    
    @Override
    protected void refresh() {
        super.refresh();
        loadProjects();
    }
    
    // ========================================
    // 🛠️ UTILITAIRES; // ========================================
    
    private ReadOnlyObjectProperty<ProjectItem> getTableSelectionProperty() {
        return projectTable.getSelectionModel().selectedItemProperty();
    }
    
    /**
     * Sélectionne et affiche un projet par son nom (pour intégration GlobalSearch)
     */
    public void selectAndViewProject(String projectName) {
        for (ProjectItem project : projectTable.getItems()) {
            if (project.getName().equalsIgnoreCase(projectName) || 
                project.getProjectNumber().equalsIgnoreCase(projectName)) {
                projectTable.getSelectionModel().select(project);
                projectTable.scrollTo(project);
                viewSelectedProject();
                break;
            }
        }
    }
    
    // Classe interne pour les données de projet (temporaire, devrait être dans le model)
    public static class ProjectItem {
        private String id;
        private String projectNumber;
        private String name;
        private String type;
        private String status;
        private String client;
        private String startDate;
        private String endDate;
        private String estimatedAmount;
        
        public ProjectItem(Map<String, Object> data) {
            this.id = String.valueOf(data.getOrDefault("id", ""));
            this.projectNumber = String.valueOf(data.getOrDefault("projectNumber", ""));
            this.name = String.valueOf(data.getOrDefault("name", ""));
            this.type = String.valueOf(data.getOrDefault("type", ""));
            this.status = String.valueOf(data.getOrDefault("status", ""));
            this.client = String.valueOf(data.getOrDefault("client", ""));
            this.startDate = String.valueOf(data.getOrDefault("startDate", ""));
            this.endDate = String.valueOf(data.getOrDefault("endDate", ""));
            this.estimatedAmount = String.valueOf(data.getOrDefault("estimatedAmount", ""));
        }
        
        public ProjectItem(String id, String projectNumber, String name, String type, 
                         String status, String client, String startDate, String endDate, 
                         String estimatedAmount) {
            this.id = id;
            this.projectNumber = projectNumber;
            this.name = name;
            this.type = type;
            this.status = status;
            this.client = client;
            this.startDate = startDate;
            this.endDate = endDate;
            this.estimatedAmount = estimatedAmount;
        }
        
        // Getters
        public String getId() { return id; }
        public String getProjectNumber() { return projectNumber; }
        public String getName() { return name; }
        public String getType() { return type; }
        public String getStatus() { return status; }
        public String getClient() { return client; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public String getEstimatedAmount() { return estimatedAmount; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("projectNumber", projectNumber);
            map.put("name", name);
            map.put("type", type);
            map.put("status", status);
            map.put("client", client);
            map.put("startDate", startDate);
            map.put("endDate", endDate);
            map.put("estimatedAmount", estimatedAmount);
            return map;
        }
    }
}

/**
 * 📝 NOTES D'ARCHITECTURE POUR STANDARDPROJECTMANAGERVIEW
 * 
 * ✅ STANDARDISATION MAJEURE RÉUSSIE :
 * - Structure complexe (BorderPane→VBox→HBox→VBox) → Structure BorderPane uniforme
 * - Double toolbar (toolbar + filterBar) → Toolbar unique standardisée
 * - Boutons custom dispersés → ViewUtils standardisés + pattern uniforme
 * - Code de ~667 lignes → Structure optimisée et plus maintenable
 * 
 * 🎯 AMÉLIORATIONS SPÉCIFIQUES PROJETS :
 * - Filtres adaptés aux besoins métier (Statut, Type, Client, Dates)
 * - DatePickers intégrés harmonieusement dans la toolbar
 * - Actions projet spécialisées (CRUD + Export)
 * - Gestion des données avec fallback démonstration
 * 
 * 🔄 COHÉRENCE AVEC AUTRES MANAGERS :
 * - Même structure toolbar (filtres gauche, actions droite)
 * - Même pattern de recherche multi-champs
 * - Même gestion des dialogs et confirmations
 * - Même intégration DetailPanelContainer
 * 
 * 💡 SPÉCIFICITÉS VENTES & INSTALLATIONS PRÉSERVÉES :
 * - Filtres par dates (début/fin) pour projets
 * - Gestion des statuts projet spécifiques (DRAFT, QUOTED, etc.)
 * - Client et montant estimé dans les colonnes
 * - Double-click pour consultation détaillée
 * - Sélection automatique après création
 */