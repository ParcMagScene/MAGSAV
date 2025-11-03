# Correction finale rapide des derniers problemes d'encodage
Write-Host "CORRECTION FINALE RAPIDE..." -ForegroundColor Green

# EquipmentDialog.java corrections rapides
$file1 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\EquipmentDialog.java"
if (Test-Path $file1) {
    Write-Host "Correction Equipment..." -ForegroundColor Cyan
    $content = [System.IO.File]::ReadAllText($file1, [System.Text.Encoding]::UTF8)
    
    $content = $content -creplace 'Ã©quipement', 'equipement'
    $content = $content -creplace 'gÃ©nÃ©rales', 'generales'  
    $content = $content -creplace 'GÃ©nÃ©ral', 'General'
    $content = $content -creplace 'DÃ©tails', 'Details'
    $content = $content -creplace 'CatÃ©gorie', 'Categorie'
    $content = $content -creplace 'SÃ©lectionnez', 'Selectionnez'
    $content = $content -creplace 'catÃ©gorie', 'categorie'
    $content = $content -creplace 'GÃ©nÃ©rer', 'Generer'
    $content = $content -creplace 'EntrepÃ´t', 'Entrepot'
    $content = $content -creplace 'ModÃ¨le', 'Modele'
    $content = $content -creplace 'NumÃ©ro', 'Numero'
    $content = $content -creplace 'sÃ©rie', 'serie'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file1, $content, $utf8NoBom)
    Write-Host "Equipment OK" -ForegroundColor Green
}

# VehicleManagerView.java corrections rapides
$file2 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\VehicleManagerView.java"  
if (Test-Path $file2) {
    Write-Host "Correction Vehicle..." -ForegroundColor Cyan
    $content = [System.IO.File]::ReadAllText($file2, [System.Text.Encoding]::UTF8)
    
    $content = $content -creplace 'vÃƒÂ©hicules', 'vehicules'
    $content = $content -creplace 'VÃƒÂ©hicule', 'Vehicule'  
    $content = $content -creplace 'spÃƒÂ©cifiques', 'specifiques'
    $content = $content -creplace 'SÃƒÂ©lection', 'Selection'
    $content = $content -creplace 'rÃƒÂ©el', 'reel'
    $content = $content -creplace 'ajoutÃƒÂ©e', 'ajoutee'
    $content = $content -creplace 'supprimÃƒÂ©', 'supprime'
    $content = $content -creplace 'ÃƒÂ©tÃƒÂ©', 'ete'
    $content = $content -creplace 'succÃƒÂ¨s', 'succes'
    $content = $content -creplace 'Ã°Å¸â€Â´', ''
    $content = $content -creplace 'Ã°Å¸Å¸Â¡', ''
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file2, $content, $utf8NoBom)
    Write-Host "Vehicle OK" -ForegroundColor Green
}

Write-Host "Compilation..." -ForegroundColor Yellow
& .\gradlew :desktop-javafx:compileJava --quiet

Write-Host "CORRECTION TERMINEE !" -ForegroundColor Green