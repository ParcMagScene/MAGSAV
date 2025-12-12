package com.magscene.magsav.desktop;

import com.magscene.magsav.desktop.component.GlobalSearchComponent;
import com.magscene.magsav.desktop.core.di.ApplicationContext;
import com.magscene.magsav.desktop.core.navigation.NavigationManager;
import com.magscene.magsav.desktop.core.navigation.Route;
import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Application JavaFX 21 pour MAGSAV-3.0 - Version Refactorisée
 * 
 * Cette version utilise la nouvelle architecture avec :
 * - Injection de dépendances (ApplicationContext)
 * - Navigation centralisée (NavigationManager)
 * - Configuration centralisée (ApplicationConfig)
 * 
 * @version 3.0.0-refactored
 */
public class MagsavDesktopApplication extends Application {

    private static MagsavDesktopApplication instance;

    // Architecture refactorisée
    private ApplicationContext applicationContext;
    private NavigationManager navigationManager;

    // UI Components
    private Stage primaryStage;
    private StackPane mainContent;
    private Label statusLabel;
    private Label pageTitleLabel; // Titre de la page courante dans le header
    private Button[] navigationButtons;

    @Override
    public void start(Stage primaryStage) {
        try {
            instance = this;
            this.primaryStage = primaryStage;

            System.out.println("Application MAGSAV 3.0 - Démarrage avec architecture refactorisée");

            // Initialisation de l'architecture
            initializeArchitecture();

            // Construction de l'interface
            buildUserInterface();

            // Finalisation
            finalizeApplication();

            System.out.println("Application MAGSAV 3.0 - Démarrage réussi");

        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur de démarrage", "Impossible de démarrer l'application: " + e.getMessage());
            Platform.exit();
        }
    }

    /**
     * Initialisation de l'architecture refactorisée
     */
    private void initializeArchitecture() {
        System.out.println("Initialisation de l'architecture...");

        // Initialisation du contexte d'application (DI Container)
        applicationContext = ApplicationContext.getInstance();
        System.out.println("ApplicationContext initialisé");

        // DESACTIVÉ : Plus de données de test - Utilisation exclusive du backend
        // System.out.println("🧪 Initialisation des données de test...");
        // com.magscene.magsav.desktop.service.TestDataService.getInstance().initialize();
        System.out.println("⚡ Mode BACKEND ONLY - Connexion à http://localhost:8080");

        // Récupération du NavigationManager via injection
        navigationManager = applicationContext.getInstance(NavigationManager.class);
        System.out.println("NavigationManager récupéré");

        // Enregistrement des factories de vues
        registerViewFactories();
    }

    /**
     * Enregistrement des factories de vues dans le NavigationManager
     */
    private void registerViewFactories() {
        // Dashboard
        navigationManager.registerView(Route.DASHBOARD, () -> {
            try {
                return new com.magscene.magsav.desktop.view.DashboardView();
            } catch (Exception e) {
                return createErrorPane("Dashboard", e);
            }
        });

        // Equipment - Nouvelle vue refactorisée
        navigationManager.registerView(Route.EQUIPMENT, () -> {
            try {
                return new com.magscene.magsav.desktop.view.equipment.NewEquipmentManagerView();
            } catch (Exception e) {
                return createErrorPane("Équipements", e);
            }
        });

        // SAV - Nouvelle vue refactorisée
        navigationManager.registerView(Route.SAV, () -> {
            try {
                return new com.magscene.magsav.desktop.view.sav.NewSAVManagerView();
            } catch (Exception e) {
                return createErrorPane("SAV", e);
            }
        });

        // Clients
        navigationManager.registerView(Route.CLIENTS, () -> {
            try {
                // Utilisation temporaire d'un service factice - à remplacer par un vrai service
                // client
                com.magscene.magsav.desktop.service.ApiService apiService = new com.magscene.magsav.desktop.service.ApiService();
                return new com.magscene.magsav.desktop.view.ClientManagerView(apiService);
            } catch (Exception e) {
                return createErrorPane("Clients", e);
            }
        });

        // Ventes & Installations (avec onglets Projets et Contrats)
        navigationManager.registerView(Route.SALES, () -> {
            try {
                com.magscene.magsav.desktop.service.ApiService apiService = new com.magscene.magsav.desktop.service.ApiService();
                return new com.magscene.magsav.desktop.view.salesinstallation.SalesInstallationTabsView(apiService);
            } catch (Exception e) {
                return createErrorPane("Ventes", e);
            }
        });

        // Véhicules
        navigationManager.registerView(Route.VEHICLES, () -> {
            try {
                com.magscene.magsav.desktop.service.ApiService apiService = new com.magscene.magsav.desktop.service.ApiService();
                return new com.magscene.magsav.desktop.view.VehicleManagerView(apiService);
            } catch (Exception e) {
                return createErrorPane("Véhicules", e);
            }
        });

        // Personnel
        navigationManager.registerView(Route.PERSONNEL, () -> {
            try {
                com.magscene.magsav.desktop.service.ApiService apiService = new com.magscene.magsav.desktop.service.ApiService();
                return new com.magscene.magsav.desktop.view.PersonnelManagerView(apiService);
            } catch (Exception e) {
                return createErrorPane("Personnel", e);
            }
        });

        // Planning
        navigationManager.registerView(Route.PLANNING, () -> {
            try {
                com.magscene.magsav.desktop.service.ApiService apiService = new com.magscene.magsav.desktop.service.ApiService();
                return new com.magscene.magsav.desktop.view.planning.PlanningManagerView(apiService);
            } catch (Exception e) {
                return createErrorPane("Planning", e);
            }
        });

        // Fournisseurs
        navigationManager.registerView(Route.SUPPLIERS, () -> {
            try {
                return (Pane) new com.magscene.magsav.desktop.view.supplier.SupplierManagerViewSimple();
            } catch (Exception e) {
                return createErrorPane("Fournisseurs", e);
            }
        });

        // Demandes de matériel
        navigationManager.registerView(Route.MATERIAL_REQUESTS, () -> {
            try {
                return (Pane) new com.magscene.magsav.desktop.view.supplier.MaterialRequestManagerViewSimple();
            } catch (Exception e) {
                return createErrorPane("Demandes Matériel", e);
            }
        });

        // Commandes groupées
        navigationManager.registerView(Route.GROUPED_ORDERS, () -> {
            try {
                return (Pane) new com.magscene.magsav.desktop.view.supplier.GroupedOrderManagerViewSimple();
            } catch (Exception e) {
                return createErrorPane("Commandes Groupées", e);
            }
        });

        // Paramètres
        navigationManager.registerView(Route.SETTINGS, () -> {
            try {
                return new com.magscene.magsav.desktop.view.settings.SettingsView();
            } catch (Exception e) {
                return createErrorPane("Paramètres", e);
            }
        });

        System.out.println("Factories de vues enregistrées avec système de fournisseurs");
    }

    /**
     * Construction de l'interface utilisateur
     */
    private void buildUserInterface() {
        System.out.println("Construction de l'interface...");

        // Configuration de la fenêtre principale
        setupMainWindow();

        // Création du layout principal
        BorderPane root = createMainLayout();

        // Création de la scène
        Scene scene = new Scene(root);
        applyTheme(scene);

        // Configuration finale
        primaryStage.setScene(scene);
        primaryStage.show();

        // Configuration des hooks de fermeture
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            showExitConfirmation();
        });

        // Navigation initiale vers le dashboard
        Platform.runLater(() -> {
            navigationManager.navigateTo(Route.DASHBOARD);
            if (navigationButtons.length > 0) {
                updateActiveButton(navigationButtons[0]);
            }
            updatePageTitle(Route.DASHBOARD.getDisplayName());
            
            // Pré-chargement des vues principales pour enregistrer les SearchProviders
            preloadMainViews();
        });

        System.out.println("Interface utilisateur construite");
    }
    
    /**
     * Pré-charge les vues principales en arrière-plan pour enregistrer les SearchProviders
     */
    private void preloadMainViews() {
        // Charger en arrière-plan les vues qui implémentent SearchProvider
        new Thread(() -> {
            try {
                // Petit délai pour laisser le dashboard se charger
                Thread.sleep(500);
                
                Platform.runLater(() -> {
                    System.out.println("🔍 Pré-chargement des SearchProviders...");
                    
                    // Charger la vue Équipements (force l'enregistrement du SearchProvider)
                    try {
                        navigationManager.navigateTo(Route.EQUIPMENT);
                        System.out.println("   ✅ Équipements chargé");
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erreur chargement Équipements: " + e.getMessage());
                    }
                    
                    // Charger la vue SAV
                    try {
                        navigationManager.navigateTo(Route.SAV);
                        System.out.println("   ✅ SAV chargé");
                    } catch (Exception e) {
                        System.err.println("   ⚠️ Erreur chargement SAV: " + e.getMessage());
                    }
                    
                    // Revenir au dashboard
                    navigationManager.navigateTo(Route.DASHBOARD);
                    if (navigationButtons.length > 0) {
                        updateActiveButton(navigationButtons[0]);
                    }
                    updatePageTitle(Route.DASHBOARD.getDisplayName());
                    
                    System.out.println("🔍 SearchProviders prêts pour la recherche globale");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "SearchProvider-Preloader").start();
    }

    /**
     * Configuration de la fenêtre principale
     */
    private void setupMainWindow() {
        primaryStage.setTitle("MAGSAV 3.0 - Architecture Refactorisée");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        // Service de mémorisation des préférences de fenêtre
        WindowPreferencesService prefsService = WindowPreferencesService.getInstance();

        // Restaurer la position et taille sauvegardées, ou utiliser les valeurs par
        // défaut
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        prefsService.restoreWindowBounds(
                primaryStage,
                "main-window",
                screenBounds.getWidth(),
                screenBounds.getHeight());

        // Activer la sauvegarde automatique lors des changements
        prefsService.setupAutoSave(primaryStage, "main-window");

        System.out.println("💾 Mémorisation fenêtre activée");
    }

    /**
     * Création du layout principal
     */
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-layout");

        // Barre d'outils utilisateur en haut
        HBox userToolbar = createUserToolbar();
        root.setTop(userToolbar);

        // Panel de navigation à gauche
        VBox navigationPanel = createNavigationPanel();
        root.setLeft(navigationPanel);

        // Contenu principal au centre
        mainContent = createMainContent();
        root.setCenter(mainContent);

        return root;
    }

    /**
     * Création du panel de navigation
     */
    private VBox createNavigationPanel() {
        VBox navPanel = new VBox(10);
        navPanel.getStyleClass().add("navigation-panel");

        // Création des boutons de navigation
        navigationButtons = createNavigationButtons();

        for (Button button : navigationButtons) {
            navPanel.getChildren().add(button);
        }

        return navPanel;
    }

    /**
     * Création des boutons de navigation
     */
    private Button[] createNavigationButtons() {
        Route[] routes = {
                Route.DASHBOARD, Route.SAV, Route.EQUIPMENT,
                Route.CLIENTS, Route.SALES,
                Route.VEHICLES, Route.PERSONNEL, Route.PLANNING,
                Route.SUPPLIERS, Route.MATERIAL_REQUESTS, Route.GROUPED_ORDERS,
                Route.SETTINGS
        };

        Button[] buttons = new Button[routes.length];

        for (int i = 0; i < routes.length; i++) {
            Route route = routes[i];
            Button button = new Button(route.getDisplayName());
            button.setMaxWidth(Double.MAX_VALUE);
            button.getStyleClass().add("nav-button");
            button.setStyle(ThemeConstants.BUTTON_STYLE);

            // Navigation via NavigationManager
            button.setOnAction(e -> {
                updateActiveButton(button);
                navigationManager.navigateTo(route);
                updateStatus("Navigation: " + route.getDisplayName());
                updatePageTitle(route.getDisplayName());
            });

            buttons[i] = button;
        }

        return buttons;
    }

    /**
     * Création du contenu principal
     */
    private StackPane createMainContent() {
        StackPane content = new StackPane();
        content.getStyleClass().add("main-content");
        // content supprimé - Style géré par CSS
        navigationManager.setViewChangeHandler(view -> {
            Platform.runLater(() -> {
                content.getChildren().clear();
                content.getChildren().add(view);

                // S'assurer que la vue prend tout l'espace disponible
                if (view instanceof Region) {
                    Region region = (Region) view;
                    region.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                    region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                }

                // Forcer un refresh de la mise en page
                content.layout();
            });
        });

        return content;
    }

    /**
     * Application du thème
     */
    private void applyTheme(Scene scene) {
        // Application du thème unifié
        UnifiedThemeManager themeManager = UnifiedThemeManager.getInstance();
        themeManager.applyThemeToScene(scene);

        // Gestionnaire de fermeture
        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            exitApplication();
        });
    }

    /**
     * Finalisation de l'application
     */
    private void finalizeApplication() {
        System.out.println("Finalisation de l'application...");

        // Test de connectivité backend
        testBackendConnectivity();

        // Configuration des hooks de fermeture
        setupShutdownHooks();

        System.out.println("Application finalisée");
    }

    /**
     * Test de connectivité avec le backend
     */
    private void testBackendConnectivity() {
        try {
            var equipmentService = applicationContext.getInstance(
                    com.magscene.magsav.desktop.service.business.EquipmentService.class);

            equipmentService.testBackendConnection()
                    .thenAccept(connected -> Platform.runLater(() -> {
                        if (connected) {
                            updateStatus("Backend connecté");
                        } else {
                            updateStatus("Backend non disponible");
                        }
                    }))
                    .exceptionally(error -> {
                        Platform.runLater(() -> updateStatus("Erreur backend: " + error.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            System.err.println("Erreur test backend: " + e.getMessage());
            updateStatus("Test backend échoué");
        }
    }

    /**
     * Configuration des hooks de fermeture
     */
    private void setupShutdownHooks() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Nettoyage des ressources...");
            if (applicationContext != null) {
                applicationContext.clear();
            }
            System.out.println("Nettoyage terminé");
        }));
    }

    // === Méthodes utilitaires === //

    /**
     * Met à jour le bouton actif
     */
    private void updateActiveButton(Button activeButton) {
        for (Button button : navigationButtons) {
            button.setStyle(ThemeConstants.BUTTON_STYLE);
        }
        activeButton.setStyle(
                ThemeConstants.BUTTON_STYLE + " -fx-background-color: #007acc; -fx-text-fill: white;");
    }

    /**
     * Met à jour le statut
     */
    private void updateStatus(String message) {
        if (statusLabel != null) {
            Platform.runLater(() -> statusLabel.setText(message));
        }
        System.out.println("Status: " + message);
    }

    /**
     * Met à jour le titre de la page dans le header
     */
    private void updatePageTitle(String title) {
        if (pageTitleLabel != null) {
            Platform.runLater(() -> pageTitleLabel.setText(title));
        }
    }

    /**
     * Fermeture de l'application
     */
    private void exitApplication() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quitter MAGSAV 3.0 ?");
        alert.setContentText("Êtes-vous sûr de vouloir quitter l'application ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    /**
     * Création de la barre d'outils utilisateur
     */
    private HBox createUserToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5, 10, 5, 10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("user-toolbar");

        // Logo ou titre de l'application
        Label appTitle = new Label("MAGSAV 3.0");
        appTitle.getStyleClass().add("app-title");
        appTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Séparateur vertical
        Separator separator = new Separator();
        separator.setOrientation(javafx.geometry.Orientation.VERTICAL);

        // Composant de recherche globale
        GlobalSearchComponent globalSearch = new GlobalSearchComponent();

        // Spacer pour centrer le titre après la recherche
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        // Titre de la page courante (centré)
        pageTitleLabel = new Label("");
        pageTitleLabel.getStyleClass().add("page-title");
        pageTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        pageTitleLabel.setAlignment(Pos.CENTER);

        // Spacer pour pousser les boutons à droite
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        // Bouton paramètres
        Button settingsButton = new Button("⚙️");
        settingsButton.setTooltip(new Tooltip("Paramètres"));
        settingsButton.getStyleClass().add("icon-button");
        settingsButton.setOnAction(e -> {
            navigationManager.navigateTo(Route.SETTINGS);
            updateStatus("Navigation: Paramètres");
        });

        toolbar.getChildren().addAll(appTitle, separator, globalSearch, leftSpacer, pageTitleLabel, rightSpacer,
                settingsButton);
        return toolbar;
    }

    /**
     * Affiche la confirmation de sortie
     */
    private void showExitConfirmation() {
        // Mode développement: sortie immédiate sans confirmation
        if (com.magscene.magsav.desktop.config.DevModeConfig.shouldAutoApproveExit()) {
            Platform.exit();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quitter MAGSAV 3.0 ?");
        alert.setContentText("Êtes-vous sûr de vouloir quitter l'application ?");

        // Mémoriser la position et taille du dialog de confirmation
        try {
            WindowPreferencesService prefsService = applicationContext.getInstance(WindowPreferencesService.class);
            prefsService.setupDialogMemory(alert.getDialogPane(), "exit-confirmation-dialog");
        } catch (Exception e) {
            // Si le service n'est pas disponible, continuer sans mémorisation
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    /**
     * Affiche un dialog d'erreur
     */
    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Crée un panneau d'erreur
     */
    private Pane createErrorPane(String viewName, Exception error) {
        VBox errorPane = new VBox(10);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        errorPane.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Erreur: " + viewName);
        titleLabel.setStyle(ThemeConstants.ERROR_MESSAGE_STYLE);

        Label messageLabel = new Label("Impossible de charger la vue: " + error.getMessage());
        messageLabel.setStyle(ThemeConstants.INFO_MESSAGE_STYLE);
        messageLabel.setWrapText(true);

        errorPane.getChildren().addAll(titleLabel, messageLabel);
        return errorPane;
    }

    /**
     * Crée un panneau par défaut
     */
    private Pane createDefaultPane(String moduleName) {
        VBox defaultPane = new VBox(10);
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        defaultPane.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(moduleName);
        titleLabel.setStyle(ThemeConstants.LARGE_TITLE_STYLE);

        Label messageLabel = new Label("Module en cours de migration vers la nouvelle architecture");
        messageLabel.setStyle(ThemeConstants.INFO_MESSAGE_STYLE);

        defaultPane.getChildren().addAll(titleLabel, messageLabel);
        return defaultPane;
    }

    // === Méthodes statiques === //

    public static MagsavDesktopApplication getInstance() {
        return instance;
    }

    /**
     * Méthode utilitaire publique pour forcer les couleurs des TextField
     * Utilisée par tous les modules pour uniformiser les couleurs des champs de
     * recherche
     */
    public static void forceSearchFieldColors(TextField textField) {
        // Style dynamique pour contrer toute surcharge CSS
        String bgColor = UnifiedThemeManager.getInstance().getCurrentBackgroundColor();
        String forceStyle = "-fx-base: " + bgColor + " !important; " +
                "-fx-background: " + bgColor + " !important; " +
                "-fx-background-color: " + bgColor + " !important; " +
                "-fx-control-inner-background: " + bgColor + " !important; " +
                "-fx-control-inner-background-alt: " + bgColor + " !important; " +
                "-fx-text-fill: #8B91FF !important; " +
                "-fx-text-base-color: #8B91FF !important; " +
                "-fx-prompt-text-fill: #8B91FF !important;";

        textField.setStyle(forceStyle);

        // Force ABSOLUE sur tous les sous-éléments avec délai pour le rendu
        Platform.runLater(() -> {
            Platform.runLater(() -> { // Double Platform.runLater pour être sûr
                textField.setStyle(forceStyle);
                textField.lookupAll(".text").forEach(node -> {
                    node.setStyle(forceStyle);
                });
            });
        });
    }

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage MAGSAV 3.0 - Architecture refactorisée");
        launch(args);
    }
}