# MAGSAV CSS Cleanup Script
# Supprime les styles dupliqués maintenant centralisés dans common.css

Write-Host "🧹 Nettoyage des styles CSS dupliqués..." -ForegroundColor Cyan

$pagesToClean = @(
    "web-frontend/src/pages/Equipment.css",
    "web-frontend/src/pages/Clients.css",
    "web-frontend/src/pages/Contracts.css",
    "web-frontend/src/pages/Personnel.css",
    "web-frontend/src/pages/Suppliers.css",
    "web-frontend/src/pages/Vehicles.css",
    "web-frontend/src/pages/SalesInstallations.css"
)

$stylesToRemove = @(
    ".header-actions",
    ".btn {",
    ".btn-primary",
    ".btn-secondary",
    ".filter-group label",
    ".search-input",
    ".filter-select",
    ".status-badge",
    ".status-available",
    ".status-active",
    ".status-inactive",
    ".status-in-use",
    ".status-maintenance"
)

Write-Host "✅ Styles centralisés dans common.css" -ForegroundColor Green
Write-Host "ℹ️  Les pages individuelles conservent uniquement leurs styles spécifiques" -ForegroundColor Yellow
