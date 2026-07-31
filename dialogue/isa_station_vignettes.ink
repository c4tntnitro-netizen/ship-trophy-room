// Hall of Triumph - Isa Leicester station vignettes
// Standalone proofreading and Inky preview source reconstructed from the
// current runtime dialogue in data/campaign/rules.csv.
//
// Runtime remains authoritative. Java owns eligibility, one-time state,
// dynamic titles, mechanical rewards, and optional-mod safety.
// Shared runtime routing rules:
//   shipTrophyIsaFactionVisitReply
//   shipTrophyIsaFactionVisitContinue
//
// Vanilla faction IDs:
//   hegemony, persean, tritachyon, sindrian_diktat, luddic_church,
//   luddic_path, pirates, independent
// Optional integrations:
//   Knights of Ludd - mod knights_of_ludd, faction knights_of_selkie
//   Iron Shell      - mod timid_xiv, faction ironshell
//
// Text in {braces} stands in for values supplied dynamically by the game.

VAR market_name = "the station"
VAR player_title = "Captain"

-> station_vignette_menu

=== station_vignette_menu ===
ISA STATION VIGNETTES

+ [Hegemony] -> station_hegemony
+ [Persean League] -> station_league
+ [Tri-Tachyon] -> station_tritachyon
+ [Sindrian Diktat] -> station_diktat
+ [Luddic Church] -> station_church
+ [Luddic Path] -> station_path
+ [Pirates] -> station_pirates
+ [Independent] -> station_independent
+ [Knights of Ludd (optional)] -> station_knights
+ [Iron Shell (optional)] -> station_ironshell
+ [End preview] -> END

=== station_hegemony ===
// Runtime rule: shipTrophyIsaFactionVisitHegemony

As your fleet joins {market_name}'s approach queue, Isa disappears from the bridge. You find her pressed face-first against the glass of an observation blister, watching Hegemony-pattern ships parade one by one down a parallel departure queue. Her slate displays a hologram of each passing vessel, all of them carefully logged and annotated.

"Look at that armor profile," she says without turning. "Every Hegemony ship looks like it expects the universe to sucker-punch it, so it makes sure to swing first. Strike hard, strike true, strike only once." She nods approvingly. "Decisive Battle Doctrine."

You nod toward the security overhang crowning the docks.

+ "You're making friends with customs." -> station_hegemony_customs

=== station_hegemony_customs ===
// Runtime rule: shipTrophyIsaFactionVisitHegemonyCustoms

Far above, two officers on the overhang are staring down at Isa. Then at you. One raises a slate while the other points.

Isa sinks several centimeters below the observation window. "What's the big deal? I just wanted to watch."

+ "They might think you're a spy." -> station_hegemony_spy

=== station_hegemony_spy ===
// Runtime rule: shipTrophyIsaFactionVisitHegemonySpy

"Fine." Isa puts the slate away. "This'll be fine, right?"

+ "I wouldn't be so sure." -> station_hegemony_not_sure

=== station_hegemony_not_sure ===
// Runtime rule: shipTrophyIsaFactionVisitHegemonyNotSure
// Runtime effect: IsaFactionVisitCMD grantHegemonyReward

It is not fine.

You and your fleet spend another fifteen hours in customs after Hegemony security confiscates Isa's slate and holds her for questioning overnight.

You eventually get your chief engineer back: grouchy, sleep-deprived, and significantly more anti-authoritarian than when she went in.

Her first act back on the bridge is to begin designing a less conspicuous observation blister for your flagship.

[Fleet sensor profile reduced by 1%]

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_league ===
// Runtime rule: shipTrophyIsaFactionVisitLeague

As your fleet enters {market_name}'s docking queue, you find Isa standing among a crowd of spectators, staring up at the star-filled sky.

A local League detachment is putting on an airshow. Flights of G-10 Thunders scream overhead to delighted cheers, banking between the station's towers before scattering into precise, independent formations.

Isa, however, is silent.

A Thunder makes a particularly low pass over the concourse. Its exhaust washes through the crowd, sending coats, hats, and unsecured market displays fluttering in its wake. Isa's hair sweeps across her eyes, but her gaze never wavers. She watches the fighters climb away, one by one, before disappearing into the waiting bays of their Heron carrier.

"Look at them," she says quietly. "The statistics say most fighter pilots have a life expectancy measured in hours of combat. It's a job for suicidal people, honestly."

Her slate lights up with a purchase listing for a surplus Thunder wing.

"But for a few minutes at a time, they're the only people in the fleet who get to choose exactly where they go."

She opens the technical specifications.

"I think I understand the appeal."

+ [Only until someone in CIC tells them where to go.] -> station_league_rebuke

=== station_league_rebuke ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueRebuke

"{player_title}?"

Isa lowers the slate and fixes you with a sharp gaze.

"I know that from your chair in the CIC, we're probably just numbers moving across a tac screen. And I know you're the one who has to make those decisions."

Her expression softens.

"But please, once in a while... remember us."

+ [Of course. Sorry, Isa.] -> station_league_apologize
+ [We've all got our role to play.] -> station_league_role
+ [No soul is merely a number in Ludd's sight.] -> station_league_faithful

=== station_league_apologize ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueApologize

She holds your gaze for another moment before looking back toward the retreating fighters.

The purchase listing remains open on her slate.

+ [Try not to buy one before we dock.] -> station_league_purchase

=== station_league_role ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueRole
// Runtime effect: IsaFactionVisitCMD grantLeagueRebuke

"Hm."

Isa turns away and busies herself with her slate.

"Sure."

[Relationship with Isa reduced by 5]

+ [Try not to buy one before we dock.] -> station_league_purchase

=== station_league_faithful ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueFaithful

Isa studies your face for a moment.

"Amen," she says, a little uncertainly.

She holds your gaze for another moment before looking back toward the retreating fighters.

The purchase listing remains open on her slate.

+ [Try not to buy one before we dock.] -> station_league_purchase

=== station_league_purchase ===
// Runtime rule: shipTrophyIsaFactionVisitLeaguePurchase
// Runtime effect: IsaFactionVisitCMD grantLeaguePurchase

"No promises."

Isa dismisses the purchase listing, but bookmarks the technical specifications.

"See? Restraint."

[Lost 2,000 credits]
[Thunder wing LPC acquired]

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_tritachyon ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyon

The moment your fleet receives docking clearance, Isa's slate begins vibrating.

She looks down at it, frowns, and dismisses a purchase offer for replacement flux conduits.

Another appears immediately.

Then another.

The next time you find her, she is standing before the panoramic window of a Tri-Tachyon show berth. Beyond the glass, an Afflictor-class frigate hangs motionless against the stars, its hull picked out by cold blue running lights.

A pleasant synthetic voice fills the concourse.

"Observe the elegance of total battlespace control."

The Afflictor vanishes.

A heartbeat later, it reappears several kilometers away, directly behind a target drone. The drone comes apart under a burst of laser fire. Polite applause ripples through the showroom.

Isa leans toward the glass.

"That turn rate was far too high for auxiliary thrusters, and I don't see any extra maneuvering ports," she murmurs. "Did those psychopaths actually disable the safety interlocks for a marketing stunt? Stars above."

Her slate vibrates again.

This time, the advertisement addresses her by name.

CUSTOMA SHIP ARCHITECTURES PERSONNEL DISCOUNT

Based on her recent technical searches, professional history, fleet composition, estimated liquidity, and observed pupil response during the demonstration, Tri-Tachyon predicts an eighty-seven percent likelihood that Isa would benefit from immediate Afflictor ownership.

Financing has already been approved.

Isa rolls her eyes.

"I didn't give them any of that information."

The showroom voice answers without being asked.

"You did not need to, Ms. Leicester. Tri-Tachyon Marketing Services is the finest in the Domain. We have--"

Isa cuts the advertisement chatbot off with a wave of the hand, scowling at the speakers overhead.

+ [That's efficient.] -> station_tritachyon_efficient
+ [That's creepy.] -> station_tritachyon_wary
+ [That's an abomination.] -> station_tritachyon_faithful

=== station_tritachyon_efficient ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonEfficient

Isa gives you a frown.

"Efficient ain't the same thing as harmless, {player_title}."

She turns the slate around. Beneath the financing offer is a projected repayment schedule extending several years beyond her estimated life expectancy.

"Though they did get my preferred reactor configuration right."

Isa begins digging through the concourse app's privacy settings. Each menu opens into three more menus, all of them already set to OPTIMAL.

"I think entering the concourse counted as consent."

She pauses.

"Possibly docking at the station counted as consent."

-> station_tritachyon_second_choice

=== station_tritachyon_wary ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonWary

"Exactly."

Isa begins digging through the concourse app's privacy settings. Each menu opens into three more menus, all of them already set to OPTIMAL.

"I think entering the concourse counted as consent."

She pauses.

"Possibly docking at the station counted as consent."

-> station_tritachyon_second_choice

=== station_tritachyon_faithful ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonFaithful

"That's one way to put it," she says quietly. "And you're not entirely wrong."

For a moment, the showroom's blue light reflects strangely in her eyes.

Isa begins digging through the concourse app's privacy settings. Each menu opens into three more menus, all of them already set to OPTIMAL.

"I think entering the concourse counted as consent."

She pauses.

"Possibly docking at the station counted as consent."

-> station_tritachyon_second_choice

=== station_tritachyon_second_choice ===
+ [I'll spring for the premium docking package next time. I hear it comes with fewer ads.] -> station_tritachyon_premium
+ [I think being born counted as consent. Your fault, really.] -> station_tritachyon_bodyguards

=== station_tritachyon_premium ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonPremium

Isa snorts.

+ [Continue.] -> station_tritachyon_response

=== station_tritachyon_bodyguards ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonBodyguards
// Runtime visual: IsaContactRulesCMD showBodyguards

Isa kicks you.

"I docked your mom and that counted as consent."

You spend the next few moments kicking each other.

It is extremely unfair that your bodyguards help her kick you too.

By the time you get up from the fetal position you were in, Isa already has her slate open.

+ [Continue.] -> station_tritachyon_response

=== station_tritachyon_response ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonResponse
// Runtime visual: IsaContactRulesCMD showContactPortrait

Isa pulls a small data wafer from her pack and plugs it back into a secondary port on her slate.

Her slate's display fractures into a hundred miniature windows, each controlled by one of her own delta-level AI agents.

Bulk agricultural machinery.

Ceremonial armor polish.

Volturnian lobster breeding equipment.

Industrial quantities of devotional candles.

On her many screens, the advertisements flicker. Sleek phase ships disappear, replaced by increasingly confused offers for farming equipment, shrine furnishings, and twelve thousand liters of crustacean feed.

Isa smiles.

"There. Now we're both learning."

Her slate vibrates again.

A complete phase-coil maintenance manual has been added to her account as a complimentary incentive.

She bookmarks it.

+ [I thought you objected to their methods.] -> station_tritachyon_objection

=== station_tritachyon_objection ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonObjection
// Runtime effect: IsaFactionVisitCMD grantTriTachyonReward

"I hate being manipulated."

Isa closes the Afflictor financing offer. The maintenance manual remains open. She winks.

"I like winning."

[Monthly fleet supply consumption reduced by 1%]

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_diktat ===
// Runtime rules: shipTrophyIsaFactionVisitDiktat / shipTrophyIsaFactionVisitResponseDiktat

The station's traffic-control feed is crowded with Diktat hulls, every one polished for inspection. Isa zooms in on an Executor until its gilded weapon housings fill the display.

"They gold-plated a range-calibration problem until it became doctrine," she says. "Awful taste. Excellent tolerances. I hate how much I like it."

+ "Don't say that where they can hear you." -> station_diktat_response

=== station_diktat_response ===

"What, the taste or the tolerances?" Isa asks. She lowers her voice anyway.

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_church ===
// Runtime rules: shipTrophyIsaFactionVisitChurch / shipTrophyIsaFactionVisitResponseChurch

An old Church warship moves past the station under tug power, armor dark with repairs accumulated over generations. Isa watches it with unusual quiet.

"Everyone calls them old," she says at last. "Old isn't the same as obsolete. A Mora knows exactly what it is: a cathedral with a flight deck, built to keep its people alive long enough to come home and patch the same plate again."

+ "You sound almost converted." -> station_church_response

=== station_church_response ===

"To redundancy, maybe." Isa watches the old ship disappear behind the station. "Every spacer finds religion eventually."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_path ===
// Runtime rules: shipTrophyIsaFactionVisitPath / shipTrophyIsaFactionVisitResponsePath

Isa studies the station's picket ships through a passive sensor feed, annotating exposed conduits, overdriven engines, and armor plates that do not appear to have begun life on the same hull.

"This is a crime scene with engine mounts," she whispers. "But look at those feed lines. They built it to keep firing while it's on fire. That's not ignorance. That's commitment without brakes."

+ "Please tell me you aren't taking notes." -> station_path_response

=== station_path_response ===

Isa tilts the slate away from you. "Of course not." Six pages of diagrams vanish from view.

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_pirates ===
// Runtime rules: shipTrophyIsaFactionVisitPirates / shipTrophyIsaFactionVisitResponsePirates

The local traffic net is less a system than a sustained argument. Isa has isolated one battered pirate hull and is tracing its mismatched components with mounting admiration.

"Every pirate ship is a confession," she says. "This one admits to a stolen thruster, a borrowed gun mount, and a structural member that used to be a pressure door. And somehow the flux grid balances. Beautiful."

+ "We are not hiring the welder." -> station_pirates_response

=== station_pirates_response ===

"Counteroffer: I hire the welder and forbid them from touching life support." Isa zooms in on the former door. "Probably."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_independent ===
// Runtime rules: shipTrophyIsaFactionVisitIndependent / shipTrophyIsaFactionVisitResponseIndependent

Isa cycles through the station's traffic registry: Mules rebuilt on three different worlds, Buffaloes with local drive modifications, and a Venture whose maintenance record seems old enough to vote.

"No doctrine," she says, beaming. "Just a thousand local answers to a thousand local disasters. This is what actually keeps the Sector alive while the great powers are busy naming their battle plans."

+ "You say that like you've found religion." -> station_independent_response

=== station_independent_response ===

"I told you." Isa closes the registry with obvious reluctance. "Every spacer does eventually."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_knights ===
// Runtime rules: shipTrophyIsaFactionVisitKnights / shipTrophyIsaFactionVisitResponseKnights
// Optional: shown only when knights_of_ludd is enabled and knights_of_selkie exists.

The station grants priority passage to a Knights of Ludd formation. Isa leans over the sensor plot as armored signatures sweep past like a procession.

"They built a cathedral into a cavalry charge," she says, visibly delighted. "All that armor, all that forward commitment, and just enough restraint in the flux grid to pretend this is a measured decision."

+ "Pretend?" -> station_knights_response

=== station_knights_response ===

"Measured decisions usually include a plan for turning around," Isa says. "These have a prayer instead. I respect the weight savings."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_ironshell ===
// Runtime rules: shipTrophyIsaFactionVisitIronShell / shipTrophyIsaFactionVisitResponseIronShell
// Optional: shown only when timid_xiv is enabled and ironshell exists.

An Iron Shell patrol cuts across the station's approach lane, XIV armor moving at a speed its silhouette has no right to possess. Isa freezes the traffic feed, rewinds it, and watches the maneuver again.

"That is deeply unfair," she says, delighted. "They took Hegemony armor doctrine and taught it iaido. Look at that drive calibration. The whole ship draws before the enemy realizes there's a duel."

+ "Please don't challenge the tax inspectors to a duel." -> station_ironshell_response

=== station_ironshell_response ===

"I'm not challenging anyone," Isa says, already opening a thrust profile. "I'm conducting a deductible professional consultation."

+ [Continue to {market_name}.] -> station_vignette_menu
