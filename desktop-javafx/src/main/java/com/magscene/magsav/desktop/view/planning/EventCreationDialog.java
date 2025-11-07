package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService;
import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService.PersonnelAssignment;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EventCreationDialog extends Stage {
    
    private TextField titleField;
    private DatePicker datePicker;
    private TextField startTimeField;
    private TextField endTimeField;
    private ComboBox<String> categoryComboBox;
    private ComboBox<String> specialtyComboBox;
    private ListView<PersonnelAssignment> personnelListView;
    private TextArea descriptionArea;
    private CheckBox allDayCheckBox;
    
    private SpecialtyPlanningService specialtyService;
    private EventResult result;
    
    public EventCreationDialog(SpecialtyPlanningService specialtyService) {
        this.specialtyService = specialtyService;
        
        setTitle("Créer un Événement");
        setWidth(600);
        setHeight(700);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        
        initializeComponents();
        createLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        titleField = new TextField();
        titleField.setPromptText("Titre de l'événement");
        
        datePicker = new DatePicker(LocalDate.now());
        
        startTimeField = new TextField("09:00");
        startTimeField.setPromptText("HH:mm");
        
        endTimeField = new TextField("17:00");
        endTimeField.setPromptText("HH:mm");
        
        allDayCheckBox = new CheckBox("Toute la journée");
        
        categoryComboBox = new ComboBox<>(FXCollections.observableArrayList(
            "installation", "maintenance", "formation", "reunion", "spectacle", "autre"
        ));
        categoryComboBox.setValue("installation");
        
        specialtyComboBox = new ComboBox<>(FXCollections.observableArrayList(
            specialtyService.getAllSpecialties()
        ));
        specialtyComboBox.setPromptText("Sélectionner une spécialité...");
        
        personnelListView = new ListView<>();
        personnelListView.setPrefHeight(150);
        personnelListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        personnelListView.setCellFactory(createPersonnelCellFactory());
        
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description de l'événement (optionnel)");
        descriptionArea.setPrefRowCount(3);
    }
    
    private Callback<ListView<PersonnelAssignment>, ListCell<PersonnelAssignment>> createPersonnelCellFactory() {
        return listView -> new ListCell<PersonnelAssignment>() {
            @Override
            protected void updateItem(PersonnelAssignment assignment, boolean empty) {
                super.updateItem(assignment, empty);
                
                if (empty || assignment == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    
                    Label nameLabel = new Label(assignment.getPersonnelName());
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    
                    Label specialtyLabel = new Label(assignment.getSpecialty());
                    specialtyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 11px;");
                    
                    // Indicateur de compétence
                    Label proficiencyLabel = new Label();
                    int proficiency = assignment.getProficiencyLevel();
                    String proficiencyText = "★".repeat(proficiency) + "☆".repeat(5 - proficiency);
                    proficiencyLabel.setText(proficiencyText);
                    proficiencyLabel.setStyle("-fx-text-fill: #ffa500;");
                    
                    // Indicateur de disponibilité
                    Label availabilityLabel = new Label();
                    if (assignment.isAvailable()) {
                        availabilityLabel.setText("✓ Disponible");
                        availabilityLabel.setStyle("-fx-text-fill: #008000;");
                    } else {
                        availabilityLabel.setText("✗ Occupé");
                        availabilityLabel.setStyle("-fx-text-fill: #ff4444;");
                    }
                    
                    VBox leftBox = new VBox(2);
                    leftBox.getChildren().addAll(nameLabel, specialtyLabel);
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    
                    VBox rightBox = new VBox(2);
                    rightBox.setAlignment(Pos.CENTER_RIGHT);
                    rightBox.getChildren().addAll(proficiencyLabel, availabilityLabel);
                    
                    hbox.getChildren().addAll(leftBox, spacer, rightBox);
                    setGraphic(hbox);
                }
            }
        };
    }
    
    private void createLayout() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #fafafa;");
        
        // Titre
        Label headerLabel = new Label("Nouvel Événement");
        headerLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Informations de base
        VBox basicInfo = createSection("Informations générales", 
            createFieldRow("Titre:", titleField),
            createFieldRow("Date:", datePicker),
            createTimeSection(),
            createFieldRow("Catégorie:", categoryComboBox)
        );
        
        // Spécialité et personnel
        VBox specialtyInfo = createSection("Spécialité et Personnel",
            createFieldRow("Spécialité requise:", specialtyComboBox),
            new Label("Personnel disponible:"),
            personnelListView
        );
        
        // Description
        VBox descriptionInfo = createSection("Description",
            descriptionArea
        );
        
        // Boutons
        HBox buttonBar = createButtonBar();
        
        root.getChildren().addAll(
            headerLabel,
            new Separator(),
            basicInfo,
            specialtyInfo,
            descriptionInfo,
            buttonBar
        );
        
        Scene scene = new Scene(new ScrollPane(root));
        setScene(scene);
    }
    
    private VBox createSection(String title, javafx.scene.Node... children) {
        VBox section = new VBox(10);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        section.getChildren().add(titleLabel);
        section.getChildren().addAll(children);
        
        return section;
    }
    
    private HBox createFieldRow(String labelText, javafx.scene.Node field) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(labelText);
        label.setPrefWidth(120);
        label.setStyle("-fx-font-weight: bold;");
        
        if (field instanceof Region) {
            ((Region) field).setPrefWidth(200);
        }
        
        row.getChildren().addAll(label, field);
        return row;
    }
    
    private VBox createTimeSection() {
        VBox timeSection = new VBox(5);
        
        HBox timeRow = new HBox(10);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        
        Label timeLabel = new Label("Horaires:");
        timeLabel.setPrefWidth(120);
        timeLabel.setStyle("-fx-font-weight: bold;");
        
        HBox timeFields = new HBox(5);
        timeFields.setAlignment(Pos.CENTER_LEFT);
        
        startTimeField.setPrefWidth(80);
        Label toLabel = new Label("à");
        endTimeField.setPrefWidth(80);
        
        timeFields.getChildren().addAll(startTimeField, toLabel, endTimeField);
        timeRow.getChildren().addAll(timeLabel, timeFields);
        
        timeSection.getChildren().addAll(timeRow, allDayCheckBox);
        
        return timeSection;
    }
    
    private HBox createButtonBar() {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setPadding(new Insets(10, 0, 0, 0));
        
        Button cancelButton = new Button("Annuler");
        cancelButton.setPrefWidth(100);
        cancelButton.setOnAction(e -> close());
        
        Button createButton = new Button("Créer");
        createButton.setPrefWidth(100);
        createButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        createButton.setOnAction(e -> createEvent());
        
        buttonBar.getChildren().addAll(cancelButton, createButton);
        
        return buttonBar;
    }
    
    private void setupEventHandlers() {
        // Mise à jour du personnel quand spécialité change
        specialtyComboBox.setOnAction(e -> updatePersonnelList());
        
        // Désactiver les champs d'heure si "toute la journée"
        allDayCheckBox.setOnAction(e -> {
            boolean allDay = allDayCheckBox.isSelected();
            startTimeField.setDisable(allDay);
            endTimeField.setDisable(allDay);
            
            if (allDay) {
                startTimeField.setText("00:00");
                endTimeField.setText("23:59");
            } else {
                startTimeField.setText("09:00");
                endTimeField.setText("17:00");
            }
        });
    }
    
    private void updatePersonnelList() {
        String selectedSpecialty = specialtyComboBox.getValue();
        if (selectedSpecialty != null) {
            List<PersonnelAssignment> personnel = specialtyService.getPersonnelBySpecialty(selectedSpecialty);
            personnelListView.setItems(FXCollections.observableArrayList(personnel));
        } else {
            personnelListView.getItems().clear();
        }
    }
    
    private void createEvent() {
        if (!validateInput()) {
            return;
        }
        
        try {
            String title = titleField.getText().trim();
            LocalDate date = datePicker.getValue();
            LocalTime startTime = LocalTime.parse(startTimeField.getText());
            LocalTime endTime = LocalTime.parse(endTimeField.getText());
            String category = categoryComboBox.getValue();
            String specialty = specialtyComboBox.getValue();
            String description = descriptionArea.getText().trim();
            
            LocalDateTime startDateTime = date.atTime(startTime);
            LocalDateTime endDateTime = date.atTime(endTime);
            
            List<PersonnelAssignment> selectedPersonnel = 
                personnelListView.getSelectionModel().getSelectedItems()
                    .stream()
                    .collect(Collectors.toList());
            
            result = new EventResult(title, startDateTime, endDateTime, category, 
                                   specialty, selectedPersonnel, description);
            
            close();
            
        } catch (Exception e) {
            showError("Erreur de saisie", "Vérifiez le format des heures (HH:mm)");
        }
    }
    
    private boolean validateInput() {
        if (titleField.getText().trim().isEmpty()) {
            showError("Titre requis", "Veuillez saisir un titre pour l'événement.");
            titleField.requestFocus();
            return false;
        }
        
        if (datePicker.getValue() == null) {
            showError("Date requise", "Veuillez sélectionner une date.");
            return false;
        }
        
        if (categoryComboBox.getValue() == null) {
            showError("Catégorie requise", "Veuillez sélectionner une catégorie.");
            return false;
        }
        
        // Validation des heures
        try {
            LocalTime start = LocalTime.parse(startTimeField.getText());
            LocalTime end = LocalTime.parse(endTimeField.getText());
            
            if (start.isAfter(end)) {
                showError("Horaires invalides", "L'heure de début doit être antérieure à l'heure de fin.");
                return false;
            }
        } catch (Exception e) {
            showError("Format d'heure invalide", "Utilisez le format HH:mm (ex: 09:00)");
            return false;
        }
        
        return true;
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public Optional<EventResult> getResult() {
        return Optional.ofNullable(result);
    }
    
    // Classe pour retourner les résultats
    public static class EventResult {
        private final String title;
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;
        private final String category;
        private final String specialty;
        private final List<PersonnelAssignment> selectedPersonnel;
        private final String description;
        
        public EventResult(String title, LocalDateTime startDateTime, LocalDateTime endDateTime,
                          String category, String specialty, List<PersonnelAssignment> selectedPersonnel,
                          String description) {
            this.title = title;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.category = category;
            this.specialty = specialty;
            this.selectedPersonnel = selectedPersonnel;
            this.description = description;
        }
        
        // Getters
        public String getTitle() { return title; }
        public LocalDateTime getStartDateTime() { return startDateTime; }
        public LocalDateTime getEndDateTime() { return endDateTime; }
        public String getCategory() { return category; }
        public String getSpecialty() { return specialty; }
        public List<PersonnelAssignment> getSelectedPersonnel() { return selectedPersonnel; }
        public String getDescription() { return description; }
        
        @Override
        public String toString() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return String.format("%s (%s - %s) [%s]", 
                title, 
                startDateTime.format(formatter),
                endDateTime.format(formatter),
                category
            );
        }
    }
}
