package com.magscene.magsav.desktop.dialog;

import java.util.Map;
import java.util.HashMap;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.Optional;

public class VehicleDialog extends Dialog<Map<String, Object>> {
    
    // Champs obligatoires
    private final TextField nameField = new TextField();
    private final TextField brandField = new TextField();
    private final TextField modelField = new TextField();
    private final TextField licensePlateField = new TextField();
    private final ComboBox<String> typeComboBox = new ComboBox<>();
    private final ComboBox<String> statusComboBox = new ComboBox<>();
    private final ComboBox<String> fuelTypeComboBox = new ComboBox<>();
    
    // Champs optionnels
    private final TextField vinField = new TextField();
    private final TextField yearManufacturedField = new TextField();
    private final TextField mileageField = new TextField();
    private final TextField maxPayloadField = new TextField();
    private final TextField dimensionsField = new TextField();
    private final TextField currentLocationField = new TextField();
    private final TextField assignedDriverField = new TextField();
    private final TextArea notesField = new TextArea();
    
    private final Map<String, Object> vehicleData;
    private final boolean isEdit;

    public VehicleDialog(Map<String, Object> vehicleData, Stage owner) {
        this.vehicleData = vehicleData;
        this.isEdit = vehicleData != null;
        
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(isEdit ? "Modifier le véhicule" : "Nouveau véhicule");
        setHeaderText(isEdit ? "Modification des informations du véhicule" : "Création d'un nouveau véhicule");
        
        // Boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Configuration des ComboBox
        setupComboBoxes();
        
        // Interface utilisateur
        getDialogPane().setContent(createContent());
        
        // Chargement des données si édition
        if (isEdit) {
            loadVehicleData();
        } else {
            // Valeurs par défaut pour nouveau véhicule
            statusComboBox.setValue("AVAILABLE");
        }
        
        // Configuration des validateurs
        setupValidation();
        
        // Résultat
        setResultConverter(this::convertResult);
    }

    private void setupComboBoxes() {
        // Types de véhicules
        typeComboBox.getItems().addAll("VAN", "TRUCK", "TRAILER", "CAR", "MOTORCYCLE");
        
        // Statuts des véhicules
        statusComboBox.getItems().addAll("AVAILABLE", "IN_USE", "MAINTENANCE", "RENTED_OUT", "OUT_OF_SERVICE");
        
        // Types de carburant
        fuelTypeComboBox.getItems().addAll("DIESEL", "GASOLINE", "ELECTRIC", "HYBRID", "OTHER");
    }

    private VBox createContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        // Onglets pour organiser les champs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Onglet Informations Générales
        Tab generalTab = new Tab("Général");
        generalTab.setContent(createGeneralTab());
        
        // Onglet Caractéristiques
        Tab specsTab = new Tab("Caractéristiques");
        specsTab.setContent(createSpecsTab());
        
        // Onglet Utilisation
        Tab usageTab = new Tab("Utilisation");
        usageTab.setContent(createUsageTab());
        
        tabPane.getTabs().addAll(generalTab, specsTab, usageTab);
        
        content.getChildren().add(tabPane);
        
        return content;
    }

    private GridPane createGeneralTab() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        // Informations de base
        grid.add(new Label("*Nom :"), 0, 0);
        nameField.setPrefWidth(250);
        grid.add(nameField, 1, 0);
        
        grid.add(new Label("*Marque :"), 0, 1);
        brandField.setPrefWidth(250);
        grid.add(brandField, 1, 1);
        
        grid.add(new Label("*Modèle :"), 0, 2);
        modelField.setPrefWidth(250);
        grid.add(modelField, 1, 2);
        
        grid.add(new Label("*Plaque d'immatriculation :"), 0, 3);
        licensePlateField.setPrefWidth(250);
        grid.add(licensePlateField, 1, 3);
        
        grid.add(new Label("Numéro VIN :"), 0, 4);
        vinField.setPrefWidth(250);
        grid.add(vinField, 1, 4);
        
        grid.add(new Label("*Type :"), 0, 5);
        typeComboBox.setPrefWidth(250);
        grid.add(typeComboBox, 1, 5);
        
        grid.add(new Label("*Statut :"), 0, 6);
        statusComboBox.setPrefWidth(250);
        grid.add(statusComboBox, 1, 6);
        
        grid.add(new Label("*Type de carburant :"), 0, 7);
        fuelTypeComboBox.setPrefWidth(250);
        grid.add(fuelTypeComboBox, 1, 7);
        
        return grid;
    }

    private GridPane createSpecsTab() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        
        grid.add(new Label("Année de fabrication :"), 0, 0);
        yearManufacturedField.setPrefWidth(250);
        grid.add(yearManufacturedField, 1, 0);
        
        grid.add(new Label("Kilométrage :"), 0, 1);
        mileageField.setPrefWidth(250);
        grid.add(mileageField, 1, 1);
        
        grid.add(new Label("Charge utile max (kg) :"), 0, 2);
        maxPayloadField.setPrefWidth(250);
        grid.add(maxPayloadField, 1, 2);
        
        grid.add(new Label("Dimensions (L x l x h) :"), 0, 3);
        dimensionsField.setPrefWidth(250);
        grid.add(dimensionsField, 1, 3);
        
        return grid;
    }

    private VBox createUsageTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        grid.add(new Label("Localisation actuelle :"), 0, 0);
        currentLocationField.setPrefWidth(250);
        grid.add(currentLocationField, 1, 0);
        
        grid.add(new Label("Conducteur assigné :"), 0, 1);
        assignedDriverField.setPrefWidth(250);
        grid.add(assignedDriverField, 1, 1);
        
        vbox.getChildren().add(grid);
        
        // Notes
        Label notesLabel = new Label("Notes :");
        notesField.setPrefRowCount(4);
        notesField.setWrapText(true);
        
        vbox.getChildren().addAll(notesLabel, notesField);
        
        return vbox;
    }

    private void loadVehicleData() {
        if (vehicleData != null) {
            nameField.setText(getStringValue("name"));
            brandField.setText(getStringValue("brand"));
            modelField.setText(getStringValue("model"));
            licensePlateField.setText(getStringValue("licensePlate"));
            vinField.setText(getStringValue("vin"));
            
            // Sélection des ComboBox
            if (vehicleData.containsKey("type")) {
                typeComboBox.setValue(getStringValue("type"));
            }
            if (vehicleData.containsKey("status")) {
                statusComboBox.setValue(getStringValue("status"));
            }
            if (vehicleData.containsKey("fuelType")) {
                fuelTypeComboBox.setValue(getStringValue("fuelType"));
            }
            
            // Champs numériques
            if (vehicleData.containsKey("yearManufactured") && vehicleData.get("yearManufactured") != null) {
                yearManufacturedField.setText(vehicleData.get("yearManufactured").toString());
            }
            if (vehicleData.containsKey("mileage") && vehicleData.get("mileage") != null) {
                mileageField.setText(vehicleData.get("mileage").toString());
            }
            if (vehicleData.containsKey("maxPayload") && vehicleData.get("maxPayload") != null) {
                maxPayloadField.setText(vehicleData.get("maxPayload").toString());
            }
            
            dimensionsField.setText(getStringValue("dimensions"));
            currentLocationField.setText(getStringValue("currentLocation"));
            assignedDriverField.setText(getStringValue("assignedDriver"));
            notesField.setText(getStringValue("notes"));
        }
    }

    private String getStringValue(String key) {
        Object value = vehicleData.get(key);
        return value != null ? value.toString() : "";
    }

    private void setupValidation() {
        // Récupérer le bouton de sauvegarde (premier bouton qui est notre saveButtonType)
        Button saveButton = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
        
        // Validation en temps réel
        Runnable validator = () -> {
            boolean valid = !nameField.getText().trim().isEmpty() &&
                           !brandField.getText().trim().isEmpty() &&
                           !modelField.getText().trim().isEmpty() &&
                           !licensePlateField.getText().trim().isEmpty() &&
                           typeComboBox.getValue() != null &&
                           statusComboBox.getValue() != null &&
                           fuelTypeComboBox.getValue() != null;
            
            saveButton.setDisable(!valid);
        };
        
        // Attachement des listeners
        nameField.textProperty().addListener((obs, old, val) -> validator.run());
        brandField.textProperty().addListener((obs, old, val) -> validator.run());
        modelField.textProperty().addListener((obs, old, val) -> validator.run());
        licensePlateField.textProperty().addListener((obs, old, val) -> validator.run());
        typeComboBox.valueProperty().addListener((obs, old, val) -> validator.run());
        statusComboBox.valueProperty().addListener((obs, old, val) -> validator.run());
        fuelTypeComboBox.valueProperty().addListener((obs, old, val) -> validator.run());
        
        // Validation initiale
        validator.run();
    }

    private Map<String, Object> convertResult(ButtonType buttonType) {
        if (buttonType == ButtonType.OK) {
            return createVehicleFromFields();
        }
        return null;
    }

    private Map<String, Object> createVehicleFromFields() {
        Map<String, Object> result = new HashMap<>();
        
        // ID si édition
        if (isEdit && vehicleData.containsKey("id")) {
            result.put("id", vehicleData.get("id"));
        }
        
        // Champs obligatoires
        result.put("name", nameField.getText().trim());
        result.put("brand", brandField.getText().trim());
        result.put("model", modelField.getText().trim());
        result.put("licensePlate", licensePlateField.getText().trim());
        result.put("type", typeComboBox.getValue());
        result.put("status", statusComboBox.getValue());
        result.put("fuelType", fuelTypeComboBox.getValue());
        
        // Champs optionnels
        String vin = vinField.getText().trim();
        if (!vin.isEmpty()) {
            result.put("vin", vin);
        }
        
        // Champs numériques avec gestion des erreurs
        try {
            String yearText = yearManufacturedField.getText().trim();
            if (!yearText.isEmpty()) {
                result.put("yearManufactured", Integer.parseInt(yearText));
            }
        } catch (NumberFormatException e) {
            // Ignore si format incorrect
        }
        
        try {
            String mileageText = mileageField.getText().trim();
            if (!mileageText.isEmpty()) {
                result.put("mileage", Integer.parseInt(mileageText));
            }
        } catch (NumberFormatException e) {
            // Ignore si format incorrect
        }
        
        try {
            String payloadText = maxPayloadField.getText().trim();
            if (!payloadText.isEmpty()) {
                result.put("maxPayload", Double.parseDouble(payloadText));
            }
        } catch (NumberFormatException e) {
            // Ignore si format incorrect
        }
        
        // Autres champs texte
        String dimensions = dimensionsField.getText().trim();
        if (!dimensions.isEmpty()) {
            result.put("dimensions", dimensions);
        }
        
        String location = currentLocationField.getText().trim();
        if (!location.isEmpty()) {
            result.put("currentLocation", location);
        }
        
        String driver = assignedDriverField.getText().trim();
        if (!driver.isEmpty()) {
            result.put("assignedDriver", driver);
        }
        
        String notes = notesField.getText().trim();
        if (!notes.isEmpty()) {
            result.put("notes", notes);
        }
        
        return result;
    }
}