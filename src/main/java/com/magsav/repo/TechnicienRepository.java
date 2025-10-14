package com.magsav.repo;

import com.magsav.cache.CacheManager;
import com.magsav.db.DB;
import com.magsav.model.Technicien;
import com.magsav.model.Technicien.StatutTechnicien;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository pour la gestion des techniciens avec cache optimisé
 */
public class TechnicienRepository {
    
    private static final CacheManager cache = CacheManager.getInstance();
    
    /**
     * Récupère tous les techniciens (avec cache)
     */
    public ObservableList<Technicien> findAll() {
        return cache.get("techniciens:all", () -> {
            ObservableList<Technicien> techniciens = FXCollections.observableArrayList();
            
            String sql = """
                SELECT id, nom, prenom, fonction, email, telephone, telephone_urgence,
                       adresse, code_postal, ville, permis_conduire, habilitations, 
                       date_obtention_permis, date_validite_habilitations, specialites, statut, notes,
                       societe_id, societe_nom,
                       google_contact_id, google_calendar_id, sync_google_enabled, last_google_sync,
                       date_creation, date_modification
                FROM techniciens 
                ORDER BY nom, prenom
            """;
            
            try (Connection conn = DB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    techniciens.add(mapResultSetToTechnicien(rs));
                }
                
            } catch (SQLException e) {
                System.err.println("Erreur lors de la récupération des techniciens: " + e.getMessage());
                e.printStackTrace();
            }
            
            return techniciens;
        }, 3); // TTL de 3 minutes
    }
    
    /**
     * Recherche des techniciens par nom, prénom ou email (avec cache)
     */
    public ObservableList<Technicien> search(String query) {
        // Cache avec clé basée sur la requête normalisée
        String normalizedQuery = query.trim().toLowerCase();
        return cache.get("techniciens:search:" + normalizedQuery, () -> {
            ObservableList<Technicien> techniciens = FXCollections.observableArrayList();
            
            String sql = """
                SELECT id, nom, prenom, email, telephone, specialites, statut, notes,
                       google_contact_id, google_calendar_id, sync_google_enabled, last_google_sync,
                       date_creation, date_modification
                FROM techniciens 
                WHERE UPPER(nom) LIKE UPPER(?) 
                   OR UPPER(prenom) LIKE UPPER(?) 
                   OR UPPER(email) LIKE UPPER(?)
                ORDER BY nom, prenom
            """;
            
            String searchPattern = "%" + query + "%";
            
            try (Connection conn = DB.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        techniciens.add(mapResultSetToTechnicien(rs));
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("Erreur lors de la recherche de techniciens: " + e.getMessage());
                e.printStackTrace();
            }
            
            return techniciens;
        }, 1); // TTL court de 1 minute pour les recherches
    }
    
    /**
     * Récupère un technicien par son ID
     */
    public Technicien findById(int id) {
        String sql = """
            SELECT id, nom, prenom, email, telephone, specialites, statut, notes,
                   google_contact_id, google_calendar_id, sync_google_enabled, last_google_sync,
                   date_creation, date_modification
            FROM techniciens 
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTechnicien(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du technicien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Récupère les techniciens disponibles (statut ACTIF)
     */
    public ObservableList<Technicien> findDisponibles() {
        ObservableList<Technicien> techniciens = FXCollections.observableArrayList();
        
        String sql = """
            SELECT id, nom, prenom, email, telephone, specialites, statut, notes,
                   google_contact_id, google_calendar_id, sync_google_enabled, last_google_sync,
                   date_creation, date_modification
            FROM techniciens 
            WHERE statut = 'ACTIF'
            ORDER BY nom, prenom
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                techniciens.add(mapResultSetToTechnicien(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des techniciens disponibles: " + e.getMessage());
            e.printStackTrace();
        }
        
        return techniciens;
    }
    
    /**
     * Sauvegarde un technicien (création ou mise à jour)
     */
    public boolean save(Technicien technicien) {
        if (technicien.getId() == 0) {
            return insert(technicien);
        } else {
            return update(technicien);
        }
    }
    
    /**
     * Insère un nouveau technicien
     */
    private boolean insert(Technicien technicien) {
        String sql = """
            INSERT INTO techniciens (nom, prenom, email, telephone, specialites, statut, notes,
                                   google_contact_id, google_calendar_id, sync_google_enabled, last_google_sync)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, technicien.getNom());
            pstmt.setString(2, technicien.getPrenom());
            pstmt.setString(3, technicien.getEmail());
            pstmt.setString(4, technicien.getTelephone());
            pstmt.setString(5, technicien.getSpecialites());
            pstmt.setString(6, technicien.getStatut().name());
            pstmt.setString(7, technicien.getNotes());
            pstmt.setString(8, technicien.getGoogleContactId());
            pstmt.setString(9, technicien.getGoogleCalendarId());
            pstmt.setBoolean(10, technicien.isSyncGoogleEnabled());
            pstmt.setString(11, technicien.getLastGoogleSync());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        technicien.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du technicien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Met à jour un technicien existant
     */
    private boolean update(Technicien technicien) {
        String sql = """
            UPDATE techniciens 
            SET nom = ?, prenom = ?, email = ?, telephone = ?, specialites = ?, statut = ?, notes = ?,
                google_contact_id = ?, google_calendar_id = ?, sync_google_enabled = ?, last_google_sync = ?,
                date_modification = datetime('now')
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, technicien.getNom());
            pstmt.setString(2, technicien.getPrenom());
            pstmt.setString(3, technicien.getEmail());
            pstmt.setString(4, technicien.getTelephone());
            pstmt.setString(5, technicien.getSpecialites());
            pstmt.setString(6, technicien.getStatut().name());
            pstmt.setString(7, technicien.getNotes());
            pstmt.setString(8, technicien.getGoogleContactId());
            pstmt.setString(9, technicien.getGoogleCalendarId());
            pstmt.setBoolean(10, technicien.isSyncGoogleEnabled());
            pstmt.setString(11, technicien.getLastGoogleSync());
            pstmt.setInt(12, technicien.getId());
            
            technicien.updateModificationDate();
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du technicien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Supprime un technicien
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM techniciens WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du technicien: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Vérifie si un email existe déjà
     */
    public boolean existsByEmail(String email, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM techniciens WHERE email = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'email: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Récupère les statistiques des techniciens par statut
     */
    public Map<String, Integer> getStatistiques() {
        Map<String, Integer> stats = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN statut = 'ACTIF' THEN 1 ELSE 0 END) as actifs,
                SUM(CASE WHEN statut = 'CONGE' THEN 1 ELSE 0 END) as en_conge,
                SUM(CASE WHEN statut = 'INDISPONIBLE' THEN 1 ELSE 0 END) as indisponibles,
                SUM(CASE WHEN statut = 'INACTIF' THEN 1 ELSE 0 END) as inactifs,
                SUM(CASE WHEN sync_google_enabled = 1 THEN 1 ELSE 0 END) as google_sync
            FROM techniciens
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("actifs", rs.getInt("actifs"));
                stats.put("en_conge", rs.getInt("en_conge"));
                stats.put("indisponibles", rs.getInt("indisponibles"));
                stats.put("inactifs", rs.getInt("inactifs"));
                stats.put("google_sync", rs.getInt("google_sync"));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Met à jour les informations de synchronisation Google
     */
    public boolean updateGoogleSync(int technicienId, String googleContactId, String googleCalendarId, 
                                  boolean syncEnabled) {
        String sql = """
            UPDATE techniciens 
            SET google_contact_id = ?, google_calendar_id = ?, sync_google_enabled = ?, 
                last_google_sync = datetime('now'), date_modification = datetime('now')
            WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, googleContactId);
            pstmt.setString(2, googleCalendarId);
            pstmt.setBoolean(3, syncEnabled);
            pstmt.setInt(4, technicienId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour Google sync: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Convertit un ResultSet en objet Technicien
     */
    private Technicien mapResultSetToTechnicien(ResultSet rs) throws SQLException {
        Technicien technicien = new Technicien();
        
        // Informations de base
        technicien.setId(rs.getInt("id"));
        technicien.setNom(rs.getString("nom"));
        technicien.setPrenom(rs.getString("prenom"));
        technicien.setFonction(rs.getString("fonction"));
        technicien.setEmail(rs.getString("email"));
        technicien.setTelephone(rs.getString("telephone"));
        technicien.setTelephoneUrgence(rs.getString("telephone_urgence"));
        
        // Adresse
        technicien.setAdresse(rs.getString("adresse"));
        technicien.setCodePostal(rs.getString("code_postal"));
        technicien.setVille(rs.getString("ville"));
        
        // Permis et habilitations
        technicien.setPermisConduire(rs.getString("permis_conduire"));
        technicien.setHabilitations(rs.getString("habilitations"));
        technicien.setDateObtentionPermis(rs.getString("date_obtention_permis"));
        technicien.setDateValiditeHabilitations(rs.getString("date_validite_habilitations"));
        
        // Spécialités et informations générales
        technicien.setSpecialites(rs.getString("specialites"));
        technicien.setNotes(rs.getString("notes"));
        
        // Association société
        technicien.setSocieteId(rs.getInt("societe_id"));
        technicien.setSocieteNom(rs.getString("societe_nom"));
        
        // Conversion sécurisée de l'enum
        String statutStr = rs.getString("statut");
        if (statutStr != null) {
            try {
                technicien.setStatut(StatutTechnicien.valueOf(statutStr));
            } catch (IllegalArgumentException e) {
                technicien.setStatut(StatutTechnicien.ACTIF); // Valeur par défaut
            }
        }
        
        // Intégration Google
        technicien.setGoogleContactId(rs.getString("google_contact_id"));
        technicien.setGoogleCalendarId(rs.getString("google_calendar_id"));
        technicien.setSyncGoogleEnabled(rs.getBoolean("sync_google_enabled"));
        technicien.setLastGoogleSync(rs.getString("last_google_sync"));
        
        // Métadonnées
        technicien.setDateCreation(rs.getString("date_creation"));
        technicien.setDateModification(rs.getString("date_modification"));
        
        return technicien;
    }
    

}