package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import shiptrophy.TrophyNetwork;

public class TrophyOpDiscountMarker extends BaseHullMod {
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats == null || stats.getVariant() == null) return;
        ShipVariantAPI variant = stats.getVariant();

        if (TrophyOpDiscounts.isDiscountHullMod(id, Gaze.DISCOUNT_PREFIX)
                && (!variant.hasHullMod(Gaze.HULLMOD_ID) || !TrophyNetwork.isZigguratShowcased())) {
            TrophyOpDiscounts.clearDiscount(variant, Gaze.DISCOUNT_PREFIX);
        } else if (TrophyOpDiscounts.isDiscountHullMod(id, Contempt.DISCOUNT_PREFIX)
                && (!variant.hasHullMod(Contempt.HULLMOD_ID) || !TrophyNetwork.isOnslaughtMkIShowcased())) {
            TrophyOpDiscounts.clearDiscount(variant, Contempt.DISCOUNT_PREFIX);
        }
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }
}
