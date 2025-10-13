package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Commande;
import com.magsav.model.LigneCommande;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository pour la gestion des commandes fournisseurs
 */
public class CommandeRepository {

    // CRUD de base pour les commandes
    
    public ObservableList<Commande> findAll() {
        ObservableList<Commande> commandes = FXCollections.observableArrayList();
        
        String query = """
            SELECT c.*, 
                   f.nom as fournisseur_nom,
                   u.nom as utilisateur_nom
            FROM commandes c
            LEFT JOIN fournisseurs f ON c.fournisseur_id = f.id
            LEFT JOIN users u ON c.utilisateur_id = u.id
            ORDER BY c.date_creation DESC
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Commande commande = mapRowToCommande(rs);
                // Charger les lignes de commande
                chargerLignesCommande(commande);
                commandes.add(commande);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des commandes : " + e.getMessage());
        }
        
        return commandes;
    }
    
    public Commande findById(long id) {
        String query = """
            SELECT c.*, 
                   f.nom as fournisseur_nom,
                   u.nom as utilisateur_nom
            FROM commandes c
            LEFT JOIN fournisseurs f ON c.fournisseur_id = f.id
            LEFT JOIN users u ON c.utilisateur_id = u.id
            WHERE c.id = ?
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Commande commande = mapRowToCommande(rs);
                    chargerLignesCommande(commande);
                    return commande;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la commande : " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean save(Commande commande) {
        if (commande.getId() == 0) {
            return insert(commande);
        } else {
            return update(commande);
        }
    }
    
    private boolean insert(Commande commande) {
        String query = """
            INSERT INTO commandes (
                numero_commande, fournisseur_id, statut, type_commande, 
                date_commande, date_livraison_prevue, date_livraison_reelle,
                montant_ht, montant_tva, montant_ttc, commentaires,
                numero_facture_fournisseur, adresse_livraison, contact_livraison,
                transporteur, numero_suivi, reception_complete, facture_recue,
                utilisateur_id, date_creation, date_modification
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            int index = 1;
            stmt.setString(index++, commande.getNumeroCommande());
            stmt.setLong(index++, commande.getFournisseurId());
            stmt.setString(index++, commande.getStatut().name());
            stmt.setString(index++, commande.getType().name());
            stmt.setDate(index++, commande.getDateCommande() != null ? Date.valueOf(commande.getDateCommande()) : null);
            stmt.setDate(index++, commande.getDateLivraisonPrevue() != null ? Date.valueOf(commande.getDateLivraisonPrevue()) : null);
            stmt.setDate(index++, commande.getDateLivraisonReelle() != null ? Date.valueOf(commande.getDateLivraisonReelle()) : null);
            stmt.setBigDecimal(index++, commande.getMontantHT());
            stmt.setBigDecimal(index++, commande.getMontantTVA());
            stmt.setBigDecimal(index++, commande.getMontantTTC());
            stmt.setString(index++, commande.getCommentaires());
            stmt.setString(index++, commande.getNumeroFactureFournisseur());
            stmt.setString(index++, commande.getAdresseLivraison());
            stmt.setString(index++, commande.getContactLivraison());
            stmt.setString(index++, commande.getTransporteur());
            stmt.setString(index++, commande.getNumeroSuivi());
            stmt.setBoolean(index++, commande.isReceptionComplete());
            stmt.setBoolean(index++, commande.isFactureRecue());
            stmt.setLong(index++, commande.getUtilisateurId());
            stmt.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        commande.setId(generatedKeys.getLong(1));
                        
                        // Sauvegarder les lignes de commande
                        for (LigneCommande ligne : commande.getLignes()) {
                            ligne.setCommandeId(commande.getId());
                            saveLigneCommande(ligne);
                        }
                        
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la commande : " + e.getMessage());
        }
        
        return false;
    }
    
    private boolean update(Commande commande) {
        String query = """
            UPDATE commandes SET
                numero_commande = ?, fournisseur_id = ?, statut = ?, type_commande = ?,
                date_commande = ?, date_livraison_prevue = ?, date_livraison_reelle = ?,
                montant_ht = ?, montant_tva = ?, montant_ttc = ?, commentaires = ?,
                numero_facture_fournisseur = ?, adresse_livraison = ?, contact_livraison = ?,
                transporteur = ?, numero_suivi = ?, reception_complete = ?, facture_recue = ?,
                date_modification = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            int index = 1;
            stmt.setString(index++, commande.getNumeroCommande());
            stmt.setLong(index++, commande.getFournisseurId());
            stmt.setString(index++, commande.getStatut().name());
            stmt.setString(index++, commande.getType().name());
            stmt.setDate(index++, commande.getDateCommande() != null ? Date.valueOf(commande.getDateCommande()) : null);
            stmt.setDate(index++, commande.getDateLivraisonPrevue() != null ? Date.valueOf(commande.getDateLivraisonPrevue()) : null);
            stmt.setDate(index++, commande.getDateLivraisonReelle() != null ? Date.valueOf(commande.getDateLivraisonReelle()) : null);
            stmt.setBigDecimal(index++, commande.getMontantHT());
            stmt.setBigDecimal(index++, commande.getMontantTVA());
            stmt.setBigDecimal(index++, commande.getMontantTTC());
            stmt.setString(index++, commande.getCommentaires());
            stmt.setString(index++, commande.getNumeroFactureFournisseur());
            stmt.setString(index++, commande.getAdresseLivraison());
            stmt.setString(index++, commande.getContactLivraison());
            stmt.setString(index++, commande.getTransporteur());
            stmt.setString(index++, commande.getNumeroSuivi());
            stmt.setBoolean(index++, commande.isReceptionComplete());
            stmt.setBoolean(index++, commande.isFactureRecue());
            stmt.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(index++, commande.getId());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                // Mettre à jour les lignes de commande
                supprimerLignesCommande(commande.getId());
                for (LigneCommande ligne : commande.getLignes()) {
                    ligne.setCommandeId(commande.getId());
                    saveLigneCommande(ligne);
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la commande : " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean delete(long id) {
        try (Connection conn = DB.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Supprimer d'abord les lignes de commande
                supprimerLignesCommande(id);
                
                // Puis la commande
                String query = "DELETE FROM commandes WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setLong(1, id);
                    int result = stmt.executeUpdate();
                    
                    conn.commit();
                    return result > 0;
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la commande : " + e.getMessage());
        }
        
        return false;
    }
    
    // Méthodes pour les lignes de commande
    
    private void chargerLignesCommande(Commande commande) {
        String query = """
            SELECT lc.*, p.nom as produit_nom, p.reference as produit_reference
            FROM lignes_commandes lc
            LEFT JOIN products p ON lc.produit_id = p.id
            WHERE lc.commande_id = ?
            ORDER BY lc.id
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, commande.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LigneCommande ligne = mapRowToLigneCommande(rs);
                    ligne.setCommande(commande);
                    commande.getLignes().add(ligne);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des lignes de commande : " + e.getMessage());
        }
    }
    
    private boolean saveLigneCommande(LigneCommande ligne) {
        String query = """
            INSERT INTO lignes_commandes (
                commande_id, produit_id, quantite_commandee, quantite_recue,
                prix_unitaire_ht, taux_tva, montant_ht, montant_tva, montant_ttc,
                description, unite, commentaires, statut_reception,
                date_creation, date_modification
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            int index = 1;
            stmt.setLong(index++, ligne.getCommandeId());
            stmt.setLong(index++, ligne.getProduitId());
            stmt.setInt(index++, ligne.getQuantiteCommandee());
            stmt.setInt(index++, ligne.getQuantiteRecue());
            stmt.setBigDecimal(index++, ligne.getPrixUnitaireHT());
            stmt.setDouble(index++, ligne.getTauxTVA());
            stmt.setBigDecimal(index++, ligne.getMontantHT());
            stmt.setBigDecimal(index++, ligne.getMontantTVA());
            stmt.setBigDecimal(index++, ligne.getMontantTTC());
            stmt.setString(index++, ligne.getDescription());
            stmt.setString(index++, ligne.getUnite());
            stmt.setString(index++, ligne.getCommentaires());
            stmt.setString(index++, ligne.getStatutReception().name());
            stmt.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ligne.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la sauvegarde de la ligne de commande : " + e.getMessage());
        }
        
        return false;
    }
    
    private void supprimerLignesCommande(long commandeId) {
        String query = "DELETE FROM lignes_commandes WHERE commande_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, commandeId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression des lignes de commande : " + e.getMessage());
        }
    }
    
    // Méthodes de recherche et filtrage
    
    public ObservableList<Commande> search(String searchTerm) {
        ObservableList<Commande> commandes = FXCollections.observableArrayList();
        
        String query = """
            SELECT c.*, 
                   f.nom as fournisseur_nom,
                   u.nom as utilisateur_nom
            FROM commandes c
            LEFT JOIN fournisseurs f ON c.fournisseur_id = f.id
            LEFT JOIN users u ON c.utilisateur_id = u.id
            WHERE LOWER(c.numero_commande) LIKE LOWER(?) 
               OR LOWER(f.nom) LIKE LOWER(?)
               OR LOWER(c.commentaires) LIKE LOWER(?)
            ORDER BY c.date_creation DESC
            """;
        
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commande commande = mapRowToCommande(rs);
                    chargerLignesCommande(commande);
                    commandes.add(commande);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de commandes : " + e.getMessage());
        }
        
        return commandes;
    }
    
    public ObservableList<Commande> findByStatut(Commande.StatutCommande statut) {
        ObservableList<Commande> commandes = FXCollections.observableArrayList();
        
        String query = """
            SELECT c.*, 
                   f.nom as fournisseur_nom,
                   u.nom as utilisateur_nom
            FROM commandes c
            LEFT JOIN fournisseurs f ON c.fournisseur_id = f.id
            LEFT JOIN users u ON c.utilisateur_id = u.id
            WHERE c.statut = ?
            ORDER BY c.date_creation DESC
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, statut.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commande commande = mapRowToCommande(rs);
                    chargerLignesCommande(commande);
                    commandes.add(commande);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par statut : " + e.getMessage());
        }
        
        return commandes;
    }
    
    public ObservableList<Commande> findByFournisseur(long fournisseurId) {
        ObservableList<Commande> commandes = FXCollections.observableArrayList();
        
        String query = """
            SELECT c.*, 
                   f.nom as fournisseur_nom,
                   u.nom as utilisateur_nom
            FROM commandes c
            LEFT JOIN fournisseurs f ON c.fournisseur_id = f.id
            LEFT JOIN users u ON c.utilisateur_id = u.id
            WHERE c.fournisseur_id = ?
            ORDER BY c.date_creation DESC
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, fournisseurId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Commande commande = mapRowToCommande(rs);
                    chargerLignesCommande(commande);
                    commandes.add(commande);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par fournisseur : " + e.getMessage());
        }
        
        return commandes;
    }
    
    public ObservableList<Commande> findCommandesEnAttenteLivraison() {
        return findByStatut(Commande.StatutCommande.EXPEDIE);
    }
    
    // Méthodes de statistiques
    
    public Map<String, Integer> getStatistiquesParStatut() {
        Map<String, Integer> stats = new HashMap<>();
        
        String query = "SELECT statut, COUNT(*) as count FROM commandes GROUP BY statut";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String statut = rs.getString("statut");
                int count = rs.getInt("count");
                stats.put(statut, count);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des statistiques : " + e.getMessage());
        }
        
        return stats;
    }
    
    public BigDecimal getMontantTotalCommandes(LocalDate dateDebut, LocalDate dateFin) {
        String query = """
            SELECT SUM(montant_ttc) as total
            FROM commandes 
            WHERE date_commande BETWEEN ? AND ?
              AND statut != 'ANNULEE'
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setDate(1, Date.valueOf(dateDebut));
            stmt.setDate(2, Date.valueOf(dateFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du montant total : " + e.getMessage());
        }
        
        return BigDecimal.ZERO;
    }
    
    // Validation et vérifications
    
    public boolean existsByNumero(String numeroCommande, Long excludeId) {
        String query = "SELECT COUNT(*) FROM commandes WHERE numero_commande = ?" + 
                      (excludeId != null ? " AND id != ?" : "");
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, numeroCommande);
            if (excludeId != null) {
                stmt.setLong(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du numéro : " + e.getMessage());
        }
        
        return false;
    }
    
    // Méthodes de mapping
    
    private Commande mapRowToCommande(ResultSet rs) throws SQLException {
        Commande commande = new Commande();
        
        commande.setId(rs.getLong("id"));
        commande.setNumeroCommande(rs.getString("numero_commande"));
        commande.setFournisseurId(rs.getLong("fournisseur_id"));
        commande.setFournisseurNom(rs.getString("fournisseur_nom"));
        
        String statut = rs.getString("statut");
        if (statut != null) {
            commande.setStatut(Commande.StatutCommande.valueOf(statut));
        }
        
        String type = rs.getString("type_commande");
        if (type != null) {
            commande.setType(Commande.TypeCommande.valueOf(type));
        }
        
        Date dateCommande = rs.getDate("date_commande");
        if (dateCommande != null) {
            commande.setDateCommande(dateCommande.toLocalDate());
        }
        
        Date dateLivraisonPrevue = rs.getDate("date_livraison_prevue");
        if (dateLivraisonPrevue != null) {
            commande.setDateLivraisonPrevue(dateLivraisonPrevue.toLocalDate());
        }
        
        Date dateLivraisonReelle = rs.getDate("date_livraison_reelle");
        if (dateLivraisonReelle != null) {
            commande.setDateLivraisonReelle(dateLivraisonReelle.toLocalDate());
        }
        
        commande.setMontantHT(rs.getBigDecimal("montant_ht"));
        commande.setMontantTVA(rs.getBigDecimal("montant_tva"));
        commande.setMontantTTC(rs.getBigDecimal("montant_ttc"));
        
        commande.setCommentaires(rs.getString("commentaires"));
        commande.setNumeroFactureFournisseur(rs.getString("numero_facture_fournisseur"));
        commande.setAdresseLivraison(rs.getString("adresse_livraison"));
        commande.setContactLivraison(rs.getString("contact_livraison"));
        commande.setTransporteur(rs.getString("transporteur"));
        commande.setNumeroSuivi(rs.getString("numero_suivi"));
        commande.setReceptionComplete(rs.getBoolean("reception_complete"));
        commande.setFactureRecue(rs.getBoolean("facture_recue"));
        
        commande.setUtilisateurId(rs.getLong("utilisateur_id"));
        commande.setUtilisateurNom(rs.getString("utilisateur_nom"));
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            commande.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            commande.setDateModification(dateModification.toLocalDateTime());
        }
        
        return commande;
    }
    
    private LigneCommande mapRowToLigneCommande(ResultSet rs) throws SQLException {
        LigneCommande ligne = new LigneCommande();
        
        ligne.setId(rs.getLong("id"));
        ligne.setCommandeId(rs.getLong("commande_id"));
        ligne.setProduitId(rs.getLong("produit_id"));
        ligne.setProduitNom(rs.getString("produit_nom"));
        ligne.setProduitReference(rs.getString("produit_reference"));
        
        ligne.setQuantiteCommandee(rs.getInt("quantite_commandee"));
        ligne.setQuantiteRecue(rs.getInt("quantite_recue"));
        ligne.setPrixUnitaireHT(rs.getBigDecimal("prix_unitaire_ht"));
        ligne.setTauxTVA(rs.getDouble("taux_tva"));
        
        ligne.setMontantHT(rs.getBigDecimal("montant_ht"));
        ligne.setMontantTVA(rs.getBigDecimal("montant_tva"));
        ligne.setMontantTTC(rs.getBigDecimal("montant_ttc"));
        
        ligne.setDescription(rs.getString("description"));
        ligne.setUnite(rs.getString("unite"));
        ligne.setCommentaires(rs.getString("commentaires"));
        
        String statutReception = rs.getString("statut_reception");
        if (statutReception != null) {
            ligne.setStatutReception(LigneCommande.StatutReception.valueOf(statutReception));
        }
        
        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            ligne.setDateCreation(dateCreation.toLocalDateTime());
        }
        
        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            ligne.setDateModification(dateModification.toLocalDateTime());
        }
        
        return ligne;
    }
    
    /**
     * Récupère les lignes d'une commande
     */
    public ObservableList<LigneCommande> findLignesCommande(long commandeId) {
        ObservableList<LigneCommande> lignes = FXCollections.observableArrayList();
        
        String query = """
            SELECT lc.*, 
                   p.nom as produit_nom, 
                   p.reference as produit_reference
            FROM lignes_commande lc
            LEFT JOIN produits p ON lc.produit_id = p.id
            WHERE lc.commande_id = ?
            ORDER BY lc.id
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setLong(1, commandeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LigneCommande ligne = mapRowToLigneCommande(rs);
                    lignes.add(ligne);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des lignes de commande : " + e.getMessage());
        }
        
        return lignes;
    }
}