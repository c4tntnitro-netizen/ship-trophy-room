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
            return "Ships displayed here contribute to the colony's Trophy Room story point generation.";
        }
        return "Requires a functional Trophy Room on this colony.";
    }

    @Override
    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        Highlights highlights = new Highlights();
        highlights.setText("Trophy Room", "story point generation", "Requires a functional Trophy Room");
        return highlights;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara("Stored ships are preserved even if the Trophy Room is disrupted, but this tab can only be opened while the structure is functional.", 10f);
    }

    private boolean hasFunctionalTrophyRoom() {
        return market != null && TrophyRoomIndustry.isFunctionalTrophyRoom(market.getIndustry(ShipTrophyRoomIds.INDUSTRY));
    }
}
