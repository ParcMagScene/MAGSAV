package com.magsav.model;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Modèle JavaFX pour les lignes de commande
 */
public class LigneCommande {
    
    // Propriétés principales
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty commandeId = new SimpleLongProperty();
    private final LongProperty produitId = new SimpleLongProperty();
    private final StringProperty produitNom = new SimpleStringProperty();
    private final StringProperty produitReference = new SimpleStringProperty();
    
    // Quantités et prix
    private final IntegerProperty quantiteCommandee = new SimpleIntegerProperty();
    private final IntegerProperty quantiteRecue = new SimpleIntegerProperty(0);
    private final ObjectProperty<BigDecimal> prixUnitaireHT = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> prixUnitaire = new SimpleObjectProperty<>(BigDecimal.ZERO); // Alias pour compatibilité
    private final ObjectProperty<BigDecimal> remise = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final DoubleProperty tauxTVA = new SimpleDoubleProperty(20.0); // 20% par défaut
    
    // Montants calculés
    private final ObjectProperty<BigDecimal> montantHT = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> montantTVA = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> montantTTC = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    // Détails
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty unite = new SimpleStringProperty("pce"); // pièce par défaut
    private final StringProperty commentaires = new SimpleStringProperty();
    
    // Statut de réception
    private final ObjectProperty<StatutReception> statutReception = new SimpleObjectProperty<>(StatutReception.EN_ATTENTE);
    
    // Métadonnées
    private final ObjectProperty<LocalDateTime> dateCreation = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> dateModification = new SimpleObjectProperty<>();
    
    // Référence vers la commande parent
    private Commande commande;
    
    /**
     * Énumération des statuts de réception
     */
    public enum StatutReception {
        EN_ATTENTE("En attente", "#6c757d"),
        PARTIELLE("Partielle", "#fd7e14"),
        COMPLETE("Complète", "#28a745"),
        REFUSE("Refusé", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        StatutReception(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    /**
     * Énumération des statuts de ligne (pour compatibilité)
     */
    public enum StatutLigne {
        COMMANDEE("Commandée", "#007bff"),
        EN_COURS("En cours", "#fd7e14"),
        LIVREE("Livrée", "#28a745"),
        ANNULEE("Annulée", "#dc3545");
        
        private final String displayName;
        private final String color;
        
        StatutLigne(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
    
    // Constructeurs
    public LigneCommande() {
        this.dateCreation.set(LocalDateTime.now());
        this.dateModification.set(LocalDateTime.now());
        
        // Listeners pour recalcul automatique
        setupCalculationListeners();
    }
    
    public LigneCommande(long produitId, String produitNom, String produitReference, 
                        int quantite, BigDecimal prixUnitaire) {
        this();
        setProduitId(produitId);
        setProduitNom(produitNom);
        setProduitReference(produitReference);
        setQuantiteCommandee(quantite);
        setPrixUnitaireHT(prixUnitaire);
        calculerMontants();
    }
    
    private void setupCalculationListeners() {
        // Recalcul automatique des montants
        quantiteCommandee.addListener((obs, oldVal, newVal) -> calculerMontants());
        prixUnitaireHT.addListener((obs, oldVal, newVal) -> calculerMontants());
        tauxTVA.addListener((obs, oldVal, newVal) -> calculerMontants());
        
        // Mise à jour du statut de réception
        quantiteRecue.addListener((obs, oldVal, newVal) -> mettreAJourStatutReception());
    }
    
    // Méthodes de calcul
    public void calculerMontants() {
        if (getPrixUnitaireHT() == null || getQuantiteCommandee() <= 0) {
            setMontantHT(BigDecimal.ZERO);
            setMontantTVA(BigDecimal.ZERO);
            setMontantTTC(BigDecimal.ZERO);
            return;
        }
        
        // Montant HT = Quantité × Prix unitaire HT
        BigDecimal montantHTCalcule = getPrixUnitaireHT()
                .multiply(BigDecimal.valueOf(getQuantiteCommandee()))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Montant TVA = Montant HT × Taux TVA / 100
        BigDecimal montantTVACalcule = montantHTCalcule
                .multiply(BigDecimal.valueOf(getTauxTVA() / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        // Montant TTC = Montant HT + Montant TVA
        BigDecimal montantTTCCalcule = montantHTCalcule.add(montantTVACalcule);
        
        setMontantHT(montantHTCalcule);
        setMontantTVA(montantTVACalcule);
        setMontantTTC(montantTTCCalcule);
        
        // Notifier la commande parent pour recalcul des totaux
        if (commande != null) {
            commande.recalculerMontants();
        }
    }
    
    private void mettreAJourStatutReception() {
        int commandee = getQuantiteCommandee();
        int recue = getQuantiteRecue();
        
        if (recue == 0) {
            setStatutReception(StatutReception.EN_ATTENTE);
        } else if (recue < commandee) {
            setStatutReception(StatutReception.PARTIELLE);
        } else if (recue >= commandee) {
            setStatutReception(StatutReception.COMPLETE);
        }
    }
    
    // Méthodes utilitaires
    public boolean estTotalementRecue() {
        return getQuantiteRecue() >= getQuantiteCommandee();
    }
    
    public boolean estPartiellemementRecue() {
        return getQuantiteRecue() > 0 && getQuantiteRecue() < getQuantiteCommandee();
    }
    
    public int getQuantiteRestante() {
        return Math.max(0, getQuantiteCommandee() - getQuantiteRecue());
    }
    
    public double getPourcentageReception() {
        if (getQuantiteCommandee() == 0) return 0.0;
        return (double) getQuantiteRecue() / getQuantiteCommandee() * 100.0;
    }
    
    public void recevoirQuantite(int quantite) {
        int nouvelleQuantiteRecue = getQuantiteRecue() + quantite;
        int maxQuantite = getQuantiteCommandee();
        
        // Ne pas dépasser la quantité commandée
        setQuantiteRecue(Math.min(nouvelleQuantiteRecue, maxQuantite));
    }
    
    public String getResumeReception() {
        return String.format("%d / %d %s", getQuantiteRecue(), getQuantiteCommandee(), getUnite());
    }
    
    public String getDisplayName() {
        return String.format("%s - %s (x%d)", getProduitReference(), getProduitNom(), getQuantiteCommandee());
    }
    
    public BigDecimal getPrixUnitaireTTC() {
        if (getPrixUnitaireHT() == null) return BigDecimal.ZERO;
        
        BigDecimal tvaUnitaire = getPrixUnitaireHT()
                .multiply(BigDecimal.valueOf(getTauxTVA() / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        
        return getPrixUnitaireHT().add(tvaUnitaire);
    }
    
    // Getters et setters pour les propriétés
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }
    
    public long getCommandeId() { return commandeId.get(); }
    public void setCommandeId(long commandeId) { this.commandeId.set(commandeId); }
    public LongProperty commandeIdProperty() { return commandeId; }
    
    public long getProduitId() { return produitId.get(); }
    public void setProduitId(long produitId) { this.produitId.set(produitId); }
    public LongProperty produitIdProperty() { return produitId; }
    
    public String getProduitNom() { return produitNom.get(); }
    public void setProduitNom(String produitNom) { this.produitNom.set(produitNom); }
    public StringProperty produitNomProperty() { return produitNom; }
    
    public String getProduitReference() { return produitReference.get(); }
    public void setProduitReference(String produitReference) { this.produitReference.set(produitReference); }
    public StringProperty produitReferenceProperty() { return produitReference; }
    
    public int getQuantiteCommandee() { return quantiteCommandee.get(); }
    public void setQuantiteCommandee(int quantiteCommandee) { this.quantiteCommandee.set(quantiteCommandee); }
    public IntegerProperty quantiteCommandeeProperty() { return quantiteCommandee; }
    
    public int getQuantiteRecue() { return quantiteRecue.get(); }
    public void setQuantiteRecue(int quantiteRecue) { this.quantiteRecue.set(quantiteRecue); }
    public IntegerProperty quantiteRecueProperty() { return quantiteRecue; }
    
    public BigDecimal getPrixUnitaireHT() { return prixUnitaireHT.get(); }
    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) { this.prixUnitaireHT.set(prixUnitaireHT); }
    public ObjectProperty<BigDecimal> prixUnitaireHTProperty() { return prixUnitaireHT; }
    
    public double getTauxTVA() { return tauxTVA.get(); }
    public void setTauxTVA(double tauxTVA) { this.tauxTVA.set(tauxTVA); }
    public DoubleProperty tauxTVAProperty() { return tauxTVA; }
    
    public BigDecimal getMontantHT() { return montantHT.get(); }
    public void setMontantHT(BigDecimal montantHT) { this.montantHT.set(montantHT); }
    public ObjectProperty<BigDecimal> montantHTProperty() { return montantHT; }
    
    public BigDecimal getMontantTVA() { return montantTVA.get(); }
    public void setMontantTVA(BigDecimal montantTVA) { this.montantTVA.set(montantTVA); }
    public ObjectProperty<BigDecimal> montantTVAProperty() { return montantTVA; }
    
    public BigDecimal getMontantTTC() { return montantTTC.get(); }
    public void setMontantTTC(BigDecimal montantTTC) { this.montantTTC.set(montantTTC); }
    public ObjectProperty<BigDecimal> montantTTCProperty() { return montantTTC; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public StringProperty descriptionProperty() { return description; }
    
    public String getUnite() { return unite.get(); }
    public void setUnite(String unite) { this.unite.set(unite); }
    public StringProperty uniteProperty() { return unite; }
    
    public String getCommentaires() { return commentaires.get(); }
    public void setCommentaires(String commentaires) { this.commentaires.set(commentaires); }
    public StringProperty commentairesProperty() { return commentaires; }
    
    public StatutReception getStatutReception() { return statutReception.get(); }
    public void setStatutReception(StatutReception statutReception) { this.statutReception.set(statutReception); }
    public ObjectProperty<StatutReception> statutReceptionProperty() { return statutReception; }
    
    public LocalDateTime getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation.set(dateCreation); }
    public ObjectProperty<LocalDateTime> dateCreationProperty() { return dateCreation; }
    
    public LocalDateTime getDateModification() { return dateModification.get(); }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification.set(dateModification); }
    public ObjectProperty<LocalDateTime> dateModificationProperty() { return dateModification; }
    
    public Commande getCommande() { return commande; }
    public void setCommande(Commande commande) { 
        this.commande = commande;
        if (commande != null) {
            setCommandeId(commande.getId());
        }
    }
    
    // Getters et setters pour les propriétés ajoutées
    public BigDecimal getPrixUnitaire() { return prixUnitaire.get(); }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { 
        this.prixUnitaire.set(prixUnitaire);
        this.prixUnitaireHT.set(prixUnitaire); // Synchronisation
    }
    public ObjectProperty<BigDecimal> prixUnitaireProperty() { return prixUnitaire; }
    
    public BigDecimal getRemise() { return remise.get(); }
    public void setRemise(BigDecimal remise) { this.remise.set(remise); }
    public ObjectProperty<BigDecimal> remiseProperty() { return remise; }
    
    // Méthode pour créer et définir un statut de ligne
    public void setStatut(StatutLigne statut) {
        // Conversion vers StatutReception pour le mapping
        switch (statut) {
            case COMMANDEE -> setStatutReception(StatutReception.EN_ATTENTE);
            case EN_COURS -> setStatutReception(StatutReception.PARTIELLE);
            case LIVREE -> setStatutReception(StatutReception.COMPLETE);
            case ANNULEE -> setStatutReception(StatutReception.REFUSE);
        }
    }
    
    public StatutLigne getStatut() {
        // Conversion depuis StatutReception
        return switch (getStatutReception()) {
            case EN_ATTENTE -> StatutLigne.COMMANDEE;
            case PARTIELLE -> StatutLigne.EN_COURS;
            case COMPLETE -> StatutLigne.LIVREE;
            case REFUSE -> StatutLigne.ANNULEE;
        };
    }
    
    @Override
    public String toString() {
        return String.format("LigneCommande{id=%d, produit='%s', quantite=%d, prixHT=%.2f, montantTTC=%.2f}",
                getId(), getProduitNom(), getQuantiteCommandee(), 
                getPrixUnitaireHT() != null ? getPrixUnitaireHT().doubleValue() : 0.0,
                getMontantTTC() != null ? getMontantTTC().doubleValue() : 0.0);
    }
}