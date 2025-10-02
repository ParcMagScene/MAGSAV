package com.magsav.repo;

import com.magsav.model.CompanyService;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class CompanyServiceRepository {
  private final DataSource ds;

  public CompanyServiceRepository(DataSource ds) {
    this.ds = ds;
  }

  private CompanyService map(ResultSet rs) throws SQLException {
    return new CompanyService(
        rs.getLong(1),
        rs.getLong(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getString(10),
        rs.getString(11),
        rs.getInt(12) == 1);
  }

  public CompanyService save(CompanyService s) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (s.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO company_services(company_id, type, name, email, phone, address_line1, address_line2, postal_code, city, country, active) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setLong(1, s.companyId());
          ps.setString(2, s.type());
          ps.setString(3, s.name());
          ps.setString(4, s.email());
          ps.setString(5, s.phone());
          ps.setString(6, s.addressLine1());
          ps.setString(7, s.addressLine2());
          ps.setString(8, s.postalCode());
          ps.setString(9, s.city());
          ps.setString(10, s.country());
          ps.setInt(11, s.active() ? 1 : 0);
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new CompanyService(
                  rs.getLong(1),
                  s.companyId(),
                  s.type(),
                  s.name(),
                  s.email(),
                  s.phone(),
                  s.addressLine1(),
                  s.addressLine2(),
                  s.postalCode(),
                  s.city(),
                  s.country(),
                  s.active());
            }
          }
        }
        return s;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE company_services SET company_id=?, type=?, name=?, email=?, phone=?, address_line1=?, address_line2=?, postal_code=?, city=?, country=?, active=? WHERE id=?")) {
          ps.setLong(1, s.companyId());
          ps.setString(2, s.type());
          ps.setString(3, s.name());
          ps.setString(4, s.email());
          ps.setString(5, s.phone());
          ps.setString(6, s.addressLine1());
          ps.setString(7, s.addressLine2());
          ps.setString(8, s.postalCode());
          ps.setString(9, s.city());
          ps.setString(10, s.country());
          ps.setInt(11, s.active() ? 1 : 0);
          ps.setLong(12, s.id());
          ps.executeUpdate();
        }
        return s;
      }
    }
  }

  public Optional<CompanyService> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, company_id, type, name, email, phone, address_line1, address_line2, postal_code, city, country, active FROM company_services WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(map(rs));
        }
      }
      return Optional.empty();
    }
  }

  public List<CompanyService> findByCompany(Long companyId) throws SQLException {
    List<CompanyService> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, company_id, type, name, email, phone, address_line1, address_line2, postal_code, city, country, active FROM company_services WHERE company_id=? ORDER BY type, name")) {
      ps.setLong(1, companyId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  public List<CompanyService> findAllActiveByType(String type) throws SQLException {
    List<CompanyService> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, company_id, type, name, email, phone, address_line1, address_line2, postal_code, city, country, active FROM company_services WHERE type=? AND active=1 ORDER BY name")) {
      ps.setString(1, type);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  public List<CompanyService> findByIds(Collection<Long> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    StringBuilder in = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0) {
        in.append(',');
      }
      in.append('?');
    }
    String sql =
        "SELECT id, company_id, type, name, email, phone, address_line1, address_line2, postal_code, city, country, active FROM company_services WHERE id IN ("
            + in
            + ") ORDER BY name";
    List<CompanyService> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      int idx = 1;
      for (Long id : ids) ps.setLong(idx++, id);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  public List<CompanyService> findActiveByIdsAndType(Collection<Long> ids, String type)
      throws SQLException {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    StringBuilder in = new StringBuilder();
    for (int i = 0; i < ids.size(); i++) {
      if (i > 0) {
        in.append(',');
      }
      in.append('?');
    }
    String sql =
        "SELECT id, company_id, type, name, email, phone, address_line1, address_line2, postal_code, city, country, active FROM company_services WHERE id IN ("
            + in
            + ") AND type=? AND active=1 ORDER BY name";
    List<CompanyService> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(sql)) {
      int idx = 1;
      for (Long id : ids) ps.setLong(idx++, id);
      ps.setString(idx, type);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }
}
