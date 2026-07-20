package shiptrophy.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.TrophyNetwork;

public class Gaze extends BaseUniqueTrophyHullMod {
    public static final String HULLMOD_ID = "ship_trophy_gaze";
    public static final String REQUIRED_BASE_HULL_ID = "ziggurat";
    public static final String DISCOUNT_PREFIX = "ship_trophy_gaze_op_discount_";
    public static final float VENT_RATE_MULT = 2f;

    @Override
    protected String getHullModId() {
        return HULLMOD_ID;
    }

    @Override
    protected String getRequiredShowcaseName() {
        return "Ziggurat";
    }

    @Override
    protected boolean isUnlocked() {
        return TrophyNetwork.isZigguratShowcased();
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        if (!super.isApplicableToShip(ship)) return false;
        return ship == null || ship.getVariant() == null || !ship.getVariant().hasHullMod(HullMods.FLUXBREAKERS);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && ship.getVariant() != null && ship.getVariant().hasHullMod(HullMods.FLUXBREAKERS)) {
            return "Incompatible with Resistant Flux Conduits";
        }
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void syncDiscountForVariant(ShipVariantAPI variant, boolean unlocked) {
        clearLegacyDiscountMarkers(variant);
    }

    public static void syncVariant(ShipVariantAPI variant, TrophyNetwork.CollectionStats stats) {
        clearLegacyDiscountMarkers(variant);
    }

    public static void syncVariant(ShipVariantAPI variant, boolean unlocked) {
        clearLegacyDiscountMarkers(variant);
    }

    private static void clearLegacyDiscountMarkers(ShipVariantAPI variant) {
        TrophyOpDiscounts.clearDiscount(variant, DISCOUNT_PREFIX);
    }

    @Override
    protected int getCurrentDiscount(ShipVariantAPI variant) {
        return 0;
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        if (stats == null) return;
        ShipVariantAPI variant = stats.getVariant();
        if (variant == null || !variant.hasHullMod(HullMods.FLUXBREAKERS)) {
            stats.getVentRateMult().modifyMult(id, VENT_RATE_MULT);
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara("Only one Hall of Triumph hullmod may be installed on a ship.", opad, h, "one Hall of Triumph hullmod");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Math.round((VENT_RATE_MULT - 1f) * 100f) + "%";
        if (index == 1) return "Resistant Flux Conduits";
        return null;
    }
}
