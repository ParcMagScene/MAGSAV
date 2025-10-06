package com.magsav.gui.societes;

import com.magsav.repo.ProductRepository;
import com.magsav.util.MediaPaths;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.nio.file.*;
import java.util.List;

public class ManufacturerDetailController {
  @FXML private Label lblName, lblCount;
  @FXML private ImageView imgLogo;
  @FXML private TableView<ProductRepository.ProductRow> table;
  @FXML private TableColumn<ProductRepository.ProductRow, Long> colId;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colCode, colNom, colSN, colSituation;

  private final ProductRepository repo = new ProductRepository();
  private String fabricant;

  @FXML
  private void initialize() {
    if (colId != null) colId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id()));
    if (colCode != null) colCode.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().code()));
    if (colNom != null) colNom.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().nom()));
    if (colSN != null) colSN.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().sn()));
    if (colSituation != null) colSituation.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().situation()));
  }

  public void setFabricant(String fabricant) {
    this.fabricant = fabricant == null ? "" : fabricant.trim();
    if (lblName != null) lblName.setText(this.fabricant);
    loadLogo();
    List<ProductRepository.ProductRow> rows = repo.findByFabricant(this.fabricant);
    if (lblCount != null) lblCount.setText(rows.size() + " produit(s)");
    if (table != null) table.setItems(FXCollections.observableArrayList(rows));
  }

  private void loadLogo() {
    if (imgLogo == null) return;
    Path dir = MediaPaths.logosDir();
    String base = slug(fabricant);
    String[] exts = { ".png", ".jpg", ".jpeg" };
    for (String ext : exts) {
      Path p = dir.resolve(base + ext);
      if (Files.exists(p)) {
        imgLogo.setImage(new Image(p.toUri().toString(), true));
        return;
      }
    }
    imgLogo.setImage(null);
  }

  private static String slug(String s) {
    if (s == null) return "unknown";
    String out = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    return out.replaceAll("^_+|_+$", "");
  }

  @FXML
  private void onChooseLogo() {
    if (fabricant == null || fabricant.trim().isEmpty() || lblName == null || lblName.getScene() == null) return;
    
    ButtonType choisir = new ButtonType("Depuis dossier logos", ButtonBar.ButtonData.LEFT);
    ButtonType importer = new ButtonType("Importer…", ButtonBar.ButtonData.RIGHT);
    ButtonType annuler = ButtonType.CANCEL;
    Alert a = new Alert(Alert.AlertType.NONE, "", choisir, importer, annuler);
    a.setTitle("Logo du fabricant");
    a.setHeaderText("Choisir la source du logo");
    var result = a.showAndWait().orElse(annuler);
    if (result == choisir) chooseFromLibrary();
    else if (result == importer) importLogo();
  }

  private void chooseFromLibrary() {
    try {
      Path dir = MediaPaths.logosDir();
      FileChooser fc = new FileChooser();
      fc.setTitle("Choisir dans le dossier logos");
      fc.setInitialDirectory(dir.toFile());
      fc.getExtensionFilters().setAll(
          new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.svg", "*.gif"),
          new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
      );
      var file = fc.showOpenDialog(lblName.getScene().getWindow());
      if (file == null) return;
      Path p = file.toPath().toAbsolutePath().normalize();
      if (!p.startsWith(dir.toAbsolutePath().normalize())) {
        new Alert(Alert.AlertType.WARNING, "Le fichier n'est pas dans le dossier logos. Utilisez 'Importer…'").showAndWait();
        return;
      }
      String filename = dir.relativize(p).toString();
      saveLogo(filename);
      loadLogo();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Erreur sélection: " + ex.getMessage()).showAndWait();
    }
  }

  private void importLogo() {
    FileChooser fc = new FileChooser();
    fc.setTitle("Importer un logo");
    fc.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.svg", "*.gif"),
        new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
    );
    var file = fc.showOpenDialog(lblName.getScene().getWindow());
    if (file == null) return;
    try {
      Path dir = MediaPaths.logosDir();
      String name = file.getName();
      String ext = "";
      int i = name.lastIndexOf('.');
      if (i > 0) ext = name.substring(i);
      String base = slug(fabricant);
      String filename = base + ext.toLowerCase();
      Path target = dir.resolve(filename);
      Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
      saveLogo(filename);
      loadLogo();
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Import impossible: " + ex.getMessage()).showAndWait();
    }
  }

  private void saveLogo(String filename) {
    // Note: Pour l'instant, le logo est sauvé automatiquement par nom de fichier
    // Si besoin d'une base de données, ajouter ici la logique de sauvegarde
  }
}