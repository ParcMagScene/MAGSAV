package com.magsav.gui.util;

import javafx.scene.control.Alert;

public final class UiAlerts {
  private UiAlerts() {}

  public static void info(String title, String message) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle(title);
    a.setHeaderText(null);
    a.setContentText(message);
    a.showAndWait();
  }

  public static void error(String title, String message) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle(title);
    a.setHeaderText(null);
    a.setContentText(message);
    a.showAndWait();
  }

  public static void warn(String title, String message) {
    Alert a = new Alert(Alert.AlertType.WARNING);
    a.setTitle(title);
    a.setHeaderText(null);
    a.setContentText(message);
    a.showAndWait();
  }
}
