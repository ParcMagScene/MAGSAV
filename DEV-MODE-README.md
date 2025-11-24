# MAGSAV 3.0 - Mode Développement Rapide

## 🚀 Configuration Automatique des Autorisations

Ce document décrit la configuration du mode développement rapide pour MAGSAV 3.0, permettant un développement sans restrictions et avec toutes les autorisations activées.

## 📋 Fichiers de Configuration

### 1. `quick-dev-mode.ps1`
Script PowerShell minimal qui active rapidement le mode développement :
```powershell
. .\quick-dev-mode.ps1
```

**Ce qu'il fait :**
- ✅ Désactive toutes les confirmations (`ConfirmPreference = None`)
- ✅ Active la variable `MAGSAV_DEV_MODE = ENABLED`
- ✅ Configure `ExecutionPolicy = Bypass`

### 2. `.magsav-profile.ps1`
Profile PowerShell automatique chargé à chaque ouverture de terminal dans VS Code.

**Fonctionnalités :**
- Désactivation automatique de toutes les confirmations
- Configuration des variables d'environnement Gradle et Java
- Création d'alias utiles (rm, del, sleep, kill)
- Fonction `Remove-ItemForce` pour suppression sans confirmation

### 3. `.vscode/settings.json`
Configuration VS Code optimisée pour le développement rapide.

**Caractéristiques :**
- Terminal par défaut : "PowerShell Dev Mode" (charge automatiquement `.magsav-profile.ps1`)
- Politique d'exécution : Bypass
- Auto-save activé (délai : 1 seconde)
- Formatage automatique à la sauvegarde
- Désactivation des confirmations Git
- Java en mode automatique (updateBuildConfiguration: automatic)
- Exclusions optimisées pour la performance

## ⚙️ Variables d'Environnement Configurées

```powershell
$env:MAGSAV_DEV_MODE = "ENABLED"
$env:GRADLE_OPTS = "-Xmx2048m -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
```

## 🛠️ Commandes Disponibles

### Suppression Sans Confirmation
```powershell
# Méthode 1 : Avec -Force
Remove-Item -Path "fichier.txt" -Force

# Méthode 2 : Alias
rm fichier.txt        # Alias de Remove-Item -Force
del fichier.txt       # Alias de Remove-Item -Force

# Méthode 3 : Fonction personnalisée (si profile chargé)
Remove-ItemForce "dossier"  # Supprime récursivement sans confirmation
```

### Autres Alias Utiles
```powershell
sleep 5              # Pause de 5 secondes (alias de Start-Sleep)
kill 1234            # Arrêter un processus (alias de Stop-Process)
```

### Fonctions MAGSAV (si .magsav-profile chargé)
```powershell
Clean-MagsavBuilds   # Nettoie tous les dossiers build/
Rebuild-Magsav       # Clean + rebuild complet sans tests
Start-MagsavFast     # Démarre rapidement l'application desktop
```

## 🔧 Activation Manuelle

Si vous voulez activer le mode dev manuellement dans un nouveau terminal :

```powershell
# Option 1 : Script rapide
. .\quick-dev-mode.ps1

# Option 2 : Profile complet
. .\.magsav-profile.ps1

# Option 3 : Configuration minimale directe
$ConfirmPreference = "None"
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
$env:MAGSAV_DEV_MODE = "ENABLED"
```

## 📊 Vérification de la Configuration

Pour vérifier que tout est bien configuré :

```powershell
# Vérifier la politique d'exécution
Get-ExecutionPolicy -List

# Vérifier les variables d'environnement
$env:MAGSAV_DEV_MODE
$env:GRADLE_OPTS
$env:JAVA_TOOL_OPTIONS

# Vérifier ConfirmPreference
$ConfirmPreference

# Tester une suppression sans confirmation
New-Item -ItemType File -Path "test.txt" -Force
Remove-Item -Path "test.txt" -Force  # Devrait supprimer sans demander
```

## ⚠️ Notes de Sécurité

### Mode Développement Uniquement
Cette configuration est destinée **uniquement au développement local**. 

**Ne pas utiliser en production** car elle :
- Désactive les confirmations de sécurité
- Permet l'exécution de tous les scripts
- Autorise les opérations destructives sans validation

### Sécurité du Code
- Les fichiers `.ps1` sont gitignorés par défaut (sauf configuration)
- Toujours vérifier le code des scripts avant exécution
- Le mode dev est limité au scope Process (ne persiste pas)

## 🔄 Désactivation du Mode Dev

Pour revenir à une configuration normale :

```powershell
# Réinitialiser les préférences
$ConfirmPreference = "High"
$env:MAGSAV_DEV_MODE = $null

# Ou simplement fermer et rouvrir le terminal
```

## 📝 Personnalisation

Pour ajouter vos propres configurations au mode dev, éditez `.magsav-profile.ps1` :

```powershell
# Exemple : Ajouter un alias personnalisé
Set-Alias -Name build -Value ".\gradlew build" -Force

# Exemple : Ajouter une fonction
function MyCustomFunction {
    Write-Host "Ma fonction personnalisée"
}
```

## 🐛 Dépannage

### Le profile ne se charge pas automatiquement
1. Vérifier que VS Code utilise le bon terminal : `Ctrl+Shift+P` → "Terminal: Select Default Profile"
2. Choisir "PowerShell Dev Mode"
3. Redémarrer VS Code

### Les commandes ne fonctionnent pas
```powershell
# Recharger le profile manuellement
. .\.magsav-profile.ps1

# Ou réactiver le mode dev
. .\quick-dev-mode.ps1
```

### Erreur "ExecutionPolicy"
```powershell
# Forcer le bypass temporairement
powershell -ExecutionPolicy Bypass -File .\quick-dev-mode.ps1
```

## ✅ Résumé des Avantages

- ⚡ **Développement rapide** : Pas de confirmations qui ralentissent
- 🔓 **Toutes les autorisations** : Remove-Item, Start-Sleep, etc. sans restrictions
- 🤖 **Automatisation** : Configuration automatique au démarrage du terminal
- 🎯 **Optimisations** : Gradle et Java configurés pour performance maximale
- 🛠️ **Outils pratiques** : Fonctions et alias pour tâches courantes

## 📚 Références

- [PowerShell ExecutionPolicy](https://docs.microsoft.com/powershell/module/microsoft.powershell.core/about/about_execution_policies)
- [VS Code Terminal Configuration](https://code.visualstudio.com/docs/terminal/profiles)
- [Gradle Performance](https://docs.gradle.org/current/userguide/performance.html)

---

**MAGSAV 3.0** - Configuration développement rapide activée ✅
