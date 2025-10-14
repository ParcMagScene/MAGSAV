import com.magsav.repo.UserRepository;
import com.magsav.model.User;
import java.util.List;

public class TestUserRepository {
    
    public static void main(String[] args) {
        try {
            UserRepository userRepo = new UserRepository();
            
            System.out.println("=== Test de récupération de tous les utilisateurs ===");
            
            List<User> users = userRepo.findAll();
            System.out.println("✅ Succès ! " + users.size() + " utilisateurs récupérés");
            
            for (User user : users) {
                System.out.println("👤 " + user.username() + " (" + user.role() + ") - " + user.fullName());
                System.out.println("   Position: " + user.position());
                System.out.println("   Société: " + user.companyId());
                System.out.println("   Actif: " + user.isActive());
                System.out.println();
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}