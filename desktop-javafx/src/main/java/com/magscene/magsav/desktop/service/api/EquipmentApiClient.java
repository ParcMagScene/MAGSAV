package com.magscene.magsav.desktop.service.api;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Client API spécialisé pour la gestion des équipements
 * Fournit des données LOCMAT simulées quand le backend n'est pas disponible
 */
public class EquipmentApiClient extends BaseApiClient {
    
    public EquipmentApiClient() {
        super();
    }
    
    /**
     * Récupère tous les équipements avec fallback sur données LOCMAT simulées
     */
    public CompletableFuture<String> getAllEquipments() {
        return getAsync("/api/equipment")
            .exceptionally(error -> {
                System.out.println("⚠️ Backend indisponible - Utilisation des données LOCMAT simulées");
                return getSimulatedLocmatData();
            });
    }
    
    /**
     * Récupère un équipement par son ID
     */
    public CompletableFuture<String> getEquipmentById(Long id) {
        return getAsync("/api/equipment/" + id);
    }
    
    /**
     * Crée un nouvel équipement
     */
    public CompletableFuture<String> createEquipment(Object equipment) {
        return postAsync("/api/equipment", equipment);
    }
    
    /**
     * Met à jour un équipement existant
     */
    public CompletableFuture<String> updateEquipment(Long id, Object equipment) {
        return putAsync("/api/equipment/" + id, equipment);
    }
    
    /**
     * Supprime un équipement
     */
    public CompletableFuture<String> deleteEquipment(Long id) {
        return deleteAsync("/api/equipment/" + id);
    }
    
    /**
     * Recherche des équipements par critères
     */
    public CompletableFuture<String> searchEquipments(String query, String category, String status) {
        StringBuilder endpoint = new StringBuilder("/api/equipment/search?");
        
        if (query != null && !query.trim().isEmpty()) {
            endpoint.append("q=").append(query).append("&");
        }
        if (category != null && !category.equals("Toutes catégories")) {
            endpoint.append("category=").append(category).append("&");
        }
        if (status != null && !status.equals("Tous statuts")) {
            endpoint.append("status=").append(status).append("&");
        }
        
        // Supprimer le dernier & si présent
        String finalEndpoint = endpoint.toString();
        if (finalEndpoint.endsWith("&")) {
            finalEndpoint = finalEndpoint.substring(0, finalEndpoint.length() - 1);
        }
        
        return getAsync(finalEndpoint);
    }
    
    /**
     * Import LOCMAT
     */
    public CompletableFuture<String> importLocmat(String filePath) {
        return postAsync("/api/locmat/import", filePath);
    }
    
    /**
     * Génère les QR codes pour les équipements
     */
    public CompletableFuture<String> generateQRCodes(List<Long> equipmentIds) {
        return postAsync("/api/equipment/qrcodes", equipmentIds);
    }
    
    /**
     * Exporte les équipements
     */
    public CompletableFuture<String> exportEquipments(String format) {
        return getAsync("/api/equipment/export?format=" + format);
    }
    
    /**
     * Données LOCMAT simulées pour le mode hors-ligne
     * Basées sur le format du fichier LOCMAT_Materiel.xlsx
     */
    private String getSimulatedLocmatData() {
        return """
            [
                {"id": 1, "name": "Console Allen & Heath SQ-7", "brand": "Allen & Heath", "model": "SQ-7", "category": "AUDIO", "subcategory": "Console", "status": "AVAILABLE", "qrCode": "LOC-AH-SQ7-001", "location": "Entrepôt A", "serialNumber": "SN-AH-2024-001", "purchasePrice": 5500.00, "notes": "Console numérique 48 canaux"},
                {"id": 2, "name": "Enceinte L-Acoustics K2", "brand": "L-Acoustics", "model": "K2", "category": "AUDIO", "subcategory": "Diffusion", "status": "AVAILABLE", "qrCode": "LOC-LA-K2-001", "location": "Entrepôt A", "serialNumber": "SN-LA-2023-012", "purchasePrice": 15000.00, "notes": "Line array"},
                {"id": 3, "name": "Projecteur Martin MAC Encore Performance", "brand": "Martin", "model": "MAC Encore Performance", "category": "ECLAIRAGE", "subcategory": "Projecteur asservi", "status": "AVAILABLE", "qrCode": "LOC-MA-ENC-001", "location": "Entrepôt B", "serialNumber": "SN-MA-2024-045", "purchasePrice": 8500.00, "notes": "Projecteur LED 1500W"},
                {"id": 4, "name": "Caméra Sony FX6", "brand": "Sony", "model": "FX6", "category": "VIDEO", "subcategory": "Caméra", "status": "IN_USE", "qrCode": "LOC-SO-FX6-001", "location": "En mission", "serialNumber": "SN-SO-2023-003", "purchasePrice": 6200.00, "notes": "Cinema Line 4K"},
                {"id": 5, "name": "Truss Prolyte H30V 3m", "brand": "Prolyte", "model": "H30V", "category": "STRUCTURE", "subcategory": "Truss", "status": "AVAILABLE", "qrCode": "LOC-PR-H30V-156", "location": "Entrepôt C", "serialNumber": "SN-PR-2022-156", "purchasePrice": 450.00, "notes": "Structure aluminium"},
                {"id": 6, "name": "Gradateur Strand C21", "brand": "Strand", "model": "C21", "category": "ECLAIRAGE", "subcategory": "Gradateur", "status": "AVAILABLE", "qrCode": "LOC-ST-C21-022", "location": "Entrepôt B", "serialNumber": "SN-ST-2021-022", "purchasePrice": 3200.00, "notes": "Gradateur 12 circuits"},
                {"id": 7, "name": "Micro Shure SM58", "brand": "Shure", "model": "SM58", "category": "AUDIO", "subcategory": "Microphone", "status": "AVAILABLE", "qrCode": "LOC-SH-SM58-089", "location": "Entrepôt A", "serialNumber": "SN-SH-2024-089", "purchasePrice": 120.00, "notes": "Micro dynamique vocal"},
                {"id": 8, "name": "Écran LED ROE MC-7H", "brand": "ROE Visual", "model": "MC-7H", "category": "VIDEO", "subcategory": "Écran LED", "status": "IN_USE", "qrCode": "LOC-RO-MC7H-008", "location": "Festival Été", "serialNumber": "SN-RO-2023-008", "purchasePrice": 25000.00, "notes": "Pitch 7.8mm outdoor"},
                {"id": 9, "name": "Console Yamaha CL5", "brand": "Yamaha", "model": "CL5", "category": "AUDIO", "subcategory": "Console", "status": "AVAILABLE", "qrCode": "LOC-YA-CL5-001", "location": "Entrepôt A", "serialNumber": "SN-YA-2023-001", "purchasePrice": 18000.00, "notes": "Console numérique 72 canaux"},
                {"id": 10, "name": "Subwoofer L-Acoustics KS28", "brand": "L-Acoustics", "model": "KS28", "category": "AUDIO", "subcategory": "Sub", "status": "AVAILABLE", "qrCode": "LOC-LA-KS28-004", "location": "Entrepôt A", "serialNumber": "SN-LA-2024-004", "purchasePrice": 8500.00, "notes": "Caisson de basses 2x18"},
                {"id": 11, "name": "Amplificateur L-Acoustics LA12X", "brand": "L-Acoustics", "model": "LA12X", "category": "AUDIO", "subcategory": "Amplification", "status": "AVAILABLE", "qrCode": "LOC-LA-12X-010", "location": "Entrepôt A", "serialNumber": "SN-LA-2023-010", "purchasePrice": 5500.00, "notes": "Ampli 4 canaux"},
                {"id": 12, "name": "Lyre Clay Paky Sharpy Plus", "brand": "Clay Paky", "model": "Sharpy Plus", "category": "ECLAIRAGE", "subcategory": "Lyre", "status": "MAINTENANCE", "qrCode": "LOC-CP-SHP-015", "location": "Atelier SAV", "serialNumber": "SN-CP-2022-015", "purchasePrice": 4200.00, "notes": "Beam hybride 330W - En réparation"},
                {"id": 13, "name": "Processeur vidéo Barco E2", "brand": "Barco", "model": "E2", "category": "VIDEO", "subcategory": "Processeur", "status": "AVAILABLE", "qrCode": "LOC-BA-E2-002", "location": "Entrepôt D", "serialNumber": "SN-BA-2023-002", "purchasePrice": 45000.00, "notes": "Processeur multi-écrans"},
                {"id": 14, "name": "Followspot Robert Juliat Cyrano", "brand": "Robert Juliat", "model": "Cyrano", "category": "ECLAIRAGE", "subcategory": "Poursuite", "status": "AVAILABLE", "qrCode": "LOC-RJ-CYR-003", "location": "Entrepôt B", "serialNumber": "SN-RJ-2024-003", "purchasePrice": 12000.00, "notes": "Poursuite longue portée"},
                {"id": 15, "name": "Micro HF Sennheiser EW 100 G4", "brand": "Sennheiser", "model": "EW 100 G4", "category": "AUDIO", "subcategory": "HF", "status": "AVAILABLE", "qrCode": "LOC-SE-EW100-025", "location": "Entrepôt A", "serialNumber": "SN-SE-2024-025", "purchasePrice": 650.00, "notes": "Système HF complet"}
            ]
            """;
    }
}