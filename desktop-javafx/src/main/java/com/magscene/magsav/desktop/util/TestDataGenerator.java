package com.magscene.magsav.desktop.util;

import com.magscene.magsav.desktop.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Générateur de données de test pour tous les modules MAGSAV
 */
public class TestDataGenerator {
    
    private static final Random random = new Random();
    
    /**
     * Génère une liste de clients de test
     */
    public static List<Client> generateClients(int count) {
        List<Client> clients = new ArrayList<>();
        String[] firstNames = {"Jean", "Marie", "Pierre", "Sophie", "Luc", "Emma", "Paul", "Camille", "Thomas", "Julie"};
        String[] lastNames = {"Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", "Leroy", "Moreau"};
        String[] companies = {"Salle des Fêtes", "Centre Culturel", "Théâtre Municipal", "Association", "Mairie", "Lycée", "Collège"};
        String[] cities = {"Paris", "Lyon", "Marseille", "Toulouse", "Bordeaux", "Nantes", "Strasbourg", "Lille", "Nice", "Rennes"};
        
        for (int i = 0; i < count; i++) {
            Client client = new Client();
            client.setId((long) (i + 1));
            
            boolean isCompany = random.nextBoolean();
            if (isCompany) {
                client.setType(Client.ClientType.CORPORATE);
                client.setCompanyName(companies[random.nextInt(companies.length)] + " " + cities[random.nextInt(cities.length)]);
            } else {
                client.setType(Client.ClientType.INDIVIDUAL);
                client.setCompanyName(firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)]);
            }
            
            String cleanName = client.getCompanyName().toLowerCase().replaceAll(" ", ".");
            client.setEmail(cleanName + "@example.com");
            client.setPhone("0" + (random.nextInt(6) + 1) + " " + 
                          String.format("%02d", random.nextInt(100)) + " " +
                          String.format("%02d", random.nextInt(100)) + " " +
                          String.format("%02d", random.nextInt(100)) + " " +
                          String.format("%02d", random.nextInt(100)));
            
            client.setAddress(random.nextInt(999) + " rue de la " + cities[random.nextInt(cities.length)]);
            client.setCity(cities[random.nextInt(cities.length)]);
            client.setPostalCode(String.format("%05d", random.nextInt(95000) + 1000));
            client.setCountry("France");
            
            client.setStatus(Client.ClientStatus.ACTIVE);
            client.setCategory(Client.ClientCategory.STANDARD);
            client.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            client.setUpdatedAt(client.getCreatedAt().plusDays(random.nextInt(30)));
            
            clients.add(client);
        }
        
        System.out.println("✅ " + clients.size() + " clients générés");
        return clients;
    }
    
    /**
     * Génère une liste d'équipements de test
     */
    public static List<Equipment> generateEquipments(int count) {
        List<Equipment> equipments = new ArrayList<>();
        String[] categories = {"Structure", "Éclairage", "Audio", "Vidéo", "Accessoires"};
        String[][] brands = {
            {"Prolyte", "Litec", "Global Truss"},
            {"Martin", "Robe", "Chauvet"},
            {"Shure", "Sennheiser", "JBL"},
            {"Barco", "Christie", "Panasonic"},
            {"Neutrik", "Stage Ninja", "Adam Hall"}
        };
        
        for (int i = 0; i < count; i++) {
            Equipment equipment = new Equipment();
            equipment.setId((long) (i + 1));
            
            int catIndex = random.nextInt(categories.length);
            String category = categories[catIndex];
            String brand = brands[catIndex][random.nextInt(brands[catIndex].length)];
            
            equipment.setCategory(category);
            equipment.setBrand(brand);
            equipment.setName(brand + " " + category + " " + (i + 1));
            equipment.setModel("Model-" + (random.nextInt(900) + 100));
            equipment.setSerialNumber("SN" + String.format("%08d", random.nextInt(100000000)));
            equipment.setQrCode("QR-" + equipment.getSerialNumber());
            
            equipment.setPurchaseDate(LocalDate.now().minusDays(random.nextInt(1000)));
            equipment.setPurchasePrice(random.nextDouble() * 5000 + 500);
            equipment.setCurrentValue(equipment.getPurchasePrice() * (0.5 + random.nextDouble() * 0.5));
            
            equipment.setLocation("Entrepôt " + (char)('A' + random.nextInt(5)));
            equipment.setCondition(random.nextInt(10) + "/10");
            equipment.setWarranty(random.nextBoolean());
            
            if (equipment.isWarranty()) {
                equipment.setWarrantyExpirationDate(LocalDate.now().plusDays(random.nextInt(730)));
            }
            
            equipment.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            equipment.setUpdatedAt(equipment.getCreatedAt().plusDays(random.nextInt(30)));
            
            equipments.add(equipment);
        }
        
        System.out.println("✅ " + equipments.size() + " équipements générés");
        return equipments;
    }
    
    /**
     * Génère une liste de contrats de test
     */
    public static List<Contract> generateContracts(int count, List<Client> clients) {
        List<Contract> contracts = new ArrayList<>();
        Contract.ContractType[] types = Contract.ContractType.values();
        Contract.ContractStatus[] statuses = Contract.ContractStatus.values();
        
        for (int i = 0; i < count; i++) {
            Contract contract = new Contract();
            contract.setId((long) (i + 1));
            contract.setContractNumber("CTR-" + String.format("%05d", i + 1));
            
            if (!clients.isEmpty()) {
                contract.setClientId(clients.get(random.nextInt(clients.size())).getId());
            }
            
            contract.setType(types[random.nextInt(types.length)]);
            contract.setStatus(statuses[random.nextInt(statuses.length)]);
            
            LocalDate startDate = LocalDate.now().minusDays(random.nextInt(180));
            contract.setStartDate(startDate);
            contract.setEndDate(startDate.plusDays(30 + random.nextInt(335)));
            
            BigDecimal amount = BigDecimal.valueOf(random.nextDouble() * 10000 + 500).setScale(2, java.math.RoundingMode.HALF_UP);
            contract.setTotalAmount(amount);
            
            contract.setDescription("Contrat de " + contract.getType().getDisplayName().toLowerCase());
            contract.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            contract.setUpdatedAt(contract.getCreatedAt().plusDays(random.nextInt(30)));
            
            contracts.add(contract);
        }
        
        System.out.println("✅ " + contracts.size() + " contrats générés");
        return contracts;
    }
    
    /**
     * Génère une liste de demandes SAV de test
     */
    public static List<ServiceRequest> generateServiceRequests(int count, List<Equipment> equipments, List<Client> clients) {
        List<ServiceRequest> requests = new ArrayList<>();
        ServiceRequest.ServiceRequestType[] types = ServiceRequest.ServiceRequestType.values();
        ServiceRequest.ServiceRequestStatus[] statuses = ServiceRequest.ServiceRequestStatus.values();
        ServiceRequest.Priority[] priorities = ServiceRequest.Priority.values();
        String[] issues = {
            "Panne électrique", "Dysfonctionnement mécanique", "Problème de connectivité",
            "Usure normale", "Dommage physique", "Problème logiciel",
            "Câblage défectueux", "Surcharge", "Problème de calibration", "Composant HS"
        };
        
        for (int i = 0; i < count; i++) {
            ServiceRequest request = new ServiceRequest();
            request.setId((long) (i + 1));
            request.setTitle("SAV-" + String.format("%06d", i + 1));
            
            if (!equipments.isEmpty()) {
                request.setEquipmentName(equipments.get(random.nextInt(equipments.size())).getName());
            }
            
            if (!clients.isEmpty()) {
                Client client = clients.get(random.nextInt(clients.size()));
                request.setRequesterName(client.getCompanyName());
                request.setRequesterEmail(client.getEmail());
            }
            
            request.setType(types[random.nextInt(types.length)]);
            request.setPriority(priorities[random.nextInt(priorities.length)]);
            request.setStatus(statuses[random.nextInt(statuses.length)]);
            
            request.setDescription(issues[random.nextInt(issues.length)]);
            request.setEstimatedCost(random.nextDouble() * 500 + 50);
            
            LocalDateTime reportedDate = LocalDateTime.now().minusDays(random.nextInt(30));
            request.setCreatedAt(reportedDate);
            
            if (request.getStatus() == ServiceRequest.ServiceRequestStatus.RESOLVED || 
                request.getStatus() == ServiceRequest.ServiceRequestStatus.CLOSED) {
                request.setResolvedAt(reportedDate.plusDays(random.nextInt(10) + 1));
                request.setActualCost(request.getEstimatedCost() * (0.8 + random.nextDouble() * 0.4));
            }
            
            request.setUpdatedAt(request.getCreatedAt().plusDays(random.nextInt(5)));
            
            requests.add(request);
        }
        
        System.out.println("✅ " + requests.size() + " demandes SAV générées");
        return requests;
    }
    
    /**
     * Génère une liste de véhicules de test
     */
    public static List<Vehicle> generateVehicles(int count) {
        List<Vehicle> vehicles = new ArrayList<>();
        String[] types = {"Utilitaire", "Camion", "Fourgon"};
        String[] brands = {"Renault", "Peugeot", "Citroën", "Ford", "Mercedes"};
        String[] models = {"Master", "Boxer", "Sprinter", "Transit", "Jumper"};
        
        for (int i = 0; i < count; i++) {
            Vehicle vehicle = new Vehicle();
            vehicle.setId((long) (i + 1));
            vehicle.setType(types[random.nextInt(types.length)]);
            vehicle.setBrand(brands[random.nextInt(brands.length)]);
            vehicle.setModel(models[random.nextInt(models.length)]);
            
            // Format immatriculation française : AA-123-AA
            String plate = String.format("%c%c-%03d-%c%c",
                (char)('A' + random.nextInt(26)), (char)('A' + random.nextInt(26)),
                random.nextInt(1000),
                (char)('A' + random.nextInt(26)), (char)('A' + random.nextInt(26)));
            vehicle.setRegistrationNumber(plate);
            
            vehicle.setYear(2015 + random.nextInt(10));
            vehicle.setPurchaseDate(LocalDate.of(vehicle.getYear(), 1 + random.nextInt(12), 1 + random.nextInt(28)));
            vehicle.setMileage(random.nextInt(150000));
            vehicle.setLoadCapacity(500 + random.nextInt(2000)); // en kg
            vehicle.setVolumeCapacity(5 + random.nextInt(15)); // en m³
            vehicle.setStatus("Disponible");
            
            LocalDate lastMaintenance = LocalDate.now().minusDays(random.nextInt(180));
            vehicle.setLastMaintenanceDate(lastMaintenance);
            vehicle.setNextMaintenanceDate(lastMaintenance.plusDays(180));
            vehicle.setInsuranceExpiry(LocalDate.now().plusDays(random.nextInt(365)));
            vehicle.setTechnicalControlExpiry(LocalDate.now().plusDays(random.nextInt(730)));
            
            vehicle.setDailyRate(50.0 + random.nextDouble() * 100.0);
            vehicle.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(730)));
            
            vehicles.add(vehicle);
        }
        
        System.out.println("✅ " + vehicles.size() + " véhicules générés");
        return vehicles;
    }
    
    /**
     * Génère une liste de projets de test
     */
    public static List<Project> generateProjects(int count, List<Client> clients) {
        List<Project> projects = new ArrayList<>();
        String[] types = {"Concert", "Festival", "Spectacle", "Conférence", "Soirée privée", "Mariage", "Corporate", "Salon"};
        String[] statuses = {"En cours", "Planifié", "Terminé", "Annulé"};
        
        for (int i = 0; i < count; i++) {
            Project project = new Project();
            project.setId((long) (i + 1));
            project.setReference("PRJ-" + String.format("%05d", i + 1));
            project.setName(types[random.nextInt(types.length)] + " " + (2024 + random.nextInt(2)));
            project.setType(types[random.nextInt(types.length)]);
            
            if (!clients.isEmpty()) {
                project.setClient(clients.get(random.nextInt(clients.size())));
            }
            
            project.setStatus(statuses[random.nextInt(statuses.length)]);
            
            LocalDate startDate = LocalDate.now().plusDays(random.nextInt(90) - 30);
            project.setStartDate(startDate);
            project.setEndDate(startDate.plusDays(1 + random.nextInt(7)));
            
            double quotedAmount = 5000 + random.nextDouble() * 50000;
            project.setQuotedAmount(quotedAmount);
            project.setFinalAmount(quotedAmount * (0.9 + random.nextDouble() * 0.2));
            
            project.setDescription("Projet " + project.getType() + " pour événement client");
            project.setLocation("Lieu événementiel - France");
            project.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(180)));
            project.setUpdatedAt(project.getCreatedAt().plusDays(random.nextInt(30)));
            
            projects.add(project);
        }
        
        System.out.println("✅ " + projects.size() + " projets générés");
        return projects;
    }
    
    /**
     * Génère une liste de personnel de test
     */
    public static List<Personnel> generatePersonnel(int count) {
        List<Personnel> personnelList = new ArrayList<>();
        String[] firstNames = {"Jean", "Marie", "Pierre", "Sophie", "Luc", "Emma", "Paul", "Camille", "Thomas", "Julie", "Marc", "Laura"};
        String[] lastNames = {"Martin", "Bernard", "Dubois", "Thomas", "Robert", "Richard", "Petit", "Durand", "Leroy", "Moreau", "Simon", "Laurent"};
        String[] positions = {"Technicien son", "Technicien lumière", "Régisseur", "Chef de projet", "Électricien", "Monteur", "Chauffeur"};
        String[] contractTypes = {"CDI", "CDD", "Intermittent", "Freelance"};
        String[] qualifications = {"Habilitation électrique", "CACES R489", "Permis B", "Permis C", "Travail en hauteur"};
        
        for (int i = 0; i < count; i++) {
            Personnel personnel = new Personnel();
            personnel.setId((long) (i + 1));
            personnel.setFirstName(firstNames[random.nextInt(firstNames.length)]);
            personnel.setLastName(lastNames[random.nextInt(lastNames.length)]);
            personnel.setEmail(personnel.getFirstName().toLowerCase() + "." + 
                             personnel.getLastName().toLowerCase() + "@magscene.fr");
            personnel.setPhone("0" + (random.nextInt(6) + 1) + " " + 
                             String.format("%02d", random.nextInt(100)) + " " +
                             String.format("%02d", random.nextInt(100)) + " " +
                             String.format("%02d", random.nextInt(100)) + " " +
                             String.format("%02d", random.nextInt(100)));
            
            personnel.setPosition(positions[random.nextInt(positions.length)]);
            personnel.setContractType(contractTypes[random.nextInt(contractTypes.length)]);
            personnel.setQualification(qualifications[random.nextInt(qualifications.length)]);
            personnel.setHireDate(LocalDate.now().minusDays(random.nextInt(1825))); // 0-5 ans
            personnel.setHourlyRate(15.0 + random.nextDouble() * 30.0); // 15-45 €/h
            personnel.setActive(random.nextInt(10) > 1); // 90% actifs
            personnel.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(365)));
            personnel.setUpdatedAt(personnel.getCreatedAt().plusDays(random.nextInt(30)));
            
            personnelList.add(personnel);
        }
        
        System.out.println("✅ " + personnelList.size() + " membres du personnel générés");
        return personnelList;
    }
    
    /**
     * Génère un jeu de données complet pour tous les modules
     */
    public static TestDataSet generateCompleteDataSet() {
        System.out.println("🧪 Génération du jeu de données de test complet...");
        System.out.println("━".repeat(50));
        
        TestDataSet dataSet = new TestDataSet();
        
        dataSet.clients = generateClients(25);
        dataSet.equipments = generateEquipments(50);
        dataSet.contracts = generateContracts(20, dataSet.clients);
        dataSet.serviceRequests = generateServiceRequests(30, dataSet.equipments, dataSet.clients);
        dataSet.vehicles = generateVehicles(10);
        dataSet.projects = generateProjects(15, dataSet.clients);
        dataSet.personnel = generatePersonnel(12);
        
        System.out.println("━".repeat(50));
        System.out.println("✅ Génération terminée avec succès !");
        System.out.println("📊 Total: " + (dataSet.clients.size() + dataSet.equipments.size() + 
                                          dataSet.contracts.size() + dataSet.serviceRequests.size() + 
                                          dataSet.vehicles.size() + dataSet.projects.size() + 
                                          dataSet.personnel.size()) + " éléments");
        
        return dataSet;
    }
    
    /**
     * Classe conteneur pour l'ensemble des données de test
     */
    public static class TestDataSet {
        public List<Client> clients = new ArrayList<>();
        public List<Equipment> equipments = new ArrayList<>();
        public List<Contract> contracts = new ArrayList<>();
        public List<ServiceRequest> serviceRequests = new ArrayList<>();
        public List<Vehicle> vehicles = new ArrayList<>();
        public List<Project> projects = new ArrayList<>();
        public List<Personnel> personnel = new ArrayList<>();
    }
}
