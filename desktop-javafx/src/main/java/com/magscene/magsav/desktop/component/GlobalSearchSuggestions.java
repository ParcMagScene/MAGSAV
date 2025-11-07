package com.magscene.magsav.desktop.component;

import com.magscene.magsav.desktop.service.GlobalSearchService;
import com.magscene.magsav.desktop.service.GlobalSearchService.SearchResult;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.stage.Popup;

/**
 * Composant de suggestions pour la recherche globale
 * Affiche les résultats en temps réel sous la barre de recherche
 */
public class GlobalSearchSuggestions {
    
    @FunctionalInterface
    public interface NavigationCallback {
        void navigateToResult(SearchResult result);
    }
    
    private final Popup suggestionPopup;
    private final VBox suggestionContainer;
    private final GlobalSearchService searchService;
    private final TextField searchField;
    private final NavigationCallback navigationCallback;
    
    public GlobalSearchSuggestions(TextField searchField, NavigationCallback navigationCallback) {
        this.searchField = searchField;
        this.navigationCallback = navigationCallback;
        this.searchService = new GlobalSearchService(); 
        this.suggestionPopup = new Popup();
        this.suggestionContainer = createSuggestionContainer();
        
        setupPopup();
        setupSearchListener();
    }
    
    public GlobalSearchSuggestions(TextField searchField, GlobalSearchService searchService, NavigationCallback navigationCallback) {
        this.searchField = searchField;
        this.searchService = searchService;
        this.navigationCallback = navigationCallback;
        this.suggestionPopup = new Popup();
        this.suggestionContainer = createSuggestionContainer();
        
        setupPopup();
        setupSearchListener();
    }
    
    private VBox createSuggestionContainer() {
        VBox container = new VBox(1);
        container.setPadding(new Insets(2));
        container.setPrefWidth(380);
        container.setMaxHeight(300);
        container.setStyle("-fx-background-color: #142240; " +
                          "-fx-border-color: #7DD3FC; " +
                          "-fx-border-width: 0.5; " +
                          "-fx-border-radius: 6; " +
                          "-fx-background-radius: 6; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        
        return container;
    }
    
    private void setupPopup() {
        suggestionPopup.getContent().add(suggestionContainer);
        suggestionPopup.setAutoFix(false); // Désactiver le repositionnement automatique
        suggestionPopup.setAutoHide(true);
        suggestionPopup.setHideOnEscape(true);
        suggestionPopup.setConsumeAutoHidingEvents(false);
    }
    
    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSuggestions(newValue);
        });
        
        searchField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                // Délai pour permettre le clic sur une suggestion
                new Thread(() -> {
                    try { Thread.sleep(150); } catch (InterruptedException e) {}
                    javafx.application.Platform.runLater(() -> hideSuggestions());
                }).start();
            }
        });
    }
    
    private void updateSuggestions(String query) {
        if (query == null || query.trim().length() < 2) {
            hideSuggestions();
            return;
        }
        
        ObservableList<SearchResult> results = searchService.search(query);
        displaySuggestions(results);
    }
    
    private void displaySuggestions(ObservableList<SearchResult> results) {
        suggestionContainer.getChildren().clear();
        
        if (results.isEmpty()) {
            Label noResults = new Label("Aucun résultat trouvé");
            noResults.setStyle("-fx-text-fill: #7DD3FC; -fx-padding: 10;");
            noResults.setFont(Font.font("System", FontPosture.ITALIC, 12));
            suggestionContainer.getChildren().add(noResults);
        } else {
            // Grouper les résultats par type et limiter à 5 par type
            results.stream()
                .collect(java.util.stream.Collectors.groupingBy(SearchResult::getType))
                .forEach((type, typeResults) -> {
                    // En-tête de type
                    Label typeHeader = createTypeHeader(type);
                    suggestionContainer.getChildren().add(typeHeader);
                    
                    // Limiter à 5 résultats par type
                    typeResults.stream()
                        .limit(5)
                        .forEach(result -> {
                            HBox resultButton = createResultButton(result);
                            suggestionContainer.getChildren().add(resultButton);
                        });
                });
        }
        
        showSuggestions();
    }
    
    private Label createTypeHeader(String type) {
        Label header = new Label(type.toUpperCase());
        header.setStyle("-fx-text-fill: #5F65D9; " +
                       "-fx-background-color: #142240; " +
                       "-fx-padding: 2 6; " +
                       "-fx-font-weight: bold; " +
                       "-fx-border-width: 0;");
        header.setFont(Font.font("System", FontWeight.BOLD, 9));
        header.setPrefWidth(380);
        header.setMaxHeight(18);
        return header;
    }
    
    private HBox createResultButton(SearchResult result) {
        HBox button = new HBox();
        button.setPrefWidth(380);
        button.setMaxHeight(30);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle("-fx-background-color: transparent; " +
                       "-fx-padding: 4 8; " +
                       "-fx-cursor: hand;");
        
        // Rendre cliquable
        button.setOnMouseClicked(e -> selectResult(result));
        
        // Icône
        Label icon = new Label(result.getIcon());
        icon.setFont(Font.font("System", 14));
        icon.setStyle("-fx-text-fill: #7DD3FC;");
        
        // Nom et description
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        
        Label name = new Label(result.getName());
        name.setStyle("-fx-text-fill: #F2F2F2; -fx-font-weight: bold;");
        name.setFont(Font.font("System", FontWeight.BOLD, 11));
        
        Label description = new Label(result.getDescription());
        description.setStyle("-fx-text-fill: #7DD3FC;");
        description.setFont(Font.font("System", 9));
        
        textContent.getChildren().addAll(name, description);
        button.getChildren().addAll(icon, textContent);
        button.setSpacing(8);
        
        // Effets de survol
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: #1D2659; " +
                           "-fx-padding: 4 8; " +
                           "-fx-cursor: hand;"));
        
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: transparent; " +
                           "-fx-padding: 4 8; " +
                           "-fx-cursor: hand;"));
        
        return button;
    }
    
    private void selectResult(SearchResult result) {
        // Remplir le champ de recherche avec le nom sélectionné
        searchField.setText(result.getName());
        hideSuggestions();
        
        // TODO: Naviguer vers l'élément sélectionné
        navigateToResult(result);
    }
    
    private void navigateToResult(SearchResult result) {
        // Appeler le callback de navigation
        if (navigationCallback != null) {
            navigationCallback.navigateToResult(result);
        } else {
            System.out.println("🎯 Navigation vers: " + result.getType() + " - " + result.getName());
        }
    }
    
    private void showSuggestions() {
        if (!suggestionPopup.isShowing() && !suggestionContainer.getChildren().isEmpty()) {
            // Ajuster la hauteur du container selon le contenu
            int itemCount = suggestionContainer.getChildren().size();
            double maxHeight = Math.min(250, itemCount * 30); // ~30px par item
            suggestionContainer.setMaxHeight(maxHeight);
            
            // Utiliser une approche plus fiable pour le positionnement
            // Attendre que le layout soit terminé
            javafx.application.Platform.runLater(() -> {
                try {
                    var bounds = searchField.localToScreen(searchField.getBoundsInLocal());
                    if (bounds != null) {
                        double x = bounds.getMinX();
                        double y = bounds.getMaxY() + 3; // 3px d'espace sous le champ
                        
                        suggestionPopup.show(searchField.getScene().getWindow(), x, y);
                    }
                } catch (Exception e) {
                    // Fallback: afficher à côté du champ
                    suggestionPopup.show(searchField, 0, searchField.getHeight() + 3);
                }
            });
        }
    }
    
    private void hideSuggestions() {
        if (suggestionPopup.isShowing()) {
            suggestionPopup.hide();
        }
    }
}