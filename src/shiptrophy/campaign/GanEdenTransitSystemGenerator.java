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
            "Power Transit Gate - Gan Eden";
    public static final String JUMP_ID =
            "ship_trophy_gan_eden_power_transit_jump";
    public static final String GRAVITY_WELL_ID =
            "ship_trophy_gan_eden_power_transit_well";
    public static final String ARRIVAL_ANCHOR_ID =
            "ship_trophy_gan_eden_power_transit_arrival";

    // Preserve the authored vanilla placement as a fraction of the configured
    // sector dimensions. Nexerelin and other sector overhauls may replace the
    // 164k-by-104k map with a substantially different one.
    private static final float DEFAULT_SECTOR_WIDTH = 164000f;
    private static final float DEFAULT_SECTOR_HEIGHT = 104000f;
    private static final float HYPERSPACE_X_FRACTION = 70000f / 164000f;
    private static final float HYPERSPACE_Y_FRACTION = 42000f / 104000f;
    private static final String JUMP_GENERATED_KEY =
            "$shipTrophyGanEdenPowerTransitJumpGenerated";
    private static final String ACCESS_VERSION_KEY =
            "$shipTrophyGanEdenPowerTransitAccessVersion";
    private static final int ACCESS_VERSION = 5;
    // Put transverse arrivals beyond the entire megastructure graveyard and
    // on the far side of the Gate from the parked Ivory Ordo. At this bearing
    // the nearest authored ruin is still several thousand units away, while
    // the guardian has room to make a visible approach rather than opening an
    // interaction on top of the arriving fleet.
    private static final float ARRIVAL_ANGLE = 210f;
    private static final float ARRIVAL_DISTANCE = 9000f;
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

        // Migrate placement once per access-layout version. Reasserting an
        // absolute position on every load would fight sector-map overhauls or
        // a save-specific relocation performed by another mod.
        system.setName(SYSTEM_NAME);
        system.setBaseName(SYSTEM_NAME);
        if (system.getMemoryWithoutUpdate().getInt(
                ACCESS_VERSION_KEY) < ACCESS_VERSION) {
            system.getLocation().set(
                    getTargetHyperspaceX(), getTargetHyperspaceY());
        }
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

    static float getTargetHyperspaceX() {
        return configuredSectorDimension(
                "sectorWidth", DEFAULT_SECTOR_WIDTH)
                * HYPERSPACE_X_FRACTION;
    }

    static float getTargetHyperspaceY() {
        return configuredSectorDimension(
                "sectorHeight", DEFAULT_SECTOR_HEIGHT)
                * HYPERSPACE_Y_FRACTION;
    }

    static float getConfiguredSectorWidth() {
        return configuredSectorDimension(
                "sectorWidth", DEFAULT_SECTOR_WIDTH);
    }

    static float getConfiguredSectorHeight() {
        return configuredSectorDimension(
                "sectorHeight", DEFAULT_SECTOR_HEIGHT);
    }

    private static float configuredSectorDimension(
            String settingId, float fallback) {
        try {
            float value = Global.getSettings().getFloat(settingId);
            if (value > 0f) return value;
        } catch (RuntimeException ignored) {
            // An overhaul may omit the vanilla setting; retain safe defaults.
        }
        return fallback;
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
        SectorEntityToken existing = Global.getSector().getEntityById(
                GRAVITY_WELL_ID);
        NascentGravityWellAPI well = existing instanceof NascentGravityWellAPI
                ? (NascentGravityWellAPI) existing
                : null;
        SectorEntityToken arrival = ensureArrivalAnchor(system, well);
        if (arrival == null) return;

        if (existing != null
                && (well == null || well.getTarget() != arrival)) {
            if (existing.getContainingLocation() != null) {
                existing.getContainingLocation().removeEntity(existing);
            }
            well = null;
        }

        if (well == null) {
            well = Global.getSector().createNascentGravityWell(arrival, 50f);
            well.setId(GRAVITY_WELL_ID);
            Global.getSector().getHyperspace().addEntity(well);
            realign = true;
        }
        if (realign) {
            well.autoUpdateHyperLocationBasedOnInSystemEntityAtRadius(
                    arrival, 0f);
        }
    }

    /**
     * Keeps Transverse Jump arrivals clear of both the Gate and its guardian.
     *
     * The token is intentionally invisible and non-interactive. The nascent
     * gravity well targets it instead of the Gate, so Starsector places an
     * arriving fleet in open space while preserving the usual transverse-
     * jump presentation and mechanics.
     */
    private static SectorEntityToken ensureArrivalAnchor(
            StarSystemAPI system, NascentGravityWellAPI existingWell) {
        SectorEntityToken arrival = system.getEntityById(ARRIVAL_ANCHOR_ID);
        if (arrival == null && existingWell != null) {
            SectorEntityToken target = existingWell.getTarget();
            if (target != null
                    && ARRIVAL_ANCHOR_ID.equals(target.getId())
                    && target.getContainingLocation() == system) {
                arrival = target;
            }
        }
        if (arrival == null) {
            arrival = system.createToken(0f, 0f);
            if (arrival == null) return null;
            arrival.setId(ARRIVAL_ANCHOR_ID);
        }

        float radians = (float) Math.toRadians(ARRIVAL_ANGLE);
        arrival.setFixedLocation(
                (float) Math.cos(radians) * ARRIVAL_DISTANCE,
                (float) Math.sin(radians) * ARRIVAL_DISTANCE);
        arrival.setName("");
        arrival.setDiscoverable(null);
        arrival.setSensorProfile(null);
        arrival.addTag(Tags.NON_CLICKABLE);
        arrival.addTag(Tags.NO_ENTITY_TOOLTIP);
        arrival.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
        return arrival;
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
