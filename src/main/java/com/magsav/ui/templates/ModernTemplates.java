package com.magsav.ui.templates;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import com.magsav.ui.components.ModernComponents;
import com.magsav.ui.icons.IconService;
import com.magsav.ui.animation.AnimationUtils;

/**
 * Templates modernes pour les interfaces MAGSAV
 * Remplace progressivement les anciens fichiers FXML
 */
public class ModernTemplates {
    
    private static final IconService iconService = IconService.getInstance();
    
    /**
     * Template moderne pour la page principale
     */
    public static BorderPane createMainPageTemplate() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-layout");
        
        // En-tête moderne
        HBox header = createModernHeader();
        mainLayout.setTop(header);
        
        // Sidebar de navigation
        VBox sidebar = createModernSidebar();
        mainLayout.setLeft(sidebar);
        
        // Zone de contenu principal
        StackPane contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        contentArea.setPadding(new Insets(24));
        mainLayout.setCenter(contentArea);
        
        // Barre de statut
        HBox statusBar = createModernStatusBar();
        mainLayout.setBottom(statusBar);
        
        return mainLayout;
    }
    
    /**
     * Template pour les formulaires modernes
     */
    public static VBox createFormTemplate(String title, Node... formFields) {
        VBox formContainer = ModernComponents.createCard(title, null);
        formContainer.getStyleClass().add("form-container");
        formContainer.setMaxWidth(600);
        
        // Titre du formulaire
        if (title != null && !title.isEmpty()) {
            Label titleLabel = ModernComponents.createTitle(title);
            titleLabel.setPadding(new Insets(0, 0, 24, 0));
            formContainer.getChildren().add(titleLabel);
        }
        
        // Champs du formulaire avec espacement
        VBox fieldsContainer = new VBox(16);
        fieldsContainer.getChildren().addAll(formFields);
        formContainer.getChildren().add(fieldsContainer);
        
        // Boutons d'action
        HBox buttonContainer = createFormButtons();
        buttonContainer.setPadding(new Insets(24, 0, 0, 0));
        formContainer.getChildren().add(buttonContainer);
        
        return formContainer;
    }
    
    /**
     * Template pour les listes avec recherche
     */
    public static VBox createListTemplate(String title, TableView<?> table) {
        VBox listContainer = new VBox(16);
        listContainer.setPadding(new Insets(24));
        
        // En-tête avec titre et recherche
        HBox headerContainer = new HBox(16);
        headerContainer.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = ModernComponents.createTitle(title);
        headerContainer.getChildren().add(titleLabel);
        
        // Spacer pour pousser les contrôles à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerContainer.getChildren().add(spacer);
        
        // Champ de recherche avec conteneur pour l'icône
        HBox searchContainer = new HBox(8);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = ModernComponents.createTextField("Rechercher...");
        searchField.setPrefWidth(250);
        Node searchIcon = iconService.createMaterialIcon("search", IconService.Size.SMALL);
        
        searchContainer.getChildren().addAll(searchIcon, searchField);
        headerContainer.getChildren().add(searchContainer);
        
        // Bouton d'ajout
        Button addButton = ModernComponents.createButtonWithIcon("Ajouter", "add", 
                                                                ModernComponents.ButtonStyle.PRIMARY);
        headerContainer.getChildren().add(addButton);
        
        listContainer.getChildren().add(headerContainer);
        
        // Table avec style moderne
        table.getStyleClass().add("modern-table-view");
        VBox.setVgrow(table, Priority.ALWAYS);
        listContainer.getChildren().add(table);
        
        return listContainer;
    }
    
    /**
     * Template pour les dialogues modernes
     */
    public static VBox createDialogTemplate(String title, String message, 
                                          ModernComponents.AlertStyle alertStyle) {
        VBox dialogContainer = new VBox(16);
        dialogContainer.setPadding(new Insets(24));
        dialogContainer.getStyleClass().add("modern-dialog");
        dialogContainer.setMaxWidth(400);
        
        // Titre avec icône
        HBox titleContainer = new HBox(12);
        titleContainer.setAlignment(Pos.CENTER_LEFT);
        
        Node icon = getIconForAlertStyle(alertStyle);
        if (icon != null) {
            titleContainer.getChildren().add(icon);
        }
        
        Label titleLabel = ModernComponents.createSubtitle(title);
        titleContainer.getChildren().add(titleLabel);
        dialogContainer.getChildren().add(titleContainer);
        
        // Message
        if (message != null && !message.isEmpty()) {
            Label messageLabel = ModernComponents.createLabel(message, 
                                                            ModernComponents.LabelStyle.NORMAL);
            messageLabel.setWrapText(true);
            dialogContainer.getChildren().add(messageLabel);
        }
        
        // Boutons d'action
        HBox buttonContainer = createDialogButtons();
        buttonContainer.setPadding(new Insets(16, 0, 0, 0));
        dialogContainer.getChildren().add(buttonContainer);
        
        return dialogContainer;
    }
    
    /**
     * Template pour les cartes de dashboard
     */
    public static VBox createDashboardCard(String title, String value, String subtitle, String iconName) {
        VBox card = ModernComponents.createCard(null, null);
        card.setPrefWidth(280);
        card.getStyleClass().add("dashboard-card");
        
        // En-tête avec icône
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Node icon = iconService.createMaterialIcon(iconName, IconService.Size.MEDIUM);
        icon.getStyleClass().add("dashboard-icon");
        header.getChildren().add(icon);
        
        Label titleLabel = ModernComponents.createLabel(title, ModernComponents.LabelStyle.SMALL);
        titleLabel.getStyleClass().add("dashboard-title");
        header.getChildren().add(titleLabel);
        
        card.getChildren().add(header);
        
        // Valeur principale
        Label valueLabel = ModernComponents.createTitle(value);
        valueLabel.getStyleClass().add("dashboard-value");
        card.getChildren().add(valueLabel);
        
        // Sous-titre
        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = ModernComponents.createLabel(subtitle, 
                                                             ModernComponents.LabelStyle.SECONDARY);
            subtitleLabel.getStyleClass().add("dashboard-subtitle");
            card.getChildren().add(subtitleLabel);
        }
        
        return card;
    }
    
    /**
     * Crée un en-tête moderne
     */
    private static HBox createModernHeader() {
        HBox header = new HBox(16);
        header.getStyleClass().add("modern-header");
        header.setPadding(new Insets(16, 24, 16, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        
        // Logo/titre de l'application
        Label appTitle = ModernComponents.createTitle("MAGSAV");
        appTitle.getStyleClass().add("app-title");
        header.getChildren().add(appTitle);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);
        
        // Contrôles d'utilisateur
        Button themeToggle = ModernComponents.createButtonWithIcon("", "palette", 
                                                                  ModernComponents.ButtonStyle.OUTLINE);
        Button userProfile = ModernComponents.createButtonWithIcon("", "account_circle", 
                                                                  ModernComponents.ButtonStyle.OUTLINE);
        
        header.getChildren().addAll(themeToggle, userProfile);
        
        return header;
    }
    
    /**
     * Crée une sidebar moderne
     */
    private static VBox createModernSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("modern-sidebar");
        sidebar.setPadding(new Insets(24, 16, 24, 16));
        sidebar.setPrefWidth(250);
        
        // Menu items
        String[] menuItems = {"Dashboard", "Clients", "Produits", "Demandes", "Interventions", "Sociétés", "Utilisateurs"};
        String[] menuIcons = {"dashboard", "people", "inventory", "assignment", "build", "business", "group"};
        
        for (int i = 0; i < menuItems.length; i++) {
            Button menuButton = ModernComponents.createButtonWithIcon(menuItems[i], menuIcons[i], 
                                                                    ModernComponents.ButtonStyle.OUTLINE);
            menuButton.getStyleClass().add("menu-item");
            menuButton.setMaxWidth(Double.MAX_VALUE);
            AnimationUtils.makeButtonInteractive(menuButton);
            sidebar.getChildren().add(menuButton);
        }
        
        return sidebar;
    }
    
    /**
     * Crée une barre de statut moderne
     */
    private static HBox createModernStatusBar() {
        HBox statusBar = new HBox(16);
        statusBar.getStyleClass().add("modern-status-bar");
        statusBar.setPadding(new Insets(8, 24, 8, 24));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        Label statusLabel = ModernComponents.createLabel("Prêt", ModernComponents.LabelStyle.SMALL);
        statusBar.getChildren().add(statusLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        statusBar.getChildren().add(spacer);
        
        // Informations système
        Label versionLabel = ModernComponents.createLabel("Version 1.2", ModernComponents.LabelStyle.SMALL);
        statusBar.getChildren().add(versionLabel);
        
        return statusBar;
    }
    
    /**
     * Crée les boutons de formulaire
     */
    private static HBox createFormButtons() {
        HBox buttonContainer = new HBox(12);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = ModernComponents.createButton("Annuler", ModernComponents.ButtonStyle.OUTLINE);
        Button saveButton = ModernComponents.createButton("Sauvegarder", ModernComponents.ButtonStyle.SUCCESS);
        
        buttonContainer.getChildren().addAll(cancelButton, saveButton);
        
        return buttonContainer;
    }
    
    /**
     * Crée les boutons de dialogue
     */
    private static HBox createDialogButtons() {
        HBox buttonContainer = new HBox(12);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = ModernComponents.createButton("Annuler", ModernComponents.ButtonStyle.OUTLINE);
        Button confirmButton = ModernComponents.createButton("Confirmer", ModernComponents.ButtonStyle.PRIMARY);
        
        buttonContainer.getChildren().addAll(cancelButton, confirmButton);
        
        return buttonContainer;
    }
    
    /**
     * Retourne l'icône appropriée selon le style d'alerte
     */
    private static Node getIconForAlertStyle(ModernComponents.AlertStyle style) {
        String iconName;
        switch (style) {
            case INFO:
                iconName = "info";
                break;
            case SUCCESS:
                iconName = "check_circle";
                break;
            case WARNING:
                iconName = "warning";
                break;
            case ERROR:
                iconName = "error";
                break;
            default:
                return null;
        }
        return iconService.createMaterialIcon(iconName, IconService.Size.MEDIUM);
    }
}