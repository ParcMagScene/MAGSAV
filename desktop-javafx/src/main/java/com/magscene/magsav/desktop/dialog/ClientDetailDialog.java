package com.magscene.magsav.desktop.dialog;

import java.util.Map;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Dialog de visualisation/édition d'un client
 * Style unifié avec EquipmentDetailDialog
 */
public class ClientDetailDialog extends Dialog<Map<String, Object>> {

    private final ApiService apiService;
    private final MediaService mediaService;
    private final Map<String, Object> clientData;
    
    private boolean editMode = false;
    @SuppressWarnings("unused")
    private Button editSaveButton;
    private VBox contentBox;
    
    private ButtonType editButtonType;
    private ButtonType closeButtonType;

    // Champs - Informations générales
    private TextField nameField;
    private ComboBox<String> typeCombo;
    private ComboBox<String> statusCombo;
    private ComboBox<String> categoryCombo;
    private TextField siretField;

    // Champs - Contact
    private TextField emailField;
    private TextField phoneField;
    private TextField contactPersonField;
    private TextField websiteField;

    // Champs - Adresse
    private TextField addressField;
    private TextField postalCodeField;
    private TextField cityField;
    private TextField countryField;

    // Champs - Notes
    private TextArea notesArea;

    public ClientDetailDialog(ApiService apiService, Map<String, Object> clientData) {
        this.apiService = apiService;
        this.mediaService = MediaService.getInstance();
        this.clientData = new java.util.HashMap<>(clientData);

        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Client");
        setHeaderText(null);
        
        editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        getDialogPane().setPrefSize(700, 700);
        getDialogPane().setMinWidth(650);
        getDialogPane().setMinHeight(600);
        
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        setResultConverter(buttonType -> editMode ? clientData : null);
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
        
        mainLayout.setTop(createHeader());
        
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
            createSection("🏢 Informations générales", createGeneralSection()),
            createSection("📞 Contact", createContactSection()),
            createSection("📍 Adresse", createAddressSection()),
            createSection("📝 Notes", createNotesSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeConstants.DIALOG_HEADER_CLIENT_STYLE);
        
        // Logo du client ou icône par défaut
        StackPane imageContainer = new StackPane();
        imageContainer.setMinSize(70, 55);
        imageContainer.setMaxSize(70, 55);
        imageContainer.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;");
        
        String logoPath = getStringValue("logoPath");
        Image logoImage = null;
        
        if (logoPath != null && !logoPath.isEmpty()) {
            logoImage = mediaService.loadClientLogo(logoPath, 70, 55);
        }
        
        if (logoImage != null) {
            ImageView imageView = new ImageView(logoImage);
            imageView.setFitWidth(70);
            imageView.setFitHeight(55);
            imageView.setPreserveRatio(true);
            
            // Effet d'ombre
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.3));
            shadow.setRadius(5);
            imageView.setEffect(shadow);
            
            imageContainer.getChildren().add(imageView);
        } else {
            // Icône selon la catégorie du client
            String category = getStringValue("category");
            Label clientIcon = new Label(getCategoryIcon(category));
            clientIcon.setFont(Font.font(32));
            imageContainer.getChildren().add(clientIcon);
        }
        
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String name = getStringValue("name");
        Label nameLabel = new Label(name != null && !name.isEmpty() ? name : "Client");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);
        
        String category = getStringValue("category");
        String type = getStringValue("type");
        Label typeLabel = new Label(
            (type != null && !type.isEmpty() ? formatType(type) : "") + 
            (type != null && !type.isEmpty() && category != null && !category.isEmpty() ? " • " : "") + 
            (category != null && !category.isEmpty() ? formatCategory(category) : "")
        );
        typeLabel.setFont(Font.font("Segoe UI", 13));
        typeLabel.setTextFill(Color.web("#bdc3c7"));
        
        String status = getStringValue("status");
        Label statusLabel = new Label("Statut: " + (status != null && !status.isEmpty() ? formatStatus(status) : "-"));
        statusLabel.setFont(Font.font("Segoe UI", 11));
        statusLabel.setTextFill(getStatusColor(status));
        
        infoBox.getChildren().addAll(nameLabel, typeLabel, statusLabel);
        
        // Badge catégorie à droite
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        if (category != null && !category.isEmpty()) {
            Label categoryBadge = new Label(formatCategory(category));
            categoryBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            categoryBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 10; -fx-background-radius: 15;");
            categoryBadge.setTextFill(Color.WHITE);
            rightBox.getChildren().add(categoryBadge);
        }
        
        header.getChildren().addAll(imageContainer, infoBox, rightBox);
        return header;
    }
    
    private String getCategoryIcon(String category) {
        if (category == null) return "👤";
        switch (category.toUpperCase()) {
            case "ADMINISTRATION": case "ADMIN": return "🏛️";
            case "ENTREPRISE": case "COMPANY": case "BUSINESS": return "🏢";
            case "PARTICULIER": case "INDIVIDUAL": case "PRIVATE": return "👤";
            case "ASSOCIATION": return "🤝";
            case "COLLECTIVITE": case "PUBLIC": return "🏛️";
            default: return "👤";
        }
    }
    
    private String formatCategory(String category) {
        if (category == null) return "";
        switch (category.toUpperCase()) {
            case "ADMINISTRATION": case "ADMIN": return "Administration";
            case "ENTREPRISE": case "COMPANY": case "BUSINESS": return "Entreprise";
            case "PARTICULIER": case "INDIVIDUAL": case "PRIVATE": return "Particulier";
            case "ASSOCIATION": return "Association";
            case "COLLECTIVITE": case "PUBLIC": return "Collectivité";
            default: return category;
        }
    }
    
    private String formatType(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "CLIENT": return "Client";
            case "PROSPECT": return "Prospect";
            case "FOURNISSEUR": case "SUPPLIER": return "Fournisseur";
            case "PARTENAIRE": case "PARTNER": return "Partenaire";
            default: return type;
        }
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "ACTIF": case "ACTIVE": return "Actif";
            case "INACTIF": case "INACTIVE": return "Inactif";
            case "PROSPECT": return "Prospect";
            case "SUSPENDU": case "SUSPENDED": return "Suspendu";
            default: return status;
        }
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch (status.toUpperCase()) {
            case "ACTIF": case "ACTIVE": return Color.web("#2ecc71");
            case "INACTIF": case "INACTIVE": return Color.web("#95a5a6");
            case "PROSPECT": return Color.web("#3498db");
            case "SUSPENDU": case "SUSPENDED": return Color.web("#e74c3c");
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
    
    // ========== SECTION CREATION ==========
    
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
        
        nameField = createTextField("name");
        row = addField(grid, row, "Nom / Raison sociale", nameField, getStringValue("name"));
        
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Entreprise", "Administration", "Association", "Particulier");
        typeCombo.setValue(getStringValue("type"));
        typeCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Type", typeCombo, getStringValue("type"));
        
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Actif", "Inactif", "Prospect", "Suspendu");
        statusCombo.setValue(getStringValue("status"));
        statusCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Statut", statusCombo, getStringValue("status"));
        
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Premium", "VIP", "Standard", "Basique");
        categoryCombo.setValue(getStringValue("category"));
        categoryCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Catégorie", categoryCombo, getStringValue("category"));
        
        siretField = createTextField("siret");
        addField(grid, row, "SIRET", siretField, getStringValue("siret"));
        
        return grid;
    }
    
    private GridPane createContactSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        emailField = createTextField("email");
        row = addField(grid, row, "Email", emailField, getStringValue("email"));
        
        phoneField = createTextField("phone");
        row = addField(grid, row, "Téléphone", phoneField, getStringValue("phone"));
        
        contactPersonField = createTextField("contactPerson");
        row = addField(grid, row, "Contact principal", contactPersonField, getStringValue("contactPerson"));
        
        websiteField = createTextField("website");
        addField(grid, row, "Site web", websiteField, getStringValue("website"));
        
        return grid;
    }
    
    private GridPane createAddressSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        addressField = createTextField("address");
        row = addField(grid, row, "Adresse", addressField, getStringValue("address"));
        
        postalCodeField = createTextField("postalCode");
        row = addField(grid, row, "Code postal", postalCodeField, getStringValue("postalCode"));
        
        cityField = createTextField("city");
        row = addField(grid, row, "Ville", cityField, getStringValue("city"));
        
        countryField = createTextField("country");
        addField(grid, row, "Pays", countryField, getStringValue("country"));
        
        return grid;
    }
    
    private GridPane createNotesSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, null, true));
        
        Label notesLabel = new Label("Notes internes :");
        notesLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        notesLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(notesLabel, 0, 0);
        
        notesArea = new TextArea(getStringValue("notes"));
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(4);
        notesArea.setEditable(editMode);
        notesArea.setStyle(editMode ? ThemeConstants.FIELD_EDITABLE_STYLE : ThemeConstants.FIELD_READONLY_STYLE);
        grid.add(notesArea, 0, 1);
        GridPane.setHgrow(notesArea, Priority.ALWAYS);
        
        return grid;
    }
    
    // ========== FIELD HELPERS ==========
    
    private TextField createTextField(String key) {
        TextField field = new TextField();
        field.setEditable(editMode);
        field.setStyle(editMode ? ThemeConstants.COMBO_EDITABLE_STYLE : "-fx-background-color: transparent; -fx-border-color: transparent;");
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
    
    // ========== SAVE & DATA ==========
    
    private void saveChanges() {
        if (nameField != null) clientData.put("name", nameField.getText().trim());
        if (siretField != null) clientData.put("siret", siretField.getText().trim());
        
        if (typeCombo != null && typeCombo.getValue() != null) {
            clientData.put("type", typeCombo.getValue());
        }
        if (statusCombo != null && statusCombo.getValue() != null) {
            clientData.put("status", statusCombo.getValue());
        }
        if (categoryCombo != null && categoryCombo.getValue() != null) {
            clientData.put("category", categoryCombo.getValue());
        }
        
        if (emailField != null) clientData.put("email", emailField.getText().trim());
        if (phoneField != null) clientData.put("phone", phoneField.getText().trim());
        if (contactPersonField != null) clientData.put("contactPerson", contactPersonField.getText().trim());
        if (websiteField != null) clientData.put("website", websiteField.getText().trim());
        
        if (addressField != null) clientData.put("address", addressField.getText().trim());
        if (postalCodeField != null) clientData.put("postalCode", postalCodeField.getText().trim());
        if (cityField != null) clientData.put("city", cityField.getText().trim());
        if (countryField != null) clientData.put("country", countryField.getText().trim());
        
        if (notesArea != null) clientData.put("notes", notesArea.getText().trim());
    }
    
    private void saveToApi() {
        Object id = clientData.get("id");
        if (id != null && apiService != null) {
            apiService.updateClient(((Number) id).longValue(), clientData)
                .thenAccept(result -> Platform.runLater(() -> {
                    System.out.println("✅ Client mis à jour avec succès");
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
    
    private String getStringValue(String key) {
        Object value = clientData.get(key);
        return value != null ? value.toString() : "";
    }
}
