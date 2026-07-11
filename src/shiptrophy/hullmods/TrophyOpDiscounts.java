package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

public class TrophyOpDiscounts {
    public static final int DISCOUNT_CAP = 63;
    private static final int[] DISCOUNT_BITS = new int[] {32, 16, 8, 4, 2, 1};

    public static int getWeaponTagDiscount(ShipVariantAPI variant, int discountPerWeapon, String... tags) {
        if (variant == null || discountPerWeapon <= 0) return 0;

        int discount = 0;
        for (String slotId : variant.getNonBuiltInWeaponSlots()) {
            WeaponSpecAPI spec = variant.getWeaponSpec(slotId);
            if (hasAnyTag(spec, tags)) {
                discount += discountPerWeapon;
            }
        }
        return clamp(discount);
    }

    public static void setDiscount(ShipVariantAPI variant, String prefix, int discount) {
        if (variant == null) return;

        clearDiscount(variant, prefix);
        int remaining = clamp(discount);
        for (int bit : DISCOUNT_BITS) {
            if (remaining >= bit) {
                variant.addMod(prefix + bit);
                remaining -= bit;
            }
        }
    }

    public static void clearDiscount(ShipVariantAPI variant, String prefix) {
        if (variant == null) return;
        for (int bit : DISCOUNT_BITS) {
            String id = prefix + bit;
            if (variant.hasHullMod(id)) {
                variant.removeMod(id);
                variant.removePermaMod(id);
            }
        }
    }

    public static boolean isDiscountHullMod(String hullModId, String prefix) {
        if (hullModId == null || prefix == null || !hullModId.startsWith(prefix)) return false;
        for (int bit : DISCOUNT_BITS) {
            if ((prefix + bit).equals(hullModId)) return true;
        }
        return false;
    }

    private static boolean hasAnyTag(WeaponSpecAPI spec, String... tags) {
        if (spec == null || tags == null) return false;
        for (String tag : tags) {
            if (tag != null && spec.hasTag(tag)) return true;
        }
        return false;
    }

    private static int clamp(int discount) {
        if (discount < 0) return 0;
        if (discount > DISCOUNT_CAP) return DISCOUNT_CAP;
        return discount;
    }
}
