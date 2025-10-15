package com.magsav.util;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;

/**
 * Classe utilitaire pour la création d'éléments d'interface utilisateur répétitifs
 */
public class UIUtils {
    
    /**
     * Crée un HBox avec un label et un champ de recherche
     */
    public static HBox createSearchBox(String labelText, TextField searchField, Button searchButton) {
        Label label = new Label(labelText);
        label.setPrefWidth(80);
        
        searchField.setPromptText("Rechercher...");
        searchField.setPrefWidth(200);
        
        searchButton.setText("🔍");
        searchButton.setPrefWidth(40);
        
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.getChildren().addAll(label, searchField, searchButton);
        
        return searchBox;
    }
    
    /**
     * Crée un ComboBox avec les options fournies
     */
    public static ComboBox<String> createFilterComboBox(String... options) {
        ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList(options));
        comboBox.setValue(options[0]); // Sélectionner le premier par défaut
        comboBox.setPrefWidth(150);
        return comboBox;
    }
    
    /**
     * Crée un HBox pour les filtres avec un label et un ComboBox
     */
    public static HBox createFilterBox(String labelText, ComboBox<String> filterCombo) {
        Label label = new Label(labelText);
        label.setPrefWidth(80);
        
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        filterBox.getChildren().addAll(label, filterCombo);
        
        return filterBox;
    }
    
    /**
     * Crée un HBox pour les statistiques avec plusieurs labels
     */
    public static HBox createStatsBox(Label... labels) {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(5));
        statsBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 5;");
        
        for (Label label : labels) {
            label.setStyle("-fx-font-weight: bold;");
            statsBox.getChildren().add(label);
        }
        
        return statsBox;
    }
    
    /**
     * Crée une toolbar avec des boutons d'action
     */
    public static ToolBar createActionToolBar(Button... buttons) {
        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(buttons);
        return toolBar;
    }
    
    /**
     * Crée un bouton avec style et taille standardisés
     */
    public static Button createStandardButton(String text, String style) {
        Button button = new Button(text);
        button.setStyle(style);
        button.setPrefWidth(100);
        return button;
    }
    
    /**
     * Crée un VBox principal avec espacement et padding standardisés
     */
    public static VBox createMainVBox() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        return vbox;
    }
    
    /**
     * Configure une TableView avec des propriétés par défaut
     */
    public static <T> void configureTableView(TableView<T> table) {
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // Double-clic pour édition - peut être surchargé
                }
            });
            return row;
        });
        
        // Utiliser la nouvelle méthode pour la politique de redimensionnement
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
    
    /**
     * Crée une colonne de tableau standard
     */
    public static <S, T> TableColumn<S, T> createTableColumn(String title, String property, double prefWidth) {
        TableColumn<S, T> column = new TableColumn<>(title);
        column.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(property));
        column.setPrefWidth(prefWidth);
        return column;
    }
}