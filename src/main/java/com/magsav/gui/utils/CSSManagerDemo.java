package com.magsav.gui.utils;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Démonstration du système CSS centralisé MAGSAV
 * Cette classe montre comment utiliser CSSManager pour créer une interface cohérente
 */
public class CSSManagerDemo extends Application {
    
    @Override
    public void start(Stage stage) {
        CSSManager cssManager = CSSManager.getInstance();
        
        // Création des composants de test
        VBox root = new VBox(15);
        
        // Titre principal
        Label titre = new Label("🎨 Démonstration CSS Manager");
        cssManager.styleTitle(titre);
        
        // Section avec conteneur
        VBox section = new VBox(10);
        cssManager.applyComponentStyle(section, "preferences-section");
        
        Label sousTitre = new Label("Boutons avec styles centralisés");
        cssManager.styleSubtitle(sousTitre);
        
        // Boutons avec différents styles
        Button btnPrimaire = new Button("Bouton Principal");
        cssManager.stylePrimaryButton(btnPrimaire);
        
        Button btnSecondaire = new Button("Bouton Secondaire");
        cssManager.styleSecondaryButton(btnSecondaire);
        
        Button btnDanger = new Button("Bouton Danger");
        cssManager.styleDangerButton(btnDanger);
        
        // Labels de statut
        Label successLabel = new Label("✅ Opération réussie");
        cssManager.styleSuccessLabel(successLabel);
        
        Label errorLabel = new Label("❌ Erreur détectée");
        cssManager.styleErrorLabel(errorLabel);
        
        // Séparateur
        Separator separator = new Separator();
        cssManager.styleSeparator(separator);
        
        // Démonstration de styles dynamiques
        Label dynamicLabel = new Label("Label avec couleur personnalisée");
        cssManager.setTextColor(dynamicLabel, "#ff6b6b");
        
        // Assemblage de l'interface
        section.getChildren().addAll(
            sousTitre,
            btnPrimaire,
            btnSecondaire, 
            btnDanger,
            successLabel,
            errorLabel
        );
        
        root.getChildren().addAll(
            titre,
            section,
            separator,
            dynamicLabel
        );
        
        // Container principal
        cssManager.applyComponentStyle(root, "preferences-container");
        
        // Configuration de la scène
        Scene scene = new Scene(root, 400, 500);
        
        // Application du thème complet
        cssManager.applyTheme(scene);
        
        stage.setTitle("MAGSAV - Test CSS Manager");
        stage.setScene(scene);
        
        // Application du thème à la fenêtre
        cssManager.initializeWindow(stage, "demo");
        
        stage.show();
        
        System.out.println("✅ CSS Manager Demo lancée avec succès !");
        System.out.println("📝 Tous les styles sont appliqués via le système centralisé");
        System.out.println("🎯 Aucun setStyle() inline utilisé !");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}