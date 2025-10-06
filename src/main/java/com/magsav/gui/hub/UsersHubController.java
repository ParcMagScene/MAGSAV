package com.magsav.gui.hub;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class UsersHubController {
  @FXML private void onUsers()  { new Alert(Alert.AlertType.INFORMATION, "Gestion des utilisateurs (à implémenter)").showAndWait(); }
  @FXML private void onAdmins() { new Alert(Alert.AlertType.INFORMATION, "Gestion des administrateurs (à implémenter)").showAndWait(); }
}