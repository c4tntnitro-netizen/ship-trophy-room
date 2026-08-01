// Hall of Triumph - Isa Leicester station vignettes
// Standalone proofreading and Inky preview source reconstructed from the
// current runtime dialogue in data/campaign/rules.csv.
//
// This is the proofreading/authoring source mirrored into rules.csv. Java
// owns eligibility, one-time state, dynamic titles, mechanical rewards, and
// optional-mod safety.
// Shared runtime routing rules:
//   shipTrophyIsaFactionVisitReply
//   shipTrophyIsaFactionVisitContinue
//
// Vanilla faction IDs:
//   hegemony, persean, tritachyon, sindrian_diktat, luddic_church,
//   luddic_path, pirates
// Optional integrations:
//   Knights of Ludd - mod knights_of_ludd, faction knights_of_selkie
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
+ [Pirate] -> station_pirate
//+ [Knights of Ludd (optional)] -> station_knights
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
// Runtime presentation: green result line + ui_acquired_blueprint cue

It is not fine.

You and your fleet spend another fifteen hours in customs after Hegemony security confiscates Isa's slate and holds her for questioning overnight.

You eventually get your chief engineer back: grouchy, sleep-deprived, and significantly more anti-authoritarian than when she went in.

Her first act back on the bridge is to begin designing a less conspicuous observation blister for your flagship.

[Fleet sensor profile reduced by 1%]

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_league ===
// Runtime rule: shipTrophyIsaFactionVisitLeague

After your fleet enters {market_name}'s docking queue, you exit the ship. You find Isa standing among a crowd of spectators, staring up at the star-filled sky.

A local League detachment is putting on an in-atmosphere airshow. Flights of G-10 Thunders scream overhead to delighted cheers, banking between the station's towers before scattering into precise, independent formations.

Isa, however, is silent.

A Thunder makes a particularly low pass over the concourse. Its exhaust washes through the crowd, sending coats, hats, and unsecured market displays fluttering in its wake. Isa's hair sweeps across her eyes, but her gaze never wavers. She watches the fighters climb away, one by one, before disappearing into the waiting bays of their Heron carrier.

"Look at them," she says quietly. "The stats say most fighter pilots have a life expectancy measured in hours after first contact. It's a job for suicidal people, honestly."

Her slate lights up with a purchase listing for a surplus Thunder wing.

"But for a few minutes at a time, they're the only people in the fleet who get to choose exactly where they go."

She opens the technical specifications.

"I think I understand the appeal."

+ ["Only until someone in CIC tells them where to go."] -> station_league_rebuke

=== station_league_rebuke ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueRebuke

"{player_title}?"

Isa lowers the slate and fixes you with a sharp gaze.

"I know that from your place in the CIC, we in the fleet are probably just icons moving across a tac screen. And I know you're the one who has to make those decisions. You are, after all, the best fleet admiral in the Sector."

Her expression softens.

"But please, once in a while... remember us."

+ ["Of course. Sorry, Isa."] -> station_league_apologize
+ ["We've all got our role to play."] -> station_league_role
+ ["No soul is merely a number in Ludd's sight."] -> station_league_faithful

=== station_league_apologize ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueApologize

She gives an reconcilatory smile, holding your gaze for another moment before looking back toward the retreating fighters.

The purchase listing remains open on her slate.

+ ["Try not to buy one before we dock."] -> station_league_purchase

=== station_league_role ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueRole
// Runtime effect: IsaFactionVisitCMD grantLeagueRebuke

"Hm."

Isa turns away and busies herself with her slate.

"Sure."

[Relationship with Isa reduced by 5]

+ ["Try not to buy one before we dock."] -> station_league_purchase

=== station_league_faithful ===
// Runtime rule: shipTrophyIsaFactionVisitLeagueFaithful

Isa studies your face for a moment.

"Amen," she says, a little uncertainly.

She holds your gaze for another moment before looking back toward the retreating fighters.

The purchase listing remains open on her slate.

+ ["Try not to buy one before we dock."] -> station_league_purchase

=== station_league_purchase ===
// Runtime rule: shipTrophyIsaFactionVisitLeaguePurchase
// Runtime effect: IsaFactionVisitCMD grantLeaguePurchase
// Runtime presentation: red credit loss, green LPC gain + acquisition cue

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

+ [continue] -> station_tritachyon_0

=== station_tritachyon_0 ===

The next time you find her, she is standing before the panoramic window of a Tri-Tachyon show berth. Beyond the glass, an Afflictor-class frigate hangs motionless against the stars, its hull picked out by cold blue running lights.

A pleasant synthetic voice fills the concourse.

"Observe the elegance of total battlespace control."

The Afflictor vanishes.

A heartbeat later, it reappears several kilometers away, directly behind a target drone. The drone comes apart under a burst of laser fire. Polite applause ripples through the showroom.

Isa leans toward the glass.

"...Turn rate and speed were far too high for secondary thrusters, and I don't see any extra exhaust ports," she mutters. "Did those psychopaths actually disable the safety interlocks for a marketing stunt?"

+ ["Hey, Isa."] -> station_tritachyon_2

+ ["Chief Leicester."] -> station_tritachyon_2

+ ["Have you seen the Officers' Lounge here? It's is insane. They serve drinks with monomolecular ice cubes."] -> station_tritachyon_2

=== station_tritachyon_2 ===
// Runtime presentation: IsaFactionVisitCMD showTriTachyonAd
// Orbitron blue header, small muted targeting copy, blue probability/offer
// highlights, and a green financing-status line.

Before Isa can answer, her slate vibrates again.

This time, the advertisement addresses her by name.

ISAAC LEICESTER
CUSTOMA SHIP ARCHITECTURES // PERSONNEL DISCOUNT

Based on her recent technical searches, professional history, fleet composition, estimated liquidity, and observed pupil response during the demonstration, Tri-Tachyon predicts an eighty-seven percent likelihood that Isa would benefit from immediate Afflictor ownership. [Highlight in light blue tone]

FINANCING STATUS: PRE-APPROVED

+ [Continue] -> station_tritachyon_3

=== station_tritachyon_3 ===

Isa hastily wipes the advert from her slate.

"I didn't give them any of that information."

The showroom voice answers without being asked, speaking in the polished cadence of a TriOS delta-level AI.

"You didn't need to, Ms. Leicester. Tri-Tachyon Marketing Services is the finest in the Domain. We have--"

Isa cuts it off with a wave, scowling at the speakers overhead.

+ [That's efficient.] -> station_tritachyon_efficient
+ [That's creepy.] -> station_tritachyon_wary

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

=== station_tritachyon_second_choice ===
+ [I'll spring for the premium docking package next time. Ad-free, I hear.] -> station_tritachyon_premium
+ [I think being born counted as consent. Your fault, really.] -> station_tritachyon_bodyguards

=== station_tritachyon_premium ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonPremium

Isa snorts.

"What, premium docking means they rotate the station so you don't have to turn your ship around?"

You glance through the panoramic window; in the background, six ox tugboats are slowly maneuvering a motionless Starliner inside of its berth.

+ ["Close, actually."] -> station_tritachyon_premium_2

=== station_tritachyon_premium_2 ===

Isa follows your gaze.

"I hate this place."

+ [Continue.] -> station_tritachyon_response

=== station_tritachyon_bodyguards ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonBodyguards
// Runtime visual: IsaContactRulesCMD showBodyguards

Isa kicks you.

"I docked your mom and that counted as consent."

You spend the next few moments kicking each other.

It is extremely unfair that your bodyguards help her kick you too.

By the time you unfold yourself from the floor, Isa already has her slate open.

+ [Continue.] -> station_tritachyon_response

=== station_tritachyon_response ===
// Runtime rule: shipTrophyIsaFactionVisitTriTachyonResponse
// Runtime visual: IsaContactRulesCMD showContactPortrait

Isa pulls a small data wafer from her pack and plugs it into a secondary port on her slate.

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
// Runtime presentation: green result line + ui_acquired_blueprint cue

"I hate being manipulated."

Isa closes the Afflictor financing offer. The maintenance manual remains open. She winks.

"I like winning."

[Monthly fleet supply consumption reduced by 1%]

+ [Continue to {market_name}.] -> station_vignette_menu


=== station_diktat ===
// Runtime rule: shipTrophyIsaFactionVisitDiktat

Before clearing customs, you pass through a visitors’ concourse glowing with Andradan restaurants, jewelers, and imported food.

The lights end at the customs barrier.

On the other side, you find Isa chatting with a couple of Diktat engineers beside the arrivals portal. She waves, breaks off from the conversation, and joins you.

There are no shops here. Only state dispensaries, shuttered offices, and a Diktat commissar standing on a raised pulpit, holding a portrait of Andrada above a line of workers.

“By the Lion’s mercy,” he announces, “every loyal citizen eats. Andrada sees your glorious labor and smiles upon you!”

Through a tiny window, a clerk hands each laborer a foil-wrapped ration small enough to palm. On one of the ration packets, the Diktat emblem has been stamped slightly off-center. Beneath it, part of a Hegemony logo remains visible.

HEGAID.

Isa watches a shipforge worker tuck his ration under one arm and shuffle away. You can hear the commissar repeat his propaganda, over and over, like a chant.

“{player_title}.”

[“This your first time in a Diktat station?”] -> station_diktat_2

=== station_diktat_2 ===

“Yeah,” Isa says. “Can we finish things quickly here? I don’t like this place.”

* [Neither do I. We won’t stay long.] -> station_diktat_agree
* [You seemed friendly enough with those engineers.] -> station_diktat_chide
* [The Diktat keeps Sindria safe. That counts for something.] -> station_diktat_defend

=== station_diktat_agree ===

“Good.” Isa glances back at the ration line. “I was worried you were about to suggest lunch.”

-> station_diktat_3

=== station_diktat_chide ===

“The engineers aren’t the problem.” Isa glances back at the ration line. “They’re usually the ones fixing it.”

-> station_diktat_3

=== station_diktat_defend ===

“So does that shipforge worker.” Isa watches him disappear into the crowd, ration tucked beneath his arm. “Behold, the liberation of Philip Andrada.”

-> station_diktat_3

=== station_diktat_3 ===

You leave the arrivals portal behind.

The station’s traffic-control feed is crowded with Diktat hulls, every one polished for inspection. On the main display, a parade fleet of Lion’s Guard ships—the supposed elite of Philip Andrada’s navy.

Finally perking up, Isa zooms in on an Executor until its gilded weapon housings fill the display.

“Look at the placement of those ordnance mounts,” she says. “Ballistics forward for shield suppression. Energy weapons held back for armor and hull.”

She studies the altered mounts, then brings up the specifications for its Lion’s Guard escorts.

“What went wrong...?”

* [You sound pained.] -> station_diktat_response

=== station_diktat_response ===

“A little, yeah.”

Isa flicks between the Executor and its escorts.

“The Executor is inspired, honestly. Its escorts are straight garbage.”

One ship drifts out of formation. The others correct a moment too late.

A white-and-purple-liveried Falcon fires its maneuvering jets and lurches sideways into its neighbor’s shields, instantly overloading both ships' flux grids. Lightning arcs dance between the two hulls. You can already imagine the warning klaxons blaring through both CICs.

Isa lowers her slate and sighs.

“I’m going back to the fleet.”

[Continue to {market_name}.] -> station_vignette_menu


=== station_diktat_pagsm ===
// Runtime rule: shipTrophyIsaFactionVisitDiktatPAGSM

The concourse resembles a fuel depot rebuilt as an Andrada-themed amusement park. Purple neon traces the bulkheads. Animated Lions salute from every wall. Somewhere behind a museum gift shop and a Volturnian lobster petting pool, the same eight bars of techno begin again. Multiple clubs are playing the same song, at seemed. 

The arrivals portal deposits you directly in Isa’s path.

She nearly collides with you, arms piled high with Andrada merchandise, a curly straw clenched between her lips. It runs down into a massive, five-gallon novelty cup shaped like a Prometheus PM-15000.

Isa stops. She lowers her sunglasses, revealing another pair waiting underneath.

After a moment, she gives you a solemn nod, turns on her heel, walks right into one of your bodyguards, probably near blind due to the double-layers of shades she was wearing and marches back toward the command ship.

[Continue to {market_name}.] -> station_vignette_menu


=== station_church ===
// Runtime rule: shipTrophyIsaFactionVisitChurch

You are late leaving the docking concourse.

By the time you reach the arrivals hall, you notice a shock of familiar red hair beyond a passing line of hooded pilgrims. Curious, you approach.

Isa has been cornered beside a devotional kiosk by a nun of the Church of Galactic Redemption.

“There is joy to be found among the Sisters,” the nun says, clasping Isa’s hands. “A life of holiness and service, far from the worldly temptations of Moloch.”

Isa carefully extracts one hand.

“That sounds real nice, ma’am, but I already have a job.”

“Whatever debt has been forced upon you, the convent can—”

* [“You should probably ask what she does for a living.”] -> station_church_ask
* [“She's actually part of our fleet's Chaplain Corps”] -> station_church_chaplain

=== station_church_ask ===

The nun hesitates.

“What is your vocation, child?”

Isa looks at you.

Then back at the nun.

“I'm an engineer. I design warships. Verrry advanced ones.”

The nun releases her hands.

“Oh.”

She makes a hurried sign of blessing and retreats into the crowd, while Isa crosses her arms, self-satisfied.

“What if I wanted to become a nun?” Isa complains to you.

* ["What were you doing here?"] -> station_church_response

=== station_church_chaplain ===

The nun clasps her hands in delight.

“A fleet chaplain? Oh, child, why didn’t you say so?”

Isa looks at you and silently mouths, I’m going to kill you.

* ["She's just modest about her calling."] -> station_church_chaplain_2

=== station_church_chaplain_2 ===

“Then perhaps your fleet would contribute to our relief ministry. For the Glory of the Prophet, Sister.”

The nun presses a worn and weathered tablet into Isa’s hands. On it, a donation form. Isa glances at the suggested amount, then she looks at you. Slowly, she begins to grin.

* [Donate 1,000 credits.] -> station_church_donate
* [Briefly boost burn level at the cost of fuel and combat readiness.] -> station_church_escape

=== station_church_donate ===
// Runtime effect: deduct 1000 credits

You authorize the transfer while the nun beams.

“May Ludd reward your generosity in heaven.”

Isa hands the tablet back.

“He already did.” She gives you a cheerful pat on the shoulder.

* ["What were you doing here?"] -> station_church_response

=== station_church_escape ===

You turn and walk briskly toward the docking concourse.

Isa calls after you.

“Captain?”

You accelerate.

“Captain!”

Your bodyguards hurry to keep formation as you weave through the passing pilgrims. By the time you reach the main concourse, Isa is gaining on you.

* [Continue to {market_name}.] -> station_vignette_menu
=== station_church_response ===
// Runtime rule: shipTrophyIsaFactionVisitResponseChurch

“Stopped by the shrine. The choir was rehearsing and I like the music.”

She looks back toward the vaulted doors. A hymn carries faintly through them, nearly lost beneath the station machinery.

“Don’t make it weird.”

* [Continue to {market_name}.] -> station_vignette_menu


=== station_path ===

+ [Mechanic] -> station_path_mechanic

=== station_path_mechanic ===
// Runtime rule: shipTrophyIsaFactionVisitPathMechanic

A young Pather mechanic notices Isa studying an exposed power junction.

“You know reactors, sister?”

His voice is friendly. He offers her a cup of tea from the flask beside his tools.

Isa accepts it cautiously.

“A little.”

He shows her a schematic and asks how to keep an overloaded core from shutting itself down. Isa begins to answer.

Then she notices the civilian transponder codes beneath the reactor diagram.

“You aren’t trying to save this ship.”

The mechanic smiles.

“No, sister.”

* [Then what are you trying to do?] -> station_path_mechanic_response

=== station_path_mechanic_response ===

“This craft is designated for our Martyr Corps," He says casually, as if discussing fuel economy, "So we just need to keep the reactor functional in the short-term.”

Isa sets the tea down. The mechanic gives her an apologetic smile.

“Was it too sweet?”

Your Chief Engineer disappears into the command ship and doesn't come out for the rest of the trip.

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_pirate ===
// Runtime rule: shipTrophyIsaFactionVisitPirates

Isa has stopped beside the grav docks to watch the arriving ships.

One of them, a heavily modified Mudskipper, struggles to settle into its berth beneath the weight of the massive gun bolted to its frame. You seem to recognize that pattern as a Hellbore.

Isa leans over the railing.

“Look at that.”

* [“You’re admiring a Mudskipper?”] -> station_pirates_2

=== station_pirates_2 ===

“The craftsmanship.”

Isa points toward the improvised thrust assembly.

“No proper yard. No matching parts. Probably no sober foreman.”

She pulls out her slate and zooms in on a web of reinforcement struts around the engine mounts.

“They grafted those jets on from three different hull classes. The couplings were machined by hand, the fuel lines are routed through the captain’s old living quarters, and I’m pretty sure that heat shield used to be a blast door.”

The pirate frigate fires its maneuvering thrusters and settles neatly against the docking clamps.

Isa smiles.

“Still flies straight. Mostly.”


* [“I’m surprised you’re so positive about their work.”] -> station_pirates_3

=== station_pirates_3 ===

She lowers the slate.

“I grew up on an independent station. Seen people from all walks of life. The only difference between an indy and a pirate is how many meals they’ve missed.”

She shrugs.

“And I like the poor, misunderstood underdog.”

* [continue] -> station_pirates_4

=== station_pirates_4 ===

The doors of a dockside bar slam open.

A pirate stumbles into the concourse, wild-eyed.

“I’ll pay! I swear I’ll pay this time!”

Several others emerge behind him. They seize him by the arms and legs, carry him to the edge of the dock, and throw him screaming and pleading through the grav field.

He hangs outside the station without a helmet, kicking soundlessly against the stars. His eyes bulge and his face begins to swell. Every desperate jerks sends the breath leaking from his lungs, turning him end over end, helplessly in the void.

After ten seconds, they drag him back by a tether around his ankle.

The moment he crosses the railing, he collapses onto the deck, wheezing and coughing blood while the bar erupts in laughter.

* [“Poor, misunderstood underdogs.”] -> station_pirates_response

=== station_pirates_response ===
// Runtime rule: shipTrophyIsaFactionVisitResponsePirates

Isa kicks you, then turns back toward the pirate frigate.

“Assholes,” she mutters darkly. “Making me look stupid.”

* [Continue to {market_name}.] -> station_vignette_menu
