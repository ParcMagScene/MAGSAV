package com.magsav.repo;

import com.magsav.model.DossierSAV;
import com.magsav.model.ProductSummary;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class DossierSAVRepository {
  private final DataSource ds;

  public DossierSAVRepository(DataSource ds) {
    this.ds = ds;
  }

  public DossierSAV save(DossierSAV dossier) throws SQLException {
    if (dossier.getId() == null) {
      return insert(dossier);
    } else {
      return update(dossier);
    }
  }

  public DossierSAV upsertBySerieProprietaire(DossierSAV dossier) throws SQLException {
    try (Connection conn = ds.getConnection()) {
      // Essayer mise à jour d'abord
      String update =
          "UPDATE dossiers_sav SET produit=?, panne=?, statut=?, detecteur=?, date_entree=?, date_sortie=? WHERE numero_serie=? AND proprietaire=?";
      try (PreparedStatement ps = conn.prepareStatement(update)) {
        ps.setString(1, dossier.getProduit());
        ps.setString(2, dossier.getPanne());
        ps.setString(3, dossier.getStatut());
        ps.setString(4, dossier.getDetecteur());
        ps.setString(
            5, dossier.getDateEntree() != null ? dossier.getDateEntree().toString() : null);
        ps.setString(
            6, dossier.getDateSortie() != null ? dossier.getDateSortie().toString() : null);
        ps.setString(7, dossier.getNumeroSerie());
        ps.setString(8, dossier.getProprietaire());

        int count = ps.executeUpdate();
        if (count > 0) {
          return findBySerieProprietaire(dossier.getNumeroSerie(), dossier.getProprietaire());
        }
      }

      // Sinon insérer
      return insert(dossier);
    }
  }

  private DossierSAV insert(DossierSAV dossier) throws SQLException {
    String sql =
        "INSERT INTO dossiers_sav(code, produit, numero_serie, proprietaire, panne, statut, detecteur, date_entree, date_sortie, category_id, subcategory_id) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      // S'assurer que le produit existe et possède un code au niveau produit
      ensureProductWithCode(conn, dossier.getNumeroSerie(), dossier.getProduit());

      // Conserver un code unique par intervention (contrainte UNIQUE),
      // mais l'affichage/recherche utilisera le code du produit via jointure.
      String code = dossier.getCode();
      if (code == null || code.isBlank()) {
        code = generateUniqueCode(conn);
      }
      ps.setString(1, code);
      ps.setString(2, dossier.getProduit());
      if (dossier.getNumeroSerie() == null || dossier.getNumeroSerie().isBlank()) {
        ps.setNull(3, Types.VARCHAR);
      } else {
        ps.setString(3, dossier.getNumeroSerie());
      }
      ps.setString(4, dossier.getProprietaire());
      ps.setString(5, dossier.getPanne());
      ps.setString(6, dossier.getStatut());
      ps.setString(7, dossier.getDetecteur());
      ps.setString(
          8,
          dossier.getDateEntree() != null
              ? dossier.getDateEntree().toString()
              : LocalDate.now().toString());
      ps.setString(9, dossier.getDateSortie() != null ? dossier.getDateSortie().toString() : null);
      if (dossier.getCategoryId() != null) {
        ps.setLong(10, dossier.getCategoryId());
      } else {
        ps.setNull(10, Types.INTEGER);
      }
      if (dossier.getSubcategoryId() != null) {
        ps.setLong(11, dossier.getSubcategoryId());
      } else {
        ps.setNull(11, Types.INTEGER);
      }

      ps.executeUpdate();

      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return dossier.withId(rs.getLong(1));
        }
        throw new SQLException("Failed to get generated ID");
      }
    }
  }

  /**
   * Recherche globale interventions par terme (produit, SN, code produit, fabricant, catégorie,
   * sous-catégorie).
   */
  public List<DossierSAV> searchInterventionsByTerm(String term) throws SQLException {
    String like = "%" + term.toLowerCase() + "%";
    String sql =
        ""
            + "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id "
            + "FROM dossiers_sav d "
            + "LEFT JOIN produits p ON p.numero_serie = d.numero_serie "
            + "LEFT JOIN manufacturers m ON m.id = p.manufacturer_id "
            + "LEFT JOIN categories c ON c.id = d.category_id "
            + "LEFT JOIN categories sc ON sc.id = d.subcategory_id "
            + "WHERE lower(d.produit) LIKE ? OR lower(d.numero_serie) LIKE ? OR lower(COALESCE(p.code,'')) LIKE ? "
            + "   OR lower(COALESCE(m.name,'')) LIKE ? OR lower(COALESCE(c.name,'')) LIKE ? OR lower(COALESCE(sc.name,'')) LIKE ? "
            + "ORDER BY date(d.date_entree) DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 1; i <= 6; i++) {
        ps.setString(i, like);
      }
      List<DossierSAV> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  // Pas de méthode additionnelle; le code produit est utilisé via jointure dans les SELECT

  public DossierSAV update(DossierSAV dossier) throws SQLException {
    String sql =
        "UPDATE dossiers_sav SET produit=?, panne=?, statut=?, detecteur=?, date_sortie=?, category_id=?, subcategory_id=? WHERE id=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, dossier.getProduit());
      ps.setString(2, dossier.getPanne());
      ps.setString(3, dossier.getStatut());
      ps.setString(4, dossier.getDetecteur());
      ps.setString(5, dossier.getDateSortie() != null ? dossier.getDateSortie().toString() : null);
      if (dossier.getCategoryId() != null) {
        ps.setLong(6, dossier.getCategoryId());
      } else {
        ps.setNull(6, Types.INTEGER);
      }
      if (dossier.getSubcategoryId() != null) {
        ps.setLong(7, dossier.getSubcategoryId());
      } else {
        ps.setNull(7, Types.INTEGER);
      }
      ps.setLong(8, dossier.getId());

      ps.executeUpdate();
      return dossier;
    }
  }

  public DossierSAV findById(Long id) throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.id=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapFromResultSet(rs);
        }
        return null;
      }
    }
  }

  public DossierSAV findBySerieProprietaire(String numeroSerie, String proprietaire)
      throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.numero_serie=? AND d.proprietaire=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, numeroSerie);
      ps.setString(2, proprietaire);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapFromResultSet(rs);
        }
        return null;
      }
    }
  }

  public List<DossierSAV> findByStatut(String statut) throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.statut=? ORDER BY d.date_entree DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, statut);
      List<DossierSAV> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  public List<DossierSAV> findByNumeroSerie(String numeroSerie) throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.numero_serie LIKE ? ORDER BY d.date_entree DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, "%" + numeroSerie + "%");
      List<DossierSAV> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  /**
   * Récupérer l'historique complet des interventions pour un produit identifié par son numéro de
   * série exact.
   */
  public List<DossierSAV> findAllByNumeroSerieExact(String numeroSerie) throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.numero_serie = ? ORDER BY d.date_entree DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, numeroSerie);
      List<DossierSAV> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  public List<DossierSAV> findByProprietaire(String proprietaire) throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE d.proprietaire LIKE ? ORDER BY d.date_entree DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, "%" + proprietaire + "%");
      List<DossierSAV> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  public List<DossierSAV> findAll() throws SQLException {
    String sql =
        "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie ORDER BY d.date_entree DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      List<DossierSAV> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  /** Recherche interventions avec filtres multiples. */
  public List<DossierSAV> searchInterventions(
      String produit,
      String numeroSerie,
      String code,
      Long categoryId,
      Long subcategoryId,
      String statut,
      String dateEntreeFrom,
      String dateEntreeTo,
      String dateSortieFrom,
      String dateSortieTo)
      throws SQLException {
    StringBuilder sb =
        new StringBuilder(
            "SELECT d.id, COALESCE(p.code, d.code) AS code, d.produit, d.numero_serie, d.proprietaire, d.panne, d.statut, d.detecteur, d.date_entree, d.date_sortie, d.category_id, d.subcategory_id FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE 1=1");
    List<Object> params = new ArrayList<>();
    if (produit != null && !produit.isBlank()) {
      sb.append(" AND d.produit LIKE ?");
      params.add('%' + produit + '%');
    }
    if (numeroSerie != null && !numeroSerie.isBlank()) {
      sb.append(" AND d.numero_serie LIKE ?");
      params.add('%' + numeroSerie + '%');
    }
    if (code != null && !code.isBlank()) {
      sb.append(" AND p.code LIKE ?");
      params.add('%' + code + '%');
    }
    if (categoryId != null) {
      sb.append(" AND d.category_id = ?");
      params.add(categoryId);
    }
    if (subcategoryId != null) {
      sb.append(" AND d.subcategory_id = ?");
      params.add(subcategoryId);
    }
    if (statut != null && !statut.isBlank()) {
      sb.append(" AND d.statut = ?");
      params.add(statut);
    }
    if (dateEntreeFrom != null && !dateEntreeFrom.isBlank()) {
      sb.append(" AND date(d.date_entree) >= date(?)");
      params.add(dateEntreeFrom);
    }
    if (dateEntreeTo != null && !dateEntreeTo.isBlank()) {
      sb.append(" AND date(d.date_entree) <= date(?)");
      params.add(dateEntreeTo);
    }
    if (dateSortieFrom != null && !dateSortieFrom.isBlank()) {
      sb.append(" AND date(d.date_sortie) >= date(?)");
      params.add(dateSortieFrom);
    }
    if (dateSortieTo != null && !dateSortieTo.isBlank()) {
      sb.append(" AND date(d.date_sortie) <= date(?)");
      params.add(dateSortieTo);
    }
    sb.append(" ORDER BY d.date_entree DESC");
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sb.toString())) {
      for (int i = 0; i < params.size(); i++) {
        ps.setObject(i + 1, params.get(i));
      }
      List<DossierSAV> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  /** Agrégation par produit + numéro de série pour le cadre Produits. */
  public List<ProductSummary> searchProducts(
      String produit,
      String numeroSerie,
      String code,
      Long categoryId,
      Long subcategoryId,
      String statut)
      throws SQLException {
    StringBuilder sb = new StringBuilder();
    sb.append(
        "SELECT d.produit, d.numero_serie, COUNT(*) AS cnt, MAX(date(d.date_entree)) AS lastIn, MAX(date(d.date_sortie)) AS lastOut FROM dossiers_sav d LEFT JOIN produits p ON p.numero_serie = d.numero_serie WHERE 1=1");
    List<Object> params = new ArrayList<>();
    if (produit != null && !produit.isBlank()) {
      sb.append(" AND d.produit LIKE ?");
      params.add('%' + produit + '%');
    }
    if (numeroSerie != null && !numeroSerie.isBlank()) {
      sb.append(" AND d.numero_serie LIKE ?");
      params.add('%' + numeroSerie + '%');
    }
    if (code != null && !code.isBlank()) {
      sb.append(" AND p.code LIKE ?");
      params.add('%' + code + '%');
    }
    if (categoryId != null) {
      sb.append(" AND d.category_id = ?");
      params.add(categoryId);
    }
    if (subcategoryId != null) {
      sb.append(" AND d.subcategory_id = ?");
      params.add(subcategoryId);
    }
    if (statut != null && !statut.isBlank()) {
      sb.append(" AND d.statut = ?");
      params.add(statut);
    }
    sb.append(" GROUP BY d.produit, d.numero_serie ORDER BY lastIn DESC NULLS LAST");
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sb.toString())) {
      for (int i = 0; i < params.size(); i++) {
        ps.setObject(i + 1, params.get(i));
      }
      List<ProductSummary> out = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String prod = rs.getString(1);
          String sn = rs.getString(2);
          long cnt = rs.getLong(3);
          String sIn = rs.getString(4);
          String sOut = rs.getString(5);
          java.time.LocalDate in = parseLocalDateSafely(sIn);
          java.time.LocalDate outDate = parseLocalDateSafely(sOut);
          out.add(new ProductSummary(prod, sn, cnt, in, outDate));
        }
      }
      return out;
    }
  }

  /**
   * Recherche globale produits par terme (produit, SN, code produit, fabricant, catégorie,
   * sous-catégorie).
   */
  public List<ProductSummary> searchProductsByTerm(String term) throws SQLException {
    String like = "%" + term.toLowerCase() + "%";
    String sql =
        ""
            + "SELECT d.produit, d.numero_serie, COUNT(*) AS cnt, MAX(date(d.date_entree)) AS lastIn, MAX(date(d.date_sortie)) AS lastOut "
            + "FROM dossiers_sav d "
            + "LEFT JOIN produits p ON p.numero_serie = d.numero_serie "
            + "LEFT JOIN manufacturers m ON m.id = p.manufacturer_id "
            + "LEFT JOIN categories c ON c.id = d.category_id "
            + "LEFT JOIN categories sc ON sc.id = d.subcategory_id "
            + "WHERE lower(d.produit) LIKE ? OR lower(d.numero_serie) LIKE ? OR lower(COALESCE(p.code,'')) LIKE ? "
            + "   OR lower(COALESCE(m.name,'')) LIKE ? OR lower(COALESCE(c.name,'')) LIKE ? OR lower(COALESCE(sc.name,'')) LIKE ? "
            + "GROUP BY d.produit, d.numero_serie ORDER BY lastIn DESC NULLS LAST";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      for (int i = 1; i <= 6; i++) {
        ps.setString(i, like);
      }
      List<ProductSummary> out = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          String prod = rs.getString(1);
          String sn = rs.getString(2);
          long cnt = rs.getLong(3);
          String sIn = rs.getString(4);
          String sOut = rs.getString(5);
          java.time.LocalDate in = parseLocalDateSafely(sIn);
          java.time.LocalDate outDate = parseLocalDateSafely(sOut);
          out.add(new ProductSummary(prod, sn, cnt, in, outDate));
        }
      }
      return out;
    }
  }

  /** Suggestions de recherche globales (limitées) */
  public List<String> searchSuggestions(String term, int limit) throws SQLException {
    String like = "%" + term.toLowerCase() + "%";
    List<String> out = new ArrayList<>();
    try (Connection conn = ds.getConnection()) {
      // Produits
      try (PreparedStatement ps =
          conn.prepareStatement(
              "SELECT DISTINCT d.produit FROM dossiers_sav d WHERE lower(d.produit) LIKE ? AND d.produit IS NOT NULL AND TRIM(d.produit)<>'' LIMIT ?")) {
        ps.setString(1, like);
        ps.setInt(2, limit);
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next()) {
            out.add(rs.getString(1));
          }
        }
      }
      if (out.size() < limit) {
        // SN
        try (PreparedStatement ps =
            conn.prepareStatement(
                "SELECT DISTINCT d.numero_serie FROM dossiers_sav d WHERE lower(d.numero_serie) LIKE ? LIMIT ?")) {
          ps.setString(1, like);
          ps.setInt(2, limit - out.size());
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              out.add(rs.getString(1));
            }
          }
        }
      }
      if (out.size() < limit) {
        // Codes produit
        try (PreparedStatement ps =
            conn.prepareStatement(
                "SELECT DISTINCT p.code FROM produits p WHERE lower(p.code) LIKE ? LIMIT ?")) {
          ps.setString(1, like);
          ps.setInt(2, limit - out.size());
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              out.add(rs.getString(1));
            }
          }
        }
      }
      if (out.size() < limit) {
        // Fabricants
        try (PreparedStatement ps =
            conn.prepareStatement(
                "SELECT DISTINCT m.name FROM manufacturers m WHERE lower(m.name) LIKE ? LIMIT ?")) {
          ps.setString(1, like);
          ps.setInt(2, limit - out.size());
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              out.add(rs.getString(1));
            }
          }
        }
      }
      if (out.size() < limit) {
        // Catégories
        try (PreparedStatement ps =
            conn.prepareStatement(
                "SELECT DISTINCT name FROM categories WHERE lower(name) LIKE ? LIMIT ?")) {
          ps.setString(1, like);
          ps.setInt(2, limit - out.size());
          try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
              out.add(rs.getString(1));
            }
          }
        }
      }
    }
    return out;
  }

  private static java.time.LocalDate parseLocalDateSafely(String s) {
    if (s == null) {
      return null;
    }
    String t = s.trim();
    if (t.isEmpty()) {
      return null;
    }
    try {
      return java.time.LocalDate.parse(t);
    } catch (Exception ignore) {
      // SQLite may return unexpected values; fallback to null
      return null;
    }
  }

  /** Compter le nombre de dossiers par statut */
  public long countByStatut(String statut) throws SQLException {
    String sql = "SELECT COUNT(*) FROM dossiers_sav WHERE statut=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, statut);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
      }
    }
    return 0;
  }

  private DossierSAV mapFromResultSet(ResultSet rs) throws SQLException {
    String dateEntreeStr = rs.getString("date_entree");
    String dateSortieStr = rs.getString("date_sortie");

    return new DossierSAV(
        rs.getLong("id"),
        rs.getString("code"),
        rs.getString("produit"),
        rs.getString("numero_serie"),
        rs.getString("proprietaire"),
        rs.getString("panne"),
        rs.getString("statut"),
        rs.getString("detecteur"),
        dateEntreeStr != null ? LocalDate.parse(dateEntreeStr) : null,
        dateSortieStr != null ? LocalDate.parse(dateSortieStr) : null,
        LocalDateTime.now(), // TODO: get from DB when we add created_at column
        ((Number) rs.getObject("category_id")) != null
            ? ((Number) rs.getObject("category_id")).longValue()
            : null,
        ((Number) rs.getObject("subcategory_id")) != null
            ? ((Number) rs.getObject("subcategory_id")).longValue()
            : null);
  }

  public void assignCategories(Long dossierId, Long categoryId, Long subcategoryId)
      throws SQLException {
    String sql = "UPDATE dossiers_sav SET category_id=?, subcategory_id=? WHERE id=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
      if (categoryId != null) {
        ps.setLong(1, categoryId);
      } else {
        ps.setNull(1, Types.INTEGER);
      }
      if (subcategoryId != null) {
        ps.setLong(2, subcategoryId);
      } else {
        ps.setNull(2, Types.INTEGER);
      }
      ps.setLong(3, dossierId);
      ps.executeUpdate();
    }
  }

  public void clearCategory(Long dossierId) throws SQLException {
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "UPDATE dossiers_sav SET category_id=NULL, subcategory_id=NULL WHERE id=?")) {
      ps.setLong(1, dossierId);
      ps.executeUpdate();
    }
  }

  public void clearSubcategory(Long dossierId) throws SQLException {
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement("UPDATE dossiers_sav SET subcategory_id=NULL WHERE id=?")) {
      ps.setLong(1, dossierId);
      ps.executeUpdate();
    }
  }

  private String generateUniqueCode(Connection conn) throws SQLException {
    String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    java.util.Random rnd = new java.util.Random();
    while (true) {
      char a = letters.charAt(rnd.nextInt(letters.length()));
      char b = letters.charAt(rnd.nextInt(letters.length()));
      int num = rnd.nextInt(10000);
      String code =
          String.valueOf(a) + b + String.format("%04d", num);
      try (PreparedStatement ps =
          conn.prepareStatement("SELECT 1 FROM dossiers_sav WHERE code=? LIMIT 1")) {
        ps.setString(1, code);
        try (ResultSet rs = ps.executeQuery()) {
          if (!rs.next()) {
            return code;
          }
        }
      }
    }
  }

  private void ensureProductWithCode(Connection conn, String numeroSerie, String produitLibelle)
      throws SQLException {
    if (numeroSerie == null || numeroSerie.isBlank()) {
      return;
    }
    // Existe déjà ?
    boolean exists;
    try (PreparedStatement ps =
        conn.prepareStatement("SELECT code FROM produits WHERE numero_serie=?")) {
      ps.setString(1, numeroSerie);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          exists = true;
          // Optionnel: mettre à jour libellé produit si vide
          if (produitLibelle != null && !produitLibelle.isBlank()) {
            try (PreparedStatement up =
                conn.prepareStatement(
                    "UPDATE produits SET produit=COALESCE(?, produit) WHERE numero_serie=?")) {
              up.setString(1, produitLibelle);
              up.setString(2, numeroSerie);
              up.executeUpdate();
            }
          }
          return;
        } else {
          exists = false;
        }
      }
    }
    if (!exists) {
      // Générer un code unique au niveau produits
      String pcode;
      String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
      java.util.Random rnd = new java.util.Random();
      while (true) {
        char a = letters.charAt(rnd.nextInt(letters.length()));
        char b = letters.charAt(rnd.nextInt(letters.length()));
        int num = rnd.nextInt(10000);
        pcode =
            String.valueOf(a) + b + String.format("%04d", num);
        try (PreparedStatement ps =
            conn.prepareStatement("SELECT 1 FROM produits WHERE code=? LIMIT 1")) {
          ps.setString(1, pcode);
          try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
              break;
            }
          }
        }
      }
      try (PreparedStatement ins =
          conn.prepareStatement(
              "INSERT INTO produits(produit, numero_serie, code) VALUES(?,?,?)")) {
        ins.setString(1, produitLibelle);
        ins.setString(2, numeroSerie);
        ins.setString(3, pcode);
        ins.executeUpdate();
      }
    }
  }
}
