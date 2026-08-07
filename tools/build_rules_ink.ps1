param(
    [string]$RulesPath = "data/campaign/rules.csv",
    [string]$OutputPath = "dialogue/rules.ink"
)

$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)

& (Join-Path $PSScriptRoot "validate_rules.ps1") -RulesPath $RulesPath

function To-KnotId([string]$RuleId) {
    return "rule_" + ($RuleId -replace '[^A-Za-z0-9_]', '_')
}

function Add-CommentBlock(
        [System.Collections.Generic.List[string]]$Lines,
        [string]$Label,
        [string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) { return }
    $Lines.Add("// " + $Label.TrimEnd())
    foreach ($line in ($Value -split "`r?`n", -1)) {
        $Lines.Add(("// " + $line).TrimEnd())
    }
}

function Parse-RuleOptions([string]$Options) {
    $parsed = New-Object System.Collections.Generic.List[object]
    if ([string]::IsNullOrWhiteSpace($Options)) { return $parsed.ToArray() }

    foreach ($line in ($Options -split "`r?`n")) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $fields = $line.Split(@(':'), 3, [System.StringSplitOptions]::None)
        if ($fields.Count -lt 3) { continue }
        $parsed.Add([PSCustomObject]@{
            id = $fields[1].Trim()
            label = $fields[2].Trim()
        })
    }
    return $parsed.ToArray()
}

function Escape-InkChoice([string]$Text) {
    return $Text.Replace('\', '\\').Replace('[', '\[').Replace(']', '\]')
}

$absoluteRulesPath = (Resolve-Path -LiteralPath $RulesPath).Path
$rows = @([System.IO.File]::ReadAllText($absoluteRulesPath, $utf8) |
    ConvertFrom-Csv)
if ($rows.Count -eq 0) { throw "No rules found in $RulesPath" }

$duplicateIds = @($rows | Group-Object id | Where-Object Count -gt 1)
if ($duplicateIds.Count -gt 0) {
    throw "Duplicate rules.csv ids: " +
        (($duplicateIds | ForEach-Object Name) -join ', ')
}

$optionTargets = @{}
foreach ($row in $rows) {
    if ($row.conditions -match '\$option\s*==\s*([A-Za-z0-9_]+)' -and
            -not $optionTargets.ContainsKey($Matches[1])) {
        $optionTargets[$Matches[1]] = $row
    }
}

$lines = New-Object 'System.Collections.Generic.List[string]'
$lines.Add("// Hall of Triumph - complete rules.csv dialogue export")
$lines.Add("//")
$lines.Add("// GENERATED PROOFREADING COPY. data/campaign/rules.csv remains the")
$lines.Add("// sole runtime dialogue authority. Regenerate with:")
$lines.Add("// powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tools\build_rules_ink.ps1")
$lines.Add("")
$lines.Add("-> rule_index")
$lines.Add("")
$lines.Add("=== rule_index ===")
$lines.Add("Hall of Triumph dialogue catalog")
$lines.Add("")
foreach ($row in $rows) {
    $lines.Add("+ [" + (Escape-InkChoice $row.id) + "] -> " +
        (To-KnotId $row.id))
}
$lines.Add("+ [End preview.] -> END")

foreach ($row in $rows) {
    $lines.Add("")
    $lines.Add("// ============================================================")
    $lines.Add("=== " + (To-KnotId $row.id) + " ===")
    $lines.Add("// rules.csv id: " + $row.id)
    Add-CommentBlock $lines "Trigger:" $row.trigger
    Add-CommentBlock $lines "Conditions:" $row.conditions
    Add-CommentBlock $lines "Runtime script:" $row.script
    Add-CommentBlock $lines "Notes:" $row.notes
    $lines.Add("")

    if ([string]::IsNullOrWhiteSpace($row.text)) {
        $lines.Add("// No literal text; the runtime script supplies this beat.")
    } else {
        $textLines = @($row.text.Trim() -split "`r?`n", -1)
        foreach ($textLine in $textLines) {
            $lines.Add($textLine.TrimEnd())
        }
    }
    $lines.Add("")

    $options = @(Parse-RuleOptions $row.options)
    foreach ($option in $options) {
        if ($optionTargets.ContainsKey($option.id)) {
            $destination = To-KnotId $optionTargets[$option.id].id
        } else {
            $destination = "rule_index"
            $lines.Add("// No static rules.csv destination for runtime option: " +
                $option.id)
        }
        $lines.Add("+ [" + (Escape-InkChoice $option.label) + "] -> " +
            $destination)
    }
    $lines.Add("+ [Return to index.] -> rule_index")
}

$lines.Add("")
$lines.Add("// ============================================================")
$lines.Add("// END OF RULES.CSV EXPORT")

$absoluteOutputPath = [System.IO.Path]::GetFullPath(
    (Join-Path (Get-Location) $OutputPath))
$outputDirectory = Split-Path -Parent $absoluteOutputPath
if (-not (Test-Path -LiteralPath $outputDirectory)) {
    New-Item -ItemType Directory -Path $outputDirectory | Out-Null
}
[System.IO.File]::WriteAllText(
    $absoluteOutputPath,
    ([string]::Join("`r`n", $lines) + "`r`n"),
    $utf8)
Write-Output ("Wrote {0} from {1} rules.csv rows" -f
    $absoluteOutputPath, $rows.Count)
