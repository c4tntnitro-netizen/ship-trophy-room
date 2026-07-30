# Hall of Triumph

> **Beta notice:** Everything in this repository should be considered a beta change. Features, balance, dialogue, assets, and save compatibility may change before a formal release.

Adds a player-colony structure called **Hall of Triumph**.

- Unlocks a dedicated **Hall of Triumph** storage tab on the colony.
- Stores ships as normal mothballed storage vessels.
- Generates story points passively while functional.
- All functional Halls of Triumph are networked.
- Adds **Isa**, a salvager and ship-modder contact who appears in the dockside bar after the first functional Hall of Triumph is built.
- Completing every vanilla Isa trophy program grants **Isa Leicester** as a steady level-8 officer with two exclusive skills; optional mod integrations are not required.
- After Isa joins the fleet, the first visit to a station owned by each supported vanilla faction plays a short, one-time vignette about that faction's shipbuilding doctrine. Knights of Ludd and Iron Shell receive their own vignettes when their respective mods are installed, but neither mod is required.
- Adds **The Shattered Ring**, an independent size-4 free port in Penelope's Star. The station is built into the remains of an adamantine ring and supports a community of salvagers, wreck-farmers, and cryopod survivors.
- Base network rate is 1 story point per 180 days per functional Hall of Triumph.
- Tracks stored ships by unique base hull id across the whole network.
- Duplicate hulls can be stored, but only one of each hull type contributes to network generation.
- Every 12 unique hull types adds another full base-rate bonus.
- Every 240 total deployment points from unique hulls adds another full base-rate bonus.
- Tracks hosted hull-size counts: frigates, destroyers, cruisers, and capitals.
- Improving a structure makes that Hall of Triumph count as a 33% stronger story-point node.

## The Shattered Ring

The Shattered Ring is generated in the fixed Penelope's Star system at game start or the next time an existing campaign is loaded. It is an Independent size-4 free port with a custom station sprite, a shipbreaking economy, Wreck Farms, a Pod Community condition, and a persistent local field of claimed derelicts and debris. In randomized-sector configurations where Penelope's Star does not exist, generation is skipped safely.

The Ring is Isa's birthplace: salvagers recovered her there as an infant in a cryopod, swaddled in a spacer suit marked **Isaac Leicester**. She shortened the inherited name to Isa after learning its masculine association. Her story is not unique there; generations of cryopod survivors—affectionately called “pod people”—form an ordinary and established part of the Ring's community.

The editable writing aid for this material is in `dialogue/shattered_ring.ink`.

## Doctrine unlocks

Showcasing 60 unique DP worth of a doctrine's ships across the Hall of Triumph network teaches the player a 0 OP hullmod:

- **Legacy**: unlocked by XIV Battlegroup ships; installable on low-tech ships. Adds 100 armor, reduces speed and maneuverability by 8%, and improves flux capacity and dissipation by 5%.
- **Zeal**: unlocked by Luddic Path ships; installable on low-tech ships. Same speed/range/fighter replacement tradeoff as Unstable Injector, increases crew casualties by 30%, and is incompatible with Unstable Injector. Its tooltip notes that it counts as a D-mod for calculations such as Derelict Operations.
- **Pageantry**: unlocked by Lion's Guard ships; installable on midline ships. Acts like Energy Bolt Coherer: +100 energy projectile range, -100 beam range, +25% crew casualties. Its tooltip notes that it counts as a D-mod for calculations such as Derelict Operations, and it is incompatible with Energy Bolt Coherer.
- **Optimization**: unlocked by Tri-Tachyon/high-tech ships; installable on high-tech ships. Reduces sensor profile by 15 and adds 200 hull integrity. Incompatible with Insulated Engine Assembly.
- **Humanity**: unlocked by showcasing 60 unique DP of Remnant ships. Reduces the ship's deployment cost to 0 DP, reduces ballistic, energy, and missile fire rate and top speed by 80%, reduces fighter engagement range by 90%, functions as Neural Interface, and is incompatible with Neural Interface.
- **Memory**: unlocked by showcasing 40 unique DP of Domain Derelict/Explorarium ships and installable only on those ships. Adds 80% hull integrity, 300 armor, and 25/20/15/15 top speed by hull size. Incompatible with Unstable Injector.
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
