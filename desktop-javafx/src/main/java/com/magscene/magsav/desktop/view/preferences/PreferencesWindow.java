package com.magscene.magsav.desktop.view.preferences;

import com.magscene.magsav.desktop.config.EquipmentPreferencesManager;
import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.theme.ThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Fenêtre principale des préférences de l'application
 */
public class PreferencesWindow extends Stage {

    private TabPane tabPane;
    private ThemePreferencesView themePreferencesView;

    public PreferencesWindow() {
        initializeWindow();
        initializeUI();
    }

    private void initializeWindow() {
        setTitle("⚙️ Préférences MAGSAV-3.0");
        setWidth(800);
        setHeight(600);
        setMinWidth(700);
        setMinHeight(500);
        initModality(Modality.APPLICATION_MODAL);

        // Mémoriser la taille et position
        WindowPreferencesService.getInstance().restoreWindowBounds(this, "preferences-window", 800, 600);
        WindowPreferencesService.getInstance().setupAutoSave(this, "preferences-window");

        centerOnScreen();
    }

    private void initializeUI() {
        BorderPane root = new BorderPane();

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Content avec onglets
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Onglet Thèmes
        Tab themeTab = new Tab("🎨 Thèmes");
        themePreferencesView = new ThemePreferencesView();
        ScrollPane themeScrollPane = new ScrollPane(themePreferencesView);
        themeScrollPane.setFitToWidth(true);
        themeScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        themeTab.setContent(themeScrollPane);

        // Onglet Général (placeholder)
        Tab generalTab = new Tab("⚙️ Général");
        generalTab.setContent(createGeneralPreferences());

        // Onglet Interface (placeholder)
        Tab interfaceTab = new Tab("🖥️ Interface");
        interfaceTab.setContent(createInterfacePreferences());

        // Onglet Sauvegardes (placeholder)
        Tab backupTab = new Tab("💾 Sauvegardes");
        backupTab.setContent(createBackupPreferences());

        tabPane.getTabs().addAll(themeTab, generalTab, interfaceTab, backupTab);
        root.setCenter(tabPane);

        // Footer avec boutons
        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS

        Label titleLabel = new Label("Préférences MAGSAV-3.0");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Personnalisez votre expérience utilisateur");
        subtitleLabel.setFont(Font.font("System", 14));
        subtitleLabel.setTextFill(Color.web(StandardColors.NEUTRAL_LIGHT));

        header.getChildren().addAll(titleLabel, subtitleLabel);
        return header;
    }

    private VBox createGeneralPreferences() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("⚙️ Paramètres Généraux");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Langue
        VBox languageSection = new VBox(10);
        Label langLabel = new Label("🌐 Langue de l'interface");
        langLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Français", "English", "Español");
        languageCombo.setValue("Français");
        languageCombo.setPrefWidth(200);
        languageSection.getChildren().addAll(langLabel, languageCombo);

        // Démarrage
        VBox startupSection = new VBox(10);
        Label startupLabel = new Label("🚀 Options de démarrage");
        startupLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        CheckBox autoStartCheckbox = new CheckBox("Démarrer MAGSAV-3.0 au lancement du système");
        CheckBox rememberWindowCheckbox = new CheckBox("Mémoriser la taille et position des fenêtres");
        CheckBox showSplashCheckbox = new CheckBox("Afficher l'écran de démarrage");
        startupSection.getChildren().addAll(startupLabel, autoStartCheckbox, rememberWindowCheckbox,
                showSplashCheckbox);

        content.getChildren().addAll(titleLabel, new Separator(), languageSection, new Separator(), startupSection);
        return content;
    }

    private VBox createInterfacePreferences() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("🖥️ Configuration de l'Interface");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Affichage
        VBox displaySection = new VBox(10);
        Label displayLabel = new Label("📊 Options d'affichage");
        displayLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        CheckBox animationsCheckbox = new CheckBox("Activer les animations d'interface");
        animationsCheckbox.setSelected(true);

        CheckBox tooltipsCheckbox = new CheckBox("Afficher les info-bulles");
        tooltipsCheckbox.setSelected(true);

        CheckBox statusBarCheckbox = new CheckBox("Afficher la barre de statut");
        statusBarCheckbox.setSelected(true);

        displaySection.getChildren().addAll(displayLabel, animationsCheckbox, tooltipsCheckbox, statusBarCheckbox);

        // Taille de police
        VBox fontSection = new VBox(10);
        Label fontLabel = new Label("🔤 Taille de la police");
        fontLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox fontBox = new HBox(10);
        fontBox.setAlignment(Pos.CENTER_LEFT);
        Label fontSizeLabel = new Label("Taille:");
        Slider fontSizeSlider = new Slider(10, 20, 12);
        fontSizeSlider.setShowTickLabels(true);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setMajorTickUnit(2);
        fontSizeSlider.setPrefWidth(200);
        Label fontValueLabel = new Label("12pt");
        fontSizeSlider.valueProperty().addListener(
                (obs, oldVal, newVal) -> fontValueLabel.setText(String.format("%.0fpt", newVal.doubleValue())));

        fontBox.getChildren().addAll(fontSizeLabel, fontSizeSlider, fontValueLabel);
        fontSection.getChildren().addAll(fontLabel, fontBox);

        // Section Parc Matériel
        VBox equipmentSection = new VBox(10);
        Label equipmentLabel = new Label("🏭 Parc Matériel");
        equipmentLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        EquipmentPreferencesManager equipmentPrefs = EquipmentPreferencesManager.getInstance();
        
        CheckBox showAllOwnersCheckbox = new CheckBox("Afficher les équipements de tous les propriétaires");
        showAllOwnersCheckbox.setSelected(equipmentPrefs.isShowAllOwners());
        showAllOwnersCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            equipmentPrefs.setShowAllOwners(newVal);
        });
        
        Label ownerHint = new Label("    Par défaut, seuls les équipements MAG SCENE sont affichés");
        ownerHint.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        
        equipmentSection.getChildren().addAll(equipmentLabel, showAllOwnersCheckbox, ownerHint);

        content.getChildren().addAll(titleLabel, new Separator(), displaySection, new Separator(), fontSection, new Separator(), equipmentSection);
        return content;
    }

    private VBox createBackupPreferences() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("💾 Gestion des Sauvegardes");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Sauvegarde automatique
        VBox autoBackupSection = new VBox(10);
        Label autoBackupLabel = new Label("⏰ Sauvegarde automatique");
        autoBackupLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        CheckBox autoBackupCheckbox = new CheckBox("Activer la sauvegarde automatique");
        autoBackupCheckbox.setSelected(true);

        HBox frequencyBox = new HBox(10);
        frequencyBox.setAlignment(Pos.CENTER_LEFT);
        Label frequencyLabel = new Label("Fréquence:");
        ComboBox<String> frequencyCombo = new ComboBox<>();
        frequencyCombo.getItems().addAll("Toutes les heures", "Toutes les 6 heures",
                "Quotidienne", "Hebdomadaire");
        frequencyCombo.setValue("Quotidienne");
        frequencyCombo.setPrefWidth(150);

        frequencyBox.getChildren().addAll(frequencyLabel, frequencyCombo);
        autoBackupSection.getChildren().addAll(autoBackupLabel, autoBackupCheckbox, frequencyBox);

        // Emplacement
        VBox locationSection = new VBox(10);
        Label locationLabel = new Label("📁 Emplacement des sauvegardes");
        locationLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox pathBox = new HBox(10);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        TextField pathField = new TextField();
        pathField.setText(System.getProperty("user.home") + "/MAGSAV-Backups");
        pathField.setPrefWidth(300);
        Button browseButton = new Button("📂 Parcourir");

        pathBox.getChildren().addAll(pathField, browseButton);
        locationSection.getChildren().addAll(locationLabel, pathBox);

        content.getChildren().addAll(titleLabel, new Separator(), autoBackupSection, new Separator(), locationSection);
        return content;
    }

    private HBox createFooter() {
        HBox footer = new HBox(15);
        footer.setPadding(new Insets(20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor()
                + "; -fx-border-color: " + StandardColors.getBorderColor() + "; -fx-border-width: 1 0 0 0;");

        Button cancelButton = new Button("❌ Annuler");
        cancelButton.setPrefWidth(120);
        cancelButton.setOnAction(e -> close());

        Button applyButton = new Button("✅ Appliquer");
        applyButton.setPrefWidth(120);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        applyButton.setOnAction(e -> applyPreferences());

        Button okButton = new Button("✔️ OK");
        okButton.setPrefWidth(120);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        okButton.setOnAction(e -> {
            applyPreferences();
            close();
        });

        footer.getChildren().addAll(cancelButton, applyButton, okButton);
        return footer;
    }

    private void applyPreferences() {
        // Les préférences des thèmes sont appliquées directement; // Ici on pourrait
        // ajouter d'autres applications de préférences

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Préférences");
        alert.setHeaderText("Succès !");
        alert.setContentText("Les préférences ont été sauvegardées avec succès.");
        alert.showAndWait();
    }

    /**
     * Méthode statique pour ouvrir la fenêtre des préférences
     */
    public static void showPreferences() {
        PreferencesWindow window = new PreferencesWindow();
        window.showAndWait();
    }
}
