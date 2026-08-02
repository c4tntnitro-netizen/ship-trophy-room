package shiptrophy.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;

/** Owns the unique Shattered Ring black-market clue. */
public final class GanEdenClueManager {
    public static final String ITEM_ID = "ship_trophy_gan_eden_receiver";
    public static final String CLUE_REVEALED_KEY =
            "$shipTrophyGanEdenBlackMarketClueRevealed";

    private static final SpecialItemData ITEM =
            new SpecialItemData(ITEM_ID, null);

    private GanEdenClueManager() {
    }

    public static void reveal() {
        if (Global.getSector() == null) return;
        Global.getSector().getMemoryWithoutUpdate().set(
                CLUE_REVEALED_KEY, true);
        GanEdenQuestManager.revealBlackMarketClue(null);
        ensureStock();
    }

    public static boolean isRevealed() {
        return Global.getSector() != null
                && Global.getSector().getMemoryWithoutUpdate()
                        .getBoolean(CLUE_REVEALED_KEY);
    }

    public static void ensureStock() {
        if (Global.getSector() == null) return;
        MarketAPI market = Global.getSector().getEconomy().getMarket(
                ShatteredRingGenerator.MARKET_ID);
        if (market == null) return;
        SubmarketAPI black = market.getSubmarket(Submarkets.SUBMARKET_BLACK);
        if (black == null || black.getCargo() == null) return;

        CargoAPI cargo = black.getCargo();
        boolean shouldStock = isRevealed()
                && GanEdenQuestManager.getStage()
                        == GanEdenQuestManager.Stage.FIND_BLACK_MARKET_CLUE
                && !GanEdenLogManager.isRecovered(GanEdenLogSpec.PART_ONE);
        float quantity = cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, ITEM);
        if (shouldStock && quantity < 1f) {
            cargo.addSpecial(ITEM, 1f);
            cargo.sort();
        } else if (!shouldStock && quantity > 0f) {
            cargo.removeItems(CargoAPI.CargoItemType.SPECIAL, ITEM, quantity);
        }
    }

    public static boolean playerHasClue() {
        return Global.getSector() != null
                && Global.getSector().getPlayerFleet() != null
                && Global.getSector().getPlayerFleet().getCargo().getQuantity(
                        CargoAPI.CargoItemType.SPECIAL, ITEM) > 0f;
    }

    public static void consumePlayerClue() {
        if (!playerHasClue()) return;
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        float quantity = cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, ITEM);
        cargo.removeItems(CargoAPI.CargoItemType.SPECIAL, ITEM, quantity);
        GanEdenLogManager.recover(GanEdenLogSpec.PART_ONE, null);
        GanEdenQuestManager.beginHypershuntSearch(null);
        ensureStock();
    }
}
