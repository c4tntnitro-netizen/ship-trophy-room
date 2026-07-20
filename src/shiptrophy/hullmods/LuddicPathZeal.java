package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import shiptrophy.TrophyDoctrine;

public class LuddicPathZeal extends BaseTrophyDoctrineHullMod {
    public static final float SPEED_BONUS_FRIGATE = 20f;
    public static final float SPEED_BONUS_DESTROYER = 15f;
    public static final float SPEED_BONUS_CRUISER = 10f;
    public static final float SPEED_BONUS_CAPITAL = 5f;
    public static final float NON_MISSILE_RANGE_MULT = 0.85f;
    public static final float FIGHTER_REFIT_TIME_PENALTY = 25f;
    public static final float CREW_CASUALTY_PENALTY = 30f;
    public static final String DMOD_MARKER = "ship_trophy_lp_zeal_dmod_marker";

    @Override
    protected TrophyDoctrine getDoctrine() {
        return TrophyDoctrine.LP;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship) && !hasUnstableInjector(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (hasUnstableInjector(ship)) return "Incompatible with Unstable Injector";
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getVariant() != null && stats.getVariant().hasHullMod(HullMods.UNSTABLE_INJECTOR)) {
            removeHiddenMarker(stats, DMOD_MARKER);
            return;
        }
        addHiddenMarker(stats, DMOD_MARKER);
        stats.getMaxSpeed().modifyFlat(id, getSpeedBonus(hullSize));
        stats.getBallisticWeaponRangeBonus().modifyMult(id, NON_MISSILE_RANGE_MULT);
        stats.getEnergyWeaponRangeBonus().modifyMult(id, NON_MISSILE_RANGE_MULT);
        stats.getFighterRefitTimeMult().modifyMult(id, 1f + FIGHTER_REFIT_TIME_PENALTY * 0.01f);
        stats.getCrewLossMult().modifyPercent(id, CREW_CASUALTY_PENALTY);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(SPEED_BONUS_FRIGATE);
        if (index == 1) return "" + Math.round(SPEED_BONUS_DESTROYER);
        if (index == 2) return "" + Math.round(SPEED_BONUS_CRUISER);
        if (index == 3) return "" + Math.round(SPEED_BONUS_CAPITAL);
        if (index == 4) return "" + Math.round((1f - NON_MISSILE_RANGE_MULT) * 100f) + "%";
        if (index == 5) return "" + Math.round(FIGHTER_REFIT_TIME_PENALTY) + "%";
        if (index == 6) return "" + Math.round(CREW_CASUALTY_PENALTY) + "%";
        return null;
    }

    @Override
    protected String getDModCalculationNote() {
        return "This hullmod counts as a D-mod.";
    }

    private float getSpeedBonus(ShipAPI.HullSize hullSize) {
        if (hullSize == ShipAPI.HullSize.FRIGATE) return SPEED_BONUS_FRIGATE;
        if (hullSize == ShipAPI.HullSize.DESTROYER) return SPEED_BONUS_DESTROYER;
        if (hullSize == ShipAPI.HullSize.CRUISER) return SPEED_BONUS_CRUISER;
        if (hullSize == ShipAPI.HullSize.CAPITAL_SHIP) return SPEED_BONUS_CAPITAL;
        return 0f;
    }

    private boolean hasUnstableInjector(ShipAPI ship) {
        return ship != null && ship.getVariant() != null && ship.getVariant().hasHullMod(HullMods.UNSTABLE_INJECTOR);
    }
}
