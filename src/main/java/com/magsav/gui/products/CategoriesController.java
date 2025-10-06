package com.magsav.gui.products;

import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class CategoriesController {
  @FXML private TableView<Category> table;
  @FXML private TableColumn<Category, String> colId;
  @FXML private TableColumn<Category, String> colNom;
  @FXML private TableColumn<Category, String> colParent;
  @FXML private TextField tfSearch;

  private final CategoryRepository repo = new CategoryRepository();
  private final ObservableList<Category> master = FXCollections.observableArrayList();
  private FilteredList<Category> filtered;

  @FXML
  private void initialize() {
    colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
    colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nom()));
    colParent.setCellValueFactory(c -> new SimpleStringProperty(parentName(c.getValue().parentId())));

    filtered = new FilteredList<>(master, it -> true);
    SortedList<Category> sorted = new SortedList<>(filtered);
    sorted.comparatorProperty().bind(table.comparatorProperty());
    table.setItems(sorted);

    if (tfSearch != null) tfSearch.textProperty().addListener((o, a, b) -> applyFilter(b));
    onRefresh();
  }

  private String parentName(Long id) {
    if (id == null) return "";
    return repo.findById(id).map(Category::nom).orElse("");
  }

  @FXML
  private void onRefresh() {
    master.setAll(repo.findAll());
    applyFilter(tfSearch == null ? "" : tfSearch.getText());
  }

  @FXML
  private void onAdd() { // bouton "Ajouter…" existant
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/products/category_form.fxml"));
      Parent root = l.load();
      CategoryFormController ctl = l.getController();

      // Pré-sélectionner comme parent la ligne actuellement sélectionnée (facultatif)
      Category selected = table.getSelectionModel().getSelectedItem();
      ctl.init(repo.findAll(), selected);

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle("Ajouter");
      d.getDialogPane().setContent(root);
      d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

      var res = d.showAndWait();
      if (res.isPresent() && res.get() == ButtonType.OK) {
        String nom = ctl.name();
        if (nom.isBlank()) {
          new Alert(Alert.AlertType.WARNING, "Le nom est requis.").showAndWait();
          return;
        }
        Long parentId = ctl.isSubcategory() ? ctl.parentId() : null;
        if (ctl.isSubcategory() && parentId == null) {
          new Alert(Alert.AlertType.WARNING, "Choisissez une catégorie parente.").showAndWait();
          return;
        }
        repo.insert(nom, parentId); // catégorie si parentId==null, sinon sous-catégorie
        onRefresh();
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur formulaire: " + e.getMessage()).showAndWait();
    }
  }

  @FXML
  private void onEdit() {
    Category sel = table.getSelectionModel().getSelectedItem();
    if (sel != null) openForm(sel);
  }

  @FXML
  private void onDelete() {
    Category sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) return;
    var confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Supprimer la catégorie \"" + sel.nom() + "\" ?").showAndWait();
    if (confirm.isPresent() && confirm.get().getButtonData().isDefaultButton()) {
      try {
        if (!repo.delete(sel.id())) {
          new Alert(Alert.AlertType.WARNING, "Rien n’a été supprimé.").showAndWait();
        }
        onRefresh();
      } catch (RuntimeException ex) {
        new Alert(Alert.AlertType.ERROR,
          "Suppression impossible (éléments liés ?): " + ex.getMessage()).showAndWait();
      }
    }
  }

  private void openForm(Category current) {
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/products/category_form.fxml"));
      Parent root = l.load();
      CategoryFormController ctl = l.getController();

      List<Category> choices = repo.findAll();
      ctl.init(choices, current); // prépare les champs (crée = masque Parent)

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle(current == null ? "Ajouter une catégorie" : "Modifier la catégorie");
      d.getDialogPane().setContent(root);
      d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
      Optional<ButtonType> res = d.showAndWait();
      if (res.isPresent() && res.get() == ButtonType.OK) {
        String nom = ctl.nom();
        Long parentId = ctl.parentId();
        if (nom == null || nom.isBlank()) {
          new Alert(Alert.AlertType.WARNING, "Le nom est requis.").showAndWait();
          return;
        }
        if (current == null) {
          long newId = repo.insert(nom, parentId);
          onRefresh();

          // Proposer d'ajouter une sous-catégorie dans la catégorie créée
          Alert ask = new Alert(Alert.AlertType.CONFIRMATION,
              "Ajouter une sous-catégorie à « " + nom + " » maintenant ?",
              ButtonType.YES, ButtonType.NO);
          ask.setHeaderText(null);
          ask.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            Category parent = repo.findById(newId).orElse(new Category(newId, nom, null));
            try { onAddSubcategory(parent); }
            catch (IOException ex) {
              new Alert(Alert.AlertType.ERROR, "Ouverture sous-catégorie: " + ex.getMessage()).showAndWait();
            }
          });
        } else {
          repo.update(new Category(current.id(), nom, parentId));
          onRefresh();
        }
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur formulaire: " + e.getMessage()).showAndWait();
    }
  }

  // Action publique pour bouton/menu: ajouter une sous-catégorie à la sélection
  @FXML
  private void onAddSubcategoryForSelected() {
    Category sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) {
      new Alert(Alert.AlertType.INFORMATION, "Sélectionnez d’abord une catégorie.").showAndWait();
      return;
    }
    try { onAddSubcategory(sel); }
    catch (IOException e) {
      new Alert(Alert.AlertType.ERROR, "Ouverture sous-catégorie: " + e.getMessage()).showAndWait();
    }
  }

  private void applyFilter(String q) {
    String s = q == null ? "" : q.trim().toLowerCase();
    filtered.setPredicate(c -> {
      if (s.isEmpty()) return true;
      return String.valueOf(c.id()).contains(s)
          || c.nom().toLowerCase().contains(s)
          || parentName(c.parentId()).toLowerCase().contains(s);
    });
  }

  // Si vous exposez aussi ces deux actions séparées (facultatif)
  private void onAddCategory() throws IOException {
    FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/products/category_form.fxml"));
    Parent root = l.load();
    CategoryFormController ctl = l.getController();
    ctl.initForCategory();

    Dialog<ButtonType> d = new Dialog<>();
    d.setTitle("Ajouter une catégorie");
    d.getDialogPane().setContent(root);
    d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    if (d.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
      String nom = ctl.name();
      if (nom == null || nom.isBlank()) {
        new Alert(Alert.AlertType.WARNING, "Le nom est requis.").showAndWait();
        return;
      }
      repo.insert(nom, null);
      onRefresh();
    }
  }

  private void onAddSubcategory(Category parent) throws IOException {
    FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/products/category_form.fxml"));
    Parent root = l.load();
    CategoryFormController ctl = l.getController();
    ctl.initForSubcategory(repo.findAll(), parent);

    Dialog<ButtonType> d = new Dialog<>();
    d.setTitle("Ajouter une sous-catégorie");
    d.getDialogPane().setContent(root);
    d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    if (d.showAndWait().filter(b -> b == ButtonType.OK).isPresent()) {
      String nom = ctl.name();
      if (nom == null || nom.isBlank()) {
        new Alert(Alert.AlertType.WARNING, "Le nom est requis.").showAndWait();
        return;
      }
      Long pid = ctl.parent() == null ? (parent == null ? null : parent.id()) : ctl.parent().id();
      repo.insert(nom, pid);
      onRefresh();
    }
  }
}