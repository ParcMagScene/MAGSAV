package com.magsav.gui.hub;

import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
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
import java.util.List;
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
  
  // Onglet Catégories
  @FXML private TreeView<String> categoriesTreeView;
  @FXML private TextField categorySearchField;
  @FXML private Button btnEditCategory, btnDeleteCategory;
  
  // Barre de statut
  @FXML private Label lblStatus;
  
  // Données
  private final ObservableList<ProductRepository.ProductRow> productsData = FXCollections.observableArrayList();
  private final CategoryRepository categoryRepository = new CategoryRepository();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupProductsTable();
    setupCategoriesTreeView();
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
        
        // Rendre les lignes cliquables pour ouvrir la fiche produit
        productsTable.setRowFactory(tv -> {
          TableRow<ProductRepository.ProductRow> row = new TableRow<>();
          row.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !row.isEmpty()) {
              ProductRepository.ProductRow selectedProduct = row.getItem();
              openProductDetail(selectedProduct);
            }
          });
          return row;
        });
      }
    } catch (Exception e) {
      AppLogger.error("Erreur configuration table produits: " + e.getMessage(), e);
    }
  }
  
  private void setupCategoriesTreeView() {
    try {
      if (categoriesTreeView != null) {
        // Configuration de la TreeView
        categoriesTreeView.setCellFactory(tv -> new TreeCell<String>() {
          @Override
          protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
              setText(null);
              setGraphic(null);
            } else {
              setText(item);
              // Ajouter des icônes selon le niveau
              if (getTreeItem() != null) {
                int level = getTreeView().getTreeItemLevel(getTreeItem());
                switch (level) {
                  case 1 -> setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                  case 2 -> setStyle("-fx-text-fill: #34495e; -fx-font-style: italic;");
                  case 3 -> setStyle("-fx-text-fill: #7f8c8d;");
                  default -> setStyle("-fx-text-fill: #2c3e50;");
                }
              }
            }
          }
        });
        
        // Gestion de la sélection des catégories
        categoriesTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
          boolean hasSelection = newSel != null;
          if (btnEditCategory != null) btnEditCategory.setDisable(!hasSelection);
          if (btnDeleteCategory != null) btnDeleteCategory.setDisable(!hasSelection);
        });
        
        // Rendre les catégories cliquables pour ouvrir la fiche catégorie
        categoriesTreeView.setOnMouseClicked(event -> {
          if (event.getClickCount() == 2) {
            TreeItem<String> selectedItem = categoriesTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
              openCategoryDetail(selectedItem.getValue());
            }
          }
        });
        
        // Chargement initial
        refreshCategoriesTree();
      }
      
      // Configuration de la recherche en temps réel
      if (categorySearchField != null) {
        categorySearchField.textProperty().addListener((obs, oldText, newText) -> {
          filterCategoriesTree(newText);
        });
      }
      
    } catch (Exception e) {
      AppLogger.error("Erreur configuration arbre catégories: " + e.getMessage(), e);
    }
  }
  
  private void refreshCategoriesTree() {
    try {
      if (categoriesTreeView == null) {
        AppLogger.warn("TreeView des catégories est null");
        return;
      }
      
      TreeItem<String> root = new TreeItem<>("Catégories");
      root.setExpanded(true);
      
      List<Category> allCategories = categoryRepository.findAll();
      AppLogger.info("Chargement de " + allCategories.size() + " catégories");
      
      // Construire l'arbre hiérarchique
      buildCategoryTree(root, allCategories, null);
      
      categoriesTreeView.setRoot(root);
      categoriesTreeView.setShowRoot(false);
      
      AppLogger.info("Arbre des catégories rafraîchi avec " + root.getChildren().size() + " éléments racine");
      
      // Mettre à jour les statistiques
      if (lblTotalCategories != null) {
        lblTotalCategories.setText(String.valueOf(allCategories.size()));
      }
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement de l'arbre des catégories: " + e.getMessage(), e);
    }
  }
  
  private void buildCategoryTree(TreeItem<String> parent, List<Category> allCategories, Long parentId) {
    // Trouver toutes les catégories ayant le parentId donné
    List<Category> children = allCategories.stream()
        .filter(cat -> {
          if (parentId == null) {
            return cat.parentId() == null;
          } else {
            return parentId.equals(cat.parentId());
          }
        })
        .sorted((a, b) -> a.nom().compareTo(b.nom()))
        .toList();
    
    for (Category category : children) {
      String displayText;
      if (category.parentId() == null) {
        displayText = "📁 " + category.nom();
      } else {
        // Compter le nombre de niveaux pour choisir l'icône
        int level = getLevel(category, allCategories);
        if (level == 1) {
          displayText = "📂 " + category.nom();
        } else {
          displayText = "📄 " + category.nom();
        }
      }
      
      TreeItem<String> item = new TreeItem<>(displayText);
      item.setExpanded(true);
      parent.getChildren().add(item);
      
      // Ajouter récursivement les sous-catégories
      buildCategoryTree(item, allCategories, category.id());
    }
  }
  
  private int getLevel(Category category, List<Category> allCategories) {
    int level = 0;
    Category current = category;
    while (current != null && current.parentId() != null) {
      level++;
      final Long parentId = current.parentId();
      current = allCategories.stream()
          .filter(cat -> cat.id() == parentId)
          .findFirst()
          .orElse(null);
    }
    return level;
  }
  
  private void filterCategoriesTree(String searchText) {
    try {
      if (categoriesTreeView == null) return;
      
      if (searchText == null || searchText.trim().isEmpty()) {
        // Afficher toutes les catégories
        refreshCategoriesTree();
        return;
      }
      
      TreeItem<String> root = new TreeItem<>("Catégories");
      root.setExpanded(true);
      
      List<Category> allCategories = categoryRepository.findAll();
      String searchLower = searchText.toLowerCase().trim();
      
      // Filtrer les catégories qui contiennent le texte de recherche
      List<Category> matchingCategories = allCategories.stream()
          .filter(cat -> cat.nom().toLowerCase().contains(searchLower))
          .sorted((a, b) -> a.nom().compareTo(b.nom()))
          .toList();
      
      // Construire l'arbre avec les résultats filtrés
      buildFilteredCategoryTree(root, allCategories, matchingCategories);
      
      categoriesTreeView.setRoot(root);
      categoriesTreeView.setShowRoot(false);
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors du filtrage des catégories: " + e.getMessage(), e);
    }
  }
  
  private void buildFilteredCategoryTree(TreeItem<String> parent, List<Category> allCategories, List<Category> matchingCategories) {
    for (Category category : matchingCategories) {
      String displayText;
      if (category.parentId() == null) {
        displayText = "📁 " + category.nom();
      } else {
        int level = getLevel(category, allCategories);
        if (level == 1) {
          displayText = "📂 " + category.nom();
        } else {
          displayText = "📄 " + category.nom();
        }
      }
      
      TreeItem<String> item = new TreeItem<>(displayText);
      item.setExpanded(true);
      parent.getChildren().add(item);
    }
  }
  
  private void loadAllData() {
    try {
      // Chargement minimal des données
      productsData.clear();
      refreshCategoriesTree();
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
  
  @FXML private void onEditCategory() { 
    if (categoriesTreeView != null) {
      TreeItem<String> selectedItem = categoriesTreeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        AppLogger.info("Édition catégorie: " + selectedItem.getValue());
      }
    }
  }
  
  @FXML private void onEditManufacturer() { AppLogger.info("Édition fabricant demandée"); }
  
  @FXML private void onDeleteProduct() { AppLogger.info("Suppression produit demandée"); }
  
  @FXML private void onDeleteCategory() { 
    if (categoriesTreeView != null) {
      TreeItem<String> selectedItem = categoriesTreeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        AppLogger.info("Suppression catégorie: " + selectedItem.getValue());
      }
    }
  }
  
  @FXML private void onDeleteManufacturer() { AppLogger.info("Suppression fabricant demandée"); }
  
  // Nouvelles méthodes pour le menu contextuel de la TreeView
  @FXML private void onNewSubcategory() {
    if (categoriesTreeView != null) {
      TreeItem<String> selectedItem = categoriesTreeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        AppLogger.info("Nouvelle sous-catégorie pour: " + selectedItem.getValue());
      }
    }
  }
  
  @FXML private void onViewCategoryProducts() {
    if (categoriesTreeView != null) {
      TreeItem<String> selectedItem = categoriesTreeView.getSelectionModel().getSelectedItem();
      if (selectedItem != null) {
        AppLogger.info("Affichage des produits pour la catégorie: " + selectedItem.getValue());
      }
    }
  }
  
  // Méthode pour ouvrir le détail d'un produit
  private void openProductDetail(ProductRepository.ProductRow product) {
    try {
      AppLogger.info("Ouverture de la fiche produit: " + product.nom() + " (ID: " + product.id() + ")");
      
      // Pour l'instant, on ne fait que logger. Dans une vraie implémentation,
      // on ouvrirait une nouvelle fenêtre avec ProductDetailController
      // ou on naviguerait vers la vue de détail du produit
      updateStatus("Ouverture de la fiche produit: " + product.nom());
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de la fiche produit: " + e.getMessage(), e);
      updateStatus("Erreur lors de l'ouverture de la fiche");
    }
  }
  
  // Méthode pour ouvrir le détail d'une catégorie
  private void openCategoryDetail(String categoryName) {
    try {
      AppLogger.info("Ouverture de la fiche catégorie: " + categoryName);
      
      // Enlever les icônes emoji du nom pour le traitement
      String cleanName = categoryName.replaceAll("^[📁📂📄]\\s*", "");
      
      // Pour l'instant, on ne fait que logger. Dans une vraie implémentation,
      // on ouvrirait une nouvelle fenêtre avec CategoryDetailController
      // ou on naviguerait vers la vue de détail de la catégorie
      updateStatus("Ouverture de la fiche catégorie: " + cleanName);
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de la fiche catégorie: " + e.getMessage(), e);
      updateStatus("Erreur lors de l'ouverture de la fiche");
    }
  }
  
  @FXML private void onClearProductSearch() { 
    if (productSearchField != null) productSearchField.clear(); 
  }
  @FXML private void onClearCategorySearch() { 
    if (categorySearchField != null) {
      categorySearchField.clear();
      refreshCategoriesTree(); // Réafficher toutes les catégories
    }
  }
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