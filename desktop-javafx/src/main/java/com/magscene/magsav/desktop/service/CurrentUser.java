package com.magscene.magsav.desktop.service;

/**
 * Utilitaire pour stocker l'utilisateur courant et son rôle.
 */
public class CurrentUser {
    private static String username;
    private static String role;

    public static void set(String username, String role) {
        CurrentUser.username = username;
        CurrentUser.role = role;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isAdmin() {
        return role != null && role.toLowerCase().contains("admin");
    }
}
