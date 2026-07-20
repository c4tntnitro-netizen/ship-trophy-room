package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class BlackLionInheritance extends BaseUniqueShowcaseHullMod {
    public static final String HULLMOD_ID = "ship_trophy_black_lion";
    public static final String REQUIRED_HULL_ID = "executor_2";
    public static final float ENERGY_DAMAGE_BONUS = 10f;
    public static final float ENERGY_FLUX_REDUCTION = 5f;

    @Override
    protected String getHullModId() {
        return HULLMOD_ID;
    }

    @Override
    protected String getRequiredHullId() {
        return REQUIRED_HULL_ID;
    }

    @Override
    protected String getRequiredShowcaseName() {
        return "The Black Lion";
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_DAMAGE_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -ENERGY_FLUX_REDUCTION);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(ENERGY_DAMAGE_BONUS) + "%";
        if (index == 1) return "" + Math.round(ENERGY_FLUX_REDUCTION) + "%";
        return null;
    }
}