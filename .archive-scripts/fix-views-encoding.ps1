# Script de correction d'encodage pour les vues Client, Personnel et Contrats
Write-Host "Correction des vues Client, Personnel et Contrats..." -ForegroundColor Green

# PersonnelManagerView.java
$file1 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\PersonnelManagerView.java"
if (Test-Path $file1) {
    $content = Get-Content -Path $file1 -Encoding UTF8 -Raw
    
    # Corrections d'encodage
    $content = $content -replace 'complÃƒÂ¨te', 'complete'
    $content = $content -replace 'FonctionnalitÃƒÂ©s', 'Fonctionnalites'
    $content = $content -replace 'dÃƒÂ©taillÃƒÂ©', 'detaille'
    $content = $content -replace 'Ã°Å¸â€œâ€¹ Gestion du Personnel', 'Gestion du Personnel'
    $content = $content -replace 'Ã°Å¸â€Â Recherche:', 'Recherche:'
    $content = $content -replace 'prÃƒÂ©nom', 'prenom'
    $content = $content -replace 'EmployÃƒÂ©', 'Employe'
    $content = $content -replace 'IntÃƒÂ©rimaire', 'Interimaire'
    $content = $content -replace 'En congÃƒÂ©', 'En conge'
    $content = $content -replace 'TerminÃƒÂ©', 'Termine'
    $content = $content -replace 'DÃƒÂ©partement:', 'Departement:'
    $content = $content -replace 'rafraÃƒÂ®chir', 'rafraichir'
    $content = $content -replace 'Ã°Å¸â€â€ž Actualiser', 'Actualiser'
    $content = $content -replace 'TÃƒÂ©lÃƒÂ©phone', 'Telephone'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file1, $content, $utf8NoBom)
    Write-Host "PersonnelManagerView corrige" -ForegroundColor Green
}

# ContractManagerView.java
$file2 = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\ContractManagerView.java"
if (Test-Path $file2) {
    $content = Get-Content -Path $file2 -Encoding UTF8 -Raw
    
    # Corrections d'encodage
    $content = $content -replace 'complÃƒÂ¨te', 'complete'
    $content = $content -replace 'FonctionnalitÃƒÂ©s', 'Fonctionnalites'
    $content = $content -replace 'dÃƒÂ©taillÃƒÂ©', 'detaille'
    $content = $content -replace 'crÃƒÂ©er', 'creer'
    $content = $content -replace 'APRÃƒË†S', 'APRES'
    
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($file2, $content, $utf8NoBom)
    Write-Host "ContractManagerView corrige" -ForegroundColor Green
}

Write-Host "Compilation..." -ForegroundColor Yellow
& .\gradlew :desktop-javafx:compileJava