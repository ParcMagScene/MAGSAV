package com.magscene.magsav.desktop.view.planning;

import com.magscene.magsav.desktop.service.planning.PlanningConflictService;
import com.magscene.magsav.desktop.service.planning.PlanningConflictService.ConflictInfo;
import com.magscene.magsav.desktop.service.planning.PlanningConflictService.ConflictResolution;
import com.magscene.magsav.desktop.service.planning.PlanningConflictService.ConflictSeverity;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;
import com.magscene.magsav.desktop.theme.UnifiedThemeManager;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panneau de notification des conflits de planning
 */
public class ConflictNotificationPanel extends VBox {
    
    private final PlanningConflictService conflictService;
    private ListView<ConflictInfo> conflictListView;
    private Label statusLabel;
    private Button refreshButton;
    private Button resolveAllButton;
    
    public ConflictNotificationPanel(PlanningConflictService conflictService) {
        this.conflictService = conflictService;
        
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: " + UnifiedThemeManager.getInstance().getCurrentUIColor() + "; -fx-border-color: #e74c3c; -fx-border-width: 2px; " +
                 "-fx-border-radius: 5px; -fx-background-radius: 5px;");
        
        initializeComponents();
        createLayout();
        setupEventHandlers();
        refreshConflicts();
    }
    
    private void initializeComponents() {
        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e74c3c;");
        
        conflictListView = new ListView<>();
        conflictListView.setPrefHeight(200);
        conflictListView.setCellFactory(createConflictCellFactory());
        
        refreshButton = new Button("🔄 Actualiser");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        
        resolveAllButton = new Button("⚡ Résoudre Tout");
        // $varName supprimÃ© - Style gÃ©rÃ© par CSS
        resolveAllButton.setDisable(true);
    }
    
    private void createLayout() {
        // En-tête avec statut
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("⚠️ Conflits de Planning");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        HBox buttonBox = new HBox(5);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(refreshButton, resolveAllButton);
        
        header.getChildren().addAll(titleLabel, spacer, buttonBox);
        
        // Corps avec liste des conflits
        getChildren().addAll(header, statusLabel, conflictListView);
    }
    
    private void setupEventHandlers() {
        refreshButton.setOnAction(e -> refreshConflicts());
        
        resolveAllButton.setOnAction(e -> {
            // TODO: Implémenter la résolution automatique
            showResolveAllDialog();
        });
        
        // Double-clic pour voir les détails d'un conflit
        conflictListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                ConflictInfo selectedConflict = conflictListView.getSelectionModel().getSelectedItem();
                if (selectedConflict != null) {
                    showConflictDetails(selectedConflict);
                }
            }
        });
    }
    
    private Callback<ListView<ConflictInfo>, ListCell<ConflictInfo>> createConflictCellFactory() {
        return listView -> new ListCell<ConflictInfo>() {
            @Override
            protected void updateItem(ConflictInfo conflict, boolean empty) {
                super.updateItem(conflict, empty);
                
                if (empty || conflict == null) {
                    setGraphic(null);
                } else {
                    setGraphic(createConflictCard(conflict));
                }
            }
        };
    }
    
    private VBox createConflictCard(ConflictInfo conflict) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle(getCardStyle(conflict.getSeverity()));
        
        // En-tête avec sévérité
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label severityIcon = new Label(getSeverityIcon(conflict.getSeverity()));
        severityIcon.setStyle("-fx-font-size: 16px;");
        
        Label severityLabel = new Label(getSeverityText(conflict.getSeverity()));
        severityLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + getSeverityColor(conflict.getSeverity()) + ";");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Label durationLabel = new Label(formatDuration(conflict.getOverlapMinutes()));
        durationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        
        headerBox.getChildren().addAll(severityIcon, severityLabel, headerSpacer, durationLabel);
        
        // Titre des événements
        Label titleLabel = new Label(conflict.getDescription());
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        
        // Détails temporels
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm");
        String timeInfo = String.format("Événement 1: %s - %s | Événement 2: %s - %s",
            conflict.getEvent1().getStartDateTime().format(timeFormatter),
            conflict.getEvent1().getEndDateTime().format(timeFormatter),
            conflict.getEvent2().getStartDateTime().format(timeFormatter),
            conflict.getEvent2().getEndDateTime().format(timeFormatter)
        );
        
        Label timeLabel = new Label(timeInfo);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
        
        // Personnel en conflit
        if (!conflict.getConflictingPersonnel().isEmpty()) {
            Label personnelLabel = new Label("Personnel concerné: " + 
                String.join(", ", conflict.getConflictingPersonnel()));
            personnelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            card.getChildren().add(personnelLabel);
        }
        
        // Boutons d'action
        HBox actionBox = new HBox(5);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(5, 0, 0, 0));
        
        Button detailsButton = new Button("Détails");
        detailsButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 8 3 8;");
        detailsButton.setOnAction(e -> showConflictDetails(conflict));
        
        Button resolveButton = new Button("Résoudre");
        resolveButton.setStyle("-fx-font-size: 10px; -fx-padding: 3 8 3 8; -fx-background-color: #27ae60; -fx-text-fill: white;");
        resolveButton.setOnAction(e -> showResolutionOptions(conflict));
        
        actionBox.getChildren().addAll(detailsButton, resolveButton);
        
        card.getChildren().addAll(headerBox, titleLabel, timeLabel, actionBox);
        
        return card;
    }
    
    private String getCardStyle(ConflictSeverity severity) {
        String baseStyle = "-fx-background-radius: 8px; -fx-border-radius: 8px; -fx-border-width: 1px; ";
        
        return baseStyle + switch (severity) {
            case CRITICAL -> "-fx-background-color: #fdf2f2; -fx-border-color: #e74c3c;";
            case HIGH -> "-fx-background-color: #fef9e7; -fx-border-color: #f39c12;";
            case MEDIUM -> "-fx-background-color: #eaf4fd; -fx-border-color: #3498db;";
            case LOW -> "-fx-background-color: #eafaf1; -fx-border-color: #27ae60;";
        };
    }
    
    private String getSeverityIcon(ConflictSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "🚨";
            case HIGH -> "⚠️";
            case MEDIUM -> "🔶";
            case LOW -> "ℹ️";
        };
    }
    
    private String getSeverityText(ConflictSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "Critique";
            case HIGH -> "Élevé";
            case MEDIUM -> "Moyen";
            case LOW -> "Faible";
        };
    }
    
    private String getSeverityColor(ConflictSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "#c0392b";
            case HIGH -> "#d68910";
            case MEDIUM -> "#2874a6";
            case LOW -> "#196f3d";
        };
    }
    
    private String formatDuration(long minutes) {
        if (minutes < 60) {
            return minutes + " min de conflit";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + "h" + (remainingMinutes > 0 ? remainingMinutes + "m" : "") + " de conflit";
        }
    }
    
    public void refreshConflicts() {
        List<ConflictInfo> conflicts = conflictService.detectAllConflicts();
        conflictListView.getItems().setAll(conflicts);
        
        updateStatus(conflicts.size());
        resolveAllButton.setDisable(conflicts.isEmpty());
    }
    
    private void updateStatus(int conflictCount) {
        if (conflictCount == 0) {
            statusLabel.setText("✅ Aucun conflit détecté");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60;");
            setVisible(false); // Masquer le panneau s'il n'y a pas de conflits
        } else {
            statusLabel.setText(conflictCount + " conflit(s) détecté(s) nécessitant une attention");
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c;");
            setVisible(true);
        }
    }
    
    private void showConflictDetails(ConflictInfo conflict) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Détails du Conflit");
        alert.setHeaderText("Conflit de Planning Détecté");
        alert.setContentText(conflict.getDescription() + "\n\n" +
            "Chevauchement: " + formatDuration(conflict.getOverlapMinutes()) + "\n" +
            "Sévérité: " + getSeverityText(conflict.getSeverity()));
        
        alert.showAndWait();
    }
    
    private void showResolutionOptions(ConflictInfo conflict) {
        List<ConflictResolution> resolutions = conflictService.proposeResolutions(conflict);
        
        if (resolutions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résolution de Conflit");
            alert.setContentText("Aucune solution automatique disponible pour ce conflit.");
            alert.showAndWait();
            return;
        }
        
        ChoiceDialog<ConflictResolution> dialog = new ChoiceDialog<>(resolutions.get(0), resolutions);
        dialog.setTitle("Résolution de Conflit");
        dialog.setHeaderText("Choisissez une solution pour résoudre le conflit");
        dialog.setContentText("Solutions disponibles:");
        
        // Les résolutions sont déjà affichées correctement par défaut
        
        dialog.showAndWait().ifPresent(resolution -> {
            // TODO: Implémenter la résolution sélectionnée
            Alert confirmation = new Alert(Alert.AlertType.INFORMATION);
            confirmation.setTitle("Solution Appliquée");
            confirmation.setContentText("Solution sélectionnée: " + resolution.getTitle() + 
                "\n\n" + resolution.getDescription());
            confirmation.showAndWait();
            
            refreshConflicts();
        });
    }
    
    private void showResolveAllDialog() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Résolution Automatique");
        confirmation.setHeaderText("Résoudre tous les conflits automatiquement?");
        confirmation.setContentText("Cette action appliquera les solutions automatiques pour tous les conflits détectés.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Implémenter la résolution automatique
                refreshConflicts();
            }
        });
    }
}
