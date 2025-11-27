package com.magscene.magsav.desktop.theme;

import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

/**
 * Constantes de thème centralisées pour MAGSAV-3.0
 * 
 * Cette classe unifie toutes les couleurs, polices, espacements et styles
 * utilisés dans l'application pour remplacer les valeurs codées en dur.
 * 
 * 🎨 PHASE 2: Harmonisation du thème - Centralisation des styles
 */
public class ThemeConstants {

    // ========================================
    // 🎨 COULEURS PRINCIPALES (CHARTE MAGSAV); //
    // ========================================

    /** Couleur primaire MAGSAV - Bleu principal */
    public static final String PRIMARY_COLOR = "#6B71F2";

    /** Couleur secondaire MAGSAV - Alignée sur primaire */
    public static final String SECONDARY_COLOR = "#6B71F2";

    /** Couleur d'arrière-plan principal (thème light) */
    public static final String BACKGROUND_PRIMARY = "#FFFFFF";

    /** Couleur d'arrière-plan secondaire (thème light) */
    public static final String BACKGROUND_SECONDARY = "#F8F9FA";

    /** Couleur d'arrière-plan tertiaire (zones de contenu) */
    public static final String BACKGROUND_TERTIARY = "#E9ECEF";

    // ========================================
    // 🎯 COULEURS DE TEXTE; // ========================================

    /** Couleur de texte principal (sombre pour thème light) */
    public static final String TEXT_PRIMARY = "#212529";

    /** Couleur de texte secondaire (grisé) */
    public static final String TEXT_SECONDARY = "#6C757D";

    /** Couleur de texte pour les titres */
    public static final String TEXT_TITLE = "#212529";

    /** Couleur de texte d'accent (MAGSAV) */
    public static final String TEXT_ACCENT = PRIMARY_COLOR;

    /** Couleur de texte sombre (pour thème clair) */
    public static final String TEXT_DARK = "#000000";

    // ========================================
    // ✅ COULEURS D'ÉTAT (STATUS); // ========================================

    /** Couleur de succès (vert) */
    public static final String SUCCESS_COLOR = "#27ae60";

    /** Couleur d'avertissement (orange) */
    public static final String WARNING_COLOR = "#f39c12";

    /** Couleur d'erreur/danger (rouge) */
    public static final String ERROR_COLOR = "#e74c3c";

    /** Couleur d'information (bleu) */
    public static final String INFO_COLOR = "#3498db";

    /** Couleur de statut inactif (gris) */
    public static final String INACTIVE_COLOR = "#95a5a6";

    /** Couleur de statut critique (rouge foncé) */
    public static final String CRITICAL_COLOR = "#c0392b";

    /** Couleur violet pour actions spéciales */
    public static final String SPECIAL_COLOR = "#9b59b6";

    /** Couleur cyan pour détails */
    public static final String DETAIL_COLOR = "#17a2b8";

    // ========================================
    // 🎯 COULEURS DE SÉLECTION; // ========================================

    /** Couleur d'arrière-plan de sélection */
    public static final String SELECTION_BACKGROUND = BACKGROUND_PRIMARY;

    /** Couleur de texte pour éléments sélectionnés */
    public static final String SELECTION_TEXT = SECONDARY_COLOR;

    /** Couleur de bordure pour éléments sélectionnés */
    public static final String SELECTION_BORDER = SECONDARY_COLOR;

    // ========================================
    // 🌈 COULEURS POUR PLANNING/CALENDRIER; //
    // ========================================

    /** Couleurs pour événements du planning */
    public static final String[] CALENDAR_COLORS = {
            "#3D4BD1", // Bleu foncé
            "#166534", // Vert foncé
            "#1E3A8A", // Bleu marine
            "#B91C1C", // Rouge
            "#7C2D92" // Violet
    };

    /** Couleurs de bordure pour événements */
    public static final String[] CALENDAR_BORDER_COLORS = {
            "#4F5AE8", // Bleu clair
            "#22543D", // Vert clair
            "#2563EB", // Bleu clair marine
            "#DC2626", // Rouge clair
            "#9333EA" // Violet clair
    };

    // ========================================
    // 📝 POLICES ET TAILLES; // ========================================

    /** Police système par défaut */
    public static final String FONT_FAMILY = "System";

    /** Taille de police pour les titres principaux */
    public static final double FONT_SIZE_TITLE = 18.0;

    /** Taille de police pour les sous-titres */
    public static final double FONT_SIZE_SUBTITLE = 14.0;

    /** Taille de police pour le texte normal */
    public static final double FONT_SIZE_NORMAL = 12.0;

    /** Taille de police pour le texte petit */
    public static final double FONT_SIZE_SMALL = 10.0;

    /** Poids de police pour les titres */
    public static final FontWeight FONT_WEIGHT_TITLE = FontWeight.BOLD;

    /** Poids de police normal */
    public static final FontWeight FONT_WEIGHT_NORMAL = FontWeight.NORMAL;

    // ========================================
    // 📐 ESPACEMENTS ET MARGES; // ========================================

    /** Espacement très petit */
    public static final double SPACING_XS = 5.0;

    /** Espacement petit */
    public static final double SPACING_SM = 7.0;

    /** Espacement moyen */
    public static final double SPACING_MD = 10.0;

    /** Espacement large */
    public static final double SPACING_LG = 15.0;

    /** Espacement très large */
    public static final double SPACING_XL = 20.0;

    /** Marge interne standard pour les containers (7px uniformisé) */
    public static final Insets PADDING_STANDARD = new Insets(SPACING_SM);

    /** Marge interne petite */
    public static final Insets PADDING_SMALL = new Insets(SPACING_XS);

    /** Marge interne moyenne */
    public static final Insets PADDING_MEDIUM = new Insets(SPACING_MD);

    /** Marge interne large */
    public static final Insets PADDING_LARGE = new Insets(SPACING_XL);

    /** Marge pour les toolbars */
    public static final Insets TOOLBAR_PADDING = new Insets(SPACING_MD);

    /** Marge externe pour les toolbars (bottom) */
    public static final Insets TOOLBAR_MARGIN = new Insets(0, 0, SPACING_SM, 0);

    // ========================================
    // 🔲 BORDURES ET RADIUS; // ========================================

    /** Rayon de bordure petit */
    public static final double BORDER_RADIUS_SM = 4.0;

    /** Rayon de bordure moyen */
    public static final double BORDER_RADIUS_MD = 8.0;

    /** Rayon de bordure large */
    public static final double BORDER_RADIUS_LG = 12.0;

    /** Largeur de bordure standard */
    public static final double BORDER_WIDTH = 2.0;

    /** Couleur de bordure standard (alignée sur charte MAGSAV) */
    public static final String BORDER_COLOR = "#8B91FF";

    /** Style de bordure pour tableaux */
    public static final String TABLE_BORDER_STYLE = "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: " + BORDER_WIDTH + "px; " +
            "-fx-border-radius: " + BORDER_RADIUS_MD + "px;";

    // ========================================
    // 📏 TAILLES DE POLICE SUPPLÉMENTAIRES; //
    // ========================================

    /** Taille de police très petite (9px) */
    public static final double FONT_SIZE_XS = 9.0;

    /** Taille de police 11px */
    public static final double FONT_SIZE_11 = 11.0;

    /** Taille de police 13px */
    public static final double FONT_SIZE_13 = 13.0;

    /** Taille de police 14px */
    public static final double FONT_SIZE_14 = 14.0;

    /** Taille de police 16px */
    public static final double FONT_SIZE_16 = 16.0;

    /** Taille de police 24px (grands titres) */
    public static final double FONT_SIZE_24 = 24.0;

    // ========================================
    // 🎛️ STYLES CSS GÉNÉRIQUES; // ========================================

    /** Style pour les labels d'en-tête */
    public static final String HEADER_LABEL_STYLE = "-fx-text-fill: " + TEXT_ACCENT + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: " + FONT_SIZE_NORMAL + "px;";

    /** Style pour les labels secondaires */
    public static final String SECONDARY_LABEL_STYLE = "-fx-text-fill: " + SECONDARY_COLOR + "; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: " + FONT_SIZE_NORMAL + "px;";

    /** Style pour les titres de section */
    public static final String SECTION_TITLE_STYLE = "-fx-font-size: " + FONT_SIZE_16 + "px; " +
            "-fx-font-weight: bold;";

    /** Style pour les grands titres */
    public static final String LARGE_TITLE_STYLE = "-fx-font-size: " + FONT_SIZE_24 + "px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #333333;";

    /** Style pour les messages d'erreur */
    public static final String ERROR_MESSAGE_STYLE = "-fx-font-size: " + FONT_SIZE_TITLE + "px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #cc0000;";

    /** Style pour les messages informatifs */
    public static final String INFO_MESSAGE_STYLE = "-fx-font-size: " + FONT_SIZE_14 + "px; " +
            "-fx-text-fill: #666666;";

    /** Style pour les champs de saisie */
    public static final String INPUT_FIELD_STYLE = "-fx-background-color: " + BACKGROUND_SECONDARY + "; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-prompt-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-background-radius: " + BORDER_RADIUS_SM + "px;";

    /** Style pour les boutons standard (avec padding) */
    public static final String BUTTON_STYLE = "-fx-padding: " + SPACING_MD + "px; " +
            "-fx-font-size: " + FONT_SIZE_NORMAL + "px;";

    /** Style pour les boutons d'action principale */
    public static final String PRIMARY_BUTTON_STYLE = "-fx-background-color: " + SUCCESS_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: " + BORDER_RADIUS_SM + "px;";

    /** Style pour les boutons secondaires */
    public static final String SECONDARY_BUTTON_STYLE = "-fx-background-color: " + INFO_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: " + BORDER_RADIUS_SM + "px;";

    /** Style pour les boutons de danger */
    public static final String DANGER_BUTTON_STYLE = "-fx-background-color: " + ERROR_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: " + BORDER_RADIUS_SM + "px;";

    /** Style pour les boutons spéciaux */
    public static final String SPECIAL_BUTTON_STYLE = "-fx-background-color: " + SPECIAL_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: " + BORDER_RADIUS_SM + "px;";

    /** Style pour les boutons de détail */
    public static final String DETAIL_BUTTON_STYLE = "-fx-background-color: " + DETAIL_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: " + BORDER_RADIUS_SM + "px;";

    /** Style pour les toolbars */
    public static final String TOOLBAR_STYLE = "-fx-background-color: " + BACKGROUND_PRIMARY + "; " +
            "-fx-border-color: " + BORDER_COLOR + "; " +
            "-fx-border-width: " + BORDER_WIDTH + "px; " +
            "-fx-border-radius: " + BORDER_RADIUS_MD + "px; " +
            "-fx-background-radius: " + BORDER_RADIUS_MD + "px;";

    /** Classe CSS pour toolbar unifiée */
    public static final String UNIFIED_TOOLBAR_CLASS = "unified-toolbar";

    // ========================================
    // 🔧 MÉTHODES UTILITAIRES; // ========================================

    /**
     * Génère un style CSS pour un texte coloré selon le statut
     * 
     * @param status Le statut (ex: "Actif", "En cours", "Erreur")
     * @return Le style CSS approprié
     */
    public static String getStatusTextStyle(String status) {
        return switch (status.toLowerCase()) {
            case "actif", "disponible", "operationnel", "en cours", "ouverte", "resolue" ->
                "-fx-text-fill: " + SUCCESS_COLOR + ";";
            case "maintenance", "attente", "attente pieces", "en conge" ->
                "-fx-text-fill: " + WARNING_COLOR + ";";
            case "hors service", "panne", "fermee", "annulee", "inactif" ->
                "-fx-text-fill: " + ERROR_COLOR + ";";
            case "indisponible", "termine" ->
                "-fx-text-fill: " + INACTIVE_COLOR + ";";
            case "critique", "urgente" ->
                "-fx-text-fill: " + CRITICAL_COLOR + ";";
            default ->
                "-fx-text-fill: " + TEXT_PRIMARY + ";";
        };
    }

    /**
     * Génère un style CSS pour un arrière-plan coloré selon le statut
     * 
     * @param status Le statut
     * @return Le style CSS avec arrière-plan coloré
     */
    public static String getStatusBackgroundStyle(String status) {
        return switch (status.toLowerCase()) {
            case "actif", "disponible" ->
                "-fx-background-color: #e8f5e8; -fx-text-fill: #2e7d32;";
            case "maintenance", "attente" ->
                "-fx-background-color: #fff3e0; -fx-text-fill: #ef6c00;";
            case "hors service", "panne" ->
                "-fx-background-color: #ffebee; -fx-text-fill: #c62828;";
            case "termine", "inactif" ->
                "-fx-background-color: #f3e5f5; -fx-text-fill: #7b1fa2;";
            default ->
                "-fx-background-color: transparent; -fx-text-fill: " + TEXT_PRIMARY + ";";
        };
    }

    /**
     * Retourne la couleur JavaFX Color correspondant à une couleur hexadécimale
     * 
     * @param hexColor Couleur en format hexadécimal (ex: "#6B71F2")
     * @return L'objet Color JavaFX
     */
    public static Color getColor(String hexColor) {
        return Color.web(hexColor);
    }

    /**
     * Retourne une couleur avec transparence
     * 
     * @param hexColor Couleur de base
     * @param opacity  Opacité (0.0 à 1.0)
     * @return L'objet Color avec transparence
     */
    public static Color getColorWithOpacity(String hexColor, double opacity) {
        return Color.web(hexColor, opacity);
    }
}