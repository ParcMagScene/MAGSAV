package com.magsav.test;

import com.magsav.model.ClientType;
import com.magsav.model.ClientUnifie;
import com.magsav.model.Societe;

/**
 * Démonstration simple de la distinction entre particuliers et sociétés
 */
public class ClientTypeDemo {
    
    public static void main(String[] args) {
        System.out.println("=== DÉMONSTRATION TYPES DE CLIENTS ===\n");
        
        // 1. Créer des exemples de clients
        Societe particulier = new Societe(
            1, "CLIENT", "Jean Dupont", 
            "jean.dupont@email.com", "0123456789", 
            "123 Rue de la Paix, Paris", 
            "Client particulier fidèle", 
            "2024-01-15"
        );
        
        Societe societe = new Societe(
            2, "SOCIETE", "ACME Corporation", 
            "contact@acme.com", "0198765432", 
            "456 Avenue des Entreprises, Nanterre", 
            "Société technologique | SIRET: 12345678901234", 
            "2024-01-16"
        );
        
        Societe administration = new Societe(
            3, "ADMINISTRATION", "Mairie de Paris", 
            "mairie@paris.fr", "0140423456", 
            "Hôtel de Ville, Paris", 
            "Administration publique locale", 
            "2024-01-17"
        );
        
        // 2. Convertir en clients unifiés et afficher les types
        ClientUnifie clientParticulier = ClientUnifie.fromSociete(particulier);
        ClientUnifie clientSociete = ClientUnifie.fromSociete(societe);
        ClientUnifie clientAdmin = ClientUnifie.fromSociete(administration);
        
        System.out.println("👤 PARTICULIER:");
        afficherClient(clientParticulier);
        
        System.out.println("\n🏢 SOCIÉTÉ/ENTREPRISE:");
        afficherClient(clientSociete);
        
        System.out.println("\n🏛️ ADMINISTRATION:");
        afficherClient(clientAdmin);
        
        // 3. Démontrer les différences
        System.out.println("\n=== DIFFÉRENCES SELON LE TYPE ===");
        
        System.out.println("\n📋 Infos légales requises:");
        for (ClientType type : ClientType.values()) {
            System.out.println("  " + type.getIcon() + " " + type.getLabel() + 
                             ": " + (type.hasLegalInfo() ? "OUI (SIRET, etc.)" : "NON"));
        }
        
        System.out.println("\n🎨 Affichage personnalisé:");
        System.out.println("  Particulier: " + clientParticulier.getNomAffichage());
        System.out.println("  Société: " + clientSociete.getNomAffichage());
        System.out.println("  Administration: " + clientAdmin.getNomAffichage());
        
        System.out.println("\n📊 Classification:");
        System.out.println("  " + clientParticulier.nom() + " → " + clientParticulier.type().getDisplayName());
        System.out.println("  " + clientSociete.nom() + " → " + clientSociete.type().getDisplayName());
        System.out.println("  " + clientAdmin.nom() + " → " + clientAdmin.type().getDisplayName());
        
        System.out.println("\n=== FIN DÉMONSTRATION ===");
    }
    
    private static void afficherClient(ClientUnifie client) {
        System.out.println("  Nom: " + client.nom());
        System.out.println("  Email: " + client.email());
        System.out.println("  Téléphone: " + client.telephone());
        System.out.println("  Type: " + client.type().getDisplayName());
        System.out.println("  Icône: " + client.type().getIcon());
        System.out.println("  Description: " + client.getDescription());
        
        if (client.type().hasLegalInfo()) {
            System.out.println("  ⚖️ Infos légales requises pour ce type");
        }
    }
}