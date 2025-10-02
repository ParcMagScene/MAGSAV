package com.magsav.repo;

import com.magsav.model.Category;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class CategoryRepository {
  private final DataSource ds;

  public CategoryRepository(DataSource ds) {
    this.ds = ds;
  }

  public Category save(Category c) throws SQLException {
    if (c.id() == null) {
      return insert(c);
    }
    return update(c);
  }

  private Category insert(Category c) throws SQLException {
    String sql = "INSERT INTO categories(name, parent_id) VALUES(?,?)";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, c.name());
      if (c.parentId() != null) {
        ps.setLong(2, c.parentId());
      } else {
        ps.setNull(2, Types.INTEGER);
      }
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return c.withId(rs.getLong(1));
        }
        throw new SQLException("Failed to get generated ID");
      }
    }
  }

  private Category update(Category c) throws SQLException {
    String sql = "UPDATE categories SET name=?, parent_id=? WHERE id=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, c.name());
      if (c.parentId() != null) {
        ps.setLong(2, c.parentId());
      } else {
        ps.setNull(2, Types.INTEGER);
      }
      ps.setLong(3, c.id());
      ps.executeUpdate();
      return c;
    }
  }

  public void delete(Long id) throws SQLException {
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement("DELETE FROM categories WHERE id=?")) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }

  public Category findById(Long id) throws SQLException {
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement("SELECT id,name,parent_id FROM categories WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Number parent = (Number) rs.getObject(3);
          Long parentId = parent != null ? parent.longValue() : null;
          return new Category(rs.getLong(1), rs.getString(2), parentId);
        }
        return null;
      }
    }
  }

  public List<Category> findAll() throws SQLException {
    List<Category> out = new ArrayList<>();
    try (Connection conn = ds.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT id,name,parent_id FROM categories ORDER BY name")) {
      while (rs.next()) {
        Number parent = (Number) rs.getObject(3);
        Long parentId = parent != null ? parent.longValue() : null;
        out.add(new Category(rs.getLong(1), rs.getString(2), parentId));
      }
    }
    return out;
  }

  public List<Category> findRoots() throws SQLException {
    List<Category> out = new ArrayList<>();
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT id,name,parent_id FROM categories WHERE parent_id IS NULL ORDER BY name")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Number parent = (Number) rs.getObject(3);
          Long parentId = parent != null ? parent.longValue() : null;
          out.add(new Category(rs.getLong(1), rs.getString(2), parentId));
        }
      }
    }
    return out;
  }

  public List<Category> findChildren(Long parentId) throws SQLException {
    List<Category> out = new ArrayList<>();
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT id,name,parent_id FROM categories WHERE parent_id=? ORDER BY name")) {
      ps.setLong(1, parentId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          Number parent = (Number) rs.getObject(3);
          Long pId = parent != null ? parent.longValue() : null;
          out.add(new Category(rs.getLong(1), rs.getString(2), pId));
        }
      }
    }
    return out;
  }
}
