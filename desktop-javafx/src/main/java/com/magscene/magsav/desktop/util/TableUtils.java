package com.magscene.magsav.desktop.util;

import com.magscene.magsav.desktop.theme.ThemeConstants;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Utilitaires centralisés pour la gestion des TableView
 * 
 * Cette classe standardise la création et la configuration des tableaux
 * utilisés dans l'application MAGSAV-3.0.
 * 
 * 🔧 PHASE 3: Refactoring des patterns de tableaux
 */
public class TableUtils {
    
    private static final Logger logger = Logger.getLogger(TableUtils.class.getName());
    
    // ========================================
    // 📋 CRÉATION DE COLONNES STANDARDISÉES; // ========================================
    
    /**
     * Crée une colonne de texte simple avec sizing responsive
     * @param <T> Type des données de la ligne
     * @param title Titre de la colonne
     * @param propertyName Nom de la propriété à afficher
     * @param columnSize Taille responsive (XS, SM, MD, LG, XL)
     * @param minWidth Largeur minimale
     * @return TableColumn configurée
     */
    public static <T> TableColumn<T, String> createTextColumn(String title, String propertyName, 
                                                             ResponsiveUtils.ColumnSize columnSize, double minWidth) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        ResponsiveUtils.setColumnResponsive(column, columnSize, minWidth);
        
        logger.fine("📋 Colonne texte créée : " + title + " (" + propertyName + ")");
        return column;
    }
    
    /**
     * Crée une colonne avec transformation de données personnalisée
     * @param <T> Type des données de la ligne
     * @param title Titre de la colonne
     * @param valueExtractor Fonction d'extraction de valeur
     * @param columnSize Taille responsive
     * @param minWidth Largeur minimale
     * @return TableColumn configurée
     */
    public static <T> TableColumn<T, String> createCustomColumn(String title, 
                                                               Function<T, String> valueExtractor,
                                                               ResponsiveUtils.ColumnSize columnSize, 
                                                               double minWidth) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(data -> new SimpleStringProperty(valueExtractor.apply(data.getValue())));
        ResponsiveUtils.setColumnResponsive(column, columnSize, minWidth);
        
        logger.fine("📋 Colonne personnalisée créée : " + title);
        return column;
    }
    
    /**
     * Crée une colonne avec affichage de statut coloré
     * @param <T> Type des données de la ligne
     * @param title Titre de la colonne
     * @param propertyName Nom de la propriété contenant le statut
     * @param columnSize Taille responsive
     * @param minWidth Largeur minimale
     * @return TableColumn avec rendu de statut coloré
     */
    public static <T> TableColumn<T, String> createStatusColumn(String title, String propertyName,
                                                               ResponsiveUtils.ColumnSize columnSize, 
                                                               double minWidth) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        ResponsiveUtils.setColumnResponsive(column, columnSize, minWidth);
        
        // Application du style de statut automatique
        column.setCellFactory(col -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText("");
                    setStyle("");
                } else {
                    setText(status);
                    setStyle(ThemeConstants.getStatusTextStyle(status));
                }
            }
        });
        
        logger.fine("📋 Colonne de statut créée : " + title + " (" + propertyName + ")");
        return column;
    }
    
    /**
     * Crée une colonne avec indicateur visuel coloré (cercle + texte)
     * @param <T> Type des données de la ligne
     * @param title Titre de la colonne  
     * @param propertyName Nom de la propriété contenant le statut
     * @param columnSize Taille responsive
     * @param minWidth Largeur minimale
     * @return TableColumn avec indicateur visuel
     */
    public static <T> TableColumn<T, HBox> createIndicatorColumn(String title, String propertyName,
                                                                ResponsiveUtils.ColumnSize columnSize,
                                                                double minWidth) {
        TableColumn<T, HBox> column = new TableColumn<>(title);
        ResponsiveUtils.setColumnResponsive(column, columnSize, minWidth);
        
        column.setCellValueFactory(data -> {
            // Extraction de la valeur via réflection ou méthode directe
            String status = extractPropertyValue(data.getValue(), propertyName);
            HBox statusBox = createStatusIndicator(status);
            return new javafx.beans.property.SimpleObjectProperty<>(statusBox);
        });
        
        logger.fine("📋 Colonne avec indicateur créée : " + title + " (" + propertyName + ")");
        return column;
    }
    
    /**
     * Crée une colonne pour affichage de prix formaté
     * @param <T> Type des données de la ligne
     * @param title Titre de la colonne
     * @param propertyName Nom de la propriété contenant le prix
     * @param columnSize Taille responsive
     * @param minWidth Largeur minimale
     * @return TableColumn avec formatage de prix
     */
    public static <T> TableColumn<T, String> createPriceColumn(String title, String propertyName,
                                                              ResponsiveUtils.ColumnSize columnSize,
                                                              double minWidth) {
        TableColumn<T, String> column = new TableColumn<>(title);
        ResponsiveUtils.setColumnResponsive(column, columnSize, minWidth);
        
        column.setCellValueFactory(data -> {
            Object priceValue = extractPropertyValue(data.getValue(), propertyName);
            String formattedPrice = "0 €";
            
            if (priceValue instanceof Number) {
                formattedPrice = String.format("%.0f €", ((Number) priceValue).doubleValue());
            }
            
            return new SimpleStringProperty(formattedPrice);
        });
        
        logger.fine("📋 Colonne de prix créée : " + title + " (" + propertyName + ")");
        return column;
    }
    
    // ========================================
    // 🎨 CRÉATION D'INDICATEURS VISUELS; // ========================================
    
    /**
     * Crée un indicateur visuel de statut (cercle coloré + label)
     * @param status Le statut à représenter
     * @return HBox avec cercle coloré et texte
     */
    public static HBox createStatusIndicator(String status) {
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle indicator = new Circle(6);
        indicator.setFill(getStatusColor(status));
        
        Label statusLabel = new Label(status != null ? status : "");
        statusBox.getChildren().addAll(indicator, statusLabel);
        
        return statusBox;
    }
    
    /**
     * Détermine la couleur d'un statut
     * @param status Le statut
     * @return Color correspondante
     */
    public static Color getStatusColor(String status) {
        if (status == null) return Color.web(ThemeConstants.TEXT_SECONDARY);
        
        return switch (status.toLowerCase()) {
            case "ouverte", "actif", "disponible", "operationnel", "en cours" -> 
                Color.web(ThemeConstants.SUCCESS_COLOR);
            case "attente pieces", "maintenance", "en conge" -> 
                Color.web(ThemeConstants.WARNING_COLOR);
            case "fermee", "annulee", "hors service", "panne", "inactif" -> 
                Color.web(ThemeConstants.ERROR_COLOR);
            case "resolue", "termine" -> 
                Color.web(ThemeConstants.INFO_COLOR);
            default -> 
                Color.web(ThemeConstants.TEXT_SECONDARY);
        };
    }
    
    // ========================================
    // ⚙️ CONFIGURATION DE TABLEAUX; // ========================================
    
    /**
     * Configure un TableView avec les paramètres standards MAGSAV
     * @param <T> Type des données du tableau
     * @param table Le TableView à configurer
     * @param placeholderText Texte à afficher quand le tableau est vide
     */
    public static <T> void configureStandardTable(TableView<T> table, String placeholderText) {
        // Application du style responsive
        ResponsiveUtils.makeTableResponsive(table);
        
        // Configuration du placeholder
        if (placeholderText != null && !placeholderText.trim().isEmpty()) {
            table.setPlaceholder(new Label(placeholderText));
        }
        
        // Style de sélection standardisé
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style de sélection
            Runnable updateStyle = () -> ViewUtils.applySelectionStyle(row, row.isSelected());
            
            // Écouters pour les changements
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            return row;
        });
        
        // Ajout de la classe CSS
        table.getStyleClass().add("standard-table");
        
        logger.fine("⚙️ Table configurée avec placeholder : " + placeholderText);
    }
    
    /**
     * Configure les événements de double-clic standard sur une table
     * @param <T> Type des données du tableau
     * @param table Le TableView
     * @param onDoubleClick Action à exécuter lors du double-clic
     */
    public static <T> void configureDoubleClickAction(TableView<T> table, Runnable onDoubleClick) {
        table.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            
            // Configuration du style de base
            Runnable updateStyle = () -> ViewUtils.applySelectionStyle(row, row.isSelected());
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
            
            // Gestion du double-clic
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2 && onDoubleClick != null) {
                    onDoubleClick.run();
                }
            });
            
            return row;
        });
        
        logger.fine("⚙️ Double-clic configuré sur la table");
    }
    
    // ========================================
    // 🔧 MÉTHODES UTILITAIRES INTERNES; // ========================================
    
    /**
     * Extrait la valeur d'une propriété d'un objet par réflection simple
     * @param object L'objet source
     * @param propertyName Le nom de la propriété
     * @return La valeur extraite ou null
     */
    @SuppressWarnings("unchecked")
    private static String extractPropertyValue(Object object, String propertyName) {
        try {
            if (object instanceof java.util.Map) {
                java.util.Map<String, Object> map = (java.util.Map<String, Object>) object;
                Object value = map.get(propertyName);
                return value != null ? value.toString() : "";
            }
            
            // Pour les objets standards, utiliser la réflection
            String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            java.lang.reflect.Method getter = object.getClass().getMethod(getterName);
            Object value = getter.invoke(object);
            return value != null ? value.toString() : "";
            
        } catch (Exception e) {
            logger.warning("⚠️ Impossible d'extraire la propriété " + propertyName + " : " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Crée un ensemble de colonnes standard pour un module de gestion
     * @param <T> Type des données
     * @param hasId Si une colonne ID est nécessaire
     * @param hasName Si une colonne Nom est nécessaire
     * @param hasStatus Si une colonne Statut est nécessaire
     * @param hasActions Si une colonne Actions est nécessaire
     * @return Array de colonnes configurées
     */
    @SafeVarargs
    public static <T> TableColumn<T, ?>[] createStandardColumns(boolean hasId, boolean hasName, 
                                                               boolean hasStatus, boolean hasActions,
                                                               TableColumn<T, ?>... additionalColumns) {
        java.util.List<TableColumn<T, ?>> columns = new java.util.ArrayList<>();
        
        if (hasId) {
            columns.add(createTextColumn("ID", "id", ResponsiveUtils.ColumnSize.XS, 40));
        }
        
        if (hasName) {
            columns.add(createTextColumn("Nom", "name", ResponsiveUtils.ColumnSize.MD, 150));
        }
        
        if (hasStatus) {
            columns.add(createStatusColumn("Statut", "status", ResponsiveUtils.ColumnSize.SM, 100));
        }
        
        // Ajout des colonnes supplémentaires
        if (additionalColumns != null) {
            columns.addAll(java.util.Arrays.asList(additionalColumns));
        }
        
        logger.fine("📋 " + columns.size() + " colonnes standard créées");
        @SuppressWarnings("unchecked")
        TableColumn<T, ?>[] result = columns.toArray(new TableColumn[0]);
        return result;
    }
}