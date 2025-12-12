package com.magscene.magsav.desktop.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de recherche globale intelligente
 * Charge les données réelles depuis l'API pour toutes les tables
 * Fournit des suggestions dynamiques classées par type
 */
public class GlobalSearchService {
    
    private final Map<String, List<SearchResult>> searchIndex;
    private ApiService apiService;
    private boolean dataLoaded = false;
    
    public GlobalSearchService() {
        this.searchIndex = new HashMap<>();
        initializeEmptyIndex();
    }
    
    /**
     * Constructeur avec ApiService pour accéder aux données réelles
     */
    public GlobalSearchService(ApiService apiService) {
        this.searchIndex = new HashMap<>();
        this.apiService = apiService;
        initializeEmptyIndex();
        loadAllRealData();
    }
    
    /**
     * Initialise l'index avec des listes vides
     */
    private void initializeEmptyIndex() {
        searchIndex.put("equipements", new ArrayList<>());
        searchIndex.put("clients", new ArrayList<>());
        searchIndex.put("fournisseurs", new ArrayList<>());
        searchIndex.put("personnel", new ArrayList<>());
        searchIndex.put("interventions", new ArrayList<>());
        searchIndex.put("contrats", new ArrayList<>());
        searchIndex.put("vehicules", new ArrayList<>());
        searchIndex.put("projets", new ArrayList<>());
    }
    
    /**
     * Charge toutes les données réelles depuis l'API
     */
    public void loadAllRealData() {
        if (apiService == null) {
            System.err.println("⚠️ ApiService non disponible - chargement des données de démonstration");
            loadDemoData();
            return;
        }
        
        System.out.println("🔄 Chargement des données pour la recherche globale...");
        
        try {
            // Charger les équipements
            loadEquipmentData();
            
            // Charger les clients
            loadClientData();
            
            // Charger les fournisseurs
            loadSupplierData();
            
            // Charger le personnel
            loadPersonnelData();
            
            // Charger les véhicules
            loadVehicleData();
            
            // Charger les interventions SAV
            loadSAVData();
            
            // Charger les contrats
            loadContractData();
            
            // Charger les projets
            loadProjectData();
            
            dataLoaded = true;
            System.out.println("✅ Recherche globale initialisée avec les données réelles");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des données: " + e.getMessage());
            loadDemoData();
        }
    }
    
    /**
     * Charge les équipements depuis l'API
     */
    private void loadEquipmentData() {
        try {
            List<Map<String, Object>> equipment = apiService.getAll("equipment");
            List<SearchResult> results = new ArrayList<>();
            
            int x15Count = 0;
            for (Map<String, Object> item : equipment) {
                String name = getStringValue(item, "name", "designation", "nom");
                String brand = getStringValue(item, "brand", "marque");
                String category = getStringValue(item, "category", "categorie");
                String qrCode = getStringValue(item, "qrCode", "qr_code");
                String locmatCode = getStringValue(item, "internalReference", "locmatCode", "locmat_code");
                String id = getStringValue(item, "id");
                
                // Debug: afficher les équipements contenant X15
                if (name.toUpperCase().contains("X15") || (locmatCode != null && locmatCode.toUpperCase().contains("X15"))) {
                    x15Count++;
                    if (x15Count <= 3) {
                        System.out.println("   🔍 DEBUG X15 trouvé: name='" + name + "', locmat='" + locmatCode + "'");
                    }
                }
                
                if (!name.isEmpty()) {
                    String description = brand;
                    if (!category.isEmpty()) {
                        description += (description.isEmpty() ? "" : " - ") + category;
                    }
                    if (!locmatCode.isEmpty()) {
                        description += " [LOCMAT: " + locmatCode + "]";
                    }
                    if (!qrCode.isEmpty()) {
                        description += " [QR: " + qrCode + "]";
                    }
                    results.add(new SearchResult("Équipement", name, description, "📦", id, locmatCode));
                }
            }
            
            if (x15Count > 0) {
                System.out.println("   🔍 DEBUG: " + x15Count + " équipements X15 trouvés au total");
            }
            
            searchIndex.put("equipements", results);
            System.out.println("   📦 " + results.size() + " équipements chargés");
            
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement équipements: " + e.getMessage());
        }
    }
    
    /**
     * Charge les clients depuis l'API
     */
    private void loadClientData() {
        try {
            apiService.getAllClients().thenAccept(clientList -> {
                List<SearchResult> results = new ArrayList<>();
                
                for (Object obj : clientList) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> client = (Map<String, Object>) obj;
                        String name = getStringValue(client, "nom", "companyName", "name");
                        String type = getStringValue(client, "type", "category");
                        String ville = getStringValue(client, "ville", "city");
                        String email = getStringValue(client, "email");
                        String id = getStringValue(client, "id");
                        
                        if (!name.isEmpty()) {
                            String description = type;
                            if (!ville.isEmpty()) {
                                description += (description.isEmpty() ? "" : " - ") + ville;
                            }
                            if (!email.isEmpty()) {
                                description += " (" + email + ")";
                            }
                            results.add(new SearchResult("Client", name, description, "👥", id));
                        }
                    }
                }
                
                searchIndex.put("clients", results);
                System.out.println("   👥 " + results.size() + " clients chargés");
                
            }).join();
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement clients: " + e.getMessage());
        }
    }
    
    /**
     * Charge les fournisseurs depuis l'API
     */
    private void loadSupplierData() {
        try {
            List<Map<String, Object>> suppliers = apiService.getAll("suppliers");
            List<SearchResult> results = new ArrayList<>();
            
            for (Map<String, Object> supplier : suppliers) {
                String name = getStringValue(supplier, "name", "nom", "companyName");
                String type = getStringValue(supplier, "type", "category");
                String contact = getStringValue(supplier, "contactName", "contact");
                String id = getStringValue(supplier, "id");
                
                if (!name.isEmpty()) {
                    String description = type;
                    if (!contact.isEmpty()) {
                        description += (description.isEmpty() ? "" : " - ") + "Contact: " + contact;
                    }
                    results.add(new SearchResult("Fournisseur", name, description, "🏭", id));
                }
            }
            
            searchIndex.put("fournisseurs", results);
            System.out.println("   🏭 " + results.size() + " fournisseurs chargés");
            
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement fournisseurs: " + e.getMessage());
        }
    }
    
    /**
     * Charge le personnel depuis l'API
     */
    private void loadPersonnelData() {
        try {
            apiService.getAllPersonnel().thenAccept(personnelList -> {
                List<SearchResult> results = new ArrayList<>();
                
                for (Object obj : personnelList) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> person = (Map<String, Object>) obj;
                        String firstName = getStringValue(person, "firstName", "prenom");
                        String lastName = getStringValue(person, "lastName", "nom");
                        String name = (firstName + " " + lastName).trim();
                        String role = getStringValue(person, "role", "fonction", "position");
                        String department = getStringValue(person, "department", "service");
                        String id = getStringValue(person, "id");
                        
                        if (!name.isEmpty() && !name.equals(" ")) {
                            String description = role;
                            if (!department.isEmpty()) {
                                description += (description.isEmpty() ? "" : " - ") + department;
                            }
                            results.add(new SearchResult("Personnel", name, description, "👤", id));
                        }
                    }
                }
                
                searchIndex.put("personnel", results);
                System.out.println("   👤 " + results.size() + " membres du personnel chargés");
                
            }).join();
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement personnel: " + e.getMessage());
        }
    }
    
    /**
     * Charge les véhicules depuis l'API
     */
    private void loadVehicleData() {
        try {
            apiService.getAllVehicles().thenAccept(vehicleList -> {
                List<SearchResult> results = new ArrayList<>();
                
                for (Object obj : vehicleList) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> vehicle = (Map<String, Object>) obj;
                        String name = getStringValue(vehicle, "name", "nom");
                        String plate = getStringValue(vehicle, "licensePlate", "immatriculation");
                        String model = getStringValue(vehicle, "model", "modele");
                        String status = getStringValue(vehicle, "status");
                        String id = getStringValue(vehicle, "id");
                        
                        if (!name.isEmpty()) {
                            String description = model;
                            if (!plate.isEmpty()) {
                                description += (description.isEmpty() ? "" : " - ") + plate;
                            }
                            if (!status.isEmpty()) {
                                description += " [" + status + "]";
                            }
                            results.add(new SearchResult("Véhicule", name, description, "🚐", id));
                        }
                    }
                }
                
                searchIndex.put("vehicules", results);
                System.out.println("   🚐 " + results.size() + " véhicules chargés");
                
            }).join();
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement véhicules: " + e.getMessage());
        }
    }
    
    /**
     * Charge les interventions SAV depuis l'API
     */
    private void loadSAVData() {
        try {
            List<Map<String, Object>> savRequests = apiService.getAll("sav-requests");
            List<SearchResult> results = new ArrayList<>();
            
            for (Map<String, Object> sav : savRequests) {
                String reference = getStringValue(sav, "reference", "numero", "id");
                String title = getStringValue(sav, "title", "objet", "description");
                String status = getStringValue(sav, "status", "statut");
                String equipment = getStringValue(sav, "equipmentName", "equipment");
                String id = getStringValue(sav, "id");
                
                String name = !reference.isEmpty() ? reference : "SAV-" + id;
                if (!title.isEmpty()) {
                    String description = title;
                    if (!equipment.isEmpty()) {
                        description += " - " + equipment;
                    }
                    if (!status.isEmpty()) {
                        description += " [" + status + "]";
                    }
                    results.add(new SearchResult("Intervention SAV", name, description, "🔧", id));
                }
            }
            
            searchIndex.put("interventions", results);
            System.out.println("   🔧 " + results.size() + " interventions SAV chargées");
            
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement SAV: " + e.getMessage());
        }
    }
    
    /**
     * Charge les contrats depuis l'API
     */
    private void loadContractData() {
        try {
            apiService.getAllContracts().thenAccept(contractList -> {
                List<SearchResult> results = new ArrayList<>();
                
                for (Object obj : contractList) {
                    if (obj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> contract = (Map<String, Object>) obj;
                        String reference = getStringValue(contract, "reference", "numero", "contractNumber");
                        String clientName = getStringValue(contract, "clientName", "client");
                        String type = getStringValue(contract, "type", "contractType");
                        String status = getStringValue(contract, "status");
                        String id = getStringValue(contract, "id");
                        
                        String name = !reference.isEmpty() ? reference : "Contrat-" + id;
                        String description = clientName;
                        if (!type.isEmpty()) {
                            description += (description.isEmpty() ? "" : " - ") + type;
                        }
                        if (!status.isEmpty()) {
                            description += " [" + status + "]";
                        }
                        results.add(new SearchResult("Contrat", name, description, "📋", id));
                    }
                }
                
                searchIndex.put("contrats", results);
                System.out.println("   📋 " + results.size() + " contrats chargés");
                
            }).join();
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement contrats: " + e.getMessage());
        }
    }
    
    /**
     * Charge les projets depuis l'API
     */
    private void loadProjectData() {
        try {
            List<Map<String, Object>> projects = apiService.getAll("projects");
            List<SearchResult> results = new ArrayList<>();
            
            for (Map<String, Object> project : projects) {
                String name = getStringValue(project, "name", "nom", "projectName");
                String clientName = getStringValue(project, "clientName", "client");
                String type = getStringValue(project, "type");
                String status = getStringValue(project, "status");
                String id = getStringValue(project, "id");
                
                if (!name.isEmpty()) {
                    String description = "";
                    if (!clientName.isEmpty()) {
                        description = "Client: " + clientName;
                    }
                    if (!type.isEmpty()) {
                        description += (description.isEmpty() ? "" : " - ") + type;
                    }
                    if (!status.isEmpty()) {
                        description += " [" + status + "]";
                    }
                    results.add(new SearchResult("Projet", name, description, "🎭", id));
                }
            }
            
            searchIndex.put("projets", results);
            System.out.println("   🎭 " + results.size() + " projets chargés");
            
        } catch (Exception e) {
            System.err.println("   ⚠️ Erreur chargement projets: " + e.getMessage());
        }
    }
    
    /**
     * Charge des données de démonstration si l'API n'est pas disponible
     */
    private void loadDemoData() {
        System.out.println("📦 Chargement des données de démonstration...");
        
        // Clients de démo
        searchIndex.put("clients", Arrays.asList(
            new SearchResult("Client", "MagScene Productions", "ENTREPRISE - Paris", "👥", "1"),
            new SearchResult("Client", "Festival Rock en Seine", "FESTIVAL - Saint-Cloud", "👥", "2"),
            new SearchResult("Client", "Théâtre du Châtelet", "THEATRE - Paris", "👥", "3"),
            new SearchResult("Client", "Zénith de Paris", "SALLE_SPECTACLE - Paris", "👥", "5")
        ));
        
        // Équipements de démo
        searchIndex.put("equipements", Arrays.asList(
            new SearchResult("Équipement", "Yamaha A15", "Enceinte active 15\" - Audio", "📦", "100"),
            new SearchResult("Équipement", "Shure SM58", "Microphone dynamique - Audio", "📦", "101"),
            new SearchResult("Équipement", "Console Yamaha M32", "Console de mixage 32 voies", "📦", "102")
        ));
        
        // Personnel de démo
        searchIndex.put("personnel", Arrays.asList(
            new SearchResult("Personnel", "Thomas MARTIN", "Ingénieur son - Technique", "👤", "10"),
            new SearchResult("Personnel", "Marie DUPONT", "Responsable planning - Administration", "👤", "11")
        ));
        
        // Véhicules de démo
        searchIndex.put("vehicules", Arrays.asList(
            new SearchResult("Véhicule", "Camion Sonorisation", "Mercedes Actros - AB-123-CD [DISPONIBLE]", "🚐", "20"),
            new SearchResult("Véhicule", "Fourgon Éclairage", "Iveco Daily - EF-456-GH [EN_MISSION]", "🚐", "21")
        ));
        
        // Interventions de démo
        searchIndex.put("interventions", Arrays.asList(
            new SearchResult("Intervention SAV", "SAV-2024-001", "Réparation enceinte - Yamaha A15 [EN_COURS]", "🔧", "30"),
            new SearchResult("Intervention SAV", "SAV-2024-002", "Maintenance console [TERMINE]", "🔧", "31")
        ));
        
        // Projets de démo
        searchIndex.put("projets", Arrays.asList(
            new SearchResult("Projet", "Concert Stade de France", "Client: MagScene Productions - Vente [NEGOCIATION]", "🎭", "40"),
            new SearchResult("Projet", "Festival Solidays", "Client: Festival Solidays - Prestation [CONFIRME]", "🎭", "41")
        ));
        
        dataLoaded = true;
    }
    
    /**
     * Utilitaire pour extraire une valeur string d'une map avec plusieurs clés possibles
     */
    private String getStringValue(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object value = map.get(key);
            if (value != null && !value.toString().isEmpty()) {
                return value.toString();
            }
        }
        return "";
    }
    
    /**
     * Recherche dynamique avec suggestions
     * Priorise les correspondances sur le code LOCMAT
     */
    public ObservableList<SearchResult> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return FXCollections.observableArrayList();
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        List<SearchResult> results = new ArrayList<>();
        
        // Debug: afficher l'état de l'index
        System.out.println("🔎 Recherche: '" + normalizedQuery + "' dans " + searchIndex.size() + " catégories");
        
        // Parcourir tous les types de données - PAS de limite ici pour permettre "Afficher plus"
        for (Map.Entry<String, List<SearchResult>> entry : searchIndex.entrySet()) {
            List<SearchResult> typeResults = entry.getValue().stream()
                .filter(item -> {
                    boolean nameMatch = item.getName().toLowerCase().contains(normalizedQuery);
                    boolean descMatch = item.getDescription().toLowerCase().contains(normalizedQuery);
                    boolean locmatMatch = item.getLocmatCode() != null && item.getLocmatCode().toLowerCase().contains(normalizedQuery);
                    return nameMatch || descMatch || locmatMatch;
                })
                .collect(Collectors.toList()); // Pas de limite ici
            
            if (!typeResults.isEmpty()) {
                System.out.println("   ✅ " + typeResults.size() + " résultats dans " + entry.getKey());
            }
            results.addAll(typeResults);
        }
        
        System.out.println("🔎 Total résultats: " + results.size());
        
        // Trier par pertinence - PRIORISER LES CORRESPONDANCES LOCMAT
        results.sort((a, b) -> {
            // 1. PRIORITÉ MAXIMALE: Correspondance exacte sur le code LOCMAT
            boolean aLocmatExact = a.getLocmatCode() != null && a.getLocmatCode().equalsIgnoreCase(normalizedQuery);
            boolean bLocmatExact = b.getLocmatCode() != null && b.getLocmatCode().equalsIgnoreCase(normalizedQuery);
            if (aLocmatExact && !bLocmatExact) return -1;
            if (!aLocmatExact && bLocmatExact) return 1;
            
            // 2. HAUTE PRIORITÉ: Le code LOCMAT commence par la recherche
            boolean aLocmatStartsWith = a.getLocmatCode() != null && a.getLocmatCode().toLowerCase().startsWith(normalizedQuery);
            boolean bLocmatStartsWith = b.getLocmatCode() != null && b.getLocmatCode().toLowerCase().startsWith(normalizedQuery);
            if (aLocmatStartsWith && !bLocmatStartsWith) return -1;
            if (!aLocmatStartsWith && bLocmatStartsWith) return 1;
            
            // 3. MOYENNE PRIORITÉ: Le code LOCMAT contient la recherche
            boolean aLocmatContains = a.getLocmatCode() != null && a.getLocmatCode().toLowerCase().contains(normalizedQuery);
            boolean bLocmatContains = b.getLocmatCode() != null && b.getLocmatCode().toLowerCase().contains(normalizedQuery);
            if (aLocmatContains && !bLocmatContains) return -1;
            if (!aLocmatContains && bLocmatContains) return 1;
            
            // 4. Correspondance exacte sur le nom
            boolean aNameStartsWith = a.getName().toLowerCase().startsWith(normalizedQuery);
            boolean bNameStartsWith = b.getName().toLowerCase().startsWith(normalizedQuery);
            if (aNameStartsWith && !bNameStartsWith) return -1;
            if (!aNameStartsWith && bNameStartsWith) return 1;
            
            // 5. Puis par type
            return a.getType().compareTo(b.getType());
        });
        
        // PAS de limite globale - la limite sera appliquée côté affichage
        return FXCollections.observableArrayList(results);
    }
    
    /**
     * Rafraîchit les données depuis l'API
     */
    public void refresh() {
        if (apiService != null) {
            loadAllRealData();
        }
    }
    
    /**
     * Définit l'ApiService et charge les données
     */
    public void setApiService(ApiService apiService) {
        this.apiService = apiService;
        loadAllRealData();
    }
    
    /**
     * Indique si les données ont été chargées
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }
    
    /**
     * Obtient le nombre total de résultats dans l'index
     */
    public int getTotalIndexedItems() {
        return searchIndex.values().stream()
                .mapToInt(List::size)
                .sum();
    }
    
    /**
     * Classe représentant un résultat de recherche
     */
    public static class SearchResult {
        private final String type;
        private final String name;
        private final String description;
        private final String icon;
        private final String id;
        private final String locmatCode;
        
        public SearchResult(String type, String name, String description, String icon) {
            this(type, name, description, icon, null, null);
        }
        
        public SearchResult(String type, String name, String description, String icon, String id) {
            this(type, name, description, icon, id, null);
        }
        
        public SearchResult(String type, String name, String description, String icon, String id, String locmatCode) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.id = id;
            this.locmatCode = locmatCode;
        }
        
        // Getters
        public String getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public String getId() { return id; }
        public String getLocmatCode() { return locmatCode; }
        
        @Override
        public String toString() {
            return icon + " " + name + " (" + type + ")";
        }
    }
}
