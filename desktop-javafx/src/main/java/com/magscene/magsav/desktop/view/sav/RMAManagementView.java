package com.magscene.magsav.desktop.view.sav;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import com.magscene.magsav.desktop.component.DetailPanel;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.dialog.sav.RMADialog;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.StandardColors;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.util.AlertUtil;
import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Interface de gestion des RMA (Return Merchandise Authorization) - VERSION
 * SIMPLIFIÉE
 * Structure épurée : BorderPane → DetailPanelContainer directement
 */
public class RMAManagementView extends BorderPane {

    private final ApiService apiService;
    private final ObservableList<RMARecord> rmaRecords;
    private final TableView<RMARecord> rmaTable;

    // Filtres spécifiques aux RMA
    private final ComboBox<String> rmaStatusFilter;
    private final ComboBox<String> rmaTypeFilter;
    private final TextField rmaSearchField;
    private final DatePicker rmaDateFrom;
    private final DatePicker rmaDateTo;

    public RMAManagementView() {
        this.apiService = new ApiService();
        this.rmaRecords = FXCollections.observableArrayList();

        // Configuration principale - STRUCTURE SIMPLIFIÉE BorderPane
        this.setStyle("-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + ";");

        // Initialisation des composants
        this.rmaStatusFilter = new ComboBox<>();
        this.rmaTypeFilter = new ComboBox<>();
        this.rmaSearchField = new TextField();
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(this.rmaSearchField);
        this.rmaDateFrom = new DatePicker();
        this.rmaDateTo = new DatePicker();
        this.rmaTable = createRMATable();

        // Construction de l'interface
        setupRMAInterface();
        setupRMAEventHandlers();

        // Chargement initial des données
        loadRMARecords();
    }

    private void setupRMAInterface() {
        // Toolbar unifiée avec filtres et actions
        HBox toolbar = createUnifiedToolbar();
        setTop(toolbar);

        // STRUCTURE SIMPLIFIÉE - Direct DetailPanelContainer comme RepairTrackingView
        // Configuration de la table RMA
        rmaTable.setPrefHeight(400);
        rmaTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_NEXT_COLUMN);
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(rmaTable);

        // Configuration directe - INTERFACE ÉPURÉE
        setCenter(containerWithDetail);

        // Configuration des filtres (logique conservée pour synchronisation toolbar)
        setupRMAFilterComboBoxes();
    }

    private HBox createUnifiedToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle(
                "-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-color: #8B91FF; " +
                        "-fx-border-width: 1px; " +
                        "-fx-border-radius: 8;");

        // Recherche
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "N° RMA, équipement...", text -> filterRMARecords());
        TextField search = (TextField) searchBox.getChildren().get(1);
        rmaSearchField.textProperty().bindBidirectional(search.textProperty());

        // Filtre Statut
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Initié", "Autorisé", "En transit retour", "Reçu", "En cours d'analyse",
                        "Réparé", "Remplacé", "Remboursé", "Refusé" },
                "Tous statuts", value -> filterRMARecords());
        if (statusBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) statusBox.getChildren().get(1);
            rmaStatusFilter.itemsProperty().bind(combo.itemsProperty());
            rmaStatusFilter.valueProperty().bindBidirectional(combo.valueProperty());
        }

        // Filtre Type
        VBox typeBox = ViewUtils.createFilterBox("📋 Type",
                new String[] { "Tous types", "Défaut de fabrication", "Dommage transport", "Non-conformité",
                        "Fin de garantie", "Upgrade", "Erreur commande" },
                "Tous types", value -> filterRMARecords());
        if (typeBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) typeBox.getChildren().get(1);
            rmaTypeFilter.itemsProperty().bind(combo.itemsProperty());
            rmaTypeFilter.valueProperty().bindBidirectional(combo.valueProperty());
        }

        // Boutons d'action
        Button addBtn = new Button("➕ Nouveau RMA");
        addBtn.getStyleClass().add("btn-add");
        addBtn.setOnAction(e -> createNewRMA());

        Button editBtn = new Button("✏️ Modifier");
        editBtn.getStyleClass().add("btn-edit");
        editBtn.disableProperty().bind(rmaTable.getSelectionModel().selectedItemProperty().isNull());
        editBtn.setOnAction(e -> editSelectedRMA());

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.getStyleClass().add("btn-delete");
        deleteBtn.disableProperty().bind(rmaTable.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.setOnAction(e -> deleteSelectedRMA());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(searchBox, statusBox, typeBox, spacer, addBtn, editBtn, deleteBtn);
        return toolbar;
    }

    private void filterRMARecords() {
        // Filtrage à implémenter
    }

    // SUPPRESSION createRMAHeaderSection() - Plus de statistiques comme
    // RepairTrackingView

    private void setupRMAFilterComboBoxes() {
        // Statuts RMA
        rmaStatusFilter.getItems().addAll(
                "Tous statuts", "Initié", "Autorisé", "En transit retour",
                "Reçu", "En cours d'analyse", "Réparé", "Remplacé", "Remboursé", "Refusé");
        rmaStatusFilter.setValue("Tous statuts");

        // Types de retour
        rmaTypeFilter.getItems().addAll(
                "Tous types", "Défaut de fabrication", "Dommage transport",
                "Non-conformité", "Fin de garantie", "Upgrade", "Erreur commande");
        rmaTypeFilter.setValue("Tous types");
    }

    // SUPPRESSION createRMAMainSection() et createRMATableSection(); //
    // Configuration directe dans setupRMAInterface(); // Méthodes de filtres et
    // actions supprimées - Maintenant gérées par la toolbar adaptative
    // SAVManagerView

    private TableView<RMARecord> createRMATable() {
        TableView<RMARecord> table = new TableView<>();
        table.setItems(rmaRecords);

        // Colonne N° RMA
        TableColumn<RMARecord, String> rmaNumberCol = new TableColumn<>("N° RMA");
        rmaNumberCol.setPrefWidth(100);
        rmaNumberCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getRmaNumber()));

        // Colonne Équipement
        TableColumn<RMARecord, String> equipmentCol = new TableColumn<>("Équipement");
        equipmentCol.setPrefWidth(150);
        equipmentCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEquipmentName()));

        // Colonne Motif
        TableColumn<RMARecord, String> reasonCol = new TableColumn<>("Motif");
        reasonCol.setPrefWidth(120);
        reasonCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getReturnReason()));

        // Colonne Statut avec icône
        TableColumn<RMARecord, String> statusCol = new TableColumn<>("Statut");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(data -> {
            String status = data.getValue().getStatus();
            String icon = getRMAStatusIcon(status);
            return new javafx.beans.property.SimpleStringProperty(icon + " " + status);
        });

        // Colonne Client/Demandeur
        TableColumn<RMARecord, String> customerCol = new TableColumn<>("Client");
        customerCol.setPrefWidth(130);
        customerCol.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCustomerName()));

        // Colonne Date création
        TableColumn<RMARecord, String> dateCol = new TableColumn<>("Créé le");
        dateCol.setPrefWidth(90);
        dateCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getCreatedAt();
            String formattedDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });

        // Colonne Valeur estimée
        TableColumn<RMARecord, String> valueCol = new TableColumn<>("Valeur");
        valueCol.setPrefWidth(80);
        valueCol.setCellValueFactory(data -> {
            Double value = data.getValue().getEstimatedValue();
            return new javafx.beans.property.SimpleStringProperty(
                    value != null ? String.format("%.0f €", value) : "N/A");
        });

        // Ajout individuel des colonnes pour éviter les warnings de generic array
        table.getColumns().add(rmaNumberCol);
        table.getColumns().add(equipmentCol);
        table.getColumns().add(reasonCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(customerCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(valueCol);

        // Classe CSS générique pour uniformiser le style
        table.getStyleClass().add("equipment-table");

        // Style conditionnel pour les lignes
        table.setRowFactory(tv -> {
            TableRow<RMARecord> row = new TableRow<RMARecord>();

            // Méthode pour appliquer le style approprié
            Runnable updateStyle = () -> {
                if (row.isEmpty() || row.getItem() == null) {
                    row.setStyle("");
                    return;
                }

                // Priorité 1: Si sélectionné, couleur de sélection MAGSAV
                if (row.isSelected()) {
                    // Style de sélection plus visible avec bordure
                    row.setStyle("-fx-background-color: " + ThemeConstants.SELECTION_BACKGROUND + "; " +
                            "-fx-text-fill: " + ThemeConstants.SELECTION_TEXT + "; " +
                            "-fx-border-color: " + ThemeConstants.SELECTION_BORDER + "; " +
                            "-fx-border-width: 2px;");
                    return;
                }

                // Priorité 2: Couleur selon le statut (seulement si pas sélectionné)
                String status = row.getItem().getStatus();

                switch (status) {
                    case "PENDING":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "APPROVED":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "IN_TRANSIT":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "COMPLETED":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    case "REJECTED":
                        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                        break;
                    default:
                        row.setStyle("");
                }
            };

            // Mise à jour du style quand l'item change
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());

            // Mise à jour du style quand la sélection change
            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> updateStyle.run());

            // Appel initial pour s'assurer que le style est appliqué
            updateStyle.run();

            // Double-clic pour consultation en mode lecture seule
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openRMADetails();
                }
            });

            return row;
        });

        return table;
    }

    private void setupRMAEventHandlers() {
        // La gestion de la sélection et l'affichage des détails sont maintenant; //
        // automatiquement gérés par le DetailPanelContainer; // Gestionnaires de
        // filtres
        rmaSearchField.textProperty().addListener((obs, oldText, newText) -> applyRMAFilters());
        rmaStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
        rmaTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
        rmaDateFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
        rmaDateTo.valueProperty().addListener((obs, oldVal, newVal) -> applyRMAFilters());
    }

    private void applyRMAFilters() {
        // Implémentation du filtrage des RMA; // Logique de filtrage basée sur les
        // critères sélectionnés
    }

    // L'affichage des détails RMA est maintenant géré par le volet de
    // visualisation; // via l'implémentation DetailPanelProvider de RMARecord

    private String getRMAStatusIcon(String status) {
        switch (status.toUpperCase()) {
            case "INITIÉ":
                return "🆕";
            case "AUTORISÉ":
                return "✅";
            case "EN TRANSIT RETOUR":
                return "🚚";
            case "REÇU":
                return "📦";
            case "EN COURS D'ANALYSE":
                return "🔍";
            case "RÉPARÉ":
                return "🔧";
            case "REMPLACÉ":
                return "🔄";
            case "REMBOURSÉ":
                return "💸";
            case "REFUSÉ":
                return "❌";
            default:
                return "❓";
        }
    }

    private String getRMARowBackgroundColor(String status) {
        // Couleurs compatibles thème sombre avec transparence
        switch (status.toUpperCase()) {
            case "INITIÉ":
                return "-fx-background-color: rgba(255, 193, 7, 0.2)"; // Jaune translucide
            case "AUTORISÉ":
                return "-fx-background-color: rgba(13, 202, 240, 0.2)"; // Cyan translucide
            case "EN TRANSIT RETOUR":
                return "-fx-background-color: rgba(108, 117, 125, 0.2)"; // Gris translucide
            case "REÇU":
                return "-fx-background-color: rgba(40, 167, 69, 0.2)"; // Vert translucide
            case "RÉPARÉ":
            case "REMPLACÉ":
            case "REMBOURSÉ":
                return "-fx-background-color: rgba(32, 201, 151, 0.2)"; // Teal translucide
            case "REFUSÉ":
                return "-fx-background-color: rgba(220, 53, 69, 0.2)"; // Rouge translucide
            default:
                return "-fx-background-color: " + ThemeConstants.BACKGROUND_PRIMARY;
        }
    }

    private void createNewRMA() {
        AlertUtil.showInfo("Nouveau RMA", "Fonctionnalité en cours de développement");
    }

    private void editSelectedRMA() {
        RMARecord selected = rmaTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            AlertUtil.showInfo("Édition RMA", "Édition du RMA: " + selected.getRmaNumber());
        }
    }

    private void deleteSelectedRMA() {
        RMARecord selected = rmaTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean confirmed = AlertUtil.showConfirmation("Supprimer RMA",
                    "Voulez-vous vraiment supprimer le RMA " + selected.getRmaNumber() + " ?");
            if (confirmed) {
                rmaRecords.remove(selected);
            }
        }
    }

    public void loadRMARecords() {
        // Simulation de données RMA pour développement
        rmaRecords.clear();

        // Exemples de RMA
        rmaRecords.addAll(
                new RMARecord("RMA-2024-001", "Projecteur LED XR-300", "Défaut de fabrication",
                        "Initié", "MagScène Production", 1250.0, LocalDateTime.now().minusDays(2),
                        "Projecteur ne s'allume plus après 3 semaines d'utilisation normale"),
                new RMARecord("RMA-2024-002", "Console Audio MX-48", "Dommage transport",
                        "Autorisé", "Festival Été Lyon", 3400.0, LocalDateTime.now().minusDays(5),
                        "Dommages visibles sur le châssis et plusieurs faders défaillants"),
                new RMARecord("RMA-2024-003", "Écran LED P3.9", "Non-conformité",
                        "Reçu", "Théâtre Municipal", 2100.0, LocalDateTime.now().minusDays(10),
                        "Résolution d'affichage non conforme aux spécifications commandées"));
    }

    /**
     * Ouvre les détails d'un RMA en mode lecture seule (double-clic)
     */
    private void openRMADetails() {
        RMARecord selectedRMA = rmaTable.getSelectionModel().getSelectedItem();
        if (selectedRMA == null) {
            AlertUtil.showWarning("Aucune sélection", "Veuillez sélectionner un RMA");
            return;
        }

        // Convertir RMARecord en Map pour le dialogue
        Map<String, Object> rmaData = convertRMARecordToMap(selectedRMA);

        // Ouvrir en mode lecture seule
        RMADialog dialog = new RMADialog("Détails du RMA", rmaData, true);
        dialog.showAndWait();
    }

    /**
     * Ouvre le dialogue pour créer un nouveau RMA
     */
    public void openNewRMADialog() {
        RMADialog dialog = new RMADialog("Nouveau RMA", null, false);
        dialog.showAndWait().ifPresent(result -> {
            // Convertir le résultat en RMARecord et l'ajouter à la liste
            RMARecord newRMA = convertMapToRMARecord(result);
            rmaRecords.add(newRMA);
            AlertUtil.showInfo("RMA Créé", "Le RMA " + newRMA.getRmaNumber() + " a été créé avec succès");
        });
    }

    /**
     * Convertit un RMARecord en Map pour le dialogue
     */
    private Map<String, Object> convertRMARecordToMap(RMARecord rma) {
        Map<String, Object> map = new HashMap<>();
        map.put("rmaNumber", rma.getRmaNumber());
        map.put("equipment", rma.getEquipmentName());
        map.put("reason", rma.getReturnReason());
        map.put("status", rma.getStatus());
        map.put("customerName", rma.getCustomerName());
        map.put("estimatedValue", rma.getEstimatedValue());
        map.put("requestDate", rma.getCreatedAt());
        map.put("description", rma.getDescription());
        // Ajouter d'autres champs selon les besoins
        return map;
    }

    /**
     * Convertit une Map en RMARecord
     */
    private RMARecord convertMapToRMARecord(Map<String, Object> data) {
        String rmaNumber = String.valueOf(data.get("rmaNumber"));
        String equipment = String.valueOf(data.get("equipment"));
        String reason = String.valueOf(data.get("reason"));
        String status = String.valueOf(data.get("status"));
        String customerName = String.valueOf(data.get("customerName"));
        Double estimatedValue = (Double) data.get("estimatedValue");
        LocalDateTime creationDate = (LocalDateTime) data.get("requestDate");
        String description = String.valueOf(data.get("description"));

        if (creationDate == null) {
            creationDate = LocalDateTime.now();
        }

        return new RMARecord(rmaNumber, equipment, reason, status, customerName, estimatedValue, creationDate,
                description);
    }

    /**
     * Classe interne pour représenter un enregistrement RMA
     */
    public static class RMARecord implements com.magscene.magsav.desktop.component.DetailPanelProvider {
        private String rmaNumber;
        private String equipmentName;
        private String returnReason;
        private String status;
        private String customerName;
        private Double estimatedValue;
        private LocalDateTime createdAt;
        private String description;

        public RMARecord(String rmaNumber, String equipmentName, String returnReason,
                String status, String customerName, Double estimatedValue,
                LocalDateTime createdAt, String description) {
            this.rmaNumber = rmaNumber;
            this.equipmentName = equipmentName;
            this.returnReason = returnReason;
            this.status = status;
            this.customerName = customerName;
            this.estimatedValue = estimatedValue;
            this.createdAt = createdAt;
            this.description = description;
        }

        // Getters
        public String getRmaNumber() {
            return rmaNumber;
        }

        public String getEquipmentName() {
            return equipmentName;
        }

        public String getReturnReason() {
            return returnReason;
        }

        public String getStatus() {
            return status;
        }

        public String getCustomerName() {
            return customerName;
        }

        public Double getEstimatedValue() {
            return estimatedValue;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public String getDescription() {
            return description;
        }

        // Setters
        public void setRmaNumber(String rmaNumber) {
            this.rmaNumber = rmaNumber;
        }

        public void setEquipmentName(String equipmentName) {
            this.equipmentName = equipmentName;
        }

        public void setReturnReason(String returnReason) {
            this.returnReason = returnReason;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public void setEstimatedValue(Double estimatedValue) {
            this.estimatedValue = estimatedValue;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        // Implémentation de DetailPanelProvider
        @Override
        public String getDetailTitle() {
            return "RMA " + (rmaNumber != null ? rmaNumber : "N/A");
        }

        @Override
        public String getDetailSubtitle() {
            StringBuilder subtitle = new StringBuilder();

            if (status != null) {
                subtitle.append(getRMAStatusIcon()).append(" ").append(status);
            }

            if (customerName != null && !customerName.trim().isEmpty()) {
                if (subtitle.length() > 0)
                    subtitle.append(" • ");
                subtitle.append("Client: ").append(customerName);
            }

            if (estimatedValue != null && estimatedValue > 0) {
                if (subtitle.length() > 0)
                    subtitle.append(" • ");
                subtitle.append(String.format("%.2f €", estimatedValue));
            }

            return subtitle.toString();
        }

        @Override
        public Image getDetailImage() {
            // Pour l'instant, pas d'image spécifique pour les RMA
            return null;
        }

        @Override
        public String getQRCodeData() {
            // Les QR codes ne concernent que les équipements, pas les RMA
            return null;
        }

        @Override
        public VBox getDetailInfoContent() {
            VBox content = new VBox(8);

            // Section informations RMA
            Label rmaInfoLabel = new Label("📦 Informations RMA");
            rmaInfoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: "
                    + StandardColors.DANGER_RED + "; -fx-padding: 5 0;");
            content.getChildren().add(rmaInfoLabel);

            if (equipmentName != null && !equipmentName.trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Équipement", equipmentName));
            }

            if (returnReason != null && !returnReason.trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Motif de retour", returnReason));
            }

            if (customerName != null && !customerName.trim().isEmpty()) {
                content.getChildren().add(DetailPanel.createInfoRow("Client", customerName));
            }

            if (status != null) {
                content.getChildren().add(DetailPanel.createInfoRow("Statut", getRMAStatusIcon() + " " + status));
            }

            // Section financière
            if (estimatedValue != null && estimatedValue > 0) {
                Label costLabel = new Label("💰 Valeur estimée");
                costLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: "
                        + StandardColors.DANGER_RED + "; -fx-padding: 10 0 5 0;");
                content.getChildren().add(costLabel);
                content.getChildren().add(DetailPanel.createInfoRow("Valeur", String.format("%.2f €", estimatedValue)));
            }

            // Section historique RMA
            Label historyLabel = new Label("📅 Historique RMA");
            historyLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: "
                    + StandardColors.DANGER_RED + "; -fx-padding: 10 0 5 0;");
            content.getChildren().add(historyLabel);

            if (createdAt != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                content.getChildren().add(DetailPanel.createInfoRow("📅 Créé le", createdAt.format(formatter)));
            }

            // Ajout d'un historique détaillé des étapes RMA
            if (status != null) {
                content.getChildren().add(DetailPanel.createInfoRow("🔄 Étape actuelle", status));

                // Simulation d'un historique d'étapes
                TextArea historyArea = new TextArea(generateRMAHistory());
                historyArea.setPrefRowCount(4);
                historyArea.setWrapText(true);
                historyArea.setEditable(false);
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                content.getChildren().add(historyArea);
            }

            if (description != null && !description.trim().isEmpty()) {
                Label descLabel = new Label("📝 Description");
                descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: "
                        + StandardColors.DANGER_RED + "; -fx-padding: 10 0 5 0;");
                content.getChildren().add(descLabel);

                TextArea descArea = new TextArea(description);
                descArea.setPrefRowCount(2);
                descArea.setWrapText(true);
                descArea.setEditable(false);
                // $varName supprimÃ© - Style gÃ©rÃ© par CSS
                content.getChildren().add(descArea);
            }

            return content;
        }

        @Override
        public String getDetailId() {
            return rmaNumber != null ? rmaNumber : "";
        }

        // Méthodes utilitaires
        private String getRMAStatusIcon() {
            if (status == null)
                return "❓";
            switch (status.toLowerCase()) {
                case "initié":
                    return "📋";
                case "autorisé":
                    return "✅";
                case "en transit retour":
                    return "🚚";
                case "reçu":
                    return "📦";
                case "en cours d'analyse":
                    return "🔍";
                case "réparé":
                    return "🔧";
                case "remplacé":
                    return "🔄";
                case "remboursé":
                    return "💰";
                case "refusé":
                    return "❌";
                default:
                    return "❓";
            }
        }

        private String generateRMAHistory() {
            StringBuilder history = new StringBuilder();
            history.append("═══ HISTORIQUE DES ÉTAPES ═══\n\n");

            if (createdAt != null) {
                history.append("📅 ").append(createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }

            // Simulation d'étapes basées sur le statut actuel
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "autorisé":
                        history.append("✅ RMA autorisé par le service qualité\n");
                        break;
                    case "en transit retour":
                        history.append("✅ RMA autorisé\n");
                        history.append("🚚 Équipement en cours de retour\n");
                        break;
                    case "reçu":
                        history.append("✅ RMA autorisé\n");
                        history.append("🚚 Retour effectué\n");
                        history.append("📦 Équipement reçu en entrepôt\n");
                        break;
                    case "en cours d'analyse":
                        history.append("✅ RMA autorisé\n");
                        history.append("📦 Équipement reçu\n");
                        history.append("🔍 Analyse technique en cours\n");
                        break;
                    case "réparé":
                        history.append("✅ Analyse terminée\n");
                        history.append("🔧 Réparation effectuée\n");
                        break;
                    case "remplacé":
                        history.append("✅ Analyse terminée\n");
                        history.append("🔄 Remplacement autorisé\n");
                        break;
                }
            }

            return history.toString();
        }
    }

    // Getters publics pour l'accès aux contrôles de filtrage depuis SAVManagerView
    public TextField getRmaSearchField() {
        return rmaSearchField;
    }

    public ComboBox<String> getRmaStatusFilter() {
        return rmaStatusFilter;
    }

    public ComboBox<String> getRmaTypeFilter() {
        return rmaTypeFilter;
    }

    public DatePicker getRmaDateFrom() {
        return rmaDateFrom;
    }

    public DatePicker getRmaDateTo() {
        return rmaDateTo;
    }

    // Méthode publique pour appliquer les filtres depuis la toolbar
    public void applyFiltersFromToolbar() {
        // Appel à la méthode privée existante
        applyRMAFilters();
    }
}
