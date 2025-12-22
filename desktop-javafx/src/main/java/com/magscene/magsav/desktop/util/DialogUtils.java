package com.magscene.magsav.desktop.util;

import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Classe utilitaire centralisée pour les boîtes de dialogue
 * Remplace les méthodes showAlert() dupliquées dans les vues
 * 
 * @author MAGSAV Architecture Team
 * @since 3.0
 */
public final class DialogUtils {
    
    private DialogUtils() {
        // Classe utilitaire - pas d'instanciation
    }
    
    // =====================================================
    // ALERTES SIMPLES
    // =====================================================
    
    /**
     * Affiche une alerte d'information
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, null, message);
    }
    
    /**
     * Affiche une alerte d'information avec header
     */
    public static void showInfo(String title, String header, String message) {
        showAlert(AlertType.INFORMATION, title, header, message);
    }
    
    /**
     * Affiche une alerte de succès
     */
    public static void showSuccess(String title, String message) {
        showAlert(AlertType.INFORMATION, title, "✅ Succès", message);
    }
    
    /**
     * Affiche une alerte d'avertissement
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, null, message);
    }
    
    /**
     * Affiche une alerte d'avertissement avec header
     */
    public static void showWarning(String title, String header, String message) {
        showAlert(AlertType.WARNING, title, header, message);
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, null, message);
    }
    
    /**
     * Affiche une alerte d'erreur avec header
     */
    public static void showError(String title, String header, String message) {
        showAlert(AlertType.ERROR, title, header, message);
    }
    
    /**
     * Affiche une alerte d'erreur à partir d'une exception
     */
    public static void showError(String title, Exception e) {
        showAlert(AlertType.ERROR, title, "❌ Erreur", e.getMessage());
        e.printStackTrace();
    }
    
    // =====================================================
    // ALERTES GÉNÉRIQUES
    // =====================================================
    
    /**
     * Affiche une alerte générique
     */
    public static void showAlert(AlertType type, String title, String header, String content) {
        Alert alert = createStyledAlert(type, title, header, content);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte générique (signature simplifiée pour rétrocompatibilité)
     */
    public static void showAlert(String title, String message) {
        showInfo(title, message);
    }
    
    // =====================================================
    // CONFIRMATIONS
    // =====================================================
    
    /**
     * Affiche une boîte de confirmation Oui/Non
     * @return true si l'utilisateur a confirmé
     */
    public static boolean confirm(String title, String message) {
        return confirm(title, null, message);
    }
    
    /**
     * Affiche une boîte de confirmation Oui/Non avec header
     * @return true si l'utilisateur a confirmé
     */
    public static boolean confirm(String title, String header, String message) {
        Alert alert = createStyledAlert(AlertType.CONFIRMATION, title, header, message);
        
        ButtonType yesButton = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }
    
    /**
     * Affiche une boîte de confirmation pour suppression
     * @return true si l'utilisateur a confirmé la suppression
     */
    public static boolean confirmDelete(String itemName) {
        return confirm(
            "Confirmer la suppression",
            "⚠️ Attention",
            "Êtes-vous sûr de vouloir supprimer " + itemName + " ?\n\nCette action est irréversible."
        );
    }
    
    /**
     * Affiche une boîte de confirmation pour suppression multiple
     * @return true si l'utilisateur a confirmé la suppression
     */
    public static boolean confirmDeleteMultiple(int count, String itemType) {
        return confirm(
            "Confirmer la suppression",
            "⚠️ Attention",
            "Êtes-vous sûr de vouloir supprimer " + count + " " + itemType + " ?\n\nCette action est irréversible."
        );
    }
    
    // =====================================================
    // SAISIE UTILISATEUR
    // =====================================================
    
    /**
     * Affiche une boîte de dialogue de saisie de texte
     * @return Le texte saisi, ou null si annulé
     */
    public static String prompt(String title, String message) {
        return prompt(title, null, message, "");
    }
    
    /**
     * Affiche une boîte de dialogue de saisie de texte avec valeur par défaut
     * @return Le texte saisi, ou null si annulé
     */
    public static String prompt(String title, String header, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(message);
        
        // Appliquer le thème
        applyTheme(dialog.getDialogPane());
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    // =====================================================
    // UTILITAIRES INTERNES
    // =====================================================
    
    /**
     * Crée une alerte stylisée avec le thème MAGSAV
     */
    private static Alert createStyledAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Appliquer le thème
        applyTheme(alert.getDialogPane());
        
        return alert;
    }
    
    /**
     * Applique le thème MAGSAV à un DialogPane
     */
    private static void applyTheme(DialogPane dialogPane) {
        try {
            UnifiedThemeManager.getInstance().applyThemeToDialog(dialogPane);
        } catch (Exception e) {
            // Fallback : style basique si le thème manager n'est pas disponible
            dialogPane.setStyle(
                "-fx-background-color: #FFFFFF;" +
                "-fx-font-family: 'Segoe UI';"
            );
        }
    }
    
    // =====================================================
    // ALERTES CONTEXTUELLES MÉTIER
    // =====================================================
    
    /**
     * Affiche une alerte de validation de formulaire
     */
    public static void showValidationError(String fieldName) {
        showError("Erreur de validation", "Le champ '" + fieldName + "' est obligatoire.");
    }
    
    /**
     * Affiche une alerte de validation de formulaire avec plusieurs champs
     */
    public static void showValidationErrors(String... fieldNames) {
        StringBuilder message = new StringBuilder("Les champs suivants sont obligatoires :\n");
        for (String field : fieldNames) {
            message.append("• ").append(field).append("\n");
        }
        showError("Erreur de validation", message.toString().trim());
    }
    
    /**
     * Affiche une alerte de sauvegarde réussie
     */
    public static void showSaveSuccess(String entityName) {
        showSuccess("Sauvegarde réussie", entityName + " a été enregistré(e) avec succès.");
    }
    
    /**
     * Affiche une alerte de suppression réussie
     */
    public static void showDeleteSuccess(String entityName) {
        showSuccess("Suppression réussie", entityName + " a été supprimé(e) avec succès.");
    }
    
    /**
     * Affiche une alerte d'erreur API
     */
    public static void showApiError(String operation, String details) {
        showError(
            "Erreur de communication",
            "❌ Échec de l'opération : " + operation,
            "Détails : " + details + "\n\nVérifiez que le serveur backend est démarré."
        );
    }
    
    /**
     * Affiche une alerte de connexion API échouée
     */
    public static void showConnectionError() {
        showError(
            "Erreur de connexion",
            "❌ Impossible de se connecter au serveur",
            "Vérifiez que le serveur backend est démarré et accessible sur le port 8080."
        );
    }
}
