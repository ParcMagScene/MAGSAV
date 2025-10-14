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
    private final StringProperty fonction = new SimpleStringProperty(""); // Fonction spécifique (Distribution, Lumière, etc.)
    private final StringProperty specialites = new SimpleStringProperty(""); // JSON
    private final ObjectProperty<StatutTechnicien> statut = new SimpleObjectProperty<>(StatutTechnicien.ACTIF);
    private final StringProperty notes = new SimpleStringProperty("");
    
    // Informations de contact et adresse
    private final StringProperty adresse = new SimpleStringProperty("");
    private final StringProperty codePostal = new SimpleStringProperty("");
    private final StringProperty ville = new SimpleStringProperty("");
    private final StringProperty telephoneUrgence = new SimpleStringProperty("");
    
    // Permis de conduire et habilitations
    private final StringProperty permisConduire = new SimpleStringProperty(""); // VL, PL, etc.
    private final StringProperty habilitations = new SimpleStringProperty(""); // JSON des habilitations
    private final StringProperty dateObtentionPermis = new SimpleStringProperty("");
    private final StringProperty dateValiditeHabilitations = new SimpleStringProperty("");
    
    // Association société
    private final IntegerProperty societeId = new SimpleIntegerProperty(0);
    private final StringProperty societeNom = new SimpleStringProperty("");
    
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
    
    public String getFonction() { return fonction.get(); }
    public void setFonction(String fonction) { this.fonction.set(fonction); }
    public StringProperty fonctionProperty() { return fonction; }
    
    public String getSpecialites() { return specialites.get(); }
    public void setSpecialites(String specialites) { this.specialites.set(specialites); }
    public StringProperty specialitesProperty() { return specialites; }
    
    public StatutTechnicien getStatut() { return statut.get(); }
    public void setStatut(StatutTechnicien statut) { this.statut.set(statut); }
    public ObjectProperty<StatutTechnicien> statutProperty() { return statut; }
    
    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }
    public StringProperty notesProperty() { return notes; }
    
    // Informations de contact et adresse
    public String getAdresse() { return adresse.get(); }
    public void setAdresse(String adresse) { this.adresse.set(adresse); }
    public StringProperty adresseProperty() { return adresse; }
    
    public String getCodePostal() { return codePostal.get(); }
    public void setCodePostal(String codePostal) { this.codePostal.set(codePostal); }
    public StringProperty codePostalProperty() { return codePostal; }
    
    public String getVille() { return ville.get(); }
    public void setVille(String ville) { this.ville.set(ville); }
    public StringProperty villeProperty() { return ville; }
    
    public String getTelephoneUrgence() { return telephoneUrgence.get(); }
    public void setTelephoneUrgence(String telephoneUrgence) { this.telephoneUrgence.set(telephoneUrgence); }
    public StringProperty telephoneUrgenceProperty() { return telephoneUrgence; }
    
    // Permis et habilitations
    public String getPermisConduire() { return permisConduire.get(); }
    public void setPermisConduire(String permisConduire) { this.permisConduire.set(permisConduire); }
    public StringProperty permisConduireProperty() { return permisConduire; }
    
    public String getHabilitations() { return habilitations.get(); }
    public void setHabilitations(String habilitations) { this.habilitations.set(habilitations); }
    public StringProperty habilitationsProperty() { return habilitations; }
    
    public String getDateObtentionPermis() { return dateObtentionPermis.get(); }
    public void setDateObtentionPermis(String dateObtentionPermis) { this.dateObtentionPermis.set(dateObtentionPermis); }
    public StringProperty dateObtentionPermisProperty() { return dateObtentionPermis; }
    
    public String getDateValiditeHabilitations() { return dateValiditeHabilitations.get(); }
    public void setDateValiditeHabilitations(String dateValiditeHabilitations) { this.dateValiditeHabilitations.set(dateValiditeHabilitations); }
    public StringProperty dateValiditeHabilitationsProperty() { return dateValiditeHabilitations; }
    
    // Association société
    public int getSocieteId() { return societeId.get(); }
    public void setSocieteId(int societeId) { this.societeId.set(societeId); }
    public IntegerProperty societeIdProperty() { return societeId; }
    
    public String getSocieteNom() { return societeNom.get(); }
    public void setSocieteNom(String societeNom) { this.societeNom.set(societeNom); }
    public StringProperty societeNomProperty() { return societeNom; }
    
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