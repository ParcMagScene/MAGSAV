package com.magsav.gui.societes;

import com.magsav.model.Societe;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.SavHistoryRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SavDetailController implements Initializable {

    @FXML private ImageView logoImageView;
    @FXML private Label nomLabel;
    @FXML private Label typeLabel;
    @FXML private Label emailLabel;
    @FXML private Label phoneLabel;
    @FXML private Label adresseLabel;
    @FXML private TextArea notesTextArea;
    @FXML private Label productsCountLabel;
    @FXML private Label historyCountLabel;
    
    // Table des produits actuels
    @FXML private TableView<ProductRepository.ProductRow> currentProductsTable;
    @FXML private TableColumn<ProductRepository.ProductRow, String> colCurrentProdNom;
    @FXML private TableColumn<ProductRepository.ProductRow, String> colCurrentProdSN;
    @FXML private TableColumn<ProductRepository.ProductRow, String> colCurrentProdUID;
    @FXML private TableColumn<ProductRepository.ProductRow, String> colCurrentProdFabricant;
    @FXML private TableColumn<ProductRepository.ProductRow, String> colCurrentProdDateSav;
    
    // Table de l'historique
    @FXML private TableView<SavHistoryRepository.SavHistoryEntry> historyTable;
    @FXML private TableColumn<SavHistoryRepository.SavHistoryEntry, String> colHistoryProdNom;
    @FXML private TableColumn<SavHistoryRepository.SavHistoryEntry, String> colHistoryDateDebut;
    @FXML private TableColumn<SavHistoryRepository.SavHistoryEntry, String> colHistoryDateFin;
    @FXML private TableColumn<SavHistoryRepository.SavHistoryEntry, String> colHistoryDuree;
    @FXML private TableColumn<SavHistoryRepository.SavHistoryEntry, String> colHistoryStatut;
    @FXML private TableColumn<SavHistoryRepository.SavHistoryEntry, String> colHistoryNotes;
    
    private Societe sav;
    private final ProductRepository productRepo = new ProductRepository();
    private final SavHistoryRepository savHistoryRepo = new SavHistoryRepository();
    private final ObservableList<ProductRepository.ProductRow> currentProducts = FXCollections.observableArrayList();
    private final ObservableList<SavHistoryRepository.SavHistoryEntry> historyEntries = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCurrentProductsTable();
        setupHistoryTable();
    }
    
    private void setupCurrentProductsTable() {
        colCurrentProdNom.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().nom()));
        colCurrentProdSN.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().sn()));
        colCurrentProdUID.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().uid()));
        colCurrentProdFabricant.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().fabricant()));
        colCurrentProdDateSav.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("TODO")); // À implémenter avec date réelle
            
        currentProductsTable.setItems(currentProducts);
    }
    
    private void setupHistoryTable() {
        colHistoryProdNom.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().productName()));
        colHistoryDateDebut.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().dateDebut()));
        colHistoryDateFin.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().dateFin() != null ? cellData.getValue().dateFin() : "En cours"));
        colHistoryDuree.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(calculateDuration(cellData.getValue())));
        colHistoryStatut.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().statut()));
        colHistoryNotes.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().notes()));
            
        historyTable.setItems(historyEntries);
    }
    
    private String calculateDuration(SavHistoryRepository.SavHistoryEntry entry) {
        try {
            java.time.LocalDate debut = java.time.LocalDate.parse(entry.dateDebut());
            java.time.LocalDate fin = entry.dateFin() != null ? 
                java.time.LocalDate.parse(entry.dateFin()) : java.time.LocalDate.now();
            
            long days = java.time.temporal.ChronoUnit.DAYS.between(debut, fin);
            return days + " jour" + (days > 1 ? "s" : "");
        } catch (Exception e) {
            return "-";
        }
    }
    
    public void setSav(Societe sav) {
        this.sav = sav;
        updateUI();
        loadData();
    }
    
    private void updateUI() {
        if (sav == null) return;
        
        nomLabel.setText(sav.nom());
        typeLabel.setText("SAV Externe");
        emailLabel.setText(sav.email() != null ? sav.email() : "-");
        phoneLabel.setText(sav.phone() != null ? sav.phone() : "-");
        adresseLabel.setText(sav.adresse() != null ? sav.adresse() : "-");
        notesTextArea.setText(sav.notes() != null ? sav.notes() : "");
        
        // Charger le logo si disponible
        loadLogo();
    }
    
    private void loadLogo() {
        try {
            // Convention de nommage : nom de la société en minuscules avec underscores
            String logoFileName = sav.nom().toLowerCase()
                .replaceAll("[^a-zA-Z0-9]", "_")
                .replaceAll("_{2,}", "_")
                .replaceAll("^_|_$", "") + ".png";
                
            String logoPath = System.getProperty("user.home") + "/MAGSAV/medias/logos/" + logoFileName;
            File logoFile = new File(logoPath);
            
            if (logoFile.exists()) {
                Image logo = new Image(logoFile.toURI().toString());
                logoImageView.setImage(logo);
            } else {
                // Logo par défaut pour SAV externe
                logoImageView.setImage(null);
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement logo: " + e.getMessage());
            logoImageView.setImage(null);
        }
    }
    
    private void loadData() {
        if (sav == null) return;
        
        try {
            // Charger les produits actuels en SAV chez cette société
            currentProducts.clear();
            currentProducts.addAll(productRepo.findBySavExterneCompatible(sav.id()));
            productsCountLabel.setText("(" + currentProducts.size() + ")");
            
            // Charger l'historique (pour l'instant, données d'exemple)
            loadHistoryData();
            
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    private void loadHistoryData() {
        try {
            historyEntries.clear();
            // Charger l'historique réel depuis la base de données
            historyEntries.addAll(savHistoryRepo.findBySavExterne(sav.id()));
            historyCountLabel.setText("(" + historyEntries.size() + ")");
        } catch (Exception e) {
            System.err.println("Erreur chargement historique: " + e.getMessage());
            historyCountLabel.setText("(0)");
        }
    }
    
    @FXML
    private void onEdit() {
        // TODO: Ouvrir le formulaire d'édition du SAV externe
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Modification");
        alert.setHeaderText("Fonction en cours de développement");
        alert.setContentText("La modification des détails du SAV externe sera bientôt disponible.");
        alert.showAndWait();
    }
    
    @FXML
    private void onClose() {
        Stage stage = (Stage) nomLabel.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}