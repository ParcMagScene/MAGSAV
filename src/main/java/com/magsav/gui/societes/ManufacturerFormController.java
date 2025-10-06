package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.util.MediaPaths;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.nio.file.*;
import java.util.regex.Pattern;

public class ManufacturerFormController {
  @FXML private ImageView imgLogo;
  @FXML private TextField tfNom;
  @FXML private TextField tfEmail;
  @FXML private TextField tfPhone;
  @FXML private TextArea taAdresse;
  @FXML private TextArea taNotes;

  private String selectedLogo;

  @FXML
  private void initialize() {
    // Autoriser lettres, chiffres, espace, ., -, _, ', &, ()
    Pattern allowed = Pattern.compile("[\\p{L}\\p{N} .\\-_'&()]*");
    tfNom.setTextFormatter(new TextFormatter<>(change ->
        allowed.matcher(change.getControlNewText()).matches() && change.getControlNewText().length() <= 100
            ? change : null));
  }

  public void init(Societe current) {
    if (current != null) {
      tfNom.setText(current.nom());
      tfEmail.setText(current.email());
      tfPhone.setText(current.phone());
      taAdresse.setText(current.adresse());
      taNotes.setText(current.notes());
      loadLogo(current.nom());
    }
  }

  private void loadLogo(String fabricantName) {
    if (imgLogo == null || fabricantName == null || fabricantName.trim().isEmpty()) return;
    try {
      Path dir = MediaPaths.logosDir();
      String base = slug(fabricantName);
      String[] exts = { ".png", ".jpg", ".jpeg", ".svg" };
      for (String ext : exts) {
        Path p = dir.resolve(base + ext);
        if (Files.exists(p)) {
          imgLogo.setImage(new Image(p.toUri().toString(), true));
          selectedLogo = dir.relativize(p).toString();
          return;
        }
      }
      imgLogo.setImage(null);
      selectedLogo = null;
    } catch (Exception ex) {
      // Ignore errors for logo loading
    }
  }

  @FXML
  private void onChooseLogo() {
    if (tfNom == null || tfNom.getText().trim().isEmpty()) {
      new Alert(Alert.AlertType.WARNING, "Veuillez d'abord saisir le nom du fabricant").showAndWait();
      return;
    }
    
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
      var file = fc.showOpenDialog(tfNom.getScene().getWindow());
      if (file == null) return;
      Path p = file.toPath().toAbsolutePath().normalize();
      if (!p.startsWith(dir.toAbsolutePath().normalize())) {
        new Alert(Alert.AlertType.WARNING, "Le fichier n'est pas dans le dossier logos. Utilisez 'Importer…'").showAndWait();
        return;
      }
      String filename = dir.relativize(p).toString();
      selectedLogo = filename;
      imgLogo.setImage(new Image(p.toUri().toString(), true));
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
    var file = fc.showOpenDialog(tfNom.getScene().getWindow());
    if (file == null) return;
    try {
      Path dir = MediaPaths.logosDir();
      String name = file.getName();
      String ext = "";
      int i = name.lastIndexOf('.');
      if (i > 0) ext = name.substring(i);
      String base = slug(tfNom.getText());
      String filename = base + ext.toLowerCase();
      Path target = dir.resolve(filename);
      Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
      selectedLogo = filename;
      imgLogo.setImage(new Image(target.toUri().toString(), true));
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Import impossible: " + ex.getMessage()).showAndWait();
    }
  }

  private static String slug(String s) {
    if (s == null) return "unknown";
    String out = s.toLowerCase().replaceAll("[^a-z0-9]+", "_");
    return out.replaceAll("^_+|_+$", "");
  }

  public boolean isValid() { return nom().trim().length() >= 2; }

  public String nom() {
    return tfNom == null ? "" : tfNom.getText();
  }
  public String email()   { return v(tfEmail.getText()); }
  public String phone()   { return v(tfPhone.getText()); }
  public String adresse() { return v(taAdresse.getText()); }
  public String notes()   { return v(taNotes.getText()); }

  private static String v(String s) { return s == null ? "" : s.trim(); }
}