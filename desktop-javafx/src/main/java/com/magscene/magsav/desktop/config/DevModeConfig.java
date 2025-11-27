package com.magscene.magsav.desktop.config;

/**
 * Configuration globale du mode développement rapide.
 * Permet de désactiver toutes les confirmations pour accélérer le
 * développement.
 */
public class DevModeConfig {

    /**
     * Mode développement activé/désactivé
     */
    private static boolean DEV_MODE = false;

    /**
     * Auto-approve pour les suppressions
     */
    private static boolean AUTO_APPROVE_DELETE = false;

    /**
     * Auto-approve pour les sorties d'application
     */
    private static boolean AUTO_APPROVE_EXIT = false;

    /**
     * Auto-approve pour les modifications de données
     */
    private static boolean AUTO_APPROVE_MODIFICATIONS = false;

    /**
     * Auto-approve pour toutes les actions
     */
    private static boolean AUTO_APPROVE_ALL = false;

    static {
        // Détection automatique du mode développement via variable d'environnement
        String envDevMode = System.getenv("MAGSAV_DEV_MODE");
        if ("ENABLED".equalsIgnoreCase(envDevMode)) {
            enableDevMode();
        }
    }

    /**
     * Active le mode développement avec toutes les approbations automatiques
     */
    public static void enableDevMode() {
        DEV_MODE = true;
        AUTO_APPROVE_DELETE = true;
        AUTO_APPROVE_EXIT = true;
        AUTO_APPROVE_MODIFICATIONS = true;
        AUTO_APPROVE_ALL = true;
        System.out.println("🚀 MAGSAV Dev Mode: Toutes les approbations automatiques activées");
    }

    /**
     * Désactive le mode développement
     */
    public static void disableDevMode() {
        DEV_MODE = false;
        AUTO_APPROVE_DELETE = false;
        AUTO_APPROVE_EXIT = false;
        AUTO_APPROVE_MODIFICATIONS = false;
        AUTO_APPROVE_ALL = false;
        System.out.println("🔒 MAGSAV Dev Mode: Mode normal rétabli");
    }

    /**
     * Vérifie si le mode développement est activé
     */
    public static boolean isDevMode() {
        return DEV_MODE;
    }

    /**
     * Vérifie si les suppressions doivent être auto-approuvées
     */
    public static boolean shouldAutoApproveDelete() {
        return AUTO_APPROVE_DELETE || AUTO_APPROVE_ALL;
    }

    /**
     * Vérifie si les sorties doivent être auto-approuvées
     */
    public static boolean shouldAutoApproveExit() {
        return AUTO_APPROVE_EXIT || AUTO_APPROVE_ALL;
    }

    /**
     * Vérifie si les modifications doivent être auto-approuvées
     */
    public static boolean shouldAutoApproveModifications() {
        return AUTO_APPROVE_MODIFICATIONS || AUTO_APPROVE_ALL;
    }

    /**
     * Vérifie si toutes les actions doivent être auto-approuvées
     */
    public static boolean shouldAutoApproveAll() {
        return AUTO_APPROVE_ALL;
    }

    /**
     * Active/désactive l'auto-approval pour les suppressions
     */
    public static void setAutoApproveDelete(boolean enable) {
        AUTO_APPROVE_DELETE = enable;
    }

    /**
     * Active/désactive l'auto-approval pour les sorties
     */
    public static void setAutoApproveExit(boolean enable) {
        AUTO_APPROVE_EXIT = enable;
    }

    /**
     * Active/désactive l'auto-approval pour les modifications
     */
    public static void setAutoApproveModifications(boolean enable) {
        AUTO_APPROVE_MODIFICATIONS = enable;
    }

    /**
     * Active/désactive l'auto-approval pour toutes les actions
     */
    public static void setAutoApproveAll(boolean enable) {
        AUTO_APPROVE_ALL = enable;
        if (enable) {
            AUTO_APPROVE_DELETE = true;
            AUTO_APPROVE_EXIT = true;
            AUTO_APPROVE_MODIFICATIONS = true;
        }
    }
}
