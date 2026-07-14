package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class IronShellParadeDrill extends BaseTrophyDoctrineHullMod {
    public static final String SUBTYPE_ID = "iron_shell";
    public static final float ARMOR_BONUS = 75f;
    public static final float BALLISTIC_RANGE_BONUS = 50f;

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorBonus().modifyFlat(id, ARMOR_BONUS);
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, BALLISTIC_RANGE_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(ARMOR_BONUS);
        if (index == 1) return "" + Math.round(BALLISTIC_RANGE_BONUS);
        return null;
    }
}
