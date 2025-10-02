package com.magsav.repo;

import com.magsav.model.ManufacturerServiceLink;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;

public class ManufacturerServiceLinkRepository {
  private final DataSource ds;

  public ManufacturerServiceLinkRepository(DataSource ds) {
    this.ds = ds;
  }

  private ManufacturerServiceLink map(ResultSet rs) throws SQLException {
    return new ManufacturerServiceLink(
        rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getString(4));
  }

  public ManufacturerServiceLink save(ManufacturerServiceLink link) throws SQLException {
    try (Connection c = ds.getConnection()) {
      if (link.id() == null) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "INSERT INTO manufacturer_services(manufacturer_id, service_id, relation_type) VALUES (?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
          ps.setLong(1, link.manufacturerId());
          ps.setLong(2, link.serviceId());
          ps.setString(3, link.relationType());
          ps.executeUpdate();
          try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
              return new ManufacturerServiceLink(
                  rs.getLong(1), link.manufacturerId(), link.serviceId(), link.relationType());
            }
          }
        }
        return link;
      } else {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE manufacturer_services SET manufacturer_id=?, service_id=?, relation_type=? WHERE id=?")) {
          ps.setLong(1, link.manufacturerId());
          ps.setLong(2, link.serviceId());
          ps.setString(3, link.relationType());
          ps.setLong(4, link.id());
          ps.executeUpdate();
        }
        return link;
      }
    }
  }

  public List<ManufacturerServiceLink> findByManufacturer(Long manufacturerId, String relationType)
      throws SQLException {
    List<ManufacturerServiceLink> list = new ArrayList<>();
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT id, manufacturer_id, service_id, relation_type FROM manufacturer_services WHERE manufacturer_id=? AND relation_type=?")) {
      ps.setLong(1, manufacturerId);
      ps.setString(2, relationType);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          list.add(map(rs));
        }
      }
    }
    return list;
  }
}
