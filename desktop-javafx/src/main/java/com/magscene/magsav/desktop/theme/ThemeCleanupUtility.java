package com.magscene.magsav.desktop.theme;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * Utilitaire de nettoyage des thèmes pour MAGSAV 3.0
 * 
 * Supprime tous les styles inline hard-codés et applique
 * les classes CSS unifiées à la place
 * 
 * @version 3.0.0-cleanup
 */
public class ThemeCleanupUtility {
    
    /**
     * Nettoie récursivement tous les styles inline d'un nœud
     */
    public static void cleanInlineStyles(Node node) {
        if (node == null) return;
        
        // Supprimer le style inline
        node.setStyle("");
        
        // Appliquer les classes CSS appropriées selon le type
        applyUnifiedClasses(node);
        
        // Traitement récursif des enfants
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : parent.getChildrenUnmodifiable()) {
                cleanInlineStyles(child);
            }
        }
    }
    
    /**
     * Applique les classes CSS unifiées selon le type de composant
     */
    private static void applyUnifiedClasses(Node node) {
        // Supprimer les anciennes classes de style
        node.getStyleClass().removeIf(cls -> 
            cls.contains("dark") || cls.contains("light") || cls.contains("blue") || cls.contains("green")
        );
        
        // Appliquer les nouvelles classes selon le type
        if (node instanceof Button) {
            Button button = (Button) node;
            String text = button.getText().toLowerCase();
            
            // Classification automatique des boutons selon leur texte
            if (text.contains("ajouter") || text.contains("nouveau") || text.contains("créer") || text.contains("+")) {
                button.getStyleClass().add("success");
            } else if (text.contains("modifier") || text.contains("éditer") || text.contains("edit")) {
                button.getStyleClass().add("primary");
            } else if (text.contains("supprimer") || text.contains("delete") || text.contains("🗑")) {
                button.getStyleClass().add("danger");
            } else if (text.contains("exporter") || text.contains("import") || text.contains("📤") || text.contains("📥")) {
                button.getStyleClass().add("warning");
            }
            
        } else if (node instanceof TableView) {
            // Tables utilisent automatiquement les styles de theme-*.css
            
        } else if (node instanceof HBox || node instanceof VBox) {
            // Vérifier si c'est une barre d'outils
            if (node.getStyleClass().contains("toolbar") || 
                (node instanceof HBox && ((HBox) node).getChildren().stream()
                    .anyMatch(child -> child instanceof Button))) {
                node.getStyleClass().add("module-toolbar");
            }
            
        } else if (node instanceof ComboBox) {
            // ComboBox utilisent automatiquement les styles de theme-*.css
            
        } else if (node instanceof TextField) {
            TextField field = (TextField) node;
            if (field.getPromptText() != null && field.getPromptText().toLowerCase().contains("recherche")) {
                field.getStyleClass().add("global-search-field");
            }
        }
    }
    
    /**
     * Nettoie une vue complète en appliquant les nouveaux styles
     */
    public static void cleanupView(Parent view) {
        if (view == null) return;
        
        // Nettoyage récursif
        cleanInlineStyles(view);
        
        // Application des classes spécifiques aux vues
        if (view.getClass().getSimpleName().contains("Equipment")) {
            view.getStyleClass().add("equipment-view");
        } else if (view.getClass().getSimpleName().contains("SAV") || 
                   view.getClass().getSimpleName().contains("Service")) {
            view.getStyleClass().add("sav-view");
        } else if (view.getClass().getSimpleName().contains("Vehicle")) {
            view.getStyleClass().add("vehicle-view");
        } else if (view.getClass().getSimpleName().contains("Personnel")) {
            view.getStyleClass().add("personnel-view");
        } else if (view.getClass().getSimpleName().contains("Settings")) {
            view.getStyleClass().add("settings-view");
        }
    }
    
    /**
     * Nettoie spécifiquement les barres d'outils avec styles hard-codés
     */
    public static void cleanupToolbar(Node toolbar) {
        if (toolbar == null) return;
        
        // Supprimer le style inline
        toolbar.setStyle("");
        
        // Appliquer la classe unifiée
        toolbar.getStyleClass().add("module-toolbar");
        
        // Nettoyer les enfants (boutons, filtres, etc.)
        if (toolbar instanceof Parent) {
            Parent parent = (Parent) toolbar;
            for (Node child : parent.getChildrenUnmodifiable()) {
                cleanInlineStyles(child);
            }
        }
    }
    
    /**
     * Nettoie spécifiquement les tableaux avec styles hard-codés
     */
    public static void cleanupTable(TableView<?> table) {
        if (table == null) return;
        
        // Supprimer le style inline
        table.setStyle("");
        
        // Les styles de table sont gérés automatiquement par CSS; // Nettoyer les factory de cellules si elles ont des styles inline
        for (@SuppressWarnings("unused") TableColumn<?, ?> column : table.getColumns()) {
            // Les cellules sont gérées automatiquement par CSS
        }
    }
    
    /**
     * Méthode utilitaire pour nettoyer une collection de nœuds
     */
    public static void cleanupNodes(Node... nodes) {
        for (Node node : nodes) {
            cleanInlineStyles(node);
        }
    }
}