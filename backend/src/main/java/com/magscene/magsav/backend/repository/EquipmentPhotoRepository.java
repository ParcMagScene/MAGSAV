package com.magscene.magsav.backend.repository;

import com.magscene.magsav.backend.entity.EquipmentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EquipmentPhotoRepository extends JpaRepository<EquipmentPhoto, Long> {
    
    // RÃƒÂ©cupÃƒÂ©rer toutes les photos d'un ÃƒÂ©quipement
    List<EquipmentPhoto> findByEquipmentIdOrderByCreatedAtAsc(Long equipmentId);
    
    // RÃƒÂ©cupÃƒÂ©rer toutes les photos d'un ÃƒÂ©quipement triÃƒÂ©es par ordre de prioritÃƒÂ©
    @Query("SELECT ep FROM EquipmentPhoto ep WHERE ep.equipment.id = :equipmentId " +
           "ORDER BY ep.isPrimary DESC, ep.createdAt ASC")
    List<EquipmentPhoto> findByEquipmentIdOrderByPriorityAndDate(@Param("equipmentId") Long equipmentId);
    
    // RÃƒÂ©cupÃƒÂ©rer la photo principale d'un ÃƒÂ©quipement
    Optional<EquipmentPhoto> findByEquipmentIdAndIsPrimaryTrue(Long equipmentId);
    
    // RÃƒÂ©cupÃƒÂ©rer les photos d'un ÃƒÂ©quipement par type MIME
    List<EquipmentPhoto> findByEquipmentIdAndMimeTypeStartingWithOrderByCreatedAtAsc(Long equipmentId, String mimeTypePrefix);
    
    // RÃƒÂ©cupÃƒÂ©rer toutes les images d'un ÃƒÂ©quipement
    @Query("SELECT ep FROM EquipmentPhoto ep WHERE ep.equipment.id = :equipmentId " +
           "AND ep.mimeType LIKE 'image/%' ORDER BY ep.isPrimary DESC, ep.createdAt ASC")
    List<EquipmentPhoto> findImagesByEquipmentId(@Param("equipmentId") Long equipmentId);
    
    // RÃƒÂ©cupÃƒÂ©rer tous les documents non-images d'un ÃƒÂ©quipement
    @Query("SELECT ep FROM EquipmentPhoto ep WHERE ep.equipment.id = :equipmentId " +
           "AND ep.mimeType NOT LIKE 'image/%' ORDER BY ep.createdAt ASC")
    List<EquipmentPhoto> findDocumentsByEquipmentId(@Param("equipmentId") Long equipmentId);
    
    // Compter les photos d'un ÃƒÂ©quipement
    Long countByEquipmentId(Long equipmentId);
    
    // Compter les images d'un ÃƒÂ©quipement
    @Query("SELECT COUNT(ep) FROM EquipmentPhoto ep WHERE ep.equipment.id = :equipmentId " +
           "AND ep.mimeType LIKE 'image/%'")
    Long countImagesByEquipmentId(@Param("equipmentId") Long equipmentId);
    
    // VÃƒÂ©rifier si un ÃƒÂ©quipement a une photo principale
    boolean existsByEquipmentIdAndIsPrimaryTrue(Long equipmentId);
    
    // RÃƒÂ©cupÃƒÂ©rer toutes les photos principales
    List<EquipmentPhoto> findByIsPrimaryTrue();
    
    // Recherche par nom de fichier
    List<EquipmentPhoto> findByFileNameContainingIgnoreCaseOrderByCreatedAtDesc(String fileName);
    
    // Recherche par description
    List<EquipmentPhoto> findByDescriptionContainingIgnoreCaseOrderByCreatedAtDesc(String description);
    
    // RÃƒÂ©cupÃƒÂ©rer les photos par taille de fichier (supÃƒÂ©rieure ÃƒÂ  une valeur)
    List<EquipmentPhoto> findByFileSizeGreaterThanOrderByFileSizeDesc(Long minSize);
    
    // RÃƒÂ©cupÃƒÂ©rer les photos par taille de fichier (infÃƒÂ©rieure ÃƒÂ  une valeur)
    List<EquipmentPhoto> findByFileSizeLessThanOrderByFileSizeAsc(Long maxSize);
    
    // RÃƒÂ©cupÃƒÂ©rer les photos dans une plage de tailles
    List<EquipmentPhoto> findByFileSizeBetweenOrderByFileSizeAsc(Long minSize, Long maxSize);
    
    // Statistiques sur les photos
    @Query("SELECT COUNT(ep), SUM(ep.fileSize), AVG(ep.fileSize), MAX(ep.fileSize) FROM EquipmentPhoto ep")
    Object[] getPhotoStatistics();
    
    // Statistiques sur les photos par ÃƒÂ©quipement
    @Query("SELECT e.name, COUNT(ep), SUM(ep.fileSize) FROM EquipmentPhoto ep " +
           "JOIN ep.equipment e GROUP BY e.id, e.name ORDER BY COUNT(ep) DESC")
    List<Object[]> getPhotoStatisticsByEquipment();
    
    // RÃƒÂ©cupÃƒÂ©rer les types MIME utilisÃƒÂ©s
    @Query("SELECT DISTINCT ep.mimeType, COUNT(ep) FROM EquipmentPhoto ep " +
           "GROUP BY ep.mimeType ORDER BY COUNT(ep) DESC")
    List<Object[]> getMimeTypeStatistics();
    
    // RÃƒÂ©cupÃƒÂ©rer les ÃƒÂ©quipements sans photos
    @Query("SELECT e FROM Equipment e WHERE e.id NOT IN " +
           "(SELECT DISTINCT ep.equipment.id FROM EquipmentPhoto ep)")
    List<Object> getEquipmentWithoutPhotos();
    
    // RÃƒÂ©cupÃƒÂ©rer les ÃƒÂ©quipements avec le plus de photos
    @Query("SELECT e.name, COUNT(ep) FROM EquipmentPhoto ep " +
           "JOIN ep.equipment e GROUP BY e.id, e.name " +
           "ORDER BY COUNT(ep) DESC")
    List<Object[]> getEquipmentWithMostPhotos();
    
    // VÃƒÂ©rifier si un fichier existe dÃƒÂ©jÃƒÂ  (pour ÃƒÂ©viter les doublons)
    boolean existsByFilePathAndFileName(String filePath, String fileName);
    
    // RÃƒÂ©cupÃƒÂ©rer une photo par son chemin complet
    Optional<EquipmentPhoto> findByFilePathAndFileName(String filePath, String fileName);
    
    // Supprimer toutes les photos d'un ÃƒÂ©quipement
    void deleteByEquipmentId(Long equipmentId);
    
    // Supprimer les photos principales d'un ÃƒÂ©quipment (pour en dÃƒÂ©finir une nouvelle)
    void deleteByEquipmentIdAndIsPrimaryTrue(Long equipmentId);
    
    // RÃƒÂ©cupÃƒÂ©rer les photos orphelines (ÃƒÂ©quipement supprimÃƒÂ©)
    @Query("SELECT ep FROM EquipmentPhoto ep WHERE ep.equipment IS NULL")
    List<EquipmentPhoto> findOrphanedPhotos();
    
    // Recherche globale dans les photos (nom de fichier + description)
    @Query("SELECT ep FROM EquipmentPhoto ep WHERE " +
           "LOWER(ep.fileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(ep.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY ep.createdAt DESC")
    List<EquipmentPhoto> searchPhotos(@Param("searchTerm") String searchTerm);
}

