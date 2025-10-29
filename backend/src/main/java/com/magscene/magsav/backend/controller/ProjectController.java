package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Project;
import com.magscene.magsav.backend.entity.Project.ProjectStatus;
import com.magscene.magsav.backend.entity.Project.ProjectType;
import com.magscene.magsav.backend.repository.ProjectRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

/**
 * ContrÃƒÂ´leur REST pour la gestion des projets
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * RÃƒÂ©cupÃƒÂ¨re tous les projets
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        try {
            List<Project> projects = projectRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
            logger.info("RÃƒÂ©cupÃƒÂ©ration de {} projets", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des projets: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re un projet par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        try {
            Optional<Project> project = projectRepository.findById(id);
            if (project.isPresent()) {
                logger.info("Projet trouvÃƒÂ©: {}", project.get().getName());
                return ResponseEntity.ok(project.get());
            } else {
                logger.warn("Projet non trouvÃƒÂ© avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration du projet {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CrÃƒÂ©e un nouveau projet
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        try {
            // GÃƒÂ©nÃƒÂ©ration automatique du numÃƒÂ©ro de projet si non fourni
            if (project.getProjectNumber() == null || project.getProjectNumber().isEmpty()) {
                project.setProjectNumber(generateProjectNumber());
            }
            
            Project savedProject = projectRepository.save(project);
            logger.info("Nouveau projet crÃƒÂ©ÃƒÂ©: {} (ID: {})", savedProject.getName(), savedProject.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProject);
        } catch (Exception e) {
            logger.error("Erreur lors de la crÃƒÂ©ation du projet: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met ÃƒÂ  jour un projet
     */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project projectDetails) {
        try {
            Optional<Project> projectOptional = projectRepository.findById(id);
            if (projectOptional.isPresent()) {
                Project project = projectOptional.get();
                
                // Mise ÃƒÂ  jour des champs
                project.setName(projectDetails.getName());
                project.setProjectNumber(projectDetails.getProjectNumber());
                project.setType(projectDetails.getType());
                project.setStatus(projectDetails.getStatus());
                project.setPriority(projectDetails.getPriority());
                project.setDescription(projectDetails.getDescription());
                project.setClientName(projectDetails.getClientName());
                project.setClientContact(projectDetails.getClientContact());
                project.setClientEmail(projectDetails.getClientEmail());
                project.setClientPhone(projectDetails.getClientPhone());
                project.setClientAddress(projectDetails.getClientAddress());
                project.setStartDate(projectDetails.getStartDate());
                project.setEndDate(projectDetails.getEndDate());
                project.setInstallationDate(projectDetails.getInstallationDate());
                project.setDeliveryDate(projectDetails.getDeliveryDate());
                project.setEstimatedAmount(projectDetails.getEstimatedAmount());
                project.setFinalAmount(projectDetails.getFinalAmount());
                project.setDepositAmount(projectDetails.getDepositAmount());
                project.setRemainingAmount(projectDetails.getRemainingAmount());
                project.setVenue(projectDetails.getVenue());
                project.setVenueAddress(projectDetails.getVenueAddress());
                project.setVenueContact(projectDetails.getVenueContact());
                project.setProjectManager(projectDetails.getProjectManager());
                project.setTechnicalManager(projectDetails.getTechnicalManager());
                project.setSalesRepresentative(projectDetails.getSalesRepresentative());
                project.setNotes(projectDetails.getNotes());
                project.setTechnicalNotes(projectDetails.getTechnicalNotes());
                project.setClientRequirements(projectDetails.getClientRequirements());

                Project updatedProject = projectRepository.save(project);
                logger.info("Projet mis ÃƒÂ  jour: {} (ID: {})", updatedProject.getName(), updatedProject.getId());
                return ResponseEntity.ok(updatedProject);
            } else {
                logger.warn("Projet non trouvÃƒÂ© pour mise ÃƒÂ  jour avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour du projet {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un projet
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            if (projectRepository.existsById(id)) {
                projectRepository.deleteById(id);
                logger.info("Projet supprimÃƒÂ© avec l'ID: {}", id);
                return ResponseEntity.noContent().build();
            } else {
                logger.warn("Projet non trouvÃƒÂ© pour suppression avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du projet {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des projets
     */
    @GetMapping("/search")
    public ResponseEntity<List<Project>> searchProjects(@RequestParam String q) {
        try {
            List<Project> projects = projectRepository.searchProjects(q);
            logger.info("Recherche '{}': {} projets trouvÃƒÂ©s", q, projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de projets: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les projets par statut
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Project>> getProjectsByStatus(@PathVariable ProjectStatus status) {
        try {
            List<Project> projects = projectRepository.findByStatus(status);
            logger.info("Projets avec statut {}: {} trouvÃƒÂ©s", status, projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des projets par statut: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les projets par type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Project>> getProjectsByType(@PathVariable ProjectType type) {
        try {
            List<Project> projects = projectRepository.findByType(type);
            logger.info("Projets de type {}: {} trouvÃƒÂ©s", type, projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des projets par type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les projets actifs
     */
    @GetMapping("/active")
    public ResponseEntity<List<Project>> getActiveProjects() {
        try {
            List<Project> projects = projectRepository.findActiveProjects();
            logger.info("Projets actifs: {} trouvÃƒÂ©s", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des projets actifs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les projets nÃƒÂ©cessitant une attention
     */
    @GetMapping("/attention")
    public ResponseEntity<List<Project>> getProjectsNeedingAttention() {
        try {
            List<Project> projects = projectRepository.findProjectsNeedingAttention();
            logger.info("Projets nÃƒÂ©cessitant attention: {} trouvÃƒÂ©s", projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des projets nÃƒÂ©cessitant attention: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les statistiques des projets
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProjectStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Comptage total
            long totalProjects = projectRepository.count();
            stats.put("total", totalProjects);
            
            // Comptage par statut
            List<Object[]> statusCounts = projectRepository.countProjectsByStatus();
            Map<String, Long> byStatus = new HashMap<>();
            for (Object[] row : statusCounts) {
                byStatus.put(row[0].toString(), (Long) row[1]);
            }
            stats.put("byStatus", byStatus);
            
            // Comptage par type
            List<Object[]> typeCounts = projectRepository.countProjectsByType();
            Map<String, Long> byType = new HashMap<>();
            for (Object[] row : typeCounts) {
                byType.put(row[0].toString(), (Long) row[1]);
            }
            stats.put("byType", byType);
            
            // Projets actifs
            long activeProjects = projectRepository.findActiveProjects().size();
            stats.put("active", activeProjects);
            
            // Projets nÃƒÂ©cessitant attention
            long attentionProjects = projectRepository.findProjectsNeedingAttention().size();
            stats.put("needingAttention", attentionProjects);
            
            logger.info("Statistiques projets gÃƒÂ©nÃƒÂ©rÃƒÂ©es: {} projets au total", totalProjects);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Erreur lors de la gÃƒÂ©nÃƒÂ©ration des statistiques projets: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * RÃƒÂ©cupÃƒÂ¨re les projets par pÃƒÂ©riode d'installation
     */
    @GetMapping("/installation-period")
    public ResponseEntity<List<Project>> getProjectsByInstallationPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<Project> projects = projectRepository.findByInstallationDateBetween(startDate, endDate);
            logger.info("Projets avec installation entre {} et {}: {} trouvÃƒÂ©s", startDate, endDate, projects.size());
            return ResponseEntity.ok(projects);
        } catch (Exception e) {
            logger.error("Erreur lors de la rÃƒÂ©cupÃƒÂ©ration des projets par pÃƒÂ©riode d'installation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met ÃƒÂ  jour le statut d'un projet
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Project> updateProjectStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            Optional<Project> projectOptional = projectRepository.findById(id);
            if (projectOptional.isPresent()) {
                Project project = projectOptional.get();
                ProjectStatus newStatus = ProjectStatus.valueOf(request.get("status"));
                project.setStatus(newStatus);
                
                Project updatedProject = projectRepository.save(project);
                logger.info("Statut du projet {} mis ÃƒÂ  jour: {}", id, newStatus);
                return ResponseEntity.ok(updatedProject);
            } else {
                logger.warn("Projet non trouvÃƒÂ© pour mise ÃƒÂ  jour statut avec l'ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise ÃƒÂ  jour du statut du projet {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GÃƒÂ©nÃƒÂ¨re un numÃƒÂ©ro de projet unique
     */
    private String generateProjectNumber() {
        String year = String.valueOf(LocalDate.now().getYear());
        long count = projectRepository.count() + 1;
        return "PRJ-" + year + "-" + String.format("%04d", count);
    }
}

