package com.magsav.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Utilitaire centralisé pour tous les dialogs et alertes de l'application
 * Élimine la duplication des méthodes showAlert dans tous les contrôleurs
 */
public final class AlertUtils {
    
    private AlertUtils() {
        // Classe utilitaire
    }
    
    /**
     * Affiche une alerte d'information
     */
    public static void showInfo(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, message);
    }
    
    /**
     * Affiche une alerte d'avertissement
     */
    public static void showWarning(String title, String message) {
        showAlert(Alert.AlertType.WARNING, title, message);
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    public static void showError(String title, String message) {
        showAlert(Alert.AlertType.ERROR, title, message);
    }
    
    /**
     * Affiche une alerte d'erreur avec stacktrace
     */
    public static void showError(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        // Ajouter la stacktrace dans un TextArea extensible
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();
        
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }
    
    /**
     * Affiche une confirmation avec Oui/Non
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        ButtonType oui = new ButtonType("Oui", ButtonBar.ButtonData.YES);
        ButtonType non = new ButtonType("Non", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(oui, non);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == oui;
    }
    
    /**
     * Affiche une confirmation avec OK/Annuler
     */
    public static boolean showOkCancel(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Affiche une alerte personnalisée
     */
    public static void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(getDefaultTitle(type));
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        // Appliquer le thème sombre si disponible
        try {
            com.magsav.util.ThemeManager.applyDarkTheme(alert.getDialogPane().getScene());
        } catch (Exception e) {
            // Ignorer les erreurs de thème
        }
        
        alert.showAndWait();
    }
    
    /**
     * Affiche une alerte de notification "non implémenté"
     */
    public static void showNotImplemented(String feature) {
        showInfo("Non implémenté", 
                "La fonctionnalité '" + feature + "' sera implémentée dans une prochaine version.");
    }
    
    /**
     * Obtient le titre par défaut selon le type d'alerte
     */
    private static String getDefaultTitle(Alert.AlertType type) {
        return switch (type) {
            case INFORMATION -> "Information";
            case WARNING -> "Avertissement";
            case ERROR -> "Erreur";
            case CONFIRMATION -> "Confirmation";
            default -> "MAGSAV";
        };
    }
}