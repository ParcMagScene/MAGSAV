package com.magsav.gui;

import com.magsav.model.DemandeIntervention;
import com.magsav.model.DemandeIntervention.TypeDemande;
import com.magsav.model.DemandeIntervention.StatutDemande;
import com.magsav.repo.DemandeInterventionRepository;
import com.magsav.repo.ProductRepository;
import com.magsav.repo.CategoryRepository;
import com.magsav.service.QrScannerService;
import com.magsav.service.UidGenerationService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class NouvelleDemandeInterventionController {
    
    // Services
    private final QrScannerService qrScanner = new QrScannerService();
    private final UidGenerationService uidService = new UidGenerationService();
    private final ProductRepository productRepo = new ProductRepository();
    private final CategoryRepository categoryRepo = new CategoryRepository();
    private final DemandeInterventionRepository demandeRepo = new DemandeInterventionRepository();
    
    // État de l'identification
    private String produitUidIdentifie = null;
    private Long produitIdExistant = null;
    private TypeDemande typeDemande = null;
    
    // Contrôles d'identification
    @FXML private Button btnScanQr;
    @FXML private Button btnSaisieManuelle;
    @FXML private VBox vboxResultatIdentification;
    @FXML private Label lblStatutProduit;
    @FXML private Label lblDetailsProduit;
    
    // Contrôles nouveau produit
    @FXML private VBox vboxNouveauProduit;
    @FXML private TextField tfProduitNom;
    @FXML private Button btnChoisirProduit;
    @FXML private TextField tfSerialNumber;
    @FXML private Button btnChoisirSN;
    @FXML private TextField tfFabricant;
    @FXML private TextField tfCategorie;
    
    // Contrôles intervention
    @FXML private TextArea taPanneDescription;
    @FXML private TextArea taClientNote;
    @FXML private TextField tfDetecteur;
    
    // Contrôles demandeur
    @FXML private TextField tfDemandeurNom;
    
    @FXML
    private void initialize() {
        // Initialiser avec le nom d'utilisateur système par défaut
        tfDemandeurNom.setText(System.getProperty("user.name"));
        
        // Masquer la section nouveau produit par défaut
        vboxNouveauProduit.setVisible(false);
        vboxNouveauProduit.setManaged(false);
    }
    
    @FXML
    private void onScanQrCode() {
        Optional<String> uidScanne = qrScanner.scanQrCode();
        if (uidScanne.isPresent()) {
            traiterUidIdentifie(uidScanne.get());
        }
    }
    
    @FXML
    private void onSaisieManuelle() {
        Optional<String> uidSaisi = qrScanner.saisirUidManuellement(
            "Saisie manuelle UID", 
            "Saisissez l'UID du produit:"
        );
        if (uidSaisi.isPresent()) {
            traiterUidIdentifie(uidSaisi.get());
        }
    }
    
    /**
     * Traite un UID identifié (scanné ou saisi) et détermine s'il s'agit d'un produit existant
     */
    private void traiterUidIdentifie(String uid) {
        produitUidIdentifie = uid;
        
        // Vérifier si le produit existe dans la base
        Optional<ProductRepository.ProductRow> produitExistant = rechercherProduitParUid(uid);
        
        if (produitExistant.isPresent()) {
            // Produit existant trouvé
            ProductRepository.ProductRow produit = produitExistant.get();
            produitIdExistant = produit.id();
            typeDemande = TypeDemande.PRODUIT_REPERTORIE;
            
            afficherProduitExistant(produit);
            masquerSectionNouveauProduit();
            
        } else {
            // Nouveau produit
            produitIdExistant = null;
            typeDemande = TypeDemande.PRODUIT_NON_REPERTORIE;
            
            afficherNouveauProduit(uid);
            afficherSectionNouveauProduit();
        }
    }
    
    private Optional<ProductRepository.ProductRow> rechercherProduitParUid(String uid) {
        try {
            return productRepo.findByUid(uid);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private void afficherProduitExistant(ProductRepository.ProductRow produit) {
        lblStatutProduit.setText("✅ Produit répertorié trouvé");
        lblStatutProduit.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        
        String details = String.format("Nom: %s | SN: %s | Fabricant: %s | UID: %s",
            produit.nom(), produit.sn(), produit.fabricant(), produit.uid());
        lblDetailsProduit.setText(details);
    }
    
    private void afficherNouveauProduit(String uid) {
        lblStatutProduit.setText("⚠️ Produit non-répertorié");
        lblStatutProduit.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        
        String details = String.format("UID: %s - Ce produit n'existe pas dans la base de données", uid);
        lblDetailsProduit.setText(details);
    }
    
    private void afficherSectionNouveauProduit() {
        vboxNouveauProduit.setVisible(true);
        vboxNouveauProduit.setManaged(true);
    }
    
    private void masquerSectionNouveauProduit() {
        vboxNouveauProduit.setVisible(false);
        vboxNouveauProduit.setManaged(false);
    }
    
    @FXML
    private void onChoisirProduit() {
        // TODO: Implémenter dialogue de sélection par arborescence de catégories
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité en développement");
        alert.setHeaderText("Sélection par catégories");
        alert.setContentText("Cette fonctionnalité sera implémentée dans une prochaine version.");
        alert.showAndWait();
    }
    
    @FXML
    private void onChoisirSerialNumber() {
        // TODO: Implémenter sélection parmi les SN existants pour le produit choisi
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Fonctionnalité en développement");
        alert.setHeaderText("Sélection numéro de série");
        alert.setContentText("Cette fonctionnalité sera implémentée dans une prochaine version.");
        alert.showAndWait();
    }
    
    /**
     * Valide que tous les champs obligatoires sont remplis
     */
    public boolean isValid() {
        // Vérifier que l'identification a été faite
        if (produitUidIdentifie == null || typeDemande == null) {
            showError("Veuillez d'abord identifier le produit (scan QR ou saisie manuelle)");
            return false;
        }
        
        // Vérifier la description de la panne
        if (taPanneDescription.getText() == null || taPanneDescription.getText().trim().isEmpty()) {
            showError("Veuillez saisir une description de la panne");
            return false;
        }
        
        // Vérifier le demandeur
        if (tfDemandeurNom.getText() == null || tfDemandeurNom.getText().trim().isEmpty()) {
            showError("Veuillez saisir le nom du demandeur");
            return false;
        }
        
        // Si nouveau produit, vérifier les infos produit
        if (typeDemande == TypeDemande.PRODUIT_NON_REPERTORIE) {
            if (tfProduitNom.getText() == null || tfProduitNom.getText().trim().isEmpty()) {
                showError("Veuillez saisir le nom du produit");
                return false;
            }
            
            if (tfSerialNumber.getText() == null || tfSerialNumber.getText().trim().isEmpty()) {
                showError("Veuillez saisir le numéro de série");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Crée et sauvegarde la demande d'intervention
     */
    public long creerDemande() {
        if (!isValid()) {
            return -1;
        }
        
        DemandeIntervention demande = new DemandeIntervention(
            0, // ID auto-généré
            
            StatutDemande.EN_ATTENTE,
            typeDemande,
            
            produitIdExistant, // null si nouveau produit
            
            // Données produit temporaires (pour nouveaux produits)
            nvl(tfProduitNom.getText()),
            nvl(tfSerialNumber.getText()),
            produitUidIdentifie,
            nvl(tfFabricant.getText()),
            nvl(tfCategorie.getText()),
            null, // subcategory
            null, // description
            
            // Informations propriétaire - TODO: implémenter l'interface utilisateur
            null, // type_proprietaire
            null, // proprietaire_id
            null, // demande_creation_proprietaire_id
            null, // proprietaire_nom_temp
            null, // proprietaire_details_temp
            
            // Détails intervention
            nvl(taPanneDescription.getText()),
            nvl(taClientNote.getText()),
            nvl(tfDetecteur.getText()),
            null, // detector_societe_id TODO: à implémenter
            
            // Métadonnées
            nvl(tfDemandeurNom.getText()),
            null, // date_demande (auto)
            null, // date_validation
            null, // validateur_nom
            null, // notes_validation
            
            null  // intervention_id
        );
        
        return demandeRepo.createDemande(demande);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText("Champs manquants");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String nvl(String str) {
        return str == null ? "" : str.trim();
    }
    
    // Getters pour accès externe si nécessaire
    public String getProduitUidIdentifie() { return produitUidIdentifie; }
    public TypeDemande getTypeDemande() { return typeDemande; }
    public Long getProduitIdExistant() { return produitIdExistant; }
}