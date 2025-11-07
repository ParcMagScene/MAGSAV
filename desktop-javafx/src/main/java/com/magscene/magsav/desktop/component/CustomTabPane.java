package com.magscene.magsav.desktop.component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomTabPane - Remplace les TabPane JavaFX par des boutons personnalisés
 * Permet un contrôle total sur le style des onglets et boutons de navigation
 */
public class CustomTabPane extends VBox {
    
    public static class CustomTab {
        private final String text;
        private final Node content;
        private final String iconText;
        
        public CustomTab(String text, Node content) {
            this(text, content, null);
        }
        
        public CustomTab(String text, Node content, String iconText) {
            this.text = text;
            this.content = content;
            this.iconText = iconText;
        }
        
        public String getText() { return text; }
        public Node getContent() { return content; }
        public String getIconText() { return iconText; }
    }
    
    private final List<CustomTab> tabs = new ArrayList<>();
    private final List<Button> tabButtons = new ArrayList<>();
    private final HBox tabHeader;
    private final VBox contentArea;
    private final ObjectProperty<CustomTab> selectedTab = new SimpleObjectProperty<>();
    
    // Navigation buttons for overflow handling
    private Button leftNavButton;
    private Button rightNavButton;
    private ScrollPane tabScrollPane;
    
    public CustomTabPane() {
        // Container principal
        this.setSpacing(0);
        this.getStyleClass().add("custom-tab-pane");
        
        // Header des onglets avec navigation
        VBox headerContainer = new VBox();
        headerContainer.getStyleClass().add("custom-tab-header-container");
        
        // Ligne des boutons de navigation et onglets
        HBox navigationRow = new HBox();
        navigationRow.setAlignment(Pos.CENTER_LEFT);
        navigationRow.getStyleClass().add("custom-tab-navigation-row");
        
        // Bouton de navigation gauche
        leftNavButton = createNavigationButton("◀", -1);
        leftNavButton.setVisible(false);
        
        // Zone scrollable pour les onglets
        tabHeader = new HBox();
        tabHeader.setAlignment(Pos.CENTER_LEFT);
        tabHeader.getStyleClass().add("custom-tab-header");
        
        tabScrollPane = new ScrollPane(tabHeader);
        tabScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tabScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        tabScrollPane.setFitToHeight(true);
        tabScrollPane.getStyleClass().add("custom-tab-scroll-pane");
        HBox.setHgrow(tabScrollPane, Priority.ALWAYS);
        
        // Bouton de navigation droite  
        rightNavButton = createNavigationButton("▶", 1);
        rightNavButton.setVisible(false);
        
        navigationRow.getChildren().addAll(leftNavButton, tabScrollPane, rightNavButton);
        headerContainer.getChildren().add(navigationRow);
        
        // Zone de contenu
        contentArea = new VBox();
        contentArea.getStyleClass().add("custom-tab-content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        this.getChildren().addAll(headerContainer, contentArea);
        
        // Styles par défaut
        applyDefaultStyles();
        
        // Gérer le redimensionnement pour afficher/masquer les boutons de navigation
        this.widthProperty().addListener((obs, oldWidth, newWidth) -> updateNavigationButtons());
    }
    
    private Button createNavigationButton(String text, int direction) {
        Button navButton = new Button(text);
        navButton.getStyleClass().addAll("custom-tab-nav-button", "navigation-button");
        
        // Style complet avec couleurs MAGSAV
        String navButtonStyle = "-fx-background-color: #091326; " +
                               "-fx-text-fill: #6B71F2; " +
                               "-fx-border-color: #6B71F2; " +
                               "-fx-border-width: 2px; " +
                               "-fx-border-radius: 5px; " +
                               "-fx-background-radius: 5px; " +
                               "-fx-padding: 5 10; " +
                               "-fx-font-size: 14px; " +
                               "-fx-font-weight: bold;";
        navButton.setStyle(navButtonStyle);
        
        // Effet hover
        navButton.setOnMouseEntered(e -> {
            String hoverStyle = navButtonStyle + 
                               "-fx-background-color: #142240; " +
                               "-fx-text-fill: #FFFFFF;";
            navButton.setStyle(hoverStyle);
        });
        
        navButton.setOnMouseExited(e -> navButton.setStyle(navButtonStyle));
        
        // Action de navigation
        navButton.setOnAction(e -> scrollTabs(direction));
        
        return navButton;
    }
    
    private void scrollTabs(int direction) {
        double scrollAmount = 100; // pixels
        double currentValue = tabScrollPane.getHvalue();
        double newValue = currentValue + (direction * scrollAmount / tabHeader.getWidth());
        newValue = Math.max(0, Math.min(1, newValue));
        tabScrollPane.setHvalue(newValue);
    }
    
    private void updateNavigationButtons() {
        // Afficher les boutons de navigation si nécessaire
        boolean needsNavigation = tabHeader.getWidth() > tabScrollPane.getWidth();
        leftNavButton.setVisible(needsNavigation);
        rightNavButton.setVisible(needsNavigation);
    }
    
    public void addTab(CustomTab tab) {
        tabs.add(tab);
        
        Button tabButton = createTabButton(tab);
        tabButtons.add(tabButton);
        tabHeader.getChildren().add(tabButton);
        
        // Sélectionner le premier onglet automatiquement
        if (tabs.size() == 1) {
            selectTab(tab);
        }
        
        updateNavigationButtons();
    }
    
    private Button createTabButton(CustomTab tab) {
        String buttonText = tab.getIconText() != null ? 
            tab.getIconText() + " " + tab.getText() : 
            tab.getText();
            
        Button tabButton = new Button(buttonText);
        tabButton.getStyleClass().addAll("custom-tab-button", "tab-button");
        
        // Style MAGSAV pour onglet non sélectionné
        String inactiveStyle = "-fx-background-color: #091326; " +
                              "-fx-text-fill: #6B71F2; " +
                              "-fx-border-color: #6B71F2; " +
                              "-fx-border-width: 0 0 2px 0; " +
                              "-fx-background-radius: 0; " +
                              "-fx-padding: 10 15; " +
                              "-fx-font-size: 12px;";
        
        // Style MAGSAV pour onglet sélectionné
        String activeStyle = "-fx-background-color: #142240; " +
                            "-fx-text-fill: #FFFFFF; " +
                            "-fx-border-color: #6B71F2; " +
                            "-fx-border-width: 0 0 3px 0; " +
                            "-fx-background-radius: 0; " +
                            "-fx-padding: 10 15; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold;";
        
        tabButton.setStyle(inactiveStyle);
        
        // Action de sélection
        tabButton.setOnAction(e -> selectTab(tab));
        
        // Effet hover pour onglets non sélectionnés
        tabButton.setOnMouseEntered(e -> {
            if (selectedTab.get() != tab) {
                String hoverStyle = inactiveStyle + "-fx-background-color: #1A2B47;";
                tabButton.setStyle(hoverStyle);
            }
        });
        
        tabButton.setOnMouseExited(e -> {
            if (selectedTab.get() != tab) {
                tabButton.setStyle(inactiveStyle);
            } else {
                tabButton.setStyle(activeStyle);
            }
        });
        
        // Listener pour mise à jour du style selon la sélection
        selectedTab.addListener((obs, oldTab, newTab) -> {
            if (newTab == tab) {
                tabButton.setStyle(activeStyle);
            } else {
                tabButton.setStyle(inactiveStyle);
            }
        });
        
        return tabButton;
    }
    
    public void selectTab(CustomTab tab) {
        if (tabs.contains(tab)) {
            selectedTab.set(tab);
            
            // Mettre à jour le contenu
            contentArea.getChildren().clear();
            if (tab.getContent() != null) {
                contentArea.getChildren().add(tab.getContent());
                VBox.setVgrow(tab.getContent(), Priority.ALWAYS);
            }
        }
    }
    
    public void selectTab(int index) {
        if (index >= 0 && index < tabs.size()) {
            selectTab(tabs.get(index));
        }
    }
    
    private void applyDefaultStyles() {
        // Styles CSS pour le composant
        String componentStyle = "-fx-background-color: #091326; -fx-spacing: 0;";
        this.setStyle(componentStyle);
        
        // Style pour le header
        String headerStyle = "-fx-background-color: #091326; -fx-padding: 0; -fx-spacing: 0;";
        tabHeader.setStyle(headerStyle);
        
        // Style pour la zone de contenu
        String contentStyle = "-fx-background-color: #091326; -fx-padding: 10;";
        contentArea.setStyle(contentStyle);
        
        // Style pour le ScrollPane
        String scrollStyle = "-fx-background-color: #091326; " +
                           "-fx-background: #091326; " +
                           "-fx-border-color: transparent;";
        tabScrollPane.setStyle(scrollStyle);
    }
    
    // Getters
    public List<CustomTab> getTabs() { return new ArrayList<>(tabs); }
    public CustomTab getSelectedTab() { return selectedTab.get(); }
    public ObjectProperty<CustomTab> selectedTabProperty() { return selectedTab; }
    
    // Méthodes utilitaires
    public void clear() {
        tabs.clear();
        tabButtons.clear();
        tabHeader.getChildren().clear();
        contentArea.getChildren().clear();
        selectedTab.set(null);
    }
}
