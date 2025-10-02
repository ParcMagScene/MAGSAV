package com.magsav.gui.menu;

import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public final class GestionMenu {
  private GestionMenu() {}

  public static Menu build() {
    Menu gestion = new Menu("Gestion");

    MenuItem fabricants = new MenuItem("Fabricants");
    fabricants.setOnAction(e -> showInfo("Fabricants"));

    MenuItem fournisseurs = new MenuItem("Fournisseurs");
    fournisseurs.setOnAction(e -> showInfo("Fournisseurs"));

    MenuItem sav = new MenuItem("SAV externe");
    sav.setOnAction(e -> showInfo("SAV externe"));

    gestion.getItems().addAll(fabricants, fournisseurs, sav);
    return gestion;
  }

  private static void showInfo(String section) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle(section);
    a.setHeaderText(section);
    a.setContentText("Vue à implémenter.");
    a.show();
  }
}
