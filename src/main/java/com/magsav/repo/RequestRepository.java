package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.RequestItem;
import com.magsav.model.RequestRow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class RequestRepository {

  public long create(String type, String commentaire, Long fournisseurId) {
    String sql = "INSERT INTO requests(type, commentaire, fournisseur_id) VALUES (?,?,?)";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, type);
      ps.setString(2, commentaire);
      if (fournisseurId == null) ps.setNull(3, Types.INTEGER); else ps.setLong(3, fournisseurId);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) {
        return k.next() ? k.getLong(1) : -1L;
      }
    } catch (SQLException e) { throw new RuntimeException("Create request failed", e); }
  }

  public void update(long id, String commentaire, Long fournisseurId) {
    String sql = "UPDATE requests SET commentaire=?, fournisseur_id=? WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, commentaire);
      if (fournisseurId == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, fournisseurId);
      ps.setLong(3, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException("Update request failed", e); }
  }

  public boolean delete(long id) {
    String sql = "DELETE FROM requests WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("Delete request failed", e); }
  }

  public List<RequestRow> list(String type) {
    String sql = """
      SELECT r.id, r.type, r.status, s.nom AS fournisseurNom, r.commentaire, r.created_at, r.validated_at
      FROM requests r
      LEFT JOIN societes s ON s.id = r.fournisseur_id
      WHERE r.type = ?
      ORDER BY r.id DESC
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, type);
      try (ResultSet rs = ps.executeQuery()) {
        List<RequestRow> out = new ArrayList<>();
        while (rs.next()) {
          out.add(new RequestRow(
              rs.getLong("id"),
              rs.getString("type"),
              rs.getString("status"),
              rs.getString("fournisseurNom"),
              rs.getString("commentaire"),
              rs.getString("created_at"),
              rs.getString("validated_at")));
        }
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("List requests failed", e); }
  }

  public List<RequestItem> items(long requestId) {
    String sql = "SELECT id, request_id, ref, qty, description FROM request_items WHERE request_id=? ORDER BY id";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, requestId);
      try (ResultSet rs = ps.executeQuery()) {
        List<RequestItem> out = new ArrayList<>();
        while (rs.next()) {
          out.add(new RequestItem(
              rs.getLong("id"),
              rs.getLong("request_id"),
              rs.getString("ref"),
              rs.getInt("qty"),
              rs.getString("description")));
        }
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("List items failed", e); }
  }

  public long addItem(long requestId, String ref, int qty, String description) {
    String sql = "INSERT INTO request_items(request_id, ref, qty, description) VALUES (?,?,?,?)";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setLong(1, requestId);
      ps.setString(2, ref);
      ps.setInt(3, qty);
      ps.setString(4, description);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) {
        return k.next() ? k.getLong(1) : -1L;
      }
    } catch (SQLException e) { throw new RuntimeException("Add item failed", e); }
  }

  public boolean updateItem(RequestItem it) {
    String sql = "UPDATE request_items SET ref=?, qty=?, description=? WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, it.ref());
      ps.setInt(2, it.qty());
      ps.setString(3, it.description());
      ps.setLong(4, it.id());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("Update item failed", e); }
  }

  public boolean deleteItem(long id) {
    String sql = "DELETE FROM request_items WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("Delete item failed", e); }
  }

  public void updateStatus(long id, String status) {
    String sql = "UPDATE requests SET status=?, validated_at = CASE WHEN ?='VALIDEE' THEN strftime('%Y-%m-%dT%H:%M:%S','now') ELSE validated_at END WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, status);
      ps.setString(2, status);
      ps.setLong(3, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException("Update status failed", e); }
  }
}