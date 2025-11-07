# 📋 MAGSAV-3.0 - Documentation Technique Complète

## 🏗️ Architecture du Projet

### Structure Générale
- **Backend** : Spring Boot 3.1 + H2 Database + JWT Security
- **Desktop** : JavaFX 21 (interface principale)  
- **Web** : React 18 TypeScript (même interface que desktop)
- **Build** : Gradle multi-module monorepo
- **Base** : Java 17+, Node.js 18+

### Modules
```
MAGSAV-3.0/
├── backend/          # Spring Boot REST API + H2
├── desktop-javafx/   # Application JavaFX desktop  
├── web-frontend/     # Interface React TypeScript
├── common-models/    # Entités JPA partagées
└── integration-tests/ # Tests E2E
```

## 🎨 Système de Thèmes et Interface

### Thème Sombre Unifié
- **Couleur principale** : `#142240` (sélection)
- **Couleurs secondaires** : Palette bleue harmonisée
- **CSS principal** : `/styles/theme-dark-ultra.css`
- **Surlignage sélection** : Cohérent sur tous les modules

### Navigation Moderne
- **Sidebar verticale** : Navigation principale optimisée
- **Onglets verticaux** : Amélioration UX
- **Recherche globale** : Intégrée avec tous les modules
- **Indicateurs de statut** : Visuels harmonisés

### Standardisation Interface
- **Espacements** : Standardisés (8px, 16px, 24px)
- **Toolbars** : Unifiées sur tous les modules
- **Boutons** : Design cohérent et états
- **Titres modules** : Format uniforme
- **Suppression zones blanches** : Thème sombre complet

## 🔧 Système de Volet de Visualisation

### Architecture Technique
- **DetailPanel** : Panneau coulissant 400px avec animation 300ms
- **DetailPanelProvider** : Interface pour objets affichables
- **DetailPanelContainer** : Wrapper automatique pour TableView/ListView
- **QRCodeGenerator** : Génération QR codes (équipements uniquement)

### Implémentation par Module

#### ✅ Équipements (Parc Matériel)
- **Images** : Photo équipement + Logo fabricant
- **QR Code** : ✅ Généré pour traçabilité
- **Détails** : Specs techniques, maintenance, localisation

#### ✅ Personnel
- **Images** : Avatar selon poste (technicien, manager, commercial, admin)
- **QR Code** : ❌ Supprimé
- **Détails** : Informations personnelles, compétences

#### ✅ Véhicules
- **Images** : Photo véhicule + Logo fabricant
- **QR Code** : ❌ Supprimé (pas pertinent)
- **Détails** : Caractéristiques, maintenance, assignations

#### ✅ Clients
- **Images** : Avatar selon type (Particulier, Entreprise, Administration, Association)
- **QR Code** : ❌ Supprimé
- **Détails** : Coordonnées, historique, contrats

#### ✅ SAV (Service Après-Vente)
- **QR Code** : ✅ Conservé pour traçabilité
- **Détails** : Demandes, statuts, techniciens assignés

#### ✅ Ventes & Installations
- **Images** : Selon projet
- **QR Code** : ❌ Supprimé
- **Détails** : Projets, affaires, équipes

#### ✅ Contrats
- **QR Code** : ❌ Supprimé
- **Détails** : Informations contractuelles

### Spécifications Images

| Module | QR Code | Images | Status |
|--------|---------|--------|--------|
| **Équipements** | ✅ | Photo + Logo fabricant | ✅ Conforme |
| **SAV** | ✅ | Selon équipement | ✅ Conforme |
| **Véhicules** | ❌ | Photo + Logo fabricant | ✅ Conforme |
| **Personnel** | ❌ | Avatar par poste | ✅ Conforme |
| **Clients** | ❌ | Avatar par type | ✅ Conforme |
| **Ventes & Installations** | ❌ | Selon projet | ✅ Conforme |
| **Contrats** | ❌ | Détails contrat | ✅ Conforme |

## 📊 Dashboard et Statistiques

### Implémentation Dashboard
- **Cartes statistiques** : Vue d'ensemble des modules
- **Graphiques** : Répartition des statuts
- **Couleurs harmonisées** : Cohérence visuelle
- **Données temps réel** : Connexion backend

### Indicateurs Clés
- Équipements par statut
- Demandes SAV ouvertes
- Projets en cours
- Personnel disponible
- Véhicules assignés

## 🔍 Système de Filtres

### Filtres Génériques
- **Recherche textuelle** : Tous les modules
- **Filtres par statut** : Disponible/Indisponible/Maintenance
- **Filtres par catégorie** : Hiérarchiques
- **Filtres par date** : Plages configurables

### Corrections Appliquées
- **NPE** : Protection contre NullPointerException
- **Incohérences** : Logique de filtrage unifiée
- **Performance** : Optimisation requêtes
- **UX** : Interface de filtrage intuitive

## 🔧 Corrections et Optimisations

### Doublons SAV Supprimés
- Boutons en double dans toolbar
- Méthodes redondantes
- Imports inutilisés nettoyés

### Harmonisation Véhicules
- Toolbar standardisée
- Filtres cohérents avec autres modules
- Suppression doublons interface

### Corrections Parc Matériel
- Filtres optimisés
- Statuts harmonisés
- Interface unifiée

### Ventes & Installations
- Implémentation volet visualisation
- Import PDF affaires
- Gestion équipes projets

## 🎯 Architecture de Données

### Entités Principales
- **Equipment** : Matériel, QR codes, photos
- **Personnel** : Employés, compétences, planning
- **Vehicle** : Flotte, maintenance, assignations
- **Client** : Prospects/clients, contacts
- **ServiceRequest** : Demandes SAV, interventions
- **Project** : Ventes, installations, contrats
- **Contract** : Contrats clients, facturation

### Relations
- Equipment ↔ ServiceRequest (1:N)
- Client ↔ Project (1:N)
- Personnel ↔ Project (N:N équipes)
- Vehicle ↔ Personnel (assignations)

## 🚀 Performance et Optimisations

### Mémoire
- Lazy loading des images
- Cache intelligent
- Gestion mémoire JavaFX optimisée

### Interface
- Animations fluides (300ms)
- Responsive design
- États de chargement

### Backend
- H2 Database optimisée
- Connexion pool
- Virtual Threads Java 21

## ⚙️ Configuration et Déploiement

### Prérequis
- Java 17+ (recommandé Java 21)
- Node.js 18+
- VS Code avec extensions Java/TypeScript

### Build et Exécution
```bash
# Build complet
./gradlew build

# Backend (port 8080)
./gradlew :backend:bootRun

# Desktop JavaFX
./gradlew :desktop-javafx:run

# Web frontend (port 3000)
cd web-frontend && npm start
```

### Tests
- Tests unitaires : `./gradlew test`
- Tests d'intégration : Module `integration-tests`
- Tests E2E : Cypress (web-frontend)

---

**Version** : MAGSAV-3.0  
**Dernière mise à jour** : 6 novembre 2025  
**Architecture** : Multi-plateforme (Desktop JavaFX + Web React)  
**Status** : ✅ Production Ready