package com.magsav.test;

import com.magsav.service.NavigationService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CategoryTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button testButton = new Button("Test Catégories");
        testButton.setOnAction(e -> {
            try {
                System.out.println("Tentative d'ouverture des catégories...");
                NavigationService.openCategories();
                System.out.println("Catégories ouvertes avec succès !");
            } catch (Exception ex) {
                System.err.println("ERREUR lors de l'ouverture des catégories:");
                ex.printStackTrace();
            }
        });

        VBox root = new VBox(testButton);
        Scene scene = new Scene(root, 200, 100);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Test Catégories");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}