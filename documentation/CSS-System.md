# Système CSS Centralisé MAGSAV

## Vue d'ensemble

Le système CSS centralisé de MAGSAV simplifie la gestion des styles et assure une cohérence visuelle à travers toute l'application. Fini le temps perdu à ajuster les styles lors de l'ajout de nouveaux modules !

## Architecture

### Structure des fichiers CSS

```
src/main/resources/css/
├── simple-dark.css      # Styles de base et thème principal
├── components.css       # Composants réutilisables
└── themes.css          # Variables et thèmes
```

### Classe CSSManager

La classe `CSSManager` est le point central pour tous les styles :

```java
// Instance singleton
CSSManager cssManager = CSSManager.getInstance();

// Application automatique des styles
cssManager.applyComponentStyle(myButton, "btn-primary");
cssManager.styleTitle(titleLabel);
cssManager.setTextColor(label, "#4CAF50");
```

## Utilisation

### 1. Initialisation dans un contrôleur

```java
import com.magsav.gui.utils.CSSManager;

public class MonController {
    private final CSSManager cssManager = CSSManager.getInstance();
    
    @FXML
    private void initialize() {
        // Applique le thème à la fenêtre
        Stage stage = (Stage) monElement.getScene().getWindow();
        cssManager.initializeWindow(stage, "dialog");
    }
}
```

### 2. Styles prédéfinis pour composants courants

#### Boutons
```java
cssManager.stylePrimaryButton(monBouton);      // Bouton principal bleu
cssManager.styleSecondaryButton(monBouton);    // Bouton secondaire gris
cssManager.styleDangerButton(monBouton);       // Bouton de danger rouge
```

#### Labels
```java
cssManager.styleTitle(titreLabel);             // Titre en gras
cssManager.styleSubtitle(sousTitreLabel);      // Sous-titre
cssManager.styleSuccessLabel(statusLabel);     // Statut succès (vert)
cssManager.styleErrorLabel(statusLabel);       // Statut erreur (rouge)
```

#### Conteneurs
```java
cssManager.stylePreferencesContainer(vbox);    // Conteneur de préférences
cssManager.stylePreferencesSection(section);   // Section de préférences
cssManager.styleDashboardCard(carte);          // Carte de dashboard
cssManager.styleSeparator(separator);          // Séparateur horizontal
```

### 3. Styles dynamiques

```java
// Application de couleurs personnalisées
cssManager.setTextColor(label, "#4CAF50");
cssManager.setBackgroundColor(panel, "#1a1a1a");
cssManager.setBorderColor(container, "#333333");

// Style personnalisé complet
cssManager.applyCustomStyle(element, "fx-font-size", "18px");
```

### 4. Application automatique des thèmes

```java
// Pour une nouvelle fenêtre
cssManager.initializeWindow(stage, "form");    // Fenêtre de formulaire
cssManager.initializeWindow(stage, "dialog");  // Dialogue
cssManager.initializeWindow(stage, "main");    // Fenêtre principale
```

## Classes CSS disponibles

### Boutons
- `.btn-primary` - Bouton principal (#4a90e2)
- `.btn-secondary` - Bouton secondaire (#6c757d)  
- `.btn-danger` - Bouton de danger (#dc3545)

### Textes
- `.title-label` - Titre (16px, gras, blanc)
- `.subtitle-label` - Sous-titre (14px, gras, blanc)
- `.status-success` - Statut succès (vert)
- `.status-error` - Statut erreur (rouge)

### Conteneurs
- `.preferences-container` - Conteneur de préférences
- `.preferences-section` - Section de préférences avec bordure
- `.dashboard-card` - Carte de dashboard
- `.separator` - Séparateur horizontal

## Migration depuis les styles inline

### Avant (problématique)
```java
// Styles dispersés et difficiles à maintenir
label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
button.setStyle("-fx-background-color: #4a90e2; -fx-text-fill: white;");
container.setStyle("-fx-padding: 15; -fx-border-color: #333333; -fx-border-radius: 8px;");
```

### Après (centralisé)
```java
// Styles cohérents et maintenables
cssManager.styleTitle(label);
cssManager.stylePrimaryButton(button);
cssManager.applyComponentStyle(container, "preferences-section");
```

## Avantages

1. **Cohérence visuelle** - Tous les composants utilisent les mêmes styles
2. **Maintenance simplifiée** - Modification globale depuis un seul endroit
3. **Ajout de modules facilité** - Les nouveaux composants héritent automatiquement des styles
4. **Performance améliorée** - Styles CSS natifs plus rapides que setStyle()
5. **Thèmes dynamiques** - Possibilité de changer de thème en temps réel

## Constantes de couleurs

```java
CSSManager.PRIMARY_BACKGROUND    // #1e3a5f (bleu marine)
CSSManager.SECONDARY_BACKGROUND  // #1a1a1a (noir)
CSSManager.ACCENT_COLOR         // #4a90e2 (bleu accent)
CSSManager.SUCCESS_COLOR        // #4CAF50 (vert)
CSSManager.ERROR_COLOR          // #dc3545 (rouge)
CSSManager.TEXT_PRIMARY         // #ffffff (blanc)
```

## Bonnes pratiques

1. **Utilisez toujours CSSManager** au lieu de setStyle() directement
2. **Initialisez le CSS** dans la méthode initialize() de chaque contrôleur
3. **Préférez les classes prédéfinies** aux styles personnalisés
4. **Documentez les nouveaux styles** ajoutés dans components.css
5. **Testez sur toutes les fenêtres** après modification des styles globaux

## Exemples complets

### Nouveau contrôleur de formulaire
```java
public class FormulaireController {
    private final CSSManager cssManager = CSSManager.getInstance();
    
    @FXML private VBox containerPrincipal;
    @FXML private Label titreFormulaire;
    @FXML private Button btnValider;
    @FXML private Button btnAnnuler;
    
    @FXML
    private void initialize() {
        // Application du thème
        Platform.runLater(() -> {
            Stage stage = (Stage) containerPrincipal.getScene().getWindow();
            cssManager.initializeWindow(stage, "form");
        });
        
        // Styles des composants
        cssManager.styleTitle(titreFormulaire);
        cssManager.stylePrimaryButton(btnValider);
        cssManager.styleSecondaryButton(btnAnnuler);
        cssManager.applyComponentStyle(containerPrincipal, "preferences-container");
    }
}
```

Ce système élimine les problèmes de maintenance CSS et assure une expérience utilisateur cohérente dans toute l'application MAGSAV.