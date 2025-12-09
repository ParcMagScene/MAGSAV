package com.magscene.magsav.desktop.view.equipment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.config.EquipmentPreferencesManager;
import com.magscene.magsav.desktop.service.business.EquipmentService;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
    private ObservableList<EquipmentItem> allEquipmentData; // Données complètes pour filtrage local
    private EquipmentService equipmentService;
    
    // Références aux filtres pour réinitialisation
    private TextField searchField;
    private ComboBox<String> categoryCombo;
    private ComboBox<String> subCategoryCombo;
    private ComboBox<String> statusCombo;
    private ComboBox<String> ownerCombo;

    @Override
    protected void initializeContent() {
        // CRITICAL: Initialiser equipmentData ICI
        if (equipmentData == null) {
            equipmentData = FXCollections.observableArrayList();
            allEquipmentData = FXCollections.observableArrayList();
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
                text -> applyFilters());
        // Récupérer le TextField de la recherche pour le reset
        searchField = (TextField) searchBox.getChildren().stream()
                .filter(n -> n instanceof TextField)
                .findFirst().orElse(null);

        // 🎵 Filtre catégorie principale avec ViewUtils
        VBox categoryBox = ViewUtils.createFilterBox("🎵 Catégorie",
                new String[] { "Toutes catégories", "SONORISATION", "ECLAIRAGE", "VIDEO", "STRUCTURE" },
                "Toutes catégories", value -> applyFilters());
        // Récupérer le ComboBox de catégorie pour le reset
        categoryCombo = (ComboBox<String>) categoryBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);

        // 📁 Filtre sous-catégorie avec ViewUtils
        VBox subCategoryBox = ViewUtils.createFilterBox("📁 Sous-catégorie",
                new String[] { "Toutes sous-catégories", "ENCEINTE", "ENCEINTES PASSIVES", "AMPLIFICATEUR", 
                    "CONSOLE", "SYSTEMES HF", "CABLAGE", "DISTRIBUTION", "PERIPHERIQUES", "LECTEURS",
                    "MICROS DYNAMIQUES", "MICROS STATIQUES", "BACKLINE",
                    "PROJECTEURS ASSERVIS", "PROJECTEURS TRADITIONNELS", "BLOC DE PUISSANCE", "GRADATEURS",
                    "ACCROCHES", "MOTEUR", "FLIGHT-CASE", "HABILLAGE",
                    "ECRANS", "ECRANS LED" },
                "Toutes sous-catégories", value -> applyFilters());
        // Récupérer le ComboBox de sous-catégorie pour le reset
        subCategoryCombo = (ComboBox<String>) subCategoryBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Disponible", "En location", "Maintenance", "Hors service" },
                "Tous statuts", value -> applyFilters());
        // Récupérer le ComboBox de statut pour le reset
        statusCombo = (ComboBox<String>) statusBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);

        // 🏢 Filtre propriétaire avec ViewUtils
        // Par défaut MAG SCENE, sauf si préférence "Tous propriétaires" activée
        EquipmentPreferencesManager prefManager = EquipmentPreferencesManager.getInstance();
        String defaultOwner = prefManager.isShowAllOwners() ? "Tous propriétaires" : "MAG SCENE";
        VBox ownerBox = ViewUtils.createFilterBox("🏢 Propriétaire",
                new String[] { "Tous propriétaires", "MAG SCENE", "RENTAL", "NICLEN", "AED RENT" },
                defaultOwner, value -> applyFilters());
        // Récupérer le ComboBox de propriétaire pour le reset
        ownerCombo = (ComboBox<String>) ownerBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);
        
        // Enregistrer le callback pour rafraîchir quand les préférences changent
        prefManager.setOnPreferencesChanged(() -> {
            if (ownerCombo != null) {
                String newDefault = prefManager.isShowAllOwners() ? "Tous propriétaires" : "MAG SCENE";
                ownerCombo.setValue(newDefault);
                applyFilters();
            }
        });

        // 🔄 Bouton réinitialiser les filtres
        Button resetButton = new Button("🔄 Réinitialiser");
        resetButton.getStyleClass().add("secondary-button");
        resetButton.setOnAction(e -> resetFilters());
        VBox resetBox = new VBox(5);
        resetBox.getChildren().addAll(new Label(" "), resetButton);

        toolbar.getChildren().addAll(searchBox, categoryBox, subCategoryBox, statusBox, ownerBox, resetBox);
    }
    
    /**
     * Réinitialise tous les filtres à leurs valeurs par défaut
     */
    private void resetFilters() {
        if (searchField != null) {
            searchField.clear();
        }
        if (categoryCombo != null) {
            categoryCombo.setValue("Toutes catégories");
        }
        if (subCategoryCombo != null) {
            subCategoryCombo.setValue("Toutes sous-catégories");
        }
        if (statusCombo != null) {
            statusCombo.setValue("Tous statuts");
        }
        if (ownerCombo != null) {
            // Remettre au propriétaire par défaut selon les préférences
            EquipmentPreferencesManager prefManager = EquipmentPreferencesManager.getInstance();
            String defaultOwner = prefManager.isShowAllOwners() ? "Tous propriétaires" : "MAG SCENE";
            ownerCombo.setValue(defaultOwner);
        }
        // Recharger toutes les données
        equipmentData.setAll(allEquipmentData);
        updateStatus("✅ Filtres réinitialisés - " + equipmentData.size() + " équipements");
    }
    
    /**
     * Applique les filtres de recherche, catégorie et statut
     */
    private void applyFilters() {
        String searchText = (searchField != null) ? searchField.getText().toLowerCase().trim() : "";
        String selectedCategory = (categoryCombo != null) ? categoryCombo.getValue() : "Toutes catégories";
        String selectedSubCategory = (subCategoryCombo != null) ? subCategoryCombo.getValue() : "Toutes sous-catégories";
        String selectedStatus = (statusCombo != null) ? statusCombo.getValue() : "Tous statuts";
        String selectedOwner = (ownerCombo != null) ? ownerCombo.getValue() : "Tous propriétaires";
        
        // Filtrage local sur allEquipmentData
        List<EquipmentItem> filtered = allEquipmentData.stream()
                .filter(item -> {
                    // Filtre recherche
                    if (!searchText.isEmpty()) {
                        String name = item.getName() != null ? item.getName().toLowerCase() : "";
                        String brand = item.getBrand() != null ? item.getBrand().toLowerCase() : "";
                        String qrCode = item.getQrCode() != null ? item.getQrCode().toLowerCase() : "";
                        String supplier = item.getSupplier() != null ? item.getSupplier().toLowerCase() : "";
                        if (!name.contains(searchText) && !brand.contains(searchText) && 
                            !qrCode.contains(searchText) && !supplier.contains(searchText)) {
                            return false;
                        }
                    }
                    // Filtre catégorie parente
                    if (!"Toutes catégories".equals(selectedCategory)) {
                        String parentCategory = item.getParentCategory() != null ? item.getParentCategory() : "";
                        if (!parentCategory.equalsIgnoreCase(selectedCategory)) {
                            return false;
                        }
                    }
                    // Filtre sous-catégorie
                    if (!"Toutes sous-catégories".equals(selectedSubCategory)) {
                        String subCategory = item.getCategory() != null ? item.getCategory() : "";
                        if (!subCategory.equalsIgnoreCase(selectedSubCategory)) {
                            return false;
                        }
                    }
                    // Filtre statut
                    if (!"Tous statuts".equals(selectedStatus)) {
                        String status = item.getStatus() != null ? item.getStatus() : "";
                        if (!status.equalsIgnoreCase(selectedStatus)) {
                            return false;
                        }
                    }
                    // Filtre propriétaire
                    if (!"Tous propriétaires".equals(selectedOwner)) {
                        String owner = item.getSupplier() != null ? item.getSupplier() : "";
                        if (!owner.equalsIgnoreCase(selectedOwner)) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        equipmentData.setAll(filtered);
        updateStatus("🔍 " + filtered.size() + " équipements trouvés");
    }

    private TableView<EquipmentItem> createEquipmentTable() {
        TableView<EquipmentItem> table = new TableView<>();
        table.setItems(equipmentData);
        table.getStyleClass().add("equipment-table");

        // Colonnes pour les équipements
        TableColumn<EquipmentItem, String> idCol = new TableColumn<>("ID");
        TableColumn<EquipmentItem, String> nameCol = new TableColumn<>("Nom");
        TableColumn<EquipmentItem, String> brandCol = new TableColumn<>("Marque");
        TableColumn<EquipmentItem, String> parentCategoryCol = new TableColumn<>("Catégorie");
        TableColumn<EquipmentItem, String> categoryCol = new TableColumn<>("Sous-catégorie");
        TableColumn<EquipmentItem, String> statusCol = new TableColumn<>("Statut");
        TableColumn<EquipmentItem, String> supplierCol = new TableColumn<>("Propriétaire");
        TableColumn<EquipmentItem, String> qrCol = new TableColumn<>("QR Code");
        TableColumn<EquipmentItem, String> locationCol = new TableColumn<>("Emplacement");

        // Configuration des cellValueFactories simplifiées avec les getters du wrapper
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId()));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        parentCategoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getParentCategory()));
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        supplierCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSupplier()));
        qrCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQrCode()));
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));

        // Configuration des largeurs
        idCol.setPrefWidth(60);
        nameCol.setPrefWidth(200);
        brandCol.setPrefWidth(120);
        parentCategoryCol.setPrefWidth(120);
        categoryCol.setPrefWidth(150);
        statusCol.setPrefWidth(100);
        supplierCol.setPrefWidth(120);
        qrCol.setPrefWidth(100);
        locationCol.setPrefWidth(120);

        table.getColumns().addAll(idCol, nameCol, brandCol, parentCategoryCol, categoryCol, statusCol, supplierCol, qrCol, locationCol);

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

                    // Stocker les données complètes pour le filtrage local
                    allEquipmentData.clear();
                    equipmentData.clear();
                    
                    // Wrapper chaque Map dans un EquipmentItem
                    for (Map<String, Object> map : equipmentList) {
                        EquipmentItem item = new EquipmentItem(map);
                        allEquipmentData.add(item);
                        equipmentData.add(item);
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
        // Méthode conservée pour compatibilité, mais applyFilters() est utilisé
        applyFilters();
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