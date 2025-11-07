package com.magscene.magsav.backend.service;

import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Override
    public void run(String... args) throws Exception {
        // Verifier si des donnees existent deja
        if (equipmentRepository.count() > 0) {
            System.out.println("\uD83D\uDD04 Donnees deja presentes, pas de reinitialisation");
            return;
        }

        System.out.println("\uD83C\uDFAF Initialisation des donnees de demonstration MAGSAV-3.0...");

        // Équipements Audio
        createEquipment("Console Yamaha M32", "Console de mixage numérique 32 canaux", "Audio", 
                       Equipment.Status.AVAILABLE, "QR001", "Yamaha", "M32", "SN-M32-001", 4500.00);
        
        createEquipment("Micro HF Shure ULX-D", "Système micro sans fil UHF-R", "Audio", 
                       Equipment.Status.IN_USE, "QR002", "Shure", "ULXD24/SM58", "SN-ULXD-002", 1200.00);
        
        createEquipment("Enceinte L-Acoustics K2", "Enceinte line array 3 voies", "Audio", 
                       Equipment.Status.AVAILABLE, "QR003", "L-Acoustics", "K2", "SN-K2-003", 8900.00);
        
        createEquipment("Amplificateur Crown iTech 12000HD", "Amplificateur de puissance classe I", "Audio", 
                       Equipment.Status.MAINTENANCE, "QR004", "Crown", "iTech 12000HD", "SN-CROWN-004", 3200.00);

        // Équipements Éclairage
        createEquipment("Projecteur Martin MAC Quantum Profile", "Projecteur LED 500W avec zoom", "Éclairage", 
                       Equipment.Status.AVAILABLE, "QR005", "Martin", "MAC Quantum Profile", "SN-MARTIN-005", 7500.00);
        
        createEquipment("Lyre Robe MegaPointe", "Lyre hybride beam/spot/wash 470W", "Éclairage", 
                       Equipment.Status.IN_USE, "QR006", "Robe", "MegaPointe", "SN-ROBE-006", 12000.00);
        
        createEquipment("Console Grand MA3 Light", "Console d'éclairage 4096 paramètres", "Éclairage", 
                       Equipment.Status.AVAILABLE, "QR007", "MA Lighting", "Grand MA3 Light", "SN-MA3-007", 15000.00);
        
        createEquipment("Projecteur Ayrton Khamsin-S", "Projecteur LED wash 900W", "Éclairage", 
                       Equipment.Status.OUT_OF_ORDER, "QR008", "Ayrton", "Khamsin-S", "SN-AYRTON-008", 4800.00);

        // Équipements Vidéo
        createEquipment("Caméra Blackmagic URSA Mini Pro 12K", "Caméra cinéma 12K Super 35", "Vidéo", 
                       Equipment.Status.AVAILABLE, "QR009", "Blackmagic", "URSA Mini Pro 12K", "SN-BMD-009", 9500.00);
        
        createEquipment("Mélangeur ATEM Television Studio Pro 4K", "Mélangeur vidéo live 8 entrées", "Vidéo", 
                       Equipment.Status.IN_USE, "QR010", "Blackmagic", "ATEM TVS Pro 4K", "SN-ATEM-010", 3500.00);
        
        createEquipment("Écran LED P2.6 500x500mm", "Dalle LED intérieure haute résolution", "Vidéo", 
                       Equipment.Status.AVAILABLE, "QR011", "Absen", "Acclaim A2615", "SN-LED-011", 2800.00);

        // Équipements Structures
        createEquipment("Pont H40V Prolyte", "Structure aluminium H40V 3m", "Structures", 
                       Equipment.Status.AVAILABLE, "QR012", "Prolyte", "H40V-L300", "SN-PROL-012", 450.00);
        
        createEquipment("Pied Manfrotto 387XBU", "Pied télescopique charge 40kg", "Structures", 
                       Equipment.Status.MAINTENANCE, "QR013", "Manfrotto", "387XBU", "SN-MANF-013", 280.00);

        // Équipements Câblage
        createEquipment("Multipaire 32 voies Sommercable", "Multipaire analogique 50m XLR", "Câblage", 
                       Equipment.Status.AVAILABLE, "QR014", "Sommercable", "SC-Meridian SP32", "SN-SOMM-014", 850.00);
        
        createEquipment("Splitter optique 1:8 Neutrik", "Splitter MADI optique 8 sorties", "Câblage", 
                       Equipment.Status.IN_USE, "QR015", "Neutrik", "NA-MADI-8O", "SN-NEUT-015", 1200.00);

        // Équipements Transport
        createEquipment("Flight Case sur mesure", "Flight case pour console M32", "Transport", 
                       Equipment.Status.AVAILABLE, "QR016", "MAGSAV Custom", "FC-M32-001", "SN-CASE-016", 320.00);
        
        createEquipment("Rack 19\" 12U mobile", "Rack de transport avec roulettes", "Transport", 
                       Equipment.Status.AVAILABLE, "QR017", "Thomann", "Rack Case 12U", "SN-RACK-017", 180.00);

        System.out.println("\u2705 " + equipmentRepository.count() + " equipements de demonstration crees !");
        System.out.println("\uD83D\uDCC9 Repartition par statut :");
        System.out.println("   - Disponible: " + equipmentRepository.countByStatus(Equipment.Status.AVAILABLE));
        System.out.println("   - En cours d'utilisation: " + equipmentRepository.countByStatus(Equipment.Status.IN_USE));
        System.out.println("   - En maintenance: " + equipmentRepository.countByStatus(Equipment.Status.MAINTENANCE));
        System.out.println("   - Hors service: " + equipmentRepository.countByStatus(Equipment.Status.OUT_OF_ORDER));
    }

    private void createEquipment(String name, String description, String category, Equipment.Status status,
                               String qrCode, String brand, String model, String serialNumber, Double price) {
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setDescription(description);
        equipment.setCategory(category);
        equipment.setStatus(status);
        equipment.setQrCode(qrCode);
        equipment.setBrand(brand);
        equipment.setModel(model);
        equipment.setSerialNumber(serialNumber);
        equipment.setPurchasePrice(price);
        equipment.setPurchaseDate(LocalDateTime.now().minusDays((long) (Math.random() * 365)));
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());
        
        equipmentRepository.save(equipment);
    }
}
