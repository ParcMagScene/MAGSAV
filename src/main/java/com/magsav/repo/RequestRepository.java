package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.model.RequestRow;
import com.magsav.model.RequestItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestRepository {

  public long insert(String type, String title, String description, String status) {
    try (Connection conn = DB.getConnection()) {
      String sql = "INSERT INTO requests (type, title, description, status, created_at) VALUES (?, ?, ?, ?, datetime('now'))";
      try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, type);
        stmt.setString(2, title);
        stmt.setString(3, description);
        stmt.setString(4, status);
        stmt.executeUpdate();
        try (ResultSet rs = stmt.getGeneratedKeys()) {
          if (rs.next()) {
            return rs.getLong(1);
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur insertion demande", e);
    }
    return -1;
  }

  public List<RequestRow> findAll() {
    List<RequestRow> requests = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT r.id, r.type, r.status, s.nom_societe as fournisseur_nom, r.description, r.created_at, r.validated_at " +
                   "FROM requests r " +
                   "LEFT JOIN societes s ON r.societe_id = s.id " +
                   "ORDER BY r.created_at DESC";
      try (PreparedStatement stmt = conn.prepareStatement(sql);
           ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          requests.add(new RequestRow(
              rs.getLong("id"),
              rs.getString("type"),
              rs.getString("status"),
              rs.getString("fournisseur_nom"),
              rs.getString("description"),
              rs.getString("created_at"),
              rs.getString("validated_at")
          ));
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération demandes", e);
    }
    return requests;
  }

  public List<RequestRow> list(String type) {
    List<RequestRow> requests = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT r.id, r.type, r.status, s.nom_societe as fournisseur_nom, r.description, r.created_at, r.validated_at " +
                   "FROM requests r " +
                   "LEFT JOIN societes s ON r.societe_id = s.id " +
                   "WHERE r.type = ? " +
                   "ORDER BY r.created_at DESC";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, type);
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            requests.add(new RequestRow(
                rs.getLong("id"),
                rs.getString("type"),
                rs.getString("status"),
                rs.getString("fournisseur_nom"),
                rs.getString("description"),
                rs.getString("created_at"),
                rs.getString("validated_at")
            ));
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération demandes par type", e);
    }
    return requests;
  }

  public List<RequestItem> items(long requestId) {
    List<RequestItem> items = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT id, request_id, reference, quantity, description FROM request_items WHERE request_id = ? ORDER BY reference";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, requestId);
        try (ResultSet rs = stmt.executeQuery()) {
          while (rs.next()) {
            items.add(new RequestItem(
                rs.getLong("id"),
                rs.getLong("request_id"),
                rs.getString("reference"),
                rs.getInt("quantity"),
                rs.getString("description")
            ));
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération items demande", e);
    }
    return items;
  }

  public long create(String type, String title, String description, Long societeId) {
    try (Connection conn = DB.getConnection()) {
      String sql = "INSERT INTO requests (type, title, description, societe_id, status, created_at) VALUES (?, ?, ?, ?, 'EN_ATTENTE', datetime('now'))";
      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, type);
      stmt.setString(2, title);
      stmt.setString(3, description);
      if (societeId != null) {
        stmt.setLong(4, societeId);
      } else {
        stmt.setNull(4, Types.BIGINT);
      }
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur création demande", e);
    }
    return -1;
  }

  public void update(long id, String title, String description, Long societeId) {
    try (Connection conn = DB.getConnection()) {
      String sql = "UPDATE requests SET title = ?, description = ?, societe_id = ? WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, title);
      stmt.setString(2, description);
      if (societeId != null) {
        stmt.setLong(3, societeId);
      } else {
        stmt.setNull(3, Types.BIGINT);
      }
      stmt.setLong(4, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Erreur mise à jour demande", e);
    }
  }

  public void updateStatus(long id, String status) {
    try (Connection conn = DB.getConnection()) {
      String sql = "UPDATE requests SET status = ? WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, status);
      stmt.setLong(2, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Erreur mise à jour statut", e);
    }
  }

  public boolean delete(long id) {
    try (Connection conn = DB.getConnection()) {
      // Delete items first
      String sqlItems = "DELETE FROM request_items WHERE request_id = ?";
      PreparedStatement stmtItems = conn.prepareStatement(sqlItems);
      stmtItems.setLong(1, id);
      stmtItems.executeUpdate();
      
      // Then delete the request
      String sql = "DELETE FROM requests WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, id);
      int rowsAffected = stmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      throw new DatabaseException("Erreur suppression demande", e);
    }
  }

  public boolean deleteItem(long itemId) {
    try (Connection conn = DB.getConnection()) {
      String sql = "DELETE FROM request_items WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, itemId);
      int rowsAffected = stmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      throw new DatabaseException("Erreur suppression item", e);
    }
  }

  public void addItem(long requestId, String ref, int qty, String description) {
    try (Connection conn = DB.getConnection()) {
      String sql = "INSERT INTO request_items (request_id, reference, quantity, description) VALUES (?, ?, ?, ?)";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, requestId);
      stmt.setString(2, ref);
      stmt.setInt(3, qty);
      stmt.setString(4, description);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Erreur ajout item", e);
    }
  }

  public void updateItem(RequestItem item) {
    try (Connection conn = DB.getConnection()) {
      String sql = "UPDATE request_items SET reference = ?, quantity = ?, description = ? WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, item.ref());
      stmt.setInt(2, item.qty());
      stmt.setString(3, item.description());
      stmt.setLong(4, item.id());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Erreur mise à jour item", e);
    }
  }
}
