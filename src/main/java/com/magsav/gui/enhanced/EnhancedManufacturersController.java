package com.magsav.gui.enhanced;

import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.ui.components.*;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Exemple de contrôleur amélioré utilisant les nouveaux composants UI
 * Démontre l'utilisation d'AlertManager, FormValidator, EnhancedTableView, etc.
 */
public class EnhancedManufacturersController {
    
    @FXML private VBox root;
    private TextField searchField;
    private EnhancedTableView<Societe> enhancedTable;
    private final SocieteRepository repo = new SocieteRepository();
    
    @FXML
    private void initialize() {
        setupEnhancedTable();
        setupLayout();
        loadData();
    }
    
    /**
     * Configuration de la table améliorée
     */
    private void setupEnhancedTable() {
        enhancedTable = new EnhancedTableView.Builder<Societe>()
            .column("ID", s -> String.valueOf(s.id()))
            .column("Nom", Societe::nom)
            .column("Email", s -> s.email() != null ? s.email() : "")
            .column("Téléphone", s -> s.phone() != null ? s.phone() : "")
            .column("Notes", s -> s.notes() != null ? s.notes() : "")
            .searchFilter(this::manufacturerMatchesSearch)
            .onDoubleClick(this::onEditManufacturer)
            .onEdit(this::onEditManufacturer)
            .onDelete(this::onDeleteManufacturer)
            .build();
        
        searchField = enhancedTable.getSearchField();
        searchField.setPromptText("Rechercher un fabricant...");
    }
    
    /**
     * Configuration du layout
     */
    private void setupLayout() {
        root.getChildren().addAll(
            searchField,
            enhancedTable.getTableView()
        );
        
        root.setSpacing(10);
    }
    
    /**
     * Charge les données
     */
    private void loadData() {
        ErrorManager.MAGSAV.safeExecute("Chargement des fabricants", () -> {
            var manufacturers = repo.findByType("FABRICANT");
            enhancedTable.setData(javafx.collections.FXCollections.observableList(manufacturers));
            NotificationManager.MAGSAV.operationSuccess("Fabricants chargés");
        });
    }
    
    /**
     * Filtre de recherche pour les fabricants
     */
    private boolean manufacturerMatchesSearch(Societe manufacturer) {
        String search = searchField.getText();
        if (search == null || search.trim().isEmpty()) {
            return true;
        }
        
        String lowerSearch = search.toLowerCase();
        return manufacturer.nom().toLowerCase().contains(lowerSearch) ||
               (manufacturer.email() != null && manufacturer.email().toLowerCase().contains(lowerSearch)) ||
               (manufacturer.phone() != null && manufacturer.phone().toLowerCase().contains(lowerSearch));
    }
    
    /**
     * Ajouter un nouveau fabricant
     */
    @FXML
    private void onAddManufacturer() {
        try {
            var result = FormDialogManager.MAGSAV.showManufacturerDialog(false, 
                FormDialogManager.Utils.getOwnerWindow(root));
            
            if (result.isSaved()) {
                loadData(); // Recharger les données
                NotificationManager.MAGSAV.itemSaved("Fabricant");
            }
        } catch (Exception e) {
            ErrorManager.MAGSAV.uiError("Dialogue fabricant", e);
        }
    }
    
    /**
     * Modifier un fabricant existant
     */
    private void onEditManufacturer(Societe manufacturer) {
        if (manufacturer == null) {
            AlertManager.MAGSAV.itemNotSelected("un fabricant");
            return;
        }
        
        try {
            var result = FormDialogManager.MAGSAV.showManufacturerDialog(true,
                FormDialogManager.Utils.getOwnerWindow(root));
            
            if (result.isSaved()) {
                loadData(); // Recharger les données
                NotificationManager.MAGSAV.itemSaved("Fabricant");
            }
        } catch (Exception e) {
            ErrorManager.MAGSAV.uiError("Dialogue fabricant", e);
        }
    }
    
    /**
     * Supprimer un fabricant
     */
    private void onDeleteManufacturer(Societe manufacturer) {
        if (manufacturer == null) {
            AlertManager.MAGSAV.itemNotSelected("un fabricant");
            return;
        }
        
        if (AlertManager.MAGSAV.confirmDelete("le fabricant", manufacturer.nom())) {
            ErrorManager.MAGSAV.safeExecute("Suppression fabricant", () -> {
                if (repo.delete(manufacturer.id())) {
                    loadData(); // Recharger les données
                    NotificationManager.MAGSAV.itemDeleted("Fabricant");
                } else {
                    AlertManager.showWarning("Suppression échouée", 
                        "Impossible de supprimer le fabricant. Il pourrait être utilisé par des produits.");
                }
            });
        }
    }
    
    /**
     * Rafraîchir les données
     */
    @FXML
    private void onRefresh() {
        loadData();
    }
    
    /**
     * Exporter les données
     */
    @FXML
    private void onExport() {
        ErrorManager.MAGSAV.safeExecute("Export fabricants", () -> {
            // TODO: Implémenter l'export
            NotificationManager.showInfo("Export non implémenté pour le moment");
        });
    }
    
    /**
     * Importer des données
     */
    @FXML
    private void onImport() {
        ErrorManager.MAGSAV.safeExecute("Import fabricants", () -> {
            // TODO: Implémenter l'import
            NotificationManager.showInfo("Import non implémenté pour le moment");
        });
    }
    
    /**
     * Exemple de formulaire de fabricant avec validation
     */
    public static class ManufacturerFormController {
        
        @FXML private TextField nameField;
        @FXML private TextField emailField;
        @FXML private TextField phoneField;
        @FXML private TextField addressField;
        @FXML private TextField notesField;
        
        private FormValidator validator;
        private Societe currentManufacturer;
        
        @FXML
        private void initialize() {
            setupValidation();
        }
        
        private void setupValidation() {
            validator = new FormValidator.Builder()
                .requiredTextField(nameField, "Nom")
                .textField(nameField, "Nom", 2, 100)
                .emailField(emailField)
                .phoneField(phoneField)
                .build();
        }
        
        public void initForm(Societe manufacturer) {
            this.currentManufacturer = manufacturer;
            
            if (manufacturer != null) {
                nameField.setText(manufacturer.nom());
                emailField.setText(manufacturer.email());
                phoneField.setText(manufacturer.phone());
                addressField.setText(manufacturer.adresse());
                notesField.setText(manufacturer.notes());
            }
        }
        
        public boolean validateForm() {
            return validator.validateAndShow();
        }
        
        public void saveFormData() {
            if (!validateForm()) {
                return;
            }
            
            ErrorManager.MAGSAV.safeExecute("Sauvegarde fabricant", () -> {
                SocieteRepository repo = new SocieteRepository();
                
                if (currentManufacturer == null) {
                    // Nouveau fabricant
                    repo.insert("FABRICANT", 
                        nameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim(),
                        addressField.getText().trim(),
                        notesField.getText().trim());
                } else {
                    // Mise à jour
                    repo.update(currentManufacturer.id(),
                        "FABRICANT",
                        nameField.getText().trim(),
                        emailField.getText().trim(), 
                        phoneField.getText().trim(),
                        addressField.getText().trim(),
                        notesField.getText().trim());
                }
            });
        }
    }
}