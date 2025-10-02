package com.magsav.gui.ops;

import com.magsav.model.CompanyService;
import com.magsav.model.PurchaseRFQ;
import com.magsav.repo.CompanyServiceRepository;
import com.magsav.repo.PurchaseRFQRepository;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javax.sql.DataSource;

public class PurchasesController implements Initializable {
  @FXML private TableView<PurchaseRFQ> table;
  @FXML private TableColumn<PurchaseRFQ, String> colId;
  @FXML private TableColumn<PurchaseRFQ, String> colService;
  @FXML private TableColumn<PurchaseRFQ, String> colProduit;
  @FXML private TableColumn<PurchaseRFQ, String> colPN;
  @FXML private TableColumn<PurchaseRFQ, String> colQty;
  @FXML private TableColumn<PurchaseRFQ, String> colStatus;
  @FXML private TableColumn<PurchaseRFQ, String> colRequested;
  @FXML private TableColumn<PurchaseRFQ, String> colResponded;
  @FXML private TableColumn<PurchaseRFQ, String> colPrice;
  @FXML private TextField searchField;
  @FXML private Button btnSearch;
  @FXML private Button btnClear;
  @FXML private Button btnNew;
  @FXML private Button btnRespond;

  private final PurchaseRFQRepository repo;
  private final javax.sql.DataSource ds;
  private final CompanyServiceRepository serviceRepo;
  private final ObservableList<PurchaseRFQ> data = FXCollections.observableArrayList();

  public PurchasesController(DataSource ds) {
    this.ds = ds;
    this.repo = new PurchaseRFQRepository(ds);
    this.serviceRepo = new CompanyServiceRepository(ds);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    colId.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().id())));
    colService.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                serviceName(cd.getValue().providerServiceId())));
    colProduit.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().produit())));
    colPN.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().partNumber())));
    colQty.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().quantity() == null ? "" : String.valueOf(cd.getValue().quantity())));
    colStatus.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().status())));
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    colRequested.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().requestedAt() == null
                    ? ""
                    : cd.getValue().requestedAt().format(fmt)));
    colResponded.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().respondedAt() == null
                    ? ""
                    : cd.getValue().respondedAt().format(fmt)));
    colPrice.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().price() == null
                    ? ""
                    : (cd.getValue().price() + " " + nz(cd.getValue().currency()))));
    table.setItems(data);
    reload();
    btnSearch.setOnAction(e -> doSearch());
    // Menu contextuel Edit/Supprimer
    ContextMenu ctx = new ContextMenu();
    MenuItem miEdit = new MenuItem("Éditer notes/quantité");
    MenuItem miDelete = new MenuItem("Supprimer");
    ctx.getItems().addAll(miEdit, miDelete);
    table.setRowFactory(
        tv -> {
          TableRow<PurchaseRFQ> row = new TableRow<>();
          row.itemProperty()
              .addListener((o, oldV, newV) -> row.setContextMenu(newV == null ? null : ctx));
          row.setOnContextMenuRequested(
              ev -> {
                if (!row.isEmpty()) {
                  table.getSelectionModel().select(row.getIndex());
                }
              });
          return row;
        });
    miEdit.setOnAction(e -> editSelected());
    miDelete.setOnAction(e -> deleteSelected());
    btnClear.setOnAction(
        e -> {
          searchField.clear();
          reload();
        });
    btnNew.setOnAction(e -> showNewDialog());
    if (btnRespond != null) {
      btnRespond.setOnAction(e -> markResponded());
      btnRespond.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    }
  }

  private void editSelected() {
    PurchaseRFQ sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) {
      return;
    }
    Dialog<EditRes> d = new Dialog<>();
    d.setTitle("Éditer RFQ");
    d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    TextField tfQty = new TextField(sel.quantity() == null ? "" : String.valueOf(sel.quantity()));
    TextArea taNotes = new TextArea(sel.notes() == null ? "" : sel.notes());
    taNotes.setPrefRowCount(3);
    GridPane gp = new GridPane();
    gp.setHgap(8);
    gp.setVgap(8);
    gp.addRow(0, new Label("Quantité"), tfQty);
    gp.addRow(1, new Label("Notes"), taNotes);
    d.getDialogPane().setContent(gp);
    d.setResultConverter(
        bt -> {
          if (bt == ButtonType.OK) {
            Integer q = null;
            String qtxt = tfQty.getText();
            if (qtxt != null && !qtxt.isBlank()) {
              try {
                q = Integer.parseInt(qtxt.trim());
              } catch (Exception ex) {
                showError("Entrée invalide", "Quantité invalide");
                return null;
              }
            }
            return new EditRes(q, taNotes.getText());
          }
          return null;
        });
    Optional<EditRes> res = d.showAndWait();
    res.ifPresent(
        r -> {
          try {
            new com.magsav.service.PurchaseService(new PurchaseRFQRepository(ds))
                .updateNotesAndQuantity(sel.id(), r.qty, r.notes);
            reload();
          } catch (Exception ex) {
            showError("Erreur", ex.getMessage());
          }
        });
  }

  private void deleteSelected() {
    PurchaseRFQ sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) {
      return;
    }
    Alert confirm =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "Supprimer la demande #" + sel.id() + " ?",
            ButtonType.CANCEL,
            ButtonType.OK);
    confirm.setHeaderText("Confirmer la suppression");
    Optional<ButtonType> r = confirm.showAndWait();
    if (r.isPresent() && r.get() == ButtonType.OK) {
      try {
        new com.magsav.service.PurchaseService(new PurchaseRFQRepository(ds)).deleteRFQ(sel.id());
        reload();
      } catch (Exception ex) {
        showError("Erreur", ex.getMessage());
      }
    }
  }

  private static class EditRes {
    final Integer qty;
    final String notes;

    EditRes(Integer q, String n) {
      this.qty = q;
      this.notes = n;
    }
  }

  private String nz(String s) {
    return s == null ? "" : s;
  }

  private String serviceName(Long id) {
    if (id == null) {
      return "";
    }
    try {
      Optional<CompanyService> cs = serviceRepo.findById(id);
      return cs.map(
              v -> v.name() == null || v.name().isBlank() ? (v.type() + " #" + v.id()) : v.name())
          .orElse("");
    } catch (SQLException e) {
      return "";
    }
  }

  private void reload() {
    try {
      data.setAll(repo.findAll());
    } catch (SQLException e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void doSearch() {
    String q = searchField.getText();
    if (q == null || q.isBlank()) {
      reload();
      return;
    }
    try {
      data.setAll(repo.search(q));
    } catch (SQLException e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void showNewDialog() {
    try {
      var loader =
          new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/ops/purchase-new.fxml"));
      loader.setControllerFactory(
          cls -> {
            try {
              if ("com.magsav.gui.ops.PurchaseNewController".equals(cls.getName())) {
                var ctor = cls.getDeclaredConstructor(javax.sql.DataSource.class);
                return ctor.newInstance(this.ds);
              }
              return cls.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      var root = loader.load();
      var stage = new javafx.stage.Stage();
      stage.setTitle("Nouvelle demande de devis");
      stage.initOwner(btnNew.getScene().getWindow());
      stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
      stage.setScene(new javafx.scene.Scene((javafx.scene.Parent) root));
      stage.showAndWait();
      // Après fermeture, recharger la liste
      reload();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void markResponded() {
    PurchaseRFQ selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showError("Aucune sélection", "Sélectionnez une demande avant de marquer la réponse.");
      return;
    }
    Dialog<Result> dialog = new Dialog<>();
    dialog.setTitle("Réponse reçue");
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    TextField tfPrice = new TextField();
    tfPrice.setPromptText("Prix");
    ComboBox<String> cbCurrency = new ComboBox<>();
    cbCurrency.getItems().setAll("EUR", "USD", "GBP");
    cbCurrency.setEditable(true);
    cbCurrency.setValue("EUR");
    GridPane gp = new GridPane();
    gp.setHgap(8);
    gp.setVgap(8);
    gp.addRow(0, new Label("Prix"), tfPrice);
    gp.addRow(1, new Label("Devise"), cbCurrency);
    dialog.getDialogPane().setContent(gp);
    dialog.setResultConverter(
        bt -> {
          if (bt == ButtonType.OK) {
            try {
              Double p = Double.parseDouble(tfPrice.getText().trim());
              return new Result(p, cbCurrency.getValue());
            } catch (Exception ex) {
              showError("Entrée invalide", "Veuillez saisir un prix valide.");
              return null;
            }
          }
          return null;
        });
    Optional<Result> res = dialog.showAndWait();
    res.ifPresent(
        r -> {
          try {
            new com.magsav.service.PurchaseService(new PurchaseRFQRepository(ds))
                .markResponded(selected.id(), r.price, r.currency);
            reload();
          } catch (Exception e) {
            showError("Erreur", e.getMessage());
          }
        });
  }

  private static class Result {
    final Double price;
    final String currency;

    Result(Double p, String c) {
      this.price = p;
      this.currency = c;
    }
  }

  private void showError(String title, String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    a.setHeaderText(title);
    a.showAndWait();
  }
}
