# Script de nettoyage des commentaires corrompus
# Supprime les commentaires francais corrompus et les remplace par des versions anglaises

param(
    [switch]$DryRun = $false
)

$searchPath = "C:\Users\aalou\MAGSAV-3.0\backend\src"

Write-Host "========================================"
Write-Host "  Nettoyage des commentaires"
Write-Host "========================================"

if ($DryRun) {
    Write-Host "[MODE SIMULATION]"
}

$javaFiles = Get-ChildItem -Path $searchPath -Recurse -Filter "*.java" -File
$totalFiles = $javaFiles.Count
$fixedFiles = 0

Write-Host "Analyse de $totalFiles fichiers..."

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $original = $content
    
    # Supprimer les lignes de commentaires contenant des caracteres corrompus
    $lines = $content -split "`r?`n"
    $cleanLines = @()
    
    foreach ($line in $lines) {
        # Detecter les commentaires corrompus (patterns multiples)
        if ($line -match '^\s*//.*[ÃÂ]' -or $line -match '^\s*//.*\?f\?' -or $line -match '^\s*//.*Ã') {
            # Remplacer par un commentaire vide ou ignorer
            continue
        }
        $cleanLines += $line
    }
    
    $newContent = $cleanLines -join "`r`n"
    
    if ($newContent -ne $original) {
        if ($DryRun) {
            Write-Host "[A NETTOYER] $($file.Name)"
        } else {
            $utf8NoBom = New-Object System.Text.UTF8Encoding $false
            [System.IO.File]::WriteAllText($file.FullName, $newContent, $utf8NoBom)
            Write-Host "[NETTOYE] $($file.Name)"
        }
        $fixedFiles++
    }
}

Write-Host "========================================"
Write-Host "  Fichiers analyses:  $totalFiles"
Write-Host "  Fichiers nettoyes: $fixedFiles"
Write-Host "========================================"
