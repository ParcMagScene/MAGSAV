package com.magscene.magsav.backend.service;

import com.magscene.magsav.backend.entity.ServiceRequest;
import com.magscene.magsav.backend.entity.ServiceRequest.ServiceRequestStatus;
import com.magscene.magsav.backend.entity.ServiceRequest.ServiceRequestType;
import com.magscene.magsav.backend.entity.ServiceRequest.Priority;
import com.magscene.magsav.backend.entity.Equipment;
import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
/**
 * Initialise les donnÃ©es de dÃ©monstration pour le module SAV
 * S'exÃ©cute aprÃ¨s l'initialisation des Ã©quipements (@Order(2))
 */
@Component
@Order(2)
public class SavDataInitializer implements CommandLineRunner {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;
    
    @Autowired 
    private EquipmentRepository equipmentRepository;

    @Override
    public void run(String... args) throws Exception {
        // VÃ©rifier si des donnÃ©es SAV existent dÃ©jÃ 
        if (serviceRequestRepository.count() > 0) {
            System.out.println("ðŸ“‹ DonnÃ©es SAV existantes dÃ©tectÃ©es, initialisation ignorÃ©e.");
            return;
        }
        
        System.out.println("ðŸ”§ Initialisation des donnÃ©es de dÃ©monstration SAV...");
        
        // RÃ©cupÃ©rer quelques Ã©quipements pour les lier aux demandes SAV
        List<Equipment> equipments = equipmentRepository.findAll();
        if (equipments.isEmpty()) {
            System.out.println("âš ï¸ Aucun Ã©quipement trouvÃ©, impossible de crÃ©er des demandes SAV.");
            return;
        }

        // CrÃ©er des demandes SAV de dÃ©monstration
        createServiceRequest(
            "Console M32 - Canal 12 dÃ©faillant", 
            "Le canal 12 de la console Yamaha M32 ne fonctionne plus. Aucun signal en sortie.",
            ServiceRequestType.REPAIR, 
            Priority.HIGH,
            ServiceRequestStatus.IN_PROGRESS,
            equipments.get(0),
            "Jean Dupont",
            "jean.dupont@magscene.fr",
            "Michel Technicien",
            1200.0,
            950.0,
            "Canal 12 rÃ©parÃ© - Remplacement du prÃ©ampli"
        );

        createServiceRequest(
            "Projecteur LED - Maintenance prÃ©ventive", 
            "Maintenance prÃ©ventive du projecteur LED 300W - Nettoyage optique et vÃ©rification gÃ©nÃ©rale.",
            ServiceRequestType.MAINTENANCE, 
            Priority.MEDIUM,
            ServiceRequestStatus.RESOLVED,
            equipments.size() > 1 ? equipments.get(1) : equipments.get(0),
            "Marie Martin",
            "marie.martin@magscene.fr", 
            "Sophie Expert",
            300.0,
            250.0,
            "Maintenance effectuÃ©e - Optique nettoyÃ©e, LED OK"
        );

        createServiceRequest(
            "Micros HF - ProblÃ¨me de portÃ©e", 
            "Les micros HF perdent le signal au-delÃ  de 50m. VÃ©rifier antennes et rÃ©cepteur.",
            ServiceRequestType.REPAIR, 
            Priority.MEDIUM,
            ServiceRequestStatus.OPEN,
            equipments.size() > 2 ? equipments.get(2) : equipments.get(0),
            "Paul IngÃ©nieur",
            "paul.ingenieur@magscene.fr",
            null,
            800.0,
            null,
            null
        );

        createServiceRequest(
            "Formation console numÃ©rique", 
            "Formation des techniciens sur la nouvelle console numÃ©rique X32.",
            ServiceRequestType.TRAINING, 
            Priority.LOW,
            ServiceRequestStatus.IN_PROGRESS,
            equipments.size() > 3 ? equipments.get(3) : equipments.get(0),
            "Responsable Formation",
            "formation@magscene.fr",
            "Expert Audio",
            1500.0,
            null,
            null
        );

        createServiceRequest(
            "RMA Ampli - DÃ©faut fabrication", 
            "Ampli avec dÃ©faut de fabrication - Retour constructeur sous garantie.",
            ServiceRequestType.RMA, 
            Priority.HIGH,
            ServiceRequestStatus.WAITING_PARTS,
            equipments.size() > 4 ? equipments.get(4) : equipments.get(0),
            "Service Achat",
            "achat@magscene.fr",
            "Responsable RMA",
            0.0,
            0.0,
            "En attente retour constructeur"
        );

        createServiceRequest(
            "Installation Ã©clairage salle", 
            "Installation complÃ¨te de l'Ã©clairage pour la nouvelle salle de spectacle.",
            ServiceRequestType.INSTALLATION, 
            Priority.HIGH,
            ServiceRequestStatus.IN_PROGRESS,
            equipments.size() > 5 ? equipments.get(5) : equipments.get(0),
            "Chef de Projet",
            "projet@magscene.fr",
            "Ã‰quipe Installation",
            5000.0,
            4200.0,
            "Installation en cours - 70% terminÃ©"
        );

        long savCount = serviceRequestRepository.count();
        System.out.println("âœ… " + savCount + " demandes SAV de dÃ©monstration crÃ©Ã©es !");
        
        // Affichage des statistiques
        displaySavStatistics();
    }
    
    private void createServiceRequest(String title, String description, ServiceRequestType type,
                                    Priority priority, ServiceRequestStatus status, Equipment equipment,
                                    String requesterName, String requesterEmail, String assignedTechnician,
                                    Double estimatedCost, Double actualCost, String resolutionNotes) {
        
        ServiceRequest request = new ServiceRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setType(type);
        request.setPriority(priority);
        request.setStatus(status);
        request.setEquipment(equipment);
        request.setRequesterName(requesterName);
        request.setRequesterEmail(requesterEmail);
        request.setAssignedTechnician(assignedTechnician);
        request.setEstimatedCost(estimatedCost);
        request.setActualCost(actualCost);
        request.setResolutionNotes(resolutionNotes);
        
        // Dates basÃ©es sur le statut
        LocalDateTime now = LocalDateTime.now();
        request.setCreatedAt(now.minusDays((long)(Math.random() * 30))); // Entre 0 et 30 jours
        request.setUpdatedAt(now.minusDays((long)(Math.random() * 5)));  // Mis Ã  jour rÃ©cemment
        
        if (status == ServiceRequestStatus.RESOLVED || status == ServiceRequestStatus.CLOSED) {
            request.setResolvedAt(now.minusDays((long)(Math.random() * 10))); // RÃ©solu dans les 10 derniers jours
        }
        
        serviceRequestRepository.save(request);
    }
    
    private void displaySavStatistics() {
        System.out.println("ðŸ“Š RÃ©partition des demandes SAV :");
        
        // Par statut
        long open = serviceRequestRepository.countByStatus(ServiceRequestStatus.OPEN);
        long inProgress = serviceRequestRepository.countByStatus(ServiceRequestStatus.IN_PROGRESS);
        long resolved = serviceRequestRepository.countByStatus(ServiceRequestStatus.RESOLVED);
        long waitingParts = serviceRequestRepository.countByStatus(ServiceRequestStatus.WAITING_PARTS);
        
        System.out.println("   - Ouvertes: " + open);
        System.out.println("   - En cours: " + inProgress);
        System.out.println("   - RÃ©solues: " + resolved);
        System.out.println("   - Attente piÃ¨ces: " + waitingParts);
        
        // Par prioritÃ©
        long high = serviceRequestRepository.countByPriority(Priority.HIGH);
        long medium = serviceRequestRepository.countByPriority(Priority.MEDIUM);
        long low = serviceRequestRepository.countByPriority(Priority.LOW);
        
        System.out.println("   - PrioritÃ© haute: " + high);
        System.out.println("   - PrioritÃ© moyenne: " + medium);
        System.out.println("   - PrioritÃ© basse: " + low);
        
        System.out.println("ðŸ”§ Module SAV initialisÃ© avec succÃ¨s !");
    }
}
