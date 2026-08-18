package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

/** No-effect replacement used by the no-trophy-hullmods distribution. */
public class DisabledTrophyHullMod extends BaseHullMod {
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        return "Trophy hullmods are disabled in this Hall of Triumph edition";
    }
}
