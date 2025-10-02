package com.magsav.gui;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.gui.widgets.ImagePickerController;
import com.magsav.model.Manufacturer;
import com.magsav.repo.ManufacturerRepository;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ManufacturerSelectController implements Initializable {
  @FXML private TextField searchField;
  @FXML private TextField newNameField;
  @FXML private TableView<Manufacturer> table;
  @FXML private TableColumn<Manufacturer, String> colName;
  @FXML private TableColumn<Manufacturer, String> colWebsite;
  @FXML private TableColumn<Manufacturer, String> colEmail;
  @FXML private TableColumn<Manufacturer, String> colPhone;
  @FXML private Button selectButton;
  @FXML private Button cancelButton;

  private HikariDataSource ds;
  private ManufacturerRepository repo;
  private final ObservableList<Manufacturer> data = FXCollections.observableArrayList();
  private Manufacturer selected;

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
      try {
        DB.migrate(ds);
      } catch (Exception ignore) {
      }
      repo = new ManufacturerRepository(ds);
    } catch (Exception e) {
      showError("Init", e.getMessage());
    }

    colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
    colWebsite.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().website())));
    colEmail.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().contactEmail())));
    colPhone.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().contactPhone())));

    table.setItems(data);
    table.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> selected = n);

    // Charger initialement
    handleSearch();
  }

  private String nz(String s) {
    return s == null ? "" : s;
  }

  @FXML
  public void handleSearch() {
    try {
      String q = searchField.getText();
      List<Manufacturer> list = q == null || q.isBlank() ? repo.findAll() : repo.search(q.trim());
      data.setAll(list);
    } catch (Exception e) {
      showError("Recherche", e.getMessage());
    }
  }

  @FXML
  public void handleCreate() {
    String name = newNameField.getText();
    if (name == null || name.isBlank()) {
      showWarn("Validation", "Nom obligatoire");
      return;
    }
    try {
      String trimmed = name.trim();
      repo.save(new Manufacturer(null, trimmed, null, null, null, null));
      newNameField.clear();
      handleSearch();
      // Sélectionner la ligne exacte par nom pour garantir la sélection
      for (Manufacturer it : data) {
        if (it.name().equalsIgnoreCase(trimmed)) {
          table.getSelectionModel().select(it);
          break;
        }
      }
    } catch (Exception e) {
      showError("Création", e.getMessage());
    }
  }

  @FXML
  public void handleSelect() {
    if (selected == null) {
      showWarn("Sélection", "Choisissez un fabricant");
      return;
    }
    closeWith(selected);
  }

  @FXML
  public void handleCancel() {
    closeWith(null);
  }

  private void closeWith(Manufacturer m) {
    this.selected = m;
    Stage st = (Stage) cancelButton.getScene().getWindow();
    st.setUserData(m);
    st.close();
  }

  public Manufacturer getResult() {
    return selected;
  }

  private void showError(String t, String m) {
    new Alert(Alert.AlertType.ERROR, t + ": " + m, ButtonType.OK).showAndWait();
  }

  private void showWarn(String t, String m) {
    new Alert(Alert.AlertType.WARNING, t + ": " + m, ButtonType.OK).showAndWait();
  }

  public void dispose() {
    if (ds != null) {
      try {
        ds.close();
      } catch (Exception ignored) {
      }
    }
  }

  @FXML
  public void handleChooseLogo() {
    try {
      Manufacturer target = selected;
      if (target == null) {
        showWarn("Logo", "Sélectionnez d'abord un fabricant dans la liste");
        return;
      }
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/image-picker.fxml"));
      Parent root = loader.load();
      ImagePickerController ctrl = loader.getController();
      java.nio.file.Path lib = java.nio.file.Path.of("photos", "logos", "manufacturers");
      ctrl.setLibraryDir(lib);
      Stage stage = new Stage();
      stage.setTitle("Choisir un logo fabricant");
      Scene scene = new Scene(root);
      try {
        scene.getStylesheets().addAll(table.getScene().getStylesheets());
      } catch (Exception ignored) {
      }
      stage.setScene(scene);
      stage.initOwner(((Stage) cancelButton.getScene().getWindow()));
      stage.initModality(Modality.WINDOW_MODAL);
      stage.showAndWait();
      Object ud = stage.getUserData();
      if (ud instanceof java.nio.file.Path p) {
        String abs = p.toAbsolutePath().toString();
        Manufacturer updated =
            new Manufacturer(
                target.id(),
                target.name(),
                target.website(),
                target.contactEmail(),
                target.contactPhone(),
                abs);
        repo.save(updated);
        handleSearch();
        // Reselect updated row
        for (Manufacturer it : data) {
          if (it.id().equals(updated.id())) {
            table.getSelectionModel().select(it);
            break;
          }
        }
      }
    } catch (Exception e) {
      showError("Logo fabricant", e.getMessage());
    }
  }
}
