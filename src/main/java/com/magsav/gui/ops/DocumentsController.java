package com.magsav.gui.ops;

import com.magsav.model.DocumentEntry;
import com.magsav.repo.DocumentRepository;
import com.magsav.service.DocumentService;
import java.awt.Desktop;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javax.sql.DataSource;

public class DocumentsController implements Initializable {
  @FXML private TableView<DocumentEntry> table;
  @FXML private TableColumn<DocumentEntry, String> colType;
  @FXML private TableColumn<DocumentEntry, String> colNorm;
  @FXML private TableColumn<DocumentEntry, String> colOrig;
  @FXML private TableColumn<DocumentEntry, String> colPath;
  @FXML private TableColumn<DocumentEntry, String> colLinked;
  @FXML private TableColumn<DocumentEntry, String> colCreated;
  @FXML private ComboBox<String> cbType;
  @FXML private TextField tfCode;
  @FXML private TextField tfSN;
  @FXML private Button btnSearch;
  @FXML private Button btnClear;
  @FXML private Button btnImport;

  // DataSource réservé pour évolutions futures (liens entités)
  private final DocumentRepository repo;
  private final ObservableList<DocumentEntry> data = FXCollections.observableArrayList();

  public DocumentsController(DataSource ds) {
    this.repo = new DocumentRepository(ds);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    colType.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().type()));
    colNorm.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().normalizedName()));
    colOrig.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().originalName()));
    colPath.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().path()));
    colLinked.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(linkText(cd.getValue())));
    colCreated.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().createdAt() == null ? "" : cd.getValue().createdAt().format(fmt)));
    table.setItems(data);
    table.setRowFactory(
        tv -> {
          TableRow<DocumentEntry> row = new TableRow<>();
          row.setOnMouseClicked(
              e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                  openFile(row.getItem());
                }
              });
          return row;
        });
    cbType.getItems().setAll("TOUS", "DEV", "FACT", "RMA", "BL", "BC");
    cbType.setValue("TOUS");
    btnSearch.setOnAction(e -> doSearch());
    btnClear.setOnAction(
        e -> {
          tfCode.clear();
          tfSN.clear();
          cbType.setValue("TOUS");
          reload();
        });
    btnImport.setOnAction(e -> doImport());
    reload();
  }

  private void reload() {
    try {
      data.setAll(repo.findAll());
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void doSearch() {
    String code = tfCode.getText();
    String sn = tfSN.getText();
    String t = cbType.getValue();
    try {
      if ((code != null && !code.isBlank()) || (sn != null && !sn.isBlank())) {
        data.setAll(repo.searchByLink(code, sn));
      } else {
        var all = repo.findAll();
        if (t != null && !"TOUS".equals(t)) {
          all.removeIf(d -> !t.equalsIgnoreCase(d.type()));
        }
        data.setAll(all);
      }
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void doImport() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Importer un document");
    java.io.File file = chooser.showOpenDialog(btnImport.getScene().getWindow());
    if (file == null) {
      return;
    }

    Dialog<ImportRes> d = new Dialog<>();
    d.setTitle("Options d'import");
    d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    ComboBox<String> cbT = new ComboBox<>();
    cbT.getItems().setAll("DEV", "FACT", "RMA", "BL", "BC");
    cbT.setValue("DEV");
    TextField tfSupplier = new TextField();
    tfSupplier.setPromptText("Fournisseur (optionnel)");
    DatePicker dpDate = new DatePicker();
    dpDate.setPromptText("Date (optionnelle)");
    TextField tfLinkCode = new TextField();
    tfLinkCode.setPromptText("Code produit lié (optionnel)");
    TextField tfLinkSn = new TextField();
    tfLinkSn.setPromptText("N° série lié (optionnel)");
    GridPane gp = new GridPane();
    gp.setHgap(8);
    gp.setVgap(8);
    gp.addRow(0, new Label("Type"), cbT);
    gp.addRow(1, new Label("Fournisseur"), tfSupplier);
    gp.addRow(2, new Label("Date"), dpDate);
    gp.addRow(3, new Label("Code produit"), tfLinkCode);
    gp.addRow(4, new Label("N° série"), tfLinkSn);
    d.getDialogPane().setContent(gp);
    d.setResultConverter(
        bt -> {
          if (bt == ButtonType.OK) {
            return new ImportRes(
                cbT.getValue(),
                tfSupplier.getText(),
                dpDate.getValue(),
                tfLinkCode.getText(),
                tfLinkSn.getText());
          }
          return null;
        });
    Optional<ImportRes> res = d.showAndWait();
    if (res.isEmpty()) {
      return;
    }
    ImportRes r = res.get();
    try {
      Path appRoot = Paths.get(""); // workspace root
      var svc = new DocumentService(this.repo, appRoot.resolve("documents"));
      DocumentService.DocType type = DocumentService.DocType.valueOf(r.type);
      svc.importAndIndex(
          file.toPath(),
          type,
          r.supplier == null || r.supplier.isBlank() ? null : r.supplier,
          r.date,
          emptyToNull(r.linkCode),
          emptyToNull(r.linkSn),
          null,
          null,
          null);
      reload();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void openFile(DocumentEntry d) {
    try {
      Path p = Paths.get("documents").resolve(d.path());
      if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().open(p.toFile());
      }
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private String linkText(DocumentEntry d) {
    StringBuilder sb = new StringBuilder();
    if (d.linkedProductCode() != null) {
      sb.append("Code:").append(d.linkedProductCode()).append(' ');
    }
    if (d.linkedNumeroSerie() != null) {
      sb.append("SN:").append(d.linkedNumeroSerie()).append(' ');
    }
    if (d.linkedDossierId() != null) {
      sb.append("Dossier#").append(d.linkedDossierId()).append(' ');
    }
    if (d.linkedRfqId() != null) {
      sb.append("RFQ#").append(d.linkedRfqId()).append(' ');
    }
    if (d.linkedRmaId() != null) {
      sb.append("RMA#").append(d.linkedRmaId()).append(' ');
    }
    return sb.toString().trim();
  }

  private static String emptyToNull(String s) {
    return s == null || s.isBlank() ? null : s;
  }

  private void showError(String title, String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    a.setHeaderText(title);
    a.showAndWait();
  }

  private record ImportRes(
      String type, String supplier, LocalDate date, String linkCode, String linkSn) {}
}
