package com.magscene.magsav.desktop.view.equipment;

import com.magscene.magsav.desktop.view.base.BaseManagerView;
import com.magscene.magsav.desktop.service.business.EquipmentService;
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
 * Gestionnaire d'équipements refactorisé utilisant la nouvelle architecture
 * Remplace EquipmentManagerView et StandardEquipmentManagerView
 */
public class NewEquipmentManagerView extends BaseManagerView<Object> {
    private TableView<Object> equipmentTable;
    private ObservableList<Object> equipmentData;
    private EquipmentService equipmentService;

    @Override
    protected void initializeContent() {
        // Injection des dépendances via ApplicationContext
        this.equipmentService = getService(EquipmentService.class);
        this.equipmentData = FXCollections.observableArrayList();

        // Chargement initial des données
        loadEquipmentData();
    }

    @Override
    protected Pane createMainContent() {
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Panneau de recherche
        HBox searchPanel = createSearchPanel();

        // Table des équipements
        equipmentTable = createEquipmentTable();

        // Panneau de détails (à droite)
        Pane detailPanel = createDetailPanel();

        // Layout principal
        HBox mainLayout = new HBox(10);
        mainLayout.getChildren().addAll(
                new VBox(10, searchPanel, equipmentTable),
                detailPanel);

        HBox.setHgrow(equipmentTable.getParent(), Priority.ALWAYS);

        content.getChildren().add(mainLayout);
        VBox.setVgrow(mainLayout, Priority.ALWAYS);

        return content;
    }

    private HBox createSearchPanel() {
        HBox searchPanel = new HBox(10);
        searchPanel.setPadding(new Insets(5));
        searchPanel.getStyleClass().add("search-panel");

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Rechercher un équipement...");
        searchField.setPrefWidth(300);

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().addAll("Toutes catégories", "Audio", "Éclairage", "Vidéo");
        categoryFilter.setValue("Toutes catégories");

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous statuts", "Disponible", "En location", "Maintenance");
        statusFilter.setValue("Tous statuts");

        Button btnSearch = new Button("🔍 Rechercher");
        btnSearch.setOnAction(e -> performSearch(searchField.getText()));

        searchPanel.getChildren().addAll(
                new Label("Recherche:"), searchField,
                new Label("Catégorie:"), categoryFilter,
                new Label("Statut:"), statusFilter,
                btnSearch);

        return searchPanel;
    }

    private TableView<Object> createEquipmentTable() {
        TableView<Object> table = new TableView<>();
        table.setItems(equipmentData);
        table.getStyleClass().add("equipment-table");

        // Colonnes pour les équipements
        TableColumn<Object, String> idCol = new TableColumn<>("ID");
        TableColumn<Object, String> nameCol = new TableColumn<>("Nom");
        TableColumn<Object, String> brandCol = new TableColumn<>("Marque");
        TableColumn<Object, String> categoryCol = new TableColumn<>("Catégorie");
        TableColumn<Object, String> statusCol = new TableColumn<>("Statut");
        TableColumn<Object, String> qrCol = new TableColumn<>("QR Code");
        TableColumn<Object, String> locationCol = new TableColumn<>("Emplacement");

        // Configuration des cellValueFactories
        idCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty(String.valueOf(map.get("id")));
            }
            return new SimpleStringProperty("");
        });

        nameCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("name"));
            }
            return new SimpleStringProperty("");
        });

        brandCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("brand"));
            }
            return new SimpleStringProperty("");
        });

        categoryCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("category"));
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

        qrCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("qrCode"));
            }
            return new SimpleStringProperty("");
        });

        locationCol.setCellValueFactory(data -> {
            if (data.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data.getValue();
                return new SimpleStringProperty((String) map.get("location"));
            }
            return new SimpleStringProperty("");
        });

        // Configuration des largeurs
        idCol.setPrefWidth(60);
        nameCol.setPrefWidth(200);
        brandCol.setPrefWidth(120);
        categoryCol.setPrefWidth(150);
        statusCol.setPrefWidth(120);
        qrCol.setPrefWidth(120);
        locationCol.setPrefWidth(150);

        table.getColumns().addAll(idCol, nameCol, brandCol, categoryCol, statusCol, qrCol, locationCol);

        return table;
    }

    private Pane createDetailPanel() {
        VBox detailPanel = new VBox(10);
        detailPanel.setPrefWidth(300);
        detailPanel.setPadding(new Insets(10));
        detailPanel.getStyleClass().add("detail-panel");

        Label titleLabel = new Label("Détails de l'équipement");
        titleLabel.getStyleClass().add("detail-title");

        // Zone de détails (sera mise à jour selon la sélection)
        TextArea detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(10);
        detailsArea.setText("Sélectionnez un équipement pour voir ses détails");

        detailPanel.getChildren().addAll(titleLabel, detailsArea);

        return detailPanel;
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
                    equipmentData.addAll(equipmentList);

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

    @Override
    protected void addCustomToolbarItems(ToolBar toolbar) {
        // Boutons spécifiques aux équipements
        Button btnImport = new Button("📥 Import LOCMAT");
        Button btnExport = new Button("📤 Export");
        Button btnQRCode = new Button("📱 QR Code");

        btnImport.setOnAction(e -> handleImportLocmat());
        btnExport.setOnAction(e -> handleExport());
        btnQRCode.setOnAction(e -> handleQRCode());

        toolbar.getItems().addAll(
                new Separator(),
                btnImport, btnExport, btnQRCode);
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