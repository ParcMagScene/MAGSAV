# Script de validation des modifications Personnel
Write-Host "=== Validation des modifications Personnel ===" -ForegroundColor Green

# Test 1: Vérification du nouveau type "Intermittent du spectacle" dans PersonnelDialog
Write-Host "`n1. Vérification PersonnelDialog.java:" -ForegroundColor Cyan
$dialogTypes = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\PersonnelDialog.java" -Pattern 'PERFORMER.*"Intermittent du spectacle"'
if ($dialogTypes) {
    Write-Host "✅ Nouveau type PERFORMER ajouté dans PersonnelDialog" -ForegroundColor Green
} else {
    Write-Host "❌ Type PERFORMER manquant dans PersonnelDialog" -ForegroundColor Red
}

# Test 2: Vérification du champ spécialités dans PersonnelDialog
$specialtiesField = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\PersonnelDialog.java" -Pattern "specialtiesArea"
if ($specialtiesField) {
    Write-Host "✅ Champ spécialités ajouté dans PersonnelDialog" -ForegroundColor Green
} else {
    Write-Host "❌ Champ spécialités manquant dans PersonnelDialog" -ForegroundColor Red
}

# Test 3: Vérification du filtre dans PersonnelManagerView
Write-Host "`n2. Vérification PersonnelManagerView.java:" -ForegroundColor Cyan
$viewFilter = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\PersonnelManagerView.java" -Pattern '"Intermittent du spectacle"'
if ($viewFilter) {
    Write-Host "✅ Filtre mis à jour dans PersonnelManagerView" -ForegroundColor Green
} else {
    Write-Host "❌ Filtre non mis à jour dans PersonnelManagerView" -ForegroundColor Red
}

# Test 4: Vérification de la colonne spécialités
$specialtiesColumn = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\PersonnelManagerView.java" -Pattern 'TableColumn.*"Spécialités"'
if ($specialtiesColumn) {
    Write-Host "✅ Colonne spécialités ajoutée dans le tableau" -ForegroundColor Green
} else {
    Write-Host "❌ Colonne spécialités manquante dans le tableau" -ForegroundColor Red
}

# Test 5: Vérification du backend Personnel.java
Write-Host "`n3. Vérification Backend Personnel.java:" -ForegroundColor Cyan
$backendType = Select-String -Path "backend\src\main\java\com\magscene\magsav\backend\entity\Personnel.java" -Pattern 'PERFORMER.*"Intermittent du spectacle"'
if ($backendType) {
    Write-Host "✅ Type PERFORMER ajouté dans l'entité backend" -ForegroundColor Green
} else {
    Write-Host "❌ Type PERFORMER manquant dans l'entité backend" -ForegroundColor Red
}

$backendSpecialties = Select-String -Path "backend\src\main\java\com\magscene\magsav\backend\entity\Personnel.java" -Pattern "specialties"
if ($backendSpecialties) {
    Write-Host "✅ Champ specialties ajouté dans l'entité backend" -ForegroundColor Green
} else {
    Write-Host "❌ Champ specialties manquant dans l'entité backend" -ForegroundColor Red
}

# Test 6: Vérification des données de test ApiService
Write-Host "`n4. Vérification ApiService.java:" -ForegroundColor Cyan
$intermittentData = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\service\ApiService.java" -Pattern "Intermittent"
if ($intermittentData) {
    Write-Host "✅ Données de test intermittents ajoutées dans ApiService" -ForegroundColor Green
} else {
    Write-Host "❌ Données de test intermittents manquantes dans ApiService" -ForegroundColor Red
}

$specialtiesGenerator = Select-String -Path "desktop-javafx\src\main\java\com\magscene\magsav\desktop\service\ApiService.java" -Pattern "generateSpecialties"
if ($specialtiesGenerator) {
    Write-Host "✅ Générateur de spécialités ajouté dans ApiService" -ForegroundColor Green
} else {
    Write-Host "❌ Générateur de spécialités manquant dans ApiService" -ForegroundColor Red
}

Write-Host "`n=== Résumé des nouveautés ===" -ForegroundColor Yellow
Write-Host "🆕 Nouveau type: Intermittent du spectacle" -ForegroundColor White
Write-Host "🎯 Spécialités multiples: Son, Éclairage, Vidéo, etc." -ForegroundColor White  
Write-Host "📊 Colonne spécialités dans le tableau Personnel" -ForegroundColor White
Write-Host "🔧 Backend prêt avec champ specialties" -ForegroundColor White
Write-Host "📝 Données de test enrichies" -ForegroundColor White

Write-Host "`n=== Instructions de test ===" -ForegroundColor Magenta
Write-Host "1. Aller dans Personnel > Nouveau personnel" -ForegroundColor White
Write-Host "2. Selectionner type Intermittent du spectacle" -ForegroundColor White
Write-Host "3. Remplir le champ Specialites (ex: Son, Video, Regie)" -ForegroundColor White
Write-Host "4. Verifier affichage dans le tableau" -ForegroundColor White