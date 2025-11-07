package com.magscene.magsav.desktop.view.planning;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Vue calendaire mensuelle avec grille de jours
 */
public class MonthCalendarView extends VBox {
    
    private static final String[] DAYS_OF_WEEK = {
        "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"
    };
    
    private YearMonth currentMonth;
    private final GridPane calendarGrid;
    private final Label monthLabel;
    private final Map<LocalDate, DayCell> dayCells;
    private final List<Event> events;
    private BiConsumer<LocalDateTime, LocalDateTime> onDaySelected;
    
    // Classe pour représenter un événement
    private static class Event {
        final String title;
        final LocalDateTime startTime;
        final LocalDateTime endTime;
        final String category;
        
        Event(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
            this.title = title;
            this.startTime = startTime;
            this.endTime = endTime;
            this.category = category;
        }
    }
    
    // Cellule pour chaque jour du mois
    private static class DayCell extends VBox {
        private final LocalDate date;
        private final Label dayLabel;
        private final VBox eventsContainer;
        private final List<Event> dayEvents;
        private boolean isCurrentMonth;
        private boolean isToday;
        private boolean selected;
        
        public DayCell(LocalDate date, boolean isCurrentMonth) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            this.isToday = date.equals(LocalDate.now());
            this.dayEvents = new ArrayList<>();
            
            getStyleClass().add("day-cell");
            if (!isCurrentMonth) {
                getStyleClass().add("other-month");
            }
            if (isToday) {
                getStyleClass().add("today");
            }
            
            this.setPrefSize(120, 100);
            this.setMinSize(100, 80);
            this.setPadding(new Insets(2));
            
            // Label du numéro du jour
            dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dayLabel.getStyleClass().add("day-number");
            if (isToday) {
                dayLabel.getStyleClass().add("today-number");
            }
            
            // Container pour les événements
            eventsContainer = new VBox(1);
            eventsContainer.getStyleClass().add("day-events");
            eventsContainer.setFillWidth(true);
            
            VBox.setVgrow(eventsContainer, Priority.ALWAYS);
            
            this.getChildren().addAll(dayLabel, eventsContainer);
            
            setupMouseHandlers();
        }
        
        private void setupMouseHandlers() {
            this.setOnMouseEntered(e -> {
                if (!selected && isCurrentMonth) {
                    this.getStyleClass().add("day-cell-hover");
                }
            });
            
            this.setOnMouseExited(e -> {
                this.getStyleClass().remove("day-cell-hover");
            });
        }
        
        public void addEvent(Event event) {
            if (dayEvents.size() >= 3) {
                // Afficher un indicateur "plus d'événements"
                if (dayEvents.size() == 3) {
                    Label moreLabel = new Label("+" + (dayEvents.size() - 2) + " autres");
                    moreLabel.getStyleClass().addAll("event-indicator", "more-events");
                    eventsContainer.getChildren().add(moreLabel);
                } else {
                    // Mettre à jour le compteur
                    Label moreLabel = (Label) eventsContainer.getChildren().get(eventsContainer.getChildren().size() - 1);
                    moreLabel.setText("+" + (dayEvents.size() - 2) + " autres");
                }
            } else {
                // Afficher l'événement
                Label eventLabel = new Label(event.title);
                eventLabel.getStyleClass().addAll("event-indicator", "event-" + event.category);
                eventLabel.setMaxWidth(Double.MAX_VALUE);
                eventLabel.setWrapText(false);
                
                // Tooltip avec plus d'informations
                String tooltipText = event.title + "\\n" + 
                                   event.startTime.format(DateTimeFormatter.ofPattern("HH:mm")) + 
                                   " - " + 
                                   event.endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(tooltipText);
                javafx.scene.control.Tooltip.install(eventLabel, tooltip);
                
                eventsContainer.getChildren().add(eventLabel);
            }
            
            dayEvents.add(event);
        }
        
        public void clearEvents() {
            dayEvents.clear();
            eventsContainer.getChildren().clear();
        }
        
        public LocalDate getDate() {
            return date;
        }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                this.getStyleClass().add("day-cell-selected");
            } else {
                this.getStyleClass().remove("day-cell-selected");
            }
        }
        
        public boolean isCurrentMonth() {
            return isCurrentMonth;
        }
        
        public List<Event> getEvents() {
            return new ArrayList<>(dayEvents);
        }
    }
    
    public MonthCalendarView(YearMonth month) {
        this.currentMonth = month;
        this.dayCells = new HashMap<>();
        this.events = new ArrayList<>();
        
        getStyleClass().add("month-calendar-view");
        
        // En-tête du mois
        monthLabel = new Label();
        monthLabel.getStyleClass().add("month-header");
        updateMonthLabel();
        
        // En-têtes des jours de la semaine
        HBox daysHeader = createDaysHeader();
        
        // Grille du calendrier
        calendarGrid = new GridPane();
        calendarGrid.getStyleClass().add("month-grid");
        calendarGrid.setHgap(1);
        calendarGrid.setVgap(1);
        
        VBox.setVgrow(calendarGrid, Priority.ALWAYS);
        
        this.getChildren().addAll(monthLabel, daysHeader, calendarGrid);
        
        createCalendarGrid();
    }
    
    private HBox createDaysHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("days-header");
        header.setAlignment(Pos.CENTER);
        
        for (String day : DAYS_OF_WEEK) {
            Label dayLabel = new Label(day);
            dayLabel.getStyleClass().add("day-header");
            dayLabel.setPrefWidth(120);
            dayLabel.setAlignment(Pos.CENTER);
            header.getChildren().add(dayLabel);
        }
        
        return header;
    }
    
    private void createCalendarGrid() {
        calendarGrid.getChildren().clear();
        dayCells.clear();
        
        // Première date à afficher (lundi de la première semaine)
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        LocalDate startDate = firstDayOfMonth.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1);
        
        // Dernière date à afficher
        LocalDate lastDayOfMonth = currentMonth.atEndOfMonth();
        LocalDate endDate = lastDayOfMonth.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 7);
        
        LocalDate date = startDate;
        int row = 0;
        
        while (!date.isAfter(endDate)) {
            for (int col = 0; col < 7; col++) {
                boolean isCurrentMonth = YearMonth.from(date).equals(currentMonth);
                DayCell dayCell = new DayCell(date, isCurrentMonth);
                
                dayCell.setOnMouseClicked(this::handleDayClick);
                
                dayCells.put(date, dayCell);
                calendarGrid.add(dayCell, col, row);
                
                // Configurer les contraintes de colonne
                if (row == 0) {
                    ColumnConstraints colConstraints = new ColumnConstraints();
                    colConstraints.setHgrow(Priority.ALWAYS);
                    colConstraints.setFillWidth(true);
                    colConstraints.setMinWidth(100);
                    calendarGrid.getColumnConstraints().add(colConstraints);
                }
                
                date = date.plusDays(1);
            }
            
            // Configurer les contraintes de ligne
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setVgrow(Priority.ALWAYS);
            rowConstraints.setFillHeight(true);
            rowConstraints.setMinHeight(80);
            calendarGrid.getRowConstraints().add(rowConstraints);
            
            row++;
        }
        
        refreshEvents();
    }
    
    private void handleDayClick(MouseEvent event) {
        if (event.getSource() instanceof DayCell dayCell) {
            LocalDateTime startTime = dayCell.getDate().atTime(9, 0); // Heure par défaut
            LocalDateTime endTime = startTime.plusHours(1);
            
            // Désélectionner les autres cellules
            dayCells.values().forEach(cell -> cell.setSelected(false));
            
            // Sélectionner cette cellule
            dayCell.setSelected(true);
            
            if (onDaySelected != null) {
                onDaySelected.accept(startTime, endTime);
            }
        }
    }
    
    private void updateMonthLabel() {
        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    }
    
    public void setCurrentMonth(YearMonth month) {
        this.currentMonth = month;
        updateMonthLabel();
        createCalendarGrid();
    }
    
    public YearMonth getCurrentMonth() {
        return currentMonth;
    }
    
    public void addEvent(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        Event event = new Event(title, startTime, endTime, category);
        events.add(event);
        
        // Ajouter l'événement aux cellules concernées
        LocalDate startDate = startTime.toLocalDate();
        LocalDate endDate = endTime.toLocalDate();
        
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            DayCell dayCell = dayCells.get(date);
            if (dayCell != null) {
                dayCell.addEvent(event);
            }
            date = date.plusDays(1);
        }
    }
    
    public void clearEvents() {
        events.clear();
        dayCells.values().forEach(DayCell::clearEvents);
    }
    
    private void refreshEvents() {
        // Nettoyer les événements existants
        dayCells.values().forEach(DayCell::clearEvents);
        
        // Ré-ajouter tous les événements
        for (Event event : events) {
            LocalDate startDate = event.startTime.toLocalDate();
            LocalDate endDate = event.endTime.toLocalDate();
            
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                DayCell dayCell = dayCells.get(date);
                if (dayCell != null) {
                    dayCell.addEvent(event);
                }
                date = date.plusDays(1);
            }
        }
    }
    
    public void setOnDaySelected(BiConsumer<LocalDateTime, LocalDateTime> callback) {
        this.onDaySelected = callback;
    }
    
    // Méthodes utilitaires
    public void goToPreviousMonth() {
        setCurrentMonth(currentMonth.minusMonths(1));
    }
    
    public void goToNextMonth() {
        setCurrentMonth(currentMonth.plusMonths(1));
    }
    
    public void goToToday() {
        YearMonth today = YearMonth.now();
        if (!today.equals(currentMonth)) {
            setCurrentMonth(today);
        }
        
        // Sélectionner aujourd'hui
        LocalDate todayDate = LocalDate.now();
        DayCell todayCell = dayCells.get(todayDate);
        if (todayCell != null) {
            dayCells.values().forEach(cell -> cell.setSelected(false));
            todayCell.setSelected(true);
        }
    }
    
    public List<Event> getEventsForDate(LocalDate date) {
        DayCell dayCell = dayCells.get(date);
        return dayCell != null ? dayCell.getEvents() : new ArrayList<>();
    }
}