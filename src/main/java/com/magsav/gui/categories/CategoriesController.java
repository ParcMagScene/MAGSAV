package com.magsav.gui.categories;

import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Window;

import java.util.Optional;

public class CategoriesController {

  private final CategoryRepository repo = new CategoryRepository();

  @FXML
  private void onAddCategory() {
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/categories/category_form.fxml"));
      Parent root = l.load();
      CategoryFormController ctl = l.getController();
      ctl.initForCategory();

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle("Ajouter une catégorie");
      d.getDialogPane().setContent(root);
      d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      Optional<ButtonType> res = d.showAndWait();
      if (res.isEmpty() || res.get() != ButtonType.OK) return;

      String name = ctl.name();
      if (name.length() < 2) { new Alert(Alert.AlertType.WARNING, "Nom requis (≥ 2 caractères)").showAndWait(); return; }

      long newCatId = repo.insertCategory(name);

      // Proposer d'ajouter une sous-catégorie
      Alert ask = new Alert(Alert.AlertType.CONFIRMATION, "Créer une sous-catégorie pour « " + name + " » maintenant ?", ButtonType.YES, ButtonType.NO);
      ask.setHeaderText(null);
      ask.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> onAddSubcategoryPrefilled(newCatId));
      // TODO: rafraîchir la liste
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onAddSubcategory() {
    onAddSubcategoryPrefilled(-1);
  }

  private void onAddSubcategoryPrefilled(long parentId) {
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/categories/category_form.fxml"));
      Parent root = l.load();
      CategoryFormController ctl = l.getController();
      var cats = repo.findAllCategories();
      Category preselect = cats.stream().filter(c -> c.id() == parentId).findFirst().orElse(null);
      ctl.initForSubcategory(cats, preselect);

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle("Ajouter une sous-catégorie");
      d.getDialogPane().setContent(root);
      d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      Optional<ButtonType> res = d.showAndWait();
      if (res.isEmpty() || res.get() != ButtonType.OK) return;

      String name = ctl.name();
      if (name.length() < 2) { new Alert(Alert.AlertType.WARNING, "Nom requis (≥ 2 caractères)").showAndWait(); return; }
      Category parent = ctl.parent();
      if (parent == null) { new Alert(Alert.AlertType.WARNING, "Sélectionner une catégorie parente").showAndWait(); return; }

      repo.insertSubcategory(parent.id(), name);
      // TODO: rafraîchir la liste
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage()).showAndWait();
    }
  }
}