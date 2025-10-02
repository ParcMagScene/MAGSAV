package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.InterventionRow;
import java.sql.*;
import java.util.*;

public final class InterventionRepository {
  public List<InterventionRow> findAllWithProductName() {
    String sql = """
      SELECT i.id, COALESCE(p.nom,'(sans nom)') AS produit_nom,
             COALESCE(i.statut,''), COALESCE(i.panne,''),
             COALESCE(i.date_entree,''), COALESCE(i.date_sortie,'')
      FROM interventions i
      LEFT JOIN produits p ON p.id = i.produit_id
      ORDER BY i.id DESC
      """;
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<InterventionRow> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new InterventionRow(
            rs.getLong(1), rs.getString(2), rs.getString(3),
            rs.getString(4), rs.getString(5), rs.getString(6)));
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("Query interventions failed", e);
    }
  }

  public void seedIfEmpty() {
    try (Connection c = DB.getConnection();
         Statement st = c.createStatement()) {
      try (ResultSet r = st.executeQuery("SELECT COUNT(*) FROM interventions")) {
        if (r.next() && r.getInt(1) > 0) return;
      }
      st.executeUpdate("INSERT INTO produits(nom, code, sn, fabricant) VALUES ('Produit demo','P-001','SN-001','DemoInc')");
      long pid;
      try (ResultSet k = st.executeQuery("SELECT last_insert_rowid()")) {
        pid = k.next() ? k.getLong(1) : 1L;
      }
      try (PreparedStatement ps = c.prepareStatement(
          "INSERT INTO interventions(produit_id, statut, panne, date_entree) VALUES (?,?,?,date('now'))")) {
        ps.setLong(1, pid);
        ps.setString(2, "Nouveau");
        ps.setString(3, "Test panne");
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Seed failed", e);
    }
  }
}