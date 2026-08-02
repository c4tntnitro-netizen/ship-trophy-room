package shiptrophy.campaign;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

/** Creates the solitary power-transit Gate on the upper rim of the Abyss. */
public final class GanEdenTransitSystemGenerator {
    public static final String SYSTEM_ID =
            "ship_trophy_gan_eden_power_transit_system";
    public static final String SYSTEM_NAME =
            "POWER TRANSIT GATE - GAN EDEN";
    public static final String JUMP_ID =
            "ship_trophy_gan_eden_power_transit_jump";

    // The Orion-Perseus Abyss label is centered near (-65000, -47000).
    // This sits on its upper-right/northeastern boundary while remaining well
    // outside the ordinary constellation field.
    private static final float HYPERSPACE_X = -44000f;
    private static final float HYPERSPACE_Y = -30000f;
    private static final String JUMP_GENERATED_KEY =
            "$shipTrophyGanEdenPowerTransitJumpGenerated";
    private static final String DAMAGED_HYPERSHUNT_TYPE =
            "ship_trophy_damaged_coronal_hypershunt";
    private static final String DAMAGED_GATE_HAULER_TYPE =
            "ship_trophy_damaged_gate_hauler";

    // angle, distance from the Gate, facing. Fixed rather than randomized so
    // old saves are repaired idempotently and every visit shows the same ruin
    // field. The innermost 1,300 units stay clear for Gate navigation.
    private static final float[][] DAMAGED_HYPERSHUNTS = {
        {18f, 1900f, 221f},
        {71f, 3650f, 14f},
        {127f, 5600f, 287f},
        {181f, 3100f, 103f},
        {226f, 6500f, 339f},
        {278f, 4300f, 166f},
        {329f, 7200f, 48f},
    };
    private static final float[][] DAMAGED_GATE_HAULERS = {
        {4f, 5200f, 82f},
        {36f, 2850f, 257f},
        {59f, 7000f, 131f},
        {94f, 2250f, 310f},
        {116f, 4100f, 27f},
        {151f, 7550f, 196f},
        {167f, 4750f, 349f},
        {204f, 2050f, 123f},
        {244f, 3600f, 271f},
        {263f, 6900f, 39f},
        {301f, 2550f, 184f},
        {346f, 4050f, 306f},
    };

    private GanEdenTransitSystemGenerator() {
    }

    public static SectorEntityToken ensureGenerated() {
        if (Global.getSector() == null) return null;

        StarSystemAPI system = findSystem();
        if (system == null) {
            system = Global.getSector().createStarSystem(SYSTEM_NAME);
            if (system == null) return null;
            system.setName(SYSTEM_NAME);
            system.setBaseName(SYSTEM_NAME);
            system.getLocation().set(HYPERSPACE_X, HYPERSPACE_Y);
            system.setLightColor(new Color(120, 130, 145, 255));
            system.setMaxRadiusInHyperspace(350f);
            system.addTag(Tags.THEME_SPECIAL);
            system.addTag(Tags.THEME_UNSAFE);
            system.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
            system.initNonStarCenter().setFixedLocation(0f, 0f);
        }

        SectorEntityToken center = system.getCenter();
        if (center == null) {
            center = system.initNonStarCenter();
            center.setFixedLocation(0f, 0f);
        }

        SectorEntityToken gate = system.getEntityById(
                GanEdenQuestManager.EXTERNAL_RING_ID);
        if (gate == null) {
            gate = system.addCustomEntity(
                    GanEdenQuestManager.EXTERNAL_RING_ID,
                    SYSTEM_NAME,
                    GanEdenQuestManager.EXTERNAL_RING_TYPE,
                    Factions.NEUTRAL);
        }
        if (gate != null) {
            gate.setName(SYSTEM_NAME);
            gate.setFixedLocation(0f, 0f);
            gate.setDiscoverable(null);
            gate.setSensorProfile(null);
            gate.addTag(Tags.GATE);
            gate.addTag(Tags.STORY_CRITICAL);
            gate.addTag(Tags.HAS_INTERACTION_DIALOG);
            gate.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
            gate.removeTag(Tags.NON_CLICKABLE);
            gate.removeTag(Tags.NO_ENTITY_TOOLTIP);
        }

        ensureMegastructureGraveyard(system);
        ensureJumpPoint(system);
        return gate;
    }

    private static void ensureMegastructureGraveyard(
            StarSystemAPI system) {
        ensureRuins(system, "ship_trophy_damaged_hypershunt_",
                "Damaged Coronal Hypershunt", DAMAGED_HYPERSHUNT_TYPE,
                DAMAGED_HYPERSHUNTS);
        ensureRuins(system, "ship_trophy_damaged_gate_hauler_",
                "Damaged Gate Hauler", DAMAGED_GATE_HAULER_TYPE,
                DAMAGED_GATE_HAULERS);
    }

    private static void ensureRuins(StarSystemAPI system, String idPrefix,
            String name, String type, float[][] placements) {
        for (int i = 0; i < placements.length; i++) {
            String id = idPrefix + (i + 1);
            SectorEntityToken ruin = system.getEntityById(id);
            if (ruin == null) {
                ruin = system.addCustomEntity(
                        id, name, type, Factions.NEUTRAL);
            }
            if (ruin == null) continue;

            float angle = placements[i][0];
            float distance = placements[i][1];
            float radians = (float) Math.toRadians(angle);
            ruin.setFixedLocation(
                    (float) Math.cos(radians) * distance,
                    (float) Math.sin(radians) * distance);
            ruin.setFacing(placements[i][2]);
            ruin.setDiscoverable(null);
            ruin.setSensorProfile(null);
            ruin.addTag(Tags.NON_CLICKABLE);
            ruin.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
            ruin.removeTag(Tags.HAS_INTERACTION_DIALOG);
        }
    }

    private static void ensureJumpPoint(StarSystemAPI system) {
        JumpPointAPI jump = null;
        SectorEntityToken existing = system.getEntityById(JUMP_ID);
        if (existing instanceof JumpPointAPI) {
            jump = (JumpPointAPI) existing;
        }
        if (jump == null) {
            jump = Global.getFactory().createJumpPoint(
                    JUMP_ID, "Power Transit Anchorage");
            jump.setFixedLocation(0f, -2600f);
            jump.setStandardWormholeToHyperspaceVisual();
            jump.setDiscoverable(null);
            jump.setSensorProfile(null);
            system.addEntity(jump);
        }
        if (!system.getMemoryWithoutUpdate().getBoolean(
                JUMP_GENERATED_KEY)) {
            system.autogenerateHyperspaceJumpPoints(false, false);
            system.getMemoryWithoutUpdate().set(
                    JUMP_GENERATED_KEY, true);
        }
    }

    public static StarSystemAPI findSystem() {
        if (Global.getSector() == null) return null;
        StarSystemAPI system = Global.getSector().getStarSystem(SYSTEM_ID);
        if (system != null) return system;
        system = Global.getSector().getStarSystem(SYSTEM_NAME);
        if (system != null) return system;
        for (StarSystemAPI candidate : Global.getSector().getStarSystems()) {
            if (SYSTEM_ID.equalsIgnoreCase(candidate.getId())
                    || SYSTEM_NAME.equalsIgnoreCase(candidate.getName())
                    || SYSTEM_NAME.equalsIgnoreCase(candidate.getBaseName())) {
                return candidate;
            }
        }
        return null;
    }
}
