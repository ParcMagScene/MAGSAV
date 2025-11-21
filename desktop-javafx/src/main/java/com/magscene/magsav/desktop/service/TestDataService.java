package com.magscene.magsav.desktop.service;

import com.magscene.magsav.desktop.model.*;
import com.magscene.magsav.desktop.util.TestDataGenerator;
import com.magscene.magsav.desktop.util.TestDataGenerator.TestDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service singleton pour gérer les données de test en mémoire
 */
public class TestDataService {
    
    private static TestDataService instance;
    private TestDataSet dataSet;
    private boolean initialized = false;
    
    private TestDataService() {}
    
    public static TestDataService getInstance() {
        if (instance == null) {
            instance = new TestDataService();
        }
        return instance;
    }
    
    /**
     * Initialise le service avec des données de test
     */
    public void initialize() {
        if (!initialized) {
            System.out.println("🔄 Initialisation du service de données de test...");
            dataSet = TestDataGenerator.generateCompleteDataSet();
            initialized = true;
            System.out.println("✅ Service de données de test initialisé avec succès!");
        }
    }
    
    /**
     * Régénère toutes les données
     */
    public void reset() {
        System.out.println("🔄 Régénération des données de test...");
        dataSet = TestDataGenerator.generateCompleteDataSet();
        System.out.println("✅ Données régénérées !");
    }
    
    // ==================== CLIENTS ====================
    
    public List<Client> getAllClients() {
        return new ArrayList<>(dataSet.clients);
    }
    
    public Client getClientById(Long id) {
        return dataSet.clients.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Client> searchClients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllClients();
        }
        String lowerQuery = query.toLowerCase();
        return dataSet.clients.stream()
            .filter(c -> 
                (c.getCompanyName() != null && c.getCompanyName().toLowerCase().contains(lowerQuery)) ||
                (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerQuery)) ||
                (c.getCity() != null && c.getCity().toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    public void addClient(Client client) {
        if (client.getId() == null) {
            long maxId = dataSet.clients.stream().mapToLong(Client::getId).max().orElse(0);
            client.setId(maxId + 1);
        }
        dataSet.clients.add(client);
    }
    
    public void updateClient(Client client) {
        for (int i = 0; i < dataSet.clients.size(); i++) {
            if (dataSet.clients.get(i).getId().equals(client.getId())) {
                dataSet.clients.set(i, client);
                return;
            }
        }
    }
    
    public void deleteClient(Long id) {
        dataSet.clients.removeIf(c -> c.getId().equals(id));
    }
    
    // ==================== ÉQUIPEMENTS ====================
    
    public List<Equipment> getAllEquipments() {
        return new ArrayList<>(dataSet.equipments);
    }
    
    public Equipment getEquipmentById(Long id) {
        return dataSet.equipments.stream()
            .filter(e -> e.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Equipment> searchEquipments(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllEquipments();
        }
        String lowerQuery = query.toLowerCase();
        return dataSet.equipments.stream()
            .filter(e -> 
                (e.getName() != null && e.getName().toLowerCase().contains(lowerQuery)) ||
                (e.getBrand() != null && e.getBrand().toLowerCase().contains(lowerQuery)) ||
                (e.getSerialNumber() != null && e.getSerialNumber().toLowerCase().contains(lowerQuery))
            )
            .collect(Collectors.toList());
    }
    
    public List<Equipment> getEquipmentsByCategory(String category) {
        return dataSet.equipments.stream()
            .filter(e -> e.getCategory() != null && e.getCategory().equalsIgnoreCase(category))
            .collect(Collectors.toList());
    }
    
    public void addEquipment(Equipment equipment) {
        if (equipment.getId() == null) {
            long maxId = dataSet.equipments.stream().mapToLong(Equipment::getId).max().orElse(0);
            equipment.setId(maxId + 1);
        }
        dataSet.equipments.add(equipment);
    }
    
    public void updateEquipment(Equipment equipment) {
        for (int i = 0; i < dataSet.equipments.size(); i++) {
            if (dataSet.equipments.get(i).getId().equals(equipment.getId())) {
                dataSet.equipments.set(i, equipment);
                return;
            }
        }
    }
    
    public void deleteEquipment(Long id) {
        dataSet.equipments.removeIf(e -> e.getId().equals(id));
    }
    
    // ==================== CONTRATS ====================
    
    public List<Contract> getAllContracts() {
        return new ArrayList<>(dataSet.contracts);
    }
    
    public Contract getContractById(Long id) {
        return dataSet.contracts.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Contract> getContractsByClient(Long clientId) {
        return dataSet.contracts.stream()
            .filter(c -> c.getClientId() != null && c.getClientId().equals(clientId))
            .collect(Collectors.toList());
    }
    
    public void addContract(Contract contract) {
        if (contract.getId() == null) {
            long maxId = dataSet.contracts.stream().mapToLong(Contract::getId).max().orElse(0);
            contract.setId(maxId + 1);
        }
        dataSet.contracts.add(contract);
    }
    
    public void updateContract(Contract contract) {
        for (int i = 0; i < dataSet.contracts.size(); i++) {
            if (dataSet.contracts.get(i).getId().equals(contract.getId())) {
                dataSet.contracts.set(i, contract);
                return;
            }
        }
    }
    
    public void deleteContract(Long id) {
        dataSet.contracts.removeIf(c -> c.getId().equals(id));
    }
    
    // ==================== DEMANDES SAV ====================
    
    public List<ServiceRequest> getAllServiceRequests() {
        return new ArrayList<>(dataSet.serviceRequests);
    }
    
    public ServiceRequest getServiceRequestById(Long id) {
        return dataSet.serviceRequests.stream()
            .filter(sr -> sr.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<ServiceRequest> getServiceRequestsByStatus(ServiceRequest.ServiceRequestStatus status) {
        return dataSet.serviceRequests.stream()
            .filter(sr -> sr.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    public void addServiceRequest(ServiceRequest request) {
        if (request.getId() == null) {
            long maxId = dataSet.serviceRequests.stream().mapToLong(ServiceRequest::getId).max().orElse(0);
            request.setId(maxId + 1);
        }
        dataSet.serviceRequests.add(request);
    }
    
    public void updateServiceRequest(ServiceRequest request) {
        for (int i = 0; i < dataSet.serviceRequests.size(); i++) {
            if (dataSet.serviceRequests.get(i).getId().equals(request.getId())) {
                dataSet.serviceRequests.set(i, request);
                return;
            }
        }
    }
    
    public void deleteServiceRequest(Long id) {
        dataSet.serviceRequests.removeIf(sr -> sr.getId().equals(id));
    }
    
    // ==================== VÉHICULES ====================
    
    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(dataSet.vehicles);
    }
    
    public Vehicle getVehicleById(Long id) {
        return dataSet.vehicles.stream()
            .filter(v -> v.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Vehicle> getAvailableVehicles() {
        return dataSet.vehicles.stream()
            .filter(v -> "Disponible".equalsIgnoreCase(v.getStatus()))
            .collect(Collectors.toList());
    }
    
    public void addVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            long maxId = dataSet.vehicles.stream().mapToLong(Vehicle::getId).max().orElse(0);
            vehicle.setId(maxId + 1);
        }
        dataSet.vehicles.add(vehicle);
    }
    
    public void updateVehicle(Vehicle vehicle) {
        for (int i = 0; i < dataSet.vehicles.size(); i++) {
            if (dataSet.vehicles.get(i).getId().equals(vehicle.getId())) {
                dataSet.vehicles.set(i, vehicle);
                return;
            }
        }
    }
    
    public void deleteVehicle(Long id) {
        dataSet.vehicles.removeIf(v -> v.getId().equals(id));
    }
    
    // ==================== PROJETS ====================
    
    public List<Project> getAllProjects() {
        return new ArrayList<>(dataSet.projects);
    }
    
    public Project getProjectById(Long id) {
        return dataSet.projects.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Project> getProjectsByClient(Long clientId) {
        return dataSet.projects.stream()
            .filter(p -> p.getClient() != null && p.getClient().getId().equals(clientId))
            .collect(Collectors.toList());
    }
    
    public void addProject(Project project) {
        if (project.getId() == null) {
            long maxId = dataSet.projects.stream().mapToLong(Project::getId).max().orElse(0);
            project.setId(maxId + 1);
        }
        dataSet.projects.add(project);
    }
    
    public void updateProject(Project project) {
        for (int i = 0; i < dataSet.projects.size(); i++) {
            if (dataSet.projects.get(i).getId().equals(project.getId())) {
                dataSet.projects.set(i, project);
                return;
            }
        }
    }
    
    public void deleteProject(Long id) {
        dataSet.projects.removeIf(p -> p.getId().equals(id));
    }
    
    // ==================== PERSONNEL ====================
    
    public List<Personnel> getAllPersonnel() {
        return new ArrayList<>(dataSet.personnel);
    }
    
    public Personnel getPersonnelById(Long id) {
        return dataSet.personnel.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    public List<Personnel> getActivePersonnel() {
        return dataSet.personnel.stream()
            .filter(p -> Boolean.TRUE.equals(p.getActive()))
            .collect(Collectors.toList());
    }
    
    public void addPersonnel(Personnel personnel) {
        if (personnel.getId() == null) {
            long maxId = dataSet.personnel.stream().mapToLong(Personnel::getId).max().orElse(0);
            personnel.setId(maxId + 1);
        }
        dataSet.personnel.add(personnel);
    }
    
    public void updatePersonnel(Personnel personnel) {
        for (int i = 0; i < dataSet.personnel.size(); i++) {
            if (dataSet.personnel.get(i).getId().equals(personnel.getId())) {
                dataSet.personnel.set(i, personnel);
                return;
            }
        }
    }
    
    public void deletePersonnel(Long id) {
        dataSet.personnel.removeIf(p -> p.getId().equals(id));
    }
    
    // ==================== STATISTIQUES ====================
    
    public int getTotalClients() {
        return dataSet.clients.size();
    }
    
    public int getActiveClients() {
        return (int) dataSet.clients.stream()
            .filter(c -> c.getStatus() == Client.ClientStatus.ACTIVE)
            .count();
    }
    
    public int getTotalEquipments() {
        return dataSet.equipments.size();
    }
    
    public int getAvailableEquipmentsCount() {
        return (int) dataSet.equipments.stream()
            .filter(e -> e.getStatus() == Equipment.EquipmentStatus.AVAILABLE)
            .count();
    }
    
    public int getPendingServiceRequests() {
        return (int) dataSet.serviceRequests.stream()
            .filter(sr -> sr.getStatus() == ServiceRequest.ServiceRequestStatus.OPEN || 
                         sr.getStatus() == ServiceRequest.ServiceRequestStatus.IN_PROGRESS)
            .count();
    }
    
    public int getActiveContracts() {
        return (int) dataSet.contracts.stream()
            .filter(c -> c.getStatus() == Contract.ContractStatus.ACTIVE)
            .count();
    }
}
