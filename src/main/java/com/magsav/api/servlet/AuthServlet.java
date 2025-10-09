package com.magsav.api.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magsav.model.User;
import com.magsav.service.AuthenticationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Servlet d'authentification pour l'API REST
 */
public class AuthServlet extends HttpServlet {
    
    private final ObjectMapper objectMapper;
    private final AuthenticationService authService;
    
    public AuthServlet(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.authService = new AuthenticationService();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Permettre CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        
        String pathInfo = request.getPathInfo();
        
        if ("/login".equals(pathInfo)) {
            handleLogin(request, response);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(request, response);
        } else if ("/me".equals(pathInfo)) {
            handleCurrentUser(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeError(response, "Endpoint non trouvé");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Permettre CORS
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        
        if ("/me".equals(pathInfo)) {
            handleCurrentUser(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writeError(response, "Endpoint non trouvé");
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Gestion CORS pour les requêtes préliminaires
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * Gère la connexion d'un utilisateur
     */
    private void handleLogin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        try {
            // Lire les données JSON
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            
            if (loginRequest.username == null || loginRequest.password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writeError(response, "Nom d'utilisateur et mot de passe requis");
                return;
            }
            
            // Authentifier l'utilisateur
            Optional<User> userOpt = authService.authenticate(loginRequest.username, loginRequest.password);
            
            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                writeError(response, "Nom d'utilisateur ou mot de passe incorrect");
                return;
            }
            
            User user = userOpt.get();
            
            // Créer une session
            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.id());
            session.setAttribute("username", user.username());
            session.setAttribute("role", user.role().name());
            
            // Réponse de succès
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("user", createUserResponse(user));
            responseData.put("sessionId", session.getId());
            
            objectMapper.writeValue(response.getWriter(), responseData);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, "Erreur lors de l'authentification");
        }
    }
    
    /**
     * Gère la déconnexion d'un utilisateur
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("message", "Déconnecté avec succès");
        
        objectMapper.writeValue(response.getWriter(), responseData);
    }
    
    /**
     * Retourne les informations de l'utilisateur connecté
     */
    private void handleCurrentUser(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeError(response, "Non authentifié");
            return;
        }
        
        try {
            Integer userId = (Integer) session.getAttribute("userId");
            // Ici on pourrait récupérer l'utilisateur complet depuis la base
            // Pour simplifier, on utilise les données de session
            
            Map<String, Object> user = new HashMap<>();
            user.put("id", userId);
            user.put("username", session.getAttribute("username"));
            user.put("role", session.getAttribute("role"));
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("user", user);
            
            objectMapper.writeValue(response.getWriter(), responseData);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeError(response, "Erreur lors de la récupération des données utilisateur");
        }
    }
    
    /**
     * Crée une réponse utilisateur sécurisée (sans mot de passe)
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.id());
        userResponse.put("username", user.username());
        userResponse.put("email", user.email());
        userResponse.put("fullName", user.fullName());
        userResponse.put("role", user.role().name());
        userResponse.put("canAccessDesktop", user.canAccessDesktop());
        return userResponse;
    }
    
    /**
     * Écrit une erreur en JSON
     */
    private void writeError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        objectMapper.writeValue(response.getWriter(), error);
    }
    
    /**
     * Classe pour désérialiser les requêtes de connexion
     */
    public static class LoginRequest {
        public String username;
        public String password;
    }
}