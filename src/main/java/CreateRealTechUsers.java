// Script temporaire pour créer des utilisateurs avec les vrais hashs de mot de passe
import com.magsav.service.AuthenticationService;
import com.magsav.model.User;
import com.magsav.db.DB;
import java.sql.*;

public class CreateRealTechUsers {
    public static void main(String[] args) {
        try {
            System.out.println("=== CRÉATION DES VRAIS UTILISATEURS TECHNICIENS ===");
            
            AuthenticationService authService = new AuthenticationService();
            String password = "tech123";
            
            // Données des techniciens
            String[] techniciens = {
                "cyril.dubois|Cyril Dubois|cyril.dubois@magscene.fr|06 12 34 56 78|Technicien Distribution",
                "celian.martin|Célian Martin|celian.martin@magscene.fr|06 23 45 67 89|Technicien Lumière", 
                "ben.lefebvre|Ben Lefebvre|ben.lefebvre@magscene.fr|06 34 56 78 90|Technicien Structure",
                "thomas.rousseau|Thomas Rousseau|thomas.rousseau@magscene.fr|06 45 67 89 01|Technicien Son",
                "flo.moreau|Flo Moreau|flo.moreau@magscene.fr|06 56 78 90 12|Stagiaire"
            };
            
            try (Connection conn = DB.getConnection()) {
                // Supprimer les anciens utilisateurs temporaires
                String deleteSql = "DELETE FROM users WHERE username IN (?, ?, ?, ?, ?)";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, "cyril.dubois");
                    deleteStmt.setString(2, "celian.martin");
                    deleteStmt.setString(3, "ben.lefebvre");
                    deleteStmt.setString(4, "thomas.rousseau");
                    deleteStmt.setString(5, "flo.moreau");
                    int deleted = deleteStmt.executeUpdate();
                    System.out.println("Supprimé " + deleted + " anciens utilisateurs temporaires");
                }
                
                // Créer les nouveaux utilisateurs avec AuthenticationService
                for (String techData : techniciens) {
                    String[] parts = techData.split("\\|");
                    String username = parts[0];
                    String fullName = parts[1];
                    String email = parts[2];
                    String phone = parts[3];
                    String position = parts[4];
                    
                    try {
                        User newUser = authService.createUser(username, email, password, 
                                                            User.Role.TECHNICIEN_MAG_SCENE, fullName, phone);
                        
                        // Mettre à jour la position
                        String updateSql = "UPDATE users SET position = ? WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, position);
                            updateStmt.setInt(2, newUser.id());
                            updateStmt.executeUpdate();
                        }
                        
                        System.out.println("✅ Créé : " + username + " (" + fullName + ") - ID: " + newUser.id());
                        
                    } catch (Exception e) {
                        System.err.println("❌ Erreur pour " + username + " : " + e.getMessage());
                    }
                }
                
                // Lier aux données techniciens
                String[] updateQueries = {
                    "UPDATE techniciens SET user_id = (SELECT id FROM users WHERE username = 'cyril.dubois') WHERE nom = 'Dubois' AND prenom = 'Cyril'",
                    "UPDATE techniciens SET user_id = (SELECT id FROM users WHERE username = 'celian.martin') WHERE nom = 'Martin' AND prenom = 'Célian'",
                    "UPDATE techniciens SET user_id = (SELECT id FROM users WHERE username = 'ben.lefebvre') WHERE nom = 'Lefebvre' AND prenom = 'Ben'",
                    "UPDATE techniciens SET user_id = (SELECT id FROM users WHERE username = 'thomas.rousseau') WHERE nom = 'Rousseau' AND prenom = 'Thomas'",
                    "UPDATE techniciens SET user_id = (SELECT id FROM users WHERE username = 'flo.moreau') WHERE nom = 'Moreau' AND prenom = 'Flo'"
                };
                
                for (String updateSql : updateQueries) {
                    try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                        stmt.executeUpdate();
                    }
                }
                
                System.out.println("\n✅ Liens techniciens ↔ users mis à jour");
                
                // Vérification finale
                String verifySql = """
                    SELECT u.username, u.full_name, u.email, u.role, u.position, t.user_id
                    FROM users u
                    LEFT JOIN techniciens t ON u.id = t.user_id  
                    WHERE u.role = 'TECHNICIEN_MAG_SCENE'
                    ORDER BY u.full_name
                """;
                
                System.out.println("\n📋 UTILISATEURS CRÉÉS :");
                try (PreparedStatement stmt = conn.prepareStatement(verifySql);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    while (rs.next()) {
                        System.out.printf("• %s (%s) - %s - %s%n", 
                            rs.getString("full_name"),
                            rs.getString("username"),
                            rs.getString("position"),
                            rs.getString("email"));
                    }
                }
                
                System.out.println("\n🔑 IDENTIFIANTS DE CONNEXION :");
                System.out.println("Mot de passe pour tous : " + password);
                
            }
            
        } catch (Exception e) {
            System.err.println("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}