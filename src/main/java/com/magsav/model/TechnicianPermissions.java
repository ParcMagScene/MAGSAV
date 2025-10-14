package com.magsav.model;

import java.util.Set;
import java.util.HashSet;

/**
 * Système de permissions granulaires pour les techniciens selon leur fonction
 * Permet de définir des droits spécifiques par poste et spécialité
 */
public class TechnicianPermissions {
    
    /**
     * Énumération des permissions disponibles dans MAGSAV
     */
    public enum Permission {
        // Permissions générales
        VIEW_PRODUCTS("Visualiser les produits"),
        VIEW_INTERVENTIONS("Visualiser les interventions"),
        
        // Permissions de création
        CREATE_DEMANDE_INTERVENTION("Créer demandes d'intervention"),
        CREATE_DEMANDE_PIECES("Créer demandes de pièces"),
        CREATE_DEMANDE_MATERIEL("Créer demandes de matériel"),
        
        // Permissions par domaine technique
        MANAGE_DISTRIBUTION("Gérer distribution/transport"),
        MANAGE_LIGHTING("Gérer éclairage/DMX"),
        MANAGE_STRUCTURE("Gérer structures/levage"),
        MANAGE_AUDIO("Gérer audio/sonorisation"),
        
        // Permissions contacts et sociétés
        VIEW_CONTACTS("Visualiser contacts"),
        CREATE_CONTACTS("Créer/modifier contacts"),
        DELETE_CONTACTS("Supprimer contacts"),
        VIEW_COMPANIES("Visualiser sociétés"),
        CREATE_COMPANIES("Créer/modifier sociétés"),
        DELETE_COMPANIES("Supprimer sociétés"),
        
        // Permissions véhicules
        VIEW_VEHICLES("Visualiser véhicules"),
        MANAGE_VEHICLES("Gérer véhicules"),
        
        // Permissions planning
        VIEW_PLANNING("Visualiser planning"),
        MANAGE_PLANNING("Gérer planning"),
        
        // Permissions validation (limitées)
        VALIDATE_OWN_INTERVENTIONS("Valider ses propres interventions"),
        APPROVE_COLLEAGUE_REQUESTS("Approuver demandes collègues"),
        
        // Permissions rapports
        GENERATE_REPORTS("Générer rapports de mission"),
        VIEW_STATISTICS("Visualiser statistiques");
        
        private final String description;
        
        Permission(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Définit les permissions par fonction technique
     */
    public static Set<Permission> getPermissionsForPosition(String position) {
        Set<Permission> permissions = new HashSet<>();
        
        // Permissions de base pour tous les techniciens
        permissions.add(Permission.VIEW_PRODUCTS);
        permissions.add(Permission.VIEW_INTERVENTIONS);
        permissions.add(Permission.CREATE_DEMANDE_INTERVENTION);
        permissions.add(Permission.VIEW_CONTACTS);
        permissions.add(Permission.VIEW_COMPANIES);
        permissions.add(Permission.VIEW_PLANNING);
        permissions.add(Permission.VALIDATE_OWN_INTERVENTIONS);
        permissions.add(Permission.GENERATE_REPORTS);
        
        // Permissions spécifiques par fonction
        switch (position.toLowerCase()) {
            case "technicien distribution":
                permissions.add(Permission.MANAGE_DISTRIBUTION);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                permissions.add(Permission.VIEW_VEHICLES);
                permissions.add(Permission.MANAGE_VEHICLES);
                permissions.add(Permission.MANAGE_PLANNING);
                permissions.add(Permission.CREATE_CONTACTS); // Pour les clients transport
                break;
                
            case "technicien lumière":
                permissions.add(Permission.MANAGE_LIGHTING);
                permissions.add(Permission.CREATE_DEMANDE_PIECES);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                permissions.add(Permission.VIEW_STATISTICS);
                break;
                
            case "technicien structure":
                permissions.add(Permission.MANAGE_STRUCTURE);
                permissions.add(Permission.CREATE_DEMANDE_PIECES);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                permissions.add(Permission.VIEW_VEHICLES);
                permissions.add(Permission.APPROVE_COLLEAGUE_REQUESTS); // Senior, peut approuver
                permissions.add(Permission.CREATE_COMPANIES); // Pour nouveaux sites
                break;
                
            case "technicien son":
                permissions.add(Permission.MANAGE_AUDIO);
                permissions.add(Permission.CREATE_DEMANDE_PIECES);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                permissions.add(Permission.VIEW_STATISTICS);
                break;
                
            case "chauffeur pl":
                permissions.add(Permission.MANAGE_VEHICLES);
                permissions.add(Permission.VIEW_VEHICLES);
                permissions.add(Permission.MANAGE_DISTRIBUTION);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                permissions.add(Permission.CREATE_CONTACTS); // Pour les clients transport
                permissions.add(Permission.MANAGE_PLANNING);
                break;
                
            case "chauffeur spl":
                permissions.add(Permission.MANAGE_VEHICLES);
                permissions.add(Permission.VIEW_VEHICLES);
                permissions.add(Permission.MANAGE_DISTRIBUTION);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                permissions.add(Permission.CREATE_CONTACTS); // Pour les clients transport
                permissions.add(Permission.MANAGE_PLANNING);
                break;
                
            case "stagiaire":
                // Permissions limitées pour le stagiaire
                permissions.add(Permission.CREATE_DEMANDE_PIECES); // Peut créer des demandes basiques
                // Pas de validation, ni gestion avancée
                break;
                
            default:
                // Permissions par défaut pour rôle technique non spécifié
                permissions.add(Permission.CREATE_DEMANDE_PIECES);
                permissions.add(Permission.CREATE_DEMANDE_MATERIEL);
                break;
        }
        
        return permissions;
    }
    
    /**
     * Vérifie si un utilisateur a une permission spécifique
     */
    public static boolean hasPermission(User user, Permission permission) {
        // Les admins ont toutes les permissions
        if (user.isAdmin()) {
            return true;
        }
        
        // Les intermittents n'ont que les permissions de base de visualisation
        if (user.isIntermittent()) {
            return permission == Permission.VIEW_PRODUCTS || 
                   permission == Permission.VIEW_INTERVENTIONS ||
                   permission == Permission.VIEW_CONTACTS ||
                   permission == Permission.VIEW_COMPANIES;
        }
        
        // Pour les techniciens Mag Scène, vérifier selon leur poste
        if (user.isTechnicienMagScene() && user.position() != null) {
            Set<Permission> userPermissions = getPermissionsForPosition(user.position());
            return userPermissions.contains(permission);
        }
        
        return false;
    }
    
    /**
     * Récupère toutes les permissions d'un utilisateur
     */
    public static Set<Permission> getAllPermissions(User user) {
        if (user.isAdmin()) {
            return Set.of(Permission.values()); // Toutes les permissions
        }
        
        if (user.isIntermittent()) {
            return Set.of(
                Permission.VIEW_PRODUCTS,
                Permission.VIEW_INTERVENTIONS,
                Permission.VIEW_CONTACTS,
                Permission.VIEW_COMPANIES
            );
        }
        
        if (user.isTechnicienMagScene() && user.position() != null) {
            return getPermissionsForPosition(user.position());
        }
        
        return new HashSet<>(); // Aucune permission par défaut
    }
    
    /**
     * Génère un résumé des permissions pour l'affichage
     */
    public static String getPermissionsSummary(User user) {
        Set<Permission> permissions = getAllPermissions(user);
        
        if (permissions.isEmpty()) {
            return "Aucune permission spécifique";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Permissions accordées (").append(permissions.size()).append(") :\n");
        
        permissions.stream()
            .sorted((p1, p2) -> p1.getDescription().compareTo(p2.getDescription()))
            .forEach(perm -> summary.append("• ").append(perm.getDescription()).append("\n"));
        
        return summary.toString();
    }
}