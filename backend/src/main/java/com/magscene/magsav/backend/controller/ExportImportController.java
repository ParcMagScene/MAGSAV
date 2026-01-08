package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.*;
import com.magscene.magsav.backend.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller REST pour les exports/imports de données
 * Gestion des exports Excel/CSV et imports CSV pour tous les modules
 */
@RestController
@RequestMapping("/api/export")
@Tag(name = "Export/Import", description = "Export et import de données (CSV, Excel)")
@CrossOrigin(origins = "*")
public class ExportImportController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private RepairRepository repairRepository;

    @Autowired
    private RMARepository rmaRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==================== EXPORTS CSV ====================

    /**
     * Export équipements au format CSV
     */
    @GetMapping("/equipment/csv")
    @Operation(summary = "Export équipements CSV", description = "Exporte tous les équipements au format CSV")
    public ResponseEntity<InputStreamResource> exportEquipmentCSV() {
        List<Equipment> equipment = equipmentRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nom,Référence Interne,Catégorie,Marque,Modèle,Statut,Emplacement,Date Création\n");

        for (Equipment e : equipment) {
            csv.append(escapeCsv(e.getId()))
                    .append(",").append(escapeCsv(e.getName()))
                    .append(",").append(escapeCsv(e.getInternalReference()))
                    .append(",").append(escapeCsv(e.getCategory()))
                    .append(",").append(escapeCsv(e.getBrand()))
                    .append(",").append(escapeCsv(e.getModel()))
                    .append(",").append(escapeCsv(e.getStatus() != null ? e.getStatus().getDisplayName() : ""))
                    .append(",").append(escapeCsv(e.getLocation()))
                    .append(",")
                    .append(escapeCsv(e.getCreatedAt() != null ? e.getCreatedAt().format(CSV_DATE_FORMAT) : ""))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "equipments_" + getTimestamp() + ".csv");
    }

    /**
     * Export véhicules au format CSV
     */
    @GetMapping("/vehicles/csv")
    @Operation(summary = "Export véhicules CSV", description = "Exporte tous les véhicules au format CSV")
    public ResponseEntity<InputStreamResource> exportVehiclesCSV() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append(
                "ID,Nom,Plaque,Type,Marque,Modèle,Statut,Carburant,Kilométrage,Assurance Expiration,Contrôle Technique\n");

        for (Vehicle v : vehicles) {
            csv.append(escapeCsv(v.getId()))
                    .append(",").append(escapeCsv(v.getName()))
                    .append(",").append(escapeCsv(v.getLicensePlate()))
                    .append(",").append(escapeCsv(v.getType().getDisplayName()))
                    .append(",").append(escapeCsv(v.getBrand()))
                    .append(",").append(escapeCsv(v.getModel()))
                    .append(",").append(escapeCsv(v.getStatus().getDisplayName()))
                    .append(",").append(escapeCsv(v.getFuelType().getDisplayName()))
                    .append(",").append(escapeCsv(v.getMileage()))
                    .append(",").append(escapeCsv(v.getInsuranceExpiration()))
                    .append(",").append(escapeCsv(v.getTechnicalControlExpiration()))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "vehicles_" + getTimestamp() + ".csv");
    }

    /**
     * Export personnel au format CSV
     */
    @GetMapping("/personnel/csv")
    @Operation(summary = "Export personnel CSV", description = "Exporte tout le personnel au format CSV")
    public ResponseEntity<InputStreamResource> exportPersonnelCSV() {
        List<Personnel> personnel = personnelRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Prénom,Nom,Email,Téléphone,Type,Statut,Poste,Département,Date Embauche\n");

        for (Personnel p : personnel) {
            csv.append(escapeCsv(p.getId()))
                    .append(",").append(escapeCsv(p.getFirstName()))
                    .append(",").append(escapeCsv(p.getLastName()))
                    .append(",").append(escapeCsv(p.getEmail()))
                    .append(",").append(escapeCsv(p.getPhone()))
                    .append(",").append(escapeCsv(p.getType().getDisplayName()))
                    .append(",").append(escapeCsv(p.getStatus().getDisplayName()))
                    .append(",").append(escapeCsv(p.getJobTitle()))
                    .append(",").append(escapeCsv(p.getDepartment()))
                    .append(",").append(escapeCsv(p.getHireDate()))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "personnel_" + getTimestamp() + ".csv");
    }

    /**
     * Export demandes SAV au format CSV
     */
    @GetMapping("/service-requests/csv")
    @Operation(summary = "Export demandes SAV CSV", description = "Exporte toutes les demandes SAV au format CSV")
    public ResponseEntity<InputStreamResource> exportServiceRequestsCSV() {
        List<ServiceRequest> requests = serviceRequestRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append(
                "ID,Titre,Demandeur,Email,Statut,Priorité,Type,Description,Technicien,Date Création,Date Résolution\n");

        for (ServiceRequest sr : requests) {
            csv.append(escapeCsv(sr.getId()))
                    .append(",").append(escapeCsv(sr.getTitle()))
                    .append(",").append(escapeCsv(sr.getRequesterName()))
                    .append(",").append(escapeCsv(sr.getRequesterEmail()))
                    .append(",").append(escapeCsv(sr.getStatus() != null ? sr.getStatus().getDisplayName() : ""))
                    .append(",").append(escapeCsv(sr.getPriority() != null ? sr.getPriority().getDisplayName() : ""))
                    .append(",").append(escapeCsv(sr.getType() != null ? sr.getType().getDisplayName() : ""))
                    .append(",").append(escapeCsv(sr.getDescription()))
                    .append(",").append(escapeCsv(sr.getAssignedTechnician()))
                    .append(",")
                    .append(escapeCsv(sr.getCreatedAt() != null ? sr.getCreatedAt().format(CSV_DATE_FORMAT) : ""))
                    .append(",")
                    .append(escapeCsv(sr.getResolvedAt() != null ? sr.getResolvedAt().format(CSV_DATE_FORMAT) : ""))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "service_requests_" + getTimestamp() + ".csv");
    }

    /**
     * Export réparations au format CSV
     */
    @GetMapping("/repairs/csv")
    @Operation(summary = "Export réparations CSV", description = "Exporte toutes les réparations au format CSV")
    public ResponseEntity<InputStreamResource> exportRepairsCSV() {
        List<Repair> repairs = repairRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Numéro,Équipement,Statut,Priorité,Technicien,Problème,Coût Estimé,Date Demande,Date Fin\n");

        for (Repair r : repairs) {
            csv.append(escapeCsv(r.getId()))
                    .append(",").append(escapeCsv(r.getRepairNumber()))
                    .append(",").append(escapeCsv(r.getEquipmentName()))
                    .append(",").append(escapeCsv(r.getStatus() != null ? r.getStatus().getDisplayName() : ""))
                    .append(",").append(escapeCsv(r.getPriority() != null ? r.getPriority().getDisplayName() : ""))
                    .append(",").append(escapeCsv(r.getTechnicianName()))
                    .append(",").append(escapeCsv(r.getProblemDescription()))
                    .append(",").append(escapeCsv(r.getEstimatedCost()))
                    .append(",").append(escapeCsv(r.getRequestDate()))
                    .append(",").append(escapeCsv(r.getCompletionDate()))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "repairs_" + getTimestamp() + ".csv");
    }

    /**
     * Export RMA au format CSV
     */
    @GetMapping("/rma/csv")
    @Operation(summary = "Export RMA CSV", description = "Exporte tous les RMA au format CSV")
    public ResponseEntity<InputStreamResource> exportRMACSV() {
        List<RMA> rmas = rmaRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Numéro,Équipement,Statut,Priorité,Problème,Date Demande\n");

        for (RMA rma : rmas) {
            csv.append(escapeCsv(rma.getId()))
                    .append(",").append(escapeCsv(rma.getRmaNumber()))
                    .append(",").append(escapeCsv(rma.getEquipmentName()))
                    .append(",").append(escapeCsv(rma.getStatus() != null ? rma.getStatus().getDisplayName() : ""))
                    .append(",").append(escapeCsv(rma.getPriority() != null ? rma.getPriority().getDisplayName() : ""))
                    .append(",").append(escapeCsv(rma.getDescription()))
                    .append(",").append(escapeCsv(rma.getRequestDate()))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "rma_" + getTimestamp() + ".csv");
    }

    /**
     * Export clients au format CSV
     */
    @GetMapping("/clients/csv")
    @Operation(summary = "Export clients CSV", description = "Exporte tous les clients au format CSV")
    public ResponseEntity<InputStreamResource> exportClientsCSV() {
        List<Client> clients = clientRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Société,Email,Téléphone,Adresse,Ville,Code Postal\n");

        for (Client c : clients) {
            csv.append(escapeCsv(c.getId()))
                    .append(",").append(escapeCsv(c.getCompanyName()))
                    .append(",").append(escapeCsv(c.getEmail()))
                    .append(",").append(escapeCsv(c.getPhone()))
                    .append(",").append(escapeCsv(c.getAddress()))
                    .append(",").append(escapeCsv(c.getCity()))
                    .append(",").append(escapeCsv(c.getPostalCode()))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "clients_" + getTimestamp() + ".csv");
    }

    /**
     * Export projets au format CSV
     */
    @GetMapping("/projects/csv")
    @Operation(summary = "Export projets CSV", description = "Exporte tous les projets au format CSV")
    public ResponseEntity<InputStreamResource> exportProjectsCSV() {
        List<Project> projects = projectRepository.findAll();

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Nom,Description,Statut,Date Création\n");

        for (Project p : projects) {
            csv.append(escapeCsv(p.getId()))
                    .append(",").append(escapeCsv(p.getName()))
                    .append(",").append(escapeCsv(p.getDescription()))
                    .append(",").append(escapeCsv(p.getStatus() != null ? p.getStatus().name() : ""))
                    .append(",")
                    .append(escapeCsv(p.getCreatedAt() != null ? p.getCreatedAt().format(CSV_DATE_FORMAT) : ""))
                    .append("\n");
        }

        return createCsvResponse(csv.toString(), "projects_" + getTimestamp() + ".csv");
    }

    // ==================== STATISTIQUES EXPORTS ====================

    /**
     * Obtenir les statistiques sur les exports disponibles
     */
    @GetMapping("/statistics")
    @Operation(summary = "Statistiques exports", description = "Nombre d'enregistrements disponibles pour export")
    public ResponseEntity<java.util.Map<String, Object>> getExportStatistics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        stats.put("equipment", equipmentRepository.count());
        stats.put("vehicles", vehicleRepository.count());
        stats.put("personnel", personnelRepository.count());
        stats.put("serviceRequests", serviceRequestRepository.count());
        stats.put("repairs", repairRepository.count());
        stats.put("rma", rmaRepository.count());
        stats.put("clients", clientRepository.count());
        stats.put("projects", projectRepository.count());
        stats.put("totalRecords",
                equipmentRepository.count() +
                        vehicleRepository.count() +
                        personnelRepository.count() +
                        serviceRequestRepository.count() +
                        repairRepository.count() +
                        rmaRepository.count() +
                        clientRepository.count() +
                        projectRepository.count());

        return ResponseEntity.ok(stats);
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String escapeCsv(Object value) {
        if (value == null)
            return "";
        String str = value.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private ResponseEntity<InputStreamResource> createCsvResponse(String csvContent, String filename) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(csvContent.getBytes(StandardCharsets.UTF_8).length)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
