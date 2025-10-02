package com.magsav.repo;

import static com.magsav.repo.util.DbUtil.*;

import com.magsav.model.PurchaseRFQ;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

public class PurchaseRFQRepository {
  private final DataSource ds;

  public PurchaseRFQRepository(DataSource ds) {
    this.ds = ds;
  }

  private static LocalDateTime readLdt(ResultSet rs, int idx) throws SQLException {
    String v = rs.getString(idx);
    return v == null ? null : LocalDateTime.parse(v.replace(' ', 'T'));
  }

  private static String fmt(LocalDateTime ldt) {
    if (ldt == null) {
      return null;
    }
    return ldt.toString().replace('T', ' ');
  }

  public PurchaseRFQ save(PurchaseRFQ r) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (r.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO purchases_rfq(provider_id, provider_service_id, produit, part_number, quantity, status, requested_at, responded_at, price, currency, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          setNullableLong(ps, 1, r.providerId());
          setNullableLong(ps, 2, r.providerServiceId());
          ps.setString(3, r.produit());
          ps.setString(4, r.partNumber());
          setNullableInt(ps, 5, r.quantity());
          ps.setString(6, r.status());
          ps.setString(7, fmt(r.requestedAt()));
          ps.setString(8, fmt(r.respondedAt()));
          setNullableDouble(ps, 9, r.price());
          ps.setString(10, r.currency());
          ps.setString(11, r.notes());
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new PurchaseRFQ(
                  rs.getLong(1),
                  r.providerId(),
                  r.providerServiceId(),
                  r.produit(),
                  r.partNumber(),
                  r.quantity(),
                  r.status(),
                  r.requestedAt(),
                  r.respondedAt(),
                  r.price(),
                  r.currency(),
                  r.notes());
            }
          }
        }
        return r;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE purchases_rfq SET provider_id=?, provider_service_id=?, produit=?, part_number=?, quantity=?, status=?, requested_at=?, responded_at=?, price=?, currency=?, notes=? WHERE id=?")) {
          setNullableLong(ps, 1, r.providerId());
          setNullableLong(ps, 2, r.providerServiceId());
          ps.setString(3, r.produit());
          ps.setString(4, r.partNumber());
          setNullableInt(ps, 5, r.quantity());
          ps.setString(6, r.status());
          ps.setString(7, fmt(r.requestedAt()));
          ps.setString(8, fmt(r.respondedAt()));
          setNullableDouble(ps, 9, r.price());
          ps.setString(10, r.currency());
          ps.setString(11, r.notes());
          ps.setLong(12, r.id());
          ps.executeUpdate();
        }
        return r;
      }
    }
  }

  public Optional<PurchaseRFQ> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, provider_id, provider_service_id, produit, part_number, quantity, status, requested_at, responded_at, price, currency, notes FROM purchases_rfq WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(
              new PurchaseRFQ(
                  rs.getLong(1),
                  rs.getObject(2) == null ? null : rs.getLong(2),
                  rs.getObject(3) == null ? null : rs.getLong(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getObject(6) == null ? null : rs.getInt(6),
                  rs.getString(7),
                  readLdt(rs, 8),
                  readLdt(rs, 9),
                  rs.getObject(10) == null ? null : rs.getDouble(10),
                  rs.getString(11),
                  rs.getString(12)));
        }
      }
      return Optional.empty();
    }
  }

  public List<PurchaseRFQ> findAll() throws SQLException {
    List<PurchaseRFQ> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, provider_id, provider_service_id, produit, part_number, quantity, status, requested_at, responded_at, price, currency, notes FROM purchases_rfq ORDER BY requested_at DESC, id DESC")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new PurchaseRFQ(
                  rs.getLong(1),
                  rs.getObject(2) == null ? null : rs.getLong(2),
                  rs.getObject(3) == null ? null : rs.getLong(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getObject(6) == null ? null : rs.getInt(6),
                  rs.getString(7),
                  readLdt(rs, 8),
                  readLdt(rs, 9),
                  rs.getObject(10) == null ? null : rs.getDouble(10),
                  rs.getString(11),
                  rs.getString(12)));
        }
      }
    }
    return list;
  }

  public List<PurchaseRFQ> search(String term) throws SQLException {
    List<PurchaseRFQ> list = new ArrayList<>();
    String like = "%" + term.toLowerCase() + "%";
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, provider_id, provider_service_id, produit, part_number, quantity, status, requested_at, responded_at, price, currency, notes FROM purchases_rfq WHERE lower(produit) LIKE ? OR lower(part_number) LIKE ? ORDER BY requested_at DESC")) {
      ps.setString(1, like);
      ps.setString(2, like);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(
              new PurchaseRFQ(
                  rs.getLong(1),
                  rs.getObject(2) == null ? null : rs.getLong(2),
                  rs.getObject(3) == null ? null : rs.getLong(3),
                  rs.getString(4),
                  rs.getString(5),
                  rs.getObject(6) == null ? null : rs.getInt(6),
                  rs.getString(7),
                  readLdt(rs, 8),
                  readLdt(rs, 9),
                  rs.getObject(10) == null ? null : rs.getDouble(10),
                  rs.getString(11),
                  rs.getString(12)));
        }
      }
    }
    return list;
  }

  public void delete(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement("DELETE FROM purchases_rfq WHERE id=?")) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }
}
