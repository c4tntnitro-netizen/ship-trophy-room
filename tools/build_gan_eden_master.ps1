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
// 1. Recruit Isa as an officer and return with her to the Shattered Ring.
// 2. Recover Personal Log 1765 from the identification wafer in Isa's suit.
// 3. Investigate both Coronal Hypershunts and recover Epitaph Parts II-III.
// 4. Defeat the Ivory Custodians at POWER TRANSIT GATE - GAN EDEN.
// 5. Enter Gan Eden and recover Part IV at Tree of Life.
// 6. Defeat Cherubim and Lahat Haharev, releasing Gan Eden's districts.
// 7. Approach the Space Elevator with Isa and recover Epitaph Final.

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
+ [Review Isa's post-quest conversations.] -> master_postquest_talk
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

Then cool-white drive signatures ignite among the wrecks. An Ivory Remnant fleet unfolds from the graveyard and accelerates to intercept.

+ [Meet the Ivory interception.] -> master_ivory_ambush
+ [Leave.] -> END


=== master_ivory_ambush ===
// Runtime encounter: GanEdenTransitAmbushManager

The Ivory Custodians carry the familiar geometry of Remnant warships beneath pale ceramic superstructures. Blue-green light shows through the white reconstruction where the old machines remain underneath.

They issue no demand and accept no hail. Their formation closes around the active Gate.

+ [Destroy the Custodians.] -> master_power_transit_gate_cleared
+ [Retreat.] -> END


=== master_power_transit_gate_cleared ===

The last Ivory signal breaks apart. Surviving contacts vanish with it, leaving the approach to the Power Transit Gate clear.

+ [Enter the Power Transit Gate.] -> master_gan_eden_arrival
+ [Leave.] -> END


=== master_gan_eden_arrival ===
// Runtime cinematic: GanEdenArrivalDialogPlugin

The Power Transit Gate closes behind the fleet. Gan Eden curves above and around you: oceans, mountain ranges, and cloud systems climbing the inside of an impossible world.

Isa's slate erupts in warnings. She silences them one by one, then freezes over a surviving emergency channel.

"Active distress beacon," she says. Her voice rises with excitement before catching on the last word. "Human format. It's pointing to a place called the Tree of Life."

She sends the coordinates to navigation, smiles, and immediately checks them again. "Someone might still be here. Or something they left for us."

+ [Approach the Tree of Life.] -> master_tree_of_life
+ [Test the approach to the Space Elevator.] -> master_elevator_repelled
+ [Return through the Eden Transit Ring.] -> master_internal_ring


=== master_elevator_repelled ===

The Space Elevator rises from the inner surface into the atmosphere above Gan Eden. Its upper terminus remains dark, but the structure itself is intact.

Your fleet begins a cautious approach.

Two strange Omega Shards emerge from the atmospheric glare. They cross the approach corridor without hailing, their overlapping drive fields building a wall of impossible vectors ahead of you.

Every attempt to advance turns into lateral acceleration. Dampers scream. Navigation gives ground before the fleet is thrown bodily into the elevator's outer superstructure.

The Shards hold until you retreat, then disappear back into the curve of the world.

+ [Withdraw and follow the distress beacon.] -> master_tree_of_life


=== master_internal_ring ===
// Runtime interaction: shipTrophyGanEdenInternalRing

The Eden Transit Ring frames a narrow wound in the sealed world's geometry. Its connection remains synchronized with POWER TRANSIT GATE - GAN EDEN.

+ [Traverse the Gate.] -> END
+ [Remain in Gan Eden.] -> master_gan_eden_arrival


=== master_tree_of_life ===
// Runtime interaction: shipTrophyGanEdenSurfaceLog

A sealed municipal archive beneath Tree of Life answers the Leicester continuity credentials. One surviving personal record is available for recovery.

+ [Recover Epitaph - Part IV.] -> four
+ [Review what follows the recovered log.] -> master_tree_beacon
+ [Leave the archive sealed.] -> master_gan_eden_arrival


=== master_tree_beacon ===

Isa closes the archive, but another alert is already unfolding across her slate.

"One more active beacon." She expands a second vector. It rises from the inner surface toward the Space Elevator. "That has to be where he went after the Gate failed."

The two golden signatures return at the edge of the tactical display. This time they do not withdraw. Cherubim and Lahat Haharev turn together and begin closing on the fleet, as if they sensed your intent.

[Objective updated: Defeat Cherubim and Lahat Haharev and reach the Space Elevator.]

+ [Face the Golden Omega.] -> master_golden_omega


=== master_golden_omega ===
// Runtime encounter: GanEdenAmbushScript and GoldenFractalCascade

The scan does not come from one direction. It blooms across every active sensor at once, measuring the fleet from two mutually impossible angles.

Cherubim and Lahat Haharev pivot in exact counterpoint. No hail follows. Your own identification packet returns instead, stripped of its header and divided into two mirrored copies.

Two golden Omega Shards hold the approach to the Space Elevator: Cherubim and Lahat Haharev. Neither they nor any of their descendants will retreat.

The Shards divide under fire into Facets and Aspect wings. The Facets divide again into Tesseracts and further Aspect wings. Only the final Tesseracts die without reproducing.

Defeating only one named Shard is not enough. The survivor reconstructs its counterpart. Both must be destroyed in the same complete victory.

+ [Engage Cherubim and Lahat Haharev.] -> master_post_battle
+ [Break off.] -> master_gan_eden_arrival


=== master_post_battle ===
// Runtime resolution: first complete Golden Omega victory

With Cherubim and Lahat Haharev destroyed together, the Space Elevator's interdiction field falls silent.

Gan Eden remains parked beyond charted hyperspace, with the Power Transit Gate as its only route. The four settlement districts are released from their sealed economy groups and can participate in ordinary Sector trade.

The victory is not permanent. Every ninety days, the Golden Shards reconstruct themselves with a larger escort of ivory Remnant hulls, escalating until the escort is roughly a full Ordo. Later victories reset that cycle without sealing Gan Eden again.

+ [Approach the Gan Eden Space Elevator with Isa.] -> master_space_elevator
+ [Remain in Gan Eden.] -> master_gan_eden_arrival


=== master_space_elevator ===
// Runtime interaction: shipTrophyGanEdenEpitaph
// Runtime visual: Hall-completion-style letterbox showing Isa at the Space Elevator archive.
// Runtime music: begins with Epitaph Final and continues until the fleet leaves Gan Eden, yielding to combat when necessary.
// Return-visit music: cycles Lonesome Journey, the complete Log V cue, and 時が終わりに導いて; combat temporarily takes priority.

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

+ [Talk with Isa about what happened.] -> master_epilogue_talk


=== master_epilogue_talk ===

You remain beside Isa at the observation glass.

"I kept thinking I'd reach the end and find out who I was supposed to be," she says. "Leah. Isaac's daughter. The Continuity Office's last loose end."

She looks down at her grease-stained hands.

"But I already knew who I was. I just didn't know I was allowed to keep her."

Below, the Tree of Life turns slowly beneath the inward sun.

"Gan Eden gets a future," Isa says. "So do I."

+ [Review the conversations available afterward.] -> master_postquest_talk
+ [Return to the fleet.] -> END


=== master_postquest_talk ===

Isa sets aside the Hall ledgers. The light from Gan Eden's inward horizon is still reflected in the photographs she brought back.

+ [Talk about what happened.] -> master_postquest_after
+ [Talk about Gan Eden's future.] -> master_postquest_future
+ [Talk about Log I.] -> master_postquest_log_one
+ [Talk about Log II.] -> master_postquest_log_two
+ [Talk about Log III.] -> master_postquest_log_three
+ [Talk about Log IV.] -> master_postquest_log_four
+ [Talk about Log V.] -> master_postquest_log_five
+ [Talk about the Golden Omega.] -> master_postquest_omega
+ [End preview.] -> END


=== master_postquest_after ===

"I spent my whole life wondering whether that name meant I belonged to somebody. Turns out it did. It also turns out belonging to someone doesn't make their choices yours."

Isa taps the nameplate on her slate: ISA LEICESTER.

"Isaac gave me a beginning. The Ring gave me a life. You lot gave me the rest. I can live with that."

-> master_postquest_talk


=== master_postquest_future ===

"Gan Eden can't stay a mausoleum," Isa says. "But it shouldn't become another company town with a pretty sky, either."

She begins listing priorities: survey teams, independent settlement charters, protected archives, and strict limits on dismantling anything that still works.

"A place built for everyone ought to belong to the people willing to make a life there. We can help. We don't get to own it."

-> master_postquest_talk


=== master_postquest_log_one ===

"The first log made him real," Isa says. "Not my father. Not yet. Just an engineer who thought a project big enough could justify anything it demanded of him."

-> master_postquest_talk


=== master_postquest_log_two ===

"The second log hurt because it gave him a reason," Isa says. "A reason isn't an excuse. But it is a reason."

-> master_postquest_talk


=== master_postquest_log_three ===

"Then even Rebecca's face went. That's the part I keep coming back to. Centuries of memory, and grief was the thing that lasted."

-> master_postquest_talk


=== master_postquest_log_four ===

"He decided the only moral thing left was to destroy his life's work—and himself with it. He was wrong about that too. Gan Eden deserved a future he couldn't imagine."

-> master_postquest_talk


=== master_postquest_log_five ===

"He called me Leah," Isa says. "It's a beautiful name. It just isn't mine."

-> master_postquest_talk


=== master_postquest_omega ===

"Isaac thought they were angels," Isa says. "Maybe Omega read that in his systems. Whatever they were built to protect, I don't think that fight was the last word."

-> master_postquest_talk


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
