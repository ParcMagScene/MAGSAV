package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.GoogleServicesConfig;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des configurations Google Services
 */
public class GoogleServicesConfigRepository {
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Crée la table si elle n'existe pas
     */
    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS configuration_google (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                client_id TEXT NOT NULL,
                client_secret TEXT NOT NULL,
                redirect_uri TEXT DEFAULT 'http://localhost:8080/oauth/callback',
                scopes TEXT DEFAULT 'https://www.googleapis.com/auth/calendar https://www.googleapis.com/auth/gmail.send https://www.googleapis.com/auth/contacts.readonly',
                access_token TEXT,
                refresh_token TEXT,
                token_type TEXT DEFAULT 'Bearer',
                token_expires_in INTEGER DEFAULT 0,
                calendrier_principal TEXT DEFAULT 'primary',
                sync_calendar_actif INTEGER DEFAULT 1,
                intervalle_sync INTEGER DEFAULT 15,
                gmail_actif INTEGER DEFAULT 1,
                email_expediteur TEXT,
                signature_email TEXT,
                contacts_actif INTEGER DEFAULT 1,
                sync_contacts_auto INTEGER DEFAULT 0,
                date_creation TEXT NOT NULL,
                date_modification TEXT NOT NULL,
                actif INTEGER DEFAULT 1
            )
            """;
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(sql);
            AppLogger.info("db", "Table configuration_google créée ou vérifiée");
            
        } catch (SQLException e) {
            AppLogger.error("Erreur création table configuration_google: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Sauvegarde une configuration (create ou update)
     */
    public boolean save(GoogleServicesConfig config) {
        createTableIfNotExists();
        
        if (config.getId() == 0) {
            return create(config);
        } else {
            return update(config);
        }
    }
    
    /**
     * Crée une nouvelle configuration
     */
    private boolean create(GoogleServicesConfig config) {
        String sql = """
            INSERT INTO configuration_google (
                nom, client_id, client_secret, redirect_uri, scopes,
                access_token, refresh_token, token_type, token_expires_in,
                calendrier_principal, sync_calendar_actif, intervalle_sync,
                gmail_actif, email_expediteur, signature_email,
                contacts_actif, sync_contacts_auto,
                date_creation, date_modification, actif
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            String now = LocalDateTime.now().format(formatter);
            
            stmt.setString(1, config.getNom());
            stmt.setString(2, config.getClientId());
            stmt.setString(3, config.getClientSecret());
            stmt.setString(4, config.getRedirectUri());
            stmt.setString(5, config.getScopes());
            stmt.setString(6, config.getAccessToken());
            stmt.setString(7, config.getRefreshToken());
            stmt.setString(8, config.getTokenType());
            stmt.setLong(9, config.getTokenExpiresIn());
            stmt.setString(10, config.getCalendrierPrincipal());
            stmt.setInt(11, config.isSyncCalendarActif() ? 1 : 0);
            stmt.setInt(12, config.getIntervalleSync());
            stmt.setInt(13, config.isGmailActif() ? 1 : 0);
            stmt.setString(14, config.getEmailExpéditeur());
            stmt.setString(15, config.getSignatureEmail());
            stmt.setInt(16, config.isContactsActif() ? 1 : 0);
            stmt.setInt(17, config.isSyncContactsAuto() ? 1 : 0);
            stmt.setString(18, now);
            stmt.setString(19, now);
            stmt.setInt(20, config.isActif() ? 1 : 0);
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    config.setId(keys.getInt(1));
                }
                config.setDateCreation(now);
                config.setDateModification(now);
                AppLogger.info("db", "Configuration Google créée avec ID: " + config.getId());
                return true;
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur création configuration Google: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Met à jour une configuration existante
     */
    private boolean update(GoogleServicesConfig config) {
        String sql = """
            UPDATE configuration_google SET
                nom = ?, client_id = ?, client_secret = ?, redirect_uri = ?, scopes = ?,
                access_token = ?, refresh_token = ?, token_type = ?, token_expires_in = ?,
                calendrier_principal = ?, sync_calendar_actif = ?, intervalle_sync = ?,
                gmail_actif = ?, email_expediteur = ?, signature_email = ?,
                contacts_actif = ?, sync_contacts_auto = ?,
                date_modification = ?, actif = ?
            WHERE id = ?
            """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String now = LocalDateTime.now().format(formatter);
            
            stmt.setString(1, config.getNom());
            stmt.setString(2, config.getClientId());
            stmt.setString(3, config.getClientSecret());
            stmt.setString(4, config.getRedirectUri());
            stmt.setString(5, config.getScopes());
            stmt.setString(6, config.getAccessToken());
            stmt.setString(7, config.getRefreshToken());
            stmt.setString(8, config.getTokenType());
            stmt.setLong(9, config.getTokenExpiresIn());
            stmt.setString(10, config.getCalendrierPrincipal());
            stmt.setInt(11, config.isSyncCalendarActif() ? 1 : 0);
            stmt.setInt(12, config.getIntervalleSync());
            stmt.setInt(13, config.isGmailActif() ? 1 : 0);
            stmt.setString(14, config.getEmailExpéditeur());
            stmt.setString(15, config.getSignatureEmail());
            stmt.setInt(16, config.isContactsActif() ? 1 : 0);
            stmt.setInt(17, config.isSyncContactsAuto() ? 1 : 0);
            stmt.setString(18, now);
            stmt.setInt(19, config.isActif() ? 1 : 0);
            stmt.setInt(20, config.getId());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                config.setDateModification(now);
                AppLogger.info("db", "Configuration Google mise à jour: " + config.getId());
                return true;
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur mise à jour configuration Google: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Trouve une configuration par ID
     */
    public Optional<GoogleServicesConfig> findById(int id) {
        createTableIfNotExists();
        
        String sql = "SELECT * FROM configuration_google WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToConfig(rs));
                }
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur recherche configuration Google: " + e.getMessage());
        }
        
        return Optional.empty();
    }
    
    /**
     * Récupère toutes les configurations
     */
    public List<GoogleServicesConfig> findAll() {
        createTableIfNotExists();
        
        List<GoogleServicesConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM configuration_google ORDER BY actif DESC, date_creation DESC";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                configs.add(mapRowToConfig(rs));
            }
            
            AppLogger.info("db", "Configurations Google récupérées: " + configs.size());
            
        } catch (SQLException e) {
            AppLogger.error("Erreur récupération configurations Google: " + e.getMessage());
        }
        
        return configs;
    }
    
    /**
     * Trouve les configurations actives
     */
    public List<GoogleServicesConfig> findActive() {
        createTableIfNotExists();
        
        List<GoogleServicesConfig> configs = new ArrayList<>();
        String sql = "SELECT * FROM configuration_google WHERE actif = 1 ORDER BY date_creation DESC";
        
        try (Connection conn = DB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                configs.add(mapRowToConfig(rs));
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur récupération configurations Google actives: " + e.getMessage());
        }
        
        return configs;
    }
    
    /**
     * Supprime une configuration
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM configuration_google WHERE id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                AppLogger.info("db", "Configuration Google supprimée: " + id);
                return true;
            }
            
        } catch (SQLException e) {
            AppLogger.error("Erreur suppression configuration Google: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Map une ligne de résultat vers un objet GoogleServicesConfig
     */
    private GoogleServicesConfig mapRowToConfig(ResultSet rs) throws SQLException {
        GoogleServicesConfig config = new GoogleServicesConfig();
        
        config.setId(rs.getInt("id"));
        config.setNom(rs.getString("nom"));
        config.setClientId(rs.getString("client_id"));
        config.setClientSecret(rs.getString("client_secret"));
        config.setRedirectUri(rs.getString("redirect_uri"));
        config.setScopes(rs.getString("scopes"));
        config.setAccessToken(rs.getString("access_token"));
        config.setRefreshToken(rs.getString("refresh_token"));
        config.setTokenType(rs.getString("token_type"));
        config.setTokenExpiresIn(rs.getLong("token_expires_in"));
        config.setCalendrierPrincipal(rs.getString("calendrier_principal"));
        config.setSyncCalendarActif(rs.getInt("sync_calendar_actif") == 1);
        config.setIntervalleSync(rs.getInt("intervalle_sync"));
        config.setGmailActif(rs.getInt("gmail_actif") == 1);
        config.setEmailExpéditeur(rs.getString("email_expediteur"));
        config.setSignatureEmail(rs.getString("signature_email"));
        config.setContactsActif(rs.getInt("contacts_actif") == 1);
        config.setSyncContactsAuto(rs.getInt("sync_contacts_auto") == 1);
        config.setDateCreation(rs.getString("date_creation"));
        config.setDateModification(rs.getString("date_modification"));
        config.setActif(rs.getInt("actif") == 1);
        
        return config;
    }
}