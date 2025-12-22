package com.magscene.magsav.desktop.service.planning;

import com.magscene.magsav.desktop.config.SpecialtiesConfigManager;
import com.magscene.magsav.desktop.service.ApiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de liaison entre les spécialités personnel et le système de planning
 * Permet de gérer les affectations par compétences et les disponibilités
 */
public class SpecialtyPlanningService {
    
    private final SpecialtiesConfigManager specialtiesManager;
    @SuppressWarnings("unused")
    private final ApiService apiService;
    private final Map<String, List<PersonnelAssignment>> specialtyAssignments;
    private final List<PersonnelAssignment> personnelAssignments;
    private final ObservableList<SpecialtyFilter> activeFilters;
    
    public SpecialtyPlanningService(ApiService apiService) {
        this.apiService = apiService;
        this.specialtiesManager = SpecialtiesConfigManager.getInstance();
        this.specialtyAssignments = new HashMap<>();
        this.personnelAssignments = new ArrayList<>();
        this.activeFilters = FXCollections.observableArrayList();
        
        initializeAssignments();
    }
    
    /**
     * Classe représentant l'affectation d'un personnel à une spécialité
     */
    public static class PersonnelAssignment {
        private final String personnelId;
        private final String personnelName;
        private final String personnelType;
        private final String specialty;
        private final int proficiencyLevel; // 1-5 (1=Débutant, 5=Expert)
        private final boolean isAvailable;
        private final Set<LocalDateTime> unavailabilityPeriods;
        
        public PersonnelAssignment(String personnelId, String personnelName, String personnelType, 
                                 String specialty, int proficiencyLevel) {
            this.personnelId = personnelId;
            this.personnelName = personnelName;
            this.personnelType = personnelType;
            this.specialty = specialty;
            this.proficiencyLevel = Math.max(1, Math.min(5, proficiencyLevel));
            this.isAvailable = true;
            this.unavailabilityPeriods = new HashSet<>();
        }
        
        // Getters
        public String getPersonnelId() { return personnelId; }
        public String getPersonnelName() { return personnelName; }
        public String getPersonnelType() { return personnelType; }
        public String getSpecialty() { return specialty; }
        public int getProficiencyLevel() { return proficiencyLevel; }
        public boolean isAvailable() { return isAvailable; }
        public Set<LocalDateTime> getUnavailabilityPeriods() { return unavailabilityPeriods; }
        
        public String getProficiencyLabel() {
            return switch (proficiencyLevel) {
                case 1 -> "Débutant";
                case 2 -> "Junior";
                case 3 -> "Confirmé";
                case 4 -> "Senior";
                case 5 -> "Expert";
                default -> "Non défini";
            };
        }
        
        @Override
        public String toString() {
            return personnelName + " (" + personnelType + ") - " + getProficiencyLabel();
        }
    }
    
    /**
     * Filtre pour sélectionner les spécialités à afficher dans le planning
     */
    public static class SpecialtyFilter {
        private final String specialty;
        private boolean active;
        private final String color;
        private boolean showOnlyExperts;
        
        public SpecialtyFilter(String specialty, String color) {
            this.specialty = specialty;
            this.active = true;
            this.color = color;
            this.showOnlyExperts = false;
        }
        
        // Getters et setters
        public String getSpecialty() { return specialty; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public String getColor() { return color; }
        public boolean isShowOnlyExperts() { return showOnlyExperts; }
        public void setShowOnlyExperts(boolean showOnlyExperts) { this.showOnlyExperts = showOnlyExperts; }
        
        @Override
        public String toString() { return specialty; }
    }
    
    /**
     * Initialise les affectations avec des données de test
     * TODO: Remplacer par des appels API réels
     */
    private void initializeAssignments() {
        // Couleurs par spécialité
        Map<String, String> specialtyColors = Map.of(
            "Son", "#2196f3",
            "Éclairage", "#ff9800", 
            "Vidéo", "#9c27b0",
            "Régie", "#4caf50",
            "Machinerie", "#795548",
            "Structure", "#607d8b",
            "Électricité", "#ffc107",
            "Sécurité", "#f44336",
            "Transport", "#00bcd4",
            "Maintenance", "#673ab7"
        );
        
        // Créer les filtres pour chaque spécialité
        for (String specialty : specialtiesManager.getAvailableSpecialties()) {
            String color = specialtyColors.getOrDefault(specialty, "#757575");
            activeFilters.add(new SpecialtyFilter(specialty, color));
        }
        
        // Données de test pour les affectations
        addSampleAssignments();
    }
    
    /**
     * Ajoute des affectations de test
     */
    private void addSampleAssignments() {
        // Son
        addAssignment(new PersonnelAssignment("1", "Jean Dupont", "Technicien", "Son", 4));
        addAssignment(new PersonnelAssignment("2", "Marie Martin", "Ingénieur", "Son", 5));
        addAssignment(new PersonnelAssignment("5", "Luc Bernard", "Intermittent", "Son", 3));
        
        // Éclairage
        addAssignment(new PersonnelAssignment("1", "Jean Dupont", "Technicien", "Éclairage", 3));
        addAssignment(new PersonnelAssignment("3", "Pierre Durand", "Intermittent", "Éclairage", 5));
        addAssignment(new PersonnelAssignment("4", "Sophie Bernard", "Chef d'équipe", "Éclairage", 4));
        
        // Vidéo
        addAssignment(new PersonnelAssignment("2", "Marie Martin", "Ingénieur", "Vidéo", 4));
        addAssignment(new PersonnelAssignment("6", "Alex Chen", "Freelance", "Vidéo", 5));
        
        // Régie
        addAssignment(new PersonnelAssignment("4", "Sophie Bernard", "Chef d'équipe", "Régie", 5));
        addAssignment(new PersonnelAssignment("7", "Emma Rousseau", "Technicienne", "Régie", 4));
        
        // Maintenance
        addAssignment(new PersonnelAssignment("8", "Thomas Lefebvre", "Technicien", "Maintenance", 5));
        addAssignment(new PersonnelAssignment("9", "Julie Moreau", "Ingénieure", "Maintenance", 4));
    }
    
    private void addAssignment(PersonnelAssignment assignment) {
        specialtyAssignments.computeIfAbsent(assignment.getSpecialty(), k -> new ArrayList<>())
                .add(assignment);
        personnelAssignments.add(assignment);
    }
    
    /**
     * Retourne le personnel affecté à une spécialité donnée
     */
    public List<PersonnelAssignment> getPersonnelForSpecialty(String specialty) {
        return specialtyAssignments.getOrDefault(specialty, new ArrayList<>());
    }
    
    /**
     * Retourne le personnel disponible pour une spécialité à une date donnée
     */
    public List<PersonnelAssignment> getAvailablePersonnelForSpecialty(String specialty, 
                                                                       LocalDateTime dateTime) {
        return getPersonnelForSpecialty(specialty).stream()
            .filter(PersonnelAssignment::isAvailable)
            .filter(assignment -> !assignment.getUnavailabilityPeriods().contains(dateTime))
            .collect(Collectors.toList());
    }
    
    /**
     * Retourne le personnel expert (niveau 4-5) pour une spécialité
     */
    public List<PersonnelAssignment> getExpertPersonnelForSpecialty(String specialty) {
        return getPersonnelForSpecialty(specialty).stream()
            .filter(assignment -> assignment.getProficiencyLevel() >= 4)
            .collect(Collectors.toList());
    }
    
    /**
     * Retourne toutes les spécialités d'un personnel donné
     */
    public List<String> getSpecialtiesForPersonnel(String personnelId) {
        return specialtyAssignments.entrySet().stream()
            .filter(entry -> entry.getValue().stream()
                .anyMatch(assignment -> assignment.getPersonnelId().equals(personnelId)))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Retourne le niveau de compétence d'un personnel pour une spécialité
     */
    public int getProficiencyLevel(String personnelId, String specialty) {
        return getPersonnelForSpecialty(specialty).stream()
            .filter(assignment -> assignment.getPersonnelId().equals(personnelId))
            .mapToInt(PersonnelAssignment::getProficiencyLevel)
            .findFirst()
            .orElse(0);
    }
    
    /**
     * Retourne les spécialités actives (filtrées) pour le planning
     */
    public List<String> getActiveSpecialties() {
        return activeFilters.stream()
            .filter(SpecialtyFilter::isActive)
            .map(SpecialtyFilter::getSpecialty)
            .collect(Collectors.toList());
    }
    
    /**
     * Retourne toutes les spécialités disponibles
     */
    public List<String> getAllSpecialties() {
        return personnelAssignments.stream()
                .map(PersonnelAssignment::getSpecialty)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne le personnel pour une spécialité donnée, trié par compétence
     */
    public List<PersonnelAssignment> getPersonnelBySpecialty(String specialty) {
        return personnelAssignments.stream()
                .filter(assignment -> assignment.getSpecialty().equals(specialty))
                .sorted((a, b) -> Integer.compare(b.getProficiencyLevel(), a.getProficiencyLevel())) // Tri par compétence décroissante
                .collect(Collectors.toList());
    }
    
    /**
     * Retourne les filtres de spécialités
     */
    public ObservableList<SpecialtyFilter> getSpecialtyFilters() {
        return activeFilters;
    }
    
    /**
     * Met à jour l'état d'un filtre de spécialité
     */
    public void updateSpecialtyFilter(String specialty, boolean active) {
        activeFilters.stream()
            .filter(filter -> filter.getSpecialty().equals(specialty))
            .findFirst()
            .ifPresent(filter -> filter.setActive(active));
    }
    
    /**
     * Retourne la couleur associée à une spécialité
     */
    public String getSpecialtyColor(String specialty) {
        return activeFilters.stream()
            .filter(filter -> filter.getSpecialty().equals(specialty))
            .map(SpecialtyFilter::getColor)
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Vérifie s'il y a des conflits de planning pour une spécialité
     */
    public boolean hasSchedulingConflicts(String specialty, LocalDateTime startTime, 
                                         LocalDateTime endTime) {
        // TODO: Implémenter la logique de détection de conflits; // Vérifier si le personnel nécessaire est disponible
        List<PersonnelAssignment> available = getAvailablePersonnelForSpecialty(specialty, startTime);
        return available.isEmpty();
    }
    
    /**
     * Suggère le meilleur personnel pour une spécialité et une période
     */
    public Optional<PersonnelAssignment> suggestBestPersonnel(String specialty, 
                                                             LocalDateTime startTime, 
                                                             LocalDateTime endTime) {
        return getAvailablePersonnelForSpecialty(specialty, startTime).stream()
            .max(Comparator.comparing(PersonnelAssignment::getProficiencyLevel)
                          .thenComparing(PersonnelAssignment::getPersonnelName));
    }
    
    /**
     * Retourne les statistiques des spécialités
     */
    public Map<String, Integer> getSpecialtyStatistics() {
        return specialtyAssignments.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));
    }
    
    /**
     * Rafraîchit les données depuis le backend
     */
    public void refreshData() {
        // TODO: Implémenter la synchronisation avec l'API
        System.out.println("Rafraîchissement des données spécialités/personnel...");
    }
}
