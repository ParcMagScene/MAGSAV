package com.magsav.ui.components;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.magsav.ui.animation.AnimationUtils;
import com.magsav.ui.icons.IconService;

/**
 * Factory pour créer des composants modernes avec styles appliqués
 */
public class ModernComponents {
    
    private static final IconService iconService = IconService.getInstance();
    
    /**
     * Crée un bouton moderne
     */
    public static Button createButton(String text, ButtonStyle style) {
        Button button = new Button(text);
        applyButtonStyle(button, style);
        AnimationUtils.makeButtonInteractive(button);
        return button;
    }
    
    /**
     * Crée un bouton moderne avec icône
     */
    public static Button createButton(String text, Node icon, ButtonStyle style) {
        Button button = new Button(text, icon);
        applyButtonStyle(button, style);
        AnimationUtils.makeButtonInteractive(button);
        return button;
    }
    
    /**
     * Crée un bouton avec icône Material Design
     */
    public static Button createButtonWithIcon(String text, String iconName, ButtonStyle style) {
        Node icon = iconService.createMaterialIcon(iconName, IconService.Size.SMALL);
        return createButton(text, icon, style);
    }
    
    /**
     * Crée un champ de texte moderne
     */
    public static TextField createTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("modern-text-field");
        return textField;
    }
    
    /**
     * Crée une zone de texte moderne
     */
    public static TextArea createTextArea(String prompt) {
        TextArea textArea = new TextArea();
        textArea.setPromptText(prompt);
        textArea.getStyleClass().add("modern-text-area");
        return textArea;
    }
    
    /**
     * Crée une carte moderne
     */
    public static VBox createCard(String title, Node content) {
        VBox card = new VBox(16);
        card.getStyleClass().addAll("modern-card");
        
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("modern-subtitle");
            card.getChildren().add(titleLabel);
        }
        
        if (content != null) {
            card.getChildren().add(content);
        }
        
        AnimationUtils.makeCardInteractive(card);
        return card;
    }
    
    /**
     * Crée une carte compacte
     */
    public static VBox createCompactCard(Node content) {
        VBox card = new VBox();
        card.getStyleClass().addAll("modern-card", "compact");
        
        if (content != null) {
            card.getChildren().add(content);
        }
        
        AnimationUtils.makeCardInteractive(card);
        return card;
    }
    
    /**
     * Crée un tableau moderne
     */
    public static <T> TableView<T> createTable() {
        TableView<T> table = new TableView<>();
        table.getStyleClass().add("modern-table-view");
        return table;
    }
    
    /**
     * Crée une liste moderne
     */
    public static <T> ListView<T> createList() {
        ListView<T> list = new ListView<>();
        list.getStyleClass().add("modern-list-view");
        return list;
    }
    
    /**
     * Crée un panneau d'onglets moderne
     */
    public static TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("modern-tab-pane");
        return tabPane;
    }
    
    /**
     * Crée une barre de progression moderne
     */
    public static ProgressBar createProgressBar() {
        ProgressBar progressBar = new ProgressBar();
        progressBar.getStyleClass().add("modern-progress-bar");
        return progressBar;
    }
    
    /**
     * Crée un indicateur de progression moderne
     */
    public static ProgressIndicator createProgressIndicator() {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.getStyleClass().add("modern-progress-indicator");
        return indicator;
    }
    
    /**
     * Crée une case à cocher moderne
     */
    public static CheckBox createCheckBox(String text) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.getStyleClass().add("modern-checkbox");
        return checkBox;
    }
    
    /**
     * Crée un bouton radio moderne
     */
    public static RadioButton createRadioButton(String text) {
        RadioButton radioButton = new RadioButton(text);
        radioButton.getStyleClass().add("modern-radio-button");
        return radioButton;
    }
    
    /**
     * Crée un label moderne
     */
    public static Label createLabel(String text, LabelStyle style) {
        Label label = new Label(text);
        applyLabelStyle(label, style);
        return label;
    }
    
    /**
     * Crée un titre moderne
     */
    public static Label createTitle(String text) {
        return createLabel(text, LabelStyle.TITLE);
    }
    
    /**
     * Crée un sous-titre moderne
     */
    public static Label createSubtitle(String text) {
        return createLabel(text, LabelStyle.SUBTITLE);
    }
    
    /**
     * Crée une alerte moderne
     */
    public static VBox createAlert(String message, AlertStyle style) {
        VBox alert = new VBox(8);
        alert.getStyleClass().addAll("modern-alert", style.getStyleClass());
        
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("modern-label");
        alert.getChildren().add(messageLabel);
        
        return alert;
    }
    
    /**
     * Applique le style à un bouton
     */
    public static void applyButtonStyle(Button button, ButtonStyle style) {
        button.getStyleClass().clear();
        button.getStyleClass().addAll("modern-button", style.getStyleClass());
    }
    
    /**
     * Applique le style à un label
     */
    public static void applyLabelStyle(Label label, LabelStyle style) {
        label.getStyleClass().clear();
        label.getStyleClass().addAll("modern-label", style.getStyleClass());
    }
    
    /**
     * Enum pour les styles de boutons
     */
    public enum ButtonStyle {
        PRIMARY(""),
        SECONDARY("secondary"),
        OUTLINE("outline"),
        DANGER("danger"),
        SUCCESS("success"),
        SMALL("small"),
        LARGE("large");
        
        private final String styleClass;
        
        ButtonStyle(String styleClass) {
            this.styleClass = styleClass;
        }
        
        public String getStyleClass() {
            return styleClass;
        }
    }
    
    /**
     * Enum pour les styles de labels
     */
    public enum LabelStyle {
        NORMAL(""),
        TITLE("modern-title"),
        SUBTITLE("modern-subtitle"),
        SECONDARY("secondary"),
        SMALL("small");
        
        private final String styleClass;
        
        LabelStyle(String styleClass) {
            this.styleClass = styleClass;
        }
        
        public String getStyleClass() {
            return styleClass;
        }
    }
    
    /**
     * Enum pour les styles d'alertes
     */
    public enum AlertStyle {
        INFO("info"),
        SUCCESS("success"),
        WARNING("warning"),
        ERROR("error");
        
        private final String styleClass;
        
        AlertStyle(String styleClass) {
            this.styleClass = styleClass;
        }
        
        public String getStyleClass() {
            return styleClass;
        }
    }
}