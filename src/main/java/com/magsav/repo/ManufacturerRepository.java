package com.magsav.repo;

import com.magsav.model.Manufacturer;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class ManufacturerRepository {
  private final DataSource ds;

  public ManufacturerRepository(DataSource ds) {
    this.ds = ds;
  }

  public Manufacturer save(Manufacturer m) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (m.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO manufacturers(name, website, contact_email, contact_phone, logo_path) VALUES(?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setString(1, m.name());
          ps.setString(2, m.website());
          ps.setString(3, m.contactEmail());
          ps.setString(4, m.contactPhone());
          ps.setString(5, m.logoPath());
          ps.executeUpdate();
          Long id = null;
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              id = rs.getLong(1);
            }
          }
          if (id == null) {
            try (var rs2 = c.createStatement().executeQuery("SELECT last_insert_rowid()")) {
              if (rs2.next()) {
                id = rs2.getLong(1);
              }
            }
          }
          if (id != null) {
            return new Manufacturer(
                id, m.name(), m.website(), m.contactEmail(), m.contactPhone(), m.logoPath());
          }
          // Fallback improbable: relire par nom
          return findByName(c, m.name()).orElse(m);
        } catch (SQLException ex) {
          // Contrainte UNIQUE (nom) : renvoyer l'existant
          if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unique")) {
            return findByName(c, m.name()).orElseThrow(() -> ex);
          }
          throw ex;
        }
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE manufacturers SET name=?, website=?, contact_email=?, contact_phone=?, logo_path=? WHERE id=?")) {
          ps.setString(1, m.name());
          ps.setString(2, m.website());
          ps.setString(3, m.contactEmail());
          ps.setString(4, m.contactPhone());
          ps.setString(5, m.logoPath());
          ps.setLong(6, m.id());
          ps.executeUpdate();
        }
        return m;
      }
    }
  }

  public Optional<Manufacturer> findByName(String name) throws SQLException {
    try (Connection c = ds.getConnection()) {
      return findByName(c, name);
    }
  }

  private Optional<Manufacturer> findByName(Connection c, String name) throws SQLException {
    try (PreparedStatement ps =
        c.prepareStatement(
            "SELECT id, name, website, contact_email, contact_phone, logo_path FROM manufacturers WHERE lower(name)=lower(?)")) {
      ps.setString(1, name);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(
              new Manufacturer(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getString(6)));
        }
        return Optional.empty();
      }
    }
  }

  public Optional<Manufacturer> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, website, contact_email, contact_phone, logo_path FROM manufacturers WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(
              new Manufacturer(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getString(6)));
        }
      }
      return Optional.empty();
    }
  }

  public List<Manufacturer> findAll() throws SQLException {
    List<Manufacturer> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, website, contact_email, contact_phone, logo_path FROM manufacturers ORDER BY name")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new Manufacturer(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getString(6)));
        }
      }
    }
    return list;
  }

  public List<Manufacturer> search(String term) throws SQLException {
    List<Manufacturer> list = new ArrayList<>();
    String like = "%" + term.toLowerCase() + "%";
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, website, contact_email, contact_phone, logo_path FROM manufacturers WHERE lower(name) LIKE ? OR lower(website) LIKE ? OR lower(contact_email) LIKE ? ORDER BY name")) {
      ps.setString(1, like);
      ps.setString(2, like);
      ps.setString(3, like);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new Manufacturer(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getString(6)));
        }
      }
    }
    return list;
  }

  public void delete(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement("DELETE FROM manufacturers WHERE id=?")) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }
}
