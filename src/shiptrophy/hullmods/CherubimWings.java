package shiptrophy.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;

/** Paired, swept white-gold drive plumes unique to Cherubim. */
public final class CherubimWings extends BaseHullMod {
    private static final String TIMER_KEY =
            "$shipTrophyCherubimWingTimer";
    private static final String PULSE_KEY =
            "$shipTrophyCherubimWingPulse";
    private static final Color GOLD_FEATHER =
            new Color(255, 166, 56, 118);
    private static final Color PALE_FEATHER =
            new Color(255, 239, 190, 150);
    private static final Color WHITE_CORE =
            new Color(255, 253, 238, 185);
    private static final float PARTICLE_INTERVAL = 0.055f;
    private static final int FEATHERS_PER_SIDE = 4;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || !ship.isAlive()) return;

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) return;

        ShipEngineControllerAPI controller = ship.getEngineController();
        if (controller == null
                || controller.isFlamedOut()
                || controller.isDisabled()) {
            return;
        }

        float intensity = driveIntensity(controller);
        Object stored = ship.getCustomData().get(TIMER_KEY);
        float timer = stored instanceof Float ? (Float) stored : 0f;
        timer += amount;

        Object pulseStored = ship.getCustomData().get(PULSE_KEY);
        int pulse = pulseStored instanceof Integer
                ? (Integer) pulseStored : 0;
        while (timer >= PARTICLE_INTERVAL) {
            timer -= PARTICLE_INTERVAL;
            pulse++;
            addWing(engine, ship, -1f, intensity, pulse);
            addWing(engine, ship, 1f, intensity, pulse);
        }
        ship.setCustomData(TIMER_KEY, Float.valueOf(timer));
        ship.setCustomData(PULSE_KEY, Integer.valueOf(pulse));
    }

    private static float driveIntensity(
            ShipEngineControllerAPI controller) {
        if (controller.isAccelerating()) return 1f;
        if (controller.isAcceleratingBackwards()
                || controller.isDecelerating()) {
            return 0.72f;
        }
        if (controller.isTurningLeft()
                || controller.isTurningRight()
                || controller.isStrafingLeft()
                || controller.isStrafingRight()) {
            return 0.82f;
        }
        return 0.42f;
    }

    private static void addWing(
            CombatEngineAPI engine,
            ShipAPI ship,
            float sideSign,
            float intensity,
            int pulse) {
        Vector2f forward = unit(ship.getFacing());
        Vector2f side = unit(ship.getFacing() + 90f);

        for (int i = 0; i < FEATHERS_PER_SIDE; i++) {
            float span = 18f + i * 9f;
            float aft = 1f + i * 6f;

            Vector2f origin = new Vector2f(ship.getLocation());
            addScaled(origin, forward, -aft);
            addScaled(origin, side, sideSign * span);

            Vector2f velocity = new Vector2f(ship.getVelocity());
            float trailSpeed = (45f + i * 17f)
                    * (0.55f + intensity * 0.65f);
            addScaled(velocity, forward, -trailSpeed);
            addScaled(velocity, side,
                    sideSign * (10f + i * 8f) * intensity);

            float size = 19f + i * 2.5f + intensity * 5f;
            float lifetime = 0.28f + i * 0.055f
                    + intensity * 0.08f;
            Color feather = (i & 1) == 0
                    ? PALE_FEATHER : GOLD_FEATHER;
            engine.addSmoothParticle(
                    origin, velocity, size, 1f, lifetime, feather);

            // Alternating narrow cores keep each plume legible as a
            // separate feather rather than one undifferentiated cloud.
            if (((pulse + i) & 1) == 0) {
                engine.addSmoothParticle(
                        origin,
                        velocity,
                        size * 0.38f,
                        1f,
                        lifetime * 0.72f,
                        WHITE_CORE);
            }
        }
    }

    private static Vector2f unit(float angleDegrees) {
        double radians = Math.toRadians(angleDegrees);
        return new Vector2f(
                (float) Math.cos(radians),
                (float) Math.sin(radians));
    }

    private static void addScaled(
            Vector2f target, Vector2f vector, float scale) {
        target.x += vector.x * scale;
        target.y += vector.y * scale;
    }
}
