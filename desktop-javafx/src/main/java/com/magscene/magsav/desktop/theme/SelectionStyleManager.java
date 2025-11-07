package com.magscene.magsav.desktop.theme;

import javafx.scene.control.*;
import javafx.util.Callback;

/**
 * Gestionnaire centralisé des styles de sélection pour TableView et ListView
 * Applique la couleur de sélection #142240 de manière uniforme dans toute l'application
 */
public class SelectionStyleManager {
    
    private static SelectionStyleManager instance;
    
    private SelectionStyleManager() {}
    
    public static SelectionStyleManager getInstance() {
        if (instance == null) {
            instance = new SelectionStyleManager();
        }
        return instance;
    }
    
    /**
     * Applique le style de sélection uniforme à une TableView
     */
    public <T> void applySelectionStyle(TableView<T> tableView) {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection prioritaire
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                } else {
                    // Style par défaut ou basé sur les données de l'item
                    row.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            return row;
        });
    }
    
    /**
     * Applique le style de sélection uniforme à une ListView
     */
    public <T> void applySelectionStyle(ListView<T> listView) {
        listView.setCellFactory(lv -> {
            ListCell<T> cell = new ListCell<T>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                    }
                }
            };
            
            // Runnable pour mettre à jour le style de sélection
            Runnable updateStyle = () -> {
                if (cell.isEmpty()) {
                    cell.setStyle("");
                } else if (cell.isSelected()) {
                    // Style de sélection prioritaire
                    cell.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                                "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                                "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                                "-fx-border-width: 2px;");
                } else {
                    // Style par défaut
                    cell.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            cell.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            cell.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            
            return cell;
        });
    }
    
    /**
     * Applique le style de sélection avec une factory personnalisée pour TableView
     */
    public <T> Callback<TableView<T>, TableRow<T>> createTableRowFactory() {
        return tv -> {
            TableRow<T> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection prioritaire
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                } else {
                    // Style par défaut
                    row.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            return row;
        };
    }
    
    /**
     * Applique le style de sélection avec une factory personnalisée pour ListView
     */
    public <T> Callback<ListView<T>, ListCell<T>> createListCellFactory() {
        return lv -> {
            ListCell<T> cell = new ListCell<T>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                    }
                }
                

            };
            
            // Écouter les changements de sélection
            cell.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
                if (!cell.isEmpty()) {
                    if (cell.isSelected()) {
                        // Style de sélection prioritaire
                        cell.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                                   "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                                   "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                                   "-fx-border-width: 2px;");
                    } else {
                        // Style par défaut
                        cell.setStyle("");
                    }
                }
            });
            
            return cell;
        };
    }
}