package com.magscene.magsav.desktop.view.base;

import com.magscene.magsav.desktop.service.ApiService;
import com.magscene.magsav.desktop.theme.ThemeConstants;
import com.magscene.magsav.desktop.theme.ThemeManager;
import com.magscene.magsav.desktop.util.ViewUtils;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Classe abstraite pour standardiser toutes les vues Manager de MAGSAV
 * 
 * STRUCTURE STANDARD OBLIGATOIRE :
 * BorderPane
 * ├── Top: HBox (toolbar standard)
 * │ ├── Left: Filtres (search, filters...)
 * │ ├── Spacer: Region
 * │ └── Right: Actions (add, edit, view, delete...)
 * └── Center: Contenu (TableView + DetailPanel ou CustomTabPane)
 * 
 * RESPONSABILITÉS :
 * - Configuration de base uniforme
 * - Toolbar standardisée avec ViewUtils
 * - Gestion des thèmes automatique
 * - Pattern filtres/actions respecté
 * 
 * @author MAGSAV Architecture Team
 * @since 3.0
 */
public abstract class AbstractManagerView extends BorderPane {

    // ========================================
    // 📊 CONSTANTES DE LAYOUT STANDARD; // ========================================

    /** Espacement dans la toolbar - Utilise ThemeConstants.SPACING_MD */
    public static final double TOOLBAR_SPACING = ThemeConstants.SPACING_MD;

    /** Padding de la toolbar - Utilise ThemeConstants.SPACING_MD */
    public static final double TOOLBAR_PADDING = ThemeConstants.SPACING_MD;

    /** Style CSS de la toolbar - Utilise ThemeConstants.TOOLBAR_STYLE */
    public static final String TOOLBAR_STYLE = ThemeConstants.TOOLBAR_STYLE;

    // ========================================
    // 🔧 COMPOSANTS COMMUNS; // ========================================

    protected final ApiService apiService;
    protected HBox standardToolbar;
    protected TextField searchField;
    protected VBox filtersContainer;
    protected VBox actionsContainer;

    // ========================================
    // 🏗️ CONSTRUCTEUR ET INITIALISATION; //
    // ========================================

    protected AbstractManagerView(ApiService apiService) {
        this.apiService = apiService;

        // Configuration de base uniforme
        setupBaseStyle();

        // Structure standard obligatoire
        initializeStandardLayout();

        // Délégation aux sous-classes pour le contenu spécifique
        initializeContent();
        createFilters();
        createActions();

        // Assemblage final standard
        assembleStandardLayout();

        // Configuration finale
        finalizeSetup();
    }

    // ========================================
    // 🎨 CONFIGURATION DE BASE STANDARD; //
    // ========================================

    private void setupBaseStyle() {
        // Style de base uniforme pour toutes les vues
        setStyle("-fx-background-color: " + ThemeManager.getInstance().getCurrentBackgroundColor() + ";");

        // CSS class pour identification et styling spécifique
        getStyleClass().add("standard-manager-view");
        getStyleClass().add(getViewCssClass());

        // S'assurer que la vue prend tout l'espace disponible
        setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    private void initializeStandardLayout() {
        // Toolbar standard obligatoire
        standardToolbar = new HBox(TOOLBAR_SPACING);
        standardToolbar.setAlignment(Pos.CENTER_LEFT);
        standardToolbar.setPadding(new Insets(TOOLBAR_PADDING));
        standardToolbar.setStyle(TOOLBAR_STYLE);

        // Marges externes pour la toolbar - Utilise ThemeConstants.TOOLBAR_MARGIN
        VBox.setMargin(standardToolbar, ThemeConstants.TOOLBAR_MARGIN);

        // Conteneurs pour filtres et actions - Utilise ThemeConstants.SPACING_XS
        filtersContainer = new VBox(ThemeConstants.SPACING_XS);
        actionsContainer = new VBox(ThemeConstants.SPACING_XS);
    }

    private void assembleStandardLayout() {
        // 🔍 Zone de recherche standardisée (toujours présente)
        VBox searchBox = ViewUtils.createSearchBox("🔍 Recherche", getSearchPromptText(), this::onSearchTextChanged);
        searchField = (TextField) searchBox.getChildren().get(1);

        // Force des couleurs uniformes
        com.magscene.magsav.desktop.MagsavDesktopApplication.forceSearchFieldColors(searchField);

        // 📂 Conteneur des filtres spécifiques
        HBox filtersHBox = new HBox(10);
        filtersHBox.getChildren().addAll(filtersContainer.getChildren());

        // ⚡ Label et conteneur des actions
        Label actionsLabel = new Label("⚡ Actions");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        actionsLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        VBox actionsBox = new VBox(5);
        HBox buttonRow = new HBox(10);
        buttonRow.getChildren().addAll(actionsContainer.getChildren());
        actionsBox.getChildren().addAll(actionsLabel, buttonRow);

        // 🔧 Spacer pour pousser les actions à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 🏗️ Assemblage de la toolbar standard
        standardToolbar.getChildren().clear();
        standardToolbar.getChildren().addAll(searchBox, filtersHBox, spacer, actionsBox);

        // 📍 Positionnement standard BorderPane
        setTop(standardToolbar);
        setCenter(createCenterContent());
    }

    // ========================================
    // 🔧 MÉTHODES ABSTRAITES À IMPLÉMENTER; //
    // ========================================

    /**
     * Retourne la classe CSS spécifique à cette vue pour le styling
     * Exemple : "equipment-manager", "personnel-manager", etc.
     */
    protected abstract String getViewCssClass();

    /**
     * Retourne le texte d'aide pour le champ de recherche
     * Exemple : "Nom, marque, modèle...", "Nom, prénom, email..."
     */
    protected abstract String getSearchPromptText();

    /**
     * Initialise le contenu spécifique de la vue (tables, composants...)
     * Appelé avant la création des filtres et actions
     */
    protected abstract void initializeContent();

    /**
     * Crée les filtres spécifiques à cette vue et les ajoute à filtersContainer
     * Utiliser ViewUtils.createFilterBox() pour la cohérence
     */
    protected abstract void createFilters();

    /**
     * Crée les boutons d'action spécifiques et les ajoute à actionsContainer
     * Utiliser ViewUtils.create*Button() pour la cohérence
     */
    protected abstract void createActions();

    /**
     * Retourne le contenu central de la vue (TableView + DetailPanel,
     * CustomTabPane...)
     * Ce contenu sera automatiquement placé dans le center du BorderPane
     */
    protected abstract Region createCenterContent();

    /**
     * Appelé quand le texte de recherche change
     * Implémenter la logique de filtrage spécifique
     */
    protected abstract void onSearchTextChanged(String searchText);

    // ========================================
    // 🔧 MÉTHODES OPTIONNELLES (avec implémentation par défaut); //
    // ========================================

    /**
     * Configuration finale après assemblage complet
     * Override si nécessaire pour des configurations spécifiques
     */
    protected void finalizeSetup() {
        // Application du thème directement sur la vue (la scène sera stylée au niveau
        // supérieur)
        this.getStyleClass().add("theme-light");

        // Log pour debug
        System.out.println("✅ " + getClass().getSimpleName() + " initialisé avec structure standard");
    }

    /**
     * Rafraîchit la vue (rechargement des données, etc.)
     * Implémentation par défaut vide, override si nécessaire
     */
    protected void refresh() {
        System.out.println("🔄 Rafraîchissement de " + getClass().getSimpleName());
    }

    /**
     * Nettoie les ressources (listeners, tâches, etc.)
     * Override si des cleanups spécifiques sont nécessaires
     */
    protected void cleanup() {
        System.out.println("🧹 Nettoyage de " + getClass().getSimpleName());
    }

    // ========================================
    // 🛠️ UTILITAIRES POUR LES SOUS-CLASSES; //
    // ========================================

    /**
     * Ajoute un filtre standardisé à la zone de filtres
     */
    protected void addFilter(String label, String[] options, String defaultValue,
            java.util.function.Consumer<String> onSelectionChanged) {
        VBox filterBox = ViewUtils.createFilterBox(label, options, defaultValue, onSelectionChanged);
        filtersContainer.getChildren().add(filterBox);
    }

    /**
     * Ajoute un bouton d'action standardisé
     */
    protected void addActionButton(Button button) {
        actionsContainer.getChildren().add(button);
    }

    /**
     * Retourne le champ de recherche pour binding ou configuration avancée
     */
    protected TextField getSearchField() {
        return searchField;
    }

    /**
     * Retourne l'ApiService pour les sous-classes
     */
    protected ApiService getApiService() {
        return apiService;
    }
}