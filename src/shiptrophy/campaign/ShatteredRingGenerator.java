package shiptrophy.campaign;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource;
import com.fs.starfarer.api.util.Misc;

/**
 * Adds the Shattered Ring to the fixed Penelope's Star system.
 *
 * The generator is deliberately idempotent so installing the mod into an
 * existing campaign works, while conquered markets and salvaged wrecks are not
 * reset on later loads.
 */
public final class ShatteredRingGenerator {
    public static final String ENTITY_ID = "ship_trophy_shattered_ring";
    public static final String ENTITY_TYPE = "ship_trophy_shattered_ring";
    public static final String MARKET_ID = ENTITY_ID;
    public static final String MARKET_NAME = "The Shattered Ring";
    public static final String POD_COMMUNITY = "ship_trophy_pod_community";
    public static final String WRECK_FARMS = "ship_trophy_wreck_farms";

    private static final String SYSTEM_NAME = "Penelope's Star";
    private static final String SYSTEM_ID = "penelope";
    private static final String ENVIRONMENT_DONE = "$shipTrophyShatteredRingEnvironmentGenerated";
    private static final String DEBRIS_ID = "ship_trophy_shattered_ring_debris";
    private static final String BELT_ID = "ship_trophy_shattered_ring_fragments";
    private static final String WRECK_PREFIX = "ship_trophy_shattered_ring_wreck_";
    private static final float ORBIT_RADIUS = 8000f;

    private static final String[] WRECK_VARIANTS = {
            "buffalo2_FS",
            "dram_Light",
            "phaeton_Standard",
            "shepherd_Frontier",
            "mudskipper_Standard"
    };

    private ShatteredRingGenerator() {
    }

    public static void ensureGenerated() {
        if (Global.getSector() == null || Global.getSector().getEconomy() == null) return;

        try {
            StarSystemAPI system = findPenelope();
            if (system == null) {
                return;
            }

            SectorEntityToken ring = Global.getSector().getEntityById(ENTITY_ID);
            if (ring != null && ring.getContainingLocation() != system) {
                return;
            }

            if (ring == null) {
                ring = createRing(system);
            }
            if (ring == null) return;

            MarketAPI market = Global.getSector().getEconomy().getMarket(MARKET_ID);
            if (market == null) {
                createMarket(ring);
            } else {
                if (ring.getMarket() == null) ring.setMarket(market);
                if (market.getPrimaryEntity() == null) market.setPrimaryEntity(ring);
            }

            ensureEnvironment(system, ring);
        } catch (RuntimeException ex) {
            // Do not prevent a campaign from loading if another sector-generation
            // mod has radically changed Penelope's Star.
            System.err.println("Hall of Triumph: failed to generate the Shattered Ring.");
            ex.printStackTrace(System.err);
        }
    }

    private static StarSystemAPI findPenelope() {
        StarSystemAPI direct = Global.getSector().getStarSystem(SYSTEM_NAME);
        if (direct != null) return direct;

        direct = Global.getSector().getStarSystem(SYSTEM_ID);
        if (direct != null) return direct;

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (SYSTEM_ID.equalsIgnoreCase(system.getId())
                    || SYSTEM_NAME.equalsIgnoreCase(system.getName())
                    || SYSTEM_NAME.equalsIgnoreCase(system.getBaseName())) {
                return system;
            }
        }
        return null;
    }

    private static SectorEntityToken createRing(StarSystemAPI system) {
        CustomCampaignEntityAPI ring = system.addCustomEntity(
                ENTITY_ID, MARKET_NAME, ENTITY_TYPE, Factions.INDEPENDENT);
        if (ring == null) {
            return null;
        }

        SectorEntityToken focus = system.getStar() != null ? system.getStar() : system.getCenter();
        ring.setCircularOrbit(focus, 315f, ORBIT_RADIUS, 420f);
        ring.setCustomDescriptionId(ENTITY_TYPE);
        ring.setInteractionImage("illustrations", "space_wreckage");
        ring.setSensorProfile(1f);
        ring.setDiscoverable(false);
        ring.addTag(Tags.STATION);

        system.addTag(Tags.THEME_INTERESTING);
        system.addTag(Tags.THEME_DERELICT);
        return ring;
    }

    private static void createMarket(SectorEntityToken ring) {
        MarketAPI market = Global.getFactory().createMarket(MARKET_ID, MARKET_NAME, 4);
        market.setFactionId(Factions.INDEPENDENT);
        market.setPrimaryEntity(ring);
        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.setFreePort(true);

        market.addCondition(Conditions.OUTPOST);
        market.addCondition(Conditions.POPULATION_4);
        market.addCondition(Conditions.FRONTIER);
        market.addCondition("shipbreaking_center");
        market.addCondition(POD_COMMUNITY);

        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.SPACEPORT);
        market.addIndustry(WRECK_FARMS);
        market.addIndustry(Industries.COMMERCE);
        market.addIndustry(Industries.PATROLHQ);
        market.addIndustry(Industries.GROUNDDEFENSES);
        market.addIndustry(Industries.ORBITALSTATION);
        market.addIndustry(Industries.WAYSTATION);

        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);

        market.getTariff().modifyFlat("default_tariff", market.getFaction().getTariffFraction());
        market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
        market.getMemoryWithoutUpdate().set("$shipTrophyIsaBirthplace", true);

        ring.setMarket(market);
        ring.setFaction(Factions.INDEPENDENT);
        market.reapplyConditions();
        market.reapplyIndustries();
        Global.getSector().getEconomy().addMarket(market, true);

        addMarketPeople(market);
    }

    private static void addMarketPeople(MarketAPI market) {
        FactionAPI independents = Global.getSector().getFaction(Factions.INDEPENDENT);
        if (independents == null) return;

        long seed = (Global.getSector().getSeedString() + MARKET_ID).hashCode();
        Random random = new Random(seed);

        PersonAPI administrator = createPerson(independents, random,
                "ship_trophy_ring_administrator", Ranks.CITIZEN, Ranks.POST_ADMINISTRATOR,
                PersonImportance.HIGH);
        market.setAdmin(administrator);
        addPerson(market, administrator);

        PersonAPI portmaster = createPerson(independents, random,
                "ship_trophy_ring_portmaster", Ranks.SPACE_CAPTAIN, Ranks.POST_PORTMASTER,
                PersonImportance.MEDIUM);
        addPerson(market, portmaster);

        PersonAPI supplier = createPerson(independents, random,
                "ship_trophy_ring_supplier", Ranks.SPACE_LIEUTENANT, Ranks.POST_SUPPLY_OFFICER,
                PersonImportance.MEDIUM);
        addPerson(market, supplier);
    }

    private static PersonAPI createPerson(FactionAPI faction, Random random, String id,
            String rank, String post, PersonImportance importance) {
        PersonAPI person = faction.createRandomPerson(random);
        person.setId(id);
        person.setRankId(rank);
        person.setPostId(post);
        person.setImportanceAndVoice(importance, random);
        return person;
    }

    private static void addPerson(MarketAPI market, PersonAPI person) {
        person.setMarket(market);
        market.addPerson(person);
        market.getCommDirectory().addPerson(person);
    }

    private static void ensureEnvironment(StarSystemAPI system, SectorEntityToken ring) {
        if (system.getMemoryWithoutUpdate().getBoolean(ENVIRONMENT_DONE)) return;

        if (system.getEntityById(BELT_ID) == null) {
            SectorEntityToken fragments = system.addAsteroidBelt(
                    ring, 56, 650f, 260f, 65f, 110f,
                    Terrain.ASTEROID_BELT, "Shattered Ring Fragments");
            if (fragments != null) fragments.setId(BELT_ID);
        }

        if (system.getEntityById(DEBRIS_ID) == null) {
            DebrisFieldParams params = new DebrisFieldParams(900f, 1.25f, 10000000f, 0f);
            params.source = DebrisFieldSource.MIXED;
            params.baseDensity = 0.8f;
            params.baseSalvageXP = 250;

            SectorEntityToken debris = Misc.addDebrisField(system, params, null);
            if (debris != null) {
                debris.setId(DEBRIS_ID);
                debris.setName("The Suitors");
                debris.setFaction(Factions.NEUTRAL);
                debris.setDiscoverable(false);
                debris.setOrbit(ring.getOrbit().makeCopy());
            }
        }

        float[] angles = {25f, 102f, 176f, 247f, 318f};
        float[] radii = {260f, 390f, 520f, 330f, 470f};
        for (int i = 0; i < WRECK_VARIANTS.length; i++) {
            String id = WRECK_PREFIX + i;
            if (system.getEntityById(id) != null) continue;

            PerShipData ship = new PerShipData(WRECK_VARIANTS[i], ShipCondition.WRECKED);
            ship.addDmods = true;
            ship.pruneWeapons = true;

            DerelictShipData data = new DerelictShipData(ship, false);
            SectorEntityToken wreck = BaseThemeGenerator.addSalvageEntity(
                    system, Entities.WRECK, Factions.NEUTRAL, data);
            if (wreck == null) continue;

            wreck.setId(id);
            wreck.setName("Claimed Derelict");
            wreck.setDiscoverable(false);
            wreck.addTag(Tags.UNRECOVERABLE);
            wreck.getMemoryWithoutUpdate().set("$shipTrophyWreckFarmClaim", true);
            wreck.setCircularOrbit(ring, angles[i], radii[i], 50f + (i * 9f));
        }

        system.getMemoryWithoutUpdate().set(ENVIRONMENT_DONE, true);
    }
}
