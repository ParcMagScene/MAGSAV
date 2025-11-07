# Script final pour ajouter tous les imports List manquants

Write-Host "=== AJOUT IMPORTS LIST FINAUX ===" -ForegroundColor Cyan

# Fichiers nécessitant l'import List d'après l'erreur de compilation
$files = @(
    "backend\src\main\java\com\magscene\magsav\backend\controller\ContractController.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ContractRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\EquipmentPhotoRestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\EquipmentRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\EquipmentPhotoRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\EquipmentRestController.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\PersonnelController.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\PersonnelRepository.java",
    "backend\src\main\java\com\magscene\magsav\backend\controller\ProjectController.java",
    "backend\src\main\java\com\magscene\magsav\backend\repository\ProjectRepository.java"
)

$fixedCount = 0
$rootPath = "C:\Users\aalou\MAGSAV-3.0"

foreach ($relativePath in $files) {
    $fullPath = Join-Path $rootPath $relativePath
    
    if (Test-Path $fullPath) {
        try {
            $content = Get-Content $fullPath -Raw
            
            # Si le fichier utilise List mais n'a pas l'import
            if ($content -match '\bList<' -and $content -notmatch 'import java\.util\.List;') {
                $lines = $content -split "`r?`n"
                
                # Trouver où insérer l'import (après les imports jakarta ou java existants)
                $importIndex = -1
                for ($i = 0; $i -lt $lines.Length; $i++) {
                    if ($lines[$i] -match '^import\s+(jakarta|java)\.') {
                        $importIndex = $i
                    }
                }
                
                if ($importIndex -ne -1) {
                    # Insérer l'import
                    $lines = $lines[0..$importIndex] + "import java.util.List;" + $lines[($importIndex + 1)..($lines.Length - 1)]
                    Set-Content -Path $fullPath -Value ($lines -join "`r`n") -Encoding UTF8
                    Write-Host "  Added List: $($relativePath -replace '.*\\', '')" -ForegroundColor Green
                    $fixedCount++
                }
            }
        }
        catch {
            Write-Host "  Error: $relativePath - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "Imports List ajoutes dans $fixedCount fichiers" -ForegroundColor Green

# Test de compilation finale
Write-Host "Test compilation finale..." -ForegroundColor Yellow
Set-Location $rootPath
& .\gradlew :backend:compileJava --no-daemon -q

if ($LASTEXITCODE -eq 0) {
    Write-Host "🎉 SUCCES COMPLET - Backend compile parfaitement!" -ForegroundColor Green
    
    # Test build complet
    Write-Host "Test build complet..." -ForegroundColor Cyan
    & .\gradlew build -x test --no-daemon -q
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "🚀 BUILD COMPLET REUSSI!" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Build partiel - backend OK mais autres modules ont des problemes" -ForegroundColor Yellow
    }
} else {
    Write-Host "❌ Erreurs restantes dans backend..." -ForegroundColor Red
}

Write-Host "=== REPARATION TERMINEE ===" -ForegroundColor Cyan