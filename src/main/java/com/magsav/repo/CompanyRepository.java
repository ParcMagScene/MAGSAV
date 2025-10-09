package com.magsav.repo;

import com.magsav.model.Company;
import com.magsav.util.AppLogger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des sociétés (fabricants, clients, administrations, etc.)
 */
public class CompanyRepository {
    
    // Requêtes SQL
    private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS companies (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            legal_name TEXT,
            type TEXT NOT NULL DEFAULT 'COMPANY',
            siret TEXT,
            address TEXT,
            postal_code TEXT,
            city TEXT,
            country TEXT DEFAULT 'France',
            phone TEXT,
            email TEXT,
            website TEXT,
            description TEXT,
            logo_path TEXT,
            sector TEXT,
            is_active INTEGER DEFAULT 1,
            created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL
        )
    """;
    
    private static final String INSERT_COMPANY = """
        INSERT INTO companies (name, legal_name, type, siret, address, postal_code, city, country, 
                              phone, email, website, description, logo_path, sector, is_active, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
    
    private static final String UPDATE_COMPANY = """
        UPDATE companies SET name = ?, legal_name = ?, type = ?, siret = ?, address = ?, postal_code = ?, 
                            city = ?, country = ?, phone = ?, email = ?, website = ?, description = ?, 
                            logo_path = ?, sector = ?, is_active = ?, updated_at = ?
        WHERE id = ?
    """;
    
    private static final String SELECT_ALL = "SELECT * FROM companies ORDER BY name";
    private static final String SELECT_BY_ID = "SELECT * FROM companies WHERE id = ?";
    private static final String SELECT_BY_TYPE = "SELECT * FROM companies WHERE type = ? ORDER BY name";
    private static final String SELECT_ACTIVE = "SELECT * FROM companies WHERE is_active = 1 ORDER BY name";
    private static final String DELETE_BY_ID = "DELETE FROM companies WHERE id = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM companies";
    
    private final Connection connection;
    
    public CompanyRepository(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }
    
    private void createTableIfNotExists() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TABLE);
            AppLogger.info("CompanyRepository", "Table companies créée ou vérifiée");
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la création de la table companies", e);
            throw new RuntimeException("Impossible de créer la table companies", e);
        }
    }
    
    /**
     * Sauvegarde ou met à jour une société
     */
    public Company save(Company company) {
        if (company.getId() == null) {
            return insert(company);
        } else {
            return update(company);
        }
    }
    
    private Company insert(Company company) {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_COMPANY, Statement.RETURN_GENERATED_KEYS)) {
            setCompanyParameters(stmt, company);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion, aucune ligne affectée");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    company.setId(generatedKeys.getLong(1));
                    AppLogger.info("CompanyRepository", "Société insérée avec l'ID: " + company.getId());
                    return company;
                } else {
                    throw new SQLException("Échec de l'insertion, aucun ID généré");
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de l'insertion de la société: " + company.getName(), e);
            throw new RuntimeException("Impossible d'insérer la société", e);
        }
    }
    
    private Company update(Company company) {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_COMPANY)) {
            setCompanyParameters(stmt, company);
            stmt.setLong(17, company.getId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la mise à jour, aucune ligne affectée");
            }
            
            AppLogger.info("CompanyRepository", "Société mise à jour: " + company.getName());
            return company;
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la mise à jour de la société: " + company.getName(), e);
            throw new RuntimeException("Impossible de mettre à jour la société", e);
        }
    }
    
    private void setCompanyParameters(PreparedStatement stmt, Company company) throws SQLException {
        stmt.setString(1, company.getName());
        stmt.setString(2, company.getLegalName());
        stmt.setString(3, company.getType() != null ? company.getType().name() : Company.CompanyType.COMPANY.name());
        stmt.setString(4, company.getSiret());
        stmt.setString(5, company.getAddress());
        stmt.setString(6, company.getPostalCode());
        stmt.setString(7, company.getCity());
        stmt.setString(8, company.getCountry());
        stmt.setString(9, company.getPhone());
        stmt.setString(10, company.getEmail());
        stmt.setString(11, company.getWebsite());
        stmt.setString(12, company.getDescription());
        stmt.setString(13, company.getLogoPath());
        stmt.setString(14, company.getSector());
        stmt.setInt(15, company.isActive() ? 1 : 0);
        stmt.setString(16, company.getCreatedAt().toString());
        stmt.setString(17, company.getUpdatedAt().toString());
    }
    
    /**
     * Trouve une société par son ID
     */
    public Optional<Company> findById(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCompany(rs));
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la recherche de la société par ID: " + id, e);
        }
        return Optional.empty();
    }
    
    /**
     * Retourne toutes les sociétés
     */
    public List<Company> findAll() {
        List<Company> companies = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            
            while (rs.next()) {
                companies.add(mapResultSetToCompany(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la récupération de toutes les sociétés", e);
        }
        return companies;
    }
    
    /**
     * Retourne les sociétés par type
     */
    public List<Company> findByType(Company.CompanyType type) {
        List<Company> companies = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT_BY_TYPE)) {
            stmt.setString(1, type.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    companies.add(mapResultSetToCompany(rs));
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la recherche des sociétés par type: " + type, e);
        }
        return companies;
    }
    
    /**
     * Retourne toutes les sociétés actives
     */
    public List<Company> findActive() {
        List<Company> companies = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ACTIVE)) {
            
            while (rs.next()) {
                companies.add(mapResultSetToCompany(rs));
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la récupération des sociétés actives", e);
        }
        return companies;
    }
    
    /**
     * Supprime une société
     */
    public boolean delete(Long id) {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_BY_ID)) {
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;
            if (deleted) {
                AppLogger.info("CompanyRepository", "Société supprimée avec l'ID: " + id);
            }
            return deleted;
        } catch (SQLException e) {
            AppLogger.error("Erreur lors de la suppression de la société: " + id, e);
            return false;
        }
    }
    
    /**
     * Compte le nombre total de sociétés
     */
    public int count() {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(COUNT_ALL)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors du comptage des sociétés", e);
        }
        return 0;
    }
    
    /**
     * Trouve la société "Mag Scène" (notre société)
     */
    public Optional<Company> findMagScene() {
        List<Company> magSceneCompanies = findByType(Company.CompanyType.OWN_COMPANY);
        return magSceneCompanies.stream().findFirst();
    }
    
    /**
     * Crée la société "Mag Scène" par défaut si elle n'existe pas
     */
    public Company createDefaultMagScene() {
        Optional<Company> existing = findMagScene();
        if (existing.isPresent()) {
            return existing.get();
        }
        
        Company magScene = new Company("Mag Scène", "Mag Scène", Company.CompanyType.OWN_COMPANY);
        magScene.setDescription("Société de service audiovisuel et technique");
        magScene.setSector("Audiovisuel");
        magScene.setCity("France");
        magScene.setEmail("contact@magscene.fr");
        
        return save(magScene);
    }
    
    private Company mapResultSetToCompany(ResultSet rs) throws SQLException {
        Company company = new Company();
        company.setId(rs.getLong("id"));
        company.setName(rs.getString("name"));
        company.setLegalName(rs.getString("legal_name"));
        
        String typeStr = rs.getString("type");
        try {
            company.setType(Company.CompanyType.valueOf(typeStr));
        } catch (IllegalArgumentException e) {
            company.setType(Company.CompanyType.COMPANY);
        }
        
        company.setSiret(rs.getString("siret"));
        company.setAddress(rs.getString("address"));
        company.setPostalCode(rs.getString("postal_code"));
        company.setCity(rs.getString("city"));
        company.setCountry(rs.getString("country"));
        company.setPhone(rs.getString("phone"));
        company.setEmail(rs.getString("email"));
        company.setWebsite(rs.getString("website"));
        company.setDescription(rs.getString("description"));
        company.setLogoPath(rs.getString("logo_path"));
        company.setSector(rs.getString("sector"));
        company.setActive(rs.getInt("is_active") == 1);
        
        String createdAtStr = rs.getString("created_at");
        if (createdAtStr != null) {
            company.setCreatedAt(LocalDateTime.parse(createdAtStr));
        }
        
        String updatedAtStr = rs.getString("updated_at");
        if (updatedAtStr != null) {
            company.setUpdatedAt(LocalDateTime.parse(updatedAtStr));
        }
        
        return company;
    }
}