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

Sensor craft trace the curvature of its containment fields. Gravimetric probes measure distortions along the transmission spines. Isa’s agents compare the results against surviving Domain engineering standards and the concealed routing data recovered at Shattered Ring.

Most of the structure follows standard automated tolerances.

Several sections do not.

Tiny deviations recur across the hypershunt’s oldest assemblies: corrections too consistent to be random, repeated through construction phases separated by centuries.

Isa overlays them.

A familiar authorization pattern emerges from the accumulated calibration data.

DCR-2F38-CB017-6A  
LEICESTER, ISAAC THOMAS  
CONTINUITY AUTHORITY

Isa leans closer.

“That’s him.”

The same pattern appears in the hypershunt’s transmission geometry, attached to a routing vector concealed beneath SUPER ALABASTER restriction.

[Hypershunt routing data recovered.]

+ [Return to the fleet.] -> END