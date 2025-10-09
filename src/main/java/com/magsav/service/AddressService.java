package com.magsav.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Service d'autocomplétion d'adresses françaises
 * Utilise l'API gouvernementale gratuite api-adresse.data.gouv.fr
 */
public class AddressService {
    
    private static final String API_BASE_URL = "https://api-adresse.data.gouv.fr/search/";
    private static final int MIN_CHARS = 3; // Minimum de caractères pour déclencher la recherche
    private static final int MAX_RESULTS = 8; // Nombre maximum de résultats à afficher
    
    /**
     * Modèle pour représenter une suggestion d'adresse
     */
    public static class AddressSuggestion {
        private final String fullAddress;
        private final String street;
        private final String city;
        private final String postalCode;
        private final double latitude;
        private final double longitude;
        
        public AddressSuggestion(String fullAddress, String street, String city, String postalCode, double latitude, double longitude) {
            this.fullAddress = fullAddress;
            this.street = street;
            this.city = city;
            this.postalCode = postalCode;
            this.latitude = latitude;
            this.longitude = longitude;
        }
        
        public String getFullAddress() { return fullAddress; }
        public String getStreet() { return street; }
        public String getCity() { return city; }
        public String getPostalCode() { return postalCode; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        
        @Override
        public String toString() {
            return fullAddress;
        }
    }
    
    /**
     * Recherche des suggestions d'adresse via l'API
     */
    public List<AddressSuggestion> searchAddresses(String query) {
        List<AddressSuggestion> suggestions = new ArrayList<>();
        
        if (query == null || query.trim().length() < MIN_CHARS) {
            return suggestions;
        }
        
        try {
            String encodedQuery = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
            String urlString = API_BASE_URL + "?q=" + encodedQuery + "&limit=" + MAX_RESULTS;
            
            URI uri = URI.create(urlString);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000); // 5 secondes timeout
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "MAGSAV/1.2");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Parser la réponse JSON avec des expressions régulières simples
                String jsonText = response.toString();
                parseJsonResponse(jsonText, suggestions);
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche d'adresse: " + e.getMessage());
        }
        
        return suggestions;
    }
    
    /**
     * Parse simple du JSON de l'API adresse sans dépendance externe
     */
    private void parseJsonResponse(String jsonText, List<AddressSuggestion> suggestions) {
        try {
            // Extraire les features
            Pattern featuresPattern = Pattern.compile("\"features\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
            Matcher featuresMatcher = featuresPattern.matcher(jsonText);
            
            if (featuresMatcher.find()) {
                String featuresContent = featuresMatcher.group(1);
                
                // Séparer chaque feature (objets entre {})
                Pattern featurePattern = Pattern.compile("\\{([^{}]*(?:\\{[^}]*\\}[^{}]*)*)\\}", Pattern.DOTALL);
                Matcher featureMatcher = featurePattern.matcher(featuresContent);
                
                while (featureMatcher.find() && suggestions.size() < MAX_RESULTS) {
                    String featureContent = featureMatcher.group(1);
                    
                    // Extraire les propriétés nécessaires
                    String label = extractJsonValue(featureContent, "label");
                    String street = extractJsonValue(featureContent, "name");
                    String city = extractJsonValue(featureContent, "city");
                    String postalCode = extractJsonValue(featureContent, "postcode");
                    
                    // Extraire les coordonnées (plus complexe)
                    Pattern coordPattern = Pattern.compile("\"coordinates\"\\s*:\\s*\\[\\s*([-\\d\\.]+)\\s*,\\s*([-\\d\\.]+)\\s*\\]");
                    Matcher coordMatcher = coordPattern.matcher(featureContent);
                    
                    double longitude = 0.0, latitude = 0.0;
                    if (coordMatcher.find()) {
                        longitude = Double.parseDouble(coordMatcher.group(1));
                        latitude = Double.parseDouble(coordMatcher.group(2));
                    }
                    
                    if (label != null && !label.isEmpty()) {
                        suggestions.add(new AddressSuggestion(
                            label, 
                            street != null ? street : "", 
                            city != null ? city : "", 
                            postalCode != null ? postalCode : "", 
                            latitude, 
                            longitude
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du parsing JSON: " + e.getMessage());
        }
    }
    
    /**
     * Extrait une valeur JSON simple
     */
    private String extractJsonValue(String jsonContent, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"");
        Matcher matcher = pattern.matcher(jsonContent);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    /**
     * Ajoute l'autocomplétion à un TextField
     */
    public void setupAddressAutocomplete(TextField textField) {
        if (textField == null) return;
        
        ContextMenu contextMenu = new ContextMenu();
        
        // Écouter les changements de texte
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() >= MIN_CHARS) {
                // Recherche asynchrone
                Task<List<AddressSuggestion>> searchTask = new Task<List<AddressSuggestion>>() {
                    @Override
                    protected List<AddressSuggestion> call() throws Exception {
                        return searchAddresses(newValue);
                    }
                    
                    @Override
                    protected void succeeded() {
                        Platform.runLater(() -> {
                            List<AddressSuggestion> suggestions = getValue();
                            updateContextMenu(contextMenu, suggestions, textField);
                        });
                    }
                    
                    @Override
                    protected void failed() {
                        Platform.runLater(() -> {
                            contextMenu.hide();
                        });
                    }
                };
                
                Thread searchThread = new Thread(searchTask);
                searchThread.setDaemon(true);
                searchThread.start();
            } else {
                contextMenu.hide();
            }
        });
        
        // Gérer les touches
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                contextMenu.hide();
            }
        });
        
        // Cacher le menu quand le TextField perd le focus
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                contextMenu.hide();
            }
        });
    }
    
    /**
     * Met à jour le menu contextuel avec les suggestions
     */
    private void updateContextMenu(ContextMenu contextMenu, List<AddressSuggestion> suggestions, TextField textField) {
        contextMenu.getItems().clear();
        
        if (suggestions.isEmpty()) {
            contextMenu.hide();
            return;
        }
        
        for (AddressSuggestion suggestion : suggestions) {
            MenuItem item = new MenuItem(suggestion.getFullAddress());
            item.setOnAction(event -> {
                textField.setText(suggestion.getFullAddress());
                textField.positionCaret(suggestion.getFullAddress().length());
                contextMenu.hide();
            });
            contextMenu.getItems().add(item);
        }
        
        // Afficher le menu sous le TextField
        if (!contextMenu.isShowing()) {
            contextMenu.show(textField, Side.BOTTOM, 0, 0);
        }
    }
    
    /**
     * Version simplifiée pour les TextArea (adresses multi-lignes)
     */
    public void setupAddressAutocompleteForTextArea(javafx.scene.control.TextArea textArea) {
        if (textArea == null) return;
        
        // Pour les TextArea, on peut ajouter un comportement simplifié
        // en écoutant les modifications et en suggérant lors de nouvelles lignes
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() >= MIN_CHARS) {
                String[] lines = newValue.split("\n");
                String lastLine = lines[lines.length - 1].trim();
                
                if (lastLine.length() >= MIN_CHARS) {
                    // On pourrait implémenter une logique similaire ici
                    // pour l'instant, on laisse le comportement par défaut
                }
            }
        });
    }
    
    /**
     * Valide si une chaîne ressemble à une adresse française
     */
    public boolean isValidFrenchAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false;
        }
        
        String normalized = address.toLowerCase().trim();
        
        // Vérifications basiques pour une adresse française
        boolean hasNumber = normalized.matches(".*\\d+.*");
        boolean hasStreetWord = normalized.matches(".*(rue|avenue|av|boulevard|bd|place|pl|impasse|imp|chemin|route|allée).*");
        boolean hasPostalCode = normalized.matches(".*\\b\\d{5}\\b.*");
        
        return hasNumber && (hasStreetWord || hasPostalCode);
    }
}