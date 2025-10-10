package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.model.Societe;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SocieteRepository {

  public long insert(String type, String nom, String email, String phone, String adresse, String notes) {
    try (Connection conn = DB.getConnection()) {
      String sql = "INSERT INTO societes (type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe) VALUES (?, ?, ?, ?, ?, ?)";
      PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      stmt.setString(1, type);
      stmt.setString(2, nom);
      stmt.setString(3, email);
      stmt.setString(4, phone);
      stmt.setString(5, adresse);
      stmt.setString(6, notes);
      stmt.executeUpdate();
      ResultSet rs = stmt.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur insertion société", e);
    }
    return -1;
  }

  public Optional<Societe> findById(long id) {
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT id, type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe, date_creation FROM societes WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, id);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(new Societe(
            rs.getLong("id"),
            rs.getString("type_societe"),
            rs.getString("nom_societe"),
            rs.getString("email_societe"),
            rs.getString("telephone_societe"),
            rs.getString("adresse_societe"),
            rs.getString("notes_societe"),
            rs.getString("date_creation")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur recherche société", e);
    }
    return Optional.empty();
  }

  public List<Societe> findByType(String type) {
    List<Societe> societes = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT id, type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe, date_creation FROM societes WHERE type_societe = ? ORDER BY nom_societe";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, type);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        societes.add(new Societe(
            rs.getLong("id"),
            rs.getString("type_societe"),
            rs.getString("nom_societe"),
            rs.getString("email_societe"),
            rs.getString("telephone_societe"),
            rs.getString("adresse_societe"),
            rs.getString("notes_societe"),
            rs.getString("date_creation")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération sociétés par type", e);
    }
    return societes;
  }

  public List<Societe> findAll() {
    List<Societe> societes = new ArrayList<>();
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT id, type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe, date_creation FROM societes ORDER BY nom_societe";
      PreparedStatement stmt = conn.prepareStatement(sql);
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        societes.add(new Societe(
            rs.getLong("id"),
            rs.getString("type_societe"),
            rs.getString("nom_societe"),
            rs.getString("email_societe"),
            rs.getString("telephone_societe"),
            rs.getString("adresse_societe"),
            rs.getString("notes_societe"),
            rs.getString("date_creation")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur récupération toutes sociétés", e);
    }
    return societes;
  }

  public void update(Societe societe) {
    try (Connection conn = DB.getConnection()) {
      String sql = "UPDATE societes SET type_societe = ?, nom_societe = ?, email_societe = ?, telephone_societe = ?, adresse_societe = ?, notes_societe = ? WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, societe.type());
      stmt.setString(2, societe.nom());
      stmt.setString(3, societe.email());
      stmt.setString(4, societe.phone());
      stmt.setString(5, societe.adresse());
      stmt.setString(6, societe.notes());
      stmt.setLong(7, societe.id());
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Erreur mise à jour société", e);
    }
  }

  public void update(long id, String type, String nom, String email, String phone, String adresse, String notes) {
    try (Connection conn = DB.getConnection()) {
      String sql = "UPDATE societes SET type_societe = ?, nom_societe = ?, email_societe = ?, telephone_societe = ?, adresse_societe = ?, notes_societe = ? WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, type);
      stmt.setString(2, nom);
      stmt.setString(3, email);
      stmt.setString(4, phone);
      stmt.setString(5, adresse);
      stmt.setString(6, notes);
      stmt.setLong(7, id);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Erreur mise à jour société", e);
    }
  }

  public boolean delete(long id) {
    try (Connection conn = DB.getConnection()) {
      String sql = "DELETE FROM societes WHERE id = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setLong(1, id);
      int rowsAffected = stmt.executeUpdate();
      return rowsAffected > 0;
    } catch (SQLException e) {
      throw new DatabaseException("Erreur suppression société", e);
    }
  }

  public List<Societe> listManufacturers() {
    return findByType("MANUFACTURER");
  }

  public Optional<Societe> findByNameAndType(String nom, String type) {
    try (Connection conn = DB.getConnection()) {
      String sql = "SELECT id, type_societe, nom_societe, email_societe, telephone_societe, adresse_societe, notes_societe, date_creation FROM societes WHERE nom_societe = ? AND type_societe = ?";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, nom);
      stmt.setString(2, type);
      ResultSet rs = stmt.executeQuery();
      if (rs.next()) {
        return Optional.of(new Societe(
            rs.getLong("id"),
            rs.getString("type_societe"),
            rs.getString("nom_societe"),
            rs.getString("email_societe"),
            rs.getString("telephone_societe"),
            rs.getString("adresse_societe"),
            rs.getString("notes_societe"),
            rs.getString("date_creation")
        ));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Erreur recherche société par nom et type", e);
    }
    return Optional.empty();
  }
}
