# Script de validation - Gestion centralisee des categories d'equipement
Write-Host "===============================================" -ForegroundColor Green
Write-Host "🗂️  VALIDATION GESTION CATEGORIES EQUIPEMENT" -ForegroundColor Green
Write-Host "===============================================" -ForegroundColor Green

# Test 1: Verification du gestionnaire de configuration des categories
Write-Host "`n1. Verification CategoriesConfigManager:" -ForegroundColor Cyan
$configManager = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\config\CategoriesConfigManager.java"
if (Test-Path $configManager) {
    $lines = (Get-Content $configManager).Count
    Write-Host "✅ CategoriesConfigManager cree ($lines lignes)" -ForegroundColor Green
    
    # Verification des fonctionalites cles
    $features = @(
        "addRootCategory",
        "addSubCategory",
        "removeCategory", 
        "updateCategory",
        "saveConfiguration",
        "loadConfiguration",
        "resetToDefaults",
        "getAllCategories"
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
    Write-Host "❌ CategoriesConfigManager non trouve" -ForegroundColor Red
}

# Test 2: Verification de la vue de configuration des categories
Write-Host "`n2. Verification CategoriesConfigView:" -ForegroundColor Cyan
$configView = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\view\config\CategoriesConfigView.java"
if (Test-Path $configView) {
    $lines = (Get-Content $configView).Count
    Write-Host "✅ CategoriesConfigView cree ($lines lignes)" -ForegroundColor Green
    
    # Verification des composants UI
    $uiComponents = @(
        "categoriesTree",
        "colorPicker", 
        "iconField",
        "assignedEquipmentList",
        "availableEquipmentList",
        "categorySelector",
        "CategoryTreeCell"
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
    Write-Host "❌ CategoriesConfigView non trouve" -ForegroundColor Red
}

# Test 3: Verification des modifications EquipmentDialog
Write-Host "`n3. Verification EquipmentDialog modifie:" -ForegroundColor Cyan
$dialog = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\dialog\EquipmentDialog.java"
if (Test-Path $dialog) {
    # Verification de l'integration du gestionnaire
    $configManagerImport = Select-String -Path $dialog -Pattern "CategoriesConfigManager" -Quiet
    $categoriesManagerField = Select-String -Path $dialog -Pattern "categoriesManager" -Quiet
    
    if ($configManagerImport -and $categoriesManagerField) {
        Write-Host "✅ EquipmentDialog mis a jour avec CategoriesConfigManager" -ForegroundColor Green
        Write-Host "  ✅ Import CategoriesConfigManager present" -ForegroundColor Green
        Write-Host "  ✅ Champ categoriesManager present" -ForegroundColor Green
    } else {
        Write-Host "❌ Modifications EquipmentDialog incompletes" -ForegroundColor Red
    }
    
    # Verification de la methode loadCategories modifiee
    $loadCategories = Select-String -Path $dialog -Pattern "getAllCategories" -Quiet
    if ($loadCategories) {
        Write-Host "  ✅ Methode loadCategories mise a jour" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Methode loadCategories non mise a jour" -ForegroundColor Red
    }
} else {
    Write-Host "❌ EquipmentDialog non trouve" -ForegroundColor Red
}

# Test 4: Verification de l'integration dans l'application principale
Write-Host "`n4. Verification integration MagsavDesktopApplication:" -ForegroundColor Cyan
$app = "desktop-javafx\src\main\java\com\magscene\magsav\desktop\MagsavDesktopApplication.java"
if (Test-Path $app) {
    $categoriesConfigImport = Select-String -Path $app -Pattern "CategoriesConfigView" -Quiet
    $categoriesTab = Select-String -Path $app -Pattern "categoriesTab" -Quiet
    
    if ($categoriesConfigImport -and $categoriesTab) {
        Write-Host "✅ Integration categories dans l'application principale" -ForegroundColor Green
        Write-Host "  ✅ Onglet Categories ajoute aux Parametres" -ForegroundColor Green
        Write-Host "  ✅ Import CategoriesConfigView present" -ForegroundColor Green
    } else {
        Write-Host "❌ Integration categories incomplete" -ForegroundColor Red
    }
} else {
    Write-Host "❌ MagsavDesktopApplication non trouve" -ForegroundColor Red
}

# Test 5: Verification coherence avec le systeme existant
Write-Host "`n5. Verification coherence systeme:" -ForegroundColor Cyan
$categoryEntity = "backend\src\main\java\com\magscene\magsav\backend\entity\Category.java"
if (Test-Path $categoryEntity) {
    Write-Host "✅ Entite Category backend trouvee" -ForegroundColor Green
    
    # Verification des champs importants
    $hierarchyFields = @("parent", "children", "color", "icon", "displayOrder")
    foreach ($field in $hierarchyFields) {
        $found = Select-String -Path $categoryEntity -Pattern $field -Quiet
        if ($found) {
            Write-Host "  ✅ Champ $field present dans entite" -ForegroundColor Green
        } else {
            Write-Host "  ❌ Champ $field manquant dans entite" -ForegroundColor Red
        }
    }
} else {
    Write-Host "❌ Entite Category backend non trouvee" -ForegroundColor Red
}

# Test 6: Compilation
Write-Host "`n6. Test de compilation:" -ForegroundColor Cyan
Write-Host "Compilation en cours..." -ForegroundColor Yellow
$compileResult = & .\gradlew :desktop-javafx:compileJava --quiet 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Compilation reussie - Aucune erreur" -ForegroundColor Green
} else {
    Write-Host "❌ Erreurs de compilation detectees" -ForegroundColor Red
    Write-Host $compileResult -ForegroundColor Red
}

# Resume final
Write-Host "`n=== RESUME FONCTIONNALITES CATEGORIES ===" -ForegroundColor Yellow
Write-Host "🗂️  Gestion centralisee des categories d'equipement" -ForegroundColor White
Write-Host "🎨 Categories avec couleurs et icones personnalisees" -ForegroundColor White  
Write-Host "🌳 Arborescence hierarchique (racines + sous-categories)" -ForegroundColor White
Write-Host "📦 Attribution equipement <-> categories" -ForegroundColor White
Write-Host "💾 Sauvegarde automatique preferences locales" -ForegroundColor White
Write-Host "🔄 Import/Export fonctionnalites" -ForegroundColor White
Write-Host "🎯 Integration avec EquipmentDialog" -ForegroundColor White

Write-Host "`n=== COMPARAISON AVEC SPECIALITES ===" -ForegroundColor Magenta
Write-Host "Specialites Personnel        Categories Equipement" -ForegroundColor White
Write-Host "- Liste simple              - Arborescence hierarchique" -ForegroundColor White
Write-Host "- CheckBox selection        - TreeView avec couleurs" -ForegroundColor White
Write-Host "- Personnel -> Specialites  - Equipement -> Categories" -ForegroundColor White
Write-Host "- TextArea -> CheckBox      - ComboBox -> TreeView" -ForegroundColor White

Write-Host "`n=== GUIDE UTILISATION ===" -ForegroundColor Cyan
Write-Host "1. Aller dans Parametres > Categories Equipement" -ForegroundColor White
Write-Host "2. Ajouter categories racines (Eclairage, Son, Video...)" -ForegroundColor White
Write-Host "3. Ajouter sous-categories (LED, Traditionnels...)" -ForegroundColor White
Write-Host "4. Personnaliser couleurs et icones" -ForegroundColor White
Write-Host "5. Attribuer equipements aux categories" -ForegroundColor White
Write-Host "6. Dans Equipement > Nouveau: selectionner categorie" -ForegroundColor White