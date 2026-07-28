package shiptrophy.shipsystems;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class IaidoStats extends BaseShipSystemScript {
    public static final float DAMAGE_TAKEN_MULT = 0.50f;
    public static final float BALLISTIC_ENERGY_ROF_MULT = 0.50f;
    public static final float MASS_MULT = 1.20f;

    private static final Color JITTER_COLOR = new Color(255, 155, 65, 110);

    private Float originalMass;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats == null || !(stats.getEntity() instanceof ShipAPI)) return;
        ShipAPI ship = (ShipAPI) stats.getEntity();

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine != null && engine.isPaused()) return;

        float speedBoost = getSpeedBoost(ship.getHullSize());
        stats.getMaxSpeed().modifyFlat(id, speedBoost * effectLevel);
        stats.getAcceleration().modifyFlat(id, speedBoost * 4f * effectLevel);
        stats.getDeceleration().modifyFlat(id, speedBoost * 2f * effectLevel);

        float damageMult = 1f - (1f - DAMAGE_TAKEN_MULT) * effectLevel;
        stats.getEmpDamageTakenMult().modifyMult(id, damageMult);
        stats.getArmorDamageTakenMult().modifyMult(id, damageMult);
        stats.getHullDamageTakenMult().modifyMult(id, damageMult);

        float rofMult = 1f - (1f - BALLISTIC_ENERGY_ROF_MULT) * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id, rofMult);
        stats.getEnergyRoFMult().modifyMult(id, rofMult);

        if (originalMass == null) originalMass = ship.getMass();
        if (effectLevel > 0f && originalMass != null) {
            ship.setMass(originalMass.floatValue() * MASS_MULT);
        }

        steerTowardTarget(ship, effectLevel);
        ship.setJitterUnder(this, JITTER_COLOR, effectLevel, 10, 0f, 9f);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats == null) return;
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getEmpDamageTakenMult().unmodify(id);
        stats.getArmorDamageTakenMult().unmodify(id);
        stats.getHullDamageTakenMult().unmodify(id);
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);

        if (stats.getEntity() instanceof ShipAPI && originalMass != null) {
            ((ShipAPI) stats.getEntity()).setMass(originalMass.floatValue());
        }
        originalMass = null;
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) return new StatusData("iaido drive engaged", false);
        if (index == 1) return new StatusData("damage taken reduced by 50%", false);
        if (index == 2) return new StatusData("ballistic and energy rate of fire reduced by 50%", true);
        if (index == 3 && (state == State.IN || state == State.ACTIVE)) {
            return new StatusData("torpedo catapult armed", false);
        }
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system == null || system.isOutOfAmmo()) return null;
        if (system.getState() == ShipSystemAPI.SystemState.IDLE) return "READY";
        return null;
    }

    private static float getSpeedBoost(ShipAPI.HullSize hullSize) {
        if (hullSize == ShipAPI.HullSize.FRIGATE) return 250f;
        if (hullSize == ShipAPI.HullSize.DESTROYER) return 275f;
        return 300f;
    }

    private static void steerTowardTarget(ShipAPI ship, float effectLevel) {
        if (ship == null || effectLevel <= 0f || ship.getSystem() == null || !ship.getSystem().isActive()) return;
        ShipAPI target = ship.getShipTarget();
        if (target == null || !target.isAlive() || target.isAlly()) return;

        Vector2f from = ship.getLocation();
        Vector2f to = target.getLocation();
        float desired = (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
        float delta = shortestRotation(ship.getFacing(), desired);
        float maxTurn = Math.max(15f, ship.getMaxTurnRate() * 2f);
        ship.setAngularVelocity(clamp(delta * 5f, -maxTurn, maxTurn));
    }

    private static float shortestRotation(float from, float to) {
        float result = (to - from) % 360f;
        if (result > 180f) result -= 360f;
        if (result < -180f) result += 360f;
        return result;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
