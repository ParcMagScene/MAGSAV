# ============================================================
# MAGSAV 3.0 - Configuration Automatique PowerShell
# Active automatiquement le mode dÃ©veloppement rapide
# ============================================================

# DÃ©sactiver toutes les confirmations
$global:ConfirmPreference = "None"
$global:VerbosePreference = "SilentlyContinue"
$global:DebugPreference = "SilentlyContinue"
$global:WarningPreference = "Continue"
$global:ErrorActionPreference = "Continue"
$global:ProgressPreference = "SilentlyContinue"

# Variables d'environnement
$env:MAGSAV_DEV_MODE = "ENABLED"
$env:GRADLE_OPTS = "-Xmx2048m -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true"
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"

# Alias utiles
Set-Alias -Name rm -Value Remove-Item -Force -ErrorAction SilentlyContinue
Set-Alias -Name del -Value Remove-Item -Force -ErrorAction SilentlyContinue
Set-Alias -Name sleep -Value Start-Sleep -Force -ErrorAction SilentlyContinue

# Fonction globale pour suppression forcÃ©e
function global:Remove-ItemForce {
    param([string]$Path)
    Remove-Item -Path $Path -Force -Recurse -ErrorAction SilentlyContinue
}

# Message de confirmation (optionnel, commentez si vous ne voulez pas)
Write-Host "âœ… MAGSAV Dev Mode activÃ© automatiquement" -ForegroundColor Green
