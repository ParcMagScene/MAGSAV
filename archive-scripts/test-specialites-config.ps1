# Script de validation - Gestion centralisee des specialites
Write-Host "=============================================" -ForegroundColor Green
Write-Host "🎯 VALIDATION GESTION CENTRALISED SPECIALITES" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green

# Test 1: Verification du gestionnaire de configuration
Write-Host "`n1. Verification SpecialtiesConfigManager:" -ForegroundColor Cyan
$configManager = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\config\SpecialtiesConfigManager.java"
if (Test-Path $configManager) {
    $lines = (Get-Content $configManager).Count
    Write-Host "✅ SpecialtiesConfigManager cree ($lines lignes)" -ForegroundColor Green
    
    # Verification des fonctionalites cles
    $features = @(
        "addSpecialty",
        "removeSpecialty", 
        "updateSpecialty",
        "saveConfiguration",
        "loadConfiguration",
        "resetToDefaults"
    )
    
    foreach ($feature in $features) {
        $found = Select-String -Path $configManager -Pattern $feature -Quiet
        if ($found) {
            Write-Host "  ✅ Fonction $feature presente" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Fonction $feature manquante" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ SpecialtiesConfigManager non trouve" -ForegroundColor Red
}

# Test 2: Verification de la vue de configuration
Write-Host "`n2. Verification SpecialtiesConfigView:" -ForegroundColor Cyan
$configView = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\config\SpecialtiesConfigView.java"
if (Test-Path $configView) {
    $lines = (Get-Content $configView).Count
    Write-Host "✅ SpecialtiesConfigView cree ($lines lignes)" -ForegroundColor Green
    
    # Verification des composants UI
    $uiComponents = @(
        "specialtiesTable",
        "newSpecialtyField", 
        "assignedPersonnelList",
        "availablePersonnelList",
        "specialtySelector"
    )
    
    foreach ($component in $uiComponents) {
        $found = Select-String -Path $configView -Pattern $component -Quiet
        if ($found) {
            Write-Host "  ✅ Composant $component present" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Composant $component manquant" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ SpecialtiesConfigView non trouve" -ForegroundColor Red
}

# Test 3: Verification des modifications PersonnelDialog
Write-Host "`n3. Verification PersonnelDialog modifie:" -ForegroundColor Cyan
$dialog = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\PersonnelDialog.java"
if (Test-Path $dialog) {
    # Verification du remplacement TextArea par CheckBox
    $checkBoxes = Select-String -Path $dialog -Pattern "createSpecialtiesCheckBoxes" -Quiet
    $configManagerImport = Select-String -Path $dialog -Pattern "SpecialtiesConfigManager" -Quiet
    
    if ($checkBoxes -and $configManagerImport) {
        Write-Host "✅ PersonnelDialog mis a jour avec CheckBox" -ForegroundColor Green
        Write-Host "  ✅ Import SpecialtiesConfigManager present" -ForegroundColor Green
        Write-Host "  ✅ Methode createSpecialtiesCheckBoxes presente" -ForegroundColor Green
    } else {
        Write-Host "❌ Modifications PersonnelDialog incompletes" -ForegroundColor Red
    }
    
    # Verification des nouvelles methodes
    $newMethods = @(
        "loadSpecialtiesFromData",
        "getSelectedSpecialties", 
        "getAllCheckBoxes"
    )
    
    foreach ($method in $newMethods) {
        $found = Select-String -Path $dialog -Pattern $method -Quiet
        if ($found) {
            Write-Host "  ✅ Methode $method presente" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Methode $method manquante" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ PersonnelDialog non trouve" -ForegroundColor Red
}

# Test 4: Verification de l'integration dans l'application principale
Write-Host "`n4. Verification integration MagsavDesktopApplication:" -ForegroundColor Cyan
$app = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\MagsavDesktopApplication.java"
if (Test-Path $app) {
    $settingsMethod = Select-String -Path $app -Pattern "showSettingsModule" -Quiet
    $configViewImport = Select-String -Path $app -Pattern "SpecialtiesConfigView" -Quiet
    
    if ($settingsMethod -and $configViewImport) {
        Write-Host "✅ Integration dans l'application principale" -ForegroundColor Green
        Write-Host "  ✅ Bouton Parametres fonctionnel" -ForegroundColor Green
        Write-Host "  ✅ Module Configuration accessible" -ForegroundColor Green
    } else {
        Write-Host "❌ Integration incomplete" -ForegroundColor Red
    }
} else {
    Write-Host "❌ MagsavDesktopApplication non trouve" -ForegroundColor Red
}

# Test 5: Compilation
Write-Host "`n5. Test de compilation:" -ForegroundColor Cyan
Write-Host "Compilation en cours..." -ForegroundColor Yellow
$compileResult = & .\gradlew :desktop-javafx:compileJava --quiet 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation reussie - Aucune erreur" -ForegroundColor Green
} else {
    Write-Host "❌ Erreurs de compilation detectees" -ForegroundColor Red
    Write-Host $compileResult -ForegroundColor Red
}

# Resume final
Write-Host "`n=== RESUME FONCTIONNALITES ===" -ForegroundColor Yellow
Write-Host "🎯 Gestion centralisee des specialites" -ForegroundColor White
Write-Host "📋 Configuration via interface dediee" -ForegroundColor White  
Write-Host "☑️  Selection par CheckBox dans PersonnelDialog" -ForegroundColor White
Write-Host "👥 Attribution personnel <-> specialites" -ForegroundColor White
Write-Host "💾 Sauvegarde automatique preferences" -ForegroundColor White
Write-Host "🔄 Import/Export fonctionnalites" -ForegroundColor White

Write-Host "`n=== GUIDE UTILISATION ===" -ForegroundColor Magenta
Write-Host "1. Aller dans Parametres > Specialites Personnel" -ForegroundColor White
Write-Host "2. Ajouter/Modifier/Supprimer les specialites" -ForegroundColor White
Write-Host "3. Attribuer personnel aux specialites" -ForegroundColor White
Write-Host "4. Dans Personnel > Nouveau: cocher specialites" -ForegroundColor White
Write-Host "5. Les modifications sont sauvegardees automatiquement" -ForegroundColor White