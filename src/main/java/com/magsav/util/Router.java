package com.magsav.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public final class Router {
  public enum Route {
    CATEGORIES("Catégories", "/fxml/categories.fxml"),
    MANUFACTURERS("Fabricants", "/fxml/manufacturers.fxml"),
    SUPPLIERS("Fournisseurs", "/fxml/suppliers.fxml"),
    EXTERNAL_SAV("SAV externes", "/fxml/external_sav.fxml"),
    CLIENTS("Clients", "/fxml/clients.fxml"),
    REQ_PARTS("Demandes de pièces", "/fxml/requests_parts.fxml"),
    REQ_EQUIP("Demandes de matériel", "/fxml/requests_equipment.fxml"),
    PRODUCT_DETAIL("Fiche produit", "/fxml/product_detail.fxml");

    public final String title;
    public final String fxml;
    Route(String title, String fxml) { this.title = title; this.fxml = fxml; }
  }

  public static void open(Route route) throws Exception {
    var url = Router.class.getResource(route.fxml);
    if (url == null) throw new IllegalStateException("FXML introuvable: " + route.fxml);
    FXMLLoader loader = new FXMLLoader(url);
    Parent root = loader.load();
    Stage stage = new Stage();
    stage.setTitle(route.title);
    // Utilise le nom de la route comme clé
    Preferences prefs = Preferences.userNodeForPackage(Router.class);
    String key = route.name().toLowerCase();
    double width = prefs.getDouble(key + ".width", 900);
    double height = prefs.getDouble(key + ".height", 650);
    stage.setScene(new Scene(root, width, height));
    stage.setOnCloseRequest(e -> {
      prefs.putDouble(key + ".width", stage.getWidth());
      prefs.putDouble(key + ".height", stage.getHeight());
    });
    stage.show();
  }

  private Router() {}
}