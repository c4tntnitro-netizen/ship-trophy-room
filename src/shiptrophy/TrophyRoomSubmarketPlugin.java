package shiptrophy;

import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;

public class TrophyRoomSubmarketPlugin extends StoragePlugin {

    @Override
    public void init(com.fs.starfarer.api.campaign.econ.SubmarketAPI submarket) {
        super.init(submarket);
        setPlayerPaidToUnlock(true);
    }


    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        return hasFunctionalTrophyRoom();
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        if (hasFunctionalTrophyRoom()) {
            return ShipTrophyL10n.get("storage_functional");
        }
        return ShipTrophyL10n.get("storage_requires_hall");
    }

    @Override
    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        Highlights highlights = new Highlights();
        highlights.setText(
                ShipTrophyL10n.get("storage_highlight_hall"),
                ShipTrophyL10n.get("storage_highlight_generation"),
                ShipTrophyL10n.get("storage_highlight_requires"));
        return highlights;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara(ShipTrophyL10n.get("storage_preserved"), 10f);
    }

    private boolean hasFunctionalTrophyRoom() {
        return market != null && TrophyRoomIndustry.isFunctionalTrophyRoom(market.getIndustry(ShipTrophyRoomIds.INDUSTRY));
    }
}
