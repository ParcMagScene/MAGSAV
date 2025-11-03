# Script PowerShell pour corriger l'encodage rapidement
Write-Host "Correction des problemes d'encodage..." -ForegroundColor Green

# ServiceRequestManagerView
$file1 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\ServiceRequestManagerView.java"
if (Test-Path $file1) {
    $content = Get-Content -Path $file1 -Encoding UTF8 -Raw
    $content = $content -replace 'Ã©', 'e' -replace 'Ã¨', 'e' -replace 'Ã§', 'c' -replace 'Ã´', 'o' -replace 'Ã ', 'a'
    $content = $content -replace 'ÃƒÂ©', 'e' -replace 'ÃƒÂ¨', 'e' -replace 'ÃƒÂ§', 'c' -replace 'ÃƒÂ´', 'o'
    $content = $content -replace 'ContrÃƒÂ´les', 'Controles'
    $content = $content -replace 'piÃƒÂ¨ces', 'pieces'
    $content = $content -replace 'RÃƒÂ©solue', 'Resolue'
    $content = $content -replace 'FermÃƒÂ©e', 'Fermee'
    $content = $content -replace 'AnnulÃƒÂ©e', 'Annulee'
    $content = $content -replace 'PrioritÃƒÂ©', 'Priorite'
    $content = $content -replace 'RÃƒÂ©paration', 'Reparation'
    $content = $content -replace 'prÃƒÂ©ventive', 'preventive'
    $content = $content -replace 'assignÃƒÂ©', 'assigne'
    $content = $content -replace 'CrÃƒÂ©ÃƒÂ©', 'Cree'
    $content = $content -replace 'trouvÃƒÂ©e', 'trouvee'
    $content = $content -replace 'RÃƒÂ©solues', 'Resolues'
    $content = $content -replace 'SuccÃƒÂ¨s', 'Succes'
    $content = $content -replace 'crÃƒÂ©ÃƒÂ©e', 'creee'
    $content = $content -replace 'crÃƒÂ©er', 'creer'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file1, $content, $utf8NoBom)
    Write-Host "ServiceRequestManagerView corrige" -ForegroundColor Green
}

# VehicleManagerView
$file2 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\VehicleManagerView.java"
if (Test-Path $file2) {
    $content = Get-Content -Path $file2 -Encoding UTF8 -Raw
    $content = $content -replace 'vÃƒÂ©hicules', 'vehicules'
    $content = $content -replace 'vÃƒÂ©hicule', 'vehicule'
    $content = $content -replace 'VÃƒÂ©hicule', 'Vehicule'
    $content = $content -replace 'crÃƒÂ©ÃƒÂ©', 'cree'
    $content = $content -replace 'ÃƒÂ©tÃƒÂ©', 'ete'
    $content = $content -replace 'modifiÃƒÂ©', 'modifie'
    $content = $content -replace 'crÃƒÂ©ation', 'creation'
    $content = $content -replace 'SuccÃƒÂ¨s', 'Succes'
    $content = $content -replace 'crÃƒÂ©er', 'creer'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file2, $content, $utf8NoBom)
    Write-Host "VehicleManagerView corrige" -ForegroundColor Green
}

Write-Host "Compilation..." -ForegroundColor Yellow
& .\gradlew :desktop-javafx:compileJava