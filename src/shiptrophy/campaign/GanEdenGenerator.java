package shiptrophy.campaign;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;

import shiptrophy.campaign.terrain.AltitudeWarningTerrainPlugin;

/**
 * Generates the prototype Dyson-sphere interior called Gan Eden.
 *
 * Gan Eden is intentionally cut off from hyperspace. The future route through
 * the Shattered Ring is represented by a feature flag and remains disabled
 * while the system is being developed and tested.
 */
public final class GanEdenGenerator {
    public static final String SYSTEM_ID = "ship_trophy_gan_eden";
    public static final String SYSTEM_NAME = "Gan Eden";
    public static final String STAR_ID = "ship_trophy_gan_eden_star";
    public static final String ALTITUDE_TERRAIN_ID = "ship_trophy_gan_eden_altitude";
    public static final String ALTITUDE_TERRAIN_TYPE = "ship_trophy_altitude_warning";
    public static final String ARRIVAL_RING_ID = "ship_trophy_gan_eden_arrival_ring";
    public static final String ARRIVAL_RING_TYPE = "ship_trophy_gan_eden_arrival_ring";

    public static final boolean SHATTERED_RING_GATEWAY_ENABLED = false;

    public static final float WARNING_INNER_RADIUS = 1650f;
    public static final float WARNING_OUTER_RADIUS = 2150f;
    public static final float HARD_SURFACE_RADIUS = 2050f;
    public static final float SURFACE_OUTER_RADIUS = 3000f;

    private static final String SURFACE_BAND_ID = "ship_trophy_gan_eden_surface";
    private static final String INNER_SEAM_ID = "ship_trophy_gan_eden_inner_seam";
    private static final String OUTER_SEAM_ID = "ship_trophy_gan_eden_outer_seam";
    private static final String WARNING_BAND_ID = "ship_trophy_gan_eden_warning_band";

    private GanEdenGenerator() {
    }

    public static void ensureGenerated() {
        if (Global.getSector() == null) return;

        try {
            StarSystemAPI system = findSystem();
            if (system == null) {
                system = createSystem();
            }
            if (system == null) return;

            configureSilo(system);
            PlanetAPI star = ensureStar(system);
            if (star == null) return;

            ensureInteriorSurface(system, star);
            ensureAltitudeWarning(system, star);
            ensureArrivalRing(system, star);
            removeHyperspaceAnchor(system);

            // Deliberately dormant for now. A later story pass will create the
            // Shattered Ring transit mechanism when this flag is enabled.
            if (SHATTERED_RING_GATEWAY_ENABLED) {
                ensureShatteredRingGateway(system);
            }
        } catch (RuntimeException ex) {
            // A partially generated experimental system should never prevent
            // an existing campaign from loading.
            System.err.println("Hall of Triumph: failed to generate Gan Eden.");
            ex.printStackTrace(System.err);
        }
    }

    private static StarSystemAPI findSystem() {
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

    private static StarSystemAPI createSystem() {
        StarSystemAPI system = Global.getSector().createStarSystem(SYSTEM_NAME);
        if (system == null) return null;

        // This location is not used for travel; it merely keeps a transient
        // anchor, should another mod force one to be created, away from the
        // playable Sector until removeHyperspaceAnchor() runs.
        system.getLocation().set(100000f, 100000f);
        system.setBaseName(SYSTEM_NAME);
        system.setBackgroundTextureFilename("graphics/backgrounds/wormhole_dest_black.jpg");
        system.setLightColor(new Color(255, 238, 190));
        system.setMapGridWidthOverride(6500f);
        system.setMapGridHeightOverride(6500f);
        system.setMaxRadiusInHyperspace(0f);
        system.setProcgen(false);
        return system;
    }

    private static void configureSilo(StarSystemAPI system) {
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
        system.addTag(Tags.THEME_HIDDEN);
        system.addTag(Tags.THEME_MISC_SKIP);
        system.addTag(Tags.DO_NOT_SHOW_STRANDED_DIALOG);
        system.addTag(Tags.DO_NOT_RESPAWN_PLAYER_IN);
        system.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
        system.addTag(Tags.SYSTEM_ALREADY_USED_FOR_STORY);
        system.setBackgroundTextureFilename("graphics/backgrounds/wormhole_dest_black.jpg");
        system.setDoNotShowIntelFromThisLocationOnMap(true);
        system.setMapGridWidthOverride(6500f);
        system.setMapGridHeightOverride(6500f);
        system.setMaxRadiusInHyperspace(0f);
    }

    private static PlanetAPI ensureStar(StarSystemAPI system) {
        PlanetAPI star = system.getStar();
        if (star == null) {
            star = system.initStar(
                    STAR_ID,
                    StarTypes.YELLOW,
                    325f,
                    150f,
                    5f,
                    0.15f,
                    1f);
        }
        if (star != null) {
            star.setName("Gan Eden");
            star.setCustomDescriptionId(null);
            star.setDiscoverable(false);
            star.addTag(Tags.STAR_HIDDEN_ON_MAP);
            star.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
            star.setLightColorOverrideIfStar(new Color(255, 238, 190));
        }
        return star;
    }

    private static void ensureInteriorSurface(StarSystemAPI system, PlanetAPI star) {
        // The first prototype used ordinary planetary ring bands. Those are
        // culled like normal orbital scenery and can be invisible from the
        // initial camera position. The altitude terrain now renders the shell
        // explicitly; remove legacy bands from saves that already generated
        // the first version.
        removeLegacyBand(system, SURFACE_BAND_ID);
        removeLegacyBand(system, INNER_SEAM_ID);
        removeLegacyBand(system, OUTER_SEAM_ID);
        removeLegacyBand(system, WARNING_BAND_ID);
    }

    private static void ensureAltitudeWarning(StarSystemAPI system, PlanetAPI star) {
        float width = WARNING_OUTER_RADIUS - WARNING_INNER_RADIUS;
        float middle = (WARNING_OUTER_RADIUS + WARNING_INNER_RADIUS) * 0.5f;
        SectorEntityToken existing = system.getEntityById(ALTITUDE_TERRAIN_ID);
        if (existing instanceof CampaignTerrainAPI) {
            CampaignTerrainAPI terrain = (CampaignTerrainAPI) existing;
            if (terrain.getPlugin() instanceof AltitudeWarningTerrainPlugin) {
                AltitudeWarningTerrainPlugin plugin =
                        (AltitudeWarningTerrainPlugin) terrain.getPlugin();
                plugin.reconfigure(star, width, middle);
                existing.setCircularOrbit(star, 0f, 0f, 100000f);
                return;
            }
        }
        if (existing != null) {
            system.removeEntity(existing);
        }

        RingParams params = new RingParams(width, middle, star, "Altitude Warning");
        SectorEntityToken terrain = system.addTerrain(ALTITUDE_TERRAIN_TYPE, params);
        if (terrain != null) {
            terrain.setId(ALTITUDE_TERRAIN_ID);
            terrain.setCircularOrbit(star, 0f, 0f, 100000f);
        }
    }

    private static void removeHyperspaceAnchor(StarSystemAPI system) {
        SectorEntityToken anchor = system.getHyperspaceAnchor();
        if (anchor == null) return;

        if (anchor.getContainingLocation() != null) {
            anchor.getContainingLocation().removeEntity(anchor);
        }
        system.setHyperspaceAnchor(null);
    }

    private static void ensureArrivalRing(StarSystemAPI system, PlanetAPI star) {
        SectorEntityToken ring = system.getEntityById(ARRIVAL_RING_ID);
        if (ring == null) {
            ring = system.addCustomEntity(
                    ARRIVAL_RING_ID,
                    "Eden Transit Ring",
                    ARRIVAL_RING_TYPE,
                    Factions.NEUTRAL);
        }
        if (ring == null) return;

        ring.setCircularOrbitPointingDown(star, 35f, 1250f, 100000f);
        ring.setDiscoverable(false);
        ring.setSensorProfile(null);
        ring.addTag(Tags.NON_CLICKABLE);
        ring.addTag(Tags.NO_ENTITY_TOOLTIP);
        ring.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
    }

    private static void removeLegacyBand(StarSystemAPI system, String id) {
        SectorEntityToken band = system.getEntityById(id);
        if (band != null) {
            system.removeEntity(band);
        }
    }

    private static void ensureShatteredRingGateway(StarSystemAPI system) {
        // Story-gated transit is intentionally not implemented during the
        // environment prototype. Keeping this method empty makes the disabled
        // state explicit and prevents accidental access from Penelope's Star.
    }
}
