package com.magsav.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

public final class Router {
  public enum Route {
    CATEGORIES("Catégories", "/fxml/categories/categories.fxml"),
    MANUFACTURERS("Fabricants", "/fxml/societes/lists/manufacturers.fxml"),
    SUPPLIERS("Fournisseurs", "/fxml/societes/lists/suppliers.fxml"),
    EXTERNAL_SAV("SAV externes", "/fxml/societes/lists/external_sav.fxml"),
    CLIENTS("Clients", "/fxml/clients.fxml"),
    REQ_PARTS("Demandes de pièces", "/fxml/requests/lists/requests_parts.fxml"),
    REQ_EQUIP("Demandes de matériel", "/fxml/requests/lists/requests_equipment.fxml"),
    PRODUCT_DETAIL("Fiche produit", "/fxml/products/details/product_detail.fxml");

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
    
    Scene scene = new Scene(root, width, height);
    // Appliquer le thème dark
    scene.getStylesheets().add(Router.class.getResource("/css/simple-dark.css").toExternalForm());
    stage.setScene(scene);
    stage.setOnCloseRequest(e -> {
      prefs.putDouble(key + ".width", stage.getWidth());
      prefs.putDouble(key + ".height", stage.getHeight());
    });
    stage.show();
  }

  private Router() {}
}