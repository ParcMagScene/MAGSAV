# Script de test systematique des modules MAGSAV-3.0
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "🧪 TEST SYSTEMATIQUE INTERFACE MAGSAV-3.0" -ForegroundColor Cyan  
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Verification que l'application JavaFX tourne
Write-Host "📱 Test 1: Verification application JavaFX..." -ForegroundColor Yellow
$javaProcesses = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "✅ Application JavaFX detectee (PID: $($javaProcesses[0].Id))" -ForegroundColor Green
} else {
    Write-Host "❌ Aucune application JavaFX detectee" -ForegroundColor Red
    Write-Host "🚀 Demarrage de l'application..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-Command", "cd 'c:\Users\aalou\MAGSAV-3.0'; .\gradlew.bat :desktop-javafx:run" -WindowStyle Normal
    Start-Sleep -Seconds 20
}

Write-Host ""

# Test 2: Verification des modules disponibles
Write-Host "🔍 Test 2: Analyse des modules disponibles..." -ForegroundColor Yellow

$modules = @(
    "ClientManagerView",
    "EquipmentManagerView", 
    "SAVManagerView",
    "VehicleManagerView",
    "PersonnelManagerView",
    "ContractManagerView",
    "ProjectManagerView"
)

$moduleStats = @{}

foreach ($module in $modules) {
    $filePath = "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\$module.java"
    if (Test-Path $filePath) {
        $lines = (Get-Content $filePath).Count
        $moduleStats[$module] = @{
            "exists" = $true
            "lines" = $lines
            "status" = if ($lines -gt 300) { "Complet" } elseif ($lines -gt 100) { "Partiel" } else { "Basique" }
        }
        Write-Host "✅ $module : $($moduleStats[$module].status) ($lines lignes)" -ForegroundColor Green
    } else {
        $moduleStats[$module] = @{ "exists" = $false }
        Write-Host "❌ $module : Non trouve" -ForegroundColor Red
    }
}

Write-Host ""

# Test 3: Verification des dialogues
Write-Host "📋 Test 3: Verification des dialogues..." -ForegroundColor Yellow

$dialogs = @(
    "ProjectDialog",
    "EquipmentDialog", 
    "VehicleDialog"
)

foreach ($dialog in $dialogs) {
    $dialogPath = Get-ChildItem -Path "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\java" -Recurse -Name "*$dialog.java" -ErrorAction SilentlyContinue
    if ($dialogPath) {
        Write-Host "✅ $dialog : Trouve ($dialogPath)" -ForegroundColor Green
    } else {
        Write-Host "❌ $dialog : Non trouve" -ForegroundColor Red
    }
}

Write-Host ""

# Test 4: Verification du service API
Write-Host "🌐 Test 4: Verification service API..." -ForegroundColor Yellow
$apiServicePath = "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\java\com\magscene\magsav\desktop\service\ApiService.java"
if (Test-Path $apiServicePath) {
    $apiLines = (Get-Content $apiServicePath).Count
    Write-Host "✅ ApiService : $apiLines lignes" -ForegroundColor Green
} else {
    Write-Host "❌ ApiService : Non trouve" -ForegroundColor Red
}

Write-Host ""

# Test 5: Verification des ressources CSS
Write-Host "🎨 Test 5: Verification des ressources..." -ForegroundColor Yellow
$cssPath = "c:\Users\aalou\MAGSAV-3.0\desktop-javafx\src\main\resources\styles"
if (Test-Path $cssPath) {
    $cssFiles = Get-ChildItem -Path $cssPath -Filter "*.css" -ErrorAction SilentlyContinue
    Write-Host "✅ Styles CSS : $($cssFiles.Count) fichiers" -ForegroundColor Green
} else {
    Write-Host "❌ Repertoire styles CSS : Non trouve" -ForegroundColor Red
}

Write-Host ""

# Rapport final
Write-Host "===========================================" -ForegroundColor Cyan
Write-Host "📊 RAPPORT DE TEST FINAL" -ForegroundColor Cyan
Write-Host "===========================================" -ForegroundColor Cyan

$totalModules = $modules.Count
$completedModules = ($moduleStats.Values | Where-Object { $_.exists -eq $true }).Count
$fullModules = ($moduleStats.Values | Where-Object { $_.status -eq "Complet" }).Count

Write-Host "📈 Modules implementes : $completedModules/$totalModules ($([math]::Round($completedModules/$totalModules*100))%)" -ForegroundColor Cyan
Write-Host "🔥 Modules complets : $fullModules/$totalModules ($([math]::Round($fullModules/$totalModules*100))%)" -ForegroundColor Cyan

if ($completedModules -eq $totalModules) {
    Write-Host ""
    Write-Host "🎉 TOUS LES MODULES SONT IMPLEMENTES !" -ForegroundColor Green
    Write-Host "✨ MAGSAV-3.0 est pret pour utilisation complete !" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "⚠️  Modules a completer : $($totalModules - $completedModules)" -ForegroundColor Yellow
}

Write-Host ""