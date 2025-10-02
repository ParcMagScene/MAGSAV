package com.magsav.repo;

import com.magsav.model.Company;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class CompanyRepository {
  private final DataSource ds;

  public CompanyRepository(DataSource ds) {
    this.ds = ds;
  }

  private Company map(ResultSet rs) throws SQLException {
    return new Company(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getString(10),
        rs.getString(11));
  }

  public Company save(Company cpy) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (cpy.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO companies(name, siret, email, phone, website, address_line1, address_line2, postal_code, city, country) VALUES (?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setString(1, cpy.name());
          ps.setString(2, cpy.siret());
          ps.setString(3, cpy.email());
          ps.setString(4, cpy.phone());
          ps.setString(5, cpy.website());
          ps.setString(6, cpy.addressLine1());
          ps.setString(7, cpy.addressLine2());
          ps.setString(8, cpy.postalCode());
          ps.setString(9, cpy.city());
          ps.setString(10, cpy.country());
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new Company(
                  rs.getLong(1),
                  cpy.name(),
                  cpy.siret(),
                  cpy.email(),
                  cpy.phone(),
                  cpy.website(),
                  cpy.addressLine1(),
                  cpy.addressLine2(),
                  cpy.postalCode(),
                  cpy.city(),
                  cpy.country());
            }
          }
        }
        return cpy;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE companies SET name=?, siret=?, email=?, phone=?, website=?, address_line1=?, address_line2=?, postal_code=?, city=?, country=? WHERE id=?")) {
          ps.setString(1, cpy.name());
          ps.setString(2, cpy.siret());
          ps.setString(3, cpy.email());
          ps.setString(4, cpy.phone());
          ps.setString(5, cpy.website());
          ps.setString(6, cpy.addressLine1());
          ps.setString(7, cpy.addressLine2());
          ps.setString(8, cpy.postalCode());
          ps.setString(9, cpy.city());
          ps.setString(10, cpy.country());
          ps.setLong(11, cpy.id());
          ps.executeUpdate();
        }
        return cpy;
      }
    }
  }

  public Optional<Company> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, siret, email, phone, website, address_line1, address_line2, postal_code, city, country FROM companies WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(map(rs));
        }
      }
      return Optional.empty();
    }
  }

  public Optional<Company> findByName(String name) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, siret, email, phone, website, address_line1, address_line2, postal_code, city, country FROM companies WHERE name=?")) {
      ps.setString(1, name);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(map(rs));
        }
      }
      return Optional.empty();
    }
  }

  public List<Company> findAll() throws SQLException {
    List<Company> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, siret, email, phone, website, address_line1, address_line2, postal_code, city, country FROM companies ORDER BY name")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }
}
