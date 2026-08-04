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

=== gan_eden_quest ===
Quest dialogue

+ [station_shattered_ring] -> station_shattered_ring
+ [gan_eden_hypershunt_investigate] -> gan_eden_hypershunt_investigate
+ [gan_eden_hypershunt_guard] -> gan_eden_hypershunt_guard
+ [log two] -> log_two
+ [log three] -> log_three
+ [End preview] -> END

=== station_shattered_ring ===

Shattered Ring comes into view one broken section at a time.

Three vast arcs circle the barren moon below, each turning at a slightly different rate. Pressurized bridges, cargo gantries, and naked lengths of structural cable span some of the gaps. Others remain open to space, their severed ends capped with bulkheads scavenged from old ships.

The self-governing colony looks less constructed than prevented from falling apart. Isa appears beside the navigation station and plants both hands on your navigator’s desk.

“Take us around the dark side.”

Your navigator glances at the approach plot.

“Traffic beacon’s directing us sunward.”

“That traffic beacon’s been wrong since I was twelve.”

+ [Continue through the approach.] -> station_shattered_ring_approach

=== station_shattered_ring_approach ===

As if summoned by the insult, the comm channel crackles.

“Approaching fleet, inbound bearing zero-three-five, elevation minus one-two. Reduce velocity and prepare to receive docking procedures—”

Isa leans over the console.

“Arthur? Tell Garret it’s me.”

There is a long pause.

“Isaac?” another voice says. “Thought we were finally rid of you.”

“Call me that again and I’ll come up there and kick your butt all over comms.”

Garret laughs.

“Roger that. Take the locals’ approach queue. You know the way. Over.”

Isa closes the channel.

“Bay Fourteen,” she tells the navigator, pointing at a row of glowing beacons hidden behind one of the arcs.

+ [“Isaac?”] -> station_shattered_ring_isaac
+ [“You seem familiar with the place.”] -> station_shattered_ring_familiar

=== station_shattered_ring_isaac ===

Isa points a finger at you.

“Shut up.”

-> station_shattered_ring_docking_impact

=== station_shattered_ring_docking_impact ===

Bay Fourteen accepts your approaching fleet with a tremendous metallic cacophony.

The entire docking tube shudders as the clamps engage. Something heavy strikes the outer hull, tumbles away, and disappears beneath the berth.

Isa waits for the noise to stop.

“Perfect.”

Everyone on your bridge exchanges looks. The docking tube groans again. Your flagship shifts in its berth with a tremendous crunch.

“Even more perfect.”

+ [Continue into the concourse.] -> station_shattered_ring_docking

=== station_shattered_ring_familiar ===

“I grew up here.”

Isa studies the approaching station.

“Mostly in Arc Two. Arc One had the good machine shops, but their gravity used to cut out whenever the ore processor started.”

-> station_shattered_ring_docking_impact

=== station_shattered_ring_docking ===

By the time you reach the main concourse, word has spread.

Dockworkers call to Isa from the overhead gantries. A food vendor reaches across his counter to press a foil-wrapped pastry into her hand. Someone shouts that the recycler on Level Six is making the drinking water taste metallic again.

A child in an oversized pressure suit runs up and presents her with a cracked maneuvering-thruster valve. Isa crouches, turns it over in her hands, and tells him which seal needs replacing.

Before you have crossed half the concourse, one of your bodyguards is carrying a crate of machine parts for an elderly mechanic. Another is holding a ladder steady while Isa points out a flickering light panel for an electrician.

Nobody has asked who you are.

+ [“I see you’ve missed them too.”] -> station_shattered_ring_missed
+ [“Are we charging for these repairs?”] -> station_shattered_ring_charged

=== station_shattered_ring_missed ===

“I didn’t say I missed anybody.”

The light above her flickers twice, then steadies.

From the floor below, someone calls, “We missed you too, Isaac!”

Isa closes her eyes.

-> station_shattered_ring_foreman

=== station_shattered_ring_charged ===

Isa looks back from the ladder.

“You own a fleet. You’re basically royalty around here.”

She returns to the light panel.

“This is the oblige part.”

+ [“My favorite part of being royalty. Noblesse oblige.”] -> station_shattered_ring_foreman

=== station_shattered_ring_foreman ===

An old salvage foreman is waiting when Isa climbs down.

He wears a patched station utility suit, its original insignia hidden beneath decades of repairs. Under one arm, he carries a narrow metal case marked with faded cryogenic-handling symbols.

Isa’s good humor disappears.

“Where did you get that?”

“Found it behind the plating in Calder’s old storeroom,” the foreman says. “Station council was clearing out abandoned property.”

He holds the case toward her.

The people passing through the concourse give the three of you a wide berth. Finally, Isa wipes her hands against her trousers and accepts the case.

“Thanks.”

+ [Continue.] -> station_shattered_ring_foreman_detail

=== station_shattered_ring_foreman_detail ===

The foreman nods. Before leaving, he looks toward you.

“She was wrapped in that when we found her. I used it as a changing mat for the first few weeks, too.”

Isa throws a glove at the man.

“You didn’t have to tell {player_gender} that.”

“Why not? You weren’t going to.”

The old man laughs, then disappears into the crowd.

+ [Let Isa take her inheritance.] -> station_shattered_ring_inheritance

=== station_shattered_ring_inheritance ===

Isa closes the case and calls for one of your crew to transfer it to her old workshop.

[Received: Isa's inheritance.]

+ [Return to the Shattered Ring concourse.] -> station_shattered_ring_colony_menu

=== station_shattered_ring_colony_menu ===

SHATTERED RING COLONY MENU

+ [Investigate Isa's suit in her workshop.] -> station_shattered_ring_workshop

=== station_shattered_ring_workshop ===

Isa leads you through the station without speaking.

You descend through Arc Two, past hydroponics bays and crowded habitation decks, until the finished corridors give way to exposed conduits and old pressure doors.

The gravity weakens with each level.

Isa adjusts automatically, shortening her steps whenever the deck shifts beneath her. Your bodyguards are less graceful. One catches the overhead piping to avoid drifting into a wall.

At last, Isa opens an abandoned machine shop.

Several names have been carved into the pressure door. Hers is among them, scratched low enough that whoever wrote it must have been very young.

ISA LESESTER IS DA BEST.

+ [Open the case.] -> station_shattered_ring_workshop_case

=== station_shattered_ring_workshop_case ===

She sets the case on an old workbench. Inside is a child-sized bundle of pressure fabric, folded carefully beneath a transparent preservation sheet.

The material was once white. Radiation and age have yellowed it almost to brown. Several sections have been cut from a much larger suit and crudely flextaped together, turning the spacesuit into something more like a cradle.

A blackened name strip remains attached to the collar.

LEICESTER, ISAAC.

Isa touches two fingers to the transparent sheet.

“This is where they got it,” she says. “My name.”

+ [“You were named after the suit?”] -> station_shattered_ring_named

=== station_shattered_ring_named ===

“I’m a ‘pod person.’ They thawed me out of a cryopod when I was still a baby, all swaddled up in that.”

Isa gives you a smile.

“I went by the name on the label until I was old enough to realize ‘Isaac’ was a man’s name.”

+ [“Cryosuspension that young is unheard of.”] -> station_shattered_ring_scan

=== station_shattered_ring_scan ===

“My great claim to fame,” Isa says, smiling. “Youngest ‘pod person’ on my Arc.”

A small metal contact is visible beneath the scorched collar.

Isa stops smiling.

“That wasn’t there before.”

She releases the preservation seal and carefully lifts the suit onto the workbench. The fabric crackles as she turns the collar over, shedding small flecks of decayed plastic onto the surface below.

The frayed insulation has exposed an identification wafer embedded beneath the name strip.

Isa removes her slate.

On scan, her slate’s display splits into dozens of windows. Her delta-level AI agents begin testing ancient authentication protocols, interpolating damaged sectors, and comparing the wafer against surviving Domain registries.

“You don’t have to stay,” she says.

+ [“Let me know when you need me.”] -> station_shattered_ring_go
+ [“I don’t have to go.”] -> station_shattered_ring_stay

=== station_shattered_ring_go ===
Isa glances at you, then nods.

“Thanks, {player_title}.”

+ [“I’ll give you privacy. Wei, Yvan.”] -> station_shattered_ring_go_2

=== station_shattered_ring_go_2 ===

You walk out with your bodyguards, leaving Isa to it.

Outside the workshop, Shattered Ring creaks and groans around you. Pumps cycle behind the walls. Somewhere far below, a cargo lift begins its slow ascent through the arc.

After a few minutes, Isa calls from the workshop.

You had finally cornered Yvan in your chess match. It was going to be weeks before you rolled a Chess960 setup that good again.

+ [Continue.] -> station_shattered_id

=== station_shattered_ring_stay ===

Isa glances at you, then nods.

“Thanks, {player_title}.”

She returns to the slate.

Minutes pass.

Outside the workshop, Shattered Ring creaks and groans around you. Pumps cycle behind the walls. Somewhere far below, a cargo lift begins its slow ascent through the arc.

-> station_shattered_id

=== station_shattered_id ===

One of the windows on Isa’s slate turns green. A personal log opens.

PERSONAL LOG 1765
AUTHOR: LEICESTER, ISAAC THOMAS

My name is Dr. Isaac Thomas Leicester.

I am Director of Engineering for Heliostructural Systems in the Persean Sector. Head Architect of the Continuity Office.

My work included the construction of the coronal hypershunts and a later, far more important task.

Both projects were expected to take centuries. The latter might have required more than a thousand years before reaching its final design capacity. No ordinary succession of administrators could preserve the technical knowledge, institutional memory, and design intent required over such a span.

The Domain therefore established a Continuity Office.

Its directors would serve across generations, entering cryosuspension between major phases of construction and returning whenever the work required their judgment.

+ [Continue reading.] -> station_shattered_id_page_two

=== station_shattered_id_page_two ===

I was elected to lead it.

I would sleep through centuries of construction, wake for design reviews, inspect the work, and approve the next phase. Each waking lasted months, sometimes years.

The first hypershunt was already under construction when I accepted the appointment. It was intended to collect energy directly from a star and make that power available to automated construction systems across the Gate network.

The engineering difficulties were substantial. We lost dozens of assembly swarms before completing the first stable framework.

I met engineers whose grandparents had worked under people I remembered. I approved sweeping changes to systems designed by men and women who had died while I slept, sometimes erasing an entire lifetime’s work with a single decision.

Humanity marched on as I slept. Problems once considered impossible became trivial. When two hypershunts became operational, the Directorate approved the work they had been built to support.

+ [Continue reading.] -> station_shattered_id_page_three

=== station_shattered_id_page_three ===

It was to be the Domain’s first enclosed—

DOMAIN INFOSEC VIOLATION THRESHOLD WARNING
// datastream resetting...

—constructed around a yellow sun on the far outskirts of the Persean Sector. At its intended capacity, it could shelter several quintillion human li—

FATAL ACCESS ERROR //

This device is not authorized to provide additional description of the referenced project, destination, or design objective. Please upgrade your data permission level in compliance with Domain Information Security Standards.

I believed that the ability to construct such a wonder proved that the Domain deserved to continue expanding.

I was wrong.

+ [Close the log.] -> one_isa

=== one_isa ===

The log ends.

Isa remains motionless, staring at the author field.

LEICESTER, ISAAC THOMAS.

She looks down at the faded label inside the spacer suit.

Then back at the identification wafer.

“So he was real.”

She scrolls through the recovered file again.

“Director of Engineering. Architect of the Continuity Office. Hypershunts.”

+ [Continue.] -> one_receipt

=== one_receipt ===

[Recovered Personal Log 1765.]
[Filed under Gan Eden Archives in Intel.]

// Replace these conditions with the actual runtime flags.
+ {hypershunts_reactivated > 0}
    [“We’ve seen those before. They’re guarded by extremely dangerous automated ships. They call themselves ‘Omega.’”] -> station_shattered_ring_hypershunts_known

+ {hypershunts_reactivated == 0}
    [“Hypershunts?”] -> station_shattered_ring_hypershunts_unknown

=== station_shattered_ring_hypershunts_known ===

Isa looks up sharply.

“Omega? AI ships?”

She turns back to the log.

“What were you doing, Isaac...?”

-> station_shattered_ring_hypershunts_compare

=== station_shattered_ring_hypershunts_unknown ===

“Incredibly old, pre-Collapse technology,” Isa says. “They draw power directly from a star.”

She brings up the relevant records.

“Only two are known to have survived in the Sector.”

-> station_shattered_ring_hypershunts_compare

=== station_shattered_ring_hypershunts_compare ===

Isa scrolls back through the log.

“He oversaw both of them. He ran the office responsible for keeping the project alive across centuries.”

She taps the author field.

“If there are any records left of him, that’s where they’ll be.”

Isa finishes copying the surviving log and INFOSEC failures to her slate.

+ [Continue.] -> station_shattered_ring_hypershunts_request

=== station_shattered_ring_hypershunts_request ===

For a while, she says nothing. The old suit remains spread across the workbench between you, Isaac Leicester’s name blackened but still legible beneath the collar. Then she closes the file.

“Captain.”

Her voice has lost its earlier lightness.

“I know this isn’t fleet business. But somebody went to a lot of trouble to hide whatever he was building. I don’t think I’m going to get much further by myself.”

Isa looks directly at you.

“I want to find out who Isaac Leicester was. What happened to him. How his suit ended up wrapped around me.”

She hesitates.

“I’m also asking for your help.”

+ [“Our chief engineer’s business is fleet business.”] -> station_shattered_ring_accept_command
+ [“We’ll visit the hypershunts. We’ll see where they lead.”] -> station_shattered_ring_accept_cautious

=== station_shattered_ring_accept_command ===

Isa gives you a small, crooked smile.

“That a direct order?”

+ [“Don't make your commanding officer repeat themself.”] -> station_shattered_ring_accept_command_2

=== station_shattered_ring_accept_command_2 ===
She snaps a salute.

“Aye-aye, {player_title}.”

-> station_shattered_ring_prepare_search

=== station_shattered_ring_accept_cautious ===

“That’s all I’m asking.”

-> station_shattered_ring_prepare_search

=== station_shattered_ring_prepare_search ===

Isa transfers the recovered log, the wafer registry number, and the INFOSEC failures into a new directory.

The folder remains unnamed for several seconds.

Then she enters:

ISAAC THOMAS LEICESTER

[Quest started: A Borrowed Name]

[Objective updated: Investigate both surviving hypershunts.]

+ [Return to the fleet.] -> END




VAR guarding_faction = "pather"
// Valid preview values: "pather", "pirate"
VAR has_story_point = true

-> gan_eden_hypershunt_guard

=== gan_eden_hypershunt_guard ===

+ [pather] -> gan_eden_hypershunt_pather_intro

+ [pirate] -> gan_eden_hypershunt_pirate_intro

// ============================================================
// LUDDIC PATH
// ============================================================

=== gan_eden_hypershunt_pather_intro ===

A large Pather fleet holds position between you and the hypershunt.

Their commander answers your hail. He wears a scorched pressure suit marked with lines of handwritten scripture.

“This place is forbidden. Turn your fleet around.”

+ [“We only need access to the hypershunt’s records.”] -> gan_eden_hypershunt_pather_refusal
+ [“Move aside.”] -> gan_eden_hypershunt_pather_fight

=== gan_eden_hypershunt_pather_refusal ===

“Knowledge is another form of temptation.”

The commander shakes his head.

“You will not approach.”

+ {has_story_point} [Use a story point to speak to him as one of the faithful.] -> gan_eden_hypershunt_pather_persuade
+ [“Then we’ll go through you.”] -> gan_eden_hypershunt_pather_fight

=== gan_eden_hypershunt_pather_persuade ===
// Runtime effect: spend 1 story point

You lower your voice.

“The machine is not the object of our pilgrimage. We seek only what the servants of Moloch tried to bury within it.”

You speak of false wonders, poisoned knowledge, and the duty of the faithful to expose the sins of the old Domain without claiming its power for themselves.

The commander studies you for a long moment.

At last, he bows his head.

“Then go, brother. Cleanse the taint of Moloch from the heavens! For the Prophet!”

The Pather fleet begins clearing the approach corridor.

[Lost 1 story point.]

+ [Approach the hypershunt.] -> gan_eden_hypershunt_investigate

=== gan_eden_hypershunt_pather_fight ===

The commander’s expression hardens.

“Then may Moloch lay with your dead!”

The channel closes.

[The Luddic Path fleet moves to engage.]

+ [Engage.] -> gan_eden_hypershunt_pather_battle

=== gan_eden_hypershunt_pather_battle ===
// Runtime: begin battle against guarding Pather fleet

-> END
// ============================================================
// PIRATE HYPERSHUNT ENCOUNTER
// ============================================================

=== gan_eden_hypershunt_pirate_intro ===

A pirate fleet blocks the approach to the hypershunt.

Their commander answers your hail with their boots resting on the console.

“Nice machine, isn’t it? Shame you got here after we claimed it.”

+ [“We only need access to its records.”] -> gan_eden_hypershunt_pirate_price
+ [“Move your fleet.”] -> gan_eden_hypershunt_pirate_fight

=== gan_eden_hypershunt_pirate_price ===

“Sure. Records.”

The commander grins.

“You can have whatever you like after you pay the docking fee.”

+ [Pay 250,000 credits.] -> gan_eden_hypershunt_pirate_pay
+ [Use a story point to negotiate like a pirate.] -> gan_eden_hypershunt_pirate_persuade
+ [“We’ll pay in ordnance.”] -> gan_eden_hypershunt_pirate_fight

=== gan_eden_hypershunt_pirate_pay ===
// Runtime effect: deduct 250,000 credits

You authorize the transfer.

The pirate commander checks the amount, then finally takes their boots off the console.

“Pleasure doing business.”

The pirate fleet begins clearing the approach corridor.

“Try not to break anything expensive.”

[Lost 250,000 credits.]

+ [Approach the hypershunt.] -> gan_eden_hypershunt_investigate

=== gan_eden_hypershunt_pirate_persuade ===
// Runtime effect: spend 1 story point

You tell them they can charge whatever they like.

After your fleet docks.

Then you explain what happens to pirates who demand payment before the customer is surrounded, stationary, and attached to something fragile.

The commander slowly lowers their boots.

“Right.”

They glance toward someone outside the transmitter’s view.

“Professional courtesy.”

The pirate fleet clears the approach corridor.

“Go on through.”

[Lost 1 story point.]

+ [Approach the hypershunt.] -> gan_eden_hypershunt_investigate

=== gan_eden_hypershunt_pirate_fight ===

The commander takes their boots off the console.

“Wrong answer.”

The channel closes.

[The pirate fleet moves to engage.]

+ [Engage.] -> gan_eden_hypershunt_pirate_battle

=== gan_eden_hypershunt_pirate_battle ===
// Runtime: begin battle against the guarding pirate fleet

-> END


// ============================================================
// POST-ENCOUNTER
// ============================================================

=== gan_eden_hypershunt_investigate ===

With the approach corridor clear, your fleet closes on the hypershunt.

The structure grows across the forward display until it no longer resembles a machine. Black towers rise through the stellar corona, joined by collector vanes and transmission spines large enough to eclipse cities. Streams of plasma bend around it in slow, incandescent arches.

Isa stands beside the sensor station, slate in hand.

“We’ll need to map the whole thing.”

She begins assigning survey patterns across the fleet.

“Field geometry. Collector alignment. Residual phase harmonics. Anything the Domain tuned by hand.”

+ [Begin the survey.] -> gan_eden_hypershunt_records

=== gan_eden_hypershunt_records ===

Your fleet spends several days circling the hypershunt.

Sensor craft trace the curvature of its containment fields. Gravimetric probes measure distortions along transmission spines large enough to dwarf capital ships. Isa compares the results against surviving Domain engineering standards and the concealed routing data recovered at Shattered Ring.

Most of the structure follows standard automated tolerances.

Several sections do not.

Minute deviations recur across the hypershunt’s output: tiny fluctuations in phase, amplitude, and field alignment, repeated too precisely to be stellar interference.

Isa overlays several hours of readings.

The irregularities resolve into a carrier pattern.

DCR-2F38-CB017-6A
LEICESTER, ISAAC THOMAS
CONTINUITY AUTHORITY

Isa leans closer.

“That’s him.”

She isolates the signal from the hypershunt’s stellar noise. It is ancient, degraded, and still repeating through the transmission architecture.

“This isn’t telemetry.”

The carrier breaks apart into thousands of incomplete fragments. Isa’s agents begin reconstructing them, using the microchip from her suit as an authorization key.

A recording emerges.

+ [Play the recovered log.] -> log_two


=== log_two ===

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART II


Before the Continuity Office was established, I lived at Telepylus Station with my wife, Rebecca Anne Sarai.

Our daughter was born there.

Rebecca went to God the same day.

A vascular condition had been identified too late for treatment. By the time the doctors understood what was happening, they could save only one of them.

I had already been considered for the proposed Continuity Office. The appointment would require its director to spend centuries in cryosuspension, waking only when the projects required his judgment.

Before Rebecca’s death, I had intended to refuse.

+ [Continue reading.] -> log_two_page_two

=== log_two_page_two ===

With her gone, I thought I had no more reason to live in the present.

I placed our daughter in cryosuspension before I had properly named her. I accepted the appointment and promised myself that I would complete the work, resign, and wake her into the world Rebecca and I had intended for her.

A better world.

That was the arrangement I made with myself.

I visited my daughter during every waking.

I inspected her chamber. I reviewed the medical reports. I repeated tests the technicians had already completed.

+ [Continue reading.] -> log_two_page_three

=== log_two_page_three ===

She remained healthy and unchanged while centuries passed beyond the glass.

I told myself that she was safe.

That I had done right by her.

I hate myself.

I hate myself.

I hate myself—

-> log_two_isa


=== log_two_isa ===

The recording terminates abruptly.

Isa does not move.

The hypershunt burns across the forward display behind her, its black superstructure suspended within the stellar corona.

After a long silence, she looks down at the microchip connected to her slate.

“His daughter.”

She swallows.

“He put her in cryosuspension.”

The carrier signal continues repeating beneath the silence.

Isa scrolls back through the recovered fragments, reading the same lines again.

“He never named her.”

+ [“Isa...”] -> log_two_isa_suit

=== log_two_isa_suit ===

“I know.”

Isa grips the edge of her slate.

“The suit. His identification. This chip.”

She shakes her head.

“But my pod was opened centuries after this was recorded. I don’t know what happened between.”

Her eyes return to the author field.

“I don’t know why he put me there. Did he just abandon me? Put me to sleep and walk away?”

She stares at the carrier trace.

“Or was he trying to come back?”

-> log_two_routing


=== log_two_routing ===

Isa returns to the carrier data.

“The message is riding the same transmission geometry as the classified routing signal.”

She separates the two patterns. A narrow vector appears on the tactical display, extending away from the hypershunt and into unexplored space.

“This gives us direction.”

Isa enlarges the projection.

She looks toward the location of the remaining hypershunt.

“We need the other signal.”

[The first hypershunt routing vector has been recovered.]

+ [Return to the fleet.] -> END

=== log_three ===

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART III

My daughter remained unchanged.

The Domain did not.

At first, the differences between my waking periods seemed incidental. Then I began to understand. The Domain had grown beyond any human capacity to comprehend it. It could move fleets across the galaxy, dismantle planets, and build cities beneath alien suns.

It had become incapable of seeing a human being standing directly before it.

I met a Domain Armada veteran cleaning industrial residue from a station floor. During one of the Domain’s thousands of civil wars, radiation from a reactor leak had destroyed half his face.

+ [Continue reading.] -> log_three_page_two

=== log_three_page_two ===

His pension had been suspended because the archive containing his service record no longer existed.

I offered to help.

He asked only that I help his son obtain a transit permit.

Seventy-two years passed before my next waking. I never learned what happened to either of them.

During another waking period, I met a woman who had spent years moving between ports because she could not obtain employment without proof of residence, or residence without proof of employment.

The station classified her as a transient clearance burden. She was slated for prison soon.

A burden.

Years blurred together. I do not know whether it was the repeated cryosuspensions or the deterioration of my own mind, but eventually I could no longer distinguish faces.

+ [Continue reading.] -> log_three_page_three

=== log_three_page_three ===

I forgot the faces of people I had worked beside.

I even forgot Rebecca’s face.

I remembered the facts of her: the scar at her wrist, the hymn she hummed while she worked, the way she squeezed my hand when she was frightened. But whenever I tried to assemble those memories into a face, there was only an absence where my wife had been.

I began speaking publicly.

At first, I presented reports and projections. I documented administrative failures and proposed reforms. The Directorate thanked me for my service and established commissions, councils, and legislative bodies.

I kept drifting through the centuries. Nothing changed.

I began speaking through my faith.

I said that human beings were not obsolete machinery.

+ [Continue reading.] -> log_three_page_four

=== log_three_page_four ===

I said that a civilization should be judged by those it possessed the power to help and chose not to.

I said that every person carried the image of God, that even if the Domain could not place each one within its stars, God knew each and every one of them as He knit them in their wombs.

People began gathering to hear me.

The gatherings became demonstrations.

Security forces were deployed, people were jailed, injured and killed.

I continued speaking, although I no longer knew whether I was helping them or merely driving them to their deaths.

With my mouth, I drove lambs toward the slaughter.

With my hands and my work, I forged the knives the Domain plunged into their necks.

Who was I?

No one.

I was a worthless hypocrite.

I spoke against the Domain of Man, this great Whore of Babylon, while I remained her greatest architect.

-> log_three_isa

=== log_three_isa ===

The recording ends.

Isa remains fixed on the last line.

Nothing changed.

The hypershunt’s signal begins repeating beneath it, carrying the words back into the stellar corona.

Isa stops the playback.

“He forgot her face.”

She says it quietly.

“Mom’s.”

Her fingers remain poised above the slate.

+ [“Centuries of cryosuspension damaged him.”] -> log_three_isa_damaged
+ [“He tried to change things.”] -> log_three_isa_tried

=== log_three_isa_damaged ===

“I know.”

Isa rubs at one eye. She looks back at the frozen image of Isaac’s authorization header.

“He spent all that time building a future for his daughter, and by the end he couldn’t even remember the woman he was building it for.”

+ [Continue.] -> log_three_isa_continue


=== log_three_isa_tried ===

“He did.”

Isa looks at the final line again. She exhales through her nose.

“I don’t know whether he’s confessing or asking whoever hears this to forgive him.”

A pause.

“Maybe he didn’t know either.”

+ [Continue.] -> log_three_isa_continue

=== log_three_isa_continue ===

Isa returns to the carrier data.

The second hypershunt’s signal contains the same concealed routing pattern as the first, shifted by centuries of stellar drift and accumulated error.

She aligns the two vectors.

They intersect far beyond the charted systems of the Sector.

A destination marker appears.

Isa stares at them.

“That’s where he sent this from.”

She enlarges the projection.

“And whatever he helped build is still there.”

[The location of the Power Transit Gate has been determined.]



+ [Set a course.] -> master_power_transit_gate

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
// Return-visit music: cycles Lonesome Journey, the complete Log V cue, and æ™‚ãŒçµ‚ã‚ã‚Šã«å°Žã„ã¦; combat temporarily takes priority.

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

"He decided the only moral thing left was to destroy his life's workâ€”and himself with it. He was wrong about that too. Gan Eden deserved a future he couldn't imagine."

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

=== one ===

PERSONAL LOG 1765
AUTHOR: LEICESTER, ISAAC THOMAS

My name is Dr. Isaac Thomas Leicester.

I am Director of Engineering for Heliostructural Systems in the Persean Sector. Head Architect of the Continuity Office.

My work included the construction of the coronal hypershunts and a later, far more important task.

Both projects were expected to take centuries. The latter might have required more than a thousand years before reaching its final design capacity. No ordinary succession of administrators could preserve the technical knowledge, institutional memory, and design intent required over such a span.

The Domain therefore established a Continuity Office.

Its directors would serve across generations, entering cryosuspension between major phases of construction and returning whenever the work required their judgment.

+ [Continue reading.] -> station_shattered_id_page_two

=== station_shattered_id_page_two ===

I was elected to lead it.

I would sleep through centuries of construction, wake for design reviews, inspect the work, and approve the next phase. Each waking lasted months, sometimes years.

The first hypershunt was already under construction when I accepted the appointment. It was intended to collect energy directly from a star and make that power available to automated construction systems across the Gate network.

The engineering difficulties were substantial. We lost dozens of assembly swarms before completing the first stable framework.

I met engineers whose grandparents had worked under people I remembered. I approved sweeping changes to systems designed by men and women who had died while I slept, sometimes erasing an entire lifetime’s work with a single decision.

Humanity marched on as I slept. Problems once considered impossible became trivial. When two hypershunts became operational, the Directorate approved the work they had been built to support.

+ [Continue reading.] -> station_shattered_id_page_three

=== station_shattered_id_page_three ===

It was to be the Domain’s first enclosed—

DOMAIN INFOSEC VIOLATION THRESHOLD WARNING
// datastream resetting...

—constructed around a yellow sun on the far outskirts of the Persean Sector. At its intended capacity, it could shelter several quintillion human li—

FATAL ACCESS ERROR //

This device is not authorized to provide additional description of the referenced project, destination, or design objective. Please upgrade your data permission level in compliance with Domain Information Security Standards.

I believed that the ability to construct such a wonder proved that the Domain deserved to continue expanding.

I was wrong.

+ [Close the log.]

-> END


=== two ===

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART II


Before the Continuity Office was established, I lived at Telepylus Station with my wife, Rebecca Anne Sarai.

Our daughter was born there.

Rebecca went to God the same day.

A vascular condition had been identified too late for treatment. By the time the doctors understood what was happening, they could save only one of them.

I had already been considered for the proposed Continuity Office. The appointment would require its director to spend centuries in cryosuspension, waking only when the projects required his judgment.

Before Rebecca’s death, I had intended to refuse.

+ [Continue reading.] -> log_two_page_two

=== log_two_page_two ===

With her gone, I thought I had no more reason to live in the present.

I placed our daughter in cryosuspension before I had properly named her. I accepted the appointment and promised myself that I would complete the work, resign, and wake her into the world Rebecca and I had intended for her.

A better world.

That was the arrangement I made with myself.

I visited my daughter during every waking.

I inspected her chamber. I reviewed the medical reports. I repeated tests the technicians had already completed.

+ [Continue reading.] -> log_two_page_three

=== log_two_page_three ===

She remained healthy and unchanged while centuries passed beyond the glass.

I told myself that she was safe.

That I had done right by her.

I hate myself.

I hate myself.

I hate myself—

-> END


=== three ===

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART III

My daughter remained unchanged.

The Domain did not.

At first, the differences between my waking periods seemed incidental. Then I began to understand. The Domain had grown beyond any human capacity to comprehend it. It could move fleets across the galaxy, dismantle planets, and build cities beneath alien suns.

It had become incapable of seeing a human being standing directly before it.

I met a Domain Armada veteran cleaning industrial residue from a station floor. During one of the Domain’s thousands of civil wars, radiation from a reactor leak had destroyed half his face.

+ [Continue reading.] -> log_three_page_two

=== log_three_page_two ===

His pension had been suspended because the archive containing his service record no longer existed.

I offered to help.

He asked only that I help his son obtain a transit permit.

Seventy-two years passed before my next waking. I never learned what happened to either of them.

During another waking period, I met a woman who had spent years moving between ports because she could not obtain employment without proof of residence, or residence without proof of employment.

The station classified her as a transient clearance burden. She was slated for prison soon.

A burden.

Years blurred together. I do not know whether it was the repeated cryosuspensions or the deterioration of my own mind, but eventually I could no longer distinguish faces.

+ [Continue reading.] -> log_three_page_three

=== log_three_page_three ===

I forgot the faces of people I had worked beside.

I even forgot Rebecca’s face.

I remembered the facts of her: the scar at her wrist, the hymn she hummed while she worked, the way she squeezed my hand when she was frightened. But whenever I tried to assemble those memories into a face, there was only an absence where my wife had been.

I began speaking publicly.

At first, I presented reports and projections. I documented administrative failures and proposed reforms. The Directorate thanked me for my service and established commissions, councils, and legislative bodies.

I kept drifting through the centuries. Nothing changed.

I began speaking through my faith.

I said that human beings were not obsolete machinery.

+ [Continue reading.] -> log_three_page_four

=== log_three_page_four ===

I said that a civilization should be judged by those it possessed the power to help and chose not to.

I said that every person carried the image of God, that even if the Domain could not place each one within its stars, God knew each and every one of them as He knit them in their wombs.

People began gathering to hear me.

The gatherings became demonstrations.

Security forces were deployed, people were jailed, injured and killed.

I continued speaking, although I no longer knew whether I was helping them or merely driving them to their deaths.

With my mouth, I drove lambs toward the slaughter.

With my hands and my work, I forged the knives the Domain plunged into their necks.

Who was I?

No one.

I was a worthless hypocrite.

I spoke against the Domain of Man, this great Whore of Babylon, while I remained her greatest architect.

-> END


=== four ===

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — PART IV


The Domain arrested me for insurrection.

At trial, they presented my speeches, correspondence, and engineering reports. I denied very little.

The Sector Governor asked whether I recognized the absolute authority of the Domain of Man.

I told him that all human authority was subordinate to God.

They found me guilty.

I was kept under house arrest at Telepylus Station while preparations were made to return me to Sol for sentencing. They still needed my knowledge of Eden. Some of its oldest systems continued to recognize my credentials, and no living engineer other than I understood the complete design.

Finally, I decided to end it.

The hypershunts supplied Gan Eden through the Gate network. Their output passed through the rings as energy before reaching the sphere’s distribution systems.

I intended to seize a transport, pass through the Penelope’s Star Gate, enter Gan Eden, and redirect the full output of all hypershunts into the structure.

The sphere would be destroyed.

I intended to die with it.

I escaped confinement and commandeered a transport. Telepylus security identified the ship before I reached the Gate.

I expected patrol craft to follow me through within seconds.

Instead, they broke formation before reaching the ring.

Traffic control began issuing contradictory emergency orders. Ships stopped answering. Gate-status displays failed across the station.

I did not know what had drawn their attention away from me, only that the route to Gan Eden remained open.

I entered the Gan-Eden coordinates and drove the transport through the Penelope's Star Gate at maximum thrust.

I emerged into the restricted system without pursuit.

Gan Eden stood around its sun.

Oh, hosanna.

Hosanna in the highest.

Despite its hubris and its separation from God, it was the most beautiful thing humanity had ever made.

Its shell crossed the heavens from horizon to horizon. Clouds turned beneath me over continents no human feet had touched. Rivers followed courses plotted centuries before their waters existed.

For one moment, I loved it again. But it had to die.

I abandoned the transport and took a shuttle to the master distribution complex at the space elevator. The system accepted my credentials.

I opened the hypershunt transfer channels and commanded their full output into the sphere.

For less than a second, the system behaved exactly as I intended.

Then the Gate network began shutting down.

The transfer paths vanished before the power could reach Gan Eden. With nowhere else to go, the discharge collapsed backward through the nearest ring still connected to the network.

Penelope’s Star.

I watched from telemetric displays as the impossible happened; great adamantine arcs shatter like clay.

The ring broke apart across the control display. A moment later, every other Gate vanished from the network.

Sol was gone.

Telepylus was gone.

My daughter was gone.

There was no route back.

I had entered Gan Eden intending to die.

Instead, I was imprisoned inside it.

-> END
=== five ===

RECOVERED PERSONAL LOG
AUTHOR: LEICESTER, ISAAC THOMAS
FILE: EPITAPH — FINAL


I am dying.

I have lived alone in Gan Eden for fifty-eight years.

I spent the first years searching its cities, shelters, maintenance tunnels, and transit stations.

I transmitted on every emergency frequency that still functioned.

No one answered.

I knew no one would.

The last human technicians had departed centuries before, when construction passed entirely to the automated swarms.

I was the one who signed the order.

I eventually settled in a place I named Galilee.

I repaired one of the agricultural houses and learned to farm. When I was not working, I wandered the shell.

I walked through empty cities.

I crossed forests planted by machines.

I stood beside coastlines no human being had ever seen.

There is no true night here. The sun remains fixed above the inner world, and darkness comes only beneath clouds or within the shadow of the great structures.

It is as though I live in the time before God separated Light from Darkness.

Gan Eden remained beautiful.

Oh, God.

I continued trying to restore the Gate connection to Penelope’s Star.

At first, I told myself I was trying to rescue my daughter.

I admit now that I wanted only to know whether I had killed her.

When the Gate would not answer, I turned to the hypershunt transfer system.

The channels were never designed to carry human communications, but power and information are not so different as we pretend. I impressed these records upon the carrier harmonics and sent them into whatever remained of the network.

For years, I received nothing in return.

I do not know whether any part of this account escaped Gan Eden.

I do not know whether these words will survive me.

Oh, my daughter.

Rebecca wanted to name you Leah.

To Leah, my beautiful daughter whom I never knew:

I saved you for a future I believed I could build. I kept you asleep while I pursued that arrogant dream across centuries. Then, when the world failed to become what I had imagined, I abandoned you to your fate.

Neither you nor God owes me forgiveness.

I prayed for you every day.

I prayed for Rebecca.

I prayed for the dead, and for the living I condemned without ever knowing their names.

I did not know whether God heard me.

Until today, the Gate answered.

It did not connect to Penelope’s Star or to any Gate I recognized.

Instead, two angels came through.

The first resembles a winged figure, though my instruments cannot hold its shape for more than a moment.

The second turns within lines of fire like a burning sword.

They descend through the sky with the light of God around them, swift as searing chariots.

I have named them Cherubim and Lahat Ha-cherev.

Surely the Lord has sent them as gatekeepers of this false paradise.

Beneath their contemptuous gaze, I will be judged as I commit my soul to God.

Leah, I hope someone found you.

I hope they gave you a life beyond mine, and a name that belonged to you.

I hope you were raised by people who loved you without asking you to carry their grief, their faith, or their failures.

I hope that when you looked toward the future, you saw something of your own choosing rather than the ruins of what I had planned for you.

I hope you laughed every day.

I hope you were stubborn like Rebecca.

I hope you were kinder than I became.

I hope you found love in this world, children of your own, and a home.

I hope you found the blessings of God beneath these infinite heavens.

I hope that one day you know this.

I love you, Leah.

Isaac Thomas Leicester.

-> master_epilogue

// ============================================================
// END OF GAN EDEN QUEST MASTER
// ============================================================
