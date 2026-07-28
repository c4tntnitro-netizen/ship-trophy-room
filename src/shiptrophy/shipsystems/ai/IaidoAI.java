package shiptrophy.shipsystems.ai;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import shiptrophy.hullmods.IronShellDiscipline;

public class IaidoAI implements ShipSystemAIScript {
    private final IntervalUtil decisionInterval = new IntervalUtil(0.15f, 0.30f);

    private ShipAPI ship;
    private ShipSystemAPI system;
    private ShipwideAIFlags flags;
    private CombatEngineAPI engine;

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
        this.flags = flags;
        this.engine = engine;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (ship == null || system == null || engine == null || engine.isPaused() || amount <= 0f) return;
        decisionInterval.advance(amount);
        if (!decisionInterval.intervalElapsed() || system.isActive() || !system.canBeActivated()) return;

        if (ship.isDirectRetreat()) {
            activate();
            return;
        }

        if (target == null) target = ship.getShipTarget();
        if (target == null || !target.isAlive() || target.isAlly()
                || target.isFighter() || target.isDrone() || target.isStationModule()) {
            return;
        }
        if (ship.getShipTarget() == null) ship.setShipTarget(target);

        if (hasReadyTorpedoSolution(target)) {
            activate();
            return;
        }

        float distanceSquared = distanceSquared(ship.getLocation(), target.getLocation());
        float facingError = Math.abs(shortestRotation(ship.getFacing(), angleTo(ship.getLocation(), target.getLocation())));
        if (distanceSquared <= 650f * 650f && facingError <= 25f) {
            activate();
        }
    }

    private boolean hasReadyTorpedoSolution(ShipAPI target) {
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (!IronShellDiscipline.isTorpedoWeapon(weapon)
                    || weapon.isDisabled()
                    || weapon.isPermanentlyDisabled()
                    || weapon.getCooldownRemaining() > 0.15f
                    || (weapon.usesAmmo() && weapon.getAmmo() <= 0)) {
                continue;
            }

            float range = Math.max(1f, weapon.getRange()) * 1.05f;
            if (distanceSquared(weapon.getLocation(), target.getLocation()) > range * range) continue;
            if (weapon.distanceFromArc(target.getLocation()) > 5f) continue;
            return true;
        }
        return false;
    }

    private void activate() {
        ship.useSystem();
        if (flags != null) flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 3.5f);
    }

    private static float distanceSquared(Vector2f a, Vector2f b) {
        float dx = b.x - a.x;
        float dy = b.y - a.y;
        return dx * dx + dy * dy;
    }

    private static float angleTo(Vector2f from, Vector2f to) {
        return (float) Math.toDegrees(Math.atan2(to.y - from.y, to.x - from.x));
    }

    private static float shortestRotation(float from, float to) {
        float result = (to - from) % 360f;
        if (result > 180f) result -= 360f;
        if (result < -180f) result += 360f;
        return result;
    }
}
