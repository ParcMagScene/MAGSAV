# ==========================================
# MAGSAV-3.0 - Script de démarrage backend
# ==========================================

param(
    [switch]$Clean = $false
)

# Configuration UTF-8 pour PowerShell
$OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# Configuration
$JAVA_HOME = "C:\Users\aalou\.jdk\jdk-21.0.8"
$PROJECT_ROOT = "C:\Users\aalou\MAGSAV-3.0"
$PORT = 8080

Write-Host "🚀 MAGSAV-3.0 Backend Starter" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

# 1. Nettoyage si demandé
if ($Clean) {
    Write-Host "🧹 Nettoyage complet..." -ForegroundColor Yellow
    
    # Arrêter tous les processus Java
    Get-Process -Name "java*" -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 2
    
    # Nettoyer le cache Gradle
    if (Test-Path "$PROJECT_ROOT\.gradle") {
        Write-Host "   - Nettoyage cache Gradle..." -ForegroundColor Cyan
        Remove-Item "$PROJECT_ROOT\.gradle\caches\*" -Recurse -Force -ErrorAction SilentlyContinue
    }
    
    Write-Host "✅ Nettoyage terminé" -ForegroundColor Green
}

# 2. Vérifications préalables
Write-Host "🔍 Vérifications système..." -ForegroundColor Cyan

# Vérifier Java
if (-not (Test-Path "$JAVA_HOME\bin\java.exe")) {
    Write-Host "❌ Java non trouvé dans $JAVA_HOME" -ForegroundColor Red
    exit 1
}

# Vérifier port disponible
$portInUse = Get-NetTCPConnection -LocalPort $PORT -ErrorAction SilentlyContinue
if ($portInUse) {
    Write-Host "⚠️  Port $PORT déjà utilisé - Arrêt du processus..." -ForegroundColor Yellow
    $process = Get-Process -Id $portInUse.OwningProcess -ErrorAction SilentlyContinue
    if ($process) {
        $process | Stop-Process -Force
        Start-Sleep -Seconds 2
    }
}

# 3. Configuration environnement
$env:JAVA_HOME = $JAVA_HOME
$env:GRADLE_OPTS = "-Xmx2g -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Duser.language=fr -Duser.country=FR -Dconsole.encoding=UTF-8"
Set-Location $PROJECT_ROOT

Write-Host "✅ Environnement configuré" -ForegroundColor Green
Write-Host "   - Java: $(& "$JAVA_HOME\bin\java.exe" -version 2>&1 | Select-String "version" | Select-Object -First 1)" -ForegroundColor Cyan
Write-Host "   - Port: $PORT" -ForegroundColor Cyan
Write-Host "   - Workspace: $PROJECT_ROOT" -ForegroundColor Cyan

# 4. Démarrage backend
Write-Host "`n🎯 Démarrage du backend..." -ForegroundColor Yellow
Write-Host "   Patientez pendant l'initialisation..." -ForegroundColor Cyan

try {
    # Utiliser Start-Process pour isoler complètement le processus
    $processInfo = New-Object System.Diagnostics.ProcessStartInfo
    $processInfo.FileName = "$PROJECT_ROOT\gradlew.bat"
    $processInfo.Arguments = ":backend:bootRun --no-daemon --console=plain"
    $processInfo.WorkingDirectory = $PROJECT_ROOT
    $processInfo.UseShellExecute = $true
    $processInfo.WindowStyle = [System.Diagnostics.ProcessWindowStyle]::Normal
    
    # Variables d'environnement
    $processInfo.EnvironmentVariables["JAVA_HOME"] = $JAVA_HOME
    $processInfo.EnvironmentVariables["GRADLE_OPTS"] = $env:GRADLE_OPTS
    
    $process = [System.Diagnostics.Process]::Start($processInfo)
    
    Write-Host "✅ Backend démarré - PID: $($process.Id)" -ForegroundColor Green
    Write-Host "`n📡 Vérification de la connectivité..." -ForegroundColor Cyan
    
    # Attendre que le service soit disponible (max 60 secondes)
    $timeout = 60
    $elapsed = 0
    $connected = $false
    
    while ($elapsed -lt $timeout -and -not $connected) {
        Start-Sleep -Seconds 2
        $elapsed += 2
        
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$PORT/actuator/health" -UseBasicParsing -TimeoutSec 3 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                $connected = $true
            }
        } catch {
            # Continuer à attendre
        }
        
        # Afficher progression
        if ($elapsed % 10 -eq 0) {
            Write-Host "   Attente... ${elapsed}s/$timeout" -ForegroundColor Yellow
        }
    }
    
    if ($connected) {
        Write-Host "`n🎉 Backend MAGSAV-3.0 opérationnel !" -ForegroundColor Green
        Write-Host "================================" -ForegroundColor Green
        Write-Host "📍 URL: http://localhost:$PORT" -ForegroundColor Cyan
        Write-Host "📊 H2 Console: http://localhost:$PORT/h2-console" -ForegroundColor Cyan
        Write-Host "📖 API Docs: http://localhost:$PORT/swagger-ui.html" -ForegroundColor Cyan
        Write-Host "`n💡 Le backend fonctionne en arrière-plan (PID: $($process.Id))" -ForegroundColor Yellow
        Write-Host "   Pour l'arrêter: Stop-Process -Id $($process.Id)" -ForegroundColor Yellow
    } else {
        Write-Host "`n⚠️  Timeout atteint - Le backend met du temps à démarrer" -ForegroundColor Yellow
        Write-Host "   Vérifiez manuellement: http://localhost:$PORT" -ForegroundColor Cyan
    }
    
} catch {
    Write-Host "`n❌ Erreur de démarrage: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}