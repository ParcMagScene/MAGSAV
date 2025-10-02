package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Product;
import java.sql.*;
import java.util.*;

public final class ProductRepository {

  public long insert(String code, String nom, String sn, String fabricant, Long catId, Long subCatId) {
    String sql = "INSERT INTO produits(code, nom, sn, fabricant, categorie_id, sous_categorie_id) VALUES (?,?,?,?,?,?)";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, code);
      ps.setString(2, nom);
      ps.setString(3, sn);
      ps.setString(4, fabricant);
      if (catId == null) ps.setNull(5, Types.INTEGER); else ps.setLong(5, catId);
      if (subCatId == null) ps.setNull(6, Types.INTEGER); else ps.setLong(6, subCatId);
      ps.executeUpdate();
      try (ResultSet keys = ps.getGeneratedKeys()) {
        return keys.next() ? keys.getLong(1) : -1L;
      }
    } catch (SQLException e) {
      throw new RuntimeException("Insert product failed", e);
    }
  }

  private static Product map(ResultSet rs) throws SQLException {
    Long cat = rs.getObject("categorie_id") == null ? null : rs.getLong("categorie_id");
    Long sub = rs.getObject("sous_categorie_id") == null ? null : rs.getLong("sous_categorie_id");
    return new Product(
        rs.getLong("id"),
        rs.getString("code"),
        rs.getString("nom"),
        rs.getString("sn"),
        rs.getString("fabricant"),
        cat,
        sub,
        rs.getString("created_at"));
  }

  public Optional<Product> findById(long id) {
    String sql = "SELECT * FROM produits WHERE id = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find product failed", e);
    }
  }

  public Optional<Product> findBySN(String sn) {
    String sql = "SELECT * FROM produits WHERE sn = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, sn);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(map(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find product by SN failed", e);
    }
  }

  public List<Product> findAll() {
    String sql = "SELECT * FROM produits ORDER BY id DESC";
    try (Connection c = DB.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      List<Product> out = new ArrayList<>();
      while (rs.next()) out.add(map(rs));
      return out;
    } catch (SQLException e) {
      throw new RuntimeException("List products failed", e);
    }
  }

  public void update(Product p) {
    String sql = """
      UPDATE produits SET code=?, nom=?, sn=?, fabricant=?, categorie_id=?, sous_categorie_id=? WHERE id=?
      """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, p.code());
      ps.setString(2, p.nom());
      ps.setString(3, p.sn());
      ps.setString(4, p.fabricant());
      if (p.categorieId() == null) ps.setNull(5, Types.INTEGER); else ps.setLong(5, p.categorieId());
      if (p.sousCategorieId() == null) ps.setNull(6, Types.INTEGER); else ps.setLong(6, p.sousCategorieId());
      ps.setLong(7, p.id());
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Update product failed", e);
    }
  }

  public boolean delete(long id) {
    String sql = "DELETE FROM produits WHERE id = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RuntimeException("Delete product failed", e);
    }
  }
}