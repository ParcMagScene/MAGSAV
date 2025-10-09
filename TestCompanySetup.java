import com.magsav.db.DB;
import com.magsav.model.Company;
import com.magsav.model.User;
import com.magsav.model.User.Role;
import com.magsav.repo.CompanyRepository;
import com.magsav.repo.UserRepository;
import com.magsav.util.AppLogger;
import com.magsav.util.PasswordUtils;

import java.sql.Connection;

/**
 * Script de test pour initialiser la société Mag Scène et créer un utilisateur admin
 */
public class TestCompanySetup {
    
    public static void main(String[] args) {
        try {
            // Initialisation de la base de données
            DB.initializeTables();
            
            Connection conn = DB.getConnection();
            CompanyRepository companyRepo = new CompanyRepository(conn);
            UserRepository userRepo = new UserRepository();
            
            // S'assurer que la table users est à jour
            UserRepository.ensureUserTableUpdated();
            
            // Création de la société Mag Scène
            Company magScene = companyRepo.createDefaultMagScene();
            System.out.println("Société créée : " + magScene.getName() + " (ID: " + magScene.getId() + ")");
            
            // Création d'un utilisateur admin de test
            String hashedPassword = PasswordUtils.hashPassword("admin123");
            
            User admin = new User(
                null, // ID sera généré
                "admin",
                "admin@magscene.com",
                hashedPassword,
                Role.ADMIN,
                "Administrateur Mag Scène",
                "01.23.45.67.89",
                magScene.getId(), // Associé à Mag Scène
                "Directeur Technique", // Position
                null, // Pas d'avatar
                true, // Actif
                null, // Pas de token
                null, // Pas d'expiration
                null, // Pas de dernière connexion
                null  // Date de création sera générée
            );
            
            long userId = userRepo.createUser(admin);
            System.out.println("Utilisateur admin créé avec l'ID : " + userId);
            System.out.println("Login : admin / Mot de passe : admin123");
            
            // Création d'un utilisateur technicien de test
            String hashedPasswordTech = PasswordUtils.hashPassword("tech123");
            
            User technicien = new User(
                null,
                "tech1",
                "technicien@magscene.com",
                hashedPasswordTech,
                Role.TECHNICIEN_MAG_SCENE,
                "Jean Dupont",
                "01.23.45.67.90",
                magScene.getId(),
                "Technicien Audio",
                null,
                true,
                null,
                null,
                null,
                null
            );
            
            long techId = userRepo.createUser(technicien);
            System.out.println("Utilisateur technicien créé avec l'ID : " + techId);
            System.out.println("Login : tech1 / Mot de passe : tech123");
            
            System.out.println("\nConfiguration terminée avec succès !");
            System.out.println("Vous pouvez maintenant vous connecter et tester l'onglet Société dans les préférences.");
            
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}