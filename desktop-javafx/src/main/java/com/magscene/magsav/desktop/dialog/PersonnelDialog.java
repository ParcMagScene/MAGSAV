package com.magscene.magsav.desktop.dialog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.magscene.magsav.desktop.config.SpecialtiesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Dialogue pour la creation et modification du personnel
 */
public class PersonnelDialog extends Dialog<Map<String, Object>> {

    private final ApiService apiService;
    private final Map<String, Object> existingPersonnel;
    private final boolean isEditMode;
    private boolean isReadOnlyMode;

    // Champs du formulaire
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private TextField phoneField;
    private ComboBox<PersonnelType> typeCombo;
    private ComboBox<PersonnelStatus> statusCombo;
    private TextField jobTitleField;
    private TextField departmentField;
    private DatePicker hireDatePicker;
    private TextArea notesArea;
    private VBox specialtiesContainer;
    private final SpecialtiesConfigManager specialtiesManager;

    // Enumerations locales pour l'interface
    private enum PersonnelType {
        EMPLOYEE("Employe"),
        FREELANCE("Freelance"),
        INTERN("Stagiaire"),
        TEMPORARY("Interimaire"),
        PERFORMER("Intermittent du spectacle");

        private final String displayName;

        PersonnelType(String displayName) {
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

    private enum PersonnelStatus {
        ACTIVE("Actif"),
        INACTIVE("Inactif"),
        ON_LEAVE("En conge"),
        TERMINATED("Termine");

        private final String displayName;

        PersonnelStatus(String displayName) {
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

    public PersonnelDialog(Map<String, Object> existingPersonnel, ApiService apiService) {
        this(existingPersonnel, apiService, false);
    }

    public PersonnelDialog(Map<String, Object> existingPersonnel, ApiService apiService, boolean readOnlyMode) {
        this.apiService = apiService;
        this.existingPersonnel = existingPersonnel;
        this.isEditMode = (existingPersonnel != null && !readOnlyMode);
        this.isReadOnlyMode = readOnlyMode;
        this.specialtiesManager = SpecialtiesConfigManager.getInstance();

        if (isReadOnlyMode) {
            setTitle("Détails du personnel");
        } else {
            setTitle(isEditMode ? "Modifier le personnel" : "Nouveau personnel");
        }

        // Creation du contenu avec boutons personnalisés
        VBox mainContainer = new VBox();
        mainContainer.getChildren().add(createContent());

        if (isReadOnlyMode) {
            mainContainer.getChildren().add(createReadOnlyButtonBar());
        } else {
            mainContainer.getChildren().add(createCustomButtonBar());
        }

        getDialogPane().setContent(mainContainer);

        // Mémoriser la taille et position
        WindowPreferencesService.getInstance().setupDialogMemory(getDialogPane(), "personnel-dialog");

        // Désactiver les champs en mode lecture seule
        if (isReadOnlyMode) {
            setFieldsReadOnly();
        }

        // Chargement des donnees existantes
        if (existingPersonnel != null) {
            loadExistingData();
        }

        // Focus initial
        if (!isReadOnlyMode) {
            Platform.runLater(() -> firstNameField.requestFocus());
        }
    }

    private HBox createReadOnlyButtonBar() {
        return ViewUtils.createDialogButtonBar(
                () -> {
                    // Action Modifier : ouvrir en mode édition puis fermer ce dialog
                    PersonnelDialog editDialog = new PersonnelDialog(existingPersonnel, apiService, false);
                    editDialog.showAndWait().ifPresent(result -> {
                        // Propager le résultat vers le parent si nécessaire
                        setResult(result);
                    });
                    close();
                },
                this::close,
                null);
    }

    private HBox createCustomButtonBar() {
        return ViewUtils.createDialogButtonBar(
                this::handleSave,
                this::close,
                null);
    }

    private void handleSave() {
        if (validateForm()) {
            Map<String, Object> result = createPersonnelFromFields();
            setResult(result);
            close();
        }
    }

    /**
     * Désactive tous les champs pour le mode lecture seule
     */
    private void setFieldsReadOnly() {
        if (firstNameField != null)
            firstNameField.setDisable(true);
        if (lastNameField != null)
            lastNameField.setDisable(true);
        if (emailField != null)
            emailField.setDisable(true);
        if (phoneField != null)
            phoneField.setDisable(true);
        if (typeCombo != null)
            typeCombo.setDisable(true);
        if (statusCombo != null)
            statusCombo.setDisable(true);
        if (jobTitleField != null)
            jobTitleField.setDisable(true);
        if (departmentField != null)
            departmentField.setDisable(true);
        if (hireDatePicker != null)
            hireDatePicker.setDisable(true);
        if (notesArea != null)
            notesArea.setDisable(true);
        if (specialtiesContainer != null)
            specialtiesContainer.setDisable(true);
    }

    private ScrollPane createContent() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        // Informations personnelles
        VBox personalInfo = createPersonalInfoSection();

        // Informations professionnelles
        VBox professionalInfo = createProfessionalInfoSection();

        // Notes
        VBox notesSection = createNotesSection();

        content.getChildren().addAll(personalInfo, professionalInfo, notesSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 600);

        // Appliquer le thème dark au dialogue
        ThemeManager.getInstance().applyThemeToDialog(getDialogPane());

        return scrollPane;
    }

    private VBox createPersonalInfoSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("👤 Informations Personnelles");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2196F3;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Prenom
        grid.add(new Label("Prenom *:"), 0, 0);
        firstNameField = new TextField();
        firstNameField.setPromptText("Prenom");
        grid.add(firstNameField, 1, 0);

        // Nom
        grid.add(new Label("Nom *:"), 0, 1);
        lastNameField = new TextField();
        lastNameField.setPromptText("Nom de famille");
        grid.add(lastNameField, 1, 1);

        // Email
        grid.add(new Label("Email:"), 0, 2);
        emailField = new TextField();
        emailField.setPromptText("email@example.com");
        grid.add(emailField, 1, 2);

        // Telephone
        grid.add(new Label("Telephone:"), 0, 3);
        phoneField = new TextField();
        phoneField.setPromptText("+33 1 23 45 67 89");
        grid.add(phoneField, 1, 3);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createProfessionalInfoSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("💼 Informations Professionnelles");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #4CAF50;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        // Type
        grid.add(new Label("Type *:"), 0, 0);
        typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(PersonnelType.values()));
        typeCombo.setValue(PersonnelType.EMPLOYEE);
        grid.add(typeCombo, 1, 0);

        // Statut
        grid.add(new Label("Statut *:"), 0, 1);
        statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(PersonnelStatus.values()));
        statusCombo.setValue(PersonnelStatus.ACTIVE);
        grid.add(statusCombo, 1, 1);

        // Poste
        grid.add(new Label("Poste:"), 0, 2);
        jobTitleField = new TextField();
        jobTitleField.setPromptText("Intitule du poste");
        grid.add(jobTitleField, 1, 2);

        // Departement
        grid.add(new Label("Departement:"), 0, 3);
        departmentField = new TextField();
        departmentField.setPromptText("Nom du departement");
        grid.add(departmentField, 1, 3);

        // Date d'embauche
        grid.add(new Label("Date d'embauche:"), 0, 4);
        hireDatePicker = new DatePicker();
        hireDatePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? date.format(formatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.trim().isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        });
        grid.add(hireDatePicker, 1, 4);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createNotesSection() {
        VBox section = new VBox(10);

        Label sectionTitle = new Label("📝 Notes");
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #FF9800;");

        notesArea = new TextArea();
        notesArea.setPromptText("Notes additionnelles...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        // Section spécialités
        Label specialtiesTitle = new Label("🎯 Spécialités");
        specialtiesTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #9C27B0;");

        specialtiesContainer = createSpecialtiesCheckBoxes();

        section.getChildren().addAll(sectionTitle, notesArea, specialtiesTitle, specialtiesContainer);
        return section;
    }

    private VBox createSpecialtiesCheckBoxes() {
        VBox container = new VBox(5);

        // Grille pour les CheckBox (3 colonnes)
        GridPane checkBoxGrid = new GridPane();
        checkBoxGrid.setHgap(10);
        checkBoxGrid.setVgap(5);

        int col = 0;
        int row = 0;

        for (String specialty : specialtiesManager.getAvailableSpecialties()) {
            CheckBox checkBox = new CheckBox(specialty);
            checkBox.setUserData(specialty); // Pour retrouver la spécialité facilement

            checkBoxGrid.add(checkBox, col, row);

            col++;
            if (col >= 3) { // 3 colonnes max
                col = 0;
                row++;
            }
        }

        container.getChildren().add(checkBoxGrid);
        return container;
    }

    private void loadSpecialtiesFromData(String specialtiesData) {
        if (specialtiesData == null || specialtiesData.trim().isEmpty()) {
            return;
        }

        String[] selectedSpecialties = specialtiesData.split(",");

        // Parcourir tous les CheckBox et cocher ceux qui correspondent
        getAllCheckBoxes().forEach(checkBox -> {
            String specialty = (String) checkBox.getUserData();
            for (String selected : selectedSpecialties) {
                if (specialty.equals(selected.trim())) {
                    checkBox.setSelected(true);
                    break;
                }
            }
        });
    }

    private String getSelectedSpecialties() {
        return getAllCheckBoxes().stream()
                .filter(CheckBox::isSelected)
                .map(checkBox -> (String) checkBox.getUserData())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private java.util.List<CheckBox> getAllCheckBoxes() {
        java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();

        if (specialtiesContainer != null && !specialtiesContainer.getChildren().isEmpty()) {
            GridPane grid = (GridPane) specialtiesContainer.getChildren().get(0);
            grid.getChildren().forEach(node -> {
                if (node instanceof CheckBox) {
                    checkBoxes.add((CheckBox) node);
                }
            });
        }

        return checkBoxes;
    }

    private void loadExistingData() {
        if (existingPersonnel == null)
            return;

        firstNameField.setText(getStringValue("firstName"));
        lastNameField.setText(getStringValue("lastName"));
        emailField.setText(getStringValue("email"));
        phoneField.setText(getStringValue("phone"));
        jobTitleField.setText(getStringValue("jobTitle"));
        departmentField.setText(getStringValue("department"));
        notesArea.setText(getStringValue("notes"));
        loadSpecialtiesFromData(getStringValue("specialties"));

        // Type
        String type = getStringValue("type");
        if (type != null) {
            try {
                PersonnelType personnelType = PersonnelType.valueOf(type.toUpperCase());
                typeCombo.setValue(personnelType);
            } catch (IllegalArgumentException e) {
                // Type non reconnu, garder la valeur par defaut
            }
        }

        // Statut
        String status = getStringValue("status");
        if (status != null) {
            try {
                PersonnelStatus personnelStatus = PersonnelStatus.valueOf(status.toUpperCase());
                statusCombo.setValue(personnelStatus);
            } catch (IllegalArgumentException e) {
                // Statut non reconnu, garder la valeur par defaut
            }
        }

        // Date d'embauche
        String hireDate = getStringValue("hireDate");
        if (hireDate != null && !hireDate.trim().isEmpty()) {
            try {
                LocalDate date = LocalDate.parse(hireDate);
                hireDatePicker.setValue(date);
            } catch (Exception e) {
                // Date non valide, ignorer
            }
        }
    }

    private String getStringValue(String key) {
        Object value = existingPersonnel.get(key);
        return value != null ? value.toString() : "";
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("- Le prenom est obligatoire\n");
        }

        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }

        String email = emailField.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            errors.append("- Format d'email invalide\n");
        }

        if (typeCombo.getValue() == null) {
            errors.append("- Le type est obligatoire\n");
        }

        if (statusCombo.getValue() == null) {
            errors.append("- Le statut est obligatoire\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreurs de validation");
            alert.setHeaderText("Veuillez corriger les erreurs suivantes :");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private Map<String, Object> createPersonnelFromFields() {
        Map<String, Object> personnelData = new HashMap<>();

        personnelData.put("firstName", firstNameField.getText().trim());
        personnelData.put("lastName", lastNameField.getText().trim());
        personnelData.put("email", emailField.getText().trim());
        personnelData.put("phone", phoneField.getText().trim());
        personnelData.put("type", typeCombo.getValue().name());
        personnelData.put("status", statusCombo.getValue().name());
        personnelData.put("jobTitle", jobTitleField.getText().trim());
        personnelData.put("department", departmentField.getText().trim());
        personnelData.put("notes", notesArea.getText().trim());
        personnelData.put("specialties", getSelectedSpecialties());

        // Date d'embauche
        if (hireDatePicker.getValue() != null) {
            personnelData.put("hireDate", hireDatePicker.getValue().toString());
        }

        return personnelData;
    }
}
