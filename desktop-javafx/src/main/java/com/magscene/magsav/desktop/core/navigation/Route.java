package com.magscene.magsav.desktop.core.navigation;

/**
 * Énumération des routes de navigation dans l'application
 */
public enum Route {
    DASHBOARD("Dashboard", "🏠", "dashboard"),
    PLANNING("Planning", "📅", "planning"),
    EQUIPMENT("Parc Matériel", "🎛️", "equipment"),
    SAV("SAV", "🔧", "sav"),
    SALES("Ventes & Installations", "💼", "sales"),
    VEHICLES("Véhicules", "🚐", "vehicles"),
    MATERIAL_REQUESTS("Demandes Matériel", "📝", "material-requests"),
    GROUPED_ORDERS("Commandes", "🛒", "grouped-orders"),
    SUPPLIERS("Fournisseurs", "🏪", "suppliers"),
    CLIENTS("Clients", "👥", "clients"),
    PERSONNEL("Personnel", "👤", "personnel"),
    CONTRACTS("Contrats", "📋", "contracts"),
    SETTINGS("Paramètres", "⚙", "settings");
    
    private final String displayName;
    private final String icon;
    private final String path;
    
    Route(String displayName, String icon, String path) {
        this.displayName = displayName;
        this.icon = icon;
        this.path = path;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getPath() {
        return path;
    }
    
    public String getFullDisplayName() {
        return icon + " " + displayName;
    }
}