package com.magscene.magsav.desktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.concurrent.Task;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;

/**
 * Service pour gérer l'import des données LOCMAT via l'API backend
 */
public class LocmatImportService {
    
    private static final String BACKEND_URL = "http://localhost:8080";
    private final ObjectMapper objectMapper;
    
    public LocmatImportService() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Créer une tâche pour importer le fichier Excel LOCMAT
     */
    public Task<ImportResult> createImportTask(File excelFile) {
        return new Task<>() {
            @Override
            protected ImportResult call() throws Exception {
                updateMessage("Préparation du fichier...");
                updateProgress(0, 100);
                
                // Configuration avec timeout étendu pour l'import Excel
                RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(10))
                        .setResponseTimeout(Timeout.ofMinutes(5)) // 5 minutes pour l'import Excel
                        .build();
                
                try (CloseableHttpClient httpClient = HttpClients.custom()
                        .setDefaultRequestConfig(requestConfig)
                        .build()) {
                    
                    // Préparer la requête multipart
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.addBinaryBody("file", excelFile, 
                            ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), 
                            excelFile.getName());
                    
                    updateMessage("Envoi du fichier au serveur...");
                    updateProgress(25, 100);
                    
                    HttpPost httpPost = new HttpPost(BACKEND_URL + "/api/locmat/import");
                    httpPost.setEntity(builder.build());
                    
                    updateMessage("Traitement de l'import en cours...");
                    updateProgress(50, 100);
                    
                    return httpClient.execute(httpPost, response -> {
                        updateProgress(75, 100);
                        
                        if (response.getCode() >= 400) {
                            throw new IOException("Erreur HTTP: " + response.getCode() + " - " + response.getReasonPhrase());
                        }
                        
                        String responseBody = EntityUtils.toString(response.getEntity());
                        JsonNode jsonResponse = objectMapper.readTree(responseBody);
                        
                        updateMessage("Finalisation...");
                        updateProgress(100, 100);
                        
                        return parseImportResult(jsonResponse);
                    });
                }
            }
        };
    }
    
    /**
     * Parser la réponse JSON en résultat d'import
     */
    private ImportResult parseImportResult(JsonNode jsonResponse) {
        ImportResult result = new ImportResult();
        
        result.success = jsonResponse.get("success").asBoolean();
        result.message = jsonResponse.get("message").asText();
        result.equipmentCount = jsonResponse.get("equipmentCount").asInt();
        result.errorCount = jsonResponse.get("errorCount").asInt();
        
        // Traiter les erreurs s'il y en a
        if (jsonResponse.has("errors") && jsonResponse.get("errors").isArray()) {
            for (JsonNode error : jsonResponse.get("errors")) {
                result.errors.add(error.asText());
            }
        }
        
        return result;
    }
    
    /**
     * Classe pour encapsuler les résultats d'import
     */
    public static class ImportResult {
        public boolean success;
        public String message;
        public int equipmentCount;
        public int errorCount;
        public java.util.List<String> errors = new java.util.ArrayList<>();
        
        public boolean hasErrors() {
            return errorCount > 0;
        }
        
        public String getDetailedMessage() {
            StringBuilder sb = new StringBuilder();
            sb.append(message);
            
            if (hasErrors()) {
                sb.append("\n\nDétail des erreurs :");
                for (int i = 0; i < Math.min(errors.size(), 10); i++) { // Limiter à 10 erreurs
                    sb.append("\n• ").append(errors.get(i));
                }
                
                if (errors.size() > 10) {
                    sb.append("\n... et ").append(errors.size() - 10).append(" autres erreurs");
                }
            }
            
            return sb.toString();
        }
    }
}