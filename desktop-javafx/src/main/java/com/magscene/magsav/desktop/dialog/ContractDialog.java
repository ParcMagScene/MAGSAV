package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.model.Client;
import com.magscene.magsav.desktop.model.Contract;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import com.magscene.magsav.desktop.service.WindowPreferencesService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Dialogue pour créer ou modifier un contrat
 * Interface complète avec validation, async operations et gestion d'erreurs
 */
public class ContractDialog extends Dialog<Contract> {
    
    private final ApiService apiService;
    private final boolean isEditing;
    private boolean isReadOnlyMode;
    private final Contract originalContract;
    
    // Onglet 1: Informations Générales
    private TextField titleField;
    private ComboBox<Contract.ContractType> typeCombo;
    private ComboBox<Contract.ContractStatus> statusCombo;
    private ComboBox<Client> clientCombo;
    private TextField contractNumberField;
    private TextArea descriptionArea;
    
    // Onglet 2: Dates et durée
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private DatePicker signatureDatePicker;
    private TextField durationMonthsField;
    private CheckBox autoRenewalCheckBox;
    private TextField renewalNoticeDaysField;
    
    // Onglet 3: Conditions financières
    private TextField totalAmountField;
    private ComboBox<Contract.BillingFrequency> billingFrequencyCombo;
    private ComboBox<Contract.PaymentTerms> paymentTermsCombo;
    private TextField discountPercentageField;
    private TextField penaltyClauseField;
    
    // Onglet 4: Détails et notes
    private TextArea termsConditionsArea;
    private TextArea notesArea;
    private TextField assignedManagerField;
    
    // Contrôles
    private Button saveButton;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    
    public ContractDialog(ApiService apiService) {
        this(apiService, null, false);
    }
    
    public ContractDialog(ApiService apiService, Contract contract) {
        this(apiService, contract, false);
    }

    public ContractDialog(ApiService apiService, Contract contract, boolean readOnlyMode) {
        this.apiService = apiService;
        this.originalContract = contract;
        this.isEditing = contract != null && !readOnlyMode;
        this.isReadOnlyMode = readOnlyMode;
        
        setupDialog();
        createUI();
        setupValidation();
        loadClients();
        
        // Désactiver les champs en mode lecture seule
        if (isReadOnlyMode) {
            setFieldsReadOnly();
        }
        
        if (originalContract != null) {
            loadContractData();
        }
    }
    
    private void setupDialog() {
        // Configuration des titres (suppression des doublons d'intitulés)
        if (isReadOnlyMode) {
            setTitle("Détails du contrat");
        } else {
            setTitle(isEditing ? "Modifier le contrat" : "Nouveau contrat");
        }
        
        // Taille de la fenêtre
        getDialogPane().setPrefSize(800, 700);
        
        // Mémorisation de la taille et position du dialog
        WindowPreferencesService.getInstance().setupDialogMemory(getDialogPane(), "contract-dialog");
        
        // L'interface sera créée avec createUI() comme avant; // mais nous ajouterons les boutons personnalisés après
        createUI();
    }
    
    private void createUI() {
        // Utiliser CustomTabPane au lieu de TabPane JavaFX pour style unifié
        com.magscene.magsav.desktop.component.CustomTabPane tabPane = 
            new com.magscene.magsav.desktop.component.CustomTabPane();
        
        // Onglet 1: Informations Générales
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab generalTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Général", createGeneralPane(), "📄");
        tabPane.addTab(generalTab);
        
        // Onglet 2: Dates et durée
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab datesTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Dates", createDatesPane(), "📅");
        tabPane.addTab(datesTab);
        
        // Onglet 3: Financier
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab financialTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Financier", createFinancialPane(), "💰");
        tabPane.addTab(financialTab);
        
        // Onglet 4: Détails
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab detailsTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Détails", createDetailsPane(), "📝");
        tabPane.addTab(detailsTab);
        
        // Sélectionner le premier onglet
        tabPane.selectTab(0);
        
        // Barre de statut et boutons personnalisés
        VBox mainContainer = new VBox(10);
        mainContainer.getChildren().addAll(tabPane, createStatusBar());
        
        // Ajouter la barre de boutons standardisée
        HBox buttonBar = createStandardButtons();
        mainContainer.getChildren().add(buttonBar);
        
        getDialogPane().setContent(mainContainer);
        
        // Appliquer le thème dark au dialogue
        ThemeManager.getInstance().applyThemeToDialog(getDialogPane());
    }
    
    private HBox createStandardButtons() {
        if (isReadOnlyMode) {
            // Mode lecture seule : boutons Modifier et Fermer
            return ViewUtils.createDialogButtonBar(
                () -> {
                    // Action Modifier : ouvrir en mode édition
                    if (originalContract != null) {
                        ContractDialog editDialog = new ContractDialog(apiService, originalContract, false);
                        editDialog.showAndWait().ifPresent(result -> {
                            // Propager le résultat vers le parent si nécessaire
                            setResult(result);
                        });
                    }
                    forceClose();
                },
                this::forceClose,
                null
            );
        } else {
            // Mode édition : boutons Enregistrer et Annuler
            return ViewUtils.createDialogButtonBar(
                this::handleSave,
                this::forceClose,
                null
            );
        }
    }
    
    private void handleSave() {
        if (validateFormForSave()) {
            Contract result = createContractFromFields();
            setResult(result);
            forceClose();
        }
    }
    
    private boolean validateFormForSave() {
        StringBuilder errors = new StringBuilder();
        
        if (titleField.getText().trim().isEmpty()) {
            errors.append("- Le titre du contrat est obligatoire\n");
        }
        
        if (typeCombo.getValue() == null) {
            errors.append("- Le type de contrat est obligatoire\n");
        }
        
        if (statusCombo.getValue() == null) {
            errors.append("- Le statut du contrat est obligatoire\n");
        }
        
        if (clientCombo.getValue() == null) {
            errors.append("- Le client est obligatoire\n");
        }
        
        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreurs de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }
        
        return true;
    }
    
    /**
     * Force la fermeture du dialog même avec des boutons personnalisés
     */
    private void forceClose() {
        getDialogPane().getButtonTypes().clear();
        close();
        
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().hide();
        }
    }
    
    private Node createGeneralPane() {
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Titre *
        grid.add(new Label("Titre du contrat *"), 0, row);
        titleField = new TextField();
        titleField.setPromptText("Ex: Contrat de maintenance annuelle");
        titleField.setPrefWidth(300);
        grid.add(titleField, 1, row++);
        
        // Type *
        grid.add(new Label("Type de contrat *"), 0, row);
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(Contract.ContractType.values()));
        typeCombo.setConverter(new StringConverter<Contract.ContractType>() {
            @Override
            public String toString(Contract.ContractType type) {
                return type != null ? type.getDisplayName() : "";
            }
            
            @Override
            public Contract.ContractType fromString(String string) {
                return null;
            }
        });
        typeCombo.setPrefWidth(300);
        grid.add(typeCombo, 1, row++);
        
        // Statut
        grid.add(new Label("Statut"), 0, row);
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(Contract.ContractStatus.values()));
        statusCombo.setConverter(new StringConverter<Contract.ContractStatus>() {
            @Override
            public String toString(Contract.ContractStatus status) {
                return status != null ? status.getDisplayName() : "";
            }
            
            @Override
            public Contract.ContractStatus fromString(String string) {
                return null;
            }
        });
        statusCombo.setValue(Contract.ContractStatus.DRAFT);
        statusCombo.setPrefWidth(300);
        grid.add(statusCombo, 1, row++);
        
        // Numéro de contrat
        grid.add(new Label("Numéro contrat"), 0, row);
        contractNumberField = new TextField();
        contractNumberField.setPromptText("Ex: CTR-2024-001");
        contractNumberField.setPrefWidth(300);
        grid.add(contractNumberField, 1, row++);
        
        // Nom du client *
        grid.add(new Label("Client *"), 0, row);
        clientCombo = new ComboBox<>();
        clientCombo.setPromptText("Sélectionnez un client");
        clientCombo.setPrefWidth(300);
        clientCombo.setConverter(new StringConverter<Client>() {
            @Override
            public String toString(Client client) {
                return client != null ? client.getDisplayName() : "";
            }
            
            @Override
            public Client fromString(String string) {
                return null;
            }
        });
        grid.add(clientCombo, 1, row++);
        
        // Description
        grid.add(new Label("Description"), 0, row);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description du contrat...");
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setWrapText(true);
        grid.add(descriptionArea, 1, row++);
        
        return grid;
    }
    
    private Node createDatesPane() {
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Date de signature
        grid.add(new Label("Date de signature"), 0, row);
        signatureDatePicker = new DatePicker();
        signatureDatePicker.setValue(LocalDate.now());
        signatureDatePicker.setPrefWidth(200);
        grid.add(signatureDatePicker, 1, row++);
        
        // Date de début *
        grid.add(new Label("Date de début *"), 0, row);
        startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now());
        startDatePicker.setPrefWidth(200);
        grid.add(startDatePicker, 1, row++);
        
        // Date de fin *
        grid.add(new Label("Date de fin *"), 0, row);
        endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now().plusMonths(12));
        endDatePicker.setPrefWidth(200);
        grid.add(endDatePicker, 1, row++);
        
        // durée (mois)
        grid.add(new Label("durée (mois)"), 0, row);
        durationMonthsField = new TextField();
        durationMonthsField.setPromptText("12");
        durationMonthsField.setPrefWidth(100);
        grid.add(durationMonthsField, 1, row++);
        
        // Renouvellement automatique
        grid.add(new Label("Renouvellement auto"), 0, row);
        autoRenewalCheckBox = new CheckBox("Activer le renouvellement automatique");
        grid.add(autoRenewalCheckBox, 1, row++);
        
        // Préavis (jours)
        grid.add(new Label("Préavis (jours)"), 0, row);
        renewalNoticeDaysField = new TextField();
        renewalNoticeDaysField.setPromptText("30");
        renewalNoticeDaysField.setPrefWidth(100);
        renewalNoticeDaysField.setDisable(true);
        grid.add(renewalNoticeDaysField, 1, row++);
        
        // Liaison checkbox -> champ Préavis
        autoRenewalCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            renewalNoticeDaysField.setDisable(!newVal);
            if (!newVal) {
                renewalNoticeDaysField.clear();
            } else {
                renewalNoticeDaysField.setText("30");
            }
        });
        
        return grid;
    }
    
    private Node createFinancialPane() {
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Montant total *
        grid.add(new Label("Montant total (€) *"), 0, row);
        totalAmountField = new TextField();
        totalAmountField.setPromptText("Ex: 15000.00");
        totalAmountField.setPrefWidth(200);
        grid.add(totalAmountField, 1, row++);
        
        // Fréquence de facturation
        grid.add(new Label("Fréquence facturation"), 0, row);
        billingFrequencyCombo = new ComboBox<>(FXCollections.observableArrayList(Contract.BillingFrequency.values()));
        billingFrequencyCombo.setConverter(new StringConverter<Contract.BillingFrequency>() {
            @Override
            public String toString(Contract.BillingFrequency frequency) {
                return frequency != null ? frequency.getDisplayName() : "";
            }
            
            @Override
            public Contract.BillingFrequency fromString(String string) {
                return null;
            }
        });
        billingFrequencyCombo.setValue(Contract.BillingFrequency.MONTHLY);
        billingFrequencyCombo.setPrefWidth(200);
        grid.add(billingFrequencyCombo, 1, row++);
        
        // Conditions de paiement
        grid.add(new Label("Conditions paiement"), 0, row);
        paymentTermsCombo = new ComboBox<>(FXCollections.observableArrayList(Contract.PaymentTerms.values()));
        paymentTermsCombo.setConverter(new StringConverter<Contract.PaymentTerms>() {
            @Override
            public String toString(Contract.PaymentTerms terms) {
                return terms != null ? terms.getDisplayName() : "";
            }
            
            @Override
            public Contract.PaymentTerms fromString(String string) {
                return null;
            }
        });
        paymentTermsCombo.setValue(Contract.PaymentTerms.NET_30);
        paymentTermsCombo.setPrefWidth(200);
        grid.add(paymentTermsCombo, 1, row++);
        
        // Remise (%)
        grid.add(new Label("Remise (%)"), 0, row);
        discountPercentageField = new TextField();
        discountPercentageField.setPromptText("0.00");
        discountPercentageField.setPrefWidth(100);
        grid.add(discountPercentageField, 1, row++);
        
        // Clause de pénalité
        grid.add(new Label("Clause pénalité"), 0, row);
        penaltyClauseField = new TextField();
        penaltyClauseField.setPromptText("pénalité en cas de non-respect...");
        penaltyClauseField.setPrefWidth(400);
        grid.add(penaltyClauseField, 1, row++);
        
        return grid;
    }
    
    private Node createDetailsPane() {
        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Responsable
        grid.add(new Label("Responsable"), 0, row);
        assignedManagerField = new TextField();
        assignedManagerField.setPromptText("Nom du responsable");
        assignedManagerField.setPrefWidth(300);
        grid.add(assignedManagerField, 1, row++);
        
        // Termes et conditions
        grid.add(new Label("Termes et conditions"), 0, row);
        termsConditionsArea = new TextArea();
        termsConditionsArea.setPromptText("Termes et conditions du contrat...");
        termsConditionsArea.setPrefRowCount(6);
        termsConditionsArea.setWrapText(true);
        grid.add(termsConditionsArea, 1, row++);
        
        // Notes
        grid.add(new Label("Notes"), 0, row);
        notesArea = new TextArea();
        notesArea.setPromptText("Notes internes...");
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);
        grid.add(notesArea, 1, row++);
        
        return grid;
    }
    
    private Node createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10));
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        statusLabel = new Label("Remplissez les champs obligatoires (*)");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(16, 16);
        progressIndicator.setVisible(false);
        
        statusBar.getChildren().addAll(statusLabel, progressIndicator);
        
        return statusBar;
    }
    
    private void setupValidation() {
        // Validation en temps réel
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        clientCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        totalAmountField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        
        // Validation du montant (nombres uniquement)
        totalAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                totalAmountField.setText(oldVal);
            }
        });
        
        // Validation des champs numériques
        durationMonthsField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                durationMonthsField.setText(oldVal);
            }
        });
        
        renewalNoticeDaysField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                renewalNoticeDaysField.setText(oldVal);
            }
        });
        
        discountPercentageField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                discountPercentageField.setText(oldVal);
            }
        });
        
        // Validation des dates
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && endDatePicker.getValue() != null && newVal.isAfter(endDatePicker.getValue())) {
                statusLabel.setText("⚠️ La date de début ne peut pas être après la date de fin");
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
            }
        });
        
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && startDatePicker.getValue() != null && newVal.isBefore(startDatePicker.getValue())) {
                statusLabel.setText("⚠️ La date de fin ne peut pas être avant la date de début");
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
            }
        });
    }
    
    private void validateForm() {
        boolean isValid = titleField.getText() != null && !titleField.getText().trim().isEmpty() &&
                         typeCombo.getValue() != null &&
                         clientCombo.getValue() != null &&
                         startDatePicker.getValue() != null &&
                         endDatePicker.getValue() != null &&
                         totalAmountField.getText() != null && !totalAmountField.getText().trim().isEmpty();
        
        // Validation des dates
        boolean datesValid = true;
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
            datesValid = !startDatePicker.getValue().isAfter(endDatePicker.getValue());
        }
        
        saveButton.setDisable(!isValid || !datesValid);
        
        if (isValid && datesValid) {
            statusLabel.setText("✅ Formulaire valide");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        } else if (!datesValid) {
            statusLabel.setText("⚠️ Vérifiez les dates");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        } else {
            statusLabel.setText("Remplissez les champs obligatoires (*)");
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        }
    }
    
    private void loadContractData() {
        if (originalContract == null) return;
        
        titleField.setText(originalContract.getTitle());
        typeCombo.setValue(originalContract.getType());
        statusCombo.setValue(originalContract.getStatus());
        contractNumberField.setText(originalContract.getContractNumber());
        // Le client sera sélectionné après chargement de la liste
        descriptionArea.setText(originalContract.getDescription());
        
        signatureDatePicker.setValue(originalContract.getSignatureDate());
        startDatePicker.setValue(originalContract.getStartDate());
        endDatePicker.setValue(originalContract.getEndDate());
        
        if (originalContract.getRenewalPeriodMonths() != null) {
            durationMonthsField.setText(String.valueOf(originalContract.getRenewalPeriodMonths()));
        }
        
        autoRenewalCheckBox.setSelected(originalContract.getIsAutoRenewable() != null ? originalContract.getIsAutoRenewable() : false);
        if (originalContract.getNoticePeriodDays() != null) {
            renewalNoticeDaysField.setText(String.valueOf(originalContract.getNoticePeriodDays()));
        }
        
        if (originalContract.getTotalAmount() != null) {
            totalAmountField.setText(originalContract.getTotalAmount().toString());
        }
        
        billingFrequencyCombo.setValue(originalContract.getBillingFrequency());
        paymentTermsCombo.setValue(originalContract.getPaymentTerms());
        
        // Pour les champs non disponibles dans le modèle actuel, nous les laissons vides; // discountPercentageField.setText("");
        // penaltyClauseField.setText("");
        termsConditionsArea.setText(originalContract.getTermsAndConditions() != null ? originalContract.getTermsAndConditions() : "");
        notesArea.setText(originalContract.getNotes() != null ? originalContract.getNotes() : "");
        assignedManagerField.setText(originalContract.getMagsceneSignatory() != null ? originalContract.getMagsceneSignatory() : "");
    }
    
    private Contract createContractFromFields() {
        Contract contract = new Contract();
        
        contract.setTitle(titleField.getText().trim());
        contract.setType(typeCombo.getValue());
        contract.setStatus(statusCombo.getValue());
        contract.setContractNumber(contractNumberField.getText().trim());
        
        // Définir le client sélectionné
        Client selectedClient = clientCombo.getValue();
        if (selectedClient != null) {
            contract.setClientId(selectedClient.getId());
            contract.setClientName(selectedClient.getDisplayName());
        }
        
        contract.setDescription(descriptionArea.getText().trim());
        
        contract.setSignatureDate(signatureDatePicker.getValue());
        contract.setStartDate(startDatePicker.getValue());
        contract.setEndDate(endDatePicker.getValue());
        
        if (!durationMonthsField.getText().trim().isEmpty()) {
            try {
                contract.setRenewalPeriodMonths(Integer.parseInt(durationMonthsField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignorer si non valide
            }
        }
        
        contract.setIsAutoRenewable(autoRenewalCheckBox.isSelected());
        if (!renewalNoticeDaysField.getText().trim().isEmpty()) {
            try {
                contract.setNoticePeriodDays(Integer.parseInt(renewalNoticeDaysField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignorer si non valide
            }
        }
        
        if (!totalAmountField.getText().trim().isEmpty()) {
            try {
                contract.setTotalAmount(new BigDecimal(totalAmountField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignorer si non valide
            }
        }
        
        contract.setBillingFrequency(billingFrequencyCombo.getValue());
        contract.setPaymentTerms(paymentTermsCombo.getValue());
        
        // Les champs discount et penalty ne sont pas dans le modèle actuel; // Nous ne les assignons pas pour l'instant
        
        contract.setTermsAndConditions(termsConditionsArea.getText().trim());
        contract.setNotes(notesArea.getText().trim());
        contract.setMagsceneSignatory(assignedManagerField.getText().trim());
        
        // Si c'est une modification, conserver l'ID
        if (isEditing && originalContract != null) {
            contract.setId(originalContract.getId());
            contract.setCreatedAt(originalContract.getCreatedAt());
        }
        
        return contract;
    }
    
    /**
     * Charge la liste des clients depuis l'API
     */
    private void loadClients() {
        apiService.getAllClients().thenAccept(clientsObjects -> {
            try {
                // Conversion temporaire pour éviter les erreurs de compilation
                clientCombo.getItems().clear();
                
                // TODO: Traitement des clients - pour l'instant on laisse vide; // La logique métier sera implémentée plus tard avec les bons types
                
            } catch (Exception e) {
                // En cas d'erreur, laisser le ComboBox vide avec un message
                clientCombo.setPromptText("Erreur de chargement des clients");
                System.err.println("Erreur lors du chargement des clients: " + e.getMessage());
            }
        }).exceptionally(throwable -> {
            clientCombo.setPromptText("Erreur de chargement des clients");
            System.err.println("Erreur lors du chargement des clients: " + throwable.getMessage());
            return null;
        });
    }

    /**
     * Désactive tous les champs pour le mode lecture seule
     */
    private void setFieldsReadOnly() {
        if (titleField != null) titleField.setDisable(true);
        if (typeCombo != null) typeCombo.setDisable(true);
        if (statusCombo != null) statusCombo.setDisable(true);
        if (clientCombo != null) clientCombo.setDisable(true);
        if (contractNumberField != null) contractNumberField.setDisable(true);
        if (descriptionArea != null) descriptionArea.setDisable(true);
        if (startDatePicker != null) startDatePicker.setDisable(true);
        if (endDatePicker != null) endDatePicker.setDisable(true);
        if (signatureDatePicker != null) signatureDatePicker.setDisable(true);
        if (durationMonthsField != null) durationMonthsField.setDisable(true);
        if (autoRenewalCheckBox != null) autoRenewalCheckBox.setDisable(true);
        if (renewalNoticeDaysField != null) renewalNoticeDaysField.setDisable(true);
        if (totalAmountField != null) totalAmountField.setDisable(true);
        if (billingFrequencyCombo != null) billingFrequencyCombo.setDisable(true);
        if (paymentTermsCombo != null) paymentTermsCombo.setDisable(true);
        if (discountPercentageField != null) discountPercentageField.setDisable(true);
        if (penaltyClauseField != null) penaltyClauseField.setDisable(true);
        if (termsConditionsArea != null) termsConditionsArea.setDisable(true);
        if (notesArea != null) notesArea.setDisable(true);
        if (assignedManagerField != null) assignedManagerField.setDisable(true);
    }
}

