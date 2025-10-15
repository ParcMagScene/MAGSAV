package com.magsav.util;

/**
 * Utilitaires pour traiter les adresses
 */
public class AddressUtils {
    
    /**
     * Extrait la ville d'une adresse complète (méthode simplifiée)
     * Format attendu: "rue, code_postal ville" ou simplement "ville"
     */
    public static String extractCityFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        
        String[] parts = address.split(",");
        if (parts.length > 1) {
            // Prendre la dernière partie et extraire la ville après le code postal
            String lastPart = parts[parts.length - 1].trim();
            String[] cityParts = lastPart.split(" ");
            if (cityParts.length > 1) {
                // Prendre tout après le premier mot (supposé être le code postal)
                return String.join(" ", java.util.Arrays.copyOfRange(cityParts, 1, cityParts.length));
            }
            return lastPart;
        }
        
        return address;
    }
}