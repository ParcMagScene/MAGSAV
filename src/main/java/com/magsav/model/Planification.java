package com.magsav.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Modèle pour les planifications d'interventions avec intégration Google
 */
public class Planification {
    
    // Propriétés de base
    private final IntegerProperty id = new SimpleIntegerProperty(0);
    private final IntegerProperty interventionId = new SimpleIntegerProperty(0);
    private final IntegerProperty technicienId = new SimpleIntegerProperty(0);
    private final IntegerProperty vehiculeId = new SimpleIntegerProperty(0);
    private final IntegerProperty clientId = new SimpleIntegerProperty(0);
    
    // Propriétés de planification
    private final StringProperty datePlanifiee = new SimpleStringProperty("");
    private final IntegerProperty dureeEstimee = new SimpleIntegerProperty(60);
    private final ObjectProperty<StatutPlanification> statut = new SimpleObjectProperty<>(StatutPlanification.PLANIFIE);
    private final ObjectProperty<PrioritePlanification> priorite = new SimpleObjectProperty<>(PrioritePlanification.NORMALE);
    private final ObjectProperty<TypeIntervention> typeIntervention = new SimpleObjectProperty<>(TypeIntervention.MAINTENANCE);
    
    // Localisation
    private final StringProperty lieuIntervention = new SimpleStringProperty("");
    private final StringProperty coordonneesGps = new SimpleStringProperty("");
    
    // Détails de l'intervention
    private final StringProperty equipementsRequis = new SimpleStringProperty(""); // JSON
    private final StringProperty notesPlanification = new SimpleStringProperty("");
    
    // Exécution réelle
    private final StringProperty dateDebutReel = new SimpleStringProperty("");
    private final StringProperty dateFinReel = new SimpleStringProperty("");
    private final StringProperty commentairesExecution = new SimpleStringProperty("");
    
    // Intégration Google
    private final StringProperty googleEventId = new SimpleStringProperty("");
    private final StringProperty googleMeetUrl = new SimpleStringProperty("");
    private final BooleanProperty syncGoogleCalendar = new SimpleBooleanProperty(false);
    
    // Notifications
    private final BooleanProperty notificationClientEmail = new SimpleBooleanProperty(true);
    private final BooleanProperty notificationTechnicienEmail = new SimpleBooleanProperty(true);
    private final BooleanProperty emailReminderSent = new SimpleBooleanProperty(false);
    
    // Métadonnées
    private final StringProperty dateCreation = new SimpleStringProperty("");
    private final StringProperty dateModification = new SimpleStringProperty("");
    
    // Propriétés de jointure (pour affichage)
    private final StringProperty technicienNom = new SimpleStringProperty("");
    private final StringProperty vehiculeImmatriculation = new SimpleStringProperty("");
    private final StringProperty clientNom = new SimpleStringProperty("");
    private final StringProperty interventionNumero = new SimpleStringProperty("");
    
    public enum StatutPlanification {
        PLANIFIE("Planifié", "#007bff"),
        EN_COURS("En cours", "#fd7e14"),
        TERMINE("Terminé", "#28a745"),
        ANNULE("Annulé", "#dc3545"),
        REPORTE("Reporté", "#6c757d");
        
        private final String displayName;
        private final String color;
        
        StatutPlanification(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getColor() {
            return color;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum PrioritePlanification {
        URGENTE("Urgente", 1),
        HAUTE("Haute", 2),
        NORMALE("Normale", 3),
        BASSE("Basse", 4);
        
        private final String displayName;
        private final int niveau;
        
        PrioritePlanification(String displayName, int niveau) {
            this.displayName = displayName;
            this.niveau = niveau;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public int getNiveau() {
            return niveau;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    public enum TypeIntervention {
        MAINTENANCE("Maintenance"),
        DEPANNAGE("Dépannage"),
        INSTALLATION("Installation"),
        CONTROLE("Contrôle"),
        FORMATION("Formation"),
        CONSULTATION("Consultation");
        
        private final String displayName;
        
        TypeIntervention(String displayName) {
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
    public Planification() {
        this.dateCreation.set(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        this.dateModification.set(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    // Getters et Setters
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }
    public IntegerProperty idProperty() { return id; }
    
    public int getInterventionId() { return interventionId.get(); }
    public void setInterventionId(int interventionId) { this.interventionId.set(interventionId); }
    public IntegerProperty interventionIdProperty() { return interventionId; }
    
    public int getTechnicienId() { return technicienId.get(); }
    public void setTechnicienId(int technicienId) { this.technicienId.set(technicienId); }
    public IntegerProperty technicienIdProperty() { return technicienId; }
    
    public int getVehiculeId() { return vehiculeId.get(); }
    public void setVehiculeId(int vehiculeId) { this.vehiculeId.set(vehiculeId); }
    public IntegerProperty vehiculeIdProperty() { return vehiculeId; }
    
    public int getClientId() { return clientId.get(); }
    public void setClientId(int clientId) { this.clientId.set(clientId); }
    public IntegerProperty clientIdProperty() { return clientId; }
    
    public String getDatePlanifiee() { return datePlanifiee.get(); }
    public void setDatePlanifiee(String datePlanifiee) { this.datePlanifiee.set(datePlanifiee); }
    public StringProperty datePlanifieeProperty() { return datePlanifiee; }
    
    public int getDureeEstimee() { return dureeEstimee.get(); }
    public void setDureeEstimee(int dureeEstimee) { this.dureeEstimee.set(dureeEstimee); }
    public IntegerProperty dureeEstimeeProperty() { return dureeEstimee; }
    
    public StatutPlanification getStatut() { return statut.get(); }
    public void setStatut(StatutPlanification statut) { this.statut.set(statut); }
    public ObjectProperty<StatutPlanification> statutProperty() { return statut; }
    
    public PrioritePlanification getPriorite() { return priorite.get(); }
    public void setPriorite(PrioritePlanification priorite) { this.priorite.set(priorite); }
    public ObjectProperty<PrioritePlanification> prioriteProperty() { return priorite; }
    
    public TypeIntervention getTypeIntervention() { return typeIntervention.get(); }
    public void setTypeIntervention(TypeIntervention typeIntervention) { this.typeIntervention.set(typeIntervention); }
    public ObjectProperty<TypeIntervention> typeInterventionProperty() { return typeIntervention; }
    
    public String getLieuIntervention() { return lieuIntervention.get(); }
    public void setLieuIntervention(String lieuIntervention) { this.lieuIntervention.set(lieuIntervention); }
    public StringProperty lieuInterventionProperty() { return lieuIntervention; }
    
    public String getCoordonneesGps() { return coordonneesGps.get(); }
    public void setCoordonneesGps(String coordonneesGps) { this.coordonneesGps.set(coordonneesGps); }
    public StringProperty coordonneesGpsProperty() { return coordonneesGps; }
    
    public String getEquipementsRequis() { return equipementsRequis.get(); }
    public void setEquipementsRequis(String equipementsRequis) { this.equipementsRequis.set(equipementsRequis); }
    public StringProperty equipementsRequisProperty() { return equipementsRequis; }
    
    public String getNotesPlanification() { return notesPlanification.get(); }
    public void setNotesPlanification(String notesPlanification) { this.notesPlanification.set(notesPlanification); }
    public StringProperty notesPlanificationProperty() { return notesPlanification; }
    
    public String getDateDebutReel() { return dateDebutReel.get(); }
    public void setDateDebutReel(String dateDebutReel) { this.dateDebutReel.set(dateDebutReel); }
    public StringProperty dateDebutReelProperty() { return dateDebutReel; }
    
    public String getDateFinReel() { return dateFinReel.get(); }
    public void setDateFinReel(String dateFinReel) { this.dateFinReel.set(dateFinReel); }
    public StringProperty dateFinReelProperty() { return dateFinReel; }
    
    public String getCommentairesExecution() { return commentairesExecution.get(); }
    public void setCommentairesExecution(String commentairesExecution) { this.commentairesExecution.set(commentairesExecution); }
    public StringProperty commentairesExecutionProperty() { return commentairesExecution; }
    
    // Propriétés Google
    public String getGoogleEventId() { return googleEventId.get(); }
    public void setGoogleEventId(String googleEventId) { this.googleEventId.set(googleEventId); }
    public StringProperty googleEventIdProperty() { return googleEventId; }
    
    public String getGoogleMeetUrl() { return googleMeetUrl.get(); }
    public void setGoogleMeetUrl(String googleMeetUrl) { this.googleMeetUrl.set(googleMeetUrl); }
    public StringProperty googleMeetUrlProperty() { return googleMeetUrl; }
    
    public boolean isSyncGoogleCalendar() { return syncGoogleCalendar.get(); }
    public void setSyncGoogleCalendar(boolean syncGoogleCalendar) { this.syncGoogleCalendar.set(syncGoogleCalendar); }
    public BooleanProperty syncGoogleCalendarProperty() { return syncGoogleCalendar; }
    
    // Propriétés de notification
    public boolean isNotificationClientEmail() { return notificationClientEmail.get(); }
    public void setNotificationClientEmail(boolean notificationClientEmail) { this.notificationClientEmail.set(notificationClientEmail); }
    public BooleanProperty notificationClientEmailProperty() { return notificationClientEmail; }
    
    public boolean isNotificationTechnicienEmail() { return notificationTechnicienEmail.get(); }
    public void setNotificationTechnicienEmail(boolean notificationTechnicienEmail) { this.notificationTechnicienEmail.set(notificationTechnicienEmail); }
    public BooleanProperty notificationTechnicienEmailProperty() { return notificationTechnicienEmail; }
    
    public boolean isEmailReminderSent() { return emailReminderSent.get(); }
    public void setEmailReminderSent(boolean emailReminderSent) { this.emailReminderSent.set(emailReminderSent); }
    public BooleanProperty emailReminderSentProperty() { return emailReminderSent; }
    
    // Métadonnées
    public String getDateCreation() { return dateCreation.get(); }
    public void setDateCreation(String dateCreation) { this.dateCreation.set(dateCreation); }
    public StringProperty dateCreationProperty() { return dateCreation; }
    
    public String getDateModification() { return dateModification.get(); }
    public void setDateModification(String dateModification) { this.dateModification.set(dateModification); }
    public StringProperty dateModificationProperty() { return dateModification; }
    
    // Propriétés de jointure pour affichage
    public String getTechnicienNom() { return technicienNom.get(); }
    public void setTechnicienNom(String technicienNom) { this.technicienNom.set(technicienNom); }
    public StringProperty technicienNomProperty() { return technicienNom; }
    
    public String getVehiculeImmatriculation() { return vehiculeImmatriculation.get(); }
    public void setVehiculeImmatriculation(String vehiculeImmatriculation) { this.vehiculeImmatriculation.set(vehiculeImmatriculation); }
    public StringProperty vehiculeImmatriculationProperty() { return vehiculeImmatriculation; }
    
    public String getClientNom() { return clientNom.get(); }
    public void setClientNom(String clientNom) { this.clientNom.set(clientNom); }
    public StringProperty clientNomProperty() { return clientNom; }
    
    public String getInterventionNumero() { return interventionNumero.get(); }
    public void setInterventionNumero(String interventionNumero) { this.interventionNumero.set(interventionNumero); }
    public StringProperty interventionNumeroProperty() { return interventionNumero; }
    
    // Méthodes utilitaires
    public String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        if (getInterventionNumero() != null && !getInterventionNumero().isEmpty()) {
            sb.append("Intervention ").append(getInterventionNumero());
        } else {
            sb.append("Planification #").append(getId());
        }
        
        if (getDatePlanifiee() != null && !getDatePlanifiee().isEmpty()) {
            sb.append(" - ").append(getDatePlanifiee());
        }
        
        return sb.toString();
    }
    
    public boolean isEnCours() {
        return getStatut() == StatutPlanification.EN_COURS;
    }
    
    public boolean isTerminee() {
        return getStatut() == StatutPlanification.TERMINE;
    }
    
    public boolean isAnnulee() {
        return getStatut() == StatutPlanification.ANNULE;
    }
    
    public boolean isGoogleSyncEnabled() {
        return isSyncGoogleCalendar() && 
               getGoogleEventId() != null && !getGoogleEventId().isEmpty();
    }
    
    public void updateModificationDate() {
        setDateModification(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    @Override
    public String toString() {
        return "Planification{" +
                "id=" + getId() +
                ", interventionId=" + getInterventionId() +
                ", datePlanifiee='" + getDatePlanifiee() + '\'' +
                ", statut=" + getStatut() +
                ", priorite=" + getPriorite() +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Planification that = (Planification) obj;
        return getId() == that.getId();
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(getId());
    }
}