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

/**
 * Dialog de visualisation/édition d'une demande SAV
 * S'ouvre en mode lecture seule, bouton Modifier pour basculer en édition
 * Style unifié avec EquipmentDetailDialog
 */
public class SAVDetailDialog extends Dialog<Map<String, Object>> {

    private final ApiService apiService;
    private final Map<String, Object> savData;
    
    private boolean editMode = false;
    private Button editSaveButton;
    private VBox contentBox;
    
    // Boutons
    private ButtonType editButtonType;
    private ButtonType closeButtonType;
    
    // Champs éditables
    private TextField titleField, equipmentField, clientField, technicianField;
    private ComboBox<String> typeCombo, statusCombo, priorityCombo;
    private TextArea descriptionArea, diagnosisArea, resolutionArea;

    public SAVDetailDialog(ApiService apiService, Map<String, Object> savData) {
        this.apiService = apiService;
        this.savData = new java.util.HashMap<>(savData);
        
        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Demande SAV");
        setHeaderText(null);
        
        editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        getDialogPane().setPrefSize(700, 650);
        getDialogPane().setMinWidth(650);
        getDialogPane().setMinHeight(600);
        
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        setResultConverter(buttonType -> editMode ? savData : null);
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
            createSection("📋 Informations générales", createGeneralSection()),
            createSection("👤 Assignation", createAssignmentSection()),
            createSection("📝 Description et diagnostic", createDescriptionSection()),
            createSection("✅ Résolution", createResolutionSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeConstants.DIALOG_HEADER_SAV_STYLE);
        
        Label savIcon = new Label("🔧");
        savIcon.setFont(Font.font(40));
        
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String id = getStringValue("id");
        Label idLabel = new Label("Demande SAV #" + (id != null && !id.isEmpty() ? id : "N/A"));
        idLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        idLabel.setTextFill(Color.WHITE);
        
        String title = getStringValue("title");
        Label titleLabel = new Label(title != null && !title.isEmpty() ? title : "Sans titre");
        titleLabel.setFont(Font.font("Segoe UI", 13));
        titleLabel.setTextFill(Color.web("#bdc3c7"));
        
        String status = getStringValue("status");
        String priority = getStringValue("priority");
        Label statusLabel = new Label(
            (status != null && !status.isEmpty() ? formatStatus(status) : "") + 
            (status != null && !status.isEmpty() && priority != null && !priority.isEmpty() ? " • " : "") + 
            (priority != null && !priority.isEmpty() ? formatPriority(priority) : "")
        );
        statusLabel.setFont(Font.font("Segoe UI", 11));
        statusLabel.setTextFill(getStatusColor(status));
        
        infoBox.getChildren().addAll(idLabel, titleLabel, statusLabel);
        header.getChildren().addAll(savIcon, infoBox);
        return header;
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "OPEN": return "Ouvert";
            case "IN_PROGRESS": return "En cours";
            case "WAITING_PARTS": return "En attente pièces";
            case "RESOLVED": return "Résolu";
            case "CLOSED": return "Clôturé";
            default: return status;
        }
    }
    
    private String formatPriority(String priority) {
        if (priority == null) return "";
        switch (priority.toUpperCase()) {
            case "LOW": return "Priorité basse";
            case "MEDIUM": return "Priorité normale";
            case "HIGH": return "Priorité haute";
            case "URGENT": return "Urgent";
            default: return priority;
        }
    }
    
    private String formatType(String type) {
        if (type == null) return "—";
        switch (type.toUpperCase()) {
            case "REPAIR": return "Réparation";
            case "MAINTENANCE": return "Maintenance";
            case "CALIBRATION": return "Calibration";
            case "INSPECTION": return "Inspection";
            case "TRAINING": return "Formation";
            case "RMA": return "RMA";
            default: return type;
        }
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch (status.toUpperCase()) {
            case "OPEN": return Color.web("#3498db");
            case "IN_PROGRESS": return Color.web("#f39c12");
            case "WAITING_PARTS": return Color.web("#9b59b6");
            case "RESOLVED": return Color.web("#2ecc71");
            case "CLOSED": return Color.web("#95a5a6");
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
        
        titleField = createTextField("title");
        row = addField(grid, row, "Titre", titleField, getStringValue("title"));
        
        typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("REPAIR", "MAINTENANCE", "CALIBRATION", "INSPECTION", "TRAINING", "RMA");
        typeCombo.setValue(getStringValue("type"));
        typeCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Type", typeCombo, formatType(getStringValue("type")));
        
        statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("OPEN", "IN_PROGRESS", "WAITING_PARTS", "RESOLVED", "CLOSED");
        statusCombo.setValue(getStringValue("status"));
        statusCombo.setDisable(!editMode);
        row = addFieldWithNode(grid, row, "Statut", statusCombo, formatStatus(getStringValue("status")));
        
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("LOW", "MEDIUM", "HIGH", "URGENT");
        priorityCombo.setValue(getStringValue("priority"));
        priorityCombo.setDisable(!editMode);
        addFieldWithNode(grid, row, "Priorité", priorityCombo, formatPriority(getStringValue("priority")));
        
        return grid;
    }
    
    private GridPane createAssignmentSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        equipmentField = createTextField("equipmentName");
        row = addField(grid, row, "Équipement", equipmentField, getStringValue("equipmentName"));
        
        clientField = createTextField("clientName");
        row = addField(grid, row, "Client", clientField, getStringValue("clientName"));
        
        technicianField = createTextField("assignedTechnician");
        row = addField(grid, row, "Technicien", technicianField, getStringValue("assignedTechnician"));
        
        row = addReadOnlyField(grid, row, "Date création", formatDate(getStringValue("createdAt")));
        addReadOnlyField(grid, row, "Dernière MAJ", formatDate(getStringValue("updatedAt")));
        
        return grid;
    }
    
    private GridPane createDescriptionSection() {
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
        
        Label diagLabel = new Label("Diagnostic :");
        diagLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        diagLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(diagLabel, 0, row++);
        
        diagnosisArea = new TextArea(getStringValue("diagnosis"));
        diagnosisArea.setWrapText(true);
        diagnosisArea.setPrefRowCount(3);
        diagnosisArea.setEditable(editMode);
        diagnosisArea.setStyle(editMode ? ThemeConstants.FIELD_EDITABLE_STYLE : ThemeConstants.FIELD_READONLY_STYLE);
        grid.add(diagnosisArea, 0, row);
        GridPane.setHgrow(diagnosisArea, Priority.ALWAYS);
        
        return grid;
    }
    
    private GridPane createResolutionSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.getColumnConstraints().add(new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, null, true));
        
        Label resLabel = new Label("Résolution / Actions effectuées :");
        resLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        resLabel.setTextFill(Color.web("#7f8c8d"));
        grid.add(resLabel, 0, 0);
        
        resolutionArea = new TextArea(getStringValue("resolution"));
        resolutionArea.setWrapText(true);
        resolutionArea.setPrefRowCount(4);
        resolutionArea.setEditable(editMode);
        resolutionArea.setStyle(editMode ? ThemeConstants.FIELD_EDITABLE_STYLE : ThemeConstants.FIELD_READONLY_STYLE);
        grid.add(resolutionArea, 0, 1);
        GridPane.setHgrow(resolutionArea, Priority.ALWAYS);
        
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
    
    // ========== SAVE & DATA ==========
    
    private void saveChanges() {
        if (titleField != null) savData.put("title", titleField.getText().trim());
        if (equipmentField != null) savData.put("equipmentName", equipmentField.getText().trim());
        if (clientField != null) savData.put("clientName", clientField.getText().trim());
        if (technicianField != null) savData.put("assignedTechnician", technicianField.getText().trim());
        
        if (typeCombo != null && typeCombo.getValue() != null) {
            savData.put("type", typeCombo.getValue());
        }
        if (statusCombo != null && statusCombo.getValue() != null) {
            savData.put("status", statusCombo.getValue());
        }
        if (priorityCombo != null && priorityCombo.getValue() != null) {
            savData.put("priority", priorityCombo.getValue());
        }
        
        if (descriptionArea != null) savData.put("description", descriptionArea.getText().trim());
        if (diagnosisArea != null) savData.put("diagnosis", diagnosisArea.getText().trim());
        if (resolutionArea != null) savData.put("resolution", resolutionArea.getText().trim());
    }
    
    private void saveToApi() {
        Object id = savData.get("id");
        if (id != null && apiService != null) {
            apiService.updateSAVRequest(((Number) id).longValue(), savData)
                .thenAccept(result -> Platform.runLater(() -> {
                    System.out.println("✅ Demande SAV mise à jour avec succès");
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
        Object value = savData.get(key);
        return value != null ? value.toString() : "";
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "—";
        try {
            if (dateStr.length() >= 10) {
                java.time.LocalDate date = java.time.LocalDate.parse(dateStr.substring(0, 10));
                return date.getDayOfMonth() + "/" + date.getMonthValue() + "/" + date.getYear();
            }
        } catch (Exception e) {
            // Ignore
        }
        return dateStr;
    }
}
