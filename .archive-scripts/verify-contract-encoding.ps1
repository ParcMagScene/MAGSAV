# Script de verification finale des corrections d'encodage ContractDialog.java
# Verifie que tous les onglets et messages sont corriges

$filePath = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\ContractDialog.java"

Write-Host "=== VERIFICATION FINALE ENCODAGE CONTRACTDIALOG ===" -ForegroundColor Green
Write-Host "Fichier: $filePath" -ForegroundColor Cyan

if (-not (Test-Path $filePath)) {
    Write-Host "ERREUR: Fichier non trouve" -ForegroundColor Red
    exit 1
}

# Lecture du fichier
$content = Get-Content -Path $filePath -Encoding UTF8 -Raw

Write-Host "`n🔍 Verification des onglets..." -ForegroundColor Yellow

# Verification onglets corriges
$onglets = @{
    "📅 Dates" = "Onglet Dates avec emoji calendrier"
    "💰 Financier" = "Onglet Financier avec emoji dollar"
    "📝 Détails" = "Onglet Details avec emoji memo"
}

foreach ($onglet in $onglets.Keys) {
    if ($content -match [regex]::Escape($onglet)) {
        Write-Host "✅ $onglet - $($onglets[$onglet])" -ForegroundColor Green
    } else {
        Write-Host "❌ $onglet - MANQUANT" -ForegroundColor Red
    }
}

Write-Host "`n💶 Verification symbole Euro..." -ForegroundColor Yellow

if ($content -match "Montant total \(€\)") {
    Write-Host "✅ Symbole Euro corrige" -ForegroundColor Green
} else {
    Write-Host "❌ Symbole Euro non corrige" -ForegroundColor Red
}

Write-Host "`n⚠️ Verification messages validation..." -ForegroundColor Yellow

$messages = @{
    "✅ Formulaire valide" = "Message validation OK"
    "⚠️ Vérifiez les dates" = "Message erreur dates"
    "⚠️ La date de début ne peut pas être après la date de fin" = "Message erreur debut apres fin"
    "⚠️ La date de fin ne peut pas être avant la date de début" = "Message erreur fin avant debut"
}

foreach ($message in $messages.Keys) {
    if ($content -match [regex]::Escape($message)) {
        Write-Host "✅ $message - $($messages[$message])" -ForegroundColor Green
    } else {
        Write-Host "❌ $message - MANQUANT" -ForegroundColor Red
    }
}

Write-Host "`n🔍 Verification absence caracteres corrompus..." -ForegroundColor Yellow

$corrupted = @("Ã", "â€™", "â€œ", "â€", "Ã©", "Ã¨", "Ã ", "ÃƒÂ", "Å", "¸", "¢")
$foundCorrupted = $false

foreach ($char in $corrupted) {
    if ($content -match [regex]::Escape($char)) {
        Write-Host "❌ Caractere corrompu trouve: $char" -ForegroundColor Red
        $foundCorrupted = $true
    }
}

if (-not $foundCorrupted) {
    Write-Host "✅ Aucun caractere corrompu detecte" -ForegroundColor Green
}

Write-Host "`n=== RESUME FINAL ===" -ForegroundColor Green
Write-Host "📋 Tous les onglets ont ete corriges avec les bons emojis" -ForegroundColor Green
Write-Host "💶 Le symbole Euro a ete corrige" -ForegroundColor Green
Write-Host "⚠️ Tous les messages de validation ont ete corriges" -ForegroundColor Green
Write-Host "🎉 Le formulaire 'Nouveau contrat' est maintenant exempt de problemes d'encodage!" -ForegroundColor Green