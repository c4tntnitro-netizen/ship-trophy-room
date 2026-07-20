$ErrorActionPreference = "Stop"

$starsector = "C:\Program Files (x86)\Fractal Softworks\Starsector"
$core = Join-Path $starsector "starsector-core"
$javac = Join-Path $starsector "jdk-23+7\bin\javac.exe"
$jar = Join-Path $starsector "jdk-23+7\bin\jar.exe"

$classes = Join-Path $PSScriptRoot "build\classes"
$jarDir = Join-Path $PSScriptRoot "jars"
$jarPath = Join-Path $jarDir "HallOfTriumph.jar"

if (Test-Path -LiteralPath $classes) {
    Remove-Item -LiteralPath $classes -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $classes | Out-Null
New-Item -ItemType Directory -Force -Path $jarDir | Out-Null

$sources = Get-ChildItem -Recurse -Path (Join-Path $PSScriptRoot "src") -Filter "*.java" | ForEach-Object { $_.FullName }
$classpath = @(
    (Join-Path $core "starfarer.api.jar"),
    (Join-Path $core "json.jar"),
    (Join-Path $core "lwjgl_util.jar")
) -join [System.IO.Path]::PathSeparator
& $javac --release 8 -classpath $classpath -d $classes @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& $jar cf $jarPath -C $classes .
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Built $jarPath"
