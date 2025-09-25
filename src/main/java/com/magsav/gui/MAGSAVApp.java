package com.magsav.gui;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.service.SAVService;
import com.magsav.imports.CSVImporter;
import com.magsav.model.DossierSAV;
import com.magsav.repo.DossierSAVRepository;
import com.zaxxer.hikari.HikariDataSource;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class MAGSAVApp extends Application {
    
    private DossierSAVRepository dossierRepo;
    private SAVService savService;
    private CSVImporter csvImporter;
    
    private TableView<DossierSAV> tableView;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Label statusLabel;
    private ObservableList<DossierSAV> dossiersList;
    
    @Override
    public void start(Stage primaryStage) {
        try {
            initializeServices();
            
            BorderPane root = new BorderPane();
            
            // Panel supérieur avec recherche
            VBox topPanel = createTopPanel();
            root.setTop(topPanel);
            
            // Table centrale
            tableView = createTableView();
            root.setCenter(tableView);
            
            // Panel inférieur avec boutons
            HBox bottomPanel = createBottomPanel();
            root.setBottom(bottomPanel);
            
            Scene scene = new Scene(root, 1200, 800);
            
            primaryStage.setTitle("MAGSAV - Gestion SAV");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            // Charger les données au démarrage
            loadData();
            
            primaryStage.show();
            
            System.out.println("Interface JavaFX lancée avec succès!");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du démarrage: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de démarrage", "Impossible de démarrer l'application: " + e.getMessage());
        }
    }
    
    private void initializeServices() throws Exception {
        Config config = new Config();
        Path configPath = Path.of("application.yml");
        if (configPath.toFile().exists()) {
            config = Config.load(configPath);
        }
        
        String dbUrl = config.get("app.database.url", "jdbc:sqlite:magsav.db");
        if (!dbUrl.startsWith("jdbc:")) {
            dbUrl = "jdbc:sqlite:" + dbUrl;
        }
        
        HikariDataSource dataSource = DB.init(dbUrl);
        
        this.dossierRepo = new DossierSAVRepository(dataSource);
        this.savService = new SAVService(dataSource);
        this.csvImporter = new CSVImporter(dataSource);
    }
    
    private VBox createTopPanel() {
        VBox topPanel = new VBox(10);
        topPanel.setPadding(new Insets(10));
        
        Label titleLabel = new Label("MAGSAV - Gestion SAV");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox searchPanel = new HBox(10);
        searchPanel.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Recherche...");
        searchField.setPrefWidth(200);
        
        statusFilter = new ComboBox<>(FXCollections.observableArrayList(
            "Tous", "En attente", "En cours", "Terminé", "Annulé"
        ));
        statusFilter.setValue("Tous");
        
        Button searchButton = new Button("Rechercher");
        searchButton.setOnAction(e -> handleSearch());
        
        searchPanel.getChildren().addAll(
            new Label("Recherche:"), searchField,
            new Label("Statut:"), statusFilter,
            searchButton
        );
        
        topPanel.getChildren().addAll(titleLabel, searchPanel);
        return topPanel;
    }
    
    private TableView<DossierSAV> createTableView() {
        tableView = new TableView<>();
        
        TableColumn<DossierSAV, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        
        TableColumn<DossierSAV, String> colProprietaire = new TableColumn<>("Propriétaire");
        colProprietaire.setCellValueFactory(new PropertyValueFactory<>("proprietaire"));
        colProprietaire.setPrefWidth(150);
        
        TableColumn<DossierSAV, String> colAppareil = new TableColumn<>("Appareil");
        colAppareil.setCellValueFactory(cellData -> {
            DossierSAV dossier = cellData.getValue();
            String appareil = String.format("%s (%s)", dossier.produit(), dossier.numeroSerie());
            return new SimpleStringProperty(appareil);
        });
        colAppareil.setPrefWidth(200);
        
        TableColumn<DossierSAV, String> colProbleme = new TableColumn<>("Problème");
        colProbleme.setCellValueFactory(new PropertyValueFactory<>("panne"));
        colProbleme.setPrefWidth(250);
        
        TableColumn<DossierSAV, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colStatut.setPrefWidth(100);
        
        TableColumn<DossierSAV, String> colDateCreation = new TableColumn<>("Date création");
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));
        colDateCreation.setPrefWidth(120);
        
        TableColumn<DossierSAV, String> colDateReparation = new TableColumn<>("Date réparation");
        colDateReparation.setCellValueFactory(new PropertyValueFactory<>("dateSortie"));
        colDateReparation.setPrefWidth(120);
        
        // Correction de l'avertissement unchecked - ajout explicite des colonnes
        tableView.getColumns().add(colId);
        tableView.getColumns().add(colProprietaire);
        tableView.getColumns().add(colAppareil);
        tableView.getColumns().add(colProbleme);
        tableView.getColumns().add(colStatut);
        tableView.getColumns().add(colDateCreation);
        tableView.getColumns().add(colDateReparation);
        
        dossiersList = FXCollections.observableArrayList();
        tableView.setItems(dossiersList);
        
        return tableView;
    }
    
    private HBox createBottomPanel() {
        HBox bottomPanel = new HBox(10);
        bottomPanel.setPadding(new Insets(10));
        bottomPanel.setAlignment(Pos.CENTER_LEFT);
        
        Button importButton = new Button("📁 Importer CSV");
        importButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        importButton.setOnAction(e -> handleImport());
        
        Button statusButton = new Button("📝 Changer Statut");
        statusButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        statusButton.setOnAction(e -> handleStatusChange());
        
        Button labelButton = new Button("🏷️ Générer Étiquette");
        labelButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;");
        labelButton.setOnAction(e -> handleLabelGeneration());
        
        Button refreshButton = new Button("⟳ Actualiser");
        refreshButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        refreshButton.setOnAction(e -> loadData());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        statusLabel = new Label("Prêt");
        
        bottomPanel.getChildren().addAll(importButton, statusButton, labelButton, refreshButton, spacer, statusLabel);
        return bottomPanel;
    }
    
    private void loadData() {
        statusLabel.setText("Chargement des données...");
        
        Task<List<DossierSAV>> task = new Task<List<DossierSAV>>() {
            @Override
            protected List<DossierSAV> call() throws Exception {
                return dossierRepo.findAll();
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    dossiersList.clear();
                    dossiersList.addAll(getValue());
                    statusLabel.setText(String.format("Chargé: %d dossiers", getValue().size()));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Erreur de chargement", getException().getMessage());
                    statusLabel.setText("Erreur de chargement");
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void handleSearch() {
        // Implémentation simplifiée pour le test
        statusLabel.setText("Recherche en cours...");
        loadData();  // Pour l'instant, on recharge tout
    }
    
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        
        File selectedFile = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        if (selectedFile != null) {
            statusLabel.setText("Import en cours...");
            showInfo("Import", "Fonctionnalité d'import disponible - fichier sélectionné: " + selectedFile.getName());
            statusLabel.setText("Import simulé");
        }
    }
    
    private void handleStatusChange() {
        DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
            return;
        }
        
        showInfo("Changement de statut", "Fonctionnalité disponible pour le dossier #" + selected.id());
    }
    
    private void handleLabelGeneration() {
        DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
            return;
        }
        
        showInfo("Génération d'étiquette", "Fonctionnalité disponible pour le dossier #" + selected.id());
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        // Configuration très agressive pour macOS Apple Silicon avec JavaFX 17
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.verbose", "false");
        System.setProperty("prism.forceGPU", "false");
        System.setProperty("prism.forcePowerOfTwo", "false");
        System.setProperty("prism.useTouchInput", "false");
        
        // Désactiver complètement l'accélération et les optimisations
        System.setProperty("glass.accessible.force", "false");
        System.setProperty("com.apple.macos.useScreenMenuBar", "false");
        System.setProperty("javafx.platform.macos.useInheritedChannels", "false");
        System.setProperty("javafx.animation.fullspeed", "false");
        System.setProperty("javafx.pulseLogger", "false");
        
        // Mode de compatibilité maximale
        System.setProperty("prism.allowhidpi", "false");
        System.setProperty("glass.win.uiScale", "1.0");
        System.setProperty("glass.gtk.uiScale", "1.0");
        
        // Désactiver les animations et effets
        System.setProperty("javafx.animation.pulse", "60");
        System.setProperty("javafx.scene.control.skin.MSAA", "false");
        
        System.out.println("Démarrage MAGSAV avec JavaFX " + System.getProperty("javafx.version", "17") + " sur macOS");
        System.out.println("Mode de compatibilité Apple Silicon activé");
        
        try {
            launch(args);
        } catch (Exception e) {
            System.err.println("Erreur lors du lancement de l'interface graphique :");
            e.printStackTrace();
            System.err.println("\nLe problème NSTrackingRectTag est connu sur macOS Apple Silicon.");
            System.err.println("Utilisez l'interface CLI avec: ./gradlew runCli");
        }
    }
}