package com.magscene.magsav.desktop.theme;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Factory pour créer des composants JavaFX avec styles standardisés
 * 
 * Cette classe centralise la création de composants pré-stylés selon
 * les constantes définies dans ThemeConstants, éliminant les styles inline.
 * 
 * @author MAGSAV Architecture Team
 * @since 3.0
 */
public class StyleFactory {

    // ========================================
    // 🏷️ LABELS
    // ========================================

    /**
     * Crée un label de titre de section
     */
    public static Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setStyle(ThemeConstants.SECTION_TITLE_STYLE);
        return label;
    }

    /**
     * Crée un label de titre principal
     */
    public static Label createLargeTitle(String text) {
        Label label = new Label(text);
        label.setStyle(ThemeConstants.LARGE_TITLE_STYLE);
        return label;
    }

    /**
     * Crée un label d'erreur
     */
    public static Label createErrorLabel(String text) {
        Label label = new Label(text);
        label.setStyle(ThemeConstants.ERROR_MESSAGE_STYLE);
        return label;
    }

    /**
     * Crée un label informatif
     */
    public static Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle(ThemeConstants.INFO_MESSAGE_STYLE);
        return label;
    }

    /**
     * Crée un label avec le style d'en-tête
     */
    public static Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle(ThemeConstants.HEADER_LABEL_STYLE);
        return label;
    }

    /**
     * Crée un label secondaire
     */
    public static Label createSecondaryLabel(String text) {
        Label label = new Label(text);
        label.setStyle(ThemeConstants.SECONDARY_LABEL_STYLE);
        return label;
    }

    // ========================================
    // 🔘 BOUTONS
    // ========================================

    /**
     * Crée un bouton primaire (vert)
     */
    public static Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(ThemeConstants.PRIMARY_BUTTON_STYLE + " " + ThemeConstants.BUTTON_STYLE);
        return button;
    }

    /**
     * Crée un bouton secondaire (bleu)
     */
    public static Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setStyle(ThemeConstants.SECONDARY_BUTTON_STYLE + " " + ThemeConstants.BUTTON_STYLE);
        return button;
    }

    /**
     * Crée un bouton de danger (rouge)
     */
    public static Button createDangerButton(String text) {
        Button button = new Button(text);
        button.setStyle(ThemeConstants.DANGER_BUTTON_STYLE + " " + ThemeConstants.BUTTON_STYLE);
        return button;
    }

    /**
     * Crée un bouton spécial (violet)
     */
    public static Button createSpecialButton(String text) {
        Button button = new Button(text);
        button.setStyle(ThemeConstants.SPECIAL_BUTTON_STYLE + " " + ThemeConstants.BUTTON_STYLE);
        return button;
    }

    /**
     * Crée un bouton de détail (cyan)
     */
    public static Button createDetailButton(String text) {
        Button button = new Button(text);
        button.setStyle(ThemeConstants.DETAIL_BUTTON_STYLE + " " + ThemeConstants.BUTTON_STYLE);
        return button;
    }

    // ========================================
    // 📝 CHAMPS DE SAISIE
    // ========================================

    /**
     * Crée un TextField avec style standardisé
     */
    public static TextField createStyledTextField() {
        TextField textField = new TextField();
        textField.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        return textField;
    }

    /**
     * Crée un TextField avec placeholder et style standardisé
     */
    public static TextField createStyledTextField(String promptText) {
        TextField textField = createStyledTextField();
        textField.setPromptText(promptText);
        return textField;
    }

    /**
     * Crée un TextArea avec style standardisé
     */
    public static TextArea createStyledTextArea() {
        TextArea textArea = new TextArea();
        textArea.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        return textArea;
    }

    // ========================================
    // 📦 CONTENEURS
    // ========================================

    /**
     * Crée un HBox toolbar standardisé
     */
    public static HBox createToolbar() {
        HBox toolbar = new HBox(ThemeConstants.SPACING_MD);
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        toolbar.setPadding(ThemeConstants.TOOLBAR_PADDING);
        toolbar.setStyle(ThemeConstants.TOOLBAR_STYLE);
        toolbar.getStyleClass().add(ThemeConstants.UNIFIED_TOOLBAR_CLASS);
        return toolbar;
    }

    /**
     * Crée un VBox avec padding standard
     */
    public static VBox createStandardVBox() {
        VBox vbox = new VBox(ThemeConstants.SPACING_MD);
        vbox.setPadding(ThemeConstants.PADDING_STANDARD);
        return vbox;
    }

    /**
     * Crée un VBox avec padding spécifique
     */
    public static VBox createVBox(double spacing) {
        VBox vbox = new VBox(spacing);
        return vbox;
    }

    /**
     * Crée un HBox avec padding standard
     */
    public static HBox createStandardHBox() {
        HBox hbox = new HBox(ThemeConstants.SPACING_MD);
        hbox.setPadding(ThemeConstants.PADDING_STANDARD);
        return hbox;
    }

    /**
     * Crée un HBox avec spacing spécifique
     */
    public static HBox createHBox(double spacing) {
        HBox hbox = new HBox(spacing);
        return hbox;
    }

    // ========================================
    // 📊 TABLEAUX
    // ========================================

    /**
     * Applique le style de bordure standard à un TableView
     */
    public static <T> void styleTable(TableView<T> table) {
        table.setStyle(ThemeConstants.TABLE_BORDER_STYLE);
    }

    /**
     * Crée un TableView avec style standardisé
     */
    public static <T> TableView<T> createStyledTable() {
        TableView<T> table = new TableView<>();
        styleTable(table);
        return table;
    }

    // ========================================
    // 🎨 STYLES DYNAMIQUES
    // ========================================

    /**
     * Retourne le style CSS pour un statut donné (texte)
     */
    public static String getStatusStyle(String status) {
        return ThemeConstants.getStatusTextStyle(status);
    }

    /**
     * Retourne le style CSS pour un statut donné (background)
     */
    public static String getStatusBackgroundStyle(String status) {
        return ThemeConstants.getStatusBackgroundStyle(status);
    }

    /**
     * Applique un padding standard à un Region
     */
    public static void applyStandardPadding(Region region) {
        region.setPadding(ThemeConstants.PADDING_STANDARD);
    }

    /**
     * Applique un padding medium à un Region
     */
    public static void applyMediumPadding(Region region) {
        region.setPadding(ThemeConstants.PADDING_MEDIUM);
    }

    /**
     * Applique un padding large à un Region
     */
    public static void applyLargePadding(Region region) {
        region.setPadding(ThemeConstants.PADDING_LARGE);
    }
}
