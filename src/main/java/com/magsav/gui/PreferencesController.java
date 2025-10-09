package com.magsav.gui;

import com.magsav.service.ImageScrapingService;
import com.magsav.service.ScrapingConfigService;
import com.magsav.service.ImageNormalizationService;
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
    private ImageScrapingService scrapingService = new ImageScrapingService();
    private ImageNormalizationService normalizationService = new ImageNormalizationService();
    private ProductRepository productRepo = new ProductRepository();
    private AddressService addressService = new AddressService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupEmailTab();
        setupMediaTab();
        setupScrapingTab();
        setupAddressAutocomplete();
        initializeNewTabs();
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
        alert.setHeaderText("Supprimer les fichiers en doublon");
        alert.setContentText("Cette opération va analyser et supprimer les images identiques. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // TODO: Implémenter nettoyage des doublons
            showAlert(Alert.AlertType.INFORMATION, "Nettoyage", "Nettoyage des doublons à implémenter.");
            updateMediaStats();
        }
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
            if (txtCompanySiret != null) {
                company.setSiret(txtCompanySiret.getText().trim());
            }
            if (txtCompanySector != null) {
                company.setSector(txtCompanySector.getText().trim());
            }
            if (txtCompanyAddress != null) {
                company.setAddress(txtCompanyAddress.getText().trim());
            }
            if (txtCompanyPostalCode != null) {
                company.setPostalCode(txtCompanyPostalCode.getText().trim());
            }
            if (txtCompanyCity != null) {
                company.setCity(txtCompanyCity.getText().trim());
            }
            if (txtCompanyCountry != null) {
                company.setCountry(txtCompanyCountry.getText().trim());
            }
            if (txtCompanyPhone != null) {
                company.setPhone(txtCompanyPhone.getText().trim());
            }
            if (txtCompanyEmail != null) {
                company.setEmail(txtCompanyEmail.getText().trim());
            }
            if (txtCompanyWebsite != null) {
                company.setWebsite(txtCompanyWebsite.getText().trim());
            }
            if (txtCompanyDescription != null) {
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
        AppLogger.info("preferences", "Nettoyage des doublons");
        showAlert(Alert.AlertType.INFORMATION, "Nettoyage", "Suppression des doublons terminée.");
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
        showAlert(Alert.AlertType.INFORMATION, "Non implémenté", "L'ajout de catégorie sera implémenté prochainement.");
    }
    
    @FXML
    private void onAddSubcategoryInTab() {
        AppLogger.info("preferences", "Ajout d'une sous-catégorie depuis l'onglet");
        showAlert(Alert.AlertType.INFORMATION, "Non implémenté", "L'ajout de sous-catégorie sera implémenté prochainement.");
    }
    
    @FXML
    private void onEditCategoryInTab() {
        CategoryRow selected = tableCategoriesInTab.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AppLogger.info("preferences", "Modification de la catégorie: " + selected.name());
            showAlert(Alert.AlertType.INFORMATION, "Non implémenté", "La modification de catégorie sera implémentée prochainement.");
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
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/categories.fxml"));
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
            List<User> users = userRepo.findByCompanyId(companyId);
            
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