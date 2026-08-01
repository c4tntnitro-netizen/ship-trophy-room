// Hall of Triumph - A Name on a Suit
// Standalone proofreading copy. Runtime dialogue is implemented in rules.csv.

VAR hypershunts_reactivated = 0
VAR gan_eden_revealed = false
VAR grave_found = false
VAR golden_shards_defeated = false

=== gargoyle_investigation ===

Isa has converted an unused berth office into a forensic workshop. The transit index hangs above its central table in thousands of red, broken fragments.

"I found our specialist," she says.

A second channel opens. Gargoyle regards the data with the patient disdain of someone deciding which rule of reality to insult first.

"You have a door," Gargoyle says. "You would like it to become embarrassed about having ever been locked."

+ [Patch Gargoyle into the Ring archive.] -> gargoyle_hack

=== gargoyle_hack ===

Gargoyle's intrusion blooms through the dead archive. Checksums mend themselves. Entire columns of meaningless symbols decide, under pressure, to become dates and destination hashes.

Isa leans over the table. "There. Passenger movements, final month before the Collapse."

Only one outbound name survives.

ISAAC LEICESTER.

He was the Ring's final passenger, carried through an adamantine transit aperture to a destination outside every surviving Sector chart.

+ [Where did he go?] -> gargoyle_triangulation

=== gargoyle_triangulation ===

"The destination field was never a coordinate," Gargoyle says. "It was a phase relationship between two stellar-scale power sources."

Two signatures appear: the Sector's Coronal Hypershunts, recorded while both were still active.

"Reactivate both taps," Isa says, already following the reconstruction. "Two baselines. One intersection. Then we find the ring Isaac went through."

-> hypershunt_objective

=== hypershunt_objective ===

[Reactivate both Coronal Hypershunts to triangulate the Gan Eden Transit Ring.]

{ hypershunts_reactivated < 2:
    Coronal Hypershunts reactivated: {hypershunts_reactivated} / 2.
- else:
    Both signatures lock together. An adamantine ring appears at the exact point where the baselines intersect.
    ~ gan_eden_revealed = true
}

-> END

=== gan_eden_ring ===

The reconstructed ring hangs in hyperspace without a nascent gravity well or any corresponding star. Its adamantine surface is awake, drawing impossible depth through an aperture that should show only more hyperspace.

+ [Enter the Gan Eden Transit Ring.] -> gan_eden_arrival
+ [Leave.] -> END

=== gan_eden_arrival ===

Gan Eden is empty.

A warm star hangs at the center of a constructed paradise. Seas and continents curve around the fleet on the inner surface of the world. No cities answer. No traffic crosses the sky.

Only two golden Omega signatures move against the light: Cherubim and Lahat Haharev.

+ [Search the Tree of Life with Isa.] -> isaac_grave
+ [Face the Golden Shards.] -> golden_shards

=== isaac_grave ===

The space elevator answers Isa's inherited suit transponder.

Its doors open onto an empty concourse. Departure boards still promise connections to cities whose lights have been dark for two centuries. Beyond them, a sealed memorial garden has kept one tree alive beneath patient lamps.

There is one grave.

ISAAC LEICESTER

+ [Open the memorial archive with Isa.] -> isaac_record

=== isaac_record ===

The archive contains no family registry. It contains one final maintenance log, recorded after every other inhabitant had gone.

Isaac's voice is exhausted.

"Isa made the crossing. The Ring confirmed receipt. That has to be enough."

The attached evacuation record bears the serial number of the cryopod that carried Isa to the Shattered Ring.

Isa reads it twice. "My father, then. Probably." She touches the name on the stone. "Close enough for me."

+ [Stay with her.] -> isaac_farewell

=== isaac_farewell ===

You wait beneath the memorial tree.

Eventually Isa sits beside the grave and begins telling Isaac Leicester about the Shattered Ring: the wreck-farms, the revival ward, the terrible food, and every impossible ship that carried her farther than he could have imagined.

When she finally stands, the paradise beyond the glass remains empty. Only the distant golden signatures of Cherubim and Lahat Haharev move against the sun.

~ grave_found = true

-> quest_resolution

=== golden_shards ===

Cherubim and Lahat Haharev do not retreat. Each Golden Shard fractures into smaller aureate geometries until the final Tessaracts are destroyed.

~ golden_shards_defeated = true

-> quest_resolution

=== quest_resolution ===

{ grave_found && golden_shards_defeated:
    Isa's search for Isaac Leicester is complete.

    The Golden Shards are gone from Gan Eden, but their aureate echoes have entered the Remnant network. Some future Ordos may be led by Cherubim or Lahat Haharev. Very rarely, both will appear together.
- else:
    { grave_found:
        The Golden Shards remain.
    - else:
        Isaac Leicester's grave remains undiscovered.
    }
}

-> END
