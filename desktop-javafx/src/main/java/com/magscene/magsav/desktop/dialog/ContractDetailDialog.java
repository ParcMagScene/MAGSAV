package com.magscene.magsav.desktop.dialog;

import java.util.Map;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Dialog de visualisation/édition d'un contrat
 * Style unifié avec EquipmentDetailDialog
 */
public class ContractDetailDialog extends Dialog<Map<String, Object>> {

    private final ApiService apiService;
    private final Map<String, Object> contractData;
    private boolean editMode = false;
    private VBox contentBox;
    
    private ButtonType editButtonType;
    private ButtonType closeButtonType;

    // Champs
    private TextField referenceField, clientField, amountField, contactField;
    private ComboBox<String> typeCombo, statusCombo;
    private DatePicker startDatePicker, endDatePicker;
    private TextArea descriptionArea, conditionsArea, notesArea;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ContractDetailDialog(ApiService apiService, Map<String, Object> contractData) {
        this.apiService = apiService;
        this.contractData = new java.util.HashMap<>(contractData);

        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Contrat");
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
        
        setResultConverter(buttonType -> editMode ? contractData : null);
    }
    
    private boolean editButtonInitialized = false;
    
    private void setupEditButton() {
        Button editButton = (Button) getDialogPane().lookupButton(editButtonType);
        if (editButton != null) {
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
            createSection("📋 Informations générales", createGeneralSection()),
            createSection("📅 Période et montant", createPeriodSection()),
            createSection("👤 Contact", createContactSection()),
            createSection("📝 Détails", createDetailsSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeConstants.DIALOG_HEADER_CONTRACT_STYLE);
        
        Label contractIcon = new Label("📄");
        contractIcon.setFont(Font.font(40));
        
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String reference = getStringValue("reference");
        Label refLabel = new Label("Contrat " + (reference != null && !reference.isEmpty() ? reference : "N/A"));
        refLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        refLabel.setTextFill(Color.WHITE);
        
        String client = getStringValue("clientName");
        Label clientLabel = new Label(client != null && !client.isEmpty() ? client : "Client");
        clientLabel.setFont(Font.font("Segoe UI", 13));
        clientLabel.setTextFill(Color.web("#bdc3c7"));
        
        String status = getStringValue("status");
        Label statusLabel = new Label(status != null && !status.isEmpty() ? status : "");
        statusLabel.setFont(Font.font("Segoe UI", 11));
        statusLabel.setTextFill(getStatusColor(status));
        
        infoBox.getChildren().addAll(refLabel, clientLabel, statusLabel);
        header.getChildren().addAll(contractIcon, infoBox);
        return header;
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch (status.toUpperCase()) {
            case "ACTIF": case "ACTIVE": return Color.web("#2ecc71");
            case "EN ATTENTE": case "PENDING": return Color.web("#f39c12");
            case "EXPIRÉ": case "EXPIRED": return Color.web("#95a5a6");
            case "RÉSILIÉ": case "TERMINATED": return Color.web("#e74c3c");
            case "SUSPENDU": case "SUSPENDED": return Color.web("#9b59b6");
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
        
        referenceField = createTextField("reference");
        row = addField(grid, row, "Référence", referenceField, getStringValue("reference"));
        
        clientField = createTextField("clientName");
        row = addField(grid, row, "Client", clientField, getStringValue("clientName"));
        
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Maintenance", "Location", "Service", "Support", "Licence", "Autre");
        typeCombo.setValue(getStringValue("type"));
        typeCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Type", typeCombo, getStringValue("type"));
        
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Actif", "En attente", "Expiré", "Résilié", "Suspendu");
        statusCombo.setValue(getStringValue("status"));
        statusCombo.setDisable(!editMode);
        addFieldWithNode(grid, row, "Statut", statusCombo, getStringValue("status"));
        
        return grid;
    }
    
    private GridPane createPeriodSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Date de début
        startDatePicker = new DatePicker();
        startDatePicker.setDisable(!editMode);
        String startDate = getStringValue("startDate");
        if (!startDate.isEmpty()) {
            try {
                startDatePicker.setValue(LocalDate.parse(startDate.substring(0, 10)));
            } catch (Exception ignored) {}
        }
        row = addFieldWithNode(grid, row, "Date de début", startDatePicker, formatDate(startDate));
        
        // Date de fin
        endDatePicker = new DatePicker();
        endDatePicker.setDisable(!editMode);
        String endDate = getStringValue("endDate");
        if (!endDate.isEmpty()) {
            try {
                endDatePicker.setValue(LocalDate.parse(endDate.substring(0, 10)));
            } catch (Exception ignored) {}
        }
        row = addFieldWithNode(grid, row, "Date de fin", endDatePicker, formatDate(endDate));
        
        // Montant
        amountField = createTextField("amount");
        addField(grid, row, "Montant", amountField, getStringValue("amount") + " €");
        
        return grid;
    }
    
    private GridPane createContactSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        contactField = createTextField("contact");
        addField(grid, 0, "Contact", contactField, getStringValue("contact"));
        
        return grid;
    }
    
    private GridPane createDetailsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, null, true));
        
        int row = 0;
        
        Label descLabel = new Label("Description :");
        descLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        descLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(descLabel, 0, row++);
        
        descriptionArea = new TextArea(getStringValue("description"));
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(3);
        descriptionArea.setEditable(editMode);
        descriptionArea.setStyle(editMode ? ThemeConstants.FIELD_EDITABLE_STYLE : ThemeConstants.FIELD_READONLY_STYLE);
        grid.add(descriptionArea, 0, row++);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);
        
        Label condLabel = new Label("Conditions particulières :");
        condLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        condLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(condLabel, 0, row++);
        
        conditionsArea = new TextArea(getStringValue("conditions"));
        conditionsArea.setWrapText(true);
        conditionsArea.setPrefRowCount(3);
        conditionsArea.setEditable(editMode);
        conditionsArea.setStyle(editMode ? ThemeConstants.FIELD_EDITABLE_STYLE : ThemeConstants.FIELD_READONLY_STYLE);
        grid.add(conditionsArea, 0, row++);
        GridPane.setHgrow(conditionsArea, Priority.ALWAYS);
        
        Label notesLabel = new Label("Notes internes :");
        notesLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        notesLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(notesLabel, 0, row++);
        
        notesArea = new TextArea(getStringValue("notes"));
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(2);
        notesArea.setEditable(editMode);
        notesArea.setStyle(editMode ? ThemeConstants.FIELD_EDITABLE_STYLE : ThemeConstants.FIELD_READONLY_STYLE);
        grid.add(notesArea, 0, row);
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
            field.setText(value != null ? value.replace(" €", "") : "");
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
        if (referenceField != null) contractData.put("reference", referenceField.getText().trim());
        if (clientField != null) contractData.put("clientName", clientField.getText().trim());
        
        if (typeCombo != null && typeCombo.getValue() != null) {
            contractData.put("type", typeCombo.getValue());
        }
        if (statusCombo != null && statusCombo.getValue() != null) {
            contractData.put("status", statusCombo.getValue());
        }
        
        if (startDatePicker != null && startDatePicker.getValue() != null) {
            contractData.put("startDate", startDatePicker.getValue().format(DATE_FORMATTER));
        }
        if (endDatePicker != null && endDatePicker.getValue() != null) {
            contractData.put("endDate", endDatePicker.getValue().format(DATE_FORMATTER));
        }
        
        if (amountField != null) contractData.put("amount", amountField.getText().trim());
        if (contactField != null) contractData.put("contact", contactField.getText().trim());
        
        if (descriptionArea != null) contractData.put("description", descriptionArea.getText().trim());
        if (conditionsArea != null) contractData.put("conditions", conditionsArea.getText().trim());
        if (notesArea != null) contractData.put("notes", notesArea.getText().trim());
    }
    
    private void saveToApi() {
        Object id = contractData.get("id");
        if (id != null && apiService != null) {
            apiService.updateContract(((Number) id).longValue(), contractData)
                .thenAccept(result -> Platform.runLater(() -> {
                    System.out.println("✅ Contrat mis à jour avec succès");
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
        Object value = contractData.get(key);
        return value != null ? value.toString() : "";
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
}
