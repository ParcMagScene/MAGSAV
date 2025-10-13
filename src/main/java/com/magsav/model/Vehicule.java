package com.magsav.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle JavaFX pour la gestion des véhicules
 * Compatible avec les propriétés observables JavaFX
 */
public class Vehicule {
    
    public enum TypeVehicule {
        VL("Véhicule Léger"),
        PL("Poids Lourd"),
        SPL("Super Poids Lourd"),
        REMORQUE("Remorque"),
        SCENE_MOBILE("Scène Mobile");
        
        private final String displayName;
        
        TypeVehicule(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum StatutVehicule {
        DISPONIBLE("Disponible"),
        EN_SERVICE("En Service"),
        MAINTENANCE("En Maintenance"),
        HORS_SERVICE("Hors Service");
        
        private final String displayName;
        
        StatutVehicule(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // Propriétés JavaFX observables
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty immatriculation = new SimpleStringProperty();
    private final ObjectProperty<TypeVehicule> typeVehicule = new SimpleObjectProperty<>();
    private final StringProperty marque = new SimpleStringProperty();
    private final StringProperty modele = new SimpleStringProperty();
    private final IntegerProperty annee = new SimpleIntegerProperty();
    private final IntegerProperty kilometrage = new SimpleIntegerProperty();
    private final ObjectProperty<StatutVehicule> statut = new SimpleObjectProperty<>(StatutVehicule.DISPONIBLE);
    private final BooleanProperty locationExterne = new SimpleBooleanProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final StringProperty dateCreation = new SimpleStringProperty();
    private final StringProperty dateModification = new SimpleStringProperty();
    
    // Constructeurs
    public Vehicule() {
        // Initialiser les timestamps
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.dateCreation.set(now);
        this.dateModification.set(now);
    }
    
    public Vehicule(String immatriculation, TypeVehicule typeVehicule) {
        this();
        this.immatriculation.set(immatriculation);
        this.typeVehicule.set(typeVehicule);
    }
    
    // Getters et Setters pour les propriétés
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    public String getImmatriculation() { return immatriculation.get(); }
    public void setImmatriculation(String immatriculation) { this.immatriculation.set(immatriculation); }
    public StringProperty immatriculationProperty() { return immatriculation; }
    
    public TypeVehicule getTypeVehicule() { return typeVehicule.get(); }
    public void setTypeVehicule(TypeVehicule typeVehicule) { this.typeVehicule.set(typeVehicule); }
    public ObjectProperty<TypeVehicule> typeVehiculeProperty() { return typeVehicule; }
    
    public String getMarque() { return marque.get(); }
    public void setMarque(String marque) { this.marque.set(marque); }
    public StringProperty marqueProperty() { return marque; }
    
    public String getModele() { return modele.get(); }
    public void setModele(String modele) { this.modele.set(modele); }
    public StringProperty modeleProperty() { return modele; }
    
    public int getAnnee() { return annee.get(); }
    public void setAnnee(int annee) { this.annee.set(annee); }
    public IntegerProperty anneeProperty() { return annee; }
    
    public int getKilometrage() { return kilometrage.get(); }
    public void setKilometrage(int kilometrage) { this.kilometrage.set(kilometrage); }
    public IntegerProperty kilometrageProperty() { return kilometrage; }
    
    public StatutVehicule getStatut() { return statut.get(); }
    public void setStatut(StatutVehicule statut) { this.statut.set(statut); }
    public ObjectProperty<StatutVehicule> statutProperty() { return statut; }
    
    public boolean isLocationExterne() { return locationExterne.get(); }
    public void setLocationExterne(boolean locationExterne) { this.locationExterne.set(locationExterne); }
    public BooleanProperty locationExterneProperty() { return locationExterne; }
    
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    public String getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(String dateCreation) { this.dateCreation.set(dateCreation); }
    public StringProperty dateCreationProperty() { return dateCreation; }
    
    public String getDateModification() { return dateModification.get(); }
    public void setDateModification(String dateModification) { this.dateModification.set(dateModification); }
    public StringProperty dateModificationProperty() { return dateModification; }
    
    // Méthodes utilitaires
    public void updateModificationDate() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.dateModification.set(now);
    }
    
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (getImmatriculation() != null && !getImmatriculation().trim().isEmpty()) {
            sb.append(getImmatriculation());
        }
        if (getMarque() != null && !getMarque().trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(getMarque());
        }
        if (getModele() != null && !getModele().trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(getModele());
        }
        return sb.length() > 0 ? sb.toString() : "Véhicule sans nom";
    }
    
    public boolean isAvailable() {
        return getStatut() == StatutVehicule.DISPONIBLE;
    }
    
    public boolean needsMaintenance() {
        return getStatut() == StatutVehicule.MAINTENANCE;
    }
    
    @Override
    public String toString() {
        return getDisplayName();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vehicule vehicule = (Vehicule) obj;
        return getId() != 0 && getId() == vehicule.getId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}