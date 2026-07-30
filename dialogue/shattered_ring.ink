// Hall of Triumph - Shattered Ring lore and dialogue writing aid
// This file is not loaded at runtime. It preserves the setting material for
// proofreading and for the planned Shattered Ring story expansion.

VAR visited_shattered_ring = false
VAR isa_origin_revealed = false

=== shattered_ring_arrival ===

Penelope's Star burns through the gaps in a broken ring of adamantine.

The surviving arcs are far too large to have been built as a station, yet that is what generations of spacers have made of them. Habitats, dockyards, revival wards, and salvage gantries cling to one fracture in layers. Tug traffic moves constantly between the station and the surrounding field of derelicts.

The locals call the orbital claims wreck-farms. Salvager families tend them for decades at a time, nudging promising hulks into safe plots and waiting for collisions to uncover something worth cutting open. Metals and machinery are the common harvest. Intact hull sections are rarer. Living cryopods are rarest of all.

~ visited_shattered_ring = true

+ [Ask why the Ring has so many revival wards.] -> shattered_ring_pod_people
+ [Preview Isa's post-recruitment homecoming.] -> isa_shattered_ring_homecoming
+ [Enter the port registry.] -> END

=== shattered_ring_pod_people ===

"Pod people," the registry clerk says, with the easy familiarity of a local nickname. "That's what we call them. Half the Ring has a grandparent who woke up here with no date, no home, and no one left to send a message to."

Cryopod survivors are common enough to be unremarkable on the Ring. Some were recovered only a few years after the Collapse. Others slept for centuries. The revival wards give them names when they have none, work when they are able, and a community that understands the particular grief of arriving late to one's own life.

+ [Leave the registry.] -> END

=== isa_shattered_ring_origin ===

Isa watches the wreck-farms turn beyond the viewport.

"I was a baby when they cut my pod out of a freighter," she says. "No manifest, no family record, nothing useful. Just a little spacer suit someone had swaddled me in. ISAAC LEICESTER was stenciled across the chest."

She taps two fingers against the glass.

"So that was my name. Isaac Leicester. At least until I was old enough to learn everyone had made a fairly understandable mistake."

Her smile is crooked, but not embarrassed.

"Isa suited me better. The Ring taught me the rest."

~ isa_origin_revealed = true

-> END

=== isa_shattered_ring_homecoming ===
// Runtime rules:
// shipTrophyIsaShatteredRingHomecoming
// shipTrophyIsaShatteredRingHomecomingReply
// shipTrophyIsaShatteredRingHomecomingName
// shipTrophyIsaShatteredRingHomecomingContinue
// Eligibility: Isa has joined the officer roster, is still in the fleet, and
// this one-time scene has not previously completed.

The broken arcs of the Shattered Ring fill the forward view, black adamantine cutting across the light of Penelope's Star. Wreck-farm tugs move among the fragments like insects tending an immense skeleton.

Isa comes to the bridge without being called. For once, she has no slate in her hands.

"Still there," she says softly. "Stupid thing to say about an adamantine ring. I used to wonder anyway."

+ "Welcome home, Isa." -> isa_shattered_ring_homecoming_origin

=== isa_shattered_ring_homecoming_origin ===

Isa laughs, but the sound catches halfway out.

"Home. Yeah. I suppose it is." She watches a salvage tug cross the viewport. "One of those crews found my pod out in the Suitors. I was small enough to carry under one arm. No manifest. No family record. Just a spacer suit big enough to swaddle me in, with ISAAC LEICESTER stenciled across the chest. So the revival ward wrote it down."

She shrugs. "That isn't much of an origin story here. Half the Ring has a version of it. Pod people, they call us. Affectionately, usually."

"Took me years to learn Isaac wasn't generally a girl's name. Took about five minutes after that to decide Isa was."

+ "Isa suits you better." -> isa_shattered_ring_homecoming_dock

=== isa_shattered_ring_homecoming_dock ===

"It does," she says. "Kept the rest, though. Somebody wanted Isaac Leicester to make it here. Close enough."

Dock control clears your fleet for berth nineteen. The comm channel immediately fills with overlapping voices, several of them shouting Isa's name.

"Oh, no," she says. Her smile betrays her. "They told everyone."

She turns for the lift before docking is complete. "Come on. I want to see what they ruined while I was gone."

+ [Take us in.] -> END
