# Script final de correction d'encodage MASSIVE
Write-Host "=== CORRECTION FINALE MASSIVE D'ENCODAGE ===" -ForegroundColor Red

# Corrections pour EquipmentDialog.java (suite)
$file1 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\EquipmentDialog.java"
if (Test-Path $file1) {
    Write-Host "Correction finale EquipmentDialog.java..." -ForegroundColor Yellow
    $content = [System.IO.File]::ReadAllText($file1, [System.Text.Encoding]::UTF8)
    
    # Corrections restantes
    $content = $content -replace 'RÃ©fÃ©rence', 'Reference'
    $content = $content -replace 'SpÃ©cifications', 'Specifications'
    $content = $content -replace 'VidÃ©o', 'Video'
    $content = $content -replace 'DerniÃ¨re', 'Derniere'
    $content = $content -replace 'sÃ©lectionnÃ©e', 'selectionnee'
    $content = $content -replace 'SÃ©lectionner', 'Selectionner'
    $content = $content -replace 'catÃ©gories', 'categories'
    $content = $content -replace 'GÃ©nÃ©ration', 'Generation'
    $content = $content -replace 'donnÃ©es', 'donnees'
    $content = $content -replace 'rÃ©el', 'reel'
    $content = $content -replace 'catÃ©gorie', 'categorie'
    $content = $content -replace 'Ãªtre', 'etre'
    $content = $content -replace 'gÃ©nÃ©rales', 'generales'
    $content = $content -replace 'financiÃ¨res', 'financieres'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file1, $content, $utf8NoBom)
    Write-Host "Equipment termine !" -ForegroundColor Green
}

# Corrections pour VehicleManagerView.java (suite)
$file2 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\VehicleManagerView.java"
if (Test-Path $file2) {
    Write-Host "Correction finale VehicleManagerView.java..." -ForegroundColor Yellow
    $content = [System.IO.File]::ReadAllText($file2, [System.Text.Encoding]::UTF8)
    
    # Corrections restantes
    $content = $content -replace 'modifiÃƒÂ©', 'modifie'
    $content = $content -replace 'vÃƒÂ©hicule', 'vehicule'
    $content = $content -replace 'ÃƒÂ©tÃƒÂ©', 'ete'
    $content = $content -replace 'succÃƒÂ¨s', 'succes'
    $content = $content -replace 'ÃƒÂ ', 'a'
    $content = $content -replace 'kilomÃƒÂ©trage', 'kilometrage'
    $content = $content -replace 'LouÃƒÂ©', 'Loue'
    $content = $content -replace 'RÃƒÂ©servÃƒÂ©', 'Reserve'
    $content = $content -replace 'VÃƒÂ©rifier', 'Verifier'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file2, $content, $utf8NoBom)
    Write-Host "Vehicle termine !" -ForegroundColor Green
}

Write-Host "Compilation..." -ForegroundColor Cyan
& .\gradlew :desktop-javafx:compileJava --quiet

if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCES TOTAL ! Lancement..." -ForegroundColor Green
    & .\gradlew :desktop-javafx:run
} else {
    Write-Host "Erreurs restantes..." -ForegroundColor Red
}