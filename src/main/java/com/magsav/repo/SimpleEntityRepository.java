package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Entity;
import com.magsav.model.EntityType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository simple pour les entités
 */
public class SimpleEntityRepository {

    public long create(Entity entity) throws SQLException {
        String sql = """
            INSERT INTO entities (type, nom, email, phone, adresse, siret, tva_number, actif)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, entity.getTypeString());
            stmt.setString(2, entity.nom());
            stmt.setString(3, entity.email());
            stmt.setString(4, entity.phone());
            stmt.setString(5, entity.adresse());
            stmt.setString(6, entity.siret());
            stmt.setString(7, entity.tvaNumber());
            stmt.setBoolean(8, entity.actif());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        throw new SQLException("Échec de création");
    }

    public void update(Entity entity) throws SQLException {
        String sql = """
            UPDATE entities 
            SET type = ?, nom = ?, email = ?, phone = ?, adresse = ?, siret = ?, tva_number = ?, actif = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, entity.getTypeString());
            stmt.setString(2, entity.nom());
            stmt.setString(3, entity.email());
            stmt.setString(4, entity.phone());
            stmt.setString(5, entity.adresse());
            stmt.setString(6, entity.siret());
            stmt.setString(7, entity.tvaNumber());
            stmt.setBoolean(8, entity.actif());
            stmt.setLong(9, entity.id());
            
            stmt.executeUpdate();
        }
    }

    public Optional<Entity> findById(long id) throws SQLException {
        String sql = "SELECT * FROM entities WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapEntity(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Entity> findAll() throws SQLException {
        String sql = "SELECT * FROM entities ORDER BY nom";
        List<Entity> entities = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                entities.add(mapEntity(rs));
            }
        }
        return entities;
    }

    public List<Entity> findByType(EntityType type) throws SQLException {
        String sql = "SELECT * FROM entities WHERE type = ? ORDER BY nom";
        List<Entity> entities = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapEntity(rs));
                }
            }
        }
        return entities;
    }

    public List<Entity> searchByName(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM entities WHERE nom LIKE ? ORDER BY nom";
        List<Entity> entities = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + searchTerm + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entities.add(mapEntity(rs));
                }
            }
        }
        return entities;
    }

    public void delete(long id) throws SQLException {
        String sql = "DELETE FROM entities WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    private Entity mapEntity(ResultSet rs) throws SQLException {
        return new Entity(
            rs.getLong("id"),
            EntityType.valueOf(rs.getString("type")),
            rs.getString("nom"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("adresse"),
            rs.getString("siret"),
            rs.getString("tva_number"),
            rs.getBoolean("actif")
        );
    }
}