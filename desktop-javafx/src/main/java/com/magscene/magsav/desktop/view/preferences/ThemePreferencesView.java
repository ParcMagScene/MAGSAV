package com.magscene.magsav.desktop.view.preferences;

import com.magscene.magsav.desktop.theme.Theme;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue de gestion des thèmes dans les préférences
 */
public class ThemePreferencesView extends VBox {
    
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private VBox themePreviewContainer;
    
    public ThemePreferencesView() {
        initializeUI();
        loadThemes();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        
        // Plus de titre ici - déjà affiché dans l'onglet; // Description
        Label descLabel = new Label("Sélectionnez un thème en cliquant directement sur l'aperçu");
        descLabel.setFont(Font.font("System", 14));
        descLabel.setTextFill(Color.web(StandardColors.NEUTRAL_GRAY));
        
        // Thème actuel
        Label currentThemeSection = createCurrentThemeSection();
        
        // Aperçu des thèmes (section principale)
        VBox previewSection = createThemePreviewSection();
        
        // Actions supplémentaires
        HBox actionsSection = createActionsSection();
        
        getChildren().addAll(
            descLabel,
            new Separator(),
            currentThemeSection,
            new Separator(), 
            previewSection,
            new Separator(),
            actionsSection
        );
    }
    
    private Label createCurrentThemeSection() {
        Theme currentTheme = themeManager.getTheme(themeManager.getCurrentTheme());
        Label currentThemeLabel = new Label("✅ Thème actuel: " + 
            (currentTheme != null ? currentTheme.getDisplayName() : "Inconnu"));
        currentThemeLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        currentThemeLabel.setTextFill(Color.web(StandardColors.SUCCESS_GREEN));
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        return currentThemeLabel;
    }
    
    private VBox createThemePreviewSection() {
        VBox section = new VBox(15);
        
        Label sectionTitle = new Label("👁️ Aperçu des Thèmes");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        themePreviewContainer = new VBox(10);
        ScrollPane scrollPane = new ScrollPane(themePreviewContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        section.getChildren().addAll(sectionTitle, scrollPane);
        return section;
    }
    
    private HBox createActionsSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER_LEFT);
        
        Button refreshButton = new Button("🔄 Actualiser");
        refreshButton.getStyleClass().add("button");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        refreshButton.setOnAction(e -> loadThemes());
        
        Button resetButton = new Button("🔄 Thème par Défaut");
        resetButton.getStyleClass().add("button");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        resetButton.setOnAction(e -> resetToDefaultTheme());
        
        Button customButton = new Button("🎨 Créer un Thème Personnalisé");
        customButton.getStyleClass().add("button");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        customButton.setOnAction(e -> createCustomTheme());
        
        section.getChildren().addAll(refreshButton, resetButton, customButton);
        return section;
    }
    
    private void loadThemes() {
        // Charge uniquement les aperçus
        loadThemePreviews();
    }
    
    private void loadThemePreviews() {
        themePreviewContainer.getChildren().clear();
        
        for (Theme theme : themeManager.getAvailableThemes()) {
            HBox previewCard = createThemePreviewCard(theme);
            themePreviewContainer.getChildren().add(previewCard);
        }
    }
    
    private HBox createThemePreviewCard(Theme theme) {
        HBox card = new HBox(15);
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        
        // Informations du thème
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(theme.getDisplayName());
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        Label descLabel = new Label(theme.getDescription());
        descLabel.setFont(Font.font("System", 12));
        descLabel.setTextFill(Color.web(StandardColors.NEUTRAL_GRAY));
        
        infoBox.getChildren().addAll(nameLabel, descLabel);
        
        // Aperçu couleurs
        HBox colorPreview = createColorPreview(theme.getId());
        
        // Bouton sélectionner
        Button selectButton = new Button("Sélectionner");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        selectButton.setOnAction(e -> applyTheme(theme));
        
        // Indicateur thème actuel
        if (theme.getId().equals(themeManager.getCurrentTheme())) {
            Label currentLabel = new Label("✅ ACTUEL");
            currentLabel.setTextFill(Color.web(StandardColors.SUCCESS_GREEN));
            currentLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
            card.getChildren().add(currentLabel);
        }
        
        card.getChildren().addAll(infoBox, colorPreview, selectButton);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        return card;
    }
    
    private HBox createColorPreview(String themeId) {
        HBox colorBox = new HBox(5);
        ThemeManager.ThemePreview preview = themeManager.getThemePreview(themeId);
        
        Region color1 = createColorSample(preview.getBackgroundColor());
        Region color2 = createColorSample(preview.getSecondaryColor());
        Region color3 = createColorSample(preview.getAccentColor());
        Region color4 = createColorSample(preview.getTextColor());
        
        colorBox.getChildren().addAll(color1, color2, color3, color4);
        return colorBox;
    }
    
    private Region createColorSample(String color) {
        Region sample = new Region();
        sample.setPrefSize(20, 20);
        sample.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 3;");
        return sample;
    }
    
    private void setupEventHandlers() {
        // Plus de gestionnaires nécessaires pour la ComboBox
    }
    
    private void applyTheme(Theme theme) {
        if (theme != null) {
            themeManager.applyTheme(theme.getId());
            
            // Actualise l'affichage
            loadThemes();
            
            // Message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thème Appliqué");
            alert.setHeaderText("Succès !");
            alert.setContentText("Le thème \"" + theme.getDisplayName() + "\" a été appliqué avec succès.");
            alert.showAndWait();
        }
    }
    
    private void resetToDefaultTheme() {
        Theme defaultTheme = themeManager.getTheme("light");
        if (defaultTheme != null) {
            applyTheme(defaultTheme);
        }
    }
    
    private void createCustomTheme() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à Venir");
        alert.setHeaderText("Création de Thèmes Personnalisés");
        alert.setContentText("La création de thèmes personnalisés sera disponible dans une prochaine version.");
        alert.showAndWait();
    }
}
