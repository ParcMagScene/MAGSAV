package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.ProductSituation;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

  public record ProductRow(long id, String nom, String sn, String fabricant, String uid, String situation) {}
  
  public record ProductRowDetailed(long id, String nom, String sn, String fabricant, String uid, 
                                   String situation, String photo, String category, String subcategory,
                                   String prix, String dateAchat, String client, String description, String garantie) {}

  private ProductRow mapRow(ResultSet rs) throws SQLException {
    return new ProductRow(
        rs.getLong("id"),
        rs.getString("nom_produit"),
        rs.getString("numero_serie"),
        rs.getString("nom_fabricant"), // Champ déprécié temporaire
        rs.getString("uid_unique"),
        rs.getString("statut_produit")
    );
  }
  
  private ProductRowDetailed mapDetailedRow(ResultSet rs) throws SQLException {
    return new ProductRowDetailed(
        rs.getLong("id"),
        rs.getString("nom_produit"),
        rs.getString("numero_serie"),
        rs.getString("nom_fabricant"), // Champ déprécié temporaire
        rs.getString("uid_unique"),
        rs.getString("statut_produit"),
        rs.getString("photo_produit"),
        rs.getString("categorie_principale"), // Champ déprécié temporaire
        rs.getString("sous_categorie"), // Champ déprécié temporaire
        rs.getString("prix_achat"),
        rs.getString("date_achat"),
        rs.getString("nom_client"), // Champ déprécié temporaire
        rs.getString("description_produit"),
        rs.getString("duree_garantie")
    );
  }

  public Optional<ProductRowDetailed> findDetailedById(long id) {
    String sql = "SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, photo_produit, categorie_principale, sous_categorie, prix_achat, date_achat, nom_client, description_produit, duree_garantie FROM produits WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(mapDetailedRow(rs)) : Optional.empty();
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findDetailedById failed", e); }
  }

  public Optional<ProductRow> findById(long id) {
    String sql = "SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit FROM produits WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findById failed", e); }
  }

  public Optional<ProductRow> findBySN(String sn) {
    String sql = "SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit FROM produits WHERE numero_serie=? LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, sn);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findBySN failed", e); }
  }

  public long insert(String nom, String sn, String fabricant, String uid, String situation) {
    // Valider et normaliser la situation
    String validatedSituation = validateAndNormalizeSituation(situation);
    
    String sql = "INSERT INTO produits(nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit) VALUES(?,?,?,?,?)";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, nom);
      ps.setString(2, sn);
      ps.setString(3, fabricant);
      ps.setString(4, uid);
      ps.setString(5, validatedSituation);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getLong(1) : 0L; }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("insert failed", e); }
  }

  public void updateUid(long id, String uid) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET uid_unique=? WHERE id=?")) {
      ps.setString(1, uid);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updateUid failed", e); }
  }

  public void updateSituation(long id, String situation) {
    // Valider et normaliser la situation
    String validatedSituation = validateAndNormalizeSituation(situation);
    
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET statut_produit=? WHERE id=?")) {
      ps.setString(1, validatedSituation);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updateSituation failed", e); }
  }

  public boolean existsUid(String uid) {
    String sql = "SELECT 1 FROM produits WHERE uid_unique=? LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, uid);
      try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("existsUid failed", e); }
  }

  public Optional<ProductRow> findByUid(String uid) {
    if (uid == null || uid.trim().isEmpty()) return Optional.empty();
    String sql = "SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit FROM produits WHERE uid_unique = ?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, uid);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findByUid failed", e); }
  }

  public Optional<ProductRow> findByInterventionId(long interventionId) {
    String sql = """
            SELECT p.id, p.code_produit, p.nom_produit, p.numero_serie, p.nom_fabricant, p.uid_unique, p.statut_produit
      FROM produits p
      JOIN interventions i ON p.id = i.produit_id
      WHERE i.id = ?
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, interventionId);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findByInterventionId failed", e); }
  }

  public List<String> listFabricants() {
    String sql = "SELECT DISTINCT nom_fabricant FROM produits WHERE COALESCE(TRIM(nom_fabricant),'')<>'' ORDER BY nom_fabricant";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("listFabricants failed", e); }
  }

  public void updateFabricantForSameNameByProduct(long productId, String fabricantName) {
    try (Connection c = DB.getConnection()) {
      String name = findNameById(c, productId).orElse(null);
      if (name == null || name.isBlank()) return;
      // Long fabId = null;
      // if (fabricantName != null && !fabricantName.isBlank()) {
      //   fabId = new SocieteRepository().upsertManufacturerByName(fabricantName.trim());
      //   // Méthode temporairement commentée - à implémenter dans SocieteRepository si nécessaire
      // }
      try (PreparedStatement ps = c.prepareStatement(
          "UPDATE produits SET nom_fabricant=?, fabricant_id=? WHERE UPPER(nom_produit)=UPPER(?)")) {
        ps.setString(1, fabricantName);
        ps.setNull(2, Types.INTEGER); // fabricant_id temporairement null
        ps.setString(3, name);
        ps.executeUpdate();
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updateFabricantForSameNameByProduct failed", e); }
  }

  public void updatePhotoForSameNameByProduct(long productId, String photoPath) {
    try (Connection c = DB.getConnection()) {
      String name = findNameById(c, productId).orElse(null);
      if (name == null || name.isBlank()) return;
      try (PreparedStatement ps = c.prepareStatement("UPDATE produits SET photo_produit=? WHERE UPPER(nom_produit)=UPPER(?)")) {
        ps.setString(1, photoPath);
        ps.setString(2, name);
        ps.executeUpdate();
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updatePhotoForSameNameByProduct failed", e); }
  }

  public void updateCategoryNamesForSameNameByProduct(long productId, String category, String subcategory) {
    try (Connection c = DB.getConnection()) {
      String name = findNameById(c, productId).orElse(null);
      if (name == null || name.isBlank()) return;
      try (PreparedStatement ps = c.prepareStatement(
          "UPDATE produits SET categorie_principale=?, sous_categorie=? WHERE UPPER(nom_produit)=UPPER(?)")) {
        ps.setString(1, category);
        ps.setString(2, subcategory);
        ps.setString(3, name);
        ps.executeUpdate();
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updateCategoryNamesForSameNameByProduct failed", e); }
  }

  public List<String> listDistinctCategories() {
    String sql = "SELECT DISTINCT category FROM produits WHERE COALESCE(TRIM(category),'')<>'' ORDER BY category";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("listDistinctCategories failed", e); }
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
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("listDistinctSubcategories failed", e); }
  }

  public List<ProductRow> findByFabricant(String fabricant) {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit
      FROM produits
      WHERE UPPER(nom_fabricant)=UPPER(?)
      ORDER BY nom_produit, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, fabricant);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductRow> out = new ArrayList<>();
        while (rs.next()) out.add(mapRow(rs));
        return out;
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findByFabricant failed", e); }
  }

  public List<com.magsav.model.ProductRow> findAllProducts() {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, statut_produit
      FROM produits
      ORDER BY nom_produit, id
    """;
    AppLogger.logSql("SELECT", "produits", "findAll");
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<com.magsav.model.ProductRow> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new com.magsav.model.ProductRow(
          rs.getLong("id"),
          rs.getString("nom_produit"),
          rs.getString("numero_serie"),
          rs.getString("nom_fabricant"),
          rs.getString("statut_produit")
        ));
      }
      AppLogger.debug("ProductRepository: {} produits chargés", out.size());
      return out;
    } catch (SQLException e) {
      System.err.println("DEBUG ProductRepository: ERREUR - " + e.getMessage());
      throw new com.magsav.exception.DatabaseException("findAllProducts failed", e);
    }
  }

  public List<ProductRow> findAllProductsWithUID() {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit
      FROM produits
      ORDER BY nom_produit, id
    """;
    AppLogger.logSql("SELECT", "produits", "findAllWithUID");
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<ProductRow> out = new ArrayList<>();
      while (rs.next()) {
        out.add(new ProductRow(
          rs.getLong("id"),
          rs.getString("nom_produit"),
          rs.getString("numero_serie"),
          rs.getString("nom_fabricant"),
          rs.getString("uid_unique"),
          rs.getString("statut_produit")
        ));
      }
      AppLogger.debug("ProductRepository: {} produits avec UID chargés", out.size());
      return out;
    } catch (SQLException e) {
      System.err.println("DEBUG ProductRepository: ERREUR - " + e.getMessage());
      throw new com.magsav.exception.DatabaseException("findAllProductsWithUID failed", e);
    }
  }

  private Optional<String> findNameById(Connection c, long id) throws SQLException {
    try (PreparedStatement ps = c.prepareStatement("SELECT nom_produit FROM produits WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.ofNullable(rs.getString(1)) : Optional.empty(); }
    }
  }

  // Méthodes manquantes essentielles pour le fonctionnement de l'application
  public List<ProductRowDetailed> findAllDetailed() {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, photo_produit, categorie_principale, sous_categorie, 
             prix_achat, date_achat, nom_client, description_produit, duree_garantie 
      FROM produits 
      ORDER BY nom_produit, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<ProductRowDetailed> out = new ArrayList<>();
      while (rs.next()) out.add(mapDetailedRow(rs));
      return out;
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findAllDetailed failed", e); }
  }

  public List<ProductRowDetailed> findAllVisible() {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, photo_produit, categorie_principale, sous_categorie, 
             prix_achat, date_achat, nom_client, description_produit, duree_garantie 
      FROM produits 
      WHERE statut_produit != 'Supprimé' 
      ORDER BY nom_produit, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<ProductRowDetailed> out = new ArrayList<>();
      while (rs.next()) out.add(mapDetailedRow(rs));
      return out;
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findAllVisible failed", e); }
  }

  public List<String> listDistinctFabricants() {
    return listFabricants();
  }

  public List<String> listDistinctSituations() {
    String sql = "SELECT DISTINCT statut_produit FROM produits WHERE COALESCE(TRIM(statut_produit),'')<>'' ORDER BY statut_produit";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("listDistinctSituations failed", e); }
  }

  public List<ProductRowDetailed> findBySituation(String situation) {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, photo_produit, categorie_principale, sous_categorie, 
             prix_achat, date_achat, nom_client, description_produit, duree_garantie 
      FROM produits 
      WHERE UPPER(statut_produit)=UPPER(?) 
      ORDER BY nom_produit, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, situation);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductRowDetailed> out = new ArrayList<>();
        while (rs.next()) out.add(mapDetailedRow(rs));
        return out;
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findBySituation failed", e); }
  }

  public List<ProductRowDetailed> findBySavExterne(long savExterneId) {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, photo_produit, categorie_principale, sous_categorie, 
             prix_achat, date_achat, nom_client, description_produit, duree_garantie 
      FROM produits 
      WHERE sav_externe_id=? 
      ORDER BY nom_produit, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, savExterneId);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductRowDetailed> out = new ArrayList<>();
        while (rs.next()) out.add(mapDetailedRow(rs));
        return out;
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findBySavExterne failed", e); }
  }

  public void updateFabricantById(long id, String fabricant) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET nom_fabricant=? WHERE id=?")) {
      ps.setString(1, fabricant);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updateFabricantById failed", e); }
  }

  public void updateCategoryNamesById(long id, String category, String subcategory) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET category=?, subcategory=? WHERE id=?")) {
      ps.setString(1, category);
      ps.setString(2, subcategory);
      ps.setLong(3, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updateCategoryNamesById failed", e); }
  }

  public void updatePhotoById(long id, String photo) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("UPDATE produits SET photo=? WHERE id=?")) {
      ps.setString(1, photo);
      ps.setLong(2, id);
      ps.executeUpdate();
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("updatePhotoById failed", e); }
  }

  // Versions compatibles qui retournent ProductRow pour les contrôleurs existants
  public List<ProductRow> findBySituationCompatible(String situation) {
    String sql = """
      SELECT id, nom, sn, fabricant, uid, situation 
      FROM produits 
      WHERE UPPER(situation)=UPPER(?) 
      ORDER BY nom, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, situation);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductRow> out = new ArrayList<>();
        while (rs.next()) out.add(mapRow(rs));
        return out;
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findBySituationCompatible failed", e); }
  }

  public List<ProductRow> findBySavExterneCompatible(long savExterneId) {
    String sql = """
      SELECT id, nom, sn, fabricant, uid, situation 
      FROM produits 
      WHERE sav_externe_id=? 
      ORDER BY nom, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, savExterneId);
      try (ResultSet rs = ps.executeQuery()) {
        List<ProductRow> out = new ArrayList<>();
        while (rs.next()) out.add(mapRow(rs));
        return out;
      }
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findBySavExterneCompatible failed", e); }
  }

  public List<ProductRow> findAllVisibleCompatible() {
    String sql = """
      SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit 
      FROM produits 
      WHERE statut_produit != 'Supprimé' 
      ORDER BY nom_produit, id
    """;
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<ProductRow> out = new ArrayList<>();
      while (rs.next()) out.add(mapRow(rs));
      return out;
    } catch (SQLException e) { throw new com.magsav.exception.DatabaseException("findAllVisibleCompatible failed", e); }
  }
  
  /**
   * Met à jour les URLs d'images scrapées pour un produit
   * Stocke les URLs séparées par des virgules dans le champ scraped_images
   */
  public void updateScrapedImages(long productId, List<String> imageUrls) {
    String urlsString = imageUrls.isEmpty() ? null : String.join(",", imageUrls);
    String sql = "UPDATE produits SET scraped_images = ? WHERE id = ?";
    
    try (Connection c = DB.getConnection(); 
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, urlsString);
      ps.setLong(2, productId);
      int updated = ps.executeUpdate();
      
      if (updated > 0) {
        AppLogger.info("database", "Images scrapées mises à jour pour produit ID " + productId + 
                      ": " + imageUrls.size() + " URLs");
      }
    } catch (SQLException e) { 
      throw new com.magsav.exception.DatabaseException("updateScrapedImages failed", e); 
    }
  }
  
  /**
   * Met à jour les URLs d'images scrapées pour un produit par UID
   */
  public void updateScrapedImagesByUid(String productUid, List<String> imageUrls) {
    String urlsString = imageUrls.isEmpty() ? null : String.join(",", imageUrls);
    String sql = "UPDATE produits SET scraped_images = ? WHERE uid = ?";
    
    try (Connection c = DB.getConnection(); 
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, urlsString);
      ps.setString(2, productUid);
      int updated = ps.executeUpdate();
      
      if (updated > 0) {
        AppLogger.info("database", "Images scrapées mises à jour pour produit UID " + productUid + 
                      ": " + imageUrls.size() + " URLs");
      }
    } catch (SQLException e) { 
      throw new com.magsav.exception.DatabaseException("updateScrapedImagesByUid failed", e); 
    }
  }
  
  /**
   * Récupère les URLs d'images scrapées pour un produit
   */
  public List<String> getScrapedImages(long productId) {
    String sql = "SELECT scraped_images FROM produits WHERE id = ?";
    
    try (Connection c = DB.getConnection(); 
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setLong(1, productId);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String urlsString = rs.getString("scraped_images");
          if (urlsString != null && !urlsString.trim().isEmpty()) {
            return Arrays.asList(urlsString.split(","));
          }
        }
        return new ArrayList<>();
      }
    } catch (SQLException e) { 
      throw new com.magsav.exception.DatabaseException("getScrapedImages failed", e); 
    }
  }
  
  /**
   * Récupère les URLs d'images scrapées pour un produit par UID
   */
  public List<String> getScrapedImagesByUid(String productUid) {
    String sql = "SELECT scraped_images FROM produits WHERE uid_unique = ?";
    
    try (Connection c = DB.getConnection(); 
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, productUid);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String urlsString = rs.getString("scraped_images");
          if (urlsString != null && !urlsString.trim().isEmpty()) {
            return Arrays.asList(urlsString.split(","));
          }
        }
        return new ArrayList<>();
      }
    } catch (SQLException e) { 
      throw new com.magsav.exception.DatabaseException("getScrapedImagesByUid failed", e); 
    }
  }
  
  /**
   * Trouve tous les produits sans images scrapées
   */
  public List<ProductRowDetailed> findProductsWithoutScrapedImages() {
    String sql = "SELECT id, nom_produit, numero_serie, nom_fabricant, uid_unique, statut_produit, photo_produit, " +
                "categorie_principale, sous_categorie, prix_achat, date_achat, nom_client, description_produit, duree_garantie " +
                "FROM produits WHERE scraped_images IS NULL OR scraped_images = ''";
    
    List<ProductRowDetailed> products = new ArrayList<>();
    try (Connection c = DB.getConnection(); 
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      
      while (rs.next()) {
        products.add(mapDetailedRow(rs));
      }
      return products;
    } catch (SQLException e) { 
      throw new com.magsav.exception.DatabaseException("findProductsWithoutScrapedImages failed", e); 
    }
  }
  
  /**
   * Compte les produits avec/sans images scrapées
   */
  public record ImageStats(int withImages, int withoutImages, int total) {}
  
  public ImageStats getImageStats() {
    String sql = "SELECT " +
                "COUNT(CASE WHEN scraped_images IS NOT NULL AND scraped_images != '' THEN 1 END) as with_images, " +
                "COUNT(CASE WHEN scraped_images IS NULL OR scraped_images = '' THEN 1 END) as without_images, " +
                "COUNT(*) as total " +
                "FROM produits";
    
    try (Connection c = DB.getConnection(); 
         PreparedStatement ps = c.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
      
      if (rs.next()) {
        return new ImageStats(
          rs.getInt("with_images"),
          rs.getInt("without_images"),
          rs.getInt("total")
        );
      }
      return new ImageStats(0, 0, 0);
    } catch (SQLException e) { 
      throw new com.magsav.exception.DatabaseException("getImageStats failed", e); 
    }
  }
  
  /**
   * Valide et normalise une situation de produit.
   * - null devient "En stock" (valeur par défaut)
   * - Situations valides sont acceptées tel quel
   * - Situations invalides lèvent une IllegalArgumentException
   */
  private String validateAndNormalizeSituation(String situation) {
    if (situation == null) {
      return ProductSituation.EN_STOCK.getLabel();
    }
    
    if (!ProductSituation.isValid(situation)) {
      throw new IllegalArgumentException("Situation non valide: " + situation + 
        ". Situations autorisées: " + String.join(", ", ProductSituation.getAllLabels()));
    }
    
    return situation;
  }
  
  /**
   * Compte le nombre de produits dans une catégorie donnée
   */
  public int getProductCountByCategory(long categoryId) {
    // Note: La table produits utilise categorie_principale (TEXT) pas categorieId (INTEGER)
    // Il faudrait d'abord récupérer le nom de la catégorie depuis l'ID
    String sql = "SELECT COUNT(*) FROM produits p JOIN categories c ON p.categorie_principale = c.nom_categorie WHERE c.id = ?";
    
    try (Connection conn = DB.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      
      stmt.setLong(1, categoryId);
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
        return 0;
      }
      
    } catch (SQLException e) {
      AppLogger.error("Erreur lors du comptage des produits pour la catégorie " + categoryId, e);
      return 0;
    }
  }
  
  /**
   * Compte le nombre total de produits
   */
  public int getTotalProductCount() {
    String sql = "SELECT COUNT(*) FROM produits";
    
    try (Connection conn = DB.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1);
        }
        return 0;
      }
      
    } catch (SQLException e) {
      AppLogger.error("Erreur lors du comptage total des produits", e);
      return 0;
    }
  }
}
