package com.magsav.gui.management;

import com.magsav.repo.ProductRepository;
import com.magsav.repo.ProductRepository.ProductRowDetailed;
import com.magsav.service.DataChangeEvent;
import com.magsav.service.DataChangeNotificationService;
import com.magsav.service.NavigationService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Predicate;

/**
 * Contrôleur pour la gestion avancée des produits avec filtrage complet
 */
public class ProductManagementController implements Initializable {

    // Composants de filtrage
    @FXML private TextField tfFilterNom;

    @FXML private TextField tfFilterSN;
    @FXML private TextField tfFilterUID;
    @FXML private ComboBox<String> cbFilterFabricant;
    @FXML private ComboBox<String> cbFilterSituation;
    @FXML private ComboBox<String> cbFilterCategory;
    @FXML private ComboBox<String> cbFilterSubcategory;
    @FXML private TextField tfFilterClient;
    @FXML private TextField tfFilterDescription;
    @FXML private DatePicker dpFilterDateAchatFrom;
    @FXML private DatePicker dpFilterDateAchatTo;
    @FXML private TextField tfFilterPrixMin;
    @FXML private TextField tfFilterPrixMax;
    
    // Table principale
    @FXML private TableView<ProductRowDetailed> table;
    @FXML private TableColumn<ProductRowDetailed, Long> colId;

    @FXML private TableColumn<ProductRowDetailed, String> colNom;
    @FXML private TableColumn<ProductRowDetailed, String> colSN;
    @FXML private TableColumn<ProductRowDetailed, String> colFabricant;
    @FXML private TableColumn<ProductRowDetailed, String> colUID;
    @FXML private TableColumn<ProductRowDetailed, String> colSituation;
    @FXML private TableColumn<ProductRowDetailed, String> colPhoto;
    @FXML private TableColumn<ProductRowDetailed, String> colCategory;
    @FXML private TableColumn<ProductRowDetailed, String> colSubcategory;
    @FXML private TableColumn<ProductRowDetailed, String> colDescription;
    @FXML private TableColumn<ProductRowDetailed, String> colDateAchat;
    @FXML private TableColumn<ProductRowDetailed, String> colClient;
    @FXML private TableColumn<ProductRowDetailed, String> colPrix;
    @FXML private TableColumn<ProductRowDetailed, String> colGarantie;
    
    // Informations et statistiques
    @FXML private Label lblTotalProduits;
    @FXML private Label lblProduitsFiltrés;
    @FXML private Label lblStatsSituations;
    @FXML private VBox vboxFilters;
    
    private final ProductRepository productRepo = new ProductRepository();
    private final ObservableList<ProductRowDetailed> masterData = FXCollections.observableArrayList();
    private FilteredList<ProductRowDetailed> filteredData;
    private SortedList<ProductRowDetailed> sortedData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTableColumns();
        initFilterComponents();
        initDataBinding();
        loadData();
        setupFiltering();
        updateStatistics();
        
        // S'abonner aux notifications de changement de données pour rafraîchissement automatique
        DataChangeNotificationService.getInstance().subscribe(this::onDataChanged);
    }
    
    /**
     * Gère les événements de changement de données pour rafraîchissement automatique
     */
    private void onDataChanged(DataChangeEvent event) {
        switch (event.getType()) {
            case PRODUCTS_IMPORTED:
            case PRODUCT_CREATED:
            case PRODUCT_UPDATED:
            case PRODUCT_DELETED:
                // Recharger les données automatiquement et de manière transparente
                loadData();
                loadDistinctValues(); // Recharger aussi les valeurs de filtres
                updateStatistics();
                break;
            default:
                // Ignorer les autres types d'événements
                break;
        }
    }

    private void initTableColumns() {
        // Configuration des colonnes avec CellValueFactory pour records Java
        colId.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().id()));
        colId.setPrefWidth(60);
        

        
        colNom.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().nom()));
        colNom.setPrefWidth(200);
        
        colSN.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().sn()));
        colSN.setPrefWidth(120);
        
        colFabricant.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().fabricant()));
        colFabricant.setPrefWidth(150);
        
        colUID.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().uid()));
        colUID.setPrefWidth(120);
        
        colSituation.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().situation()));
        colSituation.setPrefWidth(120);
        
        colPhoto.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().photo()));
        colPhoto.setPrefWidth(100);
        // Affichage simplifié pour les photos
        colPhoto.setCellFactory(new Callback<TableColumn<ProductRowDetailed, String>, TableCell<ProductRowDetailed, String>>() {
            @Override
            public TableCell<ProductRowDetailed, String> call(TableColumn<ProductRowDetailed, String> param) {
                return new TableCell<ProductRowDetailed, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null || item.trim().isEmpty()) {
                            setText("Aucune");
                        } else {
                            setText("📷 Photo");
                        }
                    }
                };
            }
        });
        
        colCategory.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().category()));
        colCategory.setPrefWidth(120);
        
        colSubcategory.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().subcategory()));
        colSubcategory.setPrefWidth(120);
        
        colDescription.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().description()));
        colDescription.setPrefWidth(200);
        
        colDateAchat.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().dateAchat()));
        colDateAchat.setPrefWidth(100);
        
        colClient.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().client()));
        colClient.setPrefWidth(150);
        
        colPrix.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().prix()));
        colPrix.setPrefWidth(80);
        
        colGarantie.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().garantie()));
        colGarantie.setPrefWidth(100);
        
        // Permettre le redimensionnement des colonnes
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Configurer le double-clic pour ouvrir les détails du produit
        table.setRowFactory(tv -> {
            TableRow<ProductRowDetailed> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    ProductRowDetailed product = row.getItem();
                    NavigationService.openProductDetail(product.id());
                }
            });
            return row;
        });
    }

    private void initFilterComponents() {
        // Initialiser les ComboBox avec les valeurs distinctes de la base
        loadDistinctValues();
        
        // Placeholder text pour les champs de filtrage
        tfFilterNom.setPromptText("Filtrer par nom...");

        tfFilterSN.setPromptText("Filtrer par N° série...");
        tfFilterUID.setPromptText("Filtrer par UID...");
        tfFilterClient.setPromptText("Filtrer par client...");
        tfFilterDescription.setPromptText("Filtrer par description...");
        tfFilterPrixMin.setPromptText("Prix min");
        tfFilterPrixMax.setPromptText("Prix max");
    }

    private void loadDistinctValues() {
        try {
            // Charger les fabricants distincts
            cbFilterFabricant.getItems().clear();
            cbFilterFabricant.getItems().add("Tous les fabricants");
            cbFilterFabricant.getItems().addAll(productRepo.listDistinctFabricants());
            cbFilterFabricant.setValue("Tous les fabricants");

            // Charger les situations distinctes
            cbFilterSituation.getItems().clear();
            cbFilterSituation.getItems().add("Toutes les situations");
            cbFilterSituation.getItems().addAll(productRepo.listDistinctSituations());
            cbFilterSituation.setValue("Toutes les situations");

            // Charger les catégories distinctes
            cbFilterCategory.getItems().clear();
            cbFilterCategory.getItems().add("Toutes les catégories");
            cbFilterCategory.getItems().addAll(productRepo.listDistinctCategories());
            cbFilterCategory.setValue("Toutes les catégories");

            // Charger les sous-catégories distinctes
            cbFilterSubcategory.getItems().clear();
            cbFilterSubcategory.getItems().add("Toutes les sous-catégories");
            cbFilterSubcategory.getItems().addAll(productRepo.listDistinctSubcategories(null));
            cbFilterSubcategory.setValue("Toutes les sous-catégories");

        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les valeurs de filtre: " + e.getMessage());
        }
    }

    private void initDataBinding() {
        // Créer les listes filtrées et triées
        filteredData = new FilteredList<>(masterData, p -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);
    }

    private void loadData() {
        try {
            masterData.clear();
            masterData.addAll(productRepo.findAllDetailed());
            updateStatistics();
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les produits: " + e.getMessage());
        }
    }

    private void setupFiltering() {
        // Configurer les listeners pour le filtrage automatique
        tfFilterNom.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        tfFilterSN.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tfFilterUID.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tfFilterClient.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tfFilterDescription.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tfFilterPrixMin.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        tfFilterPrixMax.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        cbFilterFabricant.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbFilterSituation.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbFilterCategory.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbFilterSubcategory.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        
        if (dpFilterDateAchatFrom != null) {
            dpFilterDateAchatFrom.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
        if (dpFilterDateAchatTo != null) {
            dpFilterDateAchatTo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }

    private void applyFilters() {
        Predicate<ProductRowDetailed> combinedFilter = product -> {
            // Filtre par nom
            if (!matchesFilter(tfFilterNom.getText(), product.nom())) return false;
            

            
            // Filtre par numéro de série
            if (!matchesFilter(tfFilterSN.getText(), product.sn())) return false;
            
            // Filtre par UID
            if (!matchesFilter(tfFilterUID.getText(), product.uid())) return false;
            
            // Filtre par client
            if (!matchesFilter(tfFilterClient.getText(), product.client())) return false;
            
            // Filtre par description
            if (!matchesFilter(tfFilterDescription.getText(), product.description())) return false;
            
            // Filtre par fabricant
            String selectedFabricant = cbFilterFabricant.getValue();
            if (selectedFabricant != null && !"Tous les fabricants".equals(selectedFabricant)) {
                if (!selectedFabricant.equals(product.fabricant())) return false;
            }
            
            // Filtre par situation
            String selectedSituation = cbFilterSituation.getValue();
            if (selectedSituation != null && !"Toutes les situations".equals(selectedSituation)) {
                if (!selectedSituation.equals(product.situation())) return false;
            }
            
            // Filtre par catégorie
            String selectedCategory = cbFilterCategory.getValue();
            if (selectedCategory != null && !"Toutes les catégories".equals(selectedCategory)) {
                if (!selectedCategory.equals(product.category())) return false;
            }
            
            // Filtre par sous-catégorie
            String selectedSubcategory = cbFilterSubcategory.getValue();
            if (selectedSubcategory != null && !"Toutes les sous-catégories".equals(selectedSubcategory)) {
                if (!selectedSubcategory.equals(product.subcategory())) return false;
            }
            
            // Filtre par prix
            if (!matchesPriceFilter(product.prix())) return false;
            
            return true;
        };
        
        filteredData.setPredicate(combinedFilter);
        updateStatistics();
    }

    private boolean matchesFilter(String filterText, String fieldValue) {
        if (filterText == null || filterText.trim().isEmpty()) return true;
        if (fieldValue == null) return false;
        return fieldValue.toLowerCase().contains(filterText.toLowerCase().trim());
    }

    private boolean matchesPriceFilter(String prix) {
        String minText = tfFilterPrixMin.getText();
        String maxText = tfFilterPrixMax.getText();
        
        if ((minText == null || minText.trim().isEmpty()) && 
            (maxText == null || maxText.trim().isEmpty())) {
            return true;
        }
        
        if (prix == null || prix.trim().isEmpty()) return false;
        
        try {
            double productPrice = Double.parseDouble(prix.replaceAll("[^0-9.,]", "").replace(",", "."));
            
            if (minText != null && !minText.trim().isEmpty()) {
                double minPrice = Double.parseDouble(minText.replace(",", "."));
                if (productPrice < minPrice) return false;
            }
            
            if (maxText != null && !maxText.trim().isEmpty()) {
                double maxPrice = Double.parseDouble(maxText.replace(",", "."));
                if (productPrice > maxPrice) return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            return true; // En cas d'erreur de parsing, ne pas filtrer
        }
    }

    private void updateStatistics() {
        int total = masterData.size();
        int filtered = filteredData != null ? filteredData.size() : total;
        
        lblTotalProduits.setText(String.valueOf(total));
        lblProduitsFiltrés.setText(String.valueOf(filtered));
        
        // Statistiques par situation
        if (filteredData != null) {
            var stats = filteredData.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    p -> p.situation() != null ? p.situation() : "Non définie",
                    java.util.stream.Collectors.counting()
                ));
            
            StringBuilder statsText = new StringBuilder();
            stats.forEach((situation, count) -> {
                if (statsText.length() > 0) statsText.append(" | ");
                statsText.append(situation).append(": ").append(count);
            });
            
            lblStatsSituations.setText(statsText.toString());
        }
    }

    @FXML
    private void onClearAllFilters() {
        tfFilterNom.clear();

        tfFilterSN.clear();
        tfFilterUID.clear();
        tfFilterClient.clear();
        tfFilterDescription.clear();
        tfFilterPrixMin.clear();
        tfFilterPrixMax.clear();
        
        cbFilterFabricant.setValue("Tous les fabricants");
        cbFilterSituation.setValue("Toutes les situations");
        cbFilterCategory.setValue("Toutes les catégories");
        cbFilterSubcategory.setValue("Toutes les sous-catégories");
        
        if (dpFilterDateAchatFrom != null) dpFilterDateAchatFrom.setValue(null);
        if (dpFilterDateAchatTo != null) dpFilterDateAchatTo.setValue(null);
    }

    // Méthode onRefresh supprimée - rafraîchissement automatique via système d'événements

    @FXML
    private void onImportCsv() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/csv_import.fxml"));
            DialogPane dialogPane = loader.load();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Import CSV avec Logs Automatiques");
            
            // Configurer le dialogue après l'affichage pour éviter les erreurs de null stage
            dialog.setOnShown(event -> {
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                if (stage != null) {
                    stage.setResizable(true);
                    stage.setMinWidth(750);
                    stage.setMinHeight(650);
                }
            });
            
            // Afficher le dialogue et attendre le résultat
            var result = dialog.showAndWait();
            
            // Si l'import a été effectué, recharger les données
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                showInfo("Import terminé", "L'import CSV a été effectué. Actualisation des données...");
                loadData(); // Recharger la liste des produits
            }
            
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'interface d'import CSV: " + e.getMessage());
            e.printStackTrace(); // Pour déboguer
        } catch (Exception e) {
            showError("Erreur inattendue", "Une erreur inattendue s'est produite: " + e.getMessage());
            e.printStackTrace(); // Pour déboguer
        }
    }

    @FXML
    private void onExportFiltered() {
        // TODO: Implémenter l'export des données filtrées
        showInfo("Export", "Fonctionnalité d'export à implémenter");
    }

    @FXML
    private void onToggleFilters() {
        boolean isVisible = vboxFilters.isVisible();
        vboxFilters.setVisible(!isVisible);
        vboxFilters.setManaged(!isVisible);
    }

    @FXML
    private void onCleanup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/product_cleanup.fxml"));
            DialogPane dialogPane = loader.load();
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Nettoyage des Produits");
            
            // Rendre le dialogue modal et redimensionnable
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.setResizable(true);
            
            dialog.showAndWait();
            
            // Recharger les données après le nettoyage
            loadData();
            
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir l'utilitaire de nettoyage: " + e.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}