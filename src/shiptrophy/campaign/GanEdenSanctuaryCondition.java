package shiptrophy.campaign;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

/**
 * Makes Gan Eden colonies exceptionally stable but economically inaccessible
 * while the prototype system remains sealed from the wider Sector.
 */
public class GanEdenSanctuaryCondition extends BaseMarketConditionPlugin {
    private static final float STABILITY_BONUS = 100f;

    @Override
    public void apply(String id) {
        market.getStability().modifyFlat(
                id, STABILITY_BONUS, "Gan Eden sanctuary");

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
