package shiptrophy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class TrophyRoomIndustry extends BaseIndustry {
    public static final int BASE_DAYS_PER_STORY_POINT = 180;

    @Override
    public void apply() {
        super.apply(false);
        if (!isFunctional()) return;
        ensureTrophyStorage();
    }

    @Override
    public void apply(boolean withIncomeUpdate) {
        super.apply(withIncomeUpdate);
        if (!isFunctional()) return;
        ensureTrophyStorage();
    }

    @Override
    protected void buildingFinished() {
        super.buildingFinished();
        ensureTrophyStorage();
    }

    private void ensureTrophyStorage() {
        if (market == null || market.hasSubmarket(ShipTrophyRoomIds.SUBMARKET)) return;
        market.addSubmarket(ShipTrophyRoomIds.SUBMARKET);
        if (market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getPlugin() instanceof StoragePlugin) {
            StoragePlugin plugin = (StoragePlugin) market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getPlugin();
            plugin.setPlayerPaidToUnlock(true);
        }
    }

    @Override
    public boolean isAvailableToBuild() {
        return market != null && market.isPlayerOwned();
    }

    @Override
    public boolean showWhenUnavailable() {
        return true;
    }

    @Override
    public String getUnavailableReason() {
        if (market != null && !market.isPlayerOwned()) {
            return "Can only be built on player colonies";
        }
        return super.getUnavailableReason();
    }

    @Override
    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        super.addPostDescriptionSection(tooltip, mode);
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara("Adds a dedicated Hall of Triumph storage tab for ships and cargo.", opad, h, "Hall of Triumph");
        tooltip.addPara("All Halls of Triumph are networked. Story point generation and doctrine unlocks use the whole network's unique displayed hull types.",
                opad, h, "unique", "Duplicate ships");
        tooltip.addPara("Base rate is 1 story point every %s days. Every %s unique hull types and every %s total unique deployment points each add another full rate bonus.",
                opad, h, "" + BASE_DAYS_PER_STORY_POINT, "" + TrophyNetwork.UNIQUE_HULLS_FOR_FULL_BONUS, "" + TrophyNetwork.DP_FOR_FULL_BONUS);

    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    public String getImproveMenuText() {
        return "Curate exhibits";
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        info.addPara("Reduces Hall of Triumph story point generation time by %s.", opad, h, "25%");
    }

    public static boolean isFunctionalTrophyRoom(Industry industry) {
        return industry != null && !industry.isBuilding() && industry.isFunctional();
    }

}
