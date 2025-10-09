package com.magsav.ui.components;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Gestionnaire de notifications toast non-intrusives
 * Affiche des messages temporaires en overlay
 */
public final class NotificationManager {
    
    private static final ConcurrentLinkedQueue<Popup> activeNotifications = new ConcurrentLinkedQueue<>();
    private static final int MAX_NOTIFICATIONS = 5;
    private static final double NOTIFICATION_WIDTH = 300;
    private static final double NOTIFICATION_HEIGHT = 60;
    private static final double SPACING = 10;
    
    private NotificationManager() {}
    
    /**
     * Types de notifications
     */
    public enum NotificationType {
        SUCCESS("#4CAF50", "#ffffff"),   // Vert
        INFO("#2196F3", "#ffffff"),      // Bleu
        WARNING("#FF9800", "#ffffff"),   // Orange
        ERROR("#f44336", "#ffffff");     // Rouge
        
        private final String backgroundColor;
        private final String textColor;
        
        NotificationType(String backgroundColor, String textColor) {
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
        }
        
        public String getBackgroundColor() { return backgroundColor; }
        public String getTextColor() { return textColor; }
    }
    
    /**
     * Affiche une notification de succès
     */
    public static void showSuccess(String message) {
        showNotification(message, NotificationType.SUCCESS, 3000);
    }
    
    /**
     * Affiche une notification d'information
     */
    public static void showInfo(String message) {
        showNotification(message, NotificationType.INFO, 4000);
    }
    
    /**
     * Affiche une notification d'avertissement
     */
    public static void showWarning(String message) {
        showNotification(message, NotificationType.WARNING, 5000);
    }
    
    /**
     * Affiche une notification d'erreur
     */
    public static void showError(String message) {
        showNotification(message, NotificationType.ERROR, 6000);
    }
    
    /**
     * Affiche une notification personnalisée
     */
    public static void showNotification(String message, NotificationType type, long durationMs) {
        Platform.runLater(() -> {
            // Limiter le nombre de notifications
            if (activeNotifications.size() >= MAX_NOTIFICATIONS) {
                Popup oldest = activeNotifications.poll();
                if (oldest != null) {
                    oldest.hide();
                }
            }
            
            // Créer la notification
            Popup notification = createNotification(message, type);
            activeNotifications.offer(notification);
            
            // Trouver la fenêtre active pour positionner la notification
            Window activeWindow = findActiveWindow();
            if (activeWindow != null) {
                showNotificationOnWindow(notification, activeWindow, durationMs);
            }
        });
    }
    
    /**
     * Crée une popup de notification
     */
    private static Popup createNotification(String message, NotificationType type) {
        // Créer le contenu
        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.valueOf(type.getTextColor()));
        messageLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(NOTIFICATION_WIDTH - 20);
        
        // Conteneur avec style
        StackPane container = new StackPane();
        container.getChildren().add(messageLabel);
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(NOTIFICATION_WIDTH, NOTIFICATION_HEIGHT);
        container.setMinHeight(NOTIFICATION_HEIGHT);
        container.setMaxWidth(NOTIFICATION_WIDTH);
        
        // Style du conteneur
        BackgroundFill fill = new BackgroundFill(
            Color.valueOf(type.getBackgroundColor()),
            new CornerRadii(8),
            Insets.EMPTY
        );
        container.setBackground(new Background(fill));
        container.setPadding(new Insets(10));
        
        // Ombre
        container.setStyle(container.getStyle() + 
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.3, 2, 2);");
        
        // Créer la popup
        Popup popup = new Popup();
        popup.getContent().add(container);
        popup.setAutoHide(false);
        popup.setHideOnEscape(false);
        
        return popup;
    }
    
    /**
     * Affiche la notification sur une fenêtre spécifique
     */
    private static void showNotificationOnWindow(Popup notification, Window window, long durationMs) {
        // Calculer la position
        double x = window.getX() + window.getWidth() - NOTIFICATION_WIDTH - 20;
        double y = window.getY() + 80 + (activeNotifications.size() - 1) * (NOTIFICATION_HEIGHT + SPACING);
        
        // Afficher la notification
        notification.show(window, x, y);
        
        // Animation d'apparition
        StackPane content = (StackPane) notification.getContent().get(0);
        content.setOpacity(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Programmer la disparition
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(durationMs), e -> {
            hideNotification(notification);
        }));
        timeline.play();
    }
    
    /**
     * Cache une notification avec animation
     */
    private static void hideNotification(Popup notification) {
        if (notification.isShowing()) {
            StackPane content = (StackPane) notification.getContent().get(0);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), content);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                notification.hide();
                activeNotifications.remove(notification);
                repositionNotifications();
            });
            fadeOut.play();
        }
    }
    
    /**
     * Repositionne les notifications après la suppression d'une notification
     */
    private static void repositionNotifications() {
        Window activeWindow = findActiveWindow();
        if (activeWindow == null) return;
        
        int index = 0;
        for (Popup notification : activeNotifications) {
            if (notification.isShowing()) {
                double x = activeWindow.getX() + activeWindow.getWidth() - NOTIFICATION_WIDTH - 20;
                double y = activeWindow.getY() + 80 + index * (NOTIFICATION_HEIGHT + SPACING);
                notification.setX(x);
                notification.setY(y);
                index++;
            }
        }
    }
    
    /**
     * Trouve la fenêtre active
     */
    private static Window findActiveWindow() {
        // Essayer de trouver une fenêtre focalisée
        for (Window window : Window.getWindows()) {
            if (window.isShowing() && window.isFocused()) {
                return window;
            }
        }
        
        // Fallback: prendre la première fenêtre visible
        for (Window window : Window.getWindows()) {
            if (window.isShowing()) {
                return window;
            }
        }
        
        return null;
    }
    
    /**
     * Cache toutes les notifications actives
     */
    public static void hideAll() {
        Platform.runLater(() -> {
            for (Popup notification : activeNotifications) {
                if (notification.isShowing()) {
                    notification.hide();
                }
            }
            activeNotifications.clear();
        });
    }
    
    /**
     * Méthodes de convenance pour MAGSAV
     */
    public static class MAGSAV {
        
        public static void operationSuccess(String operation) {
            showSuccess(operation + " effectuée avec succès");
        }
        
        public static void operationError(String operation, String error) {
            showError("Erreur lors de " + operation + " : " + error);
        }
        
        public static void itemSaved(String itemType) {
            showSuccess(itemType + " enregistré(e) avec succès");
        }
        
        public static void itemDeleted(String itemType) {
            showSuccess(itemType + " supprimé(e) avec succès");
        }
        
        public static void validationError(String message) {
            showWarning("Validation : " + message);
        }
        
        public static void importProgress(int processed, int total) {
            showInfo("Import en cours : " + processed + "/" + total + " éléments traités");
        }
        
        public static void importCompleted(int created, int errors) {
            if (errors == 0) {
                showSuccess("Import terminé : " + created + " éléments créés");
            } else {
                showWarning("Import terminé : " + created + " créés, " + errors + " erreurs");
            }
        }
        
        public static void cacheRefreshed() {
            showInfo("Cache des données rafraîchi");
        }
        
        public static void performanceAlert(String operation, long durationMs) {
            if (durationMs > 5000) {
                showWarning("Opération lente détectée : " + operation + " (" + durationMs + "ms)");
            }
        }
    }
}