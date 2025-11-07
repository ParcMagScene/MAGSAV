package com.magscene.magsav.desktop.model;

import javafx.beans.property.*;
import java.time.LocalDate;

/**
 * Paramètres de vue pour l'affichage du planning
 */
public class ViewParameters {
    
    // Mode de vue
    private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(ViewMode.WEEK);
    
    // Date actuelle affichée
    private final ObjectProperty<LocalDate> currentDate = new SimpleObjectProperty<>(LocalDate.now());
    
    // Configuration d'affichage
    private final BooleanProperty showWeekends = new SimpleBooleanProperty(true);
    private final BooleanProperty showAllDayEvents = new SimpleBooleanProperty(true);
    private final BooleanProperty show24HourFormat = new SimpleBooleanProperty(true);
    
    // Heures de travail
    private final IntegerProperty workDayStartHour = new SimpleIntegerProperty(8);
    private final IntegerProperty workDayEndHour = new SimpleIntegerProperty(18);
    
    // Zoom et affichage
    private final DoubleProperty zoomLevel = new SimpleDoubleProperty(1.0);
    private final IntegerProperty hoursPerRow = new SimpleIntegerProperty(1); // Pour la vue journalière
    
    public enum ViewMode {
        DAY("Jour"),
        WEEK("Semaine"), 
        MONTH("Mois"),
        AGENDA("Agenda"),
        TIMELINE("Timeline");
        
        private final String label;
        
        ViewMode(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    // === PROPRIÉTÉS ===
    
    public ViewMode getViewMode() { return viewMode.get(); }
    public void setViewMode(ViewMode viewMode) { this.viewMode.set(viewMode); }
    public ObjectProperty<ViewMode> viewModeProperty() { return viewMode; }
    
    public LocalDate getCurrentDate() { return currentDate.get(); }
    public void setCurrentDate(LocalDate currentDate) { this.currentDate.set(currentDate); }
    public ObjectProperty<LocalDate> currentDateProperty() { return currentDate; }
    
    public boolean isShowWeekends() { return showWeekends.get(); }
    public void setShowWeekends(boolean show) { this.showWeekends.set(show); }
    public BooleanProperty showWeekendsProperty() { return showWeekends; }
    
    public boolean isShowAllDayEvents() { return showAllDayEvents.get(); }
    public void setShowAllDayEvents(boolean show) { this.showAllDayEvents.set(show); }
    public BooleanProperty showAllDayEventsProperty() { return showAllDayEvents; }
    
    public boolean isShow24HourFormat() { return show24HourFormat.get(); }
    public void setShow24HourFormat(boolean show) { this.show24HourFormat.set(show); }
    public BooleanProperty show24HourFormatProperty() { return show24HourFormat; }
    
    public int getWorkDayStartHour() { return workDayStartHour.get(); }
    public void setWorkDayStartHour(int hour) { this.workDayStartHour.set(hour); }
    public IntegerProperty workDayStartHourProperty() { return workDayStartHour; }
    
    public int getWorkDayEndHour() { return workDayEndHour.get(); }
    public void setWorkDayEndHour(int hour) { this.workDayEndHour.set(hour); }
    public IntegerProperty workDayEndHourProperty() { return workDayEndHour; }
    
    public double getZoomLevel() { return zoomLevel.get(); }
    public void setZoomLevel(double zoom) { this.zoomLevel.set(Math.max(0.5, Math.min(3.0, zoom))); }
    public DoubleProperty zoomLevelProperty() { return zoomLevel; }
    
    public int getHoursPerRow() { return hoursPerRow.get(); }
    public void setHoursPerRow(int hours) { this.hoursPerRow.set(Math.max(1, Math.min(4, hours))); }
    public IntegerProperty hoursPerRowProperty() { return hoursPerRow; }
    
    // === MÉTHODES DE NAVIGATION ===
    
    public void navigateNext() {
        LocalDate current = getCurrentDate();
        switch (getViewMode()) {
            case DAY:
                setCurrentDate(current.plusDays(1));
                break;
            case WEEK:
                setCurrentDate(current.plusWeeks(1));
                break;
            case MONTH:
                setCurrentDate(current.plusMonths(1));
                break;
            default:
                setCurrentDate(current.plusDays(1));
        }
    }
    
    public void navigatePrevious() {
        LocalDate current = getCurrentDate();
        switch (getViewMode()) {
            case DAY:
                setCurrentDate(current.minusDays(1));
                break;
            case WEEK:
                setCurrentDate(current.minusWeeks(1));
                break;
            case MONTH:
                setCurrentDate(current.minusMonths(1));
                break;
            default:
                setCurrentDate(current.minusDays(1));
        }
    }
    
    public void navigateToToday() {
        setCurrentDate(LocalDate.now());
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Retourne le début de la période affichée selon le mode de vue
     */
    public LocalDate getPeriodStart() {
        LocalDate current = getCurrentDate();
        switch (getViewMode()) {
            case DAY:
                return current;
            case WEEK:
                return current.minusDays(current.getDayOfWeek().getValue() - 1);
            case MONTH:
                return current.withDayOfMonth(1);
            default:
                return current;
        }
    }
    
    /**
     * Retourne la fin de la période affichée selon le mode de vue
     */
    public LocalDate getPeriodEnd() {
        LocalDate current = getCurrentDate();
        switch (getViewMode()) {
            case DAY:
                return current;
            case WEEK:
                LocalDate weekStart = current.minusDays(current.getDayOfWeek().getValue() - 1);
                return weekStart.plusDays(6);
            case MONTH:
                return current.withDayOfMonth(current.lengthOfMonth());
            default:
                return current;
        }
    }
    
    /**
     * Retourne le titre de la période actuelle
     */
    public String getPeriodTitle() {
        LocalDate current = getCurrentDate();
        switch (getViewMode()) {
            case DAY:
                return current.format(java.time.format.DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy"));
            case WEEK:
                LocalDate start = getPeriodStart();
                LocalDate end = getPeriodEnd();
                if (start.getMonth() == end.getMonth()) {
                    return String.format("Semaine du %d au %d %s %d", 
                        start.getDayOfMonth(), end.getDayOfMonth(), 
                        start.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH),
                        start.getYear());
                } else {
                    return String.format("Semaine du %d %s au %d %s %d", 
                        start.getDayOfMonth(), 
                        start.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.FRENCH),
                        end.getDayOfMonth(),
                        end.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.FRENCH),
                        end.getYear());
                }
            case MONTH:
                return current.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
            default:
                return current.toString();
        }
    }
    
    /**
     * Calcule le nombre de jours à afficher selon le mode
     */
    public int getDisplayedDaysCount() {
        switch (getViewMode()) {
            case DAY:
                return 1;
            case WEEK:
                return isShowWeekends() ? 7 : 5;
            case MONTH:
                return getCurrentDate().lengthOfMonth();
            default:
                return 7;
        }
    }
}