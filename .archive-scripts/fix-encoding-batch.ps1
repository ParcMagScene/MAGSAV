# Script PowerShell pour corriger l'encodage de tous les fichiers JavaFX
# Correction des caractères corrompus dans les chaînes utilisateur

Write-Host "=== Correction en lot des problèmes d'encodage dans les fichiers JavaFX ===" -ForegroundColor Green

$files = @(
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\ServiceRequestManagerView.java",
    "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\VehicleManagerView.java"
)

$replacements = @{
    # Corrections courantes d'encodage
    "Ã©" = "e"
    "Ã¨" = "e"
    "Ã " = "a"
    "Ã§" = "c"
    "Ã¹" = "u"
    "Ã¢" = "a"
    "Ãª" = "e"
    "Ã®" = "i"
    "Ã´" = "o"
    "Ã»" = "u"
    "Ã«" = "e"
    "Ã¯" = "i"
    "Ã¼" = "u"
    "Ã‚" = "A"
    "ÃŠ" = "E"
    "Î" = "I"
    "Ã'" = "O"
    "Ã›" = "U"
    "Ã‰" = "E"
    "Ãˆ" = "E"
    "Ã€" = "A"
    "Ã‡" = "C"
    "Ã™" = "U"
    
    # Corriger les patterns spécifiques trouvés
    "ÃƒÂ©" = "e"
    "ÃƒÂ¨" = "e" 
    "ÃƒÂ " = "a"
    "ÃƒÂ§" = "c"
    "ÃƒÂ´" = "o"
    "ÃƒÂ©s" = "es"
    "RÃƒÂ©" = "Re"
    "FermÃƒÂ©e" = "Fermee"
    "AnnulÃƒÂ©e" = "Annulee"
    "CrÃƒÂ©" = "Cre"
    "SuccÃƒÂ¨s" = "Succes"
    "PrioritÃƒÂ©" = "Priorite"
    "vÃƒÂ©hicule" = "vehicule"
    "VÃƒÂ©hicule" = "Vehicule"
    "spÃƒÂ©" = "spe"
    "ModÃƒÂ¨le" = "Modele"
    "KilomÃƒÂ©trage" = "Kilometrage"
    "rÃƒÂ©" = "re"
    "ContrÃƒÂ´les" = "Controles"
    "piÃƒÂ¨ces" = "pieces"
    "modifiÃƒÂ©" = "modifie"
    "crÃƒÂ©ÃƒÂ©" = "cree"
    "ÃƒÂ©tÃƒÂ©" = "ete"
    "crÃƒÂ©ation" = "creation"
    "expirÃƒÂ©s" = "expires"
    "assignÃƒÂ©" = "assigne"
    "trouvÃƒÂ©e" = "trouvee"
    "RÃƒÂ©solue" = "Resolue"
    "RÃƒÂ©solues" = "Resolues"
    "RÃƒÂ©paration" = "Reparation"
    "prÃƒÂ©ventive" = "preventive"
}

foreach ($file in $files) {
    $filePath = Join-Path $PSScriptRoot $file
    
    if (Test-Path $filePath) {
        Write-Host "Traitement de $file..." -ForegroundColor Yellow
        
        $content = Get-Content -Path $filePath -Encoding UTF8 -Raw
        
        foreach ($oldText in $replacements.Keys) {
            $newText = $replacements[$oldText]
            if ($content.Contains($oldText)) {
                $content = $content.Replace($oldText, $newText)
                Write-Host "  Remplacé '$oldText' par '$newText'" -ForegroundColor Cyan
            }
        }
        
        # Sauvegarder avec encodage UTF-8 sans BOM
        $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
        [System.IO.File]::WriteAllText($filePath, $content, $utf8NoBom)
        
        Write-Host "  Fichier corrigé: $file" -ForegroundColor Green
    } else {
        Write-Host "Fichier non trouvé: $file" -ForegroundColor Red
    }
}

Write-Host "=== Correction terminée ===" -ForegroundColor Green
Write-Host "Compilation des changements..." -ForegroundColor Yellow

# Compiler pour vérifier
& .\gradlew :desktop-javafx:compileJava

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation réussie! Tous les problèmes d'encodage ont été corrigés." -ForegroundColor Green
} else {
    Write-Host "Erreur de compilation. Vérifiez les modifications." -ForegroundColor Red
}