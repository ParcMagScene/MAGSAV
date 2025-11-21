package com.magscene.magsav.desktop.dialog.sav;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.util.ViewUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Dialogue pour créer/modifier un RMA (Return Merchandise Authorization)
 */
public class RMADialog extends Dialog<Map<String, Object>> {

    // Onglet Général
    private TextField rmaNumberField;
    private TextField equipmentField;
    private ComboBox<String> reasonCombo;
    private ComboBox<String> statusCombo;
    private ComboBox<String> priorityCombo;
    private TextArea descriptionArea;

    // Onglet Client & Expédition
    private TextField customerNameField;
    private TextField customerContactField;
    private TextField customerEmailField;
    private TextField shippingAddressArea;
    private TextField trackingNumberField;
    private ComboBox<String> carrierCombo;

    // Onglet Dates & Suivi
    private DatePicker requestDatePicker;
    private DatePicker approvalDatePicker;
    private DatePicker shipmentDatePicker;
    private DatePicker returnDatePicker;
    private DatePicker completionDatePicker;

    // Onglet Financier
    private TextField estimatedValueField;
    private TextField shippingCostField;
    private TextField repairCostField;
    private TextField refundAmountField;
    private ComboBox<String> resolutionTypeCombo;

    // Onglet Notes
    private TextArea internalNotesArea;
    private TextArea customerNotesArea;
    private TextArea technicalNotesArea;

    private final Map<String, Object> rmaData;
    private boolean isReadOnlyMode;
    private final ApiService apiService;

    public RMADialog(String title, Map<String, Object> existingRMA) {
        this(title, existingRMA, false);
    }

    public RMADialog(String title, Map<String, Object> existingRMA, boolean readOnlyMode) {
        this.isReadOnlyMode = readOnlyMode;
        this.apiService = new ApiService();
        
        if (readOnlyMode) {
            setTitle("Détails du RMA");
            setHeaderText("Consultation des informations du RMA");
        } else {
            setTitle(title);
            setHeaderText("Saisir les informations du RMA");
        }
        
        this.rmaData = existingRMA != null ? new HashMap<>(existingRMA) : new HashMap<>();
        
        // Configuration des boutons
        setupButtons();
        
        // Création de l'interface à onglets
        com.magscene.magsav.desktop.component.CustomTabPane tabPane = createTabbedInterface();
        getDialogPane().setContent(tabPane);
        
        // Remplir les champs si modification
        if (existingRMA != null) {
            populateFields();
        }
        
        // Désactiver les champs en mode lecture seule
        if (isReadOnlyMode) {
            setFieldsReadOnly();
        }
        
        // Configuration du résultat et validation
        setupResultConverter();
        
        // Style CSS pour le dialogue avec thème dark
        getDialogPane().getStyleClass().add("rma-dialog");
        getDialogPane().setPrefSize(800, 600);
        
        // Appliquer le thème dark aux dialogues
        ThemeManager.getInstance().applyThemeToDialog(getDialogPane());
    }

    private com.magscene.magsav.desktop.component.CustomTabPane createTabbedInterface() {
        // Utiliser CustomTabPane au lieu de TabPane JavaFX pour style unifié
        com.magscene.magsav.desktop.component.CustomTabPane tabPane = 
            new com.magscene.magsav.desktop.component.CustomTabPane();
        
        // Onglet Général
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab generalTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Général", createGeneralPane(), "📋");
        tabPane.addTab(generalTab);
        
        // Onglet Client & Expédition
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab shippingTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Client & Expédition", createShippingPane(), "📦");
        tabPane.addTab(shippingTab);
        
        // Onglet Dates & Suivi
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab datesTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Dates & Suivi", createDatesPane(), "📅");
        tabPane.addTab(datesTab);
        
        // Onglet Financier
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab financialTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Financier", createFinancialPane(), "💰");
        tabPane.addTab(financialTab);
        
        // Onglet Notes
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab notesTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Notes", createNotesPane(), "📝");
        tabPane.addTab(notesTab);
        
        // Sélectionner le premier onglet
        tabPane.selectTab(0);
        
        return tabPane;
    }

    private Node createGeneralPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        // Numéro RMA et équipement
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        
        infoGrid.add(new Label("Numéro RMA:"), 0, 0);
        rmaNumberField = new TextField();
        rmaNumberField.setPromptText("Généré automatiquement");
        infoGrid.add(rmaNumberField, 1, 0);
        
        infoGrid.add(new Label("Équipement:"), 0, 1);
        equipmentField = new TextField();
        equipmentField.setPromptText("Nom ou référence de l'équipement");
        infoGrid.add(equipmentField, 1, 1);
        
        // Raison et statut
        infoGrid.add(new Label("Raison du retour:"), 0, 2);
        reasonCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Défaut de fabrication", "Panne", "Non-conformité", "Dommage transport", 
            "Erreur de commande", "Fin de garantie", "Autre"
        ));
        infoGrid.add(reasonCombo, 1, 2);
        
        infoGrid.add(new Label("Statut:"), 0, 3);
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(
            "INITIÉ", "AUTORISÉ", "EN TRANSIT RETOUR", "REÇU", "EN COURS D'ANALYSE",
            "RÉPARÉ", "REMPLACÉ", "REMBOURSÉ", "REFUSÉ"
        ));
        statusCombo.setValue("INITIÉ");
        infoGrid.add(statusCombo, 1, 3);
        
        infoGrid.add(new Label("Priorité:"), 0, 4);
        priorityCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Basse", "Normale", "Haute", "Critique"
        ));
        priorityCombo.setValue("Normale");
        infoGrid.add(priorityCombo, 1, 4);
        
        // Description
        Label descLabel = new Label("Description du problème:");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrire en détail le problème rencontré...");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);
        
        pane.getChildren().addAll(infoGrid, descLabel, descriptionArea);
        
        return pane;
    }

    private Node createShippingPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        GridPane shippingGrid = new GridPane();
        shippingGrid.setHgap(15);
        shippingGrid.setVgap(10);
        
        // Informations client
        shippingGrid.add(new Label("Nom du client:"), 0, 0);
        customerNameField = new TextField();
        shippingGrid.add(customerNameField, 1, 0);
        
        shippingGrid.add(new Label("Contact:"), 0, 1);
        customerContactField = new TextField();
        shippingGrid.add(customerContactField, 1, 1);
        
        shippingGrid.add(new Label("Email:"), 0, 2);
        customerEmailField = new TextField();
        shippingGrid.add(customerEmailField, 1, 2);
        
        // Adresse d'expédition
        Label addressLabel = new Label("Adresse d'expédition:");
        shippingAddressArea = new TextField();
        shippingAddressArea.setPromptText("Adresse complète pour le retour...");
        
        // Informations de transport
        shippingGrid.add(new Label("Numéro de suivi:"), 0, 3);
        trackingNumberField = new TextField();
        shippingGrid.add(trackingNumberField, 1, 3);
        
        shippingGrid.add(new Label("Transporteur:"), 0, 4);
        carrierCombo = new ComboBox<>(FXCollections.observableArrayList(
            "DHL", "UPS", "FedEx", "Chronopost", "La Poste", "Autre"
        ));
        shippingGrid.add(carrierCombo, 1, 4);
        
        pane.getChildren().addAll(shippingGrid, addressLabel, shippingAddressArea);
        
        return pane;
    }

    private Node createDatesPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        GridPane datesGrid = new GridPane();
        datesGrid.setHgap(15);
        datesGrid.setVgap(10);
        
        datesGrid.add(new Label("Date de demande:"), 0, 0);
        requestDatePicker = new DatePicker(LocalDate.now());
        datesGrid.add(requestDatePicker, 1, 0);
        
        datesGrid.add(new Label("Date d'approbation:"), 0, 1);
        approvalDatePicker = new DatePicker();
        datesGrid.add(approvalDatePicker, 1, 1);
        
        datesGrid.add(new Label("Date d'expédition:"), 0, 2);
        shipmentDatePicker = new DatePicker();
        datesGrid.add(shipmentDatePicker, 1, 2);
        
        datesGrid.add(new Label("Date de retour:"), 0, 3);
        returnDatePicker = new DatePicker();
        datesGrid.add(returnDatePicker, 1, 3);
        
        datesGrid.add(new Label("Date de finalisation:"), 0, 4);
        completionDatePicker = new DatePicker();
        datesGrid.add(completionDatePicker, 1, 4);
        
        pane.getChildren().add(datesGrid);
        
        return pane;
    }

    private Node createFinancialPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        GridPane financialGrid = new GridPane();
        financialGrid.setHgap(15);
        financialGrid.setVgap(10);
        
        financialGrid.add(new Label("Valeur estimée (€):"), 0, 0);
        estimatedValueField = new TextField();
        financialGrid.add(estimatedValueField, 1, 0);
        
        financialGrid.add(new Label("Coût d'expédition (€):"), 0, 1);
        shippingCostField = new TextField();
        financialGrid.add(shippingCostField, 1, 1);
        
        financialGrid.add(new Label("Coût de réparation (€):"), 0, 2);
        repairCostField = new TextField();
        financialGrid.add(repairCostField, 1, 2);
        
        financialGrid.add(new Label("Montant remboursé (€):"), 0, 3);
        refundAmountField = new TextField();
        financialGrid.add(refundAmountField, 1, 3);
        
        financialGrid.add(new Label("Type de résolution:"), 0, 4);
        resolutionTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Réparation", "Remplacement", "Remboursement", "Crédit", "Aucune"
        ));
        financialGrid.add(resolutionTypeCombo, 1, 4);
        
        pane.getChildren().add(financialGrid);
        
        return pane;
    }

    private Node createNotesPane() {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));

        Label internalLabel = new Label("Notes internes:");
        internalNotesArea = new TextArea();
        internalNotesArea.setPromptText("Notes pour usage interne...");
        internalNotesArea.setPrefRowCount(3);
        internalNotesArea.setWrapText(true);
        
        Label customerLabel = new Label("Notes client:");
        customerNotesArea = new TextArea();
        customerNotesArea.setPromptText("Notes visibles par le client...");
        customerNotesArea.setPrefRowCount(3);
        customerNotesArea.setWrapText(true);
        
        Label techLabel = new Label("Notes techniques:");
        technicalNotesArea = new TextArea();
        technicalNotesArea.setPromptText("Notes techniques sur l'équipement...");
        technicalNotesArea.setPrefRowCount(3);
        technicalNotesArea.setWrapText(true);
        
        pane.getChildren().addAll(
            internalLabel, internalNotesArea,
            customerLabel, customerNotesArea,
            techLabel, technicalNotesArea
        );
        
        return pane;
    }

    private void setupButtons() {
        if (isReadOnlyMode) {
            // Mode lecture seule : boutons "Modifier" et "Fermer"
            ButtonType editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.OK_DONE);
            ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
            
            // Styling des boutons
            Button editButton = (Button) getDialogPane().lookupButton(editButtonType);
            editButton.getStyleClass().add("action-button-primary");
            
        } else {
            // Mode édition : boutons "Sauvegarder" et "Annuler"
            ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
            getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Styling du bouton sauvegarder
            Button saveButton = (Button) getDialogPane().lookupButton(saveButtonType);
            saveButton.getStyleClass().add("action-button-success");
        }
    }

    private void setupResultConverter() {
        if (isReadOnlyMode) {
            setResultConverter(dialogButton -> {
                if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    // Bouton "Modifier" cliqué - ouvrir en mode édition
                    RMADialog editDialog = new RMADialog("Modifier le RMA", rmaData, false);
                    editDialog.showAndWait().ifPresent(result -> {
                        // Les modifications seront gérées par le dialogue d'édition
                    });
                    return null;
                }
                return null;
            });
        } else {
            setResultConverter(dialogButton -> {
                if (dialogButton.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    return createRMAFromFields();
                }
                return null;
            });
        }
    }

    private void setFieldsReadOnly() {
        if (rmaNumberField != null) rmaNumberField.setDisable(true);
        if (equipmentField != null) equipmentField.setDisable(true);
        if (reasonCombo != null) reasonCombo.setDisable(true);
        if (statusCombo != null) statusCombo.setDisable(true);
        if (priorityCombo != null) priorityCombo.setDisable(true);
        if (descriptionArea != null) descriptionArea.setDisable(true);
        
        if (customerNameField != null) customerNameField.setDisable(true);
        if (customerContactField != null) customerContactField.setDisable(true);
        if (customerEmailField != null) customerEmailField.setDisable(true);
        if (shippingAddressArea != null) shippingAddressArea.setDisable(true);
        if (trackingNumberField != null) trackingNumberField.setDisable(true);
        if (carrierCombo != null) carrierCombo.setDisable(true);
        
        if (requestDatePicker != null) requestDatePicker.setDisable(true);
        if (approvalDatePicker != null) approvalDatePicker.setDisable(true);
        if (shipmentDatePicker != null) shipmentDatePicker.setDisable(true);
        if (returnDatePicker != null) returnDatePicker.setDisable(true);
        if (completionDatePicker != null) completionDatePicker.setDisable(true);
        
        if (estimatedValueField != null) estimatedValueField.setDisable(true);
        if (shippingCostField != null) shippingCostField.setDisable(true);
        if (repairCostField != null) repairCostField.setDisable(true);
        if (refundAmountField != null) refundAmountField.setDisable(true);
        if (resolutionTypeCombo != null) resolutionTypeCombo.setDisable(true);
        
        if (internalNotesArea != null) internalNotesArea.setDisable(true);
        if (customerNotesArea != null) customerNotesArea.setDisable(true);
        if (technicalNotesArea != null) technicalNotesArea.setDisable(true);
    }

    private void populateFields() {
        // Remplir les champs avec les données existantes
        if (rmaData.containsKey("rmaNumber")) {
            rmaNumberField.setText(String.valueOf(rmaData.get("rmaNumber")));
        }
        if (rmaData.containsKey("equipment")) {
            equipmentField.setText(String.valueOf(rmaData.get("equipment")));
        }
        if (rmaData.containsKey("reason")) {
            reasonCombo.setValue(String.valueOf(rmaData.get("reason")));
        }
        if (rmaData.containsKey("status")) {
            statusCombo.setValue(String.valueOf(rmaData.get("status")));
        }
        // ... Ajouter d'autres champs selon les besoins
    }

    private Map<String, Object> createRMAFromFields() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("rmaNumber", rmaNumberField.getText());
        result.put("equipment", equipmentField.getText());
        result.put("reason", reasonCombo.getValue());
        result.put("status", statusCombo.getValue());
        result.put("priority", priorityCombo.getValue());
        result.put("description", descriptionArea.getText());
        
        result.put("customerName", customerNameField.getText());
        result.put("customerContact", customerContactField.getText());
        result.put("customerEmail", customerEmailField.getText());
        result.put("shippingAddress", shippingAddressArea.getText());
        result.put("trackingNumber", trackingNumberField.getText());
        result.put("carrier", carrierCombo.getValue());
        
        result.put("requestDate", requestDatePicker.getValue());
        result.put("approvalDate", approvalDatePicker.getValue());
        result.put("shipmentDate", shipmentDatePicker.getValue());
        result.put("returnDate", returnDatePicker.getValue());
        result.put("completionDate", completionDatePicker.getValue());
        
        try {
            if (!estimatedValueField.getText().isEmpty()) {
                result.put("estimatedValue", Double.parseDouble(estimatedValueField.getText()));
            }
            if (!shippingCostField.getText().isEmpty()) {
                result.put("shippingCost", Double.parseDouble(shippingCostField.getText()));
            }
            if (!repairCostField.getText().isEmpty()) {
                result.put("repairCost", Double.parseDouble(repairCostField.getText()));
            }
            if (!refundAmountField.getText().isEmpty()) {
                result.put("refundAmount", Double.parseDouble(refundAmountField.getText()));
            }
        } catch (NumberFormatException e) {
            // Gérer les erreurs de format numérique
        }
        
        result.put("resolutionType", resolutionTypeCombo.getValue());
        result.put("internalNotes", internalNotesArea.getText());
        result.put("customerNotes", customerNotesArea.getText());
        result.put("technicalNotes", technicalNotesArea.getText());
        
        return result;
    }
}