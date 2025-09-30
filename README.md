# MAGSAV 1.1 - Système de Gestion SAV

<!-- Remplacez OWNER/REPO par votre organisation et dépôt GitHub -->
[![CI](https://github.com/OWNER/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/ci.yml)

Système complet de gestion de Service Après-Vente avec import CSV, génération d'étiquettes QR et suivi des dossiers.

> Documentation unifiée: l’ensemble des guides et rapports a été consolidé dans `DOCUMENTATION_UNIFIEE.md` à la racine du projet.
>
> Accès direct à l’historique: voir la section "Historique" dans [`DOCUMENTATION_UNIFIEE.md`](./DOCUMENTATION_UNIFIEE.md#historique).
>
> Accès direct au changelog: voir la section "Changelog" dans [`DOCUMENTATION_UNIFIEE.md`](./DOCUMENTATION_UNIFIEE.md#changelog).

## 📝 Aperçu du Changelog

Dernière version: 1.1.0 (2025‑09‑29)
- Desktop: le double‑clic sur un produit ouvre toujours sa fiche (préférence retirée côté produits).
- README: mis à jour pour refléter le comportement et la préférence restante côté interventions.

Voir le détail complet dans [`DOCUMENTATION_UNIFIEE.md` → Changelog](./DOCUMENTATION_UNIFIEE.md#changelog).

## ✨ Fonctionnalités

- 📋 **Import CSV** : Import direct de vos données SAV avec support des formats de dates français
- 🔍 **Recherche avancée** : Recherche par numéro de série, propriétaire, statut
- 📊 **Suivi des statuts** : Gestion du cycle de vie complet (réception → diagnostic → réparation → livraison)
- 🏷️ **Étiquettes QR** : Génération d'étiquettes PDF avec QR codes pour traçabilité
- 💾 **Base SQLite** : Stockage local sécurisé avec pool de connexions
 - 🔐 **Code produit unique (AA1234)** : Identifiant lisible et court, unique par produit (numéro de série)
 - 🖼️ **Photo produit (Web + Desktop)** : Affichage et upload côté Web, import via l’app JavaFX, placeholder automatique si absent
 - 🖱️ **Double‑clic rapide** :
	 - Sur la liste des produits, le double‑clic ouvre toujours la fiche produit (comportement permanent)
	 - Sur la liste des interventions, le double‑clic est configurable dans Préférences (activé par défaut)

## � Photos produit (Web & JavaFX)

- Emplacement des fichiers: `./photos/prod-<numeroSerie>.<ext>` (créé automatiquement)
- Endpoints Web:
	- `GET /product/{sn}/photo` → image (photo réelle si présente, sinon PNG 1x1 transparent)
	- `POST /product/{sn}/photo` (multipart `file`) → réservé aux ADMIN
- Droits d’accès:
	- GET accessible à tous (public)
	- POST réservé aux `ROLE_ADMIN`
- Interface Web:
	- Dans la liste des produits (Accueil), miniature + formulaire d’upload (visible si connecté en ADMIN)
- Interface JavaFX:
	- Bouton “Importer photo” dans le panneau Produits; aperçu dans le volet de droite

Vérification automatique:
- Le script `scripts/smoke-test.sh` vérifie l’endpoint photo (GET) et un cycle d’upload ADMIN (POST) bout‑en‑bout.

## �🚀 Installation et Lancement

```bash
# Compilation et construction
./gradlew build

# Voir les commandes disponibles
./gradlew run --args='help'
```

## 📄 Format CSV SAV

Le système prend en charge le format CSV suivant pour l'import principal :
```
```csv
PRODUIT,N° DE SERIE,PROPRIETAIRE,PANNE,STATUT,DETECTEUR,DATE ENTREE,DATE SORTIE
iPhone 12,ABC123456789,Martin Dupont,Écran cassé,recu,Technicien 1,15/01/2024,
Samsung Galaxy S21,XYZ987654321,Sophie Martin,Batterie défaillante,diagnostique,Technicien 2,16/01/2024,
Dell Laptop,DELL2020003,Jean Moreau,Clavier défectueux,pret,Technicien 2,19/01/2024,22/01/2024
```

### Champs obligatoires
- **PRODUIT** : Nom/modèle du produit
- **N° DE SERIE** : Numéro de série unique (clé primaire avec propriétaire)
- **PROPRIETAIRE** : Nom du client propriétaire

### Champs optionnels
- **PANNE** : Description du problème
- **STATUT** : Statut actuel (défaut: `recu`)
- **DETECTEUR** : Technicien ayant diagnostiqué
- **DATE ENTREE** : Date de réception (formats: dd/MM/yyyy, yyyy-MM-dd, dd-MM-yyyy)
- **DATE SORTIE** : Date de livraison

### Statuts supportés
- `recu` : Produit reçu en SAV
- `diagnostique` : Diagnostic en cours
- `attente_pieces` : En attente de pièces
- `repare` : Réparation terminée  
- `pret` : Prêt pour récupération
- `livre` : Livré au client

## 💻 Commandes principales

### Import de données
```bash
# Import du fichier SAV principal
./gradlew run --args='import --dossiers-sav samples/dossiers_sav.csv'

# Import de clients et fournisseurs (optionnel)
./gradlew run --args='import --clients samples/clients.csv'
./gradlew run --args='import --fournisseurs samples/fournisseurs.csv'
```

### Consultation et recherche
```bash
# Lister tous les dossiers
./gradlew run --args='lister'

# Filtrer par statut
./gradlew run --args='lister --statut recu'

# Filtrer par propriétaire
./gradlew run --args='lister --proprietaire Martin'

# Recherche par numéro de série
./gradlew run --args='recherche --serie ABC123456789'

# Recherche par propriétaire
./gradlew run --args='recherche --proprietaire Martin'
```

### Gestion des statuts
```bash
# Changer le statut d'un dossier
./gradlew run --args='statut --id 1 --nouveau-statut diagnostique'

# Marquer comme livré (met automatiquement la date de sortie)
./gradlew run --args='statut --id 1 --nouveau-statut livre'
```

### Génération d'étiquettes
```bash
# Générer une étiquette PDF avec QR code
./gradlew run --args='etiquette --id 1'
```

## ⚙️ Configuration externe (application.yml)

Vous pouvez externaliser la configuration dans un fichier `application.yml` (ou un autre chemin via `--config`).

Ordre de priorité (plus fort en premier) :
1. Options CLI (`--database`, `--output`, etc.)
2. Fichier de configuration
3. Valeurs par défaut internes

### Exemple de `application.yml`
```yaml
app:
	database:
		url: magsav.db            # ou jdbc:sqlite:/chemin/vers/base.db
	output:
		directory: out            # dossier de génération étiquettes / qr
	import:
		dateFormats: "dd/MM/yyyy,yyyy-MM-dd,dd-MM-yyyy,d/M/yyyy"
	etiquette:
		pdf:
			includeQr: true
			fontSize: 12
```

### Utilisation
```bash
# Utiliser le fichier application.yml par défaut à la racine
./gradlew run --args='lister'

# Spécifier un fichier différent
./gradlew run --args='--config config/prod.yml lister'

# Override de la base via CLI (ignore celle du fichier)
./gradlew run --args='--database data/prod.db lister'

# Override uniquement du dossier output
./gradlew run --args='--output build/out etiquette --id 1'
```

Les formats de date configurés sont appliqués pour l'import CSV dans l'ordre donné (le premier qui matche est utilisé). Tout format invalide est ignoré sans bloquer l'import.

Si `app.database.url` ne commence pas par `jdbc:` il sera préfixé automatiquement par `jdbc:sqlite:`.

Pour vérifier la configuration effective, vous pouvez lancer une simple commande (`help`, `lister`, etc.) puis observer la base utilisée (taille du fichier) et le dossier de sortie.

## 🔧 Structure technique

- **Java 21 (LTS)** avec Gradle 8.14.3
- **Base de données** : SQLite avec HikariCP
- **CLI** : Picocli pour interface en ligne de commande
- **PDF/QR** : PDFBox + ZXing pour génération d'étiquettes
- **CSV** : OpenCSV pour import de données

### 📌 Modèle de données: Produit vs Intervention
- L'application distingue désormais le **Produit** (identifié par son `numero_serie`) de l'**Intervention** (anciennement « dossier »).
- Un nouveau stockage `produits` maintient un **code court unique** au format `AA1234` par `numero_serie`.
- Les listes et filtres "Code" affichent et utilisent le **code produit**. Lorsque des interventions existent sans produit associé, le code d'intervention historique est utilisé en secours (COALESCE).
- Les **QR codes** encodent le `numero_serie` (propriété du produit) afin de garantir l'unicité et la traçabilité indépendamment des interventions.
- Les routes web conservent les alias pour compatibilité: `/dossier/...` et `/intervention/...` pointent vers les mêmes pages (détail, QR, étiquette).

### ⚙️ Préférences UI
- `ui.openInterventionOnDoubleClick: true|false` — active/désactive l’ouverture au double‑clic des interventions (par défaut: `true`).
- Le double‑clic sur les produits est toujours actif (l’option n’apparaît plus dans le dialogue Préférences).

### 🧭 Note de migration de données
- La migration crée la table `produits` si nécessaire et **rétro-remplit** les produits à partir des interventions existantes (groupées par `numero_serie`).
- Un **code AA1234 unique** est généré pour chaque produit absent et, si besoin, synchronisé avec les interventions historiques.
- La migration est **idempotente** et peut être relancée sans effets secondaires.

### ℹ️ Note de routage web
- L'accueil est servi par `DossierSAVController` sur `/` (modèle Thymeleaf peuplé).
- `RootRedirectController` est conservé à titre de référence mais désactivé (sans `@Controller`) pour éviter un double mapping sur `/`.
- Pour une réactivation propre, suivez le TODO dans `src/main/java/com/magsav/web/controller/RootRedirectController.java` (basculer l'accueil sur `/index`, remettre `@Controller`, ajuster sécurité/tests si besoin).

## 📁 Données d'exemple

Le répertoire `samples/` contient des fichiers CSV d'exemple :
- `dossiers_sav.csv` : Format principal avec 5 dossiers de test
- `clients.csv` : Clients pour compatibilité
- `fournisseurs.csv` : Fournisseurs pour compatibilité

## 📦 Déploiement

```bash
# Génération de l'archive complète
./gradlew distTar

# Le fichier généré se trouve dans build/distributions/
ls build/distributions/MAGSAV-1.1-1.1.0.tar
```

## 🤖 Intégration continue (CI)

La pipeline GitHub Actions (fichier `.github/workflows/ci.yml`) vérifie automatiquement le projet sur chaque push/PR vers `main`/`master` :

- JDK 21 (Temurin) via `actions/setup-java`
- Cache Gradle activé pour accélérer les builds
- Exécution des tests avec `--no-daemon` pour une meilleure stabilité en CI
- Publication des rapports de tests en artefact (`build/reports/tests/test`)

Badge de statut (à adapter) :

```md
[![CI](https://github.com/OWNER/REPO/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/REPO/actions/workflows/ci.yml)
```

Remplacez `OWNER/REPO` par votre namespace GitHub. Vous pouvez aussi forcer une branche spécifique via `?branch=main`.

## 🧭 Démarrer et arrêter rapidement

Pour lancer le serveur web (en arrière-plan) puis l’UI JavaFX au premier plan :

```bash
scripts/start-all.sh
```

Alternative via Gradle:

```bash
./gradlew startAll
```

Notes:
- Les logs web sont écrits dans `build/logs/web.log`.
- Le PID du serveur web est enregistré dans `.web.pid` à la racine du projet.

Pour arrêter le serveur web démarré par le script ci‑dessus :

```bash
scripts/stop-web.sh
```

Ce script envoie d’abord un SIGTERM, attend l’arrêt, puis force l’arrêt (SIGKILL) si nécessaire et supprime `.web.pid`.

Alternative via Gradle:

```bash
./gradlew stopWeb
```

## 🛠 Dépannage (fichiers Java en rouge dans VS Code)

Si VS Code affiche « non-project file » ou les packages attendus comme `main.java.com...` :

1. Assurez-vous d'avoir ouvert le dossier racine `MAGSAV-1.1` (pas `src/`).
2. Lancer : Command Palette → `Java: Clean Java Language Server Workspace`.
3. Vérifier `build.gradle` contient la section `sourceSets { ... }` (déjà incluse).
4. Supprimer éventuellement le dossier `.classpath`, `.project`, `.settings` si générés (Eclipse héritage).
5. Reload window : Command Palette → `Developer: Reload Window`.
6. Re-importer le projet : accepter la notification « Import Gradle Project ».

Le fichier `.vscode/settings.json` force déjà `java.project.sourcePaths` sur `src/main/java`.

Commande de reconstruction :
```bash
./gradlew clean build
```

```

## Installation
1. Prérequis: Java 21+, Gradle (ou wrapper inclus `./gradlew`)
2. Depuis la racine du projet, construire:

```bash
./gradlew build
```

## Exécution
- Lancer l’application (génère un QR et une étiquette PDF de démonstration):

```bash
./gradlew run
```

### Interface graphique JavaFX (macOS/Linux/Windows)
Pour lancer l’UI JavaFX (desktop):

```bash
./gradlew runGui
```

Astuce: `runGui` est un alias de `run` (il délègue à la tâche `:run` du plugin Application). Vous pouvez aussi lancer:

```bash
./gradlew run
```

Notes:
- Aucune configuration de chemin JavaFX n’est requise (le plugin OpenJFX gère les modules).
- Si vous rencontrez une erreur liée aux modules JavaFX, vérifiez que votre JDK est bien Java 21 et relancez `./gradlew clean runGui`.


- Importer des CSV d’exemple (clients/fournisseurs) puis générer les fichiers de démo:

```bash
./gradlew run --args="import"
```

- macOS (Apple Silicon): assurez-vous que `JAVA_HOME` pointe vers une JDK aarch64:

```bash
/usr/libexec/java_home -V
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
```

## Import CSV
- Format minimal clients: `nom,prenom,email,tel,adresse`
- Format minimal fournisseurs: `nom,email,tel,siret`
- Déduplication: par `email`; mise à jour si déjà existant
- Fichiers d’exemple: `samples/clients.csv`, `samples/fournisseurs.csv`

## QR Codes
- Bibliothèque: ZXing
- Exemple de génération: `out/qr-demo.png`
- Le QR encode désormais le `numero_serie` (niveau produit).

## Étiquettes PDF
- Bibliothèque: Apache PDFBox
- Exemple de sortie: `out/etiquette-demo.pdf`
- L'étiquette utilise le **code produit** (AA1234) et le QR basé sur le `numero_serie`.

## Flux Entrée SAV (proposé)
1. Rechercher/créer client
2. Décrire appareil + SN + accessoires
3. Saisir symptôme + photos optionnelles
4. Générer étiquette QR et coller sur l’appareil

## Dépannage
- Problème de JDK: vérifier `java -version` et `echo $JAVA_HOME`
- Fonts PDF: intégrer Noto/DejaVu si besoin d’UTF‑8 étendu

## Licence
À définir.
