package com.magsav.util;

import com.magsav.gui.ProductDetailController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

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
        // Restaure la taille précédente
        Preferences prefs = Preferences.userNodeForPackage(Views.class);
        double width = prefs.getDouble("productSheet.width", 800);
        double height = prefs.getDouble("productSheet.height", 600);
        st.setScene(new Scene(root, width, height));
        st.setOnCloseRequest(e -> {
          prefs.putDouble("productSheet.width", st.getWidth());
          prefs.putDouble("productSheet.height", st.getHeight());
        });
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
        // Utilise le nom du FXML comme clé
        Preferences prefs = Preferences.userNodeForPackage(Views.class);
        String key = fxmlPath.replaceAll("[^a-zA-Z0-9]", "_");
        double width = prefs.getDouble(key + ".width", 800);
        double height = prefs.getDouble(key + ".height", 600);
        st.setScene(new Scene(root, width, height));
        st.setOnCloseRequest(e -> {
          prefs.putDouble(key + ".width", st.getWidth());
          prefs.putDouble(key + ".height", st.getHeight());
        });
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
        Preferences prefs = Preferences.userNodeForPackage(Views.class);
        double width = prefs.getDouble("interventionDetail.width", 900);
        double height = prefs.getDouble("interventionDetail.height", 650);
        st.setScene(new Scene(root, width, height));
        st.setOnCloseRequest(e -> {
          prefs.putDouble("interventionDetail.width", st.getWidth());
          prefs.putDouble("interventionDetail.height", st.getHeight());
        });
      st.show();
    } catch (Exception e) {
      new Alert(Alert.AlertType.ERROR, "Erreur d'ouverture de l'intervention: " + e.getMessage()).showAndWait();
      e.printStackTrace();
    }
  }
}