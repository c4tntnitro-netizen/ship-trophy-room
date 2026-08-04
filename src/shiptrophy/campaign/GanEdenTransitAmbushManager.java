package shiptrophy.campaign;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

/** Owns the one-time Ivory Remnant interception at the Power Transit Gate. */
public final class GanEdenTransitAmbushManager {
    public static final String FLEET_KEY =
            "$shipTrophyGanEdenTransitIvoryFleet";

    private static final String SPAWNED_KEY =
            "$shipTrophyGanEdenTransitIvorySpawned";
    private static final String DEFEATED_KEY =
            "$shipTrophyGanEdenTransitIvoryDefeated";
    private static final String PURSUING_KEY =
            "$shipTrophyGanEdenTransitIvoryPursuing";
    private static final String PARKED_KEY =
            "$shipTrophyGanEdenTransitIvoryParked";
    private static final String FULL_ORDO_KEY =
            "$shipTrophyGanEdenTransitIvoryFullOrdoV1";
    private static final float COMBAT_POINTS = 360f;
    private static final float FULL_ORDO_MIN_FP = 330f;
    private static final float GATE_OFFSET = 950f;
    private static final float ASSIGNMENT_DURATION = 1000000f;

    private GanEdenTransitAmbushManager() {
    }

    public static void ensureEncounter() {
        if (Global.getSector() == null
                || !GanEdenQuestManager.isAtLeast(
                        GanEdenQuestManager.Stage.GAN_EDEN_REVEALED)) {
            return;
        }

        StarSystemAPI system = GanEdenTransitSystemGenerator.findSystem();
        if (system == null) return;
        MemoryAPI sectorMemory =
                Global.getSector().getMemoryWithoutUpdate();
        CampaignFleetAPI fleet = findFleet(system);

        if (sectorMemory.getBoolean(DEFEATED_KEY)) {
            if (fleet != null && fleet.getContainingLocation() != null) {
                fleet.getContainingLocation().removeEntity(fleet);
            }
            return;
        }

        if (fleet != null && fleet.getMemoryWithoutUpdate().getBoolean(
                MemFlags.MEMORY_KEY_RECENTLY_DEFEATED_BY_PLAYER)) {
            markDefeated(fleet);
            return;
        }

        if (fleet == null || fleet.isEmpty()) {
            if (sectorMemory.getBoolean(SPAWNED_KEY)) {
                sectorMemory.set(DEFEATED_KEY, true);
                return;
            }
            fleet = createFleet(system);
            if (fleet == null || fleet.isEmpty()) {
                System.err.println(
                        "Hall of Triumph: unable to create the Power Transit "
                                + "Ivory Remnant ambush.");
                return;
            }
            sectorMemory.set(SPAWNED_KEY, true);
        }

        ensureFullOrdoStrength(fleet);
        configureFleet(fleet, system);
    }

    private static CampaignFleetAPI createFleet(StarSystemAPI system) {
        CampaignFleetAPI fleet = createRemnantFleet(
                COMBAT_POINTS, 0x69766f7279676174L);
        if (fleet == null || fleet.isEmpty()) return null;
        IvoryRemnantFleetSupport.refitFleet(fleet);

        SectorEntityToken gate = system.getEntityById(
                GanEdenQuestManager.EXTERNAL_RING_ID);
        if (gate == null) gate = system.getCenter();
        system.addEntity(fleet);
        fleet.setLocation(
                gate.getLocation().x + GATE_OFFSET,
                gate.getLocation().y);
        return fleet;
    }

    private static CampaignFleetAPI createRemnantFleet(
            float combatPoints, long seed) {
        Random random = new Random(seed);
        FleetParamsV3 params = new FleetParamsV3(
                null,
                Factions.REMNANTS,
                2f,
                FleetTypes.PATROL_LARGE,
                combatPoints,
                0f,
                0f,
                0f,
                0f,
                0f,
                0f);
        params.ignoreMarketFleetSizeMult = true;
        params.withOfficers = true;
        params.averageSMods = 1;
        params.maxNumShips = 30;
        params.random = random;

        return FleetFactoryV3.createFleet(params);
    }

    /** Upgrades the older 180-FP encounter once without healing later losses. */
    private static void ensureFullOrdoStrength(CampaignFleetAPI fleet) {
        if (fleet == null || Global.getSector() == null) return;
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
        if (sectorMemory.getBoolean(FULL_ORDO_KEY)) return;

        float current = fleet.getFleetData().getFleetPointsUsed();
        if (current < FULL_ORDO_MIN_FP) {
            float reinforcements = Math.max(40f, COMBAT_POINTS - current);
            CampaignFleetAPI donor = createRemnantFleet(
                    reinforcements, 0x69766f727966756cL);
            if (donor == null || donor.isEmpty()) return;

            for (OfficerDataAPI officer
                    : donor.getFleetData().getOfficersCopy()) {
                fleet.getFleetData().addOfficer(officer);
            }
            for (FleetMemberAPI member
                    : donor.getFleetData().getMembersListCopy()) {
                donor.getFleetData().removeFleetMember(member);
                fleet.getFleetData().addFleetMember(member);
            }
            fleet.getFleetData().sort();
            fleet.forceSync();
            IvoryRemnantFleetSupport.refitFleet(fleet);
        }

        if (fleet.getFleetData().getFleetPointsUsed()
                >= FULL_ORDO_MIN_FP) {
            sectorMemory.set(FULL_ORDO_KEY, true);
        }
    }

    private static void configureFleet(
            CampaignFleetAPI fleet, StarSystemAPI system) {
        if (fleet == null) return;
        IvoryRemnantFleetSupport.refitFleet(fleet);
        fleet.setName("Ivory Custodian Ordo");
        fleet.setNoFactionInName(true);
        fleet.setNoAutoDespawn(true);
        fleet.addTag(Tags.STORY_CRITICAL);
        ensureBattleListener(fleet);

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(FLEET_KEY, true);
        memory.set(MemFlags.STORY_CRITICAL, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        memory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        memory.set(MemFlags.MEMORY_KEY_NO_JUMP, true);
        memory.unset(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY);
        memory.unset(
                MemFlags.MEMORY_KEY_NO_SHIP_DERELICTS_IN_POST_BATTLE_DEBRIS);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true);
        memory.set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        boolean playerHere = player != null
                && player.getContainingLocation() == system;
        boolean pursuing = memory.getBoolean(PURSUING_KEY);
        if (playerHere && !pursuing) {
            memory.set(PURSUING_KEY, true);
            memory.unset(PARKED_KEY);
            fleet.clearAssignments();
            fleet.addAssignment(
                    FleetAssignment.INTERCEPT,
                    player,
                    ASSIGNMENT_DURATION,
                    "closing the gate behind you");
        } else if (!playerHere && pursuing) {
            memory.unset(PURSUING_KEY);
            orbitGate(fleet, system);
        } else if (!playerHere
                && !pursuing
                && !memory.getBoolean(PARKED_KEY)) {
            orbitGate(fleet, system);
        }
    }

    private static void orbitGate(
            CampaignFleetAPI fleet, StarSystemAPI system) {
        SectorEntityToken gate = system.getEntityById(
                GanEdenQuestManager.EXTERNAL_RING_ID);
        if (gate == null) gate = system.getCenter();
        fleet.clearAssignments();
        fleet.addAssignment(
                FleetAssignment.ORBIT_PASSIVE,
                gate,
                ASSIGNMENT_DURATION,
                "waiting beyond the active aperture");
        fleet.getMemoryWithoutUpdate().set(PARKED_KEY, true);
    }

    private static CampaignFleetAPI findFleet(StarSystemAPI system) {
        for (CampaignFleetAPI fleet : system.getFleets()) {
            if (fleet != null && fleet.getMemoryWithoutUpdate()
                    .getBoolean(FLEET_KEY)) {
                return fleet;
            }
        }
        return null;
    }

    private static void ensureBattleListener(CampaignFleetAPI fleet) {
        for (FleetEventListener listener : fleet.getEventListeners()) {
            if (listener instanceof IvoryBattleListener) return;
        }
        fleet.addEventListener(new IvoryBattleListener());
    }

    private static void markDefeated(CampaignFleetAPI fleet) {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(DEFEATED_KEY, true);
        if (fleet != null) fleet.setNoAutoDespawn(false);
    }

    /** Records a player victory without suppressing normal ship recovery. */
    public static final class IvoryBattleListener
            implements FleetEventListener {
        @Override
        public void reportBattleOccurred(
                CampaignFleetAPI fleet,
                CampaignFleetAPI primaryWinner,
                BattleAPI battle) {
            if (fleet == null
                    || primaryWinner == null
                    || battle == null
                    || !fleet.getMemoryWithoutUpdate().getBoolean(FLEET_KEY)
                    || !battle.isPlayerInvolved()
                    || !battle.isInvolved(fleet)
                    || battle.onPlayerSide(fleet)
                    || !battle.onPlayerSide(primaryWinner)) {
                return;
            }
            markDefeated(fleet);
        }

        @Override
        public void reportFleetDespawnedToListener(
                CampaignFleetAPI fleet,
                CampaignEventListener.FleetDespawnReason reason,
                Object param) {
            if (fleet != null) fleet.removeEventListener(this);
        }
    }
}
