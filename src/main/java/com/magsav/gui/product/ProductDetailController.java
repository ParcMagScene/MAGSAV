package com.magsav.gui.product;

import com.magsav.repo.ProductRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.nio.file.*;

public class ProductDetailController {

  @FXML private Label lblName, lblCode, lblSN, lblUID, lblSituation, lblOwner;
  @FXML private ImageView imgPhoto, imgLogo, imgQr;

  @FXML private TableView<Object> historyTable;
  @FXML private TableColumn<Object, String> colId, colStatut, colPanne;

  @FXML private ComboBox<String> cbManufacturer;
  @FXML private ComboBox<String> cbCategory, cbSubcategory;
  @FXML private TextField tfNewSubcategory;

  private final ProductRepository prodRepo = new ProductRepository();
  private long productId;

  @FXML
  private void initialize() {
    if (cbManufacturer != null) cbManufacturer.getItems().setAll(prodRepo.listFabricants());
    if (cbCategory != null) { cbCategory.setEditable(true); cbCategory.getItems().setAll(prodRepo.listDistinctCategories()); }
    if (cbSubcategory != null) cbSubcategory.setEditable(true);
  }

  public void setProductId(long id) { this.productId = id; load(); }

  private void load() {
    var opt = prodRepo.findDetailedById(productId);
    if (opt.isEmpty()) return;
    var p = opt.get();
    if (lblName != null) lblName.setText(nz(p.nom()));
    if (lblCode != null) lblCode.setText(nz(p.code()));
    if (lblSN != null) lblSN.setText(nz(p.sn()));
    if (lblUID != null) lblUID.setText(nz(p.uid()));
    if (lblSituation != null) lblSituation.setText(nz(p.situation()));

    // Charger la photo du produit
    loadProductPhoto(p.photo());
    
    // Charger le logo du fabricant
    loadManufacturerLogo(p.fabricant());

    if (cbManufacturer != null) {
      String manu = p.fabricant();
      if (manu != null && !manu.isBlank() && !cbManufacturer.getItems().contains(manu)) cbManufacturer.getItems().add(manu);
      cbManufacturer.setValue(manu);
    }

    if (cbCategory != null) {
      cbCategory.getItems().setAll(prodRepo.listDistinctCategories());
      cbCategory.setValue(p.category());
    }
    if (cbSubcategory != null) {
      String cat = p.category();
      cbSubcategory.getItems().setAll(prodRepo.listDistinctSubcategories(cat));
      cbSubcategory.setValue(p.subcategory());
    }
    if (historyTable != null) historyTable.setItems(FXCollections.observableArrayList());
  }

  private void loadProductPhoto(String photoFilename) {
    if (imgPhoto == null) return;
    if (photoFilename == null || photoFilename.trim().isEmpty()) {
      imgPhoto.setImage(null);
      return;
    }
    try {
      Path photoPath = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "photos", photoFilename);
      if (Files.exists(photoPath)) {
        imgPhoto.setImage(new Image(photoPath.toUri().toString(), true));
      } else {
        imgPhoto.setImage(null);
      }
    } catch (Exception ex) {
      imgPhoto.setImage(null);
    }
  }

  private void loadManufacturerLogo(String manufacturerName) {
    if (imgLogo == null) return;
    if (manufacturerName == null || manufacturerName.trim().isEmpty()) {
      imgLogo.setImage(null);
      return;
    }
    try {
      Path logosDir = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "logos");
      String base = slug(manufacturerName);
      String[] exts = { ".png", ".jpg", ".jpeg", ".svg" };
      for (String ext : exts) {
        Path logoPath = logosDir.resolve(base + ext);
        if (Files.exists(logoPath)) {
          imgLogo.setImage(new Image(logoPath.toUri().toString(), true));
          return;
        }
      }
      imgLogo.setImage(null);
    } catch (Exception ex) {
      imgLogo.setImage(null);
    }
  }

  private static String slug(String s) {
    if (s == null) return "unknown";
    String out = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    return out.replaceAll("^_+|_+$", "");
  }

  @FXML
  private void onChangeManufacturer() {
    if (productId <= 0 || cbManufacturer == null) return;
    String selected = cbManufacturer.getValue();
    String trimmed = selected == null ? null : selected.trim();
    
    if (confirmPropagateChanges("fabricant", trimmed)) {
      prodRepo.updateFabricantForSameNameByProduct(productId, trimmed);
      load();
    }
  }

  @FXML
  private void onChangeCategory() {
    if (productId <= 0 || cbCategory == null) return;
    String selectedCategory = cbCategory.getValue();
    if (cbSubcategory != null) {
      cbSubcategory.getItems().setAll(prodRepo.listDistinctSubcategories(selectedCategory));
      cbSubcategory.setValue(null); // Reset subcategory when category changes
    }
    
    String trimmedCategory = selectedCategory == null ? null : selectedCategory.trim();
    if (confirmPropagateChanges("catégorie", trimmedCategory)) {
      prodRepo.updateCategoryNamesForSameNameByProduct(productId, trimmedCategory, null);
      load();
    }
  }

  @FXML
  private void onChangeSubcategory() {
    if (productId <= 0 || cbCategory == null || cbSubcategory == null) return;
    String selectedCategory = cbCategory.getValue();
    String selectedSubcategory = cbSubcategory.getValue();
    
    String trimmedCategory = selectedCategory == null ? null : selectedCategory.trim();
    String trimmedSubcategory = selectedSubcategory == null ? null : selectedSubcategory.trim();
    
    if (confirmPropagateChanges("sous-catégorie", trimmedSubcategory)) {
      prodRepo.updateCategoryNamesForSameNameByProduct(productId, trimmedCategory, trimmedSubcategory);
      load();
    }
  }

  private boolean confirmPropagateChanges(String fieldName, String newValue) {
    if (productId <= 0) return false;
    
    var productOpt = prodRepo.findDetailedById(productId);
    if (productOpt.isEmpty()) return false;
    
    String productName = productOpt.get().nom();
    if (productName == null || productName.trim().isEmpty()) return false;
    
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Appliquer les changements");
    confirm.setHeaderText("Propagation des modifications");
    confirm.setContentText(String.format(
        "Voulez-vous appliquer le changement de %s (\"%s\") à tous les produits nommés \"%s\" ?",
        fieldName, newValue == null ? "(vide)" : newValue, productName
    ));
    
    var result = confirm.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  @FXML
  private void onChoosePhoto() {
    if (productId <= 0 || lblName == null || lblName.getScene() == null) return;
    ButtonType choisir = new ButtonType("Depuis dossier photos", ButtonBar.ButtonData.LEFT);
    ButtonType importer = new ButtonType("Importer…", ButtonBar.ButtonData.RIGHT);
    ButtonType annuler = ButtonType.CANCEL;
    Alert a = new Alert(Alert.AlertType.NONE, "", choisir, importer, annuler);
    a.setTitle("Photo du produit");
    a.setHeaderText("Choisir la source de la photo");
    var result = a.showAndWait().orElse(annuler);
    if (result == choisir) chooseFromLibrary();
    else if (result == importer) importPhoto();
  }

  private Path photosDir() throws Exception {
    Path dir = Paths.get(System.getProperty("user.home"), "MAGSAV", "medias", "photos");
    Files.createDirectories(dir);
    return dir;
  }

  private void chooseFromLibrary() {
    try {
      Path dir = photosDir();
      FileChooser fc = new FileChooser();
      fc.setTitle("Choisir dans le dossier photos");
      fc.setInitialDirectory(dir.toFile());
      fc.getExtensionFilters().setAll(
          new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
          new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
      );
      var file = fc.showOpenDialog(lblName.getScene().getWindow());
      if (file == null) return;
      Path p = file.toPath().toAbsolutePath().normalize();
      if (!p.startsWith(dir.toAbsolutePath().normalize())) {
        new Alert(Alert.AlertType.WARNING, "Le fichier n'est pas dans le dossier photos. Utilisez 'Importer…'").showAndWait();
        return;
      }
      String filename = dir.relativize(p).toString();
      propagatePhoto(filename);
      if (imgPhoto != null) imgPhoto.setImage(new Image(p.toUri().toString(), true));
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Erreur sélection: " + ex.getMessage()).showAndWait();
    }
  }

  private void importPhoto() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Importer une photo");
    fc.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
        new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
    );
    var file = fc.showOpenDialog(lblName.getScene().getWindow());
    if (file == null) return;
    try {
      Path dir = photosDir();
      String name = file.getName();
      String ext = "";
      int i = name.lastIndexOf('.');
      if (i > 0) ext = name.substring(i);
      String base = nz(lblName.getText());
      String slug = base.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
      String filename = slug + "_" + productId + ext.toLowerCase();
      Path target = dir.resolve(filename);
      Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
      propagatePhoto(filename);
      if (imgPhoto != null) imgPhoto.setImage(new Image(target.toUri().toString(), true));
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Import impossible: " + ex.getMessage()).showAndWait();
    }
  }

  private void propagatePhoto(String filename) {
    if (productId <= 0) return;
    prodRepo.updatePhotoForSameNameByProduct(productId, filename);
    load();
  }

    @FXML
  private void onValidateNewSubcategory() {
    String cat = emptyToNull(cbCategory == null ? null : cbCategory.getValue());
    String sub = emptyToNull(tfNewSubcategory == null ? null : tfNewSubcategory.getText());
    if (cat == null) { new Alert(Alert.AlertType.WARNING, "Choisissez d'abord une catégorie.").showAndWait(); return; }
    if (sub == null) { new Alert(Alert.AlertType.WARNING, "Saisissez un nom de sous‑catégorie.").showAndWait(); return; }
    if (cbSubcategory != null && !cbSubcategory.getItems().contains(sub)) {
      cbSubcategory.getItems().add(sub);
      cbSubcategory.setValue(sub);
    }
    
    String trimmedCategory = cat == null ? null : cat.trim();
    String trimmedSubcategory = sub == null ? null : sub.trim();
    if (confirmPropagateChanges("sous-catégorie", trimmedSubcategory)) {
      prodRepo.updateCategoryNamesForSameNameByProduct(productId, trimmedCategory, trimmedSubcategory);
      load();
    }
    if (tfNewSubcategory != null) tfNewSubcategory.clear();
  }

  @FXML
  private void onCreateIntervention() {
    openWindow("Nouvelle intervention", "/fxml/new_intervention.fxml");
  }

  @FXML
  private void onExportPdf() {
    new Alert(Alert.AlertType.INFORMATION, "Fonction d'export PDF à implémenter").showAndWait();
  }

  @FXML
  private void onValidateChanges() {
    // Sauvegarder tous les changements en cours
    if (cbCategory != null && cbCategory.getValue() != null) {
      String cat = cbCategory.getValue().trim();
      String sub = cbSubcategory != null ? cbSubcategory.getValue() : null;
      if (sub != null) sub = sub.trim();
      
      if (confirmPropagateChanges("catégorie et sous-catégorie", cat + (sub != null ? " / " + sub : ""))) {
        prodRepo.updateCategoryNamesForSameNameByProduct(productId, emptyToNull(cat), emptyToNull(sub));
      }
    }
    
    // Fermer la fenêtre après validation
    if (lblName != null && lblName.getScene() != null && lblName.getScene().getWindow() != null) {
      lblName.getScene().getWindow().hide();
    }
  }

  // Ouvre une fenêtre à partir d'un FXML
  private void openWindow(String title, String fxmlPath) {
    try {
      var url = getClass().getResource(fxmlPath);
      if (url == null) {
        new Alert(Alert.AlertType.ERROR, "FXML introuvable: " + fxmlPath).showAndWait();
        return;
      }
      var loader = new FXMLLoader(url);
      Parent root = loader.load();
      var stage = new Stage();
      stage.setTitle(title);
      stage.setScene(new Scene(root));
      stage.show();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture: " + e.getMessage()).showAndWait();
    }
  }

  private static String emptyToNull(String s) { return (s == null || s.isBlank()) ? null : s; }
  private static String nz(String s) { return s == null ? "" : s; }
}