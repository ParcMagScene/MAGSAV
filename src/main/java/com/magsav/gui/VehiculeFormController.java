package com.magsav.gui;

import com.magsav.model.Vehicule;
import com.magsav.model.Vehicule.TypeVehicule;
import com.magsav.model.Vehicule.StatutVehicule;
import com.magsav.repo.VehiculeRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le formulaire de création/modification d'un véhicule
 */
public class VehiculeFormController implements Initializable {
    
    // Champs du formulaire
    @FXML private TextField txtImmatriculation;
    @FXML private ComboBox<TypeVehicule> cmbType;
    @FXML private TextField txtMarque;
    @FXML private TextField txtModele;
    @FXML private Spinner<Integer> spnAnnee;
    @FXML private Spinner<Integer> spnKilometrage;
    @FXML private ComboBox<StatutVehicule> cmbStatut;
    @FXML private CheckBox chkLocationExterne;
    @FXML private TextArea txtNotes;
    
    // Informations système
    @FXML private Label lblDateCreation;
    @FXML private Label lblDateModification;
    @FXML private Label lblId;
    
    // Boutons
    @FXML private Button btnSauvegarder;
    @FXML private Button btnAnnuler;
    @FXML private Button btnEffacer;
    
    // Labels de validation
    @FXML private Label lblValidationImmatriculation;
    @FXML private Label lblValidationMarque;
    @FXML private Label lblValidationModele;
    
    private final VehiculeRepository vehiculeRepository = new VehiculeRepository();
    private Vehicule vehicule; // null pour création, objet existant pour modification
    private VehiculeController parentController;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFormFields();
        setupValidation();
        setupEventHandlers();
        clearForm();
    }
    
    /**
     * Configuration des champs du formulaire
     */
    private void setupFormFields() {
        // Configuration des ComboBox
        cmbType.getItems().addAll(TypeVehicule.values());
        cmbType.setConverter(new javafx.util.StringConverter<TypeVehicule>() {
            @Override
            public String toString(TypeVehicule type) {
                return type != null ? type.getDisplayName() : "";
            }
            
            @Override
            public TypeVehicule fromString(String string) {
                return null; // Non utilisé
            }
        });
        
        cmbStatut.getItems().addAll(StatutVehicule.values());
        cmbStatut.setConverter(new javafx.util.StringConverter<StatutVehicule>() {
            @Override
            public String toString(StatutVehicule statut) {
                return statut != null ? statut.getDisplayName() : "";
            }
            
            @Override
            public StatutVehicule fromString(String string) {
                return null; // Non utilisé
            }
        });
        
        // Configuration des Spinners
        spnAnnee.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1900, 2030, 2023));
        spnAnnee.setEditable(true);
        
        spnKilometrage.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 999999, 0));
        spnKilometrage.setEditable(true);
        
        // Configuration du TextArea notes
        txtNotes.setWrapText(true);
        txtNotes.setPrefRowCount(4);
        
        // Valeurs par défaut
        cmbStatut.setValue(StatutVehicule.DISPONIBLE);
        chkLocationExterne.setSelected(false);
    }
    
    /**
     * Configuration de la validation en temps réel
     */
    private void setupValidation() {
        // Cacher les labels de validation au départ
        lblValidationImmatriculation.setVisible(false);
        lblValidationMarque.setVisible(false);
        lblValidationModele.setVisible(false);
        
        // Style pour les messages d'erreur
        lblValidationImmatriculation.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        lblValidationMarque.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
        lblValidationModele.setStyle("-fx-text-fill: red; -fx-font-size: 10px;");
    }
    
    /**
     * Configuration des gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Validation en temps réel
        txtImmatriculation.textProperty().addListener((obs, oldText, newText) -> {
            validateImmatriculation();
        });
        
        txtMarque.textProperty().addListener((obs, oldText, newText) -> {
            validateMarque();
        });
        
        txtModele.textProperty().addListener((obs, oldText, newText) -> {
            validateModele();
        });
        
        // Conversion automatique en majuscules pour l'immatriculation
        txtImmatriculation.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && !newText.equals(newText.toUpperCase())) {
                txtImmatriculation.setText(newText.toUpperCase());
            }
        });
        
        // Formatage des champs texte
        txtMarque.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 50) {
                txtMarque.setText(oldText);
            }
        });
        
        txtModele.textProperty().addListener((obs, oldText, newText) -> {
            if (newText != null && newText.length() > 50) {
                txtModele.setText(oldText);
            }
        });
    }
    
    /**
     * Définit le véhicule à modifier (null pour création)
     */
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
        if (vehicule != null) {
            fillForm();
        } else {
            clearForm();
        }
    }
    
    /**
     * Définit le contrôleur parent pour notification
     */
    public void setParentController(VehiculeController parentController) {
        this.parentController = parentController;
    }
    
    /**
     * Remplit le formulaire avec les données du véhicule
     */
    private void fillForm() {
        if (vehicule != null) {
            txtImmatriculation.setText(vehicule.getImmatriculation());
            cmbType.setValue(vehicule.getTypeVehicule());
            txtMarque.setText(vehicule.getMarque());
            txtModele.setText(vehicule.getModele());
            spnAnnee.getValueFactory().setValue(vehicule.getAnnee() != 0 ? vehicule.getAnnee() : 2023);
            spnKilometrage.getValueFactory().setValue(vehicule.getKilometrage());
            cmbStatut.setValue(vehicule.getStatut());
            chkLocationExterne.setSelected(vehicule.isLocationExterne());
            txtNotes.setText(vehicule.getNotes());
            
            // Informations système
            lblId.setText("ID: " + vehicule.getId());
            lblDateCreation.setText("Créé: " + (vehicule.getDateCreation() != null ? vehicule.getDateCreation() : "-"));
            lblDateModification.setText("Modifié: " + (vehicule.getDateModification() != null ? vehicule.getDateModification() : "-"));
        }
    }
    
    /**
     * Vide le formulaire
     */
    private void clearForm() {
        txtImmatriculation.clear();
        cmbType.setValue(null);
        txtMarque.clear();
        txtModele.clear();
        spnAnnee.getValueFactory().setValue(2023);
        spnKilometrage.getValueFactory().setValue(0);
        cmbStatut.setValue(StatutVehicule.DISPONIBLE);
        chkLocationExterne.setSelected(false);
        txtNotes.clear();
        
        // Informations système
        lblId.setText("ID: Nouveau");
        lblDateCreation.setText("Créé: -");
        lblDateModification.setText("Modifié: -");
        
        // Cacher les messages de validation
        lblValidationImmatriculation.setVisible(false);
        lblValidationMarque.setVisible(false);
        lblValidationModele.setVisible(false);
    }
    
    // === VALIDATION ===
    
    /**
     * Valide l'immatriculation
     */
    private boolean validateImmatriculation() {
        String immat = txtImmatriculation.getText();
        if (immat == null || immat.trim().isEmpty()) {
            showValidationError(lblValidationImmatriculation, "L'immatriculation est obligatoire");
            return false;
        } else if (immat.length() < 3) {
            showValidationError(lblValidationImmatriculation, "L'immatriculation doit contenir au moins 3 caractères");
            return false;
        } else if (immat.length() > 20) {
            showValidationError(lblValidationImmatriculation, "L'immatriculation ne peut pas dépasser 20 caractères");
            return false;
        } else {
            // Vérifier l'unicité (si modification, exclure le véhicule actuel)
            boolean exists = vehiculeRepository.existsByImmatriculation(immat, vehicule != null ? vehicule.getId() : null);
            if (exists) {
                showValidationError(lblValidationImmatriculation, "Cette immatriculation existe déjà");
                return false;
            } else {
                hideValidationError(lblValidationImmatriculation);
                return true;
            }
        }
    }
    
    /**
     * Valide la marque
     */
    private boolean validateMarque() {
        String marque = txtMarque.getText();
        if (marque == null || marque.trim().isEmpty()) {
            showValidationError(lblValidationMarque, "La marque est obligatoire");
            return false;
        } else if (marque.length() > 50) {
            showValidationError(lblValidationMarque, "La marque ne peut pas dépasser 50 caractères");
            return false;
        } else {
            hideValidationError(lblValidationMarque);
            return true;
        }
    }
    
    /**
     * Valide le modèle
     */
    private boolean validateModele() {
        String modele = txtModele.getText();
        if (modele == null || modele.trim().isEmpty()) {
            showValidationError(lblValidationModele, "Le modèle est obligatoire");
            return false;
        } else if (modele.length() > 50) {
            showValidationError(lblValidationModele, "Le modèle ne peut pas dépasser 50 caractères");
            return false;
        } else {
            hideValidationError(lblValidationModele);
            return true;
        }
    }
    
    /**
     * Valide l'ensemble du formulaire
     */
    private boolean validateForm() {
        boolean valid = true;
        
        valid &= validateImmatriculation();
        valid &= validateMarque();
        valid &= validateModele();
        
        // Validation du type
        if (cmbType.getValue() == null) {
            showError("Validation", "Le type de véhicule est obligatoire");
            valid = false;
        }
        
        // Validation de l'année
        int annee = spnAnnee.getValue();
        if (annee < 1900 || annee > 2030) {
            showError("Validation", "L'année doit être comprise entre 1900 et 2030");
            valid = false;
        }
        
        // Validation du kilométrage
        int kilometrage = spnKilometrage.getValue();
        if (kilometrage < 0) {
            showError("Validation", "Le kilométrage ne peut pas être négatif");
            valid = false;
        }
        
        return valid;
    }
    
    private void showValidationError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
    }
    
    private void hideValidationError(Label label) {
        label.setVisible(false);
    }
    
    // === GESTIONNAIRES D'ÉVÉNEMENTS ===
    
    @FXML
    private void onSauvegarder() {
        if (!validateForm()) {
            return;
        }
        
        try {
            // Créer ou modifier le véhicule
            if (vehicule == null) {
                vehicule = new Vehicule();
            }
            
            // Remplir les données
            vehicule.setImmatriculation(txtImmatriculation.getText().trim());
            vehicule.setTypeVehicule(cmbType.getValue());
            vehicule.setMarque(txtMarque.getText().trim());
            vehicule.setModele(txtModele.getText().trim());
            vehicule.setAnnee(spnAnnee.getValue());
            vehicule.setKilometrage(spnKilometrage.getValue());
            vehicule.setStatut(cmbStatut.getValue());
            vehicule.setLocationExterne(chkLocationExterne.isSelected());
            vehicule.setNotes(txtNotes.getText().trim());
            
            // Sauvegarder
            boolean success = vehiculeRepository.save(vehicule);
            
            if (success) {
                // Notifier le contrôleur parent
                if (parentController != null) {
                    parentController.onVehiculesSaved();
                }
                
                // Fermer la fenêtre
                Stage stage = (Stage) btnSauvegarder.getScene().getWindow();
                stage.close();
                
            } else {
                showError("Erreur", "Impossible de sauvegarder le véhicule");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    @FXML
    private void onAnnuler() {
        // Fermer sans sauvegarder
        Stage stage = (Stage) btnAnnuler.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void onEffacer() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Effacer le formulaire");
        alert.setContentText("Êtes-vous sûr de vouloir effacer tous les champs ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                clearForm();
            }
        });
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}