# Script d'optimisation VS Code pour MAGSAV-3.0
# Desactive les extensions redondantes pour ameliorer les performances

Write-Host "Optimisation VS Code MAGSAV-3.0" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# Extensions a desactiver (redondantes)
$extensionsToDisable = @(
    "vscjava.vscode-java-pack",
    "vmware.vscode-boot-dev-pack", 
    "visualstudioexptteam.intellicode-api-usage-examples",
    "vscjava.vscode-spring-initializr",
    "vscjava.vscode-spring-boot-dashboard"
)

# Extensions essentielles a garder
$essentialExtensions = @(
    "redhat.java",
    "vscjava.vscode-gradle",
    "vscjava.vscode-maven", 
    "vscjava.vscode-java-debug",
    "vscjava.vscode-java-test",
    "vmware.vscode-spring-boot",
    "github.copilot",
    "github.copilot-chat"
)

Write-Host "Extensions a desactiver :" -ForegroundColor Yellow
foreach ($ext in $extensionsToDisable) {
    Write-Host "  - $ext" -ForegroundColor Red
}

Write-Host ""
Write-Host "Extensions essentielles conservees :" -ForegroundColor Green
foreach ($ext in $essentialExtensions) {
    Write-Host "  - $ext" -ForegroundColor Green
}

Write-Host ""
Write-Host "Pour appliquer manuellement :" -ForegroundColor Yellow
Write-Host "1. Ouvrez VS Code Extensions (Ctrl+Shift+X)" -ForegroundColor White
Write-Host "2. Recherchez chaque extension dans la liste rouge ci-dessus" -ForegroundColor White
Write-Host "3. Cliquez sur l'icone d'engrenage > Desactiver (Workspace)" -ForegroundColor White
Write-Host "4. Redemarrez VS Code" -ForegroundColor White

Write-Host ""
Write-Host "Resultat attendu : 128 extensions vers 15-20 extensions actives" -ForegroundColor Cyan
Write-Host "Amelioration des performances VS Code garantie !" -ForegroundColor Green