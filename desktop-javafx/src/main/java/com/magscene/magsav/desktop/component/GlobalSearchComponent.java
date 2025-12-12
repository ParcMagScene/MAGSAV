package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.core.di.ApplicationContext;
import com.magscene.magsav.desktop.core.navigation.NavigationManager;
import com.magscene.magsav.desktop.core.navigation.Route;
import com.magscene.magsav.desktop.core.search.GlobalSearchManager;
import com.magscene.magsav.desktop.core.search.SearchProvider;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.GlobalSearchService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Composant de recherche globale pour MAGSAV 3.0
 * 
 * Fournit une interface de recherche dans tous les modules
 * avec suggestions et résultats en temps réel groupés par type
 * Utilise GlobalSearchService pour les données API et GlobalSearchManager pour les vues
 * 
 * @version 3.0.0-refactored
 */
public class GlobalSearchComponent extends HBox {

    private final GlobalSearchManager searchManager;
    private final GlobalSearchService globalSearchService;
    private final TextField searchField;
    private final Button searchButton;
    private final Button clearButton;
    private final Label resultCountLabel;
    private final Popup resultsPopup;
    private final VBox resultsContainer;
    private final ListView<String> suggestionsList;

    public GlobalSearchComponent() {
        this.searchManager = GlobalSearchManager.getInstance();
        
        // Initialiser le GlobalSearchService avec l'ApiService pour charger les vraies données
        ApiService apiService = ApplicationContext.getInstance().getInstance(ApiService.class);
        this.globalSearchService = new GlobalSearchService(apiService);
        
        this.searchField = new TextField();
        this.searchButton = new Button("🔍");
        this.clearButton = new Button("✖");
        this.resultCountLabel = new Label();
        this.resultsPopup = new Popup();
        this.resultsContainer = new VBox(5);
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
        VBox popupContent = new VBox(8);
        popupContent.setPadding(new Insets(12));
        popupContent.getStyleClass().add("search-results-popup");
        popupContent.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #3498db; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        popupContent.setMinWidth(400);
        popupContent.setMaxWidth(500);
        popupContent.setMaxHeight(450);

        // Titre des résultats
        Label resultsTitle = new Label("🔍 Résultats de recherche");
        resultsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        
        // Container scrollable pour les résultats groupés
        ScrollPane scrollPane = new ScrollPane(resultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPrefHeight(350);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        resultsContainer.setSpacing(10);
        resultsContainer.setPadding(new Insets(5));
        
        // Suggestions par défaut (affichées quand pas de recherche)
        suggestionsList.setPrefHeight(150);
        suggestionsList.getStyleClass().add("suggestions-list");
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
                String searchTerm = extractSearchTermFromSuggestion(selected);
                searchField.setText(searchTerm);
                performSearch();
            }
        });

        popupContent.getChildren().addAll(resultsTitle, scrollPane);
        resultsPopup.getContent().add(popupContent);
        resultsPopup.setAutoHide(true);
    }
    
    /**
     * Met à jour l'affichage des résultats groupés par type
     * Combine les résultats du GlobalSearchService (données API) et du GlobalSearchManager (vues)
     */
    private void updateResultsDisplay() {
        resultsContainer.getChildren().clear();
        
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            // Afficher les suggestions par défaut
            Label suggestionsLabel = new Label("💡 Suggestions de recherche");
            suggestionsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
            resultsContainer.getChildren().add(suggestionsLabel);
            resultsContainer.getChildren().add(suggestionsList);
            return;
        }
        
        // Recharger les données si pas encore chargées
        if (!globalSearchService.isDataLoaded()) {
            System.out.println("🔄 GlobalSearchService: Rechargement des données (premier accès)...");
            globalSearchService.refresh();
        }
        
        // Récupérer les résultats du GlobalSearchService (données API de toutes les tables)
        ObservableList<GlobalSearchService.SearchResult> serviceResults = globalSearchService.search(searchTerm);
        
        // Grouper les résultats par type
        Map<String, List<GlobalSearchService.SearchResult>> resultsByType = new LinkedHashMap<>();
        for (GlobalSearchService.SearchResult result : serviceResults) {
            resultsByType.computeIfAbsent(result.getType(), k -> new ArrayList<>()).add(result);
        }
        
        if (resultsByType.isEmpty()) {
            // Aucun résultat
            Label noResults = new Label("❌ Aucun résultat pour \"" + searchTerm + "\"");
            noResults.setStyle("-fx-text-fill: #e74c3c; -fx-font-style: italic;");
            resultsContainer.getChildren().add(noResults);
            
            // Afficher les suggestions
            Label suggestionsLabel = new Label("\n💡 Essayez ces suggestions :");
            suggestionsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
            resultsContainer.getChildren().add(suggestionsLabel);
            resultsContainer.getChildren().add(suggestionsList);
            return;
        }
        
        // Afficher les résultats groupés par type
        for (Map.Entry<String, List<GlobalSearchService.SearchResult>> entry : resultsByType.entrySet()) {
            String typeName = entry.getKey();
            List<GlobalSearchService.SearchResult> typeResults = entry.getValue();
            
            // Section pour chaque type
            VBox moduleSection = createModuleSectionFromService(typeName, typeResults);
            resultsContainer.getChildren().add(moduleSection);
        }
    }
    
    /**
     * Crée une section pour un type avec ses résultats (GlobalSearchService)
     * Priorise l'affichage des correspondances LOCMAT
     */
    private VBox createModuleSectionFromService(String typeName, List<GlobalSearchService.SearchResult> results) {
        VBox section = new VBox(5);
        section.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 1;"
        );
        
        // En-tête du type avec icône et compteur
        String icon = getModuleIcon(typeName);
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label moduleLabel = new Label(icon + " " + typeName);
        moduleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label countLabel = new Label("(" + results.size() + ")");
        countLabel.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 2 8; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 11px;"
        );
        
        header.getChildren().addAll(moduleLabel, countLabel);
        section.getChildren().add(header);
        
        // Liste des résultats (max 5 affichés initialement)
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (results != null && !results.isEmpty()) {
            VBox resultsList = new VBox(3);
            resultsList.setPadding(new Insets(5, 0, 0, 15));
            
            int displayCount = Math.min(results.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                GlobalSearchService.SearchResult result = results.get(i);
                
                // Vérifier si c'est une correspondance LOCMAT
                boolean isLocmatMatch = result.getLocmatCode() != null && 
                    result.getLocmatCode().toLowerCase().contains(searchTerm);
                
                // Créer le label avec mise en évidence du code LOCMAT si correspondance
                String displayText = "• ";
                if (isLocmatMatch && result.getLocmatCode() != null && !result.getLocmatCode().isEmpty()) {
                    displayText += "🏷️ [" + result.getLocmatCode() + "] " + result.getName();
                } else {
                    displayText += result.getName();
                }
                
                Label resultLabel = new Label(displayText);
                
                // Style différent pour les correspondances LOCMAT
                if (isLocmatMatch) {
                    resultLabel.setStyle("-fx-text-fill: #e67e22; -fx-cursor: hand; -fx-font-weight: bold;");
                } else {
                    resultLabel.setStyle("-fx-text-fill: #34495e; -fx-cursor: hand;");
                }
                resultLabel.setWrapText(true);
                
                // Ajouter la description si disponible (sans répéter le LOCMAT)
                if (result.getDescription() != null && !result.getDescription().isEmpty()) {
                    String desc = result.getDescription();
                    // Supprimer le LOCMAT de la description si déjà affiché
                    if (isLocmatMatch && result.getLocmatCode() != null) {
                        desc = desc.replace(" [LOCMAT: " + result.getLocmatCode() + "]", "");
                    }
                    if (!desc.isEmpty()) {
                        resultLabel.setText(displayText + " — " + desc);
                    }
                }
                
                // Effet hover - conserver le style LOCMAT si c'est une correspondance
                final boolean finalIsLocmatMatch = isLocmatMatch;
                final String normalStyle = finalIsLocmatMatch 
                    ? "-fx-text-fill: #e67e22; -fx-cursor: hand; -fx-font-weight: bold;"
                    : "-fx-text-fill: #34495e; -fx-cursor: hand;";
                final String hoverStyle = finalIsLocmatMatch
                    ? "-fx-text-fill: #d35400; -fx-cursor: hand; -fx-font-weight: bold; -fx-underline: true;"
                    : "-fx-text-fill: #3498db; -fx-cursor: hand; -fx-underline: true;";
                
                resultLabel.setOnMouseEntered(e -> resultLabel.setStyle(hoverStyle));
                resultLabel.setOnMouseExited(e -> resultLabel.setStyle(normalStyle));
                
                // Clic sur un résultat
                final GlobalSearchService.SearchResult finalResult = result;
                resultLabel.setOnMouseClicked(e -> {
                    onServiceResultSelected(typeName, finalResult);
                    hideResultsPopup();
                });
                
                resultsList.getChildren().add(resultLabel);
            }
            
            // Si plus de 5 résultats, afficher un bouton "Afficher plus" bien visible
            if (results.size() > 5) {
                int remaining = results.size() - 5;
                Button moreButton = new Button("➕ Afficher " + remaining + " résultat(s) de plus...");
                moreButton.setStyle(
                    "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 4;"
                );
                moreButton.setOnMouseEntered(e -> moreButton.setStyle(
                    "-fx-background-color: #2980b9; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 4;"
                ));
                moreButton.setOnMouseExited(e -> moreButton.setStyle(
                    "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 4;"
                ));
                
                // Clic sur "Afficher plus" - afficher tous les résultats
                final String finalTypeName = typeName;
                final List<GlobalSearchService.SearchResult> allResults = results;
                moreButton.setOnAction(e -> {
                    showAllServiceResultsForType(finalTypeName, allResults);
                });
                
                resultsList.getChildren().add(moreButton);
            }
            
            section.getChildren().add(resultsList);
        }
        
        return section;
    }
    
    /**
     * Appelé quand un résultat du GlobalSearchService est sélectionné
     * Navigue vers la page appropriée et sélectionne l'élément
     */
    private void onServiceResultSelected(String typeName, GlobalSearchService.SearchResult result) {
        System.out.println("🔍 Résultat sélectionné: [" + typeName + "] " + result.getName() + " (ID: " + result.getId() + ")");
        
        // Mapper le type vers la route appropriée
        Route targetRoute = getRouteForType(typeName);
        
        if (targetRoute != null) {
            // Utiliser NavigationManager pour naviguer et sélectionner
            NavigationManager.getInstance().navigateToWithSelection(targetRoute, result.getId());
        } else {
            System.out.println("⚠️ Aucune route trouvée pour le type: " + typeName);
        }
    }
    
    /**
     * Mappe un type de résultat vers une route de navigation
     */
    private Route getRouteForType(String typeName) {
        if (typeName == null) return null;
        
        String type = typeName.toLowerCase();
        
        if (type.contains("équipement") || type.contains("equipment")) {
            return Route.EQUIPMENT;
        }
        if (type.contains("client")) {
            return Route.CLIENTS;
        }
        if (type.contains("fournisseur") || type.contains("supplier")) {
            return Route.SUPPLIERS;
        }
        if (type.contains("personnel") || type.contains("employé") || type.contains("employee")) {
            return Route.PERSONNEL;
        }
        if (type.contains("véhicule") || type.contains("vehicle")) {
            return Route.VEHICLES;
        }
        if (type.contains("sav") || type.contains("intervention") || type.contains("réparation")) {
            return Route.SAV;
        }
        if (type.contains("contrat") || type.contains("contract")) {
            return Route.CONTRACTS;
        }
        if (type.contains("projet") || type.contains("project") || type.contains("vente")) {
            return Route.SALES;
        }
        
        return null;
    }
    
    /**
     * Affiche tous les résultats d'un type dans une liste déroulante (GlobalSearchService)
     */
    private void showAllServiceResultsForType(String typeName, List<GlobalSearchService.SearchResult> allResults) {
        resultsContainer.getChildren().clear();
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        // En-tête avec bouton retour
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backButton = new Button("← Retour");
        backButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11px; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 4; " +
            "-fx-padding: 4 10;"
        );
        backButton.setOnAction(e -> updateResultsDisplay());
        
        String icon = getModuleIcon(typeName);
        Label titleLabel = new Label(icon + " " + typeName + " - Tous les résultats (" + allResults.size() + ")");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        headerBox.getChildren().addAll(backButton, titleLabel);
        resultsContainer.getChildren().add(headerBox);
        
        // Liste de tous les résultats
        VBox allResultsList = new VBox(4);
        allResultsList.setPadding(new Insets(10, 0, 0, 0));
        
        for (GlobalSearchService.SearchResult result : allResults) {
            // Vérifier si c'est une correspondance LOCMAT
            boolean isLocmatMatch = result.getLocmatCode() != null && 
                result.getLocmatCode().toLowerCase().contains(searchTerm);
            
            HBox resultRow = new HBox(8);
            resultRow.setAlignment(Pos.CENTER_LEFT);
            
            // Style différent pour les correspondances LOCMAT
            String normalBgStyle = isLocmatMatch 
                ? "-fx-background-color: #fff3cd; -fx-padding: 6 8; -fx-cursor: hand; -fx-background-radius: 4;"
                : "-fx-background-color: transparent; -fx-padding: 6 8; -fx-cursor: hand;";
            resultRow.setStyle(normalBgStyle);
            
            // Créer le label avec mise en évidence du code LOCMAT si correspondance
            String displayText = "• ";
            if (isLocmatMatch && result.getLocmatCode() != null && !result.getLocmatCode().isEmpty()) {
                displayText += "🏷️ [" + result.getLocmatCode() + "] " + result.getName();
            } else {
                displayText += result.getName();
            }
            
            Label resultLabel = new Label(displayText);
            if (isLocmatMatch) {
                resultLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold;");
            } else {
                resultLabel.setStyle("-fx-text-fill: #34495e;");
            }
            resultLabel.setWrapText(true);
            
            if (result.getDescription() != null && !result.getDescription().isEmpty()) {
                String desc = result.getDescription();
                // Supprimer le LOCMAT de la description si déjà affiché
                if (isLocmatMatch && result.getLocmatCode() != null) {
                    desc = desc.replace(" [LOCMAT: " + result.getLocmatCode() + "]", "");
                }
                if (!desc.isEmpty()) {
                    Label subtitleLabel = new Label(" — " + desc);
                    subtitleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                    resultRow.getChildren().addAll(resultLabel, subtitleLabel);
                } else {
                    resultRow.getChildren().add(resultLabel);
                }
            } else {
                resultRow.getChildren().add(resultLabel);
            }
            
            // Effets hover - conserver la mise en évidence LOCMAT
            final boolean finalIsLocmatMatch = isLocmatMatch;
            final String hoverStyle = finalIsLocmatMatch 
                ? "-fx-background-color: #ffeeba; -fx-padding: 6 8; -fx-cursor: hand; -fx-background-radius: 4;"
                : "-fx-background-color: #e8f4fc; -fx-padding: 6 8; -fx-cursor: hand; -fx-background-radius: 4;";
            final String normalStyle = finalIsLocmatMatch 
                ? "-fx-background-color: #fff3cd; -fx-padding: 6 8; -fx-cursor: hand; -fx-background-radius: 4;"
                : "-fx-background-color: transparent; -fx-padding: 6 8; -fx-cursor: hand;";
            
            resultRow.setOnMouseEntered(e -> resultRow.setStyle(hoverStyle));
            resultRow.setOnMouseExited(e -> resultRow.setStyle(normalStyle));
            
            // Clic sur un résultat
            final GlobalSearchService.SearchResult finalResult = result;
            resultRow.setOnMouseClicked(e -> {
                onServiceResultSelected(typeName, finalResult);
                hideResultsPopup();
            });
            
            allResultsList.getChildren().add(resultRow);
        }
        
        resultsContainer.getChildren().add(allResultsList);
    }
    
    /**
     * Crée une section pour un module avec ses résultats (ancien SearchProvider - gardé pour compatibilité)
     */
    @SuppressWarnings("unused")
    private VBox createModuleSection(String moduleName, int count, List<SearchProvider.SearchResult> results) {
        VBox section = new VBox(5);
        section.setStyle(
            "-fx-background-color: #f8f9fa; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8; " +
            "-fx-border-color: #dee2e6; " +
            "-fx-border-radius: 6; " +
            "-fx-border-width: 1;"
        );
        
        // En-tête du module avec icône et compteur
        String icon = getModuleIcon(moduleName);
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label moduleLabel = new Label(icon + " " + moduleName);
        moduleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        Label countLabel = new Label("(" + count + ")");
        countLabel.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-padding: 2 8; " +
            "-fx-background-radius: 10; " +
            "-fx-font-size: 11px;"
        );
        
        header.getChildren().addAll(moduleLabel, countLabel);
        section.getChildren().add(header);
        
        // Liste des résultats (max 5)
        if (results != null && !results.isEmpty()) {
            VBox resultsList = new VBox(3);
            resultsList.setPadding(new Insets(5, 0, 0, 15));
            
            int displayCount = Math.min(results.size(), 5);
            for (int i = 0; i < displayCount; i++) {
                SearchProvider.SearchResult result = results.get(i);
                
                Label resultLabel = new Label("• " + result.getTitle());
                resultLabel.setStyle("-fx-text-fill: #34495e; -fx-cursor: hand;");
                resultLabel.setWrapText(true);
                
                if (result.getSubtitle() != null && !result.getSubtitle().isEmpty()) {
                    resultLabel.setText("• " + result.getTitle() + " — " + result.getSubtitle());
                }
                
                // Effet hover
                resultLabel.setOnMouseEntered(e -> resultLabel.setStyle("-fx-text-fill: #3498db; -fx-cursor: hand; -fx-underline: true;"));
                resultLabel.setOnMouseExited(e -> resultLabel.setStyle("-fx-text-fill: #34495e; -fx-cursor: hand;"));
                
                // Clic sur un résultat
                final SearchProvider.SearchResult finalResult = result;
                resultLabel.setOnMouseClicked(e -> {
                    onResultSelected(moduleName, finalResult);
                    hideResultsPopup();
                });
                
                resultsList.getChildren().add(resultLabel);
            }
            
            section.getChildren().add(resultsList);
        }
        
        return section;
    }
    
    /**
     * Retourne l'icône appropriée pour un module
     */
    private String getModuleIcon(String moduleName) {
        if (moduleName == null) return "📦";
        
        String name = moduleName.toLowerCase();
        if (name.contains("équipement") || name.contains("materiel") || name.contains("parc") || name.contains("equipment")) return "🔧";
        if (name.contains("audio") || name.contains("son")) return "🎵";
        if (name.contains("éclairage") || name.contains("lumiere")) return "💡";
        if (name.contains("vidéo") || name.contains("video")) return "📹";
        if (name.contains("structure")) return "🏗️";
        if (name.contains("client")) return "👥";
        if (name.contains("fournisseur") || name.contains("supplier")) return "🏭";
        if (name.contains("sav") || name.contains("intervention") || name.contains("réparation")) return "🛠️";
        if (name.contains("véhicule") || name.contains("vehicule") || name.contains("vehicle")) return "🚐";
        if (name.contains("personnel") || name.contains("employé") || name.contains("employee")) return "👨‍💼";
        if (name.contains("planning") || name.contains("calendrier")) return "📅";
        if (name.contains("contrat") || name.contains("contract")) return "📝";
        if (name.contains("vente") || name.contains("projet") || name.contains("affaire") || name.contains("project")) return "💼";
        
        return "📦";
    }
    
    /**
     * Appelé quand un résultat est sélectionné
     */
    private void onResultSelected(String moduleName, SearchProvider.SearchResult result) {
        System.out.println("🔍 Résultat sélectionné: [" + moduleName + "] " + result.getTitle() + " (ID: " + result.getId() + ")");
        // TODO: Naviguer vers le module et sélectionner l'élément
        // Cette fonctionnalité sera implémentée en coordination avec la navigation principale
    }

    /**
     * Effectue la recherche globale
     */
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            searchManager.performGlobalSearch(searchTerm);
            updateResultsDisplay();
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
        
        // Mettre à jour l'affichage des résultats groupés
        updateResultsDisplay();

        // Couleur selon le nombre de résultats
        if (totalResults == 0) {
            resultCountLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else if (totalResults < 10) {
            resultCountLabel.setStyle("-fx-text-fill: #f39c12;");
        } else {
            resultCountLabel.setStyle("-fx-text-fill: #27ae60;");
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
    
    /**
     * Affiche tous les résultats d'un module dans une liste déroulante
     */
    @SuppressWarnings("unused") // Réservé pour future utilisation
    private void showAllResultsForModule(String moduleName, List<SearchProvider.SearchResult> allResults) {
        // Mettre à jour le container des résultats pour afficher tous les résultats de ce module
        resultsContainer.getChildren().clear();
        
        // En-tête avec bouton retour
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Button backButton = new Button("← Retour");
        backButton.setStyle(
            "-fx-background-color: #3498db; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 11px; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 4; " +
            "-fx-padding: 4 10;"
        );
        backButton.setOnAction(e -> updateResultsDisplay());
        
        String icon = getModuleIcon(moduleName);
        Label titleLabel = new Label(icon + " " + moduleName + " - Tous les résultats (" + allResults.size() + ")");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #2c3e50;");
        
        headerBox.getChildren().addAll(backButton, titleLabel);
        resultsContainer.getChildren().add(headerBox);
        
        // Liste de tous les résultats
        VBox allResultsList = new VBox(4);
        allResultsList.setPadding(new Insets(10, 0, 0, 0));
        
        for (SearchProvider.SearchResult result : allResults) {
            HBox resultRow = new HBox(8);
            resultRow.setAlignment(Pos.CENTER_LEFT);
            resultRow.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-padding: 6 8; " +
                "-fx-cursor: hand;"
            );
            
            Label resultLabel = new Label("• " + result.getTitle());
            resultLabel.setStyle("-fx-text-fill: #34495e;");
            resultLabel.setWrapText(true);
            
            if (result.getSubtitle() != null && !result.getSubtitle().isEmpty()) {
                Label subtitleLabel = new Label(" — " + result.getSubtitle());
                subtitleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                resultRow.getChildren().addAll(resultLabel, subtitleLabel);
            } else {
                resultRow.getChildren().add(resultLabel);
            }
            
            // Effets hover
            resultRow.setOnMouseEntered(e -> resultRow.setStyle(
                "-fx-background-color: #e8f4fc; " +
                "-fx-padding: 6 8; " +
                "-fx-cursor: hand; " +
                "-fx-background-radius: 4;"
            ));
            resultRow.setOnMouseExited(e -> resultRow.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-padding: 6 8; " +
                "-fx-cursor: hand;"
            ));
            
            // Clic sur un résultat
            final SearchProvider.SearchResult finalResult = result;
            resultRow.setOnMouseClicked(e -> {
                onResultSelected(moduleName, finalResult);
                hideResultsPopup();
            });
            
            allResultsList.getChildren().add(resultRow);
        }
        
        resultsContainer.getChildren().add(allResultsList);
    }
}