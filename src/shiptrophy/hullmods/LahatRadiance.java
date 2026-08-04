package shiptrophy.hullmods;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;

/** A pale aureate shroud around Lahat Haharev. */
public final class LahatRadiance extends BaseHullMod {
    private static final String TIMER_KEY =
            "$shipTrophyLahatRadianceTimer";
    private static final Color OUTER_GLOW =
            new Color(255, 174, 70, 72);
    private static final Color INNER_GLOW =
            new Color(255, 248, 224, 58);
    private static final Color ORANGE_WISP =
            new Color(255, 159, 50, 58);
    private static final Color WHITE_WISP =
            new Color(255, 244, 218, 46);
    private static final float PARTICLE_INTERVAL = 0.14f;

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null || !ship.isAlive()) return;

        ship.setJitterUnder(
                this, OUTER_GLOW, 0.65f, 12, 8f, 28f);
        ship.setJitter(
                this, INNER_GLOW, 0.28f, 4, 0f, 8f);

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.isPaused()) return;

        Object stored = ship.getCustomData().get(TIMER_KEY);
        float timer = stored instanceof Float ? (Float) stored : 0f;
        timer += amount;
        while (timer >= PARTICLE_INTERVAL) {
            timer -= PARTICLE_INTERVAL;
            addShroudWisp(engine, ship, false);
            if (Math.random() < 0.45d) {
                addShroudWisp(engine, ship, true);
            }
        }
        ship.setCustomData(TIMER_KEY, Float.valueOf(timer));
    }

    private static void addShroudWisp(
            CombatEngineAPI engine, ShipAPI ship, boolean pale) {
        double angle = Math.random() * Math.PI * 2d;
        float radius = ship.getCollisionRadius()
                * (0.25f + (float) Math.random() * 0.8f);
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        Vector2f location = new Vector2f(ship.getLocation());
        location.x += cos * radius;
        location.y += sin * radius;

        Vector2f velocity = new Vector2f(ship.getVelocity());
        velocity.x += cos * 12f;
        velocity.y += sin * 12f;

        float size = ship.getCollisionRadius()
                * (0.45f + (float) Math.random() * 0.35f);
        Color color = pale ? WHITE_WISP : ORANGE_WISP;
        engine.addNebulaParticle(
                location,
                velocity,
                size,
                1.55f,
                0.2f,
                0.35f,
                0.8f,
                color);
    }
}
