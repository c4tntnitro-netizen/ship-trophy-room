# Ship Trophy Room

Adds a player-colony structure called **Trophy Room**.

- Unlocks a dedicated **Trophy Room** storage tab on the colony.
- Stores ships as normal mothballed storage vessels.
- Generates story points passively while functional.
- All functional Trophy Rooms are networked.
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
