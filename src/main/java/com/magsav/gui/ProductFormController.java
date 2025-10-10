package com.magsav.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProductFormController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField snField;
    @FXML private TextField fabricantField;
    @FXML private TextField uidField;
    @FXML private ComboBox<String> situationCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField prixField;
    @FXML private DatePicker dateAchatPicker;
    @FXML private ComboBox<String> clientCombo;
    @FXML private TextField garantieField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> sousCategorieCombo;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private boolean saved = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFormValidation();
        setupCombos();
    }

    private void setupFormValidation() {
        // Désactiver le bouton sauvegarder tant que le nom n'est pas renseigné
        saveButton.disableProperty().bind(
            nomField.textProperty().isEmpty()
        );
    }

    private void setupCombos() {
        // Situations de produit
        situationCombo.getItems().addAll(
            "En stock",
            "En commande", 
            "En réparation",
            "Livré",
            "Retourné",
            "Perdu",
            "Détruit"
        );
        situationCombo.setValue("En stock");

        // Catégories de base (à remplacer par les données de la DB)
        categorieCombo.getItems().addAll(
            "Électronique",
            "Informatique", 
            "Audio/Vidéo",
            "Télécommunication",
            "Matériel médical",
            "Autre"
        );

        // Écouter les changements de catégorie pour mettre à jour les sous-catégories
        categorieCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateSousCategories(newVal);
        });

        // Clients (à remplacer par les données de la DB)
        clientCombo.getItems().addAll(
            "Client particulier",
            "Entreprise A",
            "Entreprise B",
            "Administration",
            "Autre"
        );
    }

    private void updateSousCategories(String categorie) {
        sousCategorieCombo.getItems().clear();
        if (categorie == null) return;

        switch (categorie) {
            case "Électronique":
                sousCategorieCombo.getItems().addAll("Composants", "Appareils ménagers", "Éclairage");
                break;
            case "Informatique":
                sousCategorieCombo.getItems().addAll("Ordinateurs", "Périphériques", "Réseaux", "Stockage");
                break;
            case "Audio/Vidéo":
                sousCategorieCombo.getItems().addAll("Haut-parleurs", "Amplificateurs", "Écrans", "Projecteurs");
                break;
            case "Télécommunication":
                sousCategorieCombo.getItems().addAll("Téléphones", "Radios", "Antennes", "Modems");
                break;
            case "Matériel médical":
                sousCategorieCombo.getItems().addAll("Diagnostic", "Traitement", "Monitoring", "Mobilité");
                break;
            default:
                sousCategorieCombo.getItems().add("Général");
                break;
        }
    }

    @FXML
    private void onSave() {
        if (validateForm()) {
            // TODO: Implémenter la sauvegarde en base de données
            saved = true;
            closeDialog();
            
            showSuccessAlert();
        }
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom du produit est obligatoire\n");
        }

        if (prixField.getText() != null && !prixField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(prixField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- Le prix doit être un nombre valide\n");
            }
        }

        if (garantieField.getText() != null && !garantieField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(garantieField.getText().trim());
            } catch (NumberFormatException e) {
                errors.append("- La garantie doit être un nombre entier de mois\n");
            }
        }

        if (errors.length() > 0) {
            showErrorAlert("Erreurs de validation", "Veuillez corriger les erreurs suivantes :\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    private void closeDialog() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showSuccessAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Produit créé");
        alert.setHeaderText(null);
        alert.setContentText("Le produit a été créé avec succès.");
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean isSaved() {
        return saved;
    }

    // Getters pour récupérer les valeurs du formulaire si besoin
    public String getNom() { return nomField.getText(); }
    public String getSn() { return snField.getText(); }
    public String getFabricant() { return fabricantField.getText(); }
    public String getUid() { return uidField.getText(); }
    public String getSituation() { return situationCombo.getValue(); }
    public String getDescription() { return descriptionArea.getText(); }
    public String getPrix() { return prixField.getText(); }
    public String getClient() { return clientCombo.getValue(); }
    public String getGarantie() { return garantieField.getText(); }
    public String getCategorie() { return categorieCombo.getValue(); }
    public String getSousCategorie() { return sousCategorieCombo.getValue(); }
}