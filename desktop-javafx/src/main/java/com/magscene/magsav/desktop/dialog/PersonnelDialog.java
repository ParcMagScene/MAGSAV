package com.magscene.magsav.desktop.dialog;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.magscene.magsav.desktop.component.AvatarView;
import com.magscene.magsav.desktop.component.CustomTabPane;
import com.magscene.magsav.desktop.config.SpecialtiesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.WindowPreferencesService;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;
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
import javafx.scene.control.Separator;
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
    
    // Avatar
    private AvatarView avatarView;
    private String currentAvatarPath;

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

        @SuppressWarnings("unused")
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

        @SuppressWarnings("unused")
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

        // Configuration du titre
        if (isReadOnlyMode) {
            setTitle("Détails du personnel");
            setHeaderText(null);
        } else {
            setTitle(isEditMode ? "Modifier le personnel" : "Nouveau personnel");
            setHeaderText(null);
        }

        // Taille réduite grâce aux onglets
        getDialogPane().setPrefSize(600, 450);

        // Creation du contenu avec onglets
        VBox mainContainer = new VBox();
        mainContainer.getChildren().add(createTabbedContent());

        // Barre de boutons personnalisée
        if (isReadOnlyMode) {
            mainContainer.getChildren().add(createReadOnlyButtonBar());
        } else {
            mainContainer.getChildren().add(createCustomButtonBar());
        }

        getDialogPane().setContent(mainContainer);

        // Appliquer le thème
        UnifiedThemeManager.getInstance().applyThemeToDialog(getDialogPane());

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
        return ViewUtils.createReadOnlyButtonBar(
                () -> {
                    // Action Modifier : ouvrir en mode édition puis fermer ce dialog
                    PersonnelDialog editDialog = new PersonnelDialog(existingPersonnel, apiService, false);
                    editDialog.showAndWait().ifPresent(result -> {
                        // Propager le résultat vers le parent si nécessaire
                        setResult(result);
                    });
                    close();
                },
                this::close);
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

    /**
     * Crée le contenu avec onglets comme EquipmentDialog
     */
    private CustomTabPane createTabbedContent() {
        CustomTabPane tabPane = new CustomTabPane();

        // Onglet 1: Informations personnelles
        CustomTabPane.CustomTab personalTab = 
            new CustomTabPane.CustomTab("Personnel", createPersonalInfoForm(), "👤");
        tabPane.addTab(personalTab);

        // Onglet 2: Informations professionnelles
        CustomTabPane.CustomTab professionalTab = 
            new CustomTabPane.CustomTab("Professionnel", createProfessionalInfoForm(), "💼");
        tabPane.addTab(professionalTab);

        // Onglet 3: Spécialités et notes
        CustomTabPane.CustomTab notesTab = 
            new CustomTabPane.CustomTab("Compétences", createNotesForm(), "🎯");
        tabPane.addTab(notesTab);

        tabPane.selectTab(0);
        return tabPane;
    }

    private VBox createPersonalInfoForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        
        // Section Avatar en haut
        HBox avatarSection = new HBox(20);
        avatarSection.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Créer l'avatar
        String personId = existingPersonnel != null ? 
            String.valueOf(existingPersonnel.getOrDefault("id", "new")) : "new";
        String firstName = existingPersonnel != null ? 
            (String) existingPersonnel.getOrDefault("firstName", "") : "";
        String lastName = existingPersonnel != null ? 
            (String) existingPersonnel.getOrDefault("lastName", "") : "";
        String avatarPath = existingPersonnel != null ? 
            (String) existingPersonnel.getOrDefault("avatarPath", null) : null;
        
        avatarView = AvatarView.createFormAvatar(personId, firstName, lastName, avatarPath);
        avatarView.setEditable(!isReadOnlyMode);
        avatarView.setOnAvatarChanged(path -> {
            currentAvatarPath = path;
            System.out.println("👤 Avatar modifié: " + path);
        });
        currentAvatarPath = avatarPath;
        
        VBox avatarInfo = new VBox(5);
        Label avatarLabel = new Label("Photo de profil");
        avatarLabel.getStyleClass().add("form-label");
        avatarLabel.setStyle("-fx-font-weight: bold;");
        
        Label avatarHint = new Label(isReadOnlyMode ? "" : "Double-clic pour modifier");
        avatarHint.setStyle("-fx-text-fill: #6C757D; -fx-font-size: 11px;");
        
        avatarInfo.getChildren().addAll(avatarLabel, avatarHint);
        avatarSection.getChildren().addAll(avatarView, avatarInfo);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        int row = 0;

        // Prénom
        Label firstNameLabel = new Label("Prénom *");
        firstNameLabel.getStyleClass().add("form-label");
        firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");
        // Mettre à jour l'avatar quand le prénom change
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (avatarView != null) {
                avatarView.setPerson(personId, newVal, lastNameField.getText(), currentAvatarPath);
            }
        });
        grid.add(firstNameLabel, 0, row);
        grid.add(firstNameField, 1, row++);

        // Nom
        Label lastNameLabel = new Label("Nom *");
        lastNameLabel.getStyleClass().add("form-label");
        lastNameField = new TextField();
        lastNameField.setPromptText("Nom de famille");
        // Mettre à jour l'avatar quand le nom change
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (avatarView != null) {
                avatarView.setPerson(personId, firstNameField.getText(), newVal, currentAvatarPath);
            }
        });
        grid.add(lastNameLabel, 0, row);
        grid.add(lastNameField, 1, row++);

        // Email
        Label emailLabel = new Label("Email");
        emailLabel.getStyleClass().add("form-label");
        emailField = new TextField();
        emailField.setPromptText("email@example.com");
        grid.add(emailLabel, 0, row);
        grid.add(emailField, 1, row++);

        // Téléphone
        Label phoneLabel = new Label("Téléphone");
        phoneLabel.getStyleClass().add("form-label");
        phoneField = new TextField();
        phoneField.setPromptText("+33 1 23 45 67 89");
        grid.add(phoneLabel, 0, row);
        grid.add(phoneField, 1, row++);

        container.getChildren().addAll(
            avatarSection,
            new Separator(),
            new Label("Informations personnelles"),
            new Separator(),
            grid
        );

        return container;
    }

    private VBox createProfessionalInfoForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        int row = 0;

        // Type
        Label typeLabel = new Label("Type *");
        typeLabel.getStyleClass().add("form-label");
        typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(PersonnelType.values()));
        typeCombo.setValue(PersonnelType.EMPLOYEE);
        typeCombo.setPrefWidth(200);
        grid.add(typeLabel, 0, row);
        grid.add(typeCombo, 1, row++);

        // Statut
        Label statusLabel = new Label("Statut *");
        statusLabel.getStyleClass().add("form-label");
        statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(PersonnelStatus.values()));
        statusCombo.setValue(PersonnelStatus.ACTIVE);
        statusCombo.setPrefWidth(200);
        grid.add(statusLabel, 0, row);
        grid.add(statusCombo, 1, row++);

        // Poste
        Label jobLabel = new Label("Poste");
        jobLabel.getStyleClass().add("form-label");
        jobTitleField = new TextField();
        jobTitleField.setPromptText("Intitulé du poste");
        grid.add(jobLabel, 0, row);
        grid.add(jobTitleField, 1, row++);

        // Département
        Label deptLabel = new Label("Département");
        deptLabel.getStyleClass().add("form-label");
        departmentField = new TextField();
        departmentField.setPromptText("Nom du département");
        grid.add(deptLabel, 0, row);
        grid.add(departmentField, 1, row++);

        // Date d'embauche
        Label hireDateLabel = new Label("Date d'embauche");
        hireDateLabel.getStyleClass().add("form-label");
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
        grid.add(hireDateLabel, 0, row);
        grid.add(hireDatePicker, 1, row++);

        container.getChildren().addAll(
            new Label("Informations professionnelles"),
            new Separator(),
            grid
        );

        return container;
    }

    private VBox createNotesForm() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));

        // Notes
        Label notesLabel = new Label("Notes");
        notesLabel.getStyleClass().add("form-label");
        notesArea = new TextArea();
        notesArea.setPromptText("Notes additionnelles...");
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        // Spécialités
        Label specialtiesLabel = new Label("Spécialités");
        specialtiesLabel.getStyleClass().add("form-label");
        specialtiesContainer = createSpecialtiesCheckBoxes();

        container.getChildren().addAll(
            new Label("Notes et compétences"),
            new Separator(),
            notesLabel,
            notesArea,
            specialtiesLabel,
            specialtiesContainer
        );

        return container;
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
        
        // Avatar
        if (currentAvatarPath != null && !currentAvatarPath.isEmpty()) {
            personnelData.put("avatarPath", currentAvatarPath);
        }

        // Date d'embauche
        if (hireDatePicker.getValue() != null) {
            personnelData.put("hireDate", hireDatePicker.getValue().toString());
        }

        return personnelData;
    }
}
