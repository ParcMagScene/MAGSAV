package com.magscene.magsav.desktop.component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    @SuppressWarnings("unused")
    private ScrollPane tabScrollPane;
    
    public CustomTabPane() {
        // Container principal - STRUCTURE SIMPLIFIÉE pour effet dossiers collés
        this.setSpacing(0);
        this.getStyleClass().add("custom-tab-pane");
        
        // SUPPRESSION containers imbriqués - onglets directement dans la ligne; // Bouton de navigation gauche
        leftNavButton = createNavigationButton("◀", -1);
        leftNavButton.setVisible(false);
        
        // Zone des onglets DIRECTE - plus de ScrollPane pour éviter le padding
        tabHeader = new HBox();
        tabHeader.setAlignment(Pos.CENTER_LEFT);
        tabHeader.getStyleClass().add("custom-tab-header");
        tabHeader.setSpacing(0); // AUCUN ESPACEMENT pour effet collé
        HBox.setHgrow(tabHeader, Priority.ALWAYS);
        
        // Bouton de navigation droite  
        rightNavButton = createNavigationButton("▶", 1);
        rightNavButton.setVisible(false);
        
        // Ligne d'onglets DIRECTE dans le CustomTabPane - plus de containers
        HBox directTabRow = new HBox();
        directTabRow.setSpacing(0); // AUCUN ESPACEMENT 
        directTabRow.setAlignment(Pos.CENTER_LEFT);
        directTabRow.getChildren().addAll(leftNavButton, tabHeader, rightNavButton);
        
        // Zone de contenu
        contentArea = new VBox();
        contentArea.getStyleClass().add("custom-tab-content-area");
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        this.getChildren().addAll(directTabRow, contentArea);
        
        // Styles par défaut
        applyDefaultStyles();
        
        // Gérer le redimensionnement pour afficher/masquer les boutons de navigation
        this.widthProperty().addListener((obs, oldWidth, newWidth) -> updateNavigationButtons());
    }
    
    private Button createNavigationButton(String text, int direction) {
        Button navButton = new Button(text);
        navButton.getStyleClass().addAll("custom-tab-nav-button", "navigation-button");
        
        // Style complet avec couleurs MAGSAV Light
        String navButtonStyle = "-fx-background-color: #FFFFFF; " +
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
                               "-fx-background-color: #F8F9FA; " +
                               "-fx-text-fill: #6B71F2;";
            navButton.setStyle(hoverStyle);
        });
        
        navButton.setOnMouseExited(e -> navButton.setStyle(navButtonStyle));
        
        // Plus d'action de navigation - boutons masqués pour interface épurée
        navButton.setOnAction(e -> {}); // Action vide
        
        return navButton;
    }
    
    // SUPPRESSION scrollTabs() - Plus de ScrollPane, onglets toujours visibles
    
    private void updateNavigationButtons() {
        // SIMPLIFICATION - Navigation désactivée pour interface épurée; // Les onglets sont maintenant toujours visibles directement
        leftNavButton.setVisible(false);
        rightNavButton.setVisible(false);
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
        
        // Style MAGSAV Light pour onglet non sélectionné - EFFET DOSSIER
        String inactiveStyle = "-fx-background-color: #E9ECEF; " +
                              "-fx-text-fill: #6B71F2; " +
                              "-fx-border-color: #DEE2E6; " +
                              "-fx-border-width: 1px 1px 0 1px; " +
                              "-fx-background-radius: 8px 8px 0 0; " +
                              "-fx-border-radius: 8px 8px 0 0; " +
                              "-fx-padding: 10 15; " +
                              "-fx-font-size: 12px;";
        
        // Style MAGSAV Light pour onglet sélectionné - DOSSIER ACTIF COLLÉ AU CONTENU
        String activeStyle = "-fx-background-color: #FFFFFF; " +
                            "-fx-text-fill: #6B71F2; " +
                            "-fx-border-color: #6B71F2; " +
                            "-fx-border-width: 2px 2px 0 2px; " +
                            "-fx-background-radius: 8px 8px 0 0; " +
                            "-fx-border-radius: 8px 8px 0 0; " +
                            "-fx-padding: 10 15; " +
                            "-fx-font-size: 12px; " +
                            "-fx-font-weight: bold;";
        
        tabButton.setStyle(inactiveStyle);
        
        // Action de sélection
        tabButton.setOnAction(e -> selectTab(tab));
        
        // Effet hover pour onglets non sélectionnés - DOSSIER ÉCLAIRÉ
        tabButton.setOnMouseEntered(e -> {
            if (selectedTab.get() != tab) {
                String hoverStyle = "-fx-background-color: #F8F9FA; " +
                                  "-fx-text-fill: #6B71F2; " +
                                  "-fx-border-color: #6B71F2; " +
                                  "-fx-border-width: 1px 1px 0 1px; " +
                                  "-fx-background-radius: 8px 8px 0 0; " +
                                  "-fx-border-radius: 8px 8px 0 0; " +
                                  "-fx-padding: 10 15; " +
                                  "-fx-font-size: 12px;";
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
        // Styles CSS Light pour le composant - EFFET DOSSIERS PARFAITEMENT COLLÉS
        String componentStyle = "-fx-background-color: #FFFFFF; -fx-spacing: 0; -fx-padding: 0;";
        this.setStyle(componentStyle);
        
        // Style Light pour le header - AUCUNE MARGE pour coller parfaitement
        String headerStyle = "-fx-background-color: #F8F9FA; -fx-padding: 0; -fx-spacing: 0;";
        tabHeader.setStyle(headerStyle);
        
        // Style Light pour la zone de contenu - AUCUN PADDING pour continuité
        String contentStyle = "-fx-background-color: #FFFFFF; -fx-padding: 0;";
        contentArea.setStyle(contentStyle);
        
        // Plus de ScrollPane à styler - interface épurée directe
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
    
    /**
     * Ajouter une toolbar sous les onglets (nouveau pattern visuel)
     */
    public void setIntegratedToolbar(Node toolbar) {
        // Supprimer l'ancienne toolbar si elle existe
        if (this.getChildren().size() > 2) {
            this.getChildren().remove(1); // Retirer l'ancienne toolbar
        }
        
        // Insérer la nouvelle toolbar entre les onglets et le contenu
        if (toolbar != null) {
            this.getChildren().add(1, toolbar); // Position 1 : entre onglets et contenu
        }
    }
}
