package com.magscene.magsav.desktop;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.GlobalSearchService;
import com.magscene.magsav.desktop.service.GlobalSearchService.SearchResult;
import com.magscene.magsav.desktop.component.GlobalSearchSuggestions;
import com.magscene.magsav.desktop.component.GlobalSearchSuggestions.NavigationCallback;
import com.magscene.magsav.desktop.utils.MemoryProfiler;
import com.magscene.magsav.desktop.utils.ResourceCleanupManager;
import com.magscene.magsav.desktop.view.DashboardView;
import com.magscene.magsav.desktop.view.ClientManagerView;
import com.magscene.magsav.desktop.view.ContractManagerView;
import com.magscene.magsav.desktop.view.EquipmentManagerView;
import com.magscene.magsav.desktop.view.SAVManagerView;
import com.magscene.magsav.desktop.view.PersonnelManagerView;
import com.magscene.magsav.desktop.view.VehicleManagerView;
import com.magscene.magsav.desktop.view.salesinstallation.ProjectManagerView;
import com.magscene.magsav.desktop.view.config.SpecialtiesConfigView;
import com.magscene.magsav.desktop.view.config.CategoriesConfigView;
import com.magscene.magsav.desktop.view.planning.PlanningView;
import com.magscene.magsav.desktop.view.preferences.ThemePreferencesView;
import com.magscene.magsav.desktop.theme.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;

/**
 * Application JavaFX 21 pour MAGSAV-3.0
 * Interface desktop moderne avec Java 21 LTS connectée à l'API Backend
 */
public class MagsavDesktopApplication extends Application {

    private ApiService apiService;
    private StackPane mainContent;
    private Label statusLabel;
    private GlobalSearchService globalSearchService;
    private GlobalSearchSuggestions globalSearchSuggestions;
    
    // Boutons de navigation pour gérer les états actifs
    private Button btnDashboard;
    private Button btnSAV;
    private Button btnEquipment;
    private Button btnClients;
    private Button btnContracts;
    private Button btnSales;
    private Button btnVehicles;
    private Button btnPersonnel;
    private Button btnPlanning;
    private Button btnSettings;
    private Button[] allNavigationButtons;
    
    // Cache des vues pour optimisation performance
    private DashboardView cachedDashboardView;
    private EquipmentManagerView cachedEquipmentView;
    private SAVManagerView cachedSAVView;
    private ClientManagerView cachedClientView;
    private ContractManagerView cachedContractView;
    private ProjectManagerView cachedSalesView;
    private VehicleManagerView cachedVehicleView;
    private PersonnelManagerView cachedPersonnelView;
    private PlanningView cachedPlanningView;
    
    /**
     * Initialise l'ApiService de manière différée pour optimiser les performances de démarrage
     */
    private ApiService getApiService() {
        if (apiService == null) {
            apiService = new ApiService();
            System.out.println("✓ ApiService initialisé avec succès (lazy loading)");
        }
        return apiService;
    }

    @Override
    public void start(Stage primaryStage) {
        // Note: ApiService sera initialisé en lazy loading pour optimiser le démarrage
        
        // Profiling mémoire au démarrage
        MemoryProfiler.logMemoryUsage("Application Start");
        
        primaryStage.setTitle("MAGSAV-3.0 - Système SAV & Parc Matériel");

        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5)); // Marges réduites

        // Header global avec barre de recherche unifiée
        VBox globalHeader = createGlobalHeader();
        root.setTop(globalHeader);

        // Zone latérale avec menu navigation
        VBox leftPanel = createLeftPanel();
        root.setLeft(leftPanel);

        // Contenu principal (occupe maintenant plus d'espace)
        mainContent = createMainContent();
        root.setCenter(mainContent);

        // Status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Scene avec système de thèmes intégré
        Scene scene = new Scene(root, 1400, 900);
        
        // Initialisation du système de thèmes
        ThemeManager themeManager = ThemeManager.getInstance();
        themeManager.setScene(scene);
        String currentTheme = themeManager.getCurrentTheme();
        themeManager.applyTheme(currentTheme);
        System.out.println("✓ Système de thèmes initialisé avec succès - Thème actuel: " + currentTheme);
        
        // Initialisation des services de recherche (d'abord sans ApiService)
        this.globalSearchService = new GlobalSearchService();
        
        primaryStage.setScene(scene);
        
        // Force tous les TextField à avoir les bonnes couleurs
        forceAllTextFieldsColors(scene);
        
        // Configuration automatique du deuxième écran
        configureSecondaryScreen(primaryStage);
        
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Initialiser le Dashboard par défaut
        showDashboardModule();
        setActiveButton(btnDashboard);
        
        // Initialiser la recherche globale avec les vraies données après le chargement
        initializeGlobalSearchWithRealData();

        // Test de connectivité au démarrage
        testBackendConnection();
        
        // Démarrage du monitoring mémoire (debug)
        MemoryProfiler.startContinuousMonitoring("MAGSAV-Desktop", 60000); // Toutes les minutes

        // Fermeture propre avec nettoyage des ressources
        primaryStage.setOnCloseRequest(e -> {
            System.out.println("🛑 Fermeture de l'application...");
            
            if (apiService != null) {
                apiService.close();
            }
            
            // Nettoyage final des ressources
            ResourceCleanupManager.getInstance().shutdown();
            MemoryProfiler.logMemoryUsage("Application Shutdown");
            
            Platform.exit();
        });
    }

    private VBox createGlobalHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(10, 15, 10, 15));
        header.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; " +
                       "-fx-border-color: #1D2659; -fx-border-width: 0 0 2 0; " +
                       "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");
        
        HBox topRow = new HBox(20);
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        // Logo et titre
        HBox logoSection = new HBox(10);
        logoSection.setAlignment(Pos.CENTER_LEFT);
        
        Label logoLabel = new Label("📋");
        logoLabel.setFont(Font.font("System", 24));
        
        VBox titleSection = new VBox();
        titleSection.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("MAGSAV-3.0");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        Label subtitleLabel = new Label("Système SAV & Parc Matériel");
        subtitleLabel.setFont(Font.font("System", 10));
        subtitleLabel.setTextFill(Color.web("#7f8c8d"));
        
        titleSection.getChildren().addAll(titleLabel, subtitleLabel);
        logoSection.getChildren().addAll(logoLabel, titleSection);
        
        // Espaceur
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Barre de recherche globale
        HBox globalSearchBox = createGlobalSearchBox();
        
        topRow.getChildren().addAll(logoSection, spacer, globalSearchBox);
        header.getChildren().add(topRow);
        
        return header;
    }
    
    private HBox createGlobalSearchBox() {
        HBox searchContainer = new HBox(10);
        searchContainer.setAlignment(Pos.CENTER_RIGHT);
        searchContainer.setPadding(new Insets(5, 10, 5, 10));
        searchContainer.getStyleClass().add("search-container");
        
        // Force le style du conteneur programmatiquement
        searchContainer.setStyle("-fx-background-color: #142240; -fx-border-color: transparent;");
        
        // Conteneur pour le champ de recherche avec icône intégrée
        HBox searchFieldContainer = new HBox(8);
        searchFieldContainer.setAlignment(Pos.CENTER_LEFT);
        searchFieldContainer.setStyle("-fx-background-color: #142240; -fx-background-radius: 4; " +
                                    "-fx-border-color: #6B71F2; -fx-border-width: 0.5; -fx-border-radius: 4; " +
                                    "-fx-padding: 6 10;");
        
        // Icône loupe intégrée
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-text-fill: #6B71F2; -fx-font-size: 14px;");
        
        // Champ de recherche global
        TextField globalSearchField = new TextField();
        globalSearchField.setPromptText("Recherche globale");
        globalSearchField.setPrefWidth(320);
        globalSearchField.getStyleClass().addAll("global-search-field", "search-container");
        
        // Force TOUS les styles programmatiquement pour surpasser JavaFX
        String searchFieldStyle = "-fx-background-color: transparent !important; " +
                                "-fx-control-inner-background: transparent !important; " +
                                "-fx-text-fill: #6B71F2 !important; " +
                                "-fx-prompt-text-fill: #6B71F2 !important; " +
                                "-fx-background-insets: 0; " +
                                "-fx-background-radius: 0; " +
                                "-fx-border-color: transparent; " +
                                "-fx-focus-color: transparent; " +
                                "-fx-faint-focus-color: transparent;";
        
        globalSearchField.setStyle(searchFieldStyle);
        
        // Force aussi après rendu pour tous les nodes internes
        Platform.runLater(() -> {
            globalSearchField.setStyle(searchFieldStyle);
            // Force sur TOUS les éléments possibles dans le TextField
            forceTextFieldColors(globalSearchField);
            
            // Re-force après délai pour être absolument sûr
            Platform.runLater(() -> {
                Platform.runLater(() -> forceTextFieldColors(globalSearchField));
            });
        });
        // Styles appliqués via CSS ET programmatiquement
        
        // Assembler l'icône et le champ de recherche
        searchFieldContainer.getChildren().addAll(searchIcon, globalSearchField);
        
        // Initialisation du composant de suggestions avec callback de navigation
        this.globalSearchSuggestions = new GlobalSearchSuggestions(globalSearchField, this::handleSearchNavigation);
        
        // Zone de résultats (PopOver qui apparaîtra)
        setupGlobalSearch(globalSearchField);
        
        searchContainer.getChildren().add(searchFieldContainer);
        return searchContainer;
    }
    
    private void setupGlobalSearch(TextField searchField) {
        // Recherche progressive pendant la frappe
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() >= 2) {
                performGlobalSearch(newValue);
            }
        });
        
        // Action sur Entrée pour recherche complète
        searchField.setOnAction(e -> {
            String searchText = searchField.getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                performGlobalSearch(searchText);
                // Ici on pourrait ouvrir une fenêtre de résultats détaillés
                showGlobalSearchResults(searchText);
            }
        });
    }
    
    private void performGlobalSearch(String query) {
        // TODO: Implémenter la recherche dans tous les modules
        System.out.println("🔍 Recherche globale: " + query);
        // Cette méthode sera complétée pour chercher dans:
        // - Équipements (nom, modèle, série, catégorie)
        // - Clients (nom, email, SIRET)
        // - SAV (numéro intervention, description)
        // - Contrats (numéro, titre, client)
        // - Véhicules (immatriculation, modèle)
        // - Personnel (nom, spécialités)
    }
    
    private void showGlobalSearchResults(String query) {
        // TODO: Afficher une fenêtre popup avec résultats classés par type
        System.out.println("📊 Affichage résultats détaillés pour: " + query);
    }

    private VBox createLeftPanel() {
        VBox leftPanel = new VBox();
        leftPanel.setPrefWidth(220); // Élargi pour une meilleure lisibilité des boutons
        leftPanel.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + ";");
        leftPanel.setPadding(new Insets(10));
        
        // Sidebar de navigation (sans header maintenant)
        VBox sidebar = createSidebar();
        
        leftPanel.getChildren().add(sidebar);
        return leftPanel;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);  // Espacement 0 entre les boutons
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(0));  // Aucune marge
        sidebar.setPrefWidth(250);

        btnDashboard = new Button("🏠 Dashboard");
        btnSAV = new Button("🔧 SAV & Interventions");
        btnEquipment = new Button("📦 Parc Matériel");
        btnClients = new Button("👥 Clients");
        btnContracts = new Button("📋 Contrats");
        btnSales = new Button("💼 Ventes & Installations");
        btnVehicles = new Button("🚐 Véhicules");
        btnPersonnel = new Button("👤 Personnel");
        btnPlanning = new Button("📅 Planning");
        btnSettings = new Button("⚙ Paramètres");

        // Initialiser le tableau des boutons pour la gestion des états
        allNavigationButtons = new Button[]{btnDashboard, btnSAV, btnEquipment, btnClients, btnContracts, btnSales, btnVehicles, btnPersonnel, btnPlanning, btnSettings};
        
        // Style des boutons
        for (Button btn : allNavigationButtons) {
            btn.getStyleClass().add("menu-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
        }

        // Actions avec gestion de l'état actif
        btnDashboard.setOnAction(e -> {
            setActiveButton(btnDashboard);
            showDashboardModule();
        });
        btnEquipment.setOnAction(e -> {
            setActiveButton(btnEquipment);
            showEquipmentModule();
        });
        btnSAV.setOnAction(e -> {
            setActiveButton(btnSAV);
            showSAVModule();
        });
        btnClients.setOnAction(e -> {
            setActiveButton(btnClients);
            showClientModule();
        });
        btnContracts.setOnAction(e -> {
            setActiveButton(btnContracts);
            showContractModule();
        });
        btnSales.setOnAction(e -> {
            setActiveButton(btnSales);
            showSalesModule();
        });
        btnVehicles.setOnAction(e -> {
            setActiveButton(btnVehicles);
            showVehicleModule();
        });
        btnPersonnel.setOnAction(e -> {
            setActiveButton(btnPersonnel);
            showPersonnelModule();
        });
        btnPlanning.setOnAction(e -> {
            setActiveButton(btnPlanning);
            showPlanningModule();
        });
        btnSettings.setOnAction(e -> {
            setActiveButton(btnSettings);
            showSettingsModule();
        });

        sidebar.getChildren().addAll(
            btnDashboard,
            btnEquipment,          // Parc Matériel
            btnSAV,               // SAV & Interventions  
            btnSales,             // Ventes & Installations
            btnVehicles,          // Véhicules
            btnPersonnel,         // Personnel
            btnPlanning,          // Planning
            btnClients,           // Clients
            btnContracts,         // Contrats
            btnSettings           // Paramètres
        );

        return sidebar;
    }

    private StackPane createMainContent() {
        StackPane content = new StackPane();
        content.getStyleClass().add("main-content");
        
        // Le contenu sera défini après l'initialisation de la sidebar
        mainContent = content;
        
        return content;
    }

    private HBox createStatusBar() {
        HBox statusBar = new HBox();
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("🔄 Connexion au backend...");
        Label backendUrl = new Label("🌐 http://localhost:8080");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label javaVersion = new Label("☕ Java " + System.getProperty("java.version"));
        
        statusBar.getChildren().addAll(statusLabel, new Label(" | "), backendUrl, spacer, javaVersion);
        return statusBar;
    }

    /**
     * Configure l'affichage automatique sur le deuxième écran si disponible
     */
    private void configureSecondaryScreen(Stage primaryStage) {
        try {
            // Obtenir tous les écrans disponibles
            var screens = Screen.getScreens();
            
            if (screens.size() > 1) {
                // Utiliser le deuxième écran (index 1)
                Screen secondaryScreen = screens.get(1);
                Rectangle2D bounds = secondaryScreen.getVisualBounds();
                
                // Positionner la fenêtre sur le deuxième écran
                primaryStage.setX(bounds.getMinX());
                primaryStage.setY(bounds.getMinY());
                primaryStage.setWidth(bounds.getWidth());
                primaryStage.setHeight(bounds.getHeight());
                
                System.out.println("✓ Application configurée sur le deuxième écran : " + 
                                   (int)bounds.getWidth() + "x" + (int)bounds.getHeight());
            } else {
                System.out.println("ℹ️ Deuxième écran non détecté, utilisation de l'écran principal");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de la configuration du deuxième écran : " + e.getMessage());
        }
    }

    /**
     * Test de connexion au backend au démarrage
     */
    private void testBackendConnection() {
        Task<Boolean> connectionTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return getApiService().testConnection().get();
            }
        };

        connectionTask.setOnSucceeded(e -> {
            boolean connected = connectionTask.getValue();
            Platform.runLater(() -> {
                if (connected) {
                    statusLabel.setText("✅ Connecté au backend MAGSAV-3.0");
                    statusLabel.getStyleClass().removeAll("status-error");
                    statusLabel.getStyleClass().add("status-success");
                } else {
                    statusLabel.setText("❌ Backend non disponible - Mode hors ligne");
                    statusLabel.getStyleClass().removeAll("status-success");
                    statusLabel.getStyleClass().add("status-error");
                }
            });
        });

        connectionTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                statusLabel.setText("❌ Erreur connexion backend");
                statusLabel.getStyleClass().removeAll("status-success");
                statusLabel.getStyleClass().add("status-error");
            });
        });

        Thread connectionThread = new Thread(connectionTask);
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private void showEquipmentModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedEquipmentView == null) {
            System.out.println("✓ Chargement initial du gestionnaire d'équipement...");
            cachedEquipmentView = new EquipmentManagerView(getApiService());
            MemoryProfiler.logMemoryUsage("Equipment View Created");
        } else {
            System.out.println("⚡ Réutilisation cache Equipment View");
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedEquipmentView);
        statusLabel.setText("📦 Module Parc Matériel actif");
    }

    private void showSAVModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedSAVView == null) {
            System.out.println("✓ Chargement initial du gestionnaire SAV...");
            cachedSAVView = new SAVManagerView(getApiService());
            MemoryProfiler.logMemoryUsage("SAV View Created");
        } else {
            System.out.println("⚡ Réutilisation cache SAV View");
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedSAVView);
        statusLabel.setText("🔧 Module SAV actif");
    }

    private void showDashboardModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedDashboardView == null) {
            cachedDashboardView = new DashboardView();
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedDashboardView);
        statusLabel.setText("🏠 Dashboard actif");
    }

    private void showClientModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedClientView == null) {
            cachedClientView = new ClientManagerView(getApiService());
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedClientView);
        statusLabel.setText("👥 Module Clients actif");
    }

    private void showContractModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedContractView == null) {
            cachedContractView = new ContractManagerView(getApiService());
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedContractView);
        statusLabel.setText("📋 Module Contrats actif");
    }

    private void showPersonnelModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedPersonnelView == null) {
            cachedPersonnelView = new PersonnelManagerView(getApiService());
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedPersonnelView);
        statusLabel.setText("👤 Module Personnel actif");
    }
    
    private void showPlanningModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedPlanningView == null) {
            cachedPlanningView = new PlanningView(getApiService());
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedPlanningView);
        statusLabel.setText("📅 Module Planning actif");
    }

    private void showVehicleModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedVehicleView == null) {
            cachedVehicleView = new VehicleManagerView(getApiService());
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedVehicleView);
        statusLabel.setText("🚐 Module Véhicules actif");
    }

    private void showSalesModule() {
        // Lazy loading avec cache pour optimisation performance
        if (cachedSalesView == null) {
            cachedSalesView = new ProjectManagerView(getApiService());
        }
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(cachedSalesView);
        statusLabel.setText("💼 Module Ventes & Installations actif");
    }

    /**
     * Gère la navigation depuis les résultats de recherche globale
     */
    private void handleSearchNavigation(SearchResult result) {
        System.out.println("🎯 Navigation vers: " + result.getType() + " - " + result.getName());
        
        switch (result.getType()) {
            case "Client":
                setActiveButton(btnClients);
                showClientModule();
                // Sélectionner et ouvrir la fiche du client (après création et chargement de la vue)
                new Thread(() -> {
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                    Platform.runLater(() -> {
                        if (cachedClientView != null) {
                            cachedClientView.selectAndViewClient(result.getName());
                        }
                    });
                }).start();
                break;
                
            case "Matériel":
                setActiveButton(btnEquipment);
                showEquipmentModule();
                // Sélectionner et ouvrir la fiche de l'équipement (après création et chargement de la vue)
                new Thread(() -> {
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                    Platform.runLater(() -> {
                        if (cachedEquipmentView != null) {
                            cachedEquipmentView.selectAndViewEquipment(result.getName());
                        }
                    });
                }).start();
                break;
                
            case "Projet":
                setActiveButton(btnSales);
                showSalesModule();
                // Sélectionner et ouvrir la fiche du projet (après création et chargement de la vue)
                new Thread(() -> {
                    try { Thread.sleep(500); } catch (InterruptedException e) {}
                    Platform.runLater(() -> {
                        if (cachedSalesView != null) {
                            cachedSalesView.selectAndViewProject(result.getName());
                        }
                    });
                }).start();
                break;
                
            case "Personnel":
                setActiveButton(btnPersonnel);
                showPersonnelModule();
                // TODO: Sélectionner la personne spécifique dans la table
                break;
                
            case "Intervention":
                setActiveButton(btnSAV);
                showSAVModule();
                // TODO: Sélectionner l'intervention spécifique dans la table
                break;
                
            default:
                // Fallback: ouvrir le dashboard
                setActiveButton(btnDashboard);
                showDashboardModule();
                System.out.println("⚠️ Type de résultat non reconnu: " + result.getType());
                break;
        }
    }

    private void showSettingsModule() {
        mainContent.getChildren().clear();
        
        // Container principal avec header unifié
        VBox settingsContainer = new VBox(10);
        settingsContainer.setPadding(new Insets(5));
        settingsContainer.getStyleClass().add("settings-container");
        
        // Header unifié selon modèle Clients
        VBox header = createSettingsHeader();
        
        // Créer une vue intégrée des préférences avec onglets
        TabPane settingsTabPane = new TabPane();
        settingsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        settingsTabPane.getStyleClass().add("settings-tab-pane");
        
        // Onglet Thèmes intégré
        Tab themeTab = new Tab("🎨 Thèmes");
        ThemePreferencesView themePreferencesView = new ThemePreferencesView();
        ScrollPane themeScrollPane = new ScrollPane(themePreferencesView);
        themeScrollPane.setFitToWidth(true);
        themeScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        themeTab.setContent(themeScrollPane);
        
        // Onglet Configuration des Spécialités
        Tab specialtiesTab = new Tab("🎯 Spécialités Personnel");
        SpecialtiesConfigView specialtiesView = new SpecialtiesConfigView(getApiService());
        specialtiesTab.setContent(specialtiesView);
        
        // Onglet Configuration des Catégories d'Équipement
        Tab categoriesTab = new Tab("🗂️ Catégories Équipement");
        CategoriesConfigView categoriesView = new CategoriesConfigView(getApiService());
        categoriesTab.setContent(categoriesView);
        
        settingsTabPane.getTabs().addAll(themeTab, specialtiesTab, categoriesTab);
        
        // Forcer le style des boutons de navigation des onglets
        forceTabNavigationButtonsStyle(settingsTabPane);
        
        // Assembly du container principal
        settingsContainer.getChildren().addAll(header, settingsTabPane);
        VBox.setVgrow(settingsTabPane, Priority.ALWAYS);
        
        mainContent.getChildren().add(settingsContainer);
        statusLabel.setText("⚙️ Module Paramètres & Thèmes actif");
    }
    
    private VBox createSettingsHeader() {
        VBox header = new VBox(10);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Label title = new Label("⚙️ Paramètres");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));
        
        header.getChildren().add(title);
        return header;
    }

    private void showWelcomeView() {
        mainContent.getChildren().clear();
        
        VBox welcomeView = new VBox(20);
        welcomeView.setAlignment(Pos.CENTER);
        
        Label welcome = new Label("🎉 Bienvenue dans MAGSAV-3.0");
        welcome.getStyleClass().add("welcome-title");
        
        Label javaInfo = new Label("✨ Propulsé par Java " + System.getProperty("java.version"));
        javaInfo.getStyleClass().add("java-info");
        
        VBox features = new VBox(10);
        features.setAlignment(Pos.CENTER);
        features.getChildren().addAll(
            new Label("🧵 Virtual Threads pour performance optimale"),
            new Label("� QR Codes pour inventaire intelligent"),
            new Label("🌐 API REST intégrée"),
            new Label("💾 Base de données H2 embarquée"),
            new Label("🔒 Interface sécurisée")
        );
        
        Button btnStartEquipment = new Button("🚀 Commencer avec le Parc Matériel");
        btnStartEquipment.getStyleClass().add("start-button");
        btnStartEquipment.setOnAction(e -> showEquipmentModule());
        
        welcomeView.getChildren().addAll(welcome, javaInfo, features, btnStartEquipment);
        mainContent.getChildren().add(welcomeView);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Gère l'état actif des boutons de navigation
     * @param activeButton Le bouton qui doit être marqué comme actif
     */
    private void setActiveButton(Button activeButton) {
        // Retirer la classe "active" de tous les boutons
        for (Button btn : allNavigationButtons) {
            btn.getStyleClass().remove("active");
        }
        // Ajouter la classe "active" au bouton sélectionné
        activeButton.getStyleClass().add("active");
    }

    /**
     * Initialise la recherche globale avec les vraies données du backend
     */
    private void initializeGlobalSearchWithRealData() {
        // Exécuter en arrière-plan pour ne pas bloquer l'UI
        javafx.concurrent.Task<Void> initTask = new javafx.concurrent.Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Remplacer le service de recherche par une version avec ApiService
                GlobalSearchService newSearchService = new GlobalSearchService(getApiService());
                globalSearchService = newSearchService;
                
                // Mettre à jour le composant de suggestions
                Platform.runLater(() -> {
                    // Note: Les suggestions utilisent déjà le service mis à jour
                    System.out.println("✅ Recherche globale initialisée avec les données réelles");
                });
                return null;
            }
        };
        
        initTask.setOnFailed(e -> {
            System.err.println("⚠️ Erreur lors de l'initialisation de la recherche globale: " + 
                             initTask.getException().getMessage());
        });
        
        new Thread(initTask).start();
    }
    
    /**
     * Force les couleurs de fond et de texte sur tous les éléments d'un TextField
     * pour s'assurer que même les éléments internes JavaFX utilisent nos couleurs
     */
    private void forceTextFieldColors(TextField textField) {
        // Utilise la méthode publique
        forceSearchFieldColors(textField);
    }

    /**
     * Méthode utilitaire publique pour forcer les couleurs des TextField
     * Utilisée par tous les modules pour uniformiser les couleurs des champs de recherche
     */
    public static void forceSearchFieldColors(TextField textField) {
        // Style ULTRA AGRESSIF pour contrer toute surcharge CSS
        String forceStyle = "-fx-base: #142240 !important; " +
                           "-fx-background: #142240 !important; " +
                           "-fx-background-color: #142240 !important; " +
                           "-fx-control-inner-background: #142240 !important; " +
                           "-fx-control-inner-background-alt: #142240 !important; " +
                           "-fx-text-fill: #6B71F2 !important; " +
                           "-fx-text-base-color: #6B71F2 !important; " +
                           "-fx-prompt-text-fill: #6B71F2 !important;";
        
        textField.setStyle(forceStyle);
        
        // Force ABSOLUE sur tous les sous-éléments avec délai pour le rendu
        Platform.runLater(() -> {
            Platform.runLater(() -> { // Double Platform.runLater pour être sûr
                // Force sur TOUS les nodes
                textField.lookupAll("*").forEach(node -> {
                    String nodeStyle = "-fx-base: #142240 !important; " +
                                     "-fx-background: #142240 !important; " +
                                     "-fx-background-color: #142240 !important; " +
                                     "-fx-fill: #6B71F2 !important; " +
                                     "-fx-text-fill: #6B71F2 !important; " +
                                     "-fx-text-base-color: #6B71F2 !important;";
                    node.setStyle(nodeStyle);
                });
            });
        });
    }

    /**
     * Méthode utilitaire pour forcer le style des boutons de navigation des onglets
     * Applique directement en Java le style #6B71F2 pour les boutons de navigation
     */
    public static void forceTabNavigationButtonsStyle(TabPane tabPane) {
        // Multiple délais pour s'assurer que tous les éléments sont rendus
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                Platform.runLater(() -> { // Triple runLater pour être sûr
                    System.out.println("🎨 Forçage du style des boutons de navigation des onglets...");
                    
                    // Rechercher TOUS les éléments possibles dans le TabPane
                    String[] selectors = {
                        ".tab-header-area", ".headers-region", ".control-buttons-tab",
                        ".tab-down-button", ".increment-button", ".decrement-button",
                        ".left-arrow", ".right-arrow", ".scroll-arrows-visible",
                        ".button", ".arrow", "Button", "StackPane", "Region"
                    };
                    
                    for (String selector : selectors) {
                        tabPane.lookupAll(selector).forEach(node -> {
                            String nodeClass = node.getClass().getSimpleName();
                            System.out.println("📍 Trouvé élément: " + nodeClass + " avec sélecteur: " + selector);
                            
                            if (selector.contains("arrow") || nodeClass.contains("Arrow")) {
                                // Style spécial pour les flèches
                                String arrowStyle = "-fx-background-color: #6B71F2 !important; " +
                                                  "-fx-shape: \"M 0 0 h 7 l -3.5 4 z\" !important;";
                                node.setStyle(arrowStyle);
                                System.out.println("➤ Flèche stylée en #6B71F2");
                            } else if (selector.contains("button") || nodeClass.contains("Button")) {
                                // Style pour les boutons
                                String buttonStyle = "-fx-background-color: #091326 !important; " +
                                                   "-fx-text-fill: #6B71F2 !important; " +
                                                   "-fx-border-color: #6B71F2 !important; " +
                                                   "-fx-border-width: 1px !important; " +
                                                   "-fx-border-radius: 3px !important; " +
                                                   "-fx-background-radius: 3px !important;";
                                node.setStyle(buttonStyle);
                                System.out.println("🔘 Bouton stylé avec bordure #6B71F2");
                            } else {
                                // Style général pour conteneurs
                                String containerStyle = "-fx-background-color: #091326 !important;";
                                node.setStyle(containerStyle);
                                System.out.println("📦 Conteneur stylé en #091326");
                            }
                        });
                    }
                    
                    // Force absolue - parcourir TOUS les nodes sans exception
                    tabPane.lookupAll("*").forEach(node -> {
                        String nodeType = node.getClass().getSimpleName();
                        String styleClasses = node.getStyleClass().toString();
                        
                        // Détecter les types de navigation par nom de classe
                        if (nodeType.toLowerCase().contains("button") || 
                            nodeType.toLowerCase().contains("arrow") ||
                            styleClasses.contains("button") ||
                            styleClasses.contains("arrow") ||
                            styleClasses.contains("control") ||
                            styleClasses.contains("increment") ||
                            styleClasses.contains("decrement")) {
                            
                            String forceStyle = "-fx-background-color: #091326 !important; " +
                                               "-fx-text-fill: #6B71F2 !important; " +
                                               "-fx-border-color: #6B71F2 !important; " +
                                               "-fx-border-width: 1px !important;";
                            node.setStyle(forceStyle);
                            System.out.println("🎯 Force absolue appliquée sur: " + nodeType);
                        }
                        
                        // Traitement SPÉCIAL pour TabControlButtons
                        if (nodeType.equals("TabControlButtons")) {
                            String tabControlStyle = "-fx-background-color: #091326 !important; " +
                                                   "-fx-border-color: #6B71F2 !important; " +
                                                   "-fx-border-width: 2px !important; " +
                                                   "-fx-border-radius: 5px !important; " +
                                                   "-fx-background-radius: 5px !important;";
                            node.setStyle(tabControlStyle);
                            System.out.println("🎯🎯 TabControlButtons SPÉCIALEMENT stylé !");
                            
                            // Stylér tous les enfants du TabControlButtons
                            if (node instanceof javafx.scene.Parent) {
                                javafx.scene.Parent parent = (javafx.scene.Parent) node;
                                parent.getChildrenUnmodifiable().forEach(child -> {
                                    String childStyle = "-fx-background-color: #091326 !important; " +
                                                       "-fx-text-fill: #6B71F2 !important; " +
                                                       "-fx-border-color: #6B71F2 !important; " +
                                                       "-fx-border-width: 1px !important;";
                                    child.setStyle(childStyle);
                                    System.out.println("🔧 Enfant de TabControlButtons stylé: " + child.getClass().getSimpleName());
                                });
                            }
                        }
                    });
                    
                    System.out.println("✅ Forçage terminé pour TabPane");
                });
            });
        });
    }

    /**
     * Force TOUS les TextField d'une scene à utiliser les bonnes couleurs
     * Méthode à appeler après création de chaque vue
     */
    public static void forceAllTextFieldsColors(Scene scene) {
        if (scene == null) return;
        
        Platform.runLater(() -> {
            scene.getRoot().lookupAll(".text-field").forEach(node -> {
                if (node instanceof TextField) {
                    forceSearchFieldColors((TextField) node);
                }
            });
            
            // Réapplication périodique pour les vues chargées dynamiquement
            Platform.runLater(() -> {
                scene.getRoot().lookupAll(".text-field").forEach(node -> {
                    if (node instanceof TextField) {
                        forceSearchFieldColors((TextField) node);
                    }
                });
            });
        });
    }

    public static void main(String[] args) {
        System.out.println("\uD83D\uDE80 Demarrage MAGSAV-3.0 Desktop avec Java " + System.getProperty("java.version"));
        launch(args);
    }
}
