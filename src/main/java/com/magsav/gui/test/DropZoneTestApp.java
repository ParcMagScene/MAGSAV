package com.magsav.gui.test;

import com.magsav.gui.component.FileDropZone;
import com.magsav.util.MediaImporter;
import com.magsav.util.AppLogger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Application de test pour les zones de glisser-déposer
 */
public class DropZoneTestApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Test des Zones de Glisser-Déposer - MAGSAV");
        
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20;");
        
        // Titre
        Label titleLabel = new Label("Test des Zones de Glisser-Déposer");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        // Zone pour les logos
        Label logoLabel = new Label("Zone de Logo :");
        logoLabel.setStyle("-fx-font-weight: bold;");
        
        FileDropZone logoDropZone = new FileDropZone(
            "Glissez ici vos logos",
            ".jpg, .jpeg, .png, .gif, .svg"
        );
        
        Label logoStatus = new Label("Aucun logo importé");
        logoStatus.setStyle("-fx-text-fill: #666;");
        
        logoDropZone.setOnFilesDropped(files -> {
            AppLogger.info("Logos reçus: " + files.size());
            logoStatus.setText("✓ " + files.size() + " logo(s) reçu(s)");
            logoStatus.setStyle("-fx-text-fill: #4CAF50;");
            
            // Simuler l'import
            for (File file : files) {
                AppLogger.info("Logo: " + file.getName() + " (" + file.length() + " bytes)");
            }
        });
        
        // Zone pour les images
        Label imageLabel = new Label("Zone d'Images :");
        imageLabel.setStyle("-fx-font-weight: bold;");
        
        FileDropZone imageDropZone = new FileDropZone(
            "Glissez ici vos images",
            ".jpg, .jpeg, .png, .gif, .bmp"
        );
        
        Label imageStatus = new Label("Aucune image importée");
        imageStatus.setStyle("-fx-text-fill: #666;");
        
        imageDropZone.setOnFilesDropped(files -> {
            AppLogger.info("Images reçues: " + files.size());
            imageStatus.setText("✓ " + files.size() + " image(s) reçue(s)");
            imageStatus.setStyle("-fx-text-fill: #4CAF50;");
            
            for (File file : files) {
                AppLogger.info("Image: " + file.getName() + " (" + file.length() + " bytes)");
            }
        });
        
        // Zone pour les documents
        Label docLabel = new Label("Zone de Documents :");
        docLabel.setStyle("-fx-font-weight: bold;");
        
        FileDropZone docDropZone = new FileDropZone(
            "Glissez ici vos documents",
            ".pdf, .doc, .docx, .xls, .xlsx, .txt"
        );
        
        Label docStatus = new Label("Aucun document importé");
        docStatus.setStyle("-fx-text-fill: #666;");
        
        docDropZone.setOnFilesDropped(files -> {
            AppLogger.info("Documents reçus: " + files.size());
            docStatus.setText("✓ " + files.size() + " document(s) reçu(s)");
            docStatus.setStyle("-fx-text-fill: #4CAF50;");
            
            for (File file : files) {
                AppLogger.info("Document: " + file.getName() + " (" + file.length() + " bytes)");
            }
        });
        
        // Boutons d'action
        HBox buttonBox = new HBox(10);
        Button clearButton = new Button("Effacer les statuts");
        Button closeButton = new Button("Fermer");
        
        clearButton.setOnAction(e -> {
            logoStatus.setText("Aucun logo importé");
            logoStatus.setStyle("-fx-text-fill: #666;");
            imageStatus.setText("Aucune image importée");
            imageStatus.setStyle("-fx-text-fill: #666;");
            docStatus.setText("Aucun document importé");
            docStatus.setStyle("-fx-text-fill: #666;");
        });
        
        closeButton.setOnAction(e -> primaryStage.close());
        
        buttonBox.getChildren().addAll(clearButton, closeButton);
        
        // Assemblage
        root.getChildren().addAll(
            titleLabel,
            new Separator(),
            logoLabel, logoDropZone, logoStatus,
            new Separator(),
            imageLabel, imageDropZone, imageStatus,
            new Separator(),
            docLabel, docDropZone, docStatus,
            new Separator(),
            buttonBox
        );
        
        Scene scene = new Scene(new ScrollPane(root), 500, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        AppLogger.info("Application de test des zones de glisser-déposer lancée");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}