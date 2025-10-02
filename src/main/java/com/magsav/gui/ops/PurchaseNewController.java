package com.magsav.gui.ops;

import com.magsav.model.CompanyService;
import com.magsav.model.PurchaseRFQ;
import com.magsav.repo.CompanyServiceRepository;
import com.magsav.service.PurchaseService;
import java.net.URL;
import java.util.*;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javax.sql.DataSource;

public class PurchaseNewController implements Initializable {
  @FXML private ComboBox<CompanyService> cbService;
  @FXML private TextField tfProduit;
  @FXML private TextField tfPN;
  @FXML private TextField tfQty;
  @FXML private TextArea taNotes;
  @FXML private Button btnCancel;
  @FXML private Button btnSave;

  private final CompanyServiceRepository serviceRepo;
  private final PurchaseService purchaseService;

  public PurchaseNewController(DataSource ds) {
    this.serviceRepo = new CompanyServiceRepository(ds);
    this.purchaseService = new PurchaseService(new com.magsav.repo.PurchaseRFQRepository(ds));
  }

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    cbService.setCellFactory(
        lv ->
            new ListCell<>() {
              @Override
              protected void updateItem(CompanyService item, boolean empty) {
                super.updateItem(item, empty);
                setText(
                    empty || item == null
                        ? ""
                        : (item.name() != null && !item.name().isBlank()
                            ? item.name()
                            : item.type() + " #" + item.id()));
              }
            });
    cbService.setButtonCell(cbService.getCellFactory().call(null));
    cbService.setConverter(
        new StringConverter<>() {
          @Override
          public String toString(CompanyService item) {
            return item == null
                ? ""
                : (item.name() != null && !item.name().isBlank()
                    ? item.name()
                    : item.type() + " #" + item.id());
          }

          @Override
          public CompanyService fromString(String s) {
            return null;
          }
        });
    try {
      var list = serviceRepo.findAllActiveByType("SUPPLIER");
      cbService.setItems(FXCollections.observableArrayList(list));
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
    btnCancel.setOnAction(e -> btnCancel.getScene().getWindow().hide());
    btnSave.setOnAction(e -> save());
  }

  private void save() {
    try {
      var svc = cbService.getValue();
      if (svc == null) {
        showError("Validation", "Veuillez choisir un service fournisseur");
        return;
      }
      Integer qty = null;
      if (tfQty.getText() != null && !tfQty.getText().isBlank()) {
        qty = Integer.parseInt(tfQty.getText().trim());
      }
      PurchaseRFQ rfq =
          new PurchaseRFQ(
              null,
              svc == null ? null : svc.companyId(),
              svc == null ? null : svc.id(),
              tfProduit.getText(),
              tfPN.getText(),
              qty,
              "draft",
              java.time.LocalDateTime.now(),
              null,
              null,
              null,
              taNotes.getText());
      purchaseService.createRFQ(rfq);
      btnSave.getScene().getWindow().hide();
    } catch (Exception e) {
      showError("Erreur", e.getMessage());
    }
  }

  private void showError(String title, String msg) {
    Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
    a.setHeaderText(title);
    a.showAndWait();
  }
}
