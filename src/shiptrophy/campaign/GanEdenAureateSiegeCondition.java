package shiptrophy.campaign;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

/**
 * System-wide stability crisis caused by Gan Eden's surviving Golden Shards.
 */
public class GanEdenAureateSiegeCondition
        extends BaseMarketConditionPlugin {
    private static final float STABILITY_PENALTY = -10f;

    @Override
    public void apply(String id) {
        market.getStability().modifyFlat(
                id,
                STABILITY_PENALTY,
                "Golden Omega blockade");
    }

    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
    }
}
