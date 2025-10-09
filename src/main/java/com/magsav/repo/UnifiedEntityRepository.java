package com.magsav.repo;

import com.magsav.model.*;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UnifiedEntityRepository {
    
    private final SimpleEntityRepository entityRepo;
    
    public UnifiedEntityRepository() {
        this.entityRepo = new SimpleEntityRepository();
    }
    
    // === ENTITÉS ===
    
    public long createEntity(Entity entity) throws SQLException {
        return entityRepo.create(entity);
    }
    
    public void updateEntity(Entity entity) throws SQLException {
        entityRepo.update(entity);
    }
    
    public Optional<Entity> findEntityById(long id) throws SQLException {
        return entityRepo.findById(id);
    }
    
    public List<Entity> findEntitiesByType(EntityType type) throws SQLException {
        return entityRepo.findByType(type);
    }
    
    public List<Entity> findAllEntities() throws SQLException {
        return entityRepo.findAll();
    }
    
    public List<Entity> searchEntitiesByName(String searchTerm) throws SQLException {
        return entityRepo.searchByName(searchTerm);
    }
    
    public void deleteEntity(long id) throws SQLException {
        entityRepo.delete(id);
    }
}
