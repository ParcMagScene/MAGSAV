package com.magscene.magsav.desktop.theme;

import javafx.geometry.Insets;

/**
 * Gestionnaire centralisé des espacements pour l'interface MAGSAV
 * Standardise tous les paddings et spacings pour une cohérence visuelle
 */
public class SpacingManager {
    
    // ==================== ESPACEMENTS STANDARDISÉS ====================
    
    // Espacement minimal pour interface compacte
    public static final double SPACING_MINIMAL = 2;
    
    // Espacement réduit - nouveau standard pour toolbars 
    public static final double SPACING_COMPACT = 5;
    
    // Espacement normal pour contenu
    public static final double SPACING_NORMAL = 10;
    
    // Espacement large pour sections importantes
    public static final double SPACING_LARGE = 15;
    
    // Espacement extra pour séparation importante
    public static final double SPACING_EXTRA = 20;
    
    // ==================== INSETS PRÉDÉFINIS ====================
    
    /**
     * Padding global pour les vues principales - RÉDUIT
     */
    public static final Insets MAIN_VIEW_PADDING = new Insets(SPACING_COMPACT);
    
    /**
     * Padding pour les headers - RÉDUIT pour plus de compacité
     */
    public static final Insets HEADER_PADDING = new Insets(0, 0, SPACING_NORMAL, 0);
    
    /**
     * Padding pour les toolbars - STANDARDISÉ COMPACT
     */
    public static final Insets TOOLBAR_PADDING = new Insets(SPACING_COMPACT);
    
    /**
     * Padding pour les barres de filtres - COMPACT
     */
    public static final Insets FILTER_BAR_PADDING = new Insets(SPACING_COMPACT, SPACING_NORMAL, SPACING_COMPACT, SPACING_NORMAL);
    
    /**
     * Padding pour les footers
     */
    public static final Insets FOOTER_PADDING = new Insets(SPACING_NORMAL, 0, 0, 0);
    
    /**
     * Padding pour les containers de contenu
     */
    public static final Insets CONTENT_PADDING = new Insets(SPACING_COMPACT);
    
    /**
     * Padding pour les boutons et actions
     */
    public static final Insets BUTTON_CONTAINER_PADDING = new Insets(SPACING_NORMAL, 0, 0, 0);
    
    /**
     * Espacement minimal entre toolbar et contenu - NOUVEAU !
     */
    public static final Insets TOOLBAR_TO_CONTENT_SPACING = new Insets(SPACING_MINIMAL, 0, 0, 0);
    
    // ==================== MÉTHODES UTILITAIRES ====================
    
    /**
     * Retourne un Insets uniforme avec la valeur spécifiée
     */
    public static Insets uniform(double value) {
        return new Insets(value);
    }
    
    /**
     * Retourne un Insets avec valeurs verticale et horizontale
     */
    public static Insets symmetric(double vertical, double horizontal) {
        return new Insets(vertical, horizontal, vertical, horizontal);
    }
    
    /**
     * Retourne un Insets avec padding seulement en bas
     */
    public static Insets bottomOnly(double bottom) {
        return new Insets(0, 0, bottom, 0);
    }
    
    /**
     * Retourne un Insets avec padding seulement en haut
     */
    public static Insets topOnly(double top) {
        return new Insets(top, 0, 0, 0);
    }
    
    /**
     * Retourne un Insets personnalisé pour toolbar compacte
     */
    public static Insets compactToolbar() {
        return TOOLBAR_PADDING;
    }
    
    /**
     * Retourne un Insets pour espacement minimal entre éléments
     */
    public static Insets minimal() {
        return uniform(SPACING_MINIMAL);
    }
    
    /**
     * Retourne un Insets pour espacement compact
     */
    public static Insets compact() {
        return uniform(SPACING_COMPACT);
    }
    
    /**
     * Retourne un Insets pour espacement normal
     */
    public static Insets normal() {
        return uniform(SPACING_NORMAL);
    }
    
    // ==================== CONFIGURATION AVANCÉE ====================
    
    /**
     * Configuration pour modules SAV (compact)
     */
    public static class SAV {
        public static final Insets VIEW_PADDING = new Insets(SPACING_COMPACT);
        public static final Insets HEADER_PADDING = new Insets(0, 0, SPACING_COMPACT, 0);
        public static final Insets TOOLBAR_SPACING = new Insets(SPACING_MINIMAL, 0, SPACING_MINIMAL, 0);
    }
    
    /**
     * Configuration pour modules principaux (équipements, véhicules, etc.)
     */
    public static class Main {
        public static final Insets VIEW_PADDING = new Insets(SPACING_COMPACT);
        public static final Insets HEADER_PADDING = new Insets(0, 0, SPACING_NORMAL, 0);
        public static final Insets TOOLBAR_SPACING = new Insets(SPACING_MINIMAL, 0, SPACING_MINIMAL, 0);
    }
    
    /**
     * Configuration pour dialogues et pop-ups
     */
    public static class Dialog {
        public static final Insets CONTENT_PADDING = new Insets(SPACING_LARGE);
        public static final Insets BUTTON_PADDING = new Insets(SPACING_NORMAL, 0, 0, 0);
    }
}
