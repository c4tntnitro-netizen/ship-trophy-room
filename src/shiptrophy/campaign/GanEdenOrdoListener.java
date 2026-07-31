package shiptrophy.campaign;

import java.util.Locale;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

/** Adds rare post-quest Golden Shard leaders to newly spawned Remnant Ordos. */
public final class GanEdenOrdoListener extends BaseCampaignEventListener {
    private static final String PROCESSED_KEY =
            "$shipTrophyGanEdenOrdoGoldenRoll";
    private static final String ALTERED_KEY =
            "$shipTrophyGanEdenOrdoGoldenShard";

    public GanEdenOrdoListener() {
        super(false);
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        if (fleet == null || !GanEdenQuestManager.isCompleted()) return;
        if (fleet.getMemoryWithoutUpdate().getBoolean(
                GanEdenAmbushScript.FLEET_KEY)) return;
        if (fleet.getMemoryWithoutUpdate().getBoolean(PROCESSED_KEY)) return;
        if (fleet.getFaction() == null
                || !Factions.REMNANTS.equals(fleet.getFaction().getId())) {
            return;
        }
        String name = fleet.getName();
        if (name == null
                || !name.toLowerCase(Locale.ROOT).contains("ordo")) {
            return;
        }

        fleet.getMemoryWithoutUpdate().set(PROCESSED_KEY, true);
        long seed = 0x617572656174656fL
                ^ Global.getSector().getSeedString().hashCode()
                ^ (fleet.getId() == null ? 0 : fleet.getId().hashCode());
        Random random = new Random(seed);
        double roll = random.nextDouble();
        if (roll >= 0.10) return;

        boolean both = roll < 0.01;
        boolean addCherubim = both || random.nextBoolean();
        boolean addLahat = both || !addCherubim;
        FleetMemberAPI leader = null;
        if (addCherubim) {
            leader = GanEdenAmbushScript.addNamedGuardian(
                    fleet, true, random);
        }
        if (addLahat) {
            FleetMemberAPI member = GanEdenAmbushScript.addNamedGuardian(
                    fleet, false, random);
            if (leader == null) leader = member;
        }
        if (leader == null) return;

        fleet.getFleetData().setFlagship(leader);
        if (leader.getCaptain() != null) {
            fleet.setCommander(leader.getCaptain());
        }
        fleet.getFleetData().sort();
        fleet.forceSync();
        fleet.setName("Aureate Ordo");
        fleet.setNoFactionInName(true);
        fleet.getMemoryWithoutUpdate().set(ALTERED_KEY, true);
    }
}
