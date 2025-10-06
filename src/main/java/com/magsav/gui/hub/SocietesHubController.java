package com.magsav.gui.hub;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class SocietesHubController {
  @FXML private void onManufacturers() { new Alert(Alert.AlertType.INFORMATION, "Fabricants (à implémenter)").showAndWait(); }
  @FXML private void onSuppliers()     { new Alert(Alert.AlertType.INFORMATION, "Fournisseurs (à implémenter)").showAndWait(); }
  @FXML private void onExternalSav()   { new Alert(Alert.AlertType.INFORMATION, "SAV/RMA externes (à implémenter)").showAndWait(); }
}