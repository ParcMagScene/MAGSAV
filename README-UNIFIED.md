# MAGSAV 3.0 - Système de Gestion SAV et Parc Matériel

## 🎯 Vue d'ensemble

Application de gestion complète pour Mag Scène avec architecture refactorisée v3.0 :
- **SAV** : Demandes d'intervention, réparations, RMA, historique
- **Parc Matériel** : Inventaire avec QR codes, catégories hiérarchiques, photos
- **Ventes & Installations** : Import PDF affaires, commandes fournisseurs groupées
- **Véhicules** : Planning, maintenance, entretiens, locations externes
- **Personnel** : Qualifications, permis, planning, intermittents/freelances

## 🏗️ Architecture Technique

### Stack
- **Backend** : Spring Boot 3.3.5 + H2 Database + JWT Security
- **Desktop** : JavaFX 21 (interface principale)
- **Web** : React 18 TypeScript (même interface que desktop)
- **Build** : Gradle multi-module monorepo
- **Prérequis** : Java 17+, Node.js 18+

### Modules
```
MAGSAV-3.0/
├── backend/              # Spring Boot REST API + H2
├── desktop-javafx/       # Application JavaFX desktop  
├── web-frontend/         # Interface React TypeScript
├── common-models/        # Entités JPA partagées
└── integration-tests/    # Tests E2E
```

### Architecture Refactorisée v3.0

**Améliorations majeures** :
- ✅ Injection de dépendances (ApplicationContext)
- ✅ Navigation centralisée (NavigationManager)
- ✅ Services métier spécialisés (API + Business)
- ✅ Hiérarchie de vues unifiée
- ✅ Gestion des préférences fenêtres
- ✅ Thème unifié avec mode clair/sombre

## 🚀 Démarrage Rapide

### Installation
```bash
git clone [repository-url]
cd MAGSAV-3.0
./gradlew build
```

### Démarrage
```powershell
# Stack complète (recommandé)
./start-magsav.ps1

# Ou individuellement :
./gradlew :backend:bootRun          # Backend sur :8080
./gradlew :desktop-javafx:run       # Application desktop
cd web-frontend && npm start        # Web sur :3000
```

### Configuration Développement

**Mode Dev automatique** : Le profil `.magsav-profile.ps1` est chargé automatiquement dans VS Code et configure :
- Désactivation des confirmations PowerShell
- Variables d'environnement Gradle optimisées
- Alias utiles (rm, del, sleep)
- Encodage UTF-8 pour Java

**Scripts utiles** :
```powershell
./start-magsav.ps1                     # Démarre backend + desktop
./test-backend-integration.ps1         # Test intégration complète
./fix-powershell-encoding.ps1          # Corrige encodage UTF-8 BOM
./validate-powershell-encoding.ps1     # Vérifie encodage scripts
```

## 📁 Structure Détaillée

### Core Framework

#### ApplicationContext (DI Container)
```java
com.magscene.magsav.desktop.core.di.ApplicationContext
```
- Instance singleton pour toute l'application
- Enregistrement automatique des services
- Injection automatique des dépendances
- Gestion du cycle de vie

#### NavigationManager
```java
com.magscene.magsav.desktop.core.navigation.NavigationManager
```
- Navigation centralisée
- Cache intelligent des vues
- Système d'événements
- Gestion typée des routes

### Services

#### API Clients
```
com.magscene.magsav.desktop.service.api/
├── BaseApiClient.java          # Client HTTP de base
├── EquipmentApiClient.java     # API Equipment
└── SAVApiClient.java          # API SAV
```

#### Business Services
```
com.magscene.magsav.desktop.service.business/
├── EquipmentService.java       # Logique métier équipement
├── SAVService.java            # Logique métier SAV
└── CategoryService.java       # Gestion catégories
```

#### Utility Services
```
com.magscene.magsav.desktop.service/
├── WindowPreferencesService.java   # Mémorisation taille/position fenêtres
├── ApiService.java                 # Service API générique (legacy)
└── TestDataService.java           # Données de test
```

### Vues

#### Hiérarchie
```
AbstractManagerView (base abstraite)
├── Equipment (EquipmentManagerView)
├── SAV (SAVManagerView)
├── Clients (ClientManagerView)
├── Contracts (ContractManagerView)
├── Vehicles (VehicleManagerView)
├── Personnel (PersonnelManagerView)
└── Settings (SettingsView)
```

#### Composants Réutilisables
```
com.magscene.magsav.desktop.component/
├── CustomTabPane.java          # Onglets personnalisés
├── GlobalSearchComponent.java  # Recherche globale
└── QRCodeComponent.java       # Génération QR codes
```

### Thème

```
com.magscene.magsav.desktop.theme/
├── UnifiedThemeManager.java    # Gestionnaire principal
└── ThemeManager.java          # @Deprecated - migration en cours
```

## 🧪 Tests et Validation

### Tests Backend
```powershell
./gradlew :backend:test          # Tests unitaires
./gradlew :backend:bootRun       # Démarrage manuel
```

### Tests Desktop
```powershell
./gradlew :desktop-javafx:test   # Tests JavaFX
./gradlew :desktop-javafx:run    # Lancement application
```

### Tests Intégration
```powershell
./test-backend-integration.ps1   # Test complet backend + desktop
```

## 📋 Bonnes Pratiques

### PowerShell
- **Encodage** : UTF-8 avec BOM obligatoire
- **VS Code** : Configuré pour encoder automatiquement en UTF-8 BOM
- **Validation** : Utiliser `validate-powershell-encoding.ps1`
- **Correction** : Utiliser `fix-powershell-encoding.ps1`

### Java
- **Encodage** : UTF-8 défini via JAVA_TOOL_OPTIONS
- **Imports** : Éviter les wildcards (import javafx.scene.control.*)
- **Deprecated** : Migrer de ThemeManager vers UnifiedThemeManager

### Git
- **Branches** : main (production), develop (développement)
- **Commits** : Messages descriptifs en français
- **Pre-commit** : Validation encodage automatique

## 🔧 Configuration VS Code

Le workspace est pré-configuré avec :
- Auto-save (1 seconde)
- Format on save
- Terminal PowerShell Dev Mode
- Java auto-update build
- Exclusions optimisées (build/, node_modules/)

## 📚 Documentation Technique

### API REST Backend
- Base URL : `http://localhost:8080/api`
- Health : `http://localhost:8080/api/health`
- Swagger UI : `http://localhost:8080/swagger-ui.html`

### Base de Données H2
- Console : `http://localhost:8080/h2-console`
- URL JDBC : `jdbc:h2:mem:magsavdb`
- User : `sa`
- Password : (vide)

## 🐛 Dépannage

### Backend ne démarre pas
```powershell
# Vérifier le port 8080
netstat -ano | findstr :8080
# Tuer le processus si occupé
Stop-Process -Id <PID> -Force
```

### Desktop ne se connecte pas au backend
```powershell
# Vérifier que le backend répond
Invoke-WebRequest http://localhost:8080/api/health
```

### Problèmes d'encodage PowerShell
```powershell
# Corriger tous les scripts
./fix-powershell-encoding.ps1
# Valider
./validate-powershell-encoding.ps1
```

## 📝 Changelog

### v3.0.0 (2025-11-26)
- ✅ Architecture refactorisée avec DI et navigation centralisée
- ✅ Mémorisation taille/position des fenêtres
- ✅ Popups s'ouvrent sur l'écran de la fenêtre principale
- ✅ Titres de page centrés dans le header
- ✅ Nettoyage scripts PowerShell (9 → 5 scripts)
- ✅ Documentation unifiée

## 👥 Contribution

Pour contribuer :
1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## 📄 Licence

Propriétaire - Mag Scène © 2025
