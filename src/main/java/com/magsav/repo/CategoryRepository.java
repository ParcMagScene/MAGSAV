package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepository {

  private static Category map(ResultSet rs) throws SQLException {
    long id = rs.getLong("id");
    String nom = rs.getString("nom");
    Object pid = rs.getObject("parent_id");
    Long parentId = (pid == null) ? null : rs.getLong("parent_id");
    return new Category(id, nom, parentId);
  }

  public List<Category> findAll() {
    String sql = "SELECT id, nom, parent_id FROM categories ORDER BY nom";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<Category> out = new ArrayList<>();
      while (rs.next()) out.add(map(rs));
      return out;
    } catch (SQLException e) { throw new RuntimeException("findAll failed", e); }
  }

  public Optional<Category> findById(Long id) {
    if (id == null) return Optional.empty();
    String sql = "SELECT id, nom, parent_id FROM categories WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findById failed", e); }
  }

  public long insert(String nom, Long parentId) {
    String sql = "INSERT INTO categories(nom, parent_id) VALUES(?, ?)";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, nom);
      if (parentId == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, parentId);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getLong(1) : -1L; }
    } catch (SQLException e) { throw new RuntimeException("insert failed", e); }
  }

  public boolean update(Category c) {
    String sql = "UPDATE categories SET nom=?, parent_id=? WHERE id=?";
    try (Connection cx = DB.getConnection(); PreparedStatement ps = cx.prepareStatement(sql)) {
      ps.setString(1, c.nom());
      if (c.parentId() == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, c.parentId());
      ps.setLong(3, c.id());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("update failed", e); }
  }

  public boolean delete(long id) {
    String sql = "DELETE FROM categories WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("delete failed", e); }
  }

  public List<Category> findSubcategories(long parentId) {
    String sql = "SELECT id, nom, parent_id FROM categories WHERE parent_id=? ORDER BY nom";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, parentId);
      try (ResultSet rs = ps.executeQuery()) {
        List<Category> out = new ArrayList<>();
        while (rs.next()) out.add(map(rs));
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("findSubcategories failed", e); }
  }

  public long insertSubcategory(long parentId, String nom) {
    return insert(nom, parentId);
  }

  // Alias pour compatibilité avec vos contrôleurs existants
  public List<Category> findAllCategories() { return findAll(); }
  public long insertCategory(String nom) { return insert(nom, null); }
}