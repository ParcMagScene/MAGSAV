package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.service.AddressService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ExternalSavController implements Initializable {

  @FXML private TextField tfSearch;
  @FXML private TableView<Societe> table;
  @FXML private TableColumn<Societe, Long> colId;
  @FXML private TableColumn<Societe, String> colNom;
  @FXML private TableColumn<Societe, String> colEmail;
  @FXML private TableColumn<Societe, String> colPhone;
  @FXML private TableColumn<Societe, String> colNotes;

  // Table des produits en SAV Externe
  @FXML private TableView<ProductRepository.ProductRow> productsTable;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdNom;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdSN;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdUID;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdFabricant;
  @FXML private TableColumn<ProductRepository.ProductRow, String> colProdSituation;

  private final SocieteRepository societeRepo = new SocieteRepository();
  private final ProductRepository productRepo = new ProductRepository();
  private final AddressService addressService = new AddressService();
  private final ObservableList<Societe> masterData = FXCollections.observableArrayList();
  private final ObservableList<ProductRepository.ProductRow> productsData = FXCollections.observableArrayList();
  private FilteredList<Societe> filteredData;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Configuration des colonnes des SAV externes
    colId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().id()));
    colNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().nom()));
    colEmail.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().email()));
    colPhone.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().phone()));
    colNotes.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().notes()));

    // Configuration des colonnes des produits
    colProdNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().nom()));
    colProdSN.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().sn()));
    colProdUID.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().uid()));
    colProdFabricant.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().fabricant()));
    colProdSituation.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().situation()));

    // Configuration de la liste filtrée
    filteredData = new FilteredList<>(masterData, p -> true);
    table.setItems(filteredData);
    productsTable.setItems(productsData);

    // Configuration de la recherche
    tfSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData(newVal));

    // Gestion de la sélection dans la table des SAV externes
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
      if (newSelection != null) {
        loadProductsForSav(newSelection.id());
      } else {
        loadProductsData(); // Afficher tous les produits SAV Externe
      }
    });

    // Gestion du double-clic sur un SAV externe
    table.setRowFactory(tv -> {
      TableRow<Societe> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          openSavDetail(row.getItem());
        }
      });
      return row;
    });

    // Chargement initial
    loadData();
    loadProductsData();
  }

  private void loadData() {
    try {
      masterData.clear();
      // Charger toutes les sociétés de type SAV/RMA externes
      masterData.addAll(societeRepo.findByType("SAV_EXTERNE"));
    } catch (Exception e) {
      showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
    }
  }

  private void loadProductsData() {
    try {
      productsData.clear();
      // Charger tous les produits en SAV Externe
      productsData.addAll(productRepo.findBySituationCompatible("SAV Externe"));
    } catch (Exception e) {
      showError("Erreur de chargement", "Impossible de charger les produits: " + e.getMessage());
    }
  }

  private void loadProductsForSav(long savExterneId) {
    try {
      productsData.clear();
      // Charger uniquement les produits en SAV Externe pour ce SAV spécifique
      productsData.addAll(productRepo.findBySavExterneCompatible(savExterneId));
    } catch (Exception e) {
      showError("Erreur de chargement", "Impossible de charger les produits pour ce SAV: " + e.getMessage());
    }
  }

  private void openSavDetail(Societe sav) {
    try {
      // Charger la fiche détaillée du SAV externe
      javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
        getClass().getResource("/fxml/societes/sav_detail.fxml")
      );
      
      javafx.scene.Parent root = loader.load();
      SavDetailController controller = loader.getController();
      controller.setSav(sav);
      
      // Créer et afficher la fenêtre
      javafx.stage.Stage stage = new javafx.stage.Stage();
      stage.setTitle("SAV Externe - " + sav.nom());
      stage.setScene(new javafx.scene.Scene(root));
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.initOwner(table.getScene().getWindow());
      
      // Taille et positionnement
      stage.setMinWidth(800);
      stage.setMinHeight(600);
      stage.centerOnScreen();
      
      stage.show();
      
    } catch (Exception e) {
      e.printStackTrace();
      showError("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
    }
  }

  private void filterData(String searchText) {
    if (searchText == null || searchText.trim().isEmpty()) {
      filteredData.setPredicate(societe -> true);
    } else {
      String lowerCaseFilter = searchText.toLowerCase().trim();
      filteredData.setPredicate(societe -> {
        // Recherche dans nom, email, téléphone, et notes
        return (societe.nom() != null && societe.nom().toLowerCase().contains(lowerCaseFilter)) ||
               (societe.email() != null && societe.email().toLowerCase().contains(lowerCaseFilter)) ||
               (societe.phone() != null && societe.phone().toLowerCase().contains(lowerCaseFilter)) ||
               (societe.notes() != null && societe.notes().toLowerCase().contains(lowerCaseFilter));
      });
    }
  }

  @FXML
  private void onRefresh() {
    loadData();
    loadProductsData();
  }

  @FXML
  private void onAdd() {
    // Créer un formulaire d'ajout de SAV externe
    Dialog<Societe> dialog = new Dialog<>();
    dialog.setTitle("Ajouter un SAV externe");
    dialog.setHeaderText("Informations du nouveau SAV externe");

    // Boutons du dialog
    ButtonType okButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

    // Créer les champs du formulaire
    TextField nomField = new TextField();
    nomField.setPromptText("Nom du SAV");
    TextField emailField = new TextField();
    emailField.setPromptText("Email");
    TextField phoneField = new TextField();
    phoneField.setPromptText("Téléphone");
    TextField adresseField = new TextField();
    adresseField.setPromptText("Adresse");
    
    // Ajouter l'autocomplétion d'adresse
    addressService.setupAddressAutocomplete(adresseField);
    TextArea notesArea = new TextArea();
    notesArea.setPromptText("Notes");
    notesArea.setPrefRowCount(3);

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
    grid.add(new Label("Notes:"), 0, 4);
    grid.add(notesArea, 1, 4);

    dialog.getDialogPane().setContent(grid);

    // Validation et focus
    nomField.requestFocus();
    javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(okButtonType);
    okButton.setDisable(true);
    nomField.textProperty().addListener((obs, oldVal, newVal) -> {
      okButton.setDisable(newVal.trim().isEmpty());
    });

    // Convertir le résultat
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == okButtonType) {
        try {
          long id = societeRepo.insert(
              "SAV_EXTERNE",
              nomField.getText().trim(),
              emailField.getText().trim(),
              phoneField.getText().trim(),
              adresseField.getText().trim(),
              notesArea.getText().trim()
          );
          return new Societe(id, "SAV_EXTERNE", nomField.getText().trim(), 
                             emailField.getText().trim(), phoneField.getText().trim(),
                             adresseField.getText().trim(), notesArea.getText().trim(), "");
        } catch (Exception e) {
          showError("Erreur d'ajout", "Impossible d'ajouter le SAV externe: " + e.getMessage());
          return null;
        }
      }
      return null;
    });

    // Afficher le dialog et traiter le résultat
    dialog.showAndWait().ifPresent(nouveauSav -> {
      if (nouveauSav != null) {
        loadData(); // Recharger la liste
        showInfo("SAV externe ajouté", "Le SAV externe '" + nouveauSav.nom() + "' a été ajouté avec succès.");
      }
    });
  }

  @FXML
  private void onEdit() {
    Societe selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un SAV externe à modifier.");
      return;
    }

    // Créer un formulaire d'édition de SAV externe
    Dialog<Societe> dialog = new Dialog<>();
    dialog.setTitle("Modifier un SAV externe");
    dialog.setHeaderText("Modifier les informations du SAV externe");

    // Boutons du dialog
    ButtonType okButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

    // Créer les champs du formulaire avec les valeurs actuelles
    TextField nomField = new TextField(selected.nom());
    TextField emailField = new TextField(selected.email());
    TextField phoneField = new TextField(selected.phone());
    TextField adresseField = new TextField(selected.adresse());
    
    // Ajouter l'autocomplétion d'adresse pour la modification aussi
    addressService.setupAddressAutocomplete(adresseField);
    
    TextArea notesArea = new TextArea(selected.notes());
    notesArea.setPrefRowCount(3);

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
    grid.add(new Label("Notes:"), 0, 4);
    grid.add(notesArea, 1, 4);

    dialog.getDialogPane().setContent(grid);

    // Validation
    javafx.scene.control.Button okButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(okButtonType);
    okButton.setDisable(false);
    nomField.textProperty().addListener((obs, oldVal, newVal) -> {
      okButton.setDisable(newVal.trim().isEmpty());
    });

    // Convertir le résultat
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == okButtonType) {
        try {
          societeRepo.update(
              selected.id(),
              "SAV_EXTERNE",
              nomField.getText().trim(),
              emailField.getText().trim(),
              phoneField.getText().trim(),
              adresseField.getText().trim(),
              notesArea.getText().trim()
          );
          return new Societe(selected.id(), "SAV_EXTERNE", nomField.getText().trim(), 
                             emailField.getText().trim(), phoneField.getText().trim(),
                             adresseField.getText().trim(), notesArea.getText().trim(), selected.createdAt());
        } catch (Exception e) {
          showError("Erreur de modification", "Impossible de modifier le SAV externe: " + e.getMessage());
          return null;
        }
      }
      return null;
    });

    // Afficher le dialog et traiter le résultat
    dialog.showAndWait().ifPresent(savModifie -> {
      if (savModifie != null) {
        loadData(); // Recharger la liste
        showInfo("SAV externe modifié", "Le SAV externe '" + savModifie.nom() + "' a été modifié avec succès.");
      }
    });
  }

  @FXML
  private void onDelete() {
    Societe selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un SAV externe à supprimer.");
      return;
    }

    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Confirmation");
    confirmation.setHeaderText("Supprimer ce SAV externe ?");
    confirmation.setContentText("Cette action est irréversible.");

    if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
      try {
        societeRepo.delete(selected.id());
        loadData();
        showInfo("Suppression réussie", "Le SAV externe a été supprimé.");
      } catch (Exception e) {
        showError("Erreur de suppression", "Impossible de supprimer: " + e.getMessage());
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