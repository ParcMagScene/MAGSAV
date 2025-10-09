package com.magsav.gui;

import com.magsav.model.DemandeIntervention;
import com.magsav.model.DemandeIntervention.TypeDemande;
import com.magsav.repo.DemandeInterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.InterventionRepository;
import com.magsav.service.UidGenerationService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Optional;

public class ValidationDemandesController {
    
    // Services et repositories
    private final DemandeInterventionRepository demandeRepo = new DemandeInterventionRepository();
    private final ProductRepository productRepo = new ProductRepository();
    private final InterventionRepository interventionRepo = new InterventionRepository();
    private final UidGenerationService uidService = new UidGenerationService();
    
    // État
    private DemandeIntervention demandeSelectionnee = null;
    private ObservableList<DemandeIntervention> demandesEnAttente = FXCollections.observableArrayList();
    
    // Contrôles généraux
    @FXML private Label lblStats;
    @FXML private Button btnActualiser;
    
    // Tableau des demandes
    @FXML private TableView<DemandeIntervention> tableDemandesEnAttente;
    @FXML private TableColumn<DemandeIntervention, String> colDate;
    @FXML private TableColumn<DemandeIntervention, String> colDemandeur;
    @FXML private TableColumn<DemandeIntervention, String> colType;
    @FXML private TableColumn<DemandeIntervention, String> colProduit;
    @FXML private TableColumn<DemandeIntervention, String> colUID;
    @FXML private TableColumn<DemandeIntervention, String> colSN;
    @FXML private TableColumn<DemandeIntervention, String> colPanne;
    
    // Détails de la demande
    @FXML private VBox vboxDetails;
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailType;
    @FXML private Label lblDetailDemandeur;
    @FXML private Label lblDetailDate;
    @FXML private Label lblDetailProduit;
    @FXML private Label lblDetailUID;
    @FXML private Label lblDetailSN;
    @FXML private Label lblDetailDetecteur;
    @FXML private TextArea taDetailPanne;
    @FXML private TextArea taDetailNotes;
    
    // Section nouveau produit
    @FXML private VBox vboxNouveauProduit;
    @FXML private TextField tfNouveauFabricant;
    @FXML private TextField tfNouvelleCategorie;
    @FXML private TextArea taNouvelleDescription;
    @FXML private CheckBox cbCreerProduit;
    
    // Actions de validation
    @FXML private TextField tfNotesValidation;
    @FXML private Button btnValider;
    @FXML private Button btnRejeter;
    
    @FXML
    private void initialize() {
        configureTableau();
        configurerDetailsSelection();
        chargerDemandesEnAttente();
    }
    
    private void configureTableau() {
        // Configuration des colonnes
        colDate.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            formatDate(data.getValue().dateDemande())
        ));
        
        colDemandeur.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            nvl(data.getValue().demandeurNom())
        ));
        
        colType.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            data.getValue().typeDemande().getLibelle()
        ));
        
        colProduit.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            nvl(data.getValue().getNomProduit())
        ));
        
        colUID.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            nvl(data.getValue().getUidProduit())
        ));
        
        colSN.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            nvl(data.getValue().produitSn())
        ));
        
        colPanne.setCellValueFactory(data -> new ReadOnlyStringWrapper(
            truncate(nvl(data.getValue().panneDescription()), 50)
        ));
        
        // Lier les données
        tableDemandesEnAttente.setItems(demandesEnAttente);
    }
    
    private void configurerDetailsSelection() {
        // Écouter les changements de sélection
        tableDemandesEnAttente.getSelectionModel().selectedItemProperty().addListener(
            (obs, ancienne, nouvelle) -> {
                demandeSelectionnee = nouvelle;
                afficherDetailsDemande(nouvelle);
            }
        );
    }
    
    @FXML
    private void onActualiser() {
        chargerDemandesEnAttente();
    }
    
    private void chargerDemandesEnAttente() {
        try {
            List<DemandeIntervention> demandes = demandeRepo.findDemandesEnAttente();
            demandesEnAttente.clear();
            demandesEnAttente.addAll(demandes);
            
            // Mettre à jour les statistiques
            lblStats.setText(demandes.size() + " demande(s) en attente");
            
        } catch (Exception e) {
            showError("Erreur de chargement", "Impossible de charger les demandes: " + e.getMessage());
        }
    }
    
    private void afficherDetailsDemande(DemandeIntervention demande) {
        if (demande == null) {
            viderDetails();
            return;
        }
        
        // Informations générales
        lblDetailId.setText(String.valueOf(demande.id()));
        lblDetailType.setText(demande.typeDemande().getLibelle());
        lblDetailDemandeur.setText(nvl(demande.demandeurNom()));
        lblDetailDate.setText(formatDate(demande.dateDemande()));
        lblDetailProduit.setText(nvl(demande.getNomProduit()));
        lblDetailUID.setText(nvl(demande.getUidProduit()));
        lblDetailSN.setText(nvl(demande.produitSn()));
        lblDetailDetecteur.setText(nvl(demande.detecteur()));
        
        taDetailPanne.setText(nvl(demande.panneDescription()));
        taDetailNotes.setText(nvl(demande.clientNote()));
        
        // Section nouveau produit
        boolean isNouveauProduit = demande.typeDemande() == TypeDemande.PRODUIT_NON_REPERTORIE;
        vboxNouveauProduit.setVisible(isNouveauProduit);
        vboxNouveauProduit.setManaged(isNouveauProduit);
        
        if (isNouveauProduit) {
            tfNouveauFabricant.setText(nvl(demande.produitFabricant()));
            tfNouvelleCategorie.setText(nvl(demande.produitCategory()));
            taNouvelleDescription.setText(nvl(demande.produitDescription()));
        }
        
        // Activer les boutons de validation
        btnValider.setDisable(false);
        btnRejeter.setDisable(false);
    }
    
    private void viderDetails() {
        lblDetailId.setText("");
        lblDetailType.setText("");
        lblDetailDemandeur.setText("");
        lblDetailDate.setText("");
        lblDetailProduit.setText("");
        lblDetailUID.setText("");
        lblDetailSN.setText("");
        lblDetailDetecteur.setText("");
        
        taDetailPanne.setText("");
        taDetailNotes.setText("");
        
        vboxNouveauProduit.setVisible(false);
        vboxNouveauProduit.setManaged(false);
        
        btnValider.setDisable(true);
        btnRejeter.setDisable(true);
    }
    
    @FXML
    private void onValiderDemande() {
        if (demandeSelectionnee == null) {
            showError("Erreur", "Aucune demande sélectionnée");
            return;
        }
        
        try {
            // Créer le produit si nécessaire
            Long nouveauProduitId = null;
            if (demandeSelectionnee.typeDemande() == TypeDemande.PRODUIT_NON_REPERTORIE && cbCreerProduit.isSelected()) {
                nouveauProduitId = creerNouveauProduit();
                if (nouveauProduitId == null) {
                    return; // Erreur dans la création
                }
            }
            
            // Créer l'intervention
            Long interventionId = creerIntervention(nouveauProduitId);
            if (interventionId == null) {
                return; // Erreur dans la création de l'intervention
            }
            
            // Valider la demande
            String validateur = System.getProperty("user.name"); // TODO: Récupérer l'utilisateur connecté
            String notes = nvl(tfNotesValidation.getText());
            
            boolean success = demandeRepo.validerDemande(
                demandeSelectionnee.id(), 
                validateur, 
                notes, 
                interventionId
            );
            
            if (success) {
                showInfo("Validation réussie", "La demande a été validée avec succès.");
                chargerDemandesEnAttente(); // Recharger la liste
                viderDetails();
            } else {
                showError("Erreur", "Impossible de valider la demande");
            }
            
        } catch (Exception e) {
            showError("Erreur de validation", "Erreur lors de la validation: " + e.getMessage());
        }
    }
    
    @FXML
    private void onRejeterDemande() {
        if (demandeSelectionnee == null) {
            showError("Erreur", "Aucune demande sélectionnée");
            return;
        }
        
        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer le rejet");
        confirmation.setHeaderText("Rejeter la demande");
        confirmation.setContentText("Êtes-vous sûr de vouloir rejeter cette demande ?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String validateur = System.getProperty("user.name");
                String notes = nvl(tfNotesValidation.getText());
                
                boolean success = demandeRepo.rejeterDemande(
                    demandeSelectionnee.id(), 
                    validateur, 
                    notes
                );
                
                if (success) {
                    showInfo("Rejet confirmé", "La demande a été rejetée.");
                    chargerDemandesEnAttente();
                    viderDetails();
                } else {
                    showError("Erreur", "Impossible de rejeter la demande");
                }
                
            } catch (Exception e) {
                showError("Erreur de rejet", "Erreur lors du rejet: " + e.getMessage());
            }
        }
    }
    
    private Long creerNouveauProduit() {
        try {
            // Générer un UID unique
            String uid = uidService.generateUniqueUid();
            
            // Récupérer les informations du formulaire
            String nom = nvl(demandeSelectionnee.getNomProduit());
            String sn = nvl(demandeSelectionnee.produitSn());
            String fabricant = nvl(tfNouveauFabricant.getText());
            String situation = "En stock"; // Situation par défaut
            
            // Créer le produit
            long produitId = productRepo.insert(nom, sn, fabricant, uid, situation);
            
            if (produitId > 0) {
                showInfo("Produit créé", "Nouveau produit créé avec UID: " + uid);
                return produitId;
            } else {
                showError("Erreur", "Impossible de créer le produit");
                return null;
            }
            
        } catch (Exception e) {
            showError("Erreur création produit", "Erreur lors de la création du produit: " + e.getMessage());
            return null;
        }
    }
    
    private Long creerIntervention(Long produitId) {
        try {
            // Utiliser l'ID du produit existant ou nouvellement créé
            Long idProduit = produitId != null ? produitId : demandeSelectionnee.productId();
            
            if (idProduit == null) {
                showError("Erreur", "Aucun produit associé à cette demande");
                return null;
            }
            
            // Créer l'intervention avec la signature correcte
            long interventionId = interventionRepo.insert(
                idProduit,
                nvl(demandeSelectionnee.produitSn()), // serialNumber
                nvl(demandeSelectionnee.clientNote()), // clientNote
                nvl(demandeSelectionnee.panneDescription()) // defectDescription
            );
            
            return interventionId > 0 ? interventionId : null;
            
        } catch (Exception e) {
            showError("Erreur création intervention", "Erreur lors de la création de l'intervention: " + e.getMessage());
            return null;
        }
    }
    
    // Méthodes utilitaires
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            // Adapter selon le format de date stocké dans la base
            return dateStr; // Simplification pour l'instant
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    private String nvl(String str) {
        return str == null ? "" : str;
    }
    
    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        return str.substring(0, maxLength) + "...";
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