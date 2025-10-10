package com.magsav.gui;

import com.magsav.repo.ProductRepository;
import com.magsav.service.NavigationService;
import com.magsav.util.AppLogger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

/**
 * Contrôleur pour l'interface de gestion centralisée
 * Regroupe la gestion des produits, fabricants, fournisseurs, SAV externes et clients
 */
public class ManagementHubController implements Initializable {
    
    // ======================= FXML ELEMENTS =======================
    
    // Onglet Produits
    @FXML private TextField txtProductSearch;
    @FXML private ComboBox<String> cbProductCategory;
    @FXML private ComboBox<String> cbProductManufacturer;
    @FXML private TableView<ProductRow> tableProducts;
    @FXML private TableColumn<ProductRow, Long> colProductId;
    @FXML private TableColumn<ProductRow, String> colProductName;
    @FXML private TableColumn<ProductRow, String> colProductCategory;
    @FXML private TableColumn<ProductRow, String> colProductManufacturer;
    @FXML private TableColumn<ProductRow, Integer> colProductStock;
    @FXML private TableColumn<ProductRow, String> colProductPrice;
    @FXML private Label lblProductCount;
    @FXML private Button btnEditProduct;
    @FXML private Button btnDeleteProduct;
    
    // Onglet Fabricants
    @FXML private TextField txtManufacturerSearch;
    @FXML private TableView<ManufacturerRow> tableManufacturers;
    @FXML private TableColumn<ManufacturerRow, Long> colManufacturerId;
    @FXML private TableColumn<ManufacturerRow, String> colManufacturerName;
    @FXML private TableColumn<ManufacturerRow, String> colManufacturerContact;
    @FXML private TableColumn<ManufacturerRow, String> colManufacturerEmail;
    @FXML private TableColumn<ManufacturerRow, Integer> colManufacturerProducts;
    @FXML private Button btnEditManufacturer;
    @FXML private Button btnDeleteManufacturer;
    
    // Onglet Fournisseurs  
    @FXML private TextField txtSupplierSearch;
    @FXML private TableView<SupplierRow> tableSuppliers;
    @FXML private TableColumn<SupplierRow, Long> colSupplierId;
    @FXML private TableColumn<SupplierRow, String> colSupplierName;
    @FXML private TableColumn<SupplierRow, String> colSupplierContact;
    @FXML private TableColumn<SupplierRow, String> colSupplierEmail;
    @FXML private TableColumn<SupplierRow, Integer> colSupplierProducts;
    @FXML private Button btnEditSupplier;
    @FXML private Button btnDeleteSupplier;
    
    // Onglet SAV Externes
    @FXML private TextField txtSavSearch;
    @FXML private TableView<SavRow> tableSav;
    @FXML private TableColumn<SavRow, Long> colSavId;
    @FXML private TableColumn<SavRow, String> colSavName;
    @FXML private TableColumn<SavRow, String> colSavSpeciality;
    @FXML private TableColumn<SavRow, String> colSavContact;
    @FXML private TableColumn<SavRow, String> colSavEmail;
    @FXML private Button btnEditSav;
    @FXML private Button btnDeleteSav;
    
    // Onglet Clients
    @FXML private TextField txtClientSearch;
    @FXML private ComboBox<String> cbClientType;
    @FXML private TableView<ClientRow> tableClients;
    @FXML private TableColumn<ClientRow, Long> colClientId;
    @FXML private TableColumn<ClientRow, String> colClientName;
    @FXML private TableColumn<ClientRow, String> colClientCompany;
    @FXML private TableColumn<ClientRow, String> colClientContact;
    @FXML private TableColumn<ClientRow, String> colClientEmail;
    @FXML private TableColumn<ClientRow, Integer> colClientInterventions;
    @FXML private Button btnEditClient;
    @FXML private Button btnDeleteClient;
    @FXML private Button btnClientHistory;
    
    // Éléments généraux
    @FXML private TabPane managementTabPane;
    @FXML private Label lblDataStats;
    
    // ======================= SERVICES =======================
    
    private final ProductRepository productRepository;
    
    // ======================= DATA =======================
    
    private final ObservableList<ProductRow> productData = FXCollections.observableArrayList();
    private final ObservableList<ManufacturerRow> manufacturerData = FXCollections.observableArrayList();
    private final ObservableList<SupplierRow> supplierData = FXCollections.observableArrayList();
    private final ObservableList<SavRow> savData = FXCollections.observableArrayList();
    private final ObservableList<ClientRow> clientData = FXCollections.observableArrayList();
    
    public ManagementHubController() {
        this.productRepository = new ProductRepository();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppLogger.info("management_hub", "Initialisation de l'interface de gestion centralisée");
        
        initializeProductsTab();
        initializeManufacturersTab();
        initializeSuppliersTab();
        initializeSavTab();
        initializeClientsTab();
        
        setupTableSelectionHandlers();
        loadAllData();
        
        AppLogger.info("management_hub", "Interface de gestion centralisée initialisée");
    }
    
    // ======================= INITIALIZATION =======================
    
    private void initializeProductsTab() {
        // Configuration des colonnes
        colProductId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colProductCategory.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colProductManufacturer.setCellValueFactory(new PropertyValueFactory<>("fabricant"));
        colProductStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colProductPrice.setCellValueFactory(new PropertyValueFactory<>("prix"));
        
        tableProducts.setItems(productData);
        
        // Recherche en temps réel
        txtProductSearch.textProperty().addListener((obs, oldText, newText) -> filterProducts());
        cbProductCategory.valueProperty().addListener((obs, oldVal, newVal) -> filterProducts());
        cbProductManufacturer.valueProperty().addListener((obs, oldVal, newVal) -> filterProducts());
    }
    
    private void initializeManufacturersTab() {
        colManufacturerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colManufacturerName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colManufacturerContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colManufacturerEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colManufacturerProducts.setCellValueFactory(new PropertyValueFactory<>("nombreProduits"));
        
        tableManufacturers.setItems(manufacturerData);
        txtManufacturerSearch.textProperty().addListener((obs, oldText, newText) -> filterManufacturers());
    }
    
    private void initializeSuppliersTab() {
        colSupplierId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSupplierName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSupplierContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colSupplierEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSupplierProducts.setCellValueFactory(new PropertyValueFactory<>("nombreProduits"));
        
        tableSuppliers.setItems(supplierData);
        txtSupplierSearch.textProperty().addListener((obs, oldText, newText) -> filterSuppliers());
    }
    
    private void initializeSavTab() {
        colSavId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSavName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSavSpeciality.setCellValueFactory(new PropertyValueFactory<>("specialite"));
        colSavContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colSavEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        tableSav.setItems(savData);
        txtSavSearch.textProperty().addListener((obs, oldText, newText) -> filterSav());
    }
    
    private void initializeClientsTab() {
        colClientId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colClientName.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colClientCompany.setCellValueFactory(new PropertyValueFactory<>("societe"));
        colClientContact.setCellValueFactory(new PropertyValueFactory<>("contact"));
        colClientEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colClientInterventions.setCellValueFactory(new PropertyValueFactory<>("nombreInterventions"));
        
        tableClients.setItems(clientData);
        txtClientSearch.textProperty().addListener((obs, oldText, newText) -> filterClients());
        cbClientType.valueProperty().addListener((obs, oldVal, newVal) -> filterClients());
    }
    
    private void setupTableSelectionHandlers() {
        // Produits
        tableProducts.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEditProduct.setDisable(!hasSelection);
            btnDeleteProduct.setDisable(!hasSelection);
        });
        
        // Fabricants
        tableManufacturers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEditManufacturer.setDisable(!hasSelection);
            btnDeleteManufacturer.setDisable(!hasSelection);
        });
        
        // Fournisseurs
        tableSuppliers.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEditSupplier.setDisable(!hasSelection);
            btnDeleteSupplier.setDisable(!hasSelection);
        });
        
        // SAV
        tableSav.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEditSav.setDisable(!hasSelection);
            btnDeleteSav.setDisable(!hasSelection);
        });
        
        // Clients
        tableClients.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            btnEditClient.setDisable(!hasSelection);
            btnDeleteClient.setDisable(!hasSelection);
            btnClientHistory.setDisable(!hasSelection);
        });
    }
    
    // ======================= ACTIONS - PRODUITS =======================
    
    @FXML private void onRefreshProducts() {
        loadProductsData();
    }
    
    @FXML private void onAddProduct() {
        AppLogger.info("management_hub", "Ajout d'un nouveau produit");
        // TODO: Ouvrir formulaire d'ajout de produit
        NavigationService.showInfoDialog("Non implémenté", "L'ajout de produit sera implémenté prochainement.");
    }
    
    @FXML private void onEditProduct() {
        ProductRow selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("management_hub", "Modification du produit: " + selected.getNom());
            // TODO: Ouvrir formulaire de modification
            NavigationService.showInfoDialog("Non implémenté", "La modification de produit sera implémentée prochainement.");
        }
    }
    
    @FXML private void onDeleteProduct() {
        ProductRow selected = tableProducts.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = NavigationService.showConfirmDialog(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le produit \"" + selected.getNom() + "\" ?"
            );
            
            if (confirmed) {
                AppLogger.info("management_hub", "Suppression du produit: " + selected.getNom());
                // TODO: Supprimer le produit de la base de données
                productData.remove(selected);
                updateProductCount();
            }
        }
    }
    
    @FXML private void onImportProducts() {
        AppLogger.info("management_hub", "Import de produits");
        NavigationService.showInfoDialog("Non implémenté", "L'import de produits sera implémenté prochainement.");
    }
    
    @FXML private void onExportProducts() {
        AppLogger.info("management_hub", "Export de produits");
        NavigationService.showInfoDialog("Non implémenté", "L'export de produits sera implémenté prochainement.");
    }
    
    @FXML private void onOpenProductManagement() {
        AppLogger.info("management_hub", "Ouverture de l'interface complète de gestion des produits");
        NavigationService.openInNewWindow("/fxml/products/management/product_management.fxml", "Gestion des Produits");
    }
    
    // ======================= ACTIONS - FABRICANTS =======================
    
    @FXML private void onRefreshManufacturers() {
        loadManufacturersData();
    }
    
    @FXML private void onAddManufacturer() {
        AppLogger.info("management_hub", "Ajout d'un nouveau fabricant");
        NavigationService.showInfoDialog("Non implémenté", "L'ajout de fabricant sera implémenté prochainement.");
    }
    
    @FXML private void onEditManufacturer() {
        ManufacturerRow selected = tableManufacturers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("management_hub", "Modification du fabricant: " + selected.getNom());
            NavigationService.showInfoDialog("Non implémenté", "La modification de fabricant sera implémentée prochainement.");
        }
    }
    
    @FXML private void onDeleteManufacturer() {
        ManufacturerRow selected = tableManufacturers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = NavigationService.showConfirmDialog(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le fabricant \"" + selected.getNom() + "\" ?"
            );
            
            if (confirmed) {
                AppLogger.info("management_hub", "Suppression du fabricant: " + selected.getNom());
                manufacturerData.remove(selected);
            }
        }
    }
    
    @FXML private void onOpenManufacturerInterface() {
        NavigationService.openInNewWindow("/fxml/societes/lists/manufacturers.fxml", "Gestion des Fabricants");
    }
    
    // ======================= ACTIONS - FOURNISSEURS =======================
    
    @FXML private void onRefreshSuppliers() {
        loadSuppliersData();
    }
    
    @FXML private void onAddSupplier() {
        AppLogger.info("management_hub", "Ajout d'un nouveau fournisseur");
        NavigationService.showInfoDialog("Non implémenté", "L'ajout de fournisseur sera implémenté prochainement.");
    }
    
    @FXML private void onEditSupplier() {
        SupplierRow selected = tableSuppliers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("management_hub", "Modification du fournisseur: " + selected.getNom());
            NavigationService.showInfoDialog("Non implémenté", "La modification de fournisseur sera implémentée prochainement.");
        }
    }
    
    @FXML private void onDeleteSupplier() {
        SupplierRow selected = tableSuppliers.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = NavigationService.showConfirmDialog(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le fournisseur \"" + selected.getNom() + "\" ?"
            );
            
            if (confirmed) {
                AppLogger.info("management_hub", "Suppression du fournisseur: " + selected.getNom());
                supplierData.remove(selected);
            }
        }
    }
    
    @FXML private void onOpenSupplierInterface() {
        NavigationService.openInNewWindow("/fxml/societes/lists/suppliers.fxml", "Gestion des Fournisseurs");
    }
    
    // ======================= ACTIONS - SAV EXTERNES =======================
    
    @FXML private void onRefreshSav() {
        loadSavData();
    }
    
    @FXML private void onAddSav() {
        AppLogger.info("management_hub", "Ajout d'un nouveau SAV externe");
        NavigationService.showInfoDialog("Non implémenté", "L'ajout de SAV externe sera implémenté prochainement.");
    }
    
    @FXML private void onEditSav() {
        SavRow selected = tableSav.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("management_hub", "Modification du SAV: " + selected.getNom());
            NavigationService.showInfoDialog("Non implémenté", "La modification de SAV sera implémentée prochainement.");
        }
    }
    
    @FXML private void onDeleteSav() {
        SavRow selected = tableSav.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = NavigationService.showConfirmDialog(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le SAV \"" + selected.getNom() + "\" ?"
            );
            
            if (confirmed) {
                AppLogger.info("management_hub", "Suppression du SAV: " + selected.getNom());
                savData.remove(selected);
            }
        }
    }
    
    @FXML private void onOpenSavInterface() {
        NavigationService.openInNewWindow("/fxml/societes/lists/external_sav.fxml", "Gestion des SAV Externes");
    }
    
    // ======================= ACTIONS - CLIENTS =======================
    
    @FXML private void onRefreshClients() {
        loadClientsData();
    }
    
    @FXML private void onAddClient() {
        AppLogger.info("management_hub", "Ajout d'un nouveau client");
        NavigationService.showInfoDialog("Non implémenté", "L'ajout de client sera implémenté prochainement.");
    }
    
    @FXML private void onEditClient() {
        ClientRow selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("management_hub", "Modification du client: " + selected.getNom());
            NavigationService.showInfoDialog("Non implémenté", "La modification de client sera implémentée prochainement.");
        }
    }
    
    @FXML private void onDeleteClient() {
        ClientRow selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = NavigationService.showConfirmDialog(
                "Confirmer la suppression",
                "Êtes-vous sûr de vouloir supprimer le client \"" + selected.getNom() + "\" ?"
            );
            
            if (confirmed) {
                AppLogger.info("management_hub", "Suppression du client: " + selected.getNom());
                clientData.remove(selected);
            }
        }
    }
    
    @FXML private void onClientHistory() {
        ClientRow selected = tableClients.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("management_hub", "Historique du client: " + selected.getNom());
            NavigationService.showInfoDialog("Non implémenté", "L'historique client sera implémenté prochainement.");
        }
    }
    
    @FXML private void onOpenClientInterface() {
        NavigationService.openInNewWindow("/fxml/clients.fxml", "Gestion des Clients");
    }
    
    // ======================= ACTIONS GÉNÉRALES =======================
    
    @FXML private void onRefreshAllData() {
        AppLogger.info("management_hub", "Actualisation de toutes les données");
        loadAllData();
    }
    
    @FXML private void onClose() {
        AppLogger.info("management_hub", "Fermeture de l'interface de gestion");
        NavigationService.closeCurrentWindow(managementTabPane);
    }
    
    // ======================= DATA LOADING =======================
    
    private void loadAllData() {
        CompletableFuture.runAsync(() -> {
            loadProductsData();
            loadManufacturersData();
            loadSuppliersData();
            loadSavData();
            loadClientsData();
            
            Platform.runLater(this::updateDataStats);
        });
    }
    
    private void loadProductsData() {
        try {
            AppLogger.info("management_hub", "Chargement des données produits");
            
            // TODO: Remplacer par les vrais appels à la base de données
            productData.clear();
            
            // Données de test
            productData.addAll(
                new ProductRow(1L, "Produit Test 1", "Électronique", "Samsung", 15, "299,99 €"),
                new ProductRow(2L, "Produit Test 2", "Informatique", "Apple", 8, "1299,00 €"),
                new ProductRow(3L, "Produit Test 3", "Électroménager", "Whirlpool", 3, "599,50 €")
            );
            
            Platform.runLater(() -> {
                updateProductCount();
                populateProductFilters();
            });
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des produits", e);
            Platform.runLater(() -> NavigationService.showErrorDialog("Erreur", "Impossible de charger les produits: " + e.getMessage()));
        }
    }
    
    private void loadManufacturersData() {
        try {
            AppLogger.info("management_hub", "Chargement des données fabricants");
            
            manufacturerData.clear();
            manufacturerData.addAll(
                new ManufacturerRow(1L, "Samsung", "John Doe", "contact@samsung.com", 25),
                new ManufacturerRow(2L, "Apple", "Jane Smith", "support@apple.com", 18),
                new ManufacturerRow(3L, "Whirlpool", "Bob Johnson", "info@whirlpool.com", 12)
            );
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des fabricants", e);
        }
    }
    
    private void loadSuppliersData() {
        try {
            AppLogger.info("management_hub", "Chargement des données fournisseurs");
            
            supplierData.clear();
            supplierData.addAll(
                new SupplierRow(1L, "TechDistrib", "Alice Brown", "orders@techdistrib.com", 35),
                new SupplierRow(2L, "ElectroPlus", "Charlie Wilson", "sales@electroplus.com", 22),
                new SupplierRow(3L, "MegaStock", "Diana Miller", "contact@megastock.com", 18)
            );
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des fournisseurs", e);
        }
    }
    
    private void loadSavData() {
        try {
            AppLogger.info("management_hub", "Chargement des données SAV externes");
            
            savData.clear();
            savData.addAll(
                new SavRow(1L, "TechRepair Pro", "Électronique", "Mike Davis", "service@techrepair.com"),
                new SavRow(2L, "HomeService+", "Électroménager", "Lisa Taylor", "help@homeservice.com"),
                new SavRow(3L, "QuickFix", "Informatique", "Tom Anderson", "support@quickfix.com")
            );
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des SAV externes", e);
        }
    }
    
    private void loadClientsData() {
        try {
            AppLogger.info("management_hub", "Chargement des données clients");
            
            clientData.clear();
            clientData.addAll(
                new ClientRow(1L, "Martin Dupont", "ACME Corp", "01 23 45 67 89", "martin@acme.com", 5),
                new ClientRow(2L, "Sophie Bernard", "TechSolutions", "01 98 76 54 32", "sophie@techsol.com", 3),
                new ClientRow(3L, "Pierre Durand", "Particulier", "06 12 34 56 78", "pierre.durand@email.com", 1)
            );
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des clients", e);
        }
    }
    
    // ======================= FILTERING =======================
    
    private void filterProducts() {
        // TODO: Implémenter le filtrage des produits
        updateProductCount();
    }
    
    private void filterManufacturers() {
        // TODO: Implémenter le filtrage des fabricants
    }
    
    private void filterSuppliers() {
        // TODO: Implémenter le filtrage des fournisseurs
    }
    
    private void filterSav() {
        // TODO: Implémenter le filtrage des SAV
    }
    
    private void filterClients() {
        // TODO: Implémenter le filtrage des clients
    }
    
    // ======================= HELPERS =======================
    
    private void updateProductCount() {
        Platform.runLater(() -> {
            int count = productData.size();
            lblProductCount.setText(count + " produit" + (count > 1 ? "s" : ""));
        });
    }
    
    private void populateProductFilters() {
        // TODO: Peupler les ComboBox de filtrage des produits
    }
    
    private void updateDataStats() {
        Platform.runLater(() -> {
            String stats = String.format("Produits: %d | Fabricants: %d | Fournisseurs: %d | SAV: %d | Clients: %d",
                productData.size(), manufacturerData.size(), supplierData.size(), savData.size(), clientData.size());
            lblDataStats.setText(stats);
        });
    }
    
    // ======================= DATA CLASSES =======================
    
    public static class ProductRow {
        private final Long id;
        private final String nom;
        private final String categorie;
        private final String fabricant;
        private final Integer stock;
        private final String prix;
        
        public ProductRow(Long id, String nom, String categorie, String fabricant, Integer stock, String prix) {
            this.id = id;
            this.nom = nom;
            this.categorie = categorie;
            this.fabricant = fabricant;
            this.stock = stock;
            this.prix = prix;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getCategorie() { return categorie; }
        public String getFabricant() { return fabricant; }
        public Integer getStock() { return stock; }
        public String getPrix() { return prix; }
    }
    
    public static class ManufacturerRow {
        private final Long id;
        private final String nom;
        private final String contact;
        private final String email;
        private final Integer nombreProduits;
        
        public ManufacturerRow(Long id, String nom, String contact, String email, Integer nombreProduits) {
            this.id = id;
            this.nom = nom;
            this.contact = contact;
            this.email = email;
            this.nombreProduits = nombreProduits;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public Integer getNombreProduits() { return nombreProduits; }
    }
    
    public static class SupplierRow {
        private final Long id;
        private final String nom;
        private final String contact;
        private final String email;
        private final Integer nombreProduits;
        
        public SupplierRow(Long id, String nom, String contact, String email, Integer nombreProduits) {
            this.id = id;
            this.nom = nom;
            this.contact = contact;
            this.email = email;
            this.nombreProduits = nombreProduits;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public Integer getNombreProduits() { return nombreProduits; }
    }
    
    public static class SavRow {
        private final Long id;
        private final String nom;
        private final String specialite;
        private final String contact;
        private final String email;
        
        public SavRow(Long id, String nom, String specialite, String contact, String email) {
            this.id = id;
            this.nom = nom;
            this.specialite = specialite;
            this.contact = contact;
            this.email = email;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getSpecialite() { return specialite; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
    }
    
    public static class ClientRow {
        private final Long id;
        private final String nom;
        private final String societe;
        private final String contact;
        private final String email;
        private final Integer nombreInterventions;
        
        public ClientRow(Long id, String nom, String societe, String contact, String email, Integer nombreInterventions) {
            this.id = id;
            this.nom = nom;
            this.societe = societe;
            this.contact = contact;
            this.email = email;
            this.nombreInterventions = nombreInterventions;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getNom() { return nom; }
        public String getSociete() { return societe; }
        public String getContact() { return contact; }
        public String getEmail() { return email; }
        public Integer getNombreInterventions() { return nombreInterventions; }
    }
    
    // ======================= MÉTHODES DE REDIRECTION VERS LES HUBS =======================
    
    @FXML
    private void onOpenProductsHub() {
        AppLogger.info("Redirection vers le Hub Produits");
        NavigationService.openInNewWindow("/fxml/products/hubs/hub_products.fxml", "Hub Produits - Mag Scène");
    }
    
    @FXML
    private void onOpenSocietesHub() {
        AppLogger.info("Redirection vers le Hub Sociétés");
        NavigationService.openInNewWindow("/fxml/societes/hubs/hub_societes.fxml", "Hub Sociétés - Mag Scène");
    }
    
    @FXML
    private void onOpenSocietesHubFournisseurs() {
        AppLogger.info("Redirection vers le Hub Sociétés (onglet Fournisseurs)");
        NavigationService.openInNewWindow("/fxml/societes/hubs/hub_societes.fxml", "Hub Sociétés - Fournisseurs - Mag Scène");
    }
    
    @FXML
    private void onOpenSocietesHubSav() {
        AppLogger.info("Redirection vers le Hub Sociétés (onglet SAV)");
        NavigationService.openInNewWindow("/fxml/societes/hubs/hub_societes.fxml", "Hub Sociétés - SAV - Mag Scène");
    }
    
    @FXML
    private void onOpenClientsHub() {
        AppLogger.info("Redirection vers le Hub Clients");
        NavigationService.openInNewWindow("/fxml/societes/hubs/hub_clients.fxml", "Hub Clients - Mag Scène");
    }
}