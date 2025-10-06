package com.magsav.gui.products;

import com.magsav.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

import java.util.List;

public class CategoryFormController {
  @FXML private TextField tfName;
  @FXML private ComboBox<Category> cbParent;
  @FXML private Label lbParent;
  @FXML private RadioButton rbCategory, rbSubcategory;

  @FXML
  private void initialize() {
    // Par défaut: masquer la ligne "Parent"
    setParentRowVisible(false);
    // Si le FXML contient les radios, bascule parent visible quand "Sous-catégorie" est coché
    if (rbSubcategory != null) {
      rbSubcategory.selectedProperty().addListener((o, a, isSub) -> setParentRowVisible(isSub));
    }
  }

  private void setParentRowVisible(boolean visible) {
    if (lbParent != null) { lbParent.setVisible(visible); lbParent.setManaged(visible); }
    if (cbParent != null) { cbParent.setVisible(visible); cbParent.setManaged(visible); }
  }

  // Utilisé par CategoriesController.openForm(...)
  public void init(List<Category> categories, Category current) {
    if (cbParent != null && categories != null) {
      cbParent.setItems(FXCollections.observableArrayList(categories));
    }
    if (current != null) {
      if (tfName != null) tfName.setText(current.nom());
      if (current.parentId() != null && cbParent != null) {
        cbParent.getItems().stream().filter(c -> c.id() == current.parentId()).findFirst()
            .ifPresent(c -> cbParent.getSelectionModel().select(c));
        setParentRowVisible(true);
        if (rbSubcategory != null) rbSubcategory.setSelected(true);
      } else {
        setParentRowVisible(false);
        if (rbCategory != null) rbCategory.setSelected(true);
      }
    } else {
      // Création par défaut: catégorie (pas de parent)
      setParentRowVisible(false);
      if (rbCategory != null) rbCategory.setSelected(true);
    }
  }

  // Compat: appel depuis onAddCategory()
  public void initForCategory() {
    setParentRowVisible(false);
    if (rbCategory != null) rbCategory.setSelected(true);
  }

  // Compat: appel depuis onAddSubcategory(...)
  public void initForSubcategory(List<Category> categories, Category preselect) {
    if (cbParent != null && categories != null) {
      cbParent.setItems(FXCollections.observableArrayList(categories));
      if (preselect != null) cbParent.getSelectionModel().select(preselect);
    }
    setParentRowVisible(true);
    if (rbSubcategory != null) rbSubcategory.setSelected(true);
  }

  // Accesseurs utilisés par le contrôleur liste
  public String name() { return tfName == null ? "" : tfName.getText().trim(); }
  public String nom() { return name(); }
  public Category parent() { return cbParent == null ? null : cbParent.getSelectionModel().getSelectedItem(); }
  public Long parentId() { Category p = parent(); return p == null ? null : p.id(); }
  public boolean isSubcategory() { return rbSubcategory != null && rbSubcategory.isSelected(); }
}