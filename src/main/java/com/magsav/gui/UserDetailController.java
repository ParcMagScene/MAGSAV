package com.magsav.gui;

import com.magsav.db.DB;
import com.magsav.util.AppLogger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la vue de détail d'un utilisateur
 * Affiche toutes les informations d'un utilisateur et ses permissions
 */
public class UserDetailController implements Initializable {
    
    // Labels pour les informations utilisateur
    @FXML private Label lblId;
    @FXML private Label lblUsername;
    @FXML private Label lblFullName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblPosition;
    @FXML private Label lblRole;
    @FXML private Label lblSociete;
    @FXML private Label lblStatus;
    @FXML private Label lblCreatedAt;
    @FXML private Label lblLastLogin;
    
    // Zone de texte pour les permissions
    @FXML private TextArea txtPermissions;
    
    // Table pour l'activité
    @FXML private TableView<ActivityData> tableActivity;
    @FXML private TableColumn<ActivityData, String> colActivityDate;
    @FXML private TableColumn<ActivityData, String> colActivityAction;
    @FXML private TableColumn<ActivityData, String> colActivityDetails;
    
    // Boutons
    @FXML private Button btnEdit;
    @FXML private Button btnResetPassword;
    @FXML private Button btnDeactivate;
    @FXML private Button btnClose;
    
    private int currentUserId;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupPermissionsDisplay();
        setupActivityTable();
        setupButtons();
    }
    
    /**
     * Configure la zone de texte des permissions (pas de table dans ce FXML)
     */
    private void setupPermissionsDisplay() {
        txtPermissions.setEditable(false);
        txtPermissions.setWrapText(true);
    }
    
    /**
     * Configure le tableau d'activité
     */
    private void setupActivityTable() {
        colActivityDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().date()));
        colActivityAction.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().action()));
        colActivityDetails.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().details()));
    }
    
    /**
     * Configure les boutons
     */
    private void setupButtons() {
        btnEdit.setOnAction(e -> editUser());
        btnResetPassword.setOnAction(e -> resetPassword());
        btnDeactivate.setOnAction(e -> toggleUserStatus(false));
        btnClose.setOnAction(e -> closeWindow());
    }
    
    /**
     * Charge un utilisateur par son ID
     */
    public void loadUser(int userId) {
        this.currentUserId = userId;
        try {
            UserData userData = loadUserData(userId);
            if (userData != null) {
                displayUserData(userData);
                loadPermissions(userData.role());
                loadActivity(userId);
            } else {
                AppLogger.error("Utilisateur non trouvé: " + userId);
                showError("Utilisateur non trouvé");
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors du chargement de l'utilisateur: " + e.getMessage(), e);
            showError("Erreur lors du chargement: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données d'un utilisateur depuis la base de données
     */
    private UserData loadUserData(int userId) throws SQLException {
        String sql = """
            SELECT u.id, u.username, u.email, u.nom, u.prenom, u.telephone, u.role, 
                   u.specialite, u.is_active, u.created_at, u.last_login
            FROM users u
            WHERE u.id = ?
            """;
            
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String fullName = (rs.getString("nom") + " " + rs.getString("prenom")).trim();
                    return new UserData(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        fullName,
                        rs.getString("telephone"),
                        rs.getString("role"),
                        rs.getString("specialite"),
                        rs.getBoolean("is_active"),
                        rs.getString("created_at"),
                        rs.getString("last_login"),
                        null // societe_name non disponible
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Affiche les données de l'utilisateur dans l'interface
     */
    private void displayUserData(UserData userData) {
        lblId.setText(String.valueOf(userData.id()));
        lblUsername.setText(userData.username());
        lblEmail.setText(userData.email());
        lblFullName.setText(userData.fullName() != null ? userData.fullName() : "Non renseigné");
        lblPhone.setText(userData.phone() != null ? userData.phone() : "Non renseigné");
        lblRole.setText(getRoleDisplayName(userData.role()));
        lblPosition.setText(userData.position() != null ? userData.position() : "Non renseigné");
        lblSociete.setText(userData.societeName() != null ? userData.societeName() : "Aucune");
        
        // Statut avec couleur
        lblStatus.setText(userData.isActive() ? "Actif" : "Inactif");
        lblStatus.setStyle(userData.isActive() ? 
            "-fx-text-fill: #2ecc71; -fx-font-weight: bold;" : 
            "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            
        lblCreatedAt.setText(formatDate(userData.createdAt()));
        String lastLogin = userData.lastLogin() != null ? formatDate(userData.lastLogin()) : "Jamais connecté";
        lblLastLogin.setText(lastLogin);
        
        // Bouton selon le statut - on n'a qu'un bouton désactiver dans ce FXML
        btnDeactivate.setText(userData.isActive() ? "🚫 Désactiver" : "✅ Activer");
    }
    
    /**
     * Charge les permissions selon le rôle dans la zone de texte
     */
    private void loadPermissions(String role) {
        StringBuilder permissionsText = new StringBuilder();
        
        switch (role) {
            case "ADMIN":
                permissionsText.append("✅ Gestion utilisateurs - Créer, modifier, supprimer des utilisateurs\n");
                permissionsText.append("✅ Gestion produits - Accès complet aux produits et stock\n");
                permissionsText.append("✅ Gestion interventions - Créer et gérer toutes les interventions\n");
                permissionsText.append("✅ Rapports - Accès à tous les rapports et statistiques\n");
                permissionsText.append("✅ Configuration - Modifier les paramètres système");
                break;
                
            case "TECHNICIEN_MAG_SCENE":
                permissionsText.append("✅ Gestion interventions - Créer et gérer ses interventions\n");
                permissionsText.append("✅ Consultation produits - Voir les produits et stock\n");
                permissionsText.append("✅ Rapports techniques - Rapports d'intervention\n");
                permissionsText.append("❌ Gestion utilisateurs - Accès limité\n");
                permissionsText.append("❌ Configuration - Pas d'accès aux paramètres");
                break;
                
            case "INTERMITTENT":
                permissionsText.append("✅ Consultation - Voir les données en lecture seule\n");
                permissionsText.append("✅ Interventions limitées - Interventions sur projets assignés\n");
                permissionsText.append("❌ Gestion complète - Accès limité aux fonctionnalités\n");
                permissionsText.append("❌ Rapports - Pas d'accès aux rapports\n");
                permissionsText.append("❌ Configuration - Pas d'accès aux paramètres");
                break;
                
            default: // USER
                permissionsText.append("✅ Consultation - Voir les données de base\n");
                permissionsText.append("✅ Demandes - Créer des demandes d'intervention\n");
                permissionsText.append("❌ Gestion - Pas d'accès aux fonctions de gestion\n");
                permissionsText.append("❌ Rapports - Pas d'accès aux rapports\n");
                permissionsText.append("❌ Configuration - Pas d'accès aux paramètres");
        }
        
        txtPermissions.setText(permissionsText.toString());
    }
    
    /**
     * Charge l'activité récente de l'utilisateur
     */
    private void loadActivity(int userId) {
        ObservableList<ActivityData> activities = FXCollections.observableArrayList();
        
        // Simulation de données d'activité récente
        activities.addAll(
            new ActivityData("14/10/2025 14:30", "Connexion", "Connexion à l'application"),
            new ActivityData("14/10/2025 13:15", "Consultation", "Consultation liste produits"),
            new ActivityData("13/10/2025 16:45", "Modification", "Mise à jour profil utilisateur"),
            new ActivityData("13/10/2025 10:20", "Création", "Nouvelle demande d'intervention"),
            new ActivityData("12/10/2025 15:30", "Consultation", "Consultation historique interventions")
        );
        
        tableActivity.setItems(activities);
    }
    
    /**
     * Modifie l'utilisateur
     */
    private void editUser() {
        // TODO: Ouvrir un formulaire d'édition
        showInfo("Fonction d'édition à implémenter");
    }
    
    /**
     * Réinitialise le mot de passe
     */
    private void resetPassword() {
        // TODO: Implémenter la réinitialisation du mot de passe
        showInfo("Fonction de réinitialisation de mot de passe à implémenter");
    }
    
    /**
     * Active/désactive l'utilisateur
     */
    private void toggleUserStatus(boolean activate) {
        try {
            String sql = "UPDATE users SET is_active = ? WHERE id = ?";
            try (Connection conn = DB.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setBoolean(1, activate);
                stmt.setInt(2, currentUserId);
                
                int updated = stmt.executeUpdate();
                if (updated > 0) {
                    String message = activate ? "Utilisateur activé" : "Utilisateur désactivé";
                    showInfo(message);
                    loadUser(currentUserId); // Recharger les données
                } else {
                    showError("Erreur lors de la mise à jour du statut");
                }
            }
        } catch (SQLException e) {
            AppLogger.error("Erreur lors du changement de statut: " + e.getMessage(), e);
            showError("Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Ferme la fenêtre
     */
    private void closeWindow() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
    
    /**
     * Méthode appelée par le FXML pour fermer la fenêtre
     */
    @FXML
    private void onClose() {
        closeWindow();
    }
    
    /**
     * Retourne le nom d'affichage du rôle
     */
    private String getRoleDisplayName(String role) {
        return switch (role) {
            case "ADMIN" -> "Administrateur";
            case "TECHNICIEN_MAG_SCENE" -> "Technicien Mag Scène";
            case "INTERMITTENT" -> "Intermittent";
            case "USER" -> "Utilisateur";
            default -> role;
        };
    }
    
    /**
     * Formate une date pour l'affichage
     */
    private String formatDate(String dateStr) {
        if (dateStr == null) return "Non renseigné";
        // TODO: Formatage plus sophistiqué si nécessaire
        return dateStr;
    }
    
    /**
     * Affiche un message d'information
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Record pour les données utilisateur
     */
    public record UserData(
        int id,
        String username,
        String email,
        String fullName,
        String phone,
        String role,
        String position,
        boolean isActive,
        String createdAt,
        String lastLogin,
        String societeName
    ) {}
    
    /**
     * Record pour les données de permission
     */
    public record PermissionData(
        String permission,
        String status,
        String description
    ) {}
    
    /**
     * Record pour les données d'activité
     */
    public record ActivityData(
        String date,
        String action,
        String details
    ) {}
}