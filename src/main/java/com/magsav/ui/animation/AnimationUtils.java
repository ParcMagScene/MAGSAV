package com.magsav.ui.animation;

import javafx.scene.Node;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Utilitaires d'animation pour les composants modernes
 * Facilite l'intégration des animations dans les contrôleurs
 */
public class AnimationUtils {
    
    private static final AnimationService animationService = AnimationService.getInstance();
    
    /**
     * Applique les animations de hover sur un bouton
     */
    public static void makeButtonInteractive(Node button) {
        button.setOnMouseEntered(e -> animationService.hoverElevate(button));
        button.setOnMouseExited(e -> animationService.hoverLower(button));
        button.setOnMousePressed(e -> animationService.quickPulse(button));
    }
    
    /**
     * Applique les animations de hover sur une carte/panel
     */
    public static void makeCardInteractive(Node card) {
        card.setOnMouseEntered(e -> animationService.elevate(card, AnimationService.Speed.FAST));
        card.setOnMouseExited(e -> animationService.lower(card, AnimationService.Speed.FAST));
    }
    
    /**
     * Animation d'entrée pour les modals/dialogs
     */
    public static void showModal(Node modal) {
        animationService.scaleIn(modal, AnimationService.Speed.NORMAL, AnimationService.Easing.SPRING);
    }
    
    /**
     * Animation de sortie pour les modals/dialogs
     */
    public static void hideModal(Node modal) {
        animationService.fadeOut(modal, AnimationService.Speed.FAST, AnimationService.Easing.EASE_IN, true);
    }
    
    /**
     * Animation d'erreur (shake + pulse rouge)
     */
    public static void showError(Node node) {
        animationService.shake(node, AnimationService.Speed.FAST);
        // Ajouter un effet visuel d'erreur si nécessaire
    }
    
    /**
     * Animation de succès (pulse vert)
     */
    public static void showSuccess(Node node) {
        animationService.pulse(node, AnimationService.Speed.NORMAL);
        // Ajouter un effet visuel de succès si nécessaire
    }
    
    /**
     * Animation d'apparition progressive pour les listes
     */
    public static void staggerIn(Node[] nodes, Duration delay) {
        for (int i = 0; i < nodes.length; i++) {
            Timeline timeline = animationService.fadeIn(nodes[i], AnimationService.Speed.NORMAL, AnimationService.Easing.EASE_OUT);
            timeline.setDelay(delay.multiply(i));
        }
    }
    
    /**
     * Animation de chargement (rotation continue)
     */
    public static Timeline showLoading(Node loadingIcon) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(
            new javafx.animation.KeyFrame(Duration.seconds(1),
                new javafx.animation.KeyValue(loadingIcon.rotateProperty(), 360)
            )
        );
        timeline.play();
        return timeline;
    }
    
    /**
     * Arrête l'animation de chargement
     */
    public static void hideLoading(Timeline loadingTimeline, Node loadingIcon) {
        if (loadingTimeline != null) {
            loadingTimeline.stop();
        }
        loadingIcon.setRotate(0);
        animationService.fadeOut(loadingIcon);
    }
}