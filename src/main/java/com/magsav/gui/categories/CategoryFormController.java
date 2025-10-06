package com.magsav.gui.categories;

import com.magsav.model.Category;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

public class CategoryFormController {

  @FXML private TextField tfName;
  @FXML private ComboBox<Category> cbParent;
  @FXML private Label lbParent;

  public enum Mode { CATEGORY, SUBCATEGORY }
  private Mode mode = Mode.CATEGORY;

  @FXML
  private void initialize() {
    // au démarrage, masquer la ligne parent
    setParentRowVisible(false);
  }

  private void setParentRowVisible(boolean visible) {
    lbParent.setVisible(visible);
    lbParent.setManaged(visible);
    cbParent.setVisible(visible);
    cbParent.setManaged(visible);
  }

  public void initForCategory() {
    mode = Mode.CATEGORY;
    setParentRowVisible(false);
    tfName.requestFocus();
  }

  public void initForSubcategory(List<Category> categories, Category preselect) {
    mode = Mode.SUBCATEGORY;
    setParentRowVisible(true);
    cbParent.setItems(FXCollections.observableArrayList(categories));
    if (preselect != null) cbParent.getSelectionModel().select(preselect);
    tfName.requestFocus();
  }

  public Mode mode() { return mode; }
  public String name() { return tfName == null ? "" : tfName.getText().trim(); }
  public Category parent() { return cbParent == null ? null : cbParent.getSelectionModel().getSelectedItem(); }
}