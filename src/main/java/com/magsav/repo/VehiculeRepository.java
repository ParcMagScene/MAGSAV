package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Vehicule;
import com.magsav.model.Vehicule.TypeVehicule;
import com.magsav.model.Vehicule.StatutVehicule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository pour la gestion des véhicules en base de données
 * Compatible avec la base SQLite existante de MAGSAV
 */
public class VehiculeRepository {
    
    private static final String INSERT_SQL = """
        INSERT INTO vehicules (immatriculation, type_vehicule, marque, modele, annee, 
                              kilometrage, statut, location_externe, notes, date_creation, date_modification)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    
    private static final String UPDATE_SQL = """
        UPDATE vehicules SET immatriculation=?, type_vehicule=?, marque=?, modele=?, 
                            annee=?, kilometrage=?, statut=?, location_externe=?, notes=?, date_modification=?
        WHERE id=?
        """;
    
    private static final String SELECT_ALL_SQL = """
        SELECT id, immatriculation, type_vehicule, marque, modele, annee, kilometrage, 
               statut, location_externe, notes, date_creation, date_modification
        FROM vehicules ORDER BY immatriculation
        """;
    
    private static final String SELECT_BY_ID_SQL = SELECT_ALL_SQL.replace("ORDER BY immatriculation", "WHERE id=?");
    
    private static final String DELETE_SQL = "DELETE FROM vehicules WHERE id=?";
    
    private static final String SELECT_BY_STATUT_SQL = SELECT_ALL_SQL.replace("ORDER BY immatriculation", "WHERE statut=? ORDER BY immatriculation");
    
    private static final String SEARCH_SQL = """
        SELECT id, immatriculation, type_vehicule, marque, modele, annee, kilometrage, 
               statut, location_externe, notes, date_creation, date_modification
        FROM vehicules 
        WHERE UPPER(immatriculation) LIKE UPPER(?) 
           OR UPPER(marque) LIKE UPPER(?) 
           OR UPPER(modele) LIKE UPPER(?)
        ORDER BY immatriculation
        """;
    
    /**
     * Récupère tous les véhicules
     */
    public ObservableList<Vehicule> findAll() {
        ObservableList<Vehicule> vehicules = FXCollections.observableArrayList();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des véhicules: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicules;
    }
    
    /**
     * Trouve un véhicule par son ID
     */
    public Vehicule findById(int id) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVehicule(rs);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche du véhicule ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Sauvegarde un véhicule (création ou mise à jour)
     */
    public boolean save(Vehicule vehicule) {
        if (vehicule.getId() == 0) {
            return insert(vehicule);
        } else {
            return update(vehicule);
        }
    }
    
    /**
     * Insère un nouveau véhicule
     */
    private boolean insert(Vehicule vehicule) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            vehicule.setDateCreation(now);
            vehicule.setDateModification(now);
            
            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getTypeVehicule() != null ? vehicule.getTypeVehicule().name() : TypeVehicule.VL.name());
            stmt.setString(3, vehicule.getMarque());
            stmt.setString(4, vehicule.getModele());
            stmt.setObject(5, vehicule.getAnnee() != 0 ? vehicule.getAnnee() : null);
            stmt.setInt(6, vehicule.getKilometrage());
            stmt.setString(7, vehicule.getStatut() != null ? vehicule.getStatut().name() : StatutVehicule.DISPONIBLE.name());
            stmt.setBoolean(8, vehicule.isLocationExterne());
            stmt.setString(9, vehicule.getNotes());
            stmt.setString(10, vehicule.getDateCreation());
            stmt.setString(11, vehicule.getDateModification());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        vehicule.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du véhicule: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Met à jour un véhicule existant
     */
    private boolean update(Vehicule vehicule) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            vehicule.updateModificationDate();
            
            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getTypeVehicule() != null ? vehicule.getTypeVehicule().name() : TypeVehicule.VL.name());
            stmt.setString(3, vehicule.getMarque());
            stmt.setString(4, vehicule.getModele());
            stmt.setObject(5, vehicule.getAnnee() != 0 ? vehicule.getAnnee() : null);
            stmt.setInt(6, vehicule.getKilometrage());
            stmt.setString(7, vehicule.getStatut() != null ? vehicule.getStatut().name() : StatutVehicule.DISPONIBLE.name());
            stmt.setBoolean(8, vehicule.isLocationExterne());
            stmt.setString(9, vehicule.getNotes());
            stmt.setString(10, vehicule.getDateModification());
            stmt.setInt(11, vehicule.getId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du véhicule: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Supprime un véhicule
     */
    public boolean delete(int id) {
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du véhicule ID " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Recherche des véhicules par immatriculation, marque ou modèle
     */
    public ObservableList<Vehicule> search(String searchTerm) {
        ObservableList<Vehicule> vehicules = FXCollections.observableArrayList();
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        
        String pattern = "%" + searchTerm.trim() + "%";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SEARCH_SQL)) {
            
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche de véhicules: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicules;
    }
    
    /**
     * Trouve les véhicules par statut
     */
    public ObservableList<Vehicule> findByStatut(StatutVehicule statut) {
        ObservableList<Vehicule> vehicules = FXCollections.observableArrayList();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_STATUT_SQL)) {
            
            stmt.setString(1, statut.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche par statut: " + e.getMessage());
            e.printStackTrace();
        }
        
        return vehicules;
    }
    
    /**
     * Calcule les statistiques des véhicules
     */
    public Map<String, Integer> getStatistiques() {
        Map<String, Integer> stats = new HashMap<>();
        
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN statut = 'DISPONIBLE' THEN 1 ELSE 0 END) as disponibles,
                SUM(CASE WHEN statut = 'EN_SERVICE' THEN 1 ELSE 0 END) as en_service,
                SUM(CASE WHEN statut = 'MAINTENANCE' THEN 1 ELSE 0 END) as maintenance,
                SUM(CASE WHEN statut = 'HORS_SERVICE' THEN 1 ELSE 0 END) as hors_service
            FROM vehicules
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total"));
                stats.put("disponibles", rs.getInt("disponibles"));
                stats.put("en_service", rs.getInt("en_service"));
                stats.put("maintenance", rs.getInt("maintenance"));
                stats.put("hors_service", rs.getInt("hors_service"));
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    /**
     * Vérifie si une immatriculation existe déjà
     */
    public boolean existsByImmatriculation(String immatriculation, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM vehicules WHERE immatriculation = ?";
        if (excludeId != null) {
            sql += " AND id != ?";
        }
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, immatriculation);
            if (excludeId != null) {
                pstmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de l'immatriculation: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Convertit un ResultSet en objet Vehicule
     */
    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        
        vehicule.setId(rs.getInt("id"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        
        // Conversion sécurisée des enums
        String typeStr = rs.getString("type_vehicule");
        if (typeStr != null) {
            try {
                vehicule.setTypeVehicule(TypeVehicule.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                vehicule.setTypeVehicule(TypeVehicule.VL); // Valeur par défaut
            }
        }
        
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        vehicule.setAnnee(rs.getInt("annee"));
        vehicule.setKilometrage(rs.getInt("kilometrage"));
        
        String statutStr = rs.getString("statut");
        if (statutStr != null) {
            try {
                vehicule.setStatut(StatutVehicule.valueOf(statutStr));
            } catch (IllegalArgumentException e) {
                vehicule.setStatut(StatutVehicule.DISPONIBLE); // Valeur par défaut
            }
        }
        
        vehicule.setLocationExterne(rs.getBoolean("location_externe"));
        vehicule.setNotes(rs.getString("notes"));
        vehicule.setDateCreation(rs.getString("date_creation"));
        vehicule.setDateModification(rs.getString("date_modification"));
        
        return vehicule;
    }
}