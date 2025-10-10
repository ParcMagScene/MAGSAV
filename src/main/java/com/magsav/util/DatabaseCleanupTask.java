package com.magsav.util;

import com.magsav.service.DataChangeNotificationService;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

/**
 * Tâche pour nettoyer les doublons de la base de données en arrière-plan
 */
public class DatabaseCleanupTask extends Task<DatabaseCleanupService.CleanupResult> {
    
    private final Runnable onSuccessCallback;
    private final Runnable onFailureCallback;
    
    public DatabaseCleanupTask() {
        this(null, null);
    }
    
    public DatabaseCleanupTask(Runnable onSuccess, Runnable onFailure) {
        this.onSuccessCallback = onSuccess;
        this.onFailureCallback = onFailure;
        
        // Ajouter les callbacks aux événements
        this.setOnSucceeded(createSuccessHandler());
        this.setOnFailed(createFailureHandler());
    }
    
    @Override
    protected DatabaseCleanupService.CleanupResult call() throws Exception {
        updateTitle("Nettoyage de la base de données");
        updateMessage("Recherche des doublons...");
        updateProgress(0, 4);
        
        DatabaseCleanupService.CleanupResult result = DatabaseCleanupService.cleanupAllDuplicates();
        
        updateProgress(4, 4);
        updateMessage("Nettoyage terminé");
        
        return result;
    }
    
    private EventHandler<WorkerStateEvent> createSuccessHandler() {
        return event -> {
            DatabaseCleanupService.CleanupResult result = getValue();
            
            // Notifier le changement global des données
            DataChangeNotificationService.getInstance().notifyDatabaseCleaned(result.getTotalCleaned());
            
            AppLogger.info("Nettoyage de la base de données terminé: " + result);
            
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
        };
    }
    
    private EventHandler<WorkerStateEvent> createFailureHandler() {
        return event -> {
            Throwable exception = getException();
            AppLogger.error("Erreur lors du nettoyage de la base de données", exception);
            
            if (onFailureCallback != null) {
                onFailureCallback.run();
            }
        };
    }
}