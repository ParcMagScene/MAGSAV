package com.magsav.gui.utils;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import java.util.List;

/**
 * Utilitaire pour créer les composants UI répétitifs
 * Évite la duplication de code dans les contrôleurs
 */
public class TabBuilderUtils {
    
    /**
     * Crée un onglet avec les propriétés de base
     */
    public static Tab createBasicTab(String title) {
        Tab tab = new Tab(title);
        tab.setClosable(false);
        return tab;
    }
    
    /**
     * Crée le contenu principal d'un onglet avec les styles appropriés
     */
    public static VBox createTabContent() {
        VBox content = new VBox();
        content.setSpacing(16);
        content.getStyleClass().addAll("main-content", "tab-content-margins");
        return content;
    }
    
    /**
     * Crée une boîte d'en-tête avec statistiques alignées à droite
     * REMARQUE: Selon spécifications "Pas de légende en haut (doublon avec les titres des onglets)", 
     * cette méthode sera progressivement supprimée
     */
    public static HBox createHeaderWithStats(HBox statsBox) {
        HBox headerBox = new HBox();
        headerBox.setSpacing(20);
        headerBox.getStyleClass().add("header-box");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        headerBox.getChildren().addAll(spacer, statsBox);
        return headerBox;
    }
    
    /**
     * Crée uniquement les statistiques sans légende en haut (nouvelle approche unifiée)
     */
    public static HBox createStatsOnlyBox(List<Label> statsLabels) {
        HBox statsBox = createStatsBox(statsLabels);
        statsBox.getStyleClass().add("stats-only-box");
        return statsBox;
    }
    
    /**
     * Crée une boîte de statistiques avec les labels fournis
     */
    public static HBox createStatsBox(List<Label> statsLabels) {
        HBox statsBox = new HBox();
        statsBox.setSpacing(15);
        
        for (Label label : statsLabels) {
            label.getStyleClass().add("stats-label");
        }
        
        statsBox.getChildren().addAll(statsLabels);
        return statsBox;
    }
    
    /**
     * Crée une barre de contrôles unifiée: boutons à gauche, filtres au-dessus 
     * (selon spécifications: Position des boutons: à gauche. Filtres pour tous les panneaux. Boutons au dessus des filtres)
     */
    public static VBox createUnifiedControlsLayout(ComboBox<String> typeFilter, TextField searchField, 
                                                  Button... actionButtons) {
        VBox layout = new VBox();
        layout.setSpacing(10);
        layout.getStyleClass().add("unified-controls");
        
        // Boutons d'action au-dessus
        HBox buttonsBox = new HBox();
        buttonsBox.setSpacing(10);
        buttonsBox.getStyleClass().add("action-buttons-box");
        
        for (Button button : actionButtons) {
            buttonsBox.getChildren().add(button);
        }
        
        // Filtres en dessous
        HBox filtersBox = new HBox();
        filtersBox.setSpacing(10);
        filtersBox.getStyleClass().add("filters-box");
        
        Label filterLabel = new Label("Filtrer:");
        filterLabel.getStyleClass().add("filter-label");
        
        filtersBox.getChildren().addAll(filterLabel, typeFilter, searchField);
        
        layout.getChildren().addAll(buttonsBox, filtersBox);
        
        return layout;
    }
    
    /**
     * Ancienne méthode conservée pour compatibilité mais deprecated
     * @deprecated Utiliser createUnifiedControlsLayout() pour la nouvelle interface unifiée
     */
    @Deprecated
    public static HBox createControlsBar(ComboBox<String> typeFilter, TextField searchField, 
                                        Button searchBtn, Button... actionButtons) {
        HBox controlsBox = new HBox();
        controlsBox.setSpacing(12);
        controlsBox.getStyleClass().add("controls-box");
        
        Label filterLabel = new Label("Filtrer:");
        filterLabel.getStyleClass().add("filter-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        controlsBox.getChildren().add(filterLabel);
        controlsBox.getChildren().add(typeFilter);
        controlsBox.getChildren().add(searchField);
        controlsBox.getChildren().add(searchBtn);
        controlsBox.getChildren().add(spacer);
        
        for (Button button : actionButtons) {
            controlsBox.getChildren().add(button);
        }
        
        return controlsBox;
    }
    
    /**
     * Configure les propriétés de base d'une TableView
     */
    public static <T> void configureBasicTable(TableView<T> table) {
        table.getStyleClass().add("table-view");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
    
    /**
     * Crée un SplitPane horizontal avec table à gauche et panel détails à droite
     */
    public static <T> SplitPane createTableWithDetailPanel(TableView<T> table, VBox detailPanel) {
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("split-pane");
        splitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        
        splitPane.getItems().addAll(table, detailPanel);
        splitPane.setDividerPositions(0.65); // 65% table, 35% détails
        
        return splitPane;
    }
    
    /**
     * Crée un bouton avec icône et texte
     */
    public static Button createIconButton(String text, String styleClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("btn", styleClass);
        return button;
    }
    
    /**
     * Configure les propriétés communes d'un ComboBox
     */
    public static void configureComboBox(ComboBox<String> comboBox, double prefWidth) {
        comboBox.getStyleClass().add("combo-box");
        comboBox.setPrefWidth(prefWidth);
    }
    
    /**
     * Configure les propriétés communes d'un TextField de recherche
     */
    public static void configureSearchField(TextField searchField, String promptText, double prefWidth) {
        searchField.getStyleClass().add("search-field");
        searchField.setPromptText(promptText);
        searchField.setPrefWidth(prefWidth);
    }
}