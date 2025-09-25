package com.magsav.repo;

import com.magsav.model.Appareil;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AppareilRepository {
    private final DataSource ds;
    
    public AppareilRepository(DataSource ds) {
        this.ds = ds;
    }
    
    public Appareil save(Appareil appareil) throws SQLException {
        if (appareil.id() == null) {
            return insert(appareil);
        } else {
            return update(appareil);
        }
    }
    
    private Appareil insert(Appareil appareil) throws SQLException {
        String sql = "INSERT INTO appareils(client_id, marque, modele, sn, accessoires) VALUES(?,?,?,?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, appareil.clientId());
            ps.setString(2, appareil.marque());
            ps.setString(3, appareil.modele());
            ps.setString(4, appareil.sn());
            ps.setString(5, appareil.accessoires());
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return appareil.withId(rs.getLong(1));
                }
                throw new SQLException("Failed to get generated ID");
            }
        }
    }
    
    private Appareil update(Appareil appareil) throws SQLException {
        String sql = "UPDATE appareils SET client_id=?, marque=?, modele=?, sn=?, accessoires=? WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, appareil.clientId());
            ps.setString(2, appareil.marque());
            ps.setString(3, appareil.modele());
            ps.setString(4, appareil.sn());
            ps.setString(5, appareil.accessoires());
            ps.setLong(6, appareil.id());
            
            ps.executeUpdate();
            return appareil;
        }
    }
    
    public Appareil findById(Long id) throws SQLException {
        String sql = "SELECT id, client_id, marque, modele, sn, accessoires FROM appareils WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFromResultSet(rs);
                }
                return null;
            }
        }
    }
    
    public List<Appareil> findBySerialNumber(String sn) throws SQLException {
        String sql = "SELECT id, client_id, marque, modele, sn, accessoires FROM appareils WHERE sn LIKE ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + sn + "%");
            List<Appareil> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    public List<Appareil> findByClientId(Long clientId) throws SQLException {
        String sql = "SELECT id, client_id, marque, modele, sn, accessoires FROM appareils WHERE client_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, clientId);
            List<Appareil> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    private Appareil mapFromResultSet(ResultSet rs) throws SQLException {
        return new Appareil(
            rs.getLong("id"),
            rs.getLong("client_id"),
            rs.getString("marque"),
            rs.getString("modele"),
            rs.getString("sn"),
            rs.getString("accessoires"),
            LocalDateTime.now() // TODO: get from DB when we add created_at column
        );
    }
}