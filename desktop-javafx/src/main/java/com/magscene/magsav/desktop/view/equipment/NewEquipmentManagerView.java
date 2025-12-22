package com.magscene.magsav.desktop.view.equipment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.magscene.magsav.desktop.component.DetailPanelContainer;
import com.magscene.magsav.desktop.config.EquipmentPreferencesManager;
import com.magscene.magsav.desktop.core.di.ApplicationContext;
import com.magscene.magsav.desktop.core.navigation.SelectableView;
import com.magscene.magsav.desktop.core.search.GlobalSearchManager;
import com.magscene.magsav.desktop.core.search.SearchProvider;
import com.magscene.magsav.desktop.dialog.EquipmentDetailDialog;
import com.magscene.magsav.desktop.dialog.QRCodeDialog;
import com.magscene.magsav.desktop.service.business.EquipmentService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.util.ViewUtils;
import com.magscene.magsav.desktop.view.base.BaseManagerView;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
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
 * Implémente SearchProvider pour la recherche globale
 * Implémente SelectableView pour la sélection depuis la recherche globale
 */
public class NewEquipmentManagerView extends BaseManagerView<EquipmentItem> implements SearchProvider, SelectableView {
    private TableView<EquipmentItem> equipmentTable;
    private ObservableList<EquipmentItem> equipmentData; // Déclaration sans initialisation
    private ObservableList<EquipmentItem> allEquipmentData; // Données complètes pour filtrage local
    private EquipmentService equipmentService;
    
    // Résultats de la dernière recherche globale
    private List<SearchResult> lastSearchResults = new ArrayList<>();
    private int lastResultCount = 0;
    
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
            
            // Lier les boutons Edit/Delete à la sélection du tableau
            bindSelectionToButtons(
                javafx.beans.binding.Bindings.createBooleanBinding(
                    () -> equipmentTable.getSelectionModel().getSelectedItem() == null,
                    equipmentTable.getSelectionModel().selectedItemProperty()
                )
            );
        }

        // Enregistrement comme fournisseur de recherche globale
        GlobalSearchManager.getInstance().registerSearchProvider(this);
        System.out.println("🔍 Équipements enregistré comme SearchProvider");

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
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", "Description, marque, QR code...",
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
        @SuppressWarnings("unchecked")
        ComboBox<String> catCombo = (ComboBox<String>) categoryBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);
        categoryCombo = catCombo;

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
        @SuppressWarnings("unchecked")
        ComboBox<String> subCatCombo = (ComboBox<String>) subCategoryBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);
        subCategoryCombo = subCatCombo;

        // 📊 Filtre statut avec ViewUtils
        VBox statusBox = ViewUtils.createFilterBox("📊 Statut",
                new String[] { "Tous statuts", "Disponible", "En location", "Maintenance", "Hors service" },
                "Tous statuts", value -> applyFilters());
        // Récupérer le ComboBox de statut pour le reset
        @SuppressWarnings("unchecked")
        ComboBox<String> statCombo = (ComboBox<String>) statusBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);
        statusCombo = statCombo;

        // 🏢 Filtre propriétaire avec ViewUtils
        // Par défaut MAG SCENE, sauf si préférence "Tous propriétaires" activée
        EquipmentPreferencesManager prefManager = EquipmentPreferencesManager.getInstance();
        String defaultOwner = prefManager.isShowAllOwners() ? "Tous propriétaires" : "MAG SCENE";
        VBox ownerBox = ViewUtils.createFilterBox("🏢 Propriétaire",
                new String[] { "Tous propriétaires", "MAG SCENE", "RENTAL", "NICLEN", "AED RENT" },
                defaultOwner, value -> applyFilters());
        // Récupérer le ComboBox de propriétaire pour le reset
        @SuppressWarnings("unchecked")
        ComboBox<String> ownCombo = (ComboBox<String>) ownerBox.getChildren().stream()
                .filter(n -> n instanceof ComboBox)
                .findFirst().orElse(null);
        ownerCombo = ownCombo;
        
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

        // 📱 Bouton QR codes (uniquement pour équipements MAG SCENE)
        Button qrButton = new Button("📱 QR Codes");
        qrButton.getStyleClass().add("primary-button");
        qrButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        qrButton.setOnAction(e -> handleQRCode());
        VBox qrBox = new VBox(5);
        qrBox.getChildren().addAll(new Label(" "), qrButton);

        toolbar.getChildren().addAll(searchBox, categoryBox, subCategoryBox, statusBox, ownerBox, resetBox, qrBox);
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
                        String locmatCode = item.getLocmatCode() != null ? item.getLocmatCode().toLowerCase() : "";
                        if (!name.contains(searchText) && !brand.contains(searchText) && 
                            !qrCode.contains(searchText) && !supplier.contains(searchText) &&
                            !locmatCode.contains(searchText)) {
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

        // Colonnes pour les équipements (QR Code = UID, donc pas besoin de colonne ID)
        TableColumn<EquipmentItem, String> qrCol = new TableColumn<>("UID");
        TableColumn<EquipmentItem, String> locmatCol = new TableColumn<>("Code LocMat");
        TableColumn<EquipmentItem, String> nameCol = new TableColumn<>("Description");
        TableColumn<EquipmentItem, String> brandCol = new TableColumn<>("Marque");
        TableColumn<EquipmentItem, String> parentCategoryCol = new TableColumn<>("Catégorie");
        TableColumn<EquipmentItem, String> categoryCol = new TableColumn<>("Sous-catégorie");
        TableColumn<EquipmentItem, String> quantityCol = new TableColumn<>("Qté");
        TableColumn<EquipmentItem, String> statusCol = new TableColumn<>("Statut");
        TableColumn<EquipmentItem, String> serialCol = new TableColumn<>("N° Série");
        TableColumn<EquipmentItem, String> locationCol = new TableColumn<>("Emplacement");

        // Configuration des cellValueFactories simplifiées avec les getters du wrapper
        qrCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQrCode()));
        locmatCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocmatCode()));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        brandCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBrand()));
        parentCategoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getParentCategory()));
        categoryCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory()));
        quantityCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuantity()));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        serialCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSerialNumber()));
        locationCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));

        // Configuration des largeurs
        qrCol.setPrefWidth(80);
        locmatCol.setPrefWidth(100);
        nameCol.setPrefWidth(180);
        brandCol.setPrefWidth(90);
        parentCategoryCol.setPrefWidth(100);
        categoryCol.setPrefWidth(120);
        quantityCol.setPrefWidth(40);
        statusCol.setPrefWidth(85);
        serialCol.setPrefWidth(120);
        locationCol.setPrefWidth(90);

        table.getColumns().addAll(qrCol, locmatCol, nameCol, brandCol, parentCategoryCol, categoryCol, quantityCol, statusCol, serialCol, locationCol);

        // Style de sélection uniforme et double-clic pour édition
        table.setRowFactory(tv -> {
            TableRow<EquipmentItem> row = new TableRow<>();

            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    row.setStyle("-fx-background-color: " + ThemeConstants.SELECTION_BACKGROUND + "; " +
                            "-fx-text-fill: " + ThemeConstants.SELECTION_TEXT + "; " +
                            "-fx-border-color: " + ThemeConstants.SELECTION_BORDER + "; " +
                            "-fx-border-width: 1px;");
                } else {
                    row.setStyle("");
                }
            };

            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());

            // Double-clic pour éditer l'équipement
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit();
                }
            });

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
                    }

                    System.out.println("✅ " + allEquipmentData.size() + " équipements chargés depuis le backend");
                    
                    // Appliquer le filtre par défaut (MAG SCENE)
                    applyFilters();

                    // Forcer le rafraîchissement du tableau
                    if (equipmentTable != null) {
                        equipmentTable.refresh();
                    }

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

    @SuppressWarnings("unused") // Conservé pour compatibilité future
    private void performLocalSearch(String query) {
        // Méthode conservée pour compatibilité, mais applyFilters() est utilisé
        applyFilters();
    }

    /**
     * Implémentation de SearchProvider.performSearch
     * Effectue une recherche globale dans les équipements
     */
    @Override
    public void performSearch(String searchTerm) {
        lastSearchResults.clear();
        lastResultCount = 0;
        
        if (searchTerm == null || searchTerm.trim().isEmpty() || allEquipmentData == null) {
            return;
        }
        
        String term = searchTerm.toLowerCase().trim();
        
        // Rechercher dans tous les équipements
        List<EquipmentItem> matchingItems = allEquipmentData.stream()
                .filter(item -> {
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";
                    String brand = item.getBrand() != null ? item.getBrand().toLowerCase() : "";
                    String qrCode = item.getQrCode() != null ? item.getQrCode().toLowerCase() : "";
                    String locmatCode = item.getLocmatCode() != null ? item.getLocmatCode().toLowerCase() : "";
                    String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                    
                    return name.contains(term) || brand.contains(term) || 
                           qrCode.contains(term) || locmatCode.contains(term) ||
                           category.contains(term);
                })
                .limit(10) // Limiter à 10 résultats pour la popup
                .collect(Collectors.toList());
        
        lastResultCount = (int) allEquipmentData.stream()
                .filter(item -> {
                    String name = item.getName() != null ? item.getName().toLowerCase() : "";
                    String brand = item.getBrand() != null ? item.getBrand().toLowerCase() : "";
                    String qrCode = item.getQrCode() != null ? item.getQrCode().toLowerCase() : "";
                    String locmatCode = item.getLocmatCode() != null ? item.getLocmatCode().toLowerCase() : "";
                    String category = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
                    
                    return name.contains(term) || brand.contains(term) || 
                           qrCode.contains(term) || locmatCode.contains(term) ||
                           category.contains(term);
                })
                .count();
        
        // Convertir en SearchResult
        for (EquipmentItem item : matchingItems) {
            String id = item.getQrCode() != null ? item.getQrCode() : String.valueOf(System.identityHashCode(item));
            String title = item.getName() != null ? item.getName() : "Équipement";
            String subtitle = (item.getBrand() != null ? item.getBrand() : "") + 
                            (item.getCategory() != null ? " - " + item.getCategory() : "");
            lastSearchResults.add(new SearchResult(id, title, subtitle, "Équipement"));
        }
    }

    @Override
    protected void handleAdd() {
        updateStatus("Ouverture du dialogue d'ajout d'équipement...");
        
        // Ouvrir le dialog d'ajout
        com.magscene.magsav.desktop.service.ApiService apiService = 
            ApplicationContext.getInstance().getInstance(com.magscene.magsav.desktop.service.ApiService.class);
        
        com.magscene.magsav.desktop.dialog.EquipmentDialog dialog = 
            new com.magscene.magsav.desktop.dialog.EquipmentDialog(apiService, null);
        dialog.initOwner(getScene().getWindow());
        
        java.util.Optional<java.util.Map<String, Object>> result = dialog.showAndWait();
        
        result.ifPresent(equipmentData -> {
            // Appeler l'API pour créer l'équipement
            apiService.createEquipment(equipmentData)
                .thenRun(() -> javafx.application.Platform.runLater(() -> {
                    loadEquipmentData(); // Recharger les données
                    updateStatus("Équipement créé avec succès");
                }))
                .exceptionally(throwable -> {
                    javafx.application.Platform.runLater(() -> {
                        updateStatus("Erreur lors de la création: " + throwable.getMessage());
                    });
                    return null;
                });
        });
    }

    @Override
    protected void handleEdit() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateStatus("Affichage de l'équipement: " + selected.getName());
            
            // Garder l'ID pour resélectionner après rafraîchissement
            Object selectedId = selected.getData().get("id");
            
            // Ouvrir le dialog en mode lecture
            com.magscene.magsav.desktop.service.ApiService apiService = 
                ApplicationContext.getInstance().getInstance(com.magscene.magsav.desktop.service.ApiService.class);
            
            EquipmentDetailDialog detailDialog = new EquipmentDetailDialog(apiService, selected.getData());
            detailDialog.initOwner(getScene().getWindow());
            detailDialog.showAndWait().ifPresent(result -> {
                // Rafraîchir si l'équipement a été modifié
                if (result != null) {
                    loadEquipmentData();
                    
                    // Resélectionner l'équipement modifié pour rafraîchir le volet de détail
                    if (selectedId != null) {
                        javafx.application.Platform.runLater(() -> {
                            for (EquipmentItem item : equipmentTable.getItems()) {
                                Object itemId = item.getData().get("id");
                                if (selectedId.equals(itemId)) {
                                    // Invalider le cache d'image pour forcer le rechargement
                                    item.invalidateImageCache();
                                    equipmentTable.getSelectionModel().select(item);
                                    break;
                                }
                            }
                        });
                    }
                    
                    updateStatus("Équipement modifié avec succès");
                }
            });
        } else {
            updateStatus("Aucun équipement sélectionné");
        }
    }

    @Override
    protected void handleDelete() {
        EquipmentItem selected = equipmentTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            updateStatus("Aucun équipement sélectionné");
            return;
        }
        
        // Demander confirmation
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'équipement");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + selected.getName() + "\" ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                Object idObj = selected.getData().get("id");
                if (idObj != null) {
                    Long id = Long.valueOf(idObj.toString());
                    
                    com.magscene.magsav.desktop.service.ApiService apiService = 
                        ApplicationContext.getInstance().getInstance(com.magscene.magsav.desktop.service.ApiService.class);
                    
                    apiService.deleteEquipment(id)
                        .thenAccept(success -> javafx.application.Platform.runLater(() -> {
                            if (success) {
                                loadEquipmentData();
                                updateStatus("Équipement supprimé avec succès");
                            } else {
                                updateStatus("Erreur lors de la suppression de l'équipement");
                            }
                        }))
                        .exceptionally(throwable -> {
                            javafx.application.Platform.runLater(() -> {
                                updateStatus("Erreur: " + throwable.getMessage());
                            });
                            return null;
                        });
                }
            }
        });
    }

    @Override
    public void refresh() {
        loadEquipmentData();
    }

    /**
     * Implémentation de SearchProvider.getModuleName
     */
    @Override
    public String getModuleName() {
        return "Équipements";
    }
    
    /**
     * Implémentation de SearchProvider.getLastResultCount
     */
    @Override
    public int getLastResultCount() {
        return lastResultCount;
    }
    
    /**
     * Implémentation de SearchProvider.getLastResults
     */
    @Override
    public List<SearchResult> getLastResults() {
        return lastSearchResults;
    }

    @Override
    protected String getViewCssClass() {
        return "equipment-manager-view";
    }

    @SuppressWarnings("unused") // Fonctionnalité planifiée
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

    @SuppressWarnings("unused") // Fonctionnalité planifiée
    private void handleExport() {
        updateStatus("Export des équipements...");
        // TODO: Implémenter l'export
    }

    private void handleQRCode() {
        if (allEquipmentData == null || allEquipmentData.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Attention");
            alert.setHeaderText(null);
            alert.setContentText("Aucun équipement chargé.");
            alert.showAndWait();
            return;
        }
        
        // Ouvrir le dialog QR codes avec les équipements actuellement affichés
        // ou tous les équipements si aucun filtre
        List<EquipmentItem> equipmentsToShow = equipmentData.isEmpty() ? 
                List.copyOf(allEquipmentData) : List.copyOf(equipmentData);
        
        QRCodeDialog dialog = new QRCodeDialog(equipmentsToShow);
        dialog.showAndWait();
        
        updateStatus("Génération des QR Codes terminée");
    }
    
    // ===== Implémentation SelectableView =====
    
    /**
     * Sélectionne un équipement par son ID
     * Utilisé par la recherche globale pour naviguer vers un résultat
     */
    @Override
    public boolean selectById(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        
        // Réinitialiser les filtres pour afficher tous les équipements
        resetFilters();
        
        // Chercher l'équipement dans les données
        EquipmentItem itemToSelect = null;
        
        // D'abord chercher dans allEquipmentData (données complètes)
        if (allEquipmentData != null) {
            for (EquipmentItem item : allEquipmentData) {
                if (id.equals(String.valueOf(item.getId()))) {
                    itemToSelect = item;
                    break;
                }
            }
        }
        
        // Si non trouvé, chercher dans equipmentData (données filtrées)
        if (itemToSelect == null && equipmentData != null) {
            for (EquipmentItem item : equipmentData) {
                if (id.equals(String.valueOf(item.getId()))) {
                    itemToSelect = item;
                    break;
                }
            }
        }
        
        if (itemToSelect != null) {
            final EquipmentItem finalItem = itemToSelect;
            
            // Sélectionner l'item dans le tableau
            Platform.runLater(() -> {
                equipmentTable.getSelectionModel().select(finalItem);
                equipmentTable.scrollTo(finalItem);
                System.out.println("✅ Équipement sélectionné: " + finalItem.getName() + " (ID: " + id + ")");
            });
            
            return true;
        }
        
        System.out.println("⚠️ Équipement non trouvé avec ID: " + id);
        return false;
    }
    
    @Override
    public String getViewName() {
        return "Parc Matériel";
    }
}