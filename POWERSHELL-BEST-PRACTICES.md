# PowerShell - Bonnes Pratiques MAGSAV 3.0

## 🔧 Configuration Encodage

### Problème
Les scripts PowerShell créés sans BOM (Byte Order Mark) peuvent causer des erreurs de syntaxe et d'encodage, notamment avec les caractères accentués français.

### Solution Automatique
VS Code est configuré pour **automatiquement sauvegarder les fichiers .ps1 en UTF-8 avec BOM**.

Configuration dans `.vscode/settings.json` :
```json
"files.encoding": "utf8bom",
"[powershell]": {
    "files.encoding": "utf8bom"
}
```

## 📝 Création de Scripts

### Méthode 1 : Via VS Code (Recommandé)
1. Créer un nouveau fichier `.ps1`
2. Écrire le code
3. Sauvegarder (automatiquement en UTF-8 BOM)

### Méthode 2 : Via Module Helper
```powershell
# Importer le module
Import-Module .\ScriptHelper.psm1

# Créer un script
$content = @'
# Mon script PowerShell
Write-Host "Hello MAGSAV" -ForegroundColor Green
'@

New-MagsavScript -Path "mon-script.ps1" -Content $content
```

### Méthode 3 : Via PowerShell Direct
```powershell
# Utiliser UTF-8 BOM explicitement
$utf8Bom = New-Object System.Text.UTF8Encoding $true
$content = "# Mon script`nWrite-Host 'Test'"
[System.IO.File]::WriteAllText("mon-script.ps1", $content, $utf8Bom)
```

## ⚠️ À Éviter

### ❌ NE PAS utiliser
```powershell
# MAUVAIS - Pas de contrôle encodage
"content" | Out-File "script.ps1"

# MAUVAIS - ASCII par défaut
Set-Content -Path "script.ps1" -Value "content"
```

### ✅ À UTILISER
```powershell
# BON - UTF-8 BOM explicite
"content" | Out-File "script.ps1" -Encoding UTF8

# BON - Via .NET avec BOM
$utf8Bom = New-Object System.Text.UTF8Encoding $true
[System.IO.File]::WriteAllText("script.ps1", $content, $utf8Bom)
```

## 🔍 Vérifier l'Encodage

```powershell
# Vérifier si un fichier a le BOM UTF-8
$bytes = [System.IO.File]::ReadAllBytes("script.ps1")
if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    Write-Host "✅ UTF-8 avec BOM" -ForegroundColor Green
} else {
    Write-Host "❌ Pas de BOM UTF-8" -ForegroundColor Red
}
```

## 🛠️ Corriger un Fichier Existant

```powershell
# Lire et ré-écrire avec BOM
$content = Get-Content "script.ps1" -Raw
$utf8Bom = New-Object System.Text.UTF8Encoding $true
[System.IO.File]::WriteAllText("script.ps1", $content, $utf8Bom)
```

## 📋 Caractères Spéciaux

### ✅ Caractères Sûrs
- Lettres accentuées : é, è, à, ç, ê, etc.
- Emojis : ✅ ❌ 🚀 📦 🔧
- Symboles : → ← ↑ ↓ • ○ ●

### ⚠️ Caractères à Échapper dans Strings
```powershell
# Guillemets doubles
"Message avec ""guillemets"""

# Dollar dans string
"Prix: `$100"

# Backslash
"Chemin: C:\Users"  # OK dans double quotes
'Chemin: C:\Users'  # OK dans single quotes
```

## 🎯 Template Script Standard

```powershell
# ==============================================================================
# MAGSAV 3.0 - [Nom du Script]
# Description : [Description]
# Auteur : MAGSAV Team
# Date : [Date]
# ==============================================================================

#Requires -Version 5.1

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$Parameter1,
    
    [Parameter(Mandatory=$false)]
    [switch]$Force
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ==============================================================================
# FONCTIONS
# ==============================================================================

function Write-MagsavMessage {
    param(
        [string]$Message,
        [ValidateSet('Info', 'Success', 'Warning', 'Error')]
        [string]$Type = 'Info'
    )
    
    $colors = @{
        'Info' = 'Cyan'
        'Success' = 'Green'
        'Warning' = 'Yellow'
        'Error' = 'Red'
    }
    
    $icons = @{
        'Info' = 'ℹ️'
        'Success' = '✅'
        'Warning' = '⚠️'
        'Error' = '❌'
    }
    
    Write-Host "$($icons[$Type]) $Message" -ForegroundColor $colors[$Type]
}

# ==============================================================================
# SCRIPT PRINCIPAL
# ==============================================================================

try {
    Write-MagsavMessage "Démarrage du script..." -Type Info
    
    # Code ici
    
    Write-MagsavMessage "Script terminé avec succès" -Type Success
    exit 0
    
} catch {
    Write-MagsavMessage "Erreur: $_" -Type Error
    Write-MagsavMessage $_.ScriptStackTrace -Type Error
    exit 1
}
```

## 🔒 Politique d'Exécution

Le projet est configuré avec `ExecutionPolicy Bypass` dans tous les terminaux.

Vérifier :
```powershell
Get-ExecutionPolicy -List
```

Activer temporairement si nécessaire :
```powershell
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass -Force
```

## 📚 Ressources

- [PowerShell Encoding](https://docs.microsoft.com/powershell/scripting/components/vscode/understanding-file-encoding)
- [UTF-8 BOM Guide](https://docs.microsoft.com/powershell/module/microsoft.powershell.management/set-content)
- [VS Code PowerShell](https://code.visualstudio.com/docs/languages/powershell)

## ✅ Checklist Avant Commit

- [ ] Fichier sauvegardé dans VS Code (encodage auto)
- [ ] Aucune erreur de syntaxe PowerShell
- [ ] Caractères accentués affichés correctement
- [ ] Script testé dans terminal PowerShell
- [ ] Commentaires en français corrects
- [ ] Template standard respecté (si applicable)

## 🚀 Mode Développement

Le script `enable-dev-mode.ps1` active automatiquement :
- ExecutionPolicy Bypass
- Pas de confirmations
- Variables d'environnement optimisées
- Alias utilitaires (rm, del, sleep, kill)
- Fonctions de nettoyage

Lancer :
```powershell
.\enable-dev-mode.ps1
```

Ou automatiquement via le profil PowerShell Dev Mode dans VS Code.
