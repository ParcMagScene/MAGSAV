package com.magsav.repo;

import java.sql.*;
import javax.sql.DataSource;

public class ProductAdminRepository {
  private final DataSource ds;

  public ProductAdminRepository(DataSource ds) {
    this.ds = ds;
  }

  public void assignManufacturerToProduit(String produit, Long manufacturerId) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "UPDATE produits SET manufacturer_id=? WHERE lower(produit)=lower(?)")) {
      ps.setLong(1, manufacturerId);
      ps.setString(2, produit);
      ps.executeUpdate();
    }
  }

  public void assignManufacturerToNumeroSerie(String numeroSerie, Long manufacturerId)
      throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement("UPDATE produits SET manufacturer_id=? WHERE numero_serie=?")) {
      ps.setLong(1, manufacturerId);
      ps.setString(2, numeroSerie);
      ps.executeUpdate();
    }
  }
}
