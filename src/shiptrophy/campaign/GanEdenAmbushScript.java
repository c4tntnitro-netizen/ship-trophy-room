package shiptrophy.campaign;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BaseSalvageSpecial;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.hullmods.WhiteRemnantEscort;

/**
 * Ensures Gan Eden's golden Omega fleet is parked beside the transit ring.
 * The descendants are created by GoldenFractalCascade during combat.
 */
public final class GanEdenAmbushScript implements EveryFrameScript {
    public static final String FLEET_KEY =
            "$shipTrophyGanEdenGoldenAmbushFleet";
    private static final String LOOT_KEY =
            "$shipTrophyGanEdenGoldenLootConfigured";
    private static final String ACTIVE_KEY =
            "$shipTrophyGanEdenGoldenAmbushActive";
    private static final String DEFEATED_KEY =
            "$shipTrophyGanEdenGoldenAmbushDefeated";
    private static final String PARTIAL_SINCE_KEY =
            "$shipTrophyGanEdenGoldenAmbushPartialSince";
    private static final String RESPAWN_SINCE_KEY =
            "$shipTrophyGanEdenGoldenRespawnSince";
    private static final String RESPAWN_WAVE_KEY =
            "$shipTrophyGanEdenGoldenRespawnWave";
    private static final String ESCORT_WAVE_KEY =
            "$shipTrophyGanEdenGoldenEscortWave";

    private static final String SINISTRAL_VARIANT =
            "ship_trophy_golden_shard_left_Attack";
    private static final String DEXTRAL_VARIANT =
            "ship_trophy_golden_shard_right_Attack";
    private static final String SINISTRAL_NAME = "Cherubim";
    private static final String DEXTRAL_NAME = "Lahat Haharev";
    private static final float RING_OFFSET = 350f;
    private static final float GUARD_DURATION = 1000000f;
    private static final float RESPAWN_DAYS = 90f;
    private static final int[] ESCORT_COMBAT_POINTS =
            new int[] {0, 45, 90, 150, 240};
    private static final long LOOT_SEED = 0x617572656174654cL;

    private float checkInterval;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null) return;
        checkInterval += amount;
        if (checkInterval < 1f) return;
        checkInterval = 0f;
        maintainFleet();
    }

    /**
     * Adds the fleet directly to Gan Eden instead of using spawnFleet().
     * The latter broadcasts a spawn event which some fleet-limiter mods
     * intercept, potentially deleting this deliberately hand-authored fleet.
     */
    public static boolean ensureFleet() {
        if (Global.getSector() == null) return false;
        StarSystemAPI system = GanEdenGenerator.findSystem();
        if (system == null) return false;
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();

        CampaignFleetAPI existing = findExistingFleet(system);
        SectorEntityToken anchor = system.getEntityById(
                GanEdenGenerator.ARRIVAL_RING_ID);
        if (anchor == null) anchor = system.getCenter();

        if (existing != null && !existing.isEmpty()) {
            configureGuard(existing, anchor);
            sectorMemory.set(ACTIVE_KEY, true);
            GanEdenGenerator.updateAureateSiegeConditions(system);
            logPlacement("reused", existing);
            return true;
        }
        if (existing != null) {
            system.removeEntity(existing);
        }
        if (sectorMemory.getBoolean(ACTIVE_KEY)) {
            recordWaveDefeat(system, sectorMemory);
            return true;
        }

        if (!GanEdenQuestManager.isAtLeast(
                GanEdenQuestManager.Stage.GAN_EDEN_REVEALED)) {
            return true;
        }

        int escortWave = 0;
        if (sectorMemory.getBoolean(DEFEATED_KEY)) {
            if (!sectorMemory.contains(RESPAWN_SINCE_KEY)) {
                sectorMemory.set(
                        RESPAWN_SINCE_KEY,
                        Global.getSector().getClock().getTimestamp());
                return true;
            }
            long since = sectorMemory.getLong(RESPAWN_SINCE_KEY);
            if (Global.getSector().getClock().getElapsedDaysSince(since)
                    < RESPAWN_DAYS) {
                return true;
            }
            escortWave = Math.min(
                    ESCORT_COMBAT_POINTS.length - 1,
                    sectorMemory.getInt(RESPAWN_WAVE_KEY) + 1);
        }

        CampaignFleetAPI fleet = createFleet(escortWave);
        if (fleet == null
                || fleet.isEmpty()
                || fleet.getFleetData().getNumMembers() < 2) {
            System.err.println(
                    "Hall of Triumph: "
                            + "Unable to create Gan Eden golden Omega fleet "
                            + "with both guardian ships.");
            return false;
        }

        system.addEntity(fleet);
        fleet.setLocation(
                anchor.getLocation().x + RING_OFFSET,
                anchor.getLocation().y);
        configureGuard(fleet, anchor);
        sectorMemory.set(ACTIVE_KEY, true);
        sectorMemory.unset(PARTIAL_SINCE_KEY);
        sectorMemory.unset(RESPAWN_SINCE_KEY);
        sectorMemory.set(RESPAWN_WAVE_KEY, escortWave);
        GanEdenGenerator.updateAureateSiegeConditions(system);
        logPlacement("placed", fleet);
        return true;
    }

    /**
     * A lone surviving named Shard recreates its counterpart. Destroying both
     * named guardians ends the current wave even if white escorts survive.
     */
    private static void maintainFleet() {
        StarSystemAPI system = GanEdenGenerator.findSystem();
        if (system == null) return;

        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        CampaignFleetAPI fleet = findExistingFleet(system);
        if (fleet == null || fleet.isEmpty()) {
            if (memory.getBoolean(ACTIVE_KEY)) {
                recordWaveDefeat(system, memory);
            } else {
                ensureFleet();
            }
            return;
        }

        boolean hasSinistral = hasGuardian(fleet, SINISTRAL_VARIANT);
        boolean hasDextral = hasGuardian(fleet, DEXTRAL_VARIANT);
        if (!hasSinistral && !hasDextral) {
            system.removeEntity(fleet);
            recordWaveDefeat(system, memory);
            return;
        }

        if (!hasSinistral || !hasDextral) {
            Random random = new Random(
                    0x726567656e657261L
                            ^ Global.getSector().getClock().getTimestamp());
            addNamedGuardian(fleet, !hasSinistral, random);
            fleet.getFleetData().sort();
            fleet.forceSync();
            SectorEntityToken anchor = system.getEntityById(
                    GanEdenGenerator.ARRIVAL_RING_ID);
            if (anchor == null) anchor = system.getCenter();
            configureGuard(fleet, anchor);
            logPlacement("regenerated missing guardian", fleet);
        }
    }

    private static boolean hasGuardian(
            CampaignFleetAPI fleet, String variantId) {
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member.getVariant() != null
                    && variantId.equals(
                            member.getVariant().getHullVariantId())) {
                return true;
            }
        }
        return false;
    }

    private static void recordWaveDefeat(
            StarSystemAPI system, MemoryAPI memory) {
        memory.set(DEFEATED_KEY, true);
        memory.unset(ACTIVE_KEY);
        memory.unset(PARTIAL_SINCE_KEY);
        memory.set(
                RESPAWN_SINCE_KEY,
                Global.getSector().getClock().getTimestamp());
        GanEdenGenerator.updateAureateSiegeConditions(system);
        // The first complete victory permanently opens Gan Eden to ordinary
        // hyperspace and releases its surface markets into the Sector economy.
        GanEdenGenerator.ensureGenerated();
    }

    private static CampaignFleetAPI findExistingFleet(
            StarSystemAPI system) {
        for (CampaignFleetAPI fleet : system.getFleets()) {
            if (fleet != null
                    && fleet.getMemoryWithoutUpdate()
                            .getBoolean(FLEET_KEY)) {
                return fleet;
            }
        }
        return null;
    }

    private static CampaignFleetAPI createFleet(int escortWave) {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(
                Factions.OMEGA, "Aureate Refractions", true);
        if (fleet == null) return null;

        Random random = new Random(
                0x6a616e6564656eL ^ (escortWave * 0x9e3779b9L));
        FleetMemberAPI sinistral = addNamedGuardian(fleet, true, random);
        FleetMemberAPI dextral = addNamedGuardian(fleet, false, random);
        if (sinistral == null || dextral == null) return fleet;
        addWhiteRemnantEscorts(fleet, escortWave, random);
        if (sinistral.getCaptain() != null) {
            fleet.setCommander(sinistral.getCaptain());
        }
        fleet.getFleetData().setFlagship(sinistral);
        fleet.getFleetData().sort();
        fleet.forceSync();
        fleet.setName("Aureate Refractions");
        fleet.setNoFactionInName(true);
        fleet.setNoAutoDespawn(true);
        fleet.getMemoryWithoutUpdate().set(ESCORT_WAVE_KEY, escortWave);
        return fleet;
    }

    private static void addWhiteRemnantEscorts(
            CampaignFleetAPI destination,
            int escortWave,
            Random random) {
        if (destination == null
                || escortWave <= 0
                || escortWave >= ESCORT_COMBAT_POINTS.length) {
            return;
        }

        FleetParamsV3 params = new FleetParamsV3(
                null,
                Factions.REMNANTS,
                2f,
                FleetTypes.PATROL_LARGE,
                ESCORT_COMBAT_POINTS[escortWave],
                0f,
                0f,
                0f,
                0f,
                0f,
                0f);
        params.averageSMods = escortWave >= 4 ? 2 : 1;
        params.ignoreMarketFleetSizeMult = true;
        params.withOfficers = true;
        params.random = random;

        CampaignFleetAPI support = FleetFactoryV3.createFleet(params);
        if (support == null) return;
        for (FleetMemberAPI member
                : support.getFleetData().getMembersListCopy()) {
            if (member == null || member.isFighterWing()) continue;
            member.setFlagship(false);
            applyWhiteEscortHullmod(member);
            readyMember(member);
            destination.getFleetData().addFleetMember(member);
        }
    }

    private static void applyWhiteEscortHullmod(FleetMemberAPI member) {
        if (member == null || member.getVariant() == null) return;
        ShipVariantAPI variant = member.getVariant().clone();
        variant.setSource(VariantSource.REFIT);
        String whiteHullId = WhiteRemnantEscort.getWhiteHullId(
                variant.getHullSpec().getBaseHullId());
        if (whiteHullId != null
                && Global.getSettings().getHullSpec(whiteHullId) != null) {
            variant.setHullSpecAPI(
                    Global.getSettings().getHullSpec(whiteHullId));
        }
        variant.addPermaMod(WhiteRemnantEscort.HULLMOD_ID);
        member.setVariant(variant, false, false);
    }

    /** Adds one of the two named Golden Shards to a Gan Eden wave. */
    public static FleetMemberAPI addNamedGuardian(
            CampaignFleetAPI fleet, boolean sinistral, Random random) {
        if (fleet == null) return null;
        FleetMemberAPI member = fleet.getFleetData().addFleetMember(
                sinistral ? SINISTRAL_VARIANT : DEXTRAL_VARIANT);
        if (member == null) return null;
        member.setShipName(sinistral ? SINISTRAL_NAME : DEXTRAL_NAME);
        PersonAPI core = createOmegaCore(random == null
                ? new Random() : random);
        if (core != null) member.setCaptain(core);
        readyMember(member);
        return member;
    }

    /** Adds an ordinary, non-aureate Omega escort to a Remnant Ordo. */
    public static FleetMemberAPI addRegularOmegaEscort(
            CampaignFleetAPI fleet, String variantId, Random random) {
        if (fleet == null || variantId == null) return null;
        FleetMemberAPI member = fleet.getFleetData().addFleetMember(variantId);
        if (member == null) return null;
        PersonAPI core = createOmegaCore(random == null
                ? new Random() : random);
        if (core != null) member.setCaptain(core);
        readyMember(member);
        return member;
    }

    public static boolean isDefeated() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(DEFEATED_KEY);
    }

    /** True while the current Golden Shard wave is physically present. */
    public static boolean isEncounterActive() {
        if (Global.getSector() == null) return false;
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        return memory.getBoolean(ACTIVE_KEY);
    }

    private static PersonAPI createOmegaCore(Random random) {
        if (Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE) == null) {
            return null;
        }
        return Misc.getAICoreOfficerPlugin(Commodities.OMEGA_CORE)
                .createPerson(Commodities.OMEGA_CORE, Factions.OMEGA, random);
    }

    private static void readyMember(FleetMemberAPI member) {
        member.getRepairTracker().setMothballed(false);
        member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
        member.updateStats();
    }

    private static void configureGuard(
            CampaignFleetAPI fleet,
            SectorEntityToken anchor) {
        ensureGuardianNames(fleet);
        ensureWhiteEscortSkins(fleet);

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(FLEET_KEY, true);
        boolean dormantForQuest = !isDefeated()
                && GanEdenQuestManager.isAtLeast(
                GanEdenQuestManager.Stage.GAN_EDEN_REVEALED)
                && !GanEdenQuestManager.isEpitaphFound();
        if (dormantForQuest) {
            memory.unset(MemFlags.MEMORY_KEY_MAKE_HOSTILE);
            memory.unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
            memory.set(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE, true);
            memory.set(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE, true);
        } else {
            memory.unset(MemFlags.MEMORY_KEY_MAKE_NON_HOSTILE);
            memory.unset(MemFlags.MEMORY_KEY_MAKE_NON_AGGRESSIVE);
            memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
            memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        }
        memory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        memory.set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
        memory.set(
                MemFlags.MEMORY_KEY_NO_SHIP_DERELICTS_IN_POST_BATTLE_DEBRIS,
                true);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);
        fleet.getStats().getSensorProfileMod().modifyFlat(
                FLEET_KEY, 2000f, "Aureate guardian signature");
        ensureReward(fleet, memory);

        fleet.clearAssignments();
        fleet.addAssignment(
                FleetAssignment.ORBIT_PASSIVE,
                anchor,
                GUARD_DURATION,
                "waiting in silence");
    }

    /** Migrates escorts spawned by builds which only applied a runtime tint. */
    private static void ensureWhiteEscortSkins(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member == null
                    || member.getVariant() == null
                    || !member.getVariant().hasHullMod(
                            WhiteRemnantEscort.HULLMOD_ID)) {
                continue;
            }
            String whiteHullId = WhiteRemnantEscort.getWhiteHullId(
                    member.getVariant().getHullSpec().getBaseHullId());
            if (whiteHullId == null
                    || whiteHullId.equals(
                            member.getVariant().getHullSpec().getHullId())) {
                continue;
            }
            applyWhiteEscortHullmod(member);
        }
        fleet.forceSync();
    }

    private static void ensureGuardianNames(CampaignFleetAPI fleet) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            String variantId = member.getVariant().getHullVariantId();
            if (SINISTRAL_VARIANT.equals(variantId)) {
                member.setShipName(SINISTRAL_NAME);
            } else if (DEXTRAL_VARIANT.equals(variantId)) {
                member.setShipName(DEXTRAL_NAME);
            }
        }
    }

    private static void ensureReward(
            CampaignFleetAPI fleet,
            MemoryAPI memory) {
        if (memory.getBoolean(LOOT_KEY)) return;

        List<DropData> randomDrops = new ArrayList<DropData>();
        randomDrops.add(createDrop("omega_weapons_large", 4, 4));
        randomDrops.add(createDrop("omega_weapons_medium", 4, 8));
        randomDrops.add(createDrop("omega_weapons_small", 12, 16));

        CargoAPI reward = SalvageEntity.generateSalvage(
                new Random(LOOT_SEED),
                1f,
                1f,
                1f,
                1f,
                new ArrayList<DropData>(),
                randomDrops);
        BaseSalvageSpecial.addExtraSalvage(fleet, reward);
        memory.set(LOOT_KEY, true);
    }

    private static DropData createDrop(
            String group,
            int minimum,
            int maximum) {
        DropData drop = new DropData();
        drop.group = group;
        drop.chances = minimum;
        drop.maxChances = maximum;
        return drop;
    }

    private static void logPlacement(
            String action,
            CampaignFleetAPI fleet) {
        System.out.println(
                "Hall of Triumph: Gan Eden golden Omega fleet " + action
                        + ": members="
                        + fleet.getFleetData().getNumMembers()
                        + ", location=("
                        + fleet.getLocation().x + ", "
                        + fleet.getLocation().y + ")");
    }
}
