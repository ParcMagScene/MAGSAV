package com.magscene.magsav.desktop.util;

import com.magscene.magsav.desktop.theme.ThemeManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.function.Consumer;

/**
 * Utilitaires pour créer des toolbars standardisées selon le design MAGSAV-3.0
 * Standard basé sur ClientManagerView avec cohérence visuelle dans toute
 * l'application
 */
public class ToolbarUtils {

    // ========================================
    // 🎨 CONSTANTES DE STYLE STANDARDISÉES; //
    // ========================================

    private static String getToolbarStyle() {
        return "-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
                "-fx-background-radius: 8;";
    }

    private static final String SEARCH_LABEL_STYLE = "-fx-text-fill: #6B71F2;";
    private static final String FILTER_LABEL_STYLE = "-fx-text-fill: #8B91FF;";
    private static final String ACTION_LABEL_STYLE = "-fx-text-fill: #6B71F2;";

    private static String getComboStyle() {
        return "-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
                "-fx-text-fill: #6B71F2; " +
                "-fx-prompt-text-fill: #6B71F2;";
    }

    // Styles des boutons d'action standardisés
    private static final String BUTTON_BASE_STYLE = "-fx-text-fill: white; " +
            "-fx-background-radius: 4; " +
            "-fx-padding: 8 16;";

    public static final String BUTTON_ADD_STYLE = "-fx-background-color: #27ae60; " + BUTTON_BASE_STYLE;
    public static final String BUTTON_EDIT_STYLE = "-fx-background-color: #3498db; " + BUTTON_BASE_STYLE;
    public static final String BUTTON_VIEW_STYLE = "-fx-background-color: #f39c12; " + BUTTON_BASE_STYLE;
    public static final String BUTTON_DELETE_STYLE = "-fx-background-color: #e74c3c; " + BUTTON_BASE_STYLE;
    public static final String BUTTON_REFRESH_STYLE = "-fx-background-color: #9b59b6; " + BUTTON_BASE_STYLE;
    public static final String BUTTON_EXPORT_STYLE = "-fx-background-color: #8e44ad; " + BUTTON_BASE_STYLE;
    public static final String BUTTON_SECONDARY_STYLE = "-fx-background-color: #34495e; " + BUTTON_BASE_STYLE;

    // ========================================
    // 🏗️ MÉTHODES DE CRÉATION STANDARDISÉES; //
    // ========================================

    /**
     * Crée une toolbar standardisée selon le design MAGSAV-3.0
     * 
     * @return HBox configuré avec le style standard
     */
    public static HBox createStandardToolbar() {
        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle(getToolbarStyle());
        return toolbar;
    }

    /**
     * Crée une section de recherche standardisée
     * 
     * @param labelText     Texte du label (ex: "🔍 Recherche")
     * @param promptText    Placeholder du champ de recherche
     * @param width         Largeur du champ (recommandé: 280px)
     * @param onTextChanged Action sur changement de texte
     * @return VBox contenant le label et le champ de recherche stylé
     */
    public static VBox createSearchSection(String labelText, String promptText, double width,
            Consumer<String> onTextChanged) {
        VBox searchBox = new VBox(5);

        Label searchLabel = new Label(labelText);
        searchLabel.setStyle(SEARCH_LABEL_STYLE);
        searchLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextField searchField = new TextField();
        searchField.setPromptText(promptText);
        searchField.setPrefWidth(width);
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);

        if (onTextChanged != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> onTextChanged.accept(newText));
        }

        searchBox.getChildren().addAll(searchLabel, searchField);
        return searchBox;
    }

    /**
     * Crée une section de filtre standardisée avec ComboBox
     * 
     * @param labelText          Texte du label (ex: "📊 Statut")
     * @param items              Éléments du ComboBox
     * @param defaultValue       Valeur par défaut
     * @param width              Largeur du ComboBox (recommandé: 120-180px)
     * @param onSelectionChanged Action sur changement de sélection
     * @return VBox contenant le label et le ComboBox stylé
     */
    public static VBox createFilterSection(String labelText, String[] items, String defaultValue, double width,
            Consumer<String> onSelectionChanged) {
        VBox filterBox = new VBox(5);

        Label filterLabel = new Label(labelText);
        filterLabel.setStyle(FILTER_LABEL_STYLE);
        filterLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll(items);
        filterCombo.setValue(defaultValue);
        filterCombo.setPrefWidth(width);
        filterCombo.setStyle(getComboStyle());

        if (onSelectionChanged != null) {
            filterCombo.setOnAction(e -> onSelectionChanged.accept(filterCombo.getValue()));
        }

        filterBox.getChildren().addAll(filterLabel, filterCombo);
        return filterBox;
    }

    /**
     * Crée une section d'actions standardisée avec boutons
     * 
     * @param labelText Texte du label (ex: "⚡ Actions")
     * @param buttons   Tableau de boutons à ajouter
     * @return VBox contenant le label et les boutons
     */
    public static VBox createActionsSection(String labelText, Button... buttons) {
        VBox actionsBox = new VBox(5);

        Label actionsLabel = new Label(labelText);
        actionsLabel.setStyle(ACTION_LABEL_STYLE);
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(buttons);

        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        return actionsBox;
    }

    /**
     * Crée un bouton d'action standardisé
     * 
     * @param text   Texte du bouton
     * @param style  Style du bouton (utiliser les constantes BUTTON_*_STYLE)
     * @param action Action à exécuter au clic
     * @return Button configuré et stylé
     */
    public static Button createActionButton(String text, String style, Runnable action) {
        Button button = new Button(text);
        button.setStyle(style);

        if (action != null) {
            button.setOnAction(e -> action.run());
        }

        return button;
    }

    /**
     * Crée un spacer standard pour pousser les éléments à droite
     * 
     * @return Region configuré comme spacer
     */
    public static Region createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    /**
     * Assemble une toolbar complète avec recherche, filtres et actions
     * 
     * @param searchSection  Section de recherche (peut être null)
     * @param filterSections Sections de filtres (peuvent être nulles)
     * @param actionsSection Section d'actions (peut être null)
     * @return HBox toolbar complète assemblée
     */
    public static HBox assembleToolbar(VBox searchSection, VBox[] filterSections, VBox actionsSection) {
        HBox toolbar = createStandardToolbar();

        if (searchSection != null) {
            toolbar.getChildren().add(searchSection);
        }

        if (filterSections != null) {
            for (VBox filterSection : filterSections) {
                if (filterSection != null) {
                    toolbar.getChildren().add(filterSection);
                }
            }
        }

        toolbar.getChildren().add(createSpacer());

        if (actionsSection != null) {
            toolbar.getChildren().add(actionsSection);
        }

        return toolbar;
    }
}