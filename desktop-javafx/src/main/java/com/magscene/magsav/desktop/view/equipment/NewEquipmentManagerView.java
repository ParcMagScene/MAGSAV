package com.magscene.magsav.desktop.view.equipment;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.service.business.EquipmentService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Gestionnaire d'équipements refactorisé utilisant la nouvelle architecture
 * Remplace EquipmentManagerView et StandardEquipmentManagerView
 */
public class NewEquipmentManagerView extends BaseManagerView<EquipmentItem> {
    private TableView<EquipmentItem> equipmentTable;
    private ObservableList<EquipmentItem> equipmentData; // Déclaration sans initialisation
    private EquipmentService equipmentService;

    @Override
    protected void initializeContent() {
        // CRITICAL: Initialiser equipmentData ICI
        if (equipmentData == null) {
            equipmentData = FXCollections.observableArrayList();
            System.out.println("✅ equipmentData initialisé");
        }

        // Injection des dépendances via ApplicationContext
        this.equipmentService = getService(EquipmentService.class);

        // Binding du tableau après création
        if (equipmentTable != null && equipmentData != null) {
            equipmentTable.setItems(equipmentData);
            System.out.println("🔗 Tableau Equipment lié à equipmentData");
        }

        // Chargement initial des données
        loadEquipmentData();
    }

    @Override
    protected Pane createMainContent() {
        // Table des équipements
        equipmentTable = createEquipmentTable();

        // Utilisation du DetailPanelContainer pour le volet de détail et mise en
        // surbrillance
        DetailPanelContainer containerWithDetail = new DetailPanelContainer(equipmentTable);

        return containerWithDetail;
    }

    @Override
    protected void addCustomToolbarItems(HBox toolbar) {
        // 🔍 Recherche avec ViewUtils
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Nom, marque, QR code...",
                text -> performSearch(text));

        // 🎵 Filtre catégorie avec ViewUtils
        VBox categoryBox = ViewUtils.createFilterBox("🎵 Catégorie",
                new String[] { "Toutes catégories", "Audio", "Éclairage", "Vidéo", "Structure" },
                "Toutes catégories", value -> loadEquipmentData());

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Disponible", "En location", "Maintenance", "Hors service" },
                "Tous statuts", value -> loadEquipmentData());

        toolbar.getChildren().addAll(searchBox, categoryBox, statusBox);
    }

    private TableView<EquipmentItem> createEquipmentTable() {
        TableView<EquipmentItem> table = new TableView<>();
        table.setItems(equipmentData);
        table.getStyleClass().add("equipment-table");

        // Colonnes pour les équipements
        TableColumn<EquipmentItem, String> idCol = new TableColumn<>("ID");
        TableColumn<EquipmentItem, String> nameCol = new TableColumn<>("Nom");
        TableColumn<EquipmentItem, String> brandCol = new TableColumn<>("Marque");
        TableColumn<EquipmentItem, String> categoryCol = new TableColumn<>("Catégorie");
        TableColumn<EquipmentItem, String> statusCol = new TableColumn<>("Statut");
        TableColumn<EquipmentItem, String> qrCol = new TableColumn<>("QR Code");
        TableColumn<EquipmentItem, String> locationCol = new TableColumn<>("Emplacement");

        // Configuration des cellValueFactories simplifiées avec les getters du wrapper
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        qrCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQrCode()));
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));

        // Configuration des largeurs
        idCol.setPrefWidth(60);
        nameCol.setPrefWidth(200);
        brandCol.setPrefWidth(120);
        categoryCol.setPrefWidth(150);
        statusCol.setPrefWidth(120);
        qrCol.setPrefWidth(120);
        locationCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, nameCol, brandCol, categoryCol, statusCol, qrCol, locationCol);

        // Style de sélection uniforme
        table.setRowFactory(tv -> {
            TableRow<EquipmentItem> row = new TableRow<>();

            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    row.setStyle("-fx-background-color: " + ThemeManager.getInstance().getSelectionColor() + "; " +
                            "-fx-text-fill: " + ThemeManager.getInstance().getSelectionTextColor() + "; " +
                            "-fx-border-color: " + ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                            "-fx-border-width: 1px;");
                } else {
                    row.setStyle("");
                }
            };

            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());

            return row;
        });

        return table;
    }

    private void loadEquipmentData() {
        updateStatus("Chargement des équipements depuis le backend...");

        equipmentService.loadAllEquipments().thenAccept(jsonResponse -> {
            Platform.runLater(() -> {
                try {
                    // Parser la réponse JSON avec Jackson
                    ObjectMapper mapper = new ObjectMapper();
                    List<Map<String, Object>> equipmentList = mapper.readValue(
                            jsonResponse,
                            new TypeReference<List<Map<String, Object>>>() {
                            });

                    equipmentData.clear();
                    // Wrapper chaque Map dans un EquipmentItem
                    for (Map<String, Object> map : equipmentList) {
                        equipmentData.add(new EquipmentItem(map));
                    }

                    // Forcer le rafraîchissement du tableau
                    if (equipmentTable != null) {
                        equipmentTable.refresh();
                        System.out
                                .println("🔄 Tableau Equipment rafraîchi - Items: " + equipmentTable.getItems().size());
                    }

                    updateStatus("✅ " + equipmentData.size() + " équipements chargés depuis le backend");
                    System.out.println("✅ " + equipmentData.size() + " équipements chargés et affichés");

                } catch (Exception e) {
                    System.err.println("❌ Erreur parsing JSON équipements: " + e.getMessage());
                    e.printStackTrace();
                    updateStatus("❌ Erreur lors du chargement des données: " + e.getMessage());
                }
            });
        }).exceptionally(error -> {
            Platform.runLater(() -> {
                System.err.println("❌ Erreur backend équipements: " + error.getMessage());
                updateStatus("❌ Erreur lors du chargement depuis le backend: " + error.getMessage());
            });
            return null;
        });
    }

    private void performSearch(String query) {
        updateStatus("Recherche: " + query);
        // TODO: Implémenter la recherche
    }

    @Override
    protected void handleAdd() {
        updateStatus("Ajout d'un nouvel équipement");
        // TODO: Ouvrir le dialog d'ajout
    }

    @Override
    protected void handleEdit() {
        Object selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Modification de l'équipement sélectionné");
            // TODO: Ouvrir le dialog de modification
        } else {
            updateStatus("Aucun équipement sélectionné");
        }
    }

    @Override
    protected void handleDelete() {
        Object selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Suppression de l'équipement sélectionné");
            // TODO: Confirmer et supprimer
        } else {
            updateStatus("Aucun équipement sélectionné");
        }
    }

    @Override
    public void refresh() {
        loadEquipmentData();
    }

    @Override
    protected String getModuleName() {
        return "Parc Matériel";
    }

    @Override
    protected String getViewCssClass() {
        return "equipment-manager-view";
    }

    private void handleImportLocmat() {
        updateStatus("Import LOCMAT en cours...");

        // TODO: Ouvrir un dialog pour sélectionner le fichier; // String filePath =
        // showFileChooser();
        // if (filePath != null) {
        // equipmentApiClient.importLocmat(filePath); // .thenRun(() ->
        // Platform.runLater(() -> {
        // updateStatus("Import LOCMAT terminé");
        // refresh();
        // })); // .exceptionally(error -> {
        // Platform.runLater(() -> updateStatus("Erreur import: " +
        // error.getMessage()));
        // return null;
        // });
        // }

        updateStatus("Import LOCMAT simulé");
    }

    private void handleExport() {
        updateStatus("Export des équipements...");
        // TODO: Implémenter l'export
    }

    private void handleQRCode() {
        updateStatus("Génération des QR Codes...");
        // TODO: Implémenter la génération de QR codes
    }
}