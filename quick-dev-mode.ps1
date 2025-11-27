# ============================================================
# MAGSAV 3.0 - Activation Mode Développement Rapide
# Active toutes les approbations automatiques
# ============================================================

Write-Host "\n[MAGSAV] Activation du Mode Developpement Rapide MAGSAV 3.0..." -ForegroundColor Cyan

# Désactiver TOUTES les confirmations PowerShell
$global:ConfirmPreference = "None"
$global:VerbosePreference = "SilentlyContinue"
$global:DebugPreference = "SilentlyContinue"
$global:WarningPreference = "SilentlyContinue"
$global:ErrorActionPreference = "Continue"
$global:ProgressPreference = "SilentlyContinue"

Write-Host "[OK] Confirmations PowerShell desactivees" -ForegroundColor Green

# Activer la variable d'environnement pour Java
$env:MAGSAV_DEV_MODE = "ENABLED"
[System.Environment]::SetEnvironmentVariable("MAGSAV_DEV_MODE", "ENABLED", "User")
Write-Host "[OK] Variable MAGSAV_DEV_MODE = ENABLED" -ForegroundColor Green

# Configuration Gradle optimisée
$env:GRADLE_OPTS = "-Xmx2048m -Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.caching=true"
Write-Host "[OK] Gradle optimise (daemon, parallel, cache)" -ForegroundColor Green

# Configuration Java
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8"
Write-Host "[OK] Java UTF-8 configure" -ForegroundColor Green

# Désactiver Git confirmations
git config --global push.autoSetupRemote true
git config --global pull.rebase false
git config --global advice.detachedHead false
Write-Host "[OK] Git auto-confirmations activees" -ForegroundColor Green

# Créer fonction de suppression forcée globale
function global:Remove-ItemForce {
    param([string]$Path)
    Remove-Item -Path $Path -Force -Recurse -Confirm:$false -ErrorAction SilentlyContinue
}
Write-Host "[OK] Fonction Remove-ItemForce disponible" -ForegroundColor Green

# Alias utiles
Set-Alias -Name rm -Value Remove-Item -Force -ErrorAction SilentlyContinue
Set-Alias -Name del -Value Remove-Item -Force -ErrorAction SilentlyContinue
Set-Alias -Name sleep -Value Start-Sleep -Force -ErrorAction SilentlyContinue
Write-Host "[OK] Alias crees (rm, del, sleep)" -ForegroundColor Green

Write-Host "\n[SUCCESS] Mode Developpement Rapide ACTIVE" -ForegroundColor Yellow -BackgroundColor DarkGreen
Write-Host "   - Aucune confirmation ne sera demandee" -ForegroundColor White
Write-Host "   - Gradle en mode parallele avec cache" -ForegroundColor White
Write-Host "   - Git avec auto-setup remote" -ForegroundColor White
Write-Host "   - JavaFX detectera automatiquement le mode dev`n" -ForegroundColor White

# Tester les approbations
Write-Host "[TEST] Test des approbations automatiques..." -ForegroundColor Cyan

# Test 1: Créer et supprimer un fichier temporaire sans confirmation
$testFile = "$PWD\.test-auto-approve"
"test" | Out-File -FilePath $testFile -Force
Remove-Item $testFile -Force -Confirm:$false -ErrorAction SilentlyContinue
if (-not (Test-Path $testFile)) {
    Write-Host "   [OK] Suppression sans confirmation: OK" -ForegroundColor Green
}
else {
    Write-Host "   [FAIL] Suppression sans confirmation: ECHEC" -ForegroundColor Red
}

# Test 2: Variable d'environnement
if ($env:MAGSAV_DEV_MODE -eq "ENABLED") {
    Write-Host "   [OK] Variable MAGSAV_DEV_MODE: OK" -ForegroundColor Green
}
else {
    Write-Host "   [FAIL] Variable MAGSAV_DEV_MODE: ECHEC" -ForegroundColor Red
}

# Test 3: Git configuration
$gitAutoSetup = git config --global push.autoSetupRemote
if ($gitAutoSetup -eq "true") {
    Write-Host "   [OK] Git auto-setup: OK" -ForegroundColor Green
}
else {
    Write-Host "   [WARN] Git auto-setup: Configuration manuelle requise" -ForegroundColor Yellow
}

Write-Host "`n[INFO] Pour utiliser ce mode dans tous les terminaux:" -ForegroundColor Cyan
Write-Host "   1. Redemarrez VS Code" -ForegroundColor White
Write-Host "   2. Le profil .magsav-profile.ps1 sera charge automatiquement" -ForegroundColor White
Write-Host "   3. Ou tapez: . .\.magsav-profile.ps1`n" -ForegroundColor White

Write-Host "[RUN] Pour lancer l'application:" -ForegroundColor Cyan
Write-Host "   .\start-dev.ps1           # Demarrage automatique backend + desktop" -ForegroundColor Yellow
Write-Host "   .\start-dev.ps1 -DesktopOnly  # Desktop seulement (backend deja actif)" -ForegroundColor Gray
Write-Host "   .\stop-dev.ps1            # Arret propre de tous les processus`n" -ForegroundColor Gray
