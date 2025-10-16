package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.exception.DatabaseException;
import com.magsav.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    public List<Client> findAll() {
        List<Client> clients = new ArrayList<>();
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT id, nom, email, telephone, adresse, created_at FROM clients ORDER BY nom";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                clients.add(new Client(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("created_at")
                    ));
                }
            }
            }
        } catch (SQLException e) {
            // Si la table n'existe pas encore, retourner une liste vide
            if (e.getMessage().contains("no such table")) {
                return clients;
            }
            throw new DatabaseException("Erreur récupération clients", e);
        }
        return clients;
    }

    public Client findById(long id) {
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT id, nom, email, telephone, adresse, created_at FROM clients WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return new Client(
                    rs.getLong("id"),
                    rs.getString("nom"),
                    rs.getString("email"),
                    rs.getString("telephone"),
                    rs.getString("adresse"),
                    rs.getString("created_at")
                    );
                }
            }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erreur récupération client", e);
        }
        return null;
    }

    public long insert(String nom, String email, String telephone, String adresse) {
        try (Connection conn = DB.getConnection()) {
            String sql = "INSERT INTO clients (nom, email, telephone, adresse, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nom);
                stmt.setString(2, email);
                stmt.setString(3, telephone);
                stmt.setString(4, adresse);
                stmt.executeUpdate();
                
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erreur insertion client", e);
        }
        return -1;
    }
}