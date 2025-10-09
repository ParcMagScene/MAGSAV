package com.magsav.gui;

import com.magsav.gui.dialogs.ShareDialogs;
import com.magsav.model.InterventionRow;
import com.magsav.model.Company;
import com.magsav.repo.InterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.CategoryRepository;
import com.magsav.repo.CompanyRepository;
import com.magsav.service.DataChangeEvent;
import com.magsav.service.DataChangeNotificationService;
import com.magsav.service.NavigationService;
import com.magsav.service.ProductServiceStatic;
import com.magsav.service.ImageNormalizationService;
import com.magsav.service.ShareService;
import com.magsav.util.AppLogger;
import com.magsav.db.DB;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

import javafx.beans.value.ChangeListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MainController {
  // Liste des produits (centre)
  @FXML private TableView<ProductRepository.ProductRow> productTable;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdNom, colProdSN, colProdUID, colProdFabricant, colProdSituation;
  @FXML private TextField productSearchField;

  // Éléments du menu principal avec logo société
  @FXML private MenuButton companyMenuButton;
  @FXML private ImageView companyLogoImage;
  @FXML private Label companyNameLabel;

  // Panneau de droite - détails produit
  @FXML private Label lblProdName, lblProdSN, lblProdUID, lblProdManufacturer, lblProdCategory, lblProdSubcategory, lblProdSubSubcategory, lblProdSituation;
  @FXML private Label lblCategoryTitle, lblSubcategoryTitle, lblSubSubcategoryTitle; // Titres des catégories pour les masquer si vides
  @FXML private Label lblQrUID, lblQrSN; // Nouveaux labels pour les légendes QR
  @FXML private ImageView imgProductPhoto, imgManufacturerLogo, imgQr;
  @FXML private TableView<InterventionRow> historyTable;
  @FXML private TableColumn<InterventionRow, Long> hColId;
  @FXML private TableColumn<InterventionRow, String> hColStatut, hColPanne, hColEntree, hColSortie;
  @FXML private Button btnEditProduct;

  // Boutons de partage
  @FXML private Button btnExportProduct, btnPrintProduct, btnEmailProduct, btnShareProduct;

  @FXML private Button btnClose;

  // Services statiques utilisés
  
  // Repositories pour certaines opérations spécifiques
  private final ProductRepository productRepo = new ProductRepository();
  private final InterventionRepository interventionRepo = new InterventionRepository();
  private final CategoryRepository categoryRepo = new CategoryRepository();
  private final ImageNormalizationService imageService = new ImageNormalizationService();
  
  // Service de partage
  private ShareService shareService;
  
  private FilteredList<ProductRepository.ProductRow> filteredProducts;
  private Long currentProductId;

  private boolean isClosed(InterventionRow r) {
    if (r == null) return false;
    String ds = r.dateSortie();
    return ds != null && !ds.trim().isEmpty();
  }

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
    
    // Configuration de la table des produits (centre)
    colProdNom.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nom()));
    colProdSN.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().sn()));
    colProdUID.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().uid()));
    colProdFabricant.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fabricant()));
    colProdSituation.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().situation()));

    // Configuration de la table d'historique (panneau de droite)
    hColId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id()));
    hColStatut.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().statut()));
    hColPanne.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().panne()));
    hColEntree.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().dateEntree()));
    hColSortie.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().dateSortie()));

    // Masquer le bouton Clôturer
    if (btnClose != null) { btnClose.setVisible(false); btnClose.setManaged(false); }

    // Gérer la sélection de produit
    productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      updateProductSelection(newSel);
    });

    // Recherche de produits
    if (productSearchField != null) {
      productSearchField.textProperty().addListener((obs, o, n) -> applyProductFilter());
    }
    
    // S'abonner aux notifications de changement de données pour rafraîchissement automatique
    DataChangeNotificationService.getInstance().subscribe(this::onDataChanged);

    // Styles pour la table des produits
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

    // Styles + double-clic pour la table d'historique du panneau de droite
    historyTable.setRowFactory(tv -> {
      TableRow<InterventionRow> r = new TableRow<>();
      ChangeListener<Object> restyle = (obs, o, n) -> applyRowStyle(r);
      r.itemProperty().addListener(restyle);
      r.selectedProperty().addListener(restyle);
      applyRowStyle(r);
      r.setOnMouseClicked(e -> {
        if (!r.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
          InterventionRow intervention = r.getItem();
          if (intervention != null) {
            com.magsav.util.Views.openInterventionDetail(intervention.id());
          }
        }
      });
      return r;
    });

    onRefresh();
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
      if (btnEditProduct != null) btnEditProduct.setDisable(true);
      
      // Désactiver les boutons de partage
      if (btnExportProduct != null) btnExportProduct.setDisable(true);
      if (btnPrintProduct != null) btnPrintProduct.setDisable(true);
      if (btnEmailProduct != null) btnEmailProduct.setDisable(true);
      if (btnShareProduct != null) btnShareProduct.setDisable(true);
      
      clearRightPanel();
      return;
    }

    currentProductId = product.id();
    if (btnEditProduct != null) btnEditProduct.setDisable(false);
    
    // Activer les boutons de partage
    if (btnExportProduct != null) btnExportProduct.setDisable(false);
    if (btnPrintProduct != null) btnPrintProduct.setDisable(false);
    if (btnEmailProduct != null) btnEmailProduct.setDisable(false);
    if (btnShareProduct != null) btnShareProduct.setDisable(false);
    
    // Récupérer les détails complets du produit pour avoir accès à la photo
    var detailedProductOpt = productRepo.findDetailedById(product.id());
    
    // Mettre à jour les détails du produit dans le panneau de droite
  if (lblProdName != null) lblProdName.setText(nz(product.nom()));
  if (lblProdSN != null) lblProdSN.setText(nz(product.sn()));
  if (lblProdUID != null) lblProdUID.setText(nz(product.uid()));
  
  // Mettre à jour les légendes QR avec typographie unifiée
  if (lblQrUID != null) lblQrUID.setText(nz(product.uid()));
  if (lblQrSN != null) lblQrSN.setText(nz(product.sn()));
  if (lblProdManufacturer != null) {
    String fabricant = product.fabricant();
    if (fabricant == null || fabricant.trim().isEmpty()) {
      lblProdManufacturer.setText("Fabricant non défini");
      lblProdManufacturer.setStyle("-fx-font-weight: normal; -fx-font-size: 10; -fx-text-fill: #999; -fx-text-alignment: center; -fx-alignment: center;");
    } else {
      lblProdManufacturer.setText(fabricant);
      lblProdManufacturer.setStyle("-fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: black; -fx-text-alignment: center; -fx-alignment: center;");
    }
  }
  if (lblProdSituation != null) lblProdSituation.setText(nz(product.situation()));
    
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

    // Charger l'historique des interventions pour ce produit dans le panneau de droite
    List<InterventionRow> interventions = ProductServiceStatic.getProductInterventions(product.id());
    if (historyTable != null) {
      historyTable.setItems(FXCollections.observableArrayList(interventions));
    }

    AppLogger.logUserAction("Produit sélectionné", product.nom(), interventions.size() + " interventions");
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
    if (lblProdName != null) lblProdName.setText("");
    if (lblProdSN != null) lblProdSN.setText("");
    if (lblProdUID != null) lblProdUID.setText("");
    if (lblQrUID != null) lblQrUID.setText("");
    if (lblQrSN != null) lblQrSN.setText("");
    if (lblProdManufacturer != null) {
      lblProdManufacturer.setText("");
      // Rétablir le style par défaut uniforme
      lblProdManufacturer.setStyle("-fx-font-weight: bold; -fx-font-size: 10; -fx-text-fill: black; -fx-text-alignment: center; -fx-alignment: center;");
    }
    updateProductCategories("", "");
    if (lblProdSituation != null) lblProdSituation.setText("");
    if (historyTable != null) historyTable.setItems(FXCollections.observableArrayList());
    
    // Effacer les images
    if (imgProductPhoto != null) imgProductPhoto.setImage(null);
    if (imgManufacturerLogo != null) imgManufacturerLogo.setImage(null);
    if (imgQr != null) imgQr.setImage(null);
  }

  private void loadProductPhoto(String photoFilename) {
    if (imgProductPhoto == null) return;
    if (photoFilename == null || photoFilename.trim().isEmpty()) {
      javafx.application.Platform.runLater(() -> imgProductPhoto.setImage(null));
      return;
    }
    try {
      // Utiliser le service de normalisation pour charger l'image optimisée
      Image image = imageService.loadImageForDisplay(photoFilename, ImageNormalizationService.ImageSize.MEDIUM);
      
      if (image != null) {
        javafx.application.Platform.runLater(() -> {
          imgProductPhoto.setImage(null); // Effacer l'ancienne
          imgProductPhoto.setImage(image); // Charger la nouvelle
        });
      } else {
        // Fallback vers l'ancien système si l'image normalisée n'existe pas
        Path photoPath = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "photos", photoFilename);
        if (Files.exists(photoPath)) {
          javafx.application.Platform.runLater(() -> {
            imgProductPhoto.setImage(null);
            String imageUrl = photoPath.toUri().toString() + "?t=" + System.currentTimeMillis();
            Image fallbackImage = new Image(imageUrl, true);
            imgProductPhoto.setImage(fallbackImage);
          });
        } else {
          javafx.application.Platform.runLater(() -> imgProductPhoto.setImage(null));
        }
      }
    } catch (Exception ex) {
      javafx.application.Platform.runLater(() -> imgProductPhoto.setImage(null));
    }
  }

  private void loadManufacturerLogo(String manufacturerName) {
    if (imgManufacturerLogo == null) return;
    if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
      imgManufacturerLogo.setImage(null);
      return;
    }
    try {
      Path logosDir = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "logos");
      String base = slug(manufacturerName);
      String[] exts = { ".png", ".jpg", ".jpeg", ".svg", ".gif" };
      
      // D'abord essayer avec le slug exact
      for (String ext : exts) {
        Path logoPath = logosDir.resolve(base + ext);
        if (Files.exists(logoPath)) {
          imgManufacturerLogo.setImage(new Image(logoPath.toUri().toString(), true));
          System.out.println("DEBUG: Logo chargé dans MainController: " + logoPath);
          return;
        }
      }
      
      // Si pas trouvé, chercher des fichiers qui contiennent le nom du fabricant
      try {
        java.util.List<Path> matchingLogos = Files.list(logosDir)
            .filter(Files::isRegularFile)
            .filter(path -> {
              String fileName = path.getFileName().toString().toLowerCase();
              String cleanManufacturer = manufacturerName.toLowerCase().replaceAll("[^a-z0-9]", "");
              String cleanFileName = fileName.replaceAll("[^a-z0-9]", "");
              return cleanFileName.contains(cleanManufacturer) || cleanManufacturer.contains(cleanFileName.split("\\.")[0]);
            })
            .collect(java.util.stream.Collectors.toList());
            
        if (!matchingLogos.isEmpty()) {
          Path logoPath = matchingLogos.get(0); // Prendre le premier match
          imgManufacturerLogo.setImage(new Image(logoPath.toUri().toString(), true));
          System.out.println("DEBUG: Logo trouvé par correspondance dans MainController: " + logoPath);
          return;
        }
      } catch (Exception e) {
        System.out.println("DEBUG: Erreur lors de la recherche de logo dans MainController: " + e.getMessage());
      }
      
      System.out.println("DEBUG: Aucun logo trouvé dans MainController pour: " + manufacturerName + " (slug: " + base + ")");
      imgManufacturerLogo.setImage(null);
    } catch (Exception ex) {
      System.out.println("DEBUG: Erreur lors du chargement du logo dans MainController: " + ex.getMessage());
      imgManufacturerLogo.setImage(null);
    }
  }

  private void loadQrCode(String uid) {
    if (imgQr == null) return;
    if (uid == null || uid.trim().isEmpty()) {
      imgQr.setImage(null);
      return;
    }
    try {
      CompletableFuture.runAsync(() -> {
        try {
          Path qrPath = com.magsav.service.QrCodeService.ensureQrPng(uid);
          if (Files.exists(qrPath)) {
            Platform.runLater(() -> {
              imgQr.setImage(new Image(qrPath.toUri().toString(), true));
              System.out.println("DEBUG: QR code chargé dans MainController: " + qrPath);
            });
          } else {
            Platform.runLater(() -> imgQr.setImage(null));
          }
        } catch (Exception e) {
          System.out.println("DEBUG: Erreur génération QR code dans MainController: " + e.getMessage());
          Platform.runLater(() -> imgQr.setImage(null));
        }
      });
    } catch (Exception ex) {
      System.out.println("DEBUG: Erreur lors du chargement du QR code dans MainController: " + ex.getMessage());
      imgQr.setImage(null);
    }
  }

  private static String slug(String s) {
    if (s == null) return "unknown";
    String out = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    return out.replaceAll("^_+|_+$", "");
  }

  @FXML 
  private void onClearProductSearch() { 
    if (productSearchField != null) productSearchField.clear(); 
  }

  private void applyRowStyle(TableRow<InterventionRow> row) {
    if (row.getItem() == null) {
      row.setStyle("");
      return;
    }
    if (!row.isSelected() && isClosed(row.getItem())) {
      row.setStyle("-fx-background-color: #e8f5e8;");
    } else {
      row.setStyle("");
    }
  }

  private String nz(String s) { return s == null ? "" : s; }

  @FXML private void onNewIntervention() { /* TODO: Implémenter création d'intervention */ }

  @FXML
  private void onOpenProductManagement() {
    NavigationService.openProductManagement(); 
  }

  @FXML private void onOpenCategories() {
    NavigationService.openCategories(); 
  }

  @FXML private void onOpenManufacturers() {
    NavigationService.openManufacturers(); 
  }  @FXML private void onOpenSuppliers() { 
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
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erreur");
      alert.setHeaderText("Impossible d'ouvrir les préférences");
      alert.setContentText("Erreur: " + e.getMessage());
      alert.showAndWait();
    }
  }

  @FXML private void onOpenManagementHub() {
    try {
      AppLogger.info("main", "Ouverture de l'interface de gestion centralisée");
      NavigationService.openInNewWindow("/fxml/management_hub.fxml", "Interface de Gestion Centralisée");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de l'interface de gestion", e);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erreur");
      alert.setHeaderText("Impossible d'ouvrir l'interface de gestion");
      alert.setContentText("Erreur: " + e.getMessage());
      alert.showAndWait();
    }
  }

  @FXML private void onOpenPreferences() {
    try {
      AppLogger.info("main", "Ouverture des préférences centralisées");
      NavigationService.openInNewWindow("/fxml/preferences.fxml", "Préférences de l'Application");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture des préférences", e);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erreur");
      alert.setHeaderText("Impossible d'ouvrir les préférences");
      alert.setContentText("Erreur: " + e.getMessage());
      alert.showAndWait();
    }
  }

  @FXML private void onOpenRequestsHub() {
    try {
      AppLogger.info("main", "Ouverture du centre de gestion des demandes");
      NavigationService.openInNewWindow("/fxml/requests_hub.fxml", "Centre de Gestion des Demandes");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture du centre des demandes", e);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erreur");
      alert.setHeaderText("Impossible d'ouvrir le centre des demandes");
      alert.setContentText("Erreur: " + e.getMessage());
      alert.showAndWait();
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
    // Résoudre la hiérarchie à partir du nom stocké dans "category" 
    // (le champ "subcategory" est généralement vide dans la base actuelle)
    CategoryRepository.CategoryHierarchy hierarchy = categoryRepo.resolveCategoryHierarchy(category);
    
    // Catégorie principale
    updateCategoryDisplay(
        hierarchy.mainCategory(),
        lblCategoryTitle, lblProdCategory, 
        "Catégorie:"
    );
    
    // Sous-catégorie
    updateCategoryDisplay(
        hierarchy.subCategory(),
        lblSubcategoryTitle, lblProdSubcategory, 
        "Sous-catégorie:"
    );
    
    // Sous-sous-catégorie
    updateCategoryDisplay(
        hierarchy.subSubCategory(),
        lblSubSubcategoryTitle, lblProdSubSubcategory, 
        "Sous-sous-catégorie:"
    );
  }
  
  /**
   * Méthode utilitaire pour afficher ou masquer une catégorie
   */
  private void updateCategoryDisplay(String categoryValue, Label titleLabel, Label valueLabel, String titleText) {
    boolean hasValue = categoryValue != null && !categoryValue.trim().isEmpty();
    
    if (hasValue) {
      if (valueLabel != null) valueLabel.setText(categoryValue);
      if (titleLabel != null) {
        titleLabel.setText(titleText);
        titleLabel.setVisible(true);
        titleLabel.setManaged(true);
      }
      if (valueLabel != null) {
        valueLabel.setVisible(true);
        valueLabel.setManaged(true);
      }
    } else {
      // Masquer complètement si vide
      if (titleLabel != null) {
        titleLabel.setVisible(false);
        titleLabel.setManaged(false);
      }
      if (valueLabel != null) {
        valueLabel.setVisible(false);
        valueLabel.setManaged(false);
        valueLabel.setText("");
      }
    }
  }

  /**
   * Charge le logo de la société Mag Scène dans le menu principal
   */
  private void loadCompanyLogo() {
    try {
      CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
      Company magScene = companyRepo.findByType(Company.CompanyType.OWN_COMPANY)
              .stream()
              .findFirst()
              .orElse(null);
      
      if (magScene != null) {
        // Mettre à jour le nom de la société
        if (companyNameLabel != null) {
          companyNameLabel.setText(magScene.getName());
        }
        
        // Charger le logo si disponible
        if (companyLogoImage != null && magScene.getLogoPath() != null && !magScene.getLogoPath().isEmpty()) {
          try {
            Image logoImage = new Image("file:" + magScene.getLogoPath());
            companyLogoImage.setImage(logoImage);
            AppLogger.info("Logo de la société chargé: " + magScene.getLogoPath());
          } catch (Exception e) {
            AppLogger.warn("Impossible de charger le logo de la société: " + e.getMessage());
            // Utiliser une image par défaut ou une icône
            setDefaultCompanyIcon();
          }
        } else {
          // Pas de logo défini, utiliser l'icône par défaut
          setDefaultCompanyIcon();
        }
      } else {
        AppLogger.warn("Société Mag Scène non trouvée en base de données");
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
      testStage.initOwner(companyMenuButton.getScene().getWindow());
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
}