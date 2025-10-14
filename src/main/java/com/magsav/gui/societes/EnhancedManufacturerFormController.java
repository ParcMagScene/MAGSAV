package com.magsav.gui.societes;

import com.magsav.gui.component.FileDropZone;
import com.magsav.model.Company;
import com.magsav.util.MediaImporter;
import com.magsav.util.AppLogger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur amélioré pour le formulaire de fabricant
 * avec zones de glisser-déposer pour les médias
 */
public class EnhancedManufacturerFormController implements Initializable {
    
    // Champs du formulaire
    @FXML private Label lblTitle;
    @FXML private TextField tfNom, tfEmail, tfPhone, tfWebsite, tfCountry;
    @FXML private TextArea taNotes;
    
    // Zones de glisser-déposer
    @FXML private FileDropZone logoDropZone;
    @FXML private FileDropZone documentsDropZone;
    
    // Boutons et labels
    @FXML private Button btnRemoveLogo, btnSave;
    @FXML private Label lblLogoStatus, lblDocumentsCount;
    
    // Données
    private Company currentCompany;
    private boolean isEditMode = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            setupDropZones();
            updateUI();
            AppLogger.info("Formulaire fabricant amélioré initialisé");
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'initialisation du formulaire fabricant: " + e.getMessage(), e);
            showErrorAlert("Erreur d'initialisation", "Impossible d'initialiser le formulaire.");
        }
    }
    
    private void setupDropZones() {
        // Configuration de la zone de logo
        if (logoDropZone != null) {
            logoDropZone.setOnFilesDropped(files -> handleLogoFiles(files));
            AppLogger.info("Zone de dépôt logo configurée");
        }
        
        // Configuration de la zone de documents
        if (documentsDropZone != null) {
            documentsDropZone.setOnFilesDropped(files -> handleDocumentFiles(files));
            AppLogger.info("Zone de dépôt documents configurée");
        }
    }
    
    private void handleLogoFiles(List<File> files) {
        if (files.isEmpty()) return;
        
        File logoFile = files.get(0); // Prendre seulement le premier fichier pour le logo
        
        try {
            // Importer le logo
            MediaImporter.importFiles(List.of(logoFile), MediaImporter.MediaType.LOGO);
            
            // Mettre à jour l'interface
            if (lblLogoStatus != null) {
                lblLogoStatus.setText("Logo: " + logoFile.getName());
                lblLogoStatus.setStyle("-fx-text-fill: #4CAF50;");
            }
            
            if (btnRemoveLogo != null) {
                btnRemoveLogo.setDisable(false);
            }
            
            AppLogger.info("Logo importé: " + logoFile.getName());
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'import du logo: " + e.getMessage(), e);
            showErrorAlert("Erreur d'import", "Impossible d'importer le logo: " + e.getMessage());
        }
    }
    
    private void handleDocumentFiles(List<File> files) {
        if (files.isEmpty()) return;
        
        try {
            // Importer les documents
            MediaImporter.importFiles(files, MediaImporter.MediaType.DOCUMENT);
            
            // Mettre à jour le compteur
            if (lblDocumentsCount != null) {
                lblDocumentsCount.setText(files.size() + " document(s) importé(s)");
                lblDocumentsCount.setStyle("-fx-text-fill: #4CAF50;");
            }
            
            AppLogger.info("Documents importés: " + files.size());
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'import des documents: " + e.getMessage(), e);
            showErrorAlert("Erreur d'import", "Impossible d'importer les documents: " + e.getMessage());
        }
    }
    
    /**
     * Définit le fabricant à éditer (null pour création)
     */
    public void setCompany(Company company) {
        this.currentCompany = company;
        this.isEditMode = (company != null);
        updateUI();
        populateFields();
    }
    
    private void updateUI() {
        if (lblTitle != null) {
            lblTitle.setText(isEditMode ? "Modifier le Fabricant" : "Nouveau Fabricant");
        }
        
        if (btnRemoveLogo != null) {
            btnRemoveLogo.setDisable(true); // Désactivé par défaut
        }
        
        if (lblDocumentsCount != null) {
            lblDocumentsCount.setText("0 document(s)");
        }
    }
    
    private void populateFields() {
        if (currentCompany == null) return;
        
        if (tfNom != null) tfNom.setText(currentCompany.getName());
        if (tfEmail != null) tfEmail.setText(currentCompany.getEmail());
        if (tfPhone != null) tfPhone.setText(currentCompany.getPhone());
        if (tfWebsite != null) tfWebsite.setText(currentCompany.getWebsite());
        if (tfCountry != null) tfCountry.setText(currentCompany.getCountry());
        if (taNotes != null) taNotes.setText(currentCompany.getDescription());
        
        // Vérifier si un logo existe
        if (currentCompany.getLogoPath() != null && !currentCompany.getLogoPath().isEmpty()) {
            if (lblLogoStatus != null) {
                lblLogoStatus.setText("Logo: " + new File(currentCompany.getLogoPath()).getName());
                lblLogoStatus.setStyle("-fx-text-fill: #4CAF50;");
            }
            if (btnRemoveLogo != null) {
                btnRemoveLogo.setDisable(false);
            }
        }
    }
    
    // Actions du formulaire
    @FXML
    private void onBrowseLogo() {
        try {
            Stage stage = (Stage) btnSave.getScene().getWindow();
            MediaImporter.showFileChooser(stage, MediaImporter.MediaType.LOGO);
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'ouverture du sélecteur de logo: " + e.getMessage(), e);
        }
    }
    
    @FXML
    private void onRemoveLogo() {
        if (lblLogoStatus != null) {
            lblLogoStatus.setText("");
        }
        if (btnRemoveLogo != null) {
            btnRemoveLogo.setDisable(true);
        }
        AppLogger.info("Logo supprimé");
    }
    
    @FXML
    private void onBrowseDocuments() {
        try {
            Stage stage = (Stage) btnSave.getScene().getWindow();
            MediaImporter.showFileChooser(stage, MediaImporter.MediaType.DOCUMENT);
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'ouverture du sélecteur de documents: " + e.getMessage(), e);
        }
    }
    
    @FXML
    private void onSave() {
        try {
            if (!validateInput()) {
                return;
            }
            
            AppLogger.info("Sauvegarde du fabricant: " + tfNom.getText());
            
            // Créer ou mettre à jour le fabricant (simulation)
            showInfoAlert("Succès", "Fabricant sauvegardé avec succès !");
            
            // Fermer la fenêtre
            closeWindow();
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la sauvegarde: " + e.getMessage(), e);
            showErrorAlert("Erreur de sauvegarde", "Impossible de sauvegarder le fabricant: " + e.getMessage());
        }
    }
    
    @FXML
    private void onCancel() {
        closeWindow();
    }
    
    private boolean validateInput() {
        if (tfNom == null || tfNom.getText().trim().isEmpty()) {
            showErrorAlert("Champ requis", "Le nom du fabricant est obligatoire.");
            if (tfNom != null) tfNom.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void closeWindow() {
        try {
            Node node = btnSave != null ? btnSave : lblTitle;
            if (node != null && node.getScene() != null && node.getScene().getWindow() instanceof Stage stage) {
                stage.close();
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la fermeture: " + e.getMessage(), e);
        }
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}