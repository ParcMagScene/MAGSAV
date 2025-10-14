import com.magsav.service.AuthenticationService;
import com.magsav.model.User;
import java.util.Optional;

public class TestAuthentication {
    
    public static void main(String[] args) {
        try {
            AuthenticationService authService = new AuthenticationService();
            
            System.out.println("=== Test d'authentification des techniciens ===");
            
            // Test des identifiants de chaque technicien
            String[] logins = {"cyril.dubois", "celian.martin", "ben.lefebvre", "thomas.rousseau", "flo.moreau"};
            String password = "tech123";
            
            for (String login : logins) {
                System.out.println("\nTest login: " + login);
                Optional<User> userOpt = authService.authenticate(login, password);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("✅ Authentification réussie");
                    System.out.println("   Nom: " + user.fullName());
                    System.out.println("   Rôle: " + user.role());
                    System.out.println("   Position: " + user.position());
                    System.out.println("   Actif: " + user.isActive());
                    
                    // Test des permissions
                    System.out.println("   Permissions:");
                    System.out.println("     - Créer contacts: " + user.canCreateContacts());
                    System.out.println("     - Gérer véhicules: " + user.canManageVehicles());
                    System.out.println("     - Créer demandes intervention: " + user.canCreateDemandeIntervention());
                    System.out.println("     - Valider demandes: " + user.canValidateDemandes());
                    
                } else {
                    System.out.println("❌ Échec de l'authentification");
                }
            }
            
            // Test avec un mauvais mot de passe
            System.out.println("\n=== Test avec mauvais mot de passe ===");
            Optional<User> invalidAuth = authService.authenticate("cyril.dubois", "wrongpassword");
            if (invalidAuth.isEmpty()) {
                System.out.println("✅ Rejet correct du mauvais mot de passe");
            } else {
                System.out.println("❌ Erreur: mauvais mot de passe accepté");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}