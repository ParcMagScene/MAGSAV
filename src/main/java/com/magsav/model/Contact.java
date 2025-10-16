package com.magsav.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Contact {
    private long id;
    private TypeContact typeContact;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String fonction;
    private Long societeId;
    private String notes;
    private boolean actif = true;
    private boolean principal = false;
    
    public static Contact utilisateur(String nom, String prenom, String email, String telephone) {
        Contact contact = new Contact();
        contact.typeContact = TypeContact.UTILISATEUR;
        contact.nom = nom;
        contact.prenom = prenom;
        contact.email = email;
        contact.telephone = telephone;
        contact.principal = true;
        return contact;
    }
    
    public static Contact contactSociete(Long societeId, String nom, String prenom, 
                                       String email, String telephone, String poste, 
                                       String service, String fonction) {
        Contact contact = new Contact();
        contact.typeContact = TypeContact.CONTACT_SOCIETE;
        contact.societeId = societeId;
        contact.nom = nom;
        contact.prenom = prenom;
        contact.email = email;
        contact.telephone = telephone;
        contact.fonction = fonction;
        return contact;
    }
    
    // Getters et Setters essentiels
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public TypeContact getTypeContact() { return typeContact; }
    public void setTypeContact(TypeContact typeContact) { this.typeContact = typeContact; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }
    
    public Long getSocieteId() { return societeId; }
    public void setSocieteId(Long societeId) { this.societeId = societeId; }
    
    public Long getParticulierId() { return null; } // Simplifié pour la démo
    public void setParticulierId(Long particulierId) { /* Simplifié pour la démo */ }
    
    public String getService() { return null; } // Simplifié pour la démo
    public void setService(String service) { /* Simplifié pour la démo */ }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public boolean isPrincipal() { return principal; }
    public void setPrincipal(boolean principal) { this.principal = principal; }
    
    public String getFullName() {
        if (prenom != null && !prenom.trim().isEmpty()) {
            return prenom + " " + nom;
        }
        return nom;
    }
    
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeContact.getIcone()).append(" ").append(getFullName());
        
        if (fonction != null && !fonction.trim().isEmpty()) {
            sb.append(" (").append(fonction).append(")");
        }
        
        if (principal) {
            sb.append(" ⭐");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
}
