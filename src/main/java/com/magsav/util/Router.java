package com.magsav.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

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
    stage.setScene(new Scene(root));
    stage.show();
  }

  private Router() {}
}