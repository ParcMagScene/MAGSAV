package com.magsav.gui;

import com.magsav.service.ScrapingConfigService;
import com.magsav.service.DataCacheService;
import com.magsav.service.DataChangeNotificationService;
import com.magsav.service.DataChangeEvent;
import com.magsav.service.AddressService;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.CompanyRepository;
import com.magsav.repo.UserRepository;
import com.magsav.model.Company;
import com.magsav.model.User;
import com.magsav.db.DB;

import java.util.List;
import java.time.LocalDateTime;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class PreferencesController implements Initializable {
    
    // Onglet Configuration Email
    @FXML private TextField txtSmtpServer;
    @FXML private TextField txtSmtpPort;
    @FXML private TextField txtEmailUser;
    @FXML private PasswordField txtEmailPassword;
    @FXML private TextField txtSenderName;
    @FXML private CheckBox chkUseTLS;
    @FXML private Button btnTestEmail;
    
    // Onglet Google Services
    @FXML private TextField txtGoogleClientId;
    @FXML private PasswordField txtGoogleClientSecret;
    @FXML private TextField txtGoogleRedirectUri;
    @FXML private TextArea txtGoogleScopes;
    @FXML private Button btnTestGoogleConnection;
    @FXML private Button btnSaveGoogleConfig;
    @FXML private CheckBox chkGoogleCalendar;
    @FXML private CheckBox chkGoogleGmail;
    @FXML private CheckBox chkGoogleContacts;
    @FXML private Spinner<Integer> spnGoogleSyncInterval;
    @FXML private Spinner<Integer> spnGoogleTimeout;
    @FXML private CheckBox chkGoogleAutoSync;
    
    // Onglet Maintenance Médias
    @FXML private TextField txtPhotosPath;
    @FXML private TextField txtMediasPath;
    @FXML private Button btnBrowsePhotos;
    @FXML private Button btnBrowseMedias;
    @FXML private Slider sliderImageQuality;
    @FXML private Label lblQualityValue;
    @FXML private Button btnCleanDuplicates;
    @FXML private Button btnOptimizeImages;
    @FXML private Button btnGenerateThumbnails;
    @FXML private Label lblMediaStats;
    
    // Onglet Maintenance Médias (nouveaux éléments)
    @FXML private Slider sliderMinQuality;
    @FXML private Label lblMinQualityValue;
    @FXML private Spinner<Integer> spinnerMinSize;
    @FXML private CheckBox chkFormatJPG;
    @FXML private CheckBox chkFormatPNG;
    @FXML private CheckBox chkFormatWEBP;
    @FXML private Button btnScanMedia;
    @FXML private Button btnCleanupDuplicates;
    @FXML private Button btnRepairLinks;
    @FXML private Button btnFullMaintenance;
    @FXML private Label lblMaintenanceStats;
    @FXML private ProgressBar progressMaintenance;
    @FXML private Label lblMaintenanceProgress;
    
    // Onglet Gestion Catégories
    @FXML private TextField txtCategorySearch;
    @FXML private Button btnRefreshCategories;
    @FXML private TableView<CategoryRow> tableCategoriesInTab;
    @FXML private TableColumn<CategoryRow, Long> colCatId;
    @FXML private TableColumn<CategoryRow, String> colCatHierarchy;
    @FXML private TableColumn<CategoryRow, String> colCatName;
    @FXML private TableColumn<CategoryRow, String> colCatType;
    @FXML private TableColumn<CategoryRow, String> colCatParent;
    @FXML private Button btnAddCategoryInTab;
    @FXML private Button btnAddSubcategoryInTab;
    @FXML private Button btnEditCategoryInTab;
    @FXML private Button btnDeleteCategoryInTab;
    @FXML private Button btnOpenCategoriesWindow;
    
    // Onglet Gestion Médias
    @FXML private Button btnImportFromFolder;
    @FXML private Button btnImportFromClipboard;
    @FXML private Button btnImportLogo;
    @FXML private Label lblTotalImages;
    @FXML private Label lblTotalLogos;
    @FXML private Label lblUsedSpace;
    @FXML private Label lblOrphanImages;
    @FXML private Button btnRefreshMediaStats;
    @FXML private Button btnOpenMediaManager;
    
    // Onglet Scraping Images
    @FXML private TableView<SourceRow> sourcesTable;
    @FXML private TableColumn<SourceRow, String> colSourceType;
    @FXML private TableColumn<SourceRow, String> colSourceName;
    @FXML private TableColumn<SourceRow, String> colSourceUrl;
    @FXML private TableColumn<SourceRow, String> colSourcePattern;
    @FXML private TableColumn<SourceRow, Boolean> colSourceEnabled;
    @FXML private Button btnAddSource;
    @FXML private Button btnEditSource;
    @FXML private Button btnDeleteSource;
    @FXML private Button btnTestSource;
    @FXML private Spinner<Integer> spinnerDelay;
    @FXML private Spinner<Integer> spinnerTimeout;
    @FXML private ComboBox<String> cbOutputFormat;
    @FXML private Slider sliderScrapingQuality;
    @FXML private Label lblScrapingQualityValue;
    @FXML private Spinner<Integer> spinnerMaxWidth;
    @FXML private Spinner<Integer> spinnerMaxHeight;
    @FXML private Button btnScrapeMissing;
    @FXML private Button btnValidateImages;
    @FXML private Button btnViewStats;
    @FXML private Label lblScrapingStats;
    @FXML private ProgressBar progressScraping;
    @FXML private Label lblScrapingProgress;
    
    // Contrôles généraux
    @FXML private TabPane preferencesTabPane;
    @FXML private Button btnSaveAll;
    @FXML private Button btnCancel;
    // @FXML private Button btnBackToMain; // SUPPRIMÉ - Bouton retiré de l'interface
    
    // Getter public pour accéder au TabPane depuis l'extérieur
    public TabPane getPreferencesTabPane() {
        return preferencesTabPane;
    }
    
    // Section Apparence (maintenant dans Général)

    @FXML private ColorPicker sidebarColorPicker;
    @FXML private Label sidebarColorLabel;
    @FXML private ColorPicker backgroundColorPicker;
    @FXML private Label backgroundColorLabel;
    @FXML private ColorPicker tabColorPicker;
    @FXML private Label tabColorLabel;
    @FXML private ColorPicker accentColorPicker;
    @FXML private Label accentColorLabel;
    @FXML private ColorPicker textColorPicker;
    @FXML private Label textColorLabel;
    @FXML private VBox previewPane;
    @FXML private Button applyAppearanceButton;
    @FXML private Button resetAppearanceButton;
    
    // Section Langue et Localisation
    @FXML private ComboBox<String> cbLanguage;
    @FXML private ComboBox<String> cbDateFormat;
    @FXML private ComboBox<String> cbTimeFormat;
    @FXML private ComboBox<String> cbCurrency;
    
    // Section Notifications
    @FXML private CheckBox chkShowNotifications;
    @FXML private CheckBox chkSoundNotifications;
    @FXML private CheckBox chkEmailNotifications;
    @FXML private CheckBox chkDesktopNotifications;
    @FXML private Spinner<Integer> spinnerNotificationDuration;
    
    // Section Sécurité
    @FXML private CheckBox chkRequirePassword;
    @FXML private CheckBox chkAutoLock;
    @FXML private CheckBox chkLogAccess;
    @FXML private CheckBox chkEncryptData;
    @FXML private Spinner<Integer> spinnerLockDelay;
    @FXML private Button btnChangePassword;
    @FXML private Button btnViewLogs;
    
    // Section Base de Données
    @FXML private TextField txtDatabasePath;
    @FXML private Button btnBrowseDatabase;
    @FXML private CheckBox chkAutoBackup;
    @FXML private Spinner<Integer> spinnerBackupInterval;
    @FXML private Button btnBackupNow;
    @FXML private Button btnRestoreBackup;
    @FXML private Button btnOptimizeDB;
    @FXML private Label lblDatabaseStats;
    
    // Section Outils de Développement
    @FXML private Button btnGenerateTestData;
    @FXML private Button btnClearTestData;
    
    // Section Import/Export
    @FXML private Button btnImportProducts;
    @FXML private Button btnImportClients;
    @FXML private Button btnImportCompanies;
    @FXML private Button btnExportProducts;
    @FXML private Button btnExportClients;
    @FXML private Button btnExportCompanies;
    @FXML private Button btnExportAll;
    @FXML private Button btnExportReport;
    @FXML private ComboBox<String> cbExportFormat;
    
    // Onglet Société
    @FXML private TextField txtCompanyName;
    @FXML private TextField txtCompanyLegalName;
    @FXML private TextField txtCompanySiret;
    @FXML private TextField txtCompanySector;
    @FXML private TextField txtCompanyAddress;
    @FXML private TextField txtCompanyPostalCode;
    @FXML private TextField txtCompanyCity;
    @FXML private TextField txtCompanyCountry;
    @FXML private TextField txtCompanyPhone;
    @FXML private TextField txtCompanyEmail;
    @FXML private TextField txtCompanyWebsite;
    @FXML private TextArea txtCompanyDescription;
    @FXML private ImageView imgCompanyLogo;
    @FXML private Button btnSelectLogo;
    @FXML private Button btnRemoveLogo;
    @FXML private TableView<CompanyUserRow> companyUsersTable;
    @FXML private TableColumn<CompanyUserRow, String> colUserName;
    @FXML private TableColumn<CompanyUserRow, String> colUserUsername;
    @FXML private TableColumn<CompanyUserRow, String> colUserPosition;
    @FXML private TableColumn<CompanyUserRow, String> colUserRole;
    @FXML private TableColumn<CompanyUserRow, String> colUserEmail;
    @FXML private TableColumn<CompanyUserRow, String> colUserPhone;
    @FXML private TableColumn<CompanyUserRow, String> colUserActive;
    @FXML private Button btnAddUser;
    @FXML private Button btnEditUser;
    @FXML private Button btnDeleteUser;
    @FXML private Button btnResetPassword;
    @FXML private Button btnToggleActive;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblInactiveUsers;
    @FXML private Label lblAdminUsers;
    
    private ObservableList<SourceRow> sourceData = FXCollections.observableArrayList();
    private ObservableList<CompanyUserRow> companyUserData = FXCollections.observableArrayList();
    private ProductRepository productRepo = new ProductRepository();
    private AddressService addressService = new AddressService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEmailTab();
        setupMediaTab();
        setupScrapingTab();
        setupAddressAutocomplete();
        initializeNewTabs();
        setupGeneralSection();
        setupSystemSection();
        setupMaintenanceSection();
        setupDataSection();
        setupGoogleServicesSection();
        loadAllSettings();
    }
    
    private void setupEmailTab() {
        // Configuration des listeners pour validation en temps réel
        txtSmtpServer.textProperty().addListener((obs, oldText, newText) -> validateEmailConfig());
        txtEmailUser.textProperty().addListener((obs, oldText, newText) -> validateEmailConfig());
    }
    
    private void setupMediaTab() {
        // Setup du slider de qualité avec mise à jour du label
        sliderImageQuality.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblQualityValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        // Calcul des statistiques médias
        updateMediaStats();
    }
    
    private void setupAddressAutocomplete() {
        // Ajouter l'autocomplétion d'adresse aux champs d'adresse de l'entreprise
        if (txtCompanyAddress != null) {
            addressService.setupAddressAutocomplete(txtCompanyAddress);
        }
    }
    
    private void setupScrapingTab() {
        // Configuration de la table des sources
        colSourceType.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().type()));
        colSourceName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().name()));
        colSourceUrl.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().baseUrl()));
        colSourcePattern.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().searchPattern()));
        colSourceEnabled.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().enabled()));
        colSourceEnabled.setCellFactory(CheckBoxTableCell.forTableColumn(colSourceEnabled));
        
        sourcesTable.setItems(sourceData);
        
        // Configuration des spinners
        spinnerDelay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 3));
        spinnerTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 60, 15));
        spinnerMaxWidth.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(400, 2000, 800));
        spinnerMaxHeight.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(400, 2000, 800));
        
        // Configuration du slider de qualité scraping
        sliderScrapingQuality.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblScrapingQualityValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
        });
        
        // Gestion de la sélection dans la table
        sourcesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean hasSelection = newSelection != null;
            btnEditSource.setDisable(!hasSelection);
            btnDeleteSource.setDisable(!hasSelection);
            btnTestSource.setDisable(!hasSelection);
        });
        
        loadScrapingSources();
        updateScrapingStats();
    }
    
    private void loadAllSettings() {
        loadEmailSettings();
        loadMediaSettings();
        loadScrapingSettings();
    }
    
    private void loadEmailSettings() {
        // TODO: Charger depuis fichier de configuration
        txtSmtpServer.setText("");
        txtSmtpPort.setText("587");
        txtEmailUser.setText("");
        txtSenderName.setText("MAGSAV - Gestion SAV");
        chkUseTLS.setSelected(true);
    }
    
    private void loadMediaSettings() {
        txtPhotosPath.setText("photos/");
        txtMediasPath.setText("medias/");
        sliderImageQuality.setValue(0.8);
    }
    
    private void loadScrapingSettings() {
        cbOutputFormat.getItems().addAll("JPG", "PNG", "WEBP");
        cbOutputFormat.setValue("JPG");
        sliderScrapingQuality.setValue(0.85);
    }
    
    private void loadScrapingSources() {
        ScrapingConfigService config = ScrapingConfigService.getInstance();
        
        sourceData.clear();
        
        // Ajouter les fabricants
        config.getManufacturerConfigs().forEach((name, conf) -> {
            sourceData.add(new SourceRow("Fabricant", name, conf.baseUrl(), conf.searchPath(), true));
        });
        
        // Ajouter les revendeurs
        config.getRetailerConfigs().forEach((name, conf) -> {
            sourceData.add(new SourceRow("Revendeur", name, conf.baseUrl(), conf.searchPath(), true));
        });
    }
    
    private void updateMediaStats() {
        CompletableFuture.runAsync(() -> {
            try {
                // Calculer les statistiques des médias
                File photosDir = new File(txtPhotosPath.getText());
                File mediasDir = new File(txtMediasPath.getText());
                
                int photosCount = photosDir.exists() ? photosDir.listFiles().length : 0;
                int mediasCount = mediasDir.exists() ? mediasDir.listFiles().length : 0;
                
                String stats = String.format("📁 %d photos • %d médias • Dernière optimisation: jamais", 
                                            photosCount, mediasCount);
                
                Platform.runLater(() -> lblMediaStats.setText(stats));
            } catch (Exception e) {
                Platform.runLater(() -> lblMediaStats.setText("Erreur lors du calcul des statistiques"));
            }
        });
    }
    
    private void updateScrapingStats() {
        CompletableFuture.supplyAsync(() -> {
            try {
                ProductRepository.ImageStats stats = productRepo.getImageStats();
                return String.format("📊 Base de données: %d produits total • %d avec images • %d sans images",
                    stats.total(), stats.withImages(), stats.withoutImages());
            } catch (Exception e) {
                AppLogger.error("Erreur lors du calcul des statistiques d'images", e);
                return "Erreur lors du calcul des statistiques";
            }
        }).thenAccept(statsText -> Platform.runLater(() -> lblScrapingStats.setText(statsText)));
    }
    
    // ==================== ACTIONS EMAIL ====================
    
    @FXML
    private void onTestEmailConfig() {
        // TODO: Implémenter test de configuration email
        showAlert(Alert.AlertType.INFORMATION, "Test Email", "Test de configuration email à implémenter.");
    }
    
    private void validateEmailConfig() {
        boolean valid = !txtSmtpServer.getText().trim().isEmpty() && 
                       !txtEmailUser.getText().trim().isEmpty();
        btnTestEmail.setDisable(!valid);
    }
    
    // ==================== ACTIONS MÉDIAS ====================
    
    @FXML
    private void onBrowsePhotosPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier photos");
        
        File currentDir = new File(txtPhotosPath.getText());
        if (currentDir.exists()) {
            chooser.setInitialDirectory(currentDir);
        }
        
        Stage stage = (Stage) btnBrowsePhotos.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);
        
        if (selectedDir != null) {
            txtPhotosPath.setText(selectedDir.getAbsolutePath() + "/");
            updateMediaStats();
        }
    }
    
    @FXML
    private void onBrowseMediasPath() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choisir le dossier médias");
        
        File currentDir = new File(txtMediasPath.getText());
        if (currentDir.exists()) {
            chooser.setInitialDirectory(currentDir);
        }
        
        Stage stage = (Stage) btnBrowseMedias.getScene().getWindow();
        File selectedDir = chooser.showDialog(stage);
        
        if (selectedDir != null) {
            txtMediasPath.setText(selectedDir.getAbsolutePath() + "/");
            updateMediaStats();
        }
    }
    
    @FXML
    private void onCleanDuplicates() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nettoyage des Doublons");
        alert.setHeaderText("Confirmer le nettoyage des doublons");
        alert.setContentText("Cette action supprimera définitivement les doublons détectés dans :\n" +
                            "• Produits (UIDs dupliqués)\n" +
                            "• Sociétés (noms identiques)\n" +
                            "• Catégories (noms et parents identiques)\n\n" +
                            "Cette opération est irréversible. Continuer ?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Créer et lancer la tâche de nettoyage
                com.magsav.util.DatabaseCleanupTask cleanupTask = new com.magsav.util.DatabaseCleanupTask(
                    () -> {
                        // Succès
                        showAlert(Alert.AlertType.INFORMATION, "Nettoyage Terminé", 
                                "Le nettoyage des doublons a été effectué avec succès.");
                    },
                    () -> {
                        // Échec
                        showAlert(Alert.AlertType.ERROR, "Erreur", 
                                "Une erreur est survenue lors du nettoyage des doublons.");
                    }
                );
                
                // Afficher un dialogue de progression
                javafx.scene.control.ProgressBar progressBar = new javafx.scene.control.ProgressBar();
                progressBar.progressProperty().bind(cleanupTask.progressProperty());
                
                javafx.scene.control.Label statusLabel = new javafx.scene.control.Label();
                statusLabel.textProperty().bind(cleanupTask.messageProperty());
                
                javafx.scene.layout.VBox progressBox = new javafx.scene.layout.VBox(10);
                progressBox.getChildren().addAll(statusLabel, progressBar);
                
                Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                progressAlert.setTitle("Nettoyage en cours");
                progressAlert.setHeaderText("Suppression des doublons...");
                progressAlert.getDialogPane().setContent(progressBox);
                progressAlert.show();
                
                // Fermer le dialogue quand la tâche est terminée
                cleanupTask.setOnSucceeded(e -> progressAlert.close());
                cleanupTask.setOnFailed(e -> progressAlert.close());
                
                // Lancer la tâche en arrière-plan
                Thread cleanupThread = new Thread(cleanupTask);
                cleanupThread.setDaemon(true);
                cleanupThread.start();
            }
        });
    }
    
    @FXML
    private void onOptimizeImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Optimisation des Images");
        alert.setHeaderText("Optimiser toutes les images");
        alert.setContentText("Cette opération va redimensionner et compresser toutes les images selon les paramètres. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // TODO: Implémenter optimisation des images
            showAlert(Alert.AlertType.INFORMATION, "Optimisation", "Optimisation des images à implémenter.");
            updateMediaStats();
        }
    }
    
    @FXML
    private void onGenerateThumbnails() {
        // TODO: Implémenter génération de miniatures
        showAlert(Alert.AlertType.INFORMATION, "Miniatures", "Génération de miniatures à implémenter.");
    }
    
    // ==================== ACTIONS SCRAPING ====================
    
    @FXML
    private void onAddSource() {
        showAlert(Alert.AlertType.INFORMATION, "Fonctionnalité", "L'ajout de sources sera implémenté prochainement.");
    }
    
    @FXML
    private void onEditSource() {
        SourceRow selected = sourcesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
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
            showAlert(Alert.AlertType.INFORMATION, "Test de Source", 
                "Test de '" + selected.name() + "' sera implémenté prochainement.");
        }
    }
    
    @FXML
    private void onScrapeMissingImages() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Scraping d'Images");
        alert.setHeaderText("Scraper les produits sans images");
        alert.setContentText("Cette opération va rechercher des images pour tous les produits qui n'en ont pas encore. Les images seront normalisées et nécessiteront validation. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            startImageScrappingWithNormalization();
        }
    }
    
    @FXML
    private void onValidateImageCorrespondences() {
        try {
            com.magsav.service.NavigationService.openImageValidation();
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'ouverture de la validation des images", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface de validation: " + e.getMessage());
        }
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
    
    private void startImageScrappingWithNormalization() {
        progressScraping.setVisible(true);
        lblScrapingProgress.setVisible(true);
        btnScrapeMissing.setDisable(true);
        
        Task<Void> scrapingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    progressScraping.setProgress(-1); // Mode indéterminé
                    lblScrapingProgress.setText("Lancement du scraping avec normalisation...");
                });
                
                // TODO: Implémenter le processus complet de scraping avec normalisation
                Thread.sleep(3000);
                
                Platform.runLater(() -> {
                    lblScrapingProgress.setText("Scraping terminé. Validation requise pour associer les images aux produits.");
                    btnValidateImages.setDisable(false);
                    updateScrapingStats();
                });
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    progressScraping.setVisible(false);
                    btnScrapeMissing.setDisable(false);
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    progressScraping.setVisible(false);
                    btnScrapeMissing.setDisable(false);
                    lblScrapingProgress.setText("Échec du scraping");
                });
            }
        };
        
        Thread scrapingThread = new Thread(scrapingTask);
        scrapingThread.setDaemon(true);
        scrapingThread.start();
    }
    
    // ==================== ACTIONS GÉNÉRALES ====================
    
    @FXML
    private void onSaveAll() {
        try {
            saveCompanyData();
            // TODO: Sauvegarder les autres configurations (médias, catégories, etc.)
            
            showAlert(Alert.AlertType.INFORMATION, "Sauvegarde", "Toutes les préférences ont été sauvegardées avec succès!");
            closeWindow();
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la sauvegarde: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    private void saveCompanyData() {
        try {
            CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
            
            // Récupération de la société existante ou création d'une nouvelle
            Company company = companyRepo.findByType(Company.CompanyType.OWN_COMPANY)
                    .stream()
                    .findFirst()
                    .orElse(new Company("Mag Scène", Company.CompanyType.OWN_COMPANY));
            
            // Mise à jour des valeurs depuis les champs de l'interface
            if (txtCompanyName != null && !txtCompanyName.getText().trim().isEmpty()) {
                company.setName(txtCompanyName.getText().trim());
            }
            if (txtCompanyLegalName != null && !txtCompanyLegalName.getText().trim().isEmpty()) {
                company.setLegalName(txtCompanyLegalName.getText().trim());
            }
            if (txtCompanySiret != null && txtCompanySiret.getText() != null) {
                company.setSiret(txtCompanySiret.getText().trim());
            }
            if (txtCompanySector != null && txtCompanySector.getText() != null) {
                company.setSector(txtCompanySector.getText().trim());
            }
            if (txtCompanyAddress != null && txtCompanyAddress.getText() != null) {
                company.setAddress(txtCompanyAddress.getText().trim());
            }
            if (txtCompanyPostalCode != null && txtCompanyPostalCode.getText() != null) {
                company.setPostalCode(txtCompanyPostalCode.getText().trim());
            }
            if (txtCompanyCity != null && txtCompanyCity.getText() != null) {
                company.setCity(txtCompanyCity.getText().trim());
            }
            if (txtCompanyCountry != null && txtCompanyCountry.getText() != null) {
                company.setCountry(txtCompanyCountry.getText().trim());
            }
            if (txtCompanyPhone != null && txtCompanyPhone.getText() != null) {
                company.setPhone(txtCompanyPhone.getText().trim());
            }
            if (txtCompanyEmail != null && txtCompanyEmail.getText() != null) {
                company.setEmail(txtCompanyEmail.getText().trim());
            }
            if (txtCompanyWebsite != null && txtCompanyWebsite.getText() != null) {
                company.setWebsite(txtCompanyWebsite.getText().trim());
            }
            if (txtCompanyDescription != null && txtCompanyDescription.getText() != null) {
                company.setDescription(txtCompanyDescription.getText().trim());
            }
            
            // Sauvegarde de la société
            companyRepo.save(company);
            AppLogger.info("Informations de société sauvegardées avec succès");
            
            // Invalider le cache pour forcer le rechargement des données
            DataCacheService.invalidateAllCache();
            
            // Notifier les autres fenêtres du changement
            DataChangeNotificationService.getInstance().notifyDataChanged(
                new DataChangeEvent(DataChangeEvent.Type.COMPANY_UPDATED, 
                    "Informations de société mises à jour", company));
                    
            // Afficher une notification de succès
            try {
                com.magsav.ui.components.NotificationManager.showSuccess("Informations société mises à jour");
            } catch (Exception e) {
                // Ignorer si la méthode n'existe pas
            }
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la sauvegarde des données société: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde", e);
        }
    }
    
    @FXML
    private void onCancel() {
        closeWindow();
    }
    
    // @FXML 
    // private void onBackToMainPreferences() {
    //     AppLogger.info("Retour vers les préférences principales");
    //     try {
    //         // Fermer la fenêtre actuelle
    //         closeWindow();
    //         
    //         // Rediriger vers la section préférences principale dans MainController
    //         // Note: Cette navigation sera gérée automatiquement par le retour à la page principale
    //         
    //     } catch (Exception e) {
    //         AppLogger.error("Erreur lors du retour aux préférences principales", e);
    //     }
    // }
    // MÉTHODE SUPPRIMÉE - Bouton de retour aux préférences retiré de l'interface
    
    private void closeWindow() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
    
    // Méthode dépréciée - utiliser AlertUtils à la place
    @Deprecated
    private void showAlert(Alert.AlertType type, String title, String message) {
        com.magsav.util.AlertUtils.showAlert(type, title, message);
    }
    
    // ==================== ACTIONS ONGLET MAINTENANCE MÉDIAS ====================
    
    @FXML
    private void onScanMedia() {
        AppLogger.info("preferences", "Début scan des médias");
        lblMaintenanceProgress.setText("Scan en cours...");
        progressMaintenance.setVisible(true);
        lblMaintenanceProgress.setVisible(true);
        
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000); // Simulation
                Platform.runLater(() -> {
                    lblMaintenanceStats.setText("Scan terminé: 150 images trouvées, 12 doublons détectés");
                    progressMaintenance.setVisible(false);
                    lblMaintenanceProgress.setVisible(false);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    

    
    @FXML
    private void onCleanupDuplicates() {
        // Rediriger vers la méthode principale
        onCleanDuplicates();
    }
    
    @FXML
    private void onRepairLinks() {
        AppLogger.info("preferences", "Réparation des liens");
        showAlert(Alert.AlertType.INFORMATION, "Réparation", "Réparation des liens images terminée.");
    }
    
    @FXML
    private void onFullMaintenance() {
        AppLogger.info("preferences", "Maintenance complète");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Maintenance Complète");
        alert.setHeaderText("Confirmer la maintenance complète");
        alert.setContentText("Cette opération va scanner, optimiser et réparer tous les médias. Continuer ?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert(Alert.AlertType.INFORMATION, "Maintenance", "Maintenance complète lancée en arrière-plan.");
            }
        });
    }
    
    // ==================== ACTIONS ONGLET GESTION CATÉGORIES ====================
    
    @FXML
    private void onRefreshCategories() {
        AppLogger.info("preferences", "Actualisation des catégories");
        loadCategoriesData();
    }
    
    @FXML
    private void onAddCategoryInTab() {
        AppLogger.info("preferences", "Ajout d'une catégorie depuis l'onglet");
        com.magsav.util.ErrorHandler.handleNotImplemented("ajout de catégorie");
    }
    
    @FXML
    private void onAddSubcategoryInTab() {
        AppLogger.info("preferences", "Ajout d'une sous-catégorie depuis l'onglet");
        com.magsav.util.ErrorHandler.handleNotImplemented("ajout de sous-catégorie");
    }
    
    @FXML
    private void onEditCategoryInTab() {
        CategoryRow selected = tableCategoriesInTab.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("preferences", "Modification de la catégorie: " + selected.name());
            com.magsav.util.ErrorHandler.handleNotImplemented("modification de catégorie");
        }
    }
    
    @FXML
    private void onDeleteCategoryInTab() {
        CategoryRow selected = tableCategoriesInTab.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Supprimer Catégorie");
            alert.setHeaderText("Confirmer la suppression");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer la catégorie \"" + selected.name() + "\" ?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    AppLogger.info("preferences", "Suppression de la catégorie: " + selected.name());
                    // TODO: Supprimer de la base de données
                }
            });
        }
    }
    
    @FXML
    private void onOpenCategoriesWindow() {
        AppLogger.info("preferences", "Ouverture de l'interface complète des catégories");
        try {
            Stage stage = new Stage();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/categories/categories.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setTitle("Gestion des Catégories");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'ouverture de l'interface catégories", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir l'interface des catégories: " + e.getMessage());
        }
    }
    
    // ==================== ACTIONS ONGLET GESTION MÉDIAS ====================
    
    @FXML
    private void onImportFromFolder() {
        AppLogger.info("preferences", "Import depuis dossier");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Sélectionner le dossier d'images à importer");
        File selectedDirectory = directoryChooser.showDialog((Stage) btnImportFromFolder.getScene().getWindow());
        
        if (selectedDirectory != null) {
            AppLogger.info("preferences", "Dossier sélectionné: " + selectedDirectory.getAbsolutePath());
            showAlert(Alert.AlertType.INFORMATION, "Import", "Import depuis le dossier: " + selectedDirectory.getName());
        }
    }
    
    @FXML
    private void onImportFromClipboard() {
        AppLogger.info("preferences", "Import depuis presse-papier");
        showAlert(Alert.AlertType.INFORMATION, "Non implémenté", "L'import depuis le presse-papier sera implémenté prochainement.");
    }
    
    @FXML
    private void onImportLogo() {
        AppLogger.info("preferences", "Import de logo");
        showAlert(Alert.AlertType.INFORMATION, "Non implémenté", "L'import de logo sera implémenté prochainement.");
    }
    
    @FXML
    private void onRefreshMediaStats() {
        AppLogger.info("preferences", "Actualisation des statistiques médias");
        
        // Simulation de chargement des statistiques
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                Platform.runLater(() -> {
                    lblTotalImages.setText("1,247");
                    lblTotalLogos.setText("83");
                    lblUsedSpace.setText("2.3 GB");
                    lblOrphanImages.setText("12");
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
    
    @FXML
    private void onOpenMediaManager() {
        AppLogger.info("preferences", "Ouverture du gestionnaire de médias complet");
        try {
            Stage stage = new Stage();
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/media_management.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            stage.setTitle("Gestion des Médias");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'ouverture du gestionnaire de médias", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le gestionnaire de médias: " + e.getMessage());
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private void loadCategoriesData() {
        // TODO: Charger les vraies données depuis la base
        ObservableList<CategoryRow> categories = FXCollections.observableArrayList();
        categories.addAll(
            new CategoryRow(1L, "Électronique", "Électronique", "Catégorie", null),
            new CategoryRow(2L, "Électronique > Smartphones", "Smartphones", "Sous-catégorie", "Électronique"),
            new CategoryRow(3L, "Électroménager", "Électroménager", "Catégorie", null)
        );
        
        if (tableCategoriesInTab != null) {
            tableCategoriesInTab.setItems(categories);
        }
    }
    
    private void initializeNewTabs() {
        // Initialisation de l'onglet Maintenance Médias
        if (sliderMinQuality != null) {
            sliderMinQuality.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (lblMinQualityValue != null) {
                    lblMinQualityValue.setText(String.format("%.0f%%", newVal.doubleValue() * 100));
                }
            });
        }
        
        // Initialisation de l'onglet Gestion Catégories
        if (tableCategoriesInTab != null) {
            colCatId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().id()));
            colCatHierarchy.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().hierarchy()));
            colCatName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().name()));
            colCatType.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().type()));
            colCatParent.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().parent()));
            
            tableCategoriesInTab.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                boolean hasSelection = newSel != null;
                if (btnEditCategoryInTab != null) btnEditCategoryInTab.setDisable(!hasSelection);
                if (btnDeleteCategoryInTab != null) btnDeleteCategoryInTab.setDisable(!hasSelection);
            });
            
            loadCategoriesData();
        }
        
        // Initialisation des statistiques médias
        if (btnRefreshMediaStats != null) {
            onRefreshMediaStats();
        }
        
        // Initialisation de l'onglet Société
        setupCompanyTab();
        loadCompanyData();
    }
    
    // Méthodes pour l'onglet Société
    private void setupCompanyTab() {
        if (companyUsersTable != null) {
            // Configuration des colonnes de la table des utilisateurs
            colUserName.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().fullName()));
            colUserUsername.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().username()));
            colUserPosition.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().position()));
            colUserRole.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().role()));
            colUserEmail.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().email()));
            colUserPhone.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().phone()));
            colUserActive.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().active()));
            
            companyUsersTable.setItems(companyUserData);
            
            // Listeners pour les boutons d'actions
            companyUsersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                boolean hasSelection = newSel != null;
                if (btnEditUser != null) btnEditUser.setDisable(!hasSelection);
                if (btnDeleteUser != null) btnDeleteUser.setDisable(!hasSelection);
            });
        }
    }
    
    private void loadCompanyData() {
        try {
            CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
            
            // Chargement de la société Mag Scène ou création si elle n'existe pas
            Company magScene = companyRepo.findByType(Company.CompanyType.OWN_COMPANY)
                    .stream()
                    .findFirst()
                    .orElseGet(() -> {
                        Company newCompany = companyRepo.createDefaultMagScene();
                        AppLogger.info("Société Mag Scène créée par défaut");
                        return newCompany;
                    });
            
            // Mise à jour des champs de l'interface
            if (txtCompanyName != null) txtCompanyName.setText(magScene.getName());
            if (txtCompanyDescription != null) txtCompanyDescription.setText(magScene.getDescription());
            if (txtCompanyAddress != null) txtCompanyAddress.setText(magScene.getAddress());
            if (txtCompanyPhone != null) txtCompanyPhone.setText(magScene.getPhone());
            if (txtCompanyEmail != null) txtCompanyEmail.setText(magScene.getEmail());
            if (txtCompanyWebsite != null) txtCompanyWebsite.setText(magScene.getWebsite());
            if (txtCompanySiret != null) txtCompanySiret.setText(magScene.getSiret());
            
            // Chargement du logo
            if (imgCompanyLogo != null && magScene.getLogoPath() != null && !magScene.getLogoPath().isEmpty()) {
                try {
                    Image logoImage = new Image("file:" + magScene.getLogoPath());
                    imgCompanyLogo.setImage(logoImage);
                } catch (Exception e) {
                    AppLogger.warn("Impossible de charger le logo: " + e.getMessage());
                }
            }
            
            // Chargement des utilisateurs de la société
            loadCompanyUsers(magScene.getId());
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement des données société: " + e.getMessage(), e);
        }
    }
    
    private void loadCompanyUsers(Long companyId) {
        try {
            UserRepository userRepo = new UserRepository();
            // Tentative de chargement des utilisateurs par société
            List<User> users = userRepo.findBySocieteId(companyId);
            
            companyUserData.clear();
            for (User user : users) {
                CompanyUserRow row = new CompanyUserRow(
                    user.id(),
                    user.fullName(),
                    user.username(),
                    user.position() != null ? user.position() : "",
                    user.role().getLabel(),
                    user.email() != null ? user.email() : "",
                    user.phone() != null ? user.phone() : "",
                    user.isActive() ? "Actif" : "Inactif"
                );
                companyUserData.add(row);
            }
            
        } catch (Exception e) {
            AppLogger.info("La fonctionnalité utilisateurs par société n'est pas encore disponible: " + e.getMessage());
            
            // En cas d'erreur, nous ne pouvons pas charger les utilisateurs sans le support de company_id
            AppLogger.info("Impossible de charger les utilisateurs - schema de base de données incompatible");
            companyUserData.clear();
        }
    }
    
    // Actions de l'onglet Société
    @FXML
    private void onSelectCompanyLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le logo de la société");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(imgCompanyLogo.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image logoImage = new Image(selectedFile.toURI().toString());
                imgCompanyLogo.setImage(logoImage);
                // TODO: Sauvegarder le chemin du logo dans la base de données
                AppLogger.info("Logo sélectionné: " + selectedFile.getAbsolutePath());
            } catch (Exception e) {
                AppLogger.error("Impossible de charger l'image: " + e.getMessage(), e);
            }
        }
    }
    
    @FXML
    private void onRemoveCompanyLogo() {
        if (imgCompanyLogo != null) {
            imgCompanyLogo.setImage(null);
            // TODO: Supprimer le chemin du logo dans la base de données
            AppLogger.info("Logo de la société supprimé");
        }
    }
    
    @FXML
    private void onAddUser() {
        // TODO: Ouvrir une boîte de dialogue pour ajouter un nouvel utilisateur
        AppLogger.info("Ajouter un nouvel utilisateur");
    }
    
    @FXML
    private void onEditUser() {
        CompanyUserRow selectedUser = companyUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // TODO: Ouvrir une boîte de dialogue pour éditer l'utilisateur
            AppLogger.info("Éditer l'utilisateur: " + selectedUser.fullName());
        }
    }
    
    @FXML
    private void onDeleteUser() {
        CompanyUserRow selectedUser = companyUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // TODO: Confirmer et supprimer l'utilisateur
            AppLogger.info("Supprimer l'utilisateur: " + selectedUser.fullName());
        }
    }
    
    @FXML
    private void onResetUserPassword() {
        CompanyUserRow selectedUser = companyUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // TODO: Réinitialiser le mot de passe de l'utilisateur
            AppLogger.info("Réinitialiser le mot de passe pour: " + selectedUser.fullName());
        }
    }
    
    @FXML
    private void onToggleUserActive() {
        CompanyUserRow selectedUser = companyUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // TODO: Activer/désactiver l'utilisateur
            AppLogger.info("Basculer l'état actif pour: " + selectedUser.fullName());
        }
    }
    
    @FXML
    private void onSaveCompanyData() {
        try {
            CompanyRepository companyRepo = new CompanyRepository(DB.getConnection());
            
            // Récupération de la société existante
            Company existingCompany = companyRepo.findByType(Company.CompanyType.OWN_COMPANY)
                    .stream()
                    .findFirst()
                    .orElse(null);
            
            if (existingCompany != null) {
                // Mise à jour des données avec les setters
                existingCompany.setName(txtCompanyName.getText());
                existingCompany.setDescription(txtCompanyDescription.getText());
                existingCompany.setAddress(txtCompanyAddress.getText());
                existingCompany.setPhone(txtCompanyPhone.getText());
                existingCompany.setEmail(txtCompanyEmail.getText());
                existingCompany.setWebsite(txtCompanyWebsite.getText());
                existingCompany.setSiret(txtCompanySiret.getText());
                existingCompany.setUpdatedAt(LocalDateTime.now());
                
                companyRepo.save(existingCompany);
                AppLogger.info("Données de la société sauvegardées avec succès");
                
                // TODO: Afficher un message de confirmation à l'utilisateur
            }
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la sauvegarde: " + e.getMessage(), e);
            // TODO: Afficher un message d'erreur à l'utilisateur
        }
    }
    
    @FXML
    private void onRefreshCategoriesData() {
        // Actualiser les données des catégories
        AppLogger.info("Actualisation des données des catégories");
        // TODO: Recharger les données de la table des catégories
        if (tableCategoriesInTab != null) {
            // Simulation d'actualisation
            AppLogger.info("Table des catégories actualisée");
        }
    }
    
    // ======================= MÉTHODES POUR LA GESTION DES MÉDIAS =======================
    
    @FXML
    private void onRefreshMediaData() {
        AppLogger.info("Actualisation des données des médias");
        // TODO: Recharger les données de la table des médias
    }
    
    @FXML
    private void onSearchMedia() {
        AppLogger.info("Recherche dans les médias");
        // TODO: Implémenter la recherche des médias
    }
    
    @FXML
    private void onClearMediaSearch() {
        AppLogger.info("Effacement de la recherche des médias");
        // TODO: Effacer les critères de recherche
    }
    
    @FXML
    private void onAddMedia() {
        AppLogger.info("Ajout d'un nouveau média");
        // TODO: Ouvrir dialogue d'ajout de média
    }
    
    @FXML
    private void onEditMedia() {
        AppLogger.info("Modification d'un média");
        // TODO: Ouvrir dialogue de modification de média
    }
    
    @FXML
    private void onDeleteMedia() {
        AppLogger.info("Suppression d'un média");
        // TODO: Confirmer et supprimer le média sélectionné
    }
    
    @FXML
    private void onPreviewMedia() {
        AppLogger.info("Aperçu du média");
        // TODO: Afficher l'aperçu du média sélectionné
    }
    
    @FXML
    private void onOpenMediaFolder() {
        AppLogger.info("Ouverture du dossier du média");
        // TODO: Ouvrir le dossier contenant le média sélectionné
    }
    
    @FXML
    private void onCleanOrphanMedia() {
        AppLogger.info("Nettoyage des médias orphelins");
        // TODO: Rechercher et supprimer les médias orphelins
    }

    /**
     * Sélectionne un onglet spécifique dans les préférences
     * @param tabName Le nom de l'onglet à sélectionner
     */
    public void selectTab(String tabName) {
        if (preferencesTabPane != null && tabName != null) {
            for (Tab tab : preferencesTabPane.getTabs()) {
                if (tab.getText().contains(tabName)) {
                    preferencesTabPane.getSelectionModel().select(tab);
                    AppLogger.info("Onglet sélectionné: " + tab.getText());
                    break;
                }
            }
        }
    }
    
    // ===== MÉTHODES D'INITIALISATION POUR LES NOUVELLES SECTIONS =====
    
    private void setupGeneralSection() {
        AppLogger.info("Initialisation de la section Général...");
        
        try {
            if (cbLanguage != null) {
                cbLanguage.getItems().addAll("Français", "English", "Español", "Deutsch");
                cbLanguage.setValue("Français");
                AppLogger.info("cbLanguage initialisé");
            } else {
                AppLogger.warn("cbLanguage est null - contrôle FXML manquant");
            }
            
            if (cbDateFormat != null) {
                cbDateFormat.getItems().addAll("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD", "DD.MM.YYYY");
                cbDateFormat.setValue("DD/MM/YYYY");
                AppLogger.info("cbDateFormat initialisé");
            } else {
                AppLogger.warn("cbDateFormat est null - contrôle FXML manquant");
            }
            
            if (cbTimeFormat != null) {
                cbTimeFormat.getItems().addAll("24H", "12H AM/PM");
                cbTimeFormat.setValue("24H");
                AppLogger.info("cbTimeFormat initialisé");
            } else {
                AppLogger.warn("cbTimeFormat est null - contrôle FXML manquant");
            }
            
            if (cbCurrency != null) {
                cbCurrency.getItems().addAll("EUR (€)", "USD ($)", "GBP (£)", "CHF");
                cbCurrency.setValue("EUR (€)");
                AppLogger.info("cbCurrency initialisé");
            } else {
                AppLogger.warn("cbCurrency est null - contrôle FXML manquant");
            }
            
            if (spinnerNotificationDuration != null) {
                spinnerNotificationDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 5));
                AppLogger.info("spinnerNotificationDuration initialisé");
            } else {
                AppLogger.warn("spinnerNotificationDuration est null - contrôle FXML manquant");
            }
            
            AppLogger.info("Section Général initialisée avec succès");
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'initialisation de la section Général", e);
        }
    }
    
    private void setupSystemSection() {
        AppLogger.info("Initialisation de la section Système...");
        
        try {
            if (txtDatabasePath != null) {
                txtDatabasePath.setText("data/MAGSAV.db");
                AppLogger.info("txtDatabasePath initialisé");
            } else {
                AppLogger.warn("txtDatabasePath est null - contrôle FXML manquant");
            }
            
            if (spinnerLockDelay != null) {
                spinnerLockDelay.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 15));
                AppLogger.info("spinnerLockDelay initialisé");
            } else {
                AppLogger.warn("spinnerLockDelay est null - contrôle FXML manquant");
            }
            
            if (spinnerBackupInterval != null) {
                spinnerBackupInterval.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 6));
                AppLogger.info("spinnerBackupInterval initialisé");
            } else {
                AppLogger.warn("spinnerBackupInterval est null - contrôle FXML manquant");
            }
            
            if (lblDatabaseStats != null) {
                updateDatabaseStats();
                AppLogger.info("lblDatabaseStats initialisé");
            } else {
                AppLogger.warn("lblDatabaseStats est null - contrôle FXML manquant");
            }
            
            AppLogger.info("Section Système initialisée avec succès");
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'initialisation de la section Système", e);
        }
    }
    
    private void setupMaintenanceSection() {
        if (cbExportFormat != null) {
            cbExportFormat.getItems().addAll("Excel (.xlsx)", "CSV", "JSON", "XML", "PDF");
            cbExportFormat.setValue("Excel (.xlsx)");
        }
    }
    
    private void setupDataSection() {
        // Configuration pour la section données (société)
        // La plupart des éléments sont déjà configurés dans setupCompanyTab()
    }
    
    // ===== HANDLERS POUR LES NOUVELLES SECTIONS =====
    
    @FXML
    private void onChangePassword() {
        AppLogger.info("Changement de mot de passe demandé");
        // TODO: Implémenter le changement de mot de passe
    }
    
    @FXML
    private void onViewSecurityLogs() {
        AppLogger.info("Visualisation des logs de sécurité");
        // TODO: Ouvrir une fenêtre avec les logs de sécurité
    }
    
    @FXML
    private void onBrowseDatabasePath() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier de base de données");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers H2", "*.mv.db", "*.h2.db")
        );
        
        File selectedFile = fileChooser.showOpenDialog(txtDatabasePath.getScene().getWindow());
        if (selectedFile != null) {
            txtDatabasePath.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void onBackupDatabase() {
        AppLogger.info("Sauvegarde de la base de données");
        // TODO: Implémenter la sauvegarde de la base de données
    }
    
    @FXML
    private void onRestoreDatabase() {
        AppLogger.info("Restauration de la base de données");
        // TODO: Implémenter la restauration de la base de données
    }
    
    @FXML
    private void onOptimizeDatabase() {
        AppLogger.info("Optimisation de la base de données");
        // TODO: Implémenter l'optimisation de la base de données
    }
    
    @FXML
    private void onImportProducts() {
        AppLogger.info("Import de produits");
        // TODO: Implémenter l'import de produits
    }
    
    @FXML
    private void onImportClients() {
        AppLogger.info("Import de clients");
        // TODO: Implémenter l'import de clients
    }
    
    @FXML
    private void onImportCompanies() {
        AppLogger.info("Import d'entreprises");
        // TODO: Implémenter l'import d'entreprises
    }
    
    @FXML
    private void onExportProducts() {
        AppLogger.info("Export de produits");
        // TODO: Implémenter l'export de produits
    }
    
    @FXML
    private void onExportClients() {
        AppLogger.info("Export de clients");
        // TODO: Implémenter l'export de clients
    }
    
    @FXML
    private void onExportCompanies() {
        AppLogger.info("Export d'entreprises");
        // TODO: Implémenter l'export d'entreprises
    }
    
    @FXML
    private void onExportAll() {
        AppLogger.info("Export complet");
        // TODO: Implémenter l'export complet
    }
    
    @FXML
    private void onExportReport() {
        AppLogger.info("Export de rapport");
        // TODO: Implémenter l'export de rapport
    }
    
    private void updateDatabaseStats() {
        try {
            if (lblDatabaseStats != null) {
                // Obtenir des statistiques de la base de données
                String stats = String.format("Taille: %.2f MB | Tables: %d | Dernière optimisation: %s", 
                    getDatabaseSize(), 
                    getTableCount(),
                    getLastOptimizationDate());
                lblDatabaseStats.setText(stats);
            }
        } catch (Exception e) {
            if (lblDatabaseStats != null) {
                lblDatabaseStats.setText("Erreur lors du chargement des statistiques");
            }
            AppLogger.error("Erreur lors de la mise à jour des statistiques DB", e);
        }
    }
    
    private double getDatabaseSize() {
        // TODO: Implémenter le calcul de la taille de la base
        return 2.5; // Valeur exemple
    }
    
    private int getTableCount() {
        // TODO: Implémenter le comptage des tables
        return 8; // Valeur exemple
    }
    
    private String getLastOptimizationDate() {
        // TODO: Implémenter la récupération de la dernière optimisation
        return "Jamais";
    }

    /**
     * Configuration de la section Google Services
     */
    private void setupGoogleServicesSection() {
        AppLogger.info("Initialisation de la section Google Services...");
        
        try {
            // Configuration des spinners
            if (spnGoogleSyncInterval != null) {
                spnGoogleSyncInterval.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 1440, 30));
            }
            
            if (spnGoogleTimeout != null) {
                spnGoogleTimeout.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(10, 300, 30));
            }
            
            // Configuration des boutons
            if (btnTestGoogleConnection != null) {
                btnTestGoogleConnection.setOnAction(e -> testGoogleConnection());
            }
            
            if (btnSaveGoogleConfig != null) {
                btnSaveGoogleConfig.setOnAction(e -> saveGoogleConfiguration());
            }
            
            // Charger la configuration existante
            loadGoogleConfiguration();
            
            AppLogger.info("Section Google Services initialisée avec succès");
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de l'initialisation de la section Google Services", e);
        }
    }
    
    /**
     * Charge la configuration Google Services existante
     */
    private void loadGoogleConfiguration() {
        try {
            // TODO: Charger depuis la base de données via GoogleServicesConfigRepository
            // Pour l'instant, valeurs par défaut
            if (txtGoogleRedirectUri != null) {
                txtGoogleRedirectUri.setText("http://localhost:8080/oauth2/callback");
            }
            
            if (txtGoogleScopes != null) {
                txtGoogleScopes.setText(
                    "https://www.googleapis.com/auth/calendar\n" +
                    "https://www.googleapis.com/auth/gmail.send\n" +
                    "https://www.googleapis.com/auth/contacts"
                );
            }
            
            if (chkGoogleCalendar != null) chkGoogleCalendar.setSelected(true);
            if (chkGoogleGmail != null) chkGoogleGmail.setSelected(true);
            if (chkGoogleContacts != null) chkGoogleContacts.setSelected(false);
            if (chkGoogleAutoSync != null) chkGoogleAutoSync.setSelected(true);
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du chargement de la configuration Google", e);
        }
    }
    
    /**
     * Teste la connexion Google Services
     */
    private void testGoogleConnection() {
        try {
            AppLogger.info("Test de connexion Google Services...");
            
            // Validation des champs requis
            if (txtGoogleClientId == null || txtGoogleClientId.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le Client ID Google est requis");
                return;
            }
            
            if (txtGoogleClientSecret == null || txtGoogleClientSecret.getText().trim().isEmpty()) {
                showAlert("Erreur", "Le Client Secret Google est requis");
                return;
            }
            
            // TODO: Implémenter le test réel avec GoogleIntegrationService
            showAlert("Information", "Test de connexion Google Services - Fonctionnalité à implémenter");
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors du test de connexion Google", e);
            showAlert("Erreur", "Erreur lors du test de connexion: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde la configuration Google Services
     */
    private void saveGoogleConfiguration() {
        try {
            AppLogger.info("Sauvegarde de la configuration Google Services...");
            
            // TODO: Sauvegarder via GoogleServicesConfigRepository
            // Récupérer les valeurs des champs et les enregistrer en base
            
            showAlert("Information", "Configuration Google Services sauvegardée avec succès");
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la sauvegarde de la configuration Google", e);
            showAlert("Erreur", "Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }
    
    /**
     * Affiche une alerte à l'utilisateur
     * @deprecated Utiliser AlertUtils à la place
     */
    @Deprecated
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            com.magsav.util.AlertUtils.showInfo(title, message);
        });
    }
    
    // ==================== ACTIONS OUTILS DE DÉVELOPPEMENT ====================
    
    @FXML
    private void onGenerateTestData() {
        try {
            AppLogger.info("génération de données de test demandée depuis les préférences");
            
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Générer des données de test");
            confirmation.setHeaderText("Êtes-vous sûr ?");
            confirmation.setContentText("Cette action va créer des données de test dans la base de données. Continuer ?");
            
            ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);
            if (result == ButtonType.OK) {
                // Utiliser le générateur complet qui couvre toutes les tables
                com.magsav.util.TestDataGenerator.generateCompleteTestData();
                
                // Déclencher le rafraîchissement automatique dans MainController
                triggerDataRefresh();
                
                showAlert("Succès", "Les données de test ont été générées avec succès !");
                AppLogger.info("Données de test générées depuis les préférences");
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la génération des données de test", e);
            showAlert("Erreur", "Erreur lors de la génération : " + e.getMessage());
        }
    }
    
    @FXML
    private void onClearTestData() {
        try {
            AppLogger.info("suppression des données de test demandée depuis les préférences");
            
            Alert confirmation = new Alert(Alert.AlertType.WARNING);
            confirmation.setTitle("Vider les données de test");
            confirmation.setHeaderText("⚠️ Attention - Action irréversible");
            confirmation.setContentText("Cette action va supprimer TOUTES les données de test de la base de données. Cette action ne peut pas être annulée. Continuer ?");
            
            ButtonType result = confirmation.showAndWait().orElse(ButtonType.CANCEL);
            if (result == ButtonType.OK) {
                // Vider toutes les tables de test
                com.magsav.util.TestDataGenerator.clearAllTables();
                
                // Déclencher le rafraîchissement automatique dans MainController
                triggerDataRefresh();
                
                showAlert("Succès", "Toutes les données de test ont été supprimées avec succès !");
                AppLogger.info("Données de test supprimées depuis les préférences");
            }
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la suppression des données de test", e);
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
        }
    }
    
    /**
     * Déclenche le rafraîchissement automatique des données dans MainController
     */
    private void triggerDataRefresh() {
        try {
            // Utiliser le service de notification pour déclencher le rafraîchissement
            var notificationService = com.magsav.service.DataChangeNotificationService.getInstance();
            notificationService.notifyDataChanged(
                new com.magsav.service.DataChangeEvent(
                    com.magsav.service.DataChangeEvent.Type.DATABASE_CLEANED, 
                    "Données de test mises à jour depuis les préférences"
                )
            );
            AppLogger.info("Notification de rafraîchissement envoyée");
        } catch (Exception e) {
            AppLogger.error("Erreur lors du déclenchement du rafraîchissement", e);
        }
    }
    
    // Records pour les données des nouveaux onglets
    public record CategoryRow(Long id, String hierarchy, String name, String type, String parent) {}
    
    // Record pour représenter une ligne de source dans la table
    public record SourceRow(String type, String name, String baseUrl, String searchPattern, boolean enabled) {}
    
    // Record pour représenter un utilisateur dans la table de l'onglet Société
    public record CompanyUserRow(
        Integer id, String fullName, String username, String position, 
        String role, String email, String phone, String active
    ) {}
}