package com.magsav.gui.component;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Composant réutilisable pour les zones de glisser-déposer de fichiers
 * Permet à l'utilisateur de glisser des fichiers directement dans l'interface
 */
public class FileDropZone extends StackPane {
    
    private final StringProperty promptText = new SimpleStringProperty();
    private final StringProperty acceptedExtensions = new SimpleStringProperty();
    private final ObjectProperty<Consumer<List<File>>> onFilesDropped = new SimpleObjectProperty<>();
    
    private Label promptLabel;
    private Label extensionsLabel;
    private VBox contentBox;
    
    public FileDropZone() {
        this("Glissez ici vos fichiers", ".jpg, .png, .gif, .pdf");
    }
    
    public FileDropZone(String prompt, String extensions) {
        this.promptText.set(prompt);
        this.acceptedExtensions.set(extensions);
        
        setupUI();
        setupDragAndDrop();
    }
    
    private void setupUI() {
        // Style principal de la zone
        setStyle(
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 2;" +
            "-fx-border-style: dashed;" +
            "-fx-background-color: #f9f9f9;" +
            "-fx-background-radius: 5;" +
            "-fx-border-radius: 5;"
        );
        
        setPrefHeight(120);
        setMaxHeight(150);
        
        // Contenu de la zone
        contentBox = new VBox(5);
        contentBox.setAlignment(Pos.CENTER);
        
        promptLabel = new Label();
        promptLabel.textProperty().bind(promptText);
        promptLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #666666;" +
            "-fx-font-weight: bold;"
        );
        promptLabel.setTextAlignment(TextAlignment.CENTER);
        
        extensionsLabel = new Label();
        extensionsLabel.textProperty().bind(acceptedExtensions);
        extensionsLabel.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #999999;"
        );
        extensionsLabel.setTextAlignment(TextAlignment.CENTER);
        
        contentBox.getChildren().addAll(promptLabel, extensionsLabel);
        getChildren().add(contentBox);
    }
    
    private void setupDragAndDrop() {
        // Événements de glisser-déposer
        setOnDragOver(this::handleDragOver);
        setOnDragEntered(this::handleDragEntered);
        setOnDragExited(this::handleDragExited);
        setOnDragDropped(this::handleDragDropped);
    }
    
    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }
    
    private void handleDragEntered(DragEvent event) {
        if (event.getGestureSource() != this && event.getDragboard().hasFiles()) {
            setStyle(getStyle().replace("#f9f9f9", "#e6f3ff").replace("#cccccc", "#4CAF50"));
            promptLabel.setStyle(promptLabel.getStyle().replace("#666666", "#4CAF50"));
        }
        event.consume();
    }
    
    private void handleDragExited(DragEvent event) {
        // Restaurer le style original
        setStyle(
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 2;" +
            "-fx-border-style: dashed;" +
            "-fx-background-color: #f9f9f9;" +
            "-fx-background-radius: 5;" +
            "-fx-border-radius: 5;"
        );
        promptLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #666666;" +
            "-fx-font-weight: bold;"
        );
        event.consume();
    }
    
    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            
            // Filtrer les fichiers selon les extensions acceptées
            List<File> validFiles = files.stream()
                .filter(this::isValidFile)
                .toList();
            
            if (!validFiles.isEmpty() && onFilesDropped.get() != null) {
                onFilesDropped.get().accept(validFiles);
                success = true;
                
                // Feedback visuel temporaire
                promptLabel.setText("✓ " + validFiles.size() + " fichier(s) reçu(s)");
                promptLabel.setStyle(promptLabel.getStyle().replace("#666666", "#4CAF50"));
                
                // Restaurer le texte original après 2 secondes
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            promptLabel.setText(promptText.get());
                            promptLabel.setStyle(
                                "-fx-font-size: 14px;" +
                                "-fx-text-fill: #666666;" +
                                "-fx-font-weight: bold;"
                            );
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        }
        
        event.setDropCompleted(success);
        event.consume();
        
        // Restaurer le style
        handleDragExited(event);
    }
    
    private boolean isValidFile(File file) {
        if (acceptedExtensions.get() == null || acceptedExtensions.get().trim().isEmpty()) {
            return true; // Accepter tous les fichiers si aucune extension spécifiée
        }
        
        String fileName = file.getName().toLowerCase();
        String[] extensions = acceptedExtensions.get().toLowerCase().split(",");
        
        for (String ext : extensions) {
            String cleanExt = ext.trim().replace(".", "");
            if (fileName.endsWith("." + cleanExt)) {
                return true;
            }
        }
        
        return false;
    }
    
    // Propriétés publiques
    public StringProperty promptTextProperty() { return promptText; }
    public String getPromptText() { return promptText.get(); }
    public void setPromptText(String promptText) { this.promptText.set(promptText); }
    
    public StringProperty acceptedExtensionsProperty() { return acceptedExtensions; }
    public String getAcceptedExtensions() { return acceptedExtensions.get(); }
    public void setAcceptedExtensions(String acceptedExtensions) { this.acceptedExtensions.set(acceptedExtensions); }
    
    public ObjectProperty<Consumer<List<File>>> onFilesDroppedProperty() { return onFilesDropped; }
    public Consumer<List<File>> getOnFilesDropped() { return onFilesDropped.get(); }
    public void setOnFilesDropped(Consumer<List<File>> onFilesDropped) { this.onFilesDropped.set(onFilesDropped); }
}