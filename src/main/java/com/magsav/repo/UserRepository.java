package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.User;
import com.magsav.model.User.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des utilisateurs
 */
public class UserRepository {
    
    /**
     * S'assure que les nouvelles colonnes existent dans la table users
     */
    public static void ensureUserTableUpdated() {
        String[] alterStatements = {
            "ALTER TABLE users ADD COLUMN company_id INTEGER DEFAULT NULL",
            "ALTER TABLE users ADD COLUMN position TEXT DEFAULT NULL",
            "ALTER TABLE users ADD COLUMN avatar_path TEXT DEFAULT NULL"
        };
        
        try (Connection conn = DB.getConnection()) {
            for (String alterSql : alterStatements) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(alterSql);
                } catch (SQLException e) {
                    // Ignore l'erreur si la colonne existe déjà
                    if (!e.getMessage().contains("duplicate column name")) {
                        System.err.println("Erreur lors de l'ajout de colonne: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de la table users: " + e.getMessage());
        }
    }
    
    /**
     * Crée un nouvel utilisateur
     */
    public long createUser(User user) {
        ensureUserTableUpdated(); // S'assurer que les nouvelles colonnes existent
        
        String sql = """
            INSERT INTO users (username, email, password_hash, role, full_name, phone, 
                             company_id, position, avatar_path, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.username());
            stmt.setString(2, user.email());
            stmt.setString(3, user.passwordHash());
            stmt.setString(4, user.role().name());
            stmt.setString(5, user.fullName());
            stmt.setString(6, user.phone());
            
            if (user.companyId() != null) {
                stmt.setLong(7, user.companyId());
            } else {
                stmt.setNull(7, Types.BIGINT);
            }
            
            stmt.setString(8, user.position());
            stmt.setString(9, user.avatarPath());
            stmt.setBoolean(10, user.isActive());
            stmt.setString(11, user.createdAt().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Échec de la création de l'utilisateur");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                } else {
                    throw new RuntimeException("Impossible de récupérer l'ID de l'utilisateur créé");
                }
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                if (e.getMessage().contains("username")) {
                    throw new RuntimeException("Ce nom d'utilisateur existe déjà");
                } else if (e.getMessage().contains("email")) {
                    throw new RuntimeException("Cet email est déjà utilisé");
                }
            }
            throw new RuntimeException("Erreur lors de la création de l'utilisateur", e);
        }
    }
    
    /**
     * Trouve un utilisateur par nom d'utilisateur
     */
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT id, username, email, password_hash, role, full_name, phone, 
                   company_id, position, avatar_path, is_active, created_at, last_login, reset_token, reset_token_expires
            FROM users WHERE username = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Trouve un utilisateur par email
     */
    public Optional<User> findByEmail(String email) {
        String sql = """
            SELECT id, username, email, password_hash, role, full_name, phone, 
                   company_id, position, avatar_path, is_active, created_at, last_login, reset_token, reset_token_expires
            FROM users WHERE email = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur par email", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Trouve un utilisateur par ID
     */
    public Optional<User> findById(long id) {
        String sql = """
            SELECT id, username, email, password_hash, role, full_name, phone, 
                   is_active, created_at, last_login, reset_token, reset_token_expires
            FROM users WHERE id = ?
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur par ID", e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère tous les utilisateurs par rôle
     */
    public List<User> findByRole(Role role) {
        String sql = """
            SELECT id, username, email, password_hash, role, full_name, phone, 
                   is_active, created_at, last_login, reset_token, reset_token_expires
            FROM users WHERE role = ?
            ORDER BY created_at DESC
        """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, role.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la récupération des utilisateurs par rôle", e);
        }
        
        return users;
    }
    
    /**
     * Met à jour la date de dernière connexion
     */
    public void updateLastLogin(long userId, LocalDateTime lastLogin) {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, lastLogin.toString());
            stmt.setLong(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Aucun utilisateur trouvé avec l'ID: " + userId);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la dernière connexion", e);
        }
    }
    
    /**
     * Met à jour le mot de passe d'un utilisateur
     */
    public void updatePassword(long userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, reset_token = NULL, reset_token_expires = NULL WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Aucun utilisateur trouvé avec l'ID: " + userId);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du mot de passe", e);
        }
    }
    
    /**
     * Met à jour le token de reset de mot de passe
     */
    public void updateResetToken(long userId, String resetToken, LocalDateTime expires) {
        String sql = "UPDATE users SET reset_token = ?, reset_token_expires = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, resetToken);
            stmt.setString(2, expires != null ? expires.toString() : null);
            stmt.setLong(3, userId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Aucun utilisateur trouvé avec l'ID: " + userId);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du token de reset", e);
        }
    }
    
    /**
     * Active ou désactive un utilisateur
     */
    public void updateActiveStatus(long userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, isActive);
            stmt.setLong(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Aucun utilisateur trouvé avec l'ID: " + userId);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du statut de l'utilisateur", e);
        }
    }
    
    /**
     * Met à jour le rôle d'un utilisateur
     */
    public boolean updateUserRole(Integer userId, Role newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newRole.name());
            stmt.setInt(2, userId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du rôle de l'utilisateur", e);
        }
    }
    
    /**
     * Vérifie si un nom d'utilisateur existe
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de l'existence du nom d'utilisateur", e);
        }
    }
    
    /**
     * Vérifie si un email existe
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la vérification de l'existence de l'email", e);
        }
    }
    
    /**
     * Mappe un ResultSet vers un objet User
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        // Gestion du company_id qui peut être null
        Long companyId = null;
        long companyIdValue = rs.getLong("company_id");
        if (!rs.wasNull()) {
            companyId = companyIdValue;
        }
        
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"),
            Role.fromString(rs.getString("role")),
            rs.getString("full_name"),
            rs.getString("phone"),
            companyId,
            rs.getString("position"),
            rs.getString("avatar_path"),
            rs.getBoolean("is_active"),
            LocalDateTime.parse(rs.getString("created_at")),
            rs.getString("last_login") != null ? LocalDateTime.parse(rs.getString("last_login")) : null,
            rs.getString("reset_token"),
            rs.getString("reset_token_expires") != null ? LocalDateTime.parse(rs.getString("reset_token_expires")) : null
        );
    }
    
    /**
     * Trouve tous les utilisateurs d'une société
     */
    public List<User> findByCompanyId(Long companyId) {
        String sql = """
            SELECT id, username, email, password_hash, role, full_name, phone, 
                   company_id, position, avatar_path, is_active, created_at, last_login, reset_token, reset_token_expires
            FROM users WHERE company_id = ? ORDER BY full_name
        """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, companyId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la recherche des utilisateurs par société", e);
        }
        
        return users;
    }
    
    /**
     * Met à jour la société d'un utilisateur
     */
    public void updateUserCompany(int userId, Long companyId) {
        String sql = "UPDATE users SET company_id = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (companyId != null) {
                stmt.setLong(1, companyId);
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de la société de l'utilisateur", e);
        }
    }
    
    /**
     * Met à jour le poste d'un utilisateur
     */
    public void updateUserPosition(int userId, String position) {
        String sql = "UPDATE users SET position = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, position);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour du poste de l'utilisateur", e);
        }
    }
    
    /**
     * Met à jour l'avatar d'un utilisateur
     */
    public void updateUserAvatar(int userId, String avatarPath) {
        String sql = "UPDATE users SET avatar_path = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, avatarPath);
            stmt.setInt(2, userId);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la mise à jour de l'avatar de l'utilisateur", e);
        }
    }
}