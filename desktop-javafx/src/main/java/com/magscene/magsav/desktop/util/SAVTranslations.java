package com.magscene.magsav.desktop.util;

/**
 * Utilitaire de traduction des enums SAV (anglais → français)
 * Centralise les traductions pour Type, Status et Priority
 */
public final class SAVTranslations {

    private SAVTranslations() {
        // Classe utilitaire, pas d'instanciation
    }

    /**
     * Traduit le statut SAV en français
     */
    public static String translateStatus(String status) {
        if (status == null || status.isEmpty()) return "—";
        switch (status.toUpperCase()) {
            // Statuts demandes
            case "OPEN": return "Ouverte";
            case "VALIDATED": return "Validée";
            // Statuts interventions
            case "IN_PROGRESS": return "En cours";
            case "WAITING_PARTS": return "Attente pièces";
            case "RESOLVED": return "Résolue";
            case "CLOSED": return "Fermée";
            case "CANCELLED": return "Annulée";
            case "EXTERNAL": return "Externe";
            default: return status;
        }
    }

    /**
     * Traduit la priorité SAV en français
     */
    public static String translatePriority(String priority) {
        if (priority == null || priority.isEmpty()) return "—";
        switch (priority.toUpperCase()) {
            case "LOW": return "Basse";
            case "MEDIUM": return "Normale";
            case "HIGH": return "Haute";
            case "URGENT": return "Urgente";
            default: return priority;
        }
    }

    /**
     * Traduit le type d'intervention SAV en français
     */
    public static String translateType(String type) {
        if (type == null || type.isEmpty()) return "—";
        switch (type.toUpperCase()) {
            case "REPAIR": return "Réparation";
            case "MAINTENANCE": return "Maintenance";
            case "INSTALLATION": return "Installation";
            case "TRAINING": return "Formation";
            case "RMA": return "RMA";
            case "WARRANTY": return "Garantie";
            case "CALIBRATION": return "Calibration";
            case "INSPECTION": return "Inspection";
            default: return type;
        }
    }

    /**
     * Retourne l'icône correspondant au statut
     */
    public static String getStatusIcon(String status) {
        if (status == null) return "⚪";
        switch (status.toUpperCase()) {
            case "OPEN": return "🔵";
            case "IN_PROGRESS": return "🟠";
            case "WAITING_PARTS": return "🟣";
            case "RESOLVED": return "🟢";
            case "CLOSED": return "⚫";
            case "CANCELLED": return "❌";
            default: return "⚪";
        }
    }

    /**
     * Retourne l'icône correspondant à la priorité
     */
    public static String getPriorityIcon(String priority) {
        if (priority == null) return "⚪";
        switch (priority.toUpperCase()) {
            case "LOW": return "🟢";
            case "MEDIUM": return "🟡";
            case "HIGH": return "🟠";
            case "URGENT": return "🔴";
            default: return "⚪";
        }
    }

    /**
     * Retourne l'icône correspondant au type
     */
    public static String getTypeIcon(String type) {
        if (type == null) return "🔧";
        switch (type.toUpperCase()) {
            case "REPAIR": return "🔧";
            case "MAINTENANCE": return "🛠️";
            case "INSTALLATION": return "📦";
            case "TRAINING": return "📚";
            case "RMA": return "↩️";
            case "WARRANTY": return "📋";
            case "CALIBRATION": return "⚖️";
            case "INSPECTION": return "🔍";
            default: return "🔧";
        }
    }
}
