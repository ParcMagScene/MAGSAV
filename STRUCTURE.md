# Structure du Projet MAGSAV-3.0

## 📁 Architecture

```
MAGSAV-3.0/
│
├── 📋 Configuration Racine
│   ├── build.gradle              # Configuration Gradle multi-module
│   ├── settings.gradle            # Déclaration des modules
│   ├── gradle.properties          # Propriétés du build
│   ├── gradlew.bat               # Wrapper Gradle (Windows)
│   └── README.md                 # Documentation principale
│
├── 🔧 Backend (Spring Boot 3.4.13 + Java 21)
│   ├── src/main/java/
│   │   └── com.magscene.magsav.backend/
│   │       ├── controller/       # 24 REST Controllers
│   │       ├── service/          # Services métier
│   │       ├── repository/       # 23 JPA Repositories
│   │       ├── security/         # JWT + Spring Security
│   │       ├── dto/              # Data Transfer Objects
│   │       └── util/             # Utilitaires
│   │
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── data.sql              # Données initiales
│   │
│   └── build.gradle              # Dépendances backend
│
├── 🌐 Frontend Web (React 18 + TypeScript)
│   ├── src/
│   │   ├── components/
│   │   │   ├── DataTable.tsx     # Table réutilisable
│   │   │   ├── Filters.tsx       # Filtres avancés
│   │   │   └── GlobalSearch.tsx  # Recherche globale
│   │   │
│   │   ├── pages/
│   │   │   ├── Dashboard.tsx     # Tableau de bord
│   │   │   ├── Equipment.tsx     # Parc matériel
│   │   │   ├── ServiceRequests.tsx # SAV
│   │   │   ├── Clients.tsx
│   │   │   ├── Contracts.tsx
│   │   │   ├── SalesInstallations.tsx
│   │   │   ├── Vehicles.tsx
│   │   │   ├── Personnel.tsx
│   │   │   ├── Planning.tsx
│   │   │   ├── Suppliers.tsx
│   │   │   └── Settings.tsx
│   │   │
│   │   ├── services/
│   │   │   └── apiService.ts     # Client API REST
│   │   │
│   │   ├── types/
│   │   │   ├── index.ts          # Types principaux
│   │   │   └── entities.ts       # Entités backend
│   │   │
│   │   ├── App.tsx               # Composant principal + routing
│   │   └── index.tsx             # Point d'entrée
│   │
│   ├── package.json              # Dépendances npm
│   └── tsconfig.json             # Configuration TypeScript
│
├── 📦 Common Models (Entités JPA partagées)
│   └── src/main/java/
│       └── com.magscene.magsav.common.models/
│           ├── Equipment.java
│           ├── ServiceRequest.java
│           ├── Client.java
│           ├── Contract.java
│           ├── Vehicle.java
│           ├── Person.java
│           ├── Supplier.java
│           └── ... (23 entités au total)
│
├── 🧪 Integration Tests
│   └── build.gradle              # Tests E2E
│
├── 📜 Scripts
│   ├── start-dev.ps1             # Démarrage dev (backend + frontend)
│   ├── stop-dev.ps1              # Arrêt des processus
│   ├── import-locmat-csv.ps1     # Import équipements LOCMAT
│   └── fix-encoding.ps1          # Correction encodage fichiers
│
├── 📊 Données
│   ├── data/                     # Base H2 (runtime)
│   └── Exports LOCMAT/           # CSV inventaire (2548 équipements)
│
└── 🖼️ Médias
    └── Medias MAGSAV/
        ├── Avatars/              # Photos personnel
        ├── Logos/                # Logos clients
        └── Photos/               # Photos équipements
```

## 🚀 Démarrage Rapide

### 1. Prérequis
- Java 21+
- Node.js 18+
- Gradle 8.4+ (inclus via wrapper)

### 2. Démarrage en mode développement

```powershell
# Démarrer backend + frontend
.\scripts\start-dev.ps1

# Backend uniquement
.\scripts\start-dev.ps1 -BackendOnly

# Frontend uniquement
.\scripts\start-dev.ps1 -FrontendOnly

# Avec nettoyage préalable
.\scripts\start-dev.ps1 -Clean
```

### 3. URLs d'accès

- **Frontend Web**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:file:./data/magsav`
  - User: `sa`
  - Password: *(vide)*

### 4. Arrêt

```powershell
.\scripts\stop-dev.ps1
```

## 📊 Statistiques du Projet

### Backend API
- **Controllers**: 24
- **Endpoints REST**: 215+
- **Repositories JPA**: 23
- **Entités**: 23
- **Services**: 20+

### Frontend Web
- **Pages**: 11
- **Composants réutilisables**: 3
- **Services API**: 1 (centralisé)
- **Routes**: 11

### Tests
- **Tests unitaires backend**: En cours
- **Tests intégration**: Module dédié
- **Tests frontend**: Jest + React Testing Library

## 🔑 Fonctionnalités Principales

### ✅ Implémenté
- [x] Gestion complète du parc matériel (CRUD + recherche + filtres)
- [x] Import CSV LOCMAT (2548 équipements)
- [x] Recherche globale dans le header (équipements, SAV, clients, véhicules)
- [x] Demandes SAV avec statuts et priorités
- [x] Gestion clients et contrats
- [x] Ventes et installations
- [x] Gestion véhicules avec planning
- [x] Gestion personnel avec qualifications
- [x] Fournisseurs et commandes
- [x] Dashboard avec statistiques
- [x] API REST complète (CRUD sur toutes les entités)
- [x] Sécurité JWT
- [x] Base H2 persistante

### 🚧 En cours
- [ ] Génération QR codes équipements
- [ ] Upload photos équipements
- [ ] Planificateur de trajets optimisés
- [ ] Notifications temps réel
- [ ] Export PDF rapports

## 🛠️ Développement

### Build
```powershell
# Build complet (sans tests)
./gradlew.bat build -x test

# Build backend uniquement
./gradlew.bat :backend:build -x test

# Build frontend
cd web-frontend
npm run build
```

### Tests
```powershell
# Tests backend
./gradlew.bat test

# Tests frontend
cd web-frontend
npm test
```

### Vérifications
```powershell
# Compilation Java
./gradlew.bat :backend:compileJava

# Vérification TypeScript
cd web-frontend
npm run type-check
```

## 📝 Conventions de Code

### Backend (Java)
- **Package structure**: `com.magscene.magsav.backend.{controller|service|repository}`
- **Naming**: PascalCase pour classes, camelCase pour méthodes
- **REST endpoints**: `/api/{resource}`
- **DTOs**: Suffixe `DTO` (ex: `EquipmentDTO`)

### Frontend (TypeScript)
- **Components**: PascalCase + `.tsx`
- **Services**: camelCase + `.ts`
- **Types**: Interface avec PascalCase
- **CSS**: `.css` co-localisé avec composant

## 🔐 Sécurité

- **Authentification**: JWT (JSON Web Tokens)
- **CORS**: Configuré pour localhost:3000
- **Base de données**: H2 avec credentials
- **API**: Spring Security avec filtres personnalisés

## 📦 Import LOCMAT

Pour importer l'inventaire LOCMAT (CSV):

```powershell
.\scripts\import-locmat-csv.ps1 -CsvFile ".\Exports LOCMAT\Inventaire_Complet_Avec_NS.csv"
```

## 🐛 Troubleshooting

### Backend ne démarre pas
```powershell
# Nettoyer les locks H2
Get-Process -Name java,javaw | Stop-Process -Force
Remove-Item backend/data/*.lock -Force
```

### Port 8080 déjà utilisé
```powershell
# Trouver et arrêter le processus
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Frontend ne compile pas
```powershell
cd web-frontend
rm -rf node_modules package-lock.json
npm install
```

## 📞 Support

Pour toute question ou problème:
- Consulter le [README.md](README.md) principal
- Vérifier les logs dans les terminaux backend/frontend
- Consulter la H2 Console pour les données
