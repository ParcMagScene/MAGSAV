# Plan de Refactorisation MAGSAV 1.2

## Objectif
Améliorer la qualité du code, la maintenabilité et la testabilité de l'application MAGSAV en suivant les recommandations du rapport de santé du projet.

## Phase 1 : Infrastructure et Logging (Priorité CRITIQUE)
**Durée estimée : 2-3 jours**

### 1.1 Système de Logging Centralisé
- [ ] **Remplacer System.out.println par un système de logging structuré**
  - Utiliser SLF4J + Logback (déjà dans les dépendances)
  - Créer une classe `Logger` utilitaire
  - Définir les niveaux : DEBUG, INFO, WARN, ERROR
  - Configuration via `logback.xml`

- [ ] **Stratégie de logs par couche**
  - Repository : logs SQL et erreurs DB
  - Services : logs métier et validation
  - Controllers : logs navigation et actions utilisateur
  - Utilitaires : logs techniques

### 1.2 Gestion d'Erreurs Typées
- [ ] **Créer des exceptions métier spécifiques**
  ```java
  // Remplacer RuntimeException par :
  ProductNotFoundException, InvalidUidException, 
  MediaFileException, DatabaseException
  ```

- [ ] **Handler d'erreurs global**
  - Centraliser la gestion des erreurs
  - Messages utilisateur cohérents
  - Logging automatique des erreurs

### 1.3 Configuration Externalisée
- [ ] **Créer une classe `AppConfig`**
  - Chemins médias configurables
  - Paramètres DB
  - Constantes métier
  - Chargement depuis `application.properties`

## Phase 2 : Architecture et Services (Priorité MAJEURE)
**Durée estimée : 3-4 jours**

### 2.1 Refactorisation du MainController
- [ ] **Extraire la logique métier en services**
  ```java
  ProductService, InterventionService, NavigationService
  ```

- [ ] **Séparer les responsabilités**
  - Navigation : `NavigationController`
  - Product management : `ProductController`
  - Data display : `MainViewController`

### 2.2 Couche Service Complète
- [ ] **ProductService** (déjà commencé)
  - Validation des données
  - Règles métier
  - Orchestration des repositories

- [ ] **MediaService** (améliorer l'existant)
  - Gestion unifiée des fichiers
  - Validation des formats
  - Redimensionnement d'images
  - Cache des images

- [ ] **InterventionService**
  - Workflow des interventions
  - Calculs et statistiques
  - Notifications

### 2.3 Repository Pattern Amélioré
- [ ] **Interface Repository**
  ```java
  interface Repository<T, ID> {
      Optional<T> findById(ID id);
      List<T> findAll();
      void save(T entity);
      void delete(ID id);
  }
  ```

- [ ] **Requêtes typées**
  - Criteria Builder pattern
  - Query Objects
  - Pagination support

## Phase 3 : Tests et Qualité (Priorité IMPORTANTE)
**Durée estimée : 4-5 jours**

### 3.1 Stratégie de Tests
- [ ] **Objectif : 80%+ de couverture**

### 3.2 Tests Unitaires (Couche Service)
- [ ] **ProductService** (déjà commencé)
- [ ] **IdService** (déjà fait)
- [ ] **MediaService**
- [ ] **InterventionService**
- [ ] **Utilitaires** (déjà commencé)

### 3.3 Tests d'Intégration
- [ ] **Repository Tests** (améliorer existants)
- [ ] **Database Integration**
- [ ] **File System Operations**
- [ ] **End-to-End Scenarios**

### 3.4 Tests JavaFX
- [ ] **TestFX Setup**
  ```gradle
  testImplementation 'org.testfx:testfx-junit5:4.0.16-alpha'
  ```
- [ ] **Controller Tests**
- [ ] **UI Component Tests**
- [ ] **Navigation Tests**

## Phase 4 : Performance et Monitoring (Priorité MINEURE)
**Durée estimée : 2-3 jours**

### 4.1 Optimisations
- [ ] **Connection Pooling** (HikariCP déjà configuré)
- [ ] **Image Caching**
- [ ] **Lazy Loading des données**
- [ ] **Index DB optimisés**

### 4.2 Monitoring
- [ ] **Métriques applicatives**
  - Temps de réponse
  - Utilisation mémoire
  - Nombre d'opérations

- [ ] **Health Checks**
  - DB connectivity
  - File system access
  - Services availability

## Estimation Globale
- **Durée totale : 11-15 jours ouvrés**
- **Effort : ~80-120 heures**
- **ROI attendu : +200% maintenabilité, -70% bugs**

## Métriques de Succès
- [ ] **Code Quality**
  - 0 `System.out.println` en production
  - 0 `RuntimeException` non spécifiques
  - 80%+ test coverage

- [ ] **Performance**
  - Démarrage < 3 secondes
  - Navigation < 500ms
  - Recherche < 1 seconde

- [ ] **Maintenabilité**
  - Complexité cyclomatique < 10
  - Couplage faible entre couches
  - Documentation API complète

## Actions Immédiates Post-Weekend
1. **Lundi : Commencer Phase 1.1** (Logging)
2. **Créer une branche `refactoring-phase1`**
3. **Setup CI/CD** pour tests automatiques
4. **Documentation des APIs** existantes

## Notes d'Implémentation
- **Approche incrémentale** : pas de big bang
- **Tests d'abord** : TDD pour nouveaux services
- **Compatibilité** : maintenir fonctionnalités existantes
- **Monitoring** : métriques de progression quotidiennes

---
*Plan généré automatiquement le ${new Date().toISOString().split('T')[0]}*