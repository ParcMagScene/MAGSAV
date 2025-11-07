package com.magscene.magsav.desktop.view.salesinstallation;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.dialog.salesinstallation.ProjectDialog;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.AlertUtil;
import com.magscene.magsav.desktop.MagsavDesktopApplication;
import com.magscene.magsav.desktop.component.DetailPanelContainer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    
    // Controles de filtrage
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
        
        // Forcer les couleurs des champs de recherche
        MagsavDesktopApplication.forceSearchFieldColors(searchField);
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
        
        TableColumn<Map<String, Object>, String> numberCol = new TableColumn<>("N° Projet");
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
        
        TableColumn<Map<String, Object>, String> startDateCol = new TableColumn<>("Date debut");
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
        
        TableColumn<Map<String, Object>, String> amountCol = new TableColumn<>("Montant estime");
        amountCol.setCellValueFactory(cellData -> {
            Object value = cellData.getValue().get("estimatedAmount");
            return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() + " €" : "");
        });
        amountCol.setPrefWidth(120);
        
        projectTable.getColumns().addAll(idCol, numberCol, nameCol, typeCol, statusCol, 
                                       clientCol, startDateCol, endDateCol, amountCol);
        
        // Style de sélection uniforme #142240
        projectTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection prioritaire (#142240)
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                } else {
                    // Style par défaut
                    row.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            // Double-clic pour éditer
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editProject();
                }
            });
            
            return row;
        });
        
        // Controles de filtrage
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "DRAFT", "QUOTED", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ON_HOLD");
        statusFilter.setValue("Tous");
        
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Vente", "Installation", "Location", "Prestation", "Maintenance");
        typeFilter.setValue("Tous");
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher projets...");
        searchField.setPrefWidth(200);
        searchField.setStyle("-fx-background-color: #142240; -fx-text-fill: #7DD3FC; -fx-border-color: #7DD3FC; -fx-border-radius: 4;");
        
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Date debut");
        
        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("Date fin");
    }

    private void layoutComponents() {
        // Configuration de base
        this.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");
        
        // Header
        VBox header = createHeader();
        
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
        statusLabel.setStyle("-fx-text-fill: #6B71F2;");
        Label typeLabel = new Label("Type:");
        typeLabel.setStyle("-fx-text-fill: #6B71F2;");
        Label searchLabel = new Label("Recherche:");
        searchLabel.setStyle("-fx-text-fill: #6B71F2;");
        Label dateLabel = new Label("Periode:");
        dateLabel.setStyle("-fx-text-fill: #6B71F2;");
        
        Button filterButton = new Button("Filtrer");
        Button clearButton = new Button("Effacer");
        
        filterButton.setOnAction(e -> applyFilters());
        clearButton.setOnAction(e -> clearFilters());
        
        filterBar.getChildren().addAll(
            searchLabel, searchField,
            statusLabel, statusFilter,
            typeLabel, typeFilter,
            dateLabel, startDatePicker, endDatePicker,
            filterButton, clearButton
        );
        
        // Enveloppement du tableau dans DetailPanelContainer pour le volet de détail
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(projectTable);
        
        // Le DetailPanelContainer gère automatiquement l'affichage pour les ProjectItem
        
        // Layout principal
        VBox topContainer = new VBox(header, toolbar, filterBar);
        
        setTop(topContainer);
        setCenter(containerWithDetail);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("💼 Ventes & Installations");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        header.getChildren().add(title);
        return header;
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
                    // Autres touches non gerees
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
                
                // Recharger la liste complete des projets
                loadProjects();
                
                // Selectionner le projet cree dans la table apres un court delai
                if (createdProject != null && createdProject.containsKey("id")) {
                    javafx.application.Platform.runLater(() -> {
                        selectProjectById(createdProject.get("id"));
                    });
                }
                
                AlertUtil.showInfo("Succes", "Projet cree avec succes !\nNom: " + projectData.get("name") + 
                                 "\nClient: " + projectData.get("clientName"));
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de creer le projet: " + e.getMessage());
            }
        });
    }

    private void editProject() {
        Map<String, Object> selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            AlertUtil.showWarning("Aucune selection", "Veuillez selectionner un projet a modifier");
            return;
        }
        
        ProjectDialog dialog = new ProjectDialog("Modifier Projet", selectedProject);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(projectData -> {
            try {
                Long id = Long.valueOf(selectedProject.get("id").toString());
                apiService.update("projects", id, projectData);
                loadProjects();
                AlertUtil.showInfo("Succes", "Projet modifie avec succes");
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de modifier le projet: " + e.getMessage());
            }
        });
    }

    private void deleteProject() {
        Map<String, Object> selectedProject = projectTable.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            AlertUtil.showWarning("Aucune selection", "Veuillez selectionner un projet a supprimer");
            return;
        }
        
        boolean confirmed = AlertUtil.showConfirmation("Confirmation", 
            "Etes-vous sur de vouloir supprimer ce projet ?\n\n" +
            "Nom: " + selectedProject.get("name") + "\n" +
            "N°: " + selectedProject.get("projectNumber"));
            
        if (confirmed) {
            try {
                Long id = Long.valueOf(selectedProject.get("id").toString());
                apiService.delete("projects", id);
                loadProjects();
                AlertUtil.showInfo("Succes", "Projet supprime avec succes");
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
            
            // Filtrage par periode d'installation
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
    
    /**
     * Sélectionne un projet par nom et ouvre sa fiche de modification
     * Méthode publique appelée depuis la recherche globale
     */
    public void selectAndViewProject(String projectName) {
        System.out.println("🔍 Recherche projet: " + projectName + " dans " + projectData.size() + " éléments");
        
        // Attendre que les données soient chargées si nécessaire
        if (projectData.isEmpty()) {
            System.out.println("⏳ Données projet non chargées, attente...");
            scheduleProjectDataCheck(projectName, 0);
            return;
        }
        
        javafx.application.Platform.runLater(() -> {
            // Rechercher le projet dans la liste
            boolean found = false;
            for (Map<String, Object> project : projectData) {
                Object nameObj = project.get("name");
                if (nameObj != null && 
                    nameObj.toString().toLowerCase().contains(projectName.toLowerCase())) {
                    // Sélectionner le projet dans la table
                    projectTable.getSelectionModel().select(project);
                    projectTable.scrollTo(project);
                    
                    System.out.println("✅ Projet trouvé et sélectionné: " + nameObj.toString());
                    
                    // Ouvrir automatiquement la fiche de modification avec délai
                    javafx.application.Platform.runLater(() -> {
                        try {
                            Thread.sleep(200); // Petit délai pour la sélection
                            editProject();
                        } catch (InterruptedException e) {
                            editProject();
                        }
                    });
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("❌ Projet non trouvé: " + projectName);
            }
        });
    }
    
    /**
     * Vérifie périodiquement si les données projet sont chargées pour la sélection automatique
     */
    private void scheduleProjectDataCheck(String projectName, int attempt) {
        if (attempt > 10) { // Maximum 10 tentatives (5 secondes)
            System.out.println("❌ Timeout: Projet non trouvé après 10 tentatives: " + projectName);
            return;
        }
        
        javafx.application.Platform.runLater(() -> {
            if (!projectData.isEmpty()) {
                System.out.println("✅ Données projet chargées, nouvelle tentative de sélection");
                selectAndViewProject(projectName);
            } else {
                // Réessayer après 500ms
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        scheduleProjectDataCheck(projectName, attempt + 1);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                }).start();
            }
        });
    }

    /**
     * Classe wrapper pour les projets/ventes avec implémentation DetailPanelProvider
     */
    public static class ProjectItem implements com.magscene.magsav.desktop.component.DetailPanelProvider {
        private final Map<String, Object> data;

        public ProjectItem(Map<String, Object> data) {
            this.data = data != null ? data : Map.of();
        }

        @Override
        public String getDetailTitle() {
            Object name = data.get("name");
            return name != null ? name.toString() : "Projet sans nom";
        }

        @Override
        public String getDetailSubtitle() {
            StringBuilder subtitle = new StringBuilder();
            
            Object type = data.get("type");
            if (type != null) {
                subtitle.append("📋 ").append(type.toString());
            }
            
            Object status = data.get("status");
            if (status != null) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append(getStatusIcon(status.toString())).append(" ").append(status.toString());
            }
            
            Object client = data.get("client");
            if (client != null) {
                if (subtitle.length() > 0) subtitle.append(" • ");
                subtitle.append("Client: ").append(client.toString());
            }
            
            return subtitle.toString();
        }

        @Override
        public javafx.scene.image.Image getDetailImage() {
            // Pas d'image spécifique pour les projets/ventes
            return null;
        }

        @Override
        public String getQRCodeData() {
            return ""; // Pas de QR code pour les projets/ventes
        }

        @Override
        public javafx.scene.layout.VBox getDetailInfoContent() {
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(8);
            
            Object client = data.get("client");
            if (client != null) {
                content.getChildren().add(com.magscene.magsav.desktop.component.DetailPanel.createInfoRow("Client", client.toString()));
            }
            
            Object status = data.get("status");
            if (status != null) {
                content.getChildren().add(com.magscene.magsav.desktop.component.DetailPanel.createInfoRow("Statut", getStatusIcon(status.toString()) + " " + status.toString()));
            }
            
            Object amount = data.get("totalAmount");
            if (amount != null) {
                content.getChildren().add(com.magscene.magsav.desktop.component.DetailPanel.createInfoRow("Montant", String.format("%.2f €", ((Number) amount).doubleValue())));
            }
            
            Object startDate = data.get("startDate");
            if (startDate != null) {
                content.getChildren().add(com.magscene.magsav.desktop.component.DetailPanel.createInfoRow("Date début", startDate.toString()));
            }
            
            Object endDate = data.get("endDate");
            if (endDate != null) {
                content.getChildren().add(com.magscene.magsav.desktop.component.DetailPanel.createInfoRow("Date fin", endDate.toString()));
            }
            
            return content;
        }

        @Override
        public String getDetailId() {
            Object id = data.get("id");
            return id != null ? id.toString() : "";
        }

        private String getStatusIcon(String status) {
            if (status == null) return "❓";
            return switch (status.toUpperCase()) {
                case "EN_COURS", "IN_PROGRESS" -> "⚙️";
                case "TERMINE", "COMPLETED" -> "✅";
                case "EN_ATTENTE", "PENDING" -> "⏳";
                case "ANNULE", "CANCELLED" -> "❌";
                default -> "📋";
            };
        }
    }
}

