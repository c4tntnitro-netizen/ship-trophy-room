package shiptrophy.campaign;

import java.awt.Color;
import java.util.ArrayList;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
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
 * Gan Eden begins cut off from ordinary hyperspace. Isa's quest adds its
 * internal transit ring as the initial route in and out. The first complete
 * defeat of the Golden Shards permanently opens a conventional jump point;
 * later guardian waves never seal it again.
 */
public final class GanEdenGenerator {
    public static final String SYSTEM_ID = "ship_trophy_gan_eden";
    public static final String SYSTEM_NAME = "Gan Eden";
    public static final String STAR_ID = "ship_trophy_gan_eden_star";
    public static final String ALTITUDE_TERRAIN_ID = "ship_trophy_gan_eden_altitude";
    public static final String ALTITUDE_TERRAIN_TYPE = "ship_trophy_altitude_warning";
    public static final String ARRIVAL_RING_ID = "ship_trophy_gan_eden_arrival_ring";
    public static final String ARRIVAL_RING_TYPE = "ship_trophy_gan_eden_arrival_ring";
    public static final String HYPERSPACE_JUMP_ID =
            "ship_trophy_gan_eden_hyperspace_jump";
    public static final String HEAVENS_SCAR_TYPE =
            "ship_trophy_gan_eden_heavens_scar";
    public static final String HEAVENS_SCAR_NAME = "Heavens Scar";
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
    public static final String CINDERWAKE_SITE_TYPE =
            "ship_trophy_gan_eden_volcanic_cinderwake_site";
    public static final String RIMEWELL_SITE_TYPE =
            "ship_trophy_gan_eden_frozen_rimewell_site";
    public static final String TREE_OF_LIFE_SITE_TYPE =
            "ship_trophy_gan_eden_terran_tree_of_life_site";
    public static final String PELAGOS_SITE_TYPE =
            "ship_trophy_gan_eden_water_pelagos_site";
    public static final String SURFACE_SITE_MEMORY_KEY =
            "$shipTrophyGanEdenSurfaceSite";
    private static final String ECONOMY_RELEASED_MEMORY_KEY =
            "$shipTrophyGanEdenEconomyReleased";

    public static final float WARNING_INNER_RADIUS = 1650f;
    public static final float HARD_SURFACE_RADIUS = 2050f;
    public static final float ALTITUDE_EFFECT_OUTER_RADIUS = 2600f;
    public static final float SURFACE_OUTER_RADIUS = 3000f;

    private static final String SURFACE_BAND_ID = "ship_trophy_gan_eden_surface";
    private static final String INNER_SEAM_ID = "ship_trophy_gan_eden_inner_seam";
    private static final String OUTER_SEAM_ID = "ship_trophy_gan_eden_outer_seam";
    private static final String WARNING_BAND_ID = "ship_trophy_gan_eden_warning_band";
    private static final String HYPERSPACE_LINK_GENERATED_KEY =
            "$shipTrophyGanEdenHyperspaceLinkGenerated";
    private static final String HYPERSPACE_PLACEMENT_VERSION_KEY =
            "$shipTrophyGanEdenHyperspacePlacementVersion";
    private static final int HYPERSPACE_PLACEMENT_VERSION = 1;
    private static final float TRANSIT_OFFSET_X_FRACTION = 0.06f;
    private static final float TRANSIT_OFFSET_Y_FRACTION = 0.06f;
    private static final float MIN_TRANSIT_OFFSET_X = 6000f;
    private static final float MIN_TRANSIT_OFFSET_Y = 4500f;
    private static final float OPEN_HYPERSPACE_RADIUS = 3500f;
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

            boolean conventionalAccessOpen =
                    GanEdenAmbushScript.isDefeated();
            configureSystem(system, conventionalAccessOpen);
            PlanetAPI star = ensureStar(system);
            if (star == null) return;

            ensureColonizableSurfaceSites(system, star);
            ensureSpaceElevator(system);
            ensureInteriorSurface(system, star);
            ensureAltitudeWarning(system, star);
            ensureArrivalRing(system, star);
            if (conventionalAccessOpen) {
                ensureConventionalHyperspaceAccess(system, star);
            } else {
                removeConventionalHyperspaceAccess(system);
                ensureHeavensScar(system, star);
            }

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

        // configureSystem() assigns a sector-relative position. Keeping the
        // initial token at the origin avoids baking vanilla map dimensions
        // into saves created by sector-overhaul mods.
        system.setBaseName(SYSTEM_NAME);
        system.setBackgroundTextureFilename("graphics/backgrounds/wormhole_dest_black.jpg");
        system.setLightColor(new Color(255, 238, 190));
        system.setMapGridWidthOverride(6500f);
        system.setMapGridHeightOverride(6500f);
        system.setMaxRadiusInHyperspace(0f);
        system.setProcgen(false);
        return system;
    }

    private static void configureSystem(
            StarSystemAPI system, boolean conventionalAccessOpen) {
        boolean placementChanged = ensureHyperspacePlacement(system);
        if (placementChanged) {
            clearAutogeneratedHyperspaceLinks(system);
            system.getMemoryWithoutUpdate().set(
                    HYPERSPACE_PLACEMENT_VERSION_KEY,
                    HYPERSPACE_PLACEMENT_VERSION);
        }
        if (conventionalAccessOpen) {
            system.removeTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
            system.removeTag(Tags.DO_NOT_SHOW_STRANDED_DIALOG);
            system.removeTag(Tags.THEME_HIDDEN);
            system.addTag(Tags.THEME_SPECIAL);
            system.setDoNotShowIntelFromThisLocationOnMap(false);
            system.setMaxRadiusInHyperspace(OPEN_HYPERSPACE_RADIUS);
        } else {
            system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER);
            system.addTag(Tags.DO_NOT_SHOW_STRANDED_DIALOG);
            system.addTag(Tags.THEME_HIDDEN);
            system.removeTag(Tags.THEME_SPECIAL);
            system.setDoNotShowIntelFromThisLocationOnMap(true);
            system.setMaxRadiusInHyperspace(0f);
        }
        system.addTag(Tags.DO_NOT_RESPAWN_PLAYER_IN);
        system.addTag(Tags.THEME_MISC_SKIP);
        system.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
        system.addTag(Tags.SYSTEM_ALREADY_USED_FOR_STORY);
        system.getMemoryWithoutUpdate().set(
                MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, MUSIC_SET_ID);
        system.setBackgroundTextureFilename("graphics/backgrounds/wormhole_dest_black.jpg");
        system.setMapGridWidthOverride(6500f);
        system.setMapGridHeightOverride(6500f);
    }

    /** Places Gan Eden near, but not on top of, its northeast transit system. */
    private static boolean ensureHyperspacePlacement(StarSystemAPI system) {
        if (system.getMemoryWithoutUpdate().getInt(
                HYPERSPACE_PLACEMENT_VERSION_KEY)
                >= HYPERSPACE_PLACEMENT_VERSION) {
            return false;
        }

        float width = GanEdenTransitSystemGenerator
                .getConfiguredSectorWidth();
        float height = GanEdenTransitSystemGenerator
                .getConfiguredSectorHeight();
        float x = GanEdenTransitSystemGenerator.getTargetHyperspaceX()
                - Math.max(MIN_TRANSIT_OFFSET_X,
                        width * TRANSIT_OFFSET_X_FRACTION);
        float y = GanEdenTransitSystemGenerator.getTargetHyperspaceY()
                - Math.max(MIN_TRANSIT_OFFSET_Y,
                        height * TRANSIT_OFFSET_Y_FRACTION);
        system.getLocation().set(x, y);
        return true;
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
                CINDERWAKE_SITE_TYPE,
                "ship_trophy_gan_eden_cinderwake", 173.2f, 1952f,
                Conditions.HOT,
                Conditions.ORE_ULTRARICH,
                Conditions.RARE_ORE_ULTRARICH);
        ensureSurfaceSite(
                system, star, RIMEWELL_ID, "Rimewell",
                RIMEWELL_SITE_TYPE,
                "ship_trophy_gan_eden_rimewell", 120.6f, 1412f,
                Conditions.VERY_COLD,
                Conditions.VOLATILES_PLENTIFUL,
                Conditions.RARE_ORE_ULTRARICH);
        ensureSurfaceSite(
                system, star, TREE_OF_LIFE_ID, "Tree of Life",
                TREE_OF_LIFE_SITE_TYPE,
                "ship_trophy_gan_eden_eden_prime", 55.6f, 1082f,
                Conditions.HABITABLE,
                Conditions.MILD_CLIMATE,
                Conditions.FARMLAND_BOUNTIFUL,
                Conditions.ORGANICS_PLENTIFUL);
        ensureSurfaceSite(
                system, star, PELAGOS_ID, "Pelagos Basin",
                PELAGOS_SITE_TYPE,
                "ship_trophy_gan_eden_pelagos_basin", 352.5f, 1699f,
                Conditions.HABITABLE,
                Conditions.WATER_SURFACE,
                Conditions.ORGANICS_PLENTIFUL,
                Conditions.VOLATILES_PLENTIFUL);

        removeLegacySurfaceMarkers(system);
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

        boolean open = GanEdenAmbushScript.isDefeated();
        if (open) {
            // Release the quest-owned economy silo once. After that, do not
            // overwrite a grouping assigned by a colony overhaul on load.
            if (!market.getMemoryWithoutUpdate().getBoolean(
                    ECONOMY_RELEASED_MEMORY_KEY)) {
                market.setEconGroup(null);
                market.getMemoryWithoutUpdate().set(
                        ECONOMY_RELEASED_MEMORY_KEY, true);
            }
            if (market.hasCondition(SANCTUARY_CONDITION_ID)) {
                market.removeCondition(SANCTUARY_CONDITION_ID);
            }
        } else {
            // Match Starsector's isolated tutorial-market pattern. A unique
            // economy group gives this colony no eligible import/export
            // partners, including the other sealed Gan Eden colony sites.
            market.setEconGroup(market.getId());
            market.getMemoryWithoutUpdate().unset(
                    ECONOMY_RELEASED_MEMORY_KEY);
            if (!market.hasCondition(SANCTUARY_CONDITION_ID)) {
                market.addCondition(SANCTUARY_CONDITION_ID);
            }
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
            String siteType,
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
                    id, star, name, siteType,
                    angle, 44f, surfaceRadius, 100000f);
        }
        if (site == null) return null;

        MarketAPI initialMarket = site.getMarket();
        boolean establishedColony = initialMarket != null
                && !initialMarket.isPlanetConditionMarketOnly();

        // Migrate unclaimed sites to the biome-bearing compatibility types,
        // but never undo a type change made to an established colony by AotD,
        // TASC, DIY Planets, or another terraforming system.
        if (!establishedColony && !siteType.equals(site.getTypeId())) {
            site.changeType(
                    siteType,
                    new java.util.Random(id.hashCode()));
        }
        site.setRadius(44f);
        if (!establishedColony || isManagedSurfaceType(site.getTypeId())) {
            site.getSpec().setPlanetColor(new Color(255, 255, 255, 255));
            site.getSpec().setIconColor(new Color(190, 195, 200, 255));
            site.applySpecChanges();
        }
        site.setFixedLocation(
                star.getLocation().x + cosDegrees(angle) * surfaceRadius,
                star.getLocation().y + sinDegrees(angle) * surfaceRadius);
        // Surface districts are usable from the moment Gan Eden is reached.
        // Reapplying a literal true here used to put an already-discovered
        // site back into sensor-contact state whenever generation maintenance
        // ran. Null is Starsector's stable, already-known state.
        site.setDiscoverable(null);
        site.setSensorProfile(null);
        site.removeTag(Tags.NON_CLICKABLE);
        site.removeTag(Tags.NO_ENTITY_TOOLTIP);
        site.setDescriptionIdOverride(SURFACE_SITE_TYPE);
        site.setInteractionImage("illustrations", illustrationId);
        site.addTag(Tags.NOT_RANDOM_MISSION_TARGET);

        MarketAPI market = site.getMarket();
        if (market != null) {
            // Once the player establishes a colony, vanilla changes this flag
            // to false. Never flip it back or overwrite the player's name on
            // a later load; only the still-unclaimed surface site is a
            // planet-condition-only market.
            establishedColony = !market.isPlanetConditionMarketOnly();
            if (!establishedColony) {
                site.setName(name);
                market.setPlanetConditionMarketOnly(true);
            }
            market.getMemoryWithoutUpdate().set(SURFACE_SITE_MEMORY_KEY, true);
            boolean resourcesChanged = false;
            if (created) {
                market.setSurveyLevel(MarketAPI.SurveyLevel.NONE);
            }
            if (!establishedColony) {
                for (String condition : conditions) {
                    if (isResourceCondition(condition)) {
                        resourcesChanged |= ensureMaximumResourceCondition(
                                market, condition);
                    } else if (created && !market.hasCondition(condition)) {
                        market.addCondition(condition);
                    }
                }
            }
            if (resourcesChanged) market.reapplyConditions();
        } else {
            site.setName(name);
        }
        return site;
    }

    private static boolean isManagedSurfaceType(String typeId) {
        if (typeId == null) return false;
        return SURFACE_SITE_TYPE.equals(typeId)
                || CINDERWAKE_SITE_TYPE.equals(typeId)
                || RIMEWELL_SITE_TYPE.equals(typeId)
                || TREE_OF_LIFE_SITE_TYPE.equals(typeId)
                || PELAGOS_SITE_TYPE.equals(typeId)
                || "ship_trophy_gan_eden_cinderwake_site".equals(typeId)
                || "ship_trophy_gan_eden_rimewell_site".equals(typeId)
                || "ship_trophy_gan_eden_tree_of_life_site".equals(typeId)
                || "ship_trophy_gan_eden_pelagos_site".equals(typeId);
    }

    /** Removes flat marker overlays created by the abandoned visibility experiment. */
    private static void removeLegacySurfaceMarkers(StarSystemAPI system) {
        if (system == null) return;
        String[] markerIds = {
            CINDERWAKE_ID + "_marker",
            RIMEWELL_ID + "_marker",
            TREE_OF_LIFE_ID + "_marker",
            PELAGOS_ID + "_marker"
        };
        for (String markerId : markerIds) {
            SectorEntityToken marker = system.getEntityById(markerId);
            if (marker != null) system.removeEntity(marker);
        }
    }

    private static boolean isResourceCondition(String condition) {
        return resourceFamily(condition) != null;
    }

    /** Replaces lower-tier deposits in the same vanilla resource family. */
    private static boolean ensureMaximumResourceCondition(
            MarketAPI market, String target) {
        String[] family = resourceFamily(target);
        if (market == null || family == null) return false;

        boolean changed = false;
        for (String condition : family) {
            if (!target.equals(condition) && market.hasCondition(condition)) {
                market.removeCondition(condition);
                changed = true;
            }
        }
        if (!market.hasCondition(target)) {
            market.addCondition(target);
            changed = true;
        }
        return changed;
    }

    private static String[] resourceFamily(String condition) {
        if (Conditions.ORE_ULTRARICH.equals(condition)) {
            return new String[] {
                Conditions.ORE_SPARSE, Conditions.ORE_MODERATE,
                Conditions.ORE_ABUNDANT, Conditions.ORE_RICH,
                Conditions.ORE_ULTRARICH
            };
        }
        if (Conditions.RARE_ORE_ULTRARICH.equals(condition)) {
            return new String[] {
                Conditions.RARE_ORE_SPARSE, Conditions.RARE_ORE_MODERATE,
                Conditions.RARE_ORE_ABUNDANT, Conditions.RARE_ORE_RICH,
                Conditions.RARE_ORE_ULTRARICH
            };
        }
        if (Conditions.VOLATILES_PLENTIFUL.equals(condition)) {
            return new String[] {
                Conditions.VOLATILES_TRACE, Conditions.VOLATILES_DIFFUSE,
                Conditions.VOLATILES_ABUNDANT, Conditions.VOLATILES_PLENTIFUL
            };
        }
        if (Conditions.ORGANICS_PLENTIFUL.equals(condition)) {
            return new String[] {
                Conditions.ORGANICS_TRACE, Conditions.ORGANICS_COMMON,
                Conditions.ORGANICS_ABUNDANT, Conditions.ORGANICS_PLENTIFUL
            };
        }
        if (Conditions.FARMLAND_BOUNTIFUL.equals(condition)) {
            return new String[] {
                Conditions.FARMLAND_POOR, Conditions.FARMLAND_ADEQUATE,
                Conditions.FARMLAND_RICH, Conditions.FARMLAND_BOUNTIFUL
            };
        }
        return null;
    }

    /**
     * Identifies Gan Eden by its canonical location rather than SYSTEM_ID.
     * Starsector derives a star system's runtime id from its base name, so a
     * system created as "Gan Eden" reports "gan eden", not our content id.
     */
    public static boolean isGanEden(StarSystemAPI system) {
        if (system == null) return false;
        if (Global.getSector() != null) {
            StarSystemAPI canonical = Global.getSector().getStarSystem(
                    SYSTEM_NAME);
            if (system == canonical) return true;
        }
        return SYSTEM_NAME.equalsIgnoreCase(system.getName())
                || SYSTEM_NAME.equalsIgnoreCase(system.getBaseName())
                || SYSTEM_NAME.equalsIgnoreCase(system.getId())
                || SYSTEM_ID.equalsIgnoreCase(system.getId());
    }

    public static boolean isGanEden(LocationAPI location) {
        return location instanceof StarSystemAPI
                && isGanEden((StarSystemAPI) location);
    }

    public static boolean isGanEden(SectorEntityToken entity) {
        return entity != null
                && (isGanEden(entity.getContainingLocation())
                        || isGanEden(entity.getStarSystem()));
    }

    /** Adds the remote elevator; its archive opens only after the Shard battle. */
    private static void ensureSpaceElevator(StarSystemAPI system) {
        SectorEntityToken elevator = system.getEntityById(SPACE_ELEVATOR_ID);
        if (elevator == null) {
            elevator = system.addCustomEntity(
                    SPACE_ELEVATOR_ID,
                    "Gan Eden Space Elevator",
                    SPACE_ELEVATOR_TYPE,
                    Factions.NEUTRAL);
        }
        if (elevator == null) return;
        elevator.setName("Gan Eden Space Elevator");

        // This is a different surface district from Tree of Life. Keeping the
        // marker well across the projected shell makes reaching it a distinct
        // post-battle journey instead of an extra button beside Log #4.
        PlanetAPI star = system.getStar();
        float centerX = star == null ? 0f : star.getLocation().x;
        float centerY = star == null ? 0f : star.getLocation().y;
        float angle = -24f;
        float radius = 1860f;
        float elevatorX = centerX + cosDegrees(angle) * radius;
        float elevatorY = centerY + sinDegrees(angle) * radius;
        elevator.setFixedLocation(elevatorX, elevatorY);
        elevator.setFacing(angle + 90f);

        boolean revealed = GanEdenQuestManager.isAtLeast(
                GanEdenQuestManager.Stage.GAN_EDEN_REVEALED);
        elevator.setDiscoverable(revealed ? null : true);
        elevator.setSensorProfile(revealed ? null : 1f);
        elevator.setCustomDescriptionId(SPACE_ELEVATOR_TYPE);
        elevator.setInteractionImage("illustrations", "orbital");
        elevator.addTag(Tags.STATION);
        elevator.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
        if (revealed) {
            elevator.removeTag(Tags.NON_CLICKABLE);
            elevator.removeTag(Tags.NO_ENTITY_TOOLTIP);
            elevator.addTag(Tags.HAS_INTERACTION_DIALOG);
        } else {
            elevator.addTag(Tags.NON_CLICKABLE);
            elevator.addTag(Tags.NO_ENTITY_TOOLTIP);
            elevator.removeTag(Tags.HAS_INTERACTION_DIALOG);
        }
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

    /**
     * Permanently opens Gan Eden after the first complete Golden Omega win.
     * The defeated flag is never cleared by the repeat-wave scheduler, so the
     * route remains present while later guardian fleets respawn.
     */
    private static void ensureConventionalHyperspaceAccess(
            StarSystemAPI system, PlanetAPI star) {
        SectorEntityToken existing = system.getEntityById(
                HYPERSPACE_JUMP_ID);
        JumpPointAPI jump = existing instanceof JumpPointAPI
                ? (JumpPointAPI) existing
                : null;
        if (existing != null && jump == null) {
            system.removeEntity(existing);
        }

        if (jump == null) {
            jump = Global.getFactory().createJumpPoint(
                    HYPERSPACE_JUMP_ID, HEAVENS_SCAR_NAME);
            jump.setRelatedPlanet(star);
            jump.setStandardWormholeToHyperspaceVisual();
            system.addEntity(jump);
        }
        jump.setName(HEAVENS_SCAR_NAME);
        jump.setCustomDescriptionId(HEAVENS_SCAR_TYPE);
        jump.setCircularOrbit(star, 215f, 1250f, 100000f);
        jump.setDiscoverable(null);
        jump.setSensorProfile(null);
        jump.addTag(Tags.STORY_CRITICAL);
        jump.addTag(Tags.NOT_RANDOM_MISSION_TARGET);

        boolean hasHyperspaceSide =
                system.getAutogeneratedJumpPointsInHyper() != null
                && !system.getAutogeneratedJumpPointsInHyper().isEmpty();
        if (!hasHyperspaceSide
                || !system.getMemoryWithoutUpdate().getBoolean(
                        HYPERSPACE_LINK_GENERATED_KEY)) {
            system.autogenerateHyperspaceJumpPoints(true, false);
            system.getMemoryWithoutUpdate().set(
                    HYPERSPACE_LINK_GENERATED_KEY, true);
        }
    }

    /** Removes conventional access until the first guardian victory. */
    private static void removeConventionalHyperspaceAccess(
            StarSystemAPI system) {
        SectorEntityToken legacyJump = system.getEntityById(
                HYPERSPACE_JUMP_ID);
        if (legacyJump instanceof JumpPointAPI) {
            system.removeEntity(legacyJump);
        }

        clearAutogeneratedHyperspaceLinks(system);
    }

    /** Clears only the hyperspace-side link and anchor, not in-system POIs. */
    private static void clearAutogeneratedHyperspaceLinks(
            StarSystemAPI system) {
        if (system == null) return;

        if (system.getAutogeneratedJumpPointsInHyper() != null) {
            for (JumpPointAPI jump : new ArrayList<JumpPointAPI>(
                    system.getAutogeneratedJumpPointsInHyper())) {
                if (jump != null && jump.getContainingLocation() != null) {
                    jump.getContainingLocation().removeEntity(jump);
                }
            }
            system.getAutogeneratedJumpPointsInHyper().clear();
        }
        system.getMemoryWithoutUpdate().unset(
                HYPERSPACE_LINK_GENERATED_KEY);

        SectorEntityToken anchor = system.getHyperspaceAnchor();
        if (anchor != null && anchor.getContainingLocation() != null) {
            anchor.getContainingLocation().removeEntity(anchor);
        }
        system.setHyperspaceAnchor(null);
    }

    /**
     * Keeps the shell rupture visible before it stabilizes into a jump point.
     * The POI and its post-victory jump share an id, name, orbit, and
     * description so the transition reads as a change of state rather than a
     * second object appearing nearby.
     */
    private static void ensureHeavensScar(
            StarSystemAPI system, PlanetAPI star) {
        SectorEntityToken scar = system.getEntityById(HYPERSPACE_JUMP_ID);
        if (scar instanceof JumpPointAPI) {
            system.removeEntity(scar);
            scar = null;
        }
        if (scar == null) {
            scar = system.addCustomEntity(
                    HYPERSPACE_JUMP_ID,
                    HEAVENS_SCAR_NAME,
                    HEAVENS_SCAR_TYPE,
                    Factions.NEUTRAL);
        }
        if (scar == null) return;

        scar.setName(HEAVENS_SCAR_NAME);
        scar.setCustomDescriptionId(HEAVENS_SCAR_TYPE);
        scar.setCircularOrbit(star, 215f, 1250f, 100000f);
        scar.setDiscoverable(null);
        scar.setSensorProfile(null);
        scar.addTag(Tags.STORY_CRITICAL);
        scar.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
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
        // Null is Starsector's post-discovery state. A literal false combined
        // with a sensor profile remains an unidentified sensor contact and
        // disappears again outside detection range.
        ring.setDiscoverable(null);
        // The system has no conventional exit. Keep the Eden-side ring usable
        // even in legacy saves or console-driven test sessions that arrived
        // before the quest reveal, so the player can never be stranded here.
        ring.setSensorProfile(null);
        ring.addTag(Tags.GATE);
        ring.addTag(Tags.STORY_CRITICAL);
        ring.addTag(Tags.HAS_INTERACTION_DIALOG);
        ring.removeTag(Tags.NON_CLICKABLE);
        ring.removeTag(Tags.NO_ENTITY_TOOLTIP);
        ring.addTag(Tags.NOT_RANDOM_MISSION_TARGET);
    }

    private static void removeLegacyBand(StarSystemAPI system, String id) {
        SectorEntityToken band = system.getEntityById(id);
        if (band != null) {
            system.removeEntity(band);
        }
    }

}
