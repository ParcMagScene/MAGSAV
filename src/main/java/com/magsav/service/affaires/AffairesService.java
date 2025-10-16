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
 * Service de gestion des affaires commerciales
 * Fournit toutes les opérations CRUD et logiques métier pour les affaires
 */
public class AffairesService {
    
    private static final Logger logger = LoggerFactory.getLogger(AffairesService.class);
    private static AffairesService instance;
    
    private AffairesService() {
        initialiserTables();
    }
    
    public static synchronized AffairesService getInstance() {
        if (instance == null) {
            instance = new AffairesService();
        }
        return instance;
    }
    
    /**
     * Les tables sont maintenant définies centralement dans H2DB.java
     * Cette méthode ne fait plus rien et sera supprimée ultérieurement
     */
    private void initialiserTables() {
        // Tables déjà créées dans H2DB.java - plus besoin de duplication
    }
    
    // === GESTION DES AFFAIRES ===
    
    /**
     * Crée une nouvelle affaire
     */
    public Long creerAffaire(Affaire affaire) {
        String sql = """
            INSERT INTO affaires (reference, nom, description, client_id, client_nom, statut, type, priorite,
                                 montant_estime, devise_code, date_creation, date_echeance, commercial_responsable,
                                 technicien_responsable, chef_projet, notes, commentaires_internes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, affaire.getReference());
            stmt.setString(2, affaire.getNom());
            stmt.setString(3, affaire.getDescription());
            stmt.setObject(4, affaire.getClientId());
            stmt.setString(5, affaire.getClientNom());
            stmt.setString(6, affaire.getStatut().name());
            stmt.setString(7, affaire.getType() != null ? affaire.getType().name() : null);
            stmt.setString(8, affaire.getPriorite().name());
            stmt.setObject(9, affaire.getMontantEstime());
            stmt.setString(10, affaire.getDeviseCode());
            stmt.setDate(11, Date.valueOf(affaire.getDateCreation()));
            stmt.setDate(12, affaire.getDateEcheance() != null ? Date.valueOf(affaire.getDateEcheance()) : null);
            stmt.setString(13, affaire.getCommercialResponsable());
            stmt.setString(14, affaire.getTechnicienResponsable());
            stmt.setString(15, affaire.getChefProjet());
            stmt.setString(16, affaire.getNotes());
            stmt.setString(17, affaire.getCommentairesInternes());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        Long id = keys.getLong(1);
                        affaire.setId(id);
                        logger.info("Affaire créée avec succès: {}", affaire.getReference());
                        return id;
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'affaire: " + affaire.getReference(), e);
        }
        
        return null;
    }
    
    /**
     * Met à jour une affaire existante
     */
    public boolean mettreAJourAffaire(Affaire affaire) {
        String sql = """
            UPDATE affaires SET 
                nom = ?, description = ?, client_id = ?, client_nom = ?, statut = ?, type = ?, priorite = ?,
                montant_estime = ?, montant_reel = ?, taux_marge = ?, devise_code = ?, date_echeance = ?,
                date_fermeture = ?, commercial_responsable = ?, technicien_responsable = ?, chef_projet = ?,
                notes = ?, commentaires_internes = ?, derniere_mise_a_jour = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, affaire.getNom());
            stmt.setString(2, affaire.getDescription());
            stmt.setObject(3, affaire.getClientId());
            stmt.setString(4, affaire.getClientNom());
            stmt.setString(5, affaire.getStatut().name());
            stmt.setString(6, affaire.getType() != null ? affaire.getType().name() : null);
            stmt.setString(7, affaire.getPriorite().name());
            stmt.setObject(8, affaire.getMontantEstime());
            stmt.setObject(9, affaire.getMontantReel());
            stmt.setObject(10, affaire.getTauxMarge());
            stmt.setString(11, affaire.getDeviseCode());
            stmt.setDate(12, affaire.getDateEcheance() != null ? Date.valueOf(affaire.getDateEcheance()) : null);
            stmt.setDate(13, affaire.getDateFermeture() != null ? Date.valueOf(affaire.getDateFermeture()) : null);
            stmt.setString(14, affaire.getCommercialResponsable());
            stmt.setString(15, affaire.getTechnicienResponsable());
            stmt.setString(16, affaire.getChefProjet());
            stmt.setString(17, affaire.getNotes());
            stmt.setString(18, affaire.getCommentairesInternes());
            stmt.setLong(19, affaire.getId());
            
            int result = stmt.executeUpdate();
            if (result > 0) {
                logger.info("Affaire mise à jour: {}", affaire.getReference());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'affaire: " + affaire.getId(), e);
        }
        
        return false;
    }
    
    /**
     * Récupère une affaire par son ID
     */
    public Optional<Affaire> obtenirAffaire(Long id) {
        String sql = "SELECT * FROM affaires WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapperAffaire(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de l'affaire: " + id, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère toutes les affaires
     */
    public List<Affaire> obtenirToutesLesAffaires() {
        return obtenirAffairesAvecFiltres(null, null, null);
    }
    
    /**
     * Récupère les affaires avec des filtres
     */
    public List<Affaire> obtenirAffairesAvecFiltres(String statut, String priorite, Long clientId) {
        StringBuilder sql = new StringBuilder("SELECT * FROM affaires WHERE 1=1");
        List<Object> parametres = new ArrayList<>();
        
        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND statut = ?");
            parametres.add(statut);
        }
        
        if (priorite != null && !priorite.isEmpty()) {
            sql.append(" AND priorite = ?");
            parametres.add(priorite);
        }
        
        if (clientId != null) {
            sql.append(" AND client_id = ?");
            parametres.add(clientId);
        }
        
        sql.append(" ORDER BY derniere_mise_a_jour DESC");
        
        List<Affaire> affaires = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < parametres.size(); i++) {
                stmt.setObject(i + 1, parametres.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    affaires.add(mapperAffaire(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des affaires avec filtres", e);
        }
        
        return affaires;
    }
    
    /**
     * Supprime une affaire et tous ses devis associés
     */
    public boolean supprimerAffaire(Long id) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Supprimer d'abord les lignes de devis
                String deleteLignesDevis = """
                    DELETE FROM lignes_devis 
                    WHERE devis_id IN (SELECT id FROM devis WHERE affaire_id = ?)
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(deleteLignesDevis)) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
                
                // Supprimer les devis
                String deleteDevis = "DELETE FROM devis WHERE affaire_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteDevis)) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
                
                // Supprimer l'affaire
                String deleteAffaire = "DELETE FROM affaires WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteAffaire)) {
                    stmt.setLong(1, id);
                    int result = stmt.executeUpdate();
                    
                    if (result > 0) {
                        conn.commit();
                        logger.info("Affaire supprimée avec succès: {}", id);
                        return true;
                    }
                }
                
                conn.rollback();
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'affaire: " + id, e);
        }
        
        return false;
    }
    
    // === MÉTHODES UTILITAIRES ===
    
    /**
     * Mappe un ResultSet vers une Affaire
     */
    private Affaire mapperAffaire(ResultSet rs) throws SQLException {
        Affaire affaire = new Affaire();
        
        affaire.setId(rs.getLong("id"));
        affaire.setReference(rs.getString("reference"));
        affaire.setNom(rs.getString("nom"));
        affaire.setDescription(rs.getString("description"));
        
        Long clientId = rs.getObject("client_id", Long.class);
        affaire.setClientId(clientId);
        affaire.setClientNom(rs.getString("client_nom"));
        
        // Conversion des enums avec gestion des erreurs
        try {
            String statutStr = rs.getString("statut");
            if (statutStr != null) {
                affaire.setStatut(StatutAffaire.valueOf(statutStr));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Statut inconnu pour l'affaire {}: {}", affaire.getId(), rs.getString("statut"));
        }
        
        try {
            String typeStr = rs.getString("type");
            if (typeStr != null) {
                affaire.setType(TypeAffaire.valueOf(typeStr));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Type inconnu pour l'affaire {}: {}", affaire.getId(), rs.getString("type"));
        }
        
        try {
            String prioriteStr = rs.getString("priorite");
            if (prioriteStr != null) {
                affaire.setPriorite(PrioriteAffaire.valueOf(prioriteStr));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Priorité inconnue pour l'affaire {}: {}", affaire.getId(), rs.getString("priorite"));
        }
        
        affaire.setMontantEstime(rs.getObject("montant_estime", Double.class));
        affaire.setMontantReel(rs.getObject("montant_reel", Double.class));
        affaire.setTauxMarge(rs.getObject("taux_marge", Double.class));
        affaire.setDeviseCode(rs.getString("devise_code"));
        
        // Dates
        Date dateCreation = rs.getDate("date_creation");
        if (dateCreation != null) {
            affaire.setDateCreation(dateCreation.toLocalDate());
        }
        
        Date dateEcheance = rs.getDate("date_echeance");
        if (dateEcheance != null) {
            affaire.setDateEcheance(dateEcheance.toLocalDate());
        }
        
        Date dateFermeture = rs.getDate("date_fermeture");
        if (dateFermeture != null) {
            affaire.setDateFermeture(dateFermeture.toLocalDate());
        }
        
        Timestamp derniereMiseAJour = rs.getTimestamp("derniere_mise_a_jour");
        if (derniereMiseAJour != null) {
            affaire.setDerniereMiseAJour(derniereMiseAJour.toLocalDateTime());
        }
        
        affaire.setCommercialResponsable(rs.getString("commercial_responsable"));
        affaire.setTechnicienResponsable(rs.getString("technicien_responsable"));
        affaire.setChefProjet(rs.getString("chef_projet"));
        affaire.setNotes(rs.getString("notes"));
        affaire.setCommentairesInternes(rs.getString("commentaires_internes"));
        
        return affaire;
    }
    
    /**
     * Génère une référence unique pour une nouvelle affaire
     */
    public String genererReferenceAffaire() {
        String reference;
        int tentatives = 0;
        final int MAX_TENTATIVES = 100;
        
        do {
            // Générer un nombre aléatoire de 5 chiffres
            int numero = new java.util.Random().nextInt(100000);
            reference = String.format("AF%05d", numero);
            tentatives++;
            
            // Vérifier l'unicité
            if (!referenceExiste(reference)) {
                logger.debug("Référence d'affaire générée: {}", reference);
                return reference;
            }
            
        } while (tentatives < MAX_TENTATIVES);
        
        // Si on n'arrive pas à générer une référence unique, utiliser timestamp
        long timestamp = System.currentTimeMillis() % 100000;
        reference = String.format("AF%05d", timestamp);
        
        logger.warn("Utilisation d'une référence basée sur timestamp: {}", reference);
        return reference;
    }
    
    /**
     * Vérifie si une référence d'affaire existe déjà
     */
    private boolean referenceExiste(String reference) {
        String sql = "SELECT COUNT(*) FROM affaires WHERE reference = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, reference);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification de référence: " + reference, e);
            return true; // En cas d'erreur, considérer que la référence existe pour éviter les doublons
        }
    }
    
    /**
     * Obtient les statistiques des affaires
     */
    public StatistiquesAffaires obtenirStatistiques() {
        String sql = """
            SELECT 
                statut,
                COUNT(*) as nombre,
                COALESCE(SUM(montant_estime), 0) as montant_total_estime,
                COALESCE(SUM(montant_reel), 0) as montant_total_reel
            FROM affaires 
            GROUP BY statut
            """;
        
        StatistiquesAffaires stats = new StatistiquesAffaires();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String statut = rs.getString("statut");
                int nombre = rs.getInt("nombre");
                double montantEstime = rs.getDouble("montant_total_estime");
                double montantReel = rs.getDouble("montant_total_reel");
                
                stats.ajouterStatutStats(statut, nombre, montantEstime, montantReel);
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul des statistiques d'affaires", e);
        }
        
        return stats;
    }
    
    /**
     * Classe pour les statistiques d'affaires
     */
    public static class StatistiquesAffaires {
        private int totalAffaires = 0;
        private double totalMontantEstime = 0.0;
        private double totalMontantReel = 0.0;
        private final java.util.Map<String, StatutStats> statistiquesParStatut = new java.util.HashMap<>();
        
        public void ajouterStatutStats(String statut, int nombre, double montantEstime, double montantReel) {
            statistiquesParStatut.put(statut, new StatutStats(nombre, montantEstime, montantReel));
            totalAffaires += nombre;
            totalMontantEstime += montantEstime;
            totalMontantReel += montantReel;
        }
        
        // Getters
        public int getTotalAffaires() { return totalAffaires; }
        public double getTotalMontantEstime() { return totalMontantEstime; }
        public double getTotalMontantReel() { return totalMontantReel; }
        public java.util.Map<String, StatutStats> getStatistiquesParStatut() { return statistiquesParStatut; }
        
        public static class StatutStats {
            private final int nombre;
            private final double montantEstime;
            private final double montantReel;
            
            public StatutStats(int nombre, double montantEstime, double montantReel) {
                this.nombre = nombre;
                this.montantEstime = montantEstime;
                this.montantReel = montantReel;
            }
            
            public int getNombre() { return nombre; }
            public double getMontantEstime() { return montantEstime; }
            public double getMontantReel() { return montantReel; }
        }
    }
}