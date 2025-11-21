package com.magscene.magsav.desktop.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Utilitaires pour l'interface utilisateur JavaFX
 */
public class UIUtils {
    
    /**
     * Exécute une tâche après un délai spécifique sur le thread JavaFX
     * Remplace l'usage de Thread.sleep() qui est une mauvaise pratique
     * 
     * @param delayMillis délai en millisecondes
     * @param task la tâche à exécuter
     */
    public static void runAfterDelay(int delayMillis, Runnable task) {
        PauseTransition pause = new PauseTransition(Duration.millis(delayMillis));
        pause.setOnFinished(event -> task.run());
        pause.play();
    }
    
    /**
     * Exécute une tâche sur le thread JavaFX après un délai
     * Équivalent à Platform.runLater() mais avec délai
     * 
     * @param delayMillis délai en millisecondes
     * @param task la tâche à exécuter sur le thread JavaFX
     */
    public static void runLaterAfterDelay(int delayMillis, Runnable task) {
        runAfterDelay(delayMillis, () -> Platform.runLater(task));
    }
    
    /**
     * Vérifie si on est sur le thread JavaFX et exécute immédiatement ou via runLater
     * 
     * @param task la tâche à exécuter
     */
    public static void runOnFXThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
}