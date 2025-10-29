# Script de réparation d'urgence - Restaurer les imports List supprimés

Write-Host "=== REPARATION IMPORTS LIST ===" -ForegroundColor Red

function Restore-ListImport {
    param([string]$filePath)
    
    if (!(Test-Path $filePath)) { return $false }
    
    $content = Get-Content $filePath -Raw
    
    # Vérifier si le fichier utilise List mais n'a pas l'import
    if ($content -match '\bList<' -and $content -notmatch 'import java\.util\.List;') {
        # Trouver la position après le dernier import jakarta ou la ligne package
        $lines = $content -split "`r?`n"
        $importIndex = -1
        
        for ($i = 0; $i -lt $lines.Length; $i++) {
            if ($lines[$i] -match '^import\s+java\.') {
                $importIndex = $i
            }
        }
        
        # Si pas d'import java trouvé, chercher après les imports jakarta
        if ($importIndex -eq -1) {
            for ($i = 0; $i -lt $lines.Length; $i++) {
                if ($lines[$i] -match '^import\s+jakarta\.') {
                    $importIndex = $i
                }
            }
        }
        
        # Si toujours pas trouvé, ajouter après package
        if ($importIndex -eq -1) {
            for ($i = 0; $i -lt $lines.Length; $i++) {
                if ($lines[$i] -match '^package\s+') {
                    $importIndex = $i + 1
                    break
                }
            }
        }
        
        if ($importIndex -ne -1) {
            # Insérer l'import List
            $lines = $lines[0..$importIndex] + "import java.util.List;" + $lines[($importIndex + 1)..($lines.Length - 1)]
            $newContent = $lines -join "`r`n"
            
            # Vérifier aussi ArrayList si nécessaire
            if ($newContent -match '\bArrayList<' -and $newContent -notmatch 'import java\.util\.ArrayList;') {
                $lines = $newContent -split "`r?`n"
                $lines = $lines[0..$importIndex] + "import java.util.ArrayList;" + $lines[($importIndex + 1)..($lines.Length - 1)]
                $newContent = $lines -join "`r`n"
            }
            
            Set-Content -Path $filePath -Value $newContent -Encoding UTF8
            Write-Host "  Restored List import: $($filePath -replace '.*\\', '')" -ForegroundColor Green
            return $true
        }
    }
    
    return $false
}

# Fichiers identifiés avec des erreurs List manquantes
$filesToFix = @(
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\controller\CategoryRestController.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\controller\ClientController.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\controller\ContactController.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\repository\CategoryRepository.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\repository\ClientRepository.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\repository\ContactRepository.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\entity\Category.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\entity\Client.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\entity\Contract.java",
    "C:\Users\aalou\MAGSAV-3.0\backend\src\main\java\com\magscene\magsav\backend\entity\Equipment.java"
)

$fixedCount = 0
foreach ($file in $filesToFix) {
    if (Restore-ListImport -filePath $file) {
        $fixedCount++
    }
}

Write-Host "Restaure les imports List dans $fixedCount fichiers" -ForegroundColor Green

# Test rapide de compilation
Write-Host "Test de compilation..." -ForegroundColor Cyan
Set-Location "C:\Users\aalou\MAGSAV-3.0"
& .\gradlew :backend:compileJava --no-daemon

Write-Host "=== REPARATION IMPORTS TERMINEE ===" -ForegroundColor Green