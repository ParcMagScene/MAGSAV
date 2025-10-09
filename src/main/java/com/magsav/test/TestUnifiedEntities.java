package com.magsav.test;

import com.magsav.db.EntityMigration;
import com.magsav.gui.entities.EntityWindowLauncher;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Classe de test pour la nouvelle interface unifiée
 */
public class TestUnifiedEntities extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialiser la base de données
            com.magsav.db.DB.init();
            
            // Exécuter la migration des entités
            EntityMigration.migrate();
            
            // Ouvrir la fenêtre unifiée
            EntityWindowLauncher.openUnifiedEntitiesWindow();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}