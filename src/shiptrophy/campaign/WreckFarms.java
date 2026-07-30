package shiptrophy.campaign;

import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Pair;

/**
 * The Ring's defining industry: a stable shipbreaking economy supplied by the
 * unusually dense derelict field surrounding the station.
 */
public class WreckFarms extends BaseIndustry {
    @Override
    public void apply() {
        apply(true);
    }

    @Override
    public void apply(boolean withIncomeUpdate) {
        super.apply(withIncomeUpdate);

        int size = market.getSize();
        demand(Commodities.CREW, Math.max(1, size - 1));
        demand(Commodities.SUPPLIES, Math.max(1, size - 2));
        demand(Commodities.HEAVY_MACHINERY, Math.max(1, size - 1));

        supply(Commodities.METALS, size + 1);
        supply(Commodities.RARE_METALS, Math.max(1, size - 2));
        supply(Commodities.SUPPLIES, Math.max(1, size - 2));
        supply(Commodities.HEAVY_MACHINERY, Math.max(1, size - 3));
        supply(Commodities.SHIPS, Math.max(1, size - 3));

        Pair<String, Integer> deficit = getMaxDeficit(
                Commodities.CREW, Commodities.SUPPLIES, Commodities.HEAVY_MACHINERY);
        applyDeficitToProduction(1, deficit,
                Commodities.METALS,
                Commodities.RARE_METALS,
                Commodities.SUPPLIES,
                Commodities.HEAVY_MACHINERY,
                Commodities.SHIPS);
        applyIncomeAndUpkeep(3f);

        if (!isFunctional()) {
            supply.clear();
        }
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    @Override
    public boolean canInstallAICores() {
        return false;
    }

    @Override
    public boolean canImprove() {
        return false;
    }
}
