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
+ [Review Isa post-quest conversations.] -> rule_shipTrophyIsaGanEdenHub
+ [End preview.] -> END

// ============================================================
=== rule_shipTrophyGanEdenGoldenEncounter ===
// rules.csv id: shipTrophyGanEdenGoldenEncounter
// Trigger:
// BeginFleetEncounter
// Conditions:
// GanEdenQuestCMD isGoldenOmegaFightable score:90000
// Runtime script:
// FleetDesc
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

+ [Continue.] -> rule_shipTrophyGanEdenExternalRing

// ============================================================
=== rule_shipTrophyGanEdenExternalRing ===
// rules.csv id: shipTrophyGanEdenExternalRing
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD isExternalRing score:50000
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
// GanEdenQuestCMD prepareSurfaceLog
// SetShortcut ship_trophy_gan_eden_surface_log_leave "ESCAPE"

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Recover the surviving record.] -> rule_shipTrophyGanEdenSurfaceLogRecover
+ [Leave the archive sealed.] -> rule_shipTrophyGanEdenSurfaceLogLeave

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

+ [Close and file the recovered log.] -> rule_shipTrophyGanEdenSurfaceLogFile

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogFile ===
// rules.csv id: shipTrophyGanEdenSurfaceLogFile
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_file
// Runtime script:
// GanEdenQuestCMD recoverSurfaceLog
// SetTextHighlightColors hColor hColor
// SetTextHighlights "Epitaph — Part IV" "Gan Eden Archives"

[Recovered Epitaph — Part IV.]
[Filed under Gan Eden Archives in Intel.]

+ [Continue.] -> rule_shipTrophyGanEdenSurfaceLogContinue

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogContinue ===
// rules.csv id: shipTrophyGanEdenSurfaceLogContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_continue
// Runtime script:
// SetTextHighlightColors hColor hColor story
// SetTextHighlights "Space Elevator" "Cherubim and Lahat Haharev" "[Objective updated: Defeat Cherubim and Lahat Haharev and reach the Space Elevator.]"

Isa closes the archive, but another alert is already unfolding across her slate.

"One more active beacon." She expands a second vector. It rises from the inner surface toward the Space Elevator. "That has to be where he went after the Gate failed."

The two golden signatures return at the edge of the tactical display. This time they do not withdraw. Cherubim and Lahat Haharev turn together and begin closing on your fleet, as if they sensed your intent.

[Objective updated: Defeat Cherubim and Lahat Haharev and reach the Space Elevator.]

+ [Return to the fleet.] -> rule_shipTrophyGanEdenSurfaceLogReturn

// ============================================================
=== rule_shipTrophyGanEdenSurfaceLogReturn ===
// rules.csv id: shipTrophyGanEdenSurfaceLogReturn
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_surface_log_return
// Runtime script:
// FireBest OpenInteractionDialog

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

With Cherubim and Lahat Haharev gone, the elevator's interdiction field is silent.

The wreck of a one-man shuttle lies embedded in the elevator's lower approach works. A narrow, glass-smooth channel has opened it from nose to engine. The cut matches the geometry of Lahat's great beam, though no surviving record confirms who fired.

The cockpit is empty. Scuffed handprints and a trail of dried suit sealant lead from it through an emergency hatch.

Isa's inherited suit transponder opens the hatch. An internal lift climbs through kilometers of dead infrastructure before jamming one level below master control.

Isaac Thomas Leicester lies in the hallway beyond, still sealed inside his pressure suit. He died against the wall within sight of the control-room doors. A portable recorder remains locked between his gauntlets.

The recorder accepts the four recovered records as an authorization chain. A fifth file unlocks:

EPITAPH — FINAL

+ [Play Isaac's final recording.] -> rule_shipTrophyGanEdenEpitaphFinalDynamic

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
FILE: EPITAPH — PART IV

The Domain arrested me for insurrection.

At trial, they presented my speeches, correspondence, and engineering reports. I denied very little. When the Sector Governor asked whether I recognized the absolute authority of the Domain of Man, I told him that all human authority was subordinate to God.

They found me guilty.

I was kept under house arrest at Telepylus Station while preparations were made to return me to Sol for sentencing. They still needed my knowledge of Gan Eden, and some of its oldest systems continued to recognize my credentials.

Finally, I decided to end it.

The hypershunts supplied Gan Eden through the Gate network. Their full output passed through the rings as energy before reaching the sphere’s master control system. I intended to seize a transport, pass through the Penelope’s Star Gate, enter the control complex, and redirect the full output of both hypershunts into Gan Eden.

The sphere would be destroyed. I intended to die with it.

I escaped confinement, commandeered the transport, and changed its destination after the transit sequence began.

Telepylus security had already identified the ship, and I expected the escort to follow me through the Gate within seconds. Instead, the patrol craft broke formation before they reached the ring. Traffic control began issuing emergency orders over one another, several ships stopped answering, and the Gate-status displays failed in rapid succession.

I did not know what had drawn their attention away from me, only that the route to Gan Eden was still open. I entered the coordinates, drove the transport through at maximum thrust, and emerged into the restricted system without pursuit. Gan Eden’s control network accepted my credentials immediately. I seized the approach corridor and pushed the ship toward the master control complex before whatever was happening at Penelope’s Star could close the way behind me.

Oh, hosanna. Hosaana in the higehest.

Despite its hubris and its separation from God, Gan Eden was the most beautiful place in the entire universe.

Once inside the system, I abandoned the transport and took a shuttle to the master control complex at the space elevator. For less than a second, the system behaved exactly as I had intended. Then the Gate network began shutting down around me.

The conduits carrying the hypershunt output vanished before the power could reach Gan Eden. With nowhere else to go, the discharge collapsed back through the nearest ring still connected to the transfer system: Penelope’s Star.

I watched the impossibility of Adamantine shattering like clay on the control display and break apart. A moment later, every other Gate disappeared from the network.

Sol was gone. Telepylus was gone. There was no route back.

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
FILE: EPITAPH — FINAL

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

Today, the Gate system finally answered.

It did not connect to Penelope’s Star or any other Gate I knew. Instead, two angels arrived through the Gate and began descending toward Gan Eden.

The first resembles a winged figure, though my instruments cannot hold its shape for more than a moment. The second turns within lines of fire like a burning sword. They burn with the light of God and race through the sky like searing chariots.

I have named them Cherubim and the Lahat Ha-cherev.

Surely the Lord has sent them down here as gatekeepers of a false paradise. Beneath their contemptuous gaze, I will be judged as I commit my soul to God.

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
// GanEdenQuestCMD markEpitaph
// SetTextHighlightColors hColor hColor
// SetTextHighlights "[Recovered Epitaph — Final.]" "[Filed under Gan Eden Archives in Intel.]"
// SetShortcut ship_trophy_gan_eden_epitaph_leave "ESCAPE"

The final log ends.

[Recovered Epitaph — Final.]
[Filed under Gan Eden Archives in Intel.]

Through the hallway windows, the impossible inward horizon curves above Isaac's final resting place. Isa says nothing for a long time.

Then she begins telling Isaac Thomas Leicester about the Shattered Ring: the wreck-farms, the revival ward, the terrible food, and every impossible ship that carried her farther than he could have imagined.

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

You remain beside Isa in the corridor outside the control room.

"I kept thinking I'd reach the end and find out who I was supposed to be," she says. "Leah. Isaac's daughter. The Continuity Office's last loose end."

She looks down at her grease-stained hands.

"But I already knew who I was. I just didn't know I was allowed to keep her."

Below, the Tree of Life turns slowly beneath the inward sun.

"Gan Eden gets a future," Isa says. "So do I."

[You can speak with Isa about each recovered log, the Golden Omega, and Gan Eden's future through her contact menu.]

+ [Return to the fleet.] -> rule_shipTrophyGanEdenEpitaphLeave

// ============================================================
=== rule_shipTrophyGanEdenEpitaphLeave ===
// rules.csv id: shipTrophyGanEdenEpitaphLeave
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_leave
// Runtime script:
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

+ [Use a story point to lure the Golden Omega back.] -> rule_shipTrophyGanEdenLureOmega

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

+ [Talk about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub

// ============================================================
=== rule_shipTrophyIsaGanEdenHub ===
// rules.csv id: shipTrophyIsaGanEdenHub
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

Isa sets aside the Hall ledgers. The light from Gan Eden's inward horizon is still reflected in the photographs she brought back.

+ [Talk about what happened.] -> rule_shipTrophyIsaGanEdenAfter
+ [Talk about Gan Eden's future.] -> rule_shipTrophyIsaGanEdenFuture
+ [Talk about Log I.] -> rule_shipTrophyIsaGanEdenLogOne
+ [Talk about Log II.] -> rule_shipTrophyIsaGanEdenLogTwo
+ [Talk about Log III.] -> rule_shipTrophyIsaGanEdenLogThree
+ [Talk about Log IV.] -> rule_shipTrophyIsaGanEdenLogFour
+ [Talk about Log V.] -> rule_shipTrophyIsaGanEdenLogFive
+ [Talk about the Golden Omega.] -> rule_shipTrophyIsaGanEdenOmega
// Runtime destination outside this volume: shipTrophyIsaContactOpen
+ [Back to the Hall ledgers.] -> END
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenAfter ===
// rules.csv id: shipTrophyIsaGanEdenAfter
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_after
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

Isa takes a while to answer.

"I spent my whole life wondering whether that name meant I belonged to somebody. Turns out it did. It also turns out belonging to someone doesn't make their choices yours."

She taps the nameplate on her slate: ISA LEICESTER.

"Isaac gave me a beginning. The Ring gave me a life. You lot gave me the rest. I can live with that."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenFuture ===
// rules.csv id: shipTrophyIsaGanEdenFuture
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_future
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"Gan Eden can't stay a mausoleum," Isa says. "But it shouldn't become another company town with a pretty sky, either."

She begins listing priorities on her fingers: survey teams, independent settlement charters, protected archives, strict limits on dismantling anything that still works.

"A place built for everyone ought to belong to the people willing to make a life there. We can help. We don't get to own it."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenLogOne ===
// rules.csv id: shipTrophyIsaGanEdenLogOne
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_log_1
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"The first log made him real," Isa says. "Not my father. Not yet. Just an engineer who thought a project big enough could justify anything it demanded of him."

She glances at the Hall ledgers. "I understand that temptation better than I'd like."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenLogTwo ===
// rules.csv id: shipTrophyIsaGanEdenLogTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_log_2
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"The second log hurt because it gave him a reason," Isa says. "Rebecca was supposed to wake with him. She died in the thaw, and after that he could never make himself open my pod."

Her expression hardens. "He called it caution for centuries because admitting he was afraid would have meant admitting what that fear cost me. A reason isn't an excuse. But it is a reason."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenLogThree ===
// rules.csv id: shipTrophyIsaGanEdenLogThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_log_3
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"He woke up often enough to watch the Domain become monstrous, but never long enough to build a life inside it," Isa says. "Then even Rebecca's face went. That's the part I keep coming back to."

She rubs her eyes. "Centuries of memory, and grief was the thing that lasted."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenLogFour ===
// rules.csv id: shipTrophyIsaGanEdenLogFour
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_log_4
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"By the fourth log, he was done asking the Domain to become kinder," Isa says. "He decided the only moral thing left was to destroy his life's work—and himself with it."

She shakes her head. "He was wrong about that too. Gan Eden deserved a future he couldn't imagine."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenLogFive ===
// rules.csv id: shipTrophyIsaGanEdenLogFive
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_log_5
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

Isa is quiet for several seconds.

"He called me Leah," she says at last. "I think he wanted to return one choice to me after making so many on my behalf."

She smiles faintly. "It's a beautiful name. It just isn't mine."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
=== rule_shipTrophyIsaGanEdenOmega ===
// rules.csv id: shipTrophyIsaGanEdenOmega
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_isa_gan_eden_omega
// Runtime script:
// SetShortcut ship_trophy_isa_close "ESCAPE"

"Isaac thought they were angels," Isa says. "Maybe Omega read that in his systems. Maybe they chose the shapes because they knew someone would understand the threat."

She pulls up the combat telemetry. "Cherubim guarded the threshold. Lahat guarded the sentence. Whatever they were built to protect, I don't think that fight was the last word."

+ [Keep talking about Gan Eden.] -> rule_shipTrophyIsaGanEdenHub
// Runtime destination outside this volume: shipTrophyIsaCloseIntel
+ [Cut the comm link.] -> END

// ============================================================
// END OF RULES.CSV EXPORT
// ============================================================
