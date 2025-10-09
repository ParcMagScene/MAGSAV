package com.magsav.gui.test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

/**
 * Test simple pour vérifier que l'interface d'import CSV se charge correctement
 */
public class CsvImportTest extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Tenter de charger l'interface d'import CSV
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/csv_import.fxml"));
            DialogPane dialogPane = loader.load();
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Test - Import CSV");
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'interface CSV:");
            e.printStackTrace();
        }
        
        primaryStage.close();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}