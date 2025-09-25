package com.magsav.gui;

import com.magsav.config.Config;
import com.magsav.db.DB;
import com.magsav.service.SAVService;
import com.magsav.imports.CSVImporter;
import com.magsav.model.DossierSAV;
import com.magsav.repo.DossierSAVRepository;
import com.zaxxer.hikari.HikariDataSource;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    
    @FXML private TableView<DossierSAV> tableView;
    @FXML private TableColumn<DossierSAV, Long> colId;
    @FXML private TableColumn<DossierSAV, String> colProprietaire;
    @FXML private TableColumn<DossierSAV, String> colAppareil;
    @FXML private TableColumn<DossierSAV, String> colProbleme;
    @FXML private TableColumn<DossierSAV, String> colStatut;
    @FXML private TableColumn<DossierSAV, String> colDateCreation;
    @FXML private TableColumn<DossierSAV, String> colDateReparation;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button searchButton;
    @FXML private Button importButton;
    @FXML private Button statusChangeButton;
    @FXML private Button labelButton;
    @FXML private Button refreshButton;
    @FXML private Label statusLabel;
    
    private DossierSAVRepository dossierRepo;
    private SAVService savService;
    private CSVImporter csvImporter;
    private ObservableList<DossierSAV> dossiersList;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeServices();
        configureTableView();
        configureControls();
        loadData();
    }
    
    private void initializeServices() {
        try {
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
            
            statusLabel.setText("Services initialisés");
        } catch (Exception e) {
            showError("Erreur d'initialisation", e.getMessage());
        }
    }
    
    private void configureTableView() {
        // Configuration des colonnes avec les bonnes propriétés du DossierSAV
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProprietaire.setCellValueFactory(new PropertyValueFactory<>("proprietaire"));
        
        // Colonne combinée pour l'appareil
        colAppareil.setCellValueFactory(cellData -> {
            DossierSAV dossier = cellData.getValue();
            String appareil = String.format("%s (%s)", 
                dossier.produit(), dossier.numeroSerie());
            return new SimpleStringProperty(appareil);
        });
        
        colProbleme.setCellValueFactory(new PropertyValueFactory<>("panne"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateEntree"));
        colDateReparation.setCellValueFactory(new PropertyValueFactory<>("dateSortie"));
        
        // Liste observable pour la table
        dossiersList = FXCollections.observableArrayList();
        tableView.setItems(dossiersList);
        
        // Permettre la sélection
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    private void configureControls() {
        // Configuration du filtre de statut
        statusFilter.setItems(FXCollections.observableArrayList(
            "Tous", "En attente", "En cours", "Terminé", "Annulé"
        ));
        statusFilter.setValue("Tous");
        
        // Événements des boutons
        searchButton.setOnAction(e -> handleSearch());
        importButton.setOnAction(e -> handleImport());
        statusChangeButton.setOnAction(e -> handleStatusChange());
        labelButton.setOnAction(e -> handleLabelGeneration());
        refreshButton.setOnAction(e -> loadData());
        
        // Recherche en temps réel
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > 2 || newText.isEmpty()) {
                handleSearch();
            }
        });
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
        String searchText = searchField.getText().trim();
        String selectedStatus = statusFilter.getValue();
        
        statusLabel.setText("Recherche en cours...");
        
        Task<List<DossierSAV>> task = new Task<List<DossierSAV>>() {
            @Override
            protected List<DossierSAV> call() throws Exception {
                List<DossierSAV> results;
                
                if ("Tous".equals(selectedStatus)) {
                    if (searchText.isEmpty()) {
                        results = dossierRepo.findAll();
                    } else {
                        results = dossierRepo.findByProprietaire(searchText);
                        if (results.isEmpty()) {
                            results = dossierRepo.findByNumeroSerie(searchText);
                        }
                    }
                } else {
                    results = dossierRepo.findByStatut(selectedStatus);
                    if (!searchText.isEmpty()) {
                        results = results.stream()
                            .filter(d -> d.proprietaire().toLowerCase().contains(searchText.toLowerCase()) ||
                                       d.numeroSerie().toLowerCase().contains(searchText.toLowerCase()) ||
                                       d.produit().toLowerCase().contains(searchText.toLowerCase()) ||
                                       d.panne().toLowerCase().contains(searchText.toLowerCase()))
                            .toList();
                    }
                }
                
                return results;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    dossiersList.clear();
                    dossiersList.addAll(getValue());
                    statusLabel.setText(String.format("Trouvé: %d dossiers", getValue().size()));
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Erreur de recherche", getException().getMessage());
                    statusLabel.setText("Erreur de recherche");
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        
        File selectedFile = fileChooser.showOpenDialog(importButton.getScene().getWindow());
        if (selectedFile != null) {
            statusLabel.setText("Import en cours...");
            
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    csvImporter.importDossiersSAV(selectedFile.toPath());
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        showInfo("Import réussi", "Les données ont été importées avec succès.");
                        statusLabel.setText("Import terminé");
                        loadData();
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showError("Erreur d'import", getException().getMessage());
                        statusLabel.setText("Erreur d'import");
                    });
                }
            };
            
            new Thread(task).start();
        }
    }
    
    private void handleStatusChange() {
        DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
            return;
        }
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>("En attente", 
            "En attente", "En cours", "Terminé", "Annulé");
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Dossier #" + selected.id());
        dialog.setContentText("Nouveau statut:");
        
        dialog.showAndWait().ifPresent(newStatus -> {
            statusLabel.setText("Mise à jour du statut...");
            
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    DossierSAV updated = selected.withStatut(newStatus);
                    if ("Terminé".equals(newStatus)) {
                        updated = updated.withDateSortie(java.time.LocalDate.now());
                    }
                    dossierRepo.update(updated);
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        statusLabel.setText("Statut mis à jour");
                        loadData();
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        showError("Erreur de mise à jour", getException().getMessage());
                        statusLabel.setText("Erreur de mise à jour");
                    });
                }
            };
            
            new Thread(task).start();
        });
    }
    
    private void handleLabelGeneration() {
        DossierSAV selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un dossier.");
            return;
        }
        
        statusLabel.setText("Génération de l'étiquette...");
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                savService.genererEtiquette(selected.id(), java.nio.file.Path.of("output"));
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    showInfo("Étiquette générée", 
                        "L'étiquette a été générée dans le dossier output/");
                    statusLabel.setText("Étiquette générée");
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    showError("Erreur de génération", getException().getMessage());
                    statusLabel.setText("Erreur de génération");
                });
            }
        };
        
        new Thread(task).start();
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
}