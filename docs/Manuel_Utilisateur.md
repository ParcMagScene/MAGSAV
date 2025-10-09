# Documentation Utilisateur MAGSAV 1.2

## Table des matières

1. [Introduction](#introduction)
2. [Installation et configuration](#installation-et-configuration)
3. [Interface utilisateur](#interface-utilisateur)
4. [Gestion des produits](#gestion-des-produits)
5. [Gestion des sociétés](#gestion-des-sociétés)
6. [Gestion des interventions](#gestion-des-interventions)
7. [Fonctionnalités avancées](#fonctionnalités-avancées)
8. [Configuration du système](#configuration-du-système)
9. [Dépannage](#dépannage)
10. [FAQ](#faq)

---

## Introduction

MAGSAV 1.2 est une application de gestion de service après-vente développée en JavaFX. Cette version apporte des améliorations significatives en termes d'interface utilisateur, de performance et de fonctionnalités.

### Nouveautés de la version 1.2

- **Interface utilisateur modernisée** avec des composants réutilisables
- **Système de validation avancé** des formulaires
- **Gestion d'erreurs centralisée** avec notifications utilisateur
- **Cache intelligent** pour améliorer les performances
- **Interface de configuration** complète
- **Métriques de performance** en temps réel

---

## Installation et configuration

### Prérequis système

- **Java 21** ou version supérieure
- **JavaFX 21** (inclus avec l'application)
- **SQLite** (base de données intégrée)
- **Mémoire** : 512 MB RAM minimum, 1 GB recommandé
- **Espace disque** : 100 MB pour l'application + espace pour les données

### Installation

1. **Téléchargement**
   - Téléchargez le package MAGSAV-1.2.zip
   - Extrayez le contenu dans le répertoire de votre choix

2. **Configuration initiale**
   ```bash
   # Sur macOS/Linux
   ./gradlew run
   
   # Sur Windows
   gradlew.bat run
   ```

3. **Premier démarrage**
   - L'application créera automatiquement la base de données
   - Configurez les paramètres via le menu Configuration

### Configuration de la base de données

La base de données SQLite est créée automatiquement au premier démarrage. Le fichier se trouve dans :
- **macOS** : `~/Library/Application Support/MAGSAV/magsav.db`
- **Windows** : `%APPDATA%\MAGSAV\magsav.db`
- **Linux** : `~/.local/share/MAGSAV/magsav.db`

---

## Interface utilisateur

### Vue d'ensemble

L'interface MAGSAV 1.2 se compose de plusieurs éléments principaux :

1. **Barre de menu** : Accès aux fonctions principales
2. **Tableau de bord** : Aperçu des données importantes
3. **Panneaux de navigation** : Accès rapide aux différentes sections
4. **Zone de contenu** : Affichage des données et formulaires
5. **Barre de statut** : Informations système et notifications

### Éléments d'interface

#### Notifications
- **Notifications de succès** : Fond vert, confirment les actions réussies
- **Notifications d'erreur** : Fond rouge, signalent les problèmes
- **Notifications d'information** : Fond bleu, donnent des informations
- **Notifications d'avertissement** : Fond orange, alertent sur des situations

#### Validation des formulaires
- **Champs requis** : Bordure rouge si vide
- **Validation en temps réel** : Feedback immédiat
- **Messages d'aide** : Conseils contextuels
- **Boutons d'action** : Désactivés si le formulaire est invalide

---

## Gestion des produits

### Vue d'ensemble des produits

La section **Produits** permet de gérer l'inventaire complet :

#### Ajout d'un produit

1. Cliquez sur **"Nouveau produit"**
2. Remplissez les champs obligatoires :
   - **Code produit** : Identifiant unique (format : ABC123)
   - **Nom** : Désignation du produit
   - **Numéro de série** : Numéro série physique
   - **Fabricant** : Sélection ou création
   - **UID** : Identifiant système unique
   - **Situation** : État actuel du produit

3. Champs optionnels :
   - **Photo** : Image du produit
   - **Catégorie** : Classification
   - **Sous-catégorie** : Classification détaillée

#### Recherche et filtrage

- **Recherche globale** : Saisie libre dans tous les champs
- **Filtres par fabricant** : Menu déroulant des fabricants
- **Filtres par catégorie** : Classification hiérarchique
- **Filtres par situation** : État du produit

#### Modification d'un produit

1. Sélectionnez le produit dans la liste
2. Cliquez sur **"Modifier"** ou double-cliquez
3. Modifiez les champs nécessaires
4. Sauvegardez avec **Ctrl+S** ou le bouton **"Sauvegarder"**

### Gestion des fabricants

#### Création d'un fabricant

1. Dans la section **Sociétés**
2. Cliquez sur **"Nouveau"** > **"Fabricant"**
3. Remplissez :
   - **Nom** (obligatoire)
   - **Email** de contact
   - **Téléphone**
   - **Adresse** complète
   - **Notes** diverses

#### Association produit-fabricant

- Sélection automatique lors de la création
- Modification possible dans le formulaire produit
- Validation de cohérence automatique

---

## Gestion des sociétés

### Types de sociétés

MAGSAV gère plusieurs types de sociétés :

1. **Fabricants** : Producteurs des équipements
2. **Fournisseurs** : Vendeurs de pièces et services
3. **Clients** : Propriétaires des équipements
4. **Détecteurs** : Sociétés signalant les pannes

### Création d'une société

1. Menu **Sociétés** > **Nouvelle société**
2. Sélectionnez le **type**
3. Remplissez les informations :
   - **Nom** (obligatoire)
   - **Type** (sélection)
   - **Coordonnées** complètes
   - **Notes** particulières

### Gestion des contacts

- **Contact principal** : Défini automatiquement
- **Contacts multiples** : Gestion via les notes
- **Historique** : Suivi des modifications

---

## Gestion des interventions

### Cycle de vie d'une intervention

#### États possibles
- **En attente** : Intervention programmée
- **En cours** : Intervention active
- **En test** : Phase de validation
- **Terminé** : Intervention complétée
- **Fermé** : Intervention archivée
- **En attente pièce** : Blocage matériel
- **Retour client** : Attente feedback client
- **Irréparable** : Impossibilité de réparation

#### Création d'une intervention

1. Sélectionnez le **produit** concerné
2. Cliquez sur **"Nouvelle intervention"**
3. Remplissez :
   - **Description** de la panne
   - **Statut** initial (généralement "En attente")
   - **Date d'entrée** (auto-complétée)
   - **Notes** diagnostiques

#### Suivi d'intervention

1. **Mise à jour régulière** du statut
2. **Ajout de notes** techniques
3. **Gestion des pièces** nécessaires
4. **Estimation** temps et coût

#### Clôture d'intervention

1. Statut **"Terminé"** ou **"Fermé"**
2. **Date de sortie** automatique
3. **Notes finales** obligatoires
4. **Validation** client si nécessaire

### Rapports d'intervention

- **Historique complet** par produit
- **Statistiques** par période
- **Performance** par technicien
- **Coûts** et rentabilité

---

## Fonctionnalités avancées

### Système de cache

#### Activation/Désactivation
- Configuration via **Paramètres** > **Cache**
- Redémarrage nécessaire pour certains changements

#### Types de cache
- **Cache fabricants** : TTL 1 heure
- **Cache catégories** : TTL 30 minutes
- **Cache produits** : TTL 15 minutes
- **Cache détails** : TTL 5 minutes

#### Optimisation
- **Nettoyage automatique** à 80% de capacité
- **Statistiques** en temps réel
- **Invalidation manuelle** possible

### Métriques de performance

#### Consultation des métriques
1. Menu **Outils** > **Performance**
2. Visualisation en temps réel
3. Export possible en CSV

#### Indicateurs clés
- **Temps de réponse** moyen
- **Opérations lentes** (> 1s)
- **Hit rate du cache**
- **Utilisation mémoire**

#### Alertes automatiques
- **Opération très lente** : > 5s
- **Cache inefficace** : < 50% hit rate
- **Mémoire faible** : < 100MB disponible

### Import/Export

#### Formats supportés
- **CSV** : Import/export de données
- **Excel** : Via conversion CSV
- **JSON** : Backup configuration

#### Procédures d'import
1. Menu **Fichier** > **Importer**
2. Sélectionnez le **type** de données
3. Choisissez le **fichier** source
4. Mappez les **colonnes**
5. Validez et **importez**

#### Procédures d'export
1. Sélectionnez les **données** à exporter
2. Menu **Fichier** > **Exporter**
3. Choisissez le **format** de sortie
4. Configurez les **options**
5. Enregistrez le **fichier**

---

## Configuration du système

### Interface de configuration

Accès via **Menu** > **Configuration** ou **Ctrl+Shift+C**

#### Sections disponibles

##### Base de données
- **Chemin** de la base de données
- **Sauvegarde automatique** : Fréquence et destination
- **Compactage** : Optimisation périodique
- **Vérification intégrité** : Contrôles automatiques

##### Performance
- **Cache activé** : On/Off
- **Taille maximale** cache : en MB
- **TTL par défaut** : en minutes
- **Threads parallèles** : Nombre de workers

##### Interface utilisateur
- **Thème** : Clair/Sombre
- **Taille police** : Petite/Normale/Grande
- **Notifications** : Durée d'affichage
- **Confirmations** : Actions sensibles

##### Sécurité
- **Sauvegarde automatique** : Activation
- **Rétention** : Nombre de sauvegardes
- **Chiffrement** : Données sensibles
- **Audit** : Traçabilité des actions

### Paramètres avancés

#### Fichier de configuration
Emplacement : `config/application.properties`

#### Paramètres clés
```properties
# Base de données
db.path=/path/to/database.db
db.backup.enabled=true
db.backup.interval=24h

# Cache
cache.enabled=true
cache.max.size=100MB
cache.default.ttl=15m

# Performance
performance.monitoring=true
performance.slow.threshold=1000ms
performance.very.slow.threshold=5000ms

# Interface
ui.theme=light
ui.font.size=normal
ui.notifications.duration=5s
```

### Maintenance

#### Sauvegarde manuelle
1. Menu **Outils** > **Sauvegarde**
2. Sélectionnez la **destination**
3. Incluez/Excluez les **logs**
4. Lancez la **sauvegarde**

#### Restauration
1. Menu **Outils** > **Restaurer**
2. Sélectionnez le **fichier** de sauvegarde
3. Confirmez la **restauration**
4. **Redémarrez** l'application

#### Nettoyage
- **Logs** : Suppression automatique > 30 jours
- **Cache** : Vidage manuel possible
- **Temporaires** : Nettoyage au démarrage

---

## Dépannage

### Problèmes courants

#### L'application ne démarre pas

**Symptômes :**
- Erreur au lancement
- Fenêtre qui se ferme immédiatement
- Message d'erreur Java

**Solutions :**
1. Vérifiez la version Java (java -version)
2. Vérifiez les variables d'environnement
3. Consultez les logs : `logs/application.log`
4. Supprimez le cache : `cache/` directory

#### Performance dégradée

**Symptômes :**
- Interface lente à réagir
- Chargement long des données
- Utilisation mémoire élevée

**Solutions :**
1. Activez le cache si désactivé
2. Réduisez la taille du cache si mémoire limitée
3. Compactez la base de données
4. Redémarrez l'application

#### Erreurs de base de données

**Symptômes :**
- Messages "Database locked"
- Données non sauvegardées
- Corruption suspectée

**Solutions :**
1. Fermez toutes les instances de l'application
2. Vérifiez les droits d'accès au fichier DB
3. Restaurez depuis une sauvegarde
4. Contactez le support technique

#### Problèmes d'affichage

**Symptômes :**
- Interface déformée
- Caractères illisibles
- Fenêtres hors écran

**Solutions :**
1. Réinitialisez la configuration UI
2. Modifiez la taille de police
3. Changez la résolution d'écran
4. Supprimez `config/ui.properties`

### Logs et diagnostic

#### Emplacements des logs
- **Application** : `logs/application.log`
- **Erreurs** : `logs/error.log`
- **Performance** : `logs/performance.log`
- **SQL** : `logs/sql.log` (si debug activé)

#### Niveaux de log
- **ERROR** : Erreurs critiques
- **WARN** : Avertissements
- **INFO** : Informations générales
- **DEBUG** : Détails techniques

#### Activation du debug
```properties
# Dans config/application.properties
log.level=DEBUG
log.sql.enabled=true
```

### Support technique

#### Informations à fournir
1. **Version** de l'application
2. **Système** d'exploitation
3. **Version Java** utilisée
4. **Logs** d'erreur récents
5. **Étapes** pour reproduire le problème

#### Collecte d'informations
Menu **Aide** > **Informations système**
- Export automatique des informations pertinentes
- Fichier ZIP prêt à envoyer

---

## FAQ

### Questions générales

**Q : Puis-je utiliser MAGSAV sur plusieurs ordinateurs ?**
R : Oui, en partageant la base de données sur un serveur réseau ou en utilisant la synchronisation.

**Q : Combien de produits puis-je gérer ?**
R : Pas de limite théorique. Performances optimales jusqu'à 100 000 produits.

**Q : Les données sont-elles sauvegardées automatiquement ?**
R : Oui, la sauvegarde automatique est configurable et activée par défaut.

### Questions techniques

**Q : Puis-je importer mes données existantes ?**
R : Oui, via import CSV. Un assistant guide la procédure.

**Q : Comment sauvegarder ma base de données ?**
R : Menu Outils > Sauvegarde ou automatiquement selon configuration.

**Q : Puis-je personnaliser l'interface ?**
R : Oui, via Menu > Configuration > Interface utilisateur.

### Questions de performance

**Q : Pourquoi l'application est-elle lente ?**
R : Vérifiez que le cache est activé et que vous avez suffisamment de mémoire RAM.

**Q : Comment optimiser les performances ?**
R : Activez le cache, compactez régulièrement la DB, et consultez les métriques de performance.

**Q : Quelle configuration matérielle recommandez-vous ?**
R : Minimum 4 GB RAM, SSD recommandé, Java 21+.

### Questions de sécurité

**Q : Mes données sont-elles sécurisées ?**
R : Oui, base de données locale avec sauvegardes chiffrées optionnelles.

**Q : Puis-je contrôler l'accès aux données ?**
R : Version actuelle mono-utilisateur. Version multi-utilisateurs en développement.

**Q : Comment récupérer des données supprimées ?**
R : Via les sauvegardes automatiques ou en contactant le support.

---

## Annexes

### Raccourcis clavier

#### Généraux
- **Ctrl+S** : Sauvegarder
- **Ctrl+Z** : Annuler
- **Ctrl+F** : Rechercher
- **Ctrl+Q** : Quitter
- **F5** : Actualiser

#### Navigation
- **Ctrl+1** : Section Produits
- **Ctrl+2** : Section Sociétés
- **Ctrl+3** : Section Interventions
- **Ctrl+4** : Tableau de bord

#### Édition
- **Ctrl+N** : Nouveau
- **Ctrl+E** : Éditer
- **Suppr** : Supprimer
- **Échap** : Annuler édition

### Formats de données

#### Codes produits
- Format recommandé : `ABC123-456`
- Longueur : 3-20 caractères
- Caractères autorisés : A-Z, 0-9, tirets

#### UIDs système
- Format automatique : `MAGSAV-YYYYMMDD-NNNN`
- Unique garanti par le système
- Non modifiable après création

### Limites système

#### Base de données
- **Taille maximale** : 1 TB (SQLite)
- **Nombre d'enregistrements** : Illimité pratiquement
- **Nombre de connexions** : 1 (mono-utilisateur)

#### Mémoire
- **Minimum requis** : 512 MB
- **Recommandé** : 1-2 GB
- **Cache maximum** : 500 MB

---

*Documentation MAGSAV 1.2 - Version 1.0*
*Dernière mise à jour : Janvier 2024*

Pour toute question ou suggestion, contactez l'équipe de développement.