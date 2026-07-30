package shiptrophy.campaign;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

/**
 * Ensures Gan Eden's golden Omega fleet is parked beside the transit ring.
 * The descendants are created by GoldenFractalCascade during combat.
 */
public final class GanEdenAmbushScript implements EveryFrameScript {
    public static final String FLEET_KEY =
            "$shipTrophyGanEdenGoldenAmbushFleet";

    private static final String SINISTRAL_VARIANT =
            "ship_trophy_golden_shard_left_Attack";
    private static final String DEXTRAL_VARIANT =
            "ship_trophy_golden_shard_right_Attack";
    private static final float RING_OFFSET = 350f;
    private static final float GUARD_DURATION = 1000000f;

    private boolean finished;

    @Override
    public boolean isDone() {
        return finished;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null || finished) return;

        StarSystemAPI system = Global.getSector().getStarSystem(
                GanEdenGenerator.SYSTEM_ID);
        if (system == null) return;

        SectorEntityToken anchor = system.getEntityById(
                GanEdenGenerator.ARRIVAL_RING_ID);
        if (anchor == null) anchor = system.getCenter();

        CampaignFleetAPI existing = findExistingFleet(system);
        if (existing != null) {
            configureGuard(existing, anchor);
            finished = true;
            return;
        }

        CampaignFleetAPI fleet = createFleet();
        if (fleet == null || fleet.isEmpty()) return;

        system.spawnFleet(anchor, RING_OFFSET, 0f, fleet);
        configureGuard(fleet, anchor);
        finished = true;
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

    private static CampaignFleetAPI createFleet() {
        CampaignFleetAPI fleet = Global.getFactory().createEmptyFleet(
                Factions.OMEGA, "Aureate Refractions", true);
        if (fleet == null) return null;

        FleetMemberAPI sinistral =
                fleet.getFleetData().addFleetMember(SINISTRAL_VARIANT);
        FleetMemberAPI dextral =
                fleet.getFleetData().addFleetMember(DEXTRAL_VARIANT);
        if (sinistral == null || dextral == null) return fleet;

        Random random = new Random(0x6a616e6564656eL);
        PersonAPI sinistralCore = createOmegaCore(random);
        PersonAPI dextralCore = createOmegaCore(random);
        if (sinistralCore != null) {
            sinistral.setCaptain(sinistralCore);
            fleet.setCommander(sinistralCore);
        }
        if (dextralCore != null) {
            dextral.setCaptain(dextralCore);
        }

        readyMember(sinistral);
        readyMember(dextral);
        fleet.getFleetData().setFlagship(sinistral);
        fleet.getFleetData().sort();
        fleet.forceSync();
        fleet.setName("Aureate Refractions");
        fleet.setNoFactionInName(true);
        fleet.setNoAutoDespawn(true);
        return fleet;
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
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(FLEET_KEY, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        memory.set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
        memory.set(
                MemFlags.MEMORY_KEY_NO_SHIP_DERELICTS_IN_POST_BATTLE_DEBRIS,
                true);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);

        fleet.clearAssignments();
        fleet.addAssignment(
                FleetAssignment.ORBIT_PASSIVE,
                anchor,
                GUARD_DURATION,
                "waiting in silence");
    }
}
