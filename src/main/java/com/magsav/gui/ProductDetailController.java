package com.magsav.gui;

import com.magsav.gui.widgets.ImagePickerController;
import com.magsav.label.LabelService;
import com.magsav.media.AvatarService;
import com.magsav.media.ImageLibraryService;
import com.magsav.media.ManufacturerLogoService;
import com.magsav.model.Manufacturer;
import com.magsav.model.ProductSummary;
import com.magsav.qr.ProductQrService;
import com.magsav.qr.QRCodeService;
import com.magsav.repo.CategoryRepository;
import com.magsav.repo.DossierSAVRepository;
import com.magsav.repo.ManufacturerRepository;
import com.magsav.repo.ProductAdminRepository;
import com.magsav.repo.ProductRepository;
import java.io.File;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductDetailController {
  @FXML private Label lblName;
  @FXML private Label lblCode;
  @FXML private Label lblSn;
  @FXML private Label lblLastIn;
  @FXML private Label lblLastOut;
  @FXML private Label lblCount;
  @FXML private javafx.scene.image.ImageView imgPhoto;
  @FXML private javafx.scene.image.ImageView imgQr;
  @FXML private Button btnDownloadLabel;
  @FXML private Button btnClose;
  @FXML private Label lblManufacturer;
  @FXML private javafx.scene.image.ImageView imgManufacturerLogo;
  @FXML private Label lblCategory;
  @FXML private Label lblSubcategory;
  @FXML private javafx.scene.control.ComboBox<Manufacturer> cbManufacturer;
  @FXML private javafx.scene.control.ComboBox<com.magsav.model.Category> cbCategory;
  @FXML private javafx.scene.control.ComboBox<com.magsav.model.Category> cbSubcategory;
  @FXML private Button btnAddManufacturer;
  @FXML private Button btnAddCategory;
  @FXML private Button btnAddSubcategory;
  @FXML private Button btnEdit;
  @FXML private Button btnValidate;
  @FXML private Button btnCancelEdit;
  @FXML private Button btnChangePhoto;
  @FXML private javafx.scene.layout.Region history; // fx:include root reference

  @FXML
  private ProductHistoryController
      historyController; // Injecté automatiquement: fx:id "history" + "Controller"

  private ProductSummary product;
  private ProductRepository productRepo;
  private javax.sql.DataSource dataSource;
  private ManufacturerRepository manufacturerRepo;
  private ProductAdminRepository productAdminRepo;
  private DossierSAVRepository dossierRepo;
  private CategoryRepository categoryRepo;
  private QRCodeService qrService;
  private ProductQrService productQrService;
  private LabelService labelService;
  private AvatarService avatarService;
  private ManufacturerLogoService manufacturerLogoService;
  private com.magsav.media.ProductPhotoService productPhotoService;
  private boolean editMode;
  private Long initialCategoryId;
  private Long initialSubcategoryId;
  private java.io.File pendingPhotoFile;

  // plus de sentinelles: on utilise des boutons “+” dédiés

  public void setData(
      ProductSummary p,
      ProductRepository repo,
      javax.sql.DataSource ds,
      QRCodeService qr,
      LabelService label) {
    this.product = p;
    this.productRepo = repo;
    this.dataSource = ds;
    this.qrService = qr;
    this.productQrService = new ProductQrService(qr);
    this.labelService = label;
    if (this.avatarService == null) {
      this.avatarService = new AvatarService();
    }
    // Init repos that need DataSource
    try {
      if (this.dataSource != null) {
        this.manufacturerRepo = new ManufacturerRepository(this.dataSource);
        this.manufacturerLogoService = new ManufacturerLogoService(this.dataSource);
        this.productAdminRepo = new ProductAdminRepository(this.dataSource);
        this.dossierRepo = new DossierSAVRepository(this.dataSource);
        this.categoryRepo = new CategoryRepository(this.dataSource);
        this.productPhotoService =
            new com.magsav.media.ProductPhotoService(new ProductRepository(this.dataSource));
      }
    } catch (Exception ignored) {
    }

    lblName.setText(p.getProduit());
    try {
      String code = productRepo.findCodeByNumeroSerie(p.getNumeroSerie());
      lblCode.setText(code != null ? code.toUpperCase() : "");
    } catch (Exception ignore) {
      lblCode.setText("");
    }
    lblSn.setText(p.getNumeroSerie());
    lblLastIn.setText(p.getLastDateEntree() == null ? "" : p.getLastDateEntree().toString());
    lblLastOut.setText(p.getLastDateSortie() == null ? "" : p.getLastDateSortie().toString());
    lblCount.setText(Long.toString(p.getInterventionsCount()));
    try {
      String mname = productRepo.findManufacturerNameByNumeroSerie(p.getNumeroSerie());
      lblManufacturer.setText(mname != null ? mname : "—");
      loadManufacturerLogo(mname);
    } catch (Exception ignore) {
      lblManufacturer.setText("—");
      loadManufacturerLogo(null);
    }

    // Récupérer Catégorie/Sous-catégorie depuis la dernière intervention connue de ce produit
    try {
      java.util.List<com.magsav.model.DossierSAV> hist =
          dossierRepo.findAllByNumeroSerieExact(p.getNumeroSerie());
      if (hist == null || hist.isEmpty()) {
        // Fallback LIKE sur le N° de série (données hétérogènes)
        var byLike = dossierRepo.findByNumeroSerie(p.getNumeroSerie());
        if (byLike != null && !byLike.isEmpty()) {
          hist = byLike;
        }
      }
      if ((hist == null || hist.isEmpty()) && p.getProduit() != null && !p.getProduit().isBlank()) {
        // Fallback final par nom de produit
        try {
          hist =
              dossierRepo.searchInterventions(
                  p.getProduit(), null, null, null, null, null, null, null, null, null);
        } catch (Exception ignored) {
        }
      }
      String catName = "—";
      String subName = "—";
      if (hist != null && !hist.isEmpty()) {
        com.magsav.model.DossierSAV last = hist.get(0); // déjà trié par date desc
        Long cid = last.getCategoryId();
        Long sid = last.getSubcategoryId();
        initialCategoryId = cid;
        initialSubcategoryId = sid;
        if (cid != null) {
          var c = categoryRepo.findById(cid);
          if (c != null && c.name() != null && !c.name().isBlank()) {
            catName = c.name();
          }
        }
        if (sid != null) {
          var sc = categoryRepo.findById(sid);
          if (sc != null && sc.name() != null && !sc.name().isBlank()) {
            subName = sc.name();
          }
        }
      }
      if (lblCategory != null) {
        lblCategory.setText(catName);
      }
      if (lblSubcategory != null) {
        lblSubcategory.setText(subName);
      }
    } catch (Exception ignored) {
    }

    loadPhoto();
    loadQr();

    // Initialiser/recharger l'onglet Historique via le contrôleur injecté
    if (historyController != null) {
      historyController.init(p.getNumeroSerie(), p.getProduit());
      historyController.setContext(p, initialCategoryId, initialSubcategoryId);
      historyController.handleFilter();
    }
  }

  private void loadPhoto() {
    try {
      String path = productRepo.findPhotoPathByNumeroSerie(product.getNumeroSerie());
      if (path != null && !path.isBlank()) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) {
          var img = new javafx.scene.image.Image(f.toURI().toString(), 260, 260, true, true);
          imgPhoto.setImage(img);
          return;
        }
      }
    } catch (Exception ignore) {
    }
    imgPhoto.setImage(null);
  }

  @FXML
  private void handleEnterEditMode() {
    if (editMode) {
      return;
    }
    editMode = true;
    // Afficher widgets d'édition
    if (cbManufacturer != null) {
      cbManufacturer.setManaged(true);
      cbManufacturer.setVisible(true);
    }
    if (cbCategory != null) {
      cbCategory.setManaged(true);
      cbCategory.setVisible(true);
    }
    if (cbSubcategory != null) {
      cbSubcategory.setManaged(true);
      cbSubcategory.setVisible(true);
    }
    if (btnValidate != null) {
      btnValidate.setManaged(true);
      btnValidate.setVisible(true);
    }
    if (btnCancelEdit != null) {
      btnCancelEdit.setManaged(true);
      btnCancelEdit.setVisible(true);
    }
    if (btnChangePhoto != null) {
      btnChangePhoto.setManaged(true);
      btnChangePhoto.setVisible(true);
    }
    if (btnAddManufacturer != null) {
      btnAddManufacturer.setManaged(true);
      btnAddManufacturer.setVisible(true);
    }
    if (btnAddCategory != null) {
      btnAddCategory.setManaged(true);
      btnAddCategory.setVisible(true);
    }
    if (btnAddSubcategory != null) {
      btnAddSubcategory.setManaged(true);
      btnAddSubcategory.setVisible(true);
    }

    // Masquer labels lecture seule
    if (lblManufacturer != null) {
      lblManufacturer.setManaged(false);
      lblManufacturer.setVisible(false);
    }
    if (imgManufacturerLogo != null) {
      imgManufacturerLogo.setManaged(false);
      imgManufacturerLogo.setVisible(false);
    }
    if (lblCategory != null) {
      lblCategory.setManaged(false);
      lblCategory.setVisible(false);
    }
    if (lblSubcategory != null) {
      lblSubcategory.setManaged(false);
      lblSubcategory.setVisible(false);
    }
    if (btnEdit != null) {
      btnEdit.setDisable(true);
    }

    // Charger données
    Platform.runLater(
        () -> {
          try {
            if (manufacturerRepo != null && cbManufacturer != null) {
              var list =
                  javafx.collections.FXCollections.observableArrayList(manufacturerRepo.findAll());
              cbManufacturer.setItems(list);
              // D'abord activer le converter/édition pour que l'affichage utilise le nom
              enableCreateForManufacturer();
              // Pré-sélection par nom label
              String current = lblManufacturer.getText();
              if (current != null && !"—".equals(current)) {
                list.stream()
                    .filter(m -> current.equals(m.name()))
                    .findFirst()
                    .ifPresent(cbManufacturer::setValue);
              }
              // Cellules du menu déroulant + bouton
              cbManufacturer.setCellFactory(
                  v ->
                      new javafx.scene.control.ListCell<>() {
                        @Override
                        protected void updateItem(Manufacturer item, boolean empty) {
                          super.updateItem(item, empty);
                          setText(empty || item == null ? null : item.name());
                        }
                      });
              cbManufacturer.setButtonCell(
                  new javafx.scene.control.ListCell<>() {
                    @Override
                    protected void updateItem(Manufacturer item, boolean empty) {
                      super.updateItem(item, empty);
                      setText(empty || item == null ? null : item.name());
                    }
                  });
            }
            if (categoryRepo != null) {
              var roots =
                  javafx.collections.FXCollections.observableArrayList(categoryRepo.findRoots());
              if (cbCategory != null) {
                cbCategory.setItems(roots);
                // Activer converter avant de positionner la valeur
                enableCreateForRootCategory();
                if (initialCategoryId != null) {
                  roots.stream()
                      .filter(c -> initialCategoryId.equals(c.id()))
                      .findFirst()
                      .ifPresent(cbCategory::setValue);
                }
                cbCategory.setCellFactory(
                    v ->
                        new javafx.scene.control.ListCell<>() {
                          @Override
                          protected void updateItem(com.magsav.model.Category item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.name());
                          }
                        });
                cbCategory.setButtonCell(
                    new javafx.scene.control.ListCell<>() {
                      @Override
                      protected void updateItem(com.magsav.model.Category item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.name());
                      }
                    });
                cbCategory
                    .valueProperty()
                    .addListener(
                        (obs, old, val) -> {
                          try {
                            if (cbSubcategory != null) {
                              javafx.collections.ObservableList<com.magsav.model.Category>
                                  children =
                                      val != null && val.id() != null && val.id() > 0
                                          ? javafx.collections.FXCollections.observableArrayList(
                                              categoryRepo.findChildren(val.id()))
                                          : javafx.collections.FXCollections.observableArrayList();
                              cbSubcategory.setItems(children);
                              cbSubcategory.setValue(null);
                            }
                          } catch (Exception ignored) {
                          }
                        });
              }
              if (cbSubcategory != null && initialCategoryId != null) {
                javafx.collections.ObservableList<com.magsav.model.Category> children =
                    javafx.collections.FXCollections.observableArrayList(
                        categoryRepo.findChildren(initialCategoryId));
                cbSubcategory.setItems(children);
                // Activer converter avant de positionner la valeur
                enableCreateForSubCategory();
                if (initialSubcategoryId != null) {
                  children.stream()
                      .filter(c -> initialSubcategoryId.equals(c.id()))
                      .findFirst()
                      .ifPresent(cbSubcategory::setValue);
                }
                cbSubcategory.setCellFactory(
                    v ->
                        new javafx.scene.control.ListCell<>() {
                          @Override
                          protected void updateItem(com.magsav.model.Category item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? null : item.name());
                          }
                        });
                cbSubcategory.setButtonCell(
                    new javafx.scene.control.ListCell<>() {
                      @Override
                      protected void updateItem(com.magsav.model.Category item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(empty || item == null ? null : item.name());
                      }
                    });
              }
            }
          } catch (Exception ex) {
            showError("Mode édition", ex.getMessage());
          }
        });
  }

  private void enableCreateForManufacturer() {
    try {
      cbManufacturer.setEditable(true);
      cbManufacturer.setConverter(
          new javafx.util.StringConverter<>() {
            @Override
            public String toString(Manufacturer m) {
              return m == null ? "" : m.name();
            }

            @Override
            public Manufacturer fromString(String s) {
              if (s == null || s.isBlank()) {
                return cbManufacturer.getValue();
              }
              return createManufacturerIfNeeded(s.trim());
            }
          });
      // bouton dédié
    } catch (Exception ignored) {
    }
  }

  private void loadQr() {
    if (imgQr == null || product == null) {
      return;
    }
    try {
      String code = null;
      try {
        code = productRepo.findCodeByNumeroSerie(product.getNumeroSerie());
      } catch (Exception ignored) {
      }
      String codeOrSn =
          code != null && !code.isBlank() ? code.toUpperCase() : product.getNumeroSerie();
      byte[] png = productQrService.generateQrPng(codeOrSn, product.getNumeroSerie());
      javafx.scene.image.Image img =
          new javafx.scene.image.Image(new java.io.ByteArrayInputStream(png));
      imgQr.setImage(img);
    } catch (Exception ignored) {
      imgQr.setImage(null);
    }
  }

  private Manufacturer createManufacturerIfNeeded(String name) {
    try {
      if (name == null || name.isBlank()) {
        return null;
      }
      // Vérifier si déjà existant dans la liste (insensible à la casse)
      for (Manufacturer it : cbManufacturer.getItems()) {
        if (it != null && name.equalsIgnoreCase(it.name())) {
          return it;
        }
      }
      // Confirmer création
      var dlg = new TextInputDialog(name);
      dlg.setTitle("Créer un fabricant");
      dlg.setHeaderText(null);
      dlg.setContentText("Nom du fabricant:");
      var res = dlg.showAndWait();
      if (res.isEmpty()) {
        return null;
      }
      String finalName = res.get().trim();
      if (finalName.isBlank()) {
        return null;
      }
      Manufacturer saved =
          manufacturerRepo.save(new Manufacturer(null, finalName, null, null, null, null));
      // Recharger liste
      var list = javafx.collections.FXCollections.observableArrayList(manufacturerRepo.findAll());
      cbManufacturer.setItems(list);
      return saved;
    } catch (Exception ex) {
      showError("Création fabricant", ex.getMessage());
      return null;
    }
  }

  private void enableCreateForRootCategory() {
    try {
      cbCategory.setEditable(true);
      cbCategory.setConverter(
          new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.magsav.model.Category c) {
              return c == null ? "" : c.name();
            }

            @Override
            public com.magsav.model.Category fromString(String s) {
              if (s == null || s.isBlank()) {
                return cbCategory.getValue();
              }
              return createRootCategoryIfNeeded(s.trim());
            }
          });
      // bouton dédié
    } catch (Exception ignored) {
    }
  }

  private com.magsav.model.Category createRootCategoryIfNeeded(String name) {
    try {
      if (name == null || name.isBlank()) {
        return null;
      }
      for (var it : cbCategory.getItems()) {
        if (it != null && it.parentId() == null && name.equalsIgnoreCase(it.name())) {
          return it;
        }
      }
      var dlg = new TextInputDialog(name);
      dlg.setTitle("Créer une catégorie");
      dlg.setHeaderText(null);
      dlg.setContentText("Nom de la catégorie:");
      var res = dlg.showAndWait();
      if (res.isEmpty()) {
        return null;
      }
      String finalName = res.get().trim();
      if (finalName.isBlank()) {
        return null;
      }
      var saved = categoryRepo.save(new com.magsav.model.Category(null, finalName, null));
      // Recharger racines
      var roots = javafx.collections.FXCollections.observableArrayList(categoryRepo.findRoots());
      cbCategory.setItems(roots);
      return saved;
    } catch (Exception ex) {
      showError("Création catégorie", ex.getMessage());
      return null;
    }
  }

  private void enableCreateForSubCategory() {
    try {
      cbSubcategory.setEditable(true);
      cbSubcategory.setConverter(
          new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.magsav.model.Category c) {
              return c == null ? "" : c.name();
            }

            @Override
            public com.magsav.model.Category fromString(String s) {
              if (s == null || s.isBlank()) {
                return cbSubcategory.getValue();
              }
              return createSubCategoryIfNeeded(s.trim());
            }
          });
      // bouton dédié
    } catch (Exception ignored) {
    }
  }

  private com.magsav.model.Category createSubCategoryIfNeeded(String name) {
    try {
      var parent = cbCategory.getValue();
      if (parent == null || parent.id() == null || parent.id() <= 0) {
        showError("Création sous-catégorie", "Veuillez d'abord choisir une catégorie parente.");
        return null;
      }
      if (name == null || name.isBlank()) {
        return null;
      }
      for (var it : cbSubcategory.getItems()) {
        if (it != null
            && it.parentId() != null
            && it.parentId().equals(parent.id())
            && name.equalsIgnoreCase(it.name())) {
          return it;
        }
      }
      var dlg = new TextInputDialog(name);
      dlg.setTitle("Créer une sous-catégorie");
      dlg.setHeaderText(null);
      dlg.setContentText("Nom de la sous-catégorie:");
      var res = dlg.showAndWait();
      if (res.isEmpty()) {
        return null;
      }
      String finalName = res.get().trim();
      if (finalName.isBlank()) {
        return null;
      }
      var saved = categoryRepo.save(new com.magsav.model.Category(null, finalName, parent.id()));
      // Recharger enfants pour le parent courant
      var children =
          javafx.collections.FXCollections.observableArrayList(
              categoryRepo.findChildren(parent.id()));
      cbSubcategory.setItems(children);
      return saved;
    } catch (Exception ex) {
      showError("Création sous-catégorie", ex.getMessage());
      return null;
    }
  }

  @FXML
  private void handleCreateManufacturer() {
    String typed = cbManufacturer.getEditor() != null ? cbManufacturer.getEditor().getText() : "";
    Manufacturer m = createManufacturerIfNeeded(typed);
    if (m != null) {
      cbManufacturer.setValue(m);
    }
  }

  @FXML
  private void handleCreateCategory() {
    String typed = cbCategory.getEditor() != null ? cbCategory.getEditor().getText() : "";
    var c = createRootCategoryIfNeeded(typed);
    if (c != null) {
      cbCategory.setValue(c);
      // Recharger sous-catégories pour ce parent
      try {
        var children =
            javafx.collections.FXCollections.observableArrayList(categoryRepo.findChildren(c.id()));
        cbSubcategory.setItems(children);
        cbSubcategory.setValue(null);
      } catch (Exception ignored) {
      }
    }
  }

  @FXML
  private void handleCreateSubcategory() {
    String typed = cbSubcategory.getEditor() != null ? cbSubcategory.getEditor().getText() : "";
    var sc = createSubCategoryIfNeeded(typed);
    if (sc != null) {
      cbSubcategory.setValue(sc);
    }
  }

  @FXML
  private void handleCancelEdit() {
    if (!editMode) {
      return;
    }
    // Revenir à l'affichage simple
    if (cbManufacturer != null) {
      cbManufacturer.setManaged(false);
      cbManufacturer.setVisible(false);
      cbManufacturer.setValue(null);
    }
    if (cbCategory != null) {
      cbCategory.setManaged(false);
      cbCategory.setVisible(false);
      cbCategory.setValue(null);
    }
    if (cbSubcategory != null) {
      cbSubcategory.setManaged(false);
      cbSubcategory.setVisible(false);
      cbSubcategory.setValue(null);
    }
    if (btnValidate != null) {
      btnValidate.setManaged(false);
      btnValidate.setVisible(false);
    }
    if (btnCancelEdit != null) {
      btnCancelEdit.setManaged(false);
      btnCancelEdit.setVisible(false);
    }
    if (btnChangePhoto != null) {
      btnChangePhoto.setManaged(false);
      btnChangePhoto.setVisible(false);
    }
    if (btnAddManufacturer != null) {
      btnAddManufacturer.setManaged(false);
      btnAddManufacturer.setVisible(false);
    }
    if (btnAddCategory != null) {
      btnAddCategory.setManaged(false);
      btnAddCategory.setVisible(false);
    }
    if (btnAddSubcategory != null) {
      btnAddSubcategory.setManaged(false);
      btnAddSubcategory.setVisible(false);
    }
    if (lblManufacturer != null) {
      lblManufacturer.setManaged(true);
      lblManufacturer.setVisible(true);
    }
    if (imgManufacturerLogo != null) {
      imgManufacturerLogo.setManaged(true);
      imgManufacturerLogo.setVisible(true);
    }
    if (lblCategory != null) {
      lblCategory.setManaged(true);
      lblCategory.setVisible(true);
    }
    if (lblSubcategory != null) {
      lblSubcategory.setManaged(true);
      lblSubcategory.setVisible(true);
    }
    if (btnEdit != null) {
      btnEdit.setDisable(false);
    }
    // Recharger affichage initial
    setData(product, productRepo, dataSource, qrService, labelService);
    editMode = false;
  }

  @FXML
  private void handleChangePhotoEdit() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/image-picker.fxml"));
      Parent root = loader.load();
      ImagePickerController ctrl = loader.getController();
      java.nio.file.Path lib = java.nio.file.Path.of("photos", "products");
      ctrl.setLibraryDir(lib);
      Stage stage = new Stage();
      stage.setTitle("Choisir une photo produit");
      Scene scene = new Scene(root);
      // hériter des styles de la fenêtre parente
      try {
        scene.getStylesheets().addAll(getStage().getScene().getStylesheets());
      } catch (Exception ignored) {
      }
      stage.setScene(scene);
      stage.initOwner(getStage());
      stage.initModality(Modality.WINDOW_MODAL);
      stage.showAndWait();
      Object ud = stage.getUserData();
      if (ud instanceof java.nio.file.Path p) {
        var img = new javafx.scene.image.Image(p.toUri().toString(), 260, 260, true, true);
        imgPhoto.setImage(img);
        pendingPhotoFile = p.toFile();
      }
    } catch (Exception ex) {
      showError("Sélection photo", ex.getMessage());
    }
  }

  @FXML
  private void handlePhotoClicked() {
    // Si en mode édition, permettre le choix de photo via clic sur l'image
    if (editMode) {
      handleChangePhotoEdit();
    }
  }

  @FXML
  private void handleValidateChanges() {
    // Récupérer les valeurs sélectionnées
    Long newManufacturerId = null;
    Long newCategoryId = null;
    Long newSubcategoryId = null;
    if (cbManufacturer != null
        && cbManufacturer.getValue() != null
        && cbManufacturer.getValue().id() != null
        && cbManufacturer.getValue().id() > 0) {
      newManufacturerId = cbManufacturer.getValue().id();
    }
    if (cbCategory != null
        && cbCategory.getValue() != null
        && cbCategory.getValue().id() != null
        && cbCategory.getValue().id() > 0) {
      newCategoryId = cbCategory.getValue().id();
    }
    if (cbSubcategory != null
        && cbSubcategory.getValue() != null
        && cbSubcategory.getValue().id() != null
        && cbSubcategory.getValue().id() > 0) {
      newSubcategoryId = cbSubcategory.getValue().id();
    }

    // Dialogue de validation de portée
    var alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Valider les changements");
    alert.setHeaderText("Appliquer les changements");
    ButtonType onlyThis =
        new ButtonType("Valider uniquement pour ce produit", ButtonBar.ButtonData.APPLY);
    ButtonType sameName =
        new ButtonType("Valider pour les produits du même nom", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancel =
        new ButtonType("Annuler les modifications", ButtonBar.ButtonData.CANCEL_CLOSE);
    alert.getButtonTypes().setAll(onlyThis, sameName, cancel);
    var res = alert.showAndWait();
    if (res.isEmpty() || res.get() == cancel) {
      return; // Annulé

    }
    boolean forSameName = res.get() == sameName;
    try {
      // Appliquer fabricant
      if (newManufacturerId != null) {
        if (forSameName && product.getProduit() != null && !product.getProduit().isBlank()) {
          productAdminRepo.assignManufacturerToProduit(product.getProduit(), newManufacturerId);
        } else {
          productAdminRepo.assignManufacturerToNumeroSerie(
              product.getNumeroSerie(), newManufacturerId);
        }
      }
      // Appliquer catégorie/sous-catégorie: on met à jour la dernière intervention (ou toutes si
      // même nom)
      if (newCategoryId != null || newSubcategoryId != null) {
        var list =
            forSameName && product.getProduit() != null && !product.getProduit().isBlank()
                ? dossierRepo.searchInterventions(
                product.getProduit(), null, null, null, null, null, null, null, null, null)
                : dossierRepo.findAllByNumeroSerieExact(product.getNumeroSerie());
        for (var d : list) {
          com.magsav.model.DossierSAV updated =
              d.withCategories(
                  newCategoryId != null ? newCategoryId : d.getCategoryId(),
                  newSubcategoryId != null ? newSubcategoryId : d.getSubcategoryId());
          dossierRepo.save(updated);
        }
      }
      // Appliquer photo si modifiée (copie et enregistrement DB)
      if (pendingPhotoFile != null) {
        try {
          String ext = ImageLibraryService.normalizeExt(pendingPhotoFile.getName(), "png");
          java.nio.file.Path dest =
              ImageLibraryService.productPhotoDest(product.getNumeroSerie(), ext);
          ImageLibraryService.copyToLibrary(pendingPhotoFile.toPath(), dest);
          String photoAbs = dest.toAbsolutePath().toString();
          if (product.getProduit() != null && !product.getProduit().isBlank() && forSameName) {
            try {
              productRepo.updatePhotoPathByProduit(product.getProduit(), photoAbs);
            } catch (Exception ignore) {
            }
          } else {
            productRepo.updatePhotoPath(product.getNumeroSerie(), photoAbs);
          }
        } catch (Exception ex) {
          showError("Sauvegarde photo", ex.getMessage());
        }
      }
      // Rafraîchir l'aperçu et l'onglet Historique
      setData(product, productRepo, dataSource, qrService, labelService);
      try {
        Object controller = history.getProperties().get("controller");
        if (controller instanceof ProductHistoryController phc) {
          phc.init(product.getNumeroSerie(), product.getProduit());
        }
      } catch (Exception ignored) {
      }
      // Quitter proprement le mode édition pour afficher l'aperçu (labels)
      handleCancelEdit();
      showInfo(
          "Changements enregistrés",
          forSameName
              ? "Modifications appliquées à tous les produits du même nom."
              : "Modifications appliquées à ce produit.");
    } catch (Exception ex) {
      showError("Validation", ex.getMessage());
    } finally {
      editMode = false;
      pendingPhotoFile = null;
    }
  }

  @FXML
  private void handleImportPhoto() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Choisir une photo produit");
    fc.getExtensionFilters()
        .addAll(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
    File file = fc.showOpenDialog(getStage());
    if (file == null) {
      return;
    }
    try {
      if (productPhotoService == null && dataSource != null) {
        productPhotoService =
            new com.magsav.media.ProductPhotoService(new ProductRepository(dataSource));
      }
      if (productPhotoService != null) {
        productPhotoService.importAndAssignPhoto(
            product.getNumeroSerie(), product.getProduit(), file.toPath());
      }
      loadPhoto();
      showInfo(
          "Photo importée", "Photo associée au produit (SN: " + product.getNumeroSerie() + ")");
    } catch (Exception ex) {
      showError("Import photo", ex.getMessage());
    }
  }

  @FXML
  private void handleClose() {
    try {
      // Nettoyer le contrôleur enfant si présent (fermeture du DataSource)
      Object controller = history.getProperties().get("controller");
      if (controller instanceof ProductHistoryController phc) {
        phc.dispose();
      }
    } catch (Exception ignored) {
    }
    getStage().close();
  }

  @FXML
  private void handleAssignManufacturer() {
    try {
      if (manufacturerRepo == null || productAdminRepo == null) {
        showError("Dépendances", "Référentiel de fabricants non initialisé");
        return;
      }
      var loader =
          new javafx.fxml.FXMLLoader(
              getClass().getResource("/fxml/manufacturer-select-dialog.fxml"));
      javafx.scene.Parent root = loader.load();
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Sélection du fabricant");
      javafx.scene.Scene scene = new javafx.scene.Scene(root);
      // Copier les styles de la fenêtre parente (inclut dark.css si actif)
      try {
        javafx.scene.Scene parentScene = getStage().getScene();
        if (parentScene != null) {
          scene.getStylesheets().addAll(parentScene.getStylesheets());
        }
      } catch (Exception ignored) {
      }
      stage.setScene(scene);
      stage.initOwner(getStage());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.showAndWait();
      Object data = stage.getUserData();
      if (!(data instanceof Manufacturer)) {
        return;
      }
      Manufacturer chosen = (Manufacturer) data;
      Long mid = chosen.id();
      if (mid == null) {
        return;
      }
      if (product.getProduit() != null && !product.getProduit().isBlank()) {
        productAdminRepo.assignManufacturerToProduit(product.getProduit(), mid);
      } else {
        productAdminRepo.assignManufacturerToNumeroSerie(product.getNumeroSerie(), mid);
      }
      lblManufacturer.setText(chosen.name());
      loadManufacturerLogo(chosen.name());
      showInfo("Fabricant assigné", "Fabricant '" + chosen.name() + "' associé au produit.");
    } catch (Exception ex) {
      showError("Assigner fabricant", ex.getMessage());
    }
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

  private void loadManufacturerLogo(String name) {
    try {
      if (imgManufacturerLogo == null) {
        return;
      }
      if (name == null || name.isBlank() || manufacturerLogoService == null) {
        imgManufacturerLogo.setImage(null);
        return;
      }
      var res = manufacturerLogoService.resolveLogo(name, 80, 24);
      if (res.pathOrNull() != null) {
        var img =
            new javafx.scene.image.Image(res.pathOrNull().toUri().toString(), 80, 24, true, true);
        imgManufacturerLogo.setImage(img);
      } else if (res.fallbackPngOrNull() != null) {
        var img =
            new javafx.scene.image.Image(new java.io.ByteArrayInputStream(res.fallbackPngOrNull()));
        imgManufacturerLogo.setImage(img);
      } else {
        imgManufacturerLogo.setImage(null);
      }
    } catch (Exception ignored) {
      imgManufacturerLogo.setImage(null);
    }
  }

  @FXML
  private void handleDownloadLabel() {
    String code = lblCode.getText();
    String codeOrSn = code != null && !code.isBlank() ? code : product.getNumeroSerie();
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Enregistrer l'étiquette produit");
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier PDF", "*.pdf"));
    fileChooser.setInitialFileName("etiquette-produit-" + codeOrSn + ".pdf");
    File dest = fileChooser.showSaveDialog(getStage());
    if (dest == null) {
      return;
    }

    try {
      String title =
          String.format(
              "Produit %s%n%s%nSN:%s", codeOrSn, product.getProduit(), product.getNumeroSerie());
      String qrContent = String.format("MAGSAV:PROD:%s:%s", codeOrSn, product.getNumeroSerie());
      var printService = new com.magsav.service.PrintService(labelService, qrService);
      java.nio.file.Path outDir = dest.toPath().getParent();
      var result = printService.generateLabel(title, qrContent, null, outDir);
      java.nio.file.Files.move(
          result.pdf(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      showInfo("Étiquette", "Fichier enregistré: " + dest.getAbsolutePath());
    } catch (Exception ex) {
      showError("Erreur", ex.getMessage());
    }
  }

  // computeInitials/makeInitialsImage extraits vers AvatarService/ManufacturerLogoService

  // DataSource désormais passé explicitement via setData()

  // Setters d'injection pour tests
  public void setAvatarService(AvatarService avatarService) {
    this.avatarService = avatarService;
  }

  public void setManufacturerLogoService(ManufacturerLogoService manufacturerLogoService) {
    this.manufacturerLogoService = manufacturerLogoService;
  }

  public void setProductQrService(ProductQrService productQrService) {
    this.productQrService = productQrService;
  }

  public void setProductPhotoService(com.magsav.media.ProductPhotoService productPhotoService) {
    this.productPhotoService = productPhotoService;
  }

  public void setLabelService(LabelService labelService) {
    this.labelService = labelService;
  }

  public void setQrService(QRCodeService qrService) {
    this.qrService = qrService;
  }

  // Getters de test (accès aux noeuds FXML)
  public Label getLblCode() {
    return lblCode;
  }

  public javafx.scene.image.ImageView getImgQr() {
    return imgQr;
  }

  public javafx.scene.image.ImageView getImgManufacturerLogo() {
    return imgManufacturerLogo;
  }
}
