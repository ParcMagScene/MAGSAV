package com.magscene.magsav.desktop.dialog;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private TextField colorField;
    private TextField ownerField;
    private TextArea notesArea;

    // Gestion des modes
    private boolean isEditMode;
    private boolean isReadOnlyMode;
    private Map<String, Object> vehicleData;

    // Énumérations pour les ComboBox
    public enum VehicleType {
        VL("VL"),
        VL_17M3("VL 17 m³"),
        VL_20M3("VL 20 m³"),
        VAN("Fourgon"),
        PORTEUR("Porteur"),
        TRACTEUR("Tracteur"),
        SEMI_REMORQUE("Semi-remorque"),
        SCENE_MOBILE("Scène Mobile"),
        TRUCK("Camion"),
        TRAILER("Remorque"),
        CAR("Voiture"),
        MOTORCYCLE("Moto"),
        OTHER("Autre");

        private final String displayName;

        VehicleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum VehicleStatus {
        AVAILABLE("Disponible"),
        IN_USE("En utilisation"),
        MAINTENANCE("En maintenance"),
        OUT_OF_ORDER("Hors service"),
        RENTED_OUT("Loué externe"),
        RESERVED("Réservé");

        private final String displayName;

        VehicleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public enum FuelType {
        GASOLINE("Essence"),
        DIESEL("Diesel"),
        ELECTRIC("Électrique"),
        HYBRID("Hybride"),
        GPL("GPL"),
        OTHER("Autre");

        private final String displayName;

        FuelType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    public VehicleDialog(Map<String, Object> vehicleData) {
        this(vehicleData, false);
    }

    public VehicleDialog(Map<String, Object> vehicleData, boolean readOnlyMode) {
        this.isEditMode = vehicleData != null && !readOnlyMode;
        this.isReadOnlyMode = readOnlyMode;
        this.vehicleData = vehicleData != null ? vehicleData : new HashMap<>();

        setupDialog();

        // Interface avec onglets
        VBox content = createFormContent();
        getDialogPane().setContent(content);

        // Appliquer le thème dark au dialogue
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());

        // Mémoriser la taille et position
        WindowPreferencesService.getInstance().setupDialogMemory(getDialogPane(), "vehicle-dialog");

        // Remplir les champs si modification
        if (vehicleData != null) {
            populateFields(vehicleData);
        }

        // Désactiver les champs si en mode lecture seule
        if (isReadOnlyMode) {
            setFieldsReadOnly();
        }

        // Focus initial si pas en mode lecture seule
        if (!isReadOnlyMode && nameField != null) {
            nameField.requestFocus();
        }
    }

    private void setupDialog() {
        // Configuration des titres (suppression des doublons)
        if (isReadOnlyMode) {
            setTitle("Détails du véhicule");
            setHeaderText(null);
        } else {
            setTitle(isEditMode ? "Modifier le véhicule" : "Nouveau véhicule");
            setHeaderText(null);
        }

        // Taille du dialogue - réduite grâce aux onglets
        getDialogPane().setPrefSize(700, 500);

        // Mémorisation de la taille et position du dialog
        WindowPreferencesService.getInstance().setupDialogMemory(getDialogPane(), "vehicle-dialog");

        // Application du thème actuel au dialogue
        try {
            String currentTheme = UnifiedThemeManager.getInstance().getCurrentTheme();
            if ("dark".equals(currentTheme)) {
                getDialogPane().getStylesheets()
                        .add(getClass().getResource("/styles/theme-dark-ultra.css").toExternalForm());
            } else {
                getDialogPane().getStylesheets()
                        .add(getClass().getResource("/styles/magsav-light.css").toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du thème : " + e.getMessage());
        }
    }

    /**
     * Désactive tous les champs de saisie pour le mode lecture seule
     */
    private void setFieldsReadOnly() {
        // Champs généraux
        if (nameField != null)
            nameField.setDisable(true);
        if (brandField != null)
            brandField.setDisable(true);
        if (modelField != null)
            modelField.setDisable(true);
        if (licensePlateField != null)
            licensePlateField.setDisable(true);
        if (vinField != null)
            vinField.setDisable(true);
        if (typeCombo != null)
            typeCombo.setDisable(true);
        if (statusCombo != null)
            statusCombo.setDisable(true);
        if (fuelTypeCombo != null)
            fuelTypeCombo.setDisable(true);
        if (yearField != null)
            yearField.setDisable(true);

        // Champs techniques
        if (mileageField != null)
            mileageField.setDisable(true);
        if (maxPayloadField != null)
            maxPayloadField.setDisable(true);
        if (dimensionsField != null)
            dimensionsField.setDisable(true);

        // Champs maintenance
        if (insuranceNumberField != null)
            insuranceNumberField.setDisable(true);
        if (insuranceExpirationPicker != null)
            insuranceExpirationPicker.setDisable(true);
        if (technicalControlExpirationPicker != null)
            technicalControlExpirationPicker.setDisable(true);
        if (lastMaintenancePicker != null)
            lastMaintenancePicker.setDisable(true);
        if (nextMaintenancePicker != null)
            nextMaintenancePicker.setDisable(true);
        if (maintenanceIntervalField != null)
            maintenanceIntervalField.setDisable(true);

        // Champs financiers
        if (purchaseDatePicker != null)
            purchaseDatePicker.setDisable(true);
        if (purchasePriceField != null)
            purchasePriceField.setDisable(true);
        if (dailyRentalRateField != null)
            dailyRentalRateField.setDisable(true);
        if (currentLocationField != null)
            currentLocationField.setDisable(true);
        if (assignedDriverField != null)
            assignedDriverField.setDisable(true);
        if (colorField != null)
            colorField.setDisable(true);
        if (ownerField != null)
            ownerField.setDisable(true);

        // Notes
        if (notesArea != null)
            notesArea.setDisable(true);
    }

    private VBox createFormContent() {
        // Utiliser CustomTabPane comme EquipmentDialog pour style unifié
        CustomTabPane tabPane = new CustomTabPane();

        // Onglet 1: Informations générales
        CustomTabPane.CustomTab generalTab = 
            new CustomTabPane.CustomTab("Général", createGeneralForm(), "🚗");
        tabPane.addTab(generalTab);

        // Onglet 2: Caractéristiques techniques
        CustomTabPane.CustomTab technicalTab = 
            new CustomTabPane.CustomTab("Technique", createTechnicalForm(), "🔧");
        tabPane.addTab(technicalTab);

        // Onglet 3: Documents et maintenance
        CustomTabPane.CustomTab maintenanceTab = 
            new CustomTabPane.CustomTab("Maintenance", createMaintenanceForm(), "🔨");
        tabPane.addTab(maintenanceTab);

        // Onglet 4: Financier et localisation
        CustomTabPane.CustomTab financialTab = 
            new CustomTabPane.CustomTab("Financier", createFinancialForm(), "💰");
        tabPane.addTab(financialTab);

        // Sélectionner le premier onglet
        tabPane.selectTab(0);

        // Conteneur principal avec barre de boutons personnalisée
        VBox mainContainer = new VBox();
        mainContainer.getChildren().add(tabPane);

        // Ajouter la barre de boutons standardisée
        HBox buttonBar = createCustomButtonBar();
        mainContainer.getChildren().add(buttonBar);

        // Appliquer le thème au dialogue
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());

        return mainContainer;
    }

    private VBox createGeneralForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        int row = 0;

        // Nom (obligatoire)
        Label nameLabel = new Label("Nom du véhicule *");
        nameLabel.getStyleClass().add("form-label");
        nameField = new TextField();
        nameField.setPromptText("Ex: Camion Scène Mobile");
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);

        // Marque - Modèle
        Label brandLabel = new Label("Marque");
        brandLabel.getStyleClass().add("form-label");
        brandField = new TextField();
        brandField.setPromptText("Ex: Mercedes");
        grid.add(brandLabel, 0, row);
        grid.add(brandField, 1, row);

        Label modelLabel = new Label("Modèle");
        modelLabel.getStyleClass().add("form-label");
        modelField = new TextField();
        modelField.setPromptText("Ex: Sprinter");
        grid.add(modelLabel, 2, row);
        grid.add(modelField, 3, row++);

        // Plaque - VIN
        Label plateLabel = new Label("Plaque");
        plateLabel.getStyleClass().add("form-label");
        licensePlateField = new TextField();
        licensePlateField.setPromptText("AB-123-CD");
        grid.add(plateLabel, 0, row);
        grid.add(licensePlateField, 1, row);

        Label vinLabel = new Label("N° VIN");
        vinLabel.getStyleClass().add("form-label");
        vinField = new TextField();
        vinField.setPromptText("Numéro VIN");
        grid.add(vinLabel, 2, row);
        grid.add(vinField, 3, row++);

        // Type - Statut
        Label typeLabel = new Label("Type *");
        typeLabel.getStyleClass().add("form-label");
        typeCombo = new ComboBox<>(FXCollections.observableArrayList(VehicleType.values()));
        typeCombo.setValue(VehicleType.VAN);
        typeCombo.setPrefWidth(150);
        grid.add(typeLabel, 0, row);
        grid.add(typeCombo, 1, row);

        Label statusLabel = new Label("Statut *");
        statusLabel.getStyleClass().add("form-label");
        statusCombo = new ComboBox<>(FXCollections.observableArrayList(VehicleStatus.values()));
        statusCombo.setValue(VehicleStatus.AVAILABLE);
        statusCombo.setPrefWidth(150);
        grid.add(statusLabel, 2, row);
        grid.add(statusCombo, 3, row++);

        // Carburant - Année
        Label fuelLabel = new Label("Carburant");
        fuelLabel.getStyleClass().add("form-label");
        fuelTypeCombo = new ComboBox<>(FXCollections.observableArrayList(FuelType.values()));
        fuelTypeCombo.setValue(FuelType.DIESEL);
        fuelTypeCombo.setPrefWidth(150);
        grid.add(fuelLabel, 0, row);
        grid.add(fuelTypeCombo, 1, row);

        Label yearLabel = new Label("Année");
        yearLabel.getStyleClass().add("form-label");
        yearField = new TextField();
        yearField.setPromptText("2020");
        grid.add(yearLabel, 2, row);
        grid.add(yearField, 3, row++);

        // Couleur - Propriétaire
        Label colorLabel = new Label("Couleur");
        colorLabel.getStyleClass().add("form-label");
        colorField = new TextField();
        colorField.setPromptText("Ex: Blanc");
        grid.add(colorLabel, 0, row);
        grid.add(colorField, 1, row);

        Label ownerLabel = new Label("Propriétaire");
        ownerLabel.getStyleClass().add("form-label");
        ownerField = new TextField();
        ownerField.setPromptText("Propriétaire du véhicule");
        grid.add(ownerLabel, 2, row);
        grid.add(ownerField, 3, row++);

        container.getChildren().addAll(
            new Label("Informations générales"),
            new Separator(),
            grid
        );

        return container;
    }

    private VBox createTechnicalForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        int row = 0;

        // Kilométrage
        Label mileageLabel = new Label("Kilométrage");
        mileageLabel.getStyleClass().add("form-label");
        mileageField = new TextField();
        mileageField.setPromptText("150000");
        grid.add(mileageLabel, 0, row);
        grid.add(mileageField, 1, row);

        // Charge utile
        Label payloadLabel = new Label("Charge utile (kg)");
        payloadLabel.getStyleClass().add("form-label");
        maxPayloadField = new TextField();
        maxPayloadField.setPromptText("1500.50");
        grid.add(payloadLabel, 2, row);
        grid.add(maxPayloadField, 3, row++);

        // Dimensions
        Label dimensionsLabel = new Label("Dimensions");
        dimensionsLabel.getStyleClass().add("form-label");
        dimensionsField = new TextField();
        dimensionsField.setPromptText("L x l x h (m)");
        grid.add(dimensionsLabel, 0, row);
        grid.add(dimensionsField, 1, row, 3, 1);

        container.getChildren().addAll(
            new Label("Caractéristiques techniques"),
            new Separator(),
            grid
        );

        return container;
    }

    private VBox createMaintenanceForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        int row = 0;

        // Assurance
        Label insuranceLabel = new Label("N° Assurance");
        insuranceLabel.getStyleClass().add("form-label");
        insuranceNumberField = new TextField();
        insuranceNumberField.setPromptText("Numéro d'assurance");
        grid.add(insuranceLabel, 0, row);
        grid.add(insuranceNumberField, 1, row);

        Label insuranceExpLabel = new Label("Expiration assurance");
        insuranceExpLabel.getStyleClass().add("form-label");
        insuranceExpirationPicker = new DatePicker();
        grid.add(insuranceExpLabel, 2, row);
        grid.add(insuranceExpirationPicker, 3, row++);

        // Contrôle technique
        Label ctLabel = new Label("Contrôle technique");
        ctLabel.getStyleClass().add("form-label");
        technicalControlExpirationPicker = new DatePicker();
        grid.add(ctLabel, 0, row);
        grid.add(technicalControlExpirationPicker, 1, row++);

        // Maintenance
        Label lastMaintLabel = new Label("Dernière maintenance");
        lastMaintLabel.getStyleClass().add("form-label");
        lastMaintenancePicker = new DatePicker();
        grid.add(lastMaintLabel, 0, row);
        grid.add(lastMaintenancePicker, 1, row);

        Label nextMaintLabel = new Label("Prochaine maintenance");
        nextMaintLabel.getStyleClass().add("form-label");
        nextMaintenancePicker = new DatePicker();
        grid.add(nextMaintLabel, 2, row);
        grid.add(nextMaintenancePicker, 3, row++);

        // Intervalle maintenance
        Label intervalLabel = new Label("Intervalle (km)");
        intervalLabel.getStyleClass().add("form-label");
        maintenanceIntervalField = new TextField();
        maintenanceIntervalField.setPromptText("15000");
        grid.add(intervalLabel, 0, row);
        grid.add(maintenanceIntervalField, 1, row);

        container.getChildren().addAll(
            new Label("Documents et maintenance"),
            new Separator(),
            grid
        );

        return container;
    }

    private VBox createFinancialForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        int row = 0;

        // Date d'achat - Prix d'achat
        Label purchaseDateLabel = new Label("Date d'achat");
        purchaseDateLabel.getStyleClass().add("form-label");
        purchaseDatePicker = new DatePicker();
        grid.add(purchaseDateLabel, 0, row);
        grid.add(purchaseDatePicker, 1, row);

        Label purchasePriceLabel = new Label("Prix d'achat (€)");
        purchasePriceLabel.getStyleClass().add("form-label");
        purchasePriceField = new TextField();
        purchasePriceField.setPromptText("45000.00");
        grid.add(purchasePriceLabel, 2, row);
        grid.add(purchasePriceField, 3, row++);

        // Tarif location
        Label rentalLabel = new Label("Tarif location/jour (€)");
        rentalLabel.getStyleClass().add("form-label");
        dailyRentalRateField = new TextField();
        dailyRentalRateField.setPromptText("150.00");
        grid.add(rentalLabel, 0, row);
        grid.add(dailyRentalRateField, 1, row++);

        // Localisation - Conducteur
        Label locationLabel = new Label("Localisation");
        locationLabel.getStyleClass().add("form-label");
        currentLocationField = new TextField();
        currentLocationField.setPromptText("Entrepôt Principal");
        grid.add(locationLabel, 0, row);
        grid.add(currentLocationField, 1, row);

        Label driverLabel = new Label("Conducteur assigné");
        driverLabel.getStyleClass().add("form-label");
        assignedDriverField = new TextField();
        assignedDriverField.setPromptText("Nom du conducteur");
        grid.add(driverLabel, 2, row);
        grid.add(assignedDriverField, 3, row++);

        // Notes
        Label notesLabel = new Label("Notes");
        notesLabel.getStyleClass().add("form-label");
        notesArea = new TextArea();
        notesArea.setPromptText("Notes, observations...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);
        grid.add(notesLabel, 0, row);
        grid.add(notesArea, 1, row, 3, 1);

        container.getChildren().addAll(
            new Label("Financier et localisation"),
            new Separator(),
            grid
        );

        return container;
    }

    /**
     * Crée une barre de boutons personnalisée avec les styles ViewUtils
     */
    private HBox createCustomButtonBar() {
        if (isReadOnlyMode) {
            return ViewUtils.createReadOnlyButtonBar(
                () -> {
                    // Action Modifier : ouvrir en mode édition
                    if (vehicleData != null) {
                        VehicleDialog editDialog = new VehicleDialog(vehicleData, false);
                        editDialog.showAndWait().ifPresent(result -> {
                            setResult(result);
                        });
                    }
                    forceClose();
                },
                () -> forceClose()
            );
        } else {
            return ViewUtils.createDialogButtonBar(
                () -> {
                    // Action Enregistrer
                    if (validateForm()) {
                        setResult(collectFormData());
                        forceClose();
                    }
                },
                () -> {
                    setResult(null);
                    forceClose();
                },
                null
            );
        }
    }

    /**
     * Valide le formulaire avant sauvegarde
     */
    private boolean validateForm() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            nameField.setStyle("-fx-border-color: red;");
            return false;
        }
        return true;
    }

    /**
     * Force la fermeture du dialog
     */
    private void forceClose() {
        getDialogPane().getButtonTypes().clear();
        close();
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().hide();
        }
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
            } catch (IllegalArgumentException ignored) {
            }
        }

        String status = getStringValue(data, "status");
        if (status != null) {
            try {
                statusCombo.setValue(VehicleStatus.valueOf(status));
            } catch (IllegalArgumentException ignored) {
            }
        }

        String fuelType = getStringValue(data, "fuelType");
        if (fuelType != null) {
            try {
                fuelTypeCombo.setValue(FuelType.valueOf(fuelType));
            } catch (IllegalArgumentException ignored) {
            }
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
        colorField.setText(getStringValue(data, "color"));
        ownerField.setText(getStringValue(data, "owner"));
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
        data.put("color", colorField.getText().trim());
        data.put("owner", ownerField.getText().trim());
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
            } catch (Exception ignored) {
            }
        }
    }

    private void putIntegerField(Map<String, Object> data, String key, TextField field) {
        String text = field.getText().trim();
        if (!text.isEmpty()) {
            try {
                data.put(key, Integer.parseInt(text));
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void putBigDecimalField(Map<String, Object> data, String key, TextField field) {
        String text = field.getText().trim();
        if (!text.isEmpty()) {
            try {
                data.put(key, new BigDecimal(text));
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
