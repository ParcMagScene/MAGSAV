# ARCHITECTURE MAGSAV - NETTOYAGE ET OPTIMISATION

## 📊 STRUCTURE BASE DE DONNÉES (SIMPLIFIÉE)

### Tables Principales (H2DB)
```sql
-- CORE BUSINESS
- users           # Utilisateurs (techniciens, admins, intermittents)
- societes        # Clients, fournisseurs, fabricants
- produits        # Matériel et équipements
- categories      # Classification des produits

-- OPÉRATIONS
- interventions   # SAV et maintenances
- requests        # Demandes (pièces, matériel, interventions)
- vehicules       # Flotte de véhicules

-- BUSINESS
- affaires        # Projets clients
- devis           # Devis et lignes_devis
- commandes       # Commandes et lignes_commandes

-- STOCK & LOGISTIQUE
- mouvements_stock     # Mouvements de stock
- alertes_stock        # Alertes de rupture

-- PLANIFICATION
- planifications              # Planning interventions
- disponibilites_techniciens  # Disponibilités

-- COMMUNICATION
- communications    # Messages et notifications
- email_templates   # Templates d'emails

-- CONFIG
- configuration_google  # Config services Google
```

## 🗑️ FICHIERS À SUPPRIMER

### 1. Dossier DEBUG complet
- Tous les fichiers debug/* (9 fichiers)
- Classes de test/diagnostic obsolètes

### 2. Migrations obsolètes  
- migration/MigrationRunner.java (duplication avec H2DB)
- migration/Migration.java
- EntityMigration.java (obsolète)

### 3. Classes dupliquées/inutilisées
- AffairesService création de tables (duplication H2DB)

## 🔄 ARCHITECTURE SERVICES (OPTIMISÉE)

### Pattern Repository (Couche Données)
```
repo/
├── BaseRepository.java       # Classe de base commune
├── ProductRepository.java    # ✅ Keep
├── SocieteRepository.java    # ✅ Keep  
├── InterventionRepository.java # ✅ Keep
├── CategoryRepository.java   # ✅ Keep
├── RequestRepository.java    # ✅ Keep
└── GoogleServicesConfigRepository.java # ✅ Keep
```

### Pattern DataService (Couche Métier)
```
service/data/ (UNIFIÉ)
├── DataServiceManager.java  # 🆕 Factory central
├── UserService.java         # Rename UserDataService
├── ClientService.java       # Rename ClientDataService  
├── ProductService.java      # Nouveau
├── RequestService.java      # Rename RequestDataService
└── CompanyService.java      # Rename CompanyDataService
```

### Pattern Controller (Couche Présentation)
```
gui/controllers/
├── BaseController.java      # 🆕 Classe de base
├── UsersController.java     # ✅ Simplifié
├── GestionController.java   # ✅ Keep
├── DemandesController.java  # ✅ Keep
└── InterventionsController.java # ✅ Keep
```

## 🎯 ACTIONS PRIORITAIRES

1. **SUPPRIMER** tous les fichiers debug/
2. **SUPPRIMER** migrations obsolètes
3. **CENTRALISER** la gestion des données
4. **STANDARDISER** les interfaces
5. **DOCUMENTER** l'architecture finale

## 📈 BÉNÉFICES ATTENDUS

- ⚡ Développement plus rapide
- 🐛 Moins de bugs d'affichage  
- 🔧 Maintenance simplifiée
- 📋 Code plus lisible
- 🎯 Architecture claire