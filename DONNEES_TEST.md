# 📊 Générateurs de Données de Test - MAGSAV 1.2

## 🚀 Démarrage Rapide

### Générer des données de test complètes
```bash
./gradlew generateTestData
```

### Régénérer des données fraîches (supprime l'existant)
```bash
./gradlew generateFreshData
```

## 📈 Données Générées

Le système génère **490+ enregistrements** répartis sur **15 tables** :

- **20 catégories** hiérarchiques avec emojis (🎤 Microphones, 🔊 Haut-parleurs, etc.)
- **50 sociétés** françaises (clients, fournisseurs, partenaires)  
- **10 techniciens** avec spécialités (Audio, Vidéo, Éclairage, HF)
- **8 véhicules** de flotte (VL, PL, SPL, remorques, scènes mobiles)
- **100 produits** audiovisuels (Yamaha, Pioneer, JBL, Shure, etc.)
- **30 interventions** SAV avec statuts variés
- **25 planifications** d'interventions avec techniciens et véhicules
- **15 commandes** fournisseurs avec **45 lignes détaillées**
- **80 mouvements** de stock (entrées, sorties, ajustements)
- **12 alertes** de stock (ruptures, stock bas)
- **20 disponibilités** de techniciens (congés, formations)
- **35 communications** (emails, SMS, appels, réunions)
- **40 entrées** d'historique SAV externe
- **Templates d'emails** préconfiguré

## 🎯 Utilisation

### Pour le Développement
```bash
# Première fois - génère les données
./gradlew generateTestData

# Développement en cours - ajoute aux données existantes  
./gradlew generateTestData
```

### Pour les Tests
```bash
# Tests nécessitant des données fraîches
./gradlew generateFreshData
```

### Vérification des Données
```bash
# Compter les enregistrements
sqlite3 data/MAGSAV.db "SELECT 'produits:', COUNT(*) FROM produits; SELECT 'interventions:', COUNT(*) FROM interventions;"

# Voir des échantillons
sqlite3 data/MAGSAV.db "SELECT nom_produit, nom_fabricant FROM produits LIMIT 5;"
```

## ✨ Caractéristiques

### Réalisme Français
- **Noms de personnes** français (Jean Martin, Marie Dubois, etc.)
- **Sociétés** avec noms commerciaux réalistes
- **Adresses** françaises complètes avec codes postaux
- **Numéros de téléphone** au format français
- **Immatriculations** de véhicules conformes

### Cohérence Métier
- **Relations logiques** entre toutes les tables
- **Dates cohérentes** (2020-2024)
- **Statuts métier** appropriés
- **Prix en euros** réalistes
- **Références produits** avec numéros de série

### Diversité des Données
- **Fabricants connus** (Yamaha, Pioneer, JBL, Sennheiser, Shure)
- **Catégories avec emojis** pour faciliter la navigation  
- **Statuts variés** (En cours, Terminé, Annulé, etc.)
- **Types de véhicules** adaptés au métier
- **Spécialités techniques** réalistes

## 🔧 Personnalisation

Les générateurs peuvent être modifiés dans `src/main/java/com/magsav/util/` :

- **TestDataGenerator.java** - Générateur principal
- **ComprehensiveTestDataGenerator.java** - Interface Gradle
- **FreshDataGenerator.java** - Nettoyage + génération
- **DataGeneratorRunner.java** - Interface simple

### Modifier les Quantités
```java
// Dans TestDataGenerator.generateCompleteTestData()
generateCategories(30);      // Au lieu de 20
generateSocietes(100);       // Au lieu de 50  
generateProduits(200);       // Au lieu de 100
```

### Ajouter des Fabricants
```java
// Dans TestDataGenerator
private static final List<String> FABRICANTS = Arrays.asList(
    "Yamaha", "Pioneer", "JBL", "Sennheiser", "Shure", "AKG", 
    "VotreNouvelleMaique" // ← Ajouter ici
);
```

## 📋 Intégration

### Tests Automatisés
```bash
# Dans vos scripts de test
./gradlew generateFreshData  # Données propres
./gradlew test              # Lancer les tests
```

### Démos et Formations
```bash
# Préparer une démonstration
./gradlew generateFreshData
./gradlew run
```

### CI/CD
```yaml
# GitHub Actions / GitLab CI
- name: Generate test data
  run: ./gradlew generateTestData
```

## ⚡ Performances

- **Génération rapide** : ~1-2 secondes pour 490+ enregistrements
- **Pas de requêtes lentes** : insertion directe sans jointures complexes
- **Contraintes respectées** : clés étrangères valides
- **Transactions optimisées** : une connexion par table

## 🛠️ Troubleshooting

### Base de données verrouillée
```bash
# Arrêter MAGSAV si lancé, puis
./gradlew generateFreshData
```

### Tables manquantes
```bash
# Réinitialiser complètement
rm data/MAGSAV.db
./gradlew generateTestData
```

### Données incohérentes
```bash
# Régénérer proprement
./gradlew generateFreshData
```

---

*🎯 Données réalistes pour un développement efficace et des tests complets*