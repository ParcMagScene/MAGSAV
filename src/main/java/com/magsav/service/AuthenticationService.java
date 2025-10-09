package com.magsav.service;

import com.magsav.model.User;
import com.magsav.repo.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Service d'authentification et de gestion des utilisateurs
 */
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final SecureRandom secureRandom;
    
    public AuthenticationService() {
        this.userRepository = new UserRepository();
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Authentifie un utilisateur avec nom d'utilisateur/email et mot de passe
     */
    public Optional<User> authenticate(String usernameOrEmail, String password) {
        // Chercher par nom d'utilisateur d'abord
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        
        // Si pas trouvé, chercher par email
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // Vérifier le mot de passe
        if (verifyPassword(password, user.passwordHash())) {
            // Mettre à jour la date de dernière connexion
            userRepository.updateLastLogin(user.id(), LocalDateTime.now());
            return Optional.of(user.withLastLogin(LocalDateTime.now()));
        }
        
        return Optional.empty();
    }
    
    /**
     * Crée un nouvel utilisateur avec mot de passe haché
     */
    public User createUser(String username, String email, String password, User.Role role, String fullName, String phone) {
        // Vérifier que le nom d'utilisateur et l'email n'existent pas
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Ce nom d'utilisateur existe déjà");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }
        
        // Hacher le mot de passe
        String passwordHash = hashPassword(password);
        
        // Créer l'utilisateur
        User user = new User(username, email, passwordHash, role, fullName, phone);
        long userId = userRepository.createUser(user);
        
        // Retourner l'utilisateur avec l'ID
        return new User(
            (int) userId, username, email, passwordHash, role, fullName, phone,
            null, null, null, // companyId, position, avatarPath
            true, LocalDateTime.now(), null, null, null
        );
    }
    
    /**
     * Change le mot de passe d'un utilisateur
     */
    public void changePassword(long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        
        User user = userOpt.get();
        
        // Vérifier l'ancien mot de passe
        if (!verifyPassword(oldPassword, user.passwordHash())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }
        
        // Mettre à jour avec le nouveau mot de passe
        String newPasswordHash = hashPassword(newPassword);
        userRepository.updatePassword(userId, newPasswordHash);
    }
    
    /**
     * Génère un token de reset de mot de passe
     */
    public String generateResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            throw new RuntimeException("Aucun utilisateur actif trouvé avec cet email");
        }
        
        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime expires = LocalDateTime.now().plusHours(24); // Token valide 24h
        
        userRepository.updateResetToken(user.id(), resetToken, expires);
        
        return resetToken;
    }
    
    /**
     * Reset le mot de passe avec un token
     */
    public void resetPassword(String resetToken, String newPassword) {
        // Chercher l'utilisateur avec ce token
        Optional<User> userOpt = findUserByResetToken(resetToken);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Token de reset invalide ou expiré");
        }
        
        User user = userOpt.get();
        if (!user.isResetTokenValid()) {
            throw new RuntimeException("Token de reset expiré");
        }
        
        // Mettre à jour le mot de passe et supprimer le token
        String newPasswordHash = hashPassword(newPassword);
        userRepository.updatePassword(user.id(), newPasswordHash);
    }
    
    /**
     * Valide un mot de passe selon les critères de sécurité
     */
    public boolean isPasswordValid(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // Au moins une lettre majuscule, une minuscule et un chiffre
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Hache un mot de passe avec sel
     */
    private String hashPassword(String password) {
        try {
            // Générer un sel aléatoire
            byte[] salt = new byte[16];
            secureRandom.nextBytes(salt);
            
            // Hacher le mot de passe avec le sel
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            
            // Combiner sel + hash et encoder en Base64
            byte[] combined = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
            
            return Base64.getEncoder().encodeToString(combined);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
    
    /**
     * Vérifie un mot de passe contre un hash
     */
    private boolean verifyPassword(String password, String hash) {
        try {
            // Décoder le hash
            byte[] combined = Base64.getDecoder().decode(hash);
            
            // Extraire le sel (16 premiers bytes)
            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, 16);
            
            // Extraire le hash (le reste)
            byte[] storedHash = new byte[combined.length - 16];
            System.arraycopy(combined, 16, storedHash, 0, storedHash.length);
            
            // Hacher le mot de passe fourni avec le même sel
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] testHash = md.digest(password.getBytes());
            
            // Comparer les hashs
            return MessageDigest.isEqual(storedHash, testHash);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Trouve un utilisateur par token de reset (méthode privée utilitaire)
     */
    private Optional<User> findUserByResetToken(String resetToken) {
        // Note: Cette méthode nécessiterait une requête SQL spécifique
        // Pour simplifier, on peut parcourir tous les utilisateurs actifs
        // Dans une vraie application, on ajouterait une méthode au repository
        return Optional.empty(); // TODO: implémenter la recherche par token
    }
    
    /**
     * Créer un administrateur par défaut si aucun n'existe
     */
    public void createDefaultAdminIfNotExists() {
        try {
            // Vérifier s'il existe déjà un admin
            if (!userRepository.findByRole(User.Role.ADMIN).isEmpty()) {
                return; // Un admin existe déjà
            }
            
            // Créer l'admin par défaut
            String defaultPassword = "Admin123!";
            createUser("admin", "admin@magscene.com", defaultPassword, 
                      User.Role.ADMIN, "Administrateur", null);
                      
            System.out.println("Administrateur par défaut créé:");
            System.out.println("  Nom d'utilisateur: admin");
            System.out.println("  Mot de passe: " + defaultPassword);
            System.out.println("  Email: admin@magscene.com");
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la création de l'admin par défaut: " + e.getMessage());
        }
    }
}