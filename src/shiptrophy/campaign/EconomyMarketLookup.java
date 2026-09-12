package shiptrophy.campaign;

import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

/** Market-id lookups that do not rebuild the economy's location cache. */
final class EconomyMarketLookup {
    private EconomyMarketLookup() {
    }

    static MarketAPI findById(String marketId) {
        if (marketId == null || Global.getSector() == null) return null;

        EconomyAPI economy = Global.getSector().getEconomy();
        if (economy == null) return null;

        // EconomyAPI.getMarket(String) rebuilds ReachEconomy's shared
        // location cache when it is dirty. Starsector 0.98a does not
        // synchronize that rebuild, so a concurrent economy reader can
        // corrupt one of its ArrayLists. An id lookup only needs the
        // authoritative market list, for which a snapshot is sufficient.
        List<MarketAPI> markets = economy.getMarketsCopy();
        // Preserve getMarket(String)'s last-entry-wins behavior if a broken
        // save happens to contain duplicate market ids.
        for (int index = markets.size() - 1; index >= 0; index--) {
            MarketAPI market = markets.get(index);
            if (market != null && marketId.equals(market.getId())) {
                return market;
            }
        }
        return null;
    }
}
