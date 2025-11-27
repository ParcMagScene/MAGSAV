package com.magscene.magsav.desktop.view.vehicle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Random;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vue du planning de disponibilités des véhicules
 * Affiche un calendrier mensuel avec matin/après-midi par véhicule
 */
public class VehicleAvailabilityView extends VBox {

    // Composants UI
    private Label monthLabel;
    private GridPane calendarGrid;
    private ComboBox<String> monthSelector;
    private ComboBox<Integer> yearSelector;
    private YearMonth currentMonth;

    // Données
    private final ObservableList<VehicleAvailabilityItem> vehicles = javafx.collections.FXCollections
            .observableArrayList();

    public VehicleAvailabilityView(ApiService apiService) {
        this.currentMonth = YearMonth.now();

        initializeView();
        setupComponents();
        loadVehicles();
        refreshCalendar();
    }

    private void initializeView() {
        setSpacing(10);
        setPadding(new Insets(20));
        getStyleClass().add("vehicle-availability-view");
        setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + ";");
    }

    private void setupComponents() {
        // Header avec navigation
        HBox header = createHeader();

        // Grille du calendrier (toolbar retirée - gérée par VehicleManagerView)
        ScrollPane calendarScrollPane = createCalendarScrollPane();

        getChildren().addAll(header, calendarScrollPane);
        VBox.setVgrow(calendarScrollPane, Priority.ALWAYS);
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        // Plus de titre ici - déjà affiché dans l'onglet; // Navigation mois/année
        HBox navigation = createMonthNavigation();

        header.getChildren().addAll(navigation);
        HBox.setHgrow(navigation, Priority.ALWAYS);

        return header;
    }

    private HBox createMonthNavigation() {
        HBox nav = new HBox(10);
        nav.setAlignment(Pos.CENTER_RIGHT);

        // Bouton mois précédent
        Button prevButton = new Button("◀");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        prevButton.setOnAction(e -> navigateMonth(-1));

        // Sélecteur de mois
        monthSelector = new ComboBox<>();
        String[] months = { "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre" };
        monthSelector.getItems().addAll(months);
        monthSelector.setValue(months[currentMonth.getMonthValue() - 1]);
        monthSelector.setOnAction(e -> updateCurrentMonth());

        // Sélecteur d'année
        yearSelector = new ComboBox<>();
        int currentYear = LocalDate.now().getYear();
        for (int year = currentYear - 2; year <= currentYear + 5; year++) {
            yearSelector.getItems().add(year);
        }
        yearSelector.setValue(currentMonth.getYear());
        yearSelector.setOnAction(e -> updateCurrentMonth());

        // Bouton mois suivant
        Button nextButton = new Button("▶");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        nextButton.setOnAction(e -> navigateMonth(1));

        // Label du mois actuel
        monthLabel = new Label();
        monthLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        updateMonthLabel();

        nav.getChildren().addAll(prevButton, monthSelector, yearSelector, nextButton, monthLabel);
        return nav;
    }

    private void updateMonthLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
        monthLabel.setText(currentMonth.format(formatter));
    }

    private ScrollPane createCalendarScrollPane() {
        calendarGrid = new GridPane();
        calendarGrid.setHgap(1);
        calendarGrid.setVgap(1);
        calendarGrid.setStyle(
                "-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + ";");

        ScrollPane scrollPane = new ScrollPane(calendarGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
                "-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + ";");

        return scrollPane;
    }

    private void refreshCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        // Configuration des colonnes
        setupColumnConstraints();

        // Header des jours
        createCalendarHeader();

        // Lignes des véhicules (2 lignes par véhicule: matin/après-midi)
        createVehicleRows();
    }

    private void setupColumnConstraints() {
        // Colonne véhicule
        ColumnConstraints vehicleCol = new ColumnConstraints(200);
        vehicleCol.setHgrow(Priority.NEVER);
        calendarGrid.getColumnConstraints().add(vehicleCol);

        // Colonne demie-journée
        ColumnConstraints timeSlotCol = new ColumnConstraints(120);
        timeSlotCol.setHgrow(Priority.NEVER);
        calendarGrid.getColumnConstraints().add(timeSlotCol);

        // Colonnes des jours du mois
        int daysInMonth = currentMonth.lengthOfMonth();
        double dayWidth = 80;

        for (int day = 1; day <= daysInMonth; day++) {
            ColumnConstraints dayCol = new ColumnConstraints(dayWidth);
            dayCol.setHgrow(Priority.SOMETIMES);
            calendarGrid.getColumnConstraints().add(dayCol);
        }
    }

    private void createCalendarHeader() {
        // Header "Véhicule"
        Label vehicleHeader = new Label("Véhicule");
        vehicleHeader.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor() + "; " +
                "-fx-text-fill: " + StandardColors.SECONDARY_BLUE + "; -fx-font-weight: bold; " +
                "-fx-padding: 10; -fx-border-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor()
                + "; " +
                "-fx-border-width: 1;");
        vehicleHeader.setMaxWidth(Double.MAX_VALUE);
        vehicleHeader.setAlignment(Pos.CENTER);

        // Header "Demie-journée"
        Label timeSlotHeader = new Label("Demie-journée");
        timeSlotHeader
                .setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor() + "; " +
                        "-fx-text-fill: " + StandardColors.getTextColor() + "; -fx-font-weight: bold; " +
                        "-fx-padding: 10; -fx-border-color: "
                        + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                        "-fx-border-width: 1;");
        timeSlotHeader.setMaxWidth(Double.MAX_VALUE);
        timeSlotHeader.setAlignment(Pos.CENTER);
        calendarGrid.add(vehicleHeader, 0, 0);
        calendarGrid.add(timeSlotHeader, 1, 0);

        // Headers des jours
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);

            VBox dayHeader = new VBox(2);
            dayHeader.setAlignment(Pos.CENTER);

            String baseHeaderStyle = "-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor()
                    + "; " +
                    "-fx-border-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                    "-fx-border-width: 1; -fx-padding: 5;";

            // Highlight weekend
            if (date.getDayOfWeek().getValue() >= 6) {
                baseHeaderStyle = "-fx-background-color: "
                        + UnifiedThemeManager.getInstance().getCurrentBackgroundColor() + "; " +
                        "-fx-border-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                        "-fx-border-width: 1; -fx-padding: 5;";
            }

            dayHeader.setStyle(baseHeaderStyle);
            dayHeader.setMaxWidth(Double.MAX_VALUE);

            Label dayNum = new Label(String.valueOf(day));
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS

            Label dayName = new Label(date.getDayOfWeek().name().substring(0, 3));
            // $varName supprimÃ© - Style gÃ©rÃ© par CSS

            dayHeader.getChildren().addAll(dayNum, dayName);
            calendarGrid.add(dayHeader, day + 1, 0); // +1 pour décaler d'une colonne (demie-journée)
        }
    }

    private void createVehicleRows() {
        int rowIndex = 1;

        for (VehicleAvailabilityItem vehicle : vehicles) {
            // Créer la cellule fusionnée pour le véhicule
            createVehicleCell(vehicle, rowIndex);

            // Ligne Matin
            createVehicleTimeRow(vehicle, "Matin", rowIndex, true);
            rowIndex++;

            // Ligne Après-midi
            createVehicleTimeRow(vehicle, "Après-midi", rowIndex, false);
            rowIndex++;
        }
    }

    private void createVehicleCell(VehicleAvailabilityItem vehicle, int startRowIndex) {
        // Cellule fusionnée pour le nom du véhicule (span 2 lignes)
        VBox vehicleCell = new VBox();
        vehicleCell.setAlignment(Pos.CENTER);
        vehicleCell.setPadding(new Insets(15, 10, 15, 10));
        vehicleCell.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor() + "; " +
                "-fx-border-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                "-fx-border-width: 1;");
        vehicleCell.setMaxWidth(Double.MAX_VALUE);
        vehicleCell.setPrefHeight(80); // 2 lignes de 40 pixels

        Label vehicleName = new Label(vehicle.getName());
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        vehicleName.setWrapText(true);
        vehicleName.setAlignment(Pos.CENTER);
        vehicleName.setMaxWidth(Double.MAX_VALUE);

        vehicleCell.getChildren().add(vehicleName);

        // Ajouter la cellule avec span de 2 lignes
        calendarGrid.add(vehicleCell, 0, startRowIndex);
        GridPane.setRowSpan(vehicleCell, 2);
    }

    private void createVehicleTimeRow(VehicleAvailabilityItem vehicle, String period, int rowIndex, boolean isMorning) {
        // Colonne demie-journée
        Label periodLabel = new Label(period);
        periodLabel.setAlignment(Pos.CENTER);
        periodLabel.setPadding(new Insets(10));
        periodLabel.setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor() + "; " +
                "-fx-border-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                "-fx-border-width: 1; -fx-text-fill: " + StandardColors.SECONDARY_BLUE
                + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        periodLabel.setMaxWidth(Double.MAX_VALUE);
        periodLabel.setMaxHeight(Double.MAX_VALUE);

        calendarGrid.add(periodLabel, 1, rowIndex);

        // Cellules des jours
        int daysInMonth = currentMonth.lengthOfMonth();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);

            Region dayCell = createDayCell(vehicle, date, isMorning);
            calendarGrid.add(dayCell, day + 1, rowIndex); // +1 pour décaler après la colonne demie-journée
        }
    }

    private Region createDayCell(VehicleAvailabilityItem vehicle, LocalDate date, boolean isMorning) {
        Region cell = new Region();
        cell.setPrefHeight(40);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setMaxHeight(Double.MAX_VALUE);

        // Déterminer le statut de disponibilité
        String status = getAvailabilityStatus(vehicle, date, isMorning);
        String backgroundColor = getStatusColor(status);

        cell.setStyle("-fx-background-color: " + backgroundColor + "; " +
                "-fx-border-color: " + UnifiedThemeManager.getInstance().getCurrentSecondaryColor() + "; " +
                "-fx-border-width: 0.5; -fx-cursor: hand;");

        // Interaction souris
        cell.setOnMouseEntered(e -> cell.setStyle(cell.getStyle() + "-fx-opacity: 0.8;"));
        cell.setOnMouseExited(e -> cell.setStyle(cell.getStyle().replace("-fx-opacity: 0.8;", "")));
        cell.setOnMouseClicked(e -> handleCellClick(vehicle, date, isMorning));

        return cell;
    }

    private String getAvailabilityStatus(VehicleAvailabilityItem vehicle, LocalDate date, boolean isMorning) {
        // TODO: Récupérer le vrai statut depuis l'API; // Pour l'instant, simulation
        Random random = new Random(vehicle.getName().hashCode() + date.hashCode() + (isMorning ? 1 : 0));
        String[] statuses = { "available", "location", "prestation", "maintenance" };
        return statuses[random.nextInt(statuses.length * 2) % statuses.length]; // Plus de disponible
    }

    private String getStatusColor(String status) {
        return StandardColors.getVehicleStatusColor(status);
    }

    private void handleCellClick(VehicleAvailabilityItem vehicle, LocalDate date, boolean isMorning) {
        String period = isMorning ? "Matin" : "Après-midi";
        showAssignmentDialog(vehicle, date, period);
    }

    private void navigateMonth(int direction) {
        currentMonth = currentMonth.plusMonths(direction);
        updateMonthLabel();
        updateSelectors();
        refreshCalendar();
    }

    private void updateCurrentMonth() {
        if (monthSelector.getValue() != null && yearSelector.getValue() != null) {
            int monthIndex = monthSelector.getSelectionModel().getSelectedIndex() + 1;
            int year = yearSelector.getValue();
            currentMonth = YearMonth.of(year, monthIndex);
            updateMonthLabel();
            refreshCalendar();
        }
    }

    private void updateSelectors() {
        monthSelector.getSelectionModel().select(currentMonth.getMonthValue() - 1);
        yearSelector.setValue(currentMonth.getYear());
    }

    private void loadVehicles() {
        // TODO: Charger les véhicules depuis l'API; // Pour l'instant, données de test
        vehicles.addAll(
                new VehicleAvailabilityItem("VH001", "Camion Scène Mobile"),
                new VehicleAvailabilityItem("VH002", "Utilitaire Éclairage"),
                new VehicleAvailabilityItem("VH003", "Fourgon Audio"),
                new VehicleAvailabilityItem("VH004", "Remorque Structure"),
                new VehicleAvailabilityItem("VH005", "Véhicule Technique"));
    }

    private void showAssignmentDialog(VehicleAvailabilityItem vehicle, LocalDate date, String period) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Affectation Véhicule");
        dialog.setHeaderText("Nouvelle Affectation");

        String vehicleInfo = vehicle != null ? vehicle.getName() : "Sélectionner véhicule";
        String message = String.format("Véhicule: %s%nDate: %s%nPériode: %s%n%nFonctionnalité en développement...",
                vehicleInfo, date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), period);
        dialog.setContentText(message);
        dialog.showAndWait();
    }

    // Classe pour représenter un véhicule dans le planning
    public static class VehicleAvailabilityItem {
        private final String id;
        private final String name;

        public VehicleAvailabilityItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}