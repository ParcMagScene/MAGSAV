package com.magscene.magsav.desktop.view;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.magscene.magsav.desktop.dialog.EquipmentDialog;
import com.magscene.magsav.desktop.view.dialog.LocmatImportDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Gestionnaire du parc matériel avec interface complète
 * - TableView des équipements avec tri et filtrage
 * - Toolbar avec actions (Ajouter, Modifier, Supprimer, QR codes, etc.)
 * - Volet de visualisation détaillée
 * - Filtres par catégorie, statut, recherche textuelle
 */
public class EquipmentManagerView extends BorderPane {
    private final ApiService apiService;
    
    // Composants de l'interface
    private TableView<EquipmentItem> equipmentTable;
    private ObservableList<EquipmentItem> equipmentList;
    private FilteredList<EquipmentItem> filteredList;
    private SortedList<EquipmentItem> sortedList;
    
    // Filtres et recherche
    private TextField searchField;
    private ComboBox<String> categoryFilter;
    private ComboBox<String> statusFilter;
    private ComboBox<String> brandFilter;
    
    // Conteneur avec volet de détails intégré
    private DetailPanelContainer tableContainer;
    
    // Toolbar standardisée
    private HBox toolbar;
    private Button addButton, editButton, deleteButton, duplicateButton, exportButton, importLocmatButton;
    
    public EquipmentManagerView(ApiService apiService) {
        this.apiService = apiService;
        
        // Initialiser les données
        equipmentList = FXCollections.observableArrayList();
        filteredList = new FilteredList<>(equipmentList, p -> true);
        sortedList = new SortedList<>(filteredList);
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadEquipmentData();
        
        // Appliquer les styles CSS
        getStyleClass().add("equipment-manager");
    }
    
    /**
     * Initialise tous les composants de l'interface
     */
    private void initializeComponents() {
        // === TABLE DES EQUIPEMENTS ===
        createEquipmentTable();
        
        // === TOOLBAR STANDARDISÉE ===
        toolbar = createToolbar();
        
        // === CONTENEUR AVEC VOLET DE DETAILS INTEGRE ===
        createTableContainer();
    }
    
    /**
     * Crée la toolbar avec les boutons d'actions - STANDARD ViewUtils
     */
    private HBox createToolbar() {
        HBox toolbar = new HBox(10); // EXACTEMENT comme PersonnelManagerView
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(10)); // EXACTEMENT comme PersonnelManagerView
        // toolbar supprimé - Style géré par CSS
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Nom, marque, modèle, numéro de série...", text -> updateFilters());
        searchField = (TextField) searchBox.getChildren().get(1);
        
        // Force des couleurs pour uniformiser l'apparence
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);
        
        // 🔧 Filtre par catégorie avec ViewUtils
        VBox categoryBox = ViewUtils.createFilterBox("📂 Catégorie", 
            new String[]{"Toutes", "Éclairage", "Son", "Vidéo", "Structure", "Électricité", "Accessoires"}, 
            "Toutes", value -> updateFilters());
        // Cast sécurisé avec vérification de type
        if (categoryBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) categoryBox.getChildren().get(1);
            categoryFilter = combo;
        }
        
        // 🔧 Filtre par statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut", 
            new String[]{"Tous", "Disponible", "Loué", "En maintenance", "Hors service"}, 
            "Tous", value -> updateFilters());
        // Cast sécurisé avec vérification de type
        if (statusBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) statusBox.getChildren().get(1);
            statusFilter = combo;
        }
        
        // 🔧 Filtre par marque avec ViewUtils
        VBox brandBox = ViewUtils.createFilterBox("🏷️ Marque", 
            new String[]{"Toutes", "Martin", "Robe", "Ayrton", "Clay Paky", "GLP", "Autres"}, 
            "Toutes", value -> updateFilters());
        // Cast sécurisé avec vérification de type
        if (brandBox.getChildren().get(1) instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> combo = (ComboBox<String>) brandBox.getChildren().get(1);
            brandFilter = combo;
        }
        
        // 🔧 Boutons d'action avec ViewUtils
        VBox actionsBox = new VBox(5);
        Label actionsLabel = new Label("⚡ Actions");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        HBox buttonRow = new HBox(10);
        addButton = ViewUtils.createAddButton("➕ Nouvel équipement", this::addEquipment);
        editButton = ViewUtils.createEditButton("✏️ Modifier", this::editSelectedEquipment, 
            equipmentTable.getSelectionModel().selectedItemProperty().isNull());
        Button viewButton = ViewUtils.createDetailsButton("👀 Détails", () -> {
            EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openEquipmentDetails(selected);
            }
        }, equipmentTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton = ViewUtils.createDeleteButton("🗑️ Supprimer", this::deleteSelectedEquipment,
            equipmentTable.getSelectionModel().selectedItemProperty().isNull());
        // Dupliquer - utiliser un bouton personnalisé
        duplicateButton = new Button("📋 Dupliquer");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        duplicateButton.setOnAction(e -> duplicateSelectedEquipment());
        duplicateButton.disableProperty().bind(equipmentTable.getSelectionModel().selectedItemProperty().isNull());
        // Exporter - utiliser un bouton personnalisé 
        exportButton = new Button("📊 Exporter");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        exportButton.setOnAction(e -> exportEquipmentList());
        
        // Import LOCMAT - bouton personnalisé avec icône Excel
        importLocmatButton = new Button("📥 Import LOCMAT");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        importLocmatButton.setOnAction(e -> openLocmatImportDialog());
        
        buttonRow.getChildren().addAll(addButton, editButton, viewButton, deleteButton, duplicateButton, exportButton, importLocmatButton);
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);
        
        // Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        toolbar.getChildren().addAll(searchBox, categoryBox, statusBox, brandBox, spacer, actionsBox);
        return toolbar;
    }
    
    /**
     * Crée la table des équipements avec les colonnes
     */
    @SuppressWarnings("unchecked")
    private void createEquipmentTable() {
        equipmentTable = new TableView<>();
        equipmentTable.getStyleClass().add("equipment-table");
        equipmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        // Configuration du tableau avec style moderne uniforme; // Les styles sont gérés automatiquement par CSS; // Colonne ID
        TableColumn<EquipmentItem, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setMinWidth(50);
        
        // Colonne Nom
        TableColumn<EquipmentItem, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(150);
        
        // Colonne Catégorie
        TableColumn<EquipmentItem, String> categoryCol = new TableColumn<>("Catégorie");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        categoryCol.setPrefWidth(120);
        
        // Colonne Statut avec cellule colorée
        TableColumn<EquipmentItem, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(column -> {
            return new TableCell<EquipmentItem, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        // Appliquer couleur selon le statut
                        switch (status.toLowerCase()) {
                            case "disponible":
                                // Style gere par CSS
                                break;
                            case "en maintenance":
                                // Style gere par CSS
                                break;
                            case "hors service":
                                // Style gere par CSS
                                break;
                            case "en sav":
                                // Style gere par CSS
                                break;
                            default:
                                // Style gere par CSS
                        }
                    }
                }
            };
        });
        
        // Colonne Marque
        TableColumn<EquipmentItem, String> brandCol = new TableColumn<>("Marque");
        brandCol.setCellValueFactory(new PropertyValueFactory<>("brand"));
        brandCol.setPrefWidth(100);
        
        // Colonne Modèle
        TableColumn<EquipmentItem, String> modelCol = new TableColumn<>("Modèle");
        modelCol.setCellValueFactory(new PropertyValueFactory<>("model"));
        modelCol.setPrefWidth(120);
        
        // Colonne Emplacement
        TableColumn<EquipmentItem, String> locationCol = new TableColumn<>("Emplacement");
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        locationCol.setPrefWidth(150);
        
        equipmentTable.getColumns().addAll(idCol, nameCol, categoryCol, statusCol, brandCol, modelCol, locationCol);
        
        // Connecter au tri
        sortedList.comparatorProperty().bind(equipmentTable.comparatorProperty());
        equipmentTable.setItems(sortedList);
        
        // Message si liste vide
        equipmentTable.setPlaceholder(new Label("Aucun équipement trouvé"));
    }

    /**
     * Crée le conteneur qui intègre automatiquement le volet de détails
     */
    private void createTableContainer() {
        // Utiliser le wrapper automatique qui intègre le DetailPanel
        tableContainer = DetailPanelContainer.wrapTableView(equipmentTable);
    }
    
    /**
     * Organise la disposition des composants
     */
    private void setupLayout() {
        // Top: Toolbar standardisée ViewUtils (contient déjà tous les filtres et actions)
        setTop(toolbar);
        
        // Center: Directement le tableContainer sans VBox intermédiaire; // pour éviter l'effet de container visible
        setCenter(tableContainer);
    }
    
    /**
     * Configure les gestionnaires d'événements
     */
    private void setupEventHandlers() {
        // Sélection dans la table - le DetailPanelContainer gère automatiquement l'affichage
        equipmentTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                updateButtonStates(newSelection != null);
            }
        );
        
        // Style de sélection uniforme avec système de surlignage
        equipmentTable.setRowFactory(tv -> {
            TableRow<EquipmentItem> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection uniforme - même système que les autres modules
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
            
            // Double-clic pour ouvrir la fiche détaillée (conservé)
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openEquipmentDetails(row.getItem());
                }
            });
            
            return row;
        });
        
        // Filtres
        searchField.textProperty().addListener((obs, oldText, newText) -> updateFilters());
        categoryFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilters());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilters());
        brandFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilters());
        
        // Actions des boutons
        addButton.setOnAction(e -> addEquipment());
        editButton.setOnAction(e -> editSelectedEquipment());
        deleteButton.setOnAction(e -> deleteSelectedEquipment());
        duplicateButton.setOnAction(e -> duplicateSelectedEquipment());
        exportButton.setOnAction(e -> exportEquipmentList());
    }
    
    /**
     * Charge les données d'équipements depuis l'API
     */
    private void loadEquipmentData() {
        Platform.runLater(() -> {
            // Mettre à jour la table en indicateur de chargement
            equipmentTable.setPlaceholder(new Label("Chargement des équipements..."));
        });
        
        CompletableFuture<List<Object>> future = apiService.getEquipments();
        future.thenAccept(equipmentData -> {
            Platform.runLater(() -> {
                equipmentList.clear();
                for (Object item : equipmentData) {
                    if (item instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) item;
                        equipmentList.add(new EquipmentItem(data));
                    }
                }
                
                updateFilterOptions();
                updateStatusLabel();
                equipmentTable.setPlaceholder(new Label("Aucun équipement trouvé"));
            });
        }).exceptionally(throwable -> {
            Platform.runLater(() -> {
                equipmentTable.setPlaceholder(new Label("Erreur de chargement des équipements"));
                showErrorAlert("Erreur", "Impossible de charger les équipements: " + throwable.getMessage());
            });
            return null;
        });
    }
    
    /**
     * Met à jour les options des filtres selon les données chargées
     */
    private void updateFilterOptions() {
        // Mise à jour des catégories
        categoryFilter.getItems().clear();
        categoryFilter.getItems().add("Toutes catégories");
        equipmentList.stream()
            .map(EquipmentItem::getCategory)
            .filter(cat -> cat != null && !cat.isEmpty())
            .distinct()
            .sorted()
            .forEach(categoryFilter.getItems()::add);
        
        // Mise à jour des statuts
        statusFilter.getItems().clear();
        statusFilter.getItems().add("Tous statuts");
        equipmentList.stream()
            .map(EquipmentItem::getStatus)
            .filter(status -> status != null && !status.isEmpty())
            .distinct()
            .sorted()
            .forEach(statusFilter.getItems()::add);
        
        // Mise à jour des marques
        brandFilter.getItems().clear();
        brandFilter.getItems().add("Toutes marques");
        equipmentList.stream()
            .map(EquipmentItem::getBrand)
            .filter(brand -> brand != null && !brand.isEmpty())
            .distinct()
            .sorted()
            .forEach(brandFilter.getItems()::add);
    }
    
    /**
     * Met à jour les filtres appliqués à la liste
     */
    private void updateFilters() {
        filteredList.setPredicate(equipment -> {
            // Filtre de recherche textuelle
            String searchText = searchField.getText();
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                if (!equipment.getName().toLowerCase().contains(lowerCaseFilter) &&
                    !equipment.getBrand().toLowerCase().contains(lowerCaseFilter) &&
                    !equipment.getModel().toLowerCase().contains(lowerCaseFilter) &&
                    !equipment.getSerialNumber().toLowerCase().contains(lowerCaseFilter)) {
                    return false;
                }
            }
            
            // Filtre par catégorie
            String category = categoryFilter.getValue();
            if (category != null && !category.equals("Toutes catégories")) {
                if (!category.equals(equipment.getCategory())) {
                    return false;
                }
            }
            
            // Filtre par statut
            String status = statusFilter.getValue();
            if (status != null && !status.equals("Tous statuts")) {
                if (!status.equals(equipment.getStatus())) {
                    return false;
                }
            }
            
            // Filtre par marque
            String brand = brandFilter.getValue();
            if (brand != null && !brand.equals("Toutes marques")) {
                if (!brand.equals(equipment.getBrand())) {
                    return false;
                }
            }
            
            return true;
        });
        
        updateStatusLabel();
    }
    
    /**
     * Remet à zéro tous les filtres
     */
    private void resetFilters() {
        searchField.clear();
        categoryFilter.setValue("Toutes catégories");
        statusFilter.setValue("Tous statuts");
        brandFilter.setValue("Toutes marques");
    }
    
    /**
     * Met à jour le label de statut dans la toolbar
     */
    private void updateStatusLabel() {
        Platform.runLater(() -> {
            int totalCount = equipmentList.size();
            int filteredCount = filteredList.size();
            
            // Note: Le statut pourra être affiché dans la barre de statut générale si nécessaire; // Pour l'instant, la toolbar est standardisée sans label de statut interne
            System.out.println(String.format("Équipements affichés: %d / %d", filteredCount, totalCount));
        });
    }
    
    /**
     * Met à jour l'état des boutons selon la sélection
     */
    private void updateButtonStates(boolean hasSelection) {
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        duplicateButton.setDisable(!hasSelection);
    }
    
    // La gestion du volet de détails est maintenant automatique via DetailPanelContainer; // === ACTIONS DES BOUTONS ===
    
    private void addEquipment() {
        // TODO: Ouvrir dialog d'ajout d'équipement
        showInfoAlert("Action", "Fonctionnalité d'ajout en cours de développement");
    }
    
    private void editSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Ouvrir dialog d'édition
            showInfoAlert("Action", "Fonctionnalité d'édition en cours de développement pour: " + selected.getName());
        }
    }
    
    private void deleteSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer la suppression");
            alert.setHeaderText("Supprimer l'équipement");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer: " + selected.getName() + " ?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // TODO: Appeler API de suppression
                    showInfoAlert("Action", "Suppression en cours de développement");
                }
            });
        }
    }
    
    private void duplicateSelectedEquipment() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Dupliquer l'équipement
            showInfoAlert("Action", "Duplication en cours de développement pour: " + selected.getName());
        }
    }
    
    private void exportEquipmentList() {
        // TODO: Export Excel/PDF
        showInfoAlert("Export", "Fonctionnalité d'export en cours de développement");
    }
    
    /**
     * Ouvre le dialogue d'import LOCMAT
     */
    private void openLocmatImportDialog() {
        try {
            LocmatImportDialog importDialog = new LocmatImportDialog();
            importDialog.showAndWait();
            
            // Recharger les données après l'import
            loadEquipmentData();
            
        } catch (Exception e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le dialogue d'import LOCMAT: " + e.getMessage());
        }
    }
    
    /**
     * Méthode publique pour rechercher et sélectionner un équipement par nom
     * Utilisée par la recherche globale
     */
    public void selectAndViewEquipment(String equipmentName) {
        if (equipmentName == null || equipmentName.trim().isEmpty()) {
            return;
        }
        
        // Rechercher dans la liste
        for (EquipmentItem item : equipmentList) {
            if (item.getName().toLowerCase().contains(equipmentName.toLowerCase())) {
                // Sélectionner et faire défiler vers l'élément
                equipmentTable.getSelectionModel().select(item);
                equipmentTable.scrollTo(item);
                
                // Mettre à jour le filtre de recherche pour montrer le contexte
                searchField.setText(equipmentName);
                
                break;
            }
        }
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Ouvre la fiche détaillée d'un équipement en mode lecture seule
     */
    private void openEquipmentDetails(EquipmentItem item) {
        if (item == null) {
            return;
        }
        
        // Convertir l'EquipmentItem en Map pour le EquipmentDialog
        Map<String, Object> equipmentData = new HashMap<>();
        equipmentData.put("id", item.getId());
        equipmentData.put("name", item.getName());
        equipmentData.put("description", item.getDescription());
        equipmentData.put("category", item.getCategory());
        equipmentData.put("status", item.getStatus());
        equipmentData.put("qrCode", item.getQrCode());
        equipmentData.put("brand", item.getBrand());
        equipmentData.put("model", item.getModel());
        equipmentData.put("serialNumber", item.getSerialNumber());
        equipmentData.put("purchasePrice", item.getPurchasePrice());
        equipmentData.put("location", item.getLocation());
        equipmentData.put("notes", item.getNotes());
        
        // Ouvrir le dialogue en mode lecture seule
        EquipmentDialog dialog = new EquipmentDialog(apiService, equipmentData, true); // true = mode lecture seule
        dialog.showAndWait().ifPresent(result -> {
            // Si des modifications ont été apportées, rafraîchir la liste
            if (result != null) {
                loadEquipmentData(); // Recharger pour refléter les changements
            }
        });
    }
}
