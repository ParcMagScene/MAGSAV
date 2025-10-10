package com.magsav.gui.entities;

import com.magsav.util.AppLogger;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.prefs.Preferences;

/**
 * Utilitaire pour ouvrir la nouvelle interface unifiée de gestion des entités
 */
public class EntityWindowLauncher {

    /**
     * Ouvre la fenêtre de gestion unifiée des entités
     */
    public static void openUnifiedEntitiesWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(EntityWindowLauncher.class.getResource("/fxml/entities/simple_entities.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestion Unifiée des Entités - MAGSAV");
            Preferences prefs = Preferences.userNodeForPackage(EntityWindowLauncher.class);
            double width = prefs.getDouble("entitiesWindow.width", 1000);
            double height = prefs.getDouble("entitiesWindow.height", 600);
            
            Scene scene = new Scene(root, width, height);
            // Appliquer le thème dark
            scene.getStylesheets().add(EntityWindowLauncher.class.getResource("/css/simple-dark.css").toExternalForm());
            stage.setScene(scene);
            stage.setOnCloseRequest(e -> {
                prefs.putDouble("entitiesWindow.width", stage.getWidth());
                prefs.putDouble("entitiesWindow.height", stage.getHeight());
            });
            stage.show();

            AppLogger.info("Fenêtre de gestion unifiée des entités ouverte");

        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'ouverture de la fenêtre unifiée des entités", e);
            showErrorDialog("Erreur", "Impossible d'ouvrir la fenêtre de gestion des entités : " + e.getMessage());
        }
    }

    private static void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}