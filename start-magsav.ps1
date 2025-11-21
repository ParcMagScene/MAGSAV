# Script de demarrage automatique de MAGSAV
Write-Host "Demarrage de MAGSAV..." -ForegroundColor Green

# Verifier si le backend est deja en cours d'execution
$backendRunning = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*backend*bootRun*" }

if (-not $backendRunning) {
    Write-Host "Demarrage du backend..." -ForegroundColor Yellow
    
    # Demarrer le backend en arriere-plan
    Start-Process -NoNewWindow -FilePath "powershell" -ArgumentList "-Command", ".\gradlew :backend:bootRun"
    
    Write-Host "Attente du demarrage du backend (15 secondes)..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    
    # Verifier que le backend repond
    $maxRetries = 5
    $retryCount = 0
    $backendReady = $false
    
    do {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 5 -ErrorAction Stop
            if ($response.StatusCode -eq 200) {
                $backendReady = $true
                Write-Host "Backend demarre avec succes !" -ForegroundColor Green
            }
        }
        catch {
            $retryCount++
            Write-Host "Tentative $retryCount/$maxRetries - Backend en cours de demarrage..." -ForegroundColor Yellow
            Start-Sleep -Seconds 3
        }
    } while (-not $backendReady -and $retryCount -lt $maxRetries)
    
    if (-not $backendReady) {
        Write-Host "Le backend semble ne pas repondre, mais on continue..." -ForegroundColor Yellow
    }
} else {
    Write-Host "Backend deja en cours d'execution" -ForegroundColor Green
}

Write-Host "Demarrage de l'application desktop..." -ForegroundColor Cyan
.\gradlew :desktop-javafx:run