package com.magsav.gui.hub;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class ConfigHubController {
  @FXML private void onPreferences()   { new Alert(Alert.AlertType.INFORMATION, "Préférences (à implémenter)").showAndWait(); }
  @FXML private void onAdvancedConfig(){ new Alert(Alert.AlertType.INFORMATION, "Configuration avancée (à implémenter)").showAndWait(); }
  @FXML private void onDbSettings()    { new Alert(Alert.AlertType.INFORMATION, "Paramètres base de données (à implémenter)").showAndWait(); }
}