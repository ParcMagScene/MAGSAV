package com.magsav.gui.hub;

import com.magsav.util.AppLogger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;

public class DemandesHubController implements Initializable {
  
  // Tables
  @FXML private TableView<RequestData> partRequestsTable;
  @FXML private TableView<RequestData> equipmentRequestsTable;
  
  // Colonnes Demandes de Pièces
  @FXML private TableColumn<RequestData, Long> colPartRequestId;
  @FXML private TableColumn<RequestData, String> colPartRequestDate, colPartRequestClient, colPartRequestDescription, colPartRequestQuantity, colPartRequestStatus, colPartRequestPriority, colPartRequestAssignee;
  
  // Colonnes Demandes de Matériel
  @FXML private TableColumn<RequestData, Long> colEquipmentRequestId;
  @FXML private TableColumn<RequestData, String> colEquipmentRequestDate, colEquipmentRequestClient, colEquipmentRequestType, colEquipmentRequestDescription, colEquipmentRequestStatus, colEquipmentRequestAssignee;
  
  // Champs de recherche
  @FXML private TextField partRequestSearchField, equipmentRequestSearchField;
  
  // Boutons
  @FXML private Button btnEditPartRequest, btnDeletePartRequest;
  @FXML private Button btnEditEquipmentRequest, btnDeleteEquipmentRequest;
  
  // Labels de statistiques
  @FXML private Label lblTotalPartRequests, lblPendingPartRequests;
  @FXML private Label lblTotalEquipmentRequests, lblPendingEquipmentRequests;
  @FXML private Label lblStatus;
  
  // Données simulées
  private final ObservableList<RequestData> partRequestsData = FXCollections.observableArrayList();
  private final ObservableList<RequestData> equipmentRequestsData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupTables();
    loadSampleData();
    updateStatistics();
    updateStatus("Hub demandes initialisé");
  }
  
  private void setupTables() {
    setupPartRequestsTable();
    setupEquipmentRequestsTable();
  }
  
  private void setupPartRequestsTable() {
    if (colPartRequestId != null) colPartRequestId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id));
    if (colPartRequestDate != null) colPartRequestDate.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().date));
    if (colPartRequestClient != null) colPartRequestClient.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().client));
    if (colPartRequestDescription != null) colPartRequestDescription.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().description));
    if (colPartRequestQuantity != null) colPartRequestQuantity.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().quantity));
    if (colPartRequestStatus != null) colPartRequestStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().status));
    if (colPartRequestPriority != null) colPartRequestPriority.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().priority));
    if (colPartRequestAssignee != null) colPartRequestAssignee.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().assignee));
    
    if (partRequestsTable != null) {
      partRequestsTable.setItems(partRequestsData);
      partRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        if (btnEditPartRequest != null) btnEditPartRequest.setDisable(!hasSelection);
        if (btnDeletePartRequest != null) btnDeletePartRequest.setDisable(!hasSelection);
      });
    }
  }
  
  private void setupEquipmentRequestsTable() {
    if (colEquipmentRequestId != null) colEquipmentRequestId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id));
    if (colEquipmentRequestDate != null) colEquipmentRequestDate.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().date));
    if (colEquipmentRequestClient != null) colEquipmentRequestClient.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().client));
    if (colEquipmentRequestType != null) colEquipmentRequestType.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().type));
    if (colEquipmentRequestDescription != null) colEquipmentRequestDescription.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().description));
    if (colEquipmentRequestStatus != null) colEquipmentRequestStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().status));
    if (colEquipmentRequestAssignee != null) colEquipmentRequestAssignee.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().assignee));
    
    if (equipmentRequestsTable != null) {
      equipmentRequestsTable.setItems(equipmentRequestsData);
      equipmentRequestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        if (btnEditEquipmentRequest != null) btnEditEquipmentRequest.setDisable(!hasSelection);
        if (btnDeleteEquipmentRequest != null) btnDeleteEquipmentRequest.setDisable(!hasSelection);
      });
    }
  }
  
  private void loadSampleData() {
    // Données d'exemple pour les demandes de pièces
    partRequestsData.add(new RequestData(1L, "2025-10-08", "ABC Corp", "Écran LCD 15 pouces", "2", "En attente", "Haute", "J. Dupont"));
    partRequestsData.add(new RequestData(2L, "2025-10-07", "XYZ Ltd", "Disque dur SSD 500Go", "1", "En cours", "Moyenne", "M. Martin"));
    partRequestsData.add(new RequestData(3L, "2025-10-06", "Tech Solutions", "Carte mère ATX", "1", "Terminé", "Basse", "A. Bernard"));
    
    // Données d'exemple pour les demandes de matériel
    equipmentRequestsData.add(new RequestData(1L, "2025-10-08", "StartupCo", "Ordinateur portable", "Configuration portable haute performance", "En attente", "P. Moreau"));
    equipmentRequestsData.add(new RequestData(2L, "2025-10-07", "MegaCorp", "Serveur", "Serveur de stockage NAS 8TB", "En cours", "L. Dubois"));
    equipmentRequestsData.add(new RequestData(3L, "2025-10-05", "SmallBiz", "Imprimante", "Imprimante laser couleur A3", "Terminé", "R. Petit"));
  }
  
  private void updateStatistics() {
    // Statistiques demandes de pièces
    if (lblTotalPartRequests != null) lblTotalPartRequests.setText(String.valueOf(partRequestsData.size()));
    long pendingParts = partRequestsData.stream().filter(r -> "En attente".equals(r.status)).count();
    if (lblPendingPartRequests != null) lblPendingPartRequests.setText(String.valueOf(pendingParts));
    
    // Statistiques demandes de matériel
    if (lblTotalEquipmentRequests != null) lblTotalEquipmentRequests.setText(String.valueOf(equipmentRequestsData.size()));
    long pendingEquipment = equipmentRequestsData.stream().filter(r -> "En attente".equals(r.status)).count();
    if (lblPendingEquipmentRequests != null) lblPendingEquipmentRequests.setText(String.valueOf(pendingEquipment));
  }
  
  private void updateStatus(String message) {
    if (lblStatus != null) {
      lblStatus.setText(message);
    }
    AppLogger.info("DemandesHub: " + message);
  }
  
  // Actions pour demandes de pièces
  @FXML private void onNewPartRequest() { AppLogger.info("Nouvelle demande de pièce"); }
  @FXML private void onEditPartRequest() { AppLogger.info("Édition demande de pièce"); }
  @FXML private void onDeletePartRequest() { AppLogger.info("Suppression demande de pièce"); }
  @FXML private void onClearPartRequestSearch() { 
    if (partRequestSearchField != null) partRequestSearchField.clear(); 
  }
  
  // Actions pour demandes de matériel
  @FXML private void onNewEquipmentRequest() { AppLogger.info("Nouvelle demande de matériel"); }
  @FXML private void onEditEquipmentRequest() { AppLogger.info("Édition demande de matériel"); }
  @FXML private void onDeleteEquipmentRequest() { AppLogger.info("Suppression demande de matériel"); }
  @FXML private void onClearEquipmentRequestSearch() { 
    if (equipmentRequestSearchField != null) equipmentRequestSearchField.clear(); 
  }
  
  // Actions générales
  @FXML private void onRefresh() {
    loadSampleData();
    updateStatistics();
    updateStatus("Données actualisées");
  }
  
  @FXML private void onClose() {
    if (partRequestsTable != null && partRequestsTable.getScene() != null && partRequestsTable.getScene().getWindow() != null) {
      partRequestsTable.getScene().getWindow().hide();
    }
  }
  
  // Classe de données pour les demandes
  public static class RequestData {
    public final Long id;
    public final String date;
    public final String client;
    public final String description;
    public final String quantity;
    public final String status;
    public final String priority;
    public final String assignee;
    public final String type; // Pour les demandes de matériel
    
    // Constructeur pour demandes de pièces
    public RequestData(Long id, String date, String client, String description, String quantity, String status, String priority, String assignee) {
      this.id = id;
      this.date = date;
      this.client = client;
      this.description = description;
      this.quantity = quantity;
      this.status = status;
      this.priority = priority;
      this.assignee = assignee;
      this.type = null;
    }
    
    // Constructeur pour demandes de matériel
    public RequestData(Long id, String date, String client, String type, String description, String status, String assignee) {
      this.id = id;
      this.date = date;
      this.client = client;
      this.type = type;
      this.description = description;
      this.status = status;
      this.assignee = assignee;
      this.quantity = null;
      this.priority = null;
    }
  }
}