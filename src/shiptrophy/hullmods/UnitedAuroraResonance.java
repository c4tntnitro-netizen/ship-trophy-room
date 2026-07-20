package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class UnitedAuroraResonance extends BaseTrophyDoctrineHullMod {
    public static final String SUBTYPE_ID = "uaf";
    public static final String ECCM_PACKAGE = "eccm";

    public static final float MISSILE_SPEED_BONUS = 25f;
    public static final float MISSILE_RANGE_MULT = 0.8f;
    public static final float MISSILE_ACCEL_BONUS = 150f;
    public static final float MISSILE_TURN_RATE_BONUS = 50f;
    public static final float MISSILE_TURN_ACCEL_BONUS = 150f;
    public static final float EW_PENALTY_MULT = 0.5f;
    public static final float ECCM_CHANCE = 0.5f;
    public static final float GUIDANCE_IMPROVEMENT = 1f;
    public static final float SMOD_ECCM_CHANCE = 1f;
    public static final float SMOD_EW_PENALTY_MULT = 0f;

    @Override
    protected String getSubtypeId() {
        return SUBTYPE_ID;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship) && !hasEccmPackage(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (hasEccmPackage(ship)) return "Incompatible with ECCM Package";
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getVariant() != null && stats.getVariant().hasHullMod(ECCM_PACKAGE)) return;

        boolean sMod = isSMod(stats);
        stats.getEccmChance().modifyFlat(id, sMod ? SMOD_ECCM_CHANCE : ECCM_CHANCE);
        stats.getMissileGuidance().modifyFlat(id, GUIDANCE_IMPROVEMENT);
        stats.getMissileMaxSpeedBonus().modifyPercent(id, MISSILE_SPEED_BONUS);
        stats.getMissileWeaponRangeBonus().modifyMult(id, MISSILE_RANGE_MULT);
        stats.getMissileAccelerationBonus().modifyPercent(id, MISSILE_ACCEL_BONUS);
        stats.getMissileMaxTurnRateBonus().modifyPercent(id, MISSILE_TURN_RATE_BONUS);
        stats.getMissileTurnAccelerationBonus().modifyPercent(id, MISSILE_TURN_ACCEL_BONUS);
        stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD)
                .modifyMult(id, sMod ? SMOD_EW_PENALTY_MULT : EW_PENALTY_MULT);
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return Math.round(ECCM_CHANCE * 100f) + "%";
        if (index == 1) return Math.round(MISSILE_SPEED_BONUS) + "%";
        if (index == 2) return Math.round(MISSILE_TURN_RATE_BONUS) + "%";
        if (index == 3) return Math.round((1f - EW_PENALTY_MULT) * 100f) + "%";
        return null;
    }

    private boolean hasEccmPackage(ShipAPI ship) {
        return ship != null && ship.getVariant() != null && ship.getVariant().hasHullMod(ECCM_PACKAGE);
    }
}
