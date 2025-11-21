package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.service.LocmatImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller pour l'import des données LOCMAT
 */
@RestController
@RequestMapping("/api/locmat")
@CrossOrigin(origins = "*")
public class LocmatImportController {
    
    @Autowired
    private LocmatImportService locmatImportService;
    
    /**
     * Endpoint de diagnostic pour l'import LOCMAT
     */
    @GetMapping("/import/debug")
    public ResponseEntity<Map<String, Object>> getImportDebugInfo() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("status", "active");
        debug.put("timestamp", System.currentTimeMillis());
        debug.put("memoryUsage", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        debug.put("maxMemory", Runtime.getRuntime().maxMemory());
        debug.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        return ResponseEntity.ok(debug);
    }
    
    /**
     * Endpoint pour importer le fichier Excel LOCMAT
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importLocmatData(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifications du fichier
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Le fichier est vide");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isExcelFile(file)) {
                response.put("success", false);
                response.put("message", "Le fichier doit être au format Excel (.xlsx)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Traitement de l'import
            LocmatImportService.ImportResult result = locmatImportService.importLocmatData(file);
            
            // Préparer la réponse
            response.put("success", true);
            response.put("message", result.getSummary());
            response.put("equipmentCount", result.getSuccessCount());
            response.put("errorCount", result.getErrors().size());
            
            if (result.hasErrors()) {
                response.put("errors", result.getErrors());
            }
            
            // Status HTTP selon les résultats
            HttpStatus status = result.hasErrors() ? 
                (result.getSuccessCount() > 0 ? HttpStatus.PARTIAL_CONTENT : HttpStatus.BAD_REQUEST) :
                HttpStatus.OK;
            
            return ResponseEntity.status(status).body(response);
            
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la lecture du fichier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur inattendue: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint pour obtenir des statistiques sur les imports LOCMAT
     */
    @GetMapping("/import/stats")
    public ResponseEntity<Map<String, Object>> getImportStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Récupération des statistiques réelles
            long totalEquipment = locmatImportService.getTotalEquipmentCount();
            stats.put("totalImports", locmatImportService.getTotalImportCount());
            stats.put("lastImportDate", locmatImportService.getLastImportDate());
            stats.put("totalEquipmentImported", totalEquipment);
            stats.put("status", "active");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            stats.put("error", "Erreur lors de la récupération des statistiques: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(stats);
        }
    }
    
    /**
     * Endpoint de test pour vérifier la connectivité
     */
    @GetMapping("/import/test")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Service LOCMAT Import fonctionnel");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint GET pour valider le service de validation
     */
    @GetMapping("/import/validate")
    public ResponseEntity<Map<String, Object>> validateService() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ready");
        response.put("message", "Service de validation LOCMAT prêt");
        response.put("supportedFormats", Arrays.asList(".xlsx", ".xls"));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint pour valider un fichier LOCMAT sans l'importer
     */
    @PostMapping("/import/validate")
    public ResponseEntity<Map<String, Object>> validateLocmatFile(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Vérifications basiques
            if (file.isEmpty()) {
                response.put("valid", false);
                response.put("message", "Fichier vide");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isExcelFile(file)) {
                response.put("valid", false);
                response.put("message", "Format de fichier invalide. Utilisez .xlsx ou .xls");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validation du contenu
            LocmatImportService.ValidationResult validation = locmatImportService.validateFile(file);
            
            response.put("valid", validation.isValid());
            response.put("message", validation.getMessage());
            response.put("rowCount", validation.getRowCount());
            response.put("validRows", validation.getValidRowCount());
            response.put("errors", validation.getErrors());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Erreur lors de la validation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint GET pour le service de prévisualisation
     */
    @GetMapping("/import/preview")
    public ResponseEntity<Map<String, Object>> previewService() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ready");
        response.put("message", "Service de prévisualisation LOCMAT prêt");
        response.put("defaultMaxRows", 10);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint pour prévisualiser les données sans les importer
     */
    @PostMapping("/import/preview")
    public ResponseEntity<Map<String, Object>> previewLocmatData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "maxRows", defaultValue = "10") int maxRows) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "Fichier vide");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (!isExcelFile(file)) {
                response.put("success", false);
                response.put("message", "Format de fichier invalide");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Prévisualisation des données
            LocmatImportService.PreviewResult preview = locmatImportService.previewFile(file, maxRows);
            
            response.put("success", true);
            response.put("preview", preview.getData());
            response.put("totalRows", preview.getTotalRows());
            response.put("previewRows", preview.getPreviewRows());
            response.put("columns", preview.getColumns());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la prévisualisation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Endpoint GET pour le service de traitement
     */
    @GetMapping("/import/process")
    public ResponseEntity<Map<String, Object>> processService() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ready");
        response.put("message", "Service de traitement LOCMAT prêt");
        response.put("acceptedMethods", Arrays.asList("POST"));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint pour traiter un import (alias pour /import)
     */
    @PostMapping("/import/process")
    public ResponseEntity<Map<String, Object>> processLocmatImport(
            @RequestParam("file") MultipartFile file) {
        // Déléguer au endpoint principal d'import
        return importLocmatData(file);
    }
    
    /**
     * Vérifier si le fichier est au format Excel
     */
    private boolean isExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        return (contentType != null && 
                (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                 contentType.equals("application/vnd.ms-excel"))) ||
               (filename != null && 
                (filename.endsWith(".xlsx") || filename.endsWith(".xls")));
    }
}