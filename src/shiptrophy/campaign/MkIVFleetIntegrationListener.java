package shiptrophy.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

/** Propagates defeated hypershunt-blockade Mk IV refits into faction fleets. */
public final class MkIVFleetIntegrationListener
        extends BaseCampaignEventListener {
    public static final String PATH_UNLOCK_KEY =
            "$shipTrophyMkIVPathUnlocked";
    public static final String PIRATE_UNLOCK_KEY =
            "$shipTrophyMkIVPirateUnlocked";

    private static final String PROCESSED_KEY =
            "$shipTrophyMkIVFleetPoolRollV1";
    private static final float FLEET_CHANCE = 0.32f;
    private static final long SEED_SALT = 0x6d6b6976706f6f6cL;

    public MkIVFleetIntegrationListener() {
        super(false);
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        integrateFleet(fleet);
    }

    public static boolean isUnlocked(String factionId) {
        if (Global.getSector() == null) return false;
        String key = unlockKey(factionId);
        return key != null && Global.getSector().getMemoryWithoutUpdate()
                .getBoolean(key);
    }

    public static void unlockFaction(String factionId) {
        if (Global.getSector() == null) return;
        String key = unlockKey(factionId);
        if (key == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(key, true);
        integrateExistingFleets(factionId);
    }

    /** Applies unlocks to fleets already alive when a save is loaded. */
    public static void integrateExistingUnlockedFleets() {
        migrateExistingLegacyMkIVFleets();
        if (isUnlocked(Factions.PIRATES)) {
            integrateExistingFleets(Factions.PIRATES);
        }
        if (isUnlocked(Factions.LUDDIC_PATH)) {
            integrateExistingFleets(Factions.LUDDIC_PATH);
        }
    }

    private static void migrateExistingLegacyMkIVFleets() {
        if (Global.getSector() == null) return;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (CampaignFleetAPI fleet
                    : new ArrayList<CampaignFleetAPI>(location.getFleets())) {
                if (fleet == null || fleet.getFaction() == null) continue;
                String factionId = fleet.getFaction().getId();
                if (Factions.PIRATES.equals(factionId)
                        || Factions.LUDDIC_PATH.equals(factionId)) {
                    GanEdenHypershuntManager.migrateLegacyMkIVMembers(
                            fleet, factionId);
                }
            }
        }
    }

    private static void integrateExistingFleets(String factionId) {
        if (Global.getSector() == null) return;
        for (LocationAPI location : Global.getSector().getAllLocations()) {
            for (CampaignFleetAPI fleet
                    : new ArrayList<CampaignFleetAPI>(location.getFleets())) {
                if (fleet != null && fleet.getFaction() != null
                        && factionId.equals(fleet.getFaction().getId())) {
                    integrateFleet(fleet);
                }
            }
        }
    }

    private static void integrateFleet(CampaignFleetAPI fleet) {
        if (fleet == null || fleet.isPlayerFleet() || fleet.isEmpty()
                || fleet.isStationMode() || fleet.getFaction() == null) {
            return;
        }
        String factionId = fleet.getFaction().getId();
        GanEdenHypershuntManager.migrateLegacyMkIVMembers(
                fleet, factionId);
        if (!isUnlocked(factionId)) return;

        MemoryAPI memory = fleet.getMemoryWithoutUpdate();
        if (memory.getBoolean(PROCESSED_KEY)
                || memory.getBoolean(GanEdenHypershuntManager.GUARD_KEY)) {
            return;
        }
        memory.set(PROCESSED_KEY, true);

        Random random = new Random(SEED_SALT
                ^ Global.getSector().getSeedString().hashCode()
                ^ factionId.hashCode()
                ^ (fleet.getId() == null ? 0 : fleet.getId().hashCode()));
        if (random.nextFloat() >= FLEET_CHANCE) return;

        List<FleetMemberAPI> eligible = new ArrayList<FleetMemberAPI>();
        for (FleetMemberAPI member
                : fleet.getFleetData().getMembersListCopy()) {
            if (member != null && !member.isFighterWing()
                    && member.getVariant() != null
                    && GanEdenHypershuntManager.canRefitMemberAsMkIV(
                            member, factionId)) {
                eligible.add(member);
            }
        }
        if (eligible.isEmpty()) return;

        Collections.shuffle(eligible, random);
        // Roughly one Mk IV for every seven ships, capped so the refit remains
        // distinctive instead of replacing the factions' normal fleets.
        int target = Math.max(1, Math.min(4, (eligible.size() + 6) / 7));
        boolean changed = false;
        for (int index = 0; index < target; index++) {
            changed |= GanEdenHypershuntManager.refitMemberAsMkIV(
                    eligible.get(index), factionId);
        }
        if (changed) {
            fleet.getFleetData().sort();
            fleet.forceSync();
        }
    }

    private static String unlockKey(String factionId) {
        if (Factions.PIRATES.equals(factionId)) return PIRATE_UNLOCK_KEY;
        if (Factions.LUDDIC_PATH.equals(factionId)) return PATH_UNLOCK_KEY;
        return null;
    }
}
