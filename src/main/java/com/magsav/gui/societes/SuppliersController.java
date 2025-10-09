package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.service.AddressService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class SuppliersController implements Initializable {

  @FXML private TextField tfSearch;
  @FXML private TableView<Societe> table;
  @FXML private TableColumn<Societe, Long> colId;
  @FXML private TableColumn<Societe, String> colNom;
  @FXML private TableColumn<Societe, String> colEmail;
  @FXML private TableColumn<Societe, String> colPhone;
  @FXML private TableColumn<Societe, String> colNotes;

  private final SocieteRepository societeRepo = new SocieteRepository();
  private final ObservableList<Societe> data = FXCollections.observableArrayList();
  private final AddressService addressService = new AddressService();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Configuration des colonnes avec lambdas (approche moderne)
    colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().id()));
    colNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().nom()));
    colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().email()));
    colPhone.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().phone()));
    colNotes.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().notes()));

    // Liaison des données
    table.setItems(data);

    // Configuration de la recherche
    tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));

    // Chargement initial
    loadData();
  }

  private void loadData() {
    try {
      data.clear();
      data.addAll(societeRepo.findByType("FOURNISSEUR"));
    } catch (Exception e) {
      showError("Erreur lors du chargement des fournisseurs", e.getMessage());
    }
  }

  private void filterData(String searchText) {
    if (searchText == null || searchText.trim().isEmpty()) {
      loadData();
      return;
    }

    try {
      data.clear();
      String search = searchText.toLowerCase().trim();
      data.addAll(societeRepo.findByType("FOURNISSEUR").stream()
          .filter(s -> 
              (s.nom() != null && s.nom().toLowerCase().contains(search)) ||
              (s.email() != null && s.email().toLowerCase().contains(search)) ||
              (s.phone() != null && s.phone().toLowerCase().contains(search)) ||
              (s.notes() != null && s.notes().toLowerCase().contains(search))
          ).toList());
    } catch (Exception e) {
      showError("Erreur lors de la recherche", e.getMessage());
    }
  }

  @FXML
  private void onRefresh() {
    loadData();
  }

  @FXML
  private void onAdd() {
    showSupplierDialog(null);
  }

  @FXML
  private void onEdit() {
    Societe selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un fournisseur à modifier.");
      return;
    }
    showSupplierDialog(selected);
  }
  
  private void showSupplierDialog(Societe fournisseur) {
    // Créer un formulaire spécifique aux fournisseurs
    Dialog<Societe> dialog = new Dialog<>();
    dialog.setTitle(fournisseur == null ? "Ajouter un fournisseur" : "Modifier le fournisseur");
    dialog.setHeaderText(fournisseur == null ? 
        "Informations du nouveau fournisseur" : 
        "Modification des informations du fournisseur");

    // Boutons du dialog
    ButtonType okButtonType = new ButtonType(fournisseur == null ? "Ajouter" : "Modifier", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

    // Créer les champs du formulaire
    TextField nomField = new TextField();
    nomField.setPromptText("Nom du fournisseur");
    TextField emailField = new TextField();
    emailField.setPromptText("Email");
    TextField phoneField = new TextField();
    phoneField.setPromptText("Téléphone");
    TextField adresseField = new TextField();
    adresseField.setPromptText("Adresse complète");
    
    // Ajouter l'autocomplétion d'adresse
    addressService.setupAddressAutocomplete(adresseField);
    TextField specialitesField = new TextField();
    specialitesField.setPromptText("Spécialités (audio, éclairage, vidéo...)");
    TextField delaisField = new TextField();
    delaisField.setPromptText("Délais de livraison moyens");
    TextArea notesArea = new TextArea();
    notesArea.setPromptText("Notes, conditions commerciales, contacts...");
    notesArea.setPrefRowCount(3);

    // Pré-remplir si modification
    if (fournisseur != null) {
      nomField.setText(fournisseur.nom());
      emailField.setText(fournisseur.email() != null ? fournisseur.email() : "");
      phoneField.setText(fournisseur.phone() != null ? fournisseur.phone() : "");
      adresseField.setText(fournisseur.adresse() != null ? fournisseur.adresse() : "");
      notesArea.setText(fournisseur.notes() != null ? fournisseur.notes() : "");
      
      // Extraire spécialités et délais des notes existantes si possible
      String notes = fournisseur.notes() != null ? fournisseur.notes() : "";
      if (notes.contains("Spécialités:")) {
        String[] parts = notes.split("Spécialités:");
        if (parts.length > 1) {
          String spec = parts[1].split("\n")[0].trim();
          specialitesField.setText(spec);
        }
      }
      if (notes.contains("Délais:")) {
        String[] parts = notes.split("Délais:");
        if (parts.length > 1) {
          String delai = parts[1].split("\n")[0].trim();
          delaisField.setText(delai);
        }
      }
    }

    // Layout du formulaire
    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

    grid.add(new Label("Nom:"), 0, 0);
    grid.add(nomField, 1, 0);
    grid.add(new Label("Email:"), 0, 1);
    grid.add(emailField, 1, 1);
    grid.add(new Label("Téléphone:"), 0, 2);
    grid.add(phoneField, 1, 2);
    grid.add(new Label("Adresse:"), 0, 3);
    grid.add(adresseField, 1, 3);
    grid.add(new Label("Spécialités:"), 0, 4);
    grid.add(specialitesField, 1, 4);
    grid.add(new Label("Délais:"), 0, 5);
    grid.add(delaisField, 1, 5);
    grid.add(new Label("Notes:"), 0, 6);
    grid.add(notesArea, 1, 6);

    dialog.getDialogPane().setContent(grid);

    // Validation
    javafx.scene.Node addButton = dialog.getDialogPane().lookupButton(okButtonType);
    addButton.setDisable(true);
    
    nomField.textProperty().addListener((observable, oldValue, newValue) -> {
      addButton.setDisable(newValue.trim().isEmpty());
    });

    // Résultat
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == okButtonType) {
        // Construire les notes avec les informations structurées
        StringBuilder notesBuilder = new StringBuilder();
        if (!specialitesField.getText().trim().isEmpty()) {
          notesBuilder.append("Spécialités: ").append(specialitesField.getText().trim()).append("\n");
        }
        if (!delaisField.getText().trim().isEmpty()) {
          notesBuilder.append("Délais: ").append(delaisField.getText().trim()).append("\n");
        }
        if (!notesArea.getText().trim().isEmpty()) {
          if (notesBuilder.length() > 0) notesBuilder.append("\n");
          notesBuilder.append(notesArea.getText().trim());
        }
        
        return new Societe(
            fournisseur != null ? fournisseur.id() : 0L,
            "FOURNISSEUR",
            nomField.getText().trim(),
            emailField.getText().trim().isEmpty() ? null : emailField.getText().trim(),
            phoneField.getText().trim().isEmpty() ? null : phoneField.getText().trim(),
            adresseField.getText().trim().isEmpty() ? null : adresseField.getText().trim(),
            notesBuilder.toString().trim().isEmpty() ? null : notesBuilder.toString().trim(),
            fournisseur != null ? fournisseur.createdAt() : null
        );
      }
      return null;
    });

    dialog.showAndWait().ifPresent(result -> {
      try {
        if (fournisseur == null) {
          // Ajout
          societeRepo.insert(result.type(), result.nom(), result.email(), result.phone(), result.adresse(), result.notes());
        } else {
          // Modification
          societeRepo.update(result.id(), result.type(), result.nom(), result.email(), result.phone(), result.adresse(), result.notes());
        }
        loadData();
        showInfo("Succès", fournisseur == null ? "Fournisseur ajouté avec succès." : "Fournisseur modifié avec succès.");
      } catch (Exception e) {
        showError("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
      }
    });
  }

  @FXML
  private void onDelete() {
    Societe selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un fournisseur à supprimer.");
      return;
    }

    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Confirmation");
    confirmation.setHeaderText("Supprimer le fournisseur");
    confirmation.setContentText("Êtes-vous sûr de vouloir supprimer \"" + selected.nom() + "\" ?\nCette action est irréversible.");

    if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
      try {
        societeRepo.delete(selected.id());
        loadData();
        showInfo("Suppression réussie", "Le fournisseur a été supprimé avec succès.");
      } catch (Exception e) {
        showError("Erreur lors de la suppression", e.getMessage());
      }
    }
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showWarning(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}