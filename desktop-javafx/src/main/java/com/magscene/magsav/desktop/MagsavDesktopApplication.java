package com.magscene.magsav.desktop;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.view.ClientManagerView;
import com.magscene.magsav.desktop.view.ContractManagerView;
import com.magscene.magsav.desktop.view.EquipmentManagerView;
import com.magscene.magsav.desktop.view.ServiceRequestManagerView;
import com.magscene.magsav.desktop.view.PersonnelManagerView;
import com.magscene.magsav.desktop.view.VehicleManagerView;
import com.magscene.magsav.desktop.view.salesinstallation.ProjectManagerView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Application JavaFX 21 pour MAGSAV-3.0
 * Interface desktop moderne avec Java 21 LTS connectée à l'API Backend
 */
public class MagsavDesktopApplication extends Application {

    private ApiService apiService;
    private StackPane mainContent;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialisation des services
        apiService = new ApiService();
        
        primaryStage.setTitle("MAGSAV-3.0 - Système SAV & Parc Matériel");

        // Layout principal
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Menu latéral 
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Contenu principal
        mainContent = createMainContent();
        root.setCenter(mainContent);

        // Status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // Scene avec thème moderne
        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/styles/magsav-theme.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Test de connectivité au démarrage
        testBackendConnection();

        // Fermeture propre
        primaryStage.setOnCloseRequest(e -> {
            apiService.close();
            Platform.exit();
        });
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(10));
        
        Label title = new Label("🏢 MAGSAV-3.0");
        title.getStyleClass().add("app-title");
        
        Label subtitle = new Label("Système de Gestion SAV et Parc Matériel - Java 21 LTS");
        subtitle.getStyleClass().add("app-subtitle");
        
        header.getChildren().addAll(title, subtitle);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(250);

        Label menuTitle = new Label("📋 MODULES");
        menuTitle.getStyleClass().add("menu-title");

        Button btnSAV = new Button("🔧 SAV & Interventions");
        Button btnEquipment = new Button("📦 Parc Matériel");
        Button btnClients = new Button("👥 Clients");
        Button btnContracts = new Button("📋 Contrats");
        Button btnSales = new Button("💼 Ventes & Installations");
        Button btnVehicles = new Button("🚐 Véhicules");
        Button btnPersonnel = new Button("👤 Personnel");
        Button btnSettings = new Button("⚙️ Paramètres");

        // Style des boutons
        for (Button btn : new Button[]{btnSAV, btnEquipment, btnClients, btnContracts, btnSales, btnVehicles, btnPersonnel, btnSettings}) {
            btn.getStyleClass().add("menu-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(40);
        }

        // Actions
        btnEquipment.setOnAction(e -> showEquipmentModule());
        btnSAV.setOnAction(e -> showSAVModule());
        btnClients.setOnAction(e -> showClientModule());
        btnContracts.setOnAction(e -> showContractModule());
        btnSales.setOnAction(e -> showSalesModule());
        btnVehicles.setOnAction(e -> showVehicleModule());
        btnPersonnel.setOnAction(e -> showPersonnelModule());

        sidebar.getChildren().addAll(
            menuTitle,
            new Separator(),
            btnSAV,
            btnEquipment,
            btnClients,
            btnContracts,
            btnSales,
            btnVehicles,
            btnPersonnel,
            new Separator(),
            btnSettings
        );

        return sidebar;
    }

    private StackPane createMainContent() {
        StackPane content = new StackPane();
        content.getStyleClass().add("main-content");
        
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
            new Label("📱 QR Codes pour inventaire intelligent"),
            new Label("🌐 API REST intégrée"),
            new Label("💾 Base de données H2 embarquée"),
            new Label("🔒 Interface sécurisée")
        );
        
        Button btnStartEquipment = new Button("🚀 Commencer avec le Parc Matériel");
        btnStartEquipment.getStyleClass().add("start-button");
        btnStartEquipment.setOnAction(e -> showEquipmentModule());
        
        welcomeView.getChildren().addAll(welcome, javaInfo, features, btnStartEquipment);
        content.getChildren().add(welcomeView);
        
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
     * Test de connexion au backend au démarrage
     */
    private void testBackendConnection() {
        Task<Boolean> connectionTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return apiService.testConnection().get();
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
        // Afficher l'interface complète de gestion du parc matériel
        EquipmentManagerView equipmentView = new EquipmentManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(equipmentView);
    }

    private void showSAVModule() {
        // Chargement de la vue SAV complète
        ServiceRequestManagerView savView = new ServiceRequestManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(savView);
    }

    private void showClientModule() {
        // Chargement de la vue Clients complète
        ClientManagerView clientView = new ClientManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(clientView);
    }

    private void showContractModule() {
        // Chargement de la vue Contrats complète
        ContractManagerView contractView = new ContractManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(contractView);
    }

    private void showPersonnelModule() {
        // Chargement de la vue Personnel complète
        PersonnelManagerView personnelView = new PersonnelManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(personnelView);
    }

    private void showVehicleModule() {
        // Chargement de la vue Véhicules complète
        VehicleManagerView vehicleView = new VehicleManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(vehicleView);
    }

    private void showSalesModule() {
        // Chargement de la vue Ventes & Installations
        ProjectManagerView salesView = new ProjectManagerView(apiService);
        
        mainContent.getChildren().clear();
        mainContent.getChildren().add(salesView);
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

    public static void main(String[] args) {
        System.out.println("🚀 Démarrage MAGSAV-3.0 Desktop avec Java " + System.getProperty("java.version"));
        launch(args);
    }
}