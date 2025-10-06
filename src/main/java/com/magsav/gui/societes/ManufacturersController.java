package com.magsav.gui.societes;

import com.magsav.util.Views;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
// Ajoutez l'import de votre modèle Societe
import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.util.Views;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import java.util.Optional;

public class ManufacturersController {
  private static final String TYPE = "FABRICANT";

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

    // Double‑clic: ouvre la fiche fabricant
    if (table != null) {
      table.setRowFactory(tv -> {
        TableRow<Societe> r = new TableRow<>();
        r.setOnMouseClicked(e -> {
          if (!r.isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
            Societe s = r.getItem();
            String fabricant = s == null ? null : nomSociete(s);
            if (fabricant != null && !fabricant.isBlank()) {
              Views.openManufacturer(fabricant);
            }
          }
        });
        return r;
      });
    }
  }

  // Utilitaire: récupère le nom d’un Societe (compatibilité getters/records)
  private String nomSociete(Societe s) {
    try { return (String) s.getClass().getMethod("nom").invoke(s); } catch (Exception ignored) {}
    try { return (String) s.getClass().getMethod("getNom").invoke(s); } catch (Exception ignored) {}
    return null;
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
        "Supprimer le fabricant \"" + sel.nom() + "\" ?").showAndWait();
    if (confirm.isPresent() && confirm.get().getButtonData().isDefaultButton()) {
      if (!repo.delete(sel.id())) new Alert(Alert.AlertType.WARNING, "Rien n’a été supprimé.").showAndWait();
      onRefresh();
    }
  }

  private void openForm(Societe current) {
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/societes/manufacturer_form.fxml"));
      javafx.scene.Parent root = l.load();                 // <-- Parent au lieu de DialogPane
      ManufacturerFormController ctl = l.getController();
      ctl.init(current);

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle(current == null ? "Ajouter un fabricant" : "Modifier le fabricant");
      d.getDialogPane().setContent(root);                  // <-- on place le contenu
      d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL); // <-- boutons

      Optional<ButtonType> res = d.showAndWait();
      if (res.isPresent() && res.get() == ButtonType.OK) { // <-- on teste OK explicite
        String nom = ctl.nom() == null ? "" : ctl.nom().trim();
        if (nom.length() < 2) {
          new Alert(Alert.AlertType.WARNING, "Nom requis (au moins 2 caractères).").showAndWait();
          return;
        }
        nom = nom.replaceAll("\\s+", " ");

        var exists = repo.findByNameAndTypeIgnoreCase(nom, "FABRICANT");
        if (exists.isPresent() && (current == null || exists.get().id() != current.id())) {
          new Alert(Alert.AlertType.INFORMATION, "Fabricant déjà existant: " + exists.get().nom()).showAndWait();
          return;
        }

        if (current == null) {
          repo.insert("FABRICANT", nom, ctl.email(), ctl.phone(), ctl.adresse(), ctl.notes());
        } else {
          // Mise à jour directe sans created_at
          repo.update(current.id(), "FABRICANT", nom, ctl.email(), ctl.phone(), ctl.adresse(), ctl.notes());
        }
        onRefresh();
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur formulaire: " + e.getClass().getSimpleName() + ": " + e.getMessage()).showAndWait();
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