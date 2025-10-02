package com.magsav.gui;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.model.DossierSAV;
import com.magsav.model.ProductSummary;
import com.magsav.repo.DossierSAVRepository;
import com.zaxxer.hikari.HikariDataSource;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class ProductHistoryController implements Initializable {
  @FXML private TextField searchField;
  @FXML private TableView<DossierSAV> table;
  @FXML private TableColumn<DossierSAV, String> colId;
  @FXML private TableColumn<DossierSAV, String> colCode;
  @FXML private TableColumn<DossierSAV, String> colOwner;
  @FXML private TableColumn<DossierSAV, String> colStatus;
  @FXML private TableColumn<DossierSAV, String> colEntree;
  @FXML private TableColumn<DossierSAV, String> colSortie;
  @FXML private Button openButton;
  @FXML private javafx.scene.layout.HBox emptyBox;
  @FXML private Button btnCreateIntervention;

  private HikariDataSource ds;
  private DossierSAVRepository repo;
  private final ObservableList<DossierSAV> all = FXCollections.observableArrayList();
  private final ObservableList<DossierSAV> filtered = FXCollections.observableArrayList();
  private ProductSummary productSummary;
  private Long defaultCategoryId;
  private Long defaultSubcategoryId;
  private boolean emptyToastShown;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
    colCode.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getCode())));
    colOwner.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getProprietaire())));
    colStatus.setCellValueFactory(c -> new SimpleStringProperty(nz(c.getValue().getStatut())));
    colEntree.setCellValueFactory(
        c ->
            new SimpleStringProperty(
                c.getValue().getDateEntree() == null
                    ? ""
                    : c.getValue().getDateEntree().toString()));
    colSortie.setCellValueFactory(
        c ->
            new SimpleStringProperty(
                c.getValue().getDateSortie() == null
                    ? ""
                    : c.getValue().getDateSortie().toString()));
    table.setItems(filtered);
  }

  public void init(String numeroSerie, String produitName) {
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
      repo = new DossierSAVRepository(ds);
      List<DossierSAV> list;
      if (numeroSerie != null && !numeroSerie.isBlank()) {
        // Essayer exact d'abord (meilleure précision pour un produit donné)
        list = repo.findAllByNumeroSerieExact(numeroSerie);
        // Fallback: si rien, tenter LIKE (cas données anciennes/hétérogènes)
        if (list.isEmpty()) {
          list = repo.findByNumeroSerie(numeroSerie);
        }
      } else {
        list = java.util.Collections.emptyList();
      }
      // Fallback final par nom de produit (LIKE) si rien trouvé par N° de série
      if ((list == null || list.isEmpty()) && produitName != null && !produitName.isBlank()) {
        try {
          list =
              repo.searchInterventions(
                  produitName, null, null, null, null, null, null, null, null, null);
        } catch (Exception ignored) {
        }
      }
      all.setAll(list);
      applyFilter();
      updateEmptyState();
    } catch (Exception e) {
      showError("Chargement", e.getMessage());
    }
  }

  /** Contexte passé par la fiche produit (SN, nom, catégories par défaut). */
  public void setContext(ProductSummary ps, Long categoryId, Long subcategoryId) {
    this.productSummary = ps;
    this.defaultCategoryId = categoryId;
    this.defaultSubcategoryId = subcategoryId;
  }

  @FXML
  public void handleFilter() {
    applyFilter();
  }

  private void applyFilter() {
    String q = searchField.getText();
    if (q == null || q.isBlank()) {
      filtered.setAll(all);
      return;
    }
    String term = q.toLowerCase();
    filtered.setAll(
        all.filtered(
            d ->
                (nz(d.getCode()).toLowerCase().contains(term))
                    || (nz(d.getProprietaire()).toLowerCase().contains(term))
                    || (nz(d.getStatut()).toLowerCase().contains(term))));
    updateEmptyState();
  }

  private void updateEmptyState() {
    boolean isEmpty = filtered.isEmpty();
    if (emptyBox != null) {
      emptyBox.setVisible(isEmpty);
      emptyBox.setManaged(isEmpty);
    }
    if (isEmpty && !emptyToastShown) {
      emptyToastShown = true;
      try {
        showToast("Aucune intervention liée. Cliquez sur ‘Créer une intervention’. ");
      } catch (Exception ignored) {
      }
    }
  }

  @FXML
  public void handleOpen() {
    DossierSAV d = table.getSelectionModel().getSelectedItem();
    if (d == null) {
      showWarn("Sélection", "Choisissez une intervention");
      return;
    }
    try {
      var loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/intervention-detail.fxml"));
      javafx.scene.Parent root = loader.load();
      InterventionDetailController ctrl = loader.getController();
      // Passer le repo pour permettre l'édition de la panne
      ctrl.setData(d, null, repo);
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Fiche intervention");
      stage.setScene(new javafx.scene.Scene(root));
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      // Pas d'owner strict ici pour simplifier; option: exposer la Stage parente depuis la fiche
      // produit
      stage.showAndWait();
    } catch (Exception e) {
      showError("Ouverture", e.getMessage());
    }
  }

  @FXML
  public void handleCreateIntervention() {
    try {
      // Dériver produit/SN à partir du contexte injecté par la fiche produit
      String produit = productSummary != null ? productSummary.getProduit() : null;
      String numeroSerie = productSummary != null ? productSummary.getNumeroSerie() : null;
      if ((produit == null || produit.isBlank() || numeroSerie == null || numeroSerie.isBlank())
          && !all.isEmpty()) {
        var first = all.get(0);
        if (produit == null || produit.isBlank()) {
          produit = first.getProduit();
        }
        if (numeroSerie == null || numeroSerie.isBlank()) {
          numeroSerie = first.getNumeroSerie();
        }
      }
      if (produit == null) {
        produit = "";
      }
      if (numeroSerie == null) {
        numeroSerie = "";
      }
      // Propriétaire par défaut: reprendre le dernier si présent
      String proprietaire =
          !all.isEmpty() && all.get(0).getProprietaire() != null
              ? all.get(0).getProprietaire()
              : "";
      DossierSAV nouveau = DossierSAV.nouveau(produit, numeroSerie, proprietaire, "", "");
      // Statut par défaut: reprendre le dernier statut si présent
      if (!all.isEmpty() && all.get(0).getStatut() != null && !all.get(0).getStatut().isBlank()) {
        nouveau = nouveau.withStatut(all.get(0).getStatut());
      }
      if (defaultCategoryId != null || defaultSubcategoryId != null) {
        nouveau = nouveau.withCategories(defaultCategoryId, defaultSubcategoryId);
      }
      // Persister puis recharger
      if (repo == null) {
        showError("Création", "Référentiel indisponible");
        return;
      }
      DossierSAV saved = repo.save(nouveau);
      // Ouvrir directement la fiche pour compléter
      var loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/intervention-detail.fxml"));
      javafx.scene.Parent root = loader.load();
      InterventionDetailController ctrl = loader.getController();
      ctrl.setData(saved, new com.magsav.repo.CategoryRepository(ds), repo);
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Nouvelle intervention");
      stage.setScene(new javafx.scene.Scene(root));
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.showAndWait();
      // Recharger
      init(numeroSerie, produit);
    } catch (Exception e) {
      showError("Création intervention", e.getMessage());
    }
  }

  private String nz(String s) {
    return s == null ? "" : s;
  }

  private void showError(String t, String m) {
    new Alert(Alert.AlertType.ERROR, t + ": " + m, ButtonType.OK).showAndWait();
  }

  private void showWarn(String t, String m) {
    new Alert(Alert.AlertType.WARNING, t + ": " + m, ButtonType.OK).showAndWait();
  }

  private void showToast(String message) {
    // Notification simple non bloquante via Alert INFORMATION auto-fermante
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(false);
    alert.setOnShown(
        e ->
          // Fermer automatiquement après 2.5s
          new Thread(
                  () -> {
                    try {
                      Thread.sleep(2500);
                    } catch (InterruptedException ignored) {
                    }
                    javafx.application.Platform.runLater(alert::close);
                  })
              .start());
    alert.show();
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
