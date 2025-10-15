package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.model.DemandeCreationProprietaire;
import com.magsav.model.DemandeCreationProprietaire.TypeProprietaire;
import com.magsav.model.DemandeCreationProprietaire.StatutDemandeProprietaire;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des demandes de création de propriétaires
 */
public class DemandeCreationProprietaireRepository {
    
    /**
     * Insère une nouvelle demande de création de propriétaire
     */
    public long insert(DemandeCreationProprietaire demande) {
        String sql = """
            INSERT INTO demandes_creation_proprietaire 
            (nom, type_proprietaire, email, phone, adresse, notes, statut, created_by, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, demande.nom());
            stmt.setString(2, demande.typeProprietaire().name());
            stmt.setString(3, demande.email());
            stmt.setString(4, demande.phone());
            stmt.setString(5, demande.adresse());
            stmt.setString(6, demande.notes());
            stmt.setString(7, demande.statut().name().toLowerCase());
            stmt.setString(8, demande.createdBy());
            stmt.setString(9, demande.createdAt().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Échec de l'insertion de la demande de création de propriétaire");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new DatabaseException("Impossible de récupérer l'ID de la demande insérée");
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de l'insertion de la demande de création de propriétaire", e);
        }
    }
    
    /**
     * Récupère toutes les demandes en attente
     */
    public List<DemandeCreationProprietaire> findByStatut(StatutDemandeProprietaire statut) {
        String sql = """
            SELECT id, nom, type_proprietaire, email, phone, adresse, notes, 
                   statut, created_by, created_at, validated_at, validated_by
            FROM demandes_creation_proprietaire 
            WHERE statut = ?
            ORDER BY created_at DESC
        """;
        
        List<DemandeCreationProprietaire> demandes = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, statut.name().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    demandes.add(mapResultSetToEntity(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la récupération des demandes de création de propriétaire", e);
        }
        
        return demandes;
    }
    
    /**
     * Récupère une demande par son ID
     */
    public Optional<DemandeCreationProprietaire> findById(long id) {
        String sql = """
            SELECT id, nom, type_proprietaire, email, phone, adresse, notes, 
                   statut, created_by, created_at, validated_at, validated_by
            FROM demandes_creation_proprietaire 
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToEntity(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la récupération de la demande de création de propriétaire", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Met à jour le statut d'une demande
     */
    public void updateStatut(long id, StatutDemandeProprietaire nouveauStatut, String validatedBy) {
        String sql = """
            UPDATE demandes_creation_proprietaire 
            SET statut = ?, validated_at = ?, validated_by = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nouveauStatut.name().toLowerCase());
            stmt.setString(2, LocalDateTime.now().toString());
            stmt.setString(3, validatedBy);
            stmt.setLong(4, id);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Aucune demande trouvée avec l'ID: " + id);
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la mise à jour du statut de la demande", e);
        }
    }
    
    /**
     * Récupère toutes les demandes pour un utilisateur
     */
    public List<DemandeCreationProprietaire> findByCreatedBy(String createdBy) {
        String sql = """
            SELECT id, nom, type_proprietaire, email, phone, adresse, notes, 
                   statut, created_by, created_at, validated_at, validated_by
            FROM demandes_creation_proprietaire 
            WHERE created_by = ?
            ORDER BY created_at DESC
        """;
        
        List<DemandeCreationProprietaire> demandes = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, createdBy);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    demandes.add(mapResultSetToEntity(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur lors de la récupération des demandes de l'utilisateur", e);
        }
        
        return demandes;
    }
    
    /**
     * Mappe un ResultSet vers une entité DemandeCreationProprietaire
     */
    private DemandeCreationProprietaire mapResultSetToEntity(ResultSet rs) throws SQLException {
        return new DemandeCreationProprietaire(
            rs.getInt("id"),
            rs.getString("nom"),
            TypeProprietaire.fromString(rs.getString("type_proprietaire")),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("adresse"),
            rs.getString("notes"),
            StatutDemandeProprietaire.fromString(rs.getString("statut")),
            rs.getString("created_by"),
            LocalDateTime.parse(rs.getString("created_at")),
            rs.getString("validated_at") != null ? LocalDateTime.parse(rs.getString("validated_at")) : null,
            rs.getString("validated_by")
        );
    }
}