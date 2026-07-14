# Creating Special Trophy Hullmods

This guide is for mod authors who want Ship Trophy Room to recognize their ships and unlock a special trophy hullmod. You can make a simple bonus hullmod without writing Java.

The short version:

1. Add Ship Trophy Room as a dependency in your mod.
2. Add one row to `data/config/ship_trophy_room/subtypes.csv`.
3. Add one row to `data/hullmods/hull_mods.csv`.
4. Add one row to `data/config/ship_trophy_room/hullmod_effects.csv`.

Do this in your own mod or patch mod. You do not need to edit Ship Trophy Room's files directly.

## 1. Add the dependency

If your hullmod uses the no-code script from Ship Trophy Room, your mod should depend on Ship Trophy Room so the script exists when the game loads:

```json
"dependencies": [
  {
    "id": "ship_trophy_room",
    "name": "Ship Trophy Room"
  }
]
```

If your mod already has dependencies, add the Ship Trophy Room entry to that list.

## 2. Add a subtype row

Create this file in your mod:

```text
data/config/ship_trophy_room/subtypes.csv
```

Use this header:

```csv
id,displayName,showcaseName,hullModId,requiredModId,unlockDp,installStyle,hullIdContains,baseHullIdContains,manufacturerContains,hullNameContains,hullTagMatches,variantTagMatches
```

Example row:

```csv
my_mod_elite,My Mod Elite,Elite,ship_trophy_my_mod_elite,my_mod,60,any,my_mod_,,my faction,,,
```

What the columns mean:

- `id`: Unique subtype id. Use your mod id as a prefix, like `my_mod_elite`.
- `displayName`: Name shown in Trophy Room status text.
- `showcaseName`: Name used in unlock text, such as "60 DP worth of Elite ships".
- `hullModId`: The hullmod to unlock when the DP target is reached.
- `requiredModId`: Optional mod id gate. Leave blank if it should always work. Use your own mod id if this row belongs to your mod.
- `unlockDp`: How many unique deployment points must be showcased before the hullmod unlocks. Blank or 0 defaults to 60.
- `installStyle`: `low-tech`, `midline`, `high-tech`, or `any`.
- `hullIdContains`: Match ships whose hull id contains this text.
- `baseHullIdContains`: Match ships whose base hull id contains this text.
- `manufacturerContains`: Match ships whose manufacturer contains this text.
- `hullNameContains`: Match ships whose hull name contains this text.
- `hullTagMatches`: Match ships with an exact hull tag.
- `variantTagMatches`: Match ships with an exact variant tag.

You only need one matching rule, but you can use several. A ship counts if any rule matches. Separate multiple values with `|` or `;`, like `my_mod_|elite_`.

Useful matching examples:

- Manufacturer contains `auroran`: put `auroran` in `manufacturerContains`.
- Hull ids all start with `my_mod_`: put `my_mod_` in `hullIdContains`.
- Your hull specs have a tag named `my_faction`: put `my_faction` in `hullTagMatches`.
- You only want special variants with a tag named `my_elite`: put `my_elite` in `variantTagMatches`.

If another mod uses the same `id`, the later loaded row can replace yours. Use a unique id unless you intentionally want to override an existing subtype.

## 3. Add the hullmod row

Create or extend this file in your mod:

```text
data/hullmods/hull_mods.csv
```

Use the normal Starsector hullmod columns. For a no-code trophy hullmod, set `script` to:

```text
shiptrophy.hullmods.ConfigurableTrophyHullMod
```

Example row:

```csv
name,id,tier,rarity,tech/manufacturer,tags,uiTags,base value,unlocked,hidden,hiddenEverywhere,cost_frigate,cost_dest,cost_cruiser,cost_capital,script,desc,short,sModDesc,sprite
My Mod Elite Trophy,ship_trophy_my_mod_elite,0,,Trophy Room,"offensive, restricted, no_drop_salvage",Weapons,0,FALSE,FALSE,FALSE,0,0,0,0,shiptrophy.hullmods.ConfigurableTrophyHullMod,"Elite parade tuning from showcased hulls increases energy weapon damage by %s and reduces shield damage taken by %s.",Elite trophy refit.,,graphics/hullmods/advanced_optics.png
```

The `id` must exactly match the `hullModId` from your subtype row.

Use `%s` in the description for values you want to fill from `descParam0`, `descParam1`, and so on in the effects file.

The `restricted` and `no_drop_salvage` tags are recommended for trophy hullmods. The hullmod is still locked by the Trophy Room network until the player showcases enough matching DP.

## 4. Add no-code effects

Create this file in your mod:

```text
data/config/ship_trophy_room/hullmod_effects.csv
```

Use this header:

```csv
hullModId,descParam0,descParam1,descParam2,descParam3,descParam4,descParam5,descParam6,descParam7,descParam8,descParam9,armorFlat,hullFlat,maxSpeedPercent,maneuverPercent,fluxCapacityPercent,fluxDissipationPercent,ballisticRangeFlat,energyRangeFlat,missileRangeFlat,ballisticDamagePercent,energyDamagePercent,missileDamagePercent,ballisticFluxCostPercent,energyFluxCostPercent,missileFluxCostPercent,shieldDamageTakenPercent,shieldUpkeepPercent,fighterRefitTimePercent,crewLossPercent,sensorProfileFlat,sensorStrengthFlat
```

Example row:

```csv
ship_trophy_my_mod_elite,5%,5%,,,,,,,,,,,,,,,,,,,5,,,,,-5,,,,,
```

This example does two things:

- `energyDamagePercent` is `5`, so energy weapon damage goes up by 5%.
- `shieldDamageTakenPercent` is `-5`, so shield damage taken goes down by 5%.

Effect columns support numbers only. Put `5`, not `5%`. The `descParam` columns can include display text like `5%`, `100`, or `-15`.

Supported no-code effect columns:

- `armorFlat`: Adds flat armor.
- `hullFlat`: Adds flat hull integrity.
- `maxSpeedPercent`: Changes maximum speed by a percent.
- `maneuverPercent`: Changes acceleration, deceleration, turn rate, and turn acceleration by a percent.
- `fluxCapacityPercent`: Changes flux capacity by a percent.
- `fluxDissipationPercent`: Changes flux dissipation by a percent.
- `ballisticRangeFlat`: Adds flat ballistic weapon range.
- `energyRangeFlat`: Adds flat energy weapon range.
- `missileRangeFlat`: Adds flat missile weapon range.
- `ballisticDamagePercent`: Changes ballistic weapon damage by a percent.
- `energyDamagePercent`: Changes energy weapon damage by a percent.
- `missileDamagePercent`: Changes missile weapon damage by a percent.
- `ballisticFluxCostPercent`: Changes ballistic weapon flux cost by a percent.
- `energyFluxCostPercent`: Changes energy weapon flux cost by a percent.
- `missileFluxCostPercent`: Changes missile weapon flux cost by a percent.
- `shieldDamageTakenPercent`: Changes shield damage taken by a percent.
- `shieldUpkeepPercent`: Changes shield upkeep by a percent.
- `fighterRefitTimePercent`: Changes fighter refit time by a percent.
- `crewLossPercent`: Changes crew losses by a percent.
- `sensorProfileFlat`: Adds flat sensor profile.
- `sensorStrengthFlat`: Adds flat sensor strength.

Negative percent values reduce a stat. For example, `fighterRefitTimePercent=-10` makes fighter refit time 10% shorter, and `ballisticFluxCostPercent=-5` makes ballistic weapons cost 5% less flux to fire.

## Complete copy-paste mini example

`data/config/ship_trophy_room/subtypes.csv`

```csv
id,displayName,showcaseName,hullModId,requiredModId,unlockDp,installStyle,hullIdContains,baseHullIdContains,manufacturerContains,hullNameContains,hullTagMatches,variantTagMatches
my_mod_elite,My Mod Elite,Elite,ship_trophy_my_mod_elite,my_mod,60,any,my_mod_,,my faction,,,
```

`data/hullmods/hull_mods.csv`

```csv
name,id,tier,rarity,tech/manufacturer,tags,uiTags,base value,unlocked,hidden,hiddenEverywhere,cost_frigate,cost_dest,cost_cruiser,cost_capital,script,desc,short,sModDesc,sprite
My Mod Elite Trophy,ship_trophy_my_mod_elite,0,,Trophy Room,"offensive, restricted, no_drop_salvage",Weapons,0,FALSE,FALSE,FALSE,0,0,0,0,shiptrophy.hullmods.ConfigurableTrophyHullMod,"Elite parade tuning from showcased hulls increases energy weapon damage by %s and reduces shield damage taken by %s.",Elite trophy refit.,,graphics/hullmods/advanced_optics.png
```

`data/config/ship_trophy_room/hullmod_effects.csv`

```csv
hullModId,descParam0,descParam1,descParam2,descParam3,descParam4,descParam5,descParam6,descParam7,descParam8,descParam9,armorFlat,hullFlat,maxSpeedPercent,maneuverPercent,fluxCapacityPercent,fluxDissipationPercent,ballisticRangeFlat,energyRangeFlat,missileRangeFlat,ballisticDamagePercent,energyDamagePercent,missileDamagePercent,ballisticFluxCostPercent,energyFluxCostPercent,missileFluxCostPercent,shieldDamageTakenPercent,shieldUpkeepPercent,fighterRefitTimePercent,crewLossPercent,sensorProfileFlat,sensorStrengthFlat
ship_trophy_my_mod_elite,5%,5%,,,,,,,,,,,,,,,,,,,5,,,,,-5,,,,,
```

## Included no-code examples

Ship Trophy Room includes three optional integrations that use this same no-code hullmod system:

- Knights of Ludd: `ship_trophy_kol_benediction`
- United Aurora Federation: `ship_trophy_uaf_resonance`
- Iron Shell: `ship_trophy_iron_shell_drill`

You can copy those rows from Ship Trophy Room's CSV files and adjust them for your own mod.

## When you still need Java

The no-code script is for straightforward stat changes. You still need a custom Java hullmod script for:

- Incompatibilities with specific hullmods.
- OP cost discounts for specific weapon groups or tags.
- Effects that count as D-mods for other calculations.
- Combat listeners or time-based combat behavior.
- Per-weapon logic, per-system logic, or special UI behavior.

If you need Java, you can still use `subtypes.csv` for the showcase tracking and unlock. Just set `hullModId` to your custom hullmod id and set the hullmod's `script` column to your own class.
