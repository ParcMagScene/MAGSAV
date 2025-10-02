package com.magsav.repo;

import com.magsav.model.ServiceContact;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class ServiceContactRepository {
  private final DataSource ds;

  public ServiceContactRepository(DataSource ds) {
    this.ds = ds;
  }

  private ServiceContact map(ResultSet rs) throws SQLException {
    return new ServiceContact(
        rs.getLong(1),
        rs.getLong(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6));
  }

  public ServiceContact save(ServiceContact sc) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (sc.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO service_contacts(service_id, name, email, phone, role) VALUES (?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setLong(1, sc.serviceId());
          ps.setString(2, sc.name());
          ps.setString(3, sc.email());
          ps.setString(4, sc.phone());
          ps.setString(5, sc.role());
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new ServiceContact(
                  rs.getLong(1), sc.serviceId(), sc.name(), sc.email(), sc.phone(), sc.role());
            }
          }
        }
        return sc;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE service_contacts SET service_id=?, name=?, email=?, phone=?, role=? WHERE id=?")) {
          ps.setLong(1, sc.serviceId());
          ps.setString(2, sc.name());
          ps.setString(3, sc.email());
          ps.setString(4, sc.phone());
          ps.setString(5, sc.role());
          ps.setLong(6, sc.id());
          ps.executeUpdate();
        }
        return sc;
      }
    }
  }

  public List<ServiceContact> findByService(Long serviceId) throws SQLException {
    List<ServiceContact> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, service_id, name, email, phone, role FROM service_contacts WHERE service_id=? ORDER BY name")) {
      ps.setLong(1, serviceId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }
}
