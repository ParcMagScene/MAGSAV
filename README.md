# MAGSAV 1.2

Application de gestion SAV (Service Après-Vente) développée en JavaFX pour macOS.

## 🎯 Objectif

MAGSAV permet de gérer efficacement :
- **Produits** : Inventaire avec photos, logos, numéros de série
- **Interventions** : Suivi SAV complet avec historique
- **Entités** : Clients, fournisseurs, fabricants
- **Import CSV** : Données en lot avec support français
- **Média** : Gestion automatisée des images

## 🏗️ Architecture

- **Interface** : JavaFX 21 (native macOS)
- **Base de données** : SQLite (~/MAGSAV/MAGSAV.db)
- **Build** : Gradle 8.10.x avec JDK 21
- **Logging** : SLF4J + Logback

### Structure du projet
```
src/main/java/com/magsav/
├── gui/           # Contrôleurs JavaFX
├── service/       # Logique métier
├── repo/          # Accès données
├── model/         # Entités
├── imports/       # Import CSV
└── util/          # Utilitaires
```

## 🚀 Démarrage rapide

### Prérequis
- macOS (Apple Silicon)
- JDK 21

### Installation
```bash
git clone [repo]
cd MAGSAV-1.2
./gradlew run
```

### Import CSV
L'application supporte l'import CSV avec colonnes françaises :
- **Produits** : PRODUIT, N° DE SERIE, FABRICANT, SITUATION
- **Interventions** : STATUS, PANNE, DATE ENTREE, DATE SORTIE, DETECTEUR, N° SUIVI

## 📁 Base de données

Emplacement : `~/MAGSAV/MAGSAV.db`

### Tables principales
- `produits` : Inventaire des produits
- `interventions` : Historique SAV
- `societes` : Clients/Fournisseurs/Fabricants
- `categories` : Classification produits

## 🧪 Tests

```bash
./gradlew test
```

## 📝 Documentation

- [Documentation complète](DOCUMENTATION.md)
- [Historique des changements](CHANGELOG.md)

## 🔧 Développement

### Compilation
```bash
./gradlew compileJava
```

### Nettoyage
```bash
./gradlew clean
```

## 📄 Licence

© 2025 - Projet MAGSAV
