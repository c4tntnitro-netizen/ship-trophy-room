package shiptrophy;

import java.awt.Color;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class TrophyRoomIndustry extends BaseIndustry {
    public static final int BASE_DAYS_PER_STORY_POINT = 180;
    public static final int CREDITS_PER_UNIQUE_OP = 10;
    public static final int OPERATING_DEMAND = 1;
    private static final String STIPEND_MOD_ID =
            "ship_trophy_preservation_stipend";

    @Override
    public void apply() {
        super.apply(false);
        applyOperatingDemand();
        if (!isFunctional()) return;
        ensureTrophyStorage();
    }

    @Override
    public void apply(boolean withIncomeUpdate) {
        super.apply(withIncomeUpdate);
        applyOperatingDemand();
        if (withIncomeUpdate) applyPreservationStipend();
        if (!isFunctional()) return;
        ensureTrophyStorage();
    }

    @Override
    public void updateIncomeAndUpkeep() {
        super.updateIncomeAndUpkeep();
        applyPreservationStipend();
    }

    @Override
    public void unapply() {
        if (getIncome() != null) {
            getIncome().unmodifyFlat(STIPEND_MOD_ID);
        }
        super.unapply();
    }

    private void applyOperatingDemand() {
        demand(Commodities.CREW, OPERATING_DEMAND);
        demand(Commodities.HEAVY_MACHINERY, OPERATING_DEMAND);
        demand(Commodities.SUPPLIES, OPERATING_DEMAND);
    }

    private void applyPreservationStipend() {
        if (getIncome() == null) return;
        getIncome().unmodifyFlat(STIPEND_MOD_ID);
        if (!isFunctional()) return;

        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        if (stats.functionalRooms <= 0 || stats.uniqueOrdnancePoints <= 0) return;

        float networkStipend = stats.uniqueOrdnancePoints
                * CREDITS_PER_UNIQUE_OP;
        float localShare = networkStipend / stats.functionalRooms;
        float demandMult = getDeficitMult(
                Commodities.CREW,
                Commodities.HEAVY_MACHINERY,
                Commodities.SUPPLIES);
        float stipend = localShare * demandMult;
        if (stipend <= 0f) return;

        getIncome().modifyFlat(
                STIPEND_MOD_ID,
                stipend,
                ShipTrophyL10n.get("industry_stipend_income"));
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
            return ShipTrophyL10n.get("industry_player_only");
        }
        return super.getUnavailableReason();
    }

    @Override
    protected void addPostDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {
        super.addPostDescriptionSection(tooltip, mode);
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        tooltip.addPara(ShipTrophyL10n.get("industry_storage"), opad, h,
                ShipTrophyL10n.get("storage_highlight_hall"));
        tooltip.addPara(ShipTrophyL10n.get("industry_network_programs"),
                opad, h,
                ShipTrophyL10n.get("industry_highlight_unique"),
                ShipTrophyL10n.get("industry_highlight_duplicates"));
        tooltip.addPara(ShipTrophyL10n.get("industry_base_rate"),
                opad, h, "" + BASE_DAYS_PER_STORY_POINT, "" + TrophyNetwork.UNIQUE_HULLS_FOR_FULL_BONUS, "" + TrophyNetwork.DP_FOR_FULL_BONUS);
        TrophyNetwork.NetworkStats stats = TrophyNetwork.computeNetworkStats();
        int networkStipend = stats.uniqueOrdnancePoints
                * CREDITS_PER_UNIQUE_OP;
        tooltip.addPara(ShipTrophyL10n.get("industry_stipend"),
                opad, h,
                "" + CREDITS_PER_UNIQUE_OP,
                "" + stats.uniqueOrdnancePoints,
                Misc.getWithDGS(networkStipend));
        tooltip.addPara(ShipTrophyL10n.get("industry_operating_demand"),
                opad, h, "" + OPERATING_DEMAND);
        float isaBonus = HallOfTriumphFeatures.getIsaStoryPointGenerationBonusPercent();
        if (isaBonus > 0f) {
            tooltip.addPara(ShipTrophyL10n.get("industry_isa_bonus"),
                    opad, h, formatPercent(isaBonus));
        }

    }

    @Override
    public boolean canImprove() {
        return true;
    }

    @Override
    public String getImproveMenuText() {
        return ShipTrophyL10n.get("industry_demand_name");
    }

    @Override
    public void addImproveDesc(TooltipMakerAPI info, ImprovementDescriptionMode mode) {
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        info.addPara(ShipTrophyL10n.get("industry_demand_effect"),
                opad, h, "25%");
    }

    public static boolean isFunctionalTrophyRoom(Industry industry) {
        return industry != null && !industry.isBuilding() && industry.isFunctional();
    }

    private static String formatPercent(float value) {
        return Math.abs(value - Math.round(value)) < 0.001f
                ? Math.round(value) + "%" : String.format("%.1f%%", value);
    }

}
