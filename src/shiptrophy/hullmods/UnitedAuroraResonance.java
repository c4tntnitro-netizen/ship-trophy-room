package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class UnitedAuroraResonance extends BaseTrophyDoctrineHullMod {
    public static final String SUBTYPE_ID = "uaf";
    public static final float FLUX_BONUS = 5f;
    public static final float FIGHTER_REFIT_REDUCTION = 10f;

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS);
        stats.getFluxDissipation().modifyPercent(id, FLUX_BONUS);
        stats.getFighterRefitTimeMult().modifyMult(id, 1f - FIGHTER_REFIT_REDUCTION * 0.01f);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(FLUX_BONUS) + "%";
        if (index == 1) return "" + Math.round(FIGHTER_REFIT_REDUCTION) + "%";
        return null;
    }
}
