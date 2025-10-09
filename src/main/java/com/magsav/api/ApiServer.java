package com.magsav.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.magsav.api.servlet.AuthServlet;
import com.magsav.api.servlet.DemandeInterventionServlet;
import com.magsav.api.servlet.ElevationPrivilegeServlet;
import com.magsav.api.servlet.ProprietaireServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Serveur API REST léger avec Jetty
 * Expose les fonctionnalités MAGSAV aux clients web/mobile
 */
public class ApiServer {
    
    private static final int API_PORT = 8080;
    private Server server;
    private ObjectMapper objectMapper;
    
    public ApiServer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Démarre le serveur API
     */
    public void start() throws Exception {
        server = new Server(API_PORT);
        
        // Configuration du contexte
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/api");
        server.setHandler(context);
        
        // Configuration des servlets
        setupServlets(context);
        
        // CORS et autres filtres
        setupFilters(context);
        
        server.start();
        System.out.println("API MAGSAV démarrée sur http://localhost:" + API_PORT + "/api");
        System.out.println("Endpoints disponibles:");
        System.out.println("  POST /api/auth/login - Authentification");
        System.out.println("  GET  /api/demandes - Liste des demandes d'intervention");
        System.out.println("  POST /api/demandes - Créer une demande d'intervention");
        System.out.println("  GET  /api/proprietaires - Liste des propriétaires");
    }
    
    /**
     * Configuration des servlets
     */
    private void setupServlets(ServletContextHandler context) {
        // Servlet d'authentification
        context.addServlet(new ServletHolder(new AuthServlet(objectMapper)), "/auth/*");
        
        // Servlet pour les demandes d'intervention
        context.addServlet(new ServletHolder(new DemandeInterventionServlet(objectMapper)), "/demandes/*");
        
        // Servlet pour l'élévation de privilèges
        context.addServlet(new ServletHolder(new ElevationPrivilegeServlet(objectMapper)), "/elevation/*");
        
        // Servlet pour les propriétaires
        context.addServlet(new ServletHolder(new ProprietaireServlet(objectMapper)), "/proprietaires/*");
    }
    
    /**
     * Configuration des filtres (CORS, authentification, etc.)
     */
    private void setupFilters(ServletContextHandler context) {
        // TODO: Ajouter filtres CORS et authentification
    }
    
    /**
     * Arrête le serveur API
     */
    public void stop() throws Exception {
        if (server != null) {
            server.stop();
            System.out.println("API MAGSAV arrêtée");
        }
    }
    
    /**
     * Point d'entrée pour lancer l'API en mode standalone
     */
    public static void main(String[] args) {
        ApiServer apiServer = new ApiServer();
        
        try {
            apiServer.start();
            apiServer.server.join(); // Attend que le serveur soit arrêté
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}