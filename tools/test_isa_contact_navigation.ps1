param(
    [string]$RulesPath = "data/campaign/rules.csv"
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$testOutput = Join-Path $projectRoot "tmp\isa-contact-regression"
New-Item -ItemType Directory -Force -Path $testOutput | Out-Null

$rules = @{}
$galleryOptions = @()
foreach ($rule in (Import-Csv -LiteralPath $RulesPath -Encoding UTF8)) {
    $rules[$rule.id] = $rule
    foreach ($option in ($rule.options -split "`r?`n")) {
        if ($option -match '^(?:-?\d+(?:\.\d+)?:)?ship_trophy_gallery_open:') {
            $galleryOptions += $rule
        }
    }
}
if ($galleryOptions.Count -ne 1 -or $galleryOptions[0].id -ne 'shipTrophyGalleryColonyOption') {
    throw 'Ship Gallery must have exactly one colony menu option'
}
$galleryRule = $galleryOptions[0]
$galleryConditions = @($galleryRule.conditions -split "`r?`n" | ForEach-Object { $_.Trim() })
if ($galleryRule.trigger -ne 'PopulateOptions' -or
        $galleryConditions -notcontains '$hasMarket' -or
        $galleryConditions -notcontains '$menuState == main' -or
        $galleryConditions -notcontains 'ShipGalleryRulesCMD shouldShow') {
    throw 'Ship Gallery menu option must pass the runtime feature and colony eligibility gate'
}
if (-not $rules.ContainsKey('shipTrophyGalleryColonyOpen')) {
    throw 'Ship Gallery must retain its existing open handler'
}
Write-Host 'PASS: the only Ship Gallery menu option uses the runtime feature and colony eligibility gate.'
$fixture = @{
    masterworkOptions = $rules.shipTrophyIsaMasterworkIncomplete.options
    mainConditions = $rules.shipTrophyIsaContactOpen.conditions
    marketClose = $rules.shipTrophyIsaCloseConversation.script
    intelClose = $rules.shipTrophyIsaCloseIntel.script
    contactEntry = $rules.shipTrophyIsaContactOption.script
    officerContactEntry = $rules.shipTrophyIsaOfficerContactOption.script
}
$fixturePath = Join-Path $testOutput "rules-fixture.json"
[IO.File]::WriteAllText($fixturePath, ($fixture | ConvertTo-Json), (New-Object Text.UTF8Encoding($false)))

$starsector = "C:\Program Files (x86)\Fractal Softworks\Starsector"
$core = Join-Path $starsector "starsector-core"
$classpath = @(
    (Join-Path $core "starfarer.api.jar"),
    (Join-Path $core "starfarer_obf.jar"),
    (Join-Path $core "fs.common_obf.jar"),
    (Join-Path $core "json.jar"),
    (Join-Path $core "log4j-1.2.9.jar"),
    (Join-Path $core "lwjgl.jar"),
    (Join-Path $core "lwjgl_util.jar")
) -join [IO.Path]::PathSeparator

& (Join-Path $starsector "jdk-23+7\bin\javac.exe") --release 8 -classpath $classpath -d $testOutput (Join-Path $PSScriptRoot "IsaContactNavigationRegression.java")
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& (Join-Path $starsector "jdk-23+7\bin\java.exe") -classpath ($testOutput + [IO.Path]::PathSeparator + $classpath) shiptrophy.IsaContactNavigationRegression $fixturePath
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
