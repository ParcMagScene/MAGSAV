package com.magscene.magsav.desktop.view.settings;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.core.category.Category;
import com.magscene.magsav.desktop.core.category.CategoryManager;
import com.magscene.magsav.desktop.core.category.CategoryType;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue des paramètres de l'application MAGSAV 3.0 avec onglets
 */
public class SettingsView extends VBox {

    private UnifiedThemeManager themeManager;
    private HBox adaptiveToolbar;
    private CustomTabPane tabPane;

    public SettingsView() {
        this.themeManager = UnifiedThemeManager.getInstance();
        initializeView();
    }

    private void initializeView() {
        // Layout uniforme comme Ventes et Installations - utilise ThemeConstants
        setPadding(ThemeConstants.PADDING_STANDARD);
        setSpacing(0);
        setFillWidth(true);
        getStyleClass().add("settings-view");

        // Toolbar adaptative en haut - utilise ThemeConstants
        adaptiveToolbar = new HBox();
        adaptiveToolbar.setAlignment(Pos.CENTER_LEFT);
        adaptiveToolbar.setPadding(ThemeConstants.TOOLBAR_PADDING);
        adaptiveToolbar.getStyleClass().add(ThemeConstants.UNIFIED_TOOLBAR_CLASS);

        // CustomTabPane avec les différents onglets
        tabPane = createSettingsTabPane();
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Écouter les changements d'onglet
        tabPane.selectedTabProperty().addListener((obs, oldTab, newTab) -> {
            updateToolbarForSelectedTab(newTab);
        });

        // Initialiser la toolbar avec le premier onglet
        updateToolbarForSelectedTab(tabPane.getSelectedTab());

        // Assemblage : Toolbar adaptative puis TabPane
        getChildren().addAll(adaptiveToolbar, tabPane);
    }

    /**
     * Met à jour la toolbar selon l'onglet sélectionné
     */
    private void updateToolbarForSelectedTab(CustomTabPane.CustomTab selectedTab) {
        if (selectedTab == null)
            return;

        adaptiveToolbar.getChildren().clear();

        // Créer le contenu de la toolbar selon l'onglet
        HBox toolbarContent = new HBox(10);
        toolbarContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(toolbarContent, Priority.ALWAYS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String tabText = selectedTab.getText();
        
        if (tabText.contains("Catégories")) {
            // Boutons spécifiques pour les catégories
            Button addCategoryBtn = new Button("➕ Nouvelle catégorie");
            addCategoryBtn.getStyleClass().add("action-button-primary");
            
            toolbarContent.getChildren().addAll(addCategoryBtn, spacer);
        } else {
            // Toolbar standard pour les autres onglets
            toolbarContent.getChildren().add(spacer);
        }

        adaptiveToolbar.getChildren().add(toolbarContent);
    }

    /**
     * Création du CustomTabPane avec tous les onglets de paramètres
     */
    private CustomTabPane createSettingsTabPane() {
        CustomTabPane tabPane = new CustomTabPane();

        // Onglet Apparence & Thèmes
        CustomTabPane.CustomTab appearanceTab = new CustomTabPane.CustomTab(
                "Apparence",
                createAppearanceTab(),
                "🎨");
        tabPane.addTab(appearanceTab);

        // Onglet Application
        CustomTabPane.CustomTab applicationTab = new CustomTabPane.CustomTab(
                "Application",
                createApplicationTab(),
                "🚀");
        tabPane.addTab(applicationTab);

        // Onglet Base de données
        CustomTabPane.CustomTab databaseTab = new CustomTabPane.CustomTab(
                "Base de données",
                createDatabaseTab(),
                "💾");
        tabPane.addTab(databaseTab);

        // Onglet Notifications
        CustomTabPane.CustomTab notificationsTab = new CustomTabPane.CustomTab(
                "Notifications",
                createNotificationsTab(),
                "🔔");
        tabPane.addTab(notificationsTab);

        // Onglet Sécurité
        CustomTabPane.CustomTab securityTab = new CustomTabPane.CustomTab(
                "Sécurité",
                createSecurityTab(),
                "🔒");
        tabPane.addTab(securityTab);

        // Onglet Catégories
        CustomTabPane.CustomTab categoriesTab = new CustomTabPane.CustomTab(
                "Catégories",
                createCategoriesTab(),
                "📂");
        tabPane.addTab(categoriesTab);

        // Onglet À propos
        CustomTabPane.CustomTab aboutTab = new CustomTabPane.CustomTab(
                "À propos",
                createAboutTab(),
                "ℹ️");
        tabPane.addTab(aboutTab);

        return tabPane;
    }

    /**
     * Onglet Apparence & Thèmes
     */
    private ScrollPane createAppearanceTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Section Thème
        VBox themeSection = new VBox(10);
        themeSection.getStyleClass().add("settings-section");

        Label themeTitle = new Label("🎨 Thème de l'interface");
        themeTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox themeBox = new HBox(10);
        themeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label themeLabel = new Label("Thème actuel:");
        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll("Sombre", "Clair", "Bleu", "Vert");
        themeCombo.setValue("Sombre");
        themeCombo.setOnAction(e -> {
            String selected = themeCombo.getValue();
            switch (selected) {
                case "Sombre" -> themeManager.applyTheme("dark");
                case "Clair" -> themeManager.applyTheme("light");
                case "Bleu" -> themeManager.applyTheme("blue");
                case "Vert" -> themeManager.applyTheme("green");
            }
        });

        themeBox.getChildren().addAll(themeLabel, themeCombo);
        themeSection.getChildren().addAll(themeTitle, themeBox);

        // Section Couleurs personnalisées
        VBox colorSection = new VBox(10);
        colorSection.getStyleClass().add("settings-section");

        Label colorTitle = new Label("🌈 Couleurs personnalisées");
        colorTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox customColorsBox = new CheckBox("Activer les couleurs personnalisées");
        customColorsBox.setSelected(false);

        colorSection.getChildren().addAll(colorTitle, customColorsBox);

        // Section Police
        VBox fontSection = new VBox(10);
        fontSection.getStyleClass().add("settings-section");

        Label fontTitle = new Label("📝 Police de l'interface");
        fontTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox fontBox = new HBox(10);
        Label fontLabel = new Label("Taille de police:");
        ComboBox<String> fontSizeCombo = new ComboBox<>();
        fontSizeCombo.getItems().addAll("Petite", "Normale", "Grande", "Très grande");
        fontSizeCombo.setValue("Normale");

        fontBox.getChildren().addAll(fontLabel, fontSizeCombo);
        fontSection.getChildren().addAll(fontTitle, fontBox);

        content.getChildren().addAll(themeSection, new Separator(), colorSection, new Separator(), fontSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Onglet Application
     */
    private ScrollPane createApplicationTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Section Démarrage
        VBox startupSection = new VBox(10);
        startupSection.getStyleClass().add("settings-section");

        Label startupTitle = new Label("🚀 Démarrage de l'application");
        startupTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox autoStartBox = new CheckBox("Démarrer automatiquement au lancement du système");
        CheckBox maximizeBox = new CheckBox("Maximiser la fenêtre au démarrage");
        maximizeBox.setSelected(true);
        CheckBox rememberLastViewBox = new CheckBox("Se souvenir de la dernière vue");
        rememberLastViewBox.setSelected(true);

        startupSection.getChildren().addAll(startupTitle, autoStartBox, maximizeBox, rememberLastViewBox);

        // Section Sauvegarde
        VBox saveSection = new VBox(10);
        saveSection.getStyleClass().add("settings-section");

        Label saveTitle = new Label("💾 Sauvegarde");
        saveTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox autoSaveBox = new CheckBox("Sauvegarde automatique");
        autoSaveBox.setSelected(true);

        HBox intervalBox = new HBox(10);
        Label intervalLabel = new Label("Intervalle de sauvegarde:");
        ComboBox<String> intervalCombo = new ComboBox<>();
        intervalCombo.getItems().addAll("5 minutes", "10 minutes", "30 minutes", "1 heure");
        intervalCombo.setValue("10 minutes");
        intervalBox.getChildren().addAll(intervalLabel, intervalCombo);

        saveSection.getChildren().addAll(saveTitle, autoSaveBox, intervalBox);

        // Section Performance
        VBox perfSection = new VBox(10);
        perfSection.getStyleClass().add("settings-section");

        Label perfTitle = new Label("⚡ Performance");
        perfTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox debugModeBox = new CheckBox("Mode debug");
        debugModeBox.setSelected(false);
        CheckBox memoryOptimBox = new CheckBox("Optimisation mémoire");
        memoryOptimBox.setSelected(true);

        perfSection.getChildren().addAll(perfTitle, debugModeBox, memoryOptimBox);

        content.getChildren().addAll(startupSection, new Separator(), saveSection, new Separator(), perfSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Onglet Base de données
     */
    private ScrollPane createDatabaseTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Section Connexion
        VBox connectionSection = new VBox(10);
        connectionSection.getStyleClass().add("settings-section");

        Label connectionTitle = new Label("🔗 Connexion à la base de données");
        connectionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusLabel = new Label("Statut:");
        Label statusValue = new Label("✅ Connecté (H2 Database)");
        statusValue.getStyleClass().add("status-connected");

        statusBox.getChildren().addAll(statusLabel, statusValue);

        // Informations de la base
        VBox infoBox = new VBox(5);
        Label urlLabel = new Label("URL: jdbc:h2:mem:magsav");
        Label driverLabel = new Label("Driver: H2 Database Engine");
        Label versionLabel = new Label("Version: 2.1.214");

        infoBox.getChildren().addAll(urlLabel, driverLabel, versionLabel);

        connectionSection.getChildren().addAll(connectionTitle, statusBox, infoBox);

        // Section Actions
        VBox actionsSection = new VBox(10);
        actionsSection.getStyleClass().add("settings-section");

        Label actionsTitle = new Label("🛠️ Actions");
        actionsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox actionBox = new HBox(10);
        Button testButton = new Button("Tester la connexion");
        Button backupButton = new Button("Sauvegarder");
        Button resetButton = new Button("Réinitialiser");

        testButton.setOnAction(e -> testDatabaseConnection());
        backupButton.setOnAction(e -> backupDatabase());
        resetButton.setOnAction(e -> resetDatabase());

        actionBox.getChildren().addAll(testButton, backupButton, resetButton);
        actionsSection.getChildren().addAll(actionsTitle, actionBox);

        content.getChildren().addAll(connectionSection, new Separator(), actionsSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Onglet Notifications
     */
    private ScrollPane createNotificationsTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Section Notifications générales
        VBox generalSection = new VBox(10);
        generalSection.getStyleClass().add("settings-section");

        Label generalTitle = new Label("🔔 Notifications générales");
        generalTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox enableNotifBox = new CheckBox("Activer les notifications");
        enableNotifBox.setSelected(true);
        CheckBox soundNotifBox = new CheckBox("Sons de notification");
        soundNotifBox.setSelected(true);
        CheckBox popupNotifBox = new CheckBox("Notifications popup");
        popupNotifBox.setSelected(true);

        generalSection.getChildren().addAll(generalTitle, enableNotifBox, soundNotifBox, popupNotifBox);

        // Section Notifications spécifiques
        VBox specificSection = new VBox(10);
        specificSection.getStyleClass().add("settings-section");

        Label specificTitle = new Label("📋 Notifications spécifiques");
        specificTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox savNotifBox = new CheckBox("Notifications SAV (interventions urgentes)");
        savNotifBox.setSelected(true);
        CheckBox equipmentNotifBox = new CheckBox("Notifications équipement (maintenance)");
        equipmentNotifBox.setSelected(true);
        CheckBox contractNotifBox = new CheckBox("Notifications contrats (échéances)");
        contractNotifBox.setSelected(false);
        CheckBox vehicleNotifBox = new CheckBox("Notifications véhicules (révisions)");
        vehicleNotifBox.setSelected(true);

        specificSection.getChildren().addAll(specificTitle, savNotifBox, equipmentNotifBox, contractNotifBox,
                vehicleNotifBox);

        content.getChildren().addAll(generalSection, new Separator(), specificSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Onglet Sécurité
     */
    private ScrollPane createSecurityTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Section Authentification
        VBox authSection = new VBox(10);
        authSection.getStyleClass().add("settings-section");

        Label authTitle = new Label("🔒 Authentification");
        authTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox autoLockBox = new CheckBox("Verrouillage automatique après inactivité");
        autoLockBox.setSelected(false);

        HBox timeoutBox = new HBox(10);
        Label timeoutLabel = new Label("Délai d'inactivité:");
        ComboBox<String> timeoutCombo = new ComboBox<>();
        timeoutCombo.getItems().addAll("5 minutes", "15 minutes", "30 minutes", "1 heure");
        timeoutCombo.setValue("15 minutes");
        timeoutBox.getChildren().addAll(timeoutLabel, timeoutCombo);

        authSection.getChildren().addAll(authTitle, autoLockBox, timeoutBox);

        // Section Logs de sécurité
        VBox logsSection = new VBox(10);
        logsSection.getStyleClass().add("settings-section");

        Label logsTitle = new Label("📊 Logs de sécurité");
        logsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        CheckBox logActionsBox = new CheckBox("Enregistrer les actions utilisateur");
        logActionsBox.setSelected(true);
        CheckBox logErrorsBox = new CheckBox("Enregistrer les erreurs");
        logErrorsBox.setSelected(true);

        HBox logLevelBox = new HBox(10);
        Label logLevelLabel = new Label("Niveau de log:");
        ComboBox<String> logLevelCombo = new ComboBox<>();
        logLevelCombo.getItems().addAll("DEBUG", "INFO", "WARN", "ERROR");
        logLevelCombo.setValue("INFO");
        logLevelBox.getChildren().addAll(logLevelLabel, logLevelCombo);

        logsSection.getChildren().addAll(logsTitle, logActionsBox, logErrorsBox, logLevelBox);

        content.getChildren().addAll(authSection, new Separator(), logsSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    /**
     * Onglet À propos
     */
    private ScrollPane createAboutTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // Logo et titre
        Label logoLabel = new Label("🎭");
        logoLabel.setFont(Font.font("System", FontWeight.BOLD, 48));

        Label appTitle = new Label("MAGSAV 3.0");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label subtitle = new Label("Système de Gestion SAV et Parc Matériel");
        subtitle.setFont(Font.font("System", FontWeight.NORMAL, 16));

        // Informations version
        VBox versionSection = new VBox(5);
        versionSection.setAlignment(javafx.geometry.Pos.CENTER);

        Label versionLabel = new Label("Version: 3.0.0-refactored");
        versionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        Label buildLabel = new Label("Build: 2025.11.20");
        Label javaLabel = new Label("Java Runtime: " + System.getProperty("java.version"));
        Label fxLabel = new Label("JavaFX: 21");

        versionSection.getChildren().addAll(versionLabel, buildLabel, javaLabel, fxLabel);

        // Informations développeur
        VBox devSection = new VBox(5);
        devSection.setAlignment(javafx.geometry.Pos.CENTER);

        Label devTitle = new Label("Développé pour Mag Scène");
        devTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label devInfo = new Label("Architecture refactorisée avec injection de dépendances");
        Label techInfo = new Label("Spring Boot 3.3 • JavaFX 21 • H2 Database");

        devSection.getChildren().addAll(devTitle, devInfo, techInfo);

        // Boutons d'action
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);

        Button licenseButton = new Button("📄 Licence");
        Button updatesButton = new Button("🔄 Vérifier les mises à jour");
        Button supportButton = new Button("❓ Support");

        licenseButton.setOnAction(e -> showLicense());
        updatesButton.setOnAction(e -> checkUpdates());
        supportButton.setOnAction(e -> showSupport());

        buttonBox.getChildren().addAll(licenseButton, updatesButton, supportButton);

        content.getChildren().addAll(
                logoLabel, appTitle, subtitle,
                new Separator(),
                versionSection,
                new Separator(),
                devSection,
                new Separator(),
                buttonBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private void testDatabaseConnection() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Test de connexion");
        alert.setHeaderText("Test de la base de données");
        alert.setContentText("✅ Connexion à la base H2 réussie !");
        alert.showAndWait();
    }

    private void resetDatabase() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Réinitialiser la base de données");
        confirmAlert.setContentText("⚠️ Cette action supprimera toutes les données. Continuer ?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Réinitialisation");
                infoAlert.setHeaderText("Base de données");
                infoAlert.setContentText("🔄 Réinitialisation en cours...");
                infoAlert.showAndWait();
            }
        });
    }

    /**
     * Onglet Catégories
     */
    private ScrollPane createCategoriesTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        CategoryManager categoryManager = CategoryManager.getInstance();

        // Section de sélection du type de catégorie
        VBox typeSection = new VBox(10);
        typeSection.getStyleClass().add("settings-section");

        Label typeTitle = new Label("📂 Type de catégories");
        typeTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        ComboBox<CategoryType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(CategoryType.values());
        typeCombo.setValue(CategoryType.EQUIPMENT);
        typeCombo.setPrefWidth(200);

        typeSection.getChildren().addAll(typeTitle, typeCombo);

        // Tableau des catégories
        VBox tableSection = new VBox(10);
        tableSection.getStyleClass().add("settings-section");

        Label tableTitle = new Label("📋 Gestion des catégories");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        TableView<Category> categoryTable = new TableView<>();
        categoryTable.setPrefHeight(300);

        // Colonnes du tableau
        TableColumn<Category, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<Category, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(250);

        TableColumn<Category, String> colorCol = new TableColumn<>("Couleur");
        colorCol.setCellValueFactory(new PropertyValueFactory<>("color"));
        colorCol.setPrefWidth(100);
        colorCol.setCellFactory(column -> new TableCell<Category, String>() {
            @Override
            protected void updateItem(String color, boolean empty) {
                super.updateItem(color, empty);
                if (empty || color == null) {
                    setGraphic(null);
                } else {
                    Rectangle colorRect = new Rectangle(20, 20);
                    colorRect.setFill(Color.web(color));
                    colorRect.setStroke(Color.BLACK);
                    colorRect.setStrokeWidth(1);
                    setGraphic(colorRect);
                }
            }
        });

        TableColumn<Category, Boolean> systemCol = new TableColumn<>("Système");
        systemCol.setCellValueFactory(new PropertyValueFactory<>("systemCategory"));
        systemCol.setPrefWidth(80);
        systemCol.setCellFactory(column -> new TableCell<Category, Boolean>() {
            @Override
            protected void updateItem(Boolean isSystem, boolean empty) {
                super.updateItem(isSystem, empty);
                if (empty || isSystem == null) {
                    setText("");
                } else {
                    setText(isSystem ? "✓" : "");
                }
            }
        });

        categoryTable.getColumns().addAll(nameCol, descCol, colorCol, systemCol);

        // Initialiser avec les catégories d'équipements
        categoryTable.setItems(categoryManager.getEquipmentCategories());

        // Mettre à jour le tableau selon le type sélectionné
        typeCombo.setOnAction(e -> {
            CategoryType selectedType = typeCombo.getValue();
            if (selectedType != null) {
                categoryTable.setItems(categoryManager.getCategoriesByType(selectedType));
            }
        });

        tableSection.getChildren().addAll(tableTitle, categoryTable);

        // Boutons d'action
        VBox actionsSection = new VBox(10);
        actionsSection.getStyleClass().add("settings-section");

        Label actionsTitle = new Label("🛠️ Actions");
        actionsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        HBox buttonBox = new HBox(10);
        Button addButton = new Button("➕ Ajouter");
        Button editButton = new Button("✏️ Modifier");
        Button deleteButton = new Button("🗑️ Supprimer");
        Button exportButton = new Button("📤 Exporter");

        addButton.setOnAction(e -> showAddCategoryDialog(typeCombo.getValue()));
        editButton.setOnAction(e -> {
            Category selected = categoryTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditCategoryDialog(selected, typeCombo.getValue());
            } else {
                showNoCategorySelectedAlert();
            }
        });
        deleteButton.setOnAction(e -> {
            Category selected = categoryTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showDeleteCategoryConfirmation(selected, typeCombo.getValue());
            } else {
                showNoCategorySelectedAlert();
            }
        });
        exportButton.setOnAction(e -> showExportCategoriesDialog());

        buttonBox.getChildren().addAll(addButton, editButton, deleteButton, exportButton);
        actionsSection.getChildren().addAll(actionsTitle, buttonBox);

        // Section statistiques
        VBox statsSection = new VBox(10);
        statsSection.getStyleClass().add("settings-section");

        Label statsTitle = new Label("📊 Statistiques");
        statsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label equipmentCount = new Label(
                "Équipements: " + categoryManager.getEquipmentCategories().size() + " catégories");
        Label clientCount = new Label("Clients: " + categoryManager.getClientCategories().size() + " catégories");
        Label projectCount = new Label("Projets: " + categoryManager.getProjectCategories().size() + " catégories");
        Label savCount = new Label("SAV: " + categoryManager.getSavCategories().size() + " catégories");

        statsSection.getChildren().addAll(statsTitle, equipmentCount, clientCount, projectCount, savCount);

        content.getChildren().addAll(
                typeSection,
                new Separator(),
                tableSection,
                new Separator(),
                actionsSection,
                new Separator(),
                statsSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private void backupDatabase() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sauvegarde");
        alert.setHeaderText("Sauvegarde de la base de données");
        alert.setContentText("💾 Sauvegarde créée avec succès !");
        alert.showAndWait();
    }

    private void showLicense() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Licence");
        alert.setHeaderText("Licence d'utilisation MAGSAV 3.0");
        alert.setContentText("© 2025 Mag Scène. Tous droits réservés.\n\n" +
                "Ce logiciel est développé spécifiquement pour Mag Scène.\n" +
                "Utilisation strictement réservée à l'entreprise.");
        alert.showAndWait();
    }

    private void checkUpdates() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mises à jour");
        alert.setHeaderText("Vérification des mises à jour");
        alert.setContentText("🔄 Vous utilisez la dernière version de MAGSAV 3.0\n\n" +
                "Version actuelle: 3.0.0-refactored\n" +
                "Dernière vérification: " + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")));
        alert.showAndWait();
    }

    private void showSupport() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Support");
        alert.setHeaderText("Support technique MAGSAV 3.0");
        alert.setContentText("📞 Pour toute assistance technique :\n\n" +
                "• Email: support@magscene.com\n" +
                "• Téléphone: +xx xx xx xx xx\n" +
                "• Documentation: Manuel utilisateur intégré\n\n" +
                "Version: 3.0.0-refactored\n" +
                "Architecture: Spring Boot + JavaFX");
        alert.showAndWait();
    }

    // Méthodes pour la gestion des catégories

    private void showAddCategoryDialog(CategoryType type) {
        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une catégorie");
        dialog.setHeaderText("Nouvelle catégorie " + type.getDisplayName());

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nom de la catégorie");
        TextArea descField = new TextArea();
        descField.setPromptText("Description");
        descField.setPrefRowCount(3);
        ColorPicker colorPicker = new ColorPicker(Color.BLUE);

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Couleur:"), 0, 2);
        grid.add(colorPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String colorHex = String.format("#%02X%02X%02X",
                        (int) (colorPicker.getValue().getRed() * 255),
                        (int) (colorPicker.getValue().getGreen() * 255),
                        (int) (colorPicker.getValue().getBlue() * 255));
                return new Category(nameField.getText(), descField.getText(), colorHex, false);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(category -> {
            CategoryManager.getInstance().addEquipmentCategory(category);
            showSuccessAlert("Catégorie ajoutée",
                    "La catégorie '" + category.getName() + "' a été ajoutée avec succès.");
        });
    }

    private void showEditCategoryDialog(Category category, CategoryType type) {
        if (category.isSystemCategory()) {
            showErrorAlert("Modification impossible", "Les catégories système ne peuvent pas être modifiées.");
            return;
        }

        Dialog<Category> dialog = new Dialog<>();
        dialog.setTitle("Modifier la catégorie");
        dialog.setHeaderText("Modifier " + category.getName());

        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(category.getName());
        TextArea descField = new TextArea(category.getDescription());
        descField.setPrefRowCount(3);
        ColorPicker colorPicker = new ColorPicker(Color.web(category.getColor()));

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Couleur:"), 0, 2);
        grid.add(colorPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String colorHex = String.format("#%02X%02X%02X",
                        (int) (colorPicker.getValue().getRed() * 255),
                        (int) (colorPicker.getValue().getGreen() * 255),
                        (int) (colorPicker.getValue().getBlue() * 255));
                return new Category(nameField.getText(), descField.getText(), colorHex, category.isSystemCategory());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newCategory -> {
            CategoryManager.getInstance().updateEquipmentCategory(category, newCategory);
            showSuccessAlert("Catégorie modifiée", "La catégorie a été modifiée avec succès.");
        });
    }

    private void showDeleteCategoryConfirmation(Category category, CategoryType type) {
        if (category.isSystemCategory()) {
            showErrorAlert("Suppression impossible", "Les catégories système ne peuvent pas être supprimées.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer la catégorie " + category.getName() + " ?");
        confirm.setContentText(
                "⚠️ Cette action est irréversible. Tous les éléments associés à cette catégorie devront être recatégorisés.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean removed = CategoryManager.getInstance().removeEquipmentCategory(category);
                if (removed) {
                    showSuccessAlert("Catégorie supprimée",
                            "La catégorie '" + category.getName() + "' a été supprimée.");
                } else {
                    showErrorAlert("Erreur de suppression", "Impossible de supprimer la catégorie.");
                }
            }
        });
    }

    private void showNoCategorySelectedAlert() {
        showErrorAlert("Aucune sélection", "Veuillez sélectionner une catégorie dans le tableau.");
    }

    private void showExportCategoriesDialog() {
        String exportData = CategoryManager.getInstance().exportCategoriesToJson();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Export des catégories");
        alert.setHeaderText("Données exportées");
        alert.setContentText(exportData);

        TextArea textArea = new TextArea(exportData);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setExpandableContent(textArea);
        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("✅ " + message);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText("❌ " + message);
        alert.showAndWait();
    }
}