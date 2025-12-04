package com.magscene.magsav.desktop.service.api;

import java.util.concurrent.CompletableFuture;

/**
 * Client API spécialisé pour la gestion du SAV
 * Fournit des données simulées quand le backend n'est pas disponible
 */
public class SAVApiClient extends BaseApiClient {

    public SAVApiClient() {
        super();
    }

    /**
     * Récupère toutes les demandes SAV avec fallback sur données simulées
     */
    public CompletableFuture<String> getAllSAVRequests() {
        return getAsync("/api/service-requests")
            .exceptionally(error -> {
                System.out.println("⚠️ Backend indisponible - Utilisation des données SAV simulées");
                return getSimulatedSAVData();
            });
    }

    /**
     * Récupère une demande SAV par son ID
     */
    public CompletableFuture<String> getSAVRequestById(Long id) {
        return getAsync("/api/service-requests/" + id);
    }

    /**
     * Crée une nouvelle demande SAV
     */
    public CompletableFuture<String> createSAVRequest(Object savRequest) {
        return postAsync("/api/service-requests", savRequest);
    }

    /**
     * Met à jour une demande SAV
     */
    public CompletableFuture<String> updateSAVRequest(Long id, Object savRequest) {
        return putAsync("/api/service-requests/" + id, savRequest);
    }

    /**
     * Supprime une demande SAV
     */
    public CompletableFuture<String> deleteSAVRequest(Long id) {
        return deleteAsync("/api/service-requests/" + id);
    }

    /**
     * Change le statut d'une demande SAV
     */
    public CompletableFuture<String> updateSAVStatus(Long id, String status) {
        return putAsync("/api/sav/" + id + "/status", status);
    }

    /**
     * Récupère les statistiques SAV
     */
    public CompletableFuture<String> getSAVStatistics() {
        return getAsync("/api/sav/statistics");
    }

    /**
     * Recherche des demandes SAV par critères
     */
    public CompletableFuture<String> searchSAVRequests(String query, String status, String priority) {
        StringBuilder endpoint = new StringBuilder("/api/sav/search?");

        if (query != null && !query.trim().isEmpty()) {
            endpoint.append("q=").append(query).append("&");
        }
        if (status != null && !status.equals("Tous statuts")) {
            endpoint.append("status=").append(status).append("&");
        }
        if (priority != null && !priority.equals("Toutes priorités")) {
            endpoint.append("priority=").append(priority).append("&");
        }

        // Supprimer le dernier & si présent
        String finalEndpoint = endpoint.toString();
        if (finalEndpoint.endsWith("&")) {
            finalEndpoint = finalEndpoint.substring(0, finalEndpoint.length() - 1);
        }

        return getAsync(finalEndpoint);
    }
    
    /**
     * Données SAV simulées pour le mode hors-ligne
     */
    private String getSimulatedSAVData() {
        return """
            [
                {"id": 1, "requestId": "SAV-2024-001", "title": "Réparation console Allen & Heath", "type": "REPARATION", "status": "IN_PROGRESS", "priority": "HIGH", "createdAt": "2024-11-15T10:30:00", "assignedTechnician": "Jean Dupont", "equipmentName": "Console Allen & Heath SQ-7", "description": "Problème de canal 12 - Pas de signal", "estimatedCost": 350.00},
                {"id": 2, "requestId": "SAV-2024-002", "title": "Maintenance projecteur Martin", "type": "MAINTENANCE", "status": "OPEN", "priority": "MEDIUM", "createdAt": "2024-11-16T14:00:00", "assignedTechnician": null, "equipmentName": "Projecteur Martin MAC Encore", "description": "Nettoyage optique et révision complète", "estimatedCost": 180.00},
                {"id": 3, "requestId": "SAV-2024-003", "title": "RMA Micro HF Sennheiser", "type": "RMA", "status": "WAITING_PARTS", "priority": "LOW", "createdAt": "2024-11-10T09:15:00", "assignedTechnician": "Sophie Martin", "equipmentName": "Micro HF Sennheiser EW 100 G4", "description": "Émetteur défectueux - Envoi en garantie constructeur", "estimatedCost": 0.00},
                {"id": 4, "requestId": "SAV-2024-004", "title": "Diagnostic lyre Clay Paky", "type": "DIAGNOSTIC", "status": "IN_PROGRESS", "priority": "HIGH", "createdAt": "2024-11-18T11:00:00", "assignedTechnician": "Marc Leroy", "equipmentName": "Lyre Clay Paky Sharpy Plus", "description": "Pan/Tilt bloqué - Investigation en cours", "estimatedCost": 450.00},
                {"id": 5, "requestId": "SAV-2024-005", "title": "Calibration caméra Sony", "type": "CALIBRATION", "status": "RESOLVED", "priority": "MEDIUM", "createdAt": "2024-11-05T16:30:00", "assignedTechnician": "Pierre Dubois", "equipmentName": "Caméra Sony FX6", "description": "Mise à jour firmware et recalibration capteur", "estimatedCost": 120.00},
                {"id": 6, "requestId": "SAV-2024-006", "title": "Réparation urgente enceinte L-Acoustics", "type": "REPARATION", "status": "OPEN", "priority": "URGENT", "createdAt": "2024-11-20T08:00:00", "assignedTechnician": null, "equipmentName": "Enceinte L-Acoustics K2", "description": "HP grave endommagé suite à incident - Festival ce weekend", "estimatedCost": 2500.00},
                {"id": 7, "requestId": "SAV-2024-007", "title": "Vérification amplificateur", "type": "VERIFICATION", "status": "CLOSED", "priority": "LOW", "createdAt": "2024-10-28T13:45:00", "assignedTechnician": "Jean Dupont", "equipmentName": "Amplificateur L-Acoustics LA12X", "description": "Test de routine après location longue durée", "estimatedCost": 80.00},
                {"id": 8, "requestId": "SAV-2024-008", "title": "Remplacement lampe poursuite", "type": "REPARATION", "status": "WAITING_PARTS", "priority": "MEDIUM", "createdAt": "2024-11-12T10:00:00", "assignedTechnician": "Sophie Martin", "equipmentName": "Followspot Robert Juliat Cyrano", "description": "Lampe Xenon à remplacer - Commande en cours", "estimatedCost": 850.00}
            ]
            """;
    }
}