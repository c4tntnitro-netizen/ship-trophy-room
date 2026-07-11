package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import shiptrophy.TrophyNetwork;

public class Gaze extends BaseUniqueTrophyHullMod {
    public static final String HULLMOD_ID = "ship_trophy_gaze";
    public static final String REQUIRED_BASE_HULL_ID = "ziggurat";
    public static final String DISCOUNT_PREFIX = "ship_trophy_gaze_op_discount_";
    public static final int OP_DISCOUNT_PER_WEAPON = 2;

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
    protected void syncDiscountForVariant(ShipVariantAPI variant, boolean unlocked) {
        syncVariant(variant, unlocked);
    }

    public static void syncVariant(ShipVariantAPI variant, TrophyNetwork.CollectionStats stats) {
        syncVariant(variant, TrophyNetwork.hasShowcasedHull(stats, REQUIRED_BASE_HULL_ID));
    }

    public static void syncVariant(ShipVariantAPI variant, boolean unlocked) {
        if (variant == null) return;
        boolean active = unlocked && variant.hasHullMod(HULLMOD_ID);
        int discount = active ? getDiscount(variant) : 0;
        TrophyOpDiscounts.setDiscount(variant, DISCOUNT_PREFIX, discount);
    }

    @Override
    protected int getCurrentDiscount(ShipVariantAPI variant) {
        return getDiscount(variant);
    }

    public static int getDiscount(ShipVariantAPI variant) {
        return TrophyOpDiscounts.getWeaponTagDiscount(variant, OP_DISCOUNT_PER_WEAPON, "omega");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + OP_DISCOUNT_PER_WEAPON;
        return null;
    }
}
