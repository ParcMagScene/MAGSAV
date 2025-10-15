package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SavHistoryRepository {
    
    public record SavHistoryEntry(long id, long productId, String productName, long savExterneId, 
            String savNom, String dateDebut, String dateFin, String statut, String notes) {}
    
    /**
     * Récupère l'historique complet pour un SAV externe
     */
    public List<SavHistoryEntry> findBySavExterne(long savExterneId) {
        List<SavHistoryEntry> entries = new ArrayList<>();
        try (Connection conn = DB.getConnection()) {
            String sql = """
                SELECT h.id, h.product_id, p.nom as product_name, h.sav_externe_id, 
                       s.nom as sav_nom, h.date_debut, h.date_fin, h.statut, h.notes
                FROM sav_history h
                JOIN produits p ON h.product_id = p.id
                JOIN societes s ON h.sav_externe_id = s.id
                WHERE h.sav_externe_id = ?
                ORDER BY h.date_debut DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, savExterneId);
                try (ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                entries.add(new SavHistoryEntry(
                    rs.getLong("id"),
                    rs.getLong("product_id"),
                    rs.getString("product_name"),
                    rs.getLong("sav_externe_id"),
                    rs.getString("sav_nom"),
                    rs.getString("date_debut"),
                    rs.getString("date_fin"),
                    rs.getString("statut"),
                    rs.getString("notes")
                    ));
                }
            }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur récupération historique SAV", e);
        }
        return entries;
    }
    
    /**
     * Récupère l'historique pour un produit spécifique
     */
    public List<SavHistoryEntry> findByProduct(long productId) {
        List<SavHistoryEntry> entries = new ArrayList<>();
        try (Connection conn = DB.getConnection()) {
            String sql = """
                SELECT h.id, h.product_id, p.nom as product_name, h.sav_externe_id, 
                       s.nom as sav_nom, h.date_debut, h.date_fin, h.statut, h.notes
                FROM sav_history h
                JOIN produits p ON h.product_id = p.id
                JOIN societes s ON h.sav_externe_id = s.id
                WHERE h.product_id = ?
                ORDER BY h.date_debut DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, productId);
                try (ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                entries.add(new SavHistoryEntry(
                    rs.getLong("id"),
                    rs.getLong("product_id"),
                    rs.getString("product_name"),
                    rs.getLong("sav_externe_id"),
                    rs.getString("sav_nom"),
                    rs.getString("date_debut"),
                    rs.getString("date_fin"),
                    rs.getString("statut"),
                    rs.getString("notes")
                    ));
                }
            }
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur récupération historique produit", e);
        }
        return entries;
    }
    
    /**
     * Ajoute une nouvelle entrée d'historique (produit envoyé en SAV)
     */
    public long startSavEntry(long productId, long savExterneId, String notes) {
        try (Connection conn = DB.getConnection()) {
            String sql = """
                INSERT INTO sav_history (product_id, sav_externe_id, date_debut, statut, notes)
                VALUES (?, ?, ?, 'En cours', ?)
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, productId);
                stmt.setLong(2, savExterneId);
                stmt.setString(3, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                stmt.setString(4, notes);
                
                stmt.executeUpdate();
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
                throw new DatabaseException("Impossible de récupérer l'ID généré");
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur création entrée historique SAV", e);
        }
    }
    
    /**
     * Termine une entrée d'historique (produit revenu du SAV)
     */
    public void endSavEntry(long historyId, String statut, String notes) {
        try (Connection conn = DB.getConnection()) {
            String sql = """
                UPDATE sav_history 
                SET date_fin = ?, statut = ?, notes = COALESCE(?, notes)
                WHERE id = ?
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                stmt.setString(2, statut);
                stmt.setString(3, notes);
                stmt.setLong(4, historyId);
                
                stmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur finalisation entrée historique SAV", e);
        }
    }
    
    /**
     * Met à jour les notes d'une entrée d'historique
     */
    public void updateNotes(long historyId, String notes) {
        try (Connection conn = DB.getConnection()) {
            String sql = "UPDATE sav_history SET notes = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, notes);
                stmt.setLong(2, historyId);
                
                stmt.executeUpdate();
            }
            
        } catch (SQLException e) {
            throw new DatabaseException("Erreur mise à jour notes historique SAV", e);
        }
    }
}