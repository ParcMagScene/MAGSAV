package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.config.CategoriesConfigManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialogue pour ajouter/modifier un equipement
 * Interface professionnelle avec validation des champs
 */
public class EquipmentDialog extends Dialog<Map<String, Object>> {
    
    private final ApiService apiService;
    private final boolean isEditMode;
    private final boolean isReadOnlyMode;
    private Map<String, Object> equipmentData;
    private final CategoriesConfigManager categoriesManager;
    
    // Champs de formulaire
    private TextField nameField;
    private TextArea descriptionField;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> statusCombo;
    private TextField brandField;
    private TextField modelField;
    private TextField serialNumberField;
    private TextField qrCodeField;
    private TextField purchasePriceField;
    private DatePicker purchaseDatePicker;
    private TextField locationField;
    private TextField supplierField;
    private TextArea notesField;
    private TextField weightField;
    private TextField dimensionsField;
    private TextField internalRefField;
    private TextField insuranceValueField;
    private DatePicker warrantyExpirationPicker;
    private DatePicker lastMaintenancePicker;
    private DatePicker nextMaintenancePicker;
    
    // Photo management
    private Label photoPathLabel;
    private Button selectPhotoButton;
    private String selectedPhotoPath;
    private File selectedPhotoFile;  // Fichier source de la photo sélectionnée
    private final MediaService mediaService;
    
    public EquipmentDialog(ApiService apiService, Map<String, Object> equipment) {
        this(apiService, equipment, false);
    }
    
    public EquipmentDialog(ApiService apiService, Map<String, Object> equipment, boolean readOnlyMode) {
        this.apiService = apiService;
        this.isEditMode = equipment != null && !readOnlyMode;
        this.isReadOnlyMode = readOnlyMode;
        this.equipmentData = equipment != null ? equipment : new HashMap<>();
        this.categoriesManager = CategoriesConfigManager.getInstance();
        this.mediaService = MediaService.getInstance();
        
        setupDialog();
        createFormContent();
        
        if (equipment != null) {
            populateFields();
        }
    }
    
    private void setupDialog() {
        // Configuration des titres (suppression des doublons d'intitulés)
        if (isReadOnlyMode) {
            setTitle("Détails de l'équipement");
            setHeaderText(null); // Suppression du doublon - titre déjà dans la barre de titre
        } else {
            setTitle(isEditMode ? "Modifier l'équipement" : "Ajouter un équipement");
            setHeaderText(null); // Suppression du doublon - titre déjà dans la barre de titre
        }
        
        // Taille du dialogue
        getDialogPane().setPrefSize(800, 600);
        
        // Mémorisation de la taille et position du dialog
        WindowPreferencesService.getInstance().setupDialogMemory(getDialogPane(), "equipment-dialog");
        
        // Application du thème actuel au dialogue
        String currentTheme = com.magscene.magsav.desktop.theme.UnifiedThemeManager.getInstance().getCurrentTheme();
        if ("dark".equals(currentTheme)) {
            getDialogPane().getStylesheets().add(getClass().getResource("/styles/theme-dark-ultra.css").toExternalForm());
        } else {
            getDialogPane().getStylesheets().add(getClass().getResource("/styles/magsav-light.css").toExternalForm());
        }
    }
    
    private void createFormContent() {
        // Utiliser CustomTabPane au lieu de TabPane JavaFX pour style unifié
        com.magscene.magsav.desktop.component.CustomTabPane tabPane = 
            new com.magscene.magsav.desktop.component.CustomTabPane();
        
        // Onglet 1: Informations générales
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab generalTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Général", createGeneralForm(), "📋");
        tabPane.addTab(generalTab);
        
        // Onglet 2: Détails techniques
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab technicalTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Technique", createTechnicalForm(), "🔧");
        tabPane.addTab(technicalTab);
        
        // Onglet 3: Maintenance et garantie
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab maintenanceTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Maintenance", createMaintenanceForm(), "🔨");
        tabPane.addTab(maintenanceTab);
        
        // Onglet 4: Photo et documents
        com.magscene.magsav.desktop.component.CustomTabPane.CustomTab mediaTab = 
            new com.magscene.magsav.desktop.component.CustomTabPane.CustomTab("Photo", createMediaForm(), "📷");
        tabPane.addTab(mediaTab);
        
        // Sélectionner le premier onglet
        tabPane.selectTab(0);
        
        // Conteneur principal avec barre de boutons personnalisée
        VBox mainContainer = new VBox();
        mainContainer.getChildren().add(tabPane);
        
        // Ajouter la barre de boutons standardisée
        HBox buttonBar = createCustomButtonBar();
        mainContainer.getChildren().add(buttonBar);
        
        getDialogPane().setContent(mainContainer);
        
        // Appliquer le thème dark au dialogue
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        // Désactiver les champs si en mode lecture seule
        if (isReadOnlyMode) {
            setFieldsReadOnly();
        }
    }
    
    /**
     * Désactive tous les champs de saisie pour le mode lecture seule
     */
    private void setFieldsReadOnly() {
        // Champs généraux
        if (nameField != null) nameField.setDisable(true);
        if (descriptionField != null) descriptionField.setDisable(true);
        if (categoryCombo != null) categoryCombo.setDisable(true);
        if (statusCombo != null) statusCombo.setDisable(true);
        if (qrCodeField != null) qrCodeField.setDisable(true);
        if (locationField != null) locationField.setDisable(true);
        
        // Champs techniques
        if (brandField != null) brandField.setDisable(true);
        if (modelField != null) modelField.setDisable(true);
        if (serialNumberField != null) serialNumberField.setDisable(true);
        if (internalRefField != null) internalRefField.setDisable(true);
        if (weightField != null) weightField.setDisable(true);
        if (dimensionsField != null) dimensionsField.setDisable(true);
        
        // Champs de maintenance
        if (purchasePriceField != null) purchasePriceField.setDisable(true);
        if (purchaseDatePicker != null) purchaseDatePicker.setDisable(true);
        if (supplierField != null) supplierField.setDisable(true);
        if (insuranceValueField != null) insuranceValueField.setDisable(true);
        if (warrantyExpirationPicker != null) warrantyExpirationPicker.setDisable(true);
        if (lastMaintenancePicker != null) lastMaintenancePicker.setDisable(true);
        if (nextMaintenancePicker != null) nextMaintenancePicker.setDisable(true);
        if (notesField != null) notesField.setDisable(true);
        
        // Boutons de photo
        if (selectPhotoButton != null) selectPhotoButton.setDisable(true);
    }
    
    private VBox createGeneralForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Nom (obligatoire)
        Label nameLabel = new Label("Nom de l'equipement *");
        nameLabel.getStyleClass().add("form-label");
        nameField = new TextField();
        nameField.setPromptText("Ex: Console Yamaha M32");
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row++);
        
        // Description
        Label descLabel = new Label("Description");
        descLabel.getStyleClass().add("form-label");
        descriptionField = new TextArea();
        descriptionField.setPromptText("Description detaillee de l'equipement...");
        descriptionField.setPrefRowCount(3);
        grid.add(descLabel, 0, row);
        grid.add(descriptionField, 1, row++);
        
        // Categorie (obligatoire)
        Label catLabel = new Label("Categorie *");
        catLabel.getStyleClass().add("form-label");
        categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Selectionnez une categorie");
        categoryCombo.setPrefWidth(200);
        loadCategories();
        grid.add(catLabel, 0, row);
        grid.add(categoryCombo, 1, row++);
        
        // Statut (obligatoire)
        Label statusLabel = new Label("Statut *");
        statusLabel.getStyleClass().add("form-label");
        statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(
            "Disponible", "En cours d'utilisation", "En maintenance", "Hors service", "En SAV"
        ));
        statusCombo.setValue("Disponible");
        statusCombo.setPrefWidth(200);
        grid.add(statusLabel, 0, row);
        grid.add(statusCombo, 1, row++);
        
        // QR Code
        Label qrLabel = new Label("QR Code");
        qrLabel.getStyleClass().add("form-label");
        qrCodeField = new TextField();
        qrCodeField.setPromptText("Ex: QR001");
        Button generateQRButton = new Button("Generer");
        generateQRButton.setOnAction(e -> generateQRCode());
        HBox qrBox = new HBox(10, qrCodeField, generateQRButton);
        grid.add(qrLabel, 0, row);
        grid.add(qrBox, 1, row++);
        
        // Localisation
        Label locationLabel = new Label("Localisation");
        locationLabel.getStyleClass().add("form-label");
        locationField = new TextField();
        locationField.setPromptText("Ex: Entrepot A - Rack 3");
        grid.add(locationLabel, 0, row);
        grid.add(locationField, 1, row++);
        
        container.getChildren().addAll(
            new Label("Informations generales"),
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
        
        // Marque
        Label brandLabel = new Label("Marque");
        brandLabel.getStyleClass().add("form-label");
        brandField = new TextField();
        brandField.setPromptText("Ex: Yamaha");
        grid.add(brandLabel, 0, row);
        grid.add(brandField, 1, row++);
        
        // Modele
        Label modelLabel = new Label("Modele");
        modelLabel.getStyleClass().add("form-label");
        modelField = new TextField();
        modelField.setPromptText("Ex: M32");
        grid.add(modelLabel, 0, row);
        grid.add(modelField, 1, row++);
        
        // Numero de serie
        Label serialLabel = new Label("Numero de serie");
        serialLabel.getStyleClass().add("form-label");
        serialNumberField = new TextField();
        serialNumberField.setPromptText("Ex: SN-M32-001");
        grid.add(serialLabel, 0, row);
        grid.add(serialNumberField, 1, row++);
        
        // Reference interne
        Label internalRefLabel = new Label("Reference interne");
        internalRefLabel.getStyleClass().add("form-label");
        internalRefField = new TextField();
        internalRefField.setPromptText("Ex: MAG-AUDIO-001");
        grid.add(internalRefLabel, 0, row);
        grid.add(internalRefField, 1, row++);
        
        // Poids
        Label weightLabel = new Label("Poids (kg)");
        weightLabel.getStyleClass().add("form-label");
        weightField = new TextField();
        weightField.setPromptText("Ex: 15.5");
        grid.add(weightLabel, 0, row);
        grid.add(weightField, 1, row++);
        
        // Dimensions
        Label dimensionsLabel = new Label("Dimensions (L x l x H)");
        dimensionsLabel.getStyleClass().add("form-label");
        dimensionsField = new TextField();
        dimensionsField.setPromptText("Ex: 60 x 40 x 20 cm");
        grid.add(dimensionsLabel, 0, row);
        grid.add(dimensionsField, 1, row++);
        
        container.getChildren().addAll(
            new Label("Specifications techniques"),
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
        
        // Prix d'achat
        Label priceLabel = new Label("Prix d'achat (€)");
        priceLabel.getStyleClass().add("form-label");
        purchasePriceField = new TextField();
        purchasePriceField.setPromptText("Ex: 4500.00");
        grid.add(priceLabel, 0, row);
        grid.add(purchasePriceField, 1, row++);
        
        // Date d'achat
        Label purchaseDateLabel = new Label("Date d'achat");
        purchaseDateLabel.getStyleClass().add("form-label");
        purchaseDatePicker = new DatePicker();
        purchaseDatePicker.setValue(LocalDate.now());
        grid.add(purchaseDateLabel, 0, row);
        grid.add(purchaseDatePicker, 1, row++);
        
        // Fournisseur
        Label supplierLabel = new Label("Fournisseur");
        supplierLabel.getStyleClass().add("form-label");
        supplierField = new TextField();
        supplierField.setPromptText("Ex: Son Video.com");
        grid.add(supplierLabel, 0, row);
        grid.add(supplierField, 1, row++);
        
        // Valeur d'assurance
        Label insuranceLabel = new Label("Valeur d'assurance (€)");
        insuranceLabel.getStyleClass().add("form-label");
        insuranceValueField = new TextField();
        insuranceValueField.setPromptText("Ex: 5000.00");
        grid.add(insuranceLabel, 0, row);
        grid.add(insuranceValueField, 1, row++);
        
        // Expiration garantie
        Label warrantyLabel = new Label("Expiration garantie");
        warrantyLabel.getStyleClass().add("form-label");
        warrantyExpirationPicker = new DatePicker();
        grid.add(warrantyLabel, 0, row);
        grid.add(warrantyExpirationPicker, 1, row++);
        
        // Derniere maintenance
        Label lastMaintenanceLabel = new Label("Derniere maintenance");
        lastMaintenanceLabel.getStyleClass().add("form-label");
        lastMaintenancePicker = new DatePicker();
        grid.add(lastMaintenanceLabel, 0, row);
        grid.add(lastMaintenancePicker, 1, row++);
        
        // Prochaine maintenance
        Label nextMaintenanceLabel = new Label("Prochaine maintenance");
        nextMaintenanceLabel.getStyleClass().add("form-label");
        nextMaintenancePicker = new DatePicker();
        grid.add(nextMaintenanceLabel, 0, row);
        grid.add(nextMaintenancePicker, 1, row++);
        
        // Notes
        Label notesLabel = new Label("Notes");
        notesLabel.getStyleClass().add("form-label");
        notesField = new TextArea();
        notesField.setPromptText("Notes et observations...");
        notesField.setPrefRowCount(3);
        grid.add(notesLabel, 0, row);
        grid.add(notesField, 1, row++);
        
        container.getChildren().addAll(
            new Label("Maintenance et finance"),
            new Separator(),
            grid
        );
        
        return container;
    }
    
    private VBox createMediaForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        Label titleLabel = new Label("Photo de l'equipement");
        titleLabel.getStyleClass().add("section-title");
        
        VBox photoSection = new VBox(10);
        photoSection.setPadding(new Insets(10));
        photoSection.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5px;");
        
        photoPathLabel = new Label("Aucune photo selectionnee");
        photoPathLabel.getStyleClass().add("text-muted");
        
        selectPhotoButton = new Button("Selectionner une photo");
        selectPhotoButton.setOnAction(e -> selectPhoto());
        
        Button removePhotoButton = new Button("Supprimer");
        removePhotoButton.setOnAction(e -> removePhoto());
        
        HBox photoButtons = new HBox(10, selectPhotoButton, removePhotoButton);
        
        photoSection.getChildren().addAll(
            new Label("Fichier photo :"),
            photoPathLabel,
            photoButtons
        );
        
        container.getChildren().addAll(titleLabel, new Separator(), photoSection);
        
        return container;
    }
    
    private void loadCategories() {
        // Chargement depuis le gestionnaire de configuration local
        categoryCombo.getItems().clear();
        
        // Ajouter toutes les catégories (racines et sous-catégories)
        for (CategoriesConfigManager.CategoryItem category : categoriesManager.getAllCategories()) {
            categoryCombo.getItems().add(category.getFullPath());
        }
        
        // Fallback: chargement depuis l'API si la configuration locale est vide
        if (categoryCombo.getItems().isEmpty()) {
            apiService.getCategories().thenAccept(categories -> {
                Platform.runLater(() -> {
                    categories.forEach(cat -> {
                        if (cat instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> categoryMap = (Map<String, Object>) cat;
                            categoryCombo.getItems().add((String) categoryMap.get("name"));
                        }
                    });
                });
            }).exceptionally(throwable -> {
                Platform.runLater(() -> showError("Erreur", "Impossible de charger les categories: " + throwable.getMessage()));
                return null;
            });
        }
    }
    
    private void generateQRCode() {
        // Generation automatique d'un QR code unique
        String prefix = "QR";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        qrCodeField.setText(prefix + timestamp);
    }
    
    private void selectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selectionner une photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp", "*.webp", "*.avif")
        );
        
        // Ouvrir par défaut dans le dossier Photos si existant
        Path photosPath = mediaService.getPhotosPath();
        if (Files.exists(photosPath)) {
            fileChooser.setInitialDirectory(photosPath.toFile());
        }
        
        File selectedFile = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (selectedFile != null) {
            selectedPhotoFile = selectedFile;
            selectedPhotoPath = selectedFile.getName(); // Utiliser uniquement le nom du fichier
            photoPathLabel.setText(selectedFile.getName());
            photoPathLabel.getStyleClass().removeAll("text-muted");
            System.out.println("📷 Photo sélectionnée: " + selectedFile.getAbsolutePath());
        }
    }
    
    private void removePhoto() {
        selectedPhotoPath = null;
        selectedPhotoFile = null;
        photoPathLabel.setText("Aucune photo selectionnee");
        photoPathLabel.getStyleClass().add("text-muted");
    }
    
    private void populateFields() {
        if (equipmentData == null) return;
        
        // Remplissage des champs avec les donnees existantes
        nameField.setText(getStringValue("name"));
        descriptionField.setText(getStringValue("description"));
        categoryCombo.setValue(getStringValue("category"));
        statusCombo.setValue(mapEnumToDisplayStatus(getStringValue("status")));
        brandField.setText(getStringValue("brand"));
        modelField.setText(getStringValue("model"));
        serialNumberField.setText(getStringValue("serialNumber"));
        qrCodeField.setText(getStringValue("qrCode"));
        locationField.setText(getStringValue("location"));
        supplierField.setText(getStringValue("supplier"));
        notesField.setText(getStringValue("notes"));
        weightField.setText(getStringValue("weight"));
        dimensionsField.setText(getStringValue("dimensions"));
        internalRefField.setText(getStringValue("internalReference"));
        
        // Prix et valeurs
        if (equipmentData.get("purchasePrice") != null) {
            purchasePriceField.setText(equipmentData.get("purchasePrice").toString());
        }
        if (equipmentData.get("insuranceValue") != null) {
            insuranceValueField.setText(equipmentData.get("insuranceValue").toString());
        }
        
        // Dates
        setDatePickerValue(purchaseDatePicker, "purchaseDate");
        setDatePickerValue(warrantyExpirationPicker, "warrantyExpiration");
        setDatePickerValue(lastMaintenancePicker, "lastMaintenanceDate");
        setDatePickerValue(nextMaintenancePicker, "nextMaintenanceDate");
        
        // Photo existante
        String existingPhotoPath = getStringValue("photoPath");
        if (!existingPhotoPath.isEmpty()) {
            selectedPhotoPath = existingPhotoPath;
            // Extraire juste le nom du fichier si c'est un chemin complet
            String displayName = existingPhotoPath.contains("/") || existingPhotoPath.contains("\\")
                ? new File(existingPhotoPath).getName()
                : existingPhotoPath;
            photoPathLabel.setText(displayName);
            photoPathLabel.getStyleClass().removeAll("text-muted");
            System.out.println("📷 Photo existante chargée: " + existingPhotoPath);
        }
    }
    
    private String getStringValue(String key) {
        Object value = equipmentData.get(key);
        return value != null ? value.toString() : "";
    }
    
    private void setDatePickerValue(DatePicker picker, String key) {
        String dateStr = getStringValue(key);
        if (!dateStr.isEmpty()) {
            try {
                picker.setValue(LocalDate.parse(dateStr.substring(0, 10)));
            } catch (Exception e) {
                // Date parsing failed, ignore
            }
        }
    }

    
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Erreur de validation", "Le nom de l'equipement est obligatoire.");
            return false;
        }
        
        if (categoryCombo.getValue() == null || categoryCombo.getValue().isEmpty()) {
            showError("Erreur de validation", "La categorie est obligatoire.");
            return false;
        }
        
        if (statusCombo.getValue() == null || statusCombo.getValue().isEmpty()) {
            showError("Erreur de validation", "Le statut est obligatoire.");
            return false;
        }
        
        // Validation du prix
        if (!purchasePriceField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(purchasePriceField.getText().trim());
            } catch (NumberFormatException e) {
                showError("Erreur de validation", "Le prix d'achat doit etre un nombre valide.");
                return false;
            }
        }
        
        // Validation de la valeur d'assurance
        if (!insuranceValueField.getText().trim().isEmpty()) {
            try {
                Double.parseDouble(insuranceValueField.getText().trim());
            } catch (NumberFormatException e) {
                showError("Erreur de validation", "La valeur d'assurance doit etre un nombre valide.");
                return false;
            }
        }
        
        return true;
    }
    
    private Map<String, Object> collectFormData() {
        Map<String, Object> data = new HashMap<>();
        
        // Informations generales
        data.put("name", nameField.getText().trim());
        data.put("description", getStringOrNull(descriptionField.getText()));
        data.put("category", categoryCombo.getValue());
        data.put("status", mapDisplayStatusToEnum(statusCombo.getValue()));
        data.put("qrCode", getStringOrNull(qrCodeField.getText()));
        data.put("location", getStringOrNull(locationField.getText()));
        
        // Informations techniques
        data.put("brand", getStringOrNull(brandField.getText()));
        data.put("model", getStringOrNull(modelField.getText()));
        data.put("serialNumber", getStringOrNull(serialNumberField.getText()));
        data.put("internalReference", getStringOrNull(internalRefField.getText()));
        data.put("dimensions", getStringOrNull(dimensionsField.getText()));
        
        // Poids (seulement si non vide et valide)
        if (!weightField.getText().trim().isEmpty()) {
            try {
                data.put("weight", Double.parseDouble(weightField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid weight
            }
        }
        
        // Informations financieres et maintenance
        if (!purchasePriceField.getText().trim().isEmpty()) {
            try {
                data.put("purchasePrice", Double.parseDouble(purchasePriceField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid price
            }
        }
        
        if (!insuranceValueField.getText().trim().isEmpty()) {
            try {
                data.put("insuranceValue", Double.parseDouble(insuranceValueField.getText().trim()));
            } catch (NumberFormatException e) {
                // Ignore invalid insurance value
            }
        }
        
        data.put("supplier", supplierField.getText().trim());
        data.put("notes", notesField.getText().trim());
        
        // Dates
        if (purchaseDatePicker.getValue() != null) {
            data.put("purchaseDate", purchaseDatePicker.getValue().toString() + "T00:00:00");
        }
        if (warrantyExpirationPicker.getValue() != null) {
            data.put("warrantyExpiration", warrantyExpirationPicker.getValue().toString() + "T00:00:00");
        }
        if (lastMaintenancePicker.getValue() != null) {
            data.put("lastMaintenanceDate", lastMaintenancePicker.getValue().toString() + "T00:00:00");
        }
        if (nextMaintenancePicker.getValue() != null) {
            data.put("nextMaintenanceDate", nextMaintenancePicker.getValue().toString() + "T00:00:00");
        }
        
        // Photo - copier dans le dossier médias si nécessaire
        if (selectedPhotoFile != null && selectedPhotoFile.exists()) {
            try {
                // Copier la photo dans le dossier Medias MAGSAV/Photos
                String photoFileName = copyPhotoToMediaFolder(selectedPhotoFile);
                data.put("photoPath", photoFileName);
                System.out.println("📷 Photo copiée et enregistrée: " + photoFileName);
            } catch (IOException e) {
                System.err.println("⚠️ Erreur copie photo: " + e.getMessage());
                // Utiliser le nom de fichier original si la copie échoue
                data.put("photoPath", selectedPhotoFile.getName());
            }
        } else if (selectedPhotoPath != null && !selectedPhotoPath.isEmpty()) {
            // Photo existante (chemin relatif déjà enregistré)
            data.put("photoPath", selectedPhotoPath);
        }
        
        // En mode edition, inclure l'ID
        if (isEditMode && equipmentData.get("id") != null) {
            data.put("id", equipmentData.get("id"));
        }
        
        return data;
    }
    
    /**
     * Copie une photo dans le dossier Medias MAGSAV/Photos
     * @param sourceFile le fichier source
     * @return le nom du fichier copié (chemin relatif)
     */
    private String copyPhotoToMediaFolder(File sourceFile) throws IOException {
        Path photosPath = mediaService.getPhotosPath();
        
        // Créer le dossier s'il n'existe pas
        if (!Files.exists(photosPath)) {
            Files.createDirectories(photosPath);
        }
        
        // Utiliser le nom original du fichier
        String fileName = sourceFile.getName();
        Path targetPath = photosPath.resolve(fileName);
        
        // Si le fichier existe déjà avec le même nom, ajouter un suffixe
        if (Files.exists(targetPath) && !sourceFile.toPath().equals(targetPath)) {
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            int counter = 1;
            while (Files.exists(targetPath)) {
                fileName = baseName + "_" + counter + extension;
                targetPath = photosPath.resolve(fileName);
                counter++;
            }
        }
        
        // Copier le fichier (sauf s'il est déjà dans le dossier cible)
        if (!sourceFile.toPath().equals(targetPath)) {
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("📷 Fichier copié vers: " + targetPath);
        }
        
        return fileName;
    }
    
    private String mapDisplayStatusToEnum(String displayStatus) {
        if (displayStatus == null) return "AVAILABLE";
        
        switch (displayStatus) {
            case "Disponible":
                return "AVAILABLE";
            case "En cours d'utilisation":
                return "IN_USE";
            case "En maintenance":
                return "MAINTENANCE";
            case "Hors service":
                return "OUT_OF_ORDER";
            case "En SAV":
                return "IN_SAV";
            default:
                return "AVAILABLE";
        }
    }
    
    private String mapEnumToDisplayStatus(String enumStatus) {
        if (enumStatus == null) return "Disponible";
        
        switch (enumStatus) {
            case "AVAILABLE":
                return "Disponible";
            case "IN_USE":
                return "En cours d'utilisation";
            case "MAINTENANCE":
                return "En maintenance";
            case "OUT_OF_ORDER":
                return "Hors service";
            case "IN_SAV":
                return "En SAV";
            case "RETIRED":
                return "Retire";
            default:
                return "Disponible";
        }
    }
    
    /**
     * Utilitaire pour convertir les chaines vides en null afin d'eviter les contraintes d'unicite
     */
    private String getStringOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Crée une barre de boutons personnalisée avec les styles ViewUtils
     */
    private HBox createCustomButtonBar() {
        if (isReadOnlyMode) {
            // Mode lecture seule : boutons Modifier et Fermer (sans icônes)
            return com.magscene.magsav.desktop.util.ViewUtils.createReadOnlyButtonBar(
                () -> {
                    // Action Modifier : ouvrir en mode édition
                    if (equipmentData != null) {
                        EquipmentDialog editDialog = new EquipmentDialog(apiService, equipmentData, false);
                        editDialog.showAndWait().ifPresent(result -> {
                            // Propager le résultat vers le parent si nécessaire
                            setResult(result);
                        });
                    }
                    forceClose();
                },
                () -> {
                    // Action Fermer
                    forceClose();
                }
            );
        } else {
            // Mode édition : boutons Enregistrer et Annuler
            return com.magscene.magsav.desktop.util.ViewUtils.createDialogButtonBar(
                () -> {
                    // Action Enregistrer
                    if (validateForm()) {
                        setResult(collectFormData());
                        forceClose();
                    }
                },
                () -> {
                    // Action Annuler
                    setResult(null);
                    forceClose();
                },
                null            );
        }
    }
    
    /**
     * Force la fermeture du dialog même avec des boutons personnalisés
     */
    private void forceClose() {
        // Méthode robuste pour fermer un Dialog JavaFX
        getDialogPane().getButtonTypes().clear(); // Vide les boutons natifs si présents
        close(); // Fermeture standard; // Si la fermeture standard échoue, forcer via la fenêtre
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().hide();
        }
    }
}
