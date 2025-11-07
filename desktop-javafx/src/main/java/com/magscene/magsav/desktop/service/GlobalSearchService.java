package com.magscene.magsav.desktop.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service de recherche globale intelligente
 * Fournit des suggestions dynamiques classées par type
 */
public class GlobalSearchService {
    
    private final Map<String, List<SearchResult>> searchIndex;
    private ApiService apiService;
    
    public GlobalSearchService() {
        this.searchIndex = new HashMap<>();
        initializeSearchData();
    }
    
    /**
     * Constructeur avec ApiService pour accéder aux données réelles
     */
    public GlobalSearchService(ApiService apiService) {
        this.searchIndex = new HashMap<>();
        this.apiService = apiService;
        initializeSearchData();
        if (apiService != null) {
            loadRealProjectData();
        }
    }
    
    /**
     * Recherche dynamique avec suggestions
     */
    public ObservableList<SearchResult> search(String query) {
        if (query == null || query.trim().length() < 2) {
            return FXCollections.observableArrayList();
        }
        
        String normalizedQuery = query.toLowerCase().trim();
        List<SearchResult> results = new ArrayList<>();
        
        // Parcourir tous les types de données
        for (Map.Entry<String, List<SearchResult>> entry : searchIndex.entrySet()) {
            List<SearchResult> typeResults = entry.getValue().stream()
                .filter(item -> item.getName().toLowerCase().contains(normalizedQuery) ||
                               item.getDescription().toLowerCase().contains(normalizedQuery))
                .limit(5) // Limite à 5 résultats par type
                .collect(Collectors.toList());
            results.addAll(typeResults);
        }
        
        // Trier par pertinence et type
        results.sort((a, b) -> {
            // Prioriser les correspondances exactes en début
            boolean aStartsWith = a.getName().toLowerCase().startsWith(normalizedQuery);
            boolean bStartsWith = b.getName().toLowerCase().startsWith(normalizedQuery);
            
            if (aStartsWith && !bStartsWith) return -1;
            if (!aStartsWith && bStartsWith) return 1;
            
            // Puis par type
            return a.getType().compareTo(b.getType());
        });
        
        // Limiter le nombre total de résultats
        return FXCollections.observableArrayList(results.stream().limit(20).collect(Collectors.toList()));
    }
    
    /**
     * Initialisation des données de recherche (simulation + données réelles)
     */
    private void initializeSearchData() {
        // Clients (données de démonstration)
        List<SearchResult> clients = Arrays.asList(
            new SearchResult("Client", "Yann RIVOAL", "EURL RIVOAL - Prestataire audiovisuel", "👥"),
            new SearchResult("Client", "Yannis GROS", "SAS EVENEMENTS PLUS - Organisation d'événements", "👥"),
            new SearchResult("Client", "Yasmina RAOULT", "Mairie de Saint-Brieuc - Service culturel", "👥"),
            new SearchResult("Client", "Yacht Club Dinard", "Association nautique - Événements privés", "👥"),
            new SearchResult("Client", "Yes We Can Events", "Agence événementielle Rennes", "👥"),
            new SearchResult("Client", "Mairie de Vannes", "Collectivité territoriale", "👥"),
            new SearchResult("Client", "Festival Vieilles Charrues", "Organisation de festivals", "👥"),
            new SearchResult("Client", "Salle Olympia Paris", "Salle de spectacle", "👥")
        );
        
        // Matériel (liste mutable)
        List<SearchResult> materiel = new ArrayList<>(Arrays.asList(
            new SearchResult("Matériel", "Yamaha A15", "Enceinte active 15\" - 700W", "📦"),
            new SearchResult("Matériel", "Yamaha B218", "Caisson de basses actif - 1000W", "📦"),
            new SearchResult("Matériel", "Yamaha MG16XU", "Console de mixage 16 voies", "📦"),
            new SearchResult("Matériel", "Yamaha P7000S", "Amplificateur de puissance", "📦"),
            new SearchResult("Matériel", "Shure SM58", "Microphone dynamique cardioïde", "📦"),
            new SearchResult("Matériel", "MacBook Pro 16\"", "Ordinateur portable pour régie", "📦"),
            new SearchResult("Matériel", "iPad Air", "Tablette de contrôle à distance", "📦"),
            new SearchResult("Matériel", "Projecteur LED 300W", "Éclairage à LED haute puissance", "📦")
        ));
        
        // Fournisseurs
        List<SearchResult> fournisseurs = Arrays.asList(
            new SearchResult("Fournisseur", "Yamaha France", "Fabricant instruments et équipements audio", "🏪"),
            new SearchResult("Fournisseur", "Yellowtec", "Fabricant équipements broadcast", "🏪"),
            new SearchResult("Fournisseur", "Nexo SA", "Fabricant enceintes professionnelles", "🏪"),
            new SearchResult("Fournisseur", "L-Acoustics", "Systèmes audio professionnels", "🏪")
        );
        
        // Personnel (liste mutable)
        List<SearchResult> personnel = new ArrayList<>(Arrays.asList(
            new SearchResult("Personnel", "Yann MOIHAT", "Technicien son - Spécialiste mixage", "👤"),
            new SearchResult("Personnel", "Yaël BERNARD", "Technicienne éclairage - Programmation", "👤"),
            new SearchResult("Personnel", "Yves CADIOU", "Responsable logistique - Transport", "👤"),
            new SearchResult("Personnel", "Yvonne LEMAIRE", "Commerciale - Devis et contrats", "👤")
        ));
        
        // Interventions SAV
        List<SearchResult> interventions = Arrays.asList(
            new SearchResult("Intervention", "INT-2024-0156", "Réparation Yamaha A15 - Haut-parleur défaillant", "🔧"),
            new SearchResult("Intervention", "INT-2024-0189", "Maintenance préventive console Yamaha", "🔧"),
            new SearchResult("Intervention", "INT-2024-0203", "Formation utilisation équipement Yacht Club", "🔧")
        );
        
        // Contrats
        List<SearchResult> contrats = Arrays.asList(
            new SearchResult("Contrat", "CTR-2024-045", "Maintenance annuelle - Yann RIVOAL", "📋"),
            new SearchResult("Contrat", "CTR-2024-067", "Location matériel - Festival Vieilles Charrues", "📋"),
            new SearchResult("Contrat", "CTR-2024-089", "Prestation complète - Yasmina RAOULT", "📋")
        );
        
        // Véhicules
        List<SearchResult> vehicules = Arrays.asList(
            new SearchResult("Véhicule", "Iveco Daily", "AB-123-CD - Fourgon matériel 20m³", "🚐"),
            new SearchResult("Véhicule", "Ford Transit", "EF-456-GH - Fourgon léger 12m³", "🚐"),
            new SearchResult("Véhicule", "Renault Master", "IJ-789-KL - Fourgon aménagé régie", "🚐")
        );
        
        // Projets/Événements
        List<SearchResult> projets = Arrays.asList(
            new SearchResult("Projets", "Festival Solidays", "Festival de musique - prestation complète son + éclairage", "🎭"),
            new SearchResult("Projets", "Fête de la Musique", "Événement municipal - sonorisation places publiques", "🎭"),
            new SearchResult("Projets", "Festival Rock en Seine", "Festival rock - système principal 4 scènes", "🎭"),
            new SearchResult("Projets", "Francofolies La Rochelle", "Festival chanson française - technique complète", "🎭"),
            new SearchResult("Projets", "Foire commerciale Paris", "Salon professionnel - équipement stands", "🎭"),
            new SearchResult("Projets", "Finale Roland Garros", "Événement sportif - sonorisation cérémonie", "🎭"),
            new SearchResult("Projets", "Fashion Week Paris", "Défilé mode - éclairage scénique LED", "🎭"),
            new SearchResult("Projets", "Festival Jazz Montreux", "Festival international - régie complète", "🎭"),
            new SearchResult("Projets", "Théâtre Mogador", "Installation fixe - système son numérique", "🎭"),
            new SearchResult("Projets", "Concert Olympia", "Prestation concert - éclairage + son", "🎭")
        );
        
        // Ajout de matériel avec "T"
        List<SearchResult> materielT = Arrays.asList(
            new SearchResult("Matériel", "Truss Prolyte H30V", "Structure aluminium 290mm - 3m", "📦"),
            new SearchResult("Matériel", "Truss Global H40V", "Structure carrée 400mm - 2m", "📦"),
            new SearchResult("Matériel", "Table de mixage X32", "Console numérique Behringer 32 voies", "📦"),
            new SearchResult("Matériel", "Télécommande Yamaha", "Contrôleur sans fil DM3-D", "📦"),
            new SearchResult("Matériel", "Transformateur 63A", "Alimentation triphasée 400V", "📦")
        );
        
        // Ajout de personnel avec "T"
        List<SearchResult> personnelT = Arrays.asList(
            new SearchResult("Personnel", "Thomas MARTIN", "Ingénieur du son - Spécialiste systèmes", "👤"),
            new SearchResult("Personnel", "Thierry DUBOIS", "Technicien éclairage - Programmation MA", "👤"),
            new SearchResult("Personnel", "Théo BERNARD", "Stagiaire technique - Formation son", "👤"),
            new SearchResult("Personnel", "Tanya ROUSSEAU", "Responsable planning - Gestion équipes", "👤")
        );
        
        searchIndex.put("clients", clients);
        searchIndex.put("materiel", materiel);
        searchIndex.put("fournisseurs", fournisseurs);
        searchIndex.put("personnel", personnel);
        searchIndex.put("interventions", interventions);
        searchIndex.put("contrats", contrats);
        searchIndex.put("vehicules", vehicules);
        searchIndex.put("projets", projets);
        
        // Ajouter le matériel et personnel avec "T" aux listes existantes
        searchIndex.get("materiel").addAll(materielT);
        searchIndex.get("personnel").addAll(personnelT);
    }
    
    /**
     * Charge les données réelles des projets depuis l'API
     */
    private void loadRealProjectData() {
        try {
            if (apiService != null) {
                // Récupérer les vrais projets
                List<Map<String, Object>> realProjects = apiService.getAll("projects");
                List<SearchResult> projectResults = new ArrayList<>();
                
                for (Map<String, Object> project : realProjects) {
                    String name = project.get("name") != null ? project.get("name").toString() : "";
                    String description = project.get("clientName") != null ? 
                        "Client: " + project.get("clientName") + " - " + project.get("type") : 
                        project.get("type") != null ? project.get("type").toString() : "";
                    
                    if (!name.isEmpty()) {
                        projectResults.add(new SearchResult("Projets", name, description, "🎭"));
                    }
                }
                
                // Remplacer les données de démonstration par les vraies données
                searchIndex.put("projets", projectResults);
                System.out.println("✅ Chargé " + projectResults.size() + " projets réels dans la recherche globale");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors du chargement des projets réels: " + e.getMessage());
            // Garder les données de démonstration en cas d'erreur
        }
    }
    
    /**
     * Classe représentant un résultat de recherche
     */
    public static class SearchResult {
        private final String type;
        private final String name;
        private final String description;
        private final String icon;
        
        public SearchResult(String type, String name, String description, String icon) {
            this.type = type;
            this.name = name;
            this.description = description;
            this.icon = icon;
        }
        
        // Getters
        public String getType() { return type; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        
        @Override
        public String toString() {
            return icon + " " + name + " (" + type + ")";
        }
    }
}
