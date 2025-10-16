package com.magsav.service.affaires;

import com.magsav.db.DB;
import com.magsav.model.affaires.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des devis
 * Fournit toutes les opérations CRUD et logiques métier pour les devis
 */
public class DevisService {
    
    private static final Logger logger = LoggerFactory.getLogger(DevisService.class);
    private static DevisService instance;
    
    private DevisService() {
        // Les tables sont créées par AffairesService
    }
    
    public static synchronized DevisService getInstance() {
        if (instance == null) {
            instance = new DevisService();
        }
        return instance;
    }
    
    // === GESTION DES DEVIS ===
    
    /**
     * Crée un nouveau devis
     */
    public Long creerDevis(Devis devis) {
        String sql = """
            INSERT INTO devis (numero, affaire_id, client_id, client_nom, objet, description, statut, version,
                              montant_ht, taux_tva, montant_tva, montant_ttc, devise_code, date_creation,
                              date_validite, conditions_paiement, delai_livraison, validite, modalites_livraison,
                              commercial_redacteur)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, devis.getNumero());
            stmt.setObject(2, devis.getAffaireId());
            stmt.setLong(3, devis.getClientId());
            stmt.setString(4, devis.getClientNom());
            stmt.setString(5, devis.getObjet());
            stmt.setString(6, devis.getDescription());
            stmt.setString(7, devis.getStatut().name());
            stmt.setInt(8, devis.getVersion());
            stmt.setObject(9, devis.getMontantHT());
            stmt.setObject(10, devis.getTauxTVA());
            stmt.setObject(11, devis.getMontantTVA());
            stmt.setObject(12, devis.getMontantTTC());
            stmt.setString(13, devis.getDeviseCode());
            stmt.setDate(14, Date.valueOf(devis.getDateCreation()));
            stmt.setDate(15, devis.getDateValidite() != null ? Date.valueOf(devis.getDateValidite()) : null);
            stmt.setString(16, devis.getConditionsPaiement());
            stmt.setString(17, devis.getDelaiLivraison());
            stmt.setString(18, devis.getValidite());
            stmt.setString(19, devis.getModalitesLivraison());
            stmt.setString(20, devis.getCommercialRedacteur());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        Long id = keys.getLong(1);
                        devis.setId(id);
                        
                        // Sauvegarder les lignes de devis
                        sauvegarderLignesDevis(conn, id, devis.getLignes());
                        
                        logger.info("Devis créé avec succès: {}", devis.getNumero());
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la création du devis: " + devis.getNumero(), e);
        }
        
        return null;
    }
    
    /**
     * Met à jour un devis existant
     */
    public boolean mettreAJourDevis(Devis devis) {
        String sql = """
            UPDATE devis SET 
                affaire_id = ?, client_id = ?, client_nom = ?, objet = ?, description = ?, statut = ?, version = ?,
                montant_ht = ?, taux_tva = ?, montant_tva = ?, montant_ttc = ?, devise_code = ?, date_validite = ?,
                date_acceptation = ?, conditions_paiement = ?, delai_livraison = ?, validite = ?, modalites_livraison = ?,
                commercial_redacteur = ?, validateur = ?, derniere_mise_a_jour = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, devis.getAffaireId());
                stmt.setLong(2, devis.getClientId());
                stmt.setString(3, devis.getClientNom());
                stmt.setString(4, devis.getObjet());
                stmt.setString(5, devis.getDescription());
                stmt.setString(6, devis.getStatut().name());
                stmt.setInt(7, devis.getVersion());
                stmt.setObject(8, devis.getMontantHT());
                stmt.setObject(9, devis.getTauxTVA());
                stmt.setObject(10, devis.getMontantTVA());
                stmt.setObject(11, devis.getMontantTTC());
                stmt.setString(12, devis.getDeviseCode());
                stmt.setDate(13, devis.getDateValidite() != null ? Date.valueOf(devis.getDateValidite()) : null);
                stmt.setDate(14, devis.getDateAcceptation() != null ? Date.valueOf(devis.getDateAcceptation()) : null);
                stmt.setString(15, devis.getConditionsPaiement());
                stmt.setString(16, devis.getDelaiLivraison());
                stmt.setString(17, devis.getValidite());
                stmt.setString(18, devis.getModalitesLivraison());
                stmt.setString(19, devis.getCommercialRedacteur());
                stmt.setString(20, devis.getValidateur());
                stmt.setLong(21, devis.getId());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    // Supprimer les anciennes lignes
                    supprimerLignesDevis(conn, devis.getId());
                    
                    // Ajouter les nouvelles lignes
                    sauvegarderLignesDevis(conn, devis.getId(), devis.getLignes());
                    
                    conn.commit();
                    logger.info("Devis mis à jour: {}", devis.getNumero());
                    return true;
                }
                
                conn.rollback();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du devis: " + devis.getId(), e);
        }
        
        return false;
    }
    
    /**
     * Récupère un devis par son ID
     */
    public Optional<Devis> obtenirDevis(Long id) {
        String sql = "SELECT * FROM devis WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Devis devis = mapperDevis(rs);
                    devis.setLignes(chargerLignesDevis(conn, id));
                    return Optional.of(devis);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération du devis: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère tous les devis d'une affaire
     */
    public List<Devis> obtenirDevisParAffaire(Long affaireId) {
        String sql = "SELECT * FROM devis WHERE affaire_id = ? ORDER BY version DESC";
        
        List<Devis> devisList = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, affaireId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Devis devis = mapperDevis(rs);
                    devis.setLignes(chargerLignesDevis(conn, devis.getId()));
                    devisList.add(devis);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des devis pour l'affaire: " + affaireId, e);
        }
        
        return devisList;
    }
    
    /**
     * Récupère tous les devis avec filtres
     */
    public List<Devis> obtenirDevisAvecFiltres(String statut, Long clientId, LocalDate dateDebut, LocalDate dateFin) {
        StringBuilder sql = new StringBuilder("SELECT * FROM devis WHERE 1=1");
        List<Object> parametres = new ArrayList<>();
        
        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND statut = ?");
            parametres.add(statut);
        }
        
        if (clientId != null) {
            sql.append(" AND client_id = ?");
            parametres.add(clientId);
        }
        
        if (dateDebut != null) {
            sql.append(" AND date_creation >= ?");
            parametres.add(Date.valueOf(dateDebut));
        }
        
        if (dateFin != null) {
            sql.append(" AND date_creation <= ?");
            parametres.add(Date.valueOf(dateFin));
        }
        
        sql.append(" ORDER BY date_creation DESC");
        
        List<Devis> devisList = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametres.size(); i++) {
                stmt.setObject(i + 1, parametres.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Devis devis = mapperDevis(rs);
                    devis.setLignes(chargerLignesDevis(conn, devis.getId()));
                    devisList.add(devis);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des devis avec filtres", e);
        }
        
        return devisList;
    }
    
    /**
     * Supprime un devis et toutes ses lignes
     */
    public boolean supprimerDevis(Long id) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Supprimer les lignes
                supprimerLignesDevis(conn, id);
                
                // Supprimer le devis
                String deleteDevis = "DELETE FROM devis WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteDevis)) {
                    stmt.setLong(1, id);
                    int result = stmt.executeUpdate();
                    
                    if (result > 0) {
                        conn.commit();
                        logger.info("Devis supprimé avec succès: {}", id);
                        return true;
                    }
                }
                
                conn.rollback();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du devis: " + id, e);
        }
        
        return false;
    }
    
    /**
     * Crée une nouvelle version d'un devis
     */
    public Optional<Devis> creerNouvelleVersion(Long devisId) {
        Optional<Devis> devisOriginal = obtenirDevis(devisId);
        
        if (devisOriginal.isPresent()) {
            Devis nouveau = new Devis(devisOriginal.get());
            nouveau.setId(null);
            nouveau.setVersion(nouveau.getVersion() + 1);
            nouveau.setStatut(StatutDevis.BROUILLON);
            nouveau.setNumero(genererNumeroDevis(nouveau.getClientId()));
            nouveau.setDateCreation(LocalDate.now());
            nouveau.setDateValidite(LocalDate.now().plusDays(30)); // Validité par défaut
            nouveau.setDateAcceptation(null);
            
            Long nouvelleId = creerDevis(nouveau);
            if (nouvelleId != null) {
                return obtenirDevis(nouvelleId);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Accepte un devis
     */
    public boolean accepterDevis(Long devisId, String validateur) {
        String sql = "UPDATE devis SET statut = ?, date_acceptation = ?, validateur = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, StatutDevis.ACCEPTE.name());
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            stmt.setString(3, validateur);
            stmt.setLong(4, devisId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                logger.info("Devis accepté: {}", devisId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de l'acceptation du devis: " + devisId, e);
        }
        
        return false;
    }
    
    /**
     * Rejette un devis
     */
    public boolean rejeterDevis(Long devisId, String raison) {
        String sql = "UPDATE devis SET statut = ?, validateur = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, StatutDevis.REFUSE.name());
            stmt.setString(2, raison);
            stmt.setLong(3, devisId);
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                logger.info("Devis rejeté: {}", devisId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors du rejet du devis: " + devisId, e);
        }
        
        return false;
    }
    
    // === MÉTHODES PRIVÉES UTILITAIRES ===
    
    /**
     * Mappe un ResultSet vers un Devis
     */
    private Devis mapperDevis(ResultSet rs) throws SQLException {
        Devis devis = new Devis();
        
        devis.setId(rs.getLong("id"));
        devis.setNumero(rs.getString("numero"));
        devis.setAffaireId(rs.getObject("affaire_id", Long.class));
        devis.setClientId(rs.getLong("client_id"));
        devis.setClientNom(rs.getString("client_nom"));
        devis.setObjet(rs.getString("objet"));
        devis.setDescription(rs.getString("description"));
        devis.setVersion(rs.getInt("version"));
        
        // Conversion du statut avec gestion des erreurs
        try {
            String statutStr = rs.getString("statut");
            if (statutStr != null) {
                devis.setStatut(StatutDevis.valueOf(statutStr));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Statut inconnu pour le devis {}: {}", devis.getId(), rs.getString("statut"));
            devis.setStatut(StatutDevis.BROUILLON);
        }
        
        devis.setMontantHT(rs.getObject("montant_ht", Double.class));
        devis.setTauxTVA(rs.getObject("taux_tva", Double.class));
        devis.setMontantTVA(rs.getObject("montant_tva", Double.class));
        devis.setMontantTTC(rs.getObject("montant_ttc", Double.class));
        devis.setDeviseCode(rs.getString("devise_code"));
        
        // Dates
        Date dateCreation = rs.getDate("date_creation");
        if (dateCreation != null) {
            devis.setDateCreation(dateCreation.toLocalDate());
        }
        
        Date dateValidite = rs.getDate("date_validite");
        if (dateValidite != null) {
            devis.setDateValidite(dateValidite.toLocalDate());
        }
        
        Date dateAcceptation = rs.getDate("date_acceptation");
        if (dateAcceptation != null) {
            devis.setDateAcceptation(dateAcceptation.toLocalDate());
        }
        
        devis.setConditionsPaiement(rs.getString("conditions_paiement"));
        devis.setDelaiLivraison(rs.getString("delai_livraison"));
        devis.setValidite(rs.getString("validite"));
        devis.setModalitesLivraison(rs.getString("modalites_livraison"));
        devis.setCommercialRedacteur(rs.getString("commercial_redacteur"));
        devis.setValidateur(rs.getString("validateur"));
        
        return devis;
    }
    
    /**
     * Charge les lignes d'un devis
     */
    private List<LigneDevis> chargerLignesDevis(Connection conn, Long devisId) throws SQLException {
        String sql = "SELECT * FROM lignes_devis WHERE devis_id = ? ORDER BY ordre";
        List<LigneDevis> lignes = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, devisId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LigneDevis ligne = new LigneDevis();
                    ligne.setOrdre(rs.getInt("ordre"));
                    ligne.setDesignation(rs.getString("designation"));
                    ligne.setDescription(rs.getString("description"));
                    ligne.setReference(rs.getString("reference"));
                    ligne.setUnite(rs.getString("unite"));
                    ligne.setQuantite(rs.getDouble("quantite"));
                    ligne.setPrixUnitaireHT(rs.getDouble("prix_unitaire_ht"));
                    ligne.setTauxRemise(rs.getDouble("taux_remise"));
                    ligne.setMontantRemise(rs.getDouble("montant_remise"));
                    ligne.setMontantHT(rs.getDouble("montant_ht"));
                    lignes.add(ligne);
                }
            }
        }
        
        return lignes;
    }
    
    /**
     * Sauvegarde les lignes d'un devis
     */
    private void sauvegarderLignesDevis(Connection conn, Long devisId, List<LigneDevis> lignes) throws SQLException {
        String sql = """
            INSERT INTO lignes_devis (devis_id, ordre, designation, description, reference, unite,
                                     quantite, prix_unitaire_ht, taux_remise, montant_remise, montant_ht)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (LigneDevis ligne : lignes) {
                stmt.setLong(1, devisId);
                stmt.setInt(2, ligne.getOrdre());
                stmt.setString(3, ligne.getDesignation());
                stmt.setString(4, ligne.getDescription());
                stmt.setString(5, ligne.getReference());
                stmt.setString(6, ligne.getUnite());
                stmt.setDouble(7, ligne.getQuantite());
                stmt.setDouble(8, ligne.getPrixUnitaireHT());
                stmt.setDouble(9, ligne.getTauxRemise());
                stmt.setDouble(10, ligne.getMontantRemise());
                stmt.setDouble(11, ligne.getMontantHT());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
    
    /**
     * Supprime les lignes d'un devis
     */
    private void supprimerLignesDevis(Connection conn, Long devisId) throws SQLException {
        String sql = "DELETE FROM lignes_devis WHERE devis_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, devisId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Génère un numéro unique pour un nouveau devis
     */
    public String genererNumeroDevis(Long clientId) {
        String annee = String.valueOf(LocalDate.now().getYear());
        String prefix = "DEV-" + annee + "-";
        
        if (clientId != null) {
            prefix += String.format("C%03d-", clientId % 1000);
        }
        
        String sql = "SELECT COUNT(*) + 1 as numero FROM devis WHERE numero LIKE ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int numero = rs.getInt("numero");
                    return prefix + String.format("%04d", numero);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la génération de numéro de devis", e);
        }
        
        // Fallback
        return prefix + System.currentTimeMillis() % 10000;
    }
    
    /**
     * Obtient les statistiques des devis
     */
    public StatistiquesDevis obtenirStatistiques() {
        String sql = """
            SELECT 
                statut,
                COUNT(*) as nombre,
                COALESCE(SUM(montant_ttc), 0) as montant_total
            FROM devis 
            GROUP BY statut
            """;
        
        StatistiquesDevis stats = new StatistiquesDevis();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String statut = rs.getString("statut");
                int nombre = rs.getInt("nombre");
                double montant = rs.getDouble("montant_total");
                
                stats.ajouterStatutStats(statut, nombre, montant);
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul des statistiques de devis", e);
        }
        
        return stats;
    }
    
    /**
     * Classe pour les statistiques de devis
     */
    public static class StatistiquesDevis {
        private int totalDevis = 0;
        private double totalMontant = 0.0;
        private final java.util.Map<String, StatutStats> statistiquesParStatut = new java.util.HashMap<>();
        
        public void ajouterStatutStats(String statut, int nombre, double montant) {
            statistiquesParStatut.put(statut, new StatutStats(nombre, montant));
            totalDevis += nombre;
            totalMontant += montant;
        }
        
        // Getters
        public int getTotalDevis() { return totalDevis; }
        public double getTotalMontant() { return totalMontant; }
        public java.util.Map<String, StatutStats> getStatistiquesParStatut() { return statistiquesParStatut; }
        
        public static class StatutStats {
            private final int nombre;
            private final double montant;
            
            public StatutStats(int nombre, double montant) {
                this.nombre = nombre;
                this.montant = montant;
            }
            
            public int getNombre() { return nombre; }
            public double getMontant() { return montant; }
        }
    }
}