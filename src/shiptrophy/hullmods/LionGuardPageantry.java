package shiptrophy.hullmods;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;

import shiptrophy.TrophyDoctrine;

public class LionGuardPageantry extends BaseTrophyDoctrineHullMod {
    public static final float PULSE_RANGE_BONUS = 100f;
    public static final float CASUALTY_PENALTY = 25f;
    public static final String ENERGY_BOLT_COHERER = "coherer";
    public static final String MODULAR_BOLT_COHERER = "vice_modular_bolt_coherer";
    public static final String DMOD_MARKER = "ship_trophy_lg_pageantry_dmod_marker";

    @Override
    protected TrophyDoctrine getDoctrine() {
        return TrophyDoctrine.LG;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return super.isApplicableToShip(ship) && !hasBoltCoherer(ship);
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (hasBoltCoherer(ship)) return "Incompatible with Energy Bolt Coherer";
        return super.getUnapplicableReason(ship);
    }

    @Override
    protected void applyDoctrineEffects(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        if (stats.getVariant() != null
                && (stats.getVariant().hasHullMod(ENERGY_BOLT_COHERER)
                || stats.getVariant().hasHullMod(MODULAR_BOLT_COHERER))) {
            removeHiddenMarker(stats, DMOD_MARKER);
            return;
        }
        addHiddenMarker(stats, DMOD_MARKER);
        stats.getEnergyWeaponRangeBonus().modifyFlat(id, PULSE_RANGE_BONUS);
        stats.getBeamWeaponRangeBonus().modifyFlat(id, -PULSE_RANGE_BONUS);
        stats.getCrewLossMult().modifyPercent(id, CASUALTY_PENALTY);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (!isUnlocked()) return;
        if (hasBoltCoherer(ship)) return;
        ship.addListener(new NonEnergyBeamRangeFix());
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + Math.round(PULSE_RANGE_BONUS);
        if (index == 1) return "" + Math.round(CASUALTY_PENALTY) + "%";
        return null;
    }

    @Override
    protected String getDModCalculationNote() {
        return "This trophy refit is not a normal D-mod, but counts as a D-mod for calculations such as Derelict Operations.";
    }

    private boolean hasBoltCoherer(ShipAPI ship) {
        if (ship == null || ship.getVariant() == null) return false;
        return ship.getVariant().hasHullMod(ENERGY_BOLT_COHERER)
                || ship.getVariant().hasHullMod(MODULAR_BOLT_COHERER);
    }

    public static class NonEnergyBeamRangeFix implements WeaponBaseRangeModifier {
        @Override
        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon) {
            return 0f;
        }

        @Override
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon) {
            return 1f;
        }

        @Override
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon) {
            if (weapon == null || weapon.getSpec() == null) return 0f;
            if (weapon instanceof BeamAPI && weapon.getSpec().getType() != WeaponAPI.WeaponType.ENERGY) {
                return PULSE_RANGE_BONUS;
            }
            return 0f;
        }
    }
}
