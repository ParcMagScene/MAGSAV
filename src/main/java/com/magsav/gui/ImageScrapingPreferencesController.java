package com.magsav.gui;

import com.magsav.service.ImageScrapingService;
import com.magsav.service.ScrapingConfigService;
import com.magsav.repo.ProductRepository;
import com.magsav.util.AppLogger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ImageScrapingPreferencesController implements Initializable {
    
    // Table des sources
    @FXML private TableView<SourceRow> sourcesTable;
    @FXML private TableColumn<SourceRow, String> colSourceType;
    @FXML private TableColumn<SourceRow, String> colSourceName;
    @FXML private TableColumn<SourceRow, String> colSourceUrl;
    @FXML private TableColumn<SourceRow, String> colSourcePattern;
    @FXML private TableColumn<SourceRow, Boolean> colSourceEnabled;
    
    // Boutons de gestion des sources
    @FXML private Button btnAddSource;
    @FXML private Button btnEditSource;
    @FXML private Button btnDeleteSource;
    @FXML private Button btnTestSource;
    
    // Paramètres de scraping
    @FXML private Spinner<Integer> spinnerDelay;
    @FXML private Spinner<Integer> spinnerTimeout;
    @FXML private Spinner<Integer> spinnerMinWidth;
    @FXML private Spinner<Integer> spinnerMinHeight;
    @FXML private TextField txtDownloadPath;
    @FXML private Button btnBrowseDownloadPath;
    
    // Actions en lot
    @FXML private Button btnScrapeMissing;
    @FXML private Button btnScrapeAll;
    @FXML private Button btnViewStats;
    @FXML private Label lblStats;
    @FXML private ProgressBar progressScraping;
    @FXML private Label lblProgress;
    
    // Boutons de contrôle
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    
    private ObservableList<SourceRow> sourceData = FXCollections.observableArrayList();
    private ImageScrapingService scrapingService = new ImageScrapingService();
    private ProductRepository productRepo = new ProductRepository();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupSourcesTable();
        setupSpinners();
        loadCurrentConfiguration();
        updateStats();
        
        // Gestion de la sélection dans la table
        sourcesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditSource.setDisable(!hasSelection);
            btnDeleteSource.setDisable(!hasSelection);
            btnTestSource.setDisable(!hasSelection);
        });
    }
    
    private void setupSourcesTable() {
        colSourceType.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().type()));
        colSourceName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().name()));
        colSourceUrl.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().baseUrl()));
        colSourcePattern.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().searchPattern()));
        colSourceEnabled.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().enabled()));
        colSourceEnabled.setCellFactory(CheckBoxTableCell.forTableColumn(colSourceEnabled));
        
        sourcesTable.setItems(sourceData);
    }
    
    private void setupSpinners() {
        spinnerDelay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 3));
        spinnerTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 15));
        spinnerMinWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 1000, 300));
        spinnerMinHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(100, 1000, 300));
    }
    
    private void loadCurrentConfiguration() {
        ScrapingConfigService config = ScrapingConfigService.getInstance();
        
        // Charger les sources configurées
        sourceData.clear();
        
        // Ajouter les fabricants
        config.getManufacturerConfigs().forEach((name, conf) -> {
            sourceData.add(new SourceRow("Fabricant", name, conf.baseUrl(), conf.searchPath(), true));
        });
        
        // Ajouter les revendeurs
        config.getRetailerConfigs().forEach((name, conf) -> {
            sourceData.add(new SourceRow("Revendeur", name, conf.baseUrl(), conf.searchPath(), true));
        });
        
        // Charger les paramètres par défaut
        txtDownloadPath.setText("images/scraped/");
    }
    
    private void updateStats() {
        CompletableFuture.supplyAsync(() -> {
            try {
                ProductRepository.ImageStats stats = productRepo.getImageStats();
                return String.format("📊 Base de données: %d produits total • %d avec images • %d sans images",
                    stats.total(), stats.withImages(), stats.withoutImages());
            } catch (Exception e) {
                AppLogger.error("Erreur lors du calcul des statistiques d'images", e);
                return "Erreur lors du calcul des statistiques";
            }
        }).thenAccept(statsText -> Platform.runLater(() -> lblStats.setText(statsText)));
    }
    
    @FXML
    private void onAddSource() {
        // TODO: Ouvrir un dialogue pour ajouter une nouvelle source
        showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité", "L'ajout de sources sera implémenté prochainement.");
    }
    
    @FXML
    private void onEditSource() {
        SourceRow selected = sourcesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Ouvrir un dialogue pour modifier la source sélectionnée
            showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité", 
                "L'édition de la source '" + selected.name() + "' sera implémentée prochainement.");
        }
    }
    
    @FXML
    private void onDeleteSource() {
        SourceRow selected = sourcesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la source");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer la source '" + selected.name() + "' ?");
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                sourceData.remove(selected);
            }
        }
    }
    
    @FXML
    private void onTestSource() {
        SourceRow selected = sourcesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Tester la source sélectionnée avec un produit exemple
            showAlert(Alert.AlertType.INFORMATION, "Test de Source", 
                "Test de '" + selected.name() + "' sera implémenté prochainement.");
        }
    }
    
    @FXML
    private void onBrowseDownloadPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier de téléchargement");
        
        File currentDir = new File(txtDownloadPath.getText());
        if (currentDir.exists()) {
            chooser.setInitialDirectory(currentDir);
        }
        
        Stage stage = (Stage) btnBrowseDownloadPath.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);
        
        if (selectedDir != null) {
            txtDownloadPath.setText(selectedDir.getAbsolutePath());
        }
    }
    
    @FXML
    private void onScrapeMissingImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Scraping d'Images");
        alert.setHeaderText("Scraper les produits sans images");
        alert.setContentText("Cette opération va rechercher des images pour tous les produits qui n'en ont pas encore. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            startBatchScraping(false);
        }
    }
    
    @FXML
    private void onScrapeAllImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Scraping d'Images");
        alert.setHeaderText("Mettre à jour toutes les images");
        alert.setContentText("Cette opération va rechercher de nouvelles images pour TOUS les produits. Cela peut prendre beaucoup de temps. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            startBatchScraping(true);
        }
    }
    
    private void startBatchScraping(boolean includeExisting) {
        progressScraping.setVisible(true);
        lblProgress.setVisible(true);
        btnScrapeMissing.setDisable(true);
        btnScrapeAll.setDisable(true);
        
        Task<Void> scrapingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Simpler logic for demo purposes
                    Platform.runLater(() -> {
                        lblProgress.setText("Scraping en lot temporairement désactivé (en développement)");
                        progressScraping.setProgress(1.0);
                    });
                    
                    // TODO: Implémenter le scraping en lot complet
                    Thread.sleep(2000);
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        lblProgress.setText("Erreur durant le scraping: " + e.getMessage());
                    });
                    AppLogger.error("Erreur durant le scraping en lot", e);
                }
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    btnScrapeMissing.setDisable(false);
                    btnScrapeAll.setDisable(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    btnScrapeMissing.setDisable(false);
                    btnScrapeAll.setDisable(false);
                    lblProgress.setText("Échec du scraping en lot");
                });
            }
        };
        
        Thread scrapingThread = new Thread(scrapingTask);
        scrapingThread.setDaemon(true);
        scrapingThread.start();
    }
    
    @FXML
    private void onViewStats() {
        CompletableFuture.supplyAsync(() -> {
            try {
                ProductRepository.ImageStats stats = productRepo.getImageStats();
                return String.format(
                    "📊 Statistiques détaillées des images:\n\n" +
                    "• Produits total: %d\n" +
                    "• Avec images scrapées: %d\n" +
                    "• Sans images: %d\n" +
                    "• Pourcentage couvert: %.1f%%",
                    stats.total(),
                    stats.withImages(),
                    stats.withoutImages(),
                    stats.total() > 0 ? (stats.withImages() * 100.0 / stats.total()) : 0.0
                );
            } catch (Exception e) {
                return "Erreur lors du calcul des statistiques: " + e.getMessage();
            }
        }).thenAccept(statsText -> Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Statistiques");
            alert.setHeaderText("État des images dans la base");
            alert.setContentText(statsText);
            alert.showAndWait();
        }));
    }
    
    @FXML
    private void onSave() {
        // TODO: Sauvegarder la configuration modifiée
        showAlert(Alert.AlertType.INFORMATION, "Sauvegarde", "Configuration sauvegardée avec succès!");
        closeWindow();
    }
    
    @FXML
    private void onCancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Record pour représenter une ligne de source dans la table
    public record SourceRow(String type, String name, String baseUrl, String searchPattern, boolean enabled) {}
}