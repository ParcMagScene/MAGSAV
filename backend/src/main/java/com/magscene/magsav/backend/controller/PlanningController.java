package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.dto.PlanningEventDTO;
import com.magscene.magsav.backend.entity.Personnel;
import com.magscene.magsav.backend.entity.Vehicle;
import com.magscene.magsav.backend.repository.PersonnelRepository;
import com.magscene.magsav.backend.repository.VehicleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller REST pour la gestion du planning global
 * Vue unifiée du planning personnel + véhicules avec détection de conflits
 */
@RestController
@RequestMapping("/api/planning")
@Tag(name = "Planning", description = "Gestion du planning global (personnel + véhicules)")
@CrossOrigin(origins = "*")
public class PlanningController {

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Obtenir la vue complète du planning pour une période donnée
     */
    @GetMapping
    @Operation(summary = "Vue planning complète", description = "Retourne tous les événements (personnel + véhicules) pour la période")
    public ResponseEntity<List<PlanningEventDTO>> getCompleteSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String resourceType) {

        List<PlanningEventDTO> events = new ArrayList<>();

        // Événements personnel (si demandé)
        if (resourceType == null || "PERSONNEL".equals(resourceType)) {
            events.addAll(getPersonnelEvents(startDate, endDate));
        }

        // Événements véhicules (si demandé)
        if (resourceType == null || "VEHICLE".equals(resourceType)) {
            events.addAll(getVehicleEvents(startDate, endDate));
        }

        // Tri par date de début
        events.sort(Comparator.comparing(PlanningEventDTO::getStartDate));

        return ResponseEntity.ok(events);
    }

    /**
     * Vérifier les disponibilités pour une période
     */
    @GetMapping("/availability")
    @Operation(summary = "Vérifier disponibilités", description = "Liste des ressources disponibles (personnel + véhicules) pour la période")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> availability = new HashMap<>();

        // Personnel disponible
        List<Personnel> allPersonnel = personnelRepository.findByStatus(Personnel.PersonnelStatus.ACTIVE);
        List<Map<String, Object>> availablePersonnel = allPersonnel.stream()
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("name", p.getFirstName() + " " + p.getLastName());
                    map.put("type", "PERSONNEL");
                    map.put("department", p.getDepartment() != null ? p.getDepartment() : "");
                    map.put("jobTitle", p.getJobTitle() != null ? p.getJobTitle() : "");
                    return map;
                })
                .collect(Collectors.toList());

        // Véhicules disponibles
        List<Vehicle> allVehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);
        List<Map<String, Object>> availableVehicles = allVehicles.stream()
                .map(v -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", v.getId());
                    map.put("name", v.getLicensePlate() + " - " + v.getType().getDisplayName());
                    map.put("type", "VEHICLE");
                    map.put("vehicleType", v.getType().name());
                    map.put("capacity", v.getMaxPayload() != null ? v.getMaxPayload().toString() : "N/A");
                    return map;
                })
                .collect(Collectors.toList());

        availability.put("personnel", availablePersonnel);
        availability.put("vehicles", availableVehicles);
        availability.put("period", Map.of("start", startDate, "end", endDate));
        availability.put("totalPersonnel", availablePersonnel.size());
        availability.put("totalVehicles", availableVehicles.size());

        return ResponseEntity.ok(availability);
    }

    /**
     * Détecter les conflits dans le planning
     */
    @GetMapping("/conflicts")
    @Operation(summary = "Détecter conflits planning", description = "Identifie les conflits de réservation (double affectation, chevauchements)")
    public ResponseEntity<List<Map<String, Object>>> detectConflicts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Map<String, Object>> conflicts = new ArrayList<>();

        // Récupérer tous les événements
        List<PlanningEventDTO> events = new ArrayList<>();
        events.addAll(getPersonnelEvents(startDate, endDate));
        events.addAll(getVehicleEvents(startDate, endDate));

        // Grouper par ressource
        Map<String, List<PlanningEventDTO>> eventsByResource = events.stream()
                .collect(Collectors.groupingBy(e -> e.getResourceType() + "_" + e.getResourceId()));

        // Détecter chevauchements
        for (Map.Entry<String, List<PlanningEventDTO>> entry : eventsByResource.entrySet()) {
            List<PlanningEventDTO> resourceEvents = entry.getValue();
            if (resourceEvents.size() > 1) {
                resourceEvents.sort(Comparator.comparing(PlanningEventDTO::getStartDate));

                for (int i = 0; i < resourceEvents.size() - 1; i++) {
                    PlanningEventDTO current = resourceEvents.get(i);
                    PlanningEventDTO next = resourceEvents.get(i + 1);

                    if (current.getEndDate().isAfter(next.getStartDate())) {
                        Map<String, Object> conflict = new HashMap<>();
                        conflict.put("resourceType", current.getResourceType());
                        conflict.put("resourceId", current.getResourceId());
                        conflict.put("resourceName", current.getResourceName());

                        Map<String, Object> event1 = new HashMap<>();
                        event1.put("title", current.getTitle());
                        event1.put("start", current.getStartDate());
                        event1.put("end", current.getEndDate());
                        conflict.put("event1", event1);

                        Map<String, Object> event2 = new HashMap<>();
                        event2.put("title", next.getTitle());
                        event2.put("start", next.getStartDate());
                        event2.put("end", next.getEndDate());
                        conflict.put("event2", event2);

                        conflict.put("overlapMinutes",
                                java.time.Duration.between(next.getStartDate(), current.getEndDate()).toMinutes());
                        conflicts.add(conflict);
                    }
                }
            }
        }

        return ResponseEntity.ok(conflicts);
    }

    /**
     * Vue planning pour une ressource spécifique
     */
    @GetMapping("/{resourceType}/{resourceId}")
    @Operation(summary = "Planning d'une ressource", description = "Planning complet pour une ressource (PERSONNEL ou VEHICLE)")
    public ResponseEntity<Map<String, Object>> getResourceSchedule(
            @PathVariable String resourceType,
            @PathVariable Long resourceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> schedule = new HashMap<>();

        if ("PERSONNEL".equalsIgnoreCase(resourceType)) {
            Optional<Personnel> personnel = personnelRepository.findById(resourceId);
            if (personnel.isPresent()) {
                Personnel p = personnel.get();
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", p.getId());
                resource.put("name", p.getFirstName() + " " + p.getLastName());
                resource.put("type", "PERSONNEL");
                resource.put("department", p.getDepartment() != null ? p.getDepartment() : "");
                resource.put("jobTitle", p.getJobTitle() != null ? p.getJobTitle() : "");
                resource.put("status", p.getStatus().name());
                schedule.put("resource", resource);
                schedule.put("events", getPersonnelEvents(startDate, endDate).stream()
                        .filter(e -> e.getResourceId().equals(resourceId))
                        .collect(Collectors.toList()));
            }
        } else if ("VEHICLE".equalsIgnoreCase(resourceType)) {
            Optional<Vehicle> vehicle = vehicleRepository.findById(resourceId);
            if (vehicle.isPresent()) {
                Vehicle v = vehicle.get();
                Map<String, Object> resource = new HashMap<>();
                resource.put("id", v.getId());
                resource.put("name", v.getLicensePlate() + " - " + v.getType().getDisplayName());
                resource.put("type", "VEHICLE");
                resource.put("vehicleType", v.getType().name());
                resource.put("status", v.getStatus().name());
                schedule.put("resource", resource);
                schedule.put("events", getVehicleEvents(startDate, endDate).stream()
                        .filter(e -> e.getResourceId().equals(resourceId))
                        .collect(Collectors.toList()));
            }
        }

        schedule.put("period", Map.of("start", startDate, "end", endDate));

        return ResponseEntity.ok(schedule);
    }

    /**
     * Statistiques du planning
     */
    @GetMapping("/statistics")
    @Operation(summary = "Statistiques planning", description = "Indicateurs : taux d'utilisation, conflits, disponibilités")
    public ResponseEntity<Map<String, Object>> getPlanningStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> stats = new HashMap<>();

        // Compter les ressources
        long totalPersonnel = personnelRepository.countByStatus(Personnel.PersonnelStatus.ACTIVE);
        long totalVehicles = vehicleRepository.countByStatus(Vehicle.VehicleStatus.AVAILABLE);

        // Événements dans la période
        List<PlanningEventDTO> allEvents = new ArrayList<>();
        allEvents.addAll(getPersonnelEvents(startDate, endDate));
        allEvents.addAll(getVehicleEvents(startDate, endDate));

        long personnelEvents = allEvents.stream()
                .filter(e -> "PERSONNEL".equals(e.getResourceType()))
                .count();
        long vehicleEvents = allEvents.stream()
                .filter(e -> "VEHICLE".equals(e.getResourceType()))
                .count();

        // Calcul taux d'utilisation (simplifié)
        double personnelUtilization = totalPersonnel > 0 ? (personnelEvents * 100.0 / totalPersonnel) : 0;
        double vehicleUtilization = totalVehicles > 0 ? (vehicleEvents * 100.0 / totalVehicles) : 0;

        stats.put("totalPersonnel", totalPersonnel);
        stats.put("totalVehicles", totalVehicles);
        stats.put("personnelEvents", personnelEvents);
        stats.put("vehicleEvents", vehicleEvents);
        stats.put("personnelUtilization", Math.round(personnelUtilization * 10) / 10.0);
        stats.put("vehicleUtilization", Math.round(vehicleUtilization * 10) / 10.0);
        stats.put("totalEvents", allEvents.size());
        stats.put("period", Map.of("start", startDate, "end", endDate));

        return ResponseEntity.ok(stats);
    }

    /**
     * Rechercher des créneaux disponibles
     */
    @GetMapping("/find-slot")
    @Operation(summary = "Trouver créneaux libres", description = "Cherche des créneaux où toutes les ressources demandées sont disponibles")
    public ResponseEntity<List<Map<String, Object>>> findAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Integer personnelCount,
            @RequestParam(required = false) Integer vehicleCount,
            @RequestParam(required = false) Integer durationHours) {

        List<Map<String, Object>> slots = new ArrayList<>();

        // Récupérer disponibilités
        List<Personnel> availablePersonnel = personnelRepository.findByStatus(Personnel.PersonnelStatus.ACTIVE);
        List<Vehicle> availableVehicles = vehicleRepository.findByStatus(Vehicle.VehicleStatus.AVAILABLE);

        boolean personnelOk = personnelCount == null || availablePersonnel.size() >= personnelCount;
        boolean vehiclesOk = vehicleCount == null || availableVehicles.size() >= vehicleCount;

        if (personnelOk && vehiclesOk) {
            slots.add(Map.of(
                    "startDate", startDate,
                    "endDate", endDate,
                    "availablePersonnel", availablePersonnel.size(),
                    "availableVehicles", availableVehicles.size(),
                    "suitable", true));
        }

        return ResponseEntity.ok(slots);
    }

    // --- Méthodes utilitaires privées ---

    private List<PlanningEventDTO> getPersonnelEvents(LocalDateTime startDate, LocalDateTime endDate) {
        List<PlanningEventDTO> events = new ArrayList<>();

        // Récupérer le personnel actif
        List<Personnel> activePersonnel = personnelRepository.findByStatus(Personnel.PersonnelStatus.ACTIVE);

        // Pour chaque personnel, créer un événement exemple
        // En production, récupérer depuis une table d'affectations/planning
        for (Personnel p : activePersonnel) {
            PlanningEventDTO event = new PlanningEventDTO(
                    PlanningEventDTO.EventType.PERSONNEL_ASSIGNMENT,
                    p.getId(),
                    p.getFirstName() + " " + p.getLastName(),
                    "PERSONNEL",
                    startDate,
                    endDate,
                    "Affectation: " + (p.getJobTitle() != null ? p.getJobTitle() : "Personnel"));
            // Générer un ID unique basé sur le type et l'ID de ressource
            event.setId(Long.valueOf("1" + String.format("%06d", p.getId())));
            event.setColor("#4CAF50");
            event.setDescription("Personnel: " + p.getFirstName() + " " + p.getLastName());
            events.add(event);
        }

        return events;
    }

    private List<PlanningEventDTO> getVehicleEvents(LocalDateTime startDate, LocalDateTime endDate) {
        List<PlanningEventDTO> events = new ArrayList<>();

        // Récupérer les véhicules (tous statuts sauf AVAILABLE pour planning)
        List<Vehicle> vehicles = vehicleRepository.findAll();

        for (Vehicle v : vehicles) {
            if (v.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
                PlanningEventDTO.EventType eventType;
                String color;

                switch (v.getStatus()) {
                    case MAINTENANCE:
                        eventType = PlanningEventDTO.EventType.VEHICLE_MAINTENANCE;
                        color = "#FF9800";
                        break;
                    case RENTED_OUT:
                        eventType = PlanningEventDTO.EventType.VEHICLE_RENTAL;
                        color = "#9C27B0";
                        break;
                    case RESERVED:
                    case IN_USE:
                        eventType = PlanningEventDTO.EventType.VEHICLE_RESERVATION;
                        color = "#2196F3";
                        break;
                    default:
                        continue;
                }

                PlanningEventDTO event = new PlanningEventDTO(
                        eventType,
                        v.getId(),
                        v.getLicensePlate() + " - " + v.getType().getDisplayName(),
                        "VEHICLE",
                        startDate,
                        endDate,
                        v.getStatus().getDisplayName());
                // Générer un ID unique basé sur le type et l'ID de ressource (préfixe 2 pour
                // véhicules)
                event.setId(Long.valueOf("2" + String.format("%06d", v.getId())));
                event.setColor(color);
                event.setDescription("Véhicule: " + v.getLicensePlate());
                events.add(event);
            }
        }

        return events;
    }
}
