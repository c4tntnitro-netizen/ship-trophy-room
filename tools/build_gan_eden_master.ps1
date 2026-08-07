param(
    [string]$RulesPath = "data/campaign/rules.csv",
    [string]$OutputDirectory = "dialogue"
)

$ErrorActionPreference = "Stop"
$utf8 = New-Object System.Text.UTF8Encoding($false)

& (Join-Path $PSScriptRoot "validate_rules.ps1") -RulesPath $RulesPath

function Read-Utf8([string]$Path) {
    return [System.IO.File]::ReadAllText(
        (Resolve-Path -LiteralPath $Path), $utf8)
}

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
        $Lines.Add("// " + $line)
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
            order = $fields[0].Trim()
            id = $fields[1].Trim()
            label = $fields[2].Trim()
        })
    }
    return $parsed.ToArray()
}

function Escape-InkChoice([string]$Text) {
    return $Text.Replace('[', '\[').Replace(']', '\]')
}

function Write-QuestPart(
        [string]$FileName,
        [string]$Title,
        [string]$Scope,
        [object[]]$Rows,
        [object[]]$IndexEntries,
        [hashtable]$GlobalOptionTargets,
        [string]$OutputDirectory) {
    if ($Rows.Count -eq 0) { throw "No rows selected for $FileName" }

    $rowIds = @{}
    foreach ($row in $Rows) { $rowIds[$row.id] = $true }

    $duplicates = @($Rows | Group-Object id | Where-Object Count -gt 1)
    if ($duplicates.Count -gt 0) {
        throw "Duplicate rules.csv ids in ${FileName}: " +
            (($duplicates | ForEach-Object Name) -join ', ')
    }

    $menuKnot = "volume_index"
    $lines = New-Object 'System.Collections.Generic.List[string]'
    $lines.Add("// Hall of Triumph - A Borrowed Name / Gan Eden")
    $lines.Add("// $Title")
    $lines.Add("//")
    $lines.Add("// GENERATED PROOFREADING COPY. data/campaign/rules.csv is the")
    $lines.Add("// sole dialogue authority used by this file. No other Ink file is")
    $lines.Add("// read, imported, concatenated, or referenced by the generator.")
    $lines.Add("// Runtime option labels below are emitted as real Ink choices.")
    $lines.Add("//")
    $lines.Add("// Scope: $Scope")
    $lines.Add("")
    $lines.Add("-> $menuKnot")
    $lines.Add("")
    $lines.Add("=== $menuKnot ===")
    $lines.Add($Title)
    $lines.Add("")
    foreach ($entry in $IndexEntries) {
        if ($rowIds.ContainsKey($entry.id)) {
            $lines.Add("+ [" + (Escape-InkChoice $entry.label) + "] -> " +
                (To-KnotId $entry.id))
        }
    }
    $lines.Add("+ [End preview.] -> END")

    for ($i = 0; $i -lt $Rows.Count; $i++) {
        $row = $Rows[$i]
        $lines.Add("")
        $lines.Add("// ============================================================")
        $lines.Add("=== " + (To-KnotId $row.id) + " ===")
        $lines.Add("// rules.csv id: " + $row.id)
        Add-CommentBlock $lines "Trigger:" $row.trigger
        Add-CommentBlock $lines "Conditions:" $row.conditions
        Add-CommentBlock $lines "Runtime script:" $row.script
        $lines.Add("")

        if (-not [string]::IsNullOrWhiteSpace($row.text)) {
            $lines.Add($row.text.Trim())
            $lines.Add("")
        } else {
            $lines.Add("// No literal text in rules.csv; the runtime script supplies this beat.")
            $lines.Add("")
        }

        $options = @(Parse-RuleOptions $row.options)
        if ($options.Count -gt 0) {
            foreach ($option in $options) {
                $target = $null
                if ($GlobalOptionTargets.ContainsKey($option.id)) {
                    $target = $GlobalOptionTargets[$option.id]
                }
                if ($target -ne $null -and $rowIds.ContainsKey($target.id)) {
                    $destination = To-KnotId $target.id
                } else {
                    $destination = "END"
                    if ($target -ne $null) {
                        $lines.Add("// Runtime destination outside this volume: " +
                            $target.id)
                    }
                }
                $lines.Add("+ [" + (Escape-InkChoice $option.label) + "] -> " +
                    $destination)
            }
        } elseif ($i + 1 -lt $Rows.Count) {
            $lines.Add("+ [Continue.] -> " + (To-KnotId $Rows[$i + 1].id))
        } else {
            $lines.Add("+ [Return to this volume's index.] -> $menuKnot")
        }
    }

    $lines.Add("")
    $lines.Add("// ============================================================")
    $lines.Add("// END OF RULES.CSV EXPORT")
    $lines.Add("// ============================================================")

    $outputPath = [System.IO.Path]::GetFullPath(
        (Join-Path (Join-Path (Get-Location) $OutputDirectory) $FileName))
    [System.IO.File]::WriteAllText(
        $outputPath,
        ([string]::Join("`r`n", $lines) + "`r`n"),
        $utf8)
    Write-Output ("Wrote {0} from {1} rules.csv rows" -f
        $outputPath, $Rows.Count)
}

$allRows = @((Read-Utf8 $RulesPath) | ConvertFrom-Csv)
$optionTargets = @{}
foreach ($row in $allRows) {
    if ($row.conditions -match '\$option\s*==\s*([A-Za-z0-9_]+)') {
        $optionId = $Matches[1]
        if (-not $optionTargets.ContainsKey($optionId)) {
            $optionTargets[$optionId] = $row
        }
    }
}

$logOneId = 'shipTrophyGanEdenEpitaphOne'
$logTwoId = 'shipTrophyGanEdenEpitaphTwo'
$logThreeId = 'shipTrophyGanEdenEpitaphThree'
$logFourId = 'shipTrophyGanEdenEpitaphFour'
$logFiveId = 'shipTrophyGanEdenEpitaphFive'
$archiveLogIds = @(
    $logOneId, $logTwoId, $logThreeId, $logFourId, $logFiveId)

$homecomingRows = @($allRows | Where-Object {
    $_.id -like 'shipTrophyIsaShatteredRingHomecoming*'
})
$partOne = @($homecomingRows + @($allRows | Where-Object {
    $_.id -eq $logOneId
}))

$partTwoCore = @($allRows | Where-Object {
    $_.id -like 'shipTrophyGanEden*' -and
    ($_.id -match 'Hypershunt' -or $_.id -match 'SecondHypershunt') -and
    $_.id -notin $archiveLogIds
})
$partTwo = @($partTwoCore + @($allRows | Where-Object {
    $_.id -eq $logTwoId -or $_.id -eq $logThreeId
}))

$partThreeGanEden = @($allRows | Where-Object {
    $_.id -like 'shipTrophyGanEden*' -and
    $_.id -notin @($partTwoCore | ForEach-Object id) -and
    $_.id -ne $logOneId -and
    $_.id -ne $logTwoId -and
    $_.id -ne $logThreeId
})
$partThreePostQuest = @($allRows | Where-Object {
    $_.id -eq 'shipTrophyIsaMainGanEden' -or
    $_.id -like 'shipTrophyIsaGanEden*'
})
$partThree = @($partThreeGanEden + $partThreePostQuest)

Write-QuestPart `
    'gan_eden_part_1_log_1.ink' `
    'Part I - A Name on a Suit' `
    'The Shattered Ring homecoming, Isa inheritance, and Log I.' `
    $partOne `
    @(
        [PSCustomObject]@{label='Begin at the Shattered Ring.'; id='shipTrophyIsaShatteredRingHomecoming'},
        [PSCustomObject]@{label='Read the complete first log.'; id=$logOneId}
    ) `
    $optionTargets `
    $OutputDirectory

Write-QuestPart `
    'gan_eden_part_2_logs_2_3.ink' `
    'Part II - The Coronal Hypershunts' `
    'Both hypershunt encounters and Logs II-III.' `
    $partTwo `
    @(
        [PSCustomObject]@{label='Begin the hypershunt investigation.'; id='shipTrophyGanEdenHypershuntPatherGuardEncounter'},
        [PSCustomObject]@{label='Review the pirate blockade.'; id='shipTrophyGanEdenHypershuntPirateGuard'},
        [PSCustomObject]@{label='Read the complete second log.'; id=$logTwoId},
        [PSCustomObject]@{label='Read the complete third log.'; id=$logThreeId}
    ) `
    $optionTargets `
    $OutputDirectory

Write-QuestPart `
    'gan_eden_part_3_logs_4_5.ink' `
    'Part III - Gan Eden' `
    'Power Transit, Gan Eden, Logs IV-Final, and the epilogue.' `
    $partThree `
    @(
        [PSCustomObject]@{label='Approach the Power Transit Gate.'; id='shipTrophyGanEdenExternalRing'},
        [PSCustomObject]@{label='Review the Golden Omega encounter.'; id='shipTrophyGanEdenGoldenEncounter'},
        [PSCustomObject]@{label='Read the complete fourth log.'; id=$logFourId},
        [PSCustomObject]@{label='Read the complete final log.'; id=$logFiveId},
        [PSCustomObject]@{label='Review Isa''s conversation about Isaac.'; id='shipTrophyIsaGanEdenHub'}
    ) `
    $optionTargets `
    $OutputDirectory
