package com.magsav.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle pour les techniciens avec intégration Google
 */
public class Technicien {
    
    // Propriétés de base
    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final StringProperty nom = new SimpleStringProperty("");
    private final StringProperty prenom = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty telephone = new SimpleStringProperty("");
    private final StringProperty specialites = new SimpleStringProperty(""); // JSON
    private final ObjectProperty<StatutTechnicien> statut = new SimpleObjectProperty<>(StatutTechnicien.ACTIF);
    private final StringProperty notes = new SimpleStringProperty("");
    
    // Intégration Google
    private final StringProperty googleContactId = new SimpleStringProperty("");
    private final StringProperty googleCalendarId = new SimpleStringProperty("");
    private final BooleanProperty syncGoogleEnabled = new SimpleBooleanProperty(false);
    private final StringProperty lastGoogleSync = new SimpleStringProperty("");
    
    // Métadonnées
    private final StringProperty dateCreation = new SimpleStringProperty("");
    private final StringProperty dateModification = new SimpleStringProperty("");
    
    public enum StatutTechnicien {
        ACTIF("Actif"),
        CONGE("En congé"),
        INDISPONIBLE("Indisponible"),
        INACTIF("Inactif");
        
        private final String displayName;
        
        StatutTechnicien(String displayName) {
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
    
    // Constructeurs
    public Technicien() {
        this.dateCreation.set(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        this.dateModification.set(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    public Technicien(String nom, String prenom, String email) {
        this();
        this.nom.set(nom);
        this.prenom.set(prenom);
        this.email.set(email);
    }
    
    // Getters et Setters pour les propriétés
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    public String getNom() { return nom.get(); }
    public void setNom(String nom) { this.nom.set(nom); }
    public StringProperty nomProperty() { return nom; }
    
    public String getPrenom() { return prenom.get(); }
    public void setPrenom(String prenom) { this.prenom.set(prenom); }
    public StringProperty prenomProperty() { return prenom; }
    
    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }
    public StringProperty emailProperty() { return email; }
    
    public String getTelephone() { return telephone.get(); }
    public void setTelephone(String telephone) { this.telephone.set(telephone); }
    public StringProperty telephoneProperty() { return telephone; }
    
    public String getSpecialites() { return specialites.get(); }
    public void setSpecialites(String specialites) { this.specialites.set(specialites); }
    public StringProperty specialitesProperty() { return specialites; }
    
    public StatutTechnicien getStatut() { return statut.get(); }
    public void setStatut(StatutTechnicien statut) { this.statut.set(statut); }
    public ObjectProperty<StatutTechnicien> statutProperty() { return statut; }
    
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    // Propriétés Google
    public String getGoogleContactId() { return googleContactId.get(); }
    public void setGoogleContactId(String googleContactId) { this.googleContactId.set(googleContactId); }
    public StringProperty googleContactIdProperty() { return googleContactId; }
    
    public String getGoogleCalendarId() { return googleCalendarId.get(); }
    public void setGoogleCalendarId(String googleCalendarId) { this.googleCalendarId.set(googleCalendarId); }
    public StringProperty googleCalendarIdProperty() { return googleCalendarId; }
    
    public boolean isSyncGoogleEnabled() { return syncGoogleEnabled.get(); }
    public void setSyncGoogleEnabled(boolean enabled) { this.syncGoogleEnabled.set(enabled); }
    public BooleanProperty syncGoogleEnabledProperty() { return syncGoogleEnabled; }
    
    public String getLastGoogleSync() { return lastGoogleSync.get(); }
    public void setLastGoogleSync(String lastSync) { this.lastGoogleSync.set(lastSync); }
    public StringProperty lastGoogleSyncProperty() { return lastGoogleSync; }
    
    // Métadonnées
    public String getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(String dateCreation) { this.dateCreation.set(dateCreation); }
    public StringProperty dateCreationProperty() { return dateCreation; }
    
    public String getDateModification() { return dateModification.get(); }
    public void setDateModification(String dateModification) { this.dateModification.set(dateModification); }
    public StringProperty dateModificationProperty() { return dateModification; }
    
    // Méthodes utilitaires
    public String getNomComplet() {
        return getPrenom() + " " + getNom();
    }
    
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNomComplet());
        if (getEmail() != null && !getEmail().isEmpty()) {
            sb.append(" (").append(getEmail()).append(")");
        }
        return sb.toString();
    }
    
    public boolean isDisponible() {
        return getStatut() == StatutTechnicien.ACTIF;
    }
    
    public boolean isGoogleSyncEnabled() {
        return isSyncGoogleEnabled() && 
               getGoogleContactId() != null && !getGoogleContactId().isEmpty();
    }
    
    public void updateModificationDate() {
        setDateModification(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    @Override
    public String toString() {
        return "Technicien{" +
                "id=" + getId() +
                ", nom='" + getNom() + '\'' +
                ", prenom='" + getPrenom() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", statut=" + getStatut() +
                ", syncGoogle=" + isSyncGoogleEnabled() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Technicien that = (Technicien) obj;
        return getId() == that.getId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}