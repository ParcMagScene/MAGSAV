package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.dto.EquipmentDTO;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import com.magscene.magsav.backend.repository.EquipmentPhotoRepository;
import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.ContractItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * ContrÃƒÆ’Ã‚Â´leur REST pour la gestion des ÃƒÆ’Ã‚Â©quipements
 */
@RestController
@RequestMapping("/api/equipment")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080" })
public class EquipmentRestController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private EquipmentPhotoRepository equipmentPhotoRepository;

    @Autowired(required = false)
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired(required = false)
    private ContractItemRepository contractItemRepository;

    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re tous les ÃƒÆ’Ã‚Â©quipements
     * GET /api/equipment
     */
    @GetMapping
    public List<EquipmentDTO> getAllEquipment() {
        return equipmentRepository.findAll()
                .stream()
                .map(EquipmentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re un ÃƒÆ’Ã‚Â©quipement par son ID
     * GET /api/equipment/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDTO> getEquipmentById(@PathVariable Long id) {
        Optional<Equipment> equipment = equipmentRepository.findById(id);
        return equipment.map(e -> ResponseEntity.ok(new EquipmentDTO(e)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re les ÃƒÆ’Ã‚Â©quipements par catÃƒÆ’Ã‚Â©gorie
     * GET /api/equipment/category/{category}
     */
    @GetMapping("/category/{category}")
    public List<EquipmentDTO> getEquipmentByCategory(@PathVariable String category) {
        return equipmentRepository.findByCategory(category)
                .stream()
                .map(EquipmentDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re les ÃƒÆ’Ã‚Â©quipements par statut
     * GET /api/equipment/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EquipmentDTO>> getEquipmentByStatus(@PathVariable String status) {
        try {
            Equipment.Status equipmentStatus = Equipment.Status.valueOf(status.toUpperCase());
            List<EquipmentDTO> equipment = equipmentRepository.findByStatus(equipmentStatus)
                    .stream()
                    .map(EquipmentDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(equipment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Recherche d'ÃƒÆ’Ã‚Â©quipements par nom
     * GET /api/equipment/search?name=...
     */
    @GetMapping("/search")
    public List<Equipment> searchEquipment(@RequestParam String name) {
        return equipmentRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * RÃƒÆ’Ã‚Â©cupÃƒÆ’Ã‚Â¨re un ÃƒÆ’Ã‚Â©quipement par son QR Code
     * GET /api/equipment/qr/{qrCode}
     */
    @GetMapping("/qr/{qrCode}")
    public ResponseEntity<Equipment> getEquipmentByQrCode(@PathVariable String qrCode) {
        Optional<Equipment> equipment = equipmentRepository.findByQrCode(qrCode);
        return equipment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Statistiques des ÃƒÆ’Ã‚Â©quipements
     * GET /api/equipment/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getEquipmentStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalCount = equipmentRepository.count();
        stats.put("total", totalCount);

        stats.put("available", equipmentRepository.countByStatus(Equipment.Status.AVAILABLE));
        stats.put("inUse", equipmentRepository.countByStatus(Equipment.Status.IN_USE));
        stats.put("maintenance", equipmentRepository.countByStatus(Equipment.Status.MAINTENANCE));
        stats.put("outOfOrder", equipmentRepository.countByStatus(Equipment.Status.OUT_OF_ORDER));

        Double totalValue = equipmentRepository.calculateTotalValue();
        stats.put("totalValue", totalValue != null ? totalValue : 0.0);

        // Statistiques par catÃƒÆ’Ã‚Â©gorie
        Map<String, Long> categoryStats = new HashMap<>();
        List<Object[]> categoryData = equipmentRepository.getEquipmentCountByCategory();
        for (Object[] row : categoryData) {
            categoryStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("categories", categoryStats);

        return stats;
    }

    /**
     * CrÃƒÆ’Ã‚Â©e un nouvel ÃƒÆ’Ã‚Â©quipement
     * POST /api/equipment
     */
    @PostMapping
    public ResponseEntity<EquipmentDTO> createEquipment(@RequestBody Equipment equipment) {
        try {
            Equipment savedEquipment = equipmentRepository.save(equipment);
            return ResponseEntity.ok(new EquipmentDTO(savedEquipment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Met à jour un équipement existant (mise à jour partielle)
     * PUT /api/equipment/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDTO> updateEquipment(@PathVariable Long id, @RequestBody EquipmentDTO dto) {
        try {
            Optional<Equipment> existingOpt = equipmentRepository.findById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Equipment existing = existingOpt.get();

            // Mise à jour partielle - ne mettre à jour que les champs non-null du DTO
            if (dto.getName() != null)
                existing.setName(dto.getName());
            if (dto.getDescription() != null)
                existing.setDescription(dto.getDescription());
            if (dto.getCategory() != null)
                existing.setCategory(dto.getCategory());
            if (dto.getStatus() != null) {
                // Convertir le displayName en enum
                Equipment.Status status = Equipment.Status.fromDisplayName(dto.getStatus());
                if (status != null)
                    existing.setStatus(status);
            }
            if (dto.getQrCode() != null)
                existing.setQrCode(dto.getQrCode());
            if (dto.getBrand() != null)
                existing.setBrand(dto.getBrand());
            if (dto.getModel() != null)
                existing.setModel(dto.getModel());
            if (dto.getSerialNumber() != null)
                existing.setSerialNumber(dto.getSerialNumber());
            if (dto.getPurchasePrice() != null)
                existing.setPurchasePrice(dto.getPurchasePrice());
            if (dto.getPurchaseDate() != null)
                existing.setPurchaseDate(dto.getPurchaseDate());
            if (dto.getLocation() != null)
                existing.setLocation(dto.getLocation());
            if (dto.getNotes() != null)
                existing.setNotes(dto.getNotes());
            if (dto.getInternalReference() != null)
                existing.setInternalReference(dto.getInternalReference());
            if (dto.getWeight() != null)
                existing.setWeight(dto.getWeight());
            if (dto.getDimensions() != null)
                existing.setDimensions(dto.getDimensions());
            if (dto.getWarrantyExpiration() != null)
                existing.setWarrantyExpiration(dto.getWarrantyExpiration());
            if (dto.getSupplier() != null)
                existing.setSupplier(dto.getSupplier());
            if (dto.getInsuranceValue() != null)
                existing.setInsuranceValue(dto.getInsuranceValue());
            if (dto.getLastMaintenanceDate() != null)
                existing.setLastMaintenanceDate(dto.getLastMaintenanceDate());
            if (dto.getNextMaintenanceDate() != null)
                existing.setNextMaintenanceDate(dto.getNextMaintenanceDate());
            if (dto.getPhotoPath() != null)
                existing.setPhotoPath(dto.getPhotoPath());

            Equipment savedEquipment = equipmentRepository.save(existing);
            return ResponseEntity.ok(new EquipmentDTO(savedEquipment));
        } catch (Exception e) {
            System.err.println("Erreur mise à jour équipement " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Supprime un ÃƒÆ’Ã‚Â©quipement
     * DELETE /api/equipment/{id}
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            Optional<Equipment> equipment = equipmentRepository.findById(id);
            if (equipment.isPresent()) {
                // Supprimer les photos associées d'abord
                equipmentPhotoRepository.deleteByEquipmentId(id);
                // Puis supprimer l'équipement
                equipmentRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur suppression équipement " + id + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Vide toute la table equipment (endpoint temporaire pour maintenance)
     * DELETE /api/equipment/clear-all
     */
    @DeleteMapping("/clear-all")
    @Transactional
    public ResponseEntity<Map<String, Object>> clearAllEquipment() {
        Map<String, Object> response = new HashMap<>();
        try {
            long count = equipmentRepository.count();
            equipmentRepository.deleteAll();
            response.put("success", true);
            response.put("deleted", count);
            response.put("message", "Tous les équipements ont été supprimés");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Supprime tous les équipements n'appartenant pas à MAG SCENE
     * DELETE /api/equipment/cleanup/non-mag-scene
     */
    @DeleteMapping("/cleanup/non-mag-scene")
    @Transactional
    public ResponseEntity<Map<String, Object>> cleanupNonMagSceneEquipment() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Equipment> allEquipment = equipmentRepository.findAll();
            int deletedCount = 0;
            int skippedCount = 0;

            for (Equipment eq : allEquipment) {
                String notes = eq.getNotes();
                boolean isMagScene = notes != null && notes.contains("MAG SCENE");

                if (!isMagScene) {
                    try {
                        // Dissocier les ServiceRequests de cet équipement
                        if (serviceRequestRepository != null) {
                            var serviceRequests = serviceRequestRepository.findByEquipmentId(eq.getId());
                            for (var sr : serviceRequests) {
                                sr.setEquipment(null);
                                serviceRequestRepository.save(sr);
                            }
                        }
                        // Dissocier les ContractItems de cet équipement
                        if (contractItemRepository != null) {
                            var contractItems = contractItemRepository.findByEquipmentId(eq.getId());
                            for (var ci : contractItems) {
                                ci.setEquipment(null);
                                contractItemRepository.save(ci);
                            }
                        }
                        // Supprimer les photos associées
                        equipmentPhotoRepository.deleteByEquipmentId(eq.getId());
                        // Puis supprimer l'équipement
                        equipmentRepository.delete(eq);
                        deletedCount++;
                        System.out.println("Supprimé équipement ID " + eq.getId() + ": " + eq.getName());
                    } catch (Exception e) {
                        System.err.println("Erreur suppression équipement " + eq.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                        skippedCount++;
                    }
                }
            }

            result.put("success", true);
            result.put("deleted", deletedCount);
            result.put("skipped", skippedCount);
            result.put("remaining", equipmentRepository.count());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * Générer un QR Code pour un équipement
     * GET /api/equipment/{id}/qrcode
     */
    @GetMapping("/{id}/qrcode")
    public ResponseEntity<Map<String, String>> generateQRCode(@PathVariable Long id) {
        Optional<Equipment> equipmentOpt = equipmentRepository.findById(id);
        if (equipmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Equipment equipment = equipmentOpt.get();
        Map<String, String> response = new HashMap<>();

        // Données du QR code : format JSON avec infos essentielles
        String qrData = String.format(
                "{\"type\":\"equipment\",\"id\":%d,\"name\":\"%s\",\"serialNumber\":\"%s\",\"internalRef\":\"%s\"}",
                equipment.getId(),
                equipment.getName(),
                equipment.getSerialNumber() != null ? equipment.getSerialNumber() : "",
                equipment.getInternalReference() != null ? equipment.getInternalReference() : "");

        response.put("equipmentId", String.valueOf(equipment.getId()));
        response.put("equipmentName", equipment.getName());
        response.put("qrData", qrData);
        response.put("qrUrl", "/api/equipment/" + id + "/qrcode/image");

        return ResponseEntity.ok(response);
    }

    /**
     * Générer des QR Codes en batch
     * POST /api/equipment/qrcode/batch
     */
    @PostMapping("/qrcode/batch")
    public ResponseEntity<List<Map<String, String>>> generateBatchQRCodes(@RequestBody List<Long> equipmentIds) {
        List<Map<String, String>> qrCodes = equipmentIds.stream()
                .map(id -> equipmentRepository.findById(id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(equipment -> {
                    Map<String, String> qrData = new HashMap<>();
                    String data = String.format(
                            "{\"type\":\"equipment\",\"id\":%d,\"name\":\"%s\",\"serialNumber\":\"%s\"}",
                            equipment.getId(),
                            equipment.getName(),
                            equipment.getSerialNumber() != null ? equipment.getSerialNumber() : "");
                    qrData.put("equipmentId", String.valueOf(equipment.getId()));
                    qrData.put("equipmentName", equipment.getName());
                    qrData.put("qrData", data);
                    return qrData;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(qrCodes);
    }

    /**
     * Scanner/décoder un QR Code
     * POST /api/equipment/qrcode/scan
     */
    @PostMapping("/qrcode/scan")
    public ResponseEntity<EquipmentDTO> scanQRCode(@RequestBody Map<String, String> scanData) {
        try {
            String qrData = scanData.get("qrData");
            // Parser le JSON du QR code pour extraire l'ID
            // Format attendu: {"type":"equipment","id":123,...}
            if (qrData != null && qrData.contains("\"id\":")) {
                String idStr = qrData.split("\"id\":")[1].split(",")[0].trim();
                Long equipmentId = Long.parseLong(idStr);

                Optional<Equipment> equipmentOpt = equipmentRepository.findById(equipmentId);
                if (equipmentOpt.isPresent()) {
                    return ResponseEntity.ok(new EquipmentDTO(equipmentOpt.get()));
                }
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Exporter les équipements avec QR codes
     * GET /api/equipment/export/qrcodes
     */
    @GetMapping("/export/qrcodes")
    public ResponseEntity<List<Map<String, String>>> exportEquipmentWithQRCodes() {
        List<Equipment> allEquipment = equipmentRepository.findAll();

        List<Map<String, String>> exportData = allEquipment.stream()
                .map(equipment -> {
                    Map<String, String> data = new HashMap<>();
                    data.put("id", String.valueOf(equipment.getId()));
                    data.put("name", equipment.getName());
                    data.put("serialNumber", equipment.getSerialNumber() != null ? equipment.getSerialNumber() : "");
                    data.put("internalRef",
                            equipment.getInternalReference() != null ? equipment.getInternalReference() : "");
                    data.put("qrData", String.format(
                            "{\"type\":\"equipment\",\"id\":%d,\"name\":\"%s\"}",
                            equipment.getId(),
                            equipment.getName()));
                    return data;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(exportData);
    }

    /**
     * Import des équipements depuis un fichier CSV LOCMAT
     * POST /api/equipment/import-locmat
     */
    @PostMapping("/import-locmat")
    @Transactional
    public ResponseEntity<Map<String, Object>> importLocmatCSV(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("error", "Fichier vide");
                return ResponseEntity.badRequest().body(response);
            }

            List<Equipment> importedEquipment = new ArrayList<>();
            int lineNumber = 0;
            int imported = 0;
            int skipped = 0;
            List<String> errors = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                @SuppressWarnings("unused")
                String[] headers = null; // Headers CSV détectés automatiquement

                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    // Première ligne = headers
                    if (lineNumber == 1) {
                        headers = line.split(",");
                        continue;
                    }

                    // Parser la ligne CSV
                    String[] values = line.split(",", -1); // -1 pour garder les valeurs vides

                    if (values.length < 6) {
                        skipped++;
                        continue; // Ligne incomplète
                    }

                    try {
                        // Extraction des colonnes CSV LOCMAT
                        // Format: Famille,Sous-famille,Catégorie,ZONE,Réf,Nom,Qté en stock,...
                        String famille = values.length > 0 ? values[0].trim() : "";
                        String sousFamille = values.length > 1 ? values[1].trim() : "";
                        String categorie = values.length > 2 ? values[2].trim() : "";
                        String zone = values.length > 3 ? values[3].trim() : "";
                        String ref = values.length > 4 ? values[4].trim() : "";
                        String nom = values.length > 5 ? values[5].trim() : "";
                        String qteStock = values.length > 6 ? values[6].trim() : "0";
                        String prixAchat = values.length > 12 ? values[12].trim() : "";
                        String numeroSerie = values.length > 16 ? values[16].trim() : "";

                        // Ignorer les lignes sans nom ou référence
                        if (nom.isEmpty() || ref.isEmpty()) {
                            skipped++;
                            continue;
                        }

                        // Créer l'équipement
                        Equipment equipment = new Equipment();
                        equipment.setName(nom);
                        equipment.setInternalReference(ref);

                        // Catégorie combinée: Famille > Sous-famille > Catégorie
                        String fullCategory = famille;
                        if (!sousFamille.isEmpty()) {
                            fullCategory += " / " + sousFamille;
                        }
                        if (!categorie.isEmpty()) {
                            fullCategory += " / " + categorie;
                        }
                        equipment.setCategory(fullCategory);

                        // Localisation
                        if (!zone.isEmpty()) {
                            equipment.setLocation(zone);
                        }

                        // Numéro de série
                        if (!numeroSerie.isEmpty()) {
                            equipment.setSerialNumber(numeroSerie);
                        }

                        // Prix d'achat
                        if (!prixAchat.isEmpty()) {
                            try {
                                // Gérer les formats français (virgule décimale)
                                String cleanPrice = prixAchat.replace(",", ".");
                                equipment.setPurchasePrice(Double.parseDouble(cleanPrice));
                            } catch (NumberFormatException e) {
                                // Ignorer si conversion impossible
                            }
                        }

                        // Statut basé sur la quantité en stock
                        int qty = 0;
                        try {
                            qty = Integer.parseInt(qteStock);
                        } catch (NumberFormatException e) {
                            // Ignorer
                        }

                        if (qty > 0) {
                            equipment.setStatus(Equipment.Status.AVAILABLE);
                        } else {
                            equipment.setStatus(Equipment.Status.OUT_OF_ORDER);
                        }

                        // Générer un QR code
                        equipment.setQrCode("LOCMAT-" + ref);

                        // Dates
                        equipment.setCreatedAt(LocalDateTime.now());
                        equipment.setUpdatedAt(LocalDateTime.now());

                        // Description avec infos LOCMAT
                        StringBuilder desc = new StringBuilder();
                        desc.append("Importé depuis LOCMAT");
                        if (!zone.isEmpty()) {
                            desc.append(" - Zone: ").append(zone);
                        }
                        if (qty > 0) {
                            desc.append(" - Qté: ").append(qty);
                        }
                        equipment.setDescription(desc.toString());

                        importedEquipment.add(equipment);
                        imported++;

                    } catch (Exception e) {
                        errors.add("Ligne " + lineNumber + ": " + e.getMessage());
                        skipped++;
                    }
                }
            }

            // Sauvegarder tous les équipements en batch
            if (!importedEquipment.isEmpty()) {
                equipmentRepository.saveAll(importedEquipment);
            }

            response.put("success", true);
            response.put("imported", imported);
            response.put("skipped", skipped);
            response.put("totalLines", lineNumber - 1); // -1 pour exclure le header
            response.put("errors", errors.isEmpty() ? null : errors);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(response);
        }
    }
}