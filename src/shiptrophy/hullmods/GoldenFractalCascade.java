package shiptrophy.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.ShardSpawner;
import com.fs.starfarer.api.impl.hullmods.ShardSpawner.ShardFadeInPlugin;

/**
 * Reverses the ordinary Omega fracture hierarchy for Gan Eden's guardians:
 * shards become facets, facets become tesseracts, and tesseracts are terminal.
 */
public final class GoldenFractalCascade extends ShardSpawner {
    public static final String HULLMOD_ID = "ship_trophy_golden_fractal";

    private static final Color GOLD_TINT = new Color(255, 194, 74);
    private static final String DATA_PREFIX =
            "ship_trophy_golden_fractal_spawned_";
    private static final String FACET_VARIANT =
            "ship_trophy_golden_facet_Attack";
    private static final String TESSERACT_VARIANT =
            "ship_trophy_golden_tesseract_Attack";
    private static final String ASPECT_WING =
            "ship_trophy_golden_aspect_wing";

    private static final int LARGE_CHILDREN = 2;
    private static final int ASPECT_WINGS = 6;

    @Override
    public void applyEffectsBeforeShipCreation(
            HullSize hullSize,
            MutableShipStatsAPI stats,
            String id) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id);
        if (stats == null
                || stats.getVariant() == null
                || stats.getVariant().getHullSpec() == null) {
            return;
        }

        String baseHullId = stats.getVariant().getHullSpec().getBaseHullId();
        if ("facet".equals(baseHullId)) {
            stats.getHullBonus().modifyMult(
                    id, 0.67f, "Aureate fracture instability");
        } else if ("tesseract".equals(baseHullId)) {
            stats.getHullBonus().modifyMult(
                    id, 0.5f, "Aureate fracture instability");
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship == null) return;

        applyGoldenAppearance(ship);

        String baseHullId = ship.getHullSpec().getBaseHullId();
        boolean shard = "shard_left".equals(baseHullId)
                || "shard_right".equals(baseHullId);
        boolean facet = "facet".equals(baseHullId);
        if (!shard && !facet) return;

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) return;
        if (ship.getOriginalOwner() != 0) {
            engine.setCombatNotOverForAtLeast(SPAWN_TIME + 1f);
        }
        if (!ship.isHulk() || !engine.isEntityInPlay(ship)) return;

        String key = DATA_PREFIX + ship.getId();
        if (Boolean.TRUE.equals(engine.getCustomData().get(key))) return;
        engine.getCustomData().put(key, Boolean.TRUE);

        // Keep the wreck stable while the stock Omega materialization effect
        // fades it out and fades its descendants in.
        ship.setHitpoints(ship.getMaxHitpoints());
        ship.getMutableStats().getHullDamageTakenMult()
                .modifyMult(HULLMOD_ID, 0f);

        String largeVariant = shard ? FACET_VARIANT : TESSERACT_VARIANT;
        List<ShardFadeInPlugin> spawners =
                new ArrayList<ShardFadeInPlugin>(
                        LARGE_CHILDREN + ASPECT_WINGS);

        float facing = ship.getFacing();
        addSpawner(engine, spawners, largeVariant, ship, facing);
        addSpawner(engine, spawners, largeVariant, ship, facing + 180f);

        // Fill the six gaps between the two large descendants. One stock
        // Aspect wing contains three craft.
        float[] wingAngles = new float[] {
                45f, 90f, 135f, 225f, 270f, 315f
        };
        for (float wingAngle : wingAngles) {
            addSpawner(
                    engine,
                    spawners,
                    ASPECT_WING,
                    ship,
                    facing + wingAngle);
        }

        engine.addPlugin(createShipFadeOutPlugin(
                ship, SPAWN_TIME, spawners));
    }

    private static void addSpawner(
            CombatEngineAPI engine,
            List<ShardFadeInPlugin> spawners,
            String variantId,
            ShipAPI source,
            float angle) {
        ShardFadeInPlugin plugin = new ShardFadeInPlugin(
                variantId, source, 0f, SPAWN_TIME, normalizeAngle(angle));
        spawners.add(plugin);
        engine.addPlugin(plugin);
    }

    private static void applyGoldenAppearance(ShipAPI ship) {
        if (ship.getSpriteAPI() != null) {
            ship.getSpriteAPI().setColor(GOLD_TINT);
        }
        ship.setVentCoreColor(new Color(255, 226, 120));
        ship.setVentFringeColor(new Color(255, 156, 36));
        ship.setExplosionFlashColorOverride(new Color(255, 190, 55));
        ship.setOverloadColor(new Color(255, 188, 62));
    }

    private static float normalizeAngle(float angle) {
        float normalized = angle % 360f;
        return normalized < 0f ? normalized + 360f : normalized;
    }
}
