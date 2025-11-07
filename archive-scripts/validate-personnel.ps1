# Script de validation Personnel - Intermittents
Write-Host "=== Validation Personnel Intermittents ===" -ForegroundColor Green

# Verification des fichiers modifies
$files = @(
    "backend\src\main\java\com\magscene\magsav\backend\entity\Personnel.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\PersonnelDialog.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\PersonnelManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\service\ApiService.java"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "✅ Fichier existe: $file" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier manquant: $file" -ForegroundColor Red
    }
}

# Recherche des modifications
Write-Host "`nRecherche des modifications:" -ForegroundColor Cyan
$performer = Select-String -Path $files -Pattern "PERFORMER" -ErrorAction SilentlyContinue
if ($performer.Count -gt 0) {
    Write-Host "✅ Type PERFORMER trouve dans $($performer.Count) emplacements" -ForegroundColor Green
} else {
    Write-Host "❌ Type PERFORMER non trouve" -ForegroundColor Red
}

$specialties = Select-String -Path $files -Pattern "specialties" -ErrorAction SilentlyContinue
if ($specialties.Count -gt 0) {
    Write-Host "✅ Specialites trouve dans $($specialties.Count) emplacements" -ForegroundColor Green
} else {
    Write-Host "❌ Specialites non trouve" -ForegroundColor Red
}

Write-Host "`n=== Instructions test ===" -ForegroundColor Yellow
Write-Host "1. Ouvrir Personnel dans l'application"
Write-Host "2. Creer nouveau personnel type Intermittent"
Write-Host "3. Tester le champ specialites"
Write-Host "4. Verifier l'affichage tableau"