package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import shiptrophy.TrophyDoctrine;

public class TriTachyonOptimization extends BaseTrophyDoctrineHullMod {
    public static final float SENSOR_PROFILE_REDUCTION = 15f;
    public static final float HULL_INTEGRITY_BONUS = 200f;

    @Override
    protected TrophyDoctrine getDoctrine() {
        return TrophyDoctrine.TT;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship) && !hasInsulatedEngines(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (hasInsulatedEngines(ship)) return "Incompatible with Insulated Engine Assembly";
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getVariant() != null && stats.getVariant().hasHullMod(HullMods.INSULATEDENGINE)) return;
        stats.getSensorProfile().modifyFlat(id, -SENSOR_PROFILE_REDUCTION);
        stats.getHullBonus().modifyFlat(id, HULL_INTEGRITY_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(SENSOR_PROFILE_REDUCTION);
        if (index == 1) return "" + Math.round(HULL_INTEGRITY_BONUS);
        return null;
    }

    private boolean hasInsulatedEngines(ShipAPI ship) {
        return ship != null && ship.getVariant() != null && ship.getVariant().hasHullMod(HullMods.INSULATEDENGINE);
    }
}
