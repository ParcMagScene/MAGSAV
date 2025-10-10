package com.magsav.ui.layout;

import javafx.scene.layout.*;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.beans.property.DoubleProperty;
import javafx.beans.binding.Bindings;
import com.magsav.util.AppLogger;

/**
 * Gestionnaire de layouts responsifs pour MAGSAV
 * Adapte la disposition selon la taille de l'écran
 */
public class ResponsiveLayout {
    
    // Breakpoints standard
    public enum Breakpoint {
        MOBILE(0, 600),
        TABLET(600, 900),
        DESKTOP(900, 1200),
        LARGE_DESKTOP(1200, Double.MAX_VALUE);
        
        private final double minWidth;
        private final double maxWidth;
        
        Breakpoint(double minWidth, double maxWidth) {
            this.minWidth = minWidth;
            this.maxWidth = maxWidth;
        }
        
        public boolean matches(double width) {
            return width >= minWidth && width < maxWidth;
        }
        
        public double getMinWidth() { return minWidth; }
        public double getMaxWidth() { return maxWidth; }
    }
    
    /**
     * Crée un conteneur en grille responsive
     */
    public static GridPane createResponsiveGrid(int columns) {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setPadding(new Insets(24));
        
        // Contraintes de colonnes flexibles
        for (int i = 0; i < columns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / columns);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }
        
        return grid;
    }
    
    /**
     * Crée un conteneur flexible horizontal
     */
    public static HBox createFlexContainer(double spacing) {
        HBox container = new HBox(spacing);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(16));
        return container;
    }
    
    /**
     * Crée un conteneur flexible vertical
     */
    public static VBox createVerticalContainer(double spacing) {
        VBox container = new VBox(spacing);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(16));
        return container;
    }
    
    /**
     * Crée un layout de type "card deck" responsive
     */
    public static FlowPane createCardDeck() {
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setPadding(new Insets(24));
        flowPane.setAlignment(Pos.TOP_LEFT);
        return flowPane;
    }
    
    /**
     * Crée un layout de sidebar avec contenu principal
     */
    public static BorderPane createSidebarLayout(Node sidebar, Node content) {
        BorderPane layout = new BorderPane();
        layout.setLeft(sidebar);
        layout.setCenter(content);
        
        // Marges pour séparer le sidebar du contenu
        BorderPane.setMargin(sidebar, new Insets(0, 16, 0, 0));
        BorderPane.setMargin(content, new Insets(0, 0, 0, 16));
        
        return layout;
    }
    
    /**
     * Applique un comportement responsive à un conteneur
     */
    public static void makeResponsive(Region container, DoubleProperty widthProperty) {
        // Adaptation automatique selon la largeur
        widthProperty.addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth.doubleValue();
            
            if (Breakpoint.MOBILE.matches(width)) {
                applyMobileLayout(container);
            } else if (Breakpoint.TABLET.matches(width)) {
                applyTabletLayout(container);
            } else if (Breakpoint.DESKTOP.matches(width)) {
                applyDesktopLayout(container);
            } else {
                applyLargeDesktopLayout(container);
            }
        });
    }
    
    /**
     * Applique les styles pour mobile
     */
    private static void applyMobileLayout(Region container) {
        container.setPadding(new Insets(8));
        
        if (container instanceof GridPane) {
            GridPane grid = (GridPane) container;
            grid.setHgap(8);
            grid.setVgap(8);
            
            // Force une seule colonne sur mobile
            grid.getColumnConstraints().clear();
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }
        
        AppLogger.info("Layout mobile appliqué");
    }
    
    /**
     * Applique les styles pour tablette
     */
    private static void applyTabletLayout(Region container) {
        container.setPadding(new Insets(16));
        
        if (container instanceof GridPane) {
            GridPane grid = (GridPane) container;
            grid.setHgap(12);
            grid.setVgap(12);
            
            // Deux colonnes sur tablette
            grid.getColumnConstraints().clear();
            for (int i = 0; i < 2; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(50);
                col.setHgrow(Priority.ALWAYS);
                grid.getColumnConstraints().add(col);
            }
        }
        
        AppLogger.info("Layout tablette appliqué");
    }
    
    /**
     * Applique les styles pour desktop
     */
    private static void applyDesktopLayout(Region container) {
        container.setPadding(new Insets(24));
        
        if (container instanceof GridPane) {
            GridPane grid = (GridPane) container;
            grid.setHgap(16);
            grid.setVgap(16);
            
            // Trois colonnes sur desktop
            grid.getColumnConstraints().clear();
            for (int i = 0; i < 3; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(33.33);
                col.setHgrow(Priority.ALWAYS);
                grid.getColumnConstraints().add(col);
            }
        }
        
        AppLogger.info("Layout desktop appliqué");
    }
    
    /**
     * Applique les styles pour grand écran
     */
    private static void applyLargeDesktopLayout(Region container) {
        container.setPadding(new Insets(32));
        
        if (container instanceof GridPane) {
            GridPane grid = (GridPane) container;
            grid.setHgap(20);
            grid.setVgap(20);
            
            // Quatre colonnes sur grand écran
            grid.getColumnConstraints().clear();
            for (int i = 0; i < 4; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(25);
                col.setHgrow(Priority.ALWAYS);
                grid.getColumnConstraints().add(col);
            }
        }
        
        AppLogger.info("Layout grand écran appliqué");
    }
    
    /**
     * Crée un conteneur avec espacement adaptatif
     */
    public static VBox createAdaptiveSpacing(DoubleProperty widthProperty) {
        VBox container = new VBox();
        
        // Liaison dynamique de l'espacement selon la largeur
        container.spacingProperty().bind(
            Bindings.when(widthProperty.lessThan(600))
                .then(8.0)
                .otherwise(
                    Bindings.when(widthProperty.lessThan(900))
                        .then(12.0)
                        .otherwise(16.0)
                )
        );
        
        return container;
    }
    
    /**
     * Crée des marges adaptatives
     */
    public static Insets createAdaptiveInsets(double width) {
        if (Breakpoint.MOBILE.matches(width)) {
            return new Insets(8);
        } else if (Breakpoint.TABLET.matches(width)) {
            return new Insets(16);
        } else if (Breakpoint.DESKTOP.matches(width)) {
            return new Insets(24);
        } else {
            return new Insets(32);
        }
    }
    
    /**
     * Utilitaire pour centrer un contenu
     */
    public static StackPane createCenteredContainer(Node content) {
        StackPane container = new StackPane(content);
        container.setAlignment(Pos.CENTER);
        return container;
    }
    
    /**
     * Crée un conteneur avec défilement
     */
    public static ScrollPane createScrollableContainer(Node content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        return scroll;
    }
    
    /**
     * Applique une contrainte de taille maximale
     */
    public static void applyMaxWidth(Region node, double maxWidth) {
        node.setMaxWidth(maxWidth);
        node.setPrefWidth(Region.USE_COMPUTED_SIZE);
    }
    
    /**
     * Applique une contrainte de taille minimale
     */
    public static void applyMinWidth(Region node, double minWidth) {
        node.setMinWidth(minWidth);
        node.setPrefWidth(Region.USE_COMPUTED_SIZE);
    }
}