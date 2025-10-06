package com.magsav.gui.interventions;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class InterventionFormController {
  @FXML private ComboBox<String> cbProduct; // simplifié pour compiler si vos modèles diffèrent
  @FXML private ComboBox<String> cbSerial;
  @FXML private ComboBox<String> cbDetector;
  @FXML private TextArea taDesc;

  private Long presetProductId;

  @FXML
  private void initialize() {
    cbProduct.setItems(FXCollections.observableArrayList());
    cbSerial.setItems(FXCollections.observableArrayList());
    cbDetector.setItems(FXCollections.observableArrayList());
  }

  public void presetProduct(long productId) { this.presetProductId = productId; }

  public Long productId() { return presetProductId; }
  public String serial() { return cbSerial.getSelectionModel().getSelectedItem(); }
  public Long detectorId() { return null; }
  public String description() { return taDesc.getText() == null ? "" : taDesc.getText().trim(); }
}