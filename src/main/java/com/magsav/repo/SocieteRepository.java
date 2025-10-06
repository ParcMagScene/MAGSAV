package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Societe;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SocieteRepository {

  private static Societe map(ResultSet rs) throws SQLException {
    return new Societe(
        rs.getLong("id"),
        rs.getString("type"),
        rs.getString("nom"),
        rs.getString("email"),
        rs.getString("phone"),
        rs.getString("adresse"),
        rs.getString("notes"),
        rs.getString("created_at")
    );
  }

  public List<String> listManufacturers() {
    String sql = "SELECT DISTINCT fabricant FROM produits WHERE COALESCE(TRIM(fabricant),'')<>'' ORDER BY fabricant";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      List<String> out = new ArrayList<>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) { throw new RuntimeException("listManufacturers failed", e); }
  }

  public long upsertManufacturerByName(String name) {
    if (name == null || name.isBlank()) return 0L;
    String find = "SELECT id FROM societes WHERE UPPER(type) IN ('MANUFACTURER','FABRICANT','FAB') AND UPPER(nom)=UPPER(?) LIMIT 1";
    try (Connection c = DB.getConnection()) {
      try (PreparedStatement ps = c.prepareStatement(find)) {
        ps.setString(1, name);
        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
      }
      try (PreparedStatement ins = c.prepareStatement("INSERT INTO societes(type, nom) VALUES(?,?)", Statement.RETURN_GENERATED_KEYS)) {
        ins.setString(1, "FABRICANT");
        ins.setString(2, name);
        ins.executeUpdate();
        try (ResultSet k = ins.getGeneratedKeys()) { if (k.next()) return k.getLong(1); }
      }
      return 0L;
    } catch (SQLException e) { throw new RuntimeException("upsertManufacturerByName failed", e); }
  }

  public List<Societe> findByType(String type) {
    String sql = "SELECT id,type,nom,email,phone,adresse,notes,created_at FROM societes WHERE UPPER(type)=UPPER(?) ORDER BY nom";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, type);
      try (ResultSet rs = ps.executeQuery()) {
        List<Societe> out = new ArrayList<>();
        while (rs.next()) out.add(map(rs));
        return out;
      }
    } catch (SQLException e) { throw new RuntimeException("findByType failed", e); }
  }

  public Optional<Societe> findByNameAndType(String name, String type) {
    String sql = "SELECT id,type,nom,email,phone,adresse,notes,created_at FROM societes WHERE nom=? AND UPPER(type)=UPPER(?) LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setString(2, type);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findByNameAndType failed", e); }
  }

  public Optional<Societe> findByNameAndTypeIgnoreCase(String name, String type) {
    String sql = "SELECT id,type,nom,email,phone,adresse,notes,created_at FROM societes WHERE UPPER(nom)=UPPER(?) AND UPPER(type)=UPPER(?) LIMIT 1";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, name);
      ps.setString(2, type);
      try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(map(rs)) : Optional.empty(); }
    } catch (SQLException e) { throw new RuntimeException("findByNameAndTypeIgnoreCase failed", e); }
  }

  public long insert(String type, String nom, String email, String phone, String adresse, String notes) {
    String sql = "INSERT INTO societes(type,nom,email,phone,adresse,notes) VALUES(?,?,?,?,?,?)";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, type);
      ps.setString(2, nom);
      ps.setString(3, email);
      ps.setString(4, phone);
      ps.setString(5, adresse);
      ps.setString(6, notes);
      ps.executeUpdate();
      try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getLong(1) : 0L; }
    } catch (SQLException e) { throw new RuntimeException("insert failed", e); }
  }

  public boolean update(Societe s) {
    String sql = "UPDATE societes SET type=?, nom=?, email=?, phone=?, adresse=?, notes=? WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, s.type());
      ps.setString(2, s.nom());
      ps.setString(3, s.email());
      ps.setString(4, s.phone());
      ps.setString(5, s.adresse());
      ps.setString(6, s.notes());
      ps.setLong(7, s.id());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("update failed", e); }
  }

  public boolean update(long id, String type, String nom, String email, String phone, String adresse, String notes) {
    String sql = "UPDATE societes SET type=?, nom=?, email=?, phone=?, adresse=?, notes=? WHERE id=?";
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, type);
      ps.setString(2, nom);
      ps.setString(3, email);
      ps.setString(4, phone);
      ps.setString(5, adresse);
      ps.setString(6, notes);
      ps.setLong(7, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("update(id,...) failed", e); }
  }

  public boolean delete(long id) {
    try (Connection c = DB.getConnection(); PreparedStatement ps = c.prepareStatement("DELETE FROM societes WHERE id=?")) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) { throw new RuntimeException("delete failed", e); }
  }
}