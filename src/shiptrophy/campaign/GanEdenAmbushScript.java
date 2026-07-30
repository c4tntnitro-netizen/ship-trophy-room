package shiptrophy.campaign;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;

/**
 * Springs Gan Eden's one-time golden Omega welcome party after the player
 * arrives. The descendants are created by GoldenFractalCascade during combat.
 */
public final class GanEdenAmbushScript implements EveryFrameScript {
    public static final String TRIGGERED_KEY =
            "$shipTrophyGanEdenGoldenAmbushTriggered";
    public static final String FLEET_KEY =
            "$shipTrophyGanEdenGoldenAmbushFleet";

    private static final String SINISTRAL_VARIANT =
            "ship_trophy_golden_shard_left_Attack";
    private static final String DEXTRAL_VARIANT =
            "ship_trophy_golden_shard_right_Attack";
    private static final float ARRIVAL_GRACE_SECONDS = 1.25f;
    private static final float SPAWN_DISTANCE = 700f;

    private float elapsedInGanEden;

    @Override
    public boolean isDone() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(TRIGGERED_KEY);
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (Global.getSector() == null
                || Global.getSector().getPlayerFleet() == null
                || isDone()) {
            return;
        }

        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        LocationAPI location = player.getContainingLocation();
        if (location == null
                || !GanEdenGenerator.SYSTEM_ID.equals(location.getId())) {
            elapsedInGanEden = 0f;
            return;
        }

        elapsedInGanEden += Math.max(0f, amount);
        if (elapsedInGanEden < ARRIVAL_GRACE_SECONDS) return;

        StarSystemAPI system = Global.getSector().getStarSystem(
                GanEdenGenerator.SYSTEM_ID);
        if (system == null) return;

        CampaignFleetAPI fleet = createFleet();
        if (fleet == null || fleet.isEmpty()) return;

        Vector2f offset = chooseTangentialSpawnOffset(player, system);
        system.spawnFleet(player, offset.x, offset.y, fleet);
        configureAttack(fleet, player);

        Global.getSector().getMemoryWithoutUpdate().set(TRIGGERED_KEY, true);
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

    private static Vector2f chooseTangentialSpawnOffset(
            CampaignFleetAPI player,
            StarSystemAPI system) {
        Vector2f center = system.getCenter().getLocation();
        Vector2f at = player.getLocation();
        float radialX = at.x - center.x;
        float radialY = at.y - center.y;
        float length = (float) Math.sqrt(
                radialX * radialX + radialY * radialY);
        if (length < 1f) {
            radialX = 1f;
            radialY = 0f;
            length = 1f;
        }

        // A tangent keeps the ambush clear of both the central star and the
        // altitude-warning boundary, regardless of how the player entered.
        return new Vector2f(
                -radialY / length * SPAWN_DISTANCE,
                radialX / length * SPAWN_DISTANCE);
    }

    private static void configureAttack(
            CampaignFleetAPI fleet,
            CampaignFleetAPI player) {
        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        memory.set(FLEET_KEY, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
        memory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        memory.set(MemFlags.MEMORY_KEY_NO_SHIP_RECOVERY, true);
        memory.set(
                MemFlags.MEMORY_KEY_NO_SHIP_DERELICTS_IN_POST_BATTLE_DEBRIS,
                true);
        memory.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true);
        memory.set(MemFlags.MEMORY_KEY_FORCE_TRANSPONDER_OFF, true);

        fleet.clearAssignments();
        fleet.addAssignment(
                FleetAssignment.INTERCEPT,
                player,
                1000f,
                "closing on your fleet");
    }
}
