package com.magscene.magsav.backend.service;

import com.magsav.entities.*;
import org.springframework.stereotype.Service;

/**
 * Service pour la gestion des notifications
 * TODO: Implémenter l'intégration avec un système de notifications réel
 */
@Service
public class NotificationService {
    
    // *** NOTIFICATIONS DEMANDES DE MATÉRIEL ***
    
    public void sendApprovalNotification(MaterialRequest request) {
        // TODO: Envoyer notification aux administrateurs
        System.out.println("NOTIFICATION: Nouvelle demande en attente d'approbation - " + request.getRequestNumber());
    }
    
    public void sendApprovalConfirmation(MaterialRequest request) {
        // TODO: Envoyer notification au demandeur
        System.out.println("NOTIFICATION: Demande approuvée - " + request.getRequestNumber() + " pour " + request.getRequesterName());
    }
    
    public void sendRejectionNotification(MaterialRequest request) {
        // TODO: Envoyer notification au demandeur
        System.out.println("NOTIFICATION: Demande rejetée - " + request.getRequestNumber() + " pour " + request.getRequesterName());
    }
    
    // *** NOTIFICATIONS COMMANDES GROUPÉES ***
    
    public void sendThresholdAlert(GroupedOrder order) {
        // TODO: Envoyer alerte seuil atteint
        System.out.println("NOTIFICATION: Seuil atteint pour commande groupée - " + order.getOrderNumber() + 
                          " chez " + order.getSupplier().getName());
    }
    
    public void sendOrderValidationNotification(GroupedOrder order) {
        // TODO: Notifier la validation de commande
        System.out.println("NOTIFICATION: Commande groupée validée - " + order.getOrderNumber() + 
                          " par " + order.getValidatedBy());
    }
    
    public void sendOrderCreationNotification(GroupedOrder order) {
        // TODO: Notifier la création de commande fournisseur
        System.out.println("NOTIFICATION: Commande fournisseur créée pour " + order.getSupplier().getName());
    }
    
    // *** NOTIFICATIONS LIVRAISONS ***
    
    public void sendDeliveryNotification(OrderAllocation allocation) {
        // TODO: Notifier les livraisons
        System.out.println("NOTIFICATION: Livraison reçue - " + allocation.getItemReference() + 
                          " pour " + allocation.getRequesterName());
    }
}