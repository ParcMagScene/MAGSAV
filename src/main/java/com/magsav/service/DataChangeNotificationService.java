package com.magsav.service;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import javafx.application.Platform;

/**
 * Service centralisé pour la gestion des événements de changement de données.
 * Permet un rafraîchissement automatique et transparent de toutes les interfaces.
 */
public class DataChangeNotificationService {
    
    private static final DataChangeNotificationService INSTANCE = new DataChangeNotificationService();
    
    // Liste thread-safe des observateurs
    private final CopyOnWriteArrayList<Consumer<DataChangeEvent>> listeners = new CopyOnWriteArrayList<>();
    
    private DataChangeNotificationService() {
        // Singleton
    }
    
    public static DataChangeNotificationService getInstance() {
        return INSTANCE;
    }
    
    /**
     * Enregistre un observateur pour recevoir les notifications de changement de données
     */
    public void subscribe(Consumer<DataChangeEvent> listener) {
        listeners.add(listener);
    }
    
    /**
     * Désenregistre un observateur
     */
    public void unsubscribe(Consumer<DataChangeEvent> listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifie tous les observateurs d'un changement de données
     * Les notifications sont exécutées sur le thread JavaFX
     */
    public void notifyDataChanged(DataChangeEvent event) {
        Platform.runLater(() -> {
            for (Consumer<DataChangeEvent> listener : listeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    System.err.println("Erreur lors de la notification d'événement: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Méthodes de convenance pour les événements les plus courants
     */
    public void notifyProductsImported(int count) {
        notifyDataChanged(new DataChangeEvent(
            DataChangeEvent.Type.PRODUCTS_IMPORTED, 
            count + " produits importés"
        ));
    }
    
    public void notifyProductCreated(String productName) {
        notifyDataChanged(new DataChangeEvent(
            DataChangeEvent.Type.PRODUCT_CREATED, 
            "Produit créé: " + productName
        ));
    }
    
    public void notifyProductUpdated(String productName) {
        notifyDataChanged(new DataChangeEvent(
            DataChangeEvent.Type.PRODUCT_UPDATED, 
            "Produit modifié: " + productName
        ));
    }
    
    public void notifyProductDeleted(String productName) {
        notifyDataChanged(new DataChangeEvent(
            DataChangeEvent.Type.PRODUCT_DELETED, 
            "Produit supprimé: " + productName
        ));
    }
}