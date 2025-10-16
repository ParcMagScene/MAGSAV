package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.model.DemandeIntervention;
import com.magsav.model.DemandeIntervention.StatutDemande;
import com.magsav.model.DemandeIntervention.TypeDemande;
import com.magsav.model.DemandeIntervention.TypeProprietaire;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemandeInterventionRepository {
    
    /**
     * Crée une nouvelle demande d'intervention
     */
    public long createDemande(DemandeIntervention demande) {
        String sql = """
            INSERT INTO demandes_intervention (
                statut, type_demande, product_id,
                produit_nom, produit_sn, produit_uid, produit_fabricant,
                produit_category, produit_subcategory, produit_description,
                type_proprietaire, proprietaire_id, demande_creation_proprietaire_id,
                proprietaire_nom_temp, proprietaire_details_temp,
                panne_description, client_note, detecteur, detector_societe_id,
                demandeur_nom
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, demande.statut().getCode());
            ps.setString(2, demande.typeDemande().getCode());
            setLongOrNull(ps, 3, demande.productId());
            
            ps.setString(4, demande.produitNom());
            ps.setString(5, demande.produitSn());
            ps.setString(6, demande.produitUid());
            ps.setString(7, demande.produitFabricant());
            ps.setString(8, demande.produitCategory());
            ps.setString(9, demande.produitSubcategory());
            ps.setString(10, demande.produitDescription());
            
            // Informations propriétaire
            ps.setString(11, demande.typeProprietaire() != null ? demande.typeProprietaire().getCode() : null);
            setLongOrNull(ps, 12, demande.proprietaireId());
            setLongOrNull(ps, 13, demande.demandeCreationProprietaireId());
            ps.setString(14, demande.proprietaireNomTemp());
            ps.setString(15, demande.proprietaireDetailsTemp());
            
            ps.setString(16, demande.panneDescription());
            ps.setString(17, demande.clientNote());
            ps.setString(18, demande.detecteur());
            setLongOrNull(ps, 19, demande.detectorSocieteId());
            
            ps.setString(20, demande.demandeurNom());
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : -1L;
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur création demande intervention", e);
        }
    }
    
    /**
     * Récupère toutes les demandes
     */
    public List<DemandeIntervention> findAll() {
        String sql = "SELECT * FROM demandes_intervention ORDER BY date_demande DESC";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            try (ResultSet rs = ps.executeQuery()) {
                List<DemandeIntervention> demandes = new ArrayList<>();
                while (rs.next()) {
                    demandes.add(mapDemande(rs));
                }
                return demandes;
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur récupération toutes demandes", e);
        }
    }

    /**
     * Récupère toutes les demandes avec un statut donné
     */
    public List<DemandeIntervention> findByStatut(StatutDemande statut) {
        String sql = "SELECT * FROM demandes_intervention WHERE statut = ? ORDER BY date_demande DESC";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, statut.getCode());
            
            try (ResultSet rs = ps.executeQuery()) {
                List<DemandeIntervention> demandes = new ArrayList<>();
                while (rs.next()) {
                    demandes.add(mapDemande(rs));
                }
                return demandes;
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur récupération demandes par statut", e);
        }
    }
    
    /**
     * Récupère une demande par son ID
     */
    public Optional<DemandeIntervention> findById(long id) {
        String sql = "SELECT * FROM demandes_intervention WHERE id = ?";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapDemande(rs)) : Optional.empty();
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur récupération demande par ID", e);
        }
    }
    
    /**
     * Valide une demande (change le statut et ajoute les infos de validation)
     */
    public boolean validerDemande(long demandeId, String validateurNom, String notesValidation, Long interventionId) {
        String sql = """
            UPDATE demandes_intervention 
            SET statut = 'validee', 
                date_validation = CURRENT_TIMESTAMP,
                validateur_nom = ?,
                notes_validation = ?,
                intervention_id = ?
            WHERE id = ?
        """;
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, validateurNom);
            ps.setString(2, notesValidation);
            setLongOrNull(ps, 3, interventionId);
            ps.setLong(4, demandeId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur validation demande", e);
        }
    }
    
    /**
     * Rejette une demande
     */
    public boolean rejeterDemande(long demandeId, String validateurNom, String notesValidation) {
        String sql = """
            UPDATE demandes_intervention 
            SET statut = 'rejetee', 
                date_validation = CURRENT_TIMESTAMP,
                validateur_nom = ?,
                notes_validation = ?
            WHERE id = ?
        """;
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, validateurNom);
            ps.setString(2, notesValidation);
            ps.setLong(3, demandeId);
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur rejet demande", e);
        }
    }
    
    /**
     * Récupère toutes les demandes en attente de validation
     */
    public List<DemandeIntervention> findDemandesEnAttente() {
        return findByStatut(StatutDemande.EN_ATTENTE);
    }
    
    /**
     * Vérifie si un produit existe par UID
     */
    public boolean produitExisteParUID(String uid) {
        if (uid == null || uid.trim().isEmpty()) return false;
        
        String sql = "SELECT COUNT(*) FROM produits WHERE uid = ?";
        
        try (Connection c = DB.getConnection(); 
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setString(1, uid.trim());
            
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur vérification existence produit", e);
        }
    }
    
    private DemandeIntervention mapDemande(ResultSet rs) throws SQLException {
        return new DemandeIntervention(
            rs.getLong("id"),
            
            StatutDemande.fromCode(rs.getString("statut")),
            TypeDemande.fromCode(rs.getString("type_demande")),
            
            getLongOrNull(rs, "product_id"),
            
            rs.getString("produit_nom"),
            rs.getString("produit_sn"),
            rs.getString("produit_uid"),
            rs.getString("produit_fabricant"),
            rs.getString("produit_category"),
            rs.getString("produit_subcategory"),
            rs.getString("produit_description"),
            
            // Informations propriétaire
            TypeProprietaire.fromCode(rs.getString("type_proprietaire")),
            getLongOrNull(rs, "proprietaire_id"),
            getLongOrNull(rs, "demande_creation_proprietaire_id"),
            rs.getString("proprietaire_nom_temp"),
            rs.getString("proprietaire_details_temp"),
            
            rs.getString("panne_description"),
            rs.getString("client_note"),
            rs.getString("detecteur"),
            getLongOrNull(rs, "detector_societe_id"),
            
            rs.getString("demandeur_nom"),
            rs.getString("date_demande"),
            rs.getString("date_validation"),
            rs.getString("validateur_nom"),
            rs.getString("notes_validation"),
            
            getLongOrNull(rs, "intervention_id")
        );
    }
    
    private void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }
    
    private Long getLongOrNull(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}