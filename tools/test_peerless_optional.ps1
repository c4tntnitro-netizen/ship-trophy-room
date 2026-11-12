param(
    [string]$ModJar = "jars/HallOfTriumph.jar",
    [string]$RulesPath = "data/campaign/rules.csv",
    [string]$ModInfoPath = "mod_info.json"
)

$ErrorActionPreference = 'Stop'
$info = Get-Content -LiteralPath $ModInfoPath -Raw | ConvertFrom-Json
if (@($info.dependencies | Where-Object { $_.id -eq 'peerless_skills' }).Count) { throw 'Peerless Skills must not be a required dependency' }
$rows = @(Import-Csv -LiteralPath $RulesPath -Encoding UTF8)
$award = @($rows | Where-Object { $_.id -eq 'shipTrophyIsaPeerlessAward' })
if ($award.Count -ne 1) { throw 'Keep exactly one independent Peerless award rule' }
$conditions = @($award[0].conditions -split "`r?`n")
if ($conditions.Count -ne 3 -or $conditions[0] -cne 'IsaContactRulesCMD peerlessEnabled' -or
        $conditions[1] -cne 'GanEdenQuestCMD questCompleted' -or
        $conditions[2] -cne '!$global.ship_trophy_room_peerless_isa_v1' -or
        $award[0].script -cne 'PeerlessSkillsCMD award ship_trophy_room_peerless_isa_v1') {
    throw 'The award must first check availability, then quest completion and the unchanged receipt key'
}
foreach ($row in $rows) {
    if ($row.id -ne 'shipTrophyIsaPeerlessAward' -and ($row.script -match 'PeerlessSkillsCMD' -or $row.conditions -match 'PeerlessSkillsCMD')) { throw ('Unexpected optional command outside guarded reward: ' + $row.id) }
    if ($row.options -match 'ship_trophy_isa_peerless') { throw 'Isa must not offer Peerless training' }
}
foreach ($hook in @('shipTrophyIsaRemoteContact','shipTrophyIsaContactOpen','shipTrophyGanEdenEpitaphFinish')) {
    if (@($rows | Where-Object { $_.id -eq $hook -and $_.script -match 'FireAll ShipTrophyIsaPeerlessAward' }).Count -ne 1) { throw ('Missing completion/catch-up hook: ' + $hook) }
}
Write-Host 'PASS: no required Peerless dependency or training menu; the optional reward preserves its receipt and completion/catch-up hooks.'
$projectRoot = Split-Path -Parent $PSScriptRoot
$testOutput = Join-Path $projectRoot 'tmp\peerless-optional-regression'
New-Item -ItemType Directory -Force -Path $testOutput | Out-Null
$gamePath = 'C:\Program Files (x86)\Fractal Softworks\Starsector'
$classpath = Join-Path $gamePath 'starsector-core\*'
& (Join-Path $gamePath 'jdk-23+7\bin\javac.exe') --release 8 -classpath $classpath -d $testOutput (Join-Path $PSScriptRoot 'PeerlessOptionalRegression.java')
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $gamePath 'jdk-23+7\bin\java.exe') -classpath ($testOutput + [IO.Path]::PathSeparator + $classpath) shiptrophy.PeerlessOptionalRegression (Resolve-Path -LiteralPath $ModJar).Path
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
