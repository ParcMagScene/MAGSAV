package com.magscene.magsav.backend.service;

import com.magscene.magsav.backend.repository.ServiceRequestRepository;
import com.magscene.magsav.backend.repository.EquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Initialise les données de démonstration pour le module SAV
 * TEMPORAIREMENT DÉSACTIVÉ - Attente migration complète des statuts
 */
@Component
@Order(2)
public class SavDataInitializer implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("⚠️ SavDataInitializer temporairement désactivé - Migration des statuts en cours");
        // L'initialisation des données sera réactivée après la migration complète des
        // statuts
    }
}
