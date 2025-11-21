package com.magscene.magsav.desktop.view.supplier.dialog;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dialogue pour créer/éditer un fournisseur
 * Interface complète avec gestion des services et catalogue
 */
public class SupplierDialog extends Dialog<SupplierDialog.SupplierResult> {
    
    // Contrôles principaux
    private TextField nameField;
    private TextField contactPersonField;
    private TextField emailField;
    private TextField phoneField;
    private TextArea addressArea;
    private TextField websiteField;
    private ComboBox<String> statusCombo;
    private TextField siretField;
    
    // Services et spécialités
    private ListView<String> servicesListView;
    private ObservableList<String> selectedServices;
    private TextField customServiceField;
    
    // Conditions commerciales
    private TextField paymentTermsField;
    private TextField deliveryTermsField;
    private TextField minimumOrderField;
    private TextArea notesArea;
    
    // Catalogue
    private CheckBox hasCatalogCheckBox;
    private Label catalogFileLabel;
    private File catalogFile;
    
    // Mode d'édition
    private final boolean editMode;
    private final SupplierResult originalSupplier;
    
    /**
     * Constructeur pour nouveau fournisseur
     */
    public SupplierDialog(Stage owner) {
        this(owner, null);
    }
    
    /**
     * Constructeur pour édition
     */
    public SupplierDialog(Stage owner, SupplierResult existingSupplier) {
        this.editMode = existingSupplier != null;
        this.originalSupplier = existingSupplier;
        
        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);
        setTitle(editMode ? "Modifier le fournisseur" : "Nouveau fournisseur");
        setResizable(true);
        
        // Configuration du dialogue
        setupUI();
        setupResultConverter();
        
        // Chargement des données existantes si mode édition
        if (editMode && originalSupplier != null) {
            loadExistingData();
        }
        
        // Validation
        setupValidation();
    }
    
    private void setupUI() {
        TabPane tabPane = new TabPane();
        tabPane.setPrefWidth(700);
        tabPane.setPrefHeight(500);
        
        // Onglets
        tabPane.getTabs().addAll(
            createGeneralTab(),
            createServicesTab(),
            createCommercialTab(),
            createCatalogTab()
        );
        
        getDialogPane().setContent(tabPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Style
        getDialogPane().getStylesheets().add(
            getClass().getResource("/styles/supplier-system.css").toExternalForm()
        );
        getDialogPane().getStyleClass().add("supplier-dialog");
    }
    
    private Tab createGeneralTab() {
        Tab tab = new Tab("Informations générales");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Nom du fournisseur
        grid.add(new Label("Nom *:"), 0, row);
        nameField = new TextField();
        nameField.setPromptText("Ex: Éclairage Professionnel SARL");
        nameField.setPrefWidth(300);
        grid.add(nameField, 1, row++);
        
        // Statut
        grid.add(new Label("Statut *:"), 0, row);
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("ACTIF", "INACTIF", "EN_ATTENTE", "SUSPENDU");
        statusCombo.setValue("ACTIF");
        grid.add(statusCombo, 1, row++);
        
        // Personne de contact
        grid.add(new Label("Contact principal:"), 0, row);
        contactPersonField = new TextField();
        contactPersonField.setPromptText("Nom et prénom");
        grid.add(contactPersonField, 1, row++);
        
        // Email
        grid.add(new Label("E-mail *:"), 0, row);
        emailField = new TextField();
        emailField.setPromptText("contact@fournisseur.fr");
        grid.add(emailField, 1, row++);
        
        // Téléphone
        grid.add(new Label("Téléphone:"), 0, row);
        phoneField = new TextField();
        phoneField.setPromptText("01 23 45 67 89");
        grid.add(phoneField, 1, row++);
        
        // Site web
        grid.add(new Label("Site web:"), 0, row);
        websiteField = new TextField();
        websiteField.setPromptText("https://www.fournisseur.fr");
        grid.add(websiteField, 1, row++);
        
        // SIRET
        grid.add(new Label("SIRET:"), 0, row);
        siretField = new TextField();
        siretField.setPromptText("12345678901234");
        grid.add(siretField, 1, row++);
        
        // Adresse
        grid.add(new Label("Adresse:"), 0, row);
        addressArea = new TextArea();
        addressArea.setPromptText("Adresse complète...");
        addressArea.setPrefRowCount(3);
        addressArea.setPrefWidth(300);
        grid.add(addressArea, 1, row);
        
        content.getChildren().add(grid);
        tab.setContent(new ScrollPane(content));
        return tab;
    }
    
    private Tab createServicesTab() {
        Tab tab = new Tab("Services & Spécialités");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Services proposés par ce fournisseur");
        titleLabel.getStyleClass().add("section-title");
        
        // Liste des services disponibles
        selectedServices = FXCollections.observableArrayList();
        servicesListView = new ListView<>(selectedServices);
        servicesListView.setPrefHeight(200);
        servicesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // Services prédéfinis
        VBox servicesBox = new VBox(10);
        servicesBox.getStyleClass().add("services-selector");
        
        List<String> availableServices = Arrays.asList(
            "Éclairage de spectacle",
            "Sonorisation",
            "Structures et podiums",
            "Équipements électriques",
            "Sécurité et protection",
            "Transport et manutention",
            "Installation technique",
            "Maintenance et réparation",
            "Formation technique",
            "Conseil et expertise"
        );
        
        FlowPane servicesFlow = new FlowPane(10, 10);
        for (String service : availableServices) {
            CheckBox checkBox = new CheckBox(service);
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    if (!selectedServices.contains(service)) {
                        selectedServices.add(service);
                    }
                } else {
                    selectedServices.remove(service);
                }
            });
            servicesFlow.getChildren().add(checkBox);
        }
        
        // Service personnalisé
        HBox customServiceBox = new HBox(10);
        customServiceBox.setAlignment(Pos.CENTER_LEFT);
        
        Label customLabel = new Label("Service personnalisé:");
        customServiceField = new TextField();
        customServiceField.setPromptText("Tapez un service spécifique...");
        customServiceField.setPrefWidth(200);
        
        Button addCustomBtn = new Button("Ajouter");
        addCustomBtn.setOnAction(e -> {
            String customService = customServiceField.getText().trim();
            if (!customService.isEmpty() && !selectedServices.contains(customService)) {
                selectedServices.add(customService);
                customServiceField.clear();
            }
        });
        
        Button removeBtn = new Button("Supprimer sélection");
        removeBtn.setOnAction(e -> {
            String selected = servicesListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedServices.remove(selected);
            }
        });
        
        customServiceBox.getChildren().addAll(customLabel, customServiceField, addCustomBtn);
        
        content.getChildren().addAll(
            titleLabel,
            new Label("Cochez les services proposés:"),
            servicesFlow,
            new Separator(),
            customServiceBox,
            new Label("Services sélectionnés:"),
            servicesListView,
            removeBtn
        );
        
        tab.setContent(new ScrollPane(content));
        return tab;
    }
    
    private Tab createCommercialTab() {
        Tab tab = new Tab("Conditions commerciales");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Conditions de paiement
        grid.add(new Label("Conditions de paiement:"), 0, row);
        paymentTermsField = new TextField();
        paymentTermsField.setPromptText("Ex: 30 jours net, 2% escompte à 10 jours");
        paymentTermsField.setPrefWidth(300);
        grid.add(paymentTermsField, 1, row++);
        
        // Conditions de livraison
        grid.add(new Label("Conditions de livraison:"), 0, row);
        deliveryTermsField = new TextField();
        deliveryTermsField.setPromptText("Ex: Franco domicile, délai 48h");
        grid.add(deliveryTermsField, 1, row++);
        
        // Commande minimum
        grid.add(new Label("Commande minimum (€):"), 0, row);
        minimumOrderField = new TextField();
        minimumOrderField.setPromptText("Ex: 500.00");
        grid.add(minimumOrderField, 1, row++);
        
        // Notes commerciales
        grid.add(new Label("Notes commerciales:"), 0, row);
        notesArea = new TextArea();
        notesArea.setPromptText("Informations complémentaires, tarifs préférentiels, etc.");
        notesArea.setPrefRowCount(4);
        notesArea.setPrefWidth(300);
        grid.add(notesArea, 1, row);
        
        content.getChildren().add(grid);
        tab.setContent(new ScrollPane(content));
        return tab;
    }
    
    private Tab createCatalogTab() {
        Tab tab = new Tab("Catalogue");
        tab.setClosable(false);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Gestion du catalogue fournisseur");
        titleLabel.getStyleClass().add("section-title");
        
        // Case à cocher pour activer le catalogue
        hasCatalogCheckBox = new CheckBox("Ce fournisseur dispose d'un catalogue électronique");
        hasCatalogCheckBox.getStyleClass().add("large-checkbox");
        
        // Section import fichier
        VBox catalogSection = new VBox(10);
        catalogSection.setPadding(new Insets(10));
        catalogSection.getStyleClass().add("bordered-section");
        
        Label importLabel = new Label("Import de catalogue");
        importLabel.getStyleClass().add("subsection-title");
        
        HBox fileBox = new HBox(10);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        
        catalogFileLabel = new Label("Aucun fichier sélectionné");
        catalogFileLabel.getStyleClass().add("file-label");
        
        Button browseBtn = new Button("Parcourir...");
        browseBtn.setOnAction(e -> selectCatalogFile());
        
        Button importBtn = new Button("Importer catalogue");
        importBtn.getStyleClass().add("btn-primary");
        importBtn.setOnAction(e -> importCatalog());
        
        fileBox.getChildren().addAll(new Label("Fichier:"), catalogFileLabel, browseBtn, importBtn);
        
        // Informations sur les formats supportés
        Label formatLabel = new Label("Formats supportés: Excel (.xlsx), CSV (.csv), XML");
        formatLabel.getStyleClass().add("info-text");
        
        // Zone d'information
        TextArea catalogInfoArea = new TextArea();
        catalogInfoArea.setPromptText("Informations sur le catalogue...\n\nExemple de contenu:\n- Référence produit\n- Désignation\n- Prix unitaire\n- Disponibilité\n- Délai de livraison");
        catalogInfoArea.setPrefRowCount(6);
        catalogInfoArea.setEditable(false);
        catalogInfoArea.getStyleClass().add("info-area");
        
        catalogSection.getChildren().addAll(
            importLabel,
            fileBox,
            formatLabel,
            new Separator(),
            new Label("Aperçu du catalogue:"),
            catalogInfoArea
        );
        
        // Liaison de la section avec la checkbox
        catalogSection.disableProperty().bind(hasCatalogCheckBox.selectedProperty().not());
        
        content.getChildren().addAll(
            titleLabel,
            hasCatalogCheckBox,
            catalogSection
        );
        
        tab.setContent(new ScrollPane(content));
        return tab;
    }
    
    private void setupResultConverter() {
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return createSupplierResult();
            }
            return null;
        });
    }
    
    private void setupValidation() {
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        
        BooleanProperty validProperty = new SimpleBooleanProperty();
        validProperty.bind(
            nameField.textProperty().isEmpty().not()
            .and(emailField.textProperty().isEmpty().not())
            .and(statusCombo.valueProperty().isNotNull())
        );
        
        okButton.disableProperty().bind(validProperty.not());
    }
    
    private void loadExistingData() {
        if (originalSupplier == null) return;
        
        nameField.setText(originalSupplier.getName());
        contactPersonField.setText(originalSupplier.getContactPerson());
        emailField.setText(originalSupplier.getEmail());
        phoneField.setText(originalSupplier.getPhone());
        websiteField.setText(originalSupplier.getWebsite());
        siretField.setText(originalSupplier.getSiret());
        statusCombo.setValue(originalSupplier.getStatus());
        
        if (originalSupplier.getAddress() != null) {
            addressArea.setText(originalSupplier.getAddress());
        }
        
        // Services
        if (originalSupplier.getServices() != null) {
            selectedServices.addAll(originalSupplier.getServices());
            // Cocher les checkboxes correspondantes
            // (Implementation dépendante de la structure UI finale)
        }
        
        // Conditions commerciales
        paymentTermsField.setText(originalSupplier.getPaymentTerms());
        deliveryTermsField.setText(originalSupplier.getDeliveryTerms());
        if (originalSupplier.getMinimumOrder() != null) {
            minimumOrderField.setText(originalSupplier.getMinimumOrder().toString());
        }
        notesArea.setText(originalSupplier.getNotes());
        
        // Catalogue
        hasCatalogCheckBox.setSelected(originalSupplier.isHasCatalog());
    }
    
    private SupplierResult createSupplierResult() {
        SupplierResult result = new SupplierResult();
        
        result.setName(nameField.getText().trim());
        result.setContactPerson(contactPersonField.getText().trim());
        result.setEmail(emailField.getText().trim());
        result.setPhone(phoneField.getText().trim());
        result.setWebsite(websiteField.getText().trim());
        result.setSiret(siretField.getText().trim());
        result.setStatus(statusCombo.getValue());
        result.setAddress(addressArea.getText().trim());
        
        // Services
        result.setServices(new ArrayList<>(selectedServices));
        
        // Conditions commerciales
        result.setPaymentTerms(paymentTermsField.getText().trim());
        result.setDeliveryTerms(deliveryTermsField.getText().trim());
        result.setNotes(notesArea.getText().trim());
        
        // Commande minimum
        try {
            if (!minimumOrderField.getText().trim().isEmpty()) {
                result.setMinimumOrder(Double.parseDouble(minimumOrderField.getText().trim()));
            }
        } catch (NumberFormatException e) {
            // Ignore, restera null
        }
        
        // Catalogue
        result.setHasCatalog(hasCatalogCheckBox.isSelected());
        result.setCatalogFile(catalogFile);
        
        return result;
    }
    
    private void selectCatalogFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner le fichier catalogue");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx", "*.xls"),
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
            new FileChooser.ExtensionFilter("Fichiers XML", "*.xml"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        catalogFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (catalogFile != null) {
            catalogFileLabel.setText(catalogFile.getName());
        }
    }
    
    private void importCatalog() {
        if (catalogFile == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun fichier");
            alert.setHeaderText("Aucun fichier sélectionné");
            alert.setContentText("Veuillez d'abord sélectionner un fichier catalogue.");
            alert.showAndWait();
            return;
        }
        
        // TODO: Implémenter l'import réel du catalogue
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Import catalogue");
        alert.setHeaderText("Import simulé");
        alert.setContentText("L'import du catalogue " + catalogFile.getName() + " sera implémenté dans une version future.");
        alert.showAndWait();
    }
    
    // Classe de résultat
    public static class SupplierResult {
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String website;
        private String siret;
        private String status;
        private String address;
        private List<String> services;
        private String paymentTerms;
        private String deliveryTerms;
        private Double minimumOrder;
        private String notes;
        private boolean hasCatalog;
        private File catalogFile;
        
        public SupplierResult() {
            this.services = new ArrayList<>();
        }
        
        // Getters et setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getContactPerson() { return contactPerson; }
        public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getWebsite() { return website; }
        public void setWebsite(String website) { this.website = website; }
        
        public String getSiret() { return siret; }
        public void setSiret(String siret) { this.siret = siret; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        
        public List<String> getServices() { return services; }
        public void setServices(List<String> services) { this.services = services; }
        
        public String getPaymentTerms() { return paymentTerms; }
        public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
        
        public String getDeliveryTerms() { return deliveryTerms; }
        public void setDeliveryTerms(String deliveryTerms) { this.deliveryTerms = deliveryTerms; }
        
        public Double getMinimumOrder() { return minimumOrder; }
        public void setMinimumOrder(Double minimumOrder) { this.minimumOrder = minimumOrder; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        public boolean isHasCatalog() { return hasCatalog; }
        public void setHasCatalog(boolean hasCatalog) { this.hasCatalog = hasCatalog; }
        
        public File getCatalogFile() { return catalogFile; }
        public void setCatalogFile(File catalogFile) { this.catalogFile = catalogFile; }
    }
}