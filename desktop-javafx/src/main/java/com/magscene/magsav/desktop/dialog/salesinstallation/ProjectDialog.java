package com.magscene.magsav.desktop.dialog.salesinstallation;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dialogue pour créer/modifier un projet
 */
public class ProjectDialog extends Dialog<Map<String, Object>> {

    // Onglet Général
    private TextField projectNumberField;
    private TextField nameField;
    private ComboBox<String> typeCombo;
    private ComboBox<String> statusCombo;
    private ComboBox<String> priorityCombo;
    private TextArea descriptionArea;

    // Onglet Client
    private TextField clientNameField;
    private TextField clientContactField;
    private TextField clientEmailField;
    private TextField clientPhoneField;
    private TextArea clientAddressArea;

    // Onglet Dates et Lieu
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private DatePicker installationDatePicker;
    private DatePicker deliveryDatePicker;
    private TextField venueField;
    private TextArea venueAddressArea;
    private TextField venueContactField;

    // Onglet Financier
    private TextField estimatedAmountField;
    private TextField finalAmountField;
    private TextField depositAmountField;
    private TextField remainingAmountField;

    // Onglet Équipe
    private TextField projectManagerField;
    private TextField technicalManagerField;
    private TextField salesRepresentativeField;

    // Onglet Notes
    private TextArea notesArea;
    private TextArea technicalNotesArea;
    private TextArea clientRequirementsArea;

    private final Map<String, Object> projectData;

    public ProjectDialog(String title, Map<String, Object> existingProject) {
        setTitle(title);
        setHeaderText("Saisir les informations du projet");
        
        this.projectData = existingProject != null ? new HashMap<>(existingProject) : new HashMap<>();
        
        // Configuration des boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Création de l'interface à onglets
        TabPane tabPane = createTabbedInterface();
        getDialogPane().setContent(tabPane);
        
        // Remplir les champs si modification
        if (existingProject != null) {
            populateFields();
        }
        
        // Configuration du résultat
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return collectData();
            }
            return null;
        });
        
        // Validation en temps réel
        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        // Initialement on laisse désactivé, puis on va valider l'état courant
        saveButton.setDisable(true);

        // Écouter les changements pour activer/désactiver le bouton Enregistrer
        nameField.textProperty().addListener((obs, old, text) -> validateForm(saveButton));
        clientNameField.textProperty().addListener((obs, old, text) -> validateForm(saveButton));

        // Valider immédiatement l'état du formulaire (utile lors de la modification existante)
        validateForm(saveButton);
    }

    private TabPane createTabbedInterface() {
        TabPane tabPane = new TabPane();
        
        // Onglet Général
        Tab generalTab = new Tab("Général", createGeneralPane());
        generalTab.setClosable(false);
        
        // Onglet Client
        Tab clientTab = new Tab("Client", createClientPane());
        clientTab.setClosable(false);
        
        // Onglet Dates et Lieu
        Tab datesTab = new Tab("Dates & Lieu", createDatesPane());
        datesTab.setClosable(false);
        
        // Onglet Financier
        Tab financialTab = new Tab("Financier", createFinancialPane());
        financialTab.setClosable(false);
        
        // Onglet Équipe
        Tab teamTab = new Tab("Équipe", createTeamPane());
        teamTab.setClosable(false);
        
        // Onglet Notes
        Tab notesTab = new Tab("Notes", createNotesPane());
        notesTab.setClosable(false);
        
        tabPane.getTabs().addAll(generalTab, clientTab, datesTab, financialTab, teamTab, notesTab);
        
        return tabPane;
    }

    private Node createGeneralPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Numéro de projet
        grid.add(new Label("Numéro de projet:"), 0, row);
        projectNumberField = new TextField();
        projectNumberField.setPromptText("Généré automatiquement si vide");
        grid.add(projectNumberField, 1, row++);
        
        // Nom du projet
        grid.add(new Label("Nom du projet *:"), 0, row);
        nameField = new TextField();
        nameField.setPromptText("Nom du projet (obligatoire)");
        grid.add(nameField, 1, row++);
        
        // Type de projet
        grid.add(new Label("Type:"), 0, row);
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "SALE", "INSTALLATION", "RENTAL", "MAINTENANCE", "EVENT", "PROJECT"));
        typeCombo.setValue("SALE");
        grid.add(typeCombo, 1, row++);
        
        // Statut
        grid.add(new Label("Statut:"), 0, row);
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(
            "DRAFT", "QUOTED", "CONFIRMED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "ON_HOLD"));
        statusCombo.setValue("DRAFT");
        grid.add(statusCombo, 1, row++);
        
        // Priorité
        grid.add(new Label("Priorité:"), 0, row);
        priorityCombo = new ComboBox<>(FXCollections.observableArrayList(
            "LOW", "MEDIUM", "HIGH", "URGENT"));
        priorityCombo.setValue("MEDIUM");
        grid.add(priorityCombo, 1, row++);
        
        // Description
        grid.add(new Label("Description:"), 0, row);
        descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        grid.add(descriptionArea, 1, row);
        
        return new ScrollPane(grid);
    }

    private Node createClientPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        grid.add(new Label("Nom du client *:"), 0, row);
        clientNameField = new TextField();
        clientNameField.setPromptText("Nom du client (obligatoire)");
        grid.add(clientNameField, 1, row++);
        
        grid.add(new Label("Contact:"), 0, row);
        clientContactField = new TextField();
        grid.add(clientContactField, 1, row++);
        
        grid.add(new Label("Email:"), 0, row);
        clientEmailField = new TextField();
        grid.add(clientEmailField, 1, row++);
        
        grid.add(new Label("Téléphone:"), 0, row);
        clientPhoneField = new TextField();
        grid.add(clientPhoneField, 1, row++);
        
        grid.add(new Label("Adresse:"), 0, row);
        clientAddressArea = new TextArea();
        clientAddressArea.setPrefRowCount(3);
        clientAddressArea.setWrapText(true);
        grid.add(clientAddressArea, 1, row);
        
        return new ScrollPane(grid);
    }

    private Node createDatesPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        grid.add(new Label("Date de début:"), 0, row);
        startDatePicker = new DatePicker();
        grid.add(startDatePicker, 1, row++);
        
        grid.add(new Label("Date de fin:"), 0, row);
        endDatePicker = new DatePicker();
        grid.add(endDatePicker, 1, row++);
        
        grid.add(new Label("Date d'installation:"), 0, row);
        installationDatePicker = new DatePicker();
        grid.add(installationDatePicker, 1, row++);
        
        grid.add(new Label("Date de livraison:"), 0, row);
        deliveryDatePicker = new DatePicker();
        grid.add(deliveryDatePicker, 1, row++);
        
        grid.add(new Label("Lieu de l'événement:"), 0, row);
        venueField = new TextField();
        grid.add(venueField, 1, row++);
        
        grid.add(new Label("Adresse du lieu:"), 0, row);
        venueAddressArea = new TextArea();
        venueAddressArea.setPrefRowCount(3);
        venueAddressArea.setWrapText(true);
        grid.add(venueAddressArea, 1, row++);
        
        grid.add(new Label("Contact sur site:"), 0, row);
        venueContactField = new TextField();
        grid.add(venueContactField, 1, row);
        
        return new ScrollPane(grid);
    }

    private Node createFinancialPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        grid.add(new Label("Montant estimé (€):"), 0, row);
        estimatedAmountField = new TextField();
        estimatedAmountField.setPromptText("0.00");
        grid.add(estimatedAmountField, 1, row++);
        
        grid.add(new Label("Montant final (€):"), 0, row);
        finalAmountField = new TextField();
        finalAmountField.setPromptText("0.00");
        grid.add(finalAmountField, 1, row++);
        
        grid.add(new Label("Acompte (€):"), 0, row);
        depositAmountField = new TextField();
        depositAmountField.setPromptText("0.00");
        grid.add(depositAmountField, 1, row++);
        
        grid.add(new Label("Solde restant (€):"), 0, row);
        remainingAmountField = new TextField();
        remainingAmountField.setPromptText("0.00");
        remainingAmountField.setEditable(false); // Calculé automatiquement
        grid.add(remainingAmountField, 1, row);
        
        // Calcul automatique du solde
        finalAmountField.textProperty().addListener((obs, old, text) -> updateRemainingAmount());
        depositAmountField.textProperty().addListener((obs, old, text) -> updateRemainingAmount());
        
        return new ScrollPane(grid);
    }

    private Node createTeamPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        grid.add(new Label("Chef de projet:"), 0, row);
        projectManagerField = new TextField();
        grid.add(projectManagerField, 1, row++);
        
        grid.add(new Label("Responsable technique:"), 0, row);
        technicalManagerField = new TextField();
        grid.add(technicalManagerField, 1, row++);
        
        grid.add(new Label("Commercial:"), 0, row);
        salesRepresentativeField = new TextField();
        grid.add(salesRepresentativeField, 1, row);
        
        return new ScrollPane(grid);
    }

    private Node createNotesPane() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        
        vbox.getChildren().add(new Label("Notes générales:"));
        notesArea = new TextArea();
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);
        vbox.getChildren().add(notesArea);
        
        vbox.getChildren().add(new Label("Notes techniques:"));
        technicalNotesArea = new TextArea();
        technicalNotesArea.setPrefRowCount(4);
        technicalNotesArea.setWrapText(true);
        vbox.getChildren().add(technicalNotesArea);
        
        vbox.getChildren().add(new Label("Exigences client:"));
        clientRequirementsArea = new TextArea();
        clientRequirementsArea.setPrefRowCount(4);
        clientRequirementsArea.setWrapText(true);
        vbox.getChildren().add(clientRequirementsArea);
        
        return new ScrollPane(vbox);
    }

    private void populateFields() {
        // Onglet Général
        projectNumberField.setText(getString("projectNumber"));
        nameField.setText(getString("name"));
        typeCombo.setValue(getString("type"));
        statusCombo.setValue(getString("status"));
        priorityCombo.setValue(getString("priority"));
        descriptionArea.setText(getString("description"));
        
        // Onglet Client
        clientNameField.setText(getString("clientName"));
        clientContactField.setText(getString("clientContact"));
        clientEmailField.setText(getString("clientEmail"));
        clientPhoneField.setText(getString("clientPhone"));
        clientAddressArea.setText(getString("clientAddress"));
        
        // Onglet Dates
        if (projectData.get("startDate") != null) {
            startDatePicker.setValue(LocalDate.parse(projectData.get("startDate").toString()));
        }
        if (projectData.get("endDate") != null) {
            endDatePicker.setValue(LocalDate.parse(projectData.get("endDate").toString()));
        }
        if (projectData.get("installationDate") != null) {
            installationDatePicker.setValue(LocalDate.parse(projectData.get("installationDate").toString()));
        }
        if (projectData.get("deliveryDate") != null) {
            deliveryDatePicker.setValue(LocalDate.parse(projectData.get("deliveryDate").toString()));
        }
        
        venueField.setText(getString("venue"));
        venueAddressArea.setText(getString("venueAddress"));
        venueContactField.setText(getString("venueContact"));
        
        // Onglet Financier
        estimatedAmountField.setText(getBigDecimalAsString("estimatedAmount"));
        finalAmountField.setText(getBigDecimalAsString("finalAmount"));
        depositAmountField.setText(getBigDecimalAsString("depositAmount"));
        remainingAmountField.setText(getBigDecimalAsString("remainingAmount"));
        
        // Onglet Équipe
        projectManagerField.setText(getString("projectManager"));
        technicalManagerField.setText(getString("technicalManager"));
        salesRepresentativeField.setText(getString("salesRepresentative"));
        
        // Onglet Notes
        notesArea.setText(getString("notes"));
        technicalNotesArea.setText(getString("technicalNotes"));
        clientRequirementsArea.setText(getString("clientRequirements"));
    }

    private Map<String, Object> collectData() {
        Map<String, Object> data = new HashMap<>();
        
        // Onglet Général
        data.put("projectNumber", projectNumberField.getText().trim());
        data.put("name", nameField.getText().trim());
        data.put("type", typeCombo.getValue());
        data.put("status", statusCombo.getValue());
        data.put("priority", priorityCombo.getValue());
        data.put("description", descriptionArea.getText().trim());
        
        // Onglet Client
        data.put("clientName", clientNameField.getText().trim());
        data.put("clientContact", clientContactField.getText().trim());
        data.put("clientEmail", clientEmailField.getText().trim());
        data.put("clientPhone", clientPhoneField.getText().trim());
        data.put("clientAddress", clientAddressArea.getText().trim());
        
        // Onglet Dates (conversion en format ISO ou null)
        data.put("startDate", startDatePicker.getValue() != null ? startDatePicker.getValue().toString() : null);
        data.put("endDate", endDatePicker.getValue() != null ? endDatePicker.getValue().toString() : null);
        data.put("installationDate", installationDatePicker.getValue() != null ? installationDatePicker.getValue().toString() : null);
        data.put("deliveryDate", deliveryDatePicker.getValue() != null ? deliveryDatePicker.getValue().toString() : null);
        data.put("venue", venueField.getText().trim());
        data.put("venueAddress", venueAddressArea.getText().trim());
        data.put("venueContact", venueContactField.getText().trim());
        
        // Onglet Financier (conversion en Double ou null pour JSON)
        data.put("estimatedAmount", parseAmountAsDouble(estimatedAmountField.getText()));
        data.put("finalAmount", parseAmountAsDouble(finalAmountField.getText()));
        data.put("depositAmount", parseAmountAsDouble(depositAmountField.getText()));
        data.put("remainingAmount", parseAmountAsDouble(remainingAmountField.getText()));
        
        // Onglet Équipe
        data.put("projectManager", projectManagerField.getText().trim());
        data.put("technicalManager", technicalManagerField.getText().trim());
        data.put("salesRepresentative", salesRepresentativeField.getText().trim());
        
        // Onglet Notes
        data.put("notes", notesArea.getText().trim());
        data.put("technicalNotes", technicalNotesArea.getText().trim());
        data.put("clientRequirements", clientRequirementsArea.getText().trim());
        
        return data;
    }

    private void updateRemainingAmount() {
        try {
            BigDecimal finalAmount = parseAmount(finalAmountField.getText());
            BigDecimal depositAmount = parseAmount(depositAmountField.getText());
            
            if (finalAmount != null && depositAmount != null) {
                BigDecimal remaining = finalAmount.subtract(depositAmount);
                remainingAmountField.setText(remaining.toString());
            }
        } catch (Exception e) {
            // Ignorer les erreurs de parsing
        }
    }

    private String getString(String key) {
        Object value = projectData.get(key);
        return value != null ? value.toString() : "";
    }

    private String getBigDecimalAsString(String key) {
        Object value = projectData.get(key);
        return value != null ? value.toString() : "";
    }

    private BigDecimal parseAmount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Double parseAmountAsDouble(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Valide le formulaire et active/désactive le bouton Enregistrer
     */
    private void validateForm(Node saveButton) {
        boolean isValid = true;
        
        // Vérifier que les champs obligatoires sont remplis
        if (nameField.getText().trim().isEmpty()) {
            isValid = false;
        }
        
        if (clientNameField.getText().trim().isEmpty()) {
            isValid = false;
        }
        
        saveButton.setDisable(!isValid);
    }
}