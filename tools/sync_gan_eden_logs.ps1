param(
    [string]$QuestPath = "dialogue/gan_eden_quest.ink",
    [string]$LogsPath = "dialogue/Logs.ink"
)

$utf8 = New-Object System.Text.UTF8Encoding($false)
$quest = [System.IO.File]::ReadAllText(
    (Resolve-Path -LiteralPath $QuestPath), $utf8)
$logs = [System.IO.File]::ReadAllText(
    (Resolve-Path -LiteralPath $LogsPath), $utf8)

function Get-Between([string]$Text, [string]$Start, [string]$End) {
    $startAt = $Text.IndexOf($Start, [System.StringComparison]::Ordinal)
    if ($startAt -lt 0) { throw "Missing start marker: $Start" }
    $startAt += $Start.Length
    $endAt = $Text.IndexOf($End, $startAt, [System.StringComparison]::Ordinal)
    if ($endAt -lt 0) { throw "Missing end marker: $End" }
    return $Text.Substring($startAt, $endAt - $startAt).Trim()
}

$one = Get-Between $quest "=== station_shattered_id ===" "-> one_isa"
$personalStart = $one.IndexOf("PERSONAL LOG 1765", [System.StringComparison]::Ordinal)
if ($personalStart -lt 0) { throw "Missing Personal Log 1765 header" }
$one = $one.Substring($personalStart).Trim()
$two = Get-Between $quest "=== log_two ===" "-> log_two_isa"
$three = Get-Between $quest "=== log_three ===" "-> log_three_isa"
$four = Get-Between $logs "=== four ===" "=== five ==="
$fiveStart = $logs.IndexOf("=== five ===", [System.StringComparison]::Ordinal)
if ($fiveStart -lt 0) { throw "Missing existing final log" }
$five = $logs.Substring($fiveStart + "=== five ===".Length).Trim()

$output = @"
// Runtime recovery map:
//
// Spacer-suit identification wafer: Personal Log 1765 / Part I.
// First hypershunt scanned: Log Part II.
// Second hypershunt scanned: Log Part III and the Power Transit Gate.
// Tree of Life: Log Part IV.
// Space Elevator: Log Final after the Golden Shards are defeated.
//
// Every recovered entry is retained under the Gan Eden Archives Intel tag.

+ [one] -> one
+ [two] -> two
+ [three] -> three
+ [four] -> four
+ [five] -> five
+ [End preview] -> END


=== one ===

$one

-> END


=== two ===

$two

-> END


=== three ===

$three

-> END


=== four ===

$four
=== five ===

$five
"@

[System.IO.File]::WriteAllText(
    (Resolve-Path -LiteralPath $LogsPath), $output.TrimStart(), $utf8)
