# 🎯 MAGSAV 3.0 - Documentation Complète

**Système de Gestion SAV et Parc Matériel pour Mag Scène**

---

## 📋 Table des matières

1. [Vue d'ensemble](#-vue-densemble)
2. [Architecture](#-architecture)
3. [Installation & Démarrage](#-installation--démarrage)
4. [Développement](#-développement)
5. [Fonctionnalités](#-fonctionnalités)
6. [Configuration](#-configuration)
7. [Scripts Utiles](#-scripts-utiles)
8. [Centralisation CSS](#-centralisation-css)

---

## 🎨 Vue d'ensemble

### Modules Métier

- **SAV** : Gestion des demandes d'intervention, réparations, RMA, historique complet
- **Parc Matériel** : Inventaire avec QR codes, catégories hiérarchiques, photos
- **Ventes & Installations** : Import PDF affaires, gestion projets et contrats
- **Fournisseurs** : Commandes groupées, demandes matériel, seuils automatiques
- **Véhicules** : Planning disponibilité, maintenance, entretiens, locations
- **Personnel** : Qualifications, permis, planning, intermittents/freelances
- **Planning** : Calendrier jour/semaine/mois/année avec optimisation trajets

### Stack Technique

- **Backend** : Spring Boot 3.3.5 + H2 Database + JWT Security
- **Desktop** : JavaFX 21 (interface principale)
- **Web** : React 18 TypeScript (interface responsive)
- **Build** : Gradle 8.4 multi-module
- **Prérequis** : Java 17+, Node.js 18+

---

## 🏗️ Architecture

### Structure Monorepo

```
MAGSAV-3.0/
├── backend/              # Spring Boot REST API + H2
│   ├── controller/       # Endpoints REST
│   ├── service/          # Logique métier
│   ├── repository/       # Accès données JPA
│   └── dto/              # Data Transfer Objects
├── desktop-javafx/       # Application JavaFX 21
│   ├── core/             # Framework (DI, Navigation)
│   ├── view/             # Vues JavaFX
│   ├── component/        # Composants réutilisables
│   ├── service/          # Services frontend
│   ├── dialog/           # Dialogues modaux
│   ├── theme/            # Gestion thèmes + CSS
│   └── util/             # Utilitaires
├── web-frontend/         # React TypeScript
├── common-models/        # Entités JPA partagées
└── integration-tests/    # Tests E2E
```

### Architecture v3.0 Refactorisée

#### 🔧 ApplicationContext (Injection de Dépendances)
**Localisation** : `com.magscene.magsav.desktop.core.di.ApplicationContext`

- Instance **Singleton** unique
- Enregistrement automatique des services
- Injection automatique des dépendances
- Gestion du cycle de vie

**Utilisation** :
```java
ApplicationContext ctx = ApplicationContext.getInstance();
ApiService api = ctx.getService(ApiService.class);
```

#### 🧭 NavigationManager (Navigation Centralisée)
**Localisation** : `com.magscene.magsav.desktop.core.navigation.NavigationManager`

- Navigation centralisée typée
- Cache intelligent des vues
- Système d'événements
- Gestion de l'historique

**Routes disponibles** :
```java
DASHBOARD, SAV, EQUIPMENT, CLIENTS, CONTRACTS, 
VEHICLES, PERSONNEL, PLANNING, SUPPLIERS, 
MATERIAL_REQUESTS, GROUPED_ORDERS, SETTINGS
```

#### 📊 Hiérarchie de Vues

**AbstractManagerView** : Classe de base pour toutes les vues avec toolbar standardisée
```
BorderPane
├── Top: Toolbar standard (filtres + actions)
└── Center: Contenu (Table + Detail Panel OU Tabs)
```

**Vues principales** :
- `SAVManagerView` : Gestion SAV complète
- `EquipmentManagerView` : Parc matériel
- `ClientManagerView` : Clients
- `VehicleManagerView` : Véhicules avec tabs
- `PersonnelManagerView` : Personnel
- `SalesInstallationTabsView` : Projets + Contrats
- `SupplierManagerView` : Fournisseurs avec tabs

#### 🎨 Système de Thèmes

**UnifiedThemeManager** : Gestion centralisée des thèmes
- Thèmes : Light, Dark, Blue, Green, Dark Ultra
- Persistance des préférences
- Hot-reload des thèmes
- Variables CSS dynamiques

**ThemeConstants** : Constantes CSS centralisées
- Couleurs, espacements, polices
- Bordures, radius, shadows
- Styles de boutons, labels, inputs

**StyleFactory** : Factory pour composants pré-stylés
```java
Label title = StyleFactory.createSectionTitle("Mon Titre");
Button btn = StyleFactory.createPrimaryButton("Action");
VBox container = StyleFactory.createStandardVBox();
```

---

## 🚀 Installation & Démarrage

### Installation

```bash
git clone [repository-url]
cd MAGSAV-3.0
./gradlew build
```

### Démarrage Full Stack

```powershell
# Démarre backend + desktop (recommandé)
./start-magsav.ps1

# OU démarre backend + desktop + web
./start-dev.ps1

# Arrêt propre
./stop-dev.ps1
```

### Démarrage Individuel

```bash
# Backend (API REST sur port 8080)
./gradlew :backend:bootRun

# Desktop JavaFX
./gradlew :desktop-javafx:run

# Web React (port 3000)
cd web-frontend
npm install
npm start
```

### Endpoints Backend

- **API REST** : http://localhost:8080/api
- **H2 Console** : http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:magsavdb`
  - User: `sa`
  - Password: `password`

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

## 📚 Documentation Complémentaire

- **Centralisation CSS** : `CSS-CENTRALIZATION.md`
- **Best Practices PowerShell** : `POWERSHELL-BEST-PRACTICES.md`
- **Copilot Instructions** : `.github/copilot-instructions.md`

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
2. Vérifier les logs dans la console
3. Tester avec `./test-backend-integration.ps1`
4. Vérifier l'encodage avec `./validate-powershell-encoding.ps1`

---

**Version** : 3.0.0  
**Dernière mise à jour** : 27 novembre 2025  
© 2024 Mag Scène
