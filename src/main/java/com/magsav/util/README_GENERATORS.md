# 🎯 Générateurs de Données de Test MAGSAV

Ce dossier contient plusieurs utilitaires pour générer des données de test réalistes pour l'application MAGSAV.

## 🚀 Générateurs Disponibles

### 1. TestDataGenerator
**Classe principale** qui génère des données complètes pour toutes les tables.

```bash
# Utilisation directe (nécessite classpath)
java com.magsav.util.TestDataGenerator
```

### 2. ComprehensiveTestDataGenerator
**Générateur complet** avec initialisation automatique de la base de données.

```bash
# Via Gradle (recommandé)
./gradlew generateTestData
```

### 3. FreshDataGenerator  
**Générateur "fraîches"** qui vide d'abord toutes les tables avant de générer.

```bash
# Via Gradle
./gradlew generateFreshData
```

### 4. DataGeneratorRunner
**Interface simple** pour lancer TestDataGenerator avec un affichage amélioré.

## 📊 Données Générées

| Table | Nombre | Description |
|-------|--------|-------------|
| `categories` | 20 | Catégories hiérarchiques avec emojis |
| `societes` | 50 | Sociétés françaises (clients, fournisseurs, etc.) |
| `techniciens` | 10 | Techniciens avec spécialités variées |
| `vehicules` | 8 | Véhicules de la flotte MAGSAV |
| `produits` | 100 | Produits audiovisuels professionnels |
| `interventions` | 30 | Interventions avec différents statuts |
| `planifications` | 25 | Planifications d'interventions |
| `commandes` | 15 | Commandes fournisseurs |
| `lignes_commandes` | 45 | Lignes détaillées des commandes |
| `mouvements_stock` | 80 | Mouvements de stock réalistes |
| `alertes_stock` | 12 | Alertes de stock configurées |
| `disponibilites_techniciens` | 20 | Disponibilités des techniciens |
| `communications` | 35 | Communications (email, SMS, etc.) |
| `sav_history` | 40 | Historique SAV |
| `email_templates` | - | Templates d'emails préconfiguré |

## 🔧 Utilisation Recommandée

### Première génération de données
```bash
./gradlew generateTestData
```

### Régénération complète (vide d'abord)
```bash
./gradlew generateFreshData
```

### Ajout de données supplémentaires
```bash
./gradlew generateTestData
```
*(ajoute aux données existantes)*

## 🎨 Caractéristiques des Données

### ✨ Réalisme
- **Noms français** pour personnes et sociétés
- **Adresses françaises** réalistes  
- **Dates cohérentes** (2020-2024)
- **Relations logiques** entre les tables
- **Statuts variés** selon les contextes métier

### 🌍 Contexte Métier  
- **Audiovisuel professionnel** (marques connues)
- **Interventions SAV** réalistes
- **Gestion de flotte** de véhicules  
- **Planification** d'interventions
- **Stock et commandes** avec prix français

### 🔗 Cohérence Relationnelle
- **Clés étrangères** valides
- **Hiérarchie** des catégories
- **Relations** produits ↔ interventions  
- **Planifications** liées aux techniciens et véhicules
- **Commandes** avec lignes détaillées

## 🛠️ Développement

### Ajouter de Nouvelles Données
1. Modifier `TestDataGenerator.java`
2. Ajouter une nouvelle méthode `generateXXX()`
3. L'appeler dans `generateCompleteTestData()`

### Modifier les Quantités
Modifier les paramètres dans `generateCompleteTestData()`:
```java
generateCategories(20);     // ← Changer ici
generateSocietes(50);       // ← Changer ici  
generateProduits(100);      // ← Changer ici
```

### Personnaliser les Données
Modifier les listes statiques en début de classe:
```java
private static final List<String> FABRICANTS = Arrays.asList(
    "Yamaha", "Pioneer", "JBL" // ← Ajouter/modifier ici
);
```

## ⚠️ Notes Importantes

- Les générateurs **créent** des données, ils ne les modifient pas
- `FreshDataGenerator` **supprime** tout avant de générer
- Les **IDs auto-incrémentés** continuent la séquence existante  
- Certains tests peuvent échouer si les données changent

## 🚀 Intégration Continue

Ces générateurs peuvent être intégrés dans:
- **Tests d'intégration** (données fraîches à chaque test)
- **Démos** (données réalistes pour présentation)  
- **Développement** (base de données peuplée rapidement)
- **Formation** (données d'exemple pour apprendre l'application)

---
*Générateurs créés pour MAGSAV 1.2 - Données réalistes pour le développement et les tests*