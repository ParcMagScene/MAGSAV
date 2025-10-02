package com.magsav.repo;

import java.sql.*;
import javax.sql.DataSource;

public class ProductRepository {
  private final DataSource ds;

  public ProductRepository(DataSource ds) {
    this.ds = ds;
  }

  public void updatePhotoPath(String numeroSerie, String photoPath) throws SQLException {
    try (Connection c = ds.getConnection()) {
      // S'assurer que le produit existe
      try (PreparedStatement psSel =
          c.prepareStatement("SELECT 1 FROM produits WHERE numero_serie=?")) {
        psSel.setString(1, numeroSerie);
        try (ResultSet rs = psSel.executeQuery()) {
          if (!rs.next()) {
            try (PreparedStatement ins =
                c.prepareStatement(
                    "INSERT INTO produits(produit, numero_serie, code, photo_path) VALUES(?,?,NULL,?)")) {
              ins.setString(1, null);
              ins.setString(2, numeroSerie);
              ins.setString(3, photoPath);
              ins.executeUpdate();
            }
            return;
          }
        }
      }
      // Essayer de récupérer le nom de produit pour appliquer la photo à tous les enregistrements
      // du même modèle
      String produit = findProduitByNumeroSerie(c, numeroSerie);
      if (produit != null && !produit.isBlank()) {
        try (PreparedStatement ps =
            c.prepareStatement(
                "UPDATE produits SET photo_path=? WHERE lower(produit) = lower(?)")) {
          ps.setString(1, photoPath);
          ps.setString(2, produit);
          ps.executeUpdate();
        }
      } else {
        // Fallback par numéro de série uniquement
        try (PreparedStatement ps =
            c.prepareStatement("UPDATE produits SET photo_path=? WHERE numero_serie=?")) {
          ps.setString(1, photoPath);
          ps.setString(2, numeroSerie);
          ps.executeUpdate();
        }
      }
    }
  }

  public String findPhotoPathByNumeroSerie(String numeroSerie) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement("SELECT photo_path FROM produits WHERE numero_serie=?")) {
      ps.setString(1, numeroSerie);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
        return null;
      }
    }
  }

  public String findCodeByNumeroSerie(String numeroSerie) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement("SELECT code FROM produits WHERE numero_serie=?")) {
      ps.setString(1, numeroSerie);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
        return null;
      }
    }
  }

  public String findManufacturerNameByNumeroSerie(String numeroSerie) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement(
                "SELECT m.name FROM manufacturers m JOIN produits p ON p.manufacturer_id = m.id WHERE p.numero_serie=? LIMIT 1")) {
      ps.setString(1, numeroSerie);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
        return null;
      }
    }
  }

  public String findProduitByNumeroSerie(String numeroSerie) throws SQLException {
    try (Connection c = ds.getConnection()) {
      return findProduitByNumeroSerie(c, numeroSerie);
    }
  }

  private String findProduitByNumeroSerie(Connection c, String numeroSerie) throws SQLException {
    // Tenter via produits
    try (PreparedStatement ps =
        c.prepareStatement("SELECT produit FROM produits WHERE numero_serie=?")) {
      ps.setString(1, numeroSerie);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          String p = rs.getString(1);
          if (p != null && !p.isBlank()) {
            return p;
          }
        }
      }
    }
    // Fallback via dossiers_sav si le nom n'est pas stocké dans produits
    try (PreparedStatement ps =
        c.prepareStatement(
            "SELECT produit FROM dossiers_sav WHERE numero_serie=? AND produit IS NOT NULL AND TRIM(produit)<>'' ORDER BY date_entree DESC LIMIT 1")) {
      ps.setString(1, numeroSerie);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
      }
    }
    return null;
  }

  public void updatePhotoPathByProduit(String produit, String photoPath) throws SQLException {
    try (Connection c = ds.getConnection();
        PreparedStatement ps =
            c.prepareStatement("UPDATE produits SET photo_path=? WHERE lower(produit)=lower(?)")) {
      ps.setString(1, photoPath);
      ps.setString(2, produit);
      ps.executeUpdate();
    }
  }
}
