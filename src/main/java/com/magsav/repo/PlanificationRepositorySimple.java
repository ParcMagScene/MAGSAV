package com.magsav.repo;

import com.magsav.db.DB;
import com.magsav.model.Planification;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Repository simplifié pour la gestion des planifications d'interventions
 * Adapté au modèle Planification existant
 */
public class PlanificationRepositorySimple {
    
    private Connection connection;
    
    public PlanificationRepositorySimple() {
        try {
            this.connection = DB.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sauvegarder une planification (version simplifiée)
     */
    public boolean save(Planification planification) {
        if (planification.getId() == 0) {
            return create(planification);
        } else {
            return update(planification);
        }
    }
    
    /**
     * Créer une nouvelle planification
     */
    private boolean create(Planification planification) {
        String sql = """
            INSERT INTO planifications (
                technicien_id, intervention_id, client_id, vehicule_id,
                date_planifiee, duree_estimee, statut, priorite, type_intervention,
                lieu_intervention, coordonnees_gps, equipements_requis, notes_planification,
                technicien_nom, client_nom, intervention_numero, vehicule_immatriculation,
                date_creation, date_modification
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, planification.getTechnicienId());
            stmt.setInt(2, planification.getInterventionId());
            stmt.setInt(3, planification.getClientId());
            stmt.setInt(4, planification.getVehiculeId());
            stmt.setString(5, planification.getDatePlanifiee());
            stmt.setInt(6, planification.getDureeEstimee());
            stmt.setString(7, planification.getStatut().name());
            stmt.setString(8, planification.getPriorite().name());
            stmt.setString(9, planification.getTypeIntervention().name());
            stmt.setString(10, planification.getLieuIntervention());
            stmt.setString(11, planification.getCoordonneesGps());
            stmt.setString(12, planification.getEquipementsRequis());
            stmt.setString(13, planification.getNotesPlanification());
            stmt.setString(14, planification.getTechnicienNom());
            stmt.setString(15, planification.getClientNom());
            stmt.setString(16, planification.getInterventionNumero());
            stmt.setString(17, planification.getVehiculeImmatriculation());
            stmt.setString(18, planification.getDateCreation());
            stmt.setString(19, LocalDateTime.now().toString());
            
            int result = stmt.executeUpdate();
            
            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    planification.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Mettre à jour une planification existante
     */
    private boolean update(Planification planification) {
        String sql = """
            UPDATE planifications SET
                technicien_id = ?, intervention_id = ?, client_id = ?, vehicule_id = ?,
                date_planifiee = ?, duree_estimee = ?, statut = ?, priorite = ?, type_intervention = ?,
                lieu_intervention = ?, coordonnees_gps = ?, equipements_requis = ?, notes_planification = ?,
                technicien_nom = ?, client_nom = ?, intervention_numero = ?, vehicule_immatriculation = ?,
                date_modification = ?
            WHERE id = ?
            """;
            
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, planification.getTechnicienId());
            stmt.setInt(2, planification.getInterventionId());
            stmt.setInt(3, planification.getClientId());
            stmt.setInt(4, planification.getVehiculeId());
            stmt.setString(5, planification.getDatePlanifiee());
            stmt.setInt(6, planification.getDureeEstimee());
            stmt.setString(7, planification.getStatut().name());
            stmt.setString(8, planification.getPriorite().name());
            stmt.setString(9, planification.getTypeIntervention().name());
            stmt.setString(10, planification.getLieuIntervention());
            stmt.setString(11, planification.getCoordonneesGps());
            stmt.setString(12, planification.getEquipementsRequis());
            stmt.setString(13, planification.getNotesPlanification());
            stmt.setString(14, planification.getTechnicienNom());
            stmt.setString(15, planification.getClientNom());
            stmt.setString(16, planification.getInterventionNumero());
            stmt.setString(17, planification.getVehiculeImmatriculation());
            stmt.setString(18, LocalDateTime.now().toString());
            stmt.setInt(19, planification.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Trouver toutes les planifications
     */
    public ObservableList<Planification> findAll() {
        String sql = "SELECT * FROM planifications ORDER BY date_planifiee";
        ObservableList<Planification> planifications = FXCollections.observableArrayList();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                planifications.add(mapResultSetToPlanification(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return planifications;
    }
    
    /**
     * Trouver les planifications d'une semaine (version simplifiée)
     */
    public ObservableList<Planification> findPlanificationsSemaine(LocalDate debut, LocalDate fin) {
        // Pour l'instant, on retourne toutes les planifications
        // Une version plus avancée filtrerait par date
        return findAll();
    }
    
    /**
     * Supprimer une planification
     */
    public boolean delete(long id) {
        String sql = "DELETE FROM planifications WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Mapper un ResultSet vers un objet Planification
     */
    private Planification mapResultSetToPlanification(ResultSet rs) throws SQLException {
        Planification planification = new Planification();
        
        planification.setId(rs.getInt("id"));
        planification.setTechnicienId(rs.getInt("technicien_id"));
        planification.setInterventionId(rs.getInt("intervention_id"));
        planification.setClientId(rs.getInt("client_id"));
        planification.setVehiculeId(rs.getInt("vehicule_id"));
        
        planification.setDatePlanifiee(rs.getString("date_planifiee"));
        planification.setDureeEstimee(rs.getInt("duree_estimee"));
        
        String statutStr = rs.getString("statut");
        if (statutStr != null) {
            try {
                planification.setStatut(Planification.StatutPlanification.valueOf(statutStr));
            } catch (IllegalArgumentException e) {
                planification.setStatut(Planification.StatutPlanification.PLANIFIE);
            }
        }
        
        String prioriteStr = rs.getString("priorite");
        if (prioriteStr != null) {
            try {
                planification.setPriorite(Planification.PrioritePlanification.valueOf(prioriteStr));
            } catch (IllegalArgumentException e) {
                planification.setPriorite(Planification.PrioritePlanification.NORMALE);
            }
        }
        
        String typeStr = rs.getString("type_intervention");
        if (typeStr != null) {
            try {
                planification.setTypeIntervention(Planification.TypeIntervention.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                planification.setTypeIntervention(Planification.TypeIntervention.MAINTENANCE);
            }
        }
        
        planification.setLieuIntervention(rs.getString("lieu_intervention"));
        planification.setCoordonneesGps(rs.getString("coordonnees_gps"));
        planification.setEquipementsRequis(rs.getString("equipements_requis"));
        planification.setNotesPlanification(rs.getString("notes_planification"));
        
        planification.setTechnicienNom(rs.getString("technicien_nom"));
        planification.setClientNom(rs.getString("client_nom"));
        planification.setInterventionNumero(rs.getString("intervention_numero"));
        planification.setVehiculeImmatriculation(rs.getString("vehicule_immatriculation"));
        
        planification.setDateCreation(rs.getString("date_creation"));
        planification.setDateModification(rs.getString("date_modification"));
        
        return planification;
    }
    
    /**
     * Récupère les planifications à venir pour les N prochains jours
     */
    public ObservableList<Planification> findUpcoming(int days) {
        ObservableList<Planification> planifications = FXCollections.observableArrayList();
        
        String sql = """
            SELECT 
                p.id, p.technicien_id, p.intervention_id, p.client_id, p.vehicule_id,
                p.date_planifiee, p.duree_estimee, p.statut, p.priorite, p.type_intervention,
                p.lieu_intervention, p.coordonnees_gps, p.equipements_requis, p.notes_planification,
                COALESCE(t.nom, 'Technicien inconnu') as technicien_nom,
                COALESCE(s.nom_societe, 'Client inconnu') as client_nom,
                COALESCE('INT-' || i.id, 'N/A') as intervention_numero,
                COALESCE(v.immatriculation, 'N/A') as vehicule_immatriculation,
                p.date_creation, p.date_modification
            FROM planifications p
            LEFT JOIN techniciens t ON p.technicien_id = t.id
            LEFT JOIN societes s ON p.client_id = s.id  
            LEFT JOIN interventions i ON p.intervention_id = i.id
            LEFT JOIN vehicules v ON p.vehicule_id = v.id
            WHERE p.date_planifiee >= CURRENT_DATE 
              AND p.date_planifiee <= DATEADD('DAY', ?, CURRENT_DATE)
              AND p.statut IN ('PLANIFIE', 'CONFIRME')
            ORDER BY p.date_planifiee ASC, technicien_nom
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, days);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    planifications.add(mapResultSetToPlanification(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des planifications à venir: " + e.getMessage());
            e.printStackTrace();
        }
        
        return planifications;
    }
}