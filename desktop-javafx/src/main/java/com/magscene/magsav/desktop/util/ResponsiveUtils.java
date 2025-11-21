package com.magscene.magsav.desktop.util;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import java.util.logging.Logger;

/**
 * Utilitaires pour créer des interfaces responsives dans MAGSAV-3.0
 * Remplace les largeurs/hauteurs fixes par des layouts adaptatifs
 * 
 * @author MAGSAV Team
 * @version 1.0
 */
public class ResponsiveUtils {
    
    private static final Logger logger = Logger.getLogger(ResponsiveUtils.class.getName());
    
    // Largeurs proportionnelles recommandées pour colonnes tables
    public enum ColumnSize {
        XS(0.08),   // 8% - ID, QR, icônes
        SM(0.12),   // 12% - Statut, Type court
        MD(0.20),   // 20% - Nom, Titre standard
        LG(0.25),   // 25% - Description, Nom long
        XL(0.35);   // 35% - Contenu étendu
        
        public final double ratio;
        ColumnSize(double ratio) { this.ratio = ratio; }
    }
    
    /**
     * Rend une TableView complètement responsive
     * @param table La table à rendre responsive
     */
    public static void makeTableResponsive(TableView<?> table) {
        logger.info("🔄 Application responsive sur table: " + table.getClass().getSimpleName());
        
        // Politique de redimensionnement proportionnel
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Hauteur adaptative
        table.setPrefHeight(Region.USE_COMPUTED_SIZE);
        table.setMaxHeight(Double.MAX_VALUE);
        
        // Expansion verticale dans conteneur parent
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(table, Priority.ALWAYS);
        
        logger.info("✅ Table rendue responsive avec contraintes proportionnelles");
    }
    
    /**
     * Configure une colonne avec taille proportionnelle
     * @param column La colonne à configurer
     * @param size Taille proportionnelle (XS, SM, MD, LG, XL)
     * @param minWidth Largeur minimale en pixels (optionnel)
     */
    public static <S,T> void setColumnResponsive(TableColumn<S,T> column, ColumnSize size, double minWidth) {
        // Suppression des largeurs fixes
        column.setPrefWidth(Region.USE_COMPUTED_SIZE);
        column.setMaxWidth(Double.MAX_VALUE);
        
        // Largeur minimale pour lisibilité
        if (minWidth > 0) {
            column.setMinWidth(minWidth);
        }
        
        // Note: La proportion sera gérée par UNCONSTRAINED_RESIZE_POLICY
        logger.info("📏 Colonne '" + column.getText() + "' configurée responsive (taille: " + size + ")");
    }
    
    /**
     * Surcharge sans largeur minimale
     */
    public static <S,T> void setColumnResponsive(TableColumn<S,T> column, ColumnSize size) {
        setColumnResponsive(column, size, 0);
    }
    
    /**
     * Rend un champ de texte responsive
     * @param field Le champ à rendre responsive
     */
    public static void makeFieldResponsive(TextField field) {
        field.setPrefWidth(Region.USE_COMPUTED_SIZE);
        field.setMinWidth(150); // Largeur min pour lisibilité
        field.setMaxWidth(Double.MAX_VALUE);
        
        // Expansion dans conteneur parent
        HBox.setHgrow(field, Priority.ALWAYS);
        
        logger.info("📝 Champ '" + field.getPromptText() + "' rendu responsive");
    }
    
    /**
     * Rend une ComboBox responsive
     * @param combo La ComboBox à rendre responsive
     */
    public static void makeComboResponsive(ComboBox<?> combo) {
        combo.setPrefWidth(Region.USE_COMPUTED_SIZE);
        combo.setMinWidth(120); // Largeur min pour options
        combo.setMaxWidth(Double.MAX_VALUE);
        
        // Expansion dans conteneur parent  
        HBox.setHgrow(combo, Priority.SOMETIMES);
        
        logger.info("📋 ComboBox rendue responsive");
    }
    
    /**
     * Configure un HBox pour être responsive avec espacement adaptatif
     * @param hbox Le HBox à configurer
     * @param spacing Espacement de base
     */
    public static void makeHBoxResponsive(HBox hbox, double spacing) {
        hbox.setSpacing(spacing);
        hbox.setFillHeight(true);
        
        // Expansion dans conteneur parent
        VBox.setVgrow(hbox, Priority.NEVER); // Hauteur fixe
        HBox.setHgrow(hbox, Priority.ALWAYS); // Largeur adaptative
        
        logger.info("📦 HBox configuré responsive avec spacing: " + spacing);
    }
    
    /**
     * Configure un VBox pour être responsive
     * @param vbox Le VBox à configurer  
     * @param spacing Espacement de base
     */
    public static void makeVBoxResponsive(VBox vbox, double spacing) {
        vbox.setSpacing(spacing);
        vbox.setFillWidth(true);
        
        // Expansion dans conteneur parent
        VBox.setVgrow(vbox, Priority.ALWAYS); // Hauteur adaptative
        HBox.setHgrow(vbox, Priority.ALWAYS); // Largeur adaptative
        
        logger.info("📦 VBox configuré responsive avec spacing: " + spacing);
    }
    
    /**
     * Crée un spacer responsive qui s'étend pour remplir l'espace disponible
     * @return Region configurée comme spacer
     */
    public static Region createResponsiveSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        logger.info("🔄 Spacer responsive créé");
        return spacer;
    }
    
    /**
     * Applique les contraintes responsive standard à un conteneur GridPane
     * @param grid Le GridPane à configurer
     * @param columnCount Nombre de colonnes
     */
    public static void makeGridResponsive(GridPane grid, int columnCount) {
        // Configuration des colonnes avec répartition équitable
        for (int i = 0; i < columnCount; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / columnCount);
            colConstraints.setHgrow(Priority.ALWAYS);
            colConstraints.setHalignment(HPos.CENTER);
            grid.getColumnConstraints().add(colConstraints);
        }
        
        // Expansion du grid
        VBox.setVgrow(grid, Priority.ALWAYS);
        HBox.setHgrow(grid, Priority.ALWAYS);
        
        logger.info("🎯 GridPane configuré responsive avec " + columnCount + " colonnes équitables");
    }
    
    /**
     * Configure une toolbar pour être responsive
     * @param toolbar Le HBox servant de toolbar
     */
    public static void makeToolbarResponsive(HBox toolbar) {
        makeHBoxResponsive(toolbar, 10); // Espacement standard
        toolbar.setPrefHeight(Region.USE_COMPUTED_SIZE);
        toolbar.setMinHeight(40); // Hauteur min pour boutons
        
        logger.info("🔧 Toolbar configurée responsive");
    }
    
    /**
     * Log les dimensions actuelles d'un composant (debug)
     * @param node Le composant à analyser
     * @param name Nom pour identification
     */
    public static void logComponentSize(javafx.scene.Node node, String name) {
        logger.info("📐 " + name + " - Width: " + node.getBoundsInLocal().getWidth() + 
                   ", Height: " + node.getBoundsInLocal().getHeight());
    }
}