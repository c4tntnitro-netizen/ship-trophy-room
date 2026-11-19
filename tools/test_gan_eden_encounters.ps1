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
foreach ($ruleId in @('shipTrophyGanEdenHypershuntPatherGuardEncounter', 'shipTrophyGanEdenHypershuntPirateGuardEncounter')) {
    if ($rules[$ruleId].trigger -ne 'BeginFleetEncounter') {
        throw "Guard dialogue must open from the vanilla fleet encounter: $ruleId"
    }
}
$hypershuntManager = Get-Content -Raw -LiteralPath (Join-Path $projectPath 'src\shiptrophy\campaign\GanEdenHypershuntManager.java')
if ($hypershuntManager -match 'addTag\(Tags\.HAS_INTERACTION_DIALOG\)') {
    throw 'Hypershunt guards must not carry HAS_INTERACTION_DIALOG; it replaces the fleet dialog with an empty rule-based one.'
}
Write-Host 'PASS: Hypershunt guards open the fleet encounter dialog that hosts their dialogue.'
& (Join-Path $gamePath 'jdk-23+7\bin\javac.exe') --release 8 -classpath $testClasspath -d $testOutputPath (Join-Path $PSScriptRoot 'GanEdenEncounterRegression.java')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $gamePath 'jdk-23+7\bin\java.exe') -classpath ($testOutputPath + ';' + $testClasspath) shiptrophy.GanEdenEncounterRegression
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
