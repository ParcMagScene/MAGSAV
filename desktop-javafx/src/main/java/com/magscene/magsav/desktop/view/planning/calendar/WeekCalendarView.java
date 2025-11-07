package com.magscene.magsav.desktop.view.planning.calendar;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import com.magscene.magsav.desktop.theme.ThemeManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Vue calendaire semaine - style Google Agenda
 * Permet la sélection de créneaux horaires et la création d'événements à la souris
 */
public class WeekCalendarView extends VBox {
    
    private LocalDate startOfWeek;
    private GridPane timeGrid;
    private final List<EventBlock> eventBlocks = new ArrayList<>();
    private OnTimeSlotSelectedListener onTimeSlotSelected;
    
    // Constantes de configuration
    private static final int HOUR_HEIGHT = 60; // pixels par heure
    private static final int DAY_WIDTH = 120; // largeur de colonne
    private static final int START_HOUR = 6; // 6h du matin
    private static final int END_HOUR = 22; // 22h
    
    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(LocalDateTime start, LocalDateTime end);
    }
    
    public WeekCalendarView(LocalDate startOfWeek) {
        this.startOfWeek = startOfWeek;
        initializeView();
    }
    
    private void initializeView() {
        getStyleClass().add("week-calendar");
        
        // Header avec les jours de la semaine
        HBox weekHeader = createWeekHeader();
        
        // Grille horaire principale
        ScrollPane scrollPane = new ScrollPane();
        timeGrid = createTimeGrid();
        scrollPane.setContent(timeGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(600);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        getChildren().addAll(weekHeader, scrollPane);
        
        // Défilement initial vers 8h du matin
        scrollPane.setVvalue(2.0 / (END_HOUR - START_HOUR));
    }
    
    private HBox createWeekHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("week-header");
        header.setPadding(new Insets(10));
        
        // Colonne vide pour les heures
        Region timeColumn = new Region();
        timeColumn.setPrefWidth(60);
        header.getChildren().add(timeColumn);
        
        // Colonnes des jours
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);
            
            VBox dayColumn = new VBox(5);
            dayColumn.setPrefWidth(DAY_WIDTH);
            dayColumn.setAlignment(Pos.CENTER);
            dayColumn.getStyleClass().add("day-column");
            
            Label dayName = new Label(day.format(dayFormatter));
            dayName.getStyleClass().add("day-name");
            
            // Highlight aujourd'hui
            if (day.equals(LocalDate.now())) {
                dayColumn.getStyleClass().add("today");
                dayName.getStyleClass().add("today-label");
            }
            
            dayColumn.getChildren().add(dayName);
            header.getChildren().add(dayColumn);
        }
        
        return header;
    }
    
    private GridPane createTimeGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("time-grid");
        
        // Configuration des colonnes
        ColumnConstraints timeColumn = new ColumnConstraints(60);
        grid.getColumnConstraints().add(timeColumn);
        
        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints(DAY_WIDTH);
            grid.getColumnConstraints().add(dayColumn);
        }
        
        // Création des lignes horaires
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            int row = hour - START_HOUR;
            
            // Colonne des heures
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.getStyleClass().add("time-label");
            grid.add(timeLabel, 0, row);
            
            // Colonnes des jours avec slots de temps
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                LocalDate day = startOfWeek.plusDays(dayIndex);
                TimeSlot timeSlot = new TimeSlot(day, hour);
                
                grid.add(timeSlot, dayIndex + 1, row);
            }
        }
        
        return grid;
    }
    
    /**
     * Slot de temps cliquable pour créer des événements
     */
    private class TimeSlot extends StackPane {
        private final LocalDate date;
        private final int hour;
        private boolean isSelected = false;
        
        public TimeSlot(LocalDate date, int hour) {
            this.date = date;
            this.hour = hour;
            
            setPrefSize(DAY_WIDTH, HOUR_HEIGHT);
            getStyleClass().add("time-slot");
            
            // Rectangle de fond
            Rectangle background = new Rectangle(DAY_WIDTH - 2, HOUR_HEIGHT - 2);
            background.setFill(Color.TRANSPARENT);
            background.setStroke(ThemeManager.getInstance().isDarkTheme() ? Color.GRAY : Color.LIGHTGRAY);
            background.setStrokeWidth(0.5);
            
            getChildren().add(background);
            
            setupMouseHandlers();
        }
        
        private void setupMouseHandlers() {
            setOnMouseEntered(e -> {
                if (!isSelected) {
                    getStyleClass().add("time-slot-hover");
                }
            });
            
            setOnMouseExited(e -> {
                getStyleClass().remove("time-slot-hover");
            });
            
            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    // Double-clic : créer événement d'1 heure
                    LocalDateTime start = LocalDateTime.of(date, LocalTime.of(hour, 0));
                    LocalDateTime end = start.plusHours(1);
                    
                    if (onTimeSlotSelected != null) {
                        onTimeSlotSelected.onTimeSlotSelected(start, end);
                    }
                } else {
                    // Simple clic : sélection
                    toggleSelection();
                }
            });
        }
        
        private void toggleSelection() {
            isSelected = !isSelected;
            if (isSelected) {
                getStyleClass().add("time-slot-selected");
            } else {
                getStyleClass().remove("time-slot-selected");
            }
        }
        
        public LocalDateTime getStartTime() {
            return LocalDateTime.of(date, LocalTime.of(hour, 0));
        }
        
        public LocalDateTime getEndTime() {
            return LocalDateTime.of(date, LocalTime.of(hour + 1, 0));
        }
    }
    
    /**
     * Bloc d'événement affiché sur le calendrier
     */
    public static class EventBlock extends StackPane {
        private final String title;
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final String category;
        
        public EventBlock(String title, LocalDateTime start, LocalDateTime end, String category) {
            this.title = title;
            this.start = start;
            this.end = end;
            this.category = category;
            
            initializeEventBlock();
        }
        
        private void initializeEventBlock() {
            getStyleClass().addAll("event-block", "event-" + category.toLowerCase());
            
            // Calcul de la hauteur selon la durée
            long durationMinutes = java.time.Duration.between(start, end).toMinutes();
            double height = (durationMinutes / 60.0) * HOUR_HEIGHT - 2;
            setPrefHeight(height);
            setMaxHeight(height);
            
            // Contenu de l'événement
            VBox content = new VBox(2);
            content.setPadding(new Insets(4));
            
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("event-title");
            titleLabel.setWrapText(true);
            
            Label timeLabel = new Label(
                start.format(DateTimeFormatter.ofPattern("HH:mm")) + 
                " - " + 
                end.format(DateTimeFormatter.ofPattern("HH:mm"))
            );
            timeLabel.getStyleClass().add("event-time");
            
            content.getChildren().addAll(titleLabel, timeLabel);
            getChildren().add(content);
            
            setupEventHandlers();
        }
        
        private void setupEventHandlers() {
            setOnMouseEntered(e -> getStyleClass().add("event-hover"));
            setOnMouseExited(e -> getStyleClass().remove("event-hover"));
            
            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    // Double-clic : éditer événement
                    System.out.println("Éditer événement : " + title);
                }
            });
        }
        
        // Getters
        public String getTitle() { return title; }
        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
        public String getCategory() { return category; }
    }
    
    /**
     * Ajouter un événement au calendrier
     */
    public void addEvent(String title, LocalDateTime start, LocalDateTime end, String category) {
        EventBlock eventBlock = new EventBlock(title, start, end, category);
        
        // Calcul de la position dans la grille
        int dayIndex = start.getDayOfWeek().getValue() - 1; // Lundi = 0
        int hourIndex = start.getHour() - START_HOUR;
        
        if (hourIndex >= 0 && hourIndex < (END_HOUR - START_HOUR) && dayIndex >= 0 && dayIndex < 7) {
            // Position dans la grille
            GridPane.setColumnIndex(eventBlock, dayIndex + 1);
            GridPane.setRowIndex(eventBlock, hourIndex);
            GridPane.setValignment(eventBlock, VPos.TOP);
            
            // Ajout à la grille avec décalage pour éviter les chevauchements
            timeGrid.getChildren().add(eventBlock);
            eventBlocks.add(eventBlock);
        }
    }
    
    /**
     * Nettoyer tous les événements
     */
    public void clearEvents() {
        timeGrid.getChildren().removeAll(eventBlocks);
        eventBlocks.clear();
    }
    
    /**
     * Changer la semaine affichée
     */
    public void setStartOfWeek(LocalDate newStartOfWeek) {
        this.startOfWeek = newStartOfWeek;
        clearEvents();
        
        // Recréer le header
        getChildren().clear();
        initializeView();
    }
    
    // Getters/Setters
    public LocalDate getStartOfWeek() {
        return startOfWeek;
    }
    
    public void setOnTimeSlotSelected(OnTimeSlotSelectedListener listener) {
        this.onTimeSlotSelected = listener;
    }
}