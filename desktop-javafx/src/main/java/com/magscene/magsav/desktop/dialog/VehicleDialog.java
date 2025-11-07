package com.magscene.magsav.desktop.dialog;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog pour création/modification d'un véhicule
 */
public class VehicleDialog extends Dialog<Map<String, Object>> {
    
    // Champs du formulaire
    private TextField nameField;
    private TextField brandField;
    private TextField modelField;
    private TextField licensePlateField;
    private TextField vinField;
    private ComboBox<VehicleType> typeCombo;
    private ComboBox<VehicleStatus> statusCombo;
    private ComboBox<FuelType> fuelTypeCombo;
    
    private TextField yearField;
    private TextField mileageField;
    private TextField maxPayloadField;
    private TextField dimensionsField;
    
    private TextField insuranceNumberField;
    private DatePicker insuranceExpirationPicker;
    private DatePicker technicalControlExpirationPicker;
    
    private DatePicker lastMaintenancePicker;
    private DatePicker nextMaintenancePicker;
    private TextField maintenanceIntervalField;
    
    private DatePicker purchaseDatePicker;
    private TextField purchasePriceField;
    private TextField dailyRentalRateField;
    
    private TextField currentLocationField;
    private TextField assignedDriverField;
    private TextArea notesArea;
    
    // Énumérations pour les ComboBox
    public enum VehicleType {
        VAN("Fourgon"),
        TRUCK("Camion"),
        TRAILER("Remorque"),
        CAR("Voiture"),
        MOTORCYCLE("Moto"),
        OTHER("Autre");
        
        private final String displayName;
        VehicleType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }
    
    public enum VehicleStatus {
        AVAILABLE("Disponible"),
        IN_USE("En utilisation"),
        MAINTENANCE("En maintenance"),
        OUT_OF_ORDER("Hors service"),
        RENTED_OUT("Loué externe"),
        RESERVED("Réservé");
        
        private final String displayName;
        VehicleStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }
    
    public enum FuelType {
        GASOLINE("Essence"),
        DIESEL("Diesel"),
        ELECTRIC("Électrique"),
        HYBRID("Hybride"),
        GPL("GPL"),
        OTHER("Autre");
        
        private final String displayName;
        FuelType(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }
    
    public VehicleDialog(Map<String, Object> vehicleData) {
        boolean isEdit = vehicleData != null;
        
        setTitle(isEdit ? "Modifier le vehicule" : "Nouveau vehicule");
        setHeaderText(isEdit ? "Modification d'un vehicule existant" : "Creation d'un nouveau vehicule");
        
        // Boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Interface
        VBox content = createFormContent();
        getDialogPane().setContent(content);
        
        // Remplir les champs si modification
        if (isEdit) {
            populateFields(vehicleData);
        }
        
        // Validation et conversion résultat
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return collectFormData();
            }
            return null;
        });
        
        // Validation en temps réel
        setupValidation();
        
        // Focus initial
        nameField.requestFocus();
    }
    
    private VBox createFormContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        
        // Section Informations générales
        content.getChildren().add(createGeneralInfoSection());
        
        // Section Caractéristiques techniques
        content.getChildren().add(createTechnicalSection());
        
        // Section Documents et maintenance
        content.getChildren().add(createMaintenanceSection());
        
        // Section Financier et localisation
        content.getChildren().add(createFinancialSection());
        
        // Section Notes
        content.getChildren().add(createNotesSection());
        
        return content;
    }
    
    private VBox createGeneralInfoSection() {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label("Informations generales");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Ligne 1
        nameField = new TextField();
        nameField.setPromptText("Nom du vehicule");
        grid.add(new Label("Nom *:"), 0, 0);
        grid.add(nameField, 1, 0);
        
        brandField = new TextField();
        brandField.setPromptText("Marque");
        grid.add(new Label("Marque:"), 2, 0);
        grid.add(brandField, 3, 0);
        
        // Ligne 2
        modelField = new TextField();
        modelField.setPromptText("Modele");
        grid.add(new Label("Modele:"), 0, 1);
        grid.add(modelField, 1, 1);
        
        licensePlateField = new TextField();
        licensePlateField.setPromptText("AB-123-CD");
        grid.add(new Label("Plaque:"), 2, 1);
        grid.add(licensePlateField, 3, 1);
        
        // Ligne 3
        vinField = new TextField();
        vinField.setPromptText("Numéro VIN");
        grid.add(new Label("VIN:"), 0, 2);
        grid.add(vinField, 1, 2);
        
        yearField = new TextField();
        yearField.setPromptText("2020");
        grid.add(new Label("Année:"), 2, 2);
        grid.add(yearField, 3, 2);
        
        // Ligne 4 - ComboBox
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(VehicleType.values()));
        typeCombo.setValue(VehicleType.VAN);
        grid.add(new Label("Type *:"), 0, 3);
        grid.add(typeCombo, 1, 3);
        
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(VehicleStatus.values()));
        statusCombo.setValue(VehicleStatus.AVAILABLE);
        grid.add(new Label("Statut *:"), 2, 3);
        grid.add(statusCombo, 3, 3);
        
        // Ligne 5
        fuelTypeCombo = new ComboBox<>(FXCollections.observableArrayList(FuelType.values()));
        fuelTypeCombo.setValue(FuelType.DIESEL);
        grid.add(new Label("Carburant:"), 0, 4);
        grid.add(fuelTypeCombo, 1, 4);
        
        section.getChildren().addAll(titleLabel, grid);
        return section;
    }
    
    private VBox createTechnicalSection() {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label("Caractéristiques techniques");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        mileageField = new TextField();
        mileageField.setPromptText("150000");
        grid.add(new Label("Kilométrage:"), 0, 0);
        grid.add(mileageField, 1, 0);
        
        maxPayloadField = new TextField();
        maxPayloadField.setPromptText("1500.50");
        grid.add(new Label("Charge utile (kg):"), 2, 0);
        grid.add(maxPayloadField, 3, 0);
        
        dimensionsField = new TextField();
        dimensionsField.setPromptText("L x l x h (m)");
        grid.add(new Label("Dimensions:"), 0, 1);
        grid.add(dimensionsField, 1, 1, 3, 1); // Span 3 colonnes
        
        section.getChildren().addAll(titleLabel, grid);
        return section;
    }
    
    private VBox createMaintenanceSection() {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label("Documents et maintenance");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Assurance
        insuranceNumberField = new TextField();
        insuranceNumberField.setPromptText("Numéro d'assurance");
        grid.add(new Label("N° Assurance:"), 0, 0);
        grid.add(insuranceNumberField, 1, 0);
        
        insuranceExpirationPicker = new DatePicker();
        grid.add(new Label("Expiration assurance:"), 2, 0);
        grid.add(insuranceExpirationPicker, 3, 0);
        
        // Contrôle technique
        technicalControlExpirationPicker = new DatePicker();
        grid.add(new Label("Contrôle technique:"), 0, 1);
        grid.add(technicalControlExpirationPicker, 1, 1);
        
        // Maintenance
        lastMaintenancePicker = new DatePicker();
        grid.add(new Label("Dernière maintenance:"), 0, 2);
        grid.add(lastMaintenancePicker, 1, 2);
        
        nextMaintenancePicker = new DatePicker();
        grid.add(new Label("Prochaine maintenance:"), 2, 2);
        grid.add(nextMaintenancePicker, 3, 2);
        
        maintenanceIntervalField = new TextField();
        maintenanceIntervalField.setPromptText("15000");
        grid.add(new Label("Intervalle maintenance (km):"), 0, 3);
        grid.add(maintenanceIntervalField, 1, 3);
        
        section.getChildren().addAll(titleLabel, grid);
        return section;
    }
    
    private VBox createFinancialSection() {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label("Informations financières et localisation");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        
        // Achat
        purchaseDatePicker = new DatePicker();
        grid.add(new Label("Date d'achat:"), 0, 0);
        grid.add(purchaseDatePicker, 1, 0);
        
        purchasePriceField = new TextField();
        purchasePriceField.setPromptText("45000.00");
        grid.add(new Label("Prix d'achat (€):"), 2, 0);
        grid.add(purchasePriceField, 3, 0);
        
        // Location
        dailyRentalRateField = new TextField();
        dailyRentalRateField.setPromptText("150.00");
        grid.add(new Label("Tarif location/jour (€):"), 0, 1);
        grid.add(dailyRentalRateField, 1, 1);
        
        // Localisation et conducteur
        currentLocationField = new TextField();
        currentLocationField.setPromptText("Entrepôt Principal");
        grid.add(new Label("Localisation actuelle:"), 0, 2);
        grid.add(currentLocationField, 1, 2);
        
        assignedDriverField = new TextField();
        assignedDriverField.setPromptText("Nom du conducteur assigné");
        grid.add(new Label("Conducteur assigné:"), 2, 2);
        grid.add(assignedDriverField, 3, 2);
        
        section.getChildren().addAll(titleLabel, grid);
        return section;
    }
    
    private VBox createNotesSection() {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label("Notes et observations");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        notesArea = new TextArea();
        notesArea.setPromptText("Notes, observations, particularités du véhicule...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        
        section.getChildren().addAll(titleLabel, notesArea);
        return section;
    }
    
    private void populateFields(Map<String, Object> data) {
        nameField.setText(getStringValue(data, "name"));
        brandField.setText(getStringValue(data, "brand"));
        modelField.setText(getStringValue(data, "model"));
        licensePlateField.setText(getStringValue(data, "licensePlate"));
        vinField.setText(getStringValue(data, "vin"));
        
        // Types énumérés
        String type = getStringValue(data, "type");
        if (type != null) {
            try {
                typeCombo.setValue(VehicleType.valueOf(type));
            } catch (IllegalArgumentException ignored) {}
        }
        
        String status = getStringValue(data, "status");
        if (status != null) {
            try {
                statusCombo.setValue(VehicleStatus.valueOf(status));
            } catch (IllegalArgumentException ignored) {}
        }
        
        String fuelType = getStringValue(data, "fuelType");
        if (fuelType != null) {
            try {
                fuelTypeCombo.setValue(FuelType.valueOf(fuelType));
            } catch (IllegalArgumentException ignored) {}
        }
        
        // Champs numériques
        setIntegerField(yearField, data, "yearManufactured");
        setIntegerField(mileageField, data, "mileage");
        setIntegerField(maintenanceIntervalField, data, "maintenanceIntervalKm");
        
        // Champs décimaux
        setBigDecimalField(maxPayloadField, data, "maxPayload");
        setBigDecimalField(purchasePriceField, data, "purchasePrice");
        setBigDecimalField(dailyRentalRateField, data, "dailyRentalRate");
        
        // Autres champs texte
        dimensionsField.setText(getStringValue(data, "dimensions"));
        insuranceNumberField.setText(getStringValue(data, "insuranceNumber"));
        currentLocationField.setText(getStringValue(data, "currentLocation"));
        assignedDriverField.setText(getStringValue(data, "assignedDriver"));
        notesArea.setText(getStringValue(data, "notes"));
        
        // Dates
        setDateField(insuranceExpirationPicker, data, "insuranceExpiration");
        setDateField(technicalControlExpirationPicker, data, "technicalControlExpiration");
        setDateField(lastMaintenancePicker, data, "lastMaintenanceDate");
        setDateField(nextMaintenancePicker, data, "nextMaintenanceDate");
        setDateField(purchaseDatePicker, data, "purchaseDate");
    }
    
    private Map<String, Object> collectFormData() {
        Map<String, Object> data = new HashMap<>();
        
        data.put("name", nameField.getText().trim());
        data.put("brand", brandField.getText().trim());
        data.put("model", modelField.getText().trim());
        data.put("licensePlate", licensePlateField.getText().trim());
        data.put("vin", vinField.getText().trim());
        
        if (typeCombo.getValue() != null) {
            data.put("type", typeCombo.getValue().name());
        }
        if (statusCombo.getValue() != null) {
            data.put("status", statusCombo.getValue().name());
        }
        if (fuelTypeCombo.getValue() != null) {
            data.put("fuelType", fuelTypeCombo.getValue().name());
        }
        
        // Champs numériques
        putIntegerField(data, "yearManufactured", yearField);
        putIntegerField(data, "mileage", mileageField);
        putIntegerField(data, "maintenanceIntervalKm", maintenanceIntervalField);
        
        putBigDecimalField(data, "maxPayload", maxPayloadField);
        putBigDecimalField(data, "purchasePrice", purchasePriceField);
        putBigDecimalField(data, "dailyRentalRate", dailyRentalRateField);
        
        data.put("dimensions", dimensionsField.getText().trim());
        data.put("insuranceNumber", insuranceNumberField.getText().trim());
        data.put("currentLocation", currentLocationField.getText().trim());
        data.put("assignedDriver", assignedDriverField.getText().trim());
        data.put("notes", notesArea.getText().trim());
        
        // Dates
        if (insuranceExpirationPicker.getValue() != null) {
            data.put("insuranceExpiration", insuranceExpirationPicker.getValue().toString());
        }
        if (technicalControlExpirationPicker.getValue() != null) {
            data.put("technicalControlExpiration", technicalControlExpirationPicker.getValue().toString());
        }
        if (lastMaintenancePicker.getValue() != null) {
            data.put("lastMaintenanceDate", lastMaintenancePicker.getValue().toString());
        }
        if (nextMaintenancePicker.getValue() != null) {
            data.put("nextMaintenanceDate", nextMaintenancePicker.getValue().toString());
        }
        if (purchaseDatePicker.getValue() != null) {
            data.put("purchaseDate", purchaseDatePicker.getValue().toString());
        }
        
        return data;
    }
    
    private void setupValidation() {
        // Validation du nom obligatoire
        nameField.textProperty().addListener((obs, oldVal, newVal) -> 
            validateRequiredField());
        
        // Validation des champs numériques
        setupNumericValidation(yearField, "Année");
        setupNumericValidation(mileageField, "Kilométrage");
        setupNumericValidation(maintenanceIntervalField, "Intervalle maintenance");
        setupDecimalValidation(maxPayloadField, "Charge utile");
        setupDecimalValidation(purchasePriceField, "Prix d'achat");
        setupDecimalValidation(dailyRentalRateField, "Tarif location");
    }
    
    private void validateRequiredField() {
        Button saveButton = (Button) getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
        boolean valid = nameField.getText() != null && !nameField.getText().trim().isEmpty();
        saveButton.setDisable(!valid);
    }
    
    private void setupNumericValidation(TextField field, String fieldName) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                try {
                    Integer.parseInt(newVal.trim());
                    field.setStyle("");
                } catch (NumberFormatException e) {
                    field.setStyle("-fx-border-color: red;");
                }
            } else {
                field.setStyle("");
            }
        });
    }
    
    private void setupDecimalValidation(TextField field, String fieldName) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.trim().isEmpty()) {
                try {
                    new BigDecimal(newVal.trim());
                    field.setStyle("");
                } catch (NumberFormatException e) {
                    field.setStyle("-fx-border-color: red;");
                }
            } else {
                field.setStyle("");
            }
        });
    }
    
    // Méthodes utilitaires
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }
    
    private void setIntegerField(TextField field, Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            field.setText(((Number) value).toString());
        }
    }
    
    private void setBigDecimalField(TextField field, Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            field.setText(((Number) value).toString());
        }
    }
    
    private void setDateField(DatePicker picker, Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value != null) {
            try {
                picker.setValue(LocalDate.parse(value.toString()));
            } catch (Exception ignored) {}
        }
    }
    
    private void putIntegerField(Map<String, Object> data, String key, TextField field) {
        String text = field.getText().trim();
        if (!text.isEmpty()) {
            try {
                data.put(key, Integer.parseInt(text));
            } catch (NumberFormatException ignored) {}
        }
    }
    
    private void putBigDecimalField(Map<String, Object> data, String key, TextField field) {
        String text = field.getText().trim();
        if (!text.isEmpty()) {
            try {
                data.put(key, new BigDecimal(text));
            } catch (NumberFormatException ignored) {}
        }
    }
}
