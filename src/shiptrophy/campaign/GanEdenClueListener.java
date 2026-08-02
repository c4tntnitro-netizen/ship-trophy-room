package shiptrophy.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.PlayerMarketTransaction;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

/** Consumes the bound clue immediately after its black-market purchase. */
public final class GanEdenClueListener extends BaseCampaignEventListener {
    private static final SpecialItemData ITEM = new SpecialItemData(
            GanEdenClueManager.ITEM_ID, null);

    public GanEdenClueListener() {
        super(false);
    }

    @Override
    public void reportPlayerOpenedMarketAndCargoUpdated(MarketAPI market) {
        if (market != null && ShatteredRingGenerator.MARKET_ID.equals(
                market.getId())) {
            GanEdenClueManager.ensureStock();
        }
    }

    @Override
    public void reportPlayerMarketTransaction(
            PlayerMarketTransaction transaction) {
        if (transaction == null
                || transaction.getMarket() == null
                || !ShatteredRingGenerator.MARKET_ID.equals(
                        transaction.getMarket().getId())
                || transaction.getBought() == null) {
            return;
        }
        float bought = transaction.getBought().getQuantity(
                CargoAPI.CargoItemType.SPECIAL, ITEM);
        if (bought <= 0f) return;

        GanEdenClueManager.consumePlayerClue();
        if (Global.getSector() != null) {
            Global.getSector().getCampaignUI().addMessage(
                    "Gate receiver decoded: Epitaph — Part I");
        }
    }
}
