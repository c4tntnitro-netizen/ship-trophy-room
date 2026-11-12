param(
    [string]$ModJar = "jars/HallOfTriumph.jar",
    [string]$SubtypesPath = "data/config/ship_trophy_room/subtypes.csv"
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$testOutput = Join-Path $projectRoot 'tmp\iron-shell-integration-regression'
New-Item -ItemType Directory -Force -Path $testOutput | Out-Null
$fixturePath = Join-Path $testOutput 'subtypes.json'
$rows = @(Import-Csv -LiteralPath $SubtypesPath -Encoding UTF8)
[IO.File]::WriteAllText($fixturePath, (ConvertTo-Json -InputObject $rows -Depth 5), (New-Object Text.UTF8Encoding($false)))
$gamePath = 'C:\Program Files (x86)\Fractal Softworks\Starsector'
$classpath = Join-Path $gamePath 'starsector-core\*'
& (Join-Path $gamePath 'jdk-23+7\bin\javac.exe') --release 8 -classpath $classpath -d $testOutput (Join-Path $PSScriptRoot 'IronShellIntegrationRegression.java')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $gamePath 'jdk-23+7\bin\java.exe') -classpath ($testOutput + [IO.Path]::PathSeparator + $classpath) shiptrophy.IronShellIntegrationRegression (Resolve-Path -LiteralPath $ModJar).Path $fixturePath
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
