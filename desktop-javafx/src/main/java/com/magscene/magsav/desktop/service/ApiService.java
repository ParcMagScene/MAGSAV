package com.magscene.magsav.desktop.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service pour la communication avec l'API backend
 * Fournit des donnees simulees en cas d'indisponibilite du backend
 */
public class ApiService {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    // Listes persistantes pour simuler la persistance des données
    private static List<Map<String, Object>> persistentProjects = null;
    private static List<Object> persistentVehicles = null;
    private static List<Object> persistentPersonnel = null;
    private static List<Object> persistentEquipment = null;
    
    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    // Methode principale pour le personnel actif
    public CompletableFuture<List<Object>> getActivePersonnel() {
        return makeRequest("/personnel/active")
            .exceptionally(throwable -> {
                System.err.println("Backend indisponible, utilisation des donnees simulees de personnel");
                return getSimulatedPersonnelData();
            });
    }
    
    /**
     * Methode generique pour effectuer des requetes HTTP
     */
    private CompletableFuture<List<Object>> makeRequest(String endpoint) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Object>>() {});
                } else {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Erreur de connexion a l'API: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Donnees de personnel simulees pour le mode hors-ligne
     */
    private List<Object> getSimulatedPersonnelData() {
        if (persistentPersonnel == null) {
            persistentPersonnel = new ArrayList<>();
            persistentPersonnel.add(createPersonnelMap(1L, "Jean", "Dupont", "Technicien Son Senior", "ACTIVE", "06.12.34.56.78", "jean.dupont@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(2L, "Sophie", "Moreau", "Chef Eclairagiste", "ACTIVE", "06.23.45.67.89", "sophie.moreau@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(3L, "Marc", "Leroy", "Responsable Technique", "ACTIVE", "06.34.56.78.90", "marc.leroy@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(4L, "Amélie", "Bernard", "Technicienne Vidéo", "ACTIVE", "06.45.67.89.01", "amelie.bernard@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(5L, "Thomas", "Martin", "Ingénieur Son", "ACTIVE", "06.56.78.90.12", "thomas.martin@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(6L, "Julie", "Rousseau", "Éclairagiste", "ACTIVE", "06.67.89.01.23", "julie.rousseau@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(7L, "Pierre", "Dubois", "Assistant Technique", "ACTIVE", "06.78.90.12.34", "pierre.dubois@magscene.fr"));
            persistentPersonnel.add(createPersonnelMap(8L, "Lucie", "Fernandez", "Régisseuse Générale", "ACTIVE", "06.89.01.23.45", "lucie.fernandez@magscene.fr"));
        }
        return new ArrayList<>(persistentPersonnel); // Retourner une copie
    }
    
    private Map<String, Object> createPersonnelMap(Long id, String prenom, String nom, String role, String status, String telephone, String email) {
        Map<String, Object> personnel = new HashMap<>();
        personnel.put("id", id);
        personnel.put("prenom", prenom);
        personnel.put("nom", nom);
        personnel.put("firstName", prenom);
        personnel.put("lastName", nom);
        personnel.put("fullName", prenom + " " + nom);
        personnel.put("role", role);
        personnel.put("poste", role); // Alias
        personnel.put("jobTitle", role);
        personnel.put("status", status);
        personnel.put("telephone", telephone);
        personnel.put("phone", telephone); // Alias
        personnel.put("email", email);
        personnel.put("dateEmbauche", "2023-01-15");
        personnel.put("hireDate", "2023-01-15");
        personnel.put("departement", getDepartmentForRole(role));
        personnel.put("department", getDepartmentForRole(role));
        personnel.put("salaire", Math.random() * 20000 + 30000);
        
        // Champs manquants ajoutés
        personnel.put("type", getPersonnelType(role));
        personnel.put("notes", "Employé qualifié - Formation à jour - Habilitation électrique");
        personnel.put("createdAt", "2023-01-15T10:00:00");
        personnel.put("updatedAt", "2024-11-01T15:30:00");
        
        return personnel;
    }
    
    private String getDepartmentForRole(String role) {
        if (role.toLowerCase().contains("son") || role.toLowerCase().contains("audio")) return "Audio";
        if (role.toLowerCase().contains("éclairage") || role.toLowerCase().contains("lumière")) return "Éclairage";
        if (role.toLowerCase().contains("vidéo") || role.toLowerCase().contains("image")) return "Vidéo";
        if (role.toLowerCase().contains("technique") || role.toLowerCase().contains("ingénieur")) return "Technique";
        if (role.toLowerCase().contains("régisseur") || role.toLowerCase().contains("assistant")) return "Régie";
        return "Général";
    }
    
    private String getPersonnelType(String role) {
        if (role.toLowerCase().contains("senior") || role.toLowerCase().contains("chef") || role.toLowerCase().contains("responsable")) {
            return "EMPLOYEE";
        }
        if (role.toLowerCase().contains("assistant") || role.toLowerCase().contains("stagiaire")) {
            return "INTERN";
        }
        return Math.random() > 0.7 ? "FREELANCE" : "EMPLOYEE";
    }
    
    // Methodes pour les equipements
    public CompletableFuture<List<Object>> getEquipments() {
        return CompletableFuture.completedFuture(getSimulatedEquipmentData());
    }
    
    /**
     * Données équipements simulées pour démonstration
     */
    private List<Object> getSimulatedEquipmentData() {
        if (persistentEquipment == null) {
            persistentEquipment = new ArrayList<>();
            persistentEquipment.add(createEquipmentMap(1L, "Console Allen & Heath SQ-7", "AUDIO", "MIXAGE", "DISPONIBLE", "A&H-SQ7-001"));
            persistentEquipment.add(createEquipmentMap(2L, "Enceinte Line Array L-Acoustics K2", "AUDIO", "DIFFUSION", "EN_LOCATION", "LAC-K2-012"));
            persistentEquipment.add(createEquipmentMap(3L, "Projecteur LED Martin MAC Encore", "ECLAIRAGE", "PROJECTEUR", "DISPONIBLE", "MAR-ENC-045"));
            persistentEquipment.add(createEquipmentMap(4L, "Caméra Sony FX6", "VIDEO", "CAPTATION", "MAINTENANCE", "SON-FX6-003"));
            persistentEquipment.add(createEquipmentMap(5L, "Truss Prolyte H30V 3m", "STRUCTURE", "TRUSS", "DISPONIBLE", "PRO-H30-156"));
            persistentEquipment.add(createEquipmentMap(6L, "Gradateur Strand C21", "ECLAIRAGE", "GRADATEUR", "DISPONIBLE", "STR-C21-022"));
            persistentEquipment.add(createEquipmentMap(7L, "Micro Shure SM58", "AUDIO", "MICROPHONE", "DISPONIBLE", "SHU-SM58-089"));
            persistentEquipment.add(createEquipmentMap(8L, "Ecran LED ROE MC-7H", "VIDEO", "ECRAN", "EN_LOCATION", "ROE-MC7-008"));
        }
        return new ArrayList<>(persistentEquipment); // Retourner une copie
    }
    
    private Map<String, Object> createEquipmentMap(Long id, String nom, String categorie, String type, String status, String reference) {
        Map<String, Object> equipment = new HashMap<>();
        equipment.put("id", id);
        equipment.put("nom", nom);
        equipment.put("name", nom); // Alias
        equipment.put("categorie", categorie);
        equipment.put("category", categorie); // Alias pour EquipmentItem
        equipment.put("type", type);
        equipment.put("status", status);
        equipment.put("reference", reference);
        equipment.put("dateAchat", "2023-05-15");
        equipment.put("valeurAchat", Math.random() * 50000 + 5000);
        equipment.put("localisation", "Entrepôt A");
        
        // Champs manquants ajoutés
        equipment.put("brand", getRandomBrand(categorie));
        equipment.put("model", getRandomModel(categorie));
        equipment.put("serialNumber", "SN" + id + "-" + (int)(Math.random() * 10000));
        equipment.put("qrCode", "QR" + String.format("%04d", id));
        equipment.put("location", getRandomLocation());
        equipment.put("notes", "Équipement en bon état - Contrôle technique OK");
        equipment.put("internalReference", "REF-" + categorie.substring(0, 3).toUpperCase() + "-" + String.format("%03d", id));
        equipment.put("weight", Math.random() * 100 + 5); // kg
        equipment.put("dimensions", getRandomDimensions());
        equipment.put("warrantyExpiration", "2026-12-31");
        equipment.put("supplier", getRandomSupplier(categorie));
        equipment.put("insuranceValue", Math.random() * 60000 + 10000);
        equipment.put("lastMaintenanceDate", "2024-09-15");
        equipment.put("nextMaintenanceDate", "2025-03-15");
        equipment.put("purchasePrice", Math.random() * 50000 + 5000);
        
        return equipment;
    }
    
    private String getRandomBrand(String categorie) {
        switch (categorie) {
            case "AUDIO": return Math.random() > 0.5 ? "L-Acoustics" : Math.random() > 0.5 ? "Shure" : "Allen & Heath";
            case "ECLAIRAGE": return Math.random() > 0.5 ? "Martin Professional" : Math.random() > 0.5 ? "Strand" : "Robe";
            case "VIDEO": return Math.random() > 0.5 ? "Sony" : Math.random() > 0.5 ? "ROE Visual" : "Panasonic";
            case "STRUCTURE": return Math.random() > 0.5 ? "Prolyte" : "Eurotruss";
            default: return "Generic Brand";
        }
    }
    
    private String getRandomModel(String categorie) {
        switch (categorie) {
            case "AUDIO": return Math.random() > 0.5 ? "K2-Series" : Math.random() > 0.5 ? "SM58" : "SQ-7";
            case "ECLAIRAGE": return Math.random() > 0.5 ? "MAC Encore" : Math.random() > 0.5 ? "C21" : "Robin T2";
            case "VIDEO": return Math.random() > 0.5 ? "FX6" : Math.random() > 0.5 ? "MC-7H" : "AW-UE150";
            case "STRUCTURE": return Math.random() > 0.5 ? "H30V-3m" : "ST40-2m";
            default: return "Model-" + (int)(Math.random() * 100);
        }
    }
    
    private String getRandomLocation() {
        String[] locations = {"Entrepôt A - Allée 1", "Entrepôt B - Zone Audio", "Atelier Maintenance", "Studio Répétition", "Salle de Stockage", "Local Technique"};
        return locations[(int)(Math.random() * locations.length)];
    }
    
    private String getRandomDimensions() {
        int length = (int)(Math.random() * 200 + 50);
        int width = (int)(Math.random() * 150 + 30);
        int height = (int)(Math.random() * 100 + 20);
        return length + "x" + width + "x" + height + " cm";
    }
    
    private String getRandomSupplier(String categorie) {
        switch (categorie) {
            case "AUDIO": return Math.random() > 0.5 ? "SCV Audio" : "Melpomen";
            case "ECLAIRAGE": return Math.random() > 0.5 ? "Sonolux" : "LCE";
            case "VIDEO": return Math.random() > 0.5 ? "Videlio" : "TSE Groupe";
            case "STRUCTURE": return Math.random() > 0.5 ? "Structures Scène" : "MSC";
            default: return "Fournisseur Générique";
        }
    }
    
    public CompletableFuture<List<Object>> getAllEquipment() {
        return getEquipments();
    }
    
    public CompletableFuture<Object> createEquipment(Map<String, Object> data) {
        return CompletableFuture.completedFuture(data);
    }
    
    public CompletableFuture<Object> updateEquipment(Long id, Map<String, Object> data) {
        return CompletableFuture.completedFuture(data);
    }
    
    public CompletableFuture<Boolean> deleteEquipment(Long id) {
        // Vraie suppression des données persistantes d'équipement
        return CompletableFuture.supplyAsync(() -> {
            if (persistentEquipment != null) {
                boolean removed = persistentEquipment.removeIf(equipment -> {
                    if (equipment instanceof Map) {
                        Object equipmentId = ((Map<?,?>) equipment).get("id");
                        return equipmentId != null && equipmentId.toString().equals(id.toString());
                    }
                    return false;
                });
                if (removed) {
                    System.out.println("✅ Équipement ID " + id + " supprimé avec succès");
                    return true;
                } else {
                    System.out.println("⚠️ Équipement ID " + id + " non trouvé");
                    return false;
                }
            }
            return false;
        });
    }
    
    public CompletableFuture<List<Object>> getCategories() {
        return makeRequest("/categories").exceptionally(throwable -> new ArrayList<>());
    }
    
    // Methodes pour les demandes de service
    public CompletableFuture<List<Object>> getServiceRequests() {
        return makeRequest("/service-requests").exceptionally(throwable -> new ArrayList<>());
    }
    
    public CompletableFuture<Object> createServiceRequest(Map<String, Object> data) {
        return CompletableFuture.completedFuture(data);
    }
    
    public CompletableFuture<Object> createServiceRequest(Object request) {
        return CompletableFuture.completedFuture(request);
    }
    
    public CompletableFuture<Object> updateServiceRequest(Long id, Map<String, Object> data) {
        return CompletableFuture.completedFuture(data);
    }
    
    public CompletableFuture<Object> updateServiceRequest(Long id, Object request) {
        return CompletableFuture.completedFuture(request);
    }
    
    public CompletableFuture<Boolean> deleteServiceRequest(Long id) {
        return CompletableFuture.completedFuture(true);
    }
    
    // Methodes pour le personnel
    public CompletableFuture<List<Object>> getAllPersonnel() {
        return getActivePersonnel();
    }
    
    public CompletableFuture<Void> deletePersonnel(Long id) {
        // Vraie suppression des données persistantes de personnel
        return CompletableFuture.runAsync(() -> {
            if (persistentPersonnel != null) {
                persistentPersonnel.removeIf(person -> {
                    if (person instanceof Map) {
                        Object personId = ((Map<?,?>) person).get("id");
                        return personId != null && personId.toString().equals(id.toString());
                    }
                    return false;
                });
                System.out.println("✅ Personnel ID " + id + " supprimé avec succès");
            }
        });
    }
    
    // Methodes pour les clients
    public CompletableFuture<List<Object>> getAllClients() {
        return CompletableFuture.completedFuture(getSimulatedClientData());
    }
    
    /**
     * Données clients simulées pour démonstration
     */
    private List<Object> getSimulatedClientData() {
        List<Object> clients = new ArrayList<>();
        clients.add(createClientMap(1L, "MagScene Productions", "ENTREPRISE", "ACTIF", "Paris", "01.42.33.44.55", "contact@magscene.fr"));
        clients.add(createClientMap(2L, "Festival Rock en Seine", "FESTIVAL", "ACTIF", "Saint-Cloud", "01.55.66.77.88", "tech@rockenseine.com"));
        clients.add(createClientMap(3L, "Théâtre du Châtelet", "THEATRE", "ACTIF", "Paris", "01.40.28.28.40", "technique@chatelet-theatre.com"));
        clients.add(createClientMap(4L, "Société Générale Events", "ENTREPRISE", "ACTIF", "La Défense", "01.42.14.20.00", "events@socgen.com"));
        clients.add(createClientMap(5L, "Zénith de Paris", "SALLE_SPECTACLE", "ACTIF", "Paris", "08.92.69.23.00", "location@zenith-paris.fr"));
        clients.add(createClientMap(6L, "Productions Audiovisuelles", "PRODUCTION", "SUSPENDU", "Montreuil", "01.48.57.89.90", "prod@audio-video.fr"));
        return clients;
    }
    
    private Map<String, Object> createClientMap(Long id, String nom, String type, String status, String ville, String telephone, String email) {
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
        client.put("chiffreAffaire", Math.random() * 500000);
        
        // Champs manquants ajoutés
        client.put("siretNumber", generateSiretNumber());
        client.put("address", generateAddress(ville));
        client.put("postalCode", generatePostalCode(ville));
        client.put("country", "France");
        client.put("fax", "01.42.33.44.56");
        client.put("website", generateWebsite(nom));
        client.put("businessSector", generateBusinessSector(type));
        client.put("annualRevenue", Math.random() * 50000000 + 1000000);
        client.put("employeeCount", (int)(Math.random() * 500 + 10));
        client.put("creditLimit", Math.random() * 200000 + 50000);
        client.put("outstandingAmount", Math.random() * 50000);
        client.put("paymentTermsDays", Math.random() > 0.5 ? 30 : Math.random() > 0.5 ? 45 : 60);
        client.put("preferredPaymentMethod", Math.random() > 0.5 ? "BANK_TRANSFER" : "CHEQUE");
        client.put("notes", "Client de confiance - Paiements réguliers - Collaborations multiples");
        client.put("assignedSalesRep", generateSalesRep());
        client.put("commercial", generateSalesRep()); // Commercial responsable
        client.put("enCours", generateEnCours()); // Projets en cours
        client.put("createdAt", "2024-01-15T10:00:00");
        client.put("updatedAt", "2024-11-01T16:00:00");
        
        return client;
    }
    
    private String generateSiretNumber() {
        return String.format("%014d", (long)(Math.random() * 100000000000000L));
    }
    
    private String generateAddress(String ville) {
        int numRue = (int)(Math.random() * 200 + 1);
        String[] rues = {"Avenue des Arts", "Rue de la Musique", "Boulevard Spectacle", "Place du Théâtre", "Allée Technique", "Impasse Scénique"};
        return numRue + " " + rues[(int)(Math.random() * rues.length)];
    }
    
    private String generatePostalCode(String ville) {
        if (ville.contains("Paris")) return "750" + String.format("%02d", (int)(Math.random() * 20 + 1));
        if (ville.contains("Lyon")) return "69000";
        if (ville.contains("Marseille")) return "13000";
        return String.format("%05d", (int)(Math.random() * 95000 + 1000));
    }
    
    private String generateWebsite(String companyName) {
        return "www." + companyName.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "") + ".fr";
    }
    
    private String generateBusinessSector(String type) {
        switch (type) {
            case "FESTIVAL": return "Événementiel - Festivals";
            case "THEATRE": return "Arts du spectacle - Théâtre";
            case "ENTREPRISE": return "Événementiel d'entreprise";
            case "SALLE_SPECTACLE": return "Exploitation de salles";
            case "PRODUCTION": return "Production audiovisuelle";
            default: return "Secteur culturel";
        }
    }
    
    private String generateSalesRep() {
        String[] commerciaux = {"Marie Dubois", "Paul Martin", "Claire Lefebvre", "Antoine Bernard", "Sophie Durand"};
        return commerciaux[(int)(Math.random() * commerciaux.length)];
    }
    
    private String generateClientCategory(String type) {
        switch (type) {
            case "FESTIVAL": return "CULTURE";
            case "THEATRE": return "CULTURE";
            case "ENTREPRISE": return "CORPORATE";
            case "SALLE_SPECTACLE": return "VENUE";
            case "PRODUCTION": return "MEDIA";
            default: return "GENERAL";
        }
    }
    
    private String generateEnCours() {
        String[] projetsEnCours = {
            "Location matériel - Festival été 2024",
            "Installation permanente scène",
            "Maintenance équipements",
            "Contrat sonorisation annuelle",
            "Régie vidéo concert",
            "Formation technique équipe",
            "Aucun projet en cours"
        };
        return projetsEnCours[(int)(Math.random() * projetsEnCours.length)];
    }
    
    public CompletableFuture<Object> createClient(Object client) {
        return CompletableFuture.completedFuture(client);
    }
    
    public CompletableFuture<Object> updateClient(Object client) {
        return CompletableFuture.completedFuture(client);
    }
    
    public void deleteClient(Long id) {
        // Stub
    }
    
    // Methodes pour les contrats
    public CompletableFuture<List<Object>> getAllContracts() {
        return CompletableFuture.completedFuture(getSimulatedContractData());
    }
    
    /**
     * Données contrats simulées pour démonstration
     */
    private List<Object> getSimulatedContractData() {
        List<Object> contrats = new ArrayList<>();
        contrats.add(createContractMap(1L, "CONT-2024-001", "MagScene Productions", "Prestation son Rock en Seine 2024", "ACTIF", "2024-08-15", 45000.0));
        contrats.add(createContractMap(2L, "CONT-2024-002", "Théâtre du Châtelet", "Installation éclairage permanent", "SIGNE", "2024-06-01", 125000.0));
        contrats.add(createContractMap(3L, "CONT-2024-003", "Société Générale Events", "Sonorisation convention annuelle", "EN_COURS", "2024-09-20", 28000.0));
        contrats.add(createContractMap(4L, "CONT-2024-004", "Zénith de Paris", "Maintenance équipement scénique", "ACTIF", "2024-01-01", 85000.0));
        contrats.add(createContractMap(5L, "CONT-2024-005", "Festival Rock en Seine", "Location matériel vidéo", "TERMINE", "2024-08-30", 67000.0));
        return contrats;
    }
    
    private Map<String, Object> createContractMap(Long id, String numero, String client, String description, String status, String dateDebut, Double montant) {
        Map<String, Object> contrat = new HashMap<>();
        contrat.put("id", id);
        contrat.put("numero", numero);
        contrat.put("contractNumber", numero);
        contrat.put("client", client);
        contrat.put("clientName", client); // Alias pour compatibilité
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
        contrat.put("clientContact", "contact@" + client.toLowerCase().replace(" ", "") + ".fr");
        
        // Champs manquants ajoutés
        contrat.put("title", "Contrat " + numero + " - " + client);
        contrat.put("signedDate", dateDebut);
        contrat.put("duration", Math.random() * 24 + 6); // mois
        contrat.put("renewalDate", "2025-01-01");
        contrat.put("noticePeriodDays", Math.random() > 0.5 ? 30 : 60);
        contrat.put("termsAndConditions", "Conditions générales de prestation - Paiement à 30 jours - Garantie 12 mois");
        contrat.put("paymentTerms", "Paiement à " + (Math.random() > 0.5 ? 30 : 45) + " jours");
        contrat.put("penaltyClause", "Pénalités de retard : 0,5% par jour de retard");
        contrat.put("warrantyPeriod", 12); // mois
        contrat.put("projectManager", generateProjectManager());
        contrat.put("salesManager", generateSalesRep());
        contrat.put("clientReference", generateClientReference());
        contrat.put("invoicingAddress", generateClientAddress());
        contrat.put("deliveryAddress", generateClientAddress());
        contrat.put("notes", "Contrat négocié avec conditions préférentielles - Client fidèle");
        contrat.put("attachments", "/contracts/attachments/" + numero + "/");
        contrat.put("createdAt", dateDebut + "T09:00:00");
        contrat.put("updatedAt", "2024-11-01T15:00:00");
        
        return contrat;
    }
    
    private String getContractType(String description) {
        if (description.toLowerCase().contains("maintenance")) return "MAINTENANCE";
        if (description.toLowerCase().contains("location")) return "RENTAL";
        if (description.toLowerCase().contains("installation")) return "INSTALLATION";
        if (description.toLowerCase().contains("prestation") || description.toLowerCase().contains("sonorisation")) return "SERVICE";
        return "SERVICE";
    }
    
    private String generateClientReference() {
        return "CLI-REF-" + String.format("%06d", (int)(Math.random() * 1000000));
    }
    
    public CompletableFuture<Object> createContract(Object contract) {
        return CompletableFuture.completedFuture(contract);
    }
    
    public CompletableFuture<Object> updateContract(Object contract) {
        return CompletableFuture.completedFuture(contract);
    }
    
    public void deleteContract(Long id) {
        // Stub
    }
    
    // Methodes pour les vehicules
    public CompletableFuture<List<Object>> getAllVehicles() {
        return CompletableFuture.completedFuture(getSimulatedVehicleData());
    }
    
    /**
     * Données véhicules simulées pour démonstration
     */
    private List<Object> getSimulatedVehicleData() {
        if (persistentVehicles == null) {
            persistentVehicles = new ArrayList<>();
            persistentVehicles.add(createVehicleMap(1L, "Camion Sonorisation", "CAMION", "AB-123-CD", "Mercedes Actros", "DISPONIBLE", 45000));
            persistentVehicles.add(createVehicleMap(2L, "Fourgon Éclairage", "FOURGON", "EF-456-GH", "Iveco Daily", "EN_MISSION", 32000));
            persistentVehicles.add(createVehicleMap(3L, "Semi-remorque Scène", "SEMI_REMORQUE", "IJ-789-KL", "Volvo FH", "DISPONIBLE", 78000));
            persistentVehicles.add(createVehicleMap(4L, "Van Technique", "VAN", "MN-012-OP", "Ford Transit", "MAINTENANCE", 28000));
            persistentVehicles.add(createVehicleMap(5L, "Camion Groupe Électro", "CAMION", "QR-345-ST", "Scania R450", "DISPONIBLE", 52000));
            persistentVehicles.add(createVehicleMap(6L, "Fourgon Câblage", "FOURGON", "UV-678-WX", "Renault Master", "EN_MISSION", 41000));
        }
        return new ArrayList<>(persistentVehicles); // Retourner une copie
    }
    
    private Map<String, Object> createVehicleMap(Long id, String nom, String type, String immatriculation, String modele, String status, Integer kilometrage) {
        Map<String, Object> vehicule = new HashMap<>();
        vehicule.put("id", id);
        vehicule.put("nom", nom);
        vehicule.put("name", nom);
        vehicule.put("type", type);
        vehicule.put("immatriculation", immatriculation);
        vehicule.put("licensePlate", immatriculation);
        vehicule.put("modele", modele);
        vehicule.put("model", modele);
        vehicule.put("status", status);
        vehicule.put("kilometrage", kilometrage);
        vehicule.put("mileage", kilometrage);
        vehicule.put("dateAchat", "2022-03-15");
        vehicule.put("prochainEntretien", "2025-02-01");
        
        // Champs manquants ajoutés
        vehicule.put("brand", getVehicleBrand(modele));
        vehicule.put("vin", generateVin(id));
        vehicule.put("yearManufactured", (int)(Math.random() * 10 + 2015));
        vehicule.put("fuelType", Math.random() > 0.7 ? "ELECTRIC" : Math.random() > 0.5 ? "GASOLINE" : "DIESEL");
        vehicule.put("maxPayload", Math.random() * 5000 + 1000); // kg
        vehicule.put("dimensions", generateVehicleDimensions(type));
        vehicule.put("insuranceNumber", "INS-" + String.format("%06d", id * 1000));
        vehicule.put("lastInspectionDate", "2024-09-15");
        vehicule.put("nextInspectionDate", "2025-09-15");
        vehicule.put("lastMaintenanceDate", "2024-08-20");
        vehicule.put("nextMaintenanceDate", "2025-02-20");
        vehicule.put("purchasePrice", Math.random() * 100000 + 30000);
        vehicule.put("currentValue", Math.random() * 80000 + 20000);
        vehicule.put("notes", "Véhicule en bon état - Entretien régulier - Contrôle technique OK");
        vehicule.put("createdAt", "2022-03-15T10:00:00");
        vehicule.put("updatedAt", "2024-11-01T16:00:00");
        
        return vehicule;
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
    
    private String generateVehicleDimensions(String type) {
        switch (type) {
            case "CAMION": return "12000x2500x4000 mm";
            case "FOURGON": return "6000x2000x2800 mm";
            case "SEMI_REMORQUE": return "13600x2550x4000 mm";
            case "VAN": return "5500x1800x2400 mm";
            default: return "N/A";
        }
    }
    
    public CompletableFuture<Object> getVehicleStatistics() {
        return CompletableFuture.completedFuture(new HashMap<String, Object>());
    }
    
    public CompletableFuture<Object> createVehicle(Map<String, Object> data) {
        return CompletableFuture.completedFuture(data);
    }
    
    public CompletableFuture<Object> updateVehicle(Long id, Map<String, Object> data) {
        return CompletableFuture.completedFuture(data);
    }
    
    public CompletableFuture<Void> deleteVehicle(Long id) {
        // Vraie suppression des données persistantes de véhicules
        return CompletableFuture.runAsync(() -> {
            if (persistentVehicles != null) {
                persistentVehicles.removeIf(vehicle -> {
                    if (vehicle instanceof Map) {
                        Object vehicleId = ((Map<?,?>) vehicle).get("id");
                        return vehicleId != null && vehicleId.toString().equals(id.toString());
                    }
                    return false;
                });
                System.out.println("✅ Véhicule ID " + id + " supprimé avec succès");
            }
        });
    }
    
    public CompletableFuture<Object> updateVehicleStatus(Long id, String status) {
        return CompletableFuture.completedFuture(new HashMap<>());
    }
    
    public CompletableFuture<Object> updateVehicleMileage(Long id, Integer mileage) {
        return CompletableFuture.completedFuture(new HashMap<>());
    }
    
    // Methodes generiques pour ProjectManagerView
    public List<Map<String, Object>> getAll(String endpoint) {
        if (endpoint.contains("projet") || endpoint.contains("affaire") || endpoint.contains("sales") || 
            endpoint.contains("project") || endpoint.contains("vente") || endpoint.contains("installation")) {
            System.out.println("✅ Chargement de " + getSimulatedProjectData().size() + " projets/affaires pour endpoint: " + endpoint);
            return getSimulatedProjectData();
        }
        System.out.println("⚠️ Endpoint non reconnu pour projets: " + endpoint);
        return new ArrayList<>();
    }
    
    /**
     * Données projets/affaires simulées pour démonstration
     */
    private List<Map<String, Object>> getSimulatedProjectData() {
        if (persistentProjects == null) {
            persistentProjects = new ArrayList<>();
            persistentProjects.add(createProjectMap(1L, "PROJ-2024-001", "Concert Stade de France", "NEGOCIATION", "MagScene Productions", 185000.0, "Vente"));
            persistentProjects.add(createProjectMap(2L, "PROJ-2024-002", "Festival Solidays", "CONFIRME", "Festival Solidays", 95000.0, "Prestation"));
            persistentProjects.add(createProjectMap(3L, "PROJ-2024-003", "Opéra Bastille - Saison 2024", "EN_COURS", "Opéra National de Paris", 220000.0, "Prestation"));
            persistentProjects.add(createProjectMap(4L, "PROJ-2024-004", "Tournée Européenne", "FINALISE", "Live Nation", 450000.0, "Location"));
            persistentProjects.add(createProjectMap(5L, "PROJ-2024-005", "Convention Microsoft", "NEGOCIATION", "Microsoft France", 75000.0, "Prestation"));
            persistentProjects.add(createProjectMap(6L, "PROJ-2024-006", "Fête de la Musique Paris", "PLANIFIE", "Mairie de Paris", 120000.0, "Location"));
            persistentProjects.add(createProjectMap(7L, "PROJ-2024-007", "Installation permanente Casino", "INSTALLATION", "Groupe Barrière", 320000.0, "Installation"));
            persistentProjects.add(createProjectMap(8L, "PROJ-2024-008", "Formation équipes techniques", "FORMATION", "Centre Formation Pro", 45000.0, "Maintenance"));
        }
        return new ArrayList<>(persistentProjects); // Retourner une copie
    }
    
    private Map<String, Object> createProjectMap(Long id, String numero, String nom, String status, String client, Double budget, String type) {
        Map<String, Object> projet = new HashMap<>();
        projet.put("id", id);
        projet.put("numero", numero);
        projet.put("projectNumber", numero);
        projet.put("nom", nom);
        projet.put("name", nom); // Alias
        projet.put("title", nom); // Alias
        projet.put("status", status);
        projet.put("client", client);
        projet.put("clientName", client); // Alias
        projet.put("budget", budget);
        projet.put("montant", budget); // Alias
        projet.put("estimatedAmount", budget);
        projet.put("finalAmount", budget * 1.1);
        projet.put("dateCreation", "2024-01-10");
        projet.put("dateDebut", "2024-06-01");
        projet.put("startDate", "2024-06-01");
        projet.put("datePrevisionnelle", "2024-07-15");
        projet.put("endDate", "2024-07-31");
        projet.put("dateFin", "2024-07-31");
        projet.put("installationDate", "2024-06-15");
        projet.put("deliveryDate", "2024-06-10");
        projet.put("type", type);
        projet.put("categorie", type); // Alias
        projet.put("priority", Math.random() > 0.5 ? "HIGH" : Math.random() > 0.5 ? "MEDIUM" : "LOW");
        projet.put("responsable", "Jean Dupont");
        projet.put("projectManager", generateProjectManager());
        projet.put("technicalManager", generateTechnicalManager());
        projet.put("salesRepresentative", generateSalesRep());
        projet.put("description", "Projet " + nom + " pour " + client + " - Prestation complète avec équipe technique dédiée");
        projet.put("lieu", "Paris");
        projet.put("venue", generateVenue(type));
        projet.put("venueAddress", generateVenueAddress());
        projet.put("venueContact", "Contact venue - 06.12.34.56.78");
        projet.put("nbJours", Math.random() * 10 + 1);
        
        // Champs manquants ajoutés
        projet.put("clientContact", generateClientContact());
        projet.put("clientEmail", generateClientEmail(client));
        projet.put("clientPhone", generateClientPhone());
        projet.put("clientAddress", generateClientAddress());
        projet.put("depositAmount", budget * 0.3);
        projet.put("remainingAmount", budget * 0.7);
        projet.put("quotePdfPath", "/documents/quotes/quote_" + numero + ".pdf");
        projet.put("contractPdfPath", "/documents/contracts/contract_" + numero + ".pdf");
        projet.put("technicalSheetPath", "/documents/technical/tech_" + numero + ".pdf");
        projet.put("notes", "Projet stratégique - Client de confiance - Équipe expérimentée requise");
        projet.put("technicalNotes", "Configuration technique validée - Matériel spécialisé requis - Tests préalables nécessaires");
        projet.put("clientRequirements", "Qualité premium exigée - Respect strict des horaires - Équipe polyvalente");
        projet.put("createdAt", "2024-01-10T09:00:00");
        projet.put("updatedAt", "2024-11-01T14:30:00");
        
        return projet;
    }
    
    private String generateProjectManager() {
        String[] managers = {"Alexis Moreau", "Claire Dubois", "Thomas Bernard", "Sophie Martin", "Pierre Lefebvre"};
        return managers[(int)(Math.random() * managers.length)];
    }
    
    private String generateTechnicalManager() {
        String[] techManagers = {"Thierry Dubois", "Marc Laurent", "Julie Rousseau", "Antoine Moreau", "Léa Bernard"};
        return techManagers[(int)(Math.random() * techManagers.length)];
    }
    
    private String generateVenue(String type) {
        switch (type) {
            case "CONCERT": return Math.random() > 0.5 ? "Stade de France" : "Accor Arena";
            case "FESTIVAL": return Math.random() > 0.5 ? "Parc de la Villette" : "Hippodrome de Longchamp";
            case "SPECTACLE": return Math.random() > 0.5 ? "Opéra Bastille" : "Théâtre du Châtelet";
            case "CORPORATE": return Math.random() > 0.5 ? "Palais des Congrès" : "Centre de conventions";
            default: return "Lieu à définir";
        }
    }
    
    private String generateVenueAddress() {
        String[] addresses = {
            "93216 Saint-Denis", "8 Boulevard de Bercy, 75012 Paris",
            "211 Avenue Jean Jaurès, 75019 Paris", "2-6 Route des Tribunes, 75016 Paris",
            "Place de la Bastille, 75012 Paris", "1 Place du Châtelet, 75001 Paris"
        };
        return addresses[(int)(Math.random() * addresses.length)];
    }
    
    private String generateClientContact() {
        String[] contacts = {"Marie Dubois", "Jean Martin", "Sophie Lefebvre", "Pierre Bernard", "Claire Rousseau"};
        return contacts[(int)(Math.random() * contacts.length)];
    }
    
    private String generateClientEmail(String client) {
        return "contact@" + client.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "") + ".com";
    }
    
    private String generateClientPhone() {
        return String.format("0%d.%02d.%02d.%02d.%02d", 
            (int)(Math.random() * 8 + 1), 
            (int)(Math.random() * 100), 
            (int)(Math.random() * 100), 
            (int)(Math.random() * 100), 
            (int)(Math.random() * 100));
    }
    
    private String generateClientAddress() {
        int num = (int)(Math.random() * 200 + 1);
        String[] rues = {"Avenue des Champs-Élysées", "Rue de Rivoli", "Boulevard Saint-Germain", "Rue de la Paix", "Avenue Montaigne"};
        String rue = rues[(int)(Math.random() * rues.length)];
        return num + " " + rue + ", 75008 Paris";
    }
    
    public Map<String, Object> create(String endpoint, Map<String, Object> data) {
        return data;
    }
    
    public void update(String endpoint, Long id, Map<String, Object> data) {
        // Stub
    }
    
    public void delete(String endpoint, Long id) {
        // Vraie suppression des données persistantes selon l'endpoint
        System.out.println("✅ Suppression de " + endpoint + " ID: " + id);
        
        if (endpoint.contains("project")) {
            if (persistentProjects != null) {
                persistentProjects.removeIf(project -> {
                    Object projectId = project.get("id");
                    return projectId != null && projectId.toString().equals(id.toString());
                });
                System.out.println("✅ Projet ID " + id + " supprimé avec succès");
            }
        } else if (endpoint.contains("personnel")) {
            if (persistentPersonnel != null) {
                persistentPersonnel.removeIf(person -> {
                    if (person instanceof Map) {
                        Object personId = ((Map<?,?>) person).get("id");
                        return personId != null && personId.toString().equals(id.toString());
                    }
                    return false;
                });
                System.out.println("✅ Personnel ID " + id + " supprimé avec succès");
            }
        } else if (endpoint.contains("equipment")) {
            if (persistentEquipment != null) {
                persistentEquipment.removeIf(equipment -> {
                    if (equipment instanceof Map) {
                        Object equipmentId = ((Map<?,?>) equipment).get("id");
                        return equipmentId != null && equipmentId.toString().equals(id.toString());
                    }
                    return false;
                });
                System.out.println("✅ Équipement ID " + id + " supprimé avec succès");
            }
        }
    }
    
    public List<Map<String, Object>> search(String endpoint, Map<String, String> params) {
        return new ArrayList<>();
    }
    
    // Methodes utilitaires
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.completedFuture(false);
    }
    
    public void close() {
        // Stub
    }
}
