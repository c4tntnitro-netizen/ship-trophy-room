package shiptrophy.hullmods;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.HullModEffect;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.IsaTrophyManager;

public class IsaTrophyProvenance extends BaseHullMod {
    private static final String ID_PREFIX = "ship_trophy_isa_smod_";
    private static final String AVARITIA_ID = "eis_avaritia";
    private static final String VANAGLORIA_ID = "eis_damperhull";
    private static final String GULA_ID = "eis_justatip";
    private static final String GULA_MIRROR_ID = ID_PREFIX + "compat_" + GULA_ID;
    private static final String DEFENSIVE_TARGETING_ARRAY_ID = "defensive_targeting_array";
    private static final String ESCORT_PACKAGE_ID = "escort_package";
    private static final String FRAGMENT_SWARM_RESPAWN_RATE_MULT = "fragment_swarm_respawn_rate_mult";
    private static final String FRAGMENT_SWARM_SIZE_MOD = "fragment_swarm_size_mod";
    private static final String HUNGERING_RIFT_HEAL_MOD = "hungering_rift_heal_mod";
    private static final float VANAGLORIA_EXTRA_RECHARGE_RATE = 0.5f;

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return IsaTrophyManager.isMasterworkComplete()
                && !TrophyHullModUtil.hasOtherTrophyHullMod(ship, IsaTrophyManager.PROVENANCE_HULLMOD_ID);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return IsaTrophyManager.isMasterworkComplete();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, IsaTrophyManager.PROVENANCE_HULLMOD_ID);
        if (other != null) return "Incompatible with " + other;
        return "Requires Onslaught XIV, Paragon, Invictus, Conquest, and Executor displays in the Hall of Triumph network";
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (!IsaTrophyManager.isMasterworkComplete() || stats == null || stats.getVariant() == null) return;
        for (String sMod : getSMods(stats.getVariant())) {
            applyExtraSModBonus(sMod, hullSize, stats, ID_PREFIX + sMod);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (!isActiveOn(ship) || !hasSMod(ship.getVariant(), GULA_ID)) return;
        if (!ship.hasListenerOfClass(DeferredGulaMirrorInstaller.class)
                && !ship.hasListenerOfClass(GulaDamageMirror.class)) {
            ship.addListener(new DeferredGulaMirrorInstaller(ship));
        }
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        if (!isActiveOn(ship) || fighter == null) return;

        if (hasSMod(ship.getVariant(), DEFENSIVE_TARGETING_ARRAY_ID)) {
            String rangeId = ID_PREFIX + DEFENSIVE_TARGETING_ARRAY_ID + "_fighter_range";
            fighter.getMutableStats().getBallisticWeaponRangeBonus().modifyFlat(rangeId, 100f);
            fighter.getMutableStats().getEnergyWeaponRangeBonus().modifyFlat(rangeId, 100f);
        }

        if (hasSMod(ship.getVariant(), GULA_ID)) {
            if (!fighter.hasListenerOfClass(DeferredGulaMirrorInstaller.class)
                    && !fighter.hasListenerOfClass(GulaDamageMirror.class)) {
                fighter.addListener(new DeferredGulaMirrorInstaller(fighter));
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!isActiveOn(ship) || amount <= 0f) return;

        installGulaMirrorIfNeeded(ship);

        if (hasSMod(ship.getVariant(), AVARITIA_ID)) {
            mirrorPositiveSourceModifiers(
                    ship.getMutableStats().getBallisticWeaponDamageMult(),
                    AVARITIA_ID,
                    ID_PREFIX + "compat_" + AVARITIA_ID + "_ballistic_damage");
            mirrorPositiveSourceModifiers(
                    ship.getMutableStats().getEnergyWeaponDamageMult(),
                    AVARITIA_ID,
                    ID_PREFIX + "compat_" + AVARITIA_ID + "_energy_damage");
            mirrorPositiveSourceModifiers(
                    ship.getMutableStats().getBallisticRoFMult(),
                    AVARITIA_ID,
                    ID_PREFIX + "compat_" + AVARITIA_ID + "_ballistic_rof");
            mirrorPositiveSourceModifiers(
                    ship.getMutableStats().getEnergyRoFMult(),
                    AVARITIA_ID,
                    ID_PREFIX + "compat_" + AVARITIA_ID + "_energy_rof");
        }

        if (ship.isDestroyer() && hasSMod(ship.getVariant(), ESCORT_PACKAGE_ID)) {
            mirrorBeneficialReductionSourceModifiers(
                    ship.getMutableStats().getShieldDamageTakenMult(),
                    ESCORT_PACKAGE_ID,
                    ID_PREFIX + ESCORT_PACKAGE_ID + "_shield_damage");
        }

        if (hasSMod(ship.getVariant(), VANAGLORIA_ID) && !hasSourceModifier(
                ship.getMutableStats().getHullDamageTakenMult(), VANAGLORIA_ID)) {
            advanceExternalHullMod(VANAGLORIA_ID, ship, amount * VANAGLORIA_EXTRA_RECHARGE_RATE);
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        if (ship != null && ship.getVariant() != null) {
            List<String> bonuses = getExtraSModBonusDescriptions(ship, hullSize);
            List<String> capped = getCappedSModBonusDescriptions(ship);
            if (bonuses.isEmpty()) {
                tooltip.addPara("Post-Awe cumulative hullmod effects on this ship: none.", opad,
                        Misc.getGrayColor(), "none");
            } else {
                tooltip.addPara("Post-Awe cumulative hullmod effects on this ship:", opad,
                        Misc.getHighlightColor(), "Post-Awe cumulative hullmod effects");
                for (String bonus : bonuses) {
                    tooltip.addPara("- " + bonus, 3f);
                }
            }
            if (!capped.isEmpty()) {
                tooltip.addPara("Already-maximized S-mod bonuses (no additional effect):", opad,
                        Misc.getHighlightColor(), "Already-maximized S-mod bonuses");
                for (String bonus : capped) {
                    tooltip.addPara("- " + bonus, 3f);
                }
            }
        }
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, Misc.getHighlightColor(), "one Hall of Triumph hullmod");
    }

    private List<String> getExtraSModBonusDescriptions(ShipAPI ship, ShipAPI.HullSize hullSize) {
        List<String> result = new ArrayList<String>();
        for (String sMod : getSMods(ship.getVariant())) {
            String description = getExtraSModBonusDescription(sMod, hullSize, ship);
            if (description != null) result.add(description);
        }
        return result;
    }

    private List<String> getCappedSModBonusDescriptions(ShipAPI ship) {
        List<String> result = new ArrayList<String>();
        for (String sMod : getSMods(ship.getVariant())) {
            String description = getCappedSModBonusDescription(sMod);
            if (description != null) result.add(description);
        }
        return result;
    }

    private String getCappedSModBonusDescription(String sMod) {
        if ("eccm".equals(sMod)) {
            return "ECCM Package: flare avoidance and ECM-range protection are already complete";
        } else if ("solar_shielding".equals(sMod)) {
            return "Solar Shielding: corona protection is already complete";
        } else if ("pointdefenseai".equals(sMod)) {
            return "Integrated Point Defense AI: all eligible small weapons are already classified as point defense";
        } else if ("neural_interface".equals(sMod)) {
            return "Neural Interface: transfers are already instant";
        } else if ("neural_integrator".equals(sMod)) {
            return "Neural Integrator: transfers are already instant";
        } else if ("adaptiveshields".equals(sMod)) {
            return "Shield Conversion - Omni: the shield-arc penalty is already fully negated";
        } else if ("militarized_subsystems".equals(sMod)) {
            return "Militarized Subsystems: the increased crew requirement is already fully negated";
        }
        return null;
    }

    private String getExtraSModBonusDescription(String sMod, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if ("advancedshieldemitter".equals(sMod)) {
            return "Accelerated Shields: +300% shield turn and unfold rate";
        } else if ("turretgyros".equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                return "Advanced Turret Gyros: +50% damage to missiles and fighters, +30% to frigates, +20% to destroyers, and +10% to cruisers";
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                return "Advanced Turret Gyros: +50% damage to missiles and fighters, +20% to frigates, and +10% to destroyers";
            } else if (hullSize == ShipAPI.HullSize.DESTROYER) {
                return "Advanced Turret Gyros: +50% damage to missiles and fighters, and +10% to frigates";
            }
            return "Advanced Turret Gyros: +50% damage to missiles and fighters";
        } else if ("armoredweapons".equals(sMod)) {
            return "Armored Weapon Mounts: +21% ballistic and energy weapon rate of fire";
        } else if ("augmentedengines".equals(sMod)) {
            return "Augmented Drive Field: +4 maximum burn";
        } else if ("autorepair".equals(sMod)) {
            return "Automated Repair Unit: weapon and engine repair time reduced to 19%; overload duration reduced to 45%";
        } else if ("auxiliarythrusters".equals(sMod)) {
            return "Auxiliary Thrusters: 4x zero-flux turn-rate bonus and +20 zero-flux speed";
        } else if ("blast_doors".equals(sMod)) {
            return "Blast Doors: -89% crew losses";
        } else if ("converted_hangar".equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                return "Converted Hangar: +50% fighter replacement-rate recovery";
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                return "Converted Hangar: +20% fighter replacement-rate recovery";
            }
            return null;
        } else if ("converted_fighterbay".equals(sMod)) {
            int bays = Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
            float singleReduction = Math.min(1f, bays * 0.15f);
            int reduction = Math.round((1f - (1f - singleReduction) * (1f - singleReduction)) * 100f);
            if (reduction > 0) return "Converted Fighter Bay: -" + reduction + "% monthly supply use";
            return null;
        } else if ("dedicated_targeting_core".equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                return "Dedicated Targeting Core: +70% ballistic and energy weapon range";
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                return "Dedicated Targeting Core: +45% ballistic and energy weapon range";
            }
            return null;
        } else if (DEFENSIVE_TARGETING_ARRAY_ID.equals(sMod)) {
            return "Defensive Targeting Array: +200 fighter ballistic and energy weapon range";
        } else if (ESCORT_PACKAGE_ID.equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.DESTROYER) {
                return "Escort Package: up to -19% shield damage taken at full connection";
            }
            return null;
        } else if ("magazines".equals(sMod)) {
            return "Expanded Magazines: +100% ballistic and energy ammo regeneration";
        } else if ("extendedshieldemitter".equals(sMod)) {
            return "Extended Shields: +180 degrees shield arc";
        } else if ("fluxbreakers".equals(sMod)) {
            return "Resistant Flux Conduits: +45% vent rate";
        } else if ("fluxcoil".equals(sMod)) {
            float capacity = bySize(hullSize, 600f, 1200f, 1800f, 3000f)
                    + 2f * bySize(hullSize, 200f, 400f, 600f, 1000f);
            return "Flux Coil Adjunct: +" + Math.round(capacity) + " flux capacity";
        } else if ("fluxdistributor".equals(sMod)) {
            float dissipation = bySize(hullSize, 30f, 60f, 90f, 150f)
                    + 2f * bySize(hullSize, 10f, 20f, 30f, 50f);
            return "Flux Distributor: +" + Math.round(dissipation) + " flux dissipation";
        } else if ("shield_shunt".equals(sMod)) {
            return "Shield Shunt: +45% armor";
        } else if ("high_scatter_amp".equals(sMod)) {
            return "High Scatter Amplifier: +20% beam weapon damage";
        } else if ("frontemitter".equals(sMod)) {
            return "Shield Conversion - Front: -9.75% shield damage taken";
        } else if ("recovery_shuttles".equals(sMod)) {
            return "Recovery Shuttles: -99% fighter pilot casualties";
        } else if ("additional_berthing".equals(sMod)) {
            return "Additional Berthing: +" + Math.round(3f * getLogisticsBaseBonus(
                    hullSize, ship.getHullSpec().getMaxCrew())) + " crew capacity";
        } else if ("auxiliary_fuel_tanks".equals(sMod)) {
            return "Auxiliary Fuel Tanks: +" + Math.round(3f * getLogisticsBaseBonus(
                    hullSize, ship.getHullSpec().getFuel())) + " fuel capacity";
        } else if ("efficiency_overhaul".equals(sMod)) {
            return "Efficiency Overhaul: -37% minimum crew, monthly supply use, and fuel use";
        } else if ("expanded_cargo_holds".equals(sMod)) {
            return "Expanded Cargo Holds: +" + Math.round(3f * getLogisticsBaseBonus(
                    hullSize, ship.getHullSpec().getCargo())) + " cargo capacity";
        } else if ("hiressensors".equals(sMod)) {
            return "High Resolution Sensors: +" + Math.round(bySize(
                    hullSize, 2000f, 3000f, 4000f, 5000f)) + " in-combat vision range";
        } else if ("insulatedengine".equals(sMod)) {
            return "Insulated Engine Assembly: +300% engine health and -98% sensor profile";
        } else if ("stabilizedshieldemitter".equals(sMod)) {
            return "Stabilized Shields: 20% of shield damage converted to soft flux";
        } else if ("surveying_equipment".equals(sMod)) {
            int reduction = Math.round(3f * bySize(hullSize, 5f, 10f, 20f, 40f));
            return "Surveying Equipment: -" + reduction + " heavy machinery and supplies required for surveys";
        } else if ("secondary_fabricator".equals(sMod)) {
            return "Secondary Fabricator: +70% fragment replacement rate";
        } else if ("fragment_coordinator".equals(sMod)) {
            return "Fragment Coordinator: +140% fragment swarm size";
        } else if ("shrouded_mantle".equals(sMod)) {
            return "Shrouded Mantle: receives 100% of Hungering Rift healing";
        } else if (AVARITIA_ID.equals(sMod)) {
            return "Avaritia Capacity Overhaul: doubles its active S-mod weapon damage and rate-of-fire bonuses";
        } else if (VANAGLORIA_ID.equals(sMod)) {
            return "Vanagloria Ionized Armor: doubles its S-mod recharge-time reduction";
        } else if (GULA_ID.equals(sMod)) {
            return "Gula Tandem Warheads: doubles its S-mod bonus damage; eligibility and damage threshold are unchanged";
        }
        return null;
    }

    private Set<String> getSMods(ShipVariantAPI variant) {
        Set<String> result = new LinkedHashSet<String>();
        if (variant == null) return result;
        result.addAll(variant.getSMods());
        result.addAll(variant.getSModdedBuiltIns());
        return result;
    }

    private boolean isActiveOn(ShipAPI ship) {
        return IsaTrophyManager.isMasterworkComplete()
                && ship != null
                && ship.getVariant() != null;
    }

    private boolean hasSMod(ShipVariantAPI variant, String hullModId) {
        return getSMods(variant).contains(hullModId);
    }

    private void installGulaMirrorIfNeeded(ShipAPI ship) {
        if (!hasSMod(ship.getVariant(), GULA_ID)
                || ship.hasListenerOfClass(GulaDamageMirror.class)) {
            return;
        }
        ship.addListener(new GulaDamageMirror());
    }

    /**
     * Clean-room compatibility: use the external hullmod through Starsector's
     * public HullModEffect API. No external classes or private state are read.
     */
    private void advanceExternalHullMod(String hullModId, ShipAPI ship, float amount) {
        if (amount <= 0f) return;
        try {
            HullModEffect effect = Global.getSettings().getHullModSpec(hullModId).getEffect();
            if (effect != null && effect != this) {
                effect.advanceInCombat(ship, amount);
            }
        } catch (RuntimeException ignored) {
            // Optional-mod compatibility: safely do nothing when the spec is unavailable.
        }
    }

    /**
     * Mirrors only modifiers published under the external hullmod's public ID.
     * Eligibility, timing, and values remain owned by the external hullmod.
     */
    private static void mirrorPositiveSourceModifiers(MutableStat stat, String sourceId, String mirrorId) {
        stat.unmodify(mirrorId);

        float flat = positiveSum(stat.getFlatMods(), sourceId);
        float percent = positiveSum(stat.getPercentMods(), sourceId);
        float mult = positiveProduct(stat.getMultMods(), sourceId);

        if (flat > 0f) stat.modifyFlat(mirrorId, flat);
        if (percent > 0f) stat.modifyPercent(mirrorId, percent);
        if (mult > 1f) stat.modifyMult(mirrorId, mult);
    }

    private static void mirrorBeneficialReductionSourceModifiers(MutableStat stat, String sourceId, String mirrorId) {
        stat.unmodify(mirrorId);

        float mult = beneficialReductionProduct(stat.getMultMods(), sourceId);
        if (mult > 0f && mult < 1f) stat.modifyMult(mirrorId, mult);
    }

    private static float positiveSum(Map<String, MutableStat.StatMod> mods, String sourceId) {
        float result = 0f;
        for (MutableStat.StatMod mod : mods.values()) {
            if (isExternalSource(mod, sourceId) && mod.getValue() > 0f) {
                result += mod.getValue();
            }
        }
        return result;
    }

    private static float positiveProduct(Map<String, MutableStat.StatMod> mods, String sourceId) {
        float result = 1f;
        for (MutableStat.StatMod mod : mods.values()) {
            if (isExternalSource(mod, sourceId) && mod.getValue() > 1f) {
                result *= mod.getValue();
            }
        }
        return result;
    }

    private static float beneficialReductionProduct(Map<String, MutableStat.StatMod> mods, String sourceId) {
        float result = 1f;
        for (MutableStat.StatMod mod : mods.values()) {
            if (isExternalSource(mod, sourceId) && mod.getValue() > 0f && mod.getValue() < 1f) {
                result *= mod.getValue();
            }
        }
        return result;
    }

    private static boolean hasSourceModifier(MutableStat stat, String sourceId) {
        return hasSourceModifier(stat.getFlatMods(), sourceId)
                || hasSourceModifier(stat.getPercentMods(), sourceId)
                || hasSourceModifier(stat.getMultMods(), sourceId);
    }

    private static boolean hasSourceModifier(Map<String, MutableStat.StatMod> mods, String sourceId) {
        for (MutableStat.StatMod mod : mods.values()) {
            if (isExternalSource(mod, sourceId)) return true;
        }
        return false;
    }

    private static boolean isExternalSource(MutableStat.StatMod mod, String sourceId) {
        if (mod == null || mod.getSource() == null) return false;
        String source = mod.getSource();
        return !source.startsWith(ID_PREFIX) && source.contains(sourceId);
    }

    private static class GulaDamageMirror implements DamageDealtModifier {
        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage,
                Vector2f point, boolean shieldHit) {
            if (damage == null || damage.getModifier() == null) return null;
            mirrorPositiveSourceModifiers(damage.getModifier(), GULA_ID, GULA_MIRROR_ID);
            return null;
        }
    }

    private static class DeferredGulaMirrorInstaller implements AdvanceableListener {
        private final ShipAPI fighter;
        private boolean installed;

        private DeferredGulaMirrorInstaller(ShipAPI fighter) {
            this.fighter = fighter;
        }

        @Override
        public void advance(float amount) {
            if (installed || fighter == null) return;
            installed = true;
            if (!fighter.hasListenerOfClass(GulaDamageMirror.class)) {
                fighter.addListener(new GulaDamageMirror());
            }
        }
    }

    private void applyExtraSModBonus(String sMod, ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if ("advancedshieldemitter".equals(sMod)) {
            stats.getShieldTurnRateMult().modifyPercent(id, 100f);
            stats.getShieldUnfoldRateMult().modifyPercent(id, 100f);
        } else if ("turretgyros".equals(sMod)) {
            stats.getDamageToMissiles().modifyPercent(id, 25f);
            stats.getDamageToFighters().modifyPercent(id, 25f);
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                stats.getDamageToFrigates().modifyPercent(id, 15f);
                stats.getDamageToDestroyers().modifyPercent(id, 10f);
                stats.getDamageToCruisers().modifyPercent(id, 5f);
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                stats.getDamageToFrigates().modifyPercent(id, 10f);
                stats.getDamageToDestroyers().modifyPercent(id, 5f);
            } else if (hullSize == ShipAPI.HullSize.DESTROYER) {
                stats.getDamageToFrigates().modifyPercent(id, 5f);
            }
        } else if ("armoredweapons".equals(sMod)) {
            stats.getBallisticRoFMult().modifyMult(id, 1.1f);
            stats.getEnergyRoFMult().modifyMult(id, 1.1f);
        } else if ("augmentedengines".equals(sMod)) {
            stats.getMaxBurnLevel().modifyFlat(id, 1f);
        } else if ("autorepair".equals(sMod)) {
            stats.getCombatEngineRepairTimeMult().modifyMult(id, 0.75f);
            stats.getCombatWeaponRepairTimeMult().modifyMult(id, 0.75f);
            stats.getOverloadTimeMod().modifyMult(id, 0.67f);
        } else if ("auxiliarythrusters".equals(sMod)) {
            stats.getDynamic().getStat(Stats.ZERO_FLUX_BOOST_TURN_RATE_BONUS_MULT).modifyMult(id, 2f);
            stats.getZeroFluxSpeedBoost().modifyFlat(id, 10f);
        } else if ("blast_doors".equals(sMod)) {
            stats.getCrewLossMult().modifyMult(id, 0.75f);
        } else if ("converted_hangar".equals(sMod)) {
            float bonus = hullSize == ShipAPI.HullSize.CAPITAL_SHIP ? 25f
                    : hullSize == ShipAPI.HullSize.CRUISER ? 10f : 0f;
            if (bonus > 0f) {
                stats.getDynamic().getStat(Stats.REPLACEMENT_RATE_INCREASE_MULT).modifyPercent(id, bonus);
            }
        } else if ("converted_fighterbay".equals(sMod)) {
            int bays = Math.round(stats.getNumFighterBays().getBaseValue());
            float bonus = Math.min(1f, bays * 0.15f);
            if (bonus > 0f) stats.getSuppliesPerMonth().modifyMult(id, 1f - bonus);
        } else if ("dedicated_targeting_core".equals(sMod)) {
            float bonus = hullSize == ShipAPI.HullSize.CAPITAL_SHIP ? 10f : hullSize == ShipAPI.HullSize.CRUISER ? 5f : 0f;
            if (bonus > 0f) {
                stats.getBallisticWeaponRangeBonus().modifyPercent(id, bonus);
                stats.getEnergyWeaponRangeBonus().modifyPercent(id, bonus);
            }
        } else if ("magazines".equals(sMod)) {
            stats.getBallisticAmmoRegenMult().modifyPercent(id, 50f);
            stats.getEnergyAmmoRegenMult().modifyPercent(id, 50f);
        } else if ("extendedshieldemitter".equals(sMod)) {
            stats.getShieldArcBonus().modifyFlat(id, 60f);
        } else if ("fluxbreakers".equals(sMod)) {
            stats.getVentRateMult().modifyPercent(id, 10f);
        } else if ("fluxcoil".equals(sMod)) {
            stats.getFluxCapacity().modifyFlat(id, bySize(hullSize, 200f, 400f, 600f, 1000f));
        } else if ("fluxdistributor".equals(sMod)) {
            stats.getFluxDissipation().modifyFlat(id, bySize(hullSize, 10f, 20f, 30f, 50f));
        } else if ("shield_shunt".equals(sMod)) {
            stats.getArmorBonus().modifyPercent(id, 15f);
        } else if ("high_scatter_amp".equals(sMod)) {
            stats.getBeamWeaponDamageMult().modifyPercent(id, 5f);
        } else if ("frontemitter".equals(sMod)) {
            stats.getShieldDamageTakenMult().modifyMult(id, 0.95f);
        } else if ("recovery_shuttles".equals(sMod)) {
            stats.getDynamic().getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 0.2f);
        } else if ("additional_berthing".equals(sMod)) {
            stats.getMaxCrewMod().modifyFlat(id, getLogisticsBaseBonus(
                    hullSize, stats.getVariant().getHullSpec().getMaxCrew()));
        } else if ("auxiliary_fuel_tanks".equals(sMod)) {
            stats.getFuelMod().modifyFlat(id, getLogisticsBaseBonus(
                    hullSize, stats.getVariant().getHullSpec().getFuel()));
        } else if ("efficiency_overhaul".equals(sMod)) {
            stats.getMinCrewMod().modifyMult(id, 0.9f);
            stats.getSuppliesPerMonth().modifyMult(id, 0.9f);
            stats.getFuelUseMod().modifyMult(id, 0.9f);
        } else if ("expanded_cargo_holds".equals(sMod)) {
            stats.getCargoMod().modifyFlat(id, getLogisticsBaseBonus(
                    hullSize, stats.getVariant().getHullSpec().getCargo()));
        } else if ("hiressensors".equals(sMod)) {
            stats.getSightRadiusMod().modifyFlat(id, bySize(
                    hullSize, 1000f, 1500f, 2000f, 2500f));
        } else if ("insulatedengine".equals(sMod)) {
            stats.getEngineHealthBonus().modifyPercent(id, 100f);
            stats.getSensorProfile().modifyMult(id, 0.2f);
        } else if ("stabilizedshieldemitter".equals(sMod)) {
            stats.getShieldSoftFluxConversion().modifyFlat(id, 0.1f);
        } else if ("surveying_equipment".equals(sMod)) {
            float bonus = bySize(hullSize, 5f, 10f, 20f, 40f);
            stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.HEAVY_MACHINERY)).modifyFlat(id, bonus);
            stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.SUPPLIES)).modifyFlat(id, bonus);
        } else if ("secondary_fabricator".equals(sMod)) {
            stats.getDynamic().getStat(FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyPercent(id, 20f);
        } else if ("fragment_coordinator".equals(sMod)) {
            stats.getDynamic().getMod(FRAGMENT_SWARM_SIZE_MOD).modifyPercent(id, 40f);
        } else if ("shrouded_mantle".equals(sMod)) {
            stats.getDynamic().getMod(HUNGERING_RIFT_HEAL_MOD).modifyFlat(id, 0.5f);
        }
    }

    private float getLogisticsBaseBonus(ShipAPI.HullSize hullSize, float baseCapacity) {
        return Math.max(bySize(hullSize, 30f, 60f, 100f, 200f), baseCapacity * 0.3f);
    }

    private float bySize(ShipAPI.HullSize hullSize, float frigate, float destroyer, float cruiser, float capital) {
        if (hullSize == ShipAPI.HullSize.FRIGATE) return frigate;
        if (hullSize == ShipAPI.HullSize.DESTROYER) return destroyer;
        if (hullSize == ShipAPI.HullSize.CRUISER) return cruiser;
        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) return capital;
        return 0f;
    }
}
