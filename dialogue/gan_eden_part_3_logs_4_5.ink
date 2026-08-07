// Hall of Triumph - A Borrowed Name / Gan Eden
// Part III - Gan Eden
//
// GENERATED PROOFREADING COPY. data/campaign/rules.csv is the
// sole dialogue authority used by this file. No other Ink file is
// read, imported, concatenated, or referenced by the generator.
// Runtime option labels below are emitted as real Ink choices.
//
// Scope: Power Transit, Gan Eden, Logs IV-Final, and the epilogue.

-> volume_index

=== volume_index ===
Part III - Gan Eden

+ [Approach the Power Transit Gate.] -> rule_shipTrophyGanEdenExternalRing
+ [Review the Golden Omega encounter.] -> rule_shipTrophyGanEdenGoldenEncounter
+ [Read the complete fourth log.] -> rule_shipTrophyGanEdenEpitaphFour
+ [Read the complete final log.] -> rule_shipTrophyGanEdenEpitaphFive
+ [Review Isa's conversation about Isaac.] -> rule_shipTrophyIsaGanEdenHub
+ [End preview.] -> END

// ============================================================
=== rule_shipTrophyGanEdenGoldenEncounter ===
// rules.csv id: shipTrophyGanEdenGoldenEncounter
// Trigger:
// BeginFleetEncounter
// Conditions:
// GanEdenQuestCMD isGoldenOmegaFightable score:90000
// Runtime script:
// $shownFleetDescAlready = true 0

The scan does not come from one direction. It blooms across every active sensor at once, measuring your fleet from two mutually impossible angles.

Cherubim and Lahat Haharev pivot in exact counterpoint. No hail follows. No warning asks you to keep your distance.

Your own identification packet returns instead, stripped of its header and divided into two mirrored copies.

+ [Continue.] -> rule_shipTrophyGanEdenLogThreeDamaged

// ============================================================
=== rule_shipTrophyGanEdenLogThreeDamaged ===
// rules.csv id: shipTrophyGanEdenLogThreeDamaged
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_log_three_damaged

"I know."

Isa rubs at one eye. She looks back at the frozen image of Isaac’s authorization header.

"He spent all that time building a future for his daughter, and by the end he couldn’t even remember the woman he was building it for."

+ [Continue.] -> rule_shipTrophyGanEdenLogThreeRoutingContinue

// ============================================================
=== rule_shipTrophyGanEdenLogThreeTried ===
// rules.csv id: shipTrophyGanEdenLogThreeTried
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_log_three_tried

"He did."

Isa looks at the final line again. She exhales through her nose.

"I don’t know whether he’s confessing or asking whoever hears this to forgive him."

A pause.

"Maybe he didn’t know either."

+ [Continue.] -> rule_shipTrophyGanEdenLogThreeRoutingContinue

// ============================================================
=== rule_shipTrophyGanEdenLogThreeRoutingContinue ===
// rules.csv id: shipTrophyGanEdenLogThreeRoutingContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_log_three_routing_continue
// Runtime script:
// FireAll ShipTrophyGanEdenHypershuntSurveyCompleteFinal

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenArrivalScene

// ============================================================
=== rule_shipTrophyGanEdenArrivalScene ===
// rules.csv id: shipTrophyGanEdenArrivalScene
// Trigger:
// ShipTrophyGanEdenArrivalScene
// Runtime script:
// SetShortcut ship_trophy_gan_eden_arrival_page_two "ESCAPE"

The Power Transit Gate closes behind your fleet.

For several seconds, nobody on the bridge says anything. The first thing you hear are altitude warnings. Your navigation display struggles to decide which way is down. Gravitational calibration spins like a top.

Then through the CIC's viewcams, you see it.

Gan Eden rises below you. Closes in above you. Rises all around you.

+ [Continue.] -> END

// ============================================================
=== rule_shipTrophyGanEdenArrivalSceneTwo ===
// rules.csv id: shipTrophyGanEdenArrivalSceneTwo
// Trigger:
// ShipTrophyGanEdenArrivalSceneTwo
// Runtime script:
// SetShortcut ship_trophy_gan_eden_arrival_page_three "ESCAPE"

Oceans stretch across the inner surface of the world, blue expanses thousands of kilometers wide. Mountain chains climb toward the horizon until distance turns them pale, then continue overhead. Cloud systems drift across continents suspended impossibly above you, their shadows moving over forests and inland seas.

There is no horizon in any direction. Only a solar-system sized, singular world.

There are no words to say. You see several of your bridge members reach out to hug each other.

Then every alarm on Isa's slate goes off at once.

+ [Continue.] -> END

// ============================================================
=== rule_shipTrophyGanEdenArrivalSceneThree ===
// rules.csv id: shipTrophyGanEdenArrivalSceneThree
// Trigger:
// ShipTrophyGanEdenArrivalSceneThree
// Runtime script:
// SetShortcut ship_trophy_gan_eden_arrival_page_four "ESCAPE"

"Gravitational reference error. Local vertical unresolved." Isa mutters, barely able to tear her eyes away. "Atmosphere where atmosphere shouldn't be—"  Isa mutters, barely able to tear her eyes away from the scenery.

She kills the warnings one after another.

Another window opens.

Isa stops.

She stares at it long enough that you turn from the viewport.

"...wait."

Her fingers move across the slate.

A thin signal resolves out of the noise.

+ [Continue.] -> END

// ============================================================
=== rule_shipTrophyGanEdenArrivalSceneFour ===
// rules.csv id: shipTrophyGanEdenArrivalSceneFour
// Trigger:
// ShipTrophyGanEdenArrivalSceneFour
// Runtime script:
// SetShortcut ship_trophy_gan_eden_arrival_continue "ESCAPE"

"Active distress beacon," she says.

There is sudden excitement in her voice. Then she reads the header.

She looks back at the impossible world surrounding your fleet.

The signal repeats.

Isa opens the attached navigation packet.

"Source is planetside. Or... shell-side. Whatever we're calling it."

Your ship's AI spends several seconds inventing a coordinate system capable of describing the destination.

Then a name appears.

TREE OF LIFE
EDEN PRIMARY BIOSPHERE
EMERGENCY ACCESS

+ [Nav, set a course.] -> END

// ============================================================
=== rule_shipTrophyGanEdenExternalRingLocked ===
// rules.csv id: shipTrophyGanEdenExternalRingLocked
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD isExternalRing score:51000
// GanEdenQuestCMD lacksUsableJanusGate
// Runtime script:
// SetShortcut ship_trophy_gan_eden_ring_leave "ESCAPE"

Power Transit Gate - Gan Eden hangs alone at the center of an empty starless system. Impossible depth shifts within the aperture but the Gate does not answer your fleet. Traversal requires a Janus Device integrated with the fleet.

+ [Leave.] -> rule_shipTrophyGanEdenRingLeave

// ============================================================
=== rule_shipTrophyGanEdenExternalRing ===
// rules.csv id: shipTrophyGanEdenExternalRing
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD isExternalRing score:50000
// GanEdenQuestCMD canUseJanusGate
// Runtime script:
// SetShortcut ship_trophy_gan_eden_ring_leave "ESCAPE"

Power Transit Gate - Gan Eden hangs alone at the center of an empty, starless system. Its adamantine surface is awake. Within the aperture, impossible depth folds toward the sealed world beyond.

+ [Enter the Power Transit Gate.] -> rule_shipTrophyGanEdenExternalRingEnter
+ [Leave.] -> rule_shipTrophyGanEdenRingLeave

// ============================================================
=== rule_shipTrophyGanEdenExternalRingEnter ===
// rules.csv id: shipTrophyGanEdenExternalRingEnter
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_ring_enter
// GanEdenQuestCMD canUseJanusGate
// Runtime script:
// GanEdenQuestCMD transitIn
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenInternalRing

// ============================================================
=== rule_shipTrophyGanEdenInternalRing ===
// rules.csv id: shipTrophyGanEdenInternalRing
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD isInternalRing score:50000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_ring_leave "ESCAPE"

The Eden Transit Ring frames a narrow wound in the sealed world's geometry. Its connection remains synchronized with Power Transit Gate - Gan Eden.

+ [Traverse the Gate.] -> rule_shipTrophyGanEdenInternalRingExit
+ [Remain in Gan Eden.] -> rule_shipTrophyGanEdenRingLeave

// ============================================================
=== rule_shipTrophyGanEdenInternalRingExit ===
// rules.csv id: shipTrophyGanEdenInternalRingExit
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_ring_exit
// Runtime script:
// GanEdenQuestCMD transitOut
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenRingLeave

// ============================================================
=== rule_shipTrophyGanEdenRingLeave ===
// rules.csv id: shipTrophyGanEdenRingLeave
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_ring_leave
// Runtime script:
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenSurfaceLog

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLog ===
// rules.csv id: shipTrophyGanEdenSurfaceLog
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD canRecoverSurfaceLog score:62000
// Runtime script:
// ShowImageVisual ship_trophy_gan_eden_eden_prime
// SetShortcut ship_trophy_gan_eden_surface_log_leave "ESCAPE"

Your fleet touches down beneath the Tree of Life.

For a while, nobody seems to know what to do.

The landing grounds lie beneath a vast canopy of living green, the trunks rising between grey, uniform towers and terraces until the branches close together overhead like the roof of a cathedral. Warm air moves through the open ramps carrying the smell of soil, leaves, and water.

Requests for shore leave begin almost immediately.

By the time the first reports reach you, entire deck crews are offering detailed explanations for why their particular ship can absolutely spare them for an hour.

Your captains recommend a compromise: short rotating leave. The schedule is approved with suspicious enthusiasm.

When the first rotation comes down the ramps, some of the spacers simply stop.

+ [Continue.] -> rule_shipTrophyGanEdenTreeLandingContinue

// ============================================================
=== rule_shipTrophyGanEdenTreeLandingContinue ===
// rules.csv id: shipTrophyGanEdenTreeLandingContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_landing_continue

Most have seen nothing but hydroponics decks and cramped crew quarters their entire lives. Some have even visited abundant worlds like Gilead. The ones who have not walk out into the grass and lie down in it, staring up through the branches into the strange, inverted shell-sky.

Before long, the landing grounds are dotted with off-duty crew doing exactly the same thing. Some of them start a kickball match.

You make a mental note to have the watches keep careful count when each rotation returns. AWOL incidents would be epidemic. Isa stands at the foot of the ramp, staring upward.

For once, she seems to have forgotten the slate in her hand.

+ [Continue.] -> rule_shipTrophyGanEdenTreeLandingIsaContinue

// ============================================================
=== rule_shipTrophyGanEdenTreeLandingIsaContinue ===
// rules.csv id: shipTrophyGanEdenTreeLandingIsaContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_landing_isa_continue

The wind moves through the canopy far overhead. Sunlight breaks through in shifting columns, catching on leaves and the pale sides of the empty towers.

Wow. She mouths.

Her slate pings.

The moment is gone.

She looks down. Every sensor window has filled at once, the same signal that drew you to the Tree of Life is bleeding across channel after channel.

"That’s him."

+ [Follow Isa.] -> rule_shipTrophyGanEdenTreeFollowIsa

// ============================================================
=== rule_shipTrophyGanEdenTreeFollowIsa ===
// rules.csv id: shipTrophyGanEdenTreeFollowIsa
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_follow_isa

The settlement known as the Tree of Life is cleaner than anything has a right to be.

There are no sweepers, no maintenance crews, no little machines trundling along the paths like you might see at Tritachyon. Dust simply does not seem to settle. Water leaves no trace on the pale walls or glass, and fallen leaves collect in neat lines along the edges of the walkways, as though the city itself knows where they belong.

At the larger intersections, broad basins of crystal-clear water descend into deep reservoirs placed in the middle of the streets. Your suit's scanners immediately flag it as drinkable—almost absurdly free of contaminants.

+ [Continue.] -> rule_shipTrophyGanEdenTreeCleanCityContinue

// ============================================================
=== rule_shipTrophyGanEdenTreeCleanCityContinue ===
// rules.csv id: shipTrophyGanEdenTreeCleanCityContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_clean_city_continue

One of your bodyguards plops herself onto a stone bench. The woman lets out a tremendous sigh of contentment. Isa runs two fingers along the back of it and looks at them.

Nothing.

"Self-cleaning surfaces." She studies the spotless stone. "I see no conduits. So it isn't electrostatic. Catalytic perhaps?"

Her slate pulses again, the signal still so strong that it bleeds across every other sensor channel. Isa follows it away from the broad civic terraces and deeper beneath the roots of the Tree of Life, into a quieter district where the buildings are smaller and the streets narrow into shaded footpaths.

The signal leads you to an old workshop.

The door opens without resistance.

+ [Enter the workshop.] -> rule_shipTrophyGanEdenTreeWorkshopEnter

// ============================================================
=== rule_shipTrophyGanEdenTreeWorkshopEnter ===
// rules.csv id: shipTrophyGanEdenTreeWorkshopEnter
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_workshop_enter

Inside, Gan Eden’s perfection finally gives way to something unmistakably human. Tools cover the benches. Components have been dismantled and rebuilt by hand. A narrow cot has been pushed against one wall, and beside it sits a small table with a teapot still resting on top.

Isa lifts the lid.

The inside is bone dry, with a dark ring of residue clinging to the bottom.

She puts it down carefully.

"He lived here."

One wall has been covered in calculations. At first they look like ordinary power-transfer notes. Isa studies them for several seconds, then steps closer. Hypershunt output frequencies have been broken apart and recombined alongside communications theory, with tiny variations marked through the carrier harmonics.

+ [Examine the calculations.] -> rule_shipTrophyGanEdenTreeWorkshopCalculations

// ============================================================
=== rule_shipTrophyGanEdenTreeWorkshopCalculations ===
// rules.csv id: shipTrophyGanEdenTreeWorkshopCalculations
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_workshop_calculations

She steps closer.

"He was using the hypershunts to send communications."

Her fingers move over the equations.

"Same principle as sending data through a power line."

The signal pulses again.

This time, Isa isolates the authorization header almost immediately.

DCR-2F38-CB017-6A
LEICESTER, ISAAC THOMAS
CONTINUITY AUTHORITY

For a moment she only stares at it.

Then the rest of the transmission begins to resolve.

+ [Let Isa work.] -> rule_shipTrophyGanEdenTreeWorkshopSignal

// ============================================================
=== rule_shipTrophyGanEdenTreeWorkshopSignal ===
// rules.csv id: shipTrophyGanEdenTreeWorkshopSignal
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_tree_workshop_signal

She looks around the workshop again: the cot, the tools, the dry teapot, the equations covering the wall.

"The first log was on the wafer. He sent the next two through the hypershunts."

Another section of the carrier reconstruction locks into place.

"Looks like he was done with a fourth log."

A file opens on her slate.

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: LOG — PART IV

Isa sits down on the edge of the workbench.

"Okay, Isaac."

+ [Play the recording.] -> rule_shipTrophyGanEdenSurfaceLogRecover

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogRecover ===
// rules.csv id: shipTrophyGanEdenSurfaceLogRecover
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_recover
// Runtime script:
// GanEdenQuestCMD showLogPage part_four 0

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenSurfaceLogPageTwo

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogPageTwo ===
// rules.csv id: shipTrophyGanEdenSurfaceLogPageTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_page_two
// Runtime script:
// GanEdenQuestCMD showLogPage part_four 1

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenSurfaceLogPageThree

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogPageThree ===
// rules.csv id: shipTrophyGanEdenSurfaceLogPageThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_page_three
// Runtime script:
// GanEdenQuestCMD showLogPage part_four 2

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenSurfaceLogPageFour

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogPageFour ===
// rules.csv id: shipTrophyGanEdenSurfaceLogPageFour
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_page_four
// Runtime script:
// GanEdenQuestCMD showLogPage part_four 3

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenSurfaceLogPageFive

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogPageFive ===
// rules.csv id: shipTrophyGanEdenSurfaceLogPageFive
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_page_five
// Runtime script:
// GanEdenQuestCMD showLogPage part_four 4

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Close the recording.] -> rule_shipTrophyGanEdenSurfaceLogFile

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogFile ===
// rules.csv id: shipTrophyGanEdenSurfaceLogFile
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_file
// Runtime script:
// GanEdenQuestCMD recoverSurfaceLog

Isa grips her arm.

"He held me."

+ [He was the last one to go through the Gate before the Collapse. That means...] -> rule_shipTrophyGanEdenSurfaceLogContinue

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogContinue ===
// rules.csv id: shipTrophyGanEdenSurfaceLogContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_continue
// Runtime script:
// SetTextHighlightColors hColor
// SetTextHighlights "Space Elevator"

"I know."

For a moment, she says nothing.

She just sniffles.

Then Isa pulls out her slate and clears Isaac’s emergency transponder from the alert queue. She archives his fourth log, then retunes her receiver.

"One last signal."

Isa looks up.

"It’s at the Space Elevator."

You step out of the workshop. Far across the inward horizon, the Space Elevator rises above the continent, its great spire reaching toward the center of Gan Eden.

A sinking feeling worms its way into your stomach.

You key the mic to your second in command.

+ ["Crew ready by 1100. Readiness Level 2. Set course for the Gan Eden Space Elevator."] -> rule_shipTrophyGanEdenSurfaceLogReturn
+ ["Double the shore leave. Then ready the fleet for a fight."] -> rule_shipTrophyGanEdenSurfaceLogShoreLeave

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogReturn ===
// rules.csv id: shipTrophyGanEdenSurfaceLogReturn
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_return
// Runtime script:
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenSurfaceLogShoreLeave

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogShoreLeave ===
// rules.csv id: shipTrophyGanEdenSurfaceLogShoreLeave
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_shore_leave
// Runtime script:
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenSurfaceLogLeave

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogLeave ===
// rules.csv id: shipTrophyGanEdenSurfaceLogLeave
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_leave
// Runtime script:
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenSpaceElevatorRepelled

// ============================================================
=== rule_shipTrophyGanEdenSpaceElevatorRepelled ===
// rules.csv id: shipTrophyGanEdenSpaceElevatorRepelled
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD spaceElevatorRepels score:62000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"

The Space Elevator rises from the inner surface into the atmosphere above Gan Eden. Its upper terminus remains dark, but the structure itself is intact.

Your fleet begins a cautious approach.

Two strange Omega Shards emerge from the atmospheric glare. They cross the approach corridor without hailing, their overlapping drive fields building a wall of impossible vectors ahead of you.

Every attempt to advance turns into lateral acceleration. Dampers scream. Navigation gives ground before the fleet is thrown bodily into the elevator's outer superstructure.

The Shards hold until you retreat, then disappear back into the curve of the world.

+ [Withdraw.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenSpaceElevatorGuarded ===
// rules.csv id: shipTrophyGanEdenSpaceElevatorGuarded
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD spaceElevatorGuarded score:62000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"

The Space Elevator is exactly where the final beacon indicated. So are Cherubim and Lahat Haharev.

The two golden Shards hold the approach corridor in mirrored formation. They make no attempt to communicate. Every projected route to the elevator passes through them.

+ [Break off and face the Shards in open space.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenEpitaph ===
// rules.csv id: shipTrophyGanEdenEpitaph
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD shouldShowEpitaph score:60000
// Runtime script:
// GanEdenQuestCMD prepareEpitaph

With Cherubim and Lahat Ha-Cherev gone, the elevator's interdiction field is silent.

Isa's inherited suit transponder opens the outer doors.

The landing dock beyond is empty except for the wreck of a Kite shuttle, driven hard against one side of the platform. Its entire right wing was scorched away and much of the hull has been melted into slag by some enormous burst of energy. You have a fairly good idea which of the two constructs was responsible.

A dark trail begins beside the shattered cockpit. Nearly two centuries have reduced it to little more than a stain against the deck, but it continues away from the wreck and into the elevator, uneven and unmistakable.

Something dragged itself from the shuttle.

Your bodyguards and Isa follow the trail through the silent corridors toward the master control room. It ends in the hallway outside.

That is where Isaac Thomas Leicester is.

His remains lie against the wall, still sealed inside a vacuum suit. The helmet is cracked across the visor, and there is nothing visible within except darkness. An identification transponder on his back continues to pulse after all these years.

LEICESTER, ISAAC THOMAS.

Beside him lies a recording unit.

Isa sucks in a breath and drops to her knees beside the suit. Tears are already running down her face by the time she reaches toward him, stopping just short of touching the cracked helmet.

"The last log."
LOG — FINAL

+ [Open the final entry with Isa.] -> rule_shipTrophyGanEdenEpitaphFinalDynamic

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFour ===
// rules.csv id: shipTrophyGanEdenEpitaphFour
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_four
// Runtime script:
// GanEdenQuestCMD prepareEpitaphLog part_four

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: LOG — PART IV

The Domain arrested me for insurrection.

At trial, they presented my speeches, correspondence, and engineering reports. I denied very little. When the Sector Governor asked whether I recognized the absolute authority of the Domain of Man, I told him that all human authority was subordinate to God.

They found me guilty.

I was kept under house arrest at Telepylus Station while preparations were made to return me to Sol for sentencing. They still needed my knowledge of Gan Eden, and some of its oldest systems continued to recognize my credentials.

Finally, I decided to end it.

For the first time in centuries, I authorized my daughter’s revival. She was so small. I could nearly palm her in my hand. With hands shaking, I fashioned a small cradle from my old spacer suit. My only inheritence to my prodigal daughter. Then I re-suspended her. It felt like burying Rebecca all over again.

Then I set my plot in motion.

The hypershunts supplied Gan Eden through the Gate network. Their full output passed through the rings as energy before reaching the sphere’s master control system. I would sieze a craft, pass through the Penelope’s Star Gate, enter the control complex, and redirect the full output of both hypershunts into Gan Eden.

The Gan Eden would be destroyed, and I with it.

I escaped confinement, commandeered the transport, and changed its destination after the transit sequence began.

Telepylus security had already identified the ship, and I expected the escort to follow me through the Gate within seconds. Instead, the patrol craft broke formation before they reached the ring. Traffic control began issuing emergency orders over one another, several ships stopped answering, and the Gate-status displays failed in rapid succession.

I did not know what had drawn their attention away from me, only that the route to Gan Eden was still open. I entered the coordinates, drove the transport through at maximum thrust, and emerged into the restricted system without pursuit. Gan Eden’s control network accepted my credentials immediately. I seized the approach corridor and pushed the ship toward the master control complex before whatever was happening at Penelope’s Star could close the way behind me.

Oh, Hosanna. Hosaana in the higehest.

Despite its hubris and its separation from God, Gan Eden was the most beautiful place in the entire universe.

Once inside the system, I abandoned the transport and took a shuttle to the master control complex at the space elevator. For less than a second, the system behaved exactly as I had intended.

Then the Gate network began shutting down around me.

The conduits carrying the hypershunt output vanished before the power could reach Gan Eden. With nowhere else to go, the discharge collapsed back through the nearest ring still connected to the transfer system: Penelope’s Star.

I watched the impossibility of Adamantine shattering like clay over telemetry and break apart. A moment later, every other Gate disappeared from the network.

I intended to die in Gan Eden.

Instead, I was left alive inside it.

+ [Continue to the final entry.] -> rule_shipTrophyGanEdenEpitaphFinalDynamic

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFinalDynamic ===
// rules.csv id: shipTrophyGanEdenEpitaphFinalDynamic
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_five
// Runtime script:
// GanEdenQuestCMD startFinalLogMusic
// GanEdenQuestCMD showLogPage final 0

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenEpitaphFinalPageTwo

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFinalPageTwo ===
// rules.csv id: shipTrophyGanEdenEpitaphFinalPageTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_final_page_two
// Runtime script:
// GanEdenQuestCMD showLogPage final 1

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenEpitaphFinalPageThree

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFinalPageThree ===
// rules.csv id: shipTrophyGanEdenEpitaphFinalPageThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_final_page_three
// Runtime script:
// GanEdenQuestCMD showLogPage final 2

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenEpitaphFinalPageFour

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFinalPageFour ===
// rules.csv id: shipTrophyGanEdenEpitaphFinalPageFour
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_final_page_four
// Runtime script:
// GanEdenQuestCMD showLogPage final 3

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenEpitaphFinalPageFive

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFinalPageFive ===
// rules.csv id: shipTrophyGanEdenEpitaphFinalPageFive
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_final_page_five
// Runtime script:
// GanEdenQuestCMD showLogPage final 4

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenEpitaphFinalPageSix

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFinalPageSix ===
// rules.csv id: shipTrophyGanEdenEpitaphFinalPageSix
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_final_page_six
// Runtime script:
// GanEdenQuestCMD showLogPage final 5

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Close the log and stay with Isa.] -> rule_shipTrophyGanEdenEpitaphStay

// ============================================================
=== rule_shipTrophyGanEdenEpitaphFive ===
// rules.csv id: shipTrophyGanEdenEpitaphFive
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_five_legacy_unused
// Runtime script:
// GanEdenQuestCMD prepareEpitaphLog final

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: LOG — FINAL

I am dying.

I have lived alone in Gan Eden for thirty-six years.

I spent the first several years searching the cities, shelters, maintenance tunnels, and transit stations. I transmitted on every emergency frequency that still worked.

No one answered. I knew no one would. The last human technicians had left Gan Eden centuries before, when its construction passed entirely to the automated swarms. I was the one who signed off on that order.

I eventually settled in this place I named Galilee, repaired one of the old agricultural houses, and learned to farm. When I was not working, I wandered the shell. I walked through empty cities, forests planted by machines, and coastlines no human being had ever seen. It is a never-ending day here, as if it were before God separated Light from Darkness.

Gan Eden remained beautiful.

Oh, God.

I also continued trying to restore the Gate connection to Penelope’s Star. At first, I told myself I was trying to rescue my daughter. I admit now that I only want to know whether I killed her.

Oh, my daughter.

Rebecca wanted to name you Leah.

To Leah, my beautiful daughter I never knew:

I said I saved you for a future I believed I could build. That was only partly true.

Every physician told me you could be revived. Every report said your chamber was sound. But whenever I imagined authorizing the thaw, I saw Rebecca dying on the revival table. I kept you asleep because I was afraid.

I dressed that fear in duty and chased my arrogant dream across centuries. Then, when the world failed to become what I envisioned, I abandoned you to your fate.

Neither you nor God owe me any forgiveness.

I prayed for you every day. I prayed for Rebecca. I prayed for the dead, and for the living I had condemned without ever knowing their names.

I do not know whether God heard me.

Until today.

Today, God finally answered.

It did not connect to Penelope’s Star or any other Gate I knew. Instead, two angels arrived through the Gate and began descending toward Gan Eden.

The first resembles a winged figure, though my instruments cannot hold its shape for more than a moment. The second turns within lines of fire like a burning sword. They burn with the light of God and race through the sky like searing chariots.

I have named them Cherubim and the Lahat Ha-cherev.

Surely the Lord has sent them down here as gatekeepers of a false paradise. I foolishly attempted to pass by their contempous gaze, but God sees all.

Leah, I hope someone found you.

I hope they gave you a life beyond mine, and a name that belonged to you. I hope you were raised by people who loved you without asking you to carry their grief, their faith, or their failures. I hope that when you looked toward the future, you saw something of your own choosing rather than the ruins of what I had planned for you.

I hope you laughed every day. I hope you were stubborn like Rebecca. I hope you were kinder than I became. I hope you found love in this world, children of your own, and a home. I hope you found the blessings of God beneath these infinite heavens.

I hope that one day you know this:

I love you, Leah.

Isaac Thomas Leicester.

+ [Close the log and stay with Isa.] -> rule_shipTrophyGanEdenEpitaphStay

// ============================================================
=== rule_shipTrophyGanEdenEpitaphStay ===
// rules.csv id: shipTrophyGanEdenEpitaphStay
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_stay
// Runtime script:
// GanEdenQuestCMD finishEpitaphLogs
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"
// SetTextHighlightColors hColor hColor
// SetTextHighlights "[Recovered Log — Final.]" "[Filed under Gan Eden Archives in Intel.]"

The final log ends.

[Recovered Log — Final.]
[Filed under Gan Eden Archives in Intel.]

The elevator observation deck looks out across the impossible inward horizon. Isa says nothing for a long time.

Then she begins telling Isaac Thomas Leicester about the Shattered Ring: the wreck-farms, the revival ward, the terrible food, and every impossible ship that carried her farther than he could have imagined.

She tells him about the people who raised her, and the people she raised in turn. She tells him about her company and friends and the people that have woven themselves into her life.

She tells her about you. How you took her in. Raised her to the stars. How you gave her a new home, a new family, a new future.

No answer comes from the empty world. This time, she does not seem to need one.

+ [Talk with Isa about what happened.] -> rule_shipTrophyGanEdenEpitaphTalk

// ============================================================
=== rule_shipTrophyGanEdenEpitaphTalk ===
// rules.csv id: shipTrophyGanEdenEpitaphTalk
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_talk
// Runtime script:
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"
// SetTextHighlightColors story
// SetTextHighlights "[You can speak with Isa about Isaac Leicester through her contact menu.]"

You remain beside Isa at the observation glass.

"I kept thinking I'd reach the end and find out who I was supposed to be," she says. "Leah. Isaac's daughter. The Continuity Office's last loose end."

She looks down at her grease-stained hands.

"But I already knew who I was. I just didn't know I was allowed to keep her."

Below, the Tree of Life turns slowly beneath the inward sun.

"Gan Eden gets a future," Isa says. "So do I."

[You can speak with Isa about Isaac Leicester through her contact menu.]

+ [Return to the fleet.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenEpitaphLeave ===
// rules.csv id: shipTrophyGanEdenEpitaphLeave
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_leave
// Runtime script:
// GanEdenQuestCMD markEpitaph
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenEpitaphNeedsIsa

// ============================================================
=== rule_shipTrophyGanEdenEpitaphNeedsIsa ===
// rules.csv id: shipTrophyGanEdenEpitaphNeedsIsa
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD epitaphNeedsIsa score:55000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"

The elevator's continuity archive recognizes Isa Leicester's inherited suit transponder, but she is not present to answer its identity challenge. You will need to return with Isa in the fleet.

+ [Leave.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenEpitaphInspected ===
// rules.csv id: shipTrophyGanEdenEpitaphInspected
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD epitaphInspected score:55000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"
// FireAll ShipTrophyGanEdenLureOptions

The silent Space Elevator remains open above Gan Eden. Its final Leicester record is secure in the fleet archives.

+ [Leave.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenLureOption ===
// rules.csv id: shipTrophyGanEdenLureOption
// Trigger:
// ShipTrophyGanEdenLureOptions
// Conditions:
// GanEdenQuestCMD canLureGoldenOmega
// Runtime script:
// SetStoryOption ship_trophy_gan_eden_lure_omega 1 shipTrophyGanEdenLureGoldenOmega technology "Lured the Golden Omega back to Gan Eden"

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Lure the Golden Omega back.] -> rule_shipTrophyGanEdenLureOmega

// ============================================================
=== rule_shipTrophyGanEdenLureOmega ===
// rules.csv id: shipTrophyGanEdenLureOmega
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_lure_omega
// Runtime script:
// GanEdenQuestCMD lureGoldenOmega
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"

Isa wakes the Space Elevator's continuity transmitter and feeds it a deliberately malformed guardian challenge.

The answer arrives before the signal has finished propagating. Two golden drive signatures ignite beyond the atmospheric curve. Around them, a newly reconstructed escort formation resolves from the glare.

The Golden Omega are coming.

+ [Return to the fleet.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenSurfaceSiteCanColonize ===
// rules.csv id: shipTrophyGanEdenSurfaceSiteCanColonize
// Trigger:
// PrintSystemCutOffText
// Conditions:
// $market.shipTrophyGanEdenSurfaceSite score:100000

Although Gan Eden is sealed from hyperspace, this fixed shell site is connected to self-contained atmosphere, power, and transit infrastructure. Establishing a colony here remains possible.

+ [Continue.] -> rule_shipTrophyGanEdenSurfaceSiteIgnoresGuardians

// ============================================================
=== rule_shipTrophyGanEdenSurfaceSiteIgnoresGuardians ===
// rules.csv id: shipTrophyGanEdenSurfaceSiteIgnoresGuardians
// Trigger:
// PrintNearbySurveyHostilesTextIfSo
// Conditions:
// $market.shipTrophyGanEdenSurfaceSite score:100000

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyIsaMainGanEden

// ============================================================
=== rule_shipTrophyIsaMainGanEden ===
// rules.csv id: shipTrophyIsaMainGanEden
// Trigger:
// ShipTrophyIsaMainOptions
// Conditions:
// GanEdenQuestCMD questCompleted

// No literal text in rules.csv; the runtime script supplies this beat.

+ ["...so. Isaac Leicester. He was Ludd."] -> rule_shipTrophyIsaGanEdenHub

// ============================================================
=== rule_shipTrophyIsaGanEdenHub ===
// rules.csv id: shipTrophyIsaGanEdenHub
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"Yeah."

Isa leans back in her chair.

For once, she does not immediately have something clever to add.

+ ["How are you processing that?"] -> rule_shipTrophyIsaGanEdenProcess
+ ["I never knew the Prophet had a daughter."] -> rule_shipTrophyIsaGanEdenLuddic
+ ["Should I curtsey and call you princess now?"] -> rule_shipTrophyIsaGanEdenPrincess

// ============================================================
=== rule_shipTrophyIsaGanEdenProcess ===
// rules.csv id: shipTrophyIsaGanEdenProcess
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_process

Isa exhales.

"Badly? Weirdly?"

She turns her slate over in her hands.

"I spent most of my life wondering if Isaac Leicester was my father. I figured maybe he was some spacer. Engineer, if I was lucky. Maybe somebody who got caught in the Collapse and shoved his kid into the safest cryopod he could find."

She gives a small shrug.

"Turns out he built the hypershunts, built Gan Eden, started a religious revolution, disappeared through a Gate, and spent the rest of his life alone inside a world the size of a solar system."

A pause.

"So I've adjusted my expectations a little."

+ ["Does it change anything?"] -> rule_shipTrophyIsaGanEdenProcessChange
+ ["You seem to be taking it pretty well."] -> rule_shipTrophyIsaGanEdenProcessWell

// ============================================================
=== rule_shipTrophyIsaGanEdenProcessChange ===
// rules.csv id: shipTrophyIsaGanEdenProcessChange
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_process_change

"About me?"

Isa shakes her head.

"No."

Then she thinks about it.

"I mean, yes. Obviously. I'm going to be unpacking this until I'm eighty."

She taps her chest.

"But I'm still me."

A faint smile.

"Isa. Chief Engineer. Occasional genius. Terrible influence on your bodyguards."

+ [Continue.] -> rule_shipTrophyIsaGanEdenEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenProcessWell ===
// rules.csv id: shipTrophyIsaGanEdenProcessWell
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_process_well

"I'm not."

Isa smiles.

"I'm just very talented."

She lets that sit for a beat.

"At engineering. This part I'm improvising."

+ [Continue.] -> rule_shipTrophyIsaGanEdenEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenLuddic ===
// rules.csv id: shipTrophyIsaGanEdenLuddic
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_luddic

Isa looks down at her slate.

"Neither did anybody else."

For a moment, she says nothing.

"You read the same logs I did."

+ ["He lived the life of a saint."] -> rule_shipTrophyIsaGanEdenSaint

// ============================================================
=== rule_shipTrophyIsaGanEdenSaint ===
// rules.csv id: shipTrophyIsaGanEdenSaint
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_saint

Isa looks at you.

"Even after all that?"

+ ["Saints are still human."] -> rule_shipTrophyIsaGanEdenSaintHuman

// ============================================================
=== rule_shipTrophyIsaGanEdenSaintHuman ===
// rules.csv id: shipTrophyIsaGanEdenSaintHuman
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_saint_human

Isa is quiet for a while.

"Yeah."

She turns the slate over in her hands.

Then she looks at you.

"So what happens to your faith now?"

+ ["I have questions."] -> rule_shipTrophyIsaGanEdenFaithQuestions
+ ["My faith is unshaken."] -> rule_shipTrophyIsaGanEdenFaithUnshaken
+ ["I want to know the God that Ludd knew."] -> rule_shipTrophyIsaGanEdenFaithGod

// ============================================================
=== rule_shipTrophyIsaGanEdenFaithQuestions ===
// rules.csv id: shipTrophyIsaGanEdenFaithQuestions
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_faith_questions

Isa gives you a small smile.

"I figured."

A pause.

"About him?"

+ ["About everything."] -> rule_shipTrophyIsaGanEdenFaithEverything

// ============================================================
=== rule_shipTrophyIsaGanEdenFaithEverything ===
// rules.csv id: shipTrophyIsaGanEdenFaithEverything
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_faith_everything

She nods.

"Probably healthy."

A pause.

"Dad could've used more of those."

+ [Continue.] -> rule_shipTrophyIsaGanEdenLuddicEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenFaithUnshaken ===
// rules.csv id: shipTrophyIsaGanEdenFaithUnshaken
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_faith_unshaken

Isa studies your face.

"Really?"

+ ["I believed in God, not in Isaac Leicester."] -> rule_shipTrophyIsaGanEdenFaithUnshakenGod

// ============================================================
=== rule_shipTrophyIsaGanEdenFaithUnshakenGod ===
// rules.csv id: shipTrophyIsaGanEdenFaithUnshakenGod
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_faith_unshaken_god

That seems to surprise her.

Then she smiles.

"Okay."

She looks down at the slate again.

"I think Dad would've liked that answer."

+ [Continue.] -> rule_shipTrophyIsaGanEdenLuddicEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenFaithGod ===
// rules.csv id: shipTrophyIsaGanEdenFaithGod
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_faith_god

Isa goes very still.

Not offended. Not amused.

Just listening.

+ ["Whatever he saw, it made him believe every person mattered."] -> rule_shipTrophyIsaGanEdenFaithGodAnswer

// ============================================================
=== rule_shipTrophyIsaGanEdenFaithGodAnswer ===
// rules.csv id: shipTrophyIsaGanEdenFaithGodAnswer
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_faith_god_answer

Isa looks away.

"He believed that right to the end."

A long pause.

"Even when he couldn't believe it about himself."

She presses her thumb against the edge of the slate.

"If you find Him..."

A faint smile.

"Ask Him what He saw in my dad."

+ [Continue.] -> rule_shipTrophyIsaGanEdenLuddicEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenLuddicEnd ===
// rules.csv id: shipTrophyIsaGanEdenLuddicEnd
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_luddic_end_continue

Isa leans back in her chair.

"I still don't know what I'm supposed to call him in my head."

She looks toward you.

"Prophet sounds ridiculous."

A beat.

"Dad feels worse."

+ [Continue.] -> rule_shipTrophyIsaGanEdenEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenPrincess ===
// rules.csv id: shipTrophyIsaGanEdenPrincess
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_princess

Isa stares at you.

"You know, I've been wondering something."

She snaps her fingers.

Wei and Yvan kick you at the same time.

+ ["Et tu, Wei?"] -> rule_shipTrophyIsaGanEdenPrincessWei

// ============================================================
=== rule_shipTrophyIsaGanEdenPrincessWei ===
// rules.csv id: shipTrophyIsaGanEdenPrincessWei
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_princess_wei

Wei looks away.

Yvan does not.

Isa folds her arms.

"Anything else, Commander?"

+ ["Your Highness."] -> rule_shipTrophyIsaGanEdenPrincessAgain
+ ["No, ma'am."] -> rule_shipTrophyIsaGanEdenPrincessSurrender

// ============================================================
=== rule_shipTrophyIsaGanEdenPrincessAgain ===
// rules.csv id: shipTrophyIsaGanEdenPrincessAgain
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_princess_again

Isa snaps her fingers again.

You attempt to flee.

Your bodyguards are faster.

+ [Continue.] -> rule_shipTrophyIsaGanEdenEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenPrincessSurrender ===
// rules.csv id: shipTrophyIsaGanEdenPrincessSurrender
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_princess_surrender

"Good."

Isa smiles sweetly.

"You're learning."

+ [Continue.] -> rule_shipTrophyIsaGanEdenEnd

// ============================================================
=== rule_shipTrophyIsaGanEdenEnd ===
// rules.csv id: shipTrophyIsaGanEdenEnd
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_isaac_end_continue

For a little while, neither of you says anything.

Whatever history eventually decides Isaac Thomas Leicester was, Isa seems content to leave him there.

Her father.

// Runtime destination outside this volume: shipTrophyIsaContactOpen
+ [Back to the Hall ledgers.] -> END
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
// END OF RULES.CSV EXPORT
// ============================================================
