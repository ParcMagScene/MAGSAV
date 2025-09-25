package com.magsav.repo;

import com.magsav.model.Dossier;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DossierRepository {
    private final DataSource ds;
    
    public DossierRepository(DataSource ds) {
        this.ds = ds;
    }
    
    public Dossier save(Dossier dossier) throws SQLException {
        if (dossier.id() == null) {
            return insert(dossier);
        } else {
            return update(dossier);
        }
    }
    
    private Dossier insert(Dossier dossier) throws SQLException {
        String sql = "INSERT INTO dossiers(appareil_id, statut, symptome, commentaire, date_entree, date_sortie) VALUES(?,?,?,?,?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setLong(1, dossier.appareilId());
            ps.setString(2, dossier.statut());
            ps.setString(3, dossier.symptome());
            ps.setString(4, dossier.commentaire());
            ps.setString(5, dossier.dateEntree() != null ? dossier.dateEntree().toString() : LocalDate.now().toString());
            ps.setString(6, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return dossier.withId(rs.getLong(1));
                }
                throw new SQLException("Failed to get generated ID");
            }
        }
    }
    
    private Dossier update(Dossier dossier) throws SQLException {
        String sql = "UPDATE dossiers SET statut=?, symptome=?, commentaire=?, date_sortie=? WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dossier.statut());
            ps.setString(2, dossier.symptome());
            ps.setString(3, dossier.commentaire());
            ps.setString(4, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);
            ps.setLong(5, dossier.id());
            
            ps.executeUpdate();
            return dossier;
        }
    }
    
    public Dossier findById(Long id) throws SQLException {
        String sql = "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers WHERE id=?";
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
    
    public List<Dossier> findByStatut(String statut) throws SQLException {
        String sql = "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers WHERE statut=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, statut);
            List<Dossier> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    public List<Dossier> findByAppareilId(Long appareilId) throws SQLException {
        String sql = "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers WHERE appareil_id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, appareilId);
            List<Dossier> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    public List<Dossier> findAll() throws SQLException {
        String sql = "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers ORDER BY date_entree DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            List<Dossier> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    private Dossier mapFromResultSet(ResultSet rs) throws SQLException {
        String dateEntreeStr = rs.getString("date_entree");
        String dateSortieStr = rs.getString("date_sortie");
        
        return new Dossier(
            rs.getLong("id"),
            rs.getLong("appareil_id"),
            rs.getString("statut"),
            rs.getString("symptome"),
            rs.getString("commentaire"),
            dateEntreeStr != null ? LocalDate.parse(dateEntreeStr) : null,
            dateSortieStr != null ? LocalDate.parse(dateSortieStr) : null,
            LocalDateTime.now() // TODO: get from DB when we add created_at column
        );
    }
}