package com.magsav.test;

import com.magsav.db.DB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Launcher pour tester l'interface des techniciens
 */
public class TechniciensTestApp extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialiser la base de données
        DB.init();
        
        // Charger l'interface FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/techniciens.fxml"));
        Parent root = loader.load();
        
        // Configurer la scène
        Scene scene = new Scene(root, 1200, 800);
        
        // Appliquer le CSS
        scene.getStylesheets().add(getClass().getResource("/css/base.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/css/components.css").toExternalForm());
        
        // Configurer la fenêtre
        primaryStage.setTitle("👥 Gestion des Techniciens Mag Scene - Test");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("🚀 Interface techniciens lancée");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}