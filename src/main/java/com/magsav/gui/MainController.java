package com.magsav.gui;

import com.magsav.gui.dialogs.ShareDialogs;
import com.magsav.model.InterventionRow;
import com.magsav.model.Societe;

import com.magsav.repo.InterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.CategoryRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.repo.RequestRepository;
import com.magsav.model.Category;
import com.magsav.service.DataChangeEvent;
import com.magsav.service.DataChangeNotificationService;
import com.magsav.service.NavigationService;
import com.magsav.service.ProductServiceStatic;

import com.magsav.service.ShareService;
import com.magsav.util.AppLogger;
import com.magsav.dto.*;
import com.magsav.service.data.UserDataService;
import com.magsav.service.data.RequestDataService;
import com.magsav.service.data.ClientDataService;
import com.magsav.service.data.CompanyDataService;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;


import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MainController {
  // Services
  private final UserDataService userDataService = new UserDataService();
  private final RequestDataService requestDataService = new RequestDataService();
  private final ClientDataService clientDataService = new ClientDataService();
  private final CompanyDataService companyDataService = new CompanyDataService();
  
  // Sidebar Navigation
  @FXML private Button dashboardBtn, gestionBtn, demandesBtn, interventionsBtn;
  @FXML private Button stockBtn, statistiquesBtn, exportBtn;
  @FXML private Button preferencesBtn, apparenceBtn;
  
  // Main TabPane
  @FXML private TabPane mainTabPane;
  
  // Navigation Elements - Nouvelles HBox avec icônes + légendes
  @FXML private HBox dashboardItem, gestionItem, demandesItem, interventionsItem;
  @FXML private HBox stockItem, vehiculesItem, statistiquesItem, exportItem, preferencesItem;
  @FXML private HBox technicienUsersItem;
  
  // UI Elements (créés dynamiquement dans les onglets)
  private TableView<ProductRepository.ProductRow> productTable;
  private TableColumn<ProductRepository.ProductRow, String> colProdNom, colProdSN, colProdUID, colProdFabricant, colProdSituation;
  private TextField productSearchField;
  private Label totalInterventionsLabel;
  private Label totalDemandesLabel;
  private Label totalProduitsLabel;
  private ListView<String> recentActivityList;

  // Company Elements
  @FXML private ImageView companyLogoImage;
  @FXML private Label companyNameLabel;

    // Old UI elements - commented out for new design
  // @FXML private Label lblProdName, lblProdCategory, lblProdSubcategory, lblProdSubSubcategory,
  //                     lblProdManufacturer, lblProdSituation, statusIndicator, historyCountLabel;
  // @FXML private Label lblCategoryTitle, lblSubcategoryTitle, lblSubSubcategoryTitle;
  // @FXML private Label lblQrUID, lblQrSN;
  // @FXML private ImageView imgProductPhoto, imgManufacturerLogo, imgQr;
  // @FXML private TableView<InterventionRow> historyTable;
  // @FXML private TableColumn<InterventionRow, Long> hColId;
  // @FXML private TableColumn<InterventionRow, String> hColStatut, hColPanne, hColEntree, hColSortie;
  // @FXML private Button btnEditProduct;
  // @FXML private Button btnExportProduct, btnPrintProduct, btnEmailProduct, btnShareProduct;
  // @FXML private Button btnClose;

  // Services statiques utilisés
  
  // Repositories pour certaines opérations spécifiques
  private final ProductRepository productRepo = new ProductRepository();
  private final InterventionRepository interventionRepo = new InterventionRepository();
  private final CategoryRepository categoryRepo = new CategoryRepository();
  private final RequestRepository requestRepo = new RequestRepository();

  
  // Service de partage
  private ShareService shareService;
  
  private FilteredList<ProductRepository.ProductRow> filteredProducts;
  private Long currentProductId;
  
  // Composants du volet de détail des produits
  private Label productNameDetail;
  private Label productReferenceDetail;
  private Label productCategoryDetail;
  private Label productStockDetail;
  private Label productPriceDetail;
  private Button editProductBtn;
  private Button deleteProductBtn;
  
  // TreeView pour les catégories
  private TreeView<CategoryTreeItem> categoriesTreeView;

  @FXML
  private void initialize() {
    // Initialisation du logo de la société
    loadCompanyLogo();
    
    // Initialisation du service de partage
    shareService = new ShareService(productRepo, interventionRepo);
    
    // Configuration des callbacks pour le retour utilisateur
    shareService.setLogCallback(message -> AppLogger.info("Share: " + message));
    shareService.setProgressCallback(progress -> {
      // Le progress sera géré par les dialogues
    });
    
    // Initialiser les éléments UI dynamiques AVANT de les utiliser
    initializeDynamicComponents();
    
    // S'abonner aux notifications de changement de données pour rafraîchissement automatique
    DataChangeNotificationService.getInstance().subscribe(this::onDataChanged);

    // Charger les données
    onRefresh();
    
    // Charger la section Gestion par défaut (sans Véhicules)
    loadGestionSection();
    
    // Set default active navigation item
    setActiveNavItem(gestionItem);
  }
  
  // === SIDEBAR NAVIGATION METHODS ===
  

  
  @FXML
  private void onShowDashboard() {
    setActiveNavItem(dashboardItem);
    loadDashboardSection();
  }
  
  @FXML
  private void onShowGestion() {
    setActiveNavItem(gestionItem);
    loadGestionSection();
  }
  
  @FXML
  private void onShowDemandes() {
    setActiveNavItem(demandesItem);
    loadDemandesSection();
  }
  
  @FXML
  private void onShowInterventions() {
    setActiveNavItem(interventionsItem);
    loadInterventionsSection();
  }
  
  @FXML
  private void onShowStock() {
    setActiveNavItem(stockItem);
    loadStockSection();
  }
  
  @FXML
  private void onShowVehicules() {
    setActiveNavItem(vehiculesItem);
    loadVehiculesSection();
  }
  
  @FXML
  private void onShowStatistiques() {
    setActiveNavItem(statistiquesItem);
    loadStatistiquesSection();
  }
  
  @FXML
  private void onShowExport() {
    setActiveNavItem(exportItem);
    loadExportSection();
  }
  
  @FXML
  private void onShowPreferences() {
    setActiveNavItem(preferencesItem);
    loadPreferencesSection();
  }
  
  @FXML
  private void onShowTechnicienUsers() {
    setActiveNavItem(technicienUsersItem);
    loadTechnicienUsersSection();
  }
  
  @FXML
  private void onGenerateTestData() {
    try {
      // Générer les données de test (forcer même si des données existent)
      com.magsav.util.SimpleTestDataGenerator.generateTestData(true);
      
      // Rafraîchir les données affichées
      onRefresh();
      
      // Afficher une confirmation
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Données de test");
      alert.setHeaderText("Génération terminée");
      alert.setContentText("Les données de test ont été générées avec succès !");
      alert.showAndWait();
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de la génération des données de test: " + e.getMessage(), e);
      
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erreur");
      alert.setHeaderText("Erreur lors de la génération");
      alert.setContentText("Détails: " + e.getMessage());
      alert.showAndWait();
    }
  }

  private void showFeatureNotImplemented(String featureName) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Fonctionnalité en développement");
    alert.setHeaderText(featureName);
    alert.setContentText("Cette fonctionnalité sera disponible dans une prochaine version.");
    alert.showAndWait();
  }

  @FXML
  private void onSearchProducts() {
    // Trigger product search
    String searchText = productSearchField.getText();
    if (filteredProducts != null) {
      filteredProducts.setPredicate(product -> {
        if (searchText == null || searchText.isEmpty()) {
          return true;
        }
        String lowerCaseFilter = searchText.toLowerCase();
        return product.nom().toLowerCase().contains(lowerCaseFilter) ||
               (product.sn() != null && product.sn().toLowerCase().contains(lowerCaseFilter)) ||
               (product.fabricant() != null && product.fabricant().toLowerCase().contains(lowerCaseFilter));
      });
    }
  }
  
  private VBox createProductDetailPanel() {
    VBox detailPanel = new VBox();
    detailPanel.setSpacing(0);
    detailPanel.setPrefWidth(300);
    detailPanel.getStyleClass().add("detail-panel");
    
    // Titre du volet
    Label detailTitle = new Label("Détails du produit");
    detailTitle.getStyleClass().add("detail-title");
    
    // Zone d'image du produit
    VBox imageBox = new VBox();
    imageBox.setSpacing(0);
    imageBox.setAlignment(javafx.geometry.Pos.CENTER);
    imageBox.setPrefHeight(200);
    imageBox.getStyleClass().add("product-image-box");
    
    Label imageLabel = new Label("📷");
    imageLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #666666;");
    
    Label noImageLabel = new Label("Aucune image disponible");
    noImageLabel.getStyleClass().add("no-image-label");
    
    imageBox.getChildren().addAll(imageLabel, noImageLabel);
    
    // Informations du produit
    VBox infoBox = new VBox();
    infoBox.setSpacing(0);
    
    Label productNameLabel = new Label("Nom :");
    productNameLabel.getStyleClass().add("info-label");
    Label productName = new Label("Sélectionner un produit");
    productName.getStyleClass().add("info-value");
    
    Label referenceLabel = new Label("Référence :");
    referenceLabel.getStyleClass().add("info-label");
    Label reference = new Label("-");
    reference.getStyleClass().add("info-value");
    
    Label categoryLabel = new Label("Catégorie :");
    categoryLabel.getStyleClass().add("info-label");
    Label category = new Label("-");
    category.getStyleClass().add("info-value");
    
    Label stockLabel = new Label("Stock :");
    stockLabel.getStyleClass().add("info-label");
    Label stock = new Label("-");
    stock.getStyleClass().add("info-value");
    
    Label priceLabel = new Label("Prix unitaire :");
    priceLabel.getStyleClass().add("info-label");
    Label price = new Label("-");
    price.getStyleClass().add("info-value");
    
    infoBox.getChildren().addAll(
      productNameLabel, productName,
      referenceLabel, reference,
      categoryLabel, category,
      stockLabel, stock,
      priceLabel, price
    );
    
    // Boutons d'action
    HBox buttonsBox = new HBox();
    buttonsBox.setSpacing(8);
    
    Button editBtn = new Button("Modifier");
    editBtn.getStyleClass().add("primary-button");
    editBtn.setDisable(true);
    
    Button deleteBtn = new Button("Supprimer");
    deleteBtn.getStyleClass().add("danger-button");
    deleteBtn.setDisable(true);
    
    buttonsBox.getChildren().addAll(editBtn, deleteBtn);
    
    // Espacement
    Region spacer = new Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    detailPanel.getChildren().addAll(detailTitle, imageBox, infoBox, spacer, buttonsBox);
    
    // Sauvegarder les références pour mise à jour
    this.productNameDetail = productName;
    this.productReferenceDetail = reference;
    this.productCategoryDetail = category;
    this.productStockDetail = stock;
    this.productPriceDetail = price;
    this.editProductBtn = editBtn;
    this.deleteProductBtn = deleteBtn;
    
    return detailPanel;
  }
  
  private void openNewProductDialog() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/products/forms/product_form.fxml"));
      Parent root = loader.load();
      
      Stage dialog = new Stage();
      dialog.setTitle("Nouveau produit");
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(mainTabPane.getScene().getWindow());
      
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      dialog.setScene(scene);
      
      dialog.showAndWait();
      
      // Actualiser la liste des produits
      refreshProductList();
      
    } catch (Exception e) {
      e.printStackTrace();
      showAlert("Erreur", "Impossible d'ouvrir le dialogue de création de produit : " + e.getMessage());
    }
  }
  
  private void refreshProductList() {
    if (filteredProducts != null) {
      List<ProductRepository.ProductRow> allProducts = productRepo.findAllProductsWithUID();
      productTable.setItems(FXCollections.observableArrayList(allProducts));
      filteredProducts = new FilteredList<>(productTable.getItems());
      productTable.setItems(filteredProducts);
    }
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
  
  // === MÉTHODES DE CONTENU POUR LES INTERVENTIONS ===
  
  private SplitPane createInterventionsListContent() {
    // Créer le SplitPane principal
    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getStyleClass().add("split-pane");
    
    // Partie gauche - Liste des interventions
    VBox leftPane = new VBox();
    leftPane.setSpacing(16);
    leftPane.getStyleClass().add("main-content");
    
    // En-tête
    VBox headerBox = new VBox();
    headerBox.setSpacing(0);
    headerBox.getStyleClass().add("content-header");
    
    HBox searchBox = new HBox();
    searchBox.setSpacing(16);
    searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    Label subtitle = new Label("Toutes les interventions");
    subtitle.getStyleClass().add("content-subtitle");
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    TextField searchField = new TextField();
    searchField.setPromptText("Rechercher...");
    searchField.getStyleClass().add("dark-text-field");
    searchField.setPrefWidth(200);
    
    ComboBox<String> statusFilter = new ComboBox<>();
    statusFilter.getItems().addAll("Tous", "En cours", "Terminées", "Annulées");
    statusFilter.setValue("Tous");
    statusFilter.getStyleClass().add("dark-combo-box");
    
    Button searchBtn = new Button("🔍");
    searchBtn.getStyleClass().add("dark-button-secondary");
    
    searchBox.getChildren().addAll(subtitle, spacer, statusFilter, searchField, searchBtn);
    headerBox.getChildren().add(searchBox);
    
    // Table des interventions
    TableView<InterventionRow> interventionTable = new TableView<>();
    interventionTable.getStyleClass().add("dark-table");
    
    // Configuration des colonnes et double-clic
    setupInterventionTableColumns(interventionTable);
    
    // Charger les données
    loadInterventionsData(interventionTable);
    
    VBox.setVgrow(interventionTable, javafx.scene.layout.Priority.ALWAYS);
    leftPane.getChildren().addAll(headerBox, interventionTable);
    
    // Partie droite - Panneau de détails
    VBox rightPane = createInterventionDetailPanel();
    
    splitPane.getItems().addAll(leftPane, rightPane);
    splitPane.setDividerPositions(0.65);
    
    // Gestion de la sélection
    interventionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        updateInterventionDetailPanel(rightPane, newSelection);
      }
    });
    
    return splitPane;
  }
  
  private VBox createNewInterventionContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Nouvelle intervention");
    title.getStyleClass().add("content-title");
    
    Label subtitle = new Label("Cette fonctionnalité ouvrira le formulaire de nouvelle intervention");
    subtitle.getStyleClass().add("content-subtitle");
    
    Button openFormBtn = new Button("Ouvrir le formulaire");
    openFormBtn.getStyleClass().add("primary-button");
    openFormBtn.setOnAction(e -> openNewInterventionDialog());
    
    content.getChildren().addAll(title, subtitle, openFormBtn);
    
    return content;
  }
  
  private VBox createInterventionsEnCoursContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Interventions en cours");
    title.getStyleClass().add("content-title");
    
    // Table filtrée sur les interventions en cours
    TableView<InterventionRow> tableEnCours = new TableView<>();
    tableEnCours.getStyleClass().add("dark-table");
    
    // Réutiliser les mêmes colonnes que la liste principale
    setupInterventionTableColumns(tableEnCours);
    
    // Charger seulement les interventions en cours
    loadInterventionsEnCoursData(tableEnCours);
    
    VBox.setVgrow(tableEnCours, javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().addAll(title, tableEnCours);
    
    return content;
  }
  
  private VBox createInterventionsTermineesContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Interventions terminées");
    title.getStyleClass().add("content-title");
    
    // Table filtrée sur les interventions terminées
    TableView<InterventionRow> tableTerminees = new TableView<>();
    tableTerminees.getStyleClass().add("dark-table");
    
    // Réutiliser les mêmes colonnes que la liste principale
    setupInterventionTableColumns(tableTerminees);
    
    // Charger seulement les interventions terminées
    loadInterventionsTermineesData(tableTerminees);
    
    VBox.setVgrow(tableTerminees, javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().addAll(title, tableTerminees);
    
    return content;
  }
  
  // Méthodes utilitaires pour les interventions
  
  private void setupInterventionTableColumns(TableView<InterventionRow> table) {
    TableColumn<InterventionRow, String> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.valueOf(data.getValue().id())));
    idCol.setPrefWidth(60);
    
    TableColumn<InterventionRow, String> produitCol = new TableColumn<>("Produit");
    produitCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().produitNom()));
    produitCol.setPrefWidth(150);
    
    TableColumn<InterventionRow, String> statutCol = new TableColumn<>("Statut");
    statutCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().statut()));
    statutCol.setPrefWidth(100);
    
    TableColumn<InterventionRow, String> panneCol = new TableColumn<>("Panne");
    panneCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().panne()));
    panneCol.setPrefWidth(200);
    
    TableColumn<InterventionRow, String> dateEntreeCol = new TableColumn<>("Date d'entrée");
    dateEntreeCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().dateEntree()));
    dateEntreeCol.setPrefWidth(100);
    
    TableColumn<InterventionRow, String> dateSortieCol = new TableColumn<>("Date de sortie");
    dateSortieCol.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().dateSortie()));
    dateSortieCol.setPrefWidth(100);
    
    var columns = table.getColumns();
    columns.addAll(Arrays.asList(idCol, produitCol, statutCol, panneCol, dateEntreeCol, dateSortieCol));
    
    // Configurer les double-clics pour ouvrir les détails de l'intervention
    table.setRowFactory(tv -> {
      TableRow<InterventionRow> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          InterventionRow intervention = row.getItem();
          AppLogger.info("Double-clic sur intervention ID: " + intervention.id());
          NavigationService.openInterventionDetail(intervention.id());
        }
      });
      return row;
    });
  }
  
  private void loadInterventionsData(TableView<InterventionRow> table) {
    try {
      List<InterventionRow> interventions = interventionRepo.findAllWithProductName();
      table.setItems(FXCollections.observableArrayList(interventions));
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des interventions: " + e.getMessage(), e);
    }
  }
  
  private void loadInterventionsEnCoursData(TableView<InterventionRow> table) {
    try {
      List<InterventionRow> interventions = interventionRepo.findAllWithProductName();
      List<InterventionRow> enCours = interventions.stream()
        .filter(i -> i.dateSortie() == null || i.dateSortie().trim().isEmpty())
        .toList();
      table.setItems(FXCollections.observableArrayList(enCours));
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des interventions en cours: " + e.getMessage(), e);
    }
  }
  
  private void loadInterventionsTermineesData(TableView<InterventionRow> table) {
    try {
      List<InterventionRow> interventions = interventionRepo.findAllWithProductName();
      List<InterventionRow> terminees = interventions.stream()
        .filter(i -> i.dateSortie() != null && !i.dateSortie().trim().isEmpty())
        .toList();
      table.setItems(FXCollections.observableArrayList(terminees));
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des interventions terminées: " + e.getMessage(), e);
    }
  }
  
  private void openNewInterventionDialog() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/interventions/forms/new_intervention.fxml"));
      Parent root = loader.load();
      
      Stage dialog = new Stage();
      dialog.setTitle("Nouvelle intervention");
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(mainTabPane.getScene().getWindow());
      
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      dialog.setScene(scene);
      
      dialog.showAndWait();
      
    } catch (Exception e) {
      e.printStackTrace();
      showAlert("Erreur", "Impossible d'ouvrir le formulaire de nouvelle intervention : " + e.getMessage());
    }
  }
  
  // === MÉTHODES DE CONTENU POUR LE STOCK ===
  
  private VBox createStockOverviewContent() {
    VBox content = new VBox();
    content.setSpacing(0);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // Métriques du stock
    HBox metricsBox = new HBox();
    metricsBox.setSpacing(0);
    metricsBox.getStyleClass().add("metrics-container");
    
    VBox totalBox = createStockMetricBox("Total produits", "322", "#4a90e2");
    VBox stockBasBox = createStockMetricBox("Stock bas", "12", "#ff6b6b");
    VBox valeurBox = createStockMetricBox("Valeur totale", "€45,234", "#51cf66");
    VBox mouvementsBox = createStockMetricBox("Mouvements (7j)", "28", "#ffd43b");
    
    metricsBox.getChildren().addAll(totalBox, stockBasBox, valeurBox, mouvementsBox);
    
    // Graphique simple représentant l'évolution du stock
    VBox chartBox = new VBox();
    chartBox.setSpacing(12);
    chartBox.getStyleClass().add("content-section");
    
    Label chartTitle = new Label("Évolution du stock (30 derniers jours)");
    chartTitle.getStyleClass().add("section-title");
    
    // Placeholder pour graphique
    VBox chartPlaceholder = new VBox();
    chartPlaceholder.setMinHeight(200);
    chartPlaceholder.setAlignment(javafx.geometry.Pos.CENTER);
    chartPlaceholder.getStyleClass().add("chart-placeholder");
    
    Label chartLabel = new Label("📊 Graphique d'évolution du stock");
    chartLabel.getStyleClass().add("placeholder-text");
    Label chartSubtitle = new Label("(Graphique détaillé disponible dans la section Statistiques)");
    chartSubtitle.getStyleClass().add("placeholder-subtitle");
    
    chartPlaceholder.getChildren().addAll(chartLabel, chartSubtitle);
    chartBox.getChildren().addAll(chartTitle, chartPlaceholder);
    
    // Actions rapides
    HBox actionsBox = new HBox();
    actionsBox.setSpacing(12);
    actionsBox.getStyleClass().add("actions-container");
    
    Button ajustementBtn = new Button("Ajustement stock");
    ajustementBtn.getStyleClass().add("primary-button");
    ajustementBtn.setOnAction(e -> showAlert("Info", "Fonctionnalité d'ajustement de stock à implémenter"));
    
    Button inventaireBtn = new Button("Nouvel inventaire");
    inventaireBtn.getStyleClass().add("dark-button-secondary");
    inventaireBtn.setOnAction(e -> showAlert("Info", "Fonctionnalité d'inventaire à implémenter"));
    
    Button exportBtn = new Button("Exporter le stock");
    exportBtn.getStyleClass().add("dark-button-secondary");
    exportBtn.setOnAction(e -> showAlert("Info", "Export du stock à implémenter"));
    
    actionsBox.getChildren().addAll(ajustementBtn, inventaireBtn, exportBtn);
    
    content.getChildren().addAll(metricsBox, chartBox, actionsBox);
    
    return content;
  }
  
  private VBox createVehiculesListContent() {
    VBox content = new VBox();
    content.setSpacing(0);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // Métriques des véhicules
    HBox metricsBox = new HBox();
    metricsBox.setSpacing(20);
    metricsBox.getStyleClass().add("metrics-container");
    
    VBox totalBox = createStockMetricBox("Total véhicules", "12", "#4a90e2");
    VBox disponiblesBox = createStockMetricBox("Disponibles", "8", "#51cf66");
    VBox maintenanceBox = createStockMetricBox("En maintenance", "3", "#ffd43b");
    VBox pannesBox = createStockMetricBox("En panne", "1", "#ff6b6b");
    
    metricsBox.getChildren().addAll(totalBox, disponiblesBox, maintenanceBox, pannesBox);
    
    // Table des véhicules
    VBox tableSection = new VBox();
    tableSection.setSpacing(0);
    tableSection.getStyleClass().add("content-section");
    
    // Créer la table des véhicules
    TableView<com.magsav.model.Vehicule> vehiculesTable = new TableView<>();
    vehiculesTable.getStyleClass().add("dark-table-view");
    vehiculesTable.setPrefHeight(400);
    
    // Colonnes de la table
    TableColumn<com.magsav.model.Vehicule, String> colImmatriculation = new TableColumn<>("Immatriculation");
    colImmatriculation.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
    colImmatriculation.setPrefWidth(130);
    
    TableColumn<com.magsav.model.Vehicule, String> colType = new TableColumn<>("Type");
    colType.setCellValueFactory(new PropertyValueFactory<>("typeVehicule"));
    colType.setPrefWidth(120);
    
    TableColumn<com.magsav.model.Vehicule, String> colMarque = new TableColumn<>("Marque");
    colMarque.setCellValueFactory(new PropertyValueFactory<>("marque"));
    colMarque.setPrefWidth(100);
    
    TableColumn<com.magsav.model.Vehicule, String> colModele = new TableColumn<>("Modèle");
    colModele.setCellValueFactory(new PropertyValueFactory<>("modele"));
    colModele.setPrefWidth(120);
    
    TableColumn<com.magsav.model.Vehicule, String> colStatut = new TableColumn<>("Statut");
    colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
    colStatut.setPrefWidth(100);
    
    TableColumn<com.magsav.model.Vehicule, String> colKilometrage = new TableColumn<>("Kilométrage");
    colKilometrage.setCellValueFactory(cellData -> {
      int km = cellData.getValue().getKilometrage();
      return new ReadOnlyStringWrapper(String.format("%,d km", km));
    });
    colKilometrage.setPrefWidth(120);
    
    vehiculesTable.getColumns().addAll(Arrays.asList(colImmatriculation, colType, colMarque, colModele, colStatut, colKilometrage));
    
    // Gestion du double-clic pour ouvrir les détails du véhicule
    vehiculesTable.setRowFactory(tv -> {
      TableRow<com.magsav.model.Vehicule> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          com.magsav.model.Vehicule vehicule = row.getItem();
          if (vehicule != null) {
            NavigationService.openVehiculeDetail(vehicule.getId());
          }
        }
      });
      return row;
    });
    
    // Charger les données des véhicules
    loadVehiculesData(vehiculesTable);
    
    tableSection.getChildren().add(vehiculesTable);
    
    // Actions
    HBox actionsBox = new HBox();
    actionsBox.setSpacing(12);
    actionsBox.getStyleClass().add("actions-container");
    
    Button addBtn = new Button("Ajouter véhicule");
    addBtn.getStyleClass().add("primary-button");
    addBtn.setOnAction(e -> showAlert("Info", "Ajout de véhicule à implémenter"));
    
    Button maintenanceBtn = new Button("Planifier maintenance");
    maintenanceBtn.getStyleClass().add("dark-button-secondary");
    maintenanceBtn.setOnAction(e -> showAlert("Info", "Planification maintenance à implémenter"));
    
    actionsBox.getChildren().addAll(addBtn, maintenanceBtn);
    
    // Créer le panneau de détails à droite pour les véhicules
    VBox vehiculeDetailPanel = createVehiculeDetailPanel();
    vehiculeDetailPanel.setVisible(false); // Masqué par défaut
    
    // Créer le SplitPane horizontal pour la table et les détails
    javafx.scene.control.SplitPane tableSplitPane = new javafx.scene.control.SplitPane();
    tableSplitPane.getStyleClass().add("split-pane");
    tableSplitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    
    // Créer un VBox pour la section table sans le titre (qui reste dans content)
    VBox tableWithoutTitle = new VBox();
    tableWithoutTitle.setSpacing(12);
    tableWithoutTitle.getChildren().add(vehiculesTable);
    
    // Ajouter la table et le panneau de détails au SplitPane
    tableSplitPane.getItems().addAll(tableWithoutTitle, vehiculeDetailPanel);
    tableSplitPane.setDividerPositions(0.65); // 65% pour la table, 35% pour les détails
    
    // Gestion de la sélection pour afficher les détails
    vehiculesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      if (newSel != null) {
        updateVehiculeDetailPanel(vehiculeDetailPanel, newSel);
        vehiculeDetailPanel.setVisible(true);
      } else {
        vehiculeDetailPanel.setVisible(false);
      }
    });
    
    // Modifier tableSection pour inclure le SplitPane
    tableSection.getChildren().clear();
    tableSection.getChildren().add(tableSplitPane);
    
    content.getChildren().addAll(metricsBox, tableSection, actionsBox);
    
    return content;
  }
  
  private VBox createVehiculesPlanningContent() {
    VBox content = new VBox();
    content.setSpacing(0);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // Calendrier simple
    VBox calendarSection = new VBox();
    calendarSection.setSpacing(12);
    calendarSection.getStyleClass().add("content-section");
    
    Label calendarTitle = new Label("Disponibilité des véhicules");
    calendarTitle.getStyleClass().add("section-title");
    
    VBox calendarPlaceholder = new VBox();
    calendarPlaceholder.setMinHeight(400);
    calendarPlaceholder.setAlignment(javafx.geometry.Pos.CENTER);
    calendarPlaceholder.getStyleClass().add("chart-placeholder");
    
    Label calendarIcon = new Label("📅");
    calendarIcon.setStyle("-fx-font-size: 48px;");
    
    Label calendarLabel = new Label("Calendrier de planning");
    calendarLabel.getStyleClass().add("placeholder-text");
    
    Label calendarSubtitle = new Label("Visualisation des réservations, maintenances et disponibilités");
    calendarSubtitle.getStyleClass().add("placeholder-subtitle");
    
    calendarPlaceholder.getChildren().addAll(calendarIcon, calendarLabel, calendarSubtitle);
    calendarSection.getChildren().addAll(calendarTitle, calendarPlaceholder);
    
    content.getChildren().add(calendarSection);
    
    return content;
  }
  
  private VBox createStockMouvementsContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // En-tête
    VBox headerBox = new VBox();
    headerBox.setSpacing(8);
    headerBox.getStyleClass().add("content-header");
    
    Label title = new Label("Mouvements de stock");
    title.getStyleClass().add("content-title");
    
    HBox searchBox = new HBox();
    searchBox.setSpacing(16);
    searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    Label subtitle = new Label("Historique des entrées et sorties");
    subtitle.getStyleClass().add("content-subtitle");
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    ComboBox<String> typeFilter = new ComboBox<>();
    typeFilter.getItems().addAll("Tous les mouvements", "Entrées", "Sorties", "Ajustements");
    typeFilter.setValue("Tous les mouvements");
    typeFilter.getStyleClass().add("dark-combo-box");
    
    DatePicker datePicker = new DatePicker();
    datePicker.setPromptText("Filtrer par date");
    datePicker.getStyleClass().add("dark-date-picker");
    
    searchBox.getChildren().addAll(subtitle, spacer, typeFilter, datePicker);
    headerBox.getChildren().addAll(title, searchBox);
    
    // Table des mouvements (structure simulée)
    TableView<String> mouvementsTable = new TableView<>();
    mouvementsTable.getStyleClass().add("dark-table");
    
    TableColumn<String, String> dateCol = new TableColumn<>("Date");
    dateCol.setPrefWidth(100);
    
    TableColumn<String, String> produitCol = new TableColumn<>("Produit");
    produitCol.setPrefWidth(150);
    
    TableColumn<String, String> typeCol = new TableColumn<>("Type");
    typeCol.setPrefWidth(100);
    
    TableColumn<String, String> quantiteCol = new TableColumn<>("Quantité");
    quantiteCol.setPrefWidth(80);
    
    TableColumn<String, String> utilisateurCol = new TableColumn<>("Utilisateur");
    utilisateurCol.setPrefWidth(100);
    
    TableColumn<String, String> commentaireCol = new TableColumn<>("Commentaire");
    commentaireCol.setPrefWidth(200);
    
    mouvementsTable.getColumns().addAll(Arrays.asList(dateCol, produitCol, typeCol, quantiteCol, utilisateurCol, commentaireCol));
    
    // Placeholder pour les données
    Label placeholder = new Label("Aucun mouvement de stock récent");
    placeholder.getStyleClass().add("table-placeholder");
    mouvementsTable.setPlaceholder(placeholder);
    
    VBox.setVgrow(mouvementsTable, javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().addAll(headerBox, mouvementsTable);
    
    return content;
  }
  
  private VBox createStockAlertesContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Alertes de stock");
    title.getStyleClass().add("content-title");
    
    // Liste des alertes
    VBox alertesBox = new VBox();
    alertesBox.setSpacing(12);
    
    // Alerte stock bas
    HBox alerte1 = createStockAlert("⚠️", "Stock bas", "12 produits ont un stock inférieur au seuil", "#ff6b6b");
    HBox alerte2 = createStockAlert("🔴", "Rupture", "3 produits sont en rupture de stock", "#e74c3c");
    HBox alerte3 = createStockAlert("📋", "Inventaire", "Inventaire recommandé dans 15 jours", "#f39c12");
    
    alertesBox.getChildren().addAll(alerte1, alerte2, alerte3);
    
    // Actions
    HBox actionsBox = new HBox();
    actionsBox.setSpacing(12);
    actionsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    Button configAlertes = new Button("Configurer les alertes");
    configAlertes.getStyleClass().add("primary-button");
    configAlertes.setOnAction(e -> showAlert("Info", "Configuration des alertes à implémenter"));
    
    Button exportAlertes = new Button("Exporter les alertes");
    exportAlertes.getStyleClass().add("dark-button-secondary");
    exportAlertes.setOnAction(e -> showAlert("Info", "Export des alertes à implémenter"));
    
    actionsBox.getChildren().addAll(configAlertes, exportAlertes);
    
    content.getChildren().addAll(title, alertesBox, actionsBox);
    
    return content;
  }
  
  private VBox createStockRapportsContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Rapports de stock");
    title.getStyleClass().add("content-title");
    
    // Types de rapports disponibles
    VBox rapportsBox = new VBox();
    rapportsBox.setSpacing(16);
    
    VBox rapport1 = createRapportOption("📊 Rapport de valorisation", "Valeur du stock par catégorie et période");
    VBox rapport2 = createRapportOption("📈 Analyse de rotation", "Produits à rotation lente/rapide");
    VBox rapport3 = createRapportOption("📋 Inventaire complet", "Liste détaillée de tous les produits");
    VBox rapport4 = createRapportOption("🔄 Historique mouvements", "Détail des entrées/sorties par période");
    
    rapportsBox.getChildren().addAll(rapport1, rapport2, rapport3, rapport4);
    
    content.getChildren().addAll(title, rapportsBox);
    
    return content;
  }
  
  // Méthodes utilitaires pour le stock
  
  private VBox createStockMetricBox(String label, String value, String color) {
    VBox box = new VBox();
    box.setSpacing(4);
    box.getStyleClass().add("metric-box");
    box.setAlignment(javafx.geometry.Pos.CENTER);
    
    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add("metric-value");
    valueLabel.setStyle("-fx-text-fill: " + color + ";");
    
    Label labelText = new Label(label);
    labelText.getStyleClass().add("metric-label");
    
    box.getChildren().addAll(valueLabel, labelText);
    
    return box;
  }
  
  private HBox createStockAlert(String icon, String type, String message, String color) {
    HBox alert = new HBox();
    alert.setSpacing(12);
    alert.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    alert.getStyleClass().add("alert-box");
    alert.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 0 4;");
    
    Label iconLabel = new Label(icon);
    iconLabel.getStyleClass().add("alert-icon");
    
    VBox textBox = new VBox();
    textBox.setSpacing(2);
    
    Label typeLabel = new Label(type);
    typeLabel.getStyleClass().add("alert-type");
    typeLabel.setStyle("-fx-text-fill: " + color + ";");
    
    Label messageLabel = new Label(message);
    messageLabel.getStyleClass().add("alert-message");
    
    textBox.getChildren().addAll(typeLabel, messageLabel);
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    Button actionBtn = new Button("Voir");
    actionBtn.getStyleClass().add("alert-action");
    actionBtn.setOnAction(e -> showAlert("Info", "Détails de l'alerte à implémenter"));
    
    alert.getChildren().addAll(iconLabel, textBox, spacer, actionBtn);
    
    return alert;
  }
  
  private VBox createRapportOption(String title, String description) {
    VBox box = new VBox();
    box.setSpacing(8);
    box.getStyleClass().add("rapport-option");
    
    HBox headerBox = new HBox();
    headerBox.setSpacing(12);
    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("rapport-title");
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    Button generateBtn = new Button("Générer");
    generateBtn.getStyleClass().add("primary-button");
    generateBtn.setOnAction(e -> showAlert("Info", "Génération du rapport à implémenter"));
    
    headerBox.getChildren().addAll(titleLabel, spacer, generateBtn);
    
    Label descLabel = new Label(description);
    descLabel.getStyleClass().add("rapport-description");
    
    box.getChildren().addAll(headerBox, descLabel);
    
    return box;
  }
  

  
  private void updateDashboardStats() {
    CompletableFuture.runAsync(() -> {
      try {
        // Get statistics from repositories
        int totalProducts = productRepo.getTotalProductCount();
        int totalInterventions = interventionRepo.getTotalInterventionCount();
        int totalDemandes = requestRepo.findAll().size();
        
        // Update UI on JavaFX thread
        Platform.runLater(() -> {
          totalProduitsLabel.setText(String.valueOf(totalProducts));
          totalInterventionsLabel.setText(String.valueOf(totalInterventions));
          totalDemandesLabel.setText(String.valueOf(totalDemandes));
          
          // Update recent activity (simplified for now)
          recentActivityList.setItems(FXCollections.observableArrayList(
            "Intervention #1234 créée",
            "Produit ABC123 mis à jour",
            "Demande #5678 traitée",
            "Export terminé avec succès"
          ));
        });
      } catch (Exception e) {
        AppLogger.error("Error updating dashboard stats", e);
      }
    });
  }
  
  // === DASHBOARD ACTION METHODS ===
  
  @FXML
  private void onNewInterventionDashboard() {
    showFeatureNotImplemented("Création d'intervention depuis le tableau de bord");
  }
  
  @FXML
  private void onNewDemande() {
    showFeatureNotImplemented("Création de nouvelle demande");
  }
  
  @FXML
  private void onShowRapports() {
    showFeatureNotImplemented("Affichage des rapports");
  }
  
  // === NAVIGATION HELPER METHODS ===
  
  private void setActiveNavItem(HBox activeItem) {
    // Supprimer la classe active de tous les éléments de navigation
    if (dashboardItem != null) dashboardItem.getStyleClass().remove("active");
    if (gestionItem != null) gestionItem.getStyleClass().remove("active");
    if (demandesItem != null) demandesItem.getStyleClass().remove("active");
    if (interventionsItem != null) interventionsItem.getStyleClass().remove("active");
    if (stockItem != null) stockItem.getStyleClass().remove("active");
    if (vehiculesItem != null) vehiculesItem.getStyleClass().remove("active");
    if (statistiquesItem != null) statistiquesItem.getStyleClass().remove("active");
    if (exportItem != null) exportItem.getStyleClass().remove("active");
    if (preferencesItem != null) preferencesItem.getStyleClass().remove("active");
    if (technicienUsersItem != null) technicienUsersItem.getStyleClass().remove("active");
    
    // Ajouter la classe active à l'élément sélectionné
    if (activeItem != null && !activeItem.getStyleClass().contains("active")) {
      activeItem.getStyleClass().add("active");
    }
  }
  
  /**
   * Gère les événements de changement de données pour rafraîchissement automatique
   */
  private void onDataChanged(DataChangeEvent event) {
    switch (event.getType()) {
      case PRODUCTS_IMPORTED:
      case PRODUCT_CREATED:
      case PRODUCT_UPDATED:
      case PRODUCT_DELETED:
        // Recharger les données automatiquement et de manière transparente
        onRefresh();
        break;
      case CATEGORY_CREATED:
      case CATEGORY_UPDATED:
      case CATEGORY_DELETED:
      case CATEGORIES_CHANGED:
        // Recharger l'arborescence des catégories
        if (categoriesTreeView != null) {
          loadCategoriesTreeData(categoriesTreeView);
        }
        break;
      case DATABASE_CLEANED:
        // Recharger toutes les données après nettoyage
        onRefresh();
        if (categoriesTreeView != null) {
          loadCategoriesTreeData(categoriesTreeView);
        }
        break;
      case INTERVENTIONS_CHANGED:
        // Recharger seulement l'historique pour le produit sélectionné
        if (currentProductId != null) {
          updateProductSelection(productTable.getSelectionModel().getSelectedItem());
        }
        break;
      default:
        // Ignorer les autres types d'événements
        break;
    }
  }

  private void updateProductSelection(ProductRepository.ProductRow product) {
    if (product == null) {
      currentProductId = null;
      updateProductDetail(null);
      return;
    }
    currentProductId = product.id();
    
    // Mettre à jour le volet de détail
    updateProductDetail(product);
    
    // Récupérer les détails complets du produit pour avoir accès à la photo
    var detailedProductOpt = productRepo.findDetailedById(product.id());
    
    // TODO: Update product details in new UI design
    // Old product details update - commented out for new design
    
    // Charger les images et catégories si les détails complets sont disponibles
    if (detailedProductOpt.isPresent()) {
      var detailedProduct = detailedProductOpt.get();
      
      // Afficher catégorie et sous-catégorie avec masquage des champs vides
      updateProductCategories(detailedProduct.category(), detailedProduct.subcategory());
      
      // Charger les images
      loadProductPhoto(detailedProduct.photo());
      loadManufacturerLogo(detailedProduct.fabricant());
      loadQrCode(product.uid());
    } else {
      // Si pas de détails, vider les champs catégorie
      updateProductCategories("", "");
    }

    // TODO: Load intervention history in new UI design
    // Old history table update - commented out for new design

    AppLogger.logUserAction("Produit sélectionné", product.nom(), "détails chargés");
  }

  private void applyProductFilter() {
    if (filteredProducts == null || productSearchField == null) return;
    
    String search = productSearchField.getText();
    if (search == null || search.trim().isEmpty()) {
      filteredProducts.setPredicate(p -> true);
    } else {
      String lower = search.toLowerCase();
      filteredProducts.setPredicate(p -> 
        (p.nom() != null && p.nom().toLowerCase().contains(lower)) ||
        (p.fabricant() != null && p.fabricant().toLowerCase().contains(lower))
      );
    }
  }

  @FXML
  private void onRefresh() {
    AppLogger.debug("Chargement des produits...");
    
  // Utiliser uniquement les produits visibles (hors Vendu/Déchet)
  List<ProductRepository.ProductRow> products = ProductServiceStatic.findAllVisibleProducts();
  AppLogger.debug("{} produits visibles chargés", products.size());

  filteredProducts = new FilteredList<>(FXCollections.observableArrayList(products), p -> true);
  SortedList<ProductRepository.ProductRow> sortedProducts = new SortedList<>(filteredProducts);
  sortedProducts.comparatorProperty().bind(productTable.comparatorProperty());
    productTable.setItems(sortedProducts);
    
    // Vider le panneau de droite jusqu'à ce qu'un produit soit sélectionné
    clearRightPanel();
  }

  private void clearRightPanel() {
    // TODO: Implement for new UI design
    // Old UI clearing methods - commented out for new design
  }

  private void loadProductPhoto(String photoFilename) {
    // TODO: Implement for new UI design
    // Old photo loading method - commented out for new design
  }

  private void loadManufacturerLogo(String manufacturerName) {
    // TODO: Implement for new UI design
    // Old logo loading method - commented out for new design
  }

  private void loadQrCode(String uid) {
    // TODO: Implement for new UI design
    // Old QR code loading method - commented out for new design
  }

  @FXML 
  private void onClearProductSearch() { 
    if (productSearchField != null) productSearchField.clear(); 
  }
  
  // === DYNAMIC COMPONENTS INITIALIZATION ===
  
  private void initializeDynamicComponents() {
    // Initialiser les labels de dashboard
    totalInterventionsLabel = new Label("42");
    totalInterventionsLabel.getStyleClass().add("dashboard-metric");
    
    totalDemandesLabel = new Label("18");
    totalDemandesLabel.getStyleClass().add("dashboard-metric");
    
    totalProduitsLabel = new Label("322");
    totalProduitsLabel.getStyleClass().add("dashboard-metric");
    
    // Initialiser la liste d'activité récente
    recentActivityList = new ListView<>();
    recentActivityList.getStyleClass().add("dark-table-view");
    
    // Initialiser la table des produits
    initializeProductTable();
  }
  
  private void initializeProductTable() {
    productTable = new TableView<>();
    productTable.getStyleClass().add("dark-table-view");
    
    // Créer les colonnes
    colProdNom = new TableColumn<>("Produit");
    colProdNom.setPrefWidth(200.0);
    colProdNom.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().nom()));
    
    colProdSN = new TableColumn<>("N° de série");
    colProdSN.setPrefWidth(120.0);
    colProdSN.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().sn()));
    
    colProdUID = new TableColumn<>("UID");
    colProdUID.setPrefWidth(80.0);
    colProdUID.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().uid()));
    
    colProdFabricant = new TableColumn<>("Fabricant");
    colProdFabricant.setPrefWidth(150.0);
    colProdFabricant.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().fabricant()));
    
    colProdSituation = new TableColumn<>("Situation");
    colProdSituation.setPrefWidth(120.0);
    colProdSituation.setCellValueFactory(cd -> new javafx.beans.property.ReadOnlyStringWrapper(cd.getValue().situation()));
    
    // Ajouter les colonnes à la table
    productTable.getColumns().addAll(Arrays.asList(colProdNom, colProdSN, colProdUID, colProdFabricant, colProdSituation));
    
    // Configuration de la table
    productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    
    // Gérer la sélection de produit
    productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      updateProductSelection(newSel);
    });
    
    // Style des lignes
    productTable.setRowFactory(tv -> {
      TableRow<ProductRepository.ProductRow> r = new TableRow<>();
      r.setOnMouseClicked(e -> {
        if (!r.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
          var product = r.getItem();
          NavigationService.openProductDetail(product.id());
        }
      });
      return r;
    });
    
    // Initialiser le champ de recherche
    productSearchField = new TextField();
    productSearchField.setPromptText("Rechercher un produit...");
    productSearchField.getStyleClass().add("dark-text-field");
    productSearchField.setPrefWidth(300);
    
    // Recherche de produits
    productSearchField.textProperty().addListener((obs, o, n) -> applyProductFilter());
  }
  
  // === THEME MANAGEMENT ===
  

  
  // === SECTION LOADING METHODS ===
  
  private void clearAndLoadTabs(Tab... tabs) {
    // Supprimer tous les onglets existants SAUF ceux créés dans le FXML
    mainTabPane.getTabs().clear();
    
    // Ajouter les nouveaux onglets
    for (Tab tab : tabs) {
      mainTabPane.getTabs().add(tab);
      AppLogger.info("Onglet ajouté: " + tab.getText());
    }
    
    AppLogger.info("TabPane contient maintenant " + mainTabPane.getTabs().size() + " onglets");
    
    // Sélectionner le premier onglet par défaut
    if (tabs.length > 0) {
      mainTabPane.getSelectionModel().select(tabs[0]);
      AppLogger.info("Onglet sélectionné: " + tabs[0].getText());
    }
  }
  
  private void loadDashboardSection() {
    try {
      // Créer l'onglet Dashboard
      Tab dashboardTab = createDashboardTab();
      clearAndLoadTabs(dashboardTab);
      updateDashboardStats();
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement du Dashboard: " + e.getMessage(), e);
    }
  }
  
  private void loadGestionSection() {
    try {
      // Créer les onglets de gestion
      Tab produitsTab = createProduitsTab();
      Tab clientsTab = createClientsTab();
      Tab societesTab = createSocietesTab();
      
      clearAndLoadTabs(produitsTab, clientsTab, societesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement de la Gestion: " + e.getMessage(), e);
    }
  }
  

  

  
  private void loadDemandesSection() {
    try {
      // Créer les onglets de demandes
      Tab demandesEquipementTab = createDemandesEquipementTab();
      Tab demandesPiecesTab = createDemandesPiecesTab();
      Tab demandesInterventionTab = createDemandesInterventionTab();
      
      clearAndLoadTabs(demandesEquipementTab, demandesPiecesTab, demandesInterventionTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Demandes: " + e.getMessage(), e);
    }
  }
  
  private void loadInterventionsSection() {
    try {
      Tab listeTab = new Tab("📋 Liste des interventions");
      listeTab.setClosable(false);
      listeTab.setContent(createInterventionsListContent());
      
      Tab nouvelleTab = new Tab("✚ Nouvelle intervention");
      nouvelleTab.setClosable(false);
      nouvelleTab.setContent(createNewInterventionContent());
      
      Tab enCoursTab = new Tab("⏳ En cours");
      enCoursTab.setClosable(false);
      enCoursTab.setContent(createInterventionsEnCoursContent());
      
      Tab termineesTab = new Tab("✅ Terminées");
      termineesTab.setClosable(false);
      termineesTab.setContent(createInterventionsTermineesContent());
      
      clearAndLoadTabs(listeTab, nouvelleTab, enCoursTab, termineesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Interventions: " + e.getMessage(), e);
    }
  }
  
  private void loadStockSection() {
    try {
      Tab stockTab = new Tab("📦 Vue d'ensemble");
      stockTab.setClosable(false);
      stockTab.setContent(createStockOverviewContent());
      
      Tab mouvementsTab = new Tab("📋 Mouvements");
      mouvementsTab.setClosable(false);
      mouvementsTab.setContent(createStockMouvementsContent());
      
      Tab alertesTab = new Tab("⚠️ Alertes stock");
      alertesTab.setClosable(false);
      alertesTab.setContent(createStockAlertesContent());
      
      Tab rapportsTab = new Tab("📊 Rapports");
      rapportsTab.setClosable(false);
      rapportsTab.setContent(createStockRapportsContent());
      
      clearAndLoadTabs(stockTab, mouvementsTab, alertesTab, rapportsTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement du Stock: " + e.getMessage(), e);
    }
  }
  
  private void loadVehiculesSection() {
    try {
      Tab listeTab = new Tab("🚗 Liste des véhicules");
      listeTab.setClosable(false);
      listeTab.setContent(createVehiculesListContent());
      
      Tab planningTab = new Tab("📅 Planning véhicules");
      planningTab.setClosable(false);
      planningTab.setContent(createVehiculesPlanningContent());
      
      clearAndLoadTabs(listeTab, planningTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Véhicules: " + e.getMessage(), e);
    }
  }
  
  private void loadStatistiquesSection() {
    try {
      Tab vueEnsembleTab = new Tab("📊 Vue d'ensemble");
      vueEnsembleTab.setClosable(false);
      vueEnsembleTab.setContent(createStatistiquesOverviewContent());
      
      Tab interventionsTab = new Tab("🔧 Interventions");
      interventionsTab.setClosable(false);
      interventionsTab.setContent(createStatistiquesInterventionsContent());
      
      Tab stockTab = new Tab("📦 Stock");
      stockTab.setClosable(false);
      stockTab.setContent(createStatistiquesStockContent());
      
      Tab financierTab = new Tab("💰 Financier");
      financierTab.setClosable(false);
      financierTab.setContent(createStatistiquesFinancierContent());
      
      clearAndLoadTabs(vueEnsembleTab, interventionsTab, stockTab, financierTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Statistiques: " + e.getMessage(), e);
    }
  }
  
  private void loadExportSection() {
    try {
      Tab exportTab = new Tab("📤 Export de données");
      exportTab.setClosable(false);
      exportTab.setContent(createExportContent());
      
      clearAndLoadTabs(exportTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement de l'Export: " + e.getMessage(), e);
    }
  }
  
  private void loadPreferencesSection() {
    try {
      // Créer les onglets de préférences
      Tab generalTab = createPreferencesGeneralTab();
      Tab systemTab = createPreferencesSystemTab();
      Tab maintenanceTab = createPreferencesMaintenanceTab();
      Tab scrapingTab = createPreferencesScrapingTab();
      Tab categoriesTab = createPreferencesCategoriesTab();
      Tab mediasTab = createPreferencesMediasTab();
      Tab dataTab = createPreferencesDataTab();
      
      clearAndLoadTabs(generalTab, systemTab, maintenanceTab, scrapingTab, categoriesTab, mediasTab, dataTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Préférences: " + e.getMessage(), e);
    }
  }
  
  private Tab createPreferencesGeneralTab() {
    Tab tab = new Tab("⚙️ Général");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("general"));
    return tab;
  }
  
  private Tab createPreferencesSystemTab() {
    Tab tab = new Tab("🖥️ Système");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("system"));
    return tab;
  }
  
  private Tab createPreferencesMaintenanceTab() {
    Tab tab = new Tab("🧹 Maintenance");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("maintenance"));
    return tab;
  }
  
  private Tab createPreferencesScrapingTab() {
    Tab tab = new Tab("🖼️ Scraping Images");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("scraping"));
    return tab;
  }
  
  private Tab createPreferencesCategoriesTab() {
    Tab tab = new Tab("📁 Catégories");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("categories"));
    return tab;
  }
  
  private Tab createPreferencesMediasTab() {
    Tab tab = new Tab("🖼️ Médias");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("medias"));
    return tab;
  }
  
  private Tab createPreferencesDataTab() {
    Tab tab = new Tab("🗂️ Données");
    tab.setClosable(false);
    tab.setContent(createPreferencesTabContent("data"));
    return tab;
  }
  
  private VBox createPreferencesTabContent(String tabType) {
    VBox content = new VBox();
    content.setSpacing(20);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    try {
      // Charger le fichier FXML complet des préférences
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/preferences.fxml"));
      loader.load();
      
      // Obtenir le contrôleur des préférences
      Object controller = loader.getController();
      if (controller instanceof com.magsav.gui.PreferencesController preferencesController) {
        // Extraire le contenu de l'onglet spécifique sans le TabPane
        TabPane internalTabPane = preferencesController.getPreferencesTabPane();
        if (internalTabPane != null && !internalTabPane.getTabs().isEmpty()) {
          int tabIndex = switch (tabType) {
            case "general" -> 0;
            case "system" -> 1;
            case "maintenance" -> 2;
            case "scraping" -> 3;
            case "categories" -> 4;
            case "medias" -> 5;
            case "data" -> 6;
            default -> 0;
          };
          
          if (tabIndex < internalTabPane.getTabs().size()) {
            Tab selectedTab = internalTabPane.getTabs().get(tabIndex);
            javafx.scene.Node tabContent = selectedTab.getContent();
            
            // Ajouter directement le contenu de l'onglet sans le TabPane
            if (tabContent != null) {
              content.getChildren().add(tabContent);
            }
          }
        }
        AppLogger.info("Préférences " + tabType + " chargées avec succès");
      }
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des préférences " + tabType + ": " + e.getMessage(), e);
      
      // Fallback en cas d'erreur
      Label errorLabel = new Label("Erreur lors du chargement des préférences " + tabType);
      errorLabel.getStyleClass().add("error-message");
      content.getChildren().add(errorLabel);
    }
    
    return content;
  }
  
  private void loadTechnicienUsersSection() {
    try {
      // Créer l'onglet Utilisateurs Techniciens
      Tab technicienUsersTab = createTechnicienUsersTab();
      
      clearAndLoadTabs(technicienUsersTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Utilisateurs Techniciens: " + e.getMessage(), e);
    }
  }
  
  // === TAB CREATION METHODS ===
  
  private Tab createDashboardTab() {
    Tab tab = new Tab("🏠 Dashboard");
    tab.setClosable(false);
    
    // Réutiliser le contenu existant du dashboard depuis le FXML
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
    
    VBox dashboardContent = createDashboardContent();
    scrollPane.setContent(dashboardContent);
    tab.setContent(scrollPane);
    
    return tab;
  }
  
  private Tab createProduitsTab() {
    Tab tab = new Tab("📦 Produits");
    tab.setClosable(false);
    
    // Réutiliser le contenu existant des produits depuis le FXML
    VBox productsContent = createProductsContent();
    tab.setContent(productsContent);
    
    return tab;
  }
  

  
  // Méthodes pour la gestion de l'arborescence des catégories
  
  private void loadCategoriesTreeData(TreeView<CategoryTreeItem> treeView) {
    try {
      // Racine invisible
      TreeItem<CategoryTreeItem> root = new TreeItem<>();
      
      // Récupérer toutes les catégories
      List<Category> allCategories = categoryRepo.findAllCategories();
      Map<Long, TreeItem<CategoryTreeItem>> categoryItems = new HashMap<>();
      Map<Long, List<Category>> childrenMap = new HashMap<>();
      
      // Organiser les catégories par parent
      List<Category> rootCategories = new ArrayList<>();
      for (Category category : allCategories) {
        if (category.parentId() == null) {
          rootCategories.add(category);
        } else {
          childrenMap.computeIfAbsent(category.parentId(), k -> new ArrayList<>()).add(category);
        }
      }
      
      // Créer les TreeItems pour toutes les catégories
      for (Category category : allCategories) {
        int nbProduits = productRepo.getProductCountByCategory(category.id());
        CategoryTreeItem treeItem = new CategoryTreeItem(
          category.id(),
          category.nom(),
          "",  // Description non disponible dans le record Category
          category.parentId(),
          nbProduits,
          ""   // Date de création non disponible dans le record Category
        );
        treeItem.setHasChildren(childrenMap.containsKey(category.id()));
        
        TreeItem<CategoryTreeItem> item = new TreeItem<>(treeItem);
        categoryItems.put(category.id(), item);
      }
      
      // Construire l'arborescence
      for (Category category : rootCategories) {
        TreeItem<CategoryTreeItem> item = categoryItems.get(category.id());
        root.getChildren().add(item);
        buildCategoryTree(item, childrenMap, categoryItems);
      }
      
      treeView.setRoot(root);
      
      // Déplier les catégories racines
      for (TreeItem<CategoryTreeItem> item : root.getChildren()) {
        item.setExpanded(true);
      }
      
    } catch (Exception e) {
      System.err.println("Erreur lors du chargement de l'arborescence des catégories: " + e.getMessage());
      e.printStackTrace();
    }
  }
  
  private void buildCategoryTree(TreeItem<CategoryTreeItem> parent, 
                                Map<Long, List<Category>> childrenMap,
                                Map<Long, TreeItem<CategoryTreeItem>> categoryItems) {
    Long parentId = parent.getValue().getId();
    List<Category> children = childrenMap.get(parentId);
    
    if (children != null) {
      for (Category child : children) {
        TreeItem<CategoryTreeItem> childItem = categoryItems.get(child.id());
        parent.getChildren().add(childItem);
        buildCategoryTree(childItem, childrenMap, categoryItems);
      }
    }
  }
  

  

  


  private Tab createClientsTab() {
    Tab tab = new Tab("👥 Clients");
    tab.setClosable(false);
    
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // Titre et statistiques
    HBox headerBox = new HBox();
    headerBox.setSpacing(20);
    headerBox.getStyleClass().add("header-box");
    
    // Zone de statistiques
    HBox statsBox = new HBox();
    statsBox.setSpacing(15);
    Label totalClientsLabel = new Label("Total: 0");
    totalClientsLabel.getStyleClass().add("stats-label");
    Label societesLabel = new Label("Sociétés: 0");
    societesLabel.getStyleClass().add("stats-label");
    Label particuliersLabel = new Label("Particuliers: 0");
    particuliersLabel.getStyleClass().add("stats-label");
    
    statsBox.getChildren().addAll(totalClientsLabel, societesLabel, particuliersLabel);
    
    headerBox.getChildren().addAll(new javafx.scene.layout.Region(), statsBox);
    HBox.setHgrow(headerBox.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().add(headerBox);
    
    // Barre de filtres et actions
    HBox controlsBox = new HBox();
    controlsBox.setSpacing(15);
    controlsBox.getStyleClass().add("controls-box");
    
    // Filtre par type
    Label filterLabel = new Label("Filtre:");
    filterLabel.getStyleClass().add("filter-label");
    
    ComboBox<String> typeFilter = new ComboBox<>();
    typeFilter.getItems().addAll("Tous", "Sociétés", "Particuliers");
    typeFilter.setValue("Tous");
    typeFilter.getStyleClass().add("filter-combo");
    
    // Barre de recherche
    TextField searchField = new TextField();
    searchField.setPromptText("Rechercher par nom...");
    searchField.getStyleClass().add("search-field");
    searchField.setPrefWidth(200);
    
    Button searchBtn = new Button("🔍");
    searchBtn.getStyleClass().addAll("button", "button-icon");
    
    // Spacer
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    // Boutons d'actions
    Button nouveauBtn = new Button("Nouveau client");
    nouveauBtn.getStyleClass().addAll("button", "button-primary");
    nouveauBtn.setOnAction(e -> openClientForm(null));
    
    Button modifierBtn = new Button("Modifier");
    modifierBtn.getStyleClass().addAll("button", "button-secondary");
    modifierBtn.setOnAction(e -> modifySelectedClient());
    modifierBtn.setDisable(true);
    
    Button supprimerBtn = new Button("Supprimer");
    supprimerBtn.getStyleClass().addAll("button", "button-danger");
    supprimerBtn.setOnAction(e -> deleteSelectedClient());
    supprimerBtn.setDisable(true);
    
    controlsBox.getChildren().addAll(filterLabel, typeFilter, searchField, searchBtn, spacer, nouveauBtn, modifierBtn, supprimerBtn);
    content.getChildren().add(controlsBox);
    
    // Table des clients avec colonne Type
    TableView<ClientRow> table = new TableView<>();
    table.getStyleClass().add("table-view");
    
    TableColumn<ClientRow, String> nomCol = new TableColumn<>("Nom");
    nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
    nomCol.setPrefWidth(200);
    
    TableColumn<ClientRow, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(cellData -> {
      ClientRow client = cellData.getValue();
      return new javafx.beans.property.SimpleStringProperty(client.getTypeDisplay());
    });
    typeCol.setPrefWidth(120);
    
    TableColumn<ClientRow, String> emailCol = new TableColumn<>("Email");
    emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
    emailCol.setPrefWidth(200);
    
    TableColumn<ClientRow, String> telephoneCol = new TableColumn<>("Téléphone");
    telephoneCol.setCellValueFactory(new PropertyValueFactory<>("telephone"));
    telephoneCol.setPrefWidth(150);
    
    TableColumn<ClientRow, String> villeCol = new TableColumn<>("Ville");
    villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
    villeCol.setPrefWidth(150);
    
    TableColumn<ClientRow, Integer> interventionsCol = new TableColumn<>("Nb Interventions");
    interventionsCol.setCellValueFactory(new PropertyValueFactory<>("nbInterventions"));
    interventionsCol.setPrefWidth(120);
    
    var clientColumns = table.getColumns();
    clientColumns.addAll(Arrays.asList(nomCol, typeCol, emailCol, telephoneCol, villeCol, interventionsCol));
    
    // Configurer le double-clic pour ouvrir les détails du client
    table.setRowFactory(tv -> {
      TableRow<ClientRow> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          ClientRow client = row.getItem();
          AppLogger.info("Double-clic sur client: " + client.getNom());
          NavigationService.openClientDetail(client.getId());
        }
      });
      return row;
    });
    
    // Charger les données avec filtres et statistiques
    loadClientsDataWithFilter(table, typeFilter.getValue(), searchField.getText(),
                             totalClientsLabel, societesLabel, particuliersLabel);
    
    // Écouteurs pour les filtres
    typeFilter.setOnAction(e -> loadClientsDataWithFilter(table, typeFilter.getValue(), searchField.getText(),
                                                         totalClientsLabel, societesLabel, particuliersLabel));
    
    searchField.textProperty().addListener((obs, oldText, newText) -> 
      loadClientsDataWithFilter(table, typeFilter.getValue(), newText,
                               totalClientsLabel, societesLabel, particuliersLabel));
    
    searchBtn.setOnAction(e -> loadClientsDataWithFilter(table, typeFilter.getValue(), searchField.getText(),
                                                        totalClientsLabel, societesLabel, particuliersLabel));
    
    // Créer le panneau de détails à droite
    VBox detailPanel = createClientDetailPanel();
    detailPanel.setVisible(false); // Masqué par défaut
    
    // Créer le SplitPane horizontal
    javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
    splitPane.getStyleClass().add("split-pane");
    splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    
    // Ajouter la table et le panneau de détails
    splitPane.getItems().addAll(table, detailPanel);
    splitPane.setDividerPositions(0.65); // 65% pour la table, 35% pour les détails
    
    // Modifier la gestion de la sélection pour afficher les détails
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      boolean hasSelection = newSel != null;
      modifierBtn.setDisable(!hasSelection);
      supprimerBtn.setDisable(!hasSelection);
      
      if (hasSelection) {
        updateClientDetailPanel(detailPanel, newSel);
        detailPanel.setVisible(true);
      } else {
        detailPanel.setVisible(false);
      }
    });
    
    VBox.setVgrow(splitPane, javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().add(splitPane);
    
    tab.setContent(content);
    return tab;
  }

  private VBox createClientDetailPanel() {
    VBox panel = new VBox();
    panel.setSpacing(10);
    panel.setPadding(new javafx.geometry.Insets(20));
    panel.getStyleClass().add("detail-panel");
    panel.setPrefWidth(300);
    panel.setMinWidth(250);
    
    // Titre du panneau
    Label titleLabel = new Label("Détails du client");
    titleLabel.getStyleClass().add("detail-panel-title");
    panel.getChildren().add(titleLabel);
    
    return panel;
  }
  
  private void updateClientDetailPanel(VBox panel, ClientRow client) {
    // Effacer le contenu existant sauf le titre
    panel.getChildren().removeIf(node -> !(node instanceof Label && 
      ((Label)node).getText().equals("Détails du client")));
    
    // Ajouter les informations du client
    panel.getChildren().addAll(
      new Label("Nom: " + client.getNom()),
      new Label("Type: " + client.getTypeDisplay()),
      new Label("Email: " + (client.getEmail() != null ? client.getEmail() : "N/A")),
      new Label("Téléphone: " + (client.getTelephone() != null ? client.getTelephone() : "N/A")),
      new Label("Ville: " + (client.getVille() != null ? client.getVille() : "N/A")),
      new Label("Nb Interventions: " + client.getNbInterventions())
    );
    
    // Espacement
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    panel.getChildren().add(spacer);
    
    // Boutons d'actions
    Button detailBtn = new Button("Voir détail complet");
    detailBtn.getStyleClass().addAll("button", "button-primary");
    detailBtn.setOnAction(e -> NavigationService.openClientDetail(client.getId()));
    
    Button editBtn = new Button("Modifier");
    editBtn.getStyleClass().addAll("button", "button-secondary");
    editBtn.setOnAction(e -> openClientForm(client));
    
    panel.getChildren().addAll(detailBtn, editBtn);
  }

  private VBox createCompanyDetailPanel() {
    VBox panel = new VBox();
    panel.setSpacing(10);
    panel.setPadding(new javafx.geometry.Insets(20));
    panel.getStyleClass().add("detail-panel");
    panel.setPrefWidth(300);
    panel.setMinWidth(250);
    
    // Titre du panneau
    Label titleLabel = new Label("Détails de la société");
    titleLabel.getStyleClass().add("detail-panel-title");
    panel.getChildren().add(titleLabel);
    
    return panel;
  }
  
  private void updateCompanyDetailPanel(VBox panel, CompanyRow company) {
    // Effacer le contenu existant sauf le titre
    panel.getChildren().removeIf(node -> !(node instanceof Label && 
      ((Label)node).getText().equals("Détails de la société")));
    
    // Ajouter les informations de la société
    panel.getChildren().addAll(
      new Label("Nom: " + company.getNom()),
      new Label("Type: " + company.getType()),
      new Label("Contact: " + (company.getContact() != null ? company.getContact() : "N/A")),
      new Label("Ville: " + (company.getVille() != null ? company.getVille() : "N/A")),
      new Label("Secteur: " + (company.getSecteur() != null ? company.getSecteur() : "N/A"))
    );
    
    // Espacement
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    panel.getChildren().add(spacer);
    
    // Boutons d'actions
    Button detailBtn = new Button("Voir détail complet");
    detailBtn.getStyleClass().addAll("button", "button-primary");
    detailBtn.setOnAction(e -> NavigationService.openCompanyDetail(company.getId()));
    
    Button editBtn = new Button("Modifier");
    editBtn.getStyleClass().addAll("button", "button-secondary");
    editBtn.setOnAction(e -> openCompanyForm(company));
    
    panel.getChildren().addAll(detailBtn, editBtn);
  }

  private VBox createVehiculeDetailPanel() {
    VBox panel = new VBox();
    panel.setSpacing(10);
    panel.setPadding(new javafx.geometry.Insets(20));
    panel.getStyleClass().add("detail-panel");
    panel.setPrefWidth(300);
    panel.setMinWidth(250);
    
    // Titre du panneau
    Label titleLabel = new Label("Détails du véhicule");
    titleLabel.getStyleClass().add("detail-panel-title");
    panel.getChildren().add(titleLabel);
    
    return panel;
  }
  
  private void updateVehiculeDetailPanel(VBox panel, com.magsav.model.Vehicule vehicule) {
    // Effacer le contenu existant sauf le titre
    panel.getChildren().removeIf(node -> !(node instanceof Label && 
      ((Label)node).getText().equals("Détails du véhicule")));
    
    // Ajouter les informations du véhicule
    panel.getChildren().addAll(
      new Label("Immatriculation: " + vehicule.getImmatriculation()),
      new Label("Type: " + vehicule.getTypeVehicule()),
      new Label("Marque: " + vehicule.getMarque()),
      new Label("Modèle: " + vehicule.getModele()),
      new Label("Statut: " + vehicule.getStatut()),
      new Label("Kilométrage: " + String.format("%,d km", vehicule.getKilometrage()))
    );
    
    // Espacement
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    panel.getChildren().add(spacer);
    
    // Boutons d'actions
    Button detailBtn = new Button("Voir détail complet");
    detailBtn.getStyleClass().addAll("button", "button-primary");
    detailBtn.setOnAction(e -> NavigationService.openVehiculeDetail(vehicule.getId()));
    
    Button maintenanceBtn = new Button("Planifier maintenance");
    maintenanceBtn.getStyleClass().addAll("button", "button-secondary");
    maintenanceBtn.setOnAction(e -> showAlert("Info", "Planification maintenance à implémenter"));
    
    panel.getChildren().addAll(detailBtn, maintenanceBtn);
  }

  private VBox createUserDetailPanel() {
    VBox panel = new VBox();
    panel.setSpacing(10);
    panel.setPadding(new javafx.geometry.Insets(20));
    panel.getStyleClass().add("detail-panel");
    panel.setPrefWidth(300);
    panel.setMinWidth(250);
    
    // Titre du panneau
    Label titleLabel = new Label("Détails de l'utilisateur");
    titleLabel.getStyleClass().add("detail-panel-title");
    panel.getChildren().add(titleLabel);
    
    return panel;
  }
  
  private void updateUserDetailPanel(VBox panel, UserRow user) {
    // Effacer le contenu existant sauf le titre
    panel.getChildren().removeIf(node -> !(node instanceof Label && 
      ((Label)node).getText().equals("Détails de l'utilisateur")));
    
    // Ajouter les informations de l'utilisateur
    panel.getChildren().addAll(
      new Label("Nom: " + user.getNom()),
      new Label("Prénom: " + user.getPrenom()),
      new Label("Email: " + user.getEmail()),
      new Label("Rôle: " + user.getRole()),
      new Label("Statut: " + user.getStatut())
    );
    
    // Espacement
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    panel.getChildren().add(spacer);
    
    // Boutons d'actions
    Button detailBtn = new Button("Voir détail complet");
    detailBtn.getStyleClass().addAll("button", "button-primary");
    detailBtn.setOnAction(e -> showAlert("Info", "Détail complet de l'utilisateur: " + user.getNom()));
    
    Button editBtn = new Button("Modifier");
    editBtn.getStyleClass().addAll("button", "button-secondary");
    editBtn.setOnAction(e -> showAlert("Info", "Modification de l'utilisateur: " + user.getNom()));
    
    panel.getChildren().addAll(detailBtn, editBtn);
  }
  
  private void loadUsersData(TableView<UserRow> table, Label totalLabel, Label activeLabel, Label inactiveLabel) {
    try {
      // Charger les utilisateurs depuis la base de données
      java.util.List<UserRow> users = userDataService.loadUsersFromDatabase();
      
      table.getItems().clear();
      table.getItems().addAll(users);
      
      // Mettre à jour les statistiques
      long total = users.size();
      long actifs = users.stream().filter(u -> "Actif".equals(u.getStatut())).count();
      long inactifs = total - actifs;
      
      totalLabel.setText("Total: " + total);
      activeLabel.setText("Actifs: " + actifs);
      inactiveLabel.setText("Inactifs: " + inactifs);
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des utilisateurs", e);
      showAlert("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
    }
  }
  
  private VBox createRequestDetailPanel() {
    VBox detailPanel = new VBox();
    detailPanel.setSpacing(15);
    detailPanel.getStyleClass().add("detail-panel");
    detailPanel.setPrefWidth(350);
    
    Label titleLabel = new Label("Détails de la demande");
    titleLabel.getStyleClass().add("detail-panel-title");
    
    Label selectLabel = new Label("Sélectionnez une demande pour voir les détails");
    selectLabel.getStyleClass().add("placeholder-text");
    
    detailPanel.getChildren().addAll(titleLabel, selectLabel);
    return detailPanel;
  }
  
  private void updateRequestDetailPanel(VBox detailPanel, RequestRow request) {
    detailPanel.getChildren().clear();
    
    Label titleLabel = new Label("Détails de la demande");
    titleLabel.getStyleClass().add("detail-panel-title");
    
    VBox content = new VBox();
    content.setSpacing(10);
    
    // Informations de base
    Label idLabel = new Label("ID: " + request.id());
    Label typeLabel = new Label("Type: " + request.type());
    Label statusLabel = new Label("Statut: " + request.status());
    Label urgenceLabel = new Label("Urgence: " + (request.urgence() != null ? request.urgence() : "Normale"));
    Label fournisseurLabel = new Label("Fournisseur: " + (request.fournisseurNom() != null ? request.fournisseurNom() : "Non spécifié"));
    Label dateLabel = new Label("Date de création: " + request.createdAt());
    
    // Description
    Label descriptionTitle = new Label("Description:");
    descriptionTitle.getStyleClass().add("detail-label");
    TextArea descriptionArea = new TextArea(request.description() != null ? request.description() : "Aucune description");
    descriptionArea.setEditable(false);
    descriptionArea.setPrefRowCount(3);
    descriptionArea.setWrapText(true);
    
    // Commentaire
    Label commentTitle = new Label("Commentaire:");
    commentTitle.getStyleClass().add("detail-label");
    TextArea commentArea = new TextArea(request.commentaire() != null ? request.commentaire() : "Aucun commentaire");
    commentArea.setEditable(false);
    commentArea.setPrefRowCount(2);
    commentArea.setWrapText(true);
    
    content.getChildren().addAll(
      idLabel, typeLabel, statusLabel, urgenceLabel, fournisseurLabel, dateLabel,
      descriptionTitle, descriptionArea,
      commentTitle, commentArea
    );
    
    detailPanel.getChildren().addAll(titleLabel, content);
  }
  
  private VBox createInterventionDetailPanel() {
    VBox detailPanel = new VBox();
    detailPanel.setSpacing(15);
    detailPanel.getStyleClass().add("detail-panel");
    detailPanel.setPrefWidth(350);
    
    Label titleLabel = new Label("Détails de l'intervention");
    titleLabel.getStyleClass().add("detail-panel-title");
    
    Label selectLabel = new Label("Sélectionnez une intervention pour voir les détails");
    selectLabel.getStyleClass().add("placeholder-text");
    
    detailPanel.getChildren().addAll(titleLabel, selectLabel);
    return detailPanel;
  }
  
  private void updateInterventionDetailPanel(VBox detailPanel, InterventionRow intervention) {
    detailPanel.getChildren().clear();
    
    Label titleLabel = new Label("Détails de l'intervention");
    titleLabel.getStyleClass().add("detail-panel-title");
    
    VBox content = new VBox();
    content.setSpacing(10);
    
    // Informations de base
    Label idLabel = new Label("ID: " + intervention.id());
    Label produitLabel = new Label("Produit: " + (intervention.produitNom() != null ? intervention.produitNom() : "Non spécifié"));
    Label statusLabel = new Label("Statut: " + (intervention.statut() != null ? intervention.statut() : "Non spécifié"));
    Label panneLabel = new Label("Type de panne: " + (intervention.panne() != null ? intervention.panne() : "Non spécifié"));
    Label dateEntreeLabel = new Label("Date d'entrée: " + (intervention.dateEntree() != null ? intervention.dateEntree() : "Non spécifiée"));
    Label dateSortieLabel = new Label("Date de sortie: " + (intervention.dateSortie() != null ? intervention.dateSortie() : "Non spécifiée"));
    
    // Informations détaillées
    Label detailsTitle = new Label("Informations détaillées:");
    detailsTitle.getStyleClass().add("detail-label");
    
    VBox detailsBox = new VBox();
    detailsBox.setSpacing(5);
    
    // Durée de l'intervention
    String duree = "En cours";
    if (intervention.dateEntree() != null && intervention.dateSortie() != null) {
      duree = "Intervention terminée";
    } else if (intervention.dateEntree() != null) {
      duree = "En cours depuis le " + intervention.dateEntree();
    }
    Label dureeLabel = new Label("Durée: " + duree);
    
    // État de l'intervention
    String etat = switch (intervention.statut() != null ? intervention.statut() : "") {
      case "EN_COURS" -> "🔧 Intervention en cours";
      case "TERMINEE" -> "✅ Intervention terminée";
      case "ANNULEE" -> "❌ Intervention annulée";
      case "PROGRAMMEE" -> "📅 Intervention programmée";
      default -> "❓ Statut inconnu";
    };
    Label etatLabel = new Label("État: " + etat);
    
    detailsBox.getChildren().addAll(dureeLabel, etatLabel);
    
    content.getChildren().addAll(
      idLabel, produitLabel, statusLabel, panneLabel, dateEntreeLabel, dateSortieLabel,
      detailsTitle, detailsBox
    );
    
    detailPanel.getChildren().addAll(titleLabel, content);
  }

  private Tab createSocietesTab() {
    Tab tab = new Tab("🏢 Sociétés");
    tab.setClosable(false);
    
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // Titre et statistiques
    HBox headerBox = new HBox();
    headerBox.setSpacing(20);
    headerBox.getStyleClass().add("header-box");
    
    // Zone de statistiques
    HBox statsBox = new HBox();
    statsBox.setSpacing(15);
    Label totalLabel = new Label("Total: 0");
    totalLabel.getStyleClass().add("stats-label");
    Label clientsLabel = new Label("Clients: 0");
    clientsLabel.getStyleClass().add("stats-label");
    Label fabricantsLabel = new Label("Fabricants: 0");
    fabricantsLabel.getStyleClass().add("stats-label");
    Label collaborateursLabel = new Label("Collaborateurs: 0");
    collaborateursLabel.getStyleClass().add("stats-label");
    Label particuliersLabel = new Label("Particuliers: 0");
    particuliersLabel.getStyleClass().add("stats-label");
    Label magSceneLabel = new Label("Mag Scène: 0");
    magSceneLabel.getStyleClass().add("stats-label");
    Label administrationLabel = new Label("Administration: 0");
    administrationLabel.getStyleClass().add("stats-label");
    
    statsBox.getChildren().addAll(totalLabel, clientsLabel, fabricantsLabel, collaborateursLabel, particuliersLabel, magSceneLabel, administrationLabel);
    
    headerBox.getChildren().addAll(new javafx.scene.layout.Region(), statsBox);
    HBox.setHgrow(headerBox.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().add(headerBox);
    
    // Barre de filtres et actions
    HBox controlsBox = new HBox();
    controlsBox.setSpacing(15);
    controlsBox.getStyleClass().add("controls-box");
    
    // Filtre par type
    Label filterLabel = new Label("Filtre:");
    filterLabel.getStyleClass().add("filter-label");
    
    ComboBox<String> typeFilter = new ComboBox<>();
    typeFilter.getItems().addAll("Tous", "Clients", "Fabricants", "Collaborateurs", "Particuliers", "Mag Scène", "Administration");
    typeFilter.setValue("Tous");
    typeFilter.getStyleClass().add("filter-combo");
    
    // Barre de recherche
    TextField searchField = new TextField();
    searchField.setPromptText("Rechercher par nom...");
    searchField.getStyleClass().add("search-field");
    searchField.setPrefWidth(200);
    
    Button searchBtn = new Button("🔍");
    searchBtn.getStyleClass().addAll("button", "button-icon");
    
    // Spacer
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    // Boutons d'actions
    Button nouveauBtn = new Button("Nouvelle société");
    nouveauBtn.getStyleClass().addAll("button", "button-primary");
    nouveauBtn.setOnAction(e -> openCompanyForm(null));
    
    Button modifierBtn = new Button("Modifier");
    modifierBtn.getStyleClass().addAll("button", "button-secondary");
    modifierBtn.setOnAction(e -> modifySelectedCompany());
    modifierBtn.setDisable(true);
    
    Button supprimerBtn = new Button("Supprimer");
    supprimerBtn.getStyleClass().addAll("button", "button-danger");
    supprimerBtn.setOnAction(e -> deleteSelectedCompany());
    supprimerBtn.setDisable(true);
    
    controlsBox.getChildren().addAll(filterLabel, typeFilter, searchField, searchBtn, spacer, nouveauBtn, modifierBtn, supprimerBtn);
    content.getChildren().add(controlsBox);
    
    // Table des sociétés avec colonnes améliorées
    TableView<CompanyRow> table = new TableView<>();
    table.getStyleClass().add("table-view");
    
    TableColumn<CompanyRow, String> nomCol = new TableColumn<>("Nom");
    nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
    nomCol.setPrefWidth(250);
    
    TableColumn<CompanyRow, String> typeCol = new TableColumn<>("Type");
    typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    typeCol.setPrefWidth(150); // Largeur augmentée pour l'icône
    // Utiliser la cellule personnalisée avec support d'icône GIF pour Mag Scène
    typeCol.setCellFactory(column -> com.magsav.util.CustomTableCellFactory.createCompanyTypeCell());
    
    TableColumn<CompanyRow, String> contactCol = new TableColumn<>("Contact");
    contactCol.setCellValueFactory(cellData -> {
      CompanyRow company = cellData.getValue();
      return new javafx.beans.property.SimpleStringProperty(company.getContact());
    });
    contactCol.setPrefWidth(200);
    
    TableColumn<CompanyRow, String> villeCol = new TableColumn<>("Ville");
    villeCol.setCellValueFactory(new PropertyValueFactory<>("ville"));
    villeCol.setPrefWidth(150);
    
    TableColumn<CompanyRow, String> secteurCol = new TableColumn<>("Secteur");
    secteurCol.setCellValueFactory(new PropertyValueFactory<>("secteur"));
    secteurCol.setPrefWidth(150);
    
    var companyColumns = table.getColumns();
    companyColumns.addAll(Arrays.asList(nomCol, typeCol, contactCol, villeCol, secteurCol));
    
    // Configurer le double-clic pour ouvrir les détails de la société
    table.setRowFactory(tv -> {
      TableRow<CompanyRow> row = new TableRow<CompanyRow>();
      
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          CompanyRow company = row.getItem();
          AppLogger.info("Double-clic sur société: " + company.getNom());
          NavigationService.openCompanyDetail(company.getId());
        }
      });
      return row;
    });
    
    // Charger les données et mettre à jour les statistiques
    loadCompaniesDataWithFilter(table, typeFilter.getValue(), searchField.getText(), 
                               totalLabel, clientsLabel, fabricantsLabel, collaborateursLabel, particuliersLabel, magSceneLabel, administrationLabel);
    
    // Écouteurs pour les filtres
    typeFilter.setOnAction(e -> loadCompaniesDataWithFilter(table, typeFilter.getValue(), searchField.getText(),
                                                           totalLabel, clientsLabel, fabricantsLabel, collaborateursLabel, particuliersLabel, magSceneLabel, administrationLabel));
    
    searchField.textProperty().addListener((obs, oldText, newText) -> 
      loadCompaniesDataWithFilter(table, typeFilter.getValue(), newText,
                                 totalLabel, clientsLabel, fabricantsLabel, collaborateursLabel, particuliersLabel, magSceneLabel, administrationLabel));
    
    searchBtn.setOnAction(e -> loadCompaniesDataWithFilter(table, typeFilter.getValue(), searchField.getText(),
                                                          totalLabel, clientsLabel, fabricantsLabel, collaborateursLabel, particuliersLabel, magSceneLabel, administrationLabel));
    
    // Créer le panneau de détails à droite
    VBox detailPanel = createCompanyDetailPanel();
    detailPanel.setVisible(false); // Masqué par défaut
    
    // Créer le SplitPane horizontal
    javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
    splitPane.getStyleClass().add("split-pane");
    splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    
    // Ajouter la table et le panneau de détails
    splitPane.getItems().addAll(table, detailPanel);
    splitPane.setDividerPositions(0.65); // 65% pour la table, 35% pour les détails
    
    // Modifier la gestion de la sélection pour afficher les détails
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      boolean hasSelection = newSel != null;
      modifierBtn.setDisable(!hasSelection);
      supprimerBtn.setDisable(!hasSelection);
      
      if (hasSelection) {
        updateCompanyDetailPanel(detailPanel, newSel);
        detailPanel.setVisible(true);
      } else {
        detailPanel.setVisible(false);
      }
    });
    
    VBox.setVgrow(splitPane, javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().add(splitPane);
    
    tab.setContent(content);
    return tab;
  }

  private Tab createDemandesEquipementTab() {
    Tab tab = new Tab("🔧 Équipement");
    tab.setClosable(false);
    
    // Créer le SplitPane principal
    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getStyleClass().add("split-pane");
    
    // Partie gauche - Liste des demandes
    VBox leftPane = new VBox();
    leftPane.setSpacing(0);
    leftPane.getStyleClass().add("main-content");
    
    // Créer la table des demandes d'équipement
    TableView<RequestRow> table = createRequestsTable("MATERIEL");
    loadRequestsData(table, "MATERIEL");
    
    leftPane.getChildren().add(table);
    
    // Partie droite - Panneau de détails
    VBox rightPane = createRequestDetailPanel();
    
    splitPane.getItems().addAll(leftPane, rightPane);
    splitPane.setDividerPositions(0.65);
    
    // Gestion de la sélection
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        updateRequestDetailPanel(rightPane, newSelection);
      }
    });
    
    tab.setContent(splitPane);
    return tab;
  }
  
  private Tab createDemandesPiecesTab() {
    Tab tab = new Tab("⚙️ Pièces");
    tab.setClosable(false);
    
    // Créer le SplitPane principal
    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getStyleClass().add("split-pane");
    
    // Partie gauche - Liste des demandes
    VBox leftPane = new VBox();
    leftPane.setSpacing(0);
    leftPane.getStyleClass().add("main-content");
    
    // Créer la table des demandes de pièces
    TableView<RequestRow> table = createRequestsTable("PIECES");
    loadRequestsData(table, "PIECES");
    
    leftPane.getChildren().add(table);
    
    // Partie droite - Panneau de détails
    VBox rightPane = createRequestDetailPanel();
    
    splitPane.getItems().addAll(leftPane, rightPane);
    splitPane.setDividerPositions(0.65);
    
    // Gestion de la sélection
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        updateRequestDetailPanel(rightPane, newSelection);
      }
    });
    
    tab.setContent(splitPane);
    return tab;
  }
  
  private Tab createDemandesInterventionTab() {
    Tab tab = new Tab("🔧 Intervention");
    tab.setClosable(false);
    
    // Créer le SplitPane principal
    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);
    splitPane.getStyleClass().add("split-pane");
    
    // Partie gauche - Liste des demandes
    VBox leftPane = new VBox();
    leftPane.setSpacing(0);
    leftPane.getStyleClass().add("main-content");
    
    // Créer la table des demandes d'intervention
    TableView<RequestRow> table = createRequestsTable("INTERVENTION");
    loadRequestsData(table, "INTERVENTION");
    
    leftPane.getChildren().add(table);
    
    // Partie droite - Panneau de détails
    VBox rightPane = createRequestDetailPanel();
    
    splitPane.getItems().addAll(leftPane, rightPane);
    splitPane.setDividerPositions(0.65);
    
    // Gestion de la sélection
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        updateRequestDetailPanel(rightPane, newSelection);
      }
    });
    
    tab.setContent(splitPane);
    return tab;
  }
  
  private TableView<RequestRow> createRequestsTable(String requestType) {
    TableView<RequestRow> table = new TableView<>();
    table.setPrefHeight(400);
    
    // Colonnes de la table
    TableColumn<RequestRow, Long> idColumn = new TableColumn<>("ID");
    idColumn.setCellValueFactory(cellData -> 
        new ReadOnlyObjectWrapper<>(cellData.getValue().id()));
    idColumn.setPrefWidth(60);
    
    TableColumn<RequestRow, String> typeColumn = new TableColumn<>("Type");
    typeColumn.setCellValueFactory(cellData -> 
        new ReadOnlyStringWrapper(cellData.getValue().type()));
    typeColumn.setPrefWidth(120);
    
    TableColumn<RequestRow, String> statusColumn = new TableColumn<>("Statut");
    statusColumn.setCellValueFactory(cellData -> 
        new ReadOnlyStringWrapper(cellData.getValue().status()));
    statusColumn.setPrefWidth(100);
    
    TableColumn<RequestRow, String> fournisseurColumn = new TableColumn<>("Fournisseur");
    fournisseurColumn.setCellValueFactory(cellData -> 
        new ReadOnlyStringWrapper(Optional.ofNullable(cellData.getValue().fournisseurNom())
            .orElse("Non spécifié")));
    fournisseurColumn.setPrefWidth(150);
    
    TableColumn<RequestRow, String> dateColumn = new TableColumn<>("Date de création");
    dateColumn.setCellValueFactory(cellData -> 
        new ReadOnlyStringWrapper(cellData.getValue().createdAt()));
    dateColumn.setPrefWidth(120);
    
    TableColumn<RequestRow, String> commentColumn = new TableColumn<>("Commentaire");
    commentColumn.setCellValueFactory(cellData -> 
        new ReadOnlyStringWrapper(Optional.ofNullable(cellData.getValue().commentaire())
            .orElse("")));
    commentColumn.setPrefWidth(200);
    
    table.getColumns().addAll(Arrays.asList(idColumn, typeColumn, statusColumn, fournisseurColumn, dateColumn, commentColumn));
    
    // Ajouter le double-clic pour ouvrir la fiche détaillée
    table.setRowFactory(tv -> {
      TableRow<RequestRow> row = new TableRow<RequestRow>();
      
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          RequestRow request = row.getItem();
          AppLogger.info("Double-clic sur demande ID: " + request.id());
          NavigationService.openRequestDetail(request.id());
        }
      });
      return row;
    });
    
    return table;
  }
  
  private void loadRequestsData(TableView<RequestRow> table, String type) {
    try {
      List<RequestRow> requests = requestDataService.loadRequestsFromDatabase(type);
      table.setItems(FXCollections.observableArrayList(requests));
      AppLogger.info("Chargé " + requests.size() + " demandes de type " + type + " depuis la base de données");
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des demandes " + type + ": " + e.getMessage(), e);
      table.setItems(FXCollections.observableArrayList());
    }
  }
  





  private Tab createTechnicienUsersTab() {
    Tab tab = new Tab("👤 Utilisateurs Techniciens");
    tab.setClosable(false);
    
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    // Titre et statistiques
    HBox headerBox = new HBox();
    headerBox.setSpacing(0);
    headerBox.getStyleClass().add("header-box");
    
    // Zone de statistiques
    HBox statsBox = new HBox();
    statsBox.setSpacing(15);
    Label totalUsersLabel = new Label("Total: 0");
    totalUsersLabel.getStyleClass().add("stats-label");
    Label activeUsersLabel = new Label("Actifs: 0");
    activeUsersLabel.getStyleClass().add("stats-label");
    Label inactiveUsersLabel = new Label("Inactifs: 0");
    inactiveUsersLabel.getStyleClass().add("stats-label");
    
    statsBox.getChildren().addAll(totalUsersLabel, activeUsersLabel, inactiveUsersLabel);
    
    headerBox.getChildren().addAll(new javafx.scene.layout.Region(), statsBox);
    HBox.setHgrow(headerBox.getChildren().get(0), javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().add(headerBox);
    
    // Barre de filtres et actions
    HBox controlsBox = new HBox();
    controlsBox.setSpacing(15);
    controlsBox.getStyleClass().add("controls-box");
    
    // Filtre par statut
    Label filterLabel = new Label("Filtre:");
    filterLabel.getStyleClass().add("filter-label");
    
    ComboBox<String> statusFilter = new ComboBox<>();
    statusFilter.getItems().addAll("Tous", "Actifs", "Inactifs");
    statusFilter.setValue("Tous");
    statusFilter.getStyleClass().add("filter-combo");
    
    // Barre de recherche
    TextField searchField = new TextField();
    searchField.setPromptText("Rechercher par nom...");
    searchField.getStyleClass().add("search-field");
    searchField.setPrefWidth(200);
    
    Button searchBtn = new Button("🔍");
    searchBtn.getStyleClass().addAll("button", "button-icon");
    
    // Spacer
    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    // Boutons d'actions
    Button nouveauBtn = new Button("Nouvel utilisateur");
    nouveauBtn.getStyleClass().addAll("button", "button-primary");
    nouveauBtn.setOnAction(e -> showAlert("Info", "Création d'utilisateur à implémenter"));
    
    Button modifierBtn = new Button("Modifier");
    modifierBtn.getStyleClass().addAll("button", "button-secondary");
    modifierBtn.setOnAction(e -> showAlert("Info", "Modification d'utilisateur à implémenter"));
    modifierBtn.setDisable(true);
    
    Button supprimerBtn = new Button("Supprimer");
    supprimerBtn.getStyleClass().addAll("button", "button-danger");
    supprimerBtn.setOnAction(e -> showAlert("Info", "Suppression d'utilisateur à implémenter"));
    supprimerBtn.setDisable(true);
    
    controlsBox.getChildren().addAll(filterLabel, statusFilter, searchField, searchBtn, spacer, nouveauBtn, modifierBtn, supprimerBtn);
    content.getChildren().add(controlsBox);
    
    // Table des utilisateurs
    TableView<UserRow> table = new TableView<>();
    table.getStyleClass().add("table-view");
    
    TableColumn<UserRow, String> nomCol = new TableColumn<>("Nom");
    nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
    nomCol.setPrefWidth(200);
    
    TableColumn<UserRow, String> prenomCol = new TableColumn<>("Prénom");
    prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
    prenomCol.setPrefWidth(150);
    
    TableColumn<UserRow, String> emailCol = new TableColumn<>("Email");
    emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
    emailCol.setPrefWidth(200);
    
    TableColumn<UserRow, String> roleCol = new TableColumn<>("Rôle");
    roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
    roleCol.setPrefWidth(120);
    
    TableColumn<UserRow, String> statutCol = new TableColumn<>("Statut");
    statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));
    statutCol.setPrefWidth(100);
    
    var userColumns = table.getColumns();
    userColumns.addAll(Arrays.asList(nomCol, prenomCol, emailCol, roleCol, statutCol));
    
    // Configurer le double-clic pour ouvrir les détails de l'utilisateur
    table.setRowFactory(tv -> {
      TableRow<UserRow> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          UserRow user = row.getItem();
          AppLogger.info("Double-clic sur utilisateur: " + user.getNom());
          NavigationService.openUserDetail(Math.toIntExact(user.getId()));
        }
      });
      return row;
    });
    
    // Créer le panneau de détails à droite
    VBox detailPanel = createUserDetailPanel();
    detailPanel.setVisible(false); // Masqué par défaut
    
    // Créer le SplitPane horizontal
    javafx.scene.control.SplitPane splitPane = new javafx.scene.control.SplitPane();
    splitPane.getStyleClass().add("split-pane");
    splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
    
    // Ajouter la table et le panneau de détails
    splitPane.getItems().addAll(table, detailPanel);
    splitPane.setDividerPositions(0.65); // 65% pour la table, 35% pour les détails
    
    // Gestion de la sélection pour afficher les détails
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      boolean hasSelection = newSel != null;
      modifierBtn.setDisable(!hasSelection);
      supprimerBtn.setDisable(!hasSelection);
      
      if (hasSelection) {
        updateUserDetailPanel(detailPanel, newSel);
        detailPanel.setVisible(true);
      } else {
        detailPanel.setVisible(false);
      }
    });
    
    // Charger les données des utilisateurs (simulées pour l'instant)
    loadUsersData(table, totalUsersLabel, activeUsersLabel, inactiveUsersLabel);
    
    VBox.setVgrow(splitPane, javafx.scene.layout.Priority.ALWAYS);
    content.getChildren().add(splitPane);
    
    tab.setContent(content);
    return tab;
  }
  
  // === CONTENT CREATION METHODS ===
  
  private VBox createDashboardContent() {
    VBox dashboardContent = new VBox();
    dashboardContent.setSpacing(20);
    dashboardContent.getStyleClass().add("dashboard");
    
    // Carte de bienvenue
    VBox welcomeCard = new VBox();
    welcomeCard.setSpacing(12);
    welcomeCard.getStyleClass().add("dashboard-card");
    
    Label welcomeTitle = new Label("Bienvenue dans MAGSAV");
    welcomeTitle.getStyleClass().add("dashboard-card-title");
    Label welcomeSubtitle = new Label("Tableau de bord principal - Gestion du SAV");
    welcomeSubtitle.getStyleClass().add("content-subtitle");
    
    welcomeCard.getChildren().addAll(welcomeTitle, welcomeSubtitle);
    
    // Métriques
    HBox metricsBox = new HBox();
    metricsBox.setSpacing(20);
    metricsBox.setAlignment(javafx.geometry.Pos.CENTER);
    
    // Intervention
    VBox interventionsMetric = createMetricCard("Interventions", totalInterventionsLabel, "Ce mois");
    VBox demandesMetric = createMetricCard("Demandes", totalDemandesLabel, "En attente");
    VBox produitsMetric = createMetricCard("Produits", totalProduitsLabel, "En stock");
    
    metricsBox.getChildren().addAll(interventionsMetric, demandesMetric, produitsMetric);
    
    // Activité récente
    VBox activityCard = new VBox();
    activityCard.setSpacing(12);
    activityCard.getStyleClass().add("dashboard-card");
    
    Label activityTitle = new Label("Activité récente");
    activityTitle.getStyleClass().add("dashboard-card-title");
    
    if (recentActivityList != null) {
      recentActivityList.setPrefHeight(200);
      recentActivityList.getStyleClass().add("dark-table-view");
      activityCard.getChildren().addAll(activityTitle, recentActivityList);
    } else {
      Label noActivity = new Label("Aucune activité récente");
      noActivity.getStyleClass().add("dashboard-metric-label");
      activityCard.getChildren().addAll(activityTitle, noActivity);
    }
    
    // Section Planning
    VBox planningCard = createPlanningCard();
    
    // Actions rapides
    VBox actionsCard = new VBox();
    actionsCard.setSpacing(12);
    actionsCard.getStyleClass().add("dashboard-card");
    
    Label actionsTitle = new Label("Actions rapides");
    actionsTitle.getStyleClass().add("dashboard-card-title");
    
    HBox buttonsBox = new HBox();
    buttonsBox.setSpacing(12);
    
    Button newInterventionBtn = new Button("+ Nouvelle intervention");
    newInterventionBtn.getStyleClass().add("dark-button-primary");
    newInterventionBtn.setOnAction(e -> onNewInterventionDashboard());
    
    Button newDemandeBtn = new Button("+ Nouvelle demande");
    newDemandeBtn.getStyleClass().add("dark-button-secondary");
    newDemandeBtn.setOnAction(e -> onNewDemande());
    
    Button rapportsBtn = new Button("📊 Voir les rapports");
    rapportsBtn.getStyleClass().add("dark-button-secondary");
    rapportsBtn.setOnAction(e -> onShowRapports());
    
    buttonsBox.getChildren().addAll(newInterventionBtn, newDemandeBtn, rapportsBtn);
    actionsCard.getChildren().addAll(actionsTitle, buttonsBox);
    
    dashboardContent.getChildren().addAll(welcomeCard, metricsBox, activityCard, planningCard, actionsCard);
    
    return dashboardContent;
  }
  
  private VBox createPlanningCard() {
    VBox planningCard = new VBox();
    planningCard.setSpacing(12);
    planningCard.getStyleClass().add("dashboard-card");
    
    Label planningTitle = new Label("📅 Planning de la semaine");
    planningTitle.getStyleClass().add("dashboard-card-title");
    
    // Container pour les planifications
    VBox planningsContainer = new VBox();
    planningsContainer.setSpacing(8);
    
    try {
      // Récupérer les planifications des 7 prochains jours
      com.magsav.repo.PlanificationRepositorySimple planRepo = new com.magsav.repo.PlanificationRepositorySimple();
      var upcomingPlans = planRepo.findUpcoming(7);
      
      if (upcomingPlans.isEmpty()) {
        Label noPlanLabel = new Label("Aucune planification cette semaine");
        noPlanLabel.getStyleClass().add("dashboard-metric-label");
        planningsContainer.getChildren().add(noPlanLabel);
      } else {
        // Afficher les premières planifications (max 5)
        int maxToShow = Math.min(5, upcomingPlans.size());
        for (int i = 0; i < maxToShow; i++) {
          var plan = upcomingPlans.get(i);
          
          HBox planItem = new HBox();
          planItem.setSpacing(10);
          planItem.getStyleClass().add("planning-item");
          
          Label dateLabel = new Label(plan.getDatePlanifiee());
          dateLabel.getStyleClass().add("planning-date");
          dateLabel.setPrefWidth(80);
          
          Label techLabel = new Label(plan.getTechnicienNom() != null ? plan.getTechnicienNom() : "Non assigné");
          techLabel.getStyleClass().add("planning-tech");
          techLabel.setPrefWidth(120);
          
          Label descLabel = new Label(plan.getNotesPlanification() != null ? plan.getNotesPlanification() : "Intervention");
          descLabel.getStyleClass().add("planning-desc");
          
          planItem.getChildren().addAll(dateLabel, techLabel, descLabel);
          planningsContainer.getChildren().add(planItem);
        }
        
        if (upcomingPlans.size() > 5) {
          Label moreLabel = new Label("... et " + (upcomingPlans.size() - 5) + " autres");
          moreLabel.getStyleClass().add("dashboard-metric-label");
          planningsContainer.getChildren().add(moreLabel);
        }
      }
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des planifications", e);
      Label errorLabel = new Label("Erreur lors du chargement du planning");
      errorLabel.getStyleClass().add("error-message");
      planningsContainer.getChildren().add(errorLabel);
    }
    
    planningCard.getChildren().addAll(planningTitle, planningsContainer);
    
    return planningCard;
  }
  
  private VBox createMetricCard(String title, Label valueLabel, String subtitle) {
    VBox metricCard = new VBox();
    metricCard.setSpacing(8);
    metricCard.setAlignment(javafx.geometry.Pos.CENTER);
    metricCard.getStyleClass().add("dashboard-card");
    metricCard.setPrefWidth(200);
    
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("dashboard-card-title");
    
    Label metricLabel = (valueLabel != null) ? valueLabel : new Label("0");
    metricLabel.getStyleClass().add("dashboard-metric");
    
    Label subtitleLabel = new Label(subtitle);
    subtitleLabel.getStyleClass().add("dashboard-metric-label");
    
    metricCard.getChildren().addAll(titleLabel, metricLabel, subtitleLabel);
    
    return metricCard;
  }
  
  private VBox createProductsContent() {
    VBox productsContent = new VBox();
    productsContent.setSpacing(16);
    productsContent.getStyleClass().add("main-content");
    
    // En-tête
    VBox headerBox = new VBox();
    headerBox.setSpacing(0);
    headerBox.getStyleClass().add("content-header");
    
    HBox searchBox = new HBox();
    searchBox.setSpacing(16);
    searchBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    Button addProductBtn = new Button("+ Nouveau produit");
    addProductBtn.getStyleClass().add("primary-button");
    addProductBtn.setOnAction(e -> openNewProductDialog());
    
    Button searchBtn = new Button("🔍");
    searchBtn.getStyleClass().add("dark-button-secondary");
    searchBtn.setOnAction(e -> onSearchProducts());
    
    searchBox.getChildren().addAll(spacer, addProductBtn, productSearchField, searchBtn);
    headerBox.getChildren().add(searchBox);
    
    // SplitPane avec table des produits et volet de détail
    SplitPane splitPane = new SplitPane();
    splitPane.getStyleClass().add("dark-split-pane");
    
    // Table des produits (côté gauche)
    VBox leftPanel = new VBox();
    leftPanel.setSpacing(8);
    VBox.setVgrow(productTable, javafx.scene.layout.Priority.ALWAYS);
    leftPanel.getChildren().add(productTable);
    
    // Volet de détail (côté droit)
    VBox rightPanel = createProductDetailPanel();
    
    splitPane.getItems().addAll(leftPanel, rightPanel);
    splitPane.setDividerPositions(0.6); // 60% pour la table, 40% pour le détail
    
    VBox.setVgrow(splitPane, javafx.scene.layout.Priority.ALWAYS);
    productsContent.getChildren().addAll(headerBox, splitPane);
    
    return productsContent;
  }



  @FXML private void onNewIntervention() { /* TODO: Implémenter création d'intervention */ }

  @FXML
  private void onOpenProductManagement() {
    NavigationService.openProductManagement(); 
  }

  @FXML private void onOpenCategories() {
    NavigationService.openCategories(); 
  }

  @FXML private void onOpenSuppliers() { 
    NavigationService.openSuppliers(); 
  }
  
  @FXML private void onOpenExternalSav() { 
    NavigationService.openExternalSav(); 
  }
  
  @FXML private void onOpenClients() { 
    NavigationService.openClients(); 
  }
  
  @FXML private void onOpenPartRequests() { 
    NavigationService.openRequestsParts(); 
  }
  
  @FXML private void onOpenEquipmentRequests() { 
    NavigationService.openRequestsEquipment(); 
  }

  @FXML private void onOpenImageMaintenance() { 
    NavigationService.openImageMaintenance(); 
  }

  @FXML private void onImageScrapingPreferences() {
    try {
      NavigationService.openImageScrapingPreferences();
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture des préférences de scraping", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", 
        "Impossible d'ouvrir les préférences. Erreur: " + e.getMessage());
    }
  }

  @FXML private void onOpenManagementHub() {
    try {
      AppLogger.info("main", "Ouverture de l'interface de gestion centralisée");
      NavigationService.openInNewWindow("/fxml/management_hub.fxml", "Interface de Gestion Centralisée");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de l'interface de gestion", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", 
        "Impossible d'ouvrir l'interface de gestion. Erreur: " + e.getMessage());
    }
  }

  @FXML private void onOpenPreferences() {
    try {
      AppLogger.info("main", "Ouverture des préférences centralisées");
      NavigationService.openInNewWindow("/fxml/preferences.fxml", "Préférences de l'Application");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture des préférences", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", 
        "Impossible d'ouvrir les préférences. Erreur: " + e.getMessage());
    }
  }

  @FXML private void onOpenRequestsHub() {
    try {
      AppLogger.info("main", "Ouverture du centre de gestion des demandes");
      NavigationService.openInNewWindow("/fxml/requests/hubs/requests_hub.fxml", "Centre de Gestion des Demandes");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture du centre des demandes", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", 
        "Impossible d'ouvrir le centre des demandes. Erreur: " + e.getMessage());
    }
  }

  @FXML
  private void onEditProduct() {
    if (currentProductId != null && currentProductId > 0) {
      NavigationService.openProductDetail(currentProductId);
    }
  }

  // ==================== MÉTHODES DE PARTAGE ====================

  @FXML
  private void onExportProduct() {
    if (currentProductId == null) return;
    
    var selectedProduct = productTable.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) return;
    
    String productName = selectedProduct.nom();
    
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Export en cours", "Export du produit: " + productName);
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.exportProduct(currentProductId).get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur d'export", 
            "Erreur lors de l'export: " + e.getMessage(), e));
        return null;
      }
    }).thenAccept(exportPath -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (exportPath != null) {
          ShareDialogs.showSuccessDialog("Export réussi", 
              "Produit exporté vers:\n" + exportPath.toString());
        }
      });
    });
  }

  @FXML
  private void onPrintProduct() {
    if (currentProductId == null) return;
    
    var selectedProduct = productTable.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) return;
    
    String productName = selectedProduct.nom();
    
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Impression en cours", "Préparation de l'impression: " + productName);
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.printProduct(currentProductId).get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur d'impression", 
            "Erreur lors de l'impression: " + e.getMessage(), e));
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Impression", 
              "Fichier ouvert pour impression");
        } else {
          ShareDialogs.showErrorDialog("Impression", "Échec de l'impression", null);
        }
      });
    });
  }

  @FXML
  private void onEmailProduct() {
    if (currentProductId == null) return;
    
    var selectedProduct = productTable.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) return;
    
    String productName = selectedProduct.nom();
    
    // Demander l'adresse email
    Optional<String> emailResult = ShareDialogs.showEmailInputDialog("");
    if (!emailResult.isPresent()) return;
    
    String email = emailResult.get();
    if (!ShareService.isValidEmail(email)) {
      ShareDialogs.showErrorDialog("Email invalide", 
          "L'adresse email fournie n'est pas valide: " + email, null);
      return;
    }
    
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Envoi par email", "Envoi du produit: " + productName);
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.emailProduct(currentProductId, email, productName).get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        if (e.getMessage().contains("Configuration email manquante")) {
          Platform.runLater(() -> {
            ShareDialogs.showErrorDialog("Configuration Email", 
                "Configuration email requise. Allez dans le menu pour configurer Gmail.", null);
          });
        } else {
          Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur email", 
              "Erreur lors de l'envoi: " + e.getMessage(), e));
        }
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Email envoyé", 
              "Produit envoyé avec succès à: " + email);
        } else {
          ShareDialogs.showErrorDialog("Email", "Échec de l'envoi", null);
        }
      });
    });
  }

  @FXML
  private void onShareProduct() {
    if (currentProductId == null) return;
    
    var selectedProduct = productTable.getSelectionModel().getSelectedItem();
    if (selectedProduct == null) return;
    
    String productName = selectedProduct.nom();
    
    // Demander l'adresse email
    Optional<String> emailResult = ShareDialogs.showEmailInputDialog("");
    if (!emailResult.isPresent()) return;
    
    String email = emailResult.get();
    if (!ShareService.isValidEmail(email)) {
      ShareDialogs.showErrorDialog("Email invalide", 
          "L'adresse email fournie n'est pas valide: " + email, null);
      return;
    }
    
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Partage complet", "Export + Email + Impression: " + productName);
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.shareProductComplete(currentProductId, productName, email).get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        if (e.getMessage().contains("Configuration email manquante")) {
          Platform.runLater(() -> {
            ShareDialogs.showErrorDialog("Configuration Email", 
                "Configuration email requise. Allez dans le menu pour configurer Gmail.", null);
          });
        } else {
          Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur partage", 
              "Erreur lors du partage: " + e.getMessage(), e));
        }
        return null;
      }
    }).thenAccept(result -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (result != null) {
          ShareDialogs.showShareResultDialog(result, productName);
        }
      });
    });
  }

  // ==================== MÉTHODES POUR LES MENUS DE PARTAGE ====================

  @FXML
  private void onExportStockReport() {
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Export du rapport de stock", "Génération du rapport de stock...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.exportStockReport().get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur export", 
            "Erreur lors de l'export: " + e.getMessage(), e));
        return null;
      }
    }).thenAccept(exportPath -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (exportPath != null) {
          ShareDialogs.showSuccessDialog("Export réussi", 
              "Rapport de stock exporté vers:\n" + exportPath.toString());
        }
      });
    });
  }

  @FXML
  private void onExportCompleteDatabase() {
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Export de la base complète", "Génération de l'export complet...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.exportCompleteDatabase().get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur export", 
            "Erreur lors de l'export: " + e.getMessage(), e));
        return null;
      }
    }).thenAccept(exportPath -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (exportPath != null) {
          ShareDialogs.showSuccessDialog("Export réussi", 
              "Base de données exportée vers:\n" + exportPath.toString());
        }
      });
    });
  }

  @FXML
  private void onPrintStockReport() {
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Impression du rapport de stock", "Préparation de l'impression...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.printStockReport().get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur impression", 
            "Erreur lors de l'impression: " + e.getMessage(), e));
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Impression", 
              "Rapport de stock ouvert pour impression");
        } else {
          ShareDialogs.showErrorDialog("Impression", "Échec de l'impression", null);
        }
      });
    });
  }

  @FXML
  private void onPrintCompleteDatabase() {
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Impression de la base complète", "Préparation de l'impression...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.printCompleteDatabase().get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur impression", 
            "Erreur lors de l'impression: " + e.getMessage(), e));
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Impression", 
              "Base de données ouverte pour impression");
        } else {
          ShareDialogs.showErrorDialog("Impression", "Échec de l'impression", null);
        }
      });
    });
  }

  @FXML
  private void onEmailStockReport() {
    Optional<String> emailResult = ShareDialogs.showEmailInputDialog("");
    if (!emailResult.isPresent()) return;
    
    String email = emailResult.get();
    if (!ShareService.isValidEmail(email)) {
      ShareDialogs.showErrorDialog("Email invalide", 
          "L'adresse email fournie n'est pas valide: " + email, null);
      return;
    }
    
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Envoi du rapport de stock", "Génération et envoi...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.emailStockReport(email).get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        if (e.getMessage().contains("Configuration email manquante")) {
          Platform.runLater(() -> {
            ShareDialogs.showErrorDialog("Configuration Email", 
                "Configuration email requise. Allez dans le menu pour configurer Gmail.", null);
          });
        } else {
          Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur email", 
              "Erreur lors de l'envoi: " + e.getMessage(), e));
        }
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Email envoyé", 
              "Rapport de stock envoyé avec succès à: " + email);
        } else {
          ShareDialogs.showErrorDialog("Email", "Échec de l'envoi", null);
        }
      });
    });
  }

  @FXML
  private void onEmailCompleteDatabase() {
    Optional<String> emailResult = ShareDialogs.showEmailInputDialog("");
    if (!emailResult.isPresent()) return;
    
    String email = emailResult.get();
    if (!ShareService.isValidEmail(email)) {
      ShareDialogs.showErrorDialog("Email invalide", 
          "L'adresse email fournie n'est pas valide: " + email, null);
      return;
    }
    
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Envoi de la base complète", "Génération et envoi...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.emailCompleteDatabase(email).get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        if (e.getMessage().contains("Configuration email manquante")) {
          Platform.runLater(() -> {
            ShareDialogs.showErrorDialog("Configuration Email", 
                "Configuration email requise. Allez dans le menu pour configurer Gmail.", null);
          });
        } else {
          Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur email", 
              "Erreur lors de l'envoi: " + e.getMessage(), e));
        }
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Email envoyé", 
              "Base de données envoyée avec succès à: " + email);
        } else {
          ShareDialogs.showErrorDialog("Email", "Échec de l'envoi", null);
        }
      });
    });
  }

  @FXML
  private void onOpenExportsFolder() {
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.openExportsFolder().get();
      } catch (Exception e) {
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur", 
            "Erreur lors de l'ouverture du dossier: " + e.getMessage(), e));
        return false;
      }
    }).thenAccept(success -> {
      if (!success) {
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur", 
            "Impossible d'ouvrir le dossier d'exports", null));
      }
    });
  }

  @FXML
  private void onConfigureEmail() {
    Optional<ShareDialogs.EmailConfig> configResult = ShareDialogs.showEmailConfigDialog();
    if (!configResult.isPresent()) return;
    
    ShareDialogs.EmailConfig config = configResult.get();
    
    // Configurer le service email
    shareService.setEmailConfiguration(config.email, config.password);
    
    // Test de la configuration
    ShareDialogs.ProgressDialog progressDialog = ShareDialogs.showProgressDialog(
        "Test de configuration", "Test de la connexion Gmail...");
    
    CompletableFuture.supplyAsync(() -> {
      try {
        return shareService.testEmailConfiguration().get();
      } catch (Exception e) {
        Platform.runLater(() -> progressDialog.close());
        Platform.runLater(() -> ShareDialogs.showErrorDialog("Erreur de configuration", 
            "Erreur lors du test: " + e.getMessage(), e));
        return false;
      }
    }).thenAccept(success -> {
      Platform.runLater(() -> {
        progressDialog.close();
        if (success) {
          ShareDialogs.showSuccessDialog("Configuration réussie", 
              "Configuration Gmail validée avec succès!");
        } else {
          ShareDialogs.showErrorDialog("Configuration échouée", 
              "Vérifiez vos identifiants et votre mot de passe d'application", null);
        }
      });
    });
  }

  @FXML
  private void onMediaMaintenance() {
    // TODO: Implémenter la maintenance des médias
    ShareDialogs.showSuccessDialog("Maintenance Médias", 
        "Fonctionnalité en cours de développement");
  }

  /**
   * Met à jour l'affichage des catégories dans le panneau de droite
   * Résout la hiérarchie des catégories et masque les titres et labels si vides
   */
  private void updateProductCategories(String category, String subcategory) {
    // TODO: Implement for new UI design
    // Old category display method - commented out for new design
  }
  
  /**
   * Méthode utilitaire pour afficher ou masquer une catégorie
   * TODO: Implement for new UI design
   */
  // private void updateCategoryDisplay(String categoryValue, Label titleLabel, Label valueLabel, String titleText) {
  //   // Old category display method - commented out for new design
  // }

  /**
   * Charge le logo de la société Mag Scène dans le menu principal
   */
  private void loadCompanyLogo() {
    try {
      SocieteRepository companyRepo = new SocieteRepository();
      // Rechercher la société Mag Scène parmi toutes les sociétés
      // (elle sera configurée via les paramètres d'administration)
      List<Societe> allSocietes = companyRepo.findAll();
      Societe magScene = allSocietes.stream()
          .filter(s -> "Mag Scène".equals(s.nom()))
          .findFirst()
          .orElse(null);
      
      if (magScene != null) {
        // Mettre à jour le nom de la société
        if (companyNameLabel != null) {
          companyNameLabel.setText(magScene.nom());
        }
        
        // Essayer de charger le logo GIF animé depuis les préférences
        if (companyLogoImage != null) {
          com.magsav.util.GifLogoManager.ensureLogoDirectoryExists();
          
          if (com.magsav.util.GifLogoManager.loadMagSceneAnimatedLogo(companyLogoImage)) {
            AppLogger.info("Logo GIF animé chargé pour la société: " + magScene.nom());
          } else {
            setDefaultCompanyIcon();
            AppLogger.info("Logo par défaut utilisé pour la société: " + magScene.nom());
          }
        }
      } else {
        AppLogger.info("Société Mag Scène non configurée - veuillez la définir dans les paramètres d'administration");
        // Utiliser le nom par défaut même si la société n'est pas trouvée
        if (companyNameLabel != null) {
          companyNameLabel.setText("Mag Scène");
        }
        setDefaultCompanyIcon();
      }
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement du logo de la société: " + e.getMessage(), e);
      setDefaultCompanyIcon();
    }
  }
  
  /**
   * Définit l'icône par défaut pour la société (icône générique)
   */
  private void setDefaultCompanyIcon() {
    if (companyLogoImage != null) {
      // Créer une image par défaut simple (icône générique entreprise)
      try {
        // Utiliser une icône par défaut du système ou créer un placeholder
        companyLogoImage.setImage(null);
        AppLogger.info("Logo par défaut appliqué pour la société");
      } catch (Exception e) {
        AppLogger.warn("Impossible de définir le logo par défaut: " + e.getMessage());
      }
    }
  }
  
  /**
   * Ouvre la fenêtre de test des zones de glisser-déposer
   */
  @FXML
  private void onTestDropZones() {
    try {
      AppLogger.info("Ouverture du test des zones de glisser-déposer");
      
      // Créer une nouvelle instance de l'application de test
      com.magsav.gui.test.DropZoneTestApp testApp = new com.magsav.gui.test.DropZoneTestApp();
      
      // Créer une nouvelle fenêtre
      javafx.stage.Stage testStage = new javafx.stage.Stage();
      testStage.initOwner(dashboardBtn.getScene().getWindow());
      testStage.setTitle("Test des Zones de Glisser-Déposer - MAGSAV");
      
      // Lancer l'application de test
      testApp.start(testStage);
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture du test: " + e.getMessage(), e);
      
      // Afficher un message d'erreur simple
      javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
      alert.setTitle("Erreur");
      alert.setHeaderText("Impossible d'ouvrir le test");
      alert.setContentText("Erreur: " + e.getMessage());
      alert.showAndWait();
    }
  }
  
  private void showAlert(String title, String message) {
    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
  
  // === MÉTHODES DE CONTENU POUR LES STATISTIQUES ===
  
  private VBox createStatistiquesOverviewContent() {
    VBox content = new VBox();
    content.setSpacing(20);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Statistiques générales");
    title.getStyleClass().add("content-title");
    
    // Métriques principales
    HBox metricsBox = new HBox();
    metricsBox.setSpacing(20);
    metricsBox.getStyleClass().add("metrics-container");
    
    VBox interventionsBox = createStockMetricBox("Total interventions", "156", "#4a90e2");
    VBox produitsBox = createStockMetricBox("Produits gérés", "322", "#51cf66");
    VBox ca = createStockMetricBox("CA mensuel", "€12,450", "#ffd43b");
    VBox satisfaction = createStockMetricBox("Satisfaction", "94%", "#ff6b6b");
    
    metricsBox.getChildren().addAll(interventionsBox, produitsBox, ca, satisfaction);
    
    // Graphiques placeholder
    VBox chartsBox = new VBox();
    chartsBox.setSpacing(16);
    
    VBox chart1 = createChartPlaceholder("Évolution du nombre d'interventions", "Graphique linéaire des 12 derniers mois");
    VBox chart2 = createChartPlaceholder("Répartition par type d'intervention", "Graphique en secteurs");
    
    chartsBox.getChildren().addAll(chart1, chart2);
    
    content.getChildren().addAll(title, metricsBox, chartsBox);
    
    return content;
  }
  
  private VBox createStatistiquesInterventionsContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Statistiques des interventions");
    title.getStyleClass().add("content-title");
    
    VBox chart1 = createChartPlaceholder("Temps de résolution moyen", "Évolution des délais par mois");
    VBox chart2 = createChartPlaceholder("Top 10 des pannes", "Analyse des problèmes les plus fréquents");
    VBox chart3 = createChartPlaceholder("Performance par technicien", "Comparaison des interventions résolues");
    
    content.getChildren().addAll(title, chart1, chart2, chart3);
    
    return content;
  }
  
  private VBox createStatistiquesStockContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Statistiques de stock");
    title.getStyleClass().add("content-title");
    
    VBox chart1 = createChartPlaceholder("Rotation des stocks", "Produits à rotation lente/rapide");
    VBox chart2 = createChartPlaceholder("Valorisation par catégorie", "Répartition de la valeur du stock");
    VBox chart3 = createChartPlaceholder("Évolution des sorties", "Tendances des mouvements de stock");
    
    content.getChildren().addAll(title, chart1, chart2, chart3);
    
    return content;
  }
  
  private VBox createStatistiquesFinancierContent() {
    VBox content = new VBox();
    content.setSpacing(16);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Statistiques financières");
    title.getStyleClass().add("content-title");
    
    // Métriques financières
    HBox metricsBox = new HBox();
    metricsBox.setSpacing(20);
    metricsBox.getStyleClass().add("metrics-container");
    
    VBox ca = createStockMetricBox("CA annuel", "€149,680", "#51cf66");
    VBox margeBox = createStockMetricBox("Marge moyenne", "34%", "#4a90e2");
    VBox impayesBox = createStockMetricBox("Impayés", "€2,180", "#ff6b6b");
    
    metricsBox.getChildren().addAll(ca, margeBox, impayesBox);
    
    VBox chart1 = createChartPlaceholder("Évolution du chiffre d'affaires", "CA mensuel des 12 derniers mois");
    VBox chart2 = createChartPlaceholder("Répartition par client", "Top clients par CA");
    
    content.getChildren().addAll(title, metricsBox, chart1, chart2);
    
    return content;
  }
  
  // === MÉTHODES DE CONTENU POUR L'EXPORT ===
  
  private VBox createExportContent() {
    VBox content = new VBox();
    content.setSpacing(20);
    content.getStyleClass().addAll("main-content", "tab-content-margins");
    
    Label title = new Label("Export de données");
    title.getStyleClass().add("content-title");
    
    // Options d'export
    VBox exportOptions = new VBox();
    exportOptions.setSpacing(16);
    
    VBox produits = createExportOption("📦 Export produits", "Exporter la liste complète des produits", "CSV, Excel, PDF");
    VBox interventions = createExportOption("🔧 Export interventions", "Exporter l'historique des interventions", "CSV, Excel, PDF");
    VBox stock = createExportOption("📊 Export stock", "Exporter les données de stock et mouvements", "CSV, Excel");
    VBox clients = createExportOption("👥 Export clients", "Exporter la base clients", "CSV, Excel, vCard");
    VBox statistiques = createExportOption("📈 Export statistiques", "Exporter les rapports statistiques", "PDF, Excel");
    
    exportOptions.getChildren().addAll(produits, interventions, stock, clients, statistiques);
    
    content.getChildren().addAll(title, exportOptions);
    
    return content;
  }
  
  // === MÉTHODES DE CONTENU POUR LES PRÉFÉRENCES ===
  

  
  // === MÉTHODES UTILITAIRES ===
  
  private VBox createChartPlaceholder(String title, String description) {
    VBox box = new VBox();
    box.setSpacing(12);
    box.getStyleClass().add("content-section");
    
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("section-title");
    
    VBox placeholder = new VBox();
    placeholder.setMinHeight(200);
    placeholder.setAlignment(javafx.geometry.Pos.CENTER);
    placeholder.getStyleClass().add("chart-placeholder");
    
    Label chartIcon = new Label("📊");
    chartIcon.setStyle("-fx-font-size: 48px;");
    
    Label descLabel = new Label(description);
    descLabel.getStyleClass().add("placeholder-subtitle");
    
    placeholder.getChildren().addAll(chartIcon, descLabel);
    box.getChildren().addAll(titleLabel, placeholder);
    
    return box;
  }
  
  private VBox createExportOption(String title, String description, String formats) {
    VBox box = new VBox();
    box.setSpacing(8);
    box.getStyleClass().add("rapport-option");
    
    HBox headerBox = new HBox();
    headerBox.setSpacing(12);
    headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
    
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("rapport-title");
    
    Region spacer = new Region();
    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
    
    Button exportBtn = new Button("Exporter");
    exportBtn.getStyleClass().add("primary-button");
    exportBtn.setOnAction(e -> showAlert("Info", "Export " + title + " à implémenter"));
    
    headerBox.getChildren().addAll(titleLabel, spacer, exportBtn);
    
    Label descLabel = new Label(description);
    descLabel.getStyleClass().add("rapport-description");
    
    Label formatsLabel = new Label("Formats: " + formats);
    formatsLabel.getStyleClass().add("placeholder-subtitle");
    
    box.getChildren().addAll(headerBox, descLabel, formatsLabel);
    
    return box;
  }
  
  // Méthodes pour la gestion des véhicules
  private void loadVehiculesData(TableView<com.magsav.model.Vehicule> table) {
    try {
      com.magsav.repo.VehiculeRepository repo = new com.magsav.repo.VehiculeRepository();
      var vehicules = repo.findAll();
      
      table.setItems(FXCollections.observableArrayList(vehicules));
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des véhicules", e);
      showAlert("Erreur", "Impossible de charger les véhicules: " + e.getMessage());
    }
  }





  // Méthodes pour la gestion des clients
  
  private void loadClientsDataWithFilter(TableView<ClientRow> table, String typeFilter, String searchText,
                                        Label totalLabel, Label societesLabel, Label particuliersLabel) {
    try {
      List<ClientRow> allClients = clientDataService.loadClientsFromDatabase();
      
      // Compteurs pour les statistiques
      int totalCount = 0, societesCount = 0, particuliersCount = 0;
      List<ClientRow> filteredRows = new ArrayList<>();
      
      // Appliquer les filtres et compter
      for (ClientRow client : allClients) {
        // Compter selon le type
        if (client.getType().equals("Société")) {
          societesCount++;
        } else {
          particuliersCount++;
        }
        totalCount++;
        
        // Appliquer les filtres
        boolean matchesTypeFilter = typeFilter.equals("Tous") || 
                                   (typeFilter.equals("Sociétés") && client.getType().equals("Société")) ||
                                   (typeFilter.equals("Particuliers") && client.getType().equals("Particulier"));
        
        boolean matchesSearchFilter = searchText == null || searchText.trim().isEmpty() ||
                                     client.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                                     client.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                                     client.getVille().toLowerCase().contains(searchText.toLowerCase());
        
        if (matchesTypeFilter && matchesSearchFilter) {
          filteredRows.add(client);
        }
      }
      
      // Mettre à jour les statistiques
      if (totalLabel != null) totalLabel.setText("Total: " + totalCount);
      if (societesLabel != null) societesLabel.setText("Sociétés: " + societesCount);
      if (particuliersLabel != null) particuliersLabel.setText("Particuliers: " + particuliersCount);
      
      table.setItems(FXCollections.observableArrayList(filteredRows));
      AppLogger.info("Chargé " + filteredRows.size() + " clients sur " + totalCount + " depuis la base de données");
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des clients", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", "Impossible de charger les clients: " + e.getMessage());
    }
  }
  
  private void openClientForm(ClientRow client) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/societes/client_form.fxml"));
      Parent root = loader.load();
      
      Stage dialog = new Stage();
      dialog.setTitle(client == null ? "Nouveau client" : "Modifier le client");
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(mainTabPane.getScene().getWindow());
      
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      dialog.setScene(scene);
      
      dialog.showAndWait();
      
      // Recharger les données
      refreshClientsTable();
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture du formulaire de client", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
    }
  }
  
  private void modifySelectedClient() {
    com.magsav.util.DialogUtils.showInfoAlert("Info", "Modification de client à implémenter");
  }
  
  private void deleteSelectedClient() {
    com.magsav.util.DialogUtils.showInfoAlert("Info", "Suppression de client à implémenter");
  }
  
  private void refreshClientsTable() {
    // Méthode pour recharger la table des clients
  }

  // Méthodes pour la gestion des sociétés
  
  private void loadCompaniesDataWithFilter(TableView<CompanyRow> table, String typeFilter, String searchText,
                                          Label totalLabel, Label clientsLabel, Label fabricantsLabel, 
                                          Label collaborateursLabel, Label particuliersLabel, Label magSceneLabel, Label administrationLabel) {
    try {
      List<CompanyRow> allCompanies = companyDataService.loadCompaniesFromDatabase();
      
      // Compteurs pour les statistiques
      int totalCount = 0, clientsCount = 0, fabricantsCount = 0, collaborateursCount = 0, particuliersCount = 0, magSceneCount = 0, administrationCount = 0;
      List<CompanyRow> filteredRows = new ArrayList<>();
      
      // Appliquer les filtres et compter
      for (CompanyRow company : allCompanies) {
        // Compter tous les types pour les statistiques
        switch (company.getType()) {
          case "Client" -> clientsCount++;
          case "Fabricant" -> fabricantsCount++;
          case "Collaborateur" -> collaborateursCount++;
          case "Particulier" -> particuliersCount++;
          case "Mag Scène" -> magSceneCount++;
          case "Administration" -> administrationCount++;
        }
        totalCount++;
        
        // Appliquer les filtres
        boolean matchesTypeFilter = typeFilter.equals("Tous") || 
                                   (typeFilter.equals("Clients") && "Client".equals(company.getType())) ||
                                   (typeFilter.equals("Fabricants") && "Fabricant".equals(company.getType())) ||
                                   (typeFilter.equals("Collaborateurs") && "Collaborateur".equals(company.getType())) ||
                                   (typeFilter.equals("Particuliers") && "Particulier".equals(company.getType())) ||
                                   (typeFilter.equals("Mag Scène") && "Mag Scène".equals(company.getType())) ||
                                   (typeFilter.equals("Administration") && "Administration".equals(company.getType()));
        
        boolean matchesSearchFilter = searchText == null || searchText.trim().isEmpty() ||
                                     company.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                                     company.getEmail().toLowerCase().contains(searchText.toLowerCase()) ||
                                     company.getVille().toLowerCase().contains(searchText.toLowerCase());
        
        if (matchesTypeFilter && matchesSearchFilter) {
          filteredRows.add(company);
        }
      }
      
      // Mettre à jour les statistiques
      if (totalLabel != null) totalLabel.setText("Total: " + totalCount);
      if (clientsLabel != null) clientsLabel.setText("Clients: " + clientsCount);
      if (fabricantsLabel != null) fabricantsLabel.setText("Fabricants: " + fabricantsCount);
      if (collaborateursLabel != null) collaborateursLabel.setText("Collaborateurs: " + collaborateursCount);
      if (particuliersLabel != null) particuliersLabel.setText("Particuliers: " + particuliersCount);
      if (magSceneLabel != null) magSceneLabel.setText("Mag Scène: " + magSceneCount);
      if (administrationLabel != null) administrationLabel.setText("Administration: " + administrationCount);
      
      table.setItems(FXCollections.observableArrayList(filteredRows));
      AppLogger.info("Chargé " + filteredRows.size() + " sociétés sur " + totalCount + " depuis la base de données");
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des sociétés", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", "Impossible de charger les sociétés: " + e.getMessage());
    }
  }
  
  private void openCompanyForm(CompanyRow company) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/societes/company_form.fxml"));
      Parent root = loader.load();
      
      Stage dialog = new Stage();
      dialog.setTitle(company == null ? "Nouvelle société" : "Modifier la société");
      dialog.initModality(Modality.APPLICATION_MODAL);
      dialog.initOwner(mainTabPane.getScene().getWindow());
      
      Scene scene = new Scene(root);
      scene.getStylesheets().add(getClass().getResource("/css/simple-dark.css").toExternalForm());
      dialog.setScene(scene);
      
      dialog.showAndWait();
      
      // Recharger les données
      refreshCompaniesTable();
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture du formulaire de société", e);
      com.magsav.util.DialogUtils.showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage());
    }
  }
  
  private void modifySelectedCompany() {
    com.magsav.util.DialogUtils.showInfoAlert("Info", "Modification de société à implémenter");
  }
  
  private void deleteSelectedCompany() {
    com.magsav.util.DialogUtils.showInfoAlert("Info", "Suppression de société à implémenter");
  }
  
  private void refreshCompaniesTable() {
    // Méthode pour recharger la table des sociétés
  }
}
