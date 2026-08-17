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
import shiptrophy.ShipTrophyL10n;

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
        return TrophyHullModUtil.areUnlocksEnabled()
                && IsaTrophyManager.isMasterworkComplete()
                && !TrophyHullModUtil.hasOtherTrophyHullMod(ship, IsaTrophyManager.PROVENANCE_HULLMOD_ID);
    }

    @Override
    public boolean showInRefitScreenModPickerFor(ShipAPI ship) {
        return TrophyHullModUtil.areUnlocksEnabled()
                && IsaTrophyManager.isMasterworkComplete();
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (!TrophyHullModUtil.areUnlocksEnabled()) {
            return ShipTrophyL10n.get("hullmod_disabled_reason");
        }
        String other = TrophyHullModUtil.getOtherTrophyHullModName(ship, IsaTrophyManager.PROVENANCE_HULLMOD_ID);
        if (other != null) return ShipTrophyL10n.format("hullmod_incompatible", other);
        return ShipTrophyL10n.get("hullmod_masterwork_requires");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (!TrophyHullModUtil.areEffectsEnabled() || !IsaTrophyManager.isMasterworkComplete()
                || stats == null || stats.getVariant() == null) return;
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
        if (!TrophyHullModUtil.areUnlocksEnabled()) {
            tooltip.addPara(ShipTrophyL10n.get("hullmod_effects_disabled"),
                    opad, Misc.getNegativeHighlightColor(),
                    ShipTrophyL10n.get("hullmod_disabled_highlight"));
        } else if (ship != null && ship.getVariant() != null) {
            List<String> bonuses = getExtraSModBonusDescriptions(ship, hullSize);
            List<String> capped = getCappedSModBonusDescriptions(ship);
            if (bonuses.isEmpty()) {
                tooltip.addPara(ShipTrophyL10n.get("provenance_none"), opad,
                        Misc.getGrayColor(),
                        ShipTrophyL10n.get("provenance_none_highlight"));
            } else {
                tooltip.addPara(ShipTrophyL10n.get("provenance_heading"), opad,
                        Misc.getHighlightColor(),
                        ShipTrophyL10n.get("provenance_heading_highlight"));
                for (String bonus : bonuses) {
                    tooltip.addPara("- " + bonus, 3f);
                }
            }
            if (!capped.isEmpty()) {
                tooltip.addPara(ShipTrophyL10n.get("provenance_capped"), opad,
                        Misc.getHighlightColor(),
                        ShipTrophyL10n.get("provenance_capped_highlight"));
                for (String bonus : capped) {
                    tooltip.addPara("- " + bonus, 3f);
                }
            }
        }
        tooltip.addPara(ShipTrophyL10n.get("hullmod_only_one"), opad,
                Misc.getHighlightColor(),
                ShipTrophyL10n.get("hullmod_only_one_highlight"));
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
            return ShipTrophyL10n.get("provenance_capped_eccm");
        } else if ("solar_shielding".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_capped_solar");
        } else if ("pointdefenseai".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_capped_pd_ai");
        } else if ("neural_interface".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_capped_neural");
        } else if ("neural_integrator".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_capped_integrator");
        } else if ("adaptiveshields".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_capped_omni");
        } else if ("militarized_subsystems".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_capped_militarized");
        }
        return null;
    }

    private String getExtraSModBonusDescription(String sMod, ShipAPI.HullSize hullSize, ShipAPI ship) {
        if ("advancedshieldemitter".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_accelerated_shields");
        } else if ("turretgyros".equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                return ShipTrophyL10n.get("provenance_turret_capital");
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                return ShipTrophyL10n.get("provenance_turret_cruiser");
            } else if (hullSize == ShipAPI.HullSize.DESTROYER) {
                return ShipTrophyL10n.get("provenance_turret_destroyer");
            }
            return ShipTrophyL10n.get("provenance_turret_frigate");
        } else if ("armoredweapons".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_armored_mounts");
        } else if ("augmentedengines".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_augmented_drive");
        } else if ("autorepair".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_auto_repair");
        } else if ("auxiliarythrusters".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_aux_thrusters");
        } else if ("blast_doors".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_blast_doors");
        } else if ("converted_hangar".equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                return ShipTrophyL10n.get("provenance_hangar_capital");
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                return ShipTrophyL10n.get("provenance_hangar_cruiser");
            }
            return null;
        } else if ("converted_fighterbay".equals(sMod)) {
            int bays = Math.round(ship.getMutableStats().getNumFighterBays().getBaseValue());
            float singleReduction = Math.min(1f, bays * 0.15f);
            int reduction = Math.round((1f - (1f - singleReduction) * (1f - singleReduction)) * 100f);
            if (reduction > 0) return ShipTrophyL10n.format(
                    "provenance_fighter_bay", reduction);
            return null;
        } else if ("dedicated_targeting_core".equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) {
                return ShipTrophyL10n.get("provenance_targeting_capital");
            } else if (hullSize == ShipAPI.HullSize.CRUISER) {
                return ShipTrophyL10n.get("provenance_targeting_cruiser");
            }
            return null;
        } else if (DEFENSIVE_TARGETING_ARRAY_ID.equals(sMod)) {
            return ShipTrophyL10n.get("provenance_defensive_array");
        } else if (ESCORT_PACKAGE_ID.equals(sMod)) {
            if (hullSize == ShipAPI.HullSize.DESTROYER) {
                return ShipTrophyL10n.get("provenance_escort_package");
            }
            return null;
        } else if ("magazines".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_magazines");
        } else if ("extendedshieldemitter".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_extended_shields");
        } else if ("fluxbreakers".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_fluxbreakers");
        } else if ("fluxcoil".equals(sMod)) {
            float capacity = bySize(hullSize, 600f, 1200f, 1800f, 3000f)
                    + 2f * bySize(hullSize, 200f, 400f, 600f, 1000f);
            return ShipTrophyL10n.format(
                    "provenance_flux_coil", Math.round(capacity));
        } else if ("fluxdistributor".equals(sMod)) {
            float dissipation = bySize(hullSize, 30f, 60f, 90f, 150f)
                    + 2f * bySize(hullSize, 10f, 20f, 30f, 50f);
            return ShipTrophyL10n.format(
                    "provenance_flux_distributor", Math.round(dissipation));
        } else if ("shield_shunt".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_shield_shunt");
        } else if ("high_scatter_amp".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_scatter_amp");
        } else if ("frontemitter".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_front_shield");
        } else if ("recovery_shuttles".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_recovery_shuttles");
        } else if ("additional_berthing".equals(sMod)) {
            return ShipTrophyL10n.format("provenance_berthing",
                    Math.round(3f * getLogisticsBaseBonus(
                            hullSize, ship.getHullSpec().getMaxCrew())));
        } else if ("auxiliary_fuel_tanks".equals(sMod)) {
            return ShipTrophyL10n.format("provenance_fuel_tanks",
                    Math.round(3f * getLogisticsBaseBonus(
                            hullSize, ship.getHullSpec().getFuel())));
        } else if ("efficiency_overhaul".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_efficiency");
        } else if ("expanded_cargo_holds".equals(sMod)) {
            return ShipTrophyL10n.format("provenance_cargo",
                    Math.round(3f * getLogisticsBaseBonus(
                            hullSize, ship.getHullSpec().getCargo())));
        } else if ("hiressensors".equals(sMod)) {
            return ShipTrophyL10n.format("provenance_sensors", Math.round(bySize(
                    hullSize, 2000f, 3000f, 4000f, 5000f)));
        } else if ("insulatedengine".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_insulated_engine");
        } else if ("stabilizedshieldemitter".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_stabilized_shields");
        } else if ("surveying_equipment".equals(sMod)) {
            int reduction = Math.round(3f * bySize(hullSize, 5f, 10f, 20f, 40f));
            return ShipTrophyL10n.format("provenance_surveying", reduction);
        } else if ("secondary_fabricator".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_secondary_fabricator");
        } else if ("fragment_coordinator".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_fragment_coordinator");
        } else if ("shrouded_mantle".equals(sMod)) {
            return ShipTrophyL10n.get("provenance_shrouded_mantle");
        } else if (AVARITIA_ID.equals(sMod)) {
            return ShipTrophyL10n.get("provenance_avaritia");
        } else if (VANAGLORIA_ID.equals(sMod)) {
            return ShipTrophyL10n.get("provenance_vanagloria");
        } else if (GULA_ID.equals(sMod)) {
            return ShipTrophyL10n.get("provenance_gula");
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
        return TrophyHullModUtil.areEffectsEnabled()
                && IsaTrophyManager.isMasterworkComplete()
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
            if (!TrophyHullModUtil.areEffectsEnabled()) return null;
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
            if (!TrophyHullModUtil.areEffectsEnabled() || installed || fighter == null) return;
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
