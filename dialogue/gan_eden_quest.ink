// Hall of Triumph - A Name on a Suit
// Standalone proofreading copy. Runtime dialogue is implemented in rules.csv.

VAR hypershunts_reactivated = 0
VAR gan_eden_revealed = false
VAR grave_found = false
VAR golden_shards_defeated = false
VAR player_gender = "him"
VAR player_title = "Captain"
VAR player_self = "himself"

->gan_eden_quest

=== gan_eden_quest ===
Quest dialogue

+ [station_shattered_ring] -> station_shattered_ring
+ [End preview] -> END

=== station_shattered_ring ===

Shattered Ring comes into view one broken section at a time.

Three vast arcs circle the barren moon below, each turning at a slightly different rate. Pressurized bridges, cargo gantries, and naked lengths of structural cable span some of the gaps. Others remain open to space, their severed ends capped with bulkheads scavenged from old ships.

The self-governing colony looks less constructed than prevented from falling apart. Isa appears beside the navigation station and plants both hands on your navigator’s desk.

“Take us around the dark side.”

Your navigator glances at the approach plot.

“Traffic beacon’s directing us sunward.”

“That traffic beacon’s been wrong since I was twelve.”

As if summoned by the insult, the comm channel crackles.

“Approaching fleet, inbound bearing zero-three-five, elevation minus one-two. Reduce velocity and prepare to receive docking procedures—”

Isa leans over the console.

“Arthur? Tell Garret it’s me.”

There is a long pause.

“Isaac?” another voice says. “Thought we were finally rid of you.”

“Call me that again and I’ll come up there and kick your butt all over comms.”

Garret laughs.

“Roger that. Take the locals' approach queue. You know the way. Over.”

Isa closes the channel.

“Bay Fourteen,” she tells the navigator, pointing at a row of glowing beacons that was hidden behind an arc.

+ [“Isaac?”] -> station_shattered_ring_isaac
+ [“You seem familiar with the place.”] -> station_shattered_ring_familiar

=== station_shattered_ring_isaac ===

Isa points a finger at you.

“No.”

+ [“No?”] -> station_shattered_ring_isaac_2

=== station_shattered_ring_isaac_2 ===

“No questions. No jokes. No telling the bodyguards.”

Behind you, one of your bodyguards discreetly opens a note on their slate.

Isa points at them too.

“Wei. I can see you.”

-> station_shattered_ring_docking

=== station_shattered_ring_familiar ===

“I grew up here.”

Isa studies the approaching station.

“Mostly in Arc Two. Arc One had the good machine shops, but their gravity used to cut out whenever the ore processor started.”

-> station_shattered_ring_docking

=== station_shattered_ring_docking ===

Bay Fourteen accepts your approaching fleet with a tremendous metallic cacophony.

The entire docking tube shudders as the clamps engage. Something heavy strikes the outer hull, tumbles away, and disappears beneath the berth.

Isa waits for the noise to stop.

“Perfect.”

Everyone on your bridge exchanges looks. The docking tube groans again. Your flagship shifts in its berth with a tremendous crunch. 

"Even more perfect."

By the time you reach the main concourse, word has spread.

Dockworkers call to Isa from the overhead gantries. A food vendor reaches across his counter to press a foil-wrapped pastry into her hand. Someone shouts that the recycler on Level Six is making the drinking water taste metallic again.

A child in an oversized pressure suit runs up and presents her with a cracked maneuvering thruster valve. Isa crouches, turns it over in her hands, and tells him which seal needs replacing.

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

“You own a fleet. You're basically royalty around here.”

She returns to the light panel.

“So they know you can afford it.”

+ ["My favorite part of being royalty. Noblesse Oblige."]-> station_shattered_ring_foreman

=== station_shattered_ring_foreman ===

An old salvage foreman is waiting when Isa climbs down.

He wears a patched station utility suit, its original insignia hidden beneath decades of repairs. Under one arm, he carries a narrow metal case marked with faded cryogenic-handling symbols.

Isa’s good humor disappears.

“Where did you get that?”

“Found it behind the plating in Calder’s old storeroom,” the foreman says. “Station council was clearing out abandoned property.”

He holds the case toward her. 

The people passing through the concourse give the three of you a wide berth. Finally, Isa wipes her hands against her trousers and accepts the case.

“Thanks.”

The foreman nods. Before leaving, he looks toward you.

“She was swaddled in that when we found her.” The foreman glances toward you. “Changed her diapers on that thing.”

Isa throws a glove at the man.

“You didn’t have to tell {player_gender} that.”

“Why not? You weren’t going to.” The old man laughs, then disappears into the crowd.

+ [“We can open it somewhere private.”] -> station_shattered_ring_workshop
+ [Say nothing.] -> station_shattered_ring_workshop

=== station_shattered_ring_workshop ===

Isa leads you through the station without speaking.

You descend through Arc Two, past hydroponics bays and crowded habitation decks, until the finished corridors give way to exposed conduits and old pressure doors.

The gravity weakens with each level.

Isa adjusts automatically, shortening her steps whenever the deck shifts beneath her. Your bodyguards are less graceful. One catches the overhead piping to avoid drifting into a wall.

At last, Isa opens an abandoned machine shop.

Several names have been carved into the pressure door. Hers is among them, scratched low enough that whoever wrote it must have been very young.

ISA LESESTER IS DA BEST.

She sets the case on an old workbench. Inside is a child-sized bundle of pressure fabric, folded carefully beneath a transparent preservation sheet.

The material was once white. Radiation and age have yellowed it almost to brown. Several sections have been cut from a much larger suit and crudely flextaped to wrap around something far smaller than its intended wearer, from a spacesuit into something more like a cradle. 

A blackened name strip remains attached to the collar.

LEICESTER, ISAAC.

Isa touches two fingers to the transparent sheet.

“This is where they got it,” she says. “My name.”

+ [“You were named after the suit?”] -> station_shattered_ring_named

=== station_shattered_ring_named ===

“I'm a 'pod person'. They thawed me outta one of those cryosleeper pods when I was still a baby, all swaddled up in that.”

Isa gives you a smile.

“I went by the name on the label until I was old enough to realize 'Isaac' was a man’s name.”

+ ["Cryosuspension that young is almost unheard of. Standard protocols should prohibit any younger than eight cycles"]-> station_shattered_ring_scan

=== station_shattered_ring_scan ===

"My great claim to fame," Isa says, smiling. "Youngest 'pod person' on my Arc."

A small metal socket is visible beneath the scorched collar.

Isa stops smiling.

“That wasn’t there before.”

She releases the preservation seal and carefully lifts the suit onto the workbench. The fabric crackles as she turns the collar over, shedding small flecks of decayed plastic onto the surface below.

The layer of insulation has frayed, revealing a identification wafer embedded beneath the name strip.

Isa removes her slate.

On scan, her slate’s display splits into dozens of windows. Her delta-level AI agents begin testing ancient authentication protocols, interpolating damaged sectors and comparing the wafer against surviving Domain registries.

“You don’t have to stay,” she says.

+ [“Let me know when you need me.”] -> station_shattered_ring_go
+ [“I don't have to go.”] -> station_shattered_ring_stay

=== station_shattered_ring_go ===
"Thanks, {player_title}."

+ [“I’ll give you privacy. Wei, Yvan.”] -> station_shattered_ring_go_2

=== station_shattered_ring_go_2 ===

You walk out with your bodyguards, leaving Isa to it.

Outside the workshop, Shattered Ring creaks and groans around you. Pumps cycle behind the walls. Somewhere far below, a cargo lift begins its slow ascent through the arc.

After a few minutes, Isa calls from the workshop.

Man. You finally had Yvan locked down in your chess match. It was going to be weeks before you rolled a 960 setup that good again.

+ [continue] -> station_shattered_id

=== station_shattered_ring_stay ===

Isa glances at you, then nods. 

"Thanks, {player_title}."

And she returns to the slate.

Minutes pass.

Outside the workshop, Shattered Ring creaks and groans around you. Pumps cycle behind the walls. Somewhere far below, a cargo lift begins its slow ascent through the arc.

->station_shattered_id

=== station_shattered_id ===

One of the windows on Isa’s slate turns green.

A personnel file unfolds above the workbench.

DOMAIN CITIZEN REGISTRY
IDENTITY VERIFIED

NAME: LEICESTER, ISAAC THOMAS
REGISTRY NUMBER: DCR-2F38-CB017-6A
SEX: MALE
OCCUPATION: DIRECTOR OF ENGINEERING
DEPARTMENT: HELIOSTRUCTURAL SYSTEMS, PERSEAN SECTOR

CLEARANCE: Querying Directorate personnel index...

ERROR // DOMAIN INFOSEC VIOLATION THRESHOLD WARNING

// datastream resetting...

CLEARANCE: ACCESS DENIED
LAST ASSIGNMENT: ACCESS DENIED
STATUS: ACCESS DENIED

IMMEDIATELY contact DOMAIN INFOSEC to review compliance with
Domain Information Security Standards.

Isa does not move. Another field appears beneath it.

+ [continue] ->station_shattered_id_2

=== station_shattered_id_2 ===

EMERGENCY DEPENDENT AUTHORIZATION
DEPENDENT: UNREGISTERED FEMALE INFANT

ISSUE OF:

LEICESTER, ISAAC THOMAS
REGISTRY NUMBER: DCR-2F38-CB017-6A
STATUS: PENDING TRANSFER TO—

ERROR // DOMAIN INFOSEC VIOLATION THRESHOLD WARNING

// datastream resetting...

LEICESTER, ISAAC THOMAS
REGISTRY NUMBER: DCR-2F38-CB017-6A
STATUS: ACCESS DENIED

LEICESTER, REBECCA ANNE, NÉE SARAI
REGISTRY NUMBER: DCR-2F38-CB018-41
STATUS: DECEASED

IMMEDIATELY contact DOMAIN INFOSEC to review compliance with
Domain Information Security Standards.

The workshop is silent except for the hum of the slate.

Isa reads the entry again.

Then a third time.

“They were real,” she says. "Both of them."

+ [“It looks that way.”] -> station_shattered_ring_real
+ [“Isaac knew you were in the pod.”] -> station_shattered_ring_real
=== station_shattered_ring_real ===

“Yeah.”

Isa enlarges the redacted assignment field. Her agents attack it from every direction, but the text dissolves into meaningless fragments.

“He put me in that suit. Registered me under his clearance. Maybe loaded the pod too.”

She stares at the name above hers.

“I always figured whoever loaded the pod grabbed whatever was nearby. Found me, found the suit, wrapped me up in it, then chucked me in.”

Her fingers tighten around the edge of the workbench.

“But he knew I was there.”

+ [“We can search the surviving registries.”] -> station_shattered_ring_begin_search
+ [“You don’t owe a dead man an investigation.”] -> station_shattered_ring_owe

=== station_shattered_ring_owe ===

“No.”

Isa looks down at the old name strip.

“But I owe myself one.”

You hear her add, almost beneath her breath:

“And I don’t actually know that he’s dead...”

-> station_shattered_ring_begin_search

=== station_shattered_ring_begin_search ===

Isa returns to the slate.

Her agents scatter through the surviving Domain archives, following Isaac Leicester’s registry number through personnel rosters, departmental accounts, procurement records, and damaged transit indexes.

Most references vanish as soon as they begin to resolve.

DCR-2F38-CB017-6A

The number appears beside a Heliostructural Systems roster.

Then a classified procurement authorization.

Then a personnel-transfer order whose destination disappears beneath another INFOSEC warning.

Isa opens the failed queries side by side.

“What are you doing?” you ask.

“Looking at how they break.”

She groups the failures by their distorted output. Fields concealing the same information collapse in nearly identical ways.

Isa’s agents begin finding matches.

CLEARANCE — ELEVEN CORRESPONDING RECORDS  
STATUS — DETAINEE PROCESSING  
LAST ASSIGNMENT — ONE CORRESPONDING RECORD

Isa selects the final match.

+ [Continue.] -> station_shattered_ring_transit

=== station_shattered_ring_transit ===

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

“That’s him.”

+ [Continue.] -> station_shattered_ring_final_transit

=== station_shattered_ring_final_transit ===

Isa opens the surviving end of the manifest.

FINAL OUTBOUND AUTHORIZATION  
PASSENGER COUNT: 1  
ORIGIN: PENELOPE’S STAR  
DESTINATION: INFOSEC REDACTED  
RING STATUS: OPERATIONAL

She studies the two destination fields.

“His declared destination was Sol.”

Her agents probe the second field. A few characters appear, then collapse beneath a new warning.

DESTINATION: SUPER ALABASTER RESTRICTED

Isa frowns.

“Super Alabaster.”

+ [“Meaning?”] -> station_shattered_ring_alabaster

=== station_shattered_ring_alabaster ===

“Meaning whoever buried this didn’t want the destination turning up anywhere else.”

Isa opens the surrounding archive references.

The restriction has propagated through transit logs, personnel orders, and every surviving copy linked to the authorization. Each record breaks at the same point.

“Can you recover it?” you ask.

“Not from this.”

She continues sorting the remaining collisions.

Two infrastructure designations recur beside Isaac’s registry number.

CORONAL HYPERSHUNT — NETWORK AUTHORIZATION  
CORONAL HYPERSHUNT — NETWORK AUTHORIZATION

Isa enlarges them.

“He worked on the hypershunts.”

// Replace these conditions with the actual runtime flags.
+ {hypershunts_reactivated > 0}
    [“We’ve seen those before. They’re guarded by extremely dangerous automated ships. They call themselves ‘Omega.’”] -> station_shattered_ring_hypershunts_known

+ {hypershunts_reactivated == 0}
    [“Hypershunts?”] -> station_shattered_ring_hypershunts_unknown

=== station_shattered_ring_hypershunts_known ===

Isa looks up sharply.

“Omega?”

She turns back to the records.

“That would’ve been useful to know.”

-> station_shattered_ring_hypershunts_compare

=== station_shattered_ring_hypershunts_unknown ===

“Incredibly old, pre-Collapse technology,” Isa says. “They draw power directly from a star.”

She enlarges the two records.

“Only two are known to have survived in the Sector.”

-> station_shattered_ring_hypershunts_compare

=== station_shattered_ring_hypershunts_compare ===

Isa’s agents compare both hypershunt records against the concealed destination.

The encrypted field remains unreadable, but the same routing authorization appears in all three records.

Isa leans closer.

“Whatever was on the other side of that Gate, both hypershunts knew about it.”

+ [“Then we visit both.”] -> station_shattered_ring_decision
+ [“What will they tell us?”] -> station_shattered_ring_hypershunt_answer

=== station_shattered_ring_hypershunt_answer ===

“I don’t know yet.”

Isa highlights the matching authorization residue.

“But this is the first thing I’ve found that the redaction didn’t completely erase.”

She closes the manifest.

“If they still carry any part of the old routing record, I can work from there.”

-> station_shattered_ring_decision

=== station_shattered_ring_decision ===

Isa finishes copying the surviving records to her slate.

For a while, she says nothing. The old suit remains spread across the workbench between you, Isaac Leicester’s name blackened but still legible beneath the collar.

Then she closes the registry.

“Captain.”

Her voice has lost its earlier lightness.

“I know this isn’t fleet business.”

She rests one hand on the metal case.

“But somebody went to a lot of trouble to bury him. Domain INFOSEC, old registry locks, whatever was left in those records. I don’t think I’m going to get much further by myself.”

Isa looks directly at you.

“I want to find out who Isaac Leicester was. What happened to him. Why he put me in that pod.”

She hesitates.

“I’m also asking for your help.”

+ [“My chief engineer’s business is fleet business.”] -> station_shattered_ring_accept_command
+ [“We’ll visit the hypershunts. We’ll see where they lead.”] -> station_shattered_ring_accept_cautious

=== station_shattered_ring_accept_command ===

Isa gives you a small, crooked smile.

“That a direct order?”

+ [“Never make your commanding officer repeat themselves.”] -> station_shattered_ring_accept_command_2

=== station_shattered_ring_accept_command_2 ===

“Aye-aye, {player_title}.”

-> station_shattered_ring_prepare_search

=== station_shattered_ring_accept_cautious ===

“That’s all I’m asking.”

-> station_shattered_ring_prepare_search

=== station_shattered_ring_prepare_search ===

Isa busies herself with her slate, transferring the registry number, INFOSEC failures, transit manifest, and hypershunt records into a new directory.

The folder remains unnamed for several seconds.

Then she enters:

ISAAC THOMAS LEICESTER

[Quest started: A Borrowed Name]

[Isaac Thomas Leicester was the final passenger authorized through the Penelope’s Star Gate.]

[Objective updated: Investigate both surviving hypershunts.]

+ [Return to the fleet.] -> END




