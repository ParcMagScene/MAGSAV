package com.magscene.magsav.desktop.dialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.view.equipment.EquipmentItem;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;

/**
 * Dialogue pour créer une nouvelle demande de SAV
 * Permet de sélectionner un équipement existant ou de demander un nouvel équipement
 */
public class NewSAVRequestDialog extends Dialog<Map<String, Object>> {

    private final ApiService apiService;
    
    // Mode de sélection
    private ToggleGroup equipmentModeGroup;
    private RadioButton existingEquipmentRadio;
    private RadioButton newEquipmentRadio;
    
    // Sélection équipement existant
    private TextField searchField;
    private TableView<EquipmentItem> equipmentTable;
    private ObservableList<EquipmentItem> equipmentData;
    private EquipmentItem selectedEquipment;
    
    // Panneau de prévisualisation de l'équipement sélectionné
    private VBox selectedEquipmentPreview;
    private ImageView selectedPhotoView;
    private ImageView selectedLogoView;
    private Label selectedNameLabel;
    private Label selectedInfoLabel;
    
    // Demande de nouvel équipement
    private VBox newEquipmentPane;
    private TextField newEquipmentName;
    private TextField newEquipmentBrand;
    private TextField newEquipmentCategory;
    private TextArea newEquipmentDescription;
    
    // Description de la panne
    private TextField titleField;
    private TextArea faultDescriptionArea;
    private ComboBox<String> priorityCombo;
    
    // Informations demandeur
    private TextField requesterNameField;
    private TextField requesterEmailField;
    
    // Boutons
    private Button submitButton;

    public NewSAVRequestDialog(ApiService apiService) {
        this.apiService = apiService;
        
        setTitle("Nouvelle demande de SAV");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        
        // Taille du dialogue
        getDialogPane().setPrefWidth(900);
        getDialogPane().setPrefHeight(700);
        
        createContent();
        setupButtons();
        loadEquipments();
    }
    
    private void createContent() {
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f5f6fa;");
        
        // En-tête
        mainContainer.getChildren().add(createHeader());
        
        // Section choix du mode équipement
        mainContainer.getChildren().add(createEquipmentModeSection());
        
        // Section équipement existant (recherche + table + preview)
        mainContainer.getChildren().add(createExistingEquipmentSection());
        
        // Section nouvel équipement (masquée par défaut)
        newEquipmentPane = createNewEquipmentSection();
        newEquipmentPane.setVisible(false);
        newEquipmentPane.setManaged(false);
        mainContainer.getChildren().add(newEquipmentPane);
        
        // Section description de la panne
        mainContainer.getChildren().add(createFaultSection());
        
        // Section informations demandeur
        mainContainer.getChildren().add(createRequesterSection());
        
        // ScrollPane pour le contenu
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f6fa; -fx-background-color: #f5f6fa;");
        
        getDialogPane().setContent(scrollPane);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        Label icon = new Label("🔧");
        icon.setFont(Font.font("System", FontWeight.BOLD, 32));
        
        VBox titleBox = new VBox(2);
        Label title = new Label("Nouvelle demande de SAV");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2c3e50"));
        
        Label subtitle = new Label("Sélectionnez un équipement et décrivez la panne constatée");
        subtitle.setFont(Font.font("Segoe UI", 13));
        subtitle.setTextFill(Color.web("#7f8c8d"));
        
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().addAll(icon, titleBox);
        
        return header;
    }
    
    private VBox createEquipmentModeSection() {
        VBox section = createSection("📦 Type d'équipement");
        
        equipmentModeGroup = new ToggleGroup();
        
        existingEquipmentRadio = new RadioButton("Équipement existant dans l'inventaire");
        existingEquipmentRadio.setToggleGroup(equipmentModeGroup);
        existingEquipmentRadio.setSelected(true);
        existingEquipmentRadio.setFont(Font.font("Segoe UI", 13));
        
        newEquipmentRadio = new RadioButton("Demander l'ajout d'un nouvel équipement (à valider par l'administrateur)");
        newEquipmentRadio.setToggleGroup(equipmentModeGroup);
        newEquipmentRadio.setFont(Font.font("Segoe UI", 13));
        
        // Listener pour basculer entre les modes
        equipmentModeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isNewEquipment = newVal == newEquipmentRadio;
            
            // Masquer/afficher les sections appropriées
            searchField.getParent().getParent().setVisible(!isNewEquipment);
            searchField.getParent().getParent().setManaged(!isNewEquipment);
            selectedEquipmentPreview.getParent().setVisible(!isNewEquipment);
            selectedEquipmentPreview.getParent().setManaged(!isNewEquipment);
            
            newEquipmentPane.setVisible(isNewEquipment);
            newEquipmentPane.setManaged(isNewEquipment);
            
            validateForm();
        });
        
        VBox radioBox = new VBox(8);
        radioBox.getChildren().addAll(existingEquipmentRadio, newEquipmentRadio);
        
        section.getChildren().add(radioBox);
        return section;
    }
    
    private VBox createExistingEquipmentSection() {
        VBox section = createSection("🔍 Sélection de l'équipement");
        
        // HBox contenant la recherche + table à gauche et la preview à droite
        HBox contentBox = new HBox(15);
        
        // Partie gauche : recherche + table
        VBox leftPane = new VBox(10);
        leftPane.setPrefWidth(500);
        
        // Champ de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchField = new TextField();
        searchField.setPromptText("Rechercher par nom, code LOCMAT, marque...");
        searchField.setPrefWidth(400);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterEquipments(newVal));
        
        Button clearBtn = new Button("✕");
        clearBtn.setOnAction(e -> searchField.clear());
        
        searchBox.getChildren().addAll(searchField, clearBtn);
        
        // Table des équipements
        equipmentTable = new TableView<>();
        equipmentData = FXCollections.observableArrayList();
        equipmentTable.setItems(equipmentData);
        equipmentTable.setPrefHeight(200);
        equipmentTable.setPlaceholder(new Label("Chargement des équipements..."));
        
        // Colonnes
        TableColumn<EquipmentItem, String> locmatCol = new TableColumn<>("Code LOCMAT");
        locmatCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocmatCode()));
        locmatCol.setPrefWidth(100);
        
        TableColumn<EquipmentItem, String> uidCol = new TableColumn<>("UID");
        uidCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQrCode()));
        uidCol.setPrefWidth(80);
        
        TableColumn<EquipmentItem, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(180);
        
        TableColumn<EquipmentItem, String> serialCol = new TableColumn<>("N° Série");
        serialCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSerialNumber()));
        serialCol.setPrefWidth(100);
        
        TableColumn<EquipmentItem, String> brandCol = new TableColumn<>("Marque");
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        brandCol.setPrefWidth(80);
        equipmentTable.getColumns().add(locmatCol);
        equipmentTable.getColumns().add(uidCol);
        equipmentTable.getColumns().add(nameCol);
        equipmentTable.getColumns().add(serialCol);
        equipmentTable.getColumns().add(brandCol);
        
        // Sélection
        equipmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedEquipment = newVal;
            updateSelectedEquipmentPreview();
            validateForm();
        });
        
        leftPane.getChildren().addAll(searchBox, equipmentTable);
        
        // Partie droite : prévisualisation
        selectedEquipmentPreview = new VBox(10);
        selectedEquipmentPreview.setPrefWidth(300);
        selectedEquipmentPreview.setPadding(new Insets(15));
        selectedEquipmentPreview.setAlignment(Pos.TOP_CENTER);
        selectedEquipmentPreview.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-radius: 8;"
        );
        
        Label previewTitle = new Label("Équipement sélectionné");
        previewTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        previewTitle.setTextFill(Color.web("#7f8c8d"));
        
        // Photo
        selectedPhotoView = new ImageView();
        selectedPhotoView.setFitWidth(150);
        selectedPhotoView.setFitHeight(150);
        selectedPhotoView.setPreserveRatio(true);
        
        StackPane photoContainer = new StackPane(selectedPhotoView);
        photoContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        photoContainer.setPrefSize(160, 160);
        
        // Logo marque
        selectedLogoView = new ImageView();
        selectedLogoView.setFitWidth(80);
        selectedLogoView.setFitHeight(50);
        selectedLogoView.setPreserveRatio(true);
        
        // Infos
        selectedNameLabel = new Label("Aucun équipement sélectionné");
        selectedNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        selectedNameLabel.setTextFill(Color.web("#2c3e50"));
        selectedNameLabel.setWrapText(true);
        selectedNameLabel.setAlignment(Pos.CENTER);
        
        selectedInfoLabel = new Label("");
        selectedInfoLabel.setFont(Font.font("Segoe UI", 12));
        selectedInfoLabel.setTextFill(Color.web("#7f8c8d"));
        selectedInfoLabel.setWrapText(true);
        selectedInfoLabel.setAlignment(Pos.CENTER);
        
        selectedEquipmentPreview.getChildren().addAll(
            previewTitle, photoContainer, selectedLogoView, selectedNameLabel, selectedInfoLabel
        );
        
        contentBox.getChildren().addAll(leftPane, selectedEquipmentPreview);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        
        section.getChildren().add(contentBox);
        return section;
    }
    
    private VBox createNewEquipmentSection() {
        VBox section = createSection("📝 Demande de nouvel équipement");
        
        Label infoLabel = new Label("⚠️ Cette demande devra être validée par un administrateur avant traitement.");
        infoLabel.setFont(Font.font("Segoe UI", 12));
        infoLabel.setTextFill(Color.web("#e67e22"));
        infoLabel.setWrapText(true);
        section.getChildren().add(infoLabel);
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Nom de l'équipement
        grid.add(createFieldLabel("Nom de l'équipement *"), 0, row);
        newEquipmentName = new TextField();
        newEquipmentName.setPromptText("Ex: Console Yamaha M32");
        newEquipmentName.textProperty().addListener((obs, o, n) -> validateForm());
        grid.add(newEquipmentName, 1, row++);
        GridPane.setHgrow(newEquipmentName, Priority.ALWAYS);
        
        // Marque
        grid.add(createFieldLabel("Marque"), 0, row);
        newEquipmentBrand = new TextField();
        newEquipmentBrand.setPromptText("Ex: Yamaha");
        grid.add(newEquipmentBrand, 1, row++);
        GridPane.setHgrow(newEquipmentBrand, Priority.ALWAYS);
        
        // Catégorie
        grid.add(createFieldLabel("Catégorie"), 0, row);
        newEquipmentCategory = new TextField();
        newEquipmentCategory.setPromptText("Ex: Console de mixage");
        grid.add(newEquipmentCategory, 1, row++);
        GridPane.setHgrow(newEquipmentCategory, Priority.ALWAYS);
        
        // Description
        grid.add(createFieldLabel("Description"), 0, row);
        newEquipmentDescription = new TextArea();
        newEquipmentDescription.setPromptText("Décrivez l'équipement, son numéro de série si connu...");
        newEquipmentDescription.setPrefRowCount(3);
        newEquipmentDescription.setWrapText(true);
        grid.add(newEquipmentDescription, 1, row++);
        GridPane.setHgrow(newEquipmentDescription, Priority.ALWAYS);
        
        section.getChildren().add(grid);
        return section;
    }
    
    private VBox createFaultSection() {
        VBox section = createSection("⚠️ Description de la panne");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Titre de la demande
        grid.add(createFieldLabel("Titre de la demande *"), 0, row);
        titleField = new TextField();
        titleField.setPromptText("Ex: Écran défaillant, Problème d'alimentation...");
        titleField.textProperty().addListener((obs, o, n) -> validateForm());
        grid.add(titleField, 1, row++);
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        
        // Priorité
        grid.add(createFieldLabel("Priorité"), 0, row);
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("Basse", "Normale", "Haute", "Urgente");
        priorityCombo.setValue("Normale");
        priorityCombo.setPrefWidth(200);
        grid.add(priorityCombo, 1, row++);
        
        // Description de la panne
        grid.add(createFieldLabel("Description de la panne *"), 0, row);
        faultDescriptionArea = new TextArea();
        faultDescriptionArea.setPromptText(
            "Décrivez le problème constaté :\n" +
            "- Symptômes observés\n" +
            "- Circonstances d'apparition\n" +
            "- Manipulations effectuées..."
        );
        faultDescriptionArea.setPrefRowCount(5);
        faultDescriptionArea.setWrapText(true);
        faultDescriptionArea.textProperty().addListener((obs, o, n) -> validateForm());
        grid.add(faultDescriptionArea, 1, row++);
        GridPane.setHgrow(faultDescriptionArea, Priority.ALWAYS);
        
        section.getChildren().add(grid);
        return section;
    }
    
    private VBox createRequesterSection() {
        VBox section = createSection("👤 Informations du demandeur");
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Nom du demandeur
        grid.add(createFieldLabel("Votre nom *"), 0, row);
        requesterNameField = new TextField();
        requesterNameField.setPromptText("Ex: Jean Dupont");
        requesterNameField.textProperty().addListener((obs, o, n) -> validateForm());
        grid.add(requesterNameField, 1, row++);
        GridPane.setHgrow(requesterNameField, Priority.ALWAYS);
        
        // Email
        grid.add(createFieldLabel("Votre email"), 0, row);
        requesterEmailField = new TextField();
        requesterEmailField.setPromptText("Ex: jean.dupont@magscene.fr");
        grid.add(requesterEmailField, 1, row++);
        GridPane.setHgrow(requesterEmailField, Priority.ALWAYS);
        
        section.getChildren().add(grid);
        return section;
    }
    
    private void setupButtons() {
        ButtonType submitButtonType = new ButtonType("Soumettre la demande", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        getDialogPane().getButtonTypes().addAll(submitButtonType, cancelButtonType);
        
        submitButton = (Button) getDialogPane().lookupButton(submitButtonType);
        submitButton.setStyle(
            "-fx-background-color: " + ThemeConstants.PRIMARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 25;"
        );
        submitButton.setDisable(true); // Désactivé jusqu'à validation du formulaire
        
        // Converter pour retourner les données
        setResultConverter(buttonType -> {
            if (buttonType == submitButtonType) {
                return buildRequestData();
            }
            return null;
        });
    }
    
    private void loadEquipments() {
        new Thread(() -> {
            try {
                // Utiliser la méthode existante de l'ApiService
                java.util.concurrent.CompletableFuture<java.util.List<Object>> future = apiService.getEquipments();
                java.util.List<Object> equipments = future.get(); // Attendre le résultat
                
                List<EquipmentItem> items = new ArrayList<>();
                for (Object eq : equipments) {
                    if (eq instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eqMap = (Map<String, Object>) eq;
                        items.add(new EquipmentItem(eqMap));
                    }
                }
                
                Platform.runLater(() -> {
                    equipmentData.setAll(items);
                    equipmentTable.setPlaceholder(new Label("Aucun équipement trouvé"));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    equipmentTable.setPlaceholder(new Label("Erreur de chargement: " + e.getMessage()));
                });
            }
        }).start();
    }
    
    private void filterEquipments(String filter) {
        if (filter == null || filter.isEmpty()) {
            equipmentTable.setItems(equipmentData);
            return;
        }
        
        String lowerFilter = filter.toLowerCase();
        ObservableList<EquipmentItem> filtered = FXCollections.observableArrayList();

        // 1. Priorité : code LOCMAT exact ou commence par la recherche
        List<EquipmentItem> locmatPriority = new ArrayList<>();
        List<EquipmentItem> secondary = new ArrayList<>();

        for (EquipmentItem item : equipmentData) {
            String name = item.getName() != null ? item.getName().toLowerCase() : "";
            String brand = item.getBrand() != null ? item.getBrand().toLowerCase() : "";
            String locmat = item.getLocmatCode() != null ? item.getLocmatCode().toLowerCase() : "";
            String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            String model = item.getModel() != null ? item.getModel().toLowerCase() : "";
            String serial = item.getSerialNumber() != null ? item.getSerialNumber().toLowerCase() : "";
            String qrCode = item.getQrCode() != null ? item.getQrCode().toLowerCase() : "";

            // Priorité 1 : code LOCMAT exact ou commence par la recherche
            if (!locmat.isEmpty() && (locmat.equals(lowerFilter) || locmat.startsWith(lowerFilter))) {
                locmatPriority.add(item);
            } else if (
                name.contains(lowerFilter) || brand.contains(lowerFilter) ||
                locmat.contains(lowerFilter) || category.contains(lowerFilter) ||
                model.contains(lowerFilter) || serial.contains(lowerFilter) ||
                qrCode.contains(lowerFilter)) {
                secondary.add(item);
            }
        }

        filtered.addAll(locmatPriority);
        filtered.addAll(secondary);
        equipmentTable.setItems(filtered);
    }
    
    private void updateSelectedEquipmentPreview() {
        if (selectedEquipment == null) {
            selectedPhotoView.setImage(null);
            selectedLogoView.setImage(null);
            selectedNameLabel.setText("Aucun équipement sélectionné");
            selectedInfoLabel.setText("");
            return;
        }
        
        // Charger la photo
        Image photo = selectedEquipment.getEquipmentImage();
        selectedPhotoView.setImage(photo);
        
        // Charger le logo
        Image logo = selectedEquipment.getBrandLogo();
        selectedLogoView.setImage(logo);
        
        // Mettre à jour les labels
        selectedNameLabel.setText(selectedEquipment.getName());
        
        StringBuilder info = new StringBuilder();
        if (selectedEquipment.getLocmatCode() != null && !selectedEquipment.getLocmatCode().isEmpty()) {
            info.append("📦 Code LOCMAT: ").append(selectedEquipment.getLocmatCode());
        }
        if (selectedEquipment.getQrCode() != null && !selectedEquipment.getQrCode().isEmpty()) {
            if (info.length() > 0) info.append("\n");
            info.append("🏷️ UID: ").append(selectedEquipment.getQrCode());
        }
        if (selectedEquipment.getSerialNumber() != null && !selectedEquipment.getSerialNumber().isEmpty()) {
            if (info.length() > 0) info.append("\n");
            info.append("🔢 N° Série: ").append(selectedEquipment.getSerialNumber());
        }
        if (selectedEquipment.getBrand() != null && !selectedEquipment.getBrand().isEmpty()) {
            if (info.length() > 0) info.append("\n");
            info.append("🏭 Marque: ").append(selectedEquipment.getBrand());
        }
        if (selectedEquipment.getCategory() != null && !selectedEquipment.getCategory().isEmpty()) {
            if (info.length() > 0) info.append("\n");
            info.append("📁 Catégorie: ").append(selectedEquipment.getCategory());
        }
        selectedInfoLabel.setText(info.toString());
    }
    
    private void validateForm() {
        boolean isValid = true;

        // Vérifier l'équipement selon le mode (seul champ obligatoire)
        if (existingEquipmentRadio.isSelected()) {
            if (selectedEquipment == null) {
                isValid = false;
            }
        } else {
            // Mode nouvel équipement
            if (newEquipmentName.getText() == null || newEquipmentName.getText().trim().isEmpty()) {
                isValid = false;
            }
        }

        // Champ titre obligatoire
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            isValid = false;
            titleField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
        } else {
            titleField.setStyle("");
        }

        submitButton.setDisable(!isValid);
    }
    
    private Map<String, Object> buildRequestData() {
        Map<String, Object> data = new HashMap<>();
        
        // Titre et description
        data.put("title", titleField.getText().trim());
        data.put("description", faultDescriptionArea.getText().trim());
        
        // Priorité (conversion vers les valeurs backend)
        String priority = priorityCombo.getValue();
        String priorityCode;
        switch (priority) {
            case "Basse": priorityCode = "LOW"; break;
            case "Haute": priorityCode = "HIGH"; break;
            case "Urgente": priorityCode = "URGENT"; break;
            default: priorityCode = "MEDIUM"; break;
        }
        data.put("priority", priorityCode);
        
        // Statut initial : PENDING (en attente de validation)
        data.put("status", "OPEN");
        
        // Type par défaut : REPAIR
        data.put("type", "REPAIR");
        
        // Informations demandeur
        data.put("requesterName", requesterNameField.getText().trim());
        if (requesterEmailField.getText() != null && !requesterEmailField.getText().trim().isEmpty()) {
            data.put("requesterEmail", requesterEmailField.getText().trim());
        }
        
        // Équipement
        if (existingEquipmentRadio.isSelected() && selectedEquipment != null) {
            // Équipement existant
            data.put("equipmentId", Long.parseLong(selectedEquipment.getId()));
            data.put("equipmentName", selectedEquipment.getName());
            data.put("isNewEquipmentRequest", false);
        } else {
            // Demande de nouvel équipement
            data.put("isNewEquipmentRequest", true);
            data.put("newEquipmentName", newEquipmentName.getText().trim());
            if (newEquipmentBrand.getText() != null && !newEquipmentBrand.getText().trim().isEmpty()) {
                data.put("newEquipmentBrand", newEquipmentBrand.getText().trim());
            }
            if (newEquipmentCategory.getText() != null && !newEquipmentCategory.getText().trim().isEmpty()) {
                data.put("newEquipmentCategory", newEquipmentCategory.getText().trim());
            }
            if (newEquipmentDescription.getText() != null && !newEquipmentDescription.getText().trim().isEmpty()) {
                data.put("newEquipmentDescription", newEquipmentDescription.getText().trim());
            }
            // Marquer comme en attente de validation admin
            data.put("status", "OPEN");
            data.put("pendingAdminApproval", true);
        }
        
        return data;
    }
    
    // === Utilitaires ===
    
    private VBox createSection(String title) {
        VBox section = new VBox(12);
        section.setPadding(new Insets(15));
        section.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        section.getChildren().add(titleLabel);
        return section;
    }
    
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setTextFill(Color.web("#7f8c8d"));
        label.setMinWidth(150);
        return label;
    }
    
    // Suppression de l'annotation orpheline
}
