package com.magsav.gui.requests;

import com.magsav.model.RequestItem;
import com.magsav.model.RequestRow;
import com.magsav.model.Societe;
import com.magsav.repo.RequestRepository;
import com.magsav.repo.SocieteRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class PartRequestsController {
  private static final String TYPE = "PIECES";

  @FXML private TableView<RequestRow> table;
  @FXML private TableColumn<RequestRow, String> colId;
  @FXML private TableColumn<RequestRow, String> colStatus;
  @FXML private TableColumn<RequestRow, String> colFournisseur;
  @FXML private TableColumn<RequestRow, String> colComment;
  @FXML private TableColumn<RequestRow, String> colCreated;
  @FXML private TableColumn<RequestRow, String> colValidated;

  @FXML private TableView<RequestItem> itemsTable;
  @FXML private TableColumn<RequestItem, String> colRef;
  @FXML private TableColumn<RequestItem, String> colQty;
  @FXML private TableColumn<RequestItem, String> colDesc;

  @FXML private TextField tfSearch;

  private final RequestRepository repo = new RequestRepository();
  private final SocieteRepository socRepo = new SocieteRepository();

  private final ObservableList<RequestRow> master = FXCollections.observableArrayList();
  private FilteredList<RequestRow> filtered;
  private final ObservableList<RequestItem> items = FXCollections.observableArrayList();

  @FXML
  private void initialize() {
    colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().id())));
    colStatus.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().status())));
    colFournisseur.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().fournisseurNom())));
    colComment.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().commentaire())));
    colCreated.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().createdAt())));
    colValidated.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().validatedAt())));

    filtered = new FilteredList<>(master, it -> true);
    SortedList<RequestRow> sorted = new SortedList<>(filtered);
    sorted.comparatorProperty().bind(table.comparatorProperty());
    table.setItems(sorted);

    colRef.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().ref())));
    colQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().qty())));
    colDesc.setCellValueFactory(c -> new SimpleStringProperty(orEmpty(c.getValue().description())));
    itemsTable.setItems(items);

    table.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> loadItems());
    if (tfSearch != null) tfSearch.textProperty().addListener((o, a, b) -> applyFilter(b));

    onRefresh();
  }

  @FXML private void onRefresh() {
    try {
      master.setAll(repo.list(TYPE));
      applyFilter(tfSearch == null ? "" : tfSearch.getText());
      loadItems();
    } catch (Exception e) {
      // Gestion gracieuse des erreurs - afficher message a l'utilisateur
      System.err.println("Erreur lors du rafraichissement des demandes de pieces: " + e.getMessage());
      master.clear(); // Vider la liste en cas d'erreur
      items.clear();
    }
  }

  private void loadItems() {
    RequestRow sel = table.getSelectionModel().getSelectedItem();
    items.setAll(sel == null ? List.of() : repo.items(sel.id()));
  }

  @FXML private void onNew() { openForm(null); }

  @FXML private void onEdit() {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel != null) openForm(sel);
  }

  @FXML private void onDelete() {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) return;
    var confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la demande #" + sel.id() + " ?").showAndWait();
    if (confirm.isPresent() && confirm.get().getButtonData().isDefaultButton()) {
      if (!repo.delete(sel.id())) new Alert(Alert.AlertType.WARNING, "Rien n’a été supprimé.").showAndWait();
      onRefresh();
    }
  }

  @FXML private void onValidate() { changeStatus("VALIDEE"); }
  @FXML private void onAskQuote() { changeStatus("DEMANDE_DEVIS"); }
  @FXML private void onOrder()    { changeStatus("COMMANDEE"); }

  private void changeStatus(String st) {
    var sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) return;
    repo.updateStatus(sel.id(), st);
    onRefresh();
  }

  private void openForm(RequestRow current) {
    try {
      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/requests/forms/request_form.fxml"));
      DialogPane pane = l.load();
      RequestFormController ctl = l.getController();

      List<Societe> fournisseurs = socRepo.findByType("FOURNISSEUR");
      Societe currentFour = null;
      if (current != null && current.fournisseurNom() != null) {
        for (Societe s : fournisseurs) if (s.nom().equals(current.fournisseurNom())) { currentFour = s; break; }
      }
      ctl.init(fournisseurs, currentFour, current == null ? "" : current.commentaire());

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle(current == null ? "Nouvelle demande (pièces)" : "Modifier la demande #" + current.id());
      d.setDialogPane(pane);
      Optional<ButtonType> res = d.showAndWait();
      if (res.isPresent() && res.get().getButtonData().isDefaultButton()) {
        if (current == null) {
          long id = repo.create(TYPE, ctl.commentaire(), ctl.fournisseurId());
          onRefresh();
          // sélectionner la nouvelle demande
          table.getItems().stream().filter(r -> r.id() == id).findFirst().ifPresent(r -> table.getSelectionModel().select(r));
        } else {
          repo.update(current.id(), ctl.commentaire(), ctl.fournisseurId());
          onRefresh();
        }
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur formulaire: " + e.getMessage()).showAndWait();
    }
  }

  @FXML private void onAddItem() { openItemForm(null); }
  @FXML private void onEditItem() {
    var it = itemsTable.getSelectionModel().getSelectedItem();
    if (it != null) openItemForm(it);
  }
  @FXML private void onDeleteItem() {
    var it = itemsTable.getSelectionModel().getSelectedItem();
    if (it == null) return;
    if (new Alert(Alert.AlertType.CONFIRMATION, "Supprimer l’article \"" + it.ref() + "\" ?").showAndWait()
        .filter(b -> b.getButtonData().isDefaultButton()).isPresent()) {
      if (!repo.deleteItem(it.id())) new Alert(Alert.AlertType.WARNING, "Rien n’a été supprimé.").showAndWait();
      loadItems();
    }
  }

  private void openItemForm(RequestItem current) {
    try {
      var sel = table.getSelectionModel().getSelectedItem();
      if (sel == null) return;

      FXMLLoader l = new FXMLLoader(getClass().getResource("/fxml/requests/forms/request_item_form.fxml"));
      DialogPane pane = l.load();
      RequestItemFormController ctl = l.getController();
      ctl.init(current);

      Dialog<ButtonType> d = new Dialog<>();
      d.setTitle(current == null ? "Ajouter un article" : "Modifier l’article");
      d.setDialogPane(pane);
      Optional<ButtonType> res = d.showAndWait();
      if (res.isPresent() && res.get().getButtonData().isDefaultButton()) {
        if (!ctl.isValid()) {
          new Alert(Alert.AlertType.WARNING, "Référence (≥ 2) et quantité (>0) requises.").showAndWait();
          return;
        }
        if (current == null) {
          repo.addItem(sel.id(), ctl.ref(), ctl.qty(), ctl.description());
        } else {
          repo.updateItem(new RequestItem(current.id(), current.requestId(), ctl.ref(), ctl.qty(), ctl.description()));
        }
        loadItems();
      }
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur article: " + e.getMessage()).showAndWait();
    }
  }

  private void applyFilter(String q) {
    String s = q == null ? "" : q.trim().toLowerCase();
    filtered.setPredicate(r -> {
      if (s.isEmpty()) return true;
      return String.valueOf(r.id()).contains(s)
          || orEmpty(r.status()).toLowerCase().contains(s)
          || orEmpty(r.fournisseurNom()).toLowerCase().contains(s)
          || orEmpty(r.commentaire()).toLowerCase().contains(s)
          || orEmpty(r.createdAt()).toLowerCase().contains(s)
          || orEmpty(r.validatedAt()).toLowerCase().contains(s);
    });
  }
  private static String orEmpty(String x) { return x == null ? "" : x; }
}