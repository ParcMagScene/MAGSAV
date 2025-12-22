package com.magscene.magsav.desktop.service;

import com.magscene.magsav.desktop.view.vehicle.VehicleAvailabilityView.Reservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Service partagé pour la gestion des réservations de véhicules
 * Permet la synchronisation entre les différentes vues (Planning et Véhicules)
 */
public class VehicleReservationService {
    
    private static VehicleReservationService instance;
    
    // Réservations par véhicule (vehicleId -> liste de réservations)
    private final Map<String, List<Reservation>> reservationsByVehicle = new HashMap<>();
    
    // Listeners pour notifier les vues des changements
    private final List<Consumer<String>> changeListeners = new CopyOnWriteArrayList<>();
    
    private VehicleReservationService() {
        // Singleton
    }
    
    public static synchronized VehicleReservationService getInstance() {
        if (instance == null) {
            instance = new VehicleReservationService();
        }
        return instance;
    }
    
    /**
     * Ajoute un listener pour être notifié des changements
     */
    public void addChangeListener(Consumer<String> listener) {
        changeListeners.add(listener);
    }
    
    /**
     * Retire un listener
     */
    public void removeChangeListener(Consumer<String> listener) {
        changeListeners.remove(listener);
    }
    
    /**
     * Notifie tous les listeners d'un changement
     */
    private void notifyChange(String vehicleId) {
        for (Consumer<String> listener : changeListeners) {
            try {
                listener.accept(vehicleId);
            } catch (Exception e) {
                System.err.println("Erreur lors de la notification de changement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Récupère les réservations pour un véhicule
     */
    public List<Reservation> getReservations(String vehicleId) {
        return reservationsByVehicle.computeIfAbsent(vehicleId, k -> new ArrayList<>());
    }
    
    /**
     * Récupère toutes les réservations
     */
    public Map<String, List<Reservation>> getAllReservations() {
        return new HashMap<>(reservationsByVehicle);
    }
    
    /**
     * Ajoute une réservation
     */
    public void addReservation(String vehicleId, Reservation reservation) {
        List<Reservation> reservations = reservationsByVehicle.computeIfAbsent(vehicleId, k -> new ArrayList<>());
        reservations.add(reservation);
        System.out.println("📅 Réservation ajoutée pour véhicule " + vehicleId + ": " + reservation.getTitle());
        notifyChange(vehicleId);
    }
    
    /**
     * Met à jour une réservation existante
     */
    public void updateReservation(String vehicleId, Reservation reservation) {
        System.out.println("📅 Réservation mise à jour pour véhicule " + vehicleId + ": " + reservation.getTitle());
        notifyChange(vehicleId);
    }
    
    /**
     * Supprime une réservation
     */
    public void removeReservation(String vehicleId, Reservation reservation) {
        List<Reservation> reservations = reservationsByVehicle.get(vehicleId);
        if (reservations != null) {
            reservations.remove(reservation);
            System.out.println("📅 Réservation supprimée pour véhicule " + vehicleId + ": " + reservation.getTitle());
            notifyChange(vehicleId);
        }
    }
    
    /**
     * Définit les réservations pour un véhicule (remplace toutes les existantes)
     */
    public void setReservations(String vehicleId, List<Reservation> reservations) {
        reservationsByVehicle.put(vehicleId, new ArrayList<>(reservations));
        notifyChange(vehicleId);
    }
    
    /**
     * Importe les réservations depuis une source externe (ne notifie pas)
     */
    public void importReservations(Map<String, List<Reservation>> reservations) {
        for (Map.Entry<String, List<Reservation>> entry : reservations.entrySet()) {
            reservationsByVehicle.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
    }
    
    /**
     * Efface toutes les réservations
     */
    public void clearAll() {
        reservationsByVehicle.clear();
        notifyChange(null);
    }
}
