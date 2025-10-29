package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.dialog.salesinstallation.ProjectDialog;
import com.magscene.magsav.desktop.util.AlertUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * Vue pour la gestion des projets (Ventes & Installations)
 */
public class ProjectManagerView extends BorderPane {

    private TableView<Map<String, Object>> projectTable;
    private ObservableList<Map<String, Object>> projectData;
    private ApiService apiService;
    
    // ContrÃƒÂ´les de filtrage
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;
    private TextField searchField;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;

    public ProjectManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.projectData = FXCollections.observableArrayList();
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        loadProjects();
    }

    private void initializeComponents() {
        // Tableau des projets
        projectTable = new TableView<>();
        projectTable.setItems(projectData);
        
        // Colonnes du tableau
        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("id");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        idCol.setPrefWidth(60);
        
        TableColumn<Map<String, Object>, String> numberCol = new TableColumn<>("NÃ‚Â° Projet");
        numberCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("projectNumber");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        numberCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("name");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        nameCol.setPrefWidth(200);
        
        TableColumn<Map<String, Object>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("type");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        typeCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("status");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        statusCol.setPrefWidth(120);
        
        TableColumn<Map<String, Object>, String> clientCol = new TableColumn<>("Client");
        clientCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("clientName");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        clientCol.setPrefWidth(150);
        
        TableColumn<Map<String, Object>, String> startDateCol = new TableColumn<>("Date dÃƒÂ©but");
        startDateCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("startDate");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        startDateCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> endDateCol = new TableColumn<>("Date fin");
        endDateCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("endDate");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "");
        });
        endDateCol.setPrefWidth(100);
        
        TableColumn<Map<String, Object>, String> amountCol = new TableColumn<>("Montant estimÃƒÂ©");
        amountCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("estimatedAmount");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() + " Ã¢â€šÂ¬" : "");
        });
        amountCol.setPrefWidth(120);
        
        projectTable.getColumns().addAll(idCol, numberCol, nameCol, typeCol, statusCol, 
                                       clientCol, startDateCol, endDateCol, amountCol);
        
        // ContrÃƒÂ´les de filtrage
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "DRAFT", "QUOTED", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ON_HOLD");
        statusFilter.setValue("Tous");
        
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "SALE", "INSTALLATION", "RENTAL", "MAINTENANCE", "EVENT", "PROJECT");
        typeFilter.setValue("Tous");
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher projets...");
        searchField.setPrefWidth(200);
        
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Date dÃƒÂ©but");
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Date fin");
    }

    private void layoutComponents() {
        // Barre d'outils
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        
        Button addButton = new Button("Nouveau Projet");
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        Button refreshButton = new Button("Actualiser");
        
        addButton.setOnAction(e -> addProject());
        editButton.setOnAction(e -> editProject());
        deleteButton.setOnAction(e -> deleteProject());
        refreshButton.setOnAction(e -> loadProjects());
        
        toolbar.getChildren().addAll(addButton, editButton, deleteButton, 
                                   new Separator(), refreshButton);
        
        // Barre de filtrage
        HBox filterBar = new HBox(10);
        filterBar.setPadding(new Insets(0, 10, 10, 10));
        
        Label statusLabel = new Label("Statut:");
        Label typeLabel = new Label("Type:");
        Label searchLabel = new Label("Recherche:");
        Label dateLabel = new Label("PÃƒÂ©riode:");
        
        Button filterButton = new Button("Filtrer");
        Button clearButton = new Button("Effacer");
        
        filterButton.setOnAction(e -> applyFilters());
        clearButton.setOnAction(e -> clearFilters());
        
        filterBar.getChildren().addAll(
            statusLabel, statusFilter,
            typeLabel, typeFilter,
            searchLabel, searchField,
            dateLabel, startDatePicker, endDatePicker,
            filterButton, clearButton
        );
        
        // Layout principal
        VBox topContainer = new VBox(toolbar, filterBar);
        
        setTop(topContainer);
        setCenter(projectTable);
    }

    private void setupEventHandlers() {
        // Double-clic pour modifier
        projectTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                editProject();
            }
        });
        
        // Raccourcis clavier
        projectTable.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DELETE:
                    deleteProject();
                    break;
                case F5:
                    loadProjects();
                    break;
                default:
                    // Autres touches non gÃƒÂ©rÃƒÂ©es
                    break;
            }
        });
    }

    private void loadProjects() {
        try {
            List<Map<String, Object>> projects = apiService.getAll("projects");
            projectData.clear();
            projectData.addAll(projects);
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Impossible de charger les projets: " + e.getMessage());
        }
    }

    private void addProject() {
        ProjectDialog dialog = new ProjectDialog("Nouveau Projet", null);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(projectData -> {
            try {
                Map<String, Object> createdProject = apiService.create("projects", projectData);
                
                // Effacer tous les filtres pour s'assurer que le nouveau projet est visible
                clearFilters();
                
                // Recharger la liste complÃƒÂ¨te des projets
                loadProjects();
                
                // SÃƒÂ©lectionner le projet crÃƒÂ©ÃƒÂ© dans la table aprÃƒÂ¨s un court dÃƒÂ©lai
                if (createdProject != null && createdProject.containsKey("id")) {
                    javafx.application.Platform.runLater(() -> {
                        selectProjectById(createdProject.get("id"));
                    });
                }
                
                AlertUtil.showInfo("SuccÃƒÂ¨s", "Projet crÃƒÂ©ÃƒÂ© avec succÃƒÂ¨s !\nNom: " + projectData.get("name") + 
                                 "\nClient: " + projectData.get("clientName"));
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de crÃƒÂ©er le projet: " + e.getMessage());
            }
        });
    }

    private void editProject() {
        Map<String, Object> selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            AlertUtil.showWarning("Aucune sÃƒÂ©lection", "Veuillez sÃƒÂ©lectionner un projet ÃƒÂ  modifier");
            return;
        }
        
        ProjectDialog dialog = new ProjectDialog("Modifier Projet", selectedProject);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(projectData -> {
            try {
                Long id = Long.valueOf(selectedProject.get("id").toString());
                apiService.update("projects", id, projectData);
                loadProjects();
                AlertUtil.showInfo("SuccÃƒÂ¨s", "Projet modifiÃƒÂ© avec succÃƒÂ¨s");
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de modifier le projet: " + e.getMessage());
            }
        });
    }

    private void deleteProject() {
        Map<String, Object> selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            AlertUtil.showWarning("Aucune sÃƒÂ©lection", "Veuillez sÃƒÂ©lectionner un projet ÃƒÂ  supprimer");
            return;
        }
        
        boolean confirmed = AlertUtil.showConfirmation("Confirmation", 
            "ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer ce projet ?\n\n" + 
            "Nom: " + selectedProject.get("name") + "\n" +
            "NÃ‚Â°: " + selectedProject.get("projectNumber"));
            
        if (confirmed) {
            try {
                Long id = Long.valueOf(selectedProject.get("id").toString());
                apiService.delete("projects", id);
                loadProjects();
                AlertUtil.showInfo("SuccÃƒÂ¨s", "Projet supprimÃƒÂ© avec succÃƒÂ¨s");
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de supprimer le projet: " + e.getMessage());
            }
        }
    }

    private void applyFilters() {
        try {
            
            // Filtrage par recherche globale
            if (!searchField.getText().trim().isEmpty()) {
                List<Map<String, Object>> filtered = apiService.search("projects/search", 
                    Map.of("q", searchField.getText().trim()));
                projectData.clear();
                projectData.addAll(filtered);
                return;
            }
            
            // Filtrage par statut
            String status = statusFilter.getValue();
            if (!"Tous".equals(status)) {
                List<Map<String, Object>> filtered = apiService.getAll("projects/status/" + status);
                projectData.clear();
                projectData.addAll(filtered);
                return;
            }
            
            // Filtrage par type
            String type = typeFilter.getValue();
            if (!"Tous".equals(type)) {
                List<Map<String, Object>> filtered = apiService.getAll("projects/type/" + type);
                projectData.clear();
                projectData.addAll(filtered);
                return;
            }
            
            // Filtrage par pÃƒÂ©riode d'installation
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (startDate != null && endDate != null) {
                List<Map<String, Object>> filtered = apiService.search("projects/installation-period", 
                    Map.of("startDate", startDate.toString(), "endDate", endDate.toString()));
                projectData.clear();
                projectData.addAll(filtered);
                return;
            }
            
            // Aucun filtre, recharger tous les projets
            loadProjects();
            
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Erreur lors du filtrage: " + e.getMessage());
        }
    }

    private void clearFilters() {
        statusFilter.setValue("Tous");
        typeFilter.setValue("Tous");
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadProjects();
    }
    
    private void selectProjectById(Object projectId) {
        if (projectId == null) return;
        
        for (Map<String, Object> project : projectData) {
            if (projectId.toString().equals(project.get("id").toString())) {
                projectTable.getSelectionModel().select(project);
                projectTable.scrollTo(project);
                break;
            }
        }
    }
}

