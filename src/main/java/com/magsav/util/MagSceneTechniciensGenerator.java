package com.magsav.util;

import com.magsav.db.DB;


import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Générateur pour créer les techniciens Mag Scene avec données complètes
 */
public class MagSceneTechniciensGenerator {
    
    private static final Random random = new Random();
    
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Génération des techniciens Mag Scene...");
            
            // D'abord, s'assurer que la société Mag Scene existe
            int magSceneId = ensureMagSceneExists();
            
            // Créer les techniciens
            createTechniciens(magSceneId);
            
            // Créer quelques demandes de test
            createTestRequests();
            
            System.out.println("✅ Génération terminée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static int ensureMagSceneExists() throws SQLException {
        String checkSql = "SELECT id FROM societe WHERE nom LIKE '%Mag Scene%' OR nom LIKE '%MagScene%'";
        String insertSql = """
            INSERT INTO societe (nom, type_societe, adresse, code_postal, ville, telephone, email, 
                               site_web, date_creation, date_modification, actif) 
            VALUES (?, 'SAV', ?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'), 1)
        """;
        
        try (Connection conn = DB.getConnection()) {
            // Vérifier si Mag Scene existe
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 ResultSet rs = checkStmt.executeQuery()) {
                
                if (rs.next()) {
                    int id = rs.getInt("id");
                    System.out.println("✓ Société Mag Scene trouvée (ID: " + id + ")");
                    return id;
                }
            }
            
            // Créer Mag Scene si elle n'existe pas
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setString(1, "Mag Scene");
                insertStmt.setString(2, "123 Avenue du Spectacle");
                insertStmt.setString(3, "75015");
                insertStmt.setString(4, "Paris");
                insertStmt.setString(5, "01 42 50 75 00");
                insertStmt.setString(6, "contact@magscene.fr");
                insertStmt.setString(7, "https://www.magscene.fr");
                
                insertStmt.executeUpdate();
                
                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int id = keys.getInt(1);
                        System.out.println("✓ Société Mag Scene créée (ID: " + id + ")");
                        return id;
                    }
                }
            }
        }
        
        throw new SQLException("Impossible de créer ou trouver la société Mag Scene");
    }
    
    private static void createTechniciens(int magSceneId) throws SQLException {
        // Définition des techniciens Mag Scene
        TechnicienData[] techniciens = {
            new TechnicienData("Cyril", "Dubois", "Technicien Distribution", 
                "cyril.dubois@magscene.fr", "06 12 34 56 78", "VL, PL",
                "Distribution, Manutention, Transport", "15 Rue de la République", 
                "92130", "Issy-les-Moulineaux", "06 87 65 43 21"),
            
            new TechnicienData("Célian", "Martin", "Technicien Lumière", 
                "celian.martin@magscene.fr", "06 23 45 67 89", "VL",
                "Éclairage scénique, DMX, Consoles lumière", "28 Boulevard des Arts", 
                "75011", "Paris", "06 78 56 34 12"),
            
            new TechnicienData("Ben", "Lefebvre", "Technicien Structure", 
                "ben.lefebvre@magscene.fr", "06 34 56 78 90", "VL, PL, CACES",
                "Structures, Levage, Sécurité", "42 Rue du Théâtre", 
                "94200", "Ivry-sur-Seine", "06 69 47 25 83"),
            
            new TechnicienData("Thomas", "Rousseau", "Technicien Son", 
                "thomas.rousseau@magscene.fr", "06 45 67 89 01", "VL",
                "Audio, Mixage, Sonorisation", "7 Place de la Musique", 
                "93100", "Montreuil", "06 58 36 14 92"),
            
            new TechnicienData("Flo", "Moreau", "Stagiaire", 
                "flo.moreau@magscene.fr", "06 56 78 90 12", "VL",
                "Formation générale, Support technique", "18 Rue des Étudiants", 
                "75020", "Paris", "06 47 25 83 61")
        };
        
        String sql = """
            INSERT INTO techniciens (nom, prenom, fonction, email, telephone, telephone_urgence,
                                   adresse, code_postal, ville, permis_conduire, habilitations, 
                                   specialites, statut, societe_id, societe_nom, 
                                   date_obtention_permis, date_validite_habilitations,
                                   date_creation, date_modification, notes, sync_google_enabled)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIF', ?, 'Mag Scene', ?, ?, 
                   datetime('now'), datetime('now'), ?, 0)
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            for (TechnicienData tech : techniciens) {
                pstmt.setString(1, tech.nom);
                pstmt.setString(2, tech.prenom);
                pstmt.setString(3, tech.fonction);
                pstmt.setString(4, tech.email);
                pstmt.setString(5, tech.telephone);
                pstmt.setString(6, tech.telephoneUrgence);
                pstmt.setString(7, tech.adresse);
                pstmt.setString(8, tech.codePostal);
                pstmt.setString(9, tech.ville);
                pstmt.setString(10, tech.permisConduire);
                pstmt.setString(11, generateHabilitationsJson(tech.habilitations));
                pstmt.setString(12, generateSpecialitesJson(tech.specialites));
                pstmt.setInt(13, magSceneId);
                pstmt.setString(14, generateDateObtentionPermis());
                pstmt.setString(15, generateDateValiditeHabilitations());
                pstmt.setString(16, generateNotes(tech.prenom, tech.fonction));
                
                pstmt.executeUpdate();
                
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        int techId = keys.getInt(1);
                        System.out.println("✓ Technicien créé: " + tech.prenom + " " + tech.nom + 
                                         " (" + tech.fonction + ") - ID: " + techId);
                    }
                }
            }
        }
    }
    
    private static String generateHabilitationsJson(String habilitations) {
        // Génère un JSON des habilitations avec dates de validité
        String[] habArray = habilitations.split(", ");
        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < habArray.length; i++) {
            if (i > 0) json.append(", ");
            json.append("{")
                .append("\"nom\": \"").append(habArray[i]).append("\", ")
                .append("\"dateObtention\": \"").append(generateRandomPastDate()).append("\", ")
                .append("\"dateValidite\": \"").append(generateRandomFutureDate()).append("\", ")
                .append("\"organisme\": \"APAVE\"")
                .append("}");
        }
        
        json.append("]");
        return json.toString();
    }
    
    private static String generateSpecialitesJson(String specialites) {
        String[] specArray = specialites.split(", ");
        StringBuilder json = new StringBuilder("[");
        
        for (int i = 0; i < specArray.length; i++) {
            if (i > 0) json.append(", ");
            json.append("\"").append(specArray[i]).append("\"");
        }
        
        json.append("]");
        return json.toString();
    }
    
    private static String generateDateObtentionPermis() {
        // Date aléatoire entre 2010 et 2020
        LocalDate start = LocalDate.of(2010, 1, 1);
        LocalDate end = LocalDate.of(2020, 12, 31);
        long days = start.until(end).getDays();
        return start.plusDays(random.nextLong(days)).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    private static String generateDateValiditeHabilitations() {
        // Date future aléatoire (1 à 3 ans)
        return LocalDate.now().plusYears(1 + random.nextInt(3))
                              .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    private static String generateRandomPastDate() {
        return LocalDate.now().minusYears(1 + random.nextInt(3))
                              .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    private static String generateRandomFutureDate() {
        return LocalDate.now().plusYears(1 + random.nextInt(3))
                              .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
    
    private static String generateNotes(String prenom, String fonction) {
        String[] notes = {
            "Expérience solide dans le domaine du spectacle.",
            "Très fiable et ponctuel.",
            "Bonne relation avec les clients.",
            "Formation continue régulière.",
            "Disponible pour les missions urgentes."
        };
        
        return prenom + " - " + fonction + ". " + notes[random.nextInt(notes.length)];
    }
    
    private static void createTestRequests() throws SQLException {
        System.out.println("📋 Création de demandes de test...");
        
        // Récupérer les IDs des techniciens créés
        List<Integer> technicienIds = new ArrayList<>();
        String sql = "SELECT id FROM techniciens WHERE societe_nom = 'Mag Scene'";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                technicienIds.add(rs.getInt("id"));
            }
        }
        
        if (technicienIds.isEmpty()) {
            System.out.println("⚠ Aucun technicien trouvé pour créer les demandes de test");
            return;
        }
        
        // Créer des demandes d'intervention diverses
        createDiverseRequests(technicienIds);
    }
    
    private static void createDiverseRequests(List<Integer> technicienIds) throws SQLException {
        String[] typesIntervention = {
            "Installation éclairage concert",
            "Maintenance système son",
            "Montage structure scène",
            "Réparation équipement",
            "Support technique événement"
        };
        
        String[] descriptions = {
            "Installation complète du système d'éclairage pour concert en plein air",
            "Maintenance préventive du système de sonorisation principal",
            "Montage et sécurisation de la structure de scène modulaire",
            "Diagnostic et réparation équipement audiovisuel défaillant",
            "Support technique général pour événement corporatif"
        };
        
        String sql = """
            INSERT INTO demande_intervention (type_demande, description, statut_demande, 
                                            date_demande, date_souhaite, priorite, technicien_assigne,
                                            client_nom, lieu_intervention, materiel_requis, notes)
            VALUES ('TECHNIQUE', ?, 'EN_ATTENTE', datetime('now'), ?, 'NORMALE', ?, 
                   ?, ?, ?, 'Demande générée automatiquement pour test')
        """;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < 10; i++) {
                int typeIndex = i % typesIntervention.length;
                int techIndex = i % technicienIds.size();
                
                pstmt.setString(1, descriptions[typeIndex]);
                pstmt.setString(2, generateRandomFutureDate());
                pstmt.setInt(3, technicienIds.get(techIndex));
                pstmt.setString(4, "Client Test " + (i + 1));
                pstmt.setString(5, "Salle de spectacle " + (i + 1) + ", Paris");
                pstmt.setString(6, generateMaterielRequis(typesIntervention[typeIndex]));
                
                pstmt.executeUpdate();
            }
            
            System.out.println("✓ 10 demandes d'intervention créées et assignées aux techniciens");
        }
    }
    
    private static String generateMaterielRequis(String typeIntervention) {
        return switch (typeIntervention) {
            case "Installation éclairage concert" -> "Projecteurs LED, consoles DMX, câblage";
            case "Maintenance système son" -> "Outils de diagnostic, pièces de rechange";
            case "Montage structure scène" -> "Éléments modulaires, outils de fixation, équipements de sécurité";
            case "Réparation équipement" -> "Kit de réparation, outils spécialisés";
            default -> "Matériel standard, outils de base";
        };
    }
    
    // Classe pour structurer les données des techniciens
    private static class TechnicienData {
        final String nom, prenom, fonction, email, telephone, permisConduire, habilitations;
        final String adresse, codePostal, ville, telephoneUrgence, specialites;
        
        TechnicienData(String prenom, String nom, String fonction, String email, String telephone,
                      String permisConduire, String habilitations, String adresse, 
                      String codePostal, String ville, String telephoneUrgence) {
            this.prenom = prenom;
            this.nom = nom;
            this.fonction = fonction;
            this.email = email;
            this.telephone = telephone;
            this.permisConduire = permisConduire;
            this.habilitations = habilitations;
            this.specialites = habilitations; // Utiliser habilitations comme spécialités
            this.adresse = adresse;
            this.codePostal = codePostal;
            this.ville = ville;
            this.telephoneUrgence = telephoneUrgence;
        }
    }
}