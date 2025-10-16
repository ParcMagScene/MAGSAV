package com.magsav.gui.components;

import com.magsav.gui.components.DetailPaneFactory.*;
import com.magsav.dto.UserRow;
import com.magsav.dto.ClientRow;
import com.magsav.dto.CompanyRow;
import com.magsav.repo.ProductRepository;
import com.magsav.model.Vehicule;
import com.magsav.util.AppLogger;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;

/**
 * Utilitaire pour créer des layouts avec panneau de visualisation/détail intégré
 * Facilite l'intégration du système unifié dans les contrôleurs existants
 */
public class DetailLayoutHelper {
    
    /**
     * Crée un layout avec table à gauche et panneau de détail à droite
     */
    public static BorderPane createTableWithDetailLayout() {
        BorderPane layout = new BorderPane();
        layout.getStyleClass().add("table-detail-layout");
        return layout;
    }
    
    /**
     * Crée un panneau de visualisation pour les demandes
     */
    public static DetailPane createDemandeVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails de la demande", "DEMANDE")
                .type(PaneType.VISUALIZATION)
                .showImage(false)
                .showQrCode(false)
                .onOpen(onOpenDetail);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de visualisation pour les produits
     */
    public static DetailPane createProductVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails du produit", "PRODUIT")
                .type(PaneType.VISUALIZATION)
                .showImage(true)
                .showQrCode(true)
                .onOpen(onOpenDetail)
                .imageSize(100, 100);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de visualisation pour les utilisateurs
     */
    public static DetailPane createUserVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails de l'utilisateur", "USER")
                .type(PaneType.VISUALIZATION)
                .showImage(true)
                .showQrCode(false)
                .onOpen(onOpenDetail)
                .imageSize(80, 80);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de visualisation pour les véhicules
     */
    public static DetailPane createVehiculeVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails du véhicule", "VEHICULE")
                .type(PaneType.VISUALIZATION)
                .showImage(true)
                .showQrCode(true)
                .onOpen(onOpenDetail)
                .imageSize(120, 90);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de visualisation pour les interventions
     */
    public static DetailPane createInterventionVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails de l'intervention", "INTERVENTION")
                .type(PaneType.VISUALIZATION)
                .showImage(false)
                .showQrCode(false)
                .onOpen(onOpenDetail);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de détail complet pour l'édition d'un produit
     */
    public static DetailPane createProductDetailPane(Runnable onEdit, Runnable onDelete) {
        PaneConfig config = new PaneConfig("Fiche produit", "PRODUIT")
                .type(PaneType.DETAIL)
                .showImage(true)
                .showQrCode(true)
                .onEdit(onEdit)
                .onDelete(onDelete)
                .imageSize(140, 140);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de détail complet pour l'édition d'un utilisateur
     */
    public static DetailPane createUserDetailPane(Runnable onEdit, Runnable onDelete) {
        PaneConfig config = new PaneConfig("Fiche utilisateur", "USER")
                .type(PaneType.DETAIL)
                .showImage(true)
                .showQrCode(false)
                .onEdit(onEdit)
                .onDelete(onDelete)
                .imageSize(100, 100);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Crée un panneau de détail complet pour l'édition d'une affaire
     */
    public static DetailPane createAffaireDetailPane(Runnable onEdit, Runnable onDelete) {
        PaneConfig config = new PaneConfig("Détails de l'affaire", "AFFAIRE")
                .type(PaneType.DETAIL)
                .showImage(false)
                .showQrCode(false)
                .onEdit(onEdit)
                .onDelete(onDelete);
        
        return DetailPaneFactory.createPane(config);
    }

    /**
     * Crée un panneau de visualisation pour les affaires
     */
    public static DetailPane createAffaireVisualizationPane(Runnable onEdit) {
        PaneConfig config = new PaneConfig("Détails de l'affaire", "AFFAIRE")
                .type(PaneType.VISUALIZATION)
                .showImage(false)
                .showQrCode(false)
                .onEdit(onEdit);
        
        return DetailPaneFactory.createPane(config);
    }

    /**
     * Crée un panneau de détail complet pour l'édition d'un véhicule
     */
    public static DetailPane createVehiculeDetailPane(Runnable onEdit, Runnable onDelete) {
        PaneConfig config = new PaneConfig("Fiche véhicule", "VEHICULE")
                .type(PaneType.DETAIL)
                .showImage(true)
                .showQrCode(true)
                .onEdit(onEdit)
                .onDelete(onDelete)
                .imageSize(160, 120);
        
        return DetailPaneFactory.createPane(config);
    }
    
    /**
     * Enroule un panneau de détail dans un ScrollPane pour les contenus longs
     */
    public static ScrollPane wrapInScrollPane(DetailPane detailPane) {
        ScrollPane scrollPane = new ScrollPane(detailPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("detail-scroll-pane");
        return scrollPane;
    }
    
    /**
     * Crée un container pour plusieurs panneaux de visualisation (layout en grille)
     */
    public static VBox createMultiPaneContainer() {
        VBox container = new VBox(16);
        container.setPadding(new Insets(16));
        container.getStyleClass().add("multi-pane-container");
        return container;
    }
    
    /**
     * Adapte un EntityInfo depuis les données d'une demande
     */
    public static EntityInfo createEntityInfoFromRequest(com.magsav.dto.RequestRow request) {
        return new EntityInfo("Demande #" + request.id())
                .reference("REQ-" + request.id())
                .category(request.type())
                .status(request.status())
                .description(request.description());
    }
    
    /**
     * Adapte un EntityInfo depuis les données d'un produit
     */
    public static EntityInfo createEntityInfoFromProduct(com.magsav.repo.ProductRepository.ProductRow product) {
        if (product == null) {
            return createEmptyEntityInfo();
        }
        
        String description = String.format(
            "UID: %s\nNuméro de série: %s\nSituation: %s",
            product.uid() != null ? product.uid() : "N/A",
            product.sn() != null ? product.sn() : "N/A",
            product.situation() != null ? product.situation() : "Non définie"
        );
        
        return new EntityInfo(product.nom() != null ? product.nom() : "Produit sans nom")
                .reference(product.sn() != null ? product.sn() : "N/A")
                .category("Produit")
                .status(product.situation() != null ? product.situation() : "Non définie")
                .description(description);
    }
    
    /**
     * Convertit un UserRow en EntityInfo pour les détails
     */
    public static EntityInfo createEntityInfoFromUser(UserRow user) {
        return new EntityInfo(user.getNom() + " " + user.getPrenom())
                .reference("USER-" + user.getId())
                .category(user.getRole())
                .status(user.getStatut())
                .description("Email: " + user.getEmail());
    }

    /**
     * Crée un panneau de visualisation pour les clients
     */
    public static DetailPane createClientVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails du client", "CLIENT")
                .type(PaneType.VISUALIZATION)
                .showImage(true)
                .showQrCode(false)
                .onOpen(onOpenDetail)
                .imageSize(80, 80);
        
        return DetailPaneFactory.createPane(config);
    }

    /**
     * Convertit un ClientRow en EntityInfo pour les détails
     */
    public static EntityInfo createEntityInfoFromClient(ClientRow client) {
        return new EntityInfo(client.getNom())
                .reference("CLIENT-" + client.getId())
                .category(client.getType())
                .status("Actif")
                .description("Email: " + (client.getEmail() != null ? client.getEmail() : "N/A") + 
                            " | Tél: " + (client.getTelephone() != null ? client.getTelephone() : "N/A") +
                            " | Ville: " + (client.getVille() != null ? client.getVille() : "N/A"));
    }

    /**
     * Crée un panneau de visualisation pour les sociétés
     */
    public static DetailPane createCompanyVisualizationPane(Runnable onOpenDetail) {
        PaneConfig config = new PaneConfig("Détails de la société", "COMPANY")
                .type(PaneType.VISUALIZATION)
                .showImage(true)
                .showQrCode(false)
                .onOpen(onOpenDetail)
                .imageSize(80, 80);
        
        return DetailPaneFactory.createPane(config);
    }

    /**
     * Convertit un CompanyRow en EntityInfo pour les détails
     */
    public static EntityInfo createEntityInfoFromCompany(com.magsav.dto.CompanyRow company) {
        return new EntityInfo(company.getNom())
                .reference("COMPANY-" + company.getId())
                .category(company.getType())
                .status("Secteur: " + (company.getSecteur() != null ? company.getSecteur() : "N/A"))
                .description("Email: " + (company.getEmail() != null ? company.getEmail() : "N/A") + 
                            " | Tél: " + (company.getTelephone() != null ? company.getTelephone() : "N/A") +
                            " | Ville: " + (company.getVille() != null ? company.getVille() : "N/A") +
                            " | Site: " + (company.getSiteweb() != null ? company.getSiteweb() : "N/A"));
    }

    /**
     * Convertit un Vehicule en EntityInfo pour les détails
     */
    public static EntityInfo createEntityInfoFromVehicule(Vehicule vehicule) {
        return new EntityInfo(vehicule.getDisplayName())
                .reference(vehicule.getImmatriculation())
                .category(vehicule.getTypeVehicule().getDisplayName())
                .status(vehicule.getStatut().getDisplayName())
                .description("Marque: " + vehicule.getMarque() + 
                            " | Modèle: " + vehicule.getModele() +
                            " | Année: " + vehicule.getAnnee() +
                            " | Km: " + vehicule.getKilometrage() + " km");
    }

    /**
     * Convertit une Affaire en EntityInfo pour les détails
     */
    public static EntityInfo createEntityInfoFromAffaire(Object affaire) {
        if (affaire == null) {
            return createEmptyEntityInfo();
        }
        
        try {
            Class<?> clazz = affaire.getClass();
            
            String reference = getFieldValue(clazz, affaire, "reference", "N/A");
            String nom = getFieldValue(clazz, affaire, "nom", "N/A");
            String client = getFieldValue(clazz, affaire, "clientNom", "N/A");
            String statut = getFieldValue(clazz, affaire, "statut", "N/A");
            String montant = getFieldValue(clazz, affaire, "montantEstime", "0");
            String dateCreation = getFieldValue(clazz, affaire, "dateCreation", "N/A");
            String description = getFieldValue(clazz, affaire, "description", "");
            
            String detailsDescription = String.format(
                "Client: %s\nMontant estimé: %s€\nDate création: %s%s",
                client, montant, dateCreation,
                (!description.isEmpty() ? "\n\nDescription: " + description : "")
            );
            
            return new EntityInfo(nom)
                    .reference(reference)
                    .category("Affaire")
                    .status(statut)
                    .description(detailsDescription);
            
        } catch (Exception e) {
            return new EntityInfo("Erreur")
                    .reference("-")
                    .category("Affaire")
                    .status("Erreur")
                    .description("Impossible de charger les détails de l'affaire");
        }
    }
    
    /**
     * Adapte un EntityInfo depuis les données d'une intervention
     */
    public static EntityInfo createEntityInfoFromIntervention(Object intervention) {
        AppLogger.info("🔍 DEBUG DetailLayoutHelper - createEntityInfoFromIntervention appelée avec: " + intervention);
        
        if (intervention == null) {
            AppLogger.info("⚠️ DEBUG DetailLayoutHelper - Intervention null, retour d'EntityInfo vide");
            return createEmptyEntityInfo();
        }
        
        // Utiliser la réflexion pour extraire les données de l'intervention
        try {
            Class<?> clazz = intervention.getClass();
            AppLogger.info("🔍 DEBUG DetailLayoutHelper - Classe de l'intervention: " + clazz.getName());
            
            String id = getFieldValue(clazz, intervention, "id", "N/A");
            String produit = getFieldValue(clazz, intervention, "produitNom", "N/A");
            String statut = getFieldValue(clazz, intervention, "statut", "N/A");
            String panne = getFieldValue(clazz, intervention, "panne", "N/A");
            String dateEntree = getFieldValue(clazz, intervention, "dateEntree", "N/A");
            String dateSortie = getFieldValue(clazz, intervention, "dateSortie", "En cours");
            
            AppLogger.info("🔍 DEBUG DetailLayoutHelper - Données extraites - ID: " + id + ", Produit: " + produit + ", Statut: " + statut);
            
            String description = String.format(
                "Produit: %s\nPanne: %s\nDate d'entrée: %s\nDate de sortie: %s",
                produit, panne, dateEntree, 
                (dateSortie != null && !dateSortie.trim().isEmpty()) ? dateSortie : "En cours"
            );
            
            EntityInfo entityInfo = new EntityInfo("Intervention #" + id)
                    .reference("INT-" + id)
                    .category("Intervention")
                    .status(statut)
                    .description(description);
            
            AppLogger.info("✅ DEBUG DetailLayoutHelper - EntityInfo créée avec succès");
            return entityInfo;
            
        } catch (Exception e) {
            AppLogger.info("❌ DEBUG DetailLayoutHelper - Erreur lors de la création de l'EntityInfo: " + e.getMessage());
            e.printStackTrace();
            return new EntityInfo("Erreur")
                    .reference("-")
                    .category("Intervention")
                    .status("Erreur")
                    .description("Impossible de charger les détails de l'intervention");
        }
    }
    
    /**
     * Méthode helper pour extraire une valeur de champ par réflexion
     */
    private static String getFieldValue(Class<?> clazz, Object instance, String fieldName, String defaultValue) {
        try {
            // Essayer d'abord comme méthode record (si c'est un record)
            try {
                java.lang.reflect.Method method = clazz.getMethod(fieldName);
                Object value = method.invoke(instance);
                return value != null ? value.toString() : defaultValue;
            } catch (NoSuchMethodException e) {
                // Pas un record ou méthode non trouvée, essayer comme champ
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(instance);
                return value != null ? value.toString() : defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Crée un EntityInfo vide (placeholder)
     */
    public static EntityInfo createEmptyEntityInfo() {
        return new EntityInfo("Aucune sélection")
                .reference("-")
                .category("-")
                .status("-")
                .description("Sélectionnez un élément pour voir ses détails");
    }
}