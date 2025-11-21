package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.view.base.AbstractManagerView;
import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.dialog.EquipmentDialog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire du parc matériel - VERSION STANDARDISÉE
 * Hérite d'AbstractManagerView pour respecter l'architecture uniforme
 * 
 * STRUCTURE AUTOMATIQUE :
 * - Top: Toolbar (recherche + filtres + actions)  
 * - Center: DetailPanelContainer (table + volet détail)
 */
public class StandardEquipmentManagerView extends AbstractManagerView {
    
    // ========================================
    // 🔧 COMPOSANTS SPÉCIFIQUES ÉQUIPEMENT; // ========================================
    
    private TableView<EquipmentItem> equipmentTable;
    private ObservableList<EquipmentItem> equipmentList;
    private FilteredList<EquipmentItem> filteredList;
    private SortedList<EquipmentItem> sortedList;
    
    // Filtres spécifiques équipement
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> brandFilter;
    
    // Boutons d'action
    private Button addButton;
    private Button editButton;
    private Button viewButton;
    private Button deleteButton;
    private Button duplicateButton;
    private Button exportButton;
    
    // ========================================
    // 🏗️ CONSTRUCTEUR; // ========================================
    
    public StandardEquipmentManagerView(ApiService apiService) {
        super(apiService);
        
        // Chargement des données après construction complète
        Platform.runLater(this::loadEquipmentData);
    }
    
    // ========================================
    // 📊 IMPLÉMENTATION ABSTRAITE OBLIGATOIRE; // ========================================
    
    @Override
    protected String getViewCssClass() {
        return "equipment-manager";
    }
    
    @Override
    protected String getSearchPromptText() {
        return "Nom, marque, modèle, numéro de série...";
    }
    
    @Override
    protected void initializeContent() {
        // Initialisation des données
        equipmentList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(equipmentList);
        sortedList = new SortedList<>(filteredList);
        
        // Création de la table
        createEquipmentTable();
    }
    
    @Override
    protected void createFilters() {
        // 📂 Filtre par catégorie
        addFilter("📂 Catégorie", 
            new String[]{"Toutes", "Éclairage", "Son", "Vidéo", "Structure", "Électricité", "Accessoires"}, 
            "Toutes", 
            this::onCategoryFilterChanged);
        
        // 📊 Filtre par statut  
        addFilter("📊 Statut",
            new String[]{"Tous", "Disponible", "Loué", "En maintenance", "Hors service"},
            "Tous",
            this::onStatusFilterChanged);
            
        // 🏷️ Filtre par marque
        addFilter("🏷️ Marque",
            new String[]{"Toutes", "Martin", "Robe", "Ayrton", "Clay Paky", "GLP", "Autres"},
            "Toutes", 
            this::onBrandFilterChanged);
        
        // Récupération des ComboBox pour les callbacks
        setupFilterReferences();
    }
    
    @Override
    protected void createActions() {
        // ➕ Ajouter équipement
        addButton = ViewUtils.createAddButton("➕ Nouvel équipement", this::addEquipment);
        addActionButton(addButton);
        
        // ✏️ Modifier équipement
        editButton = ViewUtils.createEditButton("✏️ Modifier", this::editSelectedEquipment, 
            getTableSelectionProperty().isNull());
        addActionButton(editButton);
        
        // 👀 Voir détails
        viewButton = ViewUtils.createDetailsButton("👀 Détails", this::viewSelectedEquipment,
            getTableSelectionProperty().isNull());
        addActionButton(viewButton);
        
        // 🗑️ Supprimer équipement
        deleteButton = ViewUtils.createDeleteButton("🗑️ Supprimer", this::deleteSelectedEquipment,
            getTableSelectionProperty().isNull());
        addActionButton(deleteButton);
        
        // 📋 Dupliquer équipement (bouton personnalisé)
        duplicateButton = new Button("📋 Dupliquer");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        duplicateButton.setOnAction(e -> duplicateSelectedEquipment());
        duplicateButton.disableProperty().bind(getTableSelectionProperty().isNull());
        addActionButton(duplicateButton);
        
        // 📊 Exporter liste (bouton personnalisé)
        exportButton = new Button("📊 Exporter");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        exportButton.setOnAction(e -> exportEquipmentList());
        addActionButton(exportButton);
    }
    
    @Override
    protected Region createCenterContent() {
        // DetailPanelContainer avec table + volet de détail intégré
        return new DetailPanelContainer(equipmentTable);
    }
    
    @Override
    protected void onSearchTextChanged(String searchText) {
        updateFilters();
    }
    
    // ========================================
    // 🔧 CRÉATION DE LA TABLE; // ========================================
    
    @SuppressWarnings("unchecked")
    private void createEquipmentTable() {
        equipmentTable = new TableView<>();
        equipmentTable.setItems(sortedList);
        
        // Bind sorting avec la table
        sortedList.comparatorProperty().bind(equipmentTable.comparatorProperty());
        
        // Colonnes de la table
        createTableColumns();
        
        // Configuration de la table
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    viewSelectedEquipment();
                }
            });
            return row;
        });
    }
    
    @SuppressWarnings("unchecked")
    private void createTableColumns() {
        // ID
        TableColumn<EquipmentItem, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        idColumn.setPrefWidth(60);
        
        // Nom
        TableColumn<EquipmentItem, String> nameColumn = new TableColumn<>("Nom");
        nameColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        nameColumn.setPrefWidth(200);
        
        // Marque
        TableColumn<EquipmentItem, String> brandColumn = new TableColumn<>("Marque");
        brandColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBrand()));
        brandColumn.setPrefWidth(120);
        
        // Modèle
        TableColumn<EquipmentItem, String> modelColumn = new TableColumn<>("Modèle");
        modelColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getModel()));
        modelColumn.setPrefWidth(150);
        
        // Catégorie
        TableColumn<EquipmentItem, String> categoryColumn = new TableColumn<>("Catégorie");
        categoryColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        categoryColumn.setPrefWidth(120);
        
        // Statut
        TableColumn<EquipmentItem, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        statusColumn.setPrefWidth(120);
        
        // Numéro de série
        TableColumn<EquipmentItem, String> serialColumn = new TableColumn<>("N° Série");
        serialColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSerialNumber()));
        serialColumn.setPrefWidth(150);
        
        equipmentTable.getColumns().addAll(idColumn, nameColumn, brandColumn, modelColumn, 
                                          categoryColumn, statusColumn, serialColumn);
    }
    
    // ========================================
    // 🔍 GESTION DES FILTRES; // ========================================
    
    private void setupFilterReferences() {
        // Récupération des ComboBox créées par addFilter(); // Cette méthode sera appelée après createFilters()
        Platform.runLater(() -> {
            if (filtersContainer.getChildren().size() >= 3) {
                categoryFilter = getFilterComboBox(0);
                statusFilter = getFilterComboBox(1); 
                brandFilter = getFilterComboBox(2);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private ComboBox<String> getFilterComboBox(int index) {
        try {
            return (ComboBox<String>) ((VBox) filtersContainer.getChildren().get(index)).getChildren().get(1);
        } catch (Exception e) {
            System.err.println("Erreur récupération ComboBox filtre " + index + ": " + e.getMessage());
            return null;
        }
    }
    
    private void onCategoryFilterChanged(String category) {
        updateFilters();
    }
    
    private void onStatusFilterChanged(String status) {
        updateFilters();
    }
    
    private void onBrandFilterChanged(String brand) {
        updateFilters();
    }
    
    private void updateFilters() {
        filteredList.setPredicate(equipment -> {
            // Filtre de recherche textuelle
            String searchText = getSearchField().getText();
            if (searchText != null && !searchText.trim().isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!equipment.getName().toLowerCase().contains(lowerCaseFilter) &&
                    !equipment.getBrand().toLowerCase().contains(lowerCaseFilter) &&
                    !equipment.getModel().toLowerCase().contains(lowerCaseFilter) &&
                    !equipment.getSerialNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }
            
            // Filtre par catégorie
            if (categoryFilter != null && categoryFilter.getValue() != null && 
                !categoryFilter.getValue().equals("Toutes")) {
                if (!equipment.getCategory().equals(categoryFilter.getValue())) {
                    return false;
                }
            }
            
            // Filtre par statut
            if (statusFilter != null && statusFilter.getValue() != null && 
                !statusFilter.getValue().equals("Tous")) {
                if (!equipment.getStatus().equals(statusFilter.getValue())) {
                    return false;
                }
            }
            
            // Filtre par marque
            if (brandFilter != null && brandFilter.getValue() != null && 
                !brandFilter.getValue().equals("Toutes")) {
                if (!equipment.getBrand().equals(brandFilter.getValue())) {
                    return false;
                }
            }
            
            return true;
        });
    }
    
    // ========================================
    // ⚡ ACTIONS DES BOUTONS; // ========================================
    
    private void addEquipment() {
        EquipmentDialog dialog = new EquipmentDialog(apiService, null);
        dialog.showAndWait().ifPresent(equipmentData -> {
            // TODO: Ajouter à la liste et sauvegarder via API
            System.out.println("➕ Ajout équipement: " + equipmentData);
            refresh();
        });
    }
    
    private void editSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Map<String, Object> equipmentData = selected.toMap();
            EquipmentDialog dialog = new EquipmentDialog(apiService, equipmentData);
            dialog.showAndWait().ifPresent(result -> {
                // TODO: Mettre à jour via API
                System.out.println("✏️ Modification équipement: " + result);
                refresh();
            });
        }
    }
    
    private void viewSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Map<String, Object> equipmentData = selected.toMap();
            EquipmentDialog dialog = new EquipmentDialog(apiService, equipmentData, true); // Mode lecture seule
            dialog.showAndWait();
        }
    }
    
    private void deleteSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirmation");
            confirmation.setHeaderText("Supprimer l'équipement");
            confirmation.setContentText("Êtes-vous sûr de vouloir supprimer \"" + selected.getName() + "\" ?");
            
            confirmation.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // TODO: Suppression via API
                    equipmentList.remove(selected);
                    System.out.println("🗑️ Suppression équipement: " + selected.getName());
                }
            });
        }
    }
    
    private void duplicateSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Map<String, Object> equipmentData = new HashMap<>(selected.toMap());
            equipmentData.put("name", selected.getName() + " (Copie)");
            equipmentData.remove("id"); // Nouvel ID sera généré
            equipmentData.remove("serialNumber"); // Nouveau numéro de série requis
            
            EquipmentDialog dialog = new EquipmentDialog(apiService, equipmentData);
            dialog.showAndWait().ifPresent(result -> {
                System.out.println("📋 Duplication équipement: " + result);
                refresh();
            });
        }
    }
    
    private void exportEquipmentList() {
        // TODO: Implémentation export (CSV, Excel, PDF...)
        System.out.println("📊 Export de " + equipmentList.size() + " équipements");
    }
    
    // ========================================
    // 📊 GESTION DES DONNÉES; // ========================================
    
    private void loadEquipmentData() {
        // TODO: Chargement via API; // Pour demo, ajout d'équipements fictifs
        Platform.runLater(() -> {
            equipmentList.addAll(
                new EquipmentItem("1", "Lyre LED 1", "Martin", "MAC Viper Profile", "Éclairage", "Disponible", "MAC001"),
                new EquipmentItem("2", "Console Son", "Yamaha", "CL5", "Son", "Loué", "YAM002"),
                new EquipmentItem("3", "Écran LED", "ROE", "CB5", "Vidéo", "En maintenance", "ROE003")
            );
            System.out.println("✅ " + equipmentList.size() + " équipements chargés");
        });
    }
    
    @Override
    protected void refresh() {
        super.refresh();
        equipmentList.clear();
        loadEquipmentData();
    }
    
    // ========================================
    // 🛠️ UTILITAIRES; // ========================================
    
    private ReadOnlyObjectProperty<EquipmentItem> getTableSelectionProperty() {
        return equipmentTable.getSelectionModel().selectedItemProperty();
    }
    
    // Classe interne pour les données d'équipement (temporaire, devrait être dans le model)
    public static class EquipmentItem {
        private String id;
        private String name;
        private String brand;
        private String model;
        private String category;
        private String status;
        private String serialNumber;
        
        public EquipmentItem(String id, String name, String brand, String model, 
                           String category, String status, String serialNumber) {
            this.id = id;
            this.name = name;
            this.brand = brand;
            this.model = model;
            this.category = category;
            this.status = status;
            this.serialNumber = serialNumber;
        }
        
        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getSerialNumber() { return serialNumber; }
        
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("name", name);
            map.put("brand", brand);
            map.put("model", model);
            map.put("category", category);
            map.put("status", status);
            map.put("serialNumber", serialNumber);
            return map;
        }
    }
}