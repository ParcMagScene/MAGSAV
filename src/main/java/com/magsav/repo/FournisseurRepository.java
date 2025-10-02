package com.magsav.repo;

import com.magsav.model.Fournisseur;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class FournisseurRepository {
  private final DataSource ds;

  public FournisseurRepository(DataSource ds) {
    this.ds = ds;
  }

  public DataSource getDataSource() {
    return ds;
  }

  public Fournisseur upsertByEmail(Fournisseur f) throws SQLException {
    try (Connection conn = ds.getConnection()) {
      String update = "UPDATE fournisseurs SET nom=?, tel=?, siret=? WHERE email=?";
      try (PreparedStatement ps = conn.prepareStatement(update)) {
        ps.setString(1, f.nom());
        ps.setString(2, f.tel());
        ps.setString(3, f.siret());
        ps.setString(4, f.email());
        int count = ps.executeUpdate();
        if (count > 0) {
          return findByEmail(f.email());
        }
      }
      String insert = "INSERT OR IGNORE INTO fournisseurs(nom, email, tel, siret) VALUES(?,?,?,?)";
      try (PreparedStatement ps = conn.prepareStatement(insert)) {
        ps.setString(1, f.nom());
        ps.setString(2, f.email());
        ps.setString(3, f.tel());
        ps.setString(4, f.siret());
        ps.executeUpdate();
      }
      return findByEmail(f.email());
    }
  }

  public Fournisseur findByEmail(String email) throws SQLException {
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT id, nom, email, tel, siret FROM fournisseurs WHERE email=?")) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Fournisseur(
              rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
        }
        return null;
      }
    }
  }

  public Fournisseur findById(Long id) throws SQLException {
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT id, nom, email, tel, siret FROM fournisseurs WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return new Fournisseur(
              rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5));
        }
        return null;
      }
    }
  }

  public List<Fournisseur> findAll() throws SQLException {
    List<Fournisseur> list = new ArrayList<>();
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT id, nom, email, tel, siret FROM fournisseurs ORDER BY nom")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new Fournisseur(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5)));
        }
      }
    }
    return list;
  }

  public List<Fournisseur> search(String term) throws SQLException {
    List<Fournisseur> list = new ArrayList<>();
    String like = "%" + term.toLowerCase() + "%";
    try (Connection conn = ds.getConnection();
        PreparedStatement ps =
            conn.prepareStatement(
                "SELECT id, nom, email, tel, siret FROM fournisseurs WHERE lower(nom) LIKE ? OR lower(email) LIKE ? ORDER BY nom")) {
      ps.setString(1, like);
      ps.setString(2, like);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new Fournisseur(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5)));
        }
      }
    }
    return list;
  }
}
