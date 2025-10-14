package com.magsav.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class AppearanceController implements Initializable {
    
    @FXML private ComboBox<String> themeSelector;
    @FXML private ColorPicker sidebarColorPicker;
    @FXML private ColorPicker backgroundColorPicker;
    @FXML private ColorPicker tabColorPicker;
    @FXML private ColorPicker accentColorPicker;
    @FXML private ColorPicker textColorPicker;
    
    @FXML private Label sidebarColorLabel;
    @FXML private Label backgroundColorLabel;
    @FXML private Label tabColorLabel;
    @FXML private Label accentColorLabel;
    @FXML private Label textColorLabel;
    
    @FXML private VBox previewPane;
    @FXML private Button applyButton;
    @FXML private Button resetButton;
    @FXML private Button savePresetButton;
    
    @FXML private ListView<String> presetsList;
    @FXML private Button loadPresetButton;
    @FXML private Button deletePresetButton;
    
    private Preferences prefs;
    // private ThemeManager themeManager; // TODO: Réimplémenter avec util.ThemeManager
    
    // Couleurs par défaut du thème dark authentique
    private final Color DEFAULT_SIDEBAR_COLOR = Color.web("#1e3a5f");
    private final Color DEFAULT_BACKGROUND_COLOR = Color.web("#000000");
    private final Color DEFAULT_TAB_COLOR = Color.web("#1a1a1a");
    private final Color DEFAULT_ACCENT_COLOR = Color.web("#4a90e2");
    private final Color DEFAULT_TEXT_COLOR = Color.web("#ffffff");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prefs = Preferences.userNodeForPackage(AppearanceController.class);
        // themeManager = ThemeManager.getInstance(); // TODO: Réimplémenter
        
        setupColorPickers();
        setupThemeSelector();
        setupPresetsList();
        setupEventHandlers();
        loadCurrentTheme();
    }
    
    private void setupColorPickers() {
        // Configurer les ColorPickers avec les couleurs actuelles
        sidebarColorPicker.setValue(DEFAULT_SIDEBAR_COLOR);
        backgroundColorPicker.setValue(DEFAULT_BACKGROUND_COLOR);
        tabColorPicker.setValue(DEFAULT_TAB_COLOR);
        accentColorPicker.setValue(DEFAULT_ACCENT_COLOR);
        textColorPicker.setValue(DEFAULT_TEXT_COLOR);
        
        // Ajouter des listeners pour mettre à jour les labels
        sidebarColorPicker.valueProperty().addListener(this::onColorChanged);
        backgroundColorPicker.valueProperty().addListener(this::onColorChanged);
        tabColorPicker.valueProperty().addListener(this::onColorChanged);
        accentColorPicker.valueProperty().addListener(this::onColorChanged);
        textColorPicker.valueProperty().addListener(this::onColorChanged);
    }
    
    private void setupThemeSelector() {
        themeSelector.setValue("Dark authentique");
        themeSelector.valueProperty().addListener(this::onThemeSelectionChanged);
    }
    
    private void setupPresetsList() {
        ObservableList<String> presets = FXCollections.observableArrayList(
            "Dark authentique (défaut)",
            "Bleu nuit",
            "Violet moderne"
        );
        presetsList.setItems(presets);
    }
    
    private void setupEventHandlers() {
        applyButton.setOnAction(e -> applyThemeChanges());
        resetButton.setOnAction(e -> resetToDefaults());
        savePresetButton.setOnAction(e -> saveCurrentAsPreset());
        loadPresetButton.setOnAction(e -> loadSelectedPreset());
        deletePresetButton.setOnAction(e -> deleteSelectedPreset());
    }
    
    private void onColorChanged(ObservableValue<? extends Color> obs, Color oldColor, Color newColor) {
        updateColorLabels();
        updatePreview();
        // Application automatique en temps réel
        applyThemeChangesInstantly();
    }
    
    private void onThemeSelectionChanged(ObservableValue<? extends String> obs, String oldTheme, String newTheme) {
        switch (newTheme) {
            case "Dark authentique":
                loadDarkTheme();
                break;
            case "Clair":
                loadLightTheme();
                break;
            case "Personnalisé":
                // Ne rien faire, laisser l'utilisateur configurer
                break;
        }
        updatePreview();
    }
    
    private void updateColorLabels() {
        sidebarColorLabel.setText(colorToHex(sidebarColorPicker.getValue()));
        backgroundColorLabel.setText(colorToHex(backgroundColorPicker.getValue()));
        tabColorLabel.setText(colorToHex(tabColorPicker.getValue()));
        accentColorLabel.setText(colorToHex(accentColorPicker.getValue()));
        textColorLabel.setText(colorToHex(textColorPicker.getValue()));
    }
    
    private void updatePreview() {
        // Mettre à jour la prévisualisation avec les nouvelles couleurs
        String sidebarColor = colorToHex(sidebarColorPicker.getValue());
        String backgroundColor = colorToHex(backgroundColorPicker.getValue());
        String tabColor = colorToHex(tabColorPicker.getValue());
        String accentColor = colorToHex(accentColorPicker.getValue());
        String textColor = colorToHex(textColorPicker.getValue());
        
        String previewStyle = String.format(
            "-fx-background-color: %s; -fx-border-color: %s; -fx-border-width: 1px;",
            backgroundColor, accentColor
        );
        
        previewPane.setStyle(previewStyle);
        
        // Mettre à jour les styles des éléments de prévisualisation
        previewPane.lookupAll(".preview-sidebar").forEach(node -> {
            node.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: %s;", 
                sidebarColor, textColor));
        });
        
        previewPane.lookupAll(".preview-content").forEach(node -> {
            node.setStyle(String.format("-fx-background-color: %s;", tabColor));
        });
        
        previewPane.lookupAll(".preview-text").forEach(node -> {
            node.setStyle(String.format("-fx-text-fill: %s;", textColor));
        });
    }
    
    private void loadDarkTheme() {
        sidebarColorPicker.setValue(DEFAULT_SIDEBAR_COLOR);
        backgroundColorPicker.setValue(DEFAULT_BACKGROUND_COLOR);
        tabColorPicker.setValue(DEFAULT_TAB_COLOR);
        accentColorPicker.setValue(DEFAULT_ACCENT_COLOR);
        textColorPicker.setValue(DEFAULT_TEXT_COLOR);
        updateColorLabels();
    }
    
    private void loadLightTheme() {
        sidebarColorPicker.setValue(Color.web("#f0f0f0"));
        backgroundColorPicker.setValue(Color.web("#ffffff"));
        tabColorPicker.setValue(Color.web("#fafafa"));
        accentColorPicker.setValue(Color.web("#0078d4"));
        textColorPicker.setValue(Color.web("#000000"));
        updateColorLabels();
    }
    
    private void applyThemeChanges() {
        applyThemeChangesInstantly();
        
        // Sauvegarder les préférences
        saveThemePreferences();
        
        // Afficher une confirmation
        showConfirmation("Thème appliqué avec succès!");
    }
    
    private void applyThemeChangesInstantly() {
        // Créer un nouveau thème personnalisé avec les couleurs sélectionnées
        new CustomTheme(
            colorToHex(sidebarColorPicker.getValue()),
            colorToHex(backgroundColorPicker.getValue()),
            colorToHex(tabColorPicker.getValue()),
            colorToHex(accentColorPicker.getValue()),
            colorToHex(textColorPicker.getValue())
        );
        
        // TODO: Appliquer le thème via le ThemeManager instantanément
        // themeManager.applyCustomTheme(customTheme);
    }
    
    private void resetToDefaults() {
        loadDarkTheme();
        themeSelector.setValue("Dark authentique");
        updatePreview();
    }
    
    private void saveCurrentAsPreset() {
        TextInputDialog dialog = new TextInputDialog("Mon thème personnalisé");
        dialog.setTitle("Sauvegarder le preset");
        dialog.setHeaderText("Donner un nom à ce preset");
        dialog.setContentText("Nom du preset:");
        
        dialog.showAndWait().ifPresent(name -> {
            // Sauvegarder le preset
            savePreset(name);
            // Ajouter à la liste
            presetsList.getItems().add(name);
            showConfirmation("Preset '" + name + "' sauvegardé!");
        });
    }
    
    private void loadSelectedPreset() {
        String selectedPreset = presetsList.getSelectionModel().getSelectedItem();
        if (selectedPreset != null) {
            loadPreset(selectedPreset);
            updateColorLabels();
            updatePreview();
            showConfirmation("Preset '" + selectedPreset + "' chargé!");
        }
    }
    
    private void deleteSelectedPreset() {
        String selectedPreset = presetsList.getSelectionModel().getSelectedItem();
        if (selectedPreset != null && !selectedPreset.contains("(défaut)")) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer le preset");
            alert.setHeaderText("Êtes-vous sûr de vouloir supprimer ce preset?");
            alert.setContentText("Preset: " + selectedPreset);
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deletePreset(selectedPreset);
                    presetsList.getItems().remove(selectedPreset);
                    showConfirmation("Preset supprimé!");
                }
            });
        }
    }
    
    private void loadCurrentTheme() {
        // Charger les couleurs sauvegardées depuis les préférences
        String sidebarColor = prefs.get("sidebar_color", "#1e3a5f");
        String backgroundColor = prefs.get("background_color", "#000000");
        String tabColor = prefs.get("tab_color", "#1a1a1a");
        String accentColor = prefs.get("accent_color", "#4a90e2");
        String textColor = prefs.get("text_color", "#ffffff");
        
        sidebarColorPicker.setValue(Color.web(sidebarColor));
        backgroundColorPicker.setValue(Color.web(backgroundColor));
        tabColorPicker.setValue(Color.web(tabColor));
        accentColorPicker.setValue(Color.web(accentColor));
        textColorPicker.setValue(Color.web(textColor));
        
        updateColorLabels();
        updatePreview();
    }
    
    private void saveThemePreferences() {
        prefs.put("sidebar_color", colorToHex(sidebarColorPicker.getValue()));
        prefs.put("background_color", colorToHex(backgroundColorPicker.getValue()));
        prefs.put("tab_color", colorToHex(tabColorPicker.getValue()));
        prefs.put("accent_color", colorToHex(accentColorPicker.getValue()));
        prefs.put("text_color", colorToHex(textColorPicker.getValue()));
    }
    
    private void savePreset(String name) {
        prefs.put("preset_" + name + "_sidebar", colorToHex(sidebarColorPicker.getValue()));
        prefs.put("preset_" + name + "_background", colorToHex(backgroundColorPicker.getValue()));
        prefs.put("preset_" + name + "_tab", colorToHex(tabColorPicker.getValue()));
        prefs.put("preset_" + name + "_accent", colorToHex(accentColorPicker.getValue()));
        prefs.put("preset_" + name + "_text", colorToHex(textColorPicker.getValue()));
    }
    
    private void loadPreset(String name) {
        String presetName = name.replace(" (défaut)", "");
        
        if (presetName.equals("Dark authentique")) {
            loadDarkTheme();
        } else if (presetName.equals("Bleu nuit")) {
            sidebarColorPicker.setValue(Color.web("#1a237e"));
            backgroundColorPicker.setValue(Color.web("#0d1421"));
            tabColorPicker.setValue(Color.web("#1e2732"));
            accentColorPicker.setValue(Color.web("#3f51b5"));
            textColorPicker.setValue(Color.web("#e8eaf6"));
        } else if (presetName.equals("Violet moderne")) {
            sidebarColorPicker.setValue(Color.web("#4a148c"));
            backgroundColorPicker.setValue(Color.web("#1a0033"));
            tabColorPicker.setValue(Color.web("#2e1a47"));
            accentColorPicker.setValue(Color.web("#9c27b0"));
            textColorPicker.setValue(Color.web("#f3e5f5"));
        } else {
            // Charger un preset personnalisé
            String sidebarColor = prefs.get("preset_" + presetName + "_sidebar", "#1e3a5f");
            String backgroundColor = prefs.get("preset_" + presetName + "_background", "#000000");
            String tabColor = prefs.get("preset_" + presetName + "_tab", "#1a1a1a");
            String accentColor = prefs.get("preset_" + presetName + "_accent", "#4a90e2");
            String textColor = prefs.get("preset_" + presetName + "_text", "#ffffff");
            
            sidebarColorPicker.setValue(Color.web(sidebarColor));
            backgroundColorPicker.setValue(Color.web(backgroundColor));
            tabColorPicker.setValue(Color.web(tabColor));
            accentColorPicker.setValue(Color.web(accentColor));
            textColorPicker.setValue(Color.web(textColor));
        }
    }
    
    private void deletePreset(String name) {
        String presetName = name.replace(" (défaut)", "");
        prefs.remove("preset_" + presetName + "_sidebar");
        prefs.remove("preset_" + presetName + "_background");
        prefs.remove("preset_" + presetName + "_tab");
        prefs.remove("preset_" + presetName + "_accent");
        prefs.remove("preset_" + presetName + "_text");
    }
    
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }
    
    private void showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Classes auxiliaires
    public static class CustomTheme {
        private final String sidebarColor;
        private final String backgroundColor;
        private final String tabColor;
        private final String accentColor;
        private final String textColor;
        
        public CustomTheme(String sidebarColor, String backgroundColor, String tabColor, 
                          String accentColor, String textColor) {
            this.sidebarColor = sidebarColor;
            this.backgroundColor = backgroundColor;
            this.tabColor = tabColor;
            this.accentColor = accentColor;
            this.textColor = textColor;
        }
        
        // Getters
        public String getSidebarColor() { return sidebarColor; }
        public String getBackgroundColor() { return backgroundColor; }
        public String getTabColor() { return tabColor; }
        public String getAccentColor() { return accentColor; }
        public String getTextColor() { return textColor; }
    }
}