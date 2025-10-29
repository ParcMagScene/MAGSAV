package com.magscene.magsav.backend.controller;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class HealthController {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "MAGSAV-3.0 Backend");
        response.put("version", "3.0.0");
        response.put("java", System.getProperty("java.version"));
        response.put("spring", "3.1.5");
        response.put("timestamp", System.currentTimeMillis());
        response.put("message", "Backend Spring Boot opÃƒÂ©rationnel !");
        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = equipmentRepository.count();
        stats.put("total", total);
        stats.put("available", equipmentRepository.countByStatus(Equipment.Status.AVAILABLE));
        stats.put("inUse", equipmentRepository.countByStatus(Equipment.Status.IN_USE));
        stats.put("maintenance", equipmentRepository.countByStatus(Equipment.Status.MAINTENANCE));
        stats.put("outOfService", equipmentRepository.countByStatus(Equipment.Status.OUT_OF_ORDER));
        
        // DonnÃƒÂ©es par catÃƒÂ©gorie
        List<Object[]> categoryData = equipmentRepository.getEquipmentCountByCategory();
        Map<String, Long> categories = new HashMap<>();
        for (Object[] row : categoryData) {
            categories.put((String) row[0], (Long) row[1]);
        }
        stats.put("categories", categories);
        
        return stats;
    }
}

