package com.magsav.service;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Application de test pour le service d'autocomplétion d'adresse
 */
public class AddressServiceTestApp extends Application {
    
    private AddressService addressService = new AddressService();
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Test Service Autocomplétion Adresse");
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        
        // Label d'instructions
        Label lblInstructions = new Label("Tapez au moins 3 caractères dans le champ ci-dessous pour tester l'autocomplétion:");
        lblInstructions.setWrapText(true);
        
        // Champ d'adresse avec autocomplétion
        TextField addressField = new TextField();
        addressField.setPromptText("Ex: 123 rue de la Paix, Paris");
        addressField.setPrefWidth(400);
        
        // Activer l'autocomplétion
        addressService.setupAddressAutocomplete(addressField);
        
        // TextArea pour une adresse multi-ligne
        Label lblTextArea = new Label("Test avec TextArea (adresse complète):");
        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Saisissez une adresse complète...");
        addressArea.setPrefRowCount(3);
        
        // Activer l'autocomplétion pour TextArea
        addressService.setupAddressAutocompleteForTextArea(addressArea);
        
        // Bouton de test de validation
        Button btnValidate = new Button("Valider adresse");
        Label lblResult = new Label();
        
        btnValidate.setOnAction(e -> {
            String address = addressField.getText();
            boolean isValid = addressService.isValidFrenchAddress(address);
            lblResult.setText("Adresse valide: " + isValid + " | Adresse: " + address);
        });
        
        // Informations
        Label lblInfo = new Label("✨ API utilisée: api-adresse.data.gouv.fr (gratuite et officielle)\n" +
                                 "⌨️ Autocomplétion: menu déroulant à partir de 3 caractères\n" +
                                 "🎯 Validation: vérification format adresse française");
        lblInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        root.getChildren().addAll(
            lblInstructions,
            addressField,
            lblTextArea,
            addressArea,
            btnValidate,
            lblResult,
            lblInfo
        );
        
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}