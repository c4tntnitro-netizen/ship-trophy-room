# Ship Trophy Room

Adds a player-colony structure called **Trophy Room**.

- Unlocks a dedicated **Trophy Room** storage tab on the colony.
- Stores ships as normal mothballed storage vessels.
- Generates story points passively while functional.
- All functional Trophy Rooms are networked.
- Adds **Isa**, a salvager and ship-modder contact who appears in the dockside bar after the first functional Trophy Room is built.
- Base network rate is 1 story point per 180 days per functional Trophy Room.
- Tracks stored ships by unique base hull id across the whole network.
- Duplicate hulls can be stored, but only one of each hull type contributes to network generation.
- Every 12 unique hull types adds another full base-rate bonus.
- Every 240 total deployment points from unique hulls adds another full base-rate bonus.
- Tracks hosted hull-size counts: frigates, destroyers, cruisers, and capitals.
- Improving a structure makes that Trophy Room count as a 33% stronger story-point node.

## Doctrine unlocks

Showcasing 60 unique DP worth of a doctrine's ships across the Trophy Room network teaches the player a 0 OP hullmod:

- **Fourteenth Trophy Legacy**: unlocked by XIV Battlegroup ships; installable on low-tech ships. Adds 100 armor, reduces speed and maneuverability by 8%, and improves flux capacity and dissipation by 5%.
- **Path Trophy Zeal**: unlocked by Luddic Path ships; installable on low-tech ships. Same speed/range/fighter replacement tradeoff as Unstable Injector, increases crew casualties by 30%, and is incompatible with Unstable Injector. Its tooltip notes that it counts as a D-mod for calculations such as Derelict Operations.
- **Lion's Guard Trophy Pageantry**: unlocked by Lion's Guard ships; installable on midline ships. Acts like Energy Bolt Coherer: +100 energy projectile range, -100 beam range, +25% crew casualties. Its tooltip notes that it counts as a D-mod for calculations such as Derelict Operations, and it is incompatible with Energy Bolt Coherer.
- **Tri-Tachyon Trophy Legacy**: unlocked by Tri-Tachyon/high-tech ships; installable on high-tech ships. Reduces sensor profile by 15 and adds 200 hull integrity. Incompatible with Insulated Engine Assembly.
- **Awe**: unlocked through Isa's five-capital showcase: Onslaught XIV, Paragon, Invictus, Conquest, and Executor. Doubles positive S-mod bonus effects from built-in hullmods; S-mod penalties are not doubled.

Only one Trophy Room hullmod can be installed on a ship at a time.

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

- **Knights of Ludd Trophy Benediction**: active only with `knights_of_ludd`; energy weapon damage +5%, shield damage taken -5%.
- **United Aurora Trophy Resonance**: active only with `uaf`; flux capacity/dissipation +5%, fighter refit time -10%.
- **Iron Shell Trophy Parade Drill**: active only with `timid_xiv`; +75 armor, +50 ballistic weapon range.

For a plain-language walkthrough of adding your own subtype and special trophy hullmod, see `docs/creating_trophy_hullmods.md`.

## Unique showcase unlocks

- **Gaze**: unlocked by showcasing the Ziggurat anywhere in the Trophy Room network. Reduces OP costs of fitted Omega weapons by 2 each.
- **Contempt**: unlocked by showcasing the Onslaught Mk.I anywhere in the Trophy Room network. Reduces OP costs of fitted Dweller and Threat weapons by 1 each.

## Build

From this folder:

```powershell
.\build.ps1
```

To make a clean install folder that excludes Git metadata and build scratch:

```powershell
.\package.ps1
```

Then copy `dist\ShipTrophyRoom` into `Starsector\mods`, enable **Ship Trophy Room**, and start/load a campaign.
