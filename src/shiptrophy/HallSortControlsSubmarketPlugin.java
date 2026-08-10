package shiptrophy;

import com.fs.starfarer.api.Script;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.SubmarketPlugin.DialogOption;
import com.fs.starfarer.api.campaign.SubmarketPlugin.OnClickAction;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Highlights;
import com.fs.starfarer.api.util.Misc;

import shiptrophy.HallStorageSortPreferences.SortKey;

/** In-game base-mod controls for Hall storage ordering. */
public class HallSortControlsSubmarketPlugin extends BaseSubmarketPlugin {
    @Override
    public boolean isEnabled(CoreUIAPI ui) {
        return market != null && TrophyRoomIndustry.isFunctionalTrophyRoom(
                market.getIndustry(ShipTrophyRoomIds.INDUSTRY));
    }

    @Override
    public OnClickAction getOnClickAction(CoreUIAPI ui) {
        return OnClickAction.SHOW_TEXT_DIALOG;
    }

    @Override
    public String getDialogText(CoreUIAPI ui) {
        return "Choose the two categories used to arrange ships in Hall of Triumph storage. "
                + "Current order: " + HallStorageSortPreferences.getPrimary().getLabel()
                + ", then " + HallStorageSortPreferences.getSecondary().getLabel() + ". "
                + "Choose one category below; reopen these controls to set the other category.";
    }

    @Override
    public Highlights getDialogTextHighlights(CoreUIAPI ui) {
        Highlights highlights = new Highlights();
        highlights.setText(HallStorageSortPreferences.getPrimary().getLabel(),
                HallStorageSortPreferences.getSecondary().getLabel());
        highlights.setColors(Misc.getHighlightColor(), Misc.getHighlightColor());
        return highlights;
    }

    @Override
    public DialogOption[] getDialogOptions(CoreUIAPI ui) {
        SortKey[] keys = SortKey.values();
        DialogOption[] options = new DialogOption[keys.length * 2 + 1];
        int index = 0;
        for (final SortKey key : keys) {
            options[index++] = new DialogOption("Primary: " + key.getLabel(), new Script() {
                @Override
                public void run() {
                    HallStorageSortPreferences.setPrimary(key);
                    sortHallStorage();
                }
            });
        }
        for (final SortKey key : keys) {
            options[index++] = new DialogOption("Secondary: " + key.getLabel(), new Script() {
                @Override
                public void run() {
                    HallStorageSortPreferences.setSecondary(key);
                    sortHallStorage();
                }
            });
        }
        options[index] = new DialogOption("Leave unchanged", null);
        return options;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui) {
        return "Base-mod controls; LunaLib is not required.";
    }

    @Override
    public Highlights getTooltipAppendixHighlights(CoreUIAPI ui) {
        Highlights highlights = new Highlights();
        highlights.setText("LunaLib is not required");
        highlights.setColors(Misc.getPositiveHighlightColor());
        return highlights;
    }

    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara("Current order: %s, then %s.", 10f,
                Misc.getHighlightColor(),
                HallStorageSortPreferences.getPrimary().getLabel(),
                HallStorageSortPreferences.getSecondary().getLabel());
    }

    @Override
    public boolean showInFleetScreen() {
        return false;
    }

    @Override
    public boolean showInCargoScreen() {
        return false;
    }

    @Override
    public boolean isParticipatesInEconomy() {
        return false;
    }

    private void sortHallStorage() {
        if (market == null || !market.hasSubmarket(ShipTrophyRoomIds.SUBMARKET)) return;
        if (market.getSubmarket(ShipTrophyRoomIds.SUBMARKET).getPlugin()
                instanceof TrophyRoomSubmarketPlugin) {
            ((TrophyRoomSubmarketPlugin) market
                    .getSubmarket(ShipTrophyRoomIds.SUBMARKET).getPlugin())
                    .sortStoredShips();
        }
    }
}
