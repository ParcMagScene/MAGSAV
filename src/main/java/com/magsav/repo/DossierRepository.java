package com.magsav.repo;

import com.magsav.model.Dossier;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class DossierRepository {
  private final DataSource ds;

  public DossierRepository(DataSource ds) {
    this.ds = ds;
  }

  public Dossier save(Dossier dossier) throws SQLException {
    if (dossier.id() == null) {
      return insert(dossier);
    } else {
      return update(dossier);
    }
  }

  private Dossier insert(Dossier dossier) throws SQLException {
    String sql =
        "INSERT INTO dossiers(appareil_id, statut, symptome, commentaire, date_entree, date_sortie) VALUES(?,?,?,?,?,?)";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ps.setLong(1, dossier.appareilId());
      ps.setString(2, dossier.statut());
      ps.setString(3, dossier.symptome());
      ps.setString(4, dossier.commentaire());
      ps.setString(
          5,
          dossier.dateEntree() != null
              ? dossier.dateEntree().toString()
              : LocalDate.now().toString());
      ps.setString(6, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);

      ps.executeUpdate();

      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (rs.next()) {
          return dossier.withId(rs.getLong(1));
        }
        throw new SQLException("Failed to get generated ID");
      }
    }
  }

  private Dossier update(Dossier dossier) throws SQLException {
    String sql =
        "UPDATE dossiers SET statut=?, symptome=?, commentaire=?, date_sortie=? WHERE id=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, dossier.statut());
      ps.setString(2, dossier.symptome());
      ps.setString(3, dossier.commentaire());
      ps.setString(4, dossier.dateSortie() != null ? dossier.dateSortie().toString() : null);
      ps.setLong(5, dossier.id());

      ps.executeUpdate();
      return dossier;
    }
  }

  public Dossier findById(Long id) throws SQLException {
    String sql =
        "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers WHERE id=?";
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

  public List<Dossier> findByStatut(String statut) throws SQLException {
    String sql =
        "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers WHERE statut=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, statut);
      List<Dossier> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  public List<Dossier> findByAppareilId(Long appareilId) throws SQLException {
    String sql =
        "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers WHERE appareil_id=?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setLong(1, appareilId);
      List<Dossier> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  public List<Dossier> findAll() throws SQLException {
    String sql =
        "SELECT id, appareil_id, statut, symptome, commentaire, date_entree, date_sortie FROM dossiers ORDER BY date_entree DESC";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      List<Dossier> results = new ArrayList<>();

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  /** Compter le nombre total de dossiers */
  public long count() throws SQLException {
    String sql = "SELECT COUNT(*) FROM dossiers";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      if (rs.next()) {
        return rs.getLong(1);
      }
      return 0;
    }
  }

  /** Récupérer les dossiers avec pagination */
  public List<Dossier> findPaginated(int offset, int limit, String sortClause) throws SQLException {
    String sql = "SELECT * FROM dossiers ORDER BY " + sortClause + " LIMIT ? OFFSET ?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, limit);
      ps.setInt(2, offset);

      List<Dossier> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  /** Compter les dossiers par statut */
  public long countByStatut(String statut) throws SQLException {
    String sql = "SELECT COUNT(*) FROM dossiers WHERE statut = ?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, statut);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
        return 0;
      }
    }
  }

  /** Récupérer les dossiers par statut avec pagination */
  public List<Dossier> findByStatutPaginated(
      String statut, int offset, int limit, String sortClause) throws SQLException {
    String sql =
        "SELECT * FROM dossiers WHERE statut = ? ORDER BY " + sortClause + " LIMIT ? OFFSET ?";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, statut);
      ps.setInt(2, limit);
      ps.setInt(3, offset);

      List<Dossier> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  /** Compter les dossiers correspondant à une recherche */
  public long countBySearch(String terme) throws SQLException {
    String sql =
        "SELECT COUNT(DISTINCT d.id) FROM dossiers d "
            + "JOIN appareils a ON d.appareil_id = a.id "
            + "JOIN clients c ON a.client_id = c.id "
            + "WHERE d.symptome LIKE ? OR d.commentaire LIKE ? OR "
            + "      a.marque LIKE ? OR a.modele LIKE ? OR a.sn LIKE ? OR "
            + "      c.nom LIKE ? OR c.prenom LIKE ? OR c.email LIKE ?";

    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      String searchTerm = "%" + terme + "%";
      for (int i = 1; i <= 8; i++) {
        ps.setString(i, searchTerm);
      }

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getLong(1);
        }
        return 0;
      }
    }
  }

  /** Rechercher des dossiers avec pagination */
  public List<Dossier> findBySearchPaginated(String terme, int offset, int limit, String sortClause)
      throws SQLException {
    String sql =
        "SELECT DISTINCT d.* FROM dossiers d "
            + "JOIN appareils a ON d.appareil_id = a.id "
            + "JOIN clients c ON a.client_id = c.id "
            + "WHERE d.symptome LIKE ? OR d.commentaire LIKE ? OR "
            + "      a.marque LIKE ? OR a.modele LIKE ? OR a.sn LIKE ? OR "
            + "      c.nom LIKE ? OR c.prenom LIKE ? OR c.email LIKE ? "
            + "ORDER BY d."
            + sortClause
            + " LIMIT ? OFFSET ?";

    try (Connection conn = ds.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      String searchTerm = "%" + terme + "%";
      for (int i = 1; i <= 8; i++) {
        ps.setString(i, searchTerm);
      }
      ps.setInt(9, limit);
      ps.setInt(10, offset);

      List<Dossier> results = new ArrayList<>();
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          results.add(mapFromResultSet(rs));
        }
      }
      return results;
    }
  }

  private Dossier mapFromResultSet(ResultSet rs) throws SQLException {
    String dateEntreeStr = rs.getString("date_entree");
    String dateSortieStr = rs.getString("date_sortie");

    return new Dossier(
        rs.getLong("id"),
        rs.getLong("appareil_id"),
        rs.getString("statut"),
        rs.getString("symptome"),
        rs.getString("commentaire"),
        dateEntreeStr != null ? LocalDate.parse(dateEntreeStr) : null,
        dateSortieStr != null ? LocalDate.parse(dateSortieStr) : null,
        LocalDateTime.now() // TODO: get from DB when we add created_at column
        );
  }
}
