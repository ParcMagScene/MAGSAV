package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.StandardColors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.input.MouseButton;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.Locale;

/**
 * Vue calendaire annuelle - Affiche 12 mois avec mini-calendriers
 * Permet de visualiser l'année entière et de naviguer vers un mois spécifique
 */
public class YearCalendarView extends VBox {

    private static final String[] DAYS_INITIALS = { "L", "M", "M", "J", "V", "S", "D" };

    private int currentYear;
    private GridPane yearGrid;
    private final Map<YearMonth, MiniMonthView> monthViews = new HashMap<>();
    private final List<Event> events = new ArrayList<>();
    private BiConsumer<YearMonth, LocalDate> onMonthSelected;

    // Classe pour représenter un événement
    @SuppressWarnings("unused")
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

    // Mini vue mensuelle pour la vue année
    private class MiniMonthView extends VBox {
        private final YearMonth yearMonth;
        private final GridPane daysGrid;
        private final Map<LocalDate, Label> dayLabels = new HashMap<>();

        MiniMonthView(YearMonth yearMonth) {
            this.yearMonth = yearMonth;

            getStyleClass().add("mini-month-view");
            setPadding(new Insets(5));
            setSpacing(3);
            setMinWidth(150);
            setPrefWidth(Region.USE_COMPUTED_SIZE);
            setMaxWidth(Double.MAX_VALUE);

            // Style de base
            setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                    "-fx-border-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 5; " +
                    "-fx-background-radius: 5;");

            // Header avec nom du mois
            Label monthLabel = new Label(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH));
            monthLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; " +
                    "-fx-text-fill: " + StandardColors.SECONDARY_BLUE + ";");
            monthLabel.setAlignment(Pos.CENTER);
            monthLabel.setMaxWidth(Double.MAX_VALUE);

            // Grille des jours
            daysGrid = new GridPane();
            daysGrid.setHgap(2);
            daysGrid.setVgap(2);
            daysGrid.setAlignment(Pos.CENTER);

            // En-têtes des jours (L M M J V S D)
            for (int col = 0; col < 7; col++) {
                Label dayHeader = new Label(DAYS_INITIALS[col]);
                dayHeader.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; " +
                        "-fx-text-fill: " + StandardColors.NEUTRAL_GRAY + ";");
                dayHeader.setMinWidth(18);
                dayHeader.setPrefWidth(18);
                dayHeader.setAlignment(Pos.CENTER);
                daysGrid.add(dayHeader, col, 0);
            }

            // Construire les jours du mois
            buildDays();

            getChildren().addAll(monthLabel, daysGrid);

            // Interaction au clic sur le mois entier
            setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && onMonthSelected != null) {
                    onMonthSelected.accept(yearMonth, yearMonth.atDay(1));
                }
            });

            // Effet hover
            setOnMouseEntered(e -> {
                setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                        "-fx-border-color: " + StandardColors.SECONDARY_BLUE + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;");
            });

            setOnMouseExited(e -> {
                setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                        "-fx-border-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5;");
            });
        }

        private void buildDays() {
            LocalDate firstDay = yearMonth.atDay(1);
            int dayOfWeek = firstDay.getDayOfWeek().getValue(); // 1 = Lundi, 7 = Dimanche
            int daysInMonth = yearMonth.lengthOfMonth();

            int row = 1;
            int col = dayOfWeek - 1; // Ajuster pour commencer à lundi (index 0)

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = yearMonth.atDay(day);
                Label dayLabel = new Label(String.valueOf(day));
                dayLabel.setMinWidth(18);
                dayLabel.setPrefWidth(18);
                dayLabel.setAlignment(Pos.CENTER);
                dayLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: " +
                        StandardColors.SECONDARY_BLUE + ";");

                // Mettre en évidence aujourd'hui
                if (date.equals(LocalDate.now())) {
                    dayLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; " +
                            "-fx-background-color: " + StandardColors.SECONDARY_BLUE + "; " +
                            "-fx-text-fill: " + StandardColors.LIGHT_BACKGROUND + "; " +
                            "-fx-background-radius: 9; " +
                            "-fx-padding: 2;");
                }

                dayLabels.put(date, dayLabel);
                daysGrid.add(dayLabel, col, row);

                col++;
                if (col > 6) {
                    col = 0;
                    row++;
                }
            }
        }

        void updateEvents() {
            // Réinitialiser tous les styles
            for (Map.Entry<LocalDate, Label> entry : dayLabels.entrySet()) {
                LocalDate date = entry.getKey();
                Label label = entry.getValue();

                // Compter les événements pour ce jour
                long eventCount = events.stream()
                        .filter(e -> {
                            LocalDate eventDate = e.startTime.toLocalDate();
                            return eventDate.equals(date);
                        })
                        .count();

                if (eventCount > 0) {
                    // Ajouter un indicateur visuel pour les jours avec événements
                    if (date.equals(LocalDate.now())) {
                        label.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; " +
                                "-fx-background-color: " + StandardColors.SECONDARY_BLUE + "; " +
                                "-fx-text-fill: " + StandardColors.LIGHT_BACKGROUND + "; " +
                                "-fx-background-radius: 9; " +
                                "-fx-padding: 2; " +
                                "-fx-border-color: " + StandardColors.SUCCESS_GREEN + "; " +
                                "-fx-border-width: 1; " +
                                "-fx-border-radius: 9;");
                    } else {
                        label.setStyle("-fx-font-size: 9px; " +
                                "-fx-text-fill: " + StandardColors.SECONDARY_BLUE + "; " +
                                "-fx-background-color: " + StandardColors.SUCCESS_GREEN + "40; " +
                                "-fx-background-radius: 9; " +
                                "-fx-padding: 2;");
                    }
                }
            }
        }
    }

    public YearCalendarView() {
        this.currentYear = LocalDate.now().getYear();

        getStyleClass().add("year-calendar-view");
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.TOP_CENTER);

        // Style de base
        setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + ";");

        // Permettre le scroll si nécessaire
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        buildYearView();
    }

    private void buildYearView() {
        monthViews.clear();
        getChildren().clear();

        // Titre de l'année
        Label yearLabel = new Label(String.valueOf(currentYear));
        yearLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + StandardColors.SECONDARY_BLUE + ";");
        yearLabel.setAlignment(Pos.CENTER);
        yearLabel.setMaxWidth(Double.MAX_VALUE);

        // Grille 4x3 pour les 12 mois
        yearGrid = new GridPane();
        yearGrid.setHgap(10);
        yearGrid.setVgap(10);
        yearGrid.setAlignment(Pos.CENTER);
        yearGrid.setPadding(new Insets(10));

        // Créer les 12 mini-mois
        int monthIndex = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                if (monthIndex < 12) {
                    YearMonth yearMonth = YearMonth.of(currentYear, monthIndex + 1);
                    MiniMonthView miniMonth = new MiniMonthView(yearMonth);
                    monthViews.put(yearMonth, miniMonth);
                    yearGrid.add(miniMonth, col, row);
                    GridPane.setHgrow(miniMonth, Priority.ALWAYS);
                    GridPane.setVgrow(miniMonth, Priority.ALWAYS);
                    monthIndex++;
                }
            }
        }

        getChildren().addAll(yearLabel, yearGrid);
        VBox.setVgrow(yearGrid, Priority.ALWAYS);
    }

    public void setCurrentYear(int year) {
        this.currentYear = year;
        buildYearView();
        updateAllMonths();
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public void addEvent(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        events.add(new Event(title, startTime, endTime, category));
        updateAllMonths();
    }

    public void clearEvents() {
        events.clear();
        updateAllMonths();
    }

    private void updateAllMonths() {
        for (MiniMonthView monthView : monthViews.values()) {
            monthView.updateEvents();
        }
    }

    public void setOnMonthSelected(BiConsumer<YearMonth, LocalDate> handler) {
        this.onMonthSelected = handler;
    }
}
