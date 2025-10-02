package com.magsav.gui.ops;

import com.magsav.model.CompanyService;
import com.magsav.model.Manufacturer;
import com.magsav.model.RmaRequest;
import com.magsav.repo.CompanyServiceRepository;
import com.magsav.repo.ManufacturerServiceLinkRepository;
import com.magsav.repo.RmaRequestRepository;
import com.magsav.service.RmaService;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javax.sql.DataSource;

public class RmaController implements Initializable {
  @FXML private TableView<RmaRequest> table;
  @FXML private TableColumn<RmaRequest, String> colId;
  @FXML private TableColumn<RmaRequest, String> colService;
  @FXML private TableColumn<RmaRequest, String> colProduit;
  @FXML private TableColumn<RmaRequest, String> colSN;
  @FXML private TableColumn<RmaRequest, String> colCode;
  @FXML private TableColumn<RmaRequest, String> colReason;
  @FXML private TableColumn<RmaRequest, String> colStatus;
  @FXML private TableColumn<RmaRequest, String> colRma;
  @FXML private TableColumn<RmaRequest, String> colCreated;
  @FXML private TextField searchField;
  @FXML private Button btnSearch;
  @FXML private Button btnClear;
  @FXML private Button btnNew;
  @FXML private Button btnAssign;

  private final DataSource ds;
  private final RmaRequestRepository repo;
  private final CompanyServiceRepository serviceRepo;
  private final ManufacturerServiceLinkRepository manufLinkRepo;
  private final RmaService rmaService;
  private final ObservableList<RmaRequest> data = FXCollections.observableArrayList();

  public RmaController(DataSource ds) {
    this.ds = ds;
    this.repo = new RmaRequestRepository(ds);
    this.serviceRepo = new CompanyServiceRepository(ds);
    this.rmaService = new RmaService(new RmaRequestRepository(ds));
    this.manufLinkRepo = new ManufacturerServiceLinkRepository(ds);
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    colId.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cd.getValue().id())));
    colService.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                serviceName(cd.getValue().providerServiceId())));
    colProduit.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().produit())));
    colSN.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().numeroSerie())));
    colCode.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().codeProduit())));
    colReason.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().reason())));
    colStatus.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().status())));
    colRma.setCellValueFactory(
        cd -> new javafx.beans.property.SimpleStringProperty(nz(cd.getValue().rmaNumber())));
    colCreated.setCellValueFactory(
        cd ->
            new javafx.beans.property.SimpleStringProperty(
                cd.getValue().createdAt() == null ? "" : cd.getValue().createdAt().format(fmt)));
    table.setItems(data);
    table.setRowFactory(
        tv -> {
          TableRow<RmaRequest> row = new TableRow<>();
          row.setOnMouseClicked(
              evt -> {
                if (evt.getClickCount() == 2 && !row.isEmpty()) {
                  editSelected(row.getItem());
                }
              });
          ContextMenu cm = new ContextMenu();
          MenuItem miEdit = new MenuItem("Modifier…");
          miEdit.setOnAction(e -> editSelected(row.getItem()));
          MenuItem miDelete = new MenuItem("Supprimer");
          miDelete.setOnAction(e -> deleteSelected(row.getItem()));
          cm.getItems().addAll(miEdit, miDelete);
          row.contextMenuProperty()
              .bind(
                  javafx.beans.binding.Bindings.when(row.emptyProperty())
                      .then((ContextMenu) null)
                      .otherwise(cm));
          return row;
        });
    reload();
    btnSearch.setOnAction(e -> doSearch());
    btnClear.setOnAction(
        e -> {
          searchField.clear();
          reload();
        });
    btnNew.setOnAction(e -> showNewDialog());
    btnAssign.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    btnAssign.setOnAction(e -> assignNumber());
  }

  private void reload() {
    try {
      data.setAll(repo.findAll());
    } catch (Exception e) {
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
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void showNewDialog() {
    Dialog<CreateRes> d = new Dialog<>();
    d.setTitle("Nouvelle demande RMA");
    d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    ComboBox<CompanyService> cbService = new ComboBox<>();
    try {
      cbService.getItems().setAll(serviceRepo.findAllActiveByType("SAV"));
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
      return;
    }
    Button btnSelectMan = new Button("Filtrer par fabricant…");
    Label lbMan = new Label("");
    final Long[] selectedManufacturerId = new Long[1];
    btnSelectMan.setOnAction(
        e -> selectManufacturerAndFilter(cbService, lbMan, selectedManufacturerId));
    TextField tfProduit = new TextField();
    TextField tfSN = new TextField();
    TextField tfCode = new TextField();
    TextArea taReason = new TextArea();
    taReason.setPrefRowCount(3);
    GridPane gp = new GridPane();
    gp.setHgap(8);
    gp.setVgap(8);
    gp.addRow(0, new Label("Service SAV"), cbService);
    gp.addRow(1, new Label("Fabricant"), new HBox(6, btnSelectMan, lbMan));
    gp.addRow(2, new Label("Produit"), tfProduit);
    gp.addRow(3, new Label("N° Série"), tfSN);
    gp.addRow(4, new Label("Code produit"), tfCode);
    gp.addRow(5, new Label("Motif"), taReason);
    d.getDialogPane().setContent(gp);
    d.setResultConverter(
        bt -> {
          if (bt == ButtonType.OK) {
            CompanyService svc = cbService.getValue();
            if (svc == null) {
              showError("Validation", "Choisissez un service SAV");
              return null;
            }
            return new CreateRes(
                svc,
                selectedManufacturerId[0],
                tfProduit.getText(),
                tfSN.getText(),
                tfCode.getText(),
                taReason.getText());
          }
          return null;
        });
    Optional<CreateRes> res = d.showAndWait();
    res.ifPresent(
        r -> {
          try {
            RmaRequest rq =
                new RmaRequest(
                    null,
                    r.service.companyId(),
                    r.service.id(),
                    r.manufacturerId,
                    r.produit,
                    r.sn,
                    r.code,
                    r.reason,
                    "draft",
                    null,
                    null,
                    null);
            rmaService.create(rq);
            reload();
          } catch (Exception e) {
            showError("Erreur", e.getMessage());
          }
        });
  }

  private void editSelected(RmaRequest sel) {
    if (sel == null) {
      return;
    }
    Dialog<RmaRequest> d = new Dialog<>();
    d.setTitle("Modifier RMA #" + sel.id());
    d.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    ComboBox<CompanyService> cbService = new ComboBox<>();
    try {
      cbService.getItems().setAll(serviceRepo.findAllActiveByType("SAV"));
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
      return;
    }
    CompanyService curSvc = null;
    try {
      curSvc = serviceRepo.findById(sel.providerServiceId()).orElse(null);
    } catch (Exception ignore) {
    }
    if (curSvc != null) {
      cbService.getSelectionModel().select(curSvc);
    }
    Button btnSelectMan = new Button("Filtrer par fabricant…");
    Label lbMan = new Label("");
    final Long[] selectedManufacturerId = new Long[] {sel.manufacturerId()};
    if (sel.manufacturerId() != null) {
      lbMan.setText(String.valueOf(sel.manufacturerId()));
    }
    btnSelectMan.setOnAction(
        e -> selectManufacturerAndFilter(cbService, lbMan, selectedManufacturerId));
    TextField tfProduit = new TextField(nz(sel.produit()));
    TextField tfSN = new TextField(nz(sel.numeroSerie()));
    TextField tfCode = new TextField(nz(sel.codeProduit()));
    TextArea taReason = new TextArea(nz(sel.reason()));
    taReason.setPrefRowCount(3);
    GridPane gp = new GridPane();
    gp.setHgap(8);
    gp.setVgap(8);
    gp.addRow(0, new Label("Service SAV"), cbService);
    gp.addRow(1, new Label("Fabricant"), new HBox(6, btnSelectMan, lbMan));
    gp.addRow(2, new Label("Produit"), tfProduit);
    gp.addRow(3, new Label("N° Série"), tfSN);
    gp.addRow(4, new Label("Code produit"), tfCode);
    gp.addRow(5, new Label("Motif"), taReason);
    d.getDialogPane().setContent(gp);
    d.setResultConverter(
        bt -> {
          if (bt == ButtonType.OK) {
            CompanyService svc = cbService.getValue();
            if (svc == null) {
              showError("Validation", "Choisissez un service SAV");
              return null;
            }
            return new RmaRequest(
                sel.id(),
                svc.companyId(),
                svc.id(),
                selectedManufacturerId[0],
                tfProduit.getText(),
                tfSN.getText(),
                tfCode.getText(),
                taReason.getText(),
                sel.status(),
                sel.rmaNumber(),
                sel.createdAt(),
                sel.updatedAt());
          }
          return null;
        });
    Optional<RmaRequest> res = d.showAndWait();
    res.ifPresent(
        upd -> {
          try {
            rmaService.update(upd);
            reload();
          } catch (Exception e) {
            showError("Erreur", e.getMessage());
          }
        });
  }

  private void deleteSelected(RmaRequest sel) {
    if (sel == null) {
      return;
    }
    Alert a =
        new Alert(
            Alert.AlertType.CONFIRMATION,
            "Supprimer la demande RMA #" + sel.id() + " ?",
            ButtonType.CANCEL,
            ButtonType.OK);
    a.setHeaderText("Confirmation");
    Optional<ButtonType> res = a.showAndWait();
    if (res.isPresent() && res.get() == ButtonType.OK) {
      try {
        rmaService.delete(sel.id());
        reload();
      } catch (Exception e) {
        showError("Erreur", e.getMessage());
      }
    }
  }

  private void selectManufacturerAndFilter(
      ComboBox<CompanyService> cbService, Label lbMan, Long[] selectedManufacturerId) {
    try {
      // Ouvrir la fenêtre de sélection fabricant (réutilisation du contrôleur dédié)
      javafx.fxml.FXMLLoader l =
          new javafx.fxml.FXMLLoader(
              getClass().getResource("/fxml/manufacturer-select-dialog.fxml"));
      javafx.scene.Parent root = l.load();
      javafx.stage.Stage st = new javafx.stage.Stage();
      st.setTitle("Sélection fabricant");
      st.setScene(new javafx.scene.Scene(root));
      st.initModality(javafx.stage.Modality.APPLICATION_MODAL);
      st.showAndWait();
      Object ud = st.getUserData();
      if (ud instanceof Manufacturer m) {
        lbMan.setText(m.name());
        selectedManufacturerId[0] = m.id();
        // Interroger les liens fabricant -> services de type SAV
        var links = manufLinkRepo.findByManufacturer(m.id(), "SAV");
        List<Long> ids = new ArrayList<>();
        for (var link : links) ids.add(link.serviceId());
        List<CompanyService> svcs =
            ids.isEmpty()
                ? serviceRepo.findAllActiveByType("SAV")
                : serviceRepo.findActiveByIdsAndType(ids, "SAV");
        cbService.getItems().setAll(svcs);
        if (!svcs.isEmpty()) {
          cbService.getSelectionModel().select(0);
        }
      }
    } catch (Exception e) {
      showError("Fabricant", e.getMessage());
    }
  }

  private void assignNumber() {
    RmaRequest sel = table.getSelectionModel().getSelectedItem();
    if (sel == null) {
      return;
    }
    TextInputDialog d = new TextInputDialog();
    d.setTitle("Attribuer nº RMA");
    d.setHeaderText("Renseigner le numéro RMA");
    d.getEditor().setText(nz(sel.rmaNumber()));
    Optional<String> res = d.showAndWait();
    res.ifPresent(
        num -> {
          try {
            new RmaService(new RmaRequestRepository(ds)).assignRmaNumber(sel.id(), num);
            reload();
          } catch (Exception e) {
            showError("Erreur", e.getMessage());
          }
        });
  }

  private String nz(String s) {
    return s == null ? "" : s;
  }

  private String serviceName(Long id) {
    if (id == null) {
      return "";
    }
    try {
      return serviceRepo
          .findById(id)
          .map(
              cs ->
                  cs.name() == null || cs.name().isBlank()
                      ? (cs.type() + " #" + cs.id())
                      : cs.name())
          .orElse("");
    } catch (Exception e) {
      return "";
    }
  }

  private void showError(String title, String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    a.setHeaderText(title);
    a.showAndWait();
  }

  private record CreateRes(
      CompanyService service,
      Long manufacturerId,
      String produit,
      String sn,
      String code,
      String reason) {}
}
