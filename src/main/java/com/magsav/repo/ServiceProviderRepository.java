package com.magsav.repo;

import com.magsav.model.ServiceProvider;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class ServiceProviderRepository {
  private final DataSource ds;

  public ServiceProviderRepository(DataSource ds) {
    this.ds = ds;
  }

  public ServiceProvider save(ServiceProvider s) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (s.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO service_providers(name, email, phone, address) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setString(1, s.name());
          ps.setString(2, s.email());
          ps.setString(3, s.phone());
          ps.setString(4, s.address());
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new ServiceProvider(
                  rs.getLong(1), s.name(), s.email(), s.phone(), s.address());
            }
          }
        }
        return s;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE service_providers SET name=?, email=?, phone=?, address=? WHERE id=?")) {
          ps.setString(1, s.name());
          ps.setString(2, s.email());
          ps.setString(3, s.phone());
          ps.setString(4, s.address());
          ps.setLong(5, s.id());
          ps.executeUpdate();
        }
        return s;
      }
    }
  }

  public Optional<ServiceProvider> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, email, phone, address FROM service_providers WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(
              new ServiceProvider(
                  rs.getLong(1),
                  rs.getString(2),
                  rs.getString(3),
                  rs.getString(4),
                  rs.getString(5)));
        }
      }
      return Optional.empty();
    }
  }

  public List<ServiceProvider> findAll() throws SQLException {
    List<ServiceProvider> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, email, phone, address FROM service_providers ORDER BY name")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new ServiceProvider(
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

  public List<ServiceProvider> search(String term) throws SQLException {
    List<ServiceProvider> list = new ArrayList<>();
    String like = "%" + term.toLowerCase() + "%";
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, name, email, phone, address FROM service_providers WHERE lower(name) LIKE ? OR lower(email) LIKE ? ORDER BY name")) {
      ps.setString(1, like);
      ps.setString(2, like);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new ServiceProvider(
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

  public void delete(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement("DELETE FROM service_providers WHERE id=?")) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }
}
