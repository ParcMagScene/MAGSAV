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

public class UsersHubController implements Initializable {
  
  // Tables
  @FXML private TableView<UserData> usersTable;
  @FXML private TableView<AdminData> adminsTable;
  
  // Colonnes Utilisateurs Standard
  @FXML private TableColumn<UserData, Long> colUserId;
  @FXML private TableColumn<UserData, String> colUserLogin, colUserFirstName, colUserLastName, colUserEmail, colUserRole, colUserStatus, colUserLastLogin, colUserCreatedDate;
  
  // Colonnes Administrateurs
  @FXML private TableColumn<AdminData, Long> colAdminId;
  @FXML private TableColumn<AdminData, String> colAdminLogin, colAdminFirstName, colAdminLastName, colAdminEmail, colAdminLevel, colAdminPermissions, colAdminStatus, colAdminLastLogin;
  
  // Champs de recherche
  @FXML private TextField userSearchField, adminSearchField;
  
  // Boutons
  @FXML private Button btnEditUser, btnDeleteUser;
  @FXML private Button btnEditAdmin, btnDeleteAdmin;
  
  // Labels de statistiques
  @FXML private Label lblTotalUsers, lblActiveUsers, lblTotalAdmins;
  @FXML private Label lblStatus;
  
  // Données simulées
  private final ObservableList<UserData> usersData = FXCollections.observableArrayList();
  private final ObservableList<AdminData> adminsData = FXCollections.observableArrayList();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupTables();
    loadSampleData();
    updateStatistics();
    updateStatus("Hub utilisateurs initialisé");
  }
  
  private void setupTables() {
    setupUsersTable();
    setupAdminsTable();
  }
  
  private void setupUsersTable() {
    if (colUserId != null) colUserId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id));
    if (colUserLogin != null) colUserLogin.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().login));
    if (colUserFirstName != null) colUserFirstName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().firstName));
    if (colUserLastName != null) colUserLastName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().lastName));
    if (colUserEmail != null) colUserEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().email));
    if (colUserRole != null) colUserRole.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().role));
    if (colUserStatus != null) colUserStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().status));
    if (colUserLastLogin != null) colUserLastLogin.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().lastLogin));
    if (colUserCreatedDate != null) colUserCreatedDate.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().createdDate));
    
    if (usersTable != null) {
      usersTable.setItems(usersData);
      usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        if (btnEditUser != null) btnEditUser.setDisable(!hasSelection);
        if (btnDeleteUser != null) btnDeleteUser.setDisable(!hasSelection);
      });
    }
  }
  
  private void setupAdminsTable() {
    if (colAdminId != null) colAdminId.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().id));
    if (colAdminLogin != null) colAdminLogin.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().login));
    if (colAdminFirstName != null) colAdminFirstName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().firstName));
    if (colAdminLastName != null) colAdminLastName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().lastName));
    if (colAdminEmail != null) colAdminEmail.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().email));
    if (colAdminLevel != null) colAdminLevel.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().adminLevel));
    if (colAdminPermissions != null) colAdminPermissions.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().permissions));
    if (colAdminStatus != null) colAdminStatus.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().status));
    if (colAdminLastLogin != null) colAdminLastLogin.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().lastLogin));
    
    if (adminsTable != null) {
      adminsTable.setItems(adminsData);
      adminsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
        boolean hasSelection = newSel != null;
        if (btnEditAdmin != null) btnEditAdmin.setDisable(!hasSelection);
        if (btnDeleteAdmin != null) btnDeleteAdmin.setDisable(!hasSelection);
      });
    }
  }
  
  private void loadSampleData() {
    // Données d'exemple pour les utilisateurs standard
    usersData.add(new UserData(1L, "jdupont", "Jean", "Dupont", "j.dupont@magsav.com", "Technicien", "Actif", "08/10/2025 14:30", "15/01/2025"));
    usersData.add(new UserData(2L, "mmartin", "Marie", "Martin", "m.martin@magsav.com", "Comptable", "Actif", "07/10/2025 16:45", "20/02/2025"));
    usersData.add(new UserData(3L, "pbernard", "Paul", "Bernard", "p.bernard@magsav.com", "Commercial", "Inactif", "05/10/2025 09:15", "10/03/2025"));
    usersData.add(new UserData(4L, "ldurand", "Lucie", "Durand", "l.durand@magsav.com", "Magasinier", "Actif", "08/10/2025 11:20", "25/01/2025"));
    
    // Données d'exemple pour les administrateurs
    adminsData.add(new AdminData(1L, "admin", "Administrateur", "Principal", "admin@magsav.com", "Super Admin", "Toutes", "Actif", "08/10/2025 08:00"));
    adminsData.add(new AdminData(2L, "supervisor", "Sophie", "Superviseur", "s.superviseur@magsav.com", "Admin", "Gestion, SAV", "Actif", "07/10/2025 17:30"));
    adminsData.add(new AdminData(3L, "manager", "Marc", "Manager", "m.manager@magsav.com", "Manager", "Équipes, Produits", "Actif", "06/10/2025 12:45"));
  }
  
  private void updateStatistics() {
    // Statistiques utilisateurs
    if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(usersData.size()));
    long activeUsers = usersData.stream().filter(u -> "Actif".equals(u.status)).count();
    if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(activeUsers));
    
    // Statistiques administrateurs
    if (lblTotalAdmins != null) lblTotalAdmins.setText(String.valueOf(adminsData.size()));
  }
  
  private void updateStatus(String message) {
    if (lblStatus != null) {
      lblStatus.setText(message);
    }
    AppLogger.info("UsersHub: " + message);
  }
  
  // Actions pour utilisateurs standard
  @FXML private void onNewUser() { 
    updateStatus("Création d'un nouvel utilisateur");
    AppLogger.info("Nouveau utilisateur"); 
  }
  
  @FXML private void onEditUser() { 
    UserData selected = usersTable != null ? usersTable.getSelectionModel().getSelectedItem() : null;
    if (selected != null) {
      updateStatus("Édition de l'utilisateur: " + selected.login);
      AppLogger.info("Édition utilisateur: " + selected.login);
    }
  }
  
  @FXML private void onDeleteUser() { 
    UserData selected = usersTable != null ? usersTable.getSelectionModel().getSelectedItem() : null;
    if (selected != null) {
      updateStatus("Suppression de l'utilisateur: " + selected.login);
      AppLogger.info("Suppression utilisateur: " + selected.login);
    }
  }
  
  @FXML private void onSearchUsers() {
    String searchTerm = userSearchField != null ? userSearchField.getText() : "";
    updateStatus("Recherche utilisateurs: " + searchTerm);
  }
  
  @FXML private void onClearUserSearch() { 
    if (userSearchField != null) userSearchField.clear();
    updateStatus("Recherche utilisateurs effacée");
  }
  
  // Actions pour administrateurs
  @FXML private void onNewAdmin() { 
    updateStatus("Création d'un nouvel administrateur");
    AppLogger.info("Nouvel administrateur"); 
  }
  
  @FXML private void onEditAdmin() { 
    AdminData selected = adminsTable != null ? adminsTable.getSelectionModel().getSelectedItem() : null;
    if (selected != null) {
      updateStatus("Édition de l'administrateur: " + selected.login);
      AppLogger.info("Édition administrateur: " + selected.login);
    }
  }
  
  @FXML private void onDeleteAdmin() { 
    AdminData selected = adminsTable != null ? adminsTable.getSelectionModel().getSelectedItem() : null;
    if (selected != null) {
      updateStatus("Désactivation de l'administrateur: " + selected.login);
      AppLogger.info("Désactivation administrateur: " + selected.login);
    }
  }
  
  @FXML private void onSearchAdmins() {
    String searchTerm = adminSearchField != null ? adminSearchField.getText() : "";
    updateStatus("Recherche administrateurs: " + searchTerm);
  }
  
  @FXML private void onClearAdminSearch() { 
    if (adminSearchField != null) adminSearchField.clear();
    updateStatus("Recherche administrateurs effacée");
  }
  
  @FXML private void onManagePermissions() {
    updateStatus("Gestion des permissions système");
    AppLogger.info("Gestion des permissions");
  }
  
  // Actions générales
  @FXML private void onRefresh() {
    loadSampleData();
    updateStatistics();
    updateStatus("Données utilisateurs actualisées");
  }
  
  @FXML private void onClose() {
    if (usersTable != null && usersTable.getScene() != null && usersTable.getScene().getWindow() != null) {
      usersTable.getScene().getWindow().hide();
    }
  }
  
  // Classes de données
  public static class UserData {
    public final Long id;
    public final String login;
    public final String firstName;
    public final String lastName;
    public final String email;
    public final String role;
    public final String status;
    public final String lastLogin;
    public final String createdDate;
    
    public UserData(Long id, String login, String firstName, String lastName, String email, String role, String status, String lastLogin, String createdDate) {
      this.id = id;
      this.login = login;
      this.firstName = firstName;
      this.lastName = lastName;
      this.email = email;
      this.role = role;
      this.status = status;
      this.lastLogin = lastLogin;
      this.createdDate = createdDate;
    }
  }
  
  public static class AdminData {
    public final Long id;
    public final String login;
    public final String firstName;
    public final String lastName;
    public final String email;
    public final String adminLevel;
    public final String permissions;
    public final String status;
    public final String lastLogin;
    
    public AdminData(Long id, String login, String firstName, String lastName, String email, String adminLevel, String permissions, String status, String lastLogin) {
      this.id = id;
      this.login = login;
      this.firstName = firstName;
      this.lastName = lastName;
      this.email = email;
      this.adminLevel = adminLevel;
      this.permissions = permissions;
      this.status = status;
      this.lastLogin = lastLogin;
    }
  }
}