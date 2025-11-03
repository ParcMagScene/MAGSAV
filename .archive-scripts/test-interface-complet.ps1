# MAGSAV-3.0 - Test Complet Interface Utilisateur
# Évaluation systematique de tous les modules JavaFX

Write-Host "=== TEST COMPLET INTERFACE MAGSAV-3.0 ===" -ForegroundColor Yellow
Write-Host ""

# Vérification processus JavaFX existant
$javaProcesses = Get-Process | Where-Object { $_.ProcessName -like "*java*" -and $_.MainWindowTitle -like "*MAGSAV*" }

if ($javaProcesses) {
    Write-Host "✅ Application JavaFX detectée (PID: $($javaProcesses[0].Id))" -ForegroundColor Green
} else {
    Write-Host "❌ Aucune application JavaFX detectée" -ForegroundColor Red
    Write-Host "🚀 Démarrage de l'application..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-Command", "cd c:\Users\aalou\MAGSAV-3.0; .\gradlew.bat :desktop-javafx:run" -WindowStyle Normal
    Start-Sleep -Seconds 20
}

Write-Host ""

# ================================
# ANALYSE DES MODULES INDIVIDUELS  
# ================================

Write-Host "=== ANALYSE MODULES JAVAFX ===" -ForegroundColor Cyan

# 1. CLIENT MANAGER
Write-Host "`n1. CLIENT MANAGER VIEW" -ForegroundColor White
$clientFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\ClientManagerView.java"
if (Test-Path $clientFile) {
    $clientContent = Get-Content $clientFile -Raw
    $clientLines = (Get-Content $clientFile).Count
    Write-Host "   ✅ Fichier présent: $clientLines lignes" -ForegroundColor Green
    
    # Analyse structure
    if ($clientContent -match "TableView") {
        Write-Host "   ✅ TableView présent" -ForegroundColor Green
    }
    if ($clientContent -match "TextField.*search") {
        Write-Host "   ✅ Fonction recherche détectée" -ForegroundColor Green
    }
    if ($clientContent -match "Button.*add|Button.*create") {
        Write-Host "   ✅ Bouton ajout détecté" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# 2. EQUIPMENT MANAGER
Write-Host "`n2. EQUIPMENT MANAGER VIEW" -ForegroundColor White
$equipFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\EquipmentManagerView.java"
if (Test-Path $equipFile) {
    $equipContent = Get-Content $equipFile -Raw
    $equipLines = (Get-Content $equipFile).Count
    Write-Host "   ✅ Fichier présent: $equipLines lignes" -ForegroundColor Green
    
    # Analyse structure complète
    if ($equipContent -match "TableView") {
        Write-Host "   ✅ TableView présent" -ForegroundColor Green
    }
    if ($equipContent -match "ToolBar") {
        Write-Host "   ✅ Barre d'outils présente" -ForegroundColor Green
    }
    if ($equipContent -match "ComboBox") {
        Write-Host "   ✅ Filtres ComboBox présents" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# 3. SAV MANAGER  
Write-Host "`n3. SAV MANAGER VIEW" -ForegroundColor White
$savFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\sav\SAVManagerView.java"
if (Test-Path $savFile) {
    $savContent = Get-Content $savFile -Raw
    $savLines = (Get-Content $savFile).Count
    Write-Host "   ✅ Fichier présent: $savLines lignes" -ForegroundColor Green
    
    if ($savContent -match "TableView") {
        Write-Host "   ✅ TableView présent" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# 4. VEHICLE MANAGER
Write-Host "`n4. VEHICLE MANAGER VIEW" -ForegroundColor White  
$vehicleFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\VehicleManagerView.java"
if (Test-Path $vehicleFile) {
    $vehicleContent = Get-Content $vehicleFile -Raw
    $vehicleLines = (Get-Content $vehicleFile).Count
    Write-Host "   ✅ Fichier présent: $vehicleLines lignes" -ForegroundColor Green
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# 5. PERSONNEL MANAGER
Write-Host "`n5. PERSONNEL MANAGER VIEW" -ForegroundColor White
$personnelFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\PersonnelManagerView.java"
if (Test-Path $personnelFile) {
    $personnelContent = Get-Content $personnelFile -Raw
    $personnelLines = (Get-Content $personnelFile).Count
    Write-Host "   ✅ Fichier présent: $personnelLines lignes" -ForegroundColor Green
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# 6. CONTRACT MANAGER
Write-Host "`n6. CONTRACT MANAGER VIEW" -ForegroundColor White
$contractFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\ContractManagerView.java"
if (Test-Path $contractFile) {
    $contractContent = Get-Content $contractFile -Raw
    $contractLines = (Get-Content $contractFile).Count
    Write-Host "   ✅ Fichier présent: $contractLines lignes" -ForegroundColor Green
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# 7. PROJECT MANAGER
Write-Host "`n7. PROJECT MANAGER VIEW" -ForegroundColor White
$projectFile = "desktop-javafx\src\main\java\com\magsav\desktop\views\salesinstallation\ProjectManagerView.java"
if (Test-Path $projectFile) {
    $projectContent = Get-Content $projectFile -Raw
    $projectLines = (Get-Content $projectFile).Count
    Write-Host "   ✅ Fichier présent: $projectLines lignes" -ForegroundColor Green
} else {
    Write-Host "   ❌ Fichier manquant" -ForegroundColor Red
}

# ================================
# ANALYSE DES DIALOGS
# ================================

Write-Host "`n=== ANALYSE DIALOGS ===" -ForegroundColor Cyan

$dialogPath = "desktop-javafx\src\main\java\com\magsav\desktop\dialogs"
$dialogs = @(
    "ClientDialog.java",
    "EquipmentDialog.java", 
    "SAVDialog.java",
    "VehicleDialog.java",
    "PersonnelDialog.java",
    "ContractDialog.java"
)

foreach ($dialog in $dialogs) {
    $dialogFile = Join-Path $dialogPath $dialog
    if (Test-Path $dialogFile) {
        $dialogLines = (Get-Content $dialogFile).Count
        Write-Host "   ✅ $dialog présent: $dialogLines lignes" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $dialog manquant" -ForegroundColor Red
    }
}

# ProjectDialog dans salesinstallation
$projectDialogFile = "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\salesinstallation\ProjectDialog.java"
if (Test-Path $projectDialogFile) {
    $projectDialogLines = (Get-Content $projectDialogFile).Count
    Write-Host "   ✅ ProjectDialog.java présent: $projectDialogLines lignes" -ForegroundColor Green
} else {
    Write-Host "   ❌ ProjectDialog.java manquant" -ForegroundColor Red
}

# ================================
# ANALYSE API SERVICE
# ================================

Write-Host "`n=== ANALYSE API SERVICE ===" -ForegroundColor Cyan

$apiServiceFile = "desktop-javafx\src\main\java\com\magsav\desktop\service\ApiService.java"
if (Test-Path $apiServiceFile) {
    $apiContent = Get-Content $apiServiceFile -Raw
    $apiLines = (Get-Content $apiServiceFile).Count
    Write-Host "   ✅ ApiService présent: $apiLines lignes" -ForegroundColor Green
    
    # Vérification méthodes principales
    if ($apiContent -match "getClients|getEquipment|getSAVRequests") {
        Write-Host "   ✅ Méthodes GET détectées" -ForegroundColor Green
    }
    if ($apiContent -match "createClient|createEquipment|createSAV") {
        Write-Host "   ✅ Méthodes CREATE détectées" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ ApiService manquant" -ForegroundColor Red
}

# ================================
# ANALYSE RESSOURCES CSS
# ================================

Write-Host "`n=== ANALYSE RESSOURCES CSS ===" -ForegroundColor Cyan

$cssPath = "desktop-javafx\src\main\resources"
$cssFiles = Get-ChildItem -Path $cssPath -Filter "*.css" -Recurse -ErrorAction SilentlyContinue

if ($cssFiles) {
    foreach ($css in $cssFiles) {
        Write-Host "   ✅ CSS: $($css.Name)" -ForegroundColor Green
    }
} else {
    Write-Host "   ❌ Aucun fichier CSS trouvé" -ForegroundColor Red
}

# ================================
# RÉSUMÉ FINAL
# ================================

Write-Host "`n=== RÉSUMÉ ÉVALUATION ===" -ForegroundColor Yellow

$totalModules = 7
$presentModules = 0

# Compte modules présents
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\ClientManagerView.java") { $presentModules++ }
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\EquipmentManagerView.java") { $presentModules++ }
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\sav\SAVManagerView.java") { $presentModules++ }
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\VehicleManagerView.java") { $presentModules++ }
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\PersonnelManagerView.java") { $presentModules++ }
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\ContractManagerView.java") { $presentModules++ }
if (Test-Path "desktop-javafx\src\main\java\com\magsav\desktop\views\salesinstallation\ProjectManagerView.java") { $presentModules++ }

$completionRate = [math]::Round(($presentModules / $totalModules) * 100, 1)

Write-Host "`n📊 MODULES: $presentModules/$totalModules présents ($completionRate%)" -ForegroundColor $(if($completionRate -gt 80) { "Green" } else { "Yellow" })

Write-Host "`n=== FIN TEST COMPLET ===" -ForegroundColor Yellow