package com.magscene.magsav.desktop.view.supplier;

import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.component.DetailPanelProvider;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vue simplifiée de gestion des fournisseurs pour test Phase 2
 */
public class SupplierManagerViewSimple extends BaseManagerView<Object> {

    private TableView<SupplierData> supplierTable;
    private ObservableList<SupplierData> supplierList; // Déclaration sans initialisation

    private com.magscene.magsav.desktop.service.ApiService apiService = new com.magscene.magsav.desktop.service.ApiService();

    public SupplierManagerViewSimple() {
        super();
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

        // Table des fournisseurs (pas de titre - déjà dans le header principal)
        createSupplierTable();

        // Envelopper le tableau dans DetailPanelContainer pour le volet de détail
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(supplierTable);

        mainContainer.getChildren().add(containerWithDetail);
        VBox.setVgrow(containerWithDetail, Priority.ALWAYS);

        return mainContainer;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Nom, contact, email...",
                text -> performSearch(text));

        // 📦 Filtre services avec ViewUtils
        VBox servicesBox = ViewUtils.createFilterBox("📦 Services",
                new String[] { "Tous services", "Location", "Vente", "Maintenance", "SAV" },
                "Tous services", value -> loadSuppliers());

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Actif", "Inactif", "En attente" },
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
        // CRITICAL: Initialiser supplierList ICI
        if (supplierList == null) {
            supplierList = FXCollections.observableArrayList();
            System.out.println("✅ supplierList initialisé");
        }

        // Initialiser les dépendances si nécessaire
        if (apiService == null) {
            apiService = new com.magscene.magsav.desktop.service.ApiService();
        }

        // Binding du tableau après création
        if (supplierTable != null && supplierList != null) {
            supplierTable.setItems(supplierList);
            System.out.println("🔗 Tableau Suppliers lié à supplierList");
        }

        // Charger les données depuis le backend
        loadSuppliersFromBackend();
    }

    private void createSupplierTable() {
        supplierTable = new TableView<>();
        supplierTable.setItems(supplierList);
        supplierTable.setStyle("-fx-background-color: "
                + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getCurrentUIColor()
                + "; -fx-background-radius: 8; -fx-border-color: #8B91FF; -fx-border-width: 1px; -fx-border-radius: 8px;");

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

                // Tout le traitement sur le thread JavaFX
                Platform.runLater(() -> {
                    supplierList.clear();

                    for (java.util.Map<String, Object> supplierMap : backendSuppliers) {
                        String name = (String) supplierMap.getOrDefault("name", "N/A");
                        String contact = (String) supplierMap.getOrDefault("contactPerson", "N/A");
                        String email = (String) supplierMap.getOrDefault("email", "N/A");

                        // Construire la liste des services
                        java.util.List<String> services = new java.util.ArrayList<>();
                        if (Boolean.TRUE.equals(supplierMap.get("hasAfterSalesService")))
                            services.add("SAV");
                        if (Boolean.TRUE.equals(supplierMap.get("hasRMAService")))
                            services.add("RMA");
                        if (Boolean.TRUE.equals(supplierMap.get("hasPartsService")))
                            services.add("Pièces");
                        if (Boolean.TRUE.equals(supplierMap.get("hasEquipmentService")))
                            services.add("Matériel");
                        String servicesStr = services.isEmpty() ? "Aucun" : String.join(", ", services);

                        Boolean active = (Boolean) supplierMap.getOrDefault("active", true);
                        String status = active ? "✅ Actif" : "❌ Inactif";

                        supplierList.add(new SupplierData(name, contact, email, servicesStr, status));
                    }

                    // Forcer le rafraîchissement du tableau
                    supplierTable.refresh();
                    System.out.println("🔄 Tableau Suppliers rafraîchi - Items: " + supplierTable.getItems().size());
                    updateStatus(supplierList.size() + " fournisseur(s) chargé(s)");
                    System.out.println("✅ Tableau rafraîchi avec " + supplierList.size() + " fournisseurs");
                });
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
    public static class SupplierData implements DetailPanelProvider {
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

        // Implémentation de DetailPanelProvider
        @Override
        public String getDetailTitle() {
            return getName();
        }

        @Override
        public String getDetailSubtitle() {
            return "Contact: " + getContact();
        }

        @Override
        public Image getDetailImage() {
            return null;
        }

        @Override
        public String getQRCodeData() {
            return null; // Les fournisseurs n'ont pas de QR code
        }

        @Override
        public String getDetailId() {
            return getName();
        }

        @Override
        public VBox getDetailInfoContent() {
            VBox content = new VBox(10);
            content.getChildren().addAll(
                    new Label("📧 Email: " + getEmail()),
                    new Label("🛠️ Services: " + getServices()),
                    new Label("📊 Statut: " + getStatus()));
            return content;
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