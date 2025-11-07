package com.magscene.magsav.desktop.view.preferences;

import com.magscene.magsav.desktop.theme.Theme;
import com.magscene.magsav.desktop.theme.ThemeManager;
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
    private ComboBox<Theme> themeSelector;
    private VBox themePreviewContainer;
    
    public ThemePreferencesView() {
        initializeUI();
        loadThemes();
        setupEventHandlers();
    }
    
    private void initializeUI() {
        setSpacing(20);
        setPadding(new Insets(20));
        
        // Titre
        Label titleLabel = new Label("🎨 Gestion des Thèmes");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        // Description
        Label descLabel = new Label("Personnalisez l'apparence de MAGSAV-3.0 selon vos préférences");
        descLabel.setFont(Font.font("System", 14));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        // Sélecteur de thème
        VBox selectorSection = createThemeSelectorSection();
        
        // Aperçu des thèmes
        VBox previewSection = createThemePreviewSection();
        
        // Actions
        HBox actionsSection = createActionsSection();
        
        getChildren().addAll(
            titleLabel,
            descLabel,
            new Separator(),
            selectorSection,
            new Separator(),
            previewSection,
            new Separator(),
            actionsSection
        );
    }
    
    private VBox createThemeSelectorSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("📋 Sélection du Thème");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        themeSelector = new ComboBox<>();
        themeSelector.setPrefWidth(300);
        themeSelector.setPromptText("Choisissez un thème...");
        
        Label currentThemeLabel = new Label("Thème actuel: " + 
            themeManager.getTheme(themeManager.getCurrentTheme()).getDisplayName());
        currentThemeLabel.setFont(Font.font("System", 12));
        currentThemeLabel.setTextFill(Color.web("#27ae60"));
        
        section.getChildren().addAll(sectionTitle, themeSelector, currentThemeLabel);
        return section;
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
        
        Button applyButton = new Button("✅ Appliquer le Thème");
        applyButton.getStyleClass().add("button");
        applyButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        applyButton.setOnAction(e -> applySelectedTheme());
        
        Button resetButton = new Button("🔄 Thème par Défaut");
        resetButton.getStyleClass().add("button");
        resetButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        resetButton.setOnAction(e -> resetToDefaultTheme());
        
        Button customButton = new Button("🎨 Créer un Thème Personnalisé");
        customButton.getStyleClass().add("button");
        customButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        customButton.setOnAction(e -> createCustomTheme());
        
        section.getChildren().addAll(applyButton, resetButton, customButton);
        return section;
    }
    
    private void loadThemes() {
        themeSelector.getItems().clear();
        themeSelector.getItems().addAll(themeManager.getAvailableThemes());
        
        // Sélectionne le thème actuel
        String currentThemeId = themeManager.getCurrentTheme();
        Theme currentTheme = themeManager.getTheme(currentThemeId);
        if (currentTheme != null) {
            themeSelector.setValue(currentTheme);
        }
        
        // Charge les aperçus
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
        descLabel.setTextFill(Color.web("#7f8c8d"));
        
        infoBox.getChildren().addAll(nameLabel, descLabel);
        
        // Aperçu couleurs
        HBox colorPreview = createColorPreview(theme.getId());
        
        // Bouton sélectionner
        Button selectButton = new Button("Sélectionner");
        selectButton.setOnAction(e -> {
            themeSelector.setValue(theme);
            applySelectedTheme();
        });
        
        // Indicateur thème actuel
        if (theme.getId().equals(themeManager.getCurrentTheme())) {
            Label currentLabel = new Label("✅ ACTUEL");
            currentLabel.setTextFill(Color.web("#27ae60"));
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
        themeSelector.setOnAction(e -> {
            // Aperçu en temps réel optionnel
            // Theme selected = themeSelector.getValue();
            // if (selected != null) {
            //     themeManager.applyTheme(selected.getId());
            // }
        });
    }
    
    private void applySelectedTheme() {
        Theme selectedTheme = themeSelector.getValue();
        if (selectedTheme != null) {
            themeManager.applyTheme(selectedTheme.getId());
            
            // Actualise l'affichage
            loadThemes();
            
            // Message de confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thème Appliqué");
            alert.setHeaderText("Succès !");
            alert.setContentText("Le thème \"" + selectedTheme.getDisplayName() + "\" a été appliqué avec succès.");
            alert.showAndWait();
        }
    }
    
    private void resetToDefaultTheme() {
        themeManager.applyTheme("light");
        themeSelector.setValue(themeManager.getTheme("light"));
        loadThemes();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thème Réinitialisé");
        alert.setHeaderText("Succès !");
        alert.setContentText("Le thème par défaut a été restauré.");
        alert.showAndWait();
    }
    
    private void createCustomTheme() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité à Venir");
        alert.setHeaderText("Création de Thèmes Personnalisés");
        alert.setContentText("La création de thèmes personnalisés sera disponible dans une prochaine version.");
        alert.showAndWait();
    }
}
