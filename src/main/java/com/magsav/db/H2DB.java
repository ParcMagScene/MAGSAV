package com.magsav.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.magsav.exception.DatabaseException;
import com.magsav.util.AppLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;
import java.nio.file.Paths;
import java.nio.file.Files;

/**
 * Gestionnaire de base de données H2 embarqué
 * Base de données performante et fiable pour MAGSAV
 */
public class H2DB {
    
    private static H2DB instance;
    private static HikariDataSource dataSource;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    
    private static final String DATABASE_PATH = "./data/magsav_h2";
    private static final String USERNAME = "magsav";
    private static final String PASSWORD = "magsav2024";
    
    private H2DB() {
        // Singleton
    }
    
    public static synchronized H2DB getInstance() {
        if (instance == null) {
            instance = new H2DB();
        }
        return instance;
    }
    
    /**
     * Initialise H2 embarqué
     */
    public static synchronized void init() {
        if (initialized.get()) {
            AppLogger.info("H2 Database déjà initialisé");
            return;
        }
        
        try {
            AppLogger.info("🗄️ Démarrage de H2 Database embarqué...");
            
            // S'assurer que le dossier data existe
            try {
                Files.createDirectories(Paths.get("./data"));
            } catch (Exception e) {
                // Ignore si existe déjà
            }
            
            // URL H2 avec mode mixte (file + serveur intégré)
            String jdbcUrl = "jdbc:h2:" + DATABASE_PATH + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";
            
            // Configuration HikariCP optimisée pour H2
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            config.setDriverClassName("org.h2.Driver");
            
            // Pool de connexions optimisé
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000); // 5 minutes
            config.setMaxLifetime(1800000); // 30 minutes
            config.setConnectionTimeout(10000); // 10 secondes
            
            // Propriétés H2 optimisées
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            
            // Créer le schéma initial
            ensureSchema();
            
            initialized.set(true);
            AppLogger.info("✅ H2 Database initialisé avec succès");
            
        } catch (SQLException e) {
            AppLogger.error("❌ Erreur lors de l'initialisation H2", e);
            throw new DatabaseException("Impossible d'initialiser H2", e);
        }
    }
    
    /**
     * Obtient une connexion à la base de données
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized.get()) {
            init();
        }
        
        if (dataSource == null) {
            throw new SQLException("DataSource non initialisé");
        }
        
        return dataSource.getConnection();
    }
    
    /**
     * Crée le schéma de base de données complet - toutes les tables de l'ancienne DB
     */
    private static void ensureSchema() throws SQLException {
        AppLogger.info("🔧 Création du schéma MAGSAV complet en H2...");
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Tables principales - dans l'ordre des dépendances
            createCategoriesTable(stmt);
            createSocietesTable(stmt);
            createUsersTable(stmt);
            createTechniciensTable(stmt);
            createVehiculesTable(stmt);
            createProduitsTable(stmt);
            createInterventionsTable(stmt);
            createRequestsTable(stmt);
            createRequestItemsTable(stmt);
            createAffairesTable(stmt);
            createDevisTable(stmt);
            createLignesDevisTable(stmt);
            createCommandesTable(stmt);
            createLignesCommandesTable(stmt);
            createMouvementsStockTable(stmt);
            createAlertesStockTable(stmt);
            createPlanificationsTable(stmt);
            createDisponibilitesTechniciensTable(stmt);
            createCommunicationsTable(stmt);
            createEmailTemplatesTable(stmt);
            createConfigurationGoogleTable(stmt);
            createSavHistoryTable(stmt);
            createSyncHistoryTable(stmt);
            
            AppLogger.info("✅ Schéma MAGSAV complet créé avec succès en H2");
            
        } catch (SQLException e) {
            AppLogger.error("❌ Erreur lors de la création du schéma H2", e);
            throw e;
        }
    }
    
    private static void createCategoriesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                nom_categorie TEXT,
                parent_id INTEGER
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createSocietesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS societes (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                type_societe TEXT DEFAULT 'COMPANY', 
                nom_societe TEXT NOT NULL,
                raison_sociale TEXT,
                siret TEXT,
                adresse_societe TEXT, 
                code_postal TEXT,
                ville TEXT,
                pays TEXT DEFAULT 'France',
                telephone_societe TEXT,
                email_societe TEXT,
                site_web TEXT,
                description TEXT,
                logo_path TEXT,
                secteur TEXT,
                notes_societe TEXT,
                is_active INTEGER DEFAULT 1,
                date_creation TEXT DEFAULT CURRENT_TIMESTAMP,
                date_modification TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createProduitsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS produits (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                code_produit TEXT, 
                nom_produit TEXT, 
                numero_serie TEXT,
                nom_fabricant TEXT, 
                fabricant_id INTEGER,
                uid_unique TEXT, 
                statut_produit TEXT,
                photo_produit TEXT, 
                categorie_principale TEXT, 
                sous_categorie TEXT,
                description_produit TEXT,
                date_achat TEXT,
                nom_client TEXT,
                prix_achat TEXT,
                duree_garantie TEXT,
                sav_externe_id INTEGER,
                scraped_images TEXT
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createInterventionsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS interventions (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                produit_id INTEGER,
                statut_intervention TEXT, 
                description_panne TEXT,
                numero_serie_intervention TEXT,
                note_client TEXT,
                description_defaut TEXT,
                detecteur_societe_id INTEGER,
                nom_detecteur TEXT,
                date_entree TEXT, 
                date_sortie TEXT,
                type_proprietaire TEXT, 
                proprietaire_societe_id INTEGER
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createUsersTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                username VARCHAR(100) UNIQUE NOT NULL,
                password_hash VARCHAR(255),
                nom VARCHAR(255) NOT NULL,
                prenom VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE,
                telephone VARCHAR(20),
                role VARCHAR(50) DEFAULT 'USER',
                specialite VARCHAR(255),
                date_embauche DATE,
                salaire DECIMAL(10,2),
                notes TEXT,
                avatar_filename VARCHAR(255),
                is_active BOOLEAN DEFAULT true,
                last_login TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createVehiculesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS vehicules (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                immatriculation VARCHAR(20) UNIQUE NOT NULL,
                marque VARCHAR(100) NOT NULL,
                modele VARCHAR(100) NOT NULL,
                annee INTEGER,
                couleur VARCHAR(50),
                kilometrage INTEGER DEFAULT 0,
                carburant VARCHAR(20) DEFAULT 'Essence',
                statut VARCHAR(50) DEFAULT 'Disponible',
                type_vehicule VARCHAR(50) DEFAULT 'Utilitaire',
                capacite_charge INTEGER,
                nb_places INTEGER DEFAULT 2,
                assurance_compagnie VARCHAR(255),
                assurance_numero VARCHAR(100),
                assurance_echeance DATE,
                controle_technique DATE,
                date_achat DATE,
                prix_achat DECIMAL(10,2),
                notes TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createRequestsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS requests (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT NOT NULL DEFAULT 'EN_ATTENTE',
                priority TEXT DEFAULT 'NORMALE',
                requester_name TEXT,
                requester_email TEXT,
                requester_phone TEXT,
                assigned_to TEXT,
                societe_id INTEGER,
                intervention_id INTEGER,
                estimated_cost REAL,
                actual_cost REAL,
                comments TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                validated_at TEXT,
                completed_at TEXT,
                FOREIGN KEY (societe_id) REFERENCES societes(id),
                FOREIGN KEY (intervention_id) REFERENCES interventions(id)
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createAffairesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS affaires (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                reference VARCHAR(20) UNIQUE NOT NULL,
                nom VARCHAR(255) NOT NULL,
                description TEXT,
                client_id INTEGER,
                client_nom VARCHAR(255),
                statut VARCHAR(50) DEFAULT 'PROSPECTION',
                type VARCHAR(50),
                priorite VARCHAR(20) DEFAULT 'NORMALE',
                montant_estime DECIMAL(10,2),
                montant_reel DECIMAL(10,2),
                taux_marge DECIMAL(5,2),
                devise_code VARCHAR(3) DEFAULT 'EUR',
                date_creation DATE NOT NULL,
                date_echeance DATE,
                date_fermeture DATE,
                derniere_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                commercial_responsable VARCHAR(255),
                technicien_responsable VARCHAR(255),
                chef_projet VARCHAR(255),
                notes TEXT,
                commentaires_internes TEXT,
                FOREIGN KEY (client_id) REFERENCES societes(id)
            )
            """;
        stmt.execute(sql);
    }
    
    private static void createDevisTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS devis (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                numero VARCHAR(20) UNIQUE NOT NULL,
                affaire_id INTEGER,
                client_id INTEGER NOT NULL,
                client_nom VARCHAR(255),
                objet VARCHAR(255) NOT NULL,
                description TEXT,
                statut VARCHAR(50) DEFAULT 'BROUILLON',
                version INTEGER DEFAULT 1,
                montant_ht DECIMAL(10,2),
                taux_tva DECIMAL(5,2) DEFAULT 20.0,
                montant_tva DECIMAL(10,2),
                montant_ttc DECIMAL(10,2),
                devise_code VARCHAR(3) DEFAULT 'EUR',
                date_creation DATE NOT NULL,
                date_validite DATE,
                date_acceptation DATE,
                derniere_mise_a_jour TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                conditions_paiement TEXT,
                delai_livraison VARCHAR(255),
                validite VARCHAR(255),
                modalites_livraison TEXT,
                commercial_redacteur VARCHAR(255),
                validateur VARCHAR(255),
                FOREIGN KEY (affaire_id) REFERENCES affaires(id),
                FOREIGN KEY (client_id) REFERENCES societes(id)
            )
            """;
        stmt.execute(sql);
    }
    
    /**
     * Ferme proprement H2
     */
    public static synchronized void shutdown() {
        try {
            if (dataSource != null) {
                AppLogger.info("🔄 Fermeture du pool de connexions H2...");
                dataSource.close();
            }
            
            initialized.set(false);
            AppLogger.info("✅ H2 Database fermé proprement");
            
        } catch (Exception e) {
            AppLogger.error("Erreur lors de la fermeture H2", e);
        }
    }
    
    /**
     * Crée la table techniciens
     */
    private static void createTechniciensTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS techniciens (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                nom TEXT NOT NULL,
                prenom TEXT NOT NULL,
                email TEXT UNIQUE,
                telephone TEXT,
                specialites TEXT,
                statut TEXT NOT NULL DEFAULT 'ACTIF',
                notes TEXT,
                google_contact_id TEXT,
                google_calendar_id TEXT,
                sync_google_enabled BOOLEAN DEFAULT FALSE,
                last_google_sync TEXT,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                date_modification TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }
    
    /**
     * Crée la table planifications
     */
    private static void createPlanificationsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS planifications (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                intervention_id INTEGER NOT NULL,
                technicien_id INTEGER,
                vehicule_id INTEGER,
                client_id INTEGER,
                date_planifiee TEXT NOT NULL,
                duree_estimee INTEGER NOT NULL DEFAULT 60,
                statut TEXT NOT NULL DEFAULT 'PLANIFIE',
                priorite TEXT NOT NULL DEFAULT 'NORMALE',
                type_intervention TEXT NOT NULL DEFAULT 'MAINTENANCE',
                lieu_intervention TEXT,
                coordonnees_gps TEXT,
                equipements_requis TEXT,
                notes_planification TEXT,
                date_debut_reel TEXT,
                date_fin_reel TEXT,
                commentaires_execution TEXT,
                google_event_id TEXT,
                google_meet_url TEXT,
                sync_google_calendar BOOLEAN DEFAULT FALSE,
                notification_client_email BOOLEAN DEFAULT TRUE,
                notification_technicien_email BOOLEAN DEFAULT TRUE,
                email_reminder_sent BOOLEAN DEFAULT FALSE,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                date_modification TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (intervention_id) REFERENCES interventions(id),
                FOREIGN KEY (technicien_id) REFERENCES techniciens(id),
                FOREIGN KEY (vehicule_id) REFERENCES vehicules(id),
                FOREIGN KEY (client_id) REFERENCES societes(id)
            )
            """;
        stmt.execute(sql);
    }
    
    /**
     * Crée la table sav_history
     */
    private static void createSavHistoryTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sav_history (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                produit_id INTEGER,
                sav_externe_id INTEGER,
                date_debut TEXT,
                date_fin TEXT,
                statut_historique TEXT,
                notes_historique TEXT,
                date_creation TEXT DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }

    private static void createRequestItemsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS request_items (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                request_id INTEGER NOT NULL,
                item_type TEXT NOT NULL,
                reference TEXT,
                name TEXT NOT NULL,
                description TEXT,
                quantity INTEGER NOT NULL,
                unit_price REAL,
                total_price REAL,
                supplier_id INTEGER,
                status TEXT,
                notes TEXT,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (request_id) REFERENCES requests(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createLignesDevisTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS lignes_devis (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                devis_id INTEGER NOT NULL,
                ordre INTEGER,
                designation TEXT NOT NULL,
                description TEXT,
                reference TEXT,
                unite TEXT,
                quantite REAL NOT NULL,
                prix_unitaire_ht REAL NOT NULL,
                taux_remise REAL,
                montant_remise REAL,
                montant_ht REAL NOT NULL,
                FOREIGN KEY (devis_id) REFERENCES devis(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createCommandesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS commandes (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                numero_commande TEXT NOT NULL,
                fournisseur_id INTEGER,
                statut TEXT NOT NULL,
                date_commande TEXT NOT NULL,
                date_livraison_prevue TEXT,
                date_livraison_reelle TEXT,
                montant_ht REAL NOT NULL,
                montant_tva REAL NOT NULL,
                montant_ttc REAL NOT NULL,
                conditions_paiement TEXT,
                adresse_livraison TEXT,
                notes_commande TEXT,
                bon_commande_path TEXT,
                bon_livraison_path TEXT,
                facture_path TEXT,
                gmail_thread_id TEXT,
                google_drive_folder_id TEXT,
                notification_livraison BOOLEAN,
                email_suivi_envoye BOOLEAN,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                date_modification TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }

    private static void createLignesCommandesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS lignes_commandes (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                commande_id INTEGER NOT NULL,
                produit_id INTEGER,
                reference_fournisseur TEXT,
                designation TEXT NOT NULL,
                quantite_commandee INTEGER NOT NULL,
                quantite_recue INTEGER NOT NULL DEFAULT 0,
                prix_unitaire_ht REAL NOT NULL,
                taux_tva REAL NOT NULL,
                montant_ligne_ht REAL NOT NULL,
                montant_ligne_ttc REAL NOT NULL,
                date_reception_prevue TEXT,
                date_reception_reelle TEXT,
                statut_ligne TEXT NOT NULL,
                notes_ligne TEXT,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (commande_id) REFERENCES commandes(id),
                FOREIGN KEY (produit_id) REFERENCES produits(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createMouvementsStockTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS mouvements_stock (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                produit_id INTEGER NOT NULL,
                commande_id INTEGER,
                ligne_commande_id INTEGER,
                type_mouvement TEXT NOT NULL,
                quantite INTEGER NOT NULL,
                stock_avant INTEGER NOT NULL,
                stock_après INTEGER NOT NULL,
                cout_unitaire REAL,
                valeur_mouvement REAL,
                motif TEXT,
                reference_document TEXT,
                utilisateur TEXT,
                date_mouvement TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (produit_id) REFERENCES produits(id),
                FOREIGN KEY (commande_id) REFERENCES commandes(id),
                FOREIGN KEY (ligne_commande_id) REFERENCES lignes_commandes(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createAlertesStockTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS alertes_stock (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                produit_id INTEGER NOT NULL,
                type_alerte TEXT NOT NULL,
                seuil_alerte INTEGER NOT NULL,
                stock_actuel INTEGER NOT NULL,
                statut_alerte TEXT NOT NULL,
                date_alerte TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                date_traitement TEXT,
                action_prise TEXT,
                notification_envoyee BOOLEAN DEFAULT false,
                email_notification_sent BOOLEAN DEFAULT false,
                FOREIGN KEY (produit_id) REFERENCES produits(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createDisponibilitesTechniciensTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS disponibilites_techniciens (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                technicien_id INTEGER NOT NULL,
                date_debut TEXT NOT NULL,
                date_fin TEXT NOT NULL,
                type_indispo TEXT NOT NULL,
                motif TEXT,
                google_event_id TEXT,
                sync_google_calendar BOOLEAN DEFAULT false,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (technicien_id) REFERENCES techniciens(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createCommunicationsTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS communications (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                planification_id INTEGER,
                intervention_id INTEGER,
                client_id INTEGER,
                technicien_id INTEGER,
                type_communication TEXT NOT NULL,
                statut TEXT NOT NULL,
                objet TEXT,
                contenu TEXT,
                destinataires TEXT,
                gmail_thread_id TEXT,
                gmail_message_id TEXT,
                google_meet_url TEXT,
                date_envoi TEXT,
                date_lecture TEXT,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (planification_id) REFERENCES planifications(id),
                FOREIGN KEY (intervention_id) REFERENCES interventions(id),
                FOREIGN KEY (client_id) REFERENCES societes(id),
                FOREIGN KEY (technicien_id) REFERENCES techniciens(id)
            )
            """;
        stmt.execute(sql);
    }

    private static void createEmailTemplatesTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS email_templates (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                nom_template TEXT NOT NULL,
                type_template TEXT NOT NULL,
                objet TEXT NOT NULL,
                contenu_html TEXT NOT NULL,
                contenu_text TEXT,
                variables_disponibles TEXT,
                actif BOOLEAN DEFAULT true,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                date_modification TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }

    private static void createConfigurationGoogleTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS configuration_google (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                nom TEXT NOT NULL,
                client_id TEXT NOT NULL,
                client_secret TEXT NOT NULL,
                redirect_uri TEXT,
                scopes TEXT,
                access_token TEXT,
                refresh_token TEXT,
                token_type TEXT,
                token_expires_in INTEGER,
                calendrier_principal TEXT,
                sync_calendar_actif INTEGER DEFAULT 0,
                intervalle_sync INTEGER DEFAULT 15,
                gmail_actif INTEGER DEFAULT 0,
                email_expediteur TEXT,
                signature_email TEXT,
                contacts_actif INTEGER DEFAULT 0,
                sync_contacts_auto INTEGER DEFAULT 0,
                date_creation TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                date_modification TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                actif INTEGER DEFAULT 1
            )
            """;
        stmt.execute(sql);
    }

    private static void createSyncHistoryTable(Statement stmt) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS sync_history (
                id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
                service_type TEXT NOT NULL,
                sync_type TEXT NOT NULL,
                status TEXT NOT NULL,
                items_synchronized INTEGER,
                items_failed INTEGER,
                error_details TEXT,
                sync_duration INTEGER,
                date_sync TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;
        stmt.execute(sql);
    }

    /**
     * Obtient des informations sur la base de données
     */
    public static String getDatabaseInfo() {
        if (!initialized.get()) {
            return "H2 Database non initialisé";
        }
        
        try (Connection conn = getConnection()) {
            return String.format("H2 Database - URL: %s", 
                conn.getMetaData().getURL());
        } catch (SQLException e) {
            return "Erreur lors de la récupération des informations: " + e.getMessage();
        }
    }
}