package com.magscene.magsav.desktop.dialog;

import com.magscene.magsav.desktop.config.SpecialtiesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.MediaService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog de visualisation/édition d'un membre du personnel
 * S'ouvre en mode lecture seule, bouton Modifier pour basculer en édition
 */
@SuppressWarnings("unused")
public class PersonnelDetailDialog extends Dialog<Map<String, Object>> {
    
    private final ApiService apiService;
    private final MediaService mediaService;
    private final Map<String, Object> personnelData;
    private final SpecialtiesConfigManager specialtiesManager;
    
    private boolean editMode = false;
    private Button editSaveButton;  // Utilisé pour la référence future
    private VBox contentBox;
    
    // ButtonTypes pour les boutons du bas
    private ButtonType editButtonType;
    private ButtonType closeButtonType;
    
    // Champs éditables
    private TextField firstNameField, lastNameField, emailField, phoneField;
    private TextField jobTitleField, departmentField;
    private ComboBox<String> typeCombo, statusCombo;
    private DatePicker hireDatePicker;
    private TextArea notesArea;
    private VBox specialtiesContainer;
    private Map<String, CheckBox> specialtyCheckboxes = new HashMap<>();
    
    public PersonnelDetailDialog(ApiService apiService, Map<String, Object> personnel) {
        this.apiService = apiService;
        this.mediaService = MediaService.getInstance();
        this.personnelData = new HashMap<>(personnel);
        this.specialtiesManager = SpecialtiesConfigManager.getInstance();
        
        setupDialog();
        createContent();
    }
    
    private void setupDialog() {
        setTitle("Fiche Personnel");
        setHeaderText(null);
        
        // Bouton Modifier/Enregistrer en bas du dialog (sans icône)
        editButtonType = new ButtonType("Modifier", ButtonBar.ButtonData.LEFT);
        closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        getDialogPane().setPrefSize(650, 550);
        getDialogPane().setMinWidth(600);
        getDialogPane().setMinHeight(500);
        
        // Appliquer le thème unifié
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());
        
        // Styliser et configurer le bouton Modifier
        Platform.runLater(() -> {
            setupEditButton();
            setupCloseButton();
        });
        
        setResultConverter(buttonType -> editMode ? personnelData : null);
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
        
        // Header avec infos personnel
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
            createSection("👤 Informations personnelles", createPersonalSection()),
            createSection("💼 Informations professionnelles", createProfessionalSection()),
            createSection("🎯 Compétences & Spécialités", createSkillsSection()),
            createSection("📝 Notes", createNotesSection())
        );
    }
    
    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(ThemeConstants.DIALOG_HEADER_PERSONNEL_STYLE);
        
        // Avatar/Photo ou icône par défaut
        StackPane imageContainer = new StackPane();
        imageContainer.setMinSize(60, 60);
        imageContainer.setMaxSize(60, 60);
        imageContainer.setStyle("-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 30;");
        
        String avatarPath = getStringValue("avatarPath");
        Image avatarImage = null;
        
        if (avatarPath != null && !avatarPath.isEmpty()) {
            avatarImage = mediaService.loadAvatar(avatarPath, 60, 60);
        }
        
        if (avatarImage != null) {
            ImageView imageView = new ImageView(avatarImage);
            imageView.setFitWidth(60);
            imageView.setFitHeight(60);
            imageView.setPreserveRatio(true);
            
            // Clip circulaire pour l'avatar
            Circle clip = new Circle(30, 30, 30);
            imageView.setClip(clip);
            
            // Effet d'ombre
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.rgb(0, 0, 0, 0.3));
            shadow.setRadius(5);
            imageView.setEffect(shadow);
            
            imageContainer.getChildren().add(imageView);
        } else {
            // Icône par défaut
            Label avatarIcon = new Label("👤");
            avatarIcon.setFont(Font.font(32));
            imageContainer.getChildren().add(avatarIcon);
        }
        
        // Infos centrales
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String firstName = getStringValue("firstName");
        String lastName = getStringValue("lastName");
        String fullName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
        
        Label nameLabel = new Label(!fullName.isEmpty() ? fullName : "Personnel sans nom");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        nameLabel.setTextFill(Color.WHITE);
        
        String jobTitle = getStringValue("jobTitle");
        String department = getStringValue("department");
        Label subtitleLabel = new Label(
            (jobTitle != null && !jobTitle.isEmpty() ? jobTitle : "") + 
            (jobTitle != null && !jobTitle.isEmpty() && department != null && !department.isEmpty() ? " • " : "") + 
            (department != null && !department.isEmpty() ? department : "")
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
        
        // Type à droite
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        
        String type = getStringValue("type");
        if (type != null && !type.isEmpty()) {
            Label typeLabel = new Label(formatType(type));
            typeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            typeLabel.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 10; -fx-background-radius: 15;");
            typeLabel.setTextFill(Color.WHITE);
            rightBox.getChildren().add(typeLabel);
        }
        
        header.getChildren().addAll(imageContainer, infoBox, rightBox);
        return header;
    }
    
    private String formatStatus(String status) {
        if (status == null) return "";
        switch (status.toUpperCase()) {
            case "ACTIVE": return "Actif";
            case "INACTIVE": return "Inactif";
            case "ON_LEAVE": return "En congé";
            case "TERMINATED": return "Terminé";
            default: return status;
        }
    }
    
    private String formatType(String type) {
        if (type == null) return "";
        switch (type.toUpperCase()) {
            case "EMPLOYEE": return "Employé";
            case "FREELANCE": return "Freelance";
            case "INTERN": return "Stagiaire";
            case "TEMPORARY": return "Intérimaire";
            case "PERFORMER": return "Intermittent";
            default: return type;
        }
    }
    
    private Color getStatusColor(String status) {
        if (status == null) return Color.WHITE;
        switch (status.toUpperCase()) {
            case "ACTIVE": return Color.web("#2ecc71");
            case "INACTIVE": return Color.web("#95a5a6");
            case "ON_LEAVE": return Color.web("#f39c12");
            case "TERMINATED": return Color.web("#e74c3c");
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
        if (firstNameField != null) personnelData.put("firstName", firstNameField.getText().trim());
        if (lastNameField != null) personnelData.put("lastName", lastNameField.getText().trim());
        if (emailField != null) personnelData.put("email", emailField.getText().trim());
        if (phoneField != null) personnelData.put("phone", phoneField.getText().trim());
        if (jobTitleField != null) personnelData.put("jobTitle", jobTitleField.getText().trim());
        if (departmentField != null) personnelData.put("department", departmentField.getText().trim());
        if (notesArea != null) personnelData.put("notes", notesArea.getText().trim());
        
        // ComboBox
        if (typeCombo != null && typeCombo.getValue() != null) {
            personnelData.put("type", typeCombo.getValue());
        }
        if (statusCombo != null && statusCombo.getValue() != null) {
            personnelData.put("status", statusCombo.getValue());
        }
        
        // Date
        if (hireDatePicker != null && hireDatePicker.getValue() != null) {
            personnelData.put("hireDate", hireDatePicker.getValue().toString());
        }
        
        // Spécialités
        StringBuilder specialties = new StringBuilder();
        for (Map.Entry<String, CheckBox> entry : specialtyCheckboxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                if (specialties.length() > 0) specialties.append(",");
                specialties.append(entry.getKey());
            }
        }
        personnelData.put("specialties", specialties.toString());
    }
    
    private void saveToApi() {
        // TODO: Implémenter quand l'API sera disponible
        Object id = personnelData.get("id");
        if (id != null && apiService != null) {
            System.out.println("✅ Personnel " + id + " modifié (sauvegarde API à implémenter)");
        }
    }
    
    // ========== Sections de contenu ==========
    
    private VBox createSection(String title, Region content) {
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
    
    private GridPane createPersonalSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Prénom - Nom
        grid.add(createFieldLabel("Prénom"), 0, row);
        if (editMode) {
            firstNameField = new TextField(getStringValue("firstName"));
            firstNameField.setPrefWidth(180);
            grid.add(firstNameField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("firstName")), 1, row);
        }
        
        grid.add(createFieldLabel("Nom"), 2, row);
        if (editMode) {
            lastNameField = new TextField(getStringValue("lastName"));
            lastNameField.setPrefWidth(180);
            grid.add(lastNameField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("lastName")), 3, row);
        }
        row++;
        
        // Email - Téléphone
        grid.add(createFieldLabel("Email"), 0, row);
        if (editMode) {
            emailField = new TextField(getStringValue("email"));
            emailField.setPrefWidth(200);
            grid.add(emailField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("email")), 1, row);
        }
        
        grid.add(createFieldLabel("Téléphone"), 2, row);
        if (editMode) {
            phoneField = new TextField(getStringValue("phone"));
            phoneField.setPrefWidth(150);
            grid.add(phoneField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("phone")), 3, row);
        }
        
        return grid;
    }
    
    private GridPane createProfessionalSection() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        int row = 0;
        
        // Type - Statut
        grid.add(createFieldLabel("Type"), 0, row);
        if (editMode) {
            typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                "EMPLOYEE", "FREELANCE", "INTERN", "TEMPORARY", "PERFORMER"
            ));
            typeCombo.setValue(getStringValue("type"));
            typeCombo.setPrefWidth(150);
            grid.add(typeCombo, 1, row);
        } else {
            grid.add(createValueLabel(formatType(getStringValue("type"))), 1, row);
        }
        
        grid.add(createFieldLabel("Statut"), 2, row);
        if (editMode) {
            statusCombo = new ComboBox<>(FXCollections.observableArrayList(
                "ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"
            ));
            statusCombo.setValue(getStringValue("status"));
            statusCombo.setPrefWidth(150);
            grid.add(statusCombo, 3, row);
        } else {
            grid.add(createValueLabel(formatStatus(getStringValue("status"))), 3, row);
        }
        row++;
        
        // Poste - Département
        grid.add(createFieldLabel("Poste"), 0, row);
        if (editMode) {
            jobTitleField = new TextField(getStringValue("jobTitle"));
            jobTitleField.setPrefWidth(200);
            grid.add(jobTitleField, 1, row);
        } else {
            grid.add(createValueLabel(getStringValue("jobTitle")), 1, row);
        }
        
        grid.add(createFieldLabel("Département"), 2, row);
        if (editMode) {
            departmentField = new TextField(getStringValue("department"));
            departmentField.setPrefWidth(150);
            grid.add(departmentField, 3, row);
        } else {
            grid.add(createValueLabel(getStringValue("department")), 3, row);
        }
        row++;
        
        // Date d'embauche
        grid.add(createFieldLabel("Date d'embauche"), 0, row);
        if (editMode) {
            hireDatePicker = new DatePicker(parseDate(getStringValue("hireDate")));
            grid.add(hireDatePicker, 1, row);
        } else {
            grid.add(createValueLabel(formatDate(getStringValue("hireDate"))), 1, row);
        }
        
        return grid;
    }
    
    private VBox createSkillsSection() {
        VBox container = new VBox(10);
        
        // Charger les spécialités existantes
        String existingSpecialties = getStringValue("specialties");
        List<String> selectedSpecialties = existingSpecialties != null && !existingSpecialties.isEmpty() 
            ? List.of(existingSpecialties.split(","))
            : List.of();
        
        if (editMode) {
            // Afficher les checkboxes pour chaque spécialité disponible
            specialtiesContainer = new VBox(5);
            specialtyCheckboxes.clear();
            
            FlowPane flowPane = new FlowPane();
            flowPane.setHgap(15);
            flowPane.setVgap(8);
            
            for (String specialty : specialtiesManager.getAvailableSpecialties()) {
                CheckBox cb = new CheckBox(specialty);
                cb.setSelected(selectedSpecialties.contains(specialty));
                specialtyCheckboxes.put(specialty, cb);
                flowPane.getChildren().add(cb);
            }
            
            container.getChildren().add(flowPane);
        } else {
            // Afficher les spécialités sélectionnées
            if (selectedSpecialties.isEmpty() || (selectedSpecialties.size() == 1 && selectedSpecialties.get(0).isEmpty())) {
                container.getChildren().add(createValueLabel("Aucune spécialité"));
            } else {
                FlowPane flowPane = new FlowPane();
                flowPane.setHgap(8);
                flowPane.setVgap(8);
                
                for (String specialty : selectedSpecialties) {
                    if (!specialty.isEmpty()) {
                        Label tag = new Label(specialty);
                        tag.setStyle(ThemeConstants.TAG_STYLE);
                        tag.setFont(Font.font("Segoe UI", 11));
                        flowPane.getChildren().add(tag);
                    }
                }
                
                container.getChildren().add(flowPane);
            }
        }
        
        return container;
    }
    
    private VBox createNotesSection() {
        VBox container = new VBox(5);
        
        if (editMode) {
            notesArea = new TextArea(getStringValue("notes"));
            notesArea.setPrefRowCount(3);
            notesArea.setPromptText("Notes additionnelles...");
            container.getChildren().add(notesArea);
        } else {
            String notes = getStringValue("notes");
            Label notesLabel = createValueLabel(notes.isEmpty() ? "Aucune note" : notes);
            notesLabel.setWrapText(true);
            container.getChildren().add(notesLabel);
        }
        
        return container;
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
        Object value = personnelData.get(key);
        return value != null ? value.toString() : "";
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
