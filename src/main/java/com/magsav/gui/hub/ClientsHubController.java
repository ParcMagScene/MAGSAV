package com.magsav.gui.hub;

import com.magsav.model.Company;
import com.magsav.repo.CompanyRepository;
import com.magsav.util.AppLogger;
import com.magsav.db.DB;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;

public class ClientsHubController implements Initializable {
  
  // Table et colonnes
  @FXML private TableView<Company> clientsTable;
  @FXML private TableColumn<Company, Long> colClientId;
  @FXML private TableColumn<Company, String> colClientName, colClientEmail, colClientPhone, colClientAddress, colClientCity, colClientStatus;
  
  // Champs et boutons
  @FXML private TextField clientSearchField;
  @FXML private Button btnEditClient, btnDeleteClient;
  @FXML private Label lblTotalClients, lblActiveClients, lblStatus;
  
  // Données
  private final ObservableList<Company> clientsData = FXCollections.observableArrayList();
  private FilteredList<Company> filteredClients;
  private CompanyRepository companyRepository;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      companyRepository = new CompanyRepository(DB.getConnection());
      setupTable();
      setupSearchFilter();
      loadData();
      updateStatistics();
      updateStatus("Hub clients initialisé");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'initialisation du hub clients: " + e.getMessage(), e);
      updateStatus("Erreur d'initialisation");
    }
  }
  
  private void setupTable() {
    if (colClientId != null) colClientId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
    if (colClientName != null) colClientName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
    if (colClientEmail != null) colClientEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
    if (colClientPhone != null) colClientPhone.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhone()));
    if (colClientAddress != null) colClientAddress.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getAddress()));
    if (colClientCity != null) colClientCity.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getCity()));
    if (colClientStatus != null) colClientStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper("Actif"));
    
    if (clientsTable != null) {
      clientsTable.setItems(clientsData);
      clientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        if (btnEditClient != null) btnEditClient.setDisable(!hasSelection);
        if (btnDeleteClient != null) btnDeleteClient.setDisable(!hasSelection);
      });
    }
  }
  
  private void setupSearchFilter() {
    filteredClients = new FilteredList<>(clientsData);
    if (clientsTable != null) clientsTable.setItems(filteredClients);
    
    if (clientSearchField != null) {
      clientSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
        filteredClients.setPredicate(client -> {
          if (newVal == null || newVal.isEmpty()) return true;
          String lowerCaseFilter = newVal.toLowerCase();
          return client.getName().toLowerCase().contains(lowerCaseFilter) ||
                 (client.getEmail() != null && client.getEmail().toLowerCase().contains(lowerCaseFilter)) ||
                 (client.getPhone() != null && client.getPhone().toLowerCase().contains(lowerCaseFilter));
        });
      });
    }
  }
  
  private void loadData() {
    try {
      List<Company> clients = companyRepository.findByType(Company.CompanyType.CLIENT);
      clientsData.setAll(clients);
      updateStatus("Données chargées: " + clients.size() + " clients");
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des clients: " + e.getMessage(), e);
      updateStatus("Erreur lors du chargement");
    }
  }
  
  private void updateStatistics() {
    if (lblTotalClients != null) lblTotalClients.setText(String.valueOf(clientsData.size()));
    if (lblActiveClients != null) lblActiveClients.setText(String.valueOf(clientsData.size())); // Tous actifs pour l'instant
  }
  
  private void updateStatus(String message) {
    if (lblStatus != null) {
      lblStatus.setText(message);
    }
    AppLogger.info("ClientsHub: " + message);
  }
  
  // Actions FXML
  @FXML private void onNewClient() { 
    AppLogger.info("Nouveau client demandé"); 
  }
  
  @FXML private void onEditClient() { 
    Company selected = clientsTable != null ? clientsTable.getSelectionModel().getSelectedItem() : null;
    if (selected != null) {
      AppLogger.info("Édition client: " + selected.getName());
    }
  }
  
  @FXML private void onDeleteClient() { 
    Company selected = clientsTable != null ? clientsTable.getSelectionModel().getSelectedItem() : null;
    if (selected != null) {
      AppLogger.info("Suppression client: " + selected.getName());
    }
  }
  
  @FXML private void onClearClientSearch() { 
    if (clientSearchField != null) clientSearchField.clear(); 
  }
  
  @FXML private void onExportClients() { 
    AppLogger.info("Export clients demandé"); 
  }
  
  @FXML private void onRefresh() {
    loadData();
    updateStatistics();
    updateStatus("Données actualisées");
  }
  
  @FXML private void onClose() {
    if (clientsTable != null && clientsTable.getScene() != null && clientsTable.getScene().getWindow() != null) {
      clientsTable.getScene().getWindow().hide();
    }
  }
}