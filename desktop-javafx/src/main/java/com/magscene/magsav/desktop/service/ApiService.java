package com.magscene.magsav.desktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;
/**
 * Service pour communication avec l'API REST Backend MAGSAV-3.0
 * Utilise Apache HttpClient 5 pour les appels REST asynchrones
 */
public class ApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiService.class);
    private static final String BASE_URL = "http://localhost:8080/api";
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public ApiService() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Support Java 8 Time API
    }
    
    /**
     * Test de connectivitÃƒÂ© avec le backend
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/health");
                return httpClient.execute(request, response -> {
                    logger.info("Backend health check: {}", response.getCode());
                    return response.getCode() == 200;
                });
            } catch (Exception e) {
                logger.warn("Backend non disponible: {}", e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ©ration des ÃƒÂ©quipements
     */
    public CompletableFuture<List<Object>> getEquipments() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/equipment");
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration ÃƒÂ©quipements: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * RÃƒÂ©cupÃƒÂ©ration des demandes SAV
     */
    public CompletableFuture<List<Object>> getServiceRequests() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/service-requests");
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration demandes SAV: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * CrÃƒÂ©ation d'une nouvelle demande SAV
     */
    public CompletableFuture<Object> createServiceRequest(Object serviceRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = new HttpPost(BASE_URL + "/service-requests");
                String jsonBody = objectMapper.writeValueAsString(serviceRequest);
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, Object.class);
                });
            } catch (IOException e) {
                logger.error("Erreur crÃƒÂ©ation demande SAV: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Mise ÃƒÂ  jour d'une demande SAV
     */
    public CompletableFuture<Object> updateServiceRequest(Long id, Object serviceRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(BASE_URL + "/service-requests/" + id);
                String jsonBody = objectMapper.writeValueAsString(serviceRequest);
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, Object.class);
                });
            } catch (IOException e) {
                logger.error("Erreur modification demande SAV: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    /**
     * Suppression d'une demande SAV
     */
    public CompletableFuture<Boolean> deleteServiceRequest(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpDelete request = new HttpDelete(BASE_URL + "/service-requests/" + id);
                
                return httpClient.execute(request, response -> {
                    logger.info("Suppression demande SAV {}: {}", id, response.getCode());
                    return response.getCode() >= 200 && response.getCode() < 300;
                });
            } catch (IOException e) {
                logger.error("Erreur suppression demande SAV: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration du personnel actif
     */
    public CompletableFuture<List<Object>> getActivePersonnel() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/personnel/active");
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration personnel actif: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration des catÃƒÂ©gories
     */
    public CompletableFuture<List<Map<String, Object>>> getCategories() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/categories");
                CloseableHttpResponse response = httpClient.execute(request);
                String jsonResponse = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() == 200) {
                    TypeReference<List<Map<String, Object>>> typeRef = new TypeReference<List<Map<String, Object>>>() {};
                    List<Map<String, Object>> categories = objectMapper.readValue(jsonResponse, typeRef);
                    logger.info("Categories loaded: {}", categories.size());
                    return categories;
                } else {
                    logger.error("Failed to fetch categories: {}", response.getCode());
                    return List.of();
                }
            } catch (Exception e) {
                logger.error("Error fetching categories", e);
                return List.of();
            }
        });
    }
    
    /**
     * CrÃƒÂ©ation d'un nouvel ÃƒÂ©quipement
     */
    public CompletableFuture<Map<String, Object>> createEquipment(Map<String, Object> equipmentData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = new HttpPost(BASE_URL + "/equipment");
                String jsonData = objectMapper.writeValueAsString(equipmentData);
                request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                
                CloseableHttpResponse response = httpClient.execute(request);
                String jsonResponse = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() == 200 || response.getCode() == 201) {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                    Map<String, Object> result = objectMapper.readValue(jsonResponse, typeRef);
                    logger.info("Equipment created with ID: {}", result.get("id"));
                    return result;
                } else {
                    logger.error("Failed to create equipment: {}", response.getCode());
                    return Map.of("error", "Failed to create equipment");
                }
            } catch (Exception e) {
                logger.error("Error creating equipment", e);
                return Map.of("error", e.getMessage());
            }
        });
    }
    
    /**
     * Mise ÃƒÂ  jour d'un ÃƒÂ©quipement
     */
    public CompletableFuture<Map<String, Object>> updateEquipment(Long id, Map<String, Object> equipmentData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(BASE_URL + "/equipment/" + id);
                String jsonData = objectMapper.writeValueAsString(equipmentData);
                request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                
                CloseableHttpResponse response = httpClient.execute(request);
                String jsonResponse = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() == 200) {
                    TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {};
                    Map<String, Object> result = objectMapper.readValue(jsonResponse, typeRef);
                    logger.info("Equipment updated with ID: {}", result.get("id"));
                    return result;
                } else {
                    logger.error("Failed to update equipment: {}", response.getCode());
                    return Map.of("error", "Failed to update equipment");
                }
            } catch (Exception e) {
                logger.error("Error updating equipment", e);
                return Map.of("error", e.getMessage());
            }
        });
    }
    
    /**
     * Suppression d'un ÃƒÂ©quipement
     */
    public CompletableFuture<Boolean> deleteEquipment(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpDelete request = new HttpDelete(BASE_URL + "/equipment/" + id);
                CloseableHttpResponse response = httpClient.execute(request);
                
                boolean success = (response.getCode() == 200 || response.getCode() == 204);
                if (success) {
                    logger.info("Equipment deleted with ID: {}", id);
                } else {
                    logger.error("Failed to delete equipment: {}", response.getCode());
                }
                return success;
            } catch (Exception e) {
                logger.error("Error deleting equipment", e);
                return false;
            }
        });
    }

    // ================== MÃƒâ€°THODES PERSONNEL ==================

    /**
     * RÃƒÂ©cupÃƒÂ©ration de tout le personnel
     */
    public CompletableFuture<List<Object>> getAllPersonnel() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/personnel");
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration personnel: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * CrÃƒÂ©ation d'un nouveau membre du personnel
     */
    public CompletableFuture<Map<String, Object>> createPersonnel(Map<String, Object> personnelData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost request = new HttpPost(BASE_URL + "/personnel");
                String jsonData = objectMapper.writeValueAsString(personnelData);
                request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    if (response.getCode() == 200) {
                        return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                    } else {
                        throw new RuntimeException("Erreur crÃƒÂ©ation personnel: " + jsonResponse);
                    }
                });
            } catch (IOException e) {
                logger.error("Erreur crÃƒÂ©ation personnel: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Mise ÃƒÂ  jour d'un membre du personnel
     */
    public CompletableFuture<Map<String, Object>> updatePersonnel(Long personnelId, Map<String, Object> personnelData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPut request = new HttpPut(BASE_URL + "/personnel/" + personnelId);
                String jsonData = objectMapper.writeValueAsString(personnelData);
                request.setEntity(new StringEntity(jsonData, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    if (response.getCode() == 200) {
                        return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                    } else {
                        throw new RuntimeException("Erreur mise ÃƒÂ  jour personnel: " + jsonResponse);
                    }
                });
            } catch (IOException e) {
                logger.error("Erreur mise ÃƒÂ  jour personnel: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Suppression d'un membre du personnel
     */
    public CompletableFuture<Void> deletePersonnel(Long personnelId) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpDelete request = new HttpDelete(BASE_URL + "/personnel/" + personnelId);
                
                httpClient.execute(request, response -> {
                    if (response.getCode() != 200) {
                        throw new RuntimeException("Erreur suppression personnel: " + response.getCode());
                    }
                    return null;
                });
            } catch (IOException e) {
                logger.error("Erreur suppression personnel: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Recherche de personnel par nom
     */
    public CompletableFuture<List<Object>> searchPersonnel(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                HttpGet request = new HttpGet(BASE_URL + "/personnel/search?query=" + encodedQuery);
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur recherche personnel: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    // ==================== VEHICLES API ====================
    
    /**
     * RÃƒÂ©cupÃƒÂ©ration de tous les vÃƒÂ©hicules
     */
    public CompletableFuture<List<Object>> getAllVehicles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/vehicles");
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration vÃƒÂ©hicules: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration d'un vÃƒÂ©hicule par ID
     */
    public CompletableFuture<Map<String, Object>> getVehicleById(Long vehicleId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/vehicles/" + vehicleId);
                
                return httpClient.execute(request, response -> {
                    if (response.getCode() == 404) {
                        return null;
                    }
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration vÃƒÂ©hicule: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * CrÃƒÂ©ation d'un nouveau vÃƒÂ©hicule
     */
    public CompletableFuture<Map<String, Object>> createVehicle(Map<String, Object> vehicleData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(vehicleData);
                
                HttpPost request = new HttpPost(BASE_URL + "/vehicles");
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    if (response.getCode() != 201) {
                        throw new RuntimeException("Erreur crÃƒÂ©ation vÃƒÂ©hicule: " + response.getCode());
                    }
                    return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur crÃƒÂ©ation vÃƒÂ©hicule: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Mise ÃƒÂ  jour d'un vÃƒÂ©hicule
     */
    public CompletableFuture<Map<String, Object>> updateVehicle(Long vehicleId, Map<String, Object> vehicleData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = objectMapper.writeValueAsString(vehicleData);
                
                HttpPut request = new HttpPut(BASE_URL + "/vehicles/" + vehicleId);
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    if (response.getCode() != 200) {
                        throw new RuntimeException("Erreur mise ÃƒÂ  jour vÃƒÂ©hicule: " + response.getCode());
                    }
                    return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur mise ÃƒÂ  jour vÃƒÂ©hicule: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Suppression d'un vÃƒÂ©hicule
     */
    public CompletableFuture<Void> deleteVehicle(Long vehicleId) {
        return CompletableFuture.runAsync(() -> {
            try {
                HttpDelete request = new HttpDelete(BASE_URL + "/vehicles/" + vehicleId);
                
                httpClient.execute(request, response -> {
                    if (response.getCode() != 204) {
                        throw new RuntimeException("Erreur suppression vÃƒÂ©hicule: " + response.getCode());
                    }
                    return null;
                });
            } catch (IOException e) {
                logger.error("Erreur suppression vÃƒÂ©hicule: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Recherche de vÃƒÂ©hicules
     */
    public CompletableFuture<List<Object>> searchVehicles(String query) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
                HttpGet request = new HttpGet(BASE_URL + "/vehicles/search?query=" + encodedQuery);
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur recherche vÃƒÂ©hicules: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration des vÃƒÂ©hicules disponibles
     */
    public CompletableFuture<List<Object>> getAvailableVehicles() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/vehicles/available");
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration vÃƒÂ©hicules disponibles: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration des vÃƒÂ©hicules nÃƒÂ©cessitant une maintenance
     */
    public CompletableFuture<List<Object>> getVehiclesNeedingMaintenance() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/vehicles/maintenance-needed");
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<List<Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration vÃƒÂ©hicules maintenance: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration des statistiques vÃƒÂ©hicules
     */
    public CompletableFuture<Map<String, Object>> getVehicleStatistics() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpGet request = new HttpGet(BASE_URL + "/vehicles/statistics");
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur rÃƒÂ©cupÃƒÂ©ration statistiques vÃƒÂ©hicules: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Mise ÃƒÂ  jour du statut d'un vÃƒÂ©hicule
     */
    public CompletableFuture<Map<String, Object>> updateVehicleStatus(Long vehicleId, String status) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> payload = Map.of("status", status);
                String jsonBody = objectMapper.writeValueAsString(payload);
                
                HttpPut request = new HttpPut(BASE_URL + "/vehicles/" + vehicleId + "/status");
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    if (response.getCode() != 200) {
                        throw new RuntimeException("Erreur mise ÃƒÂ  jour statut vÃƒÂ©hicule: " + response.getCode());
                    }
                    return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur mise ÃƒÂ  jour statut vÃƒÂ©hicule: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Mise ÃƒÂ  jour du kilomÃƒÂ©trage d'un vÃƒÂ©hicule
     */
    public CompletableFuture<Map<String, Object>> updateVehicleMileage(Long vehicleId, Integer mileage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, Integer> payload = Map.of("mileage", mileage);
                String jsonBody = objectMapper.writeValueAsString(payload);
                
                HttpPut request = new HttpPut(BASE_URL + "/vehicles/" + vehicleId + "/mileage");
                request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
                
                return httpClient.execute(request, response -> {
                    String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
                    if (response.getCode() != 200) {
                        throw new RuntimeException("Erreur mise ÃƒÂ  jour kilomÃƒÂ©trage vÃƒÂ©hicule: " + response.getCode());
                    }
                    return objectMapper.readValue(jsonResponse, new TypeReference<Map<String, Object>>() {});
                });
            } catch (IOException e) {
                logger.error("Erreur mise ÃƒÂ  jour kilomÃƒÂ©trage vÃƒÂ©hicule: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    // ================== MÃƒâ€°THODES CLIENTS ==================

    /**
     * RÃƒÂ©cupÃƒÂ©ration de tous les clients
     */
    public List<com.magscene.magsav.desktop.model.Client> getAllClients() throws Exception {
        List<Map<String, Object>> rawClients = getAll("clients");
        return rawClients.stream()
            .map(this::mapToClient)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration d'un client par ID
     */
    public com.magscene.magsav.desktop.model.Client getClientById(Long id) throws Exception {
        Map<String, Object> rawClient = getById("clients", id);
        return mapToClient(rawClient);
    }

    /**
     * CrÃƒÂ©ation d'un nouveau client
     */
    public com.magscene.magsav.desktop.model.Client createClient(com.magscene.magsav.desktop.model.Client client) throws Exception {
        Map<String, Object> clientData = mapFromClient(client);
        Map<String, Object> result = create("clients", clientData);
        return mapToClient(result);
    }

    /**
     * Mise ÃƒÂ  jour d'un client
     */
    public com.magscene.magsav.desktop.model.Client updateClient(com.magscene.magsav.desktop.model.Client client) throws Exception {
        Map<String, Object> clientData = mapFromClient(client);
        Map<String, Object> result = update("clients", client.getId(), clientData);
        return mapToClient(result);
    }

    /**
     * Suppression d'un client
     */
    public void deleteClient(Long id) throws Exception {
        delete("clients", id);
    }

    /**
     * Conversion d'une Map en objet Client
     */
    private com.magscene.magsav.desktop.model.Client mapToClient(Map<String, Object> data) {
        com.magscene.magsav.desktop.model.Client client = new com.magscene.magsav.desktop.model.Client();
        
        if (data.get("id") != null) {
            client.setId(((Number) data.get("id")).longValue());
        }
        client.setCompanyName((String) data.get("companyName"));
        client.setEmail((String) data.get("email"));
        client.setPhone((String) data.get("phone"));
        client.setAddress((String) data.get("address"));
        client.setNotes((String) data.get("notes"));
        
        // Conversion des enums
        if (data.get("type") != null) {
            String typeStr = (String) data.get("type");
            try {
                client.setType(com.magscene.magsav.desktop.model.Client.ClientType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                client.setType(com.magscene.magsav.desktop.model.Client.ClientType.CORPORATE);
            }
        }
        
        if (data.get("status") != null) {
            String statusStr = (String) data.get("status");
            try {
                client.setStatus(com.magscene.magsav.desktop.model.Client.ClientStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                client.setStatus(com.magscene.magsav.desktop.model.Client.ClientStatus.ACTIVE);
            }
        }
        
        if (data.get("category") != null) {
            String categoryStr = (String) data.get("category");
            try {
                client.setCategory(com.magscene.magsav.desktop.model.Client.ClientCategory.valueOf(categoryStr));
            } catch (IllegalArgumentException e) {
                client.setCategory(com.magscene.magsav.desktop.model.Client.ClientCategory.STANDARD);
            }
        }
        
        // Conversion des montants
        if (data.get("outstandingAmount") != null) {
            Object amount = data.get("outstandingAmount");
            if (amount instanceof Number) {
                client.setOutstandingAmount(new java.math.BigDecimal(amount.toString()));
            }
        }
        
        return client;
    }

    /**
     * Conversion d'un objet Client en Map
     */
    private Map<String, Object> mapFromClient(com.magscene.magsav.desktop.model.Client client) {
        Map<String, Object> data = new HashMap<>();
        
        if (client.getId() != null) {
            data.put("id", client.getId());
        }
        data.put("companyName", client.getCompanyName());
        data.put("email", client.getEmail());
        data.put("phone", client.getPhone());
        data.put("address", client.getAddress());
        data.put("notes", client.getNotes());
        
        if (client.getType() != null) {
            data.put("type", client.getType().name());
        }
        if (client.getStatus() != null) {
            data.put("status", client.getStatus().name());
        }
        if (client.getCategory() != null) {
            data.put("category", client.getCategory().name());
        }
        if (client.getOutstandingAmount() != null) {
            data.put("outstandingAmount", client.getOutstandingAmount().doubleValue());
        }
        
        return data;
    }
    
    // ================== MÃƒâ€°THODES CONTRACTS ==================

    /**
     * RÃƒÂ©cupÃƒÂ©ration de tous les contrats
     */
    public List<com.magscene.magsav.desktop.model.Contract> getAllContracts() throws Exception {
        List<Map<String, Object>> rawContracts = getAll("contracts");
        return rawContracts.stream()
            .map(this::mapToContract)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * RÃƒÂ©cupÃƒÂ©ration d'un contrat par ID
     */
    public com.magscene.magsav.desktop.model.Contract getContractById(Long id) throws Exception {
        Map<String, Object> rawContract = getById("contracts", id);
        return mapToContract(rawContract);
    }

    /**
     * CrÃƒÂ©ation d'un nouveau contrat
     */
    public com.magscene.magsav.desktop.model.Contract createContract(com.magscene.magsav.desktop.model.Contract contract) throws Exception {
        Map<String, Object> contractData = mapFromContract(contract);
        Map<String, Object> result = create("contracts", contractData);
        return mapToContract(result);
    }

    /**
     * Mise ÃƒÂ  jour d'un contrat
     */
    public com.magscene.magsav.desktop.model.Contract updateContract(com.magscene.magsav.desktop.model.Contract contract) throws Exception {
        Map<String, Object> contractData = mapFromContract(contract);
        Map<String, Object> result = update("contracts", contract.getId(), contractData);
        return mapToContract(result);
    }

    /**
     * Suppression d'un contrat
     */
    public void deleteContract(Long id) throws Exception {
        delete("contracts", id);
    }

    /**
     * Recherche de contrats par client
     */
    public List<com.magscene.magsav.desktop.model.Contract> getContractsByClient(Long clientId) throws Exception {
        Map<String, String> params = Map.of("clientId", clientId.toString());
        List<Map<String, Object>> rawContracts = search("contracts/search", params);
        return rawContracts.stream()
            .map(this::mapToContract)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Conversion d'une Map en objet Contract
     */
    private com.magscene.magsav.desktop.model.Contract mapToContract(Map<String, Object> data) {
        com.magscene.magsav.desktop.model.Contract contract = new com.magscene.magsav.desktop.model.Contract();
        
        if (data.get("id") != null) {
            contract.setId(((Number) data.get("id")).longValue());
        }
        contract.setContractNumber((String) data.get("contractNumber"));
        contract.setTitle((String) data.get("title"));
        contract.setDescription((String) data.get("description"));
        contract.setTermsAndConditions((String) data.get("termsAndConditions"));
        contract.setNotes((String) data.get("notes"));
        contract.setClientSignatory((String) data.get("clientSignatory"));
        contract.setMagsceneSignatory((String) data.get("magsceneSignatory"));
        contract.setContractFilePath((String) data.get("contractFilePath"));
        
        // Conversion des enums
        if (data.get("type") != null) {
            String typeStr = (String) data.get("type");
            try {
                contract.setType(com.magscene.magsav.desktop.model.Contract.ContractType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                contract.setType(com.magscene.magsav.desktop.model.Contract.ContractType.SERVICE);
            }
        }
        
        if (data.get("status") != null) {
            String statusStr = (String) data.get("status");
            try {
                contract.setStatus(com.magscene.magsav.desktop.model.Contract.ContractStatus.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                contract.setStatus(com.magscene.magsav.desktop.model.Contract.ContractStatus.DRAFT);
            }
        }
        
        if (data.get("billingFrequency") != null) {
            String billingStr = (String) data.get("billingFrequency");
            try {
                contract.setBillingFrequency(com.magscene.magsav.desktop.model.Contract.BillingFrequency.valueOf(billingStr));
            } catch (IllegalArgumentException e) {
                contract.setBillingFrequency(com.magscene.magsav.desktop.model.Contract.BillingFrequency.MONTHLY);
            }
        }
        
        if (data.get("paymentTerms") != null) {
            String paymentStr = (String) data.get("paymentTerms");
            try {
                contract.setPaymentTerms(com.magscene.magsav.desktop.model.Contract.PaymentTerms.valueOf(paymentStr));
            } catch (IllegalArgumentException e) {
                contract.setPaymentTerms(com.magscene.magsav.desktop.model.Contract.PaymentTerms.NET_30);
            }
        }
        
        // Conversion des dates
        if (data.get("startDate") != null) {
            String dateStr = (String) data.get("startDate");
            try {
                contract.setStartDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignorer l'erreur de parsing
            }
        }
        
        if (data.get("endDate") != null) {
            String dateStr = (String) data.get("endDate");
            try {
                contract.setEndDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignorer l'erreur de parsing
            }
        }
        
        if (data.get("signatureDate") != null) {
            String dateStr = (String) data.get("signatureDate");
            try {
                contract.setSignatureDate(java.time.LocalDate.parse(dateStr));
            } catch (Exception e) {
                // Ignorer l'erreur de parsing
            }
        }
        
        // Conversion des montants
        if (data.get("totalAmount") != null) {
            Object amount = data.get("totalAmount");
            if (amount instanceof Number) {
                contract.setTotalAmount(new java.math.BigDecimal(amount.toString()));
            }
        }
        
        if (data.get("monthlyAmount") != null) {
            Object amount = data.get("monthlyAmount");
            if (amount instanceof Number) {
                contract.setMonthlyAmount(new java.math.BigDecimal(amount.toString()));
            }
        }
        
        if (data.get("invoicedAmount") != null) {
            Object amount = data.get("invoicedAmount");
            if (amount instanceof Number) {
                contract.setInvoicedAmount(new java.math.BigDecimal(amount.toString()));
            }
        }
        
        if (data.get("remainingAmount") != null) {
            Object amount = data.get("remainingAmount");
            if (amount instanceof Number) {
                contract.setRemainingAmount(new java.math.BigDecimal(amount.toString()));
            }
        }
        
        // Autres propriÃƒÂ©tÃƒÂ©s
        if (data.get("isAutoRenewable") != null) {
            contract.setIsAutoRenewable((Boolean) data.get("isAutoRenewable"));
        }
        
        if (data.get("renewalPeriodMonths") != null) {
            Object value = data.get("renewalPeriodMonths");
            if (value instanceof Number) {
                contract.setRenewalPeriodMonths(((Number) value).intValue());
            }
        }
        
        if (data.get("noticePeriodDays") != null) {
            Object value = data.get("noticePeriodDays");
            if (value instanceof Number) {
                contract.setNoticePeriodDays(((Number) value).intValue());
            }
        }
        
        // Relations
        if (data.get("clientId") != null) {
            Object clientId = data.get("clientId");
            if (clientId instanceof Number) {
                contract.setClientId(((Number) clientId).longValue());
            }
        }
        
        contract.setClientName((String) data.get("clientName"));
        
        if (data.get("contactId") != null) {
            Object contactId = data.get("contactId");
            if (contactId instanceof Number) {
                contract.setContactId(((Number) contactId).longValue());
            }
        }
        
        contract.setContactName((String) data.get("contactName"));
        
        return contract;
    }

    /**
     * Conversion d'un objet Contract en Map
     */
    private Map<String, Object> mapFromContract(com.magscene.magsav.desktop.model.Contract contract) {
        Map<String, Object> data = new HashMap<>();
        
        if (contract.getId() != null) {
            data.put("id", contract.getId());
        }
        data.put("contractNumber", contract.getContractNumber());
        data.put("title", contract.getTitle());
        data.put("description", contract.getDescription());
        data.put("termsAndConditions", contract.getTermsAndConditions());
        data.put("notes", contract.getNotes());
        data.put("clientSignatory", contract.getClientSignatory());
        data.put("magsceneSignatory", contract.getMagsceneSignatory());
        data.put("contractFilePath", contract.getContractFilePath());
        
        if (contract.getType() != null) {
            data.put("type", contract.getType().name());
        }
        if (contract.getStatus() != null) {
            data.put("status", contract.getStatus().name());
        }
        if (contract.getBillingFrequency() != null) {
            data.put("billingFrequency", contract.getBillingFrequency().name());
        }
        if (contract.getPaymentTerms() != null) {
            data.put("paymentTerms", contract.getPaymentTerms().name());
        }
        
        // Dates
        if (contract.getStartDate() != null) {
            data.put("startDate", contract.getStartDate().toString());
        }
        if (contract.getEndDate() != null) {
            data.put("endDate", contract.getEndDate().toString());
        }
        if (contract.getSignatureDate() != null) {
            data.put("signatureDate", contract.getSignatureDate().toString());
        }
        
        // Montants
        if (contract.getTotalAmount() != null) {
            data.put("totalAmount", contract.getTotalAmount().doubleValue());
        }
        if (contract.getMonthlyAmount() != null) {
            data.put("monthlyAmount", contract.getMonthlyAmount().doubleValue());
        }
        if (contract.getInvoicedAmount() != null) {
            data.put("invoicedAmount", contract.getInvoicedAmount().doubleValue());
        }
        if (contract.getRemainingAmount() != null) {
            data.put("remainingAmount", contract.getRemainingAmount().doubleValue());
        }
        
        // Autres propriÃƒÂ©tÃƒÂ©s
        if (contract.getIsAutoRenewable() != null) {
            data.put("isAutoRenewable", contract.getIsAutoRenewable());
        }
        if (contract.getRenewalPeriodMonths() != null) {
            data.put("renewalPeriodMonths", contract.getRenewalPeriodMonths());
        }
        if (contract.getNoticePeriodDays() != null) {
            data.put("noticePeriodDays", contract.getNoticePeriodDays());
        }
        
        // Relations
        if (contract.getClientId() != null) {
            data.put("clientId", contract.getClientId());
        }
        if (contract.getContactId() != null) {
            data.put("contactId", contract.getContactId());
        }
        
        return data;
    }

    /**
     * MÃƒÂ©thodes gÃƒÂ©nÃƒÂ©riques pour tous les endpoints REST
     */    /**
     * GET - RÃƒÂ©cupÃƒÂ¨re tous les ÃƒÂ©lÃƒÂ©ments d'un endpoint
     */
    public List<Map<String, Object>> getAll(String endpoint) throws Exception {
        HttpGet request = new HttpGet(BASE_URL + "/" + endpoint);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getCode() == 200) {
                return objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                throw new Exception("Erreur HTTP: " + response.getCode() + " - " + responseBody);
            }
        }
    }

    /**
     * GET - RÃƒÂ©cupÃƒÂ¨re un ÃƒÂ©lÃƒÂ©ment par ID
     */
    public Map<String, Object> getById(String endpoint, Long id) throws Exception {
        HttpGet request = new HttpGet(BASE_URL + "/" + endpoint + "/" + id);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getCode() == 200) {
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            } else {
                throw new Exception("Erreur HTTP: " + response.getCode() + " - " + responseBody);
            }
        }
    }

    /**
     * POST - CrÃƒÂ©e un nouvel ÃƒÂ©lÃƒÂ©ment
     */
    public Map<String, Object> create(String endpoint, Map<String, Object> data) throws Exception {
        HttpPost request = new HttpPost(BASE_URL + "/" + endpoint);
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(data), ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getCode() == 201 || response.getCode() == 200) {
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            } else {
                throw new Exception("Erreur HTTP: " + response.getCode() + " - " + responseBody);
            }
        }
    }

    /**
     * PUT - Met ÃƒÂ  jour un ÃƒÂ©lÃƒÂ©ment existant
     */
    public Map<String, Object> update(String endpoint, Long id, Map<String, Object> data) throws Exception {
        HttpPut request = new HttpPut(BASE_URL + "/" + endpoint + "/" + id);
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(data), ContentType.APPLICATION_JSON));
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getCode() == 200) {
                return objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            } else {
                throw new Exception("Erreur HTTP: " + response.getCode() + " - " + responseBody);
            }
        }
    }

    /**
     * DELETE - Supprime un ÃƒÂ©lÃƒÂ©ment
     */
    public void delete(String endpoint, Long id) throws Exception {
        HttpDelete request = new HttpDelete(BASE_URL + "/" + endpoint + "/" + id);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            if (response.getCode() != 204 && response.getCode() != 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                throw new Exception("Erreur HTTP: " + response.getCode() + " - " + responseBody);
            }
        }
    }

    /**
     * GET avec paramÃƒÂ¨tres - Recherche avec paramÃƒÂ¨tres de requÃƒÂªte
     */
    public List<Map<String, Object>> search(String endpoint, Map<String, String> params) throws Exception {
        StringBuilder url = new StringBuilder(BASE_URL + "/" + endpoint);
        
        if (params != null && !params.isEmpty()) {
            url.append("?");
            params.forEach((key, value) -> url.append(key).append("=").append(value).append("&"));
            // Retirer le dernier &
            url.setLength(url.length() - 1);
        }
        
        HttpGet request = new HttpGet(url.toString());
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            
            if (response.getCode() == 200) {
                return objectMapper.readValue(responseBody, new TypeReference<List<Map<String, Object>>>() {});
            } else {
                throw new Exception("Erreur HTTP: " + response.getCode() + " - " + responseBody);
            }
        }
    }

    /**
     * Fermeture des ressources
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            logger.error("Erreur fermeture HttpClient: {}", e.getMessage());
        }
    }
}

