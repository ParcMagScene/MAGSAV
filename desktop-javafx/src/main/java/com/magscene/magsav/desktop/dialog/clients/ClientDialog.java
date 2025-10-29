package com.magscene.magsav.desktop.dialog.clients;

import com.magscene.magsav.desktop.model.Client;
import com.magscene.magsav.desktop.service.ApiService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClientDialog extends Stage {
    
    private final ApiService apiService;
    private final Client client;
    private final boolean isEdit;
    
    // Informations de base
    private TextField companyNameField;
    private ComboBox<Client.ClientType> typeComboBox;
    private ComboBox<Client.ClientStatus> statusComboBox;
    private ComboBox<Client.ClientCategory> categoryComboBox;
    
    // Contact
    private TextField emailField;
    private TextField phoneField;
    private TextField faxField;
    private TextField websiteField;
    private TextField addressField;
    private TextField cityField;
    private TextField postalCodeField;
    private TextField countryField;
    
    // Administratif
    private TextField siretField;
    private TextField vatField;
    private TextField sectorField;
    private TextField employeeCountField;
    private TextField revenueField;
    
    // Commercial
    private TextField salesRepField;
    private ComboBox<Client.PreferredPaymentMethod> paymentMethodComboBox;
    private TextField paymentTermsField;
    private TextField creditLimitField;
    private TextField outstandingAmountField;
    
    // Notes
    private TextArea notesArea;
    
    private boolean result = false;
    
    public ClientDialog(ApiService apiService, Client client) {
        this.apiService = apiService;
        this.client = client != null ? client : new Client();
        this.isEdit = client != null;
        
        initializeDialog();
        createUI();
        populateFields();
    }
    
    private void initializeDialog() {
        setTitle(isEdit ? "Modifier le client" : "Nouveau client");
        initModality(Modality.APPLICATION_MODAL);
        setWidth(800);
        setHeight(650);
        setResizable(true);
    }
    
    private void createUI() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        
        Label titleLabel = new Label(isEdit ? "Modifier le client" : "Nouveau client");
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");
        
        TabPane tabPane = createTabbedForm();
        HBox buttonBox = createButtons();
        
        root.getChildren().addAll(titleLabel, tabPane, buttonBox);
        
        Scene scene = new Scene(root);
        setScene(scene);
    }
    
    private TabPane createTabbedForm() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Onglet Informations générales
        Tab generalTab = new Tab("🏢 Général");
        generalTab.setContent(createGeneralForm());
        
        // Onglet Contact
        Tab contactTab = new Tab("📞 Contact");
        contactTab.setContent(createContactForm());
        
        // Onglet Administratif
        Tab adminTab = new Tab("📋 Administratif");
        adminTab.setContent(createAdminForm());
        
        // Onglet Commercial
        Tab commercialTab = new Tab("💼 Commercial");
        commercialTab.setContent(createCommercialForm());
        
        // Onglet Notes
        Tab notesTab = new Tab("📝 Notes");
        notesTab.setContent(createNotesForm());
        
        tabPane.getTabs().addAll(generalTab, contactTab, adminTab, commercialTab, notesTab);
        return tabPane;
    }
    
    private GridPane createGeneralForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(20));
        
        int row = 0;
        
        form.add(new Label("Nom entreprise:*"), 0, row);
        companyNameField = new TextField();
        companyNameField.setPrefWidth(250);
        form.add(companyNameField, 1, row++);
        
        form.add(new Label("Type:"), 0, row);
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(Client.ClientType.values());
        typeComboBox.setPrefWidth(250);
        form.add(typeComboBox, 1, row++);
        
        form.add(new Label("Statut:"), 0, row);
        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(Client.ClientStatus.values());
        statusComboBox.setPrefWidth(250);
        form.add(statusComboBox, 1, row++);
        
        form.add(new Label("Catégorie:"), 0, row);
        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(Client.ClientCategory.values());
        categoryComboBox.setPrefWidth(250);
        form.add(categoryComboBox, 1, row++);
        
        return form;
    }
    
    private GridPane createContactForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(20));
        
        int row = 0;
        
        form.add(new Label("Email:"), 0, row);
        emailField = new TextField();
        emailField.setPrefWidth(250);
        form.add(emailField, 1, row++);
        
        form.add(new Label("Téléphone:"), 0, row);
        phoneField = new TextField();
        phoneField.setPrefWidth(250);
        form.add(phoneField, 1, row++);
        
        form.add(new Label("Fax:"), 0, row);
        faxField = new TextField();
        faxField.setPrefWidth(250);
        form.add(faxField, 1, row++);
        
        form.add(new Label("Site web:"), 0, row);
        websiteField = new TextField();
        websiteField.setPrefWidth(250);
        form.add(websiteField, 1, row++);
        
        form.add(new Label("Adresse:"), 0, row);
        addressField = new TextField();
        addressField.setPrefWidth(250);
        form.add(addressField, 1, row++);
        
        form.add(new Label("Ville:"), 0, row);
        cityField = new TextField();
        cityField.setPrefWidth(250);
        form.add(cityField, 1, row++);
        
        form.add(new Label("Code postal:"), 0, row);
        postalCodeField = new TextField();
        postalCodeField.setPrefWidth(250);
        form.add(postalCodeField, 1, row++);
        
        form.add(new Label("Pays:"), 0, row);
        countryField = new TextField();
        countryField.setPrefWidth(250);
        countryField.setText("France"); // Valeur par défaut
        form.add(countryField, 1, row++);
        
        return form;
    }
    
    private GridPane createAdminForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(20));
        
        int row = 0;
        
        form.add(new Label("SIRET:"), 0, row);
        siretField = new TextField();
        siretField.setPrefWidth(250);
        form.add(siretField, 1, row++);
        
        form.add(new Label("TVA:"), 0, row);
        vatField = new TextField();
        vatField.setPrefWidth(250);
        form.add(vatField, 1, row++);
        
        form.add(new Label("Secteur d'activité:"), 0, row);
        sectorField = new TextField();
        sectorField.setPrefWidth(250);
        form.add(sectorField, 1, row++);
        
        form.add(new Label("Nombre d'employés:"), 0, row);
        employeeCountField = new TextField();
        employeeCountField.setPrefWidth(250);
        form.add(employeeCountField, 1, row++);
        
        form.add(new Label("Chiffre d'affaires:"), 0, row);
        revenueField = new TextField();
        revenueField.setPrefWidth(250);
        form.add(revenueField, 1, row++);
        
        return form;
    }
    
    private GridPane createCommercialForm() {
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);
        form.setPadding(new Insets(20));
        
        int row = 0;
        
        form.add(new Label("Commercial assigné:"), 0, row);
        salesRepField = new TextField();
        salesRepField.setPrefWidth(250);
        form.add(salesRepField, 1, row++);
        
        form.add(new Label("Mode de paiement:"), 0, row);
        paymentMethodComboBox = new ComboBox<>();
        paymentMethodComboBox.getItems().addAll(Client.PreferredPaymentMethod.values());
        paymentMethodComboBox.setPrefWidth(250);
        form.add(paymentMethodComboBox, 1, row++);
        
        form.add(new Label("Délais de paiement (jours):"), 0, row);
        paymentTermsField = new TextField();
        paymentTermsField.setPrefWidth(250);
        form.add(paymentTermsField, 1, row++);
        
        form.add(new Label("Limite de crédit (€):"), 0, row);
        creditLimitField = new TextField();
        creditLimitField.setPrefWidth(250);
        form.add(creditLimitField, 1, row++);
        
        form.add(new Label("Montant en attente (€):"), 0, row);
        outstandingAmountField = new TextField();
        outstandingAmountField.setPrefWidth(250);
        form.add(outstandingAmountField, 1, row++);
        
        return form;
    }
    
    private VBox createNotesForm() {
        VBox form = new VBox(10);
        form.setPadding(new Insets(20));
        
        Label notesLabel = new Label("Notes et commentaires:");
        notesLabel.setStyle("-fx-font-weight: bold;");
        
        notesArea = new TextArea();
        notesArea.setPrefRowCount(10);
        notesArea.setWrapText(true);
        
        form.getChildren().addAll(notesLabel, notesArea);
        return form;
    }
    
    private HBox createButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button saveButton = new Button(isEdit ? "Modifier" : "Creer");
        saveButton.setOnAction(e -> handleSave());
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setOnAction(e -> close());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        return buttonBox;
    }
    
    private void populateFields() {
        if (isEdit) {
            // Général
            companyNameField.setText(client.getCompanyName());
            typeComboBox.setValue(client.getType());
            statusComboBox.setValue(client.getStatus());
            categoryComboBox.setValue(client.getCategory());
            
            // Contact
            emailField.setText(client.getEmail());
            phoneField.setText(client.getPhone());
            faxField.setText(client.getFax());
            websiteField.setText(client.getWebsite());
            addressField.setText(client.getAddress());
            cityField.setText(client.getCity());
            postalCodeField.setText(client.getPostalCode());
            countryField.setText(client.getCountry());
            
            // Administratif
            siretField.setText(client.getSiretNumber());
            vatField.setText(client.getVatNumber());
            sectorField.setText(client.getBusinessSector());
            if (client.getEmployeeCount() != null) {
                employeeCountField.setText(client.getEmployeeCount().toString());
            }
            if (client.getAnnualRevenue() != null) {
                revenueField.setText(client.getAnnualRevenue().toString());
            }
            
            // Commercial
            salesRepField.setText(client.getAssignedSalesRep());
            paymentMethodComboBox.setValue(client.getPreferredPaymentMethod());
            if (client.getPaymentTermsDays() != null) {
                paymentTermsField.setText(client.getPaymentTermsDays().toString());
            }
            if (client.getCreditLimit() != null) {
                creditLimitField.setText(client.getCreditLimit().toString());
            }
            if (client.getOutstandingAmount() != null) {
                outstandingAmountField.setText(client.getOutstandingAmount().toString());
            }
            
            // Notes
            notesArea.setText(client.getNotes());
        } else {
            // Valeurs par défaut pour nouveau client
            statusComboBox.setValue(Client.ClientStatus.ACTIVE);
            categoryComboBox.setValue(Client.ClientCategory.STANDARD);
            typeComboBox.setValue(Client.ClientType.CORPORATE);
            countryField.setText("France");
        }
    }
    
    private void handleSave() {
        if (validateForm()) {
            try {
                // Général
                client.setCompanyName(companyNameField.getText().trim());
                client.setType(typeComboBox.getValue());
                client.setStatus(statusComboBox.getValue());
                client.setCategory(categoryComboBox.getValue());
                
                // Contact
                client.setEmail(emailField.getText().trim());
                client.setPhone(phoneField.getText().trim());
                client.setFax(faxField.getText().trim());
                client.setWebsite(websiteField.getText().trim());
                client.setAddress(addressField.getText().trim());
                client.setCity(cityField.getText().trim());
                client.setPostalCode(postalCodeField.getText().trim());
                client.setCountry(countryField.getText().trim());
                
                // Administratif
                client.setSiretNumber(siretField.getText().trim());
                client.setVatNumber(vatField.getText().trim());
                client.setBusinessSector(sectorField.getText().trim());
                
                String employeeCountText = employeeCountField.getText().trim();
                if (!employeeCountText.isEmpty()) {
                    client.setEmployeeCount(Integer.valueOf(employeeCountText));
                }
                
                String revenueText = revenueField.getText().trim();
                if (!revenueText.isEmpty()) {
                    client.setAnnualRevenue(new java.math.BigDecimal(revenueText));
                }
                
                // Commercial
                client.setAssignedSalesRep(salesRepField.getText().trim());
                client.setPreferredPaymentMethod(paymentMethodComboBox.getValue());
                
                String paymentTermsText = paymentTermsField.getText().trim();
                if (!paymentTermsText.isEmpty()) {
                    client.setPaymentTermsDays(Integer.valueOf(paymentTermsText));
                }
                
                String creditLimitText = creditLimitField.getText().trim();
                if (!creditLimitText.isEmpty()) {
                    client.setCreditLimit(new java.math.BigDecimal(creditLimitText));
                }
                
                String outstandingText = outstandingAmountField.getText().trim();
                if (!outstandingText.isEmpty()) {
                    client.setOutstandingAmount(new java.math.BigDecimal(outstandingText));
                }
                
                // Notes
                client.setNotes(notesArea.getText().trim());
                
                result = true;
                close();
            } catch (NumberFormatException e) {
                showAlert("Erreur de format", "Veuillez vérifier que les champs numériques contiennent des valeurs valides.");
            }
        }
    }
    
    private boolean validateForm() {
        if (companyNameField.getText().trim().isEmpty()) {
            showAlert("Le nom de l'entreprise est obligatoire");
            return false;
        }
        return true;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showAlert(String message) {
        showAlert("Attention", message);
    }
    
    public boolean getResult() {
        return result;
    }
    
    public Client getClient() {
        return client;
    }
}
