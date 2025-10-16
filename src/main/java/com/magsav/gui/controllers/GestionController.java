package com.magsav.gui.controllers;

import com.magsav.gui.utils.TabBuilderUtils;
import com.magsav.gui.controllers.affaires.AffairesController;
import com.magsav.dto.ClientRow;
import com.magsav.dto.CompanyRow;
import com.magsav.repo.ProductRepository;
import com.magsav.service.data.DataServiceManager;
import com.magsav.service.NavigationService;
import com.magsav.util.AppLogger;
import com.magsav.gui.components.DetailLayoutHelper;
import com.magsav.gui.components.DetailPaneFactory.*;
import com.magsav.service.Refreshable;

import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;

import javafx.collections.FXCollections;

import java.util.Arrays;
import java.util.List;

/**
 * Contrôleur dédié à la section Gestion
 * Gère les onglets Produits, Clients, Sociétés et Affaires
 */
public class GestionController implements Refreshable {
    
    private final DataServiceManager dataManager = DataServiceManager.getInstance();
    private final ProductRepository productRepo = new ProductRepository();
    
    // Composants UI pour le panneau de détails des produits
    private Label productNameDetail;
    private Label productReferenceDetail;
    private Label productCategoryDetail;
    private Label productStockDetail;
    private Label productPriceDetail;
    private Button editProductBtn;
    private Button deleteProductBtn;
    
    /**
     * Crée l'onglet Produits avec interface unifiée
     */
    public Tab createProduitsTab() {
        Tab tab = TabBuilderUtils.createBasicTab("📦 Produits");
        VBox content = TabBuilderUtils.createTabContent();
        
        // Boutons d'action et filtres unifiés
        Button nouveauBtn = TabBuilderUtils.createIconButton("✚ Nouveau", "btn-primary");
        Button modifierBtn = TabBuilderUtils.createIconButton("✏️ Modifier", "btn-secondary");
        Button supprimerBtn = TabBuilderUtils.createIconButton("🗑️ Supprimer", "btn-danger");
        
        modifierBtn.setDisable(true);
        supprimerBtn.setDisable(true);
        
        // Filtres
        ComboBox<String> typeFilter = new ComboBox<>();
        TabBuilderUtils.configureComboBox(typeFilter, 120);
        typeFilter.getItems().addAll("Tous", "Disponible", "Réservé", "En réparation", "Vendu");
        typeFilter.setValue("Tous");
        
        TextField searchField = new TextField();
        TabBuilderUtils.configureSearchField(searchField, "Rechercher un produit...", 200);
        
        // Interface unifiée: boutons au-dessus, filtres en dessous
        VBox controlsLayout = TabBuilderUtils.createUnifiedControlsLayout(typeFilter, searchField, 
                                                                          nouveauBtn, modifierBtn, supprimerBtn);
        content.getChildren().add(controlsLayout);
        
        // Créer le TableView des produits
        TableView<ProductRepository.ProductRow> productTable = new TableView<>();
        TabBuilderUtils.configureBasicTable(productTable);
        
        // Configurer les colonnes
        setupProductTableColumns(productTable);
        
        // Panel de visualisation unifié
        DetailPane detailPane = DetailLayoutHelper.createProductVisualizationPane(() -> {
            System.out.println("Ouverture des détails produit");
        });
        
        // SplitPane unifié
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(productTable, detailPane);
        splitPane.setDividerPositions(0.7);
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configurer les événements de sélection
        setupProductTableSelection(productTable, detailPane, modifierBtn, supprimerBtn);
        
        // Charger les données
        loadProductsData(productTable);
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Clients avec interface unifiée
     */
    public Tab createClientsTab() {
        Tab tab = TabBuilderUtils.createBasicTab("👥 Clients");
        VBox content = TabBuilderUtils.createTabContent();
        
        // Statistiques seulement (sans légende en haut - doublon avec titre onglet)
        Label totalClientsLabel = new Label("Total: 0");
        Label societesLabel = new Label("Sociétés: 0");
        Label particuliersLabel = new Label("Particuliers: 0");
        
        HBox statsBox = TabBuilderUtils.createStatsOnlyBox(Arrays.asList(
            totalClientsLabel, societesLabel, particuliersLabel));
        content.getChildren().add(statsBox);
        
        // Contrôles unifiés: boutons à gauche, filtres au-dessus
        ComboBox<String> typeFilter = createClientTypeFilter();
        TextField searchField = new TextField();
        TabBuilderUtils.configureSearchField(searchField, "Rechercher un client...", 200);
        
        Button nouveauBtn = TabBuilderUtils.createIconButton("✚ Nouveau", "btn-primary");
        Button modifierBtn = TabBuilderUtils.createIconButton("✏️ Modifier", "btn-secondary");
        Button supprimerBtn = TabBuilderUtils.createIconButton("🗑️ Supprimer", "btn-danger");
        
        modifierBtn.setDisable(true);
        supprimerBtn.setDisable(true);
        
        VBox controlsLayout = TabBuilderUtils.createUnifiedControlsLayout(typeFilter, searchField, 
                                                                          nouveauBtn, modifierBtn, supprimerBtn);
        content.getChildren().add(controlsLayout);
        
        // Table des clients
        TableView<ClientRow> table = new TableView<>();
        TabBuilderUtils.configureBasicTable(table);
        setupClientTableColumns(table);
        
        // Panel de visualisation unifié
        DetailPane detailPane = DetailLayoutHelper.createClientVisualizationPane(() -> {
            System.out.println("Ouverture des détails client");
        });
        
        // SplitPane unifié
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(table, detailPane);
        splitPane.setDividerPositions(0.7);
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration des événements
        setupClientTableSelection(table, detailPane, modifierBtn, supprimerBtn);
        
        // Charger les données
        loadClientsDataWithFilter(table, "Tous", "", totalClientsLabel, societesLabel, particuliersLabel);
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Sociétés avec interface unifiée
     */
    public Tab createSocietesTab() {
        Tab tab = TabBuilderUtils.createBasicTab("🏢 Sociétés");
        VBox content = TabBuilderUtils.createTabContent();
        
        // Statistiques seulement (sans légende en haut - doublon avec titre onglet)
        Label totalLabel = new Label("Total: 0");
        Label clientsLabel = new Label("Clients: 0");
        Label fabricantsLabel = new Label("Fabricants: 0");
        Label collaborateursLabel = new Label("Collaborateurs: 0");
        Label particuliersLabel = new Label("Particuliers: 0");
        Label magSceneLabel = new Label("Mag Scène: 0");
        Label administrationLabel = new Label("Administration: 0");
        
        HBox statsBox = TabBuilderUtils.createStatsOnlyBox(Arrays.asList(
            totalLabel, clientsLabel, fabricantsLabel, collaborateursLabel, 
            particuliersLabel, magSceneLabel, administrationLabel));
        content.getChildren().add(statsBox);
        
        // Contrôles unifiés: boutons à gauche, filtres au-dessus
        ComboBox<String> typeFilter = createCompanyTypeFilter();
        TextField searchField = new TextField();
        TabBuilderUtils.configureSearchField(searchField, "Rechercher une société...", 200);
        
        Button nouveauBtn = TabBuilderUtils.createIconButton("✚ Nouveau", "btn-primary");
        Button modifierBtn = TabBuilderUtils.createIconButton("✏️ Modifier", "btn-secondary");
        Button supprimerBtn = TabBuilderUtils.createIconButton("🗑️ Supprimer", "btn-danger");
        
        modifierBtn.setDisable(true);
        supprimerBtn.setDisable(true);
        
        VBox controlsLayout = TabBuilderUtils.createUnifiedControlsLayout(typeFilter, searchField,
                                                                          nouveauBtn, modifierBtn, supprimerBtn);
        content.getChildren().add(controlsLayout);
        
        // Table des sociétés
        TableView<CompanyRow> table = new TableView<>();
        TabBuilderUtils.configureBasicTable(table);
        setupCompanyTableColumns(table);
        
        // Panel de visualisation unifié
        DetailPane detailPane = DetailLayoutHelper.createCompanyVisualizationPane(() -> {
            System.out.println("Ouverture des détails société");
        });
        
        // SplitPane unifié
        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(table, detailPane);
        splitPane.setDividerPositions(0.7);
        content.getChildren().add(splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Configuration des événements
        setupCompanyTableSelection(table, detailPane, modifierBtn, supprimerBtn);
        
        // Charger les données
        loadCompaniesDataWithFilter(table, "Tous", "",
                                   totalLabel, clientsLabel, fabricantsLabel,
                                   collaborateursLabel, particuliersLabel, magSceneLabel, administrationLabel);
        
        tab.setContent(content);
        return tab;
    }
    
    /**
     * Crée l'onglet Affaires (délégué au contrôleur dédié)
     */
    public Tab createAffairesTab() {
        AffairesController affairesController = new AffairesController();
        return affairesController.creerOngletAffaires();
    }
    
    // === MÉTHODES PRIVÉES DE CONFIGURATION ===
    
    private ComboBox<String> createClientTypeFilter() {
        ComboBox<String> typeFilter = new ComboBox<>();
        TabBuilderUtils.configureComboBox(typeFilter, 120);
        typeFilter.getItems().addAll("Tous", "Société", "Particulier");
        typeFilter.setValue("Tous");
        return typeFilter;
    }
    
    private ComboBox<String> createCompanyTypeFilter() {
        ComboBox<String> typeFilter = new ComboBox<>();
        TabBuilderUtils.configureComboBox(typeFilter, 150);
        typeFilter.getItems().addAll("Tous", "Client", "Fabricant", "Collaborateur", 
                                     "Particulier", "Mag Scène", "Administration");
        typeFilter.setValue("Tous");
        return typeFilter;
    }
    
    private void setupProductTableColumns(TableView<ProductRepository.ProductRow> table) {
        TableColumn<ProductRepository.ProductRow, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nom()));
        nomCol.setPrefWidth(200);
        
        TableColumn<ProductRepository.ProductRow, String> snCol = new TableColumn<>("S/N");
        snCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().sn()));
        snCol.setPrefWidth(120);
        
        TableColumn<ProductRepository.ProductRow, String> uidCol = new TableColumn<>("UID");
        uidCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().uid()));
        uidCol.setPrefWidth(100);
        
        TableColumn<ProductRepository.ProductRow, String> fabricantCol = new TableColumn<>("Fabricant");
        fabricantCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fabricant()));
        fabricantCol.setPrefWidth(150);
        
        TableColumn<ProductRepository.ProductRow, String> situationCol = new TableColumn<>("Situation");
        situationCol.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().situation()));
        situationCol.setPrefWidth(120);
        
        table.getColumns().addAll(Arrays.asList(nomCol, snCol, uidCol, fabricantCol, situationCol));
    }
    
    private void setupClientTableColumns(TableView<ClientRow> table) {
        TableColumn<ClientRow, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(200);
        
        TableColumn<ClientRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(120);
        
        TableColumn<ClientRow, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<ClientRow, String> telephoneCol = new TableColumn<>("Téléphone");
        telephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        telephoneCol.setPrefWidth(140);
        
        TableColumn<ClientRow, String> villeCol = new TableColumn<>("Ville");
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        villeCol.setPrefWidth(150);
        
        table.getColumns().addAll(Arrays.asList(nomCol, typeCol, emailCol, telephoneCol, villeCol));
    }
    
    private void setupCompanyTableColumns(TableView<CompanyRow> table) {
        TableColumn<CompanyRow, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(250);
        
        TableColumn<CompanyRow, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(150);
        
        TableColumn<CompanyRow, String> contactCol = new TableColumn<>("Contact");
        contactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        contactCol.setPrefWidth(200);
        
        TableColumn<CompanyRow, String> villeCol = new TableColumn<>("Ville");
        villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
        villeCol.setPrefWidth(150);
        
        table.getColumns().addAll(Arrays.asList(nomCol, typeCol, contactCol, villeCol));
    }
    
    private void loadProductsData(TableView<ProductRepository.ProductRow> table) {
        try {
            List<ProductRepository.ProductRow> products = productRepo.findAllProductsWithUID();
            table.setItems(FXCollections.observableArrayList(products));
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des produits: " + e.getMessage(), e);
        }
    }
    
    private VBox createClientDetailPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("detail-panel");
        panel.setPrefWidth(350);
        // Configuration du panel de détails client
        return panel;
    }
    
    private VBox createCompanyDetailPanel() {
        VBox panel = new VBox();
        panel.getStyleClass().add("detail-panel");
        panel.setPrefWidth(350);
        // Configuration du panel de détails société
        return panel;
    }
    
    private VBox createProductDetailPanel() {
        VBox detailPanel = new VBox();
        detailPanel.setSpacing(0);
        detailPanel.setPrefWidth(300);
        detailPanel.getStyleClass().add("detail-panel");
        
        // Titre du volet
        Label detailTitle = new Label("Détails du produit");
        detailTitle.getStyleClass().add("detail-title");
        
        // Zone d'image du produit et QR Code
        HBox mediaBox = new HBox();
        mediaBox.setSpacing(10);
        mediaBox.setAlignment(javafx.geometry.Pos.CENTER);
        mediaBox.setPrefHeight(200);
        mediaBox.getStyleClass().add("product-media-box");
        
        // Image du produit
        VBox imageBox = new VBox();
        imageBox.setSpacing(5);
        imageBox.setAlignment(javafx.geometry.Pos.CENTER);
        imageBox.setPrefWidth(140);
        
        ImageView imgProductPhoto = new ImageView();
        imgProductPhoto.setFitWidth(120);
        imgProductPhoto.setFitHeight(120);
        imgProductPhoto.setPreserveRatio(true);
        imgProductPhoto.getStyleClass().add("product-image");
        
        Label imageTitle = new Label("Photo");
        imageTitle.getStyleClass().add("media-title");
        
        imageBox.getChildren().addAll(imageTitle, imgProductPhoto);
        
        // QR Code
        VBox qrBox = new VBox();
        qrBox.setSpacing(5);
        qrBox.setAlignment(javafx.geometry.Pos.CENTER);
        qrBox.setPrefWidth(140);
        
        ImageView imgQr = new ImageView();
        imgQr.setFitWidth(120);
        imgQr.setFitHeight(120);
        imgQr.setPreserveRatio(true);
        imgQr.getStyleClass().add("qr-code-image");
        
        Label qrTitle = new Label("QR Code");
        qrTitle.getStyleClass().add("media-title");
        
        qrBox.getChildren().addAll(qrTitle, imgQr);
        
        mediaBox.getChildren().addAll(imageBox, qrBox);
        
        // Informations du produit
        VBox infoBox = new VBox();
        infoBox.setSpacing(0);
        
        Label productNameLabel = new Label("Nom :");
        productNameLabel.getStyleClass().add("info-label");
        productNameDetail = new Label("Sélectionner un produit");
        productNameDetail.getStyleClass().add("info-value");
        
        Label referenceLabel = new Label("Référence :");
        referenceLabel.getStyleClass().add("info-label");
        productReferenceDetail = new Label("-");
        productReferenceDetail.getStyleClass().add("info-value");
        
        Label categoryLabel = new Label("Catégorie :");
        categoryLabel.getStyleClass().add("info-label");
        productCategoryDetail = new Label("-");
        productCategoryDetail.getStyleClass().add("info-value");
        
        Label stockLabel = new Label("Stock :");
        stockLabel.getStyleClass().add("info-label");
        productStockDetail = new Label("-");
        productStockDetail.getStyleClass().add("info-value");
        
        Label priceLabel = new Label("Prix unitaire :");
        priceLabel.getStyleClass().add("info-label");
        productPriceDetail = new Label("-");
        productPriceDetail.getStyleClass().add("info-value");
        
        infoBox.getChildren().addAll(
            productNameLabel, productNameDetail,
            referenceLabel, productReferenceDetail,
            categoryLabel, productCategoryDetail,
            stockLabel, productStockDetail,
            priceLabel, productPriceDetail
        );
        
        // Boutons d'action
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(8);
        
        editProductBtn = new Button("Modifier");
        editProductBtn.getStyleClass().add("primary-button");
        editProductBtn.setDisable(true);
        
        deleteProductBtn = new Button("Supprimer");
        deleteProductBtn.getStyleClass().add("danger-button");
        deleteProductBtn.setDisable(true);
        
        buttonsBox.getChildren().addAll(editProductBtn, deleteProductBtn);
        
        // Espacement
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        detailPanel.getChildren().addAll(detailTitle, mediaBox, infoBox, spacer, buttonsBox);
        
        return detailPanel;
    }
    
    private void setupClientTableEvents(TableView<ClientRow> table, VBox detailPanel,
                                       Button modifierBtn, Button supprimerBtn,
                                       ComboBox<String> typeFilter, TextField searchField, Button searchBtn,
                                       Label... statsLabels) {
        // Configuration des événements de sélection et filtrage
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            modifierBtn.setDisable(!hasSelection);
            supprimerBtn.setDisable(!hasSelection);
            detailPanel.setVisible(hasSelection);
        });
        
        // Double-clic pour ouvrir les détails
        table.setRowFactory(tv -> {
            TableRow<ClientRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ClientRow client = row.getItem();
                    NavigationService.openClientDetail(client.getId());
                }
            });
            return row;
        });
        
        // Charger les données
        loadClientsDataWithFilter(table, "Tous", "", statsLabels);
    }
    
    private void setupCompanyTableEvents(TableView<CompanyRow> table, VBox detailPanel,
                                        Button modifierBtn, Button supprimerBtn,
                                        ComboBox<String> typeFilter, TextField searchField, Button searchBtn,
                                        Label... statsLabels) {
        // Configuration similaire aux clients
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            modifierBtn.setDisable(!hasSelection);
            supprimerBtn.setDisable(!hasSelection);
            detailPanel.setVisible(hasSelection);
        });
        
        table.setRowFactory(tv -> {
            TableRow<CompanyRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    CompanyRow company = row.getItem();
                    NavigationService.openCompanyDetail(company.getId());
                }
            });
            return row;
        });
        
        // Charger les données
        loadCompaniesDataWithFilter(table, "Tous", "", statsLabels);
    }
    
    private void setupProductTableSelection(TableView<ProductRepository.ProductRow> table, DetailPane detailPane,
                                           Button modifierBtn, Button supprimerBtn) {
        // Configuration des événements de sélection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            modifierBtn.setDisable(!hasSelection);
            supprimerBtn.setDisable(!hasSelection);
            
            if (hasSelection) {
                EntityInfo entityInfo = DetailLayoutHelper.createEntityInfoFromProduct(newSel);
                detailPane.updateInfo(entityInfo);
                detailPane.setVisible(true);
            } else {
                detailPane.setVisible(false);
            }
        });
        
        // Double-clic pour ouvrir les détails du produit
        table.setRowFactory(tv -> {
            TableRow<ProductRepository.ProductRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ProductRepository.ProductRow product = row.getItem();
                    NavigationService.openProductDetail(product.id());
                }
            });
            return row;
        });
    }

    private void setupClientTableSelection(TableView<ClientRow> table, DetailPane detailPane,
                                         Button modifierBtn, Button supprimerBtn) {
        // Configuration des événements de sélection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            modifierBtn.setDisable(!hasSelection);
            supprimerBtn.setDisable(!hasSelection);
            
            if (hasSelection) {
                EntityInfo entityInfo = DetailLayoutHelper.createEntityInfoFromClient(newSel);
                detailPane.updateInfo(entityInfo);
                detailPane.setVisible(true);
            } else {
                detailPane.setVisible(false);
            }
        });
        
        // Double-clic pour ouvrir les détails du client
        table.setRowFactory(tv -> {
            TableRow<ClientRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    ClientRow client = row.getItem();
                    NavigationService.openClientDetail(client.getId());
                }
            });
            return row;
        });
    }

    private void setupCompanyTableSelection(TableView<CompanyRow> table, DetailPane detailPane,
                                          Button modifierBtn, Button supprimerBtn) {
        // Configuration des événements de sélection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            modifierBtn.setDisable(!hasSelection);
            supprimerBtn.setDisable(!hasSelection);
            
            if (hasSelection) {
                EntityInfo entityInfo = DetailLayoutHelper.createEntityInfoFromCompany(newSel);
                detailPane.updateInfo(entityInfo);
                detailPane.setVisible(true);
            } else {
                detailPane.setVisible(false);
            }
        });
        
        // Double-clic pour ouvrir les détails de la société
        table.setRowFactory(tv -> {
            TableRow<CompanyRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    CompanyRow company = row.getItem();
                    NavigationService.openCompanyDetail(company.getId());
                }
            });
            return row;
        });
    }

    private void updateProductDetail(ProductRepository.ProductRow product) {
        if (product == null) {
            productNameDetail.setText("Sélectionner un produit");
            productReferenceDetail.setText("-");
            productCategoryDetail.setText("-");
            productStockDetail.setText("-");
            productPriceDetail.setText("-");
            editProductBtn.setDisable(true);
            deleteProductBtn.setDisable(true);
        } else {
            productNameDetail.setText(product.nom());
            productReferenceDetail.setText(product.sn() != null ? product.sn() : "-");
            productCategoryDetail.setText("-"); // Pas de catégorie dans ProductRow
            productStockDetail.setText(product.situation());
            productPriceDetail.setText("-"); // Pas de prix dans ProductRow
            editProductBtn.setDisable(false);
            deleteProductBtn.setDisable(false);
        }
    }
    
    private void loadClientsDataWithFilter(TableView<ClientRow> table, String typeFilter, String searchText, Label... statsLabels) {
        try {
            List<ClientRow> clients = dataManager.getClientService().loadClientsFromDatabase();
            // Appliquer les filtres et mettre à jour les statistiques
            table.setItems(FXCollections.observableArrayList(clients));
            
            // Mise à jour des statistiques (exemple)
            if (statsLabels.length >= 3) {
                statsLabels[0].setText("Total: " + clients.size());
                // Autres calculs de stats...
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des clients: " + e.getMessage(), e);
        }
    }
    
    private void loadCompaniesDataWithFilter(TableView<CompanyRow> table, String typeFilter, String searchText, Label... statsLabels) {
        try {
            List<CompanyRow> companies = dataManager.getCompanyService().loadCompaniesFromDatabase();
            // Appliquer les filtres et mettre à jour les statistiques
            table.setItems(FXCollections.observableArrayList(companies));
            
            // Mise à jour des statistiques (exemple)
            if (statsLabels.length >= 1) {
                statsLabels[0].setText("Total: " + companies.size());
                // Autres calculs de stats...
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des sociétés: " + e.getMessage(), e);
        }
    }

    @Override
    public void refreshAllTables() {
        // Rechargement des données - à implémenter selon les besoins
        System.out.println("Refresh des tables de gestion");
    }

    @Override
    public String getComponentName() {
        return "GestionController";
    }
}