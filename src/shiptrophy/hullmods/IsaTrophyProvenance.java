package shiptrophy.hullmods;

import java.util.LinkedHashSet;
import java.util.Set;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.IsaTrophyManager;

public class IsaTrophyProvenance extends BaseHullMod {
    private static final String ID_PREFIX = "ship_trophy_isa_smod_";

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
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, Misc.getHighlightColor(), "one Hall of Triumph hullmod");
    }

    private Set<String> getSMods(ShipVariantAPI variant) {
        Set<String> result = new LinkedHashSet<String>();
        if (variant == null) return result;
        result.addAll(variant.getSMods());
        result.addAll(variant.getSModdedBuiltIns());
        return result;
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
        } else if ("eccm".equals(sMod)) {
            stats.getEccmChance().modifyFlat(id, 0.5f);
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
        } else if ("frontemitter".equals(sMod)) {
            stats.getShieldDamageTakenMult().modifyMult(id, 0.95f);
        } else if ("insulatedengine".equals(sMod)) {
            stats.getEngineHealthBonus().modifyPercent(id, 100f);
            stats.getSensorProfile().modifyMult(id, 0.2f);
        } else if ("solar_shielding".equals(sMod)) {
            stats.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 0f);
        } else if ("stabilizedshieldemitter".equals(sMod)) {
            stats.getShieldSoftFluxConversion().modifyFlat(id, 0.1f);
        } else if ("surveying_equipment".equals(sMod)) {
            float bonus = bySize(hullSize, 5f, 10f, 20f, 40f);
            stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.HEAVY_MACHINERY)).modifyFlat(id, bonus);
            stats.getDynamic().getMod(Stats.getSurveyCostReductionId(Commodities.SUPPLIES)).modifyFlat(id, bonus);
        }
    }

    private float bySize(ShipAPI.HullSize hullSize, float frigate, float destroyer, float cruiser, float capital) {
        if (hullSize == ShipAPI.HullSize.FRIGATE) return frigate;
        if (hullSize == ShipAPI.HullSize.DESTROYER) return destroyer;
        if (hullSize == ShipAPI.HullSize.CRUISER) return cruiser;
        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) return capital;
        return 0f;
    }
}
