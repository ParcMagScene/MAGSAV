package com.magsav.gui;

import com.magsav.model.DossierSAV;
import com.magsav.repo.CategoryRepository;
import com.magsav.repo.DossierSAVRepository;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class InterventionDetailController {
  @FXML private Label lblCode;
  @FXML private Label lblProduit;
  @FXML private Label lblSn;
  @FXML private Label lblProprietaire;
  @FXML private Label lblStatut;
  @FXML private Label lblCategorie;
  @FXML private Label lblSousCategorie;
  @FXML private ComboBox<String> cmbCategorie;
  @FXML private ComboBox<String> cmbSousCategorie;
  @FXML private Label lblEntree;
  @FXML private Label lblSortie;
  @FXML private TextArea txtProbleme;
  @FXML private Button btnEdit;
  @FXML private Button btnSave;
  @FXML private Button btnCancel;
  @FXML private Button btnClose;

  private DossierSAV dossier;
  private CategoryRepository categoryRepo;
  private DossierSAVRepository dossierRepo;

  private java.util.List<com.magsav.model.Category> rootCategories;
  private final java.util.Map<String, Long> categoryNameToId = new java.util.HashMap<>();
  private final java.util.Map<String, Long> subcategoryNameToId = new java.util.HashMap<>();

  public void setData(DossierSAV d, CategoryRepository catRepo, DossierSAVRepository repo) {
    this.dossier = d;
    this.categoryRepo = catRepo;
    this.dossierRepo = repo;
    populate();
    setupCombos();
  }

  private void populate() {
    String code = dossier.getCode();
    String codeOrId =
        code != null && !code.isBlank() ? code.toUpperCase() : ("ID" + dossier.getId());
    lblCode.setText(codeOrId);
    lblProduit.setText(safe(dossier.getProduit()));
    lblSn.setText(safe(dossier.getNumeroSerie()));
    lblProprietaire.setText(safe(dossier.getProprietaire()));
    lblStatut.setText(safe(dossier.getStatut()));
    lblCategorie.setText(resolveCategoryName(dossier.getCategoryId()));
    lblSousCategorie.setText(resolveCategoryName(dossier.getSubcategoryId()));
    lblEntree.setText(dossier.getDateEntree() == null ? "" : dossier.getDateEntree().toString());
    lblSortie.setText(dossier.getDateSortie() == null ? "" : dossier.getDateSortie().toString());
    txtProbleme.setText(safe(dossier.getPanne()));
  }

  private String resolveCategoryName(Long id) {
    if (id == null) {
      return "";
    }
    try {
      var c = categoryRepo.findById(id);
      return c != null ? c.name() : "";
    } catch (Exception e) {
      return "";
    }
  }

  private String safe(String s) {
    return s == null ? "" : s;
  }

  @FXML
  private void handleEdit() {
    // Activer le mode édition: rendre txtProbleme éditable et afficher ComboBox
    txtProbleme.setEditable(true);
    toggleEditControls(true);
    // Pré-sélection des combos si une valeur existe
    if (dossier.getCategoryId() != null) {
      try {
        var c = categoryRepo.findById(dossier.getCategoryId());
        if (c != null) {
          cmbCategorie.getSelectionModel().select(c.name());
          loadSubcategories(c.id());
        }
      } catch (Exception ignored) {
      }
    }
    if (dossier.getSubcategoryId() != null) {
      try {
        var sc = categoryRepo.findById(dossier.getSubcategoryId());
        if (sc != null) {
          cmbSousCategorie.getSelectionModel().select(sc.name());
        }
      } catch (Exception ignored) {
      }
    }
  }

  @FXML
  private void handleSave() {
    String newPanne = txtProbleme.getText() != null ? txtProbleme.getText().trim() : null;
    Long newCatId = null;
    Long newSubId = null;
    String catName =
        cmbCategorie != null ? cmbCategorie.getSelectionModel().getSelectedItem() : null;
    String subName =
        cmbSousCategorie != null ? cmbSousCategorie.getSelectionModel().getSelectedItem() : null;
    if (catName != null) {
      newCatId = categoryNameToId.get(catName);
    }
    if (subName != null) {
      newSubId = subcategoryNameToId.get(subName);
    }

    final Long fCat = newCatId;
    final Long fSub = newSubId;
    final String fPanne = newPanne;
    Task<Void> t =
        new Task<>() {
          @Override
          protected Void call() throws Exception {
            dossierRepo.update(
                new DossierSAV(
                    dossier.getId(),
                    dossier.getCode(),
                    dossier.getProduit(),
                    dossier.getNumeroSerie(),
                    dossier.getProprietaire(),
                    fPanne,
                    dossier.getStatut(),
                    dossier.getDetecteur(),
                    dossier.getDateEntree(),
                    dossier.getDateSortie(),
                    dossier.getCreatedAt(),
                    fCat,
                    fSub));
            return null;
          }

          @Override
          protected void succeeded() {
            Platform.runLater(
                () -> {
                  // Mettre à jour l'état local et UI
                  dossier =
                      new DossierSAV(
                          dossier.getId(),
                          dossier.getCode(),
                          dossier.getProduit(),
                          dossier.getNumeroSerie(),
                          dossier.getProprietaire(),
                          fPanne,
                          dossier.getStatut(),
                          dossier.getDetecteur(),
                          dossier.getDateEntree(),
                          dossier.getDateSortie(),
                          dossier.getCreatedAt(),
                          fCat,
                          fSub);
                  populate();
                  txtProbleme.setEditable(false);
                  toggleEditControls(false);
                  showInfo("Modifications enregistrées", "La fiche a été mise à jour.");
                });
          }

          @Override
          protected void failed() {
            Platform.runLater(() -> showError("Erreur", getException().getMessage()));
          }
        };
    new Thread(t).start();
  }

  @FXML
  private void handleCancel() {
    // Revenir à l'état lecture seule
    txtProbleme.setText(safe(dossier.getPanne()));
    txtProbleme.setEditable(false);
    toggleEditControls(false);
    // Effacer sélection combos
    if (cmbCategorie != null) {
      cmbCategorie.getSelectionModel().clearSelection();
    }
    if (cmbSousCategorie != null) {
      cmbSousCategorie.getSelectionModel().clearSelection();
    }
  }

  @FXML
  private void handleClose() {
    getStage().close();
  }

  private Stage getStage() {
    return (Stage) btnClose.getScene().getWindow();
  }

  private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void setupCombos() {
    if (cmbCategorie == null || cmbSousCategorie == null) {
      return;
    }
    try {
      rootCategories = categoryRepo.findRoots();
      cmbCategorie.getItems().clear();
      categoryNameToId.clear();
      for (var c : rootCategories) {
        cmbCategorie.getItems().add(c.name());
        categoryNameToId.put(c.name(), c.id());
      }
      cmbCategorie
          .valueProperty()
          .addListener(
              (obs, old, val) -> {
                cmbSousCategorie.getItems().clear();
                subcategoryNameToId.clear();
                if (val == null) {
                  return;
                }
                Long id = categoryNameToId.get(val);
                loadSubcategories(id);
              });
    } catch (Exception ignored) {
    }
  }

  private void loadSubcategories(Long parentId) {
    if (parentId == null) {
      return;
    }
    try {
      var children = categoryRepo.findChildren(parentId);
      cmbSousCategorie.getItems().clear();
      subcategoryNameToId.clear();
      for (var sc : children) {
        cmbSousCategorie.getItems().add(sc.name());
        subcategoryNameToId.put(sc.name(), sc.id());
      }
    } catch (Exception ignored) {
    }
  }

  private void toggleEditControls(boolean editing) {
    if (cmbCategorie != null) {
      cmbCategorie.setVisible(editing);
      cmbCategorie.setManaged(editing);
    }
    if (cmbSousCategorie != null) {
      cmbSousCategorie.setVisible(editing);
      cmbSousCategorie.setManaged(editing);
    }
    if (btnSave != null) {
      btnSave.setVisible(editing);
      btnSave.setManaged(editing);
    }
    if (btnCancel != null) {
      btnCancel.setVisible(editing);
      btnCancel.setManaged(editing);
    }
    // Les labels opposés restent visibles mais on peut aussi les masquer si souhaité
  }
}
