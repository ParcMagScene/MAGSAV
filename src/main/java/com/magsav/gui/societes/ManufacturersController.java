package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;

public class ManufacturersController implements Initializable {

  @FXML private TextField tfSearch;
  @FXML private TableView<Societe> table;
  @FXML private TableColumn<Societe, Long> colId;
  @FXML private TableColumn<Societe, String> colNom;
  @FXML private TableColumn<Societe, String> colEmail;
  @FXML private TableColumn<Societe, String> colPhone;
  @FXML private TableColumn<Societe, String> colNotes;

  private final SocieteRepository societeRepo = new SocieteRepository();
  private final ObservableList<Societe> data = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    // Configuration des colonnes avec des cell value factories compatibles avec les records
    colId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().id()));
    colNom.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().nom()));
    colEmail.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().email()));
    colPhone.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().phone()));
    colNotes.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().notes()));

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
      data.addAll(societeRepo.findByType("FABRICANT"));
    } catch (Exception e) {
      showError("Erreur lors du chargement des fabricants", e.getMessage());
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
      data.addAll(societeRepo.findByType("FABRICANT").stream()
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
    try {
      // Ouvrir le formulaire d'ajout avec null (nouveau fabricant)
      var result = com.magsav.ui.components.FormDialogManager.MAGSAV.showManufacturerDialog(null, table.getScene().getWindow());
      if (result.isSaved()) {
        loadData();
      }
    } catch (Exception e) {
      showError("Erreur lors de l'ouverture du formulaire", e.getMessage());
    }
  }

  @FXML
  private void onEdit() {
    Societe selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un fabricant à modifier.");
      return;
    }

    try {
      // Ouvrir le formulaire de modification avec la société sélectionnée
      var result = com.magsav.ui.components.FormDialogManager.MAGSAV.showManufacturerDialog(selected, table.getScene().getWindow());
      if (result.isSaved()) {
        loadData();
      }
    } catch (Exception e) {
      showError("Erreur lors de l'ouverture du formulaire", e.getMessage());
    }
  }

  @FXML
  private void onDelete() {
    Societe selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showWarning("Aucune sélection", "Veuillez sélectionner un fabricant à supprimer.");
      return;
    }

    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Confirmation");
    confirmation.setHeaderText("Supprimer le fabricant");
    confirmation.setContentText("Êtes-vous sûr de vouloir supprimer \"" + selected.nom() + "\" ?\nCette action est irréversible.");

    if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
      try {
        societeRepo.delete(selected.id());
        loadData();
        showInfo("Suppression réussie", "Le fabricant a été supprimé avec succès.");
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