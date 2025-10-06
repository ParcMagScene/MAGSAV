package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

  public record ProductRow(long id, String code, String nom, String sn, String fabricant, String uid, String situation) {}
  
  public record ProductRowDetailed(long id, String code, String nom, String sn, String fabricant, String uid, 
                                   String situation, String photo, String category, String subcategory) {}

  private ProductRow mapRow(ResultSet rs) throws SQLException {
    return new ProductRow(
        rs.getLong("id"),
        rs.getString("code"),
        rs.getString("nom"),
        rs.getString("sn"),
        rs.getString("fabricant"),
        rs.getString("uid"),
        rs.getString("situation")
    );
  }
  
  private ProductRowDetailed mapDetailedRow(ResultSet rs) throws SQLException {
    return new ProductRowDetailed(
        rs.getLong("id"),
        rs.getString("code"),
        rs.getString("nom"),
        rs.getString("sn"),
        rs.getString("fabricant"),
        rs.getString("uid"),
        rs.getString("situation"),
        rs.getString("photo"),
        rs.getString("category"),
        rs.getString("subcategory")
    );
  }

  public Optional<ProductRowDetailed> findDetailedById(long id) {
    String sql = "SELECT id, code, nom, sn, fabricant, uid, situation, photo, category, subcategory FROM produits WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapDetailedRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findDetailedById failed", e); }
  }

  public Optional<ProductRow> findById(long id) {
    String sql = "SELECT id, code, nom, sn, fabricant, uid, situation FROM produits WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findById failed", e); }
  }

  public Optional<ProductRow> findBySN(String sn) {
    String sql = "SELECT id, code, nom, sn, fabricant, uid, situation FROM produits WHERE sn=? LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, sn);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findBySN failed", e); }
  }

  public long insert(String code, String nom, String sn, String fabricant, String uid, String situation) {
    String sql = "INSERT INTO produits(code, nom, sn, fabricant, uid, situation) VALUES(?,?,?,?,?,?)";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, code);
      ps.setString(2, nom);
      ps.setString(3, sn);
      ps.setString(4, fabricant);
      ps.setString(5, uid);
      ps.setString(6, situation);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getLong(1) : 0L; }
    } catch (SQLException e) { throw new RuntimeException("insert failed", e); }
  }

  public void updateUid(long id, String uid) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET uid=? WHERE id=?")) {
      ps.setString(1, uid);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException("updateUid failed", e); }
  }

  public void updateSituation(long id, String situation) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET situation=? WHERE id=?")) {
      ps.setString(1, situation);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new RuntimeException("updateSituation failed", e); }
  }

  public boolean existsUid(String uid) {
    String sql = "SELECT 1 FROM produits WHERE uid=? LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, uid);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    } catch (SQLException e) { throw new RuntimeException("existsUid failed", e); }
  }

  public Optional<ProductRow> findByInterventionId(long interventionId) {
    String sql = """
      SELECT p.id, p.code, p.nom, p.sn, p.fabricant, p.uid, p.situation
      FROM produits p JOIN interventions i ON i.product_id = p.id
      WHERE i.id = ? LIMIT 1
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, interventionId);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findByInterventionId failed", e); }
  }

  public List<String> listFabricants() {
    String sql = "SELECT DISTINCT fabricant FROM produits WHERE COALESCE(TRIM(fabricant),'')<>'' ORDER BY fabricant";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) { throw new RuntimeException("listFabricants failed", e); }
  }

  public void updateFabricantForSameNameByProduct(long productId, String fabricantName) {
    try (Connection c = DB.getConnection()) {
      String name = findNameById(c, productId).orElse(null);
      if (name == null || name.isBlank()) return;
      Long fabId = null;
      if (fabricantName != null && !fabricantName.isBlank()) {
        fabId = new SocieteRepository().upsertManufacturerByName(fabricantName.trim());
      }
      try (PreparedStatement ps = c.prepareStatement(
          "UPDATE produits SET fabricant=?, fabricant_id=? WHERE UPPER(nom)=UPPER(?)")) {
        ps.setString(1, fabricantName);
        if (fabId == null) ps.setNull(2, Types.INTEGER); else ps.setLong(2, fabId);
        ps.setString(3, name);
        ps.executeUpdate();
      }
    } catch (SQLException e) { throw new RuntimeException("updateFabricantForSameNameByProduct failed", e); }
  }

  public void updatePhotoForSameNameByProduct(long productId, String photoPath) {
    try (Connection c = DB.getConnection()) {
      String name = findNameById(c, productId).orElse(null);
      if (name == null || name.isBlank()) return;
      try (PreparedStatement ps = c.prepareStatement("UPDATE produits SET photo=? WHERE UPPER(nom)=UPPER(?)")) {
        ps.setString(1, photoPath);
        ps.setString(2, name);
        ps.executeUpdate();
      }
    } catch (SQLException e) { throw new RuntimeException("updatePhotoForSameNameByProduct failed", e); }
  }

  public void updateCategoryNamesForSameNameByProduct(long productId, String category, String subcategory) {
    try (Connection c = DB.getConnection()) {
      String name = findNameById(c, productId).orElse(null);
      if (name == null || name.isBlank()) return;
      try (PreparedStatement ps = c.prepareStatement(
          "UPDATE produits SET category=?, subcategory=? WHERE UPPER(nom)=UPPER(?)")) {
        ps.setString(1, category);
        ps.setString(2, subcategory);
        ps.setString(3, name);
        ps.executeUpdate();
      }
    } catch (SQLException e) { throw new RuntimeException("updateCategoryNamesForSameNameByProduct failed", e); }
  }

  public List<String> listDistinctCategories() {
    String sql = "SELECT DISTINCT category FROM produits WHERE COALESCE(TRIM(category),'')<>'' ORDER BY category";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) { throw new RuntimeException("listDistinctCategories failed", e); }
  }

  public List<String> listDistinctSubcategories(String category) {
    String sql = """
      SELECT DISTINCT subcategory FROM produits
      WHERE COALESCE(TRIM(subcategory),'')<>''
        AND (? IS NULL OR UPPER(category)=UPPER(?))
      ORDER BY subcategory
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      if (category == null || category.isBlank()) {
        ps.setNull(1, Types.VARCHAR); ps.setNull(2, Types.VARCHAR);
      } else {
        ps.setString(1, category); ps.setString(2, category);
      }
      try (ResultSet rs = ps.executeQuery()) {
        List<String> out = new ArrayList<>();
        while (rs.next()) out.add(rs.getString(1));
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("listDistinctSubcategories failed", e); }
  }

  public List<ProductRow> findByFabricant(String fabricant) {
    String sql = """
      SELECT id, code, nom, sn, fabricant, uid, situation
      FROM produits
      WHERE UPPER(fabricant)=UPPER(?)
      ORDER BY nom, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, fabricant);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductRow> out = new ArrayList<>();
        while (rs.next()) out.add(mapRow(rs));
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("findByFabricant failed", e); }
  }

  public List<com.magsav.model.ProductRow> findAllProducts() {
    String sql = """
      SELECT id, code, nom, sn, fabricant, situation
      FROM produits
      ORDER BY nom, id
    """;
    AppLogger.logSql("SELECT", "produits", "findAll");
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<com.magsav.model.ProductRow> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new com.magsav.model.ProductRow(
          rs.getLong("id"),
          rs.getString("nom"),
          rs.getString("code"),
          rs.getString("sn"),
          rs.getString("fabricant"),
          rs.getString("situation")
        ));
      }
      AppLogger.debug("ProductRepository: {} produits chargés", out.size());
      return out;
    } catch (SQLException e) {
      System.err.println("DEBUG ProductRepository: ERREUR - " + e.getMessage());
      throw new RuntimeException("findAllProducts failed", e);
    }
  }

  public List<ProductRow> findAllProductsWithUID() {
    String sql = """
      SELECT id, code, nom, sn, fabricant, uid, situation
      FROM produits
      ORDER BY nom, id
    """;
    AppLogger.logSql("SELECT", "produits", "findAllWithUID");
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<ProductRow> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new ProductRow(
          rs.getLong("id"),
          rs.getString("code"),
          rs.getString("nom"),
          rs.getString("sn"),
          rs.getString("fabricant"),
          rs.getString("uid"),
          rs.getString("situation")
        ));
      }
      AppLogger.debug("ProductRepository: {} produits avec UID chargés", out.size());
      return out;
    } catch (SQLException e) {
      System.err.println("DEBUG ProductRepository: ERREUR - " + e.getMessage());
      throw new RuntimeException("findAllProductsWithUID failed", e);
    }
  }

  private Optional<String> findNameById(Connection c, long id) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("SELECT nom FROM produits WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty(); }
    }
  }
}
