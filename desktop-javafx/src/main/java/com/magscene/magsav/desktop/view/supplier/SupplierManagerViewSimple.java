package com.magscene.magsav.desktop.view.supplier;

import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

/**
 * Vue simplifiée de gestion des fournisseurs pour test Phase 2
 */
public class SupplierManagerViewSimple extends BaseManagerView<Object> {

    private TableView<SupplierData> supplierTable;
    private ObservableList<SupplierData> supplierList = FXCollections.observableArrayList();

    private com.magscene.magsav.desktop.service.ApiService apiService;

    public SupplierManagerViewSimple() {
        super();
        this.apiService = new com.magscene.magsav.desktop.service.ApiService();

        // Charger depuis le backend
        loadSuppliersFromBackend();
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

        // Table des fournisseurs (pas de titre - déjà dans le header principal)
        createSupplierTable();

        mainContainer.getChildren().add(supplierTable);
        VBox.setVgrow(supplierTable, Priority.ALWAYS);

        return mainContainer;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Nom, contact, email...", text -> performSearch(text));
        
        // 📦 Filtre services avec ViewUtils
        VBox servicesBox = ViewUtils.createFilterBox("📦 Services",
            new String[]{"Tous services", "Location", "Vente", "Maintenance", "SAV"},
            "Tous services", value -> loadSuppliers());
        
        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
            new String[]{"Tous statuts", "Actif", "Inactif", "En attente"},
            "Tous statuts", value -> loadSuppliers());
        
        toolbar.getChildren().addAll(searchBox, servicesBox, statusBox);
    }
    
    private void performSearch(String text) {
        updateStatus("Recherche: " + text);
        // TODO: Implémenter recherche
    }
    
    private void loadSuppliers() {
        loadSuppliersFromBackend();
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

    private void loadSuppliersFromBackend() {
        try {
            System.out.println("🔄 Tentative de chargement des fournisseurs depuis le backend...");
            java.util.List<java.util.Map<String, Object>> backendSuppliers = apiService.getAll("suppliers");

            if (backendSuppliers != null && !backendSuppliers.isEmpty()) {
                System.out.println("✅ Backend disponible - Chargement de " + backendSuppliers.size() + " fournisseurs");
                supplierList.clear();

                for (java.util.Map<String, Object> supplierMap : backendSuppliers) {
                    String name = (String) supplierMap.getOrDefault("companyName", "N/A");
                    String contact = (String) supplierMap.getOrDefault("contactPerson", "N/A");
                    String email = (String) supplierMap.getOrDefault("email", "N/A");
                    String category = (String) supplierMap.getOrDefault("category", "N/A");
                    Boolean active = (Boolean) supplierMap.getOrDefault("active", true);
                    String status = active ? "✅ Actif" : "❌ Inactif";

                    supplierList.add(new SupplierData(name, contact, email, category, status));
                }
                Platform.runLater(() -> updateStatus(supplierList.size() + " fournisseur(s) chargé(s)"));
            } else {
                System.out.println("⚠️ Aucun fournisseur dans le backend");
                Platform.runLater(() -> updateStatus("Aucun fournisseur disponible"));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement fournisseurs: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> updateStatus("Erreur chargement fournisseurs"));
        }
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
        public String getName() {
            return name;
        }

        public String getContact() {
            return contact;
        }

        public String getEmail() {
            return email;
        }

        public String getServices() {
            return services;
        }

        public String getStatus() {
            return status;
        }
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