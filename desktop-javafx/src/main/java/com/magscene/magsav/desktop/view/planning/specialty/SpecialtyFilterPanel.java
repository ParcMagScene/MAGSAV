package com.magscene.magsav.desktop.view.planning.specialty;

import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService;
import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService.PersonnelAssignment;
import com.magscene.magsav.desktop.service.planning.SpecialtyPlanningService.SpecialtyFilter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.function.BiConsumer;

/**
 * Panneau de sélection et filtrage des spécialités pour le planning
 * Remplace le CalendarSelectionPanel basique par une version orientée spécialités
 */
public class SpecialtyFilterPanel extends VBox {
    
    private final SpecialtyPlanningService specialtyService;
    private VBox filtersContainer;
    private Label statsLabel;
    private BiConsumer<String, Boolean> onSpecialtyToggled;
    
    // Contrôles de filtre global
    private CheckBox showAllCheckBox;
    private CheckBox showOnlyExpertsCheckBox;
    private ComboBox<String> sortOrderCombo;
    
    public SpecialtyFilterPanel(SpecialtyPlanningService specialtyService) {
        this.specialtyService = specialtyService;
        
        getStyleClass().add("specialty-filter-panel");
        setSpacing(15);
        setPadding(new Insets(15));
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadFilters();
    }
    
    private void initializeComponents() {
        // Container pour les filtres de spécialités
        filtersContainer = new VBox(8);
        filtersContainer.getStyleClass().add("filters-container");
        
        // Label de statistiques
        statsLabel = new Label();
        statsLabel.getStyleClass().add("stats-label");
        
        // Contrôles globaux
        showAllCheckBox = new CheckBox("Tout afficher");
        showAllCheckBox.setSelected(true);
        showAllCheckBox.getStyleClass().add("global-filter-checkbox");
        
        showOnlyExpertsCheckBox = new CheckBox("Experts seulement");
        showOnlyExpertsCheckBox.getStyleClass().add("global-filter-checkbox");
        
        sortOrderCombo = new ComboBox<>();
        sortOrderCombo.getItems().addAll(
            "Alphabétique",
            "Par nombre de personnel",
            "Par expertise moyenne"
        );
        sortOrderCombo.setValue("Alphabétique");
        sortOrderCombo.getStyleClass().add("sort-combo");
    }
    
    private void setupLayout() {
        // En-tête avec titre et contrôles globaux
        Label titleLabel = new Label("🎯 Spécialités");
        titleLabel.getStyleClass().addAll("section-title", "specialty-title");
        
        VBox globalControls = new VBox(5);
        globalControls.getChildren().addAll(
            showAllCheckBox,
            showOnlyExpertsCheckBox,
            new Label("Tri:"),
            sortOrderCombo
        );
        
        // Séparateur
        Separator separator1 = new Separator();
        
        // ScrollPane pour la liste des filtres
        ScrollPane filtersScrollPane = new ScrollPane(filtersContainer);
        filtersScrollPane.setFitToWidth(true);
        filtersScrollPane.getStyleClass().add("filters-scroll-pane");
        filtersScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        filtersScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        VBox.setVgrow(filtersScrollPane, Priority.ALWAYS);
        
        // Séparateur et statistiques
        Separator separator2 = new Separator();
        
        this.getChildren().addAll(
            titleLabel,
            globalControls,
            separator1,
            filtersScrollPane,
            separator2,
            statsLabel
        );
    }
    
    private void setupEventHandlers() {
        showAllCheckBox.setOnAction(e -> toggleAllSpecialties(showAllCheckBox.isSelected()));
        
        showOnlyExpertsCheckBox.setOnAction(e -> updateExpertFilter(showOnlyExpertsCheckBox.isSelected()));
        
        sortOrderCombo.setOnAction(e -> refreshFiltersOrder());
    }
    
    private void loadFilters() {
        filtersContainer.getChildren().clear();
        
        // Créer les filtres pour chaque spécialité
        for (SpecialtyFilter filter : specialtyService.getSpecialtyFilters()) {
            SpecialtyCheckBox checkBox = new SpecialtyCheckBox(filter);
            filtersContainer.getChildren().add(checkBox);
        }
        
        updateStatistics();
    }
    
    private void refreshFiltersOrder() {
        String sortOrder = sortOrderCombo.getValue();
        
        // Récupérer les filtres actuels et les trier
        var filters = specialtyService.getSpecialtyFilters();
        
        switch (sortOrder) {
            case "Par nombre de personnel" -> {
                var stats = specialtyService.getSpecialtyStatistics();
                filters.sort((f1, f2) -> Integer.compare(
                    stats.getOrDefault(f2.getSpecialty(), 0),
                    stats.getOrDefault(f1.getSpecialty(), 0)
                ));
            }
            case "Par expertise moyenne" -> {
                // TODO: Implémenter le tri par expertise moyenne
                filters.sort((f1, f2) -> f1.getSpecialty().compareTo(f2.getSpecialty()));
            }
            default -> {
                filters.sort((f1, f2) -> f1.getSpecialty().compareTo(f2.getSpecialty()));
            }
        }
        
        loadFilters();
    }
    
    private void toggleAllSpecialties(boolean showAll) {
        for (SpecialtyFilter filter : specialtyService.getSpecialtyFilters()) {
            filter.setActive(showAll);
            specialtyService.updateSpecialtyFilter(filter.getSpecialty(), showAll);
        }
        
        // Mettre à jour les CheckBox visuellement
        filtersContainer.getChildren().forEach(node -> {
            if (node instanceof SpecialtyCheckBox checkBox) {
                checkBox.updateFromFilter();
            }
        });
        
        if (onSpecialtyToggled != null) {
            onSpecialtyToggled.accept("ALL", showAll);
        }
    }
    
    private void updateExpertFilter(boolean showOnlyExperts) {
        for (SpecialtyFilter filter : specialtyService.getSpecialtyFilters()) {
            filter.setShowOnlyExperts(showOnlyExperts);
        }
        
        if (onSpecialtyToggled != null) {
            onSpecialtyToggled.accept("EXPERTS_ONLY", showOnlyExperts);
        }
    }
    
    private void updateStatistics() {
        var stats = specialtyService.getSpecialtyStatistics();
        int totalPersonnel = stats.values().stream().mapToInt(Integer::intValue).sum();
        int activeSpecialties = (int) specialtyService.getSpecialtyFilters().stream()
            .filter(SpecialtyFilter::isActive)
            .count();
        
        statsLabel.setText(String.format("📊 %d spécialités • %d personnel total\n🔍 %d spécialités actives",
            stats.size(), totalPersonnel, activeSpecialties));
    }
    
    /**
     * CheckBox personnalisée pour les spécialités avec indicateur de couleur
     */
    private class SpecialtyCheckBox extends HBox {
        private final SpecialtyFilter filter;
        private final CheckBox checkBox;
        private final Circle colorIndicator;
        private final Label personnelCountLabel;
        
        public SpecialtyCheckBox(SpecialtyFilter filter) {
            this.filter = filter;
            
            setSpacing(8);
            setAlignment(Pos.CENTER_LEFT);
            getStyleClass().add("specialty-checkbox");
            
            // Indicateur de couleur
            colorIndicator = new Circle(6);
            colorIndicator.setFill(Color.web(filter.getColor()));
            colorIndicator.getStyleClass().add("specialty-color-indicator");
            
            // CheckBox principale
            checkBox = new CheckBox(filter.getSpecialty());
            checkBox.setSelected(filter.isActive());
            checkBox.getStyleClass().add("specialty-check");
            
            // Nombre de personnel
            int personnelCount = specialtyService.getPersonnelForSpecialty(filter.getSpecialty()).size();
            personnelCountLabel = new Label("(" + personnelCount + ")");
            personnelCountLabel.getStyleClass().add("personnel-count-label");
            
            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Bouton détails (optionnel)
            Button detailsButton = new Button("👥");
            detailsButton.getStyleClass().addAll("details-button", "icon-button");
            detailsButton.setTooltip(new Tooltip("Voir le personnel"));
            detailsButton.setOnAction(e -> showPersonnelDetails());
            
            this.getChildren().addAll(
                colorIndicator,
                checkBox,
                spacer,
                personnelCountLabel,
                detailsButton
            );
            
            // Gestionnaire d'événement
            checkBox.setOnAction(e -> handleToggle());
            
            // Effet hover
            this.setOnMouseEntered(e -> this.getStyleClass().add("specialty-checkbox-hover"));
            this.setOnMouseExited(e -> this.getStyleClass().remove("specialty-checkbox-hover"));
        }
        
        private void handleToggle() {
            boolean selected = checkBox.isSelected();
            filter.setActive(selected);
            specialtyService.updateSpecialtyFilter(filter.getSpecialty(), selected);
            
            // Mettre à jour la CheckBox "Tout afficher"
            boolean allSelected = specialtyService.getSpecialtyFilters().stream()
                .allMatch(SpecialtyFilter::isActive);
            showAllCheckBox.setSelected(allSelected);
            
            updateStatistics();
            
            if (onSpecialtyToggled != null) {
                onSpecialtyToggled.accept(filter.getSpecialty(), selected);
            }
        }
        
        private void showPersonnelDetails() {
            // Créer une popup avec les détails du personnel
            var personnel = specialtyService.getPersonnelForSpecialty(filter.getSpecialty());
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Personnel - " + filter.getSpecialty());
            alert.setHeaderText("Personnel affecté à la spécialité " + filter.getSpecialty());
            
            StringBuilder content = new StringBuilder();
            if (personnel.isEmpty()) {
                content.append("Aucun personnel affecté à cette spécialité.");
            } else {
                for (PersonnelAssignment assignment : personnel) {
                    content.append("• ").append(assignment.getPersonnelName())
                           .append(" (").append(assignment.getPersonnelType()).append(")")
                           .append(" - Niveau: ").append(assignment.getProficiencyLabel());
                }
            }
            
            alert.setContentText(content.toString());
            alert.showAndWait();
        }
        
        public void updateFromFilter() {
            checkBox.setSelected(filter.isActive());
        }
    }
    
    /**
     * Définit le callback appelé quand une spécialité est activée/désactivée
     */
    public void setOnSpecialtyToggled(BiConsumer<String, Boolean> callback) {
        this.onSpecialtyToggled = callback;
    }
    
    /**
     * Retourne les spécialités actuellement actives
     */
    public java.util.List<String> getActiveSpecialties() {
        return specialtyService.getActiveSpecialties();
    }
    
    /**
     * Rafraîchit le panneau avec les données actuelles
     */
    public void refresh() {
        loadFilters();
    }
}
