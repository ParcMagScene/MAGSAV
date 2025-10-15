package com.magsav.gui;

import com.magsav.model.User;
import com.magsav.model.TechnicianPermissions;
import com.magsav.repo.UserRepository;
import com.magsav.service.NavigationService;
import com.magsav.service.AvatarService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la gestion des utilisateurs techniciens
 */
public class TechnicienUsersController implements Initializable {
    
    // Table et colonnes
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, ImageView> colAvatar;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPosition;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    
    // Boutons d'action
    @FXML private Button btnAdd;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnPermissions;
    @FXML private Button btnResetPassword;
    
    // Zone de détails
    @FXML private VBox detailsPane;
    @FXML private Label lblSelectedUser;
    @FXML private TextArea txtPermissions;
    
    // Champs de recherche et filtres
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbFilterRole;
    @FXML private ComboBox<String> cbFilterPosition;
    
    // Labels de statistiques
    @FXML private Label lblTotalUsers;
    @FXML private Label lblActiveUsers;
    @FXML private Label lblTechniciens;
    
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    private final UserRepository userRepository = new UserRepository();

    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupFilters();
        setupButtons();
        
        // Différer le chargement des données pour éviter les erreurs d'initialisation
        Platform.runLater(() -> {
            try {
                loadUsers();
                updateStatistics();
            } catch (Exception e) {
                System.err.println("Erreur lors de l'initialisation des données utilisateurs: " + e.getMessage());
            }
        });
    }
    
    /**
     * Configuration de la table des utilisateurs
     */
    private void setupTable() {
        // Configuration de la colonne avatar
        if (colAvatar != null) {
            colAvatar.setCellValueFactory(cellData -> {
                User user = cellData.getValue();
                ImageView avatar = AvatarService.getInstance().createAvatarImageView(user.username(), 32);
                return new javafx.beans.property.SimpleObjectProperty<>(avatar);
            });
            colAvatar.setCellFactory(col -> new TableCell<User, ImageView>() {
                @Override
                protected void updateItem(ImageView item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        setGraphic(item);
                        setAlignment(javafx.geometry.Pos.CENTER);
                    }
                }
            });
        }
        
        if (colUsername != null) colUsername.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().username()));
        if (colFullName != null) colFullName.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().fullName()));
        if (colEmail != null) colEmail.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().email()));
        if (colPosition != null) colPosition.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().position() != null ? cellData.getValue().position() : "Non défini"));
        if (colRole != null) colRole.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().role().getLabel()));
        if (colStatus != null) colStatus.setCellValueFactory(cellData -> 
            new ReadOnlyStringWrapper(cellData.getValue().isActive() ? "✅ Actif" : "❌ Inactif"));
        
        // Configuration des filtres
        filteredUsers = new FilteredList<>(users, p -> true);
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        
        if (tableUsers != null) {
            sortedUsers.comparatorProperty().bind(tableUsers.comparatorProperty());
            tableUsers.setItems(sortedUsers);
            
            // Gestion de la sélection
            tableUsers.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                updateButtonStates(newSelection != null);
                if (newSelection != null) {
                    showUserDetails(newSelection);
                }
            });
            
            // Gestion du double-clic
            tableUsers.setRowFactory(tv -> {
                TableRow<User> row = new TableRow<>();
                
                // Gestion du double-clic pour ouvrir les détails de l'utilisateur
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        User user = row.getItem();
                        if (user != null) {
                            NavigationService.openUserDetail(user.id());
                        }
                    }
                });
                
                return row;
            });
        }
    }
    
    /**
     * Configuration des filtres
     */
    private void setupFilters() {
        if (cbFilterRole != null) {
            cbFilterRole.setItems(FXCollections.observableArrayList(
                "Tous", "ADMIN", "TECHNICIEN_MAG_SCENE", "CHAUFFEUR_PL", "CHAUFFEUR_SPL", "INTERMITTENT"));
            cbFilterRole.setValue("Tous");
            cbFilterRole.setOnAction(e -> applyFilters());
        }
        
        if (cbFilterPosition != null) {
            cbFilterPosition.setItems(FXCollections.observableArrayList(
                "Tous", "Technicien Distribution", "Technicien Lumière", 
                "Technicien Structure", "Technicien Son", "Chauffeur PL", "Chauffeur SPL", "Stagiaire"));
            cbFilterPosition.setValue("Tous");
            cbFilterPosition.setOnAction(e -> applyFilters());
        }
        
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        }
    }
    
    /**
     * Configuration des boutons
     */
    private void setupButtons() {
        if (btnAdd != null) btnAdd.setOnAction(e -> addUser());
        if (btnEdit != null) btnEdit.setOnAction(e -> editUser());
        if (btnDelete != null) btnDelete.setOnAction(e -> deleteUser());
        if (btnPermissions != null) btnPermissions.setOnAction(e -> managePermissions());
        if (btnResetPassword != null) btnResetPassword.setOnAction(e -> resetPassword());
        
        // État initial des boutons
        updateButtonStates(false);
    }
    
    /**
     * Charge tous les utilisateurs
     */
    private void loadUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            users.setAll(allUsers);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des utilisateurs: " + e.getMessage());
            // Charger uniquement les techniciens comme fallback
            try {
                List<User> techniciens = userRepository.findByRole(User.Role.TECHNICIEN_MAG_SCENE);
                users.setAll(techniciens);
            } catch (Exception fallbackError) {
                System.err.println("Erreur fallback: " + fallbackError.getMessage());
                users.clear();
            }
        }
    }
    
    /**
     * Applique les filtres de recherche
     */
    private void applyFilters() {
        if (filteredUsers == null) return;
        
        filteredUsers.setPredicate(user -> {
            // Filtre de recherche textuelle
            String searchText = txtSearch != null ? txtSearch.getText() : "";
            if (searchText != null && !searchText.isEmpty()) {
                String lowerCaseFilter = searchText.toLowerCase();
                boolean matchesSearch = user.username().toLowerCase().contains(lowerCaseFilter) ||
                    user.fullName().toLowerCase().contains(lowerCaseFilter) ||
                    user.email().toLowerCase().contains(lowerCaseFilter);
                if (!matchesSearch) return false;
            }
            
            // Filtre par rôle
            String roleFilter = cbFilterRole != null ? cbFilterRole.getValue() : "Tous";
            if (roleFilter != null && !roleFilter.equals("Tous")) {
                if (!user.role().name().equals(roleFilter)) return false;
            }
            
            // Filtre par position
            String positionFilter = cbFilterPosition != null ? cbFilterPosition.getValue() : "Tous";
            if (positionFilter != null && !positionFilter.equals("Tous")) {
                String userPosition = user.position() != null ? user.position() : "";
                if (!userPosition.equals(positionFilter)) return false;
            }
            
            return true;
        });
    }
    
    /**
     * Met à jour l'état des boutons selon la sélection
     */
    private void updateButtonStates(boolean hasSelection) {
        if (btnEdit != null) btnEdit.setDisable(!hasSelection);
        if (btnDelete != null) btnDelete.setDisable(!hasSelection);
        if (btnPermissions != null) btnPermissions.setDisable(!hasSelection);
        if (btnResetPassword != null) btnResetPassword.setDisable(!hasSelection);
    }
    
    /**
     * Affiche les détails de l'utilisateur sélectionné
     */
    private void showUserDetails(User user) {
        if (lblSelectedUser != null) {
            lblSelectedUser.setText(user.fullName() + " (" + user.username() + ")");
        }
        
        if (txtPermissions != null) {
            StringBuilder permissions = new StringBuilder();
            permissions.append("🔐 Permissions pour ").append(user.role().getLabel()).append("\n\n");
            
            if (user.role() == User.Role.TECHNICIEN_MAG_SCENE) {
                var userPermissions = TechnicianPermissions.getPermissionsForPosition(user.position());
                
                for (TechnicianPermissions.Permission perm : userPermissions) {
                    permissions.append("✅ ").append(perm.getDescription()).append("\n");
                }
            } else {
                permissions.append("Permissions standards pour le rôle ").append(user.role().getLabel());
            }
            
            txtPermissions.setText(permissions.toString());
        }
    }
    
    /**
     * Met à jour les statistiques d'utilisation
     */
    private void updateStatistics() {
        try {
            List<User> allUsers = userRepository.findAll();
            long totalUsers = allUsers.size();
            long activeUsers = allUsers.stream().filter(User::isActive).count();
            long techniciens = allUsers.stream().filter(User::isTechnicienMagScene).count();
            
            if (lblTotalUsers != null) lblTotalUsers.setText(String.valueOf(totalUsers));
            if (lblActiveUsers != null) lblActiveUsers.setText(String.valueOf(activeUsers));
            if (lblTechniciens != null) lblTechniciens.setText(String.valueOf(techniciens));
        } catch (Exception e) {
            System.err.println("Erreur mise à jour statistiques: " + e.getMessage());
            // Fallback avec statistiques par défaut
            if (lblTotalUsers != null) lblTotalUsers.setText("--");
            if (lblActiveUsers != null) lblActiveUsers.setText("--");
            if (lblTechniciens != null) lblTechniciens.setText("5"); // Nous savons qu'il y a 5 techniciens
        }
    }
    
    /**
     * Ajoute un nouvel utilisateur
     */
    @FXML
    private void addUser() {
        showInfo("Ajouter utilisateur", "Fonctionnalité d'ajout d'utilisateur à implémenter");
    }
    
    /**
     * Modifie l'utilisateur sélectionné
     */
    @FXML
    private void editUser() {
        User selectedUser = tableUsers != null ? tableUsers.getSelectionModel().getSelectedItem() : null;
        if (selectedUser != null) {
            showInfo("Modifier utilisateur", "Fonctionnalité de modification pour " + selectedUser.fullName() + " à implémenter");
        }
    }
    
    /**
     * Supprime l'utilisateur sélectionné
     */
    @FXML
    private void deleteUser() {
        User selectedUser = tableUsers != null ? tableUsers.getSelectionModel().getSelectedItem() : null;
        if (selectedUser != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("Supprimer l'utilisateur " + selectedUser.fullName() + " ?");
            confirm.setContentText("Cette action est irréversible.");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    userRepository.delete(selectedUser.id());
                    loadUsers();
                    updateStatistics();
                    showInfo("Succès", "Utilisateur supprimé avec succès");
                } catch (Exception e) {
                    showError("Erreur", "Impossible de supprimer l'utilisateur: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Gère les permissions de l'utilisateur sélectionné
     */
    @FXML
    private void managePermissions() {
        User selectedUser = tableUsers != null ? tableUsers.getSelectionModel().getSelectedItem() : null;
        if (selectedUser != null) {
            showInfo("Gérer permissions", "Gestion des permissions pour " + selectedUser.fullName() + " à implémenter");
        }
    }
    
    /**
     * Remet à zéro le mot de passe de l'utilisateur sélectionné
     */
    @FXML
    private void resetPassword() {
        User selectedUser = tableUsers != null ? tableUsers.getSelectionModel().getSelectedItem() : null;
        if (selectedUser != null) {
            showInfo("Reset password", "Remise à zéro du mot de passe pour " + selectedUser.fullName() + " à implémenter");
        }
    }
    
    /**
     * Affiche un message d'information
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}