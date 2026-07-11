package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import shiptrophy.TrophyDoctrine;

public class XIVBattlegroupLegacy extends BaseTrophyDoctrineHullMod {
    public static final float ARMOR_BONUS = 100f;
    public static final float SPEED_MANEUVER_MULT = 0.92f;
    public static final float FLUX_BONUS_PERCENT = 5f;

    @Override
    protected TrophyDoctrine getDoctrine() {
        return TrophyDoctrine.XIV;
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyFlat(id, ARMOR_BONUS);
        stats.getMaxSpeed().modifyMult(id, SPEED_MANEUVER_MULT);
        stats.getAcceleration().modifyMult(id, SPEED_MANEUVER_MULT);
        stats.getDeceleration().modifyMult(id, SPEED_MANEUVER_MULT);
        stats.getMaxTurnRate().modifyMult(id, SPEED_MANEUVER_MULT);
        stats.getTurnAcceleration().modifyMult(id, SPEED_MANEUVER_MULT);
        stats.getFluxCapacity().modifyPercent(id, FLUX_BONUS_PERCENT);
        stats.getFluxDissipation().modifyPercent(id, FLUX_BONUS_PERCENT);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(ARMOR_BONUS);
        if (index == 1) return "8%";
        if (index == 2) return "5%";
        return null;
    }
}
