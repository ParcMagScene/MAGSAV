# Helper pour creer des scripts PowerShell sans erreur d encodage
function New-MagsavScript {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Path,
        [Parameter(Mandatory=$true)]
        [string]$Content
    )
    
    # Creer avec UTF-8 BOM
    $utf8Bom = New-Object System.Text.UTF8Encoding $true
    [System.IO.File]::WriteAllText($Path, $Content, $utf8Bom)
    Write-Host "Script cree: $Path" -ForegroundColor Green
}

Export-ModuleMember -Function New-MagsavScript
