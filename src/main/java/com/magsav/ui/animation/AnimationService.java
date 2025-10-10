package com.magsav.ui.animation;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import com.magsav.util.AppLogger;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Service d'animations modernes pour MAGSAV
 * Transitions fluides, micro-interactions et feedback visuel
 */
public class AnimationService {
    
    private static AnimationService instance;
    private final ConcurrentHashMap<Node, Timeline> runningAnimations = new ConcurrentHashMap<>();
    
    // Durées standard
    public enum Speed {
        ULTRA_FAST(Duration.millis(100)),
        FAST(Duration.millis(200)),
        NORMAL(Duration.millis(300)),
        SLOW(Duration.millis(500)),
        ULTRA_SLOW(Duration.millis(800));
        
        private final Duration duration;
        
        Speed(Duration duration) {
            this.duration = duration;
        }
        
        public Duration getDuration() {
            return duration;
        }
    }
    
    // Types d'interpolation
    public enum Easing {
        LINEAR(Interpolator.LINEAR),
        EASE_IN(Interpolator.EASE_IN),
        EASE_OUT(Interpolator.EASE_OUT),
        EASE_BOTH(Interpolator.EASE_BOTH),
        SPRING(createSpringInterpolator()),
        BOUNCE(createBounceInterpolator());
        
        private final Interpolator interpolator;
        
        Easing(Interpolator interpolator) {
            this.interpolator = interpolator;
        }
        
        public Interpolator getInterpolator() {
            return interpolator;
        }
    }
    
    private AnimationService() {
        AppLogger.info("AnimationService initialisé");
    }
    
    public static AnimationService getInstance() {
        if (instance == null) {
            instance = new AnimationService();
        }
        return instance;
    }
    
    /**
     * Animation de fade-in
     */
    public Timeline fadeIn(Node node, Speed speed, Easing easing) {
        stopExistingAnimation(node);
        
        node.setOpacity(0);
        node.setVisible(true);
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.opacityProperty(), 1.0, easing.getInterpolator())
            )
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de fade-out
     */
    public Timeline fadeOut(Node node, Speed speed, Easing easing) {
        return fadeOut(node, speed, easing, true);
    }
    
    /**
     * Animation de fade-out avec option de masquage
     */
    public Timeline fadeOut(Node node, Speed speed, Easing easing, boolean hideOnComplete) {
        stopExistingAnimation(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.opacityProperty(), 0.0, easing.getInterpolator())
            )
        );
        
        if (hideOnComplete) {
            timeline.setOnFinished(e -> {
                node.setVisible(false);
                runningAnimations.remove(node);
            });
        } else {
            timeline.setOnFinished(e -> runningAnimations.remove(node));
        }
        
        runningAnimations.put(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de slide depuis le bas
     */
    public Timeline slideInFromBottom(Node node, Speed speed, Easing easing) {
        stopExistingAnimation(node);
        
        double originalY = node.getTranslateY();
        node.setTranslateY(originalY + 50);
        node.setOpacity(0);
        node.setVisible(true);
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.translateYProperty(), originalY, easing.getInterpolator()),
                new KeyValue(node.opacityProperty(), 1.0, easing.getInterpolator())
            )
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de slide vers le haut
     */
    public Timeline slideOutToTop(Node node, Speed speed, Easing easing) {
        stopExistingAnimation(node);
        
        double targetY = node.getTranslateY() - 50;
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.translateYProperty(), targetY, easing.getInterpolator()),
                new KeyValue(node.opacityProperty(), 0.0, easing.getInterpolator())
            )
        );
        
        timeline.setOnFinished(e -> {
            node.setVisible(false);
            node.setTranslateY(0); // Reset position
            runningAnimations.remove(node);
        });
        
        runningAnimations.put(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de scale/zoom
     */
    public Timeline scaleIn(Node node, Speed speed, Easing easing) {
        stopExistingAnimation(node);
        
        node.setScaleX(0.0);
        node.setScaleY(0.0);
        node.setOpacity(0);
        node.setVisible(true);
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.scaleXProperty(), 1.0, easing.getInterpolator()),
                new KeyValue(node.scaleYProperty(), 1.0, easing.getInterpolator()),
                new KeyValue(node.opacityProperty(), 1.0, easing.getInterpolator())
            )
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de pulse (effet de battement)
     */
    public Timeline pulse(Node node, Speed speed) {
        stopExistingAnimation(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(node.scaleXProperty(), 1.0),
                new KeyValue(node.scaleYProperty(), 1.0)
            ),
            new KeyFrame(speed.getDuration().multiply(0.5),
                new KeyValue(node.scaleXProperty(), 1.05, Easing.EASE_BOTH.getInterpolator()),
                new KeyValue(node.scaleYProperty(), 1.05, Easing.EASE_BOTH.getInterpolator())
            ),
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.scaleXProperty(), 1.0, Easing.EASE_BOTH.getInterpolator()),
                new KeyValue(node.scaleYProperty(), 1.0, Easing.EASE_BOTH.getInterpolator())
            )
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de shake (tremblement)
     */
    public Timeline shake(Node node, Speed speed) {
        stopExistingAnimation(node);
        
        double originalX = node.getTranslateX();
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(node.translateXProperty(), originalX)),
            new KeyFrame(speed.getDuration().multiply(0.1), new KeyValue(node.translateXProperty(), originalX + 10)),
            new KeyFrame(speed.getDuration().multiply(0.2), new KeyValue(node.translateXProperty(), originalX - 10)),
            new KeyFrame(speed.getDuration().multiply(0.3), new KeyValue(node.translateXProperty(), originalX + 8)),
            new KeyFrame(speed.getDuration().multiply(0.4), new KeyValue(node.translateXProperty(), originalX - 8)),
            new KeyFrame(speed.getDuration().multiply(0.5), new KeyValue(node.translateXProperty(), originalX + 6)),
            new KeyFrame(speed.getDuration().multiply(0.6), new KeyValue(node.translateXProperty(), originalX - 6)),
            new KeyFrame(speed.getDuration().multiply(0.7), new KeyValue(node.translateXProperty(), originalX + 4)),
            new KeyFrame(speed.getDuration().multiply(0.8), new KeyValue(node.translateXProperty(), originalX - 4)),
            new KeyFrame(speed.getDuration().multiply(0.9), new KeyValue(node.translateXProperty(), originalX + 2)),
            new KeyFrame(speed.getDuration(), new KeyValue(node.translateXProperty(), originalX))
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation d'élévation avec ombre
     */
    public Timeline elevate(Node node, Speed speed) {
        stopExistingAnimation(node);
        
        DropShadow lightShadow = new DropShadow(4, 0, 2, Color.color(0, 0, 0, 0.1));
        DropShadow heavyShadow = new DropShadow(12, 0, 6, Color.color(0, 0, 0, 0.3));
        
        node.setEffect(lightShadow);
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.translateYProperty(), -4, Easing.EASE_OUT.getInterpolator())
            )
        );
        
        timeline.setOnFinished(e -> {
            node.setEffect(heavyShadow);
            runningAnimations.remove(node);
        });
        
        runningAnimations.put(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de retour au niveau normal
     */
    public Timeline lower(Node node, Speed speed) {
        stopExistingAnimation(node);
        
        DropShadow lightShadow = new DropShadow(4, 0, 2, Color.color(0, 0, 0, 0.1));
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.translateYProperty(), 0, Easing.EASE_OUT.getInterpolator())
            )
        );
        
        timeline.setOnFinished(e -> {
            node.setEffect(lightShadow);
            runningAnimations.remove(node);
        });
        
        runningAnimations.put(node, timeline);
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de rotation
     */
    public Timeline rotate(Node node, double angle, Speed speed, Easing easing) {
        stopExistingAnimation(node);
        
        Timeline timeline = new Timeline(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.rotateProperty(), angle, easing.getInterpolator())
            )
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Animation de transition de couleur
     */
    public Timeline colorTransition(Node node, Color fromColor, Color toColor, Speed speed) {
        stopExistingAnimation(node);
        
        Timeline timeline = new Timeline();
        
        // Cette animation dépend du type de node et doit être adaptée selon le contexte
        // Pour l'instant, on fait une animation générique sur l'opacité
        timeline.getKeyFrames().add(
            new KeyFrame(speed.getDuration(),
                new KeyValue(node.opacityProperty(), 1.0, Easing.EASE_BOTH.getInterpolator())
            )
        );
        
        runningAnimations.put(node, timeline);
        timeline.setOnFinished(e -> runningAnimations.remove(node));
        timeline.play();
        
        return timeline;
    }
    
    /**
     * Arrête l'animation en cours sur un nœud
     */
    public void stopAnimation(Node node) {
        stopExistingAnimation(node);
    }
    
    /**
     * Arrête toutes les animations en cours
     */
    public void stopAllAnimations() {
        runningAnimations.values().forEach(Timeline::stop);
        runningAnimations.clear();
    }
    
    /**
     * Vérifie si un nœud a une animation en cours
     */
    public boolean isAnimating(Node node) {
        return runningAnimations.containsKey(node);
    }
    
    /**
     * Arrête une animation existante si elle existe
     */
    private void stopExistingAnimation(Node node) {
        Timeline existing = runningAnimations.get(node);
        if (existing != null) {
            existing.stop();
            runningAnimations.remove(node);
        }
    }
    
    /**
     * Crée un interpolateur de type ressort
     */
    private static Interpolator createSpringInterpolator() {
        return new Interpolator() {
            @Override
            protected double curve(double t) {
                return t * t * ((1.70158 + 1) * t - 1.70158);
            }
        };
    }
    
    /**
     * Crée un interpolateur de type rebond
     */
    private static Interpolator createBounceInterpolator() {
        return new Interpolator() {
            @Override
            protected double curve(double t) {
                if (t < (1/2.75)) {
                    return 7.5625 * t * t;
                } else if (t < (2/2.75)) {
                    return 7.5625 * (t -= (1.5/2.75)) * t + 0.75;
                } else if (t < (2.5/2.75)) {
                    return 7.5625 * (t -= (2.25/2.75)) * t + 0.9375;
                } else {
                    return 7.5625 * (t -= (2.625/2.75)) * t + 0.984375;
                }
            }
        };
    }
    
    /**
     * Méthodes de commodité pour les animations les plus courantes
     */
    public Timeline fadeIn(Node node) {
        return fadeIn(node, Speed.NORMAL, Easing.EASE_OUT);
    }
    
    public Timeline fadeOut(Node node) {
        return fadeOut(node, Speed.NORMAL, Easing.EASE_IN);
    }
    
    public Timeline slideIn(Node node) {
        return slideInFromBottom(node, Speed.NORMAL, Easing.EASE_OUT);
    }
    
    public Timeline slideOut(Node node) {
        return slideOutToTop(node, Speed.NORMAL, Easing.EASE_IN);
    }
    
    public Timeline quickPulse(Node node) {
        return pulse(node, Speed.FAST);
    }
    
    public Timeline hoverElevate(Node node) {
        return elevate(node, Speed.FAST);
    }
    
    public Timeline hoverLower(Node node) {
        return lower(node, Speed.FAST);
    }
}