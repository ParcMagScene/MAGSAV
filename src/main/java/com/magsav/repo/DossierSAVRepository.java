package com.magsav.repo;

import com.magsav.model.DossierSAV;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DossierSAVRepository {
    private final DataSource ds;
    
    public DossierSAVRepository(DataSource ds) {
        this.ds = ds;
    }
    
    public DossierSAV save(DossierSAV dossier) throws SQLException {
        if (dossier.id() == null) {
            return insert(dossier);
        } else {
            return update(dossier);
        }
    }
    
    public DossierSAV upsertBySerieProprietaire(DossierSAV dossier) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            // Essayer mise à jour d'abord
            String update = "UPDATE dossiers_sav SET produit=?, panne=?, statut=?, detecteur=?, date_entree=?, date_sortie=? WHERE numero_serie=? AND proprietaire=?";
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setString(1, dossier.produit());
                ps.setString(2, dossier.panne());
                ps.setString(3, dossier.statut());
                ps.setString(4, dossier.detecteur());
                ps.setString(5, dossier.dateEntree() != null ? dossier.dateEntree().toString() : null);
                ps.setString(6, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);
                ps.setString(7, dossier.numeroSerie());
                ps.setString(8, dossier.proprietaire());
                
                int count = ps.executeUpdate();
                if (count > 0) {
                    return findBySerieProprietaire(dossier.numeroSerie(), dossier.proprietaire());
                }
            }
            
            // Sinon insérer
            return insert(dossier);
        }
    }
    
    private DossierSAV insert(DossierSAV dossier) throws SQLException {
        String sql = "INSERT INTO dossiers_sav(produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, dossier.produit());
            ps.setString(2, dossier.numeroSerie());
            ps.setString(3, dossier.proprietaire());
            ps.setString(4, dossier.panne());
            ps.setString(5, dossier.statut());
            ps.setString(6, dossier.detecteur());
            ps.setString(7, dossier.dateEntree() != null ? dossier.dateEntree().toString() : LocalDate.now().toString());
            ps.setString(8, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);
            
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return dossier.withId(rs.getLong(1));
                }
                throw new SQLException("Failed to get generated ID");
            }
        }
    }
    
    public DossierSAV update(DossierSAV dossier) throws SQLException {
        String sql = "UPDATE dossiers_sav SET produit=?, panne=?, statut=?, detecteur=?, date_sortie=? WHERE id=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, dossier.produit());
            ps.setString(2, dossier.panne());
            ps.setString(3, dossier.statut());
            ps.setString(4, dossier.detecteur());
            ps.setString(5, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);
            ps.setLong(6, dossier.id());
            
            ps.executeUpdate();
            return dossier;
        }
    }
    
    public DossierSAV findById(Long id) throws SQLException {
        String sql = "SELECT id, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie FROM dossiers_sav WHERE id=?";
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
    
    public DossierSAV findBySerieProprietaire(String numeroSerie, String proprietaire) throws SQLException {
        String sql = "SELECT id, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie FROM dossiers_sav WHERE numero_serie=? AND proprietaire=?";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, numeroSerie);
            ps.setString(2, proprietaire);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFromResultSet(rs);
                }
                return null;
            }
        }
    }
    
    public List<DossierSAV> findByStatut(String statut) throws SQLException {
        String sql = "SELECT id, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie FROM dossiers_sav WHERE statut=? ORDER BY date_entree DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, statut);
            List<DossierSAV> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    public List<DossierSAV> findByNumeroSerie(String numeroSerie) throws SQLException {
        String sql = "SELECT id, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie FROM dossiers_sav WHERE numero_serie LIKE ? ORDER BY date_entree DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + numeroSerie + "%");
            List<DossierSAV> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    public List<DossierSAV> findByProprietaire(String proprietaire) throws SQLException {
        String sql = "SELECT id, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie FROM dossiers_sav WHERE proprietaire LIKE ? ORDER BY date_entree DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, "%" + proprietaire + "%");
            List<DossierSAV> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    public List<DossierSAV> findAll() throws SQLException {
        String sql = "SELECT id, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie FROM dossiers_sav ORDER BY date_entree DESC";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            List<DossierSAV> results = new ArrayList<>();
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapFromResultSet(rs));
                }
            }
            return results;
        }
    }
    
    private DossierSAV mapFromResultSet(ResultSet rs) throws SQLException {
        String dateEntreeStr = rs.getString("date_entree");
        String dateSortieStr = rs.getString("date_sortie");
        
        return new DossierSAV(
            rs.getLong("id"),
            rs.getString("produit"),
            rs.getString("numero_serie"),
            rs.getString("proprietaire"),
            rs.getString("panne"),
            rs.getString("statut"),
            rs.getString("detecteur"),
            dateEntreeStr != null ? LocalDate.parse(dateEntreeStr) : null,
            dateSortieStr != null ? LocalDate.parse(dateSortieStr) : null,
            LocalDateTime.now() // TODO: get from DB when we add created_at column
        );
    }
}