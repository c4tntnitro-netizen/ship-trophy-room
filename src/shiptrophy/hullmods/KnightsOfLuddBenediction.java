package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class KnightsOfLuddBenediction extends BaseTrophyDoctrineHullMod {
    public static final String SUBTYPE_ID = "knights_of_ludd";
    public static final float ENERGY_DAMAGE_BONUS = 5f;
    public static final float SHIELD_DAMAGE_REDUCTION = 5f;

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_DAMAGE_BONUS);
        stats.getShieldDamageTakenMult().modifyMult(id, 1f - SHIELD_DAMAGE_REDUCTION * 0.01f);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(ENERGY_DAMAGE_BONUS) + "%";
        if (index == 1) return "" + Math.round(SHIELD_DAMAGE_REDUCTION) + "%";
        return null;
    }
}
