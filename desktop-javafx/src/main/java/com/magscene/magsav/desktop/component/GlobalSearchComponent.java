package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.core.search.GlobalSearchManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

/**
 * Composant de recherche globale pour MAGSAV 3.0
 * 
 * Fournit une interface de recherche dans tous les modules
 * avec suggestions et résultats en temps réel
 * 
 * @version 3.0.0-refactored
 */
public class GlobalSearchComponent extends HBox {

    private final GlobalSearchManager searchManager;
    private final TextField searchField;
    private final Button searchButton;
    private final Button clearButton;
    private final Label resultCountLabel;
    private final Popup resultsPopup;
    private final ListView<String> suggestionsList;

    public GlobalSearchComponent() {
        this.searchManager = GlobalSearchManager.getInstance();
        this.searchField = new TextField();
        this.searchButton = new Button("🔍");
        this.clearButton = new Button("✖");
        this.resultCountLabel = new Label();
        this.resultsPopup = new Popup();
        this.suggestionsList = new ListView<>();

        initializeComponent();
        setupEventHandlers();
        setupPopup();
    }

    /**
     * Initialise le composant
     */
    private void initializeComponent() {
        setSpacing(5);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(2));
        getStyleClass().add("global-search-component");

        // Configuration du champ de recherche
        searchField.setPromptText("🔍 Recherche globale dans tous les modules...");
        searchField.setPrefWidth(300);
        searchField.setMaxWidth(400);
        searchField.getStyleClass().add("global-search-field");

        // Configuration des boutons
        searchButton.getStyleClass().add("search-button");
        searchButton.setTooltip(new Tooltip("Lancer la recherche (Entrée)"));

        clearButton.getStyleClass().add("clear-button");
        clearButton.setTooltip(new Tooltip("Effacer la recherche"));
        clearButton.setVisible(false);

        // Label de résultats
        resultCountLabel.getStyleClass().add("result-count-label");
        resultCountLabel.setVisible(false);

        getChildren().addAll(searchField, searchButton, clearButton, resultCountLabel);
        HBox.setHgrow(searchField, Priority.ALWAYS);
    }

    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Recherche au clic sur le bouton ou Entrée
        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());

        // Effacer la recherche
        clearButton.setOnAction(e -> clearSearch());

        // Recherche en temps réel avec délai
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                clearButton.setVisible(false);
                resultCountLabel.setVisible(false);
                hideResultsPopup();
            } else {
                clearButton.setVisible(true);
                // Recherche automatique après 500ms d'inactivité
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
                    if (newText.equals(searchField.getText())) {
                        performSearch();
                    }
                }));
                timeline.play();
            }
        });

        // Gestion du focus pour afficher/masquer les suggestions
        searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused && !searchField.getText().trim().isEmpty()) {
                showResultsPopup();
            } else {
                // Délai pour permettre la sélection d'une suggestion
                Timeline timeline = new Timeline(new KeyFrame(Duration.millis(200), e -> hideResultsPopup()));
                timeline.play();
            }
        });

        // Écouter les changements de résultats de recherche
        searchManager.addSearchListener(this::updateResultCount);
    }

    /**
     * Configure la popup de résultats
     */
    private void setupPopup() {
        VBox popupContent = new VBox(5);
        popupContent.setPadding(new Insets(10));
        popupContent.getStyleClass().add("search-results-popup");
        popupContent.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1;");

        Label suggestionsTitle = new Label("💡 Suggestions de recherche");
        suggestionsTitle.getStyleClass().add("suggestions-title");

        suggestionsList.setPrefHeight(150);
        suggestionsList.getStyleClass().add("suggestions-list");

        // Suggestions par défaut
        suggestionsList.getItems().addAll(
                "🎵 Audio : micros, enceintes, consoles",
                "💡 Éclairage : projecteurs, LED, gradateurs",
                "📹 Vidéo : écrans, caméras, projecteurs",
                "🏗️ Structure : podiums, barres, trépieds",
                "👥 Clients : entreprises, particuliers",
                "📋 SAV : interventions, pannes, maintenance",
                "🚐 Véhicules : camions, utilitaires",
                "👨‍💼 Personnel : techniciens, chauffeurs");

        // Sélection d'une suggestion
        suggestionsList.setOnMouseClicked(e -> {
            String selected = suggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Extraire le terme de recherche de la suggestion
                String searchTerm = extractSearchTermFromSuggestion(selected);
                searchField.setText(searchTerm);
                performSearch();
                hideResultsPopup();
            }
        });

        popupContent.getChildren().addAll(suggestionsTitle, suggestionsList);
        resultsPopup.getContent().add(popupContent);
        resultsPopup.setAutoHide(true);
    }

    /**
     * Effectue la recherche globale
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            searchManager.performGlobalSearch(searchTerm);
            showResultsPopup();
        }
    }

    /**
     * Efface la recherche
     */
    private void clearSearch() {
        searchField.clear();
        searchManager.clearSearch();
        clearButton.setVisible(false);
        resultCountLabel.setVisible(false);
        hideResultsPopup();
    }

    /**
     * Met à jour le compteur de résultats
     */
    private void updateResultCount(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            resultCountLabel.setVisible(false);
            return;
        }

        int totalResults = searchManager.getTotalResultCount();
        resultCountLabel.setText(String.format("(%d résultats)", totalResults));
        resultCountLabel.setVisible(true);

        // Couleur selon le nombre de résultats
        if (totalResults == 0) {
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        } else if (totalResults < 10) {
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        } else {
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
    }

    /**
     * Affiche la popup de résultats
     */
    private void showResultsPopup() {
        if (!resultsPopup.isShowing()) {
            resultsPopup.show(searchField,
                    searchField.localToScreen(0, 0).getX(),
                    searchField.localToScreen(0, 0).getY() + searchField.getHeight() + 2);
        }
    }

    /**
     * Masque la popup de résultats
     */
    private void hideResultsPopup() {
        if (resultsPopup.isShowing()) {
            resultsPopup.hide();
        }
    }

    /**
     * Extrait le terme de recherche d'une suggestion
     */
    private String extractSearchTermFromSuggestion(String suggestion) {
        // Extrait le mot-clé après l'emoji et avant ":"
        if (suggestion.contains(":")) {
            String part = suggestion.split(":")[0];
            return part.replaceAll("[^\\p{L}\\p{Nd}\\s]", "").trim();
        }
        return suggestion.replaceAll("[^\\p{L}\\p{Nd}\\s]", "").trim();
    }

    /**
     * Définit le focus sur le champ de recherche
     */
    public void requestFocus() {
        searchField.requestFocus();
    }

    /**
     * Obtient le terme de recherche actuel
     */
    public String getSearchTerm() {
        return searchField.getText();
    }

    /**
     * Définit le terme de recherche
     */
    public void setSearchTerm(String searchTerm) {
        searchField.setText(searchTerm);
    }
}