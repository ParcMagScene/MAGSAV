package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Dialog de visualisation/édition d'un véhicule
 * S'ouvre en mode lecture seule, bouton Modifier pour basculer en édition
 */
@SuppressWarnings("unused")
public class VehicleDetailDialog extends Dialog<Map<String, Object>> {
    
    private final ApiService apiService;
    private final MediaService mediaService;
    private final Map<String, Object> vehicleData;
    
    private boolean editMode = false;
    private Button editSaveButton;  // Utilisé pour référence future
    private VBox contentBox;
    
    // ButtonTypes pour les boutons du bas
    private ButtonType editButtonType;
    private ButtonType closeButtonType;
    
    // Champs éditables
    private TextField nameField, brandField, modelField, licensePlateField, vinField;
    private TextField yearField, mileageField, maxPayloadField, dimensionsField;
    private TextField colorField, ownerField, currentLocationField, assignedDriverField;
    private TextField insuranceNumberField, maintenanceIntervalField;
    private TextField purchasePriceField, dailyRentalRateField;
    private ComboBox<String> typeCombo, statusCombo, fuelTypeCombo;
    private DatePicker insuranceExpirationPicker, technicalControlPicker;
    private DatePicker lastMaintenancePicker, nextMaintenancePicker, purchaseDatePicker;
    private TextArea notesArea;
    
    // Photo
    private Label photoPathLabel;
    private ImageView photoPreview;
    private String selectedPhotoPath;
    private File selectedPhotoFile;
    
    public VehicleDetailDialog(ApiService apiService, Map<String, Object> vehicle) {
        this.apiService = apiService;
        this.mediaService = MediaService.getInstance();
        this.vehicleData = new HashMap<>(vehicle);
        
        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Véhicule");
        setHeaderText(null);
        
        // Bouton Modifier/Enregistrer en bas du dialog (sans icône)
        editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        getDialogPane().setPrefSize(750, 600);
        getDialogPane().setMinWidth(700);
        getDialogPane().setMinHeight(550);
        
        // Appliquer le thème unifié
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        // Styliser et configurer le bouton Modifier
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        setResultConverter(buttonType -> editMode ? vehicleData : null);
    }
    
    private boolean editButtonInitialized = false;
    
    private void setupEditButton() {
        Button editButton = (Button) getDialogPane().lookupButton(editButtonType);
        if (editButton != null) {
            editSaveButton = editButton;
            updateEditButtonStyle(editButton);
            
            if (!editButtonInitialized) {
                editButtonInitialized = true;
                editButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
                    e.consume();
                    toggleEditMode();
                });
            }
        }
    }
    
    private void updateEditButtonStyle(Button editButton) {
        String buttonStyle = editMode 
            ? ThemeConstants.DIALOG_SAVE_BUTTON_STYLE
            : ThemeConstants.DIALOG_EDIT_BUTTON_STYLE;
        
        editButton.setText(editMode ? "Enregistrer" : "Modifier");
        editButton.setStyle(buttonStyle);
        editButton.setMinWidth(120);
        editButton.setPrefWidth(120);
        
        final String finalButtonStyle = buttonStyle;
        editButton.setOnMouseEntered(e -> editButton.setStyle(
            finalButtonStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        ));
        editButton.setOnMouseExited(e -> editButton.setStyle(finalButtonStyle));
    }
    
    private void setupCloseButton() {
        Button closeButton = (Button) getDialogPane().lookupButton(closeButtonType);
        if (closeButton != null) {
            closeButton.setStyle(ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE);
            closeButton.setMinWidth(100);
            closeButton.setPrefWidth(100);
            
            closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            ));
            closeButton.setOnMouseExited(e -> closeButton.setStyle(ThemeConstants.DIALOG_CLOSE_BUTTON_STYLE));
        }
    }
    
    private void createContent() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle(ThemeConstants.DIALOG_CONTENT_STYLE);
        
        // Header avec infos véhicule
        mainLayout.setTop(createHeader());
        
        // Contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle(ThemeConstants.DIALOG_CONTENT_STYLE);
        
        rebuildContent();
        
        scrollPane.setContent(contentBox);
        mainLayout.setCenter(scrollPane);
        
        getDialogPane().setContent(mainLayout);
    }
    
    private void rebuildContent() {
        contentBox.getChildren().clear();
        contentBox.getChildren().addAll(
            createSection("🚗 Informations générales", createGeneralSection()),
            createSection("🔧 Caractéristiques techniques", createTechnicalSection()),
            createSection("📋 Documents & Maintenance", createMaintenanceSection()),
            createSection("💰 Informations financières", createFinancialSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeConstants.DIALOG_HEADER_VEHICLE_STYLE);
        
        // Photo du véhicule ou icône par défaut
        String photoPath = getStringValue("photoPath");
        StackPane imageContainer = new StackPane();
        imageContainer.setMinSize(70, 55);
        imageContainer.setPrefSize(70, 55);
        imageContainer.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        
        boolean hasPhoto = false;
        if (photoPath != null && !photoPath.isEmpty()) {
            Image vehiclePhoto = mediaService.loadVehiclePhoto(photoPath, 70, 55);
            if (vehiclePhoto != null) {
                ImageView photoView = new ImageView(vehiclePhoto);
                photoView.setFitWidth(70);
                photoView.setFitHeight(55);
                photoView.setPreserveRatio(true);
                photoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 4, 0, 0, 1);");
                imageContainer.getChildren().add(photoView);
                hasPhoto = true;
            }
        }
        
        if (!hasPhoto) {
            Label vehicleIcon = new Label("🚗");
            vehicleIcon.setFont(Font.font(36));
            imageContainer.getChildren().add(vehicleIcon);
        }
        
        // Infos centrales
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String name = getStringValue("name");
        Label nameLabel = new Label(name != null && !name.isEmpty() ? name : "Véhicule sans nom");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);
        
        String brand = getStringValue("brand");
        String model = getStringValue("model");
        String plate = getStringValue("licensePlate");
        Label subtitleLabel = new Label(
            (brand != null ? brand : "") + 
            (brand != null && model != null ? " " : "") + 
            (model != null ? model : "") +
            (plate != null && !plate.isEmpty() ? " • " + plate : "")
        );
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web("#bdc3c7"));
        
        String status = getStringValue("status");
        if (status != null && !status.isEmpty()) {
            Label statusLabel = new Label("Statut: " + formatStatus(status));
            statusLabel.setFont(Font.font("Segoe UI", 11));
            statusLabel.setTextFill(getStatusColor(status));
            infoBox.getChildren().addAll(nameLabel, subtitleLabel, statusLabel);
        } else {
            infoBox.getChildren().addAll(nameLabel, subtitleLabel);
        }
        
        // Logo marque à droite
        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (brand != null && !brand.isEmpty()) {
            Image logo = mediaService.getBrandLogo(brand, 80, 40);
            if (logo != null) {
                ImageView logoView = new ImageView(logo);
                logoView.setFitHeight(35);
                logoView.setPreserveRatio(true);
                rightBox.getChildren().add(logoView);
            }
        }
        
        header.getChildren().addAll(imageContainer, infoBox, rightBox);
        return header;
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "AVAILABLE": return "Disponible";
            case "IN_USE": return "En utilisation";
            case "MAINTENANCE": return "En maintenance";
            case "OUT_OF_ORDER": return "Hors service";
            case "RENTED_OUT": return "Loué externe";
            case "RESERVED": return "Réservé";
            default: return status;
        }
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch (status.toUpperCase()) {
            case "AVAILABLE": return Color.web("#2ecc71");
            case "IN_USE": return Color.web("#3498db");
            case "MAINTENANCE": return Color.web("#f39c12");
            case "OUT_OF_ORDER": return Color.web("#e74c3c");
            case "RENTED_OUT": return Color.web("#9b59b6");
            case "RESERVED": return Color.web("#1abc9c");
            default: return Color.WHITE;
        }
    }
    
    private void toggleEditMode() {
        if (editMode) {
            saveChanges();
            saveToApi();
            editMode = false;
        } else {
            editMode = true;
        }
        
        setupEditButton();
        getDialogPane().setContent(null);
        createContent();
    }
    
    private void saveChanges() {
        if (nameField != null) vehicleData.put("name", nameField.getText().trim());
        if (brandField != null) vehicleData.put("brand", brandField.getText().trim());
        if (modelField != null) vehicleData.put("model", modelField.getText().trim());
        if (licensePlateField != null) vehicleData.put("licensePlate", licensePlateField.getText().trim());
        if (vinField != null) vehicleData.put("vin", vinField.getText().trim());
        if (colorField != null) vehicleData.put("color", colorField.getText().trim());
        if (ownerField != null) vehicleData.put("owner", ownerField.getText().trim());
        if (currentLocationField != null) vehicleData.put("currentLocation", currentLocationField.getText().trim());
        if (assignedDriverField != null) vehicleData.put("assignedDriver", assignedDriverField.getText().trim());
        if (insuranceNumberField != null) vehicleData.put("insuranceNumber", insuranceNumberField.getText().trim());
        if (dimensionsField != null) vehicleData.put("dimensions", dimensionsField.getText().trim());
        if (notesArea != null) vehicleData.put("notes", notesArea.getText().trim());
        
        // Photo - déjà sauvegardé lors de la sélection, mais on s'assure
        if (selectedPhotoPath != null) {
            vehicleData.put("photoPath", selectedPhotoPath);
        }
        
        // Champs numériques
        if (yearField != null && !yearField.getText().trim().isEmpty()) {
            try {
                vehicleData.put("yearManufactured", Integer.parseInt(yearField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (mileageField != null && !mileageField.getText().trim().isEmpty()) {
            try {
                vehicleData.put("mileage", Integer.parseInt(mileageField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (maxPayloadField != null && !maxPayloadField.getText().trim().isEmpty()) {
            try {
                vehicleData.put("maxPayload", Double.parseDouble(maxPayloadField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (maintenanceIntervalField != null && !maintenanceIntervalField.getText().trim().isEmpty()) {
            try {
                vehicleData.put("maintenanceIntervalKm", Integer.parseInt(maintenanceIntervalField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (purchasePriceField != null && !purchasePriceField.getText().trim().isEmpty()) {
            try {
                vehicleData.put("purchasePrice", Double.parseDouble(purchasePriceField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        if (dailyRentalRateField != null && !dailyRentalRateField.getText().trim().isEmpty()) {
            try {
                vehicleData.put("dailyRentalRate", Double.parseDouble(dailyRentalRateField.getText().trim()));
            } catch (NumberFormatException ignored) {}
        }
        
        // ComboBox - Convertir les valeurs d'affichage en valeurs d'enum
        if (typeCombo != null && typeCombo.getValue() != null) {
            vehicleData.put("type", unformatType(typeCombo.getValue()));
        }
        if (statusCombo != null && statusCombo.getValue() != null) {
            vehicleData.put("status", statusCombo.getValue());
        }
        if (fuelTypeCombo != null && fuelTypeCombo.getValue() != null) {
            vehicleData.put("fuelType", fuelTypeCombo.getValue());
        }
        
        // Dates
        if (insuranceExpirationPicker != null && insuranceExpirationPicker.getValue() != null) {
            vehicleData.put("insuranceExpiration", insuranceExpirationPicker.getValue().toString());
        }
        if (technicalControlPicker != null && technicalControlPicker.getValue() != null) {
            vehicleData.put("technicalControlExpiration", technicalControlPicker.getValue().toString());
        }
        if (lastMaintenancePicker != null && lastMaintenancePicker.getValue() != null) {
            vehicleData.put("lastMaintenanceDate", lastMaintenancePicker.getValue().toString());
        }
        if (nextMaintenancePicker != null && nextMaintenancePicker.getValue() != null) {
            vehicleData.put("nextMaintenanceDate", nextMaintenancePicker.getValue().toString());
        }
        if (purchaseDatePicker != null && purchaseDatePicker.getValue() != null) {
            vehicleData.put("purchaseDate", purchaseDatePicker.getValue().toString());
        }
    }
    
    private void saveToApi() {
        Object id = vehicleData.get("id");
        if (id != null && apiService != null) {
            apiService.updateVehicle(((Number) id).longValue(), vehicleData)
                .thenAccept(result -> Platform.runLater(() -> {
                    System.out.println("✅ Véhicule mis à jour avec succès");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText("Erreur lors de la sauvegarde");
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();
                    });
                    return null;
                });
        }
    }
    
    // ========== Gestion des photos ==========
    
    private void selectPhoto() {
        String vehicleName = getStringValue("name");
        String licensePlate = getStringValue("licensePlate");
        
        // Vérifier que l'immatriculation est définie
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Immatriculation requise");
            alert.setHeaderText("Veuillez d'abord saisir l'immatriculation");
            alert.setContentText("L'immatriculation est nécessaire pour nommer la photo du véhicule.");
            alert.showAndWait();
            return;
        }
        
        MediaGalleryDialog galleryDialog = new MediaGalleryDialog(
            mediaService, 
            MediaGalleryDialog.MediaType.PHOTO, 
            vehicleName, 
            licensePlate
        );
        
        galleryDialog.showAndWait().ifPresent(selection -> {
            if (selection.getSelectedFile() != null) {
                File originalFile = selection.getSelectedFile();
                
                // Créer un nom de fichier basé sur l'immatriculation
                String extension = getFileExtension(originalFile.getName());
                String newFileName = licensePlate.trim().replace(" ", "-") + extension;
                
                // Copier le fichier avec le nouveau nom si nécessaire
                File targetFile = copyPhotoWithNewName(originalFile, newFileName);
                
                if (targetFile != null) {
                    selectedPhotoFile = targetFile;
                    selectedPhotoPath = targetFile.getName();
                    
                    System.out.println("📷 Photo véhicule sélectionnée: " + selectedPhotoPath + " (basée sur immatriculation: " + licensePlate + ")");
                    
                    // Mettre à jour le label
                    if (photoPathLabel != null) {
                        photoPathLabel.setText(selectedPhotoPath);
                        photoPathLabel.setStyle("-fx-text-fill: #2c3e50;");
                    }
                    
                    // Mettre à jour l'aperçu
                    loadPhotoPreview(selectedPhotoFile.getAbsolutePath());
                    
                    // Sauvegarder dans vehicleData
                    vehicleData.put("photoPath", selectedPhotoPath);
                }
            }
        });
    }
    
    /**
     * Copie un fichier photo avec un nouveau nom basé sur l'immatriculation
     */
    private File copyPhotoWithNewName(File originalFile, String newFileName) {
        try {
            // Obtenir le dossier Photos
            File photosDir = mediaService.getPhotosDirectory();
            if (photosDir == null || !photosDir.exists()) {
                System.out.println("⚠️ Dossier Photos non trouvé");
                return originalFile; // Fallback sur le fichier original
            }
            
            File targetFile = new File(photosDir, newFileName);
            
            // Si le fichier cible existe déjà et c'est le même que l'original, ne pas copier
            if (targetFile.equals(originalFile)) {
                return originalFile;
            }
            
            // Si le fichier source est déjà dans le dossier Photos, renommer
            if (originalFile.getParentFile().equals(photosDir)) {
                // Copier plutôt que renommer pour garder l'original
                java.nio.file.Files.copy(
                    originalFile.toPath(), 
                    targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            } else {
                // Copier depuis un autre dossier
                java.nio.file.Files.copy(
                    originalFile.toPath(), 
                    targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
            
            System.out.println("✅ Photo copiée: " + originalFile.getName() + " → " + newFileName);
            return targetFile;
            
        } catch (Exception e) {
            System.out.println("⚠️ Erreur copie photo: " + e.getMessage());
            return originalFile; // Fallback sur le fichier original
        }
    }
    
    /**
     * Obtient l'extension d'un fichier
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot);
        }
        return ".jpg"; // Extension par défaut
    }
    
    private void removePhoto() {
        selectedPhotoPath = null;
        selectedPhotoFile = null;
        vehicleData.put("photoPath", null);
        
        if (photoPathLabel != null) {
            photoPathLabel.setText("Aucune photo");
            photoPathLabel.setStyle("-fx-text-fill: #6C757D; -fx-font-style: italic;");
        }
        
        if (photoPreview != null) {
            photoPreview.setImage(null);
        }
        
        System.out.println("🗑️ Photo véhicule supprimée");
    }
    
    private void loadPhotoPreview(String photoPath) {
        if (photoPreview == null) return;
        
        if (photoPath == null || photoPath.isEmpty()) {
            photoPreview.setImage(null);
            return;
        }
        
        // Essayer de charger depuis le fichier sélectionné ou via MediaService
        Image photo = null;
        if (selectedPhotoFile != null && selectedPhotoFile.exists()) {
            try {
                photo = new Image(selectedPhotoFile.toURI().toString(), 80, 60, true, true);
            } catch (Exception e) {
                System.out.println("⚠️ Erreur chargement photo sélectionnée: " + e.getMessage());
            }
        }
        
        if (photo == null) {
            photo = mediaService.loadVehiclePhoto(photoPath, 80, 60);
        }
        
        photoPreview.setImage(photo);
    }
    
    // ========== Sections de contenu ==========
    
    private VBox createSection(String title, GridPane content) {
        VBox section = new VBox(12);
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-padding: 18; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        section.getChildren().addAll(titleLabel, content);
        return section;
    }
    
    private GridPane createGeneralSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Nom
        grid.add(createFieldLabel("Nom"), 0, row);
        if (editMode) {
            nameField = new TextField(getStringValue("name"));
            nameField.setPrefWidth(200);
            grid.add(nameField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("name")), 1, row);
        }
        
        // Immatriculation
        grid.add(createFieldLabel("Immatriculation"), 2, row);
        if (editMode) {
            licensePlateField = new TextField(getStringValue("licensePlate"));
            licensePlateField.setPrefWidth(150);
            grid.add(licensePlateField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("licensePlate")), 3, row);
        }
        row++;
        
        // Marque - Modèle
        grid.add(createFieldLabel("Marque"), 0, row);
        if (editMode) {
            brandField = new TextField(getStringValue("brand"));
            brandField.setPrefWidth(150);
            grid.add(brandField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("brand")), 1, row);
        }
        
        grid.add(createFieldLabel("Modèle"), 2, row);
        if (editMode) {
            modelField = new TextField(getStringValue("model"));
            modelField.setPrefWidth(150);
            grid.add(modelField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("model")), 3, row);
        }
        row++;
        
        // Type - Statut
        grid.add(createFieldLabel("Type"), 0, row);
        if (editMode) {
            typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "VL", "VL 17 m³", "VL 20 m³", "Fourgon", "Porteur", "Tracteur", 
                "Semi-remorque", "Scène Mobile", "Camion", "Remorque", "Voiture", "Moto", "Autre"
            ));
            typeCombo.setValue(formatType(getStringValue("type")));
            typeCombo.setPrefWidth(150);
            grid.add(typeCombo, 1, row);
        } else {
            grid.add(createValueLabel(formatType(getStringValue("type"))), 1, row);
        }
        
        grid.add(createFieldLabel("Statut"), 2, row);
        if (editMode) {
            statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "AVAILABLE", "IN_USE", "MAINTENANCE", "OUT_OF_ORDER", "RENTED_OUT", "RESERVED"
            ));
            statusCombo.setValue(getStringValue("status"));
            statusCombo.setPrefWidth(150);
            grid.add(statusCombo, 3, row);
        } else {
            grid.add(createValueLabel(formatStatus(getStringValue("status"))), 3, row);
        }
        row++;
        
        // Carburant - Année
        grid.add(createFieldLabel("Carburant"), 0, row);
        if (editMode) {
            fuelTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "GASOLINE", "DIESEL", "ELECTRIC", "HYBRID", "GPL", "OTHER"
            ));
            fuelTypeCombo.setValue(getStringValue("fuelType"));
            fuelTypeCombo.setPrefWidth(150);
            grid.add(fuelTypeCombo, 1, row);
        } else {
            grid.add(createValueLabel(formatFuelType(getStringValue("fuelType"))), 1, row);
        }
        
        grid.add(createFieldLabel("Année"), 2, row);
        if (editMode) {
            yearField = new TextField(getIntValue("yearManufactured"));
            yearField.setPrefWidth(100);
            grid.add(yearField, 3, row);
        } else {
            grid.add(createValueLabel(getIntValue("yearManufactured")), 3, row);
        }
        row++;
        
        // Couleur - Propriétaire
        grid.add(createFieldLabel("Couleur"), 0, row);
        if (editMode) {
            colorField = new TextField(getStringValue("color"));
            colorField.setPrefWidth(150);
            grid.add(colorField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("color")), 1, row);
        }
        
        grid.add(createFieldLabel("Propriétaire"), 2, row);
        if (editMode) {
            ownerField = new TextField(getStringValue("owner"));
            ownerField.setPrefWidth(150);
            grid.add(ownerField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("owner")), 3, row);
        }
        row++;
        
        // VIN
        grid.add(createFieldLabel("N° VIN"), 0, row);
        if (editMode) {
            vinField = new TextField(getStringValue("vin"));
            vinField.setPrefWidth(300);
            grid.add(vinField, 1, row, 3, 1);
        } else {
            grid.add(createValueLabel(getStringValue("vin")), 1, row, 3, 1);
        }
        row++;
        
        // Photo
        grid.add(createFieldLabel("Photo"), 0, row);
        if (editMode) {
            HBox photoBox = new HBox(10);
            photoBox.setAlignment(Pos.CENTER_LEFT);
            
            // Label affichant le nom du fichier
            String currentPhoto = getStringValue("photoPath");
            selectedPhotoPath = (currentPhoto != null && !currentPhoto.isEmpty()) ? currentPhoto : null;
            photoPathLabel = new Label(currentPhoto != null && !currentPhoto.isEmpty() ? currentPhoto : "Aucune photo");
            photoPathLabel.setStyle(selectedPhotoPath != null ? "-fx-text-fill: #2c3e50;" : "-fx-text-fill: #6C757D; -fx-font-style: italic;");
            photoPathLabel.setMinWidth(150);
            
            // Bouton sélectionner
            Button selectPhotoBtn = new Button("📷 Choisir");
            selectPhotoBtn.setStyle("-fx-background-color: #6B71F2; -fx-text-fill: white; -fx-background-radius: 4;");
            selectPhotoBtn.setOnAction(e -> selectPhoto());
            
            // Bouton supprimer
            Button removePhotoBtn = new Button("❌");
            removePhotoBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 4;");
            removePhotoBtn.setOnAction(e -> removePhoto());
            
            // Aperçu de la photo si disponible
            photoPreview = new ImageView();
            photoPreview.setFitWidth(60);
            photoPreview.setFitHeight(40);
            photoPreview.setPreserveRatio(true);
            loadPhotoPreview(currentPhoto);
            
            photoBox.getChildren().addAll(photoPreview, photoPathLabel, selectPhotoBtn, removePhotoBtn);
            grid.add(photoBox, 1, row, 3, 1);
        } else {
            String photoPath = getStringValue("photoPath");
            HBox photoDisplay = new HBox(10);
            photoDisplay.setAlignment(Pos.CENTER_LEFT);
            
            if (photoPath != null && !photoPath.isEmpty()) {
                // Aperçu miniature
                Image photo = mediaService.loadVehiclePhoto(photoPath, 80, 60);
                if (photo != null) {
                    ImageView preview = new ImageView(photo);
                    preview.setFitWidth(60);
                    preview.setFitHeight(40);
                    preview.setPreserveRatio(true);
                    preview.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1);");
                    photoDisplay.getChildren().add(preview);
                }
                photoDisplay.getChildren().add(createValueLabel(photoPath));
            } else {
                photoDisplay.getChildren().add(createValueLabel("Aucune photo"));
            }
            grid.add(photoDisplay, 1, row, 3, 1);
        }
        
        return grid;
    }
    
    private GridPane createTechnicalSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Kilométrage - Charge utile
        grid.add(createFieldLabel("Kilométrage"), 0, row);
        if (editMode) {
            mileageField = new TextField(getIntValue("mileage"));
            mileageField.setPrefWidth(120);
            grid.add(mileageField, 1, row);
        } else {
            String mileage = getIntValue("mileage");
            grid.add(createValueLabel(mileage.isEmpty() ? "-" : mileage + " km"), 1, row);
        }
        
        grid.add(createFieldLabel("Charge utile"), 2, row);
        if (editMode) {
            maxPayloadField = new TextField(getDecimalValue("maxPayload"));
            maxPayloadField.setPrefWidth(120);
            grid.add(maxPayloadField, 3, row);
        } else {
            String payload = getDecimalValue("maxPayload");
            grid.add(createValueLabel(payload.isEmpty() ? "-" : payload + " kg"), 3, row);
        }
        row++;
        
        // Dimensions
        grid.add(createFieldLabel("Dimensions"), 0, row);
        if (editMode) {
            dimensionsField = new TextField(getStringValue("dimensions"));
            dimensionsField.setPrefWidth(300);
            grid.add(dimensionsField, 1, row, 3, 1);
        } else {
            grid.add(createValueLabel(getStringValue("dimensions")), 1, row, 3, 1);
        }
        row++;
        
        // Localisation - Conducteur assigné
        grid.add(createFieldLabel("Localisation"), 0, row);
        if (editMode) {
            currentLocationField = new TextField(getStringValue("currentLocation"));
            currentLocationField.setPrefWidth(200);
            grid.add(currentLocationField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("currentLocation")), 1, row);
        }
        
        grid.add(createFieldLabel("Conducteur"), 2, row);
        if (editMode) {
            assignedDriverField = new TextField(getStringValue("assignedDriver"));
            assignedDriverField.setPrefWidth(150);
            grid.add(assignedDriverField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("assignedDriver")), 3, row);
        }
        
        return grid;
    }
    
    private GridPane createMaintenanceSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Assurance
        grid.add(createFieldLabel("N° Assurance"), 0, row);
        if (editMode) {
            insuranceNumberField = new TextField(getStringValue("insuranceNumber"));
            insuranceNumberField.setPrefWidth(200);
            grid.add(insuranceNumberField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("insuranceNumber")), 1, row);
        }
        
        grid.add(createFieldLabel("Expiration assurance"), 2, row);
        if (editMode) {
            insuranceExpirationPicker = new DatePicker(parseDate(getStringValue("insuranceExpiration")));
            grid.add(insuranceExpirationPicker, 3, row);
        } else {
            grid.add(createValueLabel(formatDate(getStringValue("insuranceExpiration"))), 3, row);
        }
        row++;
        
        // Contrôle technique
        grid.add(createFieldLabel("Contrôle technique"), 0, row);
        if (editMode) {
            technicalControlPicker = new DatePicker(parseDate(getStringValue("technicalControlExpiration")));
            grid.add(technicalControlPicker, 1, row);
        } else {
            grid.add(createValueLabel(formatDate(getStringValue("technicalControlExpiration"))), 1, row);
        }
        
        grid.add(createFieldLabel("Intervalle maintenance"), 2, row);
        if (editMode) {
            maintenanceIntervalField = new TextField(getIntValue("maintenanceIntervalKm"));
            maintenanceIntervalField.setPrefWidth(100);
            grid.add(maintenanceIntervalField, 3, row);
        } else {
            String interval = getIntValue("maintenanceIntervalKm");
            grid.add(createValueLabel(interval.isEmpty() ? "-" : interval + " km"), 3, row);
        }
        row++;
        
        // Dernière / Prochaine maintenance
        grid.add(createFieldLabel("Dernière maintenance"), 0, row);
        if (editMode) {
            lastMaintenancePicker = new DatePicker(parseDate(getStringValue("lastMaintenanceDate")));
            grid.add(lastMaintenancePicker, 1, row);
        } else {
            grid.add(createValueLabel(formatDate(getStringValue("lastMaintenanceDate"))), 1, row);
        }
        
        grid.add(createFieldLabel("Prochaine maintenance"), 2, row);
        if (editMode) {
            nextMaintenancePicker = new DatePicker(parseDate(getStringValue("nextMaintenanceDate")));
            grid.add(nextMaintenancePicker, 3, row);
        } else {
            grid.add(createValueLabel(formatDate(getStringValue("nextMaintenanceDate"))), 3, row);
        }
        row++;
        
        // Notes
        grid.add(createFieldLabel("Notes"), 0, row);
        if (editMode) {
            notesArea = new TextArea(getStringValue("notes"));
            notesArea.setPrefRowCount(3);
            notesArea.setPrefWidth(500);
            grid.add(notesArea, 1, row, 3, 1);
        } else {
            Label notesLabel = createValueLabel(getStringValue("notes"));
            notesLabel.setWrapText(true);
            notesLabel.setMaxWidth(400);
            grid.add(notesLabel, 1, row, 3, 1);
        }
        
        return grid;
    }
    
    private GridPane createFinancialSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Date d'achat - Prix d'achat
        grid.add(createFieldLabel("Date d'achat"), 0, row);
        if (editMode) {
            purchaseDatePicker = new DatePicker(parseDate(getStringValue("purchaseDate")));
            grid.add(purchaseDatePicker, 1, row);
        } else {
            grid.add(createValueLabel(formatDate(getStringValue("purchaseDate"))), 1, row);
        }
        
        grid.add(createFieldLabel("Prix d'achat"), 2, row);
        if (editMode) {
            purchasePriceField = new TextField(getDecimalValue("purchasePrice"));
            purchasePriceField.setPrefWidth(120);
            grid.add(purchasePriceField, 3, row);
        } else {
            String price = getDecimalValue("purchasePrice");
            grid.add(createValueLabel(price.isEmpty() ? "-" : price + " €"), 3, row);
        }
        row++;
        
        // Tarif journalier
        grid.add(createFieldLabel("Tarif location/jour"), 0, row);
        if (editMode) {
            dailyRentalRateField = new TextField(getDecimalValue("dailyRentalRate"));
            dailyRentalRateField.setPrefWidth(120);
            grid.add(dailyRentalRateField, 1, row);
        } else {
            String rate = getDecimalValue("dailyRentalRate");
            grid.add(createValueLabel(rate.isEmpty() ? "-" : rate + " €/jour"), 1, row);
        }
        
        return grid;
    }
    
    // ========== Utilitaires ==========
    
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web("#7f8c8d"));
        label.setMinWidth(130);
        return label;
    }
    
    private Label createValueLabel(String value) {
        Label label = new Label(value != null && !value.isEmpty() ? value : "-");
        label.setFont(Font.font("Segoe UI", 12));
        label.setTextFill(Color.web("#2c3e50"));
        label.setWrapText(true);
        return label;
    }
    
    private String getStringValue(String key) {
        Object value = vehicleData.get(key);
        return value != null ? value.toString() : "";
    }
    
    private String getIntValue(String key) {
        Object value = vehicleData.get(key);
        if (value instanceof Number) {
            return String.valueOf(((Number) value).intValue());
        }
        return "";
    }
    
    private String getDecimalValue(String key) {
        Object value = vehicleData.get(key);
        if (value instanceof Number) {
            double d = ((Number) value).doubleValue();
            return d == (int) d ? String.valueOf((int) d) : String.format("%.2f", d);
        }
        return "";
    }
    
    private String formatType(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "VL": return "VL";
            case "VL_17M3": return "VL 17 m³";
            case "VL_20M3": return "VL 20 m³";
            case "VAN": return "Fourgon";
            case "PORTEUR": return "Porteur";
            case "TRACTEUR": return "Tracteur";
            case "SEMI_REMORQUE": return "Semi-remorque";
            case "SCENE_MOBILE": return "Scène Mobile";
            case "TRUCK": return "Camion";
            case "TRAILER": return "Remorque";
            case "CAR": return "Voiture";
            case "MOTORCYCLE": return "Moto";
            default: return type;
        }
    }
    
    /**
     * Convertit une valeur d'affichage en valeur d'enum pour l'API
     */
    private String unformatType(String displayType) {
        if (displayType == null) return null;
        switch (displayType) {
            case "VL": return "VL";
            case "VL 17 m³": return "VL_17M3";
            case "VL 20 m³": return "VL_20M3";
            case "Fourgon": return "VAN";
            case "Porteur": return "PORTEUR";
            case "Tracteur": return "TRACTEUR";
            case "Semi-remorque": return "SEMI_REMORQUE";
            case "Scène Mobile": return "SCENE_MOBILE";
            case "Camion": return "TRUCK";
            case "Remorque": return "TRAILER";
            case "Voiture": return "CAR";
            case "Moto": return "MOTORCYCLE";
            case "Autre": return "OTHER";
            default: return displayType;
        }
    }
    
    private String formatFuelType(String fuelType) {
        if (fuelType == null) return "";
        switch (fuelType.toUpperCase()) {
            case "GASOLINE": return "Essence";
            case "DIESEL": return "Diesel";
            case "ELECTRIC": return "Électrique";
            case "HYBRID": return "Hybride";
            case "GPL": return "GPL";
            default: return fuelType;
        }
    }
    
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String formatDate(String dateStr) {
        LocalDate date = parseDate(dateStr);
        if (date == null) return "-";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
