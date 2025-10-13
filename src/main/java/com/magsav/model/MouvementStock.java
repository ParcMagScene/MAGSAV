package com.magsav.model;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle JavaFX pour les mouvements de stock
 */
public class MouvementStock {
    
    // Propriétés principales
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty produitId = new SimpleLongProperty();
    private final StringProperty produitNom = new SimpleStringProperty();
    private final StringProperty produitReference = new SimpleStringProperty();
    
    // Type et détails du mouvement
    private final ObjectProperty<TypeMouvement> typeMouvement = new SimpleObjectProperty<>();
    private final IntegerProperty quantite = new SimpleIntegerProperty();
    private final IntegerProperty stockAvant = new SimpleIntegerProperty();
    private final IntegerProperty stockApres = new SimpleIntegerProperty();
    
    // Références et coûts
    private final StringProperty reference = new SimpleStringProperty();
    private final LongProperty documentId = new SimpleLongProperty(); // ID de la commande, intervention, etc.
    private final StringProperty documentType = new SimpleStringProperty(); // "COMMANDE", "INTERVENTION", etc.
    private final ObjectProperty<BigDecimal> coutUnitaire = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> coutTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    // Détails
    private final StringProperty commentaires = new SimpleStringProperty();
    private final StringProperty emplacement = new SimpleStringProperty();
    private final StringProperty numeroLot = new SimpleStringProperty();
    
    // Métadonnées
    private final ObjectProperty<LocalDateTime> dateMouvement = new SimpleObjectProperty<>();
    private final LongProperty utilisateurId = new SimpleLongProperty();
    private final StringProperty utilisateurNom = new SimpleStringProperty();
    
    /**
     * Énumération des types de mouvement
     */
    public enum TypeMouvement {
        ENTREE_ACHAT("Entrée - Achat", "in", "#28a745", "📦"),
        ENTREE_RETOUR("Entrée - Retour", "in", "#17a2b8", "↩️"),
        ENTREE_INVENTAIRE("Entrée - Inventaire", "in", "#6f42c1", "📊"),
        ENTREE_TRANSFERT("Entrée - Transfert", "in", "#fd7e14", "🔄"),
        
        SORTIE_VENTE("Sortie - Vente", "out", "#dc3545", "💰"),
        SORTIE_UTILISATION("Sortie - Utilisation", "out", "#ffc107", "🔧"),
        SORTIE_PERTE("Sortie - Perte", "out", "#6c757d", "❌"),
        SORTIE_TRANSFERT("Sortie - Transfert", "out", "#fd7e14", "🔄"),
        
        AJUSTEMENT_POSITIF("Ajustement +", "adjustment", "#20c997", "➕"),
        AJUSTEMENT_NEGATIF("Ajustement -", "adjustment", "#e83e8c", "➖");
        
        private final String displayName;
        private final String direction; // "in", "out", "adjustment"
        private final String color;
        private final String icon;
        
        TypeMouvement(String displayName, String direction, String color, String icon) {
            this.displayName = displayName;
            this.direction = direction;
            this.color = color;
            this.icon = icon;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDirection() { return direction; }
        public String getColor() { return color; }
        public String getIcon() { return icon; }
        
        public boolean isEntree() { return "in".equals(direction); }
        public boolean isSortie() { return "out".equals(direction); }
        public boolean isAjustement() { return "adjustment".equals(direction); }
    }
    
    // Constructeurs
    public MouvementStock() {
        this.dateMouvement.set(LocalDateTime.now());
        
        // Génération automatique de référence
        generateReference();
        
        // Listeners pour calcul automatique
        setupCalculationListeners();
    }
    
    public MouvementStock(long produitId, TypeMouvement type, int quantite, int stockAvant) {
        this();
        setProduitId(produitId);
        setTypeMouvement(type);
        setQuantite(quantite);
        setStockAvant(stockAvant);
        calculerStockApres();
    }
    
    private void generateReference() {
        if (getReference() == null || getReference().isEmpty()) {
            String ref = "MVT-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            setReference(ref);
        }
    }
    
    private void setupCalculationListeners() {
        // Recalcul automatique du stock après et du coût total
        quantite.addListener((obs, oldVal, newVal) -> {
            calculerStockApres();
            calculerCoutTotal();
        });
        
        stockAvant.addListener((obs, oldVal, newVal) -> calculerStockApres());
        typeMouvement.addListener((obs, oldVal, newVal) -> calculerStockApres());
        coutUnitaire.addListener((obs, oldVal, newVal) -> calculerCoutTotal());
    }
    
    // Méthodes de calcul
    private void calculerStockApres() {
        if (getTypeMouvement() == null) return;
        
        int stockCalcule = getStockAvant();
        
        if (getTypeMouvement().isEntree() || getTypeMouvement() == TypeMouvement.AJUSTEMENT_POSITIF) {
            stockCalcule += getQuantite();
        } else if (getTypeMouvement().isSortie() || getTypeMouvement() == TypeMouvement.AJUSTEMENT_NEGATIF) {
            stockCalcule -= getQuantite();
        }
        
        setStockApres(Math.max(0, stockCalcule)); // Stock ne peut pas être négatif
    }
    
    private void calculerCoutTotal() {
        if (getCoutUnitaire() == null) {
            setCoutTotal(BigDecimal.ZERO);
            return;
        }
        
        BigDecimal cout = getCoutUnitaire().multiply(BigDecimal.valueOf(getQuantite()));
        setCoutTotal(cout);
    }
    
    // Méthodes utilitaires
    public int getVariationStock() {
        return getStockApres() - getStockAvant();
    }
    
    public boolean isStockEnAugmentation() {
        return getVariationStock() > 0;
    }
    
    public boolean isStockEnDiminution() {
        return getVariationStock() < 0;
    }
    
    public String getResumeQuantite() {
        int variation = getVariationStock();
        String signe = variation >= 0 ? "+" : "";
        return String.format("%s%d", signe, variation);
    }
    
    public String getResumeStock() {
        return String.format("%d → %d", getStockAvant(), getStockApres());
    }
    
    public String getDisplayName() {
        return String.format("%s - %s (%s%d)",
                getReference(),
                getTypeMouvement() != null ? getTypeMouvement().getDisplayName() : "N/A",
                getVariationStock() >= 0 ? "+" : "",
                getVariationStock());
    }
    
    public String getFormattedDate() {
        if (getDateMouvement() == null) return "";
        return getDateMouvement().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
    
    public boolean concerneDocument(String typeDoc, long docId) {
        return typeDoc.equals(getDocumentType()) && docId == getDocumentId();
    }
    
    // Getters et setters pour les propriétés
    public long getId() { return id.get(); }
    public void setId(long id) { this.id.set(id); }
    public LongProperty idProperty() { return id; }
    
    public long getProduitId() { return produitId.get(); }
    public void setProduitId(long produitId) { this.produitId.set(produitId); }
    public LongProperty produitIdProperty() { return produitId; }
    
    public String getProduitNom() { return produitNom.get(); }
    public void setProduitNom(String produitNom) { this.produitNom.set(produitNom); }
    public StringProperty produitNomProperty() { return produitNom; }
    
    public String getProduitReference() { return produitReference.get(); }
    public void setProduitReference(String produitReference) { this.produitReference.set(produitReference); }
    public StringProperty produitReferenceProperty() { return produitReference; }
    
    public TypeMouvement getTypeMouvement() { return typeMouvement.get(); }
    public void setTypeMouvement(TypeMouvement typeMouvement) { this.typeMouvement.set(typeMouvement); }
    public ObjectProperty<TypeMouvement> typeMouvementProperty() { return typeMouvement; }
    
    public int getQuantite() { return quantite.get(); }
    public void setQuantite(int quantite) { this.quantite.set(quantite); }
    public IntegerProperty quantiteProperty() { return quantite; }
    
    public int getStockAvant() { return stockAvant.get(); }
    public void setStockAvant(int stockAvant) { this.stockAvant.set(stockAvant); }
    public IntegerProperty stockAvantProperty() { return stockAvant; }
    
    public int getStockApres() { return stockApres.get(); }
    public void setStockApres(int stockApres) { this.stockApres.set(stockApres); }
    public IntegerProperty stockApresProperty() { return stockApres; }
    
    public String getReference() { return reference.get(); }
    public void setReference(String reference) { this.reference.set(reference); }
    public StringProperty referenceProperty() { return reference; }
    
    public long getDocumentId() { return documentId.get(); }
    public void setDocumentId(long documentId) { this.documentId.set(documentId); }
    public LongProperty documentIdProperty() { return documentId; }
    
    public String getDocumentType() { return documentType.get(); }
    public void setDocumentType(String documentType) { this.documentType.set(documentType); }
    public StringProperty documentTypeProperty() { return documentType; }
    
    public BigDecimal getCoutUnitaire() { return coutUnitaire.get(); }
    public void setCoutUnitaire(BigDecimal coutUnitaire) { this.coutUnitaire.set(coutUnitaire); }
    public ObjectProperty<BigDecimal> coutUnitaireProperty() { return coutUnitaire; }
    
    public BigDecimal getCoutTotal() { return coutTotal.get(); }
    public void setCoutTotal(BigDecimal coutTotal) { this.coutTotal.set(coutTotal); }
    public ObjectProperty<BigDecimal> coutTotalProperty() { return coutTotal; }
    
    public String getCommentaires() { return commentaires.get(); }
    public void setCommentaires(String commentaires) { this.commentaires.set(commentaires); }
    public StringProperty commentairesProperty() { return commentaires; }
    
    public String getEmplacement() { return emplacement.get(); }
    public void setEmplacement(String emplacement) { this.emplacement.set(emplacement); }
    public StringProperty emplacementProperty() { return emplacement; }
    
    public String getNumeroLot() { return numeroLot.get(); }
    public void setNumeroLot(String numeroLot) { this.numeroLot.set(numeroLot); }
    public StringProperty numeroLotProperty() { return numeroLot; }
    
    public LocalDateTime getDateMouvement() { return dateMouvement.get(); }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement.set(dateMouvement); }
    public ObjectProperty<LocalDateTime> dateMouvementProperty() { return dateMouvement; }
    
    public long getUtilisateurId() { return utilisateurId.get(); }
    public void setUtilisateurId(long utilisateurId) { this.utilisateurId.set(utilisateurId); }
    public LongProperty utilisateurIdProperty() { return utilisateurId; }
    
    public String getUtilisateurNom() { return utilisateurNom.get(); }
    public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom.set(utilisateurNom); }
    public StringProperty utilisateurNomProperty() { return utilisateurNom; }
    
    @Override
    public String toString() {
        return String.format("MouvementStock{id=%d, produit='%s', type=%s, quantite=%s%d, stock=%d→%d}",
                getId(), getProduitNom(), 
                getTypeMouvement() != null ? getTypeMouvement().getDisplayName() : "N/A",
                getVariationStock() >= 0 ? "+" : "",
                getVariationStock(), getStockAvant(), getStockApres());
    }
}