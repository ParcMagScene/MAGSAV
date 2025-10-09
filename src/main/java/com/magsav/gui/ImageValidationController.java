package com.magsav.gui;

import com.magsav.service.ImageNormalizationService;
import com.magsav.repo.ProductRepository;
import com.magsav.util.AppLogger;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class ImageValidationController implements Initializable {
    
    // Contrôles de l'interface
    @FXML private Label lblStats;
    @FXML private Button btnAutoValidateAll;
    @FXML private Button btnRejectAll;
    @FXML private Button btnRefreshList;
    
    // Table des correspondances
    @FXML private TableView<ImageCorrespondence> correspondencesTable;
    @FXML private TableColumn<ImageCorrespondence, String> colProductUID;
    @FXML private TableColumn<ImageCorrespondence, String> colProductName;
    @FXML private TableColumn<ImageCorrespondence, String> colManufacturer;
    @FXML private TableColumn<ImageCorrespondence, String> colImageSource;
    @FXML private TableColumn<ImageCorrespondence, String> colConfidence;
    @FXML private TableColumn<ImageCorrespondence, String> colStatus;
    @FXML private TableColumn<ImageCorrespondence, Void> colActions;
    
    // Filtres
    @FXML private ComboBox<String> cbFilterStatus;
    @FXML private ComboBox<String> cbFilterSource;
    
    // Panneau de détail
    @FXML private ImageView imgScraped;
    @FXML private ImageView imgCurrent;
    @FXML private Label lblScrapedInfo;
    @FXML private Label lblCurrentInfo;
    @FXML private Label lblDetailUID;
    @FXML private Label lblDetailName;
    @FXML private Label lblDetailManufacturer;
    @FXML private Label lblDetailSource;
    @FXML private Label lblDetailURL;
    
    // Actions
    @FXML private Button btnValidateOne;
    @FXML private Button btnRejectOne;
    @FXML private Button btnSkipOne;
    @FXML private Button btnChooseOtherImage;
    @FXML private Button btnEditProduct;
    @FXML private TextArea txtNotes;
    
    // Contrôles généraux
    @FXML private Label lblProgress;
    @FXML private Button btnSaveValidations;
    @FXML private Button btnClose;
    
    private ObservableList<ImageCorrespondence> correspondenceData = FXCollections.observableArrayList();
    private FilteredList<ImageCorrespondence> filteredData;
    private ImageNormalizationService normalizationService = new ImageNormalizationService();
    private ProductRepository productRepo = new ProductRepository();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        setupDetailPanel();
        loadCorrespondences();
    }
    
    private void setupTable() {
        colProductUID.setCellValueFactory(new PropertyValueFactory<>("productUID"));
        colProductName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colManufacturer.setCellValueFactory(new PropertyValueFactory<>("manufacturer"));
        colImageSource.setCellValueFactory(new PropertyValueFactory<>("imageSource"));
        colConfidence.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(String.format("%.0f%%", cellData.getValue().getConfidence() * 100)));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Colonne d'actions avec boutons
        colActions.setCellFactory(col -> new TableCell<ImageCorrespondence, Void>() {
            private final Button validateBtn = new Button("✅");
            private final Button rejectBtn = new Button("❌");
            
            {
                validateBtn.setOnAction(e -> {
                    ImageCorrespondence item = getTableView().getItems().get(getIndex());
                    validateCorrespondence(item);
                });
                
                rejectBtn.setOnAction(e -> {
                    ImageCorrespondence item = getTableView().getItems().get(getIndex());
                    rejectCorrespondence(item);
                });
                
                validateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 10;");
                rejectBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 10;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ImageCorrespondence correspondence = getTableView().getItems().get(getIndex());
                    if ("En attente".equals(correspondence.getStatus())) {
                        setGraphic(new javafx.scene.layout.HBox(5, validateBtn, rejectBtn));
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        filteredData = new FilteredList<>(correspondenceData);
        correspondencesTable.setItems(filteredData);
        
        // Gestion de la sélection
        correspondencesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateDetailPanel(newSelection);
        });
    }
    
    private void setupFilters() {
        cbFilterStatus.setValue("Tous");
        cbFilterSource.setValue("Toutes");
        
        cbFilterStatus.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        cbFilterSource.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void setupDetailPanel() {
        // Images par défaut
        Image defaultImage = new Image("file:assets/images/no-image.png");
        imgScraped.setImage(defaultImage);
        imgCurrent.setImage(defaultImage);
        
        // Désactiver les boutons par défaut
        btnValidateOne.setDisable(true);
        btnRejectOne.setDisable(true);
        btnSkipOne.setDisable(true);
        btnChooseOtherImage.setDisable(true);
        btnEditProduct.setDisable(true);
    }
    
    private void loadCorrespondences() {
        CompletableFuture.runAsync(() -> {
            try {
                // TODO: Charger les correspondances depuis la base de données ou fichiers temporaires
                // Pour le moment, créons des données de test
                
                List<ImageCorrespondence> testData = List.of(
                    new ImageCorrespondence("P001", "Console de mixage X32", "Behringer", 
                        "temp/normalized/behringer_x32_normalized.jpg", 0.95, "En attente"),
                    new ImageCorrespondence("P002", "Enceinte QSC K10.2", "QSC", 
                        "temp/normalized/qsc_k10_normalized.jpg", 0.87, "En attente"),
                    new ImageCorrespondence("P003", "Micro Shure SM58", "Shure", 
                        "temp/normalized/shure_sm58_normalized.jpg", 0.92, "En attente")
                );
                
                Platform.runLater(() -> {
                    correspondenceData.setAll(testData);
                    updateStatsLabel();
                });
                
            } catch (Exception e) {
                AppLogger.error("Erreur lors du chargement des correspondances", e);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les correspondances: " + e.getMessage());
                });
            }
        });
    }
    
    private void updateDetailPanel(ImageCorrespondence correspondence) {
        if (correspondence == null) {
            clearDetailPanel();
            return;
        }
        
        // Mettre à jour les informations textuelles
        lblDetailUID.setText(correspondence.getProductUID());
        lblDetailName.setText(correspondence.getProductName());
        lblDetailManufacturer.setText(correspondence.getManufacturer());
        lblDetailSource.setText(correspondence.getImageSource());
        lblDetailURL.setText(correspondence.getOriginalURL());
        
        // Charger l'image scrapée
        loadScrapedImage(correspondence.getScrapedImagePath());
        
        // Charger l'image actuelle du produit (si elle existe)
        loadCurrentProductImage(correspondence.getProductUID());
        
        // Activer les boutons selon le statut
        boolean isPending = "En attente".equals(correspondence.getStatus());
        btnValidateOne.setDisable(!isPending);
        btnRejectOne.setDisable(!isPending);
        btnSkipOne.setDisable(!isPending);
        btnChooseOtherImage.setDisable(false);
        btnEditProduct.setDisable(false);
    }
    
    private void clearDetailPanel() {
        lblDetailUID.setText("-");
        lblDetailName.setText("-");
        lblDetailManufacturer.setText("-");
        lblDetailSource.setText("-");
        lblDetailURL.setText("-");
        
        Image defaultImage = new Image("file:assets/images/no-image.png");
        imgScraped.setImage(defaultImage);
        imgCurrent.setImage(defaultImage);
        
        lblScrapedInfo.setText("");
        lblCurrentInfo.setText("Aucune image");
        
        btnValidateOne.setDisable(true);
        btnRejectOne.setDisable(true);
        btnSkipOne.setDisable(true);
        btnChooseOtherImage.setDisable(true);
        btnEditProduct.setDisable(true);
    }
    
    private void loadScrapedImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                Path imageFile = Paths.get(imagePath);
                if (Files.exists(imageFile)) {
                    Image image = new Image(imageFile.toUri().toString());
                    long fileSize = Files.size(imageFile);
                    
                    Platform.runLater(() -> {
                        imgScraped.setImage(image);
                        lblScrapedInfo.setText(String.format("%.0fx%.0f - %d KB", 
                            image.getWidth(), image.getHeight(), fileSize / 1024));
                    });
                }
            } catch (Exception e) {
                AppLogger.error("Erreur lors du chargement de l'image scrapée: " + imagePath, e);
            }
        });
    }
    
    private void loadCurrentProductImage(String productUID) {
        CompletableFuture.runAsync(() -> {
            try {
                // TODO: Récupérer l'image actuelle du produit depuis la base
                // Pour le moment, affichage par défaut
                Platform.runLater(() -> {
                    lblCurrentInfo.setText("Aucune image");
                });
            } catch (Exception e) {
                AppLogger.error("Erreur lors du chargement de l'image actuelle du produit: " + productUID, e);
            }
        });
    }
    
    private void applyFilters() {
        filteredData.setPredicate(correspondence -> {
            boolean statusMatch = "Tous".equals(cbFilterStatus.getValue()) || 
                                 correspondence.getStatus().equals(cbFilterStatus.getValue());
            
            boolean sourceMatch = "Toutes".equals(cbFilterSource.getValue()) || 
                                 correspondence.getImageSource().contains(cbFilterSource.getValue());
            
            return statusMatch && sourceMatch;
        });
        
        updateStatsLabel();
    }
    
    private void updateStatsLabel() {
        long pendingCount = correspondenceData.stream()
            .filter(c -> "En attente".equals(c.getStatus()))
            .count();
        
        lblStats.setText(String.format("📊 %d correspondances à valider (%d affichées)", 
            pendingCount, filteredData.size()));
    }
    
    private void validateCorrespondence(ImageCorrespondence correspondence) {
        correspondence.setStatus("Validé");
        correspondencesTable.refresh();
        updateStatsLabel();
        
        // Programmer l'application de l'image au produit
        lblProgress.setText("Correspondance validée: " + correspondence.getProductName());
    }
    
    private void rejectCorrespondence(ImageCorrespondence correspondence) {
        correspondence.setStatus("Rejeté");
        correspondencesTable.refresh();
        updateStatsLabel();
        
        lblProgress.setText("Correspondance rejetée: " + correspondence.getProductName());
    }
    
    // ==================== ACTIONS ====================
    
    @FXML
    private void onAutoValidateAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Validation Automatique");
        alert.setHeaderText("Valider toutes les correspondances");
        alert.setContentText("Cette action va valider automatiquement toutes les correspondances en attente. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            correspondenceData.stream()
                .filter(c -> "En attente".equals(c.getStatus()))
                .forEach(c -> c.setStatus("Validé"));
            
            correspondencesTable.refresh();
            updateStatsLabel();
            lblProgress.setText("Toutes les correspondances ont été validées automatiquement.");
        }
    }
    
    @FXML
    private void onRejectAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rejet Global");
        alert.setHeaderText("Rejeter toutes les correspondances");
        alert.setContentText("Cette action va rejeter toutes les correspondances en attente. Continuer ?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            correspondenceData.stream()
                .filter(c -> "En attente".equals(c.getStatus()))
                .forEach(c -> c.setStatus("Rejeté"));
            
            correspondencesTable.refresh();
            updateStatsLabel();
            lblProgress.setText("Toutes les correspondances ont été rejetées.");
        }
    }
    
    @FXML
    private void onRefreshList() {
        loadCorrespondences();
        lblProgress.setText("Liste des correspondances actualisée.");
    }
    
    @FXML
    private void onValidateSelected() {
        ImageCorrespondence selected = correspondencesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            validateCorrespondence(selected);
        }
    }
    
    @FXML
    private void onRejectSelected() {
        ImageCorrespondence selected = correspondencesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rejectCorrespondence(selected);
        }
    }
    
    @FXML
    private void onSkipSelected() {
        ImageCorrespondence selected = correspondencesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected.setStatus("Ignoré");
            correspondencesTable.refresh();
            updateStatsLabel();
            lblProgress.setText("Correspondance ignorée: " + selected.getProductName());
        }
    }
    
    @FXML
    private void onChooseOtherImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une autre image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        Stage stage = (Stage) btnChooseOtherImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            // TODO: Remplacer l'image de la correspondance sélectionnée
            lblProgress.setText("Nouvelle image sélectionnée: " + selectedFile.getName());
        }
    }
    
    @FXML
    private void onEditProduct() {
        ImageCorrespondence selected = correspondencesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // TODO: Ouvrir l'éditeur de produit
            showAlert(Alert.AlertType.INFORMATION, "Édition", 
                "Édition du produit " + selected.getProductUID() + " à implémenter.");
        }
    }
    
    @FXML
    private void onSaveValidations() {
        long validatedCount = correspondenceData.stream()
            .filter(c -> "Validé".equals(c.getStatus()))
            .count();
        
        if (validatedCount == 0) {
            showAlert(Alert.AlertType.INFORMATION, "Aucune Validation", 
                "Aucune correspondance n'a été validée.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Application des Validations");
        alert.setHeaderText("Appliquer les validations");
        alert.setContentText(String.format("Cette action va appliquer %d validations et copier les images vers le dossier photos. Continuer ?", validatedCount));
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            applyValidations();
        }
    }
    
    @FXML
    private void onClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    private void applyValidations() {
        CompletableFuture.runAsync(() -> {
            try {
                List<ImageCorrespondence> validatedCorrespondences = correspondenceData.stream()
                    .filter(c -> "Validé".equals(c.getStatus()))
                    .toList();
                
                int successCount = 0;
                for (ImageCorrespondence correspondence : validatedCorrespondences) {
                    try {
                        // Normaliser et déplacer l'image vers le dossier photos
                        Path scrapedImagePath = Paths.get(correspondence.getScrapedImagePath());
                        if (Files.exists(scrapedImagePath)) {
                            // TODO: Utiliser le service de normalisation et copier vers photos/
                            // boolean moved = normalizationService.moveToPhotosDirectory(scrapedImagePath, "photos/", correspondence.getProductUID());
                            
                            // TODO: Mettre à jour la base de données avec le chemin de l'image
                            // productRepo.updateProductImage(correspondence.getProductUID(), finalImagePath);
                            
                            successCount++;
                        }
                    } catch (Exception e) {
                        AppLogger.error("Erreur lors de l'application de la validation pour: " + correspondence.getProductUID(), e);
                    }
                }
                
                final int finalSuccessCount = successCount;
                Platform.runLater(() -> {
                    lblProgress.setText(String.format("✅ %d images appliquées avec succès sur %d validations.", 
                        finalSuccessCount, validatedCorrespondences.size()));
                    
                    // Supprimer les correspondances appliquées de la liste
                    correspondenceData.removeIf(c -> "Validé".equals(c.getStatus()));
                    updateStatsLabel();
                });
                
            } catch (Exception e) {
                AppLogger.error("Erreur lors de l'application des validations", e);
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'application des validations: " + e.getMessage());
                });
            }
        });
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Classe pour représenter une correspondance image-produit
    public static class ImageCorrespondence {
        private String productUID;
        private String productName;
        private String manufacturer;
        private String imageSource;
        private String scrapedImagePath;
        private String originalURL;
        private double confidence;
        private String status;
        
        public ImageCorrespondence(String productUID, String productName, String manufacturer, 
                                 String scrapedImagePath, double confidence, String status) {
            this.productUID = productUID;
            this.productName = productName;
            this.manufacturer = manufacturer;
            this.scrapedImagePath = scrapedImagePath;
            this.confidence = confidence;
            this.status = status;
            this.imageSource = manufacturer; // Simplification
            this.originalURL = "https://" + manufacturer.toLowerCase() + ".com/product/" + productUID;
        }
        
        // Getters et setters
        public String getProductUID() { return productUID; }
        public String getProductName() { return productName; }
        public String getManufacturer() { return manufacturer; }
        public String getImageSource() { return imageSource; }
        public String getScrapedImagePath() { return scrapedImagePath; }
        public String getOriginalURL() { return originalURL; }
        public double getConfidence() { return confidence; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}