package com.magscene.magsav.desktop.util;

import java.util.function.Consumer;
import java.util.logging.Logger;

import com.magscene.magsav.desktop.theme.ThemeConstants;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

/**
 * Utilitaires centralisés pour la création d'interfaces utilisateur
 * 
 * Cette classe élimine la duplication de code dans les Views en fournissant
 * des méthodes standardisées pour créer les composants UI communs.
 * 
 * 🔧 PHASE 3: Refactoring et élimination des duplications
 */
public class ViewUtils {

    private static final Logger logger = Logger.getLogger(ViewUtils.class.getName());

    // ========================================
    // 🔍 CRÉATION DE COMPOSANTS DE RECHERCHE; //
    // ========================================

    /**
     * Crée un label de recherche standardisé avec icône
     * 
     * @param text Le texte du label (par défaut: "🔍 Recherche")
     * @return Label stylé selon les standards MAGSAV
     */
    public static Label createSearchLabel(String text) {
        if (text == null || text.trim().isEmpty()) {
            text = "🔍 Recherche";
        }

        Label label = new Label(text);
        label.setStyle(ThemeConstants.HEADER_LABEL_STYLE);
        label.setFont(Font.font(ThemeConstants.FONT_FAMILY, ThemeConstants.FONT_WEIGHT_TITLE,
                ThemeConstants.FONT_SIZE_NORMAL));

        logger.fine("🔍 Label de recherche créé : " + text);
        return label;
    }

    /**
     * Crée un champ de recherche standardisé avec style responsive
     * 
     * @param promptText    Le texte indicatif (placeholder)
     * @param onTextChanged Action à exécuter lors des changements de texte
     * @return TextField configuré et stylé
     */
    public static TextField createSearchField(String promptText, Consumer<String> onTextChanged) {
        TextField searchField = new TextField();
        searchField.setPromptText(promptText != null ? promptText : "Rechercher...");

        // Application des styles unifiés
        ResponsiveUtils.makeFieldResponsive(searchField);

        // Gestion des événements de recherche
        if (onTextChanged != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> onTextChanged.accept(newText));
        }

        // Force les couleurs selon la charte MAGSAV
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);

        logger.fine("🔍 Champ de recherche créé avec prompt : " + promptText);
        return searchField;
    }

    /**
     * Crée un conteneur de recherche complet (label + champ)
     * 
     * @param labelText     Texte du label
     * @param promptText    Placeholder du champ
     * @param onTextChanged Action sur changement de texte
     * @return VBox contenant le label et le champ
     */
    public static VBox createSearchBox(String labelText, String promptText, Consumer<String> onTextChanged) {
        VBox searchBox = new VBox(ThemeConstants.SPACING_XS);

        Label searchLabel = createSearchLabel(labelText);
        TextField searchField = createSearchField(promptText, onTextChanged);

        searchBox.getChildren().addAll(searchLabel, searchField);

        logger.fine("🔍 Conteneur de recherche créé : " + labelText);
        return searchBox;
    }

    // ========================================
    // 🎛️ CRÉATION DE FILTRES STANDARDISÉS; //
    // ========================================

    /**
     * Crée un ComboBox de filtre standardisé
     * 
     * @param labelText          Texte du label du filtre
     * @param items              Éléments du ComboBox
     * @param defaultValue       Valeur par défaut (généralement "Tous")
     * @param onSelectionChanged Action lors du changement de sélection
     * @return VBox contenant le label et le ComboBox
     */
    public static VBox createFilterBox(String labelText, String[] items, String defaultValue,
            Consumer<String> onSelectionChanged) {
        VBox filterBox = new VBox(ThemeConstants.SPACING_XS);

        // Création du label
        Label filterLabel = new Label(labelText);
        filterLabel.setStyle(ThemeConstants.SECONDARY_LABEL_STYLE);
        filterLabel.setFont(Font.font(ThemeConstants.FONT_FAMILY, ThemeConstants.FONT_WEIGHT_TITLE,
                ThemeConstants.FONT_SIZE_NORMAL));

        // Création du ComboBox
        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll(items);
        filterCombo.setValue(defaultValue != null ? defaultValue : "Tous");
        filterCombo.setStyle(ThemeConstants.INPUT_FIELD_STYLE);
        ResponsiveUtils.makeComboResponsive(filterCombo);

        // Gestion des événements
        if (onSelectionChanged != null) {
            filterCombo.setOnAction(e -> onSelectionChanged.accept(filterCombo.getValue()));
        }

        filterBox.getChildren().addAll(filterLabel, filterCombo);

        logger.fine("🎛️ Filtre créé : " + labelText + " avec " + items.length + " éléments");
        return filterBox;
    }

    // ========================================
    // 🔘 CRÉATION DE BOUTONS STANDARDISÉS; //
    // ========================================

    /**
     * Crée un bouton d'action primaire (Ajouter)
     * 
     * @param text     Texte du bouton (par défaut: "➕ Ajouter")
     * @param onAction Action à exécuter
     * @return Button stylé en vert
     */
    public static Button createAddButton(String text, Runnable onAction) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "➕ Ajouter";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.PRIMARY_BUTTON_STYLE);

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        logger.fine("🔘 Bouton d'ajout créé : " + buttonText);
        return button;
    }

    /**
     * Crée un bouton d'édition (Modifier)
     * 
     * @param text           Texte du bouton (par défaut: "✏️ Modifier")
     * @param onAction       Action à exécuter
     * @param disableBinding Propriété pour désactiver le bouton
     * @return Button stylé en bleu
     */
    public static Button createEditButton(String text, Runnable onAction,
            javafx.beans.binding.BooleanBinding disableBinding) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "✏️ Modifier";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.SECONDARY_BUTTON_STYLE);

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        if (disableBinding != null) {
            button.disableProperty().bind(disableBinding);
        }

        logger.fine("🔘 Bouton d'édition créé : " + buttonText);
        return button;
    }

    /**
     * Crée un bouton de suppression (Supprimer)
     * 
     * @param text           Texte du bouton (par défaut: "🗑️ Supprimer")
     * @param onAction       Action à exécuter
     * @param disableBinding Propriété pour désactiver le bouton
     * @return Button stylé en rouge
     */
    public static Button createDeleteButton(String text, Runnable onAction,
            javafx.beans.binding.BooleanBinding disableBinding) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "🗑️ Supprimer";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.DANGER_BUTTON_STYLE);

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        if (disableBinding != null) {
            button.disableProperty().bind(disableBinding);
        }

        logger.fine("🔘 Bouton de suppression créé : " + buttonText);
        return button;
    }

    /**
     * Crée un bouton de rafraîchissement (Actualiser)
     * 
     * @param text     Texte du bouton (par défaut: "🔄 Actualiser")
     * @param onAction Action à exécuter
     * @return Button stylé en violet
     */
    public static Button createRefreshButton(String text, Runnable onAction) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "🔄 Actualiser";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.SPECIAL_BUTTON_STYLE);

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        logger.fine("🔘 Bouton de rafraîchissement créé : " + buttonText);
        return button;
    }

    /**
     * Crée un bouton de détails/visualisation
     * 
     * @param text           Texte du bouton (par défaut: "👁️ Détails")
     * @param onAction       Action à exécuter
     * @param disableBinding Propriété pour désactiver le bouton
     * @return Button stylé en cyan
     */
    public static Button createDetailsButton(String text, Runnable onAction,
            javafx.beans.binding.BooleanBinding disableBinding) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "👁️ Détails";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.DETAIL_BUTTON_STYLE);

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        if (disableBinding != null) {
            button.disableProperty().bind(disableBinding);
        }

        logger.fine("🔘 Bouton de détails créé : " + buttonText);
        return button;
    }

    // ========================================
    // 📋 CRÉATION DE TOOLBARS STANDARDISÉES; //
    // ========================================

    /**
     * Crée une toolbar horizontale standardisée
     * 
     * @param components Composants à ajouter à la toolbar
     * @return HBox stylée selon les standards MAGSAV
     */
    public static HBox createStandardToolbar(Region... components) {
        HBox toolbar = new HBox(ThemeConstants.SPACING_LG);
        toolbar.setPadding(ThemeConstants.TOOLBAR_PADDING);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: "
                + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionColor() +
                "; -fx-background-radius: 8;");

        // Ajout d'un spacer flexible à la fin
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(components);
        toolbar.getChildren().add(spacer);

        logger.fine("📋 Toolbar standardisée créée avec " + components.length + " composants");
        return toolbar;
    }

    /**
     * Crée un conteneur d'actions avec label
     * 
     * @param labelText Texte du label d'actions (par défaut: "⚡ Actions")
     * @param buttons   Boutons à ajouter
     * @return VBox contenant le label et les boutons
     */
    public static VBox createActionsBox(String labelText, Button... buttons) {
        String actionText = (labelText != null && !labelText.trim().isEmpty()) ? labelText : "⚡ Actions";

        VBox actionsBox = new VBox(ThemeConstants.SPACING_XS);
        Label actionsLabel = new Label(actionText);
        actionsLabel.setStyle(ThemeConstants.HEADER_LABEL_STYLE);
        actionsLabel.setFont(Font.font(ThemeConstants.FONT_FAMILY, ThemeConstants.FONT_WEIGHT_TITLE,
                ThemeConstants.FONT_SIZE_NORMAL));

        HBox buttonRow = new HBox(ThemeConstants.SPACING_MD);
        buttonRow.getChildren().addAll(buttons);

        actionsBox.getChildren().addAll(actionsLabel, buttonRow);

        logger.fine("⚡ Conteneur d'actions créé avec " + buttons.length + " boutons");
        return actionsBox;
    }

    // ========================================
    // 📊 CRÉATION DE CONTENEURS DE STATISTIQUES; //
    // ========================================

    /**
     * Crée un label de statistiques standardisé
     * 
     * @param initialText Texte initial (par défaut: "📊 Chargement des
     *                    statistiques...")
     * @return Label stylé pour afficher les stats
     */
    public static Label createStatsLabel(String initialText) {
        String statsText = (initialText != null && !initialText.trim().isEmpty()) ? initialText
                : "📊 Chargement des statistiques...";

        Label statsLabel = new Label(statsText);
        statsLabel.setFont(Font.font(ThemeConstants.FONT_FAMILY, ThemeConstants.FONT_WEIGHT_TITLE,
                ThemeConstants.FONT_SIZE_NORMAL));
        statsLabel.setStyle("-fx-text-fill: " + ThemeConstants.TEXT_SECONDARY + ";");

        logger.fine("📊 Label de statistiques créé");
        return statsLabel;
    }

    /**
     * Crée un footer standardisé pour afficher les statistiques
     * 
     * @param statsLabel Label des statistiques à inclure
     * @return HBox configurée comme footer
     */
    public static HBox createStandardFooter(Label statsLabel) {
        HBox footer = new HBox();
        footer.setPadding(ThemeConstants.PADDING_STANDARD);
        footer.setAlignment(Pos.CENTER_LEFT);

        if (statsLabel != null) {
            footer.getChildren().add(statsLabel);
        }

        logger.fine("📊 Footer standardisé créé");
        return footer;
    }

    // ========================================
    // 🔧 MÉTHODES UTILITAIRES DIVERSES; // ========================================

    /**
     * Met à jour le texte d'un label de statistiques avec formatage
     * 
     * @param statsLabel Le label à mettre à jour
     * @param totalItems Nombre total d'éléments
     * @param itemType   Type d'élément (ex: "équipements", "demandes")
     */
    public static void updateStatsLabel(Label statsLabel, int totalItems, String itemType) {
        if (statsLabel == null)
            return;

        String statsText = String.format("📊 %d %s", totalItems, itemType != null ? itemType : "éléments");
        statsLabel.setText(statsText);

        logger.fine("📊 Stats mises à jour : " + statsText);
    }

    /**
     * Applique un style de sélection à une ligne de tableau
     * 
     * @param row        La ligne de tableau
     * @param isSelected Si la ligne est sélectionnée
     */
    public static void applySelectionStyle(TableRow<?> row, boolean isSelected) {
        if (row == null)
            return;

        if (row.isEmpty()) {
            row.setStyle("");
        } else if (isSelected) {
            row.setStyle("-fx-background-color: "
                    + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionColor() + "; " +
                    "-fx-text-fill: "
                    + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionTextColor() + "; " +
                    "-fx-border-color: "
                    + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                    "-fx-border-width: 2px;");
        } else {
            row.setStyle("");
        }
    }

    // ========================================
    // 💬 CRÉATION DE BOUTONS DE DIALOGUE; //
    // ========================================

    /**
     * Crée un bouton OK/Valider pour les dialogues
     * 
     * @param text     Texte du bouton (par défaut: "✅ Valider")
     * @param onAction Action à exécuter
     * @return Button stylé en vert (succès)
     */
    public static Button createOKButton(String text, Runnable onAction) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "✅ Valider";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.PRIMARY_BUTTON_STYLE);
        button.setDefaultButton(true); // Bouton par défaut (Enter)

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        logger.fine("💬 Bouton OK créé : " + buttonText);
        return button;
    }

    /**
     * Crée un bouton Annuler pour les dialogues
     * 
     * @param text     Texte du bouton (par défaut: "❌ Annuler")
     * @param onAction Action à exécuter
     * @return Button stylé en rouge (danger)
     */
    public static Button createCancelButton(String text, Runnable onAction) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "❌ Annuler";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.DANGER_BUTTON_STYLE);
        button.setCancelButton(true); // Bouton d'annulation (Escape)

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        logger.fine("💬 Bouton Annuler créé : " + buttonText);
        return button;
    }

    /**
     * Crée un bouton Appliquer pour les dialogues
     * 
     * @param text     Texte du bouton (par défaut: "💾 Appliquer")
     * @param onAction Action à exécuter
     * @return Button stylé en bleu (secondaire)
     */
    public static Button createApplyButton(String text, Runnable onAction) {
        String buttonText = (text != null && !text.trim().isEmpty()) ? text : "💾 Appliquer";
        Button button = new Button(buttonText);
        button.setStyle(ThemeConstants.SECONDARY_BUTTON_STYLE);

        if (onAction != null) {
            button.setOnAction(e -> onAction.run());
        }

        logger.fine("💬 Bouton Appliquer créé : " + buttonText);
        return button;
    }

    /**
     * Crée une barre de boutons standard pour dialogues
     * 
     * @param okAction     Action du bouton OK
     * @param cancelAction Action du bouton Annuler
     * @param applyAction  Action du bouton Appliquer (optionnel, null pour
     *                     l'omettre)
     * @return HBox contenant les boutons stylés et alignés
     */
    public static HBox createDialogButtonBar(Runnable okAction, Runnable cancelAction, Runnable applyAction) {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(15, 0, 0, 0));

        Button cancelBtn = createCancelButton(null, cancelAction);
        Button okBtn = createOKButton(null, okAction);

        if (applyAction != null) {
            Button applyBtn = createApplyButton(null, applyAction);
            buttonBar.getChildren().addAll(cancelBtn, applyBtn, okBtn);
        } else {
            buttonBar.getChildren().addAll(cancelBtn, okBtn);
        }

        logger.fine("💬 Barre de boutons de dialogue créée");
        return buttonBar;
    }

    // ========================================
    // 📋 CRÉATION DE SECTIONS D'INFORMATION
    // ========================================

    /**
     * Crée une section d'informations avec un titre et des lignes de contenu
     * 
     * @param title Titre de la section (avec émoji recommandé)
     * @param lines Lignes de contenu à afficher
     * @return VBox contenant la section formatée
     */
    public static VBox createInfoSection(String title, String... lines) {
        VBox section = new VBox(3);
        section.setPadding(new Insets(5));
        section.getStyleClass().add("info-section");

        // Titre de la section
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", javafx.scene.text.FontWeight.BOLD, 12));
        titleLabel.getStyleClass().add("info-section-title");
        section.getChildren().add(titleLabel);

        // Lignes de contenu
        for (String line : lines) {
            Label contentLabel = new Label(line);
            contentLabel.getStyleClass().add("info-section-content");
            section.getChildren().add(contentLabel);
        }

        logger.fine("📋 Section d'information créée : " + title + " (" + lines.length + " lignes)");
        return section;
    }

    /**
     * Crée un label de statut coloré selon le type
     * 
     * @param text Texte à afficher
     * @param type Type de statut pour le styling
     * @return Label stylé
     */
    public static Label createStatusLabel(String text, StatusType type) {
        Label label = new Label(text);
        label.getStyleClass().addAll("status-label", "status-" + type.name().toLowerCase());

        logger.fine("🏷️ Label de statut créé : " + text + " (" + type + ")");
        return label;
    }

    /**
     * Types de statut pour le styling des labels
     */
    public enum StatusType {
        SUCCESS, WARNING, ERROR, INFO, PENDING
    }
}