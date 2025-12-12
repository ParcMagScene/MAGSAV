package com.magscene.magsav.desktop.theme;

/**
 * Palette de couleurs standardisée pour MAGSAV-3.0
 * Toutes les couleurs utilisées dans l'application doivent passer par cette classe
 * pour assurer une cohérence visuelle et faciliter la maintenance
 */
public class StandardColors {
    
    // ========================================
    // 🎨 COULEURS DE BASE SYSTÈME
    // ========================================
    
    // Couleurs principales de l'interface
    public static final String PRIMARY_BLUE = "#6B71F2";      // Bleu principal MAGSAV
    public static final String SECONDARY_BLUE = "#8B91FF";    // Bleu secondaire
    public static final String ACCENT_BLUE = "#4A90E2";       // Bleu d'accent
    
    // Couleurs de fond selon le thème
    public static final String LIGHT_BACKGROUND = "#FFFFFF";
    public static final String LIGHT_SECONDARY = "#F8F9FA";
    public static final String DARK_BACKGROUND = "#142240";
    public static final String DARK_SECONDARY = "#1A2B4A";
    
    // ========================================
    // 🎯 COULEURS DE STATUT STANDARDISÉES
    // ========================================
    
    // Succès / Disponible / Validé
    public static final String SUCCESS_GREEN = "#28A745";
    public static final String SUCCESS_LIGHT = "#D4F7DC";
    
    // Attention / En cours / Modéré
    public static final String WARNING_ORANGE = "#FD7E14";
    public static final String WARNING_LIGHT = "#FFF3CD";
    
    // Information / Prestation / En attente
    public static final String INFO_BLUE = "#007BFF";
    public static final String INFO_LIGHT = "#D1ECF1";
    
    // Erreur / Urgent / Maintenance
    public static final String DANGER_RED = "#DC3545";
    public static final String DANGER_LIGHT = "#F8D7DA";
    
    // Neutre / Fermé / Gris
    public static final String NEUTRAL_GRAY = "#6C757D";
    public static final String NEUTRAL_LIGHT = "#E9ECEF";
    
    // ========================================
    // 📊 COULEURS CATÉGORIES MÉTIER
    // ========================================
    
    // Éclairage
    public static final String LIGHTING_COLOR = "#FF6B35";
    
    // Son / Audio
    public static final String AUDIO_COLOR = "#4ECDC4";
    
    // Vidéo
    public static final String VIDEO_COLOR = "#45B7D1";
    
    // Structure / Scène
    public static final String STRUCTURE_COLOR = "#96CEB4";
    
    // Transport / Logistique
    public static final String TRANSPORT_COLOR = "#FECA57";
    
    // ========================================
    // 🚗 COULEURS VÉHICULES / PLANNING
    // ========================================
    
    // États des véhicules
    public static final String VEHICLE_AVAILABLE = SUCCESS_GREEN;    // "#28A745"
    public static final String VEHICLE_LOCATION = INFO_BLUE;         // "#007BFF"
    public static final String VEHICLE_PRESTATION = WARNING_ORANGE;  // "#FD7E14"
    public static final String VEHICLE_MAINTENANCE = DANGER_RED;     // "#DC3545"
    
    // ========================================
    // 📝 COULEURS DEMANDES SAV
    // ========================================
    
    // Priorités
    public static final String PRIORITY_URGENT = DANGER_RED;         // "#DC3545"
    public static final String PRIORITY_HIGH = WARNING_ORANGE;       // "#FD7E14"
    public static final String PRIORITY_MEDIUM = INFO_BLUE;          // "#007BFF"
    public static final String PRIORITY_LOW = SUCCESS_GREEN;         // "#28A745"
    
    // États des demandes
    public static final String STATUS_OPEN = WARNING_ORANGE;         // "#FD7E14"
    public static final String STATUS_IN_PROGRESS = INFO_BLUE;       // "#007BFF"
    public static final String STATUS_WAITING_PARTS = "#FFC107";     // Jaune
    public static final String STATUS_RESOLVED = SUCCESS_GREEN;      // "#28A745"
    public static final String STATUS_CLOSED = NEUTRAL_GRAY;         // "#6C757D"
    public static final String STATUS_CANCELLED = DANGER_RED;        // "#DC3545"
    
    // ========================================
    // 🎨 MÉTHODES UTILITAIRES
    // ========================================
    
    /**
     * Obtient la couleur de fond selon le thème actuel
     */
    public static String getBackgroundColor() {
        return UnifiedThemeManager.getInstance().isDarkTheme() ? DARK_BACKGROUND : LIGHT_BACKGROUND;
    }
    
    /**
     * Obtient la couleur de fond secondaire selon le thème actuel
     */
    public static String getSecondaryBackgroundColor() {
        return UnifiedThemeManager.getInstance().isDarkTheme() ? DARK_SECONDARY : LIGHT_SECONDARY;
    }
    
    /**
     * Obtient la couleur de texte selon le thème actuel
     */
    public static String getTextColor() {
        return UnifiedThemeManager.getInstance().isDarkTheme() ? SECONDARY_BLUE : "#2C3E50";
    }
    
    /**
     * Obtient la couleur de bordure selon le thème actuel
     */
    public static String getBorderColor() {
        return UnifiedThemeManager.getInstance().isDarkTheme() ? SECONDARY_BLUE : "#DEE2E6";
    }
    
    /**
     * Obtient la couleur pour un statut de demande SAV
     */
    public static String getStatusColor(String status) {
        if (status == null) return NEUTRAL_GRAY;
        return switch (status.toLowerCase()) {
            case "ouverte", "open", "nouveau" -> STATUS_OPEN;
            case "en cours", "in_progress", "assignee" -> STATUS_IN_PROGRESS;
            case "attente pieces", "waiting_parts", "en attente" -> STATUS_WAITING_PARTS;
            case "resolue", "resolved", "terminee" -> STATUS_RESOLVED;
            case "fermee", "closed", "archivee" -> STATUS_CLOSED;
            case "annulee", "cancelled", "rejetee" -> STATUS_CANCELLED;
            default -> NEUTRAL_GRAY;
        };
    }
    
    /**
     * Obtient la couleur pour une priorité
     */
    public static String getPriorityColor(String priority) {
        if (priority == null) return NEUTRAL_GRAY;
        return switch (priority.toLowerCase()) {
            case "urgente", "urgent", "critique" -> PRIORITY_URGENT;
            case "haute", "high", "importante" -> PRIORITY_HIGH;
            case "moyenne", "medium", "normale" -> PRIORITY_MEDIUM;
            case "basse", "low", "faible" -> PRIORITY_LOW;
            default -> NEUTRAL_GRAY;
        };
    }
    
    /**
     * Obtient la couleur pour un état de véhicule
     */
    public static String getVehicleStatusColor(String status) {
        if (status == null) return NEUTRAL_GRAY;
        return switch (status.toLowerCase()) {
            case "available", "disponible", "libre" -> VEHICLE_AVAILABLE;
            case "location", "loue", "en location" -> VEHICLE_LOCATION;
            case "prestation", "en prestation", "mission" -> VEHICLE_PRESTATION;
            case "maintenance", "reparation", "panne" -> VEHICLE_MAINTENANCE;
            default -> NEUTRAL_GRAY;
        };
    }
    
    /**
     * Obtient la couleur pour une catégorie métier
     */
    public static String getCategoryColor(String category) {
        if (category == null) return PRIMARY_BLUE;
        return switch (category.toLowerCase()) {
            case "eclairage", "lighting", "éclairage" -> LIGHTING_COLOR;
            case "son", "audio", "sonorisation" -> AUDIO_COLOR;
            case "video", "vidéo", "projection" -> VIDEO_COLOR;
            case "structure", "scene", "scène" -> STRUCTURE_COLOR;
            case "transport", "logistique", "vehicule" -> TRANSPORT_COLOR;
            default -> PRIMARY_BLUE;
        };
    }
    
    /**
     * Obtient la couleur pour un type d'agenda de planning
     */
    public static String getAgendaColor(String agendaType) {
        if (agendaType == null) return PRIMARY_BLUE;
        return switch (agendaType.toLowerCase()) {
            case "principal" -> PRIMARY_BLUE;
            case "technician" -> SUCCESS_GREEN;
            case "vehicle" -> INFO_BLUE;
            case "maintenance" -> DANGER_RED;
            case "external" -> "#7C2D92"; // Violet pour externe
            default -> PRIMARY_BLUE;
        };
    }
}