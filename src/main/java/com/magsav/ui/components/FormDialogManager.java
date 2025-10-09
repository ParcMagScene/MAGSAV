package com.magsav.ui.components;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Window;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Gestionnaire pour les dialogues de formulaires
 * Simplifie la création et la gestion des formulaires modaux
 */
public final class FormDialogManager {
    
    private FormDialogManager() {}
    
    /**
     * Interface pour les contrôleurs de formulaires
     */
    public interface FormController {
        /**
         * Initialise le formulaire avec les données
         */
        void initForm();
        
        /**
         * Valide les données du formulaire
         * @return true si les données sont valides
         */
        boolean validateForm();
        
        /**
         * Récupère les données du formulaire
         */
        void saveFormData();
    }
    
    /**
     * Résultat d'un dialogue de formulaire
     */
    public static class FormResult<T> {
        private final boolean saved;
        private final T controller;
        
        public FormResult(boolean saved, T controller) {
            this.saved = saved;
            this.controller = controller;
        }
        
        public boolean isSaved() { return saved; }
        public T getController() { return controller; }
    }
    
    /**
     * Builder pour créer facilement des dialogues de formulaires
     */
    public static class Builder<T> {
        private String title;
        private String fxmlPath;
        private Window owner;
        private Consumer<T> initializer;
        private Supplier<Boolean> validator;
        private Runnable onSave;
        
        public Builder<T> title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder<T> fxml(String fxmlPath) {
            this.fxmlPath = fxmlPath;
            return this;
        }
        
        public Builder<T> owner(Window owner) {
            this.owner = owner;
            return this;
        }
        
        public Builder<T> initializer(Consumer<T> initializer) {
            this.initializer = initializer;
            return this;
        }
        
        public Builder<T> validator(Supplier<Boolean> validator) {
            this.validator = validator;
            return this;
        }
        
        public Builder<T> onSave(Runnable onSave) {
            this.onSave = onSave;
            return this;
        }
        
        public FormResult<T> show() throws IOException {
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();
            
            // Initialiser le contrôleur
            if (initializer != null) {
                initializer.accept(controller);
            }
            
            // Créer le dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title != null ? title : "Formulaire");
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            if (owner != null) {
                dialog.initOwner(owner);
            }
            
            // Gérer la validation
            if (validator != null) {
                dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                    javafx.event.ActionEvent.ACTION, event -> {
                        if (!validator.get()) {
                            event.consume(); // Empêche la fermeture du dialogue
                        }
                    }
                );
            }
            
            // Afficher et traiter le résultat
            Optional<ButtonType> result = dialog.showAndWait();
            boolean saved = result.isPresent() && result.get() == ButtonType.OK;
            
            if (saved && onSave != null) {
                onSave.run();
            }
            
            return new FormResult<>(saved, controller);
        }
    }
    
    /**
     * Méthodes de convenance pour les cas d'usage courants de MAGSAV
     */
    public static class MAGSAV {
        
        /**
         * Dialogue d'ajout d'un nouvel élément
         */
        public static <T> FormResult<T> showAddDialog(String itemType, String fxmlPath, 
                                                      Consumer<T> initializer, 
                                                      Supplier<Boolean> validator,
                                                      Runnable onSave) throws IOException {
            return new Builder<T>()
                .title("Ajouter " + itemType)
                .fxml(fxmlPath)
                .initializer(initializer)
                .validator(validator)
                .onSave(onSave)
                .show();
        }
        
        /**
         * Dialogue de modification d'un élément existant
         */
        public static <T> FormResult<T> showEditDialog(String itemType, String fxmlPath,
                                                       Consumer<T> initializer,
                                                       Supplier<Boolean> validator,
                                                       Runnable onSave) throws IOException {
            return new Builder<T>()
                .title("Modifier " + itemType)
                .fxml(fxmlPath)
                .initializer(initializer)
                .validator(validator)
                .onSave(onSave)
                .show();
        }
        
        /**
         * Dialogue pour les fabricants
         */
        public static FormResult<?> showManufacturerDialog(boolean isEdit, Window owner) throws IOException {
            String title = isEdit ? "Modifier le fabricant" : "Ajouter un fabricant";
            return new Builder<>()
                .title(title)
                .fxml("/fxml/societes/manufacturer_form.fxml")
                .owner(owner)
                .show();
        }
        
        /**
         * Dialogue pour les fabricants avec données d'initialisation
         */
        public static FormResult<com.magsav.gui.societes.ManufacturerFormController> showManufacturerDialog(com.magsav.model.Societe societe, Window owner) throws IOException {
            String title = societe != null ? "Modifier le fabricant" : "Ajouter un fabricant";
            
            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(FormDialogManager.class.getResource("/fxml/societes/manufacturer_form.fxml"));
            Parent root = loader.load();
            com.magsav.gui.societes.ManufacturerFormController controller = loader.getController();
            
            // Initialiser le contrôleur
            controller.setSocieteType("FABRICANT");
            controller.init(societe);
            
            // Créer le dialogue
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.getDialogPane().setContent(root);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            if (owner != null) {
                dialog.initOwner(owner);
            }
            
            // Gérer la validation avec accès au contrôleur
            dialog.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(
                javafx.event.ActionEvent.ACTION, event -> {
                    if (!controller.validateForm()) {
                        event.consume(); // Empêche la fermeture du dialogue
                        new Alert(Alert.AlertType.WARNING, "Veuillez corriger les erreurs avant de continuer.").showAndWait();
                    }
                }
            );
            
            // Afficher et traiter le résultat
            Optional<ButtonType> result = dialog.showAndWait();
            boolean saved = result.isPresent() && result.get() == ButtonType.OK;
            
            if (saved) {
                try {
                    controller.saveFormData();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de la sauvegarde: " + e.getMessage()).showAndWait();
                    return new FormResult<>(false, controller);
                }
            }
            
            return new FormResult<>(saved, controller);
        }
        
        /**
         * Dialogue pour les catégories
         */
        public static FormResult<?> showCategoryDialog(boolean isEdit, Window owner) throws IOException {
            String title = isEdit ? "Modifier la catégorie" : "Ajouter une catégorie";
            return new Builder<>()
                .title(title)
                .fxml("/fxml/products/category_form.fxml")
                .owner(owner)
                .show();
        }
        
        /**
         * Dialogue pour les produits
         */
        public static FormResult<?> showProductDialog(boolean isEdit, Window owner) throws IOException {
            String title = isEdit ? "Modifier le produit" : "Ajouter un produit";
            return new Builder<>()
                .title(title)
                .fxml("/fxml/product_form.fxml")
                .owner(owner)
                .show();
        }
        
        /**
         * Dialogue pour les interventions
         */
        public static FormResult<?> showInterventionDialog(boolean isEdit, Window owner) throws IOException {
            String title = isEdit ? "Modifier l'intervention" : "Nouvelle intervention";
            return new Builder<>()
                .title(title)
                .fxml("/fxml/interventions/intervention_form.fxml")
                .owner(owner)
                .show();
        }
    }
    
    /**
     * Utilitaires pour les dialogues courants
     */
    public static class Utils {
        
        /**
         * Crée un dialogue simple avec contenu personnalisé
         */
        public static Dialog<ButtonType> createSimpleDialog(String title, Parent content, Window owner) {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            if (owner != null) {
                dialog.initOwner(owner);
            }
            
            return dialog;
        }
        
        /**
         * Affiche un dialogue de confirmation personnalisé
         */
        public static boolean showCustomConfirmation(String title, String message, 
                                                    String confirmText, String cancelText, 
                                                    Window owner) {
            ButtonType confirmButton = new ButtonType(confirmText);
            ButtonType cancelButton = new ButtonType(cancelText);
            
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(title);
            dialog.setContentText(message);
            dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);
            
            if (owner != null) {
                dialog.initOwner(owner);
            }
            
            Optional<ButtonType> result = dialog.showAndWait();
            return result.isPresent() && result.get() == confirmButton;
        }
        
        /**
         * Extrait la fenêtre propriétaire d'un contrôle JavaFX
         */
        public static Window getOwnerWindow(javafx.scene.Node node) {
            if (node == null || node.getScene() == null) {
                return null;
            }
            return node.getScene().getWindow();
        }
    }
}