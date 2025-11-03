# MAGSAV-3.0 - Test Interface Modules
Write-Host "=== TEST INTERFACE MAGSAV-3.0 ===" -ForegroundColor Yellow

# 1. Verification des modules principaux
Write-Host "`nANALYSE MODULES:" -ForegroundColor Cyan

$modules = @(
    @{ Name = "ClientManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\ClientManagerView.java" },
    @{ Name = "EquipmentManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\EquipmentManagerView.java" },
    @{ Name = "SAVManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\sav\SAVManagerView.java" },
    @{ Name = "VehicleManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\VehicleManagerView.java" },
    @{ Name = "PersonnelManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\PersonnelManagerView.java" },
    @{ Name = "ContractManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\ContractManagerView.java" },
    @{ Name = "ProjectManagerView"; Path = "desktop-javafx\src\main\java\com\magsav\desktop\views\salesinstallation\ProjectManagerView.java" }
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
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\ClientDialog.java",
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\EquipmentDialog.java",
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\SAVDialog.java",
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\VehicleDialog.java",
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\PersonnelDialog.java",
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\ContractDialog.java",
    "desktop-javafx\src\main\java\com\magsav\desktop\dialogs\salesinstallation\ProjectDialog.java"
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

$apiService = "desktop-javafx\src\main\java\com\magsav\desktop\service\ApiService.java"
if (Test-Path $apiService) {
    $lines = (Get-Content $apiService).Count
    Write-Host "  ✅ ApiService.java: $lines lignes" -ForegroundColor Green
} else {
    Write-Host "  ❌ ApiService.java: MANQUANT" -ForegroundColor Red
}

# 4. Application principale
Write-Host "`nANALYSE APPLICATION:" -ForegroundColor Cyan

$mainApp = "desktop-javafx\src\main\java\com\magsav\desktop\MagsavDesktopApplication.java"
if (Test-Path $mainApp) {
    $lines = (Get-Content $mainApp).Count
    Write-Host "  ✅ MagsavDesktopApplication.java: $lines lignes" -ForegroundColor Green
} else {
    Write-Host "  ❌ MagsavDesktopApplication.java: MANQUANT" -ForegroundColor Red
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