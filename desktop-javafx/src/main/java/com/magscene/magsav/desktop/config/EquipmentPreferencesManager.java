package com.magscene.magsav.desktop.config;

import java.util.prefs.Preferences;

/**
 * Gestionnaire des préférences pour l'affichage des équipements
 */
public class EquipmentPreferencesManager {
    
    private static final String PREFS_NODE = "magsav/equipment";
    private static final String KEY_SHOW_ALL_OWNERS = "showAllOwners";
    private static final String KEY_DEFAULT_OWNER = "defaultOwner";
    
    private static EquipmentPreferencesManager instance;
    private final Preferences prefs;
    
    // Listeners pour les changements de préférences
    private Runnable onPreferencesChanged;
    
    private EquipmentPreferencesManager() {
        prefs = Preferences.userRoot().node(PREFS_NODE);
    }
    
    public static synchronized EquipmentPreferencesManager getInstance() {
        if (instance == null) {
            instance = new EquipmentPreferencesManager();
        }
        return instance;
    }
    
    /**
     * Indique si tous les propriétaires doivent être affichés
     * Par défaut: false (seul MAG SCENE est affiché)
     */
    public boolean isShowAllOwners() {
        return prefs.getBoolean(KEY_SHOW_ALL_OWNERS, false);
    }
    
    /**
     * Définit si tous les propriétaires doivent être affichés
     */
    public void setShowAllOwners(boolean showAll) {
        prefs.putBoolean(KEY_SHOW_ALL_OWNERS, showAll);
        notifyPreferencesChanged();
    }
    
    /**
     * Retourne le propriétaire par défaut pour le filtrage
     * Par défaut: "MAG SCENE"
     */
    public String getDefaultOwner() {
        return prefs.get(KEY_DEFAULT_OWNER, "MAG SCENE");
    }
    
    /**
     * Définit le propriétaire par défaut
     */
    public void setDefaultOwner(String owner) {
        prefs.put(KEY_DEFAULT_OWNER, owner);
        notifyPreferencesChanged();
    }
    
    /**
     * Enregistre un listener pour les changements de préférences
     */
    public void setOnPreferencesChanged(Runnable callback) {
        this.onPreferencesChanged = callback;
    }
    
    private void notifyPreferencesChanged() {
        if (onPreferencesChanged != null) {
            onPreferencesChanged.run();
        }
    }
}
