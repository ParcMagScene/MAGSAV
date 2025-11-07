package com.magscene.magsav.desktop.view.planning.calendar;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import com.magscene.magsav.desktop.theme.ThemeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panneau de sélection des calendriers avec cases à cocher colorées
 * Style moderne avec codes couleur pour chaque calendrier
 */
public class CalendarSelectionPanel extends VBox {
    
    private final List<CalendarCheckBox> calendarCheckBoxes = new ArrayList<>();
    private Consumer<CalendarItem> onCalendarToggled;
    
    public static class CalendarItem {
        private final String name;
        private final String category;
        private final Color color;
        private boolean visible;
        
        public CalendarItem(String name, String category, Color color, boolean visible) {
            this.name = name;
            this.category = category;
            this.color = color;
            this.visible = visible;
        }
        
        // Getters et setters
        public String getName() { return name; }
        public String getCategory() { return category; }
        public Color getColor() { return color; }
        public boolean isVisible() { return visible; }
        public void setVisible(boolean visible) { this.visible = visible; }
    }
    
    private static class CalendarCheckBox extends CheckBox {
        private final CalendarItem calendarItem;
        private final Circle colorIndicator;
        
        public CalendarCheckBox(CalendarItem calendarItem) {
            this.calendarItem = calendarItem;
            
            // Indicateur de couleur
            colorIndicator = new Circle(6);
            colorIndicator.setFill(calendarItem.getColor());
            colorIndicator.setStroke(Color.DARKGRAY);
            colorIndicator.setStrokeWidth(0.5);
            
            setGraphic(colorIndicator);
            setText(calendarItem.getName());
            setSelected(calendarItem.isVisible());
            
            getStyleClass().add("calendar-checkbox");
            
            // Action sur changement d'état
            setOnAction(e -> {
                calendarItem.setVisible(isSelected());
                updateColorIndicator();
            });
            
            updateColorIndicator();
        }
        
        private void updateColorIndicator() {
            if (isSelected()) {
                colorIndicator.setFill(calendarItem.getColor());
                colorIndicator.setOpacity(1.0);
            } else {
                colorIndicator.setFill(ThemeManager.getInstance().isDarkTheme() ? Color.GRAY : Color.LIGHTGRAY);
                colorIndicator.setOpacity(0.5);
            }
        }
        
        public CalendarItem getCalendarItem() {
            return calendarItem;
        }
    }
    
    public CalendarSelectionPanel() {
        initializePanel();
        populateWithDefaultCalendars();
    }
    
    private void initializePanel() {
        getStyleClass().add("calendar-selection-panel");
        setPadding(new Insets(15));
        setSpacing(8);
        
        // Titre du panneau
        Label title = new Label("📋 Mes Calendriers");
        title.getStyleClass().addAll("section-title", "calendar-panel-title");
        
        getChildren().add(title);
    }
    
    private void populateWithDefaultCalendars() {
        // Calendriers par défaut avec couleurs
        addCalendar("Interventions SAV", "sav", Color.web("#FF5722"), true);
        addCalendar("Installations", "installation", Color.web("#2196F3"), true);
        addCalendar("Maintenance", "maintenance", Color.web("#FF9800"), true);
        addCalendar("Formations", "formation", Color.web("#4CAF50"), false);
        addCalendar("Réunions", "reunion", Color.web("#9C27B0"), true);
        addCalendar("Congés Personnel", "conges", Color.web("#607D8B"), false);
        addCalendar("Locations Équipement", "location", Color.web("#795548"), true);
        
        // Séparateur pour les calendriers Google
        Label googleTitle = new Label("🔗 Google Calendar");
        googleTitle.getStyleClass().addAll("section-subtitle", "google-calendar-title");
        googleTitle.setPadding(new Insets(10, 0, 5, 0));
        
        getChildren().add(googleTitle);
        
        // Calendriers Google (exemples)
        addCalendar("compte1@magsav.com", "google", Color.web("#34A853"), true);
        addCalendar("planning.technique@magsav.com", "google", Color.web("#EA4335"), false);
    }
    
    public void addCalendar(String name, String category, Color color, boolean visible) {
        CalendarItem item = new CalendarItem(name, category, color, visible);
        CalendarCheckBox checkBox = new CalendarCheckBox(item);
        
        // Action sur toggle
        checkBox.setOnAction(e -> {
            if (onCalendarToggled != null) {
                onCalendarToggled.accept(item);
            }
        });
        
        calendarCheckBoxes.add(checkBox);
        getChildren().add(checkBox);
    }
    
    /**
     * Obtenir tous les calendriers sélectionnés
     */
    public List<CalendarItem> getSelectedCalendars() {
        return calendarCheckBoxes.stream()
                .filter(cb -> cb.isSelected())
                .map(CalendarCheckBox::getCalendarItem)
                .toList();
    }
    
    /**
     * Obtenir tous les calendriers
     */
    public List<CalendarItem> getAllCalendars() {
        return calendarCheckBoxes.stream()
                .map(CalendarCheckBox::getCalendarItem)
                .toList();
    }
    
    /**
     * Sélectionner/désélectionner tous les calendriers
     */
    public void selectAll(boolean selected) {
        calendarCheckBoxes.forEach(cb -> cb.setSelected(selected));
    }
    
    /**
     * Définir l'action à effectuer lors du toggle d'un calendrier
     */
    public void setOnCalendarToggled(Consumer<CalendarItem> onCalendarToggled) {
        this.onCalendarToggled = onCalendarToggled;
    }
    
    /**
     * Mettre à jour la visibilité d'un calendrier
     */
    public void updateCalendarVisibility(String calendarName, boolean visible) {
        calendarCheckBoxes.stream()
                .filter(cb -> cb.getCalendarItem().getName().equals(calendarName))
                .findFirst()
                .ifPresent(cb -> {
                    cb.setSelected(visible);
                    cb.getCalendarItem().setVisible(visible);
                });
    }
    
    /**
     * Obtenir la couleur d'un calendrier par son nom
     */
    public Color getCalendarColor(String calendarName) {
        return calendarCheckBoxes.stream()
                .filter(cb -> cb.getCalendarItem().getName().equals(calendarName))
                .map(cb -> cb.getCalendarItem().getColor())
                .findFirst()
                .orElse(Color.GRAY);
    }
}
