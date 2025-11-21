package com.magscene.magsav.desktop.view.base;

import com.magscene.magsav.desktop.core.di.ApplicationContext;
import javafx.scene.layout.Pane;

/**
 * Classe de base pour toutes les vues de l'application
 * Fournit l'injection de dépendances et les fonctionnalités communes
 */
public abstract class BaseView extends Pane {
    protected final ApplicationContext context;
    
    public BaseView() {
        this.context = ApplicationContext.getInstance();
        injectDependencies();
        initializeView();
        setupStyling();
    }
    
    /**
     * Injection automatique des dépendances
     * À surcharger dans les classes filles pour injecter les services spécifiques
     */
    protected void injectDependencies() {
        // Les classes filles peuvent surcharger cette méthode; // pour injecter leurs dépendances spécifiques
    }
    
    /**
     * Initialisation de la vue
     * À implémenter dans les classes filles
     */
    protected abstract void initializeView();
    
    /**
     * Configuration du style CSS
     */
    protected void setupStyling() {
        getStyleClass().add("base-view");
        getStyleClass().add(getViewCssClass());
    }
    
    /**
     * Retourne la classe CSS spécifique à cette vue
     */
    protected abstract String getViewCssClass();
    
    /**
     * Rafraîchit les données de la vue
     */
    public void refresh() {
        // Implémentation par défaut - à surcharger si nécessaire
        System.out.println("🔄 Rafraîchissement: " + getClass().getSimpleName());
    }
    
    /**
     * Nettoyage des ressources avant destruction
     */
    public void cleanup() {
        // Implémentation par défaut - à surcharger si nécessaire
        System.out.println("🧹 Nettoyage: " + getClass().getSimpleName());
    }
    
    /**
     * Méthode utilitaire pour récupérer un service du contexte
     */
    protected <T> T getService(Class<T> serviceClass) {
        return context.getInstance(serviceClass);
    }
}