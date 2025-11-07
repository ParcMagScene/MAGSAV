# Script de validation - Categories equipement
Write-Host "=== VALIDATION CATEGORIES EQUIPEMENT ===" -ForegroundColor Green

# Test 1: Verification CategoriesConfigManager
Write-Host "`n1. CategoriesConfigManager:" -ForegroundColor Cyan
$configManager = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\config\CategoriesConfigManager.java"
if (Test-Path $configManager) {
    Write-Host "✅ CategoriesConfigManager trouve" -ForegroundColor Green
} else {
    Write-Host "❌ CategoriesConfigManager manquant" -ForegroundColor Red
}

# Test 2: Verification CategoriesConfigView
Write-Host "`n2. CategoriesConfigView:" -ForegroundColor Cyan
$configView = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\config\CategoriesConfigView.java"
if (Test-Path $configView) {
    Write-Host "✅ CategoriesConfigView trouve" -ForegroundColor Green
} else {
    Write-Host "❌ CategoriesConfigView manquant" -ForegroundColor Red
}

# Test 3: Verification EquipmentDialog
Write-Host "`n3. EquipmentDialog modifie:" -ForegroundColor Cyan
$dialog = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\EquipmentDialog.java"
$categoriesManager = Select-String -Path $dialog -Pattern "CategoriesConfigManager" -Quiet
if ($categoriesManager) {
    Write-Host "✅ EquipmentDialog integre CategoriesConfigManager" -ForegroundColor Green
} else {
    Write-Host "❌ Integration EquipmentDialog incomplete" -ForegroundColor Red
}

# Test 4: Verification application principale
Write-Host "`n4. Application principale:" -ForegroundColor Cyan
$app = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\MagsavDesktopApplication.java"
$categoriesTab = Select-String -Path $app -Pattern "categoriesTab" -Quiet
if ($categoriesTab) {
    Write-Host "✅ Onglet categories ajoute" -ForegroundColor Green
} else {
    Write-Host "❌ Onglet categories manquant" -ForegroundColor Red
}

# Test 5: Compilation
Write-Host "`n5. Compilation:" -ForegroundColor Cyan
$compileResult = & .\gradlew :desktop-javafx:compileJava --quiet 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation reussie" -ForegroundColor Green
} else {
    Write-Host "❌ Erreurs compilation" -ForegroundColor Red
}

Write-Host "`n=== FONCTIONNALITES CATEGORIES ===" -ForegroundColor Yellow
Write-Host "Gestion centralisee categories equipement" -ForegroundColor White
Write-Host "Arborescence hierarchique avec couleurs" -ForegroundColor White  
Write-Host "Attribution equipements aux categories" -ForegroundColor White
Write-Host "Integration avec EquipmentDialog" -ForegroundColor White
Write-Host "Sauvegarde preferences locales" -ForegroundColor White

Write-Host "`n=== ACCES ===" -ForegroundColor Cyan
Write-Host "Parametres > Categories Equipement" -ForegroundColor White