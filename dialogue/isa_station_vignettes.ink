// Hall of Triumph - Isa Leicester station vignettes
// Standalone proofreading and Inky preview source.
//
// Runtime source: data/campaign/rules.csv
// After approving edits here, mirror the changed prose into rules.csv.
// The Java command owns eligibility, one-time state, and optional-mod safety.
//
// Vanilla faction IDs:
//   hegemony, persean, tritachyon, sindrian_diktat, luddic_church,
//   luddic_path, pirates, independent
// Optional integrations:
//   Knights of Ludd - mod knights_of_ludd, faction knights_of_selkie
//   Iron Shell      - mod timid_xiv, faction ironshell
//
// Text in {braces} is supplied dynamically by the game.

VAR market_name = "the station"

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
// Rule: shipTrophyIsaFactionVisitHegemony
As your fleet joins {market_name}'s approach queue, Isa disappears from the bridge. You find her pressed against the glass of an observation blister, three Hegemony hull profiles turning slowly above her slate.

"Look at that burn profile," she says without turning. "Every Hegemony ship looks like it expects the universe to punch first. Everyone calls the Onslaught blunt, but it isn't blunt. It's honest. Armor, guns, engines, and absolutely no shame about the order."

+ "You're making friends with the customs scanners." -> station_hegemony_response

=== station_hegemony_response ===
"They started it," Isa says. "Their resolution is terrible. I was helping."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_league ===
// Rule: shipTrophyIsaFactionVisitLeague
Isa has commandeered an auxiliary tactical display before docking clearance is even confirmed. A Conquest rotates above it, split down the center by a bright line marking its thrust axis.

"Two broadsides held together by optimism and transverse thrust," she says fondly. "That's League engineering. Take a Domain blueprint, argue with it for six hundred cycles, then sign your name across the parts that survived."

+ "Try not to buy one before we dock." -> station_league_response

=== station_league_response ===
"No promises." Isa dismisses the purchase listing but bookmarks the technical specification. "See? Restraint."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_tritachyon ===
// Rule: shipTrophyIsaFactionVisitTriTachyon
By the time {market_name} acknowledges your approach, Isa has three encrypted diagnostic windows open and the delighted expression of someone standing much too close to a live power conduit.

"Beautiful. Infuriating. Half the mass isn't where engineering says it should be," she says. "Tri-Tach ships are magic tricks performed by accountants with security clearances. Look at those phase tolerances. Look at them."

+ "Step away from their network, Isa." -> station_tritachyon_response

=== station_tritachyon_response ===
"I'm not in their network," Isa says, offended. One of the encrypted windows vanishes. "I'm adjacent to it."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_diktat ===
// Rule: shipTrophyIsaFactionVisitDiktat
The station's traffic-control feed is crowded with Diktat hulls, every one polished for inspection. Isa zooms in on an Executor until its gilded weapon housings fill the display.

"They gold-plated a range-calibration problem until it became doctrine," she says. "Awful taste. Excellent tolerances. I hate how much I like it."

+ "Don't say that where they can hear you." -> station_diktat_response

=== station_diktat_response ===
"What, the taste or the tolerances?" Isa asks. She lowers her voice anyway.

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_church ===
// Rule: shipTrophyIsaFactionVisitChurch
An old Church warship moves past the station under tug power, armor dark with repairs accumulated over generations. Isa watches it with unusual quiet.

"Everyone calls them old," she says at last. "Old isn't the same as obsolete. A Mora knows exactly what it is: a cathedral with a flight deck, built to keep its people alive long enough to come home and patch the same plate again."

+ "You sound almost converted." -> station_church_response

=== station_church_response ===
"To redundancy, maybe." Isa watches the old ship disappear behind the station. "Every spacer finds religion eventually."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_path ===
// Rule: shipTrophyIsaFactionVisitPath
Isa studies the station's picket ships through a passive sensor feed, annotating exposed conduits, overdriven engines, and armor plates that do not appear to have begun life on the same hull.

"This is a crime scene with engine mounts," she whispers. "But look at those feed lines. They built it to keep firing while it's on fire. That's not ignorance. That's commitment without brakes."

+ "Please tell me you aren't taking notes." -> station_path_response

=== station_path_response ===
Isa tilts the slate away from you. "Of course not." Six pages of diagrams vanish from view.

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_pirates ===
// Rule: shipTrophyIsaFactionVisitPirates
The local traffic net is less a system than a sustained argument. Isa has isolated one battered pirate hull and is tracing its mismatched components with mounting admiration.

"Every pirate ship is a confession," she says. "This one admits to a stolen thruster, a borrowed gun mount, and a structural member that used to be a pressure door. And somehow the flux grid balances. Beautiful."

+ "We are not hiring the welder." -> station_pirates_response

=== station_pirates_response ===
"Counteroffer: I hire the welder and forbid them from touching life support." Isa zooms in on the former door. "Probably."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_independent ===
// Rule: shipTrophyIsaFactionVisitIndependent
Isa cycles through the station's traffic registry: Mules rebuilt on three different worlds, Buffaloes with local drive modifications, and a Venture whose maintenance record seems old enough to vote.

"No doctrine," she says, beaming. "Just a thousand local answers to a thousand local disasters. This is what actually keeps the Sector alive while the great powers are busy naming their battle plans."

+ "You say that like you've found religion." -> station_independent_response

=== station_independent_response ===
"I told you." Isa closes the registry with obvious reluctance. "Every spacer does eventually."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_knights ===
// Rules: shipTrophyIsaFactionVisitKnights / shipTrophyIsaFactionVisitResponseKnights
// Optional: shown only when knights_of_ludd is enabled and knights_of_selkie exists.
The station grants priority passage to a Knights of Ludd formation. Isa leans over the sensor plot as armored signatures sweep past like a procession.

"They built a cathedral into a cavalry charge," she says, visibly delighted. "All that armor, all that forward commitment, and just enough restraint in the flux grid to pretend this is a measured decision."

+ "Pretend?" -> station_knights_response

=== station_knights_response ===
"Measured decisions usually include a plan for turning around," Isa says. "These have a prayer instead. I respect the weight savings."

+ [Continue to {market_name}.] -> station_vignette_menu

=== station_ironshell ===
// Rules: shipTrophyIsaFactionVisitIronShell / shipTrophyIsaFactionVisitResponseIronShell
// Optional: shown only when timid_xiv is enabled and ironshell exists.
An Iron Shell patrol cuts across the station's approach lane, XIV armor moving at a speed its silhouette has no right to possess. Isa freezes the traffic feed, rewinds it, and watches the maneuver again.

"That is deeply unfair," she says, delighted. "They took Hegemony armor doctrine and taught it iaido. Look at that drive calibration. The whole ship draws before the enemy realizes there's a duel."

+ "Please don't challenge the tax inspectors to a duel." -> station_ironshell_response

=== station_ironshell_response ===
"I'm not challenging anyone," Isa says, already opening a thrust profile. "I'm conducting a deductible professional consultation."

+ [Continue to {market_name}.] -> station_vignette_menu
