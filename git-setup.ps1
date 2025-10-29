# Script de configuration Git pour MAGSAV-3.0
# Usage: Exécuter ce script pour configurer un repository distant

Write-Host "🔗 Configuration Git pour MAGSAV-3.0" -ForegroundColor Cyan
Write-Host "=" * 50 -ForegroundColor Cyan

# Status du repository local
Write-Host "📊 Status du repository local:" -ForegroundColor Yellow
git log --oneline -3
Write-Host ""

# Commandes pour ajouter un remote (à adapter selon votre service)
Write-Host "📋 Pour ajouter un repository distant, utilisez:" -ForegroundColor Yellow
Write-Host "GitHub:" -ForegroundColor White
Write-Host "  git remote add origin https://github.com/votre-username/MAGSAV-3.0.git" -ForegroundColor Gray
Write-Host "  git branch -M main" -ForegroundColor Gray  
Write-Host "  git push -u origin main" -ForegroundColor Gray
Write-Host ""

Write-Host "GitLab:" -ForegroundColor White
Write-Host "  git remote add origin https://gitlab.com/votre-username/MAGSAV-3.0.git" -ForegroundColor Gray
Write-Host "  git branch -M main" -ForegroundColor Gray
Write-Host "  git push -u origin main" -ForegroundColor Gray
Write-Host ""

Write-Host "Azure DevOps:" -ForegroundColor White
Write-Host "  git remote add origin https://dev.azure.com/votre-org/votre-project/_git/MAGSAV-3.0" -ForegroundColor Gray
Write-Host "  git branch -M main" -ForegroundColor Gray
Write-Host "  git push -u origin main" -ForegroundColor Gray
Write-Host ""

Write-Host "✅ Repository local prêt avec 128 fichiers commités!" -ForegroundColor Green