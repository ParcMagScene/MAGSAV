package com.magsav.util;

import com.magsav.db.DB;
import com.magsav.db.ConnectionPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
/**
 * Utilitaire pour configurer les bases de données de test de manière isolée
 */
public class TestDatabaseConfig {
    
    /**
     * Configure une base de données unique pour un test avec isolation complète
     * 
     * @param testName nom du test pour identifier la base
     * @return connexion keeper pour maintenir la base en mémoire
     */
    public static Connection setupIsolatedInMemoryDb(String testName) throws SQLException {
        // Utiliser une base en mémoire simple - plus fiable que les bases nommées
        String testDbUrl = "jdbc:sqlite::memory:";
        
        // Nettoyer l'état précédent
        System.clearProperty("magsav.db.url");
        DB.resetForTesting();
        ConnectionPool.resetForTesting();
        
        // Créer la connexion keeper d'abord
        Connection keeper = DriverManager.getConnection(testDbUrl);
        
        // Configurer l'URL APRÈS avoir créé la connexion keeper
        System.setProperty("magsav.db.url", testDbUrl);
        
        // Forcer DB.init() AVANT de créer le schéma, 
        // pour que DB utilise la même base que le keeper
        DB.init();
        
        // Maintenant créer le schéma via DB.getConnection() 
        // pour qu'il soit dans la même base que le ConnectionPool
        try (Connection dbConn = DB.getConnection()) {
            createTestSchema(dbConn);
        }
        
        return keeper;
    }
    
    /**
     * Crée un schéma de base minimal pour les tests
     */
    private static void createTestSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            // Table produits (colonnes correspondant à ProductRepository)
            st.execute("""
                CREATE TABLE IF NOT EXISTS produits (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code_produit TEXT,
                    nom_produit TEXT NOT NULL,
                    numero_serie TEXT,
                    nom_fabricant TEXT,
                    fabricant_id INTEGER,
                    uid_unique TEXT UNIQUE,
                    statut_produit TEXT DEFAULT 'En stock',
                    photo_produit TEXT,
                    categorie_principale TEXT,
                    sous_categorie TEXT,
                    description_produit TEXT,
                    date_achat TEXT,
                    nom_client TEXT,
                    prix_achat TEXT,
                    duree_garantie TEXT,
                    sav_externe_id INTEGER
                )
            """);
            
            // Table interventions (colonnes correspondant à InterventionRepository)
            st.execute("""
                CREATE TABLE IF NOT EXISTS interventions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    produit_id INTEGER,
                    numero_serie TEXT,
                    date_creation TEXT DEFAULT (datetime('now')),
                    description TEXT,
                    statut TEXT DEFAULT 'En cours',
                    FOREIGN KEY (produit_id) REFERENCES produits(id)
                )
            """);
            
            // Table email_templates (pour éviter les erreurs d'initialisation)
            st.execute("""
                CREATE TABLE IF NOT EXISTS email_templates (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nom_template TEXT NOT NULL UNIQUE,
                    type_template TEXT NOT NULL,
                    objet TEXT NOT NULL,
                    contenu_html TEXT NOT NULL,
                    contenu_text TEXT,
                    variables_disponibles TEXT,
                    actif BOOLEAN DEFAULT TRUE,
                    date_creation TEXT NOT NULL DEFAULT (datetime('now')),
                    date_modification TEXT NOT NULL DEFAULT (datetime('now'))
                )
            """);
        }
    }
    
    /**
     * Version simplifiée pour les tests qui n'ont pas besoin d'isolation complète
     */
    public static Connection setupSharedInMemoryDb(String testName) throws SQLException {
        // Utiliser une base partagée mais avec des réglages optimisés
        String testDbUrl = "jdbc:sqlite:file:shared_test_db?mode=memory&cache=shared";
        
        System.clearProperty("magsav.db.url");
        DB.resetForTesting();
        
        System.setProperty("magsav.db.url", testDbUrl);
        
        Connection keeper = DriverManager.getConnection(testDbUrl);
        DB.init();
        
        return keeper;
    }
    
    /**
     * Nettoie la configuration de test
     */
    public static void cleanup() {
        System.clearProperty("magsav.db.url");
        DB.resetForTesting();
        ConnectionPool.resetForTesting();
    }
    
    /**
     * Nettoie et ferme une connexion keeper de test
     */
    public static void cleanupKeeper(Connection keeper) {
        if (keeper != null) {
            try {
                keeper.close();
            } catch (SQLException e) {
                // Ignorer les erreurs de fermeture
            }
        }
        cleanup();
    }
}