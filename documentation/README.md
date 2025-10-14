# MAGSAV 1.2

Application de gestion SAV (Service Après-Vente) développée en JavaFX pour macOS.

## 🎯 Vue d'ensemble

**MAGSAV 1.2** est une application complète de gestion SAV conçue pour gérer efficacement l'inventaire, les interventions techniques et les relations clients-fournisseurs.

### Fonctionnalités principales
- **Gestion complète des produits** : Inventaire avec photos, logos, numéros de série
- **Suivi SAV intégral** : Interventions avec historique complet et notifications
- **Gestion des entités** : Clients, fournisseurs, fabricants avec autocomplétion d'adresse
- **Import CSV français** : Support des colonnes françaises avec validation
- **API REST complète** : Accès programmatique avec authentification JWT
- **Optimisation performance** : Métriques temps réel et maintenance automatique

### Points forts techniques
✨ **Interface native macOS** : Optimisée pour Apple Silicon  
🇫🇷 **100% français** : Interface, imports CSV, API adresse officielle  
⚡ **Performance monitoring** : Métriques automatiques et optimisations  
🔒 **Sécurité avancée** : Authentification JWT avec rôles granulaires  
🖼️ **Gestion média intelligente** : Scraping automatisé avec validation  

## 🏗️ Architecture technique

### Stack technologique
- **Frontend** : JavaFX 21 (native macOS)
- **Backend** : Java 21 + Jetty Server intégré  
- **Base de données** : SQLite avec optimisations automatiques
- **API REST** : Jakarta Servlet + Jackson JSON
- **Authentification** : JWT (JSON Web Tokens)
- **Build** : Gradle 8.10.x avec JDK 21
- **Logging** : SLF4J + Logback
- **Services externes** : API Adresse Data Gouv

### Structure du projet
```
src/main/java/com/magsav/
├── gui/           # Contrôleurs JavaFX et interfaces
│   ├── hub/       # Hubs de gestion centralisés
│   └── forms/     # Formulaires de saisie
├── service/       # Logique métier et services
├── repo/          # Accès données (Repository Pattern)
├── model/         # Entités et modèles
├── imports/       # Import CSV français
├── util/          # Classes utilitaires
└── db/            # Configuration base de données
```

### Architecture en couches
```
┌─────────────────────────────────────┐
│          JavaFX GUI Layer           │  ← Contrôleurs FXML
├─────────────────────────────────────┤
│         Service Layer               │  ← Logique métier
├─────────────────────────────────────┤
│       Repository Layer              │  ← Accès données
├─────────────────────────────────────┤
│        SQLite Database              │  ← Persistence optimisée
└─────────────────────────────────────┘
```

## 🚀 Démarrage rapide

### Prérequis
- **macOS** (Apple Silicon recommandé)
- **JDK 21** (AdoptOpenJDK ou Oracle)
- **Git** pour le versioning

### Installation
```bash
# Cloner le projet
git clone [repository-url]
cd MAGSAV-1.2

# Lancement direct
./gradlew run

# Build complet avec tests
./gradlew build

# Nettoyage
./gradlew clean
```

### Configuration
La base de données SQLite se crée automatiquement dans `~/MAGSAV/MAGSAV.db` avec :
- **Tables principales** : produits, interventions, societes
- **Tables système** : categories, users, metrics
- **Index optimisés** : 11 index automatiques pour les performances

## 🎨 Fonctionnalités détaillées

### 1. Gestion des produits
- **Inventaire complet** : Nom, numéro de série, UID unique, fabricant
- **Situations multiples** : En stock, Prêté, En réparation, Vendu, etc.
- **Média intégré** : Photos et logos avec validation automatique
- **Import CSV** : Support colonnes françaises (PRODUIT, N° DE SERIE, FABRICANT, SITUATION)

### 2. Système d'interventions  
- **Suivi SAV** : Statut, panne, dates d'entrée/sortie, détecteur
- **Historique complet** : Toutes les interventions liées à un produit
- **Notifications** : Système d'alertes pour les techniciens
- **Export données** : CSV et rapports personnalisés

### 3. Gestion des entités
- **Clients** : Informations de contact avec autocomplétion d'adresse
- **Fournisseurs** : Base de données fournisseurs avec historique  
- **Fabricants** : Catalogue constructeurs avec logos et informations

### 4. Interface utilisateur moderne
- **Onglets intelligents** : Navigation intuitive et responsive
- **Hiérarchie visuelle** : Catégories avec emojis et indentation
- **Préférences persistantes** : Sauvegarde automatique des configurations
- **Mise à jour temps réel** : Actualisation automatique des données

## 🗺️ Service d'autocomplétion d'adresse

Service intégré utilisant l'API gouvernementale française gratuite `api-adresse.data.gouv.fr`.

### Fonctionnalités
✨ **Autocomplétion progressive** : Suggestions à partir de 3 caractères  
🎯 **Validation officielle** : Données gouvernementales françaises  
🚀 **Recherche asynchrone** : Interface non-bloquante  
⚡ **Performance optimizée** : Maximum 8 résultats pour rapidité  

### Usage simple
```java
import com.magsav.util.AddressAutocompleteUtil;

// Pour n'importe quel champ d'adresse
AddressAutocompleteUtil.setupFor(monChampAdresse);
```

## ⚡ Optimisation et performance

### Système de métriques avancé
```java
DatabaseMetricsService {
    + recordQuery(operation, duration, success)
    + getGlobalMetrics() → QueryMetrics
    + getSlowestQueries(limit)
    + generateReport() → String
}
```

### Optimisations appliquées
- **Resource management** : 100% try-with-resources
- **Exception handling** : DatabaseException standardisée
- **Index automatiques** : 11 index recommandés appliqués
- **Query monitoring** : Temps d'exécution et détection de lenteur
- **Maintenance auto** : VACUUM, ANALYZE, vérification d'intégrité

## 🌐 API REST et authentification

### Endpoints principaux
```http
GET    /api/products          # Liste des produits
POST   /api/products          # Création produit
GET    /api/products/{id}     # Détail produit
PUT    /api/products/{id}     # Mise à jour
DELETE /api/products/{id}     # Suppression

GET    /api/interventions     # Liste interventions
POST   /api/interventions     # Nouvelle intervention
GET    /api/manufacturers     # Liste fabricants
POST   /api/auth/login        # Authentification
```

### Authentification JWT
- **Rôles multiples** : Admin, Technicien Mag Scène, Intermittent
- **Tokens sécurisés** : JWT avec expiration configurable
- **Permissions granulaires** : Accès par rôle et ressource

## 🖼️ Gestion des images et scraping

### Système intelligent
- **Sources multiples** : Google Images, Bing, APIs constructeurs
- **Validation automatique** : Format, taille, qualité des images
- **Organisation automatique** : Classement par fabricant et type
- **Cache optimisé** : Stockage local avec invalidation intelligente

## 📁 Base de données

**Emplacement** : `~/MAGSAV/MAGSAV.db`

### Tables principales
- `produits` : Inventaire des produits
- `interventions` : Historique SAV  
- `societes` : Clients/Fournisseurs/Fabricants unifiés
- `categories` : Classification produits
- `users` : Utilisateurs avec authentification
- `metrics` : Métriques de performance

## 👤 Manuel utilisateur

### Interface principale
1. **Hub central** : Accès rapide à toutes les fonctions
2. **Onglets intégrés** : Catégories, Médias, Produits, Interventions
3. **Recherche globale** : Filtre intelligent sur tous les critères
4. **Actions rapides** : Raccourcis clavier et boutons contextuels

### Workflows principaux

#### Création d'un produit
1. Cliquer "Nouveau Produit"
2. Remplir les informations (nom, SN, fabricant)
3. Sélectionner situation (auto-complétée)
4. Ajouter photos/logos (glisser-déposer)
5. Sauvegarder (UID généré automatiquement)

#### Import CSV
1. Préparer fichier avec colonnes françaises :
   - **PRODUIT**, **N° DE SERIE**, **FABRICANT**, **SITUATION**
   - **STATUS**, **PANNE**, **DATE ENTREE**, **DATE SORTIE**, **DETECTEUR**, **N° SUIVI**
2. Menu Import → Sélectionner fichier
3. Validation automatique des données
4. Confirmation et import

## 🧪 Tests et développement

### Tests
```bash
./gradlew test
```

### Compilation
```bash
./gradlew compileJava
```

### Distribution
```bash
./gradlew distZip
```

## 🚀 Déploiement et maintenance

### Déploiement
```bash
# Build de distribution
./gradlew distZip

# Extraction et lancement
unzip build/distributions/MAGSAV-1.2.zip
./bin/MAGSAV
```

### Maintenance automatique
- **Logs** : `~/MAGSAV/logs/magsav.log`
- **Métriques** : Interface admin intégrée
- **Backup** : Sauvegarde automatique quotidienne
- **Optimisation** : Index et VACUUM automatiques

## 📈 Changelog Version 1.2

### Optimisations majeures
- **Architecture** : Pattern BaseRepository standardisé
- **Performance** : +40% vitesse requêtes avec nouveaux index
- **Stabilité** : 0 memory leak, resources auto-fermées
- **Interface** : 100% française avec autocomplétion
- **API** : REST complète avec authentification JWT
- **Monitoring** : Métriques temps réel et rapports

### Corrections
- **Build** : Compilation sans erreurs ni warnings
- **Tests** : 123 tests stabilisés  
- **Code** : Imports nettoyés, exceptions standardisées
- **Base** : Schéma unifié, optimisations appliquées

## 📄 Documentation

- [Changelog complet](CHANGELOG.md)
- [Guide des données de test](DONNEES_TEST.md)
- [Rapports techniques](docs/)

## 📄 Licence

© 2025 - Projet MAGSAV