package com.magsav.ui.components;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;
import java.util.Optional;

/**
 * Gestionnaire centralisé pour les alertes et dialogues
 * Fournit des méthodes standardisées pour l'affichage de messages
 */
public final class AlertManager {
    
    private static Window defaultOwner = null;
    
    private AlertManager() {}
    
    /**
     * Définit la fenêtre propriétaire par défaut pour les dialogues
     */
    public static void setDefaultOwner(Window owner) {
        defaultOwner = owner;
    }
    
    /**
     * Affiche une information simple
     */
    public static void showInfo(String title, String message) {
        showInfo(title, message, defaultOwner);
    }
    
    /**
     * Affiche une information avec fenêtre propriétaire
     */
    public static void showInfo(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
    
    /**
     * Affiche un avertissement
     */
    public static void showWarning(String title, String message) {
        showWarning(title, message, defaultOwner);
    }
    
    /**
     * Affiche un avertissement avec fenêtre propriétaire
     */
    public static void showWarning(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
    
    /**
     * Affiche une erreur
     */
    public static void showError(String title, String message) {
        showError(title, message, defaultOwner);
    }
    
    /**
     * Affiche une erreur avec fenêtre propriétaire
     */
    public static void showError(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        alert.showAndWait();
    }
    
    /**
     * Affiche une erreur formatée à partir d'une exception
     */
    public static void showError(String title, Exception exception) {
        showError(title, exception, defaultOwner);
    }
    
    /**
     * Affiche une erreur formatée à partir d'une exception avec fenêtre propriétaire
     */
    public static void showError(String title, Exception exception, Window owner) {
        String message = formatException(exception);
        showError(title, message, owner);
    }
    
    /**
     * Affiche une demande de confirmation
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, message, defaultOwner);
    }
    
    /**
     * Affiche une demande de confirmation avec fenêtre propriétaire
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmation(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (owner != null) {
            alert.initOwner(owner);
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Affiche une demande de confirmation avec boutons personnalisés
     * @return true si l'utilisateur a choisi "Oui", false sinon
     */
    public static boolean showYesNoConfirmation(String title, String message) {
        return showYesNoConfirmation(title, message, defaultOwner);
    }
    
    /**
     * Affiche une demande de confirmation avec boutons personnalisés et fenêtre propriétaire
     * @return true si l'utilisateur a choisi "Oui", false sinon
     */
    public static boolean showYesNoConfirmation(String title, String message, Window owner) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (owner != null) {
            alert.initOwner(owner);
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }
    
    /**
     * Crée un dialogue personnalisé avec contenu
     */
    public static Dialog<ButtonType> createCustomDialog(String title, DialogPane content) {
        return createCustomDialog(title, content, defaultOwner);
    }
    
    /**
     * Crée un dialogue personnalisé avec contenu et fenêtre propriétaire
     */
    public static Dialog<ButtonType> createCustomDialog(String title, DialogPane content, Window owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setDialogPane(content);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        return dialog;
    }
    
    /**
     * Formate une exception en message lisible
     */
    private static String formatException(Exception exception) {
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getSimpleName());
        if (exception.getMessage() != null) {
            sb.append(": ").append(exception.getMessage());
        }
        
        Throwable cause = exception.getCause();
        if (cause != null) {
            sb.append("\nCause: ").append(cause.getClass().getSimpleName());
            if (cause.getMessage() != null) {
                sb.append(": ").append(cause.getMessage());
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Raccourcis pour les cas d'usage fréquents dans MAGSAV
     */
    public static class MAGSAV {
        
        public static void operationSuccess(String operation) {
            showInfo("Succès", operation + " effectuée avec succès.");
        }
        
        public static void operationError(String operation, Exception ex) {
            showError("Erreur " + operation, formatException(ex));
        }
        
        public static void validationError(String message) {
            showWarning("Validation", message);
        }
        
        public static boolean confirmDelete(String itemType, String itemName) {
            return showYesNoConfirmation(
                "Confirmation de suppression",
                "Êtes-vous sûr de vouloir supprimer " + itemType + " \"" + itemName + "\" ?\n\nCette action est irréversible."
            );
        }
        
        public static void itemNotSelected(String itemType) {
            showWarning("Sélection requise", "Veuillez sélectionner " + itemType + " dans la liste.");
        }
        
        public static void noItemsFound(String searchQuery) {
            showInfo("Aucun résultat", "Aucun élément trouvé pour la recherche : \"" + searchQuery + "\"");
        }
        
        public static void importResult(int created, int errors, String details) {
            String title = errors == 0 ? "Import réussi" : "Import terminé avec erreurs";
            String message = String.format(
                "Import terminé :\n• %d éléments créés\n• %d erreurs",
                created, errors
            );
            if (details != null && !details.isEmpty()) {
                message += "\n\nDétails :\n" + details;
            }
            
            if (errors == 0) {
                showInfo(title, message);
            } else {
                showWarning(title, message);
            }
        }
    }
}