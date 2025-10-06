package com.magsav.gui.hub;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class DemandesHubController {
  @FXML private void onPartRequests()      { new Alert(Alert.AlertType.INFORMATION, "Demandes de pièces (à implémenter)").showAndWait(); }
  @FXML private void onEquipmentRequests() { new Alert(Alert.AlertType.INFORMATION, "Demandes de matériel (à implémenter)").showAndWait(); }
}