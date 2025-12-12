package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog de visualisation/édition d'un équipement
 * Photo et logo dans le header, édition sur place
 */
public class EquipmentDetailDialog extends Dialog<Map<String, Object>> {
    
    private final ApiService apiService;
    private final MediaService mediaService;
    private final Map<String, Object> equipmentData;
    
    private boolean editMode = false;
    private Button editSaveButton;
    private MenuButton applyButton;
    private VBox contentBox;
    
    // ButtonTypes pour les boutons du bas
    private ButtonType editButtonType;
    private ButtonType closeButtonType;
    
    // Champs éditables
    private TextField nameField, brandField, modelField, serialField, locmatField;
    private TextField locationField, supplierField, weightField, priceField;
    private ComboBox<String> categoryCombo, statusCombo, ownerCombo;
    private TextArea notesArea;
    
    public EquipmentDetailDialog(ApiService apiService, Map<String, Object> equipment) {
        this.apiService = apiService;
        this.mediaService = new MediaService();
        this.equipmentData = new HashMap<>(equipment);
        
        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Équipement");
        setHeaderText(null);
        
        // Bouton Modifier/Enregistrer en bas du dialog
        editButtonType = new ButtonType("✏️ Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer sans sauvegarder", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        getDialogPane().setPrefSize(750, 650);
        getDialogPane().setMinWidth(700);
        getDialogPane().setMinHeight(600);
        
        getDialogPane().setStyle(
            "-fx-background-color: #f5f6fa;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;"
        );
        
        // Styliser et configurer le bouton Modifier
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        setResultConverter(buttonType -> editMode ? equipmentData : null);
    }
    
    private boolean editButtonInitialized = false;
    
    private void setupEditButton() {
        Button editButton = (Button) getDialogPane().lookupButton(editButtonType);
        if (editButton != null) {
            editSaveButton = editButton;
            updateEditButtonStyle(editButton);
            
            // Ajouter l'EventFilter UNE SEULE FOIS
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
        String baseButtonStyle = "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;";
        String buttonStyle = editMode 
            ? baseButtonStyle + "-fx-background-color: #27ae60; -fx-text-fill: white;"
            : baseButtonStyle + "-fx-background-color: #3498db; -fx-text-fill: white;";
        
        editButton.setText(editMode ? "💾 Enregistrer" : "✏️ Modifier");
        editButton.setStyle(buttonStyle);
        
        final String finalButtonStyle = buttonStyle;
        editButton.setOnMouseEntered(e -> editButton.setStyle(
            finalButtonStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
        ));
        editButton.setOnMouseExited(e -> editButton.setStyle(finalButtonStyle));
    }
    
    private void setupCloseButton() {
        Button closeButton = (Button) getDialogPane().lookupButton(closeButtonType);
        if (closeButton != null) {
            String closeButtonStyle = "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px; -fx-background-color: #95a5a6; -fx-text-fill: white;";
            closeButton.setStyle(closeButtonStyle);
            
            closeButton.setOnMouseEntered(e -> closeButton.setStyle(
                closeButtonStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            ));
            closeButton.setOnMouseExited(e -> closeButton.setStyle(closeButtonStyle));
        }
    }
    
    private void createContent() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f6fa;");
        
        // Header avec photo et logo
        mainLayout.setTop(createHeader());
        
        // Contenu scrollable
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: #f5f6fa;");
        
        rebuildContent();
        
        scrollPane.setContent(contentBox);
        mainLayout.setCenter(scrollPane);
        
        getDialogPane().setContent(mainLayout);
    }
    
    private void rebuildContent() {
        contentBox.getChildren().clear();
        contentBox.getChildren().addAll(
            createSection("📋 Informations générales", createGeneralSection()),
            createSection("🔧 Technique", createTechnicalSection()),
            createSection("🛠 Maintenance", createMaintenanceSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: linear-gradient(to right, #2c3e50, #3498db);");
        
        // Photo de l'équipement (à gauche)
        ImageView photoView = new ImageView();
        photoView.setFitWidth(80);
        photoView.setFitHeight(80);
        photoView.setPreserveRatio(true);
        photoView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 8, 0, 0, 2);");
        
        String photoPath = getStringValue("photoPath");
        System.out.println("📷 createHeader - photoPath: " + photoPath);
        if (photoPath != null && !photoPath.isEmpty()) {
            Image photo = mediaService.loadEquipmentPhoto(photoPath, 80, 80);
            System.out.println("📷 createHeader - Image chargée: " + (photo != null));
            if (photo != null) {
                photoView.setImage(photo);
            }
        }
        
        // Placeholder si pas de photo
        StackPane photoContainer = new StackPane();
        photoContainer.setMinSize(80, 80);
        photoContainer.setMaxSize(80, 80);
        photoContainer.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 8;" + 
            (editMode ? " -fx-cursor: hand;" : ""));
        
        if (photoView.getImage() != null) {
            photoContainer.getChildren().add(photoView);
        } else {
            Label photoPlaceholder = new Label("📷");
            photoPlaceholder.setFont(Font.font(30));
            photoPlaceholder.setTextFill(Color.web("#ecf0f1"));
            photoContainer.getChildren().add(photoPlaceholder);
        }
        
        // En mode édition, ajouter un indicateur et rendre cliquable
        if (editMode) {
            Label editIndicator = new Label("✏️");
            editIndicator.setFont(Font.font(14));
            editIndicator.setStyle("-fx-background-color: #3498db; -fx-background-radius: 10; -fx-padding: 2 5;");
            StackPane.setAlignment(editIndicator, Pos.BOTTOM_RIGHT);
            photoContainer.getChildren().add(editIndicator);
            
            // Tooltip
            Tooltip.install(photoContainer, new Tooltip("Cliquer pour changer la photo"));
            
            // Ouvrir la galerie au clic
            photoContainer.setOnMouseClicked(e -> openPhotoGallery());
        }
        
        // Infos centrales
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String name = getStringValue("name");
        Label nameLabel = new Label(name != null ? name : "Sans nom");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);
        
        String brand = getStringValue("brand");
        String category = getStringValue("category");
        Label subtitleLabel = new Label(
            (brand != null ? brand : "") + 
            (brand != null && category != null ? " • " : "") + 
            (category != null ? category : "")
        );
        subtitleLabel.setFont(Font.font("Segoe UI", 13));
        subtitleLabel.setTextFill(Color.web("#bdc3c7"));
        
        String uid = getStringValue("qrCode");
        if (uid != null && !uid.isEmpty()) {
            Label uidLabel = new Label("UID: " + uid);
            uidLabel.setFont(Font.font("Consolas", 11));
            uidLabel.setTextFill(Color.web("#95a5a6"));
            infoBox.getChildren().addAll(nameLabel, subtitleLabel, uidLabel);
        } else {
            infoBox.getChildren().addAll(nameLabel, subtitleLabel);
        }
        
        // Zone droite avec logo et boutons
        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Logo de la marque
        if (brand != null && !brand.isEmpty()) {
            Image logo = mediaService.getBrandLogo(brand, 80, 40);
            if (logo != null) {
                ImageView logoView = new ImageView(logo);
                logoView.setFitHeight(35);
                logoView.setPreserveRatio(true);
                rightBox.getChildren().add(logoView);
            }
        }
        
        // Bouton Appliquer avec options (visible uniquement en mode édition)
        if (editMode) {
            HBox buttonBar = new HBox(8);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);
            
            String baseButtonStyle = "-fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;";
            String primaryButtonStyle = baseButtonStyle + "-fx-background-color: #3498db; -fx-text-fill: white;";
            
            applyButton = new MenuButton("📥 Appliquer les modifications");
            applyButton.setStyle(primaryButtonStyle);
            
            // Effet hover pour le MenuButton
            applyButton.setOnMouseEntered(e -> applyButton.setStyle(
                primaryButtonStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 6, 0, 0, 2);"
            ));
            applyButton.setOnMouseExited(e -> applyButton.setStyle(primaryButtonStyle));
            
            MenuItem applyThis = new MenuItem("📌 Cet équipement uniquement");
            applyThis.setOnAction(e -> applyToThisEquipment());
            
            MenuItem applySameName = new MenuItem("📋 Tous les équipements de même description");
            applySameName.setOnAction(e -> applyToSameName());
            
            MenuItem applySameLocmat = new MenuItem("🏷️ Tous les équipements de même code LocMat");
            applySameLocmat.setOnAction(e -> applyToSameLocmat());
            
            applyButton.getItems().addAll(applyThis, applySameName, applySameLocmat);
            buttonBar.getChildren().add(applyButton);
            rightBox.getChildren().add(buttonBar);
        }
        
        header.getChildren().addAll(photoContainer, infoBox, rightBox);
        return header;
    }
    
    private void toggleEditMode() {
        if (editMode) {
            saveChanges();
            // Sauvegarder automatiquement via l'API
            saveToApi();
            editMode = false;
        } else {
            editMode = true;
        }
        
        setupEditButton();
        getDialogPane().setContent(null);
        createContent();
    }
    
    /**
     * Sauvegarde les modifications via l'API backend
     */
    private void saveToApi() {
        try {
            Object id = equipmentData.get("id");
            if (id != null) {
                apiService.updateEquipment(Long.parseLong(id.toString()), equipmentData).thenAccept(result -> {
                    javafx.application.Platform.runLater(() -> {
                        System.out.println("✅ Équipement sauvegardé avec succès (ID: " + id + ")");
                    });
                }).exceptionally(e -> {
                    javafx.application.Platform.runLater(() -> {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur de sauvegarde");
                        error.setHeaderText(null);
                        error.setContentText("Erreur lors de la sauvegarde: " + e.getMessage());
                        error.showAndWait();
                    });
                    return null;
                });
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur sauvegarde API: " + e.getMessage());
        }
    }
    
    private void saveChanges() {
        if (nameField != null) equipmentData.put("name", nameField.getText());
        if (brandField != null) equipmentData.put("brand", brandField.getText());
        if (modelField != null) equipmentData.put("model", modelField.getText());
        if (serialField != null) equipmentData.put("serialNumber", serialField.getText());
        // Code LocMat -> internalReference pour l'API
        if (locmatField != null) {
            equipmentData.put("internalReference", locmatField.getText());
            equipmentData.put("locmatCode", locmatField.getText()); // Garder aussi pour compatibilité
        }
        if (locationField != null) equipmentData.put("location", locationField.getText());
        if (supplierField != null) equipmentData.put("supplier", supplierField.getText());
        // Poids en Double
        if (weightField != null) {
            try {
                String w = weightField.getText().replace(",", ".").replace("kg", "").trim();
                if (!w.isEmpty()) equipmentData.put("weight", Double.parseDouble(w));
            } catch (NumberFormatException ignored) {}
        }
        if (priceField != null) {
            try {
                String p = priceField.getText().replace(",", ".").replace("€", "").trim();
                if (!p.isEmpty()) equipmentData.put("purchasePrice", Double.parseDouble(p));
            } catch (NumberFormatException ignored) {}
        }
        if (categoryCombo != null && categoryCombo.getValue() != null) {
            equipmentData.put("category", categoryCombo.getValue());
        }
        // Mapper le statut displayName vers la valeur enum backend
        if (statusCombo != null && statusCombo.getValue() != null) {
            String displayStatus = statusCombo.getValue();
            String apiStatus = mapDisplayToApiStatus(displayStatus);
            equipmentData.put("status", apiStatus);
        }
        if (ownerCombo != null && ownerCombo.getValue() != null) {
            equipmentData.put("owner", ownerCombo.getValue());
        }
        if (notesArea != null) equipmentData.put("notes", notesArea.getText());
        // photoPath est déjà mis à jour dans openPhotoGallery()
    }
    
    /**
     * Applique les modifications uniquement à cet équipement
     */
    private void applyToThisEquipment() {
        saveChanges();
        
        try {
            Object id = equipmentData.get("id");
            if (id != null) {
                apiService.updateEquipment(Long.parseLong(id.toString()), equipmentData).thenAccept(result -> {
                    javafx.application.Platform.runLater(() -> {
                        Alert info = new Alert(Alert.AlertType.INFORMATION);
                        info.setTitle("Sauvegarde réussie");
                        info.setHeaderText(null);
                        info.setContentText("Les modifications ont été enregistrées pour cet équipement.");
                        info.showAndWait();
                    });
                }).exceptionally(e -> {
                    javafx.application.Platform.runLater(() -> {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText(null);
                        error.setContentText("Erreur lors de la sauvegarde: " + e.getMessage());
                        error.showAndWait();
                    });
                    return null;
                });
            }
        } catch (Exception e) {
            System.err.println("Erreur sauvegarde: " + e.getMessage());
        }
    }
    
    /**
     * Applique les modifications à tous les équipements de même nom
     */
    private void applyToSameName() {
        saveChanges();
        
        String name = getStringValue("name");
        if (name == null || name.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Nom manquant");
            warning.setHeaderText(null);
            warning.setContentText("L'équipement n'a pas de nom défini.");
            warning.showAndWait();
            return;
        }
        
        // Récupérer les équipements avec le même nom
        List<Map<String, Object>> sameNameEquipments = apiService.getEquipmentsByName(name);
        
        if (sameNameEquipments.isEmpty()) {
            applyToThisEquipment();
            return;
        }
        
        // Confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Appliquer à tous");
        confirmation.setHeaderText("Appliquer ces modifications à " + sameNameEquipments.size() + " équipement(s) ?");
        confirmation.setContentText(
            "Les modifications seront appliquées à tous les équipements portant le nom \"" + name + "\".\n\n" +
            "Cette action est irréversible."
        );
        
        ButtonType yesButton = new ButtonType("Oui, appliquer à tous", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Annuler", ButtonBar.ButtonData.NO);
        confirmation.getButtonTypes().setAll(yesButton, noButton);
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                int updated = 0;
                for (Map<String, Object> equipment : sameNameEquipments) {
                    try {
                        Object id = equipment.get("id");
                        if (id != null) {
                            apiService.updateEquipment(Long.parseLong(id.toString()), equipmentData);
                            updated++;
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur mise à jour: " + e.getMessage());
                    }
                }
                
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Modifications appliquées");
                info.setHeaderText(null);
                info.setContentText("Modifications appliquées à " + updated + " équipement(s).");
                info.showAndWait();
            }
        });
    }
    
    /**
     * Applique les modifications à tous les équipements de même code LocMat
     */
    private void applyToSameLocmat() {
        saveChanges();
        
        String locmatCode = getStringValue("locmatCode");
        if (locmatCode == null || locmatCode.isEmpty()) {
            locmatCode = getStringValue("internalReference");
        }
        
        if (locmatCode == null || locmatCode.isEmpty()) {
            Alert warning = new Alert(Alert.AlertType.WARNING);
            warning.setTitle("Code LocMat manquant");
            warning.setHeaderText(null);
            warning.setContentText("L'équipement n'a pas de code LocMat défini.");
            warning.showAndWait();
            return;
        }
        
        // Récupérer les équipements avec le même code LocMat
        List<Map<String, Object>> sameLocmatEquipments = apiService.getEquipmentsByLocmatCode(locmatCode);
        
        if (sameLocmatEquipments.isEmpty()) {
            applyToThisEquipment();
            return;
        }
        
        // Confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Appliquer à tous");
        confirmation.setHeaderText("Appliquer ces modifications à " + sameLocmatEquipments.size() + " équipement(s) ?");
        confirmation.setContentText(
            "Les modifications seront appliquées à tous les équipements avec le code LocMat \"" + locmatCode + "\".\n\n" +
            "Cette action est irréversible."
        );
        
        ButtonType yesButton = new ButtonType("Oui, appliquer à tous", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Annuler", ButtonBar.ButtonData.NO);
        confirmation.getButtonTypes().setAll(yesButton, noButton);
        
        final String finalLocmatCode = locmatCode;
        confirmation.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                int updated = 0;
                for (Map<String, Object> equipment : sameLocmatEquipments) {
                    try {
                        Object id = equipment.get("id");
                        if (id != null) {
                            apiService.updateEquipment(Long.parseLong(id.toString()), equipmentData);
                            updated++;
                        }
                    } catch (Exception e) {
                        System.err.println("Erreur mise à jour: " + e.getMessage());
                    }
                }
                
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Modifications appliquées");
                info.setHeaderText(null);
                info.setContentText("Modifications appliquées à " + updated + " équipement(s) avec le code LocMat \"" + finalLocmatCode + "\".");
                info.showAndWait();
            }
        });
    }
    
    /**
     * Ouvre la galerie de photos pour sélectionner une nouvelle photo
     */
    private void openPhotoGallery() {
        String equipName = getStringValue("name");
        String locmatCode = getStringValue("internalReference");
        if (locmatCode == null || locmatCode.isEmpty()) {
            locmatCode = getStringValue("locmatCode");
        }
        
        MediaGalleryDialog galleryDialog = new MediaGalleryDialog(
            mediaService, 
            MediaGalleryDialog.MediaType.PHOTO, 
            equipName, 
            locmatCode
        );
        
        galleryDialog.showAndWait().ifPresent(selection -> {
            // Mettre à jour le chemin de la photo
            if (selection.getSelectedFile() != null) {
                String photoFileName = selection.getSelectedFile().getName();
                System.out.println("📷 Photo sélectionnée: " + photoFileName);
                System.out.println("📷 Fichier complet: " + selection.getSelectedFile().getAbsolutePath());
                equipmentData.put("photoPath", photoFileName);
                System.out.println("📷 photoPath dans equipmentData: " + equipmentData.get("photoPath"));
                
                // Note: La sauvegarde se fera via le bouton "Appliquer" qui offre 3 options:
                // - Appliquer à cet équipement uniquement
                // - Appliquer à tous les équipements de même nom
                // - Appliquer à tous les équipements de même code LocMat
                
                // Rafraîchir l'interface pour afficher la nouvelle photo
                getDialogPane().setContent(null);
                createContent();
            }
        });
    }
    
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
        
        // Description (anciennement Nom)
        nameField = createTextField("name");
        row = addField(grid, row, "Description", nameField, getStringValue("name"));
        
        // Marque
        brandField = createTextField("brand");
        row = addField(grid, row, "Marque", brandField, getStringValue("brand"));
        
        // Modèle
        modelField = createTextField("model");
        row = addField(grid, row, "Modèle", modelField, getStringValue("model"));
        
        // Catégorie
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Audio", "Éclairage", "Vidéo", "Structure", "Câblage", "Accessoires", "Autres");
        categoryCombo.setValue(getStringValue("category"));
        categoryCombo.setDisable(!editMode);
        categoryCombo.setStyle(editMode ? "-fx-opacity: 1;" : "-fx-opacity: 0.9;");
        row = addFieldWithNode(grid, row, "Catégorie", categoryCombo, getStringValue("category"));
        
        // Statut - Utiliser les vraies valeurs de l'API backend
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Disponible", "En cours d'utilisation", "En maintenance", "Hors service", "En SAV", "Retiré du service");
        statusCombo.setValue(getStringValue("status"));
        statusCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Statut", statusCombo, getStringValue("status"));
        
        // Propriétaire
        ownerCombo = new ComboBox<>();
        ownerCombo.getItems().addAll("MAG SCENE", "Location externe", "Client");
        ownerCombo.setValue(getStringValue("owner"));
        ownerCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Propriétaire", ownerCombo, getStringValue("owner"));
        
        // Emplacement
        locationField = createTextField("location");
        row = addField(grid, row, "Emplacement", locationField, getStringValue("location"));
        
        // Fournisseur
        supplierField = createTextField("supplier");
        addField(grid, row, "Fournisseur", supplierField, getStringValue("supplier"));
        
        return grid;
    }
    
    private GridPane createTechnicalSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // N° Série
        serialField = createTextField("serialNumber");
        row = addField(grid, row, "N° Série", serialField, getStringValue("serialNumber"));
        
        // Code LocMat (internalReference dans l'API)
        locmatField = createTextField("internalReference");
        String locmatValue = getStringValue("internalReference");
        if (locmatValue == null || locmatValue.isEmpty()) {
            locmatValue = getStringValue("locmatCode");
        }
        row = addField(grid, row, "Code LocMat", locmatField, locmatValue);
        
        // Poids
        weightField = createTextField("weight");
        row = addField(grid, row, "Poids (kg)", weightField, getStringValue("weight"));
        
        // Prix d'achat
        priceField = createTextField("purchasePrice");
        row = addField(grid, row, "Prix d'achat", priceField, formatPrice(equipmentData.get("purchasePrice")));
        
        // Date d'achat (lecture seule)
        row = addReadOnlyField(grid, row, "Date d'achat", formatDate(getStringValue("purchaseDate")));
        
        // Garantie
        addReadOnlyField(grid, row, "Garantie jusqu'au", formatDate(getStringValue("warrantyEndDate")));
        
        return grid;
    }
    
    private GridPane createMaintenanceSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Dernière maintenance
        row = addReadOnlyField(grid, row, "Dernière maintenance", formatDate(getStringValue("lastMaintenanceDate")));
        
        // Prochaine maintenance
        row = addReadOnlyField(grid, row, "Prochaine maintenance", formatDate(getStringValue("nextMaintenanceDate")));
        
        // Notes
        Label notesLabel = new Label("Notes :");
        notesLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        notesLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(notesLabel, 0, row);
        
        notesArea = new TextArea(getStringValue("notes"));
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(3);
        notesArea.setEditable(editMode);
        notesArea.setStyle(editMode ? 
            "-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 2;" :
            "-fx-background-color: #f8f9fa; -fx-border-color: #ddd;"
        );
        grid.add(notesArea, 1, row);
        GridPane.setColumnSpan(notesArea, 2);
        
        return grid;
    }
    
    private TextField createTextField(String key) {
        TextField field = new TextField();
        field.setEditable(editMode);
        field.setStyle(editMode ? 
            "-fx-background-color: white; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 4;" :
            "-fx-background-color: transparent; -fx-border-color: transparent;"
        );
        return field;
    }
    
    private int addField(GridPane grid, int row, String label, TextField field, String value) {
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#7f8c8d"));
        labelNode.setMinWidth(130);
        
        if (editMode) {
            field.setText(value != null ? value : "");
            grid.add(labelNode, 0, row);
            grid.add(field, 1, row);
            GridPane.setHgrow(field, Priority.ALWAYS);
        } else {
            Label valueNode = new Label(value != null && !value.isEmpty() ? value : "—");
            valueNode.setFont(Font.font("Segoe UI", 12));
            valueNode.setTextFill(Color.web("#2c3e50"));
            grid.add(labelNode, 0, row);
            grid.add(valueNode, 1, row);
        }
        
        return row + 1;
    }
    
    private int addFieldWithNode(GridPane grid, int row, String label, Control control, String displayValue) {
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#7f8c8d"));
        labelNode.setMinWidth(130);
        
        if (editMode) {
            grid.add(labelNode, 0, row);
            grid.add(control, 1, row);
            GridPane.setHgrow(control, Priority.ALWAYS);
        } else {
            Label valueNode = new Label(displayValue != null && !displayValue.isEmpty() ? displayValue : "—");
            valueNode.setFont(Font.font("Segoe UI", 12));
            valueNode.setTextFill(Color.web("#2c3e50"));
            grid.add(labelNode, 0, row);
            grid.add(valueNode, 1, row);
        }
        
        return row + 1;
    }
    
    private int addReadOnlyField(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label + " :");
        labelNode.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        labelNode.setTextFill(Color.web("#7f8c8d"));
        labelNode.setMinWidth(130);
        
        Label valueNode = new Label(value != null && !value.isEmpty() ? value : "—");
        valueNode.setFont(Font.font("Segoe UI", 12));
        valueNode.setTextFill(Color.web("#2c3e50"));
        
        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
        
        return row + 1;
    }
    
    private String getStringValue(String key) {
        Object value = equipmentData.get(key);
        return value != null ? value.toString() : null;
    }
    
    private String formatPrice(Object price) {
        if (price == null) return "—";
        try {
            double value = Double.parseDouble(price.toString());
            return String.format("%.2f €", value);
        } catch (NumberFormatException e) {
            return price.toString();
        }
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "—";
        try {
            if (dateStr.length() >= 10) {
                LocalDate date = LocalDate.parse(dateStr.substring(0, 10));
                return date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear();
            }
        } catch (Exception e) {
            // Ignore
        }
        return dateStr;
    }
    
    /**
     * Mappe le statut displayName vers la valeur d'enum API backend
     */
    private String mapDisplayToApiStatus(String displayStatus) {
        if (displayStatus == null) return "AVAILABLE";
        return switch (displayStatus) {
            case "Disponible" -> "AVAILABLE";
            case "En cours d'utilisation" -> "IN_USE";
            case "En maintenance" -> "MAINTENANCE";
            case "Hors service" -> "OUT_OF_ORDER";
            case "En SAV" -> "IN_SAV";
            case "Retiré du service" -> "RETIRED";
            default -> "AVAILABLE";
        };
    }
    
    private String mapStatusToDisplay(String status) {
        if (status == null) return "—";
        return switch (status) {
            case "AVAILABLE", "Disponible" -> "Disponible";
            case "IN_USE", "En cours d'utilisation" -> "En cours d'utilisation";
            case "MAINTENANCE", "En maintenance" -> "En maintenance";
            case "OUT_OF_ORDER", "Hors service" -> "Hors service";
            case "IN_SAV", "En SAV" -> "En SAV";
            case "RETIRED", "Retiré du service" -> "Retiré du service";
            default -> status;
        };
    }
}
