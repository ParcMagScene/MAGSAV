package com.magsav.util;

import com.magsav.gui.product.ProductDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public final class Views {
  private Views() {}

  public static void openProductSheet(long productId) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource("/fxml/product_detail.fxml"));
      Parent root = l.load();
      ProductDetailController ctl = l.getController();
      ctl.setProductId(productId);
      Stage st = new Stage();
      st.setTitle("Produit #" + productId);
      st.setScene(new Scene(root));
      st.show();
    } catch (Exception e) {
      throw new RuntimeException("Ouverture fiche produit: " + e.getMessage(), e);
    }
  }

  public static void openManufacturer(String fabricant) {
    new Alert(Alert.AlertType.INFORMATION, "Fabricant: " + fabricant).show();
  }

  public static void openInNewWindow(String fxmlPath, String windowTitle) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource(fxmlPath));
      Parent root = l.load();
      Stage st = new Stage();
      st.setTitle(windowTitle);
      st.setScene(new Scene(root));
      st.show();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture de la fenêtre: " + e.getMessage()).showAndWait();
      e.printStackTrace();
    }
  }

  public static void openInterventionDetail(long interventionId) {
    openInterventionDetail(interventionId, null);
  }

  public static void openInterventionDetail(long interventionId, com.magsav.repo.ProductRepository.ProductRow product) {
    try {
      FXMLLoader l = new FXMLLoader(Views.class.getResource("/fxml/interventions/intervention_detail.fxml"));
      Parent root = l.load();
      var controller = l.getController();
      
      // Si le contrôleur a une méthode load, l'appeler
      if (controller instanceof com.magsav.gui.interventions.InterventionDetailController) {
        var detailController = (com.magsav.gui.interventions.InterventionDetailController) controller;
        detailController.load(interventionId);
        // Si on a un produit, préremplir avec ses informations
        if (product != null) {
          detailController.loadProductInfo(product);
        }
      }
      
      Stage st = new Stage();
      st.setTitle("Intervention #" + interventionId + " - Édition");
      st.setScene(new Scene(root));
      st.show();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture de l'intervention: " + e.getMessage()).showAndWait();
      e.printStackTrace();
    }
  }
}