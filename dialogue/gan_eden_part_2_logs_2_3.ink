// Hall of Triumph - A Borrowed Name / Gan Eden
// Part II - The Coronal Hypershunts
//
// GENERATED PROOFREADING COPY. data/campaign/rules.csv is the
// sole dialogue authority used by this file. No other Ink file is
// read, imported, concatenated, or referenced by the generator.
// Runtime option labels below are emitted as real Ink choices.
//
// Scope: Both hypershunt encounters and Logs II-III.

-> volume_index

=== volume_index ===
Part II - The Coronal Hypershunts

+ [Begin the hypershunt investigation.] -> rule_shipTrophyGanEdenHypershuntPatherGuardEncounter
+ [Review the pirate blockade.] -> rule_shipTrophyGanEdenHypershuntPirateGuard
+ [Read the complete second log.] -> rule_shipTrophyGanEdenEpitaphTwo
+ [Read the complete third log.] -> rule_shipTrophyGanEdenEpitaphThree
+ [End preview.] -> END

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPatherGuardEncounter ===
// rules.csv id: shipTrophyGanEdenHypershuntPatherGuardEncounter
// Trigger:
// BeginFleetEncounter
// Conditions:
// GanEdenQuestCMD isHypershuntGuard luddic_path score:70000
// Runtime script:
// FleetDesc
// HailPlayer

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntPirateGuardEncounter

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPirateGuardEncounter ===
// rules.csv id: shipTrophyGanEdenHypershuntPirateGuardEncounter
// Trigger:
// BeginFleetEncounter
// Conditions:
// GanEdenQuestCMD isHypershuntGuard pirates score:70000
// Runtime script:
// FleetDesc
// HailPlayer

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntPatherGuard

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPatherGuard ===
// rules.csv id: shipTrophyGanEdenHypershuntPatherGuard
// Trigger:
// OpenCommLink
// Conditions:
// GanEdenQuestCMD isHypershuntGuard luddic_path score:70000
// Runtime script:
// GanEdenQuestCMD prepareHypershuntGuard
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

A large Pather fleet holds position between you and the hypershunt.

Their commander answers your hail. He wears a scorched pressure suit marked with lines of handwritten scripture.

"This place is forbidden. Turn your fleet around."

+ ["We only need access to the hypershunt’s records."] -> rule_shipTrophyGanEdenHypershuntPatherRefusal
+ ["Move aside."] -> rule_shipTrophyGanEdenHypershuntPatherFight
+ [Withdraw.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPatherRefusal ===
// rules.csv id: shipTrophyGanEdenHypershuntPatherRefusal
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pather_records
// Runtime script:
// SetStoryOption ship_trophy_gan_eden_hypershunt_pather_persuade 1 shipTrophyGanEdenPatherPersuade technology "Persuaded the Pather blockade to stand aside"
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

"Knowledge is another form of temptation."

The commander shakes his head.

"You will not approach."

+ [Use a story point to speak to him as one of the faithful.] -> rule_shipTrophyGanEdenHypershuntPatherPersuade
+ ["Then we’ll go through you."] -> rule_shipTrophyGanEdenHypershuntPatherFight
+ [Withdraw.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPatherPersuade ===
// rules.csv id: shipTrophyGanEdenHypershuntPatherPersuade
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pather_persuade
// Runtime script:
// GanEdenQuestCMD clearHypershuntGuard

You lower your voice.

"The machine is not the object of our pilgrimage. We seek only what the servants of Moloch tried to bury within it."

You speak of false wonders, poisoned knowledge, and the duty of the faithful to expose the sins of the old Domain without claiming its power for themselves.

The commander studies you for a long moment.

At last, he bows his head.

"Then go, brother. Cleanse the taint of Moloch from the heavens! For the Prophet!"

The Pather fleet begins clearing the approach corridor.

+ [Return to your fleet.] -> rule_shipTrophyGanEdenHypershuntStandDown

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPatherFight ===
// rules.csv id: shipTrophyGanEdenHypershuntPatherFight
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pather_fight
// Runtime script:
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

The commander’s expression hardens.

"Then may Moloch lay with your dead!"

The channel closes.

[The Luddic Path fleet moves to engage.]

+ [Engage.] -> rule_shipTrophyGanEdenHypershuntEngage
+ [Withdraw.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPirateGuard ===
// rules.csv id: shipTrophyGanEdenHypershuntPirateGuard
// Trigger:
// OpenCommLink
// Conditions:
// GanEdenQuestCMD isHypershuntGuard pirates score:70000
// Runtime script:
// GanEdenQuestCMD prepareHypershuntGuard
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

A pirate fleet blocks the approach to the hypershunt.

Their commander answers your hail with their boots resting on the console.

"Nice machine, isn’t it? Shame you got here after we claimed it."

+ ["We only need access to its records."] -> rule_shipTrophyGanEdenHypershuntPiratePrice
+ ["Move your fleet."] -> rule_shipTrophyGanEdenHypershuntPirateFight
+ [Withdraw.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPiratePrice ===
// rules.csv id: shipTrophyGanEdenHypershuntPiratePrice
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pirate_records
// Runtime script:
// SetStoryOption ship_trophy_gan_eden_hypershunt_pirate_persuade 1 shipTrophyGanEdenPiratePersuade technology "Negotiated professional courtesy from the pirate blockade"
// FireAll ShipTrophyGanEdenHypershuntPiratePayOptions
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

"Sure. Records."

The commander grins.

"You can have whatever you like after you pay the docking fee."

+ [Use a story point to negotiate like a pirate.] -> rule_shipTrophyGanEdenHypershuntPiratePersuade
+ ["We’ll pay in ordnance."] -> rule_shipTrophyGanEdenHypershuntPirateFight
+ [Withdraw.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPiratePayOption ===
// rules.csv id: shipTrophyGanEdenHypershuntPiratePayOption
// Trigger:
// ShipTrophyGanEdenHypershuntPiratePayOptions
// Conditions:
// GanEdenQuestCMD canPayHypershuntPirates

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Pay 250,000 credits.] -> rule_shipTrophyGanEdenHypershuntPiratePay

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPiratePay ===
// rules.csv id: shipTrophyGanEdenHypershuntPiratePay
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pirate_pay
// Runtime script:
// GanEdenQuestCMD payHypershuntPirates
// SetTextHighlightColors bad
// SetTextHighlights "[Lost 250,000 credits.]"

You authorize the transfer.

The pirate commander checks the amount, then finally takes their boots off the console.

"Pleasure doing business."

The pirate fleet begins clearing the approach corridor.

"Try not to break anything expensive."

[Lost 250,000 credits.]

+ [Return to your fleet.] -> rule_shipTrophyGanEdenHypershuntStandDown

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPiratePersuade ===
// rules.csv id: shipTrophyGanEdenHypershuntPiratePersuade
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pirate_persuade
// Runtime script:
// GanEdenQuestCMD clearHypershuntGuard

You tell them they can charge whatever they like.

After your fleet docks.

Then you explain what happens to pirates who demand payment before the customer is surrounded, stationary, and attached to something fragile.

The commander slowly lowers their boots.

"Right."

They glance toward someone outside the transmitter’s view.

"Professional courtesy."

The pirate fleet clears the approach corridor.

"Go on through."

+ [Return to your fleet.] -> rule_shipTrophyGanEdenHypershuntStandDown

// ============================================================
=== rule_shipTrophyGanEdenHypershuntPirateFight ===
// rules.csv id: shipTrophyGanEdenHypershuntPirateFight
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_pirate_fight
// Runtime script:
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

The commander takes their boots off the console.

"Wrong answer."

The channel closes.

[The pirate fleet moves to engage.]

+ [Engage.] -> rule_shipTrophyGanEdenHypershuntEngage
+ [Withdraw.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntEngage ===
// rules.csv id: shipTrophyGanEdenHypershuntEngage
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_engage
// Runtime script:
// GanEdenQuestCMD engageHypershuntGuard
// MakeOtherFleetHostile shipTrophyGanEden true
// MakeOtherFleetAggressiveOnce shipTrophyGanEden true
// EndConversation

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntStandDown

// ============================================================
=== rule_shipTrophyGanEdenHypershuntStandDown ===
// rules.csv id: shipTrophyGanEdenHypershuntStandDown
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_stand_down
// Runtime script:
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntTapInvestigate

// ============================================================
=== rule_shipTrophyGanEdenHypershuntTapInvestigate ===
// rules.csv id: shipTrophyGanEdenHypershuntTapInvestigate
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD canInvestigateHypershunt score:65000
// Runtime script:
// GanEdenQuestCMD prepareHypershuntInvestigation
// FireAll ShipTrophyGanEdenHypershuntInvestigate

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntTapBlocked

// ============================================================
=== rule_shipTrophyGanEdenHypershuntTapBlocked ===
// rules.csv id: shipTrophyGanEdenHypershuntTapBlocked
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD isBlockedHypershunt score:64000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

A large armed fleet holds the approach corridor. Any attempt to reach the hypershunt will have to deal with the blockade first.

+ [Leave.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntTapSurveyed ===
// rules.csv id: shipTrophyGanEdenHypershuntTapSurveyed
// Trigger:
// OpenInteractionDialog
// Conditions:
// GanEdenQuestCMD isSurveyedHypershunt score:63000
// Runtime script:
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

Isa’s survey markers remain distributed across the hypershunt. Its concealed routing record has already been recovered.

+ [Leave.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntInvestigate ===
// rules.csv id: shipTrophyGanEdenHypershuntInvestigate
// Trigger:
// ShipTrophyGanEdenHypershuntInvestigate

With the approach corridor clear, your fleet closes on the hypershunt.

The structure grows across the forward display until it no longer resembles a machine. Black towers rise through the stellar corona, joined by collector vanes and transmission spines large enough to eclipse cities. Streams of plasma bend around it in slow, incandescent arches.

Isa stands beside the sensor station, slate in hand.

"A Hypershunt... The Engineering of the Gods."

"We’ll need to map the whole thing."

She begins assigning survey patterns across the fleet.

"Field geometry. Collector alignment. Residual phase harmonics. Anything the Domain tuned by hand."

+ [Begin the survey.] -> rule_shipTrophyGanEdenHypershuntRecords

// ============================================================
=== rule_shipTrophyGanEdenHypershuntRecords ===
// rules.csv id: shipTrophyGanEdenHypershuntRecords
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_survey

Your fleet spends several days circling the hypershunt.

Sensor craft trace the curvature of its containment fields. Gravimetric probes measure distortions along the transmission spines. Isa’s agents compare the results against surviving Domain engineering standards and the concealed routing data recovered at Shattered Ring.

Most of the structure follows standard automated tolerances.

Several sections do not.

+ [Compare the deviations.] -> rule_shipTrophyGanEdenHypershuntRecordsContinue

// ============================================================
=== rule_shipTrophyGanEdenHypershuntRecordsContinue ===
// rules.csv id: shipTrophyGanEdenHypershuntRecordsContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_records_continue
// Runtime script:
// SetTextHighlightColors "82,88,94,255" "82,88,94,255" "82,88,94,255"
// SetTextHighlights "DCR-2F38-CB017-6A" "LEICESTER, ISAAC THOMAS" "CONTINUITY AUTHORITY"

Tiny deviations recur across the hypershunt’s oldest assemblies: corrections too consistent to be random, repeated through construction phases separated by centuries.

Isa overlays them.

A familiar authorization pattern emerges from the accumulated calibration data.

DCR-2F38-CB017-6A
LEICESTER, ISAAC THOMAS
CONTINUITY AUTHORITY

Isa leans closer.

"That’s him."

The same pattern appears in the hypershunt’s transmission geometry, attached to a routing vector concealed beneath SUPER ALABASTER restriction.

[A sealed personal log is embedded beside the routing data.]

+ [Open the recovered personal log.] -> rule_shipTrophyGanEdenHypershuntReadLog

// ============================================================
=== rule_shipTrophyGanEdenHypershuntReadLog ===
// rules.csv id: shipTrophyGanEdenHypershuntReadLog
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_read_log
// Runtime script:
// GanEdenQuestCMD surveyHypershunt
// FireAll ShipTrophyGanEdenHypershuntSurveyResult

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntSurveyPending

// ============================================================
=== rule_shipTrophyGanEdenHypershuntSurveyPending ===
// rules.csv id: shipTrophyGanEdenHypershuntSurveyPending
// Trigger:
// ShipTrophyGanEdenHypershuntSurveyResult
// Conditions:
// GanEdenQuestCMD hypershuntSurveyPending
// Runtime script:
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenHypershuntLogTwoPageTwo

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogTwoPageTwo ===
// rules.csv id: shipTrophyGanEdenHypershuntLogTwoPageTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_two_page_two
// Runtime script:
// GanEdenQuestCMD showLogPage part_two 1

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenHypershuntLogTwoPageThree

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogTwoPageThree ===
// rules.csv id: shipTrophyGanEdenHypershuntLogTwoPageThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_two_page_three
// Runtime script:
// GanEdenQuestCMD showLogPage part_two 2

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Close the recovered log.] -> rule_shipTrophyGanEdenHypershuntLogTwoResponse

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogTwoResponse ===
// rules.csv id: shipTrophyGanEdenHypershuntLogTwoResponse
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_two_response

The recording terminates abruptly.

Isa does not move.

The hypershunt burns across the forward display behind her, its black superstructure suspended within the stellar corona.

After a long silence, she looks down at the microchip connected to her slate.

"His daughter."

She swallows.

"They were supposed to wake her together."

Her eyes move back over the revival report.

"Rebecca died, and he never opened the other pod."

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntLogTwoResponseContinue

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogTwoResponseContinue ===
// rules.csv id: shipTrophyGanEdenHypershuntLogTwoResponseContinue
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_two_response_continue

The carrier signal continues repeating beneath the silence.

Isa scrolls back through the recovered fragments, reading the same lines again.

"He never named her."

"I know," she says before you can answer.

Isa grips the edge of her slate.

"The suit. His identification. This chip."

She shakes her head.

"But my pod was opened centuries after this was recorded. I don’t know what happened between. Did he mean to leave me sleeping forever? Was he ever going to stop being afraid?"

She stares at the carrier trace.

"Or was he trying to come back?"

+ [Continue.] -> rule_shipTrophyGanEdenHypershuntLogTwoRouting

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogTwoRouting ===
// rules.csv id: shipTrophyGanEdenHypershuntLogTwoRouting
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_two_routing
// Runtime script:
// SetTextHighlightColors hColor hColor
// SetTextHighlights "Epitaph — Part II" "Gan Eden Archives"

Isa returns to the carrier data.

"The message is riding the same transmission geometry as the classified routing signal."

She separates the two patterns. A narrow vector appears on the tactical display, extending away from the hypershunt and into unexplored space.

"This gives us direction."

Isa enlarges the projection and looks toward the location of the remaining hypershunt.

"We need the other signal."

[Recovered Epitaph — Part II.]
[Filed under Gan Eden Archives in Intel.]

[The first hypershunt routing vector has been recovered.]

+ [Return to the fleet.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntSurveyComplete ===
// rules.csv id: shipTrophyGanEdenHypershuntSurveyComplete
// Trigger:
// ShipTrophyGanEdenHypershuntSurveyResult
// Conditions:
// GanEdenQuestCMD hypershuntSurveyComplete

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenHypershuntLogThreePageTwo

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogThreePageTwo ===
// rules.csv id: shipTrophyGanEdenHypershuntLogThreePageTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_three_page_two
// Runtime script:
// GanEdenQuestCMD showLogPage part_three 1

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenHypershuntLogThreePageThree

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogThreePageThree ===
// rules.csv id: shipTrophyGanEdenHypershuntLogThreePageThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_three_page_three
// Runtime script:
// GanEdenQuestCMD showLogPage part_three 2

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue reading.] -> rule_shipTrophyGanEdenHypershuntLogThreePageFour

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogThreePageFour ===
// rules.csv id: shipTrophyGanEdenHypershuntLogThreePageFour
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_three_page_four
// Runtime script:
// GanEdenQuestCMD showLogPage part_three 3

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Close the recovered log.] -> rule_shipTrophyGanEdenHypershuntLogThreeResponse

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLogThreeResponse ===
// rules.csv id: shipTrophyGanEdenHypershuntLogThreeResponse
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_log_three_response
// Runtime script:
// SetTextHighlightColors hColor hColor
// SetTextHighlights "Epitaph — Part III" "Gan Eden Archives"

The recording ends.

Isa remains fixed on the last line.

Nothing changed.

The hypershunt’s signal begins repeating beneath it, carrying the words back into the stellar corona.

Isa stops the playback.

"He forgot her face."

She says it quietly.

"Mom’s."

Her fingers remain poised above the slate.

[Recovered Epitaph — Part III.]
[Filed under Gan Eden Archives in Intel.]

// Runtime destination outside this volume: shipTrophyGanEdenLogThreeDamaged
+ ["Centuries of cryosuspension damaged him."] -> END
// Runtime destination outside this volume: shipTrophyGanEdenLogThreeTried
+ ["He tried to change things."] -> END

// ============================================================
=== rule_shipTrophyGanEdenHypershuntSurveyCompleteFinal ===
// rules.csv id: shipTrophyGanEdenHypershuntSurveyCompleteFinal
// Trigger:
// ShipTrophyGanEdenHypershuntSurveyCompleteFinal
// Runtime script:
// SetShortcut ship_trophy_gan_eden_hypershunt_leave "ESCAPE"
// SetTextHighlightColors story story
// SetTextHighlights "[The location of Power Transit Gate - Gan Eden has been determined.]" "[Objective updated: Find Isaac Leicester.]"

Isa returns to the carrier data.

The second hypershunt’s signal contains the same concealed routing pattern as the first, shifted by centuries of stellar drift and accumulated error.

She aligns the two vectors.

They intersect far beyond the charted systems of the Sector, near its northeastern edge. There is no stable jump point; reaching it will require a Transverse Jump.

A destination marker appears.

Isa stares at it.

"That’s where he sent this from."

She enlarges the projection.

"And whatever he helped build is still there."

[The location of Power Transit Gate - Gan Eden has been determined.]

[Objective updated: Find Isaac Leicester.]

+ [Set a course.] -> rule_shipTrophyGanEdenHypershuntLeave

// ============================================================
=== rule_shipTrophyGanEdenHypershuntLeave ===
// rules.csv id: shipTrophyGanEdenHypershuntLeave
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_hypershunt_leave
// Runtime script:
// DismissDialog

// No literal text in rules.csv; the runtime script supplies this beat.

+ [Continue.] -> rule_shipTrophyGanEdenEpitaphTwo

// ============================================================
=== rule_shipTrophyGanEdenEpitaphTwo ===
// rules.csv id: shipTrophyGanEdenEpitaphTwo
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_two
// Runtime script:
// GanEdenQuestCMD prepareEpitaphLog part_two

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART II

Before the continuity office was established, I lived at Telepylus Station with my wife, Rebecca Anne Sarai.

Rebecca was a senior engineer in stellar-transfer architecture. The Directorate offered us joint appointments: I as Director of the continuity office, Rebecca as its Chief Integration Engineer.

We intended to serve the entire tenure together, entering cryosuspension between construction phases and waking side by side whenever the work required us.

Our daughter was born shortly before the first long suspension interval. We placed her pediatric chamber beside our own, intending to wake and raise her during every active interval.

At the first scheduled revival, I woke. Rebecca did not survive the thaw.

Our daughter’s chamber remained stable. The physicians told me the pediatric thaw could proceed safely. I refused.

I told myself that one death proved the protocols were not ready. I ordered more studies. At the next waking, I refused again.

I accepted sole leadership, promising that I would finish the work and wake my daughter into the world Rebecca and I had intended for her. Every report said she remained healthy. Every specialist told me she could be revived.

I saw Rebecca dying on the thawing table and left my daughter behind the glass.

I hate myself.

I hate myself.

I hate myself—

+ [Continue to Part III.] -> rule_shipTrophyGanEdenEpitaphThree

// ============================================================
=== rule_shipTrophyGanEdenEpitaphThree ===
// rules.csv id: shipTrophyGanEdenEpitaphThree
// Trigger:
// DialogOptionSelected
// Conditions:
// $option == ship_trophy_gan_eden_epitaph_three
// Runtime script:
// GanEdenQuestCMD prepareEpitaphLog part_three

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART III

My daughter remained unchanged. I made certain of it.

At every waking, the physicians offered to revive her. At every waking, I refused. I visited her chamber, reviewed the medical reports, and repeated tests the technicians had already completed while centuries passed outside.

The Domain did not remain unchanged.

I saw a decay. The soul of Man gangrenous and rotting.

I met an Armada veteran cleaning coolant residue from a station floor. A reactor leak had destroyed half his face. His pension had been suspended because the archive containing his service record no longer existed.

I offered to help. He asked only that I help his daughter obtain a transit permit.

Seventy-two years passed. I never found out what happened to him or his daughter.

Nothing changed.

During another waking period, I met a woman who had spent years moving between ports because she could not obtain employment without proof of residence, or residence without proof of employment. The station classified her as a transient clearance burden.

Years blurred together. I can't tell if it was the repeated cryosuspensions or my deteriorating mind, but at some point I couldn't distinguish faces. I even forgot Rebecca's face.

I remembered the facts of her: the scar at her wrist, the hymn she hummed while she worked, the way she squeezed my hand when she was frightened. But whenever I tried to assemble those memories into a face, there was only an absence where my wife had been.

Rebecca.

You understood this before I did. You once told me that the Domain was the greatest work of man that had ever been made, and that was why it was so grotesque.

I began speaking publicly. At first, I presented reports and projections. The Directorate thanked me and established a commission.

Nothing changed.

I said that human beings were not obsolete machinery. I said that a civilization should be judged by those it could help and chose not to. I said that every person carried the image of God, whether or not the Domain could locate the correct record for them.

People began gathering to hear me. The gatherings became demonstrations. Security forces were deployed. People were injured, then killed.

I continued speaking, but I don't know why. With my mouth I drove lambs to the slaughter, and with my hands and my work I forged the knives the Domain of Man plunged into their necks.

Who was I?

No one. I was a worthless hypocrite.

I spoke out against the great Whore of Babylon whilst I remained her greatest slave.

Nothing changed.

// Runtime destination outside this volume: shipTrophyGanEdenEpitaphFour
+ [Continue to Part IV.] -> END

// ============================================================
// END OF RULES.CSV EXPORT
// ============================================================
