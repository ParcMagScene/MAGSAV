# 📚 MAGSAV 3.0 - Documentation Complète

> **Application multi-plateforme de gestion SAV et parc matériel pour Mag Scène**

![Statut](https://img.shields.io/badge/Statut-STABLE-green) ![Backend](https://img.shields.io/badge/Backend-OPÉRATIONNEL-green) ![Frontend](https://img.shields.io/badge/Frontend-OPÉRATIONNEL-green) ![Java](https://img.shields.io/badge/Java-21.0.8-blue) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen) ![React](https://img.shields.io/badge/React-18.2.0-blue)

**Dernière mise à jour:** 8 janvier 2026

---

## 📋 Table des Matières

- [🚀 Démarrage Rapide](#-démarrage-rapide)
- [📁 Structure du Projet](#-structure-du-projet)
- [🎯 Vue d'Ensemble](#-vue-densemble)
- [💻 Développement](#-développement)
- [🔧 Configuration](#-configuration)
- [📊 Audit Technique](#-audit-technique)
- [🧹 Nettoyage Projet](#-nettoyage-projet)

---

## 🚀 Démarrage Rapide

### ⚡ Lancement en 1 Commande

```powershell
.\start-magsav.ps1
```

**Résultat:**
- ✅ Backend Spring Boot sur http://localhost:8080
- ✅ Frontend React sur http://localhost:3000
- ✅ Navigateur s'ouvre automatiquement

### 📍 URLs Importantes

| Service | URL | Description |
|---------|-----|-------------|
| 🌐 Frontend | http://localhost:3000 | Interface React |
| 🔧 Backend | http://localhost:8080 | API REST |
| 📊 Console H2 | http://localhost:8080/h2-console | Base de données |
| 📖 Swagger | http://localhost:8080/swagger-ui.html | Documentation API |

### 💾 Accès Base H2

```
JDBC URL: jdbc:h2:file:~/magsav/data/magsav
Username: sa
Password: password
```

### 🛑 Arrêt

Fermez les terminaux PowerShell ou:
```powershell
# Arrêt manuel des processus
Get-Process -Name java | Stop-Process -Force
Get-Process -Name node | Stop-Process -Force
```

### ⚡ Performances

- ✅ Backend démarre en ~9 secondes
- ✅ Frontend compile en ~15 secondes
- ✅ Java 21 avec Virtual Threads activés
- ✅ Hot reload activé sur les 2 services

---

## 📁 Structure du Projet

### Architecture Multi-Module

```
MAGSAV-3.0/
├── 📋 Configuration Racine
│   ├── build.gradle              # Configuration Gradle multi-module
│   ├── settings.gradle            # Déclaration des modules
│   ├── gradle.properties          # Propriétés du build
│   ├── gradlew.bat               # Wrapper Gradle (Windows)
│   └── DOCUMENTATION.md          # Ce fichier
│
├── 🔧 Backend (Spring Boot 3.4.13 + Java 21)
│   ├── src/main/java/com.magscene.magsav.backend/
│   │   ├── controller/           # 24 REST Controllers
│   │   ├── service/              # Services métier
│   │   ├── repository/           # 23 JPA Repositories
│   │   ├── security/             # JWT + Spring Security
│   │   ├── dto/                  # Data Transfer Objects
│   │   └── util/                 # Utilitaires
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── data-*.sql            # Scripts SQL d'initialisation
│   └── build.gradle
│
├── 🌐 Frontend Web (React 18 + TypeScript)
│   ├── src/
│   │   ├── components/           # Composants réutilisables
│   │   │   ├── DataTable.tsx
│   │   │   ├── DetailDrawer.tsx
│   │   │   └── GlobalSearch.tsx
│   │   ├── pages/                # Pages principales (11)
│   │   │   ├── Dashboard.tsx
│   │   │   ├── Equipment.tsx
│   │   │   ├── ServiceRequests.tsx
│   │   │   └── ...
│   │   ├── services/
│   │   │   └── api.service.ts    # Client API REST
│   │   └── types/
│   │       └── entities.ts       # Types TypeScript
│   ├── package.json
│   └── tsconfig.json
│
├── 📦 Common Models
│   └── src/main/java/            # 23 entités JPA partagées
│       ├── Equipment.java
│       ├── ServiceRequest.java
│       └── ...
│
├── 🧪 Integration Tests
│   └── build.gradle
│
├── 📜 Scripts
│   ├── start-magsav.ps1          # Démarrage application
│   ├── import-locmat-csv.ps1     # Import équipements
│   └── health-check.ps1          # Vérification santé
│
├── 📊 Données
│   ├── data/                     # Base H2 (runtime)
│   └── Exports LOCMAT/           # CSV inventaire (2548 items)
│
└── 🖼️ Médias
    └── Medias MAGSAV/
        ├── Avatars/              # Photos personnel
        ├── Logos/                # Logos clients
        └── Photos/               # Photos équipements (4 items)
```

---

## 🎯 Vue d'Ensemble

### Modules Métier

#### 🔧 SAV (Service Après-Vente)
- Gestion demandes d'intervention avec statuts
- Suivi réparations (RMA, repairs)
- Historique complet des interventions
- Affectation techniciens et planning

#### 📦 Parc Matériel
- Inventaire avec QR codes
- Catégories hiérarchiques (category → subCategory → specificCategory)
- Gestion photos et médias
- États: DISPONIBLE, EN_REPARATION, EN_PRET, HORS_SERVICE
- Import CSV LOCMAT (2547 équipements)

#### 💼 Ventes & Installations
- Import PDF affaires
- Gestion projets et contrats
- Suivi installations clients
- Historique complet

#### 🏢 Fournisseurs
- Commandes groupées
- Demandes matériel
- Seuils automatiques stock
- Catalogue produits

#### 🚗 Véhicules
- Planning unifié
- Réservations et maintenance
- Entretiens programmés
- Locations externes

#### 👥 Personnel
- Qualifications et certifications
- Permis de conduire
- Planning unifié avec véhicules
- Gestion intermittents/freelances

#### 📅 Planning Global
- Vue unifiée personnel + véhicules + équipements
- Détection automatique de conflits
- Synchronisation temps réel
- Export iCal/Google Calendar

### Stack Technique

| Composant | Version | Description |
|-----------|---------|-------------|
| **Backend** | Spring Boot 3.4.13 | API REST + JPA/Hibernate |
| **Base de données** | H2 2.2.224 | Base embarquée file-based |
| **Frontend** | React 18.2.0 | Interface TypeScript |
| **Build** | Gradle 8.4 | Multi-module monorepo |
| **Java** | 21.0.8 (OpenJDK) | Microsoft Build |
| **Node.js** | 18+ | Runtime JavaScript |
| **Sécurité** | Spring Security + JWT | Authentification stateless |

### Statistiques Projet

- **Code Java Backend**: 24 controllers, 23 repositories, 23 entities
- **Frontend React**: 11 pages, 15+ composants réutilisables
- **Base de données**: 2547 équipements importés
- **API REST**: ~80 endpoints documentés (Swagger)
- **Tests**: Infrastructure E2E configurée

---

## 💻 Développement

### Prérequis

```powershell
# Vérification versions
java -version      # Java 21.0.8 requis
node -v            # Node.js 18+ requis
npm -v             # npm 9+ requis
```

### Installation Initiale

```powershell
# 1. Clone du projet
git clone https://github.com/ParcMagScene/MAGSAV.git
cd MAGSAV-3.0

# 2. Build Gradle
.\gradlew.bat build -x test

# 3. Installation dépendances Frontend
cd web-frontend
npm install
cd ..
```

### Développement Backend

```powershell
# Démarrage backend seul
.\gradlew.bat :backend:bootRun

# Avec rechargement automatique
.\gradlew.bat :backend:bootRun --continuous

# Build optimisé
.\gradlew.bat :backend:bootJar
```

### Développement Frontend

```powershell
cd web-frontend

# Démarrage dev server
npm start

# Build production
npm run build

# Tests
npm test
```

### Scripts Disponibles

| Script | Description |
|--------|-------------|
| `start-magsav.ps1` | Lance backend + frontend |
| `scripts/import-locmat-csv.ps1` | Import équipements depuis CSV |
| `scripts/health-check.ps1` | Vérification santé API |
| `scripts/build-web.ps1` | Build production frontend |

---

## 🔧 Configuration

### Backend (application.properties)

```properties
# Serveur
server.port=8080

# Base H2
spring.datasource.url=jdbc:h2:file:~/magsav/data/magsav
spring.datasource.username=sa
spring.datasource.password=password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Initialisation données
spring.sql.init.mode=always
spring.sql.init.data-locations=classpath:data-*.sql

# Console H2
spring.h2.console.enabled=true

# JWT
jwt.secret=votre-secret-jwt
jwt.expiration=86400000
```

### Frontend (environnement)

```typescript
// src/services/api.service.ts
const API_BASE_URL = 'http://localhost:8080/api';
```

### Gradle (gradle.properties)

```properties
# Build
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.jvmargs=-Xmx2048m

# Versions
springBootVersion=3.4.13
javaVersion=21
```

---

## 📊 Audit Technique

### État au 8 janvier 2026

#### ✅ Fonctionnalités Opérationnelles

- **Backend API REST**: Tous les endpoints testés et fonctionnels
- **Frontend React**: Interface complète avec 11 pages
- **Base de données**: 2547 équipements importés avec succès
- **Import CSV LOCMAT**: 2540/2548 équipements (10 doublons serial_number)
- **DTO Equipment**: Champs subCategory, specificCategory, quantityInStock ajoutés

#### 🔍 Vérifications Effectuées

1. **API REST**
   - ✅ GET /api/equipment retourne tous les champs
   - ✅ Pagination fonctionnelle (page, size)
   - ✅ Filtres avancés opérationnels

2. **Base de données**
   - ✅ DDL auto-update configuré
   - ✅ Scripts SQL exécutés au démarrage
   - ✅ Données test + import LOCMAT coexistent

3. **Build**
   - ✅ Gradle build: SUCCESS (16 tâches)
   - ✅ Backend compilation: Java 21.0.8
   - ✅ Frontend: webpack compiled successfully

#### ⚠️ Avertissements Non-Bloquants

1. **Spring Boot 3.4.x**: Support OSS terminé (2025-12-31)
   - Impact: Informatif uniquement
   - Action: Migration vers 3.5+ recommandée avant fin 2026

2. **Commons CSV (IDE)**: 12 erreurs de résolution dans VS Code
   - Impact: Aucun (build Gradle fonctionne)
   - Cause: Cache IDE non synchronisé
   - Solution: `Java: Clean Language Server Workspace`

3. **Méthodes dépréciées**: CSVFormat.withFirstRecordAsHeader()
   - Impact: Warnings compilation uniquement
   - Action: Migration vers nouvelles méthodes recommandée

#### 📈 Performance

- Temps démarrage backend: ~9s
- Temps compilation frontend: ~15s
- Requête API moyenne: <50ms
- Taille base H2: ~5 MB (2547 équipements)

---

## 🧹 Nettoyage Projet

### Rapport du 8 janvier 2026

#### Actions Effectuées

1. **Fichiers Obsolètes Supprimés** (29 fichiers, ~plusieurs MB)
   - Documentation obsolète: BILAN-*.md, EXPORT-*.md, MIGRATION-*.md, etc.
   - Logs: backend-error.log, backend-output.log
   - Fichiers test: sav-test.json, test-output.json, test-*.json
   - Scripts SQL temporaires: update-vehicle-photos.sql
   - Scripts PowerShell redondants: start-dev-full.ps1

2. **Module desktop-javafx Supprimé**
   - 200+ fichiers JavaFX obsolètes
   - Architecture desktop abandonnée au profit du web
   - ~3 MB libérés

3. **Caches Build Nettoyés**
   - `.gradle/` supprimé et reconstruit
   - `backend/build/` nettoyé
   - `web-frontend/node_modules/.cache/` vidé

4. **.gitignore Optimisé**
   - Patterns documentation temporaire supprimés
   - Patterns fichiers build ajoutés
   - cleanup.ps1 ajouté aux exclusions

#### Structure Finale Propre

```
MAGSAV-3.0/
├── DOCUMENTATION.md       # Ce fichier (documentation complète)
├── README.md             # Présentation courte
├── build.gradle          # Configuration Gradle
├── settings.gradle       # Modules
├── backend/              # API Spring Boot
├── common-models/        # Entités JPA
├── integration-tests/    # Tests E2E
├── web-frontend/         # Interface React
├── scripts/              # Scripts PowerShell
├── data/                 # Base H2
├── Exports LOCMAT/       # CSV inventaire
└── Medias MAGSAV/        # Photos/avatars
```

#### Résultats

- ✅ Build: SUCCESS (16 tâches)
- ✅ Backend: Opérationnel
- ✅ Frontend: Opérationnel
- ✅ Base: 2547 équipements
- ✅ Git: Working tree clean
- ✅ Espace libéré: ~5-10 MB

---

## 🐛 Dépannage

### Problèmes Courants

#### Port 8080 déjà utilisé
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

#### Port 3000 déjà utilisé
```powershell
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

#### npm install échoue
```bash
cd web-frontend
rm -rf node_modules package-lock.json
npm install
```

#### Gradle build échoue
```bash
.\gradlew.bat clean
.\gradlew.bat build -x test
```

#### Base H2 corrompue
```powershell
# Suppression base (sera recréée au prochain démarrage)
rm ~/magsav/data/magsav.mv.db
```

#### Cache VS Code désynchronisé
```
Ctrl+Shift+P → "Java: Clean Language Server Workspace"
```

---

## 📞 Support

- **Repository**: https://github.com/ParcMagScene/MAGSAV
- **Propriétaire**: ParcMagScene
- **Branche**: main
- **Documentation**: Ce fichier (DOCUMENTATION.md)

---

**Bon développement ! 🎉**
