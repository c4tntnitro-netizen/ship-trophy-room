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
