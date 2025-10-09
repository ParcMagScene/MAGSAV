# Changelog MAGSAV

Toutes les modifications notables de ce projet sont documentées dans ce fichier.

## [1.2.0] - 2025-10-07

### ✨ Nouveautés
- **Import CSV** : Support complet des colonnes françaises (N° DE SERIE, N° SUIVI, etc.)
- **Base de données** : Ajout des champs `detecteur` et `suivi_no` dans les interventions
- **Architecture** : Refactorisation de l'importeur CSV avec gestion d'erreurs robuste
- **Nettoyage** : Suppression des fichiers obsolètes et doublons

### 🔧 Améliorations
- **CsvImporter** : Normalisation automatique des en-têtes français
- **Gestion des erreurs** : Logs détaillés pour l'import CSV
- **Performance** : Optimisation de l'importeur avec callbacks de progression
- **Code** : Suppression de `EnhancedCsvImporter` au profit d'un `CsvImporter` unifié

### 🐛 Corrections
- **Import CSV** : Résolution des 345 erreurs d'import
- **Mapping colonnes** : Correction du mapping "N° DE SERIE" → "n_de_serie"
- **Base de données** : Suppression de la base vide `/magsav.db` en doublon
- **Compilation** : Résolution des erreurs de compilation après refactoring

### 🗂️ Structure
- **Documentation** : Consolidation en 3 fichiers principaux
- **Fichiers** : Suppression des fichiers .bak et temporaires
- **Organisation** : Nettoyage de la structure du projet

## [1.1.0] - 2025-10-06

### ✨ Nouveautés
- **Interface unifiée** : Gestion des entités (clients, fournisseurs, fabricants)
- **Composants UI** : Bibliothèque de composants réutilisables
- **Gestion médias** : Système complet de gestion des images
- **Tests** : Suite de tests complète avec couverture étendue

### 🔧 Améliorations
- **Performance** : Optimisations base de données avec index automatiques
- **UI/UX** : Interface utilisateur modernisée avec validation temps réel
- **Architecture** : Séparation claire des couches (GUI/Service/Repository)
- **Logging** : Système de logging centralisé avec SLF4J

### 🐛 Corrections
- **Stabilité** : Résolution des problèmes de performance
- **Navigation** : Amélioration de la navigation entre les vues
- **Validation** : Correction des validations de formulaires

## [1.0.0] - 2025-10-02

### ✨ Version initiale
- **Architecture** : Application JavaFX 21 pour macOS
- **Base de données** : SQLite avec migrations automatiques
- **Fonctionnalités** :
  - Gestion des produits avec photos
  - Suivi des interventions SAV
  - Import de données CSV
  - Génération de rapports

### 🏗️ Infrastructure
- **Build** : Gradle 8.10.x avec JDK 21
- **Tests** : JUnit 5 + AssertJ + Mockito
- **Qualité** : Configuration Spotless et SpotBugs
- **Documentation** : Documentation technique complète

## Versions antérieures

### [0.9.x] - Développement initial
- Prototypage de l'interface
- Architecture de base
- Premiers tests utilisateur

---

## Types de changements
- ✨ **Nouveautés** : Nouvelles fonctionnalités
- 🔧 **Améliorations** : Améliorations de fonctionnalités existantes  
- 🐛 **Corrections** : Corrections de bugs
- 🗂️ **Structure** : Changements de structure de projet
- 🏗️ **Infrastructure** : Changements d'infrastructure/build
- 📚 **Documentation** : Mises à jour de documentation
- ⚡ **Performance** : Améliorations de performance
- 🔒 **Sécurité** : Corrections de sécurité

## Format
Ce changelog suit le format [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet respecte le [Semantic Versioning](https://semver.org/lang/fr/).