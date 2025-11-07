package com.magscene.magsav.desktop.view.planning.dialog;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Dialog moderne pour créer/éditer des événements
 * Interface intuitive avec sélection de créneaux horaires
 */
public class EventCreationDialog extends Dialog<EventCreationDialog.EventResult> {
    
    // Composants de saisie
    private TextField titleField;
    private TextArea descriptionArea;
    private DatePicker startDatePicker;
    private ComboBox<String> startTimeCombo;
    private DatePicker endDatePicker;
    private ComboBox<String> endTimeCombo;
    private CheckBox allDayCheckBox;
    private ComboBox<String> calendarCombo;
    private ComboBox<String> specialtyCombo;
    private ComboBox<String> priorityCombo;
    private TextField locationField;
    
    public static class EventResult {
        public final String title;
        public final String description;
        public final LocalDateTime startDateTime;
        public final LocalDateTime endDateTime;
        public final boolean allDay;
        public final String calendar;
        public final String specialty;
        public final String priority;
        public final String location;
        
        public EventResult(String title, String description, LocalDateTime startDateTime, 
                          LocalDateTime endDateTime, boolean allDay, String calendar, 
                          String specialty, String priority, String location) {
            this.title = title;
            this.description = description;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.allDay = allDay;
            this.calendar = calendar;
            this.specialty = specialty;
            this.priority = priority;
            this.location = location;
        }
    }
    
    public EventCreationDialog() {
        this(null, null);
    }
    
    public EventCreationDialog(LocalDateTime defaultStart, LocalDateTime defaultEnd) {
        setTitle("📅 Nouvel Événement");
        setHeaderText("Créer un nouvel événement dans le planning");
        
        // Configuration du dialog
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setPrefSize(600, 500);
        
        // Contenu du dialog
        GridPane content = createContent();
        getDialogPane().setContent(content);
        
        // Pré-remplir avec les dates par défaut si fournies
        if (defaultStart != null && defaultEnd != null) {
            setDefaultTimes(defaultStart, defaultEnd);
        }
        
        // Configuration du convertisseur de résultat
        setResultConverter(this::convertResult);
        
        // Validation
        setupValidation();
    }
    
    private GridPane createContent() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        
        // Titre de l'événement
        grid.add(new Label("Titre *:"), 0, row);
        titleField = new TextField();
        titleField.setPromptText("Nom de l'événement...");
        titleField.setPrefColumnCount(30);
        grid.add(titleField, 1, row++, 2, 1);
        
        // Description
        grid.add(new Label("Description:"), 0, row);
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description détaillée...");
        descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, row++, 2, 1);
        
        // Événement toute la journée
        allDayCheckBox = new CheckBox("Événement toute la journée");
        grid.add(allDayCheckBox, 1, row++, 2, 1);
        
        // Date et heure de début
        grid.add(new Label("Début *:"), 0, row);
        HBox startBox = new HBox(8);
        startDatePicker = new DatePicker(LocalDate.now());
        startTimeCombo = new ComboBox<>();
        populateTimeCombo(startTimeCombo);
        startTimeCombo.setValue("08:00");
        startBox.getChildren().addAll(startDatePicker, startTimeCombo);
        grid.add(startBox, 1, row++, 2, 1);
        
        // Date et heure de fin
        grid.add(new Label("Fin *:"), 0, row);
        HBox endBox = new HBox(8);
        endDatePicker = new DatePicker(LocalDate.now());
        endTimeCombo = new ComboBox<>();
        populateTimeCombo(endTimeCombo);
        endTimeCombo.setValue("09:00");
        endBox.getChildren().addAll(endDatePicker, endTimeCombo);
        grid.add(endBox, 1, row++, 2, 1);
        
        // Calendrier de destination
        grid.add(new Label("Calendrier:"), 0, row);
        calendarCombo = new ComboBox<>();
        calendarCombo.getItems().addAll(
            "Interventions SAV", "Installations", "Maintenance", 
            "Formations", "Réunions", "Locations Équipment"
        );
        calendarCombo.setValue("Interventions SAV");
        grid.add(calendarCombo, 1, row++, 2, 1);
        
        // Spécialité
        grid.add(new Label("Spécialité:"), 0, row);
        specialtyCombo = new ComboBox<>();
        specialtyCombo.getItems().addAll("Éclairage", "Son", "Vidéo", "Scène", "Régie", "Généraliste");
        specialtyCombo.setValue("Généraliste");
        grid.add(specialtyCombo, 1, row++, 2, 1);
        
        // Priorité
        grid.add(new Label("Priorité:"), 0, row);
        priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("🔴 Urgent", "🟠 Haute", "🟡 Normale", "🟢 Basse");
        priorityCombo.setValue("🟡 Normale");
        grid.add(priorityCombo, 1, row++, 2, 1);
        
        // Lieu
        grid.add(new Label("Lieu:"), 0, row);
        locationField = new TextField();
        locationField.setPromptText("Adresse ou lieu de l'événement...");
        grid.add(locationField, 1, row++, 2, 1);
        
        // Configuration des colonnes
        GridPane.setHgrow(titleField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);
        
        // Gestion de l'événement "toute la journée"
        allDayCheckBox.setOnAction(e -> {
            boolean allDay = allDayCheckBox.isSelected();
            startTimeCombo.setDisable(allDay);
            endTimeCombo.setDisable(allDay);
        });
        
        return grid;
    }
    
    private void populateTimeCombo(ComboBox<String> timeCombo) {
        // Créneaux de 30 minutes de 00:00 à 23:30
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                timeCombo.getItems().add(String.format("%02d:%02d", hour, minute));
            }
        }
    }
    
    private void setDefaultTimes(LocalDateTime start, LocalDateTime end) {
        startDatePicker.setValue(start.toLocalDate());
        endDatePicker.setValue(end.toLocalDate());
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        startTimeCombo.setValue(start.format(timeFormatter));
        endTimeCombo.setValue(end.format(timeFormatter));
    }
    
    private void setupValidation() {
        // Validation du titre
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        
        okButton.disableProperty().bind(
            titleField.textProperty().isEmpty()
        );
        
        // Focus initial sur le titre
        titleField.requestFocus();
    }
    
    private EventResult convertResult(ButtonType buttonType) {
        if (buttonType != ButtonType.OK) {
            return null;
        }
        
        try {
            String title = titleField.getText().trim();
            String description = descriptionArea.getText().trim();
            boolean allDay = allDayCheckBox.isSelected();
            
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;
            
            if (allDay) {
                startDateTime = startDatePicker.getValue().atStartOfDay();
                endDateTime = endDatePicker.getValue().atTime(23, 59);
            } else {
                LocalTime startTime = LocalTime.parse(startTimeCombo.getValue());
                LocalTime endTime = LocalTime.parse(endTimeCombo.getValue());
                startDateTime = LocalDateTime.of(startDatePicker.getValue(), startTime);
                endDateTime = LocalDateTime.of(endDatePicker.getValue(), endTime);
            }
            
            String calendar = calendarCombo.getValue();
            String specialty = specialtyCombo.getValue();
            String priority = priorityCombo.getValue();
            String location = locationField.getText().trim();
            
            return new EventResult(title, description, startDateTime, endDateTime, 
                                 allDay, calendar, specialty, priority, location);
            
        } catch (Exception e) {
            // En cas d'erreur, afficher une alerte
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText("Erreur dans les données saisies");
            alert.setContentText("Veuillez vérifier les dates et heures saisies.");
            alert.showAndWait();
            
            return null;
        }
    }
    
    /**
     * Définir les calendriers disponibles
     */
    public void setAvailableCalendars(List<String> calendars) {
        calendarCombo.getItems().clear();
        calendarCombo.getItems().addAll(calendars);
        if (!calendars.isEmpty()) {
            calendarCombo.setValue(calendars.get(0));
        }
    }
    
    /**
     * Définir les spécialités disponibles
     */
    public void setAvailableSpecialties(List<String> specialties) {
        specialtyCombo.getItems().clear();
        specialtyCombo.getItems().addAll(specialties);
        if (!specialties.isEmpty()) {
            specialtyCombo.setValue(specialties.get(0));
        }
    }
}