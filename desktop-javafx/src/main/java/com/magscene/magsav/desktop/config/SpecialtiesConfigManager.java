package com.magscene.magsav.desktop.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Gestionnaire de configuration des spécialités personnel
 * Permet la gestion centralisée des spécialités disponibles et leur attribution
 */
public class SpecialtiesConfigManager {
    
    private static final String SPECIALTIES_KEY = "personnel.specialties";
    private static final String CONFIG_FILE = "specialties.properties";
    private static SpecialtiesConfigManager instance;
    
    private final Preferences prefs;
    private final ObservableList<String> availableSpecialties;
    private final Properties configProperties;
    
    private SpecialtiesConfigManager() {
        this.prefs = Preferences.userNodeForPackage(SpecialtiesConfigManager.class);
        this.availableSpecialties = FXCollections.observableArrayList();
        this.configProperties = new Properties();
        loadConfiguration();
    }
    
    public static SpecialtiesConfigManager getInstance() {
        if (instance == null) {
            instance = new SpecialtiesConfigManager();
        }
        return instance;
    }
    
    /**
     * Charge la configuration depuis les préférences et le fichier
     */
    private void loadConfiguration() {
        // Charger depuis le fichier de configuration local s'il existe
        loadFromConfigFile();
        
        // Charger depuis les préférences système
        String specialtiesStr = prefs.get(SPECIALTIES_KEY, getDefaultSpecialties());
        List<String> specialtiesList = Arrays.asList(specialtiesStr.split(";"));
        
        availableSpecialties.clear();
        availableSpecialties.addAll(specialtiesList);
        
        // Trier par ordre alphabétique
        availableSpecialties.sort(String::compareTo);
    }
    
    /**
     * Charge la configuration depuis le fichier local
     */
    private void loadFromConfigFile() {
        Path configPath = getConfigFilePath();
        if (Files.exists(configPath)) {
            try (InputStream input = Files.newInputStream(configPath)) {
                configProperties.load(input);
                
                String fileSpecialties = configProperties.getProperty(SPECIALTIES_KEY);
                if (fileSpecialties != null && !fileSpecialties.isEmpty()) {
                    // Mise à jour des préférences avec les données du fichier
                    prefs.put(SPECIALTIES_KEY, fileSpecialties);
                }
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement du fichier de configuration: " + e.getMessage());
            }
        }
    }
    
    /**
     * Sauvegarde la configuration dans les préférences et le fichier
     */
    public void saveConfiguration() {
        String specialtiesStr = String.join(";", availableSpecialties);
        
        // Sauvegarder dans les préférences système
        prefs.put(SPECIALTIES_KEY, specialtiesStr);
        
        // Sauvegarder dans le fichier local
        configProperties.setProperty(SPECIALTIES_KEY, specialtiesStr);
        saveToConfigFile();
        
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde des préférences: " + e.getMessage());
        }
    }
    
    /**
     * Sauvegarde dans le fichier de configuration local
     */
    private void saveToConfigFile() {
        Path configPath = getConfigFilePath();
        try {
            // Créer le répertoire parent s'il n'existe pas
            Files.createDirectories(configPath.getParent());
            
            try (OutputStream output = Files.newOutputStream(configPath)) {
                configProperties.store(output, "Configuration des spécialités MAGSAV-3.0");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la sauvegarde du fichier de configuration: " + e.getMessage());
        }
    }
    
    /**
     * Retourne le chemin vers le fichier de configuration
     */
    private Path getConfigFilePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, ".magsav", CONFIG_FILE);
    }
    
    /**
     * Retourne les spécialités par défaut
     */
    private String getDefaultSpecialties() {
        return "Son;Éclairage;Vidéo;Régie;Machinerie;Structure;Électricité;Sécurité;Transport;Maintenance";
    }
    
    /**
     * Retourne la liste observable des spécialités disponibles
     */
    public ObservableList<String> getAvailableSpecialties() {
        return availableSpecialties;
    }
    
    /**
     * Ajoute une nouvelle spécialité
     */
    public boolean addSpecialty(String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            return false;
        }
        
        String trimmedSpecialty = specialty.trim();
        if (!availableSpecialties.contains(trimmedSpecialty)) {
            availableSpecialties.add(trimmedSpecialty);
            availableSpecialties.sort(String::compareTo);
            saveConfiguration();
            return true;
        }
        return false;
    }
    
    /**
     * Supprime une spécialité
     */
    public boolean removeSpecialty(String specialty) {
        if (availableSpecialties.remove(specialty)) {
            saveConfiguration();
            return true;
        }
        return false;
    }
    
    /**
     * Modifie une spécialité existante
     */
    public boolean updateSpecialty(String oldSpecialty, String newSpecialty) {
        if (newSpecialty == null || newSpecialty.trim().isEmpty()) {
            return false;
        }
        
        String trimmedNew = newSpecialty.trim();
        int index = availableSpecialties.indexOf(oldSpecialty);
        if (index >= 0 && !availableSpecialties.contains(trimmedNew)) {
            availableSpecialties.set(index, trimmedNew);
            availableSpecialties.sort(String::compareTo);
            saveConfiguration();
            return true;
        }
        return false;
    }
    
    /**
     * Réinitialise les spécialités aux valeurs par défaut
     */
    public void resetToDefaults() {
        availableSpecialties.clear();
        List<String> defaults = Arrays.asList(getDefaultSpecialties().split(";"));
        availableSpecialties.addAll(defaults);
        availableSpecialties.sort(String::compareTo);
        saveConfiguration();
    }
    
    /**
     * Importe des spécialités depuis une liste
     */
    public void importSpecialties(List<String> specialties) {
        availableSpecialties.clear();
        availableSpecialties.addAll(specialties.stream()
            .filter(s -> s != null && !s.trim().isEmpty())
            .map(String::trim)
            .distinct()
            .sorted()
            .toList());
        saveConfiguration();
    }
    
    /**
     * Exporte les spécialités actuelles
     */
    public List<String> exportSpecialties() {
        return new ArrayList<>(availableSpecialties);
    }
}
