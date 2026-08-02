param(
    [string]$QuestPath = "dialogue/gan_eden_quest.ink",
    [string]$LogsPath = "dialogue/Logs.ink",
    [string]$OutputPath = "dialogue/gan_eden_master.ink"
)

$utf8 = New-Object System.Text.UTF8Encoding($false)

function Read-Utf8([string]$Path) {
    return [System.IO.File]::ReadAllText(
        (Resolve-Path -LiteralPath $Path), $utf8)
}

function Get-FromMarker(
        [string]$Text, [string]$Marker, [string]$SourceName) {
    $startAt = $Text.IndexOf(
        $Marker, [System.StringComparison]::Ordinal)
    if ($startAt -lt 0) {
        throw "Missing marker '$Marker' in $SourceName"
    }
    return $Text.Substring($startAt).Trim()
}

$quest = Read-Utf8 $QuestPath
$logs = Read-Utf8 $LogsPath
$questBody = Get-FromMarker $quest "=== gan_eden_quest ===" $QuestPath
$logsBody = Get-FromMarker $logs "=== one ===" $LogsPath

# Let the proofreading preview continue from the second hypershunt to the
# Gate, and from the final log to the epilogue. The focused source files keep
# their runtime-friendly END diverts.
$questEnding = "+ [Set a course.] -> END"
$questEndingAt = $questBody.LastIndexOf(
    $questEnding, [System.StringComparison]::Ordinal)
if ($questEndingAt -ge 0) {
    $questBody = $questBody.Remove(
        $questEndingAt, $questEnding.Length).Insert(
        $questEndingAt, "+ [Set a course.] -> master_power_transit_gate")
}

$finalEnd = "-> END"
$finalEndAt = $logsBody.LastIndexOf(
    $finalEnd, [System.StringComparison]::Ordinal)
if ($finalEndAt -ge 0) {
    $logsBody = $logsBody.Remove(
        $finalEndAt, $finalEnd.Length).Insert(
        $finalEndAt, "-> master_epilogue")
}

$header = @'
// Hall of Triumph - A Borrowed Name / Gan Eden Quest Master
//
// Standalone proofreading and editing copy of the complete quest line.
// Runtime dialogue remains implemented by data/campaign/rules.csv and Java.
// Focused source copies remain in gan_eden_quest.ink, hypershunt.ink, and
// Logs.ink. Run tools/build_gan_eden_master.ps1 after changing those files.
//
// Runtime order:
// 1. Complete At the Gates (or use Nexerelin's Galatia-story skip), recruit
//    Isa as an officer, and return with her to the Shattered Ring.
// 2. Recover Personal Log 1765 from the identification wafer in Isa's suit.
// 3. Investigate both Coronal Hypershunts and recover Epitaph Parts II-III.
// 4. Enter POWER TRANSIT GATE - GAN EDEN and recover Part IV at Tree of Life.
// 5. Defeat Cherubim and Lahat Haharev, opening Gan Eden to hyperspace.
// 6. Approach the Space Elevator with Isa and recover Epitaph Final.

VAR hypershunts_reactivated = 0
VAR gan_eden_revealed = false
VAR grave_found = false
VAR golden_shards_defeated = false
VAR player_gender = "him"
VAR player_title = "Captain"
VAR player_self = "himself"

-> gan_eden_master

=== gan_eden_master ===
Complete Gan Eden quest master

+ [Read from the Shattered Ring homecoming.] -> station_shattered_ring
+ [Review the interactive quest-source index.] -> gan_eden_quest
+ [Review the Power Transit Gate and Gan Eden arrival.] -> master_power_transit_gate
+ [Review the Tree of Life recovery.] -> master_tree_of_life
+ [Review the Golden Omega confrontation.] -> master_golden_omega
+ [Review the post-battle opening of Gan Eden.] -> master_post_battle
+ [Review the Space Elevator ending.] -> master_space_elevator
+ [Archive: Personal Log 1765 / Part I.] -> one
+ [Archive: Epitaph Part II.] -> two
+ [Archive: Epitaph Part III.] -> three
+ [Archive: Epitaph Part IV.] -> four
+ [Archive: Epitaph Final.] -> five
+ [End preview.] -> END


// ============================================================
// CURRENT INTERACTIVE QUEST SOURCE
// ============================================================
'@

$runtimeBridges = @'


// ============================================================
// POWER TRANSIT GATE AND GAN EDEN RUNTIME BRIDGES
// These beats are driven by campaign scripts rather than long rules.csv trees.
// ============================================================

=== master_power_transit_gate ===
// Runtime interaction: shipTrophyGanEdenExternalRing

POWER TRANSIT GATE - GAN EDEN hangs alone at the center of an empty, starless system. Its adamantine surface is awake. Within the aperture, impossible depth folds toward the sealed world beyond.

Around it drifts a silent graveyard of damaged Coronal Hypershunts and ruined Gate Haulers. None answer the fleet's approach.

+ [Enter the Power Transit Gate.] -> master_gan_eden_arrival
+ [Leave.] -> END


=== master_gan_eden_arrival ===
// Runtime transition: GanEdenQuestCMD transitIn

The Transit Ring releases the fleet beneath Gan Eden's impossible inward horizon: a living world wrapped around a warm central sun. Four dormant settlement districts remain anchored to the shell. The Tree of Life still carries a surviving Leicester continuity record.

Beyond it, the Gan Eden Space Elevator remains sealed behind an Omega interdiction field.

+ [Approach the Tree of Life.] -> master_tree_of_life
+ [Review the Golden Omega guardians.] -> master_golden_omega
+ [Return through the Eden Transit Ring.] -> master_internal_ring


=== master_internal_ring ===
// Runtime interaction: shipTrophyGanEdenInternalRing

The Eden Transit Ring frames a narrow wound in the sealed world's geometry. Its connection remains synchronized with POWER TRANSIT GATE - GAN EDEN.

+ [Traverse the Gate.] -> END
+ [Remain in Gan Eden.] -> master_gan_eden_arrival


=== master_tree_of_life ===
// Runtime interaction: shipTrophyGanEdenSurfaceLog

A sealed municipal archive beneath Tree of Life answers the Leicester continuity credentials. One surviving personal record is available for recovery.

+ [Recover Epitaph - Part IV.] -> four
+ [Leave the archive sealed.] -> master_gan_eden_arrival


=== master_golden_omega ===
// Runtime encounter: GanEdenAmbushScript and GoldenFractalCascade

Two golden Omega Shards hold the approach to the Space Elevator: Cherubim and Lahat Haharev. Neither they nor any of their descendants will retreat.

The Shards divide under fire into Facets and Aspect wings. The Facets divide again into Tesseracts and further Aspect wings. Only the final Tesseracts die without reproducing.

Defeating only one named Shard is not enough. The survivor reconstructs its counterpart. Both must be destroyed in the same complete victory.

+ [Engage Cherubim and Lahat Haharev.] -> master_post_battle
+ [Break off.] -> master_gan_eden_arrival


=== master_post_battle ===
// Runtime resolution: first complete Golden Omega victory

With Cherubim and Lahat Haharev destroyed together, the Space Elevator's interdiction field falls silent.

A conventional hyperspace jump point stabilizes beside Gan Eden. The four settlement districts are released from their sealed economy groups and can participate in ordinary Sector trade.

The victory is not permanent. Every ninety days, the Golden Shards reconstruct themselves with a larger escort of ivory Remnant hulls, escalating until the escort is roughly a full Ordo. Later victories reset that cycle without sealing Gan Eden again.

+ [Approach the Gan Eden Space Elevator with Isa.] -> master_space_elevator
+ [Remain in Gan Eden.] -> master_gan_eden_arrival


=== master_space_elevator ===
// Runtime interaction: shipTrophyGanEdenEpitaph

With Cherubim and Lahat Haharev gone, the elevator's interdiction field is silent.

Isa's inherited suit transponder opens the outer doors. The lift descends through kilometers of dead infrastructure before stopping at a continuity-office archive sealed away from the inhabited surface.

Four recovered records authenticate in sequence. A fifth file unlocks beneath them:

EPITAPH - FINAL

+ [Open the final entry with Isa.] -> five
+ [Leave.] -> END


=== master_epilogue ===
// Runtime resolution after Epitaph Final

The final log ends.

The elevator observation deck looks out across the impossible inward horizon. Isa says nothing for a long time.

Then she begins telling Isaac Thomas Leicester about the Shattered Ring: the wreck-farms, the revival ward, the terrible food, and every impossible ship that carried her farther than he could have imagined.

No answer comes from the empty world. This time, she does not seem to need one.

+ [Return to the fleet.] -> END


// ============================================================
// CANONICAL RECOVERED LOG ARCHIVE
// Loaded at runtime from dialogue/Logs.ink.
// ============================================================
'@

$footer = @'


// ============================================================
// END OF GAN EDEN QUEST MASTER
// ============================================================
'@

$output = @(
    $header.Trim(),
    $questBody,
    $runtimeBridges.Trim(),
    $logsBody,
    $footer.Trim()
) -join "`r`n`r`n"

$fullOutputPath = [System.IO.Path]::GetFullPath(
    (Join-Path (Get-Location) $OutputPath))
[System.IO.File]::WriteAllText($fullOutputPath, $output + "`r`n", $utf8)
Write-Output "Wrote $fullOutputPath"
