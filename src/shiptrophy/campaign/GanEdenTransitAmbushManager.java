package shiptrophy.campaign;

import shiptrophy.ShipTrophyL10n;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import com.fs.starfarer.api.util.Misc;

/** Owns the one-time Ivory Remnant interception at the Power Transit Gate. */
public final class GanEdenTransitAmbushManager {
    public static final String FLEET_KEY =
            "$shipTrophyGanEdenTransitIvoryFleet";

    private static final String SPAWNED_KEY =
            "$shipTrophyGanEdenTransitIvorySpawned";
    private static final String DEFEATED_KEY =
            "$shipTrophyGanEdenTransitIvoryDefeated";
    private static final String DEFEATED_CLEANUP_KEY =
            "$shipTrophyGanEdenTransitIvoryDefeatedCleanupV2";
    private static final String PURSUING_KEY =
            "$shipTrophyGanEdenTransitIvoryPursuing";
    private static final String PARKED_KEY =
            "$shipTrophyGanEdenTransitIvoryParked";
    private static final String LEGACY_FULL_ORDO_KEY =
            "$shipTrophyGanEdenTransitIvoryFullOrdoV1";
    private static final String PREVIOUS_FULL_ORDO_KEY =
            "$shipTrophyGanEdenTransitIvoryFullOrdoV2";
    private static final String FULL_ORDO_KEY =
            "$shipTrophyGanEdenTransitIvoryFullOrdoV3";
    private static final String IVORY_REFIT_COMPLETE_KEY =
            "$shipTrophyGanEdenTransitIvoryRefitV4";
    private static final String FLEET_ID_KEY =
            "$shipTrophyGanEdenTransitIvoryFleetIdV1";
    private static final String FID_CONFIG_GEN_KEY = "$fidConifgGen";
    private static final String CREATION_FAILED_SINCE_KEY =
            "$shipTrophyGanEdenTransitIvoryCreationFailedSinceV1";
    private static final String STRENGTH_RETRY_SINCE_KEY =
            "$shipTrophyGanEdenTransitIvoryStrengthRetrySinceV1";
    private static final String MISSING_SINCE_KEY =
            "$shipTrophyGanEdenTransitIvoryMissingSinceV1";
    private static final String FLEETS_RECONCILED_KEY =
            "$shipTrophyGanEdenTransitIvoryFleetsReconciledV2";
    private static final float COMBAT_POINTS = 360f;
    private static final float FULL_ORDO_MIN_FP = 330f;
    private static final int MAX_SHIPS = 30;
    private static final float CREATION_RETRY_DAYS = 1f;
    private static final String[] FALLBACK_VARIANTS = {
        "radiant_Assault", "radiant_Strike",
        "nova_Attack", "nova_Standard",
        "apex_Assault", "apex_Missile", "apex_Standard",
        "apex_Overdriven", "brilliant_Standard", "brilliant_Support",
    };
    private static final float GATE_OFFSET = 950f;
    private static final float ASSIGNMENT_DURATION = 1000000f;

    private GanEdenTransitAmbushManager() {
    }

    public static boolean isDefeated() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(DEFEATED_KEY);
    }

    public static void ensureEncounter() {
        if (Global.getSector() == null
                || !GanEdenQuestManager.isAtLeast(
                        GanEdenQuestManager.Stage.GAN_EDEN_REVEALED)) {
            return;
        }

        MemoryAPI sectorMemory =
                Global.getSector().getMemoryWithoutUpdate();
        GanEdenQuestManager.Stage stage = GanEdenQuestManager.getStage();
        // Saves already beyond first arrival predate this encounter. Resolve
        // any stale lifecycle state instead of introducing or retaining a
        // gate blockade after the player has already reached Gan Eden.
        if (stage.ordinal()
                        > GanEdenQuestManager.Stage.GAN_EDEN_REVEALED.ordinal()
                && !sectorMemory.getBoolean(DEFEATED_KEY)) {
            sectorMemory.set(DEFEATED_KEY, true);
            sectorMemory.unset(DEFEATED_CLEANUP_KEY);
        }
        // Once the defeated fleet has been removed there is nothing left to
        // maintain. Avoid locating the transit system and scanning its fleet
        // list on every quest-script interval for the rest of the campaign.
        if (sectorMemory.getBoolean(DEFEATED_KEY)
                && sectorMemory.getBoolean(DEFEATED_CLEANUP_KEY)) {
            return;
        }

        // Cleanup is global and must not depend on the transit system still
        // being resolvable. Detached battle fleets are retried after they
        // reattach instead of being silently orphaned.
        if (sectorMemory.getBoolean(DEFEATED_KEY)) {
            cleanupDefeatedEncounter(sectorMemory);
            return;
        }

        StarSystemAPI system = GanEdenTransitSystemGenerator.findSystem();
        if (system == null) return;
        CampaignFleetAPI fleet = null;
        if (!sectorMemory.getBoolean(FLEETS_RECONCILED_KEY)) {
            boolean previouslySpawned = sectorMemory.getBoolean(SPAWNED_KEY);
            CampaignFleetAPI recordedBeforeReconcile =
                    findRecordedFleet(sectorMemory);
            fleet = reconcileTaggedFleets(system, sectorMemory);
            sectorMemory.set(FLEETS_RECONCILED_KEY, true);
            if (fleet != null) {
                sectorMemory.set(SPAWNED_KEY, true);
            } else if (previouslySpawned
                    && recordedBeforeReconcile == null) {
                // This one-time load reconciliation is the safe point at
                // which an old SPAWNED flag plus no fleet anywhere proves
                // that the historical encounter is already gone.
                markDefeated(null);
                cleanupDefeatedEncounter(sectorMemory);
                return;
            }
        }

        List<CampaignFleetAPI> localFleets = findFleets(system);
        if (fleet == null) fleet = findRecordedFleet(sectorMemory);
        if (fleet == null && !localFleets.isEmpty()) {
            fleet = localFleets.get(0);
        }
        if (fleet != null && fleet.getId() != null) {
            // Persist the keeper before retiring duplicates so their OTHER
            // despawn callbacks cannot clear the encounter state.
            sectorMemory.set(FLEET_ID_KEY, fleet.getId());
            sectorMemory.set(SPAWNED_KEY, true);
        }
        // Old or interrupted initialization could leave duplicates. Keep one
        // encounter owner and retire the rest before they acquire listeners
        // or assignments of their own.
        for (CampaignFleetAPI duplicate : localFleets) {
            if (duplicate != fleet) despawnFleet(duplicate);
        }
        if (fleet != null && fleet.getBattle() != null) {
            sectorMemory.unset(MISSING_SINCE_KEY);
            return;
        }
        if (fleet != null && fleet.getContainingLocation() == null) {
            if (!missingGraceElapsed(sectorMemory)) return;
            resetForRespawn(sectorMemory);
            return;
        }
        if (fleet != null) sectorMemory.unset(MISSING_SINCE_KEY);
        if (fleet != null && fleet.getContainingLocation() != system) {
            // Battle construction can temporarily detach a fleet, and other
            // mods may relocate it. Never infer defeat while detached; when
            // it is safely idle in another location, restore the authored
            // encounter instead of leaving the Gate permanently blocked.
            fleet.getContainingLocation().removeEntity(fleet);
            system.addEntity(fleet);
            SectorEntityToken gate = system.getEntityById(
                    GanEdenQuestManager.EXTERNAL_RING_ID);
            if (gate == null) gate = system.getCenter();
            fleet.setLocation(
                    gate.getLocation().x + GATE_OFFSET,
                    gate.getLocation().y);
        }

        if (fleet != null && fleet.isEmpty()) {
            markDefeated(fleet);
            return;
        }
        if (fleet == null) {
            if (sectorMemory.getBoolean(SPAWNED_KEY)) {
                // A fleet can be detached from its location while a battle is
                // being built. Campaign time does not advance in combat, so
                // a short campaign-time grace avoids that race while still
                // repairing raw removals or stripped-listener despawns.
                if (!missingGraceElapsed(sectorMemory)) return;
                resetForRespawn(sectorMemory);
                // Re-run the one-time global reconciliation next tick before
                // creating anything, in case another mod merely relocated
                // the tagged fleet while stripping its recorded ID.
                return;
            }
            if (!creationRetryReady(sectorMemory)) return;
            fleet = createFleet(system);
            if (fleet == null || fleet.isEmpty()) {
                sectorMemory.set(
                        CREATION_FAILED_SINCE_KEY,
                        Global.getSector().getClock().getTimestamp());
                System.err.println(
                        "Hall of Triumph: unable to create the Power Transit "
                                + "Ivory Remnant ambush.");
                return;
            }
            sectorMemory.set(SPAWNED_KEY, true);
            sectorMemory.set(FLEETS_RECONCILED_KEY, true);
            sectorMemory.unset(CREATION_FAILED_SINCE_KEY);
            sectorMemory.unset(MISSING_SINCE_KEY);
        }
        if (fleet.getId() != null) {
            sectorMemory.set(FLEET_ID_KEY, fleet.getId());
        }

        if (!creationRetryReady(sectorMemory)) return;
        if (!prepareIvoryRoster(fleet, sectorMemory)) {
            sectorMemory.set(
                    CREATION_FAILED_SINCE_KEY,
                    Global.getSector().getClock().getTimestamp());
            System.err.println(
                    "Hall of Triumph: unable to prepare the Power Transit "
                            + "Ivory Remnant roster.");
            return;
        }
        sectorMemory.unset(CREATION_FAILED_SINCE_KEY);
        ensureFullOrdoStrength(fleet);
        configureFleet(fleet, system);
    }

    private static CampaignFleetAPI createFleet(StarSystemAPI system) {
        CampaignFleetAPI fleet = createRemnantFleet(
                COMBAT_POINTS, 0x69766f7279676174L);
        if (fleet == null
                || fleet.isEmpty()
                || !ensureIvoryRefit(fleet)
                || fleet.isEmpty()
                || fleet.getFleetData().getFleetPointsUsed()
                        < FULL_ORDO_MIN_FP) {
            fleet = createFallbackFleet(0x69766f727966616cL);
        }
        if (fleet == null || fleet.isEmpty()) return null;

        SectorEntityToken gate = system.getEntityById(
                GanEdenQuestManager.EXTERNAL_RING_ID);
        if (gate == null) gate = system.getCenter();
        system.addEntity(fleet);
        fleet.setLocation(
                gate.getLocation().x + GATE_OFFSET,
                gate.getLocation().y);
        return fleet;
    }

    /** Known-vanilla roster used when another mod empties the Remnant pool. */
    private static CampaignFleetAPI createFallbackFleet(long seed) {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(
                Factions.REMNANTS,
                ShipTrophyL10n.get("gan_eden_ivory_ordo"),
                true);
        if (fleet == null) return null;
        for (String variantId : FALLBACK_VARIANTS) {
            FleetMemberAPI member =
                    fleet.getFleetData().addFleetMember(variantId);
            if (member != null) member.setFlagship(false);
        }
        if (fleet.isEmpty()) return null;
        Random random = new Random(seed);
        RemnantSeededFleetManager.initRemnantFleetProperties(
                random, fleet, false);
        FleetFactoryV3.addCommanderAndOfficersV2(
                fleet, createRemnantParams(COMBAT_POINTS, random), random);
        if (!ensureIvoryRefit(fleet) || fleet.isEmpty()) return null;
        ensureRemnantCommander(fleet, random);
        fleet.getFleetData().sort();
        fleet.forceSync();
        return fleet;
    }

    private static CampaignFleetAPI createRemnantFleet(
            float combatPoints, long seed) {
        Random random = new Random(seed);
        FleetParamsV3 params = createRemnantParams(combatPoints, random);

        return FleetFactoryV3.createFleet(params);
    }

    private static FleetParamsV3 createRemnantParams(
            float combatPoints, Random random) {
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
        params.maxNumShips = MAX_SHIPS;
        params.random = random;
        return params;
    }

    /** Upgrades the older 180-FP encounter once without healing later losses. */
    private static void ensureFullOrdoStrength(CampaignFleetAPI fleet) {
        if (fleet == null || Global.getSector() == null) return;
        MemoryAPI sectorMemory = Global.getSector().getMemoryWithoutUpdate();
        if (sectorMemory.getBoolean(FULL_ORDO_KEY)) return;
        trimToFleetLimits(fleet);
        ensureRemnantCommander(fleet, new Random(0x69766f7279636f72L));

        // Older version keys mean this encounter was already brought to
        // authored strength. Preserve later battle losses, but apply the new
        // hard ship/FP limits once before considering migration complete.
        if (sectorMemory.getBoolean(LEGACY_FULL_ORDO_KEY)
                || sectorMemory.getBoolean(PREVIOUS_FULL_ORDO_KEY)) {
            sectorMemory.set(FULL_ORDO_KEY, true);
            return;
        }

        float current = fleet.getFleetData().getFleetPointsUsed();
        if (current < FULL_ORDO_MIN_FP) {
            if (!strengthRetryReady(sectorMemory)) return;
            float reinforcements = Math.max(40f, COMBAT_POINTS - current);
            CampaignFleetAPI donor = createRemnantFleet(
                    reinforcements, 0x69766f727966756cL);
            int moved = transferReinforcements(fleet, donor);
            if (fleet.getFleetData().getFleetPointsUsed()
                            < FULL_ORDO_MIN_FP
                    && fleet.getFleetData().getNumMembers() < MAX_SHIPS) {
                moved += transferReinforcements(
                        fleet,
                        createFallbackFleet(0x69766f7279667562L));
            }
            if (moved > 0) {
                fleet.getFleetData().sort();
                fleet.forceSync();
                fleet.getMemoryWithoutUpdate().unset(
                        IVORY_REFIT_COMPLETE_KEY);
                ensureIvoryRefit(fleet);
                ensureRemnantCommander(
                        fleet, new Random(0x69766f7279636f6dL));
            }
        }

        if (fleet.getFleetData().getFleetPointsUsed()
                        >= FULL_ORDO_MIN_FP
                || fleet.getFleetData().getNumMembers() >= MAX_SHIPS) {
            sectorMemory.set(FULL_ORDO_KEY, true);
            sectorMemory.unset(STRENGTH_RETRY_SINCE_KEY);
        } else {
            sectorMemory.set(
                    STRENGTH_RETRY_SINCE_KEY,
                    Global.getSector().getClock().getTimestamp());
        }
    }

    /** Filters incompatible mod hulls before strength is measured. */
    private static boolean prepareIvoryRoster(
            CampaignFleetAPI fleet, MemoryAPI sectorMemory) {
        if (fleet == null || sectorMemory == null || fleet.isEmpty()) {
            return false;
        }
        if (fleet.getMemoryWithoutUpdate().getBoolean(
                IVORY_REFIT_COMPLETE_KEY)) {
            return true;
        }
        int membersBefore = fleet.getFleetData().getNumMembers();
        float fpBefore = fleet.getFleetData().getFleetPointsUsed();
        boolean ready = ensureIvoryRefit(fleet);
        boolean filtered = fleet.getFleetData().getNumMembers() < membersBefore
                || fleet.getFleetData().getFleetPointsUsed() < fpBefore;
        if (filtered) clearStrengthMigration(sectorMemory);
        if (ready && !fleet.isEmpty()) return true;
        if (!fleet.isEmpty()) return false;

        // A legacy/modded roster can consist entirely of unsupported hulls.
        // Refill this same persistent entity with the known-vanilla fallback
        // rather than treating compatibility filtering as a player victory.
        CampaignFleetAPI fallback = createFallbackFleet(
                0x69766f7279726570L);
        if (fallback == null || fallback.isEmpty()) return false;
        transferReinforcements(fleet, fallback);
        fleet.getMemoryWithoutUpdate().unset(IVORY_REFIT_COMPLETE_KEY);
        clearStrengthMigration(sectorMemory);
        return !fleet.isEmpty() && ensureIvoryRefit(fleet);
    }

    private static void clearStrengthMigration(MemoryAPI memory) {
        if (memory == null) return;
        memory.unset(LEGACY_FULL_ORDO_KEY);
        memory.unset(PREVIOUS_FULL_ORDO_KEY);
        memory.unset(FULL_ORDO_KEY);
        memory.unset(STRENGTH_RETRY_SINCE_KEY);
    }

    private static void resetForRespawn(MemoryAPI memory) {
        if (memory == null) return;
        memory.unset(SPAWNED_KEY);
        memory.unset(FLEET_ID_KEY);
        memory.unset(FLEETS_RECONCILED_KEY);
        memory.unset(CREATION_FAILED_SINCE_KEY);
        memory.unset(MISSING_SINCE_KEY);
        clearStrengthMigration(memory);
    }

    private static int transferReinforcements(
            CampaignFleetAPI fleet, CampaignFleetAPI donor) {
        if (fleet == null || donor == null || donor.isEmpty()) return 0;
        int moved = 0;
        for (FleetMemberAPI member
                : donor.getFleetData().getMembersListCopy()) {
            if (fleet.getFleetData().getNumMembers() >= MAX_SHIPS) break;
            if (member == null
                    || member.isFighterWing()
                    || IvoryRemnantFleetSupport.getWhiteHullId(
                            member.getHullSpec().getBaseHullId()) == null) {
                continue;
            }
            float projected = fleet.getFleetData().getFleetPointsUsed()
                    + member.getFleetPointCost();
            if (projected > COMBAT_POINTS) continue;
            member.setFlagship(false);
            PersonAPI captain = member.getCaptain();
            OfficerDataAPI officer = captain == null
                    ? null : donor.getFleetData().getOfficerData(captain);
            donor.getFleetData().removeFleetMember(member);
            if (officer != null) donor.getFleetData().removeOfficer(captain);
            fleet.getFleetData().addFleetMember(member);
            if (officer != null) {
                if (fleet.getFleetData().getOfficerData(captain) == null) {
                    fleet.getFleetData().addOfficer(officer);
                }
                member.setCaptain(captain);
            }
            moved++;
        }
        return moved;
    }

    private static void trimToFleetLimits(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        boolean changed = false;
        List<FleetMemberAPI> members =
                fleet.getFleetData().getMembersListCopy();
        for (int i = members.size() - 1;
                i >= 0 && (fleet.getFleetData().getNumMembers() > MAX_SHIPS
                        || fleet.getFleetData().getFleetPointsUsed()
                                > COMBAT_POINTS);
                i--) {
            FleetMemberAPI member = members.get(i);
            if (member == null || member.isFlagship()) continue;
            removeMemberAndOfficer(fleet, member);
            changed = true;
        }
        if (changed) {
            fleet.getFleetData().sort();
            fleet.forceSync();
        }
    }

    private static void removeMemberAndOfficer(
            CampaignFleetAPI fleet, FleetMemberAPI member) {
        if (fleet == null || member == null) return;
        PersonAPI captain = member.getCaptain();
        OfficerDataAPI officer = captain == null
                ? null : fleet.getFleetData().getOfficerData(captain);
        fleet.getFleetData().removeFleetMember(member);
        if (officer != null) fleet.getFleetData().removeOfficer(captain);
    }

    private static void ensureRemnantCommander(
            CampaignFleetAPI fleet, Random random) {
        if (fleet == null || fleet.isEmpty()) return;
        fleet.getFleetData().ensureHasFlagship();
        FleetMemberAPI flagship = fleet.getFlagship();
        if (flagship == null) return;
        PersonAPI captain = flagship.getCaptain();
        if (captain == null || captain.isDefault()) {
            if (Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE) != null) {
                captain = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE)
                        .createPerson(
                                Commodities.ALPHA_CORE,
                                Factions.REMNANTS,
                                random == null ? new Random() : random);
                if (captain != null) {
                    flagship.setCaptain(captain);
                }
            }
        }
        if (captain != null && !captain.isDefault()) {
            if (fleet.getFleetData().getOfficerData(captain) == null) {
                fleet.getFleetData().addOfficer(captain);
            }
            fleet.setCommander(captain);
        }
    }

    private static void configureFleet(
            CampaignFleetAPI fleet, StarSystemAPI system) {
        if (fleet == null) return;
        fleet.setName(ShipTrophyL10n.get("gan_eden_ivory_ordo"));
        fleet.setNoFactionInName(true);
        fleet.setNoAutoDespawn(true);
        fleet.addTag(Tags.STORY_CRITICAL);
        ensureBattleListener(fleet);

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        if (!memory.contains(FID_CONFIG_GEN_KEY)) {
            // FleetFactory alone does not install the vanilla Remnant FID
            // delegate. Besides the proper presentation, that delegate owns
            // the normal committed-AI/no-retreat encounter semantics.
            RemnantSeededFleetManager.addRemnantInteractionConfig(fleet);
        }
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
        SectorEntityToken gate = system.getEntityById(
                GanEdenQuestManager.EXTERNAL_RING_ID);
        if (gate == null) gate = system.getCenter();
        if (playerHere && !hasAssignment(
                fleet, FleetAssignment.INTERCEPT, player)) {
            memory.set(PURSUING_KEY, true);
            memory.unset(PARKED_KEY);
            fleet.clearAssignments();
            fleet.addAssignment(
                    FleetAssignment.INTERCEPT,
                    player,
                    ASSIGNMENT_DURATION,
                    "closing the gate behind you");
        } else if (!playerHere && !hasAssignment(
                fleet, FleetAssignment.ORBIT_PASSIVE, gate)) {
            memory.unset(PURSUING_KEY);
            orbitGate(fleet, system);
        }
    }

    /**
     * Converts and readies the persistent interception fleet once. The quest
     * maintenance script runs every second, so doing the full member refit on
     * every pass needlessly recalculated the stats of the entire Ordo.
     */
    private static boolean ensureIvoryRefit(CampaignFleetAPI fleet) {
        if (fleet == null) return false;
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        if (memory.getBoolean(IVORY_REFIT_COMPLETE_KEY)) return true;
        if (IvoryRemnantFleetSupport.refitFleet(fleet)) {
            memory.set(IVORY_REFIT_COMPLETE_KEY, true);
            return true;
        }
        return false;
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

    private static List<CampaignFleetAPI> findFleets(StarSystemAPI system) {
        List<CampaignFleetAPI> result =
                new ArrayList<CampaignFleetAPI>();
        for (CampaignFleetAPI fleet : system.getFleets()) {
            if (fleet != null && fleet.getMemoryWithoutUpdate()
                    .getBoolean(FLEET_KEY)) {
                result.add(fleet);
            }
        }
        return result;
    }

    private static CampaignFleetAPI findRecordedFleet(MemoryAPI memory) {
        if (memory == null) return null;
        String id = memory.getString(FLEET_ID_KEY);
        SectorEntityToken entity = id == null
                ? null : Global.getSector().getEntityById(id);
        return entity instanceof CampaignFleetAPI
                ? (CampaignFleetAPI) entity : null;
    }

    private static void cleanupDefeatedEncounter(MemoryAPI memory) {
        CampaignFleetAPI recorded = findRecordedFleet(memory);
        if (recorded != null && recorded.getBattle() != null) {
            memory.unset(MISSING_SINCE_KEY);
        } else if (recorded != null
                && recorded.getContainingLocation() == null) {
            if (missingGraceElapsed(memory)) {
                // The entity is no longer attached to campaign state and
                // cannot be despawned. Stop retaining its stale ID after the
                // same grace used by the live-encounter repair path.
                memory.unset(FLEET_ID_KEY);
                recorded = null;
            }
        } else if (recorded != null) {
            despawnFleet(recorded);
        }
        for (CampaignFleetAPI tagged : findAllTaggedFleets()) {
            if (tagged != recorded) despawnFleet(tagged);
        }
        if (findRecordedFleet(memory) == null
                && findAllTaggedFleets().isEmpty()) {
            memory.set(DEFEATED_CLEANUP_KEY, true);
            memory.unset(FLEET_ID_KEY);
            memory.unset(MISSING_SINCE_KEY);
        }
    }

    private static CampaignFleetAPI reconcileTaggedFleets(
            StarSystemAPI system, MemoryAPI memory) {
        List<CampaignFleetAPI> all = findAllTaggedFleets();
        CampaignFleetAPI keeper = findRecordedFleet(memory);
        if (keeper != null
                && keeper.getContainingLocation() == null
                && keeper.getBattle() == null) {
            keeper = null;
        }
        if (keeper == null) {
            for (CampaignFleetAPI fleet : all) {
                if (fleet.getContainingLocation() == system) {
                    keeper = fleet;
                    break;
                }
            }
        }
        if (keeper == null && !all.isEmpty()) keeper = all.get(0);
        if (keeper == null) return null;
        if (keeper.getId() != null) memory.set(FLEET_ID_KEY, keeper.getId());
        for (CampaignFleetAPI fleet : all) {
            if (fleet != keeper) despawnFleet(fleet);
        }
        return keeper;
    }

    private static List<CampaignFleetAPI> findAllTaggedFleets() {
        List<CampaignFleetAPI> result =
                new ArrayList<CampaignFleetAPI>();
        for (com.fs.starfarer.api.campaign.LocationAPI location
                : Global.getSector().getAllLocations()) {
            for (CampaignFleetAPI fleet : location.getFleets()) {
                if (fleet != null && fleet.getMemoryWithoutUpdate()
                        .getBoolean(FLEET_KEY)) {
                    result.add(fleet);
                }
            }
        }
        return result;
    }

    private static void despawnFleet(CampaignFleetAPI fleet) {
        if (fleet == null || fleet.getContainingLocation() == null) return;
        fleet.setNoAutoDespawn(false);
        fleet.despawn(CampaignEventListener.FleetDespawnReason.OTHER, null);
    }

    private static boolean creationRetryReady(MemoryAPI memory) {
        if (!memory.contains(CREATION_FAILED_SINCE_KEY)) return true;
        return Global.getSector().getClock().getElapsedDaysSince(
                memory.getLong(CREATION_FAILED_SINCE_KEY))
                >= CREATION_RETRY_DAYS;
    }

    private static boolean missingGraceElapsed(MemoryAPI memory) {
        if (!memory.contains(MISSING_SINCE_KEY)) {
            memory.set(
                    MISSING_SINCE_KEY,
                    Global.getSector().getClock().getTimestamp());
            return false;
        }
        return Global.getSector().getClock().getElapsedDaysSince(
                memory.getLong(MISSING_SINCE_KEY)) >= 0.25f;
    }

    private static boolean strengthRetryReady(MemoryAPI memory) {
        if (!memory.contains(STRENGTH_RETRY_SINCE_KEY)) return true;
        return Global.getSector().getClock().getElapsedDaysSince(
                memory.getLong(STRENGTH_RETRY_SINCE_KEY))
                >= CREATION_RETRY_DAYS;
    }

    private static boolean hasAssignment(
            CampaignFleetAPI fleet,
            FleetAssignment assignment,
            SectorEntityToken target) {
        FleetAssignmentDataAPI current = fleet == null || fleet.getAI() == null
                ? null : fleet.getAI().getCurrentAssignment();
        return current != null
                && !current.isExpired()
                && current.getAssignment() == assignment
                && current.getTarget() == target;
    }

    private static void ensureBattleListener(CampaignFleetAPI fleet) {
        for (FleetEventListener listener : fleet.getEventListeners()) {
            if (listener instanceof IvoryBattleListener) return;
        }
        fleet.addEventListener(new IvoryBattleListener());
    }

    private static void markDefeated(CampaignFleetAPI fleet) {
        if (Global.getSector() == null) return;
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        memory.set(DEFEATED_KEY, true);
        memory.unset(DEFEATED_CLEANUP_KEY);
        memory.unset(MISSING_SINCE_KEY);
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
                    || !battle.onPlayerSide(primaryWinner)
                    || !battle.wasFleetDefeated(fleet, primaryWinner)) {
                return;
            }
            markDefeated(fleet);
        }

        @Override
        public void reportFleetDespawnedToListener(
                CampaignFleetAPI fleet,
                CampaignEventListener.FleetDespawnReason reason,
                Object param) {
            if (reason
                            == CampaignEventListener.FleetDespawnReason
                                    .DESTROYED_BY_BATTLE
                    || reason
                            == CampaignEventListener.FleetDespawnReason
                                    .NO_MEMBERS) {
                markDefeated(fleet);
            } else if (fleet != null
                    && Global.getSector() != null
                    && fleet.getMemoryWithoutUpdate().getBoolean(FLEET_KEY)) {
                MemoryAPI memory = Global.getSector()
                        .getMemoryWithoutUpdate();
                String recordedId = memory.getString(FLEET_ID_KEY);
                if (!memory.getBoolean(DEFEATED_KEY)
                        && (recordedId == null
                                || recordedId.equals(fleet.getId()))) {
                    // A noncombat despawn is not a victory. Clear only this
                    // designated encounter owner so maintenance can recreate
                    // it instead of leaving the Gate permanently locked.
                    resetForRespawn(memory);
                }
            }
            if (fleet != null) fleet.removeEventListener(this);
        }
    }
}
