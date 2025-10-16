package com.magsav.test;

import com.magsav.model.TypeContact;
import com.magsav.model.Contact;

/**
 * Démonstration du système de contacts MAGSAV
 */
public class ContactDemo {
    
    public static void main(String[] args) {
        System.out.println("=== DÉMONSTRATION SYSTÈME DE CONTACTS ===\n");
        
        // 1. Créer différents types de contacts
        
        // Utilisateur système
        Contact utilisateur = Contact.utilisateur(
            "Dupont", "Michel", "michel.dupont@magsav.com", "01 23 45 67 89"
        );
        
        // Contact de société (service comptabilité d'ACME Corp)
        Contact contactSociete = Contact.contactSociete(
            2L, // ID de ACME Corporation
            "Martin", "Sophie", 
            "sophie.martin@acme.com", "01 98 76 54 32", 
            "1234", "Comptabilité", "Responsable Comptable"
        );
        
        // Contact technique ACME
        Contact contactTechnique = Contact.contactSociete(
            2L, // ID de ACME Corporation
            "Bernard", "Thomas", 
            "thomas.bernard@acme.com", "01 98 76 54 33", 
            "1256", "Support Technique", "Ingénieur Support"
        );
        
        // Contact particulier (ami de Jean Dupont)
        Contact contactParticulier = new Contact();
        contactParticulier.setTypeContact(TypeContact.CONTACT_PARTICULIER);
        contactParticulier.setParticulierId(1L); // ID de Jean Dupont
        contactParticulier.setNom("Moreau");
        contactParticulier.setPrenom("Claire");
        contactParticulier.setEmail("claire.moreau@email.com");
        contactParticulier.setTelephone("06 12 34 56 78");
        contactParticulier.setNotes("Relation: Amie d'enfance");
        
        // 2. Afficher les contacts par type
        System.out.println("👨‍💼 UTILISATEURS SYSTÈME:");
        afficherContact(utilisateur);
        
        System.out.println("\n🏢 CONTACTS DE SOCIÉTÉS:");
        afficherContact(contactSociete);
        afficherContact(contactTechnique);
        
        System.out.println("\n👤 CONTACTS DE PARTICULIERS:");
        afficherContact(contactParticulier);
        
        // 3. Démontrer les différences
        System.out.println("\n=== FONCTIONNALITÉS SPÉCIALISÉES ===");
        
        System.out.println("\n📊 TYPES DE CONTACTS DISPONIBLES:");
        for (TypeContact type : TypeContact.values()) {
            System.out.println("  " + type.getDisplayName() + " - " + type.getDescription());
        }
        
        System.out.println("\n📋 INFORMATIONS DÉTAILLÉES:");
        System.out.println("  Utilisateur: " + utilisateur.getDescription());
        System.out.println("  Contact société: " + contactSociete.getDescription());
        System.out.println("  Contact particulier: " + contactParticulier.getDescription());
        
        System.out.println("\n🔍 RECHERCHE PAR SOCIÉTÉ:");
        System.out.println("  Contacts d'ACME Corp (ID=2):");
        if (contactSociete.getSocieteId() != null && contactSociete.getSocieteId() == 2L) {
            System.out.println("    - " + contactSociete.getFullName() + " (" + contactSociete.getFonction() + ")");
        }
        if (contactTechnique.getSocieteId() != null && contactTechnique.getSocieteId() == 2L) {
            System.out.println("    - " + contactTechnique.getFullName() + " (" + contactTechnique.getFonction() + ")");
        }
        
        System.out.println("\n=== FIN DÉMONSTRATION ===");
    }
    
    private static void afficherContact(Contact contact) {
        System.out.println("  Nom: " + contact.getFullName());
        System.out.println("  Email: " + contact.getEmail());
        System.out.println("  Téléphone: " + contact.getTelephone());
        System.out.println("  Type: " + contact.getTypeContact().getDisplayName());
        
        if (contact.getFonction() != null && !contact.getFonction().trim().isEmpty()) {
            System.out.println("  Fonction: " + contact.getFonction());
        }
        if (contact.getService() != null && !contact.getService().trim().isEmpty()) {
            System.out.println("  Service: " + contact.getService());
        }
        if (contact.getNotes() != null && !contact.getNotes().trim().isEmpty()) {
            System.out.println("  Notes: " + contact.getNotes());
        }
        if (contact.isPrincipal()) {
            System.out.println("  ⭐ Contact principal");
        }
        System.out.println();
    }
}