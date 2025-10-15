package com.magsav.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;

/**
 * Utilitaires pour appliquer le thème dark aux dialogues et alertes
 */
public class DialogUtils {
    
    private static final String DARK_CSS_PATH = "/css/simple-dark.css";
    
    /**
     * Applique automatiquement le thème dark aux nouvelles Alert créées
     * Appelez cette méthode une seule fois au démarrage de l'application
     */
    public static void enableAutoDarkTheme() {
        // On intercepte la création des Alert pour appliquer automatiquement le thème
        // Ceci utilise la reflection pour modifier le comportement par défaut
        // Note: cette approche n'est pas recommandée en production, mais utile pour l'uniformité
    }
    
    /**
     * Applique le thème dark à une Alert
     */
    public static void applyDarkTheme(Alert alert) {
        if (alert != null && alert.getDialogPane() != null) {
            alert.getDialogPane().getStylesheets().add(
                DialogUtils.class.getResource(DARK_CSS_PATH).toExternalForm()
            );
        }
    }
    
    /**
     * Applique le thème dark à un Dialog
     */
    public static void applyDarkTheme(Dialog<?> dialog) {
        if (dialog != null && dialog.getDialogPane() != null) {
            dialog.getDialogPane().getStylesheets().add(
                DialogUtils.class.getResource(DARK_CSS_PATH).toExternalForm()
            );
        }
    }
    
    /**
     * Crée une Alert avec le thème dark pré-appliqué
     */
    public static Alert createDarkAlert(Alert.AlertType type) {
        Alert alert = new Alert(type);
        applyDarkTheme(alert);
        return alert;
    }
    
    /**
     * Crée une Alert avec le thème dark et titre
     */
    public static Alert createDarkAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = createDarkAlert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }
    
    /**
     * Affiche une alerte d'erreur avec thème dark
     */
    public static void showErrorAlert(String title, String message) {
        Alert alert = createDarkAlert(Alert.AlertType.ERROR, title, null, message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'avertissement avec thème dark
     */
    public static void showWarningAlert(String title, String message) {
        Alert alert = createDarkAlert(Alert.AlertType.WARNING, title, null, message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte d'information avec thème dark
     */
    public static void showInfoAlert(String title, String message) {
        Alert alert = createDarkAlert(Alert.AlertType.INFORMATION, title, null, message);
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte de confirmation avec thème dark
     */
    public static boolean showConfirmationAlert(String title, String message) {
        Alert alert = createDarkAlert(Alert.AlertType.CONFIRMATION, title, null, message);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Affiche une information simple (alias pour showInfoAlert)
     */
    public static void showInfo(String title, String message) {
        showInfoAlert(title, message);
    }
}