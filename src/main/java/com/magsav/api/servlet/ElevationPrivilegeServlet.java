package com.magsav.api.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magsav.model.DemandeElevationPrivilege;
import com.magsav.model.User;
import com.magsav.service.DemandeElevationPrivilegeService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet pour gérer les demandes d'élévation de privilèges via API REST
 */
public class ElevationPrivilegeServlet extends HttpServlet {
    
    private final ObjectMapper objectMapper;
    private final DemandeElevationPrivilegeService demandeService;
    
    public ElevationPrivilegeServlet(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.demandeService = new DemandeElevationPrivilegeService();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /elevation - Liste des demandes d'élévation (pour test)
                List<DemandeElevationPrivilege> demandes = demandeService.getAllDemandes(10, 0);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("demandes", demandes);
                
                objectMapper.writeValue(response.getWriter(), responseData);
                
            } else if (pathInfo.equals("/en-attente")) {
                // GET /elevation/en-attente - Liste des demandes en attente
                List<DemandeElevationPrivilege> demandes = demandeService.getDemandesEnAttente();
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("demandes", demandes);
                
                objectMapper.writeValue(response.getWriter(), responseData);
                
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writeError(response, "Endpoint non trouvé");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, "Erreur interne du serveur: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        try {
            // Lire le body de la requête
            @SuppressWarnings("unchecked")
            Map<String, Object> requestData = objectMapper.readValue(request.getReader(), Map.class);
            
            String roleDemandeStr = (String) requestData.get("roleDemande");
            String justification = (String) requestData.get("justification");
            
            if (roleDemandeStr == null || justification == null || justification.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(response, "Paramètres requis : roleDemande, justification");
                return;
            }
            
            User.Role roleDemande;
            try {
                roleDemande = User.Role.valueOf(roleDemandeStr);
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(response, "Rôle invalide : " + roleDemandeStr);
                return;
            }
            
            // Pour les tests, on simule un utilisateur avec ID 1 (sera remplacé par l'authentification)
            int userId = 1;
            String createdBy = "test-user";
            
            // Créer la demande
            DemandeElevationPrivilege demande = demandeService.creerDemande(
                userId, roleDemande, justification, createdBy
            );
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Demande d'élévation créée avec succès");
            responseData.put("demande", demande);
            
            objectMapper.writeValue(response.getWriter(), responseData);
            
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeError(response, e.getMessage());
            
        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            writeError(response, e.getMessage());
            
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, "Erreur interne du serveur: " + e.getMessage());
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Méthode utilitaire pour écrire une réponse d'erreur
     */
    private void writeError(HttpServletResponse response, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        objectMapper.writeValue(response.getWriter(), error);
    }
}