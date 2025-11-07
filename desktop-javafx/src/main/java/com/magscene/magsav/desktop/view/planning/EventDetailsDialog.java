package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService.PersonnelAssignment;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.magscene.magsav.desktop.theme.ThemeManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EventDetailsDialog extends Stage {
    
    private final EventInfo eventInfo;
    
    public EventDetailsDialog(EventInfo eventInfo) {
        this.eventInfo = eventInfo;
        
        setTitle("Détails de l'Événement");
        setWidth(500);
        setHeight(600);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        
        createLayout();
    }
    
    private void createLayout() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #fafafa;");
        
        // En-tête avec titre et catégorie
        VBox header = createHeader();
        
        // Informations temporelles
        VBox timeInfo = createTimeSection();
        
        // Spécialité et personnel
        VBox specialtyInfo = createSpecialtySection();
        
        // Description
        VBox descriptionInfo = createDescriptionSection();
        
        // Actions
        HBox actionBar = createActionBar();
        
        root.getChildren().addAll(
            header,
            new Separator(),
            timeInfo,
            specialtyInfo,
            descriptionInfo,
            actionBar
        );
        
        Scene scene = new Scene(new ScrollPane(root));
        setScene(scene);
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        
        Label titleLabel = new Label(eventInfo.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label categoryLabel = new Label("Catégorie: " + formatCategory(eventInfo.getCategory()));
        categoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        // Badge de statut
        Label statusBadge = new Label(getStatusText());
        statusBadge.setStyle(getStatusStyle());
        statusBadge.setPadding(new Insets(4, 8, 4, 8));
        
        header.getChildren().addAll(titleLabel, categoryLabel, statusBadge);
        return header;
    }
    
    private VBox createTimeSection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("Horaires");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH:mm");
        
        HBox timeBox = new HBox(15);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label startIcon = new Label("🕐");
        Label startLabel = new Label("Début: " + eventInfo.getStartDateTime().format(formatter));
        startLabel.setStyle("-fx-font-size: 12px;");
        
        VBox startBox = new VBox(5);
        startBox.getChildren().addAll(
            new HBox(5, startIcon, new Label("Début")),
            new Label(eventInfo.getStartDateTime().format(formatter))
        );
        
        Label endIcon = new Label("🕒");
        VBox endBox = new VBox(5);
        endBox.getChildren().addAll(
            new HBox(5, endIcon, new Label("Fin")),
            new Label(eventInfo.getEndDateTime().format(formatter))
        );
        
        // Durée calculée
        long durationMinutes = java.time.Duration.between(
            eventInfo.getStartDateTime(), 
            eventInfo.getEndDateTime()
        ).toMinutes();
        
        Label durationIcon = new Label("⏱️");
        VBox durationBox = new VBox(5);
        durationBox.getChildren().addAll(
            new HBox(5, durationIcon, new Label("Durée")),
            new Label(formatDuration(durationMinutes))
        );
        
        timeBox.getChildren().addAll(startBox, endBox, durationBox);
        
        section.getChildren().addAll(sectionTitle, timeBox);
        return section;
    }
    
    private VBox createSpecialtySection() {
        VBox section = new VBox(10);
        
        Label sectionTitle = new Label("Spécialité et Personnel");
        sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
        
        // Spécialité requise
        HBox specialtyBox = new HBox(10);
        specialtyBox.setAlignment(Pos.CENTER_LEFT);
        
        Label specialtyIcon = new Label("🔧");
        Label specialtyLabel = new Label("Spécialité: " + 
            (eventInfo.getSpecialty() != null ? eventInfo.getSpecialty() : "Non spécifiée"));
        specialtyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");
        
        specialtyBox.getChildren().addAll(specialtyIcon, specialtyLabel);
        
        section.getChildren().addAll(sectionTitle, specialtyBox);
        
        // Personnel assigné
        if (eventInfo.getAssignedPersonnel() != null && !eventInfo.getAssignedPersonnel().isEmpty()) {
            Label personnelTitle = new Label("Personnel assigné:");
            personnelTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
            section.getChildren().add(personnelTitle);
            
            for (PersonnelAssignment person : eventInfo.getAssignedPersonnel()) {
                HBox personBox = createPersonnelBox(person);
                section.getChildren().add(personBox);
            }
        } else {
            Label noPersonnelLabel = new Label("Aucun personnel assigné");
            noPersonnelLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
            section.getChildren().add(noPersonnelLabel);
        }
        
        return section;
    }
    
    private HBox createPersonnelBox(PersonnelAssignment person) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5));
        box.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentUIColor() + "; -fx-border-color: #ecf0f1; -fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        Label avatarLabel = new Label("👤");
        
        VBox infoBox = new VBox(2);
        
        Label nameLabel = new Label(person.getPersonnelName());
        nameLabel.setStyle("-fx-font-weight: bold;");
        
        Label roleLabel = new Label(person.getSpecialty());
        roleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
        
        infoBox.getChildren().addAll(nameLabel, roleLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        VBox statusBox = new VBox(2);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        
        // Niveau de compétence
        String stars = "★".repeat(person.getProficiencyLevel()) + 
                      "☆".repeat(5 - person.getProficiencyLevel());
        Label skillLabel = new Label(stars);
        skillLabel.setStyle("-fx-text-fill: #f39c12;");
        
        // Statut de disponibilité
        Label availabilityLabel = new Label();
        if (person.isAvailable()) {
            availabilityLabel.setText("✓ Disponible");
            availabilityLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 10px;");
        } else {
            availabilityLabel.setText("✗ Occupé");
            availabilityLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
        }
        
        statusBox.getChildren().addAll(skillLabel, availabilityLabel);
        
        box.getChildren().addAll(avatarLabel, infoBox, spacer, statusBox);
        
        return box;
    }
    
    private VBox createDescriptionSection() {
        VBox section = new VBox(10);
        
        if (eventInfo.getDescription() != null && !eventInfo.getDescription().trim().isEmpty()) {
            Label sectionTitle = new Label("Description");
            sectionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #34495e;");
            
            TextArea descriptionArea = new TextArea(eventInfo.getDescription());
            descriptionArea.setEditable(false);
            descriptionArea.setWrapText(true);
            descriptionArea.setPrefRowCount(3);
            descriptionArea.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; -fx-border-color: #ecf0f1;");
            
            section.getChildren().addAll(sectionTitle, descriptionArea);
        }
        
        return section;
    }
    
    private HBox createActionBar() {
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        actionBar.setPadding(new Insets(10, 0, 0, 0));
        
        Button editButton = new Button("Modifier");
        editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        editButton.setOnAction(e -> {
            // TODO: Ouvrir dialogue de modification
            close();
        });
        
        Button deleteButton = new Button("Supprimer");
        deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> {
            // TODO: Confirmer et supprimer l'événement
            close();
        });
        
        Button closeButton = new Button("Fermer");
        closeButton.setPrefWidth(80);
        closeButton.setOnAction(e -> close());
        
        actionBar.getChildren().addAll(editButton, deleteButton, closeButton);
        
        return actionBar;
    }
    
    private String formatCategory(String category) {
        return switch (category) {
            case "installation" -> "Installation";
            case "maintenance" -> "Maintenance";
            case "formation" -> "Formation";
            case "reunion" -> "Réunion";
            case "spectacle" -> "Spectacle";
            case "sav" -> "SAV";
            default -> category;
        };
    }
    
    private String getStatusText() {
        LocalDateTime now = LocalDateTime.now();
        if (eventInfo.getEndDateTime().isBefore(now)) {
            return "Terminé";
        } else if (eventInfo.getStartDateTime().isBefore(now)) {
            return "En cours";
        } else {
            return "Planifié";
        }
    }
    
    private String getStatusStyle() {
        LocalDateTime now = LocalDateTime.now();
        if (eventInfo.getEndDateTime().isBefore(now)) {
            return "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 10px;";
        } else if (eventInfo.getStartDateTime().isBefore(now)) {
            return "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 10px;";
        } else {
            return "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 10px;";
        }
    }
    
    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " h";
            } else {
                return hours + " h " + remainingMinutes + " min";
            }
        }
    }
    
    // Classe pour stocker les informations d'un événement
    public static class EventInfo {
        private final String title;
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;
        private final String category;
        private final String specialty;
        private final List<PersonnelAssignment> assignedPersonnel;
        private final String description;
        
        public EventInfo(String title, LocalDateTime startDateTime, LocalDateTime endDateTime,
                        String category, String specialty, List<PersonnelAssignment> assignedPersonnel,
                        String description) {
            this.title = title;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.category = category;
            this.specialty = specialty;
            this.assignedPersonnel = assignedPersonnel;
            this.description = description;
        }
        
        // Getters
        public String getTitle() { return title; }
        public LocalDateTime getStartDateTime() { return startDateTime; }
        public LocalDateTime getEndDateTime() { return endDateTime; }
        public String getCategory() { return category; }
        public String getSpecialty() { return specialty; }
        public List<PersonnelAssignment> getAssignedPersonnel() { return assignedPersonnel; }
        public String getDescription() { return description; }
    }
}
