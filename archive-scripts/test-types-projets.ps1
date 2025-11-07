# Test de vérification des types de projets corrects
Write-Host "=== Test des types de projets ===" -ForegroundColor Green

# Recherche des types dans ProjectDialog
Write-Host "`nVérification ProjectDialog.java:" -ForegroundColor Cyan
$projectDialogTypes = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\salesinstallation\ProjectDialog.java" -Pattern 'addAll.*"Vente"'
if ($projectDialogTypes) {
    Write-Host "✅ ProjectDialog contient les bons types: $($projectDialogTypes.Line.Trim())" -ForegroundColor Green
} else {
    Write-Host "❌ Types incorrects dans ProjectDialog" -ForegroundColor Red
}

# Recherche des types dans ProjectManagerView  
Write-Host "`nVérification ProjectManagerView.java:" -ForegroundColor Cyan
$projectViewTypes = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\salesinstallation\ProjectManagerView.java" -Pattern 'addAll.*"Vente"'
if ($projectViewTypes) {
    Write-Host "✅ ProjectManagerView contient les bons types: $($projectViewTypes.Line.Trim())" -ForegroundColor Green
} else {
    Write-Host "❌ Types incorrects dans ProjectManagerView" -ForegroundColor Red
}

# Recherche des types dans ApiService
Write-Host "`nVérification ApiService.java:" -ForegroundColor Cyan
$apiServiceTypes = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\service\ApiService.java" -Pattern 'createProjectMap.*"Vente"'
if ($apiServiceTypes) {
    Write-Host "✅ ApiService contient les données de test avec les bons types" -ForegroundColor Green
} else {
    Write-Host "❌ Types incorrects dans les données de test ApiService" -ForegroundColor Red
}

Write-Host "`n=== Types attendus ===" -ForegroundColor Yellow
Write-Host "• Vente" -ForegroundColor White
Write-Host "• Installation" -ForegroundColor White  
Write-Host "• Location" -ForegroundColor White
Write-Host "• Prestation" -ForegroundColor White
Write-Host "• Maintenance" -ForegroundColor White