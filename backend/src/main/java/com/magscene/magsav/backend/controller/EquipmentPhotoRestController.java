package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.entity.EquipmentPhoto;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import com.magscene.magsav.backend.repository.EquipmentPhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/equipment-photos")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class EquipmentPhotoRestController {

    @Autowired
    private EquipmentPhotoRepository equipmentPhotoRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Value("${app.upload.dir:${user.home}/magsav/uploads}")
    private String uploadDir;

    // RÃƒÂ©cupÃƒÂ©rer toutes les photos
    @GetMapping
    public ResponseEntity<List<EquipmentPhoto>> getAllPhotos() {
        List<EquipmentPhoto> photos = equipmentPhotoRepository.findAll();
        return ResponseEntity.ok(photos);
    }

    // RÃƒÂ©cupÃƒÂ©rer une photo par son ID
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentPhoto> getPhotoById(@PathVariable Long id) {
        Optional<EquipmentPhoto> photo = equipmentPhotoRepository.findById(id);
        return photo.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    // RÃƒÂ©cupÃƒÂ©rer toutes les photos d'un ÃƒÂ©quipement
    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<List<EquipmentPhoto>> getPhotosByEquipment(@PathVariable Long equipmentId) {
        List<EquipmentPhoto> photos = equipmentPhotoRepository.findByEquipmentIdOrderByPriorityAndDate(equipmentId);
        return ResponseEntity.ok(photos);
    }

    // RÃƒÂ©cupÃƒÂ©rer la photo principale d'un ÃƒÂ©quipement
    @GetMapping("/equipment/{equipmentId}/primary")
    public ResponseEntity<EquipmentPhoto> getPrimaryPhoto(@PathVariable Long equipmentId) {
        Optional<EquipmentPhoto> primaryPhoto = equipmentPhotoRepository.findByEquipmentIdAndIsPrimaryTrue(equipmentId);
        return primaryPhoto.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    // RÃƒÂ©cupÃƒÂ©rer seulement les images d'un ÃƒÂ©quipement
    @GetMapping("/equipment/{equipmentId}/images")
    public ResponseEntity<List<EquipmentPhoto>> getImagesByEquipment(@PathVariable Long equipmentId) {
        List<EquipmentPhoto> images = equipmentPhotoRepository.findImagesByEquipmentId(equipmentId);
        return ResponseEntity.ok(images);
    }

    // RÃƒÂ©cupÃƒÂ©rer seulement les documents d'un ÃƒÂ©quipement
    @GetMapping("/equipment/{equipmentId}/documents")
    public ResponseEntity<List<EquipmentPhoto>> getDocumentsByEquipment(@PathVariable Long equipmentId) {
        List<EquipmentPhoto> documents = equipmentPhotoRepository.findDocumentsByEquipmentId(equipmentId);
        return ResponseEntity.ok(documents);
    }

    // Upload d'une nouvelle photo/document
    @PostMapping("/equipment/{equipmentId}/upload")
    public ResponseEntity<EquipmentPhoto> uploadPhoto(
            @PathVariable Long equipmentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPrimary", defaultValue = "false") boolean isPrimary) {

        // VÃƒÂ©rifier que l'ÃƒÂ©quipement existe
        Optional<Equipment> equipmentOpt = equipmentRepository.findById(equipmentId);
        if (!equipmentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Equipment equipment = equipmentOpt.get();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // CrÃƒÂ©er le rÃƒÂ©pertoire de destination si nÃƒÂ©cessaire
            Path uploadPath = Paths.get(uploadDir, "equipment", equipmentId.toString());
            Files.createDirectories(uploadPath);

            // GÃƒÂ©nÃƒÂ©rer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // Copier le fichier
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Si c'est une photo principale, supprimer l'ancienne photo principale
            if (isPrimary) {
                equipmentPhotoRepository.deleteByEquipmentIdAndIsPrimaryTrue(equipmentId);
            }

            // CrÃƒÂ©er l'entitÃƒÂ© EquipmentPhoto
            EquipmentPhoto photo = new EquipmentPhoto();
            photo.setEquipment(equipment);
            photo.setFileName(originalFilename);
            photo.setFilePath(uploadPath.toString());
            photo.setFileSize(file.getSize());
            photo.setMimeType(file.getContentType());
            photo.setDescription(description);
            photo.setIsPrimary(isPrimary);
            photo.setCreatedAt(LocalDateTime.now());

            EquipmentPhoto savedPhoto = equipmentPhotoRepository.save(photo);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPhoto);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // TÃƒÂ©lÃƒÂ©charger/afficher une photo
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Long id) {
        Optional<EquipmentPhoto> photoOpt = equipmentPhotoRepository.findById(id);
        if (!photoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        EquipmentPhoto photo = photoOpt.get();
        try {
            Path filePath = Paths.get(photo.getFilePath(), photo.getFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                MediaType mediaType = MediaType.parseMediaType(photo.getMimeType());
                
                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "inline; filename=\"" + photo.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mettre ÃƒÂ  jour les dÃƒÂ©tails d'une photo
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentPhoto> updatePhoto(@PathVariable Long id, @RequestBody EquipmentPhoto photoDetails) {
        Optional<EquipmentPhoto> photoOpt = equipmentPhotoRepository.findById(id);
        if (!photoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        EquipmentPhoto photo = photoOpt.get();
        
        // Si on dÃƒÂ©finit cette photo comme principale, supprimer l'ancienne
        if (photoDetails.getIsPrimary() && !photo.getIsPrimary()) {
            equipmentPhotoRepository.deleteByEquipmentIdAndIsPrimaryTrue(photo.getEquipment().getId());
        }

        photo.setDescription(photoDetails.getDescription());
        photo.setIsPrimary(photoDetails.getIsPrimary());
        photo.setUpdatedAt(LocalDateTime.now());

        EquipmentPhoto updatedPhoto = equipmentPhotoRepository.save(photo);
        return ResponseEntity.ok(updatedPhoto);
    }

    // DÃƒÂ©finir une photo comme principale
    @PutMapping("/{id}/set-primary")
    public ResponseEntity<EquipmentPhoto> setPrimaryPhoto(@PathVariable Long id) {
        Optional<EquipmentPhoto> photoOpt = equipmentPhotoRepository.findById(id);
        if (!photoOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        EquipmentPhoto photo = photoOpt.get();
        
        // Supprimer l'ancienne photo principale
        equipmentPhotoRepository.deleteByEquipmentIdAndIsPrimaryTrue(photo.getEquipment().getId());
        
        photo.setIsPrimary(true);
        photo.setUpdatedAt(LocalDateTime.now());
        
        EquipmentPhoto updatedPhoto = equipmentPhotoRepository.save(photo);
        return ResponseEntity.ok(updatedPhoto);
    }

    // Supprimer une photo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        Optional<EquipmentPhoto> photo = equipmentPhotoRepository.findById(id);
        if (!photo.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Supprimer le fichier physique
            EquipmentPhoto photoEntity = photo.get();
            Path filePath = Paths.get(photoEntity.getFilePath(), photoEntity.getFileName());
            Files.deleteIfExists(filePath);
            
            // Supprimer l'enregistrement de la base
            equipmentPhotoRepository.deleteById(id);
            
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Rechercher des photos
    @GetMapping("/search")
    public ResponseEntity<List<EquipmentPhoto>> searchPhotos(@RequestParam String q) {
        List<EquipmentPhoto> searchResults = equipmentPhotoRepository.searchPhotos(q);
        return ResponseEntity.ok(searchResults);
    }

    // Statistiques des photos
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPhotoStatistics() {
        Object[] stats = equipmentPhotoRepository.getPhotoStatistics();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPhotos", stats[0]);
        statistics.put("totalSize", stats[1]);
        statistics.put("averageSize", stats[2]);
        statistics.put("maxSize", stats[3]);
        
        // Statistiques par type MIME
        List<Object[]> mimeStats = equipmentPhotoRepository.getMimeTypeStatistics();
        statistics.put("mimeTypeStats", mimeStats);
        
        // Ãƒâ€°quipements avec le plus de photos
        List<Object[]> equipmentStats = equipmentPhotoRepository.getEquipmentWithMostPhotos();
        statistics.put("equipmentWithMostPhotos", equipmentStats);
        
        return ResponseEntity.ok(statistics);
    }

    // Nettoyer les photos orphelines
    @DeleteMapping("/cleanup-orphaned")
    public ResponseEntity<Map<String, Object>> cleanupOrphanedPhotos() {
        List<EquipmentPhoto> orphanedPhotos = equipmentPhotoRepository.findOrphanedPhotos();
        int deletedCount = 0;
        
        for (EquipmentPhoto photo : orphanedPhotos) {
            try {
                // Supprimer le fichier physique
                Path filePath = Paths.get(photo.getFilePath(), photo.getFileName());
                Files.deleteIfExists(filePath);
                
                // Supprimer l'enregistrement
                equipmentPhotoRepository.delete(photo);
                deletedCount++;
            } catch (IOException e) {
                // Continuer mÃƒÂªme si la suppression d'un fichier ÃƒÂ©choue
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("orphanedFound", orphanedPhotos.size());
        result.put("deleted", deletedCount);
        
        return ResponseEntity.ok(result);
    }

    // Compter les photos d'un ÃƒÂ©quipement
    @GetMapping("/equipment/{equipmentId}/count")
    public ResponseEntity<Map<String, Object>> getEquipmentPhotoCount(@PathVariable Long equipmentId) {
        Long totalCount = equipmentPhotoRepository.countByEquipmentId(equipmentId);
        Long imageCount = equipmentPhotoRepository.countImagesByEquipmentId(equipmentId);
        boolean hasPrimary = equipmentPhotoRepository.existsByEquipmentIdAndIsPrimaryTrue(equipmentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalPhotos", totalCount);
        result.put("totalImages", imageCount);
        result.put("totalDocuments", totalCount - imageCount);
        result.put("hasPrimaryPhoto", hasPrimary);
        
        return ResponseEntity.ok(result);
    }
}

