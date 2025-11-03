# Script PowerShell pour corriger tous les problemes d'encodage dans ContractDialog.java
# Gere tous les onglets et messages

$filePath = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\ContractDialog.java"

Write-Host "Correction des problemes d'encodage dans ContractDialog.java..." -ForegroundColor Yellow

if (-not (Test-Path $filePath)) {
    Write-Host "Fichier non trouve: $filePath" -ForegroundColor Red
    exit 1
}

try {
    # Lecture du fichier avec encodage UTF-8
    $content = Get-Content -Path $filePath -Encoding UTF8 -Raw
    Write-Host "Fichier lu avec succes" -ForegroundColor Green

    # Corrections des onglets
    Write-Host "Correction des titres d'onglets..." -ForegroundColor Cyan
    
    # Onglet Dates - correction de l'emoji et texte corrompu
    $content = $content -replace 'Tab datesTab = new Tab\("📝€¦ Dates"\);', 'Tab datesTab = new Tab("📅 Dates");'
    
    # Onglet Financier - correction emoji corrompu
    $content = $content -replace 'Tab financialTab = new Tab\("Ã°Å¸â€™Â° Financier"\);', 'Tab financialTab = new Tab("💰 Financier");'
    
    # Onglet Détails - correction emoji corrompu
    $content = $content -replace 'Tab detailsTab = new Tab\("📝€ž Détails"\);', 'Tab detailsTab = new Tab("📝 Détails");'

    # Corrections des labels et messages
    Write-Host "Correction des montants en euros..." -ForegroundColor Cyan
    
    # Symbol euro corrompu
    $content = $content -replace 'Montant total \(Ã¢â€šÂ¬\)', 'Montant total (€)'
    
    # Messages de validation avec emojis corrompus
    Write-Host "Correction des messages de validation..." -ForegroundColor Cyan
    
    # Messages d'erreur avec emoji warning corrompu
    $content = $content -replace '⚠ Ã¯Â¸Â La date de début ne peut pas être après la date de fin', '⚠️ La date de début ne peut pas être après la date de fin'
    $content = $content -replace '⚠ Ã¯Â¸Â La date de fin ne peut pas être avant la date de début', '⚠️ La date de fin ne peut pas être avant la date de début'
    $content = $content -replace '⚠ Ã¯Â¸Â Vérifiez les dates', '⚠️ Vérifiez les dates'
    
    # Message de validation réussie avec emoji check corrompu
    $content = $content -replace 'Ã¢Å"â€¦ Formulaire valide', '✅ Formulaire valide'

    Write-Host "Ecriture du fichier corrige..." -ForegroundColor Cyan
    
    # Écriture du fichier avec UTF-8 sans BOM pour éviter les problèmes
    $utf8NoBomEncoding = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText((Resolve-Path $filePath), $content, $utf8NoBomEncoding)
    
    Write-Host "Corrections d'encodage appliquees avec succes!" -ForegroundColor Green
    
} catch {
    Write-Host "Erreur lors de la correction: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "Correction terminee!" -ForegroundColor Green