[CmdletBinding()]
param(
    [ValidateSet('Export', 'Import', 'Draft', 'Quota', 'Check', 'Build', 'Report', 'Index', 'Search', 'SelfTest')]
    [string]$Command = 'Report',

    [ValidateSet('zh-Hans-CN', 'zh-Hant-TW')]
    [string]$Locale = 'zh-Hans-CN',

    [string]$SourcePath = 'data/campaign/rules.csv',
    [string]$BatchPath = '',
    [string]$OutputPath = '',
    [string]$ReferenceIndexPath = '',
    [string]$Query = '',
    [string]$UnitKey = '',
    [int]$BatchSize = 12,
    [int]$Offset = 0,
    [int]$TopK = 5,
    [switch]$IncludeTranslated,
    [switch]$BackTranslate,
    [switch]$AllowIncomplete,
    [switch]$RequireComplete
)

Set-StrictMode -Version 2.0
$ErrorActionPreference = 'Stop'

$projectRoot = [System.IO.Path]::GetFullPath(
    (Join-Path $PSScriptRoot '..'))
$expectedHeader = @(
    'id', 'trigger', 'conditions', 'script', 'text', 'options', 'notes')
$userFacingScriptCommands = @(
    'SetTooltip', 'SetStoryOption', 'SetTextHighlights')
$validStatuses = @(
    'draft', 'machine-reviewed', 'human-reviewed', 'locked')
$validConfidences = @('', 'low', 'medium', 'high')
$utf8NoBom = New-Object System.Text.UTF8Encoding($false, $true)
$script:translationStoreOverride = ''

function Resolve-ProjectPath {
    param([Parameter(Mandatory = $true)][string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $projectRoot $Path))
}

function Write-Utf8NoBom {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )

    $directory = Split-Path -Parent $Path
    if (-not [string]::IsNullOrWhiteSpace($directory)) {
        [System.IO.Directory]::CreateDirectory($directory) | Out-Null
    }
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

function Write-JsonFile {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)]$Value,
        [switch]$Compress
    )

    $json = if ($Compress) {
        $Value | ConvertTo-Json -Depth 20 -Compress
    } else {
        $Value | ConvertTo-Json -Depth 20
    }
    Write-Utf8NoBom -Path $Path -Content ($json + "`n")
}

function Get-OptionalProperty {
    param(
        [Parameter(Mandatory = $true)]$Object,
        [Parameter(Mandatory = $true)][string]$Name,
        $Default = $null
    )

    if ($Object -is [System.Collections.IDictionary] -and
            $Object.Contains($Name)) {
        return $Object[$Name]
    }
    $property = $Object.PSObject.Properties[$Name]
    if ($null -eq $property) { return $Default }
    return $property.Value
}

function Get-TextHash {
    param([Parameter(Mandatory = $true)][AllowEmptyString()][string]$Text)

    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes($Text)
        $hash = $sha.ComputeHash($bytes)
        return ([System.BitConverter]::ToString($hash)).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Normalize-TextLineEndings {
    param([Parameter(Mandatory = $true)][AllowEmptyString()][string]$Text)

    return $Text.Replace("`r`n", "`n").Replace("`r", "`n")
}

function Test-UnitSourceCurrent {
    param(
        [Parameter(Mandatory = $true)]$Entry,
        [Parameter(Mandatory = $true)]$Unit
    )

    $storedSource = [string](Get-OptionalProperty $Entry 'source' '')
    $storedHash = [string](Get-OptionalProperty $Entry 'sourceHash' '')
    if ($storedHash -cne (Get-TextHash $storedSource)) { return $false }

    return (Normalize-TextLineEndings $storedSource) -ceq
        (Normalize-TextLineEndings ([string]$Unit.Source))
}

function Read-RulesFile {
    param([Parameter(Mandatory = $true)][string]$Path)

    $absolutePath = Resolve-ProjectPath $Path
    if (-not (Test-Path -LiteralPath $absolutePath -PathType Leaf)) {
        throw "rules.csv not found: $absolutePath"
    }

    Add-Type -AssemblyName Microsoft.VisualBasic
    $parser = New-Object Microsoft.VisualBasic.FileIO.TextFieldParser(
        $absolutePath,
        [System.Text.Encoding]::UTF8)
    $parser.TextFieldType = [Microsoft.VisualBasic.FileIO.FieldType]::Delimited
    $parser.SetDelimiters(',')
    $parser.HasFieldsEnclosedInQuotes = $true
    $parser.TrimWhiteSpace = $false

    $rows = New-Object 'System.Collections.Generic.List[object]'
    try {
        if ($parser.EndOfData) { throw "rules.csv is empty: $absolutePath" }
        $header = @($parser.ReadFields())
        if ($header.Count -ne $expectedHeader.Count) {
            throw "rules.csv header has $($header.Count) columns; expected $($expectedHeader.Count)"
        }
        for ($column = 0; $column -lt $expectedHeader.Count; $column++) {
            if ($header[$column] -cne $expectedHeader[$column]) {
                throw "rules.csv column $($column + 1) is '$($header[$column])'; expected '$($expectedHeader[$column])'"
            }
        }

        $index = 0
        while (-not $parser.EndOfData) {
            $startLine = $parser.LineNumber
            try {
                $fields = @($parser.ReadFields())
            } catch [Microsoft.VisualBasic.FileIO.MalformedLineException] {
                throw "Malformed rules.csv record near line $($parser.ErrorLineNumber): $($parser.ErrorLine)"
            }
            if ($fields.Count -ne $expectedHeader.Count) {
                throw "rules.csv record near line $startLine has $($fields.Count) columns; expected 7"
            }
            $rows.Add([PSCustomObject]@{
                    Index = $index
                    StartLine = $startLine
                    Fields = [string[]]$fields
                })
            $index++
        }
    } finally {
        $parser.Close()
    }

    return [PSCustomObject]@{
        Path = $absolutePath
        Header = [string[]]$header
        Rows = [object[]]$rows.ToArray()
    }
}

function ConvertTo-CsvField {
    param([AllowEmptyString()][string]$Value)

    if ($null -eq $Value) { $Value = '' }
    if ($Value.IndexOfAny([char[]]@(',', '"', "`r", "`n")) -ge 0) {
        return '"' + $Value.Replace('"', '""') + '"'
    }
    return $Value
}

function Write-RulesFile {
    param(
        [Parameter(Mandatory = $true)]$Rules,
        [Parameter(Mandatory = $true)][string]$Path
    )

    $absolutePath = Resolve-ProjectPath $Path
    $lines = New-Object 'System.Collections.Generic.List[string]'
    $lines.Add((($Rules.Header | ForEach-Object { ConvertTo-CsvField $_ }) -join ','))
    foreach ($row in $Rules.Rows) {
        if ($row.Fields.Count -ne 7) {
            throw "Cannot write rule '$($row.Fields[0])': expected 7 fields"
        }
        $lines.Add((($row.Fields | ForEach-Object { ConvertTo-CsvField $_ }) -join ','))
    }
    Write-Utf8NoBom -Path $absolutePath -Content (($lines -join "`r`n") + "`r`n")
    return $absolutePath
}

function Parse-OptionLine {
    param([Parameter(Mandatory = $true)][AllowEmptyString()][string]$Line)

    $match = [regex]::Match(
        $Line,
        '^(?:(?<priority>-?\d+(?:\.\d+)?):)?(?<id>[A-Za-z0-9_]+):(?<label>.*)$')
    if (-not $match.Success) { return $null }
    return [PSCustomObject]@{
        Priority = $match.Groups['priority'].Value
        Id = $match.Groups['id'].Value
        Label = $match.Groups['label'].Value
        LabelStart = $match.Groups['label'].Index
        Prefix = $Line.Substring(0, $match.Groups['label'].Index)
    }
}

function Get-ScriptLiteralMatches {
    param([Parameter(Mandatory = $true)][AllowEmptyString()][string]$Line)

    $trimmed = $Line.TrimStart()
    if ([string]::IsNullOrWhiteSpace($trimmed)) { return @() }
    $command = ($trimmed -split '\s+', 2)[0]
    if ($userFacingScriptCommands -notcontains $command) { return @() }

    $result = New-Object 'System.Collections.Generic.List[object]'
    $literalIndex = 0
    foreach ($match in [regex]::Matches($Line, '"[^"]*"')) {
        $literalIndex++
        $result.Add([PSCustomObject]@{
                Command = $command
                LiteralIndex = $literalIndex
                MatchIndex = $match.Index
                MatchLength = $match.Length
                ContentStart = $match.Index + 1
                ContentLength = $match.Length - 2
                Value = $match.Value.Substring(1, $match.Length - 2)
            })
    }
    return [object[]]$result.ToArray()
}

function New-TextKey {
    param([string]$RuleId)
    return "${RuleId}::text"
}

function New-OptionKey {
    param([string]$RuleId, [int]$LineIndex, [string]$OptionId)
    return ('{0}::option::{1:D3}::{2}' -f $RuleId, $LineIndex, $OptionId)
}

function New-ScriptKey {
    param(
        [string]$RuleId,
        [int]$LineIndex,
        [string]$ScriptCommand,
        [int]$LiteralIndex
    )
    return ('{0}::script::{1:D3}::{2}::{3:D2}' -f
        $RuleId, $LineIndex, $ScriptCommand, $LiteralIndex)
}

function Get-TranslationUnits {
    param([Parameter(Mandatory = $true)]$Rules)

    $units = New-Object 'System.Collections.Generic.List[object]'
    foreach ($row in $Rules.Rows) {
        $fields = $row.Fields
        $ruleId = $fields[0]
        if ([string]::IsNullOrWhiteSpace($ruleId) -or $ruleId.StartsWith('#')) {
            continue
        }

        if (-not [string]::IsNullOrWhiteSpace($fields[4])) {
            $source = $fields[4]
            $units.Add([PSCustomObject]@{
                    Key = New-TextKey $ruleId
                    SourceHash = Get-TextHash $source
                    Source = $source
                    RuleId = $ruleId
                    RuleIndex = $row.Index
                    Kind = 'text'
                    Field = 'text'
                    LineIndex = 0
                    ItemId = ''
                    Command = ''
                    Trigger = $fields[1]
                    Conditions = $fields[2]
                    DeveloperNotes = $fields[6]
                })
        }

        $optionLineIndex = 0
        foreach ($line in ($fields[5] -split "`r?`n")) {
            $optionLineIndex++
            if ([string]::IsNullOrWhiteSpace($line)) { continue }
            $parsed = Parse-OptionLine $line
            if ($null -eq $parsed) {
                throw "Malformed option in rule '$ruleId': $line"
            }
            if ([string]::IsNullOrWhiteSpace($parsed.Label)) { continue }
            $source = $parsed.Label
            $units.Add([PSCustomObject]@{
                    Key = New-OptionKey $ruleId $optionLineIndex $parsed.Id
                    SourceHash = Get-TextHash $source
                    Source = $source
                    RuleId = $ruleId
                    RuleIndex = $row.Index
                    Kind = 'option'
                    Field = 'options'
                    LineIndex = $optionLineIndex
                    ItemId = $parsed.Id
                    Command = ''
                    Trigger = $fields[1]
                    Conditions = $fields[2]
                    DeveloperNotes = $fields[6]
                })
        }

        $scriptLineIndex = 0
        foreach ($line in ($fields[3] -split "`r?`n")) {
            $scriptLineIndex++
            foreach ($literal in @(Get-ScriptLiteralMatches $line)) {
                if ([string]::IsNullOrWhiteSpace($literal.Value)) { continue }
                $source = $literal.Value
                $units.Add([PSCustomObject]@{
                        Key = New-ScriptKey $ruleId $scriptLineIndex $literal.Command $literal.LiteralIndex
                        SourceHash = Get-TextHash $source
                        Source = $source
                        RuleId = $ruleId
                        RuleIndex = $row.Index
                        Kind = "script.$($literal.Command)"
                        Field = 'script'
                        LineIndex = $scriptLineIndex
                        ItemId = [string]$literal.LiteralIndex
                        Command = $literal.Command
                        Trigger = $fields[1]
                        Conditions = $fields[2]
                        DeveloperNotes = $fields[6]
                    })
            }
        }
    }
    return [object[]]$units.ToArray()
}

function Get-VisibleOptionLabels {
    param([AllowEmptyString()][string]$Options)

    $labels = New-Object 'System.Collections.Generic.List[string]'
    foreach ($line in ($Options -split "`r?`n")) {
        if ([string]::IsNullOrWhiteSpace($line)) { continue }
        $parsed = Parse-OptionLine $line
        if ($null -ne $parsed -and -not [string]::IsNullOrWhiteSpace($parsed.Label)) {
            $labels.Add($parsed.Label)
        }
    }
    return ($labels -join ' | ')
}

function Limit-ContextText {
    param([AllowEmptyString()][string]$Text, [int]$Maximum = 1400)

    if ($null -eq $Text) { return '' }
    if ($Text.Length -le $Maximum) { return $Text }
    return $Text.Substring(0, $Maximum) + ' [context truncated]'
}

function Get-RuleContextSummary {
    param($Row)

    if ($null -eq $Row) { return $null }
    return [ordered]@{
        ruleId = $Row.Fields[0]
        text = Limit-ContextText $Row.Fields[4]
        optionLabels = Limit-ContextText (Get-VisibleOptionLabels $Row.Fields[5])
    }
}

function Test-RuleHasPlayerText {
    param($Row)
    return (-not [string]::IsNullOrWhiteSpace($Row.Fields[4]) -or
        -not [string]::IsNullOrWhiteSpace($Row.Fields[5]))
}

function Get-UnitContext {
    param(
        [Parameter(Mandatory = $true)]$Unit,
        [Parameter(Mandatory = $true)]$Rules
    )

    $previous = $null
    for ($index = $Unit.RuleIndex - 1; $index -ge 0; $index--) {
        if (Test-RuleHasPlayerText $Rules.Rows[$index]) {
            $previous = $Rules.Rows[$index]
            break
        }
    }
    $next = $null
    for ($index = $Unit.RuleIndex + 1; $index -lt $Rules.Rows.Count; $index++) {
        if (Test-RuleHasPlayerText $Rules.Rows[$index]) {
            $next = $Rules.Rows[$index]
            break
        }
    }
    $current = $Rules.Rows[$Unit.RuleIndex]

    return [ordered]@{
        trigger = $Unit.Trigger
        conditions = $Unit.Conditions
        developerNotes = $Unit.DeveloperNotes
        currentRuleText = $current.Fields[4]
        currentRuleOptions = Get-VisibleOptionLabels $current.Fields[5]
        previousRule = Get-RuleContextSummary $previous
        nextRule = Get-RuleContextSummary $next
    }
}

function Get-ProtectedTokens {
    param([AllowEmptyString()][string]$Text)

    $tokens = New-Object 'System.Collections.Generic.List[string]'
    foreach ($match in [regex]::Matches(
            $Text,
            '\$[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)*|%[-+0-9.]*[A-Za-z]|\\[nrt]')) {
        $tokens.Add($match.Value)
    }
    return [string[]]@($tokens | Sort-Object -Unique)
}

function Get-StrictTokenSequence {
    param([AllowEmptyString()][string]$Text)

    $tokens = New-Object 'System.Collections.Generic.List[string]'
    foreach ($match in [regex]::Matches(
            $Text,
            '\$[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)*|%[-+0-9.]*[A-Za-z]|\\[nrt]')) {
        $tokens.Add($match.Value)
    }
    return [string[]]$tokens.ToArray()
}

function Get-NumericTokenSequence {
    param([AllowEmptyString()][string]$Text)

    $tokens = New-Object 'System.Collections.Generic.List[string]'
    foreach ($match in [regex]::Matches(
            $Text,
            '(?<![\p{L}\p{N}_])\d[\d,.]*(?:%|f)?(?![\p{L}\p{N}_])')) {
        $tokens.Add($match.Value)
    }
    return [string[]]$tokens.ToArray()
}

function Test-TokenSequencesEqual {
    param([string[]]$Left, [string[]]$Right)
    return (($Left -join [char]0x1f) -ceq ($Right -join [char]0x1f))
}

function Get-StatusFragments {
    param([AllowEmptyString()][string]$Text)

    $values = New-Object 'System.Collections.Generic.List[string]'
    foreach ($match in [regex]::Matches($Text, '\[[^\[\]\r\n]+\]')) {
        $values.Add($match.Value)
    }
    return [string[]]$values.ToArray()
}

function Read-Glossary {
    $path = Resolve-ProjectPath 'localization/rules/glossary.csv'
    return @(Import-Csv -LiteralPath $path -Encoding UTF8)
}

function Read-ScriptVariants {
    $path = Resolve-ProjectPath 'localization/rules/script_variants.csv'
    return @(Import-Csv -LiteralPath $path -Encoding UTF8)
}

function Get-GlossaryTarget {
    param($Row, [string]$TargetLocale)

    $property = $Row.PSObject.Properties[$TargetLocale]
    if ($null -eq $property) { return '' }
    return [string]$property.Value
}

function Get-GlossaryHints {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)]$Glossary,
        [Parameter(Mandatory = $true)][string]$TargetLocale
    )

    $hints = New-Object 'System.Collections.Generic.List[object]'
    foreach ($row in $Glossary) {
        if ([string]::IsNullOrWhiteSpace($row.source)) { continue }
        if (-not (Test-GlossaryTermPresent $Text ([string]$row.source))) {
            continue
        }
        $hints.Add([ordered]@{
                source = $row.source
                target = Get-GlossaryTarget $row $TargetLocale
                status = $row.status
                domain = $row.domain
                notes = $row.notes
            })
    }
    return [object[]]$hints.ToArray()
}

function Get-StorePath {
    param([string]$TargetLocale)
    if (-not [string]::IsNullOrWhiteSpace($script:translationStoreOverride)) {
        return Resolve-ProjectPath $script:translationStoreOverride
    }
    return Resolve-ProjectPath "localization/rules/translations/$TargetLocale.json"
}

function Read-TranslationStore {
    param([string]$TargetLocale)

    $path = Get-StorePath $TargetLocale
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Translation store not found: $path"
    }
    $store = Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([int](Get-OptionalProperty $store 'schemaVersion' 0) -ne 1) {
        throw "Unsupported translation store schema in $path"
    }
    if ([string](Get-OptionalProperty $store 'locale' '') -cne $TargetLocale) {
        throw "Translation store locale does not match $TargetLocale in $path"
    }
    return $store
}

function Get-EntryTable {
    param($Store)

    $table = @{}
    foreach ($entry in @((Get-OptionalProperty $Store 'entries' @()))) {
        $key = [string](Get-OptionalProperty $entry 'key' '')
        if ([string]::IsNullOrWhiteSpace($key)) {
            throw 'Translation store contains an entry without a key'
        }
        if ($table.ContainsKey($key)) {
            throw "Translation store contains duplicate key '$key'"
        }
        $table[$key] = $entry
    }
    return $table
}

function New-NormalizedStoredEntry {
    param(
        [Parameter(Mandatory = $true)]$Unit,
        [Parameter(Mandatory = $true)]$Candidate
    )

    $target = [string](Get-OptionalProperty $Candidate 'target' '')
    $status = [string](Get-OptionalProperty $Candidate 'status' '')
    if ([string]::IsNullOrWhiteSpace($status) -and
            -not [string]::IsNullOrWhiteSpace($target)) {
        $status = 'draft'
    }

    return [ordered]@{
        key = $Unit.Key
        sourceHash = $Unit.SourceHash
        source = $Unit.Source
        target = $target
        backTranslation = [string](Get-OptionalProperty $Candidate 'backTranslation' '')
        status = $status
        confidence = [string](Get-OptionalProperty $Candidate 'confidence' '')
        issues = [object[]]@((Get-OptionalProperty $Candidate 'issues' @()))
        reviewNotes = [string](Get-OptionalProperty $Candidate 'reviewNotes' '')
        traditionalArtFragments = [string[]]@(
            (Get-OptionalProperty $Candidate 'traditionalArtFragments' @()))
        artNote = [string](Get-OptionalProperty $Candidate 'artNote' '')
        allowedLatin = [string[]]@((Get-OptionalProperty $Candidate 'allowedLatin' @()))
        allowVariant = [bool](Get-OptionalProperty $Candidate 'allowVariant' $false)
    }
}

function Write-TranslationStore {
    param(
        [string]$TargetLocale,
        [hashtable]$EntryTable,
        [object[]]$Units
    )

    $entries = New-Object 'System.Collections.Generic.List[object]'
    foreach ($unit in $Units) {
        if (-not $EntryTable.ContainsKey($unit.Key)) { continue }
        $candidate = $EntryTable[$unit.Key]
        $normalized = New-NormalizedStoredEntry $unit $candidate
        if ([string]::IsNullOrWhiteSpace($normalized.target)) { continue }
        $entries.Add($normalized)
    }

    $store = [ordered]@{
        schemaVersion = 1
        locale = $TargetLocale
        sourceLocale = 'en'
        entries = [object[]]$entries.ToArray()
    }
    Write-JsonFile -Path (Get-StorePath $TargetLocale) -Value $store
}

function Get-FileSha256 {
    param([string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

$script:referenceStopwords = @{}
foreach ($stopword in @(
        'a', 'an', 'and', 'are', 'as', 'at', 'be', 'been', 'being', 'but',
        'by', 'can', 'could', 'did', 'do', 'does', 'for', 'from', 'had',
        'has', 'have', 'he', 'her', 'hers', 'him', 'his', 'i', 'if', 'in',
        'into', 'is', 'it', 'its', 'me', 'my', 'of', 'on', 'or', 'our',
        'ours', 'she', 'so', 'than', 'that', 'the', 'their', 'them', 'then',
        'there', 'they', 'this', 'those', 'to', 'was', 'we', 'were', 'what',
        'when', 'where', 'which', 'who', 'will', 'with', 'would', 'you',
        'your')) {
    $script:referenceStopwords[$stopword] = $true
}

function Get-ReferenceIndexFilePath {
    param([string]$RequestedPath)

    if ([string]::IsNullOrWhiteSpace($RequestedPath)) {
        $RequestedPath = 'build/localization/reference-index.json'
    }
    return Resolve-ProjectPath $RequestedPath
}

function Get-ReferenceFingerprint {
    param([string]$RulesPath)

    $parts = New-Object 'System.Collections.Generic.List[string]'
    $parts.Add('sparse-tfidf-word-bigram-v3')
    $parts.Add((Get-FileSha256 (Resolve-ProjectPath $RulesPath)))
    foreach ($targetLocale in @('zh-Hans-CN', 'zh-Hant-TW')) {
        $storePath = Get-StorePath $targetLocale
        $parts.Add((Get-FileSha256 $storePath))
    }
    return Get-TextHash ($parts -join '|')
}

function Get-SearchFeatureTokens {
    param([AllowEmptyString()][string]$Text)

    if ([string]::IsNullOrWhiteSpace($Text)) { return @() }
    $expanded = [regex]::Replace($Text, '([a-z0-9])([A-Z])', '$1 $2')
    $expanded = $expanded.Replace('_', ' ').Replace('-', ' ')
    $words = New-Object 'System.Collections.Generic.List[string]'
    foreach ($match in [regex]::Matches(
            $expanded.ToLowerInvariant(),
            '[a-z][a-z0-9]{1,}')) {
        $word = $match.Value
        if ($script:referenceStopwords.ContainsKey($word)) { continue }
        $words.Add($word)
    }

    $features = New-Object 'System.Collections.Generic.List[string]'
    foreach ($word in $words) {
        $features.Add("w:$word")
        if ($word.Length -ge 7) {
            $features.Add("p:$($word.Substring(0, 6))")
        }
        $stem = $word
        if ($word.Length -ge 7 -and $word.EndsWith('ing')) {
            $stem = $word.Substring(0, $word.Length - 3)
        } elseif ($word.Length -ge 6 -and $word.EndsWith('ed')) {
            $stem = $word.Substring(0, $word.Length - 2)
        } elseif ($word.Length -ge 6 -and $word.EndsWith('es')) {
            $stem = $word.Substring(0, $word.Length - 2)
        } elseif ($word.Length -ge 5 -and $word.EndsWith('s') -and
                -not $word.EndsWith('ss')) {
            $stem = $word.Substring(0, $word.Length - 1)
        }
        if ($stem -cne $word -and $stem.Length -ge 3) {
            $features.Add("s:$stem")
        }
    }
    for ($index = 1; $index -lt $words.Count; $index++) {
        $features.Add("b:$($words[$index - 1])_$($words[$index])")
    }
    return [string[]]$features.ToArray()
}

function Get-FeatureCounts {
    param([AllowEmptyString()][string]$Text)

    $counts = @{}
    foreach ($term in @(Get-SearchFeatureTokens $Text)) {
        if ($counts.ContainsKey($term)) {
            $counts[$term] = [int]$counts[$term] + 1
        } else {
            $counts[$term] = 1
        }
    }
    return $counts
}

function Get-UnitRetrievalText {
    param($Unit, $Rules)

    $row = $Rules.Rows[$Unit.RuleIndex]
    return @(
        $Unit.Source,
        $Unit.Source,
        $row.Fields[4],
        (Get-VisibleOptionLabels $row.Fields[5]),
        $row.Fields[6],
        $Unit.RuleId,
        $Unit.Kind) -join "`n"
}

function Get-CurrentTranslationReference {
    param(
        [hashtable]$EntryTable,
        $Unit
    )

    if (-not $EntryTable.ContainsKey($Unit.Key)) { return $null }
    $entry = $EntryTable[$Unit.Key]
    if (-not (Test-UnitSourceCurrent $entry $Unit)) {
        return $null
    }
    $target = [string](Get-OptionalProperty $entry 'target' '')
    if ([string]::IsNullOrWhiteSpace($target)) { return $null }
    return [ordered]@{
        target = $target
        backTranslation = [string](Get-OptionalProperty $entry 'backTranslation' '')
        status = [string](Get-OptionalProperty $entry 'status' '')
        confidence = [string](Get-OptionalProperty $entry 'confidence' '')
        reviewNotes = [string](Get-OptionalProperty $entry 'reviewNotes' '')
    }
}

function New-ReferenceIndex {
    param(
        [string]$RulesPath,
        [string]$RequestedIndexPath
    )

    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    $translationTables = @{}
    foreach ($targetLocale in @('zh-Hans-CN', 'zh-Hant-TW')) {
        $translationTables[$targetLocale] = Get-EntryTable (
            Read-TranslationStore $targetLocale)
    }

    $termRulePresence = @{}
    $indexedRuleIds = @{}
    $workingDocuments = New-Object 'System.Collections.Generic.List[object]'
    foreach ($unit in $units) {
        $indexedRuleIds[$unit.RuleId] = $true
        $retrievalText = Get-UnitRetrievalText $unit $rules
        $counts = Get-FeatureCounts $retrievalText
        if ($counts.Count -eq 0) {
            $counts["kind:$($unit.Kind)"] = 1
        }
        foreach ($term in $counts.Keys) {
            if (-not $termRulePresence.ContainsKey($term)) {
                $termRulePresence[$term] = @{}
            }
            $termRulePresence[$term][$unit.RuleId] = $true
        }
        $workingDocuments.Add([PSCustomObject]@{
                Unit = $unit
                Counts = $counts
            })
    }

    $idf = @{}
    $ruleCount = [double]$indexedRuleIds.Count
    foreach ($term in $termRulePresence.Keys) {
        # Same-rule fields already travel together as batch context. A feature
        # must occur in at least two different rules to help retrieve a new
        # cross-rule reference.
        $termRuleCount = [int]$termRulePresence[$term].Count
        if ($termRuleCount -lt 2) { continue }
        $idf[$term] = [math]::Log(
            ($ruleCount + 1.0) /
            ([double]$termRuleCount + 1.0)) + 1.0
    }

    $documents = New-Object 'System.Collections.Generic.List[object]'
    foreach ($working in $workingDocuments) {
        $unit = $working.Unit
        $terms = @($working.Counts.Keys |
            Where-Object { $idf.ContainsKey($_) } |
            Sort-Object)
        $rawWeights = New-Object 'System.Collections.Generic.List[double]'
        $sumSquares = 0.0
        foreach ($term in $terms) {
            $weight = (1.0 + [math]::Log([double]$working.Counts[$term])) *
                [double]$idf[$term]
            $rawWeights.Add($weight)
            $sumSquares += $weight * $weight
        }
        $norm = [math]::Sqrt($sumSquares)
        $weights = New-Object 'System.Collections.Generic.List[double]'
        foreach ($weight in $rawWeights) {
            $normalized = if ($norm -gt 0.0) { $weight / $norm } else { 0.0 }
            $weights.Add([math]::Round($normalized, 6))
        }

        $documents.Add([ordered]@{
                key = $unit.Key
                ruleId = $unit.RuleId
                kind = $unit.Kind
                source = $unit.Source
                sourceHash = $unit.SourceHash
                terms = [string[]]$terms
                weights = [double[]]$weights.ToArray()
                translations = [ordered]@{
                    'zh-Hans-CN' = Get-CurrentTranslationReference `
                        $translationTables['zh-Hans-CN'] $unit
                    'zh-Hant-TW' = Get-CurrentTranslationReference `
                        $translationTables['zh-Hant-TW'] $unit
                }
            })
    }

    $idfRows = New-Object 'System.Collections.Generic.List[object]'
    foreach ($term in @($idf.Keys | Sort-Object)) {
        $idfRows.Add([ordered]@{
                term = $term
                weight = [math]::Round([double]$idf[$term], 6)
            })
    }

    $index = [ordered]@{
        schemaVersion = 1
        algorithm = 'sparse-tfidf-word-bigram-v3'
        fingerprint = Get-ReferenceFingerprint $rules.Path
        sourcePath = 'data/campaign/rules.csv'
        documentCount = $documents.Count
        idf = [object[]]$idfRows.ToArray()
        documents = [object[]]$documents.ToArray()
    }
    $absoluteIndexPath = Get-ReferenceIndexFilePath $RequestedIndexPath
    Write-JsonFile -Path $absoluteIndexPath -Value $index -Compress
    Write-Host (
        "Built reference vector index: $($documents.Count) documents, " +
        "$($idfRows.Count) features -> $absoluteIndexPath")
    return $index
}

function Get-ReferenceIndex {
    param(
        [string]$RulesPath,
        [string]$RequestedIndexPath
    )

    $absoluteIndexPath = Get-ReferenceIndexFilePath $RequestedIndexPath
    $expectedFingerprint = Get-ReferenceFingerprint $RulesPath
    if (Test-Path -LiteralPath $absoluteIndexPath -PathType Leaf) {
        try {
            $index = Get-Content -LiteralPath $absoluteIndexPath -Raw -Encoding UTF8 |
                ConvertFrom-Json
            if ([int](Get-OptionalProperty $index 'schemaVersion' 0) -eq 1 -and
                    [string](Get-OptionalProperty $index 'algorithm' '') -ceq
                    'sparse-tfidf-word-bigram-v3' -and
                    [string](Get-OptionalProperty $index 'fingerprint' '') -ceq
                    $expectedFingerprint) {
                return $index
            }
            Write-Host 'Reference index is stale; rebuilding it.'
        } catch {
            Write-Warning "Reference index could not be read; rebuilding it: $($_.Exception.Message)"
        }
    }
    return New-ReferenceIndex $RulesPath $RequestedIndexPath
}

function Get-IndexIdfTable {
    param($Index)

    $table = @{}
    foreach ($row in @((Get-OptionalProperty $Index 'idf' @()))) {
        $table[[string](Get-OptionalProperty $row 'term' '')] =
            [double](Get-OptionalProperty $row 'weight' 0.0)
    }
    return $table
}

function New-QueryVector {
    param(
        [AllowEmptyString()][string]$Text,
        $Index
    )

    $idf = Get-IndexIdfTable $Index
    $counts = Get-FeatureCounts $Text
    $raw = @{}
    $sumSquares = 0.0
    foreach ($term in $counts.Keys) {
        if (-not $idf.ContainsKey($term)) { continue }
        $weight = (1.0 + [math]::Log([double]$counts[$term])) *
            [double]$idf[$term]
        $raw[$term] = $weight
        $sumSquares += $weight * $weight
    }
    $norm = [math]::Sqrt($sumSquares)
    $vector = @{}
    if ($norm -gt 0.0) {
        foreach ($term in $raw.Keys) {
            $vector[$term] = [double]$raw[$term] / $norm
        }
    }
    return $vector
}

function Get-DocumentVector {
    param($Document)

    $terms = @((Get-OptionalProperty $Document 'terms' @()))
    $weights = @((Get-OptionalProperty $Document 'weights' @()))
    if ($terms.Count -ne $weights.Count) {
        throw "Reference document '$([string](Get-OptionalProperty $Document 'key' ''))' has a malformed vector"
    }
    $vector = @{}
    for ($index = 0; $index -lt $terms.Count; $index++) {
        $vector[[string]$terms[$index]] = [double]$weights[$index]
    }
    return $vector
}

function Get-DocumentLocaleTranslation {
    param($Document, [string]$TargetLocale)

    $translations = Get-OptionalProperty $Document 'translations' $null
    if ($null -eq $translations) { return $null }
    return Get-OptionalProperty $translations $TargetLocale $null
}

function Find-ReferenceMatches {
    param(
        $Index,
        [hashtable]$QueryVector,
        [string]$TargetLocale,
        [int]$Limit,
        [string]$ExcludeKey = '',
        [string]$ExcludeRuleId = ''
    )

    if ($Limit -lt 1) { throw 'TopK must be at least 1' }
    if ($QueryVector.Count -eq 0) { return @() }
    $results = New-Object 'System.Collections.Generic.List[object]'
    foreach ($document in @((Get-OptionalProperty $Index 'documents' @()))) {
        $key = [string](Get-OptionalProperty $document 'key' '')
        $ruleId = [string](Get-OptionalProperty $document 'ruleId' '')
        if (-not [string]::IsNullOrWhiteSpace($ExcludeKey) -and
                $key -ceq $ExcludeKey) { continue }
        if (-not [string]::IsNullOrWhiteSpace($ExcludeRuleId) -and
                $ruleId -ceq $ExcludeRuleId) { continue }

        $terms = @((Get-OptionalProperty $document 'terms' @()))
        $weights = @((Get-OptionalProperty $document 'weights' @()))
        $score = 0.0
        for ($index = 0; $index -lt $terms.Count; $index++) {
            $term = [string]$terms[$index]
            if ($QueryVector.ContainsKey($term)) {
                $score += [double]$QueryVector[$term] * [double]$weights[$index]
            }
        }
        if ($score -lt 0.015) { continue }

        $translation = Get-DocumentLocaleTranslation $document $TargetLocale
        $status = if ($null -eq $translation) { '' } else {
            [string](Get-OptionalProperty $translation 'status' '')
        }
        $authorityBoost = switch ($status) {
            'locked' { 0.08; break }
            'human-reviewed' { 0.06; break }
            'machine-reviewed' { 0.03; break }
            default { 0.0 }
        }
        $results.Add([PSCustomObject]@{
                Score = $score
                RankingScore = $score + $authorityBoost
                Document = $document
                Translation = $translation
            })
    }

    return [object[]]@($results |
        Sort-Object -Property `
            @{ Expression = { $_.RankingScore }; Descending = $true },
            @{ Expression = { $_.Score }; Descending = $true },
            @{ Expression = { [string](Get-OptionalProperty $_.Document 'key' '') }; Descending = $false } |
        Select-Object -First $Limit)
}

function Get-RelatedReferences {
    param(
        $Index,
        $Unit,
        [string]$TargetLocale,
        [int]$Limit = 4
    )

    $queryDocument = @((Get-OptionalProperty $Index 'documents' @()) |
        Where-Object {
            [string](Get-OptionalProperty $_ 'key' '') -ceq $Unit.Key
        } | Select-Object -First 1)
    if ($queryDocument.Count -eq 0) { return @() }
    $queryVector = Get-DocumentVector $queryDocument[0]
    $matches = @(Find-ReferenceMatches `
        -Index $Index `
        -QueryVector $queryVector `
        -TargetLocale $TargetLocale `
        -Limit $Limit `
        -ExcludeKey $Unit.Key `
        -ExcludeRuleId $Unit.RuleId)

    $references = New-Object 'System.Collections.Generic.List[object]'
    foreach ($match in $matches) {
        $translation = $match.Translation
        $references.Add([ordered]@{
                similarity = [math]::Round([double]$match.Score, 4)
                key = [string](Get-OptionalProperty $match.Document 'key' '')
                ruleId = [string](Get-OptionalProperty $match.Document 'ruleId' '')
                kind = [string](Get-OptionalProperty $match.Document 'kind' '')
                source = [string](Get-OptionalProperty $match.Document 'source' '')
                target = if ($null -eq $translation) { '' } else {
                    [string](Get-OptionalProperty $translation 'target' '')
                }
                reviewStatus = if ($null -eq $translation) { 'untranslated' } else {
                    [string](Get-OptionalProperty $translation 'status' '')
                }
                backTranslation = if ($null -eq $translation) { '' } else {
                    [string](Get-OptionalProperty $translation 'backTranslation' '')
                }
            })
    }
    return [object[]]$references.ToArray()
}

function Show-ReferenceSearch {
    param(
        [string]$RulesPath,
        [string]$RequestedIndexPath,
        [string]$TargetLocale,
        [string]$SearchQuery,
        [string]$SearchUnitKey,
        [int]$Limit
    )

    $index = Get-ReferenceIndex $RulesPath $RequestedIndexPath
    $excludeKey = ''
    $excludeRuleId = ''
    if (-not [string]::IsNullOrWhiteSpace($SearchUnitKey)) {
        $document = @((Get-OptionalProperty $index 'documents' @()) |
            Where-Object {
                [string](Get-OptionalProperty $_ 'key' '') -ceq $SearchUnitKey
            } | Select-Object -First 1)
        if ($document.Count -eq 0) {
            throw "Unknown translation unit key '$SearchUnitKey'"
        }
        $queryVector = Get-DocumentVector $document[0]
        $excludeKey = $SearchUnitKey
        $excludeRuleId = [string](Get-OptionalProperty $document[0] 'ruleId' '')
        Write-Host "Related references for $SearchUnitKey"
    } else {
        if ([string]::IsNullOrWhiteSpace($SearchQuery)) {
            throw 'Search requires either -Query or -UnitKey'
        }
        $queryVector = New-QueryVector $SearchQuery $index
        Write-Host "Reference search: $SearchQuery"
    }

    $matches = @(Find-ReferenceMatches `
        -Index $index `
        -QueryVector $queryVector `
        -TargetLocale $TargetLocale `
        -Limit $Limit `
        -ExcludeKey $excludeKey `
        -ExcludeRuleId $excludeRuleId)
    if ($matches.Count -eq 0) {
        Write-Host '  No related references found.'
        return
    }
    $rank = 0
    foreach ($match in $matches) {
        $rank++
        $document = $match.Document
        Write-Host (
            "  $rank. [$([math]::Round([double]$match.Score, 4))] " +
            "$([string](Get-OptionalProperty $document 'key' ''))")
        Write-Host "     EN: $([string](Get-OptionalProperty $document 'source' ''))"
        if ($null -ne $match.Translation) {
            Write-Host (
                "     $TargetLocale [$([string](Get-OptionalProperty $match.Translation 'status' ''))]: " +
                "$([string](Get-OptionalProperty $match.Translation 'target' ''))")
        }
    }
}

function Export-TranslationBatch {
    param(
        [string]$TargetLocale,
        [string]$RulesPath,
        [string]$RequestedBatchPath,
        [string]$RequestedReferenceIndexPath,
        [int]$RequestedBatchSize,
        [int]$RequestedOffset,
        [bool]$ExportTranslated
    )

    if ($RequestedBatchSize -lt 1) { throw 'BatchSize must be at least 1' }
    if ($RequestedOffset -lt 0) { throw 'Offset cannot be negative' }

    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    $store = Read-TranslationStore $TargetLocale
    $table = Get-EntryTable $store
    $glossary = Read-Glossary
    $referenceIndex = Get-ReferenceIndex $RulesPath $RequestedReferenceIndexPath

    $eligible = New-Object 'System.Collections.Generic.List[object]'
    foreach ($unit in $units) {
        $hasCurrentTarget = $false
        if ($table.ContainsKey($unit.Key)) {
            $entry = $table[$unit.Key]
            $hasCurrentTarget = (
                (Test-UnitSourceCurrent $entry $unit) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string](Get-OptionalProperty $entry 'target' '')))
        }
        if ($ExportTranslated -or -not $hasCurrentTarget) {
            $eligible.Add($unit)
        }
    }

    $selected = @($eligible | Select-Object -Skip $RequestedOffset -First $RequestedBatchSize)
    if ($selected.Count -eq 0) {
        throw 'No matching translation units remain for this batch request'
    }

    $batchEntries = New-Object 'System.Collections.Generic.List[object]'
    foreach ($unit in $selected) {
        $existing = $null
        if ($table.ContainsKey($unit.Key)) { $existing = $table[$unit.Key] }
        $target = ''
        $backTranslation = ''
        $status = ''
        $confidence = ''
        $issues = @()
        $reviewNotes = ''
        $traditionalArtFragments = @()
        $artNote = ''
        $allowedLatin = @()
        $allowVariant = $false
        if ($null -ne $existing -and
                (Test-UnitSourceCurrent $existing $unit)) {
            $target = [string](Get-OptionalProperty $existing 'target' '')
            $backTranslation = [string](Get-OptionalProperty $existing 'backTranslation' '')
            $status = [string](Get-OptionalProperty $existing 'status' '')
            $confidence = [string](Get-OptionalProperty $existing 'confidence' '')
            $issues = @((Get-OptionalProperty $existing 'issues' @()))
            $reviewNotes = [string](Get-OptionalProperty $existing 'reviewNotes' '')
            $traditionalArtFragments = @(
                (Get-OptionalProperty $existing 'traditionalArtFragments' @()))
            $artNote = [string](Get-OptionalProperty $existing 'artNote' '')
            $allowedLatin = @((Get-OptionalProperty $existing 'allowedLatin' @()))
            $allowVariant = [bool](Get-OptionalProperty $existing 'allowVariant' $false)
        }

        $batchEntries.Add([ordered]@{
                key = $unit.Key
                sourceHash = $unit.SourceHash
                ruleId = $unit.RuleId
                kind = $unit.Kind
                source = $unit.Source
                target = $target
                backTranslation = $backTranslation
                status = $status
                confidence = $confidence
                issues = [object[]]$issues
                reviewNotes = $reviewNotes
                traditionalArtFragments = [object[]]$traditionalArtFragments
                artNote = $artNote
                allowedLatin = [object[]]$allowedLatin
                allowVariant = $allowVariant
                protectedTokens = [object[]]@(Get-ProtectedTokens $unit.Source)
                bracketedStatusFragments = [object[]]@(Get-StatusFragments $unit.Source)
                structure = [ordered]@{
                    newlineCount = ([regex]::Matches($unit.Source, "`r?`n")).Count
                    asciiDoubleQuoteCount = ([regex]::Matches($unit.Source, '"')).Count
                }
                glossaryHints = [object[]]@(
                    Get-GlossaryHints $unit.Source $glossary $TargetLocale)
                relatedReferences = [object[]]@(
                    Get-RelatedReferences $referenceIndex $unit $TargetLocale 4)
                context = Get-UnitContext $unit $rules
            })
    }

    if ([string]::IsNullOrWhiteSpace($RequestedBatchPath)) {
        $RequestedBatchPath = "build/localization/$TargetLocale-batch.json"
    }
    $absoluteBatchPath = Resolve-ProjectPath $RequestedBatchPath
    $batch = [ordered]@{
        schemaVersion = 1
        locale = $TargetLocale
        sourceLocale = 'en'
        sourcePath = 'data/campaign/rules.csv'
        sourceFileSha256 = Get-FileSha256 $rules.Path
        referenceIndexFingerprint = [string](
            Get-OptionalProperty $referenceIndex 'fingerprint' '')
        instructions = 'localization/rules/translation_brief.md'
        entries = [object[]]$batchEntries.ToArray()
    }
    Write-JsonFile -Path $absoluteBatchPath -Value $batch
    Write-Host "Exported $($batchEntries.Count) $TargetLocale unit(s) to $absoluteBatchPath"
}

function Import-TranslationBatch {
    param(
        [string]$TargetLocale,
        [string]$RulesPath,
        [string]$RequestedBatchPath
    )

    if ([string]::IsNullOrWhiteSpace($RequestedBatchPath)) {
        throw 'Import requires -BatchPath'
    }
    $absoluteBatchPath = Resolve-ProjectPath $RequestedBatchPath
    if (-not (Test-Path -LiteralPath $absoluteBatchPath -PathType Leaf)) {
        throw "Batch file not found: $absoluteBatchPath"
    }

    $batch = Get-Content -LiteralPath $absoluteBatchPath -Raw -Encoding UTF8 |
        ConvertFrom-Json
    if ([int](Get-OptionalProperty $batch 'schemaVersion' 0) -ne 1) {
        throw "Unsupported batch schema in $absoluteBatchPath"
    }
    if ([string](Get-OptionalProperty $batch 'locale' '') -cne $TargetLocale) {
        throw "Batch locale does not match $TargetLocale"
    }

    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    $unitTable = @{}
    foreach ($unit in $units) { $unitTable[$unit.Key] = $unit }

    $store = Read-TranslationStore $TargetLocale
    $entryTable = Get-EntryTable $store
    $imported = 0
    foreach ($candidate in @((Get-OptionalProperty $batch 'entries' @()))) {
        $key = [string](Get-OptionalProperty $candidate 'key' '')
        if (-not $unitTable.ContainsKey($key)) {
            throw "Batch contains unknown or removed unit '$key'"
        }
        $unit = $unitTable[$key]
        if (-not (Test-UnitSourceCurrent $candidate $unit)) {
            throw "Batch unit '$key' is stale; export it again from current English rules.csv"
        }
        $target = [string](Get-OptionalProperty $candidate 'target' '')
        if ([string]::IsNullOrWhiteSpace($target)) {
            Write-Warning "Skipped blank target '$key'"
            continue
        }
        $status = [string](Get-OptionalProperty $candidate 'status' '')
        if ([string]::IsNullOrWhiteSpace($status)) { $status = 'draft' }
        if ($validStatuses -notcontains $status) {
            throw "Batch unit '$key' has invalid status '$status'"
        }
        $confidence = [string](Get-OptionalProperty $candidate 'confidence' '')
        if ($validConfidences -notcontains $confidence) {
            throw "Batch unit '$key' has invalid confidence '$confidence'"
        }
        $entryTable[$key] = New-NormalizedStoredEntry $unit $candidate
        $imported++
    }

    Write-TranslationStore $TargetLocale $entryTable $units
    Write-Host "Imported $imported $TargetLocale unit(s) into $(Get-StorePath $TargetLocale)"
}

function Set-OrdinalIgnoreCaseText {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)][string]$Source,
        [AllowEmptyString()][string]$Replacement
    )

    return [regex]::Replace(
        $Text,
        [regex]::Escape($Source),
        [System.Text.RegularExpressions.MatchEvaluator]{
            param($match)
            return $Replacement
        },
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
}

function Get-GlossaryTermPattern {
    param([Parameter(Mandatory = $true)][string]$Term)

    $escaped = [regex]::Escape($Term)
    $prefix = if ($Term -match '^[A-Za-z0-9]') { '(?<![A-Za-z0-9])' } else { '' }
    $suffix = if ($Term -match '[A-Za-z0-9]$') { '(?![A-Za-z0-9])' } else { '' }
    return $prefix + $escaped + $suffix
}

function Test-GlossaryTermPresent {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)][string]$Term
    )

    return [regex]::IsMatch(
        $Text,
        (Get-GlossaryTermPattern $Term),
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
}

function Set-GlossaryTermText {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)][string]$Source,
        [AllowEmptyString()][string]$Replacement
    )

    return [regex]::Replace(
        $Text,
        (Get-GlossaryTermPattern $Source),
        [System.Text.RegularExpressions.MatchEvaluator]{
            param($match)
            return $Replacement
        },
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
}

function Protect-PatternText {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)][string]$Pattern,
        [AllowEmptyString()][string]$ReplacementValue,
        [Parameter(Mandatory = $true)]$Restorations,
        [Parameter(Mandatory = $true)][ref]$PlaceholderNumber,
        [System.Text.RegularExpressions.RegexOptions]$Options =
            [System.Text.RegularExpressions.RegexOptions]::None,
        [switch]$PreserveMatch
    )

    $evaluator = [System.Text.RegularExpressions.MatchEvaluator]{
        param($match)
        $placeholder = '<x id="P{0:D4}"/>' -f $PlaceholderNumber.Value
        $PlaceholderNumber.Value++
        $value = if ($PreserveMatch) {
            [string]$match.Value
        } else {
            [string]$ReplacementValue
        }
        $Restorations[$placeholder] = $value
        return $placeholder
    }.GetNewClosure()
    return [regex]::Replace($Text, $Pattern, $evaluator, $Options)
}

function Protect-MachineTranslationText {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)]$Glossary,
        [Parameter(Mandatory = $true)][string]$TargetLocale,
        [bool]$ApplyGlossary
    )

    $protected = [string]$Text
    $restorations = [ordered]@{}
    $placeholderNumber = 0

    # Give every structural occurrence a unique XML placeholder so DeepL's XML
    # handling preserves both count and order. This includes individual quote
    # marks and physical newlines, not only variables and format tokens.
    $exactPattern = (
        '\r\n|\n|"|' +
        '\$[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)*|' +
        '%[-+0-9.]*[A-Za-z]|' +
        '\\[nrt]|' +
        '(?<![\p{L}\p{N}_])\d[\d,.]*(?:%|f)?(?![\p{L}\p{N}_])')
    $protected = Protect-PatternText `
        $protected `
        $exactPattern `
        '' `
        $restorations `
        ([ref]$placeholderNumber) `
        -PreserveMatch

    foreach ($row in @($Glossary | Where-Object { $_.status -eq 'keep-latin' } |
            Sort-Object { $_.source.Length } -Descending)) {
        $sourceValue = if ($ApplyGlossary) {
            [string]$row.source
        } else {
            [string](Get-GlossaryTarget $row $TargetLocale)
        }
        $targetValue = [string](Get-GlossaryTarget $row $TargetLocale)
        if (-not [string]::IsNullOrWhiteSpace($sourceValue) -and
                -not [string]::IsNullOrWhiteSpace($targetValue) -and
                (Test-GlossaryTermPresent $protected $sourceValue)) {
            $protected = Protect-PatternText `
                $protected `
                (Get-GlossaryTermPattern $sourceValue) `
                $targetValue `
                $restorations `
                ([ref]$placeholderNumber) `
                ([System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
        }
    }

    if ($ApplyGlossary) {
        foreach ($row in @($Glossary | Where-Object { $_.status -eq 'locked' } |
                Sort-Object { $_.source.Length } -Descending)) {
            $sourceTerm = [string]$row.source
            $targetTerm = [string](Get-GlossaryTarget $row $TargetLocale)
            if (-not [string]::IsNullOrWhiteSpace($sourceTerm) -and
                    -not [string]::IsNullOrWhiteSpace($targetTerm) -and
                    (Test-GlossaryTermPresent $protected $sourceTerm)) {
                $protected = Protect-PatternText `
                    $protected `
                    (Get-GlossaryTermPattern $sourceTerm) `
                    $targetTerm `
                    $restorations `
                    ([ref]$placeholderNumber) `
                    ([System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
            }
        }
    }

    return [PSCustomObject]@{
        Text = $protected
        Restorations = $restorations
    }
}

function Restore-MachineTranslationText {
    param(
        [AllowEmptyString()][string]$Text,
        [Parameter(Mandatory = $true)]$Restorations
    )

    $restored = [string]$Text
    $sentinelRestorations = [ordered]@{}

    # Temporarily replace the XML tags themselves before quote cleanup. The
    # attribute delimiters are ASCII quotes too, and stripping them first would
    # make every placeholder impossible to recognize.
    $sentinelNumber = 0
    foreach ($placeholder in $Restorations.Keys) {
        $value = [string]$Restorations[$placeholder]
        $idMatch = [regex]::Match([string]$placeholder, 'id="(?<id>[^"]+)"')
        if (-not $idMatch.Success) {
            throw "Invalid machine-translation placeholder '$placeholder'"
        }
        $placeholderPattern = (
            '<x\s+id\s*=\s*["'']' +
            [regex]::Escape($idMatch.Groups['id'].Value) +
            '["''](?:\s*/\s*>|\s*>\s*</x\s*>)')
        $matches = [regex]::Matches(
            $restored,
            $placeholderPattern,
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
        if ($matches.Count -ne 1) {
            throw (
                "DeepL changed XML placeholder '$($idMatch.Groups['id'].Value)' " +
                "($($matches.Count) occurrences); no draft was written.")
        }
        $sentinel = 'QZXRESTORE{0:D4}QZZ' -f $sentinelNumber
        $sentinelNumber++
        $restored = [regex]::Replace(
            $restored,
            $placeholderPattern,
            $sentinel,
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)
        $sentinelRestorations[$sentinel] = $value
    }

    # Chinese MT systems commonly emit typographic or corner quotation marks.
    # Starsector dialogue in rules.csv must use straight ASCII double quotes.
    foreach ($quote in @(
            [char]0x201c, [char]0x201d,
            [char]0x300c, [char]0x300d,
            [char]0x300e, [char]0x300f,
            [char]0xff02)) {
        $restored = $restored.Replace([string]$quote, '"')
    }
    # All source double quotes are shielded as placeholders, so any quote still
    # present here was introduced by the translation engine and is removed.
    $restored = $restored.Replace('"', '')
    foreach ($sentinel in $sentinelRestorations.Keys) {
        $value = [string]$sentinelRestorations[$sentinel]
        $sentinelPattern = [regex]::Escape([string]$sentinel)
        if ($value -eq "`r`n" -or $value -eq "`n") {
            $restored = [regex]::Replace(
                $restored,
                '[ \t]*' + $sentinelPattern + '[ \t]*',
                $value)
        } else {
            $restored = [regex]::Replace(
                $restored,
                $sentinelPattern,
                [System.Text.RegularExpressions.MatchEvaluator]{
                    param($match)
                    return $value
                }.GetNewClosure())
        }
    }
    if ([regex]::IsMatch($restored, '<x\b',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase)) {
        throw 'DeepL returned an unrecognized XML placeholder; no draft was written.'
    }
    return $restored
}

function Get-DeepLApiKey {
    $key = [string]$env:DEEPL_API_KEY
    if ([string]::IsNullOrWhiteSpace($key)) {
        $key = [string][Environment]::GetEnvironmentVariable(
            'DEEPL_API_KEY',
            [EnvironmentVariableTarget]::User)
    }
    if ([string]::IsNullOrWhiteSpace($key)) {
        throw (
            'DeepL drafting requires DEEPL_API_KEY in the process or Windows user environment. ' +
            'The key is read at runtime and must not be committed to the repository.')
    }
    return $key.Trim()
}

function Get-DeepLApiRoot {
    param([Parameter(Mandatory = $true)][string]$ApiKey)

    if ($ApiKey.EndsWith(':fx', [System.StringComparison]::Ordinal)) {
        return 'https://api-free.deepl.com'
    }
    return 'https://api.deepl.com'
}

function Get-DeepLCustomInstructions {
    param(
        [Parameter(Mandatory = $true)][string]$TargetLanguage,
        [bool]$IsBackTranslation
    )

    if ($IsBackTranslation) {
        return [string[]]@(
            'Produce a literal, plain-English back-translation for semantic comparison.',
            'Preserve paragraph breaks, numbers, ASCII quotation marks, and every XML <x/> placeholder exactly.',
            'Do not embellish, explain, summarize, or repair ambiguity in the Chinese text.')
    }
    if ($TargetLanguage -eq 'ZH-HANT') {
        return [string[]]@(
            'Translate directly from English into natural Taiwan Mandarin using Traditional characters.',
            'Do not imitate Mainland wording and do not mechanically convert a Simplified Chinese draft.',
            'Preserve paragraph breaks, numbers, ASCII quotation marks, and every XML <x/> placeholder exactly.',
            'Keep player options concise; preserve character voice, implication, and science-fiction terminology.')
    }
    return [string[]]@(
        'Translate into natural Mainland Mandarin using Simplified Chinese characters.',
        'Preserve paragraph breaks, numbers, ASCII quotation marks, and every XML <x/> placeholder exactly.',
        'Keep player options concise; preserve character voice, implication, and science-fiction terminology.')
}

function Get-DeepLContext {
    param([bool]$IsBackTranslation)

    if ($IsBackTranslation) {
        return (
            'This is a fidelity audit of localized dialogue and interface text for a science-fiction game. ' +
            'The English output will be compared with the original source for omissions and semantic drift.')
    }
    return (
        'This is authored dialogue and interface text for the Starsector science-fiction mod Hall of Triumph. ' +
        'Isa is a technically fluent, energetic, irreverent engineer; Isaac and archival records are controlled ' +
        'and information-dense. Player choices and runtime status lines should be concise. Technical mechanics ' +
        'must remain precise, and elevated or religious wording should only be elevated when the English is.')
}

function Get-DeepLRequestGroups {
    param([Parameter(Mandatory = $true)]$Requests)

    $groups = New-Object 'System.Collections.Generic.List[object]'
    $current = New-Object 'System.Collections.Generic.List[object]'
    $characters = 0
    foreach ($request in @($Requests | Sort-Object Ordinal)) {
        $length = ([string]$request.Protected.Text).Length
        if ($current.Count -gt 0 -and
                ($current.Count -ge 40 -or $characters + $length -gt 60000)) {
            $groups.Add([PSCustomObject]@{
                    Items = [object[]]$current.ToArray()
                })
            $current = New-Object 'System.Collections.Generic.List[object]'
            $characters = 0
        }
        $current.Add($request)
        $characters += $length
    }
    if ($current.Count -gt 0) {
        $groups.Add([PSCustomObject]@{
                Items = [object[]]$current.ToArray()
            })
    }
    return [object[]]$groups.ToArray()
}

function Invoke-DeepLTranslationRequests {
    param(
        [Parameter(Mandatory = $true)]$Requests,
        [Parameter(Mandatory = $true)][string]$SourceLanguage,
        [Parameter(Mandatory = $true)][string]$TargetLanguage,
        [bool]$IsBackTranslation
    )

    $apiKey = Get-DeepLApiKey
    $apiRoot = Get-DeepLApiRoot $apiKey
    $handler = New-Object System.Net.Http.HttpClientHandler
    $client = New-Object System.Net.Http.HttpClient($handler)
    $client.Timeout = [TimeSpan]::FromSeconds(90)
    $client.DefaultRequestHeaders.TryAddWithoutValidation(
        'Authorization',
        "DeepL-Auth-Key $apiKey") | Out-Null
    try {
        $groups = @(Get-DeepLRequestGroups $Requests)
        $groupNumber = 0
        foreach ($group in $groups) {
            $groupNumber++
            $items = @($group.Items)
            $body = [ordered]@{
                text = [string[]]@($items | ForEach-Object {
                        [string]$_.Protected.Text
                    })
                source_lang = $SourceLanguage
                target_lang = $TargetLanguage
                preserve_formatting = $true
                tag_handling = 'xml'
                context = Get-DeepLContext $IsBackTranslation
                custom_instructions = [string[]](
                    Get-DeepLCustomInstructions $TargetLanguage $IsBackTranslation)
            }
            $json = $body | ConvertTo-Json -Depth 8 -Compress
            $response = $null
            $responseText = ''
            for ($attempt = 1; $attempt -le 4; $attempt++) {
                $content = New-Object System.Net.Http.StringContent(
                    $json,
                    [System.Text.Encoding]::UTF8,
                    'application/json')
                try {
                    $response = $client.PostAsync(
                        "$apiRoot/v2/translate",
                        $content).GetAwaiter().GetResult()
                    $responseText = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
                } finally {
                    $content.Dispose()
                }
                if ($response.IsSuccessStatusCode) { break }
                $status = [int]$response.StatusCode
                if ($attempt -lt 4 -and @(
                        429, 500, 502, 503, 504) -contains $status) {
                    $response.Dispose()
                    $response = $null
                    Start-Sleep -Seconds ([math]::Pow(2, $attempt - 1))
                    continue
                }
                throw (
                    "DeepL request failed (HTTP $status) for batch $groupNumber/$($groups.Count): " +
                    $responseText)
            }

            try {
                $parsed = $responseText | ConvertFrom-Json
                $translations = @($parsed.translations)
                if ($translations.Count -ne $items.Count) {
                    throw (
                        "DeepL returned $($translations.Count) translations for " +
                        "$($items.Count) requested strings.")
                }
                for ($index = 0; $index -lt $items.Count; $index++) {
                    $translated = [string]$translations[$index].text
                    $items[$index].Result = Restore-MachineTranslationText `
                        $translated $items[$index].Protected.Restorations
                }
            } finally {
                if ($null -ne $response) { $response.Dispose() }
            }
            Write-Progress `
                -Activity "DeepL $SourceLanguage -> $TargetLanguage" `
                -Status "Batch $groupNumber of $($groups.Count)" `
                -PercentComplete (($groupNumber / $groups.Count) * 100)
        }
        Write-Progress -Activity "DeepL $SourceLanguage -> $TargetLanguage" -Completed
    } finally {
        $client.Dispose()
        $handler.Dispose()
    }
}

function Show-DeepLUsage {
    Add-Type -AssemblyName System.Net.Http
    $apiKey = Get-DeepLApiKey
    $apiRoot = Get-DeepLApiRoot $apiKey
    $handler = New-Object System.Net.Http.HttpClientHandler
    $client = New-Object System.Net.Http.HttpClient($handler)
    $client.DefaultRequestHeaders.TryAddWithoutValidation(
        'Authorization',
        "DeepL-Auth-Key $apiKey") | Out-Null
    try {
        $response = $client.GetAsync("$apiRoot/v2/usage").GetAwaiter().GetResult()
        try {
            $responseText = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
            if (-not $response.IsSuccessStatusCode) {
                throw "DeepL usage request failed (HTTP $([int]$response.StatusCode)): $responseText"
            }
            $usage = $responseText | ConvertFrom-Json
            $used = [long]$usage.character_count
            $limit = [long]$usage.character_limit
            Write-Host "DeepL characters: $used used, $($limit - $used) remaining, $limit limit"
        } finally {
            $response.Dispose()
        }
    } finally {
        $client.Dispose()
        $handler.Dispose()
    }
}

function Get-BackTranslationCoverage {
    param(
        [AllowEmptyString()][string]$Source,
        [AllowEmptyString()][string]$BackTranslation
    )

    $sourceTerms = @([regex]::Matches($Source.ToLowerInvariant(), '[a-z]{3,}') |
        ForEach-Object { $_.Value } |
        Where-Object { -not $script:referenceStopwords.ContainsKey($_) } |
        Sort-Object -Unique)
    if ($sourceTerms.Count -eq 0) { return 1.0 }
    $backTerms = @([regex]::Matches(
            $BackTranslation.ToLowerInvariant(), '[a-z]{3,}') |
        ForEach-Object { $_.Value } | Sort-Object -Unique)
    $matched = @($sourceTerms | Where-Object { $backTerms -contains $_ }).Count
    return [double]$matched / [double]$sourceTerms.Count
}

function New-MachineDrafts {
    param(
        [string]$TargetLocale,
        [string]$RulesPath,
        [int]$RequestedBatchSize,
        [int]$RequestedOffset,
        [bool]$RedraftTranslated,
        [bool]$CreateBackTranslations
    )

    if ($RequestedBatchSize -lt 1) { throw 'BatchSize must be at least 1' }
    if ($RequestedOffset -lt 0) { throw 'Offset cannot be negative' }

    Add-Type -AssemblyName System.Net.Http
    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    $store = Read-TranslationStore $TargetLocale
    $entryTable = Get-EntryTable $store
    $glossary = Read-Glossary
    $eligible = New-Object 'System.Collections.Generic.List[object]'
    foreach ($unit in $units) {
        $isCurrent = $false
        if ($entryTable.ContainsKey($unit.Key)) {
            $entry = $entryTable[$unit.Key]
            $isCurrent = (
                (Test-UnitSourceCurrent $entry $unit) -and
                -not [string]::IsNullOrWhiteSpace(
                    [string](Get-OptionalProperty $entry 'target' '')))
        }
        if ($RedraftTranslated -or -not $isCurrent) { $eligible.Add($unit) }
    }
    $selected = @($eligible | Select-Object -Skip $RequestedOffset -First $RequestedBatchSize)
    if ($selected.Count -eq 0) {
        throw 'No matching translation units remain for this draft request.'
    }

    $targetLanguage = if ($TargetLocale -eq 'zh-Hans-CN') {
        'ZH-HANS'
    } else {
        'ZH-HANT'
    }
    $sourceCache = @{}
    $requests = New-Object 'System.Collections.Generic.List[object]'
    $ordinal = 0
    foreach ($unit in $selected) {
        if ($sourceCache.ContainsKey($unit.Source)) { continue }
        $sourceCache[$unit.Source] = $null
        $requests.Add([PSCustomObject]@{
                Ordinal = $ordinal
                Key = $unit.Key
                Source = $unit.Source
                Protected = Protect-MachineTranslationText `
                    $unit.Source $glossary $TargetLocale $true
                Result = ''
            })
        $ordinal++
    }

    Write-Host "Drafting $($selected.Count) $TargetLocale unit(s) from English ($($requests.Count) unique strings)..."
    Invoke-DeepLTranslationRequests $requests 'EN' $targetLanguage $false
    foreach ($request in $requests) { $sourceCache[$request.Source] = $request.Result }

    $backCache = @{}
    if ($CreateBackTranslations) {
        $backRequests = New-Object 'System.Collections.Generic.List[object]'
        $ordinal = 0
        foreach ($source in $sourceCache.Keys) {
            $target = [string]$sourceCache[$source]
            if ($backCache.ContainsKey($target)) { continue }
            $backCache[$target] = $null
            $backRequests.Add([PSCustomObject]@{
                    Ordinal = $ordinal
                    Key = '<back-translation>'
                    Source = $target
                    Protected = Protect-MachineTranslationText `
                        $target $glossary $TargetLocale $false
                    Result = ''
                })
            $ordinal++
        }
        Write-Host "Back-translating $($backRequests.Count) unique $TargetLocale draft(s) for semantic drift checks..."
        # DeepL accepts ZH-HANS/ZH-HANT as Chinese targets, but currently
        # requires the generic ZH code when Chinese is the source language.
        Invoke-DeepLTranslationRequests $backRequests 'ZH' 'EN-US' $true
        foreach ($request in $backRequests) {
            $backCache[$request.Source] = $request.Result
        }
    }

    foreach ($unit in $selected) {
        $target = [string]$sourceCache[$unit.Source]
        $backTranslation = ''
        $coverage = 0.0
        $status = 'draft'
        $confidence = 'medium'
        $issues = @()
        if ($CreateBackTranslations) {
            $backTranslation = [string]$backCache[$target]
            $coverage = Get-BackTranslationCoverage $unit.Source $backTranslation
            if ($coverage -ge 0.34) {
                $status = 'machine-reviewed'
            } else {
                $confidence = 'low'
                $issues = @(
                    'Automated back-translation retained less than 34% of the source content vocabulary; check for semantic drift.')
            }
        }
        $candidate = [ordered]@{
            target = $target
            backTranslation = $backTranslation
            status = $status
            confidence = $confidence
            issues = [object[]]$issues
            reviewNotes = if ($CreateBackTranslations) {
                'DeepL draft generated directly from English after glossary/token protection and locale-specific instructions; reverse-direction machine back-translation lexical coverage: {0:P0}. Fluent review still required.' -f $coverage
            } else {
                'DeepL draft generated directly from English after glossary/token protection and locale-specific instructions. Fluent review still required.'
            }
            traditionalArtFragments = @()
            artNote = ''
            allowedLatin = @()
            allowVariant = $false
        }
        $entryTable[$unit.Key] = New-NormalizedStoredEntry $unit $candidate
    }

    Write-TranslationStore $TargetLocale $entryTable $units
    Write-Host "Stored $($selected.Count) $TargetLocale draft(s) in $(Get-StorePath $TargetLocale)"
}

function Add-QaIssue {
    param(
        [Parameter(Mandatory = $true)]$List,
        [ValidateSet('error', 'warning')][string]$Severity,
        [string]$Key,
        [string]$Message
    )
    $List.Add([PSCustomObject]@{
            Severity = $Severity
            Key = $Key
            Message = $Message
        }) | Out-Null
}

function Get-AllowedLatinTerms {
    param($Entry, $Glossary)

    $allowed = New-Object 'System.Collections.Generic.List[string]'
    foreach ($term in @((Get-OptionalProperty $Entry 'allowedLatin' @()))) {
        if (-not [string]::IsNullOrWhiteSpace([string]$term)) {
            $allowed.Add([string]$term)
        }
    }
    foreach ($row in $Glossary) {
        if ($row.status -eq 'keep-latin' -and
                -not [string]::IsNullOrWhiteSpace($row.source)) {
            $allowed.Add($row.source)
        }
    }
    return [string[]]@($allowed | Sort-Object -Unique)
}

function Invoke-TranslationCheck {
    param(
        [string]$TargetLocale,
        [string]$RulesPath,
        [bool]$MustBeComplete
    )

    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    $unitTable = @{}
    foreach ($unit in $units) { $unitTable[$unit.Key] = $unit }
    $store = Read-TranslationStore $TargetLocale
    $entryTable = Get-EntryTable $store
    $glossary = Read-Glossary
    $variants = Read-ScriptVariants
    $issues = New-Object 'System.Collections.Generic.List[object]'

    foreach ($key in $entryTable.Keys) {
        if (-not $unitTable.ContainsKey($key)) {
            Add-QaIssue $issues 'error' $key 'Translation no longer maps to a current rules.csv unit.'
        }
    }

    $translated = 0
    $stale = 0
    $currentEntries = New-Object 'System.Collections.Generic.List[object]'
    foreach ($unit in $units) {
        if (-not $entryTable.ContainsKey($unit.Key)) { continue }
        $entry = $entryTable[$unit.Key]
        $target = [string](Get-OptionalProperty $entry 'target' '')
        if ([string]::IsNullOrWhiteSpace($target)) { continue }
        $translated++

        if (-not (Test-UnitSourceCurrent $entry $unit)) {
            $stale++
            Add-QaIssue $issues 'error' $unit.Key 'English source changed; re-export and re-review this unit.'
            continue
        }
        $currentEntries.Add([PSCustomObject]@{ Unit = $unit; Entry = $entry })

        $status = [string](Get-OptionalProperty $entry 'status' '')
        if ($validStatuses -notcontains $status) {
            Add-QaIssue $issues 'error' $unit.Key "Invalid review status '$status'."
        }
        $confidence = [string](Get-OptionalProperty $entry 'confidence' '')
        if ($validConfidences -notcontains $confidence) {
            Add-QaIssue $issues 'error' $unit.Key "Invalid confidence '$confidence'."
        }
        $backTranslation = [string](Get-OptionalProperty $entry 'backTranslation' '')
        if (@('machine-reviewed', 'human-reviewed', 'locked') -contains $status -and
                [string]::IsNullOrWhiteSpace($backTranslation)) {
            Add-QaIssue $issues 'error' $unit.Key "$status requires an independent English back-translation."
        }
        $openIssues = @((Get-OptionalProperty $entry 'issues' @()))
        if ($status -eq 'locked' -and $openIssues.Count -gt 0) {
            Add-QaIssue $issues 'error' $unit.Key 'Locked text still has unresolved issues.'
        }

        if ($target.Contains([string][char]0x201C) -or
                $target.Contains([string][char]0x201D)) {
            Add-QaIssue $issues 'error' $unit.Key 'Contains forbidden U+201C/U+201D smart double quotation marks; use straight ASCII quotes.'
        }
        if ($unit.Field -eq 'script' -and $target.Contains('"')) {
            Add-QaIssue $issues 'error' $unit.Key 'Script-string translations cannot contain ASCII double quotes.'
        }

        $sourceTokens = @(Get-StrictTokenSequence $unit.Source)
        $targetTokens = @(Get-StrictTokenSequence $target)
        if (-not (Test-TokenSequencesEqual $sourceTokens $targetTokens)) {
            Add-QaIssue $issues 'error' $unit.Key (
                "Protected tokens changed or moved. Source=[$($sourceTokens -join ', ')] Target=[$($targetTokens -join ', ')]")
        }

        $sourceNewlines = ([regex]::Matches($unit.Source, "`r?`n")).Count
        $targetNewlines = ([regex]::Matches($target, "`r?`n")).Count
        if ($sourceNewlines -ne $targetNewlines) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Paragraph/newline count changed from $sourceNewlines to $targetNewlines.")
        }
        $sourceQuotes = ([regex]::Matches($unit.Source, '"')).Count
        $targetQuotes = ([regex]::Matches($target, '"')).Count
        if (($targetQuotes % 2) -ne 0) {
            Add-QaIssue $issues 'error' $unit.Key 'Target contains an unmatched ASCII double quotation mark.'
        } elseif ($sourceQuotes -ne $targetQuotes) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Spoken-quotation count changed from $sourceQuotes to $targetQuotes.")
        }

        $sourceNumbers = @(Get-NumericTokenSequence $unit.Source)
        $targetNumbers = @(Get-NumericTokenSequence $target)
        if (-not (Test-TokenSequencesEqual $sourceNumbers $targetNumbers)) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Numeric tokens differ. Source=[$($sourceNumbers -join ', ')] Target=[$($targetNumbers -join ', ')]")
        }

        $traditionalArtFragments = @(
            (Get-OptionalProperty $entry 'traditionalArtFragments' @()))
        $artNote = [string](Get-OptionalProperty $entry 'artNote' '')
        if ($traditionalArtFragments.Count -gt 0 -and $TargetLocale -ne 'zh-Hans-CN') {
            Add-QaIssue $issues 'warning' $unit.Key 'traditionalArtFragments is only meaningful in the Simplified locale.'
        }
        if ($traditionalArtFragments.Count -gt 0 -and [string]::IsNullOrWhiteSpace($artNote)) {
            Add-QaIssue $issues 'error' $unit.Key 'Artistic Traditional text requires a non-empty artNote.'
        }

        $scriptScanTarget = $target
        foreach ($fragmentValue in $traditionalArtFragments) {
            $fragment = [string]$fragmentValue
            if ([string]::IsNullOrWhiteSpace($fragment)) {
                Add-QaIssue $issues 'error' $unit.Key 'traditionalArtFragments contains a blank fragment.'
                continue
            }
            if (-not $target.Contains($fragment)) {
                Add-QaIssue $issues 'error' $unit.Key (
                    "Declared Traditional art fragment is not present verbatim: '$fragment'")
                continue
            }
            $scriptScanTarget = $scriptScanTarget.Replace($fragment, '')
        }

        $wrongCharacters = New-Object 'System.Collections.Generic.List[string]'
        foreach ($pair in $variants) {
            $wrong = if ($TargetLocale -eq 'zh-Hans-CN') { $pair.hant } else { $pair.hans }
            if (-not [string]::IsNullOrWhiteSpace($wrong) -and $scriptScanTarget.Contains($wrong)) {
                $wrongCharacters.Add($wrong)
            }
        }
        if ($wrongCharacters.Count -gt 0) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Possible script mixing: $((@($wrongCharacters | Sort-Object -Unique)) -join ' ')")
        }

        $allowedLatin = @(Get-AllowedLatinTerms $entry $glossary)
        $latinScan = [regex]::Replace(
            $target,
            '\$[A-Za-z_][A-Za-z0-9_]*(?:\.[A-Za-z_][A-Za-z0-9_]*)*',
            '')
        $unexpectedLatin = New-Object 'System.Collections.Generic.List[string]'
        foreach ($match in [regex]::Matches($latinScan, '[A-Za-z]{4,}')) {
            $isAllowed = $false
            foreach ($term in $allowedLatin) {
                if ($match.Value -ieq $term -or
                        $term.IndexOf($match.Value, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
                    $isAllowed = $true
                    break
                }
            }
            if (-not $isAllowed) { $unexpectedLatin.Add($match.Value) }
        }
        if ($unexpectedLatin.Count -gt 0) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Possible untranslated English: $((@($unexpectedLatin | Sort-Object -Unique)) -join ', ')")
        }
        if ([regex]::IsMatch(
                $latinScan,
                '(?<![A-Za-z.])I(?![A-Za-z])')) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Possible untranslated English first-person pronoun: 'I'.")
        }
        if ([regex]::IsMatch(
                $latinScan,
                '(?<![A-Za-z0-9_.-])[A-HJ-Z](?=[\u3400-\u9FFF])')) {
            Add-QaIssue $issues 'warning' $unit.Key (
                'Possible single-letter English residue immediately before Chinese text.')
        }
        if ($target.Contains('(s)')) {
            Add-QaIssue $issues 'warning' $unit.Key (
                "Possible untranslated English plural suffix: '(s)'.")
        }

        foreach ($glossaryRow in $glossary) {
            if ($glossaryRow.status -ne 'locked') { continue }
            if (-not (Test-GlossaryTermPresent `
                    $unit.Source `
                    ([string]$glossaryRow.source))) {
                continue
            }
            $required = Get-GlossaryTarget $glossaryRow $TargetLocale
            if ([string]::IsNullOrWhiteSpace($required)) {
                Add-QaIssue $issues 'error' $unit.Key (
                    "Locked glossary term '$($glossaryRow.source)' has no $TargetLocale value.")
            } elseif ($target.IndexOf(
                    $required,
                    [System.StringComparison]::Ordinal) -lt 0) {
                Add-QaIssue $issues 'error' $unit.Key (
                    "Locked term '$($glossaryRow.source)' must use '$required'.")
            }
        }
    }

    foreach ($group in @($currentEntries | Group-Object { $_.Unit.Source })) {
        if ($group.Count -lt 2) { continue }
        $targets = @($group.Group | ForEach-Object {
                [string](Get-OptionalProperty $_.Entry 'target' '')
            } | Sort-Object -Unique)
        if ($targets.Count -le 1) { continue }
        $allAllowVariants = $true
        foreach ($item in $group.Group) {
            if (-not [bool](Get-OptionalProperty $item.Entry 'allowVariant' $false)) {
                $allAllowVariants = $false
                break
            }
        }
        if (-not $allAllowVariants) {
            Add-QaIssue $issues 'warning' $group.Group[0].Unit.Key (
                "Repeated English source has $($targets.Count) different translations; set allowVariant with review notes only when context requires it.")
        }
    }

    $missing = $units.Count - $translated + $stale
    if ($MustBeComplete -and $missing -gt 0) {
        Add-QaIssue $issues 'error' '<catalog>' (
            "$missing of $($units.Count) translation units are missing or stale.")
    }

    # A complete catalog can be assembled in memory, so run the same structural
    # parity test used by Build. This catches translated SetTextHighlights values
    # that no longer occur verbatim in their translated text before writing CSV.
    if ($missing -eq 0) {
        try {
            $localized = Apply-TranslationsToRules $rules $entryTable $false
            foreach ($parityError in @(Test-BuiltRulesParity $rules $localized)) {
                Add-QaIssue $issues 'error' '<parity>' $parityError
            }
        } catch {
            Add-QaIssue $issues 'error' '<parity>' $_.Exception.Message
        }
    }

    $errors = @($issues | Where-Object { $_.Severity -eq 'error' })
    $warnings = @($issues | Where-Object { $_.Severity -eq 'warning' })
    foreach ($issue in @($issues | Sort-Object Severity, Key, Message)) {
        $prefix = $issue.Severity.ToUpperInvariant()
        Write-Host "$prefix [$($issue.Key)] $($issue.Message)"
    }
    Write-Host (
        "Checked ${TargetLocale}: $translated/$($units.Count) translated, $missing missing/stale, " +
        "$($errors.Count) error(s), $($warnings.Count) warning(s)")

    return [PSCustomObject]@{
        Total = $units.Count
        Translated = $translated
        Missing = $missing
        Errors = $errors.Count
        Warnings = $warnings.Count
    }
}

function Get-TranslatedValue {
    param(
        [string]$Key,
        [string]$Source,
        [hashtable]$EntryTable,
        [bool]$CanBeIncomplete
    )

    if ($EntryTable.ContainsKey($Key)) {
        $entry = $EntryTable[$Key]
        if (-not [string]::IsNullOrWhiteSpace(
                [string](Get-OptionalProperty $entry 'target' ''))) {
            return [string](Get-OptionalProperty $entry 'target' '')
        }
    }
    if ($CanBeIncomplete) { return $Source }
    throw "Missing translation for '$Key'"
}

function Copy-RulesObject {
    param($Rules)

    $rows = New-Object 'System.Collections.Generic.List[object]'
    foreach ($row in $Rules.Rows) {
        $rows.Add([PSCustomObject]@{
                Index = $row.Index
                StartLine = $row.StartLine
                Fields = [string[]]$row.Fields.Clone()
            })
    }
    return [PSCustomObject]@{
        Path = $Rules.Path
        Header = [string[]]$Rules.Header.Clone()
        Rows = [object[]]$rows.ToArray()
    }
}

function Apply-TranslationsToRules {
    param(
        $Rules,
        [hashtable]$EntryTable,
        [bool]$CanBeIncomplete
    )

    $localized = Copy-RulesObject $Rules
    foreach ($row in $localized.Rows) {
        $fields = $row.Fields
        $ruleId = $fields[0]
        if ([string]::IsNullOrWhiteSpace($ruleId) -or $ruleId.StartsWith('#')) {
            continue
        }

        if (-not [string]::IsNullOrWhiteSpace($fields[4])) {
            $key = New-TextKey $ruleId
            $fields[4] = Get-TranslatedValue $key $fields[4] $EntryTable $CanBeIncomplete
        }

        $optionLines = @($fields[5] -split "`r?`n")
        for ($index = 0; $index -lt $optionLines.Count; $index++) {
            $line = $optionLines[$index]
            if ([string]::IsNullOrWhiteSpace($line)) { continue }
            $parsed = Parse-OptionLine $line
            if ($null -eq $parsed) {
                throw "Malformed option while building rule '$ruleId': $line"
            }
            if ([string]::IsNullOrWhiteSpace($parsed.Label)) { continue }
            $key = New-OptionKey $ruleId ($index + 1) $parsed.Id
            $target = Get-TranslatedValue $key $parsed.Label $EntryTable $CanBeIncomplete
            $optionLines[$index] = $parsed.Prefix + $target
        }
        $fields[5] = $optionLines -join "`n"

        $scriptLines = @($fields[3] -split "`r?`n")
        for ($lineIndex = 0; $lineIndex -lt $scriptLines.Count; $lineIndex++) {
            $line = $scriptLines[$lineIndex]
            $matches = @(Get-ScriptLiteralMatches $line)
            for ($matchIndex = $matches.Count - 1; $matchIndex -ge 0; $matchIndex--) {
                $literal = $matches[$matchIndex]
                if ([string]::IsNullOrWhiteSpace($literal.Value)) { continue }
                $key = New-ScriptKey $ruleId ($lineIndex + 1) $literal.Command $literal.LiteralIndex
                $target = Get-TranslatedValue $key $literal.Value $EntryTable $CanBeIncomplete
                if ($target.Contains('"')) {
                    throw "Script translation '$key' contains an unsupported ASCII double quote"
                }
                $line = $line.Remove($literal.ContentStart, $literal.ContentLength)
                $line = $line.Insert($literal.ContentStart, $target)
            }
            $scriptLines[$lineIndex] = $line
        }
        $fields[3] = $scriptLines -join "`n"
    }
    return $localized
}

function Test-BuiltRulesParity {
    param($SourceRules, $BuiltRules)

    $errors = New-Object 'System.Collections.Generic.List[string]'
    if ($SourceRules.Rows.Count -ne $BuiltRules.Rows.Count) {
        $errors.Add("Rule count changed from $($SourceRules.Rows.Count) to $($BuiltRules.Rows.Count)")
        return [string[]]$errors.ToArray()
    }

    for ($index = 0; $index -lt $SourceRules.Rows.Count; $index++) {
        $source = $SourceRules.Rows[$index].Fields
        $built = $BuiltRules.Rows[$index].Fields
        foreach ($column in @(0, 1, 2, 6)) {
            if ($source[$column] -cne $built[$column]) {
                $errors.Add(
                    "Rule '$($source[0])' changed protected column '$($expectedHeader[$column])'")
            }
        }

        $sourceOptions = @($source[5] -split "`r?`n")
        $builtOptions = @($built[5] -split "`r?`n")
        if ($sourceOptions.Count -ne $builtOptions.Count) {
            $errors.Add("Rule '$($source[0])' changed option-line count")
        } else {
            for ($optionIndex = 0; $optionIndex -lt $sourceOptions.Count; $optionIndex++) {
                if ([string]::IsNullOrWhiteSpace($sourceOptions[$optionIndex]) -and
                        [string]::IsNullOrWhiteSpace($builtOptions[$optionIndex])) {
                    continue
                }
                $sourceOption = Parse-OptionLine $sourceOptions[$optionIndex]
                $builtOption = Parse-OptionLine $builtOptions[$optionIndex]
                if ($null -eq $sourceOption -or $null -eq $builtOption -or
                        $sourceOption.Prefix -cne $builtOption.Prefix) {
                    $errors.Add(
                        "Rule '$($source[0])' changed option priority or option ID at line $($optionIndex + 1)")
                }
            }
        }

        $sourceScriptLines = @($source[3] -split "`r?`n")
        $builtScriptLines = @($built[3] -split "`r?`n")
        if ($sourceScriptLines.Count -ne $builtScriptLines.Count) {
            $errors.Add("Rule '$($source[0])' changed script-line count")
            continue
        }
        for ($scriptIndex = 0; $scriptIndex -lt $sourceScriptLines.Count; $scriptIndex++) {
            $sourceLiterals = @(Get-ScriptLiteralMatches $sourceScriptLines[$scriptIndex])
            $builtLiterals = @(Get-ScriptLiteralMatches $builtScriptLines[$scriptIndex])
            if ($sourceLiterals.Count -ne $builtLiterals.Count) {
                $errors.Add(
                    "Rule '$($source[0])' changed user-facing script literal count on script line $($scriptIndex + 1)")
                continue
            }
            for ($literalIndex = 0; $literalIndex -lt $sourceLiterals.Count; $literalIndex++) {
                if ($sourceLiterals[$literalIndex].Command -eq 'SetTextHighlights' -and
                        $source[4].Contains($sourceLiterals[$literalIndex].Value) -and
                        -not $built[4].Contains($builtLiterals[$literalIndex].Value)) {
                    $errors.Add(
                        "Rule '$($source[0])' translated highlight '$($builtLiterals[$literalIndex].Value)' is not verbatim in translated text")
                }
            }
        }
    }
    return [string[]]$errors.ToArray()
}

function Invoke-RulesValidator {
    param([string]$RulesPath)

    $validator = Resolve-ProjectPath 'tools/validate_rules.ps1'
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $validator -RulesPath $RulesPath
    if ($LASTEXITCODE -ne 0) {
        throw "validate_rules.ps1 failed for $RulesPath"
    }
}

function Build-LocalizedRules {
    param(
        [string]$TargetLocale,
        [string]$RulesPath,
        [string]$RequestedOutputPath,
        [bool]$CanBeIncomplete
    )

    $check = Invoke-TranslationCheck $TargetLocale $RulesPath (-not $CanBeIncomplete)
    if ($check.Errors -gt 0) {
        throw "Cannot build $TargetLocale with $($check.Errors) localization error(s)"
    }

    $sourceRules = Read-RulesFile $RulesPath
    $store = Read-TranslationStore $TargetLocale
    $entryTable = Get-EntryTable $store
    $localized = Apply-TranslationsToRules $sourceRules $entryTable $CanBeIncomplete

    if ([string]::IsNullOrWhiteSpace($RequestedOutputPath)) {
        $RequestedOutputPath = "build/localization/$TargetLocale/data/campaign/rules.csv"
    }
    $absoluteOutputPath = Write-RulesFile $localized $RequestedOutputPath
    $builtRules = Read-RulesFile $absoluteOutputPath
    $parityErrors = @(Test-BuiltRulesParity $sourceRules $builtRules)
    if ($parityErrors.Count -gt 0) {
        foreach ($parityError in $parityErrors) { Write-Error $parityError }
        throw "Localized rules parity check failed with $($parityErrors.Count) error(s)"
    }
    Invoke-RulesValidator $absoluteOutputPath

    $mode = if ($CanBeIncomplete) { 'PARTIAL DIAGNOSTIC' } else { 'COMPLETE' }
    Write-Host "Built $mode $TargetLocale rules.csv: $absoluteOutputPath"
}

function Show-TranslationReport {
    param([string]$TargetLocale, [string]$RulesPath)

    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    $store = Read-TranslationStore $TargetLocale
    $table = Get-EntryTable $store
    $translatedUnits = New-Object 'System.Collections.Generic.List[object]'
    $stale = 0
    foreach ($unit in $units) {
        if (-not $table.ContainsKey($unit.Key)) { continue }
        $entry = $table[$unit.Key]
        if ([string]::IsNullOrWhiteSpace(
                [string](Get-OptionalProperty $entry 'target' ''))) { continue }
        if (-not (Test-UnitSourceCurrent $entry $unit)) {
            $stale++
            continue
        }
        $translatedUnits.Add([PSCustomObject]@{ Unit = $unit; Entry = $entry })
    }

    $percent = if ($units.Count -eq 0) { 100 } else {
        [math]::Round(($translatedUnits.Count * 100.0) / $units.Count, 1)
    }
    Write-Host "$TargetLocale progress: $($translatedUnits.Count)/$($units.Count) ($percent%), stale: $stale"
    Write-Host 'By unit kind:'
    foreach ($group in @($units | Group-Object Kind | Sort-Object Name)) {
        $done = @($translatedUnits | Where-Object { $_.Unit.Kind -eq $group.Name }).Count
        Write-Host ("  {0,-28} {1,4}/{2,-4}" -f $group.Name, $done, $group.Count)
    }
    Write-Host 'By review status:'
    if ($translatedUnits.Count -eq 0) {
        Write-Host '  (no translated units)'
    } else {
        foreach ($group in @($translatedUnits | Group-Object {
                    [string](Get-OptionalProperty $_.Entry 'status' '')
                } | Sort-Object Name)) {
            $name = if ([string]::IsNullOrWhiteSpace($group.Name)) { '(unset)' } else { $group.Name }
            Write-Host ("  {0,-28} {1,4}" -f $name, $group.Count)
        }
    }
}

function Invoke-LocalizationSelfTest {
    param([string]$RulesPath)

    $rules = Read-RulesFile $RulesPath
    $units = @(Get-TranslationUnits $rules)
    if ($rules.Header.Count -ne 7) { throw 'Self-test: expected seven columns' }
    if ($rules.Rows.Count -eq 0) { throw 'Self-test: no rules found' }
    if ($units.Count -eq 0) { throw 'Self-test: no translation units found' }

    $duplicateKeys = @($units | Group-Object Key | Where-Object { $_.Count -gt 1 })
    if ($duplicateKeys.Count -gt 0) {
        throw "Self-test: duplicate translation unit key '$($duplicateKeys[0].Name)'"
    }

    $roundTripPath = Resolve-ProjectPath 'build/localization/selftest/rules.csv'
    Write-RulesFile $rules $roundTripPath | Out-Null
    $roundTrip = Read-RulesFile $roundTripPath
    if ($roundTrip.Rows.Count -ne $rules.Rows.Count) {
        throw 'Self-test: CSV round trip changed row count'
    }
    for ($rowIndex = 0; $rowIndex -lt $rules.Rows.Count; $rowIndex++) {
        for ($column = 0; $column -lt 7; $column++) {
            if ($rules.Rows[$rowIndex].Fields[$column] -cne
                    $roundTrip.Rows[$rowIndex].Fields[$column]) {
                throw "Self-test: CSV round trip changed row $rowIndex column $column"
            }
        }
    }

    $sample = 'Value $shipTrophySample and $player.shroudedSubstrateAvailable and %s'
    $sampleTokens = @(Get-StrictTokenSequence $sample)
    if (-not (Test-TokenSequencesEqual `
            -Left $sampleTokens `
            -Right @('$shipTrophySample', '$player.shroudedSubstrateAvailable', '%s'))) {
        throw 'Self-test: protected token extraction failed'
    }
    if (Test-TokenSequencesEqual `
            -Left $sampleTokens `
            -Right @('$shipTrophyChanged', '%s')) {
        throw 'Self-test: protected token mismatch was not detected'
    }

    $glossary = Read-Glossary
    $protectedDraft = Protect-MachineTranslationText `
        'Isa has $shipTrophySample, 12 DP, and says "Hello."' `
        $glossary `
        'zh-Hans-CN' `
        $true
    if ($protectedDraft.Text.Contains('$shipTrophySample') -or
            $protectedDraft.Text.Contains('12')) {
        throw 'Self-test: machine-draft token shielding failed'
    }
    $leftCorner = [string][char]0x300c
    $rightCorner = [string][char]0x300d
    $sampleChinese = (
        [string][char]0x4f60 + [string][char]0x597d + [string][char]0x3002)
    $simulatedChineseQuotes = $protectedDraft.Text.Replace(
        'Hello.',
        $leftCorner + $sampleChinese + $rightCorner)
    $restoredDraft = Restore-MachineTranslationText `
        $simulatedChineseQuotes `
        $protectedDraft.Restorations
    if (-not $restoredDraft.Contains('$shipTrophySample') -or
            -not $restoredDraft.Contains('12') -or
            $restoredDraft.Contains($leftCorner) -or
            $restoredDraft.Contains($rightCorner)) {
        throw 'Self-test: machine-draft restoration or quote normalization failed'
    }
    if ((Get-DeepLApiRoot 'test:fx') -cne 'https://api-free.deepl.com' -or
            (Get-DeepLApiRoot 'test-paid') -cne 'https://api.deepl.com') {
        throw 'Self-test: DeepL endpoint selection failed'
    }
    $traditionalInstructions = @(
        Get-DeepLCustomInstructions 'ZH-HANT' $false)
    if (($traditionalInstructions -join ' ') -notmatch 'Taiwan Mandarin') {
        throw 'Self-test: Traditional draft instructions lost Taiwan locale guidance'
    }

    # Exercise real translation replacement, including the fragile requirement
    # that bracketed text and SetTextHighlights remain byte-for-byte identical.
    $testEntries = @{}
    $plainTextUnit = @($units | Where-Object {
            $_.Key -eq 'shipTrophyIsaCloseIntel::text'
        } | Select-Object -First 1)[0]
    $optionUnit = @($units | Where-Object {
            $_.Kind -eq 'option'
        } | Select-Object -First 1)[0]
    $statusTextUnit = @($units | Where-Object {
            $_.RuleId -eq 'shipTrophyIsaShatteredRingHomecomingInheritance' -and
            $_.Kind -eq 'text'
        } | Select-Object -First 1)[0]
    if ($null -eq $plainTextUnit -or $null -eq $optionUnit -or
            $null -eq $statusTextUnit) {
        throw 'Self-test: expected fixture rules are missing'
    }
    $statusFragments = @(Get-StatusFragments $statusTextUnit.Source)
    if ($statusFragments.Count -lt 1) {
        throw 'Self-test: expected a bracketed runtime status fixture'
    }
    $statusScriptUnit = @($units | Where-Object {
            $_.RuleId -eq $statusTextUnit.RuleId -and
            $_.Kind -eq 'script.SetTextHighlights' -and
            $_.Source -ceq $statusFragments[0]
        } | Select-Object -First 1)[0]
    if ($null -eq $statusScriptUnit) {
        throw 'Self-test: matching SetTextHighlights unit is missing'
    }

    $localizedStatus = '[LOCALIZED STATUS FOR SELF-TEST]'
    $testEntries[$plainTextUnit.Key] = [PSCustomObject]@{
        target = 'LOCALIZED TEXT FOR SELF-TEST.'
    }
    $testEntries[$optionUnit.Key] = [PSCustomObject]@{
        target = 'LOCALIZED OPTION FOR SELF-TEST.'
    }
    $testEntries[$statusTextUnit.Key] = [PSCustomObject]@{
        target = $statusTextUnit.Source.Replace($statusFragments[0], $localizedStatus)
    }
    $testEntries[$statusScriptUnit.Key] = [PSCustomObject]@{
        target = $localizedStatus
    }
    $localizedRules = Apply-TranslationsToRules $rules $testEntries $true
    $localizedPath = Resolve-ProjectPath 'build/localization/selftest/localized-rules.csv'
    Write-RulesFile $localizedRules $localizedPath | Out-Null
    $localizedRoundTrip = Read-RulesFile $localizedPath
    $parityErrors = @(Test-BuiltRulesParity $rules $localizedRoundTrip)
    if ($parityErrors.Count -gt 0) {
        throw "Self-test: translated rules parity failed: $($parityErrors -join '; ')"
    }
    Invoke-RulesValidator $localizedPath

    # Exercise JSON batch import and normalized translation-store persistence in
    # an ignored build directory, leaving the real locale stores untouched.
    $previousStoreOverride = $script:translationStoreOverride
    try {
        $temporaryStorePath = Resolve-ProjectPath 'build/localization/selftest/translation-store.json'
        $script:translationStoreOverride = $temporaryStorePath
        Write-JsonFile -Path $temporaryStorePath -Value ([ordered]@{
                schemaVersion = 1
                locale = 'zh-Hans-CN'
                sourceLocale = 'en'
                entries = @()
            })
        $temporaryBatchPath = Resolve-ProjectPath 'build/localization/selftest/import-batch.json'
        Write-JsonFile -Path $temporaryBatchPath -Value ([ordered]@{
                schemaVersion = 1
                locale = 'zh-Hans-CN'
                sourceLocale = 'en'
                entries = @([ordered]@{
                        key = $plainTextUnit.Key
                        sourceHash = $plainTextUnit.SourceHash
                        source = $plainTextUnit.Source
                        target = 'LOCALIZED IMPORT SELF-TEST.'
                        backTranslation = $plainTextUnit.Source
                        status = 'machine-reviewed'
                        confidence = 'high'
                        issues = @()
                        reviewNotes = 'Synthetic import test.'
                        traditionalArtFragments = @()
                        artNote = ''
                        allowedLatin = @('LOCALIZED', 'IMPORT', 'SELF', 'TEST')
                        allowVariant = $false
                    })
            })
        Import-TranslationBatch 'zh-Hans-CN' $rules.Path $temporaryBatchPath
        $importedStore = Read-TranslationStore 'zh-Hans-CN'
        $importedTable = Get-EntryTable $importedStore
        if (-not $importedTable.ContainsKey($plainTextUnit.Key) -or
                [string](Get-OptionalProperty $importedTable[$plainTextUnit.Key] 'target' '') -cne
                'LOCALIZED IMPORT SELF-TEST.') {
            throw 'Self-test: batch import did not persist the translated target'
        }
        $temporaryCheck = Invoke-TranslationCheck 'zh-Hans-CN' $rules.Path $false
        if ($temporaryCheck.Errors -ne 0 -or $temporaryCheck.Translated -ne 1) {
            throw 'Self-test: imported translation did not pass the QA pipeline'
        }
    } finally {
        $script:translationStoreOverride = $previousStoreOverride
    }

    $selfTestIndexPath = Resolve-ProjectPath 'build/localization/selftest/reference-index.json'
    $selfTestIndex = New-ReferenceIndex $rules.Path $selfTestIndexPath
    if ([int](Get-OptionalProperty $selfTestIndex 'documentCount' 0) -ne
            $units.Count) {
        throw 'Self-test: reference index did not include every translation unit'
    }
    $duplicateSourceDocument = @(
        (Get-OptionalProperty $selfTestIndex 'documents' @()) |
        Where-Object {
            [string](Get-OptionalProperty $_ 'key' '') -ceq
            $plainTextUnit.Key
        } | Select-Object -First 1)[0]
    $duplicateMatches = @(Find-ReferenceMatches `
        -Index $selfTestIndex `
        -QueryVector (Get-DocumentVector $duplicateSourceDocument) `
        -TargetLocale 'zh-Hans-CN' `
        -Limit 5 `
        -ExcludeKey $plainTextUnit.Key `
        -ExcludeRuleId $plainTextUnit.RuleId)
    if (@($duplicateMatches | Where-Object {
                [string](Get-OptionalProperty $_.Document 'source' '') -ceq
                $plainTextUnit.Source
            }).Count -lt 1) {
        throw 'Self-test: vector retrieval missed a repeated dialogue line'
    }
    $hallQuery = New-QueryVector 'Hall of Triumph ledgers' $selfTestIndex
    $hallMatches = @(Find-ReferenceMatches `
        -Index $selfTestIndex `
        -QueryVector $hallQuery `
        -TargetLocale 'zh-Hans-CN' `
        -Limit 5)
    if (@($hallMatches | Where-Object {
                [string](Get-OptionalProperty $_.Document 'source' '') -match
                'Hall of Triumph|Hall ledgers'
            }).Count -lt 1) {
        throw 'Self-test: vector retrieval missed the Hall ledger terminology'
    }

    Invoke-RulesValidator $rules.Path
    Write-Host (
        "Localization self-test passed: $($rules.Rows.Count) rules, " +
        "$($units.Count) translation units, lossless multiline CSV round trip")
}

$resolvedSource = Resolve-ProjectPath $SourcePath
switch ($Command) {
    'Export' {
        Export-TranslationBatch `
            $Locale `
            $resolvedSource `
            $BatchPath `
            $ReferenceIndexPath `
            $BatchSize `
            $Offset `
            ([bool]$IncludeTranslated)
    }
    'Import' {
        Import-TranslationBatch $Locale $resolvedSource $BatchPath
    }
    'Draft' {
        New-MachineDrafts `
            $Locale `
            $resolvedSource `
            $BatchSize `
            $Offset `
            ([bool]$IncludeTranslated) `
            ([bool]$BackTranslate)
    }
    'Quota' {
        Show-DeepLUsage
    }
    'Check' {
        $mustBeComplete = [bool]$RequireComplete
        $result = Invoke-TranslationCheck $Locale $resolvedSource $mustBeComplete
        if ($result.Errors -gt 0) {
            throw "$Locale localization check failed with $($result.Errors) error(s)"
        }
    }
    'Build' {
        Build-LocalizedRules $Locale $resolvedSource $OutputPath ([bool]$AllowIncomplete)
    }
    'Report' {
        Show-TranslationReport $Locale $resolvedSource
    }
    'Index' {
        New-ReferenceIndex $resolvedSource $ReferenceIndexPath | Out-Null
    }
    'Search' {
        Show-ReferenceSearch `
            $resolvedSource `
            $ReferenceIndexPath `
            $Locale `
            $Query `
            $UnitKey `
            $TopK
    }
    'SelfTest' {
        Invoke-LocalizationSelfTest $resolvedSource
    }
}
