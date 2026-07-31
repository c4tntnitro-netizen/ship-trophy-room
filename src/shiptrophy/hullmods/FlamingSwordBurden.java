package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

/** Maneuverability cost of mounting the Dextral Shard's Flaming Sword. */
public class FlamingSwordBurden extends BaseHullMod {
    private static final float MANEUVERABILITY_MULT = 0.1f;

    @Override
    public void applyEffectsBeforeShipCreation(
            HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getAcceleration().modifyMult(id, MANEUVERABILITY_MULT);
        stats.getDeceleration().modifyMult(id, MANEUVERABILITY_MULT);
        stats.getMaxTurnRate().modifyMult(id, MANEUVERABILITY_MULT);
        stats.getTurnAcceleration().modifyMult(id, MANEUVERABILITY_MULT);
    }
}
