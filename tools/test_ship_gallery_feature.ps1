param(
    [string]$ModJar = "jars/HallOfTriumph.jar"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$testOutput = Join-Path $projectRoot "tmp\ship-gallery-filtered-regression"
New-Item -ItemType Directory -Force -Path $testOutput | Out-Null

$settings = Get-Content -LiteralPath (Join-Path $projectRoot 'data\config\settings.json') -Raw | ConvertFrom-Json
if ($settings.shipTrophyShipGalleryEnabled -ne $false) {
    throw 'The shipped gallery setting must default to false'
}
$luna = @(Import-Csv -LiteralPath (Join-Path $projectRoot 'data\config\LunaSettings.csv') -Encoding UTF8 |
    Where-Object { $_.fieldID -eq 'shipTrophyShipGalleryEnabled' })
if ($luna.Count -ne 1 -or $luna[0].fieldType -ne 'Boolean' -or
        $luna[0].defaultValue -ne 'false' -or $luna[0].fieldName -notmatch '(?i)unstable' -or
        $luna[0].fieldDescription -notmatch '(?i)unstable|instability') {
    throw 'LunaLib must expose one default-off Boolean gallery setting with an instability warning in its label and tooltip'
}
Write-Host 'PASS: settings.json and LunaLib default the gallery off and LunaLib displays an instability warning.'

$starsector = "C:\Program Files (x86)\Fractal Softworks\Starsector"
$core = Join-Path $starsector "starsector-core"
$classpath = @(
    (Join-Path $core "*"),
    (Join-Path $starsector "mods\LazyLib\jars\LazyLib.jar"),
    (Join-Path $starsector "mods\LazyLib\jars\internal\Kotlin-Runtime.jar")
) -join [IO.Path]::PathSeparator

& (Join-Path $starsector "jdk-23+7\bin\javac.exe") --release 8 -classpath $classpath -d $testOutput (Join-Path $PSScriptRoot "ShipGalleryFilteredRegression.java")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
# Obfuscated engine classes require the same verifier option used by the game.
& (Join-Path $starsector "jdk-23+7\bin\java.exe") -Xverify:none -classpath ($testOutput + [IO.Path]::PathSeparator + $classpath) shiptrophy.ShipGalleryFilteredRegression (Resolve-Path -LiteralPath $ModJar).Path (Join-Path $starsector "mods\LunaLib\jars\LunaLib.jar") (Join-Path $starsector "saves\common\LunaSettings\ship_trophy_room.json.data")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
