package com.magsav.repo;

import static com.magsav.repo.util.DbUtil.*;

import com.magsav.model.DocumentEntry;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class DocumentRepository {
  private final DataSource ds;

  public DocumentRepository(DataSource ds) {
    this.ds = ds;
  }

  public DocumentEntry save(DocumentEntry d) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (d.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO documents(type, original_name, normalized_name, path, linked_product_code, linked_numero_serie, linked_dossier_id, linked_rfq_id, linked_rma_id, created_at) VALUES (?,?,?,?,?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setString(1, d.type());
          ps.setString(2, d.originalName());
          ps.setString(3, d.normalizedName());
          ps.setString(4, d.path());
          ps.setString(5, d.linkedProductCode());
          ps.setString(6, d.linkedNumeroSerie());
          if (d.linkedDossierId() == null) {
            ps.setNull(7, Types.BIGINT);
          } else {
            ps.setLong(7, d.linkedDossierId());
          }
          if (d.linkedRfqId() == null) {
            ps.setNull(8, Types.BIGINT);
          } else {
            ps.setLong(8, d.linkedRfqId());
          }
          if (d.linkedRmaId() == null) {
            ps.setNull(9, Types.BIGINT);
          } else {
            ps.setLong(9, d.linkedRmaId());
          }
          ps.setString(10, fmt(d.createdAt()));
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new DocumentEntry(
                  rs.getLong(1),
                  d.type(),
                  d.originalName(),
                  d.normalizedName(),
                  d.path(),
                  d.linkedProductCode(),
                  d.linkedNumeroSerie(),
                  d.linkedDossierId(),
                  d.linkedRfqId(),
                  d.linkedRmaId(),
                  d.createdAt());
            }
          }
        }
        return d;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE documents SET type=?, original_name=?, normalized_name=?, path=?, linked_product_code=?, linked_numero_serie=?, linked_dossier_id=?, linked_rfq_id=?, linked_rma_id=?, created_at=? WHERE id=?")) {
          ps.setString(1, d.type());
          ps.setString(2, d.originalName());
          ps.setString(3, d.normalizedName());
          ps.setString(4, d.path());
          ps.setString(5, d.linkedProductCode());
          ps.setString(6, d.linkedNumeroSerie());
          if (d.linkedDossierId() == null) {
            ps.setNull(7, Types.BIGINT);
          } else {
            ps.setLong(7, d.linkedDossierId());
          }
          if (d.linkedRfqId() == null) {
            ps.setNull(8, Types.BIGINT);
          } else {
            ps.setLong(8, d.linkedRfqId());
          }
          if (d.linkedRmaId() == null) {
            ps.setNull(9, Types.BIGINT);
          } else {
            ps.setLong(9, d.linkedRmaId());
          }
          ps.setString(10, fmt(d.createdAt()));
          ps.setLong(11, d.id());
          ps.executeUpdate();
        }
        return d;
      }
    }
  }

  private DocumentEntry map(ResultSet rs) throws SQLException {
    return new DocumentEntry(
        rs.getLong(1),
        rs.getString(2),
        rs.getString(3),
        rs.getString(4),
        rs.getString(5),
        rs.getString(6),
        rs.getString(7),
        rs.getObject(8) == null ? null : rs.getLong(8),
        rs.getObject(9) == null ? null : rs.getLong(9),
        rs.getObject(10) == null ? null : rs.getLong(10),
        ldt(rs, 11));
  }

  public Optional<DocumentEntry> findById(Long id) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, type, original_name, normalized_name, path, linked_product_code, linked_numero_serie, linked_dossier_id, linked_rfq_id, linked_rma_id, created_at FROM documents WHERE id=?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(map(rs));
        }
      }
      return Optional.empty();
    }
  }

  public List<DocumentEntry> findAll() throws SQLException {
    List<DocumentEntry> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, type, original_name, normalized_name, path, linked_product_code, linked_numero_serie, linked_dossier_id, linked_rfq_id, linked_rma_id, created_at FROM documents ORDER BY created_at DESC, id DESC")) {
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }

  public List<DocumentEntry> searchByLink(String code, String sn) throws SQLException {
    List<DocumentEntry> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, type, original_name, normalized_name, path, linked_product_code, linked_numero_serie, linked_dossier_id, linked_rfq_id, linked_rma_id, created_at FROM documents WHERE linked_product_code=? OR linked_numero_serie=? ORDER BY created_at DESC")) {
      ps.setString(1, code);
      ps.setString(2, sn);
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
        PreparedStatement ps = c.prepareStatement("DELETE FROM documents WHERE id=?")) {
      ps.setLong(1, id);
      ps.executeUpdate();
    }
  }
}
