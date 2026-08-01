package shiptrophy.campaign;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.StarTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl;

import shiptrophy.campaign.terrain.AltitudeWarningTerrainPlugin;

/**
 * Generates the prototype Dyson-sphere interior called Gan Eden.
 *
 * Gan Eden is intentionally cut off from ordinary hyperspace. Isa's quest
 * reveals a separate transit ring without creating a normal system anchor.
 */
public final class GanEdenGenerator {
    public static final String SYSTEM_ID = "ship_trophy_gan_eden";
    public static final String SYSTEM_NAME = "Gan Eden";
    public static final String STAR_ID = "ship_trophy_gan_eden_star";
    public static final String ALTITUDE_TERRAIN_ID = "ship_trophy_gan_eden_altitude";
    public static final String ALTITUDE_TERRAIN_TYPE = "ship_trophy_altitude_warning";
    public static final String ARRIVAL_RING_ID = "ship_trophy_gan_eden_arrival_ring";
    public static final String ARRIVAL_RING_TYPE = "ship_trophy_gan_eden_arrival_ring";
    public static final String SPACE_ELEVATOR_ID =
            "ship_trophy_gan_eden_space_elevator";
    public static final String SPACE_ELEVATOR_TYPE =
            "ship_trophy_gan_eden_space_elevator";
    public static final String MUSIC_SET_ID = "ship_trophy_gan_eden_music";
    public static final String TREE_OF_LIFE_ID = "ship_trophy_gan_eden_prime";
    private static final String LEGACY_VERDANT_REACH_ID =
            "ship_trophy_gan_eden_verdant_reach";
    public static final String PELAGOS_ID = "ship_trophy_gan_eden_pelagos";
    public static final String CINDERWAKE_ID =
            "ship_trophy_gan_eden_cinderwake";
    public static final String RIMEWELL_ID =
            "ship_trophy_gan_eden_rimewell";
    public static final String SANCTUARY_CONDITION_ID =
            "ship_trophy_gan_eden_sanctuary";
    public static final String AUREATE_SIEGE_CONDITION_ID =
            "ship_trophy_gan_eden_aureate_siege";
    public static final String SURFACE_SITE_TYPE =
            "ship_trophy_gan_eden_surface_site";
    public static final String SURFACE_SITE_MEMORY_KEY =
            "$shipTrophyGanEdenSurfaceSite";

    public static final float WARNING_INNER_RADIUS = 1650f;
    public static final float HARD_SURFACE_RADIUS = 2050f;
    public static final float ALTITUDE_EFFECT_OUTER_RADIUS = 2600f;
    public static final float SURFACE_OUTER_RADIUS = 3000f;

    private static final String SURFACE_BAND_ID = "ship_trophy_gan_eden_surface";
    private static final String INNER_SEAM_ID = "ship_trophy_gan_eden_inner_seam";
    private static final String OUTER_SEAM_ID = "ship_trophy_gan_eden_outer_seam";
    private static final String WARNING_BAND_ID = "ship_trophy_gan_eden_warning_band";
    private static final String[] SURFACE_SITE_IDS = {
        CINDERWAKE_ID,
        RIMEWELL_ID,
        TREE_OF_LIFE_ID,
        PELAGOS_ID
    };

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

            ensureColonizableSurfaceSites(system, star);
            ensureSpaceElevator(system);
            ensureInteriorSurface(system, star);
            ensureAltitudeWarning(system, star);
            ensureArrivalRing(system, star);
            removeHyperspaceAnchor(system);

        } catch (RuntimeException ex) {
            // A partially generated experimental system should never prevent
            // an existing campaign from loading.
            System.err.println("Hall of Triumph: failed to generate Gan Eden.");
            ex.printStackTrace(System.err);
        }
    }

    public static StarSystemAPI findSystem() {
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
        system.getMemoryWithoutUpdate().set(
                MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, MUSIC_SET_ID);
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

    /**
     * Adds four fixed settlement anchors on Gan Eden's inhabited inner shell.
     *
     * These are deliberately not separate worlds. They use PlanetAPI only as
     * a thin compatibility layer for Starsector's survey and colony screens;
     * visually and spatially they are small surface installations pinned to
     * permanent coordinates on the Dyson sphere's projected inner surface.
     * Stable ids preserve survey and colony state in existing campaigns.
     */
    private static void ensureColonizableSurfaceSites(
            StarSystemAPI system, PlanetAPI star) {
        ensureSurfaceSite(
                system, star, CINDERWAKE_ID, "Cinderwake",
                "ship_trophy_gan_eden_cinderwake", 173.2f, 1952f,
                Conditions.HOT,
                Conditions.ORE_ABUNDANT,
                Conditions.RARE_ORE_MODERATE);
        ensureSurfaceSite(
                system, star, RIMEWELL_ID, "Rimewell",
                "ship_trophy_gan_eden_rimewell", 120.6f, 1412f,
                Conditions.VERY_COLD,
                Conditions.VOLATILES_ABUNDANT,
                Conditions.RARE_ORE_SPARSE);
        ensureSurfaceSite(
                system, star, TREE_OF_LIFE_ID, "Tree of Life",
                "ship_trophy_gan_eden_eden_prime", 55.6f, 1082f,
                Conditions.HABITABLE,
                Conditions.MILD_CLIMATE,
                Conditions.FARMLAND_BOUNTIFUL,
                Conditions.ORGANICS_ABUNDANT);
        ensureSurfaceSite(
                system, star, PELAGOS_ID, "Pelagos Basin",
                "ship_trophy_gan_eden_pelagos_basin", 352.5f, 1699f,
                Conditions.HABITABLE,
                Conditions.WATER_SURFACE,
                Conditions.ORGANICS_ABUNDANT,
                Conditions.VOLATILES_DIFFUSE);

        removeSurfaceSite(system, LEGACY_VERDANT_REACH_ID);
        ensureSanctuaryCondition(system, CINDERWAKE_ID);
        ensureSanctuaryCondition(system, RIMEWELL_ID);
        ensureSanctuaryCondition(system, TREE_OF_LIFE_ID);
        ensureSanctuaryCondition(system, PELAGOS_ID);
        updateAureateSiegeConditions(system);
    }

    /**
     * Applies the system-wide crisis to every Gan Eden settlement while the
     * Golden Shard encounter is active, and removes it after total victory.
     */
    public static void updateAureateSiegeConditions(StarSystemAPI system) {
        if (system == null) return;

        boolean active = GanEdenAmbushScript.isEncounterActive();
        for (String entityId : SURFACE_SITE_IDS) {
            SectorEntityToken entity = system.getEntityById(entityId);
            if (!(entity instanceof PlanetAPI)) continue;

            MarketAPI market = ((PlanetAPI) entity).getMarket();
            if (market == null) continue;

            if (active) {
                if (!market.hasCondition(AUREATE_SIEGE_CONDITION_ID)) {
                    market.addCondition(AUREATE_SIEGE_CONDITION_ID);
                }
            } else if (market.hasCondition(AUREATE_SIEGE_CONDITION_ID)) {
                market.removeCondition(AUREATE_SIEGE_CONDITION_ID);
            }
        }
    }

    private static void removeSurfaceSite(
            StarSystemAPI system, String entityId) {
        SectorEntityToken entity = system.getEntityById(entityId);
        MarketAPI market = entity instanceof PlanetAPI
                ? ((PlanetAPI) entity).getMarket()
                : null;
        if (market == null
                && Global.getSector() != null
                && Global.getSector().getEconomy() != null) {
            market = Global.getSector().getEconomy().getMarket(entityId);
        }
        if (market != null
                && Global.getSector() != null
                && Global.getSector().getEconomy() != null) {
            Global.getSector().getEconomy().removeMarket(market);
        }
        if (entity != null) {
            system.removeEntity(entity);
        }
    }

    private static void ensureSanctuaryCondition(
            StarSystemAPI system, String entityId) {
        SectorEntityToken entity = system.getEntityById(entityId);
        if (!(entity instanceof PlanetAPI)) return;

        MarketAPI market = ((PlanetAPI) entity).getMarket();
        if (market == null) return;

        // Match Starsector's isolated tutorial-market pattern. A unique
        // economy group gives this colony no eligible import/export partners,
        // including the other sealed Gan Eden colony sites.
        market.setEconGroup(market.getId());
        if (!market.hasCondition(SANCTUARY_CONDITION_ID)) {
            market.addCondition(SANCTUARY_CONDITION_ID);
        }
        // Migration for saves created while Sanctuary supplied +100
        // stability. The condition now owns isolation only.
        market.getStability().unmodify(SANCTUARY_CONDITION_ID);
    }

    private static PlanetAPI ensureSurfaceSite(
            StarSystemAPI system,
            PlanetAPI star,
            String id,
            String name,
            String illustrationId,
            float angle,
            float surfaceRadius,
            String... conditions) {
        SectorEntityToken existing = system.getEntityById(id);
        PlanetAPI site = existing instanceof PlanetAPI
                ? (PlanetAPI) existing
                : null;
        if (existing != null && site == null) {
            system.removeEntity(existing);
        }

        boolean created = site == null;
        if (created) {
            // addPlanet supplies the vanilla planet-condition market used by
            // surveying and colonization. The orbit is immediately replaced
            // by a fixed shell coordinate below.
            site = system.addPlanet(
                    id, star, name, SURFACE_SITE_TYPE,
                    angle, 44f, surfaceRadius, 100000f);
        }
        if (site == null) return null;

        if (!SURFACE_SITE_TYPE.equals(site.getTypeId())) {
            site.changeType(
                    SURFACE_SITE_TYPE,
                    new java.util.Random(id.hashCode()));
        }
        site.setRadius(44f);
        site.getSpec().setPlanetColor(new Color(255, 255, 255, 0));
        site.getSpec().setIconColor(new Color(255, 255, 255, 0));
        site.setFixedLocation(
                star.getLocation().x + cosDegrees(angle) * surfaceRadius,
                star.getLocation().y + sinDegrees(angle) * surfaceRadius);
        site.setDiscoverable(true);
        site.setDescriptionIdOverride(SURFACE_SITE_TYPE);
        site.setInteractionImage("illustrations", illustrationId);
        site.addTag(Tags.NOT_RANDOM_MISSION_TARGET);

        MarketAPI market = site.getMarket();
        if (market != null) {
            // Once the player establishes a colony, vanilla changes this flag
            // to false. Never flip it back or overwrite the player's name on
            // a later load; only the still-unclaimed surface site is a
            // planet-condition-only market.
            boolean establishedColony = !market.isPlanetConditionMarketOnly();
            if (!establishedColony) {
                site.setName(name);
                market.setPlanetConditionMarketOnly(true);
            }
            market.getMemoryWithoutUpdate().set(SURFACE_SITE_MEMORY_KEY, true);
            if (created) {
                market.setSurveyLevel(MarketAPI.SurveyLevel.NONE);
                for (String condition : conditions) {
                    market.addCondition(condition);
                }
            }
        } else {
            site.setName(name);
        }
        return site;
    }

    /**
     * Adds the Tree of Life space elevator that hosts Isa's grave cutscene.
     * Its stable id lets the eventual story interaction bind to the same POI
     * in both new and existing campaigns.
     */
    private static void ensureSpaceElevator(StarSystemAPI system) {
        SectorEntityToken treeOfLife = system.getEntityById(TREE_OF_LIFE_ID);
        if (!(treeOfLife instanceof PlanetAPI)) return;

        SectorEntityToken elevator = system.getEntityById(SPACE_ELEVATOR_ID);
        if (elevator == null) {
            elevator = system.addCustomEntity(
                    SPACE_ELEVATOR_ID,
                    "Tree of Life Space Elevator",
                    SPACE_ELEVATOR_TYPE,
                    Factions.NEUTRAL);
        }
        if (elevator == null) return;
        elevator.setName("Tree of Life Space Elevator");

        // The elevator is rooted in the shell as well. Campaign coordinates
        // are a projection of that surface, so keep its marker fixed beside
        // Tree of Life rather than literally moving it toward the central sun.
        PlanetAPI star = system.getStar();
        float centerX = star == null ? 0f : star.getLocation().x;
        float centerY = star == null ? 0f : star.getLocation().y;
        float dx = treeOfLife.getLocation().x - centerX;
        float dy = treeOfLife.getLocation().y - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        if (distance > 0f) {
            float tangentX = -dy / distance;
            float tangentY = dx / distance;
            float outwardX = dx / distance;
            float outwardY = dy / distance;
            float elevatorX = treeOfLife.getLocation().x
                    + tangentX * 125f + outwardX * 30f;
            float elevatorY = treeOfLife.getLocation().y
                    + tangentY * 125f + outwardY * 30f;
            elevator.setFixedLocation(
                    elevatorX,
                    elevatorY);
            elevator.setFacing((float) Math.toDegrees(Math.atan2(
                    treeOfLife.getLocation().y - elevatorY,
                    treeOfLife.getLocation().x - elevatorX)) - 90f);
        }
        elevator.setDiscoverable(true);
        elevator.setSensorProfile(1f);
        elevator.setCustomDescriptionId(SPACE_ELEVATOR_TYPE);
        elevator.setInteractionImage("illustrations", "orbital");
        elevator.addTag(Tags.STATION);
        elevator.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
    }

    private static float cosDegrees(float angle) {
        return (float) Math.cos(Math.toRadians(angle));
    }

    private static float sinDegrees(float angle) {
        return (float) Math.sin(Math.toRadians(angle));
    }

    private static void ensureAltitudeWarning(StarSystemAPI system, PlanetAPI star) {
        // Let the soft return field continue beneath the visible atmosphere.
        // This gives fast fleets room to decelerate and rebound without a
        // position clamp at the apparent surface.
        float width = ALTITUDE_EFFECT_OUTER_RADIUS - WARNING_INNER_RADIUS;
        float middle = (ALTITUDE_EFFECT_OUTER_RADIUS + WARNING_INNER_RADIUS) * 0.5f;
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
        if (GanEdenQuestManager.isAtLeast(
                GanEdenQuestManager.Stage.GAN_EDEN_REVEALED)) {
            ring.setSensorProfile(1f);
            ring.addTag(Tags.GATE);
            ring.addTag(Tags.STORY_CRITICAL);
            ring.addTag(Tags.HAS_INTERACTION_DIALOG);
            ring.removeTag(Tags.NON_CLICKABLE);
            ring.removeTag(Tags.NO_ENTITY_TOOLTIP);
        } else {
            ring.setSensorProfile(null);
            ring.addTag(Tags.NON_CLICKABLE);
            ring.addTag(Tags.NO_ENTITY_TOOLTIP);
        }
        ring.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
    }

    private static void removeLegacyBand(StarSystemAPI system, String id) {
        SectorEntityToken band = system.getEntityById(id);
        if (band != null) {
            system.removeEntity(band);
        }
    }

}
