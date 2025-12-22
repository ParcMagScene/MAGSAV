package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Client;
import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.entity.ServiceRequest;
import com.magscene.magsav.backend.entity.ServiceRequest.ServiceRequestStatus;
import com.magscene.magsav.backend.repository.ClientRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import com.magscene.magsav.backend.repository.PersonnelRepository;
import com.magscene.magsav.backend.repository.ProjectRepository;
import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controleur REST pour les statistiques du Dashboard
 * Fournit des donnees agregees pour l'affichage du tableau de bord
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class DashboardController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PersonnelRepository personnelRepository;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Test simple pour verifier que le controleur est charge
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Dashboard controller OK");
    }

    /**
     * Statistiques globales du dashboard
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Statistiques Equipements
            Map<String, Object> equipmentStats = new HashMap<>();
            equipmentStats.put("total", equipmentRepository.count());
            equipmentStats.put("available", safeCountEquipment(Equipment.Status.AVAILABLE));
            equipmentStats.put("inUse", safeCountEquipment(Equipment.Status.IN_USE));
            equipmentStats.put("maintenance", safeCountEquipment(Equipment.Status.MAINTENANCE));
            stats.put("equipment", equipmentStats);

            // Statistiques SAV
            Map<String, Object> savStats = new HashMap<>();
            savStats.put("total", serviceRequestRepository.count());
            long openSav = safeCountSav(ServiceRequestStatus.OPEN);
            long inProgressSav = safeCountSav(ServiceRequestStatus.IN_PROGRESS);
            savStats.put("active", openSav + inProgressSav);
            savStats.put("open", openSav);
            savStats.put("inProgress", inProgressSav);
            savStats.put("resolved", safeCountSav(ServiceRequestStatus.RESOLVED));
            savStats.put("closed", safeCountSav(ServiceRequestStatus.CLOSED));
            stats.put("sav", savStats);

            // Statistiques Clients
            Map<String, Object> clientStats = new HashMap<>();
            clientStats.put("total", clientRepository.count());
            clientStats.put("active", safeCountClient(Client.ClientStatus.ACTIVE));
            stats.put("clients", clientStats);

            // Statistiques Vehicules
            Map<String, Object> vehicleStats = new HashMap<>();
            vehicleStats.put("total", vehicleRepository.count());
            try {
                vehicleStats.put("available", vehicleRepository.findAvailableVehicles().size());
            } catch (Exception e) {
                vehicleStats.put("available", 0);
            }
            stats.put("vehicles", vehicleStats);

            // Statistiques Personnel (si disponible)
            if (personnelRepository != null) {
                Map<String, Object> personnelStats = new HashMap<>();
                personnelStats.put("total", personnelRepository.count());
                stats.put("personnel", personnelStats);
            }

            // Statistiques Projets (si disponible)
            if (projectRepository != null) {
                Map<String, Object> projectStats = new HashMap<>();
                projectStats.put("total", projectRepository.count());
                stats.put("projects", projectStats);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Erreur getDashboardStats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    private long safeCountEquipment(Equipment.Status status) {
        try {
            Long result = equipmentRepository.countByStatus(status);
            return result != null ? result : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private long safeCountSav(ServiceRequestStatus status) {
        try {
            Long result = serviceRequestRepository.countByStatus(status);
            return result != null ? result : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
    
    private long safeCountClient(Client.ClientStatus status) {
        try {
            Long result = clientRepository.countByStatus(status);
            return result != null ? result : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Statistiques SAV par mois pour le graphique
     * GET /api/dashboard/sav-by-month
     */
    @GetMapping("/sav-by-month")
    public ResponseEntity<List<Map<String, Object>>> getSavByMonth() {
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        
        try {
            LocalDate now = LocalDate.now();
            List<ServiceRequest> allRequests = serviceRequestRepository.findAll();
            
            for (int i = 5; i >= 0; i--) {
                YearMonth targetMonth = YearMonth.from(now.minusMonths(i));
                String monthName = getMonthName(targetMonth.getMonthValue());
                
                long count = allRequests.stream()
                    .filter(r -> r.getCreatedAt() != null)
                    .filter(r -> {
                        YearMonth requestMonth = YearMonth.from(r.getCreatedAt());
                        return requestMonth.equals(targetMonth);
                    })
                    .count();
                
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthName);
                monthData.put("count", count);
                monthData.put("year", targetMonth.getYear());
                monthlyData.add(monthData);
            }
        } catch (Exception e) {
            System.err.println("Erreur getSavByMonth: " + e.getMessage());
        }
        
        return ResponseEntity.ok(monthlyData);
    }

    /**
     * Repartition des equipements par categorie
     * GET /api/dashboard/equipment-by-category
     */
    @GetMapping("/equipment-by-category")
    public ResponseEntity<List<Map<String, Object>>> getEquipmentByCategory() {
        List<Map<String, Object>> categoryData = new ArrayList<>();
        
        try {
            Map<String, Long> categoryCounts = new HashMap<>();
            for (Equipment eq : equipmentRepository.findAll()) {
                String cat = eq.getCategory() != null ? eq.getCategory() : "Non categorise";
                categoryCounts.merge(cat, 1L, Long::sum);
            }
            for (Map.Entry<String, Long> entry : categoryCounts.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("category", entry.getKey());
                item.put("count", entry.getValue());
                categoryData.add(item);
            }
        } catch (Exception e) {
            System.err.println("Erreur getEquipmentByCategory: " + e.getMessage());
        }
        
        return ResponseEntity.ok(categoryData);
    }

    private String getMonthName(int month) {
        String[] months = {"", "Jan", "Fev", "Mar", "Avr", "Mai", "Juin", 
                          "Juil", "Aout", "Sept", "Oct", "Nov", "Dec"};
        return month > 0 && month < months.length ? months[month] : "";
    }
}
