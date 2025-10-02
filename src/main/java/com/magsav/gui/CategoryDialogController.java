package com.magsav.gui;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.model.Category;
import com.magsav.repo.CategoryRepository;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CategoryDialogController implements Initializable {
  @FXML private TextField rootNameField;
  @FXML private Button addRootButton;
  @FXML private ComboBox<Category> parentCombo;
  @FXML private TextField childNameField;
  @FXML private Button addChildButton;
  @FXML private TableView<Category> table;
  @FXML private TableColumn<Category, String> colId;
  @FXML private TableColumn<Category, String> colName;
  @FXML private TableColumn<Category, String> colParent;
  @FXML private Button deleteButton;

  private HikariDataSource ds;
  private CategoryRepository repo;
  private ObservableList<Category> categories;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      Config config = new Config();
      Path configPath = Path.of("application.yml");
      if (configPath.toFile().exists()) {
        config = Config.load(configPath);
      }
      String dbUrl = config.get("app.database.url", "jdbc:sqlite:magsav.db");
      if (!dbUrl.startsWith("jdbc:")) {
        dbUrl = "jdbc:sqlite:" + dbUrl;
      }
      ds = DB.init(dbUrl);
      repo = new CategoryRepository(ds);
    } catch (Exception e) {
      showError("Init", e.getMessage());
      return;
    }

    // Colonnes
    colId.setCellValueFactory(
        c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().id())));
    colName.setCellValueFactory(
        c -> new javafx.beans.property.SimpleStringProperty(c.getValue().name()));
    colParent.setCellValueFactory(
        c ->
            new javafx.beans.property.SimpleStringProperty(
                c.getValue().parentId() == null ? "-" : String.valueOf(c.getValue().parentId())));

    // Boutons
    addRootButton.setOnAction(e -> createRoot());
    addChildButton.setOnAction(e -> createChild());
    deleteButton.setOnAction(e -> deleteSelected());

    // Affichage noms dans le combo parent
    parentCombo.setConverter(
        new javafx.util.StringConverter<>() {
          @Override
          public String toString(Category c) {
            return c == null ? "" : c.name();
          }

          @Override
          public Category fromString(String s) {
            return null;
          }
        });
    parentCombo.setButtonCell(
        new ListCell<>() {
          @Override
          protected void updateItem(Category c, boolean empty) {
            super.updateItem(c, empty);
            setText(empty || c == null ? "" : c.name());
          }
        });
    parentCombo.setCellFactory(
        cb ->
            new ListCell<>() {
              @Override
              protected void updateItem(Category c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? "" : c.name());
              }
            });

    // Double-clic sur sous-catégorie dans la table
    table.setRowFactory(
        tv -> {
          TableRow<Category> row = new TableRow<>();
          row.setOnMouseClicked(
              evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                  Category cat = row.getItem();
                  if (cat.parentId() != null) { // c'est une sous-catégorie
                    openAssignProductsDialog(cat);
                  }
                }
              });
          return row;
        });

    // Charger
    refresh();
  }

  private void refresh() {
    try {
      List<Category> all = repo.findAll();
      categories = FXCollections.observableArrayList(all);
      table.setItems(categories);
      parentCombo.setItems(FXCollections.observableArrayList(repo.findRoots()));
    } catch (Exception e) {
      showError("Chargement", e.getMessage());
    }
  }

  private void createRoot() {
    String name = rootNameField.getText();
    if (name == null || name.isBlank()) {
      return;
    }
    try {
      repo.save(new Category(null, name.trim(), null));
      rootNameField.clear();
      refresh();
    } catch (Exception e) {
      showError("Création", e.getMessage());
    }
  }

  private void createChild() {
    Category parent = parentCombo.getValue();
    if (parent == null) {
      showWarn("Validation", "Choisissez un parent");
      return;
    }
    String name = childNameField.getText();
    if (name == null || name.isBlank()) {
      showWarn("Validation", "Nom obligatoire");
      return;
    }
    try {
      repo.save(new Category(null, name.trim(), parent.id()));
      childNameField.clear();
      refresh();
    } catch (Exception e) {
      showError("Création", e.getMessage());
    }
  }

  private void deleteSelected() {
    Category selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    try {
      repo.delete(selected.id());
      refresh();
    } catch (Exception e) {
      showError("Suppression", e.getMessage());
    }
  }

  private void openAssignProductsDialog(Category subcategory) {
    // Boîte simple listant les noms de produits identiques pour affectation
    Dialog<ButtonType> dlg = new Dialog<>();
    dlg.setTitle("Ajouter produits à la sous-catégorie");
    DialogPane pane = new DialogPane();
    pane.getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    VBox box = new VBox(6);
    box.setPrefWidth(420);
    box.getChildren()
        .add(new Label("Sélectionnez les produits à associer à: " + subcategory.name()));
    ListView<String> list = new ListView<>();
    list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    box.getChildren().add(list);
    pane.setContent(box);
    dlg.setDialogPane(pane);
    // Charger suggestions de produits (noms distincts)
    try {
      var ds2 = ds;
      javafx.concurrent.Task<java.util.List<String>> t =
          new javafx.concurrent.Task<>() {
            @Override
            protected java.util.List<String> call() throws Exception {
              try (var c = ds2.getConnection();
                  var ps =
                      c.prepareStatement(
                          "SELECT DISTINCT produit FROM produits WHERE produit IS NOT NULL AND TRIM(produit)<>'' ORDER BY produit")) {
                try (var rs = ps.executeQuery()) {
                  java.util.ArrayList<String> out = new java.util.ArrayList<>();
                  while (rs.next()) {
                    out.add(rs.getString(1));
                  }
                  return out;
                }
              }
            }
          };
      t.setOnSucceeded(e -> list.getItems().setAll(t.getValue()));
      new Thread(t).start();
    } catch (Exception ignore) {
    }

    var res = dlg.showAndWait();
    if (res.isPresent() && res.get() == ButtonType.OK) {
      var selected = list.getSelectionModel().getSelectedItems();
      if (selected == null || selected.isEmpty()) {
        return;
      }
      // Appliquer la sous-catégorie aux interventions correspondantes (même nom de produit)
      try {
        try (var c = ds.getConnection()) {
          try (var ps =
              c.prepareStatement(
                  "UPDATE dossiers_sav SET subcategory_id=? WHERE lower(produit)=lower(?)")) {
            for (String name : selected) {
              ps.setLong(1, subcategory.id());
              ps.setString(2, name);
              ps.addBatch();
            }
            ps.executeBatch();
          }
        }
        refresh();
        showInfo("Mise à jour", "Sous-catégorie appliquée aux produits sélectionnés.");
      } catch (Exception e) {
        showError("Association", e.getMessage());
      }
    }
  }

  private void showError(String t, String m) {
    new Alert(Alert.AlertType.ERROR, t + ": " + m, ButtonType.OK).showAndWait();
  }

  private void showWarn(String t, String m) {
    new Alert(Alert.AlertType.WARNING, t + ": " + m, ButtonType.OK).showAndWait();
  }

  private void showInfo(String t, String m) {
    new Alert(Alert.AlertType.INFORMATION, t + ": " + m, ButtonType.OK).showAndWait();
  }

  public void dispose() {
    if (ds != null) {
      try {
        ds.close();
      } catch (Exception ignored) {
      }
    }
  }
}
