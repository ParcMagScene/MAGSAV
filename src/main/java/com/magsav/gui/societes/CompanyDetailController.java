package com.magsav.gui.societes;

import com.magsav.model.Company;
import com.magsav.repo.CompanyRepository;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class CompanyDetailController implements Initializable {
    
    // Références aux composants FXML
    @FXML private ImageView imgCompanyLogo;
    @FXML private Label lblCompanyName;
    @FXML private Label lblCompanyType;
    @FXML private Label lblCompanyId;
    
    // Champs d'informations (si présents dans le FXML existant)
    @FXML private Label lblStatus;
    @FXML private Label lblLastModified;
    
    // Modèle de données
    private Company company;
    
    // Repository
    private CompanyRepository companyRepository;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            companyRepository = new CompanyRepository(com.magsav.db.DB.getConnection());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du CompanyRepository: " + e.getMessage());
        }
        
        System.out.println("CompanyDetailController initialisé");
    }
    
    /**
     * Définit la société à afficher
     */
    public void setCompany(Company company) {
        this.company = company;
        loadCompanyData();
    }
    
    /**
     * Charge les données de la société dans l'interface
     */
    private void loadCompanyData() {
        if (company == null) return;
        
        // Informations de base
        if (lblCompanyName != null) {
            lblCompanyName.setText(company.getName());
        }
        if (lblCompanyType != null) {
            lblCompanyType.setText("Type: " + (company.getType() != null ? company.getType().toString() : "Société"));
        }
        if (lblCompanyId != null) {
            lblCompanyId.setText("ID: #" + company.getId());
        }
        
        // Statut
        if (lblStatus != null) {
            lblStatus.setText("Société chargée: " + company.getName());
        }
        if (lblLastModified != null) {
            lblLastModified.setText("Dernière modification: " + 
                (company.getUpdatedAt() != null ? 
                    company.getUpdatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : 
                    "jamais"));
        }
    }
    
    /**
     * Actualise les données
     */
    @FXML
    private void onRefresh() {
        if (company != null) {
            try {
                java.util.Optional<Company> updatedCompanyOpt = companyRepository.findById(company.getId());
                if (updatedCompanyOpt.isPresent()) {
                    setCompany(updatedCompanyOpt.get());
                    if (lblStatus != null) {
                        lblStatus.setText("Données actualisées");
                    }
                } else {
                    if (lblStatus != null) {
                        lblStatus.setText("Société non trouvée lors de l'actualisation");
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'actualisation: " + e.getMessage());
                if (lblStatus != null) {
                    lblStatus.setText("Erreur lors de l'actualisation");
                }
            }
        }
    }
    
    @FXML
    private void saveCompany() {
        // TODO: Implémenter la sauvegarde de la société
        System.out.println("Sauvegarde société");
    }
    
    @FXML
    private void cancel() {
        // Fermer la fenêtre
        if (lblCompanyName != null && lblCompanyName.getScene() != null) {
            lblCompanyName.getScene().getWindow().hide();
        }
    }

    /**
     * Ferme la fenêtre
     */
    @FXML
    private void onClose() {
        Stage stage = (Stage) lblCompanyName.getScene().getWindow();
        stage.close();
    }
}