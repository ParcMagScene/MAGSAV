package com.magscene.magsav.desktop.view.sav;

import com.magscene.magsav.desktop.view.base.BaseManagerView;
import com.magscene.magsav.desktop.service.business.SAVService;
import com.magscene.magsav.desktop.util.ViewUtils;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Gestionnaire SAV refactorisé utilisant la nouvelle architecture
 * Remplace SAVManagerView et StandardSAVManagerView
 */
public class NewSAVManagerView extends BaseManagerView<Object> {
    private TableView<Object> savTable;
    private ObservableList<Object> savData;
    private SAVService savService;

    @Override
    protected void initializeContent() {
        // Injection des dépendances via ApplicationContext
        this.savService = getService(SAVService.class);
        this.savData = FXCollections.observableArrayList();

        // Chargement initial des données
        loadSAVData();
    }

    @Override
    protected Pane createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Table des demandes SAV
        savTable = createSAVTable();

        // Panneau de détails et actions
        Pane detailPanel = createDetailPanel();

        // Layout principal avec splitter
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);

        VBox leftPanel = new VBox(10, savTable);
        VBox.setVgrow(savTable, Priority.ALWAYS);

        mainSplitPane.getItems().addAll(leftPanel, detailPanel);
        mainSplitPane.setDividerPositions(0.7); // 70% pour la table, 30% pour les détails

        content.getChildren().add(mainSplitPane);
        VBox.setVgrow(mainSplitPane, Priority.ALWAYS);

        return content;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "N° SAV, client, équipement...", text -> performSAVSearch(text, null, null));
        
        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
            new String[]{"Tous statuts", "Nouveau", "En cours", "En attente pièces", "Réparé", "Irréparable", "Fermé"},
            "Tous statuts", value -> loadSAVData());
        
        // ⚡ Filtre priorité avec ViewUtils
        VBox priorityBox = ViewUtils.createFilterBox("⚡ Priorité",
            new String[]{"Toutes priorités", "Urgente", "Haute", "Normale", "Basse"},
            "Toutes priorités", value -> loadSAVData());
        
        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox);

        DatePicker dateToPicker = new DatePicker();
    }

    private TableView<Object> createSAVTable() {
        TableView<Object> table = new TableView<>();
        table.setItems(savData);
        table.getStyleClass().add("sav-table");

        // Colonnes spécifiques au SAV
        TableColumn<Object, String> idCol = new TableColumn<>("N° SAV");
        TableColumn<Object, String> titleCol = new TableColumn<>("Titre");
        TableColumn<Object, String> typeCol = new TableColumn<>("Type");
        TableColumn<Object, String> statusCol = new TableColumn<>("Statut");
        TableColumn<Object, String> priorityCol = new TableColumn<>("Priorité");
        TableColumn<Object, String> dateCol = new TableColumn<>("Date création");
        TableColumn<Object, String> technicianCol = new TableColumn<>("Technicien");

        // Configuration des cellValueFactories
        idCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty(String.valueOf(map.get("id")));
            }
            return new SimpleStringProperty("");
        });

        titleCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("title"));
            }
            return new SimpleStringProperty("");
        });

        typeCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("type"));
            }
            return new SimpleStringProperty("");
        });

        statusCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("status"));
            }
            return new SimpleStringProperty("");
        });

        priorityCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("priority"));
            }
            return new SimpleStringProperty("");
        });

        dateCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                String date = (String) map.get("createdAt");
                if (date != null && date.length() >= 10) {
                    return new SimpleStringProperty(date.substring(0, 10));
                }
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty("");
        });

        technicianCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                String technician = (String) map.get("assignedTechnician");
                return new SimpleStringProperty(technician != null ? technician : "Non assigné");
            }
            return new SimpleStringProperty("");
        });

        // Configuration des colonnes
        idCol.setPrefWidth(80);
        titleCol.setPrefWidth(250);
        typeCol.setPrefWidth(120);
        statusCol.setPrefWidth(120);
        priorityCol.setPrefWidth(100);
        dateCol.setPrefWidth(100);
        technicianCol.setPrefWidth(150);

        table.getColumns().add(idCol);
        table.getColumns().add(titleCol);
        table.getColumns().add(typeCol);
        table.getColumns().add(statusCol);
        table.getColumns().add(priorityCol);
        table.getColumns().add(dateCol);
        table.getColumns().add(technicianCol);

        // Gestion de la sélection
        table.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateDetailPanel(newSelection));

        return table;
    }

    private Pane createDetailPanel() {
        VBox detailPanel = new VBox(10);
        detailPanel.setPrefWidth(400);
        detailPanel.setPadding(new Insets(10));
        detailPanel.getStyleClass().add("sav-detail-panel");

        Label titleLabel = new Label("Détails de la demande SAV");
        titleLabel.getStyleClass().add("detail-title");

        // Zone de détails avec onglets
        TabPane detailTabs = new TabPane();

        // Onglet Informations générales
        Tab infoTab = new Tab("Informations");
        infoTab.setClosable(false);
        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setPrefRowCount(8);
        infoArea.setText("Sélectionnez une demande SAV pour voir ses détails");
        infoTab.setContent(infoArea);

        // Onglet Historique
        Tab historyTab = new Tab("Historique");
        historyTab.setClosable(false);
        ListView<String> historyList = new ListView<>();
        historyTab.setContent(historyList);

        // Onglet Actions
        Tab actionsTab = new Tab("Actions");
        actionsTab.setClosable(false);
        VBox actionsBox = createActionsPanel();
        actionsTab.setContent(actionsBox);

        detailTabs.getTabs().addAll(infoTab, historyTab, actionsTab);

        detailPanel.getChildren().addAll(titleLabel, detailTabs);
        VBox.setVgrow(detailTabs, Priority.ALWAYS);

        return detailPanel;
    }

    private VBox createActionsPanel() {
        VBox actionsBox = new VBox(10);
        actionsBox.setPadding(new Insets(10));

        // Actions rapides
        Button btnChangeStatus = new Button("📝 Changer statut");
        Button btnAssignTechnician = new Button("👤 Assigner technicien");
        Button btnAddNote = new Button("📝 Ajouter note");
        Button btnPrintLabel = new Button("🖨️ Imprimer étiquette");
        Button btnGenerateQuote = new Button("💰 Générer devis");

        btnChangeStatus.setMaxWidth(Double.MAX_VALUE);
        btnAssignTechnician.setMaxWidth(Double.MAX_VALUE);
        btnAddNote.setMaxWidth(Double.MAX_VALUE);
        btnPrintLabel.setMaxWidth(Double.MAX_VALUE);
        btnGenerateQuote.setMaxWidth(Double.MAX_VALUE);

        btnChangeStatus.setOnAction(e -> handleChangeStatus());
        btnAssignTechnician.setOnAction(e -> handleAssignTechnician());
        btnAddNote.setOnAction(e -> handleAddNote());
        btnPrintLabel.setOnAction(e -> handlePrintLabel());
        btnGenerateQuote.setOnAction(e -> handleGenerateQuote());

        actionsBox.getChildren().addAll(
                new Label("Actions rapides:"),
                btnChangeStatus,
                btnAssignTechnician,
                btnAddNote,
                new Separator(),
                btnPrintLabel,
                btnGenerateQuote);

        return actionsBox;
    }

    private void loadSAVData() {
        updateStatus("Chargement des demandes SAV depuis le backend...");

        savService.loadAllSAVRequests().thenAccept(jsonResponse -> {
            Platform.runLater(() -> {
                try {
                    // Parser la réponse JSON avec Jackson
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> savList = mapper.readValue(
                            jsonResponse,
                            new TypeReference<List<Map<String, Object>>>() {
                            });

                    savData.clear();
                    savData.addAll(savList);

                    updateStatus("✅ " + savData.size() + " demandes SAV chargées depuis le backend");
                    System.out.println("✅ " + savData.size() + " demandes SAV chargées et affichées");

                } catch (Exception e) {
                    System.err.println("❌ Erreur parsing JSON SAV: " + e.getMessage());
                    e.printStackTrace();
                    updateStatus("❌ Erreur lors du chargement des données: " + e.getMessage());
                }
            });
        }).exceptionally(error -> {
            Platform.runLater(() -> {
                System.err.println("❌ Erreur backend SAV: " + error.getMessage());
                updateStatus("❌ Erreur lors du chargement depuis le backend: " + error.getMessage());
            });
            return null;
        });
    }

    private void performSAVSearch(String query, String status, String priority) {
        updateStatus("Recherche SAV: " + query);

        // TODO: Utiliser savService.searchSAVRequests()
        updateStatus("Recherche SAV effectuée (simulation)");
    }

    private void updateDetailPanel(Object selectedSAV) {
        if (selectedSAV != null) {
            updateStatus("Demande SAV sélectionnée");
            // TODO: Mettre à jour les détails
        }
    }

    // Actions des boutons
    private void handleChangeStatus() {
        Object selected = savTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Changement de statut en cours...");
            // TODO: Ouvrir dialog de changement de statut
        } else {
            updateStatus("Aucune demande sélectionnée");
        }
    }

    private void handleAssignTechnician() {
        updateStatus("Attribution de technicien...");
        // TODO: Ouvrir dialog d'attribution
    }

    private void handleAddNote() {
        updateStatus("Ajout de note...");
        // TODO: Ouvrir dialog de note
    }

    private void handlePrintLabel() {
        updateStatus("Impression d'étiquette...");
        // TODO: Générer et imprimer l'étiquette
    }

    private void handleGenerateQuote() {
        updateStatus("Génération de devis...");
        // TODO: Générer le devis
    }

    @Override
    protected void handleAdd() {
        updateStatus("Création d'une nouvelle demande SAV");
        // TODO: Ouvrir le dialog de création
    }

    @Override
    protected void handleEdit() {
        Object selected = savTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Modification de la demande SAV");
            // TODO: Ouvrir le dialog de modification
        } else {
            updateStatus("Aucune demande sélectionnée");
        }
    }

    @Override
    protected void handleDelete() {
        Object selected = savTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Suppression de la demande SAV");
            // TODO: Confirmer et supprimer
        } else {
            updateStatus("Aucune demande sélectionnée");
        }
    }

    @Override
    public void refresh() {
        loadSAVData();
    }

    @Override
    protected String getModuleName() {
        return "Service Après-Vente";
    }

    @Override
    protected String getViewCssClass() {
        return "sav-manager-view";
    }

    // Méthodes de filtrage
    private void applyFilters(String status, String priority, java.time.LocalDate dateFrom, java.time.LocalDate dateTo) {
        updateStatus("Application des filtres...");
        // TODO: Implémenter le filtrage
        loadSAVData();
    }

    private void resetFilters(ComboBox<String> statusFilter, ComboBox<String> priorityFilter, 
                             DatePicker dateFromPicker, DatePicker dateToPicker) {
        statusFilter.setValue("Tous statuts");
        priorityFilter.setValue("Toutes priorités");
        dateFromPicker.setValue(null);
        dateToPicker.setValue(null);
        updateStatus("Filtres réinitialisés");
        loadSAVData();
    }

    private void handleImportSAV() {
        updateStatus("Import SAV en cours...");
        // TODO: Implémenter l'import SAV
    }

    private void handleExportSAV() {
        updateStatus("Export SAV en cours...");
        // TODO: Implémenter l'export SAV
    }

    private void handleShowStatistics() {
        updateStatus("Affichage des statistiques SAV...");
        // savService.getSAVStatistics().thenAccept(stats -> {
        // Platform.runLater(() -> showStatisticsDialog(stats));
        // });
    }

    private void handleGenerateReports() {
        updateStatus("Génération de rapports...");
        // TODO: Ouvrir dialog de génération de rapports
    }

    // Méthode redéfinie héritée de BaseManagerView - pas besoin de redéfinition; //
    // La méthode getService() est déjà disponible via BaseManagerView
}