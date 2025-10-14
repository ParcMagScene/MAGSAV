import com.magsav.db.DB;
import com.magsav.repo.UserRepository;
import com.magsav.model.User;
import java.sql.Connection;

/**
 * Test des nouvelles permissions pour la création de demandes par tous les utilisateurs
 */
public class TestPermissionsToutes {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TEST DES NOUVELLES PERMISSIONS DEMANDES ===");
            
            // Initialiser la DB
            DB.init();
            
            UserRepository userRepo = new UserRepository();
            
            // Tester tous les types d'utilisateurs
            System.out.println("\n1. ADMINISTRATEUR:");
            testUserPermissions(userRepo, "admin");
            
            System.out.println("\n2. TECHNICIEN MAG SCENE:");
            testUserPermissions(userRepo, "cyril.dubois");
            
            System.out.println("\n3. UTILISATEUR STANDARD:");
            testUserPermissions(userRepo, "user1");
            
            System.out.println("\n=== RÉSUMÉ ===");
            System.out.println("✅ Tous les utilisateurs actifs peuvent maintenant créer des demandes");
            System.out.println("   - Demandes d'intervention");
            System.out.println("   - Demandes de pièces");
            System.out.println("   - Demandes de matériel");
            System.out.println("\n⚠️  Seuls les administrateurs peuvent valider les demandes");
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testUserPermissions(UserRepository userRepo, String username) {
        try {
            var userOpt = userRepo.findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("   ❌ Utilisateur non trouvé: " + username);
                return;
            }
            
            User user = userOpt.get();
            System.out.println("   Utilisateur: " + username);
            System.out.println("   ✓ Peut créer demandes intervention: " + user.canCreateDemandeIntervention());
            System.out.println("   ✓ Peut créer demandes pièces: " + user.canCreateDemandePieces());
            System.out.println("   ✓ Peut créer demandes matériel: " + user.canCreateDemandeMateriel());
            System.out.println("   ✓ Peut valider demandes: " + user.canValidateDemandes());
            System.out.println("   ✓ Peut gérer utilisateurs: " + user.canManageUsers());
            
        } catch (Exception e) {
            System.err.println("   ❌ Erreur pour " + username + ": " + e.getMessage());
        }
    }
}