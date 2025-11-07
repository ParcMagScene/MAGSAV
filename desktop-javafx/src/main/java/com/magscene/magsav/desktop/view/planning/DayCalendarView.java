package com.magscene.magsav.desktop.view.planning;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Vue calendaire pour un jour avec créneaux horaires
 */
public class DayCalendarView extends VBox {
    
    private static final int HOUR_HEIGHT = 60;
    private static final int START_HOUR = 6;
    private static final int END_HOUR = 23;
    
    private LocalDate currentDate;
    private final VBox hoursContainer;
    private final List<EventBlock> eventBlocks;
    private BiConsumer<LocalDateTime, LocalDateTime> onTimeSlotSelected;
    
    // Classes internes pour les créneaux
    private static class TimeSlot extends HBox {
        private final LocalTime startTime;
        private final LocalDate date;
        private boolean selected = false;
        
        public TimeSlot(LocalDate date, LocalTime startTime) {
            this.date = date;
            this.startTime = startTime;
            this.setPrefHeight(HOUR_HEIGHT);
            this.getStyleClass().addAll("time-slot", "day-time-slot");
            
            // Label de l'heure
            Label hourLabel = new Label(startTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            hourLabel.getStyleClass().add("hour-label");
            hourLabel.setPrefWidth(60);
            hourLabel.setAlignment(Pos.CENTER_RIGHT);
            
            // Zone de contenu
            Pane contentArea = new Pane();
            contentArea.getStyleClass().add("time-slot-content");
            HBox.setHgrow(contentArea, Priority.ALWAYS);
            
            this.getChildren().addAll(hourLabel, contentArea);
            
            setupMouseHandlers();
        }
        
        private void setupMouseHandlers() {
            this.setOnMouseEntered(e -> {
                if (!selected) {
                    this.getStyleClass().add("time-slot-hover");
                }
            });
            
            this.setOnMouseExited(e -> {
                this.getStyleClass().remove("time-slot-hover");
            });
        }
        
        public LocalDateTime getStartDateTime() {
            return LocalDateTime.of(date, startTime);
        }
        
        public LocalDateTime getEndDateTime() {
            return getStartDateTime().plusHours(1);
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                this.getStyleClass().add("time-slot-selected");
            } else {
                this.getStyleClass().remove("time-slot-selected");
            }
        }
    }
    
    private static class EventBlock extends Region {
        private final String title;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final String category;
        
        public EventBlock(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.category = category;
            
            this.getStyleClass().addAll("event-block", "event-" + category);
            
            // Label du titre
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("event-title");
            titleLabel.setWrapText(true);
            
            // Temps
            String timeText = startTime.format(DateTimeFormatter.ofPattern("HH:mm")) +
                             " - " + 
                             endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            Label timeLabel = new Label(timeText);
            timeLabel.getStyleClass().add("event-time");
            
            VBox content = new VBox(2, titleLabel, timeLabel);
            content.setPadding(new Insets(4));
            this.getChildren().add(content);
            
            calculatePosition();
        }
        
        private void calculatePosition() {
            int startHour = startTime.getHour();
            int startMinute = startTime.getMinute();
            int endHour = endTime.getHour();
            int endMinute = endTime.getMinute();
            
            if (startHour < START_HOUR) startHour = START_HOUR;
            if (endHour > END_HOUR) endHour = END_HOUR;
            
            double startY = (startHour - START_HOUR) * HOUR_HEIGHT + (startMinute / 60.0) * HOUR_HEIGHT;
            double endY = (endHour - START_HOUR) * HOUR_HEIGHT + (endMinute / 60.0) * HOUR_HEIGHT;
            double height = endY - startY;
            
            this.setLayoutY(startY);
            this.setPrefHeight(Math.max(height, 30)); // Hauteur minimale
        }
    }
    
    public DayCalendarView(LocalDate date) {
        this.currentDate = date;
        this.eventBlocks = new ArrayList<>();
        
        getStyleClass().add("day-calendar-view");
        
        // En-tête avec la date
        Label dateLabel = new Label(date.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        dateLabel.getStyleClass().add("day-header");
        
        // Container des heures avec scroll
        hoursContainer = new VBox();
        hoursContainer.getStyleClass().add("hours-container");
        
        ScrollPane scrollPane = new ScrollPane(hoursContainer);
        scrollPane.getStyleClass().add("day-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        this.getChildren().addAll(dateLabel, scrollPane);
        
        createTimeSlots();
        
        // Défiler vers 8h au démarrage
        scrollPane.setVvalue(0.2);
    }
    
    private void createTimeSlots() {
        hoursContainer.getChildren().clear();
        
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            LocalTime time = LocalTime.of(hour, 0);
            TimeSlot timeSlot = new TimeSlot(currentDate, time);
            
            // Gérer les clics pour sélection de créneaux
            timeSlot.setOnMouseClicked(this::handleTimeSlotClick);
            
            hoursContainer.getChildren().add(timeSlot);
            
            // Ajouter une ligne de séparation
            if (hour < END_HOUR) {
                Region separator = new Region();
                separator.getStyleClass().add("hour-separator");
                separator.setPrefHeight(1);
                hoursContainer.getChildren().add(separator);
            }
        }
    }
    
    private void handleTimeSlotClick(MouseEvent event) {
        if (event.getSource() instanceof TimeSlot timeSlot) {
            LocalDateTime startTime = timeSlot.getStartDateTime();
            LocalDateTime endTime = timeSlot.getEndDateTime();
            
            if (onTimeSlotSelected != null) {
                onTimeSlotSelected.accept(startTime, endTime);
            }
        }
    }
    
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        createTimeSlots();
        refreshEvents();
    }
    
    public LocalDate getCurrentDate() {
        return currentDate;
    }
    
    public void addEvent(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        // Vérifier si l'événement est pour ce jour
        if (!startTime.toLocalDate().equals(currentDate)) {
            return;
        }
        
        EventBlock eventBlock = new EventBlock(title, startTime, endTime, category);
        eventBlocks.add(eventBlock);
        
        // Ajouter l'événement à la zone de contenu appropriée
        addEventToTimeSlot(eventBlock, startTime);
    }
    
    private void addEventToTimeSlot(EventBlock eventBlock, LocalDateTime startTime) {
        // Rechercher le TimeSlot correspondant à l'heure de début
        for (javafx.scene.Node node : hoursContainer.getChildren()) {
            if (node instanceof TimeSlot timeSlot) {
                LocalTime slotTime = timeSlot.startTime;
                if (slotTime.getHour() == startTime.getHour()) {
                    // Ajouter l'événement à la zone de contenu
                    if (timeSlot.getChildren().size() > 1) {
                        Pane contentArea = (Pane) timeSlot.getChildren().get(1);
                        eventBlock.setLayoutX(70); // Décalage après l'heure
                        
                        // Définir la largeur initiale
                        eventBlock.setPrefWidth(Math.max(200, contentArea.getWidth() - 80));
                        
                        contentArea.getChildren().add(eventBlock);
                        
                        // Ajuster la largeur quand la zone change de taille
                        contentArea.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                            eventBlock.setPrefWidth(Math.max(200, newWidth.doubleValue() - 80));
                        });
                        
                        return;
                    }
                }
            }
        }
    }
    
    public void clearEvents() {
        eventBlocks.clear();
        
        // Supprimer tous les événements des TimeSlots
        for (javafx.scene.Node node : hoursContainer.getChildren()) {
            if (node instanceof TimeSlot timeSlot && timeSlot.getChildren().size() > 1) {
                Pane contentArea = (Pane) timeSlot.getChildren().get(1);
                contentArea.getChildren().clear();
            }
        }
    }
    
    private void refreshEvents() {
        clearEvents();
        // Ici on rechargerait les événements depuis la source de données
        // Pour l'instant, on ne fait que nettoyer
    }
    
    public void setOnTimeSlotSelected(BiConsumer<LocalDateTime, LocalDateTime> callback) {
        this.onTimeSlotSelected = callback;
    }
    
    // Méthodes utilitaires
    public void scrollToTime(LocalTime time) {
        if (time.getHour() >= START_HOUR && time.getHour() <= END_HOUR) {
            double position = (double)(time.getHour() - START_HOUR) / (END_HOUR - START_HOUR + 1);
            
            // Rechercher le ScrollPane parent
            ScrollPane scrollPane = findScrollPane();
            if (scrollPane != null) {
                scrollPane.setVvalue(position);
            }
        }
    }
    
    private ScrollPane findScrollPane() {
        javafx.scene.Node parent = this.getParent();
        while (parent != null) {
            if (parent instanceof ScrollPane) {
                return (ScrollPane) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }
}
