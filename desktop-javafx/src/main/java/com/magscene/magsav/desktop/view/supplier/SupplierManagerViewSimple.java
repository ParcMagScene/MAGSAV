package com.magscene.magsav.desktop.view.supplier;

import com.magscene.magsav.desktop.view.base.BaseManagerView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue simplifiée de gestion des fournisseurs pour test Phase 2
 */
public class SupplierManagerViewSimple extends BaseManagerView<Object> {
    
    private TableView<SupplierData> supplierTable;
    private ObservableList<SupplierData> supplierList = FXCollections.observableArrayList();
    
    public SupplierManagerViewSimple() {
        super();
        
        // Initialiser les données test
        createTestData();
    }
    
    @Override
    protected String getModuleName() {
        return "Fournisseurs";
    }
    
    @Override
    protected String getViewCssClass() {
        return "supplier-manager";
    }
    
    @Override
    protected Pane createMainContent() {
        VBox mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        
        // Titre
        Label titleLabel = new Label("📋 Gestion des Fournisseurs");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Table des fournisseurs
        createSupplierTable();
        
        mainContainer.getChildren().addAll(titleLabel, supplierTable);
        VBox.setVgrow(supplierTable, Priority.ALWAYS);
        
        return mainContainer;
    }
    
    @Override
    protected void addCustomToolbarItems(ToolBar toolbar) {
        Button importButton = new Button("📄 Importer Catalogue");
        Button servicesButton = new Button("⚙️ Services");
        Button thresholdButton = new Button("💰 Seuils");
        
        toolbar.getItems().addAll(
            new Separator(),
            importButton,
            servicesButton,
            thresholdButton
        );
    }
    
    @Override
    protected void initializeContent() {
        // Les données seront chargées après l'initialisation par createTestData()
        if (supplierList != null && !supplierList.isEmpty()) {
            updateStatus(supplierList.size() + " fournisseur(s) chargé(s)");
        } else {
            updateStatus("Chargement...");
        }
    }
    
    private void createSupplierTable() {
        supplierTable = new TableView<>();
        supplierTable.setItems(supplierList);
        supplierTable.getStyleClass().add("data-table");
        
        // Colonne Nom
        TableColumn<SupplierData, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        
        // Colonne Contact
        TableColumn<SupplierData, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(150);
        
        // Colonne Email
        TableColumn<SupplierData, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(180);
        
        // Colonne Services
        TableColumn<SupplierData, String> servicesCol = new TableColumn<>("Services");
        servicesCol.setCellValueFactory(new PropertyValueFactory<>("services"));
        servicesCol.setPrefWidth(120);
        
        // Colonne Statut
        TableColumn<SupplierData, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(80);
        
        supplierTable.getColumns().addAll(nameCol, contactCol, emailCol, servicesCol, statusCol);
    }
    
    private void createTestData() {
        supplierList.addAll(
            new SupplierData("SonoMax Pro", "Jean Dupont", "jean@sonomax.fr", "SAV, RMA, Pièces", "✅ Actif"),
            new SupplierData("Éclairage Scène", "Marie Martin", "marie@eclairage.com", "Équipements", "✅ Actif"),
            new SupplierData("TechService Plus", "Pierre Durand", "pierre@techservice.fr", "SAV, RMA", "❌ Inactif")
        );
        // Mettre à jour le statut après chargement
        Platform.runLater(() -> updateStatus(supplierList.size() + " fournisseur(s) chargé(s)"));
    }
    
    // Classe interne pour les données test
    public static class SupplierData {
        private String name;
        private String contact;
        private String email;
        private String services;
        private String status;
        
        public SupplierData(String name, String contact, String email, String services, String status) {
            this.name = name;
            this.contact = contact;
            this.email = email;
            this.services = services;
            this.status = status;
        }
        
        // Getters pour PropertyValueFactory
        public String getName() { return name; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public String getServices() { return services; }
        public String getStatus() { return status; }
    }
    
    // Méthodes abstraites du parent
    @Override
    protected void handleAdd() {
        updateStatus("Ajout d'un nouveau fournisseur...");
        showAlert("Fonctionnalité", "Ajout de fournisseur - À implémenter");
    }
    
    @Override
    protected void handleEdit() {
        SupplierData selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun fournisseur sélectionné");
            return;
        }
        updateStatus("Modification de " + selected.getName());
        showAlert("Fonctionnalité", "Modification de fournisseur - À implémenter");
    }
    
    @Override
    protected void handleDelete() {
        SupplierData selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun fournisseur sélectionné");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le fournisseur");
        confirm.setContentText("Confirmer la suppression de " + selected.getName() + " ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                supplierList.remove(selected);
                updateStatus("Fournisseur supprimé");
            }
        });
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void refresh() {
        super.refresh();
        updateStatus("Données rafraîchies");
    }
}