package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.util.Optional;

public class ClientsController {
  private static final String TYPE = "CLIENT";

  @FXML private TableView<Societe> table;
  @FXML private TableColumn<Societe, String> colId;
  @FXML private TableColumn<Societe, String> colNom;
  @FXML private TableColumn<Societe, String> colEmail;
  @FXML private TableColumn<Societe, String> colPhone;
  @FXML private TableColumn<Societe, String> colNotes;
  @FXML private TextField tfSearch;

  private final SocieteRepository repo = new SocieteRepository();
  private final ObservableList<Societe> master = FXCollections.observableArrayList();
  private FilteredList<Societe> filtered;

  @FXML
  private void initialize() {
    colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
    colNom.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().nom())));
    colEmail.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().email())));
    colPhone.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().phone())));
    colNotes.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().notes())));

    filtered = new FilteredList<>(master, it -> true);
    SortedList<Societe> sorted = new SortedList<>(filtered);
    sorted.comparatorProperty().bind(table.comparatorProperty());
    table.setItems(sorted);

    if (tfSearch != null) tfSearch.textProperty().addListener((o, a, b) -> applyFilter(b));
    onRefresh();
  }

  @FXML private void onRefresh() {
    master.setAll(repo.findByType(TYPE));
    applyFilter(tfSearch == null ? "" : tfSearch.getText());
  }

  @FXML private void onAdd() { openForm(null); }

  @FXML private void onEdit() {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel != null) openForm(sel);
  }

  @FXML private void onDelete() {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) return;
    var confirm = new Alert(Alert.AlertType.CONFIRMATION,
        "Supprimer le client \"" + sel.nom() + "\" ?").showAndWait();
    if (confirm.isPresent() && confirm.get().getButtonData().isDefaultButton()) {
      if (!repo.delete(sel.id())) new Alert(Alert.AlertType.WARNING, "Rien n’a été supprimé.").showAndWait();
      onRefresh();
    }
  }

  private void openForm(Societe current) {
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/societes/manufacturer_form.fxml"));
      DialogPane pane = l.load();
      ManufacturerFormController ctl = l.getController();
      ctl.init(current);

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle(current == null ? "Ajouter un client" : "Modifier le client");
      d.setDialogPane(pane);
      Optional<ButtonType> res = d.showAndWait();
      if (res.isPresent() && res.get().getButtonData().isDefaultButton()) {
        if (!ctl.isValid()) {
          new Alert(Alert.AlertType.WARNING, "Le nom est requis (≥ 2 caractères).").showAndWait();
          return;
        }
        if (current == null) {
          repo.insert(TYPE, ctl.nom(), ctl.email(), ctl.phone(), ctl.adresse(), ctl.notes());
        } else {
          repo.update(new Societe(current.id(), TYPE, ctl.nom(), ctl.email(), ctl.phone(), ctl.adresse(), ctl.notes(), current.createdAt()));
        }
        onRefresh();
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur formulaire: " + e.getMessage()).showAndWait();
    }
  }

  private void applyFilter(String q) {
    String s = q == null ? "" : q.trim().toLowerCase();
    filtered.setPredicate(v -> {
      if (s.isEmpty()) return true;
      return String.valueOf(v.id()).contains(s)
          || orEmpty(v.nom()).toLowerCase().contains(s)
          || orEmpty(v.email()).toLowerCase().contains(s)
          || orEmpty(v.phone()).toLowerCase().contains(s)
          || orEmpty(v.notes()).toLowerCase().contains(s);
    });
  }

  private static String orEmpty(String x) { return x == null ? "" : x; }
}