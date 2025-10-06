package com.magsav.debug;

import com.magsav.db.DB;
import com.magsav.repo.InterventionRepository;

public class TestData {
    public static void main(String[] args) {
        try {
            DB.init();
            System.out.println("Database initialized successfully");
            
            InterventionRepository repo = new InterventionRepository();
            var rows = repo.findAllWithProductName();
            
            System.out.println("Found " + rows.size() + " interventions:");
            for (var row : rows) {
                System.out.println("ID: " + row.id() + 
                                 ", Product: " + row.produitNom() + 
                                 ", Status: " + row.statut() + 
                                 ", Problem: " + row.panne() +
                                 ", Entry: " + row.dateEntree() +
                                 ", Exit: " + row.dateSortie());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}