package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe abstraite pour les dialogs de détail d'entités
 * Factorisation du code commun entre VehicleDetailDialog, ClientDetailDialog,
 * PersonnelDetailDialog, EquipmentDetailDialog, SupplierDetailDialog, etc.
 * 
 * @param <T> Type de données retournées par le dialog
 */
public abstract class AbstractDetailDialog<T> extends Dialog<T> {
    
    // Services
    protected final ApiService apiService;
    protected final MediaService mediaService;
    
    // Données de l'entité
    protected final Map<String, Object> entityData;
    
    // État d'édition
    protected boolean editMode = false;
    protected boolean editButtonInitialized = false;
    protected Button editSaveButton;
    
    // Layout principal
    protected VBox contentBox;
    protected BorderPane mainLayout;
    
    // ButtonTypes
    protected ButtonType editButtonType;
    protected ButtonType closeButtonType;
    
    // Configuration
    protected final String dialogTitle;
    protected final String headerStyle;
    protected final double prefWidth;
    protected final double prefHeight;
    
    /**
     * Constructeur avec configuration par défaut
     */
    protected AbstractDetailDialog(ApiService apiService, Map<String, Object> data, 
                                   String dialogTitle, String headerStyle) {
        this(apiService, data, dialogTitle, headerStyle, 700, 600);
    }
    
    /**
     * Constructeur avec configuration complète
     */
    protected AbstractDetailDialog(ApiService apiService, Map<String, Object> data,
                                   String dialogTitle, String headerStyle,
                                   double prefWidth, double prefHeight) {
        this.apiService = apiService;
        this.mediaService = MediaService.getInstance();
        this.entityData = new HashMap<>(data);
        this.dialogTitle = dialogTitle;
        this.headerStyle = headerStyle;
        this.prefWidth = prefWidth;
        this.prefHeight = prefHeight;
        
        setupDialog();
        createContent();
    }
    
    // =====================================================
    // MÉTHODES ABSTRAITES À IMPLÉMENTER
    // =====================================================
    
    /**
     * Crée le header de la fiche (image, titre, sous-titre)
     */
    protected abstract HBox createHeader();
    
    /**
     * Crée les sections de contenu (informations générales, etc.)
     */
    protected abstract void rebuildContent();
    
    /**
     * Sauvegarde les données modifiées
     */
    protected abstract void saveData();
    
    /**
     * Retourne les données de l'entité (pour le result converter)
     */
    protected abstract T getResultData();
    
    // =====================================================
    // CONFIGURATION DU DIALOG
    // =====================================================
    
    protected void setupDialog() {
        setTitle(dialogTitle);
        setHeaderText(null);
        
        // Boutons
        editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        // Taille
        getDialogPane().setPrefSize(prefWidth, prefHeight);
        getDialogPane().setMinWidth(prefWidth - 50);
        getDialogPane().setMinHeight(prefHeight - 50);
        
        // Thème
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        // Configuration des boutons après le rendu
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        // Result converter
        setResultConverter(buttonType -> editMode ? getResultData() : null);
    }
    
    protected void setupEditButton() {
        Button editButton = (Button) getDialogPane().lookupButton(editButtonType);
        if (editButton != null) {
            editSaveButton = editButton;
            updateEditButtonStyle(editButton);
            
            if (!editButtonInitialized) {
                editButtonInitialized = true;
                editButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                    e.consume();
                    toggleEditMode();
                });
            }
        }
    }
    
    protected void updateEditButtonStyle(Button editButton) {
        String buttonStyle = editMode 
            ? ThemeConstants.DIALOG_SAVE_BUTTON_STYLE
            : ThemeConstants.DIALOG_EDIT_BUTTON_STYLE;
        
        editButton.setText(editMode ? "Enregistrer" : "Modifier");
        editButton.setStyle(buttonStyle);
        editButton.setMinWidth(120);
        editButton.setPrefWidth(120);
        
        final String finalButtonStyle = buttonStyle;
        editButton.setOnMouseEntered(e -> editButton.setStyle(
            finalButtonStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        ));
        editButton.setOnMouseExited(e -> editButton.setStyle(finalButtonStyle));
    }
    
    protected void setupCloseButton() {
        Button closeButton = (Button) getDialogPane().lookupButton(closeButtonType);
        if (closeButton != null) {
            closeButton.setStyle(ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE);
            closeButton.setMinWidth(100);
            closeButton.setPrefWidth(100);
            
            closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            ));
            closeButton.setOnMouseExited(e -> closeButton.setStyle(ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE));
        }
    }
    
    protected void createContent() {
        mainLayout = new BorderPane();
        mainLayout.setStyle(ThemeConstants.DIALOG_CONTENT_STYLE);
        
        // Header
        mainLayout.setTop(createHeader());
        
        // Contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle(ThemeConstants.DIALOG_CONTENT_STYLE);
        
        rebuildContent();
        
        scrollPane.setContent(contentBox);
        mainLayout.setCenter(scrollPane);
        
        getDialogPane().setContent(mainLayout);
    }
    
    // =====================================================
    // GESTION DU MODE ÉDITION
    // =====================================================
    
    protected void toggleEditMode() {
        if (editMode) {
            // Sauvegarder les modifications
            saveData();
        }
        
        editMode = !editMode;
        rebuildContent();
        
        if (editSaveButton != null) {
            updateEditButtonStyle(editSaveButton);
        }
    }
    
    // =====================================================
    // UTILITAIRES POUR CRÉER LES SECTIONS
    // =====================================================
    
    /**
     * Crée une section avec titre et contenu
     */
    protected VBox createSection(String title, Node content) {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web(ThemeConstants.PRIMARY_COLOR));
        titleLabel.setStyle("-fx-padding: 0 0 5 0; -fx-border-color: " + ThemeConstants.PRIMARY_COLOR + 
                          "; -fx-border-width: 0 0 2 0;");
        
        section.getChildren().addAll(titleLabel, content);
        return section;
    }
    
    /**
     * Crée une ligne d'information avec label et valeur
     */
    protected HBox createInfoRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("-fx-background-color: rgba(248,249,250,0.5); -fx-background-radius: 4;");
        
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#6C757D"));
        labelNode.setMinWidth(120);
        labelNode.setPrefWidth(120);
        
        Label valueNode = new Label(value != null && !value.isEmpty() ? value : "-");
        valueNode.setFont(Font.font("Segoe UI", 12));
        valueNode.setTextFill(Color.web("#212529"));
        valueNode.setWrapText(true);
        HBox.setHgrow(valueNode, Priority.ALWAYS);
        
        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }
    
    /**
     * Crée une ligne d'information éditable avec TextField
     */
    protected HBox createEditableRow(String label, TextField field, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("-fx-background-color: rgba(107,113,242,0.1); -fx-background-radius: 4;");
        
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#6B71F2"));
        labelNode.setMinWidth(120);
        labelNode.setPrefWidth(120);
        
        field.setText(value != null ? value : "");
        field.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        HBox.setHgrow(field, Priority.ALWAYS);
        
        row.getChildren().addAll(labelNode, field);
        return row;
    }
    
    /**
     * Crée une ligne d'information éditable avec ComboBox
     */
    protected <V> HBox createEditableRow(String label, ComboBox<V> comboBox, V value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("-fx-background-color: rgba(107,113,242,0.1); -fx-background-radius: 4;");
        
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#6B71F2"));
        labelNode.setMinWidth(120);
        labelNode.setPrefWidth(120);
        
        comboBox.setValue(value);
        comboBox.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        HBox.setHgrow(comboBox, Priority.ALWAYS);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        
        row.getChildren().addAll(labelNode, comboBox);
        return row;
    }
    
    /**
     * Crée une ligne d'information éditable avec DatePicker
     */
    protected HBox createEditableRow(String label, DatePicker datePicker, LocalDate value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 10, 5, 10));
        row.setStyle("-fx-background-color: rgba(107,113,242,0.1); -fx-background-radius: 4;");
        
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#6B71F2"));
        labelNode.setMinWidth(120);
        labelNode.setPrefWidth(120);
        
        datePicker.setValue(value);
        datePicker.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        HBox.setHgrow(datePicker, Priority.ALWAYS);
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        row.getChildren().addAll(labelNode, datePicker);
        return row;
    }
    
    /**
     * Crée une ligne d'information éditable avec TextArea
     */
    protected VBox createEditableTextArea(String label, TextArea textArea, String value) {
        VBox container = new VBox(5);
        container.setPadding(new Insets(5, 10, 5, 10));
        container.setStyle("-fx-background-color: rgba(107,113,242,0.1); -fx-background-radius: 4;");
        
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#6B71F2"));
        
        textArea.setText(value != null ? value : "");
        textArea.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        textArea.setPrefRowCount(3);
        textArea.setWrapText(true);
        VBox.setVgrow(textArea, Priority.ALWAYS);
        
        container.getChildren().addAll(labelNode, textArea);
        return container;
    }
    
    // =====================================================
    // UTILITAIRES POUR EXTRAIRE LES DONNÉES
    // =====================================================
    
    /**
     * Récupère une valeur String depuis les données
     */
    protected String getStringValue(String key) {
        Object value = entityData.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Récupère une valeur Integer depuis les données
     */
    protected Integer getIntValue(String key) {
        Object value = entityData.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Récupère une valeur Long depuis les données
     */
    protected Long getLongValue(String key) {
        Object value = entityData.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Récupère une valeur Double depuis les données
     */
    protected Double getDoubleValue(String key) {
        Object value = entityData.get(key);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Récupère une valeur Boolean depuis les données
     */
    protected Boolean getBooleanValue(String key) {
        Object value = entityData.get(key);
        if (value == null) return null;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }
    
    /**
     * Récupère une valeur LocalDate depuis les données
     */
    protected LocalDate getDateValue(String key) {
        Object value = entityData.get(key);
        if (value == null) return null;
        if (value instanceof LocalDate) return (LocalDate) value;
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    // =====================================================
    // UTILITAIRES DE FORMATAGE
    // =====================================================
    
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Formate une date pour affichage
     */
    protected String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }
    
    /**
     * Formate une date pour affichage
     */
    protected String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return "-";
        try {
            LocalDate date = LocalDate.parse(dateString);
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            return dateString;
        }
    }
    
    /**
     * Formate un statut pour affichage
     */
    protected String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "-";
        return switch (status.toUpperCase()) {
            case "AVAILABLE", "DISPONIBLE" -> "Disponible";
            case "IN_USE", "EN_SERVICE" -> "En service";
            case "MAINTENANCE", "EN_MAINTENANCE" -> "En maintenance";
            case "INACTIVE", "INACTIF" -> "Inactif";
            case "ACTIVE", "ACTIF" -> "Actif";
            case "MISSION" -> "En mission";
            case "OUT_OF_SERVICE", "HORS_SERVICE" -> "Hors service";
            case "RESERVED", "RESERVE" -> "Réservé";
            default -> status;
        };
    }
    
    /**
     * Retourne la couleur associée à un statut
     */
    protected Color getStatusColor(String status) {
        if (status == null) return Color.web("#6C757D");
        return switch (status.toUpperCase()) {
            case "AVAILABLE", "DISPONIBLE", "ACTIVE", "ACTIF" -> Color.web("#28A745");
            case "IN_USE", "EN_SERVICE", "MISSION" -> Color.web("#17A2B8");
            case "MAINTENANCE", "EN_MAINTENANCE" -> Color.web("#FFC107");
            case "INACTIVE", "INACTIF", "OUT_OF_SERVICE", "HORS_SERVICE" -> Color.web("#DC3545");
            case "RESERVED", "RESERVE" -> Color.web("#6B71F2");
            default -> Color.web("#6C757D");
        };
    }
    
    /**
     * Formate un montant pour affichage
     */
    protected String formatCurrency(Object amount) {
        if (amount == null) return "-";
        try {
            double value = amount instanceof Number 
                ? ((Number) amount).doubleValue() 
                : Double.parseDouble(amount.toString());
            return String.format("%.2f €", value);
        } catch (NumberFormatException e) {
            return "-";
        }
    }
    
    /**
     * Formate un nombre pour affichage
     */
    protected String formatNumber(Object number) {
        if (number == null) return "-";
        try {
            if (number instanceof Number) {
                Number n = (Number) number;
                if (n.doubleValue() == n.intValue()) {
                    return String.format("%,d", n.intValue());
                }
                return String.format("%,.2f", n.doubleValue());
            }
            return number.toString();
        } catch (Exception e) {
            return "-";
        }
    }
}
