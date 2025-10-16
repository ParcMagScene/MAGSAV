package com.magsav.repo;

import com.magsav.cache.CacheManager;
import com.magsav.db.DB;
import com.magsav.model.User;
import com.magsav.model.User.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des utilisateurs avec cache optimisé
 */
public class UserRepository {
    
    private static final CacheManager cache = CacheManager.getInstance();
    
    // Formateur pour les dates (format: 2025-10-13 16:03:18)
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Parse une date vers LocalDateTime
     */
    private static LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateStr, DATETIME_FORMAT);
        } catch (Exception e) {
            // Fallback: essayer le format ISO par défaut
            try {
                return LocalDateTime.parse(dateStr);
            } catch (Exception e2) {
                System.err.println("Impossible de parser la date: " + dateStr + " - Erreur: " + e2.getMessage());
                return null;
            }
        }
    }
    
    /**
     * S'assure que la table users est créée (utilisée lors de la création de table)
     * Note: Maintenant gérée par DB.ensureSchema()
     */
    public static void ensureUserTableUpdated() {
        // La table users est maintenant créée directement dans DB.ensureSchema()
        // Cette méthode est conservée pour la compatibilité mais ne fait plus rien
    }
    
    /**
     * Crée un nouvel utilisateur
     */
    public long createUser(User user) {
        ensureUserTableUpdated(); // S'assurer que les nouvelles colonnes existent
        
        String sql = """
            INSERT INTO users (username, email, password_hash, role, nom, prenom, phone, 
                             societe_id, position, avatar_path, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, user.username());
            stmt.setString(2, user.email());
            stmt.setString(3, user.passwordHash());
            stmt.setString(4, user.role().name());
            
            // Séparer fullName en nom et prenom
            String[] nameParts = user.fullName() != null ? user.fullName().split(" ", 2) : new String[]{"", ""};
            stmt.setString(5, nameParts.length > 0 ? nameParts[0] : ""); // nom
            stmt.setString(6, nameParts.length > 1 ? nameParts[1] : ""); // prenom
            
            stmt.setString(7, user.phone());
            
            if (user.societeId() != null) {
                stmt.setLong(8, user.societeId());
            } else {
                stmt.setNull(8, Types.BIGINT);
            }
            
            stmt.setString(9, user.position());
            stmt.setString(10, user.avatarPath());
            stmt.setBoolean(11, user.isActive());
            stmt.setString(12, user.createdAt().toString());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new RuntimeException("Échec de la création de l'utilisateur");
            }
            
            // Invalider le cache
            invalidateUserCache();
            
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
     * Trouve un utilisateur par nom d'utilisateur (avec cache)
     */
    public Optional<User> findByUsername(String username) {
        return cache.get("user:username:" + username, () -> {
            String sql = """
                SELECT id, username, email, password_hash, role, nom, prenom, phone, 
                       societe_id, position, avatar_path, is_active, created_at, last_login, reset_token, reset_token_expires
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
            
            return Optional.<User>empty();
        });
    }
    
    /**
     * Trouve un utilisateur par email (avec cache)
     */
    public Optional<User> findByEmail(String email) {
        return cache.get("user:email:" + email, () -> {
            String sql = """
                SELECT id, username, email, password_hash, role, nom, prenom, phone, 
                       societe_id, position, avatar_path, is_active, created_at, last_login, reset_token, reset_token_expires
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
            
            return Optional.<User>empty();
        });
    }
    
    /**
     * Trouve un utilisateur par ID
     */
    public Optional<User> findById(long id) {
        String sql = """
            SELECT id, username, email, password_hash, role, nom, prenom, phone, 
                   societe_id, position, avatar_path, is_active, created_at, 
                   last_login, reset_token, reset_token_expires
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
            SELECT id, username, email, password_hash, role, nom, prenom, phone, 
                   societe_id, position, avatar_path, is_active, created_at, 
                   last_login, reset_token, reset_token_expires
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
     * Récupère tous les utilisateurs (avec cache - TTL plus court)
     */
    public List<User> findAll() {
        return cache.get("users:all", () -> {
            String sql = """
                SELECT id, username, email, password_hash, role, nom, prenom, phone, 
                       societe_id, position, avatar_path, is_active, created_at, 
                       last_login, reset_token, reset_token_expires
                FROM users 
                ORDER BY created_at DESC
            """;
            
            List<User> users = new ArrayList<>();
            
            try (Connection conn = DB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
                
            } catch (SQLException e) {
                throw new RuntimeException("Erreur lors de la récupération de tous les utilisateurs", e);
            }
            
            return users;
        }, 2); // TTL de 2 minutes pour les listes
    }
    
    /**
     * Supprime un utilisateur par ID
     */
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            int affected = stmt.executeUpdate();
            
            if (affected == 0) {
                throw new RuntimeException("Aucun utilisateur trouvé avec l'ID: " + userId);
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur", e);
        }
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
        // Gestion du societe_id qui peut être null
        Long societeId = null;
        long societeIdValue = rs.getLong("societe_id");
        if (!rs.wasNull()) {
            societeId = societeIdValue;
        }
        
        // Reconstruire fullName à partir de nom et prenom
        String nom = rs.getString("nom");
        String prenom = rs.getString("prenom");
        String fullName = "";
        if (nom != null && !nom.trim().isEmpty()) {
            fullName = nom;
            if (prenom != null && !prenom.trim().isEmpty()) {
                fullName += " " + prenom;
            }
        } else if (prenom != null && !prenom.trim().isEmpty()) {
            fullName = prenom;
        }
        
        return new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash"),
            Role.fromString(rs.getString("role")),
            fullName,
            rs.getString("phone"),
            societeId,
            rs.getString("position"),
            rs.getString("avatar_path"),
            rs.getBoolean("is_active"),
            parseDateTime(rs.getString("created_at")),
            parseDateTime(rs.getString("last_login")),
            rs.getString("reset_token"),
            parseDateTime(rs.getString("reset_token_expires"))
        );
    }
    
    /**
     * Trouve tous les utilisateurs d'une société
     */
    public List<User> findBySocieteId(Long societeId) {
        String sql = """
            SELECT id, username, email, password_hash, role, nom, prenom, phone, 
                   societe_id, position, avatar_path, is_active, created_at, last_login, reset_token, reset_token_expires
            FROM users WHERE societe_id = ? ORDER BY nom, prenom
        """;
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, societeId);
            
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
    public void updateUserSociete(int userId, Long societeId) {
        String sql = "UPDATE users SET societe_id = ? WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (societeId != null) {
                stmt.setLong(1, societeId);
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
        
        // Invalider le cache après modification
        invalidateUserCache();
    }
    
    /**
     * Invalide le cache des utilisateurs
     */
    private void invalidateUserCache() {
        cache.invalidatePrefix("user:");
        cache.invalidate("users:all");
    }
}