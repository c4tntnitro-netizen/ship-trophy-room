$ErrorActionPreference = 'Stop'
$projectPath = Split-Path -Parent $PSScriptRoot
$gamePath = 'C:\Program Files (x86)\Fractal Softworks\Starsector'
$testOutputPath = Join-Path $projectPath 'tmp\gan-eden-encounters'
$testClasspath = (Join-Path $projectPath 'build\classes') + ';' + $gamePath + '\starsector-core\*'
New-Item -ItemType Directory -Force -Path $testOutputPath | Out-Null
$rules = @{}
Import-Csv -LiteralPath (Join-Path $projectPath 'data\campaign\rules.csv') -Encoding UTF8 | ForEach-Object { $rules[$_.id] = $_ }
foreach ($ruleId in @('shipTrophyGanEdenHypershuntPatherFight', 'shipTrophyGanEdenHypershuntPirateFight', 'shipTrophyGanEdenHypershuntEngage')) {
    $rule = $rules[$ruleId]
    if ($rule.script -notmatch 'GanEdenQuestCMD beginHypershuntBattle' -or $rule.options) {
        throw "The fight branch must hand off directly without adding another menu: $ruleId"
    }
}
Write-Host 'PASS: Pather, pirate and crisis fight branches reach the direct battle handoff.'
& (Join-Path $gamePath 'jdk-23+7\bin\javac.exe') --release 8 -classpath $testClasspath -d $testOutputPath (Join-Path $PSScriptRoot 'GanEdenEncounterRegression.java')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $gamePath 'jdk-23+7\bin\java.exe') -classpath ($testOutputPath + ';' + $testClasspath) shiptrophy.GanEdenEncounterRegression
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
