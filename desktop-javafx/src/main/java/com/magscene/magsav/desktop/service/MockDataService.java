package com.magscene.magsav.desktop.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service de données simulées pour le mode développement/hors-ligne.
 * Centralise toutes les données de démonstration pour ApiService.
 * 
 * À terme, ce service sera remplacé par de vraies requêtes API.
 */
@SuppressWarnings("unused")
public class MockDataService {

    private static MockDataService instance;

    // Cache des données persistantes
    private List<Object> persistentPersonnel;
    private List<Object> persistentEquipment;
    private List<Object> persistentVehicles;
    private List<Map<String, Object>> persistentProjects;
    private List<Object> persistentClients;
    private List<Object> persistentContracts;
    private List<Map<String, Object>> persistentSuppliers;

    private MockDataService() {
        // Singleton
    }

    public static synchronized MockDataService getInstance() {
        if (instance == null) {
            instance = new MockDataService();
        }
        return instance;
    }

    // ====================== PERSONNEL ======================

    public List<Object> getPersonnelData() {
        if (persistentPersonnel == null) {
            persistentPersonnel = new ArrayList<>();
            persistentPersonnel.add(createPersonnelMap(1L, "Jean", "Dupont", "Technicien Son Senior", "ACTIVE",
                    "06.12.34.56.78", "jean.dupont@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(2L, "Sophie", "Moreau", "Chef Éclairagiste", "ACTIVE",
                    "06.23.45.67.89", "sophie.moreau@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(3L, "Marc", "Leroy", "Responsable Technique", "ACTIVE",
                    "06.34.56.78.90", "marc.leroy@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(4L, "Amélie", "Bernard", "Technicienne Vidéo", "ACTIVE",
                    "06.45.67.89.01", "amelie.bernard@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(5L, "Thomas", "Martin", "Ingénieur Son", "ACTIVE",
                    "06.56.78.90.12", "thomas.martin@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(6L, "Julie", "Rousseau", "Éclairagiste", "ACTIVE",
                    "06.67.89.01.23", "julie.rousseau@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(7L, "Pierre", "Dubois", "Assistant Technique", "ACTIVE",
                    "06.78.90.12.34", "pierre.dubois@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(8L, "Lucie", "Fernandez", "Régisseuse Générale", "ACTIVE",
                    "06.89.01.23.45", "lucie.fernandez@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(9L, "Alex", "Mercier", "Intermittent Musicien", "ACTIVE",
                    "06.90.12.34.56", "alex.mercier@intermittent.fr"));
            persistentPersonnel.add(createPersonnelMap(10L, "Clara", "Durand", "Intermittent Artiste Scénique",
                    "ACTIVE", "06.01.23.45.67", "clara.durand@intermittent.fr"));
        }
        return new ArrayList<>(persistentPersonnel);
    }

    private Map<String, Object> createPersonnelMap(Long id, String prenom, String nom, String role, String status,
            String telephone, String email) {
        Map<String, Object> personnel = new HashMap<>();
        personnel.put("id", id);
        personnel.put("prenom", prenom);
        personnel.put("nom", nom);
        personnel.put("firstName", prenom);
        personnel.put("lastName", nom);
        personnel.put("fullName", prenom + " " + nom);
        personnel.put("role", role);
        personnel.put("poste", role);
        personnel.put("jobTitle", role);
        personnel.put("status", status);
        personnel.put("telephone", telephone);
        personnel.put("phone", telephone);
        personnel.put("email", email);
        personnel.put("dateEmbauche", "2023-01-15");
        personnel.put("hireDate", "2023-01-15");
        personnel.put("departement", getDepartmentForRole(role));
        personnel.put("department", getDepartmentForRole(role));
        personnel.put("type", getPersonnelType(role));
        personnel.put("specialties", generateSpecialties(role));
        personnel.put("notes", "Employé qualifié - Formation à jour");
        personnel.put("createdAt", "2023-01-15T10:00:00");
        personnel.put("updatedAt", "2024-11-01T15:30:00");
        return personnel;
    }

    // ====================== CLIENTS ======================

    public List<Object> getClientData() {
        if (persistentClients == null) {
            persistentClients = new ArrayList<>();
            persistentClients.add(createClientMap(1L, "MagScene Productions", "ENTREPRISE", "ACTIF", "Paris",
                    "01.42.33.44.55", "contact@magscene.fr"));
            persistentClients.add(createClientMap(2L, "Festival Rock en Seine", "FESTIVAL", "ACTIF", "Saint-Cloud",
                    "01.55.66.77.88", "tech@rockenseine.com"));
            persistentClients.add(createClientMap(3L, "Théâtre du Châtelet", "THEATRE", "ACTIF", "Paris",
                    "01.40.28.28.40", "technique@chatelet-theatre.com"));
            persistentClients.add(createClientMap(4L, "Société Générale Events", "ENTREPRISE", "ACTIF", "La Défense",
                    "01.42.14.20.00", "events@socgen.com"));
            persistentClients.add(createClientMap(5L, "Zénith de Paris", "SALLE_SPECTACLE", "ACTIF", "Paris",
                    "08.92.69.23.00", "location@zenith-paris.fr"));
            persistentClients.add(createClientMap(6L, "Productions Audiovisuelles", "PRODUCTION", "SUSPENDU",
                    "Montreuil", "01.48.57.89.90", "prod@audio-video.fr"));
        }
        return new ArrayList<>(persistentClients);
    }

    private Map<String, Object> createClientMap(Long id, String nom, String type, String status, String ville,
            String telephone, String email) {
        Map<String, Object> client = new HashMap<>();
        client.put("id", id);
        client.put("nom", nom);
        client.put("companyName", nom);
        client.put("type", type);
        client.put("status", status);
        client.put("category", generateClientCategory(type));
        client.put("ville", ville);
        client.put("city", ville);
        client.put("telephone", telephone);
        client.put("phone", telephone);
        client.put("email", email);
        client.put("dateCreation", "2024-01-15");
        client.put("siretNumber", generateSiretNumber());
        client.put("address", generateAddress(ville));
        client.put("postalCode", generatePostalCode(ville));
        client.put("country", "France");
        client.put("website", generateWebsite(nom));
        client.put("businessSector", generateBusinessSector(type));
        client.put("notes", "Client de confiance - Paiements réguliers");
        client.put("commercial", generateSalesRep());
        client.put("createdAt", "2024-01-15T10:00:00");
        client.put("updatedAt", "2024-11-01T16:00:00");
        return client;
    }

    // ====================== CONTRATS ======================

    public List<Object> getContractData() {
        if (persistentContracts == null) {
            persistentContracts = new ArrayList<>();
            persistentContracts.add(createContractMap(1L, "CONT-2024-001", "MagScene Productions",
                    "Prestation son Rock en Seine 2024", "ACTIF", "2024-08-15", 45000.0));
            persistentContracts.add(createContractMap(2L, "CONT-2024-002", "Théâtre du Châtelet",
                    "Installation éclairage permanent", "SIGNE", "2024-06-01", 125000.0));
            persistentContracts.add(createContractMap(3L, "CONT-2024-003", "Société Générale Events",
                    "Sonorisation convention annuelle", "EN_COURS", "2024-09-20", 28000.0));
            persistentContracts.add(createContractMap(4L, "CONT-2024-004", "Zénith de Paris",
                    "Maintenance équipement scénique", "ACTIF", "2024-01-01", 85000.0));
            persistentContracts.add(createContractMap(5L, "CONT-2024-005", "Festival Rock en Seine",
                    "Location matériel vidéo", "TERMINE", "2024-08-30", 67000.0));
        }
        return new ArrayList<>(persistentContracts);
    }

    private Map<String, Object> createContractMap(Long id, String numero, String client, String description,
            String status, String dateDebut, Double montant) {
        Map<String, Object> contrat = new HashMap<>();
        contrat.put("id", id);
        contrat.put("numero", numero);
        contrat.put("contractNumber", numero);
        contrat.put("client", client);
        contrat.put("clientName", client);
        contrat.put("description", description);
        contrat.put("status", status);
        contrat.put("dateDebut", dateDebut);
        contrat.put("startDate", dateDebut);
        contrat.put("dateFin", "2024-12-31");
        contrat.put("endDate", "2024-12-31");
        contrat.put("montant", montant);
        contrat.put("montantHT", montant);
        contrat.put("montantTTC", montant * 1.20);
        contrat.put("amount", montant);
        contrat.put("type", getContractType(description));
        contrat.put("responsable", "Jean Dupont");
        contrat.put("title", "Contrat " + numero + " - " + client);
        contrat.put("signedDate", dateDebut);
        contrat.put("paymentTerms", "Paiement à 30 jours");
        contrat.put("notes", "Contrat négocié avec conditions préférentielles");
        contrat.put("createdAt", dateDebut + "T09:00:00");
        contrat.put("updatedAt", "2024-11-01T15:00:00");
        return contrat;
    }

    // ====================== VÉHICULES ======================

    public List<Object> getVehicleData() {
        if (persistentVehicles == null) {
            persistentVehicles = new ArrayList<>();
            persistentVehicles.add(createVehicleMap(1L, "Camion Sonorisation", "CAMION", "AB-123-CD",
                    "Mercedes Actros", "DISPONIBLE", 45000));
            persistentVehicles.add(createVehicleMap(2L, "Fourgon Éclairage", "FOURGON", "EF-456-GH",
                    "Iveco Daily", "EN_MISSION", 32000));
            persistentVehicles.add(createVehicleMap(3L, "Semi-remorque Scène", "SEMI_REMORQUE", "IJ-789-KL",
                    "Volvo FH", "DISPONIBLE", 78000));
            persistentVehicles.add(createVehicleMap(4L, "Van Technique", "VAN", "MN-012-OP",
                    "Ford Transit", "MAINTENANCE", 28000));
            persistentVehicles.add(createVehicleMap(5L, "Camion Groupe Électro", "CAMION", "QR-345-ST",
                    "Scania R450", "DISPONIBLE", 52000));
            persistentVehicles.add(createVehicleMap(6L, "Fourgon Câblage", "FOURGON", "UV-678-WX",
                    "Renault Master", "EN_MISSION", 41000));
        }
        return new ArrayList<>(persistentVehicles);
    }

    private Map<String, Object> createVehicleMap(Long id, String nom, String type, String immatriculation,
            String modele, String status, Integer kilometrage) {
        Map<String, Object> vehicule = new HashMap<>();
        vehicule.put("id", id);
        vehicule.put("nom", nom);
        vehicule.put("name", nom);
        vehicule.put("type", type);
        vehicule.put("immatriculation", immatriculation);
        vehicule.put("licensePlate", immatriculation);
        vehicule.put("modele", modele);
        vehicule.put("model", modele);
        vehicule.put("brand", getVehicleBrand(modele));
        vehicule.put("status", status);
        vehicule.put("kilometrage", kilometrage);
        vehicule.put("mileage", kilometrage);
        vehicule.put("dateAchat", "2022-03-15");
        vehicule.put("prochainEntretien", "2025-02-01");
        vehicule.put("vin", generateVin(id));
        vehicule.put("fuelType", Math.random() > 0.7 ? "ELECTRIC" : "DIESEL");
        vehicule.put("notes", "Véhicule en bon état - Entretien régulier");
        vehicule.put("createdAt", "2022-03-15T10:00:00");
        vehicule.put("updatedAt", "2024-11-01T16:00:00");
        return vehicule;
    }

    // ====================== PROJETS ======================

    public List<Map<String, Object>> getProjectData() {
        if (persistentProjects == null) {
            persistentProjects = new ArrayList<>();
            persistentProjects.add(createProjectMap(1L, "PROJ-2024-001", "Concert Stade de France",
                    "NEGOCIATION", "MagScene Productions", 185000.0, "Vente"));
            persistentProjects.add(createProjectMap(2L, "PROJ-2024-002", "Festival Solidays",
                    "CONFIRME", "Festival Solidays", 95000.0, "Prestation"));
            persistentProjects.add(createProjectMap(3L, "PROJ-2024-003", "Opéra Bastille - Saison 2024",
                    "EN_COURS", "Opéra National de Paris", 220000.0, "Prestation"));
            persistentProjects.add(createProjectMap(4L, "PROJ-2024-004", "Tournée Européenne",
                    "FINALISE", "Live Nation", 450000.0, "Location"));
            persistentProjects.add(createProjectMap(5L, "PROJ-2024-005", "Convention Microsoft",
                    "NEGOCIATION", "Microsoft France", 75000.0, "Prestation"));
            persistentProjects.add(createProjectMap(6L, "PROJ-2024-006", "Fête de la Musique Paris",
                    "PLANIFIE", "Mairie de Paris", 120000.0, "Location"));
        }
        return new ArrayList<>(persistentProjects);
    }

    private Map<String, Object> createProjectMap(Long id, String numero, String nom, String status,
            String client, Double budget, String type) {
        Map<String, Object> projet = new HashMap<>();
        projet.put("id", id);
        projet.put("numero", numero);
        projet.put("projectNumber", numero);
        projet.put("nom", nom);
        projet.put("name", nom);
        projet.put("title", nom);
        projet.put("status", status);
        projet.put("client", client);
        projet.put("clientName", client);
        projet.put("budget", budget);
        projet.put("montant", budget);
        projet.put("estimatedAmount", budget);
        projet.put("dateCreation", "2024-01-10");
        projet.put("dateDebut", "2024-06-01");
        projet.put("startDate", "2024-06-01");
        projet.put("dateFin", "2024-07-31");
        projet.put("endDate", "2024-07-31");
        projet.put("type", type);
        projet.put("categorie", type);
        projet.put("priority", Math.random() > 0.5 ? "HIGH" : "MEDIUM");
        projet.put("responsable", "Jean Dupont");
        projet.put("projectManager", generateProjectManager());
        projet.put("description", "Projet " + nom + " pour " + client);
        projet.put("lieu", "Paris");
        projet.put("notes", "Projet stratégique - Client de confiance");
        projet.put("createdAt", "2024-01-10T09:00:00");
        projet.put("updatedAt", "2024-11-01T14:30:00");
        return projet;
    }

    // ====================== FOURNISSEURS ======================

    public List<Map<String, Object>> getSupplierData() {
        if (persistentSuppliers == null) {
            persistentSuppliers = new ArrayList<>();
            persistentSuppliers.add(createSupplierMap(1L, "Thomann", "Instruments & Audio", "ACTIF",
                    "+49 9546 92220", "thomann@thomann.de"));
            persistentSuppliers.add(createSupplierMap(2L, "La Boutique du Spectacle", "Éclairage", "ACTIF",
                    "01.42.33.44.55", "contact@boutiquespectacle.fr"));
            persistentSuppliers.add(createSupplierMap(3L, "EVI Audio France", "Audio Pro", "ACTIF",
                    "01.45.67.89.00", "ventes@eviaudio.fr"));
            persistentSuppliers.add(createSupplierMap(4L, "Robert Juliat", "Projecteurs", "ACTIF",
                    "01.64.97.10.00", "info@music-group.fr"));
            persistentSuppliers.add(createSupplierMap(5L, "Adam Hall", "Hardware & Câbles", "ACTIF",
                    "+49 6103 8601", "info@adamhall.com"));
        }
        return new ArrayList<>(persistentSuppliers);
    }

    private Map<String, Object> createSupplierMap(Long id, String nom, String categorie, String status,
            String telephone, String email) {
        Map<String, Object> supplier = new HashMap<>();
        supplier.put("id", id);
        supplier.put("nom", nom);
        supplier.put("name", nom);
        supplier.put("categorie", categorie);
        supplier.put("category", categorie);
        supplier.put("status", status);
        supplier.put("telephone", telephone);
        supplier.put("phone", telephone);
        supplier.put("email", email);
        supplier.put("address", generateAddress("Paris"));
        supplier.put("website", generateWebsite(nom));
        supplier.put("paymentTerms", "30 jours");
        supplier.put("notes", "Fournisseur de confiance - Délais respectés");
        supplier.put("createdAt", "2023-06-01T10:00:00");
        supplier.put("updatedAt", "2024-11-01T14:00:00");
        return supplier;
    }

    // ====================== HELPERS ======================

    private String getDepartmentForRole(String role) {
        if (role.toLowerCase().contains("son") || role.toLowerCase().contains("audio"))
            return "Audio";
        if (role.toLowerCase().contains("éclairage") || role.toLowerCase().contains("lumière"))
            return "Éclairage";
        if (role.toLowerCase().contains("vidéo") || role.toLowerCase().contains("image"))
            return "Vidéo";
        if (role.toLowerCase().contains("technique"))
            return "Technique";
        if (role.toLowerCase().contains("régisseur"))
            return "Régie";
        return "Général";
    }

    private String getPersonnelType(String role) {
        if (role.toLowerCase().contains("senior") || role.toLowerCase().contains("chef"))
            return "EMPLOYEE";
        if (role.toLowerCase().contains("intermittent"))
            return "PERFORMER";
        return "EMPLOYEE";
    }

    private String generateSpecialties(String role) {
        if (role.toLowerCase().contains("son"))
            return "Son, Mixage, Mastering";
        if (role.toLowerCase().contains("éclairage"))
            return "Éclairage, DMX, LED";
        if (role.toLowerCase().contains("vidéo"))
            return "Vidéo, Montage, Streaming";
        return "Polyvalent";
    }

    private String generateClientCategory(String type) {
        switch (type) {
            case "FESTIVAL": return "Événementiel";
            case "THEATRE": return "Arts du spectacle";
            case "ENTREPRISE": return "Corporate";
            default: return "Autre";
        }
    }

    private String generateSiretNumber() {
        return String.format("%014d", (long) (Math.random() * 100000000000000L));
    }

    private String generateAddress(String ville) {
        int numRue = (int) (Math.random() * 200 + 1);
        String[] rues = { "Avenue des Arts", "Rue de la Musique", "Boulevard Spectacle" };
        return numRue + " " + rues[(int) (Math.random() * rues.length)];
    }

    private String generatePostalCode(String ville) {
        if (ville.contains("Paris"))
            return "75001";
        return "92000";
    }

    private String generateWebsite(String companyName) {
        return "www." + companyName.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "") + ".fr";
    }

    private String generateBusinessSector(String type) {
        switch (type) {
            case "FESTIVAL": return "Événementiel - Festivals";
            case "THEATRE": return "Arts du spectacle";
            case "ENTREPRISE": return "Corporate";
            default: return "Secteur culturel";
        }
    }

    private String generateSalesRep() {
        String[] commerciaux = { "Marie Dubois", "Paul Martin", "Claire Lefebvre" };
        return commerciaux[(int) (Math.random() * commerciaux.length)];
    }

    private String getContractType(String description) {
        if (description.toLowerCase().contains("maintenance"))
            return "MAINTENANCE";
        if (description.toLowerCase().contains("location"))
            return "RENTAL";
        if (description.toLowerCase().contains("installation"))
            return "INSTALLATION";
        return "SERVICE";
    }

    private String getVehicleBrand(String modele) {
        if (modele.contains("Mercedes")) return "Mercedes-Benz";
        if (modele.contains("Iveco")) return "Iveco";
        if (modele.contains("Volvo")) return "Volvo";
        if (modele.contains("Ford")) return "Ford";
        if (modele.contains("Scania")) return "Scania";
        if (modele.contains("Renault")) return "Renault";
        return "Generic";
    }

    private String generateVin(Long id) {
        return "VF1" + String.format("%014d", id * 123456789L).substring(0, 14);
    }

    private String generateProjectManager() {
        String[] managers = { "Alexis Moreau", "Claire Dubois", "Thomas Bernard" };
        return managers[(int) (Math.random() * managers.length)];
    }

    // ====================== MUTATIONS ======================

    public void addPersonnel(Object personnel) {
        if (persistentPersonnel == null) getPersonnelData();
        persistentPersonnel.add(personnel);
    }

    public void removePersonnel(Long id) {
        if (persistentPersonnel == null) return;
        persistentPersonnel.removeIf(p -> {
            if (p instanceof Map) {
                Object pId = ((Map<?, ?>) p).get("id");
                return pId != null && pId.toString().equals(id.toString());
            }
            return false;
        });
    }

    public void addVehicle(Object vehicle) {
        if (persistentVehicles == null) getVehicleData();
        persistentVehicles.add(vehicle);
    }

    public void removeVehicle(Long id) {
        if (persistentVehicles == null) return;
        persistentVehicles.removeIf(v -> {
            if (v instanceof Map) {
                Object vId = ((Map<?, ?>) v).get("id");
                return vId != null && vId.toString().equals(id.toString());
            }
            return false;
        });
    }

    public void addProject(Map<String, Object> project) {
        if (persistentProjects == null) getProjectData();
        persistentProjects.add(project);
    }

    public void removeProject(Long id) {
        if (persistentProjects == null) return;
        persistentProjects.removeIf(p -> {
            Object pId = p.get("id");
            return pId != null && pId.toString().equals(id.toString());
        });
    }
}
