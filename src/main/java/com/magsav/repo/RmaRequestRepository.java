package com.magsav.repo;

import static com.magsav.repo.util.DbUtil.*;

import com.magsav.model.RmaRequest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

public class RmaRequestRepository {
  private final DataSource ds;

  public RmaRequestRepository(DataSource ds) {
    this.ds = ds;
  }

  private static LocalDateTime ldt(ResultSet rs, int idx) throws SQLException {
    String v = rs.getString(idx);
    return v == null ? null : LocalDateTime.parse(v.replace(' ', 'T'));
  }

  private static String fmt(LocalDateTime ldt) {
    if (ldt == null) {
      return null;
    }
    return ldt.toString().replace('T', ' ');
  }

  public RmaRequest save(RmaRequest r) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (r.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO rma_requests(provider_id, provider_service_id, manufacturer_id, produit, numero_serie, code_produit, reason, status, rma_number, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          setNullableLong(ps, 1, r.providerId());
          setNullableLong(ps, 2, r.providerServiceId());
          setNullableLong(ps, 3, r.manufacturerId());
          ps.setString(4, r.produit());
          ps.setString(5, r.numeroSerie());
          ps.setString(6, r.codeProduit());
          ps.setString(7, r.reason());
          ps.setString(8, r.status());
          ps.setString(9, r.rmaNumber());
          ps.setString(10, fmt(r.createdAt()));
          ps.setString(11, fmt(r.updatedAt()));
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new RmaRequest(
                  rs.getLong(1),
                  r.providerId(),
                  r.providerServiceId(),
                  r.manufacturerId(),
                  r.produit(),
                  r.numeroSerie(),
                  r.codeProduit(),
                  r.reason(),
                  r.status(),
                  r.rmaNumber(),
                  r.createdAt(),
                  r.updatedAt());
            }
          }
        }
        return r;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE rma_requests SET provider_id=?, provider_service_id=?, manufacturer_id=?, produit=?, numero_serie=?, code_produit=?, reason=?, status=?, rma_number=?, created_at=?, updated_at=? WHERE id=?")) {
          setNullableLong(ps, 1, r.providerId());
          setNullableLong(ps, 2, r.providerServiceId());
          setNullableLong(ps, 3, r.manufacturerId());
          ps.setString(4, r.produit());
          ps.setString(5, r.numeroSerie());
          ps.setString(6, r.codeProduit());
          ps.setString(7, r.reason());
          ps.setString(8, r.status());
          ps.setString(9, r.rmaNumber());
          ps.setString(10, fmt(r.createdAt()));
          ps.setString(11, fmt(r.updatedAt()));
          ps.setLong(12, r.id());
          ps.executeUpdate();
        }
        return r;
      }
    }
  }

  private RmaRequest map(ResultSet rs) throws SQLException {
    return new RmaRequest(
        rs.getLong(1),
        rs.getObject(2) == null ? null : rs.getLong(2),
        rs.getObject(3) == null ? null : rs.getLong(3),
        rs.getObject(4) == null ? null : rs.getLong(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getString(8),
        rs.getString(9),
        rs.getString(10),
        ldt(rs, 11),
        ldt(rs, 12));
  }

  public Optional<RmaRequest> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, provider_id, provider_service_id, manufacturer_id, produit, numero_serie, code_produit, reason, status, rma_number, created_at, updated_at FROM rma_requests WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(map(rs));
        }
      }
      return Optional.empty();
    }
  }

  public List<RmaRequest> findAll() throws SQLException {
    List<RmaRequest> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, provider_id, provider_service_id, manufacturer_id, produit, numero_serie, code_produit, reason, status, rma_number, created_at, updated_at FROM rma_requests ORDER BY created_at DESC, id DESC")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  public List<RmaRequest> search(String term) throws SQLException {
    List<RmaRequest> list = new ArrayList<>();
    String like = "%" + term.toLowerCase() + "%";
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, provider_id, provider_service_id, manufacturer_id, produit, numero_serie, code_produit, reason, status, rma_number, created_at, updated_at FROM rma_requests WHERE lower(produit) LIKE ? OR lower(numero_serie) LIKE ? OR lower(code_produit) LIKE ? OR lower(rma_number) LIKE ? ORDER BY created_at DESC")) {
      ps.setString(1, like);
      ps.setString(2, like);
      ps.setString(3, like);
      ps.setString(4, like);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  public void delete(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement("DELETE FROM rma_requests WHERE id=?")) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }
}
