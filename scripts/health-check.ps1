# =============================================================
# MAGSAV-3.0 - Script de vérification de santé
# Vérifie que tous les composants sont opérationnels
# =============================================================

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host " MAGSAV-3.0 - Vérification Santé" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

$allOk = $true

# 1. Vérifier Java
Write-Host "☕ Java..." -NoNewline
try {
    $javaVersion = java -version 2>&1 | Select-Object -First 1
    if ($javaVersion -match "21") {
        Write-Host " ✅ ($javaVersion)" -ForegroundColor Green
    } else {
        Write-Host " ⚠️  Version trouvée: $javaVersion (attendu: 21+)" -ForegroundColor Yellow
        $allOk = $false
    }
} catch {
    Write-Host " ❌ Non trouvé" -ForegroundColor Red
    $allOk = $false
}

# 2. Vérifier Node.js
Write-Host "📦 Node.js..." -NoNewline
try {
    $nodeVersion = node --version 2>&1
    if ($nodeVersion -match "v\d{2,}") {
        Write-Host " ✅ ($nodeVersion)" -ForegroundColor Green
    } else {
        Write-Host " ⚠️  Version trouvée: $nodeVersion (attendu: 18+)" -ForegroundColor Yellow
        $allOk = $false
    }
} catch {
    Write-Host " ❌ Non trouvé" -ForegroundColor Red
    $allOk = $false
}

# 3. Vérifier npm
Write-Host "📦 npm..." -NoNewline
try {
    $npmVersion = npm --version 2>&1
    Write-Host " ✅ ($npmVersion)" -ForegroundColor Green
} catch {
    Write-Host " ❌ Non trouvé" -ForegroundColor Red
    $allOk = $false
}

# 4. Vérifier structure du projet
Write-Host "`n📁 Structure du projet..." -ForegroundColor Yellow

$requiredDirs = @("backend", "web-frontend", "common-models", "scripts", "gradle")
$missingDirs = @()

foreach ($dir in $requiredDirs) {
    if (Test-Path $dir) {
        Write-Host "   ✅ $dir" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $dir manquant" -ForegroundColor Red
        $missingDirs += $dir
        $allOk = $false
    }
}

# 5. Vérifier fichiers essentiels
$requiredFiles = @("build.gradle", "settings.gradle", "gradlew.bat", "README.md")
$missingFiles = @()

foreach ($file in $requiredFiles) {
    if (Test-Path $file) {
        Write-Host "   ✅ $file" -ForegroundColor Green
    } else {
        Write-Host "   ❌ $file manquant" -ForegroundColor Red
        $missingFiles += $file
        $allOk = $false
    }
}

# 6. Vérifier dépendances frontend
Write-Host "`n📦 Dépendances frontend..." -ForegroundColor Yellow
if (Test-Path "web-frontend\node_modules") {
    Write-Host "   ✅ node_modules présent" -ForegroundColor Green
} else {
    Write-Host "   ⚠️  node_modules absent - Exécuter: cd web-frontend && npm install" -ForegroundColor Yellow
}

# 7. Vérifier compilation backend
Write-Host "`n🔨 Compilation backend..." -ForegroundColor Yellow
Write-Host "   Compilation en cours..." -NoNewline
$compileResult = ./gradlew.bat :backend:compileJava --console=plain 2>&1 | Out-String
if ($compileResult -match "BUILD SUCCESSFUL") {
    Write-Host " ✅ Succès" -ForegroundColor Green
} else {
    Write-Host " ❌ Échec" -ForegroundColor Red
    $allOk = $false
}

# 8. Vérifier TypeScript frontend
Write-Host "`n📝 TypeScript frontend..." -ForegroundColor Yellow
if (Test-Path "web-frontend\node_modules") {
    Write-Host "   Vérification en cours..." -NoNewline
    Push-Location web-frontend
    $tsResult = npm run type-check 2>&1 | Out-String
    Pop-Location
    if ($tsResult -match "No issues found" -or $tsResult -match "0 errors") {
        Write-Host " ✅ Pas d'erreurs" -ForegroundColor Green
    } else {
        Write-Host " ⚠️  Erreurs détectées" -ForegroundColor Yellow
    }
} else {
    Write-Host "   ⏭️  Ignoré (node_modules absent)" -ForegroundColor Gray
}

# 9. Vérifier ports disponibles
Write-Host "`n🔌 Ports disponibles..." -ForegroundColor Yellow

$port8080 = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($port8080) {
    Write-Host "   ⚠️  Port 8080 déjà utilisé (PID: $($port8080.OwningProcess))" -ForegroundColor Yellow
} else {
    Write-Host "   ✅ Port 8080 disponible" -ForegroundColor Green
}

$port3000 = Get-NetTCPConnection -LocalPort 3000 -ErrorAction SilentlyContinue
if ($port3000) {
    Write-Host "   ⚠️  Port 3000 déjà utilisé (PID: $($port3000.OwningProcess))" -ForegroundColor Yellow
} else {
    Write-Host "   ✅ Port 3000 disponible" -ForegroundColor Green
}

# Résultat final
Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
if ($allOk) {
    Write-Host " ✨ Tout est OK! Prêt à démarrer." -ForegroundColor Green
    Write-Host "=====================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Démarrer avec: .\scripts\start-dev.ps1" -ForegroundColor Cyan
} else {
    Write-Host " ⚠️  Problèmes détectés" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Corriger les erreurs ci-dessus avant de démarrer" -ForegroundColor Yellow
}
Write-Host ""
