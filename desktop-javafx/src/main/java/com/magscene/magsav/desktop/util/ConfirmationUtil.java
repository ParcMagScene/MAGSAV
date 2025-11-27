package com.magscene.magsav.desktop.util;

import java.util.Optional;

import com.magscene.magsav.desktop.config.DevModeConfig;
import com.magscene.magsav.desktop.service.WindowPreferencesService;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Gestionnaire centralisé des confirmations avec support du mode développement.
 * Permet de désactiver automatiquement toutes les confirmations en mode dev.
 */
public class ConfirmationUtil {

    /**
     * Affiche une confirmation de suppression.
     * En mode dev, execute directement l'action sans demander confirmation.
     *
     * @param itemName  Nom de l'élément à supprimer
     * @param onConfirm Action à exécuter si confirmé
     */
    public static void confirmDelete(String itemName, Runnable onConfirm) {
        if (DevModeConfig.shouldAutoApproveDelete()) {
            onConfirm.run();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer " + itemName + " ?");
        alert.setContentText("Cette action est irréversible.");

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "delete-confirmation-dialog");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    /**
     * Affiche une confirmation générique.
     * En mode dev, execute directement l'action sans demander confirmation.
     *
     * @param title     Titre de la confirmation
     * @param header    En-tête du message
     * @param content   Contenu du message
     * @param onConfirm Action à exécuter si confirmé
     */
    public static void confirm(String title, String header, String content, Runnable onConfirm) {
        if (DevModeConfig.shouldAutoApproveAll()) {
            onConfirm.run();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "generic-confirmation-dialog");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    /**
     * Affiche une confirmation avec choix oui/non.
     * En mode dev, retourne directement true.
     *
     * @param title   Titre de la confirmation
     * @param header  En-tête du message
     * @param content Contenu du message
     * @return true si confirmé, false sinon
     */
    public static boolean confirmYesNo(String title, String header, String content) {
        if (DevModeConfig.shouldAutoApproveAll()) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "yesno-confirmation-dialog");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Affiche une confirmation de modification.
     * En mode dev, execute directement l'action sans demander confirmation.
     *
     * @param itemName  Nom de l'élément à modifier
     * @param onConfirm Action à exécuter si confirmé
     */
    public static void confirmModification(String itemName, Runnable onConfirm) {
        if (DevModeConfig.shouldAutoApproveModifications()) {
            onConfirm.run();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de modification");
        alert.setHeaderText("Modifier " + itemName + " ?");
        alert.setContentText("Les modifications seront sauvegardées immédiatement.");

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(),
                "modification-confirmation-dialog");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    /**
     * Affiche une confirmation de sortie de l'application.
     * En mode dev, execute directement l'action sans demander confirmation.
     *
     * @param onConfirm Action à exécuter si confirmé (généralement Platform.exit())
     */
    public static void confirmExit(Runnable onConfirm) {
        if (DevModeConfig.shouldAutoApproveExit()) {
            onConfirm.run();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Quitter MAGSAV 3.0 ?");
        alert.setContentText("Êtes-vous sûr de vouloir quitter l'application ?");

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "exit-confirmation-dialog");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    /**
     * Affiche une alerte d'information (non bloquante en mode dev).
     *
     * @param title   Titre
     * @param header  En-tête
     * @param content Contenu
     */
    public static void showInfo(String title, String header, String content) {
        // En mode dev, on peut choisir de logger au lieu d'afficher
        if (DevModeConfig.isDevMode()) {
            System.out.println("ℹ️ " + title + ": " + header + " - " + content);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "info-dialog");

        alert.showAndWait();
    }

    /**
     * Affiche une alerte d'erreur.
     *
     * @param title   Titre
     * @param header  En-tête
     * @param content Contenu
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "error-dialog");

        alert.showAndWait();
    }

    /**
     * Affiche un avertissement.
     *
     * @param title   Titre
     * @param header  En-tête
     * @param content Contenu
     */
    public static void showWarning(String title, String header, String content) {
        // En mode dev, on peut choisir de logger au lieu d'afficher
        if (DevModeConfig.isDevMode()) {
            System.out.println("⚠️ " + title + ": " + header + " - " + content);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        WindowPreferencesService.getInstance().setupDialogMemory(alert.getDialogPane(), "warning-dialog");

        alert.showAndWait();
    }
}
