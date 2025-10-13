package com.magsav.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle JavaFX pour les commandes fournisseurs
 */
public class Commande {
    
    // Propriétés principales
    private final LongProperty id = new SimpleLongProperty();
    private final StringProperty numeroCommande = new SimpleStringProperty();
    private final LongProperty fournisseurId = new SimpleLongProperty();
    private final StringProperty fournisseurNom = new SimpleStringProperty();
    private final ObjectProperty<StatutCommande> statut = new SimpleObjectProperty<>(StatutCommande.BROUILLON);
    private final ObjectProperty<TypeCommande> type = new SimpleObjectProperty<>(TypeCommande.STANDARD);
    
    // Dates
    private final ObjectProperty<LocalDate> dateCommande = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateLivraisonPrevue = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateLivraisonReelle = new SimpleObjectProperty<>();
    
    // Montants
    private final ObjectProperty<BigDecimal> montantHT = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> montantTVA = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> montantTTC = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    // Détails
    private final StringProperty commentaires = new SimpleStringProperty();
    private final StringProperty numeroFactureFournisseur = new SimpleStringProperty();
    private final StringProperty adresseLivraison = new SimpleStringProperty();
    private final StringProperty contactLivraison = new SimpleStringProperty();
    
    // Propriétés ajoutées pour compatibilité avec les contrôleurs
    private final StringProperty reference = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final BooleanProperty urgente = new SimpleBooleanProperty(false);
    private final BooleanProperty facturee = new SimpleBooleanProperty(false);
    private final ObjectProperty<Societe> fournisseur = new SimpleObjectProperty<>();
    
    // Suivi
    private final StringProperty transporteur = new SimpleStringProperty();
    private final StringProperty numeroSuivi = new SimpleStringProperty();
    private final BooleanProperty receptionComplete = new SimpleBooleanProperty(false);
    private final BooleanProperty factureRecue = new SimpleBooleanProperty(false);
    
    // Métadonnées
    private final ObjectProperty<LocalDateTime> dateCreation = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dateModification = new SimpleObjectProperty<>();
    private final LongProperty utilisateurId = new SimpleLongProperty();
    private final StringProperty utilisateurNom = new SimpleStringProperty();
    
    // Lignes de commande
    private final ObservableList<LigneCommande> lignes = FXCollections.observableArrayList();
    
    /**
     * Énumération des statuts de commande
     */
    public enum StatutCommande {
        BROUILLON("Brouillon", "#6c757d"),
        VALIDEE("Validée", "#28a745"),
        ENVOYEE("Envoyée", "#007bff"),
        CONFIRMEE("Confirmée", "#17a2b8"),
        EXPEDIE("Expédiée", "#fd7e14"),
        LIVREE("Livrée", "#28a745"),
        RECUE("Reçue", "#6f42c1"),
        FACTUREE("Facturée", "#20c997"),
        ANNULEE("Annulée", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        StatutCommande(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    /**
     * Énumération des types de commande
     */
    public enum TypeCommande {
        STANDARD("Standard"),
        URGENTE("Urgente"),
        PRECOMMANDE("Pré-commande"),
        STOCK_SECURITE("Stock de sécurité"),
        REMPLACEMENT("Remplacement");
        
        private final String displayName;
        
        TypeCommande(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    // Constructeurs
    public Commande() {
        this.dateCreation.set(LocalDateTime.now());
        this.dateModification.set(LocalDateTime.now());
        this.dateCommande.set(LocalDate.now());
        
        // Génération automatique du numéro de commande
        generateNumeroCommande();
        
        // Listeners pour recalcul automatique
        setupCalculationListeners();
    }
    
    public Commande(long id) {
        this();
        this.id.set(id);
    }
    
    // Méthodes de génération
    private void generateNumeroCommande() {
        if (getNumeroCommande() == null || getNumeroCommande().isEmpty()) {
            String numero = "CMD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                          "-" + String.format("%04d", System.currentTimeMillis() % 10000);
            setNumeroCommande(numero);
        }
    }
    
    private void setupCalculationListeners() {
        // Recalcul automatique des montants quand les lignes changent
        lignes.addListener((javafx.collections.ListChangeListener<LigneCommande>) change -> {
            recalculerMontants();
        });
    }
    
    // Méthodes de calcul
    public void recalculerMontants() {
        BigDecimal totalHT = BigDecimal.ZERO;
        BigDecimal totalTVA = BigDecimal.ZERO;
        
        for (LigneCommande ligne : lignes) {
            totalHT = totalHT.add(ligne.getMontantHT());
            totalTVA = totalTVA.add(ligne.getMontantTVA());
        }
        
        setMontantHT(totalHT);
        setMontantTVA(totalTVA);
        setMontantTTC(totalHT.add(totalTVA));
    }
    
    public void ajouterLigne(LigneCommande ligne) {
        ligne.setCommande(this);
        lignes.add(ligne);
        recalculerMontants();
    }
    
    public void supprimerLigne(LigneCommande ligne) {
        lignes.remove(ligne);
        recalculerMontants();
    }
    
    // Méthodes utilitaires
    public boolean peutEtreModifiee() {
        return statut.get() == StatutCommande.BROUILLON || statut.get() == StatutCommande.VALIDEE;
    }
    
    public boolean estEnAttenteLivraison() {
        return statut.get() == StatutCommande.EXPEDIE || statut.get() == StatutCommande.CONFIRMEE;
    }
    
    public boolean estTerminee() {
        return statut.get() == StatutCommande.RECUE || statut.get() == StatutCommande.FACTUREE;
    }
    
    public int getNombreLignes() {
        return lignes.size();
    }
    
    public int getQuantiteTotale() {
        return lignes.stream().mapToInt(LigneCommande::getQuantiteCommandee).sum();
    }
    
    public String getResumeLivraison() {
        if (getDateLivraisonReelle() != null) {
            return "Livrée le " + getDateLivraisonReelle().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } else if (getDateLivraisonPrevue() != null) {
            return "Prévue le " + getDateLivraisonPrevue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "Non planifiée";
    }
    
    public String getResumeStatut() {
        return statut.get().getDisplayName();
    }
    
    public String getDisplayName() {
        return getNumeroCommande() + " - " + getFournisseurNom();
    }
    
    // Getters et setters pour les propriétés
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }
    
    public String getNumeroCommande() { return numeroCommande.get(); }
    public void setNumeroCommande(String numeroCommande) { this.numeroCommande.set(numeroCommande); }
    public StringProperty numeroCommandeProperty() { return numeroCommande; }
    
    public long getFournisseurId() { return fournisseurId.get(); }
    public void setFournisseurId(long fournisseurId) { this.fournisseurId.set(fournisseurId); }
    public LongProperty fournisseurIdProperty() { return fournisseurId; }
    
    public String getFournisseurNom() { return fournisseurNom.get(); }
    public void setFournisseurNom(String fournisseurNom) { this.fournisseurNom.set(fournisseurNom); }
    public StringProperty fournisseurNomProperty() { return fournisseurNom; }
    
    public StatutCommande getStatut() { return statut.get(); }
    public void setStatut(StatutCommande statut) { this.statut.set(statut); }
    public ObjectProperty<StatutCommande> statutProperty() { return statut; }
    
    public TypeCommande getType() { return type.get(); }
    public void setType(TypeCommande type) { this.type.set(type); }
    public ObjectProperty<TypeCommande> typeProperty() { return type; }
    
    public LocalDate getDateCommande() { return dateCommande.get(); }
    public void setDateCommande(LocalDate dateCommande) { this.dateCommande.set(dateCommande); }
    public ObjectProperty<LocalDate> dateCommandeProperty() { return dateCommande; }
    
    public LocalDate getDateLivraisonPrevue() { return dateLivraisonPrevue.get(); }
    public void setDateLivraisonPrevue(LocalDate dateLivraisonPrevue) { this.dateLivraisonPrevue.set(dateLivraisonPrevue); }
    public ObjectProperty<LocalDate> dateLivraisonPrevueProperty() { return dateLivraisonPrevue; }
    
    public LocalDate getDateLivraisonReelle() { return dateLivraisonReelle.get(); }
    public void setDateLivraisonReelle(LocalDate dateLivraisonReelle) { this.dateLivraisonReelle.set(dateLivraisonReelle); }
    public ObjectProperty<LocalDate> dateLivraisonReelleProperty() { return dateLivraisonReelle; }
    
    public BigDecimal getMontantHT() { return montantHT.get(); }
    public void setMontantHT(BigDecimal montantHT) { this.montantHT.set(montantHT); }
    public ObjectProperty<BigDecimal> montantHTProperty() { return montantHT; }
    
    public BigDecimal getMontantTVA() { return montantTVA.get(); }
    public void setMontantTVA(BigDecimal montantTVA) { this.montantTVA.set(montantTVA); }
    public ObjectProperty<BigDecimal> montantTVAProperty() { return montantTVA; }
    
    public BigDecimal getMontantTTC() { return montantTTC.get(); }
    public void setMontantTTC(BigDecimal montantTTC) { this.montantTTC.set(montantTTC); }
    public ObjectProperty<BigDecimal> montantTTCProperty() { return montantTTC; }
    
    public String getCommentaires() { return commentaires.get(); }
    public void setCommentaires(String commentaires) { this.commentaires.set(commentaires); }
    public StringProperty commentairesProperty() { return commentaires; }
    
    public String getNumeroFactureFournisseur() { return numeroFactureFournisseur.get(); }
    public void setNumeroFactureFournisseur(String numeroFactureFournisseur) { this.numeroFactureFournisseur.set(numeroFactureFournisseur); }
    public StringProperty numeroFactureFournisseurProperty() { return numeroFactureFournisseur; }
    
    public String getAdresseLivraison() { return adresseLivraison.get(); }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison.set(adresseLivraison); }
    public StringProperty adresseLivraisonProperty() { return adresseLivraison; }
    
    public String getContactLivraison() { return contactLivraison.get(); }
    public void setContactLivraison(String contactLivraison) { this.contactLivraison.set(contactLivraison); }
    public StringProperty contactLivraisonProperty() { return contactLivraison; }
    
    public String getTransporteur() { return transporteur.get(); }
    public void setTransporteur(String transporteur) { this.transporteur.set(transporteur); }
    public StringProperty transporteurProperty() { return transporteur; }
    
    public String getNumeroSuivi() { return numeroSuivi.get(); }
    public void setNumeroSuivi(String numeroSuivi) { this.numeroSuivi.set(numeroSuivi); }
    public StringProperty numeroSuiviProperty() { return numeroSuivi; }
    
    public boolean isReceptionComplete() { return receptionComplete.get(); }
    public void setReceptionComplete(boolean receptionComplete) { this.receptionComplete.set(receptionComplete); }
    public BooleanProperty receptionCompleteProperty() { return receptionComplete; }
    
    public boolean isFactureRecue() { return factureRecue.get(); }
    public void setFactureRecue(boolean factureRecue) { this.factureRecue.set(factureRecue); }
    public BooleanProperty factureRecueProperty() { return factureRecue; }
    
    public LocalDateTime getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation.set(dateCreation); }
    public ObjectProperty<LocalDateTime> dateCreationProperty() { return dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification.get(); }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification.set(dateModification); }
    public ObjectProperty<LocalDateTime> dateModificationProperty() { return dateModification; }
    
    public long getUtilisateurId() { return utilisateurId.get(); }
    public void setUtilisateurId(long utilisateurId) { this.utilisateurId.set(utilisateurId); }
    public LongProperty utilisateurIdProperty() { return utilisateurId; }
    
    public String getUtilisateurNom() { return utilisateurNom.get(); }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom.set(utilisateurNom); }
    public StringProperty utilisateurNomProperty() { return utilisateurNom; }
    
    public ObservableList<LigneCommande> getLignes() { return lignes; }
    
    // Getters et setters pour les propriétés ajoutées
    public String getReference() { return reference.get(); }
    public void setReference(String reference) { this.reference.set(reference); }
    public StringProperty referenceProperty() { return reference; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }
    
    public boolean isUrgente() { return urgente.get(); }
    public void setUrgente(boolean urgente) { this.urgente.set(urgente); }
    public BooleanProperty urgenteProperty() { return urgente; }
    
    public boolean isFacturee() { return facturee.get(); }
    public void setFacturee(boolean facturee) { this.facturee.set(facturee); }
    public BooleanProperty factureeProperty() { return facturee; }
    
    public Societe getFournisseur() { return fournisseur.get(); }
    public void setFournisseur(Societe fournisseur) { this.fournisseur.set(fournisseur); }
    public ObjectProperty<Societe> fournisseurProperty() { return fournisseur; }
    
    @Override
    public String toString() {
        return String.format("Commande{id=%d, numero='%s', fournisseur='%s', statut=%s, montantTTC=%.2f}",
                getId(), getNumeroCommande(), getFournisseurNom(), 
                getStatut(), getMontantTTC() != null ? getMontantTTC().doubleValue() : 0.0);
    }
}