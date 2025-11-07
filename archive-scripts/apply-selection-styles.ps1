# Script PowerShell pour appliquer les styles de sélection #142240 à tous les modules MAGSAV
# Recherche et remplace les patterns de setRowFactory pour ajouter le style de sélection

$workspaceRoot = "C:\Users\aalou\MAGSAV-3.0"
$javaFilesPattern = "$workspaceRoot\desktop-javafx\src\main\java\**\*.java"

# Modules déjà traités (à exclure)
$excludedFiles = @(
    "EquipmentManagerView.java",
    "ServiceRequestManagerView.java", 
    "PersonnelManagerView.java",
    "VehicleManagerView.java",
    "ClientManagerView.java",
    "RepairTrackingView.java",
    "RMAManagementView.java",
    "ContractManagerView.java"
)

# Pattern à rechercher (simplifié)
$searchPattern = 'table\.setRowFactory\(tv -> \{\s*TableRow<[^>]+> row = new TableRow<>\(\);'

# Template de remplacement avec style #142240
$replacementTemplate = @'
table.setRowFactory(tv -> {
            TableRow<$1> row = new TableRow<>();
            
            // Runnable pour mettre à jour le style
            Runnable updateStyle = () -> {
                if (row.isEmpty()) {
                    row.setStyle("");
                } else if (row.isSelected()) {
                    // Style de sélection prioritaire (#142240)
                    row.setStyle("-fx-background-color: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionColor() + "; " +
                               "-fx-text-fill: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionTextColor() + "; " +
                               "-fx-border-color: " + com.magscene.magsav.desktop.theme.ThemeManager.getInstance().getSelectionBorderColor() + "; " +
                               "-fx-border-width: 2px;");
                } else {
                    // Style par défaut
                    row.setStyle("");
                }
            };
            
            // Écouter les changements de sélection
            row.selectedProperty().addListener((obs, wasSelected, isSelected) -> updateStyle.run());
            row.emptyProperty().addListener((obs, wasEmpty, isEmpty) -> updateStyle.run());
            row.itemProperty().addListener((obs, oldItem, newItem) -> updateStyle.run());
'@

Write-Host "🎨 Application des styles de sélection #142240..." -ForegroundColor Cyan

# Trouver tous les fichiers Java avec TableView
$javaFiles = Get-ChildItem -Path $workspaceRoot -Recurse -Include "*.java" | Where-Object {
    $_.Name -notin $excludedFiles -and 
    (Get-Content $_.FullName -Raw) -match "TableView|setRowFactory"
}

Write-Host "📁 Fichiers Java trouvés avec TableView : $($javaFiles.Count)" -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    
    if ($content -match $searchPattern) {
        Write-Host "✏️ Modification de $($file.Name)..." -ForegroundColor Green
        
        # Pattern plus spécifique pour extraction du type générique
        $specificPattern = 'table\.setRowFactory\(tv -> \{\s*TableRow<([^>]+)> row = new TableRow<>\(\);'
        
        if ($content -match $specificPattern) {
            $genericType = $matches[1]
            $newReplacement = $replacementTemplate -replace '\$1', $genericType
            
            $newContent = $content -replace $specificPattern, $newReplacement
            Set-Content $file.FullName -Value $newContent -Encoding UTF8
            
            Write-Host "   ✅ Appliqué pour type: $genericType" -ForegroundColor Green
        }
    } else {
        Write-Host "⚠️ Pattern non trouvé dans $($file.Name)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🎯 Résumé de l'application des styles :" -ForegroundColor Cyan
Write-Host "   ✅ Modules déjà traités : $($excludedFiles.Count)" -ForegroundColor Green
Write-Host "   📝 Modules traités maintenant : $($javaFiles.Count)" -ForegroundColor Blue
Write-Host "   🎨 Style appliqué : #142240 (sélection)" -ForegroundColor Magenta
Write-Host "   🎨 Style appliqué : #7DD3FC (texte)" -ForegroundColor Cyan  
Write-Host "   🎨 Style appliqué : #6B71F2 (bordure)" -ForegroundColor Blue
Write-Host ""
Write-Host "✨ Application des styles terminée !" -ForegroundColor Green