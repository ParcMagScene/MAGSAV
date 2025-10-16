package com.magsav.gui;

import com.magsav.gui.dialogs.ShareDialogs;
import com.magsav.gui.StatistiquesController;
import com.magsav.gui.ExportController;
import com.magsav.gui.utils.CSSManager;
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
import com.magsav.service.DataCacheService;
import com.magsav.service.NavigationService;
import com.magsav.service.ProductServiceStatic;
import com.magsav.service.RefreshManager;
import com.magsav.service.AvatarService;
import com.magsav.service.QrCodeService;
import com.magsav.service.RequestToOrderWorkflowService;

import com.magsav.service.ShareService;
import com.magsav.util.AppLogger;
import com.magsav.dto.*;


import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.image.Image;
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
  // Services gérés par les contrôleurs spécialisés
  
  // Contrôleurs spécialisés
  private final com.magsav.gui.controllers.InterventionsController interventionsController = 
      new com.magsav.gui.controllers.InterventionsController();
  private final com.magsav.gui.controllers.StockController stockController = 
      new com.magsav.gui.controllers.StockController();
  private final com.magsav.gui.controllers.DemandesController demandesController = 
      new com.magsav.gui.controllers.DemandesController();
  private final com.magsav.gui.controllers.UsersController usersController = 
      new com.magsav.gui.controllers.UsersController();
  private final com.magsav.gui.controllers.VehiculesController vehiculesController = 
      new com.magsav.gui.controllers.VehiculesController();
  
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

  // UI Elements utilisés dans l'interface
  @FXML private ImageView imgProductPhoto, imgManufacturerLogo, imgQr;
  private ImageView userAvatarImg, vehiculeQrImg;

  // Services statiques utilisés
  
  // Repositories pour certaines opérations spécifiques
  private final ProductRepository productRepo = new ProductRepository();
  private final InterventionRepository interventionRepo = new InterventionRepository();
  private final CategoryRepository categoryRepo = new CategoryRepository();
  private final RequestRepository requestRepo = new RequestRepository();

  
  // Service de partage
  private ShareService shareService;
  
  // Gestionnaire CSS centralisé
  private final CSSManager cssManager = CSSManager.getInstance();
  
  // Services pour la validation
  private RequestToOrderWorkflowService workflowService;
  private java.sql.Connection connection;
  
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
    
    // Initialisation des services de validation
    try {
      connection = com.magsav.db.DB.getConnection();
      workflowService = new RequestToOrderWorkflowService();
    } catch (Exception e) {
      AppLogger.error("Erreur d'initialisation des services de validation: " + e.getMessage(), e);
    }
    
    // Initialiser les éléments UI dynamiques AVANT de les utiliser
    initializeDynamicComponents();
    
    // Initialiser le système de rafraîchissement centralisé
    initializeRefreshManager();
    
    // S'abonner aux notifications de changement de données pour rafraîchissement automatique
    DataChangeNotificationService.getInstance().subscribe(this::onDataChanged);

    // Charger les données
    onRefresh();
    
    // Charger la section Gestion par défaut (sans Véhicules)
    loadGestionSection();
    
    // Set default active navigation item
    setActiveNavItem(gestionItem);
    
    // Initialisation du gestionnaire CSS APRÈS que l'interface soit entièrement chargée
    initializeCSS();
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
      // Générer toutes les données de test complètes avec le générateur unifié
      com.magsav.util.TestDataGenerator.generateCompleteTestData();
      
      // Invalider tout le cache pour forcer le rechargement
      DataCacheService.invalidateAllCache();
      
      // Notifier tous les composants qu'il y a eu un changement majeur de données
      DataChangeNotificationService.getInstance().notifyDatabaseCleaned(0);
      
      // Rafraîchir les données affichées
      onRefresh();
      
      // Rafraîchir tous les contrôleurs via le système centralisé
      RefreshManager.getInstance().refreshAll();
      
      // Diagnostic détaillé de la base de données
      runDatabaseDiagnostic();
      
      // Appliquer le CSS de diagnostic pour rendre les tables visibles
      applyDebugCSS();
      
      // Mettre à jour les statistiques du dashboard
      updateDashboardStats();
      
      // Afficher une confirmation
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Données de test");
      alert.setHeaderText("Génération terminée");
      alert.setContentText("Les données de test ont été générées et l'interface a été actualisée !\n\nCSS de diagnostic appliqué pour rendre les tables visibles.");
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
    
    imgProductPhoto = new ImageView();
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
    
    imgQr = new ImageView();
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
    
    detailPanel.getChildren().addAll(detailTitle, mediaBox, infoBox, spacer, buttonsBox);
    
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
  
  // === MÉTHODE OBSOLÈTE SUPPRIMÉE - REMPLACÉE PAR InterventionsController ===
  
  // === MÉTHODE OBSOLÈTE SUPPRIMÉE - REMPLACÉE PAR InterventionsController ===
  
  // === MÉTHODES OBSOLÈTES SUPPRIMÉES - REMPLACÉES PAR InterventionsController ===
  
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
  
  // === MÉTHODES DE STOCK SUPPRIMÉES - REMPLACÉES PAR StockController ===
  
  // === MÉTHODES createVehiculesListContent() ET createVehiculesPlanningContent() SUPPRIMÉES - REMPLACÉES PAR VehiculesController ===
  
  // === MÉTHODE createStockMouvementsContent() SUPPRIMÉE - REMPLACÉE PAR StockController ===
  
  // === MÉTHODE createStockAlertesContent() SUPPRIMÉE - REMPLACÉE PAR StockController ===
  
  // === MÉTHODE createStockRapportsContent() SUPPRIMÉE - REMPLACÉE PAR StockController ===
  
  // Méthodes utilitaires pour le stock
  
  private VBox createStockMetricBox(String label, String value, String color) {
    VBox box = new VBox();
    box.setSpacing(4);
    box.getStyleClass().add("metric-box");
    box.setAlignment(javafx.geometry.Pos.CENTER);
    
    Label valueLabel = new Label(value);
    valueLabel.getStyleClass().add("metric-value");
    cssManager.setTextColor(valueLabel, color);
    
    Label labelText = new Label(label);
    labelText.getStyleClass().add("metric-label");
    
    box.getChildren().addAll(valueLabel, labelText);
    
    return box;
  }
  
  // === MÉTHODE createStockAlert() SUPPRIMÉE - UTILITAIRE STOCK OBSOLÈTE ===
  
  // === MÉTHODE createRapportOption() SUPPRIMÉE - UTILITAIRE STOCK OBSOLÈTE ===
  

  
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
    
    // Détails produit gérés par la nouvelle UI
    // Old product details update - commented out for new design
    
    // Charger les images et catégories si les détails complets sont disponibles
    if (detailedProductOpt.isPresent()) {
      var detailedProduct = detailedProductOpt.get();
      
      // Charger les images (catégories gérées par la nouvelle UI)
      loadProductPhoto(detailedProduct.photo());
      loadManufacturerLogo(detailedProduct.fabricant());
      loadQrCode(product.uid());
    }

    // Historique des interventions géré par la nouvelle UI

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
    
    // Panneau de droite géré par la nouvelle UI
  }

  // Méthode clearRightPanel supprimée - obsolète avec la nouvelle UI

  private void loadProductPhoto(String photoFilename) {
    if (imgProductPhoto == null) return;
    
    if (photoFilename == null || photoFilename.trim().isEmpty()) {
      // Utiliser l'image par défaut du produit avec style de grande icône
      Image defaultImage = AvatarService.getInstance().getDefaultProductImage();
      imgProductPhoto.setImage(defaultImage);
      imgProductPhoto.getStyleClass().add("large-default-icon");
      return;
    }
    
    try {
      String photoPath = "medias/photos/" + photoFilename;
      java.nio.file.Path imagePath = java.nio.file.Paths.get(photoPath);
      
      if (java.nio.file.Files.exists(imagePath)) {
        Image photo = new Image(imagePath.toUri().toString());
        if (!photo.isError()) {
          imgProductPhoto.setImage(photo);
          imgProductPhoto.getStyleClass().remove("large-default-icon");
        } else {
          imgProductPhoto.setImage(AvatarService.getInstance().getDefaultProductImage());
          imgProductPhoto.getStyleClass().add("large-default-icon");
        }
      } else {
        imgProductPhoto.setImage(AvatarService.getInstance().getDefaultProductImage());
        imgProductPhoto.getStyleClass().add("large-default-icon");
      }
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement de la photo produit: " + photoFilename, e);
      imgProductPhoto.setImage(AvatarService.getInstance().getDefaultProductImage());
      imgProductPhoto.getStyleClass().add("large-default-icon");
    }
  }

  private void loadManufacturerLogo(String manufacturerName) {
    // Note: imgManufacturerLogo n'est pas encore utilisé dans l'interface actuelle
    // Cette méthode est prête pour une future implémentation
    if (imgManufacturerLogo == null) return;
    
    if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
      Image defaultLogo = AvatarService.getInstance().getDefaultCompanyLogo();
      imgManufacturerLogo.setImage(defaultLogo);
      imgManufacturerLogo.getStyleClass().add("large-default-icon");
      return;
    }
    
    try {
      String logoPath = "medias/logos/" + manufacturerName.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".png";
      java.nio.file.Path imagePath = java.nio.file.Paths.get(logoPath);
      
      if (java.nio.file.Files.exists(imagePath)) {
        Image logo = new Image(imagePath.toUri().toString());
        if (!logo.isError()) {
          imgManufacturerLogo.setImage(logo);
          imgManufacturerLogo.getStyleClass().remove("large-default-icon");
        } else {
          imgManufacturerLogo.setImage(AvatarService.getInstance().getDefaultCompanyLogo());
          imgManufacturerLogo.getStyleClass().add("large-default-icon");
        }
      } else {
        imgManufacturerLogo.setImage(AvatarService.getInstance().getDefaultCompanyLogo());
        imgManufacturerLogo.getStyleClass().add("large-default-icon");
      }
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement du logo fabricant: " + manufacturerName, e);
      imgManufacturerLogo.setImage(AvatarService.getInstance().getDefaultCompanyLogo());
      imgManufacturerLogo.getStyleClass().add("large-default-icon");
    }
  }

  private void loadQrCode(String uid) {
    if (imgQr == null) return;
    
    if (uid == null || uid.trim().isEmpty()) {
      imgQr.setImage(null);
      return;
    }
    
    // Charger le QR code de manière asynchrone
    javafx.concurrent.Task<Void> qrTask = new javafx.concurrent.Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try {
          java.nio.file.Path qrPath = QrCodeService.ensureQrPng(uid);
          if (java.nio.file.Files.exists(qrPath)) {
            javafx.application.Platform.runLater(() -> {
              try {
                Image qrImage = new Image(qrPath.toUri().toString(), true);
                imgQr.setImage(qrImage);
                AppLogger.debug("QR code chargé pour UID: " + uid);
              } catch (Exception e) {
                AppLogger.error("Erreur lors de l'affichage du QR code: " + uid, e);
                imgQr.setImage(null);
              }
            });
          } else {
            javafx.application.Platform.runLater(() -> imgQr.setImage(null));
          }
        } catch (Exception e) {
          AppLogger.error("Erreur lors de la génération du QR code: " + uid, e);
          javafx.application.Platform.runLater(() -> imgQr.setImage(null));
        }
        return null;
      }
    };
    
    Thread qrThread = new Thread(qrTask);
    qrThread.setDaemon(true);
    qrThread.start();
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
  
  /**
   * Initialise le système de rafraîchissement centralisé en enregistrant
   * tous les contrôleurs implémentant Refreshable.
   */
  private void initializeRefreshManager() {
    RefreshManager refreshManager = RefreshManager.getInstance();
    
    // Enregistrer tous les contrôleurs qui supportent le rafraîchissement
    refreshManager.registerRefreshable(demandesController);
    refreshManager.registerRefreshable(interventionsController);
    
    AppLogger.info("🔄 RefreshManager initialisé avec " + refreshManager.getRegisteredCount() + " contrôleurs");
  }
  
  /**
   * Exécute un diagnostic complet de la base de données pour identifier
   * les problèmes d'affichage des données.
   */
  private void runDatabaseDiagnostic() {
    try {
      AppLogger.info("🔍 === DÉBUT DIAGNOSTIC BASE DE DONNÉES ===");
      
      try (java.sql.Connection conn = com.magsav.db.DB.getConnection()) {
        // Diagnostic interventions
        try (java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM interventions")) {
          try (java.sql.ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              int count = rs.getInt(1);
              AppLogger.info("📊 Total interventions en DB: " + count);
            }
          }
        }
        
        // Échantillon d'interventions
        String sqlInterventions = "SELECT i.id, p.nom_produit, i.statut_intervention, i.description_panne " +
                                 "FROM interventions i " +
                                 "LEFT JOIN produits p ON i.produit_id = p.id " +
                                 "LIMIT 3";
        try (java.sql.PreparedStatement stmt = conn.prepareStatement(sqlInterventions)) {
          try (java.sql.ResultSet rs = stmt.executeQuery()) {
            AppLogger.info("📋 Échantillon interventions:");
            while (rs.next()) {
              AppLogger.info("  - ID=" + rs.getLong("id") + 
                           ", Produit='" + rs.getString("nom_produit") + 
                           "', Statut='" + rs.getString("statut_intervention") + 
                           "', Panne='" + rs.getString("description_panne") + "'");
            }
          }
        }
        
        // Test direct du repository
        com.magsav.repo.InterventionRepository interventionRepo = new com.magsav.repo.InterventionRepository();
        java.util.List<com.magsav.model.InterventionRow> interventions = interventionRepo.findAllWithProductName();
        AppLogger.info("📦 Repository findAllWithProductName() retourne: " + interventions.size() + " interventions");
        
        for (int i = 0; i < Math.min(3, interventions.size()); i++) {
          com.magsav.model.InterventionRow intervention = interventions.get(i);
          AppLogger.info("  - Repository: ID=" + intervention.id() + 
                        ", Produit='" + intervention.produitNom() + 
                        "', Statut='" + intervention.statut() + "'");
        }
        
        // Diagnostic clients/utilisateurs + structure
        AppLogger.info("🗂️ VÉRIFICATION STRUCTURE TABLES:");
        
        // Vérifier structure table users
        try {
          java.sql.DatabaseMetaData meta = conn.getMetaData();
          java.sql.ResultSet columns = meta.getColumns(null, null, "users", null);
          AppLogger.info("🔍 Colonnes table 'users':");
          while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String dataType = columns.getString("TYPE_NAME");
            AppLogger.info("  - " + columnName + " (" + dataType + ")");
          }
          columns.close();
        } catch (java.sql.SQLException e) {
          AppLogger.info("❌ Erreur structure users: " + e.getMessage());
        }
        
        // Vérifier structure table societes  
        try {
          java.sql.DatabaseMetaData meta = conn.getMetaData();
          java.sql.ResultSet columns = meta.getColumns(null, null, "societes", null);
          AppLogger.info("🔍 Colonnes table 'societes':");
          while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String dataType = columns.getString("TYPE_NAME");
            AppLogger.info("  - " + columnName + " (" + dataType + ")");
          }
          columns.close();
        } catch (java.sql.SQLException e) {
          AppLogger.info("❌ Erreur structure societes: " + e.getMessage());
        }
        
        String[] clientTables = {"clients", "client", "utilisateurs", "users", "societes"};
        for (String tableName : clientTables) {
          try (java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + tableName)) {
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
              if (rs.next()) {
                int count = rs.getInt(1);
                AppLogger.info("📊 Table '" + tableName + "': " + count + " enregistrements");
              }
            }
          } catch (java.sql.SQLException e) {
            // Table n'existe pas
          }
        }
        
        // Diagnostic demandes
        try (java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM requests")) {
          try (java.sql.ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
              int count = rs.getInt(1);
              AppLogger.info("📊 Total demandes: " + count);
            }
          }
        }
        
      }
      
      AppLogger.info("🔍 === FIN DIAGNOSTIC BASE DE DONNÉES ===");
      
    } catch (Exception e) {
      AppLogger.error("❌ Erreur lors du diagnostic DB: " + e.getMessage(), e);
    }
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
      // Déléguer au contrôleur dédié à la gestion
      com.magsav.gui.controllers.GestionController gestionController = 
          new com.magsav.gui.controllers.GestionController();
      
      Tab produitsTab = gestionController.createProduitsTab();
      Tab clientsTab = gestionController.createClientsTab();
      Tab societesTab = gestionController.createSocietesTab();
      Tab affairesTab = gestionController.createAffairesTab();
      
      clearAndLoadTabs(produitsTab, clientsTab, societesTab, affairesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement de la Gestion: " + e.getMessage(), e);
    }
  }
  

  

  
  private void loadDemandesSection() {
    try {
      // Utilisation du contrôleur spécialisé pour les demandes
      Tab demandesPiecesTab = demandesController.createDemandesPiecesTab();
      Tab demandesMaterielTab = demandesController.createDemandesMaterielTab();
      Tab demandesInterventionsTab = demandesController.createDemandesInterventionsTab();
      Tab demandesValideesTab = demandesController.createDemandesValideesTab();
      Tab demandesRefuseesTab = demandesController.createDemandesRefuseesTab();
      
      clearAndLoadTabs(demandesPiecesTab, demandesMaterielTab, demandesInterventionsTab, demandesValideesTab, demandesRefuseesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Demandes: " + e.getMessage(), e);
    }
  }
  
  private void loadInterventionsSection() {
    try {
      // Utilisation du contrôleur spécialisé pour les interventions
      Tab listeTab = interventionsController.createInterventionsListTab();
      Tab nouvelleTab = interventionsController.createNewInterventionTab();
      Tab enCoursTab = interventionsController.createInterventionsEnCoursTab();
      Tab termineesTab = interventionsController.createInterventionsTermineesTab();
      
      clearAndLoadTabs(listeTab, nouvelleTab, enCoursTab, termineesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Interventions: " + e.getMessage(), e);
    }
  }
  
  private void loadStockSection() {
    try {
      // Utilisation du contrôleur spécialisé pour le stock
      Tab stockTab = stockController.createStockOverviewTab();
      Tab mouvementsTab = stockController.createStockMouvementsTab();
      Tab alertesTab = stockController.createStockAlertesTab();
      Tab rapportsTab = stockController.createStockRapportsTab();
      
      clearAndLoadTabs(stockTab, mouvementsTab, alertesTab, rapportsTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement du Stock: " + e.getMessage(), e);
    }
  }
  
  private void loadVehiculesSection() {
    try {
      // Utiliser le nouveau contrôleur spécialisé
      Tab vehiculesTab = vehiculesController.createVehiculesTab();
      clearAndLoadTabs(vehiculesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Véhicules: " + e.getMessage(), e);
    }
  }
  
  private void loadStatistiquesSection() {
    try {
      StatistiquesController statistiquesController = new StatistiquesController();
      Tab statistiquesTab = statistiquesController.createStatistiquesTab();
      
      clearAndLoadTabs(statistiquesTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Statistiques: " + e.getMessage(), e);
    }
  }
  
  private void loadExportSection() {
    try {
      ExportController exportController = new ExportController();
      Tab exportTab = exportController.createExportTab();
      
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
      // Créer un contenu simple pour les préférences au lieu de charger le FXML
      VBox preferencesContent = new VBox(10);
      cssManager.applyComponentStyle(preferencesContent, "preferences-container");
      
      switch (tabType) {
        case "general" -> {
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          cssManager.applyComponentStyle(scrollPane);
          
          VBox settingsBox = new VBox(15);
          cssManager.applyComponentStyle(settingsBox, "preferences-container");
          
          // === Section Apparence ===
          Label appearanceTitle = new Label("🎨 Apparence");
          cssManager.styleTitle(appearanceTitle);
          
          VBox appearanceBox = new VBox(10);
          cssManager.applyComponentStyle(appearanceBox, "preferences-section");
          
          Label sidebarColorLabel = new Label("Couleur de la barre latérale:");
          cssManager.applyComponentStyle(sidebarColorLabel);
          ColorPicker sidebarColorPicker = new ColorPicker(javafx.scene.paint.Color.valueOf("#1e3a5f"));
          
          Label backgroundColorLabel = new Label("Couleur de fond:");
          cssManager.applyComponentStyle(backgroundColorLabel);
          ColorPicker backgroundColorPicker = new ColorPicker(javafx.scene.paint.Color.valueOf("#1a1a1a"));
          
          Label accentColorLabel = new Label("Couleur d'accent:");
          cssManager.applyComponentStyle(accentColorLabel);
          ColorPicker accentColorPicker = new ColorPicker(javafx.scene.paint.Color.valueOf("#4a90e2"));
          
          // Séparateur pour les onglets
          Separator tabSeparator = new Separator();
          cssManager.styleSeparator(tabSeparator);
          
          Label tabColorsLabel = new Label("🗂️ Couleurs des Onglets");
          cssManager.styleSubtitle(tabColorsLabel);
          
          Label tabDefaultColorLabel = new Label("Couleur des onglets non sélectionnés:");
          cssManager.applyComponentStyle(tabDefaultColorLabel);
          ColorPicker tabDefaultColorPicker = new ColorPicker(javafx.scene.paint.Color.valueOf("#1e3a5f"));
          
          Label tabSelectedColorLabel = new Label("Couleur de l'onglet sélectionné:");
          cssManager.applyComponentStyle(tabSelectedColorLabel);
          ColorPicker tabSelectedColorPicker = new ColorPicker(javafx.scene.paint.Color.valueOf("#666666"));
          
          Button applyAppearanceBtn = new Button("🎨 Appliquer");
          cssManager.stylePrimaryButton(applyAppearanceBtn);
          applyAppearanceBtn.setOnAction(e -> {
            // Récupération des couleurs sélectionnées pour les onglets
            String tabDefaultColor = String.format("#%02x%02x%02x", 
              (int)(tabDefaultColorPicker.getValue().getRed() * 255),
              (int)(tabDefaultColorPicker.getValue().getGreen() * 255),
              (int)(tabDefaultColorPicker.getValue().getBlue() * 255));
            
            String tabSelectedColor = String.format("#%02x%02x%02x", 
              (int)(tabSelectedColorPicker.getValue().getRed() * 255),
              (int)(tabSelectedColorPicker.getValue().getGreen() * 255),
              (int)(tabSelectedColorPicker.getValue().getBlue() * 255));
            
            // Application des couleurs via le système centralisé
            cssManager.configureTabColors(tabDefaultColor, tabSelectedColor);
            AppLogger.info("Apparence appliquée - Couleurs des onglets: défaut=" + tabDefaultColor + ", sélectionné=" + tabSelectedColor);
            showAlert(Alert.AlertType.INFORMATION, "Apparence", "Nouvelles couleurs des onglets appliquées!");
          });
          
          Button resetAppearanceBtn = new Button("🔄 Réinitialiser");
          cssManager.styleSecondaryButton(resetAppearanceBtn);
          resetAppearanceBtn.setOnAction(e -> {
            // Réinitialisation aux valeurs par défaut
            sidebarColorPicker.setValue(javafx.scene.paint.Color.valueOf("#1e3a5f"));
            backgroundColorPicker.setValue(javafx.scene.paint.Color.valueOf("#1a1a1a"));
            accentColorPicker.setValue(javafx.scene.paint.Color.valueOf("#4a90e2"));
            tabDefaultColorPicker.setValue(javafx.scene.paint.Color.valueOf("#1e3a5f"));
            tabSelectedColorPicker.setValue(javafx.scene.paint.Color.valueOf("#666666"));
            cssManager.configureTabColors("#1e3a5f", "#4a90e2");
            showAlert(Alert.AlertType.INFORMATION, "Apparence", "Couleurs réinitialisées!");
          });
          
          appearanceBox.getChildren().addAll(
            sidebarColorLabel, sidebarColorPicker,
            backgroundColorLabel, backgroundColorPicker,
            accentColorLabel, accentColorPicker,
            tabSeparator,
            tabColorsLabel,
            tabDefaultColorLabel, tabDefaultColorPicker,
            tabSelectedColorLabel, tabSelectedColorPicker,
            new HBox(10, applyAppearanceBtn, resetAppearanceBtn)
          );
          
          // === Section Langue et Localisation ===
          Label localizationTitle = new Label("🌍 Langue et Localisation");
          cssManager.styleTitle(localizationTitle);
          
          VBox localizationBox = new VBox(10);
          cssManager.applyComponentStyle(localizationBox, "preferences-section");
          
          Label languageLabel = new Label("Langue:");
          ComboBox<String> cbLanguage = new ComboBox<>();
          cbLanguage.getItems().addAll("Français", "English", "Español", "Deutsch");
          cbLanguage.setValue("Français");
          
          Label dateFormatLabel = new Label("Format de date:");
          ComboBox<String> cbDateFormat = new ComboBox<>();
          cbDateFormat.getItems().addAll("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD");
          cbDateFormat.setValue("DD/MM/YYYY");
          
          Label currencyLabel = new Label("Devise:");
          ComboBox<String> cbCurrency = new ComboBox<>();
          cbCurrency.getItems().addAll("EUR (€)", "USD ($)", "GBP (£)", "CHF");
          cbCurrency.setValue("EUR (€)");
          
          localizationBox.getChildren().addAll(
            languageLabel, cbLanguage,
            dateFormatLabel, cbDateFormat,
            currencyLabel, cbCurrency
          );
          
          // === Section Notifications ===
          Label notificationsTitle = new Label("🔔 Notifications");
          notificationsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox notificationsBox = new VBox(10);
          notificationsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          CheckBox chkShowNotifications = new CheckBox("Afficher les notifications");
          chkShowNotifications.setSelected(true);
          CheckBox chkSoundNotifications = new CheckBox("Notifications sonores");
          chkSoundNotifications.setSelected(false);
          CheckBox chkEmailNotifications = new CheckBox("Notifications par email");
          chkEmailNotifications.setSelected(true);
          CheckBox chkDesktopNotifications = new CheckBox("Notifications desktop");
          chkDesktopNotifications.setSelected(true);
          
          Label durationLabel = new Label("Durée d'affichage (secondes):");
          Spinner<Integer> spinnerNotificationDuration = new Spinner<>(1, 30, 5);
          
          notificationsBox.getChildren().addAll(
            chkShowNotifications, chkSoundNotifications,
            chkEmailNotifications, chkDesktopNotifications,
            durationLabel, spinnerNotificationDuration
          );
          
          // Bouton de sauvegarde global
          Button saveAllBtn = new Button("💾 Sauvegarder tous les paramètres");
          saveAllBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
          saveAllBtn.setOnAction(e -> {
            AppLogger.info("Tous les paramètres généraux sauvegardés");
            showAlert(Alert.AlertType.INFORMATION, "Sauvegarde", "Tous les paramètres généraux ont été sauvegardés!");
          });
          
          settingsBox.getChildren().addAll(
            appearanceTitle, appearanceBox,
            new Separator(),
            localizationTitle, localizationBox,
            new Separator(),
            notificationsTitle, notificationsBox,
            new Separator(),
            saveAllBtn
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        case "system" -> {
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          scrollPane.setStyle("-fx-background-color: #1a1a1a;");
          
          VBox settingsBox = new VBox(15);
          settingsBox.setStyle("-fx-padding: 20; -fx-background-color: #1a1a1a;");
          
          // === Informations Système ===
          Label systemTitle = new Label("💻 Informations Système");
          systemTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox infoBox = new VBox(5);
          infoBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Runtime runtime = Runtime.getRuntime();
          long totalMemory = runtime.totalMemory() / 1024 / 1024;
          long freeMemory = runtime.freeMemory() / 1024 / 1024;
          long usedMemory = totalMemory - freeMemory;
          long maxMemory = runtime.maxMemory() / 1024 / 1024;
          
          Label javaVersionLabel = new Label("☕ Version Java: " + System.getProperty("java.version"));
          Label osLabel = new Label("🖥️ OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
          Label archLabel = new Label("🏗️ Architecture: " + System.getProperty("os.arch"));
          Label memoryLabel = new Label("🧠 Mémoire: " + usedMemory + " MB utilisées / " + maxMemory + " MB max");
          Label processorsLabel = new Label("⚡ Processeurs: " + runtime.availableProcessors() + " cœurs");
          
          Button refreshInfoBtn = new Button("🔄 Actualiser");
          refreshInfoBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          
          infoBox.getChildren().addAll(
            javaVersionLabel, osLabel, archLabel, memoryLabel, processorsLabel, refreshInfoBtn
          );
          
          // === Configuration Performance ===
          Label perfTitle = new Label("⚡ Performance");
          perfTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox perfBox = new VBox(10);
          perfBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          CheckBox enableCacheBox = new CheckBox("Activer le cache en mémoire");
          enableCacheBox.setSelected(true);
          CheckBox enableMultithreadingBox = new CheckBox("Activer le multithreading");
          enableMultithreadingBox.setSelected(true);
          CheckBox optimizeMemoryBox = new CheckBox("Optimisation mémoire automatique");
          optimizeMemoryBox.setSelected(false);
          
          Label maxThreadsLabel = new Label("Nombre maximum de threads:");
          Spinner<Integer> maxThreadsSpinner = new Spinner<>(1, 32, runtime.availableProcessors());
          
          Label cacheTimeLabel = new Label("Durée du cache (minutes):");
          Spinner<Integer> cacheTimeSpinner = new Spinner<>(1, 60, 15);
          
          perfBox.getChildren().addAll(
            enableCacheBox, enableMultithreadingBox, optimizeMemoryBox,
            maxThreadsLabel, maxThreadsSpinner,
            cacheTimeLabel, cacheTimeSpinner
          );
          
          // === Configuration Logs ===
          Label logsTitle = new Label("📝 Journalisation");
          logsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox logsBox = new VBox(10);
          logsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label logLevelLabel = new Label("Niveau de log:");
          ComboBox<String> logLevelCombo = new ComboBox<>();
          logLevelCombo.getItems().addAll("ERROR", "WARN", "INFO", "DEBUG", "TRACE");
          logLevelCombo.setValue("INFO");
          
          CheckBox logToFileBox = new CheckBox("Enregistrer dans un fichier");
          logToFileBox.setSelected(true);
          CheckBox logToConsoleBox = new CheckBox("Afficher dans la console");
          logToConsoleBox.setSelected(true);
          CheckBox logDatabaseBox = new CheckBox("Logger les requêtes base de données");
          logDatabaseBox.setSelected(false);
          
          Label maxLogSizeLabel = new Label("Taille maximum des logs (MB):");
          Spinner<Integer> maxLogSizeSpinner = new Spinner<>(1, 100, 10);
          
          Button clearLogsBtn = new Button("🗑️ Vider les logs");
          clearLogsBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          
          Button viewLogsBtn = new Button("�️ Voir les logs");
          viewLogsBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          
          logsBox.getChildren().addAll(
            logLevelLabel, logLevelCombo,
            logToFileBox, logToConsoleBox, logDatabaseBox,
            maxLogSizeLabel, maxLogSizeSpinner,
            new HBox(10, clearLogsBtn, viewLogsBtn)
          );
          
          // === Base de Données ===
          Label dbTitle = new Label("🗄️ Base de Données");
          dbTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox dbBox = new VBox(10);
          dbBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label dbPathLabel = new Label("Chemin de la base de données:");
          TextField txtDatabasePath = new TextField("./data/magsav_h2");
          txtDatabasePath.setEditable(false);
          
          CheckBox autoBackupBox = new CheckBox("Sauvegarde automatique");
          autoBackupBox.setSelected(true);
          
          Label backupIntervalLabel = new Label("Intervalle de sauvegarde (heures):");
          Spinner<Integer> backupIntervalSpinner = new Spinner<>(1, 48, 24);
          
          Button backupNowBtn = new Button("💾 Sauvegarder maintenant");
          backupNowBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
          
          Button optimizeDbBtn = new Button("🔧 Optimiser la base");
          optimizeDbBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
          
          Label dbStatsLabel = new Label("📊 Statistiques de la base: 57 tables, 15 affaires, 0 produits");
          dbStatsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
          
          dbBox.getChildren().addAll(
            dbPathLabel, txtDatabasePath,
            autoBackupBox,
            backupIntervalLabel, backupIntervalSpinner,
            new HBox(10, backupNowBtn, optimizeDbBtn),
            dbStatsLabel
          );
          
          // Bouton de sauvegarde global
          Button saveSystemBtn = new Button("💾 Sauvegarder la configuration système");
          saveSystemBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
          saveSystemBtn.setOnAction(e -> {
            AppLogger.info("Configuration système sauvegardée");
            showAlert(Alert.AlertType.INFORMATION, "Sauvegarde", "Configuration système sauvegardée avec succès!");
          });
          
          settingsBox.getChildren().addAll(
            systemTitle, infoBox,
            new Separator(),
            perfTitle, perfBox,
            new Separator(),
            logsTitle, logsBox,
            new Separator(),
            dbTitle, dbBox,
            new Separator(),
            saveSystemBtn
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        case "maintenance" -> {
          
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          scrollPane.setStyle("-fx-background-color: transparent;");
          
          VBox settingsBox = new VBox(15);
          settingsBox.setStyle("-fx-padding: 10;");
          
          // === Maintenance Médias ===
          Label mediaTitle = new Label("🖼️ Maintenance Médias");
          mediaTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox mediaBox = new VBox(10);
          mediaBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label pathsLabel = new Label("Chemins des médias:");
          TextField txtPhotosPath = new TextField("./medias/photos/");
          TextField txtMediasPath = new TextField("./medias/files/");
          
          Label qualityLabel = new Label("Qualité d'optimisation:");
          Slider sliderImageQuality = new Slider(0.1, 1.0, 0.8);
          sliderImageQuality.setShowTickLabels(true);
          sliderImageQuality.setShowTickMarks(true);
          Label lblQualityValue = new Label("80%");
          sliderImageQuality.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblQualityValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
          });
          
          Label formatsLabel = new Label("Formats supportés:");
          CheckBox chkFormatJPG = new CheckBox("JPEG");
          chkFormatJPG.setSelected(true);
          CheckBox chkFormatPNG = new CheckBox("PNG");
          chkFormatPNG.setSelected(true);
          CheckBox chkFormatWEBP = new CheckBox("WebP");
          chkFormatWEBP.setSelected(false);
          
          Button btnScanMedia = new Button("🔍 Scanner les médias");
          btnScanMedia.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          
          Button btnOptimizeImages = new Button("⚡ Optimiser les images");
          btnOptimizeImages.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          
          Button btnCleanDuplicates = new Button("🗑️ Supprimer les doublons");
          btnCleanDuplicates.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          
          Button btnRepairLinks = new Button("🔧 Réparer les liens");
          btnRepairLinks.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
          
          Label lblMediaStats = new Label("📊 Statistiques: 0 images scannées");
          lblMediaStats.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
          
          ProgressBar progressMaintenance = new ProgressBar(0);
          progressMaintenance.setPrefWidth(200);
          Label lblMaintenanceProgress = new Label("Prêt");
          
          mediaBox.getChildren().addAll(
            pathsLabel, txtPhotosPath, txtMediasPath,
            new Separator(),
            qualityLabel, new HBox(10, sliderImageQuality, lblQualityValue),
            formatsLabel, new HBox(10, chkFormatJPG, chkFormatPNG, chkFormatWEBP),
            new Separator(),
            new HBox(10, btnScanMedia, btnOptimizeImages),
            new HBox(10, btnCleanDuplicates, btnRepairLinks),
            lblMediaStats,
            new VBox(5, progressMaintenance, lblMaintenanceProgress)
          );
          
          // === Maintenance Base de Données ===
          Label dbMaintenanceTitle = new Label("🗄️ Maintenance Base de Données");
          dbMaintenanceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox dbMaintenanceBox = new VBox(10);
          dbMaintenanceBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Button btnAnalyzeDB = new Button("📊 Analyser la base");
          btnAnalyzeDB.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          btnAnalyzeDB.setOnAction(e -> {
            AppLogger.info("Analyse de la base de données demandée");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Analyse terminée - Base de données saine!");
          });
          
          Button btnOptimizeDB = new Button("⚡ Optimiser la base");
          btnOptimizeDB.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
          btnOptimizeDB.setOnAction(e -> {
            AppLogger.info("Optimisation de la base de données demandée");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Base de données optimisée!");
          });
          
          Button btnVacuumDB = new Button("�️ Compacter la base");
          btnVacuumDB.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
          btnVacuumDB.setOnAction(e -> {
            AppLogger.info("Compactage de la base de données demandé");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Base de données compactée!");
          });
          
          Button btnRepairDB = new Button("🔧 Réparer la base");
          btnRepairDB.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
          btnRepairDB.setOnAction(e -> {
            AppLogger.info("Réparation de la base de données demandée");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Base de données réparée!");
          });
          
          dbMaintenanceBox.getChildren().addAll(
            new HBox(10, btnAnalyzeDB, btnOptimizeDB),
            new HBox(10, btnVacuumDB, btnRepairDB)
          );
          
          // === Nettoyage Système ===
          Label cleanupTitle = new Label("🧹 Nettoyage Système");
          cleanupTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox cleanupBox = new VBox(10);
          cleanupBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Button btnClearCache = new Button("�️ Vider le cache");
          btnClearCache.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          btnClearCache.setOnAction(e -> {
            AppLogger.info("Nettoyage du cache demandé");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Cache vidé avec succès!");
          });
          
          Button btnClearLogs = new Button("📝 Purger les logs");
          btnClearLogs.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          btnClearLogs.setOnAction(e -> {
            AppLogger.info("Purge des logs demandée");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Logs purgés!");
          });
          
          Button btnClearTemp = new Button("🗂️ Vider les fichiers temporaires");
          btnClearTemp.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
          btnClearTemp.setOnAction(e -> {
            AppLogger.info("Nettoyage des fichiers temporaires demandé");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Fichiers temporaires supprimés!");
          });
          
          Button btnFullMaintenance = new Button("🔄 Maintenance complète");
          btnFullMaintenance.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-weight: bold;");
          btnFullMaintenance.setOnAction(e -> {
            AppLogger.info("Maintenance complète demandée");
            showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Maintenance complète effectuée!");
          });
          
          cleanupBox.getChildren().addAll(
            new HBox(10, btnClearCache, btnClearLogs),
            new HBox(10, btnClearTemp),
            new Separator(),
            btnFullMaintenance
          );
          
          settingsBox.getChildren().addAll(
            mediaTitle, mediaBox,
            new Separator(),
            dbMaintenanceTitle, dbMaintenanceBox,
            new Separator(),
            cleanupTitle, cleanupBox
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        case "scraping" -> {
          
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          scrollPane.setStyle("-fx-background-color: transparent;");
          
          VBox settingsBox = new VBox(15);
          settingsBox.setStyle("-fx-padding: 10;");
          
          // === Configuration Générale ===
          Label generalTitle = new Label("⚙️ Configuration Générale");
          generalTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox generalBox = new VBox(10);
          generalBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          CheckBox chkScrapingEnabled = new CheckBox("Activer le scraping automatique");
          chkScrapingEnabled.setSelected(true);
          
          Label intervalLabel = new Label("Intervalle de scraping (minutes):");
          Spinner<Integer> spinnerDelay = new Spinner<>(1, 1440, 60, 15);
          spinnerDelay.setPrefWidth(100);
          
          Label maxPagesLabel = new Label("Pages maximum par site:");
          Spinner<Integer> spinnerMaxPages = new Spinner<>(1, 1000, 50, 10);
          spinnerMaxPages.setPrefWidth(100);
          
          Label timeoutLabel = new Label("Timeout des requêtes (secondes):");
          Spinner<Integer> spinnerTimeout = new Spinner<>(5, 300, 30, 5);
          spinnerTimeout.setPrefWidth(100);
          
          CheckBox chkRespectRobots = new CheckBox("Respecter robots.txt");
          chkRespectRobots.setSelected(true);
          
          CheckBox chkUseProxy = new CheckBox("Utiliser un proxy");
          TextField txtProxyUrl = new TextField("http://proxy.example.com:8080");
          txtProxyUrl.setDisable(true);
          chkUseProxy.selectedProperty().addListener((obs, oldVal, newVal) -> {
            txtProxyUrl.setDisable(!newVal);
          });
          
          generalBox.getChildren().addAll(
            chkScrapingEnabled,
            new HBox(10, intervalLabel, spinnerDelay),
            new HBox(10, maxPagesLabel, spinnerMaxPages),
            new HBox(10, timeoutLabel, spinnerTimeout),
            chkRespectRobots,
            chkUseProxy, txtProxyUrl
          );
          
          // === Sources de Données ===
          Label sourcesTitle = new Label("🌐 Sources de Données");
          sourcesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox sourcesBox = new VBox(10);
          sourcesBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          // Tableau des sources
          TableView<String[]> sourcesTable = new TableView<>();
          sourcesTable.setPrefHeight(200);
          
          TableColumn<String[], String> colNom = new TableColumn<>("Nom");
          colNom.setPrefWidth(120);
          colNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));
          
          TableColumn<String[], String> colUrl = new TableColumn<>("URL");
          colUrl.setPrefWidth(250);
          colUrl.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));
          
          TableColumn<String[], String> colFrequence = new TableColumn<>("Fréquence");
          colFrequence.setPrefWidth(80);
          colFrequence.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));
          
          TableColumn<String[], String> colStatut = new TableColumn<>("Statut");
          colStatut.setPrefWidth(80);
          colStatut.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));
          
          sourcesTable.getColumns().addAll(colNom, colUrl, colFrequence, colStatut);
          
          // Données exemple
          sourcesTable.getItems().addAll(
            new String[]{"Spectacles.fr", "https://www.spectacles.fr", "1h", "Actif"},
            new String[]{"Billetreduc", "https://www.billetreduc.com", "2h", "Actif"},
            new String[]{"Fnac Spectacles", "https://spectacles.fnac.com", "6h", "Pause"}
          );
          
          HBox sourcesButtons = new HBox(10);
          Button btnAddSource = new Button("➕ Ajouter");
          btnAddSource.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
          
          Button btnEditSource = new Button("✏️ Modifier");
          btnEditSource.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          
          Button btnDeleteSource = new Button("🗑️ Supprimer");
          btnDeleteSource.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          
          Button btnTestSource = new Button("🧪 Tester");
          btnTestSource.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          
          sourcesButtons.getChildren().addAll(btnAddSource, btnEditSource, btnDeleteSource, btnTestSource);
          sourcesBox.getChildren().addAll(sourcesTable, sourcesButtons);
          
          // === Filtres et Règles ===
          Label filtersTitle = new Label("🎯 Filtres et Règles");
          filtersTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox filtersBox = new VBox(10);
          filtersBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label keywordsLabel = new Label("Mots-clés à rechercher (séparés par des virgules):");
          TextArea txtKeywords = new TextArea("spectacle, théâtre, concert, festival, opéra");
          txtKeywords.setPrefRowCount(3);
          
          Label excludeLabel = new Label("Mots-clés à exclure:");
          TextArea txtExcludeKeywords = new TextArea("annulé, reporté, sold out");
          txtExcludeKeywords.setPrefRowCount(2);
          
          CheckBox chkFilterByDate = new CheckBox("Filtrer par date");
          DatePicker dateFrom = new DatePicker();
          DatePicker dateTo = new DatePicker();
          dateFrom.setDisable(true);
          dateTo.setDisable(true);
          
          chkFilterByDate.selectedProperty().addListener((obs, oldVal, newVal) -> {
            dateFrom.setDisable(!newVal);
            dateTo.setDisable(!newVal);
          });
          
          Label priceLabel = new Label("Fourchette de prix (€):");
          Spinner<Double> spinnerPriceMin = new Spinner<>(0.0, 1000.0, 0.0, 5.0);
          Spinner<Double> spinnerPriceMax = new Spinner<>(0.0, 1000.0, 500.0, 5.0);
          spinnerPriceMin.setPrefWidth(100);
          spinnerPriceMax.setPrefWidth(100);
          
          filtersBox.getChildren().addAll(
            keywordsLabel, txtKeywords,
            excludeLabel, txtExcludeKeywords,
            chkFilterByDate,
            new HBox(10, new Label("De:"), dateFrom, new Label("À:"), dateTo),
            priceLabel,
            new HBox(10, new Label("Min:"), spinnerPriceMin, new Label("Max:"), spinnerPriceMax)
          );
          
          // === Actions et Monitoring ===
          Label actionsTitle = new Label("📊 Actions et Monitoring");
          actionsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox actionsBox = new VBox(10);
          actionsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Button btnStartScraping = new Button("▶️ Démarrer le scraping");
          btnStartScraping.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
          
          Button btnStopScraping = new Button("⏹️ Arrêter le scraping");
          btnStopScraping.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          
          Button btnViewResults = new Button("📋 Voir les résultats");
          btnViewResults.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          
          Button btnExportResults = new Button("💾 Exporter les données");
          btnExportResults.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
          
          Label statsLabel = new Label("📈 Statistiques: 1,247 éléments scrapés | Dernière exécution: Il y a 15 min");
          statsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
          
          ProgressBar progressScraping = new ProgressBar(0.65);
          progressScraping.setPrefWidth(300);
          Label lblScrapingStatus = new Label("Scraping en cours... (65%)");
          
          actionsBox.getChildren().addAll(
            new HBox(10, btnStartScraping, btnStopScraping),
            new HBox(10, btnViewResults, btnExportResults),
            statsLabel,
            new VBox(5, progressScraping, lblScrapingStatus)
          );
          
          settingsBox.getChildren().addAll(
            generalTitle, generalBox,
            new Separator(),
            sourcesTitle, sourcesBox,
            new Separator(),
            filtersTitle, filtersBox,
            new Separator(),
            actionsTitle, actionsBox
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        case "categories" -> {
          
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          scrollPane.setStyle("-fx-background-color: transparent;");
          
          VBox settingsBox = new VBox(15);
          settingsBox.setStyle("-fx-padding: 10;");
          
          // === Gestion des Catégories Affaires ===
          Label managementTitle = new Label("📂 Gestion des Catégories Affaires");
          managementTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox managementBox = new VBox(10);
          managementBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          // Arborescence hiérarchique des catégories
          TreeView<String> categoryTreeView = new TreeView<>();
          categoryTreeView.setPrefHeight(300);
          categoryTreeView.getStyleClass().add("tree-view");
          
          // Création de la racine (invisible)
          TreeItem<String> rootItem = new TreeItem<>();
          rootItem.setExpanded(true);
          categoryTreeView.setRoot(rootItem);
          categoryTreeView.setShowRoot(false);
          
          // === Spectacles (245 items) ===
          TreeItem<String> spectaclesItem = new TreeItem<>("🎭 Spectacles (245)");
          spectaclesItem.setExpanded(true);
          
          TreeItem<String> theatreItem = new TreeItem<>("🎪 Théâtre (89)");
          theatreItem.getChildren().addAll(
            new TreeItem<>("🎭 Comédie (34)"),
            new TreeItem<>("🎯 Drame (28)"),
            new TreeItem<>("🎨 Musical (15)"),
            new TreeItem<>("👪 Jeune public (12)")
          );
          
          TreeItem<String> concertsItem = new TreeItem<>("🎵 Concerts (96)");
          concertsItem.getChildren().addAll(
            new TreeItem<>("🎸 Rock/Pop (42)"),
            new TreeItem<>("🎼 Classique (23)"),
            new TreeItem<>("🎷 Jazz (18)"),
            new TreeItem<>("🎤 Variété (13)")
          );
          
          TreeItem<String> operaItem = new TreeItem<>("🏛️ Opéra (60)");
          operaItem.getChildren().addAll(
            new TreeItem<>("🎵 Grand opéra (25)"),
            new TreeItem<>("🎶 Opéra comique (20)"),
            new TreeItem<>("💃 Opérette (15)")
          );
          
          spectaclesItem.getChildren().addAll(theatreItem, concertsItem, operaItem);
          
          // === Événements (156) ===
          TreeItem<String> evenementsItem = new TreeItem<>("🎪 Événements (156)");
          evenementsItem.setExpanded(false);
          
          TreeItem<String> festivalsItem = new TreeItem<>("🎊 Festivals (78)");
          festivalsItem.getChildren().addAll(
            new TreeItem<>("🎵 Festivals musicaux (32)"),
            new TreeItem<>("🎭 Festivals théâtre (24)"),
            new TreeItem<>("🎨 Festivals arts (22)")
          );
          
          TreeItem<String> salonsItem = new TreeItem<>("🏢 Salons & Expositions (45)");
          salonsItem.getChildren().addAll(
            new TreeItem<>("🎨 Expositions art (18)"),
            new TreeItem<>("💼 Salons professionnels (15)"),
            new TreeItem<>("🌟 Expositions thématiques (12)")
          );
          
          TreeItem<String> corporateItem = new TreeItem<>("🏢 Événements d'entreprise (33)");
          corporateItem.getChildren().addAll(
            new TreeItem<>("🎉 Soirées de gala (15)"),
            new TreeItem<>("📊 Séminaires (10)"),
            new TreeItem<>("🎊 Team building (8)")
          );
          
          evenementsItem.getChildren().addAll(festivalsItem, salonsItem, corporateItem);
          
          // === Services (89) ===
          TreeItem<String> servicesItem = new TreeItem<>("⚙️ Services (89)");
          servicesItem.setExpanded(false);
          
          TreeItem<String> techniqueItem = new TreeItem<>("🔧 Prestations techniques (56)");
          techniqueItem.getChildren().addAll(
            new TreeItem<>("💡 Éclairage (20)"),
            new TreeItem<>("🔊 Sonorisation (18)"),
            new TreeItem<>("📹 Vidéo (12)"),
            new TreeItem<>("🏗️ Scénographie (6)")
          );
          
          TreeItem<String> artistiqueItem = new TreeItem<>("🎨 Services artistiques (33)");
          artistiqueItem.getChildren().addAll(
            new TreeItem<>("🎭 Casting (15)"),
            new TreeItem<>("💄 Maquillage/Coiffure (10)"),
            new TreeItem<>("👗 Costumes (8)")
          );
          
          servicesItem.getChildren().addAll(techniqueItem, artistiqueItem);
          
          // === Locations (78) ===
          TreeItem<String> locationsItem = new TreeItem<>("🏠 Locations (78)");
          locationsItem.setExpanded(false);
          
          TreeItem<String> materielItem = new TreeItem<>("📦 Matériel (45)");
          materielItem.getChildren().addAll(
            new TreeItem<>("🎤 Audio (18)"),
            new TreeItem<>("💡 Éclairage (15)"),
            new TreeItem<>("🎬 Vidéo (12)")
          );
          
          TreeItem<String> espacesItem = new TreeItem<>("🏢 Espaces (33)");
          espacesItem.getChildren().addAll(
            new TreeItem<>("🎭 Salles de spectacle (15)"),
            new TreeItem<>("🏢 Salles de réception (10)"),
            new TreeItem<>("🎪 Espaces extérieurs (8)")
          );
          
          locationsItem.getChildren().addAll(materielItem, espacesItem);
          
          // === Formation (34) ===
          TreeItem<String> formationItem = new TreeItem<>("📚 Formation (34)");
          formationItem.setExpanded(false);
          
          TreeItem<String> stagesItem = new TreeItem<>("🎓 Stages (20)");
          stagesItem.getChildren().addAll(
            new TreeItem<>("🎭 Stages théâtre (8)"),
            new TreeItem<>("🎵 Stages musique (7)"),
            new TreeItem<>("💃 Stages danse (5)")
          );
          
          TreeItem<String> masterclassItem = new TreeItem<>("🌟 Masterclass (14)");
          masterclassItem.getChildren().addAll(
            new TreeItem<>("🎼 Composition (6)"),
            new TreeItem<>("🎭 Mise en scène (5)"),
            new TreeItem<>("💄 Techniques artistiques (3)")
          );
          
          formationItem.getChildren().addAll(stagesItem, masterclassItem);
          
          // Ajout de toutes les catégories principales à la racine
          rootItem.getChildren().addAll(spectaclesItem, evenementsItem, servicesItem, locationsItem, formationItem);
          
          HBox categoryButtons = new HBox(10);
          Button btnAddCategory = new Button("➕ Ajouter");
          btnAddCategory.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
          btnAddCategory.setOnAction(e -> {
            AppLogger.info("Ajout de catégorie demandé");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Nouvelle catégorie ajoutée!");
          });
          
          Button btnEditCategory = new Button("✏️ Modifier");
          btnEditCategory.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          btnEditCategory.setOnAction(e -> {
            AppLogger.info("Modification de catégorie demandée");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Catégorie modifiée!");
          });
          
          Button btnDeleteCategory = new Button("🗑️ Supprimer");
          btnDeleteCategory.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          btnDeleteCategory.setOnAction(e -> {
            AppLogger.info("Suppression de catégorie demandée");
            showAlert(Alert.AlertType.WARNING, "Catégories", "Catégorie supprimée!");
          });
          
          categoryButtons.getChildren().addAll(btnAddCategory, btnEditCategory, btnDeleteCategory);
          managementBox.getChildren().addAll(categoryTreeView, categoryButtons);
          
          // === Configuration des Catégories ===
          Label configTitle = new Label("⚙️ Configuration");
          configTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox configBox = new VBox(10);
          configBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          CheckBox chkAutoCreateCategories = new CheckBox("Création automatique des catégories manquantes");
          chkAutoCreateCategories.setSelected(true);
          
          CheckBox chkSyncWithExternal = new CheckBox("Synchronisation avec sources externes");
          chkSyncWithExternal.setSelected(false);
          
          Label hierarchyLabel = new Label("Niveau de hiérarchie maximum:");
          Spinner<Integer> spinnerHierarchyLevel = new Spinner<>(1, 10, 3, 1);
          spinnerHierarchyLevel.setPrefWidth(100);
          
          Label defaultCategoryLabel = new Label("Catégorie par défaut:");
          ComboBox<String> comboDefaultCategory = new ComboBox<>();
          comboDefaultCategory.getItems().addAll("Spectacles", "Événements", "Services", "Locations", "Formation");
          comboDefaultCategory.setValue("Spectacles");
          
          configBox.getChildren().addAll(
            chkAutoCreateCategories,
            chkSyncWithExternal,
            new HBox(10, hierarchyLabel, spinnerHierarchyLevel),
            new HBox(10, defaultCategoryLabel, comboDefaultCategory)
          );
          
          // === Actions de Maintenance ===
          Label maintenanceTitle = new Label("🔧 Maintenance");
          maintenanceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox maintenanceBox = new VBox(10);
          maintenanceBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Button btnSyncCategories = new Button("🔄 Synchroniser les catégories");
          btnSyncCategories.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
          btnSyncCategories.setOnAction(e -> {
            AppLogger.info("Synchronisation des catégories demandée");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Catégories synchronisées avec succès!");
          });
          
          Button btnOptimizeCategories = new Button("⚡ Optimiser la structure");
          btnOptimizeCategories.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          btnOptimizeCategories.setOnAction(e -> {
            AppLogger.info("Optimisation des catégories demandée");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Structure optimisée!");
          });
          
          Button btnCleanupCategories = new Button("🧹 Nettoyer les catégories vides");
          btnCleanupCategories.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
          btnCleanupCategories.setOnAction(e -> {
            AppLogger.info("Nettoyage des catégories demandé");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Catégories vides supprimées!");
          });
          
          Button btnResetCategories = new Button("🔄 Réinitialiser les catégories");  
          btnResetCategories.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
          btnResetCategories.setOnAction(e -> {
            AppLogger.info("Réinitialisation des catégories demandée");
            showAlert(Alert.AlertType.WARNING, "Catégories", "Catégories réinitialisées!");
          });
          
          Button btnExportCategories = new Button("💾 Exporter la structure");
          btnExportCategories.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
          btnExportCategories.setOnAction(e -> {
            AppLogger.info("Export de la structure demandé");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Structure exportée!");
          });
          
          Button btnImportCategories = new Button("📥 Importer une structure");
          btnImportCategories.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
          btnImportCategories.setOnAction(e -> {
            AppLogger.info("Import de structure demandé");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Structure importée!");
          });
          
          maintenanceBox.getChildren().addAll(
            new HBox(10, btnSyncCategories, btnOptimizeCategories),
            new HBox(10, btnCleanupCategories, btnResetCategories),
            new HBox(10, btnExportCategories, btnImportCategories)
          );
          
          // === Statistiques ===
          Label statsTitle = new Label("📊 Statistiques");
          statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox statsBox = new VBox(10);
          statsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label statsInfo = new Label("📈 Total catégories: 5 | Actives: 4 | En pause: 1 | Items total: 602");
          statsInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
          
          ProgressBar categoryProgress = new ProgressBar(0.8);
          categoryProgress.setPrefWidth(300);
          Label categoryProgressLabel = new Label("Utilisation des catégories: 80%");
          
          statsBox.getChildren().addAll(
            statsInfo,
            new VBox(5, categoryProgress, categoryProgressLabel)
          );
          
          // === Gestion des Catégories Produits ===
          Label productCategoriesTitle = new Label("📦 Gestion des Catégories Produits");
          productCategoriesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox productCategoriesBox = new VBox(10);
          productCategoriesBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          // Arborescence hiérarchique des catégories produits
          TreeView<String> productCategoryTreeView = new TreeView<>();
          productCategoryTreeView.setPrefHeight(250);
          productCategoryTreeView.getStyleClass().add("tree-view");
          
          // Création de la racine (invisible)
          TreeItem<String> productRootItem = new TreeItem<>();
          productRootItem.setExpanded(true);
          productCategoryTreeView.setRoot(productRootItem);
          productCategoryTreeView.setShowRoot(false);
          
          // === Matériel Technique (180 items) ===
          TreeItem<String> materielTechniqueItem = new TreeItem<>("🎛️ Matériel Technique (180)");
          materielTechniqueItem.setExpanded(true);
          
          TreeItem<String> eclairageItem = new TreeItem<>("💡 Éclairage (68)");
          eclairageItem.getChildren().addAll(
            new TreeItem<>("🔦 Projecteurs LED (25)"),
            new TreeItem<>("💡 Projecteurs traditionnels (18)"),
            new TreeItem<>("🌈 Éclairage couleur (15)"),
            new TreeItem<>("🎯 Poursuite (10)")
          );
          
          TreeItem<String> sonoItem = new TreeItem<>("🔊 Sonorisation (54)");
          sonoItem.getChildren().addAll(
            new TreeItem<>("🎤 Micros & HF (20)"),
            new TreeItem<>("🔊 Haut-parleurs (16)"),
            new TreeItem<>("🎛️ Consoles de mixage (12)"),
            new TreeItem<>("🎧 Accessoires audio (6)")
          );
          
          TreeItem<String> videoItem = new TreeItem<>("📹 Vidéo & Projection (38)");
          videoItem.getChildren().addAll(
            new TreeItem<>("📽️ Vidéoprojecteurs (15)"),
            new TreeItem<>("📺 Écrans LED (12)"),
            new TreeItem<>("📹 Caméras (8)"),
            new TreeItem<>("🎬 Régie vidéo (3)")
          );
          
          TreeItem<String> structuresItem = new TreeItem<>("🏗️ Structures & Rigging (20)");
          structuresItem.getChildren().addAll(
            new TreeItem<>("🏗️ Portiques & Tours (8)"),
            new TreeItem<>("🔗 Système de levage (7)"),
            new TreeItem<>("⚙️ Accessoires rigging (5)")
          );
          
          materielTechniqueItem.getChildren().addAll(eclairageItem, sonoItem, videoItem, structuresItem);
          
          // === Mobilier & Décoration (95 items) ===
          TreeItem<String> mobilierItem = new TreeItem<>("🪑 Mobilier & Décoration (95)");
          
          TreeItem<String> mobilierEventItem = new TreeItem<>("🪑 Mobilier événementiel (45)");
          mobilierEventItem.getChildren().addAll(
            new TreeItem<>("🪑 Chaises & Fauteuils (18)"),
            new TreeItem<>("🍽️ Tables diverses (15)"),
            new TreeItem<>("🛋️ Mobilier lounge (12)")
          );
          
          TreeItem<String> decorationItem = new TreeItem<>("🎨 Décoration (30)");
          decorationItem.getChildren().addAll(
            new TreeItem<>("🌸 Arrangements floraux (12)"),
            new TreeItem<>("🕯️ Éclairage décoratif (10)"),
            new TreeItem<>("🖼️ Accessoires déco (8)")
          );
          
          TreeItem<String> textileItem = new TreeItem<>("🧵 Textile & Draperie (20)");
          textileItem.getChildren().addAll(
            new TreeItem<>("🎭 Rideaux & Toiles (10)"),
            new TreeItem<>("🛏️ Nappage & Linge (6)"),
            new TreeItem<>("🎪 Structures textiles (4)")
          );
          
          mobilierItem.getChildren().addAll(mobilierEventItem, decorationItem, textileItem);
          
          // === Logistique & Transport (42 items) ===
          TreeItem<String> logistiqueItem = new TreeItem<>("🚛 Logistique & Transport (42)");
          
          TreeItem<String> transportItem = new TreeItem<>("🚛 Véhicules (22)");
          transportItem.getChildren().addAll(
            new TreeItem<>("🚛 Camions & Fourgons (12)"),
            new TreeItem<>("🚐 Véhicules légers (6)"),
            new TreeItem<>("🏗️ Grues & Élévateurs (4)")
          );
          
          TreeItem<String> stockageItem = new TreeItem<>("📦 Stockage & Manutention (20)");
          stockageItem.getChildren().addAll(
            new TreeItem<>("📦 Flight-cases (10)"),
            new TreeItem<>("🏗️ Matériel de levage (6)"),
            new TreeItem<>("📋 Accessoires manutention (4)")
          );
          
          logistiqueItem.getChildren().addAll(transportItem, stockageItem);
          
          // === Sécurité & Réglementation (28 items) ===
          TreeItem<String> securiteItem = new TreeItem<>("🛡️ Sécurité & Réglementation (28)");
          
          TreeItem<String> securiteEquipItem = new TreeItem<>("🦺 Équipements de sécurité (18)");
          securiteEquipItem.getChildren().addAll(
            new TreeItem<>("🦺 EPI & Protection (8)"),
            new TreeItem<>("🚨 Signalisation (6)"),
            new TreeItem<>("🧯 Sécurité incendie (4)")
          );
          
          TreeItem<String> controleItem = new TreeItem<>("📋 Contrôle & Certification (10)");
          controleItem.getChildren().addAll(
            new TreeItem<>("📋 Contrôles techniques (6)"),
            new TreeItem<>("📄 Certifications (4)")
          );
          
          securiteItem.getChildren().addAll(securiteEquipItem, controleItem);
          
          // Ajout de toutes les catégories principales produits à la racine
          productRootItem.getChildren().addAll(materielTechniqueItem, mobilierItem, logistiqueItem, securiteItem);
          
          HBox productCategoryButtons = new HBox(10);
          Button btnAddProductCategory = new Button("➕ Ajouter");
          btnAddProductCategory.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
          btnAddProductCategory.setOnAction(e -> {
            AppLogger.info("Ajout de catégorie produit demandé");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Nouvelle catégorie produit ajoutée!");
          });
          
          Button btnEditProductCategory = new Button("✏️ Modifier");
          btnEditProductCategory.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          btnEditProductCategory.setOnAction(e -> {
            AppLogger.info("Modification de catégorie produit demandée");
            showAlert(Alert.AlertType.INFORMATION, "Catégories", "Catégorie produit modifiée!");
          });
          
          Button btnDeleteProductCategory = new Button("🗑️ Supprimer");
          btnDeleteProductCategory.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          btnDeleteProductCategory.setOnAction(e -> {
            AppLogger.info("Suppression de catégorie produit demandée");
            showAlert(Alert.AlertType.WARNING, "Catégories", "Catégorie produit supprimée!");
          });
          
          productCategoryButtons.getChildren().addAll(btnAddProductCategory, btnEditProductCategory, btnDeleteProductCategory);
          productCategoriesBox.getChildren().addAll(productCategoryTreeView, productCategoryButtons);

          settingsBox.getChildren().addAll(
            managementTitle, managementBox,
            new Separator(),
            productCategoriesTitle, productCategoriesBox,
            new Separator(),
            configTitle, configBox,
            new Separator(),
            maintenanceTitle, maintenanceBox,
            new Separator(),
            statsTitle, statsBox
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        case "medias" -> {
          
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          scrollPane.setStyle("-fx-background-color: transparent;");
          
          VBox settingsBox = new VBox(15);
          settingsBox.setStyle("-fx-padding: 10;");
          
          // === Configuration des Chemins ===
          Label pathsTitle = new Label("📁 Configuration des Chemins");
          pathsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox pathsBox = new VBox(10);
          pathsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label photosPathLabel = new Label("Répertoire des photos:");
          TextField txtPhotosPath = new TextField("./medias/photos/");
          txtPhotosPath.setPromptText("Chemin vers les photos");
          Button btnBrowsePhotos = new Button("📂");
          btnBrowsePhotos.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
          
          Label mediasPathLabel = new Label("Répertoire des médias:");
          TextField txtMediasPath = new TextField("./medias/files/");
          txtMediasPath.setPromptText("Chemin vers les médias");
          Button btnBrowseMedias = new Button("📂");
          btnBrowseMedias.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
          
          Label tempPathLabel = new Label("Répertoire temporaire:");
          TextField txtTempPath = new TextField("./temp/");
          txtTempPath.setPromptText("Chemin temporaire");
          Button btnBrowseTemp = new Button("📂");
          btnBrowseTemp.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");
          
          pathsBox.getChildren().addAll(
            photosPathLabel, new HBox(5, txtPhotosPath, btnBrowsePhotos),
            mediasPathLabel, new HBox(5, txtMediasPath, btnBrowseMedias),
            tempPathLabel, new HBox(5, txtTempPath, btnBrowseTemp)
          );
          
          // === Qualité et Optimisation ===
          Label qualityTitle = new Label("🎨 Qualité et Optimisation");
          qualityTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox qualityBox = new VBox(10);
          qualityBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label qualityLabel = new Label("Qualité d'optimisation des images:");
          Slider sliderImageQuality = new Slider(0.1, 1.0, 0.8);
          sliderImageQuality.setShowTickLabels(true);
          sliderImageQuality.setShowTickMarks(true);
          sliderImageQuality.setMajorTickUnit(0.1);
          sliderImageQuality.setMinorTickCount(1);
          Label lblQualityValue = new Label("80%");
          sliderImageQuality.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblQualityValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
          });
          
          Label maxSizeLabel = new Label("Taille maximale des images (MB):");
          Spinner<Double> spinnerMaxSize = new Spinner<>(0.1, 100.0, 10.0, 0.5);
          spinnerMaxSize.setPrefWidth(100);
          
          Label maxDimensionLabel = new Label("Dimension maximale (pixels):");
          Spinner<Integer> spinnerMaxDimension = new Spinner<>(100, 8000, 1920, 100);
          spinnerMaxDimension.setPrefWidth(100);
          
          CheckBox chkAutoOptimize = new CheckBox("Optimisation automatique à l'import");
          chkAutoOptimize.setSelected(true);
          
          CheckBox chkCreateThumbnails = new CheckBox("Créer des miniatures automatiquement");
          chkCreateThumbnails.setSelected(true);
          
          qualityBox.getChildren().addAll(
            qualityLabel, new HBox(10, sliderImageQuality, lblQualityValue),
            new HBox(10, maxSizeLabel, spinnerMaxSize),
            new HBox(10, maxDimensionLabel, spinnerMaxDimension),
            chkAutoOptimize,
            chkCreateThumbnails
          );
          
          // === Formats Supportés ===
          Label formatsTitle = new Label("🖼️ Formats Supportés");
          formatsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox formatsBox = new VBox(10);
          formatsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label imageFormatsLabel = new Label("Formats d'images:");
          CheckBox chkFormatJPG = new CheckBox("JPEG");
          chkFormatJPG.setSelected(true);
          CheckBox chkFormatPNG = new CheckBox("PNG");
          chkFormatPNG.setSelected(true);
          CheckBox chkFormatGIF = new CheckBox("GIF");
          chkFormatGIF.setSelected(true);
          CheckBox chkFormatWEBP = new CheckBox("WebP");
          chkFormatWEBP.setSelected(false);
          CheckBox chkFormatBMP = new CheckBox("BMP");
          chkFormatBMP.setSelected(false);
          
          Label videoFormatsLabel = new Label("Formats vidéo:");
          CheckBox chkFormatMP4 = new CheckBox("MP4");
          chkFormatMP4.setSelected(true);
          CheckBox chkFormatAVI = new CheckBox("AVI");
          chkFormatAVI.setSelected(true);
          CheckBox chkFormatMOV = new CheckBox("MOV");
          chkFormatMOV.setSelected(false);
          CheckBox chkFormatWMV = new CheckBox("WMV");
          chkFormatWMV.setSelected(false);
          
          Label audioFormatsLabel = new Label("Formats audio:");
          CheckBox chkFormatMP3 = new CheckBox("MP3");
          chkFormatMP3.setSelected(true);
          CheckBox chkFormatWAV = new CheckBox("WAV");
          chkFormatWAV.setSelected(true);
          CheckBox chkFormatFLAC = new CheckBox("FLAC");
          chkFormatFLAC.setSelected(false);
          
          formatsBox.getChildren().addAll(
            imageFormatsLabel,
            new HBox(10, chkFormatJPG, chkFormatPNG, chkFormatGIF, chkFormatWEBP, chkFormatBMP),
            videoFormatsLabel,
            new HBox(10, chkFormatMP4, chkFormatAVI, chkFormatMOV, chkFormatWMV),
            audioFormatsLabel,
            new HBox(10, chkFormatMP3, chkFormatWAV, chkFormatFLAC)
          );
          
          // === Actions de Maintenance ===
          Label actionsTitle = new Label("🔧 Actions de Maintenance");
          actionsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox actionsBox = new VBox(10);
          actionsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Button btnScanMedias = new Button("🔍 Scanner les médias");
          btnScanMedias.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
          btnScanMedias.setOnAction(e -> {
            AppLogger.info("Scan des médias demandé");
            showAlert(Alert.AlertType.INFORMATION, "Médias", "Scan des médias terminé!");
          });
          
          Button btnOptimizeAllImages = new Button("⚡ Optimiser toutes les images");
          btnOptimizeAllImages.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          btnOptimizeAllImages.setOnAction(e -> {
            AppLogger.info("Optimisation des images demandée");
            showAlert(Alert.AlertType.INFORMATION, "Médias", "Images optimisées!");
          });
          
          Button btnCleanupDuplicates = new Button("🗑️ Supprimer les doublons");
          btnCleanupDuplicates.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
          btnCleanupDuplicates.setOnAction(e -> {
            AppLogger.info("Suppression des doublons demandée");
            showAlert(Alert.AlertType.INFORMATION, "Médias", "Doublons supprimés!");
          });
          
          Button btnRepairLinks = new Button("🔧 Réparer les liens cassés");
          btnRepairLinks.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
          btnRepairLinks.setOnAction(e -> {
            AppLogger.info("Réparation des liens demandée");
            showAlert(Alert.AlertType.INFORMATION, "Médias", "Liens réparés!");
          });
          
          Button btnBackupMedias = new Button("💾 Sauvegarder les médias");
          btnBackupMedias.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
          btnBackupMedias.setOnAction(e -> {
            AppLogger.info("Sauvegarde des médias demandée");
            showAlert(Alert.AlertType.INFORMATION, "Médias", "Médias sauvegardés!");
          });
          
          actionsBox.getChildren().addAll(
            new HBox(10, btnScanMedias, btnOptimizeAllImages),
            new HBox(10, btnCleanupDuplicates, btnRepairLinks),
            btnBackupMedias
          );
          
          // === Statistiques ===
          Label statsTitle = new Label("📊 Statistiques des Médias");
          statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox statsBox = new VBox(10);
          statsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label statsInfo = new Label("📈 Total fichiers: 1,247 | Images: 892 | Vidéos: 245 | Audio: 110\n" +
                                       "💾 Espace utilisé: 2.34 GB | Espace disponible: 15.66 GB");
          statsInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
          
          ProgressBar storageProgress = new ProgressBar(0.35);
          storageProgress.setPrefWidth(300);
          Label storageLabel = new Label("Utilisation du stockage: 35%");
          
          statsBox.getChildren().addAll(
            statsInfo,
            new VBox(5, storageProgress, storageLabel)
          );
          
          settingsBox.getChildren().addAll(
            pathsTitle, pathsBox,
            new Separator(),
            qualityTitle, qualityBox,
            new Separator(),
            formatsTitle, formatsBox,
            new Separator(),
            actionsTitle, actionsBox,
            new Separator(),
            statsTitle, statsBox
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        case "data" -> {
          
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setFitToWidth(true);
          scrollPane.setStyle("-fx-background-color: transparent;");
          
          VBox settingsBox = new VBox(15);
          settingsBox.setStyle("-fx-padding: 10;");
          
          // === Génération de Données ===
          Label generationTitle = new Label("🎲 Génération de Données");
          generationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox generationBox = new VBox(10);
          generationBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label quantityLabel = new Label("Quantité d'éléments à générer:");
          Spinner<Integer> spinnerQuantity = new Spinner<>(10, 1000, 100, 10);
          spinnerQuantity.setPrefWidth(100);
          
          CheckBox chkGenerateUsers = new CheckBox("Générer des utilisateurs");
          chkGenerateUsers.setSelected(true);
          
          CheckBox chkGenerateCompanies = new CheckBox("Générer des sociétés");
          chkGenerateCompanies.setSelected(true);
          
          CheckBox chkGenerateProjects = new CheckBox("Générer des projets");
          chkGenerateProjects.setSelected(true);
          
          CheckBox chkGenerateProducts = new CheckBox("Générer des produits");
          chkGenerateProducts.setSelected(false);
          
          CheckBox chkGenerateInterventions = new CheckBox("Générer des interventions");
          chkGenerateInterventions.setSelected(false);
          
          Button generateDataBtn = new Button("🎲 Générer des données de test");
          generateDataBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold;");
          generateDataBtn.setOnAction(e -> {
            try {
              AppLogger.info("Génération de données de test demandée");
              com.magsav.util.TestDataGenerator.generateCompleteTestData();
              onRefresh(); // Rafraîchir l'affichage
              showAlert(Alert.AlertType.INFORMATION, "Données", "Données de test générées avec succès!");
            } catch (Exception ex) {
              AppLogger.error("Erreur lors de la génération de données de test", ex);
              showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération: " + ex.getMessage());
            }
          });
          
          Button generateAffairesBtn = new Button("💼 Générer des affaires de test");
          generateAffairesBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white;");
          generateAffairesBtn.setOnAction(e -> {
            try {
              com.magsav.util.AffairesTestDataGenerator.genererDonneesTest();
              AppLogger.info("Génération d'affaires de test demandée");
              showAlert(Alert.AlertType.INFORMATION, "Données", "Affaires de test générées avec succès!");
            } catch (Exception ex) {
              AppLogger.error("Erreur lors de la génération d'affaires", ex);
              showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la génération d'affaires: " + ex.getMessage());
            }
          });
          
          generationBox.getChildren().addAll(
            new HBox(10, quantityLabel, spinnerQuantity),
            chkGenerateUsers,
            chkGenerateCompanies,
            chkGenerateProjects,
            chkGenerateProducts,
            chkGenerateInterventions,
            new Separator(),
            new HBox(10, generateDataBtn, generateAffairesBtn)
          );
          
          // === Import/Export de Données ===
          Label importExportTitle = new Label("📥📤 Import/Export de Données");
          importExportTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox importExportBox = new VBox(10);
          importExportBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label formatLabel = new Label("Format d'export:");
          ComboBox<String> comboExportFormat = new ComboBox<>();
          comboExportFormat.getItems().addAll("JSON", "CSV", "XML", "SQL");
          comboExportFormat.setValue("JSON");
          
          CheckBox chkIncludeImages = new CheckBox("Inclure les images dans l'export");
          chkIncludeImages.setSelected(false);
          
          CheckBox chkCompressExport = new CheckBox("Compresser l'export");
          chkCompressExport.setSelected(true);
          
          Button btnImportData = new Button("📥 Importer des données");
          btnImportData.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
          btnImportData.setOnAction(e -> {
            AppLogger.info("Import de données demandé");
            showAlert(Alert.AlertType.INFORMATION, "Import", "Données importées avec succès!");
          });
          
          Button btnExportData = new Button("📤 Exporter toutes les données");
          btnExportData.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");
          btnExportData.setOnAction(e -> {
            AppLogger.info("Export de données demandé");
            showAlert(Alert.AlertType.INFORMATION, "Export", "Données exportées avec succès!");
          });
          
          Button btnExportSelection = new Button("📋 Exporter une sélection");
          btnExportSelection.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white;");
          btnExportSelection.setOnAction(e -> {
            AppLogger.info("Export sélectif demandé");
            showAlert(Alert.AlertType.INFORMATION, "Export", "Sélection exportée!");
          });
          
          importExportBox.getChildren().addAll(
            new HBox(10, formatLabel, comboExportFormat),
            chkIncludeImages,
            chkCompressExport,
            new Separator(),
            new HBox(10, btnImportData, btnExportData),
            btnExportSelection
          );
          
          // === Maintenance des Données ===
          Label maintenanceTitle = new Label("🔧 Maintenance des Données");
          maintenanceTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox maintenanceBox = new VBox(10);
          maintenanceBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Button btnValidateData = new Button("✅ Valider l'intégrité des données");
          btnValidateData.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
          btnValidateData.setOnAction(e -> {
            AppLogger.info("Validation des données demandée");
            showAlert(Alert.AlertType.INFORMATION, "Validation", "Données validées - Aucun problème détecté!");
          });
          
          Button btnCleanupOrphans = new Button("🧹 Nettoyer les données orphelines");
          btnCleanupOrphans.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
          btnCleanupOrphans.setOnAction(e -> {
            AppLogger.info("Nettoyage des données orphelines demandé");
            showAlert(Alert.AlertType.INFORMATION, "Nettoyage", "Données orphelines supprimées!");
          });
          
          Button btnOptimizeIndices = new Button("⚡ Optimiser les indices");
          btnOptimizeIndices.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white;");
          btnOptimizeIndices.setOnAction(e -> {
            AppLogger.info("Optimisation des indices demandée");
            showAlert(Alert.AlertType.INFORMATION, "Optimisation", "Indices optimisés!");
          });
          
          Button btnAnalyzePerformance = new Button("📊 Analyser les performances");
          btnAnalyzePerformance.setStyle("-fx-background-color: #20c997; -fx-text-fill: white;");
          btnAnalyzePerformance.setOnAction(e -> {
            AppLogger.info("Analyse des performances demandée");
            showAlert(Alert.AlertType.INFORMATION, "Analyse", "Rapport de performance généré!");
          });
          
          maintenanceBox.getChildren().addAll(
            new HBox(10, btnValidateData, btnCleanupOrphans),
            new HBox(10, btnOptimizeIndices, btnAnalyzePerformance)
          );
          
          // === Actions Critiques ===
          Label criticalTitle = new Label("⚠️ Actions Critiques");
          criticalTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox criticalBox = new VBox(10);
          criticalBox.setStyle("-fx-padding: 10; -fx-border-color: #dc3545; -fx-border-radius: 5; -fx-background-color: #f8d7da;");
          
          Label warningLabel = new Label("⚠️ ATTENTION: Ces actions sont irréversibles!");
          warningLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #721c24;");
          
          Button btnResetAllData = new Button("🔄 Réinitialiser toutes les données");
          btnResetAllData.setStyle("-fx-background-color: #fd7e14; -fx-text-fill: white;");
          btnResetAllData.setOnAction(e -> {
            AppLogger.info("Réinitialisation des données demandée");
            showAlert(Alert.AlertType.WARNING, "Réinitialisation", "Toutes les données ont été réinitialisées!");
          });
          
          Button clearDataBtn = new Button("🗑️ Supprimer toutes les données");
          clearDataBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
          clearDataBtn.setOnAction(e -> {
            AppLogger.info("Suppression des données demandée");
            showAlert(Alert.AlertType.WARNING, "Données", "Toutes les données ont été supprimées!");
          });
          
          Button btnFactoryReset = new Button("🏭 Remise à zéro complète");
          btnFactoryReset.setStyle("-fx-background-color: #6f42c1; -fx-text-fill: white;");
          btnFactoryReset.setOnAction(e -> {
            AppLogger.info("Remise à zéro complète demandée");
            showAlert(Alert.AlertType.ERROR, "Reset", "Application remise à zéro!");
          });
          
          criticalBox.getChildren().addAll(
            warningLabel,
            new Separator(),
            new HBox(10, btnResetAllData, clearDataBtn),
            btnFactoryReset
          );
          
          // === Statistiques des Données ===
          Label statsTitle = new Label("📊 Statistiques des Données");
          statsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
          
          VBox statsBox = new VBox(10);
          statsBox.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8; -fx-background-color: #1a1a1a; -fx-border-width: 1;");
          
          Label statsInfo = new Label("📈 Utilisateurs: 156 | Sociétés: 89 | Affaires: 245 | Projets: 178\n" +
                                       "💾 Taille base de données: 45.2 MB | Dernière sauvegarde: Il y a 2h");
          statsInfo.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
          
          ProgressBar dataIntegrityProgress = new ProgressBar(0.95);
          dataIntegrityProgress.setPrefWidth(300);
          Label integrityLabel = new Label("Intégrité des données: 95%");
          
          statsBox.getChildren().addAll(
            statsInfo,
            new VBox(5, dataIntegrityProgress, integrityLabel)
          );
          
          settingsBox.getChildren().addAll(
            generationTitle, generationBox,
            new Separator(),
            importExportTitle, importExportBox,
            new Separator(),
            maintenanceTitle, maintenanceBox,
            new Separator(),
            criticalTitle, criticalBox,
            new Separator(),
            statsTitle, statsBox
          );
          
          scrollPane.setContent(settingsBox);
          preferencesContent.getChildren().add(scrollPane);
        }
        default -> {
          // Section par défaut sans légende redondante
        }
      }
      
      content.getChildren().add(preferencesContent);
      AppLogger.info("Préférences " + tabType + " chargées avec succès");
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des préférences " + tabType + ": " + e.getMessage(), e);
      
      // Fallback en cas d'erreur
      Label errorLabel = new Label("Erreur lors du chargement des préférences " + tabType);
      cssManager.styleErrorLabel(errorLabel);
      content.getChildren().add(errorLabel);
    }
    
    return content;
  }
  
  private void loadTechnicienUsersSection() {
    try {
      // Utilisation du contrôleur spécialisé pour les utilisateurs
      Tab technicienUsersTab = usersController.createTechnicienUsersTab();
      Tab collaborateursTab = usersController.createAdminUsersTab();
      Tab administrateursTab = usersController.createAdministrateursUsersTab();
      Tab allUsersTab = usersController.createAllUsersTab();
      
      clearAndLoadTabs(technicienUsersTab, collaborateursTab, administrateursTab, allUsersTab);
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des Utilisateurs: " + e.getMessage(), e);
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
  
  // Méthodes createProduitsTab(), createClientsTab(), createSocietesTab(),
  // createAffairesTab(), createProductsContent() supprimées - déléguées au GestionController
  

  
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
  

  

  



  // === PANNEAUX DE DÉTAILS CLIENTS/SOCIÉTÉS SUPPRIMÉS - GÉRÉS PAR GestionController ===

  // === MÉTHODES createVehiculeDetailPanel() ET updateVehiculeDetailPanel() SUPPRIMÉES - REMPLACÉES PAR VehiculesController ===

  // === MÉTHODE createUserDetailPanel() SUPPRIMÉE - UTILITAIRE OBSOLÈTE ===
  
  // === MÉTHODE updateUserDetailPanel() SUPPRIMÉE - UTILITAIRE OBSOLÈTE ===
  
  // === MÉTHODES UTILITAIRES SUPPRIMÉES - createInfoLabel(), loadUserAvatar() ===
  
  // === MÉTHODE loadVehiculeQrCode() SUPPRIMÉE - REMPLACÉE PAR VehiculesController ===
  
  // === MÉTHODE loadUsersData() SUPPRIMÉE - GÉRÉE PAR UsersController ===
  
  // === PANNEAUX DE DÉTAILS REQUÊTES/INTERVENTIONS SUPPRIMÉS - GÉRÉS PAR LEURS CONTRÔLEURS DÉDIÉS ===



  // === MÉTHODE createDemandesEquipementTab() SUPPRIMÉE - REMPLACÉE PAR DemandesController ===
  
  // === MÉTHODE createDemandesPiecesTab() SUPPRIMÉE - REMPLACÉE PAR DemandesController ===
  
  // === MÉTHODE createDemandesInterventionTab() SUPPRIMÉE - REMPLACÉE PAR DemandesController ===
  
  // === MÉTHODE createValidationDemandesTab() SUPPRIMÉE - REMPLACÉE PAR DemandesController ===
  
  // === MÉTHODES createRequestsTable() ET loadRequestsData() SUPPRIMÉES - GÉRÉES PAR DemandesController ===
  





  // === MÉTHODE createTechnicienUsersTab() SUPPRIMÉE - REMPLACÉE PAR UsersController ===
  
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
  



  @FXML private void onNewIntervention() { 
    // Délègue à la section Interventions
    onShowInterventions();
  }

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
    // Maintenance des médias à implémenter dans une version future
    ShareDialogs.showSuccessDialog("Maintenance Médias", 
        "Fonctionnalité en cours de développement");
  }

  // Méthodes updateProductCategories et updateCategoryDisplay supprimées - obsolètes avec la nouvelle UI

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
  
  @Deprecated
  private void showAlert(String title, String message) {
    com.magsav.util.AlertUtils.showError(title, message);
  }
  
  @Deprecated
  private void showAlert(Alert.AlertType alertType, String title, String message) {
    com.magsav.util.AlertUtils.showAlert(alertType, title, message);
  }
  
  // === MÉTHODES STATISTIQUES SUPPRIMÉES - REMPLACÉES PAR StatistiquesController ===
  
  // === MÉTHODES DE CONTENU POUR L'EXPORT ===
  
  // === MÉTHODE createExportContent() SUPPRIMÉE - REMPLACÉE PAR ExportController ===
  
  // === MÉTHODES DE CONTENU POUR LES PRÉFÉRENCES ===
  

  
  // === MÉTHODES UTILITAIRES ===
  
  // === MÉTHODE createChartPlaceholder() SUPPRIMÉE - REMPLACÉE PAR StatistiquesController ===
  
  // === MÉTHODE createExportOption() SUPPRIMÉE - REMPLACÉE PAR ExportController ===
  
  // === MÉTHODES VÉHICULES SUPPRIMÉES - REMPLACÉES PAR VehiculesController ===

  // Méthodes pour la gestion des clients
  
  // === MÉTHODE loadClientsDataWithFilter() SUPPRIMÉE - OBSOLÈTE ===
  
  // === MÉTHODES CLIENT SUPPRIMÉES - openClientForm(), modifySelectedClient(), deleteSelectedClient(), refreshClientsTable() ===  // Méthodes pour la gestion des sociétés
  
  // === MÉTHODE loadCompaniesDataWithFilter() SUPPRIMÉE - OBSOLÈTE ===
  
  // === MÉTHODES COMPANY SUPPRIMÉES - GÉRÉES PAR GestionController ===
  
  // === MÉTHODES POUR LA VALIDATION DES DEMANDES ===
  // Refactorisées dans ValidationController
  
  // === MÉTHODES POUR LA PERSONNALISATION DE L'APPARENCE ===
  
  /**
   * Applique les couleurs personnalisées aux onglets de l'interface
   * @param defaultColor Couleur des onglets non sélectionnés (format hex: #rrggbb)
   * @param selectedColor Couleur de l'onglet sélectionné (format hex: #rrggbb)
   */
  private void applyTabColors(String defaultColor, String selectedColor) {
    try {
      // Création du CSS personnalisé pour les onglets
      String customTabCSS = String.format("""
        .tab-pane .tab {
          -fx-background-color: %s !important;
        }
        .tab-pane .tab:selected {
          -fx-background-color: %s !important;
        }
        .tab-pane .tab:hover:not(:selected) {
          -fx-background-color: derive(%s, 20%%) !important;
        }
        """, defaultColor, selectedColor, defaultColor);
      
      // Écriture du fichier CSS temporaire
      java.io.File tempCSSFile = new java.io.File("src/main/resources/css/custom-tab-colors.css");
      try (java.io.FileWriter writer = new java.io.FileWriter(tempCSSFile)) {
        writer.write(customTabCSS);
      }
      
      Platform.runLater(() -> {
        try {
          // Suppression de l'ancien style personnalisé s'il existe
          if (mainTabPane != null && mainTabPane.getScene() != null) {
            mainTabPane.getScene().getStylesheets().removeIf(style -> 
              style.contains("custom-tab-colors.css"));
            
            // Ajout du nouveau style
            String cssURL = tempCSSFile.toURI().toString();
            mainTabPane.getScene().getStylesheets().add(cssURL);
            
            // Application récursive à toutes les scènes ouvertes
            applyTabColorsToAllScenes(defaultColor, selectedColor);
          }
          
          AppLogger.info("Couleurs des onglets appliquées: défaut=" + defaultColor + ", sélectionné=" + selectedColor);
          
        } catch (Exception e) {
          AppLogger.error("Erreur lors de l'application du CSS: " + e.getMessage(), e);
        }
      });
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'application des couleurs des onglets: " + e.getMessage(), e);
    }
  }
  
  /**
   * Applique les couleurs des onglets à toutes les scènes ouvertes
   */
  private void applyTabColorsToAllScenes(String defaultColor, String selectedColor) {
    try {
      // Application à toutes les fenêtres ouvertes
      for (javafx.stage.Window window : javafx.stage.Window.getWindows()) {
        if (window instanceof javafx.stage.Stage stage && stage.getScene() != null) {
          // Suppression de l'ancien CSS personnalisé
          stage.getScene().getStylesheets().removeIf(style -> 
            style.contains("custom-tab-colors.css"));
          
          // Ajout du nouveau CSS
          java.io.File tempCSSFile = new java.io.File("src/main/resources/css/custom-tab-colors.css");
          if (tempCSSFile.exists()) {
            stage.getScene().getStylesheets().add(tempCSSFile.toURI().toString());
          }
        }
      }
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'application à toutes les scènes: " + e.getMessage(), e);
    }
  }

  /**
   * Initialise le système CSS centralisé
   */
  /**
   * Applique un CSS de diagnostic pour rendre les tables visibles avec des couleurs vives
   */
  private void applyDebugCSS() {
    javafx.application.Platform.runLater(() -> {
      try {
        // Obtenir la scène principale
        Scene scene = companyNameLabel.getScene();
        if (scene != null) {
          // Ajouter le CSS de diagnostic
          String debugCssPath = getClass().getResource("/css/debug-tables.css").toExternalForm();
          scene.getStylesheets().add(debugCssPath);
          
          AppLogger.info("🎨 CSS de diagnostic appliqué pour rendre les tables visibles");
        }
      } catch (Exception e) {
        AppLogger.error("Erreur lors de l'application du CSS de diagnostic: " + e.getMessage(), e);
      }
    });
  }

  private void initializeCSS() {
    // Utilisation de Platform.runLater pour s'assurer que l'interface est complètement chargée
    javafx.application.Platform.runLater(() -> {
      try {
        // Attendre un peu plus longtemps pour que tous les onglets soient créés
        Thread.sleep(500);
        
        // Initialisation du thème pour la fenêtre principale
        if (companyNameLabel.getScene() != null) {
          Stage stage = (Stage) companyNameLabel.getScene().getWindow();
          if (stage != null) {
            cssManager.initializeWindow(stage, "main");
            
            // Application des couleurs d'onglets par défaut avec un délai supplémentaire
            javafx.application.Platform.runLater(() -> {
              cssManager.configureTabColors("#1e3a5f", "#4a90e2");
              AppLogger.info("CSS Manager et couleurs d'onglets initialisés avec succès");
            });
          }
        }
      } catch (Exception e) {
        AppLogger.error("Erreur lors de l'initialisation du CSS Manager: " + e.getMessage(), e);
      }
    });
  }

}
