package com.magsav.repo;

import com.magsav.model.Company;
import com.magsav.util.AppLogger;
import com.magsav.util.CompanyProtectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des sociétés (fabricants, clients, administrations, etc.)
 */
public class CompanyRepository {
    
    // Requêtes SQL - utilise la table societes existante
    private static final String INSERT_COMPANY = """
        INSERT INTO societes (nom_societe, raison_sociale, type_societe, siret, adresse_societe, code_postal, ville, pays, 
                              telephone_societe, email_societe, site_web, description, logo_path, secteur, is_active, date_creation, date_modification)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
    
    private static final String UPDATE_COMPANY = """
        UPDATE societes SET nom_societe = ?, raison_sociale = ?, type_societe = ?, siret = ?, adresse_societe = ?, code_postal = ?, 
                            ville = ?, pays = ?, telephone_societe = ?, email_societe = ?, site_web = ?, description = ?, 
                            logo_path = ?, secteur = ?, is_active = ?, date_modification = ?
        WHERE id = ?
    """;
    
    private static final String SELECT_ALL = "SELECT * FROM societes ORDER BY nom_societe";
    private static final String SELECT_BY_ID = "SELECT * FROM societes WHERE id = ?";
    private static final String SELECT_BY_TYPE = "SELECT * FROM societes WHERE type_societe = ? ORDER BY nom_societe";
    private static final String SELECT_ACTIVE = "SELECT * FROM societes WHERE is_active = 1 ORDER BY nom_societe";
    private static final String DELETE_BY_ID = "DELETE FROM societes WHERE id = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM societes";
    
    private final Connection connection;
    
    public CompanyRepository(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
    }
    
    private void createTableIfNotExists() {
        // Table societes déjà créée dans DB.ensureSchema()
        AppLogger.info("CompanyRepository", "Table societes vérifiée (créée dans DB.ensureSchema)");
    }
    
    /**
     * Sauvegarde ou met à jour une société
     */
    public Company save(Company company) {
        // Vérification de la protection pour les mises à jour uniquement
        if (company.getId() != null) {
            try {
                CompanyProtectionManager.validateCompanyModification(company);
            } catch (CompanyProtectionManager.CompanyProtectionException e) {
                AppLogger.warn("Tentative de modification de société protégée: " + e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
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
        // Vérification de la protection avant suppression
        Optional<Company> company = findById(id);
        if (company.isPresent()) {
            try {
                CompanyProtectionManager.validateCompanyDeletion(company.get());
            } catch (CompanyProtectionManager.CompanyProtectionException e) {
                AppLogger.warn("Tentative de suppression de société protégée: " + e.getMessage());
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        
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
        company.setName(rs.getString("nom_societe"));
        company.setLegalName(rs.getString("raison_sociale"));
        
        String typeStr = rs.getString("type_societe");
        try {
            company.setType(Company.CompanyType.valueOf(typeStr));
        } catch (IllegalArgumentException e) {
            company.setType(Company.CompanyType.COMPANY);
        }
        
        company.setSiret(rs.getString("siret"));
        company.setAddress(rs.getString("adresse_societe"));
        company.setPostalCode(rs.getString("code_postal"));
        company.setCity(rs.getString("ville"));
        company.setCountry(rs.getString("pays"));
        company.setPhone(rs.getString("telephone_societe"));
        company.setEmail(rs.getString("email_societe"));
        company.setWebsite(rs.getString("site_web"));
        company.setDescription(rs.getString("description"));
        company.setLogoPath(rs.getString("logo_path"));
        company.setSector(rs.getString("secteur"));
        company.setActive(rs.getInt("is_active") == 1);
        
        String createdAtStr = rs.getString("date_creation");
        if (createdAtStr != null) {
            // Parse SQLite datetime format (yyyy-MM-dd HH:mm:ss)
            company.setCreatedAt(LocalDateTime.parse(createdAtStr.replace(" ", "T")));
        }
        
        String updatedAtStr = rs.getString("date_modification");
        if (updatedAtStr != null) {
            // Parse SQLite datetime format (yyyy-MM-dd HH:mm:ss)
            company.setUpdatedAt(LocalDateTime.parse(updatedAtStr.replace(" ", "T")));
        }
        
        return company;
    }
}