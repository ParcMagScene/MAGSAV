package com.magsav.gui.categories;

import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class CategoriesController implements Initializable {

  private final CategoryRepository repo = new CategoryRepository();
  private final ObservableList<CategoryDisplay> categoryItems = FXCollections.observableArrayList();

  @FXML private TableView<CategoryDisplay> table;
  @FXML private TableColumn<CategoryDisplay, Long> colId;
  @FXML private TableColumn<CategoryDisplay, String> colHierarchie;
  @FXML private TableColumn<CategoryDisplay, String> colNom;
  @FXML private TableColumn<CategoryDisplay, String> colType;
  @FXML private TableColumn<CategoryDisplay, String> colParent;
  @FXML private TextField tfSearch;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    colId.setCellValueFactory(new PropertyValueFactory<>("id"));
    colHierarchie.setCellValueFactory(new PropertyValueFactory<>("hierarchie"));
    colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
    colType.setCellValueFactory(new PropertyValueFactory<>("type"));
    colParent.setCellValueFactory(new PropertyValueFactory<>("parent"));
    
    table.setItems(categoryItems);
    refreshCategories();
  }

  private void refreshCategories() {
    try {
      categoryItems.clear();
      List<Category> categories = repo.findAll();
      
      for (Category cat : categories) {
        if (cat.parentId() == null) {
          String hierarchie = "� " + cat.nom();
          categoryItems.add(new CategoryDisplay(cat.id(), hierarchie, cat.nom(), "Catégorie principale", ""));
          addSubcategoriesHierarchy(categories, cat.id(), "    ", cat.nom(), 1);
        }
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
    }
  }
  
  private void addSubcategoriesHierarchy(List<Category> allCategories, long parentId, String indent, String parentName, int level) {
    for (Category cat : allCategories) {
      if (cat.parentId() != null && cat.parentId() == parentId) {
        String hierarchie = buildHierarchyString(cat.nom(), level);
        String typeLabel = level == 1 ? "Sous-catégorie" : "Sous-catégorie niveau " + level;
        categoryItems.add(new CategoryDisplay(cat.id(), hierarchie, cat.nom(), typeLabel, parentName));
        addSubcategoriesHierarchy(allCategories, cat.id(), indent + "    ", cat.nom(), level + 1);
      }
    }
  }
  
  private String buildHierarchyString(String nom, int level) {
    StringBuilder sb = new StringBuilder();
    
    // Ajouter l'indentation selon le niveau
    for (int i = 0; i < level; i++) {
      if (i == level - 1) {
        sb.append("    ├─ ");
      } else {
        sb.append("    │  ");
      }
    }
    
    // Ajouter l'icône selon le niveau
    String icon = level == 1 ? "📁" : (level == 2 ? "📄" : "🔖");
    sb.append(icon).append(" ").append(nom);
    
    return sb.toString();
  }

  @FXML
  private void onAddCategory() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/categories/category_form.fxml"));
      Parent root = loader.load();
      CategoryFormController controller = loader.getController();
      
      List<Category> allCategories = repo.findAll();
      controller.init(allCategories, null);

      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle("Ajouter une catégorie");
      dialog.getDialogPane().setContent(root);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      
      Optional<ButtonType> result = dialog.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        // Récupérer les données du formulaire et sauvegarder
        String name = controller.nom();
        if (name == null || name.trim().isEmpty()) {
          new Alert(Alert.AlertType.WARNING, "Le nom de la catégorie ne peut pas être vide").showAndWait();
          return;
        }
        
        try {
          repo.insertCategory(name.trim());
          refreshCategories();
          new Alert(Alert.AlertType.INFORMATION, "Catégorie ajoutée avec succès").showAndWait();
        } catch (Exception ex) {
          new Alert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde: " + ex.getMessage()).showAndWait();
        }
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onAddSubcategory() {
    CategoryDisplay selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie parent").showAndWait();
      return;
    }
    
    try {
      Optional<Category> parentOpt = repo.findById(selected.getId());
      if (parentOpt.isEmpty()) {
        new Alert(Alert.AlertType.ERROR, "Catégorie parent introuvable").showAndWait();
        return;
      }
      
      Category parentCategory = parentOpt.get();
      
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/categories/category_form.fxml"));
      Parent root = loader.load();
      CategoryFormController controller = loader.getController();
      
      List<Category> categories = List.of(parentCategory);
      controller.initForSubcategory(categories, parentCategory);

      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle("Ajouter une sous-catégorie à " + parentCategory.nom());
      dialog.getDialogPane().setContent(root);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      
      Optional<ButtonType> result = dialog.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        // Récupérer les données du formulaire et sauvegarder la sous-catégorie
        String name = controller.nom();
        if (name == null || name.trim().isEmpty()) {
          new Alert(Alert.AlertType.WARNING, "Le nom de la sous-catégorie ne peut pas être vide").showAndWait();
          return;
        }
        
        try {
          repo.insertSubcategory(parentCategory.id(), name.trim());
          refreshCategories();
          new Alert(Alert.AlertType.INFORMATION, "Sous-catégorie ajoutée avec succès").showAndWait();
        } catch (Exception ex) {
          new Alert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde: " + ex.getMessage()).showAndWait();
        }
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onEdit() {
    CategoryDisplay selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie à modifier").showAndWait();
      return;
    }

    try {
      Optional<Category> categoryOpt = repo.findById(selected.getId());
      if (categoryOpt.isEmpty()) {
        new Alert(Alert.AlertType.ERROR, "Catégorie introuvable").showAndWait();
        return;
      }
      
      Category selectedCategory = categoryOpt.get();
      
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/categories/category_form.fxml"));
      Parent root = loader.load();
      CategoryFormController controller = loader.getController();
      
      List<Category> allCategories = repo.findAll();
      controller.init(allCategories, selectedCategory);

      Dialog<ButtonType> dialog = new Dialog<>();
      dialog.setTitle("Modifier la catégorie");
      dialog.getDialogPane().setContent(root);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      
      Optional<ButtonType> result = dialog.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        refreshCategories();
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onDelete() {
    CategoryDisplay selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner une catégorie à supprimer").showAndWait();
      return;
    }

    try {
      List<Category> subcategories = repo.findSubcategories(selected.getId());
      if (!subcategories.isEmpty()) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Cette catégorie contient " + subcategories.size() + " sous-catégorie(s)");
        alert.setContentText("Voulez-vous supprimer cette catégorie et toutes ses sous-catégories ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
          return;
        }
      } else {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Voulez-vous vraiment supprimer la catégorie '" + selected.getNom() + "' ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
          return;
        }
      }

      repo.delete(selected.getId());
      refreshCategories();
      new Alert(Alert.AlertType.INFORMATION, "Catégorie supprimée avec succès").showAndWait();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression: " + e.getMessage()).showAndWait();
    }
  }
}
