$ErrorActionPreference = "Stop"

$packageRoot = Join-Path $PSScriptRoot "dist"
$modRoot = Join-Path $packageRoot "ShipTrophyRoom"

$resolvedPackageRoot = [System.IO.Path]::GetFullPath($packageRoot)
$resolvedModRoot = [System.IO.Path]::GetFullPath($modRoot)
$resolvedWorkspace = [System.IO.Path]::GetFullPath($PSScriptRoot)

if (-not $resolvedModRoot.StartsWith($resolvedWorkspace, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to package outside the workspace: $resolvedModRoot"
}

if (Test-Path -LiteralPath $resolvedModRoot) {
    Remove-Item -LiteralPath $resolvedModRoot -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $resolvedModRoot | Out-Null

Copy-Item -LiteralPath (Join-Path $PSScriptRoot "mod_info.json") -Destination $resolvedModRoot
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "README.md") -Destination $resolvedModRoot
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "data") -Destination $resolvedModRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "graphics") -Destination $resolvedModRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "jars") -Destination $resolvedModRoot -Recurse

Write-Host "Packaged clean install folder:"
Write-Host $resolvedModRoot
