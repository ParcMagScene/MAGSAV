package com.magscene.magsav.desktop.view.planning;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.theme.ThemeConstants;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Vue calendaire jour - Style cohérent avec WeekCalendarView
 * Affiche les créneaux horaires pour une journée complète
 */
public class DayCalendarView extends VBox {

    // Configuration cohérente avec WeekCalendarView
    private static final int HOUR_HEIGHT = 50;
    private static final int TIME_COLUMN_WIDTH = 80;
    private static final int START_HOUR = 7;
    private static final int END_HOUR = 20;

    private LocalDate currentDate;
    private GridPane dayGrid;
    private final List<EventBlock> eventBlocks = new ArrayList<>();
    private BiConsumer<LocalDateTime, LocalDateTime> onTimeSlotSelected;

    // Classe pour représenter les événements - style cohérent avec WeekCalendarView
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

            // Utiliser les couleurs d'agenda cohérentes avec WeekCalendarView
            String[] eventColors = getCategoryColors();
            String backgroundColor = eventColors[0];
            String borderColor = eventColors[1];

            // Style avec fond et bordure de la même couleur pour les coins harmonieux
            setStyle("-fx-background-color: " + backgroundColor + "; " +
                    "-fx-background-radius: 8; " +
                    "-fx-border-color: " + borderColor + "; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8;");

            // Calcul de la hauteur selon la durée - TAILLE FIXE GARANTIE
            long durationMinutes = java.time.Duration.between(start, end).toMinutes();
            double height = (durationMinutes / 60.0) * HOUR_HEIGHT - 2;

            // CONTRAINTES STRICTES DE TAILLE - ne peut pas grandir
            setPrefHeight(height);
            setMaxHeight(height);
            setMinHeight(height); // Force la hauteur exacte; // Largeur contrainte pour ne pas affecter les
                                  // cellules
            setMaxWidth(Region.USE_PREF_SIZE);
            setMinWidth(30); // Largeur minimum pour rester lisible; // Contenu de l'événement avec couleurs
                             // d'agenda et contraintes de taille
            String[] categoryColors = getCategoryColors();
            String textColor = categoryColors[2];

            VBox content = new VBox(1); // Espacement réduit
            content.setPadding(new Insets(2));
            content.setMaxWidth(Double.MAX_VALUE);
            content.setMaxHeight(Double.MAX_VALUE);
            // content - Style géré par CSS automatiquement; // Fond transparent pour
            // hériter de l'agenda

            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("event-title");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);
            titleLabel.setMaxHeight(Region.USE_PREF_SIZE);
            titleLabel.setStyle("-fx-text-fill: " + textColor + "; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 10px; " +
                    "-fx-background-color: transparent; " +
                    "-fx-background-radius: 0; " +
                    "-fx-padding: 0;");

            Label timeLabel = new Label(
                    start.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " - " +
                            end.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.getStyleClass().add("event-time");
            timeLabel.setWrapText(false);
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setMaxHeight(Region.USE_PREF_SIZE);
            timeLabel.setStyle("-fx-text-fill: " + textColor + "; " +
                    "-fx-font-size: 9px; " +
                    "-fx-background-color: transparent; " +
                    "-fx-background-radius: 0; " +
                    "-fx-padding: 0;");

            content.getChildren().addAll(titleLabel, timeLabel);

            // S'assurer que le contenu ne déborde jamais
            content.setMaxHeight(height - 4);
            content.setPrefHeight(height - 4);
            content.setMinHeight(height - 4);

            getChildren().add(content);
            setupEventHandlers();
        }

        private void setupEventHandlers() {
            String[] colors = getCategoryColors();
            String backgroundColor = colors[0];
            String borderColor = colors[1];

            // Pas de changement de couleur ni d'épaisseur au survol
            setOnMouseEntered(e -> {
                getStyleClass().add("event-hover");
                setStyle("-fx-background-color: " + backgroundColor + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8;");
            });

            setOnMouseExited(e -> {
                getStyleClass().remove("event-hover");
                setStyle("-fx-background-color: " + backgroundColor + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 8;");
            });

            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    // Double-clic : éditer événement
                    System.out.println("Éditer événement : " + title);
                }
            });
        }

        // Méthode pour accéder aux couleurs d'agenda
        public String[] getCategoryColors() {
            // Couleurs cohérentes avec WeekCalendarView [backgroundColor, borderColor,
            // textColor]
            return switch (category.toLowerCase()) {
                case "principal", "main" ->
                    new String[] { StandardColors.getAgendaColor("principal"), StandardColors.SECONDARY_BLUE,
                            StandardColors.LIGHT_BACKGROUND };
                case "technician", "techniciens", "sav", "intervention" ->
                    new String[] { StandardColors.getAgendaColor("technician"), StandardColors.SUCCESS_GREEN,
                            StandardColors.LIGHT_BACKGROUND };
                case "vehicle", "véhicules", "vehicules", "transport", "livraison" ->
                    new String[] { StandardColors.getAgendaColor("vehicle"), StandardColors.INFO_BLUE,
                            StandardColors.LIGHT_BACKGROUND };
                case "maintenance", "entretien", "réparation", "reparation" ->
                    new String[] { StandardColors.getAgendaColor("maintenance"), StandardColors.DANGER_RED,
                            StandardColors.LIGHT_BACKGROUND };
                case "external", "externe", "location", "partenaire", "prestataire" ->
                    new String[] { StandardColors.getAgendaColor("external"), "#9333EA",
                            StandardColors.LIGHT_BACKGROUND };
                default -> new String[] { StandardColors.PRIMARY_BLUE, StandardColors.SECONDARY_BLUE,
                        StandardColors.LIGHT_BACKGROUND };
            };
        }

        // Getters
        public String getTitle() {
            return title;
        }

        public LocalDateTime getStart() {
            return start;
        }

        public LocalDateTime getEnd() {
            return end;
        }

        public String getCategory() {
            return category;
        }
    }

    public DayCalendarView() {
        this.currentDate = LocalDate.now();
        initializeView();
    }

    private void initializeView() {
        getStyleClass().add("day-calendar");
        this.setStyle("-fx-border-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8; " +
                "-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;"); // CSS d'expansion forcée comme
                                                                                 // WeekCalendarView; // Forcer
                                                                                 // l'expansion totale sur la largeur et
                                                                                 // la hauteur - cohérent avec
                                                                                 // WeekCalendarView
        this.setMinWidth(400);
        this.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.setMaxWidth(Double.MAX_VALUE);
        this.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.setMaxHeight(Double.MAX_VALUE);

        // Créer la grille principale
        dayGrid = createDayGrid();

        // ScrollPane pour la navigation verticale
        ScrollPane scrollPane = new ScrollPane(dayGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: " + ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + ";");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Forcer l'expansion verticale et supprimer tous les paddings/espacements -
        // comme WeekCalendarView
        this.setFillWidth(true);
        this.setSpacing(0);
        this.setPadding(new Insets(0));

        this.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private GridPane createDayGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("day-grid");

        // CSS d'expansion forcée pour la grille
        grid.setStyle("-fx-pref-height: -1; -fx-max-height: -1; -fx-min-height: -1;");

        // Créer l'en-tête avec la date
        createDayHeader(grid);

        // Créer les lignes horaires
        createHourRows(grid);

        return grid;
    }

    private void createDayHeader(GridPane grid) {
        // Cellule vide pour alignement avec colonne des heures
        Label emptyCorner = new Label();
        emptyCorner.setPrefWidth(TIME_COLUMN_WIDTH);
        emptyCorner.setMinWidth(TIME_COLUMN_WIDTH);
        emptyCorner.setMaxWidth(TIME_COLUMN_WIDTH);
        // emptyCorner - Style géré par CSS automatiquement; // Header de la date
        Label dateHeader = new Label(currentDate.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy")));
        dateHeader.getStyleClass().add("day-header");
        dateHeader.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY
                + "; -fx-text-fill: " + ThemeConstants.BACKGROUND_SECONDARY
                + "; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-alignment: center; -fx-padding: 8;");
        dateHeader.setAlignment(Pos.CENTER);
        dateHeader.setMaxWidth(Double.MAX_VALUE);

        // Configuration de la grille pour l'en-tête
        grid.add(emptyCorner, 0, 0);
        grid.add(dateHeader, 1, 0);

        GridPane.setHgrow(dateHeader, Priority.ALWAYS);
        GridPane.setFillWidth(dateHeader, true);
    }

    private void createHourRows(GridPane grid) {
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            int row = hour - START_HOUR + 1; // +1 pour laisser place à l'en-tête; // Label de l'heure (colonne 0)
            Label hourLabel = new Label(String.format("%02d:00", hour));
            hourLabel.getStyleClass().add("hour-label");
            hourLabel.setStyle(
                    "-fx-text-fill: " + StandardColors.SECONDARY_BLUE + "; -fx-font-size: 12px; -fx-padding: 5; " +
                            "-fx-alignment: top-center; -fx-background-color: "
                            + ThemeConstants.BACKGROUND_PRIMARY + ";");
            hourLabel.setPrefWidth(TIME_COLUMN_WIDTH);
            hourLabel.setMinWidth(TIME_COLUMN_WIDTH);
            hourLabel.setMaxWidth(TIME_COLUMN_WIDTH);
            hourLabel.setMinHeight(HOUR_HEIGHT);
            hourLabel.setPrefHeight(HOUR_HEIGHT);
            hourLabel.setAlignment(Pos.TOP_CENTER);
            GridPane.setValignment(hourLabel, VPos.TOP);

            // Cellule de contenu pour les événements (colonne 1)
            Pane hourCell = new Pane();
            hourCell.getStyleClass().add("hour-cell");
            hourCell.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                    "-fx-border-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                    "-fx-border-width: 0 0 0.5 0;");
            hourCell.setMinHeight(HOUR_HEIGHT);
            hourCell.setPrefHeight(HOUR_HEIGHT);
            hourCell.setMaxWidth(Double.MAX_VALUE);

            // Gestion des événements de souris pour la sélection
            setupHourCellInteraction(hourCell, hour);

            grid.add(hourLabel, 0, row);
            grid.add(hourCell, 1, row);

            GridPane.setHgrow(hourCell, Priority.ALWAYS);
            GridPane.setFillWidth(hourCell, true);
        }
    }

    private void setupHourCellInteraction(Pane hourCell, int hour) {
        hourCell.setOnMouseEntered(e -> {
            hourCell.setStyle(hourCell.getStyle() + "; -fx-background-color: " + StandardColors.DARK_SECONDARY + ";");
        });

        hourCell.setOnMouseExited(e -> {
            hourCell.setStyle(
                    hourCell.getStyle().replace("; -fx-background-color: " + StandardColors.DARK_SECONDARY, ""));
        });

        hourCell.setOnMousePressed(e -> {
            LocalDateTime startTime = LocalDateTime.of(currentDate, LocalTime.of(hour, 0));
            LocalDateTime endTime = startTime.plusHours(1);

            if (onTimeSlotSelected != null) {
                onTimeSlotSelected.accept(startTime, endTime);
            }

            System.out.println("📅 Créneau sélectionné: " + startTime + " - " + endTime);
        });
    }

    // === MÉTHODES PUBLIQUES ===

    public void setCurrentDate(LocalDate date) {
        if (!date.equals(this.currentDate)) {
            this.currentDate = date;
            refresh();
        }
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setOnTimeSlotSelected(BiConsumer<LocalDateTime, LocalDateTime> callback) {
        this.onTimeSlotSelected = callback;
    }

    public void addEvent(String title, LocalDateTime startTime, LocalDateTime endTime, String category) {
        if (!startTime.toLocalDate().equals(currentDate)) {
            return; // Événement pas dans cette journée
        }

        EventBlock event = new EventBlock(title, startTime, endTime, category);
        eventBlocks.add(event);

        // Positionner l'événement dans l'interface
        positionEventInGrid(event);
    }

    private void positionEventInGrid(EventBlock event) {
        int startHour = event.getStart().getHour();
        int startMinute = event.getStart().getMinute();

        if (startHour < START_HOUR || startHour > END_HOUR) {
            return; // Hors des heures d'affichage
        }

        // Calculer la durée en heures pour le rowSpan
        long durationMinutes = java.time.Duration.between(event.getStart(), event.getEnd()).toMinutes();
        int duration = (int) Math.max(1, Math.ceil(durationMinutes / 60.0));

        // Position dans la grille - heure de début
        int hourIndex = startHour - START_HOUR;
        int gridRow = hourIndex + 1; // +1 pour l'en-tête; // Ajouter l'EventBlock directement à la grille comme dans
                                     // WeekCalendarView
        dayGrid.getChildren().add(event);

        // Configuration GridPane pour positionner l'événement
        GridPane.setColumnIndex(event, 1); // Colonne des événements (pas des heures)
        GridPane.setRowIndex(event, gridRow);
        GridPane.setRowSpan(event, duration); // Étendre sur plusieurs heures si nécessaire
        GridPane.setHalignment(event, HPos.LEFT);
        GridPane.setValignment(event, VPos.TOP);

        // Ajuster la position verticale selon les minutes
        if (startMinute > 0) {
            double minuteOffset = (startMinute / 60.0) * HOUR_HEIGHT;
            event.setTranslateY(minuteOffset);
        }

        // Contraintes de largeur pour l'événement
        event.setMaxWidth(Double.MAX_VALUE);
        event.setPrefWidth(Region.USE_COMPUTED_SIZE);

        System.out.println("📅 Événement positionné: " + event.getTitle() +
                " à la ligne " + gridRow + ", durée " + duration + "h");
    }

    public void clearEvents() {
        eventBlocks.clear();
        refresh();
    }

    public void refresh() {
        // Recréer la grille avec la nouvelle date
        dayGrid = createDayGrid();

        // Mettre à jour le ScrollPane
        if (!this.getChildren().isEmpty() && this.getChildren().get(0) instanceof ScrollPane scrollPane) {
            scrollPane.setContent(dayGrid);
        }

        // Recharger les événements
        List<EventBlock> tempEvents = new ArrayList<>(eventBlocks);
        eventBlocks.clear();
        for (EventBlock event : tempEvents) {
            addEvent(event.getTitle(), event.getStart(), event.getEnd(), event.getCategory());
        }

        System.out.println("🔄 Vue Jour rafraîchie pour: " + currentDate);
    }
}