package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.model.Client;
import com.magscene.magsav.desktop.model.Contract;
import com.magscene.magsav.desktop.service.ApiService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

/**
 * Dialogue pour crÃƒÂ©er ou modifier un contrat
 * Interface complÃƒÂ¨te avec validation, async operations et gestion d'erreurs
 */
public class ContractDialog extends Dialog<Contract> {
    
    private final ApiService apiService;
    private final boolean isEditing;
    private final Contract originalContract;
    
    // Onglet 1: Informations gÃƒÂ©nÃƒÂ©rales
    private TextField titleField;
    private ComboBox<Contract.ContractType> typeCombo;
    private ComboBox<Contract.ContractStatus> statusCombo;
    private ComboBox<Client> clientCombo;
    private TextField contractNumberField;
    private TextArea descriptionArea;
    
    // Onglet 2: Dates et durÃƒÂ©e
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private DatePicker signatureDatePicker;
    private TextField durationMonthsField;
    private CheckBox autoRenewalCheckBox;
    private TextField renewalNoticeDaysField;
    
    // Onglet 3: Conditions financiÃƒÂ¨res
    private TextField totalAmountField;
    private ComboBox<Contract.BillingFrequency> billingFrequencyCombo;
    private ComboBox<Contract.PaymentTerms> paymentTermsCombo;
    private TextField discountPercentageField;
    private TextField penaltyClauseField;
    
    // Onglet 4: DÃƒÂ©tails et notes
    private TextArea termsConditionsArea;
    private TextArea notesArea;
    private TextField assignedManagerField;
    
    // ContrÃƒÂ´les
    private Button saveButton;
    private Label statusLabel;
    private ProgressIndicator progressIndicator;
    
    public ContractDialog(ApiService apiService) {
        this(apiService, null);
    }
    
    public ContractDialog(ApiService apiService, Contract contract) {
        this.apiService = apiService;
        this.originalContract = contract;
        this.isEditing = contract != null;
        
        setupDialog();
        createUI();
        setupValidation();
        loadClients();
        
        if (isEditing) {
            loadContractData();
        }
    }
    
    private void setupDialog() {
        setTitle(isEditing ? "Modifier le contrat" : "Nouveau contrat");
        setHeaderText(isEditing ? 
            "Modification du contrat : " + originalContract.getTitle() :
            "CrÃƒÂ©er un nouveau contrat");
        
        // IcÃƒÂ´ne
        setGraphic(new Label(isEditing ? "Ã°Å¸â€œÂ" : "Ã°Å¸â€œâ€¹"));
        
        // Taille de la fenÃƒÂªtre
        getDialogPane().setPrefSize(800, 700);
        
        // Boutons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        saveButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        saveButton.setText(isEditing ? "Mettre ÃƒÂ  jour" : "CrÃƒÂ©er");
        saveButton.setDisable(true);
        
        // Converter pour rÃƒÂ©cupÃƒÂ©rer les donnÃƒÂ©es
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createContractFromFields();
            }
            return null;
        });
    }
    
    private void createUI() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Onglet 1: Informations gÃƒÂ©nÃƒÂ©rales
        Tab generalTab = new Tab("Ã°Å¸â€œâ€¹ GÃƒÂ©nÃƒÂ©ral");
        generalTab.setContent(createGeneralPane());
        
        // Onglet 2: Dates et durÃƒÂ©e
        Tab datesTab = new Tab("Ã°Å¸â€œâ€¦ Dates");
        datesTab.setContent(createDatesPane());
        
        // Onglet 3: Financier
        Tab financialTab = new Tab("Ã°Å¸â€™Â° Financier");
        financialTab.setContent(createFinancialPane());
        
        // Onglet 4: DÃƒÂ©tails
        Tab detailsTab = new Tab("Ã°Å¸â€œâ€ž DÃƒÂ©tails");
        detailsTab.setContent(createDetailsPane());
        
        tabPane.getTabs().addAll(generalTab, datesTab, financialTab, detailsTab);
        
        // Barre de statut
        VBox mainContainer = new VBox(10);
        mainContainer.getChildren().addAll(tabPane, createStatusBar());
        
        getDialogPane().setContent(mainContainer);
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
        
        // NumÃƒÂ©ro de contrat
        grid.add(new Label("NumÃƒÂ©ro contrat"), 0, row);
        contractNumberField = new TextField();
        contractNumberField.setPromptText("Ex: CTR-2024-001");
        contractNumberField.setPrefWidth(300);
        grid.add(contractNumberField, 1, row++);
        
        // Nom du client *
        grid.add(new Label("Client *"), 0, row);
        clientCombo = new ComboBox<>();
        clientCombo.setPromptText("SÃƒÂ©lectionnez un client");
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
        
        // Date de dÃƒÂ©but *
        grid.add(new Label("Date de dÃƒÂ©but *"), 0, row);
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
        
        // DurÃƒÂ©e (mois)
        grid.add(new Label("DurÃƒÂ©e (mois)"), 0, row);
        durationMonthsField = new TextField();
        durationMonthsField.setPromptText("12");
        durationMonthsField.setPrefWidth(100);
        grid.add(durationMonthsField, 1, row++);
        
        // Renouvellement automatique
        grid.add(new Label("Renouvellement auto"), 0, row);
        autoRenewalCheckBox = new CheckBox("Activer le renouvellement automatique");
        grid.add(autoRenewalCheckBox, 1, row++);
        
        // PrÃƒÂ©avis (jours)
        grid.add(new Label("PrÃƒÂ©avis (jours)"), 0, row);
        renewalNoticeDaysField = new TextField();
        renewalNoticeDaysField.setPromptText("30");
        renewalNoticeDaysField.setPrefWidth(100);
        renewalNoticeDaysField.setDisable(true);
        grid.add(renewalNoticeDaysField, 1, row++);
        
        // Liaison checkbox -> champ prÃƒÂ©avis
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
        grid.add(new Label("Montant total (Ã¢â€šÂ¬) *"), 0, row);
        totalAmountField = new TextField();
        totalAmountField.setPromptText("Ex: 15000.00");
        totalAmountField.setPrefWidth(200);
        grid.add(totalAmountField, 1, row++);
        
        // FrÃƒÂ©quence de facturation
        grid.add(new Label("FrÃƒÂ©quence facturation"), 0, row);
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
        
        // Clause de pÃƒÂ©nalitÃƒÂ©
        grid.add(new Label("Clause pÃƒÂ©nalitÃƒÂ©"), 0, row);
        penaltyClauseField = new TextField();
        penaltyClauseField.setPromptText("PÃƒÂ©nalitÃƒÂ© en cas de non-respect...");
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
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #d0d0d0; -fx-border-width: 1px 0 0 0;");
        
        statusLabel = new Label("Remplissez les champs obligatoires (*)");
        statusLabel.setStyle("-fx-text-fill: #666666;");
        
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(16, 16);
        progressIndicator.setVisible(false);
        
        statusBar.getChildren().addAll(statusLabel, progressIndicator);
        
        return statusBar;
    }
    
    private void setupValidation() {
        // Validation en temps rÃƒÂ©el
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
        
        // Validation des champs numÃƒÂ©riques
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
                statusLabel.setText("Ã¢Å¡Â Ã¯Â¸Â La date de dÃƒÂ©but ne peut pas ÃƒÂªtre aprÃƒÂ¨s la date de fin");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });
        
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && startDatePicker.getValue() != null && newVal.isBefore(startDatePicker.getValue())) {
                statusLabel.setText("Ã¢Å¡Â Ã¯Â¸Â La date de fin ne peut pas ÃƒÂªtre avant la date de dÃƒÂ©but");
                statusLabel.setStyle("-fx-text-fill: red;");
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
            statusLabel.setText("Ã¢Å“â€¦ Formulaire valide");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else if (!datesValid) {
            statusLabel.setText("Ã¢Å¡Â Ã¯Â¸Â VÃƒÂ©rifiez les dates");
            statusLabel.setStyle("-fx-text-fill: red;");
        } else {
            statusLabel.setText("Remplissez les champs obligatoires (*)");
            statusLabel.setStyle("-fx-text-fill: #666666;");
        }
    }
    
    private void loadContractData() {
        if (originalContract == null) return;
        
        titleField.setText(originalContract.getTitle());
        typeCombo.setValue(originalContract.getType());
        statusCombo.setValue(originalContract.getStatus());
        contractNumberField.setText(originalContract.getContractNumber());
        // Le client sera sÃƒÂ©lectionnÃƒÂ© aprÃƒÂ¨s chargement de la liste
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
        
        // Pour les champs non disponibles dans le modÃƒÂ¨le actuel, nous les laissons vides
        // discountPercentageField.setText("");
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
        
        // DÃƒÂ©finir le client sÃƒÂ©lectionnÃƒÂ©
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
        
        // Les champs discount et penalty ne sont pas dans le modÃƒÂ¨le actuel
        // Nous ne les assignons pas pour l'instant
        
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
        try {
            List<Client> clients = apiService.getAllClients();
            clientCombo.getItems().setAll(clients);
            
            // Si on est en mode ÃƒÂ©dition, sÃƒÂ©lectionner le client correspondant
            if (isEditing && originalContract != null && originalContract.getClientName() != null) {
                clients.stream()
                    .filter(client -> client.getDisplayName().equals(originalContract.getClientName()))
                    .findFirst()
                    .ifPresent(clientCombo::setValue);
            }
        } catch (Exception e) {
            // En cas d'erreur, laisser le ComboBox vide avec un message
            clientCombo.setPromptText("Erreur de chargement des clients");
            System.err.println("Erreur lors du chargement des clients: " + e.getMessage());
        }
    }
}

