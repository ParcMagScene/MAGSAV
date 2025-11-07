package com.magscene.magsav.desktop.theme;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Application de test pour le système de thèmes MAGSAV-3.0
 * Permet de tester rapidement les différents thèmes disponibles
 */
public class ThemeTestApplication extends Application {
    
    private ThemeManager themeManager;
    private ComboBox<Theme> themeSelector;
    private VBox mainContent;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("🎨 Test du Système de Thèmes MAGSAV-3.0");
        
        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        
        // Header avec sélecteur de thème
        VBox header = createHeader();
        root.setTop(header);
        
        // Contenu de test
        mainContent = createTestContent();
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        root.setCenter(scrollPane);
        
        // Scene
        Scene scene = new Scene(root, 900, 700);
        
        // Initialisation du système de thèmes
        themeManager = ThemeManager.getInstance();
        themeManager.setScene(scene);
        themeManager.applyTheme(themeManager.getCurrentTheme());
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Charger les thèmes dans le sélecteur
        loadThemes();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("🎨 Test du Système de Thèmes MAGSAV-3.0");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox themeControls = new HBox(15);
        themeControls.setAlignment(Pos.CENTER_LEFT);
        
        Label themeLabel = new Label("Sélectionner un thème:");
        themeLabel.setStyle("-fx-font-weight: bold;");
        
        themeSelector = new ComboBox<>();
        themeSelector.setPrefWidth(200);
        themeSelector.setOnAction(e -> {
            Theme selected = themeSelector.getValue();
            if (selected != null) {
                themeManager.applyTheme(selected.getId());
                System.out.println("✓ Thème appliqué: " + selected.getDisplayName());
            }
        });
        
        Button resetButton = new Button("🔄 Défaut");
        resetButton.setOnAction(e -> {
            themeManager.applyTheme("light");
            themeSelector.setValue(themeManager.getTheme("light"));
        });
        
        themeControls.getChildren().addAll(themeLabel, themeSelector, resetButton);
        header.getChildren().addAll(title, themeControls);
        
        return header;
    }
    
    private VBox createTestContent() {
        VBox content = new VBox(20);
        
        // Section Boutons
        VBox buttonSection = new VBox(10);
        Label buttonTitle = new Label("🔘 Boutons");
        buttonTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox buttons = new HBox(10);
        Button normalButton = new Button("Bouton Normal");
        Button primaryButton = new Button("Bouton Principal");
        primaryButton.setStyle("-fx-background-color: -fx-accent;");
        Button disabledButton = new Button("Bouton Désactivé");
        disabledButton.setDisable(true);
        
        buttons.getChildren().addAll(normalButton, primaryButton, disabledButton);
        buttonSection.getChildren().addAll(buttonTitle, buttons);
        
        // Section Champs de saisie
        VBox inputSection = new VBox(10);
        Label inputTitle = new Label("📝 Champs de Saisie");
        inputTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TextField textField = new TextField("Champ de texte");
        TextArea textArea = new TextArea("Zone de texte\nmulti-lignes");
        textArea.setPrefRowCount(3);
        
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("Option 1", "Option 2", "Option 3");
        comboBox.setValue("Option 1");
        
        inputSection.getChildren().addAll(inputTitle, textField, textArea, comboBox);
        
        // Section Tableau
        VBox tableSection = new VBox(10);
        Label tableTitle = new Label("📊 Tableau");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TableView<String> tableView = new TableView<>();
        TableColumn<String, String> col1 = new TableColumn<>("Colonne 1");
        TableColumn<String, String> col2 = new TableColumn<>("Colonne 2");
        TableColumn<String, String> col3 = new TableColumn<>("Colonne 3");
        
        col1.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Donnée " + data.getValue()));
        col2.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Valeur " + data.getValue()));
        col3.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Test " + data.getValue()));
        
        tableView.getColumns().addAll(col1, col2, col3);
        tableView.getItems().addAll("1", "2", "3", "4", "5");
        tableView.setPrefHeight(150);
        
        tableSection.getChildren().addAll(tableTitle, tableView);
        
        // Section Onglets
        VBox tabSection = new VBox(10);
        Label tabTitle = new Label("📂 Onglets");
        tabTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TabPane tabPane = new TabPane();
        Tab tab1 = new Tab("Onglet 1", new Label("Contenu de l'onglet 1"));
        Tab tab2 = new Tab("Onglet 2", new Label("Contenu de l'onglet 2"));
        Tab tab3 = new Tab("Onglet 3", new Label("Contenu de l'onglet 3"));
        
        tabPane.getTabs().addAll(tab1, tab2, tab3);
        tabPane.setPrefHeight(100);
        
        tabSection.getChildren().addAll(tabTitle, tabPane);
        
        // Section Progress
        VBox progressSection = new VBox(10);
        Label progressTitle = new Label("📈 Barres de Progression");
        progressTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        ProgressBar progressBar = new ProgressBar(0.7);
        ProgressIndicator progressIndicator = new ProgressIndicator(0.5);
        
        HBox progressBox = new HBox(20);
        progressBox.getChildren().addAll(progressBar, progressIndicator);
        
        progressSection.getChildren().addAll(progressTitle, progressBox);
        
        // Messages d'état
        VBox messageSection = new VBox(10);
        Label messageTitle = new Label("💬 Messages d'État");
        messageTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label successMsg = new Label("✅ Message de succès");
        successMsg.getStyleClass().add("success-message");
        successMsg.setStyle("-fx-padding: 10; -fx-background-color: #d4edda; -fx-text-fill: #155724;");
        
        Label warningMsg = new Label("⚠️ Message d'avertissement");
        warningMsg.getStyleClass().add("warning-message");
        warningMsg.setStyle("-fx-padding: 10; -fx-background-color: #fff3cd; -fx-text-fill: #856404;");
        
        Label errorMsg = new Label("❌ Message d'erreur");
        errorMsg.getStyleClass().add("error-message");
        errorMsg.setStyle("-fx-padding: 10; -fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
        
        messageSection.getChildren().addAll(messageTitle, successMsg, warningMsg, errorMsg);
        
        content.getChildren().addAll(
            buttonSection,
            new Separator(),
            inputSection,
            new Separator(),
            tableSection,
            new Separator(),
            tabSection,
            new Separator(),
            progressSection,
            new Separator(),
            messageSection
        );
        
        return content;
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
    }
    
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage du test du système de thèmes MAGSAV-3.0...");
        launch(args);
    }
}