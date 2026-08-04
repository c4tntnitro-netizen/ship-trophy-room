package shiptrophy.campaign;

import java.awt.Color;
import java.util.ArrayList;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.NascentGravityWellAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

/** Creates the solitary power-transit Gate near the Sector's northeast edge. */
public final class GanEdenTransitSystemGenerator {
    public static final String SYSTEM_ID =
            "ship_trophy_gan_eden_power_transit_system";
    public static final String SYSTEM_NAME =
            "POWER TRANSIT GATE - GAN EDEN";
    public static final String JUMP_ID =
            "ship_trophy_gan_eden_power_transit_jump";
    public static final String GRAVITY_WELL_ID =
            "ship_trophy_gan_eden_power_transit_well";

    // Core hyperspace is about 164k by 104k. This leaves the system barely
    // inside its northeast edge and far outside the ordinary constellation
    // field, while remaining a valid Transverse Jump destination.
    private static final float HYPERSPACE_X = 70000f;
    private static final float HYPERSPACE_Y = 42000f;
    private static final String JUMP_GENERATED_KEY =
            "$shipTrophyGanEdenPowerTransitJumpGenerated";
    private static final String ACCESS_VERSION_KEY =
            "$shipTrophyGanEdenPowerTransitAccessVersion";
    private static final int ACCESS_VERSION = 2;
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
        }

        // Apply placement and access policy on every load so old saves move
        // cleanly from the Abyss and lose their obsolete jump point.
        system.setName(SYSTEM_NAME);
        system.setBaseName(SYSTEM_NAME);
        system.getLocation().set(HYPERSPACE_X, HYPERSPACE_Y);
        system.setLightColor(new Color(120, 130, 145, 255));
        system.setMaxRadiusInHyperspace(350f);
        system.setDoNotShowIntelFromThisLocationOnMap(false);
        system.setProcgen(false);
        system.addTag(Tags.THEME_SPECIAL);
        system.addTag(Tags.THEME_UNSAFE);
        system.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
        system.removeTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
        system.removeTag(Tags.THEME_HIDDEN);

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
        ensureTransverseOnlyAccess(system);
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

    /**
     * Removes all ordinary entrances, then rebuilds only the hidden anchor
     * Transverse Jump uses to enter a system without a stable jump point.
     */
    private static void ensureTransverseOnlyAccess(StarSystemAPI system) {
        boolean rebuildAnchor = system.getMemoryWithoutUpdate().getInt(
                ACCESS_VERSION_KEY) < ACCESS_VERSION;
        SectorEntityToken existing = system.getEntityById(JUMP_ID);
        if (existing != null) {
            system.removeEntity(existing);
            rebuildAnchor = true;
        }

        if (system.getAutogeneratedJumpPointsInHyper() != null
                && !system.getAutogeneratedJumpPointsInHyper().isEmpty()) {
            for (JumpPointAPI jump : new ArrayList<JumpPointAPI>(
                    system.getAutogeneratedJumpPointsInHyper())) {
                if (jump != null && jump.getContainingLocation() != null) {
                    jump.getContainingLocation().removeEntity(jump);
                }
            }
            system.getAutogeneratedJumpPointsInHyper().clear();
            rebuildAnchor = true;
        }
        system.getMemoryWithoutUpdate().unset(JUMP_GENERATED_KEY);

        SectorEntityToken anchor = system.getHyperspaceAnchor();
        if (anchor == null) {
            rebuildAnchor = true;
        }
        if (rebuildAnchor) {
            // Recreate the anchor at the system's new coordinates. This does
            // not create a stable jump point, but permits Transverse Jump.
            if (anchor != null && anchor.getContainingLocation() != null) {
                anchor.getContainingLocation().removeEntity(anchor);
            }
            system.setHyperspaceAnchor(null);
            system.generateAnchorIfNeeded();
            system.getMemoryWithoutUpdate().set(
                    ACCESS_VERSION_KEY, ACCESS_VERSION);
        }

        ensureNascentGravityWell(system, rebuildAnchor);
    }

    /** Adds the native purple haze used by Transverse-Jump-only systems. */
    private static void ensureNascentGravityWell(
            StarSystemAPI system, boolean realign) {
        SectorEntityToken gate = system.getEntityById(
                GanEdenQuestManager.EXTERNAL_RING_ID);
        if (gate == null) return;

        SectorEntityToken existing = Global.getSector().getEntityById(
                GRAVITY_WELL_ID);
        NascentGravityWellAPI well = existing instanceof NascentGravityWellAPI
                ? (NascentGravityWellAPI) existing
                : null;
        if (existing != null && (well == null || well.getTarget() != gate)) {
            if (existing.getContainingLocation() != null) {
                existing.getContainingLocation().removeEntity(existing);
            }
            well = null;
        }

        if (well == null) {
            well = Global.getSector().createNascentGravityWell(gate, 50f);
            well.setId(GRAVITY_WELL_ID);
            Global.getSector().getHyperspace().addEntity(well);
            realign = true;
        }
        if (realign) {
            well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(
                    gate, 0f);
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
