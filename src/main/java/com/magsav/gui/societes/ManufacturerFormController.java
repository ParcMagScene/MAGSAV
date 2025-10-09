package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.AddressService;
import com.magsav.ui.components.FormDialogManager;
import com.magsav.util.MediaPaths;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.nio.file.*;
import java.util.regex.Pattern;

public class ManufacturerFormController implements FormDialogManager.FormController {
  @FXML private ImageView imgLogo;
  @FXML private TextField tfNom;
  @FXML private TextField tfEmail;
  @FXML private TextField tfPhone;
  @FXML private TextArea taAdresse;
  @FXML private TextArea taNotes;

  private String selectedLogo;
  private final SocieteRepository repo = new SocieteRepository();
  private final AddressService addressService = new AddressService();
  private Societe currentSociete;
  private boolean isEditMode = false;
  private String societeType = "FABRICANT"; // Par défaut

  @FXML
  private void initialize() {
    Pattern allowed = Pattern.compile("[\\p{L}\\p{N} .\\-_'&()]*");
    tfNom.setTextFormatter(new TextFormatter<>(change ->
        allowed.matcher(change.getControlNewText()).matches() && change.getControlNewText().length() <= 100
            ? change : null));
    
    // Ajouter l'autocomplétion d'adresse au TextArea
    if (taAdresse != null) {
      addressService.setupAddressAutocompleteForTextArea(taAdresse);
    }
  }

  @Override
  public void initForm() {
    // Called by FormDialogManager
  }

  /**
   * Définit le type de société (FABRICANT, FOURNISSEUR, CLIENT)
   */
  public void setSocieteType(String type) {
    this.societeType = type != null ? type : "FABRICANT";
  }

  @Override
  public boolean validateForm() {
    return isValid();
  }

  @Override
  public void saveFormData() {
    try {
      String nom = nom().trim();
      String email = email();
      String phone = phone();
      String adresse = adresse();
      String notes = notes();
      
      if (currentSociete != null) {
        repo.update(currentSociete.id(), societeType, nom, email, phone, adresse, notes);
      } else {
        repo.insert(societeType, nom, email, phone, adresse, notes);
      }
    } catch (Exception e) {
      throw new RuntimeException("Erreur sauvegarde: " + e.getMessage(), e);
    }
  }

  public void init(Societe current) {
    this.currentSociete = current;
    this.isEditMode = (current != null);
    
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
      // Ignore logo loading errors
    }
  }

  @FXML
  private void onChooseLogo() {
    if (tfNom == null || tfNom.getText().trim().isEmpty()) {
      new Alert(Alert.AlertType.WARNING, "Veuillez d'abord saisir le nom du fabricant").showAndWait();
      return;
    }
    
    // Ouvrir directement la mosaïque des logos
    chooseFromLibrary();
  }

  private void chooseFromLibrary() {
    try {
      // Utiliser la vue mosaïque pour la sélection de logos
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/dialogs/logo_mosaic.fxml"));
      javafx.scene.Parent root = loader.load();
      
      com.magsav.gui.dialogs.LogoMosaicController mosaicController = loader.getController();
      
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("Choisir un logo - Vue mosaïque");
      stage.setScene(new javafx.scene.Scene(root, 800, 600));
      stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
      stage.initOwner(tfNom.getScene().getWindow());
      
      mosaicController.setStage(stage);
      
      stage.showAndWait();
      
      String selectedLogoPath = mosaicController.getSelectedLogoPath();
      if (selectedLogoPath != null && !selectedLogoPath.trim().isEmpty()) {
        // Le chemin retourné est relatif au dossier logos
        selectedLogo = selectedLogoPath;
        
        // Charger l'image pour l'affichage
        Path fullPath = MediaPaths.logosDir().resolve(selectedLogo);
        imgLogo.setImage(new Image(fullPath.toUri().toString(), true));
      }
      
    } catch (Exception ex) {
      new Alert(Alert.AlertType.ERROR, "Erreur sélection logo: " + ex.getMessage()).showAndWait();
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

  public boolean isValid() { 
    return nom().trim().length() >= 2; 
  }

  public String nom() {
    return tfNom == null ? "" : tfNom.getText();
  }
  
  public String email() { 
    return v(tfEmail.getText()); 
  }
  
  public String phone() { 
    return v(tfPhone.getText()); 
  }
  
  public String adresse() { 
    return v(taAdresse.getText()); 
  }
  
  public String notes() { 
    return v(taNotes.getText()); 
  }

  private static String v(String s) { 
    return s == null ? "" : s.trim(); 
  }
}
