package com.magsav.gui.hub;

import com.magsav.model.Company;
import com.magsav.repo.CompanyRepository;
import com.magsav.util.AppLogger;
import com.magsav.util.CompanyProtectionManager;
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

public class SocietesHubController implements Initializable {
  
  // Tables
  @FXML private TableView<Company> manufacturersTable;
  @FXML private TableView<Company> suppliersTable;
  @FXML private TableView<Company> savTable;
  
  // Colonnes Fabricants
  @FXML private TableColumn<Company, Long> colManufacturerId;
  @FXML private TableColumn<Company, String> colManufacturerName, colManufacturerEmail, colManufacturerPhone, colManufacturerWebsite, colManufacturerCountry, colManufacturerLogo;
  
  // Colonnes Fournisseurs
  @FXML private TableColumn<Company, Long> colSupplierId;
  @FXML private TableColumn<Company, String> colSupplierName, colSupplierEmail, colSupplierPhone, colSupplierAddress, colSupplierSpecialty;
  
  // Colonnes SAV Externes
  @FXML private TableColumn<Company, Long> colSavId;
  @FXML private TableColumn<Company, String> colSavName, colSavEmail, colSavPhone, colSavSpecialties, colSavStatus;
  
  // Champs de recherche
  @FXML private TextField manufacturerSearchField, supplierSearchField, savSearchField;
  
  // Boutons
  @FXML private Button btnEditManufacturer, btnDeleteManufacturer;
  @FXML private Button btnEditSupplier, btnDeleteSupplier;
  @FXML private Button btnEditSav, btnDeleteSav;
  
  // Statut
  @FXML private Label lblStatus;
  
  // Données
  private final ObservableList<Company> manufacturersData = FXCollections.observableArrayList();
  private final ObservableList<Company> suppliersData = FXCollections.observableArrayList();
  private final ObservableList<Company> savData = FXCollections.observableArrayList();
  
  // Filtres
  private FilteredList<Company> filteredManufacturers;
  private FilteredList<Company> filteredSuppliers;
  private FilteredList<Company> filteredSav;
  
  private CompanyRepository companyRepository;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    try {
      companyRepository = new CompanyRepository(DB.getConnection());
      setupTables();
      setupSearchFilters();
      loadAllData();
      updateStatus("Hub sociétés initialisé");
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'initialisation du hub sociétés: " + e.getMessage(), e);
      updateStatus("Erreur d'initialisation");
    }
  }
  
  private void setupTables() {
    setupManufacturersTable();
    setupSuppliersTable();
    setupSavTable();
  }
  
  private void setupManufacturersTable() {
    if (colManufacturerId != null) colManufacturerId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
    if (colManufacturerName != null) colManufacturerName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
    if (colManufacturerEmail != null) colManufacturerEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
    if (colManufacturerPhone != null) colManufacturerPhone.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhone()));
    if (colManufacturerWebsite != null) colManufacturerWebsite.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getWebsite()));
    if (colManufacturerCountry != null) colManufacturerCountry.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getCountry()));
    if (colManufacturerLogo != null) colManufacturerLogo.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getLogoPath() != null ? "✓" : ""));
    
    if (manufacturersTable != null) {
      manufacturersTable.setItems(manufacturersData);
      manufacturersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        boolean isProtected = hasSelection && CompanyProtectionManager.isProtectedCompany(newSel);
        
        if (btnEditManufacturer != null) btnEditManufacturer.setDisable(!hasSelection || isProtected);
        if (btnDeleteManufacturer != null) btnDeleteManufacturer.setDisable(!hasSelection || isProtected);
      });
      
      // Rendre les lignes cliquables pour ouvrir la fiche fabricant
      manufacturersTable.setRowFactory(tv -> {
        TableRow<Company> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
          if (event.getClickCount() == 2 && !row.isEmpty()) {
            Company selectedManufacturer = row.getItem();
            openManufacturerDetail(selectedManufacturer);
          }
        });
        return row;
      });
    }
  }
  
  private void setupSuppliersTable() {
    if (colSupplierId != null) colSupplierId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
    if (colSupplierName != null) colSupplierName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
    if (colSupplierEmail != null) colSupplierEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
    if (colSupplierPhone != null) colSupplierPhone.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhone()));
    if (colSupplierAddress != null) colSupplierAddress.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getAddress()));
    if (colSupplierSpecialty != null) colSupplierSpecialty.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getDescription()));
    
    if (suppliersTable != null) {
      suppliersTable.setItems(suppliersData);
      suppliersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        boolean isProtected = hasSelection && CompanyProtectionManager.isProtectedCompany(newSel);
        
        if (btnEditSupplier != null) btnEditSupplier.setDisable(!hasSelection || isProtected);
        if (btnDeleteSupplier != null) btnDeleteSupplier.setDisable(!hasSelection || isProtected);
      });
      
      // Rendre les lignes cliquables pour ouvrir la fiche fournisseur
      suppliersTable.setRowFactory(tv -> {
        TableRow<Company> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
          if (event.getClickCount() == 2 && !row.isEmpty()) {
            Company selectedSupplier = row.getItem();
            openSupplierDetail(selectedSupplier);
          }
        });
        return row;
      });
    }
  }
  
  private void setupSavTable() {
    if (colSavId != null) colSavId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().getId()));
    if (colSavName != null) colSavName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getName()));
    if (colSavEmail != null) colSavEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getEmail()));
    if (colSavPhone != null) colSavPhone.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getPhone()));
    if (colSavSpecialties != null) colSavSpecialties.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().getDescription()));
    if (colSavStatus != null) colSavStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper("Actif"));
    
    if (savTable != null) {
      savTable.setItems(savData);
      savTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        boolean isProtected = hasSelection && CompanyProtectionManager.isProtectedCompany(newSel);
        
        if (btnEditSav != null) btnEditSav.setDisable(!hasSelection || isProtected);
        if (btnDeleteSav != null) btnDeleteSav.setDisable(!hasSelection || isProtected);
      });
      
      // Rendre les lignes cliquables pour ouvrir la fiche SAV
      savTable.setRowFactory(tv -> {
        TableRow<Company> row = new TableRow<>();
        row.setOnMouseClicked(event -> {
          if (event.getClickCount() == 2 && !row.isEmpty()) {
            Company selectedSav = row.getItem();
            openSavDetail(selectedSav);
          }
        });
        return row;
      });
    }
  }
  
  private void setupSearchFilters() {
    // Filtre Fabricants
    filteredManufacturers = new FilteredList<>(manufacturersData);
    if (manufacturersTable != null) manufacturersTable.setItems(filteredManufacturers);
    
    if (manufacturerSearchField != null) {
      manufacturerSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
        filteredManufacturers.setPredicate(company -> {
          if (newVal == null || newVal.isEmpty()) return true;
          String lowerCaseFilter = newVal.toLowerCase();
          return company.getName().toLowerCase().contains(lowerCaseFilter) ||
                 (company.getEmail() != null && company.getEmail().toLowerCase().contains(lowerCaseFilter));
        });
      });
    }
    
    // Filtre Fournisseurs
    filteredSuppliers = new FilteredList<>(suppliersData);
    if (suppliersTable != null) suppliersTable.setItems(filteredSuppliers);
    
    if (supplierSearchField != null) {
      supplierSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
        filteredSuppliers.setPredicate(company -> {
          if (newVal == null || newVal.isEmpty()) return true;
          String lowerCaseFilter = newVal.toLowerCase();
          return company.getName().toLowerCase().contains(lowerCaseFilter) ||
                 (company.getEmail() != null && company.getEmail().toLowerCase().contains(lowerCaseFilter));
        });
      });
    }
    
    // Filtre SAV
    filteredSav = new FilteredList<>(savData);
    if (savTable != null) savTable.setItems(filteredSav);
    
    if (savSearchField != null) {
      savSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
        filteredSav.setPredicate(company -> {
          if (newVal == null || newVal.isEmpty()) return true;
          String lowerCaseFilter = newVal.toLowerCase();
          return company.getName().toLowerCase().contains(lowerCaseFilter) ||
                 (company.getEmail() != null && company.getEmail().toLowerCase().contains(lowerCaseFilter));
        });
      });
    }
  }
  
  private void loadAllData() {
    try {
      // Charger les fabricants
      List<Company> manufacturers = companyRepository.findByType(Company.CompanyType.MANUFACTURER);
      manufacturersData.setAll(manufacturers);
      
      // Charger les fournisseurs
      List<Company> suppliers = companyRepository.findByType(Company.CompanyType.SUPPLIER);
      suppliersData.setAll(suppliers);
      
      // Charger les SAV (pour l'instant, on utilise un type générique)
      List<Company> savCompanies = companyRepository.findByType(Company.CompanyType.CLIENT); // Temporaire
      savData.setAll(savCompanies);
      
      updateStatus("Données chargées: " + manufacturers.size() + " fabricants, " + suppliers.size() + " fournisseurs");
    } catch (Exception e) {
      AppLogger.error("Erreur lors du chargement des données: " + e.getMessage(), e);
      updateStatus("Erreur lors du chargement");
    }
  }
  
  private void updateStatus(String message) {
    if (lblStatus != null) {
      lblStatus.setText(message);
    }
    AppLogger.info("SocietesHub: " + message);
  }
  
  // Actions pour Fabricants
  @FXML private void onNewManufacturer() { AppLogger.info("Nouveau fabricant demandé"); }
  @FXML private void onEditManufacturer() { AppLogger.info("Édition fabricant demandée"); }
  @FXML private void onDeleteManufacturer() { AppLogger.info("Suppression fabricant demandée"); }
  @FXML private void onClearManufacturerSearch() { 
    if (manufacturerSearchField != null) manufacturerSearchField.clear(); 
  }
  
  // Actions pour Fournisseurs
  @FXML private void onNewSupplier() { AppLogger.info("Nouveau fournisseur demandé"); }
  @FXML private void onEditSupplier() { AppLogger.info("Édition fournisseur demandée"); }
  @FXML private void onDeleteSupplier() { AppLogger.info("Suppression fournisseur demandée"); }
  @FXML private void onClearSupplierSearch() { 
    if (supplierSearchField != null) supplierSearchField.clear(); 
  }
  
  // Actions pour SAV
  @FXML private void onNewSav() { AppLogger.info("Nouveau SAV demandé"); }
  @FXML private void onEditSav() { AppLogger.info("Édition SAV demandée"); }
  @FXML private void onDeleteSav() { AppLogger.info("Suppression SAV demandée"); }
  @FXML private void onClearSavSearch() { 
    if (savSearchField != null) savSearchField.clear(); 
  }
  
  // Actions générales
  @FXML private void onRefresh() {
    loadAllData();
    updateStatus("Données actualisées");
  }
  
  @FXML private void onClose() {
    if (manufacturersTable != null && manufacturersTable.getScene() != null && manufacturersTable.getScene().getWindow() != null) {
      manufacturersTable.getScene().getWindow().hide();
    }
  }
  
  // Méthodes pour ouvrir les fiches détaillées
  private void openManufacturerDetail(Company manufacturer) {
    try {
      AppLogger.info("Ouverture de la fiche fabricant: " + manufacturer.getName() + " (ID: " + manufacturer.getId() + ")");
      
      // Vérifier si la société est protégée
      if (CompanyProtectionManager.isProtectedCompany(manufacturer)) {
        updateStatus("Société protégée : " + manufacturer.getName() + " - Modification uniquement dans les préférences");
        showProtectedCompanyAlert(manufacturer.getName());
        return;
      }
      
      // Pour l'instant, on ne fait que logger. Dans une vraie implémentation,
      // on ouvrirait une nouvelle fenêtre avec ManufacturerDetailController
      updateStatus("Ouverture de la fiche fabricant: " + manufacturer.getName());
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de la fiche fabricant: " + e.getMessage(), e);
      updateStatus("Erreur lors de l'ouverture de la fiche");
    }
  }
  
  private void openSupplierDetail(Company supplier) {
    try {
      AppLogger.info("Ouverture de la fiche fournisseur: " + supplier.getName() + " (ID: " + supplier.getId() + ")");
      
      // Vérifier si la société est protégée
      if (CompanyProtectionManager.isProtectedCompany(supplier)) {
        updateStatus("Société protégée : " + supplier.getName() + " - Modification uniquement dans les préférences");
        showProtectedCompanyAlert(supplier.getName());
        return;
      }
      
      // Pour l'instant, on ne fait que logger. Dans une vraie implémentation,
      // on ouvrirait une nouvelle fenêtre avec SupplierDetailController
      updateStatus("Ouverture de la fiche fournisseur: " + supplier.getName());
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de la fiche fournisseur: " + e.getMessage(), e);
      updateStatus("Erreur lors de l'ouverture de la fiche");
    }
  }
  
  private void openSavDetail(Company sav) {
    try {
      AppLogger.info("Ouverture de la fiche SAV: " + sav.getName() + " (ID: " + sav.getId() + ")");
      
      // Vérifier si la société est protégée
      if (CompanyProtectionManager.isProtectedCompany(sav)) {
        updateStatus("Société protégée : " + sav.getName() + " - Modification uniquement dans les préférences");
        showProtectedCompanyAlert(sav.getName());
        return;
      }
      
      // Pour l'instant, on ne fait que logger. Dans une vraie implémentation,
      // on ouvrirait une nouvelle fenêtre avec SavDetailController
      updateStatus("Ouverture de la fiche SAV: " + sav.getName());
      
    } catch (Exception e) {
      AppLogger.error("Erreur lors de l'ouverture de la fiche SAV: " + e.getMessage(), e);
      updateStatus("Erreur lors de l'ouverture de la fiche");
    }
  }
  
  /**
   * Affiche une alerte informant que la société est protégée
   */
  private void showProtectedCompanyAlert(String companyName) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Société protégée");
    alert.setHeaderText("Modification non autorisée");
    alert.setContentText("La société '" + companyName + "' est protégée et ne peut être modifiée que dans les préférences de l'application.\n\n" +
        "Pour modifier cette société, allez dans :\nFichier → Préférences → Onglet Société");
    alert.showAndWait();
  }
}