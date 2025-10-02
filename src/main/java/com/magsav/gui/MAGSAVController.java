package com.magsav.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MAGSAVController {
  @FXML private Label statusLabel;

  @FXML
  public void initialize() {
    // init UI si besoin
  }

  @FXML
  private void handleProductLabelDownload(ActionEvent e) {
    // ...existing code... (ex-impl dans MAGSAVApp)
  }

  @FXML
  private void handleOpenProduct(ActionEvent e) {
    // ...existing code...
  }

  @FXML
  private void handleOpenIntervention(ActionEvent e) {
    // ...existing code...
  }

  public void loadData() {
    // ...existing code...
  }
}
