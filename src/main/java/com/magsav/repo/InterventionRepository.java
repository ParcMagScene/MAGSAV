package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.model.InterventionRow;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class InterventionRepository {
  public InterventionRow findById(long id) {
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT i.id, p.nom_produit as produit_nom, i.statut_intervention, " +
                   "COALESCE(i.panne, i.defect_description, '') as panne, " +
                   "i.date_entree, i.date_sortie " +
                   "FROM interventions i " +
                   "JOIN produits p ON i.produit_id = p.id " +
                   "WHERE i.id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return new InterventionRow(
            rs.getLong("id"),
            rs.getString("produit_nom"),
            rs.getString("statut_intervention"),
            rs.getString("panne"),
            rs.getString("date_entree"),
            rs.getString("date_sortie")
        );
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération intervention par ID", e);
    }
    return null;
  }

  public long insert(long productId, String serialNumber, String clientNote, String defectDescription) {
    try (Connection conn = DB.getConnection()) {
      String sql = "INSERT INTO interventions (produit_id, numero_serie, note_client, description_panne, date_entree) VALUES (?, ?, ?, ?, datetime('now'))";
      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      stmt.setLong(1, productId);
      stmt.setString(2, serialNumber);
      stmt.setString(3, clientNote);
      stmt.setString(4, defectDescription);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur insertion intervention", e);
    }
    return -1;
  }

  public List<InterventionRow> findByProductId(long productId) {
    List<InterventionRow> interventions = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT i.id, p.nom_produit as produit_nom, i.statut_intervention, " +
                   "COALESCE(i.description_panne, '') as panne, " +
                   "i.date_entree, i.date_sortie " +
                   "FROM interventions i " +
                   "JOIN produits p ON i.produit_id = p.id " +
                   "WHERE i.produit_id = ? " +
                   "ORDER BY i.date_entree DESC";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, productId);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        interventions.add(new InterventionRow(
            rs.getLong("id"),
            rs.getString("produit_nom"),
            rs.getString("statut_intervention"),
            rs.getString("panne"),
            rs.getString("date_entree"),
            rs.getString("date_sortie")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération interventions", e);
    }
    return interventions;
  }

  public List<InterventionRow> findAllWithProductName() {
    List<InterventionRow> interventions = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT i.id, p.nom_produit as produit_nom, i.statut_intervention, " +
                   "COALESCE(i.description_panne, '') as panne, " +
                   "i.date_entree, i.date_sortie " +
                   "FROM interventions i " +
                   "JOIN produits p ON i.produit_id = p.id " +
                   "ORDER BY i.date_entree DESC";
      PreparedStatement stmt = conn.prepareStatement(sql);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        interventions.add(new InterventionRow(
            rs.getLong("id"),
            rs.getString("produit_nom"),
            rs.getString("statut_intervention"),
            rs.getString("panne"),
            rs.getString("date_entree"),
            rs.getString("date_sortie")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération toutes interventions", e);
    }
    return interventions;
  }

  public boolean close(long interventionId) {
    try (Connection conn = DB.getConnection()) {
      String sql = "UPDATE interventions SET statut_intervention = 'Terminée', date_sortie = datetime('now') WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, interventionId);
      int rowsAffected = stmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      throw new DatabaseException("Erreur fermeture intervention", e);
    }
  }

  public long insertFromImport(Long productId, String statut, String panne, String detecteur, 
                              String dateEntree, String dateSortie, String suiviNo, String ownerType, Long ownerSocieteId) {
    try (Connection conn = DB.getConnection()) {
      // Utiliser les colonnes qui existent réellement dans la table interventions
      String sql = "INSERT INTO interventions (produit_id, statut_intervention, panne, detecteur, " +
                   "date_entree, date_sortie, owner_type, owner_societe_id) " +
                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      if (productId != null) {
        stmt.setLong(1, productId);
      } else {
        stmt.setNull(1, Types.BIGINT);
      }
      stmt.setString(2, statut);
      stmt.setString(3, panne);         // Pannes dans le champ panne
      stmt.setString(4, detecteur);     // Détecteur dans le champ detecteur
      stmt.setString(5, dateEntree);
      stmt.setString(6, dateSortie);
      stmt.setString(7, ownerType);
      if (ownerSocieteId != null) {
        stmt.setLong(8, ownerSocieteId);
      } else {
        stmt.setNull(8, Types.BIGINT);
      }
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur insertion intervention depuis import", e);
    }
    return -1;
  }
  
  /**
   * Trouve les interventions d'un client donné (par owner_societe_id)
   */
  public List<InterventionRow> findByClientId(long clientId) {
    List<InterventionRow> interventions = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT i.id, p.nom_produit as produit_nom, i.statut_intervention, " +
                   "COALESCE(i.panne, i.defect_description, '') as panne, " +
                   "i.date_entree, i.date_sortie " +
                   "FROM interventions i " +
                   "LEFT JOIN produits p ON i.produit_id = p.id " +
                   "WHERE i.owner_societe_id = ? " +
                   "ORDER BY i.date_entree DESC";
      
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, clientId);
      ResultSet rs = stmt.executeQuery();
      
      while (rs.next()) {
        interventions.add(new InterventionRow(
            rs.getLong("id"),
            rs.getString("produit_nom"),
            rs.getString("statut_intervention"),
            rs.getString("panne"),
            rs.getString("date_entree"),
            rs.getString("date_sortie")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération interventions par client ID", e);
    }
    return interventions;
  }
  
  /**
   * Compte le nombre total d'interventions
   */
  public int getTotalInterventionCount() {
    String sql = "SELECT COUNT(*) FROM interventions";
    
    try (Connection conn = DB.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
        return 0;
      }
      
    } catch (SQLException e) {
      System.err.println("Erreur lors du comptage total des interventions: " + e.getMessage());
      return 0;
    }
  }
}
