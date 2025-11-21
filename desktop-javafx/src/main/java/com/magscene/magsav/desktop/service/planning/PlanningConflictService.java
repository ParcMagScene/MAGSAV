package com.magscene.magsav.desktop.service.planning;

import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService.PersonnelAssignment;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de détection et gestion des conflits dans le planning
 */
public class PlanningConflictService {
    
    private final List<PlanningEvent> events;
    private final SpecialtyPlanningService specialtyService;
    
    public PlanningConflictService(SpecialtyPlanningService specialtyService) {
        this.events = new ArrayList<>();
        this.specialtyService = specialtyService;
    }
    
    /**
     * Ajoute un événement au planning
     */
    public void addEvent(PlanningEvent event) {
        events.add(event);
    }
    
    /**
     * Supprime un événement du planning
     */
    public void removeEvent(PlanningEvent event) {
        events.remove(event);
    }
    
    /**
     * Détecte tous les conflits dans le planning
     */
    public List<ConflictInfo> detectAllConflicts() {
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        for (int i = 0; i < events.size(); i++) {
            for (int j = i + 1; j < events.size(); j++) {
                PlanningEvent event1 = events.get(i);
                PlanningEvent event2 = events.get(j);
                
                ConflictInfo conflict = detectConflict(event1, event2);
                if (conflict != null) {
                    conflicts.add(conflict);
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Détecte les conflits pour un nouvel événement
     */
    public List<ConflictInfo> detectConflictsForNewEvent(PlanningEvent newEvent) {
        List<ConflictInfo> conflicts = new ArrayList<>();
        
        for (PlanningEvent existingEvent : events) {
            ConflictInfo conflict = detectConflict(newEvent, existingEvent);
            if (conflict != null) {
                conflicts.add(conflict);
            }
        }
        
        return conflicts;
    }
    
    /**
     * Détecte un conflit entre deux événements
     */
    private ConflictInfo detectConflict(PlanningEvent event1, PlanningEvent event2) {
        // Vérification du chevauchement temporel
        if (!hasTimeOverlap(event1, event2)) {
            return null;
        }
        
        // Types de conflits
        List<ConflictType> conflictTypes = new ArrayList<>();
        List<String> conflictingPersonnel = new ArrayList<>();
        
        // Conflit de personnel
        for (PersonnelAssignment person1 : event1.getAssignedPersonnel()) {
            for (PersonnelAssignment person2 : event2.getAssignedPersonnel()) {
                if (person1.getPersonnelName().equals(person2.getPersonnelName())) {
                    conflictTypes.add(ConflictType.PERSONNEL_DOUBLE_BOOKING);
                    conflictingPersonnel.add(person1.getPersonnelName());
                }
            }
        }
        
        // Conflit de ressources (même spécialité, personnel limité)
        if (event1.getSpecialty() != null && event1.getSpecialty().equals(event2.getSpecialty())) {
            List<PersonnelAssignment> availablePersonnel = 
                specialtyService.getPersonnelBySpecialty(event1.getSpecialty());
            
            int totalRequired = event1.getAssignedPersonnel().size() + event2.getAssignedPersonnel().size();
            int totalAvailable = availablePersonnel.size();
            
            if (totalRequired > totalAvailable) {
                conflictTypes.add(ConflictType.INSUFFICIENT_PERSONNEL);
            }
        }
        
        // Conflit de localisation (si même lieu - à implémenter plus tard); // Conflit de priorité (événements critiques vs non critiques)
        
        if (!conflictTypes.isEmpty()) {
            return new ConflictInfo(
                event1, event2, conflictTypes, conflictingPersonnel,
                calculateOverlapDuration(event1, event2),
                calculateSeverity(conflictTypes, conflictingPersonnel.size())
            );
        }
        
        return null;
    }
    
    /**
     * Vérifie s'il y a un chevauchement temporel entre deux événements
     */
    private boolean hasTimeOverlap(PlanningEvent event1, PlanningEvent event2) {
        LocalDateTime start1 = event1.getStartDateTime();
        LocalDateTime end1 = event1.getEndDateTime();
        LocalDateTime start2 = event2.getStartDateTime();
        LocalDateTime end2 = event2.getEndDateTime();
        
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
    
    /**
     * Calcule la durée de chevauchement entre deux événements
     */
    private long calculateOverlapDuration(PlanningEvent event1, PlanningEvent event2) {
        LocalDateTime overlapStart = event1.getStartDateTime().isAfter(event2.getStartDateTime()) ?
            event1.getStartDateTime() : event2.getStartDateTime();
        
        LocalDateTime overlapEnd = event1.getEndDateTime().isBefore(event2.getEndDateTime()) ?
            event1.getEndDateTime() : event2.getEndDateTime();
        
        return java.time.Duration.between(overlapStart, overlapEnd).toMinutes();
    }
    
    /**
     * Calcule la sévérité du conflit
     */
    private ConflictSeverity calculateSeverity(List<ConflictType> types, int personnelConflicts) {
        if (types.contains(ConflictType.PERSONNEL_DOUBLE_BOOKING) && personnelConflicts > 2) {
            return ConflictSeverity.CRITICAL;
        }
        if (types.contains(ConflictType.PERSONNEL_DOUBLE_BOOKING) || 
            types.contains(ConflictType.INSUFFICIENT_PERSONNEL)) {
            return ConflictSeverity.HIGH;
        }
        return ConflictSeverity.MEDIUM;
    }
    
    /**
     * Propose des solutions pour résoudre un conflit
     */
    public List<ConflictResolution> proposeResolutions(ConflictInfo conflict) {
        List<ConflictResolution> resolutions = new ArrayList<>();
        
        // Décaler un des événements
        resolutions.add(new ConflictResolution(
            ResolutionType.RESCHEDULE_EVENT1,
            "Décaler l'événement '" + conflict.getEvent1().getTitle() + "' d'une heure",
            "Décale automatiquement le premier événement pour éviter le conflit"
        ));
        
        resolutions.add(new ConflictResolution(
            ResolutionType.RESCHEDULE_EVENT2,
            "Décaler l'événement '" + conflict.getEvent2().getTitle() + "' d'une heure",
            "Décale automatiquement le second événement pour éviter le conflit"
        ));
        
        // Réassigner du personnel
        if (conflict.getTypes().contains(ConflictType.PERSONNEL_DOUBLE_BOOKING)) {
            resolutions.add(new ConflictResolution(
                ResolutionType.REASSIGN_PERSONNEL,
                "Réassigner le personnel en conflit",
                "Propose du personnel alternatif avec les mêmes compétences"
            ));
        }
        
        // Diviser l'événement
        if (conflict.getOverlapMinutes() < 120) { // Moins de 2h de conflit
            resolutions.add(new ConflictResolution(
                ResolutionType.SPLIT_EVENT,
                "Diviser l'événement le plus long en deux parties",
                "Crée deux créneaux séparés pour éviter le conflit"
            ));
        }
        
        return resolutions;
    }
    
    /**
     * Événement de planning avec métadonnées
     */
    public static class PlanningEvent {
        private final String id;
        private final String title;
        private final LocalDateTime startDateTime;
        private final LocalDateTime endDateTime;
        private final String category;
        private final String specialty;
        private final List<PersonnelAssignment> assignedPersonnel;
        private final String location;
        private final int priority;
        
        public PlanningEvent(String id, String title, LocalDateTime startDateTime, 
                           LocalDateTime endDateTime, String category, String specialty,
                           List<PersonnelAssignment> assignedPersonnel, String location, int priority) {
            this.id = id;
            this.title = title;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.category = category;
            this.specialty = specialty;
            this.assignedPersonnel = assignedPersonnel != null ? assignedPersonnel : new ArrayList<>();
            this.location = location;
            this.priority = priority;
        }
        
        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public LocalDateTime getStartDateTime() { return startDateTime; }
        public LocalDateTime getEndDateTime() { return endDateTime; }
        public String getCategory() { return category; }
        public String getSpecialty() { return specialty; }
        public List<PersonnelAssignment> getAssignedPersonnel() { return assignedPersonnel; }
        public String getLocation() { return location; }
        public int getPriority() { return priority; }
    }
    
    /**
     * Informations sur un conflit détecté
     */
    public static class ConflictInfo {
        private final PlanningEvent event1;
        private final PlanningEvent event2;
        private final List<ConflictType> types;
        private final List<String> conflictingPersonnel;
        private final long overlapMinutes;
        private final ConflictSeverity severity;
        
        public ConflictInfo(PlanningEvent event1, PlanningEvent event2, List<ConflictType> types,
                           List<String> conflictingPersonnel, long overlapMinutes, ConflictSeverity severity) {
            this.event1 = event1;
            this.event2 = event2;
            this.types = types;
            this.conflictingPersonnel = conflictingPersonnel;
            this.overlapMinutes = overlapMinutes;
            this.severity = severity;
        }
        
        // Getters
        public PlanningEvent getEvent1() { return event1; }
        public PlanningEvent getEvent2() { return event2; }
        public List<ConflictType> getTypes() { return types; }
        public List<String> getConflictingPersonnel() { return conflictingPersonnel; }
        public long getOverlapMinutes() { return overlapMinutes; }
        public ConflictSeverity getSeverity() { return severity; }
        
        public String getDescription() {
            StringBuilder desc = new StringBuilder();
            desc.append("Conflit entre '").append(event1.getTitle())
                .append("' et '").append(event2.getTitle()).append("': ");
            
            List<String> typeDescriptions = types.stream()
                .map(type -> switch (type) {
                    case PERSONNEL_DOUBLE_BOOKING -> "Personnel en double réservation";
                    case INSUFFICIENT_PERSONNEL -> "Personnel insuffisant";
                    case LOCATION_CONFLICT -> "Conflit de lieu";
                    case EQUIPMENT_CONFLICT -> "Conflit d'équipement";
                })
                .collect(Collectors.toList());
            
            desc.append(String.join(", ", typeDescriptions));
            
            if (!conflictingPersonnel.isEmpty()) {
                desc.append(" (Personnel concerné: ").append(String.join(", ", conflictingPersonnel)).append(")");
            }
            
            return desc.toString();
        }
    }
    
    /**
     * Proposition de résolution de conflit
     */
    public static class ConflictResolution {
        private final ResolutionType type;
        private final String title;
        private final String description;
        
        public ConflictResolution(ResolutionType type, String title, String description) {
            this.type = type;
            this.title = title;
            this.description = description;
        }
        
        // Getters
        public ResolutionType getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
    }
    
    // Énumérations
    public enum ConflictType {
        PERSONNEL_DOUBLE_BOOKING,
        INSUFFICIENT_PERSONNEL,
        LOCATION_CONFLICT,
        EQUIPMENT_CONFLICT
    }
    
    public enum ConflictSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum ResolutionType {
        RESCHEDULE_EVENT1,
        RESCHEDULE_EVENT2,
        REASSIGN_PERSONNEL,
        SPLIT_EVENT,
        CHANGE_LOCATION,
        MANUAL_RESOLUTION
    }
}
