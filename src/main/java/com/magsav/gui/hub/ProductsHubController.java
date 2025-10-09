package com.magsav.gui.hub;

import com.magsav.repo.ProductRepository;
import com.magsav.util.AppLogger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductsHubController implements Initializable {
  
  // Onglet Vue d'ensemble - Statistiques
  @FXML private Label lblTotalProducts;
  @FXML private Label lblTotalCategories;
  @FXML private Label lblTotalManufacturers;
  @FXML private Label lblTotalInterventions;
  
  // Onglet Produits
  @FXML private TextField productSearchField;
  @FXML private TableView<ProductRepository.ProductRow> productsTable;
  @FXML private TableColumn<ProductRepository.ProductRow, Long> colProductId;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProductName, colProductSN, colProductUID, colProductManufacturer, colProductCategory, colProductSituation, colProductStatus;
  @FXML private Button btnEditProduct, btnDeleteProduct;
  
  // Barre de statut
  @FXML private Label lblStatus;
  
  // Données
  private final ObservableList<ProductRepository.ProductRow> productsData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupProductsTable();
    loadAllData();
    updateStatistics();
    updateStatus("Hub produits initialisé");
  }
  
  private void setupProductsTable() {
    try {
      if (colProductId != null) colProductId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id()));
      if (colProductName != null) colProductName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nom()));
      if (colProductSN != null) colProductSN.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().sn()));
      if (colProductUID != null) colProductUID.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().uid()));
      if (colProductManufacturer != null) colProductManufacturer.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().fabricant()));
      if (colProductSituation != null) colProductSituation.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().situation()));
      if (colProductStatus != null) colProductStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper("Actif"));
      
      if (productsTable != null) {
        productsTable.setItems(productsData);
        
        // Gestion de la sélection
        productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
          boolean hasSelection = newSel != null;
          if (btnEditProduct != null) btnEditProduct.setDisable(!hasSelection);
          if (btnDeleteProduct != null) btnDeleteProduct.setDisable(!hasSelection);
        });
      }
    } catch (Exception e) {
      AppLogger.error("Erreur configuration table produits: " + e.getMessage(), e);
    }
  }
  
  private void loadAllData() {
    try {
      // Chargement minimal des données
      productsData.clear();
      updateStatus("Données chargées");
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des données: " + e.getMessage(), e);
      updateStatus("Erreur lors du chargement");
    }
  }
  
  private void updateStatistics() {
    try {
      // Statistiques simples
      if (lblTotalProducts != null) lblTotalProducts.setText(String.valueOf(productsData.size()));
      if (lblTotalCategories != null) lblTotalCategories.setText("0");
      if (lblTotalManufacturers != null) lblTotalManufacturers.setText("0");
      if (lblTotalInterventions != null) lblTotalInterventions.setText("0");
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de la mise à jour des statistiques: " + e.getMessage(), e);
    }
  }
  
  private void updateStatus(String message) {
    if (lblStatus != null) {
      lblStatus.setText(message);
    }
    AppLogger.info("ProductsHub: " + message);
  }
  
  // Actions de l'interface - Méthodes sécurisées
  @FXML private void onNewProduct() { 
    AppLogger.info("Nouveau produit demandé"); 
  }
  
  @FXML private void onNewCategory() { 
    AppLogger.info("Nouvelle catégorie demandée"); 
  }
  
  @FXML private void onNewManufacturer() { 
    AppLogger.info("Nouveau fabricant demandé"); 
  }
  
  @FXML private void onEditProduct() {
    if (productsTable != null) {
      ProductRepository.ProductRow selected = productsTable.getSelectionModel().getSelectedItem();
      if (selected != null) {
        AppLogger.info("Édition produit: " + selected.nom());
      }
    }
  }
  
  @FXML private void onEditCategory() { AppLogger.info("Édition catégorie demandée"); }
  @FXML private void onEditManufacturer() { AppLogger.info("Édition fabricant demandée"); }
  
  @FXML private void onDeleteProduct() { AppLogger.info("Suppression produit demandée"); }
  @FXML private void onDeleteCategory() { AppLogger.info("Suppression catégorie demandée"); }
  @FXML private void onDeleteManufacturer() { AppLogger.info("Suppression fabricant demandée"); }
  
  @FXML private void onClearProductSearch() { 
    if (productSearchField != null) productSearchField.clear(); 
  }
  @FXML private void onClearCategorySearch() { AppLogger.info("Effacement recherche catégorie"); }
  @FXML private void onClearManufacturerSearch() { AppLogger.info("Effacement recherche fabricant"); }
  
  @FXML private void onExportProducts() { AppLogger.info("Export produits demandé"); }
  
  @FXML private void onRefresh() {
    loadAllData();
    updateStatistics();
    updateStatus("Données actualisées");
  }
  
  @FXML private void onClose() {
    if (productsTable != null && productsTable.getScene() != null && productsTable.getScene().getWindow() != null) {
      productsTable.getScene().getWindow().hide();
    }
  }
}