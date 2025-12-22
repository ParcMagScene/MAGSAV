package com.magscene.magsav.desktop.view.vehicle;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.service.VehicleReservationService;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue du planning de disponibilités des véhicules
 * Affiche un calendrier mensuel/hebdomadaire avec matin/après-midi par véhicule
 * Supporte la sélection par glissement et la modification des réservations
 */
public class VehicleAvailabilityView extends VBox {

    // Constantes de couleurs pour les demi-journées
    private static final String MORNING_COLOR = "#E3F2FD";      // Bleu très clair
    private static final String AFTERNOON_COLOR = "#F5F5F5";    // Gris très clair
    private static final String TODAY_BORDER_COLOR = "#2196F3"; // Bleu pour le jour actuel
    private static final String WEEKEND_BG_COLOR = "#FAFAFA";   // Gris très léger pour weekend
    private static final String SELECTION_COLOR = "#BBDEFB";    // Bleu clair pour sélection
    
    // Couleurs par véhicule pour les réservations
    private static final String[] VEHICLE_COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7",
        "#DDA0DD", "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9"
    };

    // Mode de vue
    public enum ViewMode { WEEK, MONTH }
    private ViewMode currentViewMode = ViewMode.MONTH;

    // Composants UI
    private GridPane calendarGrid;
    private ScrollPane scrollPane;
    private YearMonth currentMonth;
    private LocalDate currentWeekStart;

    // Données
    private final ApiService apiService;
    private final VehicleReservationService reservationService;
    private final ObservableList<VehicleAvailabilityItem> vehicles = FXCollections.observableArrayList();
    private final Map<String, String> vehicleColors = new HashMap<>();
    
    // Listener pour les changements de réservations
    private final Consumer<String> reservationChangeListener;

    // Sélection par glissement
    private boolean isDragging = false;
    private DragSelection currentDragSelection = null;
    private final List<StackPane> highlightedCells = new ArrayList<>();
    private final Map<String, StackPane> cellRegistry = new HashMap<>();
    
    // Overlay pour réservations fusionnées
    private Pane reservationOverlay;
    private StackPane calendarContainer;
    
    // Redimensionnement des réservations
    private boolean isResizing = false;
    private Reservation resizingReservation = null;
    private VehicleAvailabilityItem resizingVehicle = null;
    private boolean resizingStart = false; // true = début, false = fin
    
    // Pour le redimensionnement fluide (sans reconstruire la grille)
    private LocalDate originalStartDate = null;
    private boolean originalStartMorning = false;
    private LocalDate originalEndDate = null;
    private boolean originalEndMorning = false;
    private LocalDate currentResizeDate = null;
    private boolean currentResizeMorning = false;
    
    // Flag pour éviter double refresh
    private boolean initialLayoutDone = false;

    public VehicleAvailabilityView(ApiService apiService) {
        this.apiService = apiService;
        this.reservationService = VehicleReservationService.getInstance();
        this.currentMonth = YearMonth.now();
        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // Listener pour synchronisation des réservations entre les vues
        this.reservationChangeListener = vehicleId -> {
            Platform.runLater(() -> {
                if (getWidth() > 0 && !vehicles.isEmpty()) {
                    System.out.println("🔄 Synchronisation des réservations (véhicule: " + vehicleId + ")");
                    refreshCalendar();
                }
            });
        };
        reservationService.addChangeListener(reservationChangeListener);

        initializeView();
        setupComponents();
        setupLayoutListener();
        loadVehicles();
    }
    
    /**
     * Configure un listener pour rafraîchir la grille quand la vue obtient ses dimensions
     */
    private void setupLayoutListener() {
        // Listener sur la largeur - se déclenche quand la vue est ajoutée à la scène
        this.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0 && !initialLayoutDone && !vehicles.isEmpty()) {
                System.out.println("📐 VehicleAvailabilityView obtient une largeur: " + newVal);
                initialLayoutDone = true;
                Platform.runLater(this::refreshCalendar);
            }
        });
        
        // Listener sur la visibilité - se déclenche quand l'onglet devient visible
        this.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible && !vehicles.isEmpty()) {
                System.out.println("👁️ VehicleAvailabilityView devient visible!");
                Platform.runLater(this::refreshCalendar);
            }
        });
        
        // Listener sur le parent - se déclenche quand ajouté à la scène
        this.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent != null && !vehicles.isEmpty()) {
                System.out.println("👪 VehicleAvailabilityView ajouté à un parent!");
                Platform.runLater(() -> {
                    if (getWidth() > 0) {
                        refreshCalendar();
                    }
                });
            }
        });
    }

    private void initializeView() {
        setSpacing(0);
        setPadding(new Insets(10));
        getStyleClass().add("vehicle-availability-view");
        setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + ";");
    }

    private void setupComponents() {
        scrollPane = createCalendarScrollPane();
        getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
    }

    private ScrollPane createCalendarScrollPane() {
        calendarGrid = new GridPane();
        calendarGrid.setHgap(1);
        calendarGrid.setVgap(1);
        calendarGrid.setStyle("-fx-background-color: " + ThemeConstants.BORDER_COLOR + ";");
        
        // Gestionnaire global pour le redimensionnement des réservations
        // Utilise addEventFilter pour capturer les événements AVANT les cellules enfants
        calendarGrid.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
            if (isResizing && resizingReservation != null) {
                handleResizeDrag(e.getSceneX(), e.getSceneY());
                e.consume(); // Empêche les cellules enfants de recevoir l'événement
            }
        });
        
        calendarGrid.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_RELEASED, e -> {
            if (isResizing) {
                stopResizing();
                e.consume();
            }
        });
        
        // Overlay pour les réservations - on le garde mais on ne l'utilise pas pour l'instant
        reservationOverlay = new Pane();
        calendarContainer = new StackPane(); // Vide, juste pour éviter les NullPointerException

        // Mettre directement la grille dans le ScrollPane (sans StackPane)
        ScrollPane sp = new ScrollPane(calendarGrid);
        sp.setFitToWidth(true);
        sp.setFitToHeight(false);
        sp.setStyle("-fx-background: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                "-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + ";");

        return sp;
    }

    // ========================================
    // MÉTHODES PUBLIQUES POUR LA TOOLBAR
    // ========================================
    
    public void navigatePrevious() {
        if (currentViewMode == ViewMode.MONTH) {
            currentMonth = currentMonth.minusMonths(1);
        } else {
            currentWeekStart = currentWeekStart.minusWeeks(1);
        }
        refreshCalendar();
    }

    public void navigateNext() {
        if (currentViewMode == ViewMode.MONTH) {
            currentMonth = currentMonth.plusMonths(1);
        } else {
            currentWeekStart = currentWeekStart.plusWeeks(1);
        }
        refreshCalendar();
    }

    public void navigateToday() {
        currentMonth = YearMonth.now();
        currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        refreshCalendar();
    }

    public void setViewMode(ViewMode mode) {
        this.currentViewMode = mode;
        refreshCalendar();
    }

    public ViewMode getViewMode() {
        return currentViewMode;
    }

    public String getPeriodLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        if (currentViewMode == ViewMode.MONTH) {
            return capitalize(currentMonth.format(formatter));
        } else {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            DateTimeFormatter weekFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH);
            return currentWeekStart.format(weekFormatter) + " - " + weekEnd.format(weekFormatter) + " " + currentWeekStart.getYear();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    // ========================================
    // RAFRAÎCHISSEMENT DU CALENDRIER
    // ========================================

    public void refreshCalendar() {
        System.out.println("📅 refreshCalendar() appelé - vehicles.size=" + vehicles.size());
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
        cellRegistry.clear();
        highlightedCells.clear();
        reservationOverlay.getChildren().clear();

        if (currentViewMode == ViewMode.MONTH) {
            setupMonthView();
        } else {
            setupWeekView();
        }
        
        System.out.println("📅 Grille créée avec " + calendarGrid.getChildren().size() + " éléments");
        System.out.println("📅 calendarGrid visible=" + calendarGrid.isVisible() + ", managed=" + calendarGrid.isManaged());
        System.out.println("📅 calendarContainer visible=" + calendarContainer.isVisible());
        System.out.println("📅 scrollPane visible=" + scrollPane.isVisible());
        System.out.println("📅 this (VBox) visible=" + this.isVisible() + ", managed=" + this.isManaged());
        
        // Debug dimensions après layout
        Platform.runLater(() -> {
            System.out.println("📐 calendarGrid dimensions: " + calendarGrid.getWidth() + "x" + calendarGrid.getHeight());
            System.out.println("📐 calendarContainer dimensions: " + calendarContainer.getWidth() + "x" + calendarContainer.getHeight());
            System.out.println("📐 scrollPane dimensions: " + scrollPane.getWidth() + "x" + scrollPane.getHeight());
            System.out.println("📐 this (VBox) dimensions: " + this.getWidth() + "x" + this.getHeight());
            renderReservationBars();
        });
    }

    private void setupMonthView() {
        int daysInMonth = currentMonth.lengthOfMonth();
        setupColumnConstraints(daysInMonth);
        createCalendarHeader(daysInMonth, true);
        createVehicleRows(daysInMonth, true);
    }

    private void setupWeekView() {
        setupColumnConstraints(7);
        createCalendarHeader(7, false);
        createVehicleRows(7, false);
    }

    private void setupColumnConstraints(int dayCount) {
        // Colonne véhicule
        ColumnConstraints vehicleCol = new ColumnConstraints(160);
        vehicleCol.setHgrow(Priority.NEVER);
        calendarGrid.getColumnConstraints().add(vehicleCol);

        // Colonne demie-journée
        ColumnConstraints timeSlotCol = new ColumnConstraints(70);
        timeSlotCol.setHgrow(Priority.NEVER);
        calendarGrid.getColumnConstraints().add(timeSlotCol);

        // Colonnes des jours
        double dayWidth = currentViewMode == ViewMode.WEEK ? 100 : 55;
        for (int day = 0; day < dayCount; day++) {
            ColumnConstraints dayCol = new ColumnConstraints(dayWidth);
            dayCol.setHgrow(Priority.SOMETIMES);
            calendarGrid.getColumnConstraints().add(dayCol);
        }
    }

    private void createCalendarHeader(int dayCount, boolean isMonthView) {
        // Header "Véhicule"
        Label vehicleHeader = createHeaderLabel("Véhicule");
        calendarGrid.add(vehicleHeader, 0, 0);

        // Header "Créneau"
        Label timeSlotHeader = createHeaderLabel("Créneau");
        calendarGrid.add(timeSlotHeader, 1, 0);

        // Headers des jours
        LocalDate today = LocalDate.now();
        for (int i = 0; i < dayCount; i++) {
            LocalDate date = isMonthView ? currentMonth.atDay(i + 1) : currentWeekStart.plusDays(i);
            VBox dayHeader = createDayHeader(date, today);
            calendarGrid.add(dayHeader, i + 2, 0);
        }
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                "-fx-text-fill: " + StandardColors.PRIMARY_BLUE + "; -fx-font-weight: bold; " +
                "-fx-padding: 8; -fx-border-color: " + ThemeConstants.BORDER_COLOR + "; " +
                "-fx-border-width: 1;");
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        return label;
    }

    private VBox createDayHeader(LocalDate date, LocalDate today) {
        VBox dayHeader = new VBox(2);
        dayHeader.setAlignment(Pos.CENTER);
        dayHeader.setMaxWidth(Double.MAX_VALUE);
        dayHeader.setMaxHeight(Double.MAX_VALUE);

        boolean isToday = date.equals(today);
        boolean isWeekend = date.getDayOfWeek().getValue() >= 6;

        String bgColor = isWeekend ? WEEKEND_BG_COLOR : ThemeConstants.BACKGROUND_SECONDARY;
        String borderColor = isToday ? TODAY_BORDER_COLOR : ThemeConstants.BORDER_COLOR;
        String borderWidth = isToday ? "2" : "1";

        dayHeader.setStyle("-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: " + borderColor + "; " +
                "-fx-border-width: " + borderWidth + "; -fx-padding: 4;");

        Label dayNum = new Label(String.valueOf(date.getDayOfMonth()));
        dayNum.setFont(Font.font("System", FontWeight.BOLD, isToday ? 13 : 11));
        dayNum.setStyle("-fx-text-fill: " + (isToday ? TODAY_BORDER_COLOR : StandardColors.getTextColor()) + ";");

        String[] dayNames = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        Label dayName = new Label(dayNames[date.getDayOfWeek().getValue() - 1]);
        dayName.setStyle("-fx-font-size: 9px; -fx-text-fill: " + 
                (isWeekend ? "#9E9E9E" : StandardColors.getTextColor()) + ";");

        dayHeader.getChildren().addAll(dayNum, dayName);
        return dayHeader;
    }

    // ========================================
    // LIGNES DES VÉHICULES
    // ========================================

    private void createVehicleRows(int dayCount, boolean isMonthView) {
        int rowIndex = 1;

        for (int vIdx = 0; vIdx < vehicles.size(); vIdx++) {
            VehicleAvailabilityItem vehicle = vehicles.get(vIdx);
            
            // Assigner une couleur au véhicule
            if (!vehicleColors.containsKey(vehicle.getId())) {
                vehicleColors.put(vehicle.getId(), VEHICLE_COLORS[vIdx % VEHICLE_COLORS.length]);
            }

            // Cellule fusionnée pour le véhicule
            createVehicleCell(vehicle, rowIndex);

            // Ligne Matin
            createVehicleTimeRow(vehicle, rowIndex, true, dayCount, isMonthView);
            rowIndex++;

            // Ligne Après-midi
            createVehicleTimeRow(vehicle, rowIndex, false, dayCount, isMonthView);
            rowIndex++;
        }
    }

    private void createVehicleCell(VehicleAvailabilityItem vehicle, int startRowIndex) {
        VBox vehicleCell = new VBox(2);
        vehicleCell.setAlignment(Pos.CENTER);
        vehicleCell.setPadding(new Insets(8));
        
        String vehicleColor = vehicleColors.getOrDefault(vehicle.getId(), VEHICLE_COLORS[0]);
        vehicleCell.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_SECONDARY + "; " +
                "-fx-border-color: " + ThemeConstants.BORDER_COLOR + "; " +
                "-fx-border-width: 1 1 1 4; -fx-border-color: " + ThemeConstants.BORDER_COLOR + " " + 
                ThemeConstants.BORDER_COLOR + " " + ThemeConstants.BORDER_COLOR + " " + vehicleColor + ";");
        vehicleCell.setMaxWidth(Double.MAX_VALUE);
        vehicleCell.setPrefHeight(70);

        Label vehicleName = new Label(vehicle.getName());
        vehicleName.setFont(Font.font("System", FontWeight.BOLD, 11));
        vehicleName.setWrapText(true);
        vehicleName.setAlignment(Pos.CENTER);
        vehicleName.setMaxWidth(140);

        Label vehicleId = new Label(vehicle.getId());
        vehicleId.setStyle("-fx-font-size: 9px; -fx-text-fill: " + StandardColors.SECONDARY_BLUE + ";");

        vehicleCell.getChildren().addAll(vehicleName, vehicleId);

        calendarGrid.add(vehicleCell, 0, startRowIndex);
        GridPane.setRowSpan(vehicleCell, 2);
    }

    private void createVehicleTimeRow(VehicleAvailabilityItem vehicle, int rowIndex, 
                                       boolean isMorning, int dayCount, boolean isMonthView) {
        // Colonne demie-journée avec couleur alternée
        Label periodLabel = new Label(isMorning ? "☀️ Matin" : "🌙 A-midi");
        periodLabel.setAlignment(Pos.CENTER);
        periodLabel.setPadding(new Insets(6));
        periodLabel.setStyle("-fx-background-color: " + (isMorning ? MORNING_COLOR : AFTERNOON_COLOR) + "; " +
                "-fx-border-color: " + ThemeConstants.BORDER_COLOR + "; " +
                "-fx-border-width: 1; -fx-font-size: 10px; -fx-font-weight: bold;");
        periodLabel.setMaxWidth(Double.MAX_VALUE);
        periodLabel.setMaxHeight(Double.MAX_VALUE);

        calendarGrid.add(periodLabel, 1, rowIndex);

        // Cellules des jours
        for (int i = 0; i < dayCount; i++) {
            LocalDate date = isMonthView ? currentMonth.atDay(i + 1) : currentWeekStart.plusDays(i);
            StackPane dayCell = createDayCell(vehicle, date, isMorning, i + 2, rowIndex);
            calendarGrid.add(dayCell, i + 2, rowIndex);
            
            // Enregistrer la cellule pour la sélection par glissement
            String cellKey = vehicle.getId() + "_" + date + "_" + isMorning;
            cellRegistry.put(cellKey, dayCell);
        }
    }

    private StackPane createDayCell(VehicleAvailabilityItem vehicle, LocalDate date, 
                                     boolean isMorning, int colIndex, int rowIndex) {
        StackPane cell = new StackPane();
        cell.setPrefHeight(35);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);

        LocalDate today = LocalDate.now();
        boolean isToday = date.equals(today);
        boolean isWeekend = date.getDayOfWeek().getValue() >= 6;

        // Vérifier s'il y a une réservation pour cette cellule
        Reservation reservation = getReservation(vehicle, date, isMorning);
        
        String baseBgColor;
        String borderStyle;
        
        if (reservation != null) {
            // Cellule avec réservation - utiliser la couleur du véhicule
            String vehicleColor = vehicleColors.getOrDefault(vehicle.getId(), VEHICLE_COLORS[0]);
            baseBgColor = vehicleColor;
            
            // Déterminer si c'est le début, milieu ou fin de la réservation
            boolean isStart = isFirstCellOfReservation(date, isMorning, reservation);
            boolean isEnd = isLastCellOfReservation(date, isMorning, reservation);
            
            // Style de bordure pour créer l'effet de bloc fusionné matin/après-midi
            borderStyle = calculateMergedBorderStyle(date, isMorning, 
                                                      reservation.getStartDate(), reservation.isStartMorning(),
                                                      reservation.getEndDate(), reservation.isEndMorning());
            
            // Ajouter le titre de la réservation si c'est la première cellule
            if (isStart) {
                // Calculer le nombre de cellules de la réservation pour la largeur du titre
                int cellCount = calculateReservationCellCount(reservation);
                
                Label titleLabel = new Label(reservation.getTitle());
                titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                                   "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 2, 0, 1, 1); " +
                                   "-fx-background-color: transparent;");
                // Le label doit dépasser la cellule pour afficher le titre complet
                titleLabel.setMinWidth(Region.USE_PREF_SIZE);
                titleLabel.setPrefWidth(cellCount * 45); // Approximativement la largeur de chaque cellule
                titleLabel.setMaxWidth(cellCount * 45);
                titleLabel.setAlignment(Pos.CENTER_LEFT);
                titleLabel.setPadding(new Insets(2, 8, 2, 8));
                titleLabel.setMouseTransparent(true);
                
                cell.getChildren().add(titleLabel);
                StackPane.setAlignment(titleLabel, Pos.CENTER_LEFT);
                cell.setClip(null); // Permettre au texte de dépasser si nécessaire
            }
            
            // Curseur et gestion du redimensionnement aux bords
            final boolean finalIsStart = isStart;
            final boolean finalIsEnd = isEnd;
            
            cell.setOnMouseMoved(e -> {
                double x = e.getX();
                double width = cell.getWidth();
                if (finalIsStart && x < 8) {
                    cell.setCursor(Cursor.W_RESIZE);
                } else if (finalIsEnd && x > width - 8) {
                    cell.setCursor(Cursor.E_RESIZE);
                } else {
                    cell.setCursor(Cursor.HAND);
                }
            });
            
            cell.setOnMouseExited(e -> cell.setCursor(Cursor.DEFAULT));
            
            // Clic pour éditer ou drag pour redimensionner
            final Reservation finalRes = reservation;
            final VehicleAvailabilityItem finalVehicle = vehicle;
            
            cell.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    double x = e.getX();
                    double width = cell.getWidth();
                    if (finalIsStart && x < 8) {
                        // Début du redimensionnement côté début
                        startResizing(finalRes, finalVehicle, true);
                    } else if (finalIsEnd && x > width - 8) {
                        // Début du redimensionnement côté fin
                        startResizing(finalRes, finalVehicle, false);
                    }
                }
            });
            
            cell.setOnMouseReleased(e -> {
                if (isResizing) {
                    stopResizing();
                }
            });
            
            // Gestionnaire de glissement pour le redimensionnement
            cell.setOnMouseDragged(e -> {
                if (isResizing && resizingReservation != null) {
                    handleResizeDrag(e.getSceneX(), e.getSceneY());
                    e.consume();
                }
            });
            
            cell.setOnMouseClicked(e -> {
                if (!isResizing && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                    // Un seul clic pour ouvrir le dialogue d'édition
                    showEditReservationDialog(finalRes, finalVehicle);
                }
            });
            
        } else {
            // Cellule sans réservation - couleur normale
            baseBgColor = isMorning ? MORNING_COLOR : AFTERNOON_COLOR;
            if (isWeekend) {
                baseBgColor = WEEKEND_BG_COLOR;
            }
            borderStyle = "-fx-border-color: " + (isToday ? TODAY_BORDER_COLOR : ThemeConstants.BORDER_COLOR) + "; " +
                         "-fx-border-width: " + (isToday ? "0 0 0 2" : "0.5") + ";";
            cell.setCursor(Cursor.HAND);
            
            // Gestion de la sélection par glissement
            setupDragSelection(cell, vehicle, date, isMorning);
        }

        cell.setStyle("-fx-background-color: " + baseBgColor + "; " + borderStyle);

        // Stocker les données dans les propriétés
        cell.getProperties().put("vehicle", vehicle);
        cell.getProperties().put("date", date);
        cell.getProperties().put("isMorning", isMorning);
        cell.getProperties().put("colIndex", colIndex);
        cell.getProperties().put("rowIndex", rowIndex);
        cell.getProperties().put("baseStyle", cell.getStyle());
        cell.getProperties().put("reservation", reservation);

        return cell;
    }
    
    /**
     * Vérifie si c'est la dernière cellule d'une réservation
     */
    private boolean isLastCellOfReservation(LocalDate date, boolean isMorning, Reservation res) {
        return date.equals(res.getEndDate()) && isMorning == res.isEndMorning();
    }
    
    /**
     * Démarre le redimensionnement d'une réservation
     */
    private void startResizing(Reservation res, VehicleAvailabilityItem vehicle, boolean resizeStart) {
        isResizing = true;
        resizingReservation = res;
        resizingVehicle = vehicle;
        resizingStart = resizeStart;
        
        // Sauvegarder les dates originales
        originalStartDate = res.getStartDate();
        originalStartMorning = res.isStartMorning();
        originalEndDate = res.getEndDate();
        originalEndMorning = res.isEndMorning();
        currentResizeDate = resizeStart ? originalStartDate : originalEndDate;
        currentResizeMorning = resizeStart ? originalStartMorning : originalEndMorning;
        
        System.out.println("🔧 Début redimensionnement: " + res.getTitle() + " (côté " + (resizeStart ? "début" : "fin") + ")");
    }
    
    /**
     * Arrête le redimensionnement
     */
    private void stopResizing() {
        if (isResizing && resizingReservation != null && resizingVehicle != null) {
            // Appliquer les changements finaux
            if (resizingStart) {
                resizingReservation.setStartDate(currentResizeDate);
                resizingReservation.setStartMorning(currentResizeMorning);
            } else {
                resizingReservation.setEndDate(currentResizeDate);
                resizingReservation.setEndMorning(currentResizeMorning);
            }
            System.out.println("🔧 Fin redimensionnement: " + resizingReservation.getTitle());
            // Notifier le service partagé pour synchroniser les vues
            reservationService.updateReservation(resizingVehicle.getId(), resizingReservation);
        }
        isResizing = false;
        resizingReservation = null;
        resizingVehicle = null;
        originalStartDate = null;
        originalEndDate = null;
        currentResizeDate = null;
    }
    
    /**
     * Gère le glissement pour redimensionner une réservation
     * Mise à jour visuelle fluide sans reconstruire la grille
     * Détection basée sur la position X pour progresser logiquement (matin -> après-midi -> lendemain matin...)
     */
    private void handleResizeDrag(double sceneX, double sceneY) {
        if (resizingReservation == null || resizingVehicle == null) return;
        
        // Trouver la cellule la plus proche en X pour ce véhicule (ignorer Y car matin/après-midi sont sur des lignes différentes)
        StackPane closestCell = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Map.Entry<String, StackPane> entry : cellRegistry.entrySet()) {
            StackPane cell = entry.getValue();
            VehicleAvailabilityItem cellVehicle = (VehicleAvailabilityItem) cell.getProperties().get("vehicle");
            
            // Ne considérer que les cellules du véhicule en cours de redimensionnement
            if (cellVehicle != null && cellVehicle.getId().equals(resizingVehicle.getId())) {
                javafx.geometry.Bounds bounds = cell.localToScene(cell.getBoundsInLocal());
                
                // Vérifier si le curseur est dans la zone Y du véhicule (les 2 lignes matin+après-midi)
                // et calculer la distance en X
                double cellCenterX = bounds.getCenterX();
                double distance = Math.abs(sceneX - cellCenterX);
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCell = cell;
                }
            }
        }
        
        if (closestCell == null) return;
        
        LocalDate cellDate = (LocalDate) closestCell.getProperties().get("date");
        Boolean cellIsMorning = (Boolean) closestCell.getProperties().get("isMorning");
        
        if (cellDate == null || cellIsMorning == null) return;
        
        // Déterminer le slot cible basé sur la position dans la cellule
        javafx.geometry.Bounds bounds = closestCell.localToScene(closestCell.getBoundsInLocal());
        double relativePosInCell = (sceneX - bounds.getMinX()) / bounds.getWidth();
        
        LocalDate targetDate = cellDate;
        boolean targetMorning = cellIsMorning;
        
        // Si on est dans la moitié droite de la cellule et c'est le matin, passer à l'après-midi
        // Si on est dans la moitié droite et c'est l'après-midi, passer au lendemain matin
        if (relativePosInCell > 0.7) {
            if (cellIsMorning) {
                targetMorning = false; // Matin -> Après-midi
            } else {
                targetDate = cellDate.plusDays(1); // Après-midi -> Lendemain matin
                targetMorning = true;
            }
        } else if (relativePosInCell < 0.3) {
            // Si on est dans la moitié gauche, on peut vouloir reculer
            if (!cellIsMorning) {
                targetMorning = true; // Après-midi -> Matin
            } else {
                targetDate = cellDate.minusDays(1); // Matin -> Veille après-midi
                targetMorning = false;
            }
        }
        
        // Vérifier si la position a changé
        if (targetDate.equals(currentResizeDate) && targetMorning == currentResizeMorning) {
            return; // Pas de changement
        }
        
        boolean isValid = false;
        if (resizingStart) {
            // Redimensionner le début - ne pas dépasser la fin originale
            isValid = isSlotBeforeOrEqual(targetDate, targetMorning, originalEndDate, originalEndMorning);
        } else {
            // Redimensionner la fin - ne pas aller avant le début original
            isValid = isSlotAfterOrEqual(targetDate, targetMorning, originalStartDate, originalStartMorning);
        }
        
        if (isValid) {
            // Mettre à jour la position courante
            LocalDate oldDate = currentResizeDate;
            boolean oldMorning = currentResizeMorning;
            currentResizeDate = targetDate;
            currentResizeMorning = targetMorning;
            
            // Mise à jour visuelle fluide des cellules
            updateCellsVisualDuringResize(oldDate, oldMorning, targetDate, targetMorning);
        }
    }
    
    /**
     * Met à jour visuellement les cellules pendant le redimensionnement (sans reconstruire la grille)
     * Les bordures fusionnent matin et après-midi verticalement pour créer un bloc unifié
     */
    private void updateCellsVisualDuringResize(LocalDate oldDate, boolean oldMorning, LocalDate newDate, boolean newMorning) {
        String vehicleColor = vehicleColors.getOrDefault(resizingVehicle.getId(), VEHICLE_COLORS[0]);
        
        // Déterminer les nouvelles limites de la réservation
        LocalDate effectiveStart = resizingStart ? newDate : originalStartDate;
        boolean effectiveStartMorning = resizingStart ? newMorning : originalStartMorning;
        LocalDate effectiveEnd = resizingStart ? originalEndDate : newDate;
        boolean effectiveEndMorning = resizingStart ? originalEndMorning : newMorning;
        
        // Parcourir toutes les cellules du véhicule concerné et mettre à jour leur style
        for (Map.Entry<String, StackPane> entry : cellRegistry.entrySet()) {
            StackPane cell = entry.getValue();
            VehicleAvailabilityItem cellVehicle = (VehicleAvailabilityItem) cell.getProperties().get("vehicle");
            
            if (cellVehicle != null && cellVehicle.getId().equals(resizingVehicle.getId())) {
                LocalDate cellDate = (LocalDate) cell.getProperties().get("date");
                Boolean cellIsMorning = (Boolean) cell.getProperties().get("isMorning");
                
                if (cellDate != null && cellIsMorning != null) {
                    boolean isInRange = isSlotInRange(cellDate, cellIsMorning, 
                                                       effectiveStart, effectiveStartMorning,
                                                       effectiveEnd, effectiveEndMorning);
                    
                    boolean isWeekend = cellDate.getDayOfWeek().getValue() >= 6;
                    boolean isToday = cellDate.equals(LocalDate.now());
                    
                    if (isInRange) {
                        // Calculer les bordures pour fusionner matin/après-midi verticalement
                        String borderStyle = calculateMergedBorderStyle(cellDate, cellIsMorning, 
                                                                         effectiveStart, effectiveStartMorning,
                                                                         effectiveEnd, effectiveEndMorning);
                        
                        cell.setStyle("-fx-background-color: " + vehicleColor + "; " + borderStyle);
                    } else {
                        // Cellule hors réservation - remettre le style normal
                        String baseBgColor = cellIsMorning ? MORNING_COLOR : AFTERNOON_COLOR;
                        if (isWeekend) baseBgColor = WEEKEND_BG_COLOR;
                        String borderStyle = "-fx-border-color: " + (isToday ? TODAY_BORDER_COLOR : ThemeConstants.BORDER_COLOR) + "; " +
                                           "-fx-border-width: " + (isToday ? "0 0 0 2" : "0.5") + ";";
                        cell.setStyle("-fx-background-color: " + baseBgColor + "; " + borderStyle);
                    }
                }
            }
        }
    }
    
    /**
     * Calcule le style de bordure pour créer un bloc fusionné matin/après-midi
     * Format bordure: top right bottom left
     */
    private String calculateMergedBorderStyle(LocalDate cellDate, boolean cellIsMorning,
                                               LocalDate startDate, boolean startMorning,
                                               LocalDate endDate, boolean endMorning) {
        // Déterminer si cette cellule est au début/fin de la réservation (horizontalement)
        boolean isFirstDay = cellDate.equals(startDate);
        boolean isLastDay = cellDate.equals(endDate);
        
        // Déterminer les bordures horizontales (gauche/droite)
        boolean hasLeftBorder = isFirstDay && (cellIsMorning == startMorning || (cellIsMorning && !startMorning));
        boolean hasRightBorder = isLastDay && (cellIsMorning == endMorning || (!cellIsMorning && endMorning));
        
        // Ajuster: si c'est le premier jour et qu'on commence l'après-midi, pas de bordure gauche le matin
        if (isFirstDay && !startMorning && cellIsMorning) {
            hasLeftBorder = false;
        }
        // Si c'est le dernier jour et qu'on finit le matin, pas de bordure droite l'après-midi
        if (isLastDay && endMorning && !cellIsMorning) {
            hasRightBorder = false;
        }
        
        // Déterminer les bordures verticales (haut/bas) pour fusion matin/après-midi
        boolean hasTopBorder;
        boolean hasBottomBorder;
        
        if (cellIsMorning) {
            // Cellule matin
            hasTopBorder = true; // Toujours bordure en haut
            // Bordure en bas seulement si c'est le dernier slot ET c'est le matin (pas d'après-midi après)
            hasBottomBorder = isLastDay && endMorning;
        } else {
            // Cellule après-midi
            // Bordure en haut seulement si c'est le premier slot ET c'est l'après-midi (pas de matin avant)
            hasTopBorder = isFirstDay && !startMorning;
            hasBottomBorder = true; // Toujours bordure en bas
        }
        
        // Construire le style
        int top = hasTopBorder ? 2 : 0;
        int right = hasRightBorder ? 2 : 0;
        int bottom = hasBottomBorder ? 2 : 0;
        int left = hasLeftBorder ? 2 : 0;
        
        // Rayons pour les coins (seulement aux extrémités)
        String topLeftRadius = (hasTopBorder && hasLeftBorder) ? "4" : "0";
        String topRightRadius = (hasTopBorder && hasRightBorder) ? "4" : "0";
        String bottomRightRadius = (hasBottomBorder && hasRightBorder) ? "4" : "0";
        String bottomLeftRadius = (hasBottomBorder && hasLeftBorder) ? "4" : "0";
        
        return "-fx-border-color: white; " +
               "-fx-border-width: " + top + " " + right + " " + bottom + " " + left + "; " +
               "-fx-border-radius: " + topLeftRadius + " " + topRightRadius + " " + bottomRightRadius + " " + bottomLeftRadius + ";";
    }
    
    /**
     * Vérifie si un slot est dans une plage
     */
    private boolean isSlotInRange(LocalDate date, boolean isMorning, 
                                   LocalDate start, boolean startMorning,
                                   LocalDate end, boolean endMorning) {
        return isSlotAfterOrEqual(date, isMorning, start, startMorning) &&
               isSlotBeforeOrEqual(date, isMorning, end, endMorning);
    }
    
    /**
     * Vérifie si un slot est avant ou égal à un autre
     */
    private boolean isSlotBeforeOrEqual(LocalDate date1, boolean isMorning1, LocalDate date2, boolean isMorning2) {
        if (date1.isBefore(date2)) return true;
        if (date1.equals(date2)) {
            if (isMorning1 == isMorning2) return true;
            if (isMorning1 && !isMorning2) return true;
        }
        return false;
    }
    
    /**
     * Vérifie si un slot est après ou égal à un autre
     */
    private boolean isSlotAfterOrEqual(LocalDate date1, boolean isMorning1, LocalDate date2, boolean isMorning2) {
        if (date1.isAfter(date2)) return true;
        if (date1.equals(date2)) {
            if (isMorning1 == isMorning2) return true;
            if (!isMorning1 && isMorning2) return true;
        }
        return false;
    }
    
    /**
     * Calcule le nombre de cellules d'une réservation
     */
    private int calculateReservationCellCount(Reservation res) {
        LocalDate start = res.getStartDate();
        LocalDate end = res.getEndDate();
        int count = 0;
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            if (current.equals(start)) {
                // Jour de début
                count += res.isStartMorning() ? 2 : 1; // matin compte pour 2 (matin+après-midi) ou juste après-midi
                if (current.equals(end)) {
                    // Même jour début et fin
                    if (res.isStartMorning() && res.isEndMorning()) {
                        count = 1; // Juste le matin
                    } else if (res.isStartMorning() && !res.isEndMorning()) {
                        count = 2; // Matin + après-midi
                    } else {
                        count = 1; // Juste après-midi
                    }
                }
            } else if (current.equals(end)) {
                // Jour de fin (différent du début)
                count += res.isEndMorning() ? 1 : 2;
            } else {
                // Jour complet au milieu
                count += 2;
            }
            current = current.plusDays(1);
        }
        
        return Math.max(1, count);
    }
    
    /**
     * Vérifie si c'est la première cellule d'une réservation (pour afficher le titre)
     */
    private boolean isFirstCellOfReservation(LocalDate date, boolean isMorning, Reservation res) {
        return date.equals(res.getStartDate()) && isMorning == res.isStartMorning();
    }

    @SuppressWarnings("unused")
    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "…";
    }

    // ========================================
    // GESTION DES RÉSERVATIONS
    // ========================================

    private Reservation getReservation(VehicleAvailabilityItem vehicle, LocalDate date, boolean isMorning) {
        List<Reservation> reservations = reservationService.getReservations(vehicle.getId());
        if (reservations == null || reservations.isEmpty()) return null;

        for (Reservation res : reservations) {
            if (isSlotInReservation(date, isMorning, res)) {
                return res;
            }
        }
        return null;
    }

    private boolean isSlotInReservation(LocalDate date, boolean isMorning, Reservation res) {
        // Vérifier si le créneau (date + matin/après-midi) est dans la réservation
        if (date.isBefore(res.getStartDate()) || date.isAfter(res.getEndDate())) {
            return false;
        }
        
        // Cas où c'est le jour de début
        if (date.equals(res.getStartDate())) {
            if (res.isStartMorning()) {
                return true; // Commence le matin, donc matin et après-midi inclus
            } else {
                return !isMorning; // Commence l'après-midi, seul après-midi inclus
            }
        }
        
        // Cas où c'est le jour de fin
        if (date.equals(res.getEndDate())) {
            if (res.isEndMorning()) {
                return isMorning; // Finit le matin, seul matin inclus
            } else {
                return true; // Finit l'après-midi, donc matin et après-midi inclus
            }
        }
        
        // Jour intermédiaire : tout est inclus
        return true;
    }

    // ========================================
    // RENDU DES BARRES DE RÉSERVATION (désactivé - affiché dans les cellules)
    // ========================================
    
    private void renderReservationBars() {
        // Les réservations sont maintenant affichées directement dans les cellules
        // via createDayCell() - cette méthode n'est plus nécessaire
    }
    
    @SuppressWarnings("unused")
    private void renderSingleReservationBar(VehicleAvailabilityItem vehicle, Reservation res, int vehicleIndex) {
        // Calculer les positions des cellules de début et fin
        LocalDate periodStart = currentViewMode == ViewMode.MONTH ? currentMonth.atDay(1) : currentWeekStart;
        LocalDate periodEnd = currentViewMode == ViewMode.MONTH ? currentMonth.atEndOfMonth() : currentWeekStart.plusDays(6);
        
        // Vérifier si la réservation est visible dans la période actuelle
        if (res.getEndDate().isBefore(periodStart) || res.getStartDate().isAfter(periodEnd)) {
            return; // Réservation hors de la vue
        }
        
        // Trouver les cellules de début et fin (clippées à la période visible)
        LocalDate visibleStart = res.getStartDate().isBefore(periodStart) ? periodStart : res.getStartDate();
        LocalDate visibleEnd = res.getEndDate().isAfter(periodEnd) ? periodEnd : res.getEndDate();
        boolean startMorning = res.getStartDate().isBefore(periodStart) ? true : res.isStartMorning();
        boolean endMorning = res.getEndDate().isAfter(periodEnd) ? false : res.isEndMorning();
        
        // Clé de la cellule de début
        String startKey = vehicle.getId() + "_" + visibleStart + "_" + startMorning;
        String endKey = vehicle.getId() + "_" + visibleEnd + "_" + endMorning;
        
        StackPane startCell = cellRegistry.get(startKey);
        StackPane endCell = cellRegistry.get(endKey);
        
        if (startCell == null || endCell == null) {
            return;
        }
        
        // Attendre que le layout soit calculé
        Platform.runLater(() -> {
            // Calculer les coordonnées
            double startX = startCell.getBoundsInParent().getMinX();
            double startY = startCell.getBoundsInParent().getMinY();
            double endX = endCell.getBoundsInParent().getMaxX();
            double height = startCell.getHeight();
            
            // Créer la barre de réservation
            HBox bar = createReservationBar(vehicle, res, endX - startX, height);
            bar.setLayoutX(startX);
            bar.setLayoutY(startY + 2);
            bar.setPrefWidth(endX - startX - 2);
            bar.setPrefHeight(height - 4);
            
            reservationOverlay.getChildren().add(bar);
        });
    }
    
    private HBox createReservationBar(VehicleAvailabilityItem vehicle, Reservation res, double width, double height) {
        String vehicleColor = vehicleColors.getOrDefault(vehicle.getId(), VEHICLE_COLORS[0]);
        
        // Conteneur principal
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(2, 4, 2, 4));
        bar.setStyle("-fx-background-color: " + vehicleColor + "; " +
                "-fx-background-radius: 4; -fx-cursor: hand;");
        bar.setMaxHeight(height - 4);
        
        // Poignée de redimensionnement gauche
        Region leftHandle = createResizeHandle();
        leftHandle.setCursor(Cursor.W_RESIZE);
        setupResizeHandlers(leftHandle, vehicle, res, true);
        
        // Label du titre
        Label titleLabel = new Label(res.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        
        // Poignée de redimensionnement droite
        Region rightHandle = createResizeHandle();
        rightHandle.setCursor(Cursor.E_RESIZE);
        setupResizeHandlers(rightHandle, vehicle, res, false);
        
        bar.getChildren().addAll(leftHandle, titleLabel, rightHandle);
        
        // Clic pour éditer
        bar.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !isResizing) {
                showEditReservationDialog(res, vehicle);
                e.consume();
            }
        });
        
        // Hover effect
        bar.setOnMouseEntered(e -> bar.setStyle("-fx-background-color: derive(" + vehicleColor + ", -15%); " +
                "-fx-background-radius: 4; -fx-cursor: hand;"));
        bar.setOnMouseExited(e -> bar.setStyle("-fx-background-color: " + vehicleColor + "; " +
                "-fx-background-radius: 4; -fx-cursor: hand;"));
        
        return bar;
    }
    
    private Region createResizeHandle() {
        Region handle = new Region();
        handle.setPrefWidth(6);
        handle.setMinWidth(6);
        handle.setMaxWidth(6);
        handle.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 2;");
        return handle;
    }
    
    private void setupResizeHandlers(Region handle, VehicleAvailabilityItem vehicle, Reservation res, boolean isStart) {
        handle.setOnMousePressed(e -> {
            isResizing = true;
            resizingReservation = res;
            resizingVehicle = vehicle;
            resizingStart = isStart;
            e.consume();
        });
        
        handle.setOnMouseDragged(e -> {
            if (isResizing && resizingReservation != null) {
                // Trouver la cellule sous la souris
                double sceneX = e.getSceneX();
                double sceneY = e.getSceneY();
                
                for (Map.Entry<String, StackPane> entry : cellRegistry.entrySet()) {
                    StackPane cell = entry.getValue();
                    if (cell.localToScene(cell.getBoundsInLocal()).contains(sceneX, sceneY)) {
                        VehicleAvailabilityItem cellVehicle = (VehicleAvailabilityItem) cell.getProperties().get("vehicle");
                        if (cellVehicle != null && cellVehicle.getId().equals(resizingVehicle.getId())) {
                            LocalDate date = (LocalDate) cell.getProperties().get("date");
                            Boolean isMorning = (Boolean) cell.getProperties().get("isMorning");
                            if (date != null && isMorning != null) {
                                updateReservationBounds(date, isMorning);
                            }
                        }
                        break;
                    }
                }
            }
            e.consume();
        });
        
        handle.setOnMouseReleased(e -> {
            if (isResizing) {
                isResizing = false;
                resizingReservation = null;
                resizingVehicle = null;
                refreshCalendar();
            }
            e.consume();
        });
    }
    
    private void updateReservationBounds(LocalDate newDate, boolean newMorning) {
        if (resizingReservation == null) return;
        
        // Calculer le slot pour comparer
        int currentStartSlot = toSlot(resizingReservation.getStartDate(), resizingReservation.isStartMorning());
        int currentEndSlot = toSlot(resizingReservation.getEndDate(), resizingReservation.isEndMorning());
        int newSlot = toSlot(newDate, newMorning);
        
        if (resizingStart) {
            // Modifier le début (ne peut pas dépasser la fin)
            if (newSlot <= currentEndSlot) {
                resizingReservation.setStartDate(newDate);
                resizingReservation.setStartMorning(newMorning);
            }
        } else {
            // Modifier la fin (ne peut pas précéder le début)
            if (newSlot >= currentStartSlot) {
                resizingReservation.setEndDate(newDate);
                resizingReservation.setEndMorning(newMorning);
            }
        }
        
        // Rafraîchir l'affichage en temps réel
        renderReservationBars();
    }
    
    private int toSlot(LocalDate date, boolean morning) {
        return (int) date.toEpochDay() * 2 + (morning ? 0 : 1);
    }

    // ========================================
    // SÉLECTION PAR GLISSEMENT
    // ========================================

    private void setupDragSelection(StackPane cell, VehicleAvailabilityItem vehicle, 
                                     LocalDate date, boolean isMorning) {
        
        // Activer le mode drag pour recevoir les événements MouseDragEntered
        cell.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Reservation existing = getReservation(vehicle, date, isMorning);
                if (existing == null) {
                    // Démarrer une nouvelle sélection
                    isDragging = true;
                    currentDragSelection = new DragSelection(vehicle, date, isMorning);
                    highlightCell(cell, true);
                    e.consume();
                }
            }
        });

        // IMPORTANT: Démarrer le "full drag" pour que MouseDragEntered fonctionne sur les autres cellules
        cell.setOnDragDetected(e -> {
            if (isDragging && currentDragSelection != null) {
                cell.startFullDrag();
                e.consume();
            }
        });

        // Utiliser MouseDragEntered au lieu de MouseEntered (fonctionne pendant le drag)
        cell.setOnMouseDragEntered(e -> {
            if (isDragging && currentDragSelection != null) {
                // Étendre la sélection si même véhicule (permet de traverser matin/après-midi)
                if (currentDragSelection.vehicle.getId().equals(vehicle.getId())) {
                    currentDragSelection.extendTo(date, isMorning);
                    updateSelectionHighlight();
                }
            }
            e.consume();
        });

        // Hover normal (quand pas de drag)
        cell.setOnMouseEntered(e -> {
            if (!isDragging) {
                String baseStyle = (String) cell.getProperties().get("baseStyle");
                if (baseStyle != null) {
                    cell.setStyle(baseStyle + "-fx-opacity: 0.7;");
                }
            }
        });

        cell.setOnMouseExited(e -> {
            if (!isDragging && !highlightedCells.contains(cell)) {
                String baseStyle = (String) cell.getProperties().get("baseStyle");
                if (baseStyle != null) {
                    cell.setStyle(baseStyle);
                }
            }
        });

        // Relâchement du bouton (fin du drag) - utiliser MouseDragReleased pour être sûr
        cell.setOnMouseDragReleased(e -> {
            if (isDragging && currentDragSelection != null) {
                isDragging = false;
                DragSelection selection = currentDragSelection;
                currentDragSelection = null;
                clearSelectionHighlight();
                
                // Ouvrir le dialogue de réservation
                showReservationDialog(selection);
            }
            e.consume();
        });

        // Relâchement normal (sur la cellule d'origine)
        cell.setOnMouseReleased(e -> {
            if (isDragging && currentDragSelection != null) {
                isDragging = false;
                DragSelection selection = currentDragSelection;
                currentDragSelection = null;
                clearSelectionHighlight();
                
                // Ouvrir le dialogue de réservation
                showReservationDialog(selection);
            }
        });

        // Clic simple pour voir une réservation existante
        cell.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1 && !e.isDragDetect()) {
                Reservation existing = getReservation(vehicle, date, isMorning);
                if (existing != null) {
                    showReservationDetails(existing, vehicle);
                }
            }
        });
    }

    private void highlightCell(StackPane cell, boolean highlight) {
        if (highlight) {
            if (!highlightedCells.contains(cell)) {
                highlightedCells.add(cell);
            }
            cell.setStyle("-fx-background-color: " + SELECTION_COLOR + "; " +
                    "-fx-border-color: " + StandardColors.PRIMARY_BLUE + "; -fx-border-width: 2;");
        }
    }

    private void updateSelectionHighlight() {
        // Réinitialiser les cellules précédemment surlignées
        for (StackPane cell : new ArrayList<>(highlightedCells)) {
            String baseStyle = (String) cell.getProperties().get("baseStyle");
            if (baseStyle != null) {
                cell.setStyle(baseStyle);
            }
        }
        highlightedCells.clear();
        
        if (currentDragSelection == null) return;
        
        // Surligner toutes les cellules de la sélection (tous les slots)
        List<int[]> slots = currentDragSelection.getAllSlots();
        for (int[] slot : slots) {
            LocalDate d = LocalDate.ofEpochDay(slot[0]);
            boolean morning = slot[1] == 1;
            String cellKey = currentDragSelection.vehicle.getId() + "_" + d + "_" + morning;
            StackPane cell = cellRegistry.get(cellKey);
            if (cell != null) {
                highlightCell(cell, true);
            }
        }
    }

    private void clearSelectionHighlight() {
        for (StackPane cell : highlightedCells) {
            String baseStyle = (String) cell.getProperties().get("baseStyle");
            if (baseStyle != null) {
                cell.setStyle(baseStyle);
            }
        }
        highlightedCells.clear();
    }

    // ========================================
    // DIALOGUES
    // ========================================

    private void showReservationDialog(DragSelection selection) {
        Dialog<Reservation> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle Réservation");
        dialog.setHeaderText("Réserver " + selection.vehicle.getName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Titre de la réservation");
        titleField.setPrefWidth(280);

        TextField descField = new TextField();
        descField.setPromptText("Description (optionnel)");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
        LocalDate start = selection.getEffectiveStartDate();
        LocalDate end = selection.getEffectiveEndDate();
        boolean startMorning = selection.getEffectiveStartMorning();
        boolean endMorning = selection.getEffectiveEndMorning();
        
        String startPeriod = startMorning ? "Matin" : "Après-midi";
        String endPeriod = endMorning ? "Matin" : "Après-midi";
        
        String dateRange;
        if (start.equals(end) && startMorning == endMorning) {
            dateRange = start.format(fmt) + " (" + startPeriod + ")";
        } else if (start.equals(end)) {
            dateRange = start.format(fmt) + " (" + startPeriod + " → " + endPeriod + ")";
        } else {
            dateRange = start.format(fmt) + " (" + startPeriod + ") → " + end.format(fmt) + " (" + endPeriod + ")";
        }

        grid.add(new Label("Véhicule:"), 0, 0);
        grid.add(new Label(selection.vehicle.getName()), 1, 0);
        grid.add(new Label("Période:"), 0, 1);
        grid.add(new Label(dateRange), 1, 1);
        grid.add(new Label("Titre:"), 0, 2);
        grid.add(titleField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType confirmType = new ButtonType("Confirmer", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmType, cancelType);

        UnifiedThemeManager.getInstance().applyThemeToDialog(dialog.getDialogPane());

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmType && !titleField.getText().trim().isEmpty()) {
                return new Reservation(
                    titleField.getText().trim(),
                    descField.getText().trim(),
                    start,
                    end,
                    startMorning,
                    endMorning
                );
            }
            return null;
        });

        dialog.showAndWait().ifPresent(reservation -> {
            String vehicleId = selection.vehicle.getId();
            // Utiliser le service partagé pour la synchronisation
            reservationService.addReservation(vehicleId, reservation);
            System.out.println("✅ Réservation créée: " + reservation.getTitle() + " pour " + selection.vehicle.getName());
        });
    }

    private void showEditReservationDialog(Reservation reservation, VehicleAvailabilityItem vehicle) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier la réservation");
        dialog.setHeaderText(vehicle.getName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField titleField = new TextField(reservation.getTitle());
        titleField.setPrefWidth(280);

        TextField descField = new TextField(reservation.getDescription());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);
        String startPeriod = reservation.isStartMorning() ? "Matin" : "Après-midi";
        String endPeriod = reservation.isEndMorning() ? "Matin" : "Après-midi";
        
        String dateRange;
        if (reservation.getStartDate().equals(reservation.getEndDate()) && reservation.isStartMorning() == reservation.isEndMorning()) {
            dateRange = reservation.getStartDate().format(fmt) + " (" + startPeriod + ")";
        } else if (reservation.getStartDate().equals(reservation.getEndDate())) {
            dateRange = reservation.getStartDate().format(fmt) + " (" + startPeriod + " → " + endPeriod + ")";
        } else {
            dateRange = reservation.getStartDate().format(fmt) + " (" + startPeriod + ") → " + 
                       reservation.getEndDate().format(fmt) + " (" + endPeriod + ")";
        }

        grid.add(new Label("Période:"), 0, 0);
        Label periodLabel = new Label(dateRange);
        periodLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        grid.add(periodLabel, 1, 0);
        
        Label helpLabel = new Label("(Glissez les bords de la barre pour modifier)");
        helpLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #999;");
        grid.add(helpLabel, 1, 1);
        
        grid.add(new Label("Titre:"), 0, 2);
        grid.add(titleField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType deleteType = new ButtonType("Supprimer", ButtonBar.ButtonData.LEFT);
        ButtonType cancelType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, deleteType, cancelType);

        UnifiedThemeManager.getInstance().applyThemeToDialog(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(response -> {
            if (response == saveType) {
                // Mettre à jour la réservation
                reservation.setTitle(titleField.getText().trim());
                reservation.setDescription(descField.getText().trim());
                // Notifier le service partagé
                reservationService.updateReservation(vehicle.getId(), reservation);
                System.out.println("✅ Réservation modifiée: " + reservation.getTitle());
            } else if (response == deleteType) {
                // Supprimer la réservation via le service partagé
                reservationService.removeReservation(vehicle.getId(), reservation);
                System.out.println("🗑️ Réservation supprimée: " + reservation.getTitle());
            }
        });
    }

    private void showReservationDetails(Reservation reservation, VehicleAvailabilityItem vehicle) {
        // Rediriger vers le dialogue d'édition
        showEditReservationDialog(reservation, vehicle);
    }

    // ========================================
    // CHARGEMENT DES DONNÉES
    // ========================================

    private void loadVehicles() {
        vehicles.clear();
        
        apiService.getAllVehicles().thenAccept(vehicleList -> {
            Platform.runLater(() -> {
                if (vehicleList != null && !vehicleList.isEmpty()) {
                    System.out.println("🚐 Chargement de " + vehicleList.size() + " véhicules pour le planning");
                    for (Object v : vehicleList) {
                        if (v instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> vehicleMap = (Map<String, Object>) v;
                            String id = String.valueOf(vehicleMap.getOrDefault("id", ""));
                            String name = String.valueOf(vehicleMap.getOrDefault("name", "Sans nom"));
                            String registration = String.valueOf(vehicleMap.getOrDefault("registrationNumber", ""));
                            
                            String displayName = name;
                            if (registration != null && !registration.isEmpty() && !"null".equals(registration)) {
                                displayName += " (" + registration + ")";
                            }
                            
                            vehicles.add(new VehicleAvailabilityItem(id, displayName));
                        }
                    }
                    System.out.println("✅ " + vehicles.size() + " véhicules chargés dans le planning");
                    // Ne refresh que si on a déjà des dimensions
                    if (getWidth() > 0) {
                        refreshCalendar();
                    } else {
                        System.out.println("⏳ Attente du layout pour rafraîchir le calendrier...");
                    }
                } else {
                    System.out.println("⚠️ Aucun véhicule trouvé, utilisation de données de démonstration");
                    loadDemoVehicles();
                }
            });
        }).exceptionally(ex -> {
            System.err.println("❌ Erreur chargement véhicules: " + ex.getMessage());
            Platform.runLater(this::loadDemoVehicles);
            return null;
        });
    }

    private void loadDemoVehicles() {
        vehicles.clear();
        vehicles.addAll(
            new VehicleAvailabilityItem("VH001", "Camion Scène Mobile"),
            new VehicleAvailabilityItem("VH002", "Utilitaire Éclairage"),
            new VehicleAvailabilityItem("VH003", "Fourgon Audio")
        );
        // Ne refresh que si on a déjà des dimensions
        if (getWidth() > 0) {
            refreshCalendar();
        }
    }

    public void refresh() {
        initialLayoutDone = false; // Permettre un nouveau refresh au layout
        loadVehicles();
    }

    // ========================================
    // CLASSES INTERNES
    // ========================================

    private static class DragSelection {
        final VehicleAvailabilityItem vehicle;
        LocalDate startDate;
        boolean startMorning;
        LocalDate endDate;
        boolean endMorning;

        DragSelection(VehicleAvailabilityItem vehicle, LocalDate date, boolean isMorning) {
            this.vehicle = vehicle;
            this.startDate = date;
            this.startMorning = isMorning;
            this.endDate = date;
            this.endMorning = isMorning;
        }

        /**
         * Étendre la sélection vers une nouvelle date/période.
         * La progression est logique: matin → après-midi → matin suivant → ...
         */
        @SuppressWarnings("unused")
        void extendTo(LocalDate date, boolean isMorning) {
            // Calculer les "slots" (demi-journées) - slot 0 = jour 0 matin, slot 1 = jour 0 après-midi, etc.
            int startSlot = toSlot(startDate, startMorning);
            int targetSlot = toSlot(date, isMorning);
            
            // Le slot de départ est fixe, on étend le slot de fin
            endDate = date;
            endMorning = isMorning;
        }
        
        /** Convertir une date+matin en numéro de slot (2 slots par jour) */
        private int toSlot(LocalDate date, boolean morning) {
            // Nombre de jours depuis epoch * 2 + (0 pour matin, 1 pour après-midi)
            return (int) date.toEpochDay() * 2 + (morning ? 0 : 1);
        }
        
        /** Convertir un numéro de slot en date */
        private LocalDate slotToDate(int slot) {
            return LocalDate.ofEpochDay(slot / 2);
        }
        
        /** Vérifie si un slot est le matin */
        private boolean slotIsMorning(int slot) {
            return slot % 2 == 0;
        }
        
        /** Obtenir tous les slots de la sélection (du premier au dernier) */
        List<int[]> getAllSlots() {
            List<int[]> slots = new ArrayList<>();
            int startSlot = toSlot(startDate, startMorning);
            int endSlot = toSlot(endDate, endMorning);
            
            // S'assurer que start <= end
            int from = Math.min(startSlot, endSlot);
            int to = Math.max(startSlot, endSlot);
            
            for (int s = from; s <= to; s++) {
                LocalDate d = slotToDate(s);
                boolean m = slotIsMorning(s);
                slots.add(new int[] { (int) d.toEpochDay(), m ? 1 : 0 });
            }
            return slots;
        }
        
        LocalDate getEffectiveStartDate() {
            int startSlot = toSlot(startDate, startMorning);
            int endSlot = toSlot(endDate, endMorning);
            return startSlot <= endSlot ? startDate : endDate;
        }
        
        LocalDate getEffectiveEndDate() {
            int startSlot = toSlot(startDate, startMorning);
            int endSlot = toSlot(endDate, endMorning);
            return startSlot >= endSlot ? startDate : endDate;
        }
        
        boolean getEffectiveStartMorning() {
            int startSlot = toSlot(startDate, startMorning);
            int endSlot = toSlot(endDate, endMorning);
            return startSlot <= endSlot ? startMorning : endMorning;
        }
        
        boolean getEffectiveEndMorning() {
            int startSlot = toSlot(startDate, startMorning);
            int endSlot = toSlot(endDate, endMorning);
            return startSlot >= endSlot ? startMorning : endMorning;
        }
    }

    public static class VehicleAvailabilityItem {
        private final String id;
        private final String name;

        public VehicleAvailabilityItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class Reservation {
        private String title;
        private String description;
        private LocalDate startDate;
        private LocalDate endDate;
        private boolean startMorning;
        private boolean endMorning;

        public Reservation(String title, String description, LocalDate startDate, LocalDate endDate, 
                          boolean startMorning, boolean endMorning) {
            this.title = title;
            this.description = description;
            this.startDate = startDate;
            this.endDate = endDate;
            this.startMorning = startMorning;
            this.endMorning = endMorning;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public LocalDate getStartDate() { return startDate; }
        public LocalDate getEndDate() { return endDate; }
        public boolean isStartMorning() { return startMorning; }
        public boolean isEndMorning() { return endMorning; }
        
        public void setTitle(String title) { this.title = title; }
        public void setDescription(String description) { this.description = description; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        public void setStartMorning(boolean startMorning) { this.startMorning = startMorning; }
        public void setEndMorning(boolean endMorning) { this.endMorning = endMorning; }
    }
}
