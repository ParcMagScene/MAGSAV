package com.magsav.gui;

import com.magsav.model.InterventionRow;
import com.magsav.repo.InterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SocieteRepository;
import com.magsav.imports.CsvImporter;
import com.magsav.util.AppLogger;
import com.magsav.util.Views;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.FileChooser;
import javafx.beans.value.ChangeListener;

import java.util.List;

public class MainController {
  // Liste des produits (centre)
  @FXML private TableView<ProductRepository.ProductRow> productTable;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdNom, colProdSN, colProdUID, colProdFabricant, colProdSituation;
  @FXML private TextField productSearchField;

  // Panneau de droite - détails produit
  @FXML private Label lblProdName, lblProdCode, lblProdSN, lblProdUID, lblProdManufacturer, lblProdSituation;
  @FXML private TableView<InterventionRow> historyTable;
  @FXML private TableColumn<InterventionRow, Long> hColId;
  @FXML private TableColumn<InterventionRow, String> hColStatut, hColPanne, hColEntree, hColSortie;
  @FXML private Button btnEditProduct;

  @FXML private Button btnClose;

  private final InterventionRepository interRepo = new InterventionRepository();
  private final ProductRepository productRepo = new ProductRepository();
  private final SocieteRepository societeRepo = new SocieteRepository();
  private FilteredList<ProductRepository.ProductRow> filteredProducts;

  private Long currentProductId;

  private boolean isClosed(InterventionRow r) {
    if (r == null) return false;
    String ds = r.dateSortie();
    return ds != null && !ds.trim().isEmpty();
  }

  @FXML
  private void initialize() {
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

    // Styles pour la table des produits
    productTable.setRowFactory(tv -> {
      TableRow<ProductRepository.ProductRow> r = new TableRow<>();
      r.setOnMouseClicked(e -> {
        if (!r.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
          var product = r.getItem();
          Views.openProductSheet(product.id());
        }
      });
      return r;
    });

    // Styles pour la table d'historique du panneau de droite
    historyTable.setRowFactory(tv -> {
      TableRow<InterventionRow> r = new TableRow<>();
      ChangeListener<Object> restyle = (obs, o, n) -> applyRowStyle(r);
      r.itemProperty().addListener(restyle);
      r.selectedProperty().addListener(restyle);
      applyRowStyle(r);
      return r;
    });

    onRefresh();
  }

  private void updateProductSelection(ProductRepository.ProductRow product) {
    if (product == null) {
      currentProductId = null;
      if (btnEditProduct != null) btnEditProduct.setDisable(true);
      clearRightPanel();
      return;
    }

    currentProductId = product.id();
    if (btnEditProduct != null) btnEditProduct.setDisable(false);
    
    // Mettre à jour les détails du produit dans le panneau de droite
    if (lblProdName != null) lblProdName.setText(nz(product.nom()));
    if (lblProdCode != null) lblProdCode.setText(nz(product.code()));
    if (lblProdSN != null) lblProdSN.setText(nz(product.sn()));
    if (lblProdUID != null) lblProdUID.setText("");  // Pas dans le modèle simplifié
    if (lblProdManufacturer != null) lblProdManufacturer.setText(nz(product.fabricant()));
    if (lblProdSituation != null) lblProdSituation.setText(nz(product.situation()));

    // Charger l'historique des interventions pour ce produit dans le panneau de droite
    List<InterventionRow> interventions = interRepo.findByProductId(product.id());
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
        (p.code() != null && p.code().toLowerCase().contains(lower)) ||
        (p.fabricant() != null && p.fabricant().toLowerCase().contains(lower))
      );
    }
  }

  @FXML
  private void onRefresh() {
    AppLogger.debug("Chargement des produits...");
    
    // Charger tous les produits
    List<ProductRepository.ProductRow> products = productRepo.findAllProductsWithUID();
    AppLogger.debug("{} produits chargés", products.size());

    filteredProducts = new FilteredList<>(FXCollections.observableArrayList(products), p -> true);
    SortedList<ProductRepository.ProductRow> sortedProducts = new SortedList<>(filteredProducts);
    sortedProducts.comparatorProperty().bind(productTable.comparatorProperty());
    productTable.setItems(sortedProducts);
    
    // Vider le panneau de droite jusqu'à ce qu'un produit soit sélectionné
    clearRightPanel();
  }

  private void clearRightPanel() {
    if (lblProdName != null) lblProdName.setText("");
    if (lblProdCode != null) lblProdCode.setText("");
    if (lblProdSN != null) lblProdSN.setText("");
    if (lblProdUID != null) lblProdUID.setText("");
    if (lblProdManufacturer != null) lblProdManufacturer.setText("");
    if (lblProdSituation != null) lblProdSituation.setText("");
    if (historyTable != null) historyTable.setItems(FXCollections.observableArrayList());
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
  private void onImportCsv() {
    var importer = new CsvImporter(productRepo, interRepo, societeRepo);
    
    var helpDialog = new Alert(Alert.AlertType.INFORMATION);
    helpDialog.setTitle("Import CSV - Format attendu");
    helpDialog.setHeaderText("Format du fichier CSV pour l'import");
    helpDialog.setContentText(
        "Le fichier CSV doit contenir les colonnes suivantes :\n" +
        "- Nom du produit\n" +
        "- Code produit (optionnel)\n" +
        "- Numéro de série\n" +
        "- Fabricant\n" +
        "- Statut de l'intervention\n" +
        "- Description de la panne\n" +
        "- Date d'entrée\n" +
        "- Date de sortie (optionnel)\n\n" +
        "Séparateurs supportés : virgule (,) ou point-virgule (;)\n" +
        "Première ligne = en-têtes de colonnes"
    );
    
    var owner = productTable != null && productTable.getScene() != null ? productTable.getScene().getWindow() : null;
    helpDialog.initOwner(owner);
    helpDialog.showAndWait();
    
    var chooser = new FileChooser();
    chooser.setTitle("Importer un fichier CSV");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
    var file = chooser.showOpenDialog(owner);
    if (file == null) return;

    try {
      var result = importer.importFile(file.toPath(), false);
      String msg = "Import terminé :\n"
          + "Produits créés : " + result.products() + "\n"
          + "Interventions créées : " + result.interventions() + "\n"
          + "Lignes avec erreurs : " + result.errors().size();
      
      if (!result.errors().isEmpty()) {
        msg += "\n\nErreurs :\n" + String.join("\n", result.errors().subList(0, Math.min(5, result.errors().size())));
        if (result.errors().size() > 5) msg += "\n... (" + (result.errors().size() - 5) + " autres erreurs)";
      }
      
      new Alert(result.errors().isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING, msg).showAndWait();
      onRefresh(); // Actualiser la liste
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Échec import CSV: " + e.getMessage()).showAndWait();
      e.printStackTrace();
    }
  }

  @FXML
  private void onImportMedia() {
    // Choisir le type de média
    var typeChoice = new Alert(Alert.AlertType.CONFIRMATION);
    typeChoice.setTitle("Type de média");
    typeChoice.setHeaderText("Quel type de fichier souhaitez-vous importer ?");
    typeChoice.setContentText("Choisissez le type de média à importer:");
    
    var photosButton = new ButtonType("Photos");
    var logosButton = new ButtonType("Logos");
    var cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
    
    typeChoice.getButtonTypes().setAll(photosButton, logosButton, cancelButton);
    
    var owner = productTable != null && productTable.getScene() != null ? productTable.getScene().getWindow() : null;
    var typeResult = typeChoice.showAndWait();
    
    if (typeResult.isEmpty() || typeResult.get() == cancelButton) return;
    
    var mediaType = typeResult.get() == photosButton ? 
        com.magsav.imports.MediaImporter.MediaType.PHOTOS : 
        com.magsav.imports.MediaImporter.MediaType.LOGOS;
    
    // Choisir les fichiers selon le type
    var chooser = new FileChooser();
    chooser.setTitle("Importer des " + mediaType.getFolderName());
    
    if (mediaType == com.magsav.imports.MediaImporter.MediaType.PHOTOS) {
      chooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Images Photos", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp", "*.tiff"),
          new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
    } else {
      chooser.getExtensionFilters().addAll(
          new FileChooser.ExtensionFilter("Images Logos", "*.png", "*.svg", "*.jpg", "*.jpeg", "*.gif", "*.pdf"),
          new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
    }
    
    var files = chooser.showOpenMultipleDialog(owner);
    if (files == null || files.isEmpty()) return;

    try {
      var importer = new com.magsav.imports.MediaImporter();
      var filePaths = files.stream().map(java.io.File::toPath).toList();
      var result = importer.importFiles(filePaths, mediaType);
      
      String msg = "Fichiers traités: " + result.filesProcessed() + "\n"
          + "Fichiers importés: " + result.filesImported() + "\n"
          + "Type: " + mediaType.getFolderName() + "\n"
          + "Répertoire: " + result.targetDirectory();
      
      if (!result.errors().isEmpty()) {
        msg += "\n\nErreurs:\n" + String.join("\n", result.errors());
      }
      
      new Alert(result.errors().isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING, msg).showAndWait();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Échec import médias: " + e.getMessage()).showAndWait();
      e.printStackTrace();
    }
  }

  // Méthodes de navigation vers d'autres fenêtres
  @FXML private void onOpenCategories() { 
    Views.openInNewWindow("/fxml/categories.fxml", "Gestion des catégories"); 
  }
  
  @FXML private void onOpenManufacturers() { 
    Views.openInNewWindow("/fxml/manufacturers.fxml", "Gestion des fabricants"); 
  }
  
  @FXML private void onOpenSuppliers() { 
    Views.openInNewWindow("/fxml/suppliers.fxml", "Gestion des fournisseurs"); 
  }
  
  @FXML private void onOpenExternalSav() { 
    Views.openInNewWindow("/fxml/external_sav.fxml", "SAV externes"); 
  }
  
  @FXML private void onOpenClients() { 
    Views.openInNewWindow("/fxml/clients.fxml", "Gestion des clients"); 
  }
  
  @FXML private void onOpenPartRequests() { 
    Views.openInNewWindow("/fxml/requests_parts.fxml", "Demandes de pièces"); 
  }
  
  @FXML private void onOpenEquipmentRequests() { 
    Views.openInNewWindow("/fxml/requests_equipment.fxml", "Demandes de matériel"); 
  }

  @FXML
  private void onEditProduct() {
    if (currentProductId != null && currentProductId > 0) {
      Views.openProductSheet(currentProductId);
    }
  }
}