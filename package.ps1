$ErrorActionPreference = "Stop"

& (Join-Path $PSScriptRoot "tools\validate_rules.ps1")

$packageRoot = Join-Path $PSScriptRoot "dist"
$modRoot = Join-Path $packageRoot "HallOfTriumph"

$resolvedPackageRoot = [System.IO.Path]::GetFullPath($packageRoot)
$resolvedModRoot = [System.IO.Path]::GetFullPath($modRoot)
$resolvedWorkspace = [System.IO.Path]::GetFullPath($PSScriptRoot)
$workspacePrefix = $resolvedWorkspace.TrimEnd('\', '/') + [System.IO.Path]::DirectorySeparatorChar

if (-not $resolvedModRoot.StartsWith($workspacePrefix, [System.StringComparison]::OrdinalIgnoreCase)) {
    throw "Refusing to package outside the workspace: $resolvedModRoot"
}

# Preflight declared binaries before replacing an existing package. Staging and
# experimental JARs in the source directory are not release dependencies.
$modInfo = Get-Content -LiteralPath (Join-Path $PSScriptRoot "mod_info.json") -Raw | ConvertFrom-Json
$jarCopies = @()
foreach ($relativeJar in @($modInfo.jars)) {
    if ($relativeJar -isnot [string] -or [string]::IsNullOrWhiteSpace($relativeJar) -or
            [System.IO.Path]::IsPathRooted($relativeJar) -or
            [System.IO.Path]::GetExtension($relativeJar) -ine '.jar') {
        throw "Invalid runtime JAR path in mod_info.json: $relativeJar"
    }
    $sourceJar = [System.IO.Path]::GetFullPath((Join-Path $resolvedWorkspace $relativeJar))
    $destinationJar = [System.IO.Path]::GetFullPath((Join-Path $resolvedModRoot $relativeJar))
    if (-not $sourceJar.StartsWith($workspacePrefix, [System.StringComparison]::OrdinalIgnoreCase) -or
            -not $destinationJar.StartsWith($resolvedModRoot + [System.IO.Path]::DirectorySeparatorChar,
                [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Runtime JAR path must stay inside the workspace and package: $relativeJar"
    }
    if (-not (Test-Path -LiteralPath $sourceJar -PathType Leaf)) {
        throw "Declared runtime JAR not found: $sourceJar"
    }
    $sourcePart = $sourceJar
    while ($sourcePart -ne $resolvedWorkspace) {
        if ((Get-Item -LiteralPath $sourcePart -Force).Attributes -band [System.IO.FileAttributes]::ReparsePoint) {
            throw "Runtime JAR path must not traverse a filesystem link: $relativeJar"
        }
        $sourcePart = [System.IO.Path]::GetDirectoryName($sourcePart)
    }
    $jarCopies += [PSCustomObject]@{ Source = $sourceJar; Destination = $destinationJar }
}

if (Test-Path -LiteralPath $resolvedModRoot) {
    Remove-Item -LiteralPath $resolvedModRoot -Recurse -Force
}

New-Item -ItemType Directory -Force -Path $resolvedModRoot | Out-Null

Copy-Item -LiteralPath (Join-Path $PSScriptRoot "mod_info.json") -Destination $resolvedModRoot
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "README.md") -Destination $resolvedModRoot
if (Test-Path -LiteralPath (Join-Path $PSScriptRoot "CHANGELOG.md")) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "CHANGELOG.md") -Destination $resolvedModRoot
}
if (Test-Path -LiteralPath (Join-Path $PSScriptRoot "docs")) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "docs") -Destination $resolvedModRoot -Recurse
}
if (Test-Path -LiteralPath (Join-Path $PSScriptRoot "dialogue")) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "dialogue") -Destination $resolvedModRoot -Recurse
}
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "data") -Destination $resolvedModRoot -Recurse
Copy-Item -LiteralPath (Join-Path $PSScriptRoot "graphics") -Destination $resolvedModRoot -Recurse
if (Test-Path -LiteralPath (Join-Path $PSScriptRoot "sounds")) {
    Copy-Item -LiteralPath (Join-Path $PSScriptRoot "sounds") -Destination $resolvedModRoot -Recurse
}
foreach ($jarCopy in $jarCopies) {
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $jarCopy.Destination) | Out-Null
    Copy-Item -LiteralPath $jarCopy.Source -Destination $jarCopy.Destination
}

Write-Host "Packaged clean install folder:"
Write-Host $resolvedModRoot
