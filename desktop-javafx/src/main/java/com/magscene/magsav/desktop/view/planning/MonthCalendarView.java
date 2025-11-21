package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.theme.StandardColors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Vue calendaire mensuelle - Style cohérent avec WeekCalendarView
 * Affiche un calendrier mensuel avec les événements
 */
public class MonthCalendarView extends VBox {
    
    private static final String[] DAYS_OF_WEEK = {
        "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"
    };
    
    private YearMonth currentMonth;
    private GridPane monthGrid;
    private final Map<LocalDate, DayCell> dayCells = new HashMap<>();
    private final List<Event> events = new ArrayList<>();
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
        private final boolean isCurrentMonth;
        
        DayCell(LocalDate date, boolean isCurrentMonth) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            
            getStyleClass().add("day-cell");
            setMinHeight(80);
            setPrefHeight(Region.USE_COMPUTED_SIZE);
            setMaxHeight(Double.MAX_VALUE);
            setMinWidth(120);
            setPrefWidth(Region.USE_COMPUTED_SIZE);
            setMaxWidth(Double.MAX_VALUE);
            
            // Style de base
            String backgroundColor = isCurrentMonth ? ThemeManager.getInstance().getCurrentBackgroundColor() : ThemeManager.getInstance().getCurrentSecondaryColor();
            String borderColor = ThemeManager.getInstance().getCurrentSecondaryColor();
            setStyle("-fx-background-color: " + backgroundColor + "; " +
                    "-fx-border-color: " + borderColor + "; " +
                    "-fx-border-width: 0.5; " +
                    "-fx-padding: 2;");
            
            // Label du jour
            dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dayLabel.getStyleClass().add("day-number");
            String textColor = isCurrentMonth ? StandardColors.SECONDARY_BLUE : StandardColors.NEUTRAL_GRAY;
            if (date.equals(LocalDate.now())) {
                textColor = StandardColors.LIGHT_BACKGROUND;
                dayLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold; " +
                                "-fx-background-color: " + StandardColors.SECONDARY_BLUE + "; -fx-background-radius: 12; " +
                                "-fx-padding: 2 6;");
            } else {
                dayLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px;");
            }
            dayLabel.setAlignment(Pos.CENTER);
            
            // Container pour les événements
            eventsContainer = new VBox(1);
            eventsContainer.setAlignment(Pos.TOP_LEFT);
            eventsContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
            
            getChildren().addAll(dayLabel, eventsContainer);
            VBox.setVgrow(eventsContainer, Priority.ALWAYS);
            
            setupInteraction();
        }
        
        private void setupInteraction() {
            setOnMouseEntered(e -> {
                if (isCurrentMonth) {
                    setStyle(getStyle() + "; -fx-background-color: " + StandardColors.DARK_SECONDARY + ";");
                }
            });
            
            setOnMouseExited(e -> {
                String backgroundColor = isCurrentMonth ? ThemeManager.getInstance().getCurrentBackgroundColor() : ThemeManager.getInstance().getCurrentSecondaryColor();
                setStyle(getStyle().replaceAll("; -fx-background-color: " + StandardColors.DARK_SECONDARY, "") + 
                        "; -fx-background-color: " + backgroundColor + ";");
            });
        }
        
        void addEvent(Event event) {
            if (eventsContainer.getChildren().size() < 3) { // Limite à 3 événements visibles
                Label eventLabel = new Label(event.title);
                eventLabel.getStyleClass().add("month-event");
                eventLabel.setStyle("-fx-background-color: " + StandardColors.INFO_BLUE + "; -fx-text-fill: white; " +
                                  "-fx-background-radius: 3; -fx-padding: 1 4; -fx-font-size: 9px;");
                eventLabel.setMaxWidth(Double.MAX_VALUE);
                eventsContainer.getChildren().add(eventLabel);
            } else if (eventsContainer.getChildren().size() == 3) {
                // Ajouter indicateur "Plus d'événements"
                Label moreLabel = new Label("...");
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                eventsContainer.getChildren().add(moreLabel);
            }
        }
        
        void clearEvents() {
            eventsContainer.getChildren().clear();
        }
        
        @SuppressWarnings("unused")
        LocalDate getDate() {
            return date;
        }
    }
    
    public MonthCalendarView() {
        this.currentMonth = YearMonth.now();
        initializeView();
    }
    
    private void initializeView() {
        getStyleClass().add("month-calendar");
        setStyle("-fx-border-color: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;"); // CSS d'expansion forcée comme WeekCalendarView; // Forcer l'expansion totale sur la largeur et la hauteur - cohérent avec WeekCalendarView
        setMinWidth(600);
        setPrefWidth(Region.USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);
        setPrefHeight(Region.USE_COMPUTED_SIZE);
        setMaxHeight(Double.MAX_VALUE);
        
        // Forcer l'expansion verticale et supprimer tous les paddings/espacements - comme WeekCalendarView
        this.setFillWidth(true);
        this.setSpacing(0);
        this.setPadding(new Insets(0));
        
        createMonthView();
    }
    
    private void createMonthView() {
        getChildren().clear();
        
        // En-tête du mois
        createMonthHeader();
        
        // En-tête des jours de la semaine
        createWeekHeader();
        
        // Grille des jours
        createDaysGrid();
        
        VBox.setVgrow(monthGrid, Priority.ALWAYS);
    }
    
    private void createMonthHeader() {
        Label monthLabel = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthLabel.getStyleClass().add("month-header");
        monthLabel.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; -fx-text-fill: " + ThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                          "-fx-font-size: 18px; -fx-font-weight: bold; " +
                          "-fx-alignment: center; -fx-padding: 12;");
        monthLabel.setAlignment(Pos.CENTER);
        monthLabel.setMaxWidth(Double.MAX_VALUE);
        
        getChildren().add(monthLabel);
    }
    
    private void createWeekHeader() {
        HBox weekHeader = new HBox();
        weekHeader.getStyleClass().add("week-header");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        for (String dayName : DAYS_OF_WEEK) {
            Label dayLabel = new Label(dayName);
            dayLabel.getStyleClass().add("week-day-header");
            dayLabel.setStyle("-fx-text-fill: " + StandardColors.SECONDARY_BLUE + "; -fx-font-size: 11px; " +
                            "-fx-font-weight: bold; -fx-alignment: center;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(dayLabel, Priority.ALWAYS);
            
            weekHeader.getChildren().add(dayLabel);
        }
        
        getChildren().add(weekHeader);
    }
    
    private void createDaysGrid() {
        monthGrid = new GridPane();
        monthGrid.getStyleClass().add("month-grid");
        monthGrid.setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
                         "-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;"); // CSS d'expansion forcée
        
        dayCells.clear();
        
        // Calculer le premier jour à afficher (peut être du mois précédent)
        LocalDate firstOfMonth = currentMonth.atDay(1);
        LocalDate startDate = firstOfMonth.minusDays(firstOfMonth.getDayOfWeek().getValue() - 1);
        
        // Créer 6 semaines (42 jours) pour couvrir tous les cas
        for (int week = 0; week < 6; week++) {
            for (int day = 0; day < 7; day++) {
                LocalDate cellDate = startDate.plusDays(week * 7 + day);
                boolean isCurrentMonth = cellDate.getMonth() == currentMonth.getMonth();
                
                DayCell dayCell = new DayCell(cellDate, isCurrentMonth);
                dayCells.put(cellDate, dayCell);
                
                // Configuration de l'interaction
                dayCell.setOnMousePressed(e -> {
                    if (onDaySelected != null) {
                        LocalDateTime startTime = cellDate.atStartOfDay();
                        LocalDateTime endTime = startTime.plusDays(1);
                        onDaySelected.accept(startTime, endTime);
                    }
                    System.out.println("📅 Jour sélectionné: " + cellDate);
                });
                
                monthGrid.add(dayCell, day, week);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                GridPane.setFillWidth(dayCell, true);
                GridPane.setFillHeight(dayCell, true);
            }
        }
        
        getChildren().add(monthGrid);
    }
    
    // === MÉTHODES PUBLIQUES ===
    
    public void setCurrentMonth(YearMonth month) {
        if (!month.equals(this.currentMonth)) {
            this.currentMonth = month;
            refresh();
        }
    }
    
    public YearMonth getCurrentMonth() {
        return currentMonth;
    }
    
    public void setOnDaySelected(BiConsumer<LocalDateTime, LocalDateTime> callback) {
        this.onDaySelected = callback;
    }
    
    public void addEvent(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        LocalDate eventDate = startTime.toLocalDate();
        if (!eventDate.getMonth().equals(currentMonth.getMonth())) {
            return; // Événement pas dans ce mois
        }
        
        Event event = new Event(title, startTime, endTime, category);
        events.add(event);
        
        // Ajouter l'événement à la cellule correspondante
        DayCell dayCell = dayCells.get(eventDate);
        if (dayCell != null) {
            dayCell.addEvent(event);
        }
    }
    
    public void clearEvents() {
        events.clear();
        // Effacer les événements de toutes les cellules
        for (DayCell dayCell : dayCells.values()) {
            dayCell.clearEvents();
        }
    }
    
    public void refresh() {
        createMonthView();
        
        // Recharger les événements
        List<Event> tempEvents = new ArrayList<>(events);
        events.clear();
        for (Event event : tempEvents) {
            addEvent(event.title, event.startTime, event.endTime, event.category);
        }
        
        System.out.println("🔄 Vue Mois rafraîchie pour: " + currentMonth);
    }
}