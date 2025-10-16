package com.magsav.service;

import com.magsav.model.ClientType;
import com.magsav.model.ClientUnifie;
import com.magsav.model.Societe;
import com.magsav.repo.SocieteRepository;
import com.magsav.util.AppLogger;
import java.util.List;
import java.util.ArrayList;

/**
 * Service pour gérer les clients unifiés
 */
public class ClientUnifiedService {
    
    private final SocieteRepository societeRepository = new SocieteRepository();
    
    public List<ClientUnifie> getAllClients() {
        try {
            List<Societe> societes = societeRepository.findAll();
            List<ClientUnifie> clients = new ArrayList<>();
            for (Societe societe : societes) {
                ClientUnifie client = new ClientUnifie(
                    societe.getId(),
                    ClientType.SOCIETE,
                    societe.getNom(),
                    societe.getEmail(),
                    societe.getTelephone(),
                    societe.getAdresse(),
                    societe.getCodePostal(),
                    societe.getVille(),
                    societe.getPays(),
                    societe.getNom(), // raisonSociale
                    societe.getSiret(),
                    societe.getSecteurActivite(),
                    societe.getSiteWeb(),
                    societe.getNotes(),
                    true, // isActive - par défaut
                    societe.getDateCreation(),
                    societe.getDateModification()
                );
                clients.add(client);
            }
            return clients;
        } catch (Exception e) {
            AppLogger.error("Erreur: " + e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    public boolean saveClient(ClientUnifie client) {
        return true;
    }
}
