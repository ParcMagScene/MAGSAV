# Documentation Technique MAGSAV 1.2

## Architecture et Design

### Vue d'ensemble

MAGSAV 1.2 est une application JavaFX moderne utilisant une architecture en couches avec séparation claire des responsabilités :

```
┌─────────────────┐
│   Interface     │  GUI (JavaFX/FXML)
│   Utilisateur   │
├─────────────────┤
│   Contrôleurs   │  Controllers (JavaFX)
│                 │
├─────────────────┤
│   Services      │  Business Logic
│                 │
├─────────────────┤
│   Repositories  │  Data Access Layer
│                 │
├─────────────────┤
│   Base de       │  SQLite Database
│   Données       │
└─────────────────┘
```

### Structure des packages

```
com.magsav/
├── gui/                    # Contrôleurs JavaFX
│   ├── enhanced/          # Contrôleurs avancés (Phase 4)
│   └── ...
├── ui/                    # Composants UI réutilisables (Phase 4)
│   └── components/        
│       ├── AlertManager.java
│       ├── FormValidator.java
│       ├── NotificationManager.java
│       ├── ErrorManager.java
│       ├── FormDialogManager.java
│       ├── EnhancedTableView.java
│       └── ConfigurationManager.java
├── service/               # Services métier
│   ├── ProductServiceStatic.java
│   ├── DataCacheService.java
│   └── PerformanceMetricsService.java
├── repo/                  # Accès aux données
│   ├── ProductRepository.java
│   ├── SocieteRepository.java
│   └── InterventionRepository.java
├── model/                 # Modèles de données
├── db/                    # Gestion base de données
├── util/                  # Utilitaires
└── imports/               # Import de données
```

## Composants UI (Phase 4)

### AlertManager

Gestionnaire centralisé pour tous les dialogs et alertes de l'application.

#### API principale
```java
// Méthodes statiques pour faciliter l'utilisation
AlertManager.showInfo(String title, String message)
AlertManager.showWarning(String title, String message)
AlertManager.showError(String title, String message)
AlertManager.showConfirmation(String title, String message) -> boolean

// Méthodes MAGSAV spécialisées
AlertManager.MAGSAV.showSaveSuccess()
AlertManager.MAGSAV.showDeleteConfirmation() -> boolean
AlertManager.MAGSAV.showValidationError(List<String> errors)
```

#### Utilisation
```java
// Dialog simple
AlertManager.showInfo("Succès", "Produit sauvegardé avec succès");

// Confirmation avec retour
if (AlertManager.showConfirmation("Suppression", "Confirmer la suppression ?")) {
    // Procéder à la suppression
}

// Messages MAGSAV standardisés
AlertManager.MAGSAV.showSaveSuccess();
```

### FormValidator

Système de validation de formulaires avec feedback visuel en temps réel.

#### Créateur de validateur
```java
FormValidator validator = new FormValidator.Builder()
    .addTextFieldRule(nomField, "Nom requis", text -> !text.trim().isEmpty())
    .addTextFieldRule(emailField, "Email invalide", 
        text -> text.matches("^[A-Za-z0-9+_.-]+@(.+)$"))
    .addComboBoxRule(typeCombo, "Type requis", Objects::nonNull)
    .build();
```

#### Validation
```java
// Validation complète avec affichage des erreurs
if (validator.validateAndShow()) {
    // Formulaire valide, procéder
} else {
    // Erreurs affichées automatiquement
}

// Validation silencieuse
boolean isValid = validator.validate();
List<String> errors = validator.getErrors();
```

#### Validateurs prédéfinis MAGSAV
```java
// UID produit
FormValidator.Validators.MAGSAV.isValidUID(String uid)

// Code produit
FormValidator.Validators.MAGSAV.isValidProductCode(String code)

// Numéro de série
FormValidator.Validators.MAGSAV.isValidSerialNumber(String sn)
```

### NotificationManager

Système de notifications toast non-intrusives avec animations.

#### Types de notifications
```java
NotificationManager.showSuccess("Opération réussie");
NotificationManager.showError("Erreur survenue");
NotificationManager.showInfo("Information importante");
NotificationManager.showWarning("Attention requise");
```

#### Configuration avancée
```java
NotificationManager.showCustom(
    "Titre personnalisé",
    "Message détaillé",
    NotificationManager.NotificationType.SUCCESS,
    Duration.seconds(3)
);
```

### ErrorManager

Gestionnaire global d'erreurs avec rapports structurés et logging automatique.

#### Signalement d'erreurs
```java
// Erreur simple
ErrorManager.reportError(ErrorLevel.WARNING, "Module Produits", 
    "Produit non trouvé", null);

// Erreur avec exception
ErrorManager.reportError(ErrorLevel.CRITICAL, "Base de données", 
    "Connexion impossible", sqlException);

// Erreur avec details
ErrorManager.reportError(ErrorLevel.MAJOR, "Validation", 
    "Données incohérentes", null, "Product ID: 123, Expected: > 0");
```

#### Exécution sécurisée
```java
// Wrapper pour éviter les crashes
ErrorManager.MAGSAV.safeExecute("Sauvegarde produit", () -> {
    productRepo.save(product);
    showSuccessNotification();
});

// Avec gestion de retour
String result = ErrorManager.MAGSAV.safeExecuteWithReturn("Recherche produit", 
    () -> productService.findByCode(code), "Produit par défaut");
```

### FormDialogManager

Gestionnaire de dialogs de formulaires réutilisables avec validation intégrée.

#### Dialog générique
```java
FormDialogManager.showFormDialog(
    "Nouveau produit",                    // Titre
    createProductForm(),                  // Node du formulaire
    formValidator,                        // Validateur
    this::saveProduct                     // Action sur OK
);
```

#### Dialog de saisie simple
```java
Optional<String> result = FormDialogManager.showInputDialog(
    "Nom du fabricant", 
    "Entrez le nom :",
    "Fabricant par défaut",
    text -> !text.trim().isEmpty()
);
```

### EnhancedTableView

TableView avec fonctionnalités avancées intégrées.

#### Création avec recherche
```java
EnhancedTableView<Product> table = new EnhancedTableView<>();

// Configuration automatique de la recherche
table.enableSearch(product -> Arrays.asList(
    product.getName(), 
    product.getCode(), 
    product.getManufacturer()
));

// Ajout de colonnes
table.addColumn("Nom", Product::getName);
table.addColumn("Code", Product::getCode);
table.addDateColumn("Créé le", Product::getCreatedDate);
```

#### Fonctionnalités automatiques
- **Recherche** : Filtrage en temps réel
- **Tri** : Toutes les colonnes triables
- **Sélection multiple** : Avec raccourcis clavier
- **Export** : CSV et Excel intégré
- **Pagination** : Gestion automatique des grandes listes

### ConfigurationManager

Gestionnaire de configuration centralisé avec binding JavaFX.

#### Lecture de configuration
```java
// Propriétés booléennes
boolean cacheEnabled = ConfigurationManager.isCacheEnabled();
boolean debugMode = ConfigurationManager.isDebugMode();

// Propriétés de chemin
String dbPath = ConfigurationManager.getDatabasePath();
String backupPath = ConfigurationManager.getBackupPath();

// Propriétés numériques
int cacheSize = ConfigurationManager.getCacheMaxSize();
long ttl = ConfigurationManager.getCacheDefaultTTL();
```

#### Binding avec l'UI
```java
// Binding bidirectionnel avec CheckBox
CheckBox cacheCheckBox = new CheckBox("Activer le cache");
ConfigurationManager.bindCacheEnabled(cacheCheckBox.selectedProperty());

// Binding avec TextField
TextField dbPathField = new TextField();
ConfigurationManager.bindDatabasePath(dbPathField.textProperty());
```

#### Sauvegarde et validation
```java
// Validation de la configuration actuelle
boolean isValid = ConfigurationManager.validateConfiguration();

// Sauvegarde manuelle (automatique par défaut)
ConfigurationManager.saveConfiguration();

// Rechargement depuis le fichier
ConfigurationManager.reloadConfiguration();
```

## Services métier

### ProductServiceStatic

Service principal pour la gestion des produits avec cache intégré.

#### Méthodes principales
```java
// Récupération de tous les produits
List<ProductRow> products = ProductServiceStatic.findAllProducts();

// Recherche avec cache
List<ProductRow> results = ProductServiceStatic.searchProducts("terme");

// Interventions d'un produit
List<InterventionRow> interventions = 
    ProductServiceStatic.getProductInterventions(productId);

// Statistiques
ProductStatistics stats = ProductServiceStatic.getProductStatistics();
```

#### Validation
```java
// Validation UID
ProductServiceStatic.validateUidOrThrow(uid); // Lève exception si invalide

// Vérification existence
boolean exists = ProductServiceStatic.productExists(productId);
```

### DataCacheService

Service de cache intelligent avec gestion automatique de la mémoire.

#### Types de cache
```java
// Cache fabricants (TTL: 1h)
List<String> manufacturers = DataCacheService.getManufacturers();

// Cache catégories (TTL: 30min)
List<String> categories = DataCacheService.getCategories();

// Cache statuts interventions (permanent)
List<String> statuses = DataCacheService.getInterventionStatuses();

// Cache produits (TTL: 15min)
List<ProductRow> products = DataCacheService.getAllProducts();
```

#### Gestion du cache
```java
// Invalidation sélective
DataCacheService.invalidateManufacturers();
DataCacheService.invalidateProducts();

// Statistiques
CacheStatistics stats = DataCacheService.getStatistics();
System.out.println("Entrées: " + stats.productListCacheSize());
System.out.println("Cache fabricants valide: " + stats.manufacturersCacheValid());
```

### PerformanceMetricsService

Service de métriques de performance avec alertes automatiques.

#### Enregistrement de métriques
```java
// Mesure manuelle
long start = System.currentTimeMillis();
// ... opération ...
long duration = System.currentTimeMillis() - start;
PerformanceMetricsService.recordOperation("searchProducts", duration);

// Mesure automatique avec try-with-resources
try (var timer = new PerformanceMetricsService.PerformanceTimer("saveProduct")) {
    productRepo.save(product);
    // timer.markFailure(); // en cas d'erreur
}
```

#### Rapports
```java
// Rapport complet
PerformanceReport report = PerformanceMetricsService.generateReport();
System.out.println("Opérations totales: " + report.totalOperations());
System.out.println("Temps moyen: " + report.averageResponseTime() + "ms");

// Log automatique du résumé
PerformanceMetricsService.logSummary();
```

## Repositories

### ProductRepository

#### Méthodes principales
```java
// CRUD de base
long productId = repo.insert(code, nom, sn, fabricant, uid, situation);
Optional<ProductRow> product = repo.findById(productId);
Optional<ProductRowDetailed> detailed = repo.findDetailedById(productId);

// Recherches
List<ProductRow> products = repo.findAllProductsWithUID();
Optional<ProductRow> bySN = repo.findBySN(serialNumber);
List<ProductRow> byManufacturer = repo.findByFabricant(manufacturer);
```

#### Records de données
```java
// ProductRow : données de base
record ProductRow(long id, String code, String nom, String sn, 
                  String fabricant, String uid, String situation)

// ProductRowDetailed : avec informations étendues  
record ProductRowDetailed(long id, String code, String nom, String sn,
                          String fabricant, String uid, String situation,
                          String photo, String category, String subcategory)
```

### SocieteRepository

#### Gestion des sociétés
```java
// Création
long societeId = repo.insert(type, nom, email, phone, adresse, notes);

// Recherche
List<Societe> fabricants = repo.findByType("FABRICANT");
Optional<Societe> byName = repo.findByNameAndType(nom, type);

// Gestion des fabricants
long manufacturerId = repo.upsertManufacturerByName(manufacturerName);
List<String> manufacturers = repo.listManufacturers();
```

### InterventionRepository

#### Gestion des interventions
```java
// Création
long interventionId = repo.insert(productId, serial, detectorSocieteId, description);

// Recherche
List<InterventionRow> all = repo.findAllWithProductName();
List<InterventionRow> byProduct = repo.findByProductId(productId);

// Gestion du cycle de vie
boolean closed = repo.close(interventionId); // Fermeture automatique
```

## Tests d'intégration

### Structure des tests

Les tests d'intégration valident l'ensemble du workflow MAGSAV :

```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MAGSAVIntegrationTest {
    
    @Test @Order(1) void testCreateManufacturer()
    @Test @Order(2) void testCreateProduct() 
    @Test @Order(3) void testServicesWithCache()
    @Test @Order(4) void testCreateIntervention()
    @Test @Order(5) void testDataRelationsAndConsistency()
    @Test @Order(6) void testPerformanceAndOptimization()
    @Test @Order(7) void testConfiguration()
    @Test @Order(8) void testRobustnessAndErrorHandling()
    @Test @Order(9) void testCleanupAndShutdown()
    @Test @Order(10) void testSummary()
}
```

### Exécution des tests

```bash
# Compilation des tests
./gradlew compileTestJava

# Exécution des tests d'intégration
./gradlew test --tests "*.integration.*"

# Tests avec rapports détaillés
./gradlew test --info
```

## Configuration et déploiement

### Structure de configuration

```
config/
├── application.properties    # Configuration principale
├── ui.properties            # Paramètres interface
├── cache.properties         # Configuration cache
└── logging.properties       # Configuration logs
```

### Propriétés importantes

```properties
# Base de données
db.path=./data/magsav.db
db.backup.enabled=true
db.backup.interval=24h

# Cache
cache.enabled=true
cache.max.size=100MB
cache.manufacturers.ttl=1h
cache.products.ttl=15m

# Performance
performance.monitoring=true
performance.slow.threshold=1000ms
performance.alerts.enabled=true

# Interface
ui.theme=light
ui.notifications.duration=5s
ui.validation.realtime=true
```

### Scripts de build

```bash
# Build complet
./gradlew build

# Package de distribution
./gradlew distZip

# Exécution en développement
./gradlew run

# Tests uniquement
./gradlew test
```

## Maintenance et monitoring

### Logs structurés

```java
// Utilisation d'AppLogger
AppLogger.info("cache", "Cache rechargé: {} entrées", count);
AppLogger.warn("performance", "Opération lente: {}ms", duration);
AppLogger.debug("sql", "Requête exécutée: {}", sql);
```

### Métriques clés à surveiller

1. **Performance**
   - Temps de réponse moyen < 500ms
   - Opérations lentes < 5% du total
   - Hit rate cache > 80%

2. **Mémoire**
   - Utilisation < 80% heap max
   - Pas de memory leaks
   - GC pauses < 100ms

3. **Base de données**
   - Taille fichier DB
   - Temps de requête
   - Intégrité des données

### Procédures de maintenance

#### Sauvegarde automatique
```java
// Configuration dans ConfigurationManager
ConfigurationManager.setBackupEnabled(true);
ConfigurationManager.setBackupInterval(Duration.ofHours(24));
```

#### Compactage DB
```bash
# Via l'interface MAGSAV
Menu Outils > Maintenance > Compacter la base

# Ou directement SQLite
sqlite3 magsav.db "VACUUM;"
```

#### Nettoyage des logs
```bash
# Suppression logs > 30 jours
find logs/ -name "*.log" -mtime +30 -delete

# Rotation automatique configurée dans logging.properties
```

## Bonnes pratiques de développement

### Architecture

1. **Séparation des couches** : UI, Service, Repository, Model
2. **Injection de dépendance** : Via constructeurs ou factory
3. **Gestion d'erreur** : ErrorManager centralisé
4. **Validation** : FormValidator pour toutes les saisies

### Performance

1. **Cache intelligent** : DataCacheService pour données fréquentes
2. **Lazy loading** : Chargement à la demande
3. **Métriques** : PerformanceMetricsService pour monitoring
4. **Optimisation DB** : Index appropriés, requêtes optimisées

### UI/UX

1. **Composants réutilisables** : Package ui.components
2. **Validation temps réel** : FormValidator
3. **Feedback utilisateur** : NotificationManager + AlertManager
4. **Gestion d'erreur** : Messages clairs et actions possibles

### Code qualité

```java
// Documentation JavaDoc
/**
 * Recherche des produits par terme de recherche
 * @param searchTerm Terme de recherche (null = tous)
 * @return Liste des produits correspondants
 * @throws IllegalArgumentException si terme invalide
 */
public List<ProductRow> searchProducts(String searchTerm);

// Validation des paramètres
Objects.requireNonNull(productId, "Product ID ne peut pas être null");
Preconditions.checkArgument(productId > 0, "Product ID doit être positif");

// Gestion d'erreur appropriée
try {
    return productRepo.findById(id);
} catch (SQLException e) {
    ErrorManager.reportError(ErrorLevel.CRITICAL, "Database", 
        "Impossible de charger le produit", e);
    throw new ServiceException("Erreur lors du chargement", e);
}
```

---

*Documentation Technique MAGSAV 1.2*
*Version 1.0 - Janvier 2024*