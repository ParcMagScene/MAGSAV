package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.dialog.PersonnelDialog;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.CompletableFuture;
/**
 * Interface JavaFX complÃƒÂ¨te pour la gestion du personnel
 * FonctionnalitÃƒÂ©s : tableau dÃƒÂ©taillÃƒÂ©, recherche, filtres, CRUD, statistiques
 */
public class PersonnelManagerView extends VBox {
    
    private final ApiService apiService;
    private TableView<PersonnelItem> personnelTable;
    private ObservableList<PersonnelItem> personnelData;
    private TextField searchField;
    private ComboBox<String> typeFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> departmentFilter;
    private Label statsLabel;
    private ProgressIndicator loadingIndicator;
    
    public PersonnelManagerView(ApiService apiService) {
        this.apiService = apiService;
        this.personnelData = FXCollections.observableArrayList();
        initializeUI();
        loadPersonnelData();
    }
    
    private void initializeUI() {
        this.setSpacing(15);
        this.setPadding(new Insets(20));
        
        // Header avec titre et statistiques
        VBox header = createHeader();
        
        // Barre d'outils avec recherche et filtres
        HBox toolbar = createToolbar();
        
        // Zone principale avec tableau
        VBox tableContainer = createTableContainer();
        
        // Footer avec boutons d'actions
        HBox footer = createFooter();
        
        this.getChildren().addAll(header, toolbar, tableContainer, footer);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        // Titre principal
        Label titleLabel = new Label("Ã°Å¸â€œâ€¹ Gestion du Personnel");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.DARKBLUE);
        
        // Statistiques
        statsLabel = new Label("Chargement des statistiques...");
        statsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        statsLabel.setTextFill(Color.GRAY);
        
        header.getChildren().addAll(titleLabel, statsLabel);
        return header;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10, 0, 10, 0));
        
        // Champ de recherche
        Label searchLabel = new Label("Ã°Å¸â€Â Recherche:");
        searchField = new TextField();
        searchField.setPromptText("Nom, prÃƒÂ©nom, email...");
        searchField.setPrefWidth(250);
        searchField.textProperty().addListener((obs, oldText, newText) -> filterPersonnelData());
        
        // Filtres
        Label typeLabel = new Label("Type:");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "EmployÃƒÂ©", "Freelance", "Stagiaire", "IntÃƒÂ©rimaire");
        typeFilter.setValue("Tous");
        typeFilter.setOnAction(e -> filterPersonnelData());
        
        Label statusLabel = new Label("Statut:");
        statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "Actif", "Inactif", "En congÃƒÂ©", "TerminÃƒÂ©");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(e -> filterPersonnelData());
        
        Label deptLabel = new Label("DÃƒÂ©partement:");
        departmentFilter = new ComboBox<>();
        departmentFilter.getItems().add("Tous");
        departmentFilter.setValue("Tous");
        departmentFilter.setOnAction(e -> filterPersonnelData());
        
        // Bouton rafraÃƒÂ®chir
        Button refreshButton = new Button("Ã°Å¸â€â€ž Actualiser");
        refreshButton.setOnAction(e -> loadPersonnelData());
        
        toolbar.getChildren().addAll(
            searchLabel, searchField,
            new Separator(),
            typeLabel, typeFilter,
            statusLabel, statusFilter,
            deptLabel, departmentFilter,
            new Separator(),
            refreshButton
        );
        
        return toolbar;
    }
    
    private VBox createTableContainer() {
        VBox container = new VBox(10);
        
        // Indicateur de chargement
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(50, 50);
        
        // Configuration du tableau
        personnelTable = new TableView<>();
        personnelTable.setItems(personnelData);
        personnelTable.setRowFactory(tv -> {
            TableRow<PersonnelItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editPersonnel(row.getItem());
                }
            });
            return row;
        });
        
        createTableColumns();
        
        // Style du tableau
        personnelTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        personnelTable.setPrefHeight(400);
        
        container.getChildren().addAll(loadingIndicator, personnelTable);
        return container;
    }
    
    private void createTableColumns() {
        // Colonne ID
        TableColumn<PersonnelItem, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        // Colonne Nom complet
        TableColumn<PersonnelItem, String> nameCol = new TableColumn<>("Nom complet");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);
        
        // Colonne Email
        TableColumn<PersonnelItem, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        // Colonne TÃƒÂ©lÃƒÂ©phone
        TableColumn<PersonnelItem, String> phoneCol = new TableColumn<>("TÃƒÂ©lÃƒÂ©phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(130);
        
        // Colonne Type
        TableColumn<PersonnelItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(100);
        typeCol.setCellFactory(column -> new TableCell<PersonnelItem, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(type);
                    // Couleurs selon le type
                    switch (type) {
                        case "EmployÃƒÂ©":
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;");
                            break;
                        case "Freelance":
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0;");
                            break;
                        case "Stagiaire":
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                            break;
                        case "IntÃƒÂ©rimaire":
                            setStyle("-fx-background-color: #fce4ec; -fx-text-fill: #ad1457;");
                            break;
                    }
                }
            }
        });
        
        // Colonne Statut
        TableColumn<PersonnelItem, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(100);
        statusCol.setCellFactory(column -> new TableCell<PersonnelItem, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(status);
                    // Couleurs selon le statut
                    switch (status) {
                        case "Actif":
                            setStyle("-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;");
                            break;
                        case "Inactif":
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                            break;
                        case "En congÃƒÂ©":
                            setStyle("-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;");
                            break;
                        case "TerminÃƒÂ©":
                            setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2;");
                            break;
                    }
                }
            }
        });
        
        // Colonne Poste
        TableColumn<PersonnelItem, String> jobCol = new TableColumn<>("Poste");
        jobCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));
        jobCol.setPrefWidth(150);
        
        // Colonne DÃƒÂ©partement
        TableColumn<PersonnelItem, String> deptCol = new TableColumn<>("DÃƒÂ©partement");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        deptCol.setPrefWidth(120);
        
        // Colonne Date d'embauche
        TableColumn<PersonnelItem, String> hireDateCol = new TableColumn<>("Embauche");
        hireDateCol.setCellValueFactory(new PropertyValueFactory<>("hireDate"));
        hireDateCol.setPrefWidth(100);
        
        personnelTable.getColumns().addAll(
            idCol, nameCol, emailCol, phoneCol, 
            typeCol, statusCol, jobCol, deptCol, hireDateCol
        );
    }
    
    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(15, 0, 0, 0));
        
        Button addButton = new Button("Ã¢Å¾â€¢ Nouveau Personnel");
        addButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setPrefWidth(150);
        addButton.setOnAction(e -> createNewPersonnel());
        
        Button editButton = new Button("Ã¢Å“ÂÃ¯Â¸Â Modifier");
        editButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        editButton.setPrefWidth(120);
        editButton.setOnAction(e -> {
            PersonnelItem selected = personnelTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editPersonnel(selected);
            } else {
                showAlert("SÃƒÂ©lection requise", "Veuillez sÃƒÂ©lectionner un membre du personnel ÃƒÂ  modifier.");
            }
        });
        
        Button deleteButton = new Button("Ã°Å¸â€”â€˜Ã¯Â¸Â Supprimer");
        deleteButton.setStyle("-fx-background-color: #F44336; -fx-text-fill: white; -fx-font-weight: bold;");
        deleteButton.setPrefWidth(120);
        deleteButton.setOnAction(e -> {
            PersonnelItem selected = personnelTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deletePersonnel(selected);
            } else {
                showAlert("SÃƒÂ©lection requise", "Veuillez sÃƒÂ©lectionner un membre du personnel ÃƒÂ  supprimer.");
            }
        });
        
        footer.getChildren().addAll(addButton, editButton, deleteButton);
        return footer;
    }
    
    private void loadPersonnelData() {
        loadingIndicator.setVisible(true);
        personnelTable.setDisable(true);
        
        CompletableFuture<List<Object>> future = apiService.getAllPersonnel();
        future.thenAccept(personnelList -> {
            Platform.runLater(() -> {
                personnelData.clear();
                
                for (Object item : personnelList) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> personnelMap = (Map<String, Object>) item;
                        PersonnelItem personnelItem = createPersonnelItem(personnelMap);
                        personnelData.add(personnelItem);
                    }
                }
                
                updateDepartmentFilter();
                updateStatistics();
                loadingIndicator.setVisible(false);
                personnelTable.setDisable(false);
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                loadingIndicator.setVisible(false);
                personnelTable.setDisable(false);
                showAlert("Erreur de chargement", 
                    "Impossible de charger les donnÃƒÂ©es du personnel : " + throwable.getMessage());
            });
            return null;
        });
    }
    
    private PersonnelItem createPersonnelItem(Map<String, Object> personnelMap) {
        PersonnelItem item = new PersonnelItem();
        
        item.setId(String.valueOf(personnelMap.get("id")));
        item.setFirstName(getStringValue(personnelMap, "firstName"));
        item.setLastName(getStringValue(personnelMap, "lastName"));
        item.setFullName(item.getFirstName() + " " + item.getLastName());
        item.setEmail(getStringValue(personnelMap, "email"));
        item.setPhone(getStringValue(personnelMap, "phone"));
        
        // Gestion des ÃƒÂ©numÃƒÂ©rations
        String type = getStringValue(personnelMap, "type");
        item.setType(convertTypeToDisplay(type));
        
        String status = getStringValue(personnelMap, "status");
        item.setStatus(convertStatusToDisplay(status));
        
        item.setJobTitle(getStringValue(personnelMap, "jobTitle"));
        item.setDepartment(getStringValue(personnelMap, "department"));
        
        // Date d'embauche
        String hireDate = getStringValue(personnelMap, "hireDate");
        if (hireDate != null && !hireDate.isEmpty()) {
            item.setHireDate(hireDate);
        }
        
        item.setNotes(getStringValue(personnelMap, "notes"));
        
        // Stockage de la map originale pour les opÃƒÂ©rations
        item.setOriginalData(personnelMap);
        
        return item;
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    private String convertTypeToDisplay(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "EMPLOYEE": return "EmployÃƒÂ©";
            case "FREELANCE": return "Freelance";
            case "INTERN": return "Stagiaire";
            case "TEMPORARY": return "IntÃƒÂ©rimaire";
            default: return type;
        }
    }
    
    private String convertStatusToDisplay(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "ACTIVE": return "Actif";
            case "INACTIVE": return "Inactif";
            case "ON_LEAVE": return "En congÃƒÂ©";
            case "TERMINATED": return "TerminÃƒÂ©";
            default: return status;
        }
    }
    
    private void updateDepartmentFilter() {
        // RÃƒÂ©cupÃƒÂ©rer la liste des dÃƒÂ©partements uniques
        departmentFilter.getItems().clear();
        departmentFilter.getItems().add("Tous");
        
        personnelData.stream()
            .map(PersonnelItem::getDepartment)
            .filter(dept -> dept != null && !dept.trim().isEmpty())
            .distinct()
            .sorted()
            .forEach(dept -> departmentFilter.getItems().add(dept));
        
        if (!departmentFilter.getItems().contains(departmentFilter.getValue())) {
            departmentFilter.setValue("Tous");
        }
    }
    
    private void updateStatistics() {
        long total = personnelData.size();
        long actif = personnelData.stream().filter(p -> "Actif".equals(p.getStatus())).count();
        long employes = personnelData.stream().filter(p -> "EmployÃƒÂ©".equals(p.getType())).count();
        long freelances = personnelData.stream().filter(p -> "Freelance".equals(p.getType())).count();
        
        String stats = String.format(
            "Ã°Å¸â€œÅ  Total: %d Ã¢â‚¬Â¢ Actifs: %d Ã¢â‚¬Â¢ EmployÃƒÂ©s: %d Ã¢â‚¬Â¢ Freelances: %d",
            total, actif, employes, freelances
        );
        statsLabel.setText(stats);
    }
    
    private void filterPersonnelData() {
        // TODO: ImplÃƒÂ©menter le filtrage
        // Pour l'instant, on affiche toutes les donnÃƒÂ©es
    }
    
    private void createNewPersonnel() {
        PersonnelDialog dialog = new PersonnelDialog(null, apiService);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(personnelData -> {
            // Rechargement des donnÃƒÂ©es aprÃƒÂ¨s crÃƒÂ©ation
            loadPersonnelData();
        });
    }
    
    private void editPersonnel(PersonnelItem personnel) {
        PersonnelDialog dialog = new PersonnelDialog(personnel.getOriginalData(), apiService);
        Optional<Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(personnelData -> {
            // Rechargement des donnÃƒÂ©es aprÃƒÂ¨s modification
            loadPersonnelData();
        });
    }
    
    private void deletePersonnel(PersonnelItem personnel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le membre du personnel");
        alert.setContentText("ÃƒÅ tes-vous sÃƒÂ»r de vouloir supprimer " + personnel.getFullName() + " ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Long personnelId = Long.valueOf(personnel.getId());
            
            CompletableFuture<Void> future = apiService.deletePersonnel(personnelId);
            future.thenRun(() -> Platform.runLater(() -> {
                showAlert("SuccÃƒÂ¨s", "Membre du personnel supprimÃƒÂ© avec succÃƒÂ¨s.");
                loadPersonnelData();
            })).exceptionally(throwable -> {
                Platform.runLater(() -> 
                    showAlert("Erreur", "Erreur lors de la suppression : " + throwable.getMessage()));
                return null;
            });
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Classe interne pour reprÃƒÂ©senter un ÃƒÂ©lÃƒÂ©ment du personnel dans le tableau
    public static class PersonnelItem {
        private SimpleStringProperty id = new SimpleStringProperty();
        private SimpleStringProperty firstName = new SimpleStringProperty();
        private SimpleStringProperty lastName = new SimpleStringProperty();
        private SimpleStringProperty fullName = new SimpleStringProperty();
        private SimpleStringProperty email = new SimpleStringProperty();
        private SimpleStringProperty phone = new SimpleStringProperty();
        private SimpleStringProperty type = new SimpleStringProperty();
        private SimpleStringProperty status = new SimpleStringProperty();
        private SimpleStringProperty jobTitle = new SimpleStringProperty();
        private SimpleStringProperty department = new SimpleStringProperty();
        private SimpleStringProperty hireDate = new SimpleStringProperty();
        private SimpleStringProperty notes = new SimpleStringProperty();
        private Map<String, Object> originalData;
        
        // Getters et setters
        public String getId() { return id.get(); }
        public void setId(String id) { this.id.set(id); }
        public SimpleStringProperty idProperty() { return id; }
        
        public String getFirstName() { return firstName.get(); }
        public void setFirstName(String firstName) { this.firstName.set(firstName); }
        
        public String getLastName() { return lastName.get(); }
        public void setLastName(String lastName) { this.lastName.set(lastName); }
        
        public String getFullName() { return fullName.get(); }
        public void setFullName(String fullName) { this.fullName.set(fullName); }
        
        public String getEmail() { return email.get(); }
        public void setEmail(String email) { this.email.set(email); }
        
        public String getPhone() { return phone.get(); }
        public void setPhone(String phone) { this.phone.set(phone); }
        
        public String getType() { return type.get(); }
        public void setType(String type) { this.type.set(type); }
        
        public String getStatus() { return status.get(); }
        public void setStatus(String status) { this.status.set(status); }
        
        public String getJobTitle() { return jobTitle.get(); }
        public void setJobTitle(String jobTitle) { this.jobTitle.set(jobTitle); }
        
        public String getDepartment() { return department.get(); }
        public void setDepartment(String department) { this.department.set(department); }
        
        public String getHireDate() { return hireDate.get(); }
        public void setHireDate(String hireDate) { this.hireDate.set(hireDate); }
        
        public String getNotes() { return notes.get(); }
        public void setNotes(String notes) { this.notes.set(notes); }
        
        public Map<String, Object> getOriginalData() { return originalData; }
        public void setOriginalData(Map<String, Object> originalData) { this.originalData = originalData; }
    }
}

