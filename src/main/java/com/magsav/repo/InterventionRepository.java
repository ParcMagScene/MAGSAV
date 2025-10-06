package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.InterventionRow;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterventionRepository {

  public List<InterventionRow> findAllWithProductName() {
    String sql = """
      SELECT i.*, p.nom AS produit_nom
      FROM interventions i
      LEFT JOIN produits p ON p.id = i.product_id
      ORDER BY i.id DESC
      """;
    AppLogger.logSql("SELECT", "interventions", "findAllWithProductName");
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<InterventionRow> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new InterventionRow(
          rs.getLong("id"),
          rs.getString("produit_nom"),
          rs.getString("statut"),
          rs.getString("panne"),
          rs.getString("date_entree"),
          rs.getString("date_sortie")
        ));
      }
      AppLogger.debug("InterventionRepository: {} interventions trouvées", out.size());
      return out;
    } catch (SQLException e) {
      System.err.println("DEBUG InterventionRepository: ERREUR SQL - " + e.getMessage());
      throw new RuntimeException("findAllWithProductName failed", e);
    }
  }

  public List<InterventionRow> findByProductId(long productId) {
    String sql = """
      SELECT i.*, p.nom AS produit_nom
      FROM interventions i
      LEFT JOIN produits p ON p.id = i.product_id
      WHERE i.product_id=?
      ORDER BY i.id DESC
      """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        List<InterventionRow> out = new ArrayList<>();
        while (rs.next()) {
          out.add(new InterventionRow(
            rs.getLong("id"),
            rs.getString("produit_nom"),
            rs.getString("statut"),
            rs.getString("panne"),
            rs.getString("date_entree"),
            rs.getString("date_sortie")
          ));
        }
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("findByProductId failed", e); }
  }

  public boolean close(long id) {
    String[] candidates = { "Clôturée", "Cloturee", "Fermée", "Fermee" };
    try (Connection c = DB.getConnection()) {
      // Tente avec différents statuts acceptés par d’éventuels triggers
      for (String s : candidates) {
        if (tryCloseWithStatut(c, id, s)) return true;
      }
      // Fallback: ne change que la date_sortie
      try (PreparedStatement ps = c.prepareStatement(
          "UPDATE interventions " +
          "SET date_sortie = COALESCE(NULLIF(TRIM(date_sortie),''), datetime('now')) " +
          "WHERE id=?")) {
        ps.setLong(1, id);
        return ps.executeUpdate() > 0;
      }
    } catch (SQLException e) {
      throw new RuntimeException("close failed", e);
    }
  }

  private boolean tryCloseWithStatut(Connection c, long id, String statut) {
    try (PreparedStatement ps = c.prepareStatement(
        "UPDATE interventions " +
        "SET statut=?, date_sortie = COALESCE(NULLIF(TRIM(date_sortie),''), datetime('now')) " +
        "WHERE id=?")) {
      ps.setString(1, statut);
      ps.setLong(2, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException ignore) {
      return false;
    }
  }

  /*
   * Cette méthode est désactivée car la table interventions n'a pas de colonne suivi_no
   * TODO: Ajouter la colonne suivi_no à la table ou implémenter une autre logique d'idempotence
   */
  /*
  public Optional<Long> findIdBySuiviNo(String suiviNo) {
    if (suiviNo == null || suiviNo.isBlank()) return Optional.empty();
    String sql = "SELECT id FROM interventions WHERE suivi_no=? ORDER BY id DESC LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, suiviNo);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty();
      }
    } catch (SQLException e) { throw new RuntimeException("findIdBySuiviNo failed", e); }
  }
  */

  public void insertFromImport(Long productId,
                               String statut,
                               String panne,
                               String detecteur,
                               String dateEntree,
                               String dateSortie,
                               String suiviNo,
                               String ownerType,
                               Long ownerSocieteId) {
    AppLogger.logSql("INSERT", "interventions", productId, statut, panne);
    // La table n'a pas de colonne suivi_no, donc on l'ignore
    String sql = """
      INSERT INTO interventions(product_id, statut, panne, date_entree, date_sortie, owner_type, owner_societe_id, detector_societe_id, serial)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)
      """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      if (productId == null) ps.setNull(1, Types.INTEGER); else ps.setLong(1, productId);
      ps.setString(2, statut);
      ps.setString(3, panne);
      ps.setString(4, dateEntree);
      ps.setString(5, dateSortie);
      ps.setString(6, ownerType);
      if (ownerSocieteId == null) ps.setNull(7, Types.INTEGER); else ps.setLong(7, ownerSocieteId);
      if (detecteur != null && !detecteur.isEmpty()) {
        // TODO: Mapper le detecteur vers une societe_id si nécessaire
        ps.setNull(8, Types.INTEGER);
      } else {
        ps.setNull(8, Types.INTEGER);
      }
      int result = ps.executeUpdate();
      AppLogger.debug("insertFromImport réussi: {} ligne(s) insérée(s)", result);
    } catch (SQLException e) { 
      System.err.println("DEBUG: insertFromImport échec: " + e.getMessage());
      throw new RuntimeException("insertFromImport failed", e); 
    }
  }

  public void updateFromImport(Long id,
                               Long productId,
                               String statut,
                               String panne,
                               String detecteur,
                               String dateEntree,
                               String dateSortie,
                               String ownerType,
                               Long ownerSocieteId) {
    String sql = """
      UPDATE interventions
         SET product_id = ?,
             statut = ?,
             panne = ?,
             date_entree = ?,
             date_sortie = ?,
             owner_type = ?,
             owner_societe_id = ?,
             detector_societe_id = NULL
       WHERE id = ?
      """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      if (productId == null) ps.setNull(1, Types.INTEGER); else ps.setLong(1, productId);
      ps.setString(2, statut);
      ps.setString(3, panne);
      if (dateEntree == null || dateEntree.isBlank()) ps.setNull(4, Types.VARCHAR); else ps.setString(4, dateEntree);
      if (dateSortie == null || dateSortie.isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, dateSortie);
      ps.setString(6, ownerType);
      if (ownerSocieteId == null) ps.setNull(7, Types.INTEGER); else ps.setLong(7, ownerSocieteId);
      ps.setLong(8, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException("updateFromImport failed", e); }
  }

  public static record OwnerInfo(String ownerType, Long ownerSocieteId) {}
  public Optional<OwnerInfo> findLastOwnerForProduct(long productId) {
    String sql = """
      SELECT owner_type, owner_societe_id
      FROM interventions
      WHERE product_id=? AND (owner_type IS NOT NULL OR owner_societe_id IS NOT NULL)
      ORDER BY COALESCE(date_sortie, date_entree, '0000-01-01') DESC, id DESC
      LIMIT 1
      """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Object obj = rs.getObject(2);
          Long ownerId = (obj == null) ? null : ((Number) obj).longValue();
          return Optional.of(new OwnerInfo(rs.getString(1), ownerId));
        }
        return Optional.empty();
      }
    } catch (SQLException e) { throw new RuntimeException("findLastOwnerForProduct failed", e); }
  }

  public void updateOwnerForOpenByProduct(long productId, String ownerType, Long ownerSocieteId) {
    String sql = "UPDATE interventions SET owner_type=?, owner_societe_id=? WHERE product_id=? AND (date_sortie IS NULL OR TRIM(date_sortie)='')";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, ownerType);
      if (ownerSocieteId == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, ownerSocieteId);
      ps.setLong(3, productId);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException("updateOwnerForOpenByProduct failed", e); }
  }

  public long insert(Long productId, String serial, Long detectorSocieteId, String description) {
    String sql = """
      INSERT INTO interventions(product_id, statut, panne, serial, detector_societe_id, date_entree)
      VALUES (?, 'Ouverte', ?, ?, ?, COALESCE(strftime('%Y-%m-%d','now'), datetime('now')))
      """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, productId);
      ps.setString(2, description);
      ps.setString(3, serial);
      if (detectorSocieteId == null) ps.setNull(4, Types.INTEGER); else ps.setLong(4, detectorSocieteId);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getLong(1); }
      return 0L;
    } catch (SQLException e) { throw new RuntimeException("insert failed", e); }
  }
}