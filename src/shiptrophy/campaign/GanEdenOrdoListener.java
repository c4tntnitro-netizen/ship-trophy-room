package shiptrophy.campaign;

import java.util.Locale;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

/** Adds occasional ordinary Omega escorts to post-victory Remnant Ordos. */
public final class GanEdenOrdoListener extends BaseCampaignEventListener {
    private static final String PROCESSED_KEY =
            "$shipTrophyGanEdenOrdoGoldenRoll";
    private static final String ALTERED_KEY =
            "$shipTrophyGanEdenOrdoOmegaEscort";
    private static final double ESCORT_CHANCE = 0.10;
    private static final String FACET_VARIANT = "facet_Attack";
    private static final String SHARD_LEFT_VARIANT = "shard_left_Attack";
    private static final String SHARD_RIGHT_VARIANT = "shard_right_Attack";

    public GanEdenOrdoListener() {
        super(false);
    }

    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        if (fleet == null || !GanEdenAmbushScript.isDefeated()) return;
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
        if (random.nextDouble() >= ESCORT_CHANCE) return;

        int added = 0;
        boolean shardEscort = random.nextFloat() < 0.45f;
        if (shardEscort) {
            String variant = random.nextBoolean()
                    ? SHARD_LEFT_VARIANT
                    : SHARD_RIGHT_VARIANT;
            if (GanEdenAmbushScript.addRegularOmegaEscort(
                    fleet, variant, random) != null) {
                added++;
            }
            // A small fraction of successful rolls mix a Facet into the
            // Shard's screen rather than replacing the Ordo's flagship.
            if (random.nextFloat() < 0.15f
                    && GanEdenAmbushScript.addRegularOmegaEscort(
                            fleet, FACET_VARIANT, random) != null) {
                added++;
            }
        } else {
            int facets = 1 + random.nextInt(2);
            for (int i = 0; i < facets; i++) {
                if (GanEdenAmbushScript.addRegularOmegaEscort(
                        fleet, FACET_VARIANT, random) != null) {
                    added++;
                }
            }
        }
        if (added <= 0) return;

        fleet.getFleetData().sort();
        fleet.forceSync();
        fleet.getMemoryWithoutUpdate().set(ALTERED_KEY, true);
    }
}
