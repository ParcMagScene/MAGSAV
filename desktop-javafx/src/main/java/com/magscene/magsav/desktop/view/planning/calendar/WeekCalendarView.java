package com.magscene.magsav.desktop.view.planning.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Vue calendaire semaine - style Google Agenda
 * Permet la sélection de créneaux horaires et la création d'événements à la
 * souris
 */
public class WeekCalendarView extends VBox {

    private LocalDate startOfWeek;
    private GridPane timeGrid;
    private final List<EventBlock> eventBlocks = new ArrayList<>();
    private OnTimeSlotSelectedListener onTimeSlotSelected;

    // Propriétés pour la sélection à la souris
    private boolean isSelecting = false;
    private LocalDateTime selectionStart;
    private LocalDateTime selectionEnd;
    private Rectangle selectionRectangle;

    // Constantes de configuration
    private static final int HOUR_HEIGHT = 50; // pixels par heure - cohérent avec TimeSlots
    private static final int TIME_COLUMN_WIDTH = 80; // largeur colonne des heures
    private static final int START_HOUR = 7; // 7h du matin
    private static final int END_HOUR = 20; // 20h
    private static final int DEFAULT_TOTAL_WIDTH = 1200; // largeur par défaut; // Calcul dynamique de la largeur des
                                                         // colonnes de jours

    private double calculateDayColumnWidth() {
        // Utiliser la largeur réelle du composant ou fallback sur DEFAULT si pas encore
        // layout
        double totalWidth = getWidth() > 0 ? getWidth() : DEFAULT_TOTAL_WIDTH;
        // Largeur disponible = largeur totale - colonne heures
        double availableWidth = totalWidth - TIME_COLUMN_WIDTH;
        return Math.max(120, availableWidth / 7.0); // Minimum 120px par colonne
    }

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(LocalDateTime start, LocalDateTime end);
    }

    public WeekCalendarView(LocalDate startOfWeek) {
        this.startOfWeek = startOfWeek;
        initializeView();
    }

    private void initializeView() {
        getStyleClass().add("week-calendar");

        // Forcer l'expansion totale sur la largeur et la hauteur
        this.setMinWidth(600); // Largeur minimale pour éviter l'écrasement
        this.setPrefWidth(Region.USE_COMPUTED_SIZE);
        this.setMaxWidth(Double.MAX_VALUE);
        // Supprimer la hauteur minimale pour permettre l'expansion complète
        this.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.setMaxHeight(Double.MAX_VALUE);

        // Grille unifiée : header + créneaux horaires avec expansion forcée
        timeGrid = createUnifiedGrid();
        VBox.setVgrow(timeGrid, Priority.ALWAYS);

        // Forcer l'expansion verticale et supprimer tous les paddings/espacements
        this.setFillWidth(true);
        this.setSpacing(0);
        this.setPadding(new Insets(0));

        getChildren().add(timeGrid);

        // Listener pour recalculer les tailles quand le composant est redimensionné
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                // Recalculer les contraintes de colonnes pour la nouvelle taille
                double dayWidth = calculateDayColumnWidth();
                for (int col = 1; col <= 7; col++) {
                    ColumnConstraints colConstraint = new ColumnConstraints();
                    colConstraint.setPrefWidth(dayWidth);
                    colConstraint.setHgrow(Priority.ALWAYS);
                    if (timeGrid.getColumnConstraints().size() > col) {
                        timeGrid.getColumnConstraints().set(col, colConstraint);
                    }
                }

                // Aussi redimensionner les headers pour maintenir l'alignement
                updateHeaderWidths(dayWidth);
            }
        });

        // Listener pour forcer l'expansion verticale complète
        this.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                // Forcer la grille à prendre toute la hauteur disponible
                timeGrid.setPrefHeight(newVal.doubleValue());
            }
        });
    }

    private GridPane createUnifiedGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("week-unified-grid");

        // Pas de gap - les bordures des cellules créent la séparation
        grid.setHgap(0);
        grid.setVgap(0);

        // Prendre TOUT l'espace disponible
        grid.setPrefWidth(Region.USE_COMPUTED_SIZE);
        grid.setMinWidth(600);
        grid.setMaxWidth(Double.MAX_VALUE);

        // Expansion verticale complète sans contrainte minimale
        grid.setPrefHeight(Region.USE_COMPUTED_SIZE);
        grid.setMaxHeight(Double.MAX_VALUE);

        // Configuration des colonnes avec largeurs fixes et égales; // Colonne des
        // heures : largeur fixe
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setPrefWidth(TIME_COLUMN_WIDTH);
        timeColumn.setMinWidth(TIME_COLUMN_WIDTH);
        timeColumn.setMaxWidth(TIME_COLUMN_WIDTH);
        timeColumn.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().add(timeColumn);

        // Colonnes des jours : largeurs dynamiques avec expansion
        double dayColumnWidth = calculateDayColumnWidth();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setPrefWidth(dayColumnWidth);
            dayColumn.setMinWidth(120); // Largeur minimale raisonnable
            dayColumn.setMaxWidth(Double.MAX_VALUE); // Permettre l'expansion
            dayColumn.setHgrow(Priority.ALWAYS); // Expansion pour utiliser l'espace disponible
            grid.getColumnConstraints().add(dayColumn);
        }

        // Ligne 0 : Header avec les jours
        createHeaderRow(grid);

        // Configuration simple des lignes; // Ligne 0 : Header avec hauteur réduite
        RowConstraints headerRow = new RowConstraints(25); // Hauteur réduite
        headerRow.setVgrow(Priority.NEVER);
        headerRow.setValignment(VPos.CENTER);
        grid.getRowConstraints().add(headerRow);

        // Lignes des heures - toutes s'étendent pour utiliser l'espace disponible
        int totalHours = END_HOUR - START_HOUR + 1;
        for (int hour = START_HOUR; hour <= END_HOUR; hour++) {
            int row = hour - START_HOUR + 1;

            RowConstraints hourRow = new RowConstraints();
            hourRow.setMinHeight(HOUR_HEIGHT); // Hauteur minimale garantie
            hourRow.setPrefHeight(Region.USE_COMPUTED_SIZE); // Hauteur calculée automatiquement
            hourRow.setMaxHeight(Double.MAX_VALUE); // Permettre expansion complète
            hourRow.setVgrow(Priority.ALWAYS); // Toutes les lignes s'étendent équitablement
            grid.getRowConstraints().add(hourRow);

            // Label d'heure avec couleurs du thème et expansion verticale
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.getStyleClass().add("time-label");
            timeLabel.setAlignment(Pos.TOP_CENTER); // Alignement en haut pour correspondre aux lignes
            timeLabel.setPrefWidth(TIME_COLUMN_WIDTH);
            timeLabel.setMinWidth(TIME_COLUMN_WIDTH);
            timeLabel.setMaxWidth(TIME_COLUMN_WIDTH);
            timeLabel.setPrefHeight(HOUR_HEIGHT);
            timeLabel.setMinHeight(HOUR_HEIGHT);
            timeLabel.setMaxHeight(Double.MAX_VALUE); // Expansion verticale; // Forcer l'expansion dans la grille et
                                                      // alignement en haut
            GridPane.setVgrow(timeLabel, Priority.ALWAYS);
            GridPane.setFillHeight(timeLabel, true);
            GridPane.setValignment(timeLabel, VPos.TOP); // Alignement vertical en haut
            grid.add(timeLabel, 0, row);

            // Cellules des jours avec expansion verticale ET horizontale
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                LocalDate day = startOfWeek.plusDays(dayIndex);
                TimeSlot timeSlot = new TimeSlot(day, hour, dayIndex);

                // Forcer l'expansion verticale ET horizontale dans la grille
                GridPane.setVgrow(timeSlot, Priority.ALWAYS);
                GridPane.setHgrow(timeSlot, Priority.ALWAYS); // Expansion horizontale
                GridPane.setFillHeight(timeSlot, true);
                GridPane.setFillWidth(timeSlot, true); // Remplit toute la largeur
                grid.add(timeSlot, dayIndex + 1, row);
            }
        }

        return grid;
    }

    private void createHeaderRow(GridPane grid) {
        // Cellule vide harmonisée pour la colonne des heures
        Label emptyLabel = new Label("");
        emptyLabel.getStyleClass().add("time-label");
        emptyLabel.setPrefWidth(TIME_COLUMN_WIDTH);
        emptyLabel.setMinWidth(TIME_COLUMN_WIDTH);
        emptyLabel.setMaxWidth(TIME_COLUMN_WIDTH);
        grid.add(emptyLabel, 0, 0);

        // Headers des jours - largeurs dynamiques alignées avec les colonnes
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE dd/MM");
        double dayColumnWidth = calculateDayColumnWidth();

        for (int i = 0; i < 7; i++) {
            LocalDate day = startOfWeek.plusDays(i);

            Label dayLabel = new Label(day.format(dayFormatter));
            dayLabel.getStyleClass().add("day-header-label");
            dayLabel.setAlignment(Pos.CENTER);
            // Utiliser la même largeur que les colonnes pour un alignement parfait
            dayLabel.setMinWidth(120); // Même minimum que les colonnes
            dayLabel.setPrefWidth(dayColumnWidth); // Même largeur préférée
            dayLabel.setMaxWidth(Double.MAX_VALUE); // Même expansion possible; // Highlight aujourd'hui avec couleur
                                                    // plus discrète
            if (day.equals(LocalDate.now())) {
                dayLabel.getStyleClass().add("today-header");
            }

            // Alignement dans la grille
            GridPane.setHalignment(dayLabel, HPos.CENTER);
            GridPane.setFillWidth(dayLabel, true);

            grid.add(dayLabel, i + 1, 0);
        }
    }

    /**
     * Met à jour les largeurs des headers et cellules pour maintenir l'alignement
     * avec les colonnes
     */
    private void updateHeaderWidths(double dayWidth) {
        // Parcourir les enfants de la grille pour mettre à jour headers et cellules
        for (Node child : timeGrid.getChildren()) {
            Integer colIndex = GridPane.getColumnIndex(child);
            Integer rowIndex = GridPane.getRowIndex(child);

            // Mettre à jour les headers (ligne 0, colonnes 1-7)
            if (rowIndex != null && rowIndex == 0 && colIndex != null && colIndex >= 1 && colIndex <= 7) {
                if (child instanceof Label) {
                    Label headerLabel = (Label) child;
                    headerLabel.setPrefWidth(dayWidth);
                    headerLabel.setMinWidth(120);
                }
            }

            // Mettre à jour les TimeSlots (lignes 1+, colonnes 1-7)
            if (rowIndex != null && rowIndex > 0 && colIndex != null && colIndex >= 1 && colIndex <= 7) {
                if (child instanceof TimeSlot) {
                    TimeSlot timeSlot = (TimeSlot) child;
                    timeSlot.setPrefWidth(dayWidth);
                    timeSlot.setMinWidth(120);
                }
            }
        }
    }

    /**
     * Slot de temps cliquable pour créer des événements
     */
    private class TimeSlot extends StackPane {
        private final LocalDate date;
        private final int hour;
        private boolean isSelected = false;

        public TimeSlot(LocalDate date, int hour, int dayIndex) {
            this.date = date;
            this.hour = hour;

            // Largeur dynamique : laisser la grille gérer la largeur
            double dayColumnWidth = WeekCalendarView.this.calculateDayColumnWidth();
            setPrefWidth(dayColumnWidth);
            setMinWidth(120); // Même minimum que les colonnes
            setMaxWidth(Double.MAX_VALUE); // Permettre l'expansion comme les colonnes; // Hauteur : minimum fixe mais
                                           // peut s'étendre
            setPrefHeight(HOUR_HEIGHT);
            setMinHeight(HOUR_HEIGHT);
            setMaxHeight(Double.MAX_VALUE); // Permet l'expansion verticale; // Utiliser les classes CSS pour le thème
            getStyleClass().add("time-slot");

            // Style avec bordures selon position (éviter doubles lignes)
            String borderWidth = (dayIndex == 6) ? "0 0 1 0" : "0 1 1 0"; // Dernière colonne sans bordure droite
            setStyle("-fx-border-width: " + borderWidth + ";");

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

            setOnMousePressed(e -> {
                WeekCalendarView.this.isSelecting = true;
                WeekCalendarView.this.selectionStart = getTimeSlotDateTime();
                WeekCalendarView.this.selectionEnd = WeekCalendarView.this.selectionStart;
                e.consume();
            });

            setOnMouseDragged(e -> {
                if (WeekCalendarView.this.isSelecting) {
                    // Trouver le TimeSlot sous la souris
                    javafx.scene.Node target = e.getPickResult().getIntersectedNode();
                    if (target instanceof TimeSlot timeSlot) {
                        WeekCalendarView.this.selectionEnd = timeSlot.getTimeSlotDateTime();
                        WeekCalendarView.this.updateSelectionVisual();
                    }
                }
                e.consume();
            });

            setOnMouseReleased(e -> {
                if (WeekCalendarView.this.isSelecting && WeekCalendarView.this.selectionStart != null
                        && WeekCalendarView.this.selectionEnd != null) {
                    WeekCalendarView.this.isSelecting = false;

                    // Ajuster à la précision de 15 minutes
                    LocalDateTime adjustedStart = WeekCalendarView.this
                            .adjustToQuarterHour(WeekCalendarView.this.selectionStart);
                    LocalDateTime adjustedEnd = WeekCalendarView.this
                            .adjustToQuarterHour(WeekCalendarView.this.selectionEnd.plusMinutes(15));

                    // S'assurer que start < end
                    if (adjustedStart.isAfter(adjustedEnd)) {
                        LocalDateTime temp = adjustedStart;
                        adjustedStart = adjustedEnd;
                        adjustedEnd = temp;
                    }

                    // Notifier le listener
                    if (WeekCalendarView.this.onTimeSlotSelected != null) {
                        WeekCalendarView.this.onTimeSlotSelected.onTimeSlotSelected(adjustedStart, adjustedEnd);
                    }

                    WeekCalendarView.this.clearSelectionVisual();
                }
                e.consume();
            });
        }

        private LocalDateTime getTimeSlotDateTime() {
            return LocalDateTime.of(date, LocalTime.of(hour, 0));
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
    // Système de couleurs personnalisables
    private static Map<String, String> customAgendaColors = new HashMap<>();

    /**
     * Définit une couleur personnalisée pour un agenda
     */
    public static void setAgendaColor(String agendaKey, String color) {
        customAgendaColors.put(agendaKey, color);
    }

    /**
     * Obtient la couleur personnalisée d'un agenda
     */
    public static String getCustomAgendaColor(String agendaKey) {
        return customAgendaColors.get(agendaKey);
    }

    /**
     * Force la mise à jour visuelle de tous les événements
     */
    public void refreshEvents() {
        // Réappliquer le style unifié à tous les événements existants
        for (EventBlock event : eventBlocks) {
            applyUnifiedEventStyle(event, event.getWidth() > 0 ? event.getWidth() : 150);
        }

        // Forcer la relayout de tous les événements existants
        for (EventBlock event : eventBlocks) {
            // Recalculer le style de chaque événement
            applyUnifiedEventStyle(event, event.getPrefWidth());
        }
    }

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
            // Utiliser les classes CSS pour le thème
            getStyleClass().add("event-block");

            // Ajouter la classe spécifique selon la catégorie
            switch (category.toLowerCase()) {
                case "personnel" -> getStyleClass().add("event-block-personnel");
                case "vehicules", "véhicules" -> getStyleClass().add("event-block-vehicules");
                case "materiel", "matériel" -> getStyleClass().add("event-block-materiel");
                case "maintenance" -> getStyleClass().add("event-block-maintenance");
                case "externe" -> getStyleClass().add("event-block-externe");
            }

            // Calcul de la hauteur selon la durée - TAILLE FIXE GARANTIE
            long durationMinutes = java.time.Duration.between(start, end).toMinutes();
            double height = (durationMinutes / 60.0) * HOUR_HEIGHT - 2;

            // CONTRAINTES STRICTES DE TAILLE - ne peut pas grandir
            setPrefHeight(height);
            setMaxHeight(height);
            setMinHeight(height); // Force la hauteur exacte; // Largeur contrainte pour ne pas affecter les
                                  // cellules
            setMaxWidth(Region.USE_PREF_SIZE);
            setMinWidth(30); // Largeur minimum pour rester lisible; // Contenu de l'événement avec
                             // contraintes de taille
            VBox content = new VBox(1); // Espacement réduit
            content.setPadding(new Insets(2));
            content.setMaxWidth(Double.MAX_VALUE);
            content.setMaxHeight(Double.MAX_VALUE);
            content.setStyle("-fx-background-color: transparent;"); // Fond transparent pour hériter de l'agenda

            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("event-block-label");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);
            titleLabel.setMaxHeight(Region.USE_PREF_SIZE);

            Label timeLabel = new Label(
                    start.format(DateTimeFormatter.ofPattern("HH:mm")) +
                            " - " +
                            end.format(DateTimeFormatter.ofPattern("HH:mm")));
            timeLabel.getStyleClass().add("event-block-label");
            timeLabel.setWrapText(false);
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setMaxHeight(Region.USE_PREF_SIZE);

            content.getChildren().addAll(titleLabel, timeLabel);

            // S'assurer que le contenu ne déborde jamais
            content.setMaxHeight(height - 4);
            content.setPrefHeight(height - 4);
            content.setMinHeight(height - 4);

            getChildren().add(content);
            setupEventHandlers();
        }

        private void setupEventHandlers() {
            // Utiliser les classes CSS pour les interactions
            setOnMouseEntered(e -> {
                setStyle("");
            });

            setOnMouseExited(e -> {
                setStyle("");
            });

            setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    // Double-clic : éditer événement
                    System.out.println("Éditer événement : " + title);
                }
            });
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

        // Méthode publique pour accéder aux couleurs depuis l'extérieur
        public String[] getCategoryColors() {
            // Vérifier d'abord si une couleur personnalisée existe
            String customColor = getCustomAgendaColor(category.toLowerCase());
            if (customColor != null) {
                // Générer une couleur de bordure plus claire et garder texte blanc
                String borderColor = lightenColor(customColor, 20);
                return new String[] { customColor, borderColor, "#FFFFFF" };
            }

            // Couleurs FONCÉES et PROFESSIONNELLES par défaut [backgroundColor,
            // borderColor, textColor]
            return switch (category.toLowerCase()) {
                // Agenda Principal - Bleu foncé signature MAGSAV
                case "principal", "main" ->
                    new String[] { "#3D4BD1", "#4F5AE8", "#FFFFFF" };

                // Agenda Techniciens - Vert foncé professionnel
                case "technician", "techniciens", "sav", "intervention" ->
                    new String[] { "#166534", "#22543D", "#FFFFFF" };

                // Agenda Véhicules - Bleu foncé transport
                case "vehicle", "véhicules", "vehicules", "transport", "livraison" ->
                    new String[] { "#1E3A8A", "#2563EB", "#FFFFFF" };

                // Agenda Maintenance - Rouge foncé technique
                case "maintenance", "entretien", "réparation", "reparation" ->
                    new String[] { "#B91C1C", "#DC2626", "#FFFFFF" };

                // Agenda Externe - Violet foncé partenaires
                case "external", "externe", "location", "partenaire", "prestataire" ->
                    new String[] { "#7C2D92", "#9333EA", "#FFFFFF" };

                // Agenda Formation - Orange foncé pédagogie
                case "formation", "training", "enseignement" ->
                    new String[] { "#C2410C", "#EA580C", "#FFFFFF" };

                // Agenda Événements - Rose foncé spectacle
                case "event", "événement", "evenement", "spectacle", "show" ->
                    new String[] { "#BE185D", "#DB2777", "#FFFFFF" };

                // Agenda Réunions - Cyan foncé administration
                case "meeting", "réunion", "reunion", "administration" ->
                    new String[] { "#0E7490", "#0891B2", "#FFFFFF" };

                // Agenda Installation - Indigo foncé mise en œuvre
                case "installation", "montage", "démontage", "demontage" ->
                    new String[] { "#4338CA", "#5B21B6", "#FFFFFF" };

                // Agenda Personnel - Slate foncé RH
                case "personnel", "rh", "congé", "conge", "absence" ->
                    new String[] { "#334155", "#475569", "#FFFFFF" };

                // Fallback par défaut - Bleu foncé MAGSAV
                default -> new String[] { "#3D4BD1", "#4F5AE8", "#FFFFFF" };
            };
        }

        /**
         * Éclaircit une couleur hexadécimale de x%
         */
        private String lightenColor(String hexColor, int percentage) {
            if (hexColor == null || !hexColor.startsWith("#") || hexColor.length() != 7) {
                return hexColor;
            }

            try {
                int r = Integer.parseInt(hexColor.substring(1, 3), 16);
                int g = Integer.parseInt(hexColor.substring(3, 5), 16);
                int b = Integer.parseInt(hexColor.substring(5, 7), 16);

                r = Math.min(255, (int) (r + ((255 - r) * percentage / 100.0)));
                g = Math.min(255, (int) (g + ((255 - g) * percentage / 100.0)));
                b = Math.min(255, (int) (b + ((255 - b) * percentage / 100.0)));

                return String.format("#%02X%02X%02X", r, g, b);
            } catch (NumberFormatException e) {
                return hexColor; // Retourner la couleur originale en cas d'erreur
            }
        }
    }

    /**
     * Ajouter un événement au calendrier
     */
    public void addEvent(String title, LocalDateTime start, LocalDateTime end, String category) {
        EventBlock eventBlock = new EventBlock(title, start, end, category);
        eventBlocks.add(eventBlock);

        // Calcul de la position dans la grille unifiée
        int dayIndex = start.getDayOfWeek().getValue() - 1; // Lundi = 0
        int hourIndex = start.getHour() - START_HOUR;
        long durationMinutes = java.time.Duration.between(start, end).toMinutes();
        int duration = (int) Math.max(1, Math.ceil(durationMinutes / 60.0)); // Arrondi supérieur en heures

        if (hourIndex >= 0 && hourIndex < (END_HOUR - START_HOUR) && dayIndex >= 0 && dayIndex < 7) {
            timeGrid.getChildren().add(eventBlock);

            // Position dans la grille unifiée - +1 pour la row car row 0 = header
            GridPane.setColumnIndex(eventBlock, dayIndex + 1);
            GridPane.setRowIndex(eventBlock, hourIndex + 1); // +1 car row 0 = header
            GridPane.setRowSpan(eventBlock, duration);
            GridPane.setHalignment(eventBlock, HPos.LEFT); // Alignement à gauche pour le positionnement précis
            GridPane.setValignment(eventBlock, VPos.TOP);

            // CONTRAINTES STRICTES : L'événement ne doit JAMAIS affecter la taille des
            // cellules
            eventBlock.setMaxHeight(duration * HOUR_HEIGHT - 4); // Hauteur fixe basée sur la durée
            eventBlock.setPrefHeight(duration * HOUR_HEIGHT - 4);
            eventBlock.setMinHeight(duration * HOUR_HEIGHT - 4);

            // Recalculer les positions de TOUS les événements de ce jour pour éviter les
            // chevauchements
            repositionEventsInDay(dayIndex);
        }
    }

    /**
     * Repositionne tous les événements d'un jour pour éviter les chevauchements
     */
    private void repositionEventsInDay(int dayIndex) {
        // Collecter tous les événements de ce jour
        List<EventBlock> dayEvents = new ArrayList<>();
        for (EventBlock event : eventBlocks) {
            Integer eventColumn = GridPane.getColumnIndex(event);
            if (eventColumn != null && eventColumn.equals(dayIndex + 1)) {
                dayEvents.add(event);
            }
        }

        if (dayEvents.isEmpty())
            return;

        // Calculer la largeur réelle de la colonne en fonction de la grille
        double columnWidth = calculateActualColumnWidth();
        double margin = 3; // Marge de sécurité

        // Créer une matrice temporelle pour suivre l'occupation
        Map<Integer, List<EventBlock>> timeSlots = new HashMap<>();

        // Organiser les événements par créneaux horaires
        for (EventBlock event : dayEvents) {
            int startHour = event.getStart().getHour();
            int endHour = event.getEnd().getHour();

            // Ajouter l'événement à tous les créneaux qu'il occupe
            for (int hour = startHour; hour <= endHour; hour++) {
                timeSlots.computeIfAbsent(hour - START_HOUR, k -> new ArrayList<>()).add(event);
            }
        }

        // Calculer le nombre maximum d'événements simultanés
        int maxConcurrentEvents = timeSlots.values().stream()
                .mapToInt(List::size)
                .max().orElse(0);

        // ALGORITHME GOOGLE AGENDA : positionnement intelligent des événements
        layoutEventsGoogleStyle(dayEvents, columnWidth, margin);
    }

    // Cache pour largeur de colonne - DOIT RESTER CONSTANTE
    private static double cachedColumnWidth = 0;

    /**
     * Force le recalcul de la largeur des colonnes (à utiliser si le conteneur
     * change de taille)
     */
    public static void resetColumnWidth() {
        cachedColumnWidth = 0;
        System.out.println("🔄 Reset largeur colonnes - recalcul au prochain layout");
    }

    /**
     * Calcule la largeur ABSOLUMENT FIXE d'une colonne de jour
     */
    private double calculateActualColumnWidth() {
        // Si déjà calculée, utiliser le cache pour garantir la consistance
        if (cachedColumnWidth > 0) {
            System.out.printf("📐 Utilisation largeur cachée: %.1f px%n", cachedColumnWidth);
            return cachedColumnWidth;
        }

        // Calcul basé UNIQUEMENT sur la largeur du conteneur
        double totalWidth = timeGrid.getWidth() > 0 ? timeGrid.getWidth() : (getWidth() > 0 ? getWidth() : 900); // Valeur
                                                                                                                 // par
                                                                                                                 // défaut
                                                                                                                 // augmentée;
                                                                                                                 // // 8
                                                                                                                 // colonnes
                                                                                                                 // au
                                                                                                                 // total
                                                                                                                 // : 1
                                                                                                                 // colonne
                                                                                                                 // d'heures
                                                                                                                 // + 7
                                                                                                                 // colonnes
                                                                                                                 // de
                                                                                                                 // jours
        double timeColumnWidth = 80; // Largeur fixe pour la colonne des heures
        double availableForDays = Math.max(700, totalWidth - timeColumnWidth);
        double columnWidth = availableForDays / 7.0;

        // Largeur de colonne ABSOLUMENT FIXE
        cachedColumnWidth = Math.max(120, columnWidth); // Largeur minimum généreuse

        System.out.printf("🔒 Largeur colonne FIXÉE à: %.1f px (total: %.1f px)%n", cachedColumnWidth, totalWidth);
        return cachedColumnWidth;
    }

    /**
     * Algorithme de positionnement inspiré de Google Agenda
     * Positionne les événements de manière intelligente avec largeurs variables
     */
    private void layoutEventsGoogleStyle(List<EventBlock> dayEvents, double columnWidth, double margin) {
        if (dayEvents.isEmpty())
            return;

        // Trier les événements par heure de début, puis par durée (plus longs en
        // premier)
        dayEvents.sort((e1, e2) -> {
            int startCompare = e1.getStart().compareTo(e2.getStart());
            if (startCompare != 0)
                return startCompare;
            // Si même heure de début, événements plus longs en premier
            return Long.compare(
                    java.time.Duration.between(e2.getStart(), e2.getEnd()).toMinutes(),
                    java.time.Duration.between(e1.getStart(), e1.getEnd()).toMinutes());
        });

        // Structure pour suivre les colonnes occupées
        List<EventInfo> eventInfos = new ArrayList<>();

        // Phase 1 : Assignation des colonnes (comme Google Agenda)
        for (EventBlock event : dayEvents) {
            int column = 0;

            // Trouver la première colonne libre
            while (true) {
                boolean columnFree = true;

                for (EventInfo info : eventInfos) {
                    if (info.column == column && eventsOverlap(event, info.event)) {
                        columnFree = false;
                        break;
                    }
                }

                if (columnFree)
                    break;
                column++;
            }

            eventInfos.add(new EventInfo(event, column));
        }

        // Phase 2 : ALGORITHME SIMPLIFIÉ ET ROBUSTE
        int maxColumn = eventInfos.stream().mapToInt(info -> info.column).max().orElse(0);
        int totalColumns = maxColumn + 1;

        System.out.printf("🔧 Largeur colonne FIXE: %.1f px, %d événements simultanés%n", columnWidth, totalColumns);

        // CALCUL SIMPLE ET SÛRE : largeur strictement égale pour tous
        double availableWidth = columnWidth - (2 * margin) - 2; // -2px pour sécurité
        double spacing = 2.0; // Espacement fixe minimal
        double totalSpacing = Math.max(0, (totalColumns - 1) * spacing);
        double eventWidth = Math.max(15, (availableWidth - totalSpacing) / totalColumns);

        System.out.printf("📏 Largeur événement: %.1f px, espacement: %.1f px%n", eventWidth, spacing);

        // POSITIONNEMENT MATHÉMATIQUEMENT EXACT - AUCUN AJUSTEMENT
        for (EventInfo info : eventInfos) {
            // Position X calculée une seule fois, pas d'ajustement
            double eventX = margin + (info.column * (eventWidth + spacing));

            // VÉRIFICATION DE SÉCURITÉ ABSOLUE (ne devrait jamais être nécessaire)
            double maxX = columnWidth - margin - eventWidth;
            if (eventX > maxX) {
                System.err.printf("⚠️ ERREUR: X=%.1f dépasse limite %.1f pour colonne %d%n", eventX, maxX, info.column);
                eventX = maxX; // Forcer dans les limites
            }

            // Debug avec vérification
            System.out.printf("📍 '%s' → Col=%d, X=%.1f, W=%.1f, Fin=%.1f/%.1f%n",
                    info.event.getTitle().length() > 10 ? info.event.getTitle().substring(0, 10) + "..."
                            : info.event.getTitle(),
                    info.column, eventX, eventWidth, eventX + eventWidth, columnWidth - margin);

            setEventPosition(info.event, eventX, eventWidth);
        }
    }

    /**
     * Classe helper pour stocker les informations d'événement
     */
    private static class EventInfo {
        final EventBlock event;
        final int column;

        EventInfo(EventBlock event, int column) {
            this.event = event;
            this.column = column;
        }
    }

    /**
     * Trouve une colonne disponible pour un événement
     */
    private int findAvailableColumn(EventBlock event, Map<EventBlock, Integer> eventColumns, int maxColumns) {
        for (int column = 0; column < maxColumns; column++) {
            boolean columnAvailable = true;

            // Vérifier si cette colonne est libre pour cet événement
            for (Map.Entry<EventBlock, Integer> entry : eventColumns.entrySet()) {
                EventBlock existingEvent = entry.getKey();
                int existingColumn = entry.getValue();

                if (existingColumn == column && eventsOverlap(event, existingEvent)) {
                    columnAvailable = false;
                    break;
                }
            }

            if (columnAvailable) {
                return column;
            }
        }

        // Si aucune colonne n'est disponible, utiliser la dernière
        return maxColumns - 1;
    }

    /**
     * Vérifie si deux événements se chevauchent temporellement (version améliorée)
     */
    private boolean eventsOverlap(EventBlock event1, EventBlock event2) {
        // Amélioration : inclure les événements qui se touchent exactement comme des
        // chevauchements
        return event1.getStart().isBefore(event2.getEnd()) && event2.getStart().isBefore(event1.getEnd());
    }

    /**
     * Définit la position et la taille d'un événement avec style unifié
     */
    private void setEventPosition(EventBlock event, double xOffset, double width) {
        // Application directe des valeurs calculées (déjà sécurisées en amont)
        double finalWidth = Math.max(15, width);
        double finalXOffset = Math.max(0, xOffset);

        event.setPrefWidth(finalWidth);
        event.setMaxWidth(finalWidth);
        event.setMinWidth(15);
        event.setTranslateX(finalXOffset);

        // Style unifié avec angles harmonisés
        applyUnifiedEventStyle(event, finalWidth);
    }

    /**
     * Applique un style unifié aux événements avec angles harmonisés
     * Compatible avec TOUS les types d'agendas
     */
    private void applyUnifiedEventStyle(EventBlock event, double width) {
        String[] colors = event.getCategoryColors();
        String backgroundColor = colors[0];
        String borderColor = colors[1];
        String textColor = colors[2];

        // Style ULTRA-UNIFIÉ pour tous les agendas - ÉLIMINE TOUT FOND RÉSIDUEL
        String unifiedStyle =
                // Couleurs harmonisées - OVERRIDE COMPLET du fond
                "-fx-background-color: " + backgroundColor + " !important; " +
                        "-fx-border-color: " + borderColor + "; " +
                        "-fx-text-fill: " + textColor + "; " +

                        // Géométrie standardisée
                        "-fx-background-radius: 6; " +
                        "-fx-border-radius: 6; " +
                        "-fx-border-width: 1.5; " +
                        "-fx-background-insets: 1.5; " + // Alignement parfait fond/bordure
                        "-fx-border-insets: 0; " +

                        // Typographie unifiée
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: 500; " +
                        "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +

                        // Mise en page standardisée - ÉLIMINATION DES FONDS PARASITES
                        "-fx-alignment: center-left; " +
                        "-fx-padding: 4 8 4 8; " + // Padding généreux pour lisibilité
                        "-fx-label-padding: 0; " +
                        "-fx-control-inner-background: " + backgroundColor + "; " + // Override fond des contrôles
                        "-fx-text-background-color: " + backgroundColor + "; " + // Override fond du texte; // Effets
                                                                                 // visuels cohérents
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 2, 0.25, 0, 1); " +
                        "-fx-opacity: 0.96; " + // Légèrement opaque pour profondeur; // Contraintes de largeur
                        "-fx-min-width: 15; " +
                        "-fx-max-width: " + Math.max(width, 15) + "; " +
                        "-fx-pref-width: " + width + ";";

        event.setStyle(unifiedStyle);

        // Nettoyer récursivement tous les fonds parasites des enfants
        cleanChildrenBackgrounds(event, backgroundColor);
    }

    /**
     * Nettoie récursivement les fonds parasites de tous les enfants d'un événement
     */
    private void cleanChildrenBackgrounds(javafx.scene.Parent parent, String agendaBackgroundColor) {
        for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof javafx.scene.control.Labeled) {
                // Pour les Labels, Text, etc.
                child.setStyle(child.getStyle() +
                        "; -fx-background-color: transparent" +
                        "; -fx-control-inner-background: transparent" +
                        "; -fx-text-background-color: transparent");
            } else if (child instanceof javafx.scene.layout.Region) {
                // Pour les conteneurs (VBox, HBox, etc.)
                child.setStyle(child.getStyle() +
                        "; -fx-background-color: transparent");
            }

            // Récursion pour les enfants
            if (child instanceof javafx.scene.Parent) {
                cleanChildrenBackgrounds((javafx.scene.Parent) child, agendaBackgroundColor);
            }
        }
    }

    /**
     * Nettoyer tous les événements
     */
    public void clearEvents() {
        // Créer une copie pour éviter les modifications concurrentes
        List<EventBlock> eventsToRemove = new ArrayList<>(eventBlocks);

        // Supprimer tous les événements de la grille
        for (EventBlock event : eventsToRemove) {
            timeGrid.getChildren().remove(event);
        }

        // Vider la liste des événements
        eventBlocks.clear();

        // Debug pour vérifier le nettoyage
        System.out.println("🧹 Événements nettoyés, restant dans la grille: " +
                timeGrid.getChildren().filtered(node -> node instanceof EventBlock).size());
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

    /**
     * Ajuste une heure au quart d'heure le plus proche
     */
    private LocalDateTime adjustToQuarterHour(LocalDateTime dateTime) {
        int minutes = dateTime.getMinute();
        int adjustedMinutes = (minutes / 15) * 15; // Arrondi au quart d'heure inférieur
        return dateTime.withMinute(adjustedMinutes).withSecond(0).withNano(0);
    }

    /**
     * Met à jour l'affichage visuel de la sélection
     */
    private void updateSelectionVisual() {
        // TODO: Implémenter l'affichage de la sélection en cours; // Pour l'instant, on
        // peut utiliser un rectangle semi-transparent
        clearSelectionVisual();

        if (selectionStart != null && selectionEnd != null) {
            // Créer un rectangle de sélection
            LocalDateTime start = selectionStart.isBefore(selectionEnd) ? selectionStart : selectionEnd;
            LocalDateTime end = selectionStart.isBefore(selectionEnd) ? selectionEnd : selectionStart;

            selectionRectangle = new Rectangle();
            selectionRectangle.setFill(Color.web("#6B71F2", 0.3));
            selectionRectangle.setStroke(Color.web("#6B71F2"));
            selectionRectangle.setStrokeWidth(2);

            // TODO: Calculer position et taille basées sur start/end; // Pour l'instant,
            // juste marquer visuellement qu'il y a une sélection
        }
    }

    /**
     * Efface l'affichage de la sélection
     */
    private void clearSelectionVisual() {
        if (selectionRectangle != null && timeGrid.getChildren().contains(selectionRectangle)) {
            timeGrid.getChildren().remove(selectionRectangle);
        }
        selectionRectangle = null;
    }
}
