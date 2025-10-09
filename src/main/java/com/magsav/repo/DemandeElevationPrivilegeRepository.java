package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.DemandeElevationPrivilege;
import com.magsav.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DemandeElevationPrivilegeRepository {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Créer une nouvelle demande d'élévation
    public boolean creerDemande(DemandeElevationPrivilege demande) {
        String sql = """
            INSERT INTO demandes_elevation_privilege 
            (user_id, username, full_name, role_actuel, role_demande, justification, 
             created_by, expires_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, demande.getUserId());
            stmt.setString(2, demande.getUsername());
            stmt.setString(3, demande.getFullName());
            stmt.setString(4, demande.getRoleActuel().name());
            stmt.setString(5, demande.getRoleDemande().name());
            stmt.setString(6, demande.getJustification());
            stmt.setString(7, demande.getCreatedBy());
            stmt.setString(8, demande.getExpiresAt() != null ? 
                demande.getExpiresAt().format(FORMATTER) : null);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        demande.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la demande d'élévation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Récupérer une demande par ID
    public Optional<DemandeElevationPrivilege> findById(int id) {
        String sql = """
            SELECT * FROM demandes_elevation_privilege 
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDemande(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la demande: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    // Récupérer toutes les demandes en attente
    public List<DemandeElevationPrivilege> findDemandesEnAttente() {
        return findDemandesByStatut(DemandeElevationPrivilege.StatutDemande.EN_ATTENTE);
    }
    
    // Récupérer les demandes par statut
    public List<DemandeElevationPrivilege> findDemandesByStatut(DemandeElevationPrivilege.StatutDemande statut) {
        String sql = """
            SELECT * FROM demandes_elevation_privilege 
            WHERE statut = ?
            ORDER BY created_at DESC
        """;
        
        List<DemandeElevationPrivilege> demandes = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, statut.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    demandes.add(mapResultSetToDemande(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des demandes par statut: " + e.getMessage());
        }
        
        return demandes;
    }
    
    // Récupérer les demandes d'un utilisateur
    public List<DemandeElevationPrivilege> findDemandesByUserId(int userId) {
        String sql = """
            SELECT * FROM demandes_elevation_privilege 
            WHERE user_id = ?
            ORDER BY created_at DESC
        """;
        
        List<DemandeElevationPrivilege> demandes = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    demandes.add(mapResultSetToDemande(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des demandes par utilisateur: " + e.getMessage());
        }
        
        return demandes;
    }
    
    // Valider une demande (approuver ou rejeter)
    public boolean validerDemande(int demandeId, DemandeElevationPrivilege.StatutDemande nouveauStatut, 
                                  String validatedBy, String notesValidation) {
        String sql = """
            UPDATE demandes_elevation_privilege 
            SET statut = ?, validated_at = ?, validated_by = ?, notes_validation = ?
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nouveauStatut.name());
            stmt.setString(2, LocalDateTime.now().format(FORMATTER));
            stmt.setString(3, validatedBy);
            stmt.setString(4, notesValidation);
            stmt.setInt(5, demandeId);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la validation de la demande: " + e.getMessage());
        }
        
        return false;
    }
    
    // Marquer les demandes expirées
    public int marquerDemandesExpirees() {
        String sql = """
            UPDATE demandes_elevation_privilege 
            SET statut = 'EXPIREE'
            WHERE statut = 'EN_ATTENTE' 
            AND expires_at IS NOT NULL 
            AND datetime(expires_at) < datetime('now')
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            return stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du marquage des demandes expirées: " + e.getMessage());
        }
        
        return 0;
    }
    
    // Récupérer toutes les demandes avec pagination
    public List<DemandeElevationPrivilege> findAllDemandes(int limit, int offset) {
        String sql = """
            SELECT * FROM demandes_elevation_privilege 
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
        """;
        
        List<DemandeElevationPrivilege> demandes = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    demandes.add(mapResultSetToDemande(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des demandes: " + e.getMessage());
        }
        
        return demandes;
    }
    
    // Compter le total des demandes
    public int countDemandes() {
        String sql = "SELECT COUNT(*) FROM demandes_elevation_privilege";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du comptage des demandes: " + e.getMessage());
        }
        
        return 0;
    }
    
    // Supprimer une demande
    public boolean supprimerDemande(int id) {
        String sql = "DELETE FROM demandes_elevation_privilege WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la demande: " + e.getMessage());
        }
        
        return false;
    }
    
    // Mapper ResultSet vers DemandeElevationPrivilege
    private DemandeElevationPrivilege mapResultSetToDemande(ResultSet rs) throws SQLException {
        DemandeElevationPrivilege demande = new DemandeElevationPrivilege();
        
        demande.setId(rs.getInt("id"));
        demande.setUserId(rs.getInt("user_id"));
        demande.setUsername(rs.getString("username"));
        demande.setFullName(rs.getString("full_name"));
        demande.setRoleActuel(User.Role.valueOf(rs.getString("role_actuel")));
        demande.setRoleDemande(User.Role.valueOf(rs.getString("role_demande")));
        demande.setJustification(rs.getString("justification"));
        demande.setStatut(DemandeElevationPrivilege.StatutDemande.valueOf(rs.getString("statut")));
        demande.setCreatedBy(rs.getString("created_by"));
        
        // Gérer les dates
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            demande.setCreatedAt(LocalDateTime.parse(createdAtStr, FORMATTER));
        }
        
        String validatedAtStr = rs.getString("validated_at");
        if (validatedAtStr != null) {
            demande.setValidatedAt(LocalDateTime.parse(validatedAtStr, FORMATTER));
        }
        
        String expiresAtStr = rs.getString("expires_at");
        if (expiresAtStr != null) {
            demande.setExpiresAt(LocalDateTime.parse(expiresAtStr, FORMATTER));
        }
        
        demande.setValidatedBy(rs.getString("validated_by"));
        demande.setNotesValidation(rs.getString("notes_validation"));
        
        return demande;
    }
}