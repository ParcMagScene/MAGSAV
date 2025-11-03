# MAGSAV-3.0 - Test Interface Modules (Chemins corriges)
Write-Host "=== TEST INTERFACE MAGSAV-3.0 ===" -ForegroundColor Yellow

# Base path correcte
$basePath = "desktop-javafx\src\main\java\com\magscene\magsav\desktop"

# 1. Verification des modules principaux
Write-Host "`nANALYSE MODULES:" -ForegroundColor Cyan

$modules = @(
    @{ Name = "ClientManagerView"; Path = "$basePath\view\ClientManagerView.java" },
    @{ Name = "EquipmentManagerView"; Path = "$basePath\view\EquipmentManagerView.java" },
    @{ Name = "SAVManagerView"; Path = "$basePath\view\sav\SAVManagerView.java" },
    @{ Name = "VehicleManagerView"; Path = "$basePath\view\VehicleManagerView.java" },
    @{ Name = "PersonnelManagerView"; Path = "$basePath\view\PersonnelManagerView.java" },
    @{ Name = "ContractManagerView"; Path = "$basePath\view\ContractManagerView.java" },
    @{ Name = "ProjectManagerView"; Path = "$basePath\view\salesinstallation\ProjectManagerView.java" }
)

$presentCount = 0
foreach ($module in $modules) {
    if (Test-Path $module.Path) {
        $lines = (Get-Content $module.Path).Count
        Write-Host "  ✅ $($module.Name): $lines lignes" -ForegroundColor Green
        $presentCount++
    } else {
        Write-Host "  ❌ $($module.Name): MANQUANT" -ForegroundColor Red
    }
}

# 2. Verification des dialogs
Write-Host "`nANALYSE DIALOGS:" -ForegroundColor Cyan

$dialogs = @(
    "$basePath\dialog\ClientDialog.java",
    "$basePath\dialog\EquipmentDialog.java", 
    "$basePath\dialog\SAVDialog.java",
    "$basePath\dialog\VehicleDialog.java",
    "$basePath\dialog\PersonnelDialog.java",
    "$basePath\dialog\ContractDialog.java",
    "$basePath\dialog\salesinstallation\ProjectDialog.java"
)

$dialogCount = 0
foreach ($dialog in $dialogs) {
    $name = Split-Path $dialog -Leaf
    if (Test-Path $dialog) {
        $lines = (Get-Content $dialog).Count
        Write-Host "  ✅ $name`: $lines lignes" -ForegroundColor Green
        $dialogCount++
    } else {
        Write-Host "  ❌ $name`: MANQUANT" -ForegroundColor Red
    }
}

# 3. Verification ApiService
Write-Host "`nANALYSE API SERVICE:" -ForegroundColor Cyan

$apiService = "$basePath\service\ApiService.java"
if (Test-Path $apiService) {
    $lines = (Get-Content $apiService).Count
    Write-Host "  ✅ ApiService.java: $lines lignes" -ForegroundColor Green
} else {
    Write-Host "  ❌ ApiService.java: MANQUANT" -ForegroundColor Red
}

# 4. Application principale
Write-Host "`nANALYSE APPLICATION:" -ForegroundColor Cyan

$mainApp = "$basePath\MagsavDesktopApplication.java"
if (Test-Path $mainApp) {
    $lines = (Get-Content $mainApp).Count
    Write-Host "  ✅ MagsavDesktopApplication.java: $lines lignes" -ForegroundColor Green
} else {
    Write-Host "  ❌ MagsavDesktopApplication.java: MANQUANT" -ForegroundColor Red
}

# 5. Exploration structure view/
Write-Host "`nEXPLORATION VIEWS:" -ForegroundColor Cyan
$viewPath = "$basePath\view"
if (Test-Path $viewPath) {
    $viewFiles = Get-ChildItem -Path $viewPath -Filter "*.java" -Recurse
    foreach ($file in $viewFiles) {
        $lines = (Get-Content $file.FullName).Count
        Write-Host "  📁 $($file.Name): $lines lignes" -ForegroundColor Blue
    }
}

# 6. Exploration structure dialog/
Write-Host "`nEXPLORATION DIALOGS:" -ForegroundColor Cyan
$dialogPath = "$basePath\dialog"
if (Test-Path $dialogPath) {
    $dialogFiles = Get-ChildItem -Path $dialogPath -Filter "*.java" -Recurse
    foreach ($file in $dialogFiles) {
        $lines = (Get-Content $file.FullName).Count
        Write-Host "  📁 $($file.Name): $lines lignes" -ForegroundColor Blue
    }
}

# Resume final
Write-Host "`n=== RESUME ===" -ForegroundColor Yellow
Write-Host "Modules Manager: $presentCount/7" -ForegroundColor $(if($presentCount -eq 7) { "Green" } else { "Yellow" })
Write-Host "Dialogs: $dialogCount/7" -ForegroundColor $(if($dialogCount -eq 7) { "Green" } else { "Yellow" })

$total = $presentCount + $dialogCount
$maxTotal = 14
$percentage = [math]::Round(($total / $maxTotal) * 100)

Write-Host "Completion globale: $percentage%" -ForegroundColor $(if($percentage -gt 80) { "Green" } elseif($percentage -gt 60) { "Yellow" } else { "Red" })

Write-Host "`n=== FIN TEST ===" -ForegroundColor Yellow