param(
    [string]$RulesPath = "data/campaign/rules.csv"
)

$ErrorActionPreference = "Stop"

if ([System.IO.Path]::IsPathRooted($RulesPath)) {
    $resolvedRules = [System.IO.Path]::GetFullPath($RulesPath)
} else {
    $resolvedRules = [System.IO.Path]::GetFullPath(
        (Join-Path (Get-Location) $RulesPath))
}
if (-not (Test-Path -LiteralPath $resolvedRules -PathType Leaf)) {
    throw "rules.csv not found: $resolvedRules"
}

$utf8 = New-Object System.Text.UTF8Encoding($false, $true)
$content = [System.IO.File]::ReadAllText($resolvedRules, $utf8)
$errors = New-Object 'System.Collections.Generic.List[string]'

# Starsector's rules loader misparses typographic double quotes in CSV fields.
# Dialogue quotation marks must be ASCII quotes escaped as "" inside a field.
$forbiddenQuotes = @(
    [PSCustomObject]@{ Character = [char]0x201C; Name = 'U+201C LEFT DOUBLE QUOTATION MARK' },
    [PSCustomObject]@{ Character = [char]0x201D; Name = 'U+201D RIGHT DOUBLE QUOTATION MARK' }
)
foreach ($forbidden in $forbiddenQuotes) {
    $offset = 0
    while (($index = $content.IndexOf($forbidden.Character, $offset)) -ge 0) {
        $line = 1 + ([regex]::Matches($content.Substring(0, $index), "`n")).Count
        $errors.Add(
            "line \${line}: forbidden $($forbidden.Name); use an escaped ASCII double quote")
        $offset = $index + 1
    }
}

Add-Type -AssemblyName Microsoft.VisualBasic
$parser = New-Object Microsoft.VisualBasic.FileIO.TextFieldParser(
    $resolvedRules,
    [System.Text.Encoding]::UTF8)
$parser.TextFieldType = [Microsoft.VisualBasic.FileIO.FieldType]::Delimited
$parser.SetDelimiters(',')
$parser.HasFieldsEnclosedInQuotes = $true
$parser.TrimWhiteSpace = $false

$expectedHeader = @('id', 'trigger', 'conditions', 'script', 'text', 'options', 'notes')
$ids = @{}
$recordNumber = 0
$bracketedStatusCount = 0
try {
    while (-not $parser.EndOfData) {
        $recordStartLine = $parser.LineNumber
        try {
            $fields = @($parser.ReadFields())
        } catch [Microsoft.VisualBasic.FileIO.MalformedLineException] {
            $errors.Add("line $($parser.ErrorLineNumber): malformed CSV: $($parser.ErrorLine)")
            break
        }

        $recordNumber++
        if ($fields.Count -ne $expectedHeader.Count) {
            $errors.Add(
                "line \${recordStartLine}: expected $($expectedHeader.Count) columns, found $($fields.Count)")
            continue
        }

        if ($recordNumber -eq 1) {
            for ($column = 0; $column -lt $expectedHeader.Count; $column++) {
                if ($fields[$column] -cne $expectedHeader[$column]) {
                    $errors.Add(
                        "line 1: column $($column + 1) must be '$($expectedHeader[$column])', found '$($fields[$column])'")
                }
            }
            continue
        }

        $id = $fields[0]
        if ([string]::IsNullOrWhiteSpace($id)) {
            $errors.Add("line \${recordStartLine}: rule id is empty")
        } elseif (-not $id.StartsWith('#')) {
            if ($ids.ContainsKey($id)) {
                $errors.Add(
                    "line \${recordStartLine}: duplicate rule id '$id' (first seen at line $($ids[$id]))")
            } else {
                $ids[$id] = $recordStartLine
            }
        }

        foreach ($optionLine in ($fields[5] -split "`r?`n")) {
            if ([string]::IsNullOrWhiteSpace($optionLine)) { continue }
            if ($optionLine -notmatch '^(?:-?\d+(?:\.\d+)?:)?[A-Za-z0-9_]+:.*$') {
                $errors.Add(
                    "line \${recordStartLine}: malformed option '$optionLine'; expected [priority:]option_id:label")
            }
        }

        foreach ($statusMatch in [regex]::Matches(
                $fields[4], '\[[^\[\]\r\n]+\]')) {
            $bracketedStatusCount++
            $status = $statusMatch.Value
            $quotedStatus = '"' + $status + '"'
            if (-not $fields[3].Contains('SetTextHighlights') -or
                    -not $fields[3].Contains($quotedStatus)) {
                $errors.Add(
                    "line \${recordStartLine}: bracketed status '$status' must be included verbatim in SetTextHighlights")
            }
        }
    }
} finally {
    $parser.Close()
}

if ($errors.Count -gt 0) {
    foreach ($errorMessage in $errors) {
        Write-Error $errorMessage -ErrorAction Continue
    }
    throw "rules.csv validation failed with $($errors.Count) error(s)"
}

Write-Host "Validated $resolvedRules ($($recordNumber - 1) rules, 7 columns, $bracketedStatusCount highlighted bracketed statuses, no unsafe smart double quotes)"
