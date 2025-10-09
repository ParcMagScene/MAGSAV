package com.magsav.gui.entities;

import com.magsav.model.*;
import com.magsav.repo.SimpleEntityRepository;
import com.magsav.util.AppLogger;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

/**
 * Contrôleur simplifié pour la gestion des entités
 */
public class SimpleEntitiesController {

    @FXML private ComboBox<EntityType> cbEntityType;
    @FXML private TextField tfSearch;
    @FXML private TableView<EntityRowModel> tableEntities;
    @FXML private TableColumn<EntityRowModel, String> colType;
    @FXML private TableColumn<EntityRowModel, String> colNom;
    @FXML private TableColumn<EntityRowModel, String> colServices;
    @FXML private TableColumn<EntityRowModel, String> colContactPrincipal;
    @FXML private TableColumn<EntityRowModel, String> colEmail;
    @FXML private TableColumn<EntityRowModel, String> colPhone;
    
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnRefresh;

    private final SimpleEntityRepository repo = new SimpleEntityRepository();
    private final ObservableList<EntityRowModel> data = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupTable();
        setupFilters();
        loadData();
    }

    private void setupTable() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colServices.setCellValueFactory(new PropertyValueFactory<>("services"));
        colContactPrincipal.setCellValueFactory(new PropertyValueFactory<>("contactPrincipal"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        tableEntities.setItems(data);
        
        tableEntities.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean hasSelection = newVal != null;
            btnEdit.setDisable(!hasSelection);
            btnDelete.setDisable(!hasSelection);
        });
    }

    private void setupFilters() {
        cbEntityType.getItems().addAll(EntityType.values());
        cbEntityType.getItems().add(0, null);
        cbEntityType.setPromptText("Tous les types");
        cbEntityType.setOnAction(e -> loadData());

        tfSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData());
    }

    @FXML
    private void onAdd() {
        showInfo("Non implémenté", "Fonction d'ajout non encore implémentée");
    }

    @FXML
    private void onEdit() {
        showInfo("Non implémenté", "Fonction de modification non encore implémentée");
    }

    @FXML
    private void onDelete() {
        EntityRowModel selected = tableEntities.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner une entité à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setContentText("Supprimer l'entité \"" + selected.getNom() + "\" ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                repo.delete(selected.getId());
                loadData();
                showInfo("Suppression réussie", "Entité supprimée avec succès.");
            } catch (Exception e) {
                AppLogger.error("Erreur lors de la suppression", e);
                showError("Erreur", "Impossible de supprimer l'entité.");
            }
        }
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    private void loadData() {
        try {
            data.clear();
            
            List<Entity> entities;
            String searchTerm = tfSearch.getText();
            EntityType typeFilter = cbEntityType.getValue();

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                entities = repo.searchByName(searchTerm.trim());
            } else if (typeFilter != null) {
                entities = repo.findByType(typeFilter);
            } else {
                entities = repo.findAll();
            }

            for (Entity entity : entities) {
                EntityRowModel rowModel = new EntityRowModel(
                    entity.id(),
                    entity.type().getDisplayName(),
                    entity.nom(),
                    "-", // Services (pas encore implémenté)
                    "-", // Contact principal (pas encore implémenté)
                    entity.email() != null ? entity.email() : "-",
                    entity.phone() != null ? entity.phone() : "-"
                );
                data.add(rowModel);
            }

            AppLogger.info("SimpleEntitiesController: {} entités chargées", data.size());

        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des entités", e);
            showError("Erreur", "Impossible de charger les entités : " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Classe pour le modèle de ligne du tableau
    public static class EntityRowModel {
        private final long id;
        private final SimpleStringProperty type;
        private final SimpleStringProperty nom;
        private final SimpleStringProperty services;
        private final SimpleStringProperty contactPrincipal;
        private final SimpleStringProperty email;
        private final SimpleStringProperty phone;

        public EntityRowModel(long id, String type, String nom, String services, 
                             String contactPrincipal, String email, String phone) {
            this.id = id;
            this.type = new SimpleStringProperty(type);
            this.nom = new SimpleStringProperty(nom);
            this.services = new SimpleStringProperty(services);
            this.contactPrincipal = new SimpleStringProperty(contactPrincipal);
            this.email = new SimpleStringProperty(email);
            this.phone = new SimpleStringProperty(phone);
        }

        // Getters
        public long getId() { return id; }
        public String getType() { return type.get(); }
        public String getNom() { return nom.get(); }
        public String getServices() { return services.get(); }
        public String getContactPrincipal() { return contactPrincipal.get(); }
        public String getEmail() { return email.get(); }
        public String getPhone() { return phone.get(); }

        // Properties pour JavaFX
        public SimpleStringProperty typeProperty() { return type; }
        public SimpleStringProperty nomProperty() { return nom; }
        public SimpleStringProperty servicesProperty() { return services; }
        public SimpleStringProperty contactPrincipalProperty() { return contactPrincipal; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty phoneProperty() { return phone; }
    }
}