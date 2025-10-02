package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Category;
import java.sql.*;
import java.util.*;

public final class CategoryRepository {

  public long insert(String nom, Long parentId) {
    String sql = "INSERT INTO categories(nom, parent_id) VALUES (?,?)";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, nom);
      if (parentId == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, parentId);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        return keys.next() ? keys.getLong(1) : -1L;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Insert category failed", e);
    }
  }

  public Optional<Category> findById(long id) {
    String sql = "SELECT id, nom, parent_id FROM categories WHERE id = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Long parent = rs.getObject("parent_id") == null ? null : rs.getLong("parent_id");
          return Optional.of(new Category(rs.getLong("id"), rs.getString("nom"), parent));
        }
        return Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find category failed", e);
    }
  }

  public List<Category> findAll() {
    String sql = "SELECT id, nom, parent_id FROM categories ORDER BY nom";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<Category> out = new ArrayList<>();
      while (rs.next()) {
        Long parent = rs.getObject("parent_id") == null ? null : rs.getLong("parent_id");
        out.add(new Category(rs.getLong("id"), rs.getString("nom"), parent));
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("List categories failed", e);
    }
  }

  public void update(Category c1) {
    String sql = "UPDATE categories SET nom = ?, parent_id = ? WHERE id = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, c1.nom());
      if (c1.parentId() == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, c1.parentId());
      ps.setLong(3, c1.id());
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Update category failed", e);
    }
  }

  public boolean delete(long id) {
    String sql = "DELETE FROM categories WHERE id = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Delete category failed", e);
    }
  }
}