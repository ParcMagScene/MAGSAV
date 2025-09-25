package com.magsav.repo;

import com.magsav.model.Client;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {
    private final DataSource ds;
    public ClientRepository(DataSource ds) { this.ds = ds; }
    public DataSource getDataSource() { return ds; }

    public Client upsertByEmail(Client c) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            // try update
            String update = "UPDATE clients SET nom=?, prenom=?, tel=?, adresse=? WHERE email=?";
            try (PreparedStatement ps = conn.prepareStatement(update)) {
                ps.setString(1, c.nom());
                ps.setString(2, c.prenom());
                ps.setString(3, c.tel());
                ps.setString(4, c.adresse());
                ps.setString(5, c.email());
                int count = ps.executeUpdate();
                if (count > 0) {
                    return findByEmail(c.email());
                }
            }
            String insert = "INSERT OR IGNORE INTO clients(nom, prenom, email, tel, adresse) VALUES(?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(insert)) {
                ps.setString(1, c.nom());
                ps.setString(2, c.prenom());
                ps.setString(3, c.email());
                ps.setString(4, c.tel());
                ps.setString(5, c.adresse());
                ps.executeUpdate();
            }
            return findByEmail(c.email());
        }
    }

    public Client findByEmail(String email) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nom, prenom, email, tel, adresse FROM clients WHERE email=?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Client(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                }
                return null;
            }
        }
    }
    
    public Client findById(Long id) throws SQLException {
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nom, prenom, email, tel, adresse FROM clients WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Client(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6));
                }
                return null;
            }
        }
    }

    public List<Client> findAll() throws SQLException {
        List<Client> list = new ArrayList<>();
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nom, prenom, email, tel, adresse FROM clients ORDER BY nom, prenom")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Client(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        }
        return list;
    }

    public List<Client> search(String term) throws SQLException {
        List<Client> list = new ArrayList<>();
        String like = "%" + term.toLowerCase() + "%";
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nom, prenom, email, tel, adresse FROM clients WHERE lower(nom) LIKE ? OR lower(prenom) LIKE ? OR lower(email) LIKE ? ORDER BY nom, prenom")) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Client(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        }
        return list;
    }
}
