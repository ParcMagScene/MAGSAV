# Configuration de Tests MAGSAV

## Structure des Tests

```
src/test/java/
├── com/magsav/
│   ├── unit/           # Tests unitaires (services, utils)
│   ├── integration/    # Tests d'intégration (DB, fichiers)
│   ├── ui/            # Tests interface utilisateur (TestFX)
│   └── performance/   # Tests de performance
```

## Stratégie de Tests

### Tests Unitaires (Priorité 1)
- **Services** : Logique métier isolée
- **Utilitaires** : Fonctions pures
- **Validation** : Règles métier
- **Formatage** : Transformations de données

### Tests d'Intégration (Priorité 2)
- **Repository** : Interactions avec la DB
- **Media Service** : Manipulation de fichiers
- **Navigation** : Flux entre écrans
- **Import/Export** : Traitement de données externes

### Tests UI (Priorité 3)
- **Controllers** : Logique de présentation
- **Navigation** : Flux utilisateur
- **Validation** : Feedback utilisateur
- **Responsive** : Adaptabilité interface

## Configuration Gradle Avancée

```gradle
dependencies {
    // Tests unitaires (déjà configuré)
    testImplementation platform("org.junit:junit-bom:5.11.0")
    testImplementation "org.junit.jupiter:junit-jupiter"
    testImplementation "org.mockito:mockito-core:5.8.0"
    testImplementation "org.mockito:mockito-junit-jupiter:5.8.0"
    
    // Tests d'intégration
    testImplementation "org.testcontainers:junit-jupiter:1.19.3"
    testImplementation "org.testcontainers:sqlite:1.19.3"
    
    // Tests UI JavaFX
    testImplementation "org.testfx:testfx-junit5:4.0.16-alpha"
    testImplementation "org.testfx:openjfx-monocle:jdk-12.0.1+2"
    
    // Tests de performance
    testImplementation "org.openjdk.jmh:jmh-core:1.37"
    testImplementation "org.openjdk.jmh:jmh-generator-annprocess:1.37"
    
    // Couverture de code
    testImplementation "org.jacoco:org.jacoco.agent:0.8.8"
    
    // Assertions avancées
    testImplementation "org.assertj:assertj-core:3.24.2"
}

test {
    useJUnitPlatform()
    
    // Configuration de tests
    maxParallelForks = Runtime.runtime.availableProcessors()
    forkEvery = 100
    
    // JVM options pour JavaFX
    jvmArgs = [
        '--add-exports=javafx.graphics/com.sun.javafx.application=ALL-UNNAMED',
        '--add-opens=javafx.graphics/com.sun.glass.ui=ALL-UNNAMED',
        '--add-exports=javafx.base/com.sun.javafx.logging=ALL-UNNAMED'
    ]
    
    // Variables d'environnement pour tests
    systemProperty 'testfx.robot', 'glass'
    systemProperty 'testfx.headless', 'true'
    systemProperty 'prism.order', 'sw'
    systemProperty 'prism.text', 't2k'
    
    // Rapports
    reports {
        html.required = true
        junitXml.required = true
    }
    
    finalizedBy jacocoTestReport
}

// Configuration JaCoCo pour la couverture
jacoco {
    toolVersion = "0.8.8"
}

jacocoTestReport {
    dependsOn test
    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
    
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                'com/magsav/App.class',           // Point d'entrée
                'com/magsav/model/**',            // Records/DTOs
                'com/magsav/gui/**/*Controller.class' // UI (TestFX séparé)
            ])
        }))
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = 0.80  // 80% de couverture minimum
            }
        }
        rule {
            element = 'CLASS'
            excludes = ['com.magsav.App', 'com.magsav.model.*']
            limit {
                counter = 'LINE'
                minimum = 0.70  // 70% par classe minimum
            }
        }
    }
}

check.dependsOn jacocoTestCoverageVerification
```

## Profils de Tests

### Profile Rapide (CI/CD)
```bash
./gradlew test -Dtest.profile=fast
# Exclut : tests UI, performance, intégration lourde
```

### Profile Complet (Release)
```bash
./gradlew test -Dtest.profile=full
# Inclut : tous les tests
```

### Profile Performance
```bash
./gradlew test -Dtest.profile=performance
# Seulement : benchmarks et tests de charge
```

## Utilitaires de Test

### TestDataBuilder Pattern
```java
public class ProductTestDataBuilder {
    private String nom = "Produit Test";
    private String uid = "TST1234";
    // ...
    
    public ProductTestDataBuilder withNom(String nom) {
        this.nom = nom;
        return this;
    }
    
    public Product build() {
        return new Product(nom, uid, ...);
    }
}
```

### Database Test Utils
```java
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DatabaseTest {
    protected static final String TEST_DB = ":memory:";
    protected DB database;
    
    @BeforeAll
    void setupDatabase() {
        database = new DB(TEST_DB);
        database.initializeTables();
    }
    
    @BeforeEach
    void cleanDatabase() {
        // Nettoyage entre tests
    }
}
```

### File System Test Utils
```java
public class TestFileSystemHelper {
    private final Path tempDir;
    
    @TempDir
    static Path tempDirectory;
    
    public void createTestMediaStructure() {
        // Crée structure médias temporaire
    }
}
```

## Métriques et Reporting

### Seuils de Qualité
- **Couverture de code** : 80% minimum
- **Complexité cyclomatique** : ≤ 10 par méthode
- **Duplication** : ≤ 3% du code
- **Tests par service** : ≥ 5 tests

### Dashboard de Tests
- **SonarQube** ou **Codecov** pour analyse continue
- **Rapport HTML** généré automatiquement
- **Tendances** de couverture et qualité
- **Alertes** sur régression de qualité

## Commandes Utiles

```bash
# Exécuter tous les tests
./gradlew test

# Tests avec couverture
./gradlew test jacocoTestReport

# Tests spécifiques
./gradlew test --tests "com.magsav.service.*"

# Tests en continu
./gradlew test --continuous

# Rapport de couverture
open build/reports/jacoco/test/html/index.html

# Rapport de tests
open build/reports/tests/test/index.html
```

---
*Configuration générée pour MAGSAV 1.2 - Tests Strategy*