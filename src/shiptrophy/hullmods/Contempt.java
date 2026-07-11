package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;

import shiptrophy.TrophyNetwork;

public class Contempt extends BaseUniqueTrophyHullMod {
    public static final String HULLMOD_ID = "ship_trophy_contempt";
    public static final String REQUIRED_BASE_HULL_ID = "onslaught_mk1";
    public static final String DISCOUNT_PREFIX = "ship_trophy_contempt_op_discount_";
    public static final int OP_DISCOUNT_PER_WEAPON = 1;

    @Override
    protected String getHullModId() {
        return HULLMOD_ID;
    }

    @Override
    protected String getRequiredShowcaseName() {
        return "Onslaught Mk.I";
    }

    @Override
    protected boolean isUnlocked() {
        return TrophyNetwork.isOnslaughtMkIShowcased();
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
        return TrophyOpDiscounts.getWeaponTagDiscount(variant, OP_DISCOUNT_PER_WEAPON,
                "dweller", "threat", "fragment");
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + OP_DISCOUNT_PER_WEAPON;
        return null;
    }
}
