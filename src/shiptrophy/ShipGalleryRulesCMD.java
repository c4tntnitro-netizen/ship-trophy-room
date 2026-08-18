package shiptrophy;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.CommandPlugin;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc.Token;

import shiptrophy.gallery.ShipGalleryDialog;

/** Colony-menu entry point for the Hall of Triumph's Ship Gallery. */
public final class ShipGalleryRulesCMD implements CommandPlugin {
    @Override
    public boolean execute(
            String ruleId,
            InteractionDialogAPI dialog,
            List<Token> params,
            Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params == null || params.isEmpty()) return false;
        String command = params.get(0).getString(memoryMap);
        if ("shouldShow".equals(command)) return shouldShow(dialog);
        if ("show".equals(command)) {
            if (!shouldShow(dialog)) return false;
            ShipGalleryDialog.show(dialog);
            return true;
        }
        return false;
    }

    private static boolean shouldShow(InteractionDialogAPI dialog) {
        SectorEntityToken target = dialog.getInteractionTarget();
        // PopulateOptions also runs inside comm-directory conversations. The
        // Gallery belongs only on the colony's main interaction menu.
        if (target == null || target.getActivePerson() != null) return false;
        MarketAPI market = target.getMarket();
        if (market == null || !market.isPlayerOwned()) return false;
        Industry hall = market.getIndustry(ShipTrophyRoomIds.INDUSTRY);
        return TrophyRoomIndustry.isFunctionalTrophyRoom(hall);
    }

    @Override
    public boolean doesCommandAddOptions() {
        return false;
    }

    @Override
    public int getOptionOrder(
            List<Token> params, Map<String, MemoryAPI> memoryMap) {
        return 0;
    }
}
