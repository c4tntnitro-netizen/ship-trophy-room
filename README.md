# Hall of Triumph

> **Beta notice:** Everything in this repository should be considered a beta change. Features, balance, dialogue, assets, and save compatibility may change before a formal release.

Adds a player-colony structure called **Hall of Triumph**.

- Unlocks a dedicated **Hall of Triumph** storage tab on the colony.
- Stores ships as normal mothballed storage vessels.
- Generates story points passively while functional.
- All functional Halls of Triumph are networked.
- Adds **Isa**, a salvager and ship-modder contact who appears in the dockside bar after the first functional Hall of Triumph is built.
- Completing every vanilla Isa trophy program grants **Isa Leicester** as a steady level-8 officer with two exclusive skills; optional mod integrations are not required.
- After Isa joins the fleet, the first visit to a station owned by each supported vanilla faction plays a short, one-time vignette about that faction's shipbuilding doctrine. Knights of Ludd remains an optional, safely detected integration.
- Adds **The Shattered Ring**, an independent size-4 free port in Penelope's Star. The station is built into the remains of an adamantine ring and supports a community of salvagers, wreck-farmers, and cryopod survivors.
- Adds **Gan Eden**, a compact prototype Dyson-sphere interior reached through Isa's post-recruitment story quest.
- Base network rate is 1 story point per 180 days per functional Hall of Triumph.
- Tracks stored ships by unique base hull id across the whole network.
- Duplicate hulls can be stored, but only one of each hull type contributes to network generation.
- Every 12 unique hull types adds another full base-rate bonus.
- Every 240 total deployment points from unique hulls adds another full base-rate bonus.
- Tracks hosted hull-size counts: frigates, destroyers, cruisers, and capitals.
- Improving a structure makes that Hall of Triumph count as a 33% stronger story-point node.

## The Shattered Ring

The Shattered Ring is generated in the fixed Penelope's Star system at game start or the next time an existing campaign is loaded. It is an Independent size-4 free port with a custom station sprite, a shipbreaking economy, Wreck Farms, a Pod Community condition, and a persistent local field of claimed derelicts and debris. Its resident nanoforge engineer offers commissioned damage: one eligible D-mod of the player's choice can be installed on a fleet ship for one story point, up to the normal D-mod limit. In randomized-sector configurations where Penelope's Star does not exist, generation is skipped safely.

The Ring is Isa's birthplace: salvagers recovered her there as an infant in a cryopod, swaddled in a spacer suit marked **Isaac Leicester**. She shortened the inherited name to Isa after learning its masculine association. Her story is not unique there; generations of cryopod survivors—affectionately called “pod people”—form an ordinary and established part of the Ring's community.

After Isa joins the player's officer roster, bringing her to the Shattered Ring triggers a one-time homecoming scene and begins **A Borrowed Name**. An old foreman returns the spacer suit in which she was found, and Isa discovers an identification wafer hidden beneath its scorched name strip. The wafer opens Isaac Thomas Leicester's first surviving personal log and links his work to both surviving Coronal Hypershunts.

The `.ink` files under `dialogue/` are composition and proofreading material only; the mod never loads them at runtime. `data/campaign/rules.csv` is the sole dialogue authority. After reconciling drafted changes into `rules.csv`, run `tools/build_gan_eden_master.ps1` to regenerate the three rules-derived Gan Eden proofreading volumes. `dialogue/shattered_ring.ink` preserves additional Ring setting material.

## Gan Eden

Gan Eden is a deliberately compact, completely siloed star system built inside a prototype Dyson sphere. Its inward-facing surface uses a spherical projection of a custom 2:1 world map, so the geography itself curves and foreshortens into the circular horizon around the warm central star. Beyond the playable space, a white atmospheric haze deepens toward blue as the viewing angle approaches that inner surface. Sparse, disconnected remnants of dark Domain-era armor and machinery cling to the horizon, with irregular plates and antenna spars encroaching over the constructed world's inner surface. This **Altitude Warning** zone softly decelerates approaching fleets and rebounds them toward the star without causing hull, combat-readiness, or crew damage. Complete blackness lies outside the sphere's circular aperture.

The quest sends the player to investigate both vanilla Coronal Hypershunts. One is blockaded by the Luddic Path and the other by pirates; each encounter supports a noncombat resolution or battle. Peacefully resolved fleets remain intact, stand down, and visibly leave the approach corridor while the hypershunt becomes immediately usable; only fleets defeated in combat are removed as battle casualties. Both blockades field experimental **Mk IV** variants with Unstable Injector and Heavy Armor built in. Pirate Mk IV hulls carry weathered ochre tiger stripes; Pather Mk IV hulls are marked by dark, dried-blood smears and splatter. Defeating either blockade in combat unlocks that faction's refit program independently: a minority of its later ordinary fleets may field one or more Mk IV ships, including compatible modded hulls. Peaceful resolutions do not propagate the program. Their scans recover Personal Logs II and III. Comparing the two concealed routing vectors reveals **POWER TRANSIT GATE - GAN EDEN**, floating at the center of a small, starless system near the northeastern edge of the Sector. It has no stable jump point: the native purple nascent gravity well must be entered with **Transverse Jump**. A sprawling, inert graveyard of damaged Coronal Hypershunts and Gate Haulers surrounds the Gate, while a clear inner approach keeps it navigable. These wrecks are scenery only and cannot trigger vanilla megastructure interactions or discovery alerts. The first player entry awakens a one-time **Ivory Custodian** interception fleet around the Gate; it persists through retreats, clears all tactical survivors after a player victory, and never respawns. This dedicated Gate is the only entrance to Gan Eden, and the internal ring always returns the fleet to it. The system contains four fixed, surveyable settlement sites built into the Dyson sphere's inner shell and no ambient civilian population. Each uses a grey top-down settlement marker. Their terrain-appropriate deposits are set to the highest vanilla tier: ultrarich ores at Cinderwake; plentiful volatiles and ultrarich rare ores at Rimewell; bountiful farmland and plentiful organics at the Tree of Life; and plentiful organics and volatiles at Pelagos Basin. The Tree of Life preserves Part IV in its local distribution network. Every recovered record is permanently filed as a small-type fleet-log entry under the **Gan Eden Archives** Intel tag, with its body loaded from its canonical row in `data/campaign/rules.csv`.

Every battle inside Gan Eden uses a dedicated 4096x2048 enhancement of the Terran Eccentric surface texture as its full-screen backdrop and applies **Atmospheric Flow** to all combatants: ballistic and energy weapon damage and range are reduced by 5%, while ship and missile maximum speed are increased by 20%.

The Gan Eden Space Elevator is relocated to a separate surface district and remains hidden and non-interactable until **Cherubim** and **Lahat Haharev** are defeated together for the first time. That first complete victory releases its four settlement sites from their sealed economy groups so player colonies can participate in ordinary Sector trade, but Gan Eden itself remains parked beyond charted hyperspace and accessible only through the Power Transit Gate. If only one named Shard survives an encounter, it reconstructs its missing counterpart. A complete victory begins a recurring 90-day return cycle. Each new Golden Omega wave brings a larger escort of custom ivory ceramic Remnant hulls, escalating through four tiers until the escort alone is roughly a full Ordo. These reconstructed ships use exact 50/50 composites of their ivory artwork and the eight corresponding vanilla Remnant sprites, retaining the vanilla alpha masks, and have **Insulated Engine Assembly**, **Resistant Flux Conduits**, **Solar Shielding**, and **Stabilized Shields** built directly into their hulls. Later victories reset the 90-day timer without relocking the quest or transit-gate route. After the first victory, newly spawned ordinary Remnant Ordos also have a 10% chance to carry one or more regular, non-aureate Facets or Shards as escorts; Cherubim and Lahat themselves no longer appear in random Ordos. Golden Omega encounters suppress Starsector's default Remnant music, cue **Strike from the Sky** at five seconds of unpaused combat time, play its authored intro once, and then repeat its dedicated loop segment.
## Music credits

```text
Song: Lonesome Journey
Composer: Keys Of Moon
Website: https://www.youtube.com/c/keysofmoonmusic
License: Creative Commons (BY 3.0) https://creativecommons.org/licenses/by/3.0/
Music powered by BreakingCopyright: https://breakingcopyright.com
```

Adaptation notice: the supplied MP3 was converted to Ogg Vorbis and followed by ten seconds of silence to create Gan Eden's repeating ambient-music cycle. The musical content was otherwise unchanged.

```text
Song: Strike from the Sky
Composer: Ucchii0-うっちーぜろ-
Source and terms: https://ucchii0.booth.pm/items/5228335
Edition: Purchased loop-ready edition
```

Adaptation notice: the supplied WAV intro and loop segments were converted separately to Ogg Vorbis for Starsector playback. The authored edit points and musical content were otherwise unchanged.

```text
Song: Kono Saki wa, Kimi dake de. (Beyond This Point, Only You)
Composer: Ucchii0
Official video: https://www.youtube.com/watch?v=e4RagJ9OCVA
Usage terms: https://ucchii0artist.wixsite.com/ucchii0
License: Free BGM edition; attribution required. Copyright remains with Ucchii0.
```

Adaptation notice: the official 30-minute endurance upload was used as the audio source. Spectral fingerprinting identified its 167.433-second repeating cycle; the first complete cycle is used once as the final-log intro and the following cycle is converted separately to Ogg Vorbis as the seamless loop. The musical content was otherwise unchanged.

## Doctrine unlocks

Showcasing 40 unique DP worth of a doctrine's ships across the Hall of Triumph network teaches the player a 0 OP hullmod:

- **Legacy**: unlocked by XIV Battlegroup ships; installable on low-tech ships. Adds 100 armor, reduces speed and maneuverability by 8%, and improves flux capacity and dissipation by 5%.
- **Zeal**: unlocked by Luddic Path ships; installable on low-tech ships. Same speed/range/fighter replacement tradeoff as Unstable Injector, increases crew casualties by 30%, and is incompatible with Unstable Injector. Its tooltip notes that it counts as a D-mod for calculations such as Derelict Operations.
- **Pageantry**: unlocked by Lion's Guard ships; installable on midline ships. Acts like Energy Bolt Coherer: +100 energy projectile range, -100 beam range, +25% crew casualties. Its tooltip notes that it counts as a D-mod for calculations such as Derelict Operations, and it is incompatible with Energy Bolt Coherer.
- **Optimization**: unlocked by Tri-Tachyon/high-tech ships; installable on high-tech ships. Reduces sensor profile by 15 and adds 200 hull integrity. Incompatible with Insulated Engine Assembly.
- **Humanity**: unlocked by showcasing 40 unique DP of Remnant ships. Reduces the ship's deployment cost to 0 DP, reduces ballistic, energy, and missile fire rate and top speed by 80%, reduces fighter engagement range by 90%, functions as Neural Interface, and is incompatible with Neural Interface.
- **Memory**: unlocked by showcasing 20 unique DP of Domain Derelict/Explorarium ships and installable only on those ships. Adds 80% hull integrity, 300 armor, and 25/20/15/15 top speed by hull size. Incompatible with Unstable Injector.
- **Awe**: unlocked through Isa's five-capital showcase: Onslaught XIV, Paragon, Invictus, Conquest, and Executor. Doubles supported positive S-mod bonus effects, including compatible modded hullmods; S-mod penalties are not doubled.

Only one Hall of Triumph hullmod can be installed on a ship at a time.

## Data-driven subtypes

Subtype tracking is configured in `data/config/ship_trophy_room/subtypes.csv`. Other mods can integrate by shipping a merged CSV at the same path with one row per subtype. Matching ships add their unique DP to that subtype, and the listed hullmod unlocks when the threshold is reached.

Important columns:

- `id`: stable subtype id.
- `displayName` / `showcaseName`: UI text.
- `hullModId`: optional hullmod to teach the player.
- `requiredModId`: optional mod id gate; the row is inactive unless that mod is enabled.
- `unlockDp`: DP needed to unlock the hullmod.
- `installStyle`: `low-tech`, `midline`, `high-tech`, or `any`.
- `hullIdContains`, `baseHullIdContains`, `manufacturerContains`, `hullNameContains`: pipe-separated case-insensitive match text.
- `hullTagMatches`, `variantTagMatches`: pipe-separated exact tag matches.

Optional example integrations are included:

- **Honor**: active only with `knights_of_ludd`; low-tech only, hull and armor damage taken -5%, hull integrity +10%, shield damage taken -10%.
- **Resonance**: active only with `uaf`; increases guided missile maximum speed by 33%. Torpedo and multi-rocket volleys launch two forward decoy flares at 110% of the parent munition's speed, while Semibreve launchers deploy five. Decoys remain active for 2/4/10 seconds when fired by small/medium/large launchers. Missile-based point defense, fighter weapons, and submunitions are excluded. Incompatible with ECCM Package.
- **Discipline**: active only with `timid_xiv` and installable only on Iron Shell ships. Replaces the ship's system with **Iaido**, a high-speed armored charge that leaves torpedo launchers operational. Torpedoes fired during the draw gain 200% maximum and launch speed and deal 50% additional kinetic damage without a cap.

For a plain-language walkthrough of adding your own subtype and special trophy hullmod, see `docs/creating_trophy_hullmods.md`.

## Unique showcase unlocks

- **Gaze**: unlocked by showcasing the Ziggurat anywhere in the Hall of Triumph network. Doubles flux dissipation while venting and is incompatible with Resistant Flux Conduits.
- **Contempt**: unlocked by showcasing the Onslaught Mk.I anywhere in the Hall of Triumph network. Point-defense weapons deal 25% increased damage to fighters and missiles and generate 10% less flux; this includes weapons converted by S-modded Integrated Point Defense AI. All weapons also deal 5% increased damage to hull.
- Featured mod-ship showcases: **Abundant Mercy** from Knights Hospitaller (`knights_hospitallar`) and **The Black Lion** from Black Lion Ships (`black_lion_ships`) are always listed in Isa's modded unique-hull menu, even when their source mods are not installed. Their 0 OP trophy hullmods can only be unlocked when the corresponding ship is actually available and displayed.

- **Inheritance**: unlocked by showcasing The Black Lion. Increases energy weapon damage by 10% and reduces energy weapon flux cost by 5%.
- **Vow**: unlocked by showcasing the Abundant Mercy. Reduces crew casualties by 25% and fighter refit time by 10%.

## Combat Chatter integration

If **Combat Chatter** is installed, Isa uses a custom combat dialogue pool once she joins your officer roster. The integration is data-only and keyed to her officer name, **Isa Leicester**, so Combat Chatter is not required to run Hall of Triumph.

## Build

From this folder:

```powershell
.\build.ps1
```

To make a clean install folder that excludes Git metadata and build scratch:

```powershell
.\package.ps1
```

Then copy `dist\HallOfTriumph` into `Starsector\mods`, enable **Hall of Triumph**, and start/load a campaign.
