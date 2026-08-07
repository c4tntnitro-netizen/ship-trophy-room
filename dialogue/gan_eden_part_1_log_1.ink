// Hall of Triumph - A Borrowed Name / Gan Eden
// Part I - A Name on a Suit
//
// GENERATED PROOFREADING COPY. data/campaign/rules.csv is the
// sole dialogue authority used by this file. No other Ink file is
// read, imported, concatenated, or referenced by the generator.
// Runtime option labels below are emitted as real Ink choices.
//
// Scope: The Shattered Ring homecoming, Isa inheritance, and Log I.

-> volume_index

=== volume_index ===
Part I - A Name on a Suit

+ [Begin at the Shattered Ring.] -> rule_shipTrophyIsaShatteredRingHomecoming
+ [Read the complete first log.] -> rule_shipTrophyGanEdenEpitaphOne
+ [End preview.] -> END

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecoming ===
// rules.csv id: shipTrophyIsaShatteredRingHomecoming
// Trigger:
// OpenInteractionDialog
// Conditions:
// IsaHomecomingCMD shouldShow score:30000
// Runtime script:
// IsaHomecomingCMD prepare

Shattered Ring comes into view one broken section at a time.

Three vast arcs circle the barren moon below, each turning at a slightly different rate. Pressurized bridges, cargo gantries, and naked lengths of structural cable span some of the gaps. Others remain open to space, their severed ends capped with bulkheads scavenged from old ships.

The self-governing colony looks less constructed than prevented from falling apart. Isa appears beside the navigation station and plants both hands on your navigator’s desk.

"Take us around the dark side."

Your navigator glances at the approach plot.

"Traffic beacon’s directing us sunward."

"That traffic beacon’s been wrong since I was twelve."

+ [Continue through the approach.] -> rule_shipTrophyIsaShatteredRingHomecomingApproach

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingApproach ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingApproach
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_approach_continue
// Runtime script:
// IsaHomecomingCMD showIsa

As if summoned by the insult, the comm channel crackles.

"Approaching fleet, inbound bearing zero-three-five, elevation minus one-two. Reduce velocity and prepare to receive docking procedures—"

Isa leans over the console.

"Arthur? Tell Garret it’s me."

There is a long pause.

"Isaac?" another voice says. "Thought we were finally rid of you."

"Call me that again and I’ll come up there and kick your butt all over comms."

Garret laughs.

"Roger that. Take the locals' approach queue. You know the way. Over."

Isa closes the channel.

"Bay Fourteen," she tells the navigator, pointing at a row of glowing beacons hidden behind one of the arcs.

+ ["Isaac?"] -> rule_shipTrophyIsaShatteredRingHomecomingIsaac
+ ["You seem familiar with the place."] -> rule_shipTrophyIsaShatteredRingHomecomingFamiliar

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingIsaac ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingIsaac
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_isaac
// Runtime script:
// FireAll ShipTrophyIsaHomecomingDocking

Isa points a finger at you.

"Shut up."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingNo

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingNo ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingNo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_no
// Runtime script:
// FireAll ShipTrophyIsaHomecomingDocking

"No questions. No jokes. No telling the bodyguards."

Behind you, one of your bodyguards discreetly opens a note on their slate.

Isa points at them too.

"Wei. I can see you."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingFamiliar

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingFamiliar ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingFamiliar
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_familiar
// Runtime script:
// FireAll ShipTrophyIsaHomecomingDocking

"I grew up here."

Isa studies the approaching station.

"Mostly in Arc Two. Arc One had the good machine shops, but their gravity used to cut out whenever the ore processor started."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingDocking

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingDocking ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingDocking
// Trigger:
// ShipTrophyIsaHomecomingDocking

Bay Fourteen accepts your approaching fleet with a tremendous metallic cacophony.

The entire docking tube shudders as the clamps engage. Something heavy strikes the outer hull, tumbles away, and disappears beneath the berth.

Isa waits for the noise to stop.

"Perfect."

Everyone on your bridge exchanges looks. The docking tube groans again. Your flagship shifts in its berth with a tremendous crunch.

"Even more perfect."

+ [Continue into the concourse.] -> rule_shipTrophyIsaShatteredRingHomecomingDockingConcourse

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingDockingConcourse ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingDockingConcourse
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_docking_continue

By the time you reach the main concourse, word has spread.

Dockworkers call to Isa from the overhead gantries. A food vendor reaches across his counter to press a foil-wrapped pastry into her hand. Someone shouts that the recycler on Level Six is making the drinking water taste metallic again.

A child in an oversized pressure suit runs up and presents her with a cracked maneuvering thruster valve. Isa crouches, turns it over in her hands, and tells him which seal needs replacing.

Before you have crossed half the concourse, one of your bodyguards is carrying a crate of machine parts for an elderly mechanic. Another is holding a ladder steady while Isa points out a flickering light panel for an electrician.

Nobody has asked who you are.

+ ["I see you’ve missed them too."] -> rule_shipTrophyIsaShatteredRingHomecomingMissed
+ ["Are we charging for these repairs?"] -> rule_shipTrophyIsaShatteredRingHomecomingCharged

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingMissed ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingMissed
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_missed
// Runtime script:
// FireAll ShipTrophyIsaHomecomingForeman

"I didn’t say I missed anybody."

The light above her flickers twice, then steadies.

From the floor below, someone calls, "We missed you too, Isaac!"

Isa closes her eyes.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingCharged

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingCharged ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingCharged
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_charged

Isa looks back from the ladder.

"You own a fleet. You're basically royalty around here."

She returns to the light panel.

"This is where you play the part."

+ ["My favorite part of being royalty. Noblesse oblige."] -> rule_shipTrophyIsaShatteredRingHomecomingNoblesse

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingNoblesse ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingNoblesse
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_noblesse
// Runtime script:
// FireAll ShipTrophyIsaHomecomingForeman

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingForeman

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingForeman ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingForeman
// Trigger:
// ShipTrophyIsaHomecomingForeman

An old salvage foreman is waiting when Isa climbs down.

He wears a patched station utility suit, its original insignia hidden beneath decades of repairs. Under one arm, he carries a narrow metal case marked with faded cryogenic-handling symbols.

Isa’s good humor disappears.

"Where did you get that?"

"Found it behind the plating in Calder’s old storeroom," the foreman says. "Station council was clearing out abandoned property."

He holds the case toward her.

The people passing through the concourse give the three of you a wide berth. Finally, Isa wipes her hands against her trousers and accepts the case.

"Thanks."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingForemanContinue

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingForemanContinue ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingForemanContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_foreman_continue

The foreman nods. Before leaving, he looks toward you.

"She was wrapped in that when we found her. I used it as a changing mat for the first few weeks, too."

Isa throws a glove at the man.

"You didn’t have to tell $playerHimOrHer that."

"Why not? You weren’t going to."

The old man laughs, then disappears into the crowd.

+ [Let Isa take her inheritance.] -> rule_shipTrophyIsaShatteredRingHomecomingInheritance

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingInheritance ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingInheritance
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_inheritance
// Runtime script:
// IsaHomecomingCMD receiveSuit
// SetTextHighlightColors story story story
// SetTextHighlights "[Received: Isa's inheritance.]" "[Quest started: A Borrowed Name]" "[Objective: Investigate Isa's inheritance in her old workshop.]"

Isa closes the case and calls for one of your crew to transfer it to her old workshop.

[Received: Isa's inheritance.]

[Quest started: A Borrowed Name]

[Objective: Investigate Isa's inheritance in her old workshop.]

+ [Return to the Shattered Ring concourse.] -> rule_shipTrophyIsaShatteredRingHomecomingInheritanceReturn

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingInheritanceReturn ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingInheritanceReturn
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_inheritance_return
// Runtime script:
// FireBest OpenInteractionDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingWorkshopOption

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingWorkshopOption ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingWorkshopOption
// Trigger:
// PopulateOptions
// Conditions:
// IsaHomecomingCMD shouldShowWorkshop

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Investigate Isa's suit in her workshop.] -> rule_shipTrophyIsaShatteredRingHomecomingWorkshopOpen

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingWorkshopOpen ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingWorkshopOpen
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_workshop_open
// Runtime script:
// IsaHomecomingCMD prepareWorkshop
// FireAll ShipTrophyIsaHomecomingWorkshop

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingWorkshop

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingWorkshop ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingWorkshop
// Trigger:
// ShipTrophyIsaHomecomingWorkshop

Isa leads you through the station without speaking.

You descend through Arc Two, past hydroponics bays and crowded habitation decks, until the finished corridors give way to exposed conduits and old pressure doors.

The gravity weakens with each level.

Isa adjusts automatically, shortening her steps whenever the deck shifts beneath her. Your bodyguards are less graceful. One catches the overhead piping to avoid drifting into a wall.

At last, Isa opens an abandoned machine shop.

Several names have been carved into the pressure door. Hers is among them, scratched low enough that whoever wrote it must have been very young.

ISA LESESTER IS DA BEST.

+ [Open the case.] -> rule_shipTrophyIsaShatteredRingHomecomingWorkshopCase

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingWorkshopCase ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingWorkshopCase
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_workshop_continue

She sets the case on an old workbench. Inside is a child-sized bundle of pressure fabric, folded carefully beneath a transparent preservation sheet.

The material was once white. Radiation and age have yellowed it almost to brown. Several sections have been cut from a much larger suit and crudely flextaped together, turning the spacesuit into something more like a cradle.

A blackened name strip remains attached to the collar.

LEICESTER, ISAAC.

Isa touches two fingers to the transparent sheet.

"This is where they got it," she says. "My name."

+ ["You were named after the suit?"] -> rule_shipTrophyIsaShatteredRingHomecomingNamed

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingNamed ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingNamed
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_named

"I’m a ‘pod person.’ They thawed me out of a cryopod when I was still a baby, all swaddled up in that."

Isa gives you a smile.

"I went by the name on the label until I was old enough to realize ‘Isaac’ was a man’s name."

+ ["Cryosuspension that young is unheard of."] -> rule_shipTrophyIsaShatteredRingHomecomingScan

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingScan ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingScan
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_scan

"My great claim to fame," Isa says, smiling. "Youngest ‘pod person’ on my Arc."

A small metal contact is visible beneath the scorched collar.

Isa stops smiling.

"That wasn’t there before."

She releases the preservation seal and carefully lifts the suit onto the workbench. The fabric crackles as she turns the collar over, shedding small flecks of decayed plastic onto the surface below.

The frayed insulation has exposed an identification wafer embedded beneath the name strip.

Isa removes her slate.

On scan, her slate’s display splits into dozens of windows. Her delta-level AI agents begin testing ancient authentication protocols, interpolating damaged sectors, and comparing the wafer against surviving Domain registries.

"You don’t have to stay," she says.

+ ["Let me know when you need me."] -> rule_shipTrophyIsaShatteredRingHomecomingGo
+ ["I don’t have to go."] -> rule_shipTrophyIsaShatteredRingHomecomingStay

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingGo ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingGo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_go

Isa glances at you, then nods.

"Thanks, $playerRank."

+ ["I’ll give you privacy. Wei, Yvan."] -> rule_shipTrophyIsaShatteredRingHomecomingPrivacy

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingPrivacy ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingPrivacy
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_privacy
// Runtime script:
// FireAll ShipTrophyIsaHomecomingRegistryOne

You walk out with your bodyguards, leaving Isa to it.

Outside the workshop, Shattered Ring creaks and groans around you. Pumps cycle behind the walls. Somewhere far below, a cargo lift begins its slow ascent through the arc.

After a few minutes, Isa calls from the workshop.

You had finally cornered Yvan in your chess match. It was going to be weeks before you rolled a Chess960 setup that good again.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingStay

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingStay ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingStay
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_stay
// Runtime script:
// FireAll ShipTrophyIsaHomecomingRegistryOne

Isa glances at you, then nods.

"Thanks, $playerRank."

And she returns to the slate.

Minutes pass.

Outside the workshop, Shattered Ring creaks and groans around you. Pumps cycle behind the walls. Somewhere far below, a cargo lift begins its slow ascent through the arc.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingRegistryOne

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRegistryOne ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRegistryOne
// Trigger:
// ShipTrophyIsaHomecomingRegistryOne

One of the windows on Isa’s slate turns green. A personal log opens.

+ [Play the recovered log.] -> rule_shipTrophyIsaShatteredRingHomecomingRegistryTwo

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRegistryTwo ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRegistryTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_registry_two
// Runtime script:
// GanEdenQuestCMD showInitialLogPage 0

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyIsaShatteredRingHomecomingRegistryPageTwo

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRegistryPageTwo ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRegistryPageTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_registry_page_two
// Runtime script:
// GanEdenQuestCMD showInitialLogPage 1

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyIsaShatteredRingHomecomingRegistryPageThree

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRegistryPageThree ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRegistryPageThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_registry_page_three
// Runtime script:
// GanEdenQuestCMD showInitialLogPage 2

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Close the log.] -> rule_shipTrophyIsaShatteredRingHomecomingRegistryResponse

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRegistryResponse ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRegistryResponse
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_registry_response

The log ends.

Isa remains motionless, staring at the author field.

LEICESTER, ISAAC THOMAS.

She looks down at the faded label inside the spacer suit.

Then back at the identification wafer.

"So he was real."

She scrolls through the recovered file again.

"Director of Engineering. Architect of the Continuity Office. Hypershunts."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingRegistryReceipt

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRegistryReceipt ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRegistryReceipt
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_registry_receipt
// Runtime script:
// GanEdenQuestCMD recoverInitialLog
// SetTextHighlightColors hColor hColor
// SetTextHighlights "[Recovered Personal Log 1765.]" "[Filed under Gan Eden Archives in Intel.]"
// FireAll ShipTrophyIsaHomecomingHypershuntKnowledgeOptions

[Recovered Personal Log 1765.]
[Filed under Gan Eden Archives in Intel.]

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingReal

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingReal ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingReal
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_real
// Runtime script:
// FireAll ShipTrophyIsaHomecomingRealization

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingKnew

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingKnew ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingKnew
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_knew
// Runtime script:
// FireAll ShipTrophyIsaHomecomingRealization

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingRealization

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingRealization ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingRealization
// Trigger:
// ShipTrophyIsaHomecomingRealization

"Yeah."

Isa enlarges the redacted assignment field. Her agents attack it from every direction, but the text dissolves into meaningless fragments.

"He put me in that suit. Registered me under his clearance. Maybe loaded the pod too."

She stares at the name above hers.

"I always figured whoever loaded the pod grabbed whatever was nearby. Found me, found the suit, wrapped me up in it, then chucked me in."

Her fingers tighten around the edge of the workbench.

"But he knew I was there."

+ ["We can search the surviving registries."] -> rule_shipTrophyIsaShatteredRingHomecomingSearch
+ ["You don’t owe a dead man an investigation."] -> rule_shipTrophyIsaShatteredRingHomecomingOwe

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingSearch ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingSearch
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_search
// Runtime script:
// FireAll ShipTrophyIsaHomecomingBeginSearch

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingOwe

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingOwe ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingOwe
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_owe
// Runtime script:
// FireAll ShipTrophyIsaHomecomingBeginSearch

"No."

Isa looks down at the old name strip.

"But I owe myself one."

You hear her add, almost beneath her breath:

"And I don’t actually know that he’s dead..."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingBeginSearch

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingBeginSearch ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingBeginSearch
// Trigger:
// ShipTrophyIsaHomecomingBeginSearch

Isa returns to the slate.

Her agents scatter through the surviving Domain archives, following Isaac Leicester’s registry number through personnel rosters, departmental accounts, procurement records, and damaged transit indexes.

Most references vanish as soon as they begin to resolve.

DCR-2F38-CB017-6A

The number appears beside a Heliostructural Systems roster.

Then a classified procurement authorization.

Then a personnel-transfer order whose destination disappears beneath another INFOSEC warning.

Isa opens the failed queries side by side.

"What are you doing?" you ask.

"Looking at how they break."

+ [Continue the search.] -> rule_shipTrophyIsaShatteredRingHomecomingSearchContinue

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingSearchContinue ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingSearchContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_search_continue

She groups the failures by their distorted output. Fields concealing the same information collapse in nearly identical ways.

Isa’s agents begin finding matches.

CLEARANCE — ELEVEN CORRESPONDING RECORDS
STATUS — DETAINEE PROCESSING
LAST ASSIGNMENT — ONE CORRESPONDING RECORD

Isa selects the final match.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingTransit

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingTransit ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingTransit
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_transit

A damaged manifest opens above the workbench.

PENELOPE’S STAR GATE TRANSIT AUTHORITY
OUTBOUND PASSENGER CONTROL

Most of the record is unreadable. Names and registry numbers emerge briefly between broken columns and access warnings.

One line illuminates.

GATE OUTBOUND TRANSIT AUTHORIZATION
PASSENGER: INFOSEC RESTRICTED
CITIZEN REGISTRY: INFOSEC RESTRICTED
ORIGIN: PENELOPE’S STAR
DECLARED DESTINATION: SOL
TRANSIT STATUS: AUTHORIZED

Isa places the manifest beside Isaac’s citizen record.

Both restricted fields fail in precisely the same way.

She watches the paired distortions repeat.

Then she says:

"That’s him."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingFinalTransit

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingFinalTransit ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingFinalTransit
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_final_transit

Isa opens the surviving end of the manifest.

FINAL OUTBOUND AUTHORIZATION
PASSENGER COUNT: 1
ORIGIN: PENELOPE’S STAR
DESTINATION: INFOSEC REDACTED
RING STATUS: OPERATIONAL

She studies the two destination fields.

"His declared destination was Sol."

Her agents probe the second field. A few characters appear, then collapse beneath a new warning.

DESTINATION: SUPER ALABASTER RESTRICTED

Isa frowns.

"Super Alabaster."

+ ["Meaning?"] -> rule_shipTrophyIsaShatteredRingHomecomingAlabaster

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingAlabaster ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingAlabaster
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_alabaster
// Runtime script:
// FireAll ShipTrophyIsaHomecomingHypershuntKnowledgeOptions

"Meaning whoever buried this didn’t want the destination turning up anywhere else."

Isa opens the surrounding archive references.

The restriction has propagated through transit logs, personnel orders, and every surviving copy linked to the authorization. Each record breaks at the same point.

"Can you recover it?" you ask.

"Not from this."

She continues sorting the remaining collisions.

Two infrastructure designations recur beside Isaac’s registry number.

CORONAL HYPERSHUNT — NETWORK AUTHORIZATION
CORONAL HYPERSHUNT — NETWORK AUTHORIZATION

Isa enlarges them.

"He worked on the hypershunts."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingHypershuntsKnownOption

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingHypershuntsKnownOption ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingHypershuntsKnownOption
// Trigger:
// ShipTrophyIsaHomecomingHypershuntKnowledgeOptions
// Conditions:
// GanEdenQuestCMD hasKnownHypershunt

// No literal text in rules.csv; the runtime script supplies this beat.

+ ["We’ve seen those before. They’re guarded by extremely dangerous automated ships. They call themselves ‘Omega.’"] -> rule_shipTrophyIsaShatteredRingHomecomingHypershuntsKnown

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingHypershuntsUnknownOption ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingHypershuntsUnknownOption
// Trigger:
// ShipTrophyIsaHomecomingHypershuntKnowledgeOptions
// Conditions:
// !GanEdenQuestCMD hasKnownHypershunt

// No literal text in rules.csv; the runtime script supplies this beat.

+ ["Hypershunts?"] -> rule_shipTrophyIsaShatteredRingHomecomingHypershuntsUnknown

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingHypershuntsKnown ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingHypershuntsKnown
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_hypershunts_known
// Runtime script:
// FireAll ShipTrophyIsaHomecomingHypershuntsCompare

Isa looks up sharply.

"Omega? AI ships?"

She turns back to the log.

"What were you doing, Isaac...?"

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingHypershuntsUnknown

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingHypershuntsUnknown ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingHypershuntsUnknown
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_hypershunts_unknown
// Runtime script:
// FireAll ShipTrophyIsaHomecomingHypershuntsCompare

"Incredibly old, pre-Collapse technology," Isa says. "They draw power directly from a star."

She brings up the relevant records.

"Only two are known to have survived in the Sector."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingHypershuntsCompare

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingHypershuntsCompare ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingHypershuntsCompare
// Trigger:
// ShipTrophyIsaHomecomingHypershuntsCompare

Isa scrolls back through the log.

"He oversaw both of them. He ran the office responsible for keeping the project alive across centuries."

She taps the author field.

"If there are any records left of him, that’s where they’ll be."

Isa finishes copying the surviving log and INFOSEC failures to her slate.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingHypershuntsContinue

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingHypershuntsContinue ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingHypershuntsContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_hypershunts_continue

For a while, she says nothing. The old suit remains spread across the workbench between you, Isaac Leicester’s name blackened but still legible beneath the collar. Then she closes the file.

"Captain."

Her voice has lost its earlier lightness.

"I know this isn’t fleet business. But somebody went to a lot of trouble to hide whatever he was building. I don’t think I’m going to get much further by myself."

Isa looks directly at you.

"I want to find out who Isaac Leicester was. What happened to him. How his suit ended up wrapped around me."

She hesitates.

"I’m also asking for your help."

+ ["Our chief engineer’s business is fleet business."] -> rule_shipTrophyIsaShatteredRingHomecomingAcceptCommand
+ ["We’ll visit the hypershunts. We’ll see where they lead."] -> rule_shipTrophyIsaShatteredRingHomecomingAcceptCautious

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingThenVisit ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingThenVisit
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_then_visit
// Runtime script:
// FireAll ShipTrophyIsaHomecomingDecision

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingWhatTell

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingWhatTell ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingWhatTell
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_what_tell
// Runtime script:
// FireAll ShipTrophyIsaHomecomingDecision

"I don’t know yet."

Isa highlights the matching authorization residue.

"But this is the first thing I’ve found that the redaction didn’t completely erase."

She closes the manifest.

"If they still carry any part of the old routing record, I can work from there."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingDecision

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingDecision ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingDecision
// Trigger:
// ShipTrophyIsaHomecomingDecision

Isa finishes copying the surviving records to her slate.

For a while, she says nothing. The old suit remains spread across the workbench between you, Isaac Leicester’s name blackened but still legible beneath the collar.

Then she closes the registry.

"Captain."

Her voice has lost its earlier lightness.

"I know this isn’t fleet business."

She rests one hand on the metal case.

"But somebody went to a lot of trouble to bury him. Domain INFOSEC, old registry locks, whatever was left in those records. I don’t think I’m going to get much further by myself."

Isa looks directly at you.

"I want to find out who Isaac Leicester was. What happened to him. Why he put me in that pod."

She hesitates.

"I’m also asking for your help."

+ ["My chief engineer’s business is fleet business."] -> rule_shipTrophyIsaShatteredRingHomecomingAcceptCommand
+ ["We’ll visit the hypershunts. We’ll see where they lead."] -> rule_shipTrophyIsaShatteredRingHomecomingAcceptCautious

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingAcceptCommand ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingAcceptCommand
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_accept_command

Isa gives you a small, crooked smile.

"That a direct order?"

+ ["Don't make your CO repeat themself."] -> rule_shipTrophyIsaShatteredRingHomecomingDirectOrder

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingDirectOrder ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingDirectOrder
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_direct_order
// Runtime script:
// FireAll ShipTrophyIsaHomecomingPrepareSearch

She snaps a salute.

"Aye-aye, $playerRank."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingAcceptCautious

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingAcceptCautious ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingAcceptCautious
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_accept_cautious
// Runtime script:
// FireAll ShipTrophyIsaHomecomingPrepareSearch

"That’s all I’m asking."

+ [Continue.] -> rule_shipTrophyIsaShatteredRingHomecomingPrepareSearch

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingPrepareSearch ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingPrepareSearch
// Trigger:
// ShipTrophyIsaHomecomingPrepareSearch
// Runtime script:
// SetShortcut ship_trophy_isa_homecoming_return "ESCAPE"
// SetTextHighlightColors story
// SetTextHighlights "[Objective updated: Search the Coronal Hypershunts for clues about Isaac Leicester.]"

Isa transfers the recovered log, the wafer registry number, and the INFOSEC failures into a new directory.

The folder remains unnamed for several seconds.

Then she enters:

ISAAC THOMAS LEICESTER

[Objective updated: Search the Coronal Hypershunts for clues about Isaac Leicester.]

+ [Return to the fleet.] -> rule_shipTrophyIsaShatteredRingHomecomingReturn

// ============================================================
=== rule_shipTrophyIsaShatteredRingHomecomingReturn ===
// rules.csv id: shipTrophyIsaShatteredRingHomecomingReturn
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_homecoming_return
// Runtime script:
// IsaHomecomingCMD markSeen
// FireBest OpenInteractionDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenEpitaphOne

// ============================================================
=== rule_shipTrophyGanEdenEpitaphOne ===
// rules.csv id: shipTrophyGanEdenEpitaphOne
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_one
// Runtime script:
// GanEdenQuestCMD prepareEpitaphLog part_one

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: LOG — PART I

My name is Isaac Thomas Leicester, Director of Engineering for Heliostructural Systems in the Persean Sector.

My work included the construction of the coronal hypershunts and, later, Project Eden.

The projects were expected to take centuries. Eden might have required more than a thousand years before reaching its final design capacity. No ordinary succession of administrators could preserve the technical knowledge, institutional memory, and design intent required over such a span.

The Domain therefore established a continuity office. Its directors would serve across generations, entering cryosuspension between major phases of construction and returning whenever the work required their judgment.

I was elected to lead it.

The appointment required me to spend hundreds of years at a time in cryosuspension. I would sleep through epochs of construction, wake for major design reviews, inspect the work and approve the next phase.

The first hypershunt was already under construction when I accepted the appointment. It was intended to collect energy directly from a star and make that power available to automated construction systems far beyond any ordinary network.

The engineering difficulties were substantial.

The structure had to remain stable inside a star’s corona. Its collection systems had to survive constant radiation and violent changes in stellar activity. The transfer network had to carry more power in one moment than entire inhabited systems had ever consumed.

We lost dozens of unmanned assembly swarms before completing the first stable framework.

The second hypershunt was approved before the first reached full capacity. Its design incorporated everything we had learned. Construction proceeded more quickly, although centuries still passed outside my chamber.

Each waking period lasted several months, even years. I met engineers whose grandparents had worked under engineers I remembered. I approved changes to systems designed by people who had passed while I slept. The Domain expanded between each waking.

New systems joined the Gate network. Problems once considered impossible became trivial. I believed I was watching humanity mature, but I did not yet understand what was being lost.

When two hypershunts became operational, the Directorate approved the project they had been built to support.

I proposed the name Gan Eden, after the sacred garden of God.

It was to be the Domain’s first enclosed—

DOMAIN INFOSEC VIOLATION THRESHOLD WARNING
// datastream resetting...

—constructed around a yellow sun on the far outskirts of the Persean Sector. At its intended capacity, it could shelter several quintillion human li—

FATAL ACCESS ERROR //

This device is not authorized to provide additional description of the referenced project, destination, or design objective. Please upgrade your data permission level in compliance with Domain Information Security Standards.

I believed that the ability to construct such a wonder proved that the Domain deserved to continue expanding.

I was wrong.

// Runtime destination outside this volume: shipTrophyGanEdenEpitaphTwo
+ [Continue to Part II.] -> END

// ============================================================
// END OF RULES.CSV EXPORT
// ============================================================
