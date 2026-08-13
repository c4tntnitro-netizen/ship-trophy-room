[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('Report', 'Check', 'Build')]
    [string]$Command,

    [ValidateSet('zh-Hans-CN', 'zh-Hant-TW')]
    [string]$Locale = 'zh-Hans-CN',

    [string]$OutputRoot = 'build/localization'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$ProjectRoot = Split-Path -Parent $PSScriptRoot
$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)

function Resolve-ProjectPath {
    param([Parameter(Mandatory = $true)][string]$Path)
    if ([IO.Path]::IsPathRooted($Path)) {
        return [IO.Path]::GetFullPath($Path)
    }
    return [IO.Path]::GetFullPath((Join-Path $ProjectRoot $Path))
}

function Read-Utf8Text {
    param([Parameter(Mandatory = $true)][string]$Path)
    return [IO.File]::ReadAllText((Resolve-ProjectPath $Path), [Text.Encoding]::UTF8)
}

function Write-Utf8Text {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][AllowEmptyString()][string]$Text
    )
    $absolute = Resolve-ProjectPath $Path
    $parent = Split-Path -Parent $absolute
    if (-not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    [IO.File]::WriteAllText($absolute, $Text, $Utf8NoBom)
}

function Read-Store {
    $path = Resolve-ProjectPath "localization/content/translations/$Locale.json"
    if (-not (Test-Path -LiteralPath $path)) {
        throw "Missing content localization store: $path"
    }
    $store = [IO.File]::ReadAllText($path, [Text.Encoding]::UTF8) | ConvertFrom-Json
    if ($store.schemaVersion -ne 1) {
        throw "Unsupported content localization schema in $path"
    }
    if ($store.locale -ne $Locale) {
        throw "Store locale '$($store.locale)' does not match requested locale '$Locale'."
    }
    return $store
}

function Normalize-Newlines {
    param([AllowNull()][string]$Text)
    if ($null -eq $Text) { return '' }
    return $Text.Replace("`r`n", "`n").Replace("`r", "`n")
}

function Get-ProtectedTokens {
    param([AllowNull()][string]$Text)
    if ($null -eq $Text) { return @() }
    $matches = [regex]::Matches(
        $Text,
        '(?<!%)%(?:\d+\$)?[-#+0,(<]*\d*(?:\.\d+)?[a-zA-Z]|\$[A-Za-z_][A-Za-z0-9_]*')
    return @($matches | ForEach-Object { $_.Value } | Sort-Object)
}

function Test-TargetPair {
    param(
        [Parameter(Mandatory = $true)][string]$Key,
        [AllowEmptyString()][string]$Source,
        [AllowEmptyString()][string]$Target,
        [System.Collections.Generic.List[string]]$Errors,
        [System.Collections.Generic.List[string]]$Warnings
    )
    if ([string]::IsNullOrWhiteSpace($Target)) {
        $Errors.Add("$Key has an empty target.")
        return
    }
    if ($Target.IndexOf([char]0x201C) -ge 0 -or $Target.IndexOf([char]0x201D) -ge 0) {
        $Errors.Add("$Key contains forbidden typographic double quotation marks.")
    }
    if ($Target.IndexOf([char]0xFFFD) -ge 0) {
        $Errors.Add("$Key contains the Unicode replacement character.")
    }
    $sourceTokens = @(Get-ProtectedTokens $Source)
    $targetTokens = @(Get-ProtectedTokens $Target)
    if (($sourceTokens -join "`n") -ne ($targetTokens -join "`n")) {
        $Errors.Add(
            "$Key changes protected format/runtime tokens: " +
            "source=[$($sourceTokens -join ', ')] target=[$($targetTokens -join ', ')].")
    }
    if ($Source -ceq $Target -and $Source -match '[A-Za-z]{4}') {
        $Warnings.Add("$Key is unchanged English; verify that it is an intentional proper name.")
    }
}

function Read-CsvMatrix {
    param([Parameter(Mandatory = $true)][string]$Path)
    Add-Type -AssemblyName Microsoft.VisualBasic
    $absolute = Resolve-ProjectPath $Path
    $parser = New-Object Microsoft.VisualBasic.FileIO.TextFieldParser($absolute, [Text.Encoding]::UTF8)
    try {
        $parser.TextFieldType = [Microsoft.VisualBasic.FileIO.FieldType]::Delimited
        $parser.SetDelimiters(',')
        $parser.HasFieldsEnclosedInQuotes = $true
        $rows = New-Object System.Collections.Generic.List[object]
        while (-not $parser.EndOfData) {
            $rows.Add([pscustomobject]@{ Fields = [string[]]$parser.ReadFields() })
        }
        return $rows.ToArray()
    } finally {
        $parser.Close()
    }
}

function ConvertTo-CsvField {
    param([AllowNull()][string]$Value)
    if ($null -eq $Value) { $Value = '' }
    if ($Value.IndexOfAny(@([char]',', [char]'"', [char]10, [char]13)) -ge 0) {
        return '"' + $Value.Replace('"', '""') + '"'
    }
    return $Value
}

function Convert-CsvMatrixToText {
    param([Parameter(Mandatory = $true)][object[]]$Rows)
    $lines = foreach ($rowRecord in $Rows) {
        (@($rowRecord.Fields) | ForEach-Object { ConvertTo-CsvField ([string]$_) }) -join ','
    }
    return ($lines -join "`r`n") + "`r`n"
}

function Get-HeaderIndex {
    param(
        [Parameter(Mandatory = $true)][object[]]$Header,
        [Parameter(Mandatory = $true)][string]$Name
    )
    for ($i = 0; $i -lt $Header.Count; $i++) {
        if ([string]$Header[$i] -ceq $Name) { return $i }
    }
    return -1
}

function Apply-CsvEdits {
    param(
        [Parameter(Mandatory = $true)][object]$FileSpec,
        [Parameter(Mandatory = $true)][string]$Destination,
        [System.Collections.Generic.List[string]]$Errors,
        [System.Collections.Generic.List[string]]$Warnings
    )
    $rows = @(Read-CsvMatrix ([string]$FileSpec.path))
    if ($rows.Count -lt 1) {
        $Errors.Add("$($FileSpec.path) is empty.")
        return
    }
    $header = @($rows[0].Fields)
    foreach ($edit in @($FileSpec.edits)) {
        $keyColumnIndex = Get-HeaderIndex $header ([string]$edit.keyColumn)
        if ($keyColumnIndex -lt 0) {
            $Errors.Add("$($FileSpec.path) has no '$($edit.keyColumn)' column.")
            continue
        }
        $matches = New-Object System.Collections.Generic.List[int]
        for ($rowIndex = 1; $rowIndex -lt $rows.Count; $rowIndex++) {
            $row = @($rows[$rowIndex].Fields)
            if ($keyColumnIndex -lt $row.Count -and
                    [string]$row[$keyColumnIndex] -ceq [string]$edit.key) {
                $matches.Add($rowIndex)
            }
        }
        $editKey = "$($FileSpec.path)::$($edit.keyColumn)=$($edit.key)"
        if ($matches.Count -ne 1) {
            $Errors.Add("$editKey matched $($matches.Count) rows; expected exactly one.")
            continue
        }
        $rowIndex = $matches[0]
        $row = @($rows[$rowIndex].Fields)
        foreach ($field in @($edit.fields.PSObject.Properties)) {
            $columnIndex = Get-HeaderIndex $header $field.Name
            if ($columnIndex -lt 0) {
                $Errors.Add("$editKey has no '$($field.Name)' column.")
                continue
            }
            while ($row.Count -le $columnIndex) { $row += '' }
            $source = [string]$field.Value.source
            $target = [string]$field.Value.target
            $fieldKey = "$editKey::$($field.Name)"
            Test-TargetPair $fieldKey $source $target $Errors $Warnings
            if ((Normalize-Newlines ([string]$row[$columnIndex])) -cne
                    (Normalize-Newlines $source)) {
                $Errors.Add("$fieldKey source drifted from the English file.")
                continue
            }
            $row[$columnIndex] = $target
        }
        $rows[$rowIndex].Fields = [string[]]$row
    }
    if ($Errors.Count -eq 0) {
        Write-Utf8Text $Destination (Convert-CsvMatrixToText $rows)
    }
}

function Apply-TextEdits {
    param(
        [Parameter(Mandatory = $true)][object]$FileSpec,
        [Parameter(Mandatory = $true)][string]$Destination,
        [System.Collections.Generic.List[string]]$Errors,
        [System.Collections.Generic.List[string]]$Warnings
    )
    $text = Read-Utf8Text ([string]$FileSpec.path)
    foreach ($edit in @($FileSpec.edits)) {
        $source = [string]$edit.source
        $target = [string]$edit.target
        $editKey = "$($FileSpec.path)::$($edit.key)"
        Test-TargetPair $editKey $source $target $Errors $Warnings
        $expectedProperty = $edit.PSObject.Properties['expectedCount']
        $expected = if ($null -ne $expectedProperty) {
            [int]$expectedProperty.Value
        } else {
            1
        }
        $count = 0
        $offset = 0
        while ($true) {
            $found = $text.IndexOf($source, $offset, [StringComparison]::Ordinal)
            if ($found -lt 0) { break }
            $count++
            $offset = $found + $source.Length
        }
        if ($count -ne $expected) {
            $Errors.Add("$editKey matched $count source fragments; expected $expected.")
            continue
        }
        $text = $text.Replace($source, $target)
    }
    if ($Errors.Count -eq 0) {
        Write-Utf8Text $Destination $text
    }
}

function Apply-LocalizedStringsFile {
    param(
        [Parameter(Mandatory = $true)][object]$FileSpec,
        [Parameter(Mandatory = $true)][string]$Destination,
        [System.Collections.Generic.List[string]]$Errors,
        [System.Collections.Generic.List[string]]$Warnings
    )
    $sourcePath = [string]$FileSpec.path
    $translationPath = [string]$FileSpec.translation
    $sourceText = Read-Utf8Text $sourcePath
    $translationText = Read-Utf8Text $translationPath
    try {
        $sourceJson = $sourceText | ConvertFrom-Json
        $translationJson = $translationText | ConvertFrom-Json
    } catch {
        $Errors.Add("$translationPath is not valid JSON: $($_.Exception.Message)")
        return
    }

    $sourceCategories = @($sourceJson.PSObject.Properties)
    $translationCategories = @($translationJson.PSObject.Properties)
    foreach ($category in $sourceCategories) {
        $translatedCategory = $translationJson.PSObject.Properties[$category.Name]
        if ($null -eq $translatedCategory) {
            $Errors.Add("$translationPath is missing category '$($category.Name)'.")
            continue
        }
        foreach ($entry in @($category.Value.PSObject.Properties)) {
            $translatedEntry = $translatedCategory.Value.PSObject.Properties[$entry.Name]
            $entryKey = "$sourcePath::$($category.Name).$($entry.Name)"
            if ($null -eq $translatedEntry) {
                $Errors.Add("$translationPath is missing '$($category.Name).$($entry.Name)'.")
                continue
            }
            Test-TargetPair $entryKey ([string]$entry.Value) `
                ([string]$translatedEntry.Value) $Errors $Warnings
        }
        foreach ($translatedEntry in @($translatedCategory.Value.PSObject.Properties)) {
            if ($null -eq $category.Value.PSObject.Properties[$translatedEntry.Name]) {
                $Errors.Add("$translationPath has unexpected key '$($category.Name).$($translatedEntry.Name)'.")
            }
        }
    }
    foreach ($translatedCategory in $translationCategories) {
        if ($null -eq $sourceJson.PSObject.Properties[$translatedCategory.Name]) {
            $Errors.Add("$translationPath has unexpected category '$($translatedCategory.Name)'.")
        }
    }
    if ($Errors.Count -eq 0) {
        Write-Utf8Text $Destination $translationText
    }
}

function Invoke-ContentProcessing {
    param([switch]$WriteOutput)
    $store = Read-Store
    $errors = New-Object System.Collections.Generic.List[string]
    $warnings = New-Object System.Collections.Generic.List[string]
    $outputBase = Join-Path $OutputRoot $Locale

    foreach ($fileSpec in @($store.csvFiles)) {
        $destination = if ($WriteOutput) {
            Join-Path $outputBase ([string]$fileSpec.path)
        } else {
            Join-Path "build/localization/check/$Locale" ([string]$fileSpec.path)
        }
        Apply-CsvEdits $fileSpec $destination $errors $warnings
    }
    foreach ($fileSpec in @($store.textFiles)) {
        $destination = if ($WriteOutput) {
            Join-Path $outputBase ([string]$fileSpec.path)
        } else {
            Join-Path "build/localization/check/$Locale" ([string]$fileSpec.path)
        }
        Apply-TextEdits $fileSpec $destination $errors $warnings
    }
    $localizedFilesProperty = $store.PSObject.Properties['localizedFiles']
    if ($null -ne $localizedFilesProperty) {
        foreach ($fileSpec in @($localizedFilesProperty.Value)) {
            $destination = if ($WriteOutput) {
                Join-Path $outputBase ([string]$fileSpec.path)
            } else {
                Join-Path "build/localization/check/$Locale" ([string]$fileSpec.path)
            }
            Apply-LocalizedStringsFile $fileSpec $destination $errors $warnings
        }
    }

    return [pscustomobject]@{
        Store = $store
        Errors = $errors
        Warnings = $warnings
        OutputBase = Resolve-ProjectPath $outputBase
    }
}

function Write-Result {
    param([Parameter(Mandatory = $true)][object]$Result)
    foreach ($warning in $Result.Warnings) { Write-Warning $warning }
    foreach ($error in $Result.Errors) { Write-Error $error }
    $csvCount = @($Result.Store.csvFiles | ForEach-Object { @($_.edits).Count } |
            Measure-Object -Sum).Sum
    $textCount = @($Result.Store.textFiles | ForEach-Object { @($_.edits).Count } |
            Measure-Object -Sum).Sum
    $localizedFileProperty = $Result.Store.PSObject.Properties['localizedFiles']
    $localizedFileCount = if ($null -eq $localizedFileProperty) {
        0
    } else {
        @($localizedFileProperty.Value).Count
    }
    Write-Host (
        "$Locale content localization: $csvCount CSV rows, $textCount text replacements, " +
        "$localizedFileCount localized strings file(s), " +
        "$($Result.Errors.Count) error(s), $($Result.Warnings.Count) warning(s).")
}

switch ($Command) {
    'Report' {
        $store = Read-Store
        $csvCount = @($store.csvFiles | ForEach-Object { @($_.edits).Count } |
                Measure-Object -Sum).Sum
        $fieldCount = @($store.csvFiles | ForEach-Object {
                    foreach ($edit in @($_.edits)) { @($edit.fields.PSObject.Properties).Count }
                } | Measure-Object -Sum).Sum
        $textCount = @($store.textFiles | ForEach-Object { @($_.edits).Count } |
                Measure-Object -Sum).Sum
        $localizedFileProperty = $store.PSObject.Properties['localizedFiles']
        $localizedFileCount = if ($null -eq $localizedFileProperty) {
            0
        } else {
            @($localizedFileProperty.Value).Count
        }
        Write-Host "$Locale content store: $csvCount CSV rows / $fieldCount fields, $textCount text replacements, $localizedFileCount localized strings file(s)."
    }
    'Check' {
        $result = Invoke-ContentProcessing
        Write-Result $result
        if ($result.Errors.Count -gt 0) { exit 1 }
    }
    'Build' {
        $result = Invoke-ContentProcessing -WriteOutput
        Write-Result $result
        if ($result.Errors.Count -gt 0) { exit 1 }

        $rulesOutput = Join-Path $result.OutputBase 'data/campaign/rules.csv'
        & (Join-Path $PSScriptRoot 'rules_localization.ps1') `
            -Command Build -Locale $Locale -OutputPath $rulesOutput
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        Write-Host "Built Chinese content overlay: $($result.OutputBase)"
    }
}
