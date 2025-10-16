package com.magsav.debug;

import com.magsav.model.DemandeIntervention;
import com.magsav.repo.DemandeInterventionRepository;

/**
 * Générateur de quelques demandes d'intervention pour tester
 */
public class InterventionRequestGenerator {
    
    public static void main(String[] args) {
        try {
            DemandeInterventionRepository repo = new DemandeInterventionRepository();
            
            System.out.println("=== GÉNÉRATION DEMANDES INTERVENTION ===");
            
            // Créer quelques demandes d'intervention d'exemple
            for (int i = 1; i <= 3; i++) {
                DemandeIntervention demande = new DemandeIntervention(
                    0L, // ID (sera généré)
                    DemandeIntervention.StatutDemande.EN_ATTENTE, // Statut
                    DemandeIntervention.TypeDemande.PRODUIT_REPERTORIE, // Type de demande
                    
                    null, // Product ID (pas encore lié)
                    
                    // Informations produit
                    "Console M32 #" + i,
                    "M32-" + String.format("%04d", i),
                    "UID-CON-" + String.format("%03d", i),
                    "Midas",
                    "Audio",
                    "Consoles numériques",
                    "Console de mixage numérique 32 canaux",
                    
                    // Informations propriétaire
                    DemandeIntervention.TypeProprietaire.SOCIETE,
                    1L, // ID société
                    null, // Creation proprietaire ID
                    null, // Nom temp
                    null, // Details temp
                    
                    // Détails intervention
                    "Problème de crachotements sur les canaux 15-16, perte intermittente du signal.",
                    "Urgent - spectacle ce soir !",
                    "Technicien Mag Scène",
                    1L, // Detector société ID
                    
                    "Jean Dupont", // Demandeur
                    null, // Date demande (sera générée)
                    null, null, null, null // Validation (pas encore)
                );
                
                long id = repo.createDemande(demande);
                System.out.println("✅ Demande intervention créée avec ID: " + id);
            }
            
            // Vérifier les demandes créées
            System.out.println("\n=== VÉRIFICATION ===");
            var demandes = repo.findAll();
            System.out.println("Total demandes intervention: " + demandes.size());
            
            for (var d : demandes) {
                System.out.println("  ID=" + d.id() + 
                                   ", Produit=" + d.produitNom() + 
                                   ", UID=" + d.produitUid() + 
                                   ", Statut=" + d.statut());
            }
            
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }
}