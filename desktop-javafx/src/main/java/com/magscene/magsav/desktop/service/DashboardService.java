package com.magscene.magsav.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service pour récupérer les statistiques du Dashboard depuis l'API backend
 */
public class DashboardService {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Singleton
    private static DashboardService instance;

    private DashboardService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static synchronized DashboardService getInstance() {
        if (instance == null) {
            instance = new DashboardService();
        }
        return instance;
    }

    /**
     * Récupère les statistiques globales du dashboard
     */
    public CompletableFuture<DashboardStats> getStats() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/dashboard/stats"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/json")
                        .header("Accept-Charset", "UTF-8")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    Map<String, Object> data = objectMapper.readValue(response.body(), 
                            new TypeReference<Map<String, Object>>() {});
                    return DashboardStats.fromMap(data);
                } else {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur récupération stats dashboard: " + e.getMessage());
                return DashboardStats.getDefault();
            }
        });
    }

    /**
     * Récupère les statistiques SAV par mois (6 derniers mois)
     */
    public CompletableFuture<List<MonthlyData>> getSavByMonth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/dashboard/sav-by-month"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/json")
                        .header("Accept-Charset", "UTF-8")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    List<Map<String, Object>> data = objectMapper.readValue(response.body(), 
                            new TypeReference<List<Map<String, Object>>>() {});
                    List<MonthlyData> result = new ArrayList<>();
                    for (Map<String, Object> item : data) {
                        result.add(MonthlyData.fromMap(item));
                    }
                    return result;
                } else {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur récupération SAV par mois: " + e.getMessage());
                return getDefaultSavByMonth();
            }
        });
    }

    /**
     * Récupère la répartition des équipements par catégorie
     */
    public CompletableFuture<List<CategoryData>> getEquipmentByCategory() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/dashboard/equipment-by-category"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Accept", "application/json")
                        .header("Accept-Charset", "UTF-8")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() == 200) {
                    List<Map<String, Object>> data = objectMapper.readValue(response.body(), 
                            new TypeReference<List<Map<String, Object>>>() {});
                    List<CategoryData> result = new ArrayList<>();
                    for (Map<String, Object> item : data) {
                        result.add(CategoryData.fromMap(item));
                    }
                    return result;
                } else {
                    throw new RuntimeException("HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Erreur récupération équipements par catégorie: " + e.getMessage());
                return getDefaultCategories();
            }
        });
    }

    private List<MonthlyData> getDefaultSavByMonth() {
        List<MonthlyData> defaults = new ArrayList<>();
        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        for (String month : months) {
            defaults.add(new MonthlyData(month, 0, 2024));
        }
        return defaults;
    }

    private List<CategoryData> getDefaultCategories() {
        List<CategoryData> defaults = new ArrayList<>();
        defaults.add(new CategoryData("Audio", 0));
        defaults.add(new CategoryData("Vidéo", 0));
        defaults.add(new CategoryData("Éclairage", 0));
        defaults.add(new CategoryData("Structure", 0));
        defaults.add(new CategoryData("Autres", 0));
        return defaults;
    }

    // ========== Classes internes pour les données ==========

    public static class DashboardStats {
        public long totalEquipment;
        public long availableEquipment;
        public long activeSav;
        public long totalSav;
        public long totalClients;
        public long activeClients;
        public long totalVehicles;
        public long availableVehicles;
        public long totalPersonnel;
        public long totalProjects;

        public static DashboardStats fromMap(Map<String, Object> data) {
            DashboardStats stats = new DashboardStats();
            
            // Equipment
            Map<String, Object> eq = getMapSafe(data, "equipment");
            stats.totalEquipment = getLongSafe(eq, "total");
            stats.availableEquipment = getLongSafe(eq, "available");
            
            // SAV
            Map<String, Object> sav = getMapSafe(data, "sav");
            stats.totalSav = getLongSafe(sav, "total");
            stats.activeSav = getLongSafe(sav, "active");
            
            // Clients
            Map<String, Object> clients = getMapSafe(data, "clients");
            stats.totalClients = getLongSafe(clients, "total");
            stats.activeClients = getLongSafe(clients, "active");
            
            // Vehicles
            Map<String, Object> vehicles = getMapSafe(data, "vehicles");
            stats.totalVehicles = getLongSafe(vehicles, "total");
            stats.availableVehicles = getLongSafe(vehicles, "available");
            
            // Personnel
            Map<String, Object> personnel = getMapSafe(data, "personnel");
            stats.totalPersonnel = getLongSafe(personnel, "total");
            
            // Projects
            Map<String, Object> projects = getMapSafe(data, "projects");
            stats.totalProjects = getLongSafe(projects, "total");
            
            return stats;
        }

        public static DashboardStats getDefault() {
            DashboardStats stats = new DashboardStats();
            stats.totalEquipment = 0;
            stats.activeSav = 0;
            stats.totalClients = 0;
            stats.totalVehicles = 0;
            return stats;
        }

        @SuppressWarnings("unchecked")
        private static Map<String, Object> getMapSafe(Map<String, Object> data, String key) {
            Object value = data.get(key);
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
            return new HashMap<>();
        }

        private static long getLongSafe(Map<String, Object> data, String key) {
            Object value = data.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return 0;
        }
    }

    public static class MonthlyData {
        public String month;
        public long count;
        public int year;

        public MonthlyData(String month, long count, int year) {
            this.month = month;
            this.count = count;
            this.year = year;
        }

        public static MonthlyData fromMap(Map<String, Object> data) {
            String month = data.get("month") != null ? data.get("month").toString() : "";
            long count = data.get("count") instanceof Number ? ((Number) data.get("count")).longValue() : 0;
            int year = data.get("year") instanceof Number ? ((Number) data.get("year")).intValue() : 2024;
            return new MonthlyData(month, count, year);
        }
    }

    public static class CategoryData {
        public String category;
        public long count;

        public CategoryData(String category, long count) {
            this.category = category;
            this.count = count;
        }

        public static CategoryData fromMap(Map<String, Object> data) {
            String category = data.get("category") != null ? data.get("category").toString() : "Autre";
            long count = data.get("count") instanceof Number ? ((Number) data.get("count")).longValue() : 0;
            return new CategoryData(category, count);
        }
    }
}
