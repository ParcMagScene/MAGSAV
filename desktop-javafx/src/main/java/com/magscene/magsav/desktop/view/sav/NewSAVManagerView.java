package com.magscene.magsav.desktop.view.sav;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.service.business.SAVService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Gestionnaire SAV refactorisé utilisant la nouvelle architecture
 * Remplace SAVManagerView et StandardSAVManagerView
 */
public class NewSAVManagerView extends BaseManagerView<SAVRequestItem> {
    private TableView<SAVRequestItem> savTable;
    private ObservableList<SAVRequestItem> savData; // Déclaration sans initialisation
    private SAVService savService;

    @Override
    protected void initializeContent() {
        System.out.println("🔧 NewSAVManagerView.initializeContent() - Début");

        // CRITICAL: Initialiser savData ICI, pas au niveau de la classe
        if (savData == null) {
            savData = FXCollections.observableArrayList();
            System.out.println("   ✅ savData initialisé");
        }

        System.out.println("   savTable: " + (savTable != null ? "NON NULL" : "NULL"));
        System.out.println("   savData: " + (savData != null ? "NON NULL (size=" + savData.size() + ")" : "NULL"));

        // Injection des dépendances via ApplicationContext
        this.savService = getService(SAVService.class);

        // savData déjà initialisé au niveau de la classe
        // Binding du tableau après création (évite NPE)
        if (savTable != null && savData != null) {
            savTable.setItems(savData);
            System.out.println("   ✅ Tableau SAV lié à savData");

            // Debug : Logger les changements dans la liste
            savData.addListener((javafx.collections.ListChangeListener<Object>) change -> {
                System.out.println("🔔 savData modifié - Taille: " + savData.size() + " - Items tableau: "
                        + savTable.getItems().size());
            });
        } else {
            System.out.println("   ❌ ERREUR: savTable ou savData est NULL !");
        }

        // Chargement initial des données
        System.out.println("🔧 NewSAVManagerView.initializeContent() - Appel loadSAVData()");
        loadSAVData();
    }

    @Override
    protected Pane createMainContent() {
        // Table des demandes SAV
        savTable = createSAVTable();

        // Utilisation du DetailPanelContainer comme dans les autres vues
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(savTable);

        return containerWithDetail;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "N° SAV, client, équipement...",
                text -> performSAVSearch(text, null, null));

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Nouveau", "En cours", "En attente pièces", "Réparé", "Irréparable",
                        "Fermé" },
                "Tous statuts", value -> loadSAVData());

        // ⚡ Filtre priorité avec ViewUtils
        VBox priorityBox = ViewUtils.createFilterBox("⚡ Priorité",
                new String[] { "Toutes priorités", "Urgente", "Haute", "Normale", "Basse" },
                "Toutes priorités", value -> loadSAVData());

        toolbar.getChildren().addAll(searchBox, statusBox, priorityBox);

        DatePicker dateToPicker = new DatePicker();
    }

    private TableView<SAVRequestItem> createSAVTable() {
        TableView<SAVRequestItem> table = new TableView<>();
        table.setItems(savData);
        table.getStyleClass().add("sav-table");

        // Colonnes spécifiques au SAV
        TableColumn<SAVRequestItem, String> idCol = new TableColumn<>("N° SAV");
        TableColumn<SAVRequestItem, String> titleCol = new TableColumn<>("Titre");
        TableColumn<SAVRequestItem, String> typeCol = new TableColumn<>("Type");
        TableColumn<SAVRequestItem, String> statusCol = new TableColumn<>("Statut");
        TableColumn<SAVRequestItem, String> priorityCol = new TableColumn<>("Priorité");
        TableColumn<SAVRequestItem, String> dateCol = new TableColumn<>("Date création");
        TableColumn<SAVRequestItem, String> technicianCol = new TableColumn<>("Technicien");

        // Configuration des cellValueFactories
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        priorityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPriority()));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt()));
        technicianCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAssignedTechnician()));

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

        // Style de sélection uniforme
        table.setRowFactory(tv -> {
            TableRow<SAVRequestItem> row = new TableRow<>();

            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                            "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                            "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                            "-fx-border-width: 1px;");
                } else {
                    // Style par défaut
                    row.setStyle("");
                }
            };

            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit();
                }
            });
            return row;
        });

        return table;
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
                    for (Map<String, Object> map : savList) {
                        savData.add(new SAVRequestItem(map));
                    }

                    // Forcer le rafraîchissement du tableau
                    if (savTable != null) {
                        savTable.refresh();
                        System.out.println("🔄 Tableau SAV rafraîchi - Items: " + savTable.getItems().size());
                    }

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
    private void applyFilters(String status, String priority, java.time.LocalDate dateFrom,
            java.time.LocalDate dateTo) {
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

    // Méthode redéfinie héritée de BaseManagerView - pas besoin de redéfinition
    // La méthode getService() est déjà disponible via BaseManagerView
}