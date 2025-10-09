# MAGSAV - Documentation Complète

## Table des Matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Installation et Déploiement](#installation-et-déploiement)
4. [Système d'Authentification et Rôles](#système-dauthentification-et-rôles)
5. [API REST](#api-rest)
6. [Gestion des Produits](#gestion-des-produits)
7. [Système de Partage](#système-de-partage)
8. [Service Email](#service-email)
9. [Guide d'Intégration](#guide-dintégration)
10. [Manuel Utilisateur](#manuel-utilisateur)
11. [Changelog](#changelog)

---

## Vue d'ensemble

MAGSAV est une application de gestion d'inventaire développée en JavaFX avec une architecture moderne incluant une API REST, un système d'authentification multi-rôles et des fonctionnalités avancées de gestion des produits et interventions.

### Fonctionnalités Principales

- **Gestion d'inventaire complète** : Produits, catégories, fabricants, fournisseurs
- **Système d'interventions** : Suivi des demandes et interventions techniques
- **Authentification multi-rôles** : Admin, Technicien Mag Scène, Intermittent
- **API REST complète** : Accès programmatique à toutes les fonctionnalités
- **Système de partage** : Partage de produits et données via QR codes
- **Notifications email** : Alertes automatiques et notifications
- **Interface desktop moderne** : JavaFX avec design contemporain

---

## Architecture

### Stack Technique

- **Frontend Desktop** : JavaFX 21
- **Backend** : Java 21 + Jetty Server
- **Base de données** : SQLite
- **API REST** : Jakarta Servlet + Jackson JSON
- **Authentification** : JWT (JSON Web Tokens)
- **Build** : Gradle 8.x

### Structure du Projet

```
MAGSAV-1.2/
├── src/
│   ├── main/
│   │   ├── java/com/magsav/
│   │   │   ├── api/              # API REST et servlets
│   │   │   ├── db/               # Gestion base de données
│   │   │   ├── gui/              # Contrôleurs JavaFX
│   │   │   ├── model/            # Modèles de données
│   │   │   ├── repo/             # Repositories (accès données)
│   │   │   ├── service/          # Services métier
│   │   │   └── util/             # Utilitaires
│   │   └── resources/
│   │       ├── fxml/             # Fichiers FXML
│   │       └── css/              # Styles CSS
│   └── test/                     # Tests unitaires
├── data/                         # Base de données SQLite
└── docs/                         # Documentation technique
```

### Architecture 3-Tiers

1. **Présentation** : JavaFX Controllers + FXML + CSS
2. **Métier** : Services + Repositories + Models
3. **Données** : SQLite + API REST

---

## Installation et Déploiement

### Prérequis

- Java 21+ (OpenJDK recommandé)
- Gradle 8.x (wrapper inclus)
- SQLite 3.x

### Installation

1. **Cloner le projet**
```bash
git clone <repository_url>
cd MAGSAV-1.2
```

2. **Compilation**
```bash
./gradlew clean build
```

3. **Lancement de l'application desktop**
```bash
./gradlew run
```

4. **Lancement du serveur API**
```bash
./gradlew run -PmainClass=com.magsav.api.ApiServer
```

### Configuration

#### Base de données
- Fichier : `data/MAGSAV.db`
- Création automatique au premier lancement
- Migrations automatiques via `DatabaseManager`

#### API Server
- Port par défaut : `8080`
- Configuration dans `ApiServer.java`
- CORS activé pour développement

#### Email (optionnel)
- Configuration SMTP dans `EmailService`
- Support Gmail, Outlook, serveurs personnalisés

---

## Système d'Authentification et Rôles

### Architecture des Rôles

#### 1. INTERMITTENT (Priorité: 1)
- **Label** : "Intermittent"
- **Droits** : Visualisation seule + demande d'élévation de privilèges
- **Cas d'usage** : Personnel temporaire, stagiaires

#### 2. TECHNICIEN_MAG_SCENE (Priorité: 2)
- **Label** : "Technicien Mag Scène"
- **Droits** : Visualisation complète + demandes d'intervention/pièces/matériel
- **Cas d'usage** : Techniciens opérationnels

#### 3. ADMIN (Priorité: 3)
- **Label** : "Administrateur"
- **Droits** : Accès complet application + API + gestion utilisateurs
- **Cas d'usage** : Gestionnaires, superviseurs

### Système d'Élévation de Privilèges

Les utilisateurs **INTERMITTENT** peuvent demander une promotion temporaire vers **TECHNICIEN_MAG_SCENE** :

1. Création de demande avec justification
2. Validation par un **ADMIN**
3. Élévation temporaire (7 jours par défaut)
4. Retour automatique au rôle initial

### API Authentification

```bash
# Connexion
POST /auth/login
{
  "username": "admin",
  "password": "password"
}

# Réponse
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "fullName": "Administrateur"
  }
}
```

---

## API REST

### Base URL
`http://localhost:8080`

### Endpoints Principaux

#### Authentification
- `POST /auth/login` - Connexion
- `POST /auth/logout` - Déconnexion
- `GET /auth/me` - Profil utilisateur

#### Élévation de Privilèges
- `GET /elevation/en-attente` - Demandes en attente (Admin)
- `POST /elevation` - Créer demande (Intermittent)
- `PUT /elevation/{id}/approuver` - Approuver (Admin)
- `PUT /elevation/{id}/rejeter` - Rejeter (Admin)

#### Demandes d'Intervention
- `GET /demandes` - Lister les demandes
- `POST /demandes` - Créer une demande
- `PUT /demandes/{id}` - Modifier une demande
- `DELETE /demandes/{id}` - Supprimer une demande

#### Propriétaires
- `GET /proprietaires` - Lister les propriétaires
- `POST /proprietaires` - Créer un propriétaire
- `PUT /proprietaires/{id}` - Modifier un propriétaire

### Authentification JWT

Toutes les requêtes API (sauf login) nécessitent un header :
```
Authorization: Bearer <jwt_token>
```

### Codes de Réponse

- `200 OK` - Succès
- `201 Created` - Ressource créée
- `400 Bad Request` - Données invalides
- `401 Unauthorized` - Non authentifié
- `403 Forbidden` - Accès refusé
- `404 Not Found` - Ressource introuvable
- `409 Conflict` - Conflit de données
- `500 Internal Server Error` - Erreur serveur

---

## Gestion des Produits

### Modèle de Données

#### Produit
```java
public class Product {
    private String uid;           // Identifiant unique (format: ABC1234)
    private String name;          // Nom du produit
    private String category;      // Catégorie
    private String manufacturer;  // Fabricant
    private String supplier;      // Fournisseur
    private ProductSituation situation; // Situation actuelle
    private String location;      // Localisation
    private String notes;         // Notes
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### Situations de Produits
- `EN_STOCK` - Produit disponible
- `EN_INTERVENTION` - En cours de réparation
- `PRET` - Prêté/loué
- `HORS_SERVICE` - Non fonctionnel
- `ARCHIVE` - Archivé

### Génération d'UID

Format : `ABC1234` (3 lettres + 4 chiffres)
- Génération automatique
- Unicité garantie
- QR Code associé pour chaque produit

### Fonctionnalités

- **Recherche avancée** : Nom, catégorie, fabricant, UID
- **Filtrage** : Par situation, catégorie, fabricant
- **Import/Export** : CSV, Excel
- **Historique** : Traçabilité des modifications
- **Médias** : Photos, logos, documents

---

## Système de Partage

### Vue d'ensemble

Le système de partage permet de générer des liens temporaires pour partager des informations sur les produits, interventions ou listes d'équipements.

### Fonctionnalités

#### 1. Partage de Produits
```java
// Génération d'un lien de partage
ShareToken token = shareService.createProductShareToken(productId, "EQUIPEMENT", 48);
String shareUrl = "http://localhost:8080/share/" + token.getToken();
```

#### 2. Partage d'Interventions
```java
// Partage d'une intervention
ShareToken token = shareService.createInterventionShareToken(interventionId, 24);
```

#### 3. QR Codes
- Génération automatique pour chaque partage
- Format PNG, taille configurable
- Intégration dans les rapports PDF

### Configuration
- **Durée par défaut** : 48 heures
- **Formats supportés** : JSON, HTML, PDF
- **Sécurité** : Tokens uniques, expiration automatique

---

## Service Email

### Configuration SMTP

```java
// Configuration Gmail
EmailConfig gmailConfig = new EmailConfig(
    "smtp.gmail.com", 587, true,
    "votre-email@gmail.com", "mot-de-passe-app"
);

// Configuration Outlook
EmailConfig outlookConfig = new EmailConfig(
    "smtp-mail.outlook.com", 587, true,
    "votre-email@outlook.com", "votre-mot-de-passe"
);
```

### Types de Notifications

1. **Interventions**
   - Nouvelle demande
   - Changement de statut
   - Rappels d'échéance

2. **Élévation de Privilèges**
   - Nouvelle demande (→ Admins)
   - Approbation/Rejet (→ Demandeur)

3. **Système**
   - Alertes de maintenance
   - Rapports périodiques

### Templates Email

Templates HTML personnalisables dans `resources/email-templates/`:
- `intervention-notification.html`
- `privilege-elevation.html`
- `system-alert.html`

---

## Guide d'Intégration

### Intégration API

#### 1. Authentification
```javascript
// Connexion
const response = await fetch('http://localhost:8080/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'password' })
});
const { token } = await response.json();

// Utilisation du token
const apiResponse = await fetch('http://localhost:8080/demandes', {
    headers: { 'Authorization': `Bearer ${token}` }
});
```

#### 2. Gestion des Erreurs
```javascript
// Gestion centralisée des erreurs
async function apiCall(url, options = {}) {
    try {
        const response = await fetch(url, {
            ...options,
            headers: {
                'Authorization': `Bearer ${getToken()}`,
                'Content-Type': 'application/json',
                ...options.headers
            }
        });
        
        if (!response.ok) {
            throw new Error(`API Error: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('API Call failed:', error);
        throw error;
    }
}
```

### Intégration Base de Données

#### Connexion Directe SQLite
```java
// Repository pattern
public class CustomRepository {
    public List<Product> findProducts() {
        try (Connection conn = DB.getConnection()) {
            // Votre requête SQL
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
    }
}
```

### Extensibilité

#### 1. Nouveaux Services
```java
@Service
public class CustomService {
    private final CustomRepository repository;
    
    public CustomService() {
        this.repository = new CustomRepository();
    }
    
    // Vos méthodes métier
}
```

#### 2. Nouveaux Endpoints
```java
public class CustomServlet extends HttpServlet {
    private final ObjectMapper objectMapper;
    private final CustomService service;
    
    // Implémentation doGet, doPost, etc.
}
```

---

## Manuel Utilisateur

### Interface Principale

#### Connexion
1. Lancer l'application MAGSAV
2. Saisir nom d'utilisateur et mot de passe
3. Cliquer sur "Se connecter"

#### Navigation
- **Menu principal** : Accès aux modules
- **Barre d'outils** : Actions rapides
- **Panneau latéral** : Filtres et recherche
- **Zone principale** : Contenu actuel

### Modules

#### 1. Gestion des Produits
- **Ajouter** : Bouton "+" → Formulaire produit
- **Modifier** : Double-clic sur produit → Édition
- **Rechercher** : Barre de recherche + filtres
- **Exporter** : Menu "Fichier" → "Exporter"

#### 2. Interventions
- **Nouvelle intervention** : Menu "Interventions" → "Nouvelle"
- **Suivi** : Liste des interventions en cours
- **Historique** : Interventions terminées
- **Rapports** : Génération PDF/Excel

#### 3. Administration (Admin uniquement)
- **Utilisateurs** : Gestion des comptes
- **Paramètres** : Configuration système
- **Sauvegarde** : Export/Import données
- **Logs** : Journaux d'activité

### Raccourcis Clavier

- `Ctrl+N` - Nouveau produit/intervention
- `Ctrl+S` - Sauvegarder
- `Ctrl+F` - Rechercher
- `Ctrl+P` - Imprimer
- `F5` - Actualiser
- `Esc` - Fermer fenêtre/dialogue

### Conseils d'Utilisation

1. **Sauvegarde régulière** : Exporter les données périodiquement
2. **Permissions** : Respecter les niveaux d'accès par rôle
3. **Performance** : Utiliser les filtres pour grandes listes
4. **Support** : Consulter les logs en cas de problème

---

## Changelog

### Version 1.2.0 (Octobre 2024)

#### Nouvelles Fonctionnalités
- ✅ Système d'authentification multi-rôles (Admin, Technicien, Intermittent)
- ✅ API d'élévation de privilèges
- ✅ Service de partage avec QR codes
- ✅ Notifications email automatiques
- ✅ API REST complète avec JWT
- ✅ Gestion avancée des situations de produits

#### Améliorations
- 🔧 Refactorisation complète de l'architecture
- 🔧 Optimisation des performances base de données
- 🔧 Interface utilisateur modernisée
- 🔧 Documentation complète unifiée
- 🔧 Tests unitaires étendus

#### Corrections
- 🐛 Correction des doublons de servlets
- 🐛 Nettoyage des fichiers orphelins
- 🐛 Optimisation des requêtes SQL
- 🐛 Gestion d'erreurs améliorée

#### Migration depuis v1.1
1. Sauvegarde de la base de données existante
2. Mise à jour automatique du schéma au démarrage
3. Migration des utilisateurs vers nouveau système de rôles
4. Vérification des permissions d'accès

### Version 1.1.0 (Septembre 2024)

#### Fonctionnalités
- Gestion basique des produits et interventions
- Interface JavaFX simple
- Base de données SQLite
- Système d'authentification basique

---

## Support et Maintenance

### Logs et Débogage

#### Localisation des Logs
- **Application** : Console JavaFX
- **API** : Jetty server logs
- **Base de données** : SQLite logs
- **Système** : `logs/magsav.log`

#### Niveaux de Log
- `ERROR` - Erreurs critiques
- `WARN` - Avertissements
- `INFO` - Informations générales
- `DEBUG` - Débogage détaillé

### Maintenance Préventive

#### Quotidienne
- Vérification des logs d'erreur
- Monitoring des performances API
- Sauvegarde base de données

#### Hebdomadaire
- Nettoyage des tokens expirés
- Vérification intégrité données
- Mise à jour documentation

#### Mensuelle
- Optimisation base de données
- Review des permissions utilisateurs
- Tests de sécurité

### Contact Support

- **Documentation** : Ce fichier
- **Code source** : Commentaires dans le code
- **Issues** : GitHub issues (si applicable)
- **Email** : Configuration dans EmailService

---

## Annexes

### Configuration Avancée

#### Variables d'Environnement
```bash
# Optionnel - Configuration base de données
MAGSAV_DB_PATH=/custom/path/to/database.db

# Optionnel - Configuration API
MAGSAV_API_PORT=8080
MAGSAV_JWT_SECRET=your-secret-key

# Optionnel - Configuration Email
MAGSAV_SMTP_HOST=smtp.example.com
MAGSAV_SMTP_PORT=587
MAGSAV_SMTP_USER=user@example.com
MAGSAV_SMTP_PASSWORD=password
```

#### Personnalisation UI
- **Thèmes** : Modifier les fichiers CSS dans `resources/css/`
- **FXML** : Adapter les layouts dans `resources/fxml/`
- **Images** : Remplacer les assets dans `resources/images/`

### Schéma Base de Données

#### Tables Principales
- `produits` - Inventaire des produits
- `interventions` - Demandes et interventions
- `users` - Utilisateurs et authentification
- `demandes_elevation_privilege` - Système d'élévation
- `categories` - Catégories de produits
- `manufacturers` - Fabricants
- `share_tokens` - Tokens de partage

#### Relations
- `produits` ←→ `interventions` (1:N)
- `users` ←→ `demandes_elevation_privilege` (1:N)
- `categories` ←→ `produits` (1:N)
- `manufacturers` ←→ `produits` (1:N)

---

*Document généré automatiquement - Version 1.2.0 - Octobre 2024*