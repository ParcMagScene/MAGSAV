package com.magsav.db;

import com.magsav.exception.DatabaseException;
import java.io.IOException;
import java.nio.file.*;
import java.sql.*;

public final class DB {
  private static String URL;

  static { try { Class.forName("org.sqlite.JDBC"); } catch (Throwable ignore) {} }

  public static synchronized void init() {
    // Support des tests avec base en mémoire - vérifier la propriété à chaque fois
    String dbUrl = System.getProperty("magsav.db.url");
    if (dbUrl != null && !dbUrl.isEmpty()) {
      // Pour les tests, toujours utiliser l'URL spécifiée dans les propriétés système
      URL = dbUrl;
    } else if (URL == null || dbUrl == null) {
      // Utiliser une base de données locale dans le dossier du projet
      Path projectDir = Paths.get("").toAbsolutePath(); // Dossier courant du projet
      Path dataDir = projectDir.resolve("data");
      Path db = dataDir.resolve("MAGSAV.db");
      try { Files.createDirectories(dataDir); } catch (Exception ignore) {}
      URL = "jdbc:sqlite:" + db.toAbsolutePath();
    }
    ensureSchema();
  }

  public static synchronized void initForProduction() {
    // Force l'utilisation de la base de données de production
    System.clearProperty("magsav.db.url");
    URL = null;
    init();
  }

  public static synchronized void resetForTesting() {
    URL = null;
  }

  public static String getCurrentUrl() {
    return URL;
  }

  public static void diagnose() {
    System.out.println("=== DIAGNOSTIC BASE DE DONNÉES ===");
    System.out.println("URL actuelle: " + URL);
    System.out.println("Propriété système: " + System.getProperty("magsav.db.url"));
    try (Connection c = getConnection()) {
      System.out.println("Connexion: OK");
      try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM produits")) {
        if (rs.next()) {
          System.out.println("Produits dans la DB: " + rs.getInt(1));
        }
      }
    } catch (SQLException e) {
      System.out.println("Erreur connexion: " + e.getMessage());
    }
    System.out.println("=====================================");
  }

  public static Connection getConnection() throws SQLException {
    if (URL == null) init();
    return DriverManager.getConnection(URL);
  }

  public static Path dataDir() {
    // Utiliser un dossier medias local dans le projet
    Path projectDir = Paths.get("").toAbsolutePath();
    Path p = projectDir.resolve("medias");
    try {
      Files.createDirectories(p);
    } catch (IOException e) {
      throw new RuntimeException("Impossible de créer le dossier de données", e);
    }
    return p;
  }

  private static void ensureSchema() {
    try (Connection c = getConnection(); Statement st = c.createStatement()) {
      st.execute("""
        CREATE TABLE IF NOT EXISTS produits(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          code_produit TEXT, nom_produit TEXT, numero_serie TEXT,
          nom_fabricant TEXT, fabricant_id INTEGER,
          uid_unique TEXT, statut_produit TEXT,
          photo_produit TEXT, categorie_principale TEXT, sous_categorie TEXT,
          description_produit TEXT,
          date_achat TEXT,
          nom_client TEXT,
          prix_achat TEXT,
          duree_garantie TEXT,
          sav_externe_id INTEGER
        )
      """);
      
      // Migrations automatiques supprimées - structure française complète
      st.execute("""
        CREATE TABLE IF NOT EXISTS societes(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          type_societe TEXT, nom_societe TEXT,
          email_societe TEXT, telephone_societe TEXT,
          adresse_societe TEXT, notes_societe TEXT,
          date_creation TEXT DEFAULT (datetime('now'))
        )
      """);
      st.execute("""
        CREATE TABLE IF NOT EXISTS interventions(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          produit_id INTEGER,
          statut_intervention TEXT, description_panne TEXT,
          numero_serie_intervention TEXT,
          note_client TEXT,
          description_defaut TEXT,
          detecteur_societe_id INTEGER,
          nom_detecteur TEXT,
          date_entree TEXT, date_sortie TEXT,
          type_proprietaire TEXT, proprietaire_societe_id INTEGER
        )
      """);
      st.execute("""
        CREATE TABLE IF NOT EXISTS categories(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          nom_categorie TEXT,
          parent_id INTEGER
        )
      """);
      
      st.execute("""
        CREATE TABLE IF NOT EXISTS sav_history(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          produit_id INTEGER,
          sav_externe_id INTEGER,
          date_debut TEXT,
          date_fin TEXT,
          statut_historique TEXT,
          notes_historique TEXT,
          date_creation TEXT DEFAULT (datetime('now'))
        )
      """);
      
      st.execute("""
        CREATE TABLE IF NOT EXISTS vehicules(
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          immatriculation TEXT NOT NULL UNIQUE,
          type_vehicule TEXT NOT NULL CHECK (type_vehicule IN ('VL','PL','SPL','REMORQUE','SCENE_MOBILE')),
          marque TEXT,
          modele TEXT,
          annee INTEGER,
          kilometrage INTEGER DEFAULT 0,
          statut TEXT DEFAULT 'DISPONIBLE' CHECK (statut IN ('DISPONIBLE','EN_SERVICE','MAINTENANCE','HORS_SERVICE')),
          location_externe BOOLEAN DEFAULT FALSE,
          notes TEXT,
          date_creation TEXT DEFAULT (datetime('now')),
          date_modification TEXT DEFAULT (datetime('now'))
        )
      """);

      // Tables pour le module Planning avec intégration Google
      st.execute("""
        CREATE TABLE IF NOT EXISTS techniciens (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          nom TEXT NOT NULL,
          prenom TEXT NOT NULL,
          email TEXT UNIQUE,
          telephone TEXT,
          specialites TEXT, -- JSON array des spécialités
          statut TEXT NOT NULL DEFAULT 'ACTIF', -- ACTIF, CONGE, INDISPONIBLE, INACTIF
          notes TEXT,
          -- Intégration Google
          google_contact_id TEXT, -- ID du contact Google
          google_calendar_id TEXT, -- ID du calendrier Google personnel
          sync_google_enabled BOOLEAN DEFAULT FALSE,
          last_google_sync TEXT,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          date_modification TEXT NOT NULL DEFAULT (datetime('now'))
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS planifications (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          intervention_id INTEGER NOT NULL,
          technicien_id INTEGER,
          vehicule_id INTEGER,
          client_id INTEGER, -- Référence vers les societes/clients
          date_planifiee TEXT NOT NULL, -- Format ISO: YYYY-MM-DD HH:MM:SS
          duree_estimee INTEGER NOT NULL DEFAULT 60, -- en minutes
          statut TEXT NOT NULL DEFAULT 'PLANIFIE', -- PLANIFIE, EN_COURS, TERMINE, ANNULE, REPORTE
          priorite TEXT NOT NULL DEFAULT 'NORMALE', -- URGENTE, HAUTE, NORMALE, BASSE
          type_intervention TEXT NOT NULL DEFAULT 'MAINTENANCE', -- MAINTENANCE, DEPANNAGE, INSTALLATION, CONTROLE
          lieu_intervention TEXT, -- adresse ou localisation
          coordonnees_gps TEXT, -- latitude,longitude pour navigation
          equipements_requis TEXT, -- JSON array des équipements nécessaires
          notes_planification TEXT,
          date_debut_reel TEXT, -- Heure réelle de début
          date_fin_reel TEXT, -- Heure réelle de fin
          commentaires_execution TEXT,
          -- Intégration Google
          google_event_id TEXT, -- ID de l'événement Google Calendar
          google_meet_url TEXT, -- Lien Meet si réunion virtuelle
          sync_google_calendar BOOLEAN DEFAULT FALSE,
          notification_client_email BOOLEAN DEFAULT TRUE,
          notification_technicien_email BOOLEAN DEFAULT TRUE,
          email_reminder_sent BOOLEAN DEFAULT FALSE,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          date_modification TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (intervention_id) REFERENCES interventions(id) ON DELETE CASCADE,
          FOREIGN KEY (technicien_id) REFERENCES techniciens(id) ON DELETE SET NULL,
          FOREIGN KEY (vehicule_id) REFERENCES vehicules(id) ON DELETE SET NULL,
          FOREIGN KEY (client_id) REFERENCES societes(id) ON DELETE SET NULL
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS disponibilites_techniciens (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          technicien_id INTEGER NOT NULL,
          date_debut TEXT NOT NULL, -- Format ISO: YYYY-MM-DD HH:MM:SS
          date_fin TEXT NOT NULL,
          type_indispo TEXT NOT NULL DEFAULT 'CONGE', -- CONGE, FORMATION, MALADIE, AUTRE
          motif TEXT,
          -- Intégration Google
          google_event_id TEXT, -- Synchronisé avec Google Calendar
          sync_google_calendar BOOLEAN DEFAULT FALSE,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (technicien_id) REFERENCES techniciens(id) ON DELETE CASCADE
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS communications (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          planification_id INTEGER,
          intervention_id INTEGER,
          client_id INTEGER,
          technicien_id INTEGER,
          type_communication TEXT NOT NULL, -- EMAIL, SMS, APPEL, REUNION
          statut TEXT NOT NULL DEFAULT 'PLANIFIE', -- PLANIFIE, ENVOYE, RECU, ECHEC
          objet TEXT,
          contenu TEXT,
          destinataires TEXT, -- JSON array des destinataires
          -- Intégration Google
          gmail_thread_id TEXT, -- ID du thread Gmail
          gmail_message_id TEXT, -- ID du message Gmail
          google_meet_url TEXT, -- Pour les réunions virtuelles
          date_envoi TEXT,
          date_lecture TEXT,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (planification_id) REFERENCES planifications(id) ON DELETE SET NULL,
          FOREIGN KEY (intervention_id) REFERENCES interventions(id) ON DELETE SET NULL,
          FOREIGN KEY (client_id) REFERENCES societes(id) ON DELETE SET NULL,
          FOREIGN KEY (technicien_id) REFERENCES techniciens(id) ON DELETE SET NULL
        )""");

      // Tables pour le module Commandes avec intégration Google
      st.execute("""
        CREATE TABLE IF NOT EXISTS commandes (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          numero_commande TEXT NOT NULL UNIQUE,
          fournisseur_id INTEGER,
          statut TEXT NOT NULL DEFAULT 'BROUILLON', -- BROUILLON, VALIDEE, ENVOYEE, RECUE, ANNULEE
          date_commande TEXT NOT NULL DEFAULT (datetime('now')),
          date_livraison_prevue TEXT,
          date_livraison_reelle TEXT,
          montant_ht REAL NOT NULL DEFAULT 0.0,
          montant_tva REAL NOT NULL DEFAULT 0.0,
          montant_ttc REAL NOT NULL DEFAULT 0.0,
          conditions_paiement TEXT,
          adresse_livraison TEXT,
          notes_commande TEXT,
          -- Documents et fichiers
          bon_commande_path TEXT, -- Chemin vers le fichier PDF
          bon_livraison_path TEXT,
          facture_path TEXT,
          -- Intégration Google
          gmail_thread_id TEXT, -- Thread d'emails avec le fournisseur
          google_drive_folder_id TEXT, -- Dossier Google Drive pour les documents
          notification_livraison BOOLEAN DEFAULT TRUE,
          email_suivi_envoye BOOLEAN DEFAULT FALSE,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          date_modification TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (fournisseur_id) REFERENCES societes(id) ON DELETE SET NULL
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS lignes_commandes (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          commande_id INTEGER NOT NULL,
          produit_id INTEGER,
          reference_fournisseur TEXT,
          designation TEXT NOT NULL,
          quantite_commandee INTEGER NOT NULL DEFAULT 1,
          quantite_recue INTEGER NOT NULL DEFAULT 0,
          prix_unitaire_ht REAL NOT NULL DEFAULT 0.0,
          taux_tva REAL NOT NULL DEFAULT 20.0,
          montant_ligne_ht REAL NOT NULL DEFAULT 0.0,
          montant_ligne_ttc REAL NOT NULL DEFAULT 0.0,
          date_reception_prevue TEXT,
          date_reception_reelle TEXT,
          statut_ligne TEXT NOT NULL DEFAULT 'COMMANDEE', -- COMMANDEE, PARTIELLE, RECUE, ANNULEE
          notes_ligne TEXT,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE CASCADE,
          FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE SET NULL
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS mouvements_stock (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          produit_id INTEGER NOT NULL,
          commande_id INTEGER,
          ligne_commande_id INTEGER,
          type_mouvement TEXT NOT NULL, -- ENTREE, SORTIE, AJUSTEMENT, INVENTAIRE
          quantite INTEGER NOT NULL,
          stock_avant INTEGER NOT NULL DEFAULT 0,
          stock_après INTEGER NOT NULL DEFAULT 0,
          cout_unitaire REAL,
          valeur_mouvement REAL,
          motif TEXT,
          reference_document TEXT, -- Numéro de bon, facture, etc.
          utilisateur TEXT,
          date_mouvement TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE CASCADE,
          FOREIGN KEY (commande_id) REFERENCES commandes(id) ON DELETE SET NULL,
          FOREIGN KEY (ligne_commande_id) REFERENCES lignes_commandes(id) ON DELETE SET NULL
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS alertes_stock (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          produit_id INTEGER NOT NULL,
          type_alerte TEXT NOT NULL, -- STOCK_BAS, RUPTURE, SURAPPROVISIONNEMENT
          seuil_alerte INTEGER NOT NULL,
          stock_actuel INTEGER NOT NULL,
          statut_alerte TEXT NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, TRAITEE, IGNOREE
          date_alerte TEXT NOT NULL DEFAULT (datetime('now')),
          date_traitement TEXT,
          action_prise TEXT,
          -- Notifications automatiques
          notification_envoyee BOOLEAN DEFAULT FALSE,
          email_notification_sent BOOLEAN DEFAULT FALSE,
          FOREIGN KEY (produit_id) REFERENCES produits(id) ON DELETE CASCADE
        )""");

      // Table de configuration pour l'intégration Google
      // Commentée car gérée par GoogleServicesConfigRepository
      /*st.execute("""
        CREATE TABLE IF NOT EXISTS configuration_google (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          service_type TEXT NOT NULL UNIQUE, -- CALENDAR, GMAIL, CONTACTS, DRIVE
          client_id TEXT,
          client_secret TEXT,
          refresh_token TEXT,
          access_token TEXT,
          token_expiry TEXT,
          scope TEXT, -- Permissions demandées
          enabled BOOLEAN DEFAULT FALSE,
          last_sync TEXT,
          sync_frequency INTEGER DEFAULT 15, -- minutes
          auto_sync BOOLEAN DEFAULT TRUE,
          error_message TEXT,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          date_modification TEXT NOT NULL DEFAULT (datetime('now'))
        )""");*/

      // Table pour l'historique de synchronisation
      st.execute("""
        CREATE TABLE IF NOT EXISTS sync_history (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          service_type TEXT NOT NULL, -- CALENDAR, GMAIL, CONTACTS, DRIVE
          sync_type TEXT NOT NULL, -- MANUAL, AUTO, SCHEDULED
          status TEXT NOT NULL, -- SUCCESS, ERROR, PARTIAL
          items_synchronized INTEGER DEFAULT 0,
          items_failed INTEGER DEFAULT 0,
          error_details TEXT,
          sync_duration INTEGER, -- en millisecondes
          date_sync TEXT NOT NULL DEFAULT (datetime('now'))
        )""");

      // Table des templates d'emails
      st.execute("""
        CREATE TABLE IF NOT EXISTS email_templates (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          nom_template TEXT NOT NULL UNIQUE,
          type_template TEXT NOT NULL, -- INTERVENTION_PLANIFIEE, LIVRAISON_PREVUE, RAPPEL, etc.
          objet TEXT NOT NULL,
          contenu_html TEXT NOT NULL,
          contenu_text TEXT,
          variables_disponibles TEXT, -- JSON des variables disponibles
          actif BOOLEAN DEFAULT TRUE,
          date_creation TEXT NOT NULL DEFAULT (datetime('now')),
          date_modification TEXT NOT NULL DEFAULT (datetime('now'))
        )""");

      // Tables pour le système de demandes unifié (intervention, pièces, matériel, SAV externes, devis, prix)
      st.execute("""
        CREATE TABLE IF NOT EXISTS requests (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          type TEXT NOT NULL, -- INTERVENTION, PIECES, MATERIEL, SAV_EXTERNE, DEVIS, PRIX
          title TEXT NOT NULL, -- Titre/résumé de la demande
          description TEXT, -- Description détaillée
          status TEXT NOT NULL DEFAULT 'EN_ATTENTE', -- EN_ATTENTE, EN_COURS, VALIDEE, REFUSEE, TERMINEE
          priority TEXT DEFAULT 'NORMALE', -- BASSE, NORMALE, HAUTE, URGENTE
          requester_name TEXT, -- Nom du demandeur (utilisateur)
          requester_email TEXT, -- Email du demandeur
          requester_phone TEXT, -- Téléphone du demandeur
          assigned_to TEXT, -- Technicien/Admin assigné
          societe_id INTEGER, -- Société liée (client, fournisseur, etc.)
          intervention_id INTEGER, -- Intervention liée si applicable
          estimated_cost REAL, -- Coût estimé
          actual_cost REAL, -- Coût réel
          comments TEXT, -- Commentaires internes
          created_at TEXT NOT NULL DEFAULT (datetime('now')),
          updated_at TEXT NOT NULL DEFAULT (datetime('now')),
          validated_at TEXT, -- Date de validation
          completed_at TEXT, -- Date de completion
          FOREIGN KEY (societe_id) REFERENCES societes(id),
          FOREIGN KEY (intervention_id) REFERENCES interventions(id)
        )""");

      st.execute("""
        CREATE TABLE IF NOT EXISTS request_items (
          id INTEGER PRIMARY KEY AUTOINCREMENT,
          request_id INTEGER NOT NULL,
          item_type TEXT NOT NULL, -- PIECE, MATERIEL, SERVICE
          reference TEXT, -- Référence produit/pièce
          name TEXT NOT NULL, -- Nom de l'élément
          description TEXT, -- Description détaillée
          quantity INTEGER NOT NULL DEFAULT 1,
          unit_price REAL, -- Prix unitaire
          total_price REAL, -- Prix total (quantity * unit_price)
          supplier_id INTEGER, -- Fournisseur suggéré/choisi
          status TEXT DEFAULT 'EN_ATTENTE', -- EN_ATTENTE, COMMANDE, RECU, INSTALLE
          notes TEXT, -- Notes spécifiques à cet item
          created_at TEXT NOT NULL DEFAULT (datetime('now')),
          updated_at TEXT NOT NULL DEFAULT (datetime('now')),
          FOREIGN KEY (request_id) REFERENCES requests(id),
          FOREIGN KEY (supplier_id) REFERENCES societes(id)
        )""");
      
      // Index pour optimiser les requêtes fréquentes
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_nom ON produits(UPPER(nom_produit))");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_uid ON produits(uid_unique)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_fabricant ON produits(nom_fabricant)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_category ON produits(categorie_principale)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_intervention_product ON interventions(produit_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_intervention_date_entree ON interventions(date_entree)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_societes_type ON societes(type_societe)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_prod_sav_externe ON produits(sav_externe_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_sav_history_product ON sav_history(produit_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_sav_history_sav_externe ON sav_history(sav_externe_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_vehicules_immatriculation ON vehicules(immatriculation)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_vehicules_statut ON vehicules(statut)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_vehicules_type ON vehicules(type_vehicule)");
      
      // Index pour les nouvelles tables Planning et Commandes
      st.execute("CREATE INDEX IF NOT EXISTS idx_techniciens_email ON techniciens(email)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_techniciens_statut ON techniciens(statut)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_techniciens_google_contact ON techniciens(google_contact_id)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_planifications_date ON planifications(date_planifiee)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_planifications_statut ON planifications(statut)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_planifications_technicien ON planifications(technicien_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_planifications_vehicule ON planifications(vehicule_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_planifications_intervention ON planifications(intervention_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_planifications_google_event ON planifications(google_event_id)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_disponibilites_technicien ON disponibilites_techniciens(technicien_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_disponibilites_date_debut ON disponibilites_techniciens(date_debut)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_disponibilites_date_fin ON disponibilites_techniciens(date_fin)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_communications_planification ON communications(planification_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_communications_type ON communications(type_communication)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_communications_statut ON communications(statut)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_communications_gmail_thread ON communications(gmail_thread_id)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_commandes_numero ON commandes(numero_commande)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_commandes_statut ON commandes(statut)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_commandes_fournisseur ON commandes(fournisseur_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_commandes_date ON commandes(date_commande)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_commandes_livraison ON commandes(date_livraison_prevue)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_lignes_commandes_commande ON lignes_commandes(commande_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_lignes_commandes_produit ON lignes_commandes(produit_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_lignes_commandes_statut ON lignes_commandes(statut_ligne)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_mouvements_stock_produit ON mouvements_stock(produit_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_mouvements_stock_date ON mouvements_stock(date_mouvement)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_mouvements_stock_type ON mouvements_stock(type_mouvement)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_alertes_stock_produit ON alertes_stock(produit_id)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_alertes_stock_statut ON alertes_stock(statut_alerte)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_alertes_stock_date ON alertes_stock(date_alerte)");
      
      // Index Google commenté car table gérée par GoogleServicesConfigRepository
      //st.execute("CREATE INDEX IF NOT EXISTS idx_config_google_service ON configuration_google(service_type)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_sync_history_service ON sync_history(service_type)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_sync_history_date ON sync_history(date_sync)");
      
      st.execute("CREATE INDEX IF NOT EXISTS idx_email_templates_type ON email_templates(type_template)");
      st.execute("CREATE INDEX IF NOT EXISTS idx_email_templates_actif ON email_templates(actif)");
      
      // Insertion des données de base si nécessaire
      insertDefaultData();
      
    } catch (SQLException e) { throw new DatabaseException("DB ensureSchema failed", e); }
  }
  
  /**
   * Insère les données par défaut nécessaires au fonctionnement de l'application
   */
  private static void insertDefaultData() throws SQLException {
    try (Connection connection = getConnection()) {
      // Templates d'emails par défaut
      insertDefaultEmailTemplates(connection);
      // Configuration Google par défaut - commenté car géré par GoogleServicesConfigRepository
      //insertDefaultGoogleConfig(connection);
    }
  }
  
  /**
   * Insère les templates d'emails par défaut
   */
  private static void insertDefaultEmailTemplates(Connection conn) throws SQLException {
    String checkTemplate = "SELECT COUNT(*) FROM email_templates WHERE nom_template = ?";
    String insertTemplate = """
      INSERT INTO email_templates (nom_template, type_template, objet, contenu_html, contenu_text, variables_disponibles)
      VALUES (?, ?, ?, ?, ?, ?)
    """;
    
    // Template pour intervention planifiée
    try (PreparedStatement check = conn.prepareStatement(checkTemplate)) {
      check.setString(1, "intervention_planifiee");
      ResultSet rs = check.executeQuery();
      if (rs.next() && rs.getInt(1) == 0) {
        try (PreparedStatement insert = conn.prepareStatement(insertTemplate)) {
          insert.setString(1, "intervention_planifiee");
          insert.setString(2, "INTERVENTION_PLANIFIEE");
          insert.setString(3, "Intervention planifiée - {{intervention.numero}} - {{date.planifiee}}");
          insert.setString(4, """
            <h2>Intervention Planifiée</h2>
            <p>Bonjour {{client.nom}},</p>
            <p>Nous avons planifié votre intervention :</p>
            <ul>
              <li><strong>Numéro :</strong> {{intervention.numero}}</li>
              <li><strong>Date :</strong> {{date.planifiee}}</li>
              <li><strong>Heure :</strong> {{heure.planifiee}}</li>
              <li><strong>Technicien :</strong> {{technicien.nom}} {{technicien.prenom}}</li>
              <li><strong>Lieu :</strong> {{lieu.intervention}}</li>
            </ul>
            <p>Cordialement,<br>L'équipe MAGSAV</p>
            """);
          insert.setString(5, "Intervention Planifiée\nBonjour {{client.nom}},\nNous avons planifié votre intervention le {{date.planifiee}} à {{heure.planifiee}}.\nTechnicien: {{technicien.nom}} {{technicien.prenom}}\nCordialement, L'équipe MAGSAV");
          insert.setString(6, "[\"client.nom\", \"intervention.numero\", \"date.planifiee\", \"heure.planifiee\", \"technicien.nom\", \"technicien.prenom\", \"lieu.intervention\"]");
          insert.executeUpdate();
        }
      }
    }
    
    // Template pour rappel d'intervention
    try (PreparedStatement check = conn.prepareStatement(checkTemplate)) {
      check.setString(1, "rappel_intervention");
      ResultSet rs = check.executeQuery();
      if (rs.next() && rs.getInt(1) == 0) {
        try (PreparedStatement insert = conn.prepareStatement(insertTemplate)) {
          insert.setString(1, "rappel_intervention");
          insert.setString(2, "RAPPEL_INTERVENTION");
          insert.setString(3, "Rappel - Intervention demain - {{intervention.numero}}");
          insert.setString(4, """
            <h2>Rappel d'Intervention</h2>
            <p>Bonjour {{client.nom}},</p>
            <p>Nous vous rappelons que votre intervention est prévue demain :</p>
            <ul>
              <li><strong>Date :</strong> {{date.planifiee}}</li>
              <li><strong>Heure :</strong> {{heure.planifiee}}</li>
              <li><strong>Technicien :</strong> {{technicien.nom}} {{technicien.prenom}}</li>
            </ul>
            <p>Merci de vous assurer d'être disponible.<br>L'équipe MAGSAV</p>
            """);
          insert.setString(5, "Rappel d'Intervention\nBonjour {{client.nom}},\nVotre intervention est prévue demain le {{date.planifiee}} à {{heure.planifiee}}.\nCordialement, L'équipe MAGSAV");
          insert.setString(6, "[\"client.nom\", \"intervention.numero\", \"date.planifiee\", \"heure.planifiee\", \"technicien.nom\", \"technicien.prenom\"]");
          insert.executeUpdate();
        }
      }
    }
    
    // Template pour livraison prévue
    try (PreparedStatement check = conn.prepareStatement(checkTemplate)) {
      check.setString(1, "livraison_prevue");
      ResultSet rs = check.executeQuery();
      if (rs.next() && rs.getInt(1) == 0) {
        try (PreparedStatement insert = conn.prepareStatement(insertTemplate)) {
          insert.setString(1, "livraison_prevue");
          insert.setString(2, "LIVRAISON_PREVUE");
          insert.setString(3, "Livraison prévue - Commande {{commande.numero}}");
          insert.setString(4, """
            <h2>Livraison Prévue</h2>
            <p>Bonjour,</p>
            <p>Votre commande {{commande.numero}} sera livrée le {{date.livraison}} à {{heure.livraison}}.</p>
            <p>Détails de la livraison :</p>
            <ul>
              <li><strong>Adresse :</strong> {{adresse.livraison}}</li>
              <li><strong>Référence :</strong> {{commande.numero}}</li>
            </ul>
            <p>Cordialement,<br>L'équipe MAGSAV</p>
            """);
          insert.setString(5, "Livraison Prévue\nVotre commande {{commande.numero}} sera livrée le {{date.livraison}}.\nCordialement, L'équipe MAGSAV");
          insert.setString(6, "[\"commande.numero\", \"date.livraison\", \"heure.livraison\", \"adresse.livraison\"]");
          insert.executeUpdate();
        }
      }
    }
  }
  
  /**
   * Insère la configuration Google par défaut
   * Commenté car géré maintenant par GoogleServicesConfigRepository
   */
  private static void insertDefaultGoogleConfig(Connection conn) throws SQLException {
    // Méthode commentée car gérée par GoogleServicesConfigRepository
    /*String checkConfig = "SELECT COUNT(*) FROM configuration_google WHERE service_type = ?";
    String insertConfig = """
      INSERT INTO configuration_google (service_type, enabled, scope, sync_frequency, auto_sync)
      VALUES (?, ?, ?, ?, ?)
    """;
    
    String[] services = {"CALENDAR", "GMAIL", "CONTACTS", "DRIVE"};
    String[] scopes = {
      "https://www.googleapis.com/auth/calendar",
      "https://www.googleapis.com/auth/gmail.modify",
      "https://www.googleapis.com/auth/contacts",
      "https://www.googleapis.com/auth/drive.file"
    };
    
    for (int i = 0; i < services.length; i++) {
      try (PreparedStatement check = conn.prepareStatement(checkConfig)) {
        check.setString(1, services[i]);
        ResultSet rs = check.executeQuery();
        if (rs.next() && rs.getInt(1) == 0) {
          try (PreparedStatement insert = conn.prepareStatement(insertConfig)) {
            insert.setString(1, services[i]);
            insert.setBoolean(2, false); // Désactivé par défaut
            insert.setString(3, scopes[i]);
            insert.setInt(4, 15); // Synchronisation toutes les 15 minutes
            insert.setBoolean(5, false); // Auto-sync désactivé par défaut
            insert.executeUpdate();
          }
        }
      }
    }*/
  }

  private DB() {}
}