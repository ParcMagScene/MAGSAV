package com.magsav.gui;

import com.magsav.model.GoogleServicesConfig;
import com.magsav.service.google.GoogleIntegrationService;
import com.magsav.util.AppLogger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la configuration des services Google
 */
public class GoogleConfigController implements Initializable {
    
    // Interface de configuration
    @FXML private TabPane tabPane;
    
    // Onglet Configuration OAuth2
    @FXML private Tab tabAuth;
    @FXML private TextField txtNom;
    @FXML private TextField txtClientId;
    @FXML private PasswordField txtClientSecret;
    @FXML private TextField txtRedirectUri;
    @FXML private TextArea txtScopes;
    @FXML private Button btnSaveConfig;
    @FXML private Button btnAuthorize;
    @FXML private TextField txtAuthCode;
    @FXML private Button btnExchangeCode;
    @FXML private Label lblAuthStatus;
    
    // Onglet Calendar
    @FXML private Tab tabCalendar;
    @FXML private CheckBox chkCalendarActif;
    @FXML private TextField txtCalendrierPrincipal;
    @FXML private Spinner<Integer> spnIntervalleSync;
    @FXML private Button btnTestCalendar;
    @FXML private Label lblCalendarStatus;
    
    // Onglet Gmail
    @FXML private Tab tabGmail;
    @FXML private CheckBox chkGmailActif;
    @FXML private TextField txtEmailExpediteur;
    @FXML private TextArea txtSignatureEmail;
    @FXML private Button btnTestGmail;
    @FXML private Label lblGmailStatus;
    
    // Onglet Contacts
    @FXML private Tab tabContacts;
    @FXML private CheckBox chkContactsActif;
    @FXML private CheckBox chkSyncContactsAuto;
    @FXML private Button btnTestContacts;
    @FXML private Button btnSyncContacts;
    @FXML private Label lblContactsStatus;
    @FXML private TextArea txtContactsLog;
    
    // Onglet État
    @FXML private Tab tabStatus;
    @FXML private TableView<GoogleServicesConfig> tblConfigurations;
    @FXML private TableColumn<GoogleServicesConfig, String> colNom;
    @FXML private TableColumn<GoogleServicesConfig, Boolean> colActif;
    @FXML private TableColumn<GoogleServicesConfig, String> colDateCreation;
    @FXML private Button btnRefreshConfigs;
    @FXML private Button btnDeleteConfig;
    @FXML private Label lblServiceStatus;
    
    // Services
    private GoogleIntegrationService googleService;
    private GoogleServicesConfig currentConfig;
    
    // Méthodes FXML manquantes
    @FXML private void onAutoriser() {
        startAuthorization();
    }
    
    @FXML private void onSaveConfig() {
        saveConfiguration();
    }
    
    @FXML private void onExchangeCode() {
        exchangeAuthorizationCode();
    }
    
    @FXML private void onTestCalendar() {
        testCalendarConnection();
    }
    
    @FXML private void onTestGmail() {
        testGmailConnection();
    }
    
    @FXML private void onTestContacts() {
        testContactsConnection();
    }
    
    @FXML private void onSyncContacts() {
        syncContacts();
    }
    
    @FXML private void onTesterCalendar() {
        testCalendarConnection();
    }
    
    @FXML private void onTesterGmail() {
        testGmailConnection();
    }
    
    @FXML private void onTesterContacts() {
        testContactsConnection();
    }
    
    @FXML private void onSauvegarder() {
        saveConfiguration();
    }
    
    @FXML private void onAnnuler() {
        // Fermer la fenêtre ou réinitialiser les champs
        loadConfiguration();
    }
    
    @FXML private void onTesterTout() {
        testCalendarConnection();
        testGmailConnection();
        testContactsConnection();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        AppLogger.info("gui", "Initialisation contrôleur configuration Google");
        
        googleService = GoogleIntegrationService.getInstance();
        currentConfig = googleService.getCurrentConfig();
        
        setupUI();
        loadConfiguration();
        refreshServiceStatus();
    }
    
    private void setupUI() {
        // Configuration des spinners
        if (spnIntervalleSync != null) {
            spnIntervalleSync.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 120, 15, 5));
        }
        
        // Configuration du tableau - commenté car pas présent dans le FXML
        /*colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colActif.setCellValueFactory(new PropertyValueFactory<>("actif"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));
        
        // Configuration des cell factories pour l'affichage
        colActif.setCellFactory(col -> new TableCell<GoogleServicesConfig, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✓ Actif" : "✗ Inactif");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });*/
        
        // Événements
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        if (btnSaveConfig != null) btnSaveConfig.setOnAction(e -> saveConfiguration());
        if (btnAuthorize != null) btnAuthorize.setOnAction(e -> startAuthorization());
        if (btnExchangeCode != null) btnExchangeCode.setOnAction(e -> exchangeAuthorizationCode());
        
        if (btnTestCalendar != null) btnTestCalendar.setOnAction(e -> testCalendarConnection());
        if (btnTestGmail != null) btnTestGmail.setOnAction(e -> testGmailConnection());
        if (btnTestContacts != null) btnTestContacts.setOnAction(e -> testContactsConnection());
        
        if (btnSyncContacts != null) btnSyncContacts.setOnAction(e -> syncContacts());
        if (btnRefreshConfigs != null) btnRefreshConfigs.setOnAction(e -> refreshConfigurations());
        if (btnDeleteConfig != null) btnDeleteConfig.setOnAction(e -> deleteSelectedConfiguration());
        
        // Binding des champs avec la configuration
        setupBindings();
    }
    
    private void setupBindings() {
        if (currentConfig != null) {
            // Liaison bidirectionnelle des champs
            if (txtNom != null) txtNom.textProperty().bindBidirectional(currentConfig.nomProperty());
            if (txtClientId != null) txtClientId.textProperty().bindBidirectional(currentConfig.clientIdProperty());
            if (txtClientSecret != null) txtClientSecret.textProperty().bindBidirectional(currentConfig.clientSecretProperty());
            if (txtRedirectUri != null) txtRedirectUri.textProperty().bindBidirectional(currentConfig.redirectUriProperty());
            if (txtScopes != null) txtScopes.textProperty().bindBidirectional(currentConfig.scopesProperty());
            
            if (chkCalendarActif != null) chkCalendarActif.selectedProperty().bindBidirectional(currentConfig.syncCalendarActifProperty());
            if (txtCalendrierPrincipal != null) txtCalendrierPrincipal.textProperty().bindBidirectional(currentConfig.calendrierPrincipalProperty());
            if (spnIntervalleSync != null) spnIntervalleSync.getValueFactory().valueProperty().bindBidirectional(currentConfig.intervalleSyncProperty().asObject());
            
            if (chkGmailActif != null) chkGmailActif.selectedProperty().bindBidirectional(currentConfig.gmailActifProperty());
            if (txtEmailExpediteur != null) txtEmailExpediteur.textProperty().bindBidirectional(currentConfig.emailExpéditeurProperty());
            if (txtSignatureEmail != null) txtSignatureEmail.textProperty().bindBidirectional(currentConfig.signatureEmailProperty());
            
            if (chkContactsActif != null) chkContactsActif.selectedProperty().bindBidirectional(currentConfig.contactsActifProperty());
            if (chkSyncContactsAuto != null) chkSyncContactsAuto.selectedProperty().bindBidirectional(currentConfig.syncContactsAutoProperty());
        }
    }
    
    private void loadConfiguration() {
        refreshConfigurations();
        
        if (currentConfig != null) {
            updateAuthStatus();
        }
    }
    
    @FXML
    private void saveConfiguration() {
        if (currentConfig == null) {
            showError("Aucune configuration à sauvegarder");
            return;
        }
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Sauvegarde de la configuration...");
                return googleService.reloadConfiguration();
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                if (getValue()) {
                    showInfo("Configuration sauvegardée avec succès");
                    refreshServiceStatus();
                } else {
                    showError("Erreur lors de la sauvegarde");
                }
            }
        };
        
        runTask(task, "Sauvegarde configuration");
    }
    
    @FXML
    private void startAuthorization() {
        if (currentConfig == null || !currentConfig.isConfigured()) {
            showError("Configuration OAuth2 incomplète. Veuillez remplir Client ID et Client Secret.");
            return;
        }
        
        try {
            String authUrl = googleService.getAuthorizationUrl();
            
            // Ouvrir l'URL dans le navigateur
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(authUrl));
                showInfo("Navigateur ouvert pour l'autorisation. Copiez le code reçu et collez-le ci-dessous.");
            } else {
                showInfo("Ouvrez cette URL dans votre navigateur :\n" + authUrl);
            }
            
        } catch (Exception e) {
            AppLogger.error("Erreur ouverture URL autorisation: " + e.getMessage());
            showError("Erreur lors de l'ouverture du navigateur : " + e.getMessage());
        }
    }
    
    @FXML
    private void exchangeAuthorizationCode() {
        String code = txtAuthCode.getText().trim();
        if (code.isEmpty()) {
            showError("Veuillez saisir le code d'autorisation");
            return;
        }
        
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Échange du code d'autorisation...");
                return googleService.exchangeAuthorizationCode(code).get();
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                if (getValue()) {
                    showInfo("Authentification réussie !");
                    txtAuthCode.clear();
                    updateAuthStatus();
                    refreshServiceStatus();
                } else {
                    showError("Échec de l'authentification");
                }
            }
        };
        
        runTask(task, "Authentification Google");
    }
    
    @FXML
    private void testCalendarConnection() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Test connexion Google Calendar...");
                return googleService.isCalendarAvailable();
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                String status = getValue() ? "✓ Calendar OK" : "✗ Calendar indisponible";
                lblCalendarStatus.setText(status);
                lblCalendarStatus.setStyle(getValue() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            }
        };
        
        runTask(task, "Test Calendar");
    }
    
    @FXML
    private void testGmailConnection() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Test connexion Gmail...");
                return googleService.isGmailAvailable();
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                String status = getValue() ? "✓ Gmail OK" : "✗ Gmail indisponible";
                lblGmailStatus.setText(status);
                lblGmailStatus.setStyle(getValue() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            }
        };
        
        runTask(task, "Test Gmail");
    }
    
    @FXML
    private void testContactsConnection() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                updateMessage("Test connexion Google Contacts...");
                return googleService.isContactsAvailable();
            }
            
            @Override
            protected void succeeded() {
                super.succeeded();
                String status = getValue() ? "✓ Contacts OK" : "✗ Contacts indisponible";
                lblContactsStatus.setText(status);
                lblContactsStatus.setStyle(getValue() ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            }
        };
        
        runTask(task, "Test Contacts");
    }
    
    @FXML
    private void syncContacts() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateMessage("Synchronisation des contacts...");
                List<?> contacts = googleService.syncContacts().get();
                
                Platform.runLater(() -> {
                    txtContactsLog.appendText("Synchronisation terminée : " + contacts.size() + " contacts\n");
                });
                
                return null;
            }
        };
        
        runTask(task, "Synchronisation Contacts");
    }
    
    @FXML
    private void refreshConfigurations() {
        // Simuler le chargement des configurations (à implémenter)
        if (tblConfigurations != null) {
            tblConfigurations.getItems().clear();
            if (currentConfig != null) {
                tblConfigurations.getItems().add(currentConfig);
            }
        }
    }
    
    @FXML
    private void deleteSelectedConfiguration() {
        GoogleServicesConfig selected = tblConfigurations.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner une configuration à supprimer");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la configuration");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette configuration ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Implémenter la suppression
            showInfo("Configuration supprimée");
            refreshConfigurations();
        }
    }
    
    private void updateAuthStatus() {
        if (currentConfig != null && currentConfig.hasValidTokens()) {
            lblAuthStatus.setText("✓ Authentifié");
            lblAuthStatus.setStyle("-fx-text-fill: green;");
        } else {
            lblAuthStatus.setText("✗ Non authentifié");
            lblAuthStatus.setStyle("-fx-text-fill: red;");
        }
    }
    
    private void refreshServiceStatus() {
        String status = "Services Google : ";
        if (googleService.isInitialized()) {
            status += "✓ Initialisés";
            status += " (Calendar: " + (googleService.isCalendarAvailable() ? "OK" : "KO");
            status += ", Gmail: " + (googleService.isGmailAvailable() ? "OK" : "KO");
            status += ", Contacts: " + (googleService.isContactsAvailable() ? "OK" : "KO") + ")";
        } else {
            status += "✗ Non initialisés";
        }
        
        lblServiceStatus.setText(status);
    }
    
    private void runTask(Task<?> task, String title) {
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) tabPane.getScene().getWindow();
        stage.close();
    }
}