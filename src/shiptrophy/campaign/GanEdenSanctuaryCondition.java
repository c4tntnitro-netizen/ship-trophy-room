package shiptrophy.campaign;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

/** Keeps Gan Eden markets economically isolated from the wider Sector. */
public class GanEdenSanctuaryCondition extends BaseMarketConditionPlugin {
    @Override
    public void apply(String id) {
        // Earlier builds used this condition to grant +100 stability. Scrub
        // that legacy modifier so the Aureate Siege's -10 is consequential
        // in campaigns which have already visited Gan Eden.
        market.getStability().unmodify(id);

        // Starsector's StatBonus formula is:
        // (base * (1 + percent / 100) + flat) * product(multipliers).
        // Supplying one zero multiplier therefore fixes the computed
        // accessibility at zero regardless of every other modifier.
        market.getAccessibilityMod().modifyMult(
                id, 0f, "Sealed inside Gan Eden");
    }

    @Override
    public void unapply(String id) {
        market.getStability().unmodify(id);
        market.getAccessibilityMod().unmodify(id);
    }
}
