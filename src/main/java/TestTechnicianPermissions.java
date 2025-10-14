import com.magsav.model.User;
import com.magsav.repo.UserRepository;

/**
 * Script de test pour vérifier les permissions de chaque technicien
 */
public class TestTechnicianPermissions {
    
    public static void main(String[] args) {
        try {
            System.out.println("=== TEST DES PERMISSIONS TECHNICIENS MAG SCENE ===\n");
            
            UserRepository userRepo = new UserRepository();
            
            // Récupérer tous les techniciens Mag Scène
            var techniciens = userRepo.findByRole(User.Role.TECHNICIEN_MAG_SCENE);
            
            if (techniciens.isEmpty()) {
                System.out.println("❌ Aucun technicien trouvé avec le rôle TECHNICIEN_MAG_SCENE");
                return;
            }
            
            for (User tech : techniciens) {
                System.out.println("👤 " + tech.fullName() + " (" + tech.username() + ")");
                System.out.println("   Fonction: " + (tech.position() != null ? tech.position() : "Non définie"));
                System.out.println("   Email: " + tech.email());
                System.out.println();
                
                // Afficher les permissions
                System.out.println("🔐 " + tech.getPermissionsSummary());
                
                // Tests spécifiques
                System.out.println("📋 Tests de permissions spécifiques :");
                testSpecificPermissions(tech);
                
                System.out.println("═".repeat(60) + "\n");
            }
            
            // Test global des permissions
            System.out.println("🧪 TESTS GLOBAUX DE PERMISSIONS\n");
            testGlobalPermissions(techniciens);
            
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Teste des permissions spécifiques pour un technicien
     */
    private static void testSpecificPermissions(User user) {
        // Tests basiques
        System.out.println("   ✓ Peut voir produits: " + user.canViewAllProducts());
        System.out.println("   ✓ Peut créer interventions: " + user.canCreateDemandeIntervention());
        
        // Tests permissions nouvelles
        System.out.println("   ✓ Peut créer contacts: " + user.canCreateContacts());
        System.out.println("   ✓ Peut gérer véhicules: " + user.canManageVehicles());
        System.out.println("   ✓ Peut gérer planning: " + user.canManagePlanning());
        
        // Tests spécialisations techniques
        if (user.position() != null) {
            switch (user.position().toLowerCase()) {
                case "technicien distribution":
                    System.out.println("   🚛 Gestion distribution: " + user.canManageDistribution());
                    break;
                case "technicien lumière":
                    System.out.println("   💡 Gestion éclairage: " + user.canManageLighting());
                    break;
                case "technicien structure":
                    System.out.println("   🏗️ Gestion structure: " + user.canManageStructure());
                    System.out.println("   👥 Approbation collègues: " + user.canApproveColleagueRequests());
                    break;
                case "technicien son":
                    System.out.println("   🎵 Gestion audio: " + user.canManageAudio());
                    break;
                case "chauffeur pl":
                    System.out.println("   🚛 Chauffeur PL - Gestion véhicules/distribution: " + user.canManageVehicles());
                    break;
                case "chauffeur spl":
                    System.out.println("   🚐 Chauffeur SPL - Gestion véhicules/distribution: " + user.canManageVehicles());
                    break;
                case "stagiaire":
                    System.out.println("   🎓 Permissions limitées (stagiaire)");
                    break;
            }
        }
    }
    
    /**
     * Tests globaux sur l'ensemble des techniciens
     */
    private static void testGlobalPermissions(java.util.List<User> techniciens) {
        System.out.println("Nombre total de techniciens: " + techniciens.size());
        
        long withDistribution = techniciens.stream()
            .filter(User::canManageDistribution)
            .count();
        System.out.println("Techniciens avec gestion distribution: " + withDistribution);
        
        long withLighting = techniciens.stream()
            .filter(User::canManageLighting)
            .count();
        System.out.println("Techniciens avec gestion éclairage: " + withLighting);
        
        long withStructure = techniciens.stream()
            .filter(User::canManageStructure)
            .count();
        System.out.println("Techniciens avec gestion structure: " + withStructure);
        
        long withAudio = techniciens.stream()
            .filter(User::canManageAudio)
            .count();
        System.out.println("Techniciens avec gestion audio: " + withAudio);
        
        long withVehicles = techniciens.stream()
            .filter(User::canManageVehicles)
            .count();
        System.out.println("Techniciens avec gestion véhicules: " + withVehicles);
        
        long withApproval = techniciens.stream()
            .filter(User::canApproveColleagueRequests)
            .count();
        System.out.println("Techniciens avec droit d'approbation: " + withApproval);
        
        System.out.println("\n✅ Tests terminés avec succès !");
    }
}