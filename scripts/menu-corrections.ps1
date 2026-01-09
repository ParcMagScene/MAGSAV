# ============================================
# Menu Interactif - Gestion Corrections MAGSAV
# ============================================

function Show-Menu {
    Clear-Host
    Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║                                                                ║" -ForegroundColor Cyan
    Write-Host "║           🔧 MENU CORRECTIONS - MAGSAV-3.0                     ║" -ForegroundColor Cyan
    Write-Host "║                                                                ║" -ForegroundColor Cyan
    Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "📋 DOCUMENTATION" -ForegroundColor Yellow
    Write-Host "  1. Consulter le rapport d'audit complet (AUDIT-COMPLET.md)" -ForegroundColor White
    Write-Host "  2. Voir le récapitulatif des corrections (RÉCAPITULATIF-CORRECTIONS.md)" -ForegroundColor White
    Write-Host "  3. Guide migration logger frontend (MIGRATION-LOGGER.md)" -ForegroundColor White
    Write-Host ""
    Write-Host "🔧 CORRECTIONS AUTOMATIQUES" -ForegroundColor Yellow
    Write-Host "  4. Exécuter toutes les corrections critiques (RECOMMANDÉ)" -ForegroundColor Green
    Write-Host "  5. Créer backup manuel de application.properties" -ForegroundColor White
    Write-Host ""
    Write-Host "🧪 TESTS & VALIDATION" -ForegroundColor Yellow
    Write-Host "  6. Tester l'API Service Requests" -ForegroundColor White
    Write-Host "  7. Tester l'API Equipment" -ForegroundColor White
    Write-Host "  8. Vérifier les logs backend" -ForegroundColor White
    Write-Host ""
    Write-Host "🚀 DÉMARRAGE" -ForegroundColor Yellow
    Write-Host "  9. Redémarrer le backend" -ForegroundColor White
    Write-Host "  10. Démarrer frontend + backend" -ForegroundColor White
    Write-Host ""
    Write-Host "  Q. Quitter" -ForegroundColor Red
    Write-Host ""
}

function Show-AuditReport {
    $auditFile = "c:\Users\aalou\MAGSAV-3.0\AUDIT-COMPLET.md"
    if (Test-Path $auditFile) {
        code $auditFile
        Write-Host "✅ Rapport d'audit ouvert dans VS Code" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier AUDIT-COMPLET.md non trouvé" -ForegroundColor Red
    }
    Pause
}

function Show-RecapReport {
    $recapFile = "c:\Users\aalou\MAGSAV-3.0\RÉCAPITULATIF-CORRECTIONS.md"
    if (Test-Path $recapFile) {
        code $recapFile
        Write-Host "✅ Récapitulatif ouvert dans VS Code" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier RÉCAPITULATIF-CORRECTIONS.md non trouvé" -ForegroundColor Red
    }
    Pause
}

function Show-LoggerGuide {
    $loggerFile = "c:\Users\aalou\MAGSAV-3.0\web-frontend\MIGRATION-LOGGER.md"
    if (Test-Path $loggerFile) {
        code $loggerFile
        Write-Host "✅ Guide migration logger ouvert dans VS Code" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier MIGRATION-LOGGER.md non trouvé" -ForegroundColor Red
    }
    Pause
}

function Run-CriticalFixes {
    Write-Host "🔧 Exécution des corrections critiques..." -ForegroundColor Yellow
    Write-Host ""
    
    $scriptPath = "c:\Users\aalou\MAGSAV-3.0\scripts\fix-critical-issues.ps1"
    if (Test-Path $scriptPath) {
        & $scriptPath
    } else {
        Write-Host "❌ Script fix-critical-issues.ps1 non trouvé" -ForegroundColor Red
    }
    
    Write-Host ""
    Pause
}

function Create-Backup {
    $appProps = "c:\Users\aalou\MAGSAV-3.0\backend\src\main\resources\application.properties"
    $backupFile = "$appProps.backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
    
    if (Test-Path $appProps) {
        Copy-Item $appProps $backupFile
        Write-Host "✅ Backup créé: $backupFile" -ForegroundColor Green
    } else {
        Write-Host "❌ Fichier application.properties non trouvé" -ForegroundColor Red
    }
    
    Pause
}

function Test-ServiceRequestsAPI {
    Write-Host "🧪 Test API Service Requests..." -ForegroundColor Yellow
    Write-Host ""
    
    try {
        $response = Invoke-RestMethod "http://localhost:8080/api/service-requests" -ErrorAction Stop
        
        Write-Host "✅ API accessible" -ForegroundColor Green
        Write-Host "📊 Nombre de demandes: $($response.Count)" -ForegroundColor Cyan
        
        $statuses = $response | Group-Object -Property status | Select-Object Name, Count
        Write-Host ""
        Write-Host "📈 Répartition par statut:" -ForegroundColor Cyan
        $statuses | Format-Table -AutoSize
        
        # Vérifier uniquement PENDING/VALIDATED
        $oldStatuses = $response | Where-Object { 
            $_.status -notin @('PENDING', 'VALIDATED') 
        }
        
        if ($oldStatuses.Count -eq 0) {
            Write-Host "✅ PARFAIT ! Tous les statuts sont corrects (PENDING/VALIDATED uniquement)" -ForegroundColor Green
        } else {
            Write-Host "⚠️  Attention: $($oldStatuses.Count) demande(s) avec anciens statuts détectées" -ForegroundColor Yellow
        }
        
    } catch {
        Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "💡 Vérifiez que le backend est démarré (http://localhost:8080)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Pause
}

function Test-EquipmentAPI {
    Write-Host "🧪 Test API Equipment..." -ForegroundColor Yellow
    Write-Host ""
    
    try {
        $response = Invoke-RestMethod "http://localhost:8080/api/equipment" -ErrorAction Stop
        
        Write-Host "✅ API accessible" -ForegroundColor Green
        Write-Host "📊 Nombre d'équipements: $($response.Count)" -ForegroundColor Cyan
        
        $statuses = $response | Group-Object -Property status | Select-Object Name, Count
        Write-Host ""
        Write-Host "📈 Répartition par statut:" -ForegroundColor Cyan
        $statuses | Format-Table -AutoSize
        
    } catch {
        Write-Host "❌ Erreur: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "💡 Vérifiez que le backend est démarré (http://localhost:8080)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Pause
}

function Check-BackendLogs {
    Write-Host "📋 Vérification logs backend..." -ForegroundColor Yellow
    Write-Host ""
    
    $logPattern = "$env:USERPROFILE\.gradle\daemon\*\daemon-*.out.log"
    $logFiles = Get-ChildItem -Path $logPattern -ErrorAction SilentlyContinue | 
                Sort-Object LastWriteTime -Descending | 
                Select-Object -First 1
    
    if ($logFiles) {
        Write-Host "📂 Fichier log trouvé: $($logFiles.FullName)" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "🔍 Dernières lignes (50):" -ForegroundColor Yellow
        Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Gray
        
        Get-Content $logFiles.FullName -Tail 50 | ForEach-Object {
            if ($_ -match "ERROR") {
                Write-Host $_ -ForegroundColor Red
            } elseif ($_ -match "WARN") {
                Write-Host $_ -ForegroundColor Yellow
            } elseif ($_ -match "INFO") {
                Write-Host $_ -ForegroundColor Cyan
            } else {
                Write-Host $_ -ForegroundColor Gray
            }
        }
        
    } else {
        Write-Host "❌ Aucun fichier log trouvé" -ForegroundColor Red
        Write-Host "💡 Le backend n'est peut-être pas démarré" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Pause
}

function Restart-Backend {
    Write-Host "🔄 Redémarrage du backend..." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "💡 Note: Vous devrez arrêter manuellement le backend actuel (Ctrl+C)" -ForegroundColor Gray
    Write-Host ""
    
    cd "c:\Users\aalou\MAGSAV-3.0"
    
    Write-Host "Commande: .\gradlew.bat :backend:bootRun" -ForegroundColor Cyan
    Write-Host ""
    
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\Users\aalou\MAGSAV-3.0; .\gradlew.bat :backend:bootRun"
    
    Write-Host "✅ Nouvelle fenêtre PowerShell ouverte avec le backend" -ForegroundColor Green
    Write-Host ""
    Pause
}

function Start-FullStack {
    Write-Host "🚀 Démarrage Full Stack (Backend + Frontend)..." -ForegroundColor Yellow
    Write-Host ""
    
    cd "c:\Users\aalou\MAGSAV-3.0"
    
    # Backend
    Write-Host "1️⃣  Démarrage Backend..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\Users\aalou\MAGSAV-3.0; .\gradlew.bat :backend:bootRun"
    Start-Sleep -Seconds 2
    
    # Frontend
    Write-Host "2️⃣  Démarrage Frontend..." -ForegroundColor Cyan
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd c:\Users\aalou\MAGSAV-3.0\web-frontend; npm start"
    
    Write-Host ""
    Write-Host "✅ Backend et Frontend démarrés dans des fenêtres séparées" -ForegroundColor Green
    Write-Host "🌐 Backend: http://localhost:8080" -ForegroundColor Cyan
    Write-Host "🌐 Frontend: http://localhost:3000" -ForegroundColor Cyan
    Write-Host ""
    Pause
}

# ============================================
# BOUCLE PRINCIPALE
# ============================================
do {
    Show-Menu
    $choice = Read-Host "Votre choix"
    
    switch ($choice) {
        '1' { Show-AuditReport }
        '2' { Show-RecapReport }
        '3' { Show-LoggerGuide }
        '4' { Run-CriticalFixes }
        '5' { Create-Backup }
        '6' { Test-ServiceRequestsAPI }
        '7' { Test-EquipmentAPI }
        '8' { Check-BackendLogs }
        '9' { Restart-Backend }
        '10' { Start-FullStack }
        'q' { 
            Write-Host ""
            Write-Host "👋 Au revoir !" -ForegroundColor Cyan
            Write-Host ""
            return 
        }
        default {
            Write-Host "❌ Choix invalide" -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }
    
} while ($true)
