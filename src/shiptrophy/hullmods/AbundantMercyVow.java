package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class AbundantMercyVow extends BaseUniqueShowcaseHullMod {
    public static final String HULLMOD_ID = "ship_trophy_abundant_mercy";
    public static final String REQUIRED_HULL_ID = "invictus_kh";
    public static final float CREW_CASUALTY_REDUCTION = 25f;
    public static final float FIGHTER_REFIT_REDUCTION = 10f;

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
        return "Abundant Mercy";
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getCrewLossMult().modifyMult(id, 1f - CREW_CASUALTY_REDUCTION * 0.01f);
        stats.getFighterRefitTimeMult().modifyMult(id, 1f - FIGHTER_REFIT_REDUCTION * 0.01f);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(CREW_CASUALTY_REDUCTION) + "%";
        if (index == 1) return "" + Math.round(FIGHTER_REFIT_REDUCTION) + "%";
        return null;
    }
}