# 🎯 MAGSAV 3.0 - Système de Gestion SAV et Parc Matériel

**Application multi-plateforme pour Mag Scène**

![Statut](https://img.shields.io/badge/Statut-STABLE-green)
![Backend](https://img.shields.io/badge/Backend-OPÉRATIONNEL-green)
![Frontend](https://img.shields.io/badge/Frontend-OPÉRATIONNEL-green)
![Java](https://img.shields.io/badge/Java-21.0.8-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen)
![React](https://img.shields.io/badge/React-18.2.0-blue)

**Dernière validation:** 6 janvier 2026

---

## 📋 Table des matières

1. [Vue d'ensemble](#-vue-densemble)
2. [Architecture](#-architecture)
3. [Démarrage Rapide](#-démarrage-rapide)
4. [Installation](#-installation)
5. [Développement](#-développement)
6. [Fonctionnalités](#-fonctionnalités)
7. [Configuration](#-configuration)
8. [Scripts](#-scripts)
9. [Structure du Projet](#-structure-du-projet)
10. [Historique du Projet](#-historique-du-projet)

---

## 🎨 Vue d'ensemble

### Modules Métier

- **SAV**: Gestion des demandes d'intervention, réparations, RMA, historique complet
- **Parc Matériel**: Inventaire avec QR codes, catégories hiérarchiques, photos
- **Ventes & Installations**: Import PDF affaires, gestion projets et contrats
- **Fournisseurs**: Commandes groupées, demandes matériel, seuils automatiques
- **Véhicules**: Planning unifié, réservations, maintenance, entretiens, locations
- **Personnel**: Qualifications, permis, planning unifié, intermittents/freelances
- **Planning Global**: Vue unifiée personnel + véhicules, détection de conflits

### Stack Technique

- **Backend**: Spring Boot 3.4.13 + H2 Database + JWT Security
- **Frontend Web**: React 18 TypeScript (interface responsive)
- **Build**: Gradle 8.4 multi-module monorepo
- **Base**: Java 21.0.8, Node.js 18+

### Statistiques

- **Controllers**: 24
- **Endpoints REST**: 215+
- **Repositories JPA**: 23
- **Entités**: 23
- **Pages Frontend**: 11
- **Composants réutilisables**: 3

---

## 🏗️ Architecture

### Monorepo Gradle

```
MAGSAV-3.0/
├── backend/              # Spring Boot REST API + H2
│   ├── controller/       # 24 REST Controllers
│   ├── service/          # Logique métier
│   ├── repository/       # 23 JPA Repositories
│   ├── dto/              # Data Transfer Objects
│   └── entity/           # Entités JPA
├── web-frontend/         # React 18 TypeScript
│   ├── pages/            # 11 pages complètes
│   ├── components/       # Composants réutilisables
│   ├── services/         # Client API (215 endpoints)
│   └── types/            # Définitions TypeScript
├── common-models/        # Entités JPA partagées (23)
└── integration-tests/    # Tests E2E
```

### Frontend Web

**Pages principales**:
- Dashboard: Vue d'ensemble des indicateurs
- SAV: Demandes d'intervention, réparations, RMA
- Parc Matériel: Inventaire avec recherche globale
- Clients: Base clients (entreprises, associations, particuliers)
- Contrats: Maintenance, location, prestation, support
- Ventes & Installations: Projets et contrats
- Véhicules: Flotte + réservations
- Personnel: Qualifications, planning
- Planning Global: Vue unifiée
- Fournisseurs: Commandes groupées
- Paramètres: Configuration

**Composants réutilisables**:
- `DataTable`: Tableau avec tri, filtres, pagination
- `StatCard`: Cartes de statistiques
- `GlobalSearch`: Recherche globale dans le header (équipements, SAV, clients, véhicules)

---

## 🚀 Démarrage Rapide

### Option 1: Script PowerShell (Recommandé) ⭐

```powershell
.\scripts\start-dev.ps1

# Backend uniquement
.\scripts\start-dev.ps1 -BackendOnly

# Frontend uniquement
.\scripts\start-dev.ps1 -FrontendOnly

# Avec nettoyage préalable
.\scripts\start-dev.ps1 -Clean
```

### Option 2: Démarrage en 1 Commande

```powershell
# Démarre backend + frontend en parallèle
.\start-dev-full.ps1
```

### URLs d'Accès

| Service | URL | Description |
|---------|-----|-------------|
| 🌐 **Frontend** | http://localhost:3000 | Interface React |
| 🔧 **Backend** | http://localhost:8080 | API REST |
| 📊 **H2 Console** | http://localhost:8080/h2-console | Base de données |
| 📖 **Swagger** | http://localhost:8080/swagger-ui.html | Documentation API |
| ❤️ **Health** | http://localhost:8080/actuator/health | Health check |

### Base de Données H2

```
JDBC URL: jdbc:h2:file:./data/magsav
Username: sa
Password: (vide)
```

### Arrêt

```powershell
.\scripts\stop-dev.ps1
```

---

## 📦 Installation

#### Prérequis
- Java 21+ (OpenJDK recommandé)
- Node.js 18+
- Gradle 8.4 (wrapper inclus)

#### Étapes

**1. Cloner le projet**
```bash
git clone https://github.com/ParcMagScene/MAGSAV.git
cd MAGSAV-3.0
```

**2. Build complet**
```bash
./gradlew.bat clean build -x test
```

**3. Démarrer le Backend**
```bash
./gradlew.bat :backend:bootRun
```
Backend disponible sur: http://localhost:8080

**4. Démarrer le Frontend**
```bash
cd web-frontend
npm install
npm start
```
Frontend disponible sur: http://localhost:3000

**5. (Optionnel) Démarrer Desktop JavaFX**
```bash
./gradlew.bat :desktop-javafx:run
```

### 🔗 URLs Utiles

- 🌐 **Frontend React:** http://localhost:3000
- 🔧 **Backend API:** http://localhost:8080
- 📊 **Console H2:** http://localhost:8080/h2-console
- 📖 **API Swagger:** http://localhost:8080/swagger-ui.html

### 💾 Base de Données H2

**Paramètres de connexion:**
- **JDBC URL:** `jdbc:h2:file:~/magsav/data/magsav`
- **Username:** `sa`
- **Password:** `password`

### ⚡ Statut du Projet

**Dernière validation:** 6 janvier 2026
- ✅ Backend: OPÉRATIONNEL (Java 21.0.8)
- ✅ Frontend: OPÉRATIONNEL (React 18.2.0)
- ✅ Build: SUCCÈS (27 tâches)
- ✅ Virtual Threads: ACTIVÉS

📋 **Voir [AUDIT-RAPPORT.md](AUDIT-RAPPORT.md) pour l'audit complet**

### Démarrage Alternatif

```powershell
# Démarre backend + desktop (ancien script)
./start-magsav.ps1

# Arrêt propre
./stop-dev.ps1
```

---

## 💻 Développement

### Configuration Automatique

Le profil **`.magsav-profile.ps1`** est chargé automatiquement dans VS Code :
- Désactivation des confirmations PowerShell
- Variables Gradle optimisées (heap 2GB)
- Encodage UTF-8 forcé
- Alias utiles (rm, del, sleep)

### Structure VS Code

**Tasks disponibles** :
- `Build Desktop JavaFX` : Build sans tests
- `Run MAGSAV Desktop` : Lance l'application
- `Start MAGSAV Full Stack` : Lance backend + desktop
- `Start Backend Server` : Lance uniquement le backend

### Standards de Code

#### Vues JavaFX
- Hériter de `AbstractManagerView` pour vues avec toolbar
- Utiliser `StyleFactory` pour créer composants
- Utiliser `ThemeConstants` pour valeurs CSS
- Pas de styles inline hardcodés

#### Services
- Enregistrer dans `ApplicationContext`
- Injection via `getInstance()`
- Services stateless quand possible

#### Navigation
```java
NavigationManager nav = NavigationManager.getInstance();
nav.navigateTo(Route.DASHBOARD);
```

### Tests

```bash
# Tests unitaires
./gradlew test

# Tests d'intégration backend
./test-backend-integration.ps1

# Build sans tests
./gradlew build -x test
```

---

## 🎯 Fonctionnalités

### SAV
- Création/édition demandes intervention
- Suivi réparations avec statuts
- Gestion RMA fournisseurs
- Historique complet par équipement
- Planning techniciens optimisé

### Parc Matériel
- Inventaire complet avec QR codes
- Catégories hiérarchiques (Marque → Type → Modèle)
- Photos et documentation
- Localisation et affectation
- Import/Export données

### Ventes & Installations
- Import PDF affaires (parsing intelligent)
- Gestion projets avec équipements
- Contrats clients avec renouvellement auto
- Suivi installations
- Facturation

### Fournisseurs
- Fiche fournisseur complète
- Demandes matériel avec priorités
- Commandes groupées automatiques
- Seuils et alertes
- Historique commandes

### Véhicules
- Planning disponibilité visuel
- Maintenance préventive
- Entretiens et révisions
- Locations externes
- Suivi kilométrage

### Personnel
- Qualifications et certifications
- Permis et habilitations
- Planning disponibilité
- Intermittents et freelances
- Rôles et permissions

### Planning
- Vue jour/semaine/mois/année
- Drag & drop événements
- Optimisation trajets techniciens
- Export iCal
- Conflits et alertes

---

## ⚙️ Configuration

### Préférences Fenêtres

Sauvegardées automatiquement dans `%APPDATA%/.magsav/preferences/`:
- Position et taille fenêtres
- État maximisé
- Thème sélectionné
- Dernière route visitée

### Base de Données H2

**Mode** : In-memory (données perdues au redémarrage)

**Configuration** : `backend/src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:h2:mem:magsavdb
spring.jpa.hibernate.ddl-auto=create-drop
```

**Données de test** : Chargées automatiquement depuis `data*.sql`

### Thèmes Personnalisés

Fichiers CSS dans `desktop-javafx/src/main/resources/styles/`:
- `magsav-light.css` : Thème clair (défaut)
- `magsav-dark.css` : Thème sombre
- `magsav-blue.css` : Thème bleu
- `magsav-green.css` : Thème vert
- `theme-dark-ultra.css` : Thème très sombre

**Variables disponibles** :
```css
-magsav-primary: #6B71F2;
-magsav-secondary: #8B91FF;
-magsav-bg-primary: #FFFFFF;
-magsav-text-primary: #212529;
```

---

## 📜 Scripts Utiles

### Scripts Principaux

| Script | Description |
|--------|-------------|
| `start-magsav.ps1` | Lance backend + desktop |
| `start-dev.ps1` | Lance backend + desktop + web |
| `stop-dev.ps1` | Arrête tous les processus MAGSAV |
| `quick-dev-mode.ps1` | Configuration rapide environnement dev |

### Scripts Maintenance

| Script | Description |
|--------|-------------|
| `fix-powershell-encoding.ps1` | Corrige encodage UTF-8 BOM scripts |
| `validate-powershell-encoding.ps1` | Vérifie encodage scripts PS |
| `test-backend-integration.ps1` | Tests intégration complète |
| `create-desktop-shortcut.ps1` | Crée raccourci bureau Windows |

### Module ScriptHelper

**Localisation** : `ScriptHelper.psm1`

Fonctions utilitaires PowerShell :
```powershell
Write-ColorOutput "Message" "Green"
Test-Command "gradle"
Wait-ForPort 8080
Stop-JavaProcesses
```

---

## 🎨 Centralisation CSS

Voir documentation complète : **`CSS-CENTRALIZATION.md`**

### ThemeConstants.java

**Localisation** : `desktop-javafx/src/main/java/com/magscene/magsav/desktop/theme/`

#### Espacements
```java
SPACING_SM = 7.0           // Petit (standard containers)
SPACING_MD = 10.0          // Moyen (toolbars)
PADDING_STANDARD           // Insets(7) uniforme
TOOLBAR_PADDING            // Insets(10) toolbars
```

#### Bordures & Styles
```java
BORDER_RADIUS_MD = 8.0     // Tables, toolbars
BORDER_COLOR = "#8B91FF"   // Charte MAGSAV
TOOLBAR_STYLE              // Style complet toolbar
```

#### Tailles Police
```java
FONT_SIZE_NORMAL = 12.0    // Normale
FONT_SIZE_16 = 16.0        // Sections
FONT_SIZE_TITLE = 18.0     // Titres
```

### StyleFactory.java

Factory pour créer composants pré-stylés :

```java
// Labels
Label title = StyleFactory.createSectionTitle("Mon Titre");
Label error = StyleFactory.createErrorLabel("Erreur");

// Boutons
Button primary = StyleFactory.createPrimaryButton("Créer");
Button danger = StyleFactory.createDangerButton("Supprimer");

// Conteneurs
HBox toolbar = StyleFactory.createToolbar();
VBox container = StyleFactory.createStandardVBox();

// Champs
TextField input = StyleFactory.createStyledTextField("Recherche...");

// Tables
TableView<Item> table = StyleFactory.createStyledTable();
```

### Migration Styles

**❌ AVANT (à éviter)** :
```java
title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
box.setPadding(new Insets(7, 7, 7, 7));
```

**✅ APRÈS (recommandé)** :
```java
Label title = StyleFactory.createSectionTitle("Mon titre");
VBox box = StyleFactory.createStandardVBox();
```

---

## 📚 Historique du Projet

### Phase 1: Nettoyage Initial (6 janvier 2026)

Le projet a subi un nettoyage majeur pour éliminer les fichiers temporaires et optimiser la structure.

**Fichiers supprimés (27 au total)**:
- 14 fichiers MD temporaires (BILAN-FINAL-API.md, QUICKSTART.md, STRUCTURE.md, NETTOYAGE.md, etc.)
- 4 fichiers JSON de test (sav-test.json, test-output.json, etc.)
- 2 logs (backend-error.log, backend-output.log)
- 3 scripts dupliqués (start-magsav.ps1 variants)
- 2 documentations frontend redondantes
- 1 script en doublon (simple-import.ps1)
- 1 fichier SQL temporaire (update-vehicle-photos.sql)

**Résultat**:
- Structure propre et documentée
- Réduction de la duplication
- Documentation consolidée dans README.md unique
- Scripts optimisés dans `/scripts/`

### Phase 2: Améliorations Majeures

**Nouvelles fonctionnalités**:
- ✅ Recherche globale dans le header (équipements, SAV, clients, véhicules)
- ✅ Import LOCMAT CSV (endpoint API + script PowerShell)
- ✅ Scripts de démarrage unifiés (start-dev.ps1, health-check.ps1)
- ✅ 28 tests unitaires backend (100% succès)

**Corrections**:
- ✅ Alignement noms de champs (backend ↔ frontend)
- ✅ Correction enum `Equipment.Status.OUT_OF_SERVICE` → `OUT_OF_ORDER`
- ✅ Nettoyage cache Gradle

### État Actuel (6 janvier 2026)

**Backend**:
- ✅ Compilé et fonctionnel (Java 21.0.8)
- ✅ Running sur port 8080
- ✅ 24 controllers, 215+ endpoints
- ✅ Base H2 persistante avec 2548 équipements prêts à importer

**Frontend**:
- ✅ React 18.2.0 + TypeScript
- ✅ 11 pages complètes
- ✅ Recherche globale intégrée
- ✅ Type-check sans erreurs

**Documentation**:
- ✅ README.md consolidé (toutes les informations essentielles)
- ✅ 3 fichiers MD au total (README + backend tests + copilot config)
- ✅ Structure claire et maintenable

**Prochaines étapes**:
- [ ] Exécuter l'import CSV LOCMAT
- [ ] Vérifier affichage des 2548 équipements
- [ ] Génération QR codes
- [ ] Upload photos

---

## 🤝 Contribution

### Workflow Git

```bash
git checkout -b feature/ma-fonctionnalite
git commit -m "feat: description"
git push origin feature/ma-fonctionnalite
```

### Standards Commits

- `feat:` Nouvelle fonctionnalité
- `fix:` Correction bug
- `refactor:` Refactoring
- `style:` Changements CSS/UI
- `docs:` Documentation
- `chore:` Maintenance

---

## 🆘 Support

1. Consulter cette documentation
2. Vérifier les logs dans les terminaux backend/frontend
3. Tester avec `.\scripts\health-check.ps1`
4. Consulter la documentation spécifique:
   - Tests: [backend/src/test/README.md](backend/src/test/README.md)
   - Copilot: [.github/copilot-instructions.md](.github/copilot-instructions.md)

---

**Version**: 3.0.0  
**Dernière mise à jour**: 6 janvier 2026  
© 2024-2026 Mag Scène

---

**Version** : 3.0.0  
**Dernière mise à jour** : 27 novembre 2025  
© 2024 Mag Scène
