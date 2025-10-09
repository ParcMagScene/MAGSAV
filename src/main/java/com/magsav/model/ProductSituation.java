package com.magsav.model;

/**
 * Énumération des situations possibles pour un produit
 */
public enum ProductSituation {
    EN_STOCK("En stock"),
    EN_SERVICE("En service"),
    EN_REPARATION("En réparation"),
    SAV_MAG_SCENE("SAV Mag Scene"),
    SAV_EXTERNE("SAV Externe"),
    VENDU("Vendu"),
    DECHET("Déchet");

    private final String label;

    ProductSituation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Retourne la situation correspondant au label donné
     */
    public static ProductSituation fromLabel(String label) {
        if (label == null) {
            return EN_STOCK; // valeur par défaut
        }
        
        for (ProductSituation situation : values()) {
            if (situation.label.equals(label)) {
                return situation;
            }
        }
        
        return EN_STOCK; // valeur par défaut si aucune correspondance
    }

    /**
     * Vérifie si le label donné correspond à une situation valide
     */
    public static boolean isValid(String label) {
        if (label == null) {
            return false;
        }
        
        for (ProductSituation situation : values()) {
            if (situation.label.equals(label)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Retourne toutes les situations possibles sous forme de labels
     */
    public static String[] getAllLabels() {
        return new String[] {
            EN_STOCK.label,
            EN_SERVICE.label,
            EN_REPARATION.label,
            SAV_MAG_SCENE.label,
            SAV_EXTERNE.label,
            VENDU.label,
            DECHET.label
        };
    }

    @Override
    public String toString() {
        return label;
    }
}